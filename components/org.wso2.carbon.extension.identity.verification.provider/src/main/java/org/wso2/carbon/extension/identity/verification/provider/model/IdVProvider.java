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
package org.wso2.carbon.extension.identity.verification.provider.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the properties of the Identity Verification Provider.
 */
public class IdVProvider {

    private String id;
    private String uuid;
    private String type;
    private String idVProviderName;
    private boolean isEnabled = false;
    private String idVProviderDescription;
    private Map<String, String> claimMappings = new HashMap<>();
    private IdVConfigProperty[] idVConfigProperties = new IdVConfigProperty[0];

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getIdVProviderName() {

        return idVProviderName;
    }

    public void setIdVProviderName(String idVProviderName) {

        this.idVProviderName = idVProviderName;
    }

    public IdVConfigProperty[] getIdVConfigProperties() {

        return idVConfigProperties;
    }

    public void setIdVConfigProperties(IdVConfigProperty[] idVConfigProperties) {

        this.idVConfigProperties = idVConfigProperties;
    }

    public void setIdVProviderUUID(String idVProviderId) {

        this.uuid = idVProviderId;
    }

    public String getIdVProviderUuid() {

        return uuid;
    }

    public boolean isEnabled() {

        return isEnabled;
    }

    public void setEnabled(boolean enabled) {

        this.isEnabled = enabled;
    }

    public String getIdVProviderDescription() {

        return idVProviderDescription;
    }

    public void setIdVProviderDescription(String idVProviderDescription) {

        this.idVProviderDescription = idVProviderDescription;
    }

    public Map<String, String> getClaimMappings() {

        return claimMappings;
    }

    public void setClaimMappings(Map<String, String> claimMappings) {

        this.claimMappings = claimMappings;
    }
}
