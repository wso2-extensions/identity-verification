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
package org.wso2.carbon.extension.identity.verification.mgt;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAOImpl;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVProperty;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.*;

public class AbstractIdentityVerifierTest {

    AbstractIdentityVerifier abstractIdentityVerifier;
    @Mock
    IdentityVerificationDataHolder identityVerificationDataHolder;
    @Mock
    IdVProviderManager mockIdVProviderManager;
    @Mock
    private IdentityVerificationClaimDAOImpl identityVerificationClaimDAO;
    @Mock
    private RealmService mockRealmService;
    @Mock
    UserRealm mockUserRealmFromRealmService;
    @Mock
    UniqueIDUserStoreManager mockUniqueIDUserStoreManager;
    @Mock
    IdentityVerificationManagerImpl mockIdentityVerificationManager;

    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMockedStatic;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;
    private MockedStatic<IdentityVerificationDataHolder> identityVerificationDataHolderMockedStatic;
    private MockedStatic<IdentityVerificationManagerImpl> identityVerificationManagerImplMockedStatic;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        setUpCarbonHome();
        
        privilegedCarbonContextMockedStatic = mockStatic(PrivilegedCarbonContext.class);
        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        identityVerificationDataHolderMockedStatic = mockStatic(IdentityVerificationDataHolder.class);
        
        mockCarbonContextForTenant();
        mockIdentityTenantUtility();
        mockIsExistingUserCheck();
        mockIdentityVerificationClaimDAO();

