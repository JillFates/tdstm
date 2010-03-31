<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Client Dashboard</title>
	
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tabcontent.css')}" />
	
	<g:javascript src="FusionCharts.js" />
	<g:javascript src="yahoo.js" />
	<g:javascript src="animation.js" />
	<g:javascript src="crawler.js" />
	<g:javascript src="dom.js" />
	<g:javascript src="event.js" />
	<g:javascript src="tabcontent.js" />
	<g:javascript src="textscroll.js" />
	
	<g:javascript library="jquery" />
	<jq:plugin name="ui.core" />
</head>

<body class="sum_statusbar_good" onload="getMoveEventNewsDetails($('#moveEvent').val())">
<div id="doc">
	<div id="container">
	<!--Header Starts here-->
		<div id="header">
			<div id="logo">
				<g:if test="${projectLogo}">
					<img src="${createLink(controller:'project', action:'showImage', id:projectLogo?.id)}" style="height: 55px;"/>
				</g:if>
				<g:else>
					<a href="http://www.transitionaldata.com/" target="new"><img src="${createLinkTo(dir:'images',file:'tds.jpg')}" style="float: left;border: 0px"/></a>
				</g:else>
				<br>
				<g:form action="index" controller="dashboard" name="dashboardForm">
				<span style="padding-left: 10px;">
				
					<label for="moveEvent"><b>Event:</b></label>&nbsp;<select id="moveEvent" name="moveEvent" onchange="document.dashboardForm.submit();">
						<g:each status="i" in="${moveEventsList}" var="moveEventInstance">
							<option value="${moveEventInstance?.id}">${moveEventInstance?.name}</option>
						</g:each>
					</select>
				</span>
				</g:form>
				<input type="hidden" id="typeId" value="${params.type}">
				<input type="hidden" id="stateId" value="${params.state}">
				<input type="hidden" id="maxLenId" value="${params.maxLen}">
				<input type="hidden" id="sortId" value="${params.sort}">
			</div>
			<div class="clientname">${project?.client}<br>DATA CENTER RELOCATION</div>
			<div class="topdate">
				<span><img src="${createLinkTo(dir:'images',file:'powered_by.png')}" alt="Powered by TDS" width="158" height="53" title="Powered by TDS"></span>
				<br><span id="date"></span> <span id="clock"></span>&nbsp;
				<span>
					<label>
					  <select name="timezone" id="timezone" onChange="getMoveEventNewsDetails($('#moveEvent').val())" class="selecttext">
					    <option value="0" selected>GMT</option>
					    <option value="-5">EST</option>
					    <option value="-6">CST</option>
					    <option value="-7">MST</option>
					    <option value="-8">PST</option>
					  </select>
					</label>
				</span>
			</div>
		</div>
		<div id="sum_statusbar"><span id="status_text">${moveEvent?.name}: </span><span id="status_color">GREEN</span></div>
		<!-- Header Ends here-->
		<!-- Body Starts here-->
		<div id="bodytop">
			<div id="plan_summary">
				<div id="topindleft">
					<div id="summary_gauge_div" align="center"> </div>
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
					<div id="revised_gauge_div" align="center"></div>
						Status vs. Revised Plan
				</div>
				<div id="refresh" style="float: right;">Refresh: 
					<select name="refreshTime" id="refreshTimeId" class="selecttext" onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'timedRefresh()') }">
						<option selected value="60000">1 Min</option>
	            		<option value="300000">5 Min</option>
	                	<option value="600000">10 Min</option>
	                	<option value="1800000">30 Min</option>
					</select>
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
					<span id="spnBundle${moveBundle.id}" class="${ i == 0 ? 'mbhactive' : 'mbhinactive' }" onClick="displayBundleTab(${moveBundle.id})">
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
				<input type="hidden" value="${moveBundleList[0]?.id}" id="defaultBundleId">
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
									<li class="actfinish1"><span id="act_start_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
									<li class="actfinish1"><span id="act_completion_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
								</ul>
								<div id="chartdiv_${moveBundle.id}_${moveBundleStep.transitionId}" align="center" style="display: none;"> </div>
							</div>
						</g:each>
					</div>
				</g:each>
				</div>
			</div>
