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

export class DataScriptStates {
	public static readonly DATA_SCRIPT_LIST = {
		url: 'list'
	};
}

export const DataScriptRoute: Routes = [
	{path: '', pathMatch: 'full', redirectTo: DataScriptStates.DATA_SCRIPT_LIST.url},
	{
		path: DataScriptStates.DATA_SCRIPT_LIST.url,
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
	}
];

@NgModule({
	exports: [RouterModule],
	imports: [RouterModule.forChild(DataScriptRoute)]
})

export class DataScriptRouteModule {
}