package org.wso2.carbon.extension.identity.verification.ui.exception;

/**
 * This class represents the server exceptions thrown from the Identity Verification UI component.
 */
public class IdVProviderMgtUIServerException extends IdVProviderMgtUIException {

    public IdVProviderMgtUIServerException(String errorCode, String message) {

        super(errorCode, message);
    }

    public IdVProviderMgtUIServerException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }
}
