(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
'use strict';

/**
 * Created by Jorge Morayta on 11/20/2015.
 * TDSM is a global object that comes from App.js
 *
 * The following helper works in a way to make available the creation of Directive, Services and Controller
 * on fly or when deploying the app.
 *
 * We reduce the use of compile and extra steps
 */

var TDSTM = require('./App.js');

/**
 * Listen to an existing digest of the compile provider and execute the $apply immediately or after it's ready
 * @param current
 * @param fn
 */
TDSTM.safeApply = function (current, fn) {
    'use strict';

    var phase = current.$root.$$phase;
    if (phase === '$apply' || phase === '$digest') {
        if (fn) {
            current.$eval(fn);
        }
    } else {
        if (fn) {
            current.$apply(fn);
        } else {
            current.$apply();
        }
    }
};

/**
 * Helper to inject directive async if the compileProvider is available
 * @param setting
 * @param args
 */
TDSTM.createDirective = function (setting, args) {
    'use strict';

    if (TDSTM.ProviderCore.compileProvider) {
        TDSTM.ProviderCore.compileProvider.directive(setting, args);
    } else if (TDSTM.directive) {
        TDSTM.directive(setting, args);
    }
};

/**
 * Helper to inject controllers async if the controllerProvider is available
 * @param setting
 * @param args
 */
TDSTM.createController = function (setting, args) {
    'use strict';

    if (TDSTM.ProviderCore.controllerProvider) {
        TDSTM.controllerProvider.register(setting, args);
    } else if (TDSTM.controller) {
        TDSTM.controller(setting, args);
    }
};

/**
 * Helper to inject service async if the provideService is available
 * @param setting
 * @param args
 */
TDSTM.createService = function (setting, args) {
    'use strict';

    if (TDSTM.ProviderCore.provideService) {
        TDSTM.ProviderCore.provideService.service(setting, args);
    } else if (TDSTM.controller) {
        TDSTM.service(setting, args);
    }
};

/**
 * For Legacy system, what is does is to take params from the query
 * outside the AngularJS ui-routing.
 * @param param // Param to searc for /example.html?bar=foo#currentState
 */
TDSTM.getURLParam = function (param) {
    'use strict';

    $.urlParam = function (name) {
        var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
        if (results === null) {
            return null;
        } else {
            return results[1] || 0;
        }
    };

    return $.urlParam(param);
};

/**
 * This code was introduced only for the iframe migration
 * it detect when mouse enter
 */
TDSTM.iframeLoader = function () {
    'use strict';

    $('.iframeLoader').hover(function () {
        $('.navbar-ul-container .dropdown.open').removeClass('open');
    }, function () {});
};

// It will be removed after we rip off all iframes
window.TDSTM = TDSTM;

},{"./App.js":2}],2:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 11/16/2015.
 */

'use strict';

var _HTTPModule = require('../services/http/HTTPModule.js');

var _HTTPModule2 = _interopRequireDefault(_HTTPModule);

var _RestAPIModule = require('../services/RestAPI/RestAPIModule.js');

var _RestAPIModule2 = _interopRequireDefault(_RestAPIModule);

var _HeaderModule = require('../modules/header/HeaderModule.js');

var _HeaderModule2 = _interopRequireDefault(_HeaderModule);

var _LicenseManagerModule = require('../modules/licenseManager/LicenseManagerModule.js');

var _LicenseManagerModule2 = _interopRequireDefault(_LicenseManagerModule);

var _NoticeManagerModule = require('../modules/noticeManager/NoticeManagerModule.js');

var _NoticeManagerModule2 = _interopRequireDefault(_NoticeManagerModule);

var _TaskManagerModule = require('../modules/taskManager/TaskManagerModule.js');

var _TaskManagerModule2 = _interopRequireDefault(_TaskManagerModule);

function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : { default: obj };
}

require('angular');
require('angular-animate');
require('angular-mocks');
require('angular-sanitize');
require('angular-resource');
require('angular-translate');
require('angular-translate-loader-partial');
require('angular-ui-bootstrap');
require('ui-router');
require('rx-angular');

// Modules


var ProviderCore = {};

var TDSTM = angular.module('TDSTM', ['ngSanitize', 'ngResource', 'ngAnimate', 'pascalprecht.translate', // 'angular-translate'
'ui.router', 'kendo.directives', 'rx', 'ui.bootstrap', _HTTPModule2.default.name, _RestAPIModule2.default.name, _HeaderModule2.default.name, _TaskManagerModule2.default.name, _LicenseManagerModule2.default.name, _NoticeManagerModule2.default.name]).config(['$logProvider', '$rootScopeProvider', '$compileProvider', '$controllerProvider', '$provide', '$httpProvider', '$translateProvider', '$translatePartialLoaderProvider', '$urlRouterProvider', '$locationProvider', function ($logProvider, $rootScopeProvider, $compileProvider, $controllerProvider, $provide, $httpProvider, $translateProvider, $translatePartialLoaderProvider, $urlRouterProvider) {

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
}]).run(['$rootScope', '$http', '$log', '$location', function ($rootScope, $http, $log, $location, $state, $stateParams, $locale) {
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

},{"../modules/header/HeaderModule.js":8,"../modules/licenseManager/LicenseManagerModule.js":9,"../modules/noticeManager/NoticeManagerModule.js":15,"../modules/taskManager/TaskManagerModule.js":19,"../services/RestAPI/RestAPIModule.js":24,"../services/http/HTTPModule.js":27,"angular":"angular","angular-animate":"angular-animate","angular-mocks":"angular-mocks","angular-resource":"angular-resource","angular-sanitize":"angular-sanitize","angular-translate":"angular-translate","angular-translate-loader-partial":"angular-translate-loader-partial","angular-ui-bootstrap":"angular-ui-bootstrap","rx-angular":"rx-angular","ui-router":"ui-router"}],3:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 1/17/15.
 * When Angular invokes the link function, it is no longer in the context of the class instance, and therefore all this will be undefined
 * This structure will avoid those issues.
 */

'use strict';

// Controller is being used to Inject all Dependencies

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var SVGLoaderController = function SVGLoaderController($log) {
    _classCallCheck(this, SVGLoaderController);

    this.$log = $log;
};

SVGLoaderController.$inject = ['$log'];

// Directive

var SVGLoader = function () {
    function SVGLoader() {
        _classCallCheck(this, SVGLoader);

        this.restrict = 'E';
        this.controller = SVGLoaderController;
        this.scope = {
            svgData: '='
        };
    }

    _createClass(SVGLoader, [{
        key: 'link',
        value: function link(scope, element, attrs, ctrl) {
            element.html(scope.svgData);
        }
    }], [{
        key: 'directive',
        value: function directive() {
            return new SVGLoader();
        }
    }]);

    return SVGLoader;
}();

exports.default = SVGLoader.directive;

},{}],4:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/14/2015.
 * Prints out all Toast message when detected from server or custom msg using the directive itself
 *
 * Probably values are:
 *
 * success, danger, info, warning
 *
 */
'use strict';

var TDSTM = require('../../config/App.js');

TDSTM.createDirective('toastHandler', ['$log', '$timeout', 'HTTPRequestHandlerInterceptor', 'HTTPRequestErrorHandlerInterceptor', 'HTTPResponseHandlerInterceptor', 'HTTPResponseErrorHandlerInterceptor', function ($log, $timeout, HTTPRequestHandlerInterceptor, HTTPRequestErrorHandlerInterceptor, HTTPResponseHandlerInterceptor, HTTPResponseErrorHandlerInterceptor) {

    $log.debug('ToastHandler loaded');
    return {
        scope: {
            msg: '=',
            type: '=',
            status: '='
        },
        priority: 5,
        templateUrl: '../app-js/directives/Tools/ToastHandler.html',
        restrict: 'E',
        controller: ['$scope', function ($scope) {
            $scope.alert = {
                success: {
                    show: false,
                    status: '',
                    statusText: ''
                },
                danger: {
                    show: false,
                    status: '',
                    statusText: ''
                },
                info: {
                    show: false,
                    status: '',
                    statusText: ''
                },
                warning: {
                    show: false,
                    status: '',
                    statusText: ''
                }
            };

            $scope.progress = {
                show: false
            };

            function turnOffNotifications() {
                $scope.alert.success.show = false;
                $scope.alert.danger.show = false;
                $scope.alert.info.show = false;
                $scope.alert.warning.show = false;
                $scope.progress.show = false;
            }

            /**
             * Listen to any request, we can register listener if we want to add extra code.
             */
            HTTPRequestHandlerInterceptor.listenRequest().then(null, null, function (config) {
                $log.debug('Request to: ', config);
                var time = config.requestTimestamp;
                $log.debug(time);
                $scope.progress.show = true;
            });

            HTTPRequestErrorHandlerInterceptor.listenError().then(null, null, function (rejection) {
                $log.debug('Request error: ', rejection);
                $scope.progress.show = false;
            });

            HTTPResponseHandlerInterceptor.listenResponse().then(null, null, function (response) {
                var time = response.config.responseTimestamp - response.config.requestTimestamp;
                $log.debug('The request took ' + time / 1000 + ' seconds');
                $log.debug('Response result: ', response);
                $scope.progress.show = false;
            });

            HTTPResponseErrorHandlerInterceptor.listenError().then(null, null, function (rejection) {
                $log.debug('Response error: ', rejection);
                $scope.progress.show = false;
                $scope.alert.danger.show = true;
                $scope.alert.danger.status = rejection.status;
                $scope.alert.danger.statusText = rejection.statusText;
                $timeout(turnOffNotifications, 3000);
            });

            /**
             * Hide the Pop up notification manually
             */
            $scope.onCancelPopUp = function () {
                turnOffNotifications();
            };

            /**
             * It watch the value to show the msg if necessary
             */
            $scope.$watch('msg', function (newValue, oldValue) {
                if (newValue && newValue !== '') {
                    $scope.alert[$scope.type].show = true;
                    $scope.alert[$scope.type].statusText = newValue;
                    $scope.alert[$scope.type].status = $scope.status;
                    $timeout(turnOffNotifications, 2500);
                }
            });
        }]
    };
}]);

},{"../../config/App.js":2}],5:[function(require,module,exports){
'use strict';

/**
 * Created by Jorge Morayta on 12/14/2015.
 * It handler the index for any of the directives available
 */

require('./Tools/ToastHandler.js');

},{"./Tools/ToastHandler.js":4}],6:[function(require,module,exports){
'use strict';

/**
 * Created by Jorge Morayta on 11/17/2015.
 */

// Main AngularJs configuration

require('./config/App.js');

// Helpers
require('./config/AngularProviderHelper.js');

// Directives
require('./directives/index');

},{"./config/AngularProviderHelper.js":1,"./config/App.js":2,"./directives/index":5}],7:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/2/2015.
 * Header Controller manage the view available on the state.data
 * ----------------------
 * Header Controller
 * Page title                      Home -> Layout - Sub Layout
 *
 * Module Controller
 * Content
 * --------------------
 *
 */
'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var HeaderController = function () {
    function HeaderController($log, $state) {
        _classCallCheck(this, HeaderController);

        this.log = $log;
        this.state = $state;

        this.pageMetaData = {
            title: '',
            instruction: '',
            menu: []
        };

        this.prepareHeader();
        this.log.debug('Header Controller Instanced');
    }

    /**
     * Verify if we have a menu to show to made it available to the View
     */

    _createClass(HeaderController, [{
        key: 'prepareHeader',
        value: function prepareHeader() {
            if (this.state && this.state.$current && this.state.$current.data) {
                this.pageMetaData = this.state.$current.data.page;
                document.title = this.pageMetaData.title;
            }
        }
    }]);

    return HeaderController;
}();

exports.default = HeaderController;

},{}],8:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/21/2015.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _angular = require('angular');

var _angular2 = _interopRequireDefault(_angular);

var _HeaderController = require('./HeaderController.js');

var _HeaderController2 = _interopRequireDefault(_HeaderController);

function _interopRequireDefault(obj) {
  return obj && obj.__esModule ? obj : { default: obj };
}

var HeaderModule = _angular2.default.module('TDSTM.HeaderModule', []);

HeaderModule.controller('HeaderController', ['$log', '$state', _HeaderController2.default]);

exports.default = HeaderModule;

},{"./HeaderController.js":7,"angular":"angular"}],9:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/25/2016.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _angular = require('angular');

var _angular2 = _interopRequireDefault(_angular);

var _uiRouter = require('ui-router');

var _uiRouter2 = _interopRequireDefault(_uiRouter);

var _LicenseList = require('./list/LicenseList.js');

var _LicenseList2 = _interopRequireDefault(_LicenseList);

var _LicenseManagerService = require('./service/LicenseManagerService.js');

var _LicenseManagerService2 = _interopRequireDefault(_LicenseManagerService);

var _RequestLicense = require('./request/RequestLicense.js');

var _RequestLicense2 = _interopRequireDefault(_RequestLicense);

var _CreatedLicense = require('./created/CreatedLicense.js');

var _CreatedLicense2 = _interopRequireDefault(_CreatedLicense);

var _LicenseDetail = require('./detail/LicenseDetail.js');

var _LicenseDetail2 = _interopRequireDefault(_LicenseDetail);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

var LicenceManagerModule = _angular2.default.module('TDSTM.LicenseManagerModule', [_uiRouter2.default]).config(['$stateProvider', '$translatePartialLoaderProvider', function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('licenseManager');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider.state('licenseList', {
        data: { page: { title: 'Administer Licenses', instruction: '', menu: ['ADMIN', 'LICENSE', 'LIST'] } },
        url: '/license/list',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: '../app-js/modules/licenseManager/list/LicenseList.html',
                controller: 'LicenseList as licenseList'
            }
        }
    });
}]);

// Services
LicenceManagerModule.service('LicenseManagerService', ['$log', 'RestServiceHandler', _LicenseManagerService2.default]);

// Controllers
LicenceManagerModule.controller('LicenseList', ['$log', '$state', 'LicenseManagerService', '$uibModal', _LicenseList2.default]);

// Modal - Controllers
LicenceManagerModule.controller('RequestLicense', ['$log', 'LicenseManagerService', '$uibModalInstance', 'params', _RequestLicense2.default]);
LicenceManagerModule.controller('CreatedLicense', ['$log', '$uibModalInstance', 'params', _CreatedLicense2.default]);
LicenceManagerModule.controller('LicenseDetail', ['$log', 'LicenseManagerService', '$uibModalInstance', '$uibModal', _LicenseDetail2.default]);

exports.default = LicenceManagerModule;

},{"./created/CreatedLicense.js":10,"./detail/LicenseDetail.js":11,"./list/LicenseList.js":12,"./request/RequestLicense.js":13,"./service/LicenseManagerService.js":14,"angular":"angular","ui-router":"ui-router"}],10:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var CreatedRequestLicenseController = function () {
    function CreatedRequestLicenseController($log, $uibModalInstance, params) {
        _classCallCheck(this, CreatedRequestLicenseController);

        this.uibModalInstance = $uibModalInstance;
        this.client = params;
    }

    /**
     * Dismiss the dialog, no action necessary
     */

    _createClass(CreatedRequestLicenseController, [{
        key: 'cancelCloseDialog',
        value: function cancelCloseDialog() {
            this.uibModalInstance.dismiss('cancel');
        }
    }]);

    return CreatedRequestLicenseController;
}();

