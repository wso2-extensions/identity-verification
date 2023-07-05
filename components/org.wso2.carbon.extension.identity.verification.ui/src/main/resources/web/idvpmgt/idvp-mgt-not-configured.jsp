<!--
~ Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
~
~ WSO2 LLC. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page
  import="static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.RESOURCE_BUNDLE" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="<%=RESOURCE_BUNDLE%>">
    <div id="middle">
        <h2>
            <fmt:message key='identity.verification.providers'/>
        </h2>
        <div id="workArea">
            <div class="sectionSub">
                <table style="width: 100%" class="styledLeft">
                    <tbody>
                    <tr>
                        <td style="border:none !important">
                            <table class="styledLeft" width="100%" id="IdentityVerificationProviders">
                                <tbody>
                                <tr>
                                    <td colspan="3">
                                        <i>
                                            <fmt:message key="idvp.not.configured.placeholder"/>
                                            <a href="https://store.wso2.com/store/assets/isconnector/list">
                                                WSO2 Connector Store
                                            </a>
                                        </i>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</fmt:bundle>
