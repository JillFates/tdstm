

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
	
	        $("#dialog").dialog({ autoOpen: false })
	        $("#dialog1").dialog({ autoOpen: false })
	        $("#dialog2").dialog({ autoOpen: false })
	
	      })
	
	    </script>

<g:javascript>
	    	
	    	var rowId
		    function showAssetDialog( e ) {
				
		      var assetEntity = eval('(' + e.responseText + ')') 
		      
		      document.showForm.id.value = assetEntity.id
			  document.editForm.id.value = assetEntity.id    
			  document.showForm.serverName.value = assetEntity.serverName
			  document.editForm.serverName.value = assetEntity.serverName
			  document.showForm.model.value = assetEntity.model
			  document.editForm.model.value = assetEntity.model
			  document.showForm.sourceLocation.value = assetEntity.sourceLocation
			  document.editForm.sourceLocation.value = assetEntity.sourceLocation
			  document.showForm.targetLocation.value = assetEntity.targetLocation
			  document.editForm.targetLocation.value = assetEntity.targetLocation
			  document.showForm.sourceRack.value = assetEntity.sourceRack
			  document.editForm.sourceRack.value = assetEntity.sourceRack
			  document.showForm.targetRack.value = assetEntity.targetRack
			  document.editForm.targetRack.value = assetEntity.targetRack
			  document.showForm.sourceRackPosition.value = assetEntity.sourceRackPosition
			  document.editForm.sourceRackPosition.value = assetEntity.sourceRackPosition
			  document.showForm.targetRackPosition.value = assetEntity.targetRackPosition
			  document.editForm.targetRackPosition.value = assetEntity.targetRackPosition
			  document.showForm.usize.value = assetEntity.usize
			  document.editForm.usize.value = assetEntity.usize
			  document.showForm.manufacturer.value = assetEntity.manufacturer
			  document.editForm.manufacturer.value = assetEntity.manufacturer
			  document.showForm.fiberCabinet.value = assetEntity.fiberCabinet
			  document.editForm.fiberCabinet.value = assetEntity.fiberCabinet
			  document.showForm.hbaPort.value = assetEntity.hbaPort
			  document.editForm.hbaPort.value = assetEntity.hbaPort
			  document.showForm.hinfo.value = assetEntity.hinfo
			  document.editForm.hinfo.value = assetEntity.hinfo
			  document.showForm.ipAddress.value = assetEntity.ipAddress
			  document.editForm.ipAddress.value = assetEntity.ipAddress
			  document.showForm.kvmDevice.value = assetEntity.kvmDevice
			  document.editForm.kvmDevice.value = assetEntity.kvmDevice
			  document.showForm.kvmPort.value = assetEntity.kvmPort
			  document.editForm.kvmPort.value = assetEntity.kvmPort
			  document.showForm.newOrOld.value = assetEntity.newOrOld
			  document.editForm.newOrOld.value = assetEntity.newOrOld
			  document.showForm.nicPort.value = assetEntity.nicPort
			  document.editForm.nicPort.value = assetEntity.nicPort
			  document.showForm.remoteMgmPort.value = assetEntity.remoteMgmPort
			  document.editForm.remoteMgmPort.value = assetEntity.remoteMgmPort
			  document.showForm.truck.value = assetEntity.truck
			  document.editForm.truck.value = assetEntity.truck
			  if(assetEntity.assetTypeId == null) {
			  document.showForm.assetType.value = ""
			  document.editForm.assetType.value = ""
			  } else {
			  document.showForm.assetType.value = assetEntity.assetTypeId
			  document.editForm.assetType.value = assetEntity.assetTypeId
			  }
			  document.showForm.assetName.value = assetEntity.assetName
			  document.editForm.assetName.value = assetEntity.assetName
			  document.showForm.assetTag.value = assetEntity.assetTag
			  document.editForm.assetTag.value = assetEntity.assetTag
			  document.showForm.serialNumber.value = assetEntity.serialNumber
			  document.editForm.serialNumber.value = assetEntity.serialNumber
			  document.showForm.application.value = assetEntity.application
			  document.editForm.application.value = assetEntity.application			  
			  	
		      $("#dialog").dialog('option', 'width', 800)
		      $("#dialog").dialog("open")
		
		    }
	    	
	    	function createDialog(){

		      $("#dialog2").dialog('option', 'width', 800)
		      $("#dialog2").dialog("open")
		
		    }
		    
		    function editAssetDialog() {

		      $("#dialog").dialog("close")
		      $("#dialog1").dialog('option', 'width', 800)
		      $("#dialog1").dialog("open")
		
		    }
		    
		    function callUpdateDialog() {
			  if( document.editForm.serverName.value == null || document.editForm.serverName.value == "" ) {
      				alert(" Please Enter Server Name. ")
      				return false
			  } else {
			      var assetEntityId = document.editForm.id.value 
			      var serverName = document.editForm.serverName.value
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
			      var remoteMgmPort = document.editForm.remoteMgmPort.value
			      var truck = document.editForm.truck.value
			      var assetType = document.editForm.assetType.value		      
			      var assetName = document.editForm.assetName.value
			      var assetTag = document.editForm.assetTag.value
			      var serialNumber = document.editForm.serialNumber.value
			      var application = document.editForm.application.value
			      	      
			      
			      var assetNameDialog = new Array()
			      assetNameDialog[0] = assetEntityId
			      assetNameDialog[1] = serverName
			      assetNameDialog[2] = model
			      assetNameDialog[3] = sourceLocation
			      assetNameDialog[4] = targetLocation
			      assetNameDialog[5] = sourceRack
			      assetNameDialog[6] = targetRack
			      assetNameDialog[7] = sourceRackPosition
			      assetNameDialog[8] = targetRackPosition
			      assetNameDialog[9] = usize
			      assetNameDialog[10] = manufacturer
			      assetNameDialog[11] = fiberCabinet
			      assetNameDialog[12] = hbaPort
			      assetNameDialog[13] = hinfo
			      assetNameDialog[14] = ipAddress
			      assetNameDialog[15] = kvmDevice
			      assetNameDialog[16] = kvmPort
			      assetNameDialog[17] = newOrOld
			      assetNameDialog[18] = nicPort
			      assetNameDialog[19] = powerPort
			      assetNameDialog[20] = remoteMgmPort
			      assetNameDialog[21] = truck
			      assetNameDialog[22] = assetType
			      assetNameDialog[23] = assetName
			      assetNameDialog[24] = assetTag
			      assetNameDialog[25] = serialNumber
			      assetNameDialog[26] = application			      
			      assetNameDialog[27] = "null"     
			
			      ${remoteFunction(action:'updateAssetEntity', params:'\'assetDialog=\' + assetNameDialog', onComplete:'showEditAsset(e)')}
			      return true
		      }
		    }
		    
		    function showEditAsset(e) {

		      $("#dialog1").dialog("close")
		      var asset = eval('(' + e.responseText + ')')
		
		      var x = document.getElementById('assetEntityTable').rows
		      var y = x[rowId].cells
		      x[rowId].style.background = '#65a342'
		      y[1].innerHTML = asset.serverName
		      y[2].innerHTML = asset.model
		      y[3].innerHTML = asset.sourceLocation
		      y[4].innerHTML = asset.targetLocation
		      y[5].innerHTML = asset.sourceRack
		      y[6].innerHTML = asset.targetRack
		      y[7].innerHTML = asset.sourceRackPosition
		      y[8].innerHTML = asset.usize
		      if(asset.assetTypeId == null) {
		      y[9].innerHTML = ""
		      }else{
		      y[9].innerHTML = asset.assetTypeId
		      }
		      y[10].innerHTML = asset.assetName
		      y[11].innerHTML = asset.assetTag
		      y[12].innerHTML = asset.serialNumber

      		}
		    
		    
		    function setRowId(val){

      			rowId = val.id

      		}
      		
      		function validateAssetEntity() {
      			if( document.createForm.serverName.value == null || document.createForm.serverName.value == "" ){
      				alert(" Please Enter Server Name. ")
      				return false
      			} else {
      				return true
      			}
      		}
		      
	    </g:javascript>

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

			<g:sortableColumn property="serverName" title="Server Name" />

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

				<td>${fieldValue(bean:assetEntityInstance, field:'serverName')}</td>

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

