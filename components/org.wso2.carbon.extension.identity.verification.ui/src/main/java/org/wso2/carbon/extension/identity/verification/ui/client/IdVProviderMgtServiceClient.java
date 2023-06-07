package org.wso2.carbon.extension.identity.verification.ui.client;

import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;

import java.util.List;

/**
 * Client interface for IdVProviderMgtService.
 */
public interface IdVProviderMgtServiceClient {

    int getIdVProviderCount(String currentUser) throws IdVProviderMgtClientException;

    List<IdVProvider> getIdVProviders(Integer limit, Integer offset, String currentUser)
            throws IdVProviderMgtClientException;

    IdVProvider getIdVProviderById(String id, String currentUser) throws IdVProviderMgtClientException;

    IdVProvider addIdVProvider(IdVProvider provider, String currentUser) throws IdVProviderMgtClientException;

    IdVProvider updateIdVProvider(IdVProvider oldProvider, IdVProvider newProvider, String currentUser)
            throws IdVProviderMgtClientException;

    void deleteIdVProvider(String id, String currentUser) throws IdVProviderMgtClientException;

}
