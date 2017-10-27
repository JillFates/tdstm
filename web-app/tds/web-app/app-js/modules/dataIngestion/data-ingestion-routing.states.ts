import { Ng2StateDeclaration } from '@uirouter/angular';

import { DataScriptListComponent } from './components/data-script-list/data-script-list.component';
import { HeaderComponent } from '../../shared/modules/header/header.component';

import { DataIngestionService } from './service/data-ingestion.service';

export class DataIngestionStates {
	public static readonly DATA_SCRIPT_LIST = {
		name: 'tds.dataingestion',
		url: '/datascript/list'
	};
}

export const dataScriptListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: DataIngestionStates.DATA_SCRIPT_LIST.name,
	url: DataIngestionStates.DATA_SCRIPT_LIST.url,
	data: {
		page: {
			title: 'DATA_INGESTION.DATA_SCRIPT',
			instruction: '',
			menu: ['DATA_INGESTION.DATA_INGESTION', 'DATA_INGESTION.DATA_SCRIPT']
		},
		requiresAuth: true,
	},
	views: {
		'headerView@tds': { component: HeaderComponent },
		'containerView@tds': { component: DataScriptListComponent }
	},
	resolve: [
		{
			token: 'dataScripts',
			policy: { async: 'RXWAIT' },
			deps: [DataIngestionService],
			resolveFn: (service: DataIngestionService) => service.getDataScripts()
		}
	]
};

export const DATA_INGESTION_STATES = [
	dataScriptListState
];