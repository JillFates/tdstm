/**
 * Jorge Morayta 09/13/2018
 * Refactored to use Native Angular routing
 */

/*
import { TDSAppComponent } from './tds-app.component';
import { AuthService } from '../shared/services/auth.service';

import { PermissionService } from '../shared/services/permission.service';
import { PreferenceService, PREFERENCES_LIST} from '../shared/services/preference.service';
import { UILoaderService } from '../shared/services/ui-loader.service';
import { UIPromptService } from '../shared/directives/ui-prompt.directive';
import { SharedStates } from '../shared/shared-routing.states';
// Services
import { TaskService } from '../modules/taskManager/service/task.service';
import { DictionaryService } from '../shared/services/dictionary.service';
import { LAST_VISITED_PAGE } from '../shared/model/constants';

 export const tdsRoot = {
	name: 'tds',
	url: '',
	component: TDSAppComponent,
	resolve: [
		{
			token: 'preferences',
			policy: { async: 'RXWAIT', when: 'EAGER' },
			deps: [PreferenceService],

			resolveFn: (service: PreferenceService) => service.getPreferences(
				PREFERENCES_LIST.CURR_TZ,
				PREFERENCES_LIST.CURRENT_DATE_FORMAT
			)
		}
	]
};

export function MiscConfig(router: UIRouter) {
	router.stateService.defaultErrorHandler((error) => {
		console.log(error);
		router.stateService.go(SharedStates.ERROR.name);
	});

	const transitionService = router.transitionService;
	transitionService.onStart({
		to: (state) => state.data,
		exiting: (state) => state.data && !state.data.hasPendingChanges
	}, (transition) => {
		const loaderService = transition.injector().get(UILoaderService) as UILoaderService;
		const dictionaryService = transition.injector().get(DictionaryService) as DictionaryService;
		dictionaryService.set(LAST_VISITED_PAGE, transition.from().name);
		loaderService.show();
	}, { priority: 10 });
	transitionService.onFinish({
		to: (state) => state.data
	}, (transition) => {
		const loaderService = transition.injector().get(UILoaderService) as UILoaderService;
		setTimeout(() => loaderService.hide());
	}, { priority: 10 });

	transitionService.onExit({
		exiting: (state) => state.data && state.data.hasPendingChanges
	}, (transition) => {
		const promptService = transition.injector().get(UIPromptService) as UIPromptService;
		let target = transition.to();
		const params = transition['_targetState']['_params'];
		const $state = transition.router.stateService;
		promptService.open(
			'Confirmation Required',
			'You have changes that have not been saved. Do you want to continue and lose those changes?',
			'Confirm', 'Cancel').then(result => {
				if (result) {
					$state.$current.data.hasPendingChanges = false;
					$state.go(target.name, params);
				}
			});
		return false;
	}, { priority: 10 });
}
*/

// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LocationStrategy, PathLocationStrategy} from '@angular/common';

export const TDSAppRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: 'security'},
	{path: 'security', loadChildren: '../modules/security/security.module#SecurityModule'},
	{path: 'tag', loadChildren: '../modules/assetTags/asset-tags.module#AssetTagsModule'},
	{path: 'asset', loadChildren: '../modules/assetExplorer/asset-explorer.module#AssetExplorerModule'}
];

@NgModule({
	exports: [RouterModule],
	providers: [{provide: LocationStrategy, useClass: PathLocationStrategy}],
	imports: [RouterModule.forRoot(TDSAppRoute, {enableTracing: true, onSameUrlNavigation: 'reload'})]
})

export class TDSAppRouteModule {
}