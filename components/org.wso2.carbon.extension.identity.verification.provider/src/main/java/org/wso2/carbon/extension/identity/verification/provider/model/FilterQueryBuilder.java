/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.extension.identity.verification.provider.model;

import java.util.HashMap;
import java.util.Map;

public class FilterQueryBuilder {

    private Map<Integer, String> stringParameters = new HashMap<>();
    private int count = 1;
    private String filter;

    public FilterQueryBuilder() {
    }

    /**
     * Returns the map of filter attribute values.
     *
     * @return A map where the key is an integer and the value is a string representing the filter attribute value.
     */
    public Map<Integer, String> getFilterAttributeValue() {

        return this.stringParameters;
    }

    /**
     * Sets a filter attribute value.
     *
     * @param value The value to be set as a filter attribute.
     */
    public void setFilterAttributeValue(String value) {

        this.stringParameters.put(this.count, value);
        ++this.count;
    }

    /**
     * Resets the filter attribute values to an empty map.
     */
    public void setEmptyFilterAttributeValue() {
        
        this.stringParameters = new HashMap<>();
    }

    /**
     * Sets the filter query string.
     *
     * @param filter The filter query string to be set.
     */
    public void setFilterQuery(String filter) {

        this.filter = filter;
    }

    /**
     * Returns the filter query string.
     *
     * @return The filter query string.
     */
    public String getFilterQuery() {

        return this.filter;
    }
}
