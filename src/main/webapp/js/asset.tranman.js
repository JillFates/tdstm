 /**------------------------------------Asset CRUD----------------------------------*/
var requiredFields = ["assetName"];
var B2 = []
Array.prototype.contains = function (element) {
	for (var i = 0; i < this.length; i++) {
		if (this[i] == element) {
		return true;
		}
	}
	return false;
}

var stdErrorMsg = 'An unexpected error occurred. Please close and reload form to see if the problem persists'

// function to generate createForm
 function generateCreateForm( e ){
		var browser=navigator.appName;
    	var assetEntityAttributes = eval('(' + e.responseText + ')');
    	var tb = $("#createFormTbodyId");
    	var autoComp = new Array();

    	var tbody = ""
    	if (assetEntityAttributes != "") {
    		var length = assetEntityAttributes.length

    		var tableLeft = ""

    		for (var i=0; i < length; i ++ ) {
    			var attribute = assetEntityAttributes[i]

    			var labelTd = "<td class='label' nowrap><label>"+attribute.label+""
			    if(requiredFields.contains(attribute.attributeCode)){
			    	var spanAst = "<span style='color:red;'>*</span>"//document.createElement("span")
				    labelTd += spanAst
			    }
			    labelTd +="</label></td>"

			    var inputTd = "<td>";
			    inputTd += getInputType(attribute,'');
			    inputTd += "</td>"

			    if( i % 3 == 0){
			    	tableLeft +="<tr>"+labelTd + inputTd
				} else if( i % 3 == 1){
					tableLeft += labelTd + inputTd
				} else {
					tableLeft += labelTd + inputTd+"</tr>"
				}
			    var attribute = assetEntityAttributes[i]
                if(attribute.frontendInput == 'autocomplete'){
                	autoComp.push(attribute.attributeCode)
                }
    		}

		    tbody += filedRequiredMess()
		    tbody += tableLeft
    	}
    	tb.html( tbody );
    	$('#createFormTbodyId').css('display','block');

	    new Ajax.Request(contextPath+'/assetEntity/retrieveAutoCompleteDate?autoCompParams='+autoComp,{asynchronous:true,evalScripts:true,onComplete:function(e){createAutoComplete(e);}})
	    $("#assetTypeId").val("Server")
		updateManufacturerOptions("Server", null, 1)
 }
 function filedRequiredMess( table ){

	var etr = "<tr><td colspan='6'><div><span class='required'>Fields marked ( * ) are mandatory</span></div></td></tr>"
	return etr;
     //table.appendChild( etr )
 }
 function createAutoComplete(e){
  	var data = eval('(' + e.responseText + ')');
  	if (data) {
      var length = data.length
      for (var i=0; i < length; i ++ ) {
	      var attribData = data[i]
	      var code = attribData.attributeCode+"Id"
	      var codeValue = attribData.value;
  			$("#"+code).autocomplete(codeValue);
	  }
	}
 }
