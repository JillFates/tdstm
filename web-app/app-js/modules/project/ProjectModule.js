/**
 * Created by Jorge Morayta on 03/14/2016.
 */

'use strict';

import angular  from 'angular';
import uiRouter from 'ui-router';
import ProjectView from './view/ProjectView.js';
import ProjectEdit from './edit/ProjectEdit.js';
import ProjectNew from './new/ProjectNew.js';
import ProjectNewDemo from './new/ProjectNewDemo.js';
import ProjectList from './list/ProjectList.js';
import FieldSettingsView from './fieldSettings/FieldSettingsView.js';
import ManageProjectStaffView from './manageStaff/ManageProjectStaffView.js';
import ActivationEmailView from './activationEmail/ActivationEmailView.js';


var ProjectModule = angular.module('TDSTM.ProjectModule', [uiRouter]).config(['$stateProvider', function ($stateProvider) {

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: './app-js/modules/header/HeaderContainer.html',
        controller: 'HeaderController as header'
    };

    $stateProvider
        .state('projectView', {
            data: {page: {title: 'PROJECT_DETAILS', instruction: '', menu: ['PROJECT', 'DETAILS']}},
            url: '/project/view/:id?status=created',
            views: {
                'headerView@': header,
                'bodyView@': {
                    templateUrl: './app-js/modules/project/view/ProjectView.html',
                    controller: 'ProjectView as projectView'
                }
            }
        }).state('projectEdit', {
        data: {page: {title: 'EDIT_PROJECT', instruction: '', menu: ['PROJECT', 'EDIT']}},
        url: '/project/edit/:id?status',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: './app-js/modules/project/edit/ProjectEdit.html',
                controller: 'ProjectEdit as projectEdit'
            }
        }
    }).state('projectNew', {
        data: {page: {title: 'CREATE_PROJECT', instruction: '', menu: ['PROJECT', 'CREATE']}},
        url: '/project/new?status',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: './app-js/modules/project/new/ProjectNew.html',
                controller: 'ProjectNew as projectNew'
            }
        }
    }).state('projectDemo', {
        data: {page: {title: 'CREATE_DEMO_PROJECT', instruction: '', menu: ['PROJECT', 'DEMO']}},
        url: '/project/demo',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: './app-js/modules/project/new/ProjectNewDemo.html',
                controller: 'ProjectNewDemo as projectNewDemo'
            }
        }
    }).state('projectList', {
        data: {page: {title: 'PROJECT_LIST', instruction: 'ACTIVE', menu: ['PROJECT', 'LIST', 'ACTIVE']}},
        url: '/project/list?status&project',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: './app-js/modules/project/list/ProjectList.html',
                controller: 'ProjectList as projectList'
            }
        }
    }).state('fieldSettings', {
        data: {page: {title: 'PROJECT_FIELD_SETTINGS', instruction: '', menu: ['PROJECT', 'FIELD', 'SETTINGS']}},
        url: '/project/field/:id/settings?status',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: './app-js/modules/project/fieldSettings/FieldSettingsView.html',
                controller: 'FieldSettingsView as fieldSettingsView'
            }
        }
    }).state('manageProjectStaff', {
        data: {page: {title: 'PROJECT_STAFF', instruction: '', menu: ['PROJECT', 'MANAGE', 'STAFF']}},
        url: '/project/manage/staff?status',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: './app-js/modules/project/manageStaff/ManageProjectStaffView.html',
                controller: 'ManageProjectStaffView as manageProjectStaffView'
            }
        }
    }).state('activationEmail', {
        data: {page: {title: 'USER_ACTIVATION_EMAILS', instruction: '', menu: ['PROJECT', 'USER', 'ACTIVATION', 'EMAIL']}},
        url: '/project/user/activation/email?status',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: './app-js/modules/project/activationEmail/ActivationEmailView.html',
                controller: 'ActivationEmailView as activationEmailView'
            }
        }
    });
}]);

// Controllers
ProjectModule.controller('ProjectView', ['$log', '$state', ProjectView]);
ProjectModule.controller('ProjectEdit', ['$log', '$state', ProjectEdit]);
ProjectModule.controller('ProjectNew', ['$log', '$state', ProjectNew]);
ProjectModule.controller('ProjectNewDemo', ['$log', ProjectNewDemo]);
ProjectModule.controller('ProjectList', ['$log', '$state', ProjectList]);
ProjectModule.controller('FieldSettingsView', ['$log', '$state', FieldSettingsView]);
ProjectModule.controller('ManageProjectStaffView', ['$log', '$state', ManageProjectStaffView]);
ProjectModule.controller('ActivationEmailView', ['$log', '$state', ActivationEmailView]);


export default ProjectModule;