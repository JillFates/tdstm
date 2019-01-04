package net.transitionmanager.integration

import com.tdssrc.grails.ThreadLocalVariable

/**
 * Supported and encapsulated ApiAction ThreadLocal variables
 * TM-10011
 */
enum ActionThreadLocalVariable implements ThreadLocalVariable {
	ACTION_REQUEST,
	TASK_FACADE,
	ASSET_FACADE,
	REACTION_SCRIPTS
}
