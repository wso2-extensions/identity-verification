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

package org.wso2.carbon.extension.identity.verification.api.rest.v1.core;

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.extension.identity.verification.api.rest.common.Constants;
import org.wso2.carbon.extension.identity.verification.api.rest.common.IdentityVerificationServiceHolder;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.ConfigProperty;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.IdVProviderListResponse;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.IdVProviderRequest;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.IdVProviderResponse;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.VerificationClaim;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdentityVerificationProvider;
import org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.extension.identity.verification.api.rest.v1.core.IdentityVerificationUtils.getTenantId;
import static org.wso2.carbon.extension.identity.verification.api.rest.v1.core.IdentityVerificationUtils.handleException;
import static org.wso2.carbon.extension.identity.verification.api.rest.v1.core.IdentityVerificationUtils.handleIdVException;

/**
 * Service class for identity verification providers.
 */
public class IdentityVerificationProviderService {

    /**
     * Add an identity verification provider.
     *
     * @param idVProviderRequest Identity verification provider request.
     * @return Identity verification providers.
     */
    public IdVProviderResponse addIdVProvider(IdVProviderRequest idVProviderRequest) {

        IdentityVerificationProvider identityVerificationProvider;
        int tenantId = getTenantId();
        try {
            identityVerificationProvider = IdentityVerificationServiceHolder.getIdVProviderManager().
                    addIdVProvider(createIdVProvider(idVProviderRequest), tenantId);
        } catch (IdVProviderMgtException e) {
            if (IdVProviderMgtConstants.ErrorMessage.ERROR_IDVP_ALREADY_EXISTS.getCode().equals(e.getErrorCode())) {
                throw handleException(Response.Status.CONFLICT,
                        Constants.ErrorMessage.ERROR_CODE_IDVP_EXISTS, idVProviderRequest.getName());
            }
            throw IdentityVerificationUtils.handleIdVException(e,
                    Constants.ErrorMessage.ERROR_ADDING_IDVP, idVProviderRequest.getName());
        }
        return getIdVProviderResponse(identityVerificationProvider);
    }

