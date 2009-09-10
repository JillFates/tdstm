<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />
<jq:plugin name="jquery.scrollfollow" />
<g:javascript src="assetcrud.js" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
<script type="text/javascript">
	$(document).ready(function() {
	    $("#editDialog").dialog({ autoOpen: false })
	})
	function openAssetEditDialig( id ){
		$("#editFormId").val(id)
		${remoteFunction(controller:"assetEntity", action:"editShow", params:'\'id=\' + id ', onComplete:'showAssetEditDialog( e )')}
	}
	function showAssetEditDialog( e ) {
		var browser=navigator.appName;
      	var assetEntityAttributes = eval('(' + e.responseText + ')');
      	var autoComp = new Array()      			
      	var editDiv = document.getElementById("editDiv");
      	var etb = document.getElementById('editTbodyId')
		if(etb != null){
			editDiv.removeChild(etb)
		}
      	var etbody = document.createElement('table');
		etbody.id = "editTbodyId"
		// Rebuild the select
		if (assetEntityAttributes) {
			var length = assetEntityAttributes.length
			var halfLength = getLength(length) 
			var etr = document.createElement('tr');
			var etdLeft = document.createElement('td');
			etdLeft.style.border = '0'
			var etdRight = document.createElement('td');
			etdRight.style.border = '0'
			etdRight.style.verticalAlign = 'top'
			var etableLeft = document.createElement('table');
			etableLeft.style.width = '50%'
			etableLeft.style.border = '0'
			var etableRight = document.createElement('table');
			etableRight.style.width = '50%'
			etableRight.style.border = '0'
			for (var i=0; i < halfLength; i++ ) {
				var attributeLeft = assetEntityAttributes[i]
				var etrLeft = document.createElement('tr');
				// td for Edit page
				var inputTdELeft = document.createElement('td');
				var labelTdELeft = document.createElement('td');
				labelTdELeft.noWrap = 'nowrap'
				inputTdELeft.style.border = '0'
				labelTdELeft.style.border = '0'
				var labelELeft = document.createTextNode(attributeLeft.label);
				labelTdELeft.appendChild( labelELeft )
				var inputFieldELeft = getInputType(attributeLeft);
				inputFieldELeft.value = attributeLeft.value;
				inputFieldELeft.id = 'edit'+attributeLeft.attributeCode+'Id';
				inputTdELeft.appendChild( inputFieldELeft )
				labelTdELeft.style.background = '#f3f4f6 '
				labelTdELeft.style.width = '25%'
				inputTdELeft.style.width = '25%'
				etrLeft.appendChild( labelTdELeft )
				etrLeft.appendChild( inputTdELeft )
				etableLeft.appendChild( etrLeft )
			}
				      	
			for (var i=halfLength; i < length; i++ ) {
				var attributeRight = assetEntityAttributes[i]
				var etrRight = document.createElement('tr');
				// td for Edit page
				var inputTdERight = document.createElement('td');
				var labelTdERight = document.createElement('td');
				labelTdERight.noWrap = 'nowrap'
				inputTdERight.style.border = '0'
				labelTdERight.style.border = '0'
				var labelERight = document.createTextNode(attributeRight.label);
				labelTdERight.appendChild( labelERight )
				var inputFieldERight = getInputType(attributeRight);
				inputFieldERight.value = attributeRight.value;
				inputFieldERight.id = 'edit'+attributeRight.attributeCode+'Id';
				inputTdERight.appendChild( inputFieldERight )
				labelTdERight.style.background = '#f3f4f6 '
				labelTdERight.style.width = '25%'
				inputTdERight.style.width = '25%'
				etrRight.appendChild( labelTdERight )
				etrRight.appendChild( inputTdERight )
				etableRight.appendChild( etrRight )
			}
			for (var i=0; i < length; i++ ) {
				var attribute = assetEntityAttributes[i]
				if(attribute.frontendInput == 'autocomplete'){
					autoComp.push(attribute.attributeCode)
				}
			}
			etdLeft.appendChild( etableLeft )
			etdRight.appendChild( etableRight )
			etr.appendChild( etdLeft )
			etr.appendChild( etdRight )
			etbody.appendChild( etr )
		}
		
		editDiv.appendChild( etbody )
		if(browser == 'Microsoft Internet Explorer') {
			editDiv.innerHTML += "";
		} 
			    
		${remoteFunction(action:'getAutoCompleteDate', params:'\'autoCompParams=\' + autoComp ', onComplete:'updateAutoComplete(e)')} 
		$("#editDialog").dialog('option', 'width', 'auto')
		$("#editDialog").dialog('option', 'position', ['center','top']);
		$("#editDialog").dialog("open")
		$("#showDialog").dialog("close")
	}
	function showEditAsset(e) {
		var assetEntityAttributes = eval('(' + e.responseText + ')')
		if (assetEntityAttributes != "") {
			$("#editDialog").dialog("close")
		} else {
			alert("Asset Entity is not updated")
		}
	}
</script>
<title>Rack Elevation Report</title>
</head>
<body>
<div class="body">
<g:if test="${rackLayout}">
<g:each in="${rackLayout}" var="rackLayout">
	<g:if test="${frontView}">
	<table cellpadding=2 class="rack_elevation">
		<tr>
			<td colspan="13" style="border:0px;"><h2>Room: ${rackLayout?.room} - Rack: ${rackLayout?.rack}</h2></td>
		</tr>
		<tr>
			<th>U</th>
			<th>Device</th>
			<th>Bundle</th>
			<th>U</th>
		</tr>
		${rackLayout?.frontViewRows}
	</table>
	<br class="page-break-after"/>
	</g:if>
	<g:if test="${backView}">
	<table cellpadding=2 class="rack_elevation">
		<tr>
			<td colspan="13" style="border:0px;"><h2>Room: ${rackLayout?.room} - Rack: ${rackLayout?.rack}</h2></td>
		</tr>
		<tr>
			<th>U</th>
			<th>Device</th>
			<th>Bundle</th>
			<th>Cabling</th>
			<th>U</th>
			<th>Pos</th>
			<th>PDU</th>
			<th>NIC</th>
			<th>Mgmt</th>
			<th>KVM</th>
			<th>Fiber</th>
			<th>Amber</th>
			<th>OK</th>
		</tr>
		${rackLayout?.backViewRows}
	</table>
	<br class="page-break-after"/>
	</g:if>
</g:each>
</g:if>
<g:else>
<table><tr><td class="no_records">No reports found</td></tr></table>
</g:else>
</div>
<div id="editDialog" title="Edit Asset Entity" style="display: none;">
	<g:form method="post" name="editForm">
		<input type="hidden" name="id" id="editFormId" value="" />
		<div class="dialog" id="editDiv">
		</div>
		<div class="buttons">
			<span class="button">
				<input class="save" type="button" style="font-size: 12px;" value="Update Asset Entity" onClick="${remoteFunction(controller:'assetEntity', action:'getAssetAttributes', params:'\'assetId=\' + $(\'#editFormId\').val() ', onComplete:'callUpdateDialog(e)')}" />
			</span>
		</div>
	</g:form>
</div>
</body>
</html>
