
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset Entity List</title>

<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="jquery.bgiframe.min"/>
<jq:plugin name="jquery.autocomplete"/>
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />

<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />

<script>
	      $(document).ready(function() {
	        $("#showDialog").dialog({ autoOpen: false })
	        $("#editDialog").dialog({ autoOpen: false })
	        $("#createDialog").dialog({ autoOpen: false })
	        $("#commentsListDialog").dialog({ autoOpen: false })
	        $("#createCommentDialog").dialog({ autoOpen: false })
	        $("#showCommentDialog").dialog({ autoOpen: false })
	        $("#editCommentDialog").dialog({ autoOpen: false })
	      })
</script>
<script type="text/javascript">	
		    function showAssetDialog( e , action ) {
		    	$('#createCommentDialog').dialog('close');
		    	$('#commentsListDialog').dialog('close');
		    	$('#editCommentDialog').dialog('close');
		    	$('#showCommentDialog').dialog('close');
      			var assetEntityAttributes = eval('(' + e.responseText + ')');
      			var autoComp = new Array()
      			var showTable = document.getElementById("showTable");
      			var editTable = document.getElementById("editTable");
      			var stb = document.getElementById('showTbodyId')
			    if(stb != null){
			      showTable.removeChild(stb)
			    }
      			var etb = document.getElementById('editTbodyId')
			    if(etb != null){
			      editTable.removeChild(etb)
			    }
      			// create tbody for CreateTable
      			var stbody = document.createElement('tbody');
				stbody.id = "showTbodyId"
      			var etbody = document.createElement('tbody');
				etbody.id = "editTbodyId"
				// Rebuild the select
			      if (assetEntityAttributes) {
				      var length = assetEntityAttributes.length
				      var halfLength = getLength(length) 
				      var str = document.createElement('tr');
				      var etr = document.createElement('tr');
					  var stdLeft = document.createElement('td');
					  stdLeft.style.width = '50%'
					  var etdLeft = document.createElement('td');
					  var stdRight = document.createElement('td');
					  stdRight.style.width = '50%'
					  var etdRight = document.createElement('td');
					  var stableLeft = document.createElement('table');
					  var etableLeft = document.createElement('table');
					  stableLeft.style.width = '50%'
					  stableLeft.style.border = '0'
					  etableLeft.style.width = '50%'
					  etableLeft.style.border = '0'
					  var stableRight = document.createElement('table');
					  var etableRight = document.createElement('table');
					  stableRight.style.width = '50%'
					  stableRight.style.border = '0'
					  etableRight.style.width = '50%'
					  etableRight.style.border = '0'
				      	for (var i=0; i < halfLength; i++ ) {
					      var attributeLeft = assetEntityAttributes[i]
					      var strLeft = document.createElement('tr');
					      var etrLeft = document.createElement('tr');
					      // td for Show page
					      var inputTdLeft = document.createElement('td');
					      var labelTdLeft = document.createElement('td');
					      labelTdLeft.noWrap = 'nowrap'
					      var labelLeft = document.createTextNode(attributeLeft.label);
					      labelTdLeft.appendChild( labelLeft )
					      var inputFieldLeft = document.createTextNode(attributeLeft.value);
					      inputTdLeft.appendChild( inputFieldLeft )
					      labelTdLeft.style.backgroundColor = '#f3f4f6 '
					      labelTdLeft.style.width = '25%'
					      inputTdLeft.style.width = '25%'
					      strLeft.appendChild( labelTdLeft )
					      strLeft.appendChild( inputTdLeft )
					      
					      // td for Edit page
					      var inputTdELeft = document.createElement('td');
					      var labelTdELeft = document.createElement('td');
					      labelTdELeft.noWrap = 'nowrap'
					      var labelELeft = document.createTextNode(attributeLeft.label);
					      labelTdELeft.appendChild( labelELeft )
					      var inputFieldELeft = getInputType(attributeLeft);
					      	  inputFieldELeft.value = attributeLeft.value;
							  inputFieldELeft.id = 'edit'+attributeLeft.attributeCode+'Id';
					      inputTdELeft.appendChild( inputFieldELeft )
					      labelTdELeft.style.backgroundColor = '#f3f4f6 '
					      labelTdELeft.style.width = '25%'
					      inputTdELeft.style.width = '25%'
					      etrLeft.appendChild( labelTdELeft )
					      etrLeft.appendChild( inputTdELeft )
					      stableLeft.appendChild( strLeft )
					     etableLeft.appendChild( etrLeft )
				      	
				      	}
				      	for (var i=halfLength; i < length; i++ ) {
					      var attributeRight = assetEntityAttributes[i]
					      var strRight = document.createElement('tr');
					      var etrRight = document.createElement('tr');
					      // td for Show page
					      var inputTdRight = document.createElement('td');
					      var labelTdRight = document.createElement('td');
					      labelTdRight.noWrap = 'nowrap'
					      var labelRight = document.createTextNode(attributeRight.label);
					      labelTdRight.appendChild( labelRight )
					      var inputFieldRight = document.createTextNode(attributeRight.value);
					      inputTdRight.appendChild( inputFieldRight )
					      labelTdRight.style.backgroundColor = '#f3f4f6 '
					      labelTdRight.style.width = '25%'
					      inputTdRight.style.width = '25%'
					      strRight.appendChild( labelTdRight )
					      strRight.appendChild( inputTdRight )
					      
					      // td for Edit page
					      var inputTdERight = document.createElement('td');
					      var labelTdERight = document.createElement('td');
					      labelTdERight.noWrap = 'nowrap'
					      var labelERight = document.createTextNode(attributeRight.label);
					      labelTdERight.appendChild( labelERight )
					      var inputFieldERight = getInputType(attributeRight);
					      	  inputFieldERight.value = attributeRight.value;
							  inputFieldERight.id = 'edit'+attributeRight.attributeCode+'Id';
					      inputTdERight.appendChild( inputFieldERight )
					      labelTdERight.style.backgroundColor = '#f3f4f6 '
					      labelTdERight.style.width = '25%'
					      inputTdERight.style.width = '25%'
					      etrRight.appendChild( labelTdERight )
					      etrRight.appendChild( inputTdERight )
					      stableRight.appendChild( strRight )
					     etableRight.appendChild( etrRight )
				      	
				      	}
				      	for (var i=0; i < length; i++ ) {
					      	var attribute = assetEntityAttributes[i]
					      	if(attribute.frontendInput == 'autocomplete'){
					      		autoComp.push(attribute.attributeCode)
					      	}
				      	}
				  stdLeft.appendChild( stableLeft )
			      etdLeft.appendChild( etableLeft )
				  stdRight.appendChild( stableRight )
				  etdRight.appendChild( etableRight )
				  str.appendChild( stdLeft )
				  etr.appendChild( etdLeft )
				  str.appendChild( stdRight )
				  etr.appendChild( etdRight )
				  stbody.appendChild( str )
				  etbody.appendChild( etr )
			      }
			      
			     showTable.appendChild( stbody ) 
			     editTable.appendChild( etbody )
			     ${remoteFunction(action:'getAutoCompleteDate', params:'\'autoCompParams=\' + autoComp ', onComplete:'updateAutoComplete(e)')} 
			  $("#createDialog").dialog("close")
			  if(action == 'edit'){
			      $("#editDialog").dialog('option', 'width', 600)
			      $("#editDialog").dialog('option', 'position', ['center','top']);
			      $("#editDialog").dialog("open")
			      $("#showDialog").dialog("close")
		      } else if(action == 'show'){
		          $("#showDialog").dialog('option', 'width', 600)
			      $("#showDialog").dialog('option', 'position', ['center','top']);
			      $("#showDialog").dialog("open")
			      $("#editDialog").dialog("close")
		      }
		
		    }
	    	
	    	function createDialog(){

		      $("#createDialog").dialog('option', 'width', 600)
		      $("#createDialog").dialog('option', 'position', ['center','top']);
		      $("#createDialog").dialog("open")
		      $("#editDialog").dialog("close")
		      $("#showDialog").dialog("close")
		      $('#createCommentDialog').dialog('close');
		      $('#commentsListDialog').dialog('close');
		      $('#editCommentDialog').dialog('close');
		      $('#showCommentDialog').dialog('close');
		      
		    }
		    
		    function editAssetDialog() {

		      $("#showDialog").dialog("close")
		      $("#editDialog").dialog('option', 'width', 600)
		      $("#editDialog").dialog('option', 'position', ['center','top']);
		      $("#editDialog").dialog("open")
		
		    }
		    
		    function callUpdateDialog( e ) {
		    
		    	var assetEntityAttributes = eval('(' + e.responseText + ')');
				var assetId = document.editForm.id.value
		    	var assetEntityParams = new Array()
		    	if (assetEntityAttributes) {
		    		var length = assetEntityAttributes.length
				      	for (var i=0; i < length; i ++) {
				      		var attributeCode = assetEntityAttributes[i].attributeCode
				      		var attributeValue = document.getElementById('edit'+attributeCode+'Id').value
				      		if(assetEntityAttributes[i].frontendInput == 'select'){
					      		assetEntityParams.push(attributeCode+':'+attributeValue)
				      		} else {
				      			assetEntityParams.push(attributeCode+':'+attributeValue)
				      		}
				      	}
		    	}
		    ${remoteFunction(action:'updateAssetEntity', params:'\'assetEntityParams=\' + assetEntityParams +\'&id=\'+assetId', onComplete:'showEditAsset(e)')}
		    }
		    
		    function showEditAsset(e) {
		      var assetEntityAttributes = eval('(' + e.responseText + ')')
			  if (assetEntityAttributes != "") {
			  		var trObj = document.getElementById("assetRow_"+assetEntityAttributes[0].id)
			  		trObj.style.background = '#65a342'
		    		var length = assetEntityAttributes.length
				      	for (var i=0; i < length; i ++) {
				      		var attribute = assetEntityAttributes[i]
				      		var tdId = document.getElementById(attribute.attributeCode+'_'+attribute.id)
				      		if(tdId != null ){
				      				tdId.innerHTML = attribute.value
				      		}
				      	}
				  $("#editDialog").dialog("close")
				} else {
					alert("Asset Entity is not updated")
				}
      		}
		    
		    
      		function validateAssetEntity() {
      			var attributeSet = document.getElementById("attributeSetId").value;
      			if(attributeSet){
      				var assetName = document.createForm.assetName.value;
	      			if( assetName == null || assetName == "" ){
	      				alert(" Please Enter Asset Name. ");
	      				return false;
	      			} else {
	      				return true;
	      			}
      			} else {
      				alert(" Please select Attribute Set. ");
	      			return false;
      			}
      		}
      		
      		// function to generate createForm
      		
      		function generateCreateForm( e ){
						
      			var assetEntityAttributes = eval('(' + e.responseText + ')');
      			var createTable = document.getElementById("createTable");
      			var tb = document.getElementById('createFormTbodyId')
      			var autoComp = new Array()
			    if(tb != null){
			      createTable.removeChild(tb)
			    }
      			// create tbody for CreateTable
      			var tbody = document.createElement('tbody');
				tbody.id = "createFormTbodyId"
				// Rebuild the select
			      if (assetEntityAttributes) {
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
					      var inputFieldLeft = getInputType(attributeLeft); 
					      inputFieldLeft.id = attributeLeft.attributeCode+'Id';
					      inputTdLeft.appendChild( inputFieldLeft )
					      labelTdLeft.style.backgroundColor = '#f3f4f6 '
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
					      inputTdRight.appendChild( inputFieldRight )
					      labelTdRight.style.backgroundColor = '#f3f4f6 '
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
				      tbody.appendChild( tr )
			      }
			      createTable.appendChild( tbody )
			      ${remoteFunction(action:'getAutoCompleteDate', params:'\'autoCompParams=\' + autoComp ', onComplete:'createAutoComplete(e)')}
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
      		function updateAutoComplete(e){
      			var data = eval('(' + e.responseText + ')');
      			if (data) {
				      var length = data.length
				      for (var i=0; i < length; i ++ ) {
					      var attribData = data[i]
					      var code = "edit"+attribData.attributeCode+"Id"
					      var codeValue = attribData.value;
				  			$("#"+code).autocomplete(codeValue);
					  }
				}
				      			
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
      			
      			var inputField
      			if(type == 'select'){
					inputField = document.createElement('select');
					inputField.name = name ;
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
      			 	inputField = document.createElement('input');
					inputField.type = type;
					inputField.name = name;
				}
				
				return inputField; 
      		}
		    
		    // function to list the comments list
      		function listCommentsDialog(e) {
      			$("#editCommentDialog").dialog("close")
      			$("#showCommentDialog").dialog("close")
				$("#createCommentDialog").dialog("close")
				$("#createDialog").dialog("close")
		      	$("#editDialog").dialog("close")
		      	$("#showDialog").dialog("close")
      			var assetComments = eval('(' + e.responseText + ')');
      			var listTable = document.getElementById("listCommentsTable");
	      		var tbody = document.getElementById('listCommentsTbodyId')
      			if (assetComments) {
      				if(tbody != null){				
				   		listTable.removeChild(tbody)
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
					      commentTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );}})}
					      // = 'comment_'+commentObj.commentInstance.id
					      var typeTd = document.createElement('td');
					      typeTd.id = 'type_'+commentObj.commentInstance.id
					      typeTd.name = commentObj.commentInstance.id
					      
					      typeTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );}})}
					      var verifyTd = document.createElement('td');
						  verifyTd.id = 'verify_'+commentObj.commentInstance.id
						  verifyTd.name = commentObj.commentInstance.id
					      verifyTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );}})}					      
					      var image = document.createElement('img');
					      image.src = "../images/skin/database_edit.png"
					      image.border = 0
					      var link = document.createElement('a');
					      link.href = '#'
					      link.id = 'link_'+commentObj.commentInstance.id
					      link.name = commentObj.commentInstance.id
					      link.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );}})} //;return false
					      var commentText = document.createTextNode(truncate(commentObj.commentInstance.comment));
					      var typeText = document.createTextNode(commentObj.commentInstance.commentType);
					      var verifyText = document.createElement('input')
					      verifyText.id = 'verifyText_'+commentObj.commentInstance.id
					      verifyText.type = 'checkbox'
					      verifyText.disabled = 'disabled'
					      if(commentObj.commentInstance.mustVerify != 0){
					      	verifyText.checked = true
					      }
					      //createTextNode(commentObj.commentInstance.mustVerify);
					      link.appendChild( image )
						  editTd.appendChild( link  )					      
					      commentTd.appendChild( commentText )
					      typeTd.appendChild( typeText )
					      verifyTd.appendChild( verifyText )
					      tr.appendChild( editTd )
					      tr.appendChild( commentTd )
					      tr.appendChild( typeTd )
					      tr.appendChild( verifyTd )
					      listTbody.appendChild( tr )
				      	}
				      listTable.appendChild( listTbody )
      			}
      			
      			$("#commentsListDialog").dialog('option', 'width', 600)	      	
      			$("#commentsListDialog").dialog('option', 'position', ['center','top']);
		      	$("#commentsListDialog").dialog("open")
      		}
      		function showAssetCommentDialog( e , action){
      			$("#createCommentDialog").dialog("close")
      		var assetComments = eval('(' + e.responseText + ')');
      			if (assetComments) {
      				 document.getElementById("commentId").value = assetComments.id
			      	 document.getElementById("commentTdId").innerHTML = assetComments.comment
			      	 document.getElementById("commentTypeTdId").innerHTML = assetComments.commentType
			      	 document.getElementById("mustVerifyEdit").value = assetComments.mustVerify
			      	 if(assetComments.mustVerify != 0){
			      	 document.getElementById("mustVerifyShowId").checked = true
			      	 document.editCommentForm.mustVerify.checked = true
			      	 } else {
			      	 document.getElementById("mustVerifyShowId").checked = false
			      	 document.editCommentForm.mustVerify.checked = false
			      	 }
			      	 document.editCommentForm.comment.value = assetComments.comment
			      	 document.editCommentForm.commentType.value = assetComments.commentType
			      	 document.editCommentForm.mustVerify.value = assetComments.mustVerify
			      	 document.editCommentForm.id.value = assetComments.id
			      	 if(action == 'edit'){
				      	$("#editCommentDialog").dialog('option', 'width', 700)
				      	$("#editCommentDialog").dialog("open")
				      	$("#showCommentDialog").dialog("close")
			      	 } else if(action == 'show'){
			      	 	$("#showCommentDialog").dialog('option', 'width', 700)
			      	 	$("#showCommentDialog").dialog("open")
			      	 	$("#editCommentDialog").dialog("close")
			      	 }
			      	 
      			}
      		}
      		
      		function addCommentsToList( e ){
      			var status = document.getElementById('statusId').value
	      		$("#editCommentDialog").dialog("close")
			    var assetComments = eval('(' + e.responseText + ')');
		      	var tbody = document.getElementById('listCommentsTbodyId')
				if (assetComments != "") {
					if(status != 'new'){
						$("#createCommentDialog").dialog("close")
				      	//generate dynamic rows	
				      	  var tr = document.createElement('tr');
				      	  tr.style.background = '#65a342'
				      	  tr.id = "commentTr_"+assetComments.id
					      tr.setAttribute('onmouseover','this.style.backgroundColor="white";');
					      var editTd = document.createElement('td');
						  var commentTd = document.createElement('td');
						  commentTd.id = 'comment_'+assetComments.id
						  commentTd.name = assetComments.id
						  commentTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );}})}
						  var typeTd = document.createElement('td');
						  typeTd.id = 'type_'+assetComments.id
						  typeTd.name = assetComments.id
						  typeTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );}})}
						  var verifyTd = document.createElement('td');
						  verifyTd.id = 'verify_'+assetComments.id
						  verifyTd.name = assetComments.id
						  verifyTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );}})}
						  var image = document.createElement('img');
					      image.src = "../images/skin/database_edit.png"
					      image.border = 0
						  var link = document.createElement('a');
						  link.href = '#'
						  link.id = 'link_'+assetComments.id
						  link.onclick = function(){new Ajax.Request('showComment?id='+assetComments.id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );}})} //;return false
					      var commentText = document.createTextNode(truncate(assetComments.comment));
					      var typeText = document.createTextNode(assetComments.commentType);
					      var verifyText = document.createElement('input')
					      verifyText.id = 'verifyText_'+assetComments.id
					      verifyText.type = 'checkbox'
					      verifyText.disabled = 'disabled'
					      if(assetComments.mustVerify != 0){
					      	verifyText.checked = true
					      }
					      //createTextNode(assetComments.mustVerify);
					      link.appendChild( image )
						  editTd.appendChild( link  )					      
					      commentTd.appendChild( commentText )
					      typeTd.appendChild( typeText )
					      verifyTd.appendChild( verifyText )
					      tr.appendChild( editTd )
					      tr.appendChild( commentTd )
					      tr.appendChild( typeTd )
					      tr.appendChild( verifyTd )
					      tbody.appendChild( tr )
				    } else {
						window.location.reload()
					}
	      		} else {
	      				alert("Comment not created")
	      		}
      		}
      		
      		// update comments 
      		function updateCommentsOnList( e ){
      		var assetComments = eval('(' + e.responseText + ')');
      			if (assetComments) {
      				$("#editCommentDialog").dialog("close")
			      	//generate dynamic rows	
			      	  var tr = document.getElementById('commentTr_'+assetComments.id);
			      	  tr.style.background = '#65a342'
			      	  if(assetComments.mustVerify != 0){
				      document.getElementById('verifyText_'+assetComments.id).checked = true ;
				      } else {
				      document.getElementById('verifyText_'+assetComments.id).checked = false ;
				      }
				      document.getElementById('type_'+assetComments.id).innerHTML = assetComments.commentType;
				      document.getElementById('comment_'+assetComments.id).innerHTML = truncate(assetComments.comment);
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
      		
	    </script>

</head>
<body>

<div class="body">
<h1>Asset Entity List</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="list">
<table id="assetEntityTable">
	<thead>
		<tr>

			<th>Actions</th>			

			<g:sortableColumn property="application" title="Application" />
			
			<g:sortableColumn property="assetName" title="Asset Name" />
			
			<g:sortableColumn property="model" title="Model" />

			<g:sortableColumn property="sourceLocation" title="Source Location" />

			<g:sortableColumn property="sourceRack" title="Source Rack/Cab" />

			<g:sortableColumn property="targetLocation"	title="Target Location" />
			
			<g:sortableColumn property="targetRack"	title="Target Rack/Cab" />
			

			<g:sortableColumn property="assetType" title="Asset Type" />

			<g:sortableColumn property="assetTag" title="Asset Tag" />

			<g:sortableColumn property="serialNumber" title="Serial #" />


		</tr>
	</thead>
	<tbody>
		<g:each in="${assetEntityInstanceList}" status="i"
			var="assetEntityInstance">
			<tr id="assetRow_${assetEntityInstance.id}" 
				onmouseover="style.backgroundColor='#87CEEE';"
				onmouseout="style.backgroundColor='white';" >

				<td><g:remoteLink controller="assetEntity" action="editShow" id="${assetEntityInstance.id}" before="document.showForm.id.value = ${assetEntityInstance.id};document.editForm.id.value = ${assetEntityInstance.id};" onComplete="showAssetDialog( e , 'edit');">
					<img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px">
				</g:remoteLink>
				<g:if test="${AssetComment.findByAssetEntity(assetEntityInstance)}">
				<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="document.getElementById('createAssetCommentId').value = ${assetEntityInstance.id};" onComplete="listCommentsDialog( e );">
					<img src="${createLinkTo(dir:'images/skin',file:'database_table_bold.png')}" border="0px">
				</g:remoteLink>
				</g:if>
				<g:else>
				<a href="#" onclick="document.getElementById('createAssetCommentId').value = ${assetEntityInstance.id};document.getElementById('statusId').value = 'new';$('#createCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('open');$('#commentsListDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();">
					<img src="${createLinkTo(dir:'images/skin',file:'database_table_light.png')}" border="0px">
				</a>
				</g:else>
				</td>
				
				<td id="application_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}"> ${fieldValue(bean:assetEntityInstance, field:'application')} </td>

				<td id="assetName_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'assetName')}</td>

				<td id="model_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'model')}</td>

				<td id="sourceLocation_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'sourceLocation')}</td>

				<td id="sourceRack_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'sourceRack')}</td>

				<td id="targetLocation_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'targetLocation')}</td>
				
				<td id="targetRack_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'targetRack')}</td>

				<td id="assetType_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'assetType')}</td>

				<td id="assetTag_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'assetTag')}</td>

				<td id="serialNumber_${assetEntityInstance.id}" onclick="${remoteFunction(action:'editShow', params:'\'id=\'+'+assetEntityInstance.id, before:'document.showForm.id.value ='+ assetEntityInstance.id+';document.editForm.id.value = '+ assetEntityInstance.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${fieldValue(bean:assetEntityInstance, field:'serialNumber')}</td>

			</tr>
		</g:each>
	</tbody>
</table>
</div>
<div class="paginateButtons"><g:paginate
	total="${AssetEntity.count()}" /></div>
<div class="buttons"><g:form>
	<span class="button"><input type="button"
		value="New Asset Entity" class="create" onClick="createDialog()" /></span>
</g:form></div>
</div>

<div id="createDialog" title="Create Asset Entity" style="display: none;">
<g:form action="save" method="post" name="createForm" >
	<div class="dialog">
	<table id="createTable">
		<tbody>
			<tr class="prop">
				<td valign="top" class="name" ><label for="attributeSet">Attribute Set:</label><span style="padding-left: 46px;"><g:select optionKey="id" from="${com.tdssrc.eav.EavAttributeSet.list()}" id="attributeSetId" name="attributeSet.id" value="${assetEntityInstance?.attributeSet?.id}" noSelection="['':'select']" 
				 onchange="${remoteFunction(action:'getAttributes', params:'\'attribSet=\' + this.value ', onComplete:'generateCreateForm(e)')}"></g:select></span> </td>

			</tr>
		</tbody>
	</table>
	</div>
	<div class="buttons"><input type="hidden" name="projectId"
		value="${projectId }" /> <span class="button"><input
		class="save" type="submit" value="Create"
		onclick="return validateAssetEntity();" /></span></div>
</g:form></div>
<div id="showDialog" title="Show Asset Entity" style="display: none;">
<g:form action="save" method="post" name="showForm">
	<div class="dialog">
	<table id="showTable">
	</table>
	</div>
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	 <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span></div>
</g:form></div>

<div id="editDialog" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<div class="dialog">
	<table id="editTable">
	</table>
	</div>
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset Entity" onClick="${remoteFunction(action:'getAssetAttributes', params:'\'assetId=\' + document.editForm.id.value ', onComplete:'callUpdateDialog(e)')}" />
	</span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span></div>
</g:form></div>

<div id="commentsListDialog" title="Show Asset Comments" style="display: none;">
<br>
	<div class="list">
		<table id="listCommentsTable">
		<thead>
	        <tr >
	                        
	          <th nowrap>Action</th>
	          
	          <th nowrap>Comment</th>
	                        
	          <th nowrap>Comment Type</th>
	                        
	          <th nowrap>Must Verify</th>                      
	                   	    
	        </tr>
	    </thead>
		<tbody id="listCommentsTbodyId">
		
		</tbody>
		</table>
	</div>
	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		<span class="menuButton"><a class="create" href="#" onclick="document.getElementById('statusId').value = '';$('#createCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();" >New Comment</a></span>
	</div>
</div>
<div id="createCommentDialog" title="Create Asset Comment" style="display: none;">
<input type="hidden" name="assetEntity.id" id="createAssetCommentId" value="">
<input type="hidden" name="status" id="statusId" value="">
<g:form action="saveComment" method="post" name="createCommentForm" >
	<div class="dialog">
	<table id="createCommentTable">
		<tbody>
			<tr class="prop">
				<td valign="top" class="name">
                <label for="comment">Comment:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="comment" name="comment" ></textarea>
                </td>
            </tr> 
			<tr class="prop">
            	<td valign="top" class="name">
                <label for="commentType">Comment Type:</label>
                </td>
                <td valign="top" class="value">
                <g:select id="commentType" name="commentType" from="${AssetComment.constraints.commentType.inList}" value="" noSelection="['':'please select']"></g:select>
                </td>
            </tr> 
			<tr class="prop">
            	<td valign="top" class="name">
                <label for="mustVerify">Must Verify:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="mustVerify" name="mustVerify" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
		</tbody>
	</table>
	</div>
	<div class="buttons"><span class="button">
	<input class="save" type="button" value="Create" onclick="${remoteFunction(action:'saveComment', params:'\'assetEntity.id=\' + document.getElementById(\'createAssetCommentId\').value +\'&comment=\'+document.createCommentForm.comment.value +\'&commentType=\'+document.createCommentForm.commentType.value +\'&mustVerify=\'+document.createCommentForm.mustVerify.value', onComplete:'addCommentsToList(e)')}" /></span></div>
</g:form ></div>
<div id="showCommentDialog" title="Show Asset Comment" style="display: none;">
	<div class="dialog">
	<input name="id" value="" id="commentId" type="hidden">
	<table id="showCommentTable">
		<tbody>
			<tr class="prop">
				<td valign="top" class="name">
                <label for="comment">Comment:</label>
                </td>
				<td valign="top" class="value" id="commentTdId"/>
            </tr> 
			<tr class="prop">
            	<td valign="top" class="name">
                <label for="commentType">Comment Type:</label>
                </td>
                <td valign="top" class="value" id="commentTypeTdId"/>
            </tr> 
			<tr class="prop">
            	<td valign="top" class="name">
                <label for="mustVerify">Must Verify:</label>
                </td>
                <td valign="top" class="value" id="verifyTdId"><input type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0" disabled="disabled" /></td>
            </tr>
		</tbody>
	</table>
	</div>
	<div class="buttons">
	<span class="button">
	<input class="edit" type="button" value="Edit" onclick="$('#editCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
	</span>
	<span class="button">
	<input class="delete" type="button" value="Delete" onclick="${remoteFunction(action:'deleteComment', params:'\'id=\' + document.getElementById(\'commentId\').value +\'&assetEntity=\'+document.getElementById(\'createAssetCommentId\').value ', onComplete:'listCommentsDialog(e)')}" />
	</span>
	</div>
</div>
<div id="editCommentDialog" title="Edit Asset Comment" style="display: none;">
<g:form action="updateComment" method="post" name="editCommentForm" >
	<div class="dialog">
	<input type="hidden" name="id" value="">
	<table id="updateCommentTable">
		<tbody>
			<tr class="prop">
				<td valign="top" class="name">
                <label for="comment">Comment:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="comment" name="comment" ></textarea>
                </td>
            </tr> 
			<tr class="prop">
            	<td valign="top" class="name">
                <label for="commentType">Comment Type:</label>
                </td>
                <td valign="top" class="value">
                <g:select id="commentType" name="commentType" from="${AssetComment.constraints.commentType.inList}" value="" noSelection="['':'please select']"></g:select>
                </td>
            </tr> 
			<tr class="prop">
            	<td valign="top" class="name">
                <label for="mustVerify">Must Verify:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="mustVerifyEdit" name="mustVerify" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
                </td>
            </tr>
		</tbody>
	</table>
	</div>
	<div class="buttons"><span class="button">
	<input class="save" type="button" value="Update" onclick="${remoteFunction(action:'updateComment', params:'\'id=\' + document.editCommentForm.id.value +\'&comment=\'+document.editCommentForm.comment.value +\'&commentType=\'+document.editCommentForm.commentType.value +\'&mustVerify=\'+document.editCommentForm.mustVerify.value', onComplete:'updateCommentsOnList(e)')}" />
	</span>
	<span class="button">
	<input class="delete" type="button" value="Delete" onclick="${remoteFunction(action:'deleteComment', params:'\'id=\' + document.editCommentForm.id.value +\'&assetEntity=\'+document.getElementById(\'createAssetCommentId\').value ', onComplete:'listCommentsDialog(e)')}" />
	</span>
	</div>
</g:form >
</div>
</body>
</html>
