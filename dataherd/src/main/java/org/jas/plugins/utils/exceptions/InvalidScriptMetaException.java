package org.jas.plugins.utils.exceptions;

import org.jas.plugins.utils.FeatureScript;

/**
 * Created by jabali on 3/1/17.
 */
public class InvalidScriptMetaException extends RuntimeException {

    public InvalidScriptMetaException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidScriptMetaException(Class<? extends FeatureScript> errorInScript, String changeSetElement) {
        super(String.format("Error in %s ChangeSet annotation's element in %s script.", changeSetElement, errorInScript.getClass().getName()));
    }
}
