/**
 * @author David Ontiveros
 */
package net.transitionmanager.imports

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.Color
import com.tdsops.tm.enums.domain.SizeScale
import com.tdsops.tm.enums.domain.ViewSaveAsOptionEnum
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.Transactional
import net.minidev.json.JSONObject
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.command.dataview.DataviewApiFilterParam
import net.transitionmanager.command.dataview.DataviewApiParamsCommand
import net.transitionmanager.command.dataview.DataviewCrudCommand
import net.transitionmanager.command.dataview.DataviewNameValidationCommand
import net.transitionmanager.command.dataview.DataviewSchemaColumnCommand
import net.transitionmanager.command.dataview.DataviewUserParamsCommand
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.exception.ConfigurationException
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.InvalidConfigurationException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.UnauthorizedException
import net.transitionmanager.person.FavoriteDataview
import net.transitionmanager.person.Person
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.search.FieldSearchData
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.service.dataview.DataviewSpec
import net.transitionmanager.service.dataview.filter.FieldNameExtraFilter
import net.transitionmanager.service.dataview.filter.special.SpecialExtraFilter
import net.transitionmanager.task.AssetComment
import net.transitionmanager.util.JsonViewRenderService

import java.security.InvalidParameterException
/**
 * Service class with main database operations for Dataview.
 * @see Dataview
 */
class DataviewService implements ServiceMethods {

	ProjectService projectService
	JsonViewRenderService jsonViewRenderService

	// TODO : JPM 1/2020 : @Diego - why was this lazy loaded instead of just using the IOC? This should be documented here...
	@Lazy
	private static CustomDomainService customDomainService = { -> ApplicationContextHolder.getBean('customDomainService', CustomDomainService) }()

	static final InvalidParamException MISSING_ID_EXCEPTION     = new InvalidParamException('Missing required id')
	static final EmptyResultException  VIEW_NOT_FOUND_EXCEPTION = new EmptyResultException('View was not found')

	// Limit the number of favorites that should be returned in queries.
	static final int FAVORITES_MAX_SIZE = 10

	/**
	 *
	 * Query for getting all projects where: belong to current project and either shared, system or are owned by
	 * current whom in session
	 *
	 * @param whom
	 * @param project
	 * @return
	 */
    List<Dataview> list(Person userPerson, Project userProject) {
        def query
		List<Long> favoriteSystemViewIds

        boolean canSeeSystemViews = securityService.hasPermission(userPerson, Permission.AssetExplorerSystemList)
        if (canSeeSystemViews) {
            query = Dataview.where {
                (project == userProject && (isShared == true || person == userPerson)) \
				|| \
				(project.id == Project.DEFAULT_PROJECT_ID && isSystem == true)
            }
        } else {
			// Get list of user's list favorite View Ids
            favoriteSystemViewIds = FavoriteDataview.where { person == userPerson }
        		.projections { property('dataview.id') }
				.list()

            if (favoriteSystemViewIds) {
                query = Dataview.where {
                    (project == userProject  && (isShared == true || person == userPerson)) \
					|| \
                    (project.id == Project.DEFAULT_PROJECT_ID && isSystem == true && id in favoriteSystemViewIds)
                }
            } else {
                query = Dataview.where {
                    (project == userProject  && (isShared == true || person == userPerson))
                }
            }
        }

        return query.list()
    }

	/**
	 * Returns a Dataview, validating that the user has proper access
	 * if it's a system view it will look for ny overriden view that needs to be shown instead of the default using
	 * the following logic:
	 * 		1st. from the current project and created by the current user and not shared(myViews)
	 * 		2nd. from the same project and shared from other user
	 * 		3rd. from the DEFAULT Project and is shared
	 * @param id
	 * @param override true by default, we serve those views overriding the default one, if set to false,
	 * serve the actual requested view
	 * @return
	 */
	@Deprecated
	Dataview fetch(Long id, boolean override = true) throws InvalidParamException, EmptyResultException {
		if (!id || id < 1) {
			throw MISSING_ID_EXCEPTION
		}

		return fetch(securityService.userCurrentProject,securityService.loadCurrentPerson(), id, override)
	}

	/**
	 * Returns a Dataview after validating that the user has proper access to it.
	 * When it is a system view, then based on the fetchOverrideVersion parameter it will substitute the appropriate
	 * overridden version if exists instead of the actual system view.
	 * The override logic is as follows:
	 * 		1st. from the current project and created by self and not shared (i.e, myViews)
	 * 		2nd. from the same project and isShared by self or another user
	 * 		3rd. from the DEFAULT Project and is shared
	 * @param id
	 * @param fetchOverrideVersion - flag when true and the view is a system view then it should return the overridden
	 * version of the view.
	 * @return the appropriate version of the specified view
	 */
	Dataview fetch(Project currentProject, Person whom, Long id, boolean fetchOverrideVersion) throws InvalidParamException, EmptyResultException {

		Dataview dataview = Dataview.read(id)
		if ( ! dataview ) {
			throw VIEW_NOT_FOUND_EXCEPTION
		}

		// If user is in the DEFAULT project things are a bit different because there is no MyViews
		if (Project.DEFAULT_PROJECT_ID == currentProject.id) {
			// If it is a system view and the we should be overridding if one is available we will check it out
			if (dataview.isSystem && fetchOverrideVersion) {
				List<Dataview> overrideDataviewList = Dataview.where {
					project.id == Project.DEFAULT_PROJECT_ID
					overridesView.id == id
				}.list()

				if ( overrideDataviewList.size() > 1) {
					throwException(ConfigurationException, 'dataview.validate.multipleDefintionsDefined',
							[overrideDataviewList*.id],
							"Multiple dataview definitions were encountered with ids (${overrideDataviewList*.id})" )
				} else if (overrideDataviewList.size() == 1) {
					dataview = overrideDataviewList[0]
				}
			}
			return dataview
		}

		if ( fetchOverrideVersion && dataview.isSystem ) {
			/*
			 * Find the appropriate overriden system view if it exists. The search will be based on:
			 * 		1) from the current project and created by the current user as a My View
			 * 		2) from the same project and shared from other user
			 * 		3) from the DEFAULT Project and is shared
			 */
			List<Dataview> overridesDataviews = Dataview.whereAny {
				overridesView.id == id && project.id == currentProject.id && person.id == whom.id && isShared == false
				overridesView.id == id && project.id == currentProject.id && isShared == true
				overridesView.id == id && project.id == Project.DEFAULT_PROJECT_ID && isShared == true
			}.list()

			if ( overridesDataviews ) {
				// Need to sort the list into the order of precedence as listed above
				overridesDataviews.sort {
					if ( it.project.id == currentProject.id ) {
						if ( it.person.id == whom.id && ! it.isShared ) {
							// This is an overridden view saved by the user for themself
							return 1
						} else {
							// This is an overridden system view shared within the project
							return 2
						}
					} else {
						// This would represent an overridden view in the Default Project for the system view
						return 3
					}
				}
				dataview = overridesDataviews.first()
			}
		}
		// TODO : JM 1/2020 - should we pass id here?
		validateDataviewViewAccessOrException(currentProject, whom, dataview);
		return dataview
	}

