<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="companyHeader" />
    <title>Model Template</title>
    <g:javascript src="drag_drop.js" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
  </head>
  <body>
<div class="body">
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list">Model List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Model</g:link></span>
        </div>
<g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
<div style="border: 0px;margin-top: 5px;">
<fieldset>
<legend><b>Show Model Template</b></legend>
<div style="margin-left: 10px;margin-right: 10px; float: left;width: auto;">
<table style="border: 0px;">
	<tbody>
		<tr>
			<td>Manufacturer:</td>
			<td>${modelInstance?.manufacturer?.name}</td>
		</tr>
		<tr>
			<td>Model Name:</td>
			<td>${modelInstance?.modelName}</td>
		</tr>
		<tr>
			<td>AKA:</td>
			<td>${modelInstance?.aka}</td>
		</tr>
		<tr>
			<td>Asset Type:</td>
			<td>${modelInstance?.assetType}</td>
		</tr>
		<tr>
			<td>Usize:</td>
			<td>
				${modelInstance?.usize}
			</td>
		</tr>
		<tr>
			<td>Power (typical):</td>
			<td>${modelInstance?.powerUse}&nbsp;Watts</td>
		</tr>
		<tr>
			<td>Front image:</label></td>
        	<td><img src="${createLink(controller:'model', action:'getFrontImage', id:modelInstance.id)}" style="height: 50px;width: 100px;"/></td>
		</tr>
		<tr>
        	<td>Rear image:</td>
        	<td><img src="${createLink(controller:'model', action:'getRearImage', id:modelInstance.id)}"  style="height: 50px;width: 100px;" id="rearImageId"/></td>
        </tr>
        <tr>
			<td>Notes:</td>
			<td>${modelInstance?.description}</td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons" style="margin-left: 10px;margin-right: 10px;"> 
					<g:form action="update" >
						<input name="id" value="${modelInstance.id}" type="hidden"/>
						<span class="button">
							<g:actionSubmit class="edit" action="edit" value="Edit"></g:actionSubmit>
							<g:actionSubmit class="delete" action="delete" value="Delete"></g:actionSubmit>
						</span>
					</g:form>
				</div>
			</td>
		</tr>
	</tbody>
</table>
</div>
<div style="float: left;">
	<div>
		<div id="cablingPanel">
			<img src="${createLink(controller:'model', action:'getRearImage', id:modelInstance.id)}" />
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
				<div id="connector${i}" style="top:${modelConnector.connectorPosY / 2}px ;left:${modelConnector.connectorPosX}px "><img src="../../i/cabling/${modelConnector.status}.png"/><span>${modelConnector.label}</span></div>
			</g:each>
		</div>
	</div>
	<div style="clear: both;"></div>
	<div class="list" style="border: 1px solid #5F9FCF;margin-bottom: 10px;margin-right: 5px;">
		<table style="border: 0px;">
			<thead>
				<tr>
					<th>Connector</th>
					<th>Type</th>
					<th>Label</th>
					<th>Label Position</th>
					<th>Conn Pos X</th>
					<th>Conn Pos Y</th>
				</tr>
			</thead>
			<tbody id="connectorModelBody">
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
				<tr id="connectorTr${i}"  class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>${modelConnector.connector}</td>
					<td>${modelConnector.type}</td>
					<td>${modelConnector.label}</td>
					<td>${modelConnector.labelPosition}</td>
					<td>${modelConnector.connectorPosX}</td>
					<td>${modelConnector.connectorPosY}</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
</div>
</fieldset>
<script type="text/javascript">
var image = "${modelInstance.rearImage.hashCode().equals(29248850)}"
var usize = "${modelInstance.usize}"
if(image == 'true'){
	$("#cablingPanel").css("height",usize*50)
}
</script>
</div>
</body>
</html>
