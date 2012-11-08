<html>
<head>
</head>
<body>
 <div class="body">
 <table style="width: 400px">
 <th colspan="2"> SUMMARY for Move-Event : ${moveEventInstance.name}</th>
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
	 <td> Pre-move tasks :</td>
	 <td>${reportService.size()}</td>
 </tr>
 <tr><td>
     <input type="checkbox" id="version" name="version" checked="checked" /> <label for="version"> <span id="runBook" style="vertical-align: text-bottom;"><b>Update version number</b></span></label>
     </td>
 </tr>
 
 <tr><td class="buttonR">
				<input type="submit" class="submit"  value="Generate Runbook"  id="generateRunbookId"  onclick=" $('#moveEventForm').submit();"/>
	</td>
</tr>
 
 </table>
 </div>
 
</body>

</html>