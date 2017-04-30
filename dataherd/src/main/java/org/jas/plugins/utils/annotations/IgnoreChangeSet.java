package org.jas.plugins.utils.annotations;

import org.jas.plugins.utils.configs.DataMigrationConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by jabali on 3/7/17.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({DataMigrationConfiguration.class})
public @interface IgnoreChangeSet {
}
