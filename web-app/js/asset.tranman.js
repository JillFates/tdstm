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
    			
    			var labelTd = "<td style='background:#f3f4f6;width:25%;' nowrap>"+attribute.label+""
			    if(requiredFields.contains(attribute.attributeCode)){
			    	var spanAst = "<span style='color:red;'>*</span>"//document.createElement("span")
				    labelTd += spanAst 
			    }
			    labelTd +="</td>"
			      
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
			      
		    var labelTd = "<td style='background:#f3f4f6;width:25%;' nowrap>"+attribute.label+""
		    
		    var labelTdE = ""
	    	if(attribute.attributeCode == "manufacturer" && attribute.value != "" && attribute.value != null){
	    		labelTdE = "<td style='width:25%;background:#f3f4f6;font-style:underline;' nowrap><a href='javascript:showManufacturer("+attribute.manufacturerId+")'>"+attribute.label+"</a></td>"
		    } else if(attribute.attributeCode == "model"&& attribute.value != "" && attribute.value != null ){
		    	labelTdE = "<td style='width:25%;background:#f3f4f6;font-style:underline;' nowrap><a href='javascript:showModel("+attribute.modelId+")'>"+attribute.label+"</a></td>"
		    } else {
		    	labelTdE = "<td style='background:#f3f4f6;width:25%;' nowrap>"+attribute.label+"</td>"
		    }
		    
		    if(requiredFields.contains(attribute.attributeCode)){
			  	var spanAst = "<span style='color:red;'>*</span>"//document.createElement("span")
			    labelTd += spanAst 
			    labelTdE += spanAst
		    }
		    var inputTd = ""
		    if(attribute.attributeCode == "manufacturer"){
		    	inputTd = "<td style='width:25%;color:#00f;font-style:underline;' nowrap><a href='javascript:showManufacturer("+attribute.manufacturerId+")'>"+attribute.value+"</a></td>"
		    } else if(attribute.attributeCode == "model"){
		    	inputTd = "<td style='width:25%;color:#00f;font-style:underline;' nowrap><a href='javascript:showModel("+attribute.modelId+")'>"+attribute.value+"</a></td>"
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
		var role = $("#role").val();
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
		      	for (var i=0; i < length; i++) {
		      	//generate dynamic rows	
		      	  var commentObj = assetComments[i]
		      	  var tr = document.createElement('tr');
		      	  tr.id = "commentTr_"+commentObj.commentInstance.id
		      	  tr.setAttribute('onmouseover','this.style.backgroundColor="white";');
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
			      resolveTd.appendChild( resolveVal )
			      verifyTd.appendChild( verifyText )
			      categoryTd.appendChild( categoryText )
			      commentCodeTd.appendChild( commentCodeText )
			      if ( role ) {
			      	tr.appendChild( editTd )
			      }
			      tr.appendChild( commentTd )
			      tr.appendChild( typeTd )	     					      
			      tr.appendChild( resolveTd )	     					      
			      tr.appendChild( verifyTd )
			      tr.appendChild( categoryTd )
			      tr.appendChild( commentCodeTd )
			      listTbody.appendChild( tr )
			      if(commentObj.commentInstance.isResolved == 1){
			      	resolveVal.checked = true ;
			      }
		       if(commentObj.commentInstance.mustVerify == 1){
			      	verifyText.checked = true ;
			      }
		      	}
		      listTable.append( listTbody )
		}
		
		$("#commentsListDialog").dialog('option', 'width', 800)	      	
		$("#commentsListDialog").dialog('option', 'position', ['center','top']);
   	$("#commentsListDialog").dialog("open")
   	if(action == 'never'){
   		timedUpdate('never')
   	}
	}
	function showAssetCommentDialog( e , action){
		$("#createCommentDialog").dialog("close")
		var assetComments = eval('(' + e.responseText + ')');
		if (assetComments) {
		if(assetComments[0].assetComment.comment == null){
		assetComments[0].assetComment.comment = "";
		}
		if(assetComments[0].assetComment.resolution == null){
		assetComments[0].assetComment.resolution = "";
		}
			 $('#commentId').val(assetComments[0].assetComment.id)
			 $('#updateCommentId').val(assetComments[0].assetComment.id)
	      	 $('#commentTdId').val(assetComments[0].assetComment.comment)
	      	 $('#commentTypeTdId').html(assetComments[0].assetComment.commentType)
	      	 $('#mustVerifyShowId').val(assetComments[0].assetComment.mustVerify)
	      	 $('#isResolvedId').val(assetComments[0].assetComment.isResolved)
	      	 $('#categoryTdId').html(assetComments[0].assetComment.category)
	      	 $('#commentCodeTdId').html(assetComments[0].assetComment.commentCode)
	      	 if(assetComments[0].assetComment.mustVerify != 0){
	      	 $('#mustVerifyShowId').attr('checked', true);
	      	 $('#mustVerifyEditId').attr('checked', true);
	      	 } else {
	      	 $('#mustVerifyShowId').attr('checked', false);
	      	 $('#mustVerifyEditId').attr('checked', false);
	      	 }
	      	 if(assetComments[0].assetComment.isResolved != 0){
	      	 $('#isResolvedId').attr('checked', true);
	      	 $('#isResolvedEditId').attr('checked', true);
	      	 } else {
	      	 $('#isResolvedId').attr('checked', false);
	      	 $('#isResolvedEditId').attr('checked', false);
	      	 }
	      	 $('#dateResolvedId').html(assetComments[0].dtResolved)
	      	 $('#dateResolvedEditId').html(assetComments[0].dtResolved)
	      	 $('#dateCreatedId').html(assetComments[0].dtCreated)
	      	 $('#dateCreatedEditId').html(assetComments[0].dtCreated)
	      	 if(assetComments[0].personResolvedObj != null){
		      	 $('#resolvedById').html(assetComments[0].personResolvedObj.firstName+" "+assetComments[0].personResolvedObj.lastName)
		      	 $('#resolvedByEditId').html(assetComments[0].personResolvedObj.firstName+" "+assetComments[0].personResolvedObj.lastName)
	      	 }else{
		      	 $('#resolvedById').html("")
		      	 $('#resolvedByEditId').html("")
	      	 }
	      	
	      	 $('#createdById').html(assetComments[0].personCreateObj.firstName+" "+assetComments[0].personCreateObj.lastName)
	      	 $('#createdByEditId').html(assetComments[0].personCreateObj.firstName+" "+assetComments[0].personCreateObj.lastName)
	      	 $('#resolutionId').val(assetComments[0].assetComment.resolution)
	      	 $('#resolutionEditId').val(assetComments[0].assetComment.resolution)
	      	 $('#commentEditId').val(assetComments[0].assetComment.comment)
	      	 $('#commentTypeEditId').val(assetComments[0].assetComment.commentType)
	      	 $('#categoryEditId').html(assetComments[0].assetComment.category)
	      	 $('#commentCodeEditId').html(assetComments[0].assetComment.commentCode)
	      	 $('#mustVerifyEditId').val(assetComments[0].assetComment.mustVerify)
	      	 $('#isResolvedEditId').val(assetComments[0].assetComment.isResolved)
	      	 if(action == 'edit'){
		      	$("#editCommentDialog").dialog('option', 'width', 'auto')
		      	$("#editCommentDialog").dialog('option', 'position', ['center','top']);
		      	$("#editCommentDialog").dialog("open")
		      	$("#showCommentDialog").dialog("close")
	      	 } else if(action == 'show'){
	      	 	$("#showCommentDialog").dialog('option', 'width', 'auto')
	      	 	$("#showCommentDialog").dialog('option', 'position', ['center','top']);
	      	 	$("#showCommentDialog").dialog("open")
	      	 	$("#editCommentDialog").dialog("close")
	      	 }
	      	timedUpdate('never')
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
		      	  tr.id = "commentTr_"+assetComments[0].assetComment.id
			      tr.setAttribute('onmouseover','this.style.backgroundColor="white";');
			      var editTd = document.createElement('td');
				  var commentTd = document.createElement('td');
				  commentTd.id = 'comment_'+assetComments[0].assetComment.id
				  commentTd.name = assetComments[0].assetComment.id
				  commentTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  var typeTd = document.createElement('td');
				  typeTd.id = 'type_'+assetComments[0].assetComment.id
				  typeTd.name = assetComments[0].assetComment.id
				  typeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  
				  var categoryTd = document.createElement('td');
				  categoryTd.id = 'category_'+assetComments[0].assetComment.id
				  categoryTd.name = assetComments[0].assetComment.id
				  categoryTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  
				  var commentCodeTd = document.createElement('td');
				  commentCodeTd.id = 'commentCode_'+assetComments[0].assetComment.id
				  commentCodeTd.name = assetComments[0].assetComment.id
				  commentCodeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  
				  var resolveTd = document.createElement('td');
				  resolveTd.id = 'resolve_'+assetComments[0].assetComment.id
				  resolveTd.name = assetComments[0].assetComment.id
			      resolveTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}					      
			   	  var verifyTd = document.createElement('td');
				  verifyTd.id = 'verify_'+assetComments[0].assetComment.id
				  verifyTd.name = assetComments[0].assetComment.id
				  verifyTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
				  var image = document.createElement('img');
			      image.src = "../images/skin/database_edit.png"
			      image.border = 0
				  var link = document.createElement('a');
				  link.href = '#'
				  link.id = 'link_'+assetComments[0].assetComment.id
				  link.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+assetComments[0].assetComment.id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );commentChangeEdit('editResolveDiv','editCommentForm');}})} //;return false
			      var commentText = document.createTextNode(truncate(assetComments[0].assetComment.comment));
			      var typeText = document.createTextNode(assetComments[0].assetComment.commentType);
			      
			      var categoryText = document.createTextNode(assetComments[0].assetComment.category);
			      var commentCodeText = document.createTextNode(assetComments[0].assetComment.commentCode);
			      
			      var resolveVal
			      if(assetComments[0].assetComment.commentType != "issue"){
			      resolveVal = document.createTextNode('');
			      }else{
			      resolveVal = document.createElement('input')
			      resolveVal.id = 'verifyResolved_'+assetComments[0].assetComment.id
			      resolveVal.type = 'checkbox'					     
			      resolveVal.disabled = 'disabled'
			     
			      }
			      var verifyText = document.createElement('input')
			      verifyText.id = 'verifyText_'+assetComments[0].assetComment.id
			      verifyText.type = 'checkbox'
			      verifyText.disabled = 'disabled'
			     
			      //createTextNode(assetComments.mustVerify);
			      link.appendChild( image )
				  editTd.appendChild( link  )					      
			      commentTd.appendChild( commentText )
			      typeTd.appendChild( typeText )
			      resolveTd.appendChild( resolveVal )
			      verifyTd.appendChild( verifyText )
			      categoryTd.appendChild( categoryText )
			      commentCodeTd.appendChild( commentCodeText )
			      tr.appendChild( editTd )
			      tr.appendChild( commentTd )
			      tr.appendChild( typeTd )
			      tr.appendChild( resolveTd )
			      tr.appendChild( verifyTd )
			      tr.appendChild( categoryTd )
			      tr.appendChild( commentCodeTd )
			      tbody.append( tr )
			      if(assetComments[0].assetComment.isResolved == 1){
			      	resolveVal.checked = true;
			      }
			       if(assetComments[0].assetComment.mustVerify != 0){
			      	verifyText.checked = true
			      }
			  	updateAssetCommentIcon( assetComments[0] );
		} else {
				alert("Comment not created")
		}
	}
	// update comments 
	function updateCommentsOnList( e ){
	var assetComments = eval('(' + e.responseText + ')');
		if (assetComments != "") {
			$("#editCommentDialog").dialog("close")
	      	//generate dynamic rows	
	      	  var tr = $('#commentTr_'+assetComments[0].assetComment.id);
	      	  tr.css( 'background', '#65a342' );
	      	  if(assetComments[0].assetComment.mustVerify != 0){
		      $('#verifyText_'+assetComments[0].assetComment.id).attr('checked', true);
		      } else {
		      $('#verifyText_'+assetComments[0].assetComment.id).attr('checked', false);
		      }
		      if(assetComments[0].assetComment.commentType != "issue"){
		      	  $('#resolve_'+assetComments[0].assetComment.id).html("");
		      }else{
			      var checkResolveTd = $('#verifyResolved_'+assetComments[0].assetComment.id);
			      if(checkResolveTd){
			      	checkResolveTd.remove();
			      }
		      	  var resolveVal = document.createElement('input')
			      resolveVal.id = 'verifyResolved_'+assetComments[0].assetComment.id
			      resolveVal.type = 'checkbox'
			      resolveVal.disabled = 'disabled'
			      $('#resolve_'+assetComments[0].assetComment.id).append( resolveVal )
			     
			      if(assetComments[0].assetComment.isResolved != 0){
			      	$('#verifyResolved_'+assetComments[0].assetComment.id).attr('checked', true);
			      } else {
			      	$('#verifyResolved_'+assetComments[0].assetComment.id).attr('checked', false);
			      }
		      }
		      $('#type_'+assetComments[0].assetComment.id).html(assetComments[0].assetComment.commentType);
		      $('#comment_'+assetComments[0].assetComment.id).html(truncate(assetComments[0].assetComment.comment));
		      updateAssetCommentIcon( assetComments[0] )
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
	
function commentChange(resolveDiv,formName) {
var type = 	document.forms[formName].commentType.value;
if(type == "issue"){
	$(resolveDiv).css('display', 'block');
	document.forms[formName].mustVerify.checked = false;
	document.forms[formName].mustVerify.value = 0;
	document.forms[formName].isResolved.checked = false;
	document.forms[formName].isResolved.value = 0;
}else if(type == "instruction"){
	document.forms[formName].mustVerify.checked = true;
	document.forms[formName].mustVerify.value = 1;
	$(resolveDiv).css('display', 'none');
}else{
	document.forms[formName].mustVerify.checked = false;
	document.forms[formName].mustVerify.value = 0;
	$(resolveDiv).css('display', 'none');
}
}

function commentChangeEdit(resolveDiv,formName) {
var type = 	document.forms[formName].commentType.value;
if(type == "issue"){
	$("#"+resolveDiv).css('display', 'block');
}else{
	$("#"+resolveDiv).css('display', 'none');
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

function resolveValidate(formName,idVal){
	var type = 	document.forms[formName].commentType.value;
	if(type != "issue"){
		document.forms[formName].isResolved.value = 0;
	}
	var resolveBoo = document.forms[formName].isResolved.checked;
	var resolveVal = document.forms[formName].resolution.value;
	var assetId = $("#"+idVal).val()
	if(type == ""){
		alert('Please select comment type');
		return false;
	}else if(resolveBoo){
		if(resolveVal != ""){
			if(formName == "createCommentForm"){
				if($("#selectTimedId").length > 0){
					timedUpdate($("#selectTimedId").val())
				}
				new Ajax.Request('../assetEntity/saveComment?assetEntity.id='+parseInt(assetId)+'&comment='+document.forms[formName].comment.value+'&isResolved='+document.forms[formName].isResolved.value+'&resolution='+document.forms[formName].resolution.value+'&commentType='+document.forms[formName].commentType.value+'&mustVerify='+document.forms[formName].mustVerify.value+'&category='+document.forms[formName].category.value,{asynchronous:true,evalScripts:true,onComplete:function(e){addCommentsToList(e);}})
			}else{
				new Ajax.Request('../assetEntity/updateComment?id='+parseInt(assetId)+'&comment='+document.forms[formName].comment.value+'&isResolved='+document.forms[formName].isResolved.value+'&resolution='+document.forms[formName].resolution.value+'&commentType='+document.forms[formName].commentType.value+'&mustVerify='+document.forms[formName].mustVerify.value,{asynchronous:true,evalScripts:true,onComplete:function(e){updateCommentsOnList(e);}})
			}
		}else{
			alert('Please enter resolution');
			return false;
		}
	}else{
		if(formName == "createCommentForm"){
			if($("#selectTimedId").length > 0){
				timedUpdate($("#selectTimedId").val())
			}
			new Ajax.Request('../assetEntity/saveComment?assetEntity.id='+assetId+'&comment='+document.forms[formName].comment.value+'&isResolved='+document.forms[formName].isResolved.value+'&resolution='+document.forms[formName].resolution.value+'&commentType='+document.forms[formName].commentType.value+'&mustVerify='+document.forms[formName].mustVerify.value+'&category='+document.forms[formName].category.value,{asynchronous:true,evalScripts:true,onComplete:function(e){addCommentsToList(e);}})
		}else{
			new Ajax.Request('../assetEntity/updateComment?id='+assetId+'&comment='+document.forms[formName].comment.value+'&isResolved='+document.forms[formName].isResolved.value+'&resolution='+document.forms[formName].resolution.value+'&commentType='+document.forms[formName].commentType.value+'&mustVerify='+document.forms[formName].mustVerify.value,{asynchronous:true,evalScripts:true,onComplete:function(e){updateCommentsOnList(e);}})
		}
	}
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
			$("#showModelPower").html( model.powerUse )
			
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
