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
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> 
			<input name="modelId" type="hidden" value="${modelTemplate?.id}">
			</td>
		</tr>
		<tr>
			<td valign="top" class="name"><b>Manufacturer:<span style="color: red">*</span></b></td>
			<td valign="top" class="name">
				<g:select id="manufacturerId" name="manufacturer.id" from="${Manufacturer.list([sort:'name',order:'asc'])}" optionKey="id" value="${modelInstance?.manufacturer?.id}"></g:select>
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
			<td><g:select id="assetTypeId" name="assetType" from="${modelInstance.assetTypeList}" value="${modelInstance.assetType}" onchange="showBladeFields(this.value)"></g:select></td>
		</tr>
		<tr>
			<td valign="top" class="name">Usize:</td>
			<td>
				<g:select id="usizeId" name="usize" from="${modelInstance.constraints.usize.inList}" value="${modelInstance.usize}"></g:select>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap">Power (typical):</td>
			<td><input type="text" name="powerUse" id="powerUseId" value="${modelInstance.powerUse}" >&nbsp;Watts</td>
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
	        	<g:if test="${modelTemplate?.useImage}">
	       			<input type="checkbox" name="useImage" id="useImageId"  checked="checked" onclick="showImage(this.id)"/>
		        </g:if>
		        <g:else>
		        	<input type="checkbox" name="useImage" id="useImageId" onclick="showImage(this.id)"/>
		        </g:else>
	        </td>
        </tr>
		<tr id="bladeRowsId" style="display: ${modelInstance.assetType == 'Blade Chassis' ? 'block' : 'none'}">
			<td valign="top" class="name">Blade Rows:</td>
			<td><input type="text" name="bladeRows" value="${modelInstance.bladeRows}" > 
			<g:hasErrors bean="${modelInstance}" field="bladeRows">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="bladeRows" /></div>
				</g:hasErrors> 
			</td>
		</tr>
		<tr id="bladeCountId" style="display: ${modelInstance.assetType == 'Blade Chassis' ? 'block' : 'none'}">
			<td valign="top" class="name">Blade Count:</td>
			<td><input type="text" name="bladeCount" value="${modelInstance.bladeCount}" > 
				<g:hasErrors bean="${modelInstance}" field="bladeCount">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="bladeCount" /></div>
				</g:hasErrors> 
			</td>
		</tr>
		<tr id="bladeLabelCountId" style="display: ${modelInstance.assetType == 'Blade Chassis' ? 'block' : 'none'}">
			<td valign="top" class="name">Blade Label Count:</td>
			<td><input type="text" name="bladeLabelCount" value="${modelInstance.bladeLabelCount}" > 
			<g:hasErrors bean="${modelInstance}" field="bladeLabelCount">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="bladeLabelCount" /></div>
				</g:hasErrors> 
			</td>
		</tr>
		<tr id="bladeHeightId" style="display: ${modelInstance.assetType == 'Blade' ? 'block' : 'none'}">
			<td>Blade Height:</td>
			<td>
				<g:select id="bladeHeightId" name="bladeHeight" from="${modelInstance.constraints.bladeHeight.inList}" ></g:select>
			</td>
		</tr>
		<tr>
        	<td>Source TDS:</td>
	        <td>
	        	<g:if test="${modelTemplate?.sourceTDS}">
	       			<input type="checkbox" name="sourceTDS" id="sourceTDSId"  checked="checked" />
		        </g:if>
		        <g:else>
		        	<input type="checkbox" name="sourceTDS" id="sourceTDSId"/>
		        </g:else>
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
						<g:actionSubmit class="save" action="save" value="Save" onclick="return validateForm()"></g:actionSubmit>
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
			<g:if test="${modelTemplate?.rearImage}">
				<img id="rearImage" src="${createLink(controller:'model', action:'getRearImage', id:modelTemplate?.id)}" style="display: ${modelTemplate?.useImage != 1 ? 'none':'block' }"/>
			</g:if>
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
				<div id="connector${modelConnector.connector}" style="top:${modelConnector.connectorPosY / 2}px ;left:${modelConnector.connectorPosX}px ">
					<div>
						<img src="../i/cabling/${modelConnector.status}.png"/>
					</div>
					<div id="labelPositionDiv${modelConnector.connector}" class="connector_${modelConnector.labelPosition}">
						<span id='connectorLabelText${modelConnector.connector}'>${modelConnector.label}</span>
					</div>
				</div>
			</g:each>
			<g:each in="${otherConnectors}" var="count">
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
					<th>Type<input type="hidden" id="connectorCount" name="connectorCount" value="${modelConnectors ? modelConnectors.size() : 0}"></th>
					<th>Label</th>
					<th>Label Position</th>
					<th>Conn Pos X</th>
					<th>Conn Pos Y</th>
				</tr>
			</thead>
			<tbody id="connectorModelBody">
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
			<tr id="connectorTr${modelConnector.connector}">
					<td><g:select id="typeId${modelConnector.connector}" name="type${modelConnector.connector}" from="${ModelConnector.constraints.type.inList}" value="${modelConnector.type}"></g:select></td>
					<td><input title="Label" id="labelId${modelConnector.connector}" name="label${modelConnector.connector}" type="text" value="${modelConnector.label}" onchange="changeLabel(${modelConnector.connector}, this.value)"></td>
					<td><g:select id="labelPositionId${modelConnector.connector}" name="labelPosition${modelConnector.connector}" from="${['Right','Left','Top','Bottom']}" value="${modelConnector.labelPosition}" onchange="changeLabelPosition(${modelConnector.connector}, this.value)"></g:select></td>
					<td><input id="connectorPosXId${modelConnector.connector}" name="connectorPosX${modelConnector.connector}" maxlength="3" style="width: 25px;" type="text" value="${modelConnector.connectorPosX}"></td>
					<td>
						<input id="connectorId${modelConnector.connector}" name="connector${modelConnector.connector}" maxlength="5" style="width: 35px;" type="hidden" value="${modelConnector.connector}">
						<input id="connectorPosYId${modelConnector.connector}" name="connectorPosY${modelConnector.connector}" maxlength="3" style="width: 25px;" type="text" value="${modelConnector.connectorPosY}">
						<input id="statusId${modelConnector.connector}" name="status${modelConnector.connector}" type="hidden" value="${modelConnector.status}">
					</td>
				</tr>
			</g:each>
			<g:each in="${otherConnectors}" var="count">
			<tr id="connectorTr${count}" style="display: none;">
					<td><g:select id="typeId${count}" name="type" from="${ModelConnector.constraints.type.inList}"></g:select></td>
					<td><input id="labelId${count}" type="text" onchange="changeLabel(${count}, this.value)"></td>
					<td><g:select id="labelPositionId${count}" name="labelPosition" from="${['Right','Left','Top','Bottom']}" onchange="changeLabelPosition(${count}, this.value)"></g:select></td>
					<td><input id="connectorPosXId${count}" maxlength="3" style="width: 25px;" type="text" value="0"></td>
					<td>
						<input id="connectorId${count}" maxlength="5" style="width: 35px;" type="hidden" value="${count}">
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
	$('#connectorCount').val(${modelConnectors ? modelConnectors.size() : 0});
	var image = "${modelTemplate?.rearImage}"
	var usize = "${modelTemplate?.usize}"
	var useImage = "${modelTemplate?.useImage}" 
	if(!image || useImage != '1'){
		initializeConnectors( usize ? usize : 1, null )
	} else {
		initializeConnectors( 3, 'auto' )
		$("#cablingPanel").css("background-color","#FFF")
	}
	
	
	function createConnector( type ) {
		$("#connectorCount").val(parseInt($("#connectorCount").val()) + 1)
		var count = $("#connectorCount").val()
		if(count < 51 ){
			var connector = "<div><img src='../i/cabling/"+type+".png'/></div><div id='labelPositionDiv"+count+"' class='connector_Right'><span id='connectorLabelText"+count+"'>Connector"+count+"</span></div>"
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
	function changeLabel( id, value){
		var count = $("#connectorCount").val()
		for(j=1; j<=count; j++){
			var matchConnectors = 0
			for(i=1; i<=count; i++){
				if($("#labelId"+j).val().toLowerCase() == $("#labelId"+i).val().toLowerCase()){
					matchConnectors = matchConnectors + 1 
				}
			}
			if(matchConnectors > 1){
				$("#labelId"+j).attr("title","Connector label '"+$("#labelId"+j).val()+"' should be unique")
				$("#labelId"+j).addClass("field_error")
			} else {
				$("#labelId"+j).removeClass("field_error")
			}
		}
		$("#connectorLabelText"+id).html(value)
		if($("#labelPositionId"+id).val() == "Left"){
			$("#labelPositionDiv"+id).attr("style","margin-left:-"+$('#connectorLabelText'+id).outerWidth()+"px")	
		}
	}
	function changeLabelPosition(count, value){
		$("#labelPositionDiv"+count).removeAttr("class")
		$("#labelPositionDiv"+count).removeAttr("style")
		$("#labelPositionDiv"+count).addClass("connector_"+value)
		if(value == "Left"){
			$("#labelPositionDiv"+count).attr("style","margin-left:-"+$('#connectorLabelText'+count).outerWidth()+"px")	
		}
		//$('body').find('span:last').width();
	}
	function showImage( value ){
		if($("#"+value).is(":checked")){
			if(image ){
				initializeConnectors( 2, 'auto' )
				$("#rearImage").show()
			} else {
				alert("Rear image does not exist")
			}
		} else {
			$("#rearImage").hide()
			initializeConnectors( usize, null )
		}
	}
	function validateForm(){
		var isValid = true
		if($(".field_error").length){
			isValid = false
			alert("WARNING : Connector labels should be unique")
		}
		return isValid
	}
	
	function showBladeFields( value ){
		if(value == "Blade Chassis"){
			$("#bladeRowsId").show()
			$("#bladeCountId").show()
			$("#bladeLabelCountId").show()
		} else {
			$("#bladeRowsId").hide()
			$("#bladeCountId").hide()
			$("#bladeLabelCountId").hide()
		}
		if(value == "Blade"){
			$("#bladeHeightId").show()
		} else {
			$("#bladeHeightId").hide()
		}
	}
	showBladeFields($("#assetTypeId").val())
</script>
</body>
</html>
