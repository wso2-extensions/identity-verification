package org.wso2.carbon.extension.identity.verification.ui.client;

import org.json.JSONObject;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants;
import org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIUtils;
import org.wso2.carbon.identity.extension.mgt.ExtensionManager;
import org.wso2.carbon.identity.extension.mgt.ExtensionManagerImpl;
import org.wso2.carbon.identity.extension.mgt.exception.ExtensionManagementException;
import org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An OSGI client for Extension Management Service.
 */
public class ExtensionMgtServiceClientImpl implements ExtensionMgtServiceClient {
    private final ExtensionManager extensionManager;

    private ExtensionMgtServiceClientImpl() {

        extensionManager = new ExtensionManagerImpl();
    }

    private static class ExtensionMgtServiceClientImplHolder {

        private static final ExtensionMgtServiceClientImpl INSTANCE = new ExtensionMgtServiceClientImpl();
    }

    public static ExtensionMgtServiceClientImpl getInstance() {

        return ExtensionMgtServiceClientImplHolder.INSTANCE;
    }

    @Override
    public List<ExtensionInfo> getExtensionInfoOnIdVProviderTypes() throws IdVProviderMgtClientException {

        try {
            return extensionManager.getExtensionsByType(IdVProviderUIConstants.EXTENSION_TYPE);
        } catch (ExtensionManagementException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public IdVProvider getIdVProviderTemplate(String idVProviderType) throws IdVProviderMgtClientException {

        try {
            JSONObject extensionTemplate = extensionManager.getExtensionTemplate(IdVProviderUIConstants.EXTENSION_TYPE,
                    idVProviderType);
            return IdVProviderUIUtils.getIdVProviderFromJSON(extensionTemplate);
        } catch (ExtensionManagementException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public JSONObject getIdVProviderMetadata(String idVProviderType) throws IdVProviderMgtClientException {
        try {
            return extensionManager.getExtensionMetadata(IdVProviderUIConstants.EXTENSION_TYPE, idVProviderType);
        } catch (ExtensionManagementException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public Map<String, JSONObject> getIdVProviderMetadataMap(List<String> idVProviderTypes)
            throws IdVProviderMgtClientException {
        Map<String, JSONObject> idVProviderMetadataMap = new HashMap<>();
        for (String idVProviderType : idVProviderTypes) {
            idVProviderMetadataMap.put(idVProviderType, getIdVProviderMetadata(idVProviderType));
        }
        return idVProviderMetadataMap;
    }
}
