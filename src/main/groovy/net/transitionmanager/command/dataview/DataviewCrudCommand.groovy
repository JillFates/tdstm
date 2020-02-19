package net.transitionmanager.command.dataview

import net.transitionmanager.command.CommandObject
import com.tdsops.tm.enums.domain.ViewSaveAsOptionEnum
import net.transitionmanager.imports.Dataview

/**
 * A  sub command object used with the Dataview create and update functionality
 */

class DataviewCrudCommand implements CommandObject {
	/*
	 * the dataview that is being updated
	 */
	Dataview id
	/*
	 * a map of the dataview definition
	 */
	DataviewSchemaCommand schema = new DataviewSchemaCommand()
	/*
	 * flag that the view is to be shared
	 */
	Boolean isShared
	/*
	 * flag that when true indicates that the the user wants the view to be a favorite
	 */
	Boolean isFavorite
	/*
	 * the system view that the user is overriding or has overridden (optional)
	 */
	Dataview overridesView
	/*
	 * the name of the view
	 */
	String name
	/*
	 * the indicator of how the view should be saved
	 */
	ViewSaveAsOptionEnum saveAsOption
	/*
	 * The querystring passed to the dataview controller becomes a Hash object in Angular, that is used to supplement the
	 * filtering of the dataview. The parameters in the querystring get injected into the dataview columm.filter
	 * property however when saving the views, filter on those columns should be cleared out when saving the view.
	 * Therefore, the querystring is used in the save/update process to clear out the appropriate column filters.
	 */
	Map querystring = [:]

	static constraints = {
		id nullable: true
		overridesView nullable: true
		saveAsOption nullable: true
		querystring nullable: true
	}
}
