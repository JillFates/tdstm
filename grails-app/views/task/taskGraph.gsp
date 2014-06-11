<!--
The MIT License (MIT)

Copyright (c) 2013 bill@bunkat.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-->
<html>
	<head>
		<title>Task Graph</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<g:javascript src="jquery-1.9.1.js"/>	
		<g:javascript src="d3/d3.min.js"/>
		<script type="text/javascript">
		
		var container = null;
		var graph = null;
		
		// reconstruct the svg for d3
		$(document).ready(function () {
			var svg = d3.select('svg');
			container = svg.append('g')
				.attr('id', 'containerId')
				.attr('width', svg.attr('width'))
				.attr('height', svg.attr('height'));
			
			$('#containerId')[0].appendChild($('#graph0')[0]);
			graph = d3.select('#graph0');
			
			var transform = $('#graph0')[0].transform.animVal;
			for (var i = 0; i < transform.numberOfItems; ++i)
				if (transform[i].type == 2)
					container.attr('transform', 'translate(' + transform[i].matrix.e + ' ' + transform[i].matrix.f + ') scale(1)');
					
			graph.attr('transform', 'translate(0 0) scale(1)');
			addBindings();
		});		
		
		// binds d3 zooming behavior to the svg
		function addBindings () {
			var zoom = d3.behavior.zoom()
				.on("zoom", zooming);
			
			container.call(zoom);

			function zooming () {
				graph.attr('transform', 'translate(' + d3.event.translate + ') scale(' + d3.event.scale + ')');
			}
		}
		
		</script>
	</head>
	<body>
		<div class="body">
			${svg}
		</div>
	</body>
</html>