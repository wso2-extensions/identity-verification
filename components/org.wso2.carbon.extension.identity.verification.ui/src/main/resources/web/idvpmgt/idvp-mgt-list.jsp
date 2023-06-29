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

<%@ page isELIgnored="false" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.ExtensionMgtServiceClient" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.ExtensionMgtServiceClientImpl" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClient" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.client.IdVProviderMgtServiceClientImpl" %>
<%@ page import="org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page
  import="static org.wso2.carbon.extension.identity.verification.ui.util.IdVProviderUIConstants.RESOURCE_BUNDLE" %>
<%@ page import="static org.wso2.carbon.CarbonConstants.LOGGED_USER" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<carbon:breadcrumb
        label="identity.verification.providers"
        resourceBundle="org.wso2.carbon.extension.identity.verification.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"
/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
   ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, request.getLocale());
   List<IdVProvider> idVProvidersToDisplay = new ArrayList<>();
   int pageNumber = 0;
   String pageNumberStr = request.getParameter(IdVProviderUIConstants.PAGE_NUMBER);

   if (StringUtils.isNotBlank(pageNumberStr)) {
      try {
         if (Integer.parseInt(pageNumberStr) > 0) {
            pageNumber = Integer.parseInt(pageNumberStr);
         }
      } catch (NumberFormatException ignored) {
         // This exception can be safely ignored since we have already set the default value
      }
   }

   int pageCount = 0;
   try {
      ExtensionMgtServiceClient extensionMgtClient = ExtensionMgtServiceClientImpl.getInstance();
      List<ExtensionInfo> infoPerIdVProvider = extensionMgtClient.getExtensionInfoOnIdVProviderTypes();

      if (infoPerIdVProvider.isEmpty()) {
         // Redirect to the placeholder page if there are no IdV Providers templates configured
         response.sendRedirect("idvp-mgt-not-configured.jsp");
      }
      int resultsPerPage = IdentityUtil.getDefaultItemsPerPage();
      IdVProviderMgtServiceClient client = IdVProviderMgtServiceClientImpl.getInstance();
      String currentUser = (String) session.getAttribute(LOGGED_USER);
      int idVProviderCount = client.getIdVProviderCount(currentUser);
      pageCount = (int) Math.ceil((double) idVProviderCount / resultsPerPage);

      if (idVProviderCount > 0) {
         int offset = idVProviderCount > resultsPerPage ? pageNumber * resultsPerPage : 0;
         idVProvidersToDisplay = client.getIdVProviders(resultsPerPage, offset, currentUser);
      }
   } catch (Exception e) {
      String message = MessageFormat.format(resourceBundle.getString("error.loading.idvps"), e.getMessage());
      CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
   }

   request.setAttribute("idVProvidersToDisplay", idVProvidersToDisplay);
   request.setAttribute("pageNumber", pageNumber);
   request.setAttribute("pageCount", pageCount);
%>

<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>

<script type="text/javascript">

   const IDVP_DELETE_URL = "idvp-mgt-delete-finish-ajaxprocessor.jsp";
   const IDVP_ENABLE_URL = "idvp-mgt-enable-finish-ajaxprocessor.jsp";
   const IDVP_EDIT_URL = "idvp-mgt-edit.jsp";
   const TEXT_HTML = "text/html";
   const STATUS_SUCCESS = "success";

   const editIdVP = (idVPId) => {
      location.href = IDVP_EDIT_URL + "?" + "${IdVProviderUIConstants.KEY_IDVP_ID}" + "=" + encodeURIComponent(idVPId);
   };

   const deleteIdVPById = (id, name, pageNumber) => {

      const doDelete = () => {
         $.ajax({
            type: "${IdVProviderUIConstants.HTTP_POST}",
            url: IDVP_DELETE_URL,
            headers: {
               Accept: TEXT_HTML
            },
            data: "${IdVProviderUIConstants.KEY_IDVP_ID}"+ "=" + encodeURIComponent(id),
            async: false,
            success: (responseText, status) => {
               if (status === STATUS_SUCCESS) {
                  location.assign("idvp-mgt-list.jsp?pageNumber=" + encodeURIComponent(pageNumber.toString()) +
                          "&region=region1&item=idp_list");
               }
            }
         });
      };

      const message = 'Are you sure you want to delete "' + name + '" Identity Verification Provider?';
      CARBON.showConfirmationDialog(message, doDelete, null, null)
   };

   const enableOrDisableIdVP = (id, status) => {

      $.ajax({
         type: "${IdVProviderUIConstants.HTTP_POST}",
         url: IDVP_ENABLE_URL,
         headers: {
            Accept: TEXT_HTML
         },
         data: "${IdVProviderUIConstants.KEY_IDVP_ID}" + "=" + encodeURIComponent(id) + "&"
                 + "${IdVProviderUIConstants.KEY_ENABLE}" + "=" + status,
         async: false,
         success: (responseText, status) => {
            if (status === STATUS_SUCCESS) {
               location.assign("idvp-mgt-list.jsp");
            }
         }
      });
   };