<div id="dialog2" title="Create Asset Entity" style="display: none;">
<g:form action="save" method="post" name="createForm">
	<div class="dialog">

	<table>
		<tbody>

			<tr class="prop">
				<td valign="top" class="name"><label for="attributeSet">Attribute
				Set:</label></td>
				<td valign="top"><g:select optionKey="id"
					from="${com.tdssrc.eav.EavAttributeSet.list()}"
					name="attributeSet.id"
					value="${assetEntityInstance?.attributeSet?.id}"></g:select></td>

				<td valign="top" class="name"><label for="manufacturer">Manufacturer:</label>
				</td>
				<td valign="top"><input type="text" size="30" id="manufacturer"
					name="manufacturer" value="" /></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="serverName">Server
				Name:</label></td>
				<td valign="top"><input type="text" size="30" id="serverName"
					name="serverName" value="" /></td>

				<td valign="top" class="name"><label for="fiberCabinet">Fiber
				Cabinet:</label></td>
				<td valign="top"><input type="text" size="30" id="fiberCabinet"
					name="fiberCabinet" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="model">Model:</label>
				</td>
				<td valign="top"><input type="text" size="30" id="model"
					name="model" value="" /></td>

				<td valign="top" class="name"><label for="hbaPort">Hba
				Port:</label></td>
				<td valign="top"><input type="text" size="30" id="hbaPort"
					name="hbaPort" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="sourceLocation">Source
				Location:</label></td>
				<td valign="top"><input type="text" size="30"
					id="sourceLocation" name="sourceLocation" value="" /></td>

				<td valign="top" class="name"><label for="hinfo">Hinfo:</label>
				</td>
				<td valign="top"><input type="text" size="30" id="hinfo"
					name="hinfo" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="targetLocation">Target
				Location:</label></td>
				<td valign="top"><input type="text" size="30"
					id="targetLocation" name="targetLocation" value="" /></td>

				<td valign="top" class="name"><label for="ipAddress">Ip
				Address:</label></td>
				<td valign="top"><input type="text" size="30" id="ipAddress"
					name="ipAddress" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="sourceRack">Source
				Rack:</label></td>
				<td valign="top"><input type="text" size="30" id="sourceRack"
					name="sourceRack" value="" /></td>

				<td valign="top" class="name"><label for="kvmDevice">Kvm
				Device:</label></td>
				<td valign="top"><input type="text" size="30" id="kvmDevice"
					name="kvmDevice" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="targetRack">Target
				Rack:</label></td>
				<td valign="top"><input type="text" size="30" id="targetRack"
					name="targetRack" value="" /></td>

				<td valign="top" class="name"><label for="kvmPort">Kvm
				Port:</label></td>
				<td valign="top"><input type="text" size="30" id="kvmPort"
					name="kvmPort" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="sourceRackPosition">Source
				Rack Position:</label></td>
				<td valign="top"><input type="text" size="30"
					id="sourceRackPosition" name="sourceRackPosition" value="" /></td>

				<td valign="top" class="name"><label for="newOrOld">New
				or Old:</label></td>
				<td valign="top"><input type="text" size="30" id="newOrOld"
					name="newOrOld" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="targetRackPosition">Target
				Rack Position:</label></td>
				<td valign="top"><input type="text" size="30"
					id="targetRackPosition" name="targetRackPosition" value="" /></td>

				<td valign="top" class="name"><label for="nicPort">Nic
				Port:</label></td>
				<td valign="top"><input type="text" size="30" id="nicPort"
					name="nicPort" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="usize">Unit
				Size:</label></td>
				<td valign="top"><input type="text" size="30" id="usize"
					name="usize" value="" /></td>

				<td valign="top" class="name"><label for="powerPort">Power
				Port:</label></td>
				<td valign="top"><input type="text" size="30" id="powerPort"
					name="powerPort" value="" /></td>
			</tr>


			<tr class="prop">
				<td valign="top" class="name"><label for="assetType">Asset
				Type:</label></td>
				<td valign="top"><g:select optionKey="id"
					from="${AssetType.list()}" name="assetType.id"
					value="${assetEntityInstance?.assetType?.id}" noSelection="['':'']"></g:select>
				</td>

				<td valign="top" class="name"><label for="remoteMgmPort">Remote
				Mgm Port:</label></td>
				<td valign="top"><input type="text" size="30"
					id="remoteMgmPort" name="remoteMgmPort" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="assetName">Asset
				Name:</label></td>
				<td valign="top"><input type="text" size="30" id="assetName"
					name="assetName" value="" /></td>

				<td valign="top" class="name"><label for="truck">Truck:</label>
				</td>
				<td valign="top"><input type="text" size="30" id="truck"
					name="truck" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="assetTag">Asset
				Tag:</label></td>
				<td valign="top"><input type="text" size="30" id="assetTag"
					name="assetTag" value="" /></td>

				<td valign="top" class="name"><label for="application">Application:</label>
				</td>
				<td valign="top"><input type="text" size="30" id="application"
					name="application" value="" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="serialNumber">Serial
				Number:</label></td>
				<td valign="top"><input type="text" size="30" id="serialNumber"
					name="serialNumber" value="" /></td>
			</tr>

		</tbody>
	</table>
	</div>
	<div class="buttons"><input type="hidden" name="projectId"
		value="${projectId }" /> <span class="button"><input
		class="save" type="submit" value="Create"
		onclick="return validateAssetEntity();" /></span></div>
