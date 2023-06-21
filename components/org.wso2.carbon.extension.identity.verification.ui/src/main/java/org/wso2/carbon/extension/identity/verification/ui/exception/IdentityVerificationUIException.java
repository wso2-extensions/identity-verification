package org.wso2.carbon.extension.identity.verification.ui.exception;

import org.wso2.carbon.identity.base.IdentityException;

/**
 * This class represents the exceptions thrown from the Identity Verification UI component.
 */
public class IdentityVerificationUIException extends IdentityException {


    public IdentityVerificationUIException(String errorCode, String message) {

        super(errorCode, message);
    }

    public IdentityVerificationUIException(String errorCode, String message, Throwable cause) {

        super(errorCode, message, cause);
    }
}
