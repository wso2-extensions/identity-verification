package org.wso2.carbon.extension.identity.verification.ui.client;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManagerImpl;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.extension.identity.verification.ui.internal.IdVProviderMgtUIDataHolder;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;

import static org.wso2.carbon.CarbonConstants.UI_PERMISSION_ACTION;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.ErrorMessages;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.PERMISSION_IDVP_MGT_ADD;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.PERMISSION_IDVP_MGT_DELETE;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.PERMISSION_IDVP_MGT_UPDATE;
import static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.PERMISSION_IDVP_MGT_VIEW;

/**
 * OSGI Client for Identity Verification Provider Management Service.
 */
public class IdVProviderMgtServiceClientImpl implements IdVProviderMgtServiceClient {
    private final IdVProviderManager idVProviderManager;

    private IdVProviderMgtServiceClientImpl() {

        idVProviderManager = new IdVProviderManagerImpl();
    }

    private static class IdVProviderMgtServiceClientImplHolder {

        static final IdVProviderMgtServiceClientImpl INSTANCE = new IdVProviderMgtServiceClientImpl();
    }

    public static IdVProviderMgtServiceClient getInstance() {

        return IdVProviderMgtServiceClientImplHolder.INSTANCE;
    }

    @Override
    public int getIdVProviderCount(String currentUser) throws IdVProviderMgtClientException {

        handleLoggedInUserAuthorization(currentUser, PERMISSION_IDVP_MGT_VIEW);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.getCountOfIdVProviders(tenantId);
        } catch (IdVProviderMgtException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public List<IdVProvider> getIdVProviders(Integer limit, Integer offset, String currentUser)
            throws IdVProviderMgtClientException {

        handleLoggedInUserAuthorization(currentUser, PERMISSION_IDVP_MGT_VIEW);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.getIdVProviders(limit, offset, tenantId);
        } catch (IdVProviderMgtException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public IdVProvider getIdVProviderById(String id, String currentUser) throws IdVProviderMgtClientException {

        handleLoggedInUserAuthorization(currentUser, PERMISSION_IDVP_MGT_VIEW);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.getIdVProvider(id, tenantId);
        } catch (IdVProviderMgtException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public IdVProvider addIdVProvider(IdVProvider provider, String currentUser) throws IdVProviderMgtClientException {

        handleLoggedInUserAuthorization(currentUser, PERMISSION_IDVP_MGT_ADD);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.addIdVProvider(provider, tenantId);
        } catch (IdVProviderMgtException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public IdVProvider updateIdVProvider(IdVProvider oldProvider, IdVProvider newProvider, String currentUser)
            throws IdVProviderMgtClientException {

        handleLoggedInUserAuthorization(currentUser, PERMISSION_IDVP_MGT_UPDATE);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            return idVProviderManager.updateIdVProvider(oldProvider, newProvider, tenantId);
        } catch (IdVProviderMgtException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }

    @Override
    public void deleteIdVProvider(String id, String currentUser) throws IdVProviderMgtClientException {

        handleLoggedInUserAuthorization(PERMISSION_IDVP_MGT_DELETE, currentUser);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            idVProviderManager.deleteIdVProvider(id, tenantId);
        } catch (IdVProviderMgtException e) {
            throw new IdVProviderMgtClientException(e.getErrorCode(), e.getMessage(), e);
        }
    }


    /**
     * This is used to handle the authorization. Authentication supports in rest API via a tomcat valve.
     *
     * @param permission permission string.
     * @throws IdVProviderMgtClientException Consent Management Exception.
     */
    private void handleLoggedInUserAuthorization(String permission, String loggedInUser)
            throws IdVProviderMgtClientException {

        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            if (StringUtils.isBlank(loggedInUser)) {
                throw new IdVProviderMgtClientException(ErrorMessages.ERROR_NO_AUTH_USER_FOUND.getCode(),
                        ErrorMessages.ERROR_NO_AUTH_USER_FOUND.getMessage());
            }

            AuthorizationManager authorizationManager = IdVProviderMgtUIDataHolder.getInstance()
                    .getRealmService()
                    .getTenantUserRealm(tenantId)
                    .getAuthorizationManager();
            if (!authorizationManager.isUserAuthorized(loggedInUser, permission, UI_PERMISSION_ACTION)) {
                throw new IdVProviderMgtClientException(ErrorMessages.ERROR_USER_NOT_AUTHORIZED.getCode(),
                        String.format(ErrorMessages.ERROR_USER_NOT_AUTHORIZED.getMessage(), loggedInUser));
            }
        } catch (UserStoreException e) {
            throw new IdVProviderMgtClientException(ErrorMessages.ERROR_UNEXPECTED.getCode(),
                    ErrorMessages.ERROR_UNEXPECTED.getMessage());
        }
    }

}