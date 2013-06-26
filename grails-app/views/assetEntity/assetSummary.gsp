<%@ page contentType="text/html;charset=ISO-8859-1" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<meta name="layout" content="projectHeader"/>
<title>Asset Summary</title>
</head>
<body>
  <div class="body" style="width: 900px">
  <div>
  <h1>ASSET SUMMARY</h1>
  </div>
  <table style="width:700px;">
     <thead>
      <tr>
	     <th>Bundle</th>
	     <th style="text-align:center;"><g:link controller="assetEntity" action="list">Assets</g:link> </th>
	     <th style="text-align:center;"><g:link controller="application" action="list">Applications</g:link></th>
	     <th style="text-align:center;"><g:link controller="database" action="list">Database</g:link></th>
	     <th style="text-align:center;"><g:link controller="files" action="list">Storage</g:link></th>
	   </tr>
    </thead>
   
   <tbody>
   <g:each in="${assetSummaryList}" var="assetSummary" status="i">
         <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	          <td>${assetSummary.name}</td>
	          <g:if test="${assetSummary.applicationCount>0}">
	            <td  style="text-align:right;"><g:link controller="application" action="list" params='[moveBundleId:"${assetSummary.id}"]'>${assetSummary.applicationCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
	          <g:if test="${assetSummary.assetCount>0}">
	            <td  style="text-align:right;"><g:link controller="assetEntity" action="list" params='[moveBundleId:"${assetSummary.id}"]' >${assetSummary.assetCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
	          <g:if test="${assetSummary.databaseCount>0}">
	            <td  style="text-align:right;"><g:link controller="database" action="list" params='[moveBundleId:"${assetSummary.id}"]'>${assetSummary.databaseCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
	          <g:if test="${assetSummary.filesCount>0}">
	            <td  style="text-align:right;"><g:link controller="files" action="list" params='[moveBundleId:"${assetSummary.id}"]'>${assetSummary.filesCount}</g:link></td>
	          </g:if>
	          <g:else>
	           <td  style="text-align:right;"></td>
	          </g:else>
         </tr>    
    </g:each>
         <tr class='odd'>
	          <td  style="text-align:right;"><i>UnAssigned</i></td>
	          <g:if test="${unassignedAssetCount>0}">
	            <td  style="text-align:right;"><g:link controller="assetEntity" action="list" params='[moveBundleId:"unAssigned"]' >${unassignedAssetCount}</g:link></td>
	          </g:if>
	          <g:else>
	              <td  style="text-align:right;"></td>
	          </g:else>
	          <g:if test="${unassignedAppCount>0}">
	            <td  style="text-align:right;"><g:link controller="application" action="list" params='[moveBundleId:"unAssigned"]'>${unassignedAppCount}</g:link></td>
	          </g:if>
	         <g:else>
	              <td  style="text-align:right;"></td>
	          </g:else>
	           <g:if test="${unassignedAppCount>0}">
	            <td  style="text-align:right;"><g:link controller="database" action="list" params='[moveBundleId:"unAssigned"]'>${unassignedDBCount}</g:link></td>
	          </g:if>
	         <g:else>
	              <td  style="text-align:right;"></td>
	          </g:else>
	           <g:if test="${unassignedAppCount>0}">
	            <td  style="text-align:right;"><g:link controller="files" action="list" params='[moveBundleId:"unAssigned"]'>${unassignedFilesCount}</g:link></td>
	          </g:if>
	         <g:else>
	              <td  style="text-align:right;"></td>
	          </g:else>
         </tr> 
        <tr class='odd'>
	          <td  style="text-align:right;"><b>Total</b></td>
	          <td  style="text-align:right;"><b><g:link controller="assetEntity" action="list">${totalAsset}</g:link></b></td>
	          <td  style="text-align:right;"><b><g:link controller="application" action="list">${totalApplication}</g:link></b></td>
	          <td  style="text-align:right;"><b><g:link controller="database" action="list">${totalDatabase}</g:link></b></td>
	          <td  style="text-align:right;"><b><g:link controller="files" action="list">${totalFiles}</g:link></b></td>
         </tr> 
    </tbody>

  </table>

  </div>
</body>
</html>