/* 	These shapes must be hard coded here due to limitations of the d3 library.
	There is no way for d3 to parse through svg data unless it is explicitly included in the DOM.	*/

// loads the shapes into the svg defs
function defineShapes (defs) {
	
	// define the arrowhead markers used for marking dependencies
	var markers = {'arrowhead':'#000000', 'arrowheadBlackBackground':'#ffffff', 'arrowheadNA':'#909090', 'arrowheadNABlackBackground':'#eeeeee', 'arrowheadUnresolved':'red', 'arrowheadSelected':'#00dd00', 'arrowheadCyclical':'blue', 'arrowheadRedundant':'orange', 'arrowheadCut':'orange', 'arrowheadBundleConflict':'red'};
	var keys = Object.keys(markers);
	for (var i = 0; i < keys.length; ++i) {
		defs.append("marker")
			.attr("id", keys[i])
			.attr("viewBox", "0 -5 10 10")
			.attr("refX", 0)
			.attr("refY", 0)
			.attr("markerUnits", "userSpaceOnUse")
			.attr("markerWidth", 10)
			.attr("markerHeight", 10)
			.attr("orient", "auto")
			.append("path")
			.attr("d", "M0,-4L10,0L0,4")
			.attr('fill', markers[keys[i]]);
	}
	
	defs.selectAll('#arrowheadSelected,#arrowheadCyclical,#arrowheadBundleConflict')
		.attr("viewBox", "0 -6 10 12")
		.attr("markerHeight", 12);
	defs.selectAll('#arrowheadSelected path,#arrowheadCyclical path,#arrowheadBundleConflict path').attr("d", "M0,-6L10,0L0,6");

	return null;
}