package net.transitionmanager.command.reports

import net.transitionmanager.command.CommandObject

class ApplicationConflictsCommand implements CommandObject{

	/**
	 * Id of the Person selected in the UI.
	 */
	Long appOwner

	/**
	 * Include assets assigned to unrelated bundles.
	 */
	boolean bundleConflicts

	/**
	 * Limit of applications to be included in the report.
	 */
	Integer maxAssets = 100

	/**
	 * Dependencies with no "supports" or "requires".
	 */
	boolean missingDependencies

	/**
	 * Id of the selected bundle or 'useForPlanning'.
	 */
	String moveBundle

	/**
	 * Include dependencies with status 'Unknown' or 'Questioned'.
	 */
	boolean unresolvedDependencies

}
