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

package org.wso2.carbon.extension.identity.verification.ui.client;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManagerImpl;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.extension.identity.verification.ui.exception.IdVProviderMgtUIException;
import org.wso2.carbon.extension.identity.verification.ui.internal.IdVProviderMgtUIDataHolder;
import org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIExceptionMgt;
import org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

import java.util.List;

import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.PERMISSION_IDVP_MGT_ADD;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.PERMISSION_IDVP_MGT_DELETE;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.PERMISSION_IDVP_MGT_UPDATE;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.PERMISSION_IDVP_MGT_VIEW;

/**
 * OSGI Client for Identity Verification Provider Management Service.
 */
public class IdVProviderMgtServiceClientImpl implements IdVProviderMgtServiceClient {
    private final IdVProviderManager idVProviderManager;

    private IdVProviderMgtServiceClientImpl() {

        idVProviderManager = new IdVProviderManagerImpl();
    }

    private static class IdVProviderMgtServiceClientImplHolder {

        static final IdVProviderMgtServiceClientImpl INSTANCE = new IdVProviderMgtServiceClientImpl();
    }

    public static IdVProviderMgtServiceClient getInstance() {

        return IdVProviderMgtServiceClientImplHolder.INSTANCE;
    }

    @Override
    public int getIdVProviderCount(String currentUser) throws IdVProviderMgtUIException {

        IdVProviderUIUtils.handleLoggedInUserAuthorization(PERMISSION_IDVP_MGT_VIEW, currentUser);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.getCountOfIdVProviders(tenantId);
        } catch (IdVProviderMgtException e) {
            throw IdVProviderUIExceptionMgt.handleException(e);
        }
    }

    @Override
    public List<IdVProvider> getIdVProviders(Integer limit, Integer offset, String currentUser)
            throws IdVProviderMgtUIException {

        IdVProviderUIUtils.handleLoggedInUserAuthorization(PERMISSION_IDVP_MGT_VIEW, currentUser);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.getIdVProviders(limit, offset, tenantId);
        } catch (IdVProviderMgtException e) {
            throw IdVProviderUIExceptionMgt.handleException(e);
        }
    }

    @Override
    public IdVProvider getIdVProviderById(String id, String currentUser) throws IdVProviderMgtUIException {

        IdVProviderUIUtils.handleLoggedInUserAuthorization(PERMISSION_IDVP_MGT_VIEW, currentUser);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.getIdVProvider(id, tenantId);
        } catch (IdVProviderMgtException e) {
            throw IdVProviderUIExceptionMgt.handleException(e);
        }
    }

    @Override
    public IdVProvider addIdVProvider(IdVProvider provider, String currentUser) throws IdVProviderMgtUIException {

        IdVProviderUIUtils.handleLoggedInUserAuthorization(PERMISSION_IDVP_MGT_ADD, currentUser);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.addIdVProvider(provider, tenantId);
        } catch (IdVProviderMgtException e) {
            throw IdVProviderUIExceptionMgt.handleException(e);
        }
    }

    @Override
    public IdVProvider updateIdVProvider(String id, IdVProvider newProvider, String currentUser)
            throws IdVProviderMgtUIException {

        IdVProviderUIUtils.handleLoggedInUserAuthorization(PERMISSION_IDVP_MGT_UPDATE, currentUser);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            IdVProvider oldProvider = idVProviderManager.getIdVProvider(id, tenantId);
            return idVProviderManager.updateIdVProvider(oldProvider, newProvider, tenantId);
        } catch (IdVProviderMgtException e) {
            throw IdVProviderUIExceptionMgt.handleException(e);
        }

    }

    @Override
    public void deleteIdVProvider(String id, String currentUser) throws IdVProviderMgtUIException {

        IdVProviderUIUtils.handleLoggedInUserAuthorization(PERMISSION_IDVP_MGT_DELETE, currentUser);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            idVProviderManager.deleteIdVProvider(id, tenantId);
        } catch (IdVProviderMgtException e) {
            throw IdVProviderUIExceptionMgt.handleException(e);
        }
    }

    @Override
    public String[] getAllLocalClaims() throws IdVProviderMgtUIException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            List<LocalClaim> localClaims = IdVProviderMgtUIDataHolder.getInstance()
                    .getClaimMetadataManagementService().
                    getLocalClaims(tenantDomain);
            return localClaims.stream().map(Claim::getClaimURI).toArray(String[]::new);
        } catch (ClaimMetadataException e) {
            throw IdVProviderUIExceptionMgt.handleException(e);
        }
    }

}
