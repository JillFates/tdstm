<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="layout" content="projectHeader" />
	<title>Client Dashboard</title>
	
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tabcontent.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<%--<g:javascript src="FusionCharts.js" /> --%> 
	<g:javascript src="yahoo.ui.dashboard.js" />
	<%--
	<script type="text/javascript">
	/* render the individual step dial data*/
	function stepDialData( dialInd ){
		var xmlData = "<chart bgAlpha='0' bgColor='eeeded' lowerLimit='0' upperLimit='100' numberSuffix='' animation='0'"+
			" showValues='0' rotateValues='1' placeValuesInside='1' "+
			" showBorder='0' basefontColor='000000' chartTopMargin='15' chartBottomMargin='15' chartLeftMargin='5'"+
			" chartRightMargin='5' toolTipBgColor='80A905' gaugeFillMix='{dark-10},FFFFFF,{dark-10}' gaugeFillRatio='3' showTickMarks='0'>"+
	  		" <colorRange> <color minValue='0' maxValue='25' code='FF654F'/> <color minValue='25' maxValue='50' code='F6BD0F'/>"+
	  		" <color minValue='50' maxValue='100' code='8BBA00'/> </colorRange>"+
	  		" <dials> <dial value='"+dialInd+"' rearExtension='10'/> </dials>"+
	  		" <trendpoints> <point value='' displayValue='' fontcolor='FF4400' useMarker='1' dashed='1' dashLen='2' dashGap='2' valueInside='1' /> </trendpoints>"+
	  		" <annotations> <annotationGroup id='Grp1' showBelow='1' > "+
	  		" <annotation type='rectangle' x='5' y='5' toX='345' toY='195' radius='10' color='ffffff,ffffff' showBorder='0' /> "+
	  		" </annotationGroup> </annotations>"+
	  		" <styles><definition><style name='RectShadow' type='shadow' strength='0'/> </definition> <application>"+
	  		" <apply toObject='Grp1' styles='RectShadow' /> </application> </styles> </chart>";
  		return xmlData;
	}
	/* render the summary dial data*/
	function summaryDialData( dialInd ){
		var xmlData = "<chart bgAlpha='0' bgColor='FFFFFF' lowerLimit='0' upperLimit='100' numberSuffix='' showBorder='0' basefontColor='000000' "+
			"animation='0' showValues='0' rotateValues='1' placeValuesInside='1' "+ 
			"chartTopMargin='15' chartBottomMargin='15' chartLeftMargin='5' chartRightMargin='5' toolTipBgColor='80A905' "+
			"gaugeFillMix='{dark-10},FFFFFF,{dark-10}' gaugeFillRatio='3' showTickMarks='0'>"+
			" <colorRange><color minValue='0' maxValue='25' code='FF654F'/><color minValue='25' maxValue='50' code='F6BD0F'/>"+
			" <color minValue='50' maxValue='100' code='8BBA00'/></colorRange>"+
			"<dials> <dial value='"+dialInd+"' rearExtension='10'/></dials>"+
			"<trendpoints><point value='' displayValue='' fontcolor='FF4400' useMarker='' dashed='1' dashLen='2' dashGap='2' valueInside='1' />"+
			"</trendpoints> <annotations> <annotationGroup id='Grp1' showBelow='1' >"+
			"<annotation type='rectangle' x='5' y='5' toX='345' toY='195' radius='10' color='ffffff,ffffff' showBorder='0' />"+
			"</annotationGroup></annotations>"+
			"<styles>  <definition> <style name='RectShadow' type='shadow' strength='0'/> </definition> <application>"+
			"<apply toObject='Grp1' styles='RectShadow' /></application></styles></chart>";
		return xmlData;
	}
	/* render the revised dial data*/
    function revisedDialData( dialInd ){
    	var xmlData = "<chart bgAlpha='0' bgColor='FFFFFF' lowerLimit='0' upperLimit='100' numberSuffix='' showBorder='0' basefontColor='000000' "+
	    	" animation='0' showValues='0' rotateValues='1' placeValuesInside='1' "+
	    	" chartTopMargin='15' chartBottomMargin='15' chartLeftMargin='5' chartRightMargin='5' toolTipBgColor='80A905' "+
	    	" gaugeFillMix='{dark-10},FFFFFF,{dark-10}' gaugeFillRatio='3' showTickMarks='0'>"+
	    	" <colorRange><color minValue='0' maxValue='25' code='FF654F'/><color minValue='25' maxValue='50' code='F6BD0F'/>"+
	    	" <color minValue='50' maxValue='100' code='8BBA00'/></colorRange>"+
	    	" <dials><dial value='"+dialInd+"' rearExtension='10'/></dials>"+
			" <trendpoints><point value='' displayValue='' fontcolor='FF4400' useMarker='1' dashed='1' dashLen='2' dashGap='2' valueInside='1' />"+
			" </trendpoints> <annotations> <annotationGroup id='Grp1' showBelow='1' >"+
			" <annotation type='rectangle' x='5' y='5' toX='345' toY='195' radius='10' color='FFFFFF,FFFFFF' showBorder='0' /> "+
			"</annotationGroup></annotations> "+
			" <styles><definition><style name='RectShadow' type='shadow' strength='0'/></definition>"+
			" <application><apply toObject='Grp1' styles='RectShadow' /></application></styles></chart>"
    }
	</script> --%> 
