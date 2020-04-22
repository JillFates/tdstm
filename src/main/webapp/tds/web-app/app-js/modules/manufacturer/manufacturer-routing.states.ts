// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {ManufacturerListComponent} from './components/list/manufacturer-list.component';
import {ExportManufacturerModelsComponent} from './components/export-manufacturer-models/export-manufacturer-models.component';
import {ModelStates} from '../model/model-routing.states';

/**
 * Top menu parent section class.
 * @type {string}
 */
const TOP_MENU_PARENT_PROJECT = 'menu-parent-admin';

export class ManufacturerState {
	public static readonly MANUFACTURER_LIST = {
		url: 'list'
	};
}

export const ManufacturerRoutes: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ManufacturerState.MANUFACTURER_LIST.url},
	{
		path: ManufacturerState.MANUFACTURER_LIST.url,
		data: {
			page: {
				title: 'MANUFACTURER.LIST',
				instruction: '',
				menu: ['GLOBAL.ADMIN', 'MANUFACTURER.LIST'],
				topMenu: {parent: TOP_MENU_PARENT_PROJECT, child: 'menu-parent-admin-manufacturers', subMenu: true}
			},
			requiresAuth: true,
		},
		component: ManufacturerListComponent,
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(ManufacturerRoutes)]
})

export class ManufacturerRouteModule {
}