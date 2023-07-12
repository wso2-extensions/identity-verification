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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.IDV_CLAIM_URI;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.IDV_CLAIM_UUID;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.IDV_PROVIDER_ID;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.TENANT_ID;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.USER_ID;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.ONFIDO;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.TOKEN;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.TOKEN_VALUE;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.setUpCarbonHome;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.Whitebox;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAOImpl;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;

import org.wso2.carbon.extension.identity.verification.mgt.model.IdVProperty;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, IdentityVerificationDataHolder.class, IdVProviderManager.class})
public class IdentityVerificationManagerImplTest extends PowerMockTestCase {

    private IdentityVerificationManagerImpl identityVerificationManager;
    @Mock
    private IdentityVerificationClaimDAOImpl identityVerificationClaimDAO;
    @Mock
    IdentityVerificationDataHolder identityVerificationDataHolder;
    @Mock
    private RealmService mockRealmService;
    @Mock
    UserRealm mockUserRealmFromRealmService;
    @Mock
    IdVProviderManager mockIdVProviderManager;
    @Mock
    UniqueIDUserStoreManager mockUniqueIDUserStoreManager;
    @Mock
    IdentityVerifierFactory identityVerifierFactory;
    @Mock
    IdentityVerifier identityVerifier;
    @Mock
    IdVProvider idVProvider;

    @BeforeMethod
    public void setUp() throws Exception {

        setUpCarbonHome();
        mockCarbonContextForTenant();
        mockIdentityTenantUtility();
        mockIsExistingUserCheck();

        mockStatic(IdentityVerificationDataHolder.class);
        when(IdentityVerificationDataHolder.getInstance()).thenReturn(identityVerificationDataHolder);
        List<IdentityVerificationClaimDAO> daoList = new ArrayList<>();
        daoList.add(identityVerificationClaimDAO);
        when(identityVerificationDataHolder.getIdVClaimDAOs()).thenReturn(daoList);
        identityVerificationManager = IdentityVerificationManagerImpl.getInstance();
        Whitebox.setInternalState(identityVerificationManager, "idVClaimDAOs", daoList);
    }

    @Test
    public void testVerifyIdentity() throws Exception {

        IdentityVerifierData identityVerifierData = getIdentityVerifierData();
        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);
        when(mockIdVProviderManager.getIdVProvider(anyString(), anyInt())).thenReturn(idVProvider);
        when(idVProvider.getType()).thenReturn(ONFIDO);

        when(identityVerificationDataHolder.
                getIdentityVerifierFactory(anyString())).thenReturn(identityVerifierFactory);
        when(identityVerifierFactory.getIdentityVerifier(anyString())).thenReturn(identityVerifier);
        when(identityVerifier.verifyIdentity(anyString(), any(IdentityVerifierData.class), anyInt())).
                thenReturn(identityVerifierData);

