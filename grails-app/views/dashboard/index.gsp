<%@ page import="net.transitionmanager.project.MoveBundleStep" %>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.UserPreferenceEnum"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="layout" content="topNav" />
	<title>Event Dashboard</title>
	<asset:stylesheet href="css/dashboard.css" />
	<asset:stylesheet href="css/tabcontent.css" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="yahoo.ui.dashboard.js" />
	<g:render template="/layouts/responsiveAngularResources" />
	<g:javascript src="model.manufacturer.js"/>
	<g:javascript src="progressTimer.js" />
	<style>
		/*TODO: REMOVE ON COMPLETE MIGRATION */
		div.content-wrapper {
			background-color: #ecf0f5 !important;
		}
		div.elapsed-time-wrapper span#eventDescription {
			text-align: center;
			font-size: 1.5em;
		}
		div.elapsed-time-wrapper span#eventStartDate {
			text-align: center;
			font-size: 1.3em;
		}
		div.elapsed-time-wrapper div.plannedStart {
			margin-top: 2px;
			margin-bottom: 2px;
			font-weight: bold;
		}
		div.elapsed-time-wrapper span#plannedStart {
			text-align: center;
			font-size: 3em;
		}
		div.elapsed-time-wrapper div.plannedStartLabel {
			padding-left: 0px;
			display: none; /*will be shown by other jquery method.*/
		}
		div.elapsed-time-wrapper div.plannedStartLabel span, div.elapsed-time-wrapper span#eventStringId{
			margin-left: 8%;
			font-style: italic;
		}
		div.elapsed-time-wrapper div.eventRunbook{
			margin-top: 20px;
			display: none; /*will be shown by other jquery method.*/
		}
		div.elapsed-time-wrapper span#eventRunbook{
			margin-left: 5px;
		}
	</style>
</head>
<body>
<tds:subHeader title="Event Dashboard" crumbs="['Dashboard','Event']"/>
<!-- Main content -->
<section class="dashboard-event-wrapper">
	<div>
		<div class="box-body">
			<div class="box box-primary">
				<div class="box-header with-border">
					<g:form action="index" controller="dashboard" name="dashboardForm">
						<div style="float: left; padding-top: 2px;">
							<span>
								<label for="moveEvent"><b>Event:</b></label>&nbsp;
								<select id="moveEvent" name="moveEvent" onchange="submitForm();">
									<g:each status="i" in="${moveEventsList}" var="moveEventInstance">
										<option value="${moveEventInstance?.id}">
											${moveEventInstance?.name}
										</option>
									</g:each>
								</select>
								<tds:hasPermission permission="${Permission.TaskPublish}">
									<span class="checkboxContainer" style="margin-left: 12px;">
										<input type="checkbox" name="viewUnpublished" id="viewUnpublishedId" class="pointer" ${viewUnpublished=='1' ? 'checked="checked"' : ''} onchange="toggleUnpublished(event)"/>
										<label for="viewUnpublishedId" class="pointer">&nbsp;&nbsp;Include Unpublished Tasks</label>
									</span>
								</tds:hasPermission>
							</span>
						</div>
						<div>
							<g:render template="/assetEntity/progressTimerControls" model="${[timerValues:[30, 60, 120, 300, 600]]}"/>
						</div>
						<input type="hidden" id="typeId" value="${params.type}">
						<input type="hidden" id="stateId" value="${params.state}">
						<input type="hidden" id="maxLenId" value="${params.maxLen}">
						<input type="hidden" id="sortId" value="${params.sort}">
						<g:set value="${project?.runbookOn}" var="runbookOn"></g:set>
					</g:form>
				</div><!-- /.box-header -->
				<div>
					<input type="hidden" id="timeBarValueId" value="0"/>
					<div class="taskTimebar hide" id="issueTimebar"  style="float: none !important;">
						<div id="issueTimebarId"></div>
					</div>
					<div class="general-event-info-wrapper">
						<div class="row">
							<div class="col-md-3">
								<div class="gauge-graph-wrapper">
									<div id="summary_gauge_div" align="center">
										<g:if test="${EventDashboardDialOverridePerm}">
											<a href="#manualSummary" onclick="javascript:$('#manualSumStatusSpan').show();">
												<asset:image src="i/dials/dial-50.png" style="border: 0px;" />
											</a>
										</g:if>
										<g:else>
											<asset:image src="i/dials/dial-50.png" style="border: 0px;" />
										</g:else>
									</div>
									Event Status vs. Plan <br />
									<span id="manualSumStatusSpan" style="display: none; width: 10px;">
										<input type="hidden" name="manual" value="M" id="checkBoxId" />
										<input type="text" value="" name="manualSummaryStatus" id="manualSummaryStatusId" size="3" maxlength="3"
											onblur="validateManualSummary(this.value)" />&nbsp;
										<input type="button" value="Save" onclick="changeEventSummary()" />
									</span>
								</div>
							</div>

							<div class="col-md-4">
								<div class="elapsed-time-wrapper">
									<div class="row">
										<div class="col-md-12"><span id="eventDescription"></span></div>
									</div>
									<div class="row">
										<div class="col-md-12"><span id="eventStartDate"></span></div>
									</div>
									<div class="row plannedStart">
										<div class="col-md-12"><span id="plannedStart"></span></div>
									</div>
									<div class="row plannedStartLabel">
										<div class="col-md-11">
											<span>Days</span>
											<span>Hours</span>
											<span>Mins</span>
										</div>
									</div>
									<div class="row">
										<div class="col-md-11"><span id="eventStringId"></span></div>
									</div>
									<div class="row eventRunbook">
										<div class="col-md-12">
											Runbook Status: <span id="eventRunbook"></span>
										</div>
									</div>
								</div>
							</div>
							<!-- START TABAS FOR EVENT, ARCHIVE, ADD NEWS -->
							<div class="col-md-5">
								<div class="tab-items-wrapper">
									<!-- Nav tabs -->
									<ul class="nav nav-tabs" role="tablist">
										<li role="presentation" class="active"><a href="#eventNews" aria-controls="eventNews" role="tab" data-toggle="tab">Event News</a></li>
										<li role="presentation"><a href="#archive" aria-controls="archive" role="tab" data-toggle="tab">Archive</a></li>
										<li role="presentation"><button type="button" class="btn-add-news btn btn-primary" onclick="opencreateNews()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add News</button></li>
									</ul>

									<!-- Tab panes -->
									<div class="tab-content">
										<div role="tabpanel" class="tab-pane active" id="eventNews">
											<ul id="news_live" class="newscroll"></ul>
										</div>
										<div role="tabpanel" class="tab-pane" id="archive">
											<ul id="news_archived" class="newscroll"></ul>
										</div>
									</div>
								</div>
							</div>
							<!-- ENDS TABS FOR EVENT, ARCHIVE, ADD NEWS -->
						</div>
					</div>
				</div>
			</div>
		</div>
		<!-- /.box-body -->
	</div>
