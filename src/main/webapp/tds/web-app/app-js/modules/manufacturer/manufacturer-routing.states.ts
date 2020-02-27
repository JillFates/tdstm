// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {ManufacturerListComponent} from './components/list/manufacturer-list.component';

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
				menu: ['GLOBAL.PROJECT', 'MANUFACTURER.LIST']
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