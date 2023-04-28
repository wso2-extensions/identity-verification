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

package org.wso2.carbon.identity.verification.mongo.datasource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mongodb.MongoClientException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.Filters;

import org.json.JSONArray;
import org.wso2.carbon.extension.identity.verification.mgt.dao.IdentityVerificationClaimDAO;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationServerException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import com.google.gson.Gson;
import org.bson.Document;
import org.bson.conversions.Bson;

import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_PROCESSING_IDV_CLAIM;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_ADDING_IDV_CLAIMS;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_UPDATING_IDV_CLAIM;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_RETRIEVING_IDV_CLAIM;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_RETRIEVING_IDV_CLAIMS;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_CHECKING_IDV_CLAIM_EXISTENCE;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_CHECKING_IDV_CLAIM_DATA_EXISTENCE;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_DELETING_IDV_CLAIM;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_GETTING_USER_STORE_URL;
import static org.wso2.carbon.identity.verification.mongo.datasource.utils.IdentityVerificationConstants.ErrorMessage.
        ERROR_GETTING_USER_STORE_DATA;

/**
 * Identity verification claim DAO class using MongoDB as the user store.
 */
public class IdentityVerificationClaimDAOImpl implements IdentityVerificationClaimDAO {

    /**
     * Get priority value for the {@link IdentityVerificationClaimDAO}.
     *
     * @return Priority value for the DAO.
     */
    @Override
    public int getPriority() {

        return 1;
    }

