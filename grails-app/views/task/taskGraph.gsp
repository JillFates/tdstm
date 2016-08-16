<html>
	<head>
		<title>Task Graph</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'force.css')}" />
		
		<g:javascript src="asset.comment.js" />
		<g:javascript src="d3/d3.js"/>
		<g:render template="../layouts/responsiveAngularResources" />
		<g:javascript src="lodash/lodash.min.js" />
		<g:javascript src="TimerBar.js" />
		<g:javascript src="graph.js" />
		
		<g:javascript src="asset.tranman.js" />

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
		</style>
		
		<script type="text/javascript">
		
		var timerBar;
		
		$(document).ready(function () {
		
			// refresh timer initialization
			timerBar = new TimerBar(0, 'RefreshTaskGraph', function () {
				generateGraph($('#moveEventId').val());
				timerBar.resetTimer();
			});
			
			// set the view unpublished checkbox to be checked if the parameter was passed as true
			$('#viewUnpublishedId').on('change', function () {
				generateGraph($('#moveEventId').val());
			});
			
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
		var transformContainer = null;
		var widthCurrent = 0;
		var heightCurrent = 0;
		var zoomBehavior = null;
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
				$('#exitNeighborhoodId').removeAttr('disabled');
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
				.attr('id', 'svgContainerDivId')
				.attr('tabindex', '1');
			
			
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
					getDefaultPosition()
				});
			
			graph = d3.select('#graph0');
			var graph0 = graph[0][0];
			
			$('#containerId')[0].appendChild(graph0);
			
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
			
			
			transformContainer = graph;
			var outerContainer = $('#svgContainerDivId')
			var modifier = 1
			GraphUtil.addKeyListeners(outerContainer, modifier);
			
			addBindings();
			getDefaultPosition();
			performSearch();
			$('#exitNeighborhoodId').removeAttr('disabled');
		}
		
		function calculateSize () {
			if ($('#svgContainerDivId').size() > 0) {
			
				var padding = $('#svgContainerDivId').offset().left;
				heightCurrent = $(window).innerHeight() - $('#svgContainerDivId').offset().top - padding;
				widthCurrent = $(window).innerWidth() - padding * 2;
				
				
				d3.select('div.body')
					.attr('style', 'width:' + widthCurrent + 'px !important; height:' + heightCurrent + 'px !important;');
				d3.select('#svgContainerDivId')
					.attr('style', 'width:' + widthCurrent + 'px !important; height:' + heightCurrent + 'px !important;');
			
				svg = d3.select('#svgContainerDivId svg')
					.attr('style', 'width:' + widthCurrent + 'px !important; height:' + heightCurrent + 'px !important;')
					.attr('viewBox', null)
					.attr('width', widthCurrent)
					.attr('height', heightCurrent);
			}
		}
		
		// binds d3 zooming behavior to the svg
		function addBindings () {
			zoomBehavior = d3.behavior.zoom()
				.on("zoom", zooming)
				
			background.call(zoomBehavior);

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
			
			// if the view unpublished checkbox exists, add this value to the parameters for the ajax calls
			if ($('#viewUnpublishedId').size() > 0) {
				if ($('#viewUnpublishedId').is(':checked'))
					params.viewUnpublished = 1;
				else
					params.viewUnpublished = 0;
			}
			
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
		
		// fits the graph into the centered position where everything is visible
		function getDefaultPosition () {
			zoomBehavior.translate([0, 0]).scale(1).event(background)
			var graphBounds = graph0.getBoundingClientRect();
			var newScale = widthCurrent / graphBounds.width;
			var newTop = (heightCurrent / 2) - (newScale * graphBounds.height / 2) + 270;
			zoomBehavior
				.translate([0, newTop])
				.scale(newScale)
				.event(background)
		}
		
		// highlight tasks matching the user's regex
		function performSearch () {
			try {
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
						var name = (tasks[i].comment) ? (tasks[i].comment) : (tasks[i].task);
						var taskNumber = (tasks[i].taskNumber) ? (tasks[i].taskNumber) : (tasks[i].task_number);
						name = taskNumber + ':' + name;
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
			} catch (e) {
				alert('Error occurred while performing highlight search');
				console.log(e);
				return false;
			}
			
			return false;
		}
		
		function clearFilter () {
			$('#searchBoxId').val('');
			performSearch();
		}

		$(".menu-parent-tasks-task-graph").addClass('active');
		$(".menu-parent-tasks").addClass('active');

		</script>
	</head>
	<body>
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar hide" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body graphContainer" style="width:100%">
			<h1 id="pageHeadingId">Task Graph</h1>
			<div id="graphToolbarId">
				<g:if test="${flash.message}">
					<div class="message">${flash.message}</div>
				</g:if>
				<span class="controlWrapper">
					<label for="moveEventId">Event:</label>
					<g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Please select']}" value="${selectedEventId}" onchange="submitForm()" />
				</span>
				<span class="controlWrapper">
					<label for="teamSelectId">Highlight:</label>
					<select name="teamSelect" id="teamSelectId" style="width:120px;"></select>
				</span>
				<input type="button" name="Exit Neighborhood Graph" id="exitNeighborhoodId" value="View Entire Graph" onclick="submitForm()" />
				<form onsubmit="return performSearch()" id="taskSearchFormId">
					<input type="text" name="Search Box" id="searchBoxId" value="" placeholder="Enter highlighting filter" size="24"/>
					<span id="filterClearId" class="disabled ui-icon ui-icon-closethick" onclick="clearFilter()" title="Clear the current filter"></span>
					<input type="submit" name="Submit Button" id="SubmitButtonId" class="pointer" value="Highlight" />
				</form>
				<div id="zoomInButtonId" class="graphButton graphTabButton zoomButton pointer hasMargin"></div>
				<div id="zoomOutButtonId" class="graphButton graphTabButton zoomButton pointer"></div>
				<tds:hasPermission permission="PublishTasks">
					<span class="checkboxContainer">
						<input type="checkbox" name="viewUnpublished" id="viewUnpublishedId" class="pointer" ${ (viewUnpublished=='1' ? 'checked="checked"' : '') } />
						<label for="viewUnpublishedId" class="pointer">&nbsp;View Unpublished</label>
					</span>
				</tds:hasPermission>
				<span style="float:right;">
					<g:render template="../assetEntity/progressTimerControls" model="${[timerValues:[60, 120, 180, 240, 300]]}"/>
				</span>
			</div>
			<span id="spinnerId" style="display: none"><img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
		</div>
		<g:render template="../layouts/error"/>
		<script>
			$(".menu-parent-tasks-task-graph").addClass('active');
			$(".menu-parent-tasks").addClass('active');
		</script>
	</body>
</html>
