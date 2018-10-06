// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
import {FieldSettingsService} from './service/field-settings.service';
// Components
import {FieldSettingsListComponent} from './components/list/field-settings-list.component';
// Models
import {Permission} from '../../shared/model/permission.model';
import {TagListComponent} from '../assetTags/components/tag-list/tag-list.component';
import {AssetTagsRoute} from '../assetTags/asset-tags-routing.states';

export class FieldSettingsStates {
	public static readonly LIST = {
		url: 'list'
	};
}

/**
 * This state displays the field settings list.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The field settings are fetched using a resolve.
 */
export const FieldSettingsRoute: Routes = [
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
		resolve: [
			{
				token: 'fields',
				policy: { async: 'RXWAIT' },
				deps: [FieldSettingsService],
				resolveFn: (service: FieldSettingsService) => service.getFieldSettingsByDomain()
			}
		],
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(FieldSettingsRoute)]
})

export class FieldSettingsRouteModule {
};