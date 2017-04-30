package org.jas.plugins.utils.exceptions;

import org.jas.plugins.utils.FeatureScript;

/**
 * Created by jabali on 3/5/17.
 */
public class FeatureScriptTypeAwareException extends RuntimeException {

    private Class<? extends FeatureScript> script;

    public FeatureScriptTypeAwareException(Throwable cause) {
        super(cause);
    }

    public FeatureScriptTypeAwareException(Class<? extends FeatureScript> script) {
        this.script = script;
    }


    public FeatureScriptTypeAwareException(final Class<? extends FeatureScript> script,
                                           final Throwable cause) {
        super(cause);
        this.script = script;
    }

    public Class<? extends FeatureScript> getScript() {
        return script;
    }
}
