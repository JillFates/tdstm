<%@page import="com.tds.asset.AssetEntity;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<meta name="layout" content="topNav" />
		<title>Dependency Analyzer</title>
		
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'force.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-bootstrap.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-progressbar.css')}" />
		
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="projectStaff.js" />
		<g:render template="../layouts/responsiveAngularResources" />
		<g:javascript src="progressBar.js" />
		
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<g:javascript src="d3/d3.js"/>
		<g:javascript src="svg.js"/>
		<g:javascript src="load.shapes.js"/>
		<g:javascript src="keyevent_constants.js" />
		<g:javascript src="graph.js" />
		<g:javascript src="generator/runtime.js" />
		<g:javascript src="generator/generator.js" />
	</head>
	<body>
		<input type="hidden" id="redirectTo" name="redirectTo" value="dependencyConsole" />
		<div class="body fluid" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
		<div id="DependencyGroupsTableId" style="min-width: 1000px;">
			<div id="dependencyTitle" style="float: left;">
				<h1>Dependency Analyzer</h1>
			</div>
			
			<div id="checkBoxDiv"  title="Dependency Grouping Control" style="display: none" class="static-dialog">
				<div id="checkBoxDivId">
					<g:form name="checkBoxForm">
						<div class="row">
							<div class="col-sm-6">
								<h3>Connection Type</h3>
								<div class="checkboxdiv_control">
									<g:each in="${dependencyType}" var="dependency">
										<input type="checkbox" id="${dependency}"
											   name="connection" value="${dependency}" ${depGrpCrt.connectionTypes ? (depGrpCrt.connectionTypes.contains(dependency) ? 'checked' : '') : ([ 'Batch' ].contains(dependency) ? "" : "checked")}/>&nbsp;&nbsp;
										<span id="dependecy_${dependency}"> ${dependency}</span>
										<br />
									</g:each>
								</div>
							</div>
							<div class="col-sm-6">
								<h3>Connection Status</h3>
								<div class="checkboxdiv_control">
									<g:each in="${dependencyStatus}" var="dependencyStatusInst">
										<input type="checkbox" id="${dependencyStatusInst}"
											   name="status" value="${dependencyStatusInst}" ${depGrpCrt.statusTypes ? (depGrpCrt.statusTypes.contains(dependencyStatusInst) ? 'checked' : '') : (['Archived','Not Applicable'].contains(dependencyStatusInst) ? '' : 'checked')}/>&nbsp;&nbsp;
										<span id="dependecy_${dependencyStatusInst}"> ${dependencyStatusInst} </span>
										<br />
									</g:each>
								</div>
							</div>
						</div>
						<div class="row checkboxdiv_default">
							<div class="col-sm-12"><input type="checkbox" id="saveDefault" name="saveDefault" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>&nbsp;&nbsp; <span>Save as defaults</span></div>
						</div>
						<div class="buttonR">
							<button type="button" class="btn btn-default submit" value="Generate" id="generateId" onclick="submitCheckBox()"><span class="glyphicon glyphicon-stats" aria-hidden="true"></span> Generate</button>
						</div>
					</g:form>
				</div>
			</div>
			
			
			<div style="clear: both;"></div>
			
			<div id = "dependencyBundleDetailsId" >
				<g:render template="dependencyBundleDetails" />
			</div>
			
			<div style="clear: both;"></div>
			
			<div id="moveBundleSelectId" title="Assignment" style="background-color: #808080; display: none; float: right" class="static-dialog">
				<g:form name="changeBundle" action="saveAssetsToBundle" >
						
					<input type="hidden" name="assetVal" id="assetVal" />
					<input type="hidden" name="assetType" id="assetsTypeId"  />
					<input type="hidden" name="bundleSession" id="bundleSession" /> 
					<table style="border: 0px;">
						<tr>
							<td style="color:#EFEFEF ; width: 260px"> <b> Assign selected assets to :</b></td>
						</tr>
						<tr>
							<td>
								<span style="color:#EFEFEF "><b>Bundle</b></span> &nbsp;&nbsp;
								<g:select name="moveBundle" id="plannedMoveBundleList" from="${moveBundle}" optionKey="id" onchange="changeBundleSelect()" noSelection="${['':'Please Select']}"></g:select><br></br>
							</td>
						</tr>
						<tr>
							<td>
								<span style="color:#EFEFEF "><b>Plan Status</b></span> &nbsp;&nbsp;<g:select name="planStatus" id="plannedStatus" from="${planStatusOptions}" optionKey="value" optionValue="value"></g:select><br></br>
							</td>
						</tr>
						<tr>
							<td>
								<div>
									<label for="planningBundle" ><input type="radio" name="bundles" id="planningBundle" value="planningBundle" checked="checked" onChange="changeBundles(this.id)" />&nbsp;<span style="color:#EFEFEF "><b>Planning Bundles</b></span></label>
									<label for="allBundles" ><input type="radio" name="bundles" id="allBundles" value="allBundles" onChange="changeBundles(this.id)" />&nbsp;<span style="color:#EFEFEF "><b>All Bundles</b></span></label><br />
								</div>
							</td>
						</tr>
						<tr>
							<td style="text-align: left"><input type="button" id ="saveBundleId" name="saveBundle"  value= "Assign" onclick="submitMoveForm()"> </td>
						</tr>
					</table>
				</g:form>
			</div>
		</div>
		<div style="float:left;">
			<div id="items1" style="display: none"></div>
			<div id="spinnerDivId" class="containsSpinner" style="display: none"></div>
			<g:render template="../assetEntity/modelDialog" />
			<g:render template="../assetEntity/entityCrudDivs" />
			<div id="createStaffDialog" style="display:none;" class="static-dialog">
				<g:render template="../person/createStaff" model="['forWhom':'application']"></g:render>
			</div>
			<div style="display: none;">
				<g:select id="moveBundleList_all" name="moveBundleList_all" from="${allMoveBundles}" optionKey="id"  noSelection="${['':'Please Select']}"></g:select><br></br>
				<g:select id="moveBundleList_planning" name="moveBundleList_planning" from="${moveBundle}" optionKey="id" noSelection="${['':'Please Select']}"></g:select><br></br>
			</div>
			<g:render template="../assetEntity/dependentAdd" />
			<g:render template="../assetEntity/initAssetEntityData"/>
			<g:render template="../layouts/error"/>
		</div>
		</div>
		<script type="text/javascript">
			// Used to track ajax requests and abort them when needed
			var ajaxRequest;
			// Used to keep track of Color By on the dependency map
			var currentColorBy;
			
			function getList(value,dependencyBundle, force, distance, labels) {
				$('#moveBundleSelectId').dialog("close")
				var id = 'all'
				if(dependencyBundle != null) id = dependencyBundle
				
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
						var showControls = 'hide'
						if ($('#controlPanelId').css('display') == 'block')
							showControls = 'controls'
						if ($('#legendDivId').css('display') == 'block')
							showControls = 'legend'
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
							spinnerDiv.css('background-color', '#ffffff');
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
			function listUpdate(e){
				var resp = e.responseText;
				$('#items1').html(resp);
				$('#items1').css('display','block');
				ajaxRequest = null;
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
			function assignedCheckbox(chkbox) {
				$('#assinedGroup').val(chkbox.checked ? '1' : '0')
				chkbox.form.submit()
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
				var windowWidth = $(window).width();
				var dependencyDiv = $('#dependencyDivId')
				var rightOffset = dependencyDiv.parent().offset().left;
				var leftOffset = dependencyDiv.offset().left;
				var extraOffset = $('#graphToolbarId').outerWidth() ? $('#graphToolbarId').outerWidth() : 0;
				if (dependencyDiv.hasClass('floating'))
					dependencyDiv.css('max-width', (windowWidth - extraOffset - ((leftOffset - extraOffset) * 2)) + 'px');
				else
					dependencyDiv.css('max-width', (windowWidth - rightOffset - leftOffset) + 'px');
			}
			
			$(document).ready(function () {
				// Safari doesn't render correctly svg inline, since the D3 is who is injecting, we preload the values injecting in the DOM.
				$('#graphSVGContainer').append(appSVGShapes.getAll());
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
	</body>
</html>
