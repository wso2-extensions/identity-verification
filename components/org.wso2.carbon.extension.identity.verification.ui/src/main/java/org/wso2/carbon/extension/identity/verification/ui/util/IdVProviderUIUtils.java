package org.wso2.carbon.extension.identity.verification.ui.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Contains utility methods for IdVProviderUI.
 */
public class IdVProviderUIUtils {


    public static boolean isHTTPMethodAllowed(HttpServletRequest request) {
        String httpMethod = request.getMethod();
        return IdVProviderUIConstants.HTTP_POST.equalsIgnoreCase(httpMethod);
    }

    /**
     * Creates the identity verification provider object from a JSON object.
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
     * Create an array of IdVConfigProperty from the config properties JSON array.
     * @param propertiesArray JSON array of config properties.
     * @return An array of IdVConfigProperty.
     */
    private static IdVConfigProperty[] getConfigProperties(JSONArray propertiesArray) {

        IdVConfigProperty[] idVConfigProperties = new IdVConfigProperty[propertiesArray.length()];
        for (int i = 0; i < propertiesArray.length(); i++) {
            JSONObject property = propertiesArray.getJSONObject(i);
            IdVConfigProperty idVConfigProperty = new IdVConfigProperty();
            idVConfigProperty.setName(property.getString(IdVProviderUIConstants.PROVIDER_KEY));
            idVConfigProperty.setValue(property.getString(IdVProviderUIConstants.PROVIDER_VALUE));
            idVConfigProperty.setConfidential(property.getBoolean(IdVProviderUIConstants.PROVIDER_IS_SECRET));
            idVConfigProperties[i] = idVConfigProperty;
        }
        return idVConfigProperties;
    }

    /**
     * Create the claim mapping from the claims JSON array.
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
