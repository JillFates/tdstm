// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
import {TagsResolveService} from '../assetManager/resolve/tags-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {PreEventCheckListSelectorComponent} from './components/event-checklist/pre-event-checklist.component';

/**
 * Top menu parent section class for all Dependencies module.
 * @type {string}
 */
const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export class ReportStates {
	public static readonly LIST = {
		url: 'list'
	};
}

export const ReportsRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ReportStates.LIST.url},
	{
		path: ReportStates.LIST.url,
		data: {
			page: {
				title: 'DEPENDENCIES.LIST_TITLE',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'DEPENDENCIES.MENU_TITLE'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-dependencies-list', subMenu: true }
			},
			requiresAuth: true,
		},
		component: PreEventCheckListSelectorComponent,
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
	imports: [RouterModule.forChild(ReportsRoute)]
})
export class ReportsRouteModule {
}
