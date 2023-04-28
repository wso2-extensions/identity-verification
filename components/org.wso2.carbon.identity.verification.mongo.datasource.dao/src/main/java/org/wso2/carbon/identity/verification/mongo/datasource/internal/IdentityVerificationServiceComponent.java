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

package org.wso2.carbon.identity.verification.mongo.datasource.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.identity.verification.mongo.datasource.IdentityVerificationClaimDAOImpl;

/**
 * OSGi declarative services component which handles registration and un-registration of
 * IdentityVerifierMongoService
 */
@Component(
        name = "org.wso2.carbon.extension.identity.verification.mongo.service",
        immediate = true
)
public class IdentityVerificationServiceComponent {

    private static final Log log = LogFactory.getLog(IdentityVerificationServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            IdentityVerificationClaimDAO identityVerificationMongoService = new IdentityVerificationClaimDAOImpl();
            ctxt.getBundleContext().registerService(IdentityVerificationClaimDAO.class.getName(),
                    identityVerificationMongoService, null);
            log.info("IdentityVerificationMongoService bundle activated successfully.");
            if (log.isDebugEnabled()) {
                log.debug("IdentityVerificationMongoService bundle is activated");
            }
        } catch (Throwable e) {
            log.fatal(" Error while activating IdentityVerificationMongoService bundle ", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("IdentityVerificationMongoService bundle is deactivated ");
        }
    }
}

