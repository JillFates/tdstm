<%@page expressionCodec="none" %>
<%@page import="net.transitionmanager.domain.Manufacturer" %>
<%@page import="net.transitionmanager.domain.ModelConnector" %>
<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Model - Edit</title>
	<g:javascript src="model.manufacturer.js" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
	<g:javascript src="drag_drop.js" />
	<script src="${resource(dir:'js',file:'jquery.form.js')}"></script>
</head>
<body>
	<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div style="border: 0px;margin-top: 5px;" >
	<fieldset>
	<legend><b>Edit Model</b></legend>
	<g:form action="update"  enctype="multipart/form-data" name="modelForm" onsubmit="updateModel('Model', 'modelForm');">
	<div style="margin-left: 10px;margin-right: 10px; width: auto;">
	<table style="border: 0px;">
	<tbody>
		<tr>
			<td>Manufacturer:</td>
			<td><g:select id="manufacturerId" name="manufacturer.id" from="${Manufacturer.list([sort:'name',order:'asc'])}" optionKey="id" value="${modelInstance?.manufacturer.id}" onchange="akaUtil.handleParentPropChange('model')"/></td>
			<td>Model Name:</td>
			<td><input type="text" name="modelName" id="modelNameId" value="${modelInstance?.modelName}" onchange="akaUtil.handleParentPropChange('model')">
				<g:hasErrors bean="${modelInstance}" field="modelName">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelName" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr>
			<td>AKA:</td>
			<td>
				<table style="border: 0px;margin-left: -8px;">
					<tbody id="addAkaTableId">
						<g:each in="${modelAliases}" var="alias">
							<tr id="aka_${alias.id}" js-is-unique="true"><td nowrap="nowrap">
								<input type="text" class="akaValidate" id="aka_${alias.id}" name="aka_${alias.id}" value="${alias.name}" onchange="akaUtil.handleAkaChange(this, 'model', '${modelInstance?.id}')"/>
								<a href="javascript:akaUtil.deleteAkaRow('aka_${alias.id}', true, 'model')"><span class='clear_filter'><u>X</u></span></a>
								<br><div class="errors" style="display: none" id="errSpan_${alias.id}"></div>
							</td></tr>
						</g:each>
					</tbody>
				</table>
				<span style="cursor: pointer;" onclick="akaUtil.addAka('model')"><b>Add AKA</b></span>
			</td>
			<td>Asset Type:</td>
			<td><g:select id="assetTypeId" name="assetType" from="${assetTypes}" value="${modelInstance.assetType}" onchange="showBladeFields(this.value)"></g:select></td>
		</tr>
		<tr>
			<td>Usize:</td>
			<td>
				<g:select id="usizeId" name="usize" from="${modelInstance.constraints.usize.inList}" value="${modelInstance.usize}"></g:select>
			</td>
			<td>Dimensions(inches):</td>
			<td>
			H:<input type="number" min="0" size="3" name="modelHeight" id="heightId" value="${modelInstance?.height}" style="width: 44px;">
				<g:hasErrors bean="${modelInstance}" field="modelHeight">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelHeight" /></div>
				</g:hasErrors>
			W:<input type="number" min="0" size="3" name="modelWidth" id="widthId" value="${modelInstance?.width}" style="width: 44px;">
				<g:hasErrors bean="${modelInstance}" field="modelWidth">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelWidth" /></div>
				</g:hasErrors>
			D:<input type="number" min="0" size="3" name="modelDepth" id="depthId" value="${modelInstance?.depth}" style="width: 44px;">
				<g:hasErrors bean="${modelInstance}" field="modelDepth">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelDepth" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr>
			<td>Weight (pounds):</td>
			<td><input type="number" min="0" max="10000" name="modelWeight" id="weightId" value="${modelInstance?.weight}">
				<g:hasErrors bean="${modelInstance}" field="modelWeight">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelWeight" /></div>
				</g:hasErrors>
			</td>
			<td>Layout Style:</td>
			<td><input type="text" name="layoutStyle" id="layoutStyleId" value="${modelInstance?.layoutStyle}">
				<g:hasErrors bean="${modelInstance}" field="layoutStyle">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="layoutStyle" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr>
			<td>Product Line:</td>
			<td><input type="text" name="productLine" id="productLineId" value="${modelInstance?.productLine}">
				<g:hasErrors bean="${modelInstance}" field="layoutStyle">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="productLine" /></div>
				</g:hasErrors>
			</td>
			<td>Model Family:</td>
			<td><input type="text" name="modelFamily" id="modelFamilyId" value="${modelInstance?.modelFamily}">
				<g:hasErrors bean="${modelInstance}" field="layoutStyle">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelFamily" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="endOfLifeDate">End of Life Date:</label></td>
			<td valign="top" class="value ${hasErrors(bean:modelInstance,field:'endOfLifeDate','errors')}">
				<script type="text/javascript" charset="utf-8">
					jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() }); });
				</script>
				<input type="text" class="dateRange" size="15" style="width:138px;" name="endOfLifeDate" id="endOfLifeDateId"
				   value="<tds:convertDate date="${modelInstance?.endOfLifeDate}" />" />
				<g:hasErrors bean="${projectInstance}" field="startDate">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="endOfLifeDate" /></div>
				</g:hasErrors>
			</td>
			<td>End of Life Status:</td>
			<td><input type="text" name="endOfLifeStatus" id="endOfLifeStatusId" value="${modelInstance?.endOfLifeStatus}">
				<g:hasErrors bean="${modelInstance}" field="endOfLifeStatus">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="endOfLifeStatus" /></div>
				</g:hasErrors>
			</td>
		<tr>
			<td>Power (Max/Design/Avg):</td>
			<td>
				<input type="number" size="4" min="0" name="powerNameplate" id="powerNameplateEditId" value="${tds.rackPower(power: modelInstance?.powerNameplate, blankZero: true)}" style="width: 50px;"
					onblur="changePowerValue('Edit')" ><a id ="namePlateId"  title="Make standard values from nameplate" style="cursor: pointer;"
					onclick="setStanderdPower('Edit')"> >> </a>
				<input type="hidden" id="powerNameplateIdH" value="${modelInstance?.powerNameplate}">
				<input type="number" min="0" size="4" name="powerDesign" id="powerDesignEditId" style="width: 50px;" value="${tds.rackPower(power: modelInstance?.powerDesign, blankZero: true)}" >&nbsp;
				<input type="hidden" id="powerDesignIdH" value="${modelInstance?.powerDesign}" >
				<input type="number" min="0" size="4" name="powerUse" id="powerUseEditId" style="width: 50px;" value="${tds.rackPower(power: modelInstance?.powerUse, blankZero: true)}" >&nbsp;
				<input type="hidden" id="powerUseIdH" value="${modelInstance?.powerUse}" >
				<g:select id="ptype" name='powerType' value="${tds.powerType()}" from="${['Watts','Amps']}" onchange="updatePowerType(this.value,'Edit')"/>
			</td>
			<td>Notes:</td>
			<td>
				<input type="text" name="description" id="descriptionId" value="${modelInstance.description}">
			</td>
		</tr>

	<!--<tr>
			<td>Front image:</td>
			<td><input size="20" type="file" name="frontImage" id="frontImageId" accept="image/*" /></td>
			<td>Rear image:</td>
			<td><input size="20" type="file" name="rearImage" id="rearImageId" accept="image/*" />
			</td>-->
		</tr>
		<tr>
			<td valign="top" class="name">
				<label for="description">Room Object:</label>
			</td>
			<td valign="top" class="value ${hasErrors(bean:modelInstance,field:'roomObject','errors')}">
				<g:checkBox id="roomObject" name="roomObject" value='${modelInstance.roomObject}'/>
			</td>
			<!--<td>Use Image:</td>
			<td>
				<g:if test="${modelInstance.useImage}">
					<input type="checkbox" name="useImage" id="useImageId"  checked="checked" onclick="showImage(this.id)"/>
				</g:if>
				<g:else>
					<input type="checkbox" name="useImage" id="useImageId" onclick="showImage(this.id)"/>
				</g:else>
			</td>-->
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
			<g:hasErrors bean="${modelInstance}" field="bladeRows">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="bladeCount" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr id="bladeLabelCountId" style="display: ${modelInstance.assetType == 'Blade Chassis' ? 'block' : 'none'}">
			<td valign="top" class="name">Blade Label Count:</td>
			<td><input type="text" name="bladeLabelCount" value="${modelInstance.bladeLabelCount}" >
			<g:hasErrors bean="${modelInstance}" field="bladeRows">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="bladeLabelCount" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr id="bladeHeightId" style="display: ${modelInstance.assetType == 'Blade' ? 'block' : 'none'}">
			<td>Blade Height:</td>
			<td>
				<g:select id="bladeHeightId" name="bladeHeight" from="${modelInstance.constraints.bladeHeight.inList}" value="${modelInstance.bladeHeight}"></g:select>
			</td>
		</tr>
		<tr>
			<td>Created By :</td>
			<td>${modelInstance?.createdBy}</td>
			<td>Source TDS:</td>
			<td>
				<g:if test="${modelInstance.sourceTDS}">
					<input type="checkbox" name="sourceTDS" id="sourceTDSId"  checked="checked" />
				</g:if>
				<g:else>
					<input type="checkbox" name="sourceTDS" id="sourceTDSId" />
				</g:else>
			</td>
		</tr>
		<tr>
			<td>Updated By :</td>
			<td>${modelInstance?.updatedBy}</td>
			<td>Source URL :</td>
			<td><input type="text" name="sourceURL" id="sourceURLId" value="${modelInstance?.sourceURL}">
				<g:hasErrors bean="${modelInstance}" field="sourceURL">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="sourceURL" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr>
			<td>Validated By :</td>
			<td>${modelInstance?.validatedBy}</td>
			<td>Model Status :</td>
			<g:if test="${modelInstance.powerUse >0}">
				<td><g:select id="modelStatus" name='modelStatus' value ="${modelInstance?.modelStatus}" from="${['new','full','valid']}"></g:select>
				<g:hasErrors bean="${modelInstance}" field="modelStatus">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelStatus" /></div>
				</g:hasErrors>
				</td>
			</g:if>
			<g:else>
				<td>${modelInstance?.modelStatus}
				</td>
			</g:else>
		</tr>
	</tbody>
