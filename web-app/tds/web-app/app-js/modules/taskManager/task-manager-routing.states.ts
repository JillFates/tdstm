/**
 * Created by Jorge Morayta on 3/15/2017.
 */

import {Ng2StateDeclaration} from 'ui-router-ng2';
import {TaskListComponent} from './components/list/task-list.component';
import {TaskCreateComponent} from './components/create/task-create.component';
import {HeaderComponent} from '../../shared/modules/header/header.component';

/**
 * Task States
 * @class
 * @classdesc Represent the possible states and access on Task Routing
 */
export class TaskStates {
    public static readonly LIST =  {
        name: 'tds.tasklist',
        url:  '/task/list'
    };
    public static readonly CREATE = {
        name: 'tds.taskcreate',
        url: '/task/create'
    };
}

/**
 * This state displays the Task List.
 */
export const taskListState: Ng2StateDeclaration = <Ng2StateDeclaration>{
    name: TaskStates.LIST.name,
    url: TaskStates.LIST.url,
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
    name: TaskStates.CREATE.name,
    url: TaskStates.CREATE.url,
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