var modelId
var manufacturerId
// function to show asset dialog
function showAssetDialog (e, action) {

	$('#createCommentDialog').dialog('close');
	$('#commentsListDialog').dialog('close');
	$('#editCommentDialog').dialog('close');
	$('#showCommentDialog').dialog('close');
	$('#changeStatusDialog').dialog('close');
	$('#filterDialog').dialog('close');

	var assetEntityAttributes = eval('(' + e.responseText + ')');
	var autoComp = new Array();

	var showDiv = $("#showDiv");
	var editDiv = $("#editDiv");
	// create tbody for CreateTable
	var stbody = "";
	var etbody = "";
	// Rebuild the select
	if (assetEntityAttributes) {
		var length = assetEntityAttributes.length;

		var stableLeft = "";
		var etableLeft = "";

		for (var i = 0; i < length; i++ ) {
			var attribute = assetEntityAttributes[i];

			var labelTd = "<td class='label' nowrap><label>"+attribute.label+""

			var labelTdE = ""
			if (attribute.attributeCode == "manufacturer" && attribute.value != "" && attribute.value != null) {
				labelTdE = "<td class='label' nowrap><label><a href='javascript:showManufacturer("+attribute.manufacturerId+")' style='color:#00E'>"+attribute.label+"</a>"
			} else if (attribute.attributeCode == "model"&& attribute.value != "" && attribute.value != null ) {
				labelTdE = "<td class='label' nowrap><label><a href='javascript:showModel("+attribute.modelId+")' style='color:#00E'>"+attribute.label+"</a>"
			} else {
				labelTdE = "<td class='label' nowrap>"+attribute.label
			}

			if (requiredFields.contains(attribute.attributeCode)) {
				var spanAst = "<span style='color:red;'>*</span>"//document.createElement("span")
				labelTd += spanAst
				labelTdE += spanAst
			}
			labelTd +="</label></td>"
			labelTdE +="</label></td>"

			var inputTd = ""
			if (attribute.attributeCode == "manufacturer") {
				inputTd = "<td style='width:25%;color:#00f;' nowrap><a href='javascript:showManufacturer("+attribute.manufacturerId+")' style='color:#00E'>"+attribute.value+"</a></td>"
			} else if (attribute.attributeCode == "model") {
				inputTd = "<td style='width:25%;color:#00f;' nowrap><a href='javascript:showModel("+attribute.modelId+")' style='color:#00E'>"+attribute.value+"</a></td>"
			} else {
				inputTd = "<td style='width:25%;' nowrap>"+attribute.value+"</td>"
			}

			// td for Edit page
			var inputTdE = "<td>";
			inputTdE += getInputType(attribute,'edit');
			inputTdE += "</td>"

			if (i % 3 == 0) {
				stableLeft +="<tr>"+labelTd + inputTd
				etableLeft +="<tr>"+labelTdE + inputTdE
			} else if (i % 3 == 1) {
				stableLeft += labelTd + inputTd
				etableLeft += labelTdE + inputTdE
			} else {
				stableLeft +=labelTd + inputTd+"</tr>"
				etableLeft +=labelTdE + inputTdE+"</tr>"
			}

			var attribute = assetEntityAttributes[i];
			if (attribute.frontendInput == 'autocomplete') {
				autoComp.push(attribute.attributeCode);
			}
		}
		stableLeft +="</table>"

		etableLeft +="</table>"

		stbody +="<table>"+stableLeft+"</table>"


		etbody += "<table>" + filedRequiredMess()
		etbody += etableLeft+"</table>"

		showDiv.html( stbody )
		editDiv.html( etbody );
	}

	new Ajax.Request(contextPath+'/assetEntity/retrieveAutoCompleteDate?autoCompParams='+autoComp,{asynchronous:true,evalScripts:true,onComplete:function(e){updateAutoComplete(e);}})
	$("#createDialog").dialog("close");
	if (action == 'edit') {
		$("#editDialog").dialog('option', 'width', '1000px');
		$("#editDialog").dialog('option', 'position', ['center','top']);
		$("#editDialog").dialog('option', 'modal', 'true');
		$("#editDialog").dialog("open");
		$("#showDialog").dialog("close");
		$("#modelShowDialog").dialog("close")
		$("#manufacturerShowDialog").dialog("close")
	} else if (action == 'show') {
		$("#showDialog").dialog('option', 'width', '1000px');
		$("#showDialog").dialog('option', 'position', ['center','top']);
		$("#showDialog").dialog('option', 'modal', 'true');
		$("#showDialog").dialog("open");
		$("#editDialog").dialog("close");
		$("#modelShowDialog").dialog("close")
		$("#manufacturerShowDialog").dialog("close")
	}
	var assetType = $("#editassetTypeId").val()
	updateManufacturerOptions(assetType, manufacturerId, 2)
}
    function updateManufacturerOptions(assetType, manufacturerId, type){
    	new Ajax.Request(contextPath+'/manufacturer/retrieveManufacturersListAsJSON?assetType='+assetType,{
			asynchronous:false,
			evalScripts:true,
			onComplete:function(e){
				var  manufacturersList = eval('(' + e.responseText + ')')
				var inputField  = '<option value=\'\'>Unassigned</option>'
				for(i=0; i<manufacturersList.length; i++){
					var manufacturer = manufacturersList[i]

					if( manufacturerId != manufacturer.id){
						inputField += '<option value=\''+manufacturer.id+'\'>'+manufacturer.name+'</option>'
					} else {
						inputField += '<option value=\''+manufacturer.id+'\' selected>'+manufacturer.name+'</option>'
					}
				}
				if( type == 2 ){
					$("#editmanufacturerId").html( inputField )
				} else {
					$("#manufacturerId").html( inputField )
				}
			}
		})
    	var manufacturer = $("#editmanufacturerId").val()
  	    if(manufacturer && !isNaN(manufacturer)){
  	    	updateModelOptions( manufacturer, modelId, 2 ) //  assume that 2 is for edit action
  	    }
    }
	function updateModelOptions( manufacturer, modelId, type ){
		var assetType = $("#editassetTypeId").val()
		if(type == 1){
			assetType = $("#assetTypeId").val()
		}
		new Ajax.Request(contextPath+'/model/retrieveModelsListAsJSON?manufacturer='+manufacturer+"&assetType="+assetType,{
			asynchronous:false,
			evalScripts:true,
			onComplete:function(e){
				var modelsList = eval('(' + e.responseText + ')')
				var inputField = '<option value=\'\'>Unassigned</option>'
				for(i=0; i<modelsList.length; i++){
					var model = modelsList[i]
					if( modelId != model.id){
						inputField += '<option value=\''+model.id+'\'>'+model.modelName+'</option>'
					} else {
						inputField += '<option value=\''+model.id+'\' selected>'+model.modelName+'</option>'
					}
				}
				if( type == 2 ){
					$("#editmodelId").html( inputField )
				} else {
					$("#modelId").html( inputField )
				}
			}
		})
	}
	function confirmAssetTypeChange( oldValue, newValue, type ){
		if(type == 2 ){
			if(confirm("WARNING : Change of Asset Type may impact on Manufacturer and Model, Do you want to continue ?")){
				updateManufacturerOptions(newValue, null, type )
			} else {
				$("#editassetTypeId").val( oldValue )
			}
		} else {
			updateManufacturerOptions(newValue, null, type)
		}
	}
	function confirmManufacturerChange( oldValue, newValue, type ){
		if(type == 2 ){
			if(confirm("WARNING : Change of Manufacturer may impact on Model data, Do you want to continue ?")){
				updateModelOptions(newValue, null, type )
			} else {
				$("#editmanufacturerId").val( oldValue )
			}
		} else {
			updateModelOptions(newValue, null, type)
		}
	}
	function confirmModelChange( oldValue, newValue, type ){
		if(type == 2 && !confirm("WARNING : Change of Model may impact on cabling data, Do you want to continue ?")){
			$("#editmodelId").val( oldValue )
		}
	}
  function updateAutoComplete(e){
  	var data = eval('(' + e.responseText + ')');
    if (data) {
		var length = data.length;
		for (var i=0; i < length; i ++ ) {
	    	var attribData = data[i];
			var code = "edit"+attribData.attributeCode+"Id";
			var codeValue = attribData.value;
		  	$("#"+code).autocomplete(codeValue);
		}
	}
  }

 //DEPRECATED
 function setAssetId(assetId){
	$("#createAssetCommentId").val(assetId)
 }
 function setAssetEditId(assetId){
	 $("#assetValueId").val(assetId)
 }

 function getLength( length ){
 	var isOdd = (length%2 != 0) ? true : false
    var halfLength
    	if(isOdd){
    		length += 1;
    		halfLength = length / 2
    	} else {
    		halfLength = length / 2
    	}
    return halfLength;
 }

 // function to construct the frontendInput tag
 function getInputType( attribute, id ){
 	var name = attribute.attributeCode
  	var type = attribute.frontendInput
  	var options = attribute.options
  	var browser=navigator.appName;
  	var inputField = ""
  	if( name == "moveBundle"){
  		new Ajax.Request(contextPath+'/moveBundle/projectMoveBundles',{
  				asynchronous:false,
  				evalScripts:true,
  				onComplete:function(e){
  					var  bundlesList = eval('(' + e.responseText + ')')
  					inputField = '<select name=\''+name+'\' id=\''+ id +name+'Id'+'\'><option value=\'\'>Unassigned</option>'

  					for(i=0; i<bundlesList.length; i++){
  						var bundle = bundlesList[i]

  						if(attribute.bundleId != bundle.id){
  						inputField += '<option value=\''+bundle.id+'\'>'+bundle.name+'</option>'
  						} else {
  							inputField += '<option value=\''+bundle.id+'\' selected>'+bundle.name+'</option>'
  						}
  					}
  					inputField += '</select>'
  				}
  			})
  	} else if( name == "model"){
  		modelId = attribute.modelId
  		var type = id ? 2 : 1 // Assume that 1 : create and 2 : edit
  		inputField = '<select name=\''+name+'\' id=\''+ id +name+'Id'+'\' onchange=\'confirmModelChange('+ attribute.modelId +', this.value, '+type+');\'><option value=\'\'>Unassigned</option></select>'
  		/*new Ajax.Request('../model/retrieveModelsListAsJSON',{
				asynchronous:false,
				evalScripts:true,
				onComplete:function(e){
					var  modelsList = eval('(' + e.responseText + ')')
					inputField = '<select name=\''+name+'\' id=\''+ id +name+'Id'+'\'><option value=\'\'>Unassigned</option>'

					for(i=0; i<modelsList.length; i++){
						var model = modelsList[i]

						if(attribute.modelId != model.id){
						inputField += '<option value=\''+model.id+'\'>'+model.modelName+'</option>'
						} else {
							inputField += '<option value=\''+model.id+'\' selected>'+model.modelName+'</option>'
						}
					}
					inputField += '</select>'
				}
			})*/
  	} else if( name == "manufacturer"){
  		manufacturerId = attribute.manufacturerId
  		var type = id ? 2 : 1 // Assume that 1 : create and 2 : edit
  		inputField = "<select name='"+name+"' id='"+ id +name+"Id' onchange='confirmManufacturerChange("+ attribute.manufacturerId +", this.value, "+type+" )'><option value=''>Unassigned</option>"
  		/*new Ajax.Request('../manufacturer/retrieveManufacturersListAsJSON ',{
				asynchronous:false,
				evalScripts:true,
				onComplete:function(e){
					var  manufacturersList = eval('(' + e.responseText + ')')

					for(i=0; i<manufacturersList.length; i++){
						var manufacturer = manufacturersList[i]

						if(attribute.manufacturerId != manufacturer.id){
						inputField += '<option value=\''+manufacturer.id+'\'>'+manufacturer.name+'</option>'
						} else {
							inputField += '<option value=\''+manufacturer.id+'\' selected>'+manufacturer.name+'</option>'
						}
					}
					inputField += '</select>'
				}
			})*/
  	} else if( name == "assetType"){
  		var actiontype = id ? 2 : 1 // Assume that 1 : create and 2 : edit
  		inputField = "<select name='"+name+"' id='"+ id +name+"Id' onchange=\"confirmAssetTypeChange('"+ attribute.value +"', this.value, "+actiontype+" )\">"
  		//"<select name='"+name +"' id='"+ id +name+"Id' onchange='confirmAssetTypeChange("+ attribute.value +", this.value, "+type+" ) '>"
		var inputOption = '<option value=\'\' >please select</option>'
		if (options) {
			var length = options.length
		    for (var i=0; i < length; i++) {
				var optionObj = options[i]
			    if(attribute.value == optionObj.option){
			    	inputOption += '<option value=\''+ optionObj.option+'\' selected>'+optionObj.option+'</option>'
			    } else {
			    	inputOption += '<option value=\''+ optionObj.option+'\' >'+optionObj.option+'</option>'
			    }
		    }
		 }
		inputField += inputOption+'</select>'
	} else if(type == 'select'){
  		inputField = '<select name=\''+name +'\' id=\''+ id +name+'Id'+'\'>'
		var inputOption = '<option value=\'\' >please select</option>'
		if (options) {
			var length = options.length
		    for (var i=0; i < length; i++) {
				var optionObj = options[i]
			    if(attribute.value == optionObj.option){
			    	inputOption += '<option value=\''+ optionObj.option+'\' selected>'+optionObj.option+'</option>'
			    } else {
			    	inputOption += '<option value=\''+ optionObj.option+'\' >'+optionObj.option+'</option>'
			    }
		    }
		 }
		inputField += inputOption+'</select>'
	 } else {
		 	if(attribute.value){
		 		inputField = '<input type="text" name="'+name +'" id="'+ id + name + 'Id"'+' value="'+attribute.value +'"></input>'
		 	} else {
		 		inputField = '<input type="text" name="'+name +'" id="'+ id + name + 'Id"'+' ></input>'
		 	}
	 }
	return inputField;
 }

 /*Actions to perform Delete Assest and Remove Assest from project*/
 function editDialogDeleteRemove( actionType ) {
	var confirmMessage = 'Remove Asset from project, are you sure?';
	var submitAction = 'remove';
	if ( actionType != 'remove' ) {
	 	confirmMessage = 'Delete Asset, are you sure?';
	 	submitAction = 'delete';
	}
	if ( confirm(confirmMessage) ) {
		$('form#editForm').attr({action: contextPath+'/assetEntity/'+submitAction}).submit();
		return true;
	} else {
		return false;
	}
 }
 /*Number Validation */
 function IsNumeric(sText)
	{
		var ValidChars = "0123456789";
		var IsNumber=true;
		var Char;
		for (i = 0; i < sText.length && IsNumber == true; i++)
		{
			Char = sText.charAt(i);
			if (ValidChars.indexOf(Char) == -1)
  		{
  			IsNumber = false;
  		}
		}
		return IsNumber;
}

 /**------------------------------------Asset Comments----------------------------------*/
