// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
import {FieldsResolveService} from './resolve/fields-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {FieldSettingsListComponent} from './components/list/field-settings-list.component';
// Models
import {Permission} from '../../shared/model/permission.model';

export class FieldSettingsStates {
	public static readonly LIST = {
		url: 'list'
	};
}

export const FieldSettingsRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: FieldSettingsStates.LIST.url},
	{
		path: FieldSettingsStates.LIST.url,
		data: {
			page: {
				title: 'FIELD_SETTINGS.ASSET_FIELD_SETTING',
				instruction: '',
				menu: ['FIELD_SETTINGS.PROJECT_LIST', 'FIELD_SETTINGS.ASSET_FIELD_SETTING']
			},
			requiresAuth: true,
			requiresPermission: Permission.ProjectFieldSettingsView,
			hasPendingChanges: false
		},
		component: FieldSettingsListComponent,
		resolve: {
			fields: FieldsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(FieldSettingsRoute)]
})

export class FieldSettingsRouteModule {
}