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
<div style="border: 0px;margin-top: 5px;" >
<fieldset>
<legend><b>Edit Model Template</b></legend>
<g:form action="save"  enctype="multipart/form-data">
<div style="margin-left: 10px;margin-right: 10px; float: left;width: auto;">
<table style="border: 0px;">
	<tbody>
		<tr>
			<td>Manufacturer:</td>
			<td><g:select id="manufacturerId" name="manufacturer.id" from="${Manufacturer.list()}" optionKey="id" value="${modelInstance?.manufacturer.id}"></g:select></td>
		</tr>
		<tr>
			<td>Model Name:</td>
			<td><input type="text" name="modelName" id="modelNameId" value="${modelInstance?.modelName}">
			<g:hasErrors bean="${modelInstance}" field="modelName">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelName" /></div>
				</g:hasErrors>
			 </td>
		</tr>
		<tr>
			<td>AKA:</td>
			<td><input type="text" name="aka" id="akaId" value="${modelInstance?.aka}">
			<g:hasErrors bean="${modelInstance}" field="aka">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="aka" /></div>
				</g:hasErrors>
			 </td>
		</tr>
		<tr>
			<td>Asset Type:</td>
			<td><g:select id="assetTypeId" name="assetType" from="${modelInstance.assetTypeList}" value="${modelInstance.assetType}" ></g:select></td>
		</tr>
		<tr>
			<td>Usize:</td>
			<td>
				<g:select id="usizeId" name="usize" from="${modelInstance.constraints.usize.inList}" value="${modelInstance.usize}"></g:select>
			</td>
		</tr>
		<tr>
			<td>Power (typical):</td>
			<td><g:select id="powerUseId" name="powerUse" from="${modelInstance.constraints.powerUse.inList}" value="${modelInstance.powerUse}"></g:select>&nbsp;Watts</td>
		</tr>
		<tr>
		<td>Front image:</label></td>
        <td><input size="20" type="file" name="frontImage" id="frontImageId" />
		</td>
		</tr>
		<tr>
        <td>Rear image:</td>
        <td><input size="20" type="file" name="rearImage" id="rearImageId" />
        </td>
        </tr>
        <tr>
			<td>Notes:</td>
			<td><input type="text" name="description" id="descriptionId"> </td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons" style="margin-left: 10px;margin-right: 10px;"> 
					<input name="id" value="${modelInstance.id}" type="hidden"/>
					<span class="button">
						<g:actionSubmit class="save" action="update" value="Update"></g:actionSubmit>
						<g:actionSubmit class="delete" action="show" value="Cancel"></g:actionSubmit>
					</span>
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
				<div id="connector${modelConnector.connector}" style="top:${modelConnector.connectorPosY / 2}px ;left:${modelConnector.connectorPosX}px "><img src="../i/cabling/${modelConnector.status}.png"/><span id='connectorLabelText${modelConnector.connector}'>${modelConnector.label}</span></div>
			</g:each>
			<g:each in="${otherConnectors}" var="count">
				<div id="connector${count}"></div>
			</g:each>
		</div>
		<div id="optionsPanel">
			<ul><li style="font-weight: bold;"><a href="javascript:createConnector('missing')">Add Connector</a></li></ul>
			<ul><li>&nbsp;</li></ul>
			<ul>
				<li><img src="${createLinkTo(dir:'i/cabling',file:'missing.png')}"/>Missing/Unknown</li>
				<li><img src="${createLinkTo(dir:'i/cabling',file:'empty.png')}"/>Empty connector</li>
				<li><img src="${createLinkTo(dir:'i/cabling',file:'cabled.png')}"/>Cabled</li>
				<li><img src="${createLinkTo(dir:'i/cabling',file:'cabledDetails.png')}"/>Cabled with details</li>
			</ul>
		</div>
	</div>
	<div style="clear: both;"></div>
	<div style="border: 1px solid #5F9FCF;margin-bottom: 10px;margin-right: 5px;">
		<table style="border: 0px;">
			<thead>
				<tr>
					<th>Connector<input type="hidden" id="connectorCount" name="connectorCount" value="${modelConnectors.size()}"></th>
					<th>Type</th>
					<th>Label</th>
					<th>Label Position</th>
					<th>Conn Pos X</th>
					<th>Conn Pos Y</th>
				</tr>
			</thead>
			<tbody id="connectorModelBody">
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
			<tr id="connectorTr${modelConnector.connector}">
					<td><input id="connectorId${modelConnector.connector}" name="connector${modelConnector.connector}" maxlength="5" style="width: 35px;" type="text" value="${modelConnector.connector}"></td>
					<td><g:select id="typeId${modelConnector.connector}" name="type${modelConnector.connector}" from="${ModelConnector.constraints.type.inList}" value="${modelConnector.type}"></g:select></td>
					<td><input id="labelId${modelConnector.connector}" name="label${modelConnector.connector}" type="text" value="${modelConnector.label}" onchange="changeLabel(${modelConnector.connector}, this.value)"></td>
					<td><g:select id="labelPositionId${modelConnector.connector}" name="labelPosition${modelConnector.connector}" from="${['Right','Left','Top','Bottom']}" value="${modelConnector.labelPosition}"></g:select></td>
					<td><input id="connectorPosXId${modelConnector.connector}" name="connectorPosX${modelConnector.connector}" maxlength="3" style="width: 25px;" type="text" value="${modelConnector.connectorPosX}"></td>
					<td>
						<input id="connectorPosYId${modelConnector.connector}" name="connectorPosY${modelConnector.connector}" maxlength="3" style="width: 25px;" type="text" value="${modelConnector.connectorPosY}">
						<input id="statusId${modelConnector.connector}" name="status${modelConnector.connector}" type="hidden" value="${modelConnector.status}">
					</td>
				</tr>
			</g:each>
			<g:each in="${otherConnectors}" var="count">
			<tr id="connectorTr${count}" style="display: none;">
					<td><input id="connectorId${count}" maxlength="5" style="width: 35px;" type="text" value="${count}"></td>
					<td><g:select id="typeId${count}" name="type" from="${ModelConnector.constraints.type.inList}"></g:select></td>
					<td><input id="labelId${count}" type="text" onchange="changeLabel(${count}, this.value)"></td>
					<td><g:select id="labelPositionId${count}" name="labelPosition" from="${['Right','Left','Top','Bottom']}"></g:select></td>
					<td><input id="connectorPosXId${count}" maxlength="3" style="width: 25px;" type="text" value="0"></td>
					<td>
						<input id="connectorPosYId${count}" maxlength="3" style="width: 25px;" type="text" value="360">
						<input id="statusId${count}" type="hidden">
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
</div>
</g:form>
</fieldset>
</div>
<script type="text/javascript">
	$('#connectorCount').val(${modelConnectors.size()});
	var image = "${modelInstance.rearImage}"
	var usize = "${modelInstance.usize}"
	if(!image){
		initializeConnectors( usize )
	} else {
		initializeConnectors( 4 )
	}
	

	function createConnector( type ) {
		$("#connectorCount").val(parseInt($("#connectorCount").val()) + 1)
		var count = $("#connectorCount").val()
		if(count < 51 ){
			var connector = "<img src='../i/cabling/"+type+".png'/><span id='connectorLabelText"+count+"'>Connector"+count+"</span>"
			$("#connector"+count).html(connector)
			var modelConnector = $("#connectorTabe tbody").html()
			$("#connectorModelBody").append(modelConnector)
			// change attributes based on count
			$("#connectorTr"+count).show()
			$("#connectorModelBody input[id=connectorId"+count+"]").val(count)
			$("#connectorModelBody input[id=connectorId"+count+"]").attr("name","connector"+count)
			$("#connectorModelBody select[id=typeId"+count+"]").attr("name","type"+count)
			$("#connectorModelBody input[id=labelId"+count+"]").attr("name","label"+count)
			$("#connectorModelBody input[id=labelId"+count+"]").val("Connector"+count)
			$("#connectorModelBody select[id=labelPositionId"+count+"]").attr("name","labelPosition"+count)
			$("#connectorModelBody input[id=connectorPosXId"+count+"]").attr("name","connectorPosX"+count)
			$("#connectorModelBody input[id=connectorPosYId"+count+"]").attr("name","connectorPosY"+count)			
			$("#connectorModelBody input[id=connectorPosXId"+count+"]").val(0)
			$("#connectorModelBody input[id=connectorPosYId"+count+"]").val(0)
			$("#connectorModelBody input[id=statusId"+count+"]").attr("name","status"+count)			
			$("#connectorModelBody input[id=statusId"+count+"]").val(type)
			
		} else {
			alert("You are attempt to create more than 50 connectors")
		}
	}
	function changeLabel( count, value){
		$("#connectorLabelText"+count).html(value)
	}
</script>
</body>
</html>
