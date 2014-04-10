/********************************************************************************************************
 * Cabling script
 *******************************************************************************************************/
var click = 1
function openCablingDiv( assetId , type){
	var defRoomType = $("#roomTypeForCabling").val();
	if(!type && defRoomType=='0'){
		type = 'T'
	}
	
	if(!type){
		type='S'
	}
	new Ajax.Request(contextPath+'/rackLayouts/getCablingDetails?assetId='+assetId+'&roomType='+type,{asynchronous:true,evalScripts:true,onComplete:function(e){showCablingDetails(e,assetId);}})
}
function showCablingDetails( e, assetId ){
	$("#cablingDialogId").html(e.responseText);
	$("#cablingDialogId").dialog( "option", "width", "auto" )
	$("#cablingDialogId").dialog("open")
	$("#assetEntityId").val(assetId)
	$.getScript( "../js/angular/angular.min.js" )
	setTimeout(function(){
		$("#cableTable").show();
	},500);
}

function changeCableStatus(cableId){
	$('#status_'+cableId+' option[value="Cabled"]').attr('selected','selected');
}
function changeCableDetails(value, cableId){
	var scope = angular.element($('#app')).scope();
	if(value=='Empty'){
		$("#assetFromId_"+cableId).val('');
		$("#modelConnectorId_"+cableId).val('');
		 scope.modelConnectors={}
	}else if(value=='Unknown') {
		$("#assetFromId_"+cableId).val('');
		$("#color_"+cableId).val('');
		$("#cableComment_"+cableId).val('');
		$("#cableLength_"+cableId).val('');
		$("#modelConnectorId_"+cableId).val('');
		 scope.modelConnectors={}
	}else{
		$("#color_"+cableId).val(scope.cables[cableId]['color']);
		$("#cableComment_"+cableId).val(scope.cables[cableId]['comment']);
		$("#cableLength_"+cableId).val(scope.cables[cableId]['length']);
		$("#assetFromId_"+cableId).val(scope.cables[cableId]['fromAssetId']);
		scope.modelConnectors = scope.connectors[scope.cables[cableId]['fromAssetId']]
		$("#modelConnectorId_"+cableId).val(scope.cables[cableId]['connectorId'])
	}
	if(!isIE7OrLesser)
    	$("#assetFromId_"+cableId).select2();
}
function assetModelConnectors(value){
	var connectId=$("#cabledTypeId").val();
	if(value!='null'){
		jQuery.ajax({
			url:contextPath+'/rackLayouts/getAssetModelConnectors',
			data: {'value':value,'type':$("#connectorTypeId").val()},
			type:'POST',
			success: function(data) {
				$("#modelConnectorList").html(data)
				if($('#toport_'+connectId).val()){
					$('#modelConnectorId option[value="'+$('#toport_'+connectId).val()+'"]').attr('selected','selected');
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while updating asset.")
			}
		});
	}else{
		alert("Please Select an asset?")
	}
}