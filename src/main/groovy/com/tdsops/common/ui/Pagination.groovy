package com.tdsops.common.ui

import com.tdssrc.grails.NumberUtil

/**
 * Used in conjunction with pagination in the application
 */
class Pagination {
    public static final List<Integer> MAX_OPTIONS = [25, 50, 100, 250, 500, 1000]
	public static final Integer MAX_DEFAULT = 25

    /**
     * Used to validate and return a valid max row value based on user submitted parameter. If the value is not in the 
     * list of valid options then the MAX_DEFAULT value will be used.
     * @param maxParamValue - a string value that is submitted in the request
     * @return - the value as Integer if valid otherwise the default
     */
    static Integer maxRowForParam(CharSequence maxParamValue) {
		Integer maxRow = NumberUtil.toInteger(maxParamValue, -1)
		if (! MAX_OPTIONS.contains(maxRow)) {
			maxRow = MAX_DEFAULT
        }
        maxRow
    }

    /**
     * Used to determine the current page number to list
     * @param value - the value passed in the params for the page property
     * @return the requested page or 1 as the default if non or invalid
     */
    static Integer pageForParam(CharSequence value) {
        NumberUtil.toPositiveInteger(value, 1)
    }

    /**
     * Used to determine the offset to use when querying the database based on the current page and the rows per page
     * @param currentPage - the page being requested
     * @param rowsPerPage - the number of rows that appear in a page
     * @param the offset into the dataset
     */
    static Integer rowOffset(Integer currentPage, Integer rowsPerPage) {
        Integer offset = 0
        if (currentPage > 1) {
            offset = (currentPage - 1) * rowsPerPage
        }
        offset
    }

    /**
     * Returns the MAX_OPTIONS list as a comma separate list of the values as a string
     */
    static String optionsAsText(){
        MAX_OPTIONS.join(',')
    }

}
