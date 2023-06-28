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

import org.json.JSONObject;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo;

import java.util.List;
import java.util.Map;

/**
 * Client interface for ExtensionMgtService.
 */
public interface ExtensionMgtServiceClient {

    /**
     * Get the list of extension information for identity verification providers.
     * @return List of extension information.
     * @throws IdVProviderMgtClientException IdVProviderMgtClientException
     */
    List<ExtensionInfo> getExtensionInfoOnIdVProviderTypes() throws IdVProviderMgtClientException;

    /**
     * Get the identity verification provider template for the given identity verification provider type.
     * @param idVProviderType Identity verification provider type.
     * @return Identity verification provider template.
     * @throws IdVProviderMgtClientException IdVProviderMgtClientException
     */
    IdVProvider getIdVProviderTemplate(String idVProviderType) throws IdVProviderMgtClientException;

    /**
     * Get the identity verification provider metadata for the given identity verification provider type.
     * @param idVProviderType Identity verification provider type.
     * @return Identity verification provider metadata.
     * @throws IdVProviderMgtClientException IdVProviderMgtClientException
     */
    JSONObject getIdVProviderMetadata(String idVProviderType) throws IdVProviderMgtClientException;

    /**
     * Get the identity verification provider metadata for a list of given identity verification provider types.
     * @param idVProviderTypes A list of identity verification provider types.
     * @return A map of identity verification provider metadata with IdV Provider types as keys.
     * @throws IdVProviderMgtClientException IdVProviderMgtClientException
     */
    Map<String, JSONObject> getIdVProviderMetadataMap(List<String> idVProviderTypes)
            throws IdVProviderMgtClientException;

}
