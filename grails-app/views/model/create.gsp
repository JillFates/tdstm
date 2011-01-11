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
<g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
<div class="steps_table" style="border: 0px;">
<fieldset>
<legend><b>Create Model Template</b></legend>
<g:form action="save" enctype="multipart/form-data">
<div style="margin-left: 10px;margin-right: 10px; float: left;width: auto;">
<table>
	<tbody>
		<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
		</tr>
		<tr>
			<td valign="top" class="name"><b>Manufacturer:<span style="color: red">*</span></b></td>
			<td valign="top" class="name">
				<g:select id="manufacturerId" name="manufacturer.id" from="${Manufacturer.list()}" optionKey="id" value="${modelInstance?.manufacturer?.id}"></g:select>
				<g:hasErrors bean="${modelInstance}" field="manufacturer">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="manufacturer" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap"><b>Model Name:<span style="color: red">*</span></b></td>
			<td>
				<input type="text" name="modelName" id="modelNameId" value="${modelInstance?.modelName}">
				<g:hasErrors bean="${modelInstance}" field="modelName">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelName" /></div>
				</g:hasErrors> 
			</td>
		</tr>
		<tr>
			<td valign="top" class="name">AKA:</td>
			<td>
				<input type="text" name="aka" id="akaId" value="${modelInstance?.aka}">
				<g:hasErrors bean="${modelInstance}" field="aka">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="aka" /></div>
				</g:hasErrors> 
			</td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap">Asset Type:</td>
			<td><g:select id="assetTypeId" name="assetType" from="${modelInstance.assetTypeList}" value="${modelInstance.assetType}"></g:select></td>
		</tr>
		<tr>
			<td valign="top" class="name">Usize:</td>
			<td>
				<g:select id="usizeId" name="usize" from="${modelInstance.constraints.usize.inList}" value="${modelInstance.usize}"></g:select>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap">Power (typical):</td>
			<td><g:select id="powerUseId" name="powerUse" from="${modelInstance.constraints.powerUse.inList}" value="${modelInstance.powerUse}"></g:select>&nbsp;Watts</td>
		</tr>
		<tr>
		<td valign="top" class="name" nowrap="nowrap">Front image:</label></td>
        <td><input size="20" type="file" name="frontImage" id="frontImageId" />
		</td>
		</tr>
		<tr>
        <td valign="top" class="name" nowrap="nowrap">Rear image:</td>
        <td><input size="20" type="file" name="rearImage" id="rearImageId" />
        </td>
        </tr>
        <tr>
        	<td>Use Image:</td>
	        <td>
	        	<input type="checkbox" name="useImage" id="useImageId"/>
	        	<g:hasErrors bean="${modelInstance}" field="useImage">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="useImage" /></div>
				</g:hasErrors>
	        </td>
        </tr>
        <tr>
			<td valign="top" class="name">Notes:</td>
			<td><input type="text" name="description" id="descriptionId" value="${modelInstance.description}" > </td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons" style="margin-left: 10px;margin-right: 10px;"> 
						<span class="button">
							<g:actionSubmit class="save" action="save" value="Save"></g:actionSubmit>
							<g:actionSubmit class="delete" action="list" value="Cancel"></g:actionSubmit>
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
			<g:each in="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50]}" var="count">
				<div id="connector${count}"></div>
			</g:each>
		</div>
		<div id="optionsPanel">
			<span style="font-weight: bold;"><a href="javascript:createConnector('missing')">Add Connector</a></span>
		</div>
	</div>
	<div style="clear: both;"></div>
	<div style="border: 1px solid #5F9FCF;margin-bottom: 10px;margin-right: 5px;">
		<table>
			<thead>
				<tr>
					<th>Connector<input type="hidden" id="connectorCount" name="connectorCount" value="0"></th>
					<th>Type</th>
					<th>Label</th>
					<th>Label Position</th>
					<th>Conn Pos X</th>
					<th>Conn Pos Y</th>
				</tr>
			</thead>
			<tbody id="connectorModelBody">
			<g:each in="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50]}" var="count">
			<tr id="connectorTr${count}" style="display: none;">
					<td><input id="connectorId${count}" maxlength="5" style="width: 35px;" type="text" value="${count}"></td>
					<td><g:select id="typeId${count}" name="type" from="${ModelConnector.constraints.type.inList}"></g:select></td>
					<td><input id="labelId${count}" type="text" onchange="changeLabel(${count}, this.value)"></td>
					<td><g:select id="labelPositionId${count}" name="labelPosition" from="${['Right','Bottom']}" onchange="changeLabelPosition(${count}, this.value)"></g:select></td>
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
	$('#connectorCount').val(0);
	
	initializeConnectors( 1 )
		
	function createConnector( type ) {
		$("#connectorCount").val(parseInt($("#connectorCount").val()) + 1)
		var count = $("#connectorCount").val()
		if(count < 51 ){
			var connector = "<div><img src='../i/cabling/"+type+".png'/></div><div id='labelPositionDiv"+count+"' class='connector_right'><span id='connectorLabelText"+count+"'>Connector"+count+"</span></div>"
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
	function changeLabelPosition(count, value){
		if(value == "Right"){
			$("#labelPositionDiv"+count).removeClass("connector_bottom")
			$("#labelPositionDiv"+count).addClass("connector_right")
		} else {
			$("#labelPositionDiv"+count).removeClass("connector_right")
			$("#labelPositionDiv"+count).addClass("connector_bottom")
		}
		
	}
</script>
</body>
</html>