	/**
	 * Generates a map that contains the save and saveAsOptions list that will be used by the frontend to present the
	 * various saving options.
	 *
	 * The 'save' boolean setting is used to determine if the current view can be saved.
	 * The 'saveAsOptions' is list containing the various options (MY_VIEW, OVERRIDE_FOR_ME, OVERRIDE_FOR_ALL)
	 * appropriately.
	 *
	 * The ability to save is based on the following rules:
	 * 		- Owner of view
	 * 			- The view was created by them
	 * 			- the view is in the same project as the user context
	 * 		- Non-Owner in Non-Default Project
	 * 			- the view is in their project
	 * 			- it is an override of a system view
	 * 			- it is shared
	 * 			- the user has permission OVERRIDE_FOR_ALL
	 * 		- Non-Owner in Default Project
	 * 		 	- the view is in the default project
	 * 			- the user context is set to the default project
	 * 			- the view is shared
	 * 			- the view is an override of a system view
	 * @param project - the user's current Project context
	 * @param whom - the Person that is updating the view
	 * @param dataview - The dataview being saved
	 * @return - A list of the save as methods that are available for the dataview.
	 */
	Map generateSaveOptions(Project project, Person whom, Dataview dataview) {
		Set saveAsOptions = []

		boolean hasGlobalOverridePerm = hasPermission(Permission.AssetExplorerOverrideAllUserGlobal)
		boolean hasProjectOverridePerm = hasPermission(Permission.AssetExplorerOverrideAllUserProject)
		boolean isDefaultProject = project.isDefaultProject()
		boolean isOverrideable = (dataview?.isSystem || dataview?.overridesView)

		// Determine the saveAsOptions
		// User can always save as My View
		if (hasPermission(Permission.AssetExplorerSaveAs)){
			saveAsOptions << ViewSaveAsOptionEnum.MY_VIEW.name()
		}

		if ( project && ! isDefaultProject ) {
			// Check to see if user already has an overridden version of a system view for themselves and if not
			// then they get the OVERRIDE_FOR_ME option
			if ( dataview?.overridesView || dataview?.isSystem) {
				if (Dataview.where {
					project.id == project.id
					// Make sure we're querying on the root system view id
					overridesView.id == (dataview.overridesView ? dataview.overridesView.id : dataview.id)
					person.id == whom.id
					isShared == false
				}.count() == 0) {
					saveAsOptions << ViewSaveAsOptionEnum.OVERRIDE_FOR_ME.name()
				}
			}

			// Check to see if anybody has an overridden version of a system view for ALL users and if not
			// then they get the OVERRIDE_FOR_ALL option, as long as they also have that permission.
			if (	dataview &&
					isOverrideable &&
					hasProjectOverridePerm &&
					Dataview.where {
						project.id == project.id
						// Make sure we're querying on the root system view id
						overridesView.id == (dataview.overridesView ? dataview.overridesView.id : dataview.id)
						isShared == true
					}.count() == 0 ) {
						saveAsOptions << ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL.name()
			}
		}

		// See about if the user can Save As OVERRIDE_FOR_ALL globally across all projects
		if (dataview && isOverrideable && isDefaultProject && hasGlobalOverridePerm) {
			if (Dataview.where {
				project.id == project.id
				// Make sure we're querying on the root system view id
				overridesView.id == (dataview.overridesView ? dataview.overridesView.id : dataview.id)
				isShared == true
			}.count() == 0) {
				saveAsOptions << ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL.name()
			}
		}

		// Determine if the user can save the current view
		Boolean canSaveCurrentView = false
		if (dataview) {
			if (dataview.personId == whom.id) {
				canSaveCurrentView = (dataview.projectId == project.id)
			} else {
				boolean hasPerm = (isDefaultProject ? hasGlobalOverridePerm : hasProjectOverridePerm)
				canSaveCurrentView = (hasPerm &&
						dataview.isShared &&
						dataview.overridesView &&
						dataview.projectId == project.id)
			}
		}

		// Determine if the person has the ability to share a view
		boolean canShare = hasPermission(
				(isDefaultProject ? Permission.AssetExplorerSystemCreate :  Permission.AssetExplorerPublish) )

		// Determine if the Override options in the Save Modal should appear regardless of options available
		boolean canOverride = false
		if ( isOverrideable ) {
			if (isDefaultProject) {
				// If in the Default
				canOverride = hasPermission(Permission.AssetExplorerOverrideAllUserGlobal)
			} else {
				canOverride = hasAnyPermission(
						[Permission.AssetExplorerCreate, Permission.AssetExplorerOverrideAllUserProject])
			}
		}

		return [
			canOverride: canOverride,
			canShare: canShare,
			save: canSaveCurrentView,
			saveAsOptions: saveAsOptions as List
		]
	}

	/**
	 * Updates a database dataview object.
	 * At this point just schema and isShared properties are accessible to be updated.
	 * @param project - the user's current Project context
	 * @param whom - the Person that is updating the view
	 * @param id - the ID of the dataview
	 * @param dataviewJson JSONObject to take changes from.
	 * @return the Dataview object that was updated
	 * @throws net.transitionmanager.exception.DomainUpdateException, UnauthorizedException
	 */
	@Transactional
	Dataview update(Project project, Person whom, Long id, DataviewCrudCommand dataviewCommand) {
		Dataview dataview = dataviewCommand.id

		validateDataviewUpdateAccessOrException(project, whom, dataviewCommand, dataview)
		removeQueryStringFromFilters(dataviewCommand)
		validateOverrideViewFiltersMatch(dataviewCommand)
		validateViewNameUniqueness(project, whom, dataviewCommand)

		String schema = jsonViewRenderService.render('/dataview/reportSchema', dataviewCommand.schema)

		// This is an issue when a person is viewing a shared override or their own override and saving the opposite
		dataview.with {
			reportSchema = schema
			isShared = dataviewCommand.isShared
			// User's are not allowed to rename overridden system views
			if (! dataviewCommand.overridesView) {
				name = dataviewCommand.name
			}
		}
		dataview.save()

		// Check if the view is favorite and must be unfavorited.
		if (dataview.isFavorite(whom.id) && !dataviewCommand.isFavorite) {
			deleteFavoriteDataview(whom, project, dataview.id)
		} else {
			// Check if the view must be favorited
			if (!dataview.isFavorite(whom.id) && dataviewCommand.isFavorite) {
				addFavoriteDataview(whom, project, dataview.id)
			}
		}

		return dataview
	}

	/**
	 *
	 * Create a Dataview object and add it to the whom's favorite if needed.
	 * Dataview whom and project should be passed as a parameter
	 *
	 * @param currentProject - the current project for the user context
	 * @param whom - the whom that is attempting to access the view
	 * @param dataviewCommand - command object for Dataview CRUD operations
	 * @return the Dataview object that was created
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	@Transactional
	Dataview create(Project currentProject, Person whom, DataviewCrudCommand dataviewCommand) {
		validateDataviewCreateAccessOrException(dataviewCommand, currentProject, whom)
		removeQueryStringFromFilters(dataviewCommand)
		validateOverrideViewFiltersMatch(dataviewCommand)

		// If the user is overriding a System View, they may be overriding their own override or
		// a shared override so it's important to get the root system view being overridden
		if (dataviewCommand.overridesView) {
			dataviewCommand.overridesView = getRootSystemView(dataviewCommand.overridesView)
			// use the name from the view that is being overridden
			dataviewCommand.name = dataviewCommand.overridesView.name
		}
		validateViewNameUniqueness(currentProject, whom, dataviewCommand)

		String schema = jsonViewRenderService.render('/dataview/reportSchema', dataviewCommand.schema)

		Dataview dataview = new Dataview()
		dataview.with {
			if (dataviewCommand.saveAsOption == ViewSaveAsOptionEnum.MY_VIEW) {
				isShared = dataviewCommand.isShared
			} else {
				isShared = dataviewCommand.saveAsOption == ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL
			}
			name = dataviewCommand.name
			person = whom
            project = currentProject
			reportSchema = schema
			overridesView = dataviewCommand.overridesView
		}

		dataview.save()

		if (dataviewCommand.isFavorite) {
			addFavoriteDataview(whom, currentProject, dataview.id)
		}

		return dataview
	}

	/**
	 * Deletes a Dataview object
	 * Dataview whom and project are taken from current session
	 * @param project - the current project for the user context
	 * @param whom - the whom that is attempting to access the view
	 * @param id Dataview id to delete
	 * @throws DomainUpdateException, UnauthorizedException
	 */
	@Transactional
	void delete(Project project, Person whom, Long id) {
		Dataview dataview = fetch(id)
		validateDataviewDeleteAccessOrException(project, whom, id, dataview)
		dataview.delete()
	}

