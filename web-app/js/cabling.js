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