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

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getOldIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.getTestIdVProvider;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.IDV_PROVIDER_1_UUID;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.IDV_PROVIDER_1_NAME;
import static org.wso2.carbon.extension.identity.verification.provider.util.TestUtils.TENANT_ID;

/**
 * Test class for IdVProviderManagerImpl.
 */
public class IdVProviderManagerImplTest {

    private IdVProviderManager idVProviderManager;
    private IdVProviderDAO idVProviderDAO;
    private MockedStatic<IdVProviderDataHolder> idVProviderDataHolderMockedStatic;

    @BeforeMethod
    public void setUp() {

        idVProviderDAO = mock(IdVProviderDAO.class);
        IdVProviderDataHolder idVProviderDataHolder = mock(IdVProviderDataHolder.class);
        idVProviderDataHolderMockedStatic = mockStatic(IdVProviderDataHolder.class);
        idVProviderDataHolderMockedStatic.when(IdVProviderDataHolder::getInstance).thenReturn(idVProviderDataHolder);
        when(idVProviderDataHolder.getIdVProviderDAOs()).thenReturn(Collections.singletonList(idVProviderDAO));
        idVProviderManager = new IdVProviderManagerImpl();
    }

    @AfterMethod
    public void tearDown() {

        if (idVProviderDataHolderMockedStatic != null) {
            idVProviderDataHolderMockedStatic.close();
        }
    }

