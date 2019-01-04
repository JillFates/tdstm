<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<title>Task Graph</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />

		<asset:stylesheet href="components/comment/comment.css" />
		<asset:stylesheet href="css/force.css" />

		<g:javascript src="asset.comment.js" />
		<g:javascript src="d3/d3.js"/>
		<g:render template="/layouts/responsiveAngularResources" />
		<g:javascript src="lodash/lodash.min.js" />
		<g:javascript src="progressTimer.js" />
		<g:javascript src="keyevent_constants.js" />
		<g:javascript src="graph.js" />

		<g:javascript src="asset.tranman.js" />

		<script type="text/javascript">

		var progressTimer;

		$(document).ready(function () {

			// refresh timer initialization
			progressTimer = new ProgressTimer(0, '${com.tdsops.tm.enums.domain.UserPreferenceEnum.TASKGRAPH_REFRESH}', function () {
				generateGraph($('#moveEventId').val());
				progressTimer.resetTimer();
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

		// builds the graph from the server's response
		function buildGraph (response, status) {

			// hide the loading spinner
			$('#spinnerId').css('display', 'none');

			// check for errors in the ajax call
			$('#errorMessageDiv').remove();
			if (response.status != 200) {
				var message = d3.select('div#taskGraphId')
					.append('div')
					.attr('id','errorMessageDiv');
				message.html('<br />' + response.responseText);
				$('#spinnerId').css('display', 'none');
				$('#exitNeighborhoodId').removeAttr('disabled');
				return;
			}


			// parse the data from the server
			var data = $.parseJSON(response.responseText);
			tasks = data.tasks;

			// populate the Team select
			var teamSelect = GraphUtil.populateTeamSelect(data);
			teamSelect.on('change', function () {
				performSearch();
			});

			// construct the svg container
			var svgData = d3.select('div#taskGraphId')
				.append('div')
				.attr('id', 'svgContainerDivId');

			// add the prerendered svg data into the container
			svgData.html(data.svgText);

			// calculate the graph size if it hasn't been done already
			if (height == 0)
				calculateSize();

			// construct the svg container
			container = svg.append('g')
				.attr('id', 'containerId');

			// construct the background
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

			// wrap the main graph group tag in a container group tag
			graph = d3.select('#graph0').attr('transform', GraphUtil.NO_TRANSFORM);
			var graph0 = graph[0][0];
			$('#containerId')[0].appendChild(graph0);

			// bind the click handlers for the nodes
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

			// add the key listeners for the graph
			transformContainer = graph;
			GraphUtil.addKeyListeners();

			// set up the zooming and panning mouse behavior
			addBindings();

			// center the graph in the container
			getDefaultPosition();

			// finish initialization and exit
			performSearch();
			$('#exitNeighborhoodId').removeAttr('disabled');

			// Adding bold and italic styles to automated tasks (TM-7458)
            for(i = 0; i < data.automatedTasks.length; i++) {
                var autoTask = "#" + data.automatedTasks[i];
                $(autoTask).css('font-weight','bold');
                $(autoTask).css('font-style','italic');
            }
		}

		// sets the proper width and height for the graph
		function calculateSize () {
			if ($('#svgContainerDivId').size() > 0) {

				var padding = $('#svgContainerDivId').offset().left;
				heightCurrent = $(window).innerHeight() - $('#svgContainerDivId').offset().top - (padding / 2) - GraphUtil.getFooterHeight();
				widthCurrent = $(window).innerWidth() - padding * 2;

				d3.select('div#taskGraphId')
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

		// exits the neighborhood and generates the graph for the selected event
		function submitForm () {
			neighborhoodTaskId = -1;
			generateGraph($('#moveEventId').val());
		}

		// gets the given event's graph data from the server and then renders it
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
			// temporarily get rid of the scale and translate to simplify calculations
			zoomBehavior.translate([0, 0]).scale(1).event(background)

			// determine new scale and translate based on the graph size and canvas size
			var graphBounds = graph0.getBoundingClientRect()
			var xScale = widthCurrent / graphBounds.width
			var yScale = heightCurrent / graphBounds.height
			var newScale = Math.min(xScale, yScale)
			var newTop = (heightCurrent / 2) + (newScale * graphBounds.height / 2)
			var newLeft = (widthCurrent / 2) - (newScale * graphBounds.width / 2)

			// set the new scale and translate
			zoomBehavior
				.translate([newLeft, newTop])
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

		// clears the highlighting filter and removes all highlighting
		function clearFilter () {
			$('#searchBoxId').val('');
			performSearch();
		}

		// specifies which menu options are active
		$(".menu-parent-tasks-task-graph").addClass('active');
		$(".menu-parent-tasks").addClass('active');

		</script>
	</head>
	<body>
		<tds:subHeader title="Task Graph" crumbs="['Task','Graph']"/>
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar hide" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body" style="width:100%">
			<div id="taskGraphId" class="graphContainer" style="width:100%">
				<div id="toolsContainerId">
					<div id="graphToolbarId">
						<!-- Flash message -->
						<g:if test="${flash.message}">
							<div class="message">${flash.message}</div>
						</g:if>

						<!-- Event select -->
						<span class="controlWrapper">
							<label for="moveEventId">Event:</label>
							<g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Please select']}" value="${selectedEventId}" onchange="submitForm()" />
						</span>

						<!-- Team highlighting select -->
						<span class="controlWrapper">
							<label for="teamSelectId">Highlight:</label>
							<select name="teamSelect" id="teamSelectId" style="width:120px;"></select>
						</span>

						<!-- Exit Neighborhood button -->
						<input type="button" name="Exit Neighborhood Graph" id="exitNeighborhoodId" value="View Entire Graph" onclick="submitForm()" />

						<!-- Highlight filter controls -->
						<div id="highlightFormId" class="newHighlightForm">
							<input type="text" id="searchBoxId" name="Search Box" class="fullButton" value="" placeholder="Enter highlighting filter" onkeydown="GraphUtil.handleSearchKeyEvent(event, performSearch)"/>
							<span id="filterClearId" class="disabled ui-icon ui-icon-closethick" onclick="clearFilter()" title="Clear the current filter"></span>
							<span id="filterSubmitButtonId" class="graphButton" onclick="performSearch()" title="Applies the selected filtering options to the graph"></span>
						</div>

						<!-- Zoom buttons -->
						<div id="zoomInButtonId" class="graphButton graphTabButton zoomButton pointer hasMargin" onclick="GraphUtil.zoomIn()"></div>
						<div id="zoomOutButtonId" class="graphButton graphTabButton zoomButton pointer" onclick="GraphUtil.zoomOut()"></div>

						<!-- View unpublished checkbox (if the user has permission) -->
						<tds:hasPermission permission="${Permission.TaskPublish}">
							<span class="checkboxContainer">
								<input type="checkbox" name="viewUnpublished" id="viewUnpublishedId" class="pointer" ${ (viewUnpublished=='1' ? 'checked="checked"' : '') } />
								<label for="viewUnpublishedId" class="pointer">&nbsp;View Unpublished</label>
							</span>
						</tds:hasPermission>

						<!-- Refresh timer -->
						<span style="float:right; margin-right: 12px;">
							<g:render template="/assetEntity/progressTimerControls" model="${[timerValues:[60, 120, 180, 240, 300]]}"/>
						</span>
					</div>
				</div>
				<span id="spinnerId" style="display: none"><asset:image src="images/spinner.gif"/></span>
			</div>
		</div>
		<g:render template="/layouts/error"/>
		<script>
			$(".menu-parent-tasks-task-graph").addClass('active');
			$(".menu-parent-tasks").addClass('active');
		</script>
	</body>
</html>
