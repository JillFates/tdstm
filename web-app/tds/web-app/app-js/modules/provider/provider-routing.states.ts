// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {ProvidersResolveService} from './resolve/providers-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {ProviderListComponent} from './components/list/provider-list.component';

export class ProviderStates {
	public static readonly PROVIDER_LIST = {
		url: 'list'
	};
}

export const DataIngestionRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: ProviderStates.PROVIDER_LIST.url},
	{
		path: ProviderStates.PROVIDER_LIST.url,
		data: {
			page: {
				title: 'PROVIDERS.PROVIDERS',
				instruction: '',
				menu: ['GLOBAL.PROJECT', 'PROVIDERS.PROVIDERS']
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
	imports: [RouterModule.forChild(DataIngestionRoute)]
})

export class ProviderRouteModule {
}