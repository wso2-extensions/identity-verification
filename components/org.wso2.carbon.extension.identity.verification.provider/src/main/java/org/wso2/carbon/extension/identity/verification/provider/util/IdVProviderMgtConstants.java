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
package org.wso2.carbon.extension.identity.verification.provider.util;

/**
 * This class contains the constants used in the IdVProvider.
 */
public class IdVProviderMgtConstants {

    public static final String IDVP_ERROR_PREFIX = "IDVP-";
    public static final String IDVP_SECRET_TYPE_IDVP_SECRETS = "IDVP_SECRET_PROPERTIES";
    public static final String ID = "ID";
    public static final String IDVP_UUID = "UUID";
    public static final String NAME = "NAME";
    public static final String IDVP_TYPE = "IDVP_TYPE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String IS_ENABLED = "IS_ENABLED";
    public static final String PROPERTY_KEY = "PROPERTY_KEY";
    public static final String PROPERTY_VALUE = "PROPERTY_VALUE";
    public static final String IS_SECRET = "IS_SECRET";
    public static final String CLAIM = "CLAIM";
    public static final String LOCAL_CLAIM = "LOCAL_CLAIM";
    public static final String IDVP_FILTER_NAME = "name";
    public static final String IDVP_FILTER_DESCRIPTION = "description";
    public static final String IDVP_FILTER_TYPE = "type";
    public static final String IDVP_FILTER_IS_ENABLED = "isEnabled";
    public static final String IDVP_FILTER_UUID = "id";
    public static final String SEPERATOR = ":";
    public static final String EMPTY_STRING = "";
    public static final String EQ = "eq";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String CO = "co";
    public static final String IS_TRUE_VALUE = "1";
    public static final String IS_FALSE_VALUE = "0";

    /**
     * This class contains the constants used in the IdVProvider.
     */
    public static class SQLQueries {

        public static final String GET_IDVP_SQL = "SELECT ID, UUID, IDVP_TYPE, NAME, DESCRIPTION, IS_ENABLED" +
                " FROM IDVP WHERE UUID=? AND TENANT_ID=?";
        public static final String IS_IDVP_EXIST_SQL = "SELECT ID FROM IDVP WHERE UUID=? AND TENANT_ID=?";
        public static final String IS_IDVP_EXIST_BY_NAME_SQL = "SELECT ID FROM IDVP WHERE NAME=? AND TENANT_ID=?";
        public static final String GET_IDVP_BY_NAME_SQL = "SELECT ID, UUID, NAME, IDVP_TYPE, DESCRIPTION, " +
                "IS_ENABLED FROM IDVP WHERE NAME=? AND TENANT_ID=?";
        public static final String GET_IDVP_CONFIG_SQL = "SELECT PROPERTY_KEY, PROPERTY_VALUE, IS_SECRET FROM " +
                "IDVP_CONFIG WHERE IDVP_ID=? AND TENANT_ID=?";
        public static final String GET_IDVP_CLAIMS_SQL = "SELECT CLAIM, LOCAL_CLAIM FROM " +
                "IDVP_CLAIM_MAPPING WHERE IDVP_ID=? AND TENANT_ID=?";
        public static final String GET_IDVPS_SQL_BY_MSSQL = "SELECT ID, UUID, NAME, IDVP_TYPE, DESCRIPTION, IS_ENABLED FROM " +
                "IDVP WHERE TENANT_ID=? ORDER BY UUID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        public static final String GET_IDVPS_SQL_BY_MYSQL = "SELECT ID, UUID, NAME, IDVP_TYPE, DESCRIPTION, IS_ENABLED FROM " +
                "IDVP WHERE TENANT_ID=? ORDER BY UUID ASC LIMIT ?, ?";
        public static final String GET_IDVPS_SQL_BY_POSTGRESQL = "SELECT ID, UUID, NAME, IDVP_TYPE, DESCRIPTION, IS_ENABLED FROM " +
                "IDVP WHERE TENANT_ID=? ORDER BY UUID ASC LIMIT ? OFFSET ? ";
        public static final String GET_IDVPS_SQL_BY_MSSQL_WITH_FILTER = "SELECT ID, UUID, NAME, IDVP_TYPE, DESCRIPTION, IS_ENABLED FROM " +
                "IDVP WHERE %s TENANT_ID=? ORDER BY UUID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        public static final String GET_IDVPS_SQL_BY_MYSQL_WITH_FILTER = "SELECT ID, UUID, NAME, IDVP_TYPE, DESCRIPTION, IS_ENABLED FROM " +
                "IDVP WHERE %s TENANT_ID=? ORDER BY UUID ASC LIMIT ?, ?";
        public static final String GET_IDVPS_SQL_BY_POSTGRESQL_WITH_FILTER = "SELECT ID, UUID, NAME, IDVP_TYPE, DESCRIPTION, IS_ENABLED FROM " +
                "IDVP WHERE %s TENANT_ID=? ORDER BY UUID ASC LIMIT ? OFFSET ?";
        public static final String GET_COUNT_OF_IDVPS_SQL = "SELECT COUNT(*) FROM IDVP WHERE TENANT_ID=?";
        public static final String GET_COUNT_OF_IDVPS_SQL_WITH_FILTER = "SELECT COUNT(*) FROM IDVP WHERE %s TENANT_ID=?";
        public static final String DELETE_IDV_SQL = "DELETE FROM IDVP WHERE UUID=? AND TENANT_ID=?";
        public static final String ADD_IDVP_SQL = "INSERT INTO IDVP(UUID, TENANT_ID, NAME, IDVP_TYPE, " +
                "DESCRIPTION, IS_ENABLED) VALUES (?, ?, ?, ?, ?, ?)";
        public static final String ADD_IDVP_CONFIG_SQL = "INSERT INTO IDVP_CONFIG " +
                "(IDVP_ID, TENANT_ID, PROPERTY_KEY, PROPERTY_VALUE, IS_SECRET) VALUES (?, ?, ?, ?, ?)";
        public static final String ADD_IDVP_CLAIM_SQL = "INSERT INTO IDVP_CLAIM_MAPPING " +
                "(IDVP_ID, TENANT_ID, CLAIM, LOCAL_CLAIM) VALUES (?, ?, ?, ?)";
        public static final String UPDATE_IDVP_SQL = "UPDATE IDVP SET NAME=?, IDVP_TYPE=?, DESCRIPTION=?, " +
                "IS_ENABLED=? WHERE UUID=? AND TENANT_ID=?";
        public static final String DELETE_IDVP_CONFIG_SQL = "DELETE FROM IDVP_CONFIG " +
                "WHERE IDVP_ID=? AND TENANT_ID=?";
        public static final String DELETE_IDVP_CLAIM_SQL = "DELETE FROM IDVP_CLAIM_MAPPING " +
                "WHERE IDVP_ID=? AND TENANT_ID=?";
    }

