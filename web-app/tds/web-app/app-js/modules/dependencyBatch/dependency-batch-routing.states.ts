import {Ng2StateDeclaration} from '@uirouter/angular';
import {HeaderComponent} from '../../shared/modules/header/header.component';
import {DependencyBatchListComponent} from './components/dependency-batch-list/dependency-batch-list.component';
import {Permission} from '../../shared/model/permission.model';

export class DependencyBatchStates {
	public static readonly DEPENDENCY_BATCH_LIST = {
		name: 'tds.dependencybatch_list',
		url: '/dependencybatch/list'
	};
}

export const dependencyBatchList: Ng2StateDeclaration = <Ng2StateDeclaration>{
	name: DependencyBatchStates.DEPENDENCY_BATCH_LIST.name,
	url: DependencyBatchStates.DEPENDENCY_BATCH_LIST.url,
	data: {
		page: {
			title: 'DEPENDENCY_BATCH.MANAGE_LIST',
			instruction: '',
			menu: ['DEPENDENCY_BATCH.DEPENDENCY_BATCH', 'DEPENDENCY_BATCH.MANAGE_LIST']
		},
		requiresAuth: true,
		requiresPermission: Permission.DataTransferBatchView
	},
	views: {
		'headerView@tds': {component: HeaderComponent},
		'containerView@tds': {component: DependencyBatchListComponent}
	}
};

export const DEPENDENCY_BATCH_STATES = [
	dependencyBatchList,
];