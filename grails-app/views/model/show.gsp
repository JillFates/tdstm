<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Model Template</title>
    <g:javascript src="drag_drop.js" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
    <script type="text/javascript">
		$(document).ready(function() {
		   $("#showMergeDialog").dialog({ autoOpen: false })
		})
	</script>
  </head>
  <body>
<div class="body">
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="create" action="create">New Model</g:link></span>
            <span class="menuButton"><g:link class="create" action="create" params="[modelId:modelInstance.id]">New Model(copy this)</g:link></span>
        </div>
<g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
<div style="border: 0px;margin-top: 5px;">
<fieldset>
<legend><b>Model Template</b></legend>
<div style="margin-left: 10px;margin-right: 10px;width: auto;">
<table style="border: 0px;">
	<tbody>
		<tr>
			<td>Manufacturer:</td>
			<td>${modelInstance?.manufacturer?.name}</td>
			<td>Model Name: <a href="#" 
			 onclick="MyGoogle=window.open('http://www.google.com/#sclient=psy-ab&q='+escape('${modelInstance?.manufacturer?.name}'+' '+'${modelInstance?.modelName}'+' specifications'),'MyGoogle','toolbar=yes,location=yes,menubar=yes,scrollbars=yes,resizable=yes'); return false">G?</a>
			</td>
			<td>${modelInstance?.modelName}</td>
		</tr>
		<tr>
			<td>AKA:</td>
			<td>${modelInstance?.aka}</td>
			<td>Asset Type:</td>
			<td>${modelInstance?.assetType}</td>
		</tr>
		<tr>
			<td>Usize:</td>
			<td>${modelInstance?.usize}</td>
			<td>Height (inches):</td>
			<td>${modelInstance?.height}</td>
		</tr>
		<tr>
			<td>Width:</td>
			<td>${modelInstance?.width}</td>
			<td>Depth:</td>
			<td>${modelInstance?.depth}</td>
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
		    <td><tds:convertDate date="${modelInstance?.endOfLifeDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
            <td>End of Life Status:</td>
			<td>${modelInstance?.endOfLifeStatus}</td>
        </tr>
		<tr>
		    <td>Source URL:</td>
			<td>${modelInstance?.sourceURL}</td>
			<td>Model Status:</td>
			<td>${modelInstance?.modelStatus}</td>
		</tr>
		<tr>
			<td>Power Used : <td><span id="powerSpanId">${session.getAttribute("CURR_POWER_TYPE")?.CURR_POWER_TYPE !='Watts' ? modelInstance?.powerUse ? (modelInstance?.powerUse / 110)?.toFloat()?.round(1) : 0.0 : modelInstance?.powerUse}</span>
			<input type="hidden" name="powerUse" id="powerUseId" value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? modelInstance?.powerUse ? (modelInstance?.powerUse / 110 )?.toFloat()?.round(1) : 0.0 : modelInstance?.powerUse}" >&nbsp;
			<g:select id="powertype" name='powerType' value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE }" from="${['Watts','Amps']}" onchange="updatePowerType( this.value , this.name)"> </g:select>
            </td>
           <td> Power Nameplate : <td><span id="namePlatePowerSpanId">${session.getAttribute("CURR_POWER_TYPE")?.CURR_POWER_TYPE !='Watts' ? modelInstance?.powerNameplate ? (modelInstance?.powerNameplate / 110)?.toFloat()?.round(1) : 0.0 : modelInstance?.powerNameplate}</span>
			<input type="hidden" name="powerNameplate" id="powerNameplateId" value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? modelInstance?.powerNameplate ? (modelInstance?.powerNameplate / 110 )?.toFloat()?.round(1) : 0.0 : modelInstance?.powerNameplate}" >&nbsp;
			<g:select id="powerNameplateTypeId" name='powerNameplateType' value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE }" from="${['Watts','Amps']}" onchange="updatePowerType( this.value , this.name)"> </g:select>
            </td>
             
		</tr>
		<tr>
            <td>Power Design: <td><span id="PowerDesignSpanId">${session.getAttribute("CURR_POWER_TYPE")?.CURR_POWER_TYPE !='Watts' ? modelInstance?.powerDesign ? (modelInstance?.powerDesign / 110)?.toFloat()?.round(1) : 0.0 : modelInstance?.powerDesign}</span>
			<input type="hidden" name="powerDesign" id="powerDesignId" value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? modelInstance?.powerDesign ? (modelInstance?.powerDesign / 110 )?.toFloat()?.round(1) : 0.0 : modelInstance?.powerDesign}" >&nbsp;
			<g:select id="powerDesignTypeId" name='powerDesignType' value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE }" from="${['Watts','Amps']}" onchange="updatePowerType( this.value , this.name)"> </g:select>
            </td>
        	<td>Notes:</td>
			<td>${modelInstance?.description}</td>
        </tr>
        <tr>
            <td>Front image:</label></td>
        	<td>
        	<g:if test="${modelInstance.frontImage}">
        	<img src="${createLink(controller:'model', action:'getFrontImage', id:modelInstance.id)}" style="height: 50px;width: 100px;"/>
        	</g:if>
            </td>
        	<td>Rear image:</td>
        	<td>
        	<g:if test="${modelInstance.rearImage}">
        	<img src="${createLink(controller:'model', action:'getRearImage', id:modelInstance.id)}"  style="height: 50px;width: 100px;" id="rearImageId"/>
        	</g:if>
        	</td>
        </tr>	
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
		   <td>Use Image:</td>
	        <td>
		        <g:if test="${modelInstance.useImage}">
		        	<input type="checkbox" name="useImage" id="useImageId" checked="checked" disabled="disabled"/>
		        </g:if>
		        <g:else>
		       	 <input type="checkbox" name="useImage" id="useImageId" disabled="disabled"/>
		        </g:else>
	        </td>
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
		    <td>Created By :</td>
			<td>${modelInstance?.createdBy}</td>
			<td>Updated By:</td>
			<td>${modelInstance?.updatedBy}</td>
		</tr>
		<tr>
		    <td>Validated By:</td>
			<td>${modelInstance?.validatedBy}</td>
		</tr>
	</tbody>
