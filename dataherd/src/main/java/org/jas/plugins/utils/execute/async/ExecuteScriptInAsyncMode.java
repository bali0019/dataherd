package org.jas.plugins.utils.execute.async;

import org.jas.plugins.utils.FeatureScript;
import org.jas.plugins.utils.ReleaseContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.jas.plugins.utils.entity.ScriptExecutionResult;

import java.util.concurrent.Future;

/**
 * Created by jabali on 3/4/17.
 */

@Async("releaseScriptsRunnablesExecutor")
public class ExecuteScriptInAsyncMode {

    public Future<ScriptExecutionResult> runInAsync(FeatureScript featureScript, ReleaseContext releaseContext) {
        return new AsyncResult<>(featureScript.executeWithWrappedException(releaseContext));
    }

}
