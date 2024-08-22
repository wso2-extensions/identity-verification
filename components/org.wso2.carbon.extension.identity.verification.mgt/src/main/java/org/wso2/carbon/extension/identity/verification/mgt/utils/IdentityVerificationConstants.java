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

package org.wso2.carbon.extension.identity.verification.mgt.utils;

/**
 * This class contains the constants of the identity verifier.
 */
public class IdentityVerificationConstants {

    private static final String IDV_ERROR_PREFIX = "IDV-";
    public static final String ID = "ID";
    public static final String IDV_CLAIM_UUID = "UUID";
    public static final String USER_ID = "USER_ID";
    public static final String CLAIM_URI = "CLAIM_URI";
    public static final String IDVP_ID = "IDVP_ID";
    public static final String IS_VERIFIED = "IS_VERIFIED";
    public static final String METADATA = "METADATA";

    private IdentityVerificationConstants() {

    }

    /**
     * Holds constants related to IdV Claim Management database tables.
     */
    public static class SQLQueries {

        public static final String ADD_IDV_CLAIM_SQL =
                "INSERT INTO IDV_CLAIM (UUID, USER_ID, CLAIM_URI, IDVP_ID, TENANT_ID, IS_VERIFIED, METADATA) " +
                        "VALUES (?,?,?,?,?,?,?)";

        public static final String GET_IDV_CLAIM_SQL =
                "SELECT ID, UUID, USER_ID, CLAIM_URI, TENANT_ID, IDVP_ID, IS_VERIFIED, METADATA FROM IDV_CLAIM " +
                        "WHERE USER_ID=? AND UUID=? AND TENANT_ID=?";

        public static final String GET_IDV_CLAIM_BY_CLAIM_URI_SQL =
                "SELECT ID, UUID, USER_ID, CLAIM_URI, TENANT_ID, IDVP_ID, IS_VERIFIED, METADATA FROM IDV_CLAIM " +
                        "WHERE USER_ID=? AND CLAIM_URI=? AND IDVP_ID=? AND TENANT_ID=?";

        public static final String GET_IDV_CLAIMS_SQL =
                "SELECT ID, UUID, USER_ID, CLAIM_URI, IS_VERIFIED, METADATA FROM IDV_CLAIM WHERE " +
                        "USER_ID=? AND TENANT_ID=?";

        public static final String GET_IDV_CLAIMS_BY_METADATA_SQL =
                "SELECT ID, UUID, USER_ID, CLAIM_URI, IS_VERIFIED, METADATA FROM IDV_CLAIM " +
                        "WHERE IDVP_ID=? AND TENANT_ID=? AND METADATA LIKE ?";

        public static final String UPDATE_IDV_CLAIM_SQL =
                "UPDATE IDV_CLAIM SET IS_VERIFIED=?, METADATA=? WHERE USER_ID=? AND UUID=? AND TENANT_ID=?";

        public static final String DELETE_IDV_CLAIM_SQL =
                "DELETE FROM IDV_CLAIM WHERE USER_ID=? AND UUID=? AND TENANT_ID=?";

        public static final String DELETE_IDV_CLAIMS_SQL =
                "DELETE FROM IDV_CLAIM WHERE USER_ID=? AND TENANT_ID=?";

        public static final String IS_IDV_CLAIM_DATA_EXIST_SQL =
                "SELECT ID FROM IDV_CLAIM WHERE USER_ID=? AND IDVP_ID=? AND CLAIM_URI=? AND TENANT_ID=?";

        public static final String IS_IDV_CLAIM_EXIST_SQL =
                "SELECT ID FROM IDV_CLAIM WHERE UUID=? AND TENANT_ID=?";

        public static final String IDVP_FILTER = " AND IDVP_ID=?";
        public static final String CLAIM_URI_FILTER = " AND CLAIM_URI=?";
    }

    /**
     * Error messages.
     */
    public enum ErrorMessage {

        // Client Errors.
        ERROR_IDV_CLAIM_DATA_ALREADY_EXISTS("10000",
                "Identity Verification Claim data already exists for the user: %s."),
        ERROR_EMPTY_CLAIM_METADATA("10001", "Claim Metadata is empty."),
        ERROR_INVALID_IDV_PROVIDER("10002", "Identity Verification Provider: %s not found."),
        ERROR_INVALID_CLAIM_URI("10003", "Claim URI: %s not found."),
        ERROR_INVALID_USER_ID("10004", "User cannot be found with the user Id: %s."),
        ERROR_INVALID_IDV_CLAIM_ID("10005", "Identity verification claim cannot be found with the " +
                "claim id: %s."),
        ERROR_INVALID_IDV_VERIFIER("10006", "Identity Verifier: %s is not registered."),

        // Server Errors.
        ERROR_CHECKING_IDV_CLAIM_EXISTENCE("15000",
                "Error while checking the existence of the Identity Verification Claim."),
        ERROR_DELETING_IDV_CLAIM("15001", "Error deleting the Identity Verification Claim."),
        ERROR_RETRIEVING_IDV_CLAIM("15002", "Error retrieving the Identity Verification Claim."),
        ERROR_UPDATING_IDV_CLAIM("15003", "Error updating the Identity Verification Claim."),
        ERROR_ADDING_IDV_CLAIM("15004", "Error adding the Identity Verification Claim."),
        ERROR_ADDING_IDV_CLAIMS("15005", "Error adding the Identity Verification Claims."),
        ERROR_RETRIEVING_IDV_CLAIMS("15006", "Error retrieving the Identity Verification Claims."),
        ERROR_VALIDATING_IDV_PROVIDER_ID("15007",
                "Error while validating identity verification provider id: %s."),
        ERROR_CHECKING_USER_ID_EXISTENCE("15008", "Error while checking the user id existence."),
        ERROR_GETTING_USER_STORE("15009", "Error while getting the user store."),
        ERROR_RETRIEVING_IDV_PROVIDER("15010", "Error while retrieving identity verification provider."),
        ERROR_RETRIEVING_IDV_CLAIM_MAPPINGS("15011",
                "Error while retrieving identity verification claim mappings."),
        ERROR_CODE_GET_DAO("15012", "No IdV Claim DAOs are registered."),
        ERROR_RETRIEVING_IDV_CLAIMS_BY_METADATA("15013",
                "Error retrieving the Identity Verification Claims by metadata."),
        ERROR_UPDATING_CLAIM_IDV_DATA("15014",
                "Error while updating IDV data of the user %s."),
        ERROR_UPDATING_IDV_CLAIMS("15015",
                "Error while updating IDV data of claims of the user %s."),
        ERROR_DELETING_IDV_CLAIMS("15016",
                "Error deleting IDV claims of the user %s."),
        ERROR_DELETING_IDV_DATA("15017",
                "Error deleting IDV data of a claim of the user %s.");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return IDV_ERROR_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        @Override
        public String toString() {

            return code + ":" + message;
        }
    }
}
