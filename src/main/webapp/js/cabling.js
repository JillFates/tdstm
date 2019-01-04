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
	new Ajax.Request(contextPath+'/rackLayouts/retrieveCablingDetails?assetId='+assetId+'&roomType='+type,{asynchronous:true,evalScripts:true,onComplete:function(e){showCablingDetails(e,assetId);}})
}
function showCablingDetails( e, assetId ){
	$("#cablingDialogId").empty();
	$("#cablingDialogId").dialog({ autoOpen:false, modal: true });
	$("#cablingDialogId").dialog( "option", "width", "auto" );

	$("#cablingDialogId").append("<div></div>");
	var currentDiv = $("#cablingDialogId").children().first();

	$("#cablingDialogId").dialog("open");
	$("#assetEntityId").val(assetId)
	//$.getScript( "../js/angular/angular.min.js" )

	$("#cableTable").show();
	currentDiv.html(e.responseText);
}


function assetModelConnectors(value){
	var connectId=$("#cabledTypeId").val();
	if(value!='null'){
		jQuery.ajax({
			url:contextPath+'/rackLayouts/retrieveAssetModelConnectors',
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