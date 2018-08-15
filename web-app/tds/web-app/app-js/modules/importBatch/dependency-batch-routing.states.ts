import {Ng2StateDeclaration} from '@uirouter/angular';
import {HeaderComponent} from '../../shared/modules/header/header.component';
import {Permission} from '../../shared/model/permission.model';
import {ImportBatchListComponent} from './components/list/import-batch-list.component';

const TOP_MENU_PARENT_SECTION = 'menu-parent-assets';

export class ImportBatchStates {
	public static readonly IMPORT_BATCH_LIST = {
		name: 'tds.importbatch_list',
		url: '/importbatch/list'
	};
}

export const importBatchList: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: ImportBatchStates.IMPORT_BATCH_LIST.name,
	url: ImportBatchStates.IMPORT_BATCH_LIST.url,
	data: {
		page: {
			title: 'IMPORT_BATCH.MANAGE_LIST',
			instruction: '',
			menu: ['IMPORT_BATCH.IMPORT_BATCH', 'IMPORT_BATCH.MANAGE_LIST'],
			topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-assets-manage-dep-batches'}
		},
		requiresAuth: true,
		requiresPermission: Permission.DataTransferBatchView
	},
	views: {
		'headerView@tds': {component: HeaderComponent},
		'containerView@tds': {component: ImportBatchListComponent}
	}
};

export const IMPORT_BATCH_STATES = [
	importBatchList,
];