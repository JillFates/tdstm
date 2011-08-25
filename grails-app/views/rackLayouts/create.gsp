<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<g:javascript src="asset.tranman.js" />
<g:javascript src="room.rack.combined.js"/>
<title>Rack View</title>
<script type="text/javascript">
	function updateRackDetails(e) {
     	var rackDetails = eval('(' + e.responseText + ')')   	
      	var sourceSelectObj = $('#sourceRackIdSelect');
      	var targetSelectObj = $('#targetRackIdSelect');
      	var sourceRacks = rackDetails[0].sourceRackList;
      	var targetRacks = rackDetails[0].targetRackList;
      	generateOptions(sourceSelectObj,sourceRacks,'none');
      	generateOptions(targetSelectObj,targetRacks,'all');
      	/* Start with generated default */
      	$('input[value=Generate]').click();
     }
     function generateOptions(selectObj,racks,sel){
     	if (racks) {
			var length = racks.length
			if(sel == 'none')
				selectObj.html("<option value=''>All</option><option value='none' selected='selected'>None</option>");
			else
				selectObj.html("<option value='' selected='selected'>All</option><option value='none'>None</option>");
			
			racks.map(function(e) {
				var locvalue = e.location ? e.location : 'blank';
				var rmvalue = e.room ? e.room : 'blank';
				var ravalue = e.tag ? e.tag : 'blank';
				return({'value':e.id, 'innerHTML':locvalue +"/"+rmvalue+"/"+ ravalue});
			}).sort(function(a, b) {
				var compA = a.innerHTML;
				var compB = b.innerHTML;
				return (compA < compB) ? -1 : (compA > compB) ? 1 : 0;
			}).each(function(e) {
				var option = document.createElement("option");
				option.value = e.value;
				option.innerHTML = e.innerHTML;
				selectObj.append(option);
			});
      	}
     }
     function submitForm(form){
     	if($("#bundleId").val() == 'null') {
     		alert("Please select bundle")
     	} else if( !$("#frontView").is(":checked") && !$("#backView").is(":checked") ) {
     		alert("Please select print view")
     	} else if($('#commit').val() == 'Generate') {
			$("#cablingDialogId").dialog("close")
			$('#rackLayout').html('Loading...');
			jQuery.ajax({
				url: $(form).attr('action'),
				data: $(form).serialize(),
				type:'POST',
				success: function(data) {
					$('#rackLayout').html(data);
				}
			});
	 		return false;
     	}
     }
	$(document).ready(function() {
	    $("#editDialog").dialog({ autoOpen: false })
	    $("#cablingDialogId").dialog({ autoOpen: false })
	    $("#createDialog").dialog({ autoOpen: false })
	    $("#listDialog").dialog({ autoOpen: false })
	    $("#manufacturerShowDialog").dialog({ autoOpen: false })
	    $("#modelShowDialog").dialog({ autoOpen: false })
	})
	// Script to get the combined rack list
	function getRackDetails( objId ){
		var bundles = new Array()
		$("#"+objId+" option:selected").each(function () {
			bundles.push($(this).val())
       	});
       	
		${remoteFunction(action:'getRackDetails', params:'\'bundles=\' +bundles', onComplete:'updateRackDetails(e)')}
	}
    </script>
</head>
<body>
<div class="body" style="width:98%;">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<g:form action="save" name="rackLayoutCreate" method="post" target="_blank" onsubmit="return submitForm(this)" style="border: 1px solid black; width: 100%">
<table style="width:auto; border: none">
	<tbody>
		<tr>
			<td>
				<h1 style="margin: 0px;">Rack View</h1>
				<label><b>Bundle</b></label><br />
				<select id="bundleId" name="moveBundle" multiple="multiple" size="3" onchange="getRackDetails(this.id)" style="width:150px">
					<option value="all" selected="selected">All</option>
					<g:each in="${moveBundleInstanceList}" var="moveBundleList">
						<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
					</g:each>
				</select>
			</td>
			
			<td>
				<label><b>Source</b></label><br />
				<select id="sourceRackIdSelect" multiple="multiple" name="sourcerack" style="width:200px" size="4">
					<option value="null" selected="selected">All</option>
				</select>
			</td>

			<td>
				<div style="width:250px">
					<label><b>Target</b></label><br />
					<select id="targetRackIdSelect" multiple="multiple" name="targetrack" style="width:200px" size="4">
						<option value="null" selected="selected">All</option>
					</select>
				</div>
			</td>
			
			<td>
				<div style="width:150px">
					<label for="frontView" ><input type="checkbox" name="frontView" id="frontView" />&nbsp;Front</label>&nbsp
					<label for="backView" ><input type="checkbox" name="backView" id="backView" checked="checked"/>&nbsp;Back</label><br />
					<label for="bundleName" ><input type="checkbox" name="bundleName" id="bundleName" checked="checked" />&nbsp;w/ bundle names</label><br />
					<label for="otherBundle" ><input type="checkbox" name="otherBundle" id="otherBundle" checked="checked" />&nbsp;w/ other bundles</label><br />
					<label for="showCabling" ><input type="checkbox" name="showCabling" id="showCabling" />&nbsp;w/ diagrams</label><br />
					<label for="hideIcons" ><input type="checkbox" name="hideIcons" id="hideIcons" />&nbsp;w/ Add Icons</label>
				</div>
			</td>
			
			<td class="buttonR">
				<br /><br />
				<input type="hidden" id="commit" name="commit" value="" />
				<input type="submit" class="submit" value="Generate" id="generateId"/>
			</td>

			<td class="buttonR">
				<br/><br/>
				<input type="submit" class="submit" value="Print View" />
			</td>
		</tr>
	</tbody>
