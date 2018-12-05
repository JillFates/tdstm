// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {ImportBatchListComponent} from './components/list/import-batch-list.component';
import {ImportAssetsComponent} from './components/import-assets/import-assets.component';
// Models
import {Permission} from '../../shared/model/permission.model';

const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export class ImportBatchStates {
	public static readonly IMPORT_BATCH_LIST = {
		url: 'list'
	};
	public static readonly IMPORT_BATCH_VIEW = {
		url: 'list/:id'
	};
	public static readonly IMPORT_ASSETS = {
		url: 'assets'
	};
}

export const ImportBatchRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ImportBatchStates.IMPORT_BATCH_LIST.url},
	{
		path: ImportBatchStates.IMPORT_BATCH_LIST.url,
		data: {
			page: {
				title: 'IMPORT_BATCH.MANAGE_LIST',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'IMPORT_BATCH.MANAGE_LIST'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-manage-dep-batches'}
			},
			requiresAuth: true,
			requiresPermission: Permission.DataTransferBatchView
		},
		component: ImportBatchListComponent,
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	},
	{
		path: ImportBatchStates.IMPORT_ASSETS.url,
		data: {
			page: {
				title: 'IMPORT_ASSETS.MANUAL_IMPORT.IMPORT_ASSETS_ETL',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'IMPORT_ASSETS.MANUAL_IMPORT.IMPORT_ASSETS_ETL'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-import-assets-etl'}
			},
			requiresAuth: true,
			hasPendingChanges: false
		},
		component: ImportAssetsComponent,
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	},
	{
		path: ImportBatchStates.IMPORT_BATCH_VIEW.url,
		data: {
			page: {
				title: 'IMPORT_BATCH.MANAGE_LIST',
				instruction: '',
				menu: ['ASSETS.ASSETS', 'IMPORT_BATCH.MANAGE_LIST'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-manage-dep-batches'}
			},
			requiresAuth: true,
			requiresPermission: Permission.DataTransferBatchView
		},
		component: ImportBatchListComponent,
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(ImportBatchRoute)]
})

export class ImportBatchRouteModule {
}