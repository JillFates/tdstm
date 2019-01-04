<%@page import="com.tdssrc.grails.WebUtil";%>
<%@page import="net.transitionmanager.security.Permission"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Model</title>
    <script type="text/javascript">
		$(document).ready(function() {
		    $("#editModelView").dialog({ autoOpen: false })
		})
	</script>
  </head>
  <body>
<div class="body">
<div style="border: 0px;margin-top: 5px;">
<fieldset>
<legend><b>Model</b></legend>
<div style="margin-left: 10px;margin-right: 10px;width: auto;">
<table style="border: 0px;">
	<tbody>
		<tr>
			<td>Manufacturer:</td>
			<td>${modelInstance?.manufacturer?.name}</td>
			<td>Model Name: (<a href="#"
			 onclick="MyGoogle=window.open('http://www.google.com/#sclient=psy-ab&q='+escape('${modelInstance?.manufacturer?.name}'+' '+'${modelInstance?.modelName}'+' technical specifications pdf'),'MyGoogle','toolbar=yes,location=yes,menubar=yes,scrollbars=yes,resizable=yes'); return false">search</a>)
			</td>
			<td>${modelInstance?.modelName}</td>
		</tr>
		<tr>
			<td>AKA:</td>
			<td>${modelAkas}</td>
			<td>Asset Type:</td>
			<td>${modelInstance?.assetType}</td>
		</tr>
		<tr>
			<td>Usize:</td>
			<td>${modelInstance?.usize}</td>
			<td>Dimensions (inches):</td>
			<td>H:${modelInstance?.height}&nbsp;
				W:${modelInstance?.width}&nbsp;
				D:${modelInstance?.depth}
			</td>
		</tr>
		<tr>
			<td>Weight (lbs):</td>
			<td>${modelInstance?.weight}</td>
			<td>Layout Style:</td>
			<td>${modelInstance?.layoutStyle}</td>
		</tr>
		<tr>
			<td>Product Line:</td>
			<td>${modelInstance?.productLine}</td>
			<td>Model Family:</td>
			<td>${modelInstance?.modelFamily}</td>
		</tr>
		<tr>
		    <td>End of Life Date:</td>
		    <td><tds:convertDate date="${modelInstance?.endOfLifeDate}" /></td>
            <td>End of Life Status:</td>
			<td>${modelInstance?.endOfLifeStatus}</td>
        </tr>
		<tr>
			<td>Power (max/design/avg) :</td>
			<td>
				<span id="namePlatePowerSpanId">${tds.rackPower(power: modelInstance?.powerNameplate)}</span>
				<input type="hidden" id="powerNameplateId" value="${modelInstance?.powerNameplate}" >&nbsp;

				<span id="PowerDesignSpanId">${tds.rackPower(power: modelInstance?.powerDesign)}</span>
				<input type="hidden" id="powerDesignId" value="${modelInstance?.powerDesign}" >&nbsp;

				<span id="powerSpanId">${tds.rackPower(power: modelInstance?.powerUse)}</span>
				<input type="hidden" id="powerUseId" value="${modelInstance?.powerUse}" >&nbsp;
				<g:select id="powertype" name='powerType' value="${tds.powerType()}" from="${['Watts','Amps']}" onchange="updatePowerType( this.value , this.name)"> </g:select>
            </td>
        	<td>Notes:</td>
			<td>${modelInstance?.description}</td>
        </tr>
        <!--<tr>
            <td>Front image:</label></td>
        	<td>
	        	<g:if test="${modelInstance?.frontImage}">
    	    	<img id="mainImg" src="${createLink(controller:'model', action:'retrieveFrontImage', id:modelInstance.id)}" style="height: 50px;width: 100px;"/>
        		</g:if>
            </td>
        	<td>Rear image:</td>
        	<td>
        		<g:if test="${modelInstance?.rearImage}">
        		<img src="${createLink(controller:'model', action:'retrieveRearImage', id:modelInstance.id)}"  style="height: 50px;width: 100px;" id="rearImageId"/>
        		</g:if>
        	</td>
        </tr>-->
		<tr style="display: ${modelInstance.assetType == 'Blade Chassis' ? 'block' : 'none'}">
			<td>Blade Rows:</td>
			<td>${modelInstance?.bladeRows}</td>
		</tr>
		<tr style="display: ${modelInstance.assetType == 'Blade Chassis' ? 'block' : 'none'}">
			<td>Blade Count:</td>
			<td>${modelInstance?.bladeCount}</td>
		</tr>
		<tr style="display: ${modelInstance.assetType == 'Blade Chassis' ? 'block' : 'none'}">
			<td>Blade Label Count:</td>
			<td>${modelInstance?.bladeLabelCount}</td>
		</tr>
		<tr style="display: ${modelInstance.assetType == 'Blade' ? 'block' : 'none'}">
			<td>Blade Height:</td>
			<td>${modelInstance?.bladeHeight}</td>
		</tr>
		<tr>
		    <td valign="top" class="name">
                <label for="description">Room Object:</label>
            </td>
            <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'roomObject','errors')}">
                <input type="checkbox" id="roomObject" name="roomObject" ${modelInstance.roomObject ? 'checked="checked"' : ''} disabled="disabled" />
            </td>
	       <!-- <td>Use Image:</td>
	        <td>
		        <input type="checkbox" name="useImage" id="useImageId" ${modelInstance.useImage ? 'checked="checked"' : ''} disabled="disabled"/>
	        </td>-->
        </tr>
		<tr>
		    <td>Created By :</td>
			<td>${modelInstance?.createdBy}</td>
        	<td>Source TDS:</td>
	        <td>
		        <g:if test="${modelInstance.sourceTDS}">
		        	<input type="checkbox" name="sourceTDS" id="sourceTDSId" checked="checked" disabled="disabled"/>
		        </g:if>
		        <g:else>
		       	 <input type="checkbox" name="sourceTDS" id="sourceTDSId" disabled="disabled"/>
		        </g:else>
	        </td>
		</tr>
		<tr>
			<td>Updated By:</td>
			<td>${modelInstance?.updatedBy}</td>
		    <td>Source URL:</td>
			<td><div class="ellipsis"><tds:textAsLink text="${modelInstance?.sourceURL}" target="_new" /></div></td>
		</tr>
		<tr>
		    <td>Validated By:</td>
			<td>${modelInstance?.validatedBy}</td>
			<td>Model Status:</td>
			<td>${modelInstance?.modelStatus}</td>
		</tr>
	</tbody>
