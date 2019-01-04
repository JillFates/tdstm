<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="topNav" />
	<title>My Tasks</title>
	<asset:link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
	<asset:stylesheet href="css/qvga.css" />
	<g:javascript src="keyevent_constants.js" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="entity.crud.js" />
	<g:render template="/layouts/responsiveAngularResources" />
	<g:javascript src="model.manufacturer.js"/>
	<g:javascript src="progressTimer.js" />
</head>
<body>
	<tds:subHeader title="My Task" crumbs="['Tasks','My Task']"/>
	<script type="text/javascript">
		//Some Preferences
		window.PREFS = {
			PRINTER_NAME : "${prefPrinter}",
			PRINTER_COPIES : "${prefPrinterCopies}"
		}
	</script>
	<%-- BEGIN: NOTIFICATIONS SCRIPT --%>
	<span id="notification" style="display:none;"></span>
	<script id="successTemplate" type="text/x-kendo-template">
		<div class="alert alert-success alert-dismissable fadeIn" ng-show="alert.success.show">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
			<h4><i class="icon fa fa-check"></i><span ng-if="alert.success.status">#= title #:</span> #= message #</h4>
		</div>
	</script>
	<script id="errorTemplate" type="text/x-kendo-template">
		<div class="alert alert-danger alert-dismissable fadeIn">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
			<h4><i class="icon fa fa-info"></i><span ng-if="alert.info.status">#= title #: </span></h4>
			<div>
				<ul>
					<li >#= message #</li>
				</ul>
			</div>
		</div>
	</script>
	<script type="application/javascript">
		$(function(){
			//Init Notifications
			window.NOTIFICATION = $("#notification").kendoNotification({
				autoHideAfter: 4000,
				stacking: "down",
				templates: [{
					type: "success",
					template: $("#successTemplate").html()
				}, {
					type: "error",
					template: $("#errorTemplate").html()
				}]

			}).data("kendoNotification");
		});
	</script>
