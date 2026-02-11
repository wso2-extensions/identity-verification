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
package org.wso2.carbon.extension.identity.verification.provider.dao;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.provider.IdVPSecretProcessor;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.createExpressionNodeList;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.dataSourceMap;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getOldIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getTestIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.IDV_PROVIDER_1_UUID;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.IDV_PROVIDER_1_NAME;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.TENANT_ID;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.closeH2Database;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getConnection;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getFilePath;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.initiateH2Database;

/**
 * Test class for IdVProviderDAOImpl.
 */
public class IdVProviderDAOImplTest {

    private IdVProviderDAO idVProviderDAO;
    private SecretManagerImpl secretManager;
    private SecretResolveManagerImpl secretResolveManager;
    private static final String DB_NAME = "test";

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtilMockedStatic;
    private MockedConstruction<IdVPSecretProcessor> idVPSecretProcessorMockedConstruction;
    private MockedConstruction<SecretManagerImpl> secretManagerMockedConstruction;
    private MockedConstruction<SecretResolveManagerImpl> secretResolveManagerMockedConstruction;

    @BeforeClass
    public void init() throws Exception {

        initiateH2Database(getFilePath());
    }

    @BeforeMethod
    public void setUp() throws Exception {

        idVProviderDAO = new IdVProviderDAOImpl();
        IdVProviderDataHolder.getInstance().setIdVProviderDAOs(Collections.singletonList(idVProviderDAO));
        
        identityDatabaseUtilMockedStatic = mockStatic(IdentityDatabaseUtil.class);
        secretManager = mock(SecretManagerImpl.class);
        secretResolveManager = mock(SecretResolveManagerImpl.class);
        IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

        identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSourceMap.get(DB_NAME));
        
        idVPSecretProcessorMockedConstruction = mockConstruction(IdVPSecretProcessor.class, (mock, context) -> {
            // Return the input provider as-is for encrypt/decrypt operations
            doAnswer(invocation -> invocation.getArgument(0)).when(mock).decryptAssociatedSecrets(any(IdVProvider.class));
            doAnswer(invocation -> invocation.getArgument(0)).when(mock).encryptAssociatedSecrets(any(IdVProvider.class));
        });
        secretManagerMockedConstruction = mockConstruction(SecretManagerImpl.class, (mock, context) -> {
            doReturn(new Secret()).when(mock).addSecret(anyString(), any(Secret.class));
            SecretType secretType = new SecretType();
            secretType.setId("433df096-62b7-4a36-b3eb-1bed9150ed35");
            doReturn(secretType).when(mock).getSecretType(anyString());
        });
        secretResolveManagerMockedConstruction = mockConstruction(SecretResolveManagerImpl.class, (mock, context) -> {
            ResolvedSecret resolvedSecret = new ResolvedSecret();
            resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
            doReturn(resolvedSecret).when(mock).getResolvedSecret(anyString(), anyString());
        });