	/**
	 * Used to retrieve the root system view when working with an overridden view or the creation of one. This will
	 * use recursion to find it. It will only try up to 4 times since there should never be more than that level
	 * of hierarchy.
	 * @param dataview - the dataview that we're starting from
	 * @param depth - used to control the recursion (default 4) but should not be passed
	 * @return the root system view
	 */
	Dataview getRootSystemView(Dataview dataview, int depth=4) {
		if (dataview.isSystem && dataview.overridesView == null) {
			return dataview
		}
		if (dataview.overridesView.isSystem) {
			return dataview.overridesView
		}
		if (depth < 1) {
			throwException(InvalidConfigurationException, 'dataview.validate.overrideNestingExceeded', [],
					'There is an unexpected amount of nested overridden system views')
		}
		return getRootSystemView(dataview.overridesView, --depth)
	}

	/**
	 * Dataview specifications coming from the frontend may have filters that were applied to columns that were from
	 * querystring parameters of the URL request. These parameters should not be saved with the dataview specification.
	 * Therefore, this method will iterate over the list of parameters passed and clear out the filter for any of the
	 * specified columns.
	 * @param dataviewCommand
	 */
	void removeQueryStringFromFilters(DataviewCrudCommand dataviewCommand) {
		if (dataviewCommand.querystring) {
			dataviewCommand.querystring.keySet().each { param ->
				List<String> match = param.split('_')
				if (match.size() == 2 && match[0] ) {
					DataviewSchemaColumnCommand column = dataviewCommand.schema.columns.find {
						it.domain == match[0] && it.property == match[1]
					}
					if (column) {
						column.filter = ''
					}
				}
			}
		}
	}

	/**
	 * Validates that the filters are unchanged when the user is attempting to override a system view
	 * @param dataviewCommand - the command object that has the override and original system view
	 */
	void validateOverrideViewFiltersMatch(DataviewCrudCommand dataviewCommand) {
		if (dataviewCommand.overridesView) {
			// Get the list of filters fzrom the orginal system view
			Map systemViewFilters = [:]
			JSONObject schema = dataviewCommand.overridesView.schemaAsJSONObject()
			schema?.columns.each {
				systemViewFilters.put(it.property, [filter: it.filter, label: it.label] )
			}

			// Iterate over the list of fields in the new view to see if the filtering is different
			List<String>columnsWithBadFilters = []
			for (int i = 0; i < dataviewCommand.schema.columns.size(); i++ ) {
				DataviewSchemaColumnCommand column = dataviewCommand.schema.columns[i]
				if (systemViewFilters.containsKey(column.property)) {
					if (column.filter != systemViewFilters[column.property].filter) {
						columnsWithBadFilters << systemViewFilters[column.property].label
					}
				} else if (column.filter) {
					columnsWithBadFilters << column.label
				}
			}
			// Iterate over the systemViewsFilters that have filters (probably none) and make sure that they're the same
			// First get a short list of systemView columns that are not in the override view
			List<String>systemViewColumnNames = systemViewFilters.keySet() as List
			List<String>overrideViewColumnNames = schema?.columns.collect { it.property }
			List<String>missingSystemColumns = systemViewColumnNames - overrideViewColumnNames
			missingSystemColumns.each {
				if (systemViewFilters[it].filter) {
					columnsWithBadFilters << systemViewFilters[it].label
				}
			}
			if (columnsWithBadFilters) {
				throwException(InvalidParamException.class,
						'dataview.validate.overrideFiltersMatch',
						[columnsWithBadFilters],
						"Filters for column(s) {$columnsWithBadFilters} must match the original system view")
			}
		}
	}

	/**
	 * Validates if whom accessing dataview is authorized to access it
	 * - should belong to current project in session
	 * - should be either system or shared or current whom in session owned
	 * @param project - the current project for the user context
	 * @param whom - the whom that is attempting to access the view
	 * @param dataview - the loaded dataview
	 * @throws net.transitionmanager.exception.InvalidRequestException
	 */
	void validateDataviewViewAccessOrException(Project project, Person whom, Dataview dataview) {
		boolean throwNotFound = false
		if (!dataview) {
			throwNotFound = true
		}

        boolean canAccess =
			(dataview.project.id == Project.DEFAULT_PROJECT_ID && (dataview.isSystem || dataview.isShared)) \
			|| \
			(dataview.project.id == project.id && (dataview.personId == whom.id || dataview.isShared))


		// Validate Dataview belongs to the project
		if (!throwNotFound && !canAccess) {
			securityService.reportViolation("attempted to access Dataview $dataview.id")
			throwNotFound = true
		}

		// Throw an exception if any of the above falied
		if (throwNotFound) {
			throw new EmptyResultException('Dataview not found')
		}
	}

	/**
	 * Validates if the whom updating a dataview has permission to it.
	 * @param project - the current project for the user context
	 * @param whom - the whom that is attempting to access the view
	 * @param dataviewCommand - the JSON object containing information about the Dataview to create
	 * @param dataview - original object from database
	 * @throws net.transitionmanager.exception.UnauthorizedException
	 */
	void validateDataviewUpdateAccessOrException(Project project, Person whom, DataviewCrudCommand dataviewCommand, Dataview dataview) {
		validateDataviewViewAccessOrException(project, whom, dataview)

		if (dataview.isSystem) {
			throwException(InvalidParamException.class, 'dataview.validate.modifySystemView', 'System views can not be modified. Please perform Save As and choose an Override options as an alternative.')
		}

		// Make sure that the name is unique across the all cases
		validateViewNameUniqueness(project, whom, dataviewCommand)
		String requiredPerm = dataview.isSystem ? Permission.AssetExplorerSystemEdit : Permission.AssetExplorerEdit
		if (! securityService.hasPermission(requiredPerm)) {
			securityService.reportViolation("attempted to modify Dataview ($dataview.id) without required permission $requiredPerm")
			throw new UnauthorizedException(requiredPerm)
		}
	}

