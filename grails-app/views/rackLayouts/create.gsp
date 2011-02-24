<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
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
	})
	function openAssetEditDialig( id ){
		$("#editFormId").val(id)
		${remoteFunction(controller:"assetEntity", action:"editShow", params:'\'id=\' + id ', onComplete:"showAssetDialog( e , 'edit')")}
	}
	<%--
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
	} --%>
	function showEditAsset(e) {
		var assetEntityAttributes = eval('(' + e.responseText + ')')
		if (assetEntityAttributes != "") {
			$("#editDialog").dialog("close")
			$("#cablingDialogId").dialog("close")
			submitForm($('#rackLayoutCreate'));
		} else {
			alert("Asset Entity is not updated")
		}
	}
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
<g:form action="save" id="rackLayoutCreate" name="rackLayoutCreate" method="post" target="_blank" onsubmit="return submitForm(this)" style="border: 1px solid black; width: 100%">
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
					<label for="backView" ><input type="checkbox" name="backView" id="backView" checked="checked"/>&nbsp;Back</label><br /><br />
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
<div style="display: none;" id="cablingDialogId">
	<div id="cablingPanel" style="height: auto; ">
		<g:if test="${currentBundle}">
		<g:each in="${AssetEntity.findAll('FROM AssetEntity WHERE moveBundle.id = ? GROUP BY model',[Long.parseLong(currentBundle)]) }" var="assetEntity">
			<g:if test="${assetEntity?.model?.rearImage && assetEntity?.model?.useImage == 1}">
			<img id="rearImage${assetEntity.model.id}" src="${createLink(controller:'model', action:'getRearImage', id:assetEntity.model.id)}" style="display: none;"/>
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
			<input type="button" value="Cancel"  onclick="$('#assignFieldDiv').hide();$('#actionButtonsDiv').hide();$('#actionDiv').hide()"/>
			<input type="reset" id="formReset" style="display: none;"/>
		</div>
		<div style="text-align: center;display: none;" id="assignFieldDiv">
			<input type="text" name="rack" id="rackId" size="10"  onblur="validateRackData( this.value, this.id );"/>
			<input type="text" name="uposition" id="upositionId" size="2" maxlength="2" onfocus="getUpositionData()" onblur="validateUpositionData( this.value, this.id)"/>
			<input type="text" name="connector" id="connectorId" size="15" onfocus="getConnectorData()" onblur="validateConnectorData(this.value, this.id)" />
			<input type="hidden" name="assetCable" id="cabledTypeId"/>
			<input type="hidden" name="actionType" id="actionTypeId"/>
			<input type="hidden" name="connectorType" id="connectorTypeId"/>
			<input type="hidden" name="asset" id="assetEntityId"/>
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
<script type="text/javascript">

	var click = 1
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
	function openCablingDiv( assetId, value ){
		$("#assetEntityId").val(assetId)
		${remoteFunction(action:'getCablingDetails', params:'\'assetId=\' + assetId', onComplete:'showCablingDetails(e)')};
		$("#cablingDialogId").dialog( "option", "title", value+" cabling" );
		$("#cablingDialogId").dialog( "option", "width", 400 )
		$("#cablingDialogId").dialog("open")
	}
	function showCablingDetails( e ){
		$("#cablingPanel img").hide()
		$("#cablingPanel div").hide()
		$("#actionButtonsDiv").hide()
		$("#assignFieldDiv").hide()
		$("#actionDiv").hide()
		var assetCablingDetails = eval('(' + e.responseText + ')');
		var model = assetCablingDetails[0].model
		var hasImageExist = assetCablingDetails[0].hasImageExist
		if(!hasImageExist){
			$("#cablingPanel").css("height",assetCablingDetails[0].usize*30+2)
			$("#cablingPanel").css("background-color","LightGreen")
		} else {
			$("#rearImage"+model).show()
			$("#cablingPanel").css("background-color","#FFF")
		}
		var details = ""
		var tbodyDetails = ""
		for(i=0;i<assetCablingDetails.length;i++){
			var assetCabling = assetCablingDetails[i]
			var cssClass = "connector_right"
			if(assetCabling.labelPosition != "Right"){
				cssClass = "connector_bottom"
			}
			details += "<div id='connector"+assetCabling.id+"' style='top: "+(assetCabling.connectorPosY / 2)+"px; left: "+assetCabling.connectorPosX+"px;'><a href='#'><div><img id='"+assetCabling.status+"' src='../i/cabling/"+assetCabling.status+".png' onclick=\"openActionButtonsDiv( "+assetCabling.id+", this.id, '"+assetCabling.type+"')\"></div></a><div class='"+cssClass+"'><span>"+assetCabling.label+"</span></div></div>"
			tbodyDetails += "<tr id='connectorTr"+assetCabling.id+"' title="+assetCabling.status+" onclick=\"openActionButtonsDiv( "+assetCabling.id+", this.title, '"+assetCabling.type+"' )\"><td>"+assetCabling.type+"</td><td>"+assetCabling.label+"</td><td>"+assetCabling.displayStatus+"</td><td id='connectorTd"+assetCabling.id+"'>"+assetCabling.rackUposition+"</td></tr>"
		}
		$("#cablingPanel").append(details)
		if( tbodyDetails ){
			$("#cablingTableBody").html( tbodyDetails )
		}
	}
	function openActionButtonsDiv( cabledId, status, type ){
		$('#formReset').click()
		$("#cabledTypeId").val(cabledId)
		$("#connectorTypeId").val(type)
		$("#actionButtonsDiv").show()
		$("#actionButtonsDiv input").css("background-color", "")
		$("#assignFieldDiv").hide()
		$("#actionDiv").hide()
		switch(status){
			case "missing":
				$("#unknownId").css("background-color", "#5F9FCF")
				break;
			case "cabledDetails":
				$("#assignId").css("background-color", "#5F9FCF")
				var cabledDetails = $("#connectorTd"+cabledId).html()
				$("#rackId").val( cabledDetails.substring(0,cabledDetails.indexOf("/")) )
				cabledDetails = cabledDetails.substring(cabledDetails.indexOf("/")+1, cabledDetails.length)
				$("#upositionId").val( cabledDetails.substring(0,cabledDetails.indexOf("/")) )
				$("#connectorId").val( cabledDetails.substring(cabledDetails.indexOf("/")+1, cabledDetails.length) )
				openActionDiv( 'assignId' )
				break;
			case "cabled":
				$("#cabledId").css("background-color", "#5F9FCF")
				break;
			case "empty":
				$("#emptyId").css("background-color", "#5F9FCF")
				break;
		}
		$("#cablingTableBody tr").css("background","")
		$("#connectorTr"+cabledId).css("background","none repeat scroll 0 0 #7CFE80")
	}
	function openActionDiv( id ){
		$("#unknownId").css("background-color","")
		$("#emptyId").css("background-color","")
		$("#cabledId").css("background-color","")
		$("#assignId").css("background-color","")
		$("#"+id).css("background-color","#5F9FCF");
		if(id == "assignId"){
			$("#assignFieldDiv").show()
			if($("#connectorTypeId").val()=='Power'){
				$("#rackId").hide()
				$("#upositionId").hide()
			} else {
				$("#rackId").show()
				$("#upositionId").show()
			}			
		} else {
			$("#assignFieldDiv").hide()
		}
		$("#actionTypeId").val(id)
		$("#actionDiv").show()
		
	}
	function submitAction(form){
		var actionId = $("#actionTypeId").val()
		var rack = $("#rackId").val()
		var uposition = $("#upositionId").val()
		var connector = $("#connectorId").val()
		var isValid = true
		if(actionId == "assignId"){
			
			if($("#connectorTypeId").val() != 'Power'){
				if( !rack || !uposition || !connector){
					isValid = false
					alert("Please Enter the target connector details")
				} else {
					if($(".field_error").length){
						isValid = false
						alert("Error with target connector details")
					}
				}
			} else {
				if( !connector ){
					isValid = false
					alert("Please Enter the target connector details")
				}
			}
		}
		if(isValid){
			alert($("#assetEntityId").val())
			jQuery.ajax({
				url: $(form).attr('action'),
				data: $(form).serialize(),
				type:'POST',
				success: function(data) {
					openCablingDiv( data.assetId, data.assetTag )
				}
			});
			//${remoteFunction(action:'updateCablingDetails', params:'\'assetCableId=\' + cabledId+\'&status=\'+status' )};
			
			$("#assignFieldDiv").hide()
			$("#actionButtonsDiv").hide()
			$("#actionDiv").hide()
		}
	}
	/*
		RACK Autocomplete functionality
	*/
	${remoteFunction(action:'getAutoCompleteDetails', params:'\'field=rack\'', onComplete:"updateAutoComplete( e , 'rack')" )};
	function updateAutoComplete(e, field){
	  	var data = eval('(' + e.responseText + ')');
	    if (data) {
			var code = field+"Id";
		  	$("#"+code).autocomplete(data,{autoFill:true});
		}
	}
	function validateRackData(value, field){
		if(click == 2){
			click = 1
			if(value){
				jQuery.ajax({
					url: "getAutoCompleteDetails",
					data: "field=isValidRack&value="+value,
					type:'POST',
					success: function(data) {
						if(data.length > 0){
							$("#"+field).removeClass("field_error")
							$("#upositionId").val("")
							$("#upositionId").removeClass("field_error")
							$("#connectorId").val("")
							$("#connectorId").removeClass("field_error")
						} else {
							$("#"+field).addClass("field_error")
						}
					}
				});
			} else {
				alert("Please enter Rack data")	
			}
		} else{
			click = 2
		}
	}
	/*
		Update and validate the Uposition data
	*/
	function getUpositionData(){
		var rack = $("#rackId").val()
		if(rack){
			${remoteFunction(action:'getAutoCompleteDetails', params:'\'field=uposition\'+\'&rack=\'+rack', onComplete:"updateUpositionData( e )" )};
		} else {
			alert("Please enter rack data")
		}
	}
	function updateUpositionData( e ){
		var data = eval('(' + e.responseText + ')');
	    if (data) {
		    var dataArray = new Array()
		    for(i=0;i<data.length;i++){
		    	dataArray[i] = data[i].toString()
		    }
		    $("#upositionId").flushCache( )
		  	$("#upositionId").autocomplete(dataArray,{autoFill:true});
		}
	}
	function validateUpositionData(value, field){
		var rack = $("#rackId").val()
		if(click == 2){
			click = 1
			if(rack){
				if(value){
				jQuery.ajax({
					url: "getAutoCompleteDetails",
					data: "field=isValidUposition&rack="+rack+"&value="+value,
					type:'POST',
					success: function(data) {
						if(data.length > 0){
							$("#"+field).removeClass("field_error")
							$("#connectorId").val("")
							$("#connectorId").removeClass("field_error")
						} else {
							$("#"+field).addClass("field_error")
						}
					}
				});
				} else {
					alert("Please enter Uposition data")	
				}
			} else{
				alert("Please enter rack data")
			}
		} else {
			click = 2
		}
	}
	/*
		Update and validate the Connector data
	*/
	function getConnectorData(){
		if($("#connectorTypeId").val() != 'Power'){
			var rack = $("#rackId").val()
			var uposition = $("#upositionId").val()
			if(rack && uposition){
				${remoteFunction(action:'getAutoCompleteDetails', params:'\'field=connector\'+\'&rack=\'+rack+\'&uposition=\'+uposition', onComplete:"updateConnectorData( e )" )};
			} else {
				alert("Please enter rack data")
			}
		}
	}
	function updateConnectorData( e ){
		var data = eval('(' + e.responseText + ')');
	    if (data) {
		    var dataArray = new Array()
		    for(i=0;i<data.length;i++){
		    	dataArray[i] = data[i].toString()
		    }
		    $("#connectorId").flushCache( )
		  	$("#connectorId").autocomplete(dataArray,{autoFill:true});
		}
	}
	function validateConnectorData(value, field){
		if(click == 2 && $("#connectorTypeId").val() != 'Power'){
			var rack = $("#rackId").val()
			var uposition = $("#upositionId").val() 
			if(rack && uposition){
				if(value){
					jQuery.ajax({
						url: "getAutoCompleteDetails",
						data: "field=isValidConnector&rack="+rack+"&uposition="+uposition+"&value="+value,
						type:'POST',
						success: function(data) {
							if(data.length > 0){
								$("#"+field).removeClass("field_error")
							} else {
								$("#"+field).addClass("field_error")
							}
						}
					});
				} else {
					alert("Please enter Connector data")	
				}
			} else{
				alert("Rack or uposition data missing")
			}
		} else {
			click = 2
		}
	}
	
</script>
</body>
</html>
