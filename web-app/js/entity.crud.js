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

function createAssetDetails(type){
	switch(type){
	 case "Application":
		new Ajax.Request('../application/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Application');}})
		break;
	 case "Database":
		new Ajax.Request('../database/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Database');}})
		break;
	 case "Files":
		new Ajax.Request('../files/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Storage');}})
		break;
	 default :
		new Ajax.Request('../assetEntity/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Server');}})
	 }
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
		new Ajax.Request('../files/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Storage');}})
		break;
	 default :
		new Ajax.Request('../assetEntity/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Server');}})
	 }
}
function showEntityView(e, type){
	 if(B2 != ''){
		B2.Pause()
	 }
	 var resp = e.responseText;
	 if(resp.substr(0,1) == '{'){
    	var resp = eval('(' + e.responseText + ')');
   	 	alert(resp.errMsg)
     }else{
		 $("#showEntityView").html(resp);
		 $("#showEntityView").dialog('option', 'width', 'auto')
		 $("#showEntityView").dialog('option', 'position', ['center','top']);
		 $("#showEntityView").dialog('open');
		 $("#editEntityView").dialog('close');
		 $("#createEntityView").dialog('close');
		 updateTitle(type)
     }
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
			new Ajax.Request('../files/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Storage',source,rack,roomName,location,position);}})
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
function addAssetDependency( type,forWhom ){
	var rowNo = $("#"+type+"Count").val()
	var rowData = $("#assetDependencyRow tr").html().replace(/dataFlowFreq/g,"dataFlowFreq_"+type+"_"+rowNo).replace(/asset/g,"asset_"+type+"_"+rowNo).replace(/dtype/g,"dtype_"+type+"_"+rowNo).replace(/status/g,"status_"+type+"_"+rowNo).replace(/entity/g,"entity_"+type+"_"+rowNo)
	if(type!="support"){
		$("#"+forWhom+"DependentsList").append("<tr id='row_d_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow(\'row_d_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
	} else {
		$("#"+forWhom+"SupportsList").append("<tr id='row_s_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow('row_s_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
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
function selectManufacturer(value, forWhom){
	var val = value;
	manipulateFields(val)
	new Ajax.Request('../assetEntity/getManufacturersList?assetType='+val+'&forWhom='+forWhom,{asynchronous:true,evalScripts:true,onComplete:function(e){showManufacView(e, forWhom);}})
}
function showManufacView(e, forWhom){
    var resp = e.responseText;
    if(forWhom == 'Edit')
    	$("#manufacturerEditId").html(resp);
    else 
    	$("#manufacturerCreateId").html(resp);
   
    $("#manufacturers").removeAttr("multiple")
}
function selectModel(value, forWhom){
	var val = value;
	var assetType = $("#assetTypeId").val() ;
	new Ajax.Request('../assetEntity/getModelsList?assetType='+assetType+'&manufacturer='+val+'&forWhom='+forWhom,{asynchronous:true,evalScripts:true,onComplete:function(e){showModelView(e, forWhom);}})
	//${remoteFunction(action:'getModelsList', params:'\'assetType=\' +assetType +\'&=\'+ val', onComplete:'showModelView(e)' )}
}
function showModelView(e, forWhom){
    var resp = e.responseText;
    $("#model"+forWhom+"Id").html(resp);
    $("#models").removeAttr("multiple")
    if(forWhom == "assetAudit"){
    	$("#models").attr("onChange","editModelAudit(this.value)")
    }
}
function showComment(id , action){
	   var id = id
	   if(action =='edit'){
	   new Ajax.Request('../assetEntity/showComment?id='+id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog(e, 'edit');commentChangeShow();}})
	   }else{
		   new Ajax.Request('../assetEntity/showComment?id='+id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog(e, 'show');commentChangeShow();}})
	   }
}
function validateFileFormat(){
	var fileFlag = false;
    var fileSize = $('#fileSize').val();
    if( fileSize=='' || isNaN(fileSize)){
   	  alert("Please enter numeric value for Storage Size");
    }else if($('#fileFormat').val()==''){
   	  alert("Please enter value for Storage Format");
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
	if($('#selectId').attr('checked')==true){
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
			$('#allBundles').attr('checked','false')
			$('#planningBundle').attr('checked','true')
			$("#plannedMoveBundleList").html($("#moveBundleList_planning").html())
		}
	});
}
function updateToShow(){
	$('#updateView').val('updateView')
	jQuery.ajax({
		url: $('#editAssetsFormId').attr('action'),
		data: $('#editAssetsFormId').serialize(),
		type:'POST',
		success: function(data) {
			if(data.errMsg){
				alert(data.errMsg)
			}else{
				$('#editEntityView').dialog('close')
				$('#showEntityView').html(data)
				$("#showEntityView").dialog('option', 'width', 'auto')
				$("#showEntityView").dialog('option', 'position', ['center','top']);
				$("#showEntityView").dialog('open');
			}
		}
	});
	
}
function selectAllAssets(){
	$('#deleteAsset').attr('disabled',false)
	var totalCheck = document.getElementsByName('assetCheckBox');
	if($('#selectAssetId').attr('checked')==true){
	for(i=0;i<totalCheck.length;i++){
	totalCheck[i].checked = true;
	}
	isFirst = false;
	}else{
	for(i=0;i<totalCheck.length;i++){
	totalCheck[i].checked = false;
	$('#deleteAsset').attr('disabled',true)
	}
	isFirst = true;
	}
}
function deleteAssets(list,action){
	var assetArr = new Array();
	var j=0;
	for(i=0; i< list.size() ; i++){
		if($('#checkId_'+list[i]) != null){
			var booCheck = $('#checkId_'+list[i]).is(':checked');
			if(booCheck){
				assetArr[j] = list[i];
				j++;
			}
		}
	}if(j == 0){
		alert('Please select the Asset');
	}else{
			if(confirm("There is no undo! Are you sure you want to delete these ?")){
				var url
				if(action=='server'){
					url='../assetEntity/deleteBulkAsset'
				}else if(action=='application'){
					url='../application/deleteBulkAsset'
				}else if(action=='files'){
					url='../files/deleteBulkAsset'
				}else{
					url='../database/deleteBulkAsset'
				}
				
				jQuery.ajax({
				url: url,
				data: {'assetLists':assetArr},
				type:'POST',
				success: function(data) {
					if(data="success"){
						window.location.reload()
						var totalCheck = document.getElementsByName('assetCheckBox');
						for(i=0;i<totalCheck.length;i++){
							totalCheck[i].checked = false;
							$('#deleteAsset').attr('disabled',true)
						}
						
					}
				}
			});
		}
	}
	
}
function enableButton(list){
	var assetArr = new Array();
	var j=0;
	for(i=0; i< list.size() ; i++){
		if($('#checkId_'+list[i]) != null){
			var booCheck = $('#checkId_'+list[i]).is(':checked');
			if(booCheck){
				assetArr[j] = list[i];
				j++;
			}
		}
	}if(j == 0){
		$('#deleteAsset').attr('disabled',true)
	}else{
		$('#deleteAsset').attr('disabled',false)
	}
}

