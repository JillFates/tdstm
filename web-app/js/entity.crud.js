function createEntityView(e, type){
	 var resp = e.responseText;
	 $("#createEntityView").html(resp);
	 $("#createEntityView").dialog('option', 'width', 'auto')
	 $("#createEntityView").dialog('option', 'position', ['center','top']);
	 $("#createEntityView").dialog('open');
	 $("#editEntityView").dialog('close');
	 $("#showEntityView").dialog('close');
	 updateTitle(type)
	 //updateAssetInfo(source,rack,roomName,location,position)
}
function getEntityDetails(type, value){
	 switch(type){
	 case "Application":
		new Ajax.Request('../application/show?id='+value,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Application');}})
		break;
	 case "Database":
		new Ajax.Request('../database/show?id='+value,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Database');}})
		break;
	 case "Files":
		new Ajax.Request('../files/show?id='+value,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Files');}})
		break;
	 default :
		new Ajax.Request('../assetEntity/show?id='+value,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Server');}})
	 }
}
function showEntityView(e, type){
	 var resp = e.responseText;
	 $("#showEntityView").html(resp);
	 $("#showEntityView").dialog('option', 'width', 'auto')
	 $("#showEntityView").dialog('option', 'position', ['center','top']);
	 $("#showEntityView").dialog('open');
	 $("#editEntityView").dialog('close');
	 $("#createEntityView").dialog('close');
	 updateTitle(type)
}
function editEntity(type, value){
	 switch(type){
		 case "Application":
			new Ajax.Request('../application/edit?id='+value,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Application');}})
			break;
		 case "Database":
			new Ajax.Request('../database/edit?id='+value,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Database');}})
			break;
		 case "Files":
			new Ajax.Request('../files/edit?id='+value,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Files');}})
			break;
		 default :
			 new Ajax.Request('../assetEntity/edit?id='+value,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Server');}})
	 }
}
function editEntityView(e, type){
     var resps = e.responseText;
     $("#editEntityView").html(resps);
	 $("#editEntityView").dialog('option', 'width', 'auto')
	 $("#editEntityView").dialog('option', 'position', ['center','top']);
	 $("#editEntityView").dialog('open');
	 $("#showEntityView").dialog('close');
	 $("#createEntityView").dialog('close');
	 updateTitle(type)
}
function isValidDate( date ){
    var returnVal = true;
  	var objRegExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d$/;
  	if( date && !objRegExp.test(date) ){
      	alert("Date should be in 'mm/dd/yyyy HH:MM AM/PM' format");
      	returnVal  =  false;
  	} 
  	return returnVal;
}
function addAssetDependency( type ){
	var rowNo = $("#"+type+"Count").val()
	var rowData = $("#assetDependencyRow tr").html().replace("dataFlowFreq","dataFlowFreq_"+type+"_"+rowNo).replace("asset","asset_"+type+"_"+rowNo).replace("dtype","dtype_"+type+"_"+rowNo).replace("status","status_"+type+"_"+rowNo).replace("entity","entity_"+type+"_"+rowNo)
	if(type!="support"){
		$("#createDependentsList").append("<tr id='row_d_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow(\'row_d_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
	} else {
		$("#createSupportsList").append("<tr id='row_s_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow('row_s_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
	}
	$("#"+type+"Count").val(parseInt(rowNo)+1)
}
function deleteRow( rowId ){
	$("#"+rowId).remove()
}
function updateAssetsList( name, value ){
	var idValues = name.split("_")
	$("select[name='asset_"+idValues[1]+"_"+idValues[2]+"']").html($("#"+value+" select").html())
}
function updateTitle( type ){
	$("#createEntityView").dialog( "option", "title", 'Create '+type );
	$("#showEntityView").dialog( "option", "title", 'Show '+type );
	$("#editEntityView").dialog( "option", "title", 'Edit '+type );
}
function selectManufacturer(value){
	var val = value;
	new Ajax.Request('../assetEntity/getManufacturersList?assetType='+val,{asynchronous:true,evalScripts:true,onComplete:function(e){showManufacView(e);}})
	//${remoteFunction(action:'getManufacturersList', params:'\'assetType=\' + val ', onComplete:'showManufacView(e)' )}
	}
function showManufacView(e){
	alert("WARNING : Change of Asset Type may impact on Manufacturer and Model, Do you want to continue ?");
    var resp = e.responseText;
    $("#manufacturerId").html(resp);
    $("#manufacturers").removeAttr("multiple")
}
function selectModel(value){
	var val = value;
	var assetType = $("#assetTypeId").val() ;
	new Ajax.Request('../assetEntity/getModelsList?assetType='+assetType+'&manufacturer='+val,{asynchronous:true,evalScripts:true,onComplete:function(e){showModelView(e);}})
	//${remoteFunction(action:'getModelsList', params:'\'assetType=\' +assetType +\'&manufacturer=\'+ val', onComplete:'showModelView(e)' )}
}
function showModelView(e){
	alert("WARNING : Change of Manufacturer may impact on Model data, Do you want to continue ?")
    var resp = e.responseText;
    $("#modelId").html(resp);
    $("#models").removeAttr("multiple")
}
