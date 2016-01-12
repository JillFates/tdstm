<%@page import="com.tdssrc.grails.TimeUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<title>My Tasks</title>
	<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="angular/angular.min.js" />
	<g:javascript src="angular/plugins/angular-ui.js"/>	
	<g:javascript src="cabling.js"/>
	<g:javascript src="model.manufacturer.js"/>
	<g:javascript src="angular/plugins/angular-resource.js" />
	<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
	<g:javascript src="bootstrap.js" />
	<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
	<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
	<g:javascript src="TimerBar.js" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
</head>
<body>
	<input type="hidden" id="timeBarValueId" value="0"/>
	<div class="taskTimebar hide" id="issueTimebar" >
		<div id="issueTimebarId"></div>
	</div>
	<div class="menu4">
		<g:if test="${isOnIE && isCleaner}">
			<OBJECT id="TF" classid="clsid:18D87050-AAC9-4e1a-AFF2-9D2304F88F7C"
				CODEBASE="${resource(dir:'resource',file:'TFORMer60.cab')}"
				style="height: 1px;"></OBJECT>
		</g:if>
		<ul>
			<g:if test="${tab && tab == 'todo'}">
				<li onclick="setTab('todo')">
					<g:link elementId="taskLinkId" class="mobmenu mobselect" action="listUserTasks" params='["tab":"todo"]'>
						<span>Ready Tasks:</span> <span id="toDOSpanId">${todoSize}</span>
					</g:link>
				</li>
				<li onclick="setTab('all')">
					<g:link elementId="taskLinkAllId" class="mobmenu" action="listUserTasks" params='["tab":"all"]'>
						<span>All Tasks:</span> <span id="toDOAllSpanId">${allSize}</span>
					</g:link>
				</li>
			</g:if>
			<g:if test="${tab && tab == 'all'}">
				<li onclick="setTab('todo')">
					<g:link elementId="taskLinkId" class="mobmenu" action="listUserTasks" params='["tab":"todo"]'>
						<span>Ready Tasks:</span> <span id="allToDoSpanId">${todoSize}</span>
					</g:link>
				</li>
				<li onclick="setTab('all')">
					<g:link elementId="taskLinkAllId" class="mobmenu mobselect" action="listUserTasks" params='["tab":"all"]'>
						<span>All Tasks:</span> <span id="allSpanId">${allSize}</span>
					</g:link>
				</li>
			</g:if>
			<li>
				<span style="float: right;margin-right: 10px;">
					<g:render template="../assetEntity/timerBarControls" model="${[timerValues:[30, 60, 120, 180, 240, 300]]}"/>
				</span>
			</li>
		</ul>
		<div class="tab_search">
			<g:form method="post" name="issueAssetForm" action="showIssue">
				<input type="text" size="08" value="${search}" id="search" name="search" autocorrect="off" autocapitalize="off"
					onfocus="changeAction()" onblur="retainAction()" />
				<input type="hidden" name="sort" value="${sort}" />
				<input type="hidden" name="order" value="${order}" />
				<input type="hidden" name="tab" id="tabId" value="${tab}" />
				<input type="hidden" id="myPage" value="mytask" />
				<span style="color: white;">Event : 
					<g:select name='event' value="${moveEvent?.id}"
						noSelection="${['null':'All Events']}"
						from='${moveEventList}'
						optionKey="id" 
						optionValue="name"
						onfocus="changeAction()" onblur="retainAction()"
						onchange="submit()">
					</g:select>
				</span>
			</g:form>
		</div>
	</div>
	<div id="detailId" style="display: none; position: absolute; width: 420px; margin-top: 40px;"></div>
	<div class="mainbody">
		<div id="myTaskList">
			<g:render template="tasks"/>
		</div>
	</div>
	<g:render template="../assetEntity/modelDialog"/>
	<g:render template="../assetEntity/entityCrudDivs" />
	<g:render template="../assetEntity/dependentAdd" />
	<br />
<%--
/*
 **************************
 * Dependency Dialog
 **************************
 */
