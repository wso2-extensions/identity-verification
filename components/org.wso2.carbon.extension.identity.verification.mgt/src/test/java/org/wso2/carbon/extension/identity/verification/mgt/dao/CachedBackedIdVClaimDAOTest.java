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

package org.wso2.carbon.extension.identity.verification.mgt.dao;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimByIdCache;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimByIdCacheKey;
import org.wso2.carbon.extension.identity.verification.mgt.cache.IdVClaimCacheEntry;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.DB_NAME;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.ONFIDO;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.closeH2Database;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.dataSourceMap;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.getConnection;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.getFilePath;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.initiateH2Database;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.setUpCarbonHome;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

public class CachedBackedIdVClaimDAOTest {

    private IdentityVerificationClaimDAO identityVerificationClaimDAO;
    private CachedBackedIdVClaimDAO cachedBackedIdVClaimDAO;
    private IdVClaimByIdCache idVClaimByIdCache;
    private IdVClaimByIdCacheKey idVClaimByIdCacheKey;
    private IdVClaimCacheEntry idVClaimCacheEntry;
    private final String IDV_CLAIM_UUID = "d245799b-28bc-4fdb-abb4-e265038320by";
    private final String IDV_CLAIM_URI = "http://wso2.org/claims/dob";
    private final String USER_ID = "715558cb-d9c1-4a23-af09-3d95284d8e2b";
    private final String IDV_PROVIDER_ID = "1c7ce08b-2ebc-4b9e-a107-3b129c019954";
    private final int TENANT_ID = -1234;

    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMockedStatic;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtilMockedStatic;
    private MockedStatic<IdVClaimByIdCache> idVClaimByIdCacheMockedStatic;

    @BeforeClass
    public void init() throws Exception {

        initiateH2Database(getFilePath());
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @BeforeMethod
    public void setUp() {

        setUpCarbonHome();
        identityVerificationClaimDAO = new IdentityVerificationClaimDAOImpl();
        IdentityVerificationDataHolder.getInstance().setIdVClaimDAOs(Collections.
                singletonList(identityVerificationClaimDAO));

        idVClaimByIdCacheMockedStatic = mockStatic(IdVClaimByIdCache.class);
        idVClaimByIdCache = mock(IdVClaimByIdCache.class);
        idVClaimByIdCacheMockedStatic.when(IdVClaimByIdCache::getInstance).thenReturn(idVClaimByIdCache);

        cachedBackedIdVClaimDAO = new CachedBackedIdVClaimDAO(identityVerificationClaimDAO);
        IdentityVerificationDataHolder.getInstance().setIdVClaimDAOs(Collections.
                singletonList(cachedBackedIdVClaimDAO));

        idVClaimByIdCacheKey = mock(IdVClaimByIdCacheKey.class);
        idVClaimCacheEntry = mock(IdVClaimCacheEntry.class);

        identityDatabaseUtilMockedStatic = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));

        privilegedCarbonContextMockedStatic = mockStatic(PrivilegedCarbonContext.class);
        mockCarbonContextForTenant();
    }

    @AfterMethod
    public void tearDownMethod() {

        if (privilegedCarbonContextMockedStatic != null) {
            privilegedCarbonContextMockedStatic.close();
        }
        if (identityDatabaseUtilMockedStatic != null) {
            identityDatabaseUtilMockedStatic.close();
        }
        if (idVClaimByIdCacheMockedStatic != null) {
            idVClaimByIdCacheMockedStatic.close();
        }
    }

    @Test
    public void testAddIdVClaimList() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));
            cachedBackedIdVClaimDAO.addIdVClaimList(idVClaimList, TENANT_ID);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim identityVerificationClaim = cachedBackedIdVClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaimList.get(0).getClaimUri());
        }
    }

    @Test
    public void testUpdateIdVClaim() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim updatedClaim = getIdVClaim2();
            cachedBackedIdVClaimDAO.updateIdVClaim(updatedClaim, TENANT_ID);
        }
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertFalse(identityVerificationClaim.isVerified());
        }
    }
    @Test(priority = 3)
    public void testGetIDVClaimWithUniqueValues() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim identityVerificationClaim = cachedBackedIdVClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaimList.get(0).getClaimUri());
        }
    }


    @Test(priority = 4)
    public void testGetIDVClaim() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim identityVerificationClaim = cachedBackedIdVClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_UUID, TENANT_ID);
            Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaimList.get(0).getClaimUri());
        }
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaimList.get(0).getClaimUri());
        }
    }

    @Test(priority = 5)
    public void testGetIDVClaims() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim[] retrievedIdVClaimList = cachedBackedIdVClaimDAO.
                    getIDVClaims(USER_ID, IDV_PROVIDER_ID, null, TENANT_ID);
            Assert.assertEquals(retrievedIdVClaimList.length, idVClaimList.size());
        }
    }

    @Test(priority = 6)
    public void testIsIdVClaimDataExist() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));

            boolean isIdVClaimDataExist =
                    cachedBackedIdVClaimDAO.isIdVClaimDataExist(USER_ID, IDV_PROVIDER_ID,
                            "http://wso2.org/claims/dob", TENANT_ID);
            Assert.assertTrue(isIdVClaimDataExist);
        }
    }

    @Test(priority = 6)
    public void testIsIdVClaimExist() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));

            boolean isIdVClaimDataExist = cachedBackedIdVClaimDAO.isIdVClaimExist(IDV_CLAIM_UUID, TENANT_ID);
            Assert.assertTrue(isIdVClaimDataExist);
        }
    }

    @Test(priority = 7)
    public void testDeleteIdVClaim() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));
            cachedBackedIdVClaimDAO.deleteIdVClaim(USER_ID, IDV_CLAIM_UUID, TENANT_ID);
        }
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource)
                    .thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim identityVerificationClaim = cachedBackedIdVClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertNull(identityVerificationClaim);
        }
    }

    @Test(priority = 8)
    public void testDeleteIdVClaims() throws Exception {

        IdentityVerificationClaimDAO identityVerificationClaimDAO = mock(IdentityVerificationClaimDAO.class);

        // Prepare test data
        IdVClaim[] idVClaims = new IdVClaim[] { getIdVClaim() };

        // Configure the behavior of the mocked method
        when(identityVerificationClaimDAO.getIDVClaims(USER_ID, null, null, TENANT_ID)).
                thenReturn(idVClaims);

        // Create an instance of CachedBackedIdVClaimDAO using the mocked IdentityVerificationClaimDAO
        CachedBackedIdVClaimDAO cachedBackedIdVClaimDAO = new CachedBackedIdVClaimDAO(identityVerificationClaimDAO);

        // Mock the necessary dependencies for the database connection
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        // Stub the necessary method calls to return the mocked connection and prepared statement
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // Call the deleteIdVClaims method
        cachedBackedIdVClaimDAO.deleteIdVClaims(USER_ID, null, null, TENANT_ID);

        // Verify the expected interactions
        verify(identityVerificationClaimDAO, times(1))
                .deleteIdVClaims(USER_ID, null, null, TENANT_ID);
    }

    @Test
    public void testGetPriority() {

        int priority = cachedBackedIdVClaimDAO.getPriority();
        Assert.assertEquals(priority, 2);
    }

    private void mockCarbonContextForTenant() {

        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContextMockedStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        Mockito.when(privilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        Mockito.when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private List<IdVClaim> getTestIdVClaims() {

        List<IdVClaim> idVClaims = new ArrayList<>();
        IdVClaim idVClaim = getIdVClaim();
        idVClaims.add(idVClaim);
        return idVClaims;
    }

    private IdVClaim getIdVClaim() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUserId(USER_ID);
        idVClaim.setUuid(IDV_CLAIM_UUID);
        idVClaim.setClaimUri(IDV_CLAIM_URI);
        idVClaim.setIsVerified(true);
        idVClaim.setIdVPId(IDV_PROVIDER_ID);
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("source", ONFIDO);
        metadata.put("trackingId", "12sf5-bhd687-onhf7-8hjg-9hjg6");
        idVClaim.setMetadata(metadata);
        return idVClaim;
    }

    private IdVClaim getIdVClaim2() {

        IdVClaim idVClaim = new IdVClaim();
        idVClaim.setUserId(USER_ID);
        idVClaim.setUuid(IDV_CLAIM_UUID);
        idVClaim.setClaimUri(IDV_CLAIM_URI);
        idVClaim.setIsVerified(false);
        idVClaim.setIdVPId(IDV_PROVIDER_ID);
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("source", ONFIDO);
        metadata.put("trackingId", "12sf5-bhd687-onhf7-8hjg-9hjg6");
        idVClaim.setMetadata(metadata);
        return idVClaim;
    }
}