</g:form></div>


<div id="dialog" title="Show Asset Entity" style="display: none;">
<g:form action="save" method="post" name="showForm">
	<div class="dialog">
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top" class="name">Server Name:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="serverName" name="serverName" value="" style="border: 0px"
					readonly></td>

				<td valign="top" class="name">Manufacturer:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="manufacturer" name="manufacturer" value="" style="border: 0px"
					readonly></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name">Model:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="model" name="model" value="" style="border: 0px" readonly></td>

				<td valign="top" class="name">Fiber Cabinet:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="fiberCabinet" name="fiberCabinet" value="" style="border: 0px"
					readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Source Location:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="sourceLocation" name="sourceLocation" value=""
					style="border: 0px" readonly></td>

				<td valign="top" class="name">Hba Port:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="hbaPort" name="hbaPort" value="" style="border: 0px" readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Target Location:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="targetLocation" name="targetLocation" value=""
					style="border: 0px" readonly></td>

				<td valign="top" class="name">Hinfo:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="hinfo" name="hinfo" value="" style="border: 0px" readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Source Rack:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="sourceRack" name="sourceRack" value="" style="border: 0px"
					readonly></td>

				<td valign="top" class="name">Ip Address:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="ipAddress" name="ipAddress" value="" style="border: 0px"
					readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Target Rack:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="targetRack" name="targetRack" value="" style="border: 0px"
					readonly></td>

				<td valign="top" class="name">Kvm Device:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="kvmDevice" name="kvmDevice" value="" style="border: 0px"
					readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Source Rack Position:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="sourceRackPosition" name="sourceRackPosition" value=""
					style="border: 0px" readonly></td>

				<td valign="top" class="name">Kvm Port:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="kvmPort" name="kvmPort" value="" style="border: 0px" readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">target Rack Position:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="targetRackPosition" name="targetRackPosition" value=""
					style="border: 0px" readonly></td>

				<td valign="top" class="name">New or Old:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="newOrOld" name="newOrOld" value="" style="border: 0px" readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Unit Size:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="usize" name="usize" value="" style="border: 0px" readonly></td>

				<td valign="top" class="name">Nic Port:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="nicPort" name="nicPort" value="" style="border: 0px" readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Asset Type:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="assetType" name="assetType" value="" style="border: 0px"
					readonly></td>

				<td valign="top" class="name">Power Port:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="powerPort" name="powerPort" value="" style="border: 0px"
					readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Asset Name:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="assetName" name="assetName" value="" style="border: 0px"
					readonly></td>

				<td valign="top" class="name">Remote Mgm Port:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="remoteMgmPort" name="remoteMgmPort" value=""
					style="border: 0px" readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Asset Tag:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="assetTag" name="assetTag" value="" style="border: 0px" readonly></td>

				<td valign="top" class="name">Truck:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="truck" name="truck" value="" style="border: 0px" readonly></td>

			</tr>

			<tr class="prop">
				<td valign="top" class="name">Serial Number:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="serialNumber" name="serialNumber" value="" style="border: 0px"
					readonly></td>

				<td valign="top" class="name">Application:</td>

				<td valign="top" class="value"><input type="text" size="35"
					id="application" name="application" value="" style="border: 0px"
					readonly></td>

			</tr>


		</tbody>
	</table>
	</div>
	<div class="buttons"><input type="hidden" name="id"
		value="${assetEntityInstance?.id}" /> <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span></div>
</g:form></div>

<div id="dialog1" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" value="${assetEntityInstance?.id}" />
	<div class="dialog">
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top" class="name"><label for="serverName">Server
				Name:</label></td>
				<td valign="top"><input type="text" size="35" id="serverName"
					name="serverName" value="" /></td>

				<td valign="top" class="name"><label for="manufacturer">Manufacturer:</label>
				</td>
				<td valign="top"><input type="text" size="35" id="manufacturer"
					name="manufacturer" value="" /></td>
			</tr>

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

				<td valign="top" class="name"><label for="remoteMgmPort">Remote
				Mgm Port:</label></td>
				<td valign="top"><input type="text" size="35"
					id="remoteMgmPort" name="remoteMgmPort" value="" /></td>
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