//function to list the comments list
//DEPRECATED
function listCommentsDialog(e,action) {
	console.log("DEPRECATED: listCommentsDialog.");
    $('#showDialog').dialog('close');
    $('#editDialog').dialog('close');
    $('#createDialog').dialog('close');
    $('#changeStatusDialog').dialog('close');
    $('#filterDialog').dialog('close');
    var assetComments = eval('(' + e.responseText + ')');
    var listTable = $('#listCommentsTable');
    var tbody = $('#listCommentsTbodyId');
    if (assetComments) {
        var objDom = $('[ng-app]');
        var injector = angular.element(objDom).injector();
        injector.invoke(function($rootScope, commentUtils){
            $rootScope.$broadcast('commentsList', commentUtils.assetTO(assetComments[0].assetEntityId, assetComments[0].assetType));
        });
        return;
	}
}

// Invoked by Ajax call to populate the Create & Edit Asset Comment Dialog
//DEPRECATED
function showAssetCommentDialog( e , action){
	console.log("DEPRECATED: showAssetCommentDialog.");
	if(B2 != ''){
		B2.Pause()
	}

	var assetComments = eval('(' + e.responseText + ')');
	if (assetComments) {
		var params = assetComments[0]
		if(params.error){
			alert(params.error)
		}else {
			var ac = params.assetComment
		    var objDom = $('[ng-app]');
		    var injector = angular.element(objDom).injector();
		    injector.invoke(function($rootScope, commentUtils){
		        $rootScope.$broadcast('viewComment', (type==''?'issue':'comment'), commentUtils.commentTO(ac.id, ac.commentType), 'show');
		    });
		}
	}
}