    /**
     * Update identity verification provider.
     *
     * @param idVProviderId      Identity verification provider id.
     * @param idVProviderRequest Identity verification provider request.
     * @return Identity verification provider response.
     */
    public IdVProviderResponse updateIdVProvider(String idVProviderId, IdVProviderRequest idVProviderRequest) {

        IdentityVerificationProvider oldIdVProvider;
        IdentityVerificationProvider newIdVProvider;
        int tenantId = getTenantId();
        try {
            oldIdVProvider = IdentityVerificationServiceHolder.getIdVProviderManager().
                    getIdVProvider(idVProviderId, tenantId);

            if (oldIdVProvider == null) {
                throw handleException(Response.Status.NOT_FOUND,
                        Constants.ErrorMessage.ERROR_CODE_IDVP_NOT_FOUND, idVProviderId);
            }
            IdentityVerificationProvider updatedIdVProvider =
                    createUpdatedIdVProvider(oldIdVProvider, idVProviderRequest);
            newIdVProvider = IdentityVerificationServiceHolder.getIdVProviderManager().
                    updateIdVProvider(oldIdVProvider, updatedIdVProvider, tenantId);
        } catch (IdVProviderMgtException e) {
            if (IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_ID.getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_IDVP_ID_NOT_FOUND, idVProviderId);
            } else if (IdVProviderMgtConstants.ErrorMessage.ERROR_IDVP_ALREADY_EXISTS.getCode()
                    .equals(e.getErrorCode())) {
                throw handleException(Response.Status.CONFLICT,
                        Constants.ErrorMessage.ERROR_CODE_IDVP_EXISTS, idVProviderRequest.getName());
            } else {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_UPDATING_IDVP, idVProviderId);
            }
        }
        return getIdVProviderResponse(newIdVProvider);
    }

    /**
     * Get identity verification provider by id.
     *
     * @param idVProviderId Identity verification provider id.
     * @return Identity verification provider response.
     */
    public IdVProviderResponse getIdVProvider(String idVProviderId) {

        try {
            int tenantId = getTenantId();
            IdentityVerificationProvider identityVerificationProvider =
                    IdentityVerificationServiceHolder.getIdVProviderManager().getIdVProvider(idVProviderId, tenantId);
            if (identityVerificationProvider == null) {
                throw handleException(Response.Status.NOT_FOUND,
                        Constants.ErrorMessage.ERROR_CODE_IDVP_NOT_FOUND, idVProviderId);
            }
            return getIdVProviderResponse(identityVerificationProvider);
        } catch (IdVProviderMgtException e) {
            if (IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_ID.getCode().equals(e.getErrorCode())) {
                throw handleIdVException(e, Constants.ErrorMessage.ERROR_CODE_IDVP_ID_NOT_FOUND, idVProviderId);
            } else {
                throw handleIdVException(e, Constants.ErrorMessage.ERROR_RETRIEVING_IDVP, idVProviderId);
            }
        }
    }

    /**
     * Get all identity verification providers.
     *
     * @param limit  Limit per page.
     * @param offset Offset value.
     * @return Identity verification providers.
     */
    public IdVProviderListResponse getIdVProviders(Integer limit, Integer offset) {

        int tenantId = getTenantId();
        try {
            IdVProviderManager idVProviderManager = IdentityVerificationServiceHolder.getIdVProviderManager();
            int totalResults = idVProviderManager.getCountOfIdVProviders(tenantId);

            IdVProviderListResponse idVProviderListResponse = new IdVProviderListResponse();

            if (totalResults > 0) {
                List<IdentityVerificationProvider> identityVerificationProviders = idVProviderManager.
                        getIdVProviders(limit, offset, tenantId);

                if (CollectionUtils.isNotEmpty(identityVerificationProviders)) {
                    List<IdVProviderResponse> identityVerificationProvidersList = new ArrayList<>();
                    for (IdentityVerificationProvider idVP : identityVerificationProviders) {
                        IdVProviderResponse idVPlistItem = getIdVProviderResponse(idVP);
                        identityVerificationProvidersList.add(idVPlistItem);
                    }
                    idVProviderListResponse.setIdentityVerificationProviders(identityVerificationProvidersList);
                    idVProviderListResponse.setCount(identityVerificationProviders.size());
                } else {
                    idVProviderListResponse.setCount(0);
                }
            } else {
                idVProviderListResponse.setCount(0);
            }
            offset = (offset == null) ? Integer.valueOf(0) : offset;
            idVProviderListResponse.setStartIndex(offset + 1);
            idVProviderListResponse.setTotalResults(totalResults);
            return idVProviderListResponse;
        } catch (IdVProviderMgtException e) {
            throw handleIdVException(e, Constants.ErrorMessage.ERROR_RETRIEVING_IDVPS,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
    }

    /**
     * Delete identity verification provider by id.
     *
     * @param idVProviderId Identity verification provider id.
     */
    public void deleteIdVProvider(String idVProviderId) {

        int tenantId = getTenantId();
        try {
            IdentityVerificationServiceHolder.getIdVProviderManager().
                    deleteIdVProvider(idVProviderId, tenantId);
        } catch (IdVProviderMgtException e) {
            throw handleIdVException(e, Constants.ErrorMessage.ERROR_DELETING_IDVP, idVProviderId);
        }
    }

    private List<VerificationClaim> getIdVClaimMappings(IdentityVerificationProvider identityVerificationProvider) {

        Map<String, String> claimMappings = identityVerificationProvider.getClaimMappings();
        return claimMappings.entrySet().stream().map(entry -> {
            VerificationClaim verificationclaim = new VerificationClaim();
            verificationclaim.setLocalClaim(entry.getKey());
            verificationclaim.setIdvpClaim(entry.getValue());
            return verificationclaim;
        }).collect(Collectors.toList());
    }

    private IdVProviderResponse getIdVProviderResponse(IdentityVerificationProvider identityVerificationProvider) {

        // todo: change the idvp name
        IdVProviderResponse idvProviderResponse = new IdVProviderResponse();
        idvProviderResponse.setId(identityVerificationProvider.getIdVProviderUuid());
        idvProviderResponse.setName(identityVerificationProvider.getIdVProviderName());
        idvProviderResponse.setIsEnabled(identityVerificationProvider.isEnabled());
        idvProviderResponse.setDescription(identityVerificationProvider.getIdVProviderDescription());

        if (identityVerificationProvider.getIdVConfigProperties() != null) {
            List<ConfigProperty> configProperties =
                    Arrays.stream(identityVerificationProvider.getIdVConfigProperties()).
                            map(propertyToExternal).collect(Collectors.toList());

            idvProviderResponse.setConfigProperties(configProperties);
        }
        if (identityVerificationProvider.getClaimMappings() != null) {
            idvProviderResponse.setClaims(getIdVClaimMappings(identityVerificationProvider));
        }
        return idvProviderResponse;
    }

    private IdentityVerificationProvider createIdVProvider(IdVProviderRequest idVProviderRequest) {

        IdentityVerificationProvider identityVerificationProvider = new IdentityVerificationProvider();
        identityVerificationProvider.setIdVProviderName(idVProviderRequest.getName());
        identityVerificationProvider.setIdVProviderDescription(idVProviderRequest.getDescription());
        identityVerificationProvider.setEnabled(idVProviderRequest.getIsEnabled());
        if (idVProviderRequest.getClaims() != null) {
            identityVerificationProvider.setClaimMappings(getClaimMap(idVProviderRequest.getClaims()));
        }
        if (idVProviderRequest.getConfigProperties() != null) {
            List<ConfigProperty> properties = idVProviderRequest.getConfigProperties();
            identityVerificationProvider.setIdVConfigProperties(
                    properties.stream().map(propertyToInternal).toArray(IdVConfigProperty[]::new));
        }
        return identityVerificationProvider;
    }

    private IdentityVerificationProvider createUpdatedIdVProvider(IdentityVerificationProvider oldIdVProvider,
                                                                  IdVProviderRequest idVProviderRequest) {

        IdentityVerificationProvider identityVerificationProvider = new IdentityVerificationProvider();
        identityVerificationProvider.setIdVPUUID(oldIdVProvider.getIdVProviderUuid());
        identityVerificationProvider.setIdVProviderName(idVProviderRequest.getName());
        identityVerificationProvider.setIdVProviderDescription(idVProviderRequest.getDescription());
        identityVerificationProvider.setEnabled(idVProviderRequest.getIsEnabled());
        if (idVProviderRequest.getClaims() != null) {
            identityVerificationProvider.setClaimMappings(getClaimMap(idVProviderRequest.getClaims()));
        }
        if (idVProviderRequest.getConfigProperties() != null) {
            List<ConfigProperty> properties = idVProviderRequest.getConfigProperties();
            identityVerificationProvider.setIdVConfigProperties(
                    properties.stream().map(propertyToInternal).toArray(IdVConfigProperty[]::new));
        }
        return identityVerificationProvider;
    }

    private final Function<ConfigProperty, IdVConfigProperty> propertyToInternal = apiProperty -> {

        IdVConfigProperty idVConfigProperty = new IdVConfigProperty();
        idVConfigProperty.setName(apiProperty.getKey());
        idVConfigProperty.setValue(apiProperty.getValue());
        idVConfigProperty.setConfidential(apiProperty.getIsSecret());
        return idVConfigProperty;
    };

    private final Function<IdVConfigProperty, ConfigProperty> propertyToExternal = apiProperty -> {

        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setKey(apiProperty.getName());
        configProperty.setValue(apiProperty.getValue());
        configProperty.setIsSecret(apiProperty.isConfidential());
        return configProperty;
    };

    private Map<String, String> getClaimMap(List<VerificationClaim> verificationclaimList) {

        Map<String, String> claimMap = new HashMap<>();
        for (VerificationClaim verificationclaim : verificationclaimList) {
            claimMap.put(verificationclaim.getLocalClaim(), verificationclaim.getIdvpClaim());
        }
        return claimMap;
    }
}
