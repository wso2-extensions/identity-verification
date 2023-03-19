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
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;

import java.util.List;

/**
 * This interface of IdentityVerifierFactory to retrieve the required identity verifier.
 */
public interface IdentityVerificationManager {

    /**
     * Get the identity verifier data after processing the identity verification.
     *
     * @param userId               User Id.
     * @param identityVerifierData Identity verifier data.
     * @param tenantId             Tenant Id.
     * @return IdentityVerifierData.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException;

    /**
     * Get the IdVClaim.
     *
     * @param userId     User Id.
     * @param idvClaimId IdVClaim Id.
     * @param tenantId   Tenant Id.
     * @return IdVClaim.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    IdVClaim getIdVClaim(String userId, String idvClaimId, int tenantId) throws IdentityVerificationException;

    /**
     * Get the IdVClaim.
     *
     * @param userId        User Id.
     * @param idvClaimUri   IdVClaim uri.
     * @param idVProviderId IdVProvider Id.
     * @param tenantId      Tenant Id.
     * @return IdVClaim.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    IdVClaim getIdVClaim(String userId, String idvClaimUri, String idVProviderId, int tenantId)
            throws IdentityVerificationException;

    /**
     * Get the IdVClaims of a user.
     *
     * @param userId        User Id.
     * @param idvProviderId IdVProvider Id.
     * @param tenantId      Tenant Id.
     * @return IdVClaims.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    IdVClaim[] getIdVClaims(String userId, String idvProviderId, int tenantId) throws IdentityVerificationException;

    /**
     * Add the IdVClaim.
     *
     * @param userId   User Id.
     * @param idvClaim IdVClaim.
     * @param tenantId Tenant Id.
     * @return IdVClaim.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    List<IdVClaim> addIdVClaims(String userId, List<IdVClaim> idvClaim, int tenantId)
            throws IdentityVerificationException;

    /**
     * Update user's IdVClaims.
     *
     * @param userId    User Id.
     * @param idVClaims Identity Verification Claims.
     * @param tenantId  Tenant Id.
     * @return List of updated IdVClaims.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    List<IdVClaim> updateIdVClaims(String userId, List<IdVClaim> idVClaims, int tenantId)
            throws IdentityVerificationException;

    /**
     * Update the IdVClaim.
     *
     * @param idvClaim IdVClaim.
     * @param tenantId Tenant Id.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    IdVClaim updateIdVClaim(String userId, IdVClaim idvClaim, int tenantId) throws IdentityVerificationException;

    /**
     * Delete the IdVClaim.
     *
     * @param idvClaimId IdVClaim Id.
     * @param tenantId   Tenant Id.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    void deleteIDVClaim(String userId, String idvClaimId, int tenantId) throws IdentityVerificationException;

    /**
     * Delete the IdVClaims of a user.
     *
     * @param userId   User Id.
     * @param tenantId Tenant Id.
     * @throws IdentityVerificationException IdentityVerificationException.
     */
    void deleteIDVClaims(String userId, int tenantId) throws IdentityVerificationException;
}