</table>
</div>
<div style="float: left;">
	<div>
		<div id="cablingPanel" style="min-height: 70px;">
			<g:if test="${modelInstance.rearImage && modelInstance.useImage == 1}">
			<img src="${createLink(controller:'model', action:'retrieveRearImage', id:modelInstance.id)}" />
			<script type="text/javascript">
					$("#cablingPanel").css("background-color","#FFF")
				</script>
			</g:if>
			<g:else>
				<script type="text/javascript">
					var usize = "${modelInstance.usize}";
					usize = (usize=="")?1:usize;
					$("#cablingPanel").css("height", (usize*30) + "px" )
				</script>
			</g:else>
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
				<div id="sConnector${i}" style="position: relative; float: left; margin-right: 20px;">

					<div>
					<span>${modelConnector.label}</span>
					</div>
				</div>
			</g:each>
		</div>
	</div>
	<div style="clear: both;"></div>
	<div class="list" style="border: 1px solid #5F9FCF;margin-bottom: 10px;margin-right: 5px;">
		<table style="border: 0px;">
			<thead>
				<tr>
					<th>Type</th>
					<th>Label</th>
					<th>Label Position</th>
					<th>Conn Pos X</th>
					<th>Conn Pos Y</th>
				</tr>
			</thead>
			<tbody id="connectorModelBody">
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
				<tr id="sConnectorTr${i}"  class="${(i % 2) == 0 ? 'odd' : 'even'}">
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
	<tr>
			<td colspan="2">
				<div class="buttons" style="margin-left: 10px;margin-right: 10px;">
					<tds:hasPermission permission="${Permission.ModelEdit}">
						<g:form action="update" >
							<input name="id" value="${modelInstance.id}" type="hidden"/>
							 <input type="hidden" name="redirectTo" value="${redirectTo}" />
							<span class="button">

							   <g:if test="${redirectTo=='modelDialog'}">
									<span class="button"><input type="button" class="edit" value="Edit"
											onclick="showOrEditModelManuDetails('model',${modelInstance?.id},'Model','edit','Edit')" /></span>
									<g:if test="${modelRef}">
										<g:actionSubmit class="delete disableButton" action="delete" value="Delete"  disabled="disabled" onclick="return validateModelDependency(${modelInstance.id})"></g:actionSubmit>
									</g:if>
									<g:else>
										<g:actionSubmit class="delete" action="delete" value="Delete"></g:actionSubmit>
									</g:else>
							   </g:if>
							   <g:else>
									<g:actionSubmit class="edit" action="edit" value="Edit"></g:actionSubmit>
									<g:if test="${modelRef}">
										<g:actionSubmit class="disableButton delete" action="delete" value="Delete"  disabled="disabled"
										 onclick="return validateModelDependency(${modelInstance.id})"></g:actionSubmit>
									</g:if>
									<g:else>
										<g:actionSubmit class="delete" action="delete" value="Delete"></g:actionSubmit>
									</g:else>
							   </g:else>
							</span>
						</g:form>
					</tds:hasPermission>
				</div>
			</td>
		</tr>
</div>
<div id="editModelView" style="display: none;"></div>
</fieldset>
<script type="text/javascript">
$('div.connector_Left').each(function(index) {
	$(this).attr("style","margin-left:-"+$(this).children().width()+"px");
});

function validateModelDependency( modelId ){
	var returnValue = true
	jQuery.ajax({
		url: "/checkModelDependency",
		data: "modelId="+modelId,
		type:'POST',
		async:false,
		success: function(data) {
			if(data != 'false'){
				if( !confirm("Asset cabling data will be impacted, Do you want to proceed..") )
					returnValue = false
			}
		}
	});
	return returnValue
}

function updatePowerType(value,name){
	if(value=="Watts" && name =="powerType"){
		$("#powerSpanId").html($('#powerUseId').val());
		$("#namePlatePowerSpanId").html($('#powerNameplateId').val());
		$("#PowerDesignSpanId").html($('#powerDesignId').val());
	}
	else if(value=="Amps" && name == "powerType"){
		var preference= $('#powerUseId').val()/120;
		$("#powerSpanId").html(preference.toFixed(1));

		preference= $('#powerNameplateId').val()/120;
		$("#namePlatePowerSpanId").html(preference.toFixed(1));

		preference= $('#powerDesignId').val()/120;
		$("#PowerDesignSpanId").html(preference.toFixed(1));
	}
	${remoteFunction(controller:'project', action:'setPower', params:'\'p=\' + value ')}
}
function updatePage(){
	window.location.reload();
}
</script>
</div>
<script>
	currentMenuId = "#adminMenu";
	$('.menu-list-models').addClass('active');
	$('.menu-parent-admin').addClass('active');
</script>
</body>
</html>