</script>
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
                        <thead>
                        <tr>
                           <th class="leftCol-med">
                              <fmt:message key='registered.idvps'/>
                           </th>
                           <th class="leftCol-big">
                              <fmt:message key='description'/>
                           </th>
                           <th style="width: 30% ;">
                              <fmt:message key='actions'/>
                           </th>
                        </tr>
                        </thead>
                        <c:choose>
                           <c:when test="${ idVProvidersToDisplay.size() > 0}">
                              <tbody>
                              <c:forEach var="idvp" items="${idVProvidersToDisplay}">
                                 <tr>
                                    <td>
                                       <c:out value="${idvp.idVProviderName}"/>
                                    </td>
                                    <td>
                                       <c:out value="${idvp.idVProviderDescription}" default=""/>
                                    </td>
                                    <td style="width: 100px; white-space: nowrap;">
                                       <c:choose>
                                          <c:when test="${idvp.enabled}">
                                             <a title="<fmt:message key='disable.policy'/>"
                                                onclick="enableOrDisableIdVP('<c:out
                                                  value="${idvp.idVProviderUuid}"/>', 'false');return false;"
                                                href="#"
                                                style="background-image: url(images/disable.gif);"
                                                class="icon-link">
                                                <fmt:message key='disable.policy'/>
                                             </a>
                                          </c:when>
                                          <c:otherwise>
                                             <a title="<fmt:message key='enable.policy'/>"
                                                onclick="enableOrDisableIdVP('<c:out
                                                  value="${idvp.idVProviderUuid}"/>', 'true');return false;"
                                                href="#" style="background-image: url(images/enable.gif);"
                                                class="icon-link">
                                                <fmt:message key='enable.policy'/>
                                             </a>
                                          </c:otherwise>
                                       </c:choose>
                                       <a title="<fmt:message key='edit.idvp.info'/>"
                                          onclick="editIdVP('<c:out value="${idvp.idVProviderUuid}"/>');return false;"
                                          style="background-image: url(images/edit.gif);" class="icon-link">
                                          <fmt:message key='edit'/>
                                       </a>
                                       <a title="<fmt:message key='delete'/>"
                                          onclick="deleteIdVPById('<c:out value="${idvp.idVProviderUuid}"/>','<c:out
                                            value="${idvp.idVProviderName}"/>','${pageNumber}');return false;"
                                          href="#"
                                          class="icon-link"
                                          style="background-image: url(images/delete.gif)">
                                          <fmt:message key='delete'/>
                                       </a>
                                    </td>
                                 </tr>
                                 </tbody>
                              </c:forEach>
                           </c:when>
                           <c:otherwise>
                              <tbody>
                              <tr>
                                 <td colspan="3">
                                    <i> <fmt:message key="no.idvp"/> </i>
                                 </td>
                              </tr>
                              </tbody>
                           </c:otherwise>
                        </c:choose>
                     </table>
                  </td>
               </tr>
               </tbody>
            </table>
            <br/>
            <carbon:paginator
              pageNumber="${pageNumber}"
              numberOfPages="${pageCount}"
              page="idvp-mgt-list.jsp"
              pageNumberParameterName="pageNumber"
              resourceBundle="${IdVProviderUIConstants.RESOURCE_BUNDLE}"
              parameters="region=region1&item=idp_list"
              prevKey="navigation.prev"
              nextKey="navigation.next"
            />
            <br/>
         </div>
      </div>
   </div>
</fmt:bundle>
