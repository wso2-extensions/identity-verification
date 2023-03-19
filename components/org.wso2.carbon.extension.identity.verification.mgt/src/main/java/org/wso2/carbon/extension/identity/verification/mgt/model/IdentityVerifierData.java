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

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the implementation of IdentityVerifierResponse.
 */
public class IdentityVerifierData {

    String identityVerificationProviderId;
    List<IdVClaim> idVClaims;
    List<IdVProperty> idVProperties;

    public String getIdentityVerificationProviderId() {

        return identityVerificationProviderId;
    }

    public void setIdentityVerificationProviderId(String identityVerificationProviderId) {

        this.identityVerificationProviderId = identityVerificationProviderId;
    }

    public List<IdVClaim> getIdVClaims() {

        return idVClaims;
    }

    public void setIdVClaims(List<IdVClaim> idVClaims) {

        this.idVClaims = idVClaims;
    }

    public List<IdVProperty> getIdVProperties() {

        return idVProperties;
    }

    public void setIdVProperties(List<IdVProperty> idVProperties) {

        this.idVProperties = idVProperties;
    }

    public void addIdVClaimProperty(IdVClaim idVClaim) {

        if (this.idVClaims == null) {
            this.idVClaims = new ArrayList<>();
        }
        this.idVClaims.add(idVClaim);
    }

    public void addIdVProperty(IdVProperty idVProperty) {

        if (idVProperties == null) {
            idVProperties = new ArrayList<>();
        }
        idVProperties.add(idVProperty);
    }
}