--%>
	<div id="dependencyBox" style="display: none;" align="right" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
		<span class="ui-icon ui-icon-closethick" unselectable="on" onclick="closeBox()"></span>
		<table id="showCommentTable" style="border: 0px;">
			<tr class="prop">
				<td valign="top" class="name"><label for="comment"><b>Task:</b></label></td>
				<td valign="top" class="value" colspan="3">
					<span id="commentTdId_myTasks"></span>
				</td>
			</tr>
			<tr class="prop" id="predecessorShowTr">
				<td valign="top"><label for="precessorShowId">Predecessor:</label></td>
				<td valign="top" id="predecessorShowTd" colspan="2" width="34%"></td>
				<td valign="top"  style="float: left;"><label for="precessorShowId" >Successor:</label></td>
				<td valign="top" id="successorShowTd" colspan="2" width="76%" style="margin-left: 0px;float: left;"></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="status">Status:</label></td>
				<td valign="top" class="value" id="statusShowId" colspan="1" style="width: 20%"></td>
			</tr>
			<tr class="issue" id="assignedToTrId">
				<td valign="top" class="name"><label for="assignedTo">Assigned To:</label></td>
				<td valign="top" class="value" id="" colspan="3">
					<span id="assignedToTdId"></span>&nbsp;-&nbsp;
					<span id="roleTdId"></span>&nbsp;
					<input type="checkbox" id="hardAssignedShow" name="hardAssignedShow" value="0" readonly="readonly"/>
					<label for="hardAssignedShow" >Fixed Assignment</label>&nbsp;&nbsp;
					<span id="dueDateTrId">
						<label for="dueDate">Due :</label>
						<span id="dueDateId"></span>
					</span>
				</td>
			</tr>
			<tr class="issue" id="workFlowShow">
				<td valign="top" class="name"><label for="durationShowId">Duration:</label></td>
				<td valign="top" class="value" colspan="3">
					<span id="durationShowId"></span>
					<span id="durationScale"></span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<span ><label for="priorityShowId">Priority:</label></span>
					<span id="priorityShowId"></span>
				</td>
			</tr>
			<tr id="assetShowId" class="prop">
				<td valign="top" class="name" id="assetTdId"><label for="asset">Asset:</label></td>
				<td valign="top" class="value" id="assetShowValueId" colspan="3"></td>
			</tr>
			<tr class="issue" id="estStartShow">
				<td valign="top" class="name" nowrap="nowrap"><label for="estStartShowId">Est.Start:</label></td>
				<td valign="top" class="value" id="estStartShowId" nowrap="nowrap"></td>
				<td valign="top" class="name" nowrap="nowrap"><label for="estFinishShowId">Est.Finish:</label></td>
				<td valign="top" class="value" id="estFinishShowId" nowrap="nowrap"></td>
			</tr>
			<tr class="issue" id="actStartShow">
				<td valign="top" class="name"><label for="actStartShowId">Act.Start:</label></td>
				<td valign="top" class="value" id="actStartShowId"></td>
				<td valign="top" class="name" nowrap="nowrap" width="10%"><label for="actFinishShowId">Act.Finish:</label></td>
				<td valign="top" class="value" id="actFinishShowId" nowrap="nowrap"></td>
			</tr>
		</table>
	</div>

<script type="text/javascript">
/*	<g:if test="${isOnIE && isCleaner}">*/
    /*
     * To load the installed printers into session by initializing TFORMer
     */
	$(function() {
		window.TF.RefreshOSPrinters();
		var def = "";
		var dropdown = new Array();
		for (i = 0; i < window.TF.GetOSPrintersCount(); i++){
			dropdown.push(window.TF.GetOSPrinter(i))
		}
		${remoteFunction(controller:'moveTech', action:'setPrintersIntoSession', params:'\'dropdown=\' + dropdown')}
		
	});
