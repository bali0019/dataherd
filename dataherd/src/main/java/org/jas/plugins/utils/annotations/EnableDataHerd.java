package org.jas.plugins.utils.annotations;

import org.springframework.context.annotation.Import;
import org.jas.plugins.utils.configs.DataMigrationConfiguration;

import java.lang.annotation.*;

/**
 * Created by jabali on 3/1/17.
 */


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({DataMigrationConfiguration.class})
public @interface EnableDataHerd {

    String scriptsBasePackage() default "us";

    boolean isBlocking() default true;

}
