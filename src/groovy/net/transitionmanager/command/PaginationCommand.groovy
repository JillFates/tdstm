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

}