exports.default = CreatedRequestLicenseController;

},{}],11:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var LicenseDetail = function () {
    function LicenseDetail($log, licenseManagerService, $uibModalInstance, params) {
        _classCallCheck(this, LicenseDetail);

        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;
        this.LicenseModel = {};
    }

    /**
     * Dismiss the dialog, no action necessary
     */

    _createClass(LicenseDetail, [{
        key: 'cancelCloseDialog',
        value: function cancelCloseDialog() {
            this.uibModalInstance.dismiss('cancel');
        }
    }]);

    return LicenseDetail;
}();

exports.default = LicenseDetail;

},{}],12:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/25/2016.
 */
'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var LicenseList = function () {
    function LicenseList($log, $state, licenseManagerService, $uibModal) {
        _classCallCheck(this, LicenseList);

        this.log = $log;
        this.state = $state;
        this.licenseGridOptions = {};
        this.licenseManagerService = licenseManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        this.getLicenseList();
        this.log.debug('LicenseList Instanced');
    }

    _createClass(LicenseList, [{
        key: 'getDataSource',
        value: function getDataSource() {
            var _this = this;

            this.licenseGridOptions = {
                toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseList.onRequestNewLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Request New License</button> <div onclick="loadGridBundleList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                pageable: {
                    refresh: true,
                    pageSizes: true,
                    buttonCount: 5
                },
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseList.onLicenseDetails(#=licenseId#)"><span class="glyphicon glyphicon-edit"></span></button>' }, { field: 'client', title: 'Client' }, { field: 'project', title: 'Project' }, { field: 'contact_email', title: 'Contact Email' }, { field: 'status', title: 'Status' }, { field: 'type', title: 'Type' }, { field: 'method', title: 'Method' }, { field: 'servers_tokens', title: 'Server/Tokens' }, { field: 'inception', title: 'Inception' }, { field: 'expiration', title: 'Expiration' }, { field: 'environment', title: 'Env.' }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            _this.licenseManagerService.testService(function (data) {
                                e.success(data);
                            });
                        }
                    }
                }
            };
        }

        /**
         * Open a dialog with the Basic Form to request a New License
         */

    }, {
        key: 'onRequestNewLicense',
        value: function onRequestNewLicense() {
            var _this2 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseManager/request/RequestLicense.html',
                controller: 'RequestLicense as requestLicense',
                size: 'md',
                resolve: {
                    params: function params() {
                        return { id: 50, name: 'Acme, Inc.', email: 'acme@inc.com' };
                    }
                }
            });

            modalInstance.result.then(function (license) {
                _this2.log.info('New License Created: ', license);
                _this2.onNewLicenseCreated();
            }, function () {
                _this2.log.info('Request Canceled.');
            });
        }

        /**
         * After clicking on edit, we redirect the user to the Edition screen instead of open a dialog
         * du the size of the inputs
         */

    }, {
        key: 'onLicenseDetails',
        value: function onLicenseDetails(licenseId) {
            var _this3 = this;

            this.log.info('Open Details for: ', licenseId);
            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseManager/detail/LicenseDetail.html',
                controller: 'LicenseDetail as licenseDetail',
                size: 'lg',
                resolve: {
                    params: function params() {
                        return { licenseId: licenseId };
                    }
                }
            });

            modalInstance.result.then(function () {}, function () {
                _this3.log.info('Request Canceled.');
            });
        }
    }, {
        key: 'onNewLicenseCreated',
        value: function onNewLicenseCreated() {
            this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseManager/created/CreatedLicense.html',
                size: 'md',
                controller: 'CreatedLicense as createdLicense',
                resolve: {
                    params: function params() {
                        return { id: 50, name: 'Acme, Inc.', email: 'acme@inc.com' };
                    }
                }
            });
        }
    }, {
        key: 'getLicenseList',
        value: function getLicenseList() {
            var _this4 = this;

            this.licenseManagerService.getLicenseList(function (data) {
                _this4.log.info(data);
            });
        }
    }]);

    return LicenseList;
}();

exports.default = LicenseList;

},{}],13:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var RequestLicense = function () {
    function RequestLicense($log, licenseManagerService, $uibModalInstance, params) {
        _classCallCheck(this, RequestLicense);

        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;
        this.getEnvironmentDataSource();
        this.getProjectDataSource();
        this.newLicenseModel = {
            contactEmail: '',
            environmentId: null,
            projectId: null,
            client: params,
            specialInstructions: ''
        };
    }

    /**
     * Populate the Environment dropdown values
     */

    _createClass(RequestLicense, [{
        key: 'getEnvironmentDataSource',
        value: function getEnvironmentDataSource() {
            this.environmentDataSource = [{ environmentId: 1, name: 'Production' }, { environmentId: 2, name: 'Demo' }];
        }

        /**
         * Populate the Project dropdown values
         */

    }, {
        key: 'getProjectDataSource',
        value: function getProjectDataSource() {
            this.projectDataSource = [{ projectId: 1, name: 'n/a' }, { projectId: 2, name: 'DR Relo' }];
        }

        /**
         * Execute the Service call to generate a new License request
         */

    }, {
        key: 'saveLicenseRequest',
        value: function saveLicenseRequest() {
            var _this = this;

            this.log.info('New License Requested: ', this.newLicenseModel);
            this.licenseManagerService.createNewLicenseRequest(this.newLicenseModel, function (data) {
                _this.uibModalInstance.close(data);
            });
        }

        /**
         * Dismiss the dialog, no action necessary
         */

    }, {
        key: 'cancelCloseDialog',
        value: function cancelCloseDialog() {
            this.uibModalInstance.dismiss('cancel');
        }
    }]);

    return RequestLicense;
}();

exports.default = RequestLicense;

},{}],14:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/26/16.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var LicenseManagerService = function () {
    function LicenseManagerService($log, restServiceHandler) {
        _classCallCheck(this, LicenseManagerService);

        this.log = $log;
        this.restService = restServiceHandler;

        this.log.debug('LicenseManagerService Instanced');
    }

    _createClass(LicenseManagerService, [{
        key: 'testService',
        value: function testService(callback) {
            this.restService.licenseManagerServiceHandler().getLicense(function (data) {
                return callback(data);
            });
        }
    }, {
        key: 'getLicenseList',
        value: function getLicenseList(callback) {
            this.restService.licenseManagerServiceHandler().getLicenseList(function (data) {
                return callback(data);
            });
        }
    }, {
        key: 'createNewLicenseRequest',
        value: function createNewLicenseRequest(newLicense, callback) {
            // Process New License data if necessary (add, remove, etc)
            this.restService.licenseManagerServiceHandler().createNewLicenseRequest(newLicense, function (data) {
                return callback(data);
            });
        }
    }]);

    return LicenseManagerService;
}();

exports.default = LicenseManagerService;

},{}],15:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 10/07/2016.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _angular = require('angular');

var _angular2 = _interopRequireDefault(_angular);

var _uiRouter = require('ui-router');

var _uiRouter2 = _interopRequireDefault(_uiRouter);

var _NoticeList = require('./list/NoticeList.js');

var _NoticeList2 = _interopRequireDefault(_NoticeList);

var _NoticeManagerService = require('./service/NoticeManagerService.js');

var _NoticeManagerService2 = _interopRequireDefault(_NoticeManagerService);

var _EditNotice = require('./edit/EditNotice.js');

var _EditNotice2 = _interopRequireDefault(_EditNotice);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

var NoticeManagerModule = _angular2.default.module('TDSTM.NoticeManagerModule', [_uiRouter2.default]).config(['$stateProvider', '$translatePartialLoaderProvider', function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('noticeManager');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider.state('noticeList', {
        data: { page: { title: 'Notice Administration', instruction: '', menu: ['ADMIN', 'NOTICE', 'LIST'] } },
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
NoticeManagerModule.service('NoticeManagerService', ['$log', 'RestServiceHandler', _NoticeManagerService2.default]);

// Controllers
NoticeManagerModule.controller('NoticeList', ['$log', '$state', 'NoticeManagerService', '$uibModal', _NoticeList2.default]);

// Modal - Controllers
NoticeManagerModule.controller('EditNotice', ['$log', 'NoticeManagerService', '$uibModalInstance', 'params', _EditNotice2.default]);

exports.default = NoticeManagerModule;

},{"./edit/EditNotice.js":16,"./list/NoticeList.js":17,"./service/NoticeManagerService.js":18,"angular":"angular","ui-router":"ui-router"}],16:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 10/07/2016.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var EditNotice = function () {
    function EditNotice($log, noticeManagerService, $uibModalInstance, params) {
        _classCallCheck(this, EditNotice);

        this.noticeManagerService = noticeManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;

        this.action = params.action;
        this.actionType = params.actionType;

        this.kendoEditorTools = ['formatting', 'cleanFormatting', 'fontName', 'fontSize', 'justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull', 'bold', 'italic', 'viewHtml'];
        this.getTypeDataSource();
        this.editModel = {
            title: '',
            typeId: null,
            active: false,
            noticeBody: ''
        };

        if (params.notice) {
            this.editModel = params.notice;
        }
    }

    /**
     * Populate the Environment dropdown values
     */

    _createClass(EditNotice, [{
        key: 'getTypeDataSource',
        value: function getTypeDataSource() {
            this.typeDataSource = [{ typeId: 1, name: 'Prelogin' }, { typeId: 2, name: 'Postlogin' }];
        }

        /**
         * Execute the Service call to generate a new License request
         */

    }, {
        key: 'saveNotice',
        value: function saveNotice() {
            var _this = this;

            this.log.info(this.action + ' Notice Requested: ', this.editModel);

            if (this.action === this.actionType.NEW) {
                this.noticeManagerService.createNotice(this.editModel, function (data) {
                    _this.uibModalInstance.close(data);
                });
            } else if (this.action === this.actionType.EDIT) {
                this.noticeManagerService.editNotice(this.editModel, function (data) {
                    _this.uibModalInstance.close(data);
                });
            }
        }

        /**
         * Dismiss the dialog, no action necessary
         */

    }, {
        key: 'cancelCloseDialog',
        value: function cancelCloseDialog() {
            this.uibModalInstance.dismiss('cancel');
        }
    }]);

    return EditNotice;
}();

exports.default = EditNotice;

},{}],17:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 10/07/2016.
 */
'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var NoticeList = function () {
    function NoticeList($log, $state, noticeManagerService, $uibModal) {
        _classCallCheck(this, NoticeList);

        this.log = $log;
        this.state = $state;

        this.actionType = {
            NEW: 'New',
            EDIT: 'Edit'
        };
        this.noticeGridOptions = {};
        this.noticeManagerService = noticeManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        this.getLicenseList();
        this.log.debug('LicenseList Instanced');
    }

    _createClass(NoticeList, [{
        key: 'getDataSource',
        value: function getDataSource() {
            var _this = this;

            this.noticeGridOptions = {
                toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="noticeList.onEditCreateNotice(noticeList.actionType.NEW)"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Create New Notice</button> <div onclick="loadGridBundleList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                pageable: {
                    refresh: true,
                    pageSizes: true,
                    buttonCount: 5
                },
                columns: [{ field: 'noticeId', hidden: true }, { field: 'rawText', hidden: true }, { field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="noticeList.onEditCreateNotice(noticeList.actionType.EDIT, this)"><span class="glyphicon glyphicon-edit"></span></button>' }, { field: 'title', title: 'Title' }, { field: 'type', title: 'Project' }, { field: 'active', title: 'Active' }, { field: 'project', title: 'project' }, { field: 'type', title: 'Type' }, { field: 'acknowledge', title: 'Acknowledge' }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            _this.noticeManagerService.testService(function (data) {
                                e.success(data);
                            });
                        }
                    }
                }
            };
        }

        /**
         * Open a dialog with the Basic Form to request a New Notice
         */

    }, {
        key: 'onEditCreateNotice',
        value: function onEditCreateNotice(action, notice) {
            var _this2 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/noticeManager/edit/EditNotice.html',
                controller: 'EditNotice as editNotice',
                size: 'md',
                resolve: {
                    params: function params() {
                        var dataItem = notice && notice.dataItem;
                        return { action: action, notice: notice.dataItem, actionType: _this2.actionType };
                    }
                }
            });

            modalInstance.result.then(function (license) {
                _this2.log.info(action + ' Notice: ', license);
            }, function () {
                _this2.log.info(action + ' Request Canceled.');
            });
        }
    }, {
        key: 'getLicenseList',
        value: function getLicenseList() {
            var _this3 = this;

            this.noticeManagerService.getNoticeList(function (data) {
                _this3.log.info(data);
            });
        }
    }]);

    return NoticeList;
}();

exports.default = NoticeList;

},{}],18:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 10/07/16.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var NoticeManagerService = function () {
    function NoticeManagerService($log, restServiceHandler) {
        _classCallCheck(this, NoticeManagerService);

        this.log = $log;
        this.restService = restServiceHandler;

        this.log.debug('NoticeManagerService Instanced');
    }

    _createClass(NoticeManagerService, [{
        key: 'testService',
        value: function testService(callback) {
            this.restService.noticeManagerServiceHandler().getNoticeMockUp(function (data) {
                return callback(data);
            });
        }
    }, {
        key: 'getNoticeList',
        value: function getNoticeList(callback) {
            this.restService.noticeManagerServiceHandler().getNoticeList(function (data) {
                return callback(data);
            });
        }
    }, {
        key: 'createNotice',
        value: function createNotice(notice, callback) {
            // Process New License data if necessary (add, remove, etc)
            this.restService.noticeManagerServiceHandler().createNotice(notice, function (data) {
                return callback(data);
            });
        }
    }, {
        key: 'editNotice',
        value: function editNotice(notice, callback) {
            // Process New License data if necessary (add, remove, etc)
            this.restService.noticeManagerServiceHandler().editNotice(notice, function (data) {
                return callback(data);
            });
        }
    }]);

    return NoticeManagerService;
}();

exports.default = NoticeManagerService;

},{}],19:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/22/2015.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _angular = require('angular');

var _angular2 = _interopRequireDefault(_angular);

var _uiRouter = require('ui-router');

var _uiRouter2 = _interopRequireDefault(_uiRouter);

var _TaskManagerService = require('./service/TaskManagerService.js');

var _TaskManagerService2 = _interopRequireDefault(_TaskManagerService);

var _TaskManagerController = require('./list/TaskManagerController.js');

var _TaskManagerController2 = _interopRequireDefault(_TaskManagerController);

var _TaskManagerEdit = require('./edit/TaskManagerEdit.js');

var _TaskManagerEdit2 = _interopRequireDefault(_TaskManagerEdit);

var _svgLoader = require('../../directives/Svg/svgLoader.js');

var _svgLoader2 = _interopRequireDefault(_svgLoader);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