/*	</g:if>*/
	$(function() {
		var searchedAssetId = '${searchedAssetId}'
		var searchedAssetStatus = '${searchedAssetStatus}'
		if (searchedAssetId) {
			issueDetails(searchedAssetId,searchedAssetStatus);
		}
	});

	$(".actionBar").die().live('click',function(){
		var id = $(this).attr('data-itemId');
		var status = $(this).attr('data-status');
		var showStatusTr = $('#showStatusId_'+id);
		if (status=='Started') {
			$('#started_'+id).hide();
			$('#image_'+id).hide();
		}
		if (!$(this).data('state')) {
			showStatusTr.toggle();
			$(this).data('state',true);
		} else {
			showStatusTr.toggle();
			$(this).data('state',false);
			$('#detailTdId_'+id).hide();
		}
	});
	
	function setFocus() {
		$("#search").val('').focus();
	}
	function issueDetails(id,status) {
		// hideStatus(id,status)
		if (typeof timerBar !== 'undefined')
			timerBar.Pause();
		jQuery.ajax({
			url: tdsCommon.createAppURL('/task/showIssue'),
			data: {'issueId':id},
			type:'POST',
			success: function(data) {
				$('#showStatusId_'+id).css('display','none')
				//$('#issueTr_'+id).attr('onClick','cancelButton('+id+',"'+status+'")');
				$('#detailId_'+id).html(data)
				$('#detailTdId_'+id).css('display','table-row')
				//$('#detailId_'+id).css('display','block')
				$('#taskLinkId').removeClass('mobselect')
				new Ajax.Request(tdsCommon.createAppURL('/assetEntity/updateStatusSelect?id='+id),{asynchronous:false,evalScripts:true,
					onComplete:function(e){
						var resp = e.responseText;
						resp = resp.replace("statusEditId","statusEditId_"+id).replace("showResolve(this.value)","showResolve()")
						$('#statusEditTrId_'+id).html(resp)
						// $('#statusEditId_'+id).val(status)
			 		}
				})

				new Ajax.Request(tdsCommon.createAppURL('/assetEntity/isAllowToChangeStatus?id='+id),{asynchronous:false,evalScripts:true,
					onComplete:function(e){
						var resp = JSON.parse(e.responseText);
						var disabledStr = "";
						if(!resp.isAllowToChangeStatus){
							disabledStr = "disabled"
						}
						$("[id^=statusEditId_]").prop("disabled", disabledStr);
			 		}
				})

				$("#labelQuantity").focus();
			}
		});
	}
	function showAssetCommentMyTasks(id) {
		$('#dependencyBox').css('display','table');
		if (typeof timerBar !== 'undefined')
			timerBar.Pause();
		jQuery.ajax({
			url: tdsCommon.createAppURL('/assetEntity/showComment'),
			data: {'id':id},
			type:'POST',
			success: function(data) {
				var ac = data[0];
				$('#predecessorShowTd').html(ac.predecessorTable)
				$('#successorShowTd').html(ac.successorTable)
				$('#assignedToTdId').html(ac.assignedTo)
				$('#estStartShowId').html(ac.etStart)
				$('#estFinishShowId').html(ac.etFinish)
				$('#actStartShowId').html(ac.atStart)
				$('#actFinishShowId').html(ac.dtResolved)
				$('#dueDateId').html(ac.dueDate)
				ac = ac.assetComment;
				$('#statusShowId').attr("class","task_"+ac.status.toLowerCase())
				$('#showCommentTable #statusShowId').attr("class","task_"+ac.status.toLowerCase())
				$('#commentTdId_myTasks').html(ac.taskNumber+":"+ac.comment)
				$('#commentTdId1').html(ac.comment)
				$('#statusShowId').html(ac.status)
				$('#showCommentTable #statusShowId').html(ac.status)
				$('#roleTdId').html(ac.role)
				$('#hardAssignedShow').html(ac.hardAssigned)
				$('#durationShowId').html(ac.duration)
				$('#durationScale').html(ac.durationScale)
				$('#priorityShowId').html(ac.priority)
				$('#assetShowValueId').html(ac.assetEntity)
			}
		});
	}
	function closeBox() {
		$('#dependencyBox').css("display","none");
	}
	function cancelButton(id,status) {
		//$('#myIssueList').css('display','block')
		$('#detailTdId_'+id).css('display','none')
		$('#taskLinkId').addClass('mobselect')
		$('#showStatusId_'+id).css('display','table-row')
		//$('#issueTr_'+id).attr('onClick','issueDetails('+id+',"'+status+'")');
		if (typeof timerBar !== 'undefined')
			timerBar.attemptResume();
	}

function changeAction(){
	document.issueAssetForm.action = 'listUserTasks'
}

function retainAction(){
	document.issueAssetForm.action = 'showIssue'
}
function pageRefresh(){
	document.issueAssetForm.action = 'listUserTasks'
	document.issueAssetForm.submit()
}

function setTab(tab){
	$("#tabId").val(tab)
}

setFocus();
</script>
<script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
	var timerBar;
	
	$(document).ready(function() {
		timerBar = new TimerBar(60, 'RefreshMyTasks', null);
		
		$("#showEntityView").dialog({ autoOpen: false })
		$("#createEntityView").dialog({ autoOpen: false })
		$("#editEntityView").dialog({ autoOpen: false })
		$("#manufacturerShowDialog").dialog({ autoOpen: false })
		$("#modelShowDialog").dialog({ autoOpen: false })
		$("#editManufacturerView").dialog({ autoOpen: false})
		$("#createCommentDialog").dialog({ autoOpen: false })
		$("#cablingDialogId").dialog({ autoOpen:false })

		<g:if test="${selectedTaskId}">
		setTimeout(function(){ issueDetails(${selectedTaskId}); }, 500);
		</g:if>
	});
</script>
	<g:render template="../assetEntity/initAssetEntityData"/>
	<g:render template="../layouts/error"/>
</body>
</html>
