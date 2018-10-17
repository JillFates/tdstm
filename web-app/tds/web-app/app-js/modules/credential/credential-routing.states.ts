// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
import {CredentialResolveService} from './resolve/credential-resolve.service';
// Services
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {CredentialListComponent} from './components/list/credential-list.component';

export class CredentialStates {
	public static readonly CREDENTIAL_LIST = {
		url: 'list'
	};
}

export const CredentialRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: CredentialStates.CREDENTIAL_LIST.url},
	{
		path: CredentialStates.CREDENTIAL_LIST.url,
		data: {
			page: {
				title: 'DATA_INGESTION.CREDENTIALS',
				instruction: '',
				menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.CREDENTIALS']
			},
			requiresAuth: true,
		},
		component: CredentialListComponent,
		resolve: {
			credentials: CredentialResolveService
		},
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(CredentialRoute)]
})

export class CredentialRouteModule {
}