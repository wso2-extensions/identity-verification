/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
 * This class is used to manage the exceptions thrown from the Identity Verification Provider Management UI component.
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
