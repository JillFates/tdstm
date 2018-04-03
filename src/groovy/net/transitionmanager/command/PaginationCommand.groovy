package net.transitionmanager.command

import com.tdsops.common.ui.Pagination
import grails.validation.Validateable

/**
 * A generic usage pagination command object
 */
@Validateable
class PaginationCommand implements CommandObject {

	int offset = 0
	int rows = Pagination.MAX_DEFAULT

	static constraints = {
		offset min: 0
		rows min: 0
	}

	PaginationCommand(Map params) {
		if (params.offset) {
			this.offset = params.offset as Integer
		}
		if (params.rows) {
			this.rows = params.rows as Integer
		}

		// make sure the max rows param is within boundaries
		Integer rows = Pagination.maxRowForParam(this.rows as String)
		this.rows = rows
	}

}