    @Test
    public void testGetIdVProvider() throws Exception {

        IdVProvider idvProvider = getTestIdVProvider(1);
        when(idVProviderDAO.getIdVProvider(anyString(), anyInt())).thenReturn(idvProvider);
        IdVProvider identityVerificationProvider =
                idVProviderManager.getIdVProvider(IDV_PROVIDER_1_UUID, TENANT_ID);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderUuid(), IDV_PROVIDER_1_UUID);
    }

    @Test(expectedExceptions = IdVProviderMgtClientException.class)
    public void testGetIdVProviderEmptyIdVProviderID() throws IdVProviderMgtException {

        idVProviderManager.getIdVProvider(null, TENANT_ID);
    }

    @Test
    public void testAddIdVProvider() throws Exception {

        IdVProvider idvProvider = getTestIdVProvider(1);
        doNothing().when(idVProviderDAO).addIdVProvider(any(IdVProvider.class), anyInt());
        IdVProvider identityVerificationProvider = idVProviderManager.addIdVProvider(idvProvider, TENANT_ID);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), idvProvider.getIdVProviderName());
    }

    @Test
    public void testIsIdVProviderExistsByName() throws IdVProviderMgtException {

        doReturn(true).when(idVProviderDAO).isIdVProviderExistsByName(anyString(), anyInt());
        boolean isIdVProviderExistsByName =
                idVProviderManager.isIdVProviderExistsByName(IDV_PROVIDER_1_NAME, TENANT_ID);
        Assert.assertTrue(isIdVProviderExistsByName);
    }

    @Test
    public void testGetCountOfIdVProviders() throws IdVProviderMgtException {

        doReturn(5).when(idVProviderDAO).getCountOfIdVProviders(anyInt());
        int countOfIdVProviders = idVProviderManager.getCountOfIdVProviders(TENANT_ID);
        Assert.assertEquals(countOfIdVProviders, 5);
    }

    @DataProvider
    public Object[][] getCountOfFilteredIdVProvidersData() {

        return new Object[][]{
                {"name sw IdV", 2},
                {null, 4}};
    }

    @Test(dataProvider = "getCountOfFilteredIdVProvidersData")
    public void testGetCountOfFilteredIdVProviders(String filter, int totalCount) throws IdVProviderMgtException {

        doReturn(totalCount).when(idVProviderDAO).getCountOfIdVProviders(anyInt(), any());
        int countOfIdVProviders = idVProviderManager.getCountOfIdVProviders(TENANT_ID, filter);
        Assert.assertEquals(countOfIdVProviders, totalCount);
    }

    @Test
    public void testGetCountOfFilteredIdVProvidersException() {

        String filter = "Wrong_Filter";
        assertThrows(IdVProviderMgtClientException.class,
                () -> idVProviderManager.getCountOfIdVProviders(TENANT_ID, filter));
    }

    @Test
    public void testUpdateIdVProvider() throws IdVProviderMgtException {

        IdVProvider oldIdVProvider = getOldIdVProvider();
        IdVProvider updatedIdvProvider = getTestIdVProvider(1);
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
        idvProviders.add(getTestIdVProvider(1));
        doReturn(idvProviders).when(idVProviderDAO).getIdVProviders(anyInt(), anyInt(), anyInt());
        List<IdVProvider> idvProviderList = idVProviderManager.getIdVProviders(limit, offset, 1);
        Assert.assertEquals(idvProviderList.size(), expected);
    }

    @DataProvider
    public Object[][] getFilteredIdVProvidersData() {

        String filter1 = "name sw IdV";
        String filter2 = "type eq IdVProviderType1";
        String filter3 = "isEnabled co true";
        String filter4 = "isEnabled eq false";
        String filter5 = "name ew 2";

        List<IdVProvider> idvProvidersList1 = new ArrayList<>();
        idvProvidersList1.add(getTestIdVProvider(1));
        idvProvidersList1.add(getTestIdVProvider(2));

        List<IdVProvider> idvProvidersList2 = new ArrayList<>();
        idvProvidersList2.add(getTestIdVProvider(1));

        List<IdVProvider> idvProvidersList3 = new ArrayList<>();
        idvProvidersList3.add(getTestIdVProvider(2));

        return new Object[][]{
                {2, 0, filter1, 2, idvProvidersList1, "IdVProviderName1"},
                {2, 1, filter1, 1, idvProvidersList3, "IdVProviderName2"},
                {2, 0, filter2, 1, idvProvidersList2, "IdVProviderName1"},
                {2, 1, filter3, 1, idvProvidersList3, "IdVProviderName2"},
                {2, 0, filter4, 0, new ArrayList<>(), ""},
                {2, 0, filter5, 1, idvProvidersList3, "IdVProviderName2"}
        };
    }

    @Test(dataProvider = "getFilteredIdVProvidersData")
    public void testGetFilteredIdVProviders(Integer limit, Integer offset, String filter, int count,
                                            List<IdVProvider> idvProviders, String firstIdvProvider)
            throws IdVProviderMgtException {

        doReturn(idvProviders).when(idVProviderDAO).getIdVProviders(anyInt(), anyInt(), any(), anyInt());
        List<IdVProvider> idvProviderList = idVProviderManager.getIdVProviders(limit, offset, filter, TENANT_ID);
        Assert.assertEquals(idvProviderList.size(), count);
        if (count > 0) {
            assertEquals(idvProviderList.get(0).getIdVProviderName(), firstIdvProvider);
        }
    }

    @Test
    public void testGetFilteredIdVProvidersException() {

        String filter = "Wrong_Filter";
        assertThrows(IdVProviderMgtClientException.class,
                () -> idVProviderManager.getIdVProviders(10, 0, filter, TENANT_ID));
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
        idvProviders.add(getTestIdVProvider(1));
        doReturn(idvProviders).when(idVProviderDAO).getIdVProviders(anyInt(), anyInt(), anyInt());
        idVProviderManager.getIdVProviders(limit, offset, 1);
    }

    @Test
    public void testIsIdVProviderExists() throws IdVProviderMgtException {

        doReturn(true).when(idVProviderDAO).isIdVProviderExists(anyString(), anyInt());
        boolean idVProviderExists = idVProviderManager.isIdVProviderExists(IDV_PROVIDER_1_UUID, TENANT_ID);
        Assert.assertTrue(idVProviderExists);
    }

    @Test
    public void testGetIdVPByName() throws IdVProviderMgtException {

        IdVProvider idvProvider = getTestIdVProvider(1);
        doReturn(idvProvider).when(idVProviderDAO).getIdVProviderByName(anyString(), anyInt());
        IdVProvider identityVerificationProvider =
                idVProviderManager.getIdVProviderByName(IDV_PROVIDER_1_NAME, 1);
        Assert.assertNotNull(identityVerificationProvider);
        Assert.assertEquals(identityVerificationProvider.getIdVProviderName(), IDV_PROVIDER_1_NAME);
    }
}
