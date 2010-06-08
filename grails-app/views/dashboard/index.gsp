<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Client Dashboard</title>
	
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tabcontent.css')}" />
	<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<g:javascript library="prototype" />
	<%--<g:javascript src="FusionCharts.js" /> --%> 
	<g:javascript src="yahoo.ui.dashboard.js" />
	<jq:plugin name="jquery.combined" />
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
<body topmargin="0" leftmargin="0" marginheight="0" marginwidth="0" class="body_bg" onload="getMoveEventNewsDetails($('#moveEvent').val())">
<a name="page_up"></a>
<div id="doc">
	<!--Header Starts here-->
		<div id="header" style="margin-top: -2px;">
			<div id="logo">
				<g:if test="${projectLogo}">
					<img src="${createLink(controller:'project', action:'showImage', id:projectLogo?.id)}" style="height: 55px;"/>
				</g:if>
				<g:else>
					<a href="http://www.transitionaldata.com/" target="new"><img src="${createLinkTo(dir:'images',file:'tds.jpg')}" style="float: left;border: 0px"/></a>
				</g:else>
				<br>
				<div style="float: left;padding-top: 5px;">
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
				<input type="hidden" id="typeId" value="${params.type}">
				<input type="hidden" id="stateId" value="${params.state}">
				<input type="hidden" id="maxLenId" value="${params.maxLen}">
				<input type="hidden" id="sortId" value="${params.sort}">
			</div>
			<div class="clientname">${project?.client}<br/>DATA CENTER RELOCATION <br><g:link controller="project" action="show" id="${project?.id}" style="text-decoration:none;"><span class="project_link">Return to Project</span> </g:link></div>
			<div class="topdate">
				<div><img src="${createLinkTo(dir:'images',file:'powered_by.png')}" alt="Powered by TDS" width="158" height="53" title="Powered by TDS"></div>
				<div id="date"></div> <div id="clock"></div>
				<div style="height: 35px;">
					<label>
					  <select name="timezone" id="timezone" onChange="getMoveEventNewsDetails($('#moveEvent').val());setUserTimeZone()" class="selecttext">
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
			</div>
		</div>
		<div style="clear: both"></div>
		<div id="sum_statusbar" class="sum_statusbar_good"><span id="status_text">${moveEvent?.name}: </span><span id="status_color">GREEN</span></div>
		<!-- Header Ends here-->
		
		<!-- Body Starts here-->
		<div id="bodycontent">
		<div id="bodytop">
			<div id="plan_summary">
				<div id="topindleft">
					<div id="summary_gauge_div" align="center"> 
					<img id="summary_gauge" alt="Move Event Summary" src="${createLinkTo(dir:'i/dials',file:'dial-50.png')}">
					</div>
					<%--
					<script language="JavaScript">
						var summarychart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "summary_gauge", "280", "136", "0", "0");
	        			//summarychart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'summary_gauge.xml')}");
	        			 summarychart.setDataXML( summaryDialData( "50" ) )
						summarychart.render("summary_gauge_div");
					</script>  --%>
						Move Status vs. Plan
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
				<div style="float: right;height: 50px;width: 150px;">
					<div style="float: right;">
						<input type="button" value="Update:" id="update" onclick="pageReload();"/> 
						<select name="updateTime" id="updateTimeId" class="selecttext" onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'timedUpdate()') }">
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
 			</div>
			<div style="clear:both"></div>
			<div id="newsblock">
				<div id="newsbox">
					<div id="container" style="position:absolute;width:900px;height:70px;overflow:hidden;border:0px solid grey">
						<div id="content" style="position:relative;width:900px;left:0px;top:-15px">
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
									<li class="heading">${moveBundleStep.label}</li>
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
	<div id="crawler">
		<div id="mycrawler"><div id="mycrawlerId" style="width: 900px;margin-top: -6px;" >.</div></div>
	</div>
