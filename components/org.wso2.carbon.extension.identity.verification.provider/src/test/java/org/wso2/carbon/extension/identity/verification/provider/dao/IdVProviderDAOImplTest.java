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

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.provider.IdVPSecretProcessor;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
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
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.dataSourceMap;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getOldIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getTestIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.IDV_PROVIDER_ID;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.IDV_PROVIDER_NAME;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.TENANT_ID;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.closeH2Database;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getConnection;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getFilePath;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.initiateH2Database;

/**
 * Test class for IdVProviderDAOImpl.
 */
@PrepareForTest({IdentityDatabaseUtil.class, IdVPSecretProcessor.class})
public class IdVProviderDAOImplTest {

    private IdVProviderDAO idVProviderDAO;
    private SecretManagerImpl secretManager;
    private SecretResolveManagerImpl secretResolveManager;
    private static final String DB_NAME = "test";

    @BeforeClass
    public void init() throws Exception {

        initiateH2Database(getFilePath());
    }

    @BeforeMethod
    public void setUp() throws Exception {

        idVProviderDAO = new IdVProviderDAOImpl();
        IdVProviderDataHolder.getInstance().setIdVProviderDAOs(Collections.singletonList(idVProviderDAO));
        mockStatic(IdentityDatabaseUtil.class);
        secretManager = mock(SecretManagerImpl.class);
        secretResolveManager = mock(SecretResolveManagerImpl.class);
        IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);

        when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSourceMap.get(DB_NAME));
        whenNew(IdVPSecretProcessor.class).withNoArguments().thenReturn(idVPSecretProcessor);
        whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
        whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);

        setUpSecret();
    }

    @AfterClass
    public void tearDown() throws Exception {

        closeH2Database();
    }

    @Test(priority = 1)
    public void testAddIdVProvider() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            IdVProvider identityVerificationProvider = getTestIdVProvider();
            doReturn(false).when(secretManager).isSecretExist(anyString(), anyString());
            idVProviderDAO.addIdVProvider(identityVerificationProvider, TENANT_ID);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            setUpResolvedSecret();
            IdVProvider idVProvider = idVProviderDAO.getIdVProvider(IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertEquals(idVProvider.getIdVProviderName(), IDV_PROVIDER_NAME);
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
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            setUpResolvedSecret();
            IdVProvider idVProvider = idVProviderDAO.getIdVProvider(idVProviderId, TENANT_ID);
            Assert.assertEquals(idVProvider != null, result);
        }
    }

    @Test(priority = 3)
    public void testGetIdVProviders() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            setUpResolvedSecret();
            List<IdVProvider> identityVerificationProviders =
                    idVProviderDAO.getIdVProviders(2, 0, TENANT_ID);
            Assert.assertEquals(identityVerificationProviders.size(), 1);
        }
    }

    @DataProvider
    public Object[][] getIdVProviderByNameData() {

        return new Object[][]{
                {IDV_PROVIDER_NAME, true},
                {"non-existing-idvp", false},
                {"", false},
                {null, false}
        };
    }

    @Test(priority = 4, dataProvider = "getIdVProviderByNameData")
    public void testGetIdVPByName(String idVProviderName, boolean result) throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            setUpResolvedSecret();
            IdVProvider idVProvider = idVProviderDAO.getIdVProviderByName(idVProviderName,  TENANT_ID);
            Assert.assertEquals(idVProvider != null, result);
        }
    }

    @Test(priority = 5)
    public void testGetCountOfIdVProviders() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            setUpResolvedSecret();
            int countOfIdVProviders = idVProviderDAO.getCountOfIdVProviders(TENANT_ID);
            Assert.assertEquals(countOfIdVProviders, 1);
        }
    }

    @Test(priority = 6)
    public void testIsIdVProviderExists() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            setUpResolvedSecret();
            boolean isIdVProviderExists = idVProviderDAO.isIdVProviderExists(IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertTrue(isIdVProviderExists);
        }
    }

    @Test(priority = 7)
    public void testUpdateIdVProviderExists() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            IdVProvider idVProvider = getOldIdVProvider();
            IdVProvider updatedIdVProvider = getTestIdVProvider();
            doReturn(false).when(secretManager).isSecretExist(anyString(), anyString());
            idVProviderDAO.updateIdVProvider(idVProvider, updatedIdVProvider, TENANT_ID);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            setUpResolvedSecret();
            IdVProvider idVProvider = idVProviderDAO.getIdVProvider(IDV_PROVIDER_ID, TENANT_ID);
            Assert.assertTrue(idVProvider.isEnabled());
        }
    }

    @Test(priority = 8)
    public void testIsIdVProviderExistsByName() throws Exception {

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            setUpResolvedSecret();
            boolean isIdVProviderExists = idVProviderDAO.isIdVProviderExistsByName(IDV_PROVIDER_NAME, TENANT_ID);
            Assert.assertTrue(isIdVProviderExists);
        }
    }

    @Test(priority = 9)
    public void testDeleteIdVProvider() throws Exception {

        setUpResolvedSecret();
        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            idVProviderDAO.deleteIdVProvider(IDV_PROVIDER_ID, TENANT_ID);
        }

        try (Connection connection = getConnection(DB_NAME)) {
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection);
            doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
            IdVProvider idVProvider = idVProviderDAO.getIdVProvider(IDV_PROVIDER_ID, TENANT_ID);
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
