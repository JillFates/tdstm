<html>
<head>
<title>Walkthru&gt; Start</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'walkThrough.css')}" />
<script type="text/javascript">
/*-----------------------------------------------------------------
	function to load the Bundles for selected Project via AJAX
	@author : Lokanath Reddy
	@params : bundle list object as JSON
*-----------------------------------------------------------------*/
function appendBundles() {
	<%--var moveBundleList = eval('(' + e.responseText + ')')
	var moveBundleObj = document.startMenuForm.moveBundle
	moveBundleObj.innerHTML = ""
	if(moveBundleList){
		var length = moveBundleList.length
		for(i = 0; i < length; i++){
			var moveBundle = moveBundleList[i]
			var option = document.createElement("option");
			option.value = moveBundle.id
			option.innerHTML = moveBundle.name
			if('${currBundle}' == moveBundle.id){
				option.selected = "selected"
			}
			moveBundleObj.appendChild(option);
		}
	} --%>
	document.startMenuForm.action = "startMenu";
	document.startMenuForm.submit();
}
</script>
</head>
<body>
	<div class="qvga_border">
		<div class="title">Walkthru&gt; Start</div>
		<div class="input_area">
			<div style="float:left;"><g:link action="mainMenu" class="button">Main Menu</g:link> </div>
			<div style="float:right;"><g:link controller="auth" action="signOut" class="button">Logout</g:link></div>
			<br class="clear"/>
			<g:form method='post' action="selectRack" name="startMenuForm">
				<table width="100%" border="0" cellpadding="1">
				<tr >
				   	<td class="label" >Project:</td>
				   	<td class="field">
				   		<g:select from ="${Project.list()}" value ="${currProj}" optionKey ="id" optionValue="name" 
				   				id="projectId" name="project" onchange="appendBundles()"/>
				   </td>
				</tr>
				
				<tr>
				   <td class="label">Bundle:</td>
				   <td class="field">
				   <g:select from ="${moveBundlesList}" value ="${currBundle}" optionKey ="id" optionValue="name" 
				   				name="moveBundle" id="moveBundleId" class="select" onchange="appendBundles()"/>
				   </td>
				</tr>
				
				<tr>
				   <td class="label">Location:</td>
				   <td class="field">
				   		<g:select from ="${locationList}" value ="${currLocation}" name="location" id="locationId" class="select"/>
				   </td>
				</tr>
				
				<tr>
				   <td class="label">Audit Type:</td>
				   <td class="field">
				      <input type="radio" name="auditType" id="auditTypeSrc" checked value="source"><label for="auditTypeSrc" class="radio_label">Source walk-thru</label>
				      <br/>
				      <input type="radio" name="auditType" id="auditTypeTgt" disabled="disabled" value="target"><label for="auditTypeTgt" class="radio_label">Target walk-thru</label>
				      <br/>
				      <input type="radio" name="auditType" id="auditTypeQC" disabled="disabled" value="QC"><label for="auditTypeQC" class="radio_label">Post QC walk-thru</label>
				   </td>
				</tr>
				
				<tr><td>&nbsp;</td></tr>
				
				<tr style="margin-top:10px;">
				   <td colspan="2" align="center">
				      <a class="button big" href="#" onclick="document.startMenuForm.submit();">Continue</a>
				   </td>
				</tr>
				
				</table>
			</g:form>
		</div>
	</div>
</body>
</html>
		