function getAuditDetails(redirectTo, assetType, value){
	new Ajax.Request('../assetEntity/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,
		onComplete:function(e){
			$("#auditDetailViewId").html(e.responseText)
			$("#auditDetailViewId").show()
		}}
	)
}

function editAudit(redirectTo, source, assetType, value){
	new Ajax.Request('../assetEntity/edit?id='+value+'&redirectTo='+redirectTo+'&source='+source+'&assetType='+assetType,
	{asynchronous:true,evalScripts:true,
		onComplete:function(e){
			$("#auditDetailViewId").html(e.responseText)
			if(source==0){
				$("#auditLocationId").attr("name","targetLocation")
				$("#auditRoomId").attr("name","targetRoom")
				$("#auditRackId").attr("name","targetRack")
				$("#auditPosId").attr("name","targetRackPosition")
			}
			$("#auditDetailViewId").show()
		}}
	)
}

function updateAudit(){
	jQuery.ajax({
		url: $('#editAssetsAuditFormId').attr('action'),
		data: $('#editAssetsAuditFormId').serialize(),
		type:'POST',
		success: function(data) {
			if(data.errMsg){
				alert(data.errMsg)
			}else{
				$("#auditDetailViewId").html(data)
			}
		}
	});
}

function deleteAudit(id,value){
	new Ajax.Request('../assetEntity/delete?id='+id+'&dstPath=assetAudit',{asynchronous:true,evalScripts:true,
		onComplete:function(data){
				$("#auditDetailViewId").hide()
				window.location.reload()
		}}
	)
}

function showModelAudit(id){
	new Ajax.Request('../model/show?id='+id+'&redirectTo=assetAudit',{asynchronous:true,evalScripts:true,
		onComplete:function(data){
				$("#modelAuditId").html(data.responseText)
				$("#modelAuditId").show()
		}}
	)
	
}

function editModelAudit(val){
	if(val){
		var manufacturer = $("#manufacturersAuditId").val()
		new Ajax.Request('../model/getModelDetailsByName?modelName='+val+'&manufacturerName='+manufacturer,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
					$("#modelAuditId").html(data.responseText)
					$("#modelAuditId").show()
					$("#autofillIdModel").hide()
					
			}
		})
	}
}

