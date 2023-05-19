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
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationServerException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationExceptionMgt;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_GETTING_USER_STORE;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER;

/**
 * This is the abstract class of IdentityVerifier.
 */
public abstract class AbstractIdentityVerifier implements IdentityVerifier {

    private static final Log log = LogFactory.getLog(AbstractIdentityVerifier.class);

    /**
     * Get Identity Verification Provider by identity verifier data.
     *
     * @param identityVerifierData Identity verifier data.
     * @param tenantId             Tenant Id.
     * @return IdentityVerificationProvider.
     * @throws IdentityVerificationServerException IdentityVerificationServerException.
     */
    public IdVProvider getIdVProvider(IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationServerException {

        try {
            String idVProviderId = identityVerifierData.getIdVProviderId();
            return IdentityVerificationDataHolder.getInstance().
                    getIdVProviderManager().getIdVProvider(idVProviderId, tenantId);
        } catch (IdVProviderMgtException e) {
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER, e);
        }
    }

    /**
     * Get the claims that need to be verified along with the values of those claims. This will return a map with IDV
     * provider's claim name as the key of the map and the value of the claim as the value of the map.
     *
     * @param userId               User Id.
     * @param identityVerifierData Identity verifier data.
     * @param tenantId             Tenant Id.
     * @return Map of Idv Claims with values.
     */
    public Map<String, String> getIdVPClaimWithValueMap(String userId, IdentityVerifierData identityVerifierData,
                                                        int tenantId)
            throws IdentityVerificationServerException {

        List<IdVClaim> idVClaimList = identityVerifierData.getIdVClaims();
        Map<String, String> idVPClaimWithValueMap = new HashMap<>();
        UniqueIDUserStoreManager uniqueIDUserStoreManager;
        try {
            uniqueIDUserStoreManager = getUniqueIdEnabledUserStoreManager(tenantId);
            for (IdVClaim idVClaim : idVClaimList) {
                String claimUri = idVClaim.getClaimUri();
                String claimValue = uniqueIDUserStoreManager.getUserClaimValueWithID(userId, claimUri, null);
                idVPClaimWithValueMap.put(idVClaim.getClaimUri(), claimValue);
            }
            return idVPClaimWithValueMap;

        } catch (UserStoreException e) {
            if (StringUtils.isNotBlank(e.getMessage()) &&
                    e.getMessage().contains(ERROR_CODE_NON_EXISTING_USER.getCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("User does not exist with the given user id: " + userId);
                }
            }
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_RETRIEVING_IDV_CLAIM_MAPPINGS, userId, e);
        }
    }

    /**
     * Get config property map of Identity Verification Provider.
     *
     * @param idVProvider Identity Verification Provider.
     * @return Config property map of Identity Verification Provider
     */
    public Map<String, String> getIdVConfigPropertyMap(IdVProvider idVProvider) {

        IdVConfigProperty[] idVConfigProperties = idVProvider.getIdVConfigProperties();
        Map<String, String> configPropertyMap = new HashMap<>();
        for (IdVConfigProperty idVConfigProperty : idVConfigProperties) {
            configPropertyMap.put(idVConfigProperty.getName(), idVConfigProperty.getValue());
        }
        return configPropertyMap;
    }

    /**
     * Store Identity Verification Claims.
     *
     * @param userId    User Id.
     * @param idVClaims List of IdVClaim.
     * @param tenantId  Tenant Id.
     * @return Local and IdVProvider claim Map.
     */
    public List<IdVClaim> storeIdVClaims(String userId, List<IdVClaim> idVClaims, int tenantId)
            throws IdentityVerificationException {

        IdentityVerificationManager identityVerificationManager = new IdentityVerificationManagerImpl();
        return identityVerificationManager.addIdVClaims(userId, idVClaims, tenantId);
    }

    /**
     * Update Identity Verification Claim.
     *
     * @param userId   User Id.
     * @param idvClaim IdVClaim.
     * @param tenantId Tenant Id.
     * @return Local and IdVProvider claim Map.
     */
    public IdVClaim updateIdVClaim(String userId, IdVClaim idvClaim, int tenantId)
            throws IdentityVerificationException {

        IdentityVerificationManager identityVerificationManager = new IdentityVerificationManagerImpl();
        return identityVerificationManager.updateIdVClaim(userId, idvClaim, tenantId);
    }

    /**
     * Update Identity Verification Claims of a user.
     *
     * @param userId    User Id.
     * @param idVClaims Identity Verification claims.
     * @param tenantId  Tenant Id.
     * @return Local and IdVProvider claim Map.
     */
    public List<IdVClaim> updateIdVClaims(String userId, List<IdVClaim> idVClaims, int tenantId)
            throws IdentityVerificationException {

        IdentityVerificationManager identityVerificationManager = new IdentityVerificationManagerImpl();
        return identityVerificationManager.updateIdVClaims(userId, idVClaims, tenantId);
    }

    private UniqueIDUserStoreManager getUniqueIdEnabledUserStoreManager(int tenantId)
            throws IdentityVerificationServerException, UserStoreException {

        RealmService realmService = IdentityVerificationDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_GETTING_USER_STORE);
        }
        return (UniqueIDUserStoreManager) userStoreManager;
    }
}
