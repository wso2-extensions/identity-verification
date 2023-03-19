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

package org.wso2.carbon.extension.identity.verification.api.rest.v1.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.wso2.carbon.extension.identity.verification.api.rest.common.ContextLoader;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.ProvidersApiService;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.core.IdentityVerificationProviderService;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.IdVProviderListResponse;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.IdVProviderRequest;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.IdVProviderResponse;

import static org.wso2.carbon.extension.identity.verification.api.rest.common.Constants.IDV_API_PATH_COMPONENT;
import static org.wso2.carbon.extension.identity.verification.api.rest.common.Constants.V1_API_PROVIDER_PATH_COMPONENT;

import java.net.URI;

import javax.ws.rs.core.Response;

/**
 * This class implements the ProvidersApiService interface.
 */
public class ProvidersApiServiceImpl implements ProvidersApiService {

    @Autowired
    IdentityVerificationProviderService identityVerificationProviderService;

    @Override
    public Response addIdVProvider(IdVProviderRequest idVProviderRequest) {

        IdVProviderResponse idVProviderResponse =
                identityVerificationProviderService.addIdVProvider(idVProviderRequest);
        URI location = ContextLoader.buildURIForHeader(IDV_API_PATH_COMPONENT +
                V1_API_PROVIDER_PATH_COMPONENT + "/" + idVProviderResponse.getId());
        return Response.created(location).entity(idVProviderResponse).build();
    }

    @Override
    public Response deleteIdVProvider(String idvProviderId) {

        identityVerificationProviderService.deleteIdVProvider(idvProviderId);
        return Response.noContent().build();
    }

    @Override
    public Response getIdVProvider(String idvProviderId) {

        IdVProviderResponse idVProviderResponse =
                identityVerificationProviderService.getIdVProvider(idvProviderId);
        return Response.ok().entity(idVProviderResponse).build();
    }

    @Override
    public Response getIdVProviders(Integer limit, Integer offset) {

        IdVProviderListResponse idVProviderListResponse =
                identityVerificationProviderService.getIdVProviders(limit, offset);
        return Response.ok().entity(idVProviderListResponse).build();
    }

    @Override
    public Response updateIdVProviders(String idvProviderId, IdVProviderRequest idVProviderRequest) {

        IdVProviderResponse idVProviderResponse =
                identityVerificationProviderService.updateIdVProvider(idvProviderId, idVProviderRequest);
        return Response.ok().entity(idVProviderResponse).build();
    }
}
