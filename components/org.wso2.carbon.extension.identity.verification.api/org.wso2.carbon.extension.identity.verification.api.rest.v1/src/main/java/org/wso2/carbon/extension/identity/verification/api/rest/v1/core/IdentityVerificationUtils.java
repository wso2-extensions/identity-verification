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

package org.wso2.carbon.extension.identity.verification.api.rest.v1.core;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.verification.api.rest.common.Constants;
import org.wso2.carbon.extension.identity.verification.api.rest.common.ContextLoader;
import org.wso2.carbon.extension.identity.verification.api.rest.common.error.APIError;
import org.wso2.carbon.extension.identity.verification.api.rest.common.error.ErrorResponse;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import javax.ws.rs.core.Response;

/**
 * This class contains the utils for the Identity Verification APIs.
 */
public class IdentityVerificationUtils {

    private static final Log log = LogFactory.getLog(IdentityVerificationUtils.class);

    public static APIError handleIdVException(IdentityException e, Constants.ErrorMessage errorEnum, String... data) {

        ErrorResponse errorResponse;
        Response.Status status;
        if (e instanceof IdVProviderMgtClientException || e instanceof IdentityVerificationClientException) {
            status = Response.Status.BAD_REQUEST;
            errorResponse = getErrorBuilder(e, errorEnum, data)
                    .build(log, e, buildErrorDescription(errorEnum.getDescription(), data), true);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
            errorResponse = getErrorBuilder(e, errorEnum, data)
                    .build(log, e, buildErrorDescription(errorEnum.getDescription(), data), false);
        }
        return new APIError(status, errorResponse);
    }

    private static ErrorResponse.Builder getErrorBuilder(IdentityException exception,
                                                  Constants.ErrorMessage errorEnum, String... data) {

        String errorCode = (StringUtils.isBlank(exception.getErrorCode())) ?
                errorEnum.getCode() : exception.getErrorCode();
        String description = (StringUtils.isBlank(exception.getMessage())) ?
                errorEnum.getDescription() : exception.getMessage();
        return new ErrorResponse.Builder()
                .withCode(errorCode)
                .withMessage(errorEnum.getMessage())
                .withDescription(buildErrorDescription(description, data));
    }

    private static String buildErrorDescription(String description, String... data) {

        if (ArrayUtils.isNotEmpty(data)) {
            return String.format(description, (Object[]) data);
        }
        return description;
    }

    /**
     * Handle exceptions generated in API.
     *
     * @param status HTTP Status.
     * @param error  Error Message information.
     * @return APIError.
     */
    public static APIError handleException(Response.Status status, Constants.ErrorMessage error, String data) {

        return new APIError(status, getErrorBuilder(error, data).build());
    }

    /**
     * Return error builder.
     *
     * @param errorMsg Error Message information.
     * @return ErrorResponse.Builder.
     */
    public static ErrorResponse.Builder getErrorBuilder(Constants.ErrorMessage errorMsg, String data) {

        return new ErrorResponse.Builder()
                .withCode(errorMsg.getCode())
                .withMessage(errorMsg.getMessage())
                .withDescription(includeData(errorMsg, data));
    }

    /**
     * Include context data to error message.
     *
     * @param error Error message.
     * @param data  Context data.
     * @return Formatted error message.
     */
    public static String includeData(Constants.ErrorMessage error, String data) {

        if (StringUtils.isNotBlank(data)) {
            return String.format(error.getDescription(), data);
        } else {
            return error.getDescription();
        }
    }

    /**
     * Get the tenant id from the tenant domain.
     *
     * @return Tenant id.
     */
    public static int getTenantId() {

        String tenantDomain = ContextLoader.getTenantDomainFromContext();
        if (StringUtils.isBlank(tenantDomain)) {
            throw handleException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    Constants.ErrorMessage.ERROR_RETRIEVING_TENANT, tenantDomain);
        }

        return IdentityTenantUtil.getTenantId(tenantDomain);
    }
}
