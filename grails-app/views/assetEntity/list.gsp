

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>Asset Entity List</title>

<g:javascript library="prototype" />
<g:javascript library="jquery" />

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
	
	      })
</script>
<script type="text/javascript">	
	    	var rowId
		    function showAssetDialog( e ) {
      			var assetEntityAttributes = eval('(' + e.responseText + ')');
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
				      	for (var i=0; i < length; i += 2) {
					      var attribute1 = assetEntityAttributes[i]
					      var attribute2 = assetEntityAttributes[i + 1]
					      var str = document.createElement('tr');
					      var etr = document.createElement('tr');
					      // td for Show page
					      var inputTd1 = document.createElement('td');
					      var labelTd1 = document.createElement('td');
					      var label1 = document.createTextNode(attribute1.label);
					      labelTd1.appendChild( label1 )
					      var inputField1
					      if(attribute1.attributeCode != "assetType"){
					      	inputField1 = document.createTextNode(attribute1.value);
					      }else{
					      	inputField1 = document.createTextNode("");
					      }
					      inputTd1.appendChild( inputField1 )
					      labelTd1.style.backgroundColor = '#f3f4f6 '
					      labelTd1.style.width = '25%'
					      inputTd1.style.width = '25%'
					      str.appendChild( labelTd1 )
					      str.appendChild( inputTd1 )
					      
					      // td for Edit page
					      var inputTdE1 = document.createElement('td');
					      var labelTdE1 = document.createElement('td');
					      var labelE1 = document.createTextNode(attribute1.label);
					      labelTdE1.appendChild( labelE1 )
					      var inputFieldE1 = document.createElement('input');
						      inputFieldE1.type = 'text';
							  inputFieldE1.name = attribute1.attributeCode;
							  if(attribute1.attributeCode != "assetType"){
							  inputFieldE1.value = attribute1.value;
							  } else {
							  	inputFieldE1.value = "";
							  }
							  inputFieldE1.id = 'edit'+attribute1.attributeCode+'Id';
					      inputTdE1.appendChild( inputFieldE1 )
					      labelTdE1.style.backgroundColor = '#f3f4f6 '
					      labelTdE1.style.width = '25%'
					      inputTdE1.style.width = '25%'
					      etr.appendChild( labelTdE1 )
					      etr.appendChild( inputTdE1 )
					      if(attribute2){
					      // TD for Show page
					      var inputTd2 = document.createElement('td');
					      var labelTd2 = document.createElement('td');
					      var label2 = document.createTextNode(attribute2.label);
					      labelTd2.appendChild( label2 )
					      var inputField2
					      if(attribute2.attributeCode != "assetType"){
					      	inputField2 = document.createTextNode(attribute2.value);
					      } else {
					      	inputField2 = document.createTextNode(attribute2.value);
					      }
					      inputTd2.appendChild( inputField2 )
					      labelTd2.style.backgroundColor = '#f3f4f6'
					      inputTd2.style.width = '25%'
					      labelTd2.style.width = '25%'
					      str.appendChild( labelTd2 )
					      str.appendChild( inputTd2 )
					      // TD for Edit page
					      var inputTdE2 = document.createElement('td');
					      var labelTdE2 = document.createElement('td');
					      var labelE2 = document.createTextNode(attribute2.label);
					      labelTdE2.appendChild( labelE2 )
					      var inputFieldE2 = document.createElement('input');
						      inputFieldE2.type = 'text';
							  inputFieldE2.name = attribute2.attributeCode;
							  if(attribute2.attributeCode != "assetType"){
							  	inputFieldE2.value = attribute2.value;
							  } else {
							  	inputFieldE2.value = "";
							  }
							  inputFieldE2.id = 'edit'+attribute2.attributeCode+'Id';
					      inputTdE2.appendChild( inputFieldE2 )
					      labelTdE2.style.backgroundColor = '#f3f4f6'
					      inputTdE2.style.width = '25%'
					      labelTdE2.style.width = '25%'
					      etr.appendChild( labelTdE2 )
					      etr.appendChild( inputTdE2 )
					     }
					      stbody.appendChild( str )
					     etbody.appendChild( etr )
				      	
				      	}
			      }
			      showTable.appendChild( stbody ) 
			     editTable.appendChild( etbody ) 

		      $("#showDialog").dialog('option', 'width', 700)
		      $("#showDialog").dialog('option', 'position', ['right','top']);
		      $("#showDialog").dialog("open")
		
		    }
	    	
	    	function createDialog(){

		      $("#createDialog").dialog('option', 'width', 700)
		      $("#createDialog").dialog('option', 'position', ['right','top']);
		      $("#createDialog").dialog("open")
		
		    }
		    
		    function editAssetDialog() {

		      $("#showDialog").dialog("close")
		      $("#editDialog").dialog('option', 'width', 700)
		      $("#editDialog").dialog('option', 'position', ['right','top']);
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
				      		if(attributeCode != 'moveBundle'){
				      			var attributeValue = document.getElementById('edit'+attributeCode+'Id').value
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
				      		if(tdId != null && attribute.attributeCode != 'assetType'){
				      			tdId.innerHTML = attribute.value 
				      		}
				      	}
				  $("#editDialog").dialog("close")
				} else {
					alert("Asset Entity is not updated")
				}
				      	
      		}
		    
		    
		    function setRowId(val){

      			rowId = val.id

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
			    if(tb != null){
			      createTable.removeChild(tb)
			    }
      			// create tbody for CreateTable
      			var tbody = document.createElement('tbody');
				tbody.id = "createFormTbodyId"
				// Rebuild the select
			      if (assetEntityAttributes) {
				      var length = assetEntityAttributes.length
				      	for (var i=0; i < length; i += 2) {
					      var attribute1 = assetEntityAttributes[i]
					      var attribute2 = assetEntityAttributes[i + 1]
					      var tr = document.createElement('tr');
					      var inputTd1 = document.createElement('td');
					      var labelTd1 = document.createElement('td');
					      var label1 = document.createTextNode(attribute1.label);
					      labelTd1.appendChild( label1 )
					      var inputField1 = document.createElement('input');
							inputField1.type = 'text';
							inputField1.name = attribute1.attributeCode;
							inputField1.id = attribute1.attributeCode+'Id';
					      inputTd1.appendChild( inputField1 )
					      labelTd1.style.backgroundColor = '#f3f4f6 '
					      labelTd1.style.width = '25%'
					      labelTd1.noWrap = 'nowrap'
					      tr.appendChild( labelTd1 )
					      tr.appendChild( inputTd1 )
					      if(attribute2){
					      var inputTd2 = document.createElement('td');
					      var labelTd2 = document.createElement('td');
					      var label2 = document.createTextNode(attribute2.label);
					      labelTd2.appendChild( label2 )
					      var inputField2 = document.createElement('input');
							inputField2.type = 'text';
							inputField2.name = attribute2.attributeCode;
							inputField2.id = attribute2.attributeCode+'Id';
					      inputTd2.appendChild( inputField2 )
					      labelTd2.style.backgroundColor = '#f3f4f6 '
					      labelTd2.style.width = '25%'
					      labelTd2.noWrap = 'nowrap'
					      tr.appendChild( labelTd2 )
					      tr.appendChild( inputTd2 )
					     }
					      tbody.appendChild( tr )
				      	
				      	}
			      }
			      createTable.appendChild( tbody )
      		}
		      
	    </script>

</head>
<body>
<div class="menu2">
<ul>
	<li><g:link class="home" controller="projectUtil">Project </g:link>
	</li>
	<li><g:link class="home" controller="person" action="projectStaff"
		params="[projectId:projectId]">Staff</g:link></li>
	<li><g:link class="home" controller="assetEntity"
		params="[projectId:projectId]">Assets</g:link></li>
	<li><g:link class="home" controller="assetEntity"
		action="assetImport" params="[projectId:projectId]">Import/Export</g:link>
	</li>
	<li><a href="#">Contacts </a></li>
	<li><a href="#">Applications </a></li>
	<li><g:link class="home" controller="moveBundle"
		params="[projectId:projectId]">Move Bundles</g:link></li>
</ul>
</div>
<div class="body">
<h1>Asset Entity List</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div class="list">
<table id="assetEntityTable">
	<thead>
		<tr>

			<th>Show</th>			

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
			<tr id="assetRow_${assetEntityInstance.id}" onClick="setRowId(this)"
				onmouseover="style.backgroundColor='#87CEEE';"
				onmouseout="style.backgroundColor='white';">

				<td><g:remoteLink controller="assetEntity" action="editShow" id="${assetEntityInstance.id}" before="document.showForm.id.value = ${assetEntityInstance.id};document.editForm.id.value = ${assetEntityInstance.id};" onComplete="showAssetDialog( e );">
					<img src="${createLinkTo(dir:'images',file:'asset_view.png')}" border="0px">
				</g:remoteLink></td>				
				
				<td id="application_${assetEntityInstance.id}" >${fieldValue(bean:assetEntityInstance, field:'application')}</td>

				<td id="assetName_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'assetName')}</td>

				<td id="model_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'model')}</td>

				<td id="sourceLocation_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'sourceLocation')}</td>

				<td id="sourceRack_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'sourceRack')}</td>

				<td id="targetLocation_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'targetLocation')}</td>
				
				<td id="targetRack_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'targetRack')}</td>

				<td id="assetType_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'assetType')}</td>

				<td id="assetTag_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'assetTag')}</td>

				<td id="serialNumber_${assetEntityInstance.id}">${fieldValue(bean:assetEntityInstance, field:'serialNumber')}</td>

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
				<td valign="top" class="name" style="width:25%"><label for="attributeSet">Attribute Set:</label></td>
				<td valign="top"><g:select optionKey="id" from="${com.tdssrc.eav.EavAttributeSet.list()}" id="attributeSetId" name="attributeSet.id" value="${assetEntityInstance?.attributeSet?.id}" noSelection="['':'select']" 
				 onchange="${remoteFunction(action:'getAttributes', params:'\'attribSet=\' + this.value ', onComplete:'generateCreateForm(e)')}"></g:select></td>

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
</body>
</html>
