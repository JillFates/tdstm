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
import {APIActionListComponent} from './components/api-action-list/api-action-list.component';

export class DataIngestionStates {
	public static readonly DATA_SCRIPT_LIST = {
		url: '/datascript/list'
	};
	public static readonly API_ACTION_LIST = {
		url: '/action/list'
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
	}

];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(DataIngestionRoute)]
})

export class DataIngestionRouteModule {
}