/**
 * Created by Jorge Morayta on 3/15/2017.
 */

import {Ng2StateDeclaration} from 'ui-router-ng2';
import {TaskListComponent} from './components/list/task-list.component';
import {HeaderComponent} from '../../shared/modules/header/header.component';

/**
 * This state displays the notice list.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The notice are fetched using a resolve.
 */
export const noticeListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
    name: 'tds.tasklist',
    url: '/task/list',
    data: {
        page: {title: 'Task Manager', instruction: '', menu: ['Task', 'List']},
        requiresAuth: true
    },
    views: {
        'headerView@tds': {component: HeaderComponent},
        'containerView@tds': {component: TaskListComponent}
    }
};

export const TASK_MANAGER_STATES = [
    noticeListState
];