/**
 * This migration will delete obsolete permissions, create new permissions that are needed, and rename existing permissions to enforce the new naming scheme
 * It will also remove the permission group property as it is no longer needed with the new naming scheme
 * @author rmacfarlane
 * TM-5576
 */
databaseChangeLog = {
	changeSet(author: "rmacfarlane", id: "20170227 TM-5576-1") {
		comment('remove the permissionGroup property for permissions')

		preConditions(onFail:'MARK_RAN') {
			columnExists(tableName:'permissions', columnName:'permission_group')
		}

		sql("ALTER TABLE permissions DROP COLUMN permission_group")
	}
	changeSet(author: "rmacfarlane", id: "20170227 TM-5576-2") {
		comment('Remove any permissions that are no longer needed.')

		grailsChange {
			change {
				def perms = ['ClientConsoleBulkEdit','ClientConsoleCheckBox','ClientConsoleComment','ClientTeamsList',
					'BulkChangeStatus','ShowActionColumn','ShowCartTracker','ShowListNews','TeamLinks','ViewPacingMeters',
					'AssetTrackerMenuView','ConsoleMenuView','AddPerson','PersonImport']

				ctx.getBean('databaseMigrationService').removePermissions(sql, perms)
			}
		}
	}

	changeSet(author: "rmacfarlane", id: "20170227 TM-5576-3") {
		comment('Rename permissions to follow the new naming convention of NounVerb')

		grailsChange {
			change {
				def perms = [
					'AdminUtilitiesAccess': [
						description:'Can view various administration tools',
						oldName:'ViewAdminTools'
					],
					'ApplicationRestart': [
						description:'Can Restart the Server Application running the configuration script',
						oldName:'RestartApplication'
					],
					'ArchitectureView': [
						description:'Can view the Architecture Graph'
					],
					'AssetDelete': [
						description:'Can delete apps, servers, database, or logical storage'
					],
					'AssetEdit': [
						description:'Can edit data for assets'
					],
					'AssetExport': [
						description:'Can export assets',
						oldName:'Export'
					],
					'AssetImport': [
						description:'Can Import assets',
						oldName:'Import'
					],
					'BundleEdit': [
						description:'Can edit bundles',
						oldName:'MoveBundleEditView'
					],
					'BundleView': [
						description:'Can view bundles',
						oldName:'MoveBundleShowView'
					],
					'CommentView': [
						description:'Can view asset comments',
						oldName:'CommentCrudView'
					],
					'CompanyCreate': [
						description:'Can create new Companies'
					],
					'CompanyDelete': [
						description:'Can delete existing Companies'
					],
					'CompanyEdit': [
						description:'Can edit Companies'
					],
					'DepAnalyzerGenerate': [
						description:'Can generate dependency groups'
					],
					'DepAnalyzerView': [
						description:'Can view the Dependency Analyzer'
					],
					'EventDashboardDialOverride': [
						description:'Can manually update the status dial on the event dashboard',
						oldName:'ManualOverride'
					],
					'AdminMenuView': [
						description:'Can access Admin menu items'
					],
					'AssetMenuView': [
						description:'Can access Asset menu items'
					],
					'BundleMenuView': [
						description:'Can access Bundle menu items'
					],
					'DashboardMenuView': [
						description:'Can access Dashboard menu items'
					],
					'EventMenuView': [
						description:'Can access Event menu items'
					],
					'HelpMenuView': [
						description:'Can access Help menu links'
					],
					'RackMenuView': [
						description:'Can access Rack menu items'
					],
					'ReportMenuView': [
						description:'Can access Report menu items'
					],
					'ModelCreateFromImport': [
						description:'Can auto generate device models from imported data',
						oldName:'NewModelsFromImport'
					],
					'ModelEdit': [
						description:'Can Edit device models',
						oldName:'EditModel'
					],
					'ModelValidate': [
						description:'Can mark device models as valid',
						oldName:'ValidateModel'
					],
					'ModelView': [
						description:'Can view models',
						oldName:'ModelDialogView'
					],
					'NewsCreate': [
						description:'Can create event dashboard news',
						oldName:'CreateNews'
					],
					'EventChangeStatus': [
						description:'Can edit Event status',
						oldName:'MoveEventStatus'
					],
					'EventEdit': [
						description:'Can edit Events',
						oldName:'MoveEventEditView'
					],
					'EventView': [
						description:'Can view Event detail',
						oldName:'MoveEventShowView'
					],
					'PartyCreate': [
						description:'Can create a Party (Admin, see schema for relationship)',
						oldName:'PartyCreateView'
					],
					'PartyEdit': [
						description:'Can edit a Party (Admin, see schema for relationship)',
						oldName:'PartyEditView'
					],
					'PartyRelationshipTypeCreate': [
						description:'Can create a Party Relationship Type (Admin, see schema for relationship)',
						oldName:'PartyRelationshipTypeCreateView'
					],
					'PartyRelationshipTypeEdit': [
						description:'Can edit a Party Relationship Type (Admin, see schema for relationship)',
						oldName:'PartyRelationshipTypeEditView'
					],
					'PartyTypeView': [
						description:'Can view a Party (Admin, see schema for relationship)',
						oldName:'PartyTypeShowView'
					],
					'PersonBulkDelete': [
						description:'Can bulk delete person accounts',
						oldName:'BulkDeletePerson'
					],
					'PersonCreate': [
						description:'Can create a named person (not a login)',
						oldName:'PersonCreateView'
					],
					'PersonDelete': [
						description:'Can delete a named person (not a login)',
						oldName:'PersonDeleteView'
					],
					'PersonEdit': [
						description:'Can edit a named person (not a login)',
						oldName:'PersonEditView'
					],
					'PersonEditTDS': [
						description:'Can edit non-client fields and data',
						oldName:'EditTDSPerson'
					],
					'PersonExpiryDate': [
						description:'Can edit expiry dates for people'
					],
					'PersonExport': [
						description:'Can export people'
					],
					'PersonImport': [
						description:'Can update and create persons from import',
						oldName:'ImportPerson'
					],
					'PersonStaffList': [
						description:'Can view the list of staff for companies',
						oldName:'PersonListView'
					],
					'ProjectCreate': [
						description:'Can create projects',
						oldName:'CreateProject'
					],
					'ProjectDelete': [
						description:'Can delete a project (and associated data)'
					],
					'ProjectEdit': [
						description:'Can edit project details',
						oldName:'ProjectEditView'
					],
					'ProjectFieldSettingsEdit': [
						description:'Can modify project field settings',
						oldName:'EditProjectFieldSettings'
					],
					'ProjectShowAll': [
						description:'Can view all projects',
						oldName:'ShowAllProjects'
					],
					'ProjectStaffEdit': [
						description:'Can assign staff to a project',
						oldName:'EditProjectStaff'
					],
					'ProjectStaffList': [
						description:'Can view list of project staff members'
					],
					'ProjectStaffShow': [
						description:'Can view detail of project staff members'
					],
					'RackLayoutModify': [
						description:'Can modify physical rack layouts',
						oldName:'EditAssetInRackLayout'
					],
					'RecipeCreate': [
						description:'Can create a recipe',
						oldName:'CreateRecipe'
					],
					'RecipeDelete': [
						description:'Can delete a recipe',
						oldName:'DeleteRecipe'
					],
					'RecipeEdit': [
						description:'Can edit existing recipes',
						oldName:'EditRecipe'
					],
					'RecipeGenerateTasks': [
						description:'Can generate tasks from an existing recipe',
						oldName:'GenerateTasks'
					],
					'RecipeView': [
						description:'Can view the Cookbook recipe panel',
						oldName:'ViewRecipe'
					],
					'ReportViewDiscovery': [
						description:'Can access discovery report menu items',
						oldName:'ShowDiscovery'
					],
					'ReportViewEventDay': [
						description:'Can access Event day report menu items',
						oldName:'ShowMoveDay'
					],
					'ReportViewEventPrep': [
						description:'Can access Event Prep report menu items',
						oldName:'ShowMovePrep'
					],
					'ReportViewPlanning': [
						description:'Can access planning report menu items',
						oldName:'ShowPlanning'
					],
					'ReportViewProjectDailyMetrics': [
						description:'Can view project daily metrics report',
						oldName:'ShowProjectDailyMetrics'
					],
					'RolePermissionView': [
						description:'Can view Role Permissions'
					],
					'RoleTypeCreate': [
						description:'Can create new staff team types (Admin)'
					],
					'RoleTypeView': [
						description:'Can view staff team types (Admin)',
						oldName:'RoleTypeEditView'
					],
					'RoomDelete': [
						description:'Can delete empty rooms',
						oldName:'DeleteRoom'
					],
					'RoomEdit': [
						description:'Can edit room physical layout',
						oldName:'RoomEditView'
					],
					'RoomListActionColumn': [
						description:'Can access room list actions'
					],
					'RoomMerge': [
						description:'Can merge two rooms into one',
						oldName:'MergeRoom'
					],
					'TaskBatchDelete': [
						description:'Can delete generated tasks',
						oldName:'DeleteTaskBatch'
					],
					'TaskBatchRefresh': [
						description:'Can refresh tasks to update schedule, durations, assignment, etc',
						oldName:'RefreshTaskBatch'
					],
					'TaskChangeStatus': [
						description:'Can change Task Status',
						oldName:'ChangePendingStatus'
					],
					'TaskGraphView': [
						description:'Can view Task Graph',
						oldName:'ViewTaskGraph'
					],
					'TaskManagerView': [
						description:'Can view Task Manager',
						oldName:'ViewTaskManager'
					],
					'TaskPublish': [
						description:'Can publish tasks that are presently unpublished',
						oldName:'PublishTasks'
					],
					'TaskTimelineView': [
						description:'Can view the Task Timeline',
						oldName:'ViewTaskTimeline'
					],
					'TaskViewCriticalPath': [
						description:'Can access the Critical Path Data of an event',
						oldName:'CriticalPathExport'
					],
					'UserCreate': [
						description:'Can create login credentials',
						oldName:'CreateUserLogin'
					],
					'UserDelete': [
						description:'Can delete existing user accounts',
						oldName:'UserLoginDelete'
					],
					'UserView': [
						description:'Can view user accounts list and details',
						oldName:'UserLoginView'
					],
					'UserEdit': [
						description: 'Can edit login credentials',
						oldName:'EditUserLogin'
					],
					'UserExport': [
						description:'Can export user login information',
						oldName:'ExportUserLogin'
					],
					'UserImport': [
						description:'Can update and create users from import',
						oldName:'ImportUserLogin'
					],
					'UserListAll': [
						description:'Can view list of all with login credentials',
						oldName:'ShowAllUsers'
					],
					'UserResetPassword': [
						description:'Can invoke the password reset process for local user accounts',
						oldName:'ResetUserPassword'
					],
					'UserSendActivations': [
						description:'Can send Activation to users',
						oldName:'SendUserActivations'
					],
					'UserUnlock': [
						description:'Can unlock local user accounts after failed login attempts',
						oldName:'UnlockUserLogin'
					]
				]
				ctx.getBean('databaseMigrationService').renamePermissions(sql, perms)
			}
		}
	}

	changeSet(author: "rmacfarlane", id: "20170227 TM-5576-4") {
		comment('add new permissions to cover previously not secured features')

		grailsChange {
			change {
				def perms = [
					'AssetCreate': [
						group: 'NONE',
						description: 'Can create new assets',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'AssetView': [
						group: 'NONE',
						description: 'Can view data for assets in dialogs and lists',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'BundleCreate': [
						group: 'NONE',
						description: 'Can create new bundles',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'BundleDelete': [
						group: 'NONE',
						description: 'Can delete existing bundles',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'CommentCreate': [
						group: 'NONE',
						description: 'Can create new asset comments',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'CommentDelete': [
						group: 'NONE',
						description: 'Can delete existing asset comments',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'CommentEdit': [
						group: 'NONE',
						description: 'Can edit asset comments',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'CompanyView': [
						group: 'NONE',
						description: 'Can view Company information',
						roles: ['ADMIN']
					],
					'CookbookView': [
						group: 'NONE',
						description: 'Can access the cookbook feature',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'DataTransferBatchDelete': [
						group: 'NONE',
						description: 'Can delete data transfer batches',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'DataTransferBatchProcess': [
						group: 'NONE',
						description: 'Can process data transfer batches',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'DataTransferBatchView': [
						group: 'NONE',
						description: 'Can view data transfer batches',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'ManufacturerCreate': [
						group: 'NONE',
						description: 'Can create new manufacturers',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'ManufacturerCreateFromImport': [
						group: 'NONE',
						description: 'Can auto generate device manufacturers from imported data',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'ManufacturerDelete': [
						group: 'NONE',
						description: 'Can delete existing manufacturers',
						roles: ['ADMIN']
					],
					'ManufacturerEdit': [
						group: 'NONE',
						description: 'Can edit manufacturers',
						roles: ['ADMIN']
					],
					'ManufacturerList': [
						group: 'NONE',
						description: 'Can view the list of all manufacturers',
						roles: ['ADMIN']
					],
					'ManufacturerMerge': [
						group: 'NONE',
						description: 'Can merge two device manufacturers into one',
						roles: ['ADMIN']
					],
					'ManufacturerView': [
						group: 'NONE',
						description: 'Can view manufacturers',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'ModelCreate': [
						group: 'NONE',
						description: 'Can create new models',
						roles: ['ADMIN']
					],
					'ModelDelete': [
						group: 'NONE',
						description: 'Can delete existing models',
						roles: ['ADMIN']
					],
					'ModelExport': [
						group: 'NONE',
						description: 'Can export the model library',
						roles: ['ADMIN']
					],
					'ModelImport': [
						group: 'NONE',
						description: 'Can import a new model library',
						roles: ['ADMIN']
					],
					'ModelList': [
						group: 'NONE',
						description: 'Can view the list of all models',
						roles: ['ADMIN']
					],
					'ModelMerge': [
						group: 'NONE',
						description: 'Can merge two device models into one',
						roles: ['ADMIN']
					],
					'EventCreate': [
						group: 'NONE',
						description: 'Can create Events',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'EventDelete': [
						group: 'NONE',
						description: 'Can delete Events',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'TaskExport': [
						group: 'NONE',
						description: 'Can export tasks for an event',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'NewsDelete': [
						group: 'NONE',
						description: 'Can delete event dashboard news',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'NewsEdit': [
						group: 'NONE',
						description: 'Can edit event dashboard news',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'NewsView': [
						group: 'NONE',
						description: 'Can view event dashboard news',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'NoticeCreate': [
						group: 'NONE',
						description: 'Can create new notices',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'NoticeDelete': [
						group: 'NONE',
						description: 'Can delete notices',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'NoticeEdit': [
						group: 'NONE',
						description: 'Can edit notices',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'NoticeView': [
						group: 'NONE',
						description: 'Can fetch and view notices',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyDelete': [
						group: 'NONE',
						description: 'Can delete a Party (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyView': [
						group: 'NONE',
						description: 'Can view a Party (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyRelationshipTypeDelete': [
						group: 'NONE',
						description: 'Can delete a Party Relationship Type (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyRelationshipTypeView': [
						group: 'NONE',
						description: 'Can view a Party Relationship Type (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyRoleCreate': [
						group: 'NONE',
						description: 'Can create new Party Roles (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyRoleDelete': [
						group: 'NONE',
						description: 'Can delete Party Roles (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyRoleEdit': [
						group: 'NONE',
						description: 'Can edit Party Roles (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyRoleView': [
						group: 'NONE',
						description: 'Can view Party Roles (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyTypeCreate': [
						group: 'NONE',
						description: 'Can create a Party (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyTypeDelete': [
						group: 'NONE',
						description: 'Can delete a Party (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PartyTypeEdit': [
						group: 'NONE',
						description: 'Can edit a Party (Admin, see schema for relationship)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'PersonView': [
						group: 'NONE',
						description: 'Can view people',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
					],
					'PreferenceGet': [
						group: 'NONE',
						description: 'Can get user preferences',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'PreferenceSet': [
						group: 'NONE',
						description: 'Can set user preferences',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'ProgressList': [
						group: 'NONE',
						description: 'Can list the progresses of asynchronous tasks running on the server',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'ProgressView': [
						group: 'NONE',
						description: 'Can view the progress of asynchronous tasks running on the server',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'ProjectFieldSettingsView': [
						group: 'NONE',
						description: 'Can view project field settings',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
					],
					'ProjectView': [
						group: 'NONE',
						description: 'Can view a project\'s details',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'RackCreate': [
						group: 'NONE',
						description: 'Can create physical racks',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'RackDelete': [
						group: 'NONE',
						description: 'Can delete physical racks',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'RackEdit': [
						group: 'NONE',
						description: 'Can edit physical racks',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'RackView': [
						group: 'NONE',
						description: 'Can view physical racks',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'RolePermissionEdit': [
						group: 'NONE',
						description: 'Can edit Role Permissions',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
					],
					'RoleTypeDelete': [
						group: 'NONE',
						description: 'Can delete existing staff team types (Admin)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'RoleTypeEdit': [
						group: 'NONE',
						description: 'Can edit staff team types (Admin)',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'RoomCreate': [
						group: 'NONE',
						description: 'Can create new rooms',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'RoomView': [
						group: 'NONE',
						description: 'Can view room physical layout',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],
					'SequenceGetNext': [
						group: 'NONE',
						description: 'Can get the next sequence number',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'TaskBatchView': [
						group: 'NONE',
						description: 'Can view generated task batches',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'TaskCreate': [
						group: 'NONE',
						description: 'Can create new tasks',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'TaskDelete': [
						group: 'NONE',
						description: 'Can delete existing tasks',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'TaskEdit': [
						group: 'NONE',
						description: 'Can edit tasks',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'TaskSignMessage': [
						group: 'NONE',
						description: 'Can sign messages using the qz sign service',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'TaskView': [
						group: 'NONE',
						description: 'Can view tasks',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'TaskViewUnpublished': [
						group: 'NONE',
						description: 'Can view tasks that haven\'t been published yet',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],
					'UserGeneralAccess': [
						group: 'NONE',
						description: 'Can use basic user functions',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'UserResetOwnPassword': [
						group: 'NONE',
						description: 'Can invoke the password reset process for the user\'s own account',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'UserUpdateOwnAccount': [
						group: 'NONE',
						description: 'Can update the data for a user\'s own account',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],
					'WorkflowCreate': [
						group: 'NONE',
						description: 'Can create new workflows',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'WorkflowDelete': [
						group: 'NONE',
						description: 'Can delete existing workflows',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'WorkflowEdit': [
						group: 'NONE',
						description: 'Can edit existing workflows',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'WorkflowList': [
						group: 'NONE',
						description: 'Can view the list of all workflows',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'WorkflowView': [
						group: 'NONE',
						description: 'Can view individual workflows',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					]
				]

				ctx.getBean('databaseMigrationService').addPermissions(sql, perms)
			}
		}
	}

	changeSet(author: "oluna", id: "20170227 TM-5576-5.v2") {
		comment('add new permissions to cover License Admin features')

		grailsChange {
			change {
				def perms = [
						'LicenseView'          : [
								group      : 'NONE',
								description: 'Can View License Information',
								roles      : ['ADMIN']
						],
						'LicenseAdministration': [
								group      : 'NONE',
								description: 'Can perform License administration tasks',
								roles      : ['ADMIN']
						],
						'LicenseDelete'        : [
								group      : 'NONE',
								description: 'Can Delete Licenses',
								roles      : ['ADMIN']
						]
				]

				def databaseMigrationService = ctx.getBean('databaseMigrationService')
				databaseMigrationService.removePermissions(sql, perms.keySet().asList())
				databaseMigrationService.addPermissions(sql, perms)
			}
		}
	}

	changeSet(author: "jmartin", id: "20170227 TM-5576-6") {
		comment('Renamed the ReleaseRecipe permission')

		grailsChange {
			change {
				def perms = [
					'RecipeRelease': [
						description:'Can release a recipe for production use',
						oldName:'ReleaseRecipe'
					]
				]
				ctx.getBean('databaseMigrationService').renamePermissions(sql, perms)
			}
		}
	}
}
