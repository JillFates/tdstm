// Angular
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
// Resolves
import {ModuleResolveService} from '../../shared/resolves/module.resolve.service';
// Services
import {DataIngestionService} from './service/data-ingestion.service';
import {AuthGuardService} from '../security/services/auth.guard.service';
// Components
import {DataScriptListComponent} from './components/data-script-list/data-script-list.component';
import {ProviderListComponent} from './components/provider-list/provider-list.component';
import {APIActionListComponent} from './components/api-action-list/api-action-list.component';
import {CredentialListComponent} from './components/credential-list/credential-list.component';

export class DataIngestionStates {
	public static readonly DATA_SCRIPT_LIST = {
		url: '/datascript/list'
	};
	public static readonly PROVIDER_LIST = {
		url: '/provider/list'
	};
	public static readonly API_ACTION_LIST = {
		url: '/action/list'
	};
	public static readonly CREDENTIAL_LIST = {
		url: '/credential/list'
	};
}

export const DataIngestionRoute: Routes = [
	// TODO: SPLIT INTO DATA SCRIPT MODULE ONE
	{
		path: DataIngestionStates.DATA_SCRIPT_LIST.url,
		data: {
			page: {
				title: 'DATA_INGESTION.ETL_SCRIPTS',
				instruction: '',
				menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.ETL_SCRIPTS']
			},
			requiresAuth: true,
		},
		component: DataScriptListComponent,
		resolve: [
			{
				token: 'dataScripts',
				policy: {async: 'RXWAIT'},
				deps: [DataIngestionService],
				resolveFn: (service: DataIngestionService) => service.getDataScripts()
			}
		],
		canActivate: [AuthGuardService, ModuleResolveService]
	},
	// TODO: SPLIT INTO PROVIDER MODULE ONE
	{
		path: DataIngestionStates.PROVIDER_LIST.url,
		data: {
			page: {
				title: 'DATA_INGESTION.PROVIDERS',
				instruction: '',
				menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.PROVIDERS']
			},
			requiresAuth: true,
		},
		component: ProviderListComponent,
		resolve: [
			{
				token: 'providers',
				policy: {async: 'RXWAIT'},
				deps: [DataIngestionService],
				resolveFn: (service: DataIngestionService) => service.getProviders()
			}
		],
		canActivate: [AuthGuardService, ModuleResolveService]
	},
	// TODO: SPLIT INTO API ACTIONS MODULE ONE
	{
		path: DataIngestionStates.API_ACTION_LIST.url,
		data: {
			page: {
				title: 'DATA_INGESTION.API_ACTIONS',
				instruction: '',
				menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.API_ACTIONS']
			},
			requiresAuth: true,
		},
		component: APIActionListComponent,
		resolve: [
			{
				token: 'apiActions',
				policy: {async: 'RXWAIT'},
				deps: [DataIngestionService],
				resolveFn: (service: DataIngestionService) => service.getAPIActions()
			}
		],
		canActivate: [AuthGuardService, ModuleResolveService]
	},
	// TODO: SPLIT INTO CREDENTIALS MODULE ONE
	{
		path: DataIngestionStates.CREDENTIAL_LIST.url,
		data: {
			page: {
				title: 'DATA_INGESTION.CREDENTIALS',
				instruction: '',
				menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.CREDENTIALS']
			},
			requiresAuth: true,
		},
		component: CredentialListComponent,
		resolve: [
			{
				token: 'credentials',
				policy: {async: 'RXWAIT'},
				deps: [DataIngestionService],
				resolveFn: (service: DataIngestionService) => service.getCredentials()
			}
		],
		canActivate: [AuthGuardService, ModuleResolveService]
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(DataIngestionRoute)]
})

export class DataIngestionRouteModule {
}