    /**
     * Add the identity verification claim.
     *
     * @param idvClaimList IdentityVerificationClaim list.
     * @param tenantId     Tenant id.
     * @throws IdentityVerificationException Identity verification claim management exception.
     */
    @Override
    public void addIdVClaimList(List<IdVClaim> idvClaimList, int tenantId) throws IdentityVerificationException {

        try (MongoClient mongoClient = createDatabaseConnection()) {
            Gson gson = new Gson();
            List<Document> documents = new ArrayList<>();
            for (IdVClaim idVClaim : idvClaimList) {
                Document doc = Document.parse(gson.toJson(idVClaim));
                Document metadataDoc = mapMetadataToDocument(idVClaim);
                doc.remove(IdentityVerificationConstants.METADATA);
                doc.append(IdentityVerificationConstants.METADATA, metadataDoc);
                doc.append(IdentityVerificationConstants.TENANT_ID, tenantId);
                documents.add(doc);
            }
            MongoCollection<Document> collection = getCollectionName(mongoClient);
            collection.insertMany(documents);
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_ADDING_IDV_CLAIMS.getCode(),
                    ERROR_ADDING_IDV_CLAIMS.getMessage(), e);
        }
    }

    /**
     * Update the identity verification claim by the user id.
     *
     * @param idVClaim IdentityVerificationClaim.
     * @param tenantId Tenant id.
     * @throws IdentityVerificationException Identity verification claim management exception.
     */
    @Override
    public void updateIdVClaim(IdVClaim idVClaim, int tenantId) throws IdentityVerificationException {

        try (MongoClient mongoClient = createDatabaseConnection()) {
            MongoCollection<Document> collection = getCollectionName(mongoClient);
            Bson filter = Filters.and(
                    Filters.eq(IdentityVerificationConstants.IDV_CLAIM, idVClaim.getUuid()),
                    Filters.eq(IdentityVerificationConstants.USER_ID, idVClaim.getUserId()),
                    Filters.eq(IdentityVerificationConstants.TENANT_ID, tenantId));
            Document metadataDoc = Document.parse(idVClaim.getMetadata().toString());
            Bson update = Updates.combine(
                    Updates.set(IdentityVerificationConstants.STATUS, idVClaim.getStatus()),
                    Updates.set(IdentityVerificationConstants.METADATA, metadataDoc));
            collection.updateOne(filter, update);
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_UPDATING_IDV_CLAIM.getCode(),
                    ERROR_UPDATING_IDV_CLAIM.getMessage(), e);
        }
    }

    /**
     * Get the identity verification claim.
     *
     * @param userId     User id.
     * @param idVClaimId Identity verification claim id.
     * @param tenantId   Tenant id.
     * @return Identity verification claim.
     * @throws IdentityVerificationException Identity verification claim management exception.
     */
    @Override
    public IdVClaim getIDVClaim(String userId, String idVClaimId, int tenantId) throws IdentityVerificationException {

        try (MongoClient mongoClient = createDatabaseConnection()) {
            MongoCollection<Document> collection = getCollectionName(mongoClient);
            Document doc = collection.find(Filters.and(
                    Filters.eq(IdentityVerificationConstants.IDV_CLAIM, idVClaimId),
                    Filters.eq(IdentityVerificationConstants.TENANT_ID, tenantId))).first();
            if (doc != null) {
                return mapJsonToObj(doc.toJson());
            }
            return null;
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_RETRIEVING_IDV_CLAIM.getCode(),
                    ERROR_RETRIEVING_IDV_CLAIM.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new IdentityVerificationServerException(ERROR_PROCESSING_IDV_CLAIM.getCode(),
                    ERROR_PROCESSING_IDV_CLAIM.getMessage(), e);
        }
    }

    /**
     * Get the identity verification claims.
     *
     * @param tenantId Tenant id.
     * @param userId   User id.
     * @return Identity verification claim.
     * @throws IdentityVerificationException Identity verification claim management exception.
     */
    @Override
    public IdVClaim[] getIDVClaims(String userId, int tenantId) throws IdentityVerificationException {

        try (MongoClient mongoClient = createDatabaseConnection()) {
            MongoCollection<Document> collection = getCollectionName(mongoClient);
            Bson filter = Filters.and(
                    Filters.eq(IdentityVerificationConstants.USER_ID, userId),
                    Filters.eq(IdentityVerificationConstants.TENANT_ID, tenantId));
            MongoCursor<Document> documents = collection.find(filter).iterator();
            List<IdVClaim> idVClaims = new ArrayList<>();
            while (documents.hasNext()) {
                idVClaims.add(mapJsonToObj(documents.next().toJson()));
            }
            return idVClaims.toArray(new IdVClaim[0]);
        } catch (JsonProcessingException e) {
            throw new IdentityVerificationServerException(ERROR_PROCESSING_IDV_CLAIM.getCode(),
                    ERROR_PROCESSING_IDV_CLAIM.getMessage(), e);
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_RETRIEVING_IDV_CLAIMS.getCode(),
                    ERROR_RETRIEVING_IDV_CLAIMS.getMessage(), e);
        }
    }

    /**
     * Delete the identity verification claim.
     *
     * @param userId     User id.
     * @param idVClaimId Identity verification claim id.
     * @param tenantId   Tenant id.
     * @throws IdentityVerificationException Identity verification exception.
     */
    @Override
    public void deleteIdVClaim(String userId, String idVClaimId, int tenantId) throws IdentityVerificationException {

        try (MongoClient mongoClient = createDatabaseConnection()) {
            MongoCollection<Document> collection = getCollectionName(mongoClient);
            Bson filter = Filters.and(
                    Filters.eq(IdentityVerificationConstants.USER_ID, userId),
                    Filters.eq(IdentityVerificationConstants.IDV_CLAIM, idVClaimId),
                    Filters.eq(IdentityVerificationConstants.TENANT_ID, tenantId)
                                     );
            collection.deleteOne(filter);
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_DELETING_IDV_CLAIM.getCode(),
                    ERROR_DELETING_IDV_CLAIM.getMessage(), e);
        }
    }

    /**
     * Check whether the identity verification claim exist.
     *
     * @param userId   User id.
     * @param idVPId   Identity verification provider id.
     * @param uri      Claim uri.
     * @param tenantId Tenant id.
     * @return True if the identity verification claim exist.
     * @throws IdentityVerificationException Identity verification exception.
     */
    @Override
    public boolean isIdVClaimDataExist(String userId, String idVPId, String uri, int tenantId)
            throws IdentityVerificationException {

        try (MongoClient mongoClient = createDatabaseConnection()) {
            MongoCollection<Document> collection = getCollectionName(mongoClient);
            Document doc = collection.find(Filters.and(
                    Filters.eq(IdentityVerificationConstants.USER_ID, userId),
                    Filters.eq(IdentityVerificationConstants.TENANT_ID, tenantId),
                    Filters.eq(IdentityVerificationConstants.IDVP_ID, idVPId),
                    Filters.eq(IdentityVerificationConstants.CLAIM_URI, uri))).first();
            return doc != null;
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_CHECKING_IDV_CLAIM_DATA_EXISTENCE.getCode(),
                    ERROR_CHECKING_IDV_CLAIM_DATA_EXISTENCE.getMessage(), e);
        }
    }

    /**
     * Check whether the identity verification claim exist.
     *
     * @param claimId  Identity verification claim id.
     * @param tenantId Tenant id.
     * @return True if the identity verification claim exist.
     * @throws IdentityVerificationException Identity verification exception.
     */
    @Override
    public boolean isIdVClaimExist(String claimId, int tenantId) throws IdentityVerificationException {

        try (MongoClient mongoClient = createDatabaseConnection()) {
            MongoCollection<Document> collection = getCollectionName(mongoClient);
            Document doc = collection.find(Filters.and(
                    Filters.eq(IdentityVerificationConstants.IDV_CLAIM, claimId),
                    Filters.eq(IdentityVerificationConstants.TENANT_ID, tenantId))).first();
            return doc != null;
        } catch (MongoClientException e) {
            throw new IdentityVerificationServerException(ERROR_CHECKING_IDV_CLAIM_EXISTENCE.getCode(),
                    ERROR_CHECKING_IDV_CLAIM_EXISTENCE.getMessage(), e);
        }
    }

    /**
     * Connect to the MongoDB server.
     *
     * @return MongoClient connection object.
     * @throws IdentityVerificationServerException Identity Verification Server Exception.
     */
    private MongoClient createDatabaseConnection() throws IdentityVerificationServerException {

        String url = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConfigConstants.DATABASE_URL_REGEX);
        if (StringUtils.isNotBlank(url)) {
            return MongoClients.create(url);
        }
        throw new IdentityVerificationServerException(ERROR_GETTING_USER_STORE_URL.getCode(),
                ERROR_GETTING_USER_STORE_URL.getMessage());
    }

    /**
     * Get the collection name of the database.
     *
     * @param client which is a Database connetion object.
     * @return MongoCollection<Document> collection name.
     * @throws IdentityVerificationServerException Identity Verification Server Exception.
     */
    private MongoCollection<Document> getCollectionName(MongoClient client) throws IdentityVerificationServerException {

        String db = IdentityUtil.getProperty(IdentityVerificationConstants.DatabaseConfigConstants.DATABASE_NAME_REGEX);
        String collectionName = IdentityUtil.
                getProperty(IdentityVerificationConstants.DatabaseConfigConstants.DATABASE_COLLECTION_REGEX);
        if (StringUtils.isNotBlank(db) && StringUtils.isNotBlank(collectionName)) {
            return client.getDatabase(db).getCollection(collectionName);
        }
        throw new IdentityVerificationServerException(ERROR_GETTING_USER_STORE_DATA.getCode(),
                ERROR_GETTING_USER_STORE_DATA.getMessage());
    }

    /**
     * Map the json string to an identity verification claim object.
     *
     * @param json String format of the document.
     * @return Identity verification claim object.
     * @throws JsonProcessingException Json Processing Exception .
     */
    private IdVClaim mapJsonToObj(String json) throws JsonProcessingException {

        Gson gson = new Gson();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode metadataJsonNode = mapper.readTree(json).get(IdentityVerificationConstants.METADATA);
        IdVClaim idVClaim = gson.fromJson(json, IdVClaim.class);
        if (metadataJsonNode != null) {
            idVClaim.setMetadata(new JSONObject(metadataJsonNode.toString()));
        }
        return idVClaim;
    }

    /**
     * Map the metadata in identity verification claim object to a document.
     *
     * @param idVClaim IdentityVerificationClaim.
     * @return Document metadata document.
     */
    public Document mapMetadataToDocument(IdVClaim idVClaim){
        Document metadataDoc = new Document();
        for (String key : idVClaim.getMetadata().keySet()) {
            Object value = idVClaim.getMetadata().get(key);
            if (value instanceof JSONObject){
                Document aggregateMetadataDoc = Document.parse(value.toString());
                metadataDoc.append(key, aggregateMetadataDoc);
            }
            else if (value instanceof JSONArray){
                JSONObject jsonObj = new JSONObject();
                jsonObj.put(IdentityVerificationConstants.KEY, new JSONArray(value.toString()));
                Document aggregateMetadataDoc = Document.parse(jsonObj.toString());
                metadataDoc.append(key, aggregateMetadataDoc.get(IdentityVerificationConstants.KEY));
            }
            else{
                metadataDoc.append(key, value);
            }
        }
        return metadataDoc;
    }

}
