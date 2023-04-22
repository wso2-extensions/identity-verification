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
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
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

        IdVProvider idVProvider;
        int tenantId = getTenantId();
        try {
            idVProvider = IdentityVerificationServiceHolder.getIdVProviderManager().
                    addIdVProvider(createIdVProvider(idVProviderRequest), tenantId);
        } catch (IdVProviderMgtException e) {
            if (IdVProviderMgtConstants.ErrorMessage.ERROR_IDVP_ALREADY_EXISTS.getCode().equals(e.getErrorCode())) {
                throw handleException(Response.Status.CONFLICT,
                        Constants.ErrorMessage.ERROR_CODE_IDVP_EXISTS, idVProviderRequest.getName());
            }
            throw IdentityVerificationUtils.handleIdVException(e,
                    Constants.ErrorMessage.ERROR_ADDING_IDVP, idVProviderRequest.getName());
        }
        return getIdVProviderResponse(idVProvider);
    }

    /**
     * Update identity verification provider.
     *
     * @param idVProviderId      Identity verification provider id.
     * @param idVProviderRequest Identity verification provider request.
     * @return Identity verification provider response.
     */
    public IdVProviderResponse updateIdVProvider(String idVProviderId, IdVProviderRequest idVProviderRequest) {

        IdVProvider oldIdVProvider;
        IdVProvider newIdVProvider;
        int tenantId = getTenantId();
        try {
            oldIdVProvider = IdentityVerificationServiceHolder.getIdVProviderManager().
                    getIdVProvider(idVProviderId, tenantId);

            if (oldIdVProvider == null) {
                throw handleException(Response.Status.NOT_FOUND,
                        Constants.ErrorMessage.ERROR_CODE_IDVP_NOT_FOUND, idVProviderId);
            }
            IdVProvider updatedIdVProvider =
                    createUpdatedIdVProvider(oldIdVProvider, idVProviderRequest);
            newIdVProvider = IdentityVerificationServiceHolder.getIdVProviderManager().
                    updateIdVProvider(oldIdVProvider, updatedIdVProvider, tenantId);
        } catch (IdVProviderMgtException e) {
            if (IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_ID.getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_IDV_PROVIDER_NOT_FOUND, idVProviderId);
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
            IdVProvider idVProvider =
                    IdentityVerificationServiceHolder.getIdVProviderManager().getIdVProvider(idVProviderId, tenantId);
            if (idVProvider == null) {
                throw handleException(Response.Status.NOT_FOUND,
                        Constants.ErrorMessage.ERROR_CODE_IDVP_NOT_FOUND, idVProviderId);
            }
            return getIdVProviderResponse(idVProvider);
        } catch (IdVProviderMgtException e) {
            if (IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_ID.getCode().equals(e.getErrorCode())) {
                throw handleIdVException(e, Constants.ErrorMessage.ERROR_CODE_IDV_PROVIDER_NOT_FOUND, idVProviderId);
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
                List<IdVProvider> idVProviders = idVProviderManager.getIdVProviders(limit, offset, tenantId);

                if (CollectionUtils.isNotEmpty(idVProviders)) {
                    List<IdVProviderResponse> idVProvidersList = new ArrayList<>();
                    for (IdVProvider idVP : idVProviders) {
                        IdVProviderResponse idVPlistItem = getIdVProviderResponse(idVP);
                        idVProvidersList.add(idVPlistItem);
                    }
                    idVProviderListResponse.setIdentityVerificationProviders(idVProvidersList);
                    idVProviderListResponse.setCount(idVProviders.size());
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

    private List<VerificationClaim> getIdVClaimMappings(IdVProvider idVProvider) {

        Map<String, String> claimMappings = idVProvider.getClaimMappings();
        return claimMappings.entrySet().stream().map(entry -> {
            VerificationClaim verificationclaim = new VerificationClaim();
            verificationclaim.setLocalClaim(entry.getKey());
            verificationclaim.setIdvpClaim(entry.getValue());
            return verificationclaim;
        }).collect(Collectors.toList());
    }

    private IdVProviderResponse getIdVProviderResponse(IdVProvider idVProvider) {

        // todo: change the idvp name
        IdVProviderResponse idvProviderResponse = new IdVProviderResponse();
        idvProviderResponse.setId(idVProvider.getIdVProviderUuid());
        idvProviderResponse.setName(idVProvider.getIdVProviderName());
        idvProviderResponse.setIsEnabled(idVProvider.isEnabled());
        idvProviderResponse.setDescription(idVProvider.getIdVProviderDescription());

        if (idVProvider.getIdVConfigProperties() != null) {
            List<ConfigProperty> configProperties =
                    Arrays.stream(idVProvider.getIdVConfigProperties()).
                            map(propertyToExternal).collect(Collectors.toList());

            idvProviderResponse.setConfigProperties(configProperties);
        }
        if (idVProvider.getClaimMappings() != null) {
            idvProviderResponse.setClaims(getIdVClaimMappings(idVProvider));
        }
        return idvProviderResponse;
    }

    private IdVProvider createIdVProvider(IdVProviderRequest idVProviderRequest) {

        IdVProvider idVProvider = new IdVProvider();
        idVProvider.setIdVProviderName(idVProviderRequest.getName());
        idVProvider.setIdVProviderDescription(idVProviderRequest.getDescription());
        idVProvider.setEnabled(idVProviderRequest.getIsEnabled());
        if (idVProviderRequest.getClaims() != null) {
            idVProvider.setClaimMappings(getClaimMap(idVProviderRequest.getClaims()));
        }
        if (idVProviderRequest.getConfigProperties() != null) {
            List<ConfigProperty> properties = idVProviderRequest.getConfigProperties();
            idVProvider.setIdVConfigProperties(
                    properties.stream().map(propertyToInternal).toArray(IdVConfigProperty[]::new));
        }
        return idVProvider;
    }

    private IdVProvider createUpdatedIdVProvider(IdVProvider oldIdVProvider,
                                                 IdVProviderRequest idVProviderRequest) {

        IdVProvider idVProvider = new IdVProvider();
        idVProvider.setIdVProviderUUID(oldIdVProvider.getIdVProviderUuid());
        idVProvider.setIdVProviderName(idVProviderRequest.getName());
        idVProvider.setIdVProviderDescription(idVProviderRequest.getDescription());
        idVProvider.setEnabled(idVProviderRequest.getIsEnabled());
        if (idVProviderRequest.getClaims() != null) {
            idVProvider.setClaimMappings(getClaimMap(idVProviderRequest.getClaims()));
        }
        if (idVProviderRequest.getConfigProperties() != null) {
            List<ConfigProperty> properties = idVProviderRequest.getConfigProperties();
            idVProvider.setIdVConfigProperties(
                    properties.stream().map(propertyToInternal).toArray(IdVConfigProperty[]::new));
        }
        return idVProvider;
    }

    private final Function<ConfigProperty, IdVConfigProperty> propertyToInternal = apiProperty -> {

        IdVConfigProperty idVConfigProperty = new IdVConfigProperty();
        // todo: handle null values
        idVConfigProperty.setName(apiProperty.getKey());
        idVConfigProperty.setValue(apiProperty.getValue());
        Boolean isSecret = apiProperty.getIsSecret();
        idVConfigProperty.setConfidential(isSecret != null && isSecret);
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
