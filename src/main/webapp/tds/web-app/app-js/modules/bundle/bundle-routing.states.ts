// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {BundleListComponent} from './components/list/bundle-list.component';
import {BundleResolveService} from './resolve/bundle-resolve.service';

/**
 * Top menu parent section class.
 * @type {string}
 */
const TOP_MENU_PARENT_PROJECT = 'menu-parent-planning';

/**
 * Asset Explorer Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Explorer Module
 */
export class BundleStates {
	public static readonly BUNDLE_LIST = {
		url: 'list'
	}
}

export const BundleRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: BundleStates.BUNDLE_LIST.url},
	{
		path: BundleStates.BUNDLE_LIST.url,
		data: {
			page: {
				title: 'PLANNING.BUNDLES.LIST',
				instruction: '',
				menu: ['PLANNING.PLANNING', 'PLANNING.BUNDLES.LIST'],
				topMenu: { parent: TOP_MENU_PARENT_PROJECT, child: 'menu-parent-planning-list-bundles', subMenu: true}
			},
			requiresAuth: true
		},
		component: BundleListComponent,
		resolve: {
			bundles: BundleResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService],
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(BundleRoute)]
})

export class BundleRouteModule {
}
