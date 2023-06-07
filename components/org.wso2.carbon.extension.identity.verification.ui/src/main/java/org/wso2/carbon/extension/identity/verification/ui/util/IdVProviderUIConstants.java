package org.wso2.carbon.extension.identity.verification.ui.util;

/**
 * This class contains the constants used in the IdentityVerificationProvider UI.
 */
public class IdVProviderUIConstants {

    public static final String IDVP_ERROR_PREFIX = "IDVP-UI-";
    public static final String RESOURCE_BUNDLE = "org.wso2.carbon.extension.identity.verification.ui.i18n.Resources";
    public static final String HTTP_POST = "POST";
    public static final String IDVP_ID_KEY = "id";
    public static final String PAGE_NUMBER = "pageNumber";

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
        ERROR_USER_NOT_AUTHORIZED("00076", "User: %s is not authorized to perform this operation.");

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
