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
require('ngclipboard');
require('ui-router');
require('rx-angular');
require('api-check');
require('angular-formly');
require('angular-formly-templates-bootstrap');

// Modules
import HTTPModule from '../services/http/HTTPModule.js';
import RestAPIModule from '../services/RestAPI/RestAPIModule.js'
import HeaderModule from '../modules/header/HeaderModule.js';
import LicenseAdminModule from '../modules/licenseAdmin/LicenseAdminModule.js';
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
    'formly',
    'formlyBootstrap',
    'ui.bootstrap',
    HTTPModule.name,
    RestAPIModule.name,
    HeaderModule.name,
    TaskManagerModule.name,
    LicenseAdminModule.name,
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
              $translateProvider, $translatePartialLoaderProvider, $urlRouterProvider, $locationProvider) {

        $rootScopeProvider.digestTtl(30);
        // Going back to you
        $locationProvider.html5Mode(true).hashPrefix('!');

        $logProvider.debugEnabled(true);

        // After bootstrapping angular forget the provider since everything "was already loaded"
        ProviderCore.compileProvider = $compileProvider;
        ProviderCore.controllerProvider = $controllerProvider;
        ProviderCore.provideService = $provide;
        ProviderCore.httpProvider = $httpProvider;

        /**
         * Translations
         */

        /*        $translateProvider.useSanitizeValueStrategy(null);

        $translatePartialLoaderProvider.addPart('tdstm');

        $translateProvider.useLoader('$translatePartialLoader', {
            urlTemplate: '../i18n/{part}/app.i18n-{lang}.json'
        });*/

        $translateProvider.preferredLanguage('en_US');
        $translateProvider.fallbackLanguage('en_US');

        //$urlRouterProvider.otherwise('dashboard');

    }]).
    run(['$transitions', '$http', '$log', '$location', '$q','UserPreferencesService', function ($transitions, $http, $log, $location, $q, userPreferencesService) {
        $log.debug('Configuration deployed');

        $transitions.onBefore( {}, ($state, $transition$) => {
            var defer = $q.defer();

            userPreferencesService.getTimeZoneConfiguration(() => {
                defer.resolve();
            });

            return defer.promise;
        });

    }]);

// we mapped the Provider Core list (compileProvider, controllerProvider, provideService, httpProvider) to reuse after on fly
TDSTM.ProviderCore = ProviderCore;

module.exports = TDSTM;