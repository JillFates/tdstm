// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
import {TagsResolveService} from '../assetExplorer/resolve/tags-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {DependenciesViewGridComponent} from './components/view-grid/dependencies-view-grid.component';

/**
 * Top menu parent section class for all Dependencies module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export class DependenciesStates {
	public static readonly DEPENDENCIES_LIST = {
		url: 'list'
	};
}

export const DependenciesRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: DependenciesStates.DEPENDENCIES_LIST.url},
	{
		path: DependenciesStates.DEPENDENCIES_LIST.url,
		data: {
			page: {
				title: 'DEPENDENCIES.LIST_TITLE',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'DEPENDENCIES.MENU_TITLE'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-dependencies-list', subMenu: true }
			},
			requiresAuth: true,
		},
		component: DependenciesViewGridComponent,
		canActivate: [
			AuthGuardService,
			ModuleResolveService,
			PreferencesResolveService],
		resolve: {
			tagList: TagsResolveService
		},
		runGuardsAndResolvers: 'always'
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(DependenciesRoute)]
})
export class DependenciesRouteModule {
}
