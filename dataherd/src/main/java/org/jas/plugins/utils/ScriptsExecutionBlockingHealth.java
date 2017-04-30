package org.jas.plugins.utils;

import org.apache.log4j.Logger;
import org.jas.plugins.utils.annotations.EnableDataHerd;
import org.jas.plugins.utils.constant.ScriptExecutionStatusEnum;
import org.jas.plugins.utils.entity.ReleaseScriptsInfo;
import org.jas.plugins.utils.service.IExecutionStatus;
import org.jas.plugins.utils.util.DataHerdUtil;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.jas.plugins.utils.service.IReleaseScriptsService;

import java.util.Set;

/**
 * Created by jabali on 3/6/17.
 */
public class ScriptsExecutionBlockingHealth extends AbstractHealthIndicator {

    private Logger logger = Logger.getLogger(ScriptsExecutionBlockingHealth.class);

    private IExecutionStatus scriptsStatusProvider;

    private IReleaseScriptsService releaseScriptsService;


    public ScriptsExecutionBlockingHealth(
            IExecutionStatus scriptsStatusProvider,
            IReleaseScriptsService releaseScriptsService) {
        this.scriptsStatusProvider = scriptsStatusProvider;
        this.releaseScriptsService = releaseScriptsService;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        final Set<Class<?>> enabledDataMigrationApps = DataHerdUtil.findScriptComponents(
                                    "us",
                                                 EnableDataHerd.class);

        final EnableDataHerd configWithEnabledDataMigration = DataHerdUtil.validateEnabledDataMigration(enabledDataMigrationApps);

        final ReleaseScriptsInfo lastReleaseInfo = releaseScriptsService.getLastReleaseTagScriptsRan();

        if(configWithEnabledDataMigration!=null && configWithEnabledDataMigration.isBlocking()) {
            if (scriptsStatusProvider.status() == ScriptExecutionStatusEnum.COMPLETED) {
                if(lastReleaseInfo != null && lastReleaseInfo.getScriptExecutionOverallStatus() == ScriptExecutionStatusEnum.SUCCESS) {
                    builder.up();
                } else {
                    logger.info("Health status is DOWN as dataherd scripts did not execute successfully. Check scripts db entity and logs for details.");
                    builder.down();
                }
            } else {
                logger.info("Health status is DOWN as dataherd scripts are still being executed");
                builder.down();
            }
        }
    }

}