</head>
<div  class="body_bg">
<a name="page_up"></a>
<div id="doc">
	
		<!-- Body Starts here-->
		<div id="bodycontent">
		<!--Header Starts here-->
		<div id="header">
			<div style="float: left;padding-top: 2px;">
					<g:form action="index" controller="dashboard" name="dashboardForm">
					<span>
					
						<label for="moveEvent"><b>Event:</b></label>&nbsp;<select id="moveEvent" name="moveEvent" onchange="document.dashboardForm.submit();">
							<g:each status="i" in="${moveEventsList}" var="moveEventInstance">
								<option value="${moveEventInstance?.id}">${moveEventInstance?.name}</option>
							</g:each>
						</select>
					</span>
					</g:form>
			</div>
			<div style="height: 35px;display: none;" >
					<label>
					  <select name="timezone" id="timezone" onChange="setUserPrefTimeZone()" class="selecttext">
					    <option value="0">GMT</option>
					    <option value="-8">PST</option>
					    <option value="-7">PDT</option>
					    <option value="-7">MST</option>
					    <option value="-6">MDT</option>
					    <option value="-6">CST</option>
					    <option value="-5">CDT</option>
					    <option value="-5">EST</option>
					    <option value="-4">EDT</option>
					  </select>
					</label>
			</div>
			<div style="float: right;width: 150px;padding-top: 2px;">
					<div style="float: right;">
						<input type="button" value="Update:" id="update" onclick="pageReload();"/> 
						<select name="updateTime" id="updateTimeId" class="selecttext" onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'timedUpdate(e.responseText)') }">
							<option value="30000">30s</option>
							<option value="60000">1m</option>
							<option value="120000">2m</option>
							<option value="300000">5m</option>
							<option value="600000">10m</option>
							<option value="never" selected="selected">Never</option>
						</select>
					</div>
					<%-- <div style="float: right;padding: 3px 0px;"> <a href="#page_down" class="nav_button">Page Down</a></div> --%>
			</div>
			<input type="hidden" id="typeId" value="${params.type}">
			<input type="hidden" id="stateId" value="${params.state}">
			<input type="hidden" id="maxLenId" value="${params.maxLen}">
			<input type="hidden" id="sortId" value="${params.sort}">
		</div>
		<!-- Header Ends here-->
		<div id="bodytop">
			<div id="plan_summary">
				<div id="topindleft">
					<div id="summary_gauge_div" align="center"> 
					<g:if test="${isAdmin || isProjManager }">
						<a href="#manualSummary" onclick="javascript:$('#manualSumStatusSpan').show();">
							<img id="summary_gauge" alt="Move Event Summary" src="${createLinkTo(dir:'i/dials',file:'dial-50.png')}" style="border: 0px;">
						</a>
					</g:if>
					<g:else>
						<img id="summary_gauge" alt="Move Event Summary" src="${createLinkTo(dir:'i/dials',file:'dial-50.png')}" style="border: 0px;">
					</g:else>
					</div>
					<%--
					<script language="JavaScript">
						var summarychart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "summary_gauge", "280", "136", "0", "0");
	        			//summarychart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'summary_gauge.xml')}");
	        			 summarychart.setDataXML( summaryDialData( "50" ) )
						summarychart.render("summary_gauge_div");
					</script>  --%>
						Move Status vs. Plan
					<br/>
					<span id="manualSumStatusSpan" style="display: none;width: 10px;">
						<input type="text" value="" name="manualSummaryStatus" id="manualSummaryStatusId" size="3" maxlength="3" onblur="validateManulaSummary(this.value)"/>&nbsp;
						<input type="button" value="Save" onclick="changeEventSummary()"/>
					</span>
				</div>
				<div class="topleftcontent">
						Planned Completion<br>
						<!--12/12: 07:00 AM EST&#13;-->
						<span id="spanPlanned"></span>
				</div>
			</div>
			<div id="revised_summary" >
				<div id="topindright" style="display: none;">
					<div id="revised_gauge_div" align="center">
					<img id="revised_gauge" alt="Move Event Revised Summary" src="${createLinkTo(dir:'i/dials',file:'dial-50.png')}">
					</div>
					<%--<script language="JavaScript">
						var revisedChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "revised_gauge", "280", "136", "0", "0");
						//summarychart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'revised_gauge.xml')}");
						revisedChart.setDataXML( revisedDialData( "50" ) )
						revisedChart.render("revised_gauge_div");
					</script>  --%> 
						Status vs. Revised Plan
				</div>
				<div class="toprightcontent" id="revised_gauge_content" style="display: none;">
					Confidence in Revised Plan<br>
					<span class="high">High</span><br>
					<span class="redfont">Planned Completion:&#13;
					<!--12/12: 07:00 AM EST&#13;-->
					<br />
					<span id="spanRevised"></span></span>
				</div>
			</div>
		</div>
		<!-- News section starts here-->
		<div id="newssection">
			<div id="newstop">
				<div id="newsheading"> Move News </div>
				<div id="newsmenu">
				    <ul id="newstabs" class="shadetabs">
				    	<li><a href="#" rel="news_live_div" class="selected">Live</a></li>
				    	<li><a href="#" rel="news_archived_div" onmouseup="javascript:setCrossobjTop()">Archive</a></li>
				    </ul>
				</div>
				<div style="float: right; margin-right: 5px;">
					<input type="button" value="Add News" onclick="openCreateNewsDialog()">
				</div>	
 			</div>
			<div style="clear:both"></div>
			<div id="newsblock">
				<div id="newsbox">
					<div id="container" style="position:absolute;width:900px;height:70px;overflow:hidden;border:0px solid grey">
						<div id="content" style="position:relative;width:900px;left:0px;top:-5px">
							<div id="news_live_div" class="tabcontent">
								<ul id="news_live" class="newscroll">
								</ul>
							</div>
							<div id="news_archived_div" class="tabcontent">
								<ul id="news_archived" class="newscroll">
								</ul>
							</div>
						</div>
					</div>
				</div>			
				<div id="newsarrows">
					<div id="toparrow">
						<a href="javascript:moveup()"><img src="${createLinkTo(dir:'images',file:'up_arrow.png')}" alt="scroll up" width="10" height="6" border="0" /></a>
					</div>
					<div id="bottomarrow">
						<a href="javascript:movedown()"><img src="${createLinkTo(dir:'images',file:'down_arrow.png')}" alt="scroll down" width="10" height="6" border="0" /></a>
					</div>
				</div>
			</div>
		</div>
