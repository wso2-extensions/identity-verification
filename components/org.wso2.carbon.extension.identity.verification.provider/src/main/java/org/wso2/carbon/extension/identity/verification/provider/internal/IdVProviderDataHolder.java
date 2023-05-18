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
package org.wso2.carbon.extension.identity.verification.provider.internal;

import org.wso2.carbon.extension.identity.verification.provider.dao.IdVProviderDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to keep the data of the IdVProvider manager component.
 */
public class IdVProviderDataHolder {

    List<IdVProviderDAO> idVProviderDAOs = new ArrayList<>();
    private static final IdVProviderDataHolder instance = new IdVProviderDataHolder();

    private IdVProviderDataHolder() {

    }

    public static IdVProviderDataHolder getInstance() {

        return instance;
    }

    /**
     * Get the list of IdVProviderDAOs.
     *
     * @return List of IdVProviderDAOs.
     */
    public List<IdVProviderDAO> getIdVProviderDAOs() {

        return idVProviderDAOs;
    }

    /**
     * Set the list of IdVProviderDAOs.
     *
     * @param idVProviderDAOs List of IdVProviderDAOs.
     */
    public void setIdVProviderDAOs(List<IdVProviderDAO> idVProviderDAOs) {

        this.idVProviderDAOs = idVProviderDAOs;
    }
}
