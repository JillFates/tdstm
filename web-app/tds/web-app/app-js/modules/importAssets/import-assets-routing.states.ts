import { Ng2StateDeclaration } from '@uirouter/angular';
import { HeaderComponent } from '../../shared/modules/header/header.component';
import {ManualImportComponent} from './components/manual-import/manual-import.component';

export class ImportAssetsStates {
	public static readonly IMPORT_ASSETS = {
		name: 'tds.importassets',
		url: '/import/assets/'
	};
}

export const importAssetsState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: ImportAssetsStates.IMPORT_ASSETS.name,
	url: ImportAssetsStates.IMPORT_ASSETS.url,
	data: {
		page: {
			title: 'DEPENDENCY_INJECTION.MANUAL_ASSET_IMPORT',
			instruction: '',
			menu: []
		},
		requiresAuth: true,
		// requiresPermission: Permission.ProjectFieldSettingsView, TODO: add permissions.
		hasPendingChanges: false
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: ManualImportComponent }
	}
};

export const DEPENDENCY_INJECTION_STATES = [
	importAssetsState,
	{
		name: 'tds.import',
		url: '/import/assets',
		redirectTo: 'tds.importassets'
	}
];