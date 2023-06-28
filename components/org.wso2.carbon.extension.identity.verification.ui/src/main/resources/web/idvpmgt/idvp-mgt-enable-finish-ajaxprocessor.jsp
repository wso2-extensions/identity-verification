<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
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

<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIUtils" %>
<%@ page
		import="static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.RESOURCE_BUNDLE" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClientImpl" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="static org.wso2.carbon.CarbonConstants.LOGGED_USER" %>

<%
	if(!IdVProviderUIUtils.isHTTPMethodAllowed(request)){
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		return;
	};

	ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, request.getLocale());
    try {
		IdVProviderMgtServiceClient client = IdVProviderMgtServiceClientImpl.getInstance();
        IdVProvider idVProvider;
		String idVProviderId = request.getParameter(IdVProviderUIConstants.KEY_IDVP_ID);
		String isEnable = request.getParameter(IdVProviderUIConstants.KEY_ENABLE);
		String currentUser = (String) session.getAttribute(LOGGED_USER);

		if (StringUtils.isNotBlank(idVProviderId) && StringUtils.isNotBlank(isEnable)) {
			idVProvider = client.getIdVProviderById(idVProviderId, currentUser);
			idVProvider.setEnabled(isEnable.equals("true"));
		} else {
			idVProvider = new IdVProvider();
		}

		client.updateIdVProvider(idVProviderId, idVProvider, currentUser);
	} catch (Exception e) {
		String message = MessageFormat.format(resourceBundle.getString("error.updating.idvp"), e.getMessage());
		CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }
%>

<script type="text/javascript">
	 // Redirects back to the Identity provider list page
    location.href = "idvp-mgt-list.jsp";
</script>
