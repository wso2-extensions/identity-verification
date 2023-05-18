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
package org.wso2.carbon.extension.identity.verification.mgt;

import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;

/**
 * This interface contains the implementation of IdentityVerifier.
 */
public interface IdentityVerifier {

    /**
     * Verify an identity based on identity verification data. This method can also be used to initiate identity
     * verification and process identity verification.
     *
     * @param userId               User Id.
     * @param identityVerifierData Identity verification data.
     * @param tenantId             Tenant Id.
     * @return IdentityVerifierData.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException;
}
