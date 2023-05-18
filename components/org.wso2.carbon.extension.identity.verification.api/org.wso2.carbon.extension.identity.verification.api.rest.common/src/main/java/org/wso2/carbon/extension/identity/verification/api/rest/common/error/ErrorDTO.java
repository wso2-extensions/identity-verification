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

package org.wso2.carbon.extension.identity.verification.api.rest.common.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

/**
 * Common DTO class for all the API related error responses.
 */
@ApiModel(description = "")
public class ErrorDTO implements Serializable {

    private static final long serialVersionUID = 1984062651956875663L;

    @NotNull
    private String code = null;

    @NotNull
    private String message = null;

    private String description = null;

    private String ref = null;

    /**
     * Returns the error code.
     *
     * @return Error code.
     */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("code")
    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    /**
     * Returns the error message.
     *
     * @return Error message.
     */
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("message")
    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    /**
     * Returns the error description.
     *
     * @return Error description.
     */
    @ApiModelProperty(value = "")
    @JsonProperty("description")
    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Returns the traceId of the error.
     *
     * @return TraceId of the error.
     */
    @ApiModelProperty(value = "")
    @JsonProperty("traceId")
    public String getRef() {

        return ref;
    }

    public void setRef(String ref) {

        this.ref = ref;
    }

    @Override
    public String toString() {

        String sb = "class ErrorDTO {\n" +
                "  code: " + code + "\n" +
                "  message: " + message + "\n" +
                "  description: " + description + "\n" +
                "  traceId: " + ref + "\n" +
                "}\n";
        return sb;
    }
}
