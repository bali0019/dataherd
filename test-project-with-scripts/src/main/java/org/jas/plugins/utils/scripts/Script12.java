package org.jas.plugins.utils.scripts;

import org.apache.log4j.Logger;
import org.jas.plugins.utils.FeatureScript;
import org.jas.plugins.utils.ReleaseContext;
import org.jas.plugins.utils.Systems;
import org.jas.plugins.utils.annotations.ChangeSet;
import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;
import org.jas.plugins.utils.entity.ScriptExecutionResult;

/**
 * Created by jabali on 3/4/17.
 */

@ChangeSet(order = 3,
        releaseTag = 1.4,
        description = "Update ACL entries on DAAP to share existing artifacts with a new service account user",
        isBlocking = false,
        target = {Systems.MONGO},
        source = {Systems.MONGO})
public class Script12 extends FeatureScript {

    private static final Logger LOGGER = Logger.getLogger(Script12.class);

    @Override
    public ScriptExecutionResult execute(ReleaseContext releaseContext) {
        try {
            Thread.sleep(10000);
            LOGGER.info("Ran Mongo Aggregation pipeline to generate a new collection compatible for a new release.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LOGGER.info("Ran script: "+this.getClass().getName());
        return new ScriptExecutionResult(ScriptExecutionStatusEnum.FAILURE, this.getClass());
    }
}


