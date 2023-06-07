package org.wso2.carbon.extension.identity.verification.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Expose component "org.wso2.carbon.extension.identity.verification.ui" as an OSGi service.
 */
@Component(name = "org.wso2.carbon.extension.identity.verification.ui", immediate = true)
public class IdVProviderMgtUIServiceComponent {

    private static final Log log = LogFactory.getLog(IdVProviderMgtUIServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Verification Provider Management UI bundle activated!");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Identity Verification Provider Management UI bundle is deactivated");
        }
    }


    @Reference(
            name = "RealmService",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        IdVProviderMgtUIDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        IdVProviderMgtUIDataHolder.getInstance().setRealmService(null);
    }
}
