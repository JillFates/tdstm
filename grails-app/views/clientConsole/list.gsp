<%@page import="com.tds.asset.AssetCableMap;com.tds.asset.AssetDependency;com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;com.tds.asset.AssetComment"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset Tracker</title>

<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'dashboard.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tableTheme.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />

<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<g:javascript src="model.manufacturer.js"/>	

<style type="text/css" media="screen">
	.tranCell {
		width: 9px !important;
	}
</style>

</head>
<body >
<div title="Change Status" id="changeStatusDialog" style="background-color: #808080;display: none;">
	<form name="changeStatusForm">
	<input type="hidden" name="asset" id="assetIds"  />
	<input type="hidden" name="projectId" id="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" id="moveBundleIds"  />
<table style="border: 0px; width: 100%">
	<tr>
		<td width="40%"><strong>Change status for selected devices to:</strong></td>
		<td width="60%"></td>
	</tr>
	<tr>
		<td><select id="taskList" name="taskList" style="width: 250%"></select></td>
	</tr>
	<tr>
		<td>
		<textarea rows="2" cols="1"  title="Enter Note..." name="enterNote" id="enterNote" style="width: 200%" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
		</td>
	</tr>
	<tr>
		<td></td>
		<td style="text-align: right;"><input type="button" value="Save"
			onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)submitAction()" /></td>
	</tr>
</table>
</form>
</div>
<div style="width:100%">
<g:form	name="listForm" action="list" method="post">
<div id="consoleHeader" style="width: 100%;">
	
	<input type="hidden" id="role" value="${role}"/>
	<input type="hidden" id="lastPoolTimeId" value="${lastPoolTime}"/>
	<input type="hidden" id="projectId" name="projectId" value="${projectId }" />
	<table style="border: 0px;">
		<tr>
			<td>
				<span style="padding-left: 10px;">
					<label for="moveEvent"><b>Event:</b></label>&nbsp;
					<select id="moveEventId" name="moveEvent" onchange="changeMoveBundle(this.value)">
						<g:each status="i" in="${moveEventsList}" var="moveEventInstance">
							<option value="${moveEventInstance?.id}">${moveEventInstance?.name}</option>
						</g:each>
					</select>
				</span>
				
				
					<label for="moveBundle"><b>Bundle:</b></label>&nbsp;
					<span id="jsonReplace">
					<select id="moveBundleId" name="moveBundle" onchange="document.listForm.submit()" >
					    <g:if test="${showAllOption}">
						  <option value="all">All</option>
						</g:if>
						<g:each status="i" in="${moveBundleList}" var="moveBundleInstance">
							<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
						</g:each>
					</select>
				</span>
				<span>
					<input id="assetsInViewOffsetId" type="hidden" value="${params.offset}">
					<input id="sortById" type="hidden" value="${params.sort}">
					<input id="orderById" type="hidden" value="${params.order}">
					<label for="assetsInViewId"><b>Assets:</b></label>&nbsp;
					<select id="assetsInViewId" name="assetsInView" onchange="document.listForm.submit()" >
						<option value="all">All</option>
						<option value="25">25</option>
						<option value="50">50</option>
						<option value="100">100</option>
					</select>
				</span>
				<g:if test="${totalAssets > assetsInView }">
					<br/><br/>
					<span class="pmo_paginateButtons">
							<g:paginate total="${totalAssets}" params="${params }"/>
					</span>
				</g:if>				
			</td>
			<td style="padding: 0px;"><h1>Asset Tracker</h1></td>
			<g:if test="${clientConsoleBulkEditHasPermission}">
			<td style="text-align: left;width: 400px;">
				<span>
					<input type="button" name="bulkEdit" id="bulkEditId" value="Bulk Edit" class="bulkedit_inactive" onclick="performBulkEdit()"/>
				</span>
				&nbsp;&nbsp;
				<span style="display: none;" id="bulkTaskSpanId">
					<input type="button" name="bulkNa" id="bulkNaId" value="N/A" onclick="changeAction('NA')"/>
					<input type="button" name="bulkPending" id="bulkPendingId" value="Pending" onclick="changeAction('pending')"/>
					<input type="button" name="bulkDone" id="bulkDoneId" value="Done" onclick="changeAction('done')"/>
					<input type="button" name="bulkUndo" id="bulkUndoId" value="Undo" onclick="changeAction('void')"/>
					<input type="hidden" name="bulkAction" id="bulkActionId" value="done"/>
				</span>
			</td>
			</g:if>
			
			<td style="text-align: right;">
			<input type="hidden" name="last_update" value="${new Date()}"/>
			<input type="hidden" name="myForm" value="listForm"/>
			<input type="button" id="updateId"
				value="Update:" onclick="pageReload();"/> <select
				id="selectTimedId"
				onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'setUpdateTime(e)') }">
				<option value="30000">30s</option>
				<option value="60000">1m</option>
				<option value="120000">2m</option>
				<option value="300000">5m</option>
				<option value="600000">10m</option>
				<option value="never">Never</option>
			</select></td>
		</tr>
	</table>

</div>
<g:if test="${browserTest}">
<div id="tableContainer" class="tableContainerIE" style="margin-left: 5px">
</g:if>
<g:else>
<div id="tableContainer" class="tableContainer" style="margin-left: 5px">
</g:else>

