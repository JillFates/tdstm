<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<g:javascript library="prototype" />
<jq:plugin name="jquery.combined" />
<g:javascript src="asset.tranman.js" />
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
      	var editDiv = jQuery('#editDiv');
      	var etb = jQuery('#editTbodyId')
		if(etb != null){
			editDiv.remove(etb)
		}
      	var etbody = jQuery(document.createElement('table'));
		etbody.id = "editTbodyId"
		// Rebuild the select
		if (assetEntityAttributes) {
			var length = assetEntityAttributes.length
			var halfLength = getLength(length) 
			var etr = jQuery(document.createElement('tr'));
			var etdLeft = jQuery(document.createElement('td'));
			etdLeft.css('border', '0')
			var etdRight = jQuery(document.createElement('td'));
			etdRight.css('border', '0')
			etdRight.css('verticalAlign', 'top')
			var etableLeft = jQuery(document.createElement('table'));
			etableLeft.css('width', '50%')
			etableLeft.css('border', '0')
			var etableRight = jQuery(document.createElement('table'));
			etableRight.css('width', '50%')
			etableRight.css('border','0')
			for (var i=0; i < halfLength; i++ ) {
				var attributeLeft = assetEntityAttributes[i]
				var etrLeft = jQuery(document.createElement('tr'));
				// td for Edit page
				var inputTdELeft = jQuery(document.createElement('td'));
				var labelTdELeft = jQuery(document.createElement('td'));
				labelTdELeft.attr('noWrap', 'nowrap')
				inputTdELeft.css('border', '0')
				labelTdELeft.css('border', '0')
				var labelELeft = jQuery(document.createTextNode(attributeLeft.label));
				labelTdELeft.append( labelELeft )
				var inputFieldELeft = jQuery(getInputType(attributeLeft, ''));
				inputFieldELeft.value = attributeLeft.value;
				inputFieldELeft.id = 'edit'+attributeLeft.attributeCode+'Id';
				inputTdELeft.append( inputFieldELeft )
				labelTdELeft.css('background','#f3f4f6 ')
				labelTdELeft.css('width', '25%')
				inputTdELeft.css('width', '25%')
				etrLeft.append( labelTdELeft )
				etrLeft.append( inputTdELeft )
				etableLeft.append( etrLeft )
			}
				      	
			for (var i=halfLength; i < length; i++ ) {
				var attributeRight = assetEntityAttributes[i]
				var etrRight = jQuery(document.createElement('tr'));
				// td for Edit page
				var inputTdERight = jQuery(document.createElement('td'));
				var labelTdERight = jQuery(document.createElement('td'));
				labelTdERight.noWrap = 'nowrap'
				inputTdERight.css('border', '0')
				labelTdERight.css('border', '0')
				var labelERight = jQuery(document.createTextNode(attributeRight.label));
				labelTdERight.append( labelERight )
				var inputFieldERight = jQuery(getInputType(attributeRight, ''));
				inputFieldERight.value = attributeRight.value;
				inputFieldERight.id = 'edit'+attributeRight.attributeCode+'Id';
				inputTdERight.append( inputFieldERight )
				labelTdERight.css('background','#f3f4f6 ')
				labelTdERight.css('width', '25%')
				inputTdERight.css('width', '25%')
				etrRight.append( labelTdERight )
				etrRight.append( inputTdERight )
				etableRight.append( etrRight )
			}
			for (var i=0; i < length; i++ ) {
				var attribute = assetEntityAttributes[i]
				if(attribute.frontendInput == 'autocomplete'){
					autoComp.push(attribute.attributeCode)
				}
			}
			etdLeft.append( etableLeft )
			etdRight.append( etableRight )
			etr.append( etdLeft )
			etr.append( etdRight )
			etbody.append( etr )
		}
		
		editDiv.append( etbody )
		if(browser == 'Microsoft Internet Explorer') {
			editDiv.innerHTML += "";
		} 
			    
		${remoteFunction(controller:'assetEntity', action:'getAutoCompleteDate', params:'\'autoCompParams=\' + autoComp ', onComplete:'updateAutoComplete(e)')} 
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
			<td colspan="5" style="border:0px;"><h2>Room: ${rackLayout?.room} - Rack: ${rackLayout?.rack}</h2></td>
		</tr>
		<tr>
			<th>U</th>
			<th>Device</th>
			<th>Bundle</th>
			<th>Cabling</th>
			<th>U</th>
	 	<%--<th>Pos</th>
			<th>PDU</th>
			<th>NIC</th>
			<th>Mgmt</th>
			<th>KVM</th>
			<th>Fiber</th>
			<th>Amber</th>
			<th>OK</th>--%>
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
