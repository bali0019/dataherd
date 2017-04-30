package org.jas.plugins.utils.listener;

import org.jas.plugins.utils.execute.IScriptsExecute;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Created by jabali on 3/2/17.
 */
public class ScriptsBatchExecAppStartupListener {

    private IScriptsExecute scriptsExecute;

    public ScriptsBatchExecAppStartupListener(IScriptsExecute scriptsExecute) {
        this.scriptsExecute = scriptsExecute;
    }

    @EventListener
    @Order(value = Ordered.HIGHEST_PRECEDENCE)
    public void start(ApplicationReadyEvent applicationReadyEvent) {
        this.scriptsExecute.applyReleaseScripts();
    }

}
