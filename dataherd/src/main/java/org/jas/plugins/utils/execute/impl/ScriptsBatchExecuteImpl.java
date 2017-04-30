package org.jas.plugins.utils.execute.impl;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jas.plugins.utils.FeatureScript;
import org.jas.plugins.utils.ReleaseContext;
import org.jas.plugins.utils.annotations.ChangeSet;
import org.jas.plugins.utils.annotations.EnableDataHerd;
import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;
import org.jas.plugins.utils.entity.MapEntryImpl;
import org.jas.plugins.utils.entity.ReleaseScriptsInfo;
import org.jas.plugins.utils.exceptions.*;
import org.jas.plugins.utils.execute.IScriptsExecute;
import org.jas.plugins.utils.util.DataHerdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.jas.plugins.utils.annotations.IgnoreChangeSet;
import org.jas.plugins.utils.entity.ScriptExecutionResult;
import org.jas.plugins.utils.entity.ScriptStatusInfo;
import org.jas.plugins.utils.execute.async.ExecuteScriptInAsyncMode;
import org.jas.plugins.utils.service.IReleaseScriptsService;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by jabali on 3/1/17.
 */
class ScriptsBatchExecuteImpl implements IScriptsExecute {

    private final static Logger logger = Logger.getLogger(ScriptsBatchExecuteImpl.class);

    @Autowired
    private ExecuteScriptInAsyncMode executeScriptInAsyncMode;

    @Autowired
    @Qualifier("releaseScriptsRunnablesExecutor")
    private ThreadPoolTaskExecutor scriptsExecutor;

    @Autowired
    private IReleaseScriptsService releaseScriptsService;

    @Autowired
    private ReleaseContext releaseContext;

    @Override
    public void applyReleaseScripts() {


        final Set<Class<?>> enabledDataMigrationApps = DataHerdUtil.findScriptComponents(
                "",
                            EnableDataHerd.class);

        final EnableDataHerd configWithEnabledDataMigration = DataHerdUtil.validateEnabledDataMigration(enabledDataMigrationApps);

        if(configWithEnabledDataMigration!=null) {
            try {
                doExecute(configWithEnabledDataMigration);
            } catch (Exception e) {
                logger.error("Error executing data migration scripts", new DataMigrationScriptExecutionException(e));
            }
        }

    }

