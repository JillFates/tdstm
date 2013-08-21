/* This is a TDS source file.  
 * This javascript file contains fuctions used by all the list views
 */

// Set the defaults for any jqgrid
defaults = $.jgrid.defaults
defaults.rowNum = 25
defaults.height = '100%'
defaults.sortable = true
defaults.rowList = ['25','100','500','1000']
defaults.viewrecords = true
defaults.datatype = 'json'

// freezes or unfreezes the header row based its location relative to the browser window
function handleHeaderPosition () {
	var headTable = $('.ui-jqgrid-htable')
	var scroll = $(document).scrollTop()
	
	if (headTable.offset()) {
		var headerTop = headTable.offset().top
			
		if (scroll > headerTop)
			freezeHeader();
		else
			unfreezeHeader();
	}
}

// Freezes the header at the top of the window
function freezeHeader () {
	var header = $('.ui-jqgrid-htable thead')
	$('.jqgfirstrow').height(header.height());
	// The childrens' widths must be set explicitly for IOS compatibility
	header.children('.ui-jqgrid-labels').children().each(function(a,b){
		$(b).css( 'max-width', $(b).width() );
	})
	header.css( 'max-width', header.css('width') );
	header.css('position','fixed');
	header.css('top','0px');
}

// Unfreezes the header and returns it to its regular position
function unfreezeHeader () {
	var header = $('.ui-jqgrid-htable thead')
	$('.jqgfirstrow').height(0);
	header.css('max-width', '');
	header.css('position','relative');
}

/* Binds window resizing to the resizeGrid function and performs the initial resizing
 * @param String gridId The id of the grid
 */
function bindResize (gridId) {
	resizeGrid(gridId)
	$(window).resize( function() {
		resizeGrid(gridId)
	});
	$(window).scroll( function() {
		handleHeaderPosition()
	});
}


/* Called when the window is resized to resize the grid wrapper 
 * @param String gridId The id of the grid
 */
function resizeGrid (gridId) {
	// Calculate the offset based on if there is a scrollbar or not
	var horizontalOffset = 16;
	if ($(document).height() > $(window).height())
		horizontalOffset = 2;
		
	unfreezeHeader()
	handleHeaderPosition()
	
	$('#'+gridId+'Wrapper').width($('.fluid').width()-horizontalOffset) // horizontalOffset comptensates for the border/padding/etc and scroll bar
	$('#'+gridId+'Grid').fluidGrid({ base:'#'+gridId+'Wrapper', offset: 0 });
}

/**
 * This function is used to enable and disable compare/merge button in modelList,personList and BulkDelete button in assetLists 
 * when click on particular row.
 */
function validateMergeCount() {
	var checkedLen = $('.cbox:checkbox:checked').length
	if(checkedLen > 1 && checkedLen < 5) {
		$("#compareMergeId").removeAttr("disabled")
	} else {
		$("#compareMergeId").attr("disabled","disabled")
	}
	if(checkedLen > 0) {
		$("#deleteAssetId").removeAttr("disabled")
	} else {
		$("#deleteAssetId").attr("disabled","disabled")
	}
}
/**
 * This function is used to enable and disable compare/merge button in modelList,personList and BulkDelete button in assetLists
 * when click on particular CheckBox .
 */
function initCheck() {
	$('.cbox').change(validateMergeCount)
}