	/**
	 * Validates if the whom creating a dataview has permission to create a Dataview
	 * @param dataviewCommand - the command object used for Dataview CRUD
	 * @param project - the current project
	 * @param whom - the whom that is attempting to create the Dataview
	 * @throws UnauthorizedException
	 */
	void validateDataviewCreateAccessOrException(DataviewCrudCommand dataviewCommand, Project project, Person whom) {
		if (dataview.isSystem) {
			throwException(InvalidParamException.class, 'dataview.validate.createSystemView', 'Creation of System views is not permitted.')
		}

		/*
		 * Users in the Default project
		 */
		if ( project.isDefaultProject() ) {
			if ( notPermitted(Permission.AssetExplorerSystemCreate) ) {
				throwException(InvalidParameterException,
						'dataview.validation.saveInDefaultProject',
						'You do not have the necessary permission to save into the Default project')
			}

			if (dataviewCommand.overridesView) {
				if (notPermitted(Permission.AssetExplorerOverrideAllUserGlobal) ) {
					throwException(InvalidParameterException,
							'dataview.validate.overrideGlobalPermission',
							'You do not have the necessary permission to save into the Default project')
				}

				// Only allow overriding System views in the Default project for All Users
				if (dataviewCommand.saveAsOption != ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL) {
					throwException(InvalidParameterException,
							'dataview.validate.overrideDefaultProjectOnlyAllUsers',
							'Overriding system views in Default project is only allowed for All Users')
				}

				// Check if the there is already an overridden view already
				if ( Dataview.where {
					project == project
					overridesView == dataviewCommand.overridesView
				}.count() > 0 ) {
					throwException(InvalidParameterException,
							'dataview.validate.overrideAlreadyExists',
							'An override for this system view already exists in the project')
				}
			}
		} else {
			// Saving into a user project
			switch (dataviewCommand.saveAsOption) {
				case ViewSaveAsOptionEnum.MY_VIEW:
					if (notPermitted(Permission.AssetExplorerCreate)) {
						throwException(UnauthorizedException,
								'dataview.validate.createPermission',
								'You do not have the necessary permission to create views')
					}
					break

				case ViewSaveAsOptionEnum.OVERRIDE_FOR_ME:
					if (notPermitted(Permission.AssetExplorerCreate)) {
						throwException(UnauthorizedException.class,
								'dataview.validate.createPermission',
								'You do not have the necessary permission to create views')
					}

					// Make sure that the user hasn't already saved an override for themselves for this system view
					if (Dataview.where {
						project.id == project.id
						overridesView.id == dataviewCommand.overridesView.id
						person.id == whom.id
						isShared == false
					}.count() > 0) {
						throwException(InvalidParameterException.class,
								'dataview.validate.overrideAlreadyExists',
								'An override for this system view already exists in the project')
					}
					break

				case ViewSaveAsOptionEnum.OVERRIDE_FOR_ALL:
					if (notPermitted(Permission.AssetExplorerOverrideAllUserProject)) {
						throwException(UnauthorizedException.class,
								'dataview.validate.overrideAllUsers',
								'You do not have the necessary permission to save override views for all users')
					}

					// Make sure that nobody has already saved an override for this system view that is shared in the project
					if (Dataview.where {
						project.id == project.id
						overridesView.id == dataviewCommand.overridesView.id
						isShared == true
					}.count() > 0) {
						throwException(InvalidParameterException,
								'dataview.validate.overrideAlreadyExists',
								'An override for this system view already exists in the project')
					}
					break
				default:
					throwSwitchNotHandledException(dataviewCommand.saveAsOption.toString())
			}
		}
	}

	/**
	 * Validates if whom deleting a Dataview has permission to do so
	 * @param project - the current project for the user context
	 * @param whom - the whom that is attempting to access the view
	 * @param id - the id of the dataview being checked (should match that of dataview.id - not sure why this is called if there is no dataview?)
	 * @param dataview - the loaded dataview
	 * @param dataview - original object from database
	 * @throws UnauthorizedException
	 */
	@Transactional
	void validateDataviewDeleteAccessOrException(Project project, Person whom, Long id, Dataview dataview) {
		validateDataviewViewAccessOrException(project, whom, dataview)

		String requiredPerm = dataview.isSystem ? Permission.AssetExplorerSystemDelete : Permission.AssetExplorerDelete
		if (! securityService.hasPermission(requiredPerm)) {
			securityService.reportViolation("attempted to delete Dataview ($dataview.id) without required permission $requiredPerm")
			throw new UnauthorizedException(requiredPerm)
		}
	}

	/**
	 * Checks to make sure that the name is unique to avoid confusion.
	 *
	 * The rules are:
	 * 		1) When view is overriding, don't bother checking
	 * 		2) When project is Default project:
	 * 			a) View must be shared
	 * 			b) Name must be unique across all projects
	 * 		3) When project is not the Default project
	 * 			- User is Sharing:
	 * 				a) Name can't exist in any view in the Default or Client projects
	 * 			- User is Not Sharing:
	 * 				b) Name not in Default project
	 * 				c) Name not shared by others in client project
	 * 				d User doesn't have saved view with same name in client project
	 *
	 * @param project - the current project for the user context
	 * @param whom - the whom that is attempting to access the view
	 * @param dataviewCommand - the CRUD command object for the view
	 * @throws net.transitionmanager.exception.InvalidParamException
	 */
	void validateViewNameUniqueness(Project project, Person whom, DataviewCrudCommand dataviewCommand) {
		// Only have to check views that are not overridden because there are other controls in place for that
		if (dataviewCommand.overridesView == null) {
			boolean isNotDefaultProject = ! project.isDefaultProject()
			Long dataviewId = dataviewCommand.id?.id

			// Check for a system view or the user's own
			List<Long> projectIds = [project.id]
			if (isNotDefaultProject) {
				projectIds << Project.DEFAULT_PROJECT_ID
			}

			// Check to see if the name already exists in the default project and not the current view
			// Test covers 2a, 2b, 3a (partially), 3b
			boolean foundDuplicateName = Dataview.where {
				project.id == Project.DEFAULT_PROJECT_ID
				name == dataviewCommand.name
				if (dataviewId) {
					id != dataviewId
				}
			}.count() > 0

			// Query for a view of the same name in the default project or current project
			// Test covers 3a (remainder), 3c, 3d
			if (! foundDuplicateName && isNotDefaultProject) {
				foundDuplicateName = Dataview.where {
					project.id == project.id
					name == dataviewCommand.name
					if (dataviewId) {
						id != dataviewId
					}
					( 	person.id == whom.id || (person.id != whom.id && dataviewCommand.isShared) )
				}.count() > 0
			}
			if (foundDuplicateName) {
				throwException(
						InvalidParamException.class,
						'dataview.validate.nameAlreadyExists',
						[],
						'A view with the same name already exists'
				)
			}
		}
	}

	/**
	 * Perform a query against one or domains specified in the DataviewSpec passed into the method
	 *
	 * @param project - the project that the data should be isolated to
	 * @param dataview - the specifications for the view/query
	 * @param apiParamsCommand - parameters from the user for filtering and sort order
	 * @return a Map with data as a List of Map values and pagination
	 */
	// TODO : Annotate READONLY
	Map query(Project project, Dataview dataview, DataviewApiParamsCommand apiParamsCommand) {
		FieldSpecProject fieldSpecProject = customDomainService.createFieldSpecProject(project)
		DataviewSpec dataviewSpec = new DataviewSpec(apiParamsCommand, dataview, fieldSpecProject)
		previewQuery(project, dataviewSpec)
	}

	/**
	 * Gets the hql for filtering by asset ids, based of the filters from the all asset views.
	 *
	 * @param project The current project used to limit the query.
	 * @param dataViewId The id of the dataview used in generating the DataviewSpec.
	 * @param userParams the user parameters from the front end that are used in generating the DataviewSpec.
	 *
	 * @return a map containing the hql query string, and the parameters needed to run the query.
	 */
	Map getAssetIdsHql(Project project, Long dataViewId, DataviewUserParamsCommand userParams) {
		Dataview dataview = get(Dataview, dataViewId, project)

		FieldSpecProject fieldSpecProject = customDomainService.createFieldSpecProject(project)
		DataviewSpec dataviewSpec = new DataviewSpec(userParams, dataview, fieldSpecProject)

		Map whereInfo = hqlWhere(dataviewSpec, project)
		String conditions = whereInfo.conditions
		String hqlJoins = hqlJoins(dataviewSpec)

		String query = """
			SELECT AE.id
			FROM AssetEntity AE
			$hqlJoins
			WHERE AE.project = :project AND $conditions
			group by AE.id
	    """

		return [query: query, params: whereInfo.params]
	}

	Map previewQuery(Project project, Dataview dataview, DataviewUserParamsCommand dataviewUserParamsCommand) {
		FieldSpecProject fieldSpecProject = customDomainService.createFieldSpecProject(project)
		DataviewSpec dataviewSpec = new DataviewSpec(dataviewUserParamsCommand, dataview, fieldSpecProject)
		previewQuery(project, dataviewSpec)
	}

