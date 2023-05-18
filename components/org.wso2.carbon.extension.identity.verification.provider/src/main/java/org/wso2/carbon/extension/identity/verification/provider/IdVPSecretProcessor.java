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

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretsProcessor;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.IDVP_SECRET_TYPE_IDVP_SECRETS;
import static org.wso2.carbon.extension.identity.verification.provider.util.IdVProviderMgtConstants.SEPERATOR;

/**
 * This class contains the implementation for the Secrets Processor of IdVProviderManager.
 */
public class IdVPSecretProcessor implements SecretsProcessor<IdVProvider> {

    private final SecretManager secretManager;
    private final SecretResolveManager secretResolveManager;
    private final Gson gson;

    public IdVPSecretProcessor() {

        this.secretManager = new SecretManagerImpl();
        this.secretResolveManager = new SecretResolveManagerImpl();
        this.gson = new Gson();
    }

    @Override
    public IdVProvider decryptAssociatedSecrets(IdVProvider idVProvider)
            throws SecretManagementException {

        IdVProvider clonedIdVP =
                gson.fromJson(gson.toJson(idVProvider), IdVProvider.class);
        for (IdVConfigProperty idVConfigProperty : clonedIdVP.getIdVConfigProperties()) {
            if (!idVConfigProperty.isConfidential()) {
                continue;
            }
            String secretName = buildSecretName(clonedIdVP.getIdVProviderUuid(), idVConfigProperty.getName());
            if (secretManager.isSecretExist(IDVP_SECRET_TYPE_IDVP_SECRETS, secretName)) {
                ResolvedSecret resolvedSecret =
                        secretResolveManager.getResolvedSecret(IDVP_SECRET_TYPE_IDVP_SECRETS, secretName);
                // Replace secret reference with decrypted original secret.
                idVConfigProperty.setValue(resolvedSecret.getResolvedSecretValue());
            }
        }
        return clonedIdVP;
    }

    @Override
    public IdVProvider encryptAssociatedSecrets(IdVProvider idVProvider)
            throws SecretManagementException {

        IdVProvider clonedIdVP =
                gson.fromJson(gson.toJson(idVProvider), IdVProvider.class);
        for (IdVConfigProperty idVConfigProperty : clonedIdVP.getIdVConfigProperties()) {
            if (!idVConfigProperty.isConfidential()) {
                continue;
            }
            String secretName = buildSecretName(clonedIdVP.getIdVProviderUuid(), idVConfigProperty.getName());
            if (secretManager.isSecretExist(IDVP_SECRET_TYPE_IDVP_SECRETS, secretName)) {
                // Update existing secret property.
                updateExistingSecretProperty(secretName, idVConfigProperty);
                idVConfigProperty.setValue(buildSecretReference(secretName));
            } else {
                // Add secret to the DB.
                if (StringUtils.isEmpty(idVConfigProperty.getValue())) {
                    continue;
                }
                addNewIdVPSecretProperty(secretName, idVConfigProperty);
                idVConfigProperty.setValue(buildSecretReference(secretName));
            }
        }
        return clonedIdVP;
    }

    @Override
    public void deleteAssociatedSecrets(IdVProvider idVProvider) throws SecretManagementException {

        for (IdVConfigProperty idVConfigProperty : idVProvider.getIdVConfigProperties()) {
            if (!idVConfigProperty.isConfidential()) {
                continue;
            }
            String secretName = buildSecretName(idVProvider.getId(), idVConfigProperty.getName());
            if (secretManager.isSecretExist(IDVP_SECRET_TYPE_IDVP_SECRETS, secretName)) {
                secretManager.deleteSecret(IDVP_SECRET_TYPE_IDVP_SECRETS, secretName);
            }
        }
    }

    private String buildSecretName(String idpId, String propName) {

        return idpId + SEPERATOR + propName;
    }

    private String buildSecretReference(String secretName) throws SecretManagementException {

        SecretType secretType = secretManager.getSecretType(IDVP_SECRET_TYPE_IDVP_SECRETS);
        return secretType.getId() + SEPERATOR + secretName;
    }

    private void addNewIdVPSecretProperty(String secretName, IdVConfigProperty idVConfigProperty)
            throws SecretManagementException {

        Secret secret = new Secret();
        secret.setSecretName(secretName);
        secret.setSecretValue(idVConfigProperty.getValue());
        secretManager.addSecret(IDVP_SECRET_TYPE_IDVP_SECRETS, secret);
    }

    private void updateExistingSecretProperty(String secretName, IdVConfigProperty idVConfigProperty)
            throws SecretManagementException {

        ResolvedSecret resolvedSecret =
                secretResolveManager.getResolvedSecret(IDVP_SECRET_TYPE_IDVP_SECRETS, secretName);
        if (!resolvedSecret.getResolvedSecretValue().equals(idVConfigProperty.getValue())) {
            secretManager.updateSecretValue(IDVP_SECRET_TYPE_IDVP_SECRETS, secretName, idVConfigProperty.getValue());
        }
    }
}
