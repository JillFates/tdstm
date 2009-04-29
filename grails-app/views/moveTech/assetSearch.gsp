<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Home</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
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
<div class="colum_techlogin">
<div style="float: left; width: 100%; margin-left: 20px;"><g:link
	params='["bundle":bundle,"team":team,"location":location,"project":project]'
	style="height:26px; width:64px; float:left; margin:auto 0px;">
	<img src="${createLinkTo(dir:'images',file:'home.png')}" border="0" />
</g:link> <g:link action="assetTask"
	params='["bundle":bundle,"team":team,"location":location,"project":project]'
	style="height:26px; width:64px; float:left; margin:auto 0px;">
	<img src="${createLinkTo(dir:'images',file:'my_task.png')}" border="0" />
</g:link> <a href="#"
	style="height: 26px; width: 64px; float: left; margin: auto 0px;"><img
	src="${createLinkTo(dir:'images',file:'asset_h.png')}" border="0" /></a></div>
<div class="w_techlog" style="overflow-y: scroll; overflow-x: none;">

<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">
	<input name="bundle" type="hidden" value="${bundle}" />
	<input name="team" type="hidden" value="${team}" />
	<input name="location" type="hidden" value="${location}" />
	<input name="project" type="hidden" value="${project}" />



	<div
		style="float: left; margin: 5px; width: 30%; border: 1px solid #5F9FCF;">
	<g:link controller="moveTech" action="signOut" style="color: #328714">Log out</g:link>
	</div>

	<div style="float: left;"><input type="text" name="assetId"
		id="assetId" size="18" /> <input type="submit" name="search"
		value="Search" /></div>
</g:form>


<div>&nbsp;</div>

<div>
<div>

<p>&nbsp;</p>
<p>&nbsp;</p>
<p><strong>Device Description</strong></p>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> <g:if test="${projMap}">
	<p>
	${projMap?.asset?.assetName}-${projMap?.asset?.model}-${projMap?.asset?.serialNumber}-${stateVal}</p>
</g:if></div>
<div>
<table>
	<p>&nbsp;</p>
	<tr>
		<td><strong>Instructions</strong></td>
	</tr>
	<g:each status="i" in="${assetCommt}" var="comments">
		<tr>
			<td>${comments.comment}</td>
			<td><g:checkBox name="myCheckbox" value="${false}" /></td>
		</tr>
	</g:each>
	<tr>
		<td><g:actionSubmit value="Place on HOLD"
			onclick="return confirm('Are you sure???')" /></td>
		<td><g:actionSubmit value="Start Unracking"
			onclick="return confirm('Are you sure???')" /></td>
	</tr>
</table>
</div>

</div>
</div>

</div>
</div>





</body>

</html>