</section>

<section class="dashboard-task-summary-wrapper">
	<div>
		<div class="box-body">
			<div class="box box-primary">
				<div class="box-header with-border">
					<h3 class="box-title">Task Summary</h3>
				</div><!-- /.box-header -->
			<!-- News section starts here-->
				<div id="newssection">
					<div id="taskSummary">
						<g:render template="taskSummary" model="[taskCountByEvent:taskCountByEvent, taskStatusMap:taskStatusMap, totalDuration:totalDuration, teamTaskMap:teamTaskMap, roles:roles]"></g:render>
					</div>
				</div>
				<!-- News section ends here-->
				<!-- Bundle Sections starts here-->
				<div id="bdlsection">
					<div id="bdltabs" style="width: ${moveBundleList.size * 130}px;">
						<g:each in="${moveBundleList}" status="i" var="moveBundle">
                            <g:if test="${moveBundle && moveBundle.id}">
                                <span id="spnBundle${moveBundle.id}" class="${ i == 0 ? 'mbhactive' : 'mbhinactive' } tab-item" onClick="updateDash(${moveBundle.id})">
                                    ${moveBundle.name}
                                </span>
                            </g:if>
						</g:each>
					</div>
					<div id="leftcol">
						<ul id="btitle">
							<li>Step</li>
							<li>&nbsp;</li>
							<g:if test="${runbookOn == 1 }">
								<li><span class="percentage">Tasks</span></li>
							</g:if>

							<li>Planned Start</li>
							<li>Planned&nbsp;Completion</li>
							<li>Actual Start</li>
							<li>Actual&nbsp;Completion</li>
						</ul>
					</div>
					<div id="leftarrow">
						<a href="#" id="move-left">
							<asset:image src="images/left_arrow.png" alt="back" border="0" width="16" height="23" align="right" />
						</a>
					</div>
					<div class="mod" style="overflow: hidden;">
						<div id="themes" style="position:relative;">
							<input type="hidden" value="${moveBundleList ? moveBundleList[0]?.id : ''}" id="defaultBundleId">
							<g:each in="${moveBundleList}" status="i" var="moveBundle">
								<div id="bundlediv${moveBundle.id}" class="${i == 0 ? 'show_bundle_step' : 'hide_bundle_step'}">
									<g:each in="${MoveBundleStep.findAll('FROM MoveBundleStep mbs where mbs.moveBundle='+moveBundle.id+' ORDER BY mbs.transitionId')}"
											status="j" var="moveBundleStep">
										<div style="float: left; width: 130px;">
											<ul class="bdetails">
												<li class="heading" title="${moveBundleStep.label}">
													<g:if test="${moveBundleStep.label.length()>10}">
														${moveBundleStep.label.substring(0,11)}..
													</g:if>
													<g:else>
														${moveBundleStep.label}
													</g:else>
												</li>
												<li id="percentage_${moveBundle.id}_${moveBundleStep.transitionId}"></li>
												<g:if test="${runbookOn == 1 }">
													<li class="tasks" id="tasks_${moveBundle.id}_${moveBundleStep.transitionId}">&nbsp</li>
												</g:if>
												<li class="schstart">
													<span id="plan_start_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;
												</li>
												<li class="schfinish">
													<span id="plan_completion_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;
												</li>
												<li class="actstart" id="li_start_${moveBundle.id}_${moveBundleStep.transitionId}">
													<div id="act_start_${moveBundle.id}_${moveBundleStep.transitionId}" title=""></div>
												</li>
												<li class="actfinish" id="li_finish_${moveBundle.id}_${moveBundleStep.transitionId}">
													<div id="act_completion_${moveBundle.id}_${moveBundleStep.transitionId}" title=""></div>
												</li>
											</ul>
											<!-- <div id="chartdiv_${moveBundle.id}_${moveBundleStep.transitionId}" align="center" style="display: none;">
												<img id="chart_${moveBundle.id}_${moveBundleStep.transitionId}"
																	src="${resource(dir:'i/dials',file:'dial-50sm.png')}">
														</div> -->
										</div>
									</g:each>
								</div>
							</g:each>
						</div>
					</div>
					<div id="rightarrow">
						<a href="#" id="move-right">
							<asset:image src="images/right_arrow.png" alt="back" border="0" width="16" height="23" align="right" />
						</a>
					</div>
				</div>
				<div style="text-align: right; padding: 4px 0px;">
					<%--<a href="#page_up" class="nav_button" style="nowrap:nowrap;">Page Up</a> --%>
				</div>
			</div>
		</div>
		<!-- /.box-body -->
	</div>
