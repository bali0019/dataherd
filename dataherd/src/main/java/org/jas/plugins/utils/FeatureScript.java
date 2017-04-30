package org.jas.plugins.utils;


import org.jas.plugins.utils.entity.ScriptExecutionResult;
import org.jas.plugins.utils.exceptions.DataMigrationNullScriptResponseException;
import org.jas.plugins.utils.exceptions.FeatureScriptTypeAwareException;

/**
 * Created by jabali on 3/1/17.
 */
public abstract class FeatureScript {

    public abstract ScriptExecutionResult execute(ReleaseContext releaseContext);

    public ScriptExecutionResult executeWithWrappedException(ReleaseContext releaseContext) {
        try {
            ScriptExecutionResult scriptExecutionResult = execute(releaseContext);
            if(scriptExecutionResult != null) {
                return scriptExecutionResult;
            } else {
                throw new DataMigrationNullScriptResponseException(this.getClass(),
                        "FeatureScript response cannot be null.");
            }
        } catch (Throwable e) {
            throw new FeatureScriptTypeAwareException(this.getClass(), e);
        }
    }

}
