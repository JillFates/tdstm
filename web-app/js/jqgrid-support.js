/* This is a TDS source file.  
 * This javascript file contains function used by all the list views
 */

// Set the defaults for any jqgrid
if ($.jgrid && $.jgrid.defaults) {
	defaults = $.jgrid.defaults
	defaults.rowNum = 25
	defaults.height = '100%'
	defaults.sortable = true
	defaults.rowList = ['25','100','500','1000']
	defaults.viewrecords = true
	defaults.datatype = 'json'
}

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
		$(b).css( 'width', $(b).width() );
	})
	header.css( 'width', header.css('width') );
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
	/**
	 * Polyfill
	 * Introduced by the last changes on CSS3 that in browser like Safari
	 * the table-fixed thread as is supposed and max-width is not allowed anymore at table level.
	 */
	$('.ui-jqgrid-htable tr:nth-child(1) th.ui-th-column:visible:last').addClass('thForceTableFixed');
	var horizontalOffset = $('#'+gridId+'Wrapper').offset().left + 1;
	var windowWidth = $(window).width();
		
	unfreezeHeader()
	handleHeaderPosition()
	$('#'+gridId+'Wrapper').width(windowWidth - horizontalOffset * 2) // horizontalOffset comptensates for the border/padding/etc and scroll bar
	$('#'+gridId+'Grid').fluidGrid({ base:'#'+gridId+'Wrapper', offset: 0 });
}

/**
 * This function is used to enable and disable compare/merge button in modelList,personList and BulkDelete button in assetLists 
 * when click on particular row.
 */
function validateMergeCount() {
	setTimeout(function() {
		var checkedLen = $('.cbox:checkbox:checked').length
		if ($("#cb_personIdGrid").is(':checked')) {
			checkedLen--;
		}
		if (checkedLen > 1 && checkedLen < 26) {
			$("#compareMergeId").removeAttr("disabled")
		} else {
			$("#compareMergeId").attr("disabled","disabled")
		}
		if (checkedLen > 0) {
			$("#deleteAssetId").removeAttr("disabled")
		} else {
			$("#deleteAssetId").attr("disabled","disabled")
		}
	}, 500);
}
/**
 * This function is used to enable and disable compare/merge button in modelList,personList and BulkDelete button in assetLists
 * when click on particular CheckBox .
 */
function initCheck() {
	$('.cbox').change(validateMergeCount)
}

// handles positioning of the header on non-jqgrid tables
function handleHeaderPositionGeneral (scrollLimit, header, top, left, eventType) {
	var scroll = $(document).scrollTop();
	
	if (scroll > scrollLimit) {
		freezeHeaderGeneral(header, top, left, eventType);
	} else {
		unfreezeHeaderGeneral(header);
	}
}

// Freezes the header at the top of the window for non-jqgrid tables
function freezeHeaderGeneral (header, top, left, eventType) {
	if (header.parent().children('.floatingHeader').size() == 0 || eventType === 'resize') {
		var clone = header.clone();
		clone.attr('class', 'floatingHeader');
		clone.css('left', Math.floor(left) + 'px');
		header.children().each(function (a, b) {
			if (clone.children().size() > a) {
				var newWidth = b.scrollWidth /*- ($(b).innerWidth() - $(b).width())*/;
				$(clone.children()[a]).width(newWidth);
				$(clone.children()[a]).on('click', function () {
					b.click();
				});
			}
		});
		// Even when the width is calculated per column, due the position:fixed we need to set the width to the table
		clone.css('width', header.width());
		header.parent().append(clone);
	}
}

/**
 * $.browser was deprecated in newer versions, so let's use the normal way
 * The grid is being draw using a tag lib outside our repo, this will be executed a few seconds before the grid is ready
 * @returns {boolean}
 */
function processTaskSafariColumns() {
	var isSafari = (navigator.userAgent.indexOf('Safari') != -1 && navigator.userAgent.indexOf('Chrome') == -1);
	if(isSafari) {
		jQuery("#taskListIdGrid").jqGrid('hideCol',["suc","score"]);
	}
}

// Unfreezes the header and returns it to its regular position for non-jqgrid tables
function unfreezeHeaderGeneral (header) {
	$('.floatingHeader').remove();
}

/**
 * @author: Octavio Luna
 * @date: 2016-07-28
 * Create a package Domain for TDS (as a global scope)
 *
 * Custom filter toolbar to select the default bounce timeout
 * Created to replace the Grails Tag:
 *    <jqgrid:filterToolbar /> //remove this if exist in your code
 *
 * @param id id of the grails jqgrid (beware that the id is the one used in the *grails tag* that is different from the actual id set in the HTML by grails plugin)
 * @param debounce buffer time to wait for keystrokes, defaults to 700ms
 * Usage: add after declaring the jqGrid
 *   TDS.jqGridFilterToolbar(id) //defaults to 700
 *   TDS.jqGridFilterToolbar(id, 800)
 */
window.TDS = window.TDS || {};

window.TDS.jqGridFilterToolbar = function(id, debounce) {
		var VK_ENTER = 13;
		debounce = typeof debounce !== 'undefined' ? debounce : 700;
		var gridSelector = '#' + id + 'Grid';
		//Avoid internal filtering
		$(gridSelector).filterToolbar({
			autosearch: false,
			searchOnEnter: false
		});

		var triggerToolBar = $(gridSelector)[0].triggerToolbar;
		var timeoutHnd;
		$('#' + id + 'Wrapper .ui-search-toolbar .ui-th-column input').on("input", function(e) {
			if(timeoutHnd) { clearTimeout(timeoutHnd); }
			var key = e.which;
			if(key == VK_ENTER){ //ENTER
				triggerToolBar();
			}else{
				timeoutHnd = setTimeout(function(){triggerToolBar();}, debounce);
			}
		});
}