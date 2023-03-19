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
package org.wso2.carbon.extension.identity.verification.mgt.model;

import org.json.JSONObject;

/**
 * IdVClaim model class.
 */
public class IdVClaim {

    private String id;
    private String uuid;
    private String claimUri;
    private String claimValue;
    private String userId;
    private boolean status;
    private String idVPId;
    private JSONObject metadata;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public boolean getStatus() {

        return status;
    }

    public void setStatus(boolean status) {

        this.status = status;
    }

    public JSONObject getMetadata() {

        return metadata;
    }

    public void setMetadata(JSONObject metadata) {

        this.metadata = metadata;
    }

    public String getClaimUri() {

        return claimUri;
    }

    public void setClaimUri(String claimUri) {

        this.claimUri = claimUri;
    }

    public String getClaimValue() {

        return claimValue;
    }

    public void setClaimValue(String claimValue) {

        this.claimValue = claimValue;
    }

    public String getIdVPId() {

        return idVPId;
    }

    public void setIdVPId(String idVPId) {

        this.idVPId = idVPId;
    }
}
