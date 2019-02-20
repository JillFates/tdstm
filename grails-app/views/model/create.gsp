<%@page expressionCodec="none" %>
<%@page import="net.transitionmanager.domain.Manufacturer" %>
<%@page import="net.transitionmanager.domain.ModelConnector" %>
<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Model - New</title>

</head>
<body>
<div class="body">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="steps_table" style="border: 0px;">
<fieldset>
<legend><b>Create Model</b></legend>
<g:form action="list" enctype="multipart/form-data" name="modelDialogForm" onsubmit="return validateForm('modelDialogForm')">
<div style="margin-left: 10px;margin-right: 10px; width: auto;">
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
				<g:select id="manufacturerId" name="manufacturer.id" from="${Manufacturer.list([sort:'name',order:'asc'])}" optionKey="id" value="${modelInstance?.manufacturer?.id}" onchange="akaUtil.handleParentPropChange('model')"></g:select>
				<g:hasErrors bean="${modelInstance}" field="manufacturer">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="manufacturer" /></div>
				</g:hasErrors>
			</td>
			<td valign="top" class="name" nowrap="nowrap"><b>Model Name:<span style="color: red">*</span></b></td>
			<td>
				<input type="text" name="modelName" id="modelNameId" value="${modelInstance?.modelName}" onchange="akaUtil.handleParentPropChange('model')">
				<g:hasErrors bean="${modelInstance}" field="modelName">
					<div class="errors"><g:renderErrors bean="${modelInstance}" as="list" field="modelName" /></div>
				</g:hasErrors>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name">AKA:</td>
			<td>
				<table style="border:0px ;margin-left:-8px">
					<tbody id="addAkaTableId">
					</tbody>
				</table>
				<span id="addAkaId" class="addAkaDisabled" onclick="akaUtil.addAka('model')"><b>Add AKA</b></span>
			</td>
			<td valign="top" class="name" nowrap="nowrap">Asset Type:</td>
			<td><g:select id="assetTypeId" name="assetType" from="${assetTypes}" value="${modelInstance.assetType}" onchange="showBladeFields(this.value)"></g:select></td>
		<tr>
			<td valign="top" class="name">Usize:</td>
			<td>
				<g:select id="usizeId" name="usize" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(modelInstance.class).usize.inList}" value="${modelInstance.usize}"></g:select>
			</td>
			<td valign="top" class="name" nowrap="nowrap">Dimensions(inches):</td>
			<td>
			H:<input type="number" min="0" size="3" name="height" id="heightId" value="" style="width: 44px;"/>
			W:<input type="number" min="0" size="3" name="width" id="widthId" value="" style="width: 44px;"/>
			D:<input type="number" min="0" size="3" name="depth" id="depthId" value="" style="width: 44px;"/>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap">Model Weight:</td>
			<td>
				<input type="number" min="0" max="10000" name="weight" id="weightId" value=""/>
			</td>
			<td valign="top" class="name" nowrap="nowrap">Layout Style:</td>
			<td>
				<input type="text" name="layoutStyle" id="layoutStyle" value=""/>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap">Product Line:</td>
			<td>
				<input type="text" name="productLine" id="productLine" value=""/>
			</td>
			<td valign="top" class="name" nowrap="nowrap">Model Family:</td>
			<td>
				<input type="text" name="modelFamily" id="modelFamily" value=""/>
			</td>
		</tr>
		<tr>
			<td><label for="endOfLifeDate"> End of Life Date:</label></td>
			<td class="value ${hasErrors(bean:modelInstance ,field:'endOfLifeDate','errors')}">
				<script type="text/javascript" charset="utf-8">
					jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() }); });
				</script> <input type="text" class="dateRange" size="15" style="width: 138px;" name="endOfLifeDate" id="endOfLifeDateId"
					value="<tds:convertDate date="${modelInstance?.endOfLifeDate}" />" />
				<g:hasErrors bean="${modelInstance}" field="endOfLifeDate">
					<div class="errors">
						<g:renderErrors bean="${modelInstance}" as="list" field="endOfLifeDate" />
					</div>
				</g:hasErrors>
			</td>
			<td valign="top" class="name" nowrap="nowrap">End of Life Status:</td>
			<td>
				<input type="text" name="endOfLifeStatus" id="endOfLifeStatus" value=""/>
			</td>
		</tr>
			<td>
				<input type = "hidden" id="modelScope" name='modelScope' />
			</td>
		<tr>
			<td valign="top" class="name" nowrap="nowrap">Power (Max/Design/Avg):</td>
			<td>
				<input type="number" size="4" name="powerNameplate" id="powerNameplateCreateId" value="${modelInstance.powerNameplate}"
					onblur="changePowerValue('Create')" style="width: 50px;"><a id ="namePlateId"  title="Make standard values from nameplate" style="cursor: pointer;vertical-align: top"
					onclick="setStanderdPower('Create')"> >> </a>
				<input type="number" size="4" name="powerDesign" id="powerDesignCreateId" value="${modelInstance.powerDesign}" style="width: 50px;" >&nbsp;
				<input type="number" size="4" name="powerUse" id="powerUseCreateId" value="${modelInstance.powerUse}" style="width: 50px;">&nbsp;
				<g:select id="powerTypeId" name='powerType' from="${['Watts','Amps']}" value="${tds.powerType()}" /></td>
			</td>
			<td valign="top" class="name">Notes:</td>
			<td>
				<input type="text" name="description" id="descriptionId" value="${modelInstance.description}" > </td>
			</td>
		</tr>
		<!--<tr>
			<td valign="top" class="name" nowrap="nowrap">Front image:</label></td>
			<td><input size="20" type="file" name="frontImage" id="frontImageId" accept="image/*" /></td>
			<td valign="top" class="name" nowrap="nowrap">Rear image:</td>
			<td><input size="20" type="file" name="rearImage" id="rearImageId" accept="image/*" /></td>
		</tr>-->
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
				<g:select id="bladeHeightId" name="bladeHeight" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(modelInstance.class).bladeHeight.inList}" ></g:select>
			</td>
		</tr>
		<tr>
			<!--<td>Use Image:</td>
			<td>
				<g:if test="${modelTemplate?.useImage}">
					<input type="checkbox" name="useImage" id="useImageId"  checked="checked" onclick="showImage(this.id)"/>
				</g:if>
				<g:else>
					<input type="checkbox" name="useImage" id="useImageId" onclick="showImage(this.id)"/>
				</g:else>
			</td>-->
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
			<td valign="top" class="name"><label for="useForPlanning">Room Object:</label></td>
			<td><input type="checkbox" name="roomObject" id="roomObject" ${modelInstance.roomObject ? 'checked="checked"' : ''}/></td>
			<td valign="top" class="name" nowrap="nowrap">Source URL :</td>
			<td><input type="text" name="sourceURL " id="sourceURL " value=""/></td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap">Validated By:</td>
			<td>
				<input type="text" name="validatedBy" id="validatedBy" value="" readonly="readonly"/>
			</td>
			<td valign="top" class="name" nowrap="nowrap">Model Status :</td>
			<td>
				<g:select id="modelStatus" name='modelStatus' value ='${modelInstance.modelStatus}' from="${['new']}" ></g:select>
			</td>
		</tr>
	</tbody>
