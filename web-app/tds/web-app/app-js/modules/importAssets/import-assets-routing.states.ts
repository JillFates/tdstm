import { Ng2StateDeclaration } from '@uirouter/angular';
import { HeaderComponent } from '../../shared/modules/header/header.component';
import {ManualImportComponent} from './components/manual-import/manual-import.component';

const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export class ImportAssetsStates {
	public static readonly IMPORT_ASSETS = {
		name: 'tds.importassets',
		url: '/import/assets'
	};
}

export const importAssetsState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: ImportAssetsStates.IMPORT_ASSETS.name,
	url: ImportAssetsStates.IMPORT_ASSETS.url,
	data: {
		page: {
			title: 'IMPORT_ASSETS.MANUAL_IMPORT.IMPORT_ASSETS_ETL',
			instruction: '',
			menu: ['ASSETS.ASSETS', 'IMPORT_ASSETS.MANUAL_IMPORT.IMPORT_ASSETS_ETL'],
			topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-import-assets-etl'}
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

export const IMPORT_ASSETS_STATES = [
	importAssetsState,
	{
		name: 'tds.import',
		url: '/importassets/',
		redirectTo: 'tds.importassets'
	}
];