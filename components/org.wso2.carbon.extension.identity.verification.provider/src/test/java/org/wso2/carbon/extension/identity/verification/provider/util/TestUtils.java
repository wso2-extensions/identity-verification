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

package org.wso2.carbon.extension.identity.verification.provider.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.core.model.ExpressionNode;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for Identity Verification Provider Tests.
 */
public class TestUtils {

    public static final int TENANT_ID = -1234;
    public static final String IDV_PROVIDER_1_UUID = "1c7ce08b-2ebc-4b9e-a107-3b129c019954";
    public static final String IDV_PROVIDER_1_NAME = "IdVProviderName1";
    public static final String IDV_PROVIDER_1_TYPE = "IdVProviderType1";
    public static final String IDV_PROVIDER_2_UUID = "4567e08b-2ebc-1234-a107-3b129c019954";
    public static final String IDV_PROVIDER_2_NAME = "IdVProviderName2";
    public static final String IDV_PROVIDER_2_TYPE = "IdVProviderType2";
    public static final String IDV_PROVIDER_IMAGE_URL = "idv-provider-logo-url";
    public static final Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    public static final String DB_NAME = "test";

    public static IdVProvider getTestIdVProvider(int id) {

        IdVProvider idVProvider = new IdVProvider();

        if(id == 1) {
            idVProvider.setIdVProviderUUID(IDV_PROVIDER_1_UUID);
            idVProvider.setType(IDV_PROVIDER_1_TYPE);
            idVProvider.setIdVProviderName(IDV_PROVIDER_1_NAME);
        } else {
            idVProvider.setIdVProviderUUID(IDV_PROVIDER_2_UUID);
            idVProvider.setType(IDV_PROVIDER_2_TYPE);
            idVProvider.setIdVProviderName(IDV_PROVIDER_2_NAME);
        }
        idVProvider.setIdVProviderDescription("ONFIDO identity verification provider");
        idVProvider.setImageUrl(IDV_PROVIDER_IMAGE_URL);
        idVProvider.setEnabled(true);

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

    public static IdVProvider getOldIdVProvider() {

        IdVProvider idVProvider = new IdVProvider();
        idVProvider.setId("1");
        idVProvider.setIdVProviderUUID(IDV_PROVIDER_1_UUID);
        idVProvider.setType(IDV_PROVIDER_1_TYPE);
        idVProvider.setIdVProviderName(IDV_PROVIDER_1_NAME);
        idVProvider.setIdVProviderDescription("ONFIDO updated description");
        idVProvider.setEnabled(false);

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

    public static List<ExpressionNode> createExpressionNodeList(String attributeValue, String operation, String value) {

        ExpressionNode expressionNode = new ExpressionNode();
        expressionNode.setAttributeValue(attributeValue);
        expressionNode.setOperation(operation);
        expressionNode.setValue(value);
        return Collections.singletonList(expressionNode);
    }

    public static void closeH2Database() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static Connection getConnection(String database) throws SQLException {

        if (dataSourceMap.get(database) != null) {
            return dataSourceMap.get(database).getConnection();
        }
        throw new RuntimeException("No datasource initiated for database: " + database);
    }

    public static void initiateH2Database(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:test" + DB_NAME);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(DB_NAME, dataSource);
    }

    public static String getFilePath() {

        if (StringUtils.isNotBlank("h2.sql")) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", "h2.sql")
                    .toString();
        }
        throw new IllegalArgumentException("DB Script file name cannot be empty.");
    }
}
