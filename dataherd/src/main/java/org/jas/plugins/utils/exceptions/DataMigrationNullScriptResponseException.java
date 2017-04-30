package org.jas.plugins.utils.exceptions;

import org.jas.plugins.utils.FeatureScript;

/**
 * Created by jabali on 3/6/17.
 */
public class DataMigrationNullScriptResponseException extends FeatureScriptTypeAwareException {

    private String errorMessage;

    public DataMigrationNullScriptResponseException(Throwable cause) {
        super(cause);
    }

    public DataMigrationNullScriptResponseException(Class<? extends FeatureScript> script, Throwable cause) {
        super(script, cause);
    }


    public DataMigrationNullScriptResponseException(Class<? extends FeatureScript> script, String errorMessage) {
        super(script);
        this.errorMessage=errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
