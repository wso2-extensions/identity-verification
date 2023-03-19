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
package org.wso2.carbon.extension.identity.verification.mgt.cache;

import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Represents a cache entry for {@link IdVClaim}.
 */
public class IdVClaimCacheEntry extends CacheEntry {

    private IdVClaim idVClaim;

    public IdVClaimCacheEntry(IdVClaim idVClaim) {

        this.idVClaim = idVClaim;
    }

    public IdVClaim getIdVClaim() {

        return idVClaim;
    }

    public void setIdVClaim(IdVClaim idVClaim) {

        this.idVClaim = idVClaim;
    }
}