    /**
     * Error messages.
     */
    public enum ErrorMessage {

        // Client errors.
        ERROR_IDVP_ALREADY_EXISTS("60000",
                "An Identity Verification Provider already exists with the name: %s."),
        ERROR_EMPTY_IDVP_ID("60001", "Identity Verification Provider ID value is empty."),
        ERROR_EMPTY_IDVP("60002", "Identity Verification Provider Name is empty."),
        ERROR_UPDATE_IDVP("60003", "Updating Identity Verification Provider Type is not allowed."),
        ERROR_RETRIEVING_FILTERED_IDV_PROVIDERS("60003",
                "Error while retrieving Identity Verification Providers: %s."),

        // Server errors.
        ERROR_RETRIEVING_IDV_PROVIDERS("65000",
                "An error occurred while retrieving Identity Verification Providers."),
        ERROR_RETRIEVING_IDV_PROVIDER("65001",
                "An error occurred while retrieving Identity Verification Provider: %s"),
        ERROR_RETRIEVING_IDV_PROVIDER_CONFIGS("65002",
                "An error occurred while retrieving configs of Identity Verification Provider: %s."),
        ERROR_RETRIEVING_IDV_PROVIDER_CLAIMS("65003",
                "An error occurred while retrieving the claims of Identity Verification Provider: %s."),
        ERROR_ADDING_IDV_PROVIDER("65004", "Error while adding Identity Verification Provider."),
        ERROR_DELETING_IDV_PROVIDER("65005",
                "An error occurred while deleting Identity Verification Provider: %s."),
        ERROR_GETTING_IDV_PROVIDER_COUNT("65006",
                "An error occurred while getting the count of Identity Verification Providers in tenant: %s."),
        ERROR_RETRIEVING_IDV_PROVIDER_SECRETS("65007",
                "An error occurred while retrieving secrets of Identity Verification Provider: %s."),
        ERROR_ADDING_IDV_PROVIDER_CONFIGS("65008",
                "An error occurred while adding configs of Identity Verification Provider: %s."),
        ERROR_STORING_IDV_PROVIDER_SECRETS("65009",
                "An error occurred while storing secrets of Identity Verification Provider: %s."),
        ERROR_ADDING_IDV_PROVIDER_CLAIMS("65010",
                "An error occurred while adding claims of Identity Verification Provider: %s."),
        ERROR_DELETING_IDV_PROVIDER_CONFIGS("65011",
                "An error occurred while deleting configs of Identity Verification Provider: %s."),
        ERROR_DELETING_IDV_PROVIDER_CLAIMS("65012",
                "An error occurred while deleting claims of Identity Verification Provider: %s."),
        ERROR_UPDATING_IDV_PROVIDER("65013", "Error while updating Identity Verification Provider."),
        ERROR_CODE_GET_DAO("65014", "No IdV Provider DAOs are registered.");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return IDVP_ERROR_PREFIX + code;
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
