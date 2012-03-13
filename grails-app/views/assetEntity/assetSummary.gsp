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
	     <th style="text-align:center;">Assets </th>
	     <th style="text-align:center;">Applications</th>
	     <th style="text-align:center;">Database</th>
	     <th style="text-align:center;">files</th>
	   </tr>
    </thead>
   
   <tbody>
   <g:each in="${assetSummaryList}" var="assetSummary" status="i">
         <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	          <td>${assetSummary.name}</td>
	          <td  style="text-align:right;">${assetSummary.assetCount}</td>
	          <td  style="text-align:right;">${assetSummary.applicationCount}</td>
	          <td  style="text-align:right;">${assetSummary.databaseCount}</td>
	          <td  style="text-align:right;">${assetSummary.filesCount}</td>
         </tr>    
    </g:each>
         <tr class='odd'>
	          <td  style="text-align:right;"><i>UnAssigned</i></td>
	          <td  style="text-align:right;"><i>${unassignedAssetCount}</i></td>
	          <td  style="text-align:right;"><i>${unassignedAppCount}</i></td>
	          <td  style="text-align:right;"><i>${unassignedDBCount}</i></td>
	          <td  style="text-align:right;"><i>${unassignedFilesCount}</i></td>
         </tr> 
        <tr class='odd'>
	          <td  style="text-align:right;"><b>Total</b></td>
	          <td  style="text-align:right;"><b>${totalAsset}</b></td>
	          <td  style="text-align:right;"><b>${totalApplication}</b></td>
	          <td  style="text-align:right;"><b>${totalDatabase}</b></td>
	          <td  style="text-align:right;"><b>${totalFiles}</b></td>
         </tr> 
    </tbody>
    
  </table>
  
  </div>
</body>
</html>