    /**
     * Perform a query against one or domains specified in the DataviewSpec passed into the method
	 *
     * @param project - the project that the data should be isolated to
     * @param dataviewSpec - the specifications for the view/query as JSON
     * @return a Map with data as a List of Map values and pagination
     *
     * Example return values:
     * 		[
     * 			data: [
     * 		    	[
     *   				common.id: 12,
     * 	    			common.name: 'Exchange',
     * 		    		common.class: 'Application',
     * 				    common.bundle: 'M1',
     * 			    	application.sme: 'Joe',
     *   				application.owner: 'Tony'
     * 	    		],
     * 			    [
     * 				    common.id: 23,
     * 				    common.name: 'VM123',
     * 				    common.class: 'Device',
     * 				    common.bundle: 'M1',
     * 				    device.os: 'Windows'
     * 				    device.serial: '123123123',
     * 				    device.tag: 'TM-234'
     * 			    ]
     * 			],
     * 		   pagination: [
     * 		        offset: 350,
     * 		        max: 100,
     * 		        total: 472
     * 		   ]
     * 		]
     */
    // TODO : Annotate READONLY
    Map previewQuery(Project project, DataviewSpec dataviewSpec) {

		dataviewSpec = addRequieredColumns(dataviewSpec)

		String hqlColumns = hqlColumns(dataviewSpec)
		Map whereInfo = hqlWhere(dataviewSpec, project)
		String conditions = whereInfo.conditions
		Map whereParams = whereInfo.params
		String hqlOrder = hqlOrder(dataviewSpec)
		String hqlJoins = hqlJoins(dataviewSpec)

		String hql = """
            select $hqlColumns
              from AssetEntity AE
                $hqlJoins
             where AE.project = :project 
			   and $conditions
		  group by AE.id
          order by $hqlOrder
        """

		String countHql = """
            select count(DISTINCT AE)
              from AssetEntity AE
                $hqlJoins
             where AE.project = :project and $conditions
        """

		def assets = AssetEntity.executeQuery(hql, whereParams, dataviewSpec.args)
	    def totalAssets = AssetEntity.executeQuery(countHql, whereParams)

	    Map queryResults = previewQueryResults(assets, totalAssets[0], dataviewSpec)

	    postProcessAssetQuery(queryResults, whereInfo.mixedFields)

	    return queryResults

    }

	/**
	 * After the query for assets is invoked, this method needs to be called
	 * to perform some final operations on the query results, if needed.
	 *
	 * @param queryResults
	 * @param mixedFieldsInfo
	 */
	private void postProcessAssetQuery(Map queryResults, Map mixedFieldsInfo) {
		// Check if mixed fields were detected when building the query.
		if (mixedFieldsInfo) {
			mixedFieldsInfo.each {field, fieldInfo ->
				Closure transformer = mixedTransformerFor(field)
				if (transformer) {
					transformer(queryResults, field, fieldInfo)
				}
			}
		}

		// Add the flags signaling whether or not each asset has comments and/or tasks associated.
		appendTasksAndComments(queryResults)

	}

	/**
	 * After querying for the assets we need to determine if the returned assets
	 * have comments and/or tasks associated.
	 *
	 * @param queryResults - the result of the assets query.
	 */
	private void appendTasksAndComments(Map queryResults) {
		// Build a list with the asset ids and initialize tasks and comments flags.
		List<Long> assetIds = []
		// List with the assets found by the preview method.
		List<Map> assetResults = queryResults['assets']
		// This map will contain, for each asset, the flags for each comment type.
		Map<Long, Map<String, Boolean>> commentsAndTasksMap = [:]
		// Iterate over the assets keeping their id and initializing the map for its flags.
		assetResults.each { Map assetMap ->
			Long assetId = NumberUtil.toLong(assetMap['common_id'])
			assetIds << assetId
			commentsAndTasksMap[assetId] = [issue: false, comment: false]
		}

		if (assetIds) {
			// Query for assets and the comment types associated with them.
			String tasksAndCommentsQuery = """
				SELECT assetEntity.id, commentType FROM AssetComment 
				WHERE assetEntity.id IN (:assetIds) AND isPublished = true
				GROUP BY assetEntity.id, commentType
			"""

			// Execute the query.
			List tasksAndComments = AssetComment.executeQuery(tasksAndCommentsQuery, [assetIds: assetIds])

			// Iterate over the results from the database updating the flags map.
			for (taskOrComment in tasksAndComments) {
				Long assetId = taskOrComment[0]
				String commentType = taskOrComment[1]
				commentsAndTasksMap[assetId][commentType] = true
			}

			// Iterate over the preview assets setting the flags map as an attribute.
			assetResults.each { Map assetMap ->
				Long assetId = NumberUtil.toLong(assetMap['common_id'])
				assetMap['taskAndCommentFlags'] = commentsAndTasksMap[assetId]
			}
		}
	}

	/**
	 * Retrieve the list of favorite data views for the current whom,
	 * @return List of dataviews (DataView instances) the user has favorited.
	 */
	List<Dataview> getFavorites(Person person) {
		// Retrieve all the favorited views for the whom.
		/* Using createCriteria instead of where because there's no way of limiting the number
		  of results using DetachedCriteria */
		List<FavoriteDataview> favorites = FavoriteDataview.createCriteria().list {
			and {
				person == person
			}
			projections {
				property('dataview')
			}
			maxResults(FAVORITES_MAX_SIZE)

		}
		return favorites
	}

	/**
	 * Delete the given dataview from the current whom's favorites.
	 * @param dataviewId
	 * @return
	 */
	@Transactional
	void deleteFavoriteDataview(Person person, Project currentProject, Long dataviewId) throws EmptyResultException, DomainUpdateException{
		Dataview dataview = fetch(dataviewId)

		// Delete the corresponding favorite
		int deletedFavs = FavoriteDataview.where{
			person == person && dataview == dataview
		}.deleteAll()

		// If no favs were deleted, throw an exception.
		if (deletedFavs == 0) {
			throw new DomainUpdateException('Favorite was not found')
		}
	}

	/**
	 * Add the given dataview to the current whom's favorites.
	 * @param dataviewId
	 */
	@Transactional
	void addFavoriteDataview(Person person, Project currentProject, Long dataviewId) {
		Dataview dataview = fetch(dataviewId)

		// Check if the favorite already exists.
		FavoriteDataview favoriteDataview = FavoriteDataview.where {
			person == person && dataview == dataview
		}.find()

		// If a favorite was found, throw an exception
		if (favoriteDataview) {
			throw new DomainUpdateException('View is already a favorite')
		}

		favoriteDataview = new FavoriteDataview(person: person, dataview: dataview)
		if (!favoriteDataview.save(failOnError: false)) {
			throw new DomainUpdateException('Unable to create favorite', favoriteDataview)
		}
	}

    /**
     *
     * Prepares the previewQuery result with data and pagination details.
     *
     * In data it returns all rows with column domain correctly set. e.g. common.id or application.appOwner
     * In pagination it returns the following map:
     *
     *  [ max: max, offset: offset, total: total ]
     *
     * @param assets a list from HQL result
     * @param total total of asset for the HQL query that's paginated
     * @param dataviewSpec a DataviewSpec instace with order definition
     * @return a Map with data and pagination defined as [data: [.. ..], pagination: [ max: ..., offset: ..., total: ... ]]
     */
    private Map previewQueryResults(List assets, Long total, DataviewSpec dataviewSpec) {
	    // Find the position of the AssetClass field.
	    int indexOfAssetClassField = dataviewSpec.columns.findIndexOf {it.property == 'assetClass'}
        [
			pagination: [
				offset: dataviewSpec.offset, max: dataviewSpec.max?:total, total: total
			],
			assets: assets.collect { columns ->
				Map row = [:]
				columns = [columns].flatten()
				// Find the domain for this asset.
				String assetDomain = indexOfAssetClassField > -1 ? columns[indexOfAssetClassField].toLowerCase() : ''
				columns.eachWithIndex { cell, index ->
					Map column = dataviewSpec.columns[index]

					if (column.property == 'tagAssets'){
						cell = handleTags(cell)
					}
					if (column.property == 'scale'){
						if (cell) {
							cell = cell.value
						}
					}
					// Determine if the field is a custom field based on the field's spec.
					Boolean isCustom = column.fieldSpec?.isUserDefinedField
					// Determine if the (custom) field is shared or it belongs to the same domain.
					Boolean displayIfCustom = isCustom && (column.domain == assetDomain || column.fieldSpec.shared)
					// A field should be populated if it isn't user defined or it's a displayable custom field.
					Boolean shouldBePopulated = !isCustom || displayIfCustom
					if (column && shouldBePopulated) {
						row["${column.domain}${DataviewApiFilterParam.FIELD_NAME_SEPARATOR_CHARACTER}${column.property}"] = cell
					}
				}

				row
			}
        ]
    }