<!-- News section ends here-->
<!-- Bundle Sections starts here-->
		<div id="bdlsection">
			<div id="bdltabs">
				<span style="line-height: 28px;">&nbsp;</span>
				<g:each in="${moveBundleList}" status="i" var="moveBundle">
					<span id="spnBundle${moveBundle.id}" class="${ i == 0 ? 'mbhactive' : 'mbhinactive' }" onClick="updateDash(${moveBundle.id})">
					${moveBundle.name}</span>&nbsp;&nbsp;
				</g:each>
			</div>
			<div id="leftcol">
				<ul id="btitle" >
					<li>Step</li>
					<li><span class="percentage">In Percentage</span></li>
					<!-- <li><span class="percentage">Completion</span></li> -->
					<li>Planned Start</li>
					<li>Planned&nbsp;Completion</li>
					<li>Actual Start</li>
					<li >Actual&nbsp;Completion</li>
				</ul>
			</div>
			<div id="leftarrow"><a href="javascript:void(0);" id="move-left"><img src="${createLinkTo(dir:'images',file:'left_arrow.png')}" alt="back" border="0" width="16" height="23" align="right"></a></div>
			<div class="mod">
				<div id="themes">
				<input type="hidden" value="${moveBundleList ? moveBundleList[0]?.id : ''}" id="defaultBundleId">
				<g:each in="${moveBundleList}" status="i" var="moveBundle">
					<div id="bundlediv${moveBundle.id}" class="${i == 0 ? 'show_bundle_step' : 'hide_bundle_step'}">
						<g:each in="${MoveBundleStep.findAll('FROM MoveBundleStep mbs where mbs.moveBundle='+moveBundle.id+' ORDER BY mbs.transitionId')}" status="j" var="moveBundleStep">
							<div style="float:left;width:130px;" >
								<ul class="bdetails">
									<li class="heading" title="${moveBundleStep.label}">
										<g:if test="${moveBundleStep.label.length()>10}">
											${moveBundleStep.label.substring(0,11)}..
										</g:if>
										<g:else>
											${moveBundleStep.label}
										</g:else>
									</li>
									<li id="percentage_${moveBundle.id}_${moveBundleStep.transitionId}" > </li>
									<!-- <li class="actfinish1"><span id="completion_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li> -->
									<li class="schstart"><span id="plan_start_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
									<li class="schfinish"><span id="plan_completion_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
									<li class="actstart" id="li_start_${moveBundle.id}_${moveBundleStep.transitionId}"><span id="act_start_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
									<li class="actfinish" id="li_finish_${moveBundle.id}_${moveBundleStep.transitionId}"><span id="act_completion_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
								</ul>
								<div id="chartdiv_${moveBundle.id}_${moveBundleStep.transitionId}" align="center" style="display: none;">
									<jsec:hasAnyRole in="['ADMIN']"><img id="chart_${moveBundle.id}_${moveBundleStep.transitionId}" src="${createLinkTo(dir:'i/dials',file:'dial-50sm.png')}"></jsec:hasAnyRole>
								</div>
								<%-- <jsec:hasAnyRole in="['ADMIN']">
								<script language="JavaScript">
							         var stepchart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "chart_${moveBundle.id}_${moveBundleStep.transitionId}", "100", "75", "0", "0");
							         //stepchart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'step_gauge.xml')}");
							         stepchart.setDataXML( stepDialData( "50" ) )
							         stepchart.render("chartdiv_${moveBundle.id}_${moveBundleStep.transitionId}");
								</script>  
								</jsec:hasAnyRole> --%>
							</div>
						</g:each>
					</div>
				</g:each>
				</div>
			</div>
<div id="rightarrow"><a href="javascript:void(0);" id="move-right"><img src="${createLinkTo(dir:'images',file:'right_arrow.png')}" alt="back" border="0" width="16" height="23" align="right"></a></div>
		</div>
		<div style="text-align: right;padding:4px 0px;">
			<%--<a href="#page_up" class="nav_button" style="nowrap:nowrap;">Page Up</a> --%>
		</div>
	</div>

<!-- Bundle Sections ends here-->

<!-- Footer starts here-->
	<div style="clear:both"></div>
	<!--<div id="crawler">
		<div id="mycrawler"><div id="mycrawlerId" style="width: 900px;margin-top: -6px;" >.</div></div>
	</div>-->
<!-- Footer Ends here-->
<!-- Body Ends here-->
<a name="page_down"></a>
</div>
<div id="createNewsDialog" title="Create News Comment" style="display: none;">
	<input name="moveEvent.id" value="${moveEvent?.id}" type="hidden" id="moveEventId"/>
		<div class="dialog" style="border: 1px solid #5F9FCF">
		<table id="createCommentTable" style="border: 0px">
			<tr>
				<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>
			<tr>
		<td valign="top" class="name"><label>Comment Type:</label></td>
				<td valign="top" class="value" > 
				<select disabled="disabled">
				<option>News</option>
				</select>
				</td>
		</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="messageId"><b>Comment:&nbsp;<span style="color: red">*</span></b></label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="messageId" name="message"></textarea></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name" nowrap="nowrap"><label for="isArchivedId" >Resolved / Archived:</label></td>
				<td valign="top" class="value" id="archivedTdId">
				<input type="checkbox" id="isArchivedId" value="0" onclick="updateHidden('isArchivedId','isArchivedHiddenId')"/>
				<input type="hidden" name="isArchived" value="0" id="isArchivedHiddenId"/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="resolutionNewsId">Resolution:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="resolutionNewsId" name="resolution"></textarea> </td>
			</tr>
			
		</table>
		</div>
		<div class="buttons"><span class="button"> 
		<input class="save" type="button" value="Create" onclick="return submitCreateNewsForm()"/></span>
		<span class="button"> 
	<input class="delete" type="button" value="Cancel" onclick="resetCreateNewsForm();"/>
	</span>
		</div>
