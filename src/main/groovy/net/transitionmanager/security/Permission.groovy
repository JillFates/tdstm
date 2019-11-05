package net.transitionmanager.security

/**
 * This interface defines the Permission types for the HasPermission Annotation
 * Used an interface instead of a class since is not going to be instantiated
 * the variables by default are static and final, can be 'inherited' so it can be
 * used in a more direct way
 *
 * @author rmacfarlane, oluna
 *
 * @see com.tdsops.common.security.spring.HasPermission
 */

interface Permission {
	String	AdminMenuView                 = 'AdminMenuView',
			AdminUtilitiesAccess          = 'AdminUtilitiesAccess',
			ActionAssignment			  = 'ActionAssignment',
			ActionCreate				  = 'ActionCreate',
			ActionDelete				  = 'ActionDelete',
			ActionEdit				  	  = 'ActionEdit',
			ActionInvoke			  	  = 'ActionInvoke',
			ActionRemoteAllowed			  = 'ActionRemoteAllowed',
			ActionReset				  	  = 'ActionReset',
			ActionViewScript			  = 'ActionViewScript',
			ApplicationRestart            = 'ApplicationRestart',
			ArchitectureView              = 'ArchitectureView',
			AssetCreate                   = 'AssetCreate',
			AssetCloneDependencies		  = 'AssetCloneDependencies',
			AssetDependenciesBulkSelect	  = 'AssetDependenciesBulkSelect',
			AssetDependencyEdit			  = 'AssetDependencyEdit',
			AssetDependencyDelete		  = 'AssetDependencyDelete',
			AssetDelete                   = 'AssetDelete',
			AssetEdit                     = 'AssetEdit',
			AssetExport                   = 'AssetExport',
			AssetBulkSelect               = 'AssetBulkSelect',
			AssetExplorerCreate			  = 'AssetExplorerCreate',
			AssetExplorerSystemList		  = 'AssetExplorerSystemList',
			AssetExplorerDelete			  = 'AssetExplorerDelete',
			AssetExplorerEdit			  = 'AssetExplorerEdit',
			AssetExplorerSaveAs			  = 'AssetExplorerSaveAs',
			AssetExplorerSystemCreate     = 'AssetExplorerSystemCreate',
			AssetExplorerSystemDelete     =	'AssetExplorerSystemDelete',
			AssetExplorerSystemEdit		  = 'AssetExplorerSystemEdit',
			AssetExplorerSystemSaveAs     = 'AssetExplorerSystemSaveAs',
			AssetExplorerOverrideAllUserGlobal = 'AssetExplorerOverrideAllUserGlobal',
			AssetExplorerOverrideAllUserProject = 'AssetExplorerOverrideAllUserProject',
			AssetExplorerPublish     	  = 'AssetExplorerPublish',
			AssetExplorerUnPublish        = 'AssetExplorerUnPublish',
			AssetImport                   = 'AssetImport',
			AssetMenuView                 = 'AssetMenuView',
			AssetView                     = 'AssetView',
			BundleCreate                  = 'BundleCreate',
			BundleDelete                  = 'BundleDelete',
			BundleEdit                    = 'BundleEdit',
			BundleMenuView                = 'BundleMenuView',
			BundleView                    = 'BundleView',
			CommentCreate                 = 'CommentCreate',
			CommentDelete                 = 'CommentDelete',
			CommentEdit                   = 'CommentEdit',
			CommentView                   = 'CommentView',
			CompanyCreate                 = 'CompanyCreate',
			CompanyDelete                 = 'CompanyDelete',
			CompanyEdit                   = 'CompanyEdit',
			CompanyView                   = 'CompanyView',
			CookbookView                  = 'CookbookView',
			CredentialCreate			  = 'CredentialCreate',
			CredentialEdit				  = 'CredentialEdit',
			CredentialView				  = 'CredentialView',
			CredentialDelete			  = 'CredentialDelete',
			DashboardMenuView             = 'DashboardMenuView',
			DataTransferBatchDelete       = 'DataTransferBatchDelete',
			DataTransferBatchProcess      = 'DataTransferBatchProcess',
			DataTransferBatchView         = 'DataTransferBatchView',
			DepAnalyzerGenerate           = 'DepAnalyzerGenerate',
			DepAnalyzerView               = 'DepAnalyzerView',
			EarlyAccessTMR                = 'EarlyAccessTMR',
			ETLScriptCreate               = "ETLScriptCreate",
			ETLScriptDelete               = "ETLScriptDelete",
			ETLScriptUpdate               = "ETLScriptUpdate",
			ETLScriptView                 = "ETLScriptView",
			ETLScriptLoadSampleData       = "ETLScriptLoadSampleData",
			EventChangeStatus             = 'EventChangeStatus',
			EventCreate                   = 'EventCreate',
			EventDashboardDialOverride    = 'EventDashboardDialOverride',
			EventDelete                   = 'EventDelete',
			EventEdit                     = 'EventEdit',
			EventMenuView                 = 'EventMenuView',
			EventView                     = 'EventView',
			HelpMenuView                  = 'HelpMenuView',
			LicenseView					  = 'LicenseView',
			LicenseAdministration		  = 'LicenseAdministration',
			LicenseDelete                 = 'LicenseDelete',
			ManufacturerCreate            = 'ManufacturerCreate',
			ManufacturerCreateFromImport  = 'ManufacturerCreateFromImport',
			ManufacturerDelete            = 'ManufacturerDelete',
			ManufacturerEdit              = 'ManufacturerEdit',
			ManufacturerList              = 'ManufacturerList',
			ManufacturerMerge             = 'ManufacturerMerge',
			ManufacturerView              = 'ManufacturerView',
			ModelCreate                   = 'ModelCreate',
			ModelCreateFromImport         = 'ModelCreateFromImport',
			ModelDelete                   = 'ModelDelete',
			ModelEdit                     = 'ModelEdit',
			ModelExport                   = 'ModelExport',
			ModelImport                   = 'ModelImport',
			ModelList                     = 'ModelList',
			ModelMerge                    = 'ModelMerge',
			ModelValidate                 = 'ModelValidate',
			ModelView                     = 'ModelView',
			MoveEventView				  = 'MoveEventView',
			NewsCreate                    = 'NewsCreate',
			NewsDelete                    = 'NewsDelete',
			NewsEdit                      = 'NewsEdit',
			NewsView                      = 'NewsView',
			NoticeCreate                  = 'NoticeCreate',
			NoticeDelete                  = 'NoticeDelete',
			NoticeEdit                    = 'NoticeEdit',
			NoticeView                    = 'NoticeView',
			PartyCreate                   = 'PartyCreate',
			PartyDelete                   = 'PartyDelete',
			PartyEdit                     = 'PartyEdit',
			PartyRelationshipTypeCreate   = 'PartyRelationshipTypeCreate',
			PartyRelationshipTypeDelete   = 'PartyRelationshipTypeDelete',
			PartyRelationshipTypeEdit     = 'PartyRelationshipTypeEdit',
			PartyRelationshipTypeView     = 'PartyRelationshipTypeView',
			PartyRoleCreate               = 'PartyRoleCreate',
			PartyRoleDelete               = 'PartyRoleDelete',
			PartyRoleEdit                 = 'PartyRoleEdit',
			PartyRoleView                 = 'PartyRoleView',
			PartyTypeCreate               = 'PartyTypeCreate',
			PartyTypeDelete               = 'PartyTypeDelete',
			PartyTypeEdit                 = 'PartyTypeEdit',
			PartyTypeView                 = 'PartyTypeView',
			PartyView                     = 'PartyView',
			AddPerson					  = 'AddPerson',   //todo: 03/17 this should be fixed, let it be in the meantime
			PersonBulkDelete              = 'PersonBulkDelete',
			PersonCreate                  = 'PersonCreate',
			PersonDelete                  = 'PersonDelete',
			PersonEdit                    = 'PersonEdit',
			PersonEditTDS                 = 'PersonEditTDS',
			PersonExpiryDate              = 'PersonExpiryDate',
			PersonExport                  = 'PersonExport',
			PersonImport                  = 'PersonImport',
			PersonShowView				  = 'PersonShowView',
			PersonStaffList               = 'PersonStaffList',
			PersonView                    = 'PersonView',
			PreferenceGet                 = 'PreferenceGet',
			PreferenceSet                 = 'PreferenceSet',
			ProgressList                  = 'ProgressList',
			ProgressView                  = 'ProgressView',
			ProjectCreate                 = 'ProjectCreate',
			ProjectManageDefaults			= 'ProjectManageDefaults',
			ProjectDelete                 = 'ProjectDelete',
			ProjectEdit                   = 'ProjectEdit',
			ProjectFieldSettingsEdit      = 'ProjectFieldSettingsEdit',
			ProjectFieldSettingsView      = 'ProjectFieldSettingsView',
			ProjectShowAll                = 'ProjectShowAll',
			ProjectStaffEdit              = 'ProjectStaffEdit',
			ProjectStaffList              = 'ProjectStaffList',
			ProjectStaffShow              = 'ProjectStaffShow',
			ProjectTeamCreate			  = 'ProjectTeamCreate',
			ProjectTeamDelete			  = 'ProjectTeamDelete',
			ProjectTeamEdit				  = 'ProjectTeamEdit',
			ProjectTeamView				  = 'ProjectTeamView',
			ProjectView                   = 'ProjectView',
			ProviderCreate					= 'ProviderCreate',
			ProviderDelete					= 'ProviderDelete',
			ProviderUpdate					= 'ProviderUpdate',
			ProviderView					= 'ProviderView',
			RackCreate                    = 'RackCreate',
			RackDelete                    = 'RackDelete',
			RackEdit                      = 'RackEdit',
			RackLayoutModify              = 'RackLayoutModify',
			RackMenuView                  = 'RackMenuView',
			RackView                      = 'RackView',
			RecipeCreate                  = 'RecipeCreate',
			RecipeDelete                  = 'RecipeDelete',
			RecipeEdit                    = 'RecipeEdit',
			RecipeGenerateTasks           = 'RecipeGenerateTasks',
			RecipeRelease				  = 'RecipeRelease',
			RecipeView                    = 'RecipeView',
			ReportMenuView                = 'ReportMenuView',
			ReportViewEventDay            = 'ReportViewEventDay',
			ReportViewEventPrep           = 'ReportViewEventPrep',
			ReportViewPlanning            = 'ReportViewPlanning',
			ReportViewProjectDailyMetrics = 'ReportViewProjectDailyMetrics',
			RolePermissionEdit            = 'RolePermissionEdit',
			RolePermissionView            = 'RolePermissionView',
			RoleTypeCreate                = 'RoleTypeCreate',
			RoleTypeDelete                = 'RoleTypeDelete',
			RoleTypeEdit                  = 'RoleTypeEdit',
			RoleTypeView                  = 'RoleTypeView',
			RoomCreate                    = 'RoomCreate',
			RoomDelete                    = 'RoomDelete',
			RoomEdit                      = 'RoomEdit',
			RoomListActionColumn          = 'RoomListActionColumn',
			RoomView                      = 'RoomView',
			SequenceGetNext               = 'SequenceGetNext',
			ShowListNews				  = 'ShowListNews',
			TaskBatchDelete               = 'TaskBatchDelete',
			TaskBatchRefresh              = 'TaskBatchRefresh',
			TaskBatchView                 = 'TaskBatchView',
			TaskChangeStatus              = 'TaskChangeStatus',
			TaskCreate                    = 'TaskCreate',
			TaskDelete                    = 'TaskDelete',
			TaskEdit                      = 'TaskEdit',
			TaskExport                    = 'TaskExport',
			TaskGraphView                 = 'TaskGraphView',
			TaskManagerView               = 'TaskManagerView',
			TaskPublish                   = 'TaskPublish',
			TaskSignMessage               = 'TaskSignMessage',
			TaskTimelineView              = 'TaskTimelineView',
			TaskView                      = 'TaskView',
			TaskViewCriticalPath          = 'TaskViewCriticalPath',
			TaskViewUnpublished           = 'TaskViewUnpublished',
			TestCaseMethodCall			  = 'TestCaseMethodCall',
			UserCreate                    = 'UserCreate',
			UserDelete                    = 'UserDelete',
			UserEdit                      = 'UserEdit',
			UserExport                    = 'UserExport',
			UserGeneralAccess             = 'UserGeneralAccess',
			UserImport                    = 'UserImport',
			UserListAll                   = 'UserListAll',
			UserResetOwnPassword          = 'UserResetOwnPassword',
			UserResetPassword             = 'UserResetPassword',
			UserSendActivations           = 'UserSendActivations',
			UserUnlock                    = 'UserUnlock',
			UserUpdateOwnAccount          = 'UserUpdateOwnAccount',
			UserView                      = 'UserView',
			ViewPacingMeters			  = 'ViewPacingMeters',
			TagCreate                     = 'TagCreate',
			TagDelete                     = 'TagDelete',
			TagEdit                       = 'TagEdit',
			TagView                       = 'TagView',
			TagMerge                      = 'TagMerge',

			// Refactor the following:
			moveBundleShowView			  = 'moveBundleShowView',
			rackLayouts					  = 'rackLayouts'
			// MoveDashboardView			  = 'MoveDashboardView',
}
