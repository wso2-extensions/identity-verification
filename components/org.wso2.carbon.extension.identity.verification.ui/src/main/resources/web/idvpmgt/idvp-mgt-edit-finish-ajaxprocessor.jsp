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
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.ExtensionMgtServiceClient" %>
<%@ page
import="static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.RESOURCE_BUNDLE" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.ExtensionMgtServiceClientImpl" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClientImpl" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIUtils" %>
<%@ page import="static org.wso2.carbon.CarbonConstants.LOGGED_USER" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page
import="static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.METADATA_COMMON" %>
<%@ page import="static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.*" %>

<%
	if (!IdVProviderUIUtils.isHTTPMethodAllowed(request)) {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		return;
	}

	ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, request.getLocale());
	try {
		IdVProvider idVProvider;
		IdVProviderMgtServiceClient client = IdVProviderMgtServiceClientImpl.getInstance();
		ExtensionMgtServiceClient extensionMgtClient = ExtensionMgtServiceClientImpl.getInstance();
		String idVProviderId = request.getParameter(IdVProviderUIConstants.KEY_IDVP_ID);
		String currentUser = (String) session.getAttribute(LOGGED_USER);
		String idVPType = request.getParameter(IdVProviderUIConstants.KEY_IDVP_TYPE);
		JSONArray metadata = extensionMgtClient.getIdVProviderMetadata(idVPType).getJSONObject(METADATA_COMMON)
		.getJSONArray(METADATA_CONFIG_PROPERTIES);

		if (StringUtils.isNotBlank(idVProviderId)) {
			// Update existing IdV Provider
			idVProvider = client.getIdVProviderById(idVProviderId, currentUser);
			IdVProviderUIUtils.populateIdVPInfo(idVProvider, request, metadata);
			client.updateIdVProvider(idVProviderId, idVProvider, currentUser);
		} else {
			// Add a new IdV Provider
			idVProvider = extensionMgtClient.getIdVProviderTemplate(idVPType);
			IdVProviderUIUtils.populateIdVPInfo(idVProvider, request, metadata);
			client.addIdVProvider(idVProvider, currentUser);
		}

	} catch (Exception e) {
		String message = MessageFormat.format(resourceBundle.getString("error.updating.idvp"), e.getMessage());
		CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
	}
%>

<script type="text/javascript">
	 // Redirects back to the Identity provider list page
    location.href = "idvp-mgt-list.jsp";
</script>
%>
