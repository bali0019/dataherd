package org.jas.plugins.utils.entity;

import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;

/**
 * Created by jabali on 3/4/17.
 */
public class ScriptStatusInfo {

    public ScriptStatusInfo() {
    }

    public ScriptStatusInfo(ScriptExecutionStatusEnum scriptExecutionStatusEnum) {
        this.scriptExecutionStatusEnum = scriptExecutionStatusEnum;
    }

    public ScriptStatusInfo(ScriptExecutionStatusEnum scriptExecutionStatusEnum, String details) {
        this.scriptExecutionStatusEnum = scriptExecutionStatusEnum;
        this.details = details;
    }

    private ScriptExecutionStatusEnum scriptExecutionStatusEnum;
    private String details;

    public ScriptExecutionStatusEnum getScriptExecutionStatusEnum() {
        return scriptExecutionStatusEnum;
    }

    public void setScriptExecutionStatusEnum(ScriptExecutionStatusEnum scriptExecutionStatusEnum) {
        this.scriptExecutionStatusEnum = scriptExecutionStatusEnum;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
