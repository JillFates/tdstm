package net.transitionmanager.command.reports

import net.transitionmanager.command.CommandObject

class DatabaseConflictsCommand implements CommandObject{

	/**
	 * Bundle ID or 'useForPlanning'.
	 */
	String moveBundle

	/**
	 * Include Databases with dependencies to assets assigned to unrelated bundles.
	 */
	Boolean bundleConflicts

	/**
	 * Include Databases having dependencies with status UNKNOWN or QUESTIONED.
	 */
	Boolean unresolvedDependencies

	/**
	 * Include Databases that don't have a dependency where the asset is an application.
	 */
	Boolean missingApplications

	/**
	 * Include Databases having no Requires dependency indication where the database resides.
	 */
	Boolean unsupportedDependencies

	/**
	 * Max number of assets to be included.
	 */
	Integer maxAssets = 100

}
