<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Home</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="shortcut icon"
	href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:javascript library="application" />

</head>
<body>

<div id="serverInfoDialog" title="Server Info"></div>
<div id="spinner" class="spinner" style="display: none;"><img
	src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="mainbody" style="width: 100%;">
<div class="colum_techlogin" style="float:left;">
<div style="float: left; width: 100%; margin-left: 20px;"><g:link
	params='["bundle":bundle,"team":team,"location":location,"project":project]'
	style="height:26px; width:64px; float:left; margin:auto 0px;">
	<img src="${createLinkTo(dir:'images',file:'home.png')}" border="0" />
</g:link> <g:link action="assetTask"
	params='["bundle":bundle,"team":team,"location":location,"project":project,"tab":"Todo"]'
	style="height:26px; width:64px; float:left; margin:auto 0px;">
	<img style="background-color: #FFFF00;" src="${createLinkTo(dir:'images',file:'my_task.png')}" border="0" /></g:link>
<img
	src="${createLinkTo(dir:'images',file:'asset_h.png')}" border="0" /></div>
<div class="w_techlog" style="overflow-y: scroll; overflow-x: none;">


<div>&nbsp;</div>

<div style="float:left; width:100%; margin:5px 0; ">
<table style="border:0px;" >


 <g:if test="${flash.message}">
	<div style="color: red;"><ul><li>${flash.message}</li></ul></div>
</g:if> 		
<g:if test="${projMap}">
<tr><td>Asset:</td><td> ${projMap?.asset?.assetName}</td></tr>
<tr><td>Model:</td><td> ${projMap?.asset?.model}</td></tr>
<tr><td>Rack/Pos:</td><td> <g:if test="${location == 's'}">${projMap?.asset?.sourceRack}/${projMap?.asset?.sourceRackPosition}</g:if><g:else test="${location == 't'}">${projMap?.asset?.targetRack}/${projMap?.asset?.targetRackPosition}</g:else> </td></tr>
	
</g:if>
	
	<tr>
		<td><strong>Instructions:</strong></td>
	</tr>
	<g:each status="i" in="${assetCommt}" var="comments">
		<tr>
			<td>${comments.comment}</td>
			<td><g:checkBox name="myCheckbox" value="${false}" /></td>
		</tr>
	</g:each>
	<tr>
		<td>&nbsp;</td>
		<td><g:actionSubmit value="Start Unracking"
			onclick="#" /></td>
	</tr>
	

	<table>	
		
		<tr><td><g:actionSubmit  value="Enter Note..."
			onclick="#" /></td> </tr>
			<tr>
		<td width="60%">&nbsp;</td>		
		<td><g:actionSubmit value="Place on HOLD"
			onclick="return confirm('Are you sure???')" /></td>
	</tr>
	</table>



</table>
</div>
</div>

</div>
</div>





</body>

</html>
