package org.jas.plugins.utils.execute.impl;

import org.jas.plugins.utils.execute.IScriptsExecute;

/**
 * Created by jabali on 3/5/17.
 */
public class ScriptsBatchExecuteImplExporter {

    public static IScriptsExecute scriptsBatchProcessor() {
        return new ScriptsBatchExecuteImpl();
    }

}
