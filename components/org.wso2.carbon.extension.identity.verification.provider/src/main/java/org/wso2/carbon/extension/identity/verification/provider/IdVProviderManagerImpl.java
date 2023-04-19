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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.internal.IdVProviderDataHolder;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants;
import org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtExceptionManagement;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.List;
import java.util.UUID;

import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_CODE_GET_DAO;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.ErrorMessage.ERROR_IDVP_ALREADY_EXISTS;

/**
 * This class contains the implementation for the IdVProviderManager.
 */
public class IdVProviderManagerImpl implements IdVProviderManager {

    private static final Log log = LogFactory.getLog(IdVProviderManagerImpl.class);
    private final List<IdVProviderDAO> idVProviderDAOs;

    public IdVProviderManagerImpl() {

        this.idVProviderDAOs = IdVProviderDataHolder.getInstance().getIdVProviderDAOs();
    }

    /**
     * Select highest priority IdVProvider DAO from an already sorted list of IdVProvider DAOs.
     *
     * @return Highest priority Resource DAO.
     */
    private IdVProviderDAO getIdVProviderDAO() throws IdVProviderMgtException {

        if (!this.idVProviderDAOs.isEmpty()) {
            return idVProviderDAOs.get(idVProviderDAOs.size() - 1);
        } else {
            throw IdVProviderMgtExceptionManagement.handleServerException(ERROR_CODE_GET_DAO);
        }
    }

    @Override
    public IdVProvider getIdVProvider(String idVProviderId, int tenantId)
            throws IdVProviderMgtException {

        if (StringUtils.isEmpty(idVProviderId)) {
            throw IdVProviderMgtExceptionManagement.
                    handleClientException(IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_ID);
        }
        return getIdVProviderDAO().getIdVProvider(idVProviderId, tenantId);
    }

    @Override
    public IdVProvider addIdVProvider(IdVProvider idVProvider, int tenantId) throws IdVProviderMgtException {

        String idVPName = idVProvider.getIdVProviderName();
        if (isIdVProviderExistsByName(idVPName, tenantId)) {
            throw IdVProviderMgtExceptionManagement.handleClientException(ERROR_IDVP_ALREADY_EXISTS, idVPName);
        }
        idVProvider.setIdVProviderUUID(UUID.randomUUID().toString());
        this.getIdVProviderDAO().addIdVProvider(idVProvider, tenantId);
        return idVProvider;
    }

    @Override
    public int getCountOfIdVProviders(int tenantId) throws IdVProviderMgtException {

        return getIdVProviderDAO().getCountOfIdVProviders(tenantId);
    }

    @Override
    public void deleteIdVProvider(String idVProviderId, int tenantId) throws IdVProviderMgtException {

        if (StringUtils.isEmpty(idVProviderId)) {
            throw IdVProviderMgtExceptionManagement.
                    handleClientException(IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_ID);
        }
        getIdVProviderDAO().deleteIdVProvider(idVProviderId, tenantId);
    }

    @Override
    public IdVProvider updateIdVProvider(IdVProvider oldIdVProvider,
                                         IdVProvider updatedIdVProvider,
                                         int tenantId) throws IdVProviderMgtException {

        if (oldIdVProvider == null || updatedIdVProvider == null) {
            throw IdVProviderMgtExceptionManagement.handleServerException(IdVProviderMgtConstants.ErrorMessage.
                    ERROR_UPDATING_IDV_PROVIDER);
        }
        if (isIdVProviderExistsByName(updatedIdVProvider.getIdVProviderName(), tenantId)) {
            throw IdVProviderMgtExceptionManagement.
                    handleClientException(ERROR_IDVP_ALREADY_EXISTS, updatedIdVProvider.getIdVProviderName());
        }
        getIdVProviderDAO().updateIdVProvider(oldIdVProvider, updatedIdVProvider, tenantId);
        return updatedIdVProvider;
    }

    @Override
    public List<IdVProvider> getIdVProviders(Integer limit, Integer offset, int tenantId)
            throws IdVProviderMgtException {

        return getIdVProviderDAO().getIdVProviders(validateLimit(limit), validateOffset(offset), tenantId);
    }

    @Override
    public boolean isIdVProviderExists(String idvProviderId, int tenantId) throws IdVProviderMgtException {

        if (StringUtils.isEmpty(idvProviderId)) {
            throw IdVProviderMgtExceptionManagement.
                    handleClientException(IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_ID);
        }
        return getIdVProviderDAO().isIdVProviderExists(idvProviderId, tenantId);
    }

    @Override
    public boolean isIdVProviderExistsByName(String idvProviderName, int tenantId) throws IdVProviderMgtException {

        if (StringUtils.isEmpty(idvProviderName)) {
            throw IdVProviderMgtExceptionManagement.
                    handleClientException(IdVProviderMgtConstants.ErrorMessage.ERROR_EMPTY_IDVP_);
        }
        return getIdVProviderDAO().isIdVProviderExistsByName(idvProviderName, tenantId);
    }

    @Override
    public IdVProvider getIdVProviderByName(String idVPName, int tenantId)
            throws IdVProviderMgtException {

        if (StringUtils.isEmpty(idVPName)) {
            throw IdVProviderMgtExceptionManagement.handleClientException(ERROR_EMPTY_IDVP_);
        }
        return getIdVProviderDAO().getIdVProviderByName(idVPName, tenantId);
    }

    /**
     * Validate limit.
     *
     * @param limit given limit value.
     * @return validated limit and offset value.
     */
    private int validateLimit(Integer limit) throws IdVProviderMgtClientException {

        if (limit == null) {
            if (log.isDebugEnabled()) {
                log.debug("Given limit is null. Therefore we get the default limit from identity.xml.");
            }
            limit = IdentityUtil.getDefaultItemsPerPage();
        }
        if (limit < 0) {
            String message = "Given limit: " + limit + " is a negative value.";
            throw IdVProviderMgtExceptionManagement.
                    handleClientException(IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDERS, message);
        }

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();
        if (limit > maximumItemsPerPage) {
            if (log.isDebugEnabled()) {
                log.debug("Given limit exceed the maximum limit. Therefore we get the default limit from " +
                        "identity.xml. limit: " + maximumItemsPerPage);
            }
            limit = maximumItemsPerPage;
        }
        return limit;
    }

    /**
     * Validate offset.
     *
     * @param offset given offset value.
     * @return validated limit and offset value.
     * @throws IdVProviderMgtClientException Error while set offset
     */
    private int validateOffset(Integer offset) throws IdVProviderMgtClientException {

        if (offset == null) {
            offset = 0;
        }

        if (offset < 0) {
            String message = "Invalid offset applied. Offset should not negative. offSet: " + offset;
            throw IdVProviderMgtExceptionManagement.
                    handleClientException(IdVProviderMgtConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDERS, message);
        }
        return offset;
    }
}