</table>
</div>
<div style="float: left;">
	<div>
		<div id="cablingPanel" style="min-height: 70px;">
			<g:if test="${modelTemplate?.rearImage}">
				<img id="rearImage" src="${createLink(controller:'model', action:'retrieveRearImage', id:modelTemplate?.id)}" style="max-width:375px; display: ${modelTemplate?.useImage != 1 ? 'none':'block' }"/>
			</g:if>
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
				<div id="connector${modelConnector.connector}" style="top:${modelConnector.connectorPosY / 2}px ;left:${modelConnector.connectorPosX}px ">
					<div>
						<img src="${resource(dir:'i/cabling',file: '' + modelConnector.status + '.png')}"/>
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
					<td><g:select id="typeId${modelConnector.connector}" name="type${modelConnector.connector}" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(ModelConnector).type.inList}" value="${modelConnector.type}"></g:select></td>
					<td><input title="Label" id="labelId${modelConnector.connector}" name="label${modelConnector.connector}" type="text" value="${modelConnector.label}" onchange="changeLabel(${modelConnector.connector}, this.value)"></td>
					<td><g:select id="labelPositionId${modelConnector.connector}" name="labelPosition${modelConnector.connector}" from="${['Right','Left','Top','Bottom']}" value="${modelConnector.labelPosition}" onchange="changeLabelPosition(${modelConnector.connector}, this.value)"></g:select></td>
					<td><input id="connectorPosXId${modelConnector.connector}" name="connectorPosX${modelConnector.connector}" maxlength="3" style="width: 25px;" type="number" min="0" value="${modelConnector.connectorPosX}"></td>
					<td>
						<input id="connectorId${modelConnector.connector}" name="connector${modelConnector.connector}" maxlength="5" style="width: 35px;" type="hidden" value="${modelConnector.connector}">
						<input id="connectorPosYId${modelConnector.connector}" name="connectorPosY${modelConnector.connector}" maxlength="3" style="width: 25px;" type="number" min="0" value="${modelConnector.connectorPosY}">
						<input id="statusId${modelConnector.connector}" name="status${modelConnector.connector}" type="hidden" value="${modelConnector.status}">
					</td>
				</tr>
			</g:each>
			<g:each in="${otherConnectors}" var="count">
			<tr id="connectorTr${count}" style="display: none;">
					<td><g:select id="typeId${count}" name="type" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(ModelConnector).type.inList}"></g:select></td>
					<td><input id="labelId${count}" type="text" onchange="changeLabel(${count}, this.value)"></td>
					<td><g:select id="labelPositionId${count}" name="labelPosition" from="${['Right','Left','Top','Bottom']}" onchange="changeLabelPosition(${count}, this.value)"></g:select></td>
					<td><input id="connectorPosXId${count}" maxlength="3" style="width: 35px;" type="number" min="0" value="0"></td>
					<td>
						<input id="connectorId${count}" maxlength="5" style="width: 35px;" type="hidden" value="${count}">
						<input id="connectorPosYId${count}" maxlength="3" style="width: 35px;" type="number" min="0" value="360">
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
					<span class="button">
						<g:actionSubmit class="save" id="saveModelId" action="save" value="Save"></g:actionSubmit>
					</span>
					</tds:hasPermission>
				</div>
			</td>
		</tr>
</div>

</g:form>
</fieldset>
	<div id="akaTemplateDiv" style="display:none;">
		<input type="text" class="akaValidate" name="aka" id="akaId" value="" onchange="akaUtil.handleAkaChange(this, 'model', '')">
	</div>
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
			var connector = "<div><img src=\"/../i/cabling/"+ type +".png\"/></div><div id='labelPositionDiv"+count+"' class='connector_Right'><span id='connectorLabelText"+count+"'>Connector"+count+"</span></div>";
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

	function validateForm(form){
		var isValid = true
		var modelName = $("form[name = "+form+"] input[name = 'modelName']").val()
		if(!modelName){
			alert("The model name cannot be null.")
			isValid = false
		}

		$("#modelNameId").val($("#modelNameId").val().trim())

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
<script>
	currentMenuId = "#adminMenu";
	$('.menu-list-models').addClass('active');
	$('.menu-parent-admin').addClass('active');
</script>
</body>
</html>
