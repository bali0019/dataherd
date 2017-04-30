package org.jas.plugins.utils.annotations;

import org.springframework.context.annotation.Import;
import org.jas.plugins.utils.configs.DataMigrationConfiguration;
import org.jas.plugins.utils.Systems;

import java.lang.annotation.*;

/**
 * Created by jabali on 3/1/17.
 */


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({DataMigrationConfiguration.class})
public @interface ChangeSet {

    long order();

    double releaseTag();

    String description() default "";

    boolean isBlocking() default true;

    Systems[] target();

    Systems[] source();

}
