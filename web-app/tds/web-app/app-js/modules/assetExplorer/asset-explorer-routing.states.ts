// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
import {ReportResolveService} from './resolve/report-resolve.service';
import {ReportsResolveService} from './resolve/reports-resolve.service';
import {FieldsResolveService} from './resolve/fields-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
import {AssetExplorerService} from './service/asset-explorer.service';
import {PreferenceService} from '../../shared/services/preference.service';
import {TagService} from '../assetTags/service/tag.service';
// Components
import {AssetExplorerIndexComponent} from './components/index/asset-explorer-index.component';
import {AssetExplorerViewConfigComponent} from './components/view-config/asset-explorer-view-config.component';
import {AssetExplorerViewShowComponent} from './components/view-show/asset-explorer-view-show.component';
// Models
import {ApiResponseModel} from '../../shared/model/ApiResponseModel';

/**
 * Asset Explorer Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Explorer Module
 */
export class AssetExplorerStates {
	public static readonly PARENT = 'asset';
	public static readonly REPORT_SELECTOR = {
		url: 'views'
	};
	public static readonly REPORT_CREATE = {
		url: 'views/create'
	};
	public static readonly REPORT_EDIT = {
		url: 'views/:id/edit'
	};
	public static readonly REPORT_SHOW = {
		url: 'views/:id/show'
	};
}

/**
 * Top menu parent section class for all Assets Explorer module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

const assetsListSizeResolve = {
	token: 'preferences',
	policy: { async: 'RXWAIT', when: 'EAGER' },
	deps: [PreferenceService],
	resolveFn: (service: PreferenceService) => service.getPreference('assetListSize')
};

const resolveTagList = {
	token: 'tagList',
	policy: { async: 'RXWAIT' },
	deps: [TagService],
	resolveFn: (tagService: TagService) => tagService.getTags().map( (result: ApiResponseModel) => {
		return result.status === ApiResponseModel.API_SUCCESS && result.data ? result.data : [];
	})
};

/**
 * This state displays the field settings list.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The field settings are fetched using a resolve.
 */
export const AssetExplorerRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: AssetExplorerStates.REPORT_SELECTOR.url},
	{
		path: AssetExplorerStates.REPORT_SELECTOR.url,
		data: {
			page: {
				title: 'ASSET_EXPLORER.ASSET_EXPLORER',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'ASSET_EXPLORER.ASSET_EXPLORER'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-asset-manager', subMenu: true }
			},
			requiresAuth: true,
		},
		component: AssetExplorerIndexComponent,
		resolve: [
			{
				token: 'reports',
				policy: { async: 'RXWAIT' },
				deps: [AssetExplorerService],
				resolveFn: (service: AssetExplorerService) => service.getReports()
			}
		],
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	},
	{
		path: AssetExplorerStates.REPORT_CREATE.url,
		data: {
			page: {
				title: 'ASSET_EXPLORER.ASSET_EXPLORER',
				instruction: '',
				menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER'}, 'ASSET_EXPLORER.CREATE']
			},
			requiresAuth: true,
			requiresPermission: 'AssetExplorerCreate',
			hasPendingChanges: false
		},
		component: AssetExplorerViewConfigComponent,
		resolve: {
		// 	assetsListSizeResolve,
		// 	{
		// 		token: 'report',
		// 		policy: { async: 'RXWAIT' },
		// 		deps: [Transition],
		// 		resolveFn: (trans: Transition) => Observable.of(trans.targetState().params())
		// 	}, {
		// 		token: 'reports',
		// 		policy: { async: 'RXWAIT' },
		// 		deps: [AssetExplorerService],
		// 		resolveFn: (service: AssetExplorerService) => service.getReports()
		// 	},
		// 	resolveTagList
			fields: FieldsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	},
	{
		path: AssetExplorerStates.REPORT_EDIT.url,
		data: {
			page: {
				title: 'ASSET_EXPLORER.ASSET_EXPLORER',
				instruction: '',
				menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER'}, 'ASSET_EXPLORER.EDIT']
			},
			requiresAuth: true,
			requiresPermission: 'AssetExplorerEdit',
			hasPendingChanges: false
		},
		component: AssetExplorerViewConfigComponent,
		resolve: {
			// 	assetsListSizeResolve,
			// 	{
			// 		token: 'report',
			// 		policy: { async: 'RXWAIT' },
			// 		deps: [AssetExplorerService, Transition],
			// 		resolveFn: (service: AssetExplorerService, trans: Transition) => service.getReport(trans.params().id)
			// 	}, {
			// 		token: 'reports',
			// 		policy: { async: 'RXWAIT' },
			// 		deps: [AssetExplorerService],
			// 		resolveFn: (service: AssetExplorerService) => service.getReports()
			// 	},
			// 	resolveTagList
			report: ReportResolveService,
			reports: ReportsResolveService,
			fields: FieldsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	},
	{
		path: AssetExplorerStates.REPORT_SHOW.url,
		data: {
			page: {
				title: 'ASSET_EXPLORER.ASSET_EXPLORER',
				instruction: '',
				menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER'}, 'ASSET_EXPLORER.SHOW'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-asset-explorer', subMenu: true }
			},
			requiresAuth: true
		},
		component: AssetExplorerViewShowComponent,
		resolve: {
			// assetsListSizeResolve,
			// resolveTagList
			report: ReportResolveService,
			reports: ReportsResolveService,
			fields: FieldsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(AssetExplorerRoute)]
})

export class AssetExplorerRouteModule {
}
