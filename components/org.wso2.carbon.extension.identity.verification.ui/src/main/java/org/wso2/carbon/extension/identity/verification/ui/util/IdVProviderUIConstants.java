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

    // Query param keys
    public static final String KEY_IDVP_ID = "id";
    public static final String KEY_ENABLE = "enable";

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