function addCommentsToList( e ){
	var status = $('#statusId').val();

	$("#editCommentDialog").dialog("close")
    var assetComments = eval('(' + e.responseText + ')');
	var tbody = $('#listCommentsTbodyId')
	if (assetComments != "") {
			$("#createCommentDialog").dialog("close")
	      	//generate dynamic rows
	      	  var tr = document.createElement('tr');
	      	  tr.style.background = '#65a342'
	      	  tr.setAttribute('onmouseover','this.style.backgroundColor="white";');
	          if(assetComments.assetComment.commentType=='issue'){
		      	  tr.setAttribute('onmouseout','this.style.backgroundColor="'+assetComments.cssClass+'";');
		      	  tr.style.background = assetComments.cssClass
	          }
	      	  tr.id = "commentTr_"+assetComments.assetComment.id

		      var editTd = document.createElement('td');
			  var commentTd = document.createElement('td');
			  commentTd.id = 'comment_'+assetComments.assetComment.id
			  commentTd.name = assetComments.assetComment.id
			  commentTd.onclick = function(){new Ajax.Request(contextPath+'/assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
			  var typeTd = document.createElement('td');
			  typeTd.id = 'type_'+assetComments.assetComment.id
			  typeTd.name = assetComments.assetComment.id
			  typeTd.onclick = function(){new Ajax.Request(contextPath+'/assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}

			  var dueDateTd = document.createElement('td');
			  dueDateTd.id = 'type_'+assetComments.assetComment.id
			  dueDateTd.name = assetComments.assetComment.id
			  dueDateTd.onclick = function(){new Ajax.Request(contextPath+'/assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}

			  var categoryTd = document.createElement('td');
			  categoryTd.id = 'category_'+assetComments.assetComment.id
			  categoryTd.name = assetComments.assetComment.id
			  categoryTd.onclick = function(){new Ajax.Request(contextPath+'/assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}

			  var commentCodeTd = document.createElement('td');
			  commentCodeTd.id = 'commentCode_'+assetComments.assetComment.id
			  commentCodeTd.name = assetComments.assetComment.id
			  commentCodeTd.onclick = function(){new Ajax.Request(contextPath+'/assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}

			  var resolveTd = document.createElement('td');
			  resolveTd.id = 'resolve_'+assetComments.assetComment.id
			  resolveTd.name = assetComments.assetComment.id
		      resolveTd.onclick = function(){new Ajax.Request(contextPath+'/assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}
		   	  var verifyTd = document.createElement('td');
			  verifyTd.id = 'verify_'+assetComments.assetComment.id
			  verifyTd.name = assetComments.assetComment.id
			  verifyTd.onclick = function(){new Ajax.Request(contextPath+'/assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
			  var image = document.createElement('img');
		      image.src = "../icons/database_edit.png"
		      image.border = 0
			  var link = document.createElement('a');
			  link.href = '#'
			  link.id = 'link_'+assetComments.assetComment.id
			  link.onclick = function(){new Ajax.Request(contextPath+'/assetEntity/showComment?id='+assetComments.assetComment.id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );}})} //;return false
		      var commentText = document.createTextNode(truncate(assetComments.assetComment.comment));
		      var typeText = document.createTextNode(assetComments.assetComment.commentType);
		      var formatedDueDate = formatDueDate(assetComments.assetComment.dueDate);
		      var duedate = document.createTextNode(formatedDueDate);
		      var categoryText = document.createTextNode(assetComments.assetComment.category);
		      var commentCodeText = document.createTextNode(assetComments.assetComment.commentCode);

		      var resolveVal
		      if(assetComments.assetComment.commentType != "issue"){
		      resolveVal = document.createTextNode('');
		      }else{
		      resolveVal = document.createElement('input')
		      resolveVal.id = 'verifyResolved_'+assetComments.assetComment.id
		      resolveVal.type = 'checkbox'
		      resolveVal.disabled = 'disabled'

		      }
		      var verifyText = document.createElement('input')
		      verifyText.id = 'verifyText_'+assetComments.assetComment.id
		      verifyText.type = 'checkbox'
		      verifyText.disabled = 'disabled'

		      //createTextNode(assetComments.mustVerify);
		      link.appendChild( image )
			  editTd.appendChild( link  )
		      commentTd.appendChild( commentText )
		      typeTd.appendChild( typeText )
		      dueDateTd.appendChild( duedate )
		      resolveTd.appendChild( resolveVal )
		      //verifyTd.appendChild( verifyText )
		      categoryTd.appendChild( categoryText )
		      //commentCodeTd.appendChild( commentCodeText )
		      tr.appendChild( editTd )
		      tr.appendChild( commentTd )
		      tr.appendChild( typeTd )
		      tr.appendChild( dueDateTd )
		      tr.appendChild( resolveTd )
		      //tr.appendChild( verifyTd )
		      tr.appendChild( categoryTd )
		      //tr.appendChild( commentCodeTd )
		      tbody.append( tr )
		      if(assetComments.assetComment.isResolved == 1){
		      	resolveVal.checked = true;
		      }
		       if(assetComments.assetComment.mustVerify != 0){
		      	verifyText.checked = true
		      }
		  	updateAssetCommentIcon( assetComments );
	} else {
			alert("Comment not created")
	}
}

// Update comments on list view
function updateCommentsOnList( e ){
	var assetComments = eval('(' + e.responseText + ')');
	if (assetComments != "") {
		$("#editCommentDialog").dialog("close")
      	//generate dynamic rows
      	  var tr = $('#commentTr_'+assetComments.assetComment.id);
      	 tr.css( 'background', '#65a342' );
      	  if(assetComments.assetComment.mustVerify != 0){
	      $('#verifyText_'+assetComments.assetComment.id).attr('checked', true);
	      } else {
	      $('#verifyText_'+assetComments.assetComment.id).attr('checked', false);
	      }
	      if(assetComments.assetComment.commentType != "issue"){
	      	  $('#resolve_'+assetComments.assetComment.id).html("");
	      }else{
		      var checkResolveTd = $('#verifyResolved_'+assetComments.assetComment.id);
		      if(checkResolveTd){
		      	checkResolveTd.remove();
		      }
	      	  var resolveVal = document.createElement('input')
		      resolveVal.id = 'verifyResolved_'+assetComments.assetComment.id
		      resolveVal.type = 'checkbox'
		      resolveVal.disabled = 'disabled'
		      $('#resolve_'+assetComments.assetComment.id).append( resolveVal )

		      if(assetComments.assetComment.isResolved != 0){
		      	$('#verifyResolved_'+assetComments.assetComment.id).attr('checked', true);
		      } else {
		      	$('#verifyResolved_'+assetComments.assetComment.id).attr('checked', false);
		      }
	      }
	      var formatedDueDate = formatDueDate(assetComments.assetComment.dueDate);
	      var duedate = document.createTextNode(formatedDueDate);

	      $('#type_'+assetComments.assetComment.id).html(assetComments.assetComment.commentType);
	      $('#dueDate_'+assetComments.assetComment.id).html(duedate);
	      $('#comment_'+assetComments.assetComment.id).html(truncate(assetComments.assetComment.comment));
	      $('#category_'+assetComments.assetComment.id).html(assetComments.assetComment.category);
	      updateAssetCommentIcon( assetComments )
	}
}

//DEPRECATED
function createIssue(asset, type, id, forWhom, assetType){
	console.log("DEPRECATED: createIssue.");
    var objDom = $('[ng-app]');
    var injector = angular.element(objDom).injector();
    injector.invoke(function($rootScope, commentUtils){
        $rootScope.$broadcast('createComment', ((!type || type=='' || type=='issue')?'issue':'comment'), commentUtils.assetTO(id, assetType));
    });
}

/*UPDATE THE ASSET COMMENT ICON*/
function updateAssetCommentIcon( assetComments ){
	var link = document.createElement('a');
	link.href = '#'

	link.onclick = function(){setAssetId(assetComments.assetComment.assetEntity.id);new Ajax.Request(contextPath+'/assetEntity/listComments?id='+assetComments.assetComment.assetEntity.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'never');}})} //;return false
	if( assetComments.status ){
		link.innerHTML = "<img src=\"../i/db_table_red.png\" border=\"0px\">"
	}else{
		link.innerHTML = "<img src=\"../i/db_table_bold.png\" border=\"0px\">"
	}
	var iconObj = $('#icon_'+assetComments.assetComment.assetEntity.id);
	iconObj.html(link)
}

/*
 * remove a single element from an Array
 * Used to Remove Current Task from from succ or Pred select list.
 */
function removeByElement(arrayName,arrayElement)
{
	 for(var i=0; i<arrayName.length;i++ ){
		 if(arrayName[i].split("_")[1]==arrayElement)
		 arrayName.splice(i,1);
	 }
 return arrayName
}

/*
 * validate the text area size
*/
function textCounter(fieldId, maxlimit) {
	var value = $("#"+fieldId).val()
    if (value.length > maxlimit) { // if too long...trim it!
    	$("#"+fieldId).val(value.substring(0, maxlimit));
    	return false;
    } else {
    	return true;
    }
}
/*
 *
 */
function showManufacturer(id){
	new Ajax.Request(contextPath+'/manufacturer/retrieveManufacturerAsJSON?id='+id,{
		asynchronous:false,
		evalScripts:true,
		onComplete:function(e){
			var data = eval('(' + e.responseText + ')')
			var manufacturer = data.manufacturer
			$("#showManuName").html( manufacturer.name )
			$("#showManuAka").html( data.aliases )
			$("#showManuDescription").html( manufacturer.description )
			$("#show_manufacturerId").val( manufacturer.id )
			$("#manufacturerShowDialog").dialog("open")
		}
	})
}
function showModel(id){
	new Ajax.Request(contextPath+'/model/retrieveModelAsJSON?id='+id,{
		asynchronous:false,
		evalScripts:true,
		onComplete:function(e){
			var model = eval('(' + e.responseText + ')')
			$("#show_modelId").val( model.id )
			$("#showManufacturer").html( model.manufacturer )
			$("#showModelName").html( model.modelName )
			$("#showModelAka").html( model.aka )
			$("#showModelNotes").html( model.description )
			$("#showModelAssetType").html( model.assetType )
			$("#showModelUsize").html( model.usize )
			$("#namePlatePowerSpanId").html( model.powerNameplate )
			$("#PowerDesignSpanId").html( model.powerDesign )
			$("#powerSpanId").html( model.powerUse )

			if(model.frontImage != ''){
				$("#showModelFrontImage").html( "<img src='../model/retrieveFrontImage/"+model.id+"' style='height: 50px; width: 100px;' id='rearImageId'>" )
			} else {
				$("#showModelFrontImage").html("")
			}
			if(model.rearImage != ''){
				$("#showModelRearImage").html( "<img src='../model/retrieveRearImage/"+model.id+"' style='height: 50px; width: 100px;' id='rearImageId'>" )
			} else {
				$("#showModelRearImage").html("")
			}
			if(model.useImage){
				$("#showModelUseImage").html( "<input type='checkbox' checked='checked' disabled='disabled'/>" )
			} else {
				$("#showModelUseImage").html( "<input type='checkbox' disabled='disabled'/>" )
			}
			if(model.assetType == 'Blade Chassis'){
				$("#showModelBladeRows").html( model.bladeRows )
				$("#showModelBladeCount").html( model.bladeCount )
				$("#showModelBladLabelCount").html( model.bladeLabelCount )
			} else {
				$("#showModelBladeRowsTr").hide()
				$("#showModelBladeCountTr").hide()
				$("#showModelBladLabelCountTr").hide()
			}
			if(model.assetType == 'Blade'){
				$("#showModelBladeHeight").html( model.bladeHeight )
			} else {
				$("#showModelBladeHeightTr").hide()
			}
			if(model.sourceTDS){
				$("#showModelSourceTds").html( "<input type='checkbox' checked='checked' disabled='disabled'/>" )
			} else {
				$("#showModelSourceTds").html( "<input type='checkbox' disabled='disabled'/>" )
			}

			$("#modelShowDialog").dialog("open")
		}
	})
}

//DEPRECATED
function createNewAssetComment(asset, assetName, assetType){
	console.log("DEPRECATED: createNewAssetComment.");

	createComments(asset, assetName, assetType);
}

//DEPRECATED
function createComments(asset, assetName, assetType){
	console.log("DEPRECATED: createComments.");
    var objDom = $('[ng-app]');
    var injector = angular.element(objDom).injector();
    injector.invoke(function($rootScope, commentUtils){
        $rootScope.$broadcast('createComment', 'issue', commentUtils.assetTO(asset, assetType));
    });
}

//DEPRECATED
function showAssetComment(commentId, commentType){
	console.log("DEPRECATED: showAssetComment.");
	$("#commentcloseId").val('close');
    var objDom = $('[ng-app]');
    var injector = angular.element(objDom).injector();
    injector.invoke(function($rootScope, commentUtils){
        $rootScope.$broadcast('viewComment', commentUtils.commentTO(commentId, commentType), 'show');
    });
    return;
}

function updateCommentsLists(){
	$('#editCommentDialog').dialog('close');
	window.location.reload();
}

/*function formatDate(dateValue){
	    var M = "" + (dateValue.getMonth()+1);
	    var MM = "0" + M;
	    MM = MM.substring(MM.length-2, MM.length);
	    var D = "" + (dateValue.getDate());
	    var DD = "0" + D;
	    DD = DD.substring(DD.length-2, DD.length);
	    var YYYY = "" + (dateValue.getFullYear());
        var currentDate = MM + "/" +DD + "/" + YYYY
        $("#dueDateCreateId").val(currentDate);
}*/

function formatDueDate(input){
	 var currentDate = ""
	 if(input){
		  var datePart = input.match(/\d+/g),
		  year = datePart[0].substring(0), // get only two digits
		  month = datePart[1], day = datePart[2];
	      currentDate = month+'/'+day+'/'+year;
	 }
    return currentDate
}

function showResolve(value){
	var statusWarn = $('#statuWarnId').val()
  	if(statusWarn==1){
  		alert("Warning: you are overriding the predefined ordering of tasks by changing the task status prematurely.")
  	}
	if(value=='Completed'){
		$('#resolutionTrId').css('display','table-row')
		$('#resolutionEditTrId').css('display','table-row')
	}else{
		$('#resolutionTrId').css('display','none')
		$('#resolutionEditTrId').css('display','none')
	}

}

function generateDepSel(taskId, taskDependencyId, category, selectedPred, selectId, selectName){
	jQuery.ajax({
		url:contextPath+'/assetEntity/generateDepSelect',
		data: {'taskId':taskId, 'category':category},
		type:'POST',
		success: function(data) {
			var selectBoxId = "#"+selectId+"_"+taskDependencyId
			$(selectBoxId).html(data)
			$(selectBoxId).val(selectedPred)
			$(selectBoxId).removeAttr('onmouseover')
		}
	});
}


function enableCreateIcon(id){
	$(".create_"+id).css("display","block")
	$("#span_"+id).html("<img src='../images/plus_disabled.gif'/>")
	$("#span_"+id).attr("onClick","disableCreateIcon("+id+")")
	new Ajax.Request(contextPath+'/rackLayouts/saveAddIconPreference?mode=enabled',{asynchronous:true,evalScripts:true })
}

function disableCreateIcon(id){
	$(".create_"+id).css("display","none")
	$("#span_"+id).html(" <img src='../images/plus.gif'/>")
	$("#span_"+id).attr("onClick","enableCreateIcon("+id+")")
	new Ajax.Request(contextPath +'/rackLayouts/saveAddIconPreference?mode=disabled',{asynchronous:true,evalScripts:true })
}

function toggleDependencies(forWhom,view){
	if(forWhom=='right'){
		$("#predecessor"+view+"Tr").show()
		$("#predecessorTrEditId").show()
		$("#predAddButton").show()
		$("#succAddButton").show()
		$(".rightArrow"+view).hide()
		$(".leftArrow"+view).show()
	}else{
		$("#predecessor"+view+"Tr").hide()
		$("#predecessorTrEditId").hide()
		$("#predAddButton").hide()
		$("#succAddButton").hide()
		$(".leftArrow"+view).hide()
		$(".rightArrow"+view).show()
	}
}
function redirectToListTasks(){
	window.location.href= contextPath+'/assetEntity/listTasks'
 }