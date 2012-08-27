 /**------------------------------------Asset CRUD----------------------------------*/
var requiredFields = ["assetName"];
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
	      
	    new Ajax.Request('../assetEntity/getAutoCompleteDate?autoCompParams='+autoComp,{asynchronous:true,evalScripts:true,onComplete:function(e){createAutoComplete(e);}})
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
function showAssetDialog( e , action ) {
	
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
		 
		for (var i=0; i < length; i++ ) {
		   	var attribute = assetEntityAttributes[i];
			      
		    var labelTd = "<td class='label' nowrap><label>"+attribute.label+""
		    
		    var labelTdE = ""
	    	if(attribute.attributeCode == "manufacturer" && attribute.value != "" && attribute.value != null){
	    		labelTdE = "<td class='label' nowrap><label><a href='javascript:showManufacturer("+attribute.manufacturerId+")' style='color:#00E'>"+attribute.label+"</a>"
		    } else if(attribute.attributeCode == "model"&& attribute.value != "" && attribute.value != null ){
		    	labelTdE = "<td class='label' nowrap><label><a href='javascript:showModel("+attribute.modelId+")' style='color:#00E'>"+attribute.label+"</a>"
		    } else {
		    	labelTdE = "<td class='label' nowrap>"+attribute.label
		    }
		    
		    if(requiredFields.contains(attribute.attributeCode)){
			  	var spanAst = "<span style='color:red;'>*</span>"//document.createElement("span")
			    labelTd += spanAst 
			    labelTdE += spanAst
		    }
		    labelTd +="</label></td>"
		    labelTdE +="</label></td>"
		    	
		    var inputTd = ""
		    if(attribute.attributeCode == "manufacturer"){
		    	inputTd = "<td style='width:25%;color:#00f;' nowrap><a href='javascript:showManufacturer("+attribute.manufacturerId+")' style='color:#00E'>"+attribute.value+"</a></td>"
		    } else if(attribute.attributeCode == "model"){
		    	inputTd = "<td style='width:25%;color:#00f;' nowrap><a href='javascript:showModel("+attribute.modelId+")' style='color:#00E'>"+attribute.value+"</a></td>"
		    } else {
		    	inputTd = "<td style='width:25%;' nowrap>"+attribute.value+"</td>"
		    }

		    // td for Edit page
		    var inputTdE = "<td>";
		    inputTdE += getInputType(attribute,'edit');
		    inputTdE += "</td>"   
		    
		    if( i % 3 == 0){
			   	stableLeft +="<tr>"+labelTd + inputTd
			   	etableLeft +="<tr>"+labelTdE + inputTdE
			} else if( i % 3 == 1){
				stableLeft += labelTd + inputTd 
				etableLeft += labelTdE + inputTdE 
			} else {
				stableLeft +=labelTd + inputTd+"</tr>"
				etableLeft +=labelTdE + inputTdE+"</tr>"
			}
			
		    var attribute = assetEntityAttributes[i];
		    if(attribute.frontendInput == 'autocomplete'){
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
	      
	  new Ajax.Request('../assetEntity/getAutoCompleteDate?autoCompParams='+autoComp,{asynchronous:true,evalScripts:true,onComplete:function(e){updateAutoComplete(e);}}) 
	  $("#createDialog").dialog("close");
	  if(action == 'edit'){
	      $("#editDialog").dialog('option', 'width', '1000px');
	      $("#editDialog").dialog('option', 'position', ['center','top']);
	      $("#editDialog").dialog("open");
	      $("#showDialog").dialog("close");
	      $("#modelShowDialog").dialog("close")
	      $("#manufacturerShowDialog").dialog("close")
      } else if(action == 'show'){
          $("#showDialog").dialog('option', 'width', '1000px');
	      $("#showDialog").dialog('option', 'position', ['center','top']);
	      $("#showDialog").dialog("open");
	      $("#editDialog").dialog("close");
	      $("#modelShowDialog").dialog("close")
	      $("#manufacturerShowDialog").dialog("close")
      }
	  var assetType = $("#editassetTypeId").val()
	  updateManufacturerOptions(assetType, manufacturerId, 2)
	  timedUpdate('never')
    }
    function updateManufacturerOptions(assetType, manufacturerId, type){
    	new Ajax.Request('../manufacturer/getManufacturersListAsJSON?assetType='+assetType,{
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
		new Ajax.Request('../model/getModelsListAsJSON?manufacturer='+manufacturer+"&assetType="+assetType,{
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
    		
  function callUpdateDialog( e ) {
    	var assetEntityAttributes = eval('(' + e.responseText + ')');
		var assetId = document.editForm.id.value
    	var assetEntityParams = new Array()
    	if (assetEntityAttributes) {
    		var length = assetEntityAttributes.length
		      	for (var i=0; i < length; i ++) {
		      		var attributeCode = assetEntityAttributes[i].attributeCode;
		      		var attributeValue = $('#edit'+attributeCode+'Id').val();
		      		if(assetEntityAttributes[i].frontendInput == 'select'){
			      		assetEntityParams.push(attributeCode+':'+attributeValue+'~');
		      		} else {
		      			assetEntityParams.push(attributeCode+':'+attributeValue+'~');
		      		}
		      	}
    	}
    var safeQueryString = escape( assetEntityParams );
    new Ajax.Request('../assetEntity/updateAssetEntity?id='+assetId+'&assetEntityParams='+safeQueryString,{asynchronous:true,evalScripts:true,onComplete:function(e){showEditAsset(e);}})
    
 }
    
 function setAssetId(assetId){
	$("#createAssetCommentId").val(assetId)
 }
 
 function updateWorkflowTransitions(assetId, category, transitionID, predecessorID,id){
	new Ajax.Request('../assetEntity/getWorkflowTransition?assetId='+assetId+'&category='+category+'&assetCommentId='+id,{asynchronous:true,evalScripts:true,
		onComplete:function(e){
			if(e.responseText.length=='0'){
				$('#workFlowTransitionTrId').css('display','none')
				$('#workFlowTransitionEditTrId').css('display','none')
			}else{
				$('#'+transitionID).html(e.responseText);
			    if(document.forms['editCommentForm'].commentType.value =='issue'){
			      $('#workFlowTransitionEditTrId').css('display','table-row')
			    }
			}
		}
	})
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
  		new Ajax.Request('../moveBundle/projectMoveBundles',{
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
  		/*new Ajax.Request('../model/getModelsListAsJSON',{
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
  		/*new Ajax.Request('../manufacturer/getManufacturersListAsJSON ',{
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
		$('form#editForm').attr({action: '../assetEntity/'+submitAction}).submit();
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
	function listCommentsDialog(e,action) {
		$("#editCommentDialog").dialog("close");
		$("#showCommentDialog").dialog("close");
		$("#createCommentDialog").dialog("close");
		$('#showDialog').dialog('close');
		$('#editDialog').dialog('close');
		$('#createDialog').dialog('close');
		$('#changeStatusDialog').dialog('close');
		$('#filterDialog').dialog('close');
		var assetComments = eval('(' + e.responseText + ')');
		var listTable = $('#listCommentsTable');
		var tbody = $('#listCommentsTbodyId');
		if (assetComments) {
			if(tbody != null){				
		   		tbody.remove();
		    }
		    var listTbody = document.createElement('tbody');
		    listTbody.id = 'listCommentsTbodyId'
			var length = assetComments.length
		    $('#assetEntityInputId').html(assetComments[0].assetName)
		    $('#newCommentId').attr('onClick','createComments('+assetComments[0].assetEntityId+',"'+assetComments[0].assetName+'")')
		      
		      	for (var i=0; i < length; i++) {
		      	//generate dynamic rows	
		      	  var commentObj = assetComments[i]
		      	  var tr = document.createElement('tr');
		      	  tr.id = "commentTr_"+commentObj.commentInstance.id
		      	  //tr.setAttribute("class", commentObj.cssClass)
		      	  tr.setAttribute('onmouseover','this.style.backgroundColor="white";');
		      	  if(commentObj.commentInstance.commentType=='issue'){
			      	  tr.setAttribute('onmouseout','this.style.backgroundColor="'+commentObj.cssClass+'";');
			      	  tr.style.backgroundColor=commentObj.cssClass;
		      	  }
			      var editTd = document.createElement('td');
			      var commentTd = document.createElement('td');
			      commentTd.id = 'comment_'+commentObj.commentInstance.id
			      commentTd.name = commentObj.commentInstance.id
			   	  
			      commentTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
			      // = 'comment_'+commentObj.commentInstance.id
			      var typeTd = document.createElement('td');
			      typeTd.id = 'type_'+commentObj.commentInstance.id
			      typeTd.name = commentObj.commentInstance.id
			      typeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}
			      
			      var dueDateTd = document.createElement('td');
			      dueDateTd.id = 'dueDate_'+commentObj.commentInstance.id
			      dueDateTd.name = commentObj.commentInstance.id					   	  
			      dueDateTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
			      
			      var resolveTd = document.createElement('td');
				  resolveTd.id = 'resolve_'+commentObj.commentInstance.id
				  resolveTd.name = commentObj.commentInstance.id
			      resolveTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}					      
			      var verifyTd = document.createElement('td');
			      
				  verifyTd.id = 'verify_'+commentObj.commentInstance.id
				  verifyTd.name = commentObj.commentInstance.id
			      verifyTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}
			      
			      var categoryTd = document.createElement('td');
			      categoryTd.id = 'category_'+commentObj.commentInstance.id
			      categoryTd.name = commentObj.commentInstance.id					   	  
			      categoryTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
			      
			      var commentCodeTd = document.createElement('td');
			      commentCodeTd.id = 'commentCode_'+commentObj.commentInstance.id
			      commentCodeTd.name = commentObj.commentInstance.id					   	  
			      commentCodeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
			      				     
			      
			      
			      var image = document.createElement('img');
			      image.src = "../images/skin/database_edit.png"
			      image.border = 0
			      var link = document.createElement('a');
			      //link.href = '#'
			      link.id = 'link_'+commentObj.commentInstance.id
			      link.name = commentObj.commentInstance.id
			      link.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );commentChangeEdit('editResolveDiv','editCommentForm');}})} //;return false
			      var commentText = document.createTextNode(truncate(commentObj.commentInstance.comment));
			      var typeText = document.createTextNode(commentObj.commentInstance.commentType); 
			      var formatedDueDate = formatDueDate(commentObj.commentInstance.dueDate);
			      var duedate = document.createTextNode(formatedDueDate);
			      
			      var resolveVal
			      if(commentObj.commentInstance.commentType != "issue"){
			      resolveVal = document.createTextNode('');
			      }else{
			      resolveVal = document.createElement('input')
			      resolveVal.id = 'verifyResolved_'+commentObj.commentInstance.id
			      resolveVal.type = 'checkbox'
			      resolveVal.disabled = 'disabled'
			     
			      }
			      var categoryText = document.createTextNode(truncate(commentObj.commentInstance.category));
			      var commentCodeText = document.createTextNode(truncate(commentObj.commentInstance.commentCode));
			     
			      
			      var verifyText = document.createElement('input')
			      verifyText.id = 'verifyText_'+commentObj.commentInstance.id
			      verifyText.type = 'checkbox'
			      verifyText.disabled = 'disabled'
			     
			      //createTextNode(commentObj.commentInstance.mustVerify);
			      link.appendChild( image )
				  editTd.appendChild( link )	
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
			      listTbody.appendChild( tr )
			      //$('#createAssetCommentId').val(commentObj.commentInstance.assetEntity.id)
			      if(commentObj.commentInstance.isResolved == 1){
			      	resolveVal.checked = true ;
			      }
		      	}
		      listTable.append( listTbody )
		}
		
		$("#commentsListDialog").dialog('option', 'width', 'auto')	      	
		$("#commentsListDialog").dialog('option', 'position', ['center','top']);
   	$("#commentsListDialog").dialog("open")
   	if(action == 'never'){
   		timedUpdate('never')
   	}
	}
	// Invoked by Ajax call to populate the Create & Edit Asset Comment Dialog
	function showAssetCommentDialog( e , action){
		$("#createCommentDialog").dialog("close")
		var assetComments = eval('(' + e.responseText + ')');
		if (assetComments) {
			
			var params = assetComments[0]
			var ac = params.assetComment
			
			if(ac.comment == null) {
				ac.comment = "";
			}
			if(ac.resolution == null){
				ac.resolution = "";
			}
			 $('#commentId').val(ac.id)
			 $('#predCount').val(params.maxVal)
			 $('#updateCommentId').val(ac.id)
	      	 $('#commentTdId').val(ac.comment) 
	      	 $('#commentTypeTdId').html(ac.commentType)
	      	 $('#mustVerifyShowId').val(ac.mustVerify)
	      	 $('#isResolvedId').val(ac.isResolved)
	      	 $('#categoryTdId').html(ac.category)
	      	 $('#commentCodeTdId').html(ac.commentCode)
	      	 $('#assetShowValueId').html(params.assetName)
		     $('#assetTrShowId').html(params.assetName)
		     $('#eventShowValueId').html(params.eventName)
		     
		     
	      	 if(ac.commentType=='issue'){
	      		 if(ac.resolution || ac.status=='Completed'){
	      		   $('#resolutionEditTrId').css('display','table-row')
	      		 }else{
	      			 $('#resolutionEditTrId').css('display','none')
	      		}
	      		 if(params.assignedTo){
			      	 $('#assignedToTdId').html(params.assignedTo.firstName + " " + params.assignedTo.lastName)
			      	 $('#assignedToEdit').val(params.assignedTo.id)
	      		 }
	      		 var notes = params.notes
	      		 var noteTable = '<table style="border:0px">'
      		     for(i=0; i<params.notes.length; i++){
      		    	 if (i>0) {
      		    		 noteTable += ""
      		    	 }
      		    	 noteTable += "<tr><td>" + notes[i][0] + "</td><td>" + notes[i][1] + "</td><td><span>" + notes[i][2] + "</span></td></tr>"
      		    	 }
      		     noteTable += "</table>"
      		     if(params.predecessorTable.length==0 && params.successorTable.length==0){ 
      		    	 $('#predecessorShowTr').css('display','none')
      		     }else{
      		    	$('#predecessorShowTr').css('display','table-row')
      		     }
      		     $('#statuWarnId').val(params.statusWarn)
                 $('#predecessorShowTd').html(params.predecessorTable)
                 var taskNumber = ac.taskNumber == null ? '&nbsp;' : ac.taskNumber
                 $('#taskNumberId').html('Task #: '+'<b>'+taskNumber+'</b>')
                 $('#taskNumberSpanEditId').html('Task #: '+'<b>'+taskNumber+'</b>')
                 $('#successorShowTd').html(params.successorTable)
                 $('#predecessorTrEditId').css('display','table-row')
    	      	 $('#previousNotesShowId').html(noteTable)
    	      	 $('#previousNote').html(noteTable)
	      		 $('#dueDateId').html(params.dueDate)
	      		 $('#dueDateEdit').val(params.dueDate)
		      	 $('#noteEditId').val('')
		      	 $('#statusEditId').val(ac.status)
		      	 $('#hardAssignedShow').attr('disabled', 'disabled');
      		     $('#overrideShow').attr('disabled', 'disabled');
		      	 if(ac.hardAssigned==1){
		      		 $('#hardAssignedShow').attr('checked', true);
		      		 $('#hardAssignedEdit').attr('checked', true);
		      	 }else{
		      		$('#hardAssignedShow').attr('checked', false);
		      		$('#hardAssignedEdit').attr('checked', false);
		      	 }
      		     if(ac.workflowOverride==1){
		      		 $('#overrideShow').attr('checked', true);
		      		 $('#overrideEdit').attr('checked', true);
		      	 }
      		     $('#hardAssignedEdit').val(ac.hardAssigned)
      		     $('#roleTdId').html(params.roles)
      		     ac.role ? $('#roleTypeEdit').val(ac.role) :$('#roleTypeEdit').val('')
      		     $('#estStartShowId').html(params.etStart)
      		     params.etStart ? $('#estStartEditId').val(params.etStart) : $('#estStartEditId').val('')
      		     $('#estStartEditTrId').css('display','table-row')
      		     $('#estFinishShowId').html(params.etFinish)
      		     params.etFinish ? $('#estFinishEditId').val(params.etFinish) : $('#estFinishEditId').val('')
      		     $('#actStartShowId').html(params.atStart)
      		     $('#actStartEditId').val(params.atStart)
      		     $('#actFinishShowId').html(params.dtResolved)
      		     $('#actFinishEditId').val(params.dtResolved)
      		     if(params.workflow){
      		      $('#workFlowShowId').html(params.workflow)
      		     }else{
      		      $('#workFlowShow').css('display','none')
      		     }
      		     $('#workFlowEditId').html(params.workflow)
      		     if(ac.priority==1||ac.priority==2){
      		       $('#priorityShowId').html('<b>'+ac.priority+'</b>')
      		     }else{
      		    	 $('#priorityShowId').html(ac.priority)
      		     }
      		     var duration = ac.duration ? ac.duration :''
      		     var durationScale = ac.durationScale ?ac.durationScale:''
      		     $('#durationShowId').html(duration +" "+ durationScale )
      		     $('#durationEdit').val(duration )
      		     ac.durationScale ? $('#durationScaleEdit').val(ac.durationScale) : $('#durationScaleEdit').val('m') 
      		     ac.priority ? $('#priorityEdit').val(ac.priority) : $('#priorityEdit').val('')
      		     
      		     $('#commentButtonEditId').attr('onClick','showAssetComment('+ac.id+', "edit")')
      		     $('#commentTypeEditTdId').css('display','none')
      	      	 $('#typeListTdId').css('display','none')
      		     $('#commentShowTrId').css('display','none')
      		     $('#predecessorAddTr').css('display','table-row')
      		     $('#workFlowShow').css('display','table-row')
	      		 $('#estFinishTrEditId').css('display','table-row')
	      		 $('#priorityEditId').css('display','table-row')
      		     $('#predecessorTrEditId').css('display','table-row')
      		     $('#durationEditId').css('display','table-row')
      		     $('#priorityShow').css('display','table-row')
      		     $('#estStartShowId').css('display','table-row')
      		     $('#estFinishShowId').css('display','table-row')
      		     $('#actStartShowId').css('display','table-row')
		      	 $('#mustVerifyId').css('display','none')
		      	 $('#mustVerifyEditTr').css('display','none')
			     $('#assetShowValueId').css('display','block')
			     $('#assetTrShowId').css('display','block')
		         $('#commentTypeEditId').attr("disabled","disabled");
      		     $('.issue').css('display','table-row')
      		     $('#deleteCommentId').css('display','none')
			     if(ac.assetEntity==null){
			    	$('#moveShowId').css('display','table-row')
			    	$('#assetShowId').css('display','none')
			    	$('#assetTrId').css('display','none')
			    	$('#moveEventEditId').val(ac.moveEvent ? ac.moveEvent.id : '')
			    	$('#moveEventEditTrId').css('display','table-row')
			     }else{
			    	$('#moveShowId').css('display','none')
			    	$('#assetShowId').css('display','table-row')
			    	$('#moveEventEditTrId').css('display','none')
			    	$('#assetTrId').css('display','table-row')
			    	$('#assetValueId').val(ac.assetEntity ? ac.assetEntity.id : '')
			     }
	      	 } else {
	      		$('.issue').css('display','none')
	      		$('#deleteCommentId').removeAttr('style')
	      		$('#commentTypeEditId').removeAttr("disabled");
	      		$('#commentTypeEditTdId').removeAttr("style");
     	      	$('#typeListTdId').css('display','block')
     	      	$('#commentShowTrId').css('display','table-row')
     	      	$('#predecessorAddTr').css('display','none')
     	      	$('#previousNote').html('')
     	      	$('#predecessorShowTr').css('display','none')
	      	 }
			 if(ac.commentType=='instruction'){
				 $('#mustVerifyId').css('display','table-row')
				 $('#mustVerifyEditTr').css('display','table-row')
				 
			 }else{
				 $('#mustVerifyId').css('display','none')
				 $('#mustVerifyEditTr').css('display','none')
			 }
	      	 if(ac.mustVerify != 0){
		      	 $('#mustVerifyShowId').attr('checked', true);
		      	 $('#mustVerifyEditId').attr('checked', true);
	      	 } else {
		      	 $('#mustVerifyShowId').attr('checked', false);
		      	 $('#mustVerifyEditId').attr('checked', false);
	      	 }
	      	 $('#statusShowId').html(ac.status);
	         $("#statusShowId").removeAttr('class')
	      	 $('#statusShowId').addClass(params.cssForCommentStatus);
	      	 $('#isResolvedEditId').val(ac.isResolved);
	      	
	      	 $('#dateResolvedId').html(params.dtResolved)
	      	 $('#dateResolvedEditId').html(params.dtResolved)
	      	 if(params.personResolvedObj != null){
		      	 $('#resolvedById').html(params.personResolvedObj.firstName+" "+params.personResolvedObj.lastName)
		      	 $('#resolvedByEditId').html(params.personResolvedObj.firstName+" "+params.personResolvedObj.lastName)
	      	 }else{
		      	 $('#resolvedById').html("")
		      	 $('#resolvedByEditId').html("")
	      	 }
	      	
	      	 $('#createdById').html(params.personCreateObj.firstName+" "+params.personCreateObj.lastName+" at "+params.dtCreated)
	      	 $('#createdByEditId').html(params.personCreateObj.firstName+" "+params.personCreateObj.lastName+" at "+params.dtCreated)
	      	 $('#resolutionId').html(ac.resolution)
	      	 $('#resolutionEditId').val(ac.resolution)
	      	 $('#commentEditId').val(ac.comment)
	      	 $('#commentTypeEditId').val(ac.commentType)
	      	 $('#commentTypeEditIdReadOnly').val(ac.commentType)
	      	 $('#categoryEditId').val(ac.category)
	      	 $('#commentCodeEditId').html(ac.commentCode)
	      	 $('#mustVerifyEditId').val(ac.mustVerify)
	      	 $('#isResolvedEditId').val(ac.isResolved)
	      	 if(action == 'edit'){
	      		updateStatusSelect(ac.id);
	      	    if(ac.assetEntity){
	    		       updateWorkflowTransitions(ac.assetEntity.id, ac.category, 'workFlowTransitionEditId', 'predecessorEditId',ac.id)
	    		}else{
	    		       updateWorkflowTransitions('', ac.category, 'workFlowTransitionEditId', 'predecessorEditId',ac.id)
	    		}
				$('#successorEditId').html(params.successorTable)
	            updateAssignedToList('assignedToEdit','assignedEditSpan',ac.id);
				
				commentChangeEdit('editResolveDiv','editCommentForm');
		      	$("#editCommentDialog").dialog('option', 'width', 'auto')
		      	$("#editCommentDialog").dialog('option', 'position', ['center','top']);
		      	$("#editCommentDialog").dialog("open")
		      	$("#showCommentDialog").dialog("close")
		      	loadEditPredecessor(ac.id);
	      	 } else if(action == 'show'){
	      	 	$("#showCommentDialog").dialog('option', 'width', 'auto')
	      	 	$("#showCommentDialog").dialog('option', 'position', ['center','top']);
	      	 	$("#showCommentDialog").dialog("open")
	      	 	$("#editCommentDialog").dialog("close")
	      	 }
	      	 if($("#selectTimedId").length > 0){
	      		 timedUpdate('never')
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
				  commentTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  var typeTd = document.createElement('td');
				  typeTd.id = 'type_'+assetComments.assetComment.id
				  typeTd.name = assetComments.assetComment.id
				  typeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  
				  var dueDateTd = document.createElement('td');
				  dueDateTd.id = 'type_'+assetComments.assetComment.id
				  dueDateTd.name = assetComments.assetComment.id
				  dueDateTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  
				  var categoryTd = document.createElement('td');
				  categoryTd.id = 'category_'+assetComments.assetComment.id
				  categoryTd.name = assetComments.assetComment.id
				  categoryTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  
				  var commentCodeTd = document.createElement('td');
				  commentCodeTd.id = 'commentCode_'+assetComments.assetComment.id
				  commentCodeTd.name = assetComments.assetComment.id
				  commentCodeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  
				  var resolveTd = document.createElement('td');
				  resolveTd.id = 'resolve_'+assetComments.assetComment.id
				  resolveTd.name = assetComments.assetComment.id
			      resolveTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}					      
			   	  var verifyTd = document.createElement('td');
				  verifyTd.id = 'verify_'+assetComments.assetComment.id
				  verifyTd.name = assetComments.assetComment.id
				  verifyTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  var image = document.createElement('img');
			      image.src = "../images/skin/database_edit.png"
			      image.border = 0
				  var link = document.createElement('a');
				  link.href = '#'
				  link.id = 'link_'+assetComments.assetComment.id
				  link.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+assetComments.assetComment.id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );}})} //;return false
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
	// Truncate the text 
	function truncate( text ){
		var trunc = text
		if(text){
			if(text.length > 30){
				trunc = trunc.substring(0, 30);
				trunc += '...'
			}
		}
		return trunc;
	}

function createIssue(){
	updateWorkflowTransitions( '', "general", "workFlowTransitionId", "predecessorId",'' );
	updateAssignedToList('assignedToSave','assignedCreateSpan',0);
	document.forms['createCommentForm'].commentType.value = 'issue'
	document.forms['createCommentForm'].commentType.disabled = 'disabled'
	commentChange('#createResolveDiv','createCommentForm')
	$('#assetEntityTrId').css('display','none')
	$('#createResolveDiv').css('display','table-row');
	$('#createCommentDialog').dialog('option', 'width', 'auto');
	$('#createCommentDialog').dialog('option', 'position', ['center','top']);
	$('#createCommentDialog').dialog('open');
	$('#showCommentDialog').dialog('close');
	$('#editCommentDialog').dialog('close');
	$('#moveEventTrId').css('display','table-row')
	$('#workFlowTransitionTrId').css('display','table-row')
	$('#predecessorHeadTrId').css('display','table-row')
	$('#predecessorTrId').css('display','table-row')
}	
function commentChange(resolveDiv,formName) {
	var type = 	document.forms[formName].commentType.value;
	$("#commentTypeCreateTdId").css('display', 'table-row');
	if(type == "issue"){
		var commentId=$('#commentId').val()
		updateAssignedToList('assignedToEdit','assignedEditSpan', commentId);
		//var now = new Date();
		//now.setDate(now.getDate() + 30)
	    //formatDate(now);
//		$("#dueDateTrId").css('display', 'table-row');
		$("#assignedToId").css('display', 'table-row');
		$("#assignedToTrEditId").css('display', 'table-row');
		$("#assignedToEditTdId").css('display', 'table-row');
		
		$("#typeCommentCreateId").css('display', 'none');
		$("#commentTypeCreateTdId").css('display', 'none');
		$("#commentTypeEditTdId").css('display', 'none');
		$("#typeListTdId").css('display', 'none');
		$('#deleteCommentId').css('display','none')
		
		$("#issueItemId").html('<label for="comment">Issue:</label>');
		$("#mustVerifyTd").css('display', 'none');
		$("#mustVerifyEditTr").css('display', 'none');
		$("#assignedToEditedId").css('display', 'table-row');
		$("#dueDateEditId").css('display', 'block');
		$('#estStartTrId').css('display', 'table-row');
		$('#estStartEditTrId').css('display', 'table-row');
//		$('#estFinishTrId').css('display', 'table-row');
		$('#actStartTrId').css('display', 'table-row');
		$(resolveDiv).css('display', 'table-row');
		$('#workFlowTransitionTrId').css('display','table-row')
		$('#workFlowTransitionEditTrId').css('display', 'table-row');
		$('relatedIssueEditId').css('display', 'table-row');
		$('#predecessorHeadTrId').css('display','table-row')
		$('#predecessorTrId').css('display','table-row')
		$("#predecessorAddTr").css('display', 'table-row');
		$("#predecessorTrEditId").css('display', 'table-row');
		$('#priorityTrId').css('display', 'table-row');
		$('#durationTrId').css('display', 'table-row');
		$("#durationEditId").css('display', 'table-row');
		document.forms[formName].mustVerify.checked = false;
		document.forms[formName].mustVerify.value = 0;
		document.forms[formName].isResolved.checked = false;
		document.forms[formName].isResolved.value = 0;
		
	}else if(type == "instruction"){
		document.forms[formName].mustVerify.checked = true;
		document.forms[formName].mustVerify.value = 1;
		$("#mustVerifyEditId").css('display', 'block');
		$(resolveDiv).css('display', 'none');
//		$("#dueDateTrId").css('display', 'none');
		//$("#typeCommentCreateId").css('display', 'table-row');
		//$("#commentType").css('display', 'table-row');
		$("#assignedToId").css('display', 'none');
		$("#mustVerifyTd").css('display', 'block');
		$("#mustVerifyEditTr").css('display', 'table-row');
		$("#assignedToEditedId").css('display', 'none');
		$("#assignedToTrEditId").css('display', 'none');
		$("#dueDateEditId").css('display', 'none');
		$('#workFlowTransitionTrId').css('display','none')
		$('#predecessorHeadTrId').css('display','none')
		$('#predecessorTrId').css('display','none')
		$("#commentEditId").html('<label for="comment">Comment:</label>');
		$("#issueItemId").html('<label for="comment">Comment:</label>');
		$('#estStartTrId').css('display', 'none');
//		$('#estFinishTrId').css('display', 'none');
		$('#actStartTrId').css('display', 'none');
		$('#priorityTrId').css('display', 'none');
		$('#durationTrId').css('display', 'none');
	}else{
		document.forms[formName].mustVerify.checked = false;
		document.forms[formName].mustVerify.value = 0;
		$(resolveDiv).css('display', 'none');
//		$("#dueDateTrId").css('display', 'none');
		//$("#typeCommentCreateId").css('display', 'table-row');
		//$("#commentType").css('display', 'table-row');
		$("#assignedToId").css('display', 'none');
		$("#mustVerifyTd").css('display', 'none')
		$("#mustVerifyEditTr").css('display', 'none');
		$("#issueItemId").html('<label for="comment">Comment:</label>');
		$("#assignedToEditedId").css('display', 'none');
		$("#assignedToTrEditId").css('display', 'none');
		$("#dueDateEditId").css('display', 'none');
		$("#commentEditId").html('<label for="comment">Comment:</label>');
		$('#workFlowTransitionTrId').css('display','none')
		$('#predecessorHeadTrId').css('display','none')
		$('#predecessorTrId').css('display','none')
		$('#estStartTrId').css('display', 'none');
//		$('#estFinishTrId').css('display', 'none');
		$('#actStartTrId').css('display', 'none');
		$('#priorityTrId').css('display', 'none');
		$('#durationTrId').css('display', 'none');
	}
}

function commentChangeEdit(resolveDiv,formName) {
var type = 	document.forms[formName].commentType.value;
if(type == "issue"){
	$('#noteEditId').val('')
	$("#dueDateEditId").css('display', 'block');
	//$("#commentTypeEditId").html('<label for="comment">Issue:</label>');
	$("#assignedToEditedId").css('display', 'table-row');
	$("#"+resolveDiv).css('display', 'block');
	//$('#estFinishTrEditId').css('display', 'block')
	//$('#workFlowTransitionEditTrId').css('display', 'block')
	//$('#priorityEditId').css('display', 'block')
	//$('#predecessorTrEditId').css('display', 'block')
	//$('#durationEditId').css('display', 'block')
	
}else{
	$("#assignedToEditedId").css('display', 'none');
	$("#"+resolveDiv).css('display', 'none');
	$("#dueDateEditId").css('display', 'none');
	//$("#commentTypeEditId").html('<label for="comment">Comment:</label>');
	$('#estFinishTrEditId').css('display', 'none')
	$('#workFlowTransitionEditTrId').css('display', 'none')
	$('#priorityEditId').css('display', 'none')
	$('#predecessorHeadTrId').css('display','none')
	$('#predecessorTrEditId').css('display', 'none')
	$('#durationEditId').css('display', 'none')
}
}

function commentChangeShow() {
	var type = 	$('#commentTypeTdId').html();
	if(type == "issue"){
		$('#showResolveDiv').css('display', 'block');
	}else{
		$('#showResolveDiv').css('display', 'none');
	}
}

/*UPDATE THE ASSET COMMENT ICON*/
function updateAssetCommentIcon( assetComments ){
	var link = document.createElement('a');
	link.href = '#'
		
	link.onclick = function(){setAssetId(assetComments.assetComment.assetEntity.id);new Ajax.Request('../assetEntity/listComments?id='+assetComments.assetComment.assetEntity.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'never');}})} //;return false
	if( assetComments.status ){
		link.innerHTML = "<img src=\"../i/db_table_red.png\" border=\"0px\">"
	}else{
		link.innerHTML = "<img src=\"../i/db_table_bold.png\" border=\"0px\">"
	}
	var iconObj = $('#icon_'+assetComments.assetComment.assetEntity.id);
	iconObj.html(link)
}

//
// Invoked by createCommentForm and editCommentDialog to make Ajax call to persist changes of new and existing assetComment classes
//
function resolveValidate(formName, idVal, redirectTo,open) {
	// Bump the list timer if it exists
	if ($("#selectTimedId").length > 0){ timedUpdate($("#selectTimedId").val()) }

	var type = 	document.forms[formName].commentType.value;
	if (type == ""){
		alert('Please select a comment type');
		return false;
	}
	if ( ($('#statusEditId').val() == "Completed" && $('#resolutionEditId').val() == '') &&
		 ($('#statusId').val() == "Completed" && $('#resolution').val() == '') ) {
		alert('Please enter a resolution');
		return false;
	}
	
	// idVal has the name of the element that holds the assetEntity.id (create) or assetComment.id  (update)
	var objId = ''
	if ($("#"+idVal).val()) { objId = $("#"+idVal).val() }

	// Get params from create or update forms approppriately
	var predArr = new Array();
	if (formName == "createCommentForm") {
		$('#taskDependencyTdId').html("")
		$('#relatedIssueEditId').html("")
		
		$('select[name="taskDependencySave"]').each(function(){
			predArr.push($(this).val())
	    });
	    predArr = removeDuplicateElement(predArr)
	    $('#predecessorTableId').html("")
		var url = '../assetEntity/saveComment'
		var params = { 'comment':$('#comment').val(), 'commentType':$('#commentType').val(),
			'isResolved':$('#isResolved').val(), 'resolution':$('#resolution').val(),
			'mustVerify':$('#mustVerify').val(), 'category':$('#createCategory').val(),
			'assignedTo':$('#assignedToSave').val(), 'dueDate':$('#dueDateCreateId').val(),
			'moveEvent':$('#moveEvent').val(), 'status':$('#statusId').val(),
			'estStart':$('#estStartCreateId').val(), 'estFinish':$('#estFinishCreateId').val(),
			'actStart':$('#actStartCreateId').val(), 'workflowTransition':$('#workFlowId').val(),
			'hardAssigned':$('#hardAssigned').val(),'priority':$('#priority').val(),
			'duration':$('#duration').val(),'durationScale':$('#durationScale').val(),
			'assetEntity':objId ,'override':$('#override').val(),'role':$('#roleType').val(),
			'taskDependency' : predArr,
			'manageDependency':1 };
		var completeFunc = function(e) { addCommentsToList(e); }
	} else {
		$('#taskDependencyTdId').html("")
		$('#relatedIssueEditId').html("")
		var url = '../assetEntity/updateComment'
		$('select[name="taskDependencyEdit"]').each(function(){
			predArr.push($(this).val())
	    });
	    predArr = removeDuplicateElement(predArr)
		var params = { 'comment':$('#commentEditId').val(), 'commentType':$('#commentTypeEditId').val(),
			'isResolved':$('#isResolvedEditId').val(), 'resolution':$('#resolutionEditId').val(), 
			'mustVerify':$('#mustVerifyEditId').val(), 'category':$('#categoryEditId').val(), 
			'assignedTo':$('#assignedToEdit').val(), 'dueDate':$('#dueDateEdit').val(), 
			'moveEvent':$('#moveEventEditId').val(), 'status':$('#statusEditId').val(),
//			'note':$('#noteEditId').val(),'assetEntity.id':$('#assetValueId').val(),
			'estStart':$('#estStartEditId').val(), 'estFinish':$('#estFinishEditId').val(),
			'actStart':$('#actStartEditId').val(), 'workflowTransition':$('#workFlowId').val(),
			'hardAssigned':$('#hardAssignedEdit').val(),'priority':$('#priorityEdit').val(),
			'duration':$('#durationEdit').val(),'durationScale':$('#durationScaleEdit').val(),
			'override':$('#overrideEdit').val(),'role':$('#roleTypeEdit').val(),
			'note':$('#noteEditId').val(),'taskDependency' : $("#taskDependencyEditId").val(),
			'id':objId, 'taskDependency':predArr,'manageDependency':1, 'open':open};
		var completeFunc = function(e) { updateCommentsOnList(e); }
	}
	if (redirectTo) { completeFunc = function(e) { updateCommentsLists(e); } }
	else if(open=='view'){ completeFunc = function(e) { showAssetCommentDialog( e , 'show'); } }
	
	jQuery.ajax({
		url: url,
		data: params,
		type:'POST',
		complete: completeFunc
	});
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
	new Ajax.Request('../manufacturer/getManufacturerAsJSON?id='+id,{
		asynchronous:false,
		evalScripts:true,
		onComplete:function(e){
			var manufacturer = eval('(' + e.responseText + ')')
			$("#showManuName").html( manufacturer.name )
			$("#showManuAka").html( manufacturer.aka )
			$("#showManuDescription").html( manufacturer.description )
			$("#show_manufacturerId").val( manufacturer.id )
			$("#manufacturerShowDialog").dialog("open")
		}
	})
}
function showModel(id){
	new Ajax.Request('../model/getModelAsJSON?id='+id,{
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
				$("#showModelFrontImage").html( "<img src='../model/getFrontImage/"+model.id+"' style='height: 50px; width: 100px;' id='rearImageId'>" )
			} else {
				$("#showModelFrontImage").html("")
			}
			if(model.rearImage != ''){
				$("#showModelRearImage").html( "<img src='../model/getRearImage/"+model.id+"' style='height: 50px; width: 100px;' id='rearImageId'>" )
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
function createNewAssetComment(asset, assetName){
	setAssetId( asset )
	updateWorkflowTransitions( asset, "general", "workFlowTransitionId", "predecessorId",'' );
	updateAssignedToList('assignedToSave','assignedCreateSpan',0);
	var name = assetName
	$('#createCommentDialog').dialog('option', 'width', 'auto');
	$('#assetEntityTrId').css('display','table-row')
	$('#assetEntityInputId').html(name)
	$('#typeCommentCreateId').removeAttr('style')
	$('#commentTypeCreateTdId').removeAttr('style')
	$('#createCommentDialog').dialog('open');
	$('#commentsListDialog').dialog('close');
	$('#editCommentDialog').dialog('close');
	$('#showCommentDialog').dialog('close');
	$('#showDialog').dialog('close');
	$('#editDialog').dialog('close');
	$('#createDialog').dialog('close');
	$('#filterDialog').dialog('close');
	document.createCommentForm.mustVerify.value=0;
	document.createCommentForm.reset();
	commentChange('#createResolveDiv','createCommentForm');
	
}
function createComments(asset, assetName){
	setAssetId( asset )
	updateWorkflowTransitions( asset, "general", "workFlowTransitionId", "predecessorId", '');
	updateAssignedToList('assignedToSave','assignedCreateSpan',0);
	var name = assetName
	$('#workFlowTransitionTrId').css('display','none')
	$('#predecessorTr').css('display','none')
	$('#createCommentDialog').dialog('option', 'width', '800px');
	$('#assetEntityTrId').css('display','table-row')
	$('#assetEntityInputId').html(name)
	$('#createCommentDialog').dialog('open');
	$('#typeCommentCreateId').removeAttr('style')
	$('#commentTypeCreateTdId').removeAttr('style')
	//$("#typeCommentCreateId").css('display', 'table-row');
	//$("#commentTypeCreateTdId").css('display', 'table-row');
	document.createCommentForm.mustVerify.value=0;
	document.createCommentForm.reset();
	commentChange('#createResolveDiv','createCommentForm');
	
}
function showAssetComment(id ,type){
	new Ajax.Request('../assetEntity/showComment?id='+id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, type );commentChangeShow();}})
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
function roleChange(value){
	if(value==''){
		$('#hardAssigned').attr('checked',true)
	}
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
function addPredecessor(issueCategory,predecessorCategory,comment,row,span){
	var rowNo = $("#predCount").val()
	var category 
		if(predecessorCategory){
			category =  $('#'+predecessorCategory).val() 
		}else{
			category =$('#'+issueCategory).val()
		}
	var commentId = comment ? $('#'+comment).val() : ''
	new Ajax.Request('../assetEntity/getPredecessor?category='+category+'&assetCommentId='+commentId,{asynchronous:false,evalScripts:true,
		 onComplete:function(e){
		     $('#'+span).html(e.responseText)
		     $('#taskDependencyTdId').html(e.responseText)
	         var taskRow
	         if(comment){
	           taskRow =  $('#taskDependencyRow tr').html().replace("predecessorCategoryId","predecessorCategoryId_"+rowNo).replace("taskDependencyId","taskDependencyEditId_"+rowNo).replace("taskDependencyTdId","taskDependencyEditTdId_"+rowNo).replace("taskDependencyEditId","taskDependencyEditId_"+rowNo).replace("fillPredecessor(this.id, this.value,'')","fillPredecessor(this.id, this.value,"+commentId+")")
	            $('#predecessorEditTableId').append("<tr id='row_Edit_"+rowNo+"'>"+taskRow+"<td><a href=\"javascript:deleteRow(\'row_Edit_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td><tr>")
	         }else{
	           taskRow =  $('#taskDependencyRow tr').html().replace("predecessorCategoryId","predecessorCategoryId_"+rowNo).replace("taskDependencyId","taskDependencyId_"+rowNo).replace("taskDependencyTdId","taskDependencySaveTdId_"+rowNo).replace("taskDependencyEditId","taskDependencyEditId_"+rowNo).replace(' name="taskDependencyEdit"','name="taskDependencySave"').replace("fillPredecessor(this.id, this.value,'')","fillPredecessor(this.id, this.value,'')")
	            $('#predecessorTableId').append("<tr id='row_d_"+rowNo+"'>"+taskRow+"<td><a href=\"javascript:deleteRow(\'row_d_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
	         }
		     
		     if(issueCategory){
		    	 $('#predecessorCategoryId_'+rowNo).val($('#'+issueCategory).val())
		     }
		     $("#predCount").val(parseInt(rowNo)+1)
	         $('#predecessorTr').show()
	         
		 }
	})
}
function fillPredecessor(id, category,commentId){
	var row = id.split('_')[1]
	new Ajax.Request('../assetEntity/getPredecessor?category='+category+'&assetCommentId='+commentId,{asynchronous:true,evalScripts:true,
		 onComplete:function(e){
			 var resp =  e.responseText.replace('taskDependencyId','taskDependencyId_'+row)
			 var data = e.responseText
		     $('#taskDependencySaveTdId_'+row).html(data)
		     $('#taskDependencyEditTdId_'+row).html(data)
		 }
	})
	
}
 function removeDuplicateElement(arrayName)
{
	  var newArray=new Array();
	  label:for(var i=0; i<arrayName.length;i++ )
	  {  
	  for(var j=0; j<newArray.length;j++ )
	  {
	  if(newArray[j]==arrayName[i]) 
	  continue label;
	  }
	  newArray[newArray.length] = arrayName[i];
	  }
	  return newArray;
}
function updateAssignedToList(forView,span,id){
	new Ajax.Request('../assetEntity/updateAssignedToSelect?forView='+forView+'&id='+id,{
		asynchronous:false, evalScripts:true,
		onSuccess:function(e){
			$('#'+span).html(e.responseText)
		},
		onFailure:function(jqXHR, textStatus, errorThrown){
			alert(stdErrorMsg)
		}
	})
}
function updateStatusSelect(taskId){
	new Ajax.Request('../assetEntity/updateStatusSelect?id='+taskId,{asynchronous:false,evalScripts:true,
		 onComplete:function(e){
			 $('#statusEditTrId').html(e.responseText)
		 }
	})
}
function loadEditPredecessor(id){
	new Ajax.Request('../assetEntity/loadPredecessor?id='+id,{asynchronous:true,evalScripts:true,
		onLoading:function(){
			$('#predecessorEditId').html('');
			var processTab = jQuery('#processDiv');
		    processTab.css("display", "table-row");
		    $('#processingId').show();
	    },
	    onSuccess:function(e){
	    	 var resp = e.responseText;
	    	 if(resp.length > 0){
		    	 $('#predecessorEditId').html(resp);
		    	 $('#processingId').hide();
	    	 }
		}
	})
}

