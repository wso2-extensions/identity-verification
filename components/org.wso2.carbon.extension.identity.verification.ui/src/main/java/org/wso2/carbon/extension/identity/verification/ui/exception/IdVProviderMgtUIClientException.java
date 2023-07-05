package org.wso2.carbon.extension.identity.verification.ui.exception;

/**
 * This class represents the exceptions thrown from the Identity Verification UI component.
 */
public class IdVProviderMgtUIClientException extends IdVProviderMgtUIException {

    public IdVProviderMgtUIClientException(String errorCode, String message) {
        super(errorCode, message);
    }

    public IdVProviderMgtUIClientException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }
}
