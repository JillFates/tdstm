
function openAssetEditDialig( id ){
	$("#editFormId").val(id)
	 var redirectTo = $('#redirectTo').val() == 'room' ? 'room' : 'rack'
	new Ajax.Request(contextPath+'/assetEntity/edit?id='+id+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetDialog( e , 'edit');}})
}
function showEditAsset(e) {
	var assetEntityAttributes = eval('(' + e.responseText + ')')
	if (assetEntityAttributes != "") {
		$("#editDialog").dialog("close")
		$("#cablingDialogId").dialog("close")
		$('#commit').val('Generate')
		$("#generateId").click()
	} else {
		alert("Asset is not updated, Please check the required fields")
	}
}
function createDialog(source,rack,roomName,location,position){
    var redirectTo = $('#redirectTo').val() == 'room' ? 'room' : 'rack'
    new Ajax.Request(contextPath+'/assetEntity/create?redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e);updateAssetInfo(source,rack,roomName,location,position);}})
  }
function updateAssetInfo(source,rack,roomName,location,position){
	var target = source != '1' ? 'target' : 'source'
	$("#"+target+"RackId").val(rack)
	$("#"+target+"LocationId").val(location)
	$("#"+target+"RoomId").val(roomName)
	$("#"+target+"RackPositionId").val(position)
    
}
function validateAssetEntity(formname) {
	var attributeSet = $("#attributeSetId").val();
	if(attributeSet || formname == 'editForm'){
		var assetName = document.forms[formname].assetName.value.replace(/^\s*/, "").replace(/\s*$/, "");
		
		if( !assetName ){
			alert(" Please Enter Asset Name. ");
			return false;
		} else {
			return true;
		}
	} else {
		alert(" Please select Attribute Set. ");
		return false;
	}
}
function listDialog(assign,sort,order,source,rack,roomName,location,position){
	jQuery.ajax({
		url: contextPath+"/room/getAssetsListToAddRack",
		data: "source="+source+"&rack="+rack+"&roomName="+roomName+"&location="+location+"&position="+position+"&sort="+sort+"&order="+order+"&assign="+assign,
		type:'POST',
		success: function(data) {
			if(data != null && data != ""){
				$("#listDiv").html(data)
				$("#listDialog").dialog('option', 'width', 600)
				$("#listDialog").dialog('option', 'height', 600)
				$("#listDialog").dialog('option', 'position', ['center','top']);
				$("#createDialog").dialog("close")
				$("#listDialog").dialog("open")
			}
		}
	});
}
function editDialog(assetId,source,rack,roomName,location,position){
	openAssetEditDialig(assetId)
	setTimeout("updateEditForm('"+source+"','"+rack+"','"+roomName+"','"+location+"','"+position+"')",1000);
}
function updateEditForm(source,rack,roomName,location,position){
	var target = source != '1' ? 'target' : 'source'
	$("#edit"+target+"RackId").val(rack)
	$("#edit"+target+"LocationId").val(location)
	$("#edit"+target+"RoomId").val(roomName)
	$("#edit"+target+"RackPositionId").val(position)
}
function openSelectedRackLayout(){
	$('#generateId').click()
	setTimeout("$('#'+$('#selectedRackId').val()).click()",1000);
	$("#editDialog").dialog("close")
    $("#createDialog").dialog("close")
    $("#listDialog").dialog("close")
    $("#cablingDialogId").dialog("close")
}
function updateAssetBladeInfo(source,blade,position,manufacturer,moveBundle){
	var target = source != '1' ? 'target' : 'source'
	$("#assetTypeCreateId").val("Blade")	
	$("#"+target+"BladeChassisId").val(blade)
	$("#"+target+"BladePositionId").val(position)
	$("#manufacturer").val(manufacturer)
	$("#moveBundle").val(moveBundle)
}
function listBladeDialog(source,blade,position, assign){
	jQuery.ajax({
		url: contextPath+"/room/getBladeAssetsListToAddRack",
		data: "source="+source+"&blade="+blade+"&position="+position+"&assign="+assign,
		type:'POST',
		success: function(data) {
			if(data != null && data != ""){
				$("#listDiv").html(data)
				$("#listDialog").dialog('option', 'width', 600)
				$("#listDialog").dialog('option', 'position', ['center','top']);
				$("#createDialog").dialog("close")
				$("#listDialog").dialog("open")
			}
		}
	});
}
function editBladeDialog(assetId,source,blade,position, bundleId ){
	openAssetEditDialig(assetId)
	setTimeout("updateBladeEditForm('"+source+"','"+blade+"','"+position+"',"+bundleId+")",1000);
}
function updateBladeEditForm(source,blade,position, bundleId){
	var target = source != '1' ? 'target' : 'source'
	$("#editassetTypeId").val("Blade")	
	$("#edit"+target+"BladeChassisId").val(blade)
	$("#edit"+target+"BladePositionId").val(position)
	$("#editmoveBundleId").val(bundleId)
}
/********************************************************************************************************
 * Cabling script
 *******************************************************************************************************/
var click = 1
function openCablingDiv( assetId ){
	$("#assetEntityId").val(assetId)
	new Ajax.Request(contextPath+'/rackLayouts/getCablingDetails?assetId='+assetId,{asynchronous:true,evalScripts:true,onComplete:function(e){showCablingDetails(e);}})
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
	$("#cablingDialogId").dialog( "option", "title", assetCablingDetails[0].title );
	for(i=0;i<assetCablingDetails.length;i++){
		var assetCabling = assetCablingDetails[i]
		var cssClass = "connector_"+assetCabling.labelPosition
		details += "<div id='connector"+assetCabling.id+"' style='top: "+(assetCabling.connectorPosY / 2)+"px; left: "+assetCabling.connectorPosX+"px;'><a href='#'><div><img id='"+assetCabling.status+"' src='../i/cabling/"+assetCabling.status+".png' onclick=\"openActionButtonsDiv( "+assetCabling.id+", this.id, '"+assetCabling.type+"')\"></div></a><div class='"+cssClass+"'><span>"+assetCabling.label+"</span></div></div>"
		
		tbodyDetails += "<tr id='connectorTr"+assetCabling.id+"'><td title="+assetCabling.status+" onclick=\"openActionButtonsDiv( "+assetCabling.id+", this.title, '"+assetCabling.type+"' )\">"+assetCabling.type+"</td><td title="+assetCabling.status+" onclick=\"openActionButtonsDiv( "+assetCabling.id+", this.title, '"+assetCabling.type+"' )\">"+assetCabling.label+"</td><td title="+assetCabling.status+" onclick=\"openActionButtonsDiv( "+assetCabling.id+", this.title, '"+assetCabling.type+"' )\">"+assetCabling.displayStatus+"</td>"

		if(assetCabling.color != "" ){
			tbodyDetails += "<td id='color_"+assetCabling.id+"' title="+assetCabling.status+" class='"+assetCabling.color+"' onclick=\"openActionButtonsDiv( "+assetCabling.id+", this.title, '"+assetCabling.type+"' )\">&nbsp;</td>"
		} else {
			tbodyDetails += "<td id='color_"+assetCabling.id+"' title="+assetCabling.status+" onclick=\"openActionButtonsDiv( "+assetCabling.id+", this.title, '"+assetCabling.type+"' )\">&nbsp;</td>"
		}
		if(assetCabling.toAssetId != null){
				tbodyDetails += "<td id='connectorTd"+assetCabling.id+"'><a title='"+assetCabling.title+"' style='text-decoration: underline;color:blue;' href='javascript:openCablingDiv("+assetCabling.toAssetId+")'>"+assetCabling.rackUposition+"</a></td></tr>"
		} else {
			tbodyDetails += "<td id='connectorTd"+assetCabling.id+"'>"+assetCabling.rackUposition+"</td></tr>"
		}
	}
	$("#cablingPanel").append(details)
	if( tbodyDetails ){
		$("#cablingTableBody").html( tbodyDetails )
		$('div.connector_Left').each(function(index) {
			$(this).attr("style","margin-left:-"+$(this).children().width()+"px");
		});
		
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
			if($("#connectorTypeId").val() !='Power'){
				cabledDetails = $("#connectorTd"+cabledId+" a").html()
			}
			$("#rackId").val( cabledDetails.substring(0,cabledDetails.indexOf("/")) )
			cabledDetails = cabledDetails.substring(cabledDetails.indexOf("/")+1, cabledDetails.length)
			$("#upositionId").val( cabledDetails.substring(0,cabledDetails.indexOf("/")) )
			$("#connectorId").val( cabledDetails.substring(cabledDetails.indexOf("/")+1, cabledDetails.length) )
			var presetValue = $("#connectorId").val()
			$("#staticConnector_"+presetValue).attr("checked",true)
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
	$("#colorId").val($("#color_"+cabledId).attr("class"))
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
			$("#inputDiv").hide()
			$("#powerDiv").show()
		} else {
			$("#inputDiv").show()
			$("#powerDiv").hide()
		}			
	} else {
		$("#assignFieldDiv").hide()
	}
	if(id == "unknownId" || id == "emptyId"){
		var connectorId = $("#cabledTypeId").val()
		var color = $("#color_"+connectorId).attr("class")
		if(color){
			$("#previousColor").val($("#color_"+connectorId).attr("class"))
			$("#color_"+connectorId).removeAttr("class")
		}
		$("#colorId").val("")
		$("#colorId option").removeAttr("class")
	}
	$("#actionTypeId").val(id)
	$("#actionDiv").show()
	
}
function submitAction(form){
	var actionId = $("#actionTypeId").val()
	var isValid = true
	if(actionId == "assignId"){
		
		if($("#connectorTypeId").val() != 'Power'){

			var rack = $("#rackId").val()
			var uposition = $("#upositionId").val()
			var connector = $("#connectorId").val()
			
			if( !rack || !uposition || !connector){
				isValid = false
				alert("Please enter the target connector details")
			} else {
				if($(".field_error").length){
					isValid = false
					alert("Error with target connector details")
				}
			}
		} else {
			var staticConn = $("input:radio[name=staticConnector]:checked").val()
			if( !staticConn ){
				isValid = false
				alert("Please select the target connector")
			}
		}
	}
	if(isValid){
		jQuery.ajax({
			url: $(form).attr('action'),
			data: $(form).serialize(),
			type:'POST',
			success: function(data) {
				openCablingDiv( data.assetId )
			}
		});
		//${remoteFunction(action:'updateCablingDetails', params:'\'assetCableId=\' + cabledId+\'&status=\'+status' )};
		
		$("#assignFieldDiv").hide()
		$("#actionButtonsDiv").hide()
		$("#actionDiv").hide()
	}
}
function cancelAction(){
	var color = $("#previousColor").val()
	var connectorId = $("#cabledTypeId").val()
	$("#color_"+connectorId).addClass(color)
	$('#assignFieldDiv').hide();
	$('#actionButtonsDiv').hide();
	$('#actionDiv').hide()
}
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
				url: contextPath+"/rackLayouts/getAutoCompleteDetails",
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
		new Ajax.Request(contextPath+'/rackLayouts/getAutoCompleteDetails?field=uposition&rack='+rack,{asynchronous:true,evalScripts:true,onComplete:function(e){updateUpositionData(e);}})
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
				url: contextPath+"/rackLayouts/getAutoCompleteDetails",
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
			new Ajax.Request(contextPath+'/rackLayouts/getAutoCompleteDetails?field=connector&rack='+rack+'&uposition='+uposition,{asynchronous:true,evalScripts:true,onComplete:function(e){updateConnectorData(e);}})
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
					url: contextPath+"/rackLayouts/getAutoCompleteDetails",
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
function updateCell(color){
	var connectorId = $("#cabledTypeId").val()
	if(connectorId){
		$("#color_"+connectorId).removeAttr("class")
		$("#color_"+connectorId).addClass(color)
		$("#colorId option").removeAttr("class")
		$("#colorId option:selected").addClass(color)
	}
}
function assignPowers(id){
	if(confirm("Connect devices to power?")){
		new Ajax.Request($("#contextPath").val()+'/rackLayouts/assignPowers?rackId='+id,{asynchronous:true,evalScripts:true,
			onSuccess:function(e){
				$("#anchor_"+id).html("<img src='../images/power-off.png' />")
			 }})
	}
}
function getAssignedDetails(forWhom, rackId){
	var selectedBundles  = new Array()
	var selectedSourceRoom =  new Array()
	var selectedTargetRoom =  new Array()
	if(forWhom == 'rack'){
		$("#bundleId option:selected").each(function () {
			selectedBundles.push($(this).val())
	   	});
		$("#sourceRackIdSelect option:selected").each(function () {
			selectedSourceRoom.push($(this).val())
	   	});
		$("#targetRackIdSelect option:selected").each(function () {
			selectedTargetRoom.push($(this).val())
	   	});
	} 
	jQuery.ajax({
		url: contextPath+"/rackLayouts/getAssignedCables",
		data: {'moveBundle':selectedBundles, 'sourcerack':selectedSourceRoom, 'targetrack':selectedTargetRoom, 'rackId':rackId},
		type:'POST',
		success: function(data) {
			    if (data) {
				    for(i=0;i<data.rackIds.length;i++){
				    	var rackId = data.rackIds[i]
					    var cssClass = data.data[rackId]
				    	if(cssClass == "Assigned"){
				    		$("#anchor_"+rackId).html("<img src='../images/power-off.png'/>")
					    } else {
					    	$("#anchor_"+rackId).html("<img src='../images/power-icon.png'/>")
						}
				    }
				}
		}
	});
   	
}

