package net.transitionmanager.command

import net.transitionmanager.project.Workflow

class ApplicationMigrationCommand {

	/**
	 * Move Bundle ID or 'useForPlanning'
	 */
	String moveBundle

	/**
	 * Person id or 'null'.
	 */
	String sme

	/**
	 * Asset Comment Category.
	 */
	String startCategory

	/**
	 * Asset Comment Category.
	 */
	String stopCategory

	/** Workflow Transition ID */
	Long testing


	String outageWindow
}
