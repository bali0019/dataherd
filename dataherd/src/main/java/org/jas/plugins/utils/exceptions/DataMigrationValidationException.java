package org.jas.plugins.utils.exceptions;

import org.jas.plugins.utils.FeatureScript;

/**
 * Created by jabali on 3/1/17.
 */
public class DataMigrationValidationException extends RuntimeException {

    public DataMigrationValidationException(Throwable cause) {
        super(cause);
    }

    public DataMigrationValidationException(Class<? extends FeatureScript> errorScript) {
        super(String.format("Error in %s script", errorScript.getClass().getName()));
    }

    public DataMigrationValidationException(String errorMessage) {
        super(errorMessage);
    }

}