        IdentityVerifierData idVData = identityVerificationManager.verifyIdentity(USER_ID, identityVerifierData, TENANT_ID);
        Assert.assertEquals(idVData.getIdVProviderId(), IDV_PROVIDER_ID);
        Assert.assertEquals(idVData.getIdVProperties().get(0).getName(), TOKEN);
        Assert.assertEquals(idVData.getIdVProperties().get(0).getValue(), TOKEN_VALUE);
        Assert.assertEquals(idVData.getIdVClaims().get(0).getClaimUri(), IDV_CLAIM_URI);
    }

    @Test
    public void testGetIdVClaim() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        when(identityVerificationClaimDAO.getIDVClaim(anyString(), anyString(), anyInt())).thenReturn(getIdVClaim());
        doNothing().when(identityVerificationClaimDAO).addIdVClaimList(any(), anyInt());
        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        IdVClaim idVClaim = identityVerificationManager.getIdVClaim(USER_ID, IDV_CLAIM_UUID, TENANT_ID);
        Assert.assertEquals(idVClaim.getClaimUri(), "http://wso2.org/claims/dob");
        Assert.assertEquals(idVClaim.getClaimValue(), "1990-01-01");
        Assert.assertNotNull(idVClaim.getId());
    }

    @Test
    public void testGetIdVClaimWithUniqueValues() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        when(identityVerificationClaimDAO.getIDVClaim(anyString(), anyString(), anyString(), anyInt())).
                thenReturn(getIdVClaim());
        doNothing().when(identityVerificationClaimDAO).addIdVClaimList(any(), anyInt());
        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        IdVClaim idVClaim = identityVerificationManager.getIdVClaim(USER_ID, IDV_CLAIM_URI, IDV_CLAIM_UUID, TENANT_ID);
        Assert.assertEquals(idVClaim.getClaimUri(), "http://wso2.org/claims/dob");
    }

    @Test
    public void testAddIdVClaims() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimDataExist(anyString(), anyString(), anyString(), anyInt())).
                thenReturn(false);
        doNothing().when(identityVerificationClaimDAO).addIdVClaimList(anyList(), anyInt());
        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        List<IdVClaim> idVClaims = new ArrayList<>();
        idVClaims.add(getIdVClaim());
        List<IdVClaim> addedIdVClaims = identityVerificationManager.addIdVClaims(USER_ID, idVClaims, TENANT_ID);
        Assert.assertEquals(addedIdVClaims.get(0).getClaimUri(), "http://wso2.org/claims/dob");
    }

    @Test
    public void testUpdateIdVClaim() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        doNothing().when(identityVerificationClaimDAO).updateIdVClaim(any(IdVClaim.class), anyInt());
        IdVClaim idVClaim = getIdVClaim();
        IdVClaim updatedIdVClaim = identityVerificationManager.updateIdVClaim(USER_ID, idVClaim, TENANT_ID);
        Assert.assertEquals(updatedIdVClaim.getClaimUri(), "http://wso2.org/claims/dob");
    }

    @Test
    public void testUpdateIdVClaims() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        doNothing().when(identityVerificationClaimDAO).updateIdVClaim(any(IdVClaim.class), anyInt());
        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);

        List<IdVClaim> idVClaims = new ArrayList<>();
        IdVClaim idVClaim = getIdVClaim();
        idVClaim.setIsVerified(false);
        idVClaims.add(idVClaim);

        List<IdVClaim> updatedIdVClaim = identityVerificationManager.updateIdVClaims(USER_ID, idVClaims, TENANT_ID);
        Assert.assertFalse(updatedIdVClaim.get(0).isVerified());
    }

    @Test
    public void testDeleteIDVClaim() throws Exception {

        doNothing().when(identityVerificationClaimDAO).deleteIdVClaim(anyString(), anyString(), anyInt());
        identityVerificationManager.deleteIDVClaim(USER_ID, IDV_CLAIM_UUID, TENANT_ID);

        // Verify that the deleteIdVClaim method is called with the expected arguments
        verify(identityVerificationClaimDAO).deleteIdVClaim(USER_ID, IDV_CLAIM_UUID, TENANT_ID);

    }

    @Test
    public void testGetIDVClaims() throws Exception {

        when(identityVerificationClaimDAO.isIdVClaimExist(anyString(), anyInt())).thenReturn(true);
        when(identityVerificationClaimDAO.getIDVClaims(anyString(), anyString(), anyString(),
                anyInt())).thenReturn(getIdVClaims());
        when(identityVerificationDataHolder.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.isIdVProviderExists(anyString(), anyInt())).thenReturn(true);
        IdVClaim[] idVClaims = identityVerificationManager.
                getIdVClaims(USER_ID, IDV_PROVIDER_ID, IDV_CLAIM_URI, TENANT_ID);
        Assert.assertEquals(idVClaims.length, 1);
        Assert.assertEquals(idVClaims[0].getClaimUri(), "http://wso2.org/claims/dob");
    }

    private void mockIsExistingUserCheck() throws UserStoreException {

        when(identityVerificationDataHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealmFromRealmService);
        when(mockUserRealmFromRealmService.getUserStoreManager()).thenReturn(mockUniqueIDUserStoreManager);
        when(mockUniqueIDUserStoreManager.isExistingUserWithID(anyString())).thenReturn(true);
        doReturn("testValue").when(mockUniqueIDUserStoreManager).getUserClaimValueWithID
                (anyString(), anyString(), anyString());
    }

    private void mockCarbonContextForTenant() {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    private IdVClaim[] getIdVClaims() {

        IdVClaim[] idVClaims = new IdVClaim[1];
        idVClaims[0] = getIdVClaim();
        return idVClaims;
    }

    private IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setId("1");
        idVClaim.setUuid(IDV_CLAIM_UUID);
        idVClaim.setUserId(USER_ID);
        idVClaim.setClaimUri(IDV_CLAIM_URI);
        idVClaim.setClaimValue("1990-01-01");
        idVClaim.setIsVerified(true);
        idVClaim.setIdVPId(IDV_PROVIDER_ID);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "ONFIDO");
        metadata.put("trackingId", "123e4567-e89b-12d3-a456-556642440000");
        idVClaim.setMetadata(metadata);
        return idVClaim;
    }

    private static IdentityVerifierData getIdentityVerifierData() {

        IdentityVerifierData identityVerifierData = new IdentityVerifierData();
        identityVerifierData.setIdVProviderId(IDV_PROVIDER_ID);
        IdVProperty idVProperty = new IdVProperty();
        idVProperty.setName(TOKEN);
        idVProperty.setValue(TOKEN_VALUE);
        identityVerifierData.addIdVProperty(idVProperty);
        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setClaimUri(IDV_CLAIM_URI);
        identityVerifierData.addIdVClaimProperty(idVClaim);
        return identityVerifierData;
    }
}