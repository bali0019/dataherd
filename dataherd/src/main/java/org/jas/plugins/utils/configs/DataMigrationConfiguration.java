package org.jas.plugins.utils.configs;

import org.jas.plugins.utils.execute.IScriptsExecute;
import org.jas.plugins.utils.service.impl.ExecutionStatusProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.jas.plugins.utils.ReleaseContext;
import org.jas.plugins.utils.ScriptsExecutionBlockingHealth;
import org.jas.plugins.utils.execute.async.ExecuteScriptInAsyncMode;
import org.jas.plugins.utils.execute.impl.ScriptsBatchExecuteImplExporter;
import org.jas.plugins.utils.listener.ScriptsBatchExecAppStartupListener;
import org.jas.plugins.utils.repository.ReleaseScriptsRepository;
import org.jas.plugins.utils.service.IExecutionStatus;
import org.jas.plugins.utils.service.IReleaseScriptsService;
import org.jas.plugins.utils.service.impl.ReleaseScriptsServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by jabali on 3/1/17.
 */

@EnableAsync
@Configuration
@ConditionalOnProperty(value = "datamigration", havingValue = "enabled")
@ComponentScan(value = "us.deloitteinnovation.utils")
@EnableMongoRepositories(basePackages = "us")
@PropertySource("classpath:application.properties")
public class DataMigrationConfiguration {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ReleaseScriptsRepository releaseScriptsRepository;

    @Bean
    public IScriptsExecute setupExecute() {
        return ScriptsBatchExecuteImplExporter.scriptsBatchProcessor();
    }


    @Bean
    public ScriptsBatchExecAppStartupListener scriptsStartupListener() {
        return new ScriptsBatchExecAppStartupListener(setupExecute());
    }

    @Bean
    public ExecuteScriptInAsyncMode executeScriptInAsyncMode() {
        return new ExecuteScriptInAsyncMode();
    }

    @Bean
    public IReleaseScriptsService releaseScriptsService(){
        return new ReleaseScriptsServiceImpl(releaseScriptsRepository, mongoTemplate);
    }

    @Bean
    public IExecutionStatus executionStatus() {
        List<ThreadPoolTaskExecutor> scriptExecutors = new ArrayList<>();
        scriptExecutors.add(releaseScriptsRunnablesExecutor());
        return new ExecutionStatusProvider(scriptExecutors);
    }

    @Bean
    public AbstractHealthIndicator scriptsHealthCheckStatus() {
        return new ScriptsExecutionBlockingHealth(executionStatus(), releaseScriptsService());
    }



    private static final int KEEP_ALIVE_IN_SECONDS = 1000;
    private static final int QUEUE_CAPACITY = 1000;
    private static final int NO_OF_THREAD_POOLS = 2;
    private static final int SCALE_FACTOR = 4;

    @Bean(name = "releaseScriptsRunnablesExecutor")
    public ThreadPoolTaskExecutor releaseScriptsRunnablesExecutor() {
        return getExecutor();
    }

    private ThreadPoolTaskExecutor getExecutor() {
        int cpus = Runtime.getRuntime().availableProcessors();
        int maxThreads = cpus * SCALE_FACTOR / NO_OF_THREAD_POOLS;
        maxThreads = (maxThreads > 0 ? maxThreads : 1);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setCorePoolSize(cpus);
        executor.setMaxPoolSize(maxThreads);
        executor.setKeepAliveSeconds(KEEP_ALIVE_IN_SECONDS);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        return executor;
    }


    @Bean
    public ReleaseContext releaseContext() {
        return new ReleaseContext();
    }

}
