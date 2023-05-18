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
package org.wso2.carbon.extension.identity.verification.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManagerImpl;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifierFactory;
import org.wso2.carbon.extension.identity.verification.mgt.dao.CachedBackedIdVClaimDAO;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAOImpl;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Comparator;

/**
 * OSGi declarative services component which handles registration and un-registration of
 * IdentityVerifierService.
 */
@Component(
        name = "org.wso2.carbon.extension.identity.verification.service",
        immediate = true
)
public class IdentityVerificationServiceComponent {

    private static final Log log = LogFactory.getLog(IdentityVerificationServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            IdentityVerificationClaimDAO identityVerificationClaimDAO = new IdentityVerificationClaimDAOImpl();
            ctxt.getBundleContext().registerService(IdentityVerificationClaimDAO.class.getName(),
                    new CachedBackedIdVClaimDAO(identityVerificationClaimDAO), null);
            IdentityVerificationManager identityVerificationService = new IdentityVerificationManagerImpl();
            ctxt.getBundleContext().registerService(IdentityVerificationManager.class.getName(),
                    identityVerificationService, null);
            log.info("IdentityVerificationService bundle activated successfully.");
            if (log.isDebugEnabled()) {
                log.debug("IdentityVerificationService bundle is activated");
            }
        } catch (Throwable e) {
            log.fatal(" Error while activating IdentityVerificationService bundle ", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("IdentityVerificationService bundle is deactivated ");
        }
    }

    @Reference(
            name = "identity.verifier.component",
            service = IdentityVerifierFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityVerifierFactory"
    )
    protected void setIdentityVerifierFactory(IdentityVerifierFactory identityVerifierFactory) {

        IdentityVerificationDataHolder.getInstance().setIdentityVerifierFactory(identityVerifierFactory);
    }

    protected void unsetIdentityVerifierFactory(IdentityVerifierFactory identityVerifierFactory) {

        IdentityVerificationDataHolder.getInstance().unbindIdentityVerifierFactory(identityVerifierFactory);
    }

    @Reference(
            name = "RealmService",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        IdentityVerificationDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        IdentityVerificationDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "IdVProviderManager",
            service = org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdVProviderManager")
    protected void setIdVProviderManager(IdVProviderManager idVProviderManager) {

        IdentityVerificationDataHolder.getInstance().setIdVProviderManager(idVProviderManager);
    }

    protected void unsetIdVProviderManager(IdVProviderManager idVProviderManager) {

        IdentityVerificationDataHolder.getInstance().setIdVProviderManager(null);
    }

    @Reference(
            name = "idvclaim.dao",
            service = org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdVClaimDAO"
    )
    protected void setIdVClaimDAO(IdentityVerificationClaimDAO identityVerificationClaimDAO) {

        if (identityVerificationClaimDAO != null) {
            if (log.isDebugEnabled()) {
                log.debug("idVClaimDAO is registered in IdentityVerificationManagerService.");
            }

            IdentityVerificationDataHolder.getInstance().getIdVClaimDAOs().add(identityVerificationClaimDAO);
            IdentityVerificationDataHolder.getInstance().getIdVClaimDAOs().
                    sort(Comparator.comparingInt(IdentityVerificationClaimDAO::getPriority));
        }
    }

    protected void unsetIdVClaimDAO(IdentityVerificationClaimDAO identityVerificationClaimDAO) {

        if (log.isDebugEnabled()) {
            log.debug("IdVClaimDAO is unregistered in IdentityVerificationService.");
        }
        IdentityVerificationDataHolder.getInstance().getIdVClaimDAOs().remove(identityVerificationClaimDAO);
    }
}