<table cellpadding="0" cellspacing="0" style="border:0px;">
	<thead>
		<g:form action="list">
		<tr>
			<th style="padding-top:35px;">
				<span>Actions</span><br />
				<g:if test="${clientConsoleCheckBoxHasPermission}"> 
					<input type="button" value="State..." onclick="changeState()" title="Change State" style="width: 80px;"/><br />
					<a href="#" onclick="selectAll()" style="color:blue;text-decoration: underline;">All</a>
				</g:if>
			</th>
			
			<th style="padding-top:35px;" >
				<tds:sortableLink id="column1Label" style="border:0px;" property="${columns?.column1.field}"  title="${columns?.column1.label}" params="['projectId':projectId, moveEvent:moveEventInstance?.id, 'moveBundle':moveBundleInstance?.id,'column1':column1Value,'column2':column2Value,'column3':column3Value,'column4':column4Value, 'assetsInView':assetsInView, 'offset':params.offset]"/>
				<span id="column1Select" style="display: none;"><g:select from="${attributesList}" optionKey="attributeCode" optionValue="frontendLabel" name="column1Attribute"  value="${columns?.column1.field}"></g:select></span>
				<span id="column1Edit"><img src="${resource(dir:'i',file:'db_edit.png')}" border="0px" onclick="changeLabelToSelect()"/></span>
				<span id="column1Save" style="display: none;"><input type="submit" value="Save"/>&nbsp;<input type="button" value="X" onclick="changeToLabel('1')"/></span> 
				<br />
					
				<select id="column1Id" name="column1" onchange="document.listForm.submit();" style="width: 120px;">
					<option value="" selected="selected">All</option>
					<g:each in="${column1List}" var="column1Obj">
						<option value="${column1Obj.id ? column1Obj.id : 'blank'}">${column1Obj.key ? column1Obj.key : 'blank'}&nbsp;(${column1Obj.value})</option>
					</g:each>
				</select>
			</th>
			<th style="padding-top:35px;">
				<tds:sortableLink id="column2Label" style="border:0px;" property="${columns?.column2.field}"  title="${columns?.column2.label}" params="['projectId':projectId,moveEvent:moveEventInstance?.id, 'moveBundle':moveBundleInstance?.id, 'column1':column1Value,'column2':column2Value,'column3':column3Value,'column4':column4Value, 'assetsInView':assetsInView, 'offset':params.offset]" />
				<span id="column2Select" style="display: none;"><g:select from="${attributesList}" optionKey="attributeCode" optionValue="frontendLabel" name="column2Attribute" value="${columns?.column2.field}"></g:select></span>
				<span id="column2Edit"><img src="${resource(dir:'i',file:'db_edit.png')}" border="0px" onclick="changeLabelToSelect()"/></span>
				<span id="column2Save" style="display: none;"><input type="submit" value="Save"/>&nbsp;<input type="button" value="X" onclick="changeToLabel('2')"/></span> 
				<br />

				<select id="column2Id" name="column2"	onchange="document.listForm.submit();" style="width: 120px;">
					<option value="" selected="selected">All</option>
					<g:each in="${column2List}" var="column2Obj">
						<option value="${column2Obj.id ? column2Obj.id : 'blank'}">${column2Obj.key ? column2Obj.key : 'blank'}&nbsp;(${column2Obj.value})</option>	
					</g:each>
				</select>
			</th>
			<th style="padding-top:35px;">
				<tds:sortableLink id="column3Label" style="border:0px;" property="${columns?.column3.field}"  title="${columns?.column3.label}" params="['projectId':projectId, moveEvent:moveEventInstance?.id, 'moveBundle':moveBundleInstance?.id, 'column1':column1Value,'column2':column2Value,'column3':column3Value,'column4':column4Value, 'assetsInView':assetsInView, 'offset':params.offset]"/>
				<span id="column3Select" style="display: none;"><g:select from="${attributesList}" optionKey="attributeCode" optionValue="frontendLabel" name="column3Attribute" value="${columns?.column3.field}" ></g:select></span>
				<span id="column3Edit"><img src="${resource(dir:'i',file:'db_edit.png')}" border="0px" onclick="changeLabelToSelect()"/></span>
				<span id="column3Save" style="display: none;"><input type="submit" value="Save"/>&nbsp;<input type="button" value="X" onclick="changeToLabel('3')"/></span> 
				<br />

				<select id="column3Id" name="column3" onchange="document.listForm.submit();" style="width: 120px;">
					<option value="" selected="selected">All</option>
					<g:each in="${column3List}" var="column3Obj">
						<option value="${column3Obj.id ? column3Obj.id : 'blank'}">${column3Obj.key ? column3Obj.key : 'blank'}&nbsp;(${column3Obj.value})</option>	
					</g:each>
				</select>
			</th>
			<th style="padding-top:35px;">
				<tds:sortableLink id="column4Label" style="border:0px;" property="${columns?.column4.field}"  title="${columns?.column4.label}" params="['projectId':projectId, moveEvent:moveEventInstance?.id, 'moveBundle':moveBundleInstance?.id, 'column1':column1Value,'column2':column2Value,'column3':column3Value,'column4':column4Value, 'assetsInView':assetsInView, 'offset':params.offset]"/>
				<span id="column4Select" style="display: none;"><g:select from="${attributesList}" optionKey="attributeCode" optionValue="frontendLabel" name="column4Attribute" value="${columns?.column4.field}" ></g:select></span>
				<span id="column4Edit"><img src="${resource(dir:'i',file:'db_edit.png')}" border="0px" onclick="changeLabelToSelect()"/></span>
				<span id="column4Save" style="display: none;"><input type="submit" value="Save"/>&nbsp;<input type="button" value="X" onclick="changeToLabel('4')"/></span> 
				<br />

				<select id="column4Id" name="column4" onchange="document.listForm.submit();" style="width: 120px;">
					<option value="" selected="selected">All</option>
					<g:each in="${column4List}" var="column4Obj">
						<option value="${column4Obj.id ? column4Obj.id : 'blank'}">${column4Obj.key ? column4Obj.key : 'blank'}&nbsp;(${column4Obj.value})</option>	
					</g:each>
				</select>
			</th>
			<g:sortableColumn property="updated" title="Updated" params="['projectId':projectId]" />

			<g:if test="${browserTest}">
			<g:each in="${processTransitionList}"  var="task">
				<th class="verticaltext" title="${task.header}" style="color: ${task.fillColor}" onclick="bulkTransitionsByHeader('${task.transId}')">${task?.header}</th>
			</g:each>
			</g:if>
			<g:else>
			<th style="padding-left: 0px; height: 102px" colspan="${headerCount}"><embed src="${resource(dir:'templates',file:'headerSvg_'+bundleId+'.svg')}" type="image/svg+xml" width="${headerCount*21.80}" height="102px"/></th>
			</g:else>
		</tr>
	</g:form>
	</thead>
	<tbody id="assetListTbody" onclick="catchevent(event)">
		<g:if test="${assetEntityList}">
		<g:each in="${assetEntityList}" var="assetEntity">
			<tr id="assetRow_${assetEntity.id}">
			<td>
			<g:if test="${clientConsoleCheckBoxHasPermission}">
			<span id="action_${assetEntity.id}">
				<g:checkBox name="checkChange" id="checkId_${assetEntity.id}" onclick="timedUpdate('never')"></g:checkBox>
			</span>
			</g:if>
			<img id="asset_${assetEntity.id}" src="${resource(dir:'images',file:'asset_view.png')}" border="0px" />
			<span id="icon_${assetEntity.id}">
				<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+assetEntity.id+' and commentType = ? and isResolved = ?',['issue',0])}">
						<img id="comment_${assetEntity.id}" src="${resource(dir:'i',file:'db_table_red.png')}" border="0px" />
				</g:if>
				<g:else>
					<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+assetEntity.id)}">
						<img id="comment_${assetEntity.id}" src="${resource(dir:'i',file:'db_table_bold.png')}" border="0px" />
					</g:if>
					<g:else>
					<g:if test="${clientConsoleCommentHasPermission}">
						<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" onclick="createNewAssetComment(${assetEntity.id},'${assetEntity.asset.assetName}','${assetEntity.asset.assetType}');"/>
					</g:if>
					</g:else>
			</g:else>
			</span>
			</td>
			<td  id="${assetEntity.id}_column1">
				<g:if test="${columns?.column1.field != 'currentStatus'}">
					${assetEntity.asset[columns?.column1.field]}&nbsp;
				</g:if>
				<g:else>
					${assetEntity.currentStatus}&nbsp;
				</g:else>
			</td>
			<td id="${assetEntity.id}_column2">
				<g:if test="${columns?.column2.field != 'currentStatus'}">
					${assetEntity.asset[columns?.column2.field]}&nbsp;
				</g:if>
				<g:else>
					${assetEntity.currentStatus}&nbsp;
				</g:else>
			</td>
			<td id="${assetEntity.id}_column3">
				<g:if test="${columns?.column3.field != 'currentStatus'}">
					${assetEntity.asset[columns?.column3.field]}&nbsp;
				</g:if>
				<g:else>
					${assetEntity.currentStatus}&nbsp;
				</g:else>
			</td>
			<td id="${assetEntity.id}_column4">
				<g:if test="${columns?.column4.field != 'currentStatus'}">
					${assetEntity.asset[columns?.column4.field]}&nbsp;
				</g:if>
				<g:else>
					${assetEntity.currentStatus}&nbsp;
				</g:else>
			</td>
			<td id="${assetEntity.id}_column5" nowrap="nowrap">
			<tds:convertDateTime date="${assetEntity.asset.updated}" formate="hh:mm"
							  		timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
			  
			</td>
			<g:each in="${assetEntity.transitions}" var="transition">${transition}</g:each>
			</tr>
		</g:each>
		</g:if>
		<g:else>
			<tr><td colspan="40" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table></div>
