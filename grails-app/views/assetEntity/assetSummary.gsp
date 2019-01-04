<%@ page contentType="text/html;charset=ISO-8859-1" %>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.UserPreferenceEnum"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<meta name="layout" content="topNav"/>
<title>Asset Summary Table</title>
</head>
<body>
<tds:subHeader title="Asset Summary Table" crumbs="['Assets', 'Summary']" justPlanningOptIn="true" />
  <div class="body" style="width: 900px">
  <table class="asset-summary-table" style="width:700px;">
     <thead>
      <tr>
	     <th>Bundle</th>
	     <th style="text-align:center;"><g:link controller="application" action="list">Applications</g:link> </th>
	     <th style="text-align:center;"><g:link controller="assetEntity" action="list" params="[filter:'server']">Servers</g:link></th>
	     <th style="text-align:center;"><g:link controller="assetEntity" action="list" params="[filter:'physical']">Physical Devices</g:link></th>
	     <th style="text-align:center;"><g:link controller="database" action="list">Database</g:link></th>
	     <th style="text-align:center;"><g:link controller="files" action="list">Logical Storage</g:link></th>
	   </tr>
    </thead>

   <tbody>
   <g:each in="${assetSummaryList}" var="assetSummary" status="i">
         <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	          <td>
				<tds:hasPermission permission="${Permission.BundleView}">
					<a href=/moveBundle/show/${assetSummary.id}>${assetSummary.name}</a>
				</tds:hasPermission>
			  </td>
	          <g:if test="${assetSummary.applicationCount>0}">
	            <td  style="text-align:right;"><g:link controller="application" action="list" params='[filter:"application", moveBundleId:"${assetSummary.id}"]'>${assetSummary.applicationCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
	          <g:if test="${assetSummary.assetCount>0}">
	            <td  style="text-align:right;"><g:link controller="assetEntity" action="list" params='[filter:"server", moveBundleId:"${assetSummary.id}"]' >${assetSummary.assetCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
	          <g:if test="${assetSummary.physicalCount>0}">
	            <td  style="text-align:right;"><g:link controller="assetEntity" action="list" params='[filter:"physical", moveBundleId:"${assetSummary.id}"]' >${assetSummary.physicalCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
	          <g:if test="${assetSummary.databaseCount>0}">
	            <td  style="text-align:right;"><g:link controller="database" action="list" params='[filter:"db", moveBundleId:"${assetSummary.id}"]'>${assetSummary.databaseCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
	          <g:if test="${assetSummary.filesCount>0}">
	            <td  style="text-align:right;"><g:link controller="files" action="list" params='[filter:"storage", moveBundleId:"${assetSummary.id}"]'>${assetSummary.filesCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
         </tr>
    </g:each>
        <tr class='odd'>
	          <td  style="text-align:right;"><b>Total</b></td>
	          <td  style="text-align:right;"><b><g:link controller="application" action="list">${totalApplication}</g:link></b></td>
	          <td  style="text-align:right;"><b><g:link controller="assetEntity" action="list" params="[filter:'server']">${totalAsset}</g:link></b></td>
	          <td  style="text-align:right;"><b><g:link controller="assetEntity" action="list" params="[filter:'physical']">${totalPhysical}</g:link></b></td>
	          <td  style="text-align:right;"><b><g:link controller="database" action="list">${totalDatabase}</g:link></b></td>
	          <td  style="text-align:right;"><b><g:link controller="files" action="list">${totalFiles}</g:link></b></td>
         </tr>
    </tbody>

  </table>

  </div>
	<script>
		$(document).load(function() {
            $(".menu-parent-assets-summary-table").addClass('active');
            $(".menu-parent-assets").addClass('active');
		});
        function toggleJustPlanning(input) {
            var isChecked = $(input).attr('checked') === 'checked';
            jQuery.ajax({
                url: tdsCommon.createAppURL('/ws/user/preference'),
                data: { 'value': isChecked, 'code': '${UserPreferenceEnum.ASSET_JUST_PLANNING.name()}' },
                type: 'POST',
                success: function (data) {
                    window.location.reload();
                }
            });
        }
	</script>
</body>
</html>
