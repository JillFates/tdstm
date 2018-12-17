<%@page import="com.tds.asset.AssetCableMap" %>
<%@page import="com.tds.asset.AssetDependency" %>
<%@page import="com.tds.asset.AssetEntity" %>
<%@page import="com.tds.asset.Application" %>
<%@page import="com.tds.asset.Database" %>
<%@page import="com.tds.asset.Files" %>
<%@page import="net.transitionmanager.domain.Room" %>
<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />

		<title>Location List</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:render template="../layouts/responsiveAngularResources" />
		<g:javascript src="asset.comment.js" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<asset:stylesheet href="css/jquery.autocomplete.css" />
		<asset:stylesheet href="css/ui.accordion.css" />
		<asset:stylesheet href="css/ui.resizable.css" />
		<asset:stylesheet href="css/ui.slider.css" />
		<asset:stylesheet href="css/ui.tabs.css" />
		<asset:stylesheet href="css/ui.datepicker.css" />
		<asset:stylesheet href="css/resources/ui.datetimepicker.css" />
		<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
		<asset:stylesheet href="css/rackLayout.css" />
		<g:javascript src="drag_drop.js" />
		<g:javascript src="room.rack.combined.js"/>
		<g:javascript src="cabling.js"/>

	</head>
	<body>
	<g:if test="${roomId && viewType != 'list'}">
		<!-- do nothing -->
	</g:if>
	<g:else>
		<tds:subHeader title="Location List" crumbs="['Locations List']"/>
	</g:else>
		<input type = "hidden" id = "dstPath" name = "dstPath" value ="room"/>
		<div class="body fluid" style="margin-top: 10px;width:98%;" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">

			<g:render template="../assetEntity/listTitleAlerts" ></g:render>

			<g:if test="${flash.error}">
				<div class="errors">${flash.error}</div>
			</g:if>

			<div id="processDiv" style="display: none;">
				<asset:image src="images/processing.gif" /> Merging In Progress...
			</div>
			<div id="roomListView" style="width:500px;">
				<g:if test="${roomId && viewType != 'list'}">
				<!-- do nothing -->
				</g:if>
				<g:else>
						<g:form action="create" >

							<div style="float: left; width: auto;">
								<table>
									<thead>
										<tr>

											<tds:hasPermission permission="${Permission.RoomListActionColumn}"><th><a href="#">Action</a></th></tds:hasPermission>

											<g:sortableColumn property="location" title="Location" />

											<g:sortableColumn property="roomName" title="Room/VPC" />

											<th><a href="#">City</a></th>

											<th><a href="#">State Prov</a></th>

											<th><a href="#">Rack count</a></th>

											<th><a href="#">Asset count</a></th>

											<th><a href="#">Source / Target</a></th>
										</tr>
									</thead>
									<tbody>
										<g:each in="${roomInstanceList}" status="i" var="roomInstance">
											<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

												 <tds:hasPermission permission="${Permission.RoomListActionColumn}">
													<td><input type="checkbox" class="listCheck" name="checkbox_${roomInstance.id}" id="roomCheckBoxId" onclick="enableActions()"></td>
												</tds:hasPermission>
												<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(XMLHttpRequest)')}">${fieldValue(bean: roomInstance, field: "location")}</td>

												<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(XMLHttpRequest)')}">${fieldValue(bean: roomInstance, field: "roomName")}</td>

												<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(XMLHttpRequest)')}">${fieldValue(bean: roomInstance, field: "city")}</td>

												<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(XMLHttpRequest)')}">${fieldValue(bean: roomInstance, field: "stateProv")}</td>

												<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(XMLHttpRequest)')}">${roomInstance.getRackCountByType('Rack')}</td>

												<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(XMLHttpRequest)')}">${fieldValue(bean: roomInstance, field: "assetCount")}</td>
												<td style="cursor: pointer;" onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(XMLHttpRequest)')}">${roomInstance.source ? 'Source' : 'Target'}</td>

											</tr>
										</g:each>
									</tbody>
								</table>
								<div class="buttons">
									<span class="button">
									    <tds:hasPermission permission="${Permission.RoomCreate}">
										   <input type="button" class="edit" value="Create" onclick="$('#createRoomDialog').dialog('open'); saveData();"/>
									    </tds:hasPermission>
										<tds:hasPermission permission="${Permission.RoomDelete}">
											<g:actionSubmit class="delete disableButton" action="delete" id="deleteId" value="Delete" disabled="disabled"/>
										</tds:hasPermission>
									</span>
								</div>
							</div>
						</g:form>
					</fieldset>
				</g:else>
			</div>
			<div id="roomShowView" name="roomShowView" style="display: none;"></div>
		</div>
		<div id="listDialog" title="Asset List" style="display: none;">
		</div>
		<div id="createRoomDialog" title="Create Location Room/VPC" style="display: none;">
			<g:form method="post" name="createRoomForm" action="save" onsubmit="return validateForm()">
				<table>
					<tbody>
						<tr>
							<td colspan="3"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
						</tr>
						<tr>
							<td>Location<span style="color: red">*</span><td/>
							<td>
								<input type="hidden" name="project.id" id="projectId" value="${projectId}">
								<input type="text" name="location" id="locationId" value="${roomInstance.location}">
							</td>
						</tr>
						<tr>
							<td>Room/VPC<span style="color: red">*</span><td/>
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
							<td>State/Prov<td/>
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
							</td>
						</tr>
					</tbody>
				</table>
			</g:form>
		</div>

		<g:render template="../assetEntity/modelDialog"/>
		<g:render template="../assetEntity/entityCrudDivs" />
		<g:render template="../assetEntity/dependentAdd" />

		<input type="hidden" id="role" value="role"/>

	<script type="text/javascript">

		var roomId = "${roomId}"
		var viewType = "${viewType}"
		if (roomId && viewType != 'list') {
			var rackId = "${filterRackId}"
			$.ajax({
				url: contextPath+"/room/show",
				data: {'id':roomId},
				type:'POST',
				success: function(data) {aler(JSON.stringify(data));
					$("#roomListView").hide()
					$("#roomShowView").html(data)
					$("#roomShowView").show()
					if(rackId)
						getTimeOut(rackId)
				}
			});
		}
		$(document).ready(function() {
			$("#editDialog,#createRoomDialog").dialog({modal: true, autoOpen: false,open:function(){
				$('.ui-dialog-titlebar-close').html('')
				.append('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
			},close:function(){ location.reload();} });
			$("#editEntityView").dialog({close:function(){ location.reload();} });
			$("#manufacturerShowDialog,#modelShowDialog").dialog({modal: true,autoOpen: false})
			$("#commentsListDialog,#createCommentDialog,#showCommentDialog").dialog({modal: true, autoOpen: false })
			$("#editCommentDialog,#editManufacturerView").dialog({modal: true, autoOpen: false})
			$(document).on('entityAssetCreated', function(e) { updateRackLayoutView(); });
			$(document).on('entityAssetUpdated', function(e) { updateRackLayoutView(); });
		});


		var dataStore = {};
		function saveData() {
			var container = $('.ui-dialog .ui-dialog-titlebar');
			var content = $('#createRoomDialog form');

			if (content.length == 0) {
                container.after(dataStore.content);
			}
            dataStore.content = content;
		}

		function updateRackLayoutView(){
			var currentRackId = $('#selectedRackId').val()
			if ((currentRackId != null) && (currentRackId != "")) {
				getRackLayout(currentRackId);
			}
		}

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
			if (rackId) {
				var otherBundle = $("#otherBundle").is(":checked") ? 'on' : ''
				var moveBundleId = ''
				$("#bundleId option:selected").each(function () {
					moveBundleId +="moveBundleId="+$(this).val()+"&"
				});

				$(".objectSelected").removeClass("objectSelected")
				$("#rack_"+rackId).addClass("objectSelected")

				var forWhom = $("#auditCheckId").val() == 1 ? "room" : ""
				new Ajax.Request('../rackLayouts/generateElevations',{asynchronous:true,evalScripts:true,
					onSuccess:function(e){updateRackPower( rackId )},
					onComplete:function(e){
							jQuery('#rackLayout').html(e.responseText);
							if (forWhom=='room') {
								$("#auditDetailViewId").show();
								$('#rackLayout').addClass('audit');
								$('#rackLayout').removeClass('notAudit');
							} else {
								$("#auditDetailViewId").hide();
								$('#rackLayout').addClass('notAudit');
								$('#rackLayout').removeClass('audit');
							}
							getAssignedDetails('room', rackId)
						}, parameters:moveBundleId+'rackId='+rackId+'&viewMode=Generate&backView=off&showCabling=off&otherBundle='+otherBundle+'&bundleName=on&hideIcons=on&forWhom='+forWhom});return false;
			}
		}
		currentMenuId = "#roomsMenu";

		if(viewType && viewType == 'list') {
			$(".menu-parent-data-centers-list-rooms").addClass('active');
		} else {
			$(".menu-parent-data-centers-selected-center").addClass('active');
		}

		$(".menu-parent-data-centers").addClass('active');

	</script>
		<g:render template="../assetEntity/initAssetEntityData"/>
	</body>
</html>
