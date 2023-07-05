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

import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.extension.identity.verification.ui.exception.IdVProviderMgtUIException;

import java.util.List;

/**
 * Client interface for IdVProviderMgtService.
 */
public interface IdVProviderMgtServiceClient {

    /**
     * Get the count of Identity Verification Providers.
     *
     * @param currentUser Currently logged-in user.
     * @return Count of Identity Verification Providers.
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    int getIdVProviderCount(String currentUser) throws IdVProviderMgtUIException;

    /**
     * Get the list of Identity Verification Providers.
     *
     * @param limit Maximum number of Identity Verification Providers to return.
     * @param offset Start index of the Identity Verification Providers to return.
     * @param currentUser Currently logged-in user.
     * @return List of Identity Verification Providers.
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    List<IdVProvider> getIdVProviders(Integer limit, Integer offset, String currentUser)
            throws IdVProviderMgtUIException;

    /**
     * Get the Identity Verification Provider by id.
     *
     * @param id IdentityVerificationProvider Id.
     * @param currentUser Currently logged-in user.
     * @return IdentityVerificationProvider.
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    IdVProvider getIdVProviderById(String id, String currentUser) throws IdVProviderMgtUIException;

    /**
     * Add a new Identity Verification Provider.
     *
     * @param provider Identity Verification Provider to be added.
     * @param currentUser Currently logged-in user.
     * @return Added Identity Verification Provider.
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    IdVProvider addIdVProvider(IdVProvider provider, String currentUser) throws IdVProviderMgtUIException;

    /**
     * Update the Identity Verification Provider.
     *
     * @param id IdentityVerificationProvider Id.
     * @param newProvider Identity Verification Provider to be updated.
     * @param currentUser Currently logged-in user.
     * @return Updated Identity Verification Provider.
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    IdVProvider updateIdVProvider(String id, IdVProvider newProvider, String currentUser)
            throws IdVProviderMgtUIException;

    /**
     * Delete the Identity Verification Provider by id.
     *
     * @param id IdentityVerificationProvider Id.
     * @param currentUser Currently logged-in user.
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    void deleteIdVProvider(String id, String currentUser) throws IdVProviderMgtUIException;

    /**
     * Returns all the local claims.
     *
     * @return String array of local claims.
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    String[] getAllLocalClaims() throws IdVProviderMgtUIException;
}