</table>
	</g:form>
</div>

<div id="rackLayout" style="width:100%; overflow-x:auto; border: 1px solid black">

</div>
<div id="createDialog" title="Create Asset" style="display: none;">
<g:form action="save" controller="assetEntity" method="post" name="createForm" >

	<div class="dialog" id="createDiv" >
		<table id="createFormTbodyId"></table>
	</div>
	
	<div class="buttons">
	<input type="hidden" name="projectId" value="${projectId }" />
	<input type="hidden" id="attributeSetId" name="attributeSet.id" value="" />
	<input type="hidden" name="redirectTo" value="rack" />
	<span class="button"><input class="save" type="submit" value="Create" onclick="return validateAssetEntity('createForm');" /></span>
	</div>
</g:form></div>
<div id="listDialog" title="Asset List" style="display: none;">
		<div class="dialog" >
			<table id="listDiv">
			</table>
		</div>
</div>
<div id="editDialog" title="Edit Asset" style="display: none;">
	<g:form method="post" name="editForm">
		<input type="hidden" name="id" id="editFormId" value="" />
		<div class="dialog" id="editDiv">
		</div>
		<div class="buttons">
			<span class="button">
				<input class="save" type="button" style="font-size: 12px;" value="Update Asset" onClick="${remoteFunction(controller:'assetEntity', action:'getAssetAttributes', params:'\'assetId=\' + $(\'#editFormId\').val() ', onComplete:'callUpdateDialog(e);openSelectedRackLayout()')}" />
			</span>
		</div>
	</g:form>
</div>
<div style="display: none;" id="cablingDialogId">
	<div id="cablingPanel" style="height: auto; ">
		<g:if test="${currentBundle}">
		<g:each in="${models}" var="model">
			<g:if test="${model?.rearImage && model?.useImage == 1}">
			<img id="rearImage${model.id}" src="${createLink(controller:'model', action:'getRearImage', id:model.id)}" style="display: none;"/>
			</g:if>
		</g:each>
		</g:if>
	</div>
	<div class="inputs_div">
		<g:form action="updateCablingDetails" name="cablingDetailsForm">
		<div id="actionButtonsDiv" style="margin-top: 5px;float: left;display: none;">
			<input type="button" value="Unknown" onclick="openActionDiv(this.id)" id="unknownId"/>
			<input type="button" value="0" onclick="openActionDiv(this.id)" style="background-color: #5F9FCF;" id="emptyId"/>
			<input type="button" value="X" onclick="openActionDiv(this.id)" id="cabledId"/>
			<input type="button" value="Assign" onclick="openActionDiv(this.id)" id="assignId"/>
		</div>
		<div id="actionDiv" style="margin-top: 5px;float: right;display: none;">
			<input type="button" value="Ok" onclick="submitAction($('form[name=cablingDetailsForm]'))"/>
			<input type="button" value="Cancel"  onclick="cancelAction()"/>
			<g:select id="colorId" name="color" from="${AssetCableMap.constraints.color.inList}" noSelection="${['':'']}" onchange="updateCell(this.value)"></g:select>
			<input type="reset" id="formReset" style="display: none;"/>
		</div>
		<div style="clear: both;"></div>
		<div style="text-align: center;margin-bottom: 5px;display: none;" id="assignFieldDiv">
			<div id="inputDiv">
				<input type="text" name="rack" id="rackId" size="10"  onblur="validateRackData( this.value, this.id );"/>
				<input type="text" name="uposition" id="upositionId" size="2" maxlength="2" onfocus="getUpositionData()" onblur="validateUpositionData( this.value, this.id)"/>
				<input type="text" name="connector" id="connectorId" size="15" onfocus="getConnectorData()" onblur="validateConnectorData(this.value, this.id)" />
			</div>
			<div id="powerDiv" style="display: none;">
				<input type="radio" name="staticConnector" id="staticConnector_A" value="A">A</input>&nbsp;
				<input type="radio" name="staticConnector" id="staticConnector_B" value="B">B</input>&nbsp;
				<input type="radio" name="staticConnector" id="staticConnector_C" value="C">C</input>
			</div>
			<div>
				<input type="hidden" name="assetCable" id="cabledTypeId"/>
				<input type="hidden" name="actionType" id="actionTypeId"/>
				<input type="hidden" name="connectorType" id="connectorTypeId"/>
				<input type="hidden" name="asset" id="assetEntityId"/>
				<input type="hidden" id="previousColor"/>
			</div>
		</div>
		
		</g:form>
	</div>
	<div style="clear: both;"></div>
	<div class="list">
		<table>
			<thead>
				<tr>
					<th>Type</th>
					<th>Label</th>
					<th>Status</th>
					<th>Color</th>
					<th>Rack/Upos/Conn</th>
				</tr>
			</thead>
			<tbody id="cablingTableBody">
			<tr>
				<td colspan="5">No Connectors found</td>
			</tr>
			</tbody>
		</table>
	</div>
