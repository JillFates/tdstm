import { Ng2StateDeclaration } from '@uirouter/angular';
import { FieldSettingsListComponent } from './components/list/field-settings-list.component';
import { HeaderComponent } from '../../shared/modules/header/header.component';
import { Permission } from '../../shared/model/permission.model';
import { FieldSettingsService } from './service/field-settings.service';

export class FieldSettingsStates {
	public static readonly LIST = {
		name: 'tds.fieldsettingslist',
		url: '/fieldsettings/list'
	};
}

/**
 * This state displays the field settings list.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The field settings are fetched using a resolve.
 */
export const fieldSettingListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: FieldSettingsStates.LIST.name,
	url: FieldSettingsStates.LIST.url,
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
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: FieldSettingsListComponent }
	},
	resolve: [
		{
			token: 'fields',
			policy: { async: 'RXWAIT' },
			deps: [FieldSettingsService],
			resolveFn: (service: FieldSettingsService) => service.getFieldSettingsByDomain()
		}
	]
};

export const FIELD_SETTINGS_STATES = [
	fieldSettingListState
];