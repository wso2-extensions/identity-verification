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
package org.wso2.carbon.extension.identity.verification.mgt.listeners;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManagerImpl;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.tenant.mgt.util.TenantMgtUtil;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.Map;

import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_DELETING_IDV_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_DELETING_IDV_DATA;

/**
 * This listener is to handle IDV related user data.
 */
public class IdVUserOperationEventListener extends AbstractIdentityUserOperationEventListener {

    private static final String IDV_CLAIM_URI_THREAD_LOCAL = "idvClaimURIThreadLocal";

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }

        IdentityUtil.threadLocalProperties.get().remove(IDV_CLAIM_URI_THREAD_LOCAL);
        IdentityUtil.threadLocalProperties.get().put(IDV_CLAIM_URI_THREAD_LOCAL, claimURI);
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (StringUtils.isBlank(userID)) {
            return true;
        }
        return doPostSetUserClaimValueWithID(userID, userStoreManager);
    }

    @Override
    public boolean doPreSetUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        return doPreSetUserClaimValue(null, claimURI, claimValue, profileName, userStoreManager);
    }

    @Override
    public boolean doPostSetUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }

        try {
            String claimURI = (String) IdentityUtil.threadLocalProperties.get().get(IDV_CLAIM_URI_THREAD_LOCAL);
            if (StringUtils.isBlank(claimURI)) {
                return true;
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            IdentityVerificationManagerImpl.getInstance().deleteIDVClaims(userID, null, claimURI, tenantId);
        } catch (IdentityVerificationException e) {
            throw new UserStoreException(String.format(ERROR_DELETING_IDV_DATA.getMessage(), userID),
                    ERROR_DELETING_IDV_DATA.getCode(), e);
        } finally {
            IdentityUtil.threadLocalProperties.get().remove(IDV_CLAIM_URI_THREAD_LOCAL);
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (StringUtils.isBlank(userID)) {
            return true;
        }
        return doPostSetUserClaimValuesWithID(userID, claims, profileName, userStoreManager);
    }

    @Override
    public boolean doPostSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }

        boolean isTenantCreationOperation = TenantMgtUtil.isTenantCreation();
        if (isTenantCreationOperation) {
            return true;
        }

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            IdentityVerificationManagerImpl identityVerificationManager = IdentityVerificationManagerImpl.getInstance();
            for (Map.Entry<String, String> claim : claims.entrySet()) {
                identityVerificationManager.deleteIDVClaims(userID, null, claim.getKey(), tenantId);
            }
        } catch (IdentityVerificationException e) {
            throw new UserStoreException(String.format(ERROR_DELETING_IDV_DATA.getMessage(), userID),
                    ERROR_DELETING_IDV_DATA.getCode(), e);
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUserWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            IdentityVerificationManagerImpl.getInstance().
                    deleteIDVClaims(userID, null, null, tenantId);
        } catch (IdentityVerificationException e) {
            throw new UserStoreException(String.format(ERROR_DELETING_IDV_CLAIMS.getMessage(), userID),
                    ERROR_DELETING_IDV_CLAIMS.getCode(), e);
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (StringUtils.isBlank(userID)) {
            return true;
        }
        return doPostDeleteUserWithID(userID, userStoreManager);
    }

    @Override
    public boolean doPreDeleteUserClaimValuesWithID(String userID, String[] claims, String profileName,
                                                    UserStoreManager userStoreManager) throws UserStoreException {
        if (!isEnable() || userStoreManager == null) {
            return true;
        }

        IdentityUtil.threadLocalProperties.get().remove(IDV_CLAIM_URI_THREAD_LOCAL);
        IdentityUtil.threadLocalProperties.get().put(IDV_CLAIM_URI_THREAD_LOCAL, claims);
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValuesWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }
        try {
            String[] claims = (String[]) IdentityUtil.threadLocalProperties.get().get(IDV_CLAIM_URI_THREAD_LOCAL);
            if (ArrayUtils.isEmpty(claims)) {
                return true;
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            for (String claimURI : claims) {
                IdentityVerificationManagerImpl.getInstance().
                        deleteIDVClaims(userID, null, claimURI, tenantId);
            }
        } catch (IdentityVerificationException e) {
            throw new UserStoreException(String.format(ERROR_DELETING_IDV_DATA.getMessage(), userID),
                    ERROR_DELETING_IDV_DATA.getCode(), e);
        } finally {
            IdentityUtil.threadLocalProperties.get().remove(IDV_CLAIM_URI_THREAD_LOCAL);
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        return doPreDeleteUserClaimValuesWithID(null, claims, profileName, userStoreManager);
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (StringUtils.isBlank(userID)) {
            return true;
        }
        return doPostDeleteUserClaimValuesWithID(userID, userStoreManager);
    }

    @Override
    public boolean doPreDeleteUserClaimValueWithID(String userID, String claimURI, String profileName,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }

        IdentityUtil.threadLocalProperties.get().remove(IDV_CLAIM_URI_THREAD_LOCAL);
        IdentityUtil.threadLocalProperties.get().put(IDV_CLAIM_URI_THREAD_LOCAL, claimURI);
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable() || userStoreManager == null) {
            return true;
        }
        try {
            String claimURI = (String) IdentityUtil.threadLocalProperties.get().get(IDV_CLAIM_URI_THREAD_LOCAL);
            if (StringUtils.isBlank(claimURI)) {
                return true;
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            IdentityVerificationManagerImpl.getInstance().deleteIDVClaims(userID, null, claimURI, tenantId);
        } catch (IdentityVerificationException e) {
            throw new UserStoreException(String.format(ERROR_DELETING_IDV_DATA.getMessage(), userID),
                    ERROR_DELETING_IDV_DATA.getCode(), e);
        } finally {
            IdentityUtil.threadLocalProperties.get().remove(IDV_CLAIM_URI_THREAD_LOCAL);
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        return doPreDeleteUserClaimValueWithID(null, claimURI, profileName, userStoreManager);
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (StringUtils.isBlank(userID)) {
            return true;
        }
        return doPostDeleteUserClaimValueWithID(userID, userStoreManager);
    }

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }

        return 150;
    }
}