	private handleTags(String cell) {
		def json = JsonUtil.parseJson("""{"tags":$cell}""").tags

		return json.collect { Map tag ->
			tag.css = Color.valueOfParam(tag.color).css
			return tag
		}
	}

    /**
	 * Used to prepare the left outer join sentence based on
	 * columns of a dataview Spec and transformations for HQL query
     * @param dataviewSpec
     * @return the left outer join HQL sentence from the a Dataview spec
     */
	private String hqlJoins(DataviewSpec dataviewSpec) {
		return dataviewSpec.columns.collect { Map column ->
            "${joinFor(column)}"
        }.join(" ")
    }


	/**
	 * Creates a String with all the columns correctly set for select clause
	 * @param dataviewSpec an instance of {@code DataviewSpec}
	 * @return
	 */
	private String hqlColumns(DataviewSpec dataviewSpec){
		return dataviewSpec.columns.collect { Map column ->
			"${projectionPropertyFor(column)}"
		}.join(", ")
	}

    /**
     * Creates a String with all the columns correctly set for where clause
     * @param dataviewSpec
	 * @param project
     * @return
     */
	private Map<String, ?> hqlWhere(DataviewSpec dataviewSpec, Project project) {

		DataviewHqlWhereCollector whereCollector = new DataviewHqlWhereCollector()
		whereCollector.addParams([project: project ])

		if(!dataviewSpec.domains.isEmpty()){
			whereCollector.addCondition("AE.assetClass in (:assetClasses)")
				.addParams([
				"assetClasses": dataviewSpec.domains.findResults { AssetClass.safeValueOf(it.toUpperCase()) }
			])
		}

		if (dataviewSpec.justPlanning != null) {
			whereCollector.addCondition("AE.moveBundle in (:moveBundles)")
				.addParams([
				moveBundles: MoveBundle.where {
					project == project && useForPlanning == dataviewSpec.justPlanning
				}.list()
			])
		}

		// Populate this list with the mix fields that the user is using for filtering.
		Map<String, List> mixedFieldsInfo = [:]

		// Iterate over each column
		dataviewSpec.columns.each { Map column ->
			addColumnFilter(column, project, whereCollector, mixedFieldsInfo)
		}

		// There are 2 types of extra filters:
		// 1) A simple extra filter like assetName == 'FOO', or application.appTech == 'Apple' or 'moveBundle.id' == '2323'
		dataviewSpec.fieldNameExtraFilters?.each { FieldNameExtraFilter extraFilter ->
			addColumnFilter(extraFilter.properties, project, whereCollector, mixedFieldsInfo)
		}

		// 2) Special extra filters resolved in {@code SpecialExtraFilter} class hierarchy
		dataviewSpec.specialExtraFilters?.each { SpecialExtraFilter extraFilter ->
			Map<String, ?> hqlExtraFilters = extraFilter.generateHQL(project)
			whereCollector.addCondition(hqlExtraFilters.hqlExpression).addParams(hqlExtraFilters.hqlParams)
		}

		return [
			conditions: whereCollector.conditions.join("\n\t\t\t   and "),
			params: whereCollector.params,
			mixedFields: mixedFieldsInfo
		]
	}

	/**
	 * <p>Add a column filter results in HSQL sentence.</p>
	 * <p>It uses {@code DataviewHqlWhereCollector} </p>
	 * @param column
	 * @param project
	 * @param whereCollector
	 * @param mixedKeys
	 * @param mixedFieldsInfo
	 */
	private void addColumnFilter(
		Map<String, ?> column,
		Project project,
		DataviewHqlWhereCollector whereCollector,
		Map<String, List> mixedFieldsInfo) {

		// The keys for all the declared mixed fields.
		Set mixedKeys = mixedFields.keySet()

		Class type = typeFor(column)
		String filter = filterFor(column)

		if (StringUtil.isNotBlank(filter)) {

			String property = propertyFor(column)

			// Create a basic FieldSearchData with the info for filtering an individual field.
			FieldSearchData fieldSearchData = new FieldSearchData([
				column           : property,
				columnAlias      : namedParameterFor(column),
				domain           : domainFor(column),
				filter           : filter,
				type             : type,
				whereProperty    : wherePropertyFor(column),
				manyToManyQueries: manyToManyQueriesFor(column),
				domainAlias: 	'AE',
				referenceProperty: column.referenceProperty,
				fieldSpec        : column.fieldSpec
			])


			// Check if the current column requires special treatment (e.g. startupBy, etc.)

			if (property in mixedKeys) {
				// Flag the fieldSearchData as mixed.
				fieldSearchData.setMixed(true)
				// Retrieve the additional results (e.g: persons matching the filter).
				Closure sourceForField = sourceFor(property)
				Map additionalResults = sourceForField(project, filterFor(column), mixedFieldsInfo)
				if (additionalResults) {
					// Keep a copy of this results for later use.
					mixedFieldsInfo[property] = additionalResults
					// Add additional information for the query (e.g: the staff ids for IN clause).
					Closure paramsInjector = injectWhereParamsFor(property)
					paramsInjector(fieldSearchData, property, additionalResults)
					// Add the sql where clause for including the additional fields in the query
					Closure whereInjector = injectWhereClauseFor(property)
					whereInjector(fieldSearchData, property)
				} else {
					// If no additional results, then unset the flag as no additional filtering should be required.
					fieldSearchData.setMixed(false)
				}
			}

			// Trigger the parsing of the parameter.
			SqlUtil.parseParameter(fieldSearchData)

			if (fieldSearchData.sqlSearchExpression) {
				// Append the where clause to the list of conditions.
				// hqlWhereConditions << fieldSearchData.sqlSearchExpression
				whereCollector.addCondition(fieldSearchData.sqlSearchExpression)
			}

			if (fieldSearchData.sqlSearchParameters) {
				// Add the parameters required for this field.
				// hqlWhereParams += fieldSearchData.sqlSearchParameters
				whereCollector.addParams(fieldSearchData.sqlSearchParameters)
			}

			// If the filter for this column is empty, some logic/transformation might still be required for the mixed fields
		} else {
			String property = propertyFor(column)
			if (property in mixedKeys) {
				Closure sourceForField = sourceFor(property)
				Map additionalResults = sourceForField(project, filterFor(column), mixedFieldsInfo)
				// Keep a copy of this results for later use.
				mixedFieldsInfo[property] = additionalResults
			}
		}
	}

    /**
     * Columnn filters value could be split by '|' separator
	 * @param column - a set of column attributes
	 * @return one or more strings based on filter being spilt
     * For example:
     * 		{ "domain": "common", "property": "environment", "filter": "production|development" }
     */
	private String[] splitColumnFilter(Map column) {
        return column.filter.split("\\|")
    }

    /**
     * Calculates the order from DataviewSpec for HQL query
     * @param dataviewSpec
     * @return a String HQL order sentence
     */
	private String hqlOrder(DataviewSpec dataviewSpec){
		return "${orderFor(dataviewSpec.order)} ${dataviewSpec.order.sort}"
    }

	/**
	 * Defines a named parameers
	 * @param column
	 * @return
	 */
    private static String namedParameterFor(Map column) {

		if (column.referenceProperty) {
			return column.property
		} else {
			return  transformations[column.property].namedParameter
		}
    }

