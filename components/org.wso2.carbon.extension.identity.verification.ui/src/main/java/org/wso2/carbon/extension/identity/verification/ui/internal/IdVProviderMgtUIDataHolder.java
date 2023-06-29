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

package org.wso2.carbon.extension.identity.verification.ui.internal;

import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Data holder for the Identity Verification Provider Management UI bundle.
 */
public class IdVProviderMgtUIDataHolder {

    private RealmService realmService;
    private ClaimMetadataManagementService claimMetadataManagementService;

    private static class SingletonHelper {

        static final IdVProviderMgtUIDataHolder INSTANCE = new IdVProviderMgtUIDataHolder();
    }

    public static IdVProviderMgtUIDataHolder getInstance() {

        return SingletonHelper.INSTANCE;
    }

    /**
     * Get the RealmService.
     *
     * @return RealmService.
     */
    public RealmService getRealmService() {

        if (realmService == null) {
            throw new RuntimeException("RealmService was not set during the IdVProviderMgtUIServiceComponent startup");
        }
        return realmService;
    }

    /**
     * Set the RealmService.
     *
     * @param realmService RealmService.
     */
    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Get the ClaimMetadataManagementService.
     *
     * @return ClaimMetadataManagementService.
     */
    public ClaimMetadataManagementService getClaimMetadataManagementService() {

        if (claimMetadataManagementService == null) {
            throw new RuntimeException("ClaimMetadataManagementService was not set during the " +
                    "IdVProviderMgtUIServiceComponent startup");
        }
        return claimMetadataManagementService;
    }

    /**
     * Set the ClaimMetadataManagementService.
     *
     * @param claimMetadataManagementService ClaimMetadataManagementService.
     */
    public void setClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        this.claimMetadataManagementService = claimMetadataManagementService;
    }
}