</table>
</div>
<div style="float: left;">
	<div>
		<div id="cablingPanel" style="height: auto; ">
			<g:if test="${modelInstance.rearImage && modelInstance.useImage == 1}">
			<img src="${createLink(controller:'model', action:'getRearImage', id:modelInstance.id)}" />
			<script type="text/javascript">
					$("#cablingPanel").css("background-color","#FFF")
				</script>
			</g:if>
			<g:else>
				<script type="text/javascript">
					var usize = "${modelInstance.usize}"
					$("#cablingPanel").css("height",usize*30)
				</script>
			</g:else>
			<g:each in="${modelConnectors}" status="i" var="modelConnector">
				<div id="connector${i}" style="top:${modelConnector.connectorPosY / 2}px ;left:${modelConnector.connectorPosX}px ">
					<div>
					<img src="../../i/cabling/${modelConnector.status}.png"/>
					</div>
					<div class="connector_${modelConnector.labelPosition}">
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
				<tr id="connectorTr${i}"  class="${(i % 2) == 0 ? 'odd' : 'even'}">
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
	<div id="showMergeDialog" title="Select the item to merge to:" style="display: none;" class="list">
		<table cellpadding="0" cellspacing="0">
			<thead>
				<tr><th>Name</th><th>AKA</th></tr>
			</thead>
            <tbody>
            	<g:each in="${Model.findAll('from Model where id != ? and manufacturer = ?', [modelInstance?.id, modelInstance?.manufacturer])?.sort{it.modelName}}" status="i" var="model">
            		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                     <td valign="top" class="name">
                     	<g:link action="merge" id="${model.id}" params="[fromId:modelInstance?.id]" style="font-weight: ${model.aka ? 'bold' : 'normal'}">
                      	${model.modelName}
                      </g:link>
                     </td>
                     <td valign="top" class="value">${model.aka}</td>
                 </tr>
            	</g:each>
            </tbody>
        </table>
	</div>
	<tr>
			<td colspan="2">
				<div class="buttons" style="margin-left: 10px;margin-right: 10px;"> 
					<g:form action="update" >
						<input name="id" value="${modelInstance.id}" type="hidden"/>
						<span class="button">
							<g:actionSubmit class="edit" action="edit" value="Edit"></g:actionSubmit>
							<span class="button"><input class="create" type="button" value="Merge" onclick="showMergeDialog()"/></span>
							<g:actionSubmit class="delete" action="delete" value="Delete" onclick="return validateModelDependency(${modelInstance.id})"></g:actionSubmit>
							<g:if test="${isAdmin && modelInstance?.modelStatus=='full'}">
							  <input type="button" class="edit" value="Validate" onclick="validateModel(${modelInstance.id})"/>
							</g:if>
							<g:else>
							  <input type="button" class="edit" value="Validate" disabled="disabled" />
							</g:else>
						</span>
					</g:form>
				</div>
			</td>
		</tr>
</div>
</fieldset>
<script type="text/javascript">
$('div.connector_Left').each(function(index) {
	$(this).attr("style","margin-left:-"+$(this).children().width()+"px");
});
function validateModelDependency( modelId ){
	var returnValue = true
	jQuery.ajax({
		url: "../checkModelDependency",
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
function showMergeDialog(){
	$("#showMergeDialog").dialog('option', 'height', 530 )
    $("#showMergeDialog").dialog('open')
}

function updatePowerType(value,name){
	var preference
	if(value=="Watts" && name =="powerType"){
		preference=$('#powerUseId').val()*110;
		preference= preference.toFixed(0)
		$('#powerUseId').val(preference);
		$("#powerSpanId").html(preference);
	}else if(value=="Watts" && name == "powerNameplateType"){
		preference=$('#powerNameplateId').val()*110;
		preference= preference.toFixed(0)
		$('#powerNameplateId').val(preference);
		$("#namePlatePowerSpanId").html(preference);
	}else if(value=="Watts" && name == "powerDesignType"){
		preference=$('#powerDesignId').val()*110;
		preference= preference.toFixed(0)
		$('#powerDesignId').val(preference);
		$("#PowerDesignSpanId").html(preference);
	}
	else if(value=="Amps" && name == "powerType"){
		preference= $('#powerUseId').val()/110;
		preference= preference.toFixed(1)
		$('#powerUseId').val(preference);
		$("#powerSpanId").html(preference);
	}
	else if(value=="Amps" && name == "powerNameplateType"){
		preference= $('#powerNameplateId').val()/110;
		preference= preference.toFixed(1)
		$('#powerNameplateId').val(preference);
		$("#namePlatePowerSpanId").html(preference);
	}
	else {
		preference= $('#powerDesignId').val()/110;
		preference= preference.toFixed(1)
		$('#powerDesignId').val(preference);
		$("#PowerDesignSpanId").html(preference);
	}
	${remoteFunction(controller:'project', action:'setPower', params:'\'p=\' + value ')}
}
function validateModel(id){
    if(confirm("All data in this model is reasonable and valid ?")){
    	${remoteFunction(action:'validateModel', params:'\'id=\' + id ',onComplete="updatePage()")}
    }
}
function updatePage(){
	window.location.reload();
}
</script>
</div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
</body>
</html>
