var requiredFields = ["assetName","assetTag"];
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
    			var createDiv = document.getElementById("createDiv");
    			//var createTable = document.getElementById("createTable");
    			var tb = document.getElementById('createFormTbodyId');
    			var autoComp = new Array();
	    if(tb != null){
	      createDiv.removeChild(tb);
	    }
    			// create tbody for CreateTable
    			var tbody = document.createElement('table');
		tbody.id = "createFormTbodyId";
		// Rebuild the select
	      if (assetEntityAttributes != "") {
		      var length = assetEntityAttributes.length
		      var halfLength = getLength(length) 
		      var tr = document.createElement('tr');
			  var tdLeft = document.createElement('td');
			  var tdRight = document.createElement('td');
			  var tableLeft = document.createElement('table');
			  tableLeft.style.width = '50%'
			  tableLeft.style.border = '0'
			  var tableRight = document.createElement('table');
			  tableRight.style.width = '50%'
			  tableRight.style.border = '0'
		      for (var i=0; i < halfLength; i ++ ) {
			      var attributeLeft = assetEntityAttributes[i]
			      var trLeft = document.createElement('tr');
			      var inputTdLeft = document.createElement('td');
			      var labelTdLeft = document.createElement('td');
			      var labelLeft = document.createTextNode(attributeLeft.label);
			      labelTdLeft.appendChild( labelLeft )
			      if(requiredFields.contains(attributeLeft.attributeCode)){
				      var spanAst = document.createElement("span")
				      spanAst.style.color = 'red';
				      spanAst.appendChild(document.createTextNode("*"))
				      labelTdLeft.appendChild( spanAst )
			      }
			      var inputFieldLeft = getInputType(attributeLeft); 
			      inputFieldLeft.id = attributeLeft.attributeCode+'Id';
			      inputFieldLeft.setAttribute('name',attributeLeft.attributeCode); 
			      inputTdLeft.appendChild( inputFieldLeft )
			      labelTdLeft.style.background = '#f3f4f6 '
			      labelTdLeft.style.width = '25%'
			      labelTdLeft.noWrap = 'nowrap'
			      trLeft.appendChild( labelTdLeft )
			      trLeft.appendChild( inputTdLeft )
			      tableLeft.appendChild( trLeft )
		      }
		      for (var i=halfLength; i < length; i ++ ) {
			      var attributeRight = assetEntityAttributes[i]
			      var trRight = document.createElement('tr');
			      var inputTdRight = document.createElement('td');
			      var labelTdRight = document.createElement('td');
			      var labelRight = document.createTextNode(attributeRight.label);
			      labelTdRight.appendChild( labelRight )
			      var inputFieldRight = getInputType(attributeRight); 
			      inputFieldRight.id = attributeRight.attributeCode+'Id';
			      inputFieldRight.setAttribute('name',attributeRight.attributeCode);
			      inputTdRight.appendChild( inputFieldRight )
			      labelTdRight.style.background = '#f3f4f6 '
			      labelTdRight.style.width = '25%'
			      labelTdRight.noWrap = 'nowrap'
			      trRight.appendChild( labelTdRight )
			      trRight.appendChild( inputTdRight )
			      tableRight.appendChild( trRight )
		      }
		      for (var i=0; i < length; i++ ) {
		      	var attribute = assetEntityAttributes[i]
		      	if(attribute.frontendInput == 'autocomplete'){
		      		autoComp.push(attribute.attributeCode)
		      	}
		      }
		      tdLeft.appendChild( tableLeft )
		      tdRight.appendChild( tableRight )
		      tr.appendChild( tdLeft )
		      tr.appendChild( tdRight )
		      filedRequiredMess(tbody)
			  tbody.appendChild( tr )
	      }
	      createDiv.appendChild( tbody )			     
	      if(browser == 'Microsoft Internet Explorer') {
	      createDiv.innerHTML += "";
	      }
	      new Ajax.Request(contextPath+'/assetEntity/getAutoCompleteDate?autoCompParams='+autoComp,{asynchronous:true,evalScripts:true,onComplete:function(e){createAutoComplete(e);}})
 }
 function filedRequiredMess( table ){
	 
	 var etr = document.createElement('tr');
     var etd = document.createElement('td');
     etd.colSpan="4"
     var divText = document.createElement('div');
     var spanText = document.createTextNode("Fields marked ( * ) are mandatory ");

     divText.className = "required";
     divText.appendChild( spanText );
     etd.appendChild( divText );
     etr.appendChild( etd );
     table.appendChild( etr )
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
 
  // function to show asset dialog
  function showAssetDialog( e , action ) {
    	$('#createCommentDialog').dialog('close');
    	$('#commentsListDialog').dialog('close');
    	$('#editCommentDialog').dialog('close');
    	$('#showCommentDialog').dialog('close');
		$('#changeStatusDialog').dialog('close');
		$('#filterDialog').dialog('close');
    	 var browser=navigator.appName;
    			var assetEntityAttributes = eval('(' + e.responseText + ')');
    			var autoComp = new Array();
    			var showDiv = document.getElementById("showDiv");
    			var editDiv = document.getElementById("editDiv");
    			var stb = document.getElementById('showTbodyId');
	    if(stb != null){
	      showDiv.removeChild(stb);
	    }
    			var etb = document.getElementById('editTbodyId');
	    if(etb != null){
	      editDiv.removeChild(etb);
	    }
    			// create tbody for CreateTable
    			var stbody = document.createElement('table');
		stbody.id = "showTbodyId";
    			var etbody = document.createElement('table');
		etbody.id = "editTbodyId";
		// Rebuild the select
	      if (assetEntityAttributes) {
		      var length = assetEntityAttributes.length;
		      var halfLength = getLength(length); 
		      var str = document.createElement('tr');
		      var etr = document.createElement('tr');
			  var stdLeft = document.createElement('td');
			  stdLeft.style.width = '50%';
			  var etdLeft = document.createElement('td');
			  var stdRight = document.createElement('td');
			  stdRight.style.width = '50%';
			  var etdRight = document.createElement('td');
			  var stableLeft = document.createElement('table');
			  var etableLeft = document.createElement('table');
			  stableLeft.style.width = '50%';
			  stableLeft.style.border = '0';
			  etableLeft.style.width = '50%';
			  etableLeft.style.border = '0';
			  var stableRight = document.createElement('table');
			  var etableRight = document.createElement('table');
			  stableRight.style.width = '50%';
			  stableRight.style.border = '0';
			  etableRight.style.width = '50%';
			  etableRight.style.border = '0';
		      	for (var i=0; i < halfLength; i++ ) {
			      var attributeLeft = assetEntityAttributes[i];
			      var strLeft = document.createElement('tr');
			      var etrLeft = document.createElement('tr');
			      // td for Show page
			      var inputTdLeft = document.createElement('td');
			      var labelTdLeft = document.createElement('td');
			      labelTdLeft.noWrap = 'nowrap';
			      var labelLeft = document.createTextNode(attributeLeft.label);
			      labelTdLeft.appendChild( labelLeft );
			      var inputFieldLeft = document.createTextNode(attributeLeft.value);
			      inputTdLeft.appendChild( inputFieldLeft );
			      labelTdLeft.style.background = '#f3f4f6 ';
			      labelTdLeft.style.width = '25%';
			      inputTdLeft.style.width = '25%';
			      strLeft.appendChild( labelTdLeft );
			      strLeft.appendChild( inputTdLeft );
			      
			      // td for Edit page
			      var inputTdELeft = document.createElement('td');
			      var labelTdELeft = document.createElement('td');
			      labelTdELeft.noWrap = 'nowrap';
			      var labelELeft = document.createTextNode(attributeLeft.label);
			      labelTdELeft.appendChild( labelELeft );
			      if(requiredFields.contains(attributeLeft.attributeCode)){
				      var spanAst = document.createElement("span")
				      spanAst.style.color = 'red';
				      spanAst.appendChild(document.createTextNode("*"))
				      labelTdELeft.appendChild( spanAst )
			      }
			      var inputFieldELeft = getInputType(attributeLeft);
			      	 inputFieldELeft.value = attributeLeft.value;
					  inputFieldELeft.id = 'edit'+attributeLeft.attributeCode+'Id';							 
					 
			      inputTdELeft.appendChild( inputFieldELeft );
			  
			      labelTdELeft.style.background = '#f3f4f6 ';
			      labelTdELeft.style.width = '25%';
			      inputTdELeft.style.width = '25%';
			      etrLeft.appendChild( labelTdELeft );
			      etrLeft.appendChild( inputTdELeft );
			      stableLeft.appendChild( strLeft );
			     etableLeft.appendChild( etrLeft );
		      	
		      	}
		      	for (var i=halfLength; i < length; i++ ) {
			      var attributeRight = assetEntityAttributes[i];
			      var strRight = document.createElement('tr');
			      var etrRight = document.createElement('tr');
			      // td for Show page
			      var inputTdRight = document.createElement('td');
			      var labelTdRight = document.createElement('td');
			      labelTdRight.noWrap = 'nowrap';
			      var labelRight = document.createTextNode(attributeRight.label);
			      labelTdRight.appendChild( labelRight );
			      var inputFieldRight = document.createTextNode(attributeRight.value);
			      inputTdRight.appendChild( inputFieldRight );
			      labelTdRight.style.background = '#f3f4f6 ';
			      labelTdRight.style.width = '25%';
			      inputTdRight.style.width = '25%';
			      strRight.appendChild( labelTdRight );
			      strRight.appendChild( inputTdRight );
			      
			      // td for Edit page
			      var inputTdERight = document.createElement('td');
			      var labelTdERight = document.createElement('td');
			      labelTdERight.noWrap = 'nowrap';
			      var labelERight = document.createTextNode(attributeRight.label);
			      labelTdERight.appendChild( labelERight );
			      var inputFieldERight = getInputType(attributeRight);
			      	  inputFieldERight.value = attributeRight.value;
					  inputFieldERight.id = 'edit'+attributeRight.attributeCode+'Id';
			      inputTdERight.appendChild( inputFieldERight );
			      labelTdERight.style.background = '#f3f4f6 ';
			      labelTdERight.style.width = '25%';
			      inputTdERight.style.width = '25%';
			      etrRight.appendChild( labelTdERight );
			      etrRight.appendChild( inputTdERight );
			      stableRight.appendChild( strRight );
			     etableRight.appendChild( etrRight );
		      	
		      	}
		      	for (var i=0; i < length; i++ ) {
			      	var attribute = assetEntityAttributes[i];
			      	if(attribute.frontendInput == 'autocomplete'){
			      		autoComp.push(attribute.attributeCode);
			      	}
		      	}
		  stdLeft.appendChild( stableLeft );
	      etdLeft.appendChild( etableLeft );
		  stdRight.appendChild( stableRight );
		  etdRight.appendChild( etableRight );
		  str.appendChild( stdLeft );
		  etr.appendChild( etdLeft );
		  str.appendChild( stdRight );
		  etr.appendChild( etdRight );
		  stbody.appendChild( str );
		  filedRequiredMess(etbody)
		  etbody.appendChild( etr );
	      }
	      
	     showDiv.appendChild( stbody )
	      showDiv.innerHTML += "";
	     editDiv.appendChild( etbody );
	      if(browser == 'Microsoft Internet Explorer') {
			editDiv.innerHTML += "";
		  } 
	      
	     new Ajax.Request(contextPath+'/assetEntity/getAutoCompleteDate?autoCompParams='+autoComp,{asynchronous:true,evalScripts:true,onComplete:function(e){updateAutoComplete(e);}}) 
	  $("#createDialog").dialog("close");
	  if(action == 'edit'){
	      $("#editDialog").dialog('option', 'width', 600);
	      $("#editDialog").dialog('option', 'position', ['center','top']);
	      $("#editDialog").dialog("open");
	      $("#showDialog").dialog("close");
      } else if(action == 'show'){
          $("#showDialog").dialog('option', 'width', 600);
	      $("#showDialog").dialog('option', 'position', ['center','top']);
	      $("#showDialog").dialog("open");
	      $("#editDialog").dialog("close");
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
    new Ajax.Request(contextPath+'/assetEntity/updateAssetEntity?id='+assetId+'&assetEntityParams='+safeQueryString,{asynchronous:true,evalScripts:true,onComplete:function(e){showEditAsset(e);}})
    
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
 function getInputType( attribute ){
 	var name = attribute.attributeCode
  	var type = attribute.frontendInput
  	var options = attribute.options
  	var browser=navigator.appName;
  	var inputField
  		if(type == 'select'){
			if(browser == 'Microsoft Internet Explorer') {
				inputField = document.createElement('<select name='+name +' />');
			} else {
				inputField = document.createElement('select');
				inputField.name = name ;
			}
			var inputOption = document.createElement('option');
			inputOption.value = ''
			inputOption.innerHTML = 'please select'
			inputField.appendChild(inputOption)
			if (options) {
				var length = options.length
			    for (var i=0; i < length; i++) {
					var optionObj = options[i]
				    var popt = document.createElement('option');
				    popt.innerHTML = optionObj.option
				    popt.value = optionObj.option
				    if(attribute.value == optionObj.option){
				   		popt.selected = true
				    }
				    try {
				      	inputField.appendChild(popt, null) // standards compliant; doesn't work in IE
				    } catch(ex) {
				      	inputField.appendChild(popt) // IE only
				    }
			      }
			 }						
		} else {
			if(browser == 'Microsoft Internet Explorer') {
  			 	inputField = document.createElement('<input type="text" name='+name +' />');
  			 } else {
  			 	inputField = document.createElement('input');
				inputField.type = "text";
				inputField.name = name;
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
