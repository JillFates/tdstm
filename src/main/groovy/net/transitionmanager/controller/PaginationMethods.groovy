package net.transitionmanager.controller

import com.tdsops.common.ui.Pagination
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.person.UserPreferenceService
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.NumberUtil
import com.tdsops.common.grails.ApplicationContextHolder

trait PaginationMethods {

	static final def PAGINATION_SORT_ORDER_VALUES = ['ASC', 'DESC', 'A', 'D'].asImmutable()
	static final InvalidParamException PAGINATION_INVALID_SORT_ORDER_EXCEPTION = new InvalidParamException('Invalid sort order specified')
	static final InvalidParamException PAGINATION_INVALID_ORDER_BY_EXCEPTION = new InvalidParamException('Invalid field name specified to order by')
	static final RuntimeException PAGINATION_INVALID_DEFAULT_ORDER_BY_EXCEPTION = new RuntimeException('Invalid or missing required defaultProperty parameter')

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

	/**
	 * Used to validate a field name requested for the ORDER BY is a valid domain property. When an
	 * invalid field is specified an exception is thrown.
	 * @domainClass - the class of the domain to check against
	 * @paramName paramName - the domain fieldName specified for the order
	 * @paramName defaultProperty - the default value if param name not provided
	 * @return the propertyName to order by if specified in params or the default property otherwise
	 * @throws InvalidParamException - when paramName specified with an invalid property for the domain
	 */
	String paginationOrderBy(def domainClass, String paramName, String defaultProperty) {
		if (params.containsKey(paramName) && params[paramName]) {
			if (! GormUtil.isDomainProperty(domainClass, params[paramName]) ) {
				throw PAGINATION_INVALID_ORDER_BY_EXCEPTION
			}
			return params[paramName]
		} else {
			// Check that the code is referencing a valid property
			if (! defaultProperty || ! GormUtil.isDomainProperty(domainClass, defaultProperty) ) {
				throw PAGINATION_INVALID_DEFAULT_ORDER_BY_EXCEPTION
			}
			return defaultProperty
		}
	}

	/**
	 * Used to validate a field name requested for the ORDER BY is a known alias. When an
	 * invalid field is specified an exception is thrown.
	 * @aliases - a list of field aliases.
	 * @paramName paramName - the domain fieldName specified for the order
	 * @paramName defaultProperty - the default value if param name not provided
	 * @return the propertyName to order by if specified in params or the default property otherwise
	 * @throws InvalidParamException - when paramName specified with an invalid property for the domain
	 */
	String paginationOrderByAlias(Set<String> aliases, String paramName, String defaultProperty) {
		if (params[paramName] && aliases) {
			if (aliases.contains(params[paramName])) {
				return params[paramName]
			}
			throw PAGINATION_INVALID_ORDER_BY_EXCEPTION
		} else {
			// Check that the code is referencing a valid property
			if (! aliases.contains(defaultProperty) ) {
				throw PAGINATION_INVALID_DEFAULT_ORDER_BY_EXCEPTION
			}
			return defaultProperty
		}
	}

	/**
	 * Used to retrieve the sort order for the data query
	 * @param orderParamName - the parameter name that is used to indicate the user's selection (default sord)
	 * @param orderDefault - the order to sort by (default ASC)
	 * @return ASC or DESC based on the parameter passed to the request with default behavior
	 */
	String paginationSortOrder(String orderParamName = 'sorder', String orderDefault='ASC') {
		if (orderParamName) {
			String value = params[orderParamName] ?: orderDefault
			return _validatePaginationSortOrderParam(value)
		} else {
			return _validatePaginationSortOrderParam(orderDefault)
		}
	}

	/**
	 * Used internally to validate that the sort order param value is one of the correct values. If set
	 * or the errorOnBlank is true then if blank or bad value the InvalidParamException parameter is thrown.
	 * @param orderValue - the value is evaluated for ASC|DESC|A|D
	 * @param errorOnBlank - a flag to throw the exception when blank
	 * @return the sort order normalized to ASC|DESC
	 * @throws InvalidParamException
	 */
	private String _validatePaginationSortOrderParam(String orderValue, Boolean errorOnBlank=false) {
		String result = ''
		if (orderValue) {
			String value = orderValue.toUpperCase()
			if (PAGINATION_SORT_ORDER_VALUES.contains(value)) {
				result = value.startsWith('A') ? 'ASC' : 'DESC'
			}
		}
		if ( result == '' && (errorOnBlank || orderValue != '' ) ) {
			throw PAGINATION_INVALID_SORT_ORDER_EXCEPTION
		}

		return result
	}

}
