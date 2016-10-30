/**
 * Created by Jorge Morayta on 11/16/2015.
 */

'use strict';

require('angular');
require('angular-animate');
require('angular-mocks');
require('angular-sanitize');
require('angular-resource');
require('angular-translate');
require('angular-translate-loader-partial');
require('angular-ui-bootstrap');
require('ngClipboard');
require('ui-router');
require('rx-angular');

// Modules
import HTTPModule from '../services/http/HTTPModule.js';
import RestAPIModule from '../services/RestAPI/RestAPIModule.js'
import HeaderModule from '../modules/header/HeaderModule.js';
import LicenseManagerModule from '../modules/licenseManager/LicenseManagerModule.js';
import NoticeManagerModule from '../modules/noticeManager/NoticeManagerModule.js';
import TaskManagerModule from '../modules/taskManager/TaskManagerModule.js';

var ProviderCore = {};

var TDSTM = angular.module('TDSTM', [
    'ngSanitize',
    'ngResource',
    'ngAnimate',
    'pascalprecht.translate', // 'angular-translate'
    'ui.router',
    'ngclipboard',
    'kendo.directives',
    'rx',
    'ui.bootstrap',
    HTTPModule.name,
    RestAPIModule.name,
    HeaderModule.name,
    TaskManagerModule.name,
    LicenseManagerModule.name,
    NoticeManagerModule.name
]).config([
    '$logProvider',
    '$rootScopeProvider',
    '$compileProvider',
    '$controllerProvider',
    '$provide',
    '$httpProvider',
    '$translateProvider',
    '$translatePartialLoaderProvider',
    '$urlRouterProvider',
    '$locationProvider',
    function ($logProvider, $rootScopeProvider, $compileProvider, $controllerProvider, $provide, $httpProvider,
              $translateProvider, $translatePartialLoaderProvider, $urlRouterProvider) {

        $rootScopeProvider.digestTtl(30);

        $logProvider.debugEnabled(true);

        // After bootstrapping angular forget the provider since everything "was already loaded"
        ProviderCore.compileProvider = $compileProvider;
        ProviderCore.controllerProvider = $controllerProvider;
        ProviderCore.provideService = $provide;
        ProviderCore.httpProvider = $httpProvider;

        /**
         * Translations
         */

        $translateProvider.useSanitizeValueStrategy(null);

        $translatePartialLoaderProvider.addPart('tdstm');

        $translateProvider.useLoader('$translatePartialLoader', {
            urlTemplate: '../i18n/{part}/app.i18n-{lang}.json'
        });

        $translateProvider.preferredLanguage('en_US');
        $translateProvider.fallbackLanguage('en_US');

        //$urlRouterProvider.otherwise('dashboard');

    }]).
    run(['$rootScope', '$http', '$log', '$location', function ($rootScope, $http, $log, $location, $state, $stateParams, $locale) {
        $log.debug('Configuration deployed');

        $rootScope.$on('$stateChangeStart', function (event, toState, toParams, fromState, fromParams) {
            $log.debug('State Change to ' + toState.name);
            if (toState.data && toState.data.page) {
                window.document.title = toState.data.page.title;
            }
        });

    }]);

// we mapped the Provider Core list (compileProvider, controllerProvider, provideService, httpProvider) to reuse after on fly
TDSTM.ProviderCore = ProviderCore;

module.exports = TDSTM;