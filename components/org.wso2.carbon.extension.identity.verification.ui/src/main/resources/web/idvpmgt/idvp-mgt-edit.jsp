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

<%@ page isELIgnored="false" %>

<%@page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.ExtensionMgtServiceClient" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.ExtensionMgtServiceClientImpl" %>
<%@ page import="static org.wso2.carbon.CarbonConstants.LOGGED_USER" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClientImpl" %>
<%@ page
  import="static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.RESOURCE_BUNDLE" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants" %>
<%@ page import="org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>
<link href="css/idvp-mgt.css" rel="stylesheet" type="text/css" media="all"/>

<%
    String idVPId = request.getParameter(IdVProviderUIConstants.KEY_IDVP_ID);

    String[] claimURIs;
    StringBuilder existingIdVProviderNames;
    IdVProvider idVProvider = null;
    JSONObject idVProviderUIMetadata = null;
    List<ExtensionInfo> infoPerIdVProvider;
    Map<String, JSONObject> metadataPerIdVProvider;
    IdVProviderMgtServiceClient idVPMgtClient = IdVProviderMgtServiceClientImpl.getInstance();
    ExtensionMgtServiceClient extensionMgtClient = ExtensionMgtServiceClientImpl.getInstance();
    String currentUser = (String) session.getAttribute(LOGGED_USER);
    ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, request.getLocale());

    try {
        infoPerIdVProvider = extensionMgtClient.getExtensionInfoOnIdVProviderTypes();

        if (infoPerIdVProvider.isEmpty()) {
            // Redirect to the placeholder page if there are no IdV Providers templates configured
            response.sendRedirect("idvp-mgt-not-configured.jsp");
        }

        List<String> availableIdVPTypes = infoPerIdVProvider.stream().map(ExtensionInfo::getId)
          .collect(Collectors.toList());
        metadataPerIdVProvider = extensionMgtClient.getIdVProviderMetadataMap(availableIdVPTypes);

        // If there is an idVPId, the page is loaded in edit mode with existing data. Otherwise, the page is loaded in
        // create mode with default data.
        if (StringUtils.isNotBlank(idVPId)) {
            idVProvider = idVPMgtClient.getIdVProviderById(idVPId, currentUser);
            idVProviderUIMetadata = extensionMgtClient.getIdVProviderMetadata(idVProvider.getType());
        } else {
            idVProviderUIMetadata = metadataPerIdVProvider.get(availableIdVPTypes.get(0));
        }

        existingIdVProviderNames = new StringBuilder("[").append(idVPMgtClient.getIdVProviders(null, null, currentUser)
            .stream()
            .map(provider -> "\"" + provider.getIdVProviderName() + "\"")
            .collect(Collectors.joining(",")))
          .append("]");
        claimURIs = idVPMgtClient.getAllLocalClaims();
    } catch (Exception e) {
        claimURIs = new String[0];
        existingIdVProviderNames = new StringBuilder("[]");
        infoPerIdVProvider = new ArrayList<>();
        metadataPerIdVProvider = new HashMap<>();
        String message = MessageFormat.format(resourceBundle.getString("error.loading.idvp.info"), e.getMessage());
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }
    request.setAttribute("infoPerIdVProvider", infoPerIdVProvider);
    request.setAttribute("idVProvider", idVProvider);
    request.setAttribute("claimURIs", claimURIs);
    request.setAttribute("existingIdVProviderNames", existingIdVProviderNames.toString());
    request.setAttribute("idVProviderUIMetadata", idVProviderUIMetadata);
    request.setAttribute("metadataPerIdVProvider", metadataPerIdVProvider);
%>