<!-- Footer Ends here-->
<!-- Body Ends here-->
<a name="page_down"></a>
</div>
<script type="text/javascript">
	if("${session.getAttribute('CURR_TZ')?.CURR_TZ}"){
		$("#timezone").find("option[text='${session.getAttribute('CURR_TZ')?.CURR_TZ}']").attr("selected","selected");
	} else {
		$("#timezone").find("option[text='EDT']").attr("selected","selected");
	}
	$("#updateTimeId").val("${timeToUpdate}")
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
	marqueeInit({
		uniqueid: 'mycrawler',
		inc: 8, //speed - pixel increment for each iteration of this marquee's movement
		mouse: 'cursor driven', //mouseover behavior ('pause' 'cursor driven' or false)
		moveatleast: 4,
		neutral: 150,
		savedirection: false
	});
	/*-----------------------------------------------
	* function to move the data steps to right / left
	*----------------------------------------------*/
	var	AditionalFrames = 1;
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
						x = 1;
						bundle = defaultBundle;
					}
					switch(this.id) {
						case 'move-left':
							if ( x === 1 ) {
								return;
							}
							var attributes = {
								points : {
									by : [130, 0]
								}
							};
							x--;
						break;
						case 'move-right':
							if ( x === AditionalFrames ) {
								return;
							}
							var attributes = {
								points : {
									by : [-130, 0]
								}
							};
							x++;
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
	function timedUpdate() {
		var updateTime = $("#updateTimeId").val();
		if(updateTime != 'never'){
			handler = setInterval("getMoveEventNewsDetails($('#moveEvent').val())",updateTime);
		} else {
			clearInterval(handler)
		}
	}
	/* script to assign the move evnt value*/
	var moveEvent = "${moveEvent?.id}"
	if(moveEvent){
		$("#moveEvent").val(moveEvent)
	}
	timedUpdate();
	/* Function to load the data for a particular MoveEvent */
	var doUpdate = true
	function getMoveEventNewsDetails( moveEvent ){
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
			if(state == "A"){
				archived +=	"<li><span class='newstime'>"+convertTime(offset,news[i].created)+" :</span> <span class='normaltext'>"+news[i].text+"</span></li>";
			} else {
				live +=	"<li><span class='newstime'>"+convertTime(offset,news[i].created) +" :</span> <span class='normaltext'>"+news[i].text+"</span></li>";
				scrollText +=" "+news[i].text +"..."
			}
		}
		$("#news_live").html(live);
		$("#news_archived").html(archived);
		$("#mycrawlerId").html(scrollText)
		
	}
	function setUserTimeZone(){
		var timeZone = $("#timezone :selected").text()
  		${remoteFunction(controller:'project', action:'setUserTimeZone', params:'\'tz=\' + timeZone ')}
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
		if (parseInt(crossobj.css("top"))<=-20){
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

	 /* update move bundal data once ajax call success */
	
	function updateMoveBundleSteps( dataPointStep ) {
		try{
			var offset = $("#timezone").val()
			
			var snapshot = dataPointStep.snapshot;
			var moveBundleId = snapshot.moveBundleId;
			
			var steps = snapshot.steps;
			var revSum = snapshot.revSum;
			var planSum = snapshot.planSum
			var sumDialInd = planSum.dialInd ? planSum.dialInd : 50
			AditionalFrames = ( steps.length > 6 ? steps.length - 5 : 1 );
			$("#themes").css("left","0px");
			defaultBundle = moveBundleId;
			if( sumDialInd < 25){
				$(".sum_statusbar_good").attr("class","sum_statusbar_bad")
				$(".sum_statusbar_yellow").attr("class","sum_statusbar_bad")
				$("#status_color").html("RED")
			} else if( sumDialInd >= 25 && sumDialInd < 50){
				$(".sum_statusbar_good").attr("class","sum_statusbar_yellow");
				$(".sum_statusbar_bad").attr("class","sum_statusbar_yellow");
				$("#status_color").html("YELLOW")
			} else {
				$(".sum_statusbar_bad").attr("class","sum_statusbar_good")
				$(".sum_statusbar_yellow").attr("class","sum_statusbar_good")
				$("#status_color").html("GREEN")
			}
			updateSummaryGauge("summary_gauge",planSum.dialInd ? planSum.dialInd : '50');
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
		window.location = document.URL;
	}
	</script>
</body>
</html>
