/*
 * Javascript functions used by the Asset CRUD forms and Lists
 */

var AssetCrudModule = {

	// Creates a Select2 control for an Asset Name selector
	assetNameSelect2: function(element) {
		element.select2( {
			minimumInputLength: 0,
			initSelection: function (element, callback) {
				var data = { id: element.val(), text: element.data("asset-name")};
				callback(data);
			},
			placeholder: "Please select",
			ajax: {
				url: contextPath+"/assetEntity/assetListForSelect2",
				dataType: 'json',
				quietMillis: 600,
				data: function (term, page) {
					return {
						q: term, 
						max: 25, 
						page: page,
						assetClassOption:$(this).data("asset-type"),
					};
				},
				results: function (data, page) {
					var more = (page * 25) < data.total;
					return { results: data.results , more: more};
				}
			}
		} );
	},

	// Used to change the Select2 Asset Name SELECT when the Class SELECT is changed
	updateDependentAssetNameSelect: function(name) {
		var split = name.split("_");
		var classSelect = $("select[name='entity_"+split[1]+"_"+split[2]+"']");
		var nameSelect = $("input[name='asset_"+split[1]+"_"+split[2]+"']");
		nameSelect.data("asset-type", classSelect.val());
		nameSelect.select2("val", "");
	}, 

	/**
	 * Used to call Ajax Update for given asset form and then load the show view of the asset
	 * @param me - the form
	 * @param forWhom - the asset class of the form (app, files, database)
	 **/
	 performAssetUpdate: function($me, forWhom) {
		var act = $me.data('action');
		var type = 'Server';
		var redirect = $me.data('redirect');

		$('#updateView').val('updateView');

		var validateOkay=true;
		switch(forWhom) {
			case 'app':
				type = 'Application';
				validateOkay = validateAppForm('Edit','editAssetsFormId');
				break;

			case 'files':
				type = 'Storage';
				validateOkay = validateStorageForm('editAssetsFormId');
				break;

			case 'database':
				type = 'Database';
				validateOkay = validateDBForm('editAssetsFormId');
				break;

			case 'server':
				type = 'Server';
				validateOkay = validateDeviceForm('editAssetsFormId');
				break;

			default:
				alert('Unsupported case for ' + forWhom);
		}

		if (validateOkay)
			validateOkay = validateDependencies('editAssetsFormId');

		if (validateOkay) {
			jQuery.ajax({
				url: $('#editAssetsFormId').attr('action'),
				data: $('#editAssetsFormId').serialize(),
				type:'POST',
				success: function(data) {
					if(data.errMsg){
						alert(data.errMsg)
					} else {
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

}

function createEntityView (e, type,source,rack,roomName,location,position) {
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

function createAssetDetails (type) {
	switch(type){
	case "Application":
		new Ajax.Request(contextPath+'/application/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Application');}})
		break;
	case "Database":
		new Ajax.Request(contextPath+'/database/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Database');}})
		break;
	case "Files":
		new Ajax.Request(contextPath+'/files/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Logical Storage');}})
		break;
	default :
		new Ajax.Request(contextPath+'/assetEntity/create',{asynchronous:true,evalScripts:true,onComplete:function(e){createEntityView(e, 'Device');}})
	}
}

function getEntityDetails (redirectTo, type, value) {
	switch (type) {
	case "Application":
		new Ajax.Request(contextPath+'/application/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Application');}})
		break;
	case "Database":
		new Ajax.Request(contextPath+'/database/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Database');}})
		break;
	case "Files":
		new Ajax.Request(contextPath+'/files/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Logical Storage');}})
		break;
	default :
		new Ajax.Request(contextPath+'/assetEntity/show?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){showEntityView(e, 'Device');}})
	}
}

