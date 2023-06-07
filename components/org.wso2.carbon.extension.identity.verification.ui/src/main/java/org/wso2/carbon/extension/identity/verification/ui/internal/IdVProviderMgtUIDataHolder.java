package org.wso2.carbon.extension.identity.verification.ui.internal;

import org.wso2.carbon.user.core.service.RealmService;

/**
 * Data holder for the Identity Verification Provider Management UI bundle.
 */
public class IdVProviderMgtUIDataHolder {

    private RealmService realmService;

    private static class SingletonHelper {

        static final IdVProviderMgtUIDataHolder INSTANCE = new IdVProviderMgtUIDataHolder();
    }

    public static IdVProviderMgtUIDataHolder getInstance() {

        return SingletonHelper.INSTANCE;
    }

    /**
     * Get the RealmService.
     *
     * @return RealmService.
     */
    public RealmService getRealmService() {

        if (realmService == null) {
            throw new RuntimeException("RealmService was not set during the IdVProviderMgtUIServiceComponent startup");
        }
        return realmService;
    }

    /**
     * Set the RealmService.
     *
     * @param realmService RealmService.
     */
    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }
}