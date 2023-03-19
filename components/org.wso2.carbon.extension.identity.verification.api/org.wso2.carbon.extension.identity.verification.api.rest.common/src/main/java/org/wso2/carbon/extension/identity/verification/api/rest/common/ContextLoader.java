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

package org.wso2.carbon.extension.identity.verification.api.rest.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.api.rest.common.error.APIError;
import org.wso2.carbon.extension.identity.verification.api.rest.common.error.ErrorResponse;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.URI;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.extension.identity.verification.api.rest.common.Constants.ErrorMessage.ERROR_BUILDING_URL;
import static org.wso2.carbon.extension.identity.verification.api.rest.common.Constants.ErrorMessage.ERROR_RESOLVING_USER;
import static org.wso2.carbon.extension.identity.verification.api.rest.common.Constants.IDV_API_PATH_COMPONENT;
import static org.wso2.carbon.extension.identity.verification.api.rest.common.Constants.TENANT_CONTEXT_PATH_COMPONENT;
import static org.wso2.carbon.extension.identity.verification.api.rest.common.Constants.TENANT_NAME_FROM_CONTEXT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.UNEXPECTED_SERVER_ERROR;

/**
 * Load information from context.
 */
public class ContextLoader {

    private static final Log log = LogFactory.getLog(ContextLoader.class);

    /**
     * Retrieves loaded tenant domain from carbon context.
     *
     * @return tenant domain of the request is being served.
     */
    public static String getTenantDomainFromContext() {

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (IdentityUtil.threadLocalProperties.get().get(TENANT_NAME_FROM_CONTEXT) != null) {
            tenantDomain = (String) IdentityUtil.threadLocalProperties.get().get(TENANT_NAME_FROM_CONTEXT);
        }
        return tenantDomain;
    }

    /**
     * Retrieves authenticated username from carbon context.
     *
     * @return username of the authenticated user.
     */
    public static String getUsernameFromContext() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    /**
     * Retrieves authenticated username from carbon context.
     *
     * @return username of the authenticated user.
     */
    public static String getUserIdFromContext() {

        try {
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userRealm.getUserStoreManager();

            if (userStoreManager == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Userstore Manager is null");
                }
                throw buildInternalServerError(null, ERROR_RESOLVING_USER.getMessage(),
                        ERROR_RESOLVING_USER.getDescription());
            }
            return userStoreManager.getUserIDFromUserName(getUsernameFromContext());
        } catch (UserStoreException e) {
            throw buildInternalServerError(e, ERROR_RESOLVING_USER.getMessage(),
                    ERROR_RESOLVING_USER.getDescription());
        }
    }

    /**
     * Build the complete URI prepending the tenant association API context without the proxy context path, to the
     * endpoint. Ex: https://localhost:9443/t/<tenant-domain>/api/idv/<endpoint>.
     *
     * @param endpoint Relative endpoint path.
     * @return Fully qualified and complete URI.
     */
    public static URI buildURIForHeader(String endpoint) {

        URI loc;
        String context = getContext(endpoint);

        try {
            String url = ServiceURLBuilder.create().addPath(context).build().getAbsolutePublicURL();
            loc = URI.create(url);
        } catch (URLBuilderException e) {
            throw buildInternalServerError(e, ERROR_BUILDING_URL.getMessage(),
                    ERROR_BUILDING_URL.getDescription());
        }
        return loc;
    }

    /**
     * Builds the API context on whether the tenant qualified url is enabled or not. In tenant qualified mode the
     * ServiceURLBuilder appends the tenant domain to the URI as a path param automatically. But
     * in non tenant qualified mode we need to append the tenant domain to the path manually.
     *
     * @param endpoint Relative endpoint path.
     * @return Context of the API.
     */
    private static String getContext(String endpoint) {

        String context;
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            context = IDV_API_PATH_COMPONENT + endpoint;
        } else {
            context = String.format(TENANT_CONTEXT_PATH_COMPONENT, getTenantDomainFromContext()) +
                    IDV_API_PATH_COMPONENT + endpoint;
        }
        return context;
    }

    /**
     * Builds APIError to be thrown if the URL building fails.
     *
     * @param errorMessage     Error message.
     * @param errorDescription Description of the error.
     * @return APIError object which contains the error description.
     */
    private static APIError buildInternalServerError(Exception e,
                                                     String errorMessage, String errorDescription) {

        String errorCode = UNEXPECTED_SERVER_ERROR.getCode();
        ErrorResponse errorResponse;
        if (e != null) {
            errorResponse = new ErrorResponse.Builder().
                    withCode(errorCode)
                    .withMessage(errorMessage)
                    .withDescription(errorDescription)
                    .build(log, e, errorMessage, false);
        } else {
            errorResponse = new ErrorResponse.Builder().
                    withCode(errorCode)
                    .withMessage(errorMessage)
                    .withDescription(errorDescription)
                    .build(log, errorMessage, false);
        }

        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        return new APIError(status, errorResponse);
    }
}
