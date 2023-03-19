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

package org.wso2.carbon.extension.identity.verification.mgt.utils;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationClientException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationServerException;

/**
 * This class contains the implementation for the Identity Verification exception management.
 */
public class IdentityVerificationExceptionMgt {

    /**
     * Handle the client exceptions.
     *
     * @param error The ErrorMessage.
     * @param data  The data to be added to the message if needed.
     * @return IdentityVerificationClientException.
     */
    public static IdentityVerificationClientException handleClientException(
            IdentityVerificationConstants.ErrorMessage error, String data) {

        String message = includeData(error, data);
        return new IdentityVerificationClientException(error.getCode(), message);
    }

    /**
     * Handle the client exceptions.
     *
     * @param error The ErrorMessage.
     * @param data  The data to be added to the message if needed.
     * @param e     The throwable.
     * @return IdentityVerificationClientException.
     */
    public static IdentityVerificationClientException handleClientException(
            IdentityVerificationConstants.ErrorMessage error, String data, Throwable e) {

        String message = includeData(error, data);
        return new IdentityVerificationClientException(error.getCode(), message, e);
    }

    /**
     * Handle the client exceptions.
     *
     * @param error The ErrorMessage.
     * @return IdentityVerificationClientException.
     */
    public static IdentityVerificationClientException handleClientException(
            IdentityVerificationConstants.ErrorMessage error) {

        String message = error.getMessage();
        return new IdentityVerificationClientException(error.getCode(), message);
    }

    /**
     * Handle the Server exceptions.
     *
     * @param error The ErrorMessage.
     * @param data  The data to be added to the message if needed.
     * @param e     The throwable.
     * @return IdentityVerificationClientException.
     */
    public static IdentityVerificationServerException handleServerException(
            IdentityVerificationConstants.ErrorMessage error, String data, Throwable e) {

        String message = includeData(error, data);
        return new IdentityVerificationServerException(error.getCode(), message, e);
    }

    /**
     * Handle the server exceptions.
     *
     * @param error The ErrorMessage.
     * @return IdentityVerificationServerException.
     */
    public static IdentityVerificationServerException handleServerException(
            IdentityVerificationConstants.ErrorMessage error) {

        String message = error.getMessage();
        return new IdentityVerificationServerException(error.getCode(), message);
    }

    /**
     * Handle the server exceptions.
     *
     * @param error The ErrorMessage.
     * @param e     The throwable.
     * @return IdentityVerificationServerException.
     */
    public static IdentityVerificationServerException handleServerException(
            IdentityVerificationConstants.ErrorMessage error, Throwable e) {

        String message = error.getMessage();
        return new IdentityVerificationServerException(error.getCode(), message, e);
    }

    /**
     * Include the data to the error message.
     *
     * @param error FunctionLibraryManagementConstants.ErrorMessage.
     * @param data  data to replace if message needs to be replaced.
     * @return message format with data.
     */
    private static String includeData(IdentityVerificationConstants.ErrorMessage error, String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }
}
