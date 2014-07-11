<html>
	<head>
		<title>Task Graph</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		
		<g:javascript src="jquery-1.9.1.js"/>	
		<g:javascript src="d3/d3.min.js"/>
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>	
		<g:javascript src="angular/plugins/angular-resource.js" />
		<g:javascript src="asset.comment.js" />
		
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
		</style>
		
		<script type="text/javascript">

		
		$(document).ready(function () {
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
			
			// var data = $.parseJSON(response.responseText);
			var svgData = d3.select('div.body')
				.append('div')
				.attr('id', 'svgContainerDivId');
			
			svgData.html(response.responseText);
			
			if (height == 0)
				calculateSize();
				
			container = svg.append('g')
				.attr('id', 'containerId');
			
			
			background = container.append('rect')
				.attr('x', -5000)
				.attr('y', -5000)
				.attr('width', 10000)
				.attr('height', 10000)
				.attr('opacity', 0)
				.attr('class', 'background-rect')
				.on('dblclick', function () {
					zoom.translate([0, 0]).scale(1);
					graph.attr('transform', 'translate(0, 0) scale(1)');
				});
			
			$('#containerId')[0].appendChild($('#graph0')[0]);
			graph = d3.select('#graph0');
			
			var transform = $('#graph0')[0].transform.animVal;
			for (var i = 0; i < transform.numberOfItems; ++i)
				if (transform[i].type == 2)
					container.attr('transform', 'translate(' + transform[i].matrix.e + ' ' + transform[i].matrix.f + ') scale(1)');
					
			graph.attr('transform', 'translate(0 0) scale(1)');
			
			$('g').children('a')
				.removeAttr('xlink:href')
				.removeAttr('xlink:title')
				.on('click', function (a, b) {
					var parent = $(this).parent().parent();
					var id = parent.attr('id');
					if (id != 'placeholder') {
						neighborhoodTaskId = id;
						generateGraph($('#moveEventId').val());
					}
				})
			
				
			
			addBindings();
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
				$('#pageHeadingId').html('Task Graph - Neighborhood');
				$('#exitNeighborhoodId').css('display', 'inline-block');
			}
			
			height = 0;
			var params = {'id':neighborhoodTaskId};
			if (event != 0)
				params = {'moveEventId':event, 'id':neighborhoodTaskId};
			
			// show the loading spinner
			$('#spinnerId').css('display', 'block');
			
			
			if (neighborhoodTaskId == -1)
				jQuery.ajax({
					dataType: 'text',
					url: 'moveEventTaskGraph',
					data: params,
					type:'GET',
					complete: buildGraph
				});
			else
				jQuery.ajax({
					dataType: 'text',
					url: 'neighborhoodGraph',
					data: params,
					type:'GET',
					complete: buildGraph
				});
		}
		</script>
	</head>
	<body>
		<div class="body">
			<h1 id="pageHeadingId">Task Graph</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			Event: <g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Please select']}" value="${selectedEventId}" onchange="submitForm()" />
			&nbsp; 
			<input type="button" name="Exit Neighborhood Graph" id="exitNeighborhoodId" value="View Entire Graph" onclick="submitForm()" />
			<br>
			<span id="spinnerId" style="display: none"><img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
		</div>
	</body>
</html>
