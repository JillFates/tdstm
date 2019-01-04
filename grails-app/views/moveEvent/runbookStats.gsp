<html>
<head>
</head>
<body>
 <div class="body">
 <table style="width: 400px">
 <th colspan="2"> SUMMARY for Event : ${moveEventInstance.name}</th>
 <tr>
	 <td> Bundles :</td>
	 <td>${bundles?.size() }</td>
 </tr>
  <tr >
	 <td> Applications : </td>
	 <td>${applcationAssigned}</td>
 </tr>
  <tr>
	 <td> Servers :</td>
	 <td>${assetCount }</td>
 </tr>
  <tr>
	 <td> Databases :</td>
	 <td>${databaseCount}</td>
 </tr>
  <tr>
	 <td> Storage :	</td>
	 <td>${fileCount }</td>
 </tr>
  <tr>
	 <td> Other :</td>
	 <td>${otherAssetCount}</td>
 </tr>
  <tr>
	 <td> Pre-event tasks :</td>
	 <td>${preMoveSize}</td>
 </tr>
 <tr>
	 <td> Schedule tasks :</td>
	 <td>${sheduleSize}</td>
 </tr>
 <tr>
	 <td>Post-event tasks:</td>
	 <td>${postMoveSize}</td>
 </tr>
 <tr><td>
     <input type="checkbox" id="version" name="version" checked="checked" /> <label for="version"> <span id="runBook" style="vertical-align: text-bottom;"><b>Update version number</b></span></label>
     </td>
 </tr>
 
 <tr><td>
	 <button type="button" class="btn btn-default" id="generateRunbookId"  onclick=" $('#moveEventForm').submit();">
		 Export Runbook (Excel)
		 <span class="exportIcon glyphicon glyphicon-download" aria-hidden="true"></span>
	 </button>
 </td>
</tr>
 
 </table>
 </div>
 
</body>

</html>