    private void doExecute(final EnableDataHerd configWithEnabledDataMigration) throws InterruptedException,ExecutionException  {
        // Fetch all release scripts
        logger.info("Scanning dataherd scripts in the classpath");
        final Set<Class<?>> scriptsIncludingIgnored = DataHerdUtil.findScriptComponents(
                                                    configWithEnabledDataMigration.scriptsBasePackage(),
                                                    ChangeSet.class);

        logger.info(String.format("Found total of %s dataherd scripts in the projects classpath",
                scriptsIncludingIgnored==null ? 0 : scriptsIncludingIgnored.size()));

        final Set<Class<?>> scripts = scriptsIncludingIgnored
                        .stream()
                        .filter(item -> {
                            boolean include = item.getAnnotation(IgnoreChangeSet.class) == null;
                            if(!include) {
                                logger.info(String.format("Skipping data migration script with @IgnoreScriptSet turned on: %s",
                                        item.getName()));
                            }
                            return include;
                        })
                        .collect(Collectors.toSet());

        logger.info(String.format("Total dataherd scripts found: %s",
                scripts==null ? 0 : scripts.size()));

        //Attempt downcast and transform candidate scripts into feature script types
        final Set<Class<? extends FeatureScript>> scriptCandidates =  scripts.stream()
                .filter(item -> DataHerdUtil.wrapClassNotFoundExceptionAsRuntime(item).getAnnotation(ChangeSet.class) != null)
                .map(item -> (Class<? extends FeatureScript>) DataHerdUtil.wrapClassNotFoundExceptionAsRuntime(item).asSubclass(FeatureScript.class))
                .collect(Collectors.toSet());

        // Run validations to expect tags such as release and order of scripts execution
        validateExecuteScripts(scriptCandidates);


        final Map<Long, Class<? extends FeatureScript>> orderedScripts = prepareScripts(scriptCandidates);

        if(orderedScripts.size() > 0) {

            final List<Future<ScriptExecutionResult>> nonBlockingScriptsFutureList = new ArrayList<>();

            final Map<Class<? extends FeatureScript>, ScriptStatusInfo> scriptsProcessStatusMap = new HashMap<>();

            //Execute scripts
            orderedScripts
                    .entrySet()
                    .stream()
                    .forEach(item->{
                        try {
                            if(!item.getValue().getAnnotation(ChangeSet.class).isBlocking()) {
                                try {
                                    nonBlockingScriptsFutureList.add(
                                            executeScriptInAsyncMode.runInAsync(item.getValue().newInstance(), releaseContext));
                                    logger.info(String.format("Started Execution of non-blocking dataherd script %s with order %s", item.getValue().getName(), item.getKey()));
                                } catch (Exception e) {
                                    scriptsProcessStatusMap.put(item.getValue(), new ScriptStatusInfo(ScriptExecutionStatusEnum.FAILURE, ExceptionUtils.getStackTrace(e.getCause())));
                                }
                            } else {
                                if(!scriptsProcessStatusMap.containsKey(item.getValue())) {
                                    try {
                                         ScriptExecutionResult scriptExecutionResult =  item.getValue().newInstance().executeWithWrappedException(releaseContext);
                                        if(scriptExecutionResult!=null) {
                                            scriptsProcessStatusMap.put(item.getValue(), new ScriptStatusInfo(scriptExecutionResult.getScriptStatus()));
                                            logger.info(String.format("%s in executing dataherd script with description '%s'",
                                                    scriptExecutionResult.getScriptStatus().name(),
                                                    item.getValue().getAnnotation(ChangeSet.class).description()));
                                        } else {
                                            logger.info(String.format("%s in executing dataherd script with description '%s'",
                                                    ScriptExecutionStatusEnum.FAILURE.name(),
                                                    item.getValue().getAnnotation(ChangeSet.class).description()));
                                            scriptsProcessStatusMap.put(item.getValue(), new ScriptStatusInfo(ScriptExecutionStatusEnum.FAILURE));
                                        }
                                    } catch (Exception e) {
                                        scriptsProcessStatusMap.put(item.getValue(), new ScriptStatusInfo(ScriptExecutionStatusEnum.FAILURE, ExceptionUtils.getStackTrace(e.getCause())));
                                        orderedScripts
                                                .entrySet()
                                                .stream()
                                                .filter(item1 -> item1.getKey() > item.getKey())
                                                .forEach(item1 -> scriptsProcessStatusMap.put(item1.getValue(), new ScriptStatusInfo(ScriptExecutionStatusEnum.SKIPPED)));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            blockUntilScriptsAreProcessed(nonBlockingScriptsFutureList, scriptsProcessStatusMap);

            updateDbWithScriptsStatus(scriptsProcessStatusMap);
        } else {
            final ReleaseScriptsInfo lastReleaseInfo = releaseScriptsService.getLastReleaseTagScriptsRan();
            if(lastReleaseInfo!=null) {
                logger.info("Last dataherd scripts ran for release tag: "+lastReleaseInfo.getReleaseTag());
                logger.info("Found No dataherd scripts to run since release tag: "+lastReleaseInfo.getReleaseTag());
            }
            logger.info("Found No dataherd scripts to run");
            scriptsExecutor.shutdown();
        }

    }

    private void updateDbWithScriptsStatus(final Map<Class<? extends FeatureScript>, ScriptStatusInfo> scriptsProcessStatusMap) {

        // Group scripts by release using Java8 stream grouping
       final Map<Double, List<Map.Entry<Class<? extends FeatureScript>, ScriptStatusInfo>>> groupedByRelease =
                scriptsProcessStatusMap
                .entrySet()
                .stream()
                .collect(Collectors.groupingBy(item -> item.getKey().getAnnotation(ChangeSet.class).releaseTag()));

       groupedByRelease
               .entrySet()
               .stream()
               .forEach(item -> {
                   logger.info(String.format("Total of %s dataherd scripts ran for release tag %s ", item.getValue().size() , item.getKey()));
               });

       // Transform this map into a map with value as list of class names and not classes, since there is no converter to convert from class to class name
       final Map<Double, List<Map.Entry<String, ScriptStatusInfo>>> groupedByReleaseAsScriptsName =
                groupedByRelease
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e->e.getKey(),
                        item-> item.getValue().stream().map(item1 ->
                                new MapEntryImpl<>(item1.getKey().getSimpleName(), item1.getValue()))
                                .collect(Collectors.toList())));
                //.map(item -> item.getValue().stream().map(item1 -> item1.getKey().getName()))

       // If one of the scripts under that release fails, flag overall release scripts execution as failed
        final ReleaseScriptsInfo lastReleaseInfo = releaseScriptsService.getLastReleaseTagScriptsRan();

        groupedByReleaseAsScriptsName.entrySet().stream().forEach(item -> {
           final ScriptExecutionStatusEnum releaseScriptsOverallStatus =
                   item.getValue().stream()
                           .filter(item1 -> item1.getValue().getScriptExecutionStatusEnum() == ScriptExecutionStatusEnum.FAILURE)
                           .map(item1 -> item1.getValue().getScriptExecutionStatusEnum()).collect(Collectors.toList()).size() > 0 ?
                                        ScriptExecutionStatusEnum.FAILURE : ScriptExecutionStatusEnum.SUCCESS;
           // Save the results

            if(lastReleaseInfo!=null && lastReleaseInfo.getReleaseTag() > 0  &&
                    lastReleaseInfo.getReleaseTag() == item.getKey()) {
                lastReleaseInfo.getScriptsRan().addAll(item.getValue());
                lastReleaseInfo.setScriptsExecutedOn(new Date());
                item.getValue().forEach(item2->{
                    if(item2!=null && item2.getValue().getScriptExecutionStatusEnum() == ScriptExecutionStatusEnum.FAILURE) {
                        lastReleaseInfo.setScriptExecutionOverallStatus(ScriptExecutionStatusEnum.FAILURE);
                    }
                });
                releaseScriptsService.save(lastReleaseInfo);
            } else {
                releaseScriptsService.save(new ReleaseScriptsInfo(item.getKey(), item.getValue(), new Date(), releaseScriptsOverallStatus));
            }
       });

        // Group scripts by release using Java8 stream grouping
        scriptsProcessStatusMap
                .entrySet()
                .stream()
                .collect(Collectors.groupingBy(item -> item.getValue().getScriptExecutionStatusEnum()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e->e.getKey(),
                        item-> item.getValue().stream().map(item1 -> item1.getKey().getName())
                                .collect(Collectors.toList())))
                .entrySet()
                .forEach(item -> {
                   // final ScriptExecutionStatusEnum releaseScriptsOverallStatus
                    logger.info(String.format("Following dataherd scripts were %s: %s",item.getKey(),item.getValue()));
                });
    }


    private void blockUntilScriptsAreProcessed(final List<Future<ScriptExecutionResult>> nonBlockingScriptsFutureList,
                                            final Map<Class<? extends FeatureScript>, ScriptStatusInfo> scriptsProcessStatusMap) {
        try {

          outerLoop:  while(true) {

                final Iterator<Future<ScriptExecutionResult>> itr = nonBlockingScriptsFutureList.iterator();

                while (itr.hasNext()) {
                    final Future<ScriptExecutionResult> futureResult = itr.next();
                    try {
                        if (futureResult.isDone()) {
                            logger.info(
                                    String.format("Executed non-blocking dataherd script %s with order %s", futureResult.get().getScriptClass(),
                                            futureResult.get().getScriptClass().getAnnotation(ChangeSet.class).order()));
                            if(futureResult!=null) {
                                ScriptExecutionResult scriptExecutionResult = futureResult.get();
                                if(scriptExecutionResult!=null) {
                                    scriptsProcessStatusMap.put(futureResult.get().getScriptClass(), new ScriptStatusInfo(scriptExecutionResult.getScriptStatus()));
                                    logger.info(String.format("%s in executing dataherd script with description '%s'",
                                            scriptExecutionResult.getScriptStatus(),
                                            futureResult.get().getScriptClass().getAnnotation(ChangeSet.class).description()));
                                } else {
                                    scriptsProcessStatusMap.put(futureResult.get().getScriptClass(), new ScriptStatusInfo(ScriptExecutionStatusEnum.FAILURE));
                                    logger.info(String.format("%s in executing dataherd script with description '%s'",
                                            ScriptExecutionStatusEnum.FAILURE.name(),
                                            futureResult.get().getScriptClass().getAnnotation(ChangeSet.class).description()));
                                }
                            }
                            itr.remove();
                        }
                    } catch (Exception e) {
                        if(e.getCause().getClass()==FeatureScriptTypeAwareException.class) {
                            scriptsProcessStatusMap.put(((FeatureScriptTypeAwareException)e.getCause()).getScript(),
                                    new ScriptStatusInfo(ScriptExecutionStatusEnum.FAILURE, ExceptionUtils.getStackTrace(e.getCause())));
                            logger.error(
                                    String.format("Error Executing non-blocking dataherd script %s with order %s",
                                            ((FeatureScriptTypeAwareException)e.getCause()).getScript(),
                                            ((FeatureScriptTypeAwareException)e.getCause()).getScript().getAnnotation(ChangeSet.class).order()));
                        } else if (e.getCause().getClass() == DataMigrationNullScriptResponseException.class) {
                            scriptsProcessStatusMap.put(((DataMigrationNullScriptResponseException)e.getCause()).getScript(),
                                    new ScriptStatusInfo(ScriptExecutionStatusEnum.FAILURE, ExceptionUtils.getStackTrace(e.getCause())));
                            logger.error(((DataMigrationNullScriptResponseException)e.getCause()).getErrorMessage());
                        }
                        itr.remove();
                    }
                }

                if(nonBlockingScriptsFutureList==null ||
                        nonBlockingScriptsFutureList.size() == 0 || nonBlockingScriptsFutureList.isEmpty()) {
                    break outerLoop;
                } else {
                    logger.info(String.format("Waiting for %s dataherd scripts to complete", nonBlockingScriptsFutureList.size()));
                    Thread.sleep(10000);
                }

            }


            if (nonBlockingScriptsFutureList.isEmpty()) {
                logger.info("All dataherd scripts completed.");
                if (scriptsExecutor.getActiveCount() == 0) {
                    logger.info("Shutting down dataherd scripts' task runner executor.");
                    scriptsExecutor.shutdown();
                } else {
                    while (scriptsExecutor.getActiveCount() > 0) {
                        Thread.sleep(5000);
                        logger.info("Shutting down dataherd tasks runner executor.");
                        scriptsExecutor.shutdown();
                    }
                }
                logger.info("Dataherd task runner executor shutdown complete.");
            }
        } catch (Exception e) {
            logger.error(new DataMigrationScriptExecutionException(e));
        }
    }

    private Map<Long, Class<? extends FeatureScript>> prepareScripts(final Set<Class<? extends FeatureScript>> scriptCandidates) {

        final ReleaseScriptsInfo lastReleaseInfo = releaseScriptsService.getLastReleaseTagScriptsRan();

        if(lastReleaseInfo!=null && lastReleaseInfo.getReleaseTag()>=0) {
            logger.info("Last dataherd release tag ran: " + lastReleaseInfo.getReleaseTag());
            logger.info("Skipping dataherd scripts with tag or lesser that have already been executed: " + lastReleaseInfo.getReleaseTag());
        }

        // Extract all Scripts for all skipped releases to be run
        final Map<Long, Class<? extends FeatureScript>> scripts = scriptCandidates
                .stream()
                .filter(item -> {
                    if(lastReleaseInfo != null && lastReleaseInfo.getReleaseTag()>=0) {
                        if(item.getAnnotation(ChangeSet.class).releaseTag() >= lastReleaseInfo.getReleaseTag()) {
                            if(lastReleaseInfo.getScriptsRan().contains(
                                    new MapEntryImpl<>(item.getSimpleName(), item.getSimpleName()))) {
                                logger.info(String.format("%s Dataherd script is already executed in the last execution for release tag %s",
                                        item.getSimpleName(),
                                        item.getAnnotation(ChangeSet.class).releaseTag()+""));
                            }
                            return !lastReleaseInfo.getScriptsRan().contains(
                                    new MapEntryImpl<>(item.getSimpleName(), item.getSimpleName()));
                        } else {
                            return false;
                        }
                        //return item.getAnnotation(ChangeSet.class).releaseTag() > lastReleaseInfo.getReleaseTag();
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toMap(e -> e.getAnnotation(ChangeSet.class).order(), e->e));

        final Map<Long, Class<? extends FeatureScript>> orderedScripts =
                new TreeMap<>(Comparator.naturalOrder());

        orderedScripts.putAll(scripts);


        logger.info(String.format("Total of %s dataherd scripts will be executed: %s",
                                            scripts==null ? 0 : scripts.size(),
                                            scripts==null ? "" : scripts.toString()));

        return orderedScripts;

    }

    private void validateExecuteScripts(Set<Class<? extends FeatureScript>> scripts) {

        //Validate ChangSet tags
        scripts.stream().forEach(item -> {
            if(item.getAnnotation(ChangeSet.class).order()<=0) {
                throw new InvalidScriptMetaException(item, "order");
            } else if(item.getAnnotation(ChangeSet.class).releaseTag()<=0) {
                throw new InvalidScriptMetaException(item, "releaseTag");
            }
        });

        //validate duplicate order
       final Map<Long, List<Class<? extends FeatureScript>>> orderDuplicateCheckMap = scripts
               .stream()
               .collect(Collectors.groupingBy(item->item.getAnnotation(ChangeSet.class).order()))
               .entrySet()
               .stream()
               .filter(item -> item.getValue().size() > 1)
               .collect(Collectors.toMap(e->e.getKey(), e->e.getValue()));

        if(orderDuplicateCheckMap.size() > 0) {
            throw new DataMigrationValidationException(
                    String.format("Following order number on ChangeSet annotations for dataherd scripts are repeated: %s .",
                            orderDuplicateCheckMap.toString()) +
                            ChangeSet.class.getName() + ".order must be a unique number across dataherd scripts"
            );
        }

    }


}