function showEntityView (e, type) {
	
	if (B2 != '') {
		B2.Pause()
	}
	var resp = e.responseText;
	if (resp.substr(0,1) == '{') {
		var resp = eval('(' + e.responseText + ')');
		alert(resp.errMsg)
	} else {
		$("#showEntityView").html(resp);
		$("#showEntityView").dialog('option', 'width', 'auto')
		$("#showEntityView").dialog('option', 'position', ['center','top']);
		$("#showEntityView").dialog('open');
		$("#editEntityView").dialog('close');
		$("#createEntityView").dialog('close');
		updateAssetTitle(type)
		if (!isIE7OrLesser)
			getHelpTextAsToolTip(type);
	}
}

var title = document.title;
function changeDocTitle ( newTitle ) {
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

function editEntity (redirectTo,type, value, source,rack,roomName,location,position) {
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
			new Ajax.Request(contextPath+'/files/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Logical Storage',source,rack,roomName,location,position);}})
			break;
		 default :
			 new Ajax.Request(contextPath+'/assetEntity/edit?id='+value+'&redirectTo='+redirectTo,{asynchronous:true,evalScripts:true,onComplete:function(e){editEntityView(e, 'Device',source,rack,roomName,location,position);}})
	 }
}

function editEntityView (e, type,source,rack,roomName,location,position) {
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

function isValidDate ( date ) {
	var returnVal = true;
	var objRegExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d$/;
	if( date && !objRegExp.test(date) ){
		alert("Date should be in 'mm/dd/yyyy HH:MM AM/PM' format");
		returnVal  =  false;
	}
	return returnVal;
}

// Used to dynamically add a new dependency row to the dependency form table
// @param type - the dependency type [support|dependent]
// @param forWhom - used to indicate edit or create? views
function addAssetDependency (type, forWhom) {
	
	var rowNo = $("#"+forWhom+"_"+type+"AddedId").val();
	var typeRowNo = type+"_"+rowNo;
	var rowData = $("#assetDependencyRow tr").html()
		.replace(/dataFlowFreq/g,"dataFlowFreq_"+typeRowNo)
		.replace(/asset/g,"asset_"+typeRowNo)
		.replace(/dependenciesId/g,"dep_"+typeRowNo+"_"+forWhom)
		.replace(/dtype/g,"dtype_"+typeRowNo)
		.replace(/status/g,"status_"+typeRowNo)
		.replace(/bundles/g,"moveBundle_"+typeRowNo)
		.replace(/entity/g,"entity_"+typeRowNo)
		.replace(/aDepComment/g,"comment_"+typeRowNo)
		.replace(/dep_comment/g,"dep_comment_"+typeRowNo)
		.replace(/depComment/g,"depComment_"+typeRowNo)
		.replace(/commLink/g,"commLink_"+typeRowNo);
	$("#comment_"+typeRowNo).val('')
	$("#dep_comment_"+typeRowNo).val('')
	if (type=="support") {
		$("#"+forWhom+"SupportsList").append("<tr id='row_s_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow('row_s_"+rowNo+"', 'edit_supportAddedId')\"><span class='clear_filter'>X</span></a></td></tr>")
	} else {
		$("#"+forWhom+"DependentsList").append("<tr id='row_d_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow(\'row_d_"+rowNo+"', 'edit_dependentAddedId')\"><span class='clear_filter'>X</span></a></td></tr>")
	}
	$("#dep_"+typeRowNo+"_"+forWhom).addClass("scrollSelect");

	$("#dep_"+typeRowNo+"_"+forWhom).attr("data-asset-type", $("#entity_"+typeRowNo).val())
	
	if (!isIE7OrLesser) {
		AssetCrudModule.assetNameSelect2( $("#dep_"+typeRowNo+"_"+forWhom) );
	}
	
	$("#depComment_"+typeRowNo).dialog({ autoOpen: false})

	$("#"+forWhom+"_"+type+"AddedId").val(parseInt(rowNo)-1)	
}

function deleteRow ( rowId, forWhomId ) {
	$("#"+rowId).remove()
	var id = rowId.split('_')[3]

	if(id)
		$("#deletedDepId").val(( $("#deletedDepId").val() ? $("#deletedDepId").val()+"," : "") + id)
	else
		$("#"+forWhomId).val(parseInt($("#"+forWhomId).val())+1)
}

function updateAssetTitle ( type ) {
	$("#createEntityView").dialog( "option", "title", type+ ' Detail' );
	$("#showEntityView").dialog( "option", "title", type+' Detail' );
	$("#editEntityView").dialog( "option", "title", type+' Detail' );
}

function selectManufacturer (value, forWhom) {
	var val = value;
	manipulateFields(val)
	new Ajax.Request(contextPath+'/assetEntity/getManufacturersList?assetType='+val+'&forWhom='+forWhom,{asynchronous:true,evalScripts:true,onComplete:function(e){showManufacView(e, forWhom);}})
}

function showManufacView (e, forWhom) {
	var resp = e.responseText;
	if(forWhom == 'Edit')
		$("#manufacturerEditId").html(resp);
	else
		$("#manufacturerCreateId").html(resp);
	
	$("#manufacturers").removeAttr("multiple")
	if(!isIE7OrLesser)
		$("select.assetSelect").select2()
}

function selectModel (value, forWhom) {
	var val = value;
	var assetType = $("#assetType"+forWhom+"Id").val() ;
	new Ajax.Request(contextPath+'/assetEntity/getModelsList?assetType='+assetType+'&manufacturer='+val+'&forWhom='+forWhom,{asynchronous:true,evalScripts:true,onComplete:function(e){showModelView(e, forWhom);}})
	//${remoteFunction(action:'getModelsList', params:'\'assetType=\' +assetType +\'&=\'+ val', onComplete:'showModelView(e)' )}
}

function showModelView (e, forWhom) {
	var resp = e.responseText;
	$("#model"+forWhom+"Id").html(resp);
	$("#models").removeAttr("multiple")
	if (forWhom == "assetAudit") {
		$("#models").attr("onChange","editModelAudit(this.value)")
	}
	if (!isIE7OrLesser) {
		$("select.assetSelect").select2()
	}
}

//DEPRECATED
function showComment (commentId , action, commentType) {
	console.log('DEPRECATED: showComment.');
	var id = id
	var objDom = $('[ng-app]');
	var injector = angular.element(objDom).injector();
		injector.invoke(function($rootScope, commentUtils){
			if(action =='edit'){
				$rootScope.$broadcast('editComment', commentUtils.commentTO(commentId, commentType));
			} else {
				$rootScope.$broadcast('viewComment', commentUtils.commentTO(commentId, commentType), 'show');
			}
		});
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
 function highlightCssByValidation(phase,forWhom, id){
	 jQuery.ajax({
			url: contextPath+'/assetEntity/getHighlightCssMap',
			data: {'validation':phase, 'type':forWhom,'id':id},
			type:'POST',
			success: function(resp) {
				console.log(resp)
				$("td,input,select").removeClass("highField")
				for (var key in resp) {
					var value = resp[key]
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

/**
 * Used to save newly created assets from the Create forms. After validating that the fields are 
 * okay it will make an Ajax call to create the asset.
 *
 * @param me - the form that is being processed
 * @param forWhom - string indicating which form is being processed (note that this is inconsistent with the update metho)
 */
function saveToShow($me, forWhom){
	var act = $me.data('action')

	if($me.data('redirect'))
		var redirect = $me.data('redirect').split("_")[0]
	if(act=='close'){
		$('#showView').val('closeView')
	}else{
		$('#showView').val('showView')
	}

	var type = forWhom;
	var validateOkay=true;

	switch(forWhom) {
		case 'Application':
			validateOkay = validateAppForm('Edit','createAssetsFormId');
			break;

		case 'Files':
		case 'Logical Storage':
			type = 'Storage';
			validateOkay = validateStorageForm('createAssetsFormId');
			break;

		case 'Database':
			validateOkay = validateDBForm('createAssetsFormId');
			break;

		case 'AssetEntity':
			type = "Server";
			validateOkay = validateDeviceForm('createAssetsFormId');
			break;

		default:
			alert('Unsupported case for ' + forWhom);
	}

	if (validateOkay)
		validateOkay = validateDependencies('createAssetsFormId')
		
	if (validateOkay) {
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

/**
 * Used to validate common fields on any of the asset create/edit forms
 * @return true if valid
 **/
function validateCommonFields(form) {
	var ok = false;

	// Validate that asset name is not blank
	var fieldVal = $('#'+form+' #assetName').val();
	if (fieldVal == '') {
		alert('Please provide a name for the asset')
	} else {
		ok = true
	}
	return ok;
}

/**
 * Used to validate the Storage asset create/edit forms
 * @return true if valid
 **/
function validateStorageForm(form) {
	var ok = validateCommonFields(form);
	if (ok) {
		ok = false;
		var size = $('#'+form+' #size').val();
		if ( size=='' || isNaN(size)) {
			alert("Please enter numeric value for Storage Size");
		} else if ($('#'+form+' #fileFormat').val()=='') {
			alert("Please enter value for Storage Format");
		} else {
			ok = true;
		}
	}
	return ok
}

/**
 * Used to validate the Database asset create/edit forms
 * @return true if valid
 **/
function validateDBForm(form) {
	var ok = validateCommonFields(form);
	if (ok) {
		ok = false;
	    var size = $('#'+form+' #size').val();
	    if ( size=='' || isNaN(size)){
	    	alert("Please enter numeric value for DB Size");
	    }else if($('#'+form+' #dbFormat').val()==''){
	    	alert("Please enter value for DB Format");
	    }else{
	    	ok = true;
	    }
	}
	return ok
}

/**
 * Used to validate the Server/Device asset create/edit forms
 * @return true if valid
 **/
function validateDeviceForm(form) {
	var ok = validateCommonFields(form);
	return ok
}

function validateAppForm(forWhom, form){
	var ok = validateCommonFields(form);
	if (ok) {
		ok = false;
		if($('#'+form+' #sme1'+forWhom).val()=='0' || $('#'+form+' #sme2'+forWhom).val()=='0' || $('#'+form+' #appOwner'+forWhom).val()=='0' ){
			alert("Please unselect the 'Add Person' option from SME, SME2 or Application Owner properties")
		} else {
			var msg = '';
			// Check to see if the durations have legit numbers
			var fname = ['Shutdown', 'Startup', 'Testing'];
			// Hack because of inconsistency in the field ids
			var suffix = (form == 'createAssetsFormId' ? '' : 'Edit');
			var c=0;
			fname.forEach( function(name) {
				var field = $('#'+form+' #' + name.toLowerCase() + 'Duration'+suffix).val();
				if ( field != '' && isNaN(field) ) {
					msg = ( msg!='' ? msg + ', ' : '' ) + name;
					c++;
				}
			});
			if (msg != '') {
				alert("Please make sure that the " + msg + ' Duration field' + (c>1 ? 's have' : ' has a') + ' numeric value' + (c>1 ? 's' : ''));
			} else {
				ok = true;
			}	
		}
	}
	return ok;
}

function validateDependencies(formName){
	var flag = true
	$('#'+formName+'  input[name^="asset_"]').each( function() {
		if( $(this).val() == 'null' ||  $(this).val() == '')
			flag = false
			return flag
	})
	if(flag==false)
		alert("Please select a valid asset for all dependencies ")
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
            if (event.srcElement.type == "textarea") {
                return;
            }
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
		$("#"+commLink).html('<img border="0px" src="'+contextPath+'/icons/comment_edit.png">')
	}else {
		$("#"+commLink).html('<img border="0px" src="'+contextPath+'/icons/comment_edit.png">')
	}
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

