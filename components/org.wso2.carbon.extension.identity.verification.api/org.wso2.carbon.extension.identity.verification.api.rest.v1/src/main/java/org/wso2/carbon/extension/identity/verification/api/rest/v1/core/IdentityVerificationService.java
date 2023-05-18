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

import org.wso2.carbon.extension.identity.verification.api.rest.common.Constants;
import org.wso2.carbon.extension.identity.verification.api.rest.common.IdentityVerificationServiceHolder;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.ProviderProperty;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.VerificationClaimRequest;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.VerificationClaimResponse;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.VerificationClaimUpdateRequest;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.VerificationPostResponse;
import org.wso2.carbon.extension.identity.verification.api.rest.v1.model.VerifyRequest;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVProperty;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.extension.identity.verification.api.rest.v1.core.IdentityVerificationUtils.getTenantId;
import static org.wso2.carbon.extension.identity.verification.api.rest.v1.core.IdentityVerificationUtils.handleException;

/**
 * Service class for identity verification.
 */
public class IdentityVerificationService {

    /**
     * Add identity verification claim.
     *
     * @param verificationClaimRequest Verification claim request.
     * @return Verification claim response.
     */
    public List<VerificationClaimResponse> addIdVClaims(String userId,
                                                        List<VerificationClaimRequest> verificationClaimRequest) {

        List<IdVClaim> idVClaims;
        int tenantId = getTenantId();
        try {
            idVClaims = IdentityVerificationServiceHolder.getIdentityVerificationManager().
                    addIdVClaims(userId, createIdVClaims(userId, verificationClaimRequest), tenantId);
        } catch (IdentityVerificationException e) {
            if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID.getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_USER_ID_NOT_FOUND, userId);
            } else if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_PROVIDER.getCode().
                    equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_IDV_PROVIDER_NOT_FOUND, userId);
            } else if (IdentityVerificationConstants.ErrorMessage.ERROR_IDV_CLAIM_DATA_ALREADY_EXISTS.getCode().
                    equals(e.getErrorCode())) {
                throw handleException(Response.Status.CONFLICT, Constants.ErrorMessage.ERROR_CODE_IDV_CLAIM_CONFLICT,
                        userId);
            } else {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_ADDING_VERIFICATION_CLAIM, userId);
            }
        }
        return createVerificationClaimsResponse(idVClaims);
    }

    /**
     * Get identity verification claim.
     *
     * @param claimId Claim id.
     * @return Verification claim response.
     */
    public VerificationClaimResponse getIdVClaim(String userId, String claimId) {

        IdVClaim idVClaim;
        int tenantId = getTenantId();
        try {
            idVClaim = IdentityVerificationServiceHolder.
                    getIdentityVerificationManager().getIdVClaim(userId, claimId, tenantId);
            if (idVClaim == null) {
                throw IdentityVerificationUtils.handleException(Response.Status.NOT_FOUND,
                        Constants.ErrorMessage.ERROR_CODE_IDV_CLAIM_NOT_FOUND, claimId);
            }
        } catch (IdentityVerificationException e) {
            if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID.getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_USER_ID_NOT_FOUND, userId);
            } else if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_CLAIM_ID.
                    getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_CLAIM_ID_NOT_FOUND, claimId);
            } else {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_GETTING_VERIFICATION_CLAIM, claimId);
            }
        }
        return createVerificationClaimResponse(idVClaim);
    }

    /**
     * Update identity verification claim.
     *
     * @param userId                         User id.
     * @param claimId                        Claim id.
     * @param verificationClaimUpdateRequest Verification claim update request.
     * @return Verification claim response.
     */
    public VerificationClaimResponse updateIdVClaim(String userId, String claimId,
                                                    VerificationClaimUpdateRequest verificationClaimUpdateRequest) {

        IdVClaim idVClaim;
        int tenantId = getTenantId();
        try {
            idVClaim = IdentityVerificationServiceHolder.getIdentityVerificationManager().
                    getIdVClaim(userId, claimId, tenantId);
            if (idVClaim == null) {
                throw IdentityVerificationUtils.handleException(Response.Status.NOT_FOUND,
                        Constants.ErrorMessage.ERROR_CODE_IDV_CLAIM_NOT_FOUND, claimId);
            }
            if (verificationClaimUpdateRequest.getIsVerified() == null ||
                    verificationClaimUpdateRequest.getClaimMetadata() == null) {
                throw IdentityVerificationUtils.handleException(Response.Status.BAD_REQUEST,
                        Constants.ErrorMessage.ERROR_CODE_INCOMPLETE_UPDATE_REQUEST, claimId);
            }
            idVClaim = IdentityVerificationServiceHolder.getIdentityVerificationManager().
                    updateIdVClaim(userId, createIdVClaims(verificationClaimUpdateRequest, userId, claimId), tenantId);
        } catch (IdentityVerificationException e) {
            if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID.getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_USER_ID_NOT_FOUND, userId);
            } else if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_CLAIM_ID.
                    getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_CLAIM_ID_NOT_FOUND, claimId);
            } else {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_UPDATING_VERIFICATION_CLAIM, claimId);
            }
        }
        return createVerificationClaimResponse(idVClaim);
    }

    /**
     * Update identity verification claim.
     *
     * @param userId                         User id.
     * @param verificationClaimRequest Verification claim request..
     * @return Verification claim response.
     */
    public List<VerificationClaimResponse> updateIdVClaims(String userId,
                                                     List<VerificationClaimRequest> verificationClaimRequest) {

        List<IdVClaim> idVClaims;
        int tenantId = getTenantId();
        try {
            idVClaims = IdentityVerificationServiceHolder.getIdentityVerificationManager().
                    updateIdVClaims(userId, createIdVClaims(userId, verificationClaimRequest), tenantId);
        } catch (IdentityVerificationException e) {
            if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID.getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_USER_ID_NOT_FOUND, userId);
            } else if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_PROVIDER.getCode().
                    equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_IDV_PROVIDER_NOT_FOUND, userId);
            } else if (IdentityVerificationConstants.ErrorMessage.ERROR_IDV_CLAIM_DATA_ALREADY_EXISTS.getCode().
                    equals(e.getErrorCode())) {
                throw handleException(Response.Status.CONFLICT, Constants.ErrorMessage.ERROR_CODE_IDV_CLAIM_CONFLICT,
                        userId);
            } else {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_ADDING_VERIFICATION_CLAIM, userId);
            }
        }
        return createVerificationClaimsResponse(idVClaims);
    }

    /**
     * Get identity verification info.
     *
     * @param userId        User id.
     * @param idvProviderId Identity verification provider id.
     * @return Identity verification info.
     */
    public List<VerificationClaimResponse> getIdVClaims(String userId, String idvProviderId) {

        IdVClaim[] idVClaims;
        int tenantId = getTenantId();
        try {
            idVClaims = IdentityVerificationServiceHolder.getIdentityVerificationManager().
                    getIdVClaims(userId, idvProviderId, tenantId);
        } catch (IdentityVerificationException e) {
            if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID.getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_USER_ID_NOT_FOUND, userId);
            }else if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_IDV_PROVIDER.getCode().
                    equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_IDV_PROVIDER_NOT_FOUND, userId);
            } else {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_RETRIEVING_USER_IDV_CLAIMS, userId);
            }
        }
        return createVerificationGetResponse(idVClaims);
    }

    /**
     * Verify an identity.
     *
     * @param verifyRequest Verification claim request.
     * @return Verification post response.
     */
    public VerificationPostResponse verifyIdentity(String userId, VerifyRequest verifyRequest) {

        IdentityVerifierData identityVerifierResponse;
        IdentityVerifierData identityVerifierData = getIdentityVerifierData(verifyRequest);
        int tenantId = getTenantId();
        try {
            identityVerifierResponse = IdentityVerificationServiceHolder.getIdentityVerificationManager().
                    verifyIdentity(userId, identityVerifierData, tenantId);
        } catch (IdentityVerificationException e) {
            if (IdentityVerificationConstants.ErrorMessage.ERROR_INVALID_USER_ID.getCode().equals(e.getErrorCode())) {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_CODE_USER_ID_NOT_FOUND, userId);
            } else {
                throw IdentityVerificationUtils.handleIdVException(e,
                        Constants.ErrorMessage.ERROR_PERFORMING_IDENTITY_VERIFICATION, userId);
            }
        }
        return getVerificationPostResponse(identityVerifierResponse);
    }

    /**
     * Create IdVClaim object based on the VerificationClaimPostRequest.
     *
     * @param userId                   User id.
     * @param verificationClaimRequest Verification claim request.
     * @return Identity verification info.
     */
    private List<IdVClaim> createIdVClaims(String userId, List<VerificationClaimRequest> verificationClaimRequest) {

        List<IdVClaim> idVClaimList = new ArrayList<>();
        for (VerificationClaimRequest verificationClaim : verificationClaimRequest) {
            IdVClaim idVClaim = new IdVClaim();
            idVClaim.setUserId(userId);
            idVClaim.setClaimUri(verificationClaim.getUri());
            idVClaim.setIsVerified(verificationClaim.getIsVerified());
            idVClaim.setIdVPId(verificationClaim.getIdvProviderId());
            idVClaim.setMetadata(verificationClaim.getClaimMetadata());
            idVClaimList.add(idVClaim);
        }
        return idVClaimList;
    }

    private IdVClaim createIdVClaims(VerificationClaimUpdateRequest verificationClaimUpdateRequest, String userId,
                                     String claimId) {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUuid(claimId);
        idVClaim.setUserId(userId);
        idVClaim.setIsVerified(verificationClaimUpdateRequest.getIsVerified());
        idVClaim.setMetadata(verificationClaimUpdateRequest.getClaimMetadata());
        return idVClaim;
    }

    private IdentityVerifierData getIdentityVerifierData(VerifyRequest verifyRequest) {

        IdentityVerifierData identityVerifier = new IdentityVerifierData();
        identityVerifier.setIdVProviderId(verifyRequest.getIdVProviderId());
        if (verifyRequest.getProperties() == null) {
            return identityVerifier;
        }
        for (ProviderProperty property : verifyRequest.getProperties()) {
            IdVProperty idVProperty = new IdVProperty();
            idVProperty.setName(property.getKey());
            idVProperty.setValue(property.getValue());
            identityVerifier.addIdVProperty(idVProperty);
        }
        for (String claim : verifyRequest.getClaims()) {
            IdVClaim idVClaim = new IdVClaim();
            idVClaim.setClaimUri(claim);
            identityVerifier.addIdVClaimProperty(idVClaim);
        }
        return identityVerifier;
    }

    private VerificationPostResponse getVerificationPostResponse(IdentityVerifierData identityVerifierData) {

        VerificationPostResponse verificationPostResponse = new VerificationPostResponse();
        verificationPostResponse.setIdVProviderId(identityVerifierData.getIdVProviderId());
        for (IdVClaim idVClaim : identityVerifierData.getIdVClaims()) {
            VerificationClaimResponse verificationClaimResponse = new VerificationClaimResponse();
            verificationClaimResponse.setId(idVClaim.getUuid());
            verificationClaimResponse.setUri(idVClaim.getClaimUri());
            verificationClaimResponse.setIsVerified(idVClaim.isVerified());
            verificationClaimResponse.setClaimMetadata(idVClaim.getMetadata());
            verificationPostResponse.addClaimsItem(verificationClaimResponse);
        }
        return verificationPostResponse;
    }

    private VerificationClaimResponse createVerificationClaimResponse(IdVClaim idVClaim) {

        VerificationClaimResponse verificationClaimResponse = new VerificationClaimResponse();
        verificationClaimResponse.setId(idVClaim.getUuid());
        verificationClaimResponse.setUri(idVClaim.getClaimUri());
        verificationClaimResponse.setIsVerified(idVClaim.isVerified());
        verificationClaimResponse.setClaimMetadata(idVClaim.getMetadata());
        return verificationClaimResponse;
    }

    private List<VerificationClaimResponse> createVerificationClaimsResponse(List<IdVClaim> idVClaims) {

        List<VerificationClaimResponse> verificationClaimResponseList = new ArrayList<>();
        for (IdVClaim idVClaim : idVClaims) {
            VerificationClaimResponse verificationClaimResponse = new VerificationClaimResponse();
            verificationClaimResponse.setId(idVClaim.getUuid());
            verificationClaimResponse.setUri(idVClaim.getClaimUri());
            verificationClaimResponse.setIsVerified(idVClaim.isVerified());
            verificationClaimResponse.setClaimMetadata(idVClaim.getMetadata());
            verificationClaimResponseList.add(verificationClaimResponse);
        }
        return verificationClaimResponseList;

    }

    private List<VerificationClaimResponse> createVerificationGetResponse(IdVClaim[] idVClaims) {

        List<VerificationClaimResponse> verificationClaimList = new ArrayList<>();
        for (IdVClaim idVClaim : idVClaims) {
            VerificationClaimResponse verificationClaimResponse = new VerificationClaimResponse();
            verificationClaimResponse.setId(idVClaim.getUuid());
            verificationClaimResponse.setUri(idVClaim.getClaimUri());
            verificationClaimResponse.setIsVerified(idVClaim.isVerified());
            verificationClaimResponse.setClaimMetadata(idVClaim.getMetadata());
            verificationClaimList.add(verificationClaimResponse);
        }
        return verificationClaimList;
    }
}