</table>
</div>
<div style="float: left;">
	<div>
		<div id="cablingPanelEdit" style="height: auto;min-height: 70px " class="cablingPanel">
			<g:if test="${modelInstance.rearImage}">
			<!--<img id="rearImage" src="${createLink(controller:'model', action:'retrieveRearImage', id:modelInstance.id)}" style="max-width:375px;display: ${modelInstance.useImage != 1 ? 'none':'block' }"/>-->
				<script type="text/javascript">
					$("#cablingPanelEdit").css("background-color","#FFF")
				</script>
			</g:if>
			<g:else>
				<script type="text/javascript">
					var usize = "${modelInstance.usize}";
					usize = (usize=="")?1:usize;
					$("#cablingPanelEdit").css("height", (usize*30) + 'px');
				</script>
			</g:else>

			<g:each in="${modelConnectors}" status="i" var="modelConnector">
				<div id="connector${modelConnector.connector}" style="float: left; position: relative; margin-right: 20px;">
					<%--
					<div style="position: relative;">
						<img src="${resource(dir:'i/cabling',file: '' + modelConnector.status + '.png')}" style="float: left;"/>
					</div>
					--%>
					<div id="labelPositionDiv${modelConnector.connector}" style="position: relative;">
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
		<table style="border: 0px;">
			<thead>
				<tr>
					<th>Type<input type="hidden" id="connectorCount" name="connectorCount" value="${nextConnector}"></th>
					<th>Label</th>
					<th>Label Position</th>
					<th>Conn Pos X</th>
					<th>Conn Pos Y</th>
				</tr>
			</thead>
			<tbody id="connectorModelBody">
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
			<tr id="connectorTr${modelConnector.connector}">
					<td><a href="javascript:verifyAndDeleteConnector(${modelConnector.connector})"><span class="clear_filter"><u>X</u></span></a>&nbsp;<g:select id="typeId${modelConnector.connector}" name="type${modelConnector.connector}" from="${ModelConnector.constraints.type.inList}" value="${modelConnector.type}"></g:select></td>
					<td><input id="labelId${modelConnector.connector}" name="label${modelConnector.connector}" type="text" value="${modelConnector.label}" onchange="changeLabel(${modelConnector.connector}, this.value)"></td>
					<td><g:select id="labelPositionId${modelConnector.connector}" name="labelPosition${modelConnector.connector}" from="${['Right','Left','Top','Bottom']}" value="${modelConnector.labelPosition}" onchange="changeLabelPosition(${modelConnector.connector}, this.value)"></g:select></td>
					<td><input id="connectorPosXId${modelConnector.connector}" name="connectorPosX${modelConnector.connector}" maxlength="3" style="width:35px;" type="number" min="0" value="${modelConnector.connectorPosX}"></td>
					<td>
						<input id="connectorId${modelConnector.connector}" name="connector${modelConnector.connector}" maxlength="5" style="width: 35px;" type="hidden" value="${modelConnector.connector}">
						<input id="connectorPosYId${modelConnector.connector}" name="connectorPosY${modelConnector.connector}" maxlength="3" style="width: 35px;" type="number" min="0" value="${modelConnector.connectorPosY}">
						<input id="statusId${modelConnector.connector}" name="status${modelConnector.connector}" type="hidden" value="${modelConnector.status}">
					</td>
				</tr>
			</g:each>
			<g:each in="${otherConnectors}" var="count">
			<tr id="connectorTr${count}" style="display: none;">
					<td><a href="javascript:verifyAndDeleteConnector(${count})"><span class="clear_filter"><u>X</u></span></a>&nbsp;<g:select id="typeId${count}" name="type" from="${ModelConnector.constraints.type.inList}"></g:select></td>
					<td><input id="labelId${count}" type="text" onchange="changeLabel(${count}, this.value)"></td>
					<td><g:select id="labelPositionId${count}" name="labelPosition" from="${['Right','Left','Top','Bottom']}" onchange="changeLabelPosition(${count}, this.value)"></g:select></td>
					<td><input id="connectorPosXId${count}" maxlength="3" style="width: 35px;" type="number" value="0"></td>
					<td>
						<input id="connectorId${count}" maxlength="5" style="width: 35px;" type="hidden" value="${count}">
						<input id="connectorPosYId${count}" maxlength="3" style="width: 35px;" type="number" value="360">
						<input id="statusId${count}" type="hidden">
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
	<tr>
		<td colspan="2">
			<div class="buttons" style="margin-left: 10px;margin-right: 10px;">
				<tds:hasPermission permission="${Permission.ModelEdit}">
					<input id="modelId" name="id" value="${modelInstance.id}" type="hidden"/>
					<input type="hidden" name="redirectTo" value="${redirectTo }" />
					<span class="button">
					 <g:if test="${redirectTo=='modelDialog'}">
						 <script>
							 $(document).ready(function(){
                                 $(document).on('submit','#modelForm',function(e){
									
									return false;
								 });
							 });
						 </script>
						<input type="submit" class="save" id="saveModelId" value="Update"/>
					</g:if>
					<g:else>
						<input type="submit" class="save" id="saveModelId" value="Update" />
					</g:else>
					</span>
				</tds:hasPermission>
				<span class="button">
					<input type="button" class="cancel" value="Cancel" id="cancelButtonId" onclick="showOrEditModelManuDetails('model',${modelInstance?.id},'Model','show','Show')" />
				</span>
			</div>
		</td>
	</tr>
