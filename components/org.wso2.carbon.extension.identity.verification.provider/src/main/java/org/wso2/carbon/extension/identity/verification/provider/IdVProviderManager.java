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
package org.wso2.carbon.extension.identity.verification.provider;

import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;

import java.util.List;

/**
 * This interface contains the methods to manage the IdentityVerificationProvider.
 */
public interface IdVProviderManager {

    /**
     * Get the IdentityVerificationProvider by id.
     *
     * @param idVProviderId IdentityVerificationProvider Id.
     * @param tenantId      Tenant Id.
     * @return IdentityVerificationProvider.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    IdVProvider getIdVProvider(String idVProviderId, int tenantId) throws IdVProviderMgtException;

    /**
     * Add a new IdentityVerificationProvider.
     *
     * @param idVProvider IdentityVerificationProvider.
     * @param tenantId    Tenant Id.
     * @return IdentityVerificationProvider.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    IdVProvider addIdVProvider(IdVProvider idVProvider, int tenantId)
            throws IdVProviderMgtException;

    /**
     * Delete the IdentityVerificationProvider.
     *
     * @param idVProviderId IdentityVerificationProvider Id.
     * @param tenantId      Tenant Id.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    void deleteIdVProvider(String idVProviderId, int tenantId) throws IdVProviderMgtException;

    /**
     * Update the IdentityVerificationProvider.
     *
     * @param newIdVProvider New IdentityVerificationProvider.
     * @param oldIdVProvider Old IdentityVerificationProvider.
     * @param tenantId       Tenant Id.
     * @return IdentityVerificationProvider.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    IdVProvider updateIdVProvider(IdVProvider oldIdVProvider,
                                  IdVProvider newIdVProvider,
                                  int tenantId) throws IdVProviderMgtException;

    /**
     * Get all the IdentityVerificationProviders.
     *
     * @param limit    Limit per page.
     * @param offset   Offset value.
     * @param tenantId Tenant Id.
     * @return List of IdentityVerificationProviders.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    List<IdVProvider> getIdVProviders(Integer limit, Integer offset,
                                      int tenantId) throws IdVProviderMgtException;

    /**
     * Get all the IdentityVerificationProviders with filtering conditions.
     *
     * @param limit    Limit per page.
     * @param offset   Offset value.
     * @param filter   Condition to filter.
     * @param tenantId Tenant Id.
     * @return List of IdentityVerificationProviders.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    List<IdVProvider> getIdVProviders(Integer limit, Integer offset, String filter,
                                      int tenantId) throws IdVProviderMgtException;

    /**
     * Get the IdentityVerificationProvider by name.
     *
     * @param idPName  IdentityVerificationProvider name.
     * @param tenantId Tenant Id.
     * @return IdentityVerificationProvider.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    IdVProvider getIdVProviderByName(String idPName, int tenantId) throws IdVProviderMgtException;

    /**
     * Get the count of IdentityVerificationProviders with filtering conditions.
     *
     * @param tenantId Tenant Id.
     * @param filter   Condition to filter.
     * @return Count of IdentityVerificationProviders.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    int getCountOfIdVProviders(int tenantId, String filter) throws IdVProviderMgtException;

    /**
     * Get the count of IdentityVerificationProviders.
     *
     * @param tenantId Tenant Id.
     * @return Count of IdentityVerificationProviders.
     * @throws IdVProviderMgtException IdVProviderMgtException.
     */
    int getCountOfIdVProviders(int tenantId) throws IdVProviderMgtException;

    /**
     * Check whether the IdentityVerificationProvider exists.
     *
     * @param idvProviderId IdentityVerificationProvider Id.
     * @param tenantId      Tenant Id.
     * @return boolean.
     */
    boolean isIdVProviderExists(String idvProviderId, int tenantId) throws IdVProviderMgtException;

    /**
     * Check whether the Identity Verification Provider exists by IDV Provider name.
     *
     * @param idvProviderName Identity Verification Provider Name.
     * @param tenantId        Tenant Id.
     * @return boolean.
     */
    boolean isIdVProviderExistsByName(String idvProviderName, int tenantId) throws IdVProviderMgtException;
}