function updateModelAudit(){
	jQuery.ajax({
		url: $('#modelAuditEdit').attr('action'),
		data: $('#modelAuditEdit').serialize(),
		type:'POST',
		success: function(data) {
			$("#modelAuditId").html(data)
		}
	});
}

function createAuditPage(type,source,rack,roomName,location,position){
	new Ajax.Request('../assetEntity/create?redirectTo=assetAudit'+'&assetType='+type+'&source='+source,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
				$("#auditDetailViewId").html(data.responseText)
				$("#auditLocationId").val(location)
				$("#auditRoomId").val(roomName)
				$("#assetTypeId").val(type)
				$("#auditRackId").val(rack)
				$("#auditPosId").val(position)
				$("#auditLocationId").val(location)
				if(source==0 && type!='Blade'){
					$("#auditLocationId").attr("name","targetLocation")
					$("#auditRoomId").attr("name","targetRoom")
					$("#auditRackId").attr("name","targetRack")
					$("#auditPosId").attr("name","targetRackPosition")
					$("#sourceId").val("0")
				}
				$("#auditDetailViewId").show()
		}}
	)
}

function createBladeAuditPage(source,blade,position,manufacturer,assetType,assetEntityId, moveBundleId){
	new Ajax.Request('../assetEntity/create?redirectTo=assetAudit'+'&assetType='+assetType+'&source='+source,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
				$("#auditDetailViewId").html(data.responseText)
				$("#BladeChassisId").val(blade)
				$("#bladePositionId").val(position)
				$("#assetTypeId").val(assetType)
				$("#moveBundleId").val(moveBundleId)
				$("#sourceId").val(source)
				$("#auditDetailViewId").show()
		}}
	)
}

function saveAuditPref(val, id){
	new Ajax.Request('../room/show?id='+id+'&auditView='+val,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
			openRoomView(data)
		}}
	)
}

var manuLoadRequest
var modelLoadRequest

function getAlikeManu(val) {
 if(manuLoadRequest)manuLoadRequest.abort();
 manuLoadRequest = jQuery.ajax({
						url: '../manufacturer/autoCompleteManufacturer',
						data: {'value':val},
						type:'POST',
						success: function(data) {
							$("#autofillId").html(data)
							$("#autofillId").show()
						}
					});
	 
}

function getAlikeModel(val){
	if(modelLoadRequest)modelLoadRequest.abort()
	var manufacturer= $("#manufacturersAuditId").val()
	modelLoadRequest = jQuery.ajax({
							url: '../model/autoCompleteModel',
							data: {'value':val,'manufacturer':manufacturer},
							type:'POST',
							success: function(data) {
								$("#autofillIdModel").html(data)
								$("#autofillIdModel").show()
							}
						});
}
function updateManu(name){
	$("#manufacturersAuditId").val(name)
	$("#autofillId").hide()
	$("#modelsAuditId").val("")
}

function updateModelForAudit(name){
	$("#modelsAuditId").val(name)
	$("#modelsAuditId").focus()
	$("#autofillIdModel").hide()
	$("#modelsAuditId").attr('onBlur','getAssetType("'+name+'")')
}

function getAssetType(val){
	new Ajax.Request('../model/getModelType?value='+val,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
			$("#assetTypeId").val(data.responseText)
			editModelAudit(""+val+"")
		}}
	)
}

function setType(id){
	new Ajax.Request('../assetEntity/getAssetModelType?id='+id,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
			$("#assetTypeId").val(data.responseText)
			manipulateFields(data.responseText)
		}}
	)	
	
}

function manipulateFields( val ){
	if(val=='Blade'){
		$(".bladeLabel").show()
		$(".rackLabel").hide()
		$(".vmLabel").hide()
	 } else if(val=='VM') {
		$(".bladeLabel").hide()
		$(".rackLabel").hide()
		$(".vmLabel").show()
	} else {
		$(".bladeLabel").hide()
		$(".rackLabel").show()
		$(".vmLabel").hide()
	}
} 

function populateDependency(assetId){
	jQuery.ajax({
		url: '../assetEntity/populateDependency',
		data: {'id':assetId},
		type:'POST',
		success: function(data) {
			$("#editSupportsList").html(data.supports)
			$("#editDependentsList").html(data.dependents)				
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An Unexpected error while populating dependent asset.")
		}
	});
}

/*function updateModel(rackId,value){
	var val = value;
	new Ajax.Request('../assetEntity/getModelsList?='+val,{asynchronous:true,evalScripts:true,onComplete:function(e){populateModelSelect(e,rackId);}})
}
function populateModelSelect(e,rackId){
    var resp = e.responseText;
    resp = resp.replace("model.id","model_"+rackId+"").replace("Unassigned","Select Model")
    $("#modelSpan_"+rackId).html(resp);
}*/