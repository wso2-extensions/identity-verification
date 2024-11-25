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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.extension.identity.verification.provider.IdVPSecretProcessor;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdvProviderMgtServerException;
import org.wso2.carbon.extension.identity.verification.provider.model.FilterQueryBuilder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtExceptionManagement;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.CLAIM;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.CO;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.DESCRIPTION;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.EMPTY_STRING;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.EQ;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.EW;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_ADDING_IDV_PROVIDER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_ADDING_IDV_PROVIDER_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_ADDING_IDV_PROVIDER_CONFIGS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_DELETING_IDV_PROVIDER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_DELETING_IDV_PROVIDER_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_DELETING_IDV_PROVIDER_CONFIGS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_GETTING_IDV_PROVIDER_COUNT;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_FILTERED_IDV_PROVIDERS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDERS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER_CLAIMS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER_CONFIGS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER_SECRETS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_STORING_IDV_PROVIDER_SECRETS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_UPDATING_IDV_PROVIDER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ID;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IDVP_FILTER_DESCRIPTION;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IDVP_FILTER_IS_ENABLED;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IDVP_FILTER_NAME;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IDVP_FILTER_TYPE;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IDVP_FILTER_UUID;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IDVP_TYPE;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IS_ENABLED;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IS_SECRET;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.LOCAL_CLAIM;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.NAME;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.PROPERTY_KEY;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.PROPERTY_VALUE;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.ADD_IDVP_CLAIM_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.ADD_IDVP_CONFIG_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.ADD_IDVP_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.DELETE_IDVP_CLAIM_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.DELETE_IDVP_CONFIG_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.DELETE_IDV_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_COUNT_OF_IDVPS_SQL_WITH_FILTER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVPS_SQL_BY_MSSQL_WITH_FILTER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVPS_SQL_BY_MYSQL_WITH_FILTER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVPS_SQL_BY_POSTGRESQL_WITH_FILTER;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVP_BY_NAME_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVP_CLAIMS_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVP_CONFIG_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.GET_IDVP_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.IS_IDVP_EXIST_BY_NAME_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.IS_IDVP_EXIST_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SQLQueries.UPDATE_IDVP_SQL;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IDVP_UUID;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SW;

/**
 * Data Access Layer functionality for Identity Verification Provider management.
 */
