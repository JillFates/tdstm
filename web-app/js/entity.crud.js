function createEntityView(e, type,source,rack,roomName,location,position){
	 if(!isIE7OrLesser) 
		getHelpTextAsToolTip(type);
	 var resp = e.responseText;
	 $("#createEntityView").html(resp);
	 $("#createEntityView").dialog('option', 'width', 'auto')
	 $("#createEntityView").dialog('option', 'position', ['center','top']);
	 $("#createEntityView").dialog('open');
	 $("#editEntityView").dialog('close');
	 $("#showEntityView").dialog('close');
	 updateAssetTitle(type);
	 updateAssetInfo(source,rack,roomName,location,position,'create')
}

function createAssetDetails(type){
	switch(type){
	 case "Application":
		new Ajax.Request(contextPath+'/application/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Application');}})
		break;
	 case "Database":
		new Ajax.Request(contextPath+'/database/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Database');}})
		break;
	 case "Files":
		new Ajax.Request(contextPath+'/files/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Storage');}})
		break;
	 default :
		new Ajax.Request(contextPath+'/assetEntity/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Server');}})
	 }
}

function getEntityDetails(redirectTo, type, value){
	 switch(type){
	 case "Application":
		new Ajax.Request(contextPath+'/application/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Application');}})
		break;
	 case "Database":
		new Ajax.Request(contextPath+'/database/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Database');}})
		break;
	 case "Files":
		new Ajax.Request(contextPath+'/files/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Storage');}})
		break;
	 default :
		new Ajax.Request(contextPath+'/assetEntity/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Server');}})
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
		 updateAssetTitle(type)
		 if(!isIE7OrLesser)
			 getHelpTextAsToolTip(type);
     }
}
var title = document.title;
function changeDocTitle( newTitle ){
	$(document).attr('title', newTitle);
	$(document).keyup(function(e) {     
	    if(e.keyCode== 27) {
	    	$(document).attr('title', title);
	    } 
	});
	$(".ui-dialog .ui-dialog-titlebar-close").click(function(){
		$(document).attr('title', title);
	});
	$( "#deps" ).tooltip({
		 position: {
			my: "center bottom-20",
			at: "center top",
			using: function( position, feedback ) {
				$( this ).css( position );
				$( "<div>" )
				.addClass( "arrow" )
				.addClass( feedback.vertical )
				.addClass( feedback.horizontal )
					 .appendTo( this );
			}
		 }
	});
}

function editEntity(redirectTo,type, value, source,rack,roomName,location,position){
	if(redirectTo == "rack"){
		redirectTo = $('#redirectTo').val() == 'rack' ? 'rack' : $('#redirectTo').val()
	}
	 switch(type){
		 case "Application":
			new Ajax.Request(contextPath+'/application/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Application',source,rack,roomName,location,position);}})
			break;
		 case "Database":
			new Ajax.Request(contextPath+'/database/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Database',source,rack,roomName,location,position);}})
			break;
		 case "Files":
			new Ajax.Request(contextPath+'/files/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Storage',source,rack,roomName,location,position);}})
			break;
		 default :
			 new Ajax.Request(contextPath+'/assetEntity/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Server',source,rack,roomName,location,position);}})
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
	 if(!isIE7OrLesser)
		 getHelpTextAsToolTip(type);
	 updateAssetTitle(type)
	 if(rack)
		 updateAssetInfo(source,rack,roomName,location,position,'edit')
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
	
	var rowNo = $("#"+forWhom+"_"+type+"AddedId").val()
	var rowData = $("#assetDependencyRow tr").html()
		.replace(/dataFlowFreq/g,"dataFlowFreq_"+type+"_"+rowNo)
		.replace(/asset/g,"asset_"+type+"_"+rowNo)
		.replace(/dependenciesId/g,"dep_"+type+"_"+rowNo+"_"+forWhom)
		.replace(/dtype/g,"dtype_"+type+"_"+rowNo)
		.replace(/status/g,"status_"+type+"_"+rowNo)
		.replace(/bundles/g,"moveBundle_"+type+"_"+rowNo)
		.replace(/entity/g,"entity_"+type+"_"+rowNo)
		.replace(/aDepComment/g,"comment_"+type+"_"+rowNo)
		.replace(/dep_comment/g,"dep_comment_"+type+"_"+rowNo)
		.replace(/depComment/g,"depComment_"+type+"_"+rowNo)
		.replace(/commLink/g,"commLink_"+type+"_"+rowNo);
	$("#comment_"+type+"_"+rowNo).val('')
	$("#dep_comment_"+type+"_"+rowNo).val('')
	if(type!="support"){
		$("#"+forWhom+"DependentsList").append("<tr id='row_d_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow(\'row_d_"+rowNo+"', 'edit_dependentAddedId')\"><span class='clear_filter'>X</span></a></td></tr>")
	} else {
		$("#"+forWhom+"SupportsList").append("<tr id='row_s_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow('row_s_"+rowNo+"', 'edit_supportAddedId')\"><span class='clear_filter'>X</span></a></td></tr>")
	}
	$("#dep_"+type+"_"+rowNo+"_"+forWhom).addClass("assetSelect");
	$("#"+forWhom+"_"+type+"AddedId").val(parseInt(rowNo)-1)
	
	if(!isIE7OrLesser)
		$("select.assetSelect").select2();
	
	$("#depComment_"+type+"_"+rowNo).dialog({ autoOpen: false})
}

