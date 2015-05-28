<html>
	<head>
		<title>Task Graph</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		
		<g:javascript src="d3/d3.min.js"/>
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>
		<g:javascript src="angular/plugins/angular-resource.js" />
		<g:javascript src="lodash/lodash.min.js" />
		
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="asset.comment.js" />
		
		<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>

		<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>

		<style type="text/css">
			g#graph0 {
				pointer-events: none;
			}
			a {
				pointer-events: auto !important;
			}
			g.node.unselected g a path,g.node.unselected g a polygon {
				stroke-width: 1px !important;
				stroke: #000000 !important;
			}
			g.node.selected g a path,g.node.selected g a polygon {
				stroke-width: 6px !important;
				stroke: #ff0000 !important;
			}
			form#taskSearchFormId {
				display: inline-block !important;
			}
			span#filterClearId {
				position: absolute !important;
				display: inline !important;
				margin-top: 4px !important;
				cursor: pointer !important;
				opacity: 1 !important;
			}
			span#filterClearId.disabled {
				opacity: 0.3 !important;
				cursor: auto !important;
			}
		</style>
		
		<script type="text/javascript">

		
		$(document).ready(function () {
			
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
			
			generateGraph($('#moveEventId').val());
			
			$(window).resize(function () {
				calculateSize();
			});
		});
		
		
		var container = null;
		var graph = null;
		var svg = null;
		var neighborhoodTaskId = ${neighborhoodTaskId};
		var background = null;
		var width = 0;
		var height = 0;
		var zoom;
		var tasks = [];
		
		function buildGraph (response, status) {
			
			// hide the loading spinner
			$('#spinnerId').css('display', 'none');
			
			// check for errors in the ajax call
			$('#errorMessageDiv').remove();
			//if (status != 'success') {
			if (response.status != 200) {
				var message = d3.select('div.body')
					.append('div')
					.attr('id','errorMessageDiv');
				message.html('<br />' + response.responseText);
				$('#spinnerId').css('display', 'none');
				return;
			}
			
			var data = $.parseJSON(response.responseText);
			
			tasks = data.tasks;
			
			// populate the Team select
			var teamSelect = $("#teamSelectId")
			teamSelect.children().remove();
			teamSelect.append('<option value="ALL">All Teams</option>');
			teamSelect.append('<option value="NONE">No Team Assignment</option>');
			teamSelect.append('<option disabled>──────────</option>');
			$.each(data.roles, function (index, team) {
				teamSelect.append('<option value="' + team + '">' + team + '</option>');
			});
			teamSelect.val('ALL');
			teamSelect.on('change', function () {
				performSearch();
			});
			
			var svgData = d3.select('div.body')
				.append('div')
				.attr('id', 'svgContainerDivId');
			
			svgData.html(data.svgText);
			
			if (height == 0)
				calculateSize();
				
			container = svg.append('g')
				.attr('id', 'containerId');
			
			
			background = container.append('rect')
				.attr('x', -500000)
				.attr('y', -500000)
				.attr('width', 1000000)
				.attr('height', 1000000)
				.attr('opacity', 0)
				.attr('class', 'background-rect')
				.on('dblclick', function () {
					zoom.translate([0, 0]).scale(1);
					graph.attr('transform', 'translate(0, 0) scale(1)');
				});
			
			graph = d3.select('#graph0');
			var graph0 = graph[0][0];

			$('#containerId')[0].appendChild(graph0);
			
			var transform = graph0.transform.animVal;
			for (var i = 0; i < transform.numberOfItems; ++i)
				if (transform[i].type == 2)
					container.attr('transform', 'translate(' + transform[i].matrix.e + ' ' + transform[i].matrix.f + ') scale(1)');
					
			graph.attr('transform', 'translate(0 0) scale(1)');
			
			$('g').children('a')
				.removeAttr('xlink:href')
				.on('click', function (a, b) {
					var parent = $(this).parent().parent();
					var id = parent.attr('id');
					if (id != 'placeholder') {
						neighborhoodTaskId = id;
						generateGraph($('#moveEventId').val());
					}
				});
			
			addBindings();
			performSearch();
			$('#exitNeighborhoodId').removeAttr('disabled');
		}
		
		function calculateSize () {
			if ($('#svgContainerDivId').size() > 0) {
			
				var padding = $('#svgContainerDivId').offset().left;
				height = $(window).innerHeight() - $('#svgContainerDivId').offset().top - padding;
				width = $(window).innerWidth() - padding * 2;
				
				
				d3.select('div.body')
					.attr('style', 'width:' + width + 'px !important; height:' + height + 'px !important;');
				d3.select('#svgContainerDivId')
					.attr('style', 'width:' + width + 'px !important; height:' + height + 'px !important;');
			
				svg = d3.select('svg')
					.attr('style', 'width:' + width + 'px !important; height:' + height + 'px !important;')
					.attr('width', width)
					.attr('height', height);
			}
		}
		
		// binds d3 zooming behavior to the svg
		function addBindings () {
			zoom = d3.behavior.zoom()
				.on("zoom", zooming)
				
			background.call(zoom);

			background.on("dblclick.zoom", null);

			function zooming () {
				graph.attr('transform', 'translate(' + d3.event.translate + ') scale(' + d3.event.scale + ')');
			}
		}
		
		function submitForm () {
			neighborhoodTaskId = -1;
			generateGraph($('#moveEventId').val());
		}

		function generateGraph (event) {
			
			$('#svgContainerDivId').remove();
			if (neighborhoodTaskId == -1) {
				$('#pageHeadingId').html('Task Graph');
				$('#exitNeighborhoodId').css('display', 'none');
			} else {
				$('#exitNeighborhoodId').attr('disabled','disabled');
				$('#pageHeadingId').html('Task Graph - Neighborhood');
				$('#exitNeighborhoodId').css('display', 'inline-block');
			}
			
			height = 0;
			var params = {'id':neighborhoodTaskId};
			if (event != 0)
				params = {'moveEventId':event, 'id':neighborhoodTaskId};
			
			// show the loading spinner
			$('#spinnerId').css('display', 'block');
			
			// calculate the position for the filter clear button
			$('#filterClearId').css('left', function () {
				return $('#searchBoxId').offset().left + $('#searchBoxId').outerWidth() - 20;
			});
			
			if (neighborhoodTaskId == -1)
				jQuery.ajax({
					dataType: 'json',
					url: tdsCommon.createAppURL('/task/moveEventTaskGraphSvg'),
					data: params,
					type:'GET',
					complete: buildGraph
				});
			else
				jQuery.ajax({
					dataType: 'json',
					url: tdsCommon.createAppURL('/task/neighborhoodGraphSvg'),
					data: params,
					type:'GET',
					complete: buildGraph
				});
		}
		
		// highlight tasks matching the user's regex
		function performSearch () {
			if (graph != null) {
				var searchString = $('#searchBoxId').val();
				var nodes = $('g.node');
				var hasSlashes = (searchString.length > 0) && (searchString.charAt(0) == '/' && searchString.charAt(searchString.length-1) == '/');
				var isRegex = false;
				var regex = /.*/;
				
				// check if the user entered an invalid regex
				if (hasSlashes) {
					try {
						regex = new RegExp(searchString.substring(1, searchString.length-1));
						isRegex = _.isRegExp(regex);
					} catch (e) {
						alert(e);
						searchString = '';
					}
				}
				
				// determine whether the "clear filter" icon should be usable
				if (searchString != '')
					$('#filterClearId').attr('class', 'ui-icon ui-icon-closethick');
				else
					$('#filterClearId').attr('class', 'disabled ui-icon ui-icon-closethick');
				
				
				var val = $('#teamSelectId').val();
				var useRegex = (searchString != '');
				var useRole = (val != 'ALL');				
				for (var i = 0; i < tasks.size(); ++i) {
					
					// check this task against the regex
					var name = tasks[i].task;
					var matchRegex = false;
					if (isRegex && name.match(regex) != null)
						matchRegex = true;
					else if (!isRegex && name.toLowerCase().indexOf(searchString.toLowerCase()) != -1)
						matchRegex = true;
							
					// check this task against the role filter
					var matchRole = false;
					if ((tasks[i].role ? tasks[i].role : 'NONE') == val)
						matchRole = true;
					
					// determine if this task should be highlighted
					var highlight = true;
					if ( (useRole && !matchRole) || (useRegex && !matchRegex) )
						highlight = false;
					if (!useRegex && !useRole)
						highlight = false;
					
					// highlight the task
					if (highlight)
						$('#' + tasks[i].id).attr('class', 'node selected');
					else
						$('#' + tasks[i].id).attr('class', 'node unselected');
				}
			}
			
			return false;
		}
		
		function clearFilter () {
			$('#searchBoxId').val('');
			performSearch();
		}
		</script>
	</head>
	<body>
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body" style="width:100%">
			<h1 id="pageHeadingId">Task Graph</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			Event: <g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Please select']}" value="${selectedEventId}" onchange="submitForm()" />
			&nbsp; Highlight: <select name="teamSelect" id="teamSelectId" style="width:120px;"></select>
			&nbsp; 
			<input type="button" name="Exit Neighborhood Graph" id="exitNeighborhoodId" value="View Entire Graph" onclick="submitForm()" />
			<form onsubmit="return performSearch()" id="taskSearchFormId">
				<input type="text" name="Search Box" id="searchBoxId" value="" placeholder="Enter highlighting filter" size="24"/>
				<span id="filterClearId" class="disabled ui-icon ui-icon-closethick" onclick="clearFilter()" title="Clear the current filter"></span>
				&nbsp; 
				<input type="submit" name="Submit Button" id="SubmitButtonId" value="Highlight" />
			</form>
			<span style="float:right; margin-right:30px;">
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
