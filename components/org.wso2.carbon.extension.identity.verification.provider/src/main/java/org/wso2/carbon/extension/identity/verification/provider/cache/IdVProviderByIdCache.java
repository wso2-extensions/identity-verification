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

package org.wso2.carbon.extension.identity.verification.provider.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Represents the cache that holds the IdentityVerificationProvider by id
 * {@link org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider}.
 */
public class IdVProviderByIdCache extends BaseCache<IdVProviderByIdCacheKey, IdVProviderCacheEntry> {

    private static final String IDV_PROVIDER_CACHE_NAME = "IdVProviderByIdCache";
    private static volatile IdVProviderByIdCache instance;

    private IdVProviderByIdCache() {

        super(IDV_PROVIDER_CACHE_NAME);
    }

    public static IdVProviderByIdCache getInstance() {

        if (instance == null) {
            synchronized (IdVProviderByIdCache.class) {
                if (instance == null) {
                    instance = new IdVProviderByIdCache();
                }
            }
        }
        return instance;
    }
}
