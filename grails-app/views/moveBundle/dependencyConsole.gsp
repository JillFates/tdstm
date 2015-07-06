<%@page import="com.tds.asset.AssetEntity;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<meta name="layout" content="projectHeader" />
		<title>Dependency Analyzer</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'force.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-bootstrap.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-progressbar.css')}" />

		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="projectStaff.js" />
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>
		<g:javascript src="angular/plugins/angular-resource.js" />
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
		<g:javascript src="progressBar.js" />
		<g:javascript src="bootstrap.js" />

		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<g:javascript src="d3/d3.min.js"/>
		<g:javascript src="load.shapes.js"/>
		<g:javascript src="graph.js" />
		<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
 
	</head>
	<body>
		<input type="hidden" id="redirectTo" name="redirectTo" value="dependencyConsole" />
		<div class="body fluid" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
		<div id="DependencyGroupsTableId" style="min-width: 1000px;">
			<div id="dependencyTitle" style="float: left;">
				<h1>Dependency Analyzer</h1>
				<div style="position: absolute; margin: -25px 170px 0;">
					<g:link controller="moveBundle" action="dependencyConsole" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary">
						<img src="${resource(dir:'icons',file:'arrow_refresh.png')}" title="Refresh Data">
					</g:link>
				</div>
			</div>
			<tds:hasPermission permission='MoveBundleEditView'>
				<div id="checkBoxDiv"  title="Dependency Grouping Control" style="display: none" class="static-dialog">
					<div id="checkBoxDivId">
						<g:form name="checkBoxForm"> 
							<div style="float: left; margin-left:18px;">
								<fieldset>
									<legend>Connection Type:</legend>
									<g:each in="${dependencyType}" var="dependency">
										<input type="checkbox" id="${dependency}"
											name="connection" value="${dependency}" ${depGrpCrt.connectionTypes ? (depGrpCrt.connectionTypes.contains(dependency) ? 'checked' : '') : ([ 'Batch' ].contains(dependency) ? "" : "checked")}/>&nbsp;&nbsp;
											<span id="dependecy_${dependency}"> ${dependency}</span>
										<br />
									</g:each>
								</fieldset>
							</div>
							&nbsp;
							<div style="float: left;margin-left: 10px;">
								<fieldset>
									<legend>Connection Status:</legend>
									<g:each in="${dependencyStatus}" var="dependencyStatusValue">
										<input type="checkbox" id="${dependencyStatusValue}"
											name="status" value="${dependencyStatusValue}" ${depGrpCrt.statusTypes ? (depGrpCrt.statusTypes.contains(dependencyStatusValue) ? 'checked' : '') : (['Archived','Not Applicable'].contains(dependencyStatusValue) ? '' : 'checked')}/>&nbsp;&nbsp;
											<span id="dependecy_${dependencyStatusValue}"> ${dependencyStatusValue} </span>
										<br />
									</g:each>
								</fieldset>
								<input type="checkbox" id="saveDefault" name="saveDefault" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
										&nbsp;&nbsp; <span>Save as defaults</span>
							</div>
							<div class="buttonR">
								<input type="button" class="submit" style="margin-top: 40px; margin-left: 10px;" value="Generate" onclick="submitCheckBox()" />
							</div>
							
						</g:form>
					</div> 
				</div>
			</tds:hasPermission>

			<div style="clear: both;"></div>
			<tds:hasPermission permission='MoveBundleEditView'>
				<div id = "dependencyBundleDetailsId" >
					<g:render template="dependencyBundleDetails" />
				</div>
			</tds:hasPermission>
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
			<tds:hasPermission permission='MoveBundleEditView'>
				<div id="items1" style="display: none"></div>
				<div id="spinnerDivId" style="display: none"></div>
			</tds:hasPermission>
			<g:render template="../assetEntity/modelDialog" />
			<g:render template="../assetEntity/entityCrudDivs" />
			<div id="createStaffDialog" style="display:none;">
				<g:render template="../person/createStaff" model="['forWhom':'application']"></g:render>
			</div>
			<div style="display: none;">
			  <g:select id="moveBundleList_all" name="moveBundleList_all" from="${allMoveBundles}" optionKey="id"  noSelection="${['':'Please Select']}"></g:select><br></br>
			  <g:select id="moveBundleList_planning" name="moveBundleList_planning" from="${moveBundle}" optionKey="id" noSelection="${['':'Please Select']}"></g:select><br></br>
			</div>
			<g:render template="../assetEntity/dependentAdd" />
			<g:render template="../assetEntity/initAssetEntityData"/>
		</div>
		</div>
		<script type="text/javascript">
			// Used to track ajax requests and abort them when needed
			var ajaxRequest;
			
			function getList(value,dependencyBundle, force, distance, labels) {
				$('#moveBundleSelectId').dialog("close")
				var id = 'all'
				if(dependencyBundle != null) id = dependencyBundle
				
				$('.depGroupSelected').removeClass('depGroupSelected')
				$('.app_count').removeClass('app_count')
				$('.server_count').removeClass('server_count')
				$('.vm_count').removeClass('vm_count')
				$('.db_count').removeClass('db_count')
				$('.file_count').removeClass('file_count')
				
				$('#span_'+id).addClass('depGroupSelected')
				$('#app_'+id).addClass('app_count')
				$('#server_'+id).addClass('server_count')
				$('#vm_'+id).addClass('vm_count')
				$('#db_'+id).addClass('db_count')
				$('#file_'+id).addClass('file_count')

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
						if ($('#controlPanel').css('display') == 'block')
							showControls = 'controls'
						if ($('#legendDivId').css('display') == 'block')
							showControls = 'legend'
						compressList();
						ajaxRequest = ${remoteFunction(controller:'assetEntity', action:'retrieveLists', params:'"entity=" + value + "&dependencyBundle=" + dependencyBundle + "&showControls=" + showControls + "&bundle=" + bundle + "&fullscreen=" + fullscreen', onComplete:'listUpdate(XMLHttpRequest)') }
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
						if (fullscreen) {
							svgElement = $('#svgContainerId').children('svg');
							spinnerDiv.css('background-color', '#ffffff');
						}
						svgElement.css('opacity', 0);
						spinnerDiv.addClass('graph')
							.css('position', 'fixed')
							.css('left', svgElement.offset().left + 'px')
							.css('top', svgElement.offset().top + 'px')
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
				$('#items1').html(resp)
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
			function compactControlPref($me){
				var isChecked= $me.is(":checked")
				jQuery.ajax({
			        url:contextPath+'/moveBundle/setCompactControlPref',
			        data:{'selected':isChecked, 'prefFor':'depConsoleCompact'},
			        type:'POST',
					success: function(data) {
						compactDivToggle(data);
					}
			    });
			}
			function compactDivToggle (data) {
				if (data == 'true')
					$(".compactClass").hide();
				else
					$(".compactClass").show();
				GraphUtil.resetGraphSize();
			}
			
			function setGroupTablePosition () {
				var windowWidth = $(window).width();
				var rightOffset = $('#dependencyDivId').parent().offset().left;
				var leftOffset = $('#dependencyDivId').offset().left;
				var extraOffset = $('#mapReferenceId').outerWidth();
				$("#dependencyDivId:not(.floating)").css('max-width', (windowWidth - rightOffset - leftOffset) + 'px');
				$("#dependencyDivId.floating").css('max-width', (windowWidth - extraOffset - (rightOffset * 2)) + 'px');
			}
		</script>

		<script type="text/javascript">
			
			( function($) {
			
				var compactPref= '${compactPref}'
				compactDivToggle(compactPref);
				
				// ${remoteFunction(controller:'assetEntity', action:'retrieveLists', params:'\'entity=\' + "apps" +\'&dependencyBundle=\'+ null', onComplete:'listUpdate(XMLHttpRequest)') }
				$("#checkBoxDiv").dialog({ autoOpen: false, resizable: false })

				$("#moveBundleSelectId").dialog({ autoOpen: false })
				$("#createStaffDialog").dialog({ autoOpen: false })

				currentMenuId = "#assetMenu";
				$("#assetMenuId a").css('background-color','#003366')
				setGroupTablePosition();
				$(window).resize(function() {
					setGroupTablePosition();
				});
			})(jQuery);		
			
		</script>

	</body>
</html>
