package org.jas.plugins.utils.entity;

import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by jabali on 3/1/17.
 */


@Document(collection = "data_herd_release_info")
public class ReleaseScriptsInfo {


    public ReleaseScriptsInfo() {
    }

    public ReleaseScriptsInfo(double releaseTag,
                              List<Map.Entry<String, ScriptStatusInfo>> scriptsRan,
                              Date scriptsExecutedOn,
                              ScriptExecutionStatusEnum scriptExecutionOverallStatus) {
        this.releaseTag = releaseTag;
        this.scriptsRan = scriptsRan;
        this.scriptsExecutedOn = scriptsExecutedOn;
        this.scriptExecutionOverallStatus = scriptExecutionOverallStatus;
    }

    @Id
    private String id;

    @NotNull
    private double releaseTag;
    @NotNull
    private List<Map.Entry<String, ScriptStatusInfo>> scriptsRan;
    @NotNull
    private Date scriptsExecutedOn;
    @NotNull
    private ScriptExecutionStatusEnum scriptExecutionOverallStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getReleaseTag() {
        return releaseTag;
    }

    public void setReleaseTag(double releaseTag) {
        this.releaseTag = releaseTag;
    }


    public List<Map.Entry<String, ScriptStatusInfo>> getScriptsRan() {
        return scriptsRan;
    }

    public void setScriptsRan(List<Map.Entry<String, ScriptStatusInfo>> scriptsRan) {
        this.scriptsRan = scriptsRan;
    }

    public Date getScriptsExecutedOn() {
        return scriptsExecutedOn;
    }

    public void setScriptsExecutedOn(Date scriptsExecutedOn) {
        this.scriptsExecutedOn = scriptsExecutedOn;
    }

    public ScriptExecutionStatusEnum getScriptExecutionOverallStatus() {
        return scriptExecutionOverallStatus;
    }

    public void setScriptExecutionOverallStatus(ScriptExecutionStatusEnum scriptExecutionOverallStatus) {
        this.scriptExecutionOverallStatus = scriptExecutionOverallStatus;
    }
}
