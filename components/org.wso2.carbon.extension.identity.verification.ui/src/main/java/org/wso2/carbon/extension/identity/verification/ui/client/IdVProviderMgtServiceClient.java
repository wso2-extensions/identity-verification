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

import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;

import java.util.List;

/**
 * Client interface for IdVProviderMgtService.
 */
public interface IdVProviderMgtServiceClient {

    int getIdVProviderCount(String currentUser) throws IdVProviderMgtClientException;

    List<IdVProvider> getIdVProviders(Integer limit, Integer offset, String currentUser)
            throws IdVProviderMgtClientException;

    IdVProvider getIdVProviderById(String id, String currentUser) throws IdVProviderMgtClientException;

    IdVProvider addIdVProvider(IdVProvider provider, String currentUser) throws IdVProviderMgtClientException;

    IdVProvider updateIdVProvider(String id, IdVProvider newProvider, String currentUser)
            throws IdVProviderMgtClientException;

    void deleteIdVProvider(String id, String currentUser) throws IdVProviderMgtClientException;

    String[] getAllLocalClaims() throws IdVProviderMgtClientException;
}
