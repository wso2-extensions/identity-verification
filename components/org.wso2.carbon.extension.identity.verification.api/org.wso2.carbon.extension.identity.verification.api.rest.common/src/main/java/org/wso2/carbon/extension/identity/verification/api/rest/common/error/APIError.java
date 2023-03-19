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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Common Exception class for all the API related errors.
 */
public class APIError extends WebApplicationException {

    private static final long serialVersionUID = -5096160501895108984L;
    private String message;
    private String code;
    private ErrorDTO responseEntity;
    private Response.Status status;

    /**
     * API Error Constructor with status and error response.
     *
     * @param status        Status enum of Response object.
     * @param errorResponse Error data object.
     */
    public APIError(Response.Status status, ErrorDTO errorResponse) {

        this.responseEntity = errorResponse;
        this.message = status.getReasonPhrase();
        this.code = errorResponse.getCode();
        this.status = status;
    }

    /**
     * API Error Constructor with status, message and error response.
     *
     * @param status        Status enum of Response object.
     * @param message       Error message.
     * @param errorResponse Error data object.
     */
    public APIError(Response.Status status, String message, ErrorDTO errorResponse) {

        this(status, errorResponse);
        this.message = message;
        this.code = errorResponse.getCode();
        this.status = status;
    }

    @Override
    public String getMessage() {

        return message;
    }

    public String getCode() {

        return code;
    }

    public ErrorDTO getResponseEntity() {

        return responseEntity;
    }

    public Response.Status getStatus() {

        return status;
    }
}