	/**
	 * https://docs.jboss.org/hibernate/orm/3.5/reference/en/html/queryhql.html#queryhql-expressions
	 * @param column
	 * @return
	 */
	private static String propertyFor(Map column) {

		String property
		if (column.referenceProperty) {
			property = 'AE.' + column.property + '.' + column.referenceProperty
		} else if (column.fieldSpec?.isCustom()) {
			property = column.fieldSpec.getHibernateCastSentence(column.property)
		}  else {
			property = transformations[column.property].property
		}

		return property
	}

    private static String joinFor(Map column) {
		return transformations[column.property].join
    }

	private static String orderFor(Map column) {
		return transformations[column.property].alias ?: propertyFor(column)
	}

	/**
	 * Return the type for this column
	 * @param column
	 * @return
	 */
	private static Class typeFor(Map column) {

		Class type
		if (column.referenceProperty) {
			type = Long
		} else if (column.fieldSpec?.isCustom()) {
			type = column.fieldSpec.getClassType()
		} else {
			type = transformations[column.property].type
		}

		return type
	}

	/**
	 * Return the alias for the given column.
	 * @param column
	 * @return
	 */
	private static String aliasFor(Map column) {
		return transformations[column.property].alias
	}

	/**
	 * Build the projection for the given column.
	 *
	 * @param column a Map with column definitions
	 * @return
	 */
	private static String projectionPropertyFor(Map column) {
		String projection
		String property = propertyFor(column)
		String alias = aliasFor(column)
		if (alias) {
			projection = "$property AS $alias"
		} else {
			projection = property
		}

		return projection
	}


	/**
	 * Return the Domain class this column belongs to.
	 *
	 * @param column
	 * @return
	 */
	private static Class domainFor(Map column) {
		String domain = column.domain == "common" ? "DEVICE" : column.domain.toUpperCase()
		AssetClass assetClass = AssetClass.safeValueOf(domain)
		return AssetClass.domainClassFor(assetClass)
	}

	/**
	 * Retrieve the filter the user provided for this column.
	 * @param column
	 * @return
	 */
	private static String filterFor(Map column) {
		return column.filter
	}

	/**
	 * Return the expression that needs to be used when constructing the 'where'
	 * clause for the given field.
	 *
	 * This is added to catch scenarios, such as tags, where the expression for projecting the field
	 * and for filtering it are different.
	 * @param column
	 * @return
	 */
	private static String wherePropertyFor(Map column) {
		return transformations[column.property].whereProperty?: propertyFor(column)
	}

	/**
	 * Return the complementary query to be used in cases of many to many relationships.
	 * @param column
	 * @return
	 */
	private static Map manyToManyQueriesFor(Map column) {
		return transformations[column.property].manyToManyQueries
	}

	/**
	 * Return the parameter to be used when filtering many to many relationships.
	 * @param column
	 * @return
	 */
	private static String manyToManyParameterNameFor(Map column) {
		return transformations[column.property].manyToManyParameterName
	}


	/**
	 * Return the domain alias for the domain of the given field. This will be useful for handling special cases,
	 * such as custom fields, where additional columns need to be queried.
	 * @param column
	 * @return
	 */
	private static domainAliasFor(Map column) {
		return transformations[column.property].domainAlias
	}


	/**
	 * Return the closure for fetching additional elements for this
	 * mixed field.
	 *
	 * @param mixedField
	 * @return
	 */
	private static Closure sourceFor(String mixedField) {
		return mixedFields[mixedField]["source"]
	}

	/**
	 * Return the closure that needs to be executed in order to
	 * do the final replacement/transformation.
	 *
	 * @param mixedField
	 * @return
	 */
	private static Closure mixedTransformerFor(String mixedField) {
		Closure transformer = null
		Map field = mixedFields[mixedField]
		if (field) {
			transformer = field["transform"]
		}
		return transformer
	}

	private static String mixedAliasFor(String mixedField) {
		return mixedFields[mixedField]["mixedAlias"]
	}

	/**
	 * Return the closure that is executed to add additional where clauses according to the field.
	 *
	 * @param mixedField
	 * @return
	 */
	private static Closure injectWhereClauseFor(String mixedField) {
		return mixedFields[mixedField]["injectWhereClause"]
	}

	/**
	 * Return the closure to be executed to perform additional work on the
	 * given field before triggering the search, like providing the list
	 * of staff ids for the by properties.
	 *
	 * @param mixedField
	 * @return
	 */
	private static Closure injectWhereParamsFor(String mixedField) {
		return mixedFields[mixedField]["injectWhereParams"]
	}

	/**
	 * Return the alias to be used while post processing the query results.
	 *
	 * @param field
	 * @return
	 */
	private static String postProcessAliasFor(String field) {
		return mixedFields[field]["postProcessAlias"]
	}

	/**
	 * Tansformer for the By fields that replaces IDs with the
	 * corresponding whom's fullname.
	 */
	private static transformByField = { Map queryResults, field,  mixedResults->
		queryResults.assets.each { asset ->
			String fieldAlias = postProcessAliasFor(field)
			String originalValue = asset[fieldAlias]
			if (NumberUtil.isLong(originalValue)) {
				asset[fieldAlias] = mixedResults[originalValue]
			}
		}

	}

	/**
	 * Add the where for querying a list of persons ids.
	 */
	private static injectByPropertyWhereClause = { FieldSearchData fieldSearchData, String field ->
		fieldSearchData.setMixedSqlExpression("${field} in (:${mixedAliasFor(field)}")
	}

	/**
	 * Add the staff ids as a list of strings.
	 */
	private static injectByPropertyWhereParams = { FieldSearchData fieldSearchData, String property, Map additionalResults ->
		Set ids = additionalResults.keySet()
		fieldSearchData.addSqlSearchParameter(mixedAliasFor(property), ids)
	}

	/**
	 * Source for the By fields. It returns a map[id: fullName] for the persons
	 * that match the filter criteria.
	 *
	 * This closure is smart enough to avoid querying the database multiple times
	 * when all the staff is required.
	 */
	private static getMatchingStaff = { Project project, String filter, Map mixedFieldsInfo ->
		Map results = [:]
		boolean setAllStaff = false
		// Check if the filter is empty, in which case all the persons are required.
		if (!filter || !filter.trim()) {
			// Check if we already fetched all the persons
			if (mixedFieldsInfo["allStaff"]) {
				results = mixedFieldsInfo["allStaff"]
			// If they haven't been fetched, signal that they should be gathered and 'allStaff' set.
			} else {
				setAllStaff = true
			}
		}

		// Check if the query needs to be executed
		if (!results) {
			List<Person> individuals = projectService.getAssociatedStaffByName(project, filter)
			for (Person person : individuals) {
				results[person.id.toString()] = person.toString()
			}
		}

		// If the flag is set, update the mixedFieldsInfo to avoid executing the same query a second time.
		if (setAllStaff) {
			mixedFieldsInfo["allStaff"] = results
		}

		return results
	}


	/**
	 * Mixed fields are those, such as startupBy, shutdownBy and testingBy that may
	 * contain references to different things (teams, staff, other properties, etc).
	 *
	 * In this map you should specify the field and define a closure to be executed
	 * in order to fetch additional records (like persons) and the closure that
	 * is going to be executed to do the actual replacement/transformation.
	 */
	private static final Map mixedFields = [
			'AE.shutdownBy': [source: getMatchingStaff,
			                  injectWhereParams: injectByPropertyWhereParams,
			                  injectWhereClause: injectByPropertyWhereClause ,
			                  transform: transformByField,
			                  postProcessAlias: "application_shutdownBy",
			                  mixedAlias: "mixedShutdownBy"
			],
			'AE.startupBy': [source: getMatchingStaff,
			                 injectWhereParams: injectByPropertyWhereParams,
			                 injectWhereClause: injectByPropertyWhereClause ,
			                 transform: transformByField,
			                 postProcessAlias: "application_startupBy",
			                 mixedAlias: "mixedStartupBy"
			],
			'AE.testingBy': [source: getMatchingStaff,
			                 injectWhereParams: injectByPropertyWhereParams,
			                 injectWhereClause: injectByPropertyWhereClause ,
			                 transform: transformByField,
			                 postProcessAlias: "application_testingBy",
			                 mixedAlias: "mixedTestingBy"
			]
	]