<div id="rightarrow"><a href="javascript:void(0);" id="move-right"><img src="${createLinkTo(dir:'images',file:'right_arrow.png')}" alt="back" border="0" width="16" height="23" align="right"></a></div>
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
</div>
<script type="text/javascript">

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
	function moveDataSteps(){
		YAHOO.example = function() {
			
			var $D = YAHOO.util.Dom;
			var $E = YAHOO.util.Event;
			var $A = YAHOO.util.Anim;
			var $M = YAHOO.util.Motion;
			var $DD = YAHOO.util.DD;
			var $ = $D.get;
			var x = 1;
			
			return {
				init : function() {
					$E.on(['move-left','move-right'], 'click', this.move);
				},
				move : function(e) {
					$E.stopEvent(e);
					switch(this.id) {
						case 'move-left':
							if ( x === 1 ) {
								return;
							}
							var attributes = {
								points : {
									by : [132, 0]
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
									by : [-132, 0]
								}
							};
							x++;
						break;
					};
					var anim = new $M('themes', attributes, 0.5, YAHOO.util.Easing.easeOut);
					anim.animate();
				}
			};
		}();
		YAHOO.util.Event.onAvailable('doc',YAHOO.example.init, YAHOO.example, true);
	}
	moveDataSteps()
	
	/* script to assign the move evnt value*/
	var moveEvent = "${moveEvent?.id}"
	if(moveEvent){
		$("#moveEvent").val(moveEvent)
	}
	timedRefresh();
	/* Function to load the data for a particular MoveEvent */
	function getMoveEventNewsDetails( moveEvent ){
		refreshDash( $("#defaultBundleId").val() )
		if(moveEvent){
			jQuery.ajax({
		        type:"GET",
		        url:"../ws/moveEventNews/"+moveEvent+"?type="+$("#typeId").val()+"&state="+$("#stateId").val()+"&maxLen="+$("#maxLenId").val()+"&sort="+$("#sortId").val(),
		        dataType: 'json',
		        success:updateMoveEventNews
			});
		}
	}
	/* Update the Move news once ajax call success*/
	function updateMoveEventNews( news ){
		var offset = $("#timezone").val()
		var newsLength = news.length;
		var live = "";
		var archived = "";
		var scrollText = " "
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
	/* set time to load the move news and move bundle data*/
	var timer
	function timedRefresh() {
		var refreshTime = $("#refreshTimeId").val();
		timer = setTimeout("getMoveEventNewsDetails($('#moveEvent').val())",refreshTime);
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
	    var tsource2;
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
		date = new Date(temp)
		utcDate = date.getTime() ;
	    convertedDate = new Date(utcDate + (3600000*offset));                               
	    return getTimeFormate( convertedDate )
	    
	}
	function getTimeFormate( date )
	{
		var timeString = ""
		var month =  date.getMonth();
		
		if( !isNaN(month) ){
		   month = month + 1
		   var monthday    = date.getDate();
		   var year        = date.getYear() + 1900;
		   
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
		   var timeString = month+"/"+monthday+" "+hour + ':' + minute + ':' +  second + " " +  ap;
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
	
	/* display bundle tab and call refreshDash method to load the appropriate data*/
	function displayBundleTab(Id) {
		 $(".mbhactive").attr("class","mbhinactive");
		 $("#spnBundle"+Id).attr("class","mbhactive");
		 $(".show_bundle_step").attr("class","hide_bundle_step");
		 $("#bundlediv"+Id).attr("class","show_bundle_step");
		 $("#defaultBundleId").val(Id)
		 refreshDash( Id )
	 }
	/*----------------------------------------
	 * 
	 *--------------------------------------*/
	 
	 function refreshDash( bundleId ) {
		 var moveEvent = $("#moveEvent").val()
		 jQuery.ajax({
		        type:"GET",
		        url:"../ws/dashboard/bundleData/"+ bundleId+"?moveEventId="+moveEvent,
		        dataType: 'json',
		        success:updateMoveBundleSteps
			});
	 }

	 /* update move bundal data once ajax call success */
	 
	function updateMoveBundleSteps( dataPointStep ) {

		var offset = $("#timezone").val()
		
		var snapshot = dataPointStep.snapshot;
		var moveBundleId = snapshot.moveBundleId;
		var steps = snapshot.steps;
		var revSum = snapshot.revSum;
		var planSum = snapshot.planSum

		AditionalFrames = ( steps.length > 6 ? steps.length - 5 : 1 )
		
		if( snapshot.planDelta > 0){
			$(".sum_statusbar_good").attr("class","sum_statusbar_bad")
			$("#status_color").html("RED")
		} else {
			$(".sum_statusbar_bad").attr("class","sum_statusbar_good")
			$("#status_color").html("GREEN")
		}
		updateSummaryGauge("summary_gauge_div",planSum.dialInd);
		$("#spanPlanned").html(convertTime(offset, planSum.compTime))
		
		if(revSum.dialInd == "-1") {
			$("#topindright").hide();
			$("#revised_gauge_content").hide();
		} else if(snapshot.revisedComp) {

			updateRevisedGauge("revised_gauge_div",revSum.dialInd)
			$("#spanRevised").html(convertTime(offset, revSum.compTime))
			
			$("#topindright").show();
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
				startDelta = new Date(steps[i].actStart).getTime() - new Date(steps[i].planStart).getTime()
			}
			$("#act_start_"+moveBundleId+"_"+steps[i].tid).html(convertTime(offset, steps[i].actStart+": ("+ startDelta +"m)"));
			if( steps[i].actStart && !steps[i].actComp ) {
				$("#chartdiv_"+moveBundleId+"_"+steps[i].tid ).show();
				$("#act_completion_"+moveBundleId+"_"+steps[i].tid).html("<span id='databox'>Total Devices "+steps[i].tskTot+" Completed "+steps[i].tskComp+"</span>")
			} else {
				actDelta = new Date(steps[i].actComp).getTime() - new Date(steps[i].planComp).getTime()
				$("#act_completion_"+moveBundleId+"_"+steps[i].tid).html(convertTime(offset, steps[i].actComp+": ("+actDelta+"m)"));
			}
			post_init( "chartdiv_"+moveBundleId+"_"+steps[i].tid, steps[i].dialInd )
		}
		
	}
	/* function to render the dials */
	function post_init( divId, dialInd ){
		var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId2b", "100", "75", "0", "0");
		var xmlData = "<chart bgAlpha='0' bgColor='FFFFFF' lowerLimit='0' upperLimit='100' numberSuffix='' "+
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
			  		" <apply toObject='Grp1' styles='RectShadow' /> </application> </styles> </chart>"
		myChart.setDataXML( xmlData );
		myChart.render(divId);
	}
	function updateSummaryGauge( divId, dialInd ){
    var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId", "280", "136", "0", "0");
    var xmlData = "<chart bgAlpha='0' bgColor='FFFFFF' lowerLimit='0' upperLimit='100' numberSuffix='' showBorder='0' basefontColor='000000'"+ 
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
        				"<apply toObject='Grp1' styles='RectShadow' /></application></styles></chart>"
    myChart.setDataXML( xmlData );
    myChart.render(divId);
	}
	function updateRevisedGauge( divId, dialInd ){
	    var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId1", "180", "136", "0", "0");
	    var xmlData = "<chart bgAlpha='0' bgColor='FFFFFF' lowerLimit='0' upperLimit='100' numberSuffix='' showBorder='0' basefontColor='000000' "+
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
	    myChart.setDataXML( xmlData );
	    myChart.render(divId);
	    }
	</script>
</body>
</html>
