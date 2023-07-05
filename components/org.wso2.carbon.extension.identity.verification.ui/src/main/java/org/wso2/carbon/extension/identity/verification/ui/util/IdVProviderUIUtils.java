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

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.extension.identity.verification.ui.exception.IdVProviderMgtUIClientException;
import org.wso2.carbon.extension.identity.verification.ui.exception.IdVProviderMgtUIException;
import org.wso2.carbon.extension.identity.verification.ui.internal.IdVProviderMgtUIDataHolder;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.wso2.carbon.CarbonConstants.UI_PERMISSION_ACTION;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.ErrorMessages;

/**
 * Contains utility methods for IdVProviderUI.
 */
public class IdVProviderUIUtils {

    private IdVProviderUIUtils() {
        // Adding a private constructor to hide the implicit public constructor since all methods are static.
    }

    /**
     * This method is used to handle the user authorization.
     *
     * @param permission permission string.
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    public static void handleLoggedInUserAuthorization(String permission, String loggedInUser)
            throws IdVProviderMgtUIException {

        if (StringUtils.isBlank(loggedInUser)) {
            throw new IdVProviderMgtUIClientException(ErrorMessages.ERROR_NO_AUTH_USER_FOUND.getCode(),
                    ErrorMessages.ERROR_NO_AUTH_USER_FOUND.getMessage());
        }

        if (!isUserAuthorized(permission, loggedInUser)) {
            throw new IdVProviderMgtUIClientException(ErrorMessages.ERROR_USER_NOT_AUTHORIZED.getCode(),
                    String.format(ErrorMessages.ERROR_USER_NOT_AUTHORIZED.getMessage(), loggedInUser));
        }
    }

    /**
     * This method is used to check if the user has a particular permission.
     *
     * @param permission permission string.
     * @return True if user has the provided permission, false otherwise
     * @throws IdVProviderMgtUIException IdVProviderMgtUIException.
     */
    public static boolean isUserAuthorized(String permission, String loggedInUser)
            throws IdVProviderMgtUIException {

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            if (StringUtils.isBlank(loggedInUser)) {
                return false;
            }

            AuthorizationManager authorizationManager = IdVProviderMgtUIDataHolder.getInstance()
                    .getRealmService()
                    .getTenantUserRealm(tenantId)
                    .getAuthorizationManager();
            return authorizationManager.isUserAuthorized(loggedInUser, permission, UI_PERMISSION_ACTION);
        } catch (UserStoreException e) {
            throw IdVProviderUIExceptionMgt.handleException(e);
        }
    }


    /**
     * Checks whether the HTTP method is allowed.
     *
     * @param request HTTP request.
     * @return True if the HTTP method is allowed, False otherwise.
     */
    public static boolean isHTTPMethodAllowed(HttpServletRequest request) {
        String httpMethod = request.getMethod();
        return IdVProviderUIConstants.HTTP_POST.equalsIgnoreCase(httpMethod);
    }

    /**
     * Creates the identity verification provider object from a JSON object.
     *
     * @param json JSON object.
     * @return IdVProvider object.
     */
    public static IdVProvider getIdVProviderFromJSON(JSONObject json) {
        IdVProvider idVProvider = new IdVProvider();
        idVProvider.setIdVProviderName(json.getString(IdVProviderUIConstants.PROVIDER_NAME));
        idVProvider.setType(json.getString(IdVProviderUIConstants.PROVIDER_TYPE));
        idVProvider.setIdVProviderDescription(json.getString(IdVProviderUIConstants.PROVIDER_DESCRIPTION));
        idVProvider.setEnabled(json.getBoolean(IdVProviderUIConstants.PROVIDER_IS_ENABLED));
        idVProvider.setClaimMappings(createClaimMap(json.getJSONArray(IdVProviderUIConstants.PROVIDER_CLAIMS)));
        idVProvider.setIdVConfigProperties(getConfigProperties(json.getJSONArray(
                IdVProviderUIConstants.PROVIDER_CONFIG_PROPERTIES)));
        return idVProvider;
    }

    /**
     * Populates the identity verification provider object from a JSON object.
     *
     * @param idVProvider Identity verification provider that needs to be populated.
     * @param request The HTTP request that contains Identity verification provider info.
     * @param metadata UI metadata for the provider.
     */
    public static void populateIdVPInfo(IdVProvider idVProvider, HttpServletRequest request, JSONArray metadata) {

        idVProvider.setIdVProviderName(request.getParameter(IdVProviderUIConstants.KEY_IDVP_NAME));
        idVProvider.setIdVProviderDescription(request.getParameter(IdVProviderUIConstants.KEY_IDVP_DESCRIPTION));

        Map<String, JSONObject> metadataMap = new HashMap<>();
        for (int i = 0; i < metadata.length(); i++) {
            JSONObject metadataObject = metadata.getJSONObject(i);
            metadataMap.put(metadataObject.getString(IdVProviderUIConstants.METADATA_NAME), metadataObject);
        }

        // Populate config properties.
        for (IdVConfigProperty idVConfigProperty : idVProvider.getIdVConfigProperties()) {
            String value = request.getParameter(idVConfigProperty.getName());
            if (value != null) {
                idVConfigProperty.setValue(value);
            } else {
                String inputType = metadataMap.get(idVConfigProperty.getName())
                        .getString(IdVProviderUIConstants.METADATA_TYPE);
                if (IdVProviderUIConstants.INPUT_TYPE_CHECKBOX.equals(inputType) ||
                        IdVProviderUIConstants.INPUT_TYPE_TOGGLE.equals(inputType)) {
                    idVConfigProperty.setValue("false");
                }
            }
        }

        // Populate claim mappings.
        Map<String, String> claimMappings = new HashMap<>();
        final int claimRowCount = Integer.parseInt(request.getParameter(IdVProviderUIConstants.KEY_CLAIM_ROW_COUNT));
        for (int i = 0; i < claimRowCount; i++) {
            String externalClaim = request.getParameter(IdVProviderUIConstants.EXTERNAL_CLAIM_PREFIX + i);
            String localClaim = request.getParameter(IdVProviderUIConstants.WSO2_CLAIM_PREFIX + i);
            claimMappings.put(localClaim, externalClaim);
        }
        idVProvider.setClaimMappings(claimMappings);
    }

    /**
     * Create an array of IdVConfigProperty from the config properties JSON array.
     *
     * @param propertiesArray JSON array of config properties.
     * @return An array of IdVConfigProperty.
     */
    private static IdVConfigProperty[] getConfigProperties(JSONArray propertiesArray) {

        IdVConfigProperty[] idVConfigProperties = new IdVConfigProperty[propertiesArray.length()];
        for (int i = 0; i < propertiesArray.length(); i++) {
            JSONObject property = propertiesArray.getJSONObject(i);
            IdVConfigProperty idVConfigProperty = new IdVConfigProperty();
            idVConfigProperty.setName(property.getString(IdVProviderUIConstants.PROVIDER_KEY));
            Object value = property.get(IdVProviderUIConstants.PROVIDER_VALUE);
            if (value instanceof String) {
                idVConfigProperty.setValue((String) value);
            } else if (value instanceof Boolean) {
                idVConfigProperty.setValue(Boolean.toString((boolean) value));
            }
            idVConfigProperty.setConfidential(property.getBoolean(IdVProviderUIConstants.PROVIDER_IS_SECRET));
            idVConfigProperties[i] = idVConfigProperty;
        }
        return idVConfigProperties;
    }

    /**
     * Create the claim mapping from the claims JSON array.
     *
     * @param claimsArray JSON array of claims.
     * @return Map of claims in the form of {externalClaim, localClaim}.
     */
    private static Map<String, String> createClaimMap(JSONArray claimsArray) {

        Map<String, String> claimsMap = new HashMap<>();
        for (int i = 0; i < claimsArray.length(); i++) {
            JSONObject claim = claimsArray.getJSONObject(i);
            claimsMap.put(claim.getString(IdVProviderUIConstants.PROVIDER_IDVP_CLAIM), claim.getString(
                    IdVProviderUIConstants.PROVIDER_LOCAL_CLAIM));
        }
        return claimsMap;
    }
}