    private static final Map<String, Map> transformations = [
		'id'             : [property: 'AE.id', type: Long, namedParameter: 'id', join: ''],
		'assetClass'     : [property: 'str(AE.assetClass)', type: String, namedParameter: 'assetClass', join: ''],
		'moveBundle'     : [property: 'AE.moveBundle.name', type: String, namedParameter: 'moveBundleName', join: 'left outer join AE.moveBundle'],
		'project'        : [property: 'AE.project.description', type: String, namedParameter: 'projectDescription', join: 'left outer join AE.project'],
		'manufacturer'   : [property: 'AE.manufacturer.name', type: String, namedParameter: 'manufacturerName', join: 'left outer join AE.manufacturer'],
		'appOwner'       : [property: SqlUtil.personFullName('appOwner', 'AE'),
							type: String, namedParameter: 'appOwnerName',
							join: 'left outer join AE.appOwner',
							alias:'appOwner'],
		'sme'            : [property: SqlUtil.personFullName('sme', 'AE'),
							type: String,
							namedParameter: 'smeName',
							join: 'left outer join AE.sme',
							alias:'sme'],
		'sme2'           : [property: SqlUtil.personFullName('sme2', 'AE'),
							type: String,
							namedParameter: 'sme2Name',
							join: 'left outer join AE.sme2',
							alias:'sme2'],
		'model'          : [property: 'AE.model.modelName', type: String, namedParameter: 'modelModelName', join: 'left outer join AE.model'],
		'locationSource' : [property: 'AE.roomSource.location', type: String, namedParameter: 'sourceLocation', join: 'left outer join AE.roomSource'],
		'rackSource'     : [property: 'AE.rackSource.tag', type: String, namedParameter: 'sourceRack', join: 'left outer join AE.rackSource'],
		'roomSource'     : [property: 'AE.roomSource.roomName', type: String, namedParameter: 'roomSourceName', join: 'left outer join AE.roomSource'],
		'locationTarget' : [property: 'AE.roomTarget.location', type: String, namedParameter: 'targetLocation', join: 'left outer join AE.roomTarget'],
		'rackTarget'     : [property: 'AE.rackTarget.tag', type: String, namedParameter: 'targetRack', join: 'left outer join AE.rackTarget'],
		'roomTarget'     : [property: 'AE.roomTarget.roomName', type: String, namedParameter: 'roomTargetName', join: 'left outer join AE.roomTarget'],
		'targetRackPosition': [property: "AE.targetRackPosition", type: Integer, namedParameter: 'targetRackPosition', join: ""],
		'sourceRackPosition': [property: "AE.sourceRackPosition", type: Integer, namedParameter: 'sourceRackPosition', join: ""],
		'sourceBladePosition': [property: "AE.sourceBladePosition", type: Integer, namedParameter: 'sourceBladePosition', join: ""],
		'targetBladePosition': [property: "AE.targetBladePosition", type: Integer, namedParameter: 'targetBladePosition', join: ""],
		'scale' : [property: 'AE.scale', type: SizeScale, namedParameter: 'scale', join: ''],
		'size': [property: "AE.size", type: Integer, namedParameter: 'size', join: ""],
		'rateOfChange': [property: "AE.rateOfChange", type: Integer, namedParameter: 'rateOfChange', join: ""],
	    'sourceChassis': [property: "AE.sourceChassis.assetName", type: String, namedParameter: 'sourceChassis', join: 'left outer join AE.sourceChassis'],
		'targetChassis': [property: "AE.targetChassis.assetName", type: String, namedParameter: 'targetChassis', join: 'left outer join AE.targetChassis'],
		'lastUpdated': [property: "AE.lastUpdated", type: Date, namedParameter: 'lastUpdated', join: ''],
		'maintExpDate': [property: "AE.maintExpDate", type: Date, namedParameter: 'maintExpDate', join: ''],
		'purchaseDate': [property: "AE.purchaseDate", type: Date, namedParameter: 'purchaseDate', join: ''],
		'retireDate': [property: "AE.retireDate", type: Date, namedParameter: 'retireDate', join: ''],
		'tagAssets': [
			property: """
				CONCAT(
					'[',
					if(
						TA.id,
						group_concat(
							json_object('id', TA.id, 'tagId', T.id, 'name', T.name, 'description', T.description, 'color', T.color)
						),
						''
					),
					']'
				)""",
			type: String,
			namedParameter: 'assetId',
			alias: 'tags',
			join: """
				left outer join AE.tagAssets TA
				left outer join TA.tag T
			""",
			whereProperty: 'AE.id',
			manyToManyQueries: [
			    AND : { String filter ->
				    List<String> numbers = filter.split('&')
				    List<Long> tagList = NumberUtil.toPositiveLongList(numbers)
				    Long listSize = tagList.size() // Assign to long to avoid 'Integer cannot be casted to Long' error.
				    return [
				        query: "SELECT asset.id FROM TagAsset WHERE tag.id in (:tagList) GROUP BY asset.id HAVING count(*) = :tagListSize",
					    params: [
					        tagList: tagList,
						    tagListSize: listSize
					    ]
				    ]
			    },
				OR: { String filter ->
					List<String> numbers = filter.split('\\|')
					List<Long> tagList = NumberUtil.toPositiveLongList(numbers)
					return [
					    query: "SELECT DISTINCT(ta.asset.id) FROM TagAsset ta WHERE tag.id in (:tagList)",
						params: [tagList: tagList]
					]

				}
			]
		],

    ].withDefault {
        String key -> [property: "AE." + key, type: String, namedParameter: key, join: "", mode:"where", domainAlias: "AE"]
    }

	/**
	 * Mutate DataViewSpec to add the required Columns when querying the DB
	 * This mutates the original Object
	 * @param dataviewSpec Original DataViewSpec
	 * @return the mutated dataViewSpec
	 */
	private DataviewSpec addRequieredColumns(DataviewSpec dataviewSpec){
		HashSet<String> requiredColumns = ['id', 'assetClass']

		requiredColumns = requiredColumns - dataviewSpec.columns*.property

		for(String property: requiredColumns) {
			dataviewSpec.addColumn(DataviewSpec.COMMON, property)
		}

		return dataviewSpec
	}

	/**
	 * Validate if the name for the view is valid.
	 * @param command
	 * @return
	 */
	boolean validateUniqueName(DataviewNameValidationCommand command) {
		return validateUniqueName(command.name, command.dataViewId)
	}

	/**
	 * Check if a given DataView name is unique across project.
	 *
	 * This method will check that there's no other DataView for this project
	 * with the same name or, if there is, that they have the same id.
	 *
	 * @param dataViewName
	 * @param dataViewId
	 * @param project (optional)
	 * @return  true if the DataView name is unique for this project, false otherwise.
	 */
	boolean validateUniqueName(String dataViewName, Long dataViewId, Project project = null) {
		boolean isUnique = true
		if (!project) {
			project = securityService.userCurrentProject
		}

		// If the name is null don't validate, throw an exception.
		if (!dataViewName) {
			throw new InvalidParamException("The DataView name cannot be null.")
		}

		Dataview dataView = Dataview.where {
			name == dataViewName
			project == project
		}.find()

		if (dataView) {
			// If the ids don't match or params has no id, then it's a duplicate.
			if (dataViewId != dataView.id) {
				isUnique = false
			}
		}
		return isUnique
	}
}
