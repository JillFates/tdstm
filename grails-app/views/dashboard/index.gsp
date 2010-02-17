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
	<script type="text/javascript">
	function post_init( location, dialInd ){
		var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId2b", "100", "75", "0", "0");
		myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'step_gauge.xml')}");
		myChart.render(location);
	}
	</script>
</head>

<body class="sum_statusbar_good" onLoad="getMoveEventNewsDetails($('#moveEvent').val())">
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
				<span style="padding-left: 10px;">
					<label for="moveEvent"><b>Event:</b></label>&nbsp;
					<select id="moveEvent" name="moveEvent" onchange="getMoveEventNewsDetails(this.value)">
						<g:each status="i" in="${moveEventsList}" var="moveEventInstance">
							<option value="${moveEventInstance?.id}">${moveEventInstance?.name}</option>
						</g:each>
					</select>
				</span>
				<input type="hidden" id="typeId" value="${params.type}">
				<input type="hidden" id="stateId" value="${params.state}">
				<input type="hidden" id="maxLenId" value="${params.maxLen}">
				<input type="hidden" id="sortId" value="${params.sort}">
			</div>
			<div class="clientname">DEVON ENERGY<br>DATA CENTER RELOCATION</div>
			<div class="topdate">
				<span><img src="${createLinkTo(dir:'images',file:'powered_by.png')}" alt="Powered by TDS" width="158" height="53" title="Powered by TDS"></span>
				<br><span id="date"></span> <span id="clock"></span>&nbsp;
				<span>
					<label>
					  <select name="timezone" id="timezone" onChange="convertTimeZones();" class="selecttext">
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
		<div id="sum_statusbar"><span id="status_text">02/12 MOVE STATUS : GREEN</span></div>
		<!-- Header Ends here-->
		<!-- Body Starts here-->
		<div id="bodytop">
			<div id="plan_summary">
				<div id="topindleft">
					<div id="summary_gauge_div" align="center"> </div>
						<script type="text/javascript">
				        var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId", "280", "136", "0", "0");
				        myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'summary_gauge.xml')}");
				        myChart.render("summary_gauge_div");
				        </script>Move Status vs. Plan
				</div>
				<div class="topleftcontent">
						Planned Completion<br>
						<input type="hidden" id="hdnPlan" value="12/12: 07:00 AM" />
						<!--12/12: 07:00 AM EST&#13;-->
						<span id="spanPlan">12/12: 07:00 AM</span> <span id="spanPlanTZ"></span>
				</div>
			</div>
			<div id="revised_summary" >
				<div id="topindright">
					<div id="revised_gauge_div" align="center"></div>
						<script type="text/javascript">
			            var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId1", "180", "136", "0", "0");
			            myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'revised_gauge.xml')}");
			            myChart.render("revised_gauge_div");
			            </script>Status vs. Revised Plan
				</div>
				<div id="toprightbox">
					<div id="refresh" style="float: right;">Refresh: 
						<select name="refreshTime" id="refreshTimeId" class="selecttext">
							<option selected value="60000">1 Min</option>
	            			<option value="300000">5 Min</option>
	                    	<option value="600000">10 Min</option>
	                        <option value="1800000">30 Min</option>
						</select>
					</div>
					<div class="toprightcontent" id="revised_gauge_content">
						Confidence in Revised Plan<br>
						<span class="high">High</span><br>
						<span class="redfont">Planned Completion:&#13;
						<input type="hidden" id="hdnPlan1" value="12/12: 07:00 AM" />
						<!--12/12: 07:00 AM EST&#13;-->
						<br />
						<span id="spanPlan1">12/12: 07:00 AM</span> <span id="span2"></span></span>
					</div>
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
					<li><span class="percentage">Completion</span></li>
					<li>Planned Start</li>
					<li>Planned Completion</li>
					<li>Actual Start</li>
					<li >Actual Completion</li>
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
									<li class="actfinish1"><span id="completion_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
									<li class="schstart"><span id="plan_start_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
									<li class="schfinish"><span id="plan_completion_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
									<li class="actfinish1"><span id="act_start_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
									<li class="actfinish1"><span id="act_completion_${moveBundle.id}_${moveBundleStep.transitionId}"></span>&nbsp;</li>
								</ul>
								<div id="chartdiv_${moveBundle.id}_${moveBundleStep.transitionId}" align="center" style="display: none;"> </div>
								<script type="text/javascript">post_init( "chartdiv_${moveBundle.id}_${moveBundleStep.transitionId}", "${moveBundle.id}" ) </script>
							</div>
						</g:each>
					</div>
				</g:each>
				</div>
			<%--<div id="wunracking">
					<div id="bottomwidgets">
						<div id="chartdiv1b" align="center"></div>
						<script type="text/javascript">
						var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId1b", "100", "75", "0", "0");
						myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'step_gauge.xml')}");
						myChart.render("chartdiv1b");
						</script>
					</div>
					<div id="bottomwidgets">
						<div id="chartdiv2b" align="center"></div>
						<script type="text/javascript">
							var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId2b", "100", "75", "0", "0");
							myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'step_gauge.xml')}");
							myChart.render("chartdiv2b");
						</script>
					</div>
					<div id="bottomwidgets">
						<div id="chartdiv3b" align="center"></div>
						<script type="text/javascript">
							var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId3b", "100", "75", "0", "0");
							myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'step_gauge.xml')}");
							myChart.render("chartdiv3b");
						</script>
					</div>
					<div id="bottomwidgets">
						<div id="chartdiv4b" align="center"></div>
						<script type="text/javascript">
							var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId4b", "100", "75", "0", "0");
							myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'step_gauge.xml')}");
							myChart.render("chartdiv4b");
						</script>
					</div>
					<div id="bottomwidgets">
						<div id="chartdiv5b" align="center"></div>
						<script type="text/javascript">
							var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId5b", "100", "75", "0", "0");
							myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'step_gauge.xml')}");
							myChart.render("chartdiv5b");
						</script>
					</div>
					<div id="bottomwidgets">
						<div id="chartdiv6b" align="center"></div>
						<script type="text/javascript">
							var myChart = new FusionCharts("${createLinkTo(dir:'swf',file:'AngularGauge.swf')}", "myChartId6b", "100", "75", "0", "0");
							myChart.setDataURL("${createLinkTo(dir:'resource/dashboard',file:'step_gauge.xml')}");
							myChart.render("chartdiv6b");
						</script>
					</div>
				</div> --%>
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
	function moveDateSteps(){
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
	moveDateSteps()
	
	/* script to assign the move evnt value*/
	var moveEvent = "${moveEvent?.id}"
	if(moveEvent){
		$("#moveEvent").val(moveEvent)
	}
	timedRefresh();
	/* Function to load the data for a particular MoveEvent */
	function getMoveEventNewsDetails( moveEvent ){
		if(moveEvent){
			jQuery.ajax({
		        type:"GET",
		        url:"../ws/moveEventNews/"+moveEvent+"?type="+$("#typeId").val()+"&state="+$("#stateId").val()+"&maxLen="+$("#maxLenId").val()+"&sort="+$("#sortId").val(),
		        dataType: 'json',
		        success:updateMoveEventNews,
		        error:function( data, error ) {
		            alert("error = "+error);
		        }
			});
		}
	}
	/* Update the Move news once ajax call success*/
	function updateMoveEventNews( news ){
		var newsLength = news.length;
		var live = "";
		var archived = "";
		var scrollText = " "
			var myDate = new Date();
		for( i = 0; i< newsLength; i++){
			var state = news[i].state;
			if(state == "A"){
				archived +=	"<li><span class='newstime'>"+news[i].created+":</span> <span class='normaltext'>"+news[i].text+"</span></li>";
			} else {
				live +=	"<li><span class='newstime'>"+news[i].created+":</span> <span class='normaltext'>"+news[i].text+"</span></li>";
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
	 function convertTimeZones() {            
		offset = $("#timezone").val();                
	   // var xmlFileName = "bundledata" + $("#bundleState").val() + ".xml"  
		var hdnState = $("#hdnState");
	    //bundledata(parseInt(hdnState.val()), xmlFileName);
	    convertTime(offset, $("#hdnPlan"), $("#spanPlan"));
	    convertTime(offset, $("#hdnPlan1"), $("#spanPlan1"));
	}
	function convertTime(offset, source, target){
		//12/12: 12:30 AM: (-210m)        
	    //d1 = new Date("12/12/2010 12:30 AM")        
		temp = source.val()
		dtemp = trimAll(temp.substring(0,5))
	    ttemp = trimAll(temp.substring(5,temp.length))
	    temp = dtemp + "/2010 " + ttemp                      

	    d1 = new Date(temp)
	    utc1 = d1.getTime() + (d1.getTimezoneOffset() * 60000);
	    nd1 = new Date(utc1 + (3600000*offset));                               
	    date = new Date(nd1.toLocaleString());

	    //Constructs the time part (hr:mm AM/PM)
	    gettime = nd1.toLocaleString();                        
	    gettime = trimAll(gettime.substring(gettime.length-11, gettime.length))                        
	    var elementSubstr = gettime.substring(0, gettime.length).split(":");
	    gettime = elementSubstr[0] + ":" + elementSubstr[1] + " " + trimAll(elementSubstr[2].substring(elementSubstr[2].length-2, elementSubstr[2].length))
	    
	    //Extracts the month part            
	    month = nd1.getMonth() + 1      
	        
	    target.innerHTML = month + "/" + nd1.getDate() + ": " + gettime;                        

	}
	function setTimeZone(){
		$("#spanPlanTZ").html($("#timezone").options[$("#timezone").val()].text ) ;
	    $("#span2").html($("#timezone").options[$("#timezone").val()].text );
	}
	function convertTimeforXML(offset, source){
		//12/12: 12:30 AM: (-210m)        
	    //d1 = new Date("12/12/2010 12:30 AM")
	    if (source ==  ""){
	    	return ""
		} else if (source.substring(0,5).toLowerCase() ==  "total"){
			return source
		}

		temp = source
		d1 = new Date(temp)
	    utc1 = d1.getTime() + (d1.getTimezoneOffset() * 60000);
	    nd1 = new Date(utc1 + (3600000*offset));                               
	    date = new Date(nd1.toLocaleString());
	        
	    //Constructs the time part (hr:mm AM/PM)
	    gettime = nd1.toLocaleString();                        
	    gettime = trimAll(gettime.substring(gettime.length-11, gettime.length))                        
	    var elementSubstr = gettime.substring(0, gettime.length).split(":");
	    gettime = elementSubstr[0] + ":" + elementSubstr[1] + " " + trimAll(elementSubstr[2].substring(elementSubstr[2].length-2, elementSubstr[2].length))
	        
	    //Extracts the month part            
	    month = nd1.getMonth() + 1      
	    
	    return month + "/" + nd1.getDate() + ": " + gettime;                        
}
	    
	function convertTimeforXML_AS_AF(offset, source) {
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
	        tsource1 = tsource1.substring(0,tsource1.length-1);
	        temp = tsource1;
	        tsource2 = trimAll(tsource[1]);        
	        tsource2 = "(" + tsource2;
		} else {
	    	temp = trimAll(source);
		}       
	                         
		d1 = new Date(temp)
	    utc1 = d1.getTime() + (d1.getTimezoneOffset() * 60000);
	    nd1 = new Date(utc1 + (3600000*offset));                               
	    date = new Date(nd1.toLocaleString());
	        
	    //Constructs the time part (hr:mm AM/PM)
	    gettime = nd1.toLocaleString();                        
	    gettime = trimAll(gettime.substring(gettime.length-11, gettime.length))                        
		try{
			var elementSubstr = gettime.substring(0, gettime.length).split(":");
	    	gettime = elementSubstr[0] + ":" + elementSubstr[1] + " " + trimAll(elementSubstr[2].substring(elementSubstr[2].length-2, elementSubstr[2].length))
	        
			//Extracts the month part            
	        month = nd1.getMonth() + 1      
	        
	        if (p == ")"){
	            return month + "/" + nd1.getDate() + ": " + gettime + ": " + tsource2;            
	        } else {
	            return month + "/" + nd1.getDate() + ": " + gettime;
	        }

		} catch(e) {
	              // alert error message
		}
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
		        success:updateMoveBundleSteps,
		        error:function( data, error ) {
		            alert("error = "+error);
		        }
			});
	 }

	 /* update move bundal data once ajax call success */
	 
	function updateMoveBundleSteps( dataPointStep ) {
		var snapshot = dataPointStep.snapshot;
		var moveBundleId = snapshot.moveBundleId;
		var steps = snapshot.steps;
		var revSum = snapshot.revSum;
		
		for( i = 0; i < steps.length; i++ ) {
			var offset = $("#timezone").val()
			$("#completion_"+moveBundleId+"_"+steps[i].tid).html(steps[i].projComp);
			$("#plan_start_"+moveBundleId+"_"+steps[i].tid).html(steps[i].planStart);
			$("#plan_completion_"+moveBundleId+"_"+steps[i].tid).html(steps[i].planComp);
			$("#act_start_"+moveBundleId+"_"+steps[i].tid).html(steps[i].actStart);
			if( steps[i].actStart && !steps[i].actComp ) {
				$("#chartdiv_"+moveBundleId+"_"+steps[i].tid ).show();
				$("#act_completion_"+moveBundleId+"_"+steps[i].tid).html("<span id='databox'>Total Devices "+steps[i].tskTot+" Completed "+steps[i].tskComp+"</span>")
			} else {
				$("#act_completion_"+moveBundleId+"_"+steps[i].tid).html(steps[i].actComp);
			}
		}
		if(revSum.dialInd == "-1") {
			$("#topindright").hide();
			$("#revised_gauge_content").hide();
		} else if(snapshot.moveEvent.revisedCompletionTime) {
			$("#topindright").show();
			$("#revised_gauge_content").show();
		}
		
	}
		
		/* get first bundle data*/
		refreshDash( $("#defaultBundleId").val() )
	 <%--
	 var str = 1;
	function bundledata(str, xmlFileName) {
	    var bundletext = '';
	    if (window.XMLHttpRequest) {
	        xhttp = new XMLHttpRequest();
	    }
	    else // Internet Explorer 5/6
	    {
	        xhttp = new ActiveXObject("Microsoft.XMLHTTP");
	    }
	    xhttp.open("GET", xmlFileName, false);
	    xhttp.send(null);
	    xmlDoc = xhttp.responseXML;
	    
	    var x = xmlDoc.getElementsByTagName("Bundle" + str);
		var bundleChilds = x[0].childNodes
		var xLength = bundleChilds.length
	if (xLength>12){
		AditionalFrames = (((xLength-1)/2) -5);
	}
		for(i=1; i < xLength ; i = i+2){
		    bundletext +="<div style='float:left;margin-left:1px;margin-right:1px;width:130px;'><ul class=\"bdetails\"><li class=\"heading\">"+bundleChilds[i].getAttribute('name')+"</li>"
		    bundletext +="<li class="+bundleChilds[i].getAttribute('clsName')+">";
		    bundletext += bundleChilds[i].getAttribute('wid');
		    bundletext += "</li><li class=\"schstart\">"+convertTimeforXML($("#timezone").val(), bundleChilds[i].getAttribute('Schstart'))+"</li>";
		    bundletext += "<li class=\"schfinish\">"+convertTimeforXML($("#timezone").val(), bundleChilds[i].getAttribute('Schfinish'))+"</li>";
		    bundletext += "<li class="+bundleChilds[i].getAttribute('ActstartClass')+">"+convertTimeforXML_AS_AF($("#timezone").val(), bundleChilds[i].getAttribute('Actstart'))+"&nbsp;</li>";
		    bundletext += "<li class=\"actfinish1\"><span id="+bundleChilds[i].getAttribute('ActfinishClass')+">"+convertTimeforXML_AS_AF($("#timezone").val(), bundleChilds[i].getAttribute('Actfinish'))+"</span>&nbsp;</li></ul></div>";
		}
		$('#bundlediv').html(bundletext);

		 var val1 = x[0].getElementsByTagName("Unracking")[0].getAttribute('wid');
	     if (val1 == "100%" || val1 == "0%") {
	         $("#chartdiv1b").hide();
	     }
	     else { $("#chartdiv1b").show(); }

	     val1 = x[0].getElementsByTagName("Staging")[0].getAttribute('wid');
	     if (val1 == "100%" || val1 == "0%") {
	         $("#chartdiv2b").hide();
	     }
	     else { $("#chartdiv2b").show(); }

	     val1 = x[0].getElementsByTagName("Transport")[0].getAttribute('wid');
	     if (val1 == "100%" || val1 == "0%") {
	         $("#chartdiv3b").hide();
	     }
	     else { $("#chartdiv3b").show(); }

	     val1 = x[0].getElementsByTagName("Staging-2")[0].getAttribute('wid');
	     if (val1 == "100%" || val1 == "0%") {
	         $("#chartdiv4b").hide();
	     }
	     else { $("#chartdiv4b").show(); }

	     val1 = x[0].getElementsByTagName("Reracking")[0].getAttribute('wid');
	     if (val1 == "100%" || val1 == "0%") {
	         $("#chartdiv5b").hide();
	     }
	     else { $("#chartdiv5b").show(); }

	     val1 = x[0].getElementsByTagName("Cabling")[0].getAttribute('wid');
	     if (val1 == "100%" || val1 == "0%") {
	         $("#chartdiv6b").hide();
	     }
	     else { $("#chartdiv6b").show(); }
	 }
	 
	var	AditionalFrames = 1; // sdjflkasdjf 
    var count = 4;
    var prev = 0;
    function next1(x) {
        var bundleValue = $("#bundleState").val();
        refreshDash('next', bundleValue);

        if (count < 16) {
            prev = x;
            count = count + 4;
            var a = x + 1;
            var b = x + 2;
            var c = x + 3;
            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId", "280", "136", "0", "0");
            myChart.setDataURL("Data/TopAngular" + count + ".xml");
            myChart.render("summary_gauge_div");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId1", "180", "136", "0", "0");
            myChart.setDataURL("Data/TopAngular" + a + ".xml");
            myChart.render("revised_gauge_div");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId1b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + b + ".xml");
            myChart.render("chartdiv1b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId2b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + c + ".xml");
            myChart.render("chartdiv2b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId3b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + b + ".xml");
            myChart.render("chartdiv3b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId4b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + c + ".xml");
            myChart.render("chartdiv4b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId5b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + b + ".xml");
            myChart.render("chartdiv5b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId6b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + c + ".xml");
            myChart.render("chartdiv6b");
        }
        else {
            //alert("No Data");
        }
    }

    function back1(x) {
        var bundleValue = $("#bundleState").val();
        refreshDash('pre', bundleValue);

        if (prev >= 4) {
            prev = prev - 4;
            var a = x - 3;
            var b = x - 2;
            var c = x - 1;
            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId", "280", "136", "0", "0");
            myChart.setDataURL("Data/TopAngular" + x + ".xml");
            myChart.render("summary_gauge_div");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId1", "180", "136", "0", "0");
            myChart.setDataURL("Data/TopAngular" + a + ".xml");
            myChart.render("revised_gauge_div");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId1b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + b + ".xml");
            myChart.render("chartdiv1b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId2b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + c + ".xml");
            myChart.render("chartdiv2b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId3b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + b + ".xml");
            myChart.render("chartdiv3b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId4b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + c + ".xml");
            myChart.render("chartdiv4b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId5b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + b + ".xml");
            myChart.render("chartdiv5b");

            var myChart = new FusionCharts("../Charts/AngularGauge.swf", "myChartId6b", "100", "75", "0", "0");
            myChart.setDataURL("Data/Angular" + c + ".xml");
            myChart.render("chartdiv6b");
        }
        else {
            //alert("No Data");
        }
    }
    
	var dayarray=new Array("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")
	var montharray=new Array("January","February","March","April","May","June","July","August","September","October","November","December")
	
	function getthedate(){
	   var mydate=new Date()
	   var year=mydate.getYear()
	   if (year < 1000)
	      year+=1900
	   var day=mydate.getDay()
	   var month=mydate.getMonth()
	   var daym=mydate.getDate()
	   if (daym<10)
	      daym="0"+daym
	   var hours=mydate.getHours()
	   var minutes=mydate.getMinutes()
	   var seconds=mydate.getSeconds()
	   var dn="AM"
	   if (hours>=12)
	      dn="PM"
	   if (hours>12){
	      hours=hours-12
	   }
	   if (hours==0)
	      hours=12
	   if (minutes<=9)
	      minutes="0"+minutes
	   if (seconds<=9)
	      seconds="0"+seconds
	   //change font size here
	   var cdate="<small><font color='000000' face='Arial'><b>"+hours+":"+minutes+" "+dn+"</b></font></small>"
	   var ddate="<small><font color='000000' face='Arial'><b>"+mydate.toLocaleDateString()+"</b></font></small>"
	   if (document.all)
	      document.all.clock.innerHTML=cdate
	      else if (document.getElementById) {
	         $("#clock").html(cdate);
	         $("#date").html(ddate);
	      }
	      else
	         document.write(cdate, cdate1)
	      }
	   if (!document.all&&!document.getElementById)
	      getthedate()
	
	function goforit(){
	if (document.all||document.getElementById)
	setInterval("getthedate()",1000)
	}
	--%>
	</script>
</body>
</html>
