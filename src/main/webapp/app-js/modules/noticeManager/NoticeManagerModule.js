/**
 * Created by Jorge Morayta on 10/07/2016.
 */

'use strict';

import angular  from 'angular';
import uiRouter from 'ui-router';

import NoticeList from './list/NoticeList.js';
import NoticeManagerService from './service/NoticeManagerService.js';
import EditNotice from './edit/EditNotice.js';

var NoticeManagerModule = angular.module('TDSTM.NoticeManagerModule', [uiRouter]).config(['$stateProvider',  '$translatePartialLoaderProvider',
    function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('noticeManager');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider
        .state('noticeList', {
            data: {page: {title: 'Notice Administration', instruction: '', menu: ['Admin', 'Notice', 'List']}},
            url: '/notice/list',
            views: {
                'headerView@': header,
                'bodyView@': {
                    templateUrl: '../app-js/modules/noticeManager/list/NoticeList.html',
                    controller: 'NoticeList as noticeList'
                }
            }
        });
}]);

// Services
NoticeManagerModule.service('NoticeManagerService', ['$log', 'RestServiceHandler', NoticeManagerService]);

// Controllers
NoticeManagerModule.controller('NoticeList', ['$log', '$state', 'NoticeManagerService', '$uibModal', NoticeList]);

// Modal - Controllers
NoticeManagerModule.controller('EditNotice', ['$log', 'NoticeManagerService', '$uibModal', '$uibModalInstance', 'params', EditNotice]);

export default NoticeManagerModule;