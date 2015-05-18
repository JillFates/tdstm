<html>
	<head>
		<title>Task Timeline</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<g:javascript src="asset.tranman.js"/>
		<g:javascript src="asset.comment.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="d3/d3.min.js"/>
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>	
		<g:javascript src="angular/plugins/angular-resource.js" />
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
		<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<g:javascript src="lodash/lodash.min.js" />

		<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'task-timeline.css')}" />
		<g:javascript src="task-timeline.js" />
		<script type="text/javascript">
			$(document).ready(function() {
				$('#issueTimebar').width("100%")
				$("#selectTimedBarId").val(${timeToUpdate})			
				taskManagerTimePref = ${timeToUpdate}
				$(window).resize(function() {
					B2.Restart(taskManagerTimePref)
				});
				
				if (taskManagerTimePref != 0) {
					B2.Start(taskManagerTimePref);
				} else {
					B2.Pause(0);
				}
			})
		</script>
	</head>
	<body>
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
			<h1>Task Timeline</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			Event: <g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Select a move event']}" value="${selectedEventId}" onchange="submitForm()" />
			&nbsp; Highlight: <select name="teamSelect" id="teamSelectId" style="width:120px;"></select>
			<form onsubmit="return performSearch()" id="taskSearchFormId">
				&nbsp; <input type="text" name="Search Box" id="searchBoxId" value="" placeholder="Enter highlighting filter" size="20">
				<span id="filterClearId" class="disabled ui-icon ui-icon-closethick" onclick="clearFilter()" title="Clear the current filter"></span>
				&nbsp; <input type="submit" name="Submit Button" id="SubmitButtonId" value="Highlight">
			</form>
			<span style="float:right; margin-right:30px;">
				Task Size (pixels): <input type="text" id="mainHeightFieldId" value="30" size="3"/ style="width:20px;">
				&nbsp; <input type="checkbox" id="useHeightCheckBoxId" checked="checked"> Use Heights 
				&nbsp; <input type="checkbox" id="hideRedundantCheckBoxId" checked="checked"> Hide Redundant
				
				<input type="button" value="Refresh" onclick="pageRefresh()" style="cursor: pointer;">&nbsp;
					<select id="selectTimedBarId"
					    onchange="${remoteFunction(controller:'userLogin', action:'setTimePreference', params:'\'timer=\'+ this.value +\'&prefFor=TASKMGR_REFRESH\' ', onComplete:'changeTimebarPref(e)') }">
						<option value="0" selected="selected">Manual</option>
						<option value="60">1 Min</option>
						<option value="120">2 Min</option>
						<option value="180">3 Min</option>
						<option value="240">4 Min</option>
						<option value="300">5 Min</option>
					</select>
				
			</span>
			<br>
			<span id="spinnerId" style="display: none"><img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
			<g:render template="../assetEntity/initAssetEntityData"/>
		</div>
		
		<script type="text/javascript">
			
			function pageRefresh () {
				window.location.reload()
			}

			function Bar (o) {
				var obj = document.getElementById(o.ID);
				this.oop = new zxcAnimate('width',obj,0);
				this.max = $('#issueTimebar').width();
				this.to = null;
			}
			Bar.prototype = {
				Start:function (sec) {
					clearTimeout(this.to);
					this.oop.animate(0,$('#issueTimebar').width(),sec*1000);
					this.srt = new Date();
					this.sec = sec;
					this.Time();
				},
				Time:function (sec) {
					var oop = this
						,sec=this.sec-Math.floor((new Date()-this.srt)/1000);
					//this.oop.obj.innerHTML=sec+' sec';
					$('#timeBarValueId').val(sec)
					if (sec > 0) {
						this.to = setTimeout(function(){ oop.Time(); },1000);
					} 
					else if (isNaN(sec)) {
						var timePref = $("#selectTimedBarId").val()
						if(timePref != 0){
							B2.Start(timePref);
						} else{
							B2.Pause(0);
						}
					}
					else {
						pageRefresh();
					}
				},
				Pause:function (sec) {
					console.log("Pause:function");
					clearTimeout(this.to);
					if (sec == 0) {
						this.oop.animate(sec,'',sec*1000);
					} else {
						this.oop.animate($('#issueTimebarId').width(),$('#issueTimebarId').width(),sec*1000);
					}
				},
				Restart:function (sec) {
					clearTimeout(this.to);
					var second = $('#timeBarValueId').val()
					this.oop.animate($('#issueTimebarId').width(),$('#issueTimebar').width(),second*1000);
					this.srt = new Date();
					this.sec = second;
					this.Time();
				}
			}

			B2 = new Bar({
				ID:'issueTimebarId'
			});
		</script>
	</body>
</html>