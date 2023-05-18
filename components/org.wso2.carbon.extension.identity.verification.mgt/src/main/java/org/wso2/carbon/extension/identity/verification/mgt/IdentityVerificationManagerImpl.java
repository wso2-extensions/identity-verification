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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationClientException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationServerException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationExceptionMgt;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_CODE_GET_DAO;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_GETTING_USER_STORE;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_PROVIDER;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_VERIFIER;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER;

/**
 * This class contains the implementation for the IdentityVerificationService.
 */
public class IdentityVerificationManagerImpl implements IdentityVerificationManager {

    private static final Log log = LogFactory.getLog(IdentityVerificationManagerImpl.class);
    private final List<IdentityVerificationClaimDAO> idVClaimDAOs;

    public IdentityVerificationManagerImpl() {

        this.idVClaimDAOs = IdentityVerificationDataHolder.getInstance().getIdVClaimDAOs();
    }

    /**
     * Select highest priority IdVProvider DAO from an already sorted list of IdVClaim DAOs.
     *
     * @return Highest priority IdVClaim DAO.
     * @throws IdentityVerificationException If an error occurs while getting the IdVClaim DAO.
     */
    private IdentityVerificationClaimDAO getIdVClaimDAO() throws IdentityVerificationException {

        if (!this.idVClaimDAOs.isEmpty()) {
            return idVClaimDAOs.get(idVClaimDAOs.size() - 1);
        } else {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_CODE_GET_DAO);
        }
    }

    @Override
    public IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException {

        validateUserId(userId, tenantId);
        String idVProviderId = identityVerifierData.getIdVProviderId();
        if (StringUtils.isBlank(idVProviderId) || !isValidIdVProviderId(idVProviderId, tenantId)) {
            throw IdentityVerificationExceptionMgt.
                    handleClientException(ERROR_INVALID_IDV_PROVIDER, idVProviderId);
        }
        String idVProviderType = getIdentityVerifierType(idVProviderId, tenantId);

        IdentityVerifierFactory identityVerifierFactory =
                IdentityVerificationDataHolder.getInstance().getIdentityVerifierFactory(idVProviderType);
        if (identityVerifierFactory == null) {
            throw IdentityVerificationExceptionMgt.
                    handleClientException(ERROR_INVALID_IDV_VERIFIER, idVProviderId);
        }
        IdentityVerifier identityVerifier = identityVerifierFactory.getIdentityVerifier(idVProviderType);
        if (identityVerifier == null) {
            throw IdentityVerificationExceptionMgt.
                    handleClientException(ERROR_INVALID_IDV_VERIFIER, idVProviderId);
        }
        return identityVerifier.verifyIdentity(userId, identityVerifierData, tenantId);
    }

    @Override
    public IdVClaim getIdVClaim(String userId, String idvClaimId, int tenantId) throws IdentityVerificationException {

        validateIdVClaimId(idvClaimId, tenantId);
        validateUserId(userId, tenantId);
        return getIdVClaimDAO().getIDVClaim(userId, idvClaimId, tenantId);
    }

    @Override
    public IdVClaim getIdVClaim(String userId, String idvClaimUri, String idVProviderId, int tenantId)
            throws IdentityVerificationException {

        validateUserId(userId, tenantId);
        if (StringUtils.isNotBlank(idVProviderId)) {
            if (!isValidIdVProviderId(idVProviderId, tenantId)) {
                throw IdentityVerificationExceptionMgt.handleClientException(ERROR_INVALID_IDV_PROVIDER, idVProviderId);
            }
        }
        return getIdVClaimDAO().getIDVClaim(userId, idvClaimUri, idVProviderId, tenantId);
    }

    @Override
    public List<IdVClaim> addIdVClaims(String userId, List<IdVClaim> idVClaims, int tenantId)
            throws IdentityVerificationException {

        validateUserId(userId, tenantId);
        for (IdVClaim idVClaim : idVClaims) {
            // Set uuid for each identity verification claim.
            idVClaim.setUuid(UUID.randomUUID().toString());

            // Validate the identity verification claim.
            validateIdVClaimInputs(userId, idVClaim, tenantId);
        }
        getIdVClaimDAO().addIdVClaimList(idVClaims, tenantId);
        return idVClaims;
    }

    @Override
    public List<IdVClaim> updateIdVClaims(String userId, List<IdVClaim> idVClaims, int tenantId)
            throws IdentityVerificationException {

        validateUserId(userId, tenantId);
        deleteIDVClaims(userId, tenantId);
        for (IdVClaim idVClaim : idVClaims) {
            // Set uuid for each identity verification claim.
            idVClaim.setUuid(UUID.randomUUID().toString());

            // Validate the identity verification claim.
            validateIdVClaimInputs(userId, idVClaim, tenantId);
        }
        getIdVClaimDAO().addIdVClaimList(idVClaims, tenantId);
        return idVClaims;
    }

    @Override
    public IdVClaim updateIdVClaim(String userId, IdVClaim idvClaim, int tenantId)
            throws IdentityVerificationException {

        String idvClaimId = idvClaim.getUuid();
        validateIdVClaimId(idvClaimId, tenantId);
        validateUserId(userId, tenantId);
        if (idvClaim.getMetadata() == null) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_EMPTY_CLAIM_METADATA);
        }
        getIdVClaimDAO().updateIdVClaim(idvClaim, tenantId);
        return idvClaim;
    }

    @Override
    public void deleteIDVClaim(String userId, String idvClaimId, int tenantId) throws IdentityVerificationException {

        validateUserId(userId, tenantId);
        getIdVClaimDAO().deleteIdVClaim(userId, idvClaimId, tenantId);
    }

    @Override
    public void deleteIDVClaims(String userId, int tenantId) throws IdentityVerificationException {

        validateUserId(userId, tenantId);
        IdVClaim[] idVClaims = getIdVClaims(userId, null, tenantId);
        getIdVClaimDAO().deleteIdVClaims(userId, idVClaims, tenantId);
    }

    @Override
    public IdVClaim[] getIdVClaims(String userId, String idvProviderId, int tenantId)
            throws IdentityVerificationException {

        validateUserId(userId, tenantId);
        if (StringUtils.isNotBlank(idvProviderId)) {
            if (!isValidIdVProviderId(idvProviderId, tenantId)) {
                throw IdentityVerificationExceptionMgt.handleClientException(ERROR_INVALID_IDV_PROVIDER, idvProviderId);
            }
        }
        return getIdVClaimDAO().getIDVClaims(userId, idvProviderId, tenantId);
    }

    private void validateIdVClaimId(String idvClaimId, int tenantId) throws IdentityVerificationException {

        if (StringUtils.isBlank(idvClaimId) || !isIdVClaimExists(idvClaimId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_CLAIM_ID, idvClaimId);
        }
    }

    private void validateUserId(String userId, int tenantId)
            throws IdentityVerificationServerException, IdentityVerificationClientException {

        if (StringUtils.isBlank(userId) || !isValidUserId(userId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(ERROR_INVALID_USER_ID, userId);
        }
    }

    private void validateIdVClaimInputs(String userId, IdVClaim idVClaim, int tenantId)
            throws IdentityVerificationException {

        String idvProviderId = idVClaim.getIdVPId();
        String claimUri = idVClaim.getClaimUri();
        if (StringUtils.isBlank(idvProviderId) || !isValidIdVProviderId(idvProviderId, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(ERROR_INVALID_IDV_PROVIDER, idvProviderId);
        }
        if (StringUtils.isBlank(claimUri)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_CLAIM_URI, claimUri);
        }
        if (getIdVClaimDAO().isIdVClaimDataExist(userId, idvProviderId, claimUri, tenantId)) {
            throw IdentityVerificationExceptionMgt.handleClientException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_IDV_CLAIM_DATA_ALREADY_EXISTS, userId);
        }
    }

    private boolean isValidIdVProviderId(String idvProviderId, int tenantId) throws IdentityVerificationException {

        try {
            if (IdentityVerificationDataHolder.getInstance().
                    getIdVProviderManager().isIdVProviderExists(idvProviderId, tenantId)) {
                return true;
            }
        } catch (IdVProviderMgtException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_VALIDATING_IDV_PROVIDER_ID, idvProviderId, e);
        }
        return false;
    }

    private boolean isValidUserId(String userId, int tenantId) throws IdentityVerificationServerException {

        UniqueIDUserStoreManager uniqueIDUserStoreManager;
        try {
            uniqueIDUserStoreManager =
                    getUniqueIdEnabledUserStoreManager(IdentityVerificationDataHolder.getInstance().getRealmService(),
                            IdentityTenantUtil.getTenantDomain(tenantId));
            if (uniqueIDUserStoreManager.isExistingUserWithID(userId)) {
                if (log.isDebugEnabled()) {
                    log.debug("User exists with the given user id: " + userId);
                }
                return true;
            }
        } catch (UserStoreException e) {
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().
                    contains(ERROR_CODE_NON_EXISTING_USER.getCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("User does not exist with the given user id: " + userId);
                }
                return false;
            }
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_CHECKING_USER_ID_EXISTENCE, userId, e);
        } catch (IdentityVerificationServerException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_CHECKING_USER_ID_EXISTENCE, userId, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("User does not exist with the given user id: " + userId);
        }
        return false;
    }

    private boolean isIdVClaimExists(String idVClaimId, int tenantId) throws IdentityVerificationException {

        return getIdVClaimDAO().isIdVClaimExist(idVClaimId, tenantId);
    }

    private UniqueIDUserStoreManager getUniqueIdEnabledUserStoreManager(RealmService realmService, String tenantDomain)
            throws IdentityVerificationServerException, UserStoreException {

        UserStoreManager userStoreManager = realmService.getTenantUserRealm(
                IdentityTenantUtil.getTenantId(tenantDomain)).getUserStoreManager();
        if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
            if (log.isDebugEnabled()) {
                log.debug("Provided user store manager does not support unique user IDs.");
            }
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_GETTING_USER_STORE);
        }
        return (UniqueIDUserStoreManager) userStoreManager;
    }

    private static String getIdentityVerifierType(String identityVerifierId, int tenantId)
            throws IdentityVerificationServerException {

        String identityVerifierType;
        try {
            IdVProvider idvProvider = IdentityVerificationDataHolder.getInstance().
                    getIdVProviderManager().getIdVProvider(identityVerifierId, tenantId);
            identityVerifierType = idvProvider.getType();
        } catch (IdVProviderMgtException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_VALIDATING_IDV_PROVIDER_ID, identityVerifierId, e);
        }
        return identityVerifierType;
    }
}
