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

package org.wso2.carbon.extension.identity.verification.mgt.dao;

import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;

import java.util.List;

/**
 * This interface of IdentityVerificationClaimDAO.
 */
public interface IdentityVerificationClaimDAO {

    /**
     * Get priority value for the {@link IdentityVerificationClaimDAO}.
     *
     * @return Priority value for the DAO.
     */
    int getPriority();

    /**
     * Add the identity verification claim.
     *
     * @param idvClaimList IDV claim list.
     * @param tenantId     Tenant id.
     * @throws IdentityVerificationException Identity verification exception.
     */
    void addIdVClaimList(List<IdVClaim> idvClaimList, int tenantId) throws IdentityVerificationException;

    /**
     * Update the identity verification claim by the user id.
     *
     * @param idVClaim IdentityVerificationClaim.
     * @param tenantId Tenant id.
     * @throws IdentityVerificationException Identity verification exception.
     */
    void updateIdVClaim(IdVClaim idVClaim, int tenantId) throws IdentityVerificationException;

    /**
     * Get the identity verification claim by userId and idVClaimId.
     *
     * @param userId     User id.
     * @param idVClaimId Identity verification claim id.
     * @param tenantId   Tenant id.
     * @return Identity verification claim.
     * @throws IdentityVerificationException Identity verification exception.
     */
    IdVClaim getIDVClaim(String userId, String idVClaimId, int tenantId) throws IdentityVerificationException;

    /**
     * Get the identity verification claim.
     *
     * @param userId        User id.
     * @param idvClaimUri   Identity verification claim URI.
     * @param idVProviderId Identity verification provider id.
     * @param tenantId      Tenant id.
     * @return Identity verification claim.
     * @throws IdentityVerificationException Identity verification exception.
     */
    IdVClaim getIDVClaim(String userId, String idvClaimUri, String idVProviderId, int tenantId)
            throws IdentityVerificationException;

    /**
     * Get the identity verification claims.
     *
     * @param userId        User id.
     * @param idvProviderId Identity verification provider id.
     * @param tenantId      Identity verification claim id.
     * @return Identity verification claims.
     * @throws IdentityVerificationException Identity verification exception.
     */
    IdVClaim[] getIDVClaims(String userId, String idvProviderId, int tenantId) throws IdentityVerificationException;

    /**
     * Get the identity verification claims by specific metadata.
     *
     * @param metadataKey   Key of required metadata.
     * @param metadataValue Value of  of required metadata.
     * @param idvProviderId Identity verification provider id.
     * @param tenantId      Tenant id.
     * @return Identity verification claims.
     * @throws IdentityVerificationException Identity verification exception.
     */
    IdVClaim[] getIdVClaimsByMetadata(String metadataKey, String metadataValue, String idvProviderId,
                                      int tenantId) throws IdentityVerificationException;

    /**
     * Delete the identity verification claim.
     *
     * @param userId     User id.
     * @param idVClaimId Identity verification claim id.
     * @param tenantId   Tenant id.
     * @throws IdentityVerificationException Identity verification exception.
     */
    void deleteIdVClaim(String userId, String idVClaimId, int tenantId) throws IdentityVerificationException;

    /**
     * Delete identity verification claims of a user.
     *
     * @param userId    User id.
     * @param idVClaims Identity verification claims.
     * @param tenantId  Tenant id.
     * @throws IdentityVerificationException Identity verification exception.
     */
    void deleteIdVClaims(String userId, IdVClaim[] idVClaims, int tenantId) throws IdentityVerificationException;

    /**
     * Check whether the identity verification claim exist.
     *
     * @param userId        User id.
     * @param idvProviderId Identity verification provider id.
     * @param uri           Claim uri.
     * @param tenantId      Tenant id.
     * @return True if the identity verification claim exist.
     * @throws IdentityVerificationException Identity verification exception.
     */
    boolean isIdVClaimDataExist(String userId, String idvProviderId, String uri, int tenantId)
            throws IdentityVerificationException;

    /**
     * Check whether the identity verification claim exist.
     *
     * @param claimId  Identity verification claim id.
     * @param tenantId Tenant id.
     * @return True if the identity verification claim exist.
     * @throws IdentityVerificationException Identity verification exception.
     */
    boolean isIdVClaimExist(String claimId, int tenantId) throws IdentityVerificationException;
}