public class IdVProviderDAOImpl implements IdVProviderDAO {

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public IdVProvider getIdVProvider(String idVPUuid, int tenantId)
            throws IdVProviderMgtException {

        IdVProvider idVProvider;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            idVProvider = getIDVProviderByUUID(idVPUuid, tenantId, connection);
            if (idVProvider == null) {
                return null;
            }
            // Get configs of identity verification provider.
            idVProvider = getIdVProviderWithConfigs(idVProvider, tenantId, connection);

            // Get claim mappings of identity verification provider.
            getIdVProvidersWithClaims(idVProvider, tenantId, connection);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDERS, e);
        }
        return idVProvider;
    }

    @Override
    public boolean isIdVProviderExists(String idVPUuid, int tenantId) throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = connection.prepareStatement(IS_IDVP_EXIST_SQL)) {
            getIdVProvidersStmt.setString(1, idVPUuid);
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                return idVProviderResultSet.next();
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER, idVPUuid, e);
        }
    }

    @Override
    public boolean isIdVProviderExistsByName(String idvProviderName, int tenantId) throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = connection.prepareStatement(IS_IDVP_EXIST_BY_NAME_SQL)) {
            getIdVProvidersStmt.setString(1, idvProviderName);
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                return idVProviderResultSet.next();
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.
                    handleServerException(ERROR_RETRIEVING_IDV_PROVIDER, idvProviderName, e);
        }
    }

    @Override
    public void addIdVProvider(IdVProvider idVProvider, int tenantId) throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement addIdVProviderStmt = connection.prepareStatement(ADD_IDVP_SQL)) {
            String idVProviderUuid = idVProvider.getIdVProviderUuid();
            addIdVProviderStmt.setString(1, idVProviderUuid);
            addIdVProviderStmt.setInt(2, tenantId);
            addIdVProviderStmt.setString(3, idVProvider.getIdVProviderName());
            addIdVProviderStmt.setString(4, idVProvider.getType());
            addIdVProviderStmt.setString(5, idVProvider.getIdVProviderDescription());
            addIdVProviderStmt.setString(6, idVProvider.isEnabled() ? "1" : "0");
            addIdVProviderStmt.executeUpdate();

            // Get the just added identity verification provider along with the id.
            IdVProvider createdIdVP = getIDVProviderByUUID(idVProviderUuid, tenantId, connection);
            idVProvider.setId(createdIdVP.getId());

            // Add configs of identity verification provider.
            addIDVProviderConfigs(idVProvider, tenantId, connection);

            // Add claims of identity verification provider.
            addIDVProviderClaims(idVProvider, tenantId, connection);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_ADDING_IDV_PROVIDER, e);
        }
    }

    @Override
    public void updateIdVProvider(IdVProvider oldIdVProvider, IdVProvider newIdVProvider,
                                  int tenantId) throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement updateIdVProviderStmt = connection.prepareStatement(UPDATE_IDVP_SQL)) {
            updateIdVProviderStmt.setString(1, newIdVProvider.getIdVProviderName());
            updateIdVProviderStmt.setString(2, newIdVProvider.getType());
            updateIdVProviderStmt.setString(3, newIdVProvider.getIdVProviderDescription());
            updateIdVProviderStmt.setString(4, newIdVProvider.isEnabled() ? "1" : "0");
            updateIdVProviderStmt.setString(5, oldIdVProvider.getIdVProviderUuid());
            updateIdVProviderStmt.setInt(6, tenantId);
            updateIdVProviderStmt.executeUpdate();

            // Update configs of identity verification provider.
            newIdVProvider.setId(oldIdVProvider.getId());
            updateIDVProviderConfigs(newIdVProvider, tenantId, connection);

            // Update claims of identity verification provider.
            updateIDVProviderClaims(newIdVProvider, tenantId, connection);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_UPDATING_IDV_PROVIDER, e);
        }
    }

    @Override
    public List<IdVProvider> getIdVProviders(Integer limit, Integer offset, int tenantId)
            throws IdVProviderMgtException {

        return getIdVProviders(limit, offset, null, tenantId);
    }

    @Override
    public List<IdVProvider> getIdVProviders(Integer limit, Integer offset, List<ExpressionNode> expressionNode,
                                             int tenantId) throws IdVProviderMgtException {

        List<IdVProvider> idVProviders = new ArrayList<>();
        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        this.appendFilterQuery(expressionNode, filterQueryBuilder);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = generateGetIdvProvidersPrepStmt(connection, filterQueryBuilder)) {
            bindGetIdvProvidersPrepStmt(getIdVProvidersStmt, tenantId, offset, limit, filterQueryBuilder,
                    connection.getMetaData().getDatabaseProductName());

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    IdVProvider idVProvider = new IdVProvider();
                    idVProvider.setId(idVProviderResultSet.getString(ID));
                    idVProvider.setIdVProviderUUID(idVProviderResultSet.getString(IDVP_UUID));
                    idVProvider.setIdVProviderName(idVProviderResultSet.getString(NAME));
                    idVProvider.setType(idVProviderResultSet.getString(IDVP_TYPE));
                    idVProvider.
                            setIdVProviderDescription(idVProviderResultSet.getString(DESCRIPTION));
                    idVProvider.setEnabled(idVProviderResultSet.getBoolean(IS_ENABLED));

                    // Get configs of identity verification provider.
                    idVProvider =
                            getIdVProviderWithConfigs(idVProvider, tenantId, connection);

                    // Get claim mappings of identity verification provider.
                    getIdVProvidersWithClaims(idVProvider, tenantId, connection);

                    idVProviders.add(idVProvider);
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDERS, e);
        }
        return idVProviders;
    }

    @Override
    public int getCountOfIdVProviders(int tenantId) throws IdVProviderMgtException {

        return getCountOfIdVProviders(tenantId, null);
    }

    @Override
    public int getCountOfIdVProviders(int tenantId, List<ExpressionNode> expressionNode)
            throws IdVProviderMgtException {

        int count = 0;
        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNode, filterQueryBuilder);
        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();
        String getIdVProvidersCountSqlQuery =
                String.format(GET_COUNT_OF_IDVPS_SQL_WITH_FILTER, filterQueryBuilder.getFilterQuery());

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = connection.prepareStatement(getIdVProvidersCountSqlQuery)) {

            for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
                getIdVProvidersStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
            }
            getIdVProvidersStmt.setInt(filterAttributeValue.entrySet().size() + 1, tenantId);
            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    count = idVProviderResultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_GETTING_IDV_PROVIDER_COUNT,
                    IdentityTenantUtil.getTenantDomain(tenantId), e);
        }
        return count;
    }

    @Override
    public IdVProvider getIdVProviderByName(String idVPName, int tenantId) throws IdVProviderMgtException {

        IdVProvider idVProvider = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVP_BY_NAME_SQL)) {
            getIdVProvidersStmt.setString(1, idVPName);
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    idVProvider = new IdVProvider();
                    idVProvider.setId(idVProviderResultSet.getString(ID));
                    idVProvider.setIdVProviderUUID(idVProviderResultSet.getString(IDVP_UUID));
                    idVProvider.setIdVProviderName(idVProviderResultSet.getString(NAME));
                    idVProvider.setType(idVProviderResultSet.getString(IDVP_TYPE));
                    idVProvider.setIdVProviderDescription(idVProviderResultSet.
                            getString(DESCRIPTION));
                    idVProvider.setEnabled(idVProviderResultSet.getBoolean(IS_ENABLED));
                }
            }

            if (idVProvider == null) {
                return null;
            }
            // Get configs of identity verification provider.
            idVProvider = getIdVProviderWithConfigs(idVProvider, tenantId, connection);

            // Get claim mappings of identity verification provider.
            getIdVProvidersWithClaims(idVProvider, tenantId, connection);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDERS, e);
        }
        return idVProvider;
    }

    /**
     * Delete Identity Verification Provider by ID.
     *
     * @param idVProviderId Identity Verification Provider ID.
     * @param tenantId      Tenant ID.
     * @throws IdVProviderMgtException Error when getting Identity Verification Provider.
     */
    public void deleteIdVProvider(String idVProviderId, int tenantId) throws IdVProviderMgtException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            IdVProvider idVProvider = getIDVProviderByUUID(idVProviderId, tenantId, connection);
            if (idVProvider == null) {
                return;
            }
            try (PreparedStatement deleteIdVProviderStmt = connection.prepareStatement(DELETE_IDV_SQL)) {
                deleteIdVProviderStmt.setString(1, idVProviderId);
                deleteIdVProviderStmt.setInt(2, tenantId);
                deleteIdVProviderStmt.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw IdVProviderMgtExceptionManagement.
                        handleServerException(ERROR_DELETING_IDV_PROVIDER, idVProviderId, e1);
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_DELETING_IDV_PROVIDER, e);
        }
    }

    private IdVProvider getIDVProviderByUUID(String idVPUuid, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        IdVProvider idVProvider = null;
        try (PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVP_SQL)) {
            getIdVProvidersStmt.setString(1, idVPUuid);
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    idVProvider = new IdVProvider();
                    idVProvider.setId(idVProviderResultSet.getString(ID));
                    idVProvider.setIdVProviderUUID(idVProviderResultSet.getString(IDVP_UUID));
                    idVProvider.setType(idVProviderResultSet.getString(IDVP_TYPE));
                    idVProvider.setIdVProviderName(idVProviderResultSet.getString(NAME));
                    idVProvider.setIdVProviderDescription(idVProviderResultSet.getString(DESCRIPTION));
                    idVProvider.setEnabled(idVProviderResultSet.getBoolean(IS_ENABLED));
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER, idVPUuid, e);
        }
        return idVProvider;
    }

    private void addIDVProviderConfigs(IdVProvider idVProvider, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        if (idVProvider.getIdVConfigProperties() == null) {
            idVProvider.setIdVConfigProperties(new IdVConfigProperty[0]);
        }

        try (PreparedStatement addIDVProviderConfigsStmt = connection.prepareStatement(ADD_IDVP_CONFIG_SQL)) {
            IdVPSecretProcessor idVPSecretProcessor = new IdVPSecretProcessor();
            idVProvider = idVPSecretProcessor.encryptAssociatedSecrets(idVProvider);
            for (IdVConfigProperty idVConfigProperty : idVProvider.getIdVConfigProperties()) {
                addIDVProviderConfigsStmt.setInt(1, Integer.parseInt(idVProvider.getId()));
                addIDVProviderConfigsStmt.setInt(2, tenantId);
                addIDVProviderConfigsStmt.setString(3, idVConfigProperty.getName());
                addIDVProviderConfigsStmt.setString(4, idVConfigProperty.getValue());
                addIDVProviderConfigsStmt.setString(5, idVConfigProperty.isConfidential() ? "1" : "0");
                addIDVProviderConfigsStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_ADDING_IDV_PROVIDER_CONFIGS, e);
        } catch (SecretManagementException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_STORING_IDV_PROVIDER_SECRETS,
                    idVProvider.getIdVProviderName(), e);
        }
    }

    private void addIDVProviderClaims(IdVProvider idVProvider, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        try (PreparedStatement addIDVProviderClaimsStmt = connection.prepareStatement(ADD_IDVP_CLAIM_SQL)) {
            for (Map.Entry<String, String> claimMapping : idVProvider.getClaimMappings().entrySet()) {
                addIDVProviderClaimsStmt.setInt(1, Integer.parseInt(idVProvider.getId()));
                addIDVProviderClaimsStmt.setInt(2, tenantId);
                addIDVProviderClaimsStmt.setString(3, claimMapping.getKey());
                addIDVProviderClaimsStmt.setString(4, claimMapping.getValue());
                addIDVProviderClaimsStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_ADDING_IDV_PROVIDER_CLAIMS,
                    idVProvider.getIdVProviderName(), e);
        }
    }

    private void updateIDVProviderClaims(IdVProvider idVProvider, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        int idVProviderId = Integer.parseInt(idVProvider.getId());
        deleteIDVProviderClaims(idVProviderId, tenantId, connection);
        if (MapUtils.isEmpty(idVProvider.getClaimMappings())) {
            return;
        }
        addIDVProviderClaims(idVProvider, tenantId, connection);
    }

    private void updateIDVProviderConfigs(IdVProvider idVProvider,
                                          int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        int idVProviderId = Integer.parseInt(idVProvider.getId());
        deleteIDVProviderConfigs(idVProviderId, tenantId, connection);
        if (ArrayUtils.isEmpty(idVProvider.getIdVConfigProperties())) {
            return;
        }
        addIDVProviderConfigs(idVProvider, tenantId, connection);
    }

    private void deleteIDVProviderConfigs(int idVId, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        try (PreparedStatement deleteIDVProviderConfigsStmt = connection.prepareStatement(DELETE_IDVP_CONFIG_SQL)) {
            deleteIDVProviderConfigsStmt.setInt(1, idVId);
            deleteIDVProviderConfigsStmt.setInt(2, tenantId);
            deleteIDVProviderConfigsStmt.executeUpdate();
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_DELETING_IDV_PROVIDER_CONFIGS,
                    String.valueOf(idVId), e);
        }
    }

    private void deleteIDVProviderClaims(int idVId, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        try (PreparedStatement deleteIDVProviderClaimsStmt = connection.prepareStatement(DELETE_IDVP_CLAIM_SQL)) {
            deleteIDVProviderClaimsStmt.setInt(1, idVId);
            deleteIDVProviderClaimsStmt.setInt(2, tenantId);
            deleteIDVProviderClaimsStmt.executeUpdate();
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_DELETING_IDV_PROVIDER_CLAIMS,
                    String.valueOf(idVId), e);
        }
    }

    private IdVProvider getIdVProviderWithConfigs(IdVProvider idVProvider,
                                                  int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        IdVConfigProperty[] idVConfigProperties = new IdVConfigProperty[0];
        List<IdVConfigProperty> idVConfigPropertyList = new ArrayList<>();
        try (PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVP_CONFIG_SQL)) {
            getIdVProvidersStmt.setInt(1, Integer.parseInt(idVProvider.getId()));
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    IdVConfigProperty idVConfigProperty = new IdVConfigProperty();
                    idVConfigProperty.setName(idVProviderResultSet.getString(PROPERTY_KEY));
                    idVConfigProperty.setValue(idVProviderResultSet.getString(PROPERTY_VALUE));
                    idVConfigProperty.setConfidential(idVProviderResultSet.getBoolean(IS_SECRET));
                    idVConfigPropertyList.add(idVConfigProperty);
                }
                idVProvider.setIdVConfigProperties(idVConfigPropertyList.toArray(idVConfigProperties));
            }
            IdVPSecretProcessor secretProcessor = new IdVPSecretProcessor();
            return secretProcessor.decryptAssociatedSecrets(idVProvider);
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER_CONFIGS, e);
        } catch (SecretManagementException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER_SECRETS,
                    idVProvider.getIdVProviderName(), e);
        }
    }

    private void getIdVProvidersWithClaims(IdVProvider idVProvider, int tenantId, Connection connection)
            throws IdvProviderMgtServerException {

        Map<String, String> idVClaimMap = new HashMap<>();
        try (PreparedStatement getIdVProvidersStmt = connection.prepareStatement(GET_IDVP_CLAIMS_SQL)) {
            getIdVProvidersStmt.setInt(1, Integer.parseInt(idVProvider.getId()));
            getIdVProvidersStmt.setInt(2, tenantId);

            try (ResultSet idVProviderResultSet = getIdVProvidersStmt.executeQuery()) {
                while (idVProviderResultSet.next()) {
                    idVClaimMap.put(idVProviderResultSet.getString(CLAIM), idVProviderResultSet.getString(LOCAL_CLAIM));
                    idVProvider.setClaimMappings(idVClaimMap);
                }
            }
        } catch (SQLException e) {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDER_CLAIMS,
                    idVProvider.getIdVProviderName(), e);
        }
    }

    /**
     * Get the SQL query for retrieving identity verification providers based on the database type.
     *
     * @param databaseProductName Name of the database product.
     * @return SQL query string.
     * @throws IdvProviderMgtServerException If an error occurs while retrieving the SQL query.
     */
    private String getIdvProvidersSqlQuery(String databaseProductName) throws IdvProviderMgtServerException {

        String sqlQuery;
        if (databaseProductName.contains("H2") || databaseProductName.contains("MySQL") ||
                databaseProductName.contains("MariaDB") || databaseProductName.contains("DB2")) {
            sqlQuery = GET_IDVPS_SQL_BY_MYSQL_WITH_FILTER;
        } else if (databaseProductName.contains("Oracle") || databaseProductName.contains("Microsoft")) {
            sqlQuery = GET_IDVPS_SQL_BY_MSSQL_WITH_FILTER;
        } else if (databaseProductName.contains("PostgreSQL")) {
            sqlQuery = GET_IDVPS_SQL_BY_POSTGRESQL_WITH_FILTER;
        } else {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_RETRIEVING_IDV_PROVIDERS);
        }
        return sqlQuery;
    }

    /**
     * Generate a prepared statement for retrieving identity verification providers.
     *
     * @param connection         Database connection.
     * @param filterQueryBuilder Filter query builder containing filter conditions.
     * @return Prepared statement for retrieving identity verification providers.
     * @throws SQLException                  If an SQL error occurs.
     * @throws IdvProviderMgtServerException If an error occurs while generating the prepared statement.
     */
    private PreparedStatement generateGetIdvProvidersPrepStmt(Connection connection, FilterQueryBuilder filterQueryBuilder)
            throws SQLException, IdvProviderMgtServerException {

        String databaseProductName = connection.getMetaData().getDatabaseProductName();
        String sqlQuery = getIdvProvidersSqlQuery(databaseProductName);
        sqlQuery = String.format(sqlQuery, filterQueryBuilder.getFilterQuery());

        return connection.prepareStatement(sqlQuery);
    }

    /**
     * Bind parameters to the prepared statement for retrieving identity verification providers.
     *
     * @param prepStmt            Prepared statement.
     * @param tenantId            Tenant ID.
     * @param offset              Offset for pagination.
     * @param limit               Limit for pagination.
     * @param filterQueryBuilder  Filter query builder containing filter conditions.
     * @param databaseProductName Name of the database product.
     * @throws SQLException If an SQL error occurs.
     */
    private void bindGetIdvProvidersPrepStmt(PreparedStatement prepStmt, int tenantId, int offset, int limit, FilterQueryBuilder filterQueryBuilder, String databaseProductName)
            throws SQLException {

        Map<Integer, String> filterAttributeValue = filterQueryBuilder.getFilterAttributeValue();
        int filterAttributeValueSize = filterAttributeValue.entrySet().size();

        for (Map.Entry<Integer, String> prepareStatement : filterAttributeValue.entrySet()) {
            prepStmt.setString(prepareStatement.getKey(), prepareStatement.getValue());
        }

        prepStmt.setInt(filterAttributeValueSize + 1, tenantId);

        if (databaseProductName.contains("PostgreSQL")) {
            prepStmt.setInt(filterAttributeValueSize + 2, limit);
            prepStmt.setInt(filterAttributeValueSize + 3, offset);
        } else {
            prepStmt.setInt(filterAttributeValueSize + 2, offset);
            prepStmt.setInt(filterAttributeValueSize + 3, limit);
        }
    }

    /**
     * Append filter query conditions to the filter query builder.
     *
     * @param expressionNodes    List of expression nodes containing filter conditions.
     * @param filterQueryBuilder Filter query builder to append conditions to.
     * @throws IdVProviderMgtClientException If an error occurs while appending filter conditions.
     */
    private void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder)
            throws IdVProviderMgtClientException {

        StringBuilder filter = new StringBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(EMPTY_STRING);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attributeName = expressionNode.getAttributeValue();
                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
                    switch (attributeName) {
                        case IDVP_FILTER_NAME:
                            attributeName = NAME;
                            break;
                        case IDVP_FILTER_DESCRIPTION:
                            attributeName = DESCRIPTION;
                            break;
                        case IDVP_FILTER_TYPE:
                            attributeName = IDVP_TYPE;
                            break;
                        case IDVP_FILTER_IS_ENABLED:
                            attributeName = IS_ENABLED;
                            break;
                        case IDVP_FILTER_UUID:
                            attributeName = IDVP_UUID;
                            break;
                        default:
                            String message = "Invalid filter attribute name. Filter attribute : " + attributeName;
                            throw IdVProviderMgtExceptionManagement.
                                    handleClientException(ERROR_RETRIEVING_FILTERED_IDV_PROVIDERS, message);
                    }

                    if (EQ.equals(operation)) {
                        filter.append(attributeName).append(" = ? AND ");
                        filterQueryBuilder.setFilterAttributeValue(value);
                    } else if (SW.equals(operation)) {
                        filter.append(attributeName).append(" like ? AND ");
                        filterQueryBuilder.setFilterAttributeValue(value + "%");
                    } else if (EW.equals(operation)) {
                        filter.append(attributeName).append(" like ? AND ");
                        filterQueryBuilder.setFilterAttributeValue("%" + value);
                    } else if (CO.equals(operation)) {
                        filter.append(attributeName).append(" like ? AND ");
                        filterQueryBuilder.setFilterAttributeValue("%" + value + "%");
                    } else {
                        String message = "Invalid filter value. filter: " + operation;
                        throw IdVProviderMgtExceptionManagement.
                                handleClientException(ERROR_RETRIEVING_FILTERED_IDV_PROVIDERS, message);
                    }
                }
            }
            if (StringUtils.isBlank(filter.toString())) {
                filterQueryBuilder.setFilterQuery(EMPTY_STRING);
            } else {
                filterQueryBuilder.setFilterQuery(filter.toString());
            }
        }
    }
}
