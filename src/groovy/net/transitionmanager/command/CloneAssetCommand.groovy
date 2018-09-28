package net.transitionmanager.command

class CloneAssetCommand {

	/**
	 * Id of the asset being cloned.
	 */
	Long id

	/**
	 * Name of the asset to be cloned
	 */
	String name

	/**
	 * Flag to clone dependencies
	 */
	Boolean cloneDependencies

	static constraints = {
		name nullable: false, blank: false
		id nullable: false
		cloneDependencies nullable: false

	}
}