function deleteRow( rowId, forWhomId ){
	$("#"+rowId).remove()
	var id = rowId.split('_')[3]
	if(id)
		$("#deletedDepId").val(( $("#deletedDepId").val() ? $("#deletedDepId").val()+"," : "") + id)
	else
		$("#"+forWhomId).val(parseInt($("#"+forWhomId).val())+1)
}

function updateAssetsList(name, assetType, assetId ) {
	var idValues = name.split("_")
	var csc = $("select[name='entity_"+idValues[1]+"_"+idValues[2]+"']")
	var claz = csc.val()
	var asc = $("select[name='asset_"+idValues[1]+"_"+idValues[2]+"']")
	asc.removeAttr('onmousedown')
	asc.html($("#"+claz+" select").html())
	console.log("in updateAssetsList name="+name+", claz="+claz+", assetType="+assetType+", assetId="+assetId)

	if(assetId === undefined){
		if(!isIE7OrLesser)
	    	$("select.assetSelect").select2()
	}
	// Set the value if we were passing in the original value for a pre-existing asset
	if ( 
	  (claz == 'Application' && assetType == claz) ||
	  (claz == 'Database' && assetType == claz) ||
	  (claz == 'Other' && assetType == claz) ||
	  (claz == 'Storage' && ( assetType == claz || assetType == 'Files')) || 
	  (claz == 'Server' && assetType!='Application' && assetType!='Database' && assetType!='Other' && assetType!='Storage' && assetType!='Files') 
	) {
		// relookup the SELECT
	    // $("select[name='asset_"+idValues[1]+"_"+idValues[2]+"'] option=[value='"+assetId+"']").attr('selected','selected')
		asc.val(assetId)
		//jQuery("select#selectBox option[value='requiredValue']").attr("selected",selected");
		//console.log("updateAssetsList() setting select to assetId " + assetId)
		//console.log(asc.html())
	}
}
function updateAssetTitle( type ){
	$("#createEntityView").dialog( "option", "title", 'Create '+type );
	$("#showEntityView").dialog( "option", "title", type+' Detail' );
	$("#editEntityView").dialog( "option", "title", 'Edit '+type );
}
function selectManufacturer(value, forWhom){
	var val = value;
	manipulateFields(val)
	new Ajax.Request(contextPath+'/assetEntity/getManufacturersList?assetType='+val+'&forWhom='+forWhom,{asynchronous:true,evalScripts:true,onComplete:function(e){showManufacView(e, forWhom);}})
}
function showManufacView(e, forWhom){
    var resp = e.responseText;
    if(forWhom == 'Edit')
    	$("#manufacturerEditId").html(resp);
    else 
    	$("#manufacturerCreateId").html(resp);
   
    $("#manufacturers").removeAttr("multiple")
    if(!isIE7OrLesser)
    	$("select.assetSelect").select2()
}
function selectModel(value, forWhom){
	var val = value;
	var assetType = $("#assetType"+forWhom+"Id").val() ;
	new Ajax.Request(contextPath+'/assetEntity/getModelsList?assetType='+assetType+'&manufacturer='+val+'&forWhom='+forWhom,{asynchronous:true,evalScripts:true,onComplete:function(e){showModelView(e, forWhom);}})
	//${remoteFunction(action:'getModelsList', params:'\'assetType=\' +assetType +\'&=\'+ val', onComplete:'showModelView(e)' )}
}
function showModelView(e, forWhom){
    var resp = e.responseText;
    $("#model"+forWhom+"Id").html(resp);
    $("#models").removeAttr("multiple")
    if(forWhom == "assetAudit"){
    	$("#models").attr("onChange","editModelAudit(this.value)")
    }
    if(!isIE7OrLesser){
    	$("select.assetSelect").select2()
    }
}
function showComment(id , action){
	   var id = id
	   if(action =='edit'){
	   new Ajax.Request(contextPath+'/assetEntity/showComment?id='+id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog(e, 'edit');commentChangeShow();}})
	   }else{
		   new Ajax.Request(contextPath+'/assetEntity/showComment?id='+id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog(e, 'show');commentChangeShow();}})
	   }
}
function validateFileFormat(form){
	var fileFlag = false;
    var size = $('#'+form+' #size').val();
    if( size=='' || isNaN(size)){
   	  alert("Please enter numeric value for Storage Size");
    }else if($('#'+form+' #fileFormat').val()==''){
   	  alert("Please enter value for Storage Format");
    }else{
   	  fileFlag = true;
    }
  return fileFlag
}
function validateDbFormat(form){
	var dbFlag = false;
    var size = $('#'+form+' #size').val();
    if( size=='' || isNaN(size)){
   	  alert("Please enter numeric value for DB Size");
    }else if($('#'+form+' #dbFormat').val()==''){
   	  alert("Please enter value for DB Format");
    }else{
    	dbFlag = true;
    }
  return dbFlag
}
function submitRemoteForm(){
		jQuery.ajax({
			url: $('#editAssetsFormId').attr('action'),
			data: $('#editAssetsFormId').serialize(),
			type:'POST',
			success: function(data) {
				var assetName = $("#assetName").val()
				$('#editEntityView').dialog('close')
				$('#items1').html(data);
				$("#messageId").html( "Entity "+assetName+" Updated." )
				$("#messageId").show()
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while updating asset.")
			}
		});
 		return false;
}
function deleteAsset(id,value){
	var redirectTo = 'dependencyConsole'
	if(value=='server'){
		new Ajax.Request(contextPath+'/assetEntity/delete?id='+id+'&dstPath='+redirectTo,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
			$('#editEntityView').dialog('close');
			$('#showEntityView').dialog('close');
			$('#items1').html(data.responseText);
			}
		})
	}else if(value=='app'){
		new Ajax.Request(contextPath+'/application/delete?id='+id+'&dstPath='+redirectTo,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
			$('#editEntityView').dialog('close');
			$('#showEntityView').dialog('close');
			$('#items1').html(data.responseText);
			}
		})
	}else if(value=='database'){
		new Ajax.Request(contextPath+'/database/delete?id='+id+'&dstPath='+redirectTo,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
			$('#editEntityView').dialog('close');
			$('#showEntityView').dialog('close');
			$('#items1').html(data.responseText);
			}
		})
	}else {
		new Ajax.Request(contextPath+'/files/delete?id='+id+'&dstPath='+redirectTo,{asynchronous:true,evalScripts:true,
			onComplete:function(data){
			$('#editEntityView').dialog('close');
			$('#showEntityView').dialog('close');
			$('#items1').html(data.responseText);
			}
		})
	}
	
}
function submitCheckBox(){
	var moveBundleId = $("#planningBundleSelectId").val();
	var data = $('#checkBoxForm').serialize() + "&bundle="+moveBundleId;
	new Ajax.Request(contextPath+'/moveBundle/generateDependency?'+data,{asynchronous:true,evalScripts:true,
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
			}, onFailure: function() { 
				alert("Please associate appropriate assets to one or more 'Planning' bundles before continuing"); 
			}
		});
}
var isFirst = true;
function selectAll(){
	var totalCheck = $("input[name=checkBox]");
	if($('#selectId').is(":checked")){
		for(i=0;i<totalCheck.size();i++){
			totalCheck[i].checked = true;
		}
		isFirst = false;
	} else {
		for(i=0;i<totalCheck.size();i++){
			totalCheck[i].checked = false;
		}
		isFirst = true;
	}
}
function changeMoveBundle(assetType,totalAsset,assignBundle){
	if(!assignBundle){
		$("#saveBundleId").attr("disabled", "disabled");
	}
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
		$('#plannedMoveBundleList').val(assignBundle);
		$('#bundleSession').val(assignBundle);
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
function updateToShow($me, forWhom){
	var act = $me.data('action')
	var type = 'Server'
	var redirect = $me.data('redirect')
	if(act=='close')
		$('#updateView').val('closeView')
	else
		$('#updateView').val('updateView')
	var flag=true
	if(forWhom=='app'){
		type = 'Application'
		flag = validateFields('Edit','editAssetsFormId')
	}
	if(forWhom=='files'){
		type = 'Storage'
		flag = validateFileFormat('editAssetsFormId')
	}
	if(forWhom=='database'){
		type = 'Database'
		flag = validateDbFormat('editAssetsFormId')
	}
	if(flag!=false)
		flag = validateDependencies('editAssetsFormId')
	if(flag){
		jQuery.ajax({
			url: $('#editAssetsFormId').attr('action'),
			data: $('#editAssetsFormId').serialize(),
			type:'POST',
			success: function(data) {
				if(data.errMsg){
					alert(data.errMsg)
				}else{
					if(act=='close'){
						$('#editEntityView').dialog('close')
						$('#messageId').show();
						$('#messageId').html(data);
						if($('.ui-icon-refresh').length)
							$('.ui-icon-refresh').click();
					}else{
						$('#editEntityView').dialog('close')
						if(redirect == 'room')
							getRackLayout( $('#selectedRackId').val() )
						$('#showEntityView').html(data)
						$("#showEntityView").dialog('option', 'width', 'auto')
						$("#showEntityView").dialog('option', 'position', ['center','top']);
						$("#showEntityView").dialog('open');
					}
					changeDocTitle(title)
					if(!isIE7OrLesser) 
						getHelpTextAsToolTip(type);
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				var err = jqXHR.responseText
				alert("An unexpected error occurred while updating Asset."+ err.substring(err.indexOf("<span>")+6, err.indexOf("</span>")))
			}
		});
	}
	
}

function updateToRefresh(){
	jQuery.ajax({
		url: $('#editAssetsFormId').attr('action'),
		data: $('#editAssetsFormId').serialize(),
		type:'POST',
		success: function(data) {
			$('#editEntityView').dialog('close')
			$("#taskMessageDiv").html(data)
			$("#taskMessageDiv").show()
			loadGrid();
		}
	});
}

function selectAllAssets(){
	$('#deleteAsset').attr('disabled',false)
	var totalCheck = document.getElementsByName('assetCheckBox');
	if($('#selectAssetId').is(":checked")){
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

function deleteAssets(action){
	var assetArr = new Array();
    $(".cbox:checkbox:checked").each(function(){
        var assetId = $(this).attr('id').split("_")[2]
		if(assetId)  
			assetArr.push(assetId)
  })
  	if(!assetArr){
		alert('Please select the Asset');
	}else{
		if(confirm("You are about to delete all of the selected assets for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel.")){
			jQuery.ajax({
			url:contextPath+'/assetEntity/deleteBulkAsset',
			data: {'assetLists':assetArr,'type':action},
			type:'POST',
			success: function(data) {
					$(".ui-icon-refresh").click();
					$("#messageId").show();
					$("#messageId").html(data.resp);
					$('#deleteAssetId').attr('disabled',true)
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
	new Ajax.Request(contextPath+'/assetEntity/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,
		onComplete:function(e){
			$("#auditDetailViewId").html(e.responseText)
			$("#auditDetailViewId").show()
		}}
	)
}

function editAudit(redirectTo, source, assetType, value){
	new Ajax.Request(contextPath+'/assetEntity/edit?id='+value+'&redirectTo='+redirectTo+'&source='+source+'&assetType='+assetType,
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
				getRackLayout( $('#selectedRackId').val() )
				$("#auditDetailViewId").html(data)
			}
		}
	});
}

function deleteAudit(id,value){
	new Ajax.Request(contextPath+'/assetEntity/delete?id='+id+'&dstPath=assetAudit',{asynchronous:true,evalScripts:true,
		onComplete:function(data){
				$("#auditDetailViewId").hide()
				window.location.reload()
		}}
	)
}

function showModelAudit(id){
	new Ajax.Request(contextPath+'/model/show?id='+id+'&redirectTo=assetAudit',{asynchronous:true,evalScripts:true,
		onComplete:function(data){
				$("#modelAuditId").html(data.responseText)
				$("#modelAuditId").show()
		}}
	)
	
}

function editModelAudit(val){
	if(val){
		var manufacturer = $("#manufacturersAuditId").val()
		new Ajax.Request(contextPath+'/model/getModelDetailsByName?modelName='+val+'&manufacturerName='+manufacturer,{asynchronous:true,evalScripts:true,
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
	new Ajax.Request(contextPath+'/assetEntity/create?redirectTo=assetAudit'+'&assetType='+type+'&source='+source,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
				$("#auditDetailViewId").html(data.responseText)
				$("#auditLocationId").val(location)
				$("#auditRoomId").val(roomName)
				$("#assetTypeCreateId").val(type)
				$(".bladeLabel").hide()
				$(".rackLabel").show()
				if(source==0 && type!='Blade'){
					$("#auditLocationId").attr("name","targetLocation")
					$("#auditRoomId").attr("name","targetRoom")
					$("#auditRackId").attr("name","targetRack")
					$("#auditPosId").attr("name","targetRackPosition")
					$("#sourceId").val("0")
					$("#targetRack").val(rack)
					$("#targetRackPosition").val(position)
				} else if (source=="1" && type!='Blade'){
					$("#sourceRack").val(rack)
					$("#sourceRackPosition").val(position)
				}				
				$("#auditDetailViewId").show()
		}}
	)
}

function createBladeAuditPage(source,blade,position,manufacturer,assetType,assetEntityId, moveBundleId){
	new Ajax.Request(contextPath+'/assetEntity/create?redirectTo=assetAudit'+'&assetType='+assetType+'&source='+source,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
				$("#auditDetailViewId").html(data.responseText)
				$("#BladeChassisId").val(blade)
				$("#bladePositionId").val(position)
				$("#assetTypeCreateId").val(assetType)
				$("#moveBundleId").val(moveBundleId)
				$("#sourceId").val(source)
				$(".bladeLabel").show()
				$(".rackLabel").hide()
				$("#auditDetailViewId").show()
		}}
	)
}

function saveAuditPref(val, id){
	new Ajax.Request(contextPath+'/room/show?id='+id+'&auditView='+val,{asynchronous:true,evalScripts:true,
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
						url: contextPath+'/manufacturer/autoCompleteManufacturer',
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
							url: contextPath+'/model/autoCompleteModel',
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
	new Ajax.Request(contextPath+'/model/getModelType?value='+val,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
			$("#assetTypeEditId").val(data.responseText)
			editModelAudit(""+val+"")
		}}
	)
}

function setType(id, forWhom){
	new Ajax.Request(contextPath+'/assetEntity/getAssetModelType?id='+id,{asynchronous:true,evalScripts:true,
		onComplete:function(data){
			$("#assetType"+forWhom+"Id").val(data.responseText)
			if(!isIE7OrLesser)
				$("select.assetSelect").select2()
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

function populateDependency(assetId, whom, thisDialog){
	$(".updateDep").attr('disabled','disabled')
		jQuery.ajax({
			url: contextPath+'/assetEntity/populateDependency',
			data: {'id':assetId,'whom':thisDialog},
			type:'POST',
			success: function(data) { 
				$("#"+whom+"DependentId").html(data)
				$(".updateDep").removeAttr('disabled')
				if(!isIE7OrLesser)
					$("select.assetSelect").select2();
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while populating dependent asset.")
			}
		});
}

function showTask(selected){
	if(selected =='1'){
		 $('.resolved').show();
	     $('#showEntityView').dialog('option', 'height', 'auto')
	} else {
		 $('.resolved').hide();
		 $('#showEntityView').dialog('option', 'height', 'auto')
	}
	new Ajax.Request(contextPath+'/assetEntity/setShowAllPreference?selected='+selected,{asynchronous:true,evalScripts:true})

}
/*function updateModel(rackId,value){
	var val = value;
	new Ajax.Request('contextPath+/assetEntity/getModelsList?='+val,{asynchronous:true,evalScripts:true,onComplete:function(e){populateModelSelect(e,rackId);}})
}
function populateModelSelect(e,rackId){
    var resp = e.responseText;
    resp = resp.replace("model.id","model_"+rackId+"").replace("Unassigned","Select Model")
    $("#modelSpan_"+rackId).html(resp);
}*/

function showDependencyControlDiv(){
	$("#checkBoxDiv").dialog('option', 'width', '350px')
	$("#checkBoxDiv").dialog('option', 'position', ['center','top']);
	$("#checkBoxDiv").dialog('open')
	$("#checkBoxDivId").show();
}

// Sets the field importance style classes in the edit and create views for all asset classes
function assetFieldImportance(phase,type){
	jQuery.ajax({
		url: contextPath+'/assetEntity/getassetImportance',
		data: {'validation':phase, 'type':type},
		type:'POST',
		success: function(resp) {
			$("td,input,select").removeClass("C")
			$("td,input,select").removeClass("H")
			$("td,input,select").removeClass("I")
			$("td,input,select").removeClass("N")
			for (var key in resp) {
				var value = resp[key]
				$(".dialog input[name="+key+"],select[name="+key+"],input[name='"+key+".id'],select[name='"+key+".id']").addClass(value);
				$(".dialog label[for="+key+"],label[for="+key+"Id]").parent().addClass(value);
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while getting asset.")
		}
	});
	
}

function getHelpTextAsToolTip(type){
	jQuery.ajax({
		url: contextPath+'/common/getTooltips',
		data: {'type':type},
		type:'POST',
		success: function(resp) {
			for (var key in resp) {
					var value = resp[key]
					$(".dialog input[name="+key+"],input[name='"+key+".id']" ).tooltip({ position: {my: "left top"} });
					$(".dialog label[for="+key+"],label[for="+key+"Id]").tooltip({ position: {my: "left top"} });
					$(".dialog input[name="+key+"],input[name='"+key+".id']").attr("title",value);
					$(".dialog label[for="+key+"],label[for="+key+"Id]").attr("title",value);
					
					$(".dialog label[for="+key+"]").closest('td').next('td').tooltip({ position: {my: "left top"} });
					$(".dialog label[for="+key+"]").closest('td').next('td').attr("title",value);
				}
			},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while getting asset.")
		}
	});
}

function saveToShow($me, forWhom){
	var act = $me.data('action')
	var type = 'Server'
	if($me.data('redirect'))
		var redirect = $me.data('redirect').split("_")[0]
	if(act=='close'){
		$('#showView').val('closeView')
	}else{
		$('#showView').val('showView')
	}
	var flag=true
	if(forWhom=='Application'){
		flag = validateFields('','createAssetsFormId')
		type = 'Application'
	}
	if(forWhom=='Files'){
		flag = validateFileFormat('createAssetsFormId')
		type='Storage'
	}
	if(forWhom=='Database'){
		flag = validateDbFormat('createAssetsFormId')
		type = 'Database'
	}
	if(flag!=false)
		flag = validateDependencies('createAssetsFormId')
		
	if(flag){
		jQuery.ajax({
			url: $('#createAssetsFormId').attr('action'),
			data: $('#createAssetsFormId').serialize(),
			type:'POST',
			success: function(data) {
				if(data.errMsg){
					alert(data.errMsg)
				}else{
					if(act=='close'){
						$('#createEntityView').dialog('close')
						if($('.ui-icon-refresh').length)
							$('.ui-icon-refresh').click();
						$('#messageId').show();
						$('#messageId').html(data);
					}else{
						$('#createEntityView').dialog('close')
						if($('.ui-icon-refresh').length)
							$('.ui-icon-refresh').click();
						if(redirect=='room')
							getRackLayout( $('#selectedRackId').val() )
						$('#showEntityView').html(data)
						$("#showEntityView").dialog('option', 'width', 'auto')
						$("#showEntityView").dialog('option', 'position', ['center','top']);
						$("#showEntityView").dialog('open');
						updateAssetTitle(forWhom);
						if(!isIE7OrLesser) 
							getHelpTextAsToolTip(type);
					}
					changeDocTitle(title)
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				var err = jqXHR.responseText
				alert("An unexpected error occurred while creating Asset.")
			}
		});
	}
}

function validateFields(forWhom,formName){
    var flag = true
	if($("#sme1"+forWhom).val()=='0' || $("#sme2"+forWhom).val()=='0' || $("#appOwner"+forWhom).val()=='0' ){
		flag = false
		alert("Please De-select 'Add-Person' Option from sme , sme2 or appOwner select")
		return flag
	} else if (isNaN($("#shutdownDuration"+forWhom).val()) || isNaN($("#startupDuration"+forWhom).val()) || isNaN($("#testingDuration"+forWhom).val())){
		flag = false
		alert("Please enter numeric value for Shutdown Duration, Startup Duration, Testing Duration ")
		return flag
	} 
	return flag
}

/**
 * function is used to make hard assgined check box enabled - disabled based on criteria
 * @param value : value of select
 * @param gid : id of select
 */
function changeHard(value, gid){
	if(value.indexOf('@')==0){
		$("#"+gid+"Fixed").removeAttr("checked").attr("disabled", "disabled").val(0);
	}else {
		$("#"+gid+"Fixed").removeAttr("disabled");
	}
}

function shufflePerson(sFrom,sTo){
	var sFromVal=$("#"+sFrom).val()
	var sToVal=$("#"+sTo).val()
	if(sFromVal!='0' && sToVal!='0'){
		$("#"+sFrom).val(sToVal)
		$("#"+sTo).val(sFromVal)
		if(!isIE7OrLesser)
			$("select.assetSelect").select2();
	}
}

function changeMovebundle(assetId, depId, assetBundelId){
	var splittedDep = depId.split("_")
	jQuery.ajax({
		url: contextPath+'/assetEntity/getChangedBundle',
		data: {'assetId':assetId, 'dependentId':splittedDep[2], 'type':splittedDep[1]},
		type:'POST',
		success: function(resp) {
			$("#moveBundle_"+splittedDep[1]+"_"+splittedDep[2]).val(resp.id)
			changeMoveBundleColor(depId,assetBundelId,resp.id,'')
		}
	});
}

function changeMoveBundleColor(depId,assetId,assetBundleId, status){
	var splittedDep = depId.split("_")
	var bundleObj = $("#moveBundle_"+splittedDep[1]+"_"+splittedDep[2])
	var status = status != '' ? status : $("#status_"+splittedDep[1]+"_"+splittedDep[2]).val()
	var assetId = assetId != '' ? assetId : bundleObj.val()
	bundleObj.removeAttr("class").removeAttr("style")
	
	if(assetId != assetBundleId && status == 'Validated'){
		bundleObj.css('background-color','red')
	} else {
		if(status != 'Questioned' && status != 'Validated')
			bundleObj.addClass('dep-Unknown')
		else
			bundleObj.addClass('dep-'+status)
	}
}

$(document).ready(function() {
	$(window).keydown(function(event){
		if(event.keyCode == 13) {
			event.preventDefault();
			var activeSup = $('[id^=depComment_support_]:visible')
			var activeDep = $('[id^=depComment_dependent_]:visible')
			// NOTE : Order of the condition is MOST important as different div's open on another div
			if(activeSup.find(".save").length > 0){
				activeSup.find(".save").click()
				$('.ui-dialog').focus()
			} else if(activeDep.find(".save").length > 0){
				activeDep.find(".save").click()
				$('.ui-dialog').focus()
			} else if($("#updateCloseId").length > 0){
				$("#updateCloseId").click();
			} else if($("#updatedId").length > 0) {
				$("#updatedId").click();
			}
		
		}
		$("[id^=gs_]").keydown(function(event){
			$(".clearFilterId").removeAttr("disabled");
		});
		
	});
});

function toogleRoom(value, source){
	if( value == '-1' )
		$(".newRoom"+source).show()
	else
		$(".newRoom"+source).hide()
}

function getRacksPerRoom(value, type, assetId, forWhom,rack){
	jQuery.ajax({
		url: contextPath+'/assetEntity/getRacksPerRoom',
		data: {'roomId':value,'sourceType':type ,'assetId':assetId,'forWhom':forWhom},
		type:'POST',
		success: function(resp) {
			console.log('success');
			$("#rack"+type+"Id"+forWhom).html(resp);
			var myOption = "<option value='-1'>Add Rack...</option>"
			$("#rack"+type+"Id"+forWhom+" option:first").after(myOption);
			if(rack){
				$("#rack"+type+"Id"+forWhom).val(rack);
			}
			if(!isIE7OrLesser)
				$("select.assetSelect").select2();
		}
	});
}

function toogleRack(value, source){
	if( value == '-1' )
		$(".newRack"+source).show()
	else
		$(".newRack"+source).hide()
}
function openCommentDialog(id){
	 var type = id.split("_")[1]
	 var rowNo = id.split("_")[2]
	 $("#"+id).dialog('option', 'width', 'auto')
	 $("#"+id).dialog('option', 'position', 'absolute');
	 $("#"+id).dialog('option', 'title', type+" Comment");
	 $("#"+id).dialog('open');
	 if(!$("#comment_"+type+"_"+rowNo).val()){
		 $("#comment_"+type+"_"+rowNo).val('')
		 $("#dep_comment_"+type+"_"+rowNo).val('')
	 } else {
		 $("#dep_comment_"+type+"_"+rowNo).val($("#comment_"+type+"_"+rowNo).val())
	 }
}

function saveDepComment(textId, hiddenId, dialogId, commLink){
	$("#"+hiddenId).val($("#"+textId).val())
	$("#"+dialogId).dialog("close")
	if($("#"+hiddenId).val()){
		$("#"+commLink).html('<img border="0px" src="'+contextPath+'/i/db_table_bold.png">')
	}else {
		$("#"+commLink).html('<img border="0px" src="'+contextPath+'/i/db_table_light.png">')
	}
}
function validateDependencies(formName){
	var flag = true
	$('#'+formName+' select[name*="asset_"]').each( function() {
		if( $(this).val() == 'null' )
			flag = false
			return flag
	})
	if(flag==false)
		alert("Please select a valid asset for all dependencies ")
	return flag
}
function changeBundleSelect(){
	if($("#plannedMoveBundleList").val()){
		$("#saveBundleId").removeAttr("disabled");
	}else {
		$("#saveBundleId").attr("disabled", "disabled");
	}
}
function setColumnAssetPref(value,key, type){
	jQuery.ajax({
		url: contextPath+'/application/columnAssetPref',
		data: {'columnValue':value,'from':key,'previousValue':$("#previousValue_"+key).val(),'type':type},
		type:'POST',
		success: function(resp) {
			console.log('success');
			if(resp){
				if( type!='Task_Columns')
					window.location.reload()
				else
					submitForm()
			}
		}
	});
}
var columnPref=''
function showSelect(column, type, key){
	if(column!=columnPref){
		$("#"+type+"IdGrid_"+column).append($("#columnCustomDiv_"+column).html());
	}
	$(".columnDiv_"+key).show();
	columnPref=column
}

$(document).click(function(e){
	var customizeCount = $("#customizeFieldCount").val()
	if(!customizeCount)
		customizeCount = 5;
	
	for(var i=1;i<customizeCount;i++){
		if($(".columnDiv_"+i+":visible").length){
		    if (!$(e.target).is(".editSelectimage_"+i)) {
		    	$(".columnDiv_"+i).hide();
		    }
		}
	}
});

var lastScroll = 0;
$(window).scroll(function(event){
    //Sets the current scroll position
    var st = $(this).scrollTop();
    //Determines up-or-down scrolling
    if (st > lastScroll){
       //Replace this with your function call for downward-scrolling
    	$(".customScroll").hide();
    }
    else {
       //Replace this with your function call for upward-scrolling
    	$(".customScroll").hide();
    }
    //Updates scroll position
    lastScroll = st;
});

function toggleJustPlanning($me){
	var isChecked= $me.is(":checked")
	jQuery.ajax({
        url:contextPath+'/assetEntity/setImportPerferences',
        data:{'selected':isChecked, 'prefFor':'assetJustPlanning'},
        type:'POST',
		success: function(data) {
			window.location.reload()
		}
    });
}
function clearFilter(gridId){
	$("[id^=gs_]").val('');
	var data = new Object();
	$("[id^=gs_]").each(function(){
		 data[$(this).attr("name")] = '';//{assetName='',appOwner:'',environment:'',....}
	});
	$("#"+gridId+"Grid").setGridParam({postData: data});
	$('.ui-icon-refresh').click();
	$(".clearFilterId").attr("disabled","disabled");
}

