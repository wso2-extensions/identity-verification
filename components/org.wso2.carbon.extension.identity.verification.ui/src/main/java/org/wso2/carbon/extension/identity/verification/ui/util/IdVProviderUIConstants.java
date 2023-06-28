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

package org.wso2.carbon.extension.identity.verification.ui.util;

/**
 * This class contains the constants used in the IdentityVerificationProvider UI.
 */
public class IdVProviderUIConstants {

    public static final String IDVP_ERROR_PREFIX = "IDVP-UI-";
    public static final String RESOURCE_BUNDLE = "org.wso2.carbon.extension.identity.verification.ui.i18n.Resources";
    public static final String HTTP_POST = "POST";
    public static final String PAGE_NUMBER = "pageNumber";
    public static final String EXTENSION_TYPE = "identity-verification-providers";

    // Query param and form keys
    public static final String KEY_IDVP_ID = "id";
    public static final String KEY_ENABLE = "enable";
    public static final String KEY_IDVP_NAME = "idVPName";
    public static final String KEY_IDVP_DESCRIPTION = "idVPDescription";
    public static final String KEY_IDVP_TYPE = "idVPType";
    public static final String KEY_CLAIM_ROW_COUNT = "claimRowCount";
    public static final String EXTERNAL_CLAIM_PREFIX = "external-claim-name_";
    public static final String WSO2_CLAIM_PREFIX = "claim-row-name-wso2_";

    // Metadata JSON keys
    public static final String METADATA_NAME = "name";
    public static final String METADATA_COMMON = "common";
    public static final String METADATA_CONFIG_PROPERTIES = "configProperties";
    public static final String METADATA_TYPE = "type";

    // Metadata Input Types
    public static final String INPUT_TYPE_CHECKBOX = "checkbox";
    public static final String INPUT_TYPE_TOGGLE = "toggle";

    // keys of IdV Template JSON object
    public static final String PROVIDER_NAME = "Name";
    public static final String PROVIDER_TYPE = "Type";
    public static final String PROVIDER_DESCRIPTION = "description";
    public static final String PROVIDER_IS_ENABLED = "isEnabled";
    public static final String PROVIDER_CLAIMS = "claims";
    public static final String PROVIDER_LOCAL_CLAIM = "localClaim";
    public static final String PROVIDER_IDVP_CLAIM = "idvpClaim";
    public static final String PROVIDER_CONFIG_PROPERTIES = "configProperties";
    public static final String PROVIDER_KEY = "key";
    public static final String PROVIDER_VALUE = "value";
    public static final String PROVIDER_IS_SECRET = "isSecret";


    // Permissions
    public static final String PERMISSION_IDVP_MGT_DELETE = "/permission/admin/manage/identity/idvp/delete";
    public static final String PERMISSION_IDVP_MGT_ADD = "/permission/admin/manage/identity/idvp/add";
    public static final String PERMISSION_IDVP_MGT_UPDATE = "/permission/admin/manage/identity/idvp/update";
    public static final String PERMISSION_IDVP_MGT_VIEW = "/permission/admin/manage/identity/idvp/view";

    /**
     * This enum contains the error messages.
     */
    public enum ErrorMessages {

        ERROR_UNEXPECTED("15013", "Unexpected Error"),
        ERROR_NO_AUTH_USER_FOUND("00078", "No authenticated user found to perform the operation"),
        ERROR_USER_NOT_AUTHORIZED("00076", "User: %s is not authorized to perform this operation."),
        ERROR_LOADING_EXTENSION_INFO("00077", "Error while loading extension information.");

        private final String code;
        private final String message;


        ErrorMessages(String code, String message) {

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
