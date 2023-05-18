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
package org.wso2.carbon.extension.identity.verification.provider.dao;

import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;

import java.util.List;

public interface IdVProviderDAO {

    /**
     * Get priority value for the {@link IdVProviderDAO}.
     *
     * @return Priority value for the DAO.
     */
    int getPriority();

    /**
     * Get Identity Verification Provider.
     *
     * @param idVProviderUuid Identity Verification Provider UUID.
     * @param tenantId        Tenant ID.
     * @return Identity Verification Provider.
     * @throws IdVProviderMgtException Error when getting Identity Verification Provider.
     */
    IdVProvider getIdVProvider(String idVProviderUuid, int tenantId)
            throws IdVProviderMgtException;

    /**
     * Check whether an Identity Verification Provider exists.
     *
     * @param idVProviderUuid Identity Verification Provider UUID.
     * @param tenantId        Tenant ID.
     * @return Whether the identity verification provider exists or not.
     * @throws IdVProviderMgtException Error when checking the Identity Verification Provider existence.
     */
    boolean isIdVProviderExists(String idVProviderUuid, int tenantId) throws IdVProviderMgtException;

    /**
     * Check whether an Identity Verification Provider exists by idv provider name.
     *
     * @param idvProviderName Identity Verification Provider UUID.
     * @param tenantId        Tenant ID.
     * @return Whether the identity verification provider exists or not.
     * @throws IdVProviderMgtException Error when checking the Identity Verification Provider existence.
     */
    boolean isIdVProviderExistsByName(String idvProviderName, int tenantId) throws IdVProviderMgtException;

    /**
     * Add Identity Verification Provider.
     *
     * @param idVProvider Identity Verification Provider.
     * @param tenantId    Tenant ID.
     * @throws IdVProviderMgtException Identity Verification Provider Management Exception.
     */
    void addIdVProvider(IdVProvider idVProvider, int tenantId) throws IdVProviderMgtException;

    /**
     * Update Identity Verification Provider.
     *
     * @param oldIdVProvider Old Identity Verification Provider.
     * @param newIdVProvider New Identity Verification Provider that needs to be updated.
     * @param tenantId       Tenant ID.
     * @throws IdVProviderMgtException Identity Verification Provider Management Exception.
     */
    void updateIdVProvider(IdVProvider oldIdVProvider, IdVProvider newIdVProvider,
                           int tenantId) throws IdVProviderMgtException;

    /**
     * Get Identity Verification Providers.
     *
     * @param limit    Limit.
     * @param offset   Offset.
     * @param tenantId Tenant ID.
     * @throws IdVProviderMgtException Identity Verification Provider Management Exception.
     */
    List<IdVProvider> getIdVProviders(Integer limit, Integer offset, int tenantId)
            throws IdVProviderMgtException;

    /**
     * Get Identity Verification Provider count in a given tenant.
     *
     * @param tenantId Tenant ID.
     * @throws IdVProviderMgtException Identity Verification Provider Management Exception.
     */
    int getCountOfIdVProviders(int tenantId) throws IdVProviderMgtException;

    /**
     * Get Identity Verification Provider by name.
     *
     * @param idVPName Identity Verification Provider name.
     * @param tenantId Tenant ID.
     * @throws IdVProviderMgtException Identity Verification Provider Management Exception.
     */
    IdVProvider getIdVProviderByName(String idVPName, int tenantId) throws IdVProviderMgtException;

    /**
     * Delete Identity Verification Provider by ID.
     *
     * @param idVProviderId Identity Verification Provider ID.
     * @param tenantId      Tenant ID.
     * @throws IdVProviderMgtException Error when getting Identity Verification Provider.
     */
    void deleteIdVProvider(String idVProviderId, int tenantId) throws IdVProviderMgtException;

}