var TaskManagerModule = _angular2.default.module('TDSTM.TaskManagerModule', [_uiRouter2.default]).config(['$stateProvider', function ($stateProvider) {

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: 'app-js/modules/header/HeaderContainer.html',
        controller: 'HeaderController as header'
    };

    $stateProvider.state('taskList', {
        data: { page: { title: 'My Task Manager', instruction: '', menu: ['Task Manager'] } },
        url: '/task/list',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: 'app-js/modules/taskManager/list/TaskManagerContainer.html',
                controller: 'TaskManagerController as taskManager'
            }
        }
    });
}]);

// Services


// Directives
TaskManagerModule.service('taskManagerService', ['$log', 'RestServiceHandler', _TaskManagerService2.default]);

// Controllers
TaskManagerModule.controller('TaskManagerController', ['$log', 'taskManagerService', '$uibModal', _TaskManagerController2.default]);
TaskManagerModule.controller('TaskManagerEdit', ['$log', _TaskManagerEdit2.default]);

// Directives
TaskManagerModule.directive('svgLoader', _svgLoader2.default);

exports.default = TaskManagerModule;

},{"../../directives/Svg/svgLoader.js":3,"./edit/TaskManagerEdit.js":20,"./list/TaskManagerController.js":21,"./service/TaskManagerService.js":22,"angular":"angular","ui-router":"ui-router"}],20:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 3/11/2016.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var TaskManagerEdit = function TaskManagerEdit($log, taskManagerService, $uibModal) {
    _classCallCheck(this, TaskManagerEdit);
};

exports.default = TaskManagerEdit;

},{}],21:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/20/2015.
 */
'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var TaskManagerController = function () {
    function TaskManagerController($log, taskManagerService, $uibModal) {
        _classCallCheck(this, TaskManagerController);

        this.log = $log;
        this.uibModal = $uibModal;
        this.module = 'TaskManager';
        this.taskManagerService = taskManagerService;
        this.taskGridOptions = {};
        this.eventDataSource = [];

        // Init Class
        this.getEventDataSource();
        this.getDataSource();
        this.log.debug('TaskManager Controller Instanced');
    }

    _createClass(TaskManagerController, [{
        key: 'openModalDemo',
        value: function openModalDemo() {
            var _this = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: 'app-js/modules/taskManager/edit/TaskManagerEdit.html',
                controller: 'TaskManagerEdit',
                size: 'lg',
                resolve: {
                    items: function items() {
                        return ['1', 'a2', 'gg'];
                    }
                }
            });

            modalInstance.result.then(function (selectedItem) {
                _this.debug(selectedItem);
            }, function () {
                _this.log.info('Modal dismissed at: ' + new Date());
            });
        }
    }, {
        key: 'getDataSource',
        value: function getDataSource() {
            var _this2 = this;

            this.taskGridOptions = {
                groupable: true,
                sortable: true,
                pageable: {
                    refresh: true,
                    pageSizes: true,
                    buttonCount: 5
                },
                columns: [{ field: 'action', title: 'Action' }, { field: 'task', title: 'Task' }, { field: 'description', title: 'Description' }, { field: 'assetName', title: 'Asset Name' }, { field: 'assetType', title: 'Asset Type' }, { field: 'updated', title: 'Updated' }, { field: 'due', title: 'Due' }, { field: 'status', title: 'Status' }, { field: 'assignedTo', title: 'Assigned To' }, { field: 'team', title: 'Team' }, { field: 'category', title: 'Category' }, { field: 'suc', title: 'Suc.' }, { field: 'score', title: 'Score' }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            _this2.taskManagerService.testService(function (data) {
                                e.success(data);
                            });
                        }
                    }
                }
            };
        }
    }, {
        key: 'getEventDataSource',
        value: function getEventDataSource() {
            this.eventDataSource = [{ eventId: 1, eventName: 'All' }, { eventId: 2, eventName: 'Buildout' }, { eventId: 3, eventName: 'DR-EP' }, { eventId: 4, eventName: 'M1-Physical' }];
        }
    }, {
        key: 'onErrorHappens',
        value: function onErrorHappens() {
            this.taskManagerService.failCall(function () {});
        }
    }]);

    return TaskManagerController;
}();

exports.default = TaskManagerController;

},{}],22:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 22/07/15.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var TaskManagerService = function () {
    function TaskManagerService($log, RestServiceHandler) {
        _classCallCheck(this, TaskManagerService);

        this.log = $log;
        this.restService = RestServiceHandler;

        this.log.debug('TaskManagerService Instanced');
    }

    _createClass(TaskManagerService, [{
        key: 'failCall',
        value: function failCall(callback) {
            this.restService.ResourceServiceHandler().getSVG();
        }
    }, {
        key: 'testService',
        value: function testService(callback) {
            this.restService.TaskServiceHandler().getFeeds(function (data) {
                return callback(data);
            });
        }
    }]);

    return TaskManagerService;
}();

exports.default = TaskManagerService;

},{}],23:[function(require,module,exports){
"use strict";

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

/**
 * Created by Jorge Morayta on 12/23/2015.
 * Implements RX Observable to dispose and track better each call to the server
 * The Observer subscribe a promise.
 */

var RequestHandler = function () {
    function RequestHandler(rx) {
        _classCallCheck(this, RequestHandler);

        this.rx = rx;
        this.promise = [];
    }

    /**
     * Called from RestServiceHandler.subscribeRequest
     * it verify that the call is being done to the server and return a promise
     * @param request
     * @returns {*}
     */

    _createClass(RequestHandler, [{
        key: "subscribeRequest",
        value: function subscribeRequest(request, callback) {
            var rxObservable = this.rx.Observable.fromPromise(request);
            // Verify is not a duplicate call
            if (this.isSubscribed(rxObservable)) {
                this.cancelRequest(rxObservable);
            }

            // Subscribe the request
            var resultSubscribe = this.addSubscribe(rxObservable, callback);
            if (resultSubscribe && resultSubscribe.isStopped) {
                // An error happens, tracked by HttpInterceptorInterface
                delete this.promise[rxObservable._p];
            }
        }
    }, {
        key: "addSubscribe",
        value: function addSubscribe(rxObservable, callback) {
            var _this = this;

            this.promise[rxObservable._p] = rxObservable.subscribe(function (response) {
                return _this.onSubscribedSuccess(response, rxObservable, callback);
            }, function (error) {
                return _this.onSubscribedError(error, rxObservable, callback);
            }, function () {
                // NO-OP Subscribe completed
            });

            return this.promise[rxObservable._p];
        }
    }, {
        key: "cancelRequest",
        value: function cancelRequest(rxObservable) {
            if (this.isSubscribed(rxObservable)) {
                delete this.promise[rxObservable._p];
                rxObservable.dispose();
            }
        }
    }, {
        key: "isSubscribed",
        value: function isSubscribed(rxObservable) {
            return rxObservable && rxObservable._p && this.promise[rxObservable._p];
        }
    }, {
        key: "onSubscribedSuccess",
        value: function onSubscribedSuccess(response, rxObservable, callback) {
            if (this.isSubscribed(rxObservable)) {
                delete this.promise[rxObservable._p];
            }
            if (callback) {
                return callback(response.data);
            }
        }

        /**
         * Throws immediately error when the petition call is wrong
         * or with a delay if the call is valid
         * @param error
         * @returns {*}
         */

    }, {
        key: "onSubscribedError",
        value: function onSubscribedError(error, rxObservable, callback) {
            if (this.isSubscribed(rxObservable)) {
                delete this.promise[rxObservable._p];
            }
            if (callback) {
                return callback({});
            }
        }
    }]);

    return RequestHandler;
}();

exports.default = RequestHandler;

},{}],24:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/22/2015.
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _angular = require('angular');

var _angular2 = _interopRequireDefault(_angular);

var _RestServiceHandler = require('./RestServiceHandler.js');

var _RestServiceHandler2 = _interopRequireDefault(_RestServiceHandler);

function _interopRequireDefault(obj) {
  return obj && obj.__esModule ? obj : { default: obj };
}

var RestAPIModule = _angular2.default.module('TDSTM.RestAPIModule', []);

RestAPIModule.service('RestServiceHandler', ['$log', '$http', '$resource', 'rx', _RestServiceHandler2.default]);

exports.default = RestAPIModule;

},{"./RestServiceHandler.js":25,"angular":"angular"}],25:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/08/15.
 * It abstract each one of the existing call to the API, it should only contains the call functions and reference
 * to the callback, no logic at all.
 *
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

var _RequestHandler = require('./RequestHandler.js');

var _RequestHandler2 = _interopRequireDefault(_RequestHandler);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

var RestServiceHandler = function () {
    function RestServiceHandler($log, $http, $resource, rx) {
        _classCallCheck(this, RestServiceHandler);

        this.rx = rx;
        this.log = $log;
        this.http = $http;
        this.resource = $resource;
        this.prepareHeaders();
        this.log.debug('RestService Loaded');
    }

    _createClass(RestServiceHandler, [{
        key: 'prepareHeaders',
        value: function prepareHeaders() {
            this.http.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';
        }
    }, {
        key: 'TaskServiceHandler',
        value: function TaskServiceHandler() {
            var _this = this;

            return {
                getFeeds: function getFeeds(callback) {
                    return _this.subscribeRequest(_this.http.get('test/mockupData/TaskManager/taskManagerList.json'), callback);
                }
            };
        }
    }, {
        key: 'licenseManagerServiceHandler',
        value: function licenseManagerServiceHandler() {
            var _this2 = this;

            return {
                getLicenseList: function getLicenseList(data, callback) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/cookbook/recipe/list?archived=n&context=All&rand=oDFqLTpbZRj38AW'), callback);
                },
                getLicense: function getLicense(callback) {
                    // Mockup Data for testing see url
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../test/mockupData/LicenseManager/licenseManagerList.json'), callback);
                },
                createNewLicenseRequest: function createNewLicenseRequest(data, callback) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.post('../test/mockupData/LicenseManager/licenseManagerList.json', data), callback);
                }
            };
        }
    }, {
        key: 'noticeManagerServiceHandler',
        value: function noticeManagerServiceHandler() {
            var _this3 = this;

            return {
                getNoticeList: function getNoticeList(data, callback) {
                    // real ws example
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/cookbook/recipe/list?archived=n&context=All&rand=oDFqLTpbZRj38AW'), callback);
                },
                getNoticeMockUp: function getNoticeMockUp(callback) {
                    // Mockup Data for testing see url
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../test/mockupData/NoticeManager/noticeManagerList.json'), callback);
                },
                createNotice: function createNotice(data, callback) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.post('../test/mockupData/NoticeManager/noticeManagerList.json', data), callback);
                },
                editNotice: function editNotice(data, callback) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.post('../test/mockupData/NoticeManager/noticeManagerList.json', data), callback);
                }
            };
        }
    }]);

    return RestServiceHandler;
}();

exports.default = RestServiceHandler;

},{"./RequestHandler.js":23}],26:[function(require,module,exports){
'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

/* interface*/ /**
                * Created by Jorge Morayta on 12/22/2015.
                * ES6 Interceptor calls inner methods in a global scope, then the "this" is being lost
                * in the definition of the Class for interceptors only
                * This is a interface that take care of the issue.
                */

var HttpInterceptor = function HttpInterceptor(methodToBind) {
    var _this = this;

    _classCallCheck(this, HttpInterceptor);

    // If not method to bind, we assume our interceptor is using all the inner functions
    if (!methodToBind) {
        ['request', 'requestError', 'response', 'responseError'].forEach(function (method) {
            if (_this[method]) {
                _this[method] = _this[method].bind(_this);
            }
        });
    } else {
        // methodToBind reference to a single child class
        this[methodToBind] = this[methodToBind].bind(this);
    }
};

exports.default = HttpInterceptor;

},{}],27:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/22/2015.
 * Use this module to modify anything related to the Headers and Request
 */

'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _angular = require('angular');

var _angular2 = _interopRequireDefault(_angular);

var _HTTPRequestHandlerInterceptor = require('./HTTPRequestHandlerInterceptor.js');

var _HTTPRequestHandlerInterceptor2 = _interopRequireDefault(_HTTPRequestHandlerInterceptor);

var _HTTPRequestErrorHandlerInterceptor = require('./HTTPRequestErrorHandlerInterceptor.js');

var _HTTPRequestErrorHandlerInterceptor2 = _interopRequireDefault(_HTTPRequestErrorHandlerInterceptor);

var _HTTPResponseErrorHandlerInterceptor = require('./HTTPResponseErrorHandlerInterceptor.js');

var _HTTPResponseErrorHandlerInterceptor2 = _interopRequireDefault(_HTTPResponseErrorHandlerInterceptor);

var _HTTPResponseHandlerInterceptor = require('./HTTPResponseHandlerInterceptor.js');

var _HTTPResponseHandlerInterceptor2 = _interopRequireDefault(_HTTPResponseHandlerInterceptor);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

var HTTPModule = _angular2.default.module('TDSTM.HTTPModule', ['ngResource']).config(['$httpProvider', function ($httpProvider) {

    //initialize get if not there
    if (!$httpProvider.defaults.headers.get) {
        $httpProvider.defaults.headers.get = {};
    }

    //Disable IE ajax request caching
    $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Mon, 26 Jul 1997 05:00:00 GMT';
    // extra
    $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
    $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';

    // Injects our Interceptors for Request
    $httpProvider.interceptors.push('HTTPRequestHandlerInterceptor');
    $httpProvider.interceptors.push('HTTPRequestErrorHandlerInterceptor');
    // Injects our Interceptors for Response
    $httpProvider.interceptors.push('HTTPResponseHandlerInterceptor');
    $httpProvider.interceptors.push('HTTPResponseErrorHandlerInterceptor');
}]);

HTTPModule.service('HTTPRequestHandlerInterceptor', ['$log', '$q', 'rx', _HTTPRequestHandlerInterceptor2.default]);
HTTPModule.service('HTTPRequestErrorHandlerInterceptor', ['$log', '$q', 'rx', _HTTPRequestErrorHandlerInterceptor2.default]);
HTTPModule.service('HTTPResponseHandlerInterceptor', ['$log', '$q', 'rx', _HTTPResponseHandlerInterceptor2.default]);
HTTPModule.service('HTTPResponseErrorHandlerInterceptor', ['$log', '$q', 'rx', _HTTPResponseErrorHandlerInterceptor2.default]);

exports.default = HTTPModule;

},{"./HTTPRequestErrorHandlerInterceptor.js":28,"./HTTPRequestHandlerInterceptor.js":29,"./HTTPResponseErrorHandlerInterceptor.js":30,"./HTTPResponseHandlerInterceptor.js":31,"angular":"angular"}],28:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/11/15.
 * It implement an abstract call to HTTP Interceptors to manage error handler
 * Sometimes a request can't be sent or it is rejected by an interceptor.
 * Request error interceptor captures requests that have been canceled by a previous request interceptor.
 * It can be used in order to recover the request and sometimes undo things that have been set up before a request,
 * like removing overlays and loading indicators, enabling buttons and fields and so on.
 */

'use strict';

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

var _HTTPInterceptorInterface = require('./HTTPInterceptorInterface.js');

