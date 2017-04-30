package org.jas.plugins.utils.scripts;

import org.apache.log4j.Logger;
import org.jas.plugins.utils.annotations.ChangeSet;
import org.jas.plugins.utils.FeatureScript;
import org.jas.plugins.utils.ReleaseContext;
import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;
import org.jas.plugins.utils.entity.ScriptExecutionResult;
import org.jas.plugins.utils.Systems;

/**
 * Created by jabali on 3/4/17.
 */

@ChangeSet(order = 5,
        releaseTag = 1.3,
        description = "Update ACL entries on DAAP to share existing artifacts with a new service account user",
        isBlocking = false,
        target = {Systems.MONGO},
        source = {Systems.MONGO}
        )
public class Script14 extends FeatureScript {

    private static final Logger LOGGER = Logger.getLogger(Script14.class);


    @Override
    public ScriptExecutionResult execute(ReleaseContext releaseContext) {
        try {
            Thread.sleep(15000);
            LOGGER.info("Completed execution of dataflow on DAAP to copy artifacts to a new path");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ScriptExecutionResult(ScriptExecutionStatusEnum.SUCCESS, this.getClass());
    }
}
