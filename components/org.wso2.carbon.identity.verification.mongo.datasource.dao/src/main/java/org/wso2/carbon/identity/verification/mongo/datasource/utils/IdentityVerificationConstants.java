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

package org.wso2.carbon.identity.verification.mongo.datasource.utils;

/**
 * This class contains the constants of the identity verifier.
 */
public class IdentityVerificationConstants {

    private static final String IDV_ERROR_PREFIX = "IDV-MDS-";
    public static final String TENANT_ID = "tenantId";
    public static final String IDV_CLAIM = "uuid";
    public static final String STATUS = "status";
    public static final String METADATA = "metadata";
    public static final String USER_ID = "userId";
    public static final String CLAIM_URI = "claimUri";
    public static final String IDVP_ID = "idVPId";
    public static final String KEY = "key";

    /**
     * Holds constants related to configuring the database connection.
     */
    public static class DatabaseConfigConstants {

        public static final String DATABASE_URL_REGEX = "datasource.configuration.url";
        public static final String DATABASE_NAME_REGEX = "datasource.configuration.databaseName";
        public static final String DATABASE_COLLECTION_REGEX = "datasource.configuration.collectionName";
    }

    /**
     * Error messages.
     */
    public enum ErrorMessage {

        ERROR_CHECKING_IDV_CLAIM_EXISTENCE("15000",
                "Error checking the existence of the Identity Verification Claim."),
        ERROR_CHECKING_IDV_CLAIM_DATA_EXISTENCE("15001",
                "Error checking the existence of the Identity Verification Claim."),
        ERROR_PROCESSING_IDV_CLAIM("15002", "Error getting the Identity Verification Claims."),
        ERROR_ADDING_IDV_CLAIMS("15003", "Error adding the Identity Verification Claims."),
        ERROR_UPDATING_IDV_CLAIM("15004", "Error updating the Identity Verification Claim."),
        ERROR_DELETING_IDV_CLAIM("15005", "Error deleting the Identity Verification Claim."),
        ERROR_RETRIEVING_IDV_CLAIM("15006", "Error retrieving the Identity Verification Claim."),
        ERROR_RETRIEVING_IDV_CLAIMS("15007", "Error retrieving the Identity Verification Claims."),
        ERROR_GETTING_USER_STORE_URL("15008", "Error while getting the user store URL."),
        ERROR_GETTING_USER_STORE_DATA("15008",
                "Error while getting the user store name and collection name.");

        private final String errorCode;
        private final String errorMessage;

        ErrorMessage(String errorCode, String errorMessage) {

            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public String getCode() {

            return IDV_ERROR_PREFIX + errorCode;
        }

        public String getMessage() {

            return errorMessage;
        }

        @Override
        public String toString() {

            return errorCode + ":" + errorMessage;
        }
    }
}