var _HTTPInterceptorInterface2 = _interopRequireDefault(_HTTPInterceptorInterface);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

function _possibleConstructorReturn(self, call) {
    if (!self) {
        throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
    }return call && ((typeof call === "undefined" ? "undefined" : _typeof(call)) === "object" || typeof call === "function") ? call : self;
}

function _inherits(subClass, superClass) {
    if (typeof superClass !== "function" && superClass !== null) {
        throw new TypeError("Super expression must either be null or a function, not " + (typeof superClass === "undefined" ? "undefined" : _typeof(superClass)));
    }subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } });if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
}

var HTTPRequestErrorHandlerInterceptor = function (_HTTPInterceptorInter) {
    _inherits(HTTPRequestErrorHandlerInterceptor, _HTTPInterceptorInter);

    function HTTPRequestErrorHandlerInterceptor($log, $q, rx) {
        _classCallCheck(this, HTTPRequestErrorHandlerInterceptor);

        var _this = _possibleConstructorReturn(this, (HTTPRequestErrorHandlerInterceptor.__proto__ || Object.getPrototypeOf(HTTPRequestErrorHandlerInterceptor)).call(this, 'requestError'));

        _this.log = $log;
        _this.q = $q;
        _this.defer = _this.q.defer();
        _this.log.debug('HTTPRequestErrorHandlerInterceptor instanced');
        return _this;
    }

    _createClass(HTTPRequestErrorHandlerInterceptor, [{
        key: 'requestError',
        value: function requestError(rejection) {
            // do something on error
            // do something on error
            //if (canRecover(rejection)) {
            //    return responseOrNewPromise
            //}
            this.defer.notify(rejection);

            return this.q.reject(rejection);
        }
    }, {
        key: 'listenError',
        value: function listenError() {
            return this.defer.promise;
        }
    }]);

    return HTTPRequestErrorHandlerInterceptor;
}( /*implements*/_HTTPInterceptorInterface2.default);

exports.default = HTTPRequestErrorHandlerInterceptor;

},{"./HTTPInterceptorInterface.js":26}],29:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/11/15.
 * It implement an abstract call to HTTP Interceptors to manage only request
 */

'use strict';

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

var _HTTPInterceptorInterface = require('./HTTPInterceptorInterface.js');

var _HTTPInterceptorInterface2 = _interopRequireDefault(_HTTPInterceptorInterface);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

function _possibleConstructorReturn(self, call) {
    if (!self) {
        throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
    }return call && ((typeof call === "undefined" ? "undefined" : _typeof(call)) === "object" || typeof call === "function") ? call : self;
}

function _inherits(subClass, superClass) {
    if (typeof superClass !== "function" && superClass !== null) {
        throw new TypeError("Super expression must either be null or a function, not " + (typeof superClass === "undefined" ? "undefined" : _typeof(superClass)));
    }subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } });if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
}

var HTTPRequestHandlerInterceptor = function (_HTTPInterceptorInter) {
    _inherits(HTTPRequestHandlerInterceptor, _HTTPInterceptorInter);

    function HTTPRequestHandlerInterceptor($log, $q, rx) {
        _classCallCheck(this, HTTPRequestHandlerInterceptor);

        var _this = _possibleConstructorReturn(this, (HTTPRequestHandlerInterceptor.__proto__ || Object.getPrototypeOf(HTTPRequestHandlerInterceptor)).call(this, 'request'));

        _this.log = $log;
        _this.q = $q;
        _this.defer = _this.q.defer();
        _this.log.debug('HTTPRequestHandlerInterceptor instanced');
        return _this;
    }

    _createClass(HTTPRequestHandlerInterceptor, [{
        key: 'request',
        value: function request(config) {
            // We can add headers if on the incoming request made it we have the token inside
            // defined by some conditions
            //config.headers['x-session-token'] = my.token;

            config.requestTimestamp = new Date().getTime();

            this.defer.notify(config);

            return config || this.q.when(config);
        }
    }, {
        key: 'listenRequest',
        value: function listenRequest() {
            return this.defer.promise;
        }
    }]);

    return HTTPRequestHandlerInterceptor;
}( /*implements*/_HTTPInterceptorInterface2.default);

exports.default = HTTPRequestHandlerInterceptor;

},{"./HTTPInterceptorInterface.js":26}],30:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/11/15.
 * If backend call fails or it might be rejected by a request interceptor or by a previous response interceptor;
 * In those cases, response error interceptor can help us to recover the backend call.
 */

'use strict';

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

var _HTTPInterceptorInterface = require('./HTTPInterceptorInterface.js');

var _HTTPInterceptorInterface2 = _interopRequireDefault(_HTTPInterceptorInterface);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

function _possibleConstructorReturn(self, call) {
    if (!self) {
        throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
    }return call && ((typeof call === "undefined" ? "undefined" : _typeof(call)) === "object" || typeof call === "function") ? call : self;
}

function _inherits(subClass, superClass) {
    if (typeof superClass !== "function" && superClass !== null) {
        throw new TypeError("Super expression must either be null or a function, not " + (typeof superClass === "undefined" ? "undefined" : _typeof(superClass)));
    }subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } });if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
}

var HTTPResponseErrorHandlerInterceptor = function (_HTTPInterceptorInter) {
    _inherits(HTTPResponseErrorHandlerInterceptor, _HTTPInterceptorInter);

    function HTTPResponseErrorHandlerInterceptor($log, $q, rx) {
        _classCallCheck(this, HTTPResponseErrorHandlerInterceptor);

        var _this = _possibleConstructorReturn(this, (HTTPResponseErrorHandlerInterceptor.__proto__ || Object.getPrototypeOf(HTTPResponseErrorHandlerInterceptor)).call(this, 'responseError'));

        _this.log = $log;
        _this.q = $q;
        _this.defer = _this.q.defer();
        _this.log.debug('HTTPResponseErrorHandlerInterceptor instanced');
        return _this;
    }

    _createClass(HTTPResponseErrorHandlerInterceptor, [{
        key: 'responseError',
        value: function responseError(rejection) {
            // do something on error
            //if (canRecover(rejection)) {
            //    return responseOrNewPromise
            // }

            this.defer.notify(rejection);
            return this.q.reject(rejection);
        }
    }, {
        key: 'listenError',
        value: function listenError() {
            return this.defer.promise;
        }
    }]);

    return HTTPResponseErrorHandlerInterceptor;
}( /*implements*/_HTTPInterceptorInterface2.default);

exports.default = HTTPResponseErrorHandlerInterceptor;

},{"./HTTPInterceptorInterface.js":26}],31:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/11/15.
 * This method is called right after $http receives the response from the backend,
 * so you can modify the response and make other actions. This function receives a response object as a parameter
 * and has to return a response object or a promise. The response object includes
 * the request configuration, headers, status and data that returned from the backend.
 * Returning an invalid response object or promise that will be rejected, will make the $http call to fail.
 */

'use strict';

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () {
    function defineProperties(target, props) {
        for (var i = 0; i < props.length; i++) {
            var descriptor = props[i];descriptor.enumerable = descriptor.enumerable || false;descriptor.configurable = true;if ("value" in descriptor) descriptor.writable = true;Object.defineProperty(target, descriptor.key, descriptor);
        }
    }return function (Constructor, protoProps, staticProps) {
        if (protoProps) defineProperties(Constructor.prototype, protoProps);if (staticProps) defineProperties(Constructor, staticProps);return Constructor;
    };
}();

var _HTTPInterceptorInterface = require('./HTTPInterceptorInterface.js');

var _HTTPInterceptorInterface2 = _interopRequireDefault(_HTTPInterceptorInterface);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

function _classCallCheck(instance, Constructor) {
    if (!(instance instanceof Constructor)) {
        throw new TypeError("Cannot call a class as a function");
    }
}

function _possibleConstructorReturn(self, call) {
    if (!self) {
        throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
    }return call && ((typeof call === "undefined" ? "undefined" : _typeof(call)) === "object" || typeof call === "function") ? call : self;
}

function _inherits(subClass, superClass) {
    if (typeof superClass !== "function" && superClass !== null) {
        throw new TypeError("Super expression must either be null or a function, not " + (typeof superClass === "undefined" ? "undefined" : _typeof(superClass)));
    }subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } });if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
}

var HTTPResponseHandlerInterceptor = function (_HTTPInterceptorInter) {
    _inherits(HTTPResponseHandlerInterceptor, _HTTPInterceptorInter);

    function HTTPResponseHandlerInterceptor($log, $q, rx) {
        _classCallCheck(this, HTTPResponseHandlerInterceptor);

        var _this = _possibleConstructorReturn(this, (HTTPResponseHandlerInterceptor.__proto__ || Object.getPrototypeOf(HTTPResponseHandlerInterceptor)).call(this, 'response'));

        _this.log = $log;
        _this.q = $q;
        _this.defer = _this.q.defer();
        _this.log.debug('HTTPResponseHandlerInterceptor instanced');
        return _this;
    }

    _createClass(HTTPResponseHandlerInterceptor, [{
        key: 'response',
        value: function response(_response) {
            // do something on success

            _response.config.responseTimestamp = new Date().getTime();

            this.defer.notify(_response);
            return _response || this.q.when(_response);
        }
    }, {
        key: 'listenResponse',
        value: function listenResponse() {
            return this.defer.promise;
        }
    }]);

    return HTTPResponseHandlerInterceptor;
}( /*implements*/_HTTPInterceptorInterface2.default);