</section>
<div id="createNews" title="Create News" style="display: none;" class="static-dialog">
	<form id="createNewsForm">
		<input type="hidden" name="mode" value="ajax">
		<input type="hidden" name="moveEventId" value="${moveEvent?.id}" id="moveEventId">
		<div class="dialog" style="border: 1px solid #5F9FCF">
			<table id="createCommentTable" style="border: 0px">
				<tr>
					<td colspan="2"><div class="required">Fields marked ( * ) are mandatory</div></td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name">
						<label for="messageNews">
							<b>Message:&nbsp;
								<span style="color: red">*</span>
							</b>
						</label>
					</td>
					<td valign="top" class="value">
						<textarea cols="80" rows="5"
								  id="messageNews" name="message"
								  onkeydown="textCounter(this.id,255)"
								  onkeyup="textCounter(this.id,255)"></textarea>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name" nowrap="nowrap">
						<label for="archivedTdId">Resolved / Archived:</label>
					</td>
					<td valign="top" class="value" id="archivedTdId">
						<input type="checkbox" id="isArchivedId" value="0" onclick="updateHidden('isArchivedId','isArchivedHiddenId')" />
						<input type="hidden" name="isArchived" value="0" id="isArchivedHiddenId" />
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name">
						<label for="resolutionNews">Resolution:</label>
					</td>
					<td valign="top" class="value">
						<textarea cols="80" rows="5"
								  id="resolutionNews" name="resolution"
								  onkeydown="textCounter(this.id,255)"
								  onkeyup="textCounter(this.id,255)"></textarea>
					</td>
				</tr>
			</table>
		</div>
		<div class="buttons">
			<span class="button">
				<input class="save" type="button" value="Create" onclick="return submitCreateNewsForm()" />
			</span>
			<span class="button">
				<input class="cancel" type="button" value="Cancel" onclick="resetCreateNewsForm();" />
			</span>
		</div>
	</form>
</div>

<div id="showEditCommentDialog" title="Edit News" style="display: none;" class="static-dialog">
	<form id="editNewsForm">
		<input type="hidden" name="id" value="" id="commentId">
		<input type="hidden" name="mode" value="ajax">

		<div class="dialog" style="border: 1px solid #5F9FCF">
			<div>
				<table id="showCommentTable" style="border: 0px">
					<tr>
						<td valign="top" class="name">
							<label for="dateCreatedId">Created At:</label>
						</td>
						<td valign="top" class="value" id="dateCreatedId"></td>
					</tr>
					<tr>
						<td valign="top" class="name">
							<label for="createdById">Created By:</label>
						</td>
						<td valign="top" class="value" id="createdById"></td>
					</tr>
					<tr>
						<td valign="top" class="name">
							<label>Comment Type:</label>
						</td>
						<td valign="top" class="value">
							<select disabled="disabled" id="commentTypeOption">
								<option>Issue</option>
							</select>
						</td>
					</tr>
					<tr id="displayOptionTr">
						<td valign="top" class="name" nowrap="nowrap">
							<label for="displayOption">User / Generic Cmt:</label>
						</td>
						<td valign="top" class="value" id="displayOption">
							<input type="radio" name="displayOption" value="U" checked="checked" id="displayOptionUid" />&nbsp;
							<span style="vertical-align: text-top;">User Comment</span>&nbsp;&nbsp;&nbsp;
							<input type="radio" name="displayOption" value="G" id="displayOptionGid" />&nbsp;
							<span style="vertical-align: text-top;">Generic Comment&nbsp;</span>
						</td>
					</tr>
					<tr class="prop" id="assetTrId">
						<td valign="top" class="name">
							<label for="assetTdId">Asset:</label>
						</td>
						<td valign="top" class="value">
							<input type="text" disabled="disabled" id="assetTdId" />
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="commentTdId">Message:</label>
						</td>
						<td valign="top" class="value">
							<textarea cols="80" rows="5"
									  id="commentTdId" name="message"
									  onkeydown="textCounter(this.id,255)"
									  onkeyup="textCounter(this.id,255)"></textarea>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name" nowrap="nowrap">
							<label for="resolveTdId">Resolved / Archived:</label>
						</td>
						<td valign="top" class="value" id="resolveTdId">
							<input type="checkbox" id="isResolvedId" value="0" onclick="updateHidden('isResolvedId','isResolvedHiddenId')" />
							<input type="hidden" name="isArchived" value="0" id="isResolvedHiddenId" />
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="resolutionId">Resolution:</label>
						</td>
						<td valign="top" class="value">
							<textarea cols="80" rows="5"
									  id="resolutionId" name="resolution"
									  onkeydown="textCounter(this.id,255)"
									  onkeyup="textCounter(this.id,255)"></textarea>
						</td>
					</tr>
					<tr>
						<td valign="top" class="name">
							<label for="dateResolvedId">Resolved At:</label>
						</td>
						<td valign="top" class="value" id="dateResolvedId"></td>
					</tr>
					<tr>
						<td valign="top" class="name">
							<label for="resolvedById">Resolved By:</label>
						</td>
						<td valign="top" class="value" id="resolvedById"></td>
					</tr>
				</table>
			</div>
			<div class="buttons">
				<span class="button">
					<input class="save" type="button" value="Update" onclick="return submitUpdateNewsForm()" />
				</span>
				<span class="button">
					<input class="delete" type="button" value="Delete" onclick="return submitDeleteNewsForm()" />
				</span>
				<span>
					<input type="button" class="cancel" value="Cancel" onclick="$('#showEditCommentDialog').dialog('close');">
				</span>
			</div>
		</div>
	</form>
