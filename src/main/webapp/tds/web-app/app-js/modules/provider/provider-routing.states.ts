// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {ProvidersResolveService} from './resolve/providers-resolve.service';
// Services
import {AuthGuardService} from '../auth/service/auth.guard.service';
// Components
import {ProviderListComponent} from './components/list/provider-list.component';

/**
 * Top menu parent section class.
 * @type {string}
 */
const TOP_MENU_PARENT_PROJECT = 'menu-parent-project';

export class ProviderStates {
	public static readonly PROVIDER_LIST = {
		url: 'list'
	};
}

export const ProviderRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ProviderStates.PROVIDER_LIST.url},
	{
		path: ProviderStates.PROVIDER_LIST.url,
		data: {
			page: {
				title: 'PROVIDER.PROVIDERS',
				instruction: '',
				menu: ['GLOBAL.PROJECT', 'PROVIDER.PROVIDERS'],
				topMenu: { parent: TOP_MENU_PARENT_PROJECT, child: 'menu-parent-projects-providers', subMenu: true}
			},
			requiresAuth: true,
		},
		component: ProviderListComponent,
		resolve: {
			providers: ProvidersResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(ProviderRoute)]
})

export class ProviderRouteModule {
}
