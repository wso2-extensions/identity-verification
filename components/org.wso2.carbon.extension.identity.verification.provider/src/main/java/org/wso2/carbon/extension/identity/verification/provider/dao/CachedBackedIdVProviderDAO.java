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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByIdCache;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByIdCacheKey;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByNameCache;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderByNameCacheKey;
import org.wso2.carbon.extension.identity.verification.provider.cache.IdVProviderCacheEntry;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;

import java.util.List;

/**
 * This is a wrapper data access object to the default data access object to provide caching functionalities.
 */
public class CachedBackedIdVProviderDAO implements IdVProviderDAO {

    private static final Log log = LogFactory.getLog(CachedBackedIdVProviderDAO.class);
    private final IdVProviderDAO idVProviderManagerDAO;
    private final IdVProviderByIdCache idVProviderByIdCache;
    private final IdVProviderByNameCache idVProviderByNameCache;

    public CachedBackedIdVProviderDAO(IdVProviderDAO idVProviderManagerDAO) {

        this.idVProviderManagerDAO = idVProviderManagerDAO;
        this.idVProviderByIdCache = IdVProviderByIdCache.getInstance();
        this.idVProviderByNameCache = IdVProviderByNameCache.getInstance();
    }

    @Override
    public int getPriority() {

        return 2;
    }

    @Override
    public IdentityVerificationProvider getIdVProvider(String idVProviderUuid, int tenantId)
            throws IdVProviderMgtException {

        IdentityVerificationProvider identityVerificationProvider = getIdVPFromCacheById(idVProviderUuid, tenantId);
        if (identityVerificationProvider != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache hit for IdVProvider by it's id: %s, Tenant id: %d",
                        idVProviderUuid, tenantId);
                log.debug(message);
            }
        } else {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache miss for IdVProvider by it's id: %s. Tenant id: %d",
                        idVProviderUuid, tenantId);
                log.debug(message);
            }
            identityVerificationProvider = idVProviderManagerDAO.getIdVProvider(idVProviderUuid, tenantId);
            addIdVPToCache(identityVerificationProvider, tenantId);
        }
        return identityVerificationProvider;
    }

    @Override
    public boolean isIdVProviderExists(String idVProviderUuid, int tenantId) throws IdVProviderMgtException {

        IdentityVerificationProvider identityVerificationProvider = getIdVPFromCacheById(idVProviderUuid, tenantId);
        if (identityVerificationProvider != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache hit for IdVProvider by it's id: %s, Tenant id: %d",
                        idVProviderUuid, tenantId);
                log.debug(message);
            }
            return true;
        }
        return idVProviderManagerDAO.isIdVProviderExists(idVProviderUuid, tenantId);
    }

    @Override
    public void addIdVProvider(IdentityVerificationProvider identityVerificationProvider, int tenantId)
            throws IdVProviderMgtException {

        idVProviderManagerDAO.addIdVProvider(identityVerificationProvider, tenantId);
        addIdVPToCache(identityVerificationProvider, tenantId);
    }

    @Override
    public void updateIdVProvider(IdentityVerificationProvider oldIdVProvider,
                                  IdentityVerificationProvider newIdVProvider, int tenantId)
            throws IdVProviderMgtException {

        idVProviderManagerDAO.updateIdVProvider(oldIdVProvider, newIdVProvider, tenantId);
        deleteIdVPFromCache(newIdVProvider, tenantId);
    }

    @Override
    public List<IdentityVerificationProvider> getIdVProviders(Integer limit, Integer offset, int tenantId)
            throws IdVProviderMgtException {

        return idVProviderManagerDAO.getIdVProviders(limit, offset, tenantId);
    }

    @Override
    public int getCountOfIdVProviders(int tenantId) throws IdVProviderMgtException {

        return idVProviderManagerDAO.getCountOfIdVProviders(tenantId);
    }

    @Override
    public IdentityVerificationProvider getIdVPByName(String idVPName, int tenantId) throws IdVProviderMgtException {

        IdentityVerificationProvider identityVerificationProvider = getIdVPFromCacheByName(idVPName, tenantId);
        if (identityVerificationProvider != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache hit for IdVProvider by it's name: %s, Tenant id: %d",
                        idVPName, tenantId);
                log.debug(message);
            }
        } else {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache miss for secret by it's name: %s. Tenant id: %d",
                        idVPName, tenantId);
                log.debug(message);
            }
            identityVerificationProvider = idVProviderManagerDAO.getIdVPByName(idVPName, tenantId);
            addIdVPToCache(identityVerificationProvider, tenantId);
        }
        return identityVerificationProvider;
    }

    @Override
    public void deleteIdVProvider(String idVProviderId, int tenantId) throws IdVProviderMgtException {

        idVProviderManagerDAO.deleteIdVProvider(idVProviderId, tenantId);
    }

    private IdentityVerificationProvider getIdVPFromCacheById(String idVProviderId, int tenantId) {

        IdVProviderByIdCacheKey idVProviderByIdCacheKey = new IdVProviderByIdCacheKey(idVProviderId);
        IdVProviderCacheEntry idVProviderCacheEntry =
                idVProviderByIdCache.getValueFromCache(idVProviderByIdCacheKey, tenantId);
        if (idVProviderCacheEntry != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Entry found from IdVProvider by id cache. IdVProvider id: %s.",
                        idVProviderId);
                log.debug(message);
            }
            return idVProviderCacheEntry.getIdentityVerificationProvider();
        }
        return null;
    }

    private IdentityVerificationProvider getIdVPFromCacheByName(String idVProviderName, int tenantId) {

        IdVProviderByNameCacheKey idVProviderByNameCacheKey = new IdVProviderByNameCacheKey(idVProviderName);
        IdVProviderCacheEntry idVProviderCacheEntry =
                idVProviderByNameCache.getValueFromCache(idVProviderByNameCacheKey, tenantId);
        if (idVProviderCacheEntry != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Entry found from IdVProvider by name cache. IdVProvider name: %s.",
                        idVProviderName);
                log.debug(message);
            }
            return idVProviderCacheEntry.getIdentityVerificationProvider();
        }
        return null;
    }

    private void addIdVPToCache(IdentityVerificationProvider identityVerificationProvider, int tenantId) {

        if (identityVerificationProvider == null) {
            return;
        }
        IdVProviderByIdCacheKey idVProviderByIdCacheKey =
                new IdVProviderByIdCacheKey(identityVerificationProvider.getIdVProviderUuid());
        IdVProviderCacheEntry idVProviderCacheEntry = new IdVProviderCacheEntry(identityVerificationProvider);
        if (log.isDebugEnabled()) {
            String message = String.format("IdVProvider by id cache %s is created",
                    identityVerificationProvider.getIdVProviderUuid());
            log.debug(message);
        }
        idVProviderByIdCache.addToCache(idVProviderByIdCacheKey, idVProviderCacheEntry, tenantId);
    }

    private void deleteIdVPFromCache(IdentityVerificationProvider identityVerificationProvider, int tenantId) {

        if (identityVerificationProvider == null) {
            return;
        }
        IdVProviderByIdCacheKey idVProviderByIdCacheKey =
                new IdVProviderByIdCacheKey(identityVerificationProvider.getIdVProviderUuid());

        if (log.isDebugEnabled()) {
            String message = String.format("IdVProvider by id cache %s is deleted.",
                    identityVerificationProvider.getIdVProviderUuid());
            log.debug(message);
        }
        idVProviderByIdCache.clearCacheEntry(idVProviderByIdCacheKey, tenantId);
    }
}
