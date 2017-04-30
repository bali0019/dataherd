package org.jas.plugins.utils.entity;

import org.jas.plugins.utils.FeatureScript;
import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;

/**
 * Created by jabali on 3/4/17.
 */
public class ScriptExecutionResult {

    public ScriptExecutionResult(ScriptExecutionStatusEnum scriptStatus, Class<? extends FeatureScript> scriptClass) {
        this.scriptStatus = scriptStatus;
        this.scriptClass = scriptClass;
    }

    private ScriptExecutionStatusEnum scriptStatus;
    private Class<? extends FeatureScript> scriptClass;

    public ScriptExecutionStatusEnum getScriptStatus() {
        return scriptStatus;
    }

    public void setScriptStatus(ScriptExecutionStatusEnum scriptStatus) {
        this.scriptStatus = scriptStatus;
    }

    public Class<? extends FeatureScript> getScriptClass() {
        return scriptClass;
    }

    public void setScriptClass(Class<? extends FeatureScript> scriptClass) {
        this.scriptClass = scriptClass;
    }
}
