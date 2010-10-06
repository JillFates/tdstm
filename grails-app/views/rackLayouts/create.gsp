<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
<g:javascript src="asset.tranman.js" />
<title>Rack View</title>
<script type="text/javascript">
	function updateRackDetails(e) {
     	var rackDetails = eval('(' + e.responseText + ')')   	
      	var sourceSelectObj = $('#sourceRackId');
      	var targetSelectObj = $('#targetRackId');
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
				return({'value':e.id, 'innerHTML':locvalue +"/"+rmvalue +"/"+ ravalue});
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
		jQuery('#editDiv #editTbodyId').remove();
      	var etbody = jQuery(document.createElement('table'));
		etbody.attr('id', "editTbodyId");
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
				inputFieldELeft.attr('value', attributeLeft.value);
				inputFieldELeft.attr('id', 'edit'+attributeLeft.attributeCode+'Id');
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
				labelTdERight.attr('noWrap', 'nowrap')
				inputTdERight.css('border', '0')
				labelTdERight.css('border', '0')
				var labelERight = jQuery(document.createTextNode(attributeRight.label));
				labelTdERight.append( labelERight )
				var inputFieldERight = jQuery(getInputType(attributeRight, ''));
				inputFieldERight.attr('value', attributeRight.value);
				inputFieldERight.attr('id', 'edit'+attributeRight.attributeCode+'Id');
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
			submitForm($('#rackLayoutCreate'));
		} else {
			alert("Asset Entity is not updated")
		}
	}
    </script>
</head>
<body>
<div class="body" style="width:98%;">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="dialog">
<g:form action="save" id="rackLayoutCreate" name="rackLayoutCreate" method="post" target="_blank" onsubmit="return submitForm(this)" style="border: 1px solid black; width: 100%">
<table style="width:auto; border: none">
	<tbody>
		<tr>
			<td>
				<h1>Rack View</h1>
				<label><b>Bundle</b></label><br />
				<select id="bundleId" name="moveBundle" onchange="${remoteFunction(action:'getRackDetails', params:'\'bundleId=\' + this.value', onComplete:'updateRackDetails(e)')}" style="width:150px">
					<option value="null" selected="selected">Please Select</option>
					<g:each in="${moveBundleInstanceList}" var="moveBundleList">
						<option value="${moveBundleList?.id}">${moveBundleList?.name}</option>
					</g:each>
				</select>
			</td>
			
			<td>
				<label><b>Source</b></label><br />
				<select id="sourceRackId" multiple="multiple" name="sourcerack" style="width:200px" size="4">
					<option value="null" selected="selected">All</option>
				</select>
			</td>

			<td>
				<div style="width:250px">
					<label><b>Target</b></label><br />
					<select id="targetRackId" multiple="multiple" name="targetrack" style="width:200px" size="4">
						<option value="null" selected="selected">All</option>
					</select>
				</div>
			</td>
			
			<td>
				<div style="width:150px">
					<label for="frontView" ><input type="checkbox" name="frontView" id="frontView" checked="checked" />&nbsp;Front</label>&nbsp
					<label for="backView" ><input type="checkbox" name="backView" id="backView" />&nbsp;Back</label><br /><br />
					<label for="bundleName" ><input type="checkbox" name="bundleName" id="bundleName" checked="checked" />&nbsp;Include bundle names</label><br /><br />
					<label for="otherBundle" ><input type="checkbox" name="otherBundle" id="otherBundle" checked="checked" />&nbsp;Include other bundles</label>
				</div>
			</td>
			
			<td class="buttonR">
				<br /><br />
				<input type="hidden" id="commit" name="commit" value="" />
				<input type="submit" class="submit" value="Generate" />
			</td>

			<td class="buttonR">
				<br /><br />
				<input type="submit" class="submit" value="Print View" />
			</td>
		</tr>
	</tbody>
</table>
	</g:form>
</div>

<div id="rackLayout" style="width:100%; overflow-x:auto; border: 1px solid black">

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
<script type="text/javascript">
	$(document).ready(function() {
		var bundleObj = $("#bundleId");
		bundleObj.val('${currentBundle}');
		var bundleId = bundleObj.val();
		${remoteFunction(action:'getRackDetails', params:'\'bundleId=\' + bundleId', onComplete:'updateRackDetails(e)')};
		
		$('input.submit').click(function() {
			$('#commit').val($(this).val());
		});
	});
</script>
</body>
</html>