</div>
<script type="text/javascript">
	var eventType = "load";
	var hasTimedOut = false;
	var modWidth
	var tz = '${tds.timeZone()}'
	var stepWidth = 130;
	var totalSteps = $('div.show_bundle_step').children().size();
	var progressTimer;

	$(document).ready(function() {
		progressTimer = new ProgressTimer(60, '${com.tdsops.tm.enums.domain.UserPreferenceEnum.EVENTDB_REFRESH}', refreshDashboard);

		$("#showEditCommentDialog").dialog({autoOpen: false});
		$("#createNews").dialog({autoOpen: false});
		setStepsWidth();

		$(window).resize(function() {
			if (hasTimedOut != false) {
				clearTimeout(hasTimedOut);
			}
			hasTimedOut = setTimeout(function() {
				AditionalFrames = 1;
				stepCount = 1;
				if ($("#defaultBundleId").val() && $("#defaultBundleId").val() > 0) {
					updateDash( $("#defaultBundleId").val() );
                }
			}, 100);
		});
		// used to call the function once page loaded
		getMoveEventNewsDetails($('#moveEvent').val());
		moveDataSteps();

        // standard fix to add a close icon on popup dialogs
        $('.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
	});

    var numCols = $('div.show_bundle_step').children().length;
    var themes = $('#themes');
    var left = $('#move-left').find('img');
    var right = $('#move-right').find('img');

    if (numCols <= 7) {
    	$('#move-left').find('img').addClass('disabled');
        $('#move-right').find('img').addClass('disabled');
    } else {
        $('#move-right').find('img').removeClass('disabled');
	}


    $("#move-left").click(function (event) {
        event.preventDefault();

        if (numCols <= 7) {
            return;
        } else {
            //Maximum displacement multiple
            var maxMultiple = numCols - 7;

            //Read the offset of #themes
            var offset = parseInt(themes.css('left'));

            if (offset < 0) {
                //Move #themes one more step to the right
                offset = offset + 130;
                if (offset >= 0) {
                    left.addClass('disabled');
                }
                themes.css('left', offset + 'px');
                right.removeClass('disabled'); //Enable right arrow
            } else {
                left.addClass('disabled');
			}
		}
    });

	$("#move-right").click(function (event) {
	    event.preventDefault();

	    if (numCols <= 7) {
	        return;
		} else {
	        //Maximum displacement multiple
			var maxMultiple = numCols - modWidth;

	        //Read the offset of #themes
			var offset = parseInt(themes.css('left'));

			if (offset > -maxMultiple*130) {
			    //Move #themes one more step to the left
				offset = offset - 130;
                if (offset <= -maxMultiple*130) {
                    right.addClass('disabled');
                }
				themes.css('left', offset + 'px');
                left.removeClass('disabled'); //Activate left arrow
			} else {
                right.addClass('disabled');  //Disable right arrow
			}
		}
    });

	function refreshDashboard () {
		getMoveEventNewsDetails($('#moveEvent').val());
		updateTaskSummary();
		if (typeof progressTimer !== 'undefined')
			progressTimer.resetTimer();
	}

	function submitForm (event) {
		if ($('#viewUnpublishedId').is(':checked')) {
			$('#viewUnpublishedId').attr('value', '1');
			$('#viewUnpublishedId').attr('checked', 'checked');
			$('#dashboardForm').submit();
		} else {
			$('#viewUnpublishedId').attr('value', '0');
			$('#viewUnpublishedId').attr('checked', 'checked');
			$('#dashboardForm').submit();
			$('#viewUnpublishedId').removeAttr('checked');
		}
	}

	function toggleUnpublished (e) {
		var checkedValue = $(e.srcElement).is(':checked');
		setUserPreference('${UserPreferenceEnum.VIEW_UNPUBLISHED.name()}', checkedValue, function () {
			refreshDashboard();
		});
	}

	// recalculates the width of the container holding the step columns
	function setStepsWidth () {
		totalSteps = $('div.show_bundle_step').children().size();
		var maxWidth = $('div#bdlsection').outerWidth();
		var labelsWidth = $('div#leftcol').outerWidth();
		var arrowWidth = $('div#leftarrow').outerWidth();
		var baseWidth = maxWidth - labelsWidth - arrowWidth * 2;
		modWidth = Math.min(parseInt(baseWidth / stepWidth), totalSteps);
		$(".mod").css("width", modWidth * 130 + "px");
        themes.css("width",totalSteps * 130 + "px");
		checkArrowStatus();
	}

	// greys out the arrows if they cannot be used
	function checkArrowStatus () {
		var themes = $('div#themes');
		var lastStep = $('div.show_bundle_step').children().last();
		var rightArrow = $('div#rightarrow a img');
		var leftArrow = $('div#leftarrow a img');

		// check left arrow
		if (themes.css('left') == '0px' || themes.css('left') == 'auto')
			leftArrow.addClass('disabled');
		else
			leftArrow.removeClass('disabled');

		// check right arrow
		if(typeof lastStep.offset() != 'undefined' ){
			if (lastStep.offset().left + lastStep.outerWidth() - rightArrow.offset().left < 10)
				rightArrow.addClass('disabled');
			else
				rightArrow.removeClass('disabled');
		}
	}

	if (tz) {
		$("#timezone option:contains(" + tz + ")").attr("selected","selected");
	} else {
		$("#timezone option:contains(EDT)").attr("selected","selected");
	}
	if ('${timeToUpdate}') {
		$("#updateTimeId").val("${timeToUpdate}")
	}
	var sURL = unescape(window.location);
	var timer
	var errorCode = '200'
	var dialReload = true;

	/*---------------------------------------------------
	 * Script to load the marquee to scroll the live news
	 *--------------------------------------------------*/
	<%-- marqueeInit({
        uniqueid: 'mycrawler',
        inc: 8, //speed - pixel increment for each iteration of this marquee's movement
        mouse: 'cursor driven', //mouseover behavior ('pause' 'cursor driven' or false)
        moveatleast: 4,
        neutral: 150,
        savedirection: false
    }); --%>

	/*-----------------------------------------------
	 * function to move the data steps to right / left
	 *----------------------------------------------*/
	var AditionalFrames = 1;
	var stepCount = 1;
	var defaultBundle = $("#defaultBundleId").val();
	function moveDataSteps () {
		YAHOO.example = function() {

			var $D = YAHOO.util.Dom;
			var $E = YAHOO.util.Event;
			var $A = YAHOO.util.Anim;
			var $M = YAHOO.util.Motion;
			var $DD = YAHOO.util.DD;
			var $ = $D.get;
			var x = 1;
			var bundle = defaultBundle;

			return {
				init : function() {
					$E.on(['move-left','move-right'], 'click', this.move);
				},
				move : function(e) {
					$E.stopEvent(e);
					if( bundle != defaultBundle ){
						stepCount = 1
						bundle = defaultBundle;
					}
					switch(this.id) {
						case 'move-left':
							if ( stepCount == 1 ) {
								return;
							}
							var attributes = {
								points : {
									by : [130, 0]
								}
							};
							stepCount--;
							break;
						case 'move-right':
							if ( stepCount == AditionalFrames ) {
								return;
							}
							var attributes = {
								points : {
									by : [-130, 0]
								}
							};
							stepCount++;
							break;
					};
					var anim = new $M('themes', attributes, 0.1, YAHOO.util.Easing.easeOut);
					anim.onComplete.subscribe(checkArrowStatus);
					anim.animate();
				}
			};
		}();
		YAHOO.util.Event.onAvailable('doc',YAHOO.example.init, YAHOO.example, true);
	}

	/* script to assign the event value*/
	var moveEvent = "${moveEvent?.id}"
	if (moveEvent) {
		$("#moveEvent").val(moveEvent)
	}

	/* Function to load the data for a particular MoveEvent */
	function getMoveEventNewsDetails (moveEvent) {
		$("#createNews").dialog("close");
		$("#messageNews").val("");
		$("#resolutionNews").val("");
		$('#isArchivedId').attr("checked",false)
		$('#isResolvedId').attr("checked",false)
		$("#showEditCommentDialog").dialog("close");
		updateDash( $("#defaultBundleId").val() );
		/*		<%--	if(dialReload && doUpdate){
							timer = setTimeout( "getDialsData($('#defaultBundleId').val() )", 5000 );
						}--%>*/
		if (moveEvent) {
			jQuery.ajax({
				type:"GET",
				async : true,
				cache: false,
				url:"../ws/moveEventNews/"+moveEvent+"?type="+$("#typeId").val()+"&state="+$("#stateId").val()+"&maxLen="+$("#maxLenId").val()+"&sort="+$("#sortId").val(),
				dataType: 'json',
				success:updateMoveEventNews,
				error:function (xhr, ajaxOptions, thrownError){
					if (errorCode ==  xhr.status) {
						if (xhr.status == "403") {
							alert("403 Forbidden occurred, user don't have permission to load the current project data.");
						}
					} else {
						errorCode =  xhr.status;
					}
				}
			});
		}
	}
	/* Update the news once ajax call success*/
	function updateMoveEventNews (news) {

		var newsLength = news.length;
		var live = "";
		var archived = "";
		var scrollText = " ";
		for (i = 0; i< newsLength; i++) {
			var state = news[i].state;
			var liId = news[i].type+"_"+news[i].id
			var createdTime = tdsCommon.parseAndFormatDateTimeFromZulu(news[i].created)
			if (state == "A") {
				archived +=	"<li id="+liId+" onclick='openEditNewsDialog(this.id)'><span class='newstime'>"+createdTime+" :</span> <span class='normaltext Arrowcursor'>"+news[i].text+"</span></li>";
			} else {
				live +=	"<li id="+liId+" onclick='openEditNewsDialog(this.id)'><span class='newstime'>"+createdTime +" :</span> <span class='normaltext Arrowcursor'>"+news[i].text+"</span></li>";
				scrollText += createdTime+" : "+news[i].text +". "
			}
		}
		$("#news_live").html(live);
		$("#news_archived").html(archived);
		$("#head_mycrawlerId").html(scrollText)

	}

	function updateTaskSummary () {
		jQuery.ajax({
			type:"POST",
			async : true,
			data: $('#teamTaskPercentageFormId').serialize(),
			url:"../dashboard/taskSummary/"+moveEvent,
			success:function (data){
				$("#taskSummary").html(data);
			},
			error:function (xhr, ajaxOptions, thrownError){
				if (errorCode ==  xhr.status) {
					if (handler) {
						clearInterval(handler);
                    }
					$("#update").css("color","red");
					if( xhr.status == "403"){
						alert("403 Forbidden occurred, user don't have permission to load the current project data.");
					}
				} else {
					errorCode =  xhr.status ;
				}
			}
		});
	}

	/* function to load the user agent*/
	if (navigator.appName == "Microsoft Internet Explorer") {
		$("#content").css("top",0)
	}
	var speed = 10
	var crossobjTop = $("#content").css("top")

	function movedown () {

		var crossobj = $("#content")
		var contentheight = crossobj.height()
		if ( parseInt(crossobj.css("top")) >= (contentheight - 60)*(-1) ){
			crossobj.css("top",parseInt(crossobj.css("top"))-speed+"px")
		}
	}

	function moveup () {

		var crossobj = $("#content")
		var contentheight = crossobj.height()
		if (parseInt(crossobj.css("top"))<=-15) {
			crossobj.css("top",parseInt(crossobj.css("top"))+speed+"px")
		}
	}

	/*-----------------------------------------------------------
	 * functions to convert Date & Time into respective timezones
	 *-----------------------------------------------------------*/

	/* display bundle tab and call updateDash method to load the appropriate data*/
	function displayBundleTab (Id) {
		$(".mbhactive").attr("class","mbhinactive tab-item");
		$("#spnBundle"+Id).attr("class","mbhactive tab-item");
		$(".show_bundle_step").attr("class","hide_bundle_step");
		$("#bundlediv"+Id).attr("class","show_bundle_step");
		$("#defaultBundleId").val(Id);
	}
	/*----------------------------------------
	 *
	 *--------------------------------------*/

	function updateDash (bundleId) {
		var moveEvent = $("#moveEvent").val()
		if (bundleId && moveEvent) {
			displayBundleTab( bundleId )
			jQuery.ajax({
				type:"GET",
				async : true,
				cache: false,
				url:"../ws/dashboard/bundleData/"+ bundleId+"?moveEventId="+moveEvent,
				dataType: 'json',
				success:updateMoveBundleSteps,
				error:function (xhr, ajaxOptions, thrownError){
					if (errorCode ==  xhr.status ) {
						if (xhr.status == "403") {
							alert("403 Forbidden occurred, user don't have permission to load the current project data.");
						}
					} else {
						errorCode = xhr.status;
					}
				}
			});
		}
		setStepsWidth();
	}

	function onEvenNewstHeaderLoad(e) {
		var newsAndStatus = JSON.parse(e.responseText);
		$("#head_mycrawlerId").html(newsAndStatus[0].news);
		$("#head_crawler").addClass(newsAndStatus[0].cssClass)
		$("#moveEventStatus").html(newsAndStatus[0].status)
	}

	/* update bundle data once ajax call success */

	function updateMoveBundleSteps (bundleMap) {
		try {
			var snapshot = bundleMap.snapshot;
			var runbookOn = snapshot.runbookOn;
			var moveBundleId = snapshot.moveBundleId;
			var calcMethod = snapshot.calcMethod;
			var steps = snapshot.steps;
			var revSum = snapshot.revSum;
			var planSum = snapshot.planSum;
			var eventStartDate = snapshot.eventStartDate;
			var sumDialInd =  planSum.dialInd ? planSum.dialInd : 50;
			if(sumDialInd < 0){
				sumDialInd = 0
			}
			if(AditionalFrames == 1 || defaultBundle != moveBundleId){
				visibleSteps = modWidth;
				AditionalFrames = ( totalSteps > visibleSteps ? ((totalSteps - visibleSteps) + 1) : 1 );
				$("#themes").css("left","0px");
			}
			defaultBundle = moveBundleId;
			if( sumDialInd < 25){
				$(".statusbar_good").attr("class","statusbar_bad")
				$(".statusbar_yellow").attr("class","statusbar_bad")
				$("#moveEventStatus").html("RED")
			} else if( sumDialInd >= 25 && sumDialInd < 50){
				$(".statusbar_good").attr("class","statusbar_yellow");
				$(".statusbar_bad").attr("class","statusbar_yellow");
				$("#moveEventStatus").html("YELLOW")
			} else {
				$(".statusbar_bad").attr("class","statusbar_good")
				$(".statusbar_yellow").attr("class","statusbar_good")
				$("#moveEventStatus").html("GREEN")
			}
			updateSummaryGauge("summary_gauge", sumDialInd);
			if(calcMethod == "M") {
				$('#checkBoxId').attr("checked","checked");
			} else {
				$('#checkBoxId').removeAttr("checked");
			}
			$("#manualSummaryStatusId").val( sumDialInd );
			$("div.plannedStartLabel").show();
			$("#eventStartDate").html(eventStartDate);
			$("#plannedStart").html(planSum.dayTime);
			$("#eventDescription").html(planSum.eventDescription);
			$("#eventStringId").html(planSum.eventString);
			$("div.eventRunbook").show();
			$("#eventRunbook").html(planSum.eventRunbook);

			var taskManagerUrl = contextPath + "/assetEntity/listTasks?bundle=" + moveBundleId + "&justRemaining="
			// handle the calculations for each step
			for( i = 0; i < steps.length; i++ ) {
				$("#percentage_"+moveBundleId+"_"+steps[i].tid).html(isNaN(steps[i].tskComp / steps[i].tskTot) ? 0+ "%" : parseInt( (steps[i].tskComp / steps[i].tskTot ) * 100 ) +"%");
				$("#percentage_"+moveBundleId+"_"+steps[i].tid).attr("class",steps[i].percentageStyle)


				var remainingTasksNumber = 0
				var totalTasksNumber = 0
				if(!isNaN(steps[i].tskComp / steps[i].tskTot)){
					remainingTasksNumber = steps[i].tskComp
					totalTasksNumber = steps[i].tskTot
				}
				var firstUrl = taskManagerUrl + "1&step=" +steps[i].wfTranId
				var secondUrl = taskManagerUrl + "0&step=" +steps[i].wfTranId
				var linksHtml = "<a href=\""+ firstUrl +"\">" + remainingTasksNumber + "</a> (of <a href=\"" + secondUrl+ "\">" + totalTasksNumber + "</a>)"

				$("#tasks_"+moveBundleId+"_"+steps[i].tid).html(linksHtml);

				$("#plan_start_"+moveBundleId+"_"+steps[i].tid).html(tdsCommon.parseAndFormatDateTimeFromZulu(steps[i].planStart));
				$("#plan_completion_"+moveBundleId+"_"+steps[i].tid).html(tdsCommon.parseAndFormatDateTimeFromZulu(steps[i].planComp));
				var startDelta = 0
				var actDelta = 0
				if( steps[i].actStart ){
					var actStartTime = tdsCommon.parseDateTimeFromZulu(steps[i].actStart).valueOf()
					var planStartTime = tdsCommon.parseDateTimeFromZulu(steps[i].planStart).valueOf()
					startDelta = parseInt((actStartTime - planStartTime)/60000);
					if(startDelta > 0){
						$("#li_start_"+moveBundleId+"_"+steps[i].tid).removeClass("actstart");
						$("#li_start_"+moveBundleId+"_"+steps[i].tid).addClass("actstart_red");
					}
				}
				var actStartFormatted = tdsCommon.parseAndFormatDateTimeFromZulu(steps[i].actStart)
				if (startDelta > 0) {
					actStartFormatted += ": ("+ steps[i].startOverdueDuration +")"
				}
				$("#act_start_"+moveBundleId+"_"+steps[i].tid).html(actStartFormatted);
                $("#act_start_"+moveBundleId+"_"+steps[i].tid).attr('title', actStartFormatted);
				if( steps[i].actStart && !steps[i].actComp && steps[i].calcMethod != "M" && ${runbookOn} != 1) {
					$("#act_completion_"+moveBundleId+"_"+steps[i].tid).html("<span id='databox'>Total Devices "+steps[i].tskTot+" Completed "+steps[i].tskComp+"</span>")
				} else {
					var actCompTime = tdsCommon.parseDateTimeFromZulu(steps[i].actComp).valueOf()
					var planCompTime = tdsCommon.parseDateTimeFromZulu(steps[i].planComp).valueOf()
					actDelta = parseInt((actCompTime - planCompTime)/60000);
					if(actDelta > 0){
						$("#li_finish_"+moveBundleId+"_"+steps[i].tid).removeClass("actfinish");
						$("#li_finish_"+moveBundleId+"_"+steps[i].tid).addClass("actfinish_red");
					}
					var actCompFormatted = tdsCommon.parseAndFormatDateTimeFromZulu(steps[i].actComp)
					if (actDelta > 0) {
						actCompFormatted += ": ("+ steps[i].startOverdueDuration +")"
					}
					$("#act_completion_"+moveBundleId+"_"+steps[i].tid).html(actCompFormatted);
                    $("#act_completion_"+moveBundleId+"_"+steps[i].tid).attr('title',actCompFormatted);
				}
				var percentage = $("#percentage_"+moveBundleId+"_"+steps[i].tid).html()
				if(percentage != "100%" && percentage != "0%"){
                    $("#chartdiv_"+moveBundleId+"_"+steps[i].tid ).show();
                    post_init( "chart_"+moveBundleId+"_"+steps[i].tid, steps[i].dialInd )
                    // post_init( "chart_'+moveBundleId+'_'+steps[i].tid+'", '+steps[i].dialInd+' )
                } else {
                    $("#chartdiv_"+moveBundleId+"_"+steps[i].tid ).hide();
                }
            }
            //Append recent changes to status bar
            ${remoteFunction(controller:'moveEvent', action:'retrieveMoveEventNewsAndStatus', params:'\'id=\' + moveEvent',onComplete:'onEvenNewstHeaderLoad(XMLHttpRequest)')}
            setStepsWidth();
            //$("#bdltabs").css("width",$(".mod").css("width"));
        } catch (ex) {
        }

    }

    /* function to render the dials */
    function post_init( divId, dialInd ){
        var dInd = dialInd % 2 == 0 ? dialInd : dialInd+1
        var src = "/i/dials/dial-"+dInd+"sm.png";
        $("#"+divId).attr("src", src);
        $("#"+divId).attr("title", dialInd);

    }
    function updateSummaryGauge( divId, dialInd ){
        var dInd = dialInd % 2 == 0 ? dialInd : dialInd+1
        var src = "/i/dials/dial-"+dInd+".png";
        $("#"+divId).attr("src", src);
        $("#"+divId).attr("title", dialInd);
        <%--//var myChart = new FusionCharts("${resource(dir:'swf',file:'AngularGauge.swf')}", "myChartId", "280", "136", "0", "0");
        updateChartXML(divId, summaryDialData( dialInd ) );
        //myChart.setDataXML( xmlData );
        //myChart.render(divId);--%>
	}
	/*
	 will popup the dialog to create news
	 */
	function opencreateNews(){
		progressTimer.Pause();
		$("#createNews").dialog('option', 'width', 'auto');
		$("#createNews").dialog('option', 'position', ['center','top']);
		$("#createNews").dialog('option', 'modal', 'auto');
		$("#showEditCommentDialog").dialog("close");
		$('#createNews').dialog('open');
		$('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
	}

	$('div#createNews').on('dialogclose', function() {
		progressTimer.attemptResume();;
	});

	function updateHidden(checkBoxId,hiddenId){
		var resolve = $("#"+checkBoxId).is(':checked');
		if (resolve) {
			$("#"+hiddenId).val(1);
		} else {
			$("#"+hiddenId).val(0);
		}
	}

	/*
	 * Used to validate the create news form and submit to
	 */
	function submitCreateNewsForm() {
		var moveEvent = $("#moveEventId").val();
		var resolveBoo = $("#isResolvedId").is(':checked');
		var resolveVal = $("#resolutionNews").val();
		var news = $("#messageNews").val()

		var validate = false;
		if (moveEvent) {
			if (resolveBoo && resolveVal == "") {
				alert('Please enter Resolution');
			} else if ( !news ) {
				alert('Please enter Comment');
			} else {
				validate = true;
			}
		} else {
			alert("Please select an event before creating a news message");
		}

		if (validate) {
			var form=$('#createNewsForm');
			if (form.length) {
				jQuery.ajax({
					url: tdsCommon.createAppURL('/newsEditor/saveNews'),
					data: form.serialize(),
					type:'POST',
					success: function(response) {
						if (response.status == 'success') {
							$('#createNews').dialog('close');
							getMoveEventNewsDetails(moveEvent);
						} else {
							alert("Save failed - " + response.errors );
						}
					},
					error: function(jqXHR, textStatus, errorThrown ) {
						alert('Save errored - ' + errorThrown);
					}
				});
			} else {
				console.log('Error: Unable to locate createNewsForm in submitCreateNewsForm');
			}
			/*
			${remoteFunction(controller:'newsEditor',action:'saveNews', params:'\'moveEvent.id=\' + moveEvent +\'&message=\'+ news +\'&isArchived=\'+$(\'#isArchivedHiddenId\').val()+\'&resolution=\'+$(\'#resolutionNews\').val()+\'&isResolved=\'+$(\'#isResolvedHiddenId\').val()', onComplete:'getMoveEventNewsDetails(moveEvent)')}
			*/
		}
		progressTimer.attemptResume();
	}

	/*
	 * Called when the user clicks cancel from the Create New Form
	 */
	function resetCreateNewsForm(){
		$("#messageNews").val("");
		$('#isArchivedHiddenId').val("0");
		$('#resolutionNews').val("")
		$('#isArchivedId').attr("checked",false)
		$('#createNews').dialog('close');
		progressTimer.attemptResume();
	}

	/* will popup the dialog to edit news */
	function openEditNewsDialog( newsId ){
		var idArray = newsId.split("_")
		var type = idArray [0]
		var id = idArray [1]
		${remoteFunction(controller:'newsEditor', action:'retrieveCommetOrNewsData',params:'\'id=\' + id +\'&commentType=\'+type', onComplete:'showEditNewsForm( XMLHttpRequest )')}
	}

	/**
	 * Used to populate the News Edit Form
	 * @param  Ajax/JSON response object
	 */
	function showEditNewsForm( e ){
		progressTimer.resetTimer();
		var assetComments = eval('(' + e.responseText + ')');
		if (assetComments) {

			$('#commentId').val(assetComments[0].commentObject.id)
			$('#assetTdId').val(assetComments[0].assetName)
			$('#dateCreatedId').html(assetComments[0].dtCreated);
			if (assetComments[0].personResolvedObj != null) {
				$('#resolvedById').html(assetComments[0].personResolvedObj);
			} else {
				$('#resolvedById').html("");
				$('#resolvedByEditId').html("");
			}
			$('#createdById').html(assetComments[0].personCreateObj);
			$('#resolutionId').val(assetComments[0].commentObject.resolution);

			if (assetComments[0].commentObject.commentType != 'issue') {

				$('#commentTypeId').val("news")
				$('#dateResolvedId').html(assetComments[0].dtResolved);
				$('#isResolvedId').val(assetComments[0].commentObject.isArchived)
				$('#commentTdId').val(assetComments[0].commentObject.message)
				if (assetComments[0].commentObject.isArchived != 0) {
					$('#isResolvedId').attr('checked', true);
					$("#isResolvedHiddenId").val(1);
				} else {
					$('#isResolvedId').attr('checked', false);
					$("#isResolvedHiddenId").val(0);
				}
				$("#displayOptionTr").hide();
				$("#commentTypeOption").html("<option>News</option>");
				$("#assetTrId").hide();
				$("#showEditCommentDialog").dialog('option','title','Edit News');

			} else {

				$('#commentTypeId').val("issue")
				$('#dateResolvedId').html(assetComments[0].dtResolved);
				$('#isResolvedId').val(assetComments[0].commentObject.isResolved)
				$('#commentTdId').val(assetComments[0].commentObject.comment)
				if (assetComments[0].commentObject.isResolved != 0) {
					$('#isResolvedId').attr('checked', true);
					$("#isResolvedHiddenId").val(1);
				} else {
					$('#isResolvedId').attr('checked', false);
					$("#isResolvedHiddenId").val(0);
				}
				if (assetComments[0].commentObject.displayOption == "G") {
					$("#displayOptionGid").attr('checked', true);
				} else {
					$("#displayOptionUid").attr('checked', true);
				}
				$("#displayOptionTr").show();
				$("#commentTypeOption").html("<option>Issue</option>");
				$("#assetTrId").show();
				$("#showEditCommentDialog").dialog('option','title','Edit Issues Comment');

			}

			$("#showEditCommentDialog").dialog('option', 'width', 'auto');
			$("#showEditCommentDialog").dialog('option', 'position', ['center','top']);
			$("#showEditCommentDialog").dialog('option', 'modal', 'auto');
			$("#showEditCommentDialog").dialog("open");
			$("#createNews").dialog("close");
		}
	}

	/*
	 * used to validate and submit the Edit News Form to the server for updating
	 */
	function submitUpdateNewsForm() {
		var id = $("#commentId").val();
		var moveEvent = $("#moveEventId").val();	// Note that this comes from separate field

		var form = $('#editNewsForm');
		if (form.length) {
			jQuery.ajax({
				url: tdsCommon.createAppURL('/newsEditor/updateNews'),
				data: form.serialize(),
				type:'POST',
				success: function(response) {
					if (response.status == 'success') {
						$('#showEditCommentDialog').dialog('close');
						getMoveEventNewsDetails(moveEvent);
					} else {
						alert("Update failed - " + response.errors );
					}
				},
				error: function(jqXHR, textStatus, errorThrown ) {
					alert('Update errored - ' + errorThrown);
				}
			});
		} else {
			console.log('Error: Unable to locate updateNewsForm in submitUpdateNewsForm');
		}
	}

	/*
	 * used to validate and submit the Edit News Form to the server for updating
	 */
	function submitDeleteNewsForm() {
		var id = $("#commentId").val();
		var moveEvent = $("#moveEventId").val();	// Note that this comes from separate field

		var form = $('#editNewsForm');
		if (form.length) {
			jQuery.ajax({
				url: tdsCommon.createAppURL('/newsEditor/deleteNews'),
				data: form.serialize(),
				type:'POST',
				success: function(response) {
					if (response.status == 'success') {
						$('#showEditCommentDialog').dialog('close');
						getMoveEventNewsDetails(moveEvent);
					} else {
						alert("Delete failed - " + response.errors );
					}
				},
				error: function(jqXHR, textStatus, errorThrown ) {
					alert('Delete errored - ' + errorThrown);
				}
			});
		} else {
			console.log('Error: Unable to locate updateNewsForm in submitDeleteNewsForm');
		}
	}


	// validate the manual summary input value
	function validateManualSummary(value){
		var check = true;
		if ( !isNaN(parseFloat(value)) && isFinite(value) ) {
			if(value > 100){
				alert("Summary status should not be greater than 100");
				check = false;
			}

            if(value < 0){
                alert("Summary status should not be lesser than 0");
                check = false;
            }

		} else {
			alert("Summary status should be Alpha Numeric ");
			check = false;
		}
		return check;
	}
	// send the request to update the manual summary value if it is valid
	function changeEventSummary(){
		var value = $("#manualSummaryStatusId").val();
		if (validateManualSummary( value )) {
			//var checkbox = $('#checkBoxId').is(":checked");
			var checkbox = true;
			var moveEvent = $("#moveEventId").val();
			${remoteFunction(controller:'moveEvent',action:'updateEventSumamry', params:'\'moveEventId=\'+moveEvent+\'&value=\'+value+\'&checkbox=\'+checkbox', onComplete:'updateDash( $("#defaultBundleId").val() )')};
			$("#manualSumStatusSpan").hide();
		}
	}
	/*
	 * validate the text area size
	 */
	function textCounter(fieldId, maxlimit) {
		var value = $("#"+fieldId).val()
		if (value.length > maxlimit) { // if too long...trim it!
			$("#"+fieldId).val(value.substring(0, maxlimit));
			return false;
		} else {
			return true;
		}
	}
</script>
<script>
	currentMenuId = "#dashboardMenu";
	$(".menu-parent-dashboard-event-dashboard").addClass('active');
	$(".menu-parent-dashboard").addClass('active');
</script>
<g:render template="/layouts/error"/>
</body>
</html>
