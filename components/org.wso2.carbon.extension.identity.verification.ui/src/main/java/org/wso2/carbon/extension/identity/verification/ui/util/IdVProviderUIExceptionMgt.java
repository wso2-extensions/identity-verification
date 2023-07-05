package org.wso2.carbon.extension.identity.verification.ui.util;

import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.ui.exception.IdVProviderMgtUIClientException;
import org.wso2.carbon.extension.identity.verification.ui.exception.IdVProviderMgtUIException;
import org.wso2.carbon.extension.identity.verification.ui.exception.IdVProviderMgtUIServerException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.user.api.UserStoreClientException;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * This class is used to manage the exceptions thrown from the Identity Verification UI component.
 */
public class IdVProviderUIExceptionMgt {

    private IdVProviderUIExceptionMgt() {
        // Adding a private constructor to hide the implicit public constructor since all methods are static.
    }

    /**
     * Wraps the IdVProviderMgtException with the correct subtype of IdVProviderMgtUIException.
     *
     * @param e IdVProviderMgtException
     * @return The correct subtype of IdVProviderMgtUIException.
     */
    public static IdVProviderMgtUIException handleException(IdVProviderMgtException e) {

        if (e instanceof IdVProviderMgtClientException) {
            return new IdVProviderMgtUIClientException(e.getErrorCode(), e.getMessage(), e);
        }

        return new IdVProviderMgtUIServerException(e.getErrorCode(), e.getMessage(), e);
    }

    /**
     * Wrap the ClaimMetadataException with the correct subtype of IdVProviderMgtUIException.
     *
     * @param e ClaimMetadataException
     * @return The correct subtype of IdVProviderMgtUIException.
     */
    public static IdVProviderMgtUIException handleException(ClaimMetadataException e) {

        if (e instanceof ClaimMetadataClientException) {
            return new IdVProviderMgtUIClientException(e.getErrorCode(), e.getMessage(), e);
        }

        return new IdVProviderMgtUIServerException(e.getErrorCode(), e.getMessage(), e);
    }


    /**
     * Wraps the UserStoreException with the correct subtype of IdVProviderMgtUIException.
     *
     * @param e UserStoreException
     * @return The correct subtype of IdVProviderMgtUIException.
     */
    public static IdVProviderMgtUIException handleException(UserStoreException e) {

        String errorCode = IdVProviderUIConstants.ErrorMessages.ERROR_UNEXPECTED.getCode();
        if (e instanceof UserStoreClientException) {
            errorCode = ((UserStoreClientException) e).getErrorCode();
            return new IdVProviderMgtUIClientException(errorCode, e.getMessage(), e);
        }

        return new IdVProviderMgtUIServerException(errorCode, e.getMessage(), e);

    }
}