</g:form>
<g:render template="../assetEntity/commentCrud"/>
<g:render template="../assetEntity/modelDialog"/>
<div id="editManufacturerView" style="display: none;"></div>
<div class="contextMenu" id="myMenu"></div>
<div class="contextMenu" id="transitionMenu" style="visibility: hidden;">
	<ul>
        <li id="start">Start</li>
		<li id="done">Done</li>
        <li id="NA">N/A</li>
        <li id="pending">Pending</li>
        <li id="void">Undo</li>
        <li id="ready">Ready</li>
        <li id="noOptions">No Options</li>
    </ul>
</div>
<div id ="showEntityView" style="display: none" title="Show Asset"></div>
<div id ="editEntityView" style="display: none" title="Edit Asset"></div>
<div id="editManufacturerView" style="display: none;"></div>
<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>

<script type="text/javascript" src="/tdstm/js/jquery.fixedheadertable.1.1.2.js"></script>

<script type="text/javascript">
/*<![CDATA[*/
	var timeInterval;
	var fieldId;
	var hasTimedOut = false;
	$(document).ready(function() {
		var windowWidth = $(window).width() - 10;
		var windowHeight = $(window).height() - $('.header').height() - $('#consoleHeader').height() - 25;
		if ($.browser.msie == true) {
			windowWidth -= 20;
			windowHeight -= 10;
		}
		
		$(window).resize(function() {
			if(hasTimedOut != false) {
				clearTimeout(hasTimedOut);
			}
			hasTimedOut = setTimeout(function() {
				var windowWidth = $(window).width() - 10;
				var windowHeight = $(window).height() - $('.header').height() - $('#consoleHeader').height() - 25;
				if ($.browser.msie == true) {
					windowWidth -= 20;
					windowHeight -= 10;
				}

				$('#tableContainer').css({'width': windowWidth+'px', 'height': windowHeight+'px'});
			}, 100);
		});
		
		$('#tableContainer').css({'width': windowWidth+'px', 'height': windowHeight+'px'});
		if(!$.browser.msie) {
			jQuery('#tableContainer').fixedHeaderTable({autoResize:true, footer:false});
			$('.fht_table_body thead select').remove();
			$('.fht_table_body thead input').remove();
		}
		
		$('body').click(function(){
			$(".cell-selected").removeClass('cell-selected');
		});
		
		$("#moveBundleId").val(${moveBundleInstance?.id});
		$("#moveEventId").val(${moveEventInstance?.id});
		$("#column4Id").val("${column4Value}");
		$("#column3Id").val("${column3Value}");
		$("#column2Id").val("${column2Value}");
		$("#column1Id").val("${column1Value}");
		$("#assetsInViewId").val("${assetsInView}");
		if($("#assetsInViewId")[0].selectedIndex == -1)
			$("#assetsInViewId")[0].selectedIndex = 0;
		
		timedUpdate($("#selectTimedId").val());
		
		var time = '${timeToUpdate}';
		if(time != "" ){
			$("#selectTimedId").val( time ) ;
		} else if(time == "" ){
			$("selectTimedId").val( 120000 );	
		}
		
		$("#changeStatusDialog").dialog({ autoOpen: false })
		$("#editEntityView").dialog({autoOpen: false})
	    $("#showEntityView").dialog({autoOpen: false})
		$("#commentsListDialog").dialog({ autoOpen: false })
		$("#createCommentDialog").dialog({ autoOpen: false })
	    $("#showCommentDialog").dialog({ autoOpen: false })
	    $("#editCommentDialog").dialog({ autoOpen: false })
	    $("#showChangeStatusDialog").dialog({ autoOpen: false })
	    $("#manufacturerShowDialog").dialog({ autoOpen: false })
	    $("#modelShowDialog").dialog({ autoOpen: false })
	    $("#editManufacturerView").dialog({ autoOpen: false})
		var role = "${role}";
		// Show menu when #myDiv is clicked
		if(role) {
			var actionId
			var bundle = $("#moveBundleId").val()
			$('tbody#assetListTbody').contextMenu('transitionMenu', {
				onContextMenu: function(e) {
					return($(e.target).is('td.tranCell') && !$(e.target).is('td.asset_hold'));
				},
				onShowMenu: function(e, menu) {
					$(".cell-selected").removeClass('cell-selected');
					$(e.target).addClass('cell-selected');
					actionId = $(e.target).attr("id") 
					${remoteFunction(action:'getMenuList', params:'\'id=\' + actionId ', onComplete:'updateMenu(e,menu)')};
					return menu;
				},
				bindings: {
	        		'start': function(t) {
			       		${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=start\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
			        },
			        'done': function(t) {
			       		${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=done\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
			        },
			        'ready': function(t) {
			          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=ready\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
			        },
			        'NA': function(t) {
			          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=NA\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
			        },
			        'pending': function(t) {
			          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=pending\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
			        },
			        'void': function(t) {
			          	if(confirm("Undo this specific task and any dependent (workflow) transitions. Are you sure?")){
			          		${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=void\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
						} else {
			          		return false
			         	}
			        },
			        'noOptions': function(t){
			        	$(".cell-selected").attr('class',$("#cssClassId").val());
			        }
		      	}
	    	});
		}
		$("tbody#assetListTbody tr td").click(function () {
	    	var tdId = $(this).attr("id")
	    	if(!isNaN(tdId.split("_")[1])){
		        if($("#bulkEditId").hasClass("bulkedit_active")){
				    var action = $("#bulkActionId").val()
				    switch (action){
					    case "pending" :
					    	${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + tdId +\'&type=pending\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
						break;
						case "done" :
							${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + tdId +\'&type=done\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
						break;
						case "NA" :
							${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + tdId +\'&type=NA\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
						break;
						case "void" :
							${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + tdId +\'&type=void\'+\'&bundle=\'+bundle', onComplete:'updateTransitionRow(e)' )};
						break;	
				    }
		        } else {
		        	${remoteFunction(controller:'assetEntity', action:'showStatus', params:'\'id=\'+tdId', onComplete:'window.status = e.responseText')}
		        }
	    	}
	    });
		$("tbody#assetListTbody tr td").mouseout(function(){
			window.status = ""
		}); 
	});
	
	/*------------------------------------------------------------
	 * update the menu for transition 
	 *------------------------------------------------------------*/
	function updateMenu(e,menu){
		var actionType = e.responseText
		switch(actionType){
			case "noTransMenu": 
				$('#start, #pending, #void, #ready, #noOptions', menu ).remove()
				break;
			case "naMenu": 
				$('#start, #NA, #void, #ready, #noOptions', menu ).remove()
				break;
			case "doneMenu": 
				$('#start, #done, #void, #ready, #noOptions', menu ).remove()
				break;
			case "readyMenu": 
				$('#start, #NA, #done, #void, #pending, #noOptions', menu ).remove()
				break;
			case "voidMenu": 
				$('#start, #NA, #done, #ready, #pending, #noOptions', menu ).remove()
				break;
			case "doMenu": 
				$('#start, #NA, #ready, #pending, #void, #noOptions', menu ).remove()
				break;
			case "noOption":
			case "rb_noOption":
				$('#start, #NA, #done, #ready, #pending, #void', menu ).remove()
				break;
			case "rb_doMenu": 
				$('#NA, #ready, #pending, #void, #noOptions', menu ).remove()
				break;
			case "rb_doneMenu": 
				$('#start, #NA, #ready, #pending, #noOptions', menu ).remove()
				break;
			case "rb_voidMenu": 
				$('#start, #NA, #done, #ready, #pending, #noOptions', menu ).remove()
				break;
			default :
				$('#start, #NA, #done, #ready, #pending, #void', menu ).remove()
				break;
		}
		menu.show()
	}
	/*-------------------------------------------------------------
	 * update the row as per user transition transition 
	 *------------------------------------------------------------*/
	function updateTransitionRow( e ){
		var assetTransitions = eval('(' + e.responseText + ')');
		var length = assetTransitions.length;
		if(length > 0){
			var moveEventId = $("#moveEventId").val();
			var moveBundleName = $("#moveBundleId").val();
			${remoteFunction(action:'getCurrentStatusOptions', params:'\'moveEventId=\' + moveEventId +\'&bundle=\'+ moveBundleName ', onComplete:'updateCurrentStatusOptions(e);' )}
			if($("#column1Attribute").val() == 'currentStatus'){ 
				$("#"+assetTransitions[0].id+"_column1").html(assetTransitions[0].cssClass);
			}
			if($("#column2Attribute").val() == 'currentStatus'){ 
				$("#"+assetTransitions[0].id+"_column2").html(assetTransitions[0].cssClass)
			}
			if($("#column3Attribute").val() == 'currentStatus'){ 
				$("#"+assetTransitions[0].id+"_column3").html(assetTransitions[0].cssClass)
			}
			if($("#column4Attribute").val() == 'currentStatus'){ 
				$("#"+assetTransitions[0].id+"_column4").html(assetTransitions[0].cssClass)
			}
			for( i=0; i<length; i++ ) {
				var transition = assetTransitions[i];
				$("#"+transition.id).attr("class",transition.cssClass );
				$("#"+transition.id).addClass('tranCell');
			}
			if (assetTransitions[0].message) {
				alert("Error: " + assetTransitions[0].message)
			}
			doAjaxCall();
		}
	}
	/*
	*	Update the current Status options when transition done through the Bulk edit 
	*/
	function updateCurrentStatusOptions( e ){
		var options = eval('(' + e.responseText + ')');
		var optionsString = "<option value='' selected='selected'>All</option>"
		if( options.length > 0 ){
			for(i=0; i<options.length; i++ ){
				var option = options[i] 
				var value = option.id ? option.id : 'blank'
				var text = option.key ? option.key : 'blank'
				optionsString+= "<option value='"+value+"'>"+ text +"("+option.value+")</option>"	
			}
		}
		if($("#column1Attribute").val() == 'currentStatus'){ 
			$("#column1Id").html(optionsString)
		}
		if($("#column2Attribute").val() == 'currentStatus'){ 
			$("#column2Id").html(optionsString)
		}
		if($("#column3Attribute").val() == 'currentStatus'){ 
			$("#column3Id").html(optionsString)
		}
		if($("#column4Attribute").val() == 'currentStatus'){ 
			$("#column4Id").html(optionsString)
		}
	}
	function editAssetDialog() {
		timedUpdate('never')
		$("#showDialog").dialog("close")
		$("#editDialog").dialog('option', 'width', 'auto')
		$("#editDialog").dialog('option', 'position', ['center','top']);
		$("#editDialog").dialog("open")
		
	}
		    
	function showEditAsset(e) {
   		var assetEntityAttributes = eval('(' + e.responseText + ')')
			if (assetEntityAttributes != "") {
		    	var length = assetEntityAttributes.length
				for (var i=0; i < length; i ++) {
					var attribute = assetEntityAttributes[i]
				    var tdId = $("#"+attribute.attributeCode+'_'+attribute.id)
				    if(tdId != null ){
				    	tdId.html( attribute.value )
				    }
				}
				$("#editDialog").dialog("close")
				timedUpdate($("#selectTimedId").val())
			} else {
		   		alert("Asset is not updated, Please check the required fields")
			}
	}

	function showChangeStatusDialog(e){
		timedUpdate('never')
		var task = eval('(' + e.responseText + ')');
		var taskLen = task[0].item.length;
		var options = '';
		var moveBundle = $("#moveBundleId").val()
		if(taskLen == 0){
			alert('Sorry but there were no common states for the assets selected');
			return false;
		}else{
	      	for (var i = 0; i < taskLen; i++) {
	        	options += '<option value="' + task[0].item[i].state + '">' + task[0].item[i].label + '</option>';
	      	}
	      	$("select#taskList").html(options);
	      	if(taskLen > 1 && task[0].item[0].state == "Hold"){
	      		$('#taskList').children().eq(1).attr('selected',true);
	      	}
	       	$("#assetIds").val(task[0].asset);
	       	$("#moveBundleIds").val(moveBundle);
			$("#changeStatusDialog").dialog('option', 'width', 400)
			$("#changeStatusDialog").dialog('option', 'position', ['center','top']);
			$('#changeStatusDialog').dialog('open');
			$('#createCommentDialog').dialog('close');
			$('#commentsListDialog').dialog('close');
			$('#editCommentDialog').dialog('close');
			$('#showCommentDialog').dialog('close');
			$('#showDialog').dialog('close');
			$('#editDialog').dialog('close');
			$('#createDialog').dialog('close');
		}
	}
	
	function submitAction(){
		if(doCheck()){
			document.changeStatusForm.action = "changeStatus";
			document.changeStatusForm.submit();
			timedUpdate($("#selectTimedId").val())
		}else{
			return false;
		}
	}
	
	function doCheck(){
		var taskVal = $('#taskList').val();
		var noteVal = $('#enterNote').val();
		if((taskVal == "Hold")&&(noteVal == "")){
			alert('Please Enter Note');
			return false;
		}else{
			return true;
		}
	}
	
	function setUpdateTime(e) {
		var timeUpdate = eval("(" + e.responseText + ")")
		if(timeUpdate){
			timedUpdate(timeUpdate[0].updateTime.CLIENT_CONSOLE_REFRESH)
		}
	}
	
	var timer
	function timedUpdate(timeoutPeriod) {
		if(timeoutPeriod != 'never'){
			clearTimeout(timer)
			timer = setTimeout("doAjaxCall()",timeoutPeriod);
			$("#selectTimedId").val( timeoutPeriod );
		} else {
			clearTimeout(timer)
		}
	}
	function pageReload(){
		if('${myForm}'){
			document.forms['${myForm}'].submit() ;
		} else {
			window.location.href = document.URL;
		}
	}
	
	function doAjaxCall(){
		var moveEvent = $("#moveEventId").val();
		var moveBundle = $("#moveBundleId").val();
		var c1f = "${columns.column1.field}"
		var c1v = "${column1Value}"
		var c2f = "${columns.column2.field}"
		var c2v = "${column2Value}"
		var c3f = "${columns.column3.field}"
		var c3v = "${column3Value}"
		var c4f = "${columns.column4.field}"
		var c4v = "${column4Value}"
		var offset = $("#assetsInViewOffsetId").val()
		var max = $("#assetsInViewId").val()
		var sort = $("#sortById").val()
		var order = $("#orderById").val()
		var lastPoolTime = $("#lastPoolTimeId").val();
		${remoteFunction(action:'getTransitions', params:'\'moveBundle=\' + moveBundle +\'&moveEvent=\'+moveEvent +\'&c1f=\'+c1f+\'&c2f=\'+c2f+\'&c3f=\'+c3f+\'&c4f=\'+c4f+\'&c1v=\'+c1v+\'&c2v=\'+c2v+\'&c3v=\'+c3v+\'&c4v=\'+c4v+\'&lastPoolTime=\'+lastPoolTime+\'&offset=\'+offset+\'&max=\'+max+\'&sort=\'+sort+\'&order=\'+order', onFailure:"handleErrors()", onComplete:'updateTransitions(e);' )}
		${remoteFunction(action:'getCurrentStatusOptions', params:'\'moveEventId=\' + moveEvent +\'&bundle=\'+ moveBundle ', onComplete:'updateCurrentStatusOptions(e);' )}
		timedUpdate($("#selectTimedId").val())
	}
	var doUpdate = true
	function handleErrors(){
		if( !doUpdate ){
			clearTimeout(timer);
		}
		doUpdate = false
		$("#updateId").css("color","red");
		alert("Sorry, there is a problem receiving updates to this page. Try reloading to resolve.");
	}
	function updateTransitions(e){
		try{
			var assetEntityCommentList = eval('(' + e.responseText + ')');
			var assetTransitions = assetEntityCommentList[0].assetEntityList;
			var assetComments = assetEntityCommentList[0].assetCommentsList;
			var assetslength = assetTransitions.length;
			var assetCommentsLength = assetComments.length;
			var sessionStatus = isNaN(parseInt(assetslength));
			if( !sessionStatus ){
				if(assetTransitions){
					for( i = 0; i <assetslength ; i++){
						var assetTransition = assetTransitions[i]
						var action = $("#action_"+assetTransition.id)
						if(action){
							if(!assetEntityCommentList[0].check){
								action.html('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
							}
						}
						
						$("#"+assetTransition.id+"_column1").html( assetTransition.column1value );
						$("#"+assetTransition.id+"_column2").html( assetTransition.column2value );
						$("#"+assetTransition.id+"_column3").html( assetTransition.column3value );
						$("#"+assetTransition.id+"_column4").html( assetTransition.column4value );
						$("#"+assetTransition.id+"_column5").html( assetTransition.lastUpdated );
						
						var tdIdslength = assetTransition.tdId.length
						for(j = 0; j< tdIdslength ; j++){
							var transition = assetTransition.tdId[j]
							var transTd = $("#"+transition.id)
							transTd.attr("class",transition.cssClass )
							transTd.addClass('tranCell');
						}
					}
				}
				if(assetComments){
					for( i = 0; i <assetCommentsLength ; i++){
						var assetComment = assetComments[i]
						var commentIcon = $("#icon_"+assetComment.assetEntityId)
						if(commentIcon){
							var link = document.createElement('a');
							link.href = '#';
							link.id = assetComment.assetEntityId;
							if ( assetComment.type == "db_table_light.png" ) {
								link.onclick = function(){$('#newAssetCommentId').val(this.id);createNewAssetComment('');};
							} else {
								link.onclick = function(){$('#createAssetCommentId').val(this.id);new Ajax.Request('../assetEntity/listComments?id='+this.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'action');}})} //;return false
							}
							link.innerHTML = "<img src=\"../i/"+assetComment.type+"\" border=\"0px\" />";
							commentIcon.html(link);
						}
					}
				}
				$("#lastPoolTimeId").val(assetEntityCommentList[0].lastPoolTime)
			} else {
				location.reload(false);
			//timedUpdate('never')
			}
		} catch(ex){
		//location.reload(false);
			if( doUpdate ){
				handleErrors();
			}
		}
	}

	function changeState(){
		timedUpdate('never')
		var assetArr = new Array();
		var totalAsset = ${assetEntityList.id};
		var j=0;
		for(i=0; i< totalAsset.size() ; i++){
			if($('#checkId_'+totalAsset[i]) != null){
				var booCheck = $('#checkId_'+totalAsset[i]).is(':checked');
				if(booCheck == true){
					assetArr[j] = totalAsset[i];
					j++;
				}
			}
		}	
		if(j == 0){
			alert('Please select the Asset');
		}else{
			${remoteFunction(action:'getList', params:'\'assetArray=\' + assetArr', onComplete:'showChangeStatusDialog(e);' )}
		}
	}
	
	var isFirst = true;
	function selectAll(){
		timedUpdate('never')
		var totalCheck = document.getElementsByName('checkChange');
		if(isFirst){
			for(i=0;i<totalCheck.length;i++){
				totalCheck[i].checked = true;
			}
			isFirst = false;
		}else{
			for(i=0;i<totalCheck.length;i++){
				totalCheck[i].checked = false;
			}
			isFirst = true;
		}
	}
	
	function showAssetDetails( assetId ){
		document.editForm.id.value=assetId
		document.showForm.id.value=assetId
		//${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+ assetId', before:'document.showForm.id.value ='+ assetId+';', onComplete:'showAssetDialog(e , \'show\')')}
	}
	function vpWidth(type) {
		var data
		if(type == "width"){
			data  = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
		} else {
			data  = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
		}
		return data
	}

	//To catch the event and call the specific remotefunction 
	function catchevent(event) {
		var oSource = event.target
		var srcElement = event.srcElement ? event.srcElement : event.target;
		eventSrcID=(srcElement)?srcElement.id:'undefined';
		var idArray = eventSrcID.split('_')
		if( idArray.length = 2  ) {
			var assetId = idArray[1]
			if( idArray[0] == "comment"  ) {
				${remoteFunction(controller:'assetEntity', action:'listComments', params:'\'id=\'+assetId', before:'setAssetId(assetId);',onComplete:'listCommentsDialog( e ,\"action\");')}
			}else if( idArray[0] == "asset"  ) {
				getEntityDetails('clientConsole','Server',assetId);
			}else if( idArray[0] == "task" ) {
				${remoteFunction( action:"getTask", params:'\'assetEntity=\'+assetId', onComplete:'showChangeStatusDialog(e);')}
			}
		}
	}
	var bulkEdit = true;
	function performBulkEdit(){
		
		if(bulkEdit){
			${remoteFunction(action:'setBulkWarning', onComplete:'showBulkEdit(e);' )}
		} else {
			var bulkEditButton = $("#bulkEditId");
			bulkEditButton.removeClass("bulkedit_active")
			bulkEditButton.addClass("bulkedit_inactive")
			bulkEditButton.attr('value','Bulk Edit')
			bulkEdit = true
			$("#bulkTaskSpanId").hide();			
		}
	}
	function showBulkEdit(e){
		var bulkEditButton = $("#bulkEditId");
		if(e.responseText != "true"){
			alert("You are now in bulk edit mode. Select the state then the cells you want to change. Remember to turn off Bulk Edit when done." )
		}
		bulkEditButton.removeClass("bulkedit_inactive")
		bulkEditButton.addClass("bulkedit_active")
		bulkEditButton.attr('value','End Bulk Edit');
		bulkEdit = false
		/*------- show Done as default ----------*/
		changeAction( "done" )
		
		$("#bulkTaskSpanId").show();
	}
	function changeAction( action ){
		switch ( action ){
			case "pending" :
				$("#bulkDoneId").removeClass("bulkDone_active")
				$("#bulkNaId").removeClass("bulkNa_active")
				$("#bulkUndoId").removeClass("bulkPending_active")
				$("#bulkPendingId").addClass("bulkPending_active")
				$("#bulkActionId").val("pending")
			break;
			case "done" :
				$("#bulkNaId").removeClass("bulkNa_active")
				$("#bulkPendingId").removeClass("bulkPending_active")
				$("#bulkUndoId").removeClass("bulkPending_active")
				$("#bulkDoneId").addClass("bulkDone_active")
				$("#bulkActionId").val("done")
			break;
			case "void" :
				$("#bulkDoneId").removeClass("bulkDone_active")
				$("#bulkNaId").removeClass("bulkNa_active")
				$("#bulkPendingId").removeClass("bulkPending_active")
				$("#bulkUndoId").addClass("bulkPending_active")
				$("#bulkActionId").val("void")
			break;
			case "NA" :
				$("#bulkPendingId").removeClass("bulkPending_active")
				$("#bulkDoneId").removeClass("bulkDone_active")
				$("#bulkUndoId").removeClass("bulkPending_active")
				$("#bulkNaId").addClass("bulkNa_active")
				$("#bulkActionId").val("NA")
			break;	
		}
	}

	/* 
	 * Function to switch the Labels to Select list when user click on edit icon.
	 */
	function changeLabelToSelect(){
		$("#column1Label").hide()
		$("#column1Select").show()
		$("#column1Edit").hide()
		$("#column1Save").show()
		$("#column2Label").hide()
		$("#column2Select").show()
		$("#column2Edit").hide()
		$("#column2Save").show()
		$("#column3Label").hide()
		$("#column3Select").show()
		$("#column3Edit").hide()
		$("#column3Save").show()
		$("#column4Label").hide()
		$("#column4Select").show()
		$("#column4Edit").hide()
		$("#column4Save").show()
	}
	/*
	* Function to switch the Select list to Label when user click on 'X' button.
	*/
	function changeToLabel( colId ){
		/*var value = ""
		switch (colId) {
		case "1" :
			value = "${columns?.column1.field}"
			break;
		case "2" :
			value = "${columns?.column2.field}"
			break;
		case "3" :
			value = "${columns?.column3.field}"
			break;
		case "4" :
			value = "${columns?.column4.field}"
			break;
		}
		$("#column"+colId+"Label").show()
		$("#column"+colId+"Select select").val(value)
		$("#column"+colId+"Select").hide()
		$("#column"+colId+"Edit").show()
		$("#column"+colId+"Save").hide()*/
		
		$("#column1Label").show()
		$("#column1Select select").val("${columns?.column1.field}")
		$("#column1Select").hide()
		$("#column1Edit").show()
		$("#column1Save").hide()
		$("#column2Label").show()
		$("#column2Select select").val("${columns?.column2.field}")
		$("#column2Select").hide()
		$("#column2Edit").show()
		$("#column2Save").hide()
		$("#column3Label").show()
		$("#column3Select select").val("${columns?.column3.field}")
		$("#column3Select").hide()
		$("#column3Edit").show()
		$("#column3Save").hide()
		$("#column4Label").show()
		$("#column4Select select").val("${columns?.column4.field}")
		$("#column4Select").hide()
		$("#column4Edit").show()
		$("#column4Save").hide()
	}
	/*
	* 	Bulk edit of transitions by letting the project manager click on column head to transition the displayed assets to that step.
	*/
	function bulkTransitionsByHeader( transId ){
		if($("#bulkEditId").hasClass("bulkedit_active")){
			var eventId = $("#moveEventId").val();
			var bundleId = $("#moveBundleId").val();
			var type = $("#bulkActionId").val();
			var c1f = "${columns.column1.field}"
			var c1v = "${column1Value}"
			var c2f = "${columns.column2.field}"
			var c2v = "${column2Value}"
			var c3f = "${columns.column3.field}"
			var c3v = "${column3Value}"
			var c4f = "${columns.column4.field}"
			var c4v = "${column4Value}"
			var offset = $("#assetsInViewOffsetId").val()
			var max = $("#assetsInViewId").val()
			var sort = $("#sortById").val()
			var order = $("#orderById").val()
			
			${remoteFunction(action:'getAssetsCountForBulkTransition', 
								params:'\'transId=\' + transId +\'&bundleId=\'+bundleId+\'&eventId=\'+eventId+\'&type=\'+type+\'&c1f=\'+c1f+\'&c2f=\'+c2f+\'&c3f=\'+c3f+\'&c4f=\'+c4f+\'&c1v=\'+c1v+\'&c2v=\'+c2v+\'&c3v=\'+c3v+\'&c4v=\'+c4v+\'&offset=\'+offset+\'&max=\'+max+\'&sort=\'+sort+\'&order=\'+order', 
								onComplete:'doBulkTransitionsByHeader(e,transId)' )};
		}
	}
	function doBulkTransitionsByHeader( e, transId){
		var message = e.responseText
		if(confirm( message )){
			var eventId = $("#moveEventId").val();
			var bundleId = $("#moveBundleId").val();
			var type = $("#bulkActionId").val();
			var type = $("#bulkActionId").val()
			var c1f = "${columns.column1.field}"
			var c1v = "${column1Value}"
			var c2f = "${columns.column2.field}"
			var c2v = "${column2Value}"
			var c3f = "${columns.column3.field}"
			var c3v = "${column3Value}"
			var c4f = "${columns.column4.field}"
			var c4v = "${column4Value}"
			var offset = $("#assetsInViewOffsetId").val()
			var max = $("#assetsInViewId").val()
			var sort = $("#sortById").val()
			var order = $("#orderById").val()
			${remoteFunction(action:'doBulkTransitionsByHeader', 
							params:'\'transId=\' + transId +\'&bundleId=\'+bundleId+\'&eventId=\'+eventId+\'&type=\'+type+\'&c1f=\'+c1f+\'&c2f=\'+c2f+\'&c3f=\'+c3f+\'&c4f=\'+c4f+\'&c1v=\'+c1v+\'&c2v=\'+c2v+\'&c3v=\'+c3v+\'&c4v=\'+c4v+\'&offset=\'+offset+\'&max=\'+max+\'&sort=\'+sort+\'&order=\'+order', 
							onComplete:'doAjaxCall()' )};
		}
	}

	function changeMoveBundle(moveEventValue){
		var moveEventnName =  moveEventValue
		${remoteFunction(action:'moveBundleList', params:'\'moveEvent=\' + moveEventnName', onSuccess:'fillMoveBundle(e)' )};
	}
	
	function fillMoveBundle(e){
		var data = eval('(' + e.responseText + ')');
		var selectContent = "<select id='moveBundleId' name='moveBundle'>"

		for (var i = 0; i < data.size(); i++){
			if( data[i].showAllOption ){
			   selectContent += "<option selected='selected' value='all' >"+'All'+"</option>"
			 } 
			 selectContent += "<option value ="+data[i].id+">"+data[i].name+"</option>"
		}
		selectContent += "</select>"
	    $('#jsonReplace').html(selectContent);
		submitForm()
	} 
	function submitForm(){
		document.listForm.submit()
	}
/*]]>*/
</script>
<script>
	currentMenuId = "#dashboardMenu";
	$("#dashboardMenuId a").css('background-color','#003366')
</script>
</body>
</html>