</div>
<div id="manufacturerShowDialog" title="Show Manufacturer">
	<div class="dialog">
		<table>
	    	<tbody>
				<tr class="prop">
	            	<td valign="top" class="name">Name:</td>
					<td valign="top" class="value" id="showManuName"></td>
				</tr>
	            <tr>
	 				<td valign="top" class="name">AKA:</td>
					<td valign="top" class="value"  id="showManuAka"></td>
				</tr>
	            <tr class="prop">
	            	<td valign="top" class="name">Description:</td>
					<td valign="top" class="value" id="showManuDescription"></td>
				</tr>
			</tbody>
		</table>
	</div>
	<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
	<div class="buttons">
	    <g:form controller="manufacturer" action="edit" target="new">
	        <input type="hidden" name="id" id="show_manufacturerId" />
	        <span class="button"><input type="submit" class="edit" value="Edit" onclick="$('#manufacturerShowDialog').dialog('close')"/></span>
	    </g:form>
	</div>
	</jsec:hasAnyRole >
</div>
<div id="modelShowDialog"  title="Show Model">
<div class="dialog">
<table>
	<tbody>
		<tr>
			<td valign="top" class="name">Manufacturer:</td>
			<td valign="top" class="value" id="showManufacturer"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Model Name:</td>
			<td valign="top" class="value" id="showModelName"></td>
		</tr>
		<tr>
			<td valign="top" class="name">AKA:</td>
			<td valign="top" class="value" id="showModelAka"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Asset Type:</td>
			<td valign="top" class="value" id="showModelAssetType"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Usize:</td>
			<td valign="top" class="value" id="showModelUsize"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Power (typical):</td>
			<td valign="top" class="value" id="showModelPower"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Front image:</label></td>
        	<td valign="top" class="value" id="showModelFrontImage"></td>
		</tr>
		<tr>
        	<td valign="top" class="name">Rear image:</td>
        	<td valign="top" class="value" id="showModelRearImage"></td>
        </tr>
        <tr>
        	<td valign="top" class="name">Use Image:</td>
	        <td valign="top" class="value" id="showModelUseImage"></td>
        </tr>
		<tr id="showModelBladeRowsTr">
			<td valign="top" class="name">Blade Rows:</td>
			<td valign="top" class="value" id="showModelBladeRows"></td>
		</tr>
		<tr id="showModelBladeCountTr">
			<td valign="top" class="name">Blade Count:</td>
			<td valign="top" class="value" id="showModelBladeCount"></td>
		</tr>
		<tr id="showModelBladLabelCountTr">
			<td valign="top" class="name">Blade Label Count:</td>
			<td valign="top" class="value" id="showModelBladLabelCount"></td>
		</tr>
		<tr id="showModelBladeHeightTr">
			<td valign="top" class="name">Blade Height:</td>
			<td valign="top" class="value" id="showModelBladeHeight"></td>
		</tr>
		<tr>
        	<td valign="top" class="name">Source TDS:</td>
	        <td valign="top" class="value" id="showModelSourceTds"></td>
        </tr>
		<tr>
			<td valign="top" class="name">Notes:</td>
			<td valign="top" class="value" id="showModelNotes"></td>
		</tr>
	</tbody>
</table>
</div>
<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
<div class="buttons"> 
	<g:form action="edit" controller="model" target="new">
		<input name="id" type="hidden" id="show_modelId"/>
		<span class="button">
			<input type="submit" class="edit" value="Edit"></input>
		</span>
	</g:form>
</div>
</jsec:hasAnyRole>
</div>

<script type="text/javascript">

	$(document).ready(function() {
		var bundleObj = $("#bundleId");
		var isCurrentBundle = '${isCurrentBundle}'
		var bundleId = 'all';
		if(isCurrentBundle == "true"){
			bundleObj.val('${currentBundle}');
			bundleId = bundleObj.val();
		}
		${remoteFunction(action:'getRackDetails', params:'\'bundles=\' + bundleId', onComplete:'updateRackDetails(e)')};
		
		$('input.submit').click(function() {
			$('#commit').val($(this).val());
		});
	});
	
	/*
		RACK Autocomplete functionality
	*/
	${remoteFunction(action:'getAutoCompleteDetails', params:'\'field=rack\'', onComplete:"updateAutoComplete( e , 'rack')" )};
</script>
</body>
</html>
