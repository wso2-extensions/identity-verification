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

<%@page import="org.owasp.encoder.Encode" %>

<%@ page import="org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClientImpl" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants" %>
<%@ page import="java.util.Set" %>
<%@ page import="static org.wso2.carbon.CarbonConstants.LOGGED_USER" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtClientException" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page
  import="static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.RESOURCE_BUNDLE" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>
<link href="css/idvpmgt.css" rel="stylesheet" type="text/css" media="all"/>

<%
    String idVPId = request.getParameter(IdVProviderUIConstants.KEY_IDVP_ID);

    String idVPName = "";
    String description = "";
    Map<String, String> claimMappings = null;
    String[] claimURIs;
    IdVProvider idVProvider = null;
    IdVProviderMgtServiceClient client = IdVProviderMgtServiceClientImpl.getInstance();
    String currentUser = (String) session.getAttribute(LOGGED_USER);
    ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, request.getLocale());

    try {
        if (StringUtils.isNotBlank(idVPId)) {
            idVProvider = client.getIdVProviderById(idVPId, currentUser);
            idVPName = idVProvider.getIdVProviderName();
            description = idVProvider.getIdVProviderDescription();
            claimMappings = idVProvider.getClaimMappings();
        }

        claimURIs = client.getAllLocalClaims();
    } catch (Exception e) {
        claimURIs = new String[0];
        String message = MessageFormat.format(resourceBundle.getString("error.loading.idvp.info"), e.getMessage());
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
    }

%>

<script>

    let claimRowId = <%= claimMappings != null ? (claimMappings.size() - 1) : -1 %>;

    $(document).ready(() => {

        $('#claimConfig').hide();

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

            claimRowId++;
            let options = '<option value="">---Select Claim URI ---</option>';

            <%
                for(String claimURI : claimURIs) {
            %>
            options += '<option value="' + "<%=claimURI%>" + '">' + "<%=claimURI%>" + '</option>';
            <%
                }
            %>

            const newRow = $(generateHTMLForClaimMappingRows(claimRowId, options));
            $("#claimAddTable").append(newRow);

            handleClaimAddTableVisibility()
        })

    })

    const idvpMgtUpdate = () => {
        //TODO: Implement the update logic
    };

</script>

<fmt:bundle basename="<%=RESOURCE_BUNDLE%>">
    <div id="middle">
    <div id="workArea">
        <form
          id="idp-mgt-edit-form" name="idp-mgt-edit-form" method="post"
          action="idvp-mgt-edit-finish-ajaxprocessor.jsp?<csrf:tokenname/>=<csrf:tokenvalue/>"
          enctype="multipart/form-data">

                <%-- Basic Info Start --%>
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
                              value="<%=Encode.forHtmlAttribute(idVPName)%>"
                              autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='name.help'/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">
                            <fmt:message key='description'/>
                        </td>
                        <td>
                            <input
                              id="idVPDescription"
                              name="idVPDescription"
                              type="text"
                              value="<%=Encode.forHtmlAttribute(description)%>"
                              autofocus/>
                            <div class="sectionHelp">
                                <fmt:message key='description.help'/>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
                <%-- Basic Info End --%>

                <%-- Claim Config Start --%>
            <h2
              id="claim_config_head"
              class="sectionSeperator trigger active">
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
                                        <fmt:message key='idvp.claim'/>
                                    </th>
                                    <th>
                                        <fmt:message key='wso2.claim'/>
                                    </th>
                                    <th>
                                        <fmt:message key='actions'/>
                                    </th>
                                </tr>
                                </thead>
                                <tbody>
                                <%
                                    if (claimMappings != null && claimMappings.size() > 0) {
                                %>
                                <script>
                                    $('#claimAddTable').toggle();
                                </script>
                                <%
                                    int i = 0;
                                    for (Map.Entry<String, String> claimMapping : claimMappings.entrySet()) {
                                %>
                                <tr id="claim-row_<%=i%>">
                                    <td>
                                        <input
                                          type="text"
                                          style=" width: 90%; "
                                          class="external-claim"
                                          value="<%=Encode.forHtmlAttribute(claimMapping.getValue())%>"
                                          id="external-claim-id_<%=i%>"
                                          name="external-claim-name_<%=i%>"/>
                                    </td>
                                    <td>
                                        <select
                                          id="claim-row-id_wso2_<%=i%>"
                                          class="claim-row-wso2"
                                          name="claim-row-name-wso2_<%=i%>">
                                            <option value="">
                                                --- Select Claim URI ---
                                            </option>
                                            <%
                                               for(String wso2ClaimName : claimURIs) {
                                            %>
                                            <option
                                              <%
                                                  if (claimMapping.getKey() != null && claimMapping.getKey().equals(wso2ClaimName)) {
                                              %>
                                              selected="selected"
                                              <%
                                                  }
                                                  String encodedWSO2Claim = Encode.forHtmlAttribute(wso2ClaimName);
                                              %>
                                              value="<%= encodedWSO2Claim %>">
                                                <%= encodedWSO2Claim %>
                                            </option>
                                            <% } %>
                                    </td>

                                    <td>
                                        <a
                                          title="<fmt:message key='delete.claim'/>"
                                          onclick="deleteClaimRow(<%=i%>);return false;"
                                          href="#"
                                          class="icon-link"
                                          style="background-image: url(images/delete.gif)">
                                            <fmt:message key='delete'/>
                                        </a>
                                    </td>
                                </tr>

                                <%
                                        i++;
                                    }
                                %>
                                <% } %>

                                </tbody>

                            </table>
                        </td>
                    </tr>

                </table>
            </div>
                <%-- Claim Config End --%>
        </form>
    </div>
    <!-- sectionSub Div -->
    <div class="buttonRow">
        <input
          type="button"
          value="<fmt:message key='<%= idVProvider != null ? "update" : "register"%>'/>"
          onclick="idvpMgtUpdate();"
        />
        <input type="button" value="<fmt:message key='cancel'/>" onclick="idpMgtCancel();"/>
    </div>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="js/idp_mgt_edit.js"></script>
    <script type="text/javascript" src="js/idvp_mgt_edit.js"></script>

</fmt:bundle>
