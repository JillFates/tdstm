function createEntityView(e, type,source,rack,roomName,location,position){
	 var resp = e.responseText;
	 $("#createEntityView").html(resp);
	 $("#createEntityView").dialog('option', 'width', 'auto')
	 $("#createEntityView").dialog('option', 'position', ['center','top']);
	 $("#createEntityView").dialog('open');
	 $("#editEntityView").dialog('close');
	 $("#showEntityView").dialog('close');
	 updateTitle(type)
	 updateAssetInfo(source,rack,roomName,location,position)
}
function getEntityDetails(redirectTo, type, value){
	 switch(type){
	 case "Application":
		new Ajax.Request('../application/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Application');}})
		break;
	 case "Database":
		new Ajax.Request('../database/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Database');}})
		break;
	 case "Files":
		new Ajax.Request('../files/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Files');}})
		break;
	 default :
		new Ajax.Request('../assetEntity/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Server');}})
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
function editEntity(redirectTo,type, value, source,rack,roomName,location,position){
	if(redirectTo == "rack"){
		redirectTo = $('#redirectTo').val() == 'rack' ? 'rack' : $('#redirectTo').val()
	}
	 switch(type){
		 case "Application":
			new Ajax.Request('../application/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Application',source,rack,roomName,location,position);}})
			break;
		 case "Database":
			new Ajax.Request('../database/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Database',source,rack,roomName,location,position);}})
			break;
		 case "Files":
			new Ajax.Request('../files/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Files',source,rack,roomName,location,position);}})
			break;
		 default :
			 new Ajax.Request('../assetEntity/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Server',source,rack,roomName,location,position);}})
	 }
}
function editEntityView(e, type,source,rack,roomName,location,position){
     var resps = e.responseText;
     $("#editEntityView").html(resps);
	 $("#editEntityView").dialog('option', 'width', 'auto')
	 $("#editEntityView").dialog('option', 'position', ['center','top']);
	 $("#editEntityView").dialog('open');
	 $("#showEntityView").dialog('close');
	 $("#createEntityView").dialog('close');
	 updateTitle(type)
	 updateAssetInfo(source,rack,roomName,location,position)
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
    var resp = e.responseText;
    $("#modelId").html(resp);
    $("#models").removeAttr("multiple")
}
function showComment(id , action){
	   var id = id
	   if(action =='edit'){
	   new Ajax.Request('../assetEntity/showComment?id='+id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog(e, 'edit');}})
	   }else{
		   new Ajax.Request('../assetEntity/showComment?id='+id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog(e, 'show');}})
	   }
}
function validateFileFormat(){
	var fileFlag = false;
    var fileSize = $('#fileSize').val();
    if( fileSize=='' || isNaN(fileSize)){
   	  alert("Please enter numeric value for File Size");
    }else if($('#fileFormat').val()==''){
   	  alert("Please enter value for File Format");
    }else{
   	  fileFlag = true;
    }
  return fileFlag
}
function submitRemoteForm(){
		jQuery.ajax({
			url: $('#editAssetsFormId').attr('action'),
			data: $('#editAssetsFormId').serialize(),
			type:'POST',
			success: function(data) {
				$('#editEntityView').dialog('close')
				$('#items1').html(data);
			}
		});
 		return false;
}
function deleteAsset(id,value){
	var redirectTo = 'planningConsole'
	if(value=='server'){
		new Ajax.Request('../assetEntity/delete?id='+id+'&dstPath='+redirectTo,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
			$('#editEntityView').dialog('close');
			$('#showEntityView').dialog('close');
			$('#items1').html(data.responseText);
			}
		})
	}else if(value=='app'){
		new Ajax.Request('../application/delete?id='+id+'&dstPath='+redirectTo,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
			$('#editEntityView').dialog('close');
			$('#showEntityView').dialog('close');
			$('#items1').html(data.responseText);
			}
		})
	}else if(value=='database'){
		new Ajax.Request('../database/delete?id='+id+'&dstPath='+redirectTo,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
			$('#editEntityView').dialog('close');
			$('#showEntityView').dialog('close');
			$('#items1').html(data.responseText);
			}
		})
	}else {
		new Ajax.Request('../files/delete?id='+id+'&dstPath='+redirectTo,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
			$('#editEntityView').dialog('close');
			$('#showEntityView').dialog('close');
			$('#items1').html(data.responseText);
			}
		})
	}
	
}
function submitCheckBox(){
	var data = $('#checkBoxForm').serialize()
	new Ajax.Request('../moveBundle/generateDependency?'+data,{asynchronous:true,evalScripts:true,
		    onLoading:function(){
		    	var processTab = jQuery('#processDiv');
			    processTab.attr("style", "display:block");
			    processTab.attr("style", "margin-left: 180px");
			    var assetTab = jQuery('#dependencyTableId');
			    assetTab.attr("style", "display:none");
			    assetTab.attr("style", "display:none");
			    jQuery('#items1').css("display","none");
			    $('#upArrow').css('display','none')
		    }, onComplete:function(data){
				$('#dependencyBundleDetailsId').html(data.responseText)
				var processTab = jQuery('#processDiv');
			    processTab.attr("style", "display:none");
			    var assetTab = jQuery('#dependencyBundleDetailsId');
			    assetTab.attr("style", "display:block");
			    $('#upArrow').css('display','inline');
			    $('#downArrow').css('display','none');
			}
		});
}
var isFirst = true;
function selectAll(){
	var totalCheck = document.getElementsByName('checkBox');
	if(isFirst){
	for(i=0;i<totalCheck.length;i++){
	totalCheck[i].checked = true;
	}
	isFirst = false;
	}else{
	for(i=0;i<totalCheck.length;i++){
	totalCheck[i].checked = false;
	}
	isFirst = true;
	}
}
function changeMoveBundle(assetType,totalAsset){
	var assetArr = new Array();
	var j=0;
	for(i=0; i< totalAsset.size() ; i++){
		if($('#checkId_'+totalAsset[i]) != null){
			var booCheck = $('#checkId_'+totalAsset[i]).is(':checked');
			if(booCheck){
				assetArr[j] = totalAsset[i];
				j++;
			}
		}
	}if(j == 0){
		alert('Please select the Asset');
	}else{
		$('#assetsTypeId').val(assetType)
		$('#assetVal').val(assetArr);
		$('#moveBundleSelectId').dialog('open')
	}
}	
function submitMoveForm(){
	jQuery.ajax({
		url: $('#changeBundle').attr('action'),
		data: $('#changeBundle').serialize(),
		type:'POST',
		success: function(data) {
			$('#moveBundleSelectId').dialog("close")
			$('#items1').html(data);
		}
	});
}