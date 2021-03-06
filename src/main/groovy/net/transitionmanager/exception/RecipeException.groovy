package net.transitionmanager.exception

import groovy.transform.CompileStatic

/**
 * Flags an error in a recipe's syntax.
 */
@CompileStatic
class RecipeException extends Exception {
	RecipeException(String message) {
		super(message)
	}
}
