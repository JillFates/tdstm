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
	{path: 'asset', loadChildren: '../modules/assetExplorer/asset-explorer.module#AssetExplorerModule'},
	{path: 'fieldsettings', loadChildren: '../modules/fieldSettings/field-settings.module#FieldSettingsModule'},
	{path: 'importbatch', loadChildren: '../modules/importBatch/import-batch.module#ImportBatchModule'},
	{path: 'provider', loadChildren: '../modules/provider/provider.module#ProviderModule'},
	{path: 'credential', loadChildren: '../modules/credential/credential.module#CredentialModule'},
	{path: 'action', loadChildren: '../modules/apiAction/api-action.module#APIActionModule'},
	{path: 'datascript', loadChildren: '../modules/dataScript/data-script.module#DataScriptModule'},
	{path: 'user', loadChildren: '../modules/user/user.module#UserModule'}
];

@NgModule({
	exports: [RouterModule],
	providers: [{provide: LocationStrategy, useClass: PathLocationStrategy}],
	imports: [RouterModule.forRoot(TDSAppRoute, {enableTracing: true, onSameUrlNavigation: 'reload'})]
})

export class TDSAppRouteModule {
}