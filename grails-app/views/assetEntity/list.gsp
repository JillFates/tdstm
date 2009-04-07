

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
      			var tb = document.getElementById('showTbodyId')
			    if(tb != null){
			      showTable.removeChild(tb)
			    }
      			// create tbody for CreateTable
      			var tbody = document.createElement('tbody');
				tbody.id = "showTbodyId"
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
					      var inputField1 = document.createTextNode(attribute1.value);
					      inputTd1.appendChild( inputField1 )
					      labelTd1.style.backgroundColor = '#f3f4f6 '
					      labelTd1.style.width = '25%'
					      inputTd1.style.width = '25%'
					      tr.appendChild( labelTd1 )
					      tr.appendChild( inputTd1 )
					      if(attribute2){
					      var inputTd2 = document.createElement('td');
					      var labelTd2 = document.createElement('td');
					      var label2 = document.createTextNode(attribute2.label);
					      labelTd2.appendChild( label2 )
					      var inputField2 = document.createTextNode(attribute2.value);
					      inputTd2.appendChild( inputField2 )
					      labelTd2.style.backgroundColor = '#f3f4f6'
					      inputTd2.style.width = '25%'
					      labelTd2.style.width = '25%'
					      tr.appendChild( labelTd2 )
					      tr.appendChild( inputTd2 )
					     }
					      tbody.appendChild( tr )
				      	
				      	}
			      }
			      showTable.appendChild( tbody ) 

		      $("#showDialog").dialog('option', 'width', 700)
		      $("#showDialog").dialog("open")
		
		    }
	    	
	    	function createDialog(){

		      $("#createDialog").dialog('option', 'width', 700)
		      $("#createDialog").dialog("open")
		
		    }
		    
		    function editAssetDialog() {

		      
		      $("#editDialog").dialog('option', 'width', 800)
		      $("#editDialog").dialog("open")
		
		    }
		    
		    function callUpdateDialog() {
			  if( document.editForm.assetName.value == null || document.editForm.assetName.value == "" ) {
      				alert(" Please Enter Asset Name. ")
      				return false
			  } else {
			      var assetEntityId = document.editForm.id.value 			      
			      var model = document.editForm.model.value
			      var sourceLocation = document.editForm.sourceLocation.value
			      var targetLocation = document.editForm.targetLocation.value
			      var sourceRack = document.editForm.sourceRack.value
			      var targetRack = document.editForm.targetRack.value
			      var sourceRackPosition = document.editForm.sourceRackPosition.value
			      var targetRackPosition = document.editForm.targetRackPosition.value
			      var usize = document.editForm.usize.value
			      var manufacturer = document.editForm.manufacturer.value
			      var fiberCabinet = document.editForm.fiberCabinet.value
			      var hbaPort = document.editForm.hbaPort.value
			      var hinfo = document.editForm.hinfo.value
			      var ipAddress = document.editForm.ipAddress.value
			      var kvmDevice = document.editForm.kvmDevice.value
			      var kvmPort = document.editForm.kvmPort.value
			      var newOrOld = document.editForm.newOrOld.value
			      var nicPort = document.editForm.nicPort.value
			      var powerPort = document.editForm.powerPort.value
			      var remoteMgmtPort = document.editForm.remoteMgmtPort.value
			      var truck = document.editForm.truck.value
			      var assetType = document.editForm.assetType.value		      
			      var assetName = document.editForm.assetName.value
			      var assetTag = document.editForm.assetTag.value
			      var serialNumber = document.editForm.serialNumber.value
			      var application = document.editForm.application.value
			      	      
			      
			      var assetNameDialog = new Array()
			      assetNameDialog[0] = assetEntityId			      
			      assetNameDialog[1] = model
			      assetNameDialog[2] = sourceLocation
			      assetNameDialog[3] = targetLocation
			      assetNameDialog[4] = sourceRack
			      assetNameDialog[5] = targetRack
			      assetNameDialog[6] = sourceRackPosition
			      assetNameDialog[7] = targetRackPosition
			      assetNameDialog[8] = usize
			      assetNameDialog[9] = manufacturer
			      assetNameDialog[10] = fiberCabinet
			      assetNameDialog[11] = hbaPort
			      assetNameDialog[12] = hinfo
			      assetNameDialog[13] = ipAddress
			      assetNameDialog[14] = kvmDevice
			      assetNameDialog[15] = kvmPort
			      assetNameDialog[16] = newOrOld
			      assetNameDialog[17] = nicPort
			      assetNameDialog[18] = powerPort
			      assetNameDialog[19] = remoteMgmtPort
			      assetNameDialog[20] = truck
			      assetNameDialog[21] = assetType
			      assetNameDialog[22] = assetName
			      assetNameDialog[23] = assetTag
			      assetNameDialog[24] = serialNumber
			      assetNameDialog[25] = application			      
			      assetNameDialog[26] = "null"      
			
			      ${remoteFunction(action:'updateAssetEntity', params:'\'assetDialog=\' + assetNameDialog', onComplete:'showEditAsset(e)')}
			      return true
		      }
		    }
		    
		    function showEditAsset(e) {

		      $("#editDialog").dialog("close")
		      var asset = eval('(' + e.responseText + ')')
		
		      var x = document.getElementById('assetEntityTable').rows
		      var y = x[rowId].cells
		      x[rowId].style.background = '#65a342'
		      y[1].innerHTML = asset.model
		      y[2].innerHTML = asset.sourceLocation		      
		      y[3].innerHTML = asset.sourceRack		      
		      y[4].innerHTML = asset.sourceRackPosition
		      y[5].innerHTML = asset.assetName
		      if(asset.assetTypeId == null) {
		      y[6].innerHTML = ""
		      }else{
		      y[6].innerHTML = asset.assetTypeId
		      }		      
		      y[7].innerHTML = asset.assetTag
		      y[8].innerHTML = asset.serialNumber

      		}
		    
		    
		    function setRowId(val){

      			rowId = val.id

      		}
      		
      		function validateAssetEntity() {
      			if( document.createForm.assetName.value == null || document.createForm.assetName.value == "" ){
      				alert(" Please Enter Asset Name. ")
      				return false
      			} else {
      				return true
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

			<g:sortableColumn property="model" title="Model" />

			<g:sortableColumn property="sourceLocation" title="Source Location" />

			<g:sortableColumn property="sourceRack" title="Source Rack" />

			<g:sortableColumn property="sourceRackPosition"
				title="Source Rack Position" />

			<g:sortableColumn property="assetName" title="Asset Name" />

			<g:sortableColumn property="assetType" title="Asset Type" />

			<g:sortableColumn property="assetTag" title="Asset Tag" />

			<g:sortableColumn property="serialNumber" title="Serial Number" />


		</tr>
	</thead>
	<tbody>
		<%  int k = 1 %>
		<g:each in="${assetEntityInstanceList}" status="i"
			var="assetEntityInstance">
			<tr id="${k}" onClick="setRowId(this)"
				onmouseover="style.backgroundColor='#87CEEE';"
				onmouseout="style.backgroundColor='white';">

				<td><g:remoteLink controller="assetEntity" action="editShow"
					id="${assetEntityInstance.id}" onComplete="showAssetDialog( e );">
					<img src="/tds/images/asset_view.png" border="0px">
				</g:remoteLink></td>				

				<td>${fieldValue(bean:assetEntityInstance, field:'model')}</td>

				<td>${fieldValue(bean:assetEntityInstance,
				field:'sourceLocation')}</td>

				<td>${fieldValue(bean:assetEntityInstance, field:'sourceRack')}</td>

				<td>${fieldValue(bean:assetEntityInstance,
				field:'sourceRackPosition')}</td>

				<td>${fieldValue(bean:assetEntityInstance, field:'assetName')}</td>

				<td>${fieldValue(bean:assetEntityInstance, field:'assetType')}</td>

				<td>${fieldValue(bean:assetEntityInstance, field:'assetTag')}</td>

				<td>${fieldValue(bean:assetEntityInstance,
				field:'serialNumber')}</td>

			</tr>
			<%  k = ++k %>
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
				<td valign="top" class="name"><label for="attributeSet">Attribute Set:</label></td>
				<td valign="top"><g:select optionKey="id" from="${com.tdssrc.eav.EavAttributeSet.list()}" name="attributeSet.id" value="${assetEntityInstance?.attributeSet?.id}" noSelection="['':'select']" 
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
	<input type="hidden" name="id" value="${assetEntityInstance?.id}" />
	<input type="hidden" name="projectId" value="${projectId}" />
	 <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span></div>
</g:form></div>

<div id="editDialog" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" value="${assetEntityInstance?.id}" />
	<input type="hidden" name="projectId" value="" />
	<div class="dialog">
	<table>
		<tbody>			

			<tr class="prop">
				<td valign="top" class="name"><label for="model">Model:</label>
				</td>
				<td valign="top"><input type="text" size="35" id="model"
					name="model" value="" /></td>

				<td valign="top" class="name"><label for="fiberCabinet">Fiber
				Cabinet:</label></td>
				<td valign="top"><input type="text" size="35" id="fiberCabinet"
					name="fiberCabinet" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="sourceLocation">Source
				Location:</label></td>
				<td valign="top"><input type="text" size="35"
					id="sourceLocation" name="sourceLocation" value="" /></td>

				<td valign="top" class="name"><label for="hbaPort">Hba
				Port:</label></td>
				<td valign="top"><input type="text" size="35" id="hbaPort"
					name="hbaPort" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="targetLocation">Target
				Location:</label></td>
				<td valign="top"><input type="text" size="35"
					id="targetLocation" name="targetLocation" value="" /></td>

				<td valign="top" class="name"><label for="hinfo">Hinfo:</label>
				</td>
				<td valign="top"><input type="text" size="35" id="hinfo"
					name="hinfo" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="sourceRack">Source
				Rack:</label></td>
				<td valign="top"><input type="text" size="35" id="sourceRack"
					name="sourceRack" value="" /></td>

				<td valign="top" class="name"><label for="ipAddress">Ip
				Address:</label></td>
				<td valign="top"><input type="text" size="35" id="ipAddress"
					name="ipAddress" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="targetRack">Target
				Rack:</label></td>
				<td valign="top"><input type="text" size="35" id="targetRack"
					name="targetRack" value="" /></td>

				<td valign="top" class="name"><label for="kvmDevice">Kvm
				Device:</label></td>
				<td valign="top"><input type="text" size="35" id="kvmDevice"
					name="kvmDevice" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="sourceRackPosition">Source
				Rack Position:</label></td>
				<td valign="top"><input type="text" size="35"
					id="sourceRackPosition" name="sourceRackPosition" value="" /></td>

				<td valign="top" class="name"><label for="kvmPort">Kvm
				Port:</label></td>
				<td valign="top"><input type="text" size="35" id="kvmPort"
					name="kvmPort" value="" /></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="targetRackPosition">Target
				Rack Position:</label></td>
				<td valign="top"><input type="text" size="35"
					id="targetRackPosition" name="targetRackPosition" value="" /></td>

				<td valign="top" class="name"><label for="newOrOld">New
				or Old:</label></td>
				<td valign="top"><input type="text" size="35" id="newOrOld"
					name="newOrOld" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="usize">Unit
				Size:</label></td>
				<td valign="top"><input type="text" size="35" id="usize"
					name="usize" value="" /></td>

				<td valign="top" class="name"><label for="nicPort">Nic
				Port:</label></td>
				<td valign="top"><input type="text" size="35" id="nicPort"
					name="nicPort" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="assetType">Asset
				Type:</label></td>
				<td valign="top"><g:select optionKey="id"
					from="${AssetType.list()}" name="assetType" value=""
					noSelection="['':'']"></g:select></td>

				<td valign="top" class="name"><label for="powerPort">Power
				Port:</label></td>
				<td valign="top"><input type="text" size="35" id="powerPort"
					name="powerPort" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="assetName">Asset
				Name:</label></td>
				<td valign="top"><input type="text" size="35" id="assetName"
					name="assetName" value="" /></td>

				<td valign="top" class="name"><label for="remoteMgmtPort">Remote
				Mgm Port:</label></td>
				<td valign="top"><input type="text" size="35"
					id="remoteMgmtPort" name="remoteMgmtPort" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="assetTag">Asset
				Tag:</label></td>
				<td valign="top"><input type="text" size="35" id="assetTag"
					name="assetTag" value="" /></td>

				<td valign="top" class="name"><label for="truck">Truck:</label>
				</td>
				<td valign="top"><input type="text" size="35" id="truck"
					name="truck" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="serialNumber">Serial
				Number:</label></td>
				<td valign="top"><input type="text" size="35" id="serialNumber"
					name="serialNumber" value="" /></td>

				<td valign="top" class="name"><label for="application">Application:</label>
				</td>
				<td valign="top"><input type="text" size="35" id="application"
					name="application" value="" /></td>
			</tr>
			
			<tr class="prop">				

				<td valign="top" class="name"><label for="manufacturer">Manufacturer:</label>
				</td>
				<td valign="top"><input type="text" size="35" id="manufacturer"
					name="manufacturer" value="" /></td>
			</tr>

		</tbody>
	</table>
	</div>
	<div class="buttons"><span class="button"><input
		type="button" class="save" value="Update Asset Entity"
		onClick="return callUpdateDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span></div>
</g:form></div>
</body>
</html>