        setUpSecret();
    }

    @AfterMethod
    public void tearDownMethod() {

        if (identityDatabaseUtilMockedStatic != null) {
            identityDatabaseUtilMockedStatic.close();
        }
        if (idVPSecretProcessorMockedConstruction != null) {
            idVPSecretProcessorMockedConstruction.close();
        }
        if (secretManagerMockedConstruction != null) {
            secretManagerMockedConstruction.close();
        }
        if (secretResolveManagerMockedConstruction != null) {
            secretResolveManagerMockedConstruction.close();
        }
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @Test(priority = 1)
    public void testAddIdVProvider() throws Exception {

        // Add the first IdVProvider
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            IdVProvider identityVerificationProvider1 = getTestIdVProvider(1);
            idVProviderDAO.addIdVProvider(identityVerificationProvider1, TENANT_ID);
        }

        // Add the second IdVProvider
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            IdVProvider identityVerificationProvider2 = getTestIdVProvider(2);
            idVProviderDAO.addIdVProvider(identityVerificationProvider2, TENANT_ID);
        }

        // Verify the first IdVProvider was added correctly
        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            IdVProvider idVProvider = idVProviderDAO.getIdVProvider(IDV_PROVIDER_1_UUID, TENANT_ID);
            Assert.assertEquals(idVProvider.getIdVProviderName(), IDV_PROVIDER_1_NAME);
        }
    }

    @DataProvider
    public Object[][] getIdVProviderData() {

        return new Object[][]{
                {"1c7ce08b-2ebc-4b9e-a107-3b129c019954", true},
                {"1vffg7-vghf8-zsa4e34-6678f-23y6dxffk", false},
                {"", false},
                {null, false}
        };
    }

    @Test(priority = 2, dataProvider = "getIdVProviderData")
    public void testGetIdVProvider(String idVProviderId, boolean result) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            IdVProvider idVProvider = idVProviderDAO.getIdVProvider(idVProviderId, TENANT_ID);
            Assert.assertEquals(idVProvider != null, result);
        }
    }

    @Test(priority = 3)
    public void testGetIdVProviders() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            List<IdVProvider> identityVerificationProviders =
                    idVProviderDAO.getIdVProviders(2, 0, TENANT_ID);
            Assert.assertEquals(identityVerificationProviders.size(), 2);
        }
    }

    @DataProvider
    public Object[][] getIdVProviderByNameData() {

        return new Object[][]{
                {IDV_PROVIDER_1_NAME, true},
                {"non-existing-idvp", false},
                {"", false},
                {null, false}
        };
    }

    @Test(priority = 4, dataProvider = "getIdVProviderByNameData")
    public void testGetIdVPByName(String idVProviderName, boolean result) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            IdVProvider idVProvider = idVProviderDAO.getIdVProviderByName(idVProviderName,  TENANT_ID);
            Assert.assertEquals(idVProvider != null, result);
        }
    }

    @Test(priority = 5)
    public void testGetCountOfIdVProviders() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            int countOfIdVProviders = idVProviderDAO.getCountOfIdVProviders(TENANT_ID);
            Assert.assertEquals(countOfIdVProviders, 2);
        }
    }

    @DataProvider
    public Object[][] getIdvProvidersSearchWithExpressionNodesData() {

        List<ExpressionNode> expressionNodesList1 = createExpressionNodeList("name", "co", "IdV");
        List<ExpressionNode> expressionNodesList2 = createExpressionNodeList("name", "eq", "IdVProviderName1");
        List<ExpressionNode> expressionNodesList3 = createExpressionNodeList("name", "ew", "2");

        return new Object[][]{
                {expressionNodesList1, 2, 0, 2, "IdVProviderName1"},
                {expressionNodesList1, 2, 1, 1, "IdVProviderName2"},
                {expressionNodesList1, 1, 0, 1, "IdVProviderName1"},
                {expressionNodesList1, 1, 1, 1, "IdVProviderName2"},
                {expressionNodesList2, 1, 0, 1, "IdVProviderName1"},
                {expressionNodesList3, 1, 0, 1, "IdVProviderName2"},
        };
    }

    @Test(priority = 6, dataProvider = "getIdvProvidersSearchWithExpressionNodesData")
    public void testGetIdvProvidersSearchWithExpressionNodes(List<ExpressionNode> expressionNodes, int limit,
                                                             int offset, int count, String firstIdvProvider)
            throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            List<IdVProvider> identityVerificationProviders =
                    idVProviderDAO.getIdVProviders(limit, offset, expressionNodes, TENANT_ID);
            Assert.assertEquals(identityVerificationProviders.size(), count);
            if (count > 0) {
                assertEquals(identityVerificationProviders.get(0).getIdVProviderName(), firstIdvProvider);
            }
        }
    }

    @DataProvider
    public Object[][] getIdvpsSearchWithExpressionNodesExceptionData() {

        List<ExpressionNode> expressionNodesList1 =
                createExpressionNodeList("InvalidAttribute", "eq", "IdVProviderName1");
        List<ExpressionNode> expressionNodesList2 = createExpressionNodeList("description", "InvalidOperation", "IdV");

        return new Object[][]{
                {expressionNodesList1, 2, 0},
                {expressionNodesList2, 2, 0},
        };
    }

    @Test(priority = 7, dataProvider = "getIdvpsSearchWithExpressionNodesExceptionData")
    public void testGetIdvpsSearchWithExpressionNodesException(List<ExpressionNode> expressionNodes,
                                                               int limit, int offset) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            assertThrows(IdVProviderMgtException.class,
                    () -> idVProviderDAO.getIdVProviders(limit, offset, expressionNodes, TENANT_ID));
        }
    }

    @DataProvider
    public Object[][] getCountOfFilteredIdVProvidersData() {

        return new Object[][]{
                {createExpressionNodeList("name", "co", "IdV"), 2},
                {createExpressionNodeList("name", "eq", "IdVProviderName1"), 1},
                {createExpressionNodeList("name", "ew", "2"), 1},
        };
    }

    @Test(priority = 8)
    public void testGetCountOfFilteredIdVProviders(List<ExpressionNode> expressionNodes, int totalCount)
            throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            int countOfIdVProviders = idVProviderDAO.getCountOfIdVProviders(TENANT_ID, expressionNodes);
            Assert.assertEquals(countOfIdVProviders, totalCount);
        }
    }

    @DataProvider
    public Object[][] getCountOfFilteredIdVProvidersExceptionData() {

        List<ExpressionNode> expressionNodesList1 =
                createExpressionNodeList("InvalidAttribute", "eq", "IdVProviderName1");
        List<ExpressionNode> expressionNodesList2 = createExpressionNodeList("description", "InvalidOperation", "IdV");

        return new Object[][]{
                {expressionNodesList1},
                {expressionNodesList2},
        };
    }

    @Test(priority = 9, dataProvider = "getCountOfFilteredIdVProvidersExceptionData")
    public void testGetCountOfFilteredIdVProvidersException(List<ExpressionNode> expressionNodes) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            assertThrows(IdVProviderMgtException.class,
                    () -> idVProviderDAO.getCountOfIdVProviders(TENANT_ID, expressionNodes));
        }
    }

    @Test(priority = 10)
    public void testIsIdVProviderExists() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            boolean isIdVProviderExists = idVProviderDAO.isIdVProviderExists(IDV_PROVIDER_1_UUID, TENANT_ID);
            Assert.assertTrue(isIdVProviderExists);
        }
    }

    @Test(priority = 11)
    public void testUpdateIdVProviderExists() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            IdVProvider idVProvider = getOldIdVProvider();
            IdVProvider updatedIdVProvider = getTestIdVProvider(1);
            idVProviderDAO.updateIdVProvider(idVProvider, updatedIdVProvider, TENANT_ID);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            IdVProvider idVProvider = idVProviderDAO.getIdVProvider(IDV_PROVIDER_1_UUID, TENANT_ID);
            Assert.assertTrue(idVProvider.isEnabled());
        }
    }

    @Test(priority = 12)
    public void testIsIdVProviderExistsByName() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            boolean isIdVProviderExists = idVProviderDAO.isIdVProviderExistsByName(IDV_PROVIDER_1_NAME, TENANT_ID);
            Assert.assertTrue(isIdVProviderExists);
        }
    }

    @Test(priority = 13)
    public void testDeleteIdVProvider() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            idVProviderDAO.deleteIdVProvider(IDV_PROVIDER_1_UUID, TENANT_ID);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                    .thenReturn(connection);
            IdVProvider idVProvider = idVProviderDAO.getIdVProvider(IDV_PROVIDER_1_UUID, TENANT_ID);
            Assert.assertNull(idVProvider);
        }
    }

    @Test
    public void testGetPriority() {

        int priority = idVProviderDAO.getPriority();
        Assert.assertEquals(priority, 1);
    }

    private void setUpSecret() throws SecretManagementException {

        Secret secret = new Secret();
        doReturn(secret).when(secretManager).addSecret(anyString(), any(Secret.class));
        SecretType secretType = new SecretType();
        secretType.setId("433df096-62b7-4a36-b3eb-1bed9150ed35");
        doReturn(secretType).when(secretManager).getSecretType(anyString());
    }

    private void setUpResolvedSecret() throws SecretManagementException {

        ResolvedSecret resolvedSecret = new ResolvedSecret();
        resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
        doReturn(resolvedSecret).when(secretResolveManager).getResolvedSecret(anyString(), anyString());
    }
}
