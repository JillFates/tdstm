// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {AssetSummaryListComponent} from './components/list/asset-summary-list.component';

/**
 * Asset Summary Routes
 * @class
 * @classdesc To use externally to reference possible state of the Asset Summary Module
 */
export class AssetSummaryRoutes {
	public static readonly SUMMARY_LIST = {
		url: 'list'
	};
}

/**
 * Top menu parent section class for all Assets Summary module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export const AssetSummaryRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: AssetSummaryRoutes.SUMMARY_LIST.url},
	{
		path: AssetSummaryRoutes.SUMMARY_LIST.url,
		data: {
			page: {
				title: 'ASSET_SUMMARY.ASSET_SUMMARY',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'ASSET_SUMMARY.SUMMARY'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-summary-table', subMenu: true }
			},
			requiresAuth: true,
		},
		component: AssetSummaryListComponent,
		canActivate: [AuthGuardService, ModuleResolveService],
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(AssetSummaryRoute)]
})

export class AssetSummaryRouteModule {
}
