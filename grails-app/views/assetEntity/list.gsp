
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
<g:javascript src="assetcommnet.js" />
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
	        $("#filterPane").draggable()
})
</script>
<script type="text/javascript">	
		    function showAssetDialog( e , action ) {
		    	$('#createCommentDialog').dialog('close');
		    	$('#commentsListDialog').dialog('close');
		    	$('#editCommentDialog').dialog('close');
		    	$('#showCommentDialog').dialog('close');
		    	 var browser=navigator.appName;
      			var assetEntityAttributes = eval('(' + e.responseText + ')');
      			var autoComp = new Array()
      			var showDiv = document.getElementById("showDiv");
      			var editDiv = document.getElementById("editDiv");
      			var stb = document.getElementById('showTbodyId')
			    if(stb != null){
			      showDiv.removeChild(stb)
			    }
      			var etb = document.getElementById('editTbodyId')
			    if(etb != null){
			      editDiv.removeChild(etb)
			    }
      			// create tbody for CreateTable
      			var stbody = document.createElement('table');
				stbody.id = "showTbodyId"
      			var etbody = document.createElement('table');
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
					      labelTdLeft.style.background = '#f3f4f6 '
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
					  
					      labelTdELeft.style.background = '#f3f4f6 '
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
					      labelTdRight.style.background = '#f3f4f6 '
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
					      labelTdERight.style.background = '#f3f4f6 '
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
			      
			     showDiv.appendChild( stbody )
			      showDiv.innerHTML += "";
			     editDiv.appendChild( etbody )
			      if(browser == 'Microsoft Internet Explorer') {
			editDiv.innerHTML += "";
		} 
			      
			      
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
		      if(document.getElementById('createFormTbodyId')){
		      document.getElementById('createFormTbodyId').style.display = 'none';
		      document.getElementById('attributeSetId').value = '';
		      }
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
      				var assetName = document.createForm.assetNameId.value;
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
				      tbody.appendChild( tr )
			      }
			      createDiv.appendChild( tbody )			     
			      if(browser == 'Microsoft Internet Explorer') {
			      createDiv.innerHTML += "";
			      }
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
      			var browser=navigator.appName;
      			var inputField
      			if(type == 'select'){
      			if(browser == 'Microsoft Internet Explorer') {
					inputField = document.createElement('<select name='+name +' />');
					}else{
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
      			 	}else{
      			 	inputField = document.createElement('input');
					inputField.type = "text";
					inputField.name = name;
					}
				}
				
				return inputField; 
      		}
	function resolveValidate(formName,idVal){
	
	var type = 	document.forms[formName].commentType.value;
	if(type != "issue"){
		document.forms[formName].isResolved.value = 0;
	}
	var resolveBoo = document.forms[formName].isResolved.checked;
	var resolveVal = document.forms[formName].resolution.value;
	if(type == ""){
		alert('Please select comment type');
		return false;
	}else if(resolveBoo){
		if(resolveVal != ""){
		if(formName == "createCommentForm"){
			${remoteFunction(action:'saveComment', params:'\'assetEntity.id=\' + document.getElementById(idVal).value +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'addCommentsToList(e)')}
		}else{
			${remoteFunction(action:'updateComment', params:'\'id=\' + document.getElementById(idVal).value +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'updateCommentsOnList(e)')}
		}
		}else{
			alert('Please enter resolution');
			return false;
		}
	}else{
		if(formName == "createCommentForm"){
			${remoteFunction(action:'saveComment', params:'\'assetEntity.id=\' + document.getElementById(idVal).value +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'addCommentsToList(e)')}
		}else{
			${remoteFunction(action:'updateComment', params:'\'id=\' + document.getElementById(idVal).value +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'updateCommentsOnList(e)')}
		}
	}
}
	    
      		
	    </script>
<filterpane:includes />
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

			<g:sortableColumn property="application" title="Application" params="${filterParams}"/>
			
			<g:sortableColumn property="assetName" title="Asset Name" params="${filterParams}"/>
			
			<g:sortableColumn property="model" title="Model" params="${filterParams}"/>

			<g:sortableColumn property="sourceLocation" title="Source Location" params="${filterParams}"/>

			<g:sortableColumn property="sourceRack" title="Source Rack/Cab" params="${filterParams}"/>

			<g:sortableColumn property="targetLocation"	title="Target Location" params="${filterParams}"/>
			
			<g:sortableColumn property="targetRack"	title="Target Rack/Cab" params="${filterParams}"/>
			

			<g:sortableColumn property="assetType" title="Asset Type" params="${filterParams}"/>

			<g:sortableColumn property="assetTag" title="Asset Tag" params="${filterParams}"/>

			<g:sortableColumn property="serialNumber" title="Serial #" params="${filterParams}"/>


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

<div class="paginateButtons">
<g:form name="paginateRows" action="list">
<table>
<tr>
<td style="width: 770px;">
<g:paginate total="${assetEntityCount == null ? AssetEntity.findAll('from AssetEntity where project = '+projectId).size() :  assetEntityCount }" params="${filterParams}"/>
<filterpane:filterButton textKey="fp.tag.filterButton.text" appliedTextKey="fp.tag.filterButton.appliedText" text="Filter Me" appliedText="Change Filter" />
<filterpane:isNotFiltered>Pure and Unfiltered!</filterpane:isNotFiltered>
<filterpane:isFiltered>Filter Applied!</filterpane:isFiltered>
</td>
<td >
Rows per Page:&nbsp;<g:select  from="[25,50,100,200]" id="rowVal" name="rowVal" value="${maxVal}" onchange="document.paginateRows.submit();"></g:select></td>
</table>
</g:form>
</div>
<filterpane:filterPane domainBean="AssetEntity"  excludeProperties="sourceRackPosition,targetRackPosition,usize,railType,fiberCabinet,hbaPort,ipAddress,hinfo,kvmDevice,kvmPort,newOrOld,nicPort,powerPort,remoteMgmtPort,truck,priority,cart,shelf,dateCreated,project.name" />
<div class="buttons"><g:form>
	<span class="button"><input type="button"
		value="New Asset Entity" class="create" onClick="createDialog()" /></span>
</g:form></div>
</div>

<div id="createDialog" title="Create Asset Entity" style="display: none;">
<g:form action="save" method="post" name="createForm" >

	<div class="dialog" id="createDiv" >

		<table >
			<tr class="prop">
				<td valign="top" class="name" ><label for="attributeSet">Attribute Set:</label><span style="padding-left: 46px;"><g:select optionKey="id" from="${com.tdssrc.eav.EavAttributeSet.list()}" id="attributeSetId" name="attributeSet.id" value="${assetEntityInstance?.attributeSet?.id}" noSelection="['':'select']" 
				 onchange="${remoteFunction(action:'getAttributes', params:'\'attribSet=\' + this.value ', onComplete:'generateCreateForm(e)')}"></g:select></span> </td>

			</tr>
			</table>
		

	</div>
	
	<div class="buttons"><input type="hidden" name="projectId"
		value="${projectId }" /> <span class="button"><input
		class="save" type="submit" value="Create"
		onclick="return validateAssetEntity();" /></span></div>
</g:form></div>
<div id="showDialog" title="Show Asset Entity" style="display: none;">
<g:form action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	
	</div>
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	 <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Are you sure?');"
		value="Remove From Project" /></span>
		</div>
</g:form></div>

<div id="editDialog" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<div class="dialog" id="editDiv">
	
	</div>
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset Entity" onClick="${remoteFunction(action:'getAssetAttributes', params:'\'assetId=\' + document.editForm.id.value ', onComplete:'callUpdateDialog(e)')}" />
	</span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Are you sure?');"
		value="Remove From Project" /></span>
		</div>
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
	          
	          <th nowrap>Resolved</th>
	                        
	          <th nowrap>Must Verify</th>                      
	                   	    
	        </tr>
	    </thead>
		<tbody id="listCommentsTbodyId">
		
		</tbody>
		</table>
	</div>
	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		<span class="menuButton"><a class="create" href="#" onclick="document.getElementById('statusId').value = '';document.getElementById('createResolveDiv').style.display = 'none' ;$('#createCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();" >New Comment</a></span>
	</div>
</div>
<div id="createCommentDialog" title="Create Asset Comment"
	style="display: none;"><input type="hidden" name="assetEntity.id"
	id="createAssetCommentId" value=""> <input type="hidden"
	name="status" id="statusId" value=""> <g:form
	action="saveComment" method="post" name="createCommentForm">
	
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
	<table id="createCommentTable" style="border: 0px">
		
			<tr class="prop" >
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" style="width: 20%;" ><g:select id="commentType"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					noSelection="['':'please select']" onChange="commentChange('createResolveDiv','createCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				
				<input type="checkbox"
					id="mustVerifyEdit" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must
				Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="comment" name="comment"></textarea></td>
			</tr>
		
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px" >
            <tr class="prop">
            	<td valign="top" class="name">
                <label for="isResolved">Resolved:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
          
            <tr class="prop">
				<td valign="top" class="name">
                <label for="resolution">Resolution:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="resolution" name="resolution" ></textarea>
                </td>
            </tr> 
                
            </table>
            </div>
		
	</div>
	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Create"
		onclick="resolveValidate('createCommentForm','createAssetCommentId');" /></span></div>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment"
	style="display: none;">
<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId"
	type="hidden">
	<div>
<table id="showCommentTable" style="border: 0px">
	
	<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdById" />
	</tr>
		
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Comment
			Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" />
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="mustVerify">Must
			Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId"><input
				type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0"
				disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label>
			</td>
			<td valign="top" class="value" ><textarea cols="80" rows="5"
					id="commentTdId" readonly="readonly"></textarea> </td>
		</tr>
		</table>
		</div>
		<div id="showResolveDiv" style="display: none;">
		<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="isResolved">Is
			Resolved:</label></td>
			<td valign="top" class="value" id="resolveTdId"><input
				type="checkbox" id="isResolvedId" name="isResolved" value="0"
				disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label>
			</td>
			<td valign="top" class="value" ><textarea cols="80" rows="5"
					id="resolutionId" readonly="readonly"></textarea> </td>
		</tr>
			<tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedById" />
	</tr>
		
	
</table>
</div>
<div class="buttons"><span class="button"> <input
	class="edit" type="button" value="Edit"
	onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 700);$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
</span> <span class="button"> <input class="delete" type="button"
	value="Delete"
	onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment', params:'\'id=\' + document.getElementById(\'commentId\').value +\'&assetEntity=\'+document.getElementById(\'createAssetCommentId\').value ', onComplete:'listCommentsDialog(e)')}" />
</span></div>
</div>
<div id="editCommentDialog" title="Edit Asset Comment"
	style="display: none;"><g:form action="updateComment"
	method="post" name="editCommentForm">
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value="">
	<div>
	<table id="updateCommentTable" style="border: 0px">
		
		
			<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedEditId"  />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdByEditId" />
	</tr>
			<tr class="prop" >
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" style="width: 20%;" >
				<input type="text" id="commentType" name="commentType" readonly="readonly">&nbsp;&nbsp;&nbsp;&nbsp;			
				
				<input type="checkbox"
					id="mustVerifyEdit" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must
				Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="comment" name="comment"></textarea></td>
			</tr>
			</table>
			
			</div>
			<div id="editResolveDiv" style="display: none;">
		<table id="updateResolveTable" style="border: 0px">
            <tr class="prop">
            	<td valign="top" class="name">
                <label for="isResolved">Resolved:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
          
            <tr class="prop">
				<td valign="top" class="name">
                <label for="resolution">Resolution:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="resolution" name="resolution" ></textarea>
                </td>
            </tr> 
               <tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedEditId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedByEditId"  />
	</tr>
            </table>
            </div>
		
		

	</div>

	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Update"
		onclick="resolveValidate('editCommentForm','updateCommentId');" />
	</span> <span class="button"> <input class="delete" type="button"
		value="Delete"
		onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment', params:'\'id=\' + document.editCommentForm.id.value +\'&assetEntity=\'+document.getElementById(\'createAssetCommentId\').value ', onComplete:'listCommentsDialog(e)')}" />
	</span></div>
</g:form></div>
</body>
</html>
