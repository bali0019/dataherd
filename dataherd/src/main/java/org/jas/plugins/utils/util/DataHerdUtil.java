package org.jas.plugins.utils.util;

import org.apache.log4j.Logger;
import org.jas.plugins.utils.exceptions.DataMigrationValidationException;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.jas.plugins.utils.annotations.EnableDataHerd;

import java.util.Set;

/**
 * Created by jabali on 3/6/17.
 */
public class DataHerdUtil {

    private final static Logger logger = Logger.getLogger(DataHerdUtil.class);

    private DataHerdUtil() {
    }


    public static Set<Class<?>> findScriptComponents(final String basePackage, final Class annotationType) {

       ConfigurationBuilder cb =  new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(basePackage))
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner())
                .filterInputsBy(new FilterBuilder().includePackage(basePackage));

       // Allow reflections to use its own configuration builder
        // but the above builde can be configured as desired
        Reflections reflections = new Reflections();

        Set<Class<?>> candidates = reflections.getTypesAnnotatedWith(annotationType);

       if(reflections.getConfiguration().getExecutorService()!=null)    {
           reflections.getConfiguration().getExecutorService().shutdown();
       }

       return candidates;

    }

    public static EnableDataHerd validateEnabledDataMigration(final Set<Class<?>> enabledDataMigrationApps) {
        if(enabledDataMigrationApps.size() > 1) {
            throw new DataMigrationValidationException("EnableDataHerd annotation is enabled more than once.");
        } else if(enabledDataMigrationApps.size()==1) {
            for(Class<?> item : enabledDataMigrationApps) {
                return wrapClassNotFoundExceptionAsRuntime(item).getAnnotation(EnableDataHerd.class);
            }
        } else {
            logger.info("EnableDataHerd annotation is not enabled for the app. Skipping scripts setup");
        }
        return null;
    }

    public static Class<?> wrapClassNotFoundExceptionAsRuntime(final Class<?> type) {
        try {
            return Class.forName(type.getName());
        } catch (ClassNotFoundException e) {
            throw new DataMigrationValidationException(e);
        }
    }
}
