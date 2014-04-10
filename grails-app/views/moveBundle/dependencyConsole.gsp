<%@page import="com.tds.asset.AssetEntity;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<meta name="layout" content="projectHeader" />
		<title>Dependency Analyzer</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="projectStaff.js" />
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<script type="text/javascript" src="${resource(dir:'d3',file:'d3.js')} "></script>
		<link rel="stylesheet" href="${resource(dir:'d3/force',file:'force.css')}" type="text/css"/>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		
		<script type="text/javascript">
			// This variable must remain outside the scope of the rendered dependency map
			// to ensure that only one d3 force layout can be running at a time
			var force
			
			$(document).ready(function() {
				var compactPref= '${compactPref}'
				compactDivToggle(compactPref);
				
				// ${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + "apps" +\'&dependencyBundle=\'+ null', onComplete:'listUpdate(e)') }
				$("#checkBoxDiv").dialog({ autoOpen: false, resizable: false })
				$("#createEntityView").dialog({ autoOpen: false })
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false })
				$("#commentsListDialog").dialog({ autoOpen: false })
				$("#createCommentDialog").dialog({ autoOpen: false })
				$("#showCommentDialog").dialog({ autoOpen: false })
				$("#editCommentDialog").dialog({ autoOpen: false })
				$("#manufacturerShowDialog").dialog({ autoOpen: false })
				$("#modelShowDialog").dialog({ autoOpen: false })
				$("#moveBundleSelectId").dialog({ autoOpen: false })
				$("#editManufacturerView").dialog({ autoOpen: false})
				$("#createStaffDialog").dialog({ autoOpen: false })
				$("#cablingDialogId").dialog({ autoOpen:false })
				currentMenuId = "#assetMenu";
				$("#assetMenuId a").css('background-color','#003366')	
				$("#dependencyDivId").css('max-width', ($(window).width()-185)+'px');
				$(window).resize(function() {
					$("#dependencyDivId").css('max-width', ($(window).width()-185)+'px');
				});
			});
		</script>
	</head>
	<body>
		<input type="hidden" id="redirectTo" name="redirectTo" value="dependencyConsole" />
		<div class="body">
		<div style="min-width: 1000px;">
			<div id="dependencyTitle" style="float: left;">
				<h1>Dependency Analyzer</h1>
				<div style="position:absolute;margin: -25px 176px 0;">
					<input type="checkbox" id="compactControl" name="compactControl" ${compactPref == 'true' ? 'checked="checked"' :''} onclick="compactControlPref( $(this) )"/>
					&nbsp;<span> Compact Control </span>
				</div>
			</div>
			<tds:hasPermission permission='MoveBundleEditView'>
				<div id="checkBoxDiv"  title="Dependency Grouping Control" style="display: none">
					<div id="checkBoxDivId">
						<g:form name="checkBoxForm"> 
							<div style="float: left; margin-left:18px;">
							   <fieldset>
								  <legend>Connection Type:</legend>
									<g:each in="${dependencyType}" var="dependency">
										<input type="checkbox" id="${dependency.value}"
											name="connection" value="${dependency.value}" ${depGrpCrt.connectionTypes ? (depGrpCrt.connectionTypes.contains(dependency.value) ? 'checked' : '') : ([ 'Batch' ].contains(dependency.value) ? "" : "checked")}/>&nbsp;&nbsp;
											<span id="dependecy_${dependency.id}"> ${dependency.value}</span>
										<br />
									</g:each>
								</fieldset>
							</div>
							&nbsp;
							<div style="float: left;margin-left: 10px;">
								<fieldset>
									<legend>Connection Status:</legend>
									<g:each in="${dependencyStatus}" var="dependencyStatus">
										<input type="checkbox" id="${dependencyStatus.value}"
											name="status" value="${dependencyStatus.value}" ${depGrpCrt.statusTypes ? (depGrpCrt.statusTypes.contains(dependencyStatus.value) ? 'checked' : '') : (['Archived','Not Applicable'].contains(dependencyStatus.value) ? '' : 'checked')}/>&nbsp;&nbsp;
											<span id="dependecy_${dependencyStatus.id}"> ${dependencyStatus.value} </span>
										<br />
									</g:each>
								</fieldset>
								<input type="checkbox" id="saveDefault" name="saveDefault" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
										&nbsp;&nbsp; <span>Save as defaults</span>
							</div>
							<div class="buttonR">
								<input type="button" class="submit"
									style="margin-top: 40px; margin-left: 10px;" value="Generate" onclick="submitCheckBox()" />
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
			
			<div id="moveBundleSelectId" title="Assignment" style="background-color: #808080; display: none; float: right" >
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
			</tds:hasPermission>
			<g:render template="../assetEntity/commentCrud" />
			<g:render template="../assetEntity/modelDialog" />
			<div id="createEntityView" style="display: none;"></div>
			<div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>
			<div id="editManufacturerView" style="display: none;"></div>
			<div id="createStaffDialog" style="display:none;">'
			<div id="cablingDialogId" style="display: none;"></div>
				<g:render template="../person/createStaff" model="['forWhom':'application']"></g:render>
			</div>
			<div style="display: none;">
			  <g:select id="moveBundleList_all" from="${allMoveBundles}" optionKey="id"  noSelection="${['':'Please Select']}"></g:select><br></br>
			  <g:select id="moveBundleList_planning" from="${moveBundle}" optionKey="id" noSelection="${['':'Please Select']}"></g:select><br></br>
			</div>
			<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
		</div>
		</div>
		<script type="text/javascript">
			function getList(value,dependencyBundle, force, distance, labels){
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
				
				switch(value){
					case "server" :
						$('#assetCheck').attr('checked','false')
					case "apps" :
					case "database" :
					case "files" :
						var bundle = $("#planningBundleSelectId").val()
						${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + value +\'&dependencyBundle=\'+ dependencyBundle+\'&bundle=\'+ bundle', onComplete:'listUpdate(e)') }
						break
					case "graph" :
						var labelsList = "Application"
						var blackBackground = null
						var bundle = $("#planningBundleSelectId").val()
						var showControls = 'hide'
						if ($('#controlPanel').css('display') == 'block')
							showControls = 'controls'
						if ($('#legendDivId').css('display') == 'block')
							showControls = 'legend'
						if ($('#blackBackgroundId').is(':checked'))
							blackBackground = true
						else if ($('#blackBackgroundId').is(':not(:checked)'))
							blackBackground = false
						compressList()
						${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + value +\'&dependencyBundle=\'+ dependencyBundle+\'&force=\'+ force+\'&distance=\'+ distance + compressList() + \'&showControls=\'+ showControls + \'&blackBackground=\'+ blackBackground+\'&bundle=\'+ bundle', onComplete:'listUpdate(e)') }
						break
				}
			}
			function listUpdate(e){
				var resp = e.responseText;
				$('#items1').html(resp)
				$('#items1').css('display','block');
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
				console.log(sort)
				var bundle = $("#planningBundleSelectId").val()
				var sortBy = $("#sortBy").val()
				var orderBy = $("#orderBy").val() != 'asc' ? 'asc' : 'desc'
				orderBy = (sortBy == sort) ? orderBy : 'asc'
				${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + value +\'&dependencyBundle=\'+ dependencyBundle+\'&bundle=\'+ bundle+\'&sort=\'+ sort+\'&orderBy=\'+ orderBy', onComplete:'listUpdate(e)') }
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
			function compactDivToggle(data){
				if(data=='true')
					$(".compactClass").hide();
				else
					$(".compactClass").show();
			}
		</script>
	</body>
</html>
