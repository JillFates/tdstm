package net.transitionmanager.command

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


	String outageWindow
}