</div>
<div id="showEditCommentDialog" title="Edit Issue Comment"
	style="display: none;">
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<input name="id" value="" id="commentId" type="hidden"/>
	<input name="commentType" value="" id="commentTypeId" type="hidden"/>
	<input name="moveEvent.id" value="${moveEvent?.id}" type="hidden"/>
		<div>
	<table id="showCommentTable" style="border: 0px">
		
		<tr>
		<td valign="top" class="name"><label for="dateCreated">Created
				At:</label></td>
				<td valign="top" class="value" id="dateCreatedId" ></td>
		</tr>
			<tr>
		<td valign="top" class="name"><label for="createdBy">Created
				By:</label></td>
				<td valign="top" class="value" id="createdById" ></td>
		</tr>
		<tr>
		<td valign="top" class="name"><label>Comment Type:</label></td>
				<td valign="top" class="value" > 
				<select disabled="disabled" id="commentTypeOption">
				<option>Issue</option>
				</select>
				</td>
		</tr>
		<tr id="displayOptionTr">
			
		<td valign="top" class="name" nowrap="nowrap">
			<label for="category">User / Generic Cmt:</label></td>
				<td valign="top" class="value" id="displayOption" >
				<input type="radio" name="displayOption" value="U" id="displayOptionUid"/>&nbsp;
				<span style="vertical-align: text-top;">User Comment</span>&nbsp;&nbsp;&nbsp;
				<input type="radio" name="displayOption" value="G" checked="checked" id="displayOptionGid"/>&nbsp;
				<span style="vertical-align:text-top;">Generic Comment&nbsp;</span>
				</td>
		</tr>
		<tr class="prop" id="assetTrId">
		<td valign="top" class="name"><label for="assetTdId">Asset:</label></td>
				<td valign="top" class="value"><input type="text" disabled="disabled" id="assetTdId"/></td>
		</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="commentTdId" name="comment"></textarea> </td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name" nowrap="nowrap"><label for="isResolved" >Resolved / Archived:</label></td>
				<td valign="top" class="value" id="resolveTdId">
				<input type="checkbox" id="isResolvedId" value="0" onclick="updateHidden('isResolvedId','isResolvedHiddenId')"/>
				<input type="hidden" name="isResolved" value="0" id="isResolvedHiddenId"/>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="resolution">Resolution:</label>
				</td>
				<td valign="top" class="value" ><textarea cols="80" rows="5"
						id="resolutionId" name="resolution"></textarea> </td>
			</tr>
				<tr>
		<td valign="top" class="name"><label for="dateResolved">Resolved
				At:</label></td>
				<td valign="top" class="value" id="dateResolvedId" ></td>
		</tr>
			<tr>
		<td valign="top" class="name"><label for="resolvedBy">Resolved
				By:</label></td>
				<td valign="top" class="value" id="resolvedById" ></td>
		</tr>
		
	</table>
	</div>
	<div class="buttons"><span class="button"> 
	<input class="save" type="button" value="Update" onclick="return submitUpdateNewsForm()"/>
	</span> <span class="button"> 
	<input class="delete" type="button" value="Cancel" onclick="timedUpdate( $('#updateTimeId').val() );$('#showEditCommentDialog').dialog('close');"/>
	</span></div>
	</div>
