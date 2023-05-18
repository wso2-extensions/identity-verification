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
package org.wso2.carbon.extension.identity.verification.provider.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManagerImpl;
import org.wso2.carbon.extension.identity.verification.provider.dao.CachedBackedIdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAOImpl;

import java.util.Comparator;

/**
 * OSGi declarative services component which handles registration and un-registration of
 * IdVProviderManager.
 */
@Component(
        name = "org.wso2.carbon.extension.identity.verification.provider.mgt",
        immediate = true
)
public class IdVProviderMgtServiceComponent {

    private static final Log log = LogFactory.getLog(IdVProviderMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        IdVProviderDAO idVProviderDAO = new IdVProviderDAOImpl();
        ctxt.getBundleContext().registerService(IdVProviderDAO.class.getName(),
                new CachedBackedIdVProviderDAO(idVProviderDAO), null);
        ctxt.getBundleContext().registerService(IdVProviderManager.class.getName(),
                new IdVProviderManagerImpl(), null);
        log.info("IdVProviderMgtService bundle activated successfully.");
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("IdVProviderMgtService bundle is deactivated.");
        }
    }

    @Reference(
            name = "idvprovider.dao",
            service = org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdVProviderDAO"
    )
    protected void setIdVProviderDAO(IdVProviderDAO idVProviderDAO) {

        if (idVProviderDAO != null) {
            if (log.isDebugEnabled()) {
                log.debug("idVProviderDAO is registered in IdVProviderMgtService service.");
            }

            IdVProviderDataHolder.getInstance().getIdVProviderDAOs().add(idVProviderDAO);
            IdVProviderDataHolder.getInstance().getIdVProviderDAOs().
                    sort(Comparator.comparingInt(IdVProviderDAO::getPriority));
        }
    }

    protected void unsetIdVProviderDAO(IdVProviderDAO idVProviderDAO) {

        if (log.isDebugEnabled()) {
            log.debug("IdVProviderDAO is unregistered in IdVProviderMgtService service.");
        }
        IdVProviderDataHolder.getInstance().getIdVProviderDAOs().remove(idVProviderDAO);
    }
}
