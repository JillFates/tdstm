/**
 * Created by Jorge Morayta on 3/15/2017.
 */

import {Ng2StateDeclaration} from 'ui-router-ng2';
import {TaskListComponent} from './components/list/task-list.component';
import {TaskCreateComponent} from './components/create/task-create.component';
import {HeaderComponent} from '../../shared/modules/header/header.component';

export class TaskStates {
    public static readonly LIST = 'tds.tasklist';
    public static readonly CREATE = 'tds.taskcreate';
}

/**
 * This state displays the Task List.
 */
export const taskListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
    name: TaskStates.LIST,
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

/**
 * This state displays the Task Creation View
 */
export const taskViewState: Ng2StateDeclaration = <Ng2StateDeclaration>{
    name: TaskStates.CREATE,
    url: '/task/create',
    data: {
        page: {title: 'Create Task', instruction: '', menu: ['Task', 'List', 'Create']},
        requiresAuth: true
    },
    views: {
        'headerView@tds': {component: HeaderComponent},
        'containerView@tds': {component: TaskCreateComponent}
    }
};

export const TASK_MANAGER_STATES = [
    taskListState,
    taskViewState
];