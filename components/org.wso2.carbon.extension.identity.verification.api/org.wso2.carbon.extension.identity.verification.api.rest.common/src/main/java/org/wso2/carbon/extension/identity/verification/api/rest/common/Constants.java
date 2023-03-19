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

/**
 * Constants related to identity verification service.
 */
public class Constants {

    public static final String V1_API_PROVIDER_PATH_COMPONENT = "/v1/providers";
    public static final String TENANT_NAME_FROM_CONTEXT = "TenantNameFromContext";
    public static final String TENANT_CONTEXT_PATH_COMPONENT = "/t/%s";
    public static final String IDV_API_PATH_COMPONENT = "/api/idv";
    public static final String CORRELATION_ID_MDC = "Correlation-ID";
    public static final String ERROR_PREFIX = "IDV-";

    /**
     * Enum for identity verification related errors.
     * Error Code - code to identify the error.
     * Error Message - What went wrong.
     * Error Description - Why it went wrong.
     */
    public enum ErrorMessage {

        // Client errors - IDVP server APIs.
        ERROR_CODE_IDVP_NOT_FOUND("60000",
                "Invalid identity verification provider id.",
                "Could not find an identity verification provider with given id %s."),
        ERROR_CODE_IDVP_EXISTS("60001",
                "Identity Verification Provider already exists with the given name.",
                "Identity Verification Provider: %s already exists with the given name."),

        // Server errors - IDVP server APIs.
        ERROR_ADDING_IDVP("65000",
                "Unable to add identity verification provider.",
                "Server encountered an error while adding the identity verification provider: %s."),
        ERROR_UPDATING_IDVP("65001",
                "Unable to update identity verification provider.",
                "Server encountered an error while updating the identity verification provider."),
        ERROR_RETRIEVING_IDVP("65002",
                "Unable to retrieve identity verification provider.",
                "Server encountered an error while retrieving the identity verification provider: %s."),
        ERROR_DELETING_IDVP("65003",
                "Unable to delete identity verification provider.",
                "Server encountered an error while deleting the identity verification provider."),
        ERROR_RETRIEVING_TENANT("65004",
                "Error retrieving tenant.",
                "Error occurred while retrieving tenant id for the tenant domain: %s."),
        ERROR_RETRIEVING_IDVPS("6505",
                "Unable to retrieve identity verification providers.",
                "Server encountered an error while retrieving the identity verification providers in " +
                        "the tenant: %s."),

        // Client errors - IDVP user APIs.
        ERROR_CODE_IDV_CLAIM_NOT_FOUND("10000", "Resource not found.",
                "Unable to find a resource matching the provided identity claim identifier %s."),
        ERROR_CODE_USER_ID_NOT_FOUND("10001", "User Id not found.",
                "Unable to find a user for the given user id: %s."),
        ERROR_CODE_CLAIM_ID_NOT_FOUND("10002", "Claim Id not found.",
                "Identity verification claim cannot be found with the claim id: %s."),
        ERROR_CODE_IDV_PROVIDER_NOT_FOUND("10003", "Identity Provider not found.",
                "Provided Identity Verification Provider: %s is not found."),
        ERROR_CODE_IDV_CLAIM_CONFLICT("10004", "Identity verification claim data already exists.",
                "Identity verification claim data already exists for the user: %s."),
        ERROR_CODE_INCOMPLETE_UPDATE_REQUEST("10005",
                "Incomplete identity verification claim update request.",
                "The update request body is incomplete of Identity verification claim: %s."),

        // Server errors - IDVP user APIs.
        ERROR_PERFORMING_IDENTITY_VERIFICATION("15000",
                "Error performing identity verification.",
                "Encountered an error while performing the identity verification for the user: %s."),
        ERROR_RETRIEVING_USER_IDV_CLAIMS("15001",
                "Unable to retrieve identity verification claims",
                "Server encountered an error while retrieving the identity verification claim " +
                        "of the user %s.."),
        ERROR_ADDING_VERIFICATION_CLAIM("15002", "Unable to add the verification claim.",
                "Error occurred while adding the verification claim for the user %s."),
        ERROR_GETTING_VERIFICATION_CLAIM("15003", "Unable to get the verification claim %s.",
                "Error occurred while getting the verification claim."),
        ERROR_UPDATING_VERIFICATION_CLAIM("15004", "Unable to update the verification claim %s.",
                "Error occurred while updating the verification claim."),
        ERROR_RESOLVING_USER("15005", "Error user resolving",
                "Error occured while resolving the user"),
        ERROR_BUILDING_URL("15006", "Error building URL for response header",
                "Server encountered an error while building URL for response header.");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessage(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return ERROR_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + " | " + message;
        }
    }
}