        abstractIdentityVerifier = new AbstractIdentityVerifier() {

            @Override
            public IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData,
                                                       int tenantId) throws IdentityVerificationException {
                return null;
            }
        };
    }

    @AfterMethod
    public void tearDown() {

        if (privilegedCarbonContextMockedStatic != null) {
            privilegedCarbonContextMockedStatic.close();
        }
        if (identityTenantUtilMockedStatic != null) {
            identityTenantUtilMockedStatic.close();
        }
        if (identityVerificationDataHolderMockedStatic != null) {
            identityVerificationDataHolderMockedStatic.close();
        }
        if (identityVerificationManagerImplMockedStatic != null) {
            identityVerificationManagerImplMockedStatic.close();
        }
    }

    @Test
    public void testGetIdVProvider() throws Exception {

        IdentityVerifierData identityVerifierData = new IdentityVerifierData();
        identityVerifierData.setIdVProviderId(IDV_PROVIDER_ID);
        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.getIdVProvider(anyString(), anyInt())).thenReturn(getTestIdVProvider());

        IdVProvider retrievedIdVProvider = abstractIdentityVerifier.getIdVProvider(identityVerifierData, TENANT_ID);
        Assert.assertEquals(retrievedIdVProvider.getIdVProviderUuid(), IDV_PROVIDER_ID);
    }

    @Test
    public void testGetIdVPClaimWithValueMap() throws Exception {

        when(mockUniqueIDUserStoreManager.getUserClaimValueWithID(anyString(), anyString(),
                nullable(String.class))).thenReturn("test");
        IdentityVerifierData identityVerifierData = getIdentityVerifierData();

        Map<String, String> idVPClaimWithValueMap =
                abstractIdentityVerifier.getIdVPClaimWithValueMap(USER_ID, identityVerifierData, TENANT_ID);
        Assert.assertEquals(idVPClaimWithValueMap.get(IDV_CLAIM_URI), "test");
    }

    @Test
    public void testGetIdVConfigPropertyMap() {

        IdVProvider idVProvider = getTestIdVProvider();
        Map<String, String> idVConfigPropertMap = abstractIdentityVerifier.getIdVConfigPropertyMap(idVProvider);
        Assert.assertEquals(idVConfigPropertMap.get("token"), "1234-5678-91234-654246");
        Assert.assertEquals(idVConfigPropertMap.get("apiUrl"), "https://api.test.com/v1/");
    }

    @Test
    public void testStoreIdVClaims() throws Exception {

        List<IdVClaim> idvClaimList = Arrays.asList(getIdVClaims());
        identityVerificationManagerImplMockedStatic = mockStatic(IdentityVerificationManagerImpl.class);
        identityVerificationManagerImplMockedStatic.when(IdentityVerificationManagerImpl::getInstance)
                .thenReturn(mockIdentityVerificationManager);
        when(mockIdentityVerificationManager.
                addIdVClaims(anyString(), anyList(), anyInt())).thenReturn(idvClaimList);
        List<IdVClaim> storedIdVClaims =
                abstractIdentityVerifier.storeIdVClaims(USER_ID, idvClaimList, TENANT_ID);
        Assert.assertEquals(storedIdVClaims.size(), 1);
        Assert.assertNotNull(storedIdVClaims.get(0).getUuid());
        Assert.assertEquals(storedIdVClaims.get(0).getUserId(), USER_ID);
    }

    @Test
    public void testUpdateIdVClaim() throws Exception {

        IdVClaim idVClaim = getIdVClaim();
        identityVerificationManagerImplMockedStatic = mockStatic(IdentityVerificationManagerImpl.class);
        identityVerificationManagerImplMockedStatic.when(IdentityVerificationManagerImpl::getInstance)
                .thenReturn(mockIdentityVerificationManager);
        when(mockIdentityVerificationManager.
                updateIdVClaim(anyString(), any(IdVClaim.class), anyInt())).thenReturn(idVClaim);
        IdVClaim updatedIdVClaim = abstractIdentityVerifier.updateIdVClaim(USER_ID, idVClaim, TENANT_ID);
        Assert.assertTrue(updatedIdVClaim.isVerified());
    }

    @Test
    public void testUpdateIdVClaims() throws Exception {

        List<IdVClaim> idvClaimList = Arrays.asList(getIdVClaims());
        identityVerificationManagerImplMockedStatic = mockStatic(IdentityVerificationManagerImpl.class);
        identityVerificationManagerImplMockedStatic.when(IdentityVerificationManagerImpl::getInstance)
                .thenReturn(mockIdentityVerificationManager);
        when(mockIdentityVerificationManager.
                updateIdVClaims(anyString(), anyList(), anyInt())).thenReturn(idvClaimList);

        List<IdVClaim> updatedIdVClaims =
                abstractIdentityVerifier.updateIdVClaims(USER_ID, idvClaimList, TENANT_ID);
        Assert.assertTrue(updatedIdVClaims.get(0).isVerified());
    }

    private IdVClaim[] getIdVClaims() {

        IdVClaim[] idVClaims = new IdVClaim[1];
        idVClaims[0] = getIdVClaim();
        return idVClaims;
    }

    private IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUuid(IDV_CLAIM_UUID);
        idVClaim.setUserId(USER_ID);
        idVClaim.setClaimUri("http://wso2.org/claims/dob");
        idVClaim.setIsVerified(true);
        idVClaim.setIdVPId(IDV_PROVIDER_ID);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "ONFIDO");
        metadata.put("trackingId", "123e4567-e89b-12d3-a456-556642440000");
        idVClaim.setMetadata(metadata);
        return idVClaim;
    }

    private IdVProvider getTestIdVProvider() {

        IdVProvider idVProvider = new IdVProvider();
        idVProvider.setIdVProviderUUID(IDV_PROVIDER_ID);
        idVProvider.setType(IDV_PROVIDER_TYPE);
        idVProvider.setIdVProviderName(IDV_PROVIDER_NAME);
        idVProvider.setIdVProviderDescription("ONFIDO identity verification provider");

        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put("http://wso2.org/claims/givenname", "firstName");
        claimMappings.put("http://wso2.org/claims/lastname", "lastName");
        idVProvider.setClaimMappings(claimMappings);

        IdVConfigProperty[] idVConfigProperties = new IdVConfigProperty[2];
        IdVConfigProperty idVConfigProperty1 = new IdVConfigProperty();
        idVConfigProperty1.setName("token");
        idVConfigProperty1.setValue("1234-5678-91234-654246");
        idVConfigProperty1.setConfidential(true);
        idVConfigProperties[0] = idVConfigProperty1;

        IdVConfigProperty idVConfigProperty2 = new IdVConfigProperty();
        idVConfigProperty2.setName("apiUrl");
        idVConfigProperty2.setValue("https://api.test.com/v1/");
        idVConfigProperty2.setConfidential(false);
        idVConfigProperties[1] = idVConfigProperty2;

        idVProvider.setIdVConfigProperties(idVConfigProperties);
        return idVProvider;
    }

    private IdentityVerifierData getIdentityVerifierData() {

        IdentityVerifierData identityVerifierData = new IdentityVerifierData();
        identityVerifierData.setIdVProviderId(IDV_PROVIDER_ID);

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setClaimUri(IDV_CLAIM_URI);
        identityVerifierData.addIdVClaimProperty(idVClaim);

        List<IdVProperty> idVProperties = new ArrayList<>();
        IdVProperty idVProperty = new IdVProperty();
        idVProperty.setName("status");
        idVProperty.setValue("INITIATED");
        idVProperties.add(idVProperty);
        identityVerifierData.setIdVProperties(idVProperties);
        return identityVerifierData;
    }

    private void mockIsExistingUserCheck() throws UserStoreException {

        when(identityVerificationDataHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealmFromRealmService);
        when(mockUserRealmFromRealmService.getUserStoreManager()).thenReturn(mockUniqueIDUserStoreManager);
        when(mockUniqueIDUserStoreManager.isExistingUserWithID(anyString())).thenReturn(true);
    }

    private void mockIdentityVerificationClaimDAO() {

        when(IdentityVerificationDataHolder.getInstance()).thenReturn(identityVerificationDataHolder);
        when(identityVerificationDataHolder.getIdVClaimDAOs()).
                thenReturn(Collections.singletonList(identityVerificationClaimDAO));
    }

    private void mockCarbonContextForTenant() {

        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContextMockedStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantDomain(any(Integer.class)))
                .thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }
}
