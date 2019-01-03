// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {LicensesResolveService} from './resolve/licenses-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {LicenseListComponent} from './components/list/license-list.component';

export class LicenseAdminStates {
	public static readonly LICENSE_LIST = {
		url: 'list'
	};
}

export const LicenseAdminRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: LicenseAdminStates.LICENSE_LIST.url},
	{
		path: LicenseAdminStates.LICENSE_LIST.url,
		data: {
			page: {
				title: 'LICENSE.ADMIN',
				instruction: '',
				menu: ['GLOBAL.ADMIN', 'LICENSE.ADMIN']
			},
			requiresAuth: true,
		},
		component: LicenseListComponent,
		resolve: {
			licenses: LicensesResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(LicenseAdminRoute)]
})

export class LicenseAdminRouteModule {
}