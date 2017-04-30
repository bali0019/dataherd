package org.jas.plugins.utils.scripts;

import org.apache.log4j.Logger;
import org.jas.plugins.utils.FeatureScript;
import org.jas.plugins.utils.ReleaseContext;
import org.jas.plugins.utils.annotations.ChangeSet;
import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;
import org.jas.plugins.utils.entity.ScriptExecutionResult;
import org.jas.plugins.utils.Systems;

/**
 * Created by jabali on 3/3/17.
 */

@ChangeSet(
        order = 2,
        releaseTag = 1.2,
        description = "Scripts for adding new documents to a collection A.",
        isBlocking = true,
        target = {Systems.MONGO},
        source = {Systems.MONGO})
public class MongoCollectionAScript extends FeatureScript {

    private static final Logger LOGGER = Logger.getLogger(MongoCollectionAScript.class);

    @Override
    public ScriptExecutionResult execute(ReleaseContext releaseContext) {
        LOGGER.info("Update of A Collection complete.");
        return new ScriptExecutionResult(ScriptExecutionStatusEnum.SUCCESS, this.getClass());
    }
}
