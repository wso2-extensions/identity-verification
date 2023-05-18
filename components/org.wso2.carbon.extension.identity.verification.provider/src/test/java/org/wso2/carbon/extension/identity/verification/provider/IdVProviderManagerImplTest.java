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
package org.wso2.carbon.extension.identity.verification.provider;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getOldIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getTestIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.IDV_PROVIDER_ID;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.IDV_PROVIDER_NAME;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.TENANT_ID;

/**
 * Test class for IdVProviderManagerImpl.
 */
@PrepareForTest({IdVProviderDataHolder.class, IdVProvider.class})
public class IdVProviderManagerImplTest extends PowerMockTestCase {

    private IdVProviderManager idVProviderManager;
    private IdVProviderDAO idVProviderDAO;

    @BeforeMethod
    public void setUp() {

        idVProviderDAO = mock(IdVProviderDAO.class);
        IdVProviderDataHolder idVProviderDataHolder = mock(IdVProviderDataHolder.class);
        mockStatic(IdVProviderDataHolder.class);
        when(IdVProviderDataHolder.getInstance()).thenReturn(idVProviderDataHolder);
        when(idVProviderDataHolder.getIdVProviderDAOs()).thenReturn(Collections.singletonList(idVProviderDAO));
        idVProviderManager = new IdVProviderManagerImpl();
    }

    @Test
    public void testGetIdVProvider() throws Exception {

        IdVProvider idvProvider = getTestIdVProvider();
        when(idVProviderDAO.getIdVProvider(anyString(), anyInt())).thenReturn(idvProvider);
        IdVProvider identityVerificationProvider =
                idVProviderManager.getIdVProvider(IDV_PROVIDER_ID, TENANT_ID);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderUuid(), IDV_PROVIDER_ID);
    }

    @Test(expectedExceptions = IdVProviderMgtClientException.class)
    public void testGetIdVProviderEmptyIdVProviderID() {

        try {
            idVProviderManager.getIdVProvider(null, TENANT_ID);
        } catch (IdVProviderMgtException e) {
            Assert.assertEquals(e.getErrorCode(), "IdVProvider ID cannot be empty.");
        }
    }

    @Test
    public void testAddIdVProvider() throws Exception {

        IdVProvider idvProvider = getTestIdVProvider();
        doNothing().when(idVProviderDAO).addIdVProvider(any(IdVProvider.class), anyInt());
        IdVProvider identityVerificationProvider = idVProviderManager.addIdVProvider(idvProvider, -1234);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), idvProvider.getIdVProviderName());
    }

    @Test
    public void testIsIdVProviderExistsByName() throws IdVProviderMgtException {

        doReturn(true).when(idVProviderDAO).isIdVProviderExistsByName(anyString(), anyInt());
        boolean isIdVProviderExistsByName =
                idVProviderManager.isIdVProviderExistsByName(IDV_PROVIDER_NAME, TENANT_ID);
        Assert.assertTrue(isIdVProviderExistsByName);
    }

    @Test
    public void testGetCountOfIdVProviders() throws IdVProviderMgtException {

        doReturn(5).when(idVProviderDAO).getCountOfIdVProviders(anyInt());
        int countOfIdVProviders = idVProviderManager.getCountOfIdVProviders(TENANT_ID);
        Assert.assertEquals(countOfIdVProviders, 5);
    }

    @Test
    public void testUpdateIdVProvider() throws IdVProviderMgtException {

        IdVProvider oldIdVProvider = getOldIdVProvider();
        IdVProvider updatedIdvProvider = getTestIdVProvider();
        doNothing().when(idVProviderDAO).updateIdVProvider(any(IdVProvider.class), any(IdVProvider.class), anyInt());
        IdVProvider idvProviderList =
                idVProviderManager.updateIdVProvider(oldIdVProvider, updatedIdvProvider, 1);
        Assert.assertTrue(idvProviderList.isEnabled());
    }

    @DataProvider(name = "getIdVProvidersData")
    public Object[][] createValidParameters() {

        return new Object[][]{
                {2, 0, 1},
                {null, 0, 1},
                {105, 0, 1},
                {2, null, 1}};
    }

    @Test(dataProvider = "getIdVProvidersData")
    public void testGetIdVProviders(Integer limit, Integer offset, int expected)
            throws IdVProviderMgtException {

        List<IdVProvider> idvProviders = new ArrayList<>();
        idvProviders.add(getTestIdVProvider());
        doReturn(idvProviders).when(idVProviderDAO).getIdVProviders(anyInt(), anyInt(), anyInt());
        List<IdVProvider> idvProviderList = idVProviderManager.getIdVProviders(limit, offset, 1);
        Assert.assertEquals(idvProviderList.size(), expected);
    }

    @DataProvider(name = "getIdVProvidersDataWithInvalidInputs")
    public Object[][] createInvalidValidParameters() {

        return new Object[][]{
                {-1, 0},
                {2, -1}};
    }

    @Test(dataProvider = "getIdVProvidersDataWithInvalidInputs",
            expectedExceptions = IdVProviderMgtClientException.class)
    public void testGetIdVProvidersInvalidInputs(Integer limit, Integer offset) throws IdVProviderMgtException {

        List<IdVProvider> idvProviders = new ArrayList<>();
        idvProviders.add(getTestIdVProvider());
        doReturn(idvProviders).when(idVProviderDAO).getIdVProviders(anyInt(), anyInt(), anyInt());
        idVProviderManager.getIdVProviders(limit, offset, 1);
    }

    @Test
    public void testIsIdVProviderExists() throws IdVProviderMgtException {

        doReturn(true).when(idVProviderDAO).isIdVProviderExists(anyString(), anyInt());
        boolean idVProviderExists = idVProviderManager.isIdVProviderExists(IDV_PROVIDER_ID, TENANT_ID);
        Assert.assertTrue(idVProviderExists);
    }

    @Test
    public void testGetIdVPByName() throws IdVProviderMgtException {

        IdVProvider idvProvider = getTestIdVProvider();
        doReturn(idvProvider).when(idVProviderDAO).getIdVProviderByName(anyString(), anyInt());
        IdVProvider identityVerificationProvider =
                idVProviderManager.getIdVProviderByName(IDV_PROVIDER_NAME, 1);
        Assert.assertNotNull(identityVerificationProvider);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), IDV_PROVIDER_NAME);
    }
}
