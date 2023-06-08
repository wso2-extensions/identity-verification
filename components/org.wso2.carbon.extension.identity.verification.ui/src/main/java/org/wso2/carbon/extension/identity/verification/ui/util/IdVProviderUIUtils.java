package org.wso2.carbon.extension.identity.verification.ui.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Contains utility methods for IdVProviderUI.
 */
public class IdVProviderUIUtils {


    public static boolean isHTTPMethodAllowed(HttpServletRequest request) {
        String httpMethod = request.getMethod();
        return IdVProviderUIConstants.HTTP_POST.equalsIgnoreCase(httpMethod);
    }
}