exports.default = HTTPResponseHandlerInterceptor;

},{"./HTTPInterceptorInterface.js":26}]},{},[6])
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxjb25maWdcXEFwcC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcU3ZnXFxzdmdMb2FkZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXFRvb2xzXFxUb2FzdEhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXGluZGV4LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtYWluLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxoZWFkZXJcXEhlYWRlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGhlYWRlclxcSGVhZGVyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcTGljZW5zZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxjcmVhdGVkXFxDcmVhdGVkTGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXGRldGFpbFxcTGljZW5zZURldGFpbC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXGxpc3RcXExpY2Vuc2VMaXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxccmVxdWVzdFxcUmVxdWVzdExpY2Vuc2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxzZXJ2aWNlXFxMaWNlbnNlTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXE5vdGljZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXGVkaXRcXEVkaXROb3RpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXGxpc3RcXE5vdGljZUxpc3QuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXHNlcnZpY2VcXE5vdGljZU1hbmFnZXJTZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFx0YXNrTWFuYWdlclxcVGFza01hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxlZGl0XFxUYXNrTWFuYWdlckVkaXQuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxsaXN0XFxUYXNrTWFuYWdlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxzZXJ2aWNlXFxUYXNrTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RBUElNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXN0U2VydmljZUhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7OztBQ0FBOzs7Ozs7Ozs7O0FBVUEsSUFBSSxRQUFRLFFBQVosQUFBWSxBQUFROztBQUVwQjs7Ozs7QUFLQSxNQUFBLEFBQU0sWUFBWSxVQUFBLEFBQVUsU0FBVixBQUFtQixJQUFJLEFBQ3JDO0FBQ0E7O1FBQUksUUFBUSxRQUFBLEFBQVEsTUFBcEIsQUFBMEIsQUFDMUI7UUFBSSxVQUFBLEFBQVUsWUFBWSxVQUExQixBQUFvQyxXQUFXLEFBQzNDO1lBQUEsQUFBSSxJQUFJLEFBQ0o7b0JBQUEsQUFBUSxNQUFSLEFBQWMsQUFDakI7QUFDSjtBQUpELFdBSU8sQUFDSDtZQUFBLEFBQUksSUFBSSxBQUNKO29CQUFBLEFBQVEsT0FBUixBQUFlLEFBQ2xCO0FBRkQsZUFFTyxBQUNIO29CQUFBLEFBQVEsQUFDWDtBQUNKO0FBQ0o7QUFkRDs7QUFnQkE7Ozs7O0FBS0EsTUFBQSxBQUFNLGtCQUFrQixVQUFBLEFBQVUsU0FBVixBQUFtQixNQUFNLEFBQzdDO0FBQ0E7O1FBQUksTUFBQSxBQUFNLGFBQVYsQUFBdUIsaUJBQWlCLEFBQ3BDO2NBQUEsQUFBTSxhQUFOLEFBQW1CLGdCQUFuQixBQUFtQyxVQUFuQyxBQUE2QyxTQUE3QyxBQUFzRCxBQUN6RDtBQUZELFdBRU8sSUFBSSxNQUFKLEFBQVUsV0FBVyxBQUN4QjtjQUFBLEFBQU0sVUFBTixBQUFnQixTQUFoQixBQUF5QixBQUM1QjtBQUNKO0FBUEQ7O0FBU0E7Ozs7O0FBS0EsTUFBQSxBQUFNLG1CQUFtQixVQUFBLEFBQVUsU0FBVixBQUFtQixNQUFNLEFBQzlDO0FBQ0E7O1FBQUksTUFBQSxBQUFNLGFBQVYsQUFBdUIsb0JB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LEFBQW1CLEFBQVksQUFDbEM7Ozs7eUNBRWdCLEFBQ2I7bUJBQU8sS0FBQSxBQUFLLE1BQVosQUFBa0IsQUFDckI7Ozs7O0dBcEJ1RCxjOztrQixBQUF2QyIsImZpbGUiOiJnZW5lcmF0ZWQuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlc0NvbnRlbnQiOlsiKGZ1bmN0aW9uIGUodCxuLHIpe2Z1bmN0aW9uIHMobyx1KXtpZighbltvXSl7aWYoIXRbb10pe3ZhciBhPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7aWYoIXUmJmEpcmV0dXJuIGEobywhMCk7aWYoaSlyZXR1cm4gaShvLCEwKTt2YXIgZj1uZXcgRXJyb3IoXCJDYW5ub3QgZmluZCBtb2R1bGUgJ1wiK28rXCInXCIpO3Rocm93IGYuY29kZT1cIk1PRFVMRV9OT1RfRk9VTkRcIixmfXZhciBsPW5bb109e2V4cG9ydHM6e319O3Rbb11bMF0uY2FsbChsLmV4cG9ydHMsZnVuY3Rpb24oZSl7dmFyIG49dFtvXVsxXVtlXTtyZXR1cm4gcyhuP246ZSl9LGwsbC5leHBvcnRzLGUsdCxuLHIpfXJldHVybiBuW29dLmV4cG9ydHN9dmFyIGk9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtmb3IodmFyIG89MDtvPHIubGVuZ3RoO28rKylzKHJbb10pO3JldHVybiBzfSkiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzIwLzIwMTUuXHJcbiAqIFREU00gaXMgYSBnbG9iYWwgb2JqZWN0IHRoYXQgY29tZXMgZnJvbSBBcHAuanNcclxuICpcclxuICogVGhlIGZvbGxvd2luZyBoZWxwZXIgd29ya3MgaW4gYSB3YXkgdG8gbWFrZSBhdmFpbGFibGUgdGhlIGNyZWF0aW9uIG9mIERpcmVjdGl2ZSwgU2VydmljZXMgYW5kIENvbnRyb2xsZXJcclxuICogb24gZmx5IG9yIHdoZW4gZGVwbG95aW5nIHRoZSBhcHAuXHJcbiAqXHJcbiAqIFdlIHJlZHVjZSB0aGUgdXNlIG9mIGNvbXBpbGUgYW5kIGV4dHJhIHN0ZXBzXHJcbiAqL1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi9BcHAuanMnKTtcclxuXHJcbi8qKlxyXG4gKiBMaXN0ZW4gdG8gYW4gZXhpc3RpbmcgZGlnZXN0IG9mIHRoZSBjb21waWxlIHByb3ZpZGVyIGFuZCBleGVjdXRlIHRoZSAkYXBwbHkgaW1tZWRpYXRlbHkgb3IgYWZ0ZXIgaXQncyByZWFkeVxyXG4gKiBAcGFyYW0gY3VycmVudFxyXG4gKiBAcGFyYW0gZm5cclxuICovXHJcblREU1RNLnNhZmVBcHBseSA9IGZ1bmN0aW9uIChjdXJyZW50LCBmbikge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgdmFyIHBoYXNlID0gY3VycmVudC4kcm9vdC4kJHBoYXNlO1xyXG4gICAgaWYgKHBoYXNlID09PSAnJGFwcGx5JyB8fCBwaGFzZSA9PT0gJyRkaWdlc3QnKSB7XHJcbiAgICAgICAgaWYgKGZuKSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGV2YWwoZm4pO1xyXG4gICAgICAgIH1cclxuICAgIH0gZWxzZSB7XHJcbiAgICAgICAgaWYgKGZuKSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGFwcGx5KGZuKTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRhcHBseSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IGRpcmVjdGl2ZSBhc3luYyBpZiB0aGUgY29tcGlsZVByb3ZpZGVyIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyKSB7XHJcbiAgICAgICAgVERTVE0uUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlci5kaXJlY3RpdmUoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmRpcmVjdGl2ZSkge1xyXG4gICAgICAgIFREU1RNLmRpcmVjdGl2ZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IGNvbnRyb2xsZXJzIGFzeW5jIGlmIHRoZSBjb250cm9sbGVyUHJvdmlkZXIgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVDb250cm9sbGVyID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUuY29udHJvbGxlclByb3ZpZGVyKSB7XHJcbiAgICAgICAgVERTVE0uY29udHJvbGxlclByb3ZpZGVyLnJlZ2lzdGVyKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5jb250cm9sbGVyKSB7XHJcbiAgICAgICAgVERTVE0uY29udHJvbGxlcihzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IHNlcnZpY2UgYXN5bmMgaWYgdGhlIHByb3ZpZGVTZXJ2aWNlIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlU2VydmljZSA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlKSB7XHJcbiAgICAgICAgVERTVE0uUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlLnNlcnZpY2Uoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmNvbnRyb2xsZXIpIHtcclxuICAgICAgICBURFNUTS5zZXJ2aWNlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEZvciBMZWdhY3kgc3lzdGVtLCB3aGF0IGlzIGRvZXMgaXMgdG8gdGFrZSBwYXJhbXMgZnJvbSB0aGUgcXVlcnlcclxuICogb3V0c2lkZSB0aGUgQW5ndWxhckpTIHVpLXJvdXRpbmcuXHJcbiAqIEBwYXJhbSBwYXJhbSAvLyBQYXJhbSB0byBzZWFyYyBmb3IgL2V4YW1wbGUuaHRtbD9iYXI9Zm9vI2N1cnJlbnRTdGF0ZVxyXG4gKi9cclxuVERTVE0uZ2V0VVJMUGFyYW0gPSBmdW5jdGlvbiAocGFyYW0pIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgICQudXJsUGFyYW0gPSBmdW5jdGlvbiAobmFtZSkge1xyXG4gICAgICAgIHZhciByZXN1bHRzID0gbmV3IFJlZ0V4cCgnW1xcPyZdJyArIG5hbWUgKyAnPShbXiYjXSopJykuZXhlYyh3aW5kb3cubG9jYXRpb24uaHJlZik7XHJcbiAgICAgICAgaWYgKHJlc3VsdHMgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgcmV0dXJuIG51bGw7XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGVsc2Uge1xyXG4gICAgICAgICAgICByZXR1cm4gcmVzdWx0c1sxXSB8fCAwO1xyXG4gICAgICAgIH1cclxuICAgIH07XHJcblxyXG4gICAgcmV0dXJuICQudXJsUGFyYW0ocGFyYW0pO1xyXG59O1xyXG5cclxuLyoqXHJcbiAqIFRoaXMgY29kZSB3YXMgaW50cm9kdWNlZCBvbmx5IGZvciB0aGUgaWZyYW1lIG1pZ3JhdGlvblxyXG4gKiBpdCBkZXRlY3Qgd2hlbiBtb3VzZSBlbnRlclxyXG4gKi9cclxuVERTVE0uaWZyYW1lTG9hZGVyID0gZnVuY3Rpb24gKCkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgJCgnLmlmcmFtZUxvYWRlcicpLmhvdmVyKFxyXG4gICAgICAgIGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgJCgnLm5hdmJhci11bC1jb250YWluZXIgLmRyb3Bkb3duLm9wZW4nKS5yZW1vdmVDbGFzcygnb3BlbicpO1xyXG4gICAgICAgIH0sIGZ1bmN0aW9uICgpIHtcclxuICAgICAgICB9XHJcbiAgICApO1xyXG59O1xyXG5cclxuXHJcbi8vIEl0IHdpbGwgYmUgcmVtb3ZlZCBhZnRlciB3ZSByaXAgb2ZmIGFsbCBpZnJhbWVzXHJcbndpbmRvdy5URFNUTSA9IFREU1RNOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMTYvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5yZXF1aXJlKCdhbmd1bGFyJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItYW5pbWF0ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLW1vY2tzJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItc2FuaXRpemUnKTtcclxucmVxdWlyZSgnYW5ndWxhci1yZXNvdXJjZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXRyYW5zbGF0ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXRyYW5zbGF0ZS1sb2FkZXItcGFydGlhbCcpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXVpLWJvb3RzdHJhcCcpO1xyXG5yZXF1aXJlKCd1aS1yb3V0ZXInKTtcclxucmVxdWlyZSgncngtYW5ndWxhcicpO1xyXG5cclxuLy8gTW9kdWxlc1xyXG5pbXBvcnQgSFRUUE1vZHVsZSBmcm9tICcuLi9zZXJ2aWNlcy9odHRwL0hUVFBNb2R1bGUuanMnO1xyXG5pbXBvcnQgUmVzdEFQSU1vZHVsZSBmcm9tICcuLi9zZXJ2aWNlcy9SZXN0QVBJL1Jlc3RBUElNb2R1bGUuanMnXHJcbmltcG9ydCBIZWFkZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9oZWFkZXIvSGVhZGVyTW9kdWxlLmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvTGljZW5zZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTm90aWNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL25vdGljZU1hbmFnZXIvTm90aWNlTWFuYWdlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL3Rhc2tNYW5hZ2VyL1Rhc2tNYW5hZ2VyTW9kdWxlLmpzJztcclxuXHJcbnZhciBQcm92aWRlckNvcmUgPSB7fTtcclxuXHJcbnZhciBURFNUTSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTScsIFtcclxuICAgICduZ1Nhbml0aXplJyxcclxuICAgICduZ1Jlc291cmNlJyxcclxuICAgICduZ0FuaW1hdGUnLFxyXG4gICAgJ3Bhc2NhbHByZWNodC50cmFuc2xhdGUnLCAvLyAnYW5ndWxhci10cmFuc2xhdGUnXHJcbiAgICAndWkucm91dGVyJyxcclxuICAgICdrZW5kby5kaXJlY3RpdmVzJyxcclxuICAgICdyeCcsXHJcbiAgICAndWkuYm9vdHN0cmFwJyxcclxuICAgIEhUVFBNb2R1bGUubmFtZSxcclxuICAgIFJlc3RBUElNb2R1bGUubmFtZSxcclxuICAgIEhlYWRlck1vZHVsZS5uYW1lLFxyXG4gICAgVGFza01hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIExpY2Vuc2VNYW5hZ2VyTW9kdWxlLm5hbWUsXHJcbiAgICBOb3RpY2VNYW5hZ2VyTW9kdWxlLm5hbWVcclxuXSkuY29uZmlnKFtcclxuICAgICckbG9nUHJvdmlkZXInLFxyXG4gICAgJyRyb290U2NvcGVQcm92aWRlcicsXHJcbiAgICAnJGNvbXBpbGVQcm92aWRlcicsXHJcbiAgICAnJGNvbnRyb2xsZXJQcm92aWRlcicsXHJcbiAgICAnJHByb3ZpZGUnLFxyXG4gICAgJyRodHRwUHJvdmlkZXInLFxyXG4gICAgJyR0cmFuc2xhdGVQcm92aWRlcicsXHJcbiAgICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICAnJHVybFJvdXRlclByb3ZpZGVyJyxcclxuICAgICckbG9jYXRpb25Qcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJGxvZ1Byb3ZpZGVyLCAkcm9vdFNjb3BlUHJvdmlkZXIsICRjb21waWxlUHJvdmlkZXIsICRjb250cm9sbGVyUHJvdmlkZXIsICRwcm92aWRlLCAkaHR0cFByb3ZpZGVyLFxyXG4gICAgICAgICAgICAgICR0cmFuc2xhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlciwgJHVybFJvdXRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgICAgICRyb290U2NvcGVQcm92aWRlci5kaWdlc3RUdGwoMzApO1xyXG5cclxuICAgICAgICAkbG9nUHJvdmlkZXIuZGVidWdFbmFibGVkKHRydWUpO1xyXG5cclxuICAgICAgICAvLyBBZnRlciBib290c3RyYXBwaW5nIGFuZ3VsYXIgZm9yZ2V0IHRoZSBwcm92aWRlciBzaW5jZSBldmVyeXRoaW5nIFwid2FzIGFscmVhZHkgbG9hZGVkXCJcclxuICAgICAgICBQcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyID0gJGNvbXBpbGVQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUuY29udHJvbGxlclByb3ZpZGVyID0gJGNvbnRyb2xsZXJQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2UgPSAkcHJvdmlkZTtcclxuICAgICAgICBQcm92aWRlckNvcmUuaHR0cFByb3ZpZGVyID0gJGh0dHBQcm92aWRlcjtcclxuXHJcbiAgICAgICAgLyoqXHJcbiAgICAgICAgICogVHJhbnNsYXRpb25zXHJcbiAgICAgICAgICovXHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci51c2VTYW5pdGl6ZVZhbHVlU3RyYXRlZ3kobnVsbCk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgndGRzdG0nKTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnVzZUxvYWRlcignJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXInLCB7XHJcbiAgICAgICAgICAgIHVybFRlbXBsYXRlOiAnLi4vaTE4bi97cGFydH0vYXBwLmkxOG4te2xhbmd9Lmpzb24nXHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci5wcmVmZXJyZWRMYW5ndWFnZSgnZW5fVVMnKTtcclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIuZmFsbGJhY2tMYW5ndWFnZSgnZW5fVVMnKTtcclxuXHJcbiAgICAgICAgLy8kdXJsUm91dGVyUHJvdmlkZXIub3RoZXJ3aXNlKCdkYXNoYm9hcmQnKTtcclxuXHJcbiAgICB9XSkuXHJcbiAgICBydW4oWyckcm9vdFNjb3BlJywgJyRodHRwJywgJyRsb2cnLCAnJGxvY2F0aW9uJywgZnVuY3Rpb24gKCRyb290U2NvcGUsICRodHRwLCAkbG9nLCAkbG9jYXRpb24sICRzdGF0ZSwgJHN0YXRlUGFyYW1zLCAkbG9jYWxlKSB7XHJcbiAgICAgICAgJGxvZy5kZWJ1ZygnQ29uZmlndXJhdGlvbiBkZXBsb3llZCcpO1xyXG5cclxuICAgICAgICAkcm9vdFNjb3BlLiRvbignJHN0YXRlQ2hhbmdlU3RhcnQnLCBmdW5jdGlvbiAoZXZlbnQsIHRvU3RhdGUsIHRvUGFyYW1zLCBmcm9tU3RhdGUsIGZyb21QYXJhbXMpIHtcclxuICAgICAgICAgICAgJGxvZy5kZWJ1ZygnU3RhdGUgQ2hhbmdlIHRvICcgKyB0b1N0YXRlLm5hbWUpO1xyXG4gICAgICAgICAgICBpZiAodG9TdGF0ZS5kYXRhICYmIHRvU3RhdGUuZGF0YS5wYWdlKSB7XHJcbiAgICAgICAgICAgICAgICB3aW5kb3cuZG9jdW1lbnQudGl0bGUgPSB0b1N0YXRlLmRhdGEucGFnZS50aXRsZTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgIH1dKTtcclxuXHJcbi8vIHdlIG1hcHBlZCB0aGUgUHJvdmlkZXIgQ29yZSBsaXN0IChjb21waWxlUHJvdmlkZXIsIGNvbnRyb2xsZXJQcm92aWRlciwgcHJvdmlkZVNlcnZpY2UsIGh0dHBQcm92aWRlcikgdG8gcmV1c2UgYWZ0ZXIgb24gZmx5XHJcblREU1RNLlByb3ZpZGVyQ29yZSA9IFByb3ZpZGVyQ29yZTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxLzE3LzE1LlxyXG4gKiBXaGVuIEFuZ3VsYXIgaW52b2tlcyB0aGUgbGluayBmdW5jdGlvbiwgaXQgaXMgbm8gbG9uZ2VyIGluIHRoZSBjb250ZXh0IG9mIHRoZSBjbGFzcyBpbnN0YW5jZSwgYW5kIHRoZXJlZm9yZSBhbGwgdGhpcyB3aWxsIGJlIHVuZGVmaW5lZFxyXG4gKiBUaGlzIHN0cnVjdHVyZSB3aWxsIGF2b2lkIHRob3NlIGlzc3Vlcy5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCdcclxuXHJcbi8vIENvbnRyb2xsZXIgaXMgYmVpbmcgdXNlZCB0byBJbmplY3QgYWxsIERlcGVuZGVuY2llc1xyXG5jbGFzcyBTVkdMb2FkZXJDb250cm9sbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2cpIHtcclxuICAgICAgICB0aGlzLiRsb2cgPSAkbG9nO1xyXG4gICAgfVxyXG59XHJcblxyXG5TVkdMb2FkZXJDb250cm9sbGVyLiRpbmplY3QgPSBbJyRsb2cnXTtcclxuXHJcbi8vIERpcmVjdGl2ZVxyXG5jbGFzcyBTVkdMb2FkZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCl7XHJcbiAgICAgICAgdGhpcy5yZXN0cmljdCA9ICdFJztcclxuICAgICAgICB0aGlzLmNvbnRyb2xsZXIgPSBTVkdMb2FkZXJDb250cm9sbGVyO1xyXG4gICAgICAgIHRoaXMuc2NvcGUgPSB7XHJcbiAgICAgICAgICAgIHN2Z0RhdGE6ICc9J1xyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgbGluayhzY29wZSwgZWxlbWVudCwgYXR0cnMsIGN0cmwpIHtcclxuICAgICAgICBlbGVtZW50Lmh0bWwoc2NvcGUuc3ZnRGF0YSk7XHJcbiAgICB9XHJcblxyXG4gICAgc3RhdGljIGRpcmVjdGl2ZSgpIHtcclxuICAgICAgICByZXR1cm4gbmV3IFNWR0xvYWRlcigpO1xyXG4gICAgfVxyXG59XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgU1ZHTG9hZGVyLmRpcmVjdGl2ZTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBQcmludHMgb3V0IGFsbCBUb2FzdCBtZXNzYWdlIHdoZW4gZGV0ZWN0ZWQgZnJvbSBzZXJ2ZXIgb3IgY3VzdG9tIG1zZyB1c2luZyB0aGUgZGlyZWN0aXZlIGl0c2VsZlxyXG4gKlxyXG4gKiBQcm9iYWJseSB2YWx1ZXMgYXJlOlxyXG4gKlxyXG4gKiBzdWNjZXNzLCBkYW5nZXIsIGluZm8sIHdhcm5pbmdcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgndG9hc3RIYW5kbGVyJywgWyckbG9nJywgJyR0aW1lb3V0JywgJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLFxyXG4gICAgJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicsICdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICBmdW5jdGlvbiAoJGxvZywgJHRpbWVvdXQsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLFxyXG4gICAgICAgICAgICAgIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciwgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IpIHtcclxuXHJcbiAgICAkbG9nLmRlYnVnKCdUb2FzdEhhbmRsZXIgbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHNjb3BlOiB7XHJcbiAgICAgICAgICAgIG1zZzogJz0nLFxyXG4gICAgICAgICAgICB0eXBlOiAnPScsXHJcbiAgICAgICAgICAgIHN0YXR1czogJz0nXHJcbiAgICAgICAgfSxcclxuICAgICAgICBwcmlvcml0eTogNSxcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9kaXJlY3RpdmVzL1Rvb2xzL1RvYXN0SGFuZGxlci5odG1sJyxcclxuICAgICAgICByZXN0cmljdDogJ0UnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6IFsnJHNjb3BlJywgZnVuY3Rpb24gKCRzY29wZSkge1xyXG4gICAgICAgICAgICAkc2NvcGUuYWxlcnQgPSB7XHJcbiAgICAgICAgICAgICAgICBzdWNjZXNzOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGRhbmdlcjoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBpbmZvOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHdhcm5pbmc6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzID0ge1xyXG4gICAgICAgICAgICAgICAgc2hvdzogZmFsc2VcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIGZ1bmN0aW9uIHR1cm5PZmZOb3RpZmljYXRpb25zKCl7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuc3VjY2Vzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5pbmZvLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC53YXJuaW5nLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBMaXN0ZW4gdG8gYW55IHJlcXVlc3QsIHdlIGNhbiByZWdpc3RlciBsaXN0ZW5lciBpZiB3ZSB3YW50IHRvIGFkZCBleHRyYSBjb2RlLlxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVxdWVzdCgpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24oY29uZmlnKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgdG86ICcsICBjb25maWcpO1xyXG4gICAgICAgICAgICAgICAgdmFyIHRpbWUgPSBjb25maWcucmVxdWVzdFRpbWVzdGFtcDtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcodGltZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgZXJyb3I6ICcsICByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVzcG9uc2UoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlc3BvbnNlKXtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wIC0gcmVzcG9uc2UuY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdUaGUgcmVxdWVzdCB0b29rICcgKyAodGltZSAvIDEwMDApICsgJyBzZWNvbmRzJyk7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSByZXN1bHQ6ICcsIHJlc3BvbnNlKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuRXJyb3IoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlamVjdGlvbil7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSBlcnJvcjogJywgcmVqZWN0aW9uKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zdGF0dXMgPSByZWplY3Rpb24uc3RhdHVzO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zdGF0dXNUZXh0ID0gcmVqZWN0aW9uLnN0YXR1c1RleHQ7XHJcbiAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMzAwMCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEhpZGUgdGhlIFBvcCB1cCBub3RpZmljYXRpb24gbWFudWFsbHlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRzY29wZS5vbkNhbmNlbFBvcFVwID0gZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgICAgICAgICB0dXJuT2ZmTm90aWZpY2F0aW9ucygpO1xyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUuJHdhdGNoKCdtc2cnLCBmdW5jdGlvbihuZXdWYWx1ZSwgb2xkVmFsdWUpIHtcclxuICAgICAgICAgICAgICAgIGlmIChuZXdWYWx1ZSAmJiBuZXdWYWx1ZSAhPT0gJycpIHtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzVGV4dCA9IG5ld1ZhbHVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzID0gJHNjb3BlLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMjUwMCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICB9XVxyXG4gICAgfTtcclxufV0pO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzE0LzIwMTUuXHJcbiAqIEl0IGhhbmRsZXIgdGhlIGluZGV4IGZvciBhbnkgb2YgdGhlIGRpcmVjdGl2ZXMgYXZhaWxhYmxlXHJcbiAqL1xyXG5cclxucmVxdWlyZSgnLi9Ub29scy9Ub2FzdEhhbmRsZXIuanMnKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8xNy8yMDE1LlxyXG4gKi9cclxuXHJcbi8vIE1haW4gQW5ndWxhckpzIGNvbmZpZ3VyYXRpb25cclxucmVxdWlyZSgnLi9jb25maWcvQXBwLmpzJyk7XHJcblxyXG4vLyBIZWxwZXJzXHJcbnJlcXVpcmUoJy4vY29uZmlnL0FuZ3VsYXJQcm92aWRlckhlbHBlci5qcycpO1xyXG5cclxuLy8gRGlyZWN0aXZlc1xyXG5yZXF1aXJlKCcuL2RpcmVjdGl2ZXMvaW5kZXgnKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yLzIwMTUuXHJcbiAqIEhlYWRlciBDb250cm9sbGVyIG1hbmFnZSB0aGUgdmlldyBhdmFpbGFibGUgb24gdGhlIHN0YXRlLmRhdGFcclxuICogLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gKiBIZWFkZXIgQ29udHJvbGxlclxyXG4gKiBQYWdlIHRpdGxlICAgICAgICAgICAgICAgICAgICAgIEhvbWUgLT4gTGF5b3V0IC0gU3ViIExheW91dFxyXG4gKlxyXG4gKiBNb2R1bGUgQ29udHJvbGxlclxyXG4gKiBDb250ZW50XHJcbiAqIC0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIZWFkZXJDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2dcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG5cclxuICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICBpbnN0cnVjdGlvbjogJycsXHJcbiAgICAgICAgICAgIG1lbnU6IFtdXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hlYWRlciBDb250cm9sbGVyIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmVyaWZ5IGlmIHdlIGhhdmUgYSBtZW51IHRvIHNob3cgdG8gbWFkZSBpdCBhdmFpbGFibGUgdG8gdGhlIFZpZXdcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUhlYWRlcigpIHtcclxuICAgICAgICBpZiAodGhpcy5zdGF0ZSAmJiB0aGlzLnN0YXRlLiRjdXJyZW50ICYmIHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YSkge1xyXG4gICAgICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YS5wYWdlO1xyXG4gICAgICAgICAgICBkb2N1bWVudC50aXRsZSA9IHRoaXMucGFnZU1ldGFEYXRhLnRpdGxlO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIxLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgSGVhZGVyQ29udHJvbGxlciBmcm9tICcuL0hlYWRlckNvbnRyb2xsZXIuanMnO1xyXG5cclxudmFyIEhlYWRlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5IZWFkZXJNb2R1bGUnLCBbXSk7XHJcblxyXG5IZWFkZXJNb2R1bGUuY29udHJvbGxlcignSGVhZGVyQ29udHJvbGxlcicsIFsnJGxvZycsICckc3RhdGUnLCBIZWFkZXJDb250cm9sbGVyXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIZWFkZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTGljZW5zZUxpc3QgZnJvbSAnLi9saXN0L0xpY2Vuc2VMaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RMaWNlbnNlIGZyb20gJy4vcmVxdWVzdC9SZXF1ZXN0TGljZW5zZS5qcyc7XHJcbmltcG9ydCBDcmVhdGVkTGljZW5zZSBmcm9tICcuL2NyZWF0ZWQvQ3JlYXRlZExpY2Vuc2UuanMnO1xyXG5pbXBvcnQgTGljZW5zZURldGFpbCBmcm9tICcuL2RldGFpbC9MaWNlbnNlRGV0YWlsLmpzJztcclxuXHJcblxyXG52YXIgTGljZW5jZU1hbmFnZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uTGljZW5zZU1hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ2xpY2Vuc2VNYW5hZ2VyJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdsaWNlbnNlTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0FkbWluaXN0ZXIgTGljZW5zZXMnLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnQURNSU4nLCAnTElDRU5TRScsICdMSVNUJ119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvbGlzdC9MaWNlbnNlTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZUxpc3QgYXMgbGljZW5zZUxpc3QnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTGljZW5jZU1hbmFnZXJNb2R1bGUuc2VydmljZSgnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIExpY2Vuc2VNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTGljZW5jZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZUxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5MaWNlbmNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdSZXF1ZXN0TGljZW5zZScsIFsnJGxvZycsICdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgUmVxdWVzdExpY2Vuc2VdKTtcclxuTGljZW5jZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignQ3JlYXRlZExpY2Vuc2UnLCBbJyRsb2cnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgQ3JlYXRlZExpY2Vuc2VdKTtcclxuTGljZW5jZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZURldGFpbCcsIFsnJGxvZycsICdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAnJHVpYk1vZGFsJywgTGljZW5zZURldGFpbF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2VuY2VNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBDcmVhdGVkUmVxdWVzdExpY2Vuc2VDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5jbGllbnQgPSBwYXJhbXM7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlRGV0YWlsIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5MaWNlbnNlTW9kZWwgPSB7IH1cclxuICAgIH1cclxuXHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VMaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldExpY2Vuc2VMaXN0KCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VMaXN0IEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cImxpY2Vuc2VMaXN0Lm9uUmVxdWVzdE5ld0xpY2Vuc2UoKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBSZXF1ZXN0IE5ldyBMaWNlbnNlPC9idXR0b24+IDxkaXYgb25jbGljaz1cImxvYWRHcmlkQnVuZGxlTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2xpY2Vuc2VJZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibGljZW5zZUxpc3Qub25MaWNlbnNlRGV0YWlscygjPWxpY2Vuc2VJZCMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLWVkaXRcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2xpZW50JywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QnLCB0aXRsZTogJ1Byb2plY3QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NvbnRhY3RfZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlJywgdGl0bGU6ICdUeXBlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QnLCB0aXRsZTogJ01ldGhvZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc2VydmVyc190b2tlbnMnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2luY2VwdGlvbicsIHRpdGxlOiAnSW5jZXB0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uJywgdGl0bGU6ICdFeHBpcmF0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbnZpcm9ubWVudCcsIHRpdGxlOiAnRW52Lid9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnRlc3RTZXJ2aWNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvcmVxdWVzdC9SZXF1ZXN0TGljZW5zZS5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1JlcXVlc3RMaWNlbnNlIGFzIHJlcXVlc3RMaWNlbnNlJyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgaWQ6IDUwLCBuYW1lOiAnQWNtZSwgSW5jLicsIGVtYWlsOiAnYWNtZUBpbmMuY29tJyB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTmV3IExpY2Vuc2UgQ3JlYXRlZDogJywgbGljZW5zZSk7XHJcbiAgICAgICAgICAgIHRoaXMub25OZXdMaWNlbnNlQ3JlYXRlZCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEFmdGVyIGNsaWNraW5nIG9uIGVkaXQsIHdlIHJlZGlyZWN0IHRoZSB1c2VyIHRvIHRoZSBFZGl0aW9uIHNjcmVlbiBpbnN0ZWFkIG9mIG9wZW4gYSBkaWFsb2dcclxuICAgICAqIGR1IHRoZSBzaXplIG9mIHRoZSBpbnB1dHNcclxuICAgICAqL1xyXG4gICAgb25MaWNlbnNlRGV0YWlscyhsaWNlbnNlSWQpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPcGVuIERldGFpbHMgZm9yOiAnLCBsaWNlbnNlSWQpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2RldGFpbC9MaWNlbnNlRGV0YWlsLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZURldGFpbCBhcyBsaWNlbnNlRGV0YWlsJyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgbGljZW5zZUlkOiBsaWNlbnNlSWQgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuXHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdSZXF1ZXN0IENhbmNlbGVkLicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIG9uTmV3TGljZW5zZUNyZWF0ZWQoKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2NyZWF0ZWQvQ3JlYXRlZExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdDcmVhdGVkTGljZW5zZSBhcyBjcmVhdGVkTGljZW5zZScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGlkOiA1MCwgbmFtZTogJ0FjbWUsIEluYy4nLCBlbWFpbDogJ2FjbWVAaW5jLmNvbScgIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdCgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdExpY2Vuc2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0UHJvamVjdERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLm5ld0xpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgY29udGFjdEVtYWlsOiAnJyxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnRJZDogbnVsbCxcclxuICAgICAgICAgICAgcHJvamVjdElkOiBudWxsLFxyXG4gICAgICAgICAgICBjbGllbnQ6IHBhcmFtcyxcclxuICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogJydcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge2Vudmlyb25tZW50SWQ6IDEsIG5hbWU6ICdQcm9kdWN0aW9uJ30sXHJcbiAgICAgICAgICAgIHtlbnZpcm9ubWVudElkOiAyLCBuYW1lOiAnRGVtbyd9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBQcm9qZWN0IGRyb3Bkb3duIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnByb2plY3REYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7cHJvamVjdElkOiAxLCBuYW1lOiAnbi9hJ30sXHJcbiAgICAgICAgICAgIHtwcm9qZWN0SWQ6IDIsIG5hbWU6ICdEUiBSZWxvJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSB0aGUgU2VydmljZSBjYWxsIHRvIGdlbmVyYXRlIGEgbmV3IExpY2Vuc2UgcmVxdWVzdFxyXG4gICAgICovXHJcbiAgICBzYXZlTGljZW5zZVJlcXVlc3QoKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnTmV3IExpY2Vuc2UgUmVxdWVzdGVkOiAnLCB0aGlzLm5ld0xpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QodGhpcy5uZXdMaWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZU1hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHRlc3RTZXJ2aWNlKGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0TGljZW5zZUxpc3QoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIC8vIFByb2Nlc3MgTmV3IExpY2Vuc2UgZGF0YSBpZiBuZWNlc3NhcnkgKGFkZCwgcmVtb3ZlLCBldGMpXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBOb3RpY2VMaXN0IGZyb20gJy4vbGlzdC9Ob3RpY2VMaXN0LmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9Ob3RpY2VNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBFZGl0Tm90aWNlIGZyb20gJy4vZWRpdC9FZGl0Tm90aWNlLmpzJztcclxuXHJcblxyXG52YXIgTm90aWNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5Ob3RpY2VNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdub3RpY2VNYW5hZ2VyJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdub3RpY2VMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTm90aWNlIEFkbWluaXN0cmF0aW9uJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FETUlOJywgJ05PVElDRScsICdMSVNUJ119fSxcclxuICAgICAgICAgICAgdXJsOiAnL25vdGljZS9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2xpc3QvTm90aWNlTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTm90aWNlTGlzdCBhcyBub3RpY2VMaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuc2VydmljZSgnTm90aWNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgTm90aWNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTm90aWNlTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTm90aWNlTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0VkaXROb3RpY2UnLCBbJyRsb2cnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRWRpdE5vdGljZV0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgTm90aWNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRWRpdE5vdGljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbm90aWNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlID0gbm90aWNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLmFjdGlvbiA9IHBhcmFtcy5hY3Rpb247XHJcbiAgICAgICAgdGhpcy5hY3Rpb25UeXBlID0gcGFyYW1zLmFjdGlvblR5cGU7XHJcblxyXG4gICAgICAgIHRoaXMua2VuZG9FZGl0b3JUb29scyA9IFtcclxuICAgICAgICAgICAgJ2Zvcm1hdHRpbmcnLCAnY2xlYW5Gb3JtYXR0aW5nJyxcclxuICAgICAgICAgICAgJ2ZvbnROYW1lJywgJ2ZvbnRTaXplJyxcclxuICAgICAgICAgICAgJ2p1c3RpZnlMZWZ0JywgJ2p1c3RpZnlDZW50ZXInLCAnanVzdGlmeVJpZ2h0JywgJ2p1c3RpZnlGdWxsJyxcclxuICAgICAgICAgICAgJ2JvbGQnLFxyXG4gICAgICAgICAgICAnaXRhbGljJyxcclxuICAgICAgICAgICAgJ3ZpZXdIdG1sJ1xyXG4gICAgICAgIF07XHJcbiAgICAgICAgdGhpcy5nZXRUeXBlRGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsID0ge1xyXG4gICAgICAgICAgICB0aXRsZTogJycsXHJcbiAgICAgICAgICAgIHR5cGVJZDogbnVsbCxcclxuICAgICAgICAgICAgYWN0aXZlOiBmYWxzZSxcclxuICAgICAgICAgICAgbm90aWNlQm9keTogJydcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIGlmKHBhcmFtcy5ub3RpY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwgPSBwYXJhbXMubm90aWNlO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0VHlwZURhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy50eXBlRGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge3R5cGVJZDogMSwgbmFtZTogJ1ByZWxvZ2luJ30sXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDIsIG5hbWU6ICdQb3N0bG9naW4nfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIHRoZSBTZXJ2aWNlIGNhbGwgdG8gZ2VuZXJhdGUgYSBuZXcgTGljZW5zZSByZXF1ZXN0XHJcbiAgICAgKi9cclxuICAgIHNhdmVOb3RpY2UoKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbyh0aGlzLmFjdGlvbiArICcgTm90aWNlIFJlcXVlc3RlZDogJywgdGhpcy5lZGl0TW9kZWwpO1xyXG5cclxuICAgICAgICBpZih0aGlzLmFjdGlvbiA9PT0gdGhpcy5hY3Rpb25UeXBlLk5FVykge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmNyZWF0ZU5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIGlmKHRoaXMuYWN0aW9uID09PSB0aGlzLmFjdGlvblR5cGUuRURJVCkge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmVkaXROb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBub3RpY2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHtcclxuICAgICAgICAgICAgTkVXOiAnTmV3JyxcclxuICAgICAgICAgICAgRURJVDogJ0VkaXQnXHJcbiAgICAgICAgfVxyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlID0gbm90aWNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXRMaWNlbnNlTGlzdCgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cIm5vdGljZUxpc3Qub25FZGl0Q3JlYXRlTm90aWNlKG5vdGljZUxpc3QuYWN0aW9uVHlwZS5ORVcpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IENyZWF0ZSBOZXcgTm90aWNlPC9idXR0b24+IDxkaXYgb25jbGljaz1cImxvYWRHcmlkQnVuZGxlTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ25vdGljZUlkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdyYXdUZXh0JywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuRURJVCwgdGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tZWRpdFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0aXRsZScsIHRpdGxlOiAnVGl0bGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUnLCB0aXRsZTogJ1Byb2plY3QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2ZScsIHRpdGxlOiAnQWN0aXZlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0JywgdGl0bGU6ICdwcm9qZWN0J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlJywgdGl0bGU6ICdUeXBlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY2tub3dsZWRnZScsIHRpdGxlOiAnQWNrbm93bGVkZ2UnfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLnRlc3RTZXJ2aWNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBOb3RpY2VcclxuICAgICAqL1xyXG4gICAgb25FZGl0Q3JlYXRlTm90aWNlKGFjdGlvbiwgbm90aWNlKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9lZGl0L0VkaXROb3RpY2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdFZGl0Tm90aWNlIGFzIGVkaXROb3RpY2UnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IG5vdGljZSAmJiBub3RpY2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgYWN0aW9uOiBhY3Rpb24sIG5vdGljZTogbm90aWNlLmRhdGFJdGVtLCBhY3Rpb25UeXBlOiB0aGlzLmFjdGlvblR5cGV9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbyhhY3Rpb24gKyAnIE5vdGljZTogJywgbGljZW5zZSk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0TGljZW5zZUxpc3QoKSB7XHJcbiAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTm90aWNlTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgdGVzdFNlcnZpY2UoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldE5vdGljZU1vY2tVcCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0Tm90aWNlTGlzdChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0Tm90aWNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgY3JlYXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIC8vIFByb2Nlc3MgTmV3IExpY2Vuc2UgZGF0YSBpZiBuZWNlc3NhcnkgKGFkZCwgcmVtb3ZlLCBldGMpXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZWRpdE5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICAvLyBQcm9jZXNzIE5ldyBMaWNlbnNlIGRhdGEgaWYgbmVjZXNzYXJ5IChhZGQsIHJlbW92ZSwgZXRjKVxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZWRpdE5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcbmltcG9ydCBUYXNrTWFuYWdlclNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL1Rhc2tNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlckNvbnRyb2xsZXIgZnJvbSAnLi9saXN0L1Rhc2tNYW5hZ2VyQ29udHJvbGxlci5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlckVkaXQgZnJvbSAnLi9lZGl0L1Rhc2tNYW5hZ2VyRWRpdC5qcyc7XHJcblxyXG4vLyBEaXJlY3RpdmVzXHJcbmltcG9ydCBTVkdMb2FkZXIgZnJvbSAnLi4vLi4vZGlyZWN0aXZlcy9Tdmcvc3ZnTG9hZGVyLmpzJ1xyXG5cclxudmFyIFRhc2tNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLlRhc2tNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIpIHtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJ2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJDb250YWluZXIuaHRtbCcsXHJcbiAgICAgICAgY29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG4gICAgfTtcclxuXHJcbiAgICAkc3RhdGVQcm92aWRlclxyXG4gICAgICAgIC5zdGF0ZSgndGFza0xpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdNeSBUYXNrIE1hbmFnZXInLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnVGFzayBNYW5hZ2VyJ119fSxcclxuICAgICAgICAgICAgdXJsOiAnL3Rhc2svbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnYXBwLWpzL21vZHVsZXMvdGFza01hbmFnZXIvbGlzdC9UYXNrTWFuYWdlckNvbnRhaW5lci5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnVGFza01hbmFnZXJDb250cm9sbGVyIGFzIHRhc2tNYW5hZ2VyJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcblRhc2tNYW5hZ2VyTW9kdWxlLnNlcnZpY2UoJ3Rhc2tNYW5hZ2VyU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCBUYXNrTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcblRhc2tNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ1Rhc2tNYW5hZ2VyQ29udHJvbGxlcicsIFsnJGxvZycsICd0YXNrTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgVGFza01hbmFnZXJDb250cm9sbGVyXSk7XHJcblRhc2tNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ1Rhc2tNYW5hZ2VyRWRpdCcsIFsnJGxvZycsIFRhc2tNYW5hZ2VyRWRpdF0pO1xyXG5cclxuLy8gRGlyZWN0aXZlc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5kaXJlY3RpdmUoJ3N2Z0xvYWRlcicsIFNWR0xvYWRlcik7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBUYXNrTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMvMTEvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlckVkaXQge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHRhc2tNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcblxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjAvMjAxNS5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyQ29udHJvbGxlciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgdGFza01hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLm1vZHVsZSA9ICdUYXNrTWFuYWdlcic7XHJcbiAgICAgICAgdGhpcy50YXNrTWFuYWdlclNlcnZpY2UgPSB0YXNrTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy50YXNrR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmV2ZW50RGF0YVNvdXJjZSA9IFtdO1xyXG5cclxuICAgICAgICAvLyBJbml0IENsYXNzXHJcbiAgICAgICAgdGhpcy5nZXRFdmVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVGFza01hbmFnZXIgQ29udHJvbGxlciBJbnN0YW5jZWQnKTtcclxuXHJcbiAgICB9XHJcblxyXG4gICAgb3Blbk1vZGFsRGVtbygpIHtcclxuXHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnYXBwLWpzL21vZHVsZXMvdGFza01hbmFnZXIvZWRpdC9UYXNrTWFuYWdlckVkaXQuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdUYXNrTWFuYWdlckVkaXQnLFxyXG4gICAgICAgICAgICBzaXplOiAnbGcnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBpdGVtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBbJzEnLCdhMicsJ2dnJ107XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoc2VsZWN0ZWRJdGVtKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMuZGVidWcoc2VsZWN0ZWRJdGVtKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ01vZGFsIGRpc21pc3NlZCBhdDogJyArIG5ldyBEYXRlKCkpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy50YXNrR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGdyb3VwYWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgc29ydGFibGU6IHRydWUsXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW3tmaWVsZDogJ2FjdGlvbicsIHRpdGxlOiAnQWN0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0YXNrJywgdGl0bGU6ICdUYXNrJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdkZXNjcmlwdGlvbicsIHRpdGxlOiAnRGVzY3JpcHRpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Fzc2V0TmFtZScsIHRpdGxlOiAnQXNzZXQgTmFtZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzZXRUeXBlJywgdGl0bGU6ICdBc3NldCBUeXBlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd1cGRhdGVkJywgdGl0bGU6ICdVcGRhdGVkJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdkdWUnLCB0aXRsZTogJ0R1ZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3RhdHVzJywgdGl0bGU6ICdTdGF0dXMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Fzc2lnbmVkVG8nLCB0aXRsZTogJ0Fzc2lnbmVkIFRvJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0ZWFtJywgdGl0bGU6ICdUZWFtJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjYXRlZ29yeScsIHRpdGxlOiAnQ2F0ZWdvcnknfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N1YycsIHRpdGxlOiAnU3VjLid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc2NvcmUnLCB0aXRsZTogJ1Njb3JlJ31dLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnRhc2tNYW5hZ2VyU2VydmljZS50ZXN0U2VydmljZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGdldEV2ZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmV2ZW50RGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDEsIGV2ZW50TmFtZTogJ0FsbCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMiwgZXZlbnROYW1lOiAnQnVpbGRvdXQnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDMsIGV2ZW50TmFtZTogJ0RSLUVQJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiA0LCBldmVudE5hbWU6ICdNMS1QaHlzaWNhbCd9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICBvbkVycm9ySGFwcGVucygpIHtcclxuICAgICAgICB0aGlzLnRhc2tNYW5hZ2VyU2VydmljZS5mYWlsQ2FsbChmdW5jdGlvbiAoKSB7XHJcblxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAyMi8wNy8xNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIFJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gUmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVGFza01hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGZhaWxDYWxsKGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5SZXNvdXJjZVNlcnZpY2VIYW5kbGVyKCkuZ2V0U1ZHKCk7XHJcbiAgICB9XHJcblxyXG4gICAgdGVzdFNlcnZpY2UoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlRhc2tTZXJ2aWNlSGFuZGxlcigpLmdldEZlZWRzKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMy8yMDE1LlxyXG4gKiBJbXBsZW1lbnRzIFJYIE9ic2VydmFibGUgdG8gZGlzcG9zZSBhbmQgdHJhY2sgYmV0dGVyIGVhY2ggY2FsbCB0byB0aGUgc2VydmVyXHJcbiAqIFRoZSBPYnNlcnZlciBzdWJzY3JpYmUgYSBwcm9taXNlLlxyXG4gKi9cclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SGFuZGxlciB7XHJcbiAgICBjb25zdHJ1Y3RvcihyeCkge1xyXG4gICAgICAgIHRoaXMucnggPSByeDtcclxuICAgICAgICB0aGlzLnByb21pc2UgPSBbXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENhbGxlZCBmcm9tIFJlc3RTZXJ2aWNlSGFuZGxlci5zdWJzY3JpYmVSZXF1ZXN0XHJcbiAgICAgKiBpdCB2ZXJpZnkgdGhhdCB0aGUgY2FsbCBpcyBiZWluZyBkb25lIHRvIHRoZSBzZXJ2ZXIgYW5kIHJldHVybiBhIHByb21pc2VcclxuICAgICAqIEBwYXJhbSByZXF1ZXN0XHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgc3Vic2NyaWJlUmVxdWVzdChyZXF1ZXN0LCBjYWxsYmFjaykge1xyXG4gICAgICAgIHZhciByeE9ic2VydmFibGUgPSB0aGlzLnJ4Lk9ic2VydmFibGUuZnJvbVByb21pc2UocmVxdWVzdCk7XHJcbiAgICAgICAgLy8gVmVyaWZ5IGlzIG5vdCBhIGR1cGxpY2F0ZSBjYWxsXHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgdGhpcy5jYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBTdWJzY3JpYmUgdGhlIHJlcXVlc3RcclxuICAgICAgICB2YXIgcmVzdWx0U3Vic2NyaWJlID0gdGhpcy5hZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBjYWxsYmFjayk7XHJcbiAgICAgICAgaWYgKHJlc3VsdFN1YnNjcmliZSAmJiByZXN1bHRTdWJzY3JpYmUuaXNTdG9wcGVkKSB7XHJcbiAgICAgICAgICAgIC8vIEFuIGVycm9yIGhhcHBlbnMsIHRyYWNrZWQgYnkgSHR0cEludGVyY2VwdG9ySW50ZXJmYWNlXHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgYWRkU3Vic2NyaWJlKHJ4T2JzZXJ2YWJsZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSA9IHJ4T2JzZXJ2YWJsZS5zdWJzY3JpYmUoXHJcbiAgICAgICAgICAgIChyZXNwb25zZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIChlcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAvLyBOTy1PUCBTdWJzY3JpYmUgY29tcGxldGVkXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICByZXR1cm4gdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICB9XHJcblxyXG4gICAgY2FuY2VsUmVxdWVzdChyeE9ic2VydmFibGUpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgICAgIHJ4T2JzZXJ2YWJsZS5kaXNwb3NlKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpIHtcclxuICAgICAgICByZXR1cm4gKHJ4T2JzZXJ2YWJsZSAmJiByeE9ic2VydmFibGUuX3AgJiYgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF0pO1xyXG4gICAgfVxyXG5cclxuICAgIG9uU3Vic2NyaWJlZFN1Y2Nlc3MocmVzcG9uc2UsIHJ4T2JzZXJ2YWJsZSwgY2FsbGJhY2spIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKGNhbGxiYWNrKXtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKHJlc3BvbnNlLmRhdGEpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRocm93cyBpbW1lZGlhdGVseSBlcnJvciB3aGVuIHRoZSBwZXRpdGlvbiBjYWxsIGlzIHdyb25nXHJcbiAgICAgKiBvciB3aXRoIGEgZGVsYXkgaWYgdGhlIGNhbGwgaXMgdmFsaWRcclxuICAgICAqIEBwYXJhbSBlcnJvclxyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIG9uU3Vic2NyaWJlZEVycm9yKGVycm9yLCByeE9ic2VydmFibGUsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgICAgICBpZihjYWxsYmFjayl7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayh7fSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBSZXN0U2VydmljZUhhbmRsZXIgZnJvbSAnLi9SZXN0U2VydmljZUhhbmRsZXIuanMnO1xyXG5cclxudmFyIFJlc3RBUElNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uUmVzdEFQSU1vZHVsZScsW10pO1xyXG5cclxuUmVzdEFQSU1vZHVsZS5zZXJ2aWNlKCdSZXN0U2VydmljZUhhbmRsZXInLCBbJyRsb2cnLCAnJGh0dHAnLCAnJHJlc291cmNlJywgJ3J4JywgUmVzdFNlcnZpY2VIYW5kbGVyXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBSZXN0QVBJTW9kdWxlO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzA4LzE1LlxyXG4gKiBJdCBhYnN0cmFjdCBlYWNoIG9uZSBvZiB0aGUgZXhpc3RpbmcgY2FsbCB0byB0aGUgQVBJLCBpdCBzaG91bGQgb25seSBjb250YWlucyB0aGUgY2FsbCBmdW5jdGlvbnMgYW5kIHJlZmVyZW5jZVxyXG4gKiB0byB0aGUgY2FsbGJhY2ssIG5vIGxvZ2ljIGF0IGFsbC5cclxuICpcclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IFJlcXVlc3RIYW5kbGVyIGZyb20gJy4vUmVxdWVzdEhhbmRsZXIuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVzdFNlcnZpY2VIYW5kbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRodHRwLCAkcmVzb3VyY2UsIHJ4KSB7XHJcbiAgICAgICAgdGhpcy5yeCA9IHJ4O1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLmh0dHAgPSAkaHR0cDtcclxuICAgICAgICB0aGlzLnJlc291cmNlID0gJHJlc291cmNlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUhlYWRlcnMoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnUmVzdFNlcnZpY2UgTG9hZGVkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUhlYWRlcnMoKSB7XHJcbiAgICAgICAgdGhpcy5odHRwLmRlZmF1bHRzLmhlYWRlcnMucG9zdFsnQ29udGVudC1UeXBlJ10gPSAnYXBwbGljYXRpb24veC13d3ctZm9ybS11cmxlbmNvZGVkJztcclxuICAgIH1cclxuXHJcbiAgICBUYXNrU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgZ2V0RmVlZHM6IChjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCd0ZXN0L21vY2t1cERhdGEvVGFza01hbmFnZXIvdGFza01hbmFnZXJMaXN0Lmpzb24nKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBsaWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9jb29rYm9vay9yZWNpcGUvbGlzdD9hcmNoaXZlZD1uJmNvbnRleHQ9QWxsJnJhbmQ9b0RGcUxUcGJaUmozOEFXJyksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0TGljZW5zZTogKGNhbGxiYWNrKSA9PiB7IC8vIE1vY2t1cCBEYXRhIGZvciB0ZXN0aW5nIHNlZSB1cmxcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZU1hbmFnZXIvbGljZW5zZU1hbmFnZXJMaXN0Lmpzb24nKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdDogKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLnBvc3QoJy4uL3Rlc3QvbW9ja3VwRGF0YS9MaWNlbnNlTWFuYWdlci9saWNlbnNlTWFuYWdlckxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIG5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXROb3RpY2VMaXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHsgLy8gcmVhbCB3cyBleGFtcGxlXHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvY29va2Jvb2svcmVjaXBlL2xpc3Q/YXJjaGl2ZWQ9biZjb250ZXh0PUFsbCZyYW5kPW9ERnFMVHBiWlJqMzhBVycpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldE5vdGljZU1vY2tVcDogKGNhbGxiYWNrKSA9PiB7IC8vIE1vY2t1cCBEYXRhIGZvciB0ZXN0aW5nIHNlZSB1cmxcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi90ZXN0L21vY2t1cERhdGEvTm90aWNlTWFuYWdlci9ub3RpY2VNYW5hZ2VyTGlzdC5qc29uJyksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY3JlYXRlTm90aWNlOiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL05vdGljZU1hbmFnZXIvbm90aWNlTWFuYWdlckxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGVkaXROb3RpY2U6IChkYXRhLCBjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTm90aWNlTWFuYWdlci9ub3RpY2VNYW5hZ2VyTGlzdC5qc29uJywgZGF0YSksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIEVTNiBJbnRlcmNlcHRvciBjYWxscyBpbm5lciBtZXRob2RzIGluIGEgZ2xvYmFsIHNjb3BlLCB0aGVuIHRoZSBcInRoaXNcIiBpcyBiZWluZyBsb3N0XHJcbiAqIGluIHRoZSBkZWZpbml0aW9uIG9mIHRoZSBDbGFzcyBmb3IgaW50ZXJjZXB0b3JzIG9ubHlcclxuICogVGhpcyBpcyBhIGludGVyZmFjZSB0aGF0IHRha2UgY2FyZSBvZiB0aGUgaXNzdWUuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IC8qIGludGVyZmFjZSovIGNsYXNzIEh0dHBJbnRlcmNlcHRvciB7XHJcbiAgICBjb25zdHJ1Y3RvcihtZXRob2RUb0JpbmQpIHtcclxuICAgICAgICAvLyBJZiBub3QgbWV0aG9kIHRvIGJpbmQsIHdlIGFzc3VtZSBvdXIgaW50ZXJjZXB0b3IgaXMgdXNpbmcgYWxsIHRoZSBpbm5lciBmdW5jdGlvbnNcclxuICAgICAgICBpZighbWV0aG9kVG9CaW5kKSB7XHJcbiAgICAgICAgICAgIFsncmVxdWVzdCcsICdyZXF1ZXN0RXJyb3InLCAncmVzcG9uc2UnLCAncmVzcG9uc2VFcnJvciddXHJcbiAgICAgICAgICAgICAgICAuZm9yRWFjaCgobWV0aG9kKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpc1ttZXRob2RdKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXNbbWV0aG9kXSA9IHRoaXNbbWV0aG9kXS5iaW5kKHRoaXMpO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIC8vIG1ldGhvZFRvQmluZCByZWZlcmVuY2UgdG8gYSBzaW5nbGUgY2hpbGQgY2xhc3NcclxuICAgICAgICAgICAgdGhpc1ttZXRob2RUb0JpbmRdID0gdGhpc1ttZXRob2RUb0JpbmRdLmJpbmQodGhpcyk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIFVzZSB0aGlzIG1vZHVsZSB0byBtb2RpZnkgYW55dGhpbmcgcmVsYXRlZCB0byB0aGUgSGVhZGVycyBhbmQgUmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5cclxuXHJcbnZhciBIVFRQTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkhUVFBNb2R1bGUnLCBbJ25nUmVzb3VyY2UnXSkuY29uZmlnKFsnJGh0dHBQcm92aWRlcicsIGZ1bmN0aW9uKCRodHRwUHJvdmlkZXIpe1xyXG5cclxuICAgIC8vaW5pdGlhbGl6ZSBnZXQgaWYgbm90IHRoZXJlXHJcbiAgICBpZiAoISRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXQpIHtcclxuICAgICAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0ID0ge307XHJcbiAgICB9XHJcblxyXG4gICAgLy9EaXNhYmxlIElFIGFqYXggcmVxdWVzdCBjYWNoaW5nXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydJZi1Nb2RpZmllZC1TaW5jZSddID0gJ01vbiwgMjYgSnVsIDE5OTcgMDU6MDA6MDAgR01UJztcclxuICAgIC8vIGV4dHJhXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydDYWNoZS1Db250cm9sJ10gPSAnbm8tY2FjaGUnO1xyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnUHJhZ21hJ10gPSAnbm8tY2FjaGUnO1xyXG5cclxuXHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlcXVlc3RcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlc3BvbnNlXHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcblxyXG5cclxufV0pO1xyXG5cclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIVFRQTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIEl0IGltcGxlbWVudCBhbiBhYnN0cmFjdCBjYWxsIHRvIEhUVFAgSW50ZXJjZXB0b3JzIHRvIG1hbmFnZSBlcnJvciBoYW5kbGVyXHJcbiAqIFNvbWV0aW1lcyBhIHJlcXVlc3QgY2FuJ3QgYmUgc2VudCBvciBpdCBpcyByZWplY3RlZCBieSBhbiBpbnRlcmNlcHRvci5cclxuICogUmVxdWVzdCBlcnJvciBpbnRlcmNlcHRvciBjYXB0dXJlcyByZXF1ZXN0cyB0aGF0IGhhdmUgYmVlbiBjYW5jZWxlZCBieSBhIHByZXZpb3VzIHJlcXVlc3QgaW50ZXJjZXB0b3IuXHJcbiAqIEl0IGNhbiBiZSB1c2VkIGluIG9yZGVyIHRvIHJlY292ZXIgdGhlIHJlcXVlc3QgYW5kIHNvbWV0aW1lcyB1bmRvIHRoaW5ncyB0aGF0IGhhdmUgYmVlbiBzZXQgdXAgYmVmb3JlIGEgcmVxdWVzdCxcclxuICogbGlrZSByZW1vdmluZyBvdmVybGF5cyBhbmQgbG9hZGluZyBpbmRpY2F0b3JzLCBlbmFibGluZyBidXR0b25zIGFuZCBmaWVsZHMgYW5kIHNvIG9uLlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3JlcXVlc3RFcnJvcicpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdEVycm9yKHJlamVjdGlvbikge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vaWYgKGNhblJlY292ZXIocmVqZWN0aW9uKSkge1xyXG4gICAgICAgIC8vICAgIHJldHVybiByZXNwb25zZU9yTmV3UHJvbWlzZVxyXG4gICAgICAgIC8vfVxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnEucmVqZWN0KHJlamVjdGlvbik7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuRXJyb3IoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSXQgaW1wbGVtZW50IGFuIGFic3RyYWN0IGNhbGwgdG8gSFRUUCBJbnRlcmNlcHRvcnMgdG8gbWFuYWdlIG9ubHkgcmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXF1ZXN0Jyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdChjb25maWcpIHtcclxuICAgICAgICAvLyBXZSBjYW4gYWRkIGhlYWRlcnMgaWYgb24gdGhlIGluY29taW5nIHJlcXVlc3QgbWFkZSBpdCB3ZSBoYXZlIHRoZSB0b2tlbiBpbnNpZGVcclxuICAgICAgICAvLyBkZWZpbmVkIGJ5IHNvbWUgY29uZGl0aW9uc1xyXG4gICAgICAgIC8vY29uZmlnLmhlYWRlcnNbJ3gtc2Vzc2lvbi10b2tlbiddID0gbXkudG9rZW47XHJcblxyXG4gICAgICAgIGNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KGNvbmZpZyk7XHJcblxyXG4gICAgICAgIHJldHVybiBjb25maWcgfHwgdGhpcy5xLndoZW4oY29uZmlnKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXF1ZXN0KCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIElmIGJhY2tlbmQgY2FsbCBmYWlscyBvciBpdCBtaWdodCBiZSByZWplY3RlZCBieSBhIHJlcXVlc3QgaW50ZXJjZXB0b3Igb3IgYnkgYSBwcmV2aW91cyByZXNwb25zZSBpbnRlcmNlcHRvcjtcclxuICogSW4gdGhvc2UgY2FzZXMsIHJlc3BvbnNlIGVycm9yIGludGVyY2VwdG9yIGNhbiBoZWxwIHVzIHRvIHJlY292ZXIgdGhlIGJhY2tlbmQgY2FsbC5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZUVycm9yJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzcG9uc2VFcnJvcihyZWplY3Rpb24pIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvL2lmIChjYW5SZWNvdmVyKHJlamVjdGlvbikpIHtcclxuICAgICAgICAvLyAgICByZXR1cm4gcmVzcG9uc2VPck5ld1Byb21pc2VcclxuICAgICAgICAvLyB9XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMucS5yZWplY3QocmVqZWN0aW9uKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5FcnJvcigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBUaGlzIG1ldGhvZCBpcyBjYWxsZWQgcmlnaHQgYWZ0ZXIgJGh0dHAgcmVjZWl2ZXMgdGhlIHJlc3BvbnNlIGZyb20gdGhlIGJhY2tlbmQsXHJcbiAqIHNvIHlvdSBjYW4gbW9kaWZ5IHRoZSByZXNwb25zZSBhbmQgbWFrZSBvdGhlciBhY3Rpb25zLiBUaGlzIGZ1bmN0aW9uIHJlY2VpdmVzIGEgcmVzcG9uc2Ugb2JqZWN0IGFzIGEgcGFyYW1ldGVyXHJcbiAqIGFuZCBoYXMgdG8gcmV0dXJuIGEgcmVzcG9uc2Ugb2JqZWN0IG9yIGEgcHJvbWlzZS4gVGhlIHJlc3BvbnNlIG9iamVjdCBpbmNsdWRlc1xyXG4gKiB0aGUgcmVxdWVzdCBjb25maWd1cmF0aW9uLCBoZWFkZXJzLCBzdGF0dXMgYW5kIGRhdGEgdGhhdCByZXR1cm5lZCBmcm9tIHRoZSBiYWNrZW5kLlxyXG4gKiBSZXR1cm5pbmcgYW4gaW52YWxpZCByZXNwb25zZSBvYmplY3Qgb3IgcHJvbWlzZSB0aGF0IHdpbGwgYmUgcmVqZWN0ZWQsIHdpbGwgbWFrZSB0aGUgJGh0dHAgY2FsbCB0byBmYWlsLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZScpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXNwb25zZShyZXNwb25zZSkge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBzdWNjZXNzXHJcblxyXG4gICAgICAgIHJlc3BvbnNlLmNvbmZpZy5yZXNwb25zZVRpbWVzdGFtcCA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZXNwb25zZSk7XHJcbiAgICAgICAgcmV0dXJuIHJlc3BvbnNlIHx8IHRoaXMucS53aGVuKHJlc3BvbnNlKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXNwb25zZSgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG59XHJcblxyXG4iXX0=
