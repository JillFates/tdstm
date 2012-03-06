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
  <table >
     <thead>
      <tr>
	     <th>Bundle</th>
	     <th>Assets </th>
	     <th>Applications</th>
	     <th>Database</th>
	     <th>files</th>
	   </tr>
    </thead>
   
   <tbody>
   <g:each in="${assetSummaryList}" var="assetSummary" status="i">
         <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	          <td>${assetSummary.name}</td>
	          <td>${assetSummary.assetCount}</td>
	          <td>${assetSummary.applicationCount}</td>
	          <td>${assetSummary.databaseCount}</td>
	          <td>${assetSummary.filesCount}</td>
         </tr>    
    </g:each>
         <tr class='odd'>
	          <td><b>UnAssigned</b></td>
	          <td><b>${unassignedAssetCount}</b></td>
	          <td><b>${unassignedAppCount}</b></td>
	          <td><b>${unassignedDBCount}</b></td>
	          <td><b>${unassignedFilesCount}</b></td>
         </tr> 
        <tr class='odd'>
	          <td><b>Total</b></td>
	          <td><b>${totalAsset}</b></td>
	          <td><b>${totalApplication}</b></td>
	          <td><b>${totalDatabase}</b></td>
	          <td><b>${totalFiles}</b></td>
         </tr> 
    </tbody>
    
  </table>
  
  </div>
</body>
</html>