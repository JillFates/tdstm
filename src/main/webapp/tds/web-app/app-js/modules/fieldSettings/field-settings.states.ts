// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {FieldsResolveService} from './resolve/fields-resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
import {PreventUrlChangeService} from './service/prevent-url-change.service';
// Components
import {FieldSettingsListComponent} from './components/list/field-settings-list.component';
// Models
import {Permission} from '../../shared/model/permission.model';

/**
 * Top menu parent section class.
 * @type {string}
 */
const TOP_MENU_PARENT_PROJECT = 'menu-parent-project';

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
				menu: ['FIELD_SETTINGS.PROJECT', 'FIELD_SETTINGS.ASSET_FIELD_SETTING'],
				topMenu: { parent: TOP_MENU_PARENT_PROJECT, child: 'menu-projects-field-settings', subMenu: true}
			},
			requiresAuth: true,
			requiresPermission: Permission.ProjectFieldSettingsView,
			hasPendingChanges: false
		},
		component: FieldSettingsListComponent,
		resolve: {
			fields: FieldsResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService],
		canDeactivate: [PreventUrlChangeService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(FieldSettingsRoute)]
})

export class FieldSettingsRouteModule {
}
