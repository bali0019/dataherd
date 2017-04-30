package org.jas.plugins.utils.scripts;

import org.apache.log4j.Logger;
import org.jas.plugins.utils.FeatureScript;
import org.jas.plugins.utils.ReleaseContext;
import org.jas.plugins.utils.annotations.ChangeSet;
import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;
import org.jas.plugins.utils.entity.ScriptExecutionResult;
import org.jas.plugins.utils.Systems;

/**
 * Created by jabali on 3/4/17.
 */

@ChangeSet(order = 6,
        releaseTag = 1.4,
        description = "Update ACL entries on DAAP to share existing artifacts with a new service account user",
        isBlocking = false,
        target = {Systems.MONGO},
        source = {Systems.MONGO})
public class Script15 extends FeatureScript {

    private static final Logger LOGGER = Logger.getLogger(Script15.class);



    @Override
    public ScriptExecutionResult execute(ReleaseContext releaseContext) {
        try {
            Thread.sleep(16000);
            LOGGER.info("Transformed existing classification datasets to add CST columns");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ScriptExecutionResult(ScriptExecutionStatusEnum.SUCCESS, this.getClass());
    }

}
