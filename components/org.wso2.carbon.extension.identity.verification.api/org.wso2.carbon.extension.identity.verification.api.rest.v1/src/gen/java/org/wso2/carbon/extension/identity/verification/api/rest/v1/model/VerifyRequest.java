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

package org.wso2.carbon.extension.identity.verification.api.rest.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.Property;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class VerifyRequest  {
  
    private String identityVerificationProviderId;
    private List<Property> properties = null;


    /**
    **/
    public VerifyRequest identityVerificationProviderId(String identityVerificationProviderId) {

        this.identityVerificationProviderId = identityVerificationProviderId;
        return this;
    }
    
    @ApiModelProperty(example = "2cfb6d01-5384-4e30-aa37-f9a519e95ffc", required = true, value = "")
    @JsonProperty("identityVerificationProviderId")
    @Valid
    @NotNull(message = "Property identityVerificationProviderId cannot be null.")

    public String getIdentityVerificationProviderId() {
        return identityVerificationProviderId;
    }
    public void setIdentityVerificationProviderId(String identityVerificationProviderId) {
        this.identityVerificationProviderId = identityVerificationProviderId;
    }

    /**
    **/
    public VerifyRequest properties(List<Property> properties) {

        this.properties = properties;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("properties")
    @Valid
    public List<Property> getProperties() {
        return properties;
    }
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public VerifyRequest addPropertiesItem(Property propertiesItem) {
        if (this.properties == null) {
            this.properties = new ArrayList<>();
        }
        this.properties.add(propertiesItem);
        return this;
    }

    

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VerifyRequest verifyRequest = (VerifyRequest) o;
        return Objects.equals(this.identityVerificationProviderId, verifyRequest.identityVerificationProviderId) &&
            Objects.equals(this.properties, verifyRequest.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identityVerificationProviderId, properties);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class VerifyRequest {\n");
        
        sb.append("    identityVerificationProviderId: ").append(toIndentedString(identityVerificationProviderId)).append("\n");
        sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

