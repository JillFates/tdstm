// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {LicensesResolveService} from './resolve/licenses-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
import {PreferencesResolveService} from '../../shared/resolves/preferences-resolve.service';
// Components
import {LicenseListComponent} from './components/list/license-list.component';

export class LicenseManagerStates {
	public static readonly LICENSE_LIST = {
		url: 'list'
	};
}

const TOP_MENU_PARENT_SECTION = 'menu-parent-admin';

export const LicenseManagerRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: LicenseManagerStates.LICENSE_LIST.url},
	{
		path: LicenseManagerStates.LICENSE_LIST.url,
		data: {
			page: {
				title: 'LICENSE.MANAGER',
				instruction: '',
				menu: ['GLOBAL.ADMIN', 'LICENSE.MANAGER'],
				topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-admin-license-manager', subMenu: true }
			},
			requiresAuth: true,
		},
		component: LicenseListComponent,
		resolve: {
			licenses: LicensesResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService, PreferencesResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(LicenseManagerRoute)]
})

export class LicenseManagerRouteModule {
}