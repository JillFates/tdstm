<%@page import="com.tds.asset.AssetCableMap;com.tds.asset.AssetDependency;com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />

		<title>Data Center Room List</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="drag_drop.js" />
		<g:javascript src="room.rack.combined.js"/>
		<g:javascript src="cabling.js"/>
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>	
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="select2.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>	

	</head>
	<body>

		<input type = "hidden" id = "dstPath" name = "dstPath" value ="room"/>
		<div class="body" style="margin-top: 10px;width:98%;">
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		<div id="messageDivId" class="message" style="display: none;"></div>
		<div id="processDiv" style="display: none;">
			<img src="../images/processing.gif" /> Merging In Progress...
		</div>
		<div id="roomListView" style="width:500px;">
		<g:if test="${roomId && viewType != 'list'}">
		<!-- do nothing -->
		</g:if>
		<g:else>
		<fieldset><legend><b>Room List</b></legend>
		<g:form action="create" >

		<div style="float: left; width: auto;">
		<table>
			<thead>
				<tr>

					 <tds:hasPermission permission='RoomListActionColumn'><th><a href="#">Action</a></th></tds:hasPermission>

					<g:sortableColumn property="location" title="Data Center" />

					<g:sortableColumn property="roomName" title="Room" />
					
					<th><a href="#">City</a></th>

					<th><a href="#">state Prov</a></th>

					<th><a href="#">Rack count</a></th>

					<th><a href="#">Asset count</a></th>

				</tr>
			</thead>
			<tbody>
				<g:each in="${roomInstanceList}" status="i" var="roomInstance">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

						 <tds:hasPermission permission='RoomListActionColumn'>
							<td><input type="checkbox" class="listCheck" name="checkbox_${roomInstance.id}" id="roomCheckBoxId" onclick="enableActions()"></td>
						</tds:hasPermission>
						<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "location")}</td>
						
						<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "roomName")}</td>

						<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "city")}</td>

						<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "stateProv")}</td>
						
						<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${roomInstance.getRackCountByType('Rack')}</td>

						<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "assetCount")}</td>

					</tr>
				</g:each>
			</tbody>
		</table>
		<div class="buttons"> 
			<span class="button">
			 <tds:hasPermission permission='DeleteRoom'>
				<input type="button" class="edit" value="Create Room" onclick="$('#createRoomDialog').dialog('open');$('#mergeRoomDialog').dialog('close')"/>
			 </tds:hasPermission>
				 <tds:hasPermission permission='MergeRoom'>
					<span class="button"><input class="create disableButton" id="mergeId" type="button" value="Merge" onclick="showMergeDialog()" disabled="disabled" /></span>
				 </tds:hasPermission>
				 <tds:hasPermission permission='DeleteRoom'>
					<g:actionSubmit class="delete disableButton" action="delete" id="deleteId" value="Delete" disabled="disabled"/>
				</tds:hasPermission>
			</span>
		</div>
		</div>
		</g:form>
		</fieldset>
		</g:else>
		</div>
		<div id="roomShowView" name="roomShowView" style="display: none;">
		</div>
		</div>
		<div id="listDialog" title="Asset List" style="display: none;">
				<div class="dialog" >
					<table id="listDiv">
					</table>
				</div>
		</div>
		<div id="createRoomDialog" title="Create Room" style="display: none;">
			<g:form method="post" name="createRoomForm" action="save" onsubmit="return validateForm()">
				<table>
					<tbody>
						<tr>
							<td>Data Center<td/>
							<td>
								<input type="hidden" name="project.id" id="projectId" value="${projectId}">
								<input type="text" name="location" id="locationId" value="${roomInstance.location}">
							</td>
						</tr>
						<tr>
							<td>Room<td/>
							<td>
								<input type="text" name="roomName" id="roomNameId" value="${roomInstance.roomName}">
							</td>
						</tr>
						<tr>
							<td>Width<td/>
							<td>
								<input type="text" name="roomWidth" id="roomWidthId" value="${roomInstance.roomWidth}">
							</td>
						</tr>
						<tr>
							<td>Depth<td/>
							<td>
								<input type="text" name="roomDepth" id="roomDepthId" value="${roomInstance.roomDepth}">
							</td>
						</tr>
						<tr>
							<td>Address<td/>
							<td>
								<input type="text" name="address" id="addressId" value="">
							</td>
						</tr>
						<tr>
							<td>City<td/>
							<td>
								<input type="text" name="city" id="cityId" value="">
							</td>
						</tr>	
						<tr>
							<td>stateProv<td/>
							<td>
								<input type="text" name="stateProv" id="stateProvId" value="">
							</td>
						</tr>
						<tr>
							<td>Postal Code<td/>
							<td>
								<input type="text" name="postalCode" id="postalCodeId" value="">
							</td>
						</tr>
						<tr>
							<td>Country<td/>
							<td>
								<input type="text" name="country" id="countryId" value="USA">
							</td>
						</tr>
						
						<tr>
							<td class="buttons" colspan="4">
								<input type="submit" class="save" value="Save" />
								<input type="button" class="show" value="Cancel" onclick="$('#createRoomDialog').dialog('close');" />
							</td>
						</tr>
					</tbody>
				</table>
			</g:form>
		</div>
		<div id="mergeRoomDialog" title="Merge Room" style="display: none;">
			<g:form method="post" name="mergeRoomForm" action="mergeRoom">
				<table>
					<thead>
						<tr>
							<th>Data Center<input type="hidden" name="sourceRoom" id="sRoomId"> </th>
							<th>Room<input type="hidden" name="targetRoom" id="tRoomId"></th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${roomInstanceList}" status="i" var="roomInstance">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'} pointer" id="mergeRow_${roomInstance.id}" onclick="submitMergeForm(this.id)">
								
								<td>${fieldValue(bean: roomInstance, field: "location")}</td>
						
								<td>${fieldValue(bean: roomInstance, field: "roomName")}</td>

							</tr>
						</g:each>
					</tbody>
				</table>
			</g:form>
		</div>
		<div style="display: none;" id="cablingDialogId">
			<div id="cablingPanel" style="height: auto; ">
				<g:if test="${currentBundle}">
				<g:each in="${models}" var="model">
					<g:if test="${model?.rearImage && model?.useImage == 1}">
					<img id="rearImage${model.id}" src="${createLink(controller:'model', action:'getRearImage', id:model.id)}" style="display: none;"/>
					</g:if>
				</g:each>
				</g:if>
			</div>
			<div class="inputs_div">
				<g:form controller='rackLayouts' action="updateCablingDetails" name="cablingDetailsForm">
				<div id="actionButtonsDiv" style="margin-top: 5px;float: left;display: none;">
					<input type="button" value="Unknown" onclick="openActionDiv(this.id)" id="unknownId"/>
					<input type="button" value="0" onclick="openActionDiv(this.id)" style="background-color: #5F9FCF;" id="emptyId"/>
					<input type="button" value="X" onclick="openActionDiv(this.id)" id="cabledId"/>
					<input type="button" value="Assign" onclick="openActionDiv(this.id)" id="assignId"/>
				</div>
				<div id="actionDiv" style="margin-top: 5px;float: right;display: none;">
					<input type="button" value="Ok" onclick="submitAction($('form[name=cablingDetailsForm]'))"/>
					<input type="button" value="Cancel"  onclick="cancelAction()"/>
					<g:select id="colorId" name="color" from="${AssetCableMap.constraints.cableColor.inList}" noSelection="${['':'']}" onchange="updateCell(this.value)"></g:select>
					<input type="reset" id="formReset" style="display: none;"/>
				</div>
				<div style="clear: both;"></div>
				<div style="text-align: center;margin-bottom: 5px;display: none;" id="assignFieldDiv">
					<div id="inputDiv">
						<input type="text" name="rack" id="rackId" size="10"  onblur="validateRackData( this.value, this.id );"/>
						<input type="text" name="uposition" id="upositionId" size="2" maxlength="2" onfocus="getUpositionData()" onblur="validateUpositionData( this.value, this.id)"/>
						<input type="text" name="connector" id="connectorId" size="15" onfocus="getConnectorData()" onblur="validateConnectorData(this.value, this.id)" />
					</div>
					<div id="powerDiv" style="display: none;">
						<input type="radio" name="staticConnector" id="staticConnector_A" value="A">A</input>&nbsp;
						<input type="radio" name="staticConnector" id="staticConnector_B" value="B">B</input>&nbsp;
						<input type="radio" name="staticConnector" id="staticConnector_C" value="C">C</input>
					</div>
					<div>
						<input type="hidden" name="assetCable" id="cabledTypeId"/>
						<input type="hidden" name="actionType" id="actionTypeId"/>
						<input type="hidden" name="connectorType" id="connectorTypeId"/>
						<input type="hidden" name="asset" id="assetEntityId"/>
						<input type="hidden" id="previousColor"/>
					</div>
				</div>
				
				</g:form>
			</div>
			<div style="clear: both;"></div>
			<div class="list">
				<table>
					<thead>
						<tr>
							<th>Type</th>
							<th>Label</th>
							<th>Status</th>
							<th>Color</th>
							<th>assetTo/Conn</th>
						</tr>
					</thead>
					<tbody id="cablingTableBody">
					<tr>
						<td colspan="5">No Connectors found</td>
					</tr>
					</tbody>
				</table>
			</div>
		</div>
		<g:render template="../assetEntity/commentCrud"/>
		<g:render template="../assetEntity/modelDialog"/>
		<div id ="createEntityView" style="display: none" ></div>
		<div id ="showEntityView" style="display: none"></div>
		<div id ="editEntityView" style="display: none"></div>
		<div id="editManufacturerView" style="display: none;"></div>
		<input type="hidden" id="role" value="role"/>

		<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
	<script type="text/javascript">   
		
		var roomId = "${roomId}"
		var viewType = "${viewType}"
		if (roomId && viewType != 'list') {
			var rackId = "${filterRackId}"
			$.ajax({
				url: contextPath+"/room/show",
				data: {'id':roomId},
				type:'POST',
				success: function(data) {
					$("#roomListView").hide()
					$("#roomShowView").html(data)
					$("#roomShowView").show()
					if(rackId)
						getTimeOut(rackId)
				}
			});
		}
		$(document).ready(function() {
			$("#editDialog,#createRoomDialog,#mergeRoomDialog,#listDialog,#cablingDialogId").dialog({ autoOpen: false })
			$("#manufacturerShowDialog,#modelShowDialog,#createEntityView,#showEntityView").dialog({autoOpen: false})
			$("#editEntityView,#commentsListDialog,#createCommentDialog,#showCommentDialog").dialog({ autoOpen: false })
			$("#editCommentDialog,#editManufacturerView").dialog({ autoOpen: false})
		})
		function openRoomView(e,browser){
			
			var resp = e.responseText
			$("#roomListView").hide()
			$("#roomShowView").html(resp)
			$("#roomShowView").show()
			if(browser){
				$("#bundleId").removeAttr("multiple")
			}
			$("#room_layout").css("height","auto")
		}


		function enableActions(){
			var inputCheckBox = $("input:checkbox")
			var enableButtons = 0
			$('.listCheck:checked').each(function(index, val) {
				if ($(this).attr('id') == "roomCheckBoxId") {
				  enableButtons++;
				}
			});
			if(enableButtons == 1){
				$("#mergeId").removeAttr("disabled");
				$("#mergeId").removeClass('disableButton')
				var checkBoxId = $("input:checked").attr('name')
				var roomId = checkBoxId.substring(9,checkBoxId.length)
				jQuery.ajax({
					url: "verifyRoomAssociatedRecords",
					data: "roomId="+roomId,
					type:'POST',
					success: function(data) {
						if(data == null || data == ""){
							$("#deleteId").removeAttr("disabled");
							$("#deleteId").removeClass('disableButton')
						}
					}
				});
			} else {
				$("#mergeId, #deleteId").attr("disabled", "disabled");
				$("#mergeId, #deleteId").addClass('disableButton')
			}
		}
		function showMergeDialog(){
			var inputCheckBox = $("input:checked")
			var checkBoxId = inputCheckBox.attr('id')
			var sRoomId = inputCheckBox.attr('name').substring(inputCheckBox.attr('name').indexOf("_")+1,checkBoxId.length)

			$("#mergeRoomDialog tbody tr").each(function() {
				var rowId = $(this).attr('id')
				if(rowId.substring(9,rowId.length) == sRoomId){
					$(this).hide()
				} else {
					$(this).show()
				}
			});
			$("#sRoomId").val(sRoomId)
			$('#createRoomDialog').dialog('close');
			$('#mergeRoomDialog').dialog('open')
		}
		function submitMergeForm(selectedRoom){
			var tRoomId = selectedRoom.substring(9,selectedRoom.length)
			if(tRoomId){
				$("#tRoomId").val(tRoomId)
				$('.message').hide();
				$('#mergeRoomDialog').dialog('close');
				$("#processDiv").show();
				$("form#mergeRoomForm").submit()
			}
		}
		function validateForm(){
			if($("#locationId").val()!="" && $("#roomName").val()!=""){
				return true
			} else {
				alert("ERROR : Data Center and Room should not be blank")
				return false
			}
		}
		function getTimeOut(rackId){
			setTimeout(function(){
					getRackLayout( rackId )
			},600);
		}
		function getRackLayout( rackId ){
			if(rackId){
				var otherBundle = $("#otherBundle").is(":checked") ? 'on' : ''
				var moveBundleId = ''
				$("#bundleId option:selected").each(function () {
					moveBundleId +="moveBundleId="+$(this).val()+"&"
				});
				
				$(".objectSelected").removeClass("objectSelected")
				$("#rack_"+rackId).addClass("objectSelected")
				
				var forWhom = $("#auditCheckId").val() == 1 ? "room" : ""
				new Ajax.Request('../rackLayouts/save',{asynchronous:true,evalScripts:true,
					onSuccess:function(e){updateRackPower( rackId )},
					onComplete:function(e){
							jQuery('#rackLayout').html(e.responseText);
							if(forWhom=='room'){
								$("#auditDetailViewId").show();
								$('#rackLayout').addClass('audit');
								$('#rackLayout').removeClass('notAudit'); 
							}else{
								$("#auditDetailViewId").hide();
								$('#rackLayout').addClass('notAudit');
								$('#rackLayout').removeClass('audit');
							}
							getAssignedDetails('room', rackId)
						},parameters:moveBundleId+'rackId='+rackId+'&backView=off&showCabling=off&otherBundle='+otherBundle+'&bundleName=on&hideIcons=on&forWhom='+forWhom});return false;
			}
		}
		currentMenuId = "#roomsMenu";
		$("#roomMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
