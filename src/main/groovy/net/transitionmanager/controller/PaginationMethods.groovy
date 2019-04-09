package net.transitionmanager.controller

import com.tdsops.common.ui.Pagination
import net.transitionmanager.person.UserPreferenceService
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.NumberUtil
import com.tdsops.common.grails.ApplicationContextHolder

trait PaginationMethods {

	/**
	 * Returns the number of rows to be displayed in a datagrid based on param or user preference if indicated. This will
	 * attempt to use the param passed in. If the value is not in the accepted list then it will use the user preference
	 * if it exists and is valid or the default value (25). If the user preference is supplied then it will set it if the 
	 * parameter is different than the current or add if doesn't exist based on the updatePreference parameter.
	 *
	 * @param paramName - the name of the parameter that represents the rows per page (default to 25)
	 * @param userPreferenceCode - the User Preference Code name used to save the user preference, if null then not used
	 * @param updatePreference - flag if the User Preference should be updated which if true and a preference code is indicated 
	 * @return an acceptable max rows per page value
	 */
	Long paginationMaxRowValue(CharSequence paramName='rows', UserPreferenceEnum userPreferenceCode=null, boolean updatePreference=true) {
		UserPreferenceService userPreferenceService

		Integer maxRow = Pagination.maxRowForParam(params[paramName])
		
		// Get the bean and the user's saved preference as we'll need it soon
		if (userPreferenceCode) {
			userPreferenceService = ApplicationContextHolder.getBean('userPreferenceService')
			String userPrefValue = NumberUtil.toInteger(userPreferenceService.getPreference(userPreferenceCode), -1)
			if (maxRow.toString() != userPrefValue) {
				userPreferenceService.setPreference(userPreferenceCode, maxRow.toString())
			}
		}

		return maxRow
	} 

    /**
     * Used to determine the page number based on the value passed in via the params. This will default to 1 if the param
     * is missing or not a positive value.
     * @param paramName - the name of the parameter that represents the page property (default 'page')
     * @return the requested page number or default 1
     */
    Integer paginationPage(CharSequence paramName='page') {
        Pagination.pageForParam(params[paramName])
    }

    /**
     * Used to determine the offset to use when querying the database based on the current page and the rows per page
     * @param currentPage - the page being requested
     * @param rowsPerPage - the number of rows that appear in a page
     * @param the offset into the dataset
     */
    Integer paginationRowOffset(Integer currentPage, Integer rowsPerPage) {
		Pagination.rowOffset(currentPage, rowsPerPage) 
    }

}
