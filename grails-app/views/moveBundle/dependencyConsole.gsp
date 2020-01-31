<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta name="layout" content="topNav" />
		<title>Dependency Analyzer</title>

		<asset:stylesheet href="css/force.css" />
		<asset:stylesheet href="css/ui.datepicker.css" />
		<asset:stylesheet href="css/progressbar.css" />
		<asset:stylesheet href="css/tds-bootstrap.css" />
		<asset:stylesheet href="css/tds-progressbar.css" />

		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="projectStaff.js" />
		<g:render template="/layouts/responsiveAngularResources" />
		<g:javascript src="progressBar.js" />

		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<g:javascript src="d3/d3.js"/>
		<g:javascript src="svg.js"/>
		<g:javascript src="load.shapes.js"/>
		<g:javascript src="keyevent_constants.js" />
		<g:javascript src="graph.js" />
		<g:javascript src="tds-util.js" />
		<g:javascript src="generator/runtime.js" />
		<g:javascript src="generator/generator.js" />
	</head>
	<body>
		<tds:subHeader title="Dependency Analyzer" crumbs="['Assets','Dependency Analyzer']"/>
		<input type="hidden" id="redirectTo" name="redirectTo" value="dependencyConsole" />
		<div class="body fluid" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
		<div id="DependencyGroupsTableId">
			<div id="checkBoxDiv" title="Dependency Grouping Control" style="display: none;" class="static-dialog">
				<div id="checkBoxDivId" class="tds-modal-content" style="overflow-x: hidden; padding:unset;">
					<div class="modal-header">
						<button class="btn btn-icon close-button" onclick="hideDependencyControlDiv()">
							<i class="fas fa-times"></i>
						</button>

						<div class="modal-title-container">
							<div class="modal-title" style="padding: unset;">Dependency Grouping Control</div>
						</div>
					</div>

					<div class="modal-body">
						<g:form name="checkBoxForm">
							<div class="modal-grid-2">
								<div>
									<div class="modal-section-header">Connection Type</div>
									<div class="checkboxdiv_control">
										<g:each in="${dependencyType}" var="dependency">
											<div class="clr-control-container"  style="display:block;">
												<div class="clr-checkbox-wrapper">
													<input type="checkbox" id="${dependency}"
														name="connection" value="${dependency}" ${depGrpCrt.connectionTypes ? (depGrpCrt.connectionTypes.contains(dependency) ? 'checked' : '') : ([ 'Batch' ].contains(dependency) ? "" : "checked")}/>
													<label class="clr-control-label" id="dependecy_${dependency}" for="${dependency}">${dependency}</label>
												</div>
											</div>
										</g:each>
									</div>
								</div>
								<div>
									<div class="modal-section-header">Connection Status</div>
									<div class="checkboxdiv_control">
										<g:each in="${dependencyStatus}" var="dependencyStatusInst">
											<div class="clr-control-container"  style="display:block;">
												<div class="clr-checkbox-wrapper">
													<input type="checkbox" id="${dependencyStatusInst}"
													name="status" value="${dependencyStatusInst}" ${depGrpCrt.statusTypes ? (depGrpCrt.statusTypes.contains(dependencyStatusInst) ? 'checked' : '') : (['Archived','Not Applicable'].contains(dependencyStatusInst) ? '' : 'checked')}/>
													<label class="clr-control-label" id="dependecy_${dependencyStatusInst}" for="${dependencyStatusInst}">${dependencyStatusInst}</label>
												</div>
											</div>
										</g:each>
									</div>
								</div>
							</div>
							<div class="checkboxdiv_default">
								<div class="checkboxdiv_control">
									<div class="clr-control-container ">
										<div class="clr-checkbox-wrapper">
											<input type="checkbox" id="saveDefault" name="saveDefault" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
											<label class="clr-control-label" for="saveDefault" style="font-weight:600;">Save As Defaults</label>
										</div>
									</div>
								</div>
							</div>
						</g:form>
					</div>

					<div class="modal-footer">
						<button
							type="button"
							id="generateId"
							class="btn btn-icon btn-outline"
							value="Generate"
							onclick="submitCheckBox()"
							>
							<i class="fas fa-chart-bar"></i> Regenerate
						</button>
					</div>	
				</div>
			</div>


			<div style="clear: both;"></div>

			<div id="dependencyBundleDetailsId" >
				<div style="margin-top: 10px;">
					<div class="compactClass">
						<div class="dependency-filters-container">
							<input type="hidden" id="tabTypeId" name="tabType" value="${asset}" />
							<div class="message" id="messageId" style="display:none">${flash.message}</div>
							<div class="row dependencyConsoleForm">
								<div class="f-grid" id="angular-compiler">
									<div class="f-column-1" style="display: flex; max-width:300px; align-items: center;">
										<label>Dependency Groups</label>
										<tds:hasPermission permission="${Permission.DepAnalyzerGenerate}">
											<input type="button" class="btn btn-sm" value="Regenerate" onclick="showDependencyControlDiv()"/>
										</tds:hasPermission>
									</div>
									<div class="f-column-2">
										<label>Bundle:</label>
										<div class="clr-select-wrapper">
											<g:select class="clr-select" style="width:160px; margin-left:10px;" id="planningBundleSelectId" name="bundle" from="${moveBundle}" noSelection="${['':'All Planning']}" optionKey="id" value="${moveBundleId}" onchange="onDependencyFiltersChange()"/>
										</div>
									</div>
									<div class="f-column-3">
										<label>Tags:</label>
										<tm-asset-tag-selector ng-init="assetsSelector.tag = ${tagIds}" id="tmHighlightGroupSelector" pre-asset-selector="assetsSelector" pre-selected-operator="'${tagMatch}'" asset-selector="dependencyGroup.assetSelector" on-change="onDependencyFiltersChange()"></tm-asset-tag-selector>
									</div>
									<div class="f-column-4">
										<div class="clr-checkbox-wrapper">
											<input class="clr-checkbox" type="checkbox" id="assignedGroupCB" ${isAssigned == '1' ? 'checked="checked"' : ''} onchange="onDependencyFiltersChange()">
											<label for="assignedGroupCB">Show ONLY Work In Progress</label>
											<g:link controller="moveBundle" action="dependencyConsole"  style="display:inline-block; margin-left: 10px;">
												<span style="font-size:16px; color:#0077b8;" title="Refresh Data"><i class="fas fa-sync"></i></span>
											</g:link>
										</div>

									</div>
								</div>
							</div>
						</div>
						<div id="processDiv" style="display: none;">
							<asset:image src="images/processing.gif" />
						</div>
					</div>
				</div>
				<g:render template="dependencyBundleDetails" />
			</div>

			<div style="clear: both;"></div>

			<div id="moveBundleSelectId" title="Assignment" style="display: none;" class="static-dialog tds-modal-content">
				<div class="modal-header">
					<button class="btn btn-icon close-button" onclick="$('#moveBundleSelectId').dialog('close')">
						<i class="fas fa-times"></i>
					</button>

					<div class="modal-title-container">
						<div class="modal-title" style="padding: unset;">Asset Assignment</div>
					</div>
				</div>
				
				<g:form name="changeBundle" action="assetsAssignment" class="clr-form clr-form-horizontal clr-form-compact" >
					<div class="modal-body">
						<input type="hidden" name="assetType" id="assetsTypeId"  />
						<input type="hidden" name="bundleSession" id="bundleSession" />
						<div>Assign selected assets to:</div>
						
						<div class="clr-form-control">
							<label for="tmAssignmentTagSelector" class="clr-control-label" style="width: 4rem; min-width: unset;">Tags</label>
							<div class="clr-control-container">
								<tm-asset-tag-selector hide-operator="true" form-data='true' id="tmAssignmentTagSelector" pre-asset-selector="assignments.preAssetSelector" asset-selector="assignments.assetSelector"></tm-asset-tag-selector>
							</div>
						</div>

						<div class="clr-form-control">
							<label for="plannedStatus" class="clr-control-label" style="width: 4rem; min-width: unset;">Plan Status</label>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select name="planStatus" id="plannedStatus" from="${planStatusOptions}" optionKey="value" optionValue="value" class="clr-select"></g:select>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<label for="plannedMoveBundleList" class="clr-control-label" style="width: 4rem; min-width: unset;">Bundle</label>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select name="moveBundle" id="plannedMoveBundleList" class="clr-select" from="${moveBundle}" optionKey="id" optionValue="name" onchange="changeBundleSelect()" noSelection="${['':'Please Select']}"></g:select>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<div class="clr-control-container">
								<div class="clr-radio-wrapper">
									<input type="radio" name="bundles" id="planningBundle" value="planningBundle" checked="checked" onChange="changeBundles(this.id)" class="clr-radio"/>
									<label for="planningBundle" class="clr-control-label">Planning Bundles</label>
								</div>
								<div class="clr-radio-wrapper">
									<input type="radio" name="bundles" id="allBundles" value="allBundles" onChange="changeBundles(this.id)" class="clr-radio"/>
									<label for="allBundles" class="clr-control-label">All Bundles</label>
								</div>
							</div>
						</div>
					</div>

					<div class="modal-footer">
						<input type="button" class="btn btn-outiline" id ="saveBundleId" name="saveBundle"  value= "Assign" onclick="submitMoveForm()">
					</div>
				</g:form>
			</div>
		</div>
		<div id="statusColors"  title="Dependency Group Status" style="display: none" class="static-dialog tds-modal-content">
			<div class="modal-header">
				<button class="btn btn-icon close-button" onclick="$('#statusColors').dialog('close')">
					<i class="fas fa-times"></i>
				</button>

				<div class="modal-title-container">
					<div class="modal-title" style="padding: unset;">Dependency Group Status</div>
				</div>
			</div>

			<div class="modal-body">
				<div class="box-header" style="background-color: yellow;">
					<h3 class="box-title">Conflicts</h3>
				</div>
				<div class="box-body">
					Yellow indicates that there is some conflict(s) with assets in the group. The conflict may be that two or more assets are assigned to different events or one or more assets' dependency status is set to Questioned or Unknown.
				</div>
				<div class="box-header" style="background-color: #e0e0e0;">
					<h3 class="box-title">Pending</h3>
				</div>
				<div class="box-body">
					Grey indicates that there are no outstanding conflicts but the Asset Validation needs to be set to Plan Ready indicating the group is ready to be assigned.
				</div>
				<div class="box-header" style="background-color: lightgreen;">
					<h3 class="box-title">Ready to Assign</h3>
				</div>
				<div class="box-body">
					Green indicates that the assets in the group are Ready to be assigned.
				</div>
				<div class="box-header" style="background-color: #5f9fcf;">
					<h3 class="box-title">Assigned</h3>
				</div>
				<div class="box-body">
					Blue indicates that there are no conflicts and all of the assets in the group have been assigned correctly.
				</div>
			</div>
		</div>
		<div style="float:left; width:100%">
			<div id="items1" style="display: none"></div>
			<div id="spinnerDivId" class="containsSpinner" style="display: none"></div>
			<g:render template="/assetEntity/modelDialog" />
			<g:render template="/assetEntity/entityCrudDivs" />
			<div id="createStaffDialog" style="display:none;" class="static-dialog">
				<g:render template="/person/createStaff" model="['forWhom':'application']"></g:render>
			</div>
			<div style="display: none;">
				<g:select id="moveBundleList_all" name="moveBundleList_all" from="${allMoveBundles}" optionKey="id" optionValue="name"  noSelection="${['':'Please Select']}"></g:select><br></br>
				<g:select id="moveBundleList_planning" name="moveBundleList_planning" from="${moveBundle}" optionKey="id" optionValue="name" noSelection="${['':'Please Select']}"></g:select><br></br>
			</div>
			<g:render template="/assetEntity/dependentAdd" />
			<g:render template="/assetEntity/initAssetEntityData"/>
			<g:render template="/layouts/error"/>
		</div>
		</div>
		<script type="text/javascript">
			// Used to track ajax requests and abort them when needed
			var ajaxRequest;
			// Used to keep track of Color By on the dependency map
			var currentColorBy;
			var currentValue,currentdependencyBundle;

			$(document).on('entityAssetUpdated',function (e,obj) {
				$('#refreshButtonId').prop('title', 'Refresh to reflect pending changes');
				$('#refreshButtonId').addClass('warning-change');
			});

			function getList(value, dependencyBundle, force, distance, labels) {
				$('#refreshButtonId').removeClass('warning-change');
				$('#refreshButtonId').prop('title','Refreshes the graph');
				$('#moveBundleSelectId').dialog("close")
				var id = 'all'
				if(dependencyBundle != null) id = dependencyBundle
				currentValue=value;
				currentdependencyBundle = dependencyBundle;
				$('.depGroupSelected').removeClass('depGroupSelected').removeClass('selectedGroup')
				$('.app_count').removeClass('app_count').removeClass('selectedGroup')
				$('.server_count').removeClass('server_count').removeClass('selectedGroup')
				$('.vm_count').removeClass('vm_count').removeClass('selectedGroup')
				$('.db_count').removeClass('db_count').removeClass('selectedGroup')
				$('.file_count').removeClass('file_count').removeClass('selectedGroup')

				$('#span_'+id).addClass('depGroupSelected').addClass('selectedGroup')
				$('#app_'+id).addClass('app_count').addClass('selectedGroup')
				$('#server_'+id).addClass('server_count').addClass('selectedGroup')
				$('#vm_'+id).addClass('vm_count').addClass('selectedGroup')
				$('#db_'+id).addClass('db_count').addClass('selectedGroup')
				$('#file_'+id).addClass('file_count').addClass('selectedGroup')

				$('.tabs li').removeClass('active');
				var fullscreen = GraphUtil.isFullscreen();

				// abort the last ajax request if it still hasn't completed
				if (ajaxRequest)
					ajaxRequest.abort();

				switch (value) {
					case "server" :
						$('#serverli').addClass('active');
						break;
					case "apps" :
						$('#appli').addClass('active');
						break;
					case "database" :
						$('#dbli').addClass('active');
						break;
					case "files" :
						$('#fileli').addClass('active');
						break;
					case "all" :
						$('#allli').addClass('active');
						break;
					case "graph" :
						var labelsList = "Application"
						var bundle = $("#planningBundleSelectId").val()
						var showControls = GraphUtil.getOpenPanel();
						compressList();
						ajaxRequest = ${remoteFunction(
							controller:'assetEntity',
							action:'retrieveLists',
							params:'"entity=" + value + "&dependencyBundle=" + dependencyBundle + "&showControls=" + showControls + "&bundle=" + bundle + "&fullscreen=" + fullscreen',
							onComplete:'listUpdate(XMLHttpRequest)'
						)}
						$('#graphli').addClass('active');
						break;
				}

				var spinnerDiv = $('#spinnerDivId').clone().css('display','block');
				if (value != "graph") {
					GraphUtil.disableFullscreen();
					$('#assetCheck').attr('checked','false');
					var bundle = $("#planningBundleSelectId").val()
					ajaxRequest = ${remoteFunction(controller:'assetEntity', action:'retrieveLists', params:'"entity=" + value + "&dependencyBundle=" + dependencyBundle + "&bundle=" + bundle', onComplete:'listUpdate(XMLHttpRequest)') }
					if ($('#svgContainerId').size() == 0) {
						if ($('#items1 .tabInner div table').size() > 0)
							spinnerDiv.css('width', $('#items1 .tabInner div table').innerWidth() - 10);
						else if ($('#items1 .tabInner #spinnerDivId').size() > 0)
							spinnerDiv.css('width', $('#items1 .tabInner #spinnerDivId').width());
					}
					$('#items1 .tabInner').html(spinnerDiv);
				} else {

					var svgElement = $('#svgContainerId');
					if (svgElement.size() > 0) {
						var leftPosition = svgElement.offset().left,
							topPosition = svgElement.offset().top;

						if (fullscreen) {
							spinnerDiv
								.css('border', 'none')
								.css('background-color', '#ffffff');
							leftPosition = $('#toolsContainerId').offset().left;
							topPosition = $('#toolsContainerId').offset().top;
						}
						svgElement.css('opacity', 0);
						spinnerDiv.addClass('graph')
							.css('position', 'fixed')
							.css('left', leftPosition + 'px')
							.css('top', topPosition + 'px')
							.css('width', svgElement.innerWidth() - 2 + 'px')
							.css('height', svgElement.innerHeight() + 'px');
						$('#item1').append(spinnerDiv);
					} else {
						$('#items1 .tabInner').html(spinnerDiv);
					}
				}

			}

			function openStatusColors() {
				$("#statusColors").dialog({ autoOpen: false, resizable: false });
				$("#statusColors").dialog('option', 'width', '480px');
				$("#statusColors").dialog('option', 'modal', 'true');
				$("#statusColors").dialog('option', 'position', ['center', 60]);
				$("#statusColors").dialog('open');
				// $('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
				$('div.ui-dialog-titlebar').hide();
				
				$("#statusColors").show();

				$("#btnCloseStatusColors").click(function () {
					$("#statusColors").dialog('close');
				});
			}

			function listUpdate(e){
				var resp = e.responseText;
				$('#items1').html(resp);
				$('#items1').css('display','block');
				ajaxRequest = null;
				setTimeout( function () {
					recompileDOM('tmHighlighttAssetSelector');
				});
			}
			function fillView(e){
				var data = e.responseText
				$('#item1').html(data)
			}
			function changeBundles(id){
				var bundle = $('#bundleSession').val()
				if (id=="allBundles") {
					$("#plannedMoveBundleList").html($("#moveBundleList_all").html())
					$("#plannedMoveBundleList").val(bundle);
					changeBundleSelect();
				} else {
					$("#plannedMoveBundleList").html($("#moveBundleList_planning").html())
					$("#plannedMoveBundleList").val(bundle);
					changeBundleSelect();
				}
			}
			function compressList() {
				var objectString = ''
				if ($('#listCheckId').size() > 0) {
					var list = listCheck()
					for (prop in list)
						objectString += "&"+prop+"="+list[prop]
				}
				return objectString
			}
			function getListBySort(value,dependencyBundle,sort){
				var bundle = $("#planningBundleSelectId").val()
				var sortBy = $("#sortBy").val()
				var orderBy = $("#orderBy").val() != 'asc' ? 'asc' : 'desc'
				orderBy = (sortBy == sort) ? orderBy : 'asc'

				// abort the last ajax request if it still hasn't completed
				if (ajaxRequest)
					ajaxRequest.abort();

				ajaxRequest = ${remoteFunction(controller:'assetEntity', action:'retrieveLists', params:'"entity=" + value + "&dependencyBundle=" + dependencyBundle + "&bundle=" + bundle + "&sort=" + sort + "&orderBy=" + orderBy', onComplete:'listUpdate(XMLHttpRequest)') }
			}

			function setGroupTablePosition () {
				var tools = $('#toolsContainerId');
				var dependencyDiv = $('#dependencyDivId');
				var toolsWidth = tools.width();
                var dependencyTable = $('#dependencyTableWrapperId');

				if (dependencyDiv.hasClass('floating')) {
					dependencyDiv.css('right', 0);
					dependencyDiv.css('left', toolsWidth + 20);
				}
				else{
					dependencyTable.css('max-width', 'auto');
				}

                $('#dependencyTableWrapperId').css('overflow-x', 'auto');
			}

			$(document).ready(function () {
				// Safari doesn't render correctly svg inline, since the D3 is who is injecting, we preload the values injecting in the DOM.
				$('#graphSVGContainer').append(appSVGShapes.getAll());
				var showTabs = '${showTabs}';
				var assetName = '${assetName}';
				if(showTabs == "true") {
						// TM-8842, show the Dependency Analyzer Map, drilled-in from the group Id
						getList("apps", '${groupId}'); // needed to show the selected groupId in the table, and for the other tabs to work
						switch ('${tabName}') {
								case "apps":
										break; // do nothing, we are already there
							case "map":
										getList("graph", '${groupId}');
										// fix for low networks, retry n times until the values are established
										// completely by the gsp file
										TDSUtilFunctions.retryFunction(
												6,
												1500,
												function () {
													return $('#searchBoxId').length;
												},
												function () {
													$('#searchBoxId').val(assetName);
													GraphUtil.performSearch();
												}
										);

										break;
								case "all":
										getList("all", '${groupId}');
										break;
								case "servers":
										getList("server", '${groupId}');
										break;
								case "databases":
										getList("database", '${groupId}');
										break;
								case "storage":
										getList("files", '${groupId}');
										break;
						}

				}
			});

		</script>

		<script type="text/javascript">

			( function($) {

				$("#checkBoxDiv").dialog({ autoOpen: false, resizable: false })

				$("#moveBundleSelectId").dialog({ autoOpen: false })
				$("#createStaffDialog").dialog({ autoOpen: false })

				currentMenuId = "#assetMenu";
				$(".menu-parent-assets-dependency-analyzer").addClass('active');
				$(".menu-parent-assets").addClass('active');
				setGroupTablePosition();
				$(window).resize(function() {
					setGroupTablePosition();
				});
			})(jQuery);

		</script>
		<div style="display: none;" id="graphSVGContainer"></div>
		<div class="tdsAssetsApp" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets"></div>
	</body>
</html>