</div>
<script type="text/javascript">
	var eventType = "load"
	var hasTimedOut = false;
	var modWidth
	$(document).ready(function() {

		var viewPort = $(window).width();
		modWidth = parseInt((viewPort-210) / 130);
		$(".mod").css("width",(modWidth * 130 )+"px");

		$(window).resize(function() {
			if(hasTimedOut != false) {
				clearTimeout(hasTimedOut);
			}
			hasTimedOut = setTimeout(function() {
				var viewPort = $(window).width() //window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
				modWidth = parseInt((viewPort-210) / 130)
				$(".mod").css("width",(modWidth * 130 )+"px");
				AditionalFrames = 1
				stepCount = 1
				updateDash( $("#defaultBundleId").val() );
			}, 100);
		});
		
	    $("#showEditCommentDialog").dialog({
	        autoOpen: false,
	        resizable: false
 		});
	    $("#createNewsDialog").dialog({
	        autoOpen: false,
	        resizable: false
 		});
	})
	
	
	if("${session.getAttribute('CURR_TZ')?.CURR_TZ}"){
		$("#timezone").find("option[text='${session.getAttribute('CURR_TZ')?.CURR_TZ}']").attr("selected","selected");
	} else {
		$("#timezone").find("option[text='EDT']").attr("selected","selected");
	}
	if('${timeToUpdate}'){
		$("#updateTimeId").val("${timeToUpdate}")
	}
	var sURL = unescape(window.location);
	var timer
	var errorCode = '200'
	var dialReload = true;
	var countries=new ddtabcontent("newstabs")
	countries.setpersist(true)
	countries.setselectedClassTarget("link") //"link" or "linkparent"
	countries.init()
	
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
	var	AditionalFrames = 1;
	var stepCount = 1;
	var defaultBundle = $("#defaultBundleId").val();
	function moveDataSteps(){
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
					anim.animate();
				}
			};
		}();
		YAHOO.util.Event.onAvailable('doc',YAHOO.example.init, YAHOO.example, true);
	}
	moveDataSteps()
	/* set time to load the move news and move bundle data*/
	var handler = 0
	function timedUpdate( updateTime ) {
		if(updateTime != 'never'){
			handler = setInterval("getMoveEventNewsDetails($('#moveEvent').val())",updateTime);
			$("#updateTimeId").val(updateTime)
		} else {
			clearInterval(handler)
		}
	}
	/* script to assign the move evnt value*/
	var moveEvent = "${moveEvent?.id}"
	if(moveEvent){
		$("#moveEvent").val(moveEvent)
	}
	timedUpdate( $("#updateTimeId").val() );
	/* Function to load the data for a particular MoveEvent */
	var doUpdate = true
	function getMoveEventNewsDetails( moveEvent ){
		$("#createNewsDialog").dialog("close");
		$("#showEditCommentDialog").dialog("close");
		updateDash( $("#defaultBundleId").val() );
	<%--	if(dialReload && doUpdate){
			timer = setTimeout( "getDialsData($('#defaultBundleId').val() )", 5000 );
		}  --%>
		if(moveEvent){
			jQuery.ajax({
		        type:"GET",
		        async : true,
		        cache: false,
		        url:"../ws/moveEventNews/"+moveEvent+"?type="+$("#typeId").val()+"&state="+$("#stateId").val()+"&maxLen="+$("#maxLenId").val()+"&sort="+$("#sortId").val(),
		        dataType: 'json',
		        success:updateMoveEventNews,
                error:function (xhr, ajaxOptions, thrownError){
            		if( doUpdate && errorCode ==  xhr.status ){
	                    clearInterval(handler);
	                    $("#update").css("color","red")
	                    if( xhr.status == "403"){
	                    	alert("403 Forbidden occurred, user don't have permission to load the current project data.");
	                    } else {
	                    	alert("Sorry, there is a problem receiving updates to this page. Try reloading to resolve.");
	                    }    
                	} else {
                		errorCode =  xhr.status ; 
                	}
                }	 
			});
		}
	}
	/* Update the Move news once ajax call success*/
	function updateMoveEventNews( news ){
		
		var offset = $("#timezone").val()
		var newsLength = news.length;
		var live = "";
		var archived = "";
		var scrollText = " ";
		var myDate = new Date();
		for( i = 0; i< newsLength; i++){
			var state = news[i].state;
			var liId = news[i].type+"_"+news[i].id
			var createdTime = convertTime(offset,news[i].created)
			if(state == "A"){
				archived +=	"<li id="+liId+" onclick='openEditNewsDialog(this.id)'><span class='newstime'>"+createdTime+" :</span> <span class='normaltext'>"+news[i].text+"</span></li>";
			} else {
				live +=	"<li id="+liId+" onclick='openEditNewsDialog(this.id)'><span class='newstime'>"+createdTime +" :</span> <span class='normaltext'>"+news[i].text+"</span></li>";
				scrollText += createdTime+" : "+news[i].text +". "
			}
		}
		$("#news_live").html(live);
		$("#news_archived").html(archived);
		$("#head_mycrawlerId").html(scrollText)
		
	}
	function setUserPrefTimeZone(){
		var timeZone = $("#timezone :selected").text()
  		${remoteFunction(controller:'project', action:'setUserTimeZone', params:'\'tz=\' + timeZone ', onComplete:'getMoveEventNewsDetails($(\'#moveEvent\').val());')}
  	}

	/* function to load the user agent*/
	if(navigator.appName == "Microsoft Internet Explorer"){
		$("#content").css("top",0)
	}
	var speed = 10
	var crossobjTop = $("#content").css("top")
	function movedown(){
		
		var crossobj = $("#content")
		var contentheight = crossobj.height()
		if ( parseInt(crossobj.css("top")) >= (contentheight - 60)*(-1) ){
			crossobj.css("top",parseInt(crossobj.css("top"))-speed+"px")
		}
	}
	
	function moveup(){
		
		var crossobj=$("#content")
		var contentheight=crossobj.height()
		if (parseInt(crossobj.css("top"))<=-15){
			crossobj.css("top",parseInt(crossobj.css("top"))+speed+"px")
		}
	}
	function setCrossobjTop(){
		$("#content").css("top",crossobjTop);
		if(navigator.appName == "Microsoft Internet Explorer"){
			$("#content").css("top",0)
		}
	}
	
	/*-----------------------------------------------------------
	 * functions to convert Date & Time into respective timezones
	 *-----------------------------------------------------------*/

	function convertTime(offset, source) {
		try{
			//12/11: 12:30 PM: (0m)     
		    if (source ==  ""){
			    return ""
			} else if (source.substring(0,5).toLowerCase() ==  "total"){
				return source
			}  
		                                
		    var p = trimAll(source);
		    p = p.substring(p.length-1, p.length);
		    var tsource;
		    var tsource1;
		    var tsource2 = 0;
		    var temp
		    
		    if (p == ")"){
			tsource = source.substring(0,source.length).split("(");
		        tsource1 = tsource[0];            
		        tsource1 = tsource1.substring(0,tsource1.length-2);
		        temp = tsource1;
		        tsource2 = trimAll(tsource[1]);        
		        tsource2 = "(" + tsource2;
			} else {
		    	temp = trimAll(source);
			}
			if(temp == "null"){
				return "";
			} else {
				var date = new Date(temp);
				var utcDate = date.getTime() ;
			    var convertedDate = new Date(utcDate + (3600000*offset));
			    return getTimeFormate( convertedDate ) +" "+ (tsource2 ? tsource2 : "")
			}
	    }catch(e){
		    if(doUpdate){
		    	clearInterval(handler);
	      		doUpdate = false;
	      		$("#update").css("color","red")
				alert("Sorry, there is a problem receiving updates to this page. Try reloading to resolve.");
		    }
		}
	    
	}
	function getTimeFormate( date )
	{
		var timeString = ""
		var month =  date.getMonth();
		
		if( !isNaN(month) ){
		   month = month + 1;
		   var monthday    = date.getDate();
		   var year        = date.getFullYear();
		   
		   var hour   = date.getHours();
		   var minute = date.getMinutes();
		   var second = date.getSeconds();
		   var ap = "AM";
		   if (hour   > 11) { ap = "PM";             }
		   if (hour   > 12) { hour = hour - 12;      }
		   if (hour   == 0) { hour = 12;             }
		   if (hour   < 10) { hour   = "0" + hour;   }
		   if (minute < 10) { minute = "0" + minute; }
		   if (second < 10) { second = "0" + second; }
		   var timeString = month+"/"+monthday+" "+hour + ':' + minute +" " +  ap;
		}
	   return timeString;
	   
	}
	function trimAll(sString) {
		while (sString.substring(0,1) == ' ') {
			sString = sString.substring(1, sString.length);
		}
	    while (sString.substring(sString.length-1, sString.length) == ' '){
			sString = sString.substring(0,sString.length-1);
		}
		return sString;
	}
	
	/* display bundle tab and call updateDash method to load the appropriate data*/
	function displayBundleTab(Id) {
		 $(".mbhactive").attr("class","mbhinactive");
		 $("#spnBundle"+Id).attr("class","mbhactive");
		 $(".show_bundle_step").attr("class","hide_bundle_step");
		 $("#bundlediv"+Id).attr("class","show_bundle_step");
		 $("#defaultBundleId").val(Id)
	 }
	/*----------------------------------------
	 * 
	 *--------------------------------------*/
	 
	 function updateDash( bundleId ) {
		 var moveEvent = $("#moveEvent").val()
		 if(moveEvent){
		 	displayBundleTab( bundleId )
		 	jQuery.ajax({
		        type:"GET",
		        async : true,
		        cache: false,
		        url:"../ws/dashboard/bundleData/"+ bundleId+"?moveEventId="+moveEvent,
		        dataType: 'json',
		        success:updateMoveBundleSteps,
                error:function (xhr, ajaxOptions, thrownError){
	          		if(errorCode ==  xhr.status ){
	          			clearInterval(handler);
		          		doUpdate = false;
		          		$("#update").css("color","red")
		            	if( xhr.status == "403"){
		             		alert("403 Forbidden occurred, user don't have permission to load the current project data.");
						} else {
		             		alert("Sorry, there is a problem receiving updates to this page. Try reloading to resolve.");
		             	}    
	          		} else {
	          			errorCode = xhr.status;
	          			doUpdate = false;
	          		}
		 		}
			});
		 }
	 }

	 /* update move bundal data once ajax call success */
	
	function updateMoveBundleSteps( dataPointStep ) {
		try{
			var offset = $("#timezone").val()
			
			var snapshot = dataPointStep.snapshot;
			var moveBundleId = snapshot.moveBundleId;
			
			var steps = snapshot.steps;
			var revSum = snapshot.revSum;
			var planSum = snapshot.planSum
			var sumDialInd = planSum.dialInd != null ? planSum.dialInd : 50
			if(AditionalFrames == 1 || defaultBundle != moveBundleId){
				AditionalFrames = ( steps.length > modWidth ? steps.length - (modWidth-1) : 1 );
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
			$("#manualSummaryStatusId").val( sumDialInd );
			$("#spanPlanned").html(convertTime(offset, planSum.compTime))
			
			if(revSum.dialInd == "-1") {
				$("#topindright").hide();
				$("#revised_gauge_content").hide();
			} else if(snapshot.revisedComp) {
				$("#topindright").show();
				updateSummaryGauge("revised_gauge",revSum.dialInd)
				$("#spanRevised").html(convertTime(offset, revSum.compTime))
				$("#revised_gauge_content").show();
			}
			for( i = 0; i < steps.length; i++ ) {
				$("#percentage_"+moveBundleId+"_"+steps[i].tid).html(isNaN(steps[i].tskComp / steps[i].tskTot) ? 0+ "%" : parseInt( (steps[i].tskComp / steps[i].tskTot ) * 100 ) +"%");
				$("#percentage_"+moveBundleId+"_"+steps[i].tid).attr("class",steps[i].percentageStyle)
				//$("#completion_"+moveBundleId+"_"+steps[i].tid).html(steps[i].projComp);
				$("#plan_start_"+moveBundleId+"_"+steps[i].tid).html(convertTime(offset, steps[i].planStart));
				$("#plan_completion_"+moveBundleId+"_"+steps[i].tid).html(convertTime(offset, steps[i].planComp));
				var startDelta = 0
				var actDelta = 0
				if( steps[i].actStart ){
					startDelta = parseInt((new Date(steps[i].actStart).getTime() - new Date(steps[i].planStart).getTime())/60000);
					if(startDelta > 0){
						$("#li_start_"+moveBundleId+"_"+steps[i].tid).removeClass("actstart");
						$("#li_start_"+moveBundleId+"_"+steps[i].tid).addClass("actstart_red");
					}
				}
				$("#act_start_"+moveBundleId+"_"+steps[i].tid).html(convertTime(offset, steps[i].actStart+": ("+ startDelta +"m)"));
				if( steps[i].actStart && !steps[i].actComp && steps[i].calcMethod != "M") {
					$("#act_completion_"+moveBundleId+"_"+steps[i].tid).html("<span id='databox'>Total Devices "+steps[i].tskTot+" Completed "+steps[i].tskComp+"</span>")
				} else {
					actDelta = parseInt((new Date(steps[i].actComp).getTime() - new Date(steps[i].planComp).getTime())/60000);
					if(actDelta > 0){
						$("#li_finish_"+moveBundleId+"_"+steps[i].tid).removeClass("actfinish");
						$("#li_finish_"+moveBundleId+"_"+steps[i].tid).addClass("actfinish_red");
					}
					$("#act_completion_"+moveBundleId+"_"+steps[i].tid).html(convertTime(offset, steps[i].actComp+": ("+actDelta+"m)"));
				}
				var percentage = $("#percentage_"+moveBundleId+"_"+steps[i].tid).html()
				if(percentage != "100%" && percentage != "0%"){
					<jsec:hasAnyRole in="['ADMIN']">
					$("#chartdiv_"+moveBundleId+"_"+steps[i].tid ).show();
					post_init( "chart_"+moveBundleId+"_"+steps[i].tid, steps[i].dialInd )
					//post_init( "chart_'+moveBundleId+'_'+steps[i].tid+'", '+steps[i].dialInd+' )
					</jsec:hasAnyRole>
				} else {
					$("#chartdiv_"+moveBundleId+"_"+steps[i].tid ).hide();
				}
			}
		} catch(ex){
			if(doUpdate){
				clearInterval(handler);
	      		doUpdate = false;
	      		$("#update").css("color","red")
				alert("Sorry, there is a problem receiving updates to this page. Try reloading to resolve.");
			}
		}
		
	}
	<%--
	function getDialsData( bundleId ) {
		 var moveEvent = $("#moveEvent").val()
		 if(doUpdate){
			 jQuery.ajax({
			        type:"GET",
			        async : true,
			        cache: false,
			        url:"../ws/dashboard/bundleData/"+ bundleId+"?moveEventId="+moveEvent,
			        dataType: 'json',
			        success:updateDials,
	                error:function (xhr, ajaxOptions, thrownError){
				 		if(doUpdate && errorCode ==  xhr.status ){
			            	clearInterval(handler);
			            	$("#update").css("color","red");
			            	if( xhr.status == "403"){
			             		alert("403 Forbidden occurred, user don't have permission to load the current project data.");
			             	} else {
				             	alert("Sorry, there is a problem receiving updates to this page. Try reloading to resolve.");
				            }
				 		} else {
				 			errorCode = xhr.status;
				 		}    
			 		}     
			});
		 }
	 }
	function updateDials( dataPointStep ) {
		try{
			var snapshot = dataPointStep.snapshot;
			var moveBundleId = snapshot.moveBundleId;
			var steps = snapshot.steps;
			var revSum = snapshot.revSum;
			var planSum = snapshot.planSum
	
			if( snapshot.planDelta > 0){
				$(".sum_statusbar_good").attr("class","sum_statusbar_bad")
				$("#status_color").html("RED")
			} else {
				$(".sum_statusbar_bad").attr("class","sum_statusbar_good")
				$("#status_color").html("GREEN")
			}
			updateSummaryGauge("summary_gauge",planSum.dialInd ? planSum.dialInd : '50');
			
			if(snapshot.revisedComp) {
				updateSummaryGauge("revised_gauge",revSum.dialInd)
			}
			for( i = 0; i < steps.length; i++ ) {
				var percentage = $("#percentage_"+moveBundleId+"_"+steps[i].tid).html()
				if(percentage != "100%" && percentage != "0%"){
					<jsec:hasAnyRole in="['ADMIN']">
					$("#chartdiv_"+moveBundleId+"_"+steps[i].tid ).show();
					post_init( "chart_"+moveBundleId+"_"+steps[i].tid, steps[i].dialInd )
					</jsec:hasAnyRole>
				} else {
					$("#chartdiv_"+moveBundleId+"_"+steps[i].tid ).hide();
				}
			}
			clearTimeout(timer)
			dialReload = false;
		} catch(ex){alert(ex)}
		
	}  --%>
	/* function to render the dials */
	function post_init( divId, dialInd ){

		var dInd = dialInd % 2 == 0 ? dialInd : dialInd+1
		var src = "../i/dials/dial-"+dInd+"sm.png";
        $("#"+divId).attr("src", src);
        $("#"+divId).attr("title", dialInd);
		
		<%--try{
			//var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId2b", "100", "75", "0", "0");
			updateChartXML( divId, stepDialData( dialInd ) ); 
			//myChart.setDataXML( xmlData );
			//myChart.render(divId);
		} catch(e){
			if(doUpdate){
				clearInterval(handler);
	      		doUpdate = false;
	      		$("#update").css("color","red")
				alert("Sorry, there is a problem receiving updates to this page. Try reloading to resolve.");
			}
		} --%>
	}
	function updateSummaryGauge( divId, dialInd ){
		var dInd = dialInd % 2 == 0 ? dialInd : dialInd+1
		var src = "../i/dials/dial-"+dInd+".png";
		$("#"+divId).attr("src", src);
		$("#"+divId).attr("title", dialInd);
		<%--//var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId", "280", "136", "0", "0");
		updateChartXML(divId, summaryDialData( dialInd ) );
		//myChart.setDataXML( xmlData );
	   	//myChart.render(divId); --%>
	}
	<%--function updateRevisedGauge( divId, dialInd ){
		   //var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId1", "180", "136", "0", "0");
		updateChartXML( divId, revisedDialData( dialInd ) ); 
	    //myChart.setDataXML( xmlData );
	   	//myChart.render(divId); 
	}--%>
	function pageReload(){
		if(eventType != "load" || !window.ActiveXObject) {
			window.location = document.URL;
		}
		eventType ="";
	}
	/*
	will popup the dialog to create news
	*/
	function openCreateNewsDialog(){
		timedUpdate('never');
		$("#createNewsDialog").dialog('option', 'width', 'auto');
		$("#createNewsDialog").dialog('option', 'position', ['center','top']);
		$("#showEditCommentDialog").dialog("close");
		$('#createNewsDialog').dialog('open');
	}
	function updateHidden(checkBoxId,hiddenId){
		var resolve = $("#"+checkBoxId).is(':checked');
		if(resolve){
			$("#"+hiddenId).val(1);
		} else {
			$("#"+hiddenId).val(0);
		}
	}
	/*
	* this function is used to validate the crete news form before sending to server
	*/
	function submitCreateNewsForm(){
		var moveEvent = $("#moveEventId").val();
		var resolveBoo = $("#isArchivedId").is(':checked');
		var resolveVal = $("#resolutionNewsId").val();
		var news = $("#messageId").val()
		var validate = false;
		if(moveEvent){
			if(resolveBoo && resolveVal == ""){
				alert('Please enter Resolution');
			} else if( !news ){
				alert('Please enter Comment');
			} else {
				validate = true;
			}
		} else{
			alert("Please Assign MoveEvent to Current Bundle")
		}
		if(validate){
			timedUpdate( $("#updateTimeId").val() );
			${remoteFunction(action:'saveNews', 
					params:'\'moveEvent.id=\' + moveEvent +\'&message=\'+ news +\'&isArchived=\'+$(\'#isArchivedHiddenId\').val()+\'&resolution=\'+$(\'#resolutionNewsId\').val()', 
					onComplete:'getMoveEventNewsDetails(moveEvent)')}
		}
	}
	function resetCreateNewsForm(){
		$("#messageId").val("");
		$('#isArchivedHiddenId').val("0");
		$('#resolutionNewsId').val("")
		$('#isArchivedId').attr("checked",false)
		$('#createNewsDialog').dialog('close');
		timedUpdate( $("#updateTimeId").val() );
	}
	/* will popup the dialog to edit news */
	function openEditNewsDialog( newsId ){
		var idArray = newsId.split("_")
		var type = idArray [0]
		var id = idArray [1] 
		${remoteFunction(controller:'newsEditor', action:'getCommetOrNewsData',params:'\'id=\' + id +\'&commentType=\'+type', onComplete:'showEditNewsForm( e )')}
	}
	function showEditNewsForm(){

	}
	/*-------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : assetComment / moveEventNews object based on comment Type as JSON object
	 * @return : Edit form
	 *-------------------------------------------*/
	function showEditNewsForm( e ){
		timedUpdate('never');
		var assetComments = eval('(' + e.responseText + ')');
		if (assetComments) {
			
			$('#commentId').val(assetComments[0].commentObject.id)
			$('#assetTdId').val(assetComments[0].assetName)
			$('#dateCreatedId').html(assetComments[0].dtCreated);
			if(assetComments[0].personResolvedObj != null){
				$('#resolvedById').html(assetComments[0].personResolvedObj.firstName+" "+assetComments[0].personResolvedObj.lastName);
			}else{
				$('#resolvedById').html("");
				$('#resolvedByEditId').html("");
			}
			$('#createdById').html(assetComments[0].personCreateObj.firstName+" "+assetComments[0].personCreateObj.lastName);
			$('#resolutionId').val(assetComments[0].commentObject.resolution);
			
			if(assetComments[0].commentObject.commentType != 'issue'){

				$('#commentTypeId').val("news")
				$('#dateResolvedId').html(assetComments[0].dtResolved);
				$('#isResolvedId').val(assetComments[0].commentObject.isArchived)
				$('#commentTdId').val(assetComments[0].commentObject.message)
				if(assetComments[0].commentObject.isArchived != 0){
					$('#isResolvedId').attr('checked', true);
					$("#isResolvedHiddenId").val(1);
				} else {
					$('#isResolvedId').attr('checked', false);
					$("#isResolvedHiddenId").val(0);
				}
				$("#displayOptionTr").hide();
				$("#commentTypeOption").html("<option>News</option>");
				$("#assetTrId").hide();
				$("#showEditCommentDialog").dialog('option','title','Edit News Comment');

			} else {

				$('#commentTypeId').val("issue")
				$('#dateResolvedId').html(assetComments[0].dtResolved);
				$('#isResolvedId').val(assetComments[0].commentObject.isResolved)
				$('#commentTdId').val(assetComments[0].commentObject.comment)
				if(assetComments[0].commentObject.isResolved != 0){
					$('#isResolvedId').attr('checked', true);
					$("#isResolvedHiddenId").val(1);
				} else {
					$('#isResolvedId').attr('checked', false);
					$("#isResolvedHiddenId").val(0);
				}
				if(assetComments[0].commentObject.displayOption == "G"){
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
			$("#showEditCommentDialog").dialog("open");
			$("#createNewsDialog").dialog("close");
			}
	}
	/* will submit news edit form*/
	function submitUpdateNewsForm(){
		var moveEvent = $("#moveEventId").val();
		var id = $("#commentId").val();
		var resolveBoo = $("#isResolvedId").is(':checked');
		var resolveVal = $("#resolutionId").val();
		var validate = false
		if(resolveBoo && resolveVal == ""){
			alert('Please enter Resolution');
		} else {
			validate = true;
		}
		if(validate){
			timedUpdate( $("#updateTimeId").val() );
			${remoteFunction(action:'updateNewsOrComment', 
					params:'\'moveEvent.id=\' + moveEvent +\'&id=\'+ id +\'&isResolved=\'+$(\'#isResolvedHiddenId\').val()+\'&comment=\'+$(\'#commentTdId\').val()+\'&resolution=\'+resolveVal+\'&commentType=\'+$(\'#commentTypeId\').val()+\'&displayOption=\'+$(\'input[name=displayOption]:checked\').val()', 
					onComplete:'getMoveEventNewsDetails(moveEvent)')}
		}
	}
	// validate the manual summary input value
	function validateManulaSummary(value){
		var check = true
		if( !isNaN(value) ){
			if(value > 100){
				alert("Summary status should not be greater than 100")
				check = false
			}
		} else {
			alert("Summary status should be Alpha Numeric ")
			check = false
		}
		return check
	}
	// send the request to update the manual summary value if it is valid
	function changeEventSummary(){
		var value = $("#manualSummaryStatusId").val()
		if(validateManulaSummary( value )){
			var moveEvent = $("#moveEventId").val();
			${remoteFunction(controller:"moveEvent",action:'updateEventSumamry', 
					params:'\'moveEventId=\' + moveEvent +\'&value=\'+ value', 
					onComplete:'updateDash( $("#defaultBundleId").val() )')}
			$("#manualSumStatusSpan").hide();
		}
	}
	// FUNCTION TO SET THE STEP DIV WIDTH.
	function getStepDivWidth() {
		var viewPort = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
		var modWidth = parseInt((viewPort-210) / 130)
		$(".mod").css("width",(modWidth * 130 )+"px");
		return modWidth;
	}
	// used to call the function once page loaded
	getMoveEventNewsDetails($('#moveEvent').val())
	</script>
</div>
</html>