</div>
<input type="hidden" name="deletedAka" id="deletedAka" />
</g:form>
</fieldset>
<div id="akaTemplateDiv" style="display:none;">
	<input type="text" name="aka" id="akaId" class="akaValidate" value=""
		onchange="akaUtil.handleAkaChange(this, 'model', '${modelInstance?.id}')"/>
</div>
</div>

<script type="text/javascript">
	//$('#connectorCount').val(${modelConnectors.size()});
	var image = "${modelInstance.rearImage}"
	var usize = "${modelInstance.usize}"
	usize = (usize=="")?1:usize;
	var useImage = "${modelInstance.useImage}"
	if(!image || useImage != '1'){
		initializeConnectors( usize, null )
	} else {
		initializeConnectors( 3, 'auto' )
	}
	$('div.connector_Left').each(function(index) {
		$(this).attr("style","margin-left:-"+$(this).children().width()+"px");
	});
	function createConnector( type ) {
		$("#connectorCount").val(parseInt($("#connectorCount").val()) + 1)
		var count = $("#connectorCount").val()
		if(count < 51 ){
			var connector = "<div style='position: relative; float: left;'></div><div id='labelPositionDiv"+count+"' style='position: relative;'><span id='connectorLabelText"+count+"'>Connector"+count+"</span></div>"
			$("#connector"+count).html(connector)
		  	$("#connector"+count).css({"position": "relative", "float": "left", "margin-right": "20px"})
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
				var connectorJ = $("#labelId"+j)
				var connectorI = $("#labelId"+i)
				if((connectorJ.length > 0) && (connectorI.length > 0) &&
					(connectorJ.val().toLowerCase() == connectorI.val().toLowerCase())) {
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
	}
	function showImage( value ){
		var imageTemp = $('#rearImageId').val() || image  ;
		if($("#"+value).is(":checked")){
			if(imageTemp){
				if(imageTemp != image)
				{
					if(!$("#rearImage").prop('src'))
						$('#cablingPanelEdit').css('height','auto').prepend('<img id="rearImage" style="display:none;max-width: 375px;"/>')
					readURL($('#rearImageId')[0],"#rearImage");
				}
				initializeConnectors( 2, 'auto' )
				$("#rearImage").show()
			} else {
				alert("Rear image does not exist")
				$("#"+value).prop("checked",false);
			}
		} else {
			$("#rearImage").hide()
			initializeConnectors( usize, null )
		}
	}

	function readURL(input,target) {

		if (input.files && input.files[0]) {
			var reader = new FileReader();

			reader.onload = function (e) {
				$(target).attr('src', e.target.result);
			}

			reader.readAsDataURL(input.files[0]);
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

	function verifyAndDeleteConnector( connector ){
		var modelId = "${modelInstance.id}"
		jQuery.ajax({
			url: "retrieveAssetCablesForConnector",
			data: "connector="+connector+"&modelId="+modelId,
			type:'POST',
			success: function(data) {
				if(data.length > 0){
					if(confirm("Some assets used this connector. Be sure you want to remove it before proceeding")){
						$("#connectorTr"+connector).remove() // Remove row from table
						$("#connector"+connector).remove() // Remove the image from model panel
					}
				} else {
					$("#connectorTr"+connector).remove() // Remove row from table
					$("#connector"+connector).remove() // Remove the image from model panel
				}
			}
		});
	}

	function updatePowerType( value , whom){
		convertPowerType(value, whom)
		${remoteFunction(controller:'project', action:'setPower', params:'\'p=\' + value ')}
	}

</script>
<script>
	currentMenuId = "#adminMenu";
	$('.menu-list-models').addClass('active');
	$('.menu-parent-admin').addClass('active');
</script>
</body>
</html>