<script>

    let idVProviderUIMetadata = <c:out value="${idVProviderUIMetadata.toString()}" escapeXml="false"/>;
    const existingIdVProviderNames = <c:out value="${existingIdVProviderNames}" escapeXml="false"/>;

    const metadataPerIdVProvider = new Map();
    <c:forEach var="metadata" items="${metadataPerIdVProvider}">
        metadataPerIdVProvider.set(
            "<c:out value="${metadata.key}"/>",
            <c:out value="${metadata.value.toString()}" escapeXml="false"/>
        );
    </c:forEach>

    const currentConfigProperties = new Map();
    <c:forEach var="property" items="${idVProvider.idVConfigProperties}">
        currentConfigProperties.set("<c:out value='${property.name}'/>", "<c:out value='${property.value}'/>");
    </c:forEach>

    $(document).ready(() => {

        // Handle the toggling of sections when H2 elements are clicked
        $('h2.trigger').click(function () {
            if ($(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }
            $(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        });

        $('#claimAddLink').click(() => {

            let options = '<option value="">---Select Claim URI ---</option>';
            <c:forEach var="claimURI" items="${claimURIs}">
                options += '<option value="${claimURI}"><c:out value="${claimURI}"/></option>';
            </c:forEach>

            const newRow = $(generateHTMLForClaimMappingRows(options));
            $("#claimAddTable").append(newRow);

            handleClaimAddTableVisibility()
        })

        $("#idvp-type-dropdown")
            .change(() => {
                const idVProviderType = $("#idvp-type-dropdown").val();
                idVProviderUIMetadata = metadataPerIdVProvider.get(idVProviderType);
                renderConfigurationPropertySection(idVProviderUIMetadata, currentConfigProperties);
            })
            .trigger("change");
    })

    /**
     * Handles the form submission for creating or updating an IdVProvider.
     */
    const handleIdVPMgtUpdate = () => {

        const idVProviderType = $("#idvp-type-dropdown");
        idVProviderUIMetadata = metadataPerIdVProvider.get(idVProviderType.val());

        if (performValidation(existingIdVProviderNames, "${idVProvider.idVProviderName}", idVProviderUIMetadata)) {
            const claimMappingCount = $("#claimAddTable tbody tr").length;
            // Sending the number of claim mappings as a hidden element. It is used to extract the claim mappings from
            // the form data.
            let hiddenElements = `<input type="hidden" name="${IdVProviderUIConstants.KEY_CLAIM_ROW_COUNT}"
                value="\${claimMappingCount}"/>`;
            // Sending the IdVProvider id as a hidden element when updating an existing IdVProvider.
            <c:if test="${not empty idVProvider.idVProviderUuid}">
                hiddenElements += `<input type="hidden" name="${IdVProviderUIConstants.KEY_IDVP_ID}"
                    value="${idVProvider.idVProviderUuid}"/>`;
            </c:if>

            // Sending the IdVProvider type as a hidden element when updating an existing IdVProvider.
            if (idVProviderType.prop("disabled")) {
                hiddenElements += `<input type="hidden" name="${IdVProviderUIConstants.KEY_IDVP_TYPE}"
                    value="\${idVProviderType.val()}"/>`;
            }

            const form = $("#idvp-mgt-edit-form");
            form.append(hiddenElements);
            form.submit();
        }
    };

</script>

<fmt:bundle basename="<%=RESOURCE_BUNDLE%>">
    <div id="middle">
    <div id="workArea">
        <form
          id="idvp-mgt-edit-form"
          name="idvp-mgt-edit-form"
          method="post"
          action="idvp-mgt-edit-finish-ajaxprocessor.jsp?<csrf:tokenname/>=<csrf:tokenvalue/>"
          enctype="application/x-www-form-urlencoded">

            <!-- Basic Info Start -->
            <div class="sectionSeperator togglebleTitle">
                <fmt:message key='basic.info.heading'/>
            </div>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <tr>
                        <td class="leftCol-med labelField">
                            <fmt:message key='name'/>:<span class="required">*</span>
                        </td>
                        <td>
                            <input
                              id="idVPName"
                              name="idVPName"
                              type="text"
                              value="<c:out value='${idVProvider.idVProviderName}'/>"
                              autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='name.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            <fmt:message key='description'/>:
                        </td>
                        <td>
                            <input
                              id="idVPDescription"
                              name="idVPDescription"
                              type="text"
                              value="<c:out value='${idVProvider.idVProviderDescription}'/>"
                              autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='description.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            <fmt:message key='idvp.type'/>:<span class="required">*</span>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty idVProvider}">
                                    <select id="idvp-type-dropdown" class="selectField" name="idVPType" disabled>
                                        <c:forEach
                                          var="idVProviderInfo"
                                          items="${infoPerIdVProvider}"
                                          varStatus="status">
                                            <option
                                              value="${idVProviderInfo.id}"
                                                ${idVProvider.type.equals(idVProviderInfo.id) ? "selected":""}>
                                                    ${idVProviderInfo.name}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </c:when>
                                <c:otherwise>
                                    <select id="idvp-type-dropdown" name="idVPType" class="selectField">
                                        <c:forEach
                                          var="idVProviderInfo"
                                          items="${infoPerIdVProvider}"
                                          varStatus="status">
                                            <option
                                              value="${idVProviderInfo.id}"
                                                ${status.first ? "selected":""}>
                                                    ${idVProviderInfo.name}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                            <div class="sectionHelp">
                                <fmt:message key='idvp.type.help'/>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
            <!-- Basic Info End -->

            <!-- Configuration Properties Start -->
            <h2
              id="config_prop_head"
              class="sectionSeperator trigger">
                <a href="#">
                    <fmt:message key="configuration.properties.head"/>
                </a>
            </h2>
            <div
              class="toggle_container sectionSub"
              style="margin-bottom:10px;"
              id="config-property-section">
                <table id="config-property-table" class="carbonFormTable"></table>
            </div>
            <!-- Configuration Properties End -->

            <!-- Claim Config Start -->
            <h2
              id="claim_config_head"
              class="sectionSeperator trigger">
                <a href="#">
                    <fmt:message key="claim.config.head"/>
                </a>
            </h2>
            <div
              class="toggle_container sectionSub"
              style="margin-bottom:10px;"
              id="claimConfig">
                <table>
                    <tr>
                        <td class="leftCol-med labelField customClaim">
                            <fmt:message key='claimURIs'/>:
                        </td>
                        <td class="customClaim">
                            <a
                              id="claimAddLink"
                              class="icon-link"
                              style="margin-left:0;background-image:url(images/add.gif);">
                                <fmt:message key='add.claim'/>
                            </a>
                            <div style="clear:both"></div>
                            <div class="sectionHelp">
                                <fmt:message key='claimURIs.help'/>
                            </div>
                            <table
                              class="styledLeft"
                              id="claimAddTable"
                              style="display:none">
                                <thead>
                                    <tr>
                                        <th class="leftCol-big">
                                            <fmt:message key='claim.add.table.headings.external'/>
                                        </th>
                                        <th>
                                            <fmt:message key='claim.add.table.headings.wso2'/>
                                        </th>
                                        <th>
                                            <fmt:message key='actions'/>
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                <c:if
                                  test="${not empty idVProvider.claimMappings && idVProvider.claimMappings.size() > 0}">
                                    <script>
                                        $('#claimAddTable').toggle();
                                    </script>
                                    <c:forEach
                                      var="claimMapping"
                                      items="${idVProvider.claimMappings.entrySet()}"
                                      varStatus="status">
                                        <tr id="claim-row_${status.index}">
                                            <td>
                                                <input
                                                  type="text"
                                                  style=" width: 90%; "
                                                  class="external-claim"
                                                  value="<c:out value='${claimMapping.getValue()}'/>"
                                                  id="external-claim-id_${status.index}"
                                                  name="external-claim-name_${status.index}"/>
                                            </td>
                                            <td>
                                                <select
                                                  id="claim-row-id-wso2_${status.index}"
                                                  class="claim-row-wso2"
                                                  name="claim-row-name-wso2_${status.index}">
                                                    <option value=""> --- Select Claim URI --- </option>
                                                    <c:forEach var="wso2ClaimName" items="${claimURIs}">
                                                        <option
                                                          value="${wso2ClaimName}"
                                                          ${claimMapping.getKey() != null &&
                                                          claimMapping.getKey().equals(wso2ClaimName) ? "selected":""} >
                                                            <c:out value="${wso2ClaimName}"/>
                                                        </option>
                                                    </c:forEach>
                                            </td>
                                            <td>
                                                <a
                                                  title="<fmt:message key='delete.claim'/>"
                                                  onclick="deleteClaimRow(${status.index});return false;"
                                                  href="#"
                                                  class="icon-link"
                                                  style="background-image: url(images/delete.gif)">
                                                    <fmt:message key='delete'/>
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:if>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </table>
            </div>
            <!-- Claim Config End -->
        </form>
    </div>
    <!-- sectionSub Div -->
    <div class="buttonRow">
        <input
          type="button"
          value="<fmt:message key='${idVProvider != null ? "update" : "register"}'/>"
          onclick="handleIdVPMgtUpdate();"
        />
        <input type="button" value="<fmt:message key='cancel'/>" onclick="handleIdVPMgtCancel();"/>
    </div>

    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/joi@17.9.2/dist/joi-browser.min.js"></script>
    <script type="text/javascript" src="js/idvp_mgt_edit.js"></script>

</fmt:bundle>
