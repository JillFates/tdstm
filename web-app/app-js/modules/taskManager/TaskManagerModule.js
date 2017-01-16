/**
 * Created by Jorge Morayta on 12/22/2015.
 */

'use strict';

import angular  from 'angular';
import uiRouter from 'ui-router';

import TaskManagerService from './service/TaskManagerService.js';
import TaskManagerController from './list/TaskManagerController.js';
import TaskManagerEdit from './edit/TaskManagerEdit.js';

var TaskManagerModule = angular.module('TDSTM.TaskManagerModule', [uiRouter]).config(['$stateProvider', 'formlyConfigProvider',
    function ($stateProvider, formlyConfigProvider) {

    formlyConfigProvider.setType({
        name: 'custom',
        templateUrl: 'custom.html'
    });

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider
        .state('taskList', {
            data: {page: {title: 'My Task Manager', instruction: '', menu: ['Task Manager']}},
            url: '/task/list',
            views: {
                'headerView@': header,
                'bodyView@': {
                    templateUrl: '../app-js/modules/taskManager/list/TaskManagerContainer.html',
                    controller: 'TaskManagerController as taskManager'
                }
            }
        });
}]);

// Services
TaskManagerModule.service('taskManagerService', ['$log', 'RestServiceHandler', TaskManagerService]);

// Controllers
TaskManagerModule.controller('TaskManagerController', ['$log', 'taskManagerService', '$uibModal', TaskManagerController]);
TaskManagerModule.controller('TaskManagerEdit', ['$log', TaskManagerEdit]);


export default TaskManagerModule;