import {Ng2StateDeclaration} from '@uirouter/angular';

import {DataScriptListComponent} from './components/data-script-list/data-script-list.component';
import {ProviderListComponent} from './components/provider-list/provider-list.component';
import {APIActionListComponent} from './components/api-action-list/api-action-list.component';
import {CredentialListComponent} from './components/credential-list/credential-list.component';
import {HeaderComponent} from '../../shared/modules/header/header.component';

import {DataIngestionService} from './service/data-ingestion.service';

export class DataIngestionStates {
	public static readonly DATA_SCRIPT_LIST = {
		name: 'tds.dataingestion_datascript',
		url: '/datascript/list'
	};
	public static readonly PROVIDER_LIST = {
		name: 'tds.dataingestion_provider',
		url: '/provider/list'
	};
	public static readonly API_ACTION_LIST = {
		name: 'tds.dataingestion_apiaciton',
		url: '/action/list'
	};
	public static readonly CREDENTIAL_LIST = {
		name: 'tds.dataingestion_credential',
		url: '/credential/list'
	};
}

export const dataScriptListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: DataIngestionStates.DATA_SCRIPT_LIST.name,
	url: DataIngestionStates.DATA_SCRIPT_LIST.url,
	data: {
		page: {
			title: 'DATA_INGESTION.ETL_SCRIPTS',
			instruction: '',
			menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.ETL_SCRIPTS']
		},
		requiresAuth: true,
	},
	views: {
		'headerView@tds': {component: HeaderComponent},
		'containerView@tds': {component: DataScriptListComponent}
	},
	resolve: [
		{
			token: 'dataScripts',
			policy: {async: 'RXWAIT'},
			deps: [DataIngestionService],
			resolveFn: (service: DataIngestionService) => service.getDataScripts()
		}
	]
};

export const providerListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: DataIngestionStates.PROVIDER_LIST.name,
	url: DataIngestionStates.PROVIDER_LIST.url,
	data: {
		page: {
			title: 'DATA_INGESTION.PROVIDERS',
			instruction: '',
			menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.PROVIDERS']
		},
		requiresAuth: true,
	},
	views: {
		'headerView@tds': {component: HeaderComponent},
		'containerView@tds': {component: ProviderListComponent}
	},
	resolve: [
		{
			token: 'providers',
			policy: {async: 'RXWAIT'},
			deps: [DataIngestionService],
			resolveFn: (service: DataIngestionService) => service.getProviders()
		}
	]
};

export const apiActionListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: DataIngestionStates.API_ACTION_LIST.name,
	url: DataIngestionStates.API_ACTION_LIST.url,
	data: {
		page: {
			title: 'DATA_INGESTION.API_ACTIONS',
			instruction: '',
			menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.API_ACTIONS']
		},
		requiresAuth: true,
	},
	views: {
		'headerView@tds': {component: HeaderComponent},
		'containerView@tds': {component: APIActionListComponent}
	},
	resolve: [
		{
			token: 'apiActions',
			policy: {async: 'RXWAIT'},
			deps: [DataIngestionService],
			resolveFn: (service: DataIngestionService) => service.getAPIActions()
		}
	]
};

export const credentialListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: DataIngestionStates.CREDENTIAL_LIST.name,
	url: DataIngestionStates.CREDENTIAL_LIST.url,
	data: {
		page: {
			title: 'DATA_INGESTION.CREDENTIALS',
			instruction: '',
			menu: ['DATA_INGESTION.PROJECT', 'DATA_INGESTION.CREDENTIALS']
		},
		requiresAuth: true,
	},
	views: {
		'headerView@tds': {component: HeaderComponent},
		'containerView@tds': {component: CredentialListComponent}
	},
	resolve: [
		{
			token: 'credentials',
			policy: {async: 'RXWAIT'},
			deps: [DataIngestionService],
			resolveFn: (service: DataIngestionService) => service.getCredentials()
		}
	]
};

export const DATA_INGESTION_STATES = [
	dataScriptListState,
	providerListState,
	apiActionListState,
	credentialListState
];