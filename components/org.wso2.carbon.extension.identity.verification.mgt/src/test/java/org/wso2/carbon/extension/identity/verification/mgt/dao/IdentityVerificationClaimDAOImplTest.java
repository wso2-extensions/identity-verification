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

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.ONFIDO;
import static org.wso2.carbon.extension.identity.verification.mgt.util.TestUtils.setUpCarbonHome;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class})
public class IdentityVerificationClaimDAOImplTest extends PowerMockTestCase {

    private IdentityVerificationClaimDAO identityVerificationClaimDAO;
    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private static final String DB_NAME = "test";
    private final String IDV_CLAIM_UUID = "d245799b-28bc-4fdb-abb4-e265038320by";
    private final String IDV_CLAIM_URI = "http://wso2.org/claims/dob";
    private final String USER_ID = "715558cb-d9c1-4a23-af09-3d95284d8e2b";
    private final String IDV_PROVIDER_ID = "1c7ce08b-2ebc-4b9e-a107-3b129c019954";
    private final int TENANT_ID = -1234;

    @BeforeMethod
    public void setUp() throws Exception {

        setUpCarbonHome();
        identityVerificationClaimDAO = new IdentityVerificationClaimDAOImpl();
        IdentityVerificationDataHolder.getInstance().
                setIdVClaimDAOs(Collections.singletonList(identityVerificationClaimDAO));
        initiateH2Database(getFilePath("h2.sql"));
        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();
        mockStatic(IdentityDatabaseUtil.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @Test(priority = 1)
    public void testAddIdVClaimList() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, TENANT_ID);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaimList.get(0).getClaimUri());
        }
    }

    @Test(priority = 2)
    public void testUpdateIdVClaim() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, TENANT_ID);
        }

        IdVClaim updatedClaim = getIdVClaim();
        updatedClaim.setIsVerified(false);
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.updateIdVClaim(updatedClaim, TENANT_ID);
        }
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertFalse(identityVerificationClaim.isVerified());
        }
    }

    @Test(priority = 3)
    public void testGetIDVClaimWithUniqueValues() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaimList.get(0).getClaimUri());
        }
    }

    @Test(priority = 3)
    public void testGetIDVClaim() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_UUID, TENANT_ID);
            Assert.assertEquals(identityVerificationClaim.getClaimUri(), idVClaimList.get(0).getClaimUri());
        }
    }

    @Test(priority = 4)
    public void testGetIDVClaims() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, TENANT_ID);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            IdVClaim[] retrievedIdVClaimList = identityVerificationClaimDAO.
                    getIDVClaims(USER_ID, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertEquals(retrievedIdVClaimList.length, idVClaimList.size());
        }
    }

    @Test(priority = 5)
    public void testIsIdVClaimDataExist() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            boolean isIdVClaimDataExist =
                    identityVerificationClaimDAO.isIdVClaimDataExist(USER_ID, IDV_PROVIDER_ID,
                            "http://wso2.org/claims/dob", TENANT_ID);
            Assert.assertTrue(isIdVClaimDataExist);
        }
    }

    @Test(priority = 6)
    public void testIsIdVClaimExist() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            boolean isIdVClaimDataExist = identityVerificationClaimDAO.isIdVClaimExist(IDV_CLAIM_UUID, TENANT_ID);
            Assert.assertTrue(isIdVClaimDataExist);
        }
    }

    @Test(priority = 7)
    public void testDeleteIdVClaim() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.deleteIdVClaim(USER_ID, IDV_CLAIM_UUID, TENANT_ID);
        }
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim identityVerificationClaim = identityVerificationClaimDAO.
                    getIDVClaim(USER_ID, IDV_CLAIM_URI, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertNull(identityVerificationClaim);
        }
    }

    @Test(priority = 8)
    public void testDeleteIdVClaims() throws Exception {

        List<IdVClaim> idVClaimList = getTestIdVClaims();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            identityVerificationClaimDAO.addIdVClaimList(idVClaimList, -1234);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));

            identityVerificationClaimDAO.deleteIdVClaims(USER_ID, getIdVClaims(), TENANT_ID);
        }
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
            IdVClaim[] retrievedIdVClaimList = identityVerificationClaimDAO.
                    getIDVClaims(USER_ID, IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertEquals(retrievedIdVClaimList.length, 0);
        }
    }

    @Test
    public void testGetPriority() {

        int priority = identityVerificationClaimDAO.getPriority();
        Assert.assertEquals(priority, 1);
    }

    private void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        when(privilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    private List<IdVClaim> getTestIdVClaims() {

        List<IdVClaim> idVClaims = new ArrayList<>();
        IdVClaim idVClaim = getIdVClaim();
        idVClaims.add(idVClaim);
        return idVClaims;
    }

    private IdVClaim[] getIdVClaims() {

        IdVClaim[] idVClaims = new IdVClaim[1];
        idVClaims[0] = getIdVClaim();
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

    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static String getFilePath(String fileName) {

        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }

    private static Connection getConnection(String database) throws SQLException {

        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + database);
    }

    private void initiateH2Database(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + IdentityVerificationClaimDAOImplTest.DB_NAME);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(IdentityVerificationClaimDAOImplTest.DB_NAME, dataSource);
    }
}
