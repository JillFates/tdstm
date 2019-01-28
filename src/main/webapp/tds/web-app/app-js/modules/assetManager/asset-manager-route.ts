// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
import {ReportResolveService} from './resolve/report-resolve.service';
import {ReportsResolveService} from './resolve/reports-resolve.service';
import {FieldsResolveService} from './resolve/fields-resolve.service';
import {TagsResolveService} from './resolve/tags-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {AssetViewManagerComponent} from './components/asset-view-manager/asset-view-manager.component';
import {AssetViewConfigComponent} from './components/asset-view-config/asset-view-config.component';
import {AssetViewShowComponent} from './components/asset-view-show/asset-view-show.component';

/**
 * Asset Manager Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Manager Module
 */
export class AssetExplorerStates {
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
 * Top menu parent section class for all Assets Manager module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export const AssetManagerRoute: Routes = [
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
		component: AssetViewManagerComponent,
		resolve: {
			reports: ReportsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService],
		runGuardsAndResolvers: 'always'
	},
	{
		path: AssetExplorerStates.REPORT_CREATE.url,
		data: {
			page: {
				title: 'ASSET_EXPLORER.ASSET_EXPLORER',
				instruction: '',
				menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER', navigateTo: 'asset/' + AssetExplorerStates.REPORT_SELECTOR.url}, 'ASSET_EXPLORER.CREATE']
			},
			requiresAuth: true,
			requiresPermission: 'AssetExplorerCreate',
			hasPendingChanges: false
		},
		component: AssetViewConfigComponent,
		resolve: {
			tagList: TagsResolveService,
			report: ReportResolveService,
			reports: ReportsResolveService,
			fields: FieldsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService],
		runGuardsAndResolvers: 'always'
	},
	{
		path: AssetExplorerStates.REPORT_EDIT.url,
		data: {
			page: {
				title: 'ASSET_EXPLORER.ASSET_EXPLORER',
				instruction: '',
				menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER', navigateTo: 'asset/' + AssetExplorerStates.REPORT_SELECTOR.url}, 'ASSET_EXPLORER.EDIT']
			},
			requiresAuth: true,
			requiresPermission: 'AssetExplorerEdit',
			hasPendingChanges: false
		},
		component: AssetViewConfigComponent,
		resolve: {
			tagList: TagsResolveService,
			report: ReportResolveService,
			reports: ReportsResolveService,
			fields: FieldsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService],
		runGuardsAndResolvers: 'always'
	},
	{
		path: AssetExplorerStates.REPORT_SHOW.url,
		data: {
			page: {
				title: 'ASSET_EXPLORER.ASSET_EXPLORER',
				instruction: '',
				menu: ['ASSETS.ASSETS', { text: 'ASSET_EXPLORER.ASSET_EXPLORER', navigateTo: 'asset/' + AssetExplorerStates.REPORT_SELECTOR.url}, 'ASSET_EXPLORER.SHOW'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-asset-explorer', subMenu: true }
			},
			requiresAuth: true
		},
		component: AssetViewShowComponent,
		resolve: {
			tagList: TagsResolveService,
			report: ReportResolveService,
			reports: ReportsResolveService,
			fields: FieldsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService],
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(AssetManagerRoute)]
})

export class AssetManagerRouteModule {
}