<%-- END: NOTIFICATIONS SCRIPT --%>
	<input type="hidden" id="timeBarValueId" value="0"/>

	<div class="taskTimebar hide" id="issueTimebar" >
		<div id="issueTimebarId"></div>
	</div>
	<div class="menu4 my-task-container">
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
					<g:render template="/assetEntity/progressTimerControls" model="${[timerValues:[30, 60, 120, 180, 240, 300]]}"/>
				</span>
			</li>
		</ul>
		<div class="tab_search">
			<g:form method="post" id="issueAssetForm" name="issueAssetForm" action="showIssue">
				<input type="hidden" id="oldSearchValue" value="${search}"/>
				<input size="12" value="${search}" id="search" name="search" placeholder="Asset Tag" class="clearable" autocomplete="off" autocorrect="off" autocapitalize="off"
					onfocus="changeAction()" onblur="retainAction()" />
				<input type="hidden" name="sort" value="${sort}" />
				<input type="hidden" name="order" value="${order}" />
				<input type="hidden" name="tab" id="tabId" value="${tab}" />
				<input type="hidden" id="myPage" value="mytask" />
				<input type="hidden" id="searchExecuted" value="true" />
				<span style="color: white;">Event :
					<g:select name='event' value="${moveEvent?.id}" class="slc-event"
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
			<span class="keyStrokesHandler" status="enable"></span>
			<g:render template="tasks"/>
		</div>
	</div>
	<g:render template="/assetEntity/modelDialog"/>
	<g:render template="/assetEntity/entityCrudDivs" />
	<g:render template="/assetEntity/dependentAdd" />
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
	jQuery(function($) {
		//////////////////////////
		// CLEARABLE INPUT
		function tog(v){return v?'addClass':'removeClass';}
		$(document).on('input', '.clearable', function(){
			$(this)[tog(this.value)]('x');
		}).on('mousemove', '.x', function( e ){
			$(this)[tog(this.offsetWidth-18 < e.clientX-this.getBoundingClientRect().left)]('onX');
		}).on('touchstart click', '.onX', function( ev ){
			ev.preventDefault();
			$(this).removeClass('x onX').val('').change();
			pageSubmit(); //clear and submit
	});
		$(".clearable").each(function(){
			$(this)[tog(this.value)]('x');
		})
	});

	$(function() {
		var searchedAssetId = '${searchedAssetId}'
		var searchedAssetStatus = '${searchedAssetStatus}'
		if (searchedAssetId) {
			issueDetails(searchedAssetId,searchedAssetStatus);
		}
	});

	$(".actionBar").die().live('click',function(){
		//debugger;
		var openedDetail = $('#opened-detail-'+$(this).attr('data-itemId'));
		if(openedDetail && openedDetail.length > 0) {
			$('#detailTdId_'+$(this).attr('data-itemId')).hide();
			$('#detailId_'+$(this).attr('data-itemId')).html('');
			return;
		}

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
		$("#search").focus();
	}

	function issueDetails(id,status) {
		// hideStatus(id,status)
		if (typeof progressTimer !== 'undefined') {
			progressTimer.Pause();
		}
		jQuery.ajax({
			url: tdsCommon.createAppURL('/task/showIssue'),
			data: {'issueId':id},
			type:'POST',
			success: function(data) {
				$('tr.taskDetailsRow').hide();
				$('div.task-details').empty();
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
		if (typeof progressTimer !== 'undefined')
			progressTimer.Pause();
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
		$('#search').val('');
		pageSubmit();
	}

function changeAction(){
	document.issueAssetForm.action = 'listUserTasks'
}

function hasSearchValue() {
	return $('#search').val().trim() !== $('#oldSearchValue').val().trim();
}

function retainAction(){
	if(hasSearchValue()) {
		$('#issueAssetForm').submit();
		return false;
}
	document.issueAssetForm.action = 'showIssue';
}

function pageSubmit(){
	document.issueAssetForm.action = 'listUserTasks';
	document.issueAssetForm.submit();
	//var form = $('#issueAssetForm');
	//form.action = 'listUserTasks';
	//form.submit();
}

function pageSubmitIfValidSearch(){
	if(hasSearchValue()) {
		pageSubmit();
		/*
		var form = $('#issueAssetForm');
		form.action = 'listUserTasks';
		form.submit();
		*/
	}
}

function clearSearch() {
	$('#search').val('');
	pageSubmit();
}

function setTab(tab){
	$("#tabId").val(tab)
}

$('#search').keyup(function(event) {
	if (event.keyCode == KeyEvent.DOM_VK_RETURN) {
		pageSubmit();
		return false;
	}
}).on("focus", function(){
	$(this).select();
});

setFocus();
</script>
<script>
	currentMenuId = "#teamMenuId";
	var progressTimer;

	$(document).ready(function() {
		progressTimer = new ProgressTimer(40, '${com.tdsops.tm.enums.domain.UserPreferenceEnum.MYTASKS_REFRESH}', null);

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

		$('div.ui-dialog-content').on('dialogopen', function(event) {
			$('.keyStrokesHandler').attr('status', 'disabled');
		});

		$('div.ui-dialog-content').on('dialogclose', function(event) {
			$('.keyStrokesHandler').attr('status', 'enable');
		});

	});
</script>
<g:if test="${isCleaner}">
<script type="text/javascript" src="${resource(dir:'js/qz-tray/lib/dependencies',file:'rsvp-3.1.0.min.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js/qz-tray/lib/dependencies',file:'sha-256.min.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js/qz-tray/lib',file:'qz-tray.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js/qz-tray',file:'qzShowCleanerTasks.js')}"></script>
<script type="text/javascript">
	jQuery(function(){
		function loadPrinters(){
			var printersEL = $("#printers");
			if(printersEL && printersEL.val() === null) {
				var prefPrinter = window.PREFS.PRINTER_NAME;
				window.QZObj.findPrinters(function (printers) {
					console.log("QZObj.findPrinters: " + printers);
					printersEL.empty();
					$.each(printers, function (key, value) {
						var attrs = {value: value};
						if (value == prefPrinter) {
							attrs.selected = "selected";
						}
						printersEL.append($("<option>", attrs).text(value));
					});

					//if is Defined focus on Print and Done
					if(typeof focusOnPrintAndDone === "function"){
						focusOnPrintAndDone();
					}
				});
			}
		}


		QZ({
			codebase: "${resource(dir:'.')}",
			signaturePath:  "${createLink(mapping:'qzSignLink')}",
			onSuccess: function () {
				$(".printView").show();
				$(".printViewError").hide();
				window.QZObj.loadPrinters = loadPrinters;
				loadPrinters();
			},
			onFail: function (error) {
				if (error) {
					window.NOTIFICATION.show({
						title: "Error",
						message: error
					}, "error");
				}
			},
			onConnectionError: function (error){
				//swallow Error
			}
		});
	});
</script>
</g:if>
	<g:render template="/assetEntity/initAssetEntityData"/>
	<g:render template="/layouts/error"/>
</body>
</html>
