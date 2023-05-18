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
package org.wso2.carbon.extension.identity.verification.mgt.internal;

import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifierFactory;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service holder class for Identity Verifier.
 */
public class IdentityVerificationDataHolder {

    private Map<String, IdentityVerifierFactory> identityVerifierFactoryMap;
    private RealmService realmService;
    private IdVProviderManager idVProviderManager;
    private List<IdentityVerificationClaimDAO> idVClaimDAOs = new ArrayList<>();
    private static final IdentityVerificationDataHolder instance = new IdentityVerificationDataHolder();

    private IdentityVerificationDataHolder() {

    }

    public static IdentityVerificationDataHolder getInstance() {

        return instance;
    }

    /**
     * Get the RealmService.
     *
     * @return RealmService.
     */
    public RealmService getRealmService() {

        if (realmService == null) {
            throw new RuntimeException("RealmService was not set during the " +
                    "IdVProviderMgtServiceComponent startup");
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
     * Get IdVProviderManager.
     *
     * @return IdVProviderManager.
     */
    public IdVProviderManager getIdVProviderManager() {

        if (idVProviderManager == null) {
            throw new RuntimeException("IdVProviderManager was not set during the " +
                    "IdVProviderMgtServiceComponent startup");
        }
        return idVProviderManager;
    }

    /**
     * Set IdVProviderManager.
     *
     * @param idVProviderManager IdVProviderManager.
     */
    public void setIdVProviderManager(IdVProviderManager idVProviderManager) {

        this.idVProviderManager = idVProviderManager;
    }

    /**
     * Set IdentityVerifierFactory.
     *
     * @param identityVerifierFactory IdentityVerifierFactory.
     */
    public void setIdentityVerifierFactory(IdentityVerifierFactory identityVerifierFactory) {

        if (identityVerifierFactoryMap == null) {
            identityVerifierFactoryMap = new HashMap<>();
        }
        identityVerifierFactoryMap.put(identityVerifierFactory.getIdentityVerifierType(), identityVerifierFactory);
    }

    /**
     * Get IdentityVerifierFactory.
     *
     * @param identityVerifierName IdentityVerifierFactory name.
     * @return IdentityVerifierFactory.
     */
    public IdentityVerifierFactory getIdentityVerifierFactory(String identityVerifierName) {

        if (identityVerifierFactoryMap == null) {
            return null;
        }
        return identityVerifierFactoryMap.get(identityVerifierName);
    }

    /**
     * Unbind IdentityVerifierFactory.
     *
     * @param identityVerifierFactory IdentityVerifierFactory.
     */
    public void unbindIdentityVerifierFactory(IdentityVerifierFactory identityVerifierFactory) {

        identityVerifierFactoryMap.remove(identityVerifierFactory.getIdentityVerifierType());
    }

    /**
     * Get IdentityVerificationClaimDAO.
     *
     * @return List of IdentityVerificationClaimDAOs.
     */
    public List<IdentityVerificationClaimDAO> getIdVClaimDAOs() {

        return idVClaimDAOs;
    }

    /**
     * Set IdentityVerificationClaimDAO.
     *
     * @param idVClaimDAOs List of IdentityVerificationClaimDAOs.
     */
    public void setIdVClaimDAOs(List<IdentityVerificationClaimDAO> idVClaimDAOs) {

        this.idVClaimDAOs = idVClaimDAOs;
    }
}
