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

TDSTM.getConvertedDateFormat = function (dateString, userDTFormat, timeZone) {
    'use strict';

    var timeString = '';
    if (dateString) {
        if (timeZone === null) {
            timeZone = 'GMT';
        }
        var format = 'MM/DD/YYYY';
        if (userDTFormat === 'DD/MM/YYYY') {
            format = 'DD/MM/YYYY';
        }
        // Convert zulu datetime to a specific timezone/format
        timeString = moment(dateString).tz(timeZone).format(format);
    }
    return timeString;
};

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

var _LicenseAdminModule = require('../modules/licenseAdmin/LicenseAdminModule.js');

var _LicenseAdminModule2 = _interopRequireDefault(_LicenseAdminModule);

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
require('ngclipboard');
require('ui-router');
require('rx-angular');
require('api-check');
require('angular-formly');
require('angular-formly-templates-bootstrap');

// Modules


var ProviderCore = {};

var TDSTM = angular.module('TDSTM', ['ngSanitize', 'ngResource', 'ngAnimate', 'pascalprecht.translate', // 'angular-translate'
'ui.router', 'ngclipboard', 'kendo.directives', 'rx', 'formly', 'formlyBootstrap', 'ui.bootstrap', _HTTPModule2.default.name, _RestAPIModule2.default.name, _HeaderModule2.default.name, _TaskManagerModule2.default.name, _LicenseAdminModule2.default.name, _LicenseManagerModule2.default.name, _NoticeManagerModule2.default.name]).config(['$logProvider', '$rootScopeProvider', '$compileProvider', '$controllerProvider', '$provide', '$httpProvider', '$translateProvider', '$translatePartialLoaderProvider', '$urlRouterProvider', '$locationProvider', function ($logProvider, $rootScopeProvider, $compileProvider, $controllerProvider, $provide, $httpProvider, $translateProvider, $translatePartialLoaderProvider, $urlRouterProvider, $locationProvider) {

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
}]).run(['$transitions', '$http', '$log', '$location', '$q', 'UserPreferencesService', function ($transitions, $http, $log, $location, $q, userPreferencesService) {
        $log.debug('Configuration deployed');

        $transitions.onBefore({}, function ($state, $transition$) {
                var defer = $q.defer();

                userPreferencesService.getTimeZoneConfiguration(function () {
                        defer.resolve();
                });

                return defer.promise;
        });
}]);

// we mapped the Provider Core list (compileProvider, controllerProvider, provideService, httpProvider) to reuse after on fly
TDSTM.ProviderCore = ProviderCore;

module.exports = TDSTM;

},{"../modules/header/HeaderModule.js":9,"../modules/licenseAdmin/LicenseAdminModule.js":10,"../modules/licenseManager/LicenseManagerModule.js":18,"../modules/noticeManager/NoticeManagerModule.js":23,"../modules/taskManager/TaskManagerModule.js":27,"../services/RestAPI/RestAPIModule.js":33,"../services/http/HTTPModule.js":37,"angular":"angular","angular-animate":"angular-animate","angular-formly":"angular-formly","angular-formly-templates-bootstrap":"angular-formly-templates-bootstrap","angular-mocks":"angular-mocks","angular-resource":"angular-resource","angular-sanitize":"angular-sanitize","angular-translate":"angular-translate","angular-translate-loader-partial":"angular-translate-loader-partial","angular-ui-bootstrap":"angular-ui-bootstrap","api-check":"api-check","ngclipboard":"ngclipboard","rx-angular":"rx-angular","ui-router":"ui-router"}],3:[function(require,module,exports){
'use strict';

/**
 * Created by Jorge Morayta on 12/14/2015.
 * It handler the index for any of the directives available
 */

require('./tools/ToastHandler.js');
require('./tools/ModalWindowActivation.js');

},{"./tools/ModalWindowActivation.js":4,"./tools/ToastHandler.js":5}],4:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 30/10/2016.
 * Listen to Modal Window to make any modal window draggabble
 *
 */
'use strict';

var TDSTM = require('../../config/App.js');

TDSTM.createDirective('modalRender', ['$log', function ($log) {
    $log.debug('ModalWindowActivation loaded');
    return {
        restrict: 'EA',
        link: function link() {
            $('.modal-dialog').draggable({
                handle: '.modal-header'
            });
        }
    };
}]);

},{"../../config/App.js":2}],5:[function(require,module,exports){
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
        templateUrl: '../app-js/directives/tools/ToastHandler.html',
        restrict: 'E',
        controller: ['$scope', '$rootScope', function ($scope, $rootScope) {
            $scope.alert = {
                success: {
                    show: false,
                    status: '',
                    statusText: '',
                    time: 2000
                },
                danger: {
                    show: false,
                    status: '',
                    statusText: '',
                    time: 4000
                },
                info: {
                    show: false,
                    status: '',
                    statusText: '',
                    time: 2000
                },
                warning: {
                    show: false,
                    status: '',
                    statusText: '',
                    time: 4000
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

                if (response && response.headers && response.headers('x-login-url')) {
                    window.location.href = response.headers('x-login-url');
                }
            });

            HTTPResponseErrorHandlerInterceptor.listenError().then(null, null, function (rejection) {
                $log.debug('Response error: ', rejection);
                $scope.progress.show = false;
                $scope.alert.danger.show = true;
                $scope.alert.danger.status = rejection.status;
                $scope.alert.danger.statusText = rejection.statusText;
                $scope.alert.danger.errors = rejection.data.errors;
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
            $rootScope.$on('broadcast-msg', function (event, args) {
                $log.debug('broadcast-msg executed');
                $scope.alert[args.type].show = true;
                $scope.alert[args.type].statusText = args.text;
                $scope.alert[args.type].status = null;
                $timeout(turnOffNotifications, $scope.alert[args.type].time);
                $scope.$apply(); // rootScope and watch exclude the apply and needs the next cycle to run
            });

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

},{"../../config/App.js":2}],6:[function(require,module,exports){
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

},{"./config/AngularProviderHelper.js":1,"./config/App.js":2,"./directives/index":3}],7:[function(require,module,exports){
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

var DialogAction = function () {
    function DialogAction($log, $uibModal, $uibModalInstance, params) {
        _classCallCheck(this, DialogAction);

        this.uibModal = $uibModal;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;

        this.title = params.title;
        this.message = params.message;
    }
    /**
     * Acccept and Confirm
     */

    _createClass(DialogAction, [{
        key: 'confirmAction',
        value: function confirmAction() {
            this.uibModalInstance.close();
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

    return DialogAction;
}();

exports.default = DialogAction;

},{}],8:[function(require,module,exports){
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

},{}],9:[function(require,module,exports){
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

var _DialogAction = require('../dialogAction/DialogAction.js');

var _DialogAction2 = _interopRequireDefault(_DialogAction);

function _interopRequireDefault(obj) {
  return obj && obj.__esModule ? obj : { default: obj };
}

var HeaderModule = _angular2.default.module('TDSTM.HeaderModule', []);

HeaderModule.controller('HeaderController', ['$log', '$state', _HeaderController2.default]);

// Modal - Controllers
HeaderModule.controller('DialogAction', ['$log', '$uibModal', '$uibModalInstance', 'params', _DialogAction2.default]);

/*
 * Filter change the date into a proper format timezone date
 */
HeaderModule.filter('convertDateIntoTimeZone', ['UserPreferencesService', function (userPreferencesService) {
  return function (dateString) {
    return userPreferencesService.getConvertedDateIntoTimeZone(dateString);
  };
}]);

HeaderModule.filter('convertDateTimeIntoTimeZone', ['UserPreferencesService', function (userPreferencesService) {
  return function (dateString) {
    return userPreferencesService.getConvertedDateTimeIntoTimeZone(dateString);
  };
}]);

exports.default = HeaderModule;

},{"../dialogAction/DialogAction.js":7,"./HeaderController.js":8,"angular":"angular"}],10:[function(require,module,exports){
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

var _LicenseAdminList = require('./list/LicenseAdminList.js');

var _LicenseAdminList2 = _interopRequireDefault(_LicenseAdminList);

var _LicenseAdminService = require('./service/LicenseAdminService.js');

var _LicenseAdminService2 = _interopRequireDefault(_LicenseAdminService);

var _RequestLicense = require('./request/RequestLicense.js');

var _RequestLicense2 = _interopRequireDefault(_RequestLicense);

var _CreatedLicense = require('./created/CreatedLicense.js');

var _CreatedLicense2 = _interopRequireDefault(_CreatedLicense);

var _ApplyLicenseKey = require('./applyLicenseKey/ApplyLicenseKey.js');

var _ApplyLicenseKey2 = _interopRequireDefault(_ApplyLicenseKey);

var _ManuallyRequest = require('./manuallyRequest/ManuallyRequest.js');

var _ManuallyRequest2 = _interopRequireDefault(_ManuallyRequest);

var _LicenseDetail = require('./detail/LicenseDetail.js');

var _LicenseDetail2 = _interopRequireDefault(_LicenseDetail);

function _interopRequireDefault(obj) {
		return obj && obj.__esModule ? obj : { default: obj };
}

var LicenseAdminModule = _angular2.default.module('TDSTM.LicenseAdminModule', [_uiRouter2.default]).config(['$stateProvider', '$translatePartialLoaderProvider', '$locationProvider', function ($stateProvider, $translatePartialLoaderProvider, $locationProvider) {

		$translatePartialLoaderProvider.addPart('licenseAdmin');

		// Define a generic header for the entire module, or it can be changed for each instance.
		var header = {
				templateUrl: '../app-js/modules/header/HeaderView.html',
				controller: 'HeaderController as header'
		};

		$stateProvider.state('licenseAdminList', {
				data: { page: { title: 'Administer Licenses', instruction: '', menu: ['Admin', 'License', 'List'] } },
				url: '/license/admin/list',
				views: {
						'headerView@': header,
						'bodyView@': {
								templateUrl: '../app-js/modules/licenseAdmin/list/LicenseAdminList.html',
								controller: 'LicenseAdminList as licenseAdminList'
						}
				}
		});
}]);

// Services
LicenseAdminModule.service('LicenseAdminService', ['$log', 'RestServiceHandler', '$rootScope', _LicenseAdminService2.default]);

// Controllers
LicenseAdminModule.controller('LicenseAdminList', ['$log', '$state', 'LicenseAdminService', '$uibModal', _LicenseAdminList2.default]);

// Modal - Controllers
LicenseAdminModule.controller('RequestLicense', ['$log', '$scope', 'LicenseAdminService', '$uibModal', '$uibModalInstance', _RequestLicense2.default]);
LicenseAdminModule.controller('CreatedLicense', ['$log', '$uibModalInstance', 'params', _CreatedLicense2.default]);
LicenseAdminModule.controller('ApplyLicenseKey', ['$log', '$scope', 'LicenseAdminService', '$uibModal', '$uibModalInstance', 'params', _ApplyLicenseKey2.default]);
LicenseAdminModule.controller('ManuallyRequest', ['$log', '$scope', 'LicenseAdminService', '$uibModalInstance', 'params', _ManuallyRequest2.default]);
LicenseAdminModule.controller('LicenseDetail', ['$log', 'LicenseAdminService', '$uibModal', '$uibModalInstance', 'params', _LicenseDetail2.default]);

/*
 * Filter to URL Encode text for the 'mailto'
 */
LicenseAdminModule.filter('escapeURLEncoding', function () {
		return function (text) {
				if (text) {
						text = encodeURI(text);
				}
				return text;
		};
});

exports.default = LicenseAdminModule;

},{"./applyLicenseKey/ApplyLicenseKey.js":11,"./created/CreatedLicense.js":12,"./detail/LicenseDetail.js":13,"./list/LicenseAdminList.js":14,"./manuallyRequest/ManuallyRequest.js":15,"./request/RequestLicense.js":16,"./service/LicenseAdminService.js":17,"angular":"angular","ui-router":"ui-router"}],11:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/28/2016.
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

var _FormValidator2 = require('../../utils/form/FormValidator.js');

var _FormValidator3 = _interopRequireDefault(_FormValidator2);

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

var ApplyLicenseKey = function (_FormValidator) {
    _inherits(ApplyLicenseKey, _FormValidator);

    function ApplyLicenseKey($log, $scope, licenseAdminService, $uibModal, $uibModalInstance, params) {
        _classCallCheck(this, ApplyLicenseKey);

        var _this = _possibleConstructorReturn(this, (ApplyLicenseKey.__proto__ || Object.getPrototypeOf(ApplyLicenseKey)).call(this, $log, $scope, $uibModal, $uibModalInstance));

        _this.licenseAdminService = licenseAdminService;
        _this.uibModalInstance = $uibModalInstance;

        _this.licenseModel = {
            id: params.license.id,
            key: params.license.key
        };
        _this.saveForm(_this.licenseModel);
        return _this;
    }

    /**
     * Execute and validate the Key is correct
     */

    _createClass(ApplyLicenseKey, [{
        key: 'applyKey',
        value: function applyKey() {
            var _this2 = this;

            if (this.isDirty()) {
                this.licenseAdminService.applyLicense(this.licenseModel, function (data) {
                    _this2.uibModalInstance.close(data);
                }, function (data) {
                    _this2.uibModalInstance.close(data);
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

    return ApplyLicenseKey;
}(_FormValidator3.default);

exports.default = ApplyLicenseKey;

},{"../../utils/form/FormValidator.js":31}],12:[function(require,module,exports){
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

var CreatedRequestLicense = function () {
    function CreatedRequestLicense($log, $uibModalInstance, params) {
        _classCallCheck(this, CreatedRequestLicense);

        this.uibModalInstance = $uibModalInstance;
        this.client = params;
    }

    /**
     * Dismiss the dialog, no action necessary
     */

    _createClass(CreatedRequestLicense, [{
        key: 'cancelCloseDialog',
        value: function cancelCloseDialog() {
            this.uibModalInstance.dismiss('cancel');
        }
    }]);

    return CreatedRequestLicense;
}();

exports.default = CreatedRequestLicense;

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

var LicenseDetail = function () {
		function LicenseDetail($log, licenseAdminService, $uibModal, $uibModalInstance, params) {
				_classCallCheck(this, LicenseDetail);

				this.licenseAdminService = licenseAdminService;
				this.uibModalInstance = $uibModalInstance;
				this.uibModal = $uibModal;
				this.log = $log;
				this.licenseModel = {
						method: {
								name: params.license.method.name,
								max: params.license.method.max
						},
						projectName: params.license.project.name,
						clientName: params.license.client.name,
						email: params.license.email,
						toEmail: params.license.toEmail,
						environment: params.license.environment,
						inception: params.license.requestDate,
						expiration: params.license.expirationDate,
						requestNote: params.license.requestNote,
						active: params.license.status === 'ACTIVE',
						id: params.license.id,
						replaced: params.license.replaced,
						encryptedDetail: params.license.encryptedDetail,
						applied: false
				};

				this.prepareMethodOptions();
		}

		_createClass(LicenseDetail, [{
				key: 'prepareMethodOptions',
				value: function prepareMethodOptions() {
						this.methodOptions = [{
								name: 'MAX_SERVERS',
								text: 'Servers'
						}, {
								name: 'TOKEN',
								text: 'Tokens'
						}, {
								name: 'CUSTOM',
								text: 'Custom'
						}];
				}

				/**
     * The user apply and server should validate the key is correct
     */

		}, {
				key: 'applyLicenseKey',
				value: function applyLicenseKey() {
						var _this = this;

						var modalInstance = this.uibModal.open({
								animation: true,
								templateUrl: '../app-js/modules/licenseAdmin/applyLicenseKey/ApplyLicenseKey.html',
								controller: 'ApplyLicenseKey as applyLicenseKey',
								size: 'md',
								resolve: {
										params: function params() {
												return { license: _this.licenseModel };
										}
								}
						});

						modalInstance.result.then(function (data) {
								_this.licenseModel.applied = data.success;
								if (data.success) {
										_this.licenseModel.active = data.success;
										_this.uibModalInstance.close({ id: _this.licenseModel.id, updated: true });
								}
						});
				}

				/**
     * Opens a dialog and allow the user to manually send the request or copy the encripted code
     */

		}, {
				key: 'manuallyRequest',
				value: function manuallyRequest() {
						var _this2 = this;

						var modalInstance = this.uibModal.open({
								animation: true,
								templateUrl: '../app-js/modules/licenseAdmin/manuallyRequest/ManuallyRequest.html',
								controller: 'ManuallyRequest as manuallyRequest',
								size: 'md',
								resolve: {
										params: function params() {
												return { license: _this2.licenseModel };
										}
								}
						});

						modalInstance.result.then(function () {});
				}

				/**
     * If by some reason the License was not applied at first time, this will do a request for it
     */

		}, {
				key: 'resubmitLicenseRequest',
				value: function resubmitLicenseRequest() {
						this.licenseAdminService.resubmitLicenseRequest(this.licenseModel, function (data) {});
				}
		}, {
				key: 'deleteLicense',
				value: function deleteLicense() {
						var _this3 = this;

						var modalInstance = this.uibModal.open({
								animation: true,
								templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
								controller: 'DialogAction as dialogAction',
								size: 'sm',
								resolve: {
										params: function params() {
												return { title: 'Confirmation Required', message: 'Are you sure you want to delete it? This action cannot be undone.' };
										}
								}
						});

						modalInstance.result.then(function () {
								_this3.licenseAdminService.deleteLicense(_this3.licenseModel, function (data) {
										_this3.uibModalInstance.close(data);
								});
						});
				}

				/**
     * Dismiss the dialog, no action necessary
     */

		}, {
				key: 'cancelCloseDialog',
				value: function cancelCloseDialog() {
						if (this.licenseModel.applied) {
								this.uibModalInstance.close();
						}
						this.uibModalInstance.dismiss('cancel');
				}
		}]);

		return LicenseDetail;
}();

exports.default = LicenseDetail;

},{}],14:[function(require,module,exports){
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

var LicenseAdminList = function () {
    function LicenseAdminList($log, $state, licenseAdminService, $uibModal) {
        _classCallCheck(this, LicenseAdminList);

        this.log = $log;
        this.state = $state;
        this.licenseGrid = {};
        this.licenseGridOptions = {};
        this.licenseAdminService = licenseAdminService;
        this.uibModal = $uibModal;
        this.openLastLicenseId = 0;

        this.getDataSource();
        this.log.debug('LicenseAdminList Instanced');
    }

    _createClass(LicenseAdminList, [{
        key: 'getDataSource',
        value: function getDataSource() {
            var _this = this;

            this.licenseGridOptions = {
                toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseAdminList.onRequestNewLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Request New License</button> <div ng-click="licenseAdminList.reloadLicenseAdminList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                pageable: {
                    refresh: true,
                    pageSizes: true,
                    buttonCount: 5,
                    pageSize: 20
                },
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project', template: '<span style="text-transform: capitalize;">#=((data.project && data.project.name)? data.project.name.toLowerCase(): "" )#</span>' }, { field: 'email', title: 'Contact Email' }, { field: 'status', title: 'Status', template: '<span style="text-transform: capitalize;">#=((data.status)? data.status.toLowerCase(): "" )#</span>' }, { field: 'type.name', title: 'Type', template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#' }, { field: 'method.name', title: 'Method', template: '<span style="text-transform: capitalize;">#=((data.method && data.method.name)? data.method.name.toLowerCase(): "" )#</span>' }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'requestDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.activationDate | convertDateIntoTimeZone }}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.expirationDate | convertDateIntoTimeZone }}' }, { field: 'environment', title: 'Environment', template: '<span style="text-transform: capitalize;">#=((data.environment)? data.environment.toLowerCase(): "" )#</span>' }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            _this.licenseAdminService.getLicenseList(function (data) {
                                e.success(data);
                            });
                        }
                    },
                    sort: {
                        field: 'project.name',
                        dir: 'asc'
                    },
                    change: function change(e) {
                        // We are coming from a new imported request license
                        if (_this.openLastLicenseId !== 0 && _this.licenseGrid.dataSource._data) {
                            var lastLicense = _this.licenseGrid.dataSource._data.find(function (license) {
                                return license.id === _this.openLastLicenseId;
                            });

                            _this.openLastLicenseId = 0;

                            if (lastLicense) {
                                _this.onLicenseDetails(lastLicense);
                            }
                        }
                    }
                },
                sortable: true,
                filterable: {
                    extra: false
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
                templateUrl: '../app-js/modules/licenseAdmin/request/RequestLicense.html',
                controller: 'RequestLicense as requestLicense',
                size: 'md'
            });

            modalInstance.result.then(function (license) {
                _this2.log.info('New License Created: ', license);
                _this2.onNewLicenseCreated(license);
                _this2.reloadLicenseAdminList();
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
        value: function onLicenseDetails(license) {
            var _this3 = this;

            this.log.info('Open Details for: ', license);
            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseAdmin/detail/LicenseDetail.html',
                controller: 'LicenseDetail as licenseDetail',
                size: 'lg',
                resolve: {
                    params: function params() {
                        var dataItem = {};
                        if (license && license.dataItem) {
                            dataItem = license.dataItem;
                        } else {
                            dataItem = license;
                        }
                        return { license: dataItem };
                    }
                }
            });

            modalInstance.result.then(function (data) {
                _this3.openLastLicenseId = 0;
                if (data.updated) {
                    _this3.openLastLicenseId = data.id; // take this param from the last imported license, of course
                }

                _this3.reloadLicenseAdminList();
            }, function () {
                _this3.log.info('Request Canceled.');
            });
        }
    }, {
        key: 'onNewLicenseCreated',
        value: function onNewLicenseCreated(license) {
            this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseAdmin/created/CreatedLicense.html',
                size: 'md',
                controller: 'CreatedLicense as createdLicense',
                resolve: {
                    params: function params() {
                        return { email: license.email };
                    }
                }
            });
        }
    }, {
        key: 'reloadLicenseAdminList',
        value: function reloadLicenseAdminList() {
            if (this.licenseGrid.dataSource) {
                this.licenseGrid.dataSource.read();
            }
        }
    }]);

    return LicenseAdminList;
}();

exports.default = LicenseAdminList;

},{}],15:[function(require,module,exports){
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

var ManuallyRequest = function () {
    function ManuallyRequest($log, $scope, licenseAdminService, $uibModalInstance, params) {
        _classCallCheck(this, ManuallyRequest);

        this.log = $log;
        this.scope = $scope;
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseEmailModel = {
            id: params.license.id
        };

        // Get the hash code using the id.
        this.getEmailContent();
    }

    _createClass(ManuallyRequest, [{
        key: 'getEmailContent',
        value: function getEmailContent() {
            var _this = this;

            this.licenseAdminService.getEmailContent(this.licenseEmailModel.id, function (data) {
                _this.licenseEmailModel = data;
                window.TDSTM.safeApply(_this.scope);
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

    return ManuallyRequest;
}();

exports.default = ManuallyRequest;

},{}],16:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/26/2016.
 * Create a new Request to get a License
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

var _FormValidator2 = require('../../utils/form/FormValidator.js');

var _FormValidator3 = _interopRequireDefault(_FormValidator2);

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

var RequestLicense = function (_FormValidator) {
    _inherits(RequestLicense, _FormValidator);

    /**
     * Initialize all the properties
     * @param $log
     * @param licenseAdminService
     * @param $uibModalInstance
     */
    function RequestLicense($log, $scope, licenseAdminService, $uibModal, $uibModalInstance) {
        _classCallCheck(this, RequestLicense);

        var _this = _possibleConstructorReturn(this, (RequestLicense.__proto__ || Object.getPrototypeOf(RequestLicense)).call(this, $log, $scope, $uibModal, $uibModalInstance));

        _this.licenseAdminService = licenseAdminService;
        _this.uibModalInstance = $uibModalInstance;
        _this.log = $log;

        // Defined the Environment Select
        _this.environmentDataSource = [];
        // Define the Project Select
        _this.selectProject = {};
        _this.selectProjectListOptions = [];

        _this.getEnvironmentDataSource();
        _this.getProjectDataSource();

        // Create the Model for the New License
        _this.newLicenseModel = {
            email: '',
            environment: '',
            projectId: 0,
            clientName: '',
            requestNote: ''
        };

        return _this;
    }

    /**
     * Populate the Environment dropdown values
     */

    _createClass(RequestLicense, [{
        key: 'getEnvironmentDataSource',
        value: function getEnvironmentDataSource() {
            var _this2 = this;

            this.licenseAdminService.getEnvironmentDataSource(function (data) {
                _this2.environmentDataSource = data;
                if (_this2.environmentDataSource) {
                    var index = _this2.environmentDataSource.findIndex(function (enviroment) {
                        return enviroment === 'PRODUCTION';
                    });
                    index = index || 0;
                    _this2.newLicenseModel.environment = data[index];
                }
            });
        }

        /**
         * Populate the Project dropdown values
         */

    }, {
        key: 'getProjectDataSource',
        value: function getProjectDataSource() {
            var _this3 = this;

            this.selectProjectListOptions = {
                dataSource: {
                    transport: {
                        read: function read(e) {
                            _this3.licenseAdminService.getProjectDataSource(function (data) {
                                _this3.newLicenseModel.projectId = data[0].id;
                                _this3.saveForm(_this3.newLicenseModel);
                                return e.success(data);
                            });
                        }
                    }
                },
                dataTextField: 'name',
                dataValueField: 'id',
                valuePrimitive: true,
                select: function select(e) {
                    // On Project Change, select the Client Name
                    var item = _this3.selectProject.dataItem(e.item);
                    _this3.newLicenseModel.clientName = item.client.name;
                }
            };
        }

        /**
         * Execute the Service call to generate a new License request
         */

    }, {
        key: 'saveLicenseRequest',
        value: function saveLicenseRequest() {
            var _this4 = this;

            if (this.isDirty()) {
                this.log.info('New License Requested: ', this.newLicenseModel);
                this.licenseAdminService.createNewLicenseRequest(this.newLicenseModel, function (data) {
                    _this4.uibModalInstance.close(_this4.newLicenseModel);
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

    return RequestLicense;
}(_FormValidator3.default);

exports.default = RequestLicense;

},{"../../utils/form/FormValidator.js":31}],17:[function(require,module,exports){
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

var LicenseAdminService = function () {
    function LicenseAdminService($log, restServiceHandler, $rootScope) {
        _classCallCheck(this, LicenseAdminService);

        this.log = $log;
        this.restService = restServiceHandler;
        this.rootScope = $rootScope;
        this.statusSuccess = 'success';
        this.log.debug('licenseAdminService Instanced');
    }

    _createClass(LicenseAdminService, [{
        key: 'getLicenseList',
        value: function getLicenseList(onSuccess) {
            this.restService.licenseAdminServiceHandler().getLicenseList(function (data) {
                return onSuccess(data.data);
            });
        }
    }, {
        key: 'getEnvironmentDataSource',
        value: function getEnvironmentDataSource(onSuccess) {
            this.restService.licenseAdminServiceHandler().getEnvironmentDataSource(function (data) {
                return onSuccess(data.data);
            });
        }
    }, {
        key: 'getProjectDataSource',
        value: function getProjectDataSource(onSuccess) {
            this.restService.licenseAdminServiceHandler().getProjectDataSource(function (data) {
                return onSuccess(data.data);
            });
        }
    }, {
        key: 'getHashCode',
        value: function getHashCode(licenseId, onSuccess) {
            this.restService.licenseAdminServiceHandler().getHashCode(licenseId, function (data) {
                return onSuccess(data.data);
            });
        }
    }, {
        key: 'getEmailContent',
        value: function getEmailContent(licenseId, onSuccess) {
            this.restService.licenseAdminServiceHandler().getEmailContent(licenseId, function (data) {
                return onSuccess(data.data);
            });
        }

        /**
         * Create a New License passing params
         * @param newLicense
         * @param callback
         */

    }, {
        key: 'createNewLicenseRequest',
        value: function createNewLicenseRequest(newLicense, onSuccess) {
            this.restService.licenseAdminServiceHandler().createNewLicenseRequest(newLicense, function (data) {
                return onSuccess(data);
            });
        }
    }, {
        key: 'resubmitLicenseRequest',
        value: function resubmitLicenseRequest(license, onSuccess) {
            var _this = this;

            this.restService.licenseAdminServiceHandler().resubmitLicenseRequest(license.id, function (data) {

                if (data.status === _this.statusSuccess) {
                    _this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Request License was successfully' });
                } else {
                    _this.rootScope.$emit('broadcast-msg', { type: 'warning', text: data.data });
                    return onSuccess({ success: false });
                }

                return onSuccess(data);
            });
        }
    }, {
        key: 'emailRequest',
        value: function emailRequest(license, callback) {
            var _this2 = this;

            this.restService.licenseAdminServiceHandler().emailRequest(license, function (data) {
                _this2.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Request License was successfully.' });
                return callback(data);
            });
        }

        /**
         *  Apply The License
         * @param license
         * @param onSuccess
         */

    }, {
        key: 'applyLicense',
        value: function applyLicense(license, onSuccess, onError) {
            var _this3 = this;

            var hash = {
                hash: license.key
            };

            this.restService.licenseAdminServiceHandler().applyLicense(license.id, hash, function (data) {
                if (data.status === _this3.statusSuccess) {
                    _this3.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully applied' });
                } else {
                    _this3.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was not applied' });
                    return onError({ success: false });
                }

                return onSuccess({ success: true });
            });
        }
    }, {
        key: 'deleteLicense',
        value: function deleteLicense(license, onSuccess) {
            this.restService.licenseAdminServiceHandler().deleteLicense(license, function (data) {
                return onSuccess(data);
            });
        }
    }]);

    return LicenseAdminService;
}();

exports.default = LicenseAdminService;

},{}],18:[function(require,module,exports){
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

var _LicenseManagerList = require('./list/LicenseManagerList.js');

var _LicenseManagerList2 = _interopRequireDefault(_LicenseManagerList);

var _LicenseManagerService = require('./service/LicenseManagerService.js');

var _LicenseManagerService2 = _interopRequireDefault(_LicenseManagerService);

var _RequestImport = require('./requestImport/RequestImport.js');

var _RequestImport2 = _interopRequireDefault(_RequestImport);

var _LicenseManagerDetail = require('./detail/LicenseManagerDetail.js');

var _LicenseManagerDetail2 = _interopRequireDefault(_LicenseManagerDetail);

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

var LicenseManagerModule = _angular2.default.module('TDSTM.LicenseManagerModule', [_uiRouter2.default]).config(['$stateProvider', '$translatePartialLoaderProvider', function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('licenseManager');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider.state('licenseManagerList', {
        data: { page: { title: 'Licensing Manager', instruction: '', menu: ['Manager', 'License', 'List'] } },
        url: '/license/manager/list',
        views: {
            'headerView@': header,
            'bodyView@': {
                templateUrl: '../app-js/modules/licenseManager/list/LicenseManagerList.html',
                controller: 'LicenseManagerList as licenseManagerList'
            }
        }
    });
}]);

// Services
LicenseManagerModule.service('LicenseManagerService', ['$log', 'RestServiceHandler', '$rootScope', _LicenseManagerService2.default]);

// Controllers
LicenseManagerModule.controller('LicenseManagerList', ['$log', '$state', 'LicenseManagerService', '$uibModal', _LicenseManagerList2.default]);

// Modal - Controllers
LicenseManagerModule.controller('RequestImport', ['$log', '$scope', 'LicenseManagerService', '$uibModal', '$uibModalInstance', _RequestImport2.default]);
LicenseManagerModule.controller('LicenseManagerDetail', ['$log', '$scope', 'LicenseManagerService', 'UserPreferencesService', '$uibModal', '$uibModalInstance', 'params', _LicenseManagerDetail2.default]);

exports.default = LicenseManagerModule;

},{"./detail/LicenseManagerDetail.js":19,"./list/LicenseManagerList.js":20,"./requestImport/RequestImport.js":21,"./service/LicenseManagerService.js":22,"angular":"angular","ui-router":"ui-router"}],19:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/26/2016.
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

var _FormValidator2 = require('../../utils/form/FormValidator.js');

var _FormValidator3 = _interopRequireDefault(_FormValidator2);

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

var LicenseManagerDetail = function (_FormValidator) {
    _inherits(LicenseManagerDetail, _FormValidator);

    function LicenseManagerDetail($log, $scope, licenseManagerService, userPreferencesService, $uibModal, $uibModalInstance, params, timeZoneConfiguration) {
        _classCallCheck(this, LicenseManagerDetail);

        var _this = _possibleConstructorReturn(this, (LicenseManagerDetail.__proto__ || Object.getPrototypeOf(LicenseManagerDetail)).call(this, $log, $scope, $uibModal, $uibModalInstance));

        _this.scope = $scope;
        _this.licenseManagerService = licenseManagerService;
        _this.userPreferencesService = userPreferencesService;
        _this.uibModalInstance = $uibModalInstance;
        _this.uibModal = $uibModal;
        _this.log = $log;

        _this.editMode = false;

        _this.timeZoneConfiguration = timeZoneConfiguration;

        _this.licenseModel = {
            id: params.license.id,
            ownerName: params.license.owner.name,
            email: params.license.email,
            project: {
                id: params.license.project.id,
                name: params.license.project.name
            },
            clientId: params.license.client.id,
            clientName: params.license.client.name,
            status: params.license.status,
            method: {
                name: params.license.method.name,
                max: params.license.method.max
            },
            environment: params.license.environment,
            requestDate: params.license.requestDate,
            initDate: params.license.activationDate !== null ? angular.copy(params.license.activationDate) : '',
            endDate: params.license.expirationDate !== null ? angular.copy(params.license.expirationDate) : '',
            specialInstructions: params.license.requestNote,
            websiteName: params.license.websitename,

            bannerMessage: params.license.bannerMessage,
            requestedId: params.license.requestedId,
            replaced: params.license.replaced,
            replacedId: params.license.replacedId,
            hostName: params.license.hostName,
            hash: params.license.id,
            gracePeriodDays: params.license.gracePeriodDays,

            applied: params.license.applied,
            keyId: params.license.keyId
        };

        _this.licenseKey = 'Licenses has not been issued';

        // Defined the Environment Select
        _this.selectEnvironment = {};
        _this.selectEnvironmentListOptions = [];
        _this.getEnvironmentDataSource();

        // Defined the Status Select List
        _this.selectStatus = [];

        // Init the two Kendo Dates for Init and EndDate
        _this.initDate = {};
        _this.initDateOptions = {
            format: _this.userPreferencesService.getConvertedDateFormatToKendoDate(),
            open: function open(e) {
                _this.onChangeInitDate();
            },
            change: function change(e) {
                _this.onChangeInitDate();
            }
        };

        _this.endDate = {};
        _this.endDateOptions = {
            format: _this.userPreferencesService.getConvertedDateFormatToKendoDate(),
            open: function open(e) {
                _this.onChangeEndDate();
            },
            change: function change(e) {
                _this.onChangeEndDate();
            }
        };

        _this.prepareMethodOptions();
        _this.prepareLicenseKey();
        _this.prepareActivityList();

        _this.prepareControlActionButtons();

        return _this;
    }

    /**
     * Controls what buttons to show
     */

    _createClass(LicenseManagerDetail, [{
        key: 'prepareControlActionButtons',
        value: function prepareControlActionButtons() {
            this.pendingLicense = this.licenseModel.status === 'PENDING' && !this.editMode;
            this.expiredOrTerminated = this.licenseModel.status === 'EXPIRED' || this.licenseModel.status === 'TERMINATED';
            this.activeShowMode = this.licenseModel.status === 'ACTIVE' && !this.expiredOrTerminated && !this.editMode;
        }
    }, {
        key: 'prepareMethodOptions',
        value: function prepareMethodOptions() {
            this.methodOptions = [{
                name: 'MAX_SERVERS',
                text: 'Servers',
                max: 0
            }, {
                name: 'TOKEN',
                text: 'Tokens',
                max: 0
            }, {
                name: 'CUSTOM',
                text: 'Custom'
            }];
        }
    }, {
        key: 'prepareLicenseKey',
        value: function prepareLicenseKey() {
            var _this2 = this;

            if (this.licenseModel.status === 'ACTIVE') {
                this.licenseManagerService.getKeyCode(this.licenseModel.id, function (data) {
                    if (data) {
                        _this2.licenseKey = data;
                        window.TDSTM.safeApply(_this2.scope);
                    }
                });
            }
        }
    }, {
        key: 'prepareActivityList',
        value: function prepareActivityList() {
            var _this3 = this;

            this.activityGrid = {};
            this.activityGridOptions = {
                pageable: {
                    refresh: true,
                    pageSizes: true,
                    buttonCount: 5,
                    pageSize: 20
                },
                columns: [{ field: 'dateCreated', title: 'Date', width: 160, type: 'date', format: '{0:dd/MMM/yyyy h:mm:ss tt}', template: '{{ dataItem.dateCreated | convertDateTimeIntoTimeZone }}' }, { field: 'author.personName', title: 'Whom', width: 160 }, { field: 'changes', title: 'Action', template: '<table class="inner-activity_table"><tbody><tr><td></td><td class="col-action_td"><span class="glyphicon glyphicon-minus" aria-hidden="true"></span></td><td class="col-action_td"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></td></tr>#for(var i = 0; i < data.changes.length; i++){#<tr><td style="font-weight: bold;">#=data.changes[i].field# </td><td class="col-value_td"><span class="activity-list-old-val" style="color:darkred; font-weight: bold;">{{ \'#=data.changes[i].oldValue#\' | convertDateIntoTimeZone }}</span></td><td class="col-value_td"><span class="activity-list-new-val" style="color: green; font-weight: bold;">{{ \'#=data.changes[i].newValue#\' | convertDateIntoTimeZone }}</td></tr>#}#</tbody></table>' }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            _this3.licenseManagerService.getActivityLog(_this3.licenseModel, function (data) {
                                e.success(data.data);
                            });
                        }
                    },
                    sort: {
                        field: 'dateCreated',
                        dir: 'asc'
                    }
                },
                scrollable: true
            };
        }

        /**
         * If by some reason the License was not applied at first time, this will do a request for it
         */

    }, {
        key: 'activateLicense',
        value: function activateLicense() {
            var _this4 = this;

            this.licenseManagerService.activateLicense(this.licenseModel, function (data) {
                if (data) {
                    _this4.licenseModel.status = 'ACTIVE';
                    _this4.saveForm(_this4.licenseModel);
                    _this4.prepareControlActionButtons();
                    _this4.prepareLicenseKey();
                    _this4.reloadRequired = true;
                    _this4.reloadLicenseManagerList();
                }
            });
        }
    }, {
        key: 'revokeLicense',
        value: function revokeLicense() {
            var _this5 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
                controller: 'DialogAction as dialogAction',
                size: 'sm',
                resolve: {
                    params: function params() {
                        return { title: 'Confirmation Required', message: 'Are you sure you want to revoke it? This action cannot be undone.' };
                    }
                }
            });

            modalInstance.result.then(function () {
                _this5.licenseManagerService.revokeLicense(_this5.licenseModel, function (data) {
                    _this5.uibModalInstance.close(data);
                });
            });
        }

        /**
         * If by some reason the License was not applied at first time, this will do a request for it
         */

    }, {
        key: 'manuallyRequest',
        value: function manuallyRequest() {
            this.licenseManagerService.manuallyRequest(this.licenseModel, function (data) {});
        }

        /**
         * Validate the input on Server or Tokens is only integer only
         * This will be converted in a more complex directive later
         * TODO: Convert into a directive
         */

    }, {
        key: 'validateIntegerOnly',
        value: function validateIntegerOnly(e, model) {
            try {
                var newVal = parseInt(model);
                if (!isNaN(newVal)) {
                    model = newVal;
                } else {
                    model = 0;
                }
                if (e && e.currentTarget) {
                    e.currentTarget.value = model;
                }
            } catch (e) {
                this.$log.warn('Invalid Number Exception', model);
            }
        }

        /**
         * Save current changes
         */

    }, {
        key: 'saveLicense',
        value: function saveLicense() {
            var _this6 = this;

            if (this.isDirty()) {
                this.editMode = false;
                this.prepareControlActionButtons();
                this.licenseManagerService.saveLicense(this.licenseModel, function (data) {
                    _this6.reloadRequired = true;
                    _this6.saveForm(_this6.licenseModel);
                    _this6.reloadLicenseManagerList();
                    _this6.log.info('License Saved');
                });
            } else {
                this.editMode = false;
                this.prepareControlActionButtons();
            }
        }

        /**
         * Change the status to Edit
         */

    }, {
        key: 'modifyLicense',
        value: function modifyLicense() {
            this.editMode = true;
            this.prepareControlActionButtons();
        }

        /**
         * Populate values
         */

    }, {
        key: 'getEnvironmentDataSource',
        value: function getEnvironmentDataSource() {
            var _this7 = this;

            this.selectEnvironmentListOptions = {
                dataSource: {
                    transport: {
                        read: function read(e) {
                            _this7.licenseManagerService.getEnvironmentDataSource(function (data) {
                                if (!_this7.licenseModel.environment) {
                                    _this7.licenseModel.environment = data[0];
                                }

                                _this7.saveForm(_this7.licenseModel);
                                return e.success(data);
                            });
                        }
                    }
                },
                valueTemplate: '<span style="text-transform: capitalize;">#=((data)? data.toLowerCase(): "" )#</span>',
                template: '<span style="text-transform: capitalize;">#=((data)? data.toLowerCase(): "" )#</span>',
                valuePrimitive: true
            };
        }
    }, {
        key: 'onChangeInitDate',
        value: function onChangeInitDate() {
            var startDate = this.initDate.value(),
                endDate = this.endDate.value();

            if (startDate) {
                startDate = new Date(startDate);
                startDate.setDate(startDate.getDate());
                this.endDate.min(startDate);

                if (endDate) {
                    if (this.initDate.value() > this.endDate.value()) {
                        endDate = new Date(endDate);
                        endDate.setDate(startDate.getDate());
                        this.licenseModel.endDate = endDate;
                    }
                }
            }
        }
    }, {
        key: 'onChangeEndDate',
        value: function onChangeEndDate() {
            var endDate = this.endDate.value(),
                startDate = this.initDate.value();

            if (endDate) {
                endDate = new Date(endDate);
                endDate.setDate(endDate.getDate());
            } else if (startDate) {
                this.endDate.min(new Date(startDate));
            } else {
                endDate = new Date();
                this.initDate.max(endDate);
                this.endDate.min(endDate);
            }
        }

        /**
         * Dismiss the dialog, no action necessary
         */

    }, {
        key: 'cancelCloseDialog',
        value: function cancelCloseDialog() {
            var _this8 = this;

            if (this.editMode) {
                this.resetForm(function () {
                    _this8.onResetForm();
                });
            } else if (this.reloadRequired) {
                this.uibModalInstance.close({});
            } else {
                this.uibModalInstance.dismiss('cancel');
            }
        }

        /**
         * Depending the number of fields and type of field, the reset can't be on the FormValidor, at least not now
         */

    }, {
        key: 'onResetForm',
        value: function onResetForm() {
            this.resetDropDown(this.selectEnvironment, this.licenseModel.environment);
            this.onChangeInitDate();
            this.onChangeEndDate();

            this.editMode = false;
            this.prepareControlActionButtons();
        }

        /**
         * Manual reload after a change has been performed to the License
         */

    }, {
        key: 'reloadLicenseManagerList',
        value: function reloadLicenseManagerList() {
            if (this.activityGrid.dataSource) {
                this.activityGrid.dataSource.read();
            }
        }
    }]);

    return LicenseManagerDetail;
}(_FormValidator3.default);

exports.default = LicenseManagerDetail;

},{"../../utils/form/FormValidator.js":31}],20:[function(require,module,exports){
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

var LicenseManagerList = function () {
    function LicenseManagerList($log, $state, licenseManagerService, $uibModal) {
        _classCallCheck(this, LicenseManagerList);

        this.log = $log;
        this.state = $state;
        this.licenseGrid = {};
        this.licenseGridOptions = {};
        this.licenseManagerService = licenseManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        //this.getLicenseList();
        this.log.debug('LicenseManagerList Instanced');
        this.openLastImportedLicenseId = 0;
    }

    _createClass(LicenseManagerList, [{
        key: 'getDataSource',
        value: function getDataSource() {
            var _this = this;

            this.licenseGridOptions = {
                toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseManagerList.onRequestImportLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Import License Request</button> <div ng-click="licenseManagerList.reloadLicenseManagerList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                pageable: {
                    refresh: true,
                    pageSizes: true,
                    buttonCount: 5,
                    pageSize: 20
                },
                columns: [{ field: 'id', hidden: true }, { field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseManagerList.onLicenseManagerDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'owner.name', title: 'Owner' }, { field: 'websitename', title: 'Website Name' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project', template: '<span style="text-transform: capitalize;">#=((data.project && data.project.name)? data.project.name.toLowerCase(): "" )#</span>' }, { field: 'email', title: 'Contact Email' }, { field: 'status', title: 'Status', template: '<span style="text-transform: capitalize;">#=((data.status)? data.status.toLowerCase(): "" )#</span>' }, { field: 'type.name', title: 'Type', template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#' }, { field: 'method.name', title: 'Method', template: '<span style="text-transform: capitalize;">#=((data.method && data.method.name)? data.method.name.toLowerCase(): "" )#</span>' }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'activationDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.activationDate | convertDateIntoTimeZone }}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.expirationDate | convertDateIntoTimeZone }}' }, { field: 'environment', title: 'Environment', template: '<span style="text-transform: capitalize;">#=((data.environment)? data.environment.toLowerCase(): "" )#</span>' }, { field: 'gracePeriodDays', hidden: true }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            _this.licenseManagerService.getLicenseList(function (data) {
                                e.success(data);
                            });
                        }
                    },
                    sort: {
                        field: 'project.name',
                        dir: 'asc'
                    },
                    change: function change(e) {
                        // We are coming from a new imported request license
                        if (_this.openLastImportedLicenseId !== 0 && _this.licenseGrid.dataSource._data) {
                            var newLicenseCreated = _this.licenseGrid.dataSource._data.find(function (license) {
                                return license.id === _this.openLastImportedLicenseId;
                            });

                            _this.openLastImportedLicenseId = 0;

                            if (newLicenseCreated) {
                                _this.onLicenseManagerDetails(newLicenseCreated);
                            }
                        }
                    }
                },
                sortable: true,
                filterable: {
                    extra: false
                }
            };
        }

        /**
         * The user Import a new License
         */

    }, {
        key: 'onRequestImportLicense',
        value: function onRequestImportLicense() {
            var _this2 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseManager/requestImport/RequestImport.html',
                controller: 'RequestImport as requestImport',
                size: 'md'
            });

            modalInstance.result.then(function (licenseImported) {
                _this2.openLastImportedLicenseId = licenseImported.id; // take this param from the last imported license, of course
                _this2.reloadLicenseManagerList();
            });
        }

        /**
         * After clicking on edit, we redirect the user to the Edition screen instead of open a dialog
         * du the size of the inputs
         */

    }, {
        key: 'onLicenseManagerDetails',
        value: function onLicenseManagerDetails(license) {
            var _this3 = this;

            this.log.info('Open Details for: ', license);
            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseManager/detail/LicenseManagerDetail.html',
                controller: 'LicenseManagerDetail as licenseManagerDetail',
                size: 'lg',
                resolve: {
                    params: function params() {
                        var dataItem = {};
                        if (license && license.dataItem) {
                            dataItem = license.dataItem;
                        } else {
                            dataItem = license;
                        }
                        return { license: dataItem };
                    }
                }
            });

            modalInstance.result.then(function () {
                _this3.reloadLicenseManagerList();
            }, function () {
                _this3.log.info('Request Canceled.');
            });
        }
    }, {
        key: 'reloadLicenseManagerList',
        value: function reloadLicenseManagerList() {
            if (this.licenseGrid.dataSource) {
                this.licenseGrid.dataSource.read();
            }
        }
    }]);

    return LicenseManagerList;
}();

exports.default = LicenseManagerList;

},{}],21:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/28/2016.
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

var _FormValidator2 = require('../../utils/form/FormValidator.js');

var _FormValidator3 = _interopRequireDefault(_FormValidator2);

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

var RequestImport = function (_FormValidator) {
    _inherits(RequestImport, _FormValidator);

    function RequestImport($log, $scope, licenseManagerService, $uibModal, $uibModalInstance) {
        _classCallCheck(this, RequestImport);

        var _this = _possibleConstructorReturn(this, (RequestImport.__proto__ || Object.getPrototypeOf(RequestImport)).call(this, $log, $scope, $uibModal, $uibModalInstance));

        _this.licenseManagerService = licenseManagerService;
        _this.uibModalInstance = $uibModalInstance;
        _this.licenseModel = {
            hash: ''
        };

        _this.saveForm(_this.licenseModel);
        return _this;
    }

    /**
     * Execute and validate the Key is correct
     */

    _createClass(RequestImport, [{
        key: 'onImportLicense',
        value: function onImportLicense() {
            var _this2 = this;

            if (this.isDirty()) {
                this.licenseManagerService.importLicense(this.licenseModel, function (licenseImported) {
                    _this2.uibModalInstance.close(licenseImported.data);
                }, function (licenseImported) {
                    _this2.uibModalInstance.close(licenseImported.data);
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

    return RequestImport;
}(_FormValidator3.default);

exports.default = RequestImport;

},{"../../utils/form/FormValidator.js":31}],22:[function(require,module,exports){
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
    function LicenseManagerService($log, restServiceHandler, $rootScope) {
        _classCallCheck(this, LicenseManagerService);

        this.log = $log;
        this.restService = restServiceHandler;
        this.rootScope = $rootScope;
        this.statusSuccess = 'success';
        this.log.debug('licenseManagerService Instanced');
    }

    _createClass(LicenseManagerService, [{
        key: 'getLicenseList',
        value: function getLicenseList(onSuccess) {
            this.restService.licenseManagerServiceHandler().getLicenseList(function (data) {

                return onSuccess(data.data);
            });
        }
    }, {
        key: 'getProjectDataSource',
        value: function getProjectDataSource(onSuccess) {
            this.restService.licenseManagerServiceHandler().getProjectDataSource(function (data) {
                return onSuccess(data.data);
            });
        }
    }, {
        key: 'getEnvironmentDataSource',
        value: function getEnvironmentDataSource(onSuccess) {
            this.restService.licenseManagerServiceHandler().getEnvironmentDataSource(function (data) {
                return onSuccess(data.data);
            });
        }
    }, {
        key: 'getKeyCode',
        value: function getKeyCode(licenseId, onSuccess) {
            this.restService.licenseManagerServiceHandler().getKeyCode(licenseId, function (data) {
                return onSuccess(data.data);
            });
        }
    }, {
        key: 'getTimeZoneConfiguration',
        value: function getTimeZoneConfiguration(onSuccess) {
            this.restService.commonServiceHandler().getTimeZoneConfiguration(function (data) {
                return onSuccess(data.data);
            });
        }
    }, {
        key: 'manuallyRequest',
        value: function manuallyRequest(license, onSuccess) {
            var _this = this;

            this.restService.licenseManagerServiceHandler().manuallyRequest(license.id, function (data) {

                if (data.status === _this.statusSuccess) {
                    _this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Email License was successfully' });
                } else {
                    _this.rootScope.$emit('broadcast-msg', { type: 'warning', text: data.data });
                    return onSuccess({ success: false });
                }

                return onSuccess(data);
            });
        }

        /**
         * Save the License
         */

    }, {
        key: 'saveLicense',
        value: function saveLicense(license, onSuccess) {

            var licenseModified = {
                environment: license.environment,
                method: {
                    name: license.method.name
                },
                activationDate: moment(license.initDate).format('YYYY-MM-DD'),
                expirationDate: moment(license.endDate).format('YYYY-MM-DD'),
                status: license.status,
                project: {
                    id: license.project.id !== 'all' ? parseInt(license.project.id) : license.project.id, // We pass 'all' when is multiproject
                    name: license.project.name
                },
                bannerMessage: license.bannerMessage,
                gracePeriodDays: license.gracePeriodDays,
                websitename: license.websiteName,
                hostName: license.hostName
            };
            if (license.method.name !== 'CUSTOM') {
                licenseModified.method.max = parseInt(license.method.max);
            }

            this.restService.licenseManagerServiceHandler().saveLicense(license.id, licenseModified, function (data) {
                return onSuccess(data);
            });
        }
        /**
         * Does the activation of the current license if this is not active
         * @param license
         * @param callback
         */

    }, {
        key: 'activateLicense',
        value: function activateLicense(license, callback) {
            var _this2 = this;

            this.restService.licenseManagerServiceHandler().activateLicense(license.id, function (data) {
                if (data.status === _this2.statusSuccess) {
                    _this2.rootScope.$emit('broadcast-msg', {
                        type: 'info',
                        text: 'The license was activated and the license was emailed.'
                    });
                    return callback(data);
                } else {
                    _this2.rootScope.$emit('broadcast-msg', {
                        type: 'warning',
                        text: data.data
                    });
                    return callback();
                }
            });
        }

        /**
         * Make the request to Import the license, if fails, throws an exception visible for the user to take action
         * @param license
         * @param callback
         */

    }, {
        key: 'importLicense',
        value: function importLicense(license, onSuccess, onError) {
            var _this3 = this;

            var hash = {
                data: license.hash
            };

            this.restService.licenseManagerServiceHandler().requestImport(hash, function (data) {
                if (data.status === _this3.statusSuccess) {
                    _this3.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully Imported' });
                } else {
                    _this3.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was not applied. Review the provided License Key is correct.' });
                    return onError({ success: false });
                }
                return onSuccess(data);
            });
        }
    }, {
        key: 'revokeLicense',
        value: function revokeLicense(license, onSuccess) {
            this.restService.licenseManagerServiceHandler().revokeLicense(license, function (data) {
                return onSuccess(data);
            });
        }
    }, {
        key: 'getActivityLog',
        value: function getActivityLog(license, onSuccess) {
            this.restService.licenseManagerServiceHandler().getActivityLog(license.id, function (data) {
                return onSuccess(data);
            });
        }

        /**
         * Create a New License passing params
         * @param newLicense
         * @param callback
         */

    }, {
        key: 'createNewLicenseRequest',
        value: function createNewLicenseRequest(newLicense, callback) {
            this.restService.licenseManagerServiceHandler().createNewLicenseRequest(newLicense, function (data) {
                return callback(data);
            });
        }
    }]);

    return LicenseManagerService;
}();

exports.default = LicenseManagerService;

},{}],23:[function(require,module,exports){
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
        data: { page: { title: 'Notice Administration', instruction: '', menu: ['Admin', 'Notice', 'List'] } },
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
NoticeManagerModule.controller('EditNotice', ['$log', 'NoticeManagerService', '$uibModal', '$uibModalInstance', 'params', _EditNotice2.default]);

exports.default = NoticeManagerModule;

},{"./edit/EditNotice.js":24,"./list/NoticeList.js":25,"./service/NoticeManagerService.js":26,"angular":"angular","ui-router":"ui-router"}],24:[function(require,module,exports){
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
    function EditNotice($log, noticeManagerService, $uibModal, $uibModalInstance, params) {
        _classCallCheck(this, EditNotice);

        this.noticeManagerService = noticeManagerService;
        this.uibModal = $uibModal;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;

        this.action = params.action;
        this.actionType = params.actionType;

        this.kendoEditorTools = ['formatting', 'cleanFormatting', 'fontName', 'fontSize', 'justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull', 'bold', 'italic', 'viewHtml'];

        // CSS has not canceling attributes, so instead of removing every possible HTML, we make editor has same css
        this.kendoStylesheets = ['../static/dist/js/vendors/bootstrap/dist/css/bootstrap.min.css', // Ourt current Bootstrap css
        '../static/dist/css/TDSTMLayout.min.css' // Original Template CSS

        ];

        this.getTypeDataSource();
        this.editModel = {
            title: '',
            typeId: 0,
            active: false,
            htmlText: '',
            rawText: ''

            // On Edition Mode we cc the model and only the params we need
        };if (params.notice) {
            this.editModel.id = params.notice.id;
            this.editModel.title = params.notice.title;
            this.editModel.typeId = params.notice.type.id;
            this.editModel.active = params.notice.active;
            this.editModel.htmlText = params.notice.htmlText;
        }
    }

    /**
     * Populate the Environment dropdown values
     */

    _createClass(EditNotice, [{
        key: 'getTypeDataSource',
        value: function getTypeDataSource() {
            this.typeDataSource = [{ typeId: 1, name: 'Prelogin' }, { typeId: 2, name: 'Postlogin'
                //{typeId: 3, name: 'General'} Disabled until Phase II
            }];
        }

        /**
         * Execute the Service call to Create/Edit a notice
         */

    }, {
        key: 'saveNotice',
        value: function saveNotice() {
            var _this = this;

            this.log.info(this.action + ' Notice Requested: ', this.editModel);
            this.editModel.rawText = $('#kendo-editor-create-edit').text();
            this.editModel.typeId = parseInt(this.editModel.typeId);
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
    }, {
        key: 'deleteNotice',
        value: function deleteNotice() {
            var _this2 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
                controller: 'DialogAction as dialogAction',
                size: 'sm',
                resolve: {
                    params: function params() {
                        return { title: 'Confirmation Required', message: 'Are you sure you want to delete it? This action cannot be undone.' };
                    }
                }
            });

            modalInstance.result.then(function () {
                _this2.noticeManagerService.deleteNotice(_this2.editModel, function (data) {
                    _this2.uibModalInstance.close(data);
                });
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

    return EditNotice;
}();

exports.default = EditNotice;

},{}],25:[function(require,module,exports){
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

        this.noticeGrid = {};
        this.noticeGridOptions = {};
        this.noticeManagerService = noticeManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        this.log.debug('LicenseList Instanced');
    }

    _createClass(NoticeList, [{
        key: 'getDataSource',
        value: function getDataSource() {
            var _this = this;

            this.noticeGridOptions = {
                toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="noticeList.onEditCreateNotice(noticeList.actionType.NEW)"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Create New Notice</button> <div ng-click="noticeList.reloadNoticeList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                pageable: {
                    refresh: true,
                    pageSizes: true,
                    buttonCount: 5
                },
                columns: [{ field: 'id', hidden: true }, { field: 'htmlText', hidden: true }, { field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="noticeList.onEditCreateNotice(noticeList.actionType.EDIT, this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'title', title: 'Title' }, { field: 'type.id', hidden: true }, { field: 'type.name', title: 'Type' }, { field: 'active', title: 'Active', template: '#if(active) {# Yes #} else {# No #}#' }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            _this.noticeManagerService.getNoticeList(function (data) {
                                e.success(data);
                            });
                        }
                    },
                    sort: {
                        field: 'title',
                        dir: 'asc'
                    }
                },
                sortable: true
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
                        return { action: action, notice: dataItem, actionType: _this2.actionType };
                    }
                }
            });

            modalInstance.result.then(function (notice) {
                _this2.log.info(action + ' Notice: ', notice);
                // After a new value is added, lets to refresh the Grid
                _this2.reloadNoticeList();
            }, function () {
                _this2.log.info(action + ' Request Canceled.');
            });
        }
    }, {
        key: 'reloadNoticeList',
        value: function reloadNoticeList() {
            if (this.noticeGrid.dataSource) {
                this.noticeGrid.dataSource.read();
            }
        }
    }]);

    return NoticeList;
}();

exports.default = NoticeList;

},{}],26:[function(require,module,exports){
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

        this.TYPE = {
            '1': 'Prelogin',
            '2': 'Postlogin',
            '3': 'General'
        };

        this.log.debug('NoticeManagerService Instanced');
    }

    _createClass(NoticeManagerService, [{
        key: 'getNoticeList',
        value: function getNoticeList(callback) {
            var _this = this;

            this.restService.noticeManagerServiceHandler().getNoticeList(function (data) {
                var noticeList = [];
                try {
                    // Verify the List returns what we expect and we convert it to an Array value
                    if (data && data.notices) {
                        noticeList = data.notices;
                        if (noticeList && noticeList.length > 0) {
                            for (var i = 0; i < noticeList.length; i = i + 1) {
                                noticeList[i].type = {
                                    id: noticeList[i].typeId,
                                    name: _this.TYPE[noticeList[i].typeId]
                                };
                                delete noticeList[i].typeId;
                            }
                        }
                    }
                } catch (e) {
                    _this.log.error('Error parsing the Notice List', e);
                }
                return callback(noticeList);
            });
        }

        /**
         * Create a New Notice passing params
         * @param notice
         * @param callback
         */

    }, {
        key: 'createNotice',
        value: function createNotice(notice, callback) {
            this.restService.noticeManagerServiceHandler().createNotice(notice, function (data) {
                return callback(data);
            });
        }

        /**
         * Notice should have the ID in order to edit the Notice
         * @param notice
         * @param callback
         */

    }, {
        key: 'editNotice',
        value: function editNotice(notice, callback) {
            this.restService.noticeManagerServiceHandler().editNotice(notice, function (data) {
                return callback(data);
            });
        }

        /**
         * Notice should have the ID in order to delete the notice
         * @param notice
         * @param callback
         */

    }, {
        key: 'deleteNotice',
        value: function deleteNotice(notice, callback) {
            this.restService.noticeManagerServiceHandler().deleteNotice(notice, function (data) {
                return callback(data);
            });
        }
    }]);

    return NoticeManagerService;
}();

exports.default = NoticeManagerService;

},{}],27:[function(require,module,exports){
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

function _interopRequireDefault(obj) {
    return obj && obj.__esModule ? obj : { default: obj };
}

var TaskManagerModule = _angular2.default.module('TDSTM.TaskManagerModule', [_uiRouter2.default]).config(['$stateProvider', 'formlyConfigProvider', function ($stateProvider, formlyConfigProvider) {

    formlyConfigProvider.setType({
        name: 'custom',
        templateUrl: 'custom.html'
    });

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider.state('taskList', {
        data: { page: { title: 'My Task Manager', instruction: '', menu: ['Task Manager'] } },
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
TaskManagerModule.service('taskManagerService', ['$log', 'RestServiceHandler', _TaskManagerService2.default]);

// Controllers
TaskManagerModule.controller('TaskManagerController', ['$log', 'taskManagerService', '$uibModal', _TaskManagerController2.default]);
TaskManagerModule.controller('TaskManagerEdit', ['$log', _TaskManagerEdit2.default]);

exports.default = TaskManagerModule;

},{"./edit/TaskManagerEdit.js":28,"./list/TaskManagerController.js":29,"./service/TaskManagerService.js":30,"angular":"angular","ui-router":"ui-router"}],28:[function(require,module,exports){
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

},{}],29:[function(require,module,exports){
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
        this.initForm();
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
                            /*this.taskManagerService.testService((data) => {
                                e.success(data);
                            });*/
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
    }, {
        key: 'initForm',
        value: function initForm() {
            this.userFields = [{
                key: 'email',
                type: 'input',
                templateOptions: {
                    type: 'email',
                    label: 'Email address',
                    placeholder: 'Enter email'
                }
            }, {
                key: 'password',
                type: 'input',
                templateOptions: {
                    type: 'password',
                    label: 'Password',
                    placeholder: 'Password'
                }
            }, {
                key: 'file',
                type: 'file',
                templateOptions: {
                    label: 'File input',
                    description: 'Example block-level help text here',
                    url: 'https://example.com/upload'
                }
            }, {
                key: 'checked',
                type: 'checkbox',
                templateOptions: {
                    label: 'Check me out'
                }
            }];
        }
    }]);

    return TaskManagerController;
}();

exports.default = TaskManagerController;

},{}],30:[function(require,module,exports){
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

},{}],31:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 12/3/2016.
 */

'use strict';

var _typeof2 = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _typeof = typeof Symbol === "function" && _typeof2(Symbol.iterator) === "symbol" ? function (obj) {
    return typeof obj === "undefined" ? "undefined" : _typeof2(obj);
} : function (obj) {
    return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj === "undefined" ? "undefined" : _typeof2(obj);
};

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

var FormValidator = function () {
    function FormValidator($log, $scope, $uibModal, $uibModalInstance) {
        var _this = this;

        _classCallCheck(this, FormValidator);

        this.log = $log;
        this.scope = $scope;

        // JS does a argument pass by reference
        this.currentObject = null;
        // A copy without reference from the original object
        this.originalData = null;
        // A CC as JSON for comparison Purpose
        this.objectAsJSON = null;

        // Only for Modal Windows
        this.reloadRequired = false;
        this.uibModal = $uibModal;
        this.uibModalInstance = $uibModalInstance;

        if ($scope.$on) {
            $scope.$on('modal.closing', function (event, reason, closed) {
                _this.onCloseDialog(event, reason, closed);
            });
        }
    }

    /**
     * Saves the Form in 3 instances, one to keep track of the original data, other is the current object and
     * a JSON format for comparison purpose
     * @param newObjectInstance
     */

    _createClass(FormValidator, [{
        key: 'saveForm',
        value: function saveForm(newObjectInstance) {
            this.currentObject = newObjectInstance;
            this.originalData = angular.copy(newObjectInstance, this.originalData);
            this.objectAsJSON = angular.toJson(newObjectInstance);
        }

        /**
         * Get the Current Object on his reference Format
         * @returns {null|*}
         */

    }, {
        key: 'getForm',
        value: function getForm() {
            return this.currentObject;
        }

        /**
         * Get the Object as JSON from the Original Data
         * @returns {null|string|undefined|string|*}
         */

    }, {
        key: 'getFormAsJSON',
        value: function getFormAsJSON() {
            return this.objectAsJSON;
        }

        /**
         *
         * @param objetToReset object to reset
         * @param onResetForm callback
         * @returns {*}
         */

    }, {
        key: 'resetForm',
        value: function resetForm(onResetForm) {
            this.currentObject = angular.copy(this.originalData, this.currentObject);
            this.safeApply();

            if (onResetForm) {
                return onResetForm();
            }
        }

        /**
         * Validates if the current object differs from where it was originally saved
         * @returns {boolean}
         */

    }, {
        key: 'isDirty',
        value: function isDirty() {
            var newObjectInstance = angular.toJson(this.currentObject);
            return newObjectInstance !== this.getFormAsJSON();
        }

        /**
         * This function is only available when the Form is being called from a Dialog PopUp
         */

    }, {
        key: 'onCloseDialog',
        value: function onCloseDialog(event, reason, closed) {
            this.log.info('modal.closing: ' + (closed ? 'close' : 'dismiss') + '(' + reason + ')');
            if (this.isDirty() && reason !== 'cancel-confirmation' && (typeof reason === 'undefined' ? 'undefined' : _typeof(reason)) !== 'object') {
                event.preventDefault();
                this.confirmCloseForm();
            }
        }

        /**
         * A Confirmation Dialog when the information can be lost
         * @param event
         */

    }, {
        key: 'confirmCloseForm',
        value: function confirmCloseForm(event) {
            var _this2 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
                controller: 'DialogAction as dialogAction',
                size: 'sm',
                resolve: {
                    params: function params() {
                        return {
                            title: 'Confirmation Required',
                            message: 'Changes you made may not be saved. Do you want to continue?'
                        };
                    }
                }
            });

            modalInstance.result.then(function () {
                _this2.uibModalInstance.dismiss('cancel-confirmation');
            });
        }

        /**
         * Util to call safe if required
         * @param fn
         */

    }, {
        key: 'safeApply',
        value: function safeApply(fn) {
            var phase = this.scope.$root.$$phase;
            if (phase === '$apply' || phase === '$digest') {
                if (fn && typeof fn === 'function') {
                    fn();
                }
            } else {
                this.scope.$apply(fn);
            }
        }

        /**
         * Util to Reset a Dropdown list on Kendo
         */

    }, {
        key: 'resetDropDown',
        value: function resetDropDown(selectorInstance, selectedId, force) {
            if (selectorInstance && selectorInstance.dataItems) {
                selectorInstance.dataItems().forEach(function (value, index) {
                    if (selectedId === value.id || selectedId === value) {
                        selectorInstance.select(index);
                    }
                });

                if (force) {
                    selectorInstance.trigger('change');
                    this.safeApply();
                }
            }
        }
    }]);

    return FormValidator;
}();

exports.default = FormValidator;

},{}],32:[function(require,module,exports){
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
        value: function subscribeRequest(request, onSuccess, onError) {
            var rxObservable = this.rx.Observable.fromPromise(request);
            // Verify is not a duplicate call
            if (this.isSubscribed(rxObservable)) {
                this.cancelRequest(rxObservable);
            }

            // Subscribe the request
            var resultSubscribe = this.addSubscribe(rxObservable, onSuccess, onError);
            if (resultSubscribe && resultSubscribe.isStopped) {
                // An error happens, tracked by HttpInterceptorInterface
                delete this.promise[rxObservable._p];
            }
        }
    }, {
        key: "addSubscribe",
        value: function addSubscribe(rxObservable, onSuccess, onError) {
            var _this = this;

            this.promise[rxObservable._p] = rxObservable.subscribe(function (response) {
                return _this.onSubscribedSuccess(response, rxObservable, onSuccess);
            }, function (error) {
                return _this.onSubscribedError(error, rxObservable, onError);
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
        value: function onSubscribedSuccess(response, rxObservable, onSuccess) {
            if (this.isSubscribed(rxObservable)) {
                delete this.promise[rxObservable._p];
            }
            if (onSuccess) {
                return onSuccess(response.data);
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
        value: function onSubscribedError(error, rxObservable, onError) {
            if (this.isSubscribed(rxObservable)) {
                delete this.promise[rxObservable._p];
            }
            if (onError) {
                return onError({});
            }
        }
    }]);

    return RequestHandler;
}();

exports.default = RequestHandler;

},{}],33:[function(require,module,exports){
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

var _UserPreferencesService = require('./UserPreferencesService.js');

var _UserPreferencesService2 = _interopRequireDefault(_UserPreferencesService);

function _interopRequireDefault(obj) {
  return obj && obj.__esModule ? obj : { default: obj };
}

var RestAPIModule = _angular2.default.module('TDSTM.RestAPIModule', []);

RestAPIModule.service('RestServiceHandler', ['$log', '$http', '$resource', 'rx', _RestServiceHandler2.default]);
RestAPIModule.service('UserPreferencesService', ['$log', 'RestServiceHandler', _UserPreferencesService2.default]);

exports.default = RestAPIModule;

},{"./RestServiceHandler.js":34,"./UserPreferencesService.js":35,"angular":"angular"}],34:[function(require,module,exports){
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
        this.req = {
            method: '',
            url: '',
            headers: {
                'Content-Type': 'application/json;charset=UTF-8'
            },
            data: []
        };
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
        key: 'commonServiceHandler',
        value: function commonServiceHandler() {
            var _this2 = this;

            return {
                getTimeZoneConfiguration: function getTimeZoneConfiguration(onSuccess) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/user/preferences/CURR_DT_FORMAT,CURR_TZ'), onSuccess);
                }
            };
        }
    }, {
        key: 'licenseAdminServiceHandler',
        value: function licenseAdminServiceHandler() {
            var _this3 = this;

            return {
                getLicense: function getLicense(onSuccess) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/licenses'), onSuccess);
                },
                getEnvironmentDataSource: function getEnvironmentDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/license/environment'), onSuccess);
                },
                getProjectDataSource: function getProjectDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/license/project'), onSuccess);
                },
                getLicenseList: function getLicenseList(onSuccess) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/license'), onSuccess);
                },
                createNewLicenseRequest: function createNewLicenseRequest(data, onSuccess, onError) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/license/request';
                    _this3.req.data = data;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                applyLicense: function applyLicense(licenseId, data, onSuccess, onError) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/license/' + licenseId + '/load';
                    _this3.req.data = data;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                getHashCode: function getHashCode(licenseId, onSuccess, onError) {
                    _this3.req.method = 'GET';
                    _this3.req.url = '../ws/license/' + licenseId + '/hash';
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                getEmailContent: function getEmailContent(licenseId, onSuccess, onError) {
                    _this3.req.method = 'GET';
                    _this3.req.url = '../ws/license/' + licenseId + '/email/request';
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                //--------------------------------------------
                resubmitLicenseRequest: function resubmitLicenseRequest(licenseId, onSuccess, onError) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/license/' + licenseId + '/email/request';
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                emailRequest: function emailRequest(data, callback) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/???';
                    _this3.req.data = data;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
                },
                deleteLicense: function deleteLicense(data, onSuccess, onError) {
                    _this3.req.method = 'DELETE';
                    _this3.req.url = '../ws/license/' + data.id;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                }
            };
        }
    }, {
        key: 'licenseManagerServiceHandler',
        value: function licenseManagerServiceHandler() {
            var _this4 = this;

            return {
                requestImport: function requestImport(data, onSuccess, onError) {
                    _this4.req.method = 'POST';
                    _this4.req.url = '../ws/manager/license/request';
                    _this4.req.data = data;
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                },
                getLicenseList: function getLicenseList(onSuccess) {
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http.get('../ws/manager/license'), onSuccess);
                },
                getProjectDataSource: function getProjectDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http.get('../ws/license/project'), onSuccess);
                },
                getEnvironmentDataSource: function getEnvironmentDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http.get('../ws/license/environment'), onSuccess);
                },
                getKeyCode: function getKeyCode(licenseId, onSuccess, onError) {
                    _this4.req.method = 'GET';
                    _this4.req.url = '../ws/manager/license/' + licenseId + '/key';
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                },
                saveLicense: function saveLicense(licenseId, licenseModified, onSuccess, onError) {
                    _this4.req.method = 'PUT';
                    _this4.req.url = '../ws/manager/license/' + licenseId;
                    _this4.req.data = licenseModified;
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                },
                revokeLicense: function revokeLicense(data, onSuccess, onError) {
                    _this4.req.method = 'DELETE';
                    _this4.req.url = '../ws/manager/license/' + data.id;
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                },
                activateLicense: function activateLicense(licenseId, onSuccess, onError) {
                    _this4.req.method = 'POST';
                    _this4.req.url = '../ws/manager/license/' + licenseId + '/activate';
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                },
                getActivityLog: function getActivityLog(licenseId, onSuccess, onError) {
                    _this4.req.method = 'GET';
                    _this4.req.url = '../ws/manager/license/' + licenseId + '/activitylog';
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                },
                manuallyRequest: function manuallyRequest(licenseId, onSuccess, onError) {
                    _this4.req.method = 'POST';
                    _this4.req.url = '../ws/manager/license/' + licenseId + '/email/send';
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                }
            };
        }
    }, {
        key: 'noticeManagerServiceHandler',
        value: function noticeManagerServiceHandler() {
            var _this5 = this;

            return {
                getNoticeList: function getNoticeList(onSuccess) {
                    // real ws example
                    return new _RequestHandler2.default(_this5.rx).subscribeRequest(_this5.http.get('../ws/notices'), onSuccess);
                },
                createNotice: function createNotice(data, onSuccess, onError) {
                    _this5.req.method = 'POST';
                    _this5.req.url = '../ws/notices';
                    _this5.req.data = data;
                    return new _RequestHandler2.default(_this5.rx).subscribeRequest(_this5.http(_this5.req), onSuccess, onError);
                },
                editNotice: function editNotice(data, onSuccess, onError) {
                    _this5.req.method = 'PUT';
                    _this5.req.url = '../ws/notices/' + data.id;
                    _this5.req.data = data;
                    return new _RequestHandler2.default(_this5.rx).subscribeRequest(_this5.http(_this5.req), onSuccess, onError);
                },
                deleteNotice: function deleteNotice(data, onSuccess, onError) {
                    _this5.req.method = 'DELETE';
                    _this5.req.url = '../ws/notices/' + data.id;
                    return new _RequestHandler2.default(_this5.rx).subscribeRequest(_this5.http(_this5.req), onSuccess, onError);
                }
            };
        }
    }]);

    return RestServiceHandler;
}();

exports.default = RestServiceHandler;

},{"./RequestHandler.js":32}],35:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 3/7/2017.
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

var UserPreferencesService = function () {
    function UserPreferencesService($log, restServiceHandler) {
        _classCallCheck(this, UserPreferencesService);

        this.log = $log;
        this.restService = restServiceHandler;
        this.log.debug('UserPreferencesService Instanced');

        this.timeZoneConfiguration = {
            preferences: {}
        };
    }

    _createClass(UserPreferencesService, [{
        key: 'getTimeZoneConfiguration',
        value: function getTimeZoneConfiguration(onSuccess) {
            var _this = this;

            this.restService.commonServiceHandler().getTimeZoneConfiguration(function (data) {
                _this.timeZoneConfiguration = data.data;
                return onSuccess();
            });
        }
    }, {
        key: 'getConvertedDateIntoTimeZone',
        value: function getConvertedDateIntoTimeZone(dateString) {
            var timeString = dateString;
            var userDTFormat = this.timeZoneConfiguration.preferences.CURR_DT_FORMAT;
            var timeZone = this.timeZoneConfiguration.preferences.CURR_TZ;

            if (dateString !== 'null' && dateString && moment(dateString).isValid() && !(typeof Number(dateString.toString()) === 'number' && !isNaN(dateString.toString()))) {
                if (timeZone === null) {
                    timeZone = 'GMT';
                }
                var format = 'MM/DD/YYYY';
                if (userDTFormat === 'DD/MM/YYYY') {
                    format = 'DD/MM/YYYY';
                }
                timeString = moment(dateString).tz(timeZone).format(format);
            }

            return timeString !== 'null' ? timeString : '';
        }
    }, {
        key: 'getConvertedDateTimeIntoTimeZone',
        value: function getConvertedDateTimeIntoTimeZone(dateString) {
            var timeString = dateString;
            var userDTFormat = this.timeZoneConfiguration.preferences.CURR_DT_FORMAT;
            var timeZone = this.timeZoneConfiguration.preferences.CURR_TZ;

            if (dateString !== 'null' && dateString && moment(dateString).isValid() && !(typeof Number(dateString.toString()) === 'number' && !isNaN(dateString.toString()))) {
                if (timeZone === null) {
                    timeZone = 'GMT';
                }
                var format = 'MM/DD/YYYY hh:mm a';
                if (userDTFormat === 'DD/MM/YYYY') {
                    format = 'DD/MM/YYYY hh:mm a';
                }
                timeString = moment(dateString).tz(timeZone).format(format);
            }

            return timeString !== 'null' ? timeString : '';
        }

        /**
         * Kendo Date Format is quite different and threats the chars with a different meaning
         * this Functions return the one in the standar format.
         * Consider this is only from our Current Format
         */

    }, {
        key: 'getConvertedDateFormatToKendoDate',
        value: function getConvertedDateFormatToKendoDate() {
            var userDTFormat = this.timeZoneConfiguration.preferences.CURR_DT_FORMAT;

            var format = 'MM/dd/yyyy';
            if (userDTFormat !== null) {
                format = format.replace('DD', 'dd');
                format = format.replace('YYYY', 'yyyy');
            }

            return format;
        }
    }]);

    return UserPreferencesService;
}();

exports.default = UserPreferencesService;

},{}],36:[function(require,module,exports){
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

},{}],37:[function(require,module,exports){
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

},{"./HTTPRequestErrorHandlerInterceptor.js":38,"./HTTPRequestHandlerInterceptor.js":39,"./HTTPResponseErrorHandlerInterceptor.js":40,"./HTTPResponseHandlerInterceptor.js":41,"angular":"angular"}],38:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":36}],39:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":36}],40:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":36}],41:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":36}]},{},[6])
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxjb25maWdcXEFwcC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxNb2RhbFdpbmRvd0FjdGl2YXRpb24uanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxUb2FzdEhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1haW4uanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGRpYWxvZ0FjdGlvblxcRGlhbG9nQWN0aW9uLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxoZWFkZXJcXEhlYWRlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGhlYWRlclxcSGVhZGVyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXExpY2Vuc2VBZG1pbk1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxhcHBseUxpY2Vuc2VLZXlcXEFwcGx5TGljZW5zZUtleS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxjcmVhdGVkXFxDcmVhdGVkTGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxkZXRhaWxcXExpY2Vuc2VEZXRhaWwuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcbGlzdFxcTGljZW5zZUFkbWluTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxtYW51YWxseVJlcXVlc3RcXE1hbnVhbGx5UmVxdWVzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxyZXF1ZXN0XFxSZXF1ZXN0TGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxzZXJ2aWNlXFxMaWNlbnNlQWRtaW5TZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcTGljZW5zZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxkZXRhaWxcXExpY2Vuc2VNYW5hZ2VyRGV0YWlsLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcbGlzdFxcTGljZW5zZU1hbmFnZXJMaXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxccmVxdWVzdEltcG9ydFxcUmVxdWVzdEltcG9ydC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXHNlcnZpY2VcXExpY2Vuc2VNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcTm90aWNlTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcZWRpdFxcRWRpdE5vdGljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcbGlzdFxcTm90aWNlTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcc2VydmljZVxcTm90aWNlTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxUYXNrTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGVkaXRcXFRhc2tNYW5hZ2VyRWRpdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGxpc3RcXFRhc2tNYW5hZ2VyQ29udHJvbGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXHNlcnZpY2VcXFRhc2tNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdXRpbHNcXGZvcm1cXEZvcm1WYWxpZGF0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RBUElNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXN0U2VydmljZUhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUE1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBOzs7QUNB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LEFBQWtCLEFBQ2xCO21CQUFPLGFBQVksS0FBQSxBQUFLLEVBQUwsQUFBTyxLQUExQixBQUFtQixBQUFZLEFBQ2xDOzs7O3lDQUVnQixBQUNiO21CQUFPLEtBQUEsQUFBSyxNQUFaLEFBQWtCLEFBQ3JCOzs7OztHQXBCdUQsYzs7a0IsQUFBdkMiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8yMC8yMDE1LlxyXG4gKiBURFNNIGlzIGEgZ2xvYmFsIG9iamVjdCB0aGF0IGNvbWVzIGZyb20gQXBwLmpzXHJcbiAqXHJcbiAqIFRoZSBmb2xsb3dpbmcgaGVscGVyIHdvcmtzIGluIGEgd2F5IHRvIG1ha2UgYXZhaWxhYmxlIHRoZSBjcmVhdGlvbiBvZiBEaXJlY3RpdmUsIFNlcnZpY2VzIGFuZCBDb250cm9sbGVyXHJcbiAqIG9uIGZseSBvciB3aGVuIGRlcGxveWluZyB0aGUgYXBwLlxyXG4gKlxyXG4gKiBXZSByZWR1Y2UgdGhlIHVzZSBvZiBjb21waWxlIGFuZCBleHRyYSBzdGVwc1xyXG4gKi9cclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4vQXBwLmpzJyk7XHJcblxyXG4vKipcclxuICogTGlzdGVuIHRvIGFuIGV4aXN0aW5nIGRpZ2VzdCBvZiB0aGUgY29tcGlsZSBwcm92aWRlciBhbmQgZXhlY3V0ZSB0aGUgJGFwcGx5IGltbWVkaWF0ZWx5IG9yIGFmdGVyIGl0J3MgcmVhZHlcclxuICogQHBhcmFtIGN1cnJlbnRcclxuICogQHBhcmFtIGZuXHJcbiAqL1xyXG5URFNUTS5zYWZlQXBwbHkgPSBmdW5jdGlvbiAoY3VycmVudCwgZm4pIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciBwaGFzZSA9IGN1cnJlbnQuJHJvb3QuJCRwaGFzZTtcclxuICAgIGlmIChwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRldmFsKGZuKTtcclxuICAgICAgICB9XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRhcHBseShmbik7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgY3VycmVudC4kYXBwbHkoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBkaXJlY3RpdmUgYXN5bmMgaWYgdGhlIGNvbXBpbGVQcm92aWRlciBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIuZGlyZWN0aXZlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5kaXJlY3RpdmUpIHtcclxuICAgICAgICBURFNUTS5kaXJlY3RpdmUoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBjb250cm9sbGVycyBhc3luYyBpZiB0aGUgY29udHJvbGxlclByb3ZpZGVyIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlQ29udHJvbGxlciA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbnRyb2xsZXJQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXJQcm92aWRlci5yZWdpc3RlcihzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uY29udHJvbGxlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXIoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBzZXJ2aWNlIGFzeW5jIGlmIHRoZSBwcm92aWRlU2VydmljZSBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZVNlcnZpY2UgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZSkge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZS5zZXJ2aWNlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5jb250cm9sbGVyKSB7XHJcbiAgICAgICAgVERTVE0uc2VydmljZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBGb3IgTGVnYWN5IHN5c3RlbSwgd2hhdCBpcyBkb2VzIGlzIHRvIHRha2UgcGFyYW1zIGZyb20gdGhlIHF1ZXJ5XHJcbiAqIG91dHNpZGUgdGhlIEFuZ3VsYXJKUyB1aS1yb3V0aW5nLlxyXG4gKiBAcGFyYW0gcGFyYW0gLy8gUGFyYW0gdG8gc2VhcmMgZm9yIC9leGFtcGxlLmh0bWw/YmFyPWZvbyNjdXJyZW50U3RhdGVcclxuICovXHJcblREU1RNLmdldFVSTFBhcmFtID0gZnVuY3Rpb24gKHBhcmFtKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICAkLnVybFBhcmFtID0gZnVuY3Rpb24gKG5hbWUpIHtcclxuICAgICAgICB2YXIgcmVzdWx0cyA9IG5ldyBSZWdFeHAoJ1tcXD8mXScgKyBuYW1lICsgJz0oW14mI10qKScpLmV4ZWMod2luZG93LmxvY2F0aW9uLmhyZWYpO1xyXG4gICAgICAgIGlmIChyZXN1bHRzID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgIHJldHVybiBudWxsO1xyXG4gICAgICAgIH1cclxuICAgICAgICBlbHNlIHtcclxuICAgICAgICAgICAgcmV0dXJuIHJlc3VsdHNbMV0gfHwgMDtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG5cclxuICAgIHJldHVybiAkLnVybFBhcmFtKHBhcmFtKTtcclxufTtcclxuXHJcbi8qKlxyXG4gKiBUaGlzIGNvZGUgd2FzIGludHJvZHVjZWQgb25seSBmb3IgdGhlIGlmcmFtZSBtaWdyYXRpb25cclxuICogaXQgZGV0ZWN0IHdoZW4gbW91c2UgZW50ZXJcclxuICovXHJcblREU1RNLmlmcmFtZUxvYWRlciA9IGZ1bmN0aW9uICgpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgICQoJy5pZnJhbWVMb2FkZXInKS5ob3ZlcihcclxuICAgICAgICBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICQoJy5uYXZiYXItdWwtY29udGFpbmVyIC5kcm9wZG93bi5vcGVuJykucmVtb3ZlQ2xhc3MoJ29wZW4nKTtcclxuICAgICAgICB9LCBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgfVxyXG4gICAgKTtcclxufTtcclxuXHJcblREU1RNLmdldENvbnZlcnRlZERhdGVGb3JtYXQgPSBmdW5jdGlvbiggZGF0ZVN0cmluZywgdXNlckRURm9ybWF0LCB0aW1lWm9uZSApIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciB0aW1lU3RyaW5nID0gJyc7XHJcbiAgICBpZihkYXRlU3RyaW5nKXtcclxuICAgICAgICBpZiAodGltZVpvbmUgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgdGltZVpvbmUgPSAnR01UJztcclxuICAgICAgICB9XHJcbiAgICAgICAgdmFyIGZvcm1hdCA9ICdNTS9ERC9ZWVlZJztcclxuICAgICAgICBpZiAodXNlckRURm9ybWF0ID09PSAnREQvTU0vWVlZWScpIHtcclxuICAgICAgICAgICAgZm9ybWF0ID0gJ0REL01NL1lZWVknO1xyXG4gICAgICAgIH1cclxuICAgICAgICAvLyBDb252ZXJ0IHp1bHUgZGF0ZXRpbWUgdG8gYSBzcGVjaWZpYyB0aW1lem9uZS9mb3JtYXRcclxuICAgICAgICB0aW1lU3RyaW5nID0gbW9tZW50KGRhdGVTdHJpbmcpLnR6KHRpbWVab25lKS5mb3JtYXQoZm9ybWF0KVxyXG4gICAgfVxyXG4gICAgcmV0dXJuIHRpbWVTdHJpbmc7XHJcbn07XHJcblxyXG53aW5kb3cuVERTVE0gPSBURFNUTTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzE2LzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxucmVxdWlyZSgnYW5ndWxhcicpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWFuaW1hdGUnKTtcclxucmVxdWlyZSgnYW5ndWxhci1tb2NrcycpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXNhbml0aXplJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItcmVzb3VyY2UnKTtcclxucmVxdWlyZSgnYW5ndWxhci10cmFuc2xhdGUnKTtcclxucmVxdWlyZSgnYW5ndWxhci10cmFuc2xhdGUtbG9hZGVyLXBhcnRpYWwnKTtcclxucmVxdWlyZSgnYW5ndWxhci11aS1ib290c3RyYXAnKTtcclxucmVxdWlyZSgnbmdjbGlwYm9hcmQnKTtcclxucmVxdWlyZSgndWktcm91dGVyJyk7XHJcbnJlcXVpcmUoJ3J4LWFuZ3VsYXInKTtcclxucmVxdWlyZSgnYXBpLWNoZWNrJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItZm9ybWx5Jyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItZm9ybWx5LXRlbXBsYXRlcy1ib290c3RyYXAnKTtcclxuXHJcbi8vIE1vZHVsZXNcclxuaW1wb3J0IEhUVFBNb2R1bGUgZnJvbSAnLi4vc2VydmljZXMvaHR0cC9IVFRQTW9kdWxlLmpzJztcclxuaW1wb3J0IFJlc3RBUElNb2R1bGUgZnJvbSAnLi4vc2VydmljZXMvUmVzdEFQSS9SZXN0QVBJTW9kdWxlLmpzJ1xyXG5pbXBvcnQgSGVhZGVyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvaGVhZGVyL0hlYWRlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBMaWNlbnNlQWRtaW5Nb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9saWNlbnNlQWRtaW4vTGljZW5zZUFkbWluTW9kdWxlLmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvTGljZW5zZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTm90aWNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL25vdGljZU1hbmFnZXIvTm90aWNlTWFuYWdlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL3Rhc2tNYW5hZ2VyL1Rhc2tNYW5hZ2VyTW9kdWxlLmpzJztcclxuXHJcbnZhciBQcm92aWRlckNvcmUgPSB7fTtcclxuXHJcbnZhciBURFNUTSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTScsIFtcclxuICAgICduZ1Nhbml0aXplJyxcclxuICAgICduZ1Jlc291cmNlJyxcclxuICAgICduZ0FuaW1hdGUnLFxyXG4gICAgJ3Bhc2NhbHByZWNodC50cmFuc2xhdGUnLCAvLyAnYW5ndWxhci10cmFuc2xhdGUnXHJcbiAgICAndWkucm91dGVyJyxcclxuICAgICduZ2NsaXBib2FyZCcsXHJcbiAgICAna2VuZG8uZGlyZWN0aXZlcycsXHJcbiAgICAncngnLFxyXG4gICAgJ2Zvcm1seScsXHJcbiAgICAnZm9ybWx5Qm9vdHN0cmFwJyxcclxuICAgICd1aS5ib290c3RyYXAnLFxyXG4gICAgSFRUUE1vZHVsZS5uYW1lLFxyXG4gICAgUmVzdEFQSU1vZHVsZS5uYW1lLFxyXG4gICAgSGVhZGVyTW9kdWxlLm5hbWUsXHJcbiAgICBUYXNrTWFuYWdlck1vZHVsZS5uYW1lLFxyXG4gICAgTGljZW5zZUFkbWluTW9kdWxlLm5hbWUsXHJcbiAgICBMaWNlbnNlTWFuYWdlck1vZHVsZS5uYW1lLFxyXG4gICAgTm90aWNlTWFuYWdlck1vZHVsZS5uYW1lXHJcbl0pLmNvbmZpZyhbXHJcbiAgICAnJGxvZ1Byb3ZpZGVyJyxcclxuICAgICckcm9vdFNjb3BlUHJvdmlkZXInLFxyXG4gICAgJyRjb21waWxlUHJvdmlkZXInLFxyXG4gICAgJyRjb250cm9sbGVyUHJvdmlkZXInLFxyXG4gICAgJyRwcm92aWRlJyxcclxuICAgICckaHR0cFByb3ZpZGVyJyxcclxuICAgICckdHJhbnNsYXRlUHJvdmlkZXInLFxyXG4gICAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgJyR1cmxSb3V0ZXJQcm92aWRlcicsXHJcbiAgICAnJGxvY2F0aW9uUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRsb2dQcm92aWRlciwgJHJvb3RTY29wZVByb3ZpZGVyLCAkY29tcGlsZVByb3ZpZGVyLCAkY29udHJvbGxlclByb3ZpZGVyLCAkcHJvdmlkZSwgJGh0dHBQcm92aWRlcixcclxuICAgICAgICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIsICR1cmxSb3V0ZXJQcm92aWRlciwgJGxvY2F0aW9uUHJvdmlkZXIpIHtcclxuXHJcbiAgICAgICAgJHJvb3RTY29wZVByb3ZpZGVyLmRpZ2VzdFR0bCgzMCk7XHJcbiAgICAgICAgLy8gR29pbmcgYmFjayB0byB5b3VcclxuICAgICAgICAkbG9jYXRpb25Qcm92aWRlci5odG1sNU1vZGUodHJ1ZSkuaGFzaFByZWZpeCgnIScpO1xyXG5cclxuICAgICAgICAkbG9nUHJvdmlkZXIuZGVidWdFbmFibGVkKHRydWUpO1xyXG5cclxuICAgICAgICAvLyBBZnRlciBib290c3RyYXBwaW5nIGFuZ3VsYXIgZm9yZ2V0IHRoZSBwcm92aWRlciBzaW5jZSBldmVyeXRoaW5nIFwid2FzIGFscmVhZHkgbG9hZGVkXCJcclxuICAgICAgICBQcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyID0gJGNvbXBpbGVQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUuY29udHJvbGxlclByb3ZpZGVyID0gJGNvbnRyb2xsZXJQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2UgPSAkcHJvdmlkZTtcclxuICAgICAgICBQcm92aWRlckNvcmUuaHR0cFByb3ZpZGVyID0gJGh0dHBQcm92aWRlcjtcclxuXHJcbiAgICAgICAgLyoqXHJcbiAgICAgICAgICogVHJhbnNsYXRpb25zXHJcbiAgICAgICAgICovXHJcblxyXG4gICAgICAgIC8qICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIudXNlU2FuaXRpemVWYWx1ZVN0cmF0ZWd5KG51bGwpO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ3Rkc3RtJyk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci51c2VMb2FkZXIoJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyJywge1xyXG4gICAgICAgICAgICB1cmxUZW1wbGF0ZTogJy4uL2kxOG4ve3BhcnR9L2FwcC5pMThuLXtsYW5nfS5qc29uJ1xyXG4gICAgICAgIH0pOyovXHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci5wcmVmZXJyZWRMYW5ndWFnZSgnZW5fVVMnKTtcclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIuZmFsbGJhY2tMYW5ndWFnZSgnZW5fVVMnKTtcclxuXHJcbiAgICAgICAgLy8kdXJsUm91dGVyUHJvdmlkZXIub3RoZXJ3aXNlKCdkYXNoYm9hcmQnKTtcclxuXHJcbiAgICB9XSkuXHJcbiAgICBydW4oWyckdHJhbnNpdGlvbnMnLCAnJGh0dHAnLCAnJGxvZycsICckbG9jYXRpb24nLCAnJHEnLCdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgZnVuY3Rpb24gKCR0cmFuc2l0aW9ucywgJGh0dHAsICRsb2csICRsb2NhdGlvbiwgJHEsIHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UpIHtcclxuICAgICAgICAkbG9nLmRlYnVnKCdDb25maWd1cmF0aW9uIGRlcGxveWVkJyk7XHJcblxyXG4gICAgICAgICR0cmFuc2l0aW9ucy5vbkJlZm9yZSgge30sICgkc3RhdGUsICR0cmFuc2l0aW9uJCkgPT4ge1xyXG4gICAgICAgICAgICB2YXIgZGVmZXIgPSAkcS5kZWZlcigpO1xyXG5cclxuICAgICAgICAgICAgdXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24oKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgZGVmZXIucmVzb2x2ZSgpO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIHJldHVybiBkZWZlci5wcm9taXNlO1xyXG4gICAgICAgIH0pO1xyXG5cclxuICAgIH1dKTtcclxuXHJcbi8vIHdlIG1hcHBlZCB0aGUgUHJvdmlkZXIgQ29yZSBsaXN0IChjb21waWxlUHJvdmlkZXIsIGNvbnRyb2xsZXJQcm92aWRlciwgcHJvdmlkZVNlcnZpY2UsIGh0dHBQcm92aWRlcikgdG8gcmV1c2UgYWZ0ZXIgb24gZmx5XHJcblREU1RNLlByb3ZpZGVyQ29yZSA9IFByb3ZpZGVyQ29yZTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBJdCBoYW5kbGVyIHRoZSBpbmRleCBmb3IgYW55IG9mIHRoZSBkaXJlY3RpdmVzIGF2YWlsYWJsZVxyXG4gKi9cclxuXHJcbnJlcXVpcmUoJy4vdG9vbHMvVG9hc3RIYW5kbGVyLmpzJyk7XHJcbnJlcXVpcmUoJy4vdG9vbHMvTW9kYWxXaW5kb3dBY3RpdmF0aW9uLmpzJyk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMzAvMTAvMjAxNi5cclxuICogTGlzdGVuIHRvIE1vZGFsIFdpbmRvdyB0byBtYWtlIGFueSBtb2RhbCB3aW5kb3cgZHJhZ2dhYmJsZVxyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi4vLi4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlKCdtb2RhbFJlbmRlcicsIFsnJGxvZycsIGZ1bmN0aW9uICgkbG9nKSB7XHJcbiAgICAkbG9nLmRlYnVnKCdNb2RhbFdpbmRvd0FjdGl2YXRpb24gbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHJlc3RyaWN0OiAnRUEnLFxyXG4gICAgICAgIGxpbms6IGZ1bmN0aW9uKCkge1xyXG4gICAgICAgICAgICAkKCcubW9kYWwtZGlhbG9nJykuZHJhZ2dhYmxlKHtcclxuICAgICAgICAgICAgICAgIGhhbmRsZTogJy5tb2RhbC1oZWFkZXInXHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH07XHJcbn1dKTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzE0LzIwMTUuXHJcbiAqIFByaW50cyBvdXQgYWxsIFRvYXN0IG1lc3NhZ2Ugd2hlbiBkZXRlY3RlZCBmcm9tIHNlcnZlciBvciBjdXN0b20gbXNnIHVzaW5nIHRoZSBkaXJlY3RpdmUgaXRzZWxmXHJcbiAqXHJcbiAqIFByb2JhYmx5IHZhbHVlcyBhcmU6XHJcbiAqXHJcbiAqIHN1Y2Nlc3MsIGRhbmdlciwgaW5mbywgd2FybmluZ1xyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi4vLi4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlKCd0b2FzdEhhbmRsZXInLCBbJyRsb2cnLCAnJHRpbWVvdXQnLCAnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InLCAnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICAnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyxcclxuICAgIGZ1bmN0aW9uICgkbG9nLCAkdGltZW91dCwgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IsXHJcbiAgICAgICAgICAgICAgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcikge1xyXG5cclxuICAgICRsb2cuZGVidWcoJ1RvYXN0SGFuZGxlciBsb2FkZWQnKTtcclxuICAgIHJldHVybiB7XHJcbiAgICAgICAgc2NvcGU6IHtcclxuICAgICAgICAgICAgbXNnOiAnPScsXHJcbiAgICAgICAgICAgIHR5cGU6ICc9JyxcclxuICAgICAgICAgICAgc3RhdHVzOiAnPSdcclxuICAgICAgICB9LFxyXG4gICAgICAgIHByaW9yaXR5OiA1LFxyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL2RpcmVjdGl2ZXMvdG9vbHMvVG9hc3RIYW5kbGVyLmh0bWwnLFxyXG4gICAgICAgIHJlc3RyaWN0OiAnRScsXHJcbiAgICAgICAgY29udHJvbGxlcjogWyckc2NvcGUnLCAnJHJvb3RTY29wZScsIGZ1bmN0aW9uICgkc2NvcGUsICRyb290U2NvcGUpIHtcclxuICAgICAgICAgICAgJHNjb3BlLmFsZXJ0ID0ge1xyXG4gICAgICAgICAgICAgICAgc3VjY2Vzczoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGltZTogMjAwMFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGRhbmdlcjoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGltZTogNDAwMFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGluZm86IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRpbWU6IDIwMDBcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICB3YXJuaW5nOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJyxcclxuICAgICAgICAgICAgICAgICAgICB0aW1lOiA0MDAwXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzID0ge1xyXG4gICAgICAgICAgICAgICAgc2hvdzogZmFsc2VcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIGZ1bmN0aW9uIHR1cm5PZmZOb3RpZmljYXRpb25zKCl7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuc3VjY2Vzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5pbmZvLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC53YXJuaW5nLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBMaXN0ZW4gdG8gYW55IHJlcXVlc3QsIHdlIGNhbiByZWdpc3RlciBsaXN0ZW5lciBpZiB3ZSB3YW50IHRvIGFkZCBleHRyYSBjb2RlLlxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVxdWVzdCgpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24oY29uZmlnKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgdG86ICcsICBjb25maWcpO1xyXG4gICAgICAgICAgICAgICAgdmFyIHRpbWUgPSBjb25maWcucmVxdWVzdFRpbWVzdGFtcDtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcodGltZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgZXJyb3I6ICcsICByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVzcG9uc2UoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlc3BvbnNlKXtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wIC0gcmVzcG9uc2UuY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdUaGUgcmVxdWVzdCB0b29rICcgKyAodGltZSAvIDEwMDApICsgJyBzZWNvbmRzJyk7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSByZXN1bHQ6ICcsIHJlc3BvbnNlKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcblxyXG4gICAgICAgICAgICAgICAgaWYocmVzcG9uc2UgJiYgcmVzcG9uc2UuaGVhZGVycyAmJiByZXNwb25zZS5oZWFkZXJzKCd4LWxvZ2luLXVybCcpKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgd2luZG93LmxvY2F0aW9uLmhyZWYgPSByZXNwb25zZS5oZWFkZXJzKCd4LWxvZ2luLXVybCcpO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmxpc3RlbkVycm9yKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZWplY3Rpb24pe1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVzcG9uc2UgZXJyb3I6ICcsIHJlamVjdGlvbik7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc3RhdHVzID0gcmVqZWN0aW9uLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc3RhdHVzVGV4dCA9IHJlamVjdGlvbi5zdGF0dXNUZXh0O1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5lcnJvcnMgPSByZWplY3Rpb24uZGF0YS5lcnJvcnM7XHJcbiAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMzAwMCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEhpZGUgdGhlIFBvcCB1cCBub3RpZmljYXRpb24gbWFudWFsbHlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRzY29wZS5vbkNhbmNlbFBvcFVwID0gZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgICAgICAgICB0dXJuT2ZmTm90aWZpY2F0aW9ucygpO1xyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkcm9vdFNjb3BlLiRvbignYnJvYWRjYXN0LW1zZycsIGZ1bmN0aW9uKGV2ZW50LCBhcmdzKSB7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdicm9hZGNhc3QtbXNnIGV4ZWN1dGVkJyk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnN0YXR1c1RleHQgPSBhcmdzLnRleHQ7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zdGF0dXMgPSBudWxsO1xyXG4gICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS50aW1lKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS4kYXBwbHkoKTsgLy8gcm9vdFNjb3BlIGFuZCB3YXRjaCBleGNsdWRlIHRoZSBhcHBseSBhbmQgbmVlZHMgdGhlIG5leHQgY3ljbGUgdG8gcnVuXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUuJHdhdGNoKCdtc2cnLCBmdW5jdGlvbihuZXdWYWx1ZSwgb2xkVmFsdWUpIHtcclxuICAgICAgICAgICAgICAgIGlmIChuZXdWYWx1ZSAmJiBuZXdWYWx1ZSAhPT0gJycpIHtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzVGV4dCA9IG5ld1ZhbHVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzID0gJHNjb3BlLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMjUwMCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICB9XVxyXG4gICAgfTtcclxufV0pO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzE3LzIwMTUuXHJcbiAqL1xyXG5cclxuLy8gTWFpbiBBbmd1bGFySnMgY29uZmlndXJhdGlvblxyXG5yZXF1aXJlKCcuL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcbi8vIEhlbHBlcnNcclxucmVxdWlyZSgnLi9jb25maWcvQW5ndWxhclByb3ZpZGVySGVscGVyLmpzJyk7XHJcblxyXG4vLyBEaXJlY3RpdmVzXHJcbnJlcXVpcmUoJy4vZGlyZWN0aXZlcy9pbmRleCcpO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRGlhbG9nQWN0aW9uIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy50aXRsZSA9IHBhcmFtcy50aXRsZTtcclxuICAgICAgICB0aGlzLm1lc3NhZ2UgPSBwYXJhbXMubWVzc2FnZTtcclxuXHJcbiAgICB9XHJcbiAgICAvKipcclxuICAgICAqIEFjY2NlcHQgYW5kIENvbmZpcm1cclxuICAgICAqL1xyXG4gICAgY29uZmlybUFjdGlvbigpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yLzIwMTUuXHJcbiAqIEhlYWRlciBDb250cm9sbGVyIG1hbmFnZSB0aGUgdmlldyBhdmFpbGFibGUgb24gdGhlIHN0YXRlLmRhdGFcclxuICogLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gKiBIZWFkZXIgQ29udHJvbGxlclxyXG4gKiBQYWdlIHRpdGxlICAgICAgICAgICAgICAgICAgICAgIEhvbWUgLT4gTGF5b3V0IC0gU3ViIExheW91dFxyXG4gKlxyXG4gKiBNb2R1bGUgQ29udHJvbGxlclxyXG4gKiBDb250ZW50XHJcbiAqIC0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIZWFkZXJDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2dcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG5cclxuICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICBpbnN0cnVjdGlvbjogJycsXHJcbiAgICAgICAgICAgIG1lbnU6IFtdXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hlYWRlciBDb250cm9sbGVyIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmVyaWZ5IGlmIHdlIGhhdmUgYSBtZW51IHRvIHNob3cgdG8gbWFkZSBpdCBhdmFpbGFibGUgdG8gdGhlIFZpZXdcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUhlYWRlcigpIHtcclxuICAgICAgICBpZiAodGhpcy5zdGF0ZSAmJiB0aGlzLnN0YXRlLiRjdXJyZW50ICYmIHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YSkge1xyXG4gICAgICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YS5wYWdlO1xyXG4gICAgICAgICAgICBkb2N1bWVudC50aXRsZSA9IHRoaXMucGFnZU1ldGFEYXRhLnRpdGxlO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIxLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgSGVhZGVyQ29udHJvbGxlciBmcm9tICcuL0hlYWRlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgRGlhbG9nQWN0aW9uIGZyb20gJy4uL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uanMnO1xyXG5cclxudmFyIEhlYWRlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5IZWFkZXJNb2R1bGUnLCBbXSk7XHJcblxyXG5IZWFkZXJNb2R1bGUuY29udHJvbGxlcignSGVhZGVyQ29udHJvbGxlcicsIFsnJGxvZycsICckc3RhdGUnLCBIZWFkZXJDb250cm9sbGVyXSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbkhlYWRlck1vZHVsZS5jb250cm9sbGVyKCdEaWFsb2dBY3Rpb24nLCBbJyRsb2cnLCckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRGlhbG9nQWN0aW9uXSk7XHJcblxyXG4vKlxyXG4gKiBGaWx0ZXIgY2hhbmdlIHRoZSBkYXRlIGludG8gYSBwcm9wZXIgZm9ybWF0IHRpbWV6b25lIGRhdGVcclxuICovXHJcbkhlYWRlck1vZHVsZS5maWx0ZXIoJ2NvbnZlcnREYXRlSW50b1RpbWVab25lJywgWydVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgZnVuY3Rpb24gKHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UpIHtcclxuICAgIHJldHVybiAoZGF0ZVN0cmluZykgPT4gdXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlSW50b1RpbWVab25lKGRhdGVTdHJpbmcpO1xyXG59XSk7XHJcblxyXG5IZWFkZXJNb2R1bGUuZmlsdGVyKCdjb252ZXJ0RGF0ZVRpbWVJbnRvVGltZVpvbmUnLCBbJ1VzZXJQcmVmZXJlbmNlc1NlcnZpY2UnLCBmdW5jdGlvbiAodXNlclByZWZlcmVuY2VzU2VydmljZSkge1xyXG4gICAgcmV0dXJuIChkYXRlU3RyaW5nKSA9PiB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlLmdldENvbnZlcnRlZERhdGVUaW1lSW50b1RpbWVab25lKGRhdGVTdHJpbmcpO1xyXG59XSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIZWFkZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTGljZW5zZUFkbWluTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZUFkbWluTGlzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlQWRtaW5TZXJ2aWNlIGZyb20gJy4vc2VydmljZS9MaWNlbnNlQWRtaW5TZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RMaWNlbnNlIGZyb20gJy4vcmVxdWVzdC9SZXF1ZXN0TGljZW5zZS5qcyc7XHJcbmltcG9ydCBDcmVhdGVkTGljZW5zZSBmcm9tICcuL2NyZWF0ZWQvQ3JlYXRlZExpY2Vuc2UuanMnO1xyXG5pbXBvcnQgQXBwbHlMaWNlbnNlS2V5IGZyb20gJy4vYXBwbHlMaWNlbnNlS2V5L0FwcGx5TGljZW5zZUtleS5qcyc7XHJcbmltcG9ydCBNYW51YWxseVJlcXVlc3QgZnJvbSAnLi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZURldGFpbC5qcyc7XHJcblxyXG5cclxudmFyIExpY2Vuc2VBZG1pbk1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlQWRtaW5Nb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsICckbG9jYXRpb25Qcm92aWRlcicsXHJcblx0XHRmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIsICRsb2NhdGlvblByb3ZpZGVyKSB7XHJcblxyXG5cdFx0JHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdsaWNlbnNlQWRtaW4nKTtcclxuXHJcblx0XHQvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG5cdFx0dmFyIGhlYWRlciA9IHtcclxuXHRcdFx0XHR0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG5cdFx0XHRcdGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuXHRcdH07XHJcblxyXG5cdFx0JHN0YXRlUHJvdmlkZXJcclxuXHRcdFx0XHQuc3RhdGUoJ2xpY2Vuc2VBZG1pbkxpc3QnLCB7XHJcblx0XHRcdFx0XHRcdGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdBZG1pbmlzdGVyIExpY2Vuc2VzJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FkbWluJywgJ0xpY2Vuc2UnLCAnTGlzdCddfX0sXHJcblx0XHRcdFx0XHRcdHVybDogJy9saWNlbnNlL2FkbWluL2xpc3QnLFxyXG5cdFx0XHRcdFx0XHR2aWV3czoge1xyXG5cdFx0XHRcdFx0XHRcdFx0J2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG5cdFx0XHRcdFx0XHRcdFx0J2JvZHlWaWV3QCc6IHtcclxuXHRcdFx0XHRcdFx0XHRcdFx0XHR0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9saXN0L0xpY2Vuc2VBZG1pbkxpc3QuaHRtbCcsXHJcblx0XHRcdFx0XHRcdFx0XHRcdFx0Y29udHJvbGxlcjogJ0xpY2Vuc2VBZG1pbkxpc3QgYXMgbGljZW5zZUFkbWluTGlzdCdcclxuXHRcdFx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuc2VydmljZSgnTGljZW5zZUFkbWluU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCAnJHJvb3RTY29wZScsIExpY2Vuc2VBZG1pblNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlQWRtaW5MaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsIExpY2Vuc2VBZG1pbkxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ1JlcXVlc3RMaWNlbnNlJywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RMaWNlbnNlXSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdDcmVhdGVkTGljZW5zZScsIFsnJGxvZycsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBDcmVhdGVkTGljZW5zZV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignQXBwbHlMaWNlbnNlS2V5JywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBBcHBseUxpY2Vuc2VLZXldKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ01hbnVhbGx5UmVxdWVzdCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBNYW51YWxseVJlcXVlc3RdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VEZXRhaWwnLCBbJyRsb2cnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTGljZW5zZURldGFpbF0pO1xyXG5cclxuLypcclxuICogRmlsdGVyIHRvIFVSTCBFbmNvZGUgdGV4dCBmb3IgdGhlICdtYWlsdG8nXHJcbiAqL1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuZmlsdGVyKCdlc2NhcGVVUkxFbmNvZGluZycsIGZ1bmN0aW9uICgpIHtcclxuXHRyZXR1cm4gZnVuY3Rpb24gKHRleHQpIHtcclxuXHRcdGlmKHRleHQpe1xyXG5cdFx0XHR0ZXh0ID0gZW5jb2RlVVJJKHRleHQpO1xyXG5cdFx0fVxyXG5cdFx0cmV0dXJuIHRleHQ7XHJcblx0fVxyXG59KTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VBZG1pbk1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEFwcGx5TGljZW5zZUtleSBleHRlbmRzIEZvcm1WYWxpZGF0b3J7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpXHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAga2V5OiBwYXJhbXMubGljZW5zZS5rZXlcclxuICAgICAgICB9XHJcbiAgICAgICAgO1xyXG4gICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIGFwcGx5S2V5KCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5hcHBseUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0sIChkYXRhKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQ3JlYXRlZFJlcXVlc3RMaWNlbnNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5jbGllbnQgPSBwYXJhbXM7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlRGV0YWlsIHtcclxuXHJcblx0XHRjb25zdHJ1Y3RvcigkbG9nLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuXHRcdFx0XHR0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG5cdFx0XHRcdHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cdFx0XHRcdHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuXHRcdFx0XHR0aGlzLmxvZyA9ICRsb2c7XHJcblx0XHRcdFx0dGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcblx0XHRcdFx0XHRcdG1ldGhvZDoge1xyXG5cdFx0XHRcdFx0XHRcdFx0bmFtZTogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLm5hbWUsXHJcblx0XHRcdFx0XHRcdFx0XHRtYXg6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5tYXhcclxuXHRcdFx0XHRcdFx0fSxcclxuXHRcdFx0XHRcdFx0cHJvamVjdE5hbWU6IHBhcmFtcy5saWNlbnNlLnByb2plY3QubmFtZSxcclxuXHRcdFx0XHRcdFx0Y2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcblx0XHRcdFx0XHRcdGVtYWlsOiBwYXJhbXMubGljZW5zZS5lbWFpbCxcclxuXHRcdFx0XHRcdFx0dG9FbWFpbDogcGFyYW1zLmxpY2Vuc2UudG9FbWFpbCxcclxuXHRcdFx0XHRcdFx0ZW52aXJvbm1lbnQ6IHBhcmFtcy5saWNlbnNlLmVudmlyb25tZW50LFxyXG5cdFx0XHRcdFx0XHRpbmNlcHRpb246IHBhcmFtcy5saWNlbnNlLnJlcXVlc3REYXRlLFxyXG5cdFx0XHRcdFx0XHRleHBpcmF0aW9uOiBwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSxcclxuXHRcdFx0XHRcdFx0cmVxdWVzdE5vdGU6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3ROb3RlLFxyXG5cdFx0XHRcdFx0XHRhY3RpdmU6IHBhcmFtcy5saWNlbnNlLnN0YXR1cyA9PT0gJ0FDVElWRScsXHJcblx0XHRcdFx0XHRcdGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuXHRcdFx0XHRcdFx0cmVwbGFjZWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkLFxyXG5cdFx0XHRcdFx0XHRlbmNyeXB0ZWREZXRhaWw6IHBhcmFtcy5saWNlbnNlLmVuY3J5cHRlZERldGFpbCxcclxuXHRcdFx0XHRcdFx0YXBwbGllZDogZmFsc2VcclxuXHRcdFx0XHR9O1xyXG5cclxuXHRcdFx0XHR0aGlzLnByZXBhcmVNZXRob2RPcHRpb25zKCk7XHJcblx0XHR9XHJcblxyXG5cdFx0cHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcblx0XHRcdFx0dGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG5cdFx0XHRcdFx0XHR7XHJcblx0XHRcdFx0XHRcdFx0XHRuYW1lOiAnTUFYX1NFUlZFUlMnLFxyXG5cdFx0XHRcdFx0XHRcdFx0dGV4dDogJ1NlcnZlcnMnXHJcblx0XHRcdFx0XHRcdH0sXHJcblx0XHRcdFx0XHRcdHtcclxuXHRcdFx0XHRcdFx0XHRcdG5hbWU6ICdUT0tFTicsXHJcblx0XHRcdFx0XHRcdFx0XHR0ZXh0OiAnVG9rZW5zJ1xyXG5cdFx0XHRcdFx0XHR9LFxyXG5cdFx0XHRcdFx0XHR7XHJcblx0XHRcdFx0XHRcdFx0XHRuYW1lOiAnQ1VTVE9NJyxcclxuXHRcdFx0XHRcdFx0XHRcdHRleHQ6ICdDdXN0b20nXHJcblx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHRdXHJcblx0XHR9XHJcblxyXG5cdFx0LyoqXHJcblx0XHQgKiBUaGUgdXNlciBhcHBseSBhbmQgc2VydmVyIHNob3VsZCB2YWxpZGF0ZSB0aGUga2V5IGlzIGNvcnJlY3RcclxuXHRcdCAqL1xyXG5cdFx0YXBwbHlMaWNlbnNlS2V5KCkge1xyXG5cdFx0XHRcdHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuXHRcdFx0XHRcdFx0YW5pbWF0aW9uOiB0cnVlLFxyXG5cdFx0XHRcdFx0XHR0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5Lmh0bWwnLFxyXG5cdFx0XHRcdFx0XHRjb250cm9sbGVyOiAnQXBwbHlMaWNlbnNlS2V5IGFzIGFwcGx5TGljZW5zZUtleScsXHJcblx0XHRcdFx0XHRcdHNpemU6ICdtZCcsXHJcblx0XHRcdFx0XHRcdHJlc29sdmU6IHtcclxuXHRcdFx0XHRcdFx0XHRcdHBhcmFtczogKCkgPT4ge1xyXG5cdFx0XHRcdFx0XHRcdFx0XHRcdHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcblx0XHRcdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHR9KTtcclxuXHJcblx0XHRcdFx0bW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoZGF0YSkgPT4ge1xyXG5cdFx0XHRcdFx0XHR0aGlzLmxpY2Vuc2VNb2RlbC5hcHBsaWVkID0gZGF0YS5zdWNjZXNzO1xyXG5cdFx0XHRcdFx0XHRpZihkYXRhLnN1Y2Nlc3MpIHtcclxuXHRcdFx0XHRcdFx0XHRcdHRoaXMubGljZW5zZU1vZGVsLmFjdGl2ZSA9IGRhdGEuc3VjY2VzcztcclxuXHRcdFx0XHRcdFx0XHRcdHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSh7IGlkOiB0aGlzLmxpY2Vuc2VNb2RlbC5pZCwgdXBkYXRlZDogdHJ1ZX0pO1xyXG5cdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0fSk7XHJcblx0XHR9XHJcblxyXG5cdFx0LyoqXHJcblx0XHQgKiBPcGVucyBhIGRpYWxvZyBhbmQgYWxsb3cgdGhlIHVzZXIgdG8gbWFudWFsbHkgc2VuZCB0aGUgcmVxdWVzdCBvciBjb3B5IHRoZSBlbmNyaXB0ZWQgY29kZVxyXG5cdFx0ICovXHJcblx0XHRtYW51YWxseVJlcXVlc3QoKSB7XHJcblx0XHRcdFx0dmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG5cdFx0XHRcdFx0XHRhbmltYXRpb246IHRydWUsXHJcblx0XHRcdFx0XHRcdHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL21hbnVhbGx5UmVxdWVzdC9NYW51YWxseVJlcXVlc3QuaHRtbCcsXHJcblx0XHRcdFx0XHRcdGNvbnRyb2xsZXI6ICdNYW51YWxseVJlcXVlc3QgYXMgbWFudWFsbHlSZXF1ZXN0JyxcclxuXHRcdFx0XHRcdFx0c2l6ZTogJ21kJyxcclxuXHRcdFx0XHRcdFx0cmVzb2x2ZToge1xyXG5cdFx0XHRcdFx0XHRcdFx0cGFyYW1zOiAoKSA9PiB7XHJcblx0XHRcdFx0XHRcdFx0XHRcdFx0cmV0dXJuIHsgbGljZW5zZTogdGhpcy5saWNlbnNlTW9kZWwgfTtcclxuXHRcdFx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdH0pO1xyXG5cclxuXHRcdFx0XHRtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHt9KTtcclxuXHRcdH1cclxuXHJcblx0XHQvKipcclxuXHRcdCAqIElmIGJ5IHNvbWUgcmVhc29uIHRoZSBMaWNlbnNlIHdhcyBub3QgYXBwbGllZCBhdCBmaXJzdCB0aW1lLCB0aGlzIHdpbGwgZG8gYSByZXF1ZXN0IGZvciBpdFxyXG5cdFx0ICovXHJcblx0XHRyZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KCkge1xyXG5cdFx0XHRcdHRoaXMubGljZW5zZUFkbWluU2VydmljZS5yZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge30pO1xyXG5cdFx0fVxyXG5cclxuXHRcdGRlbGV0ZUxpY2Vuc2UoKSB7XHJcblx0XHRcdFx0dmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG5cdFx0XHRcdFx0XHRhbmltYXRpb246IHRydWUsXHJcblx0XHRcdFx0XHRcdHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuXHRcdFx0XHRcdFx0Y29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG5cdFx0XHRcdFx0XHRzaXplOiAnc20nLFxyXG5cdFx0XHRcdFx0XHRyZXNvbHZlOiB7XHJcblx0XHRcdFx0XHRcdFx0XHRwYXJhbXM6ICgpID0+IHtcclxuXHRcdFx0XHRcdFx0XHRcdFx0XHRyZXR1cm4geyB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsIG1lc3NhZ2U6ICdBcmUgeW91IHN1cmUgeW91IHdhbnQgdG8gZGVsZXRlIGl0PyBUaGlzIGFjdGlvbiBjYW5ub3QgYmUgdW5kb25lLid9O1xyXG5cdFx0XHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0fSk7XHJcblxyXG5cdFx0XHRcdG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG5cdFx0XHRcdFx0XHR0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZGVsZXRlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuXHRcdFx0XHRcdFx0XHRcdHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuXHRcdFx0XHRcdFx0fSk7XHJcblx0XHRcdFx0fSk7XHJcblx0XHR9XHJcblxyXG5cdFx0LyoqXHJcblx0XHQgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuXHRcdCAqL1xyXG5cdFx0Y2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcblx0XHRcdFx0aWYodGhpcy5saWNlbnNlTW9kZWwuYXBwbGllZCkge1xyXG5cdFx0XHRcdFx0XHR0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoKTtcclxuXHRcdFx0XHR9XHJcblx0XHRcdFx0dGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG5cdFx0fVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VBZG1pbkxpc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZUFkbWluTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJsaWNlbnNlQWRtaW5MaXN0Lm9uUmVxdWVzdE5ld0xpY2Vuc2UoKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBSZXF1ZXN0IE5ldyBMaWNlbnNlPC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJsaWNlbnNlQWRtaW5MaXN0LnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAyMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdsaWNlbnNlSWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIGZpbHRlcmFibGU6IGZhbHNlLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJsaWNlbnNlQWRtaW5MaXN0Lm9uTGljZW5zZURldGFpbHModGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NsaWVudC5uYW1lJywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QubmFtZScsIHRpdGxlOiAnUHJvamVjdCcsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5wcm9qZWN0ICYmIGRhdGEucHJvamVjdC5uYW1lKT8gZGF0YS5wcm9qZWN0Lm5hbWUudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2VtYWlsJywgdGl0bGU6ICdDb250YWN0IEVtYWlsJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMnLCB0aXRsZTogJ1N0YXR1cycsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5zdGF0dXMpPyBkYXRhLnN0YXR1cy50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5uYW1lJywgdGl0bGU6ICdUeXBlJywgIHRlbXBsYXRlOiAnI2lmKGRhdGEudHlwZSAmJiBkYXRhLnR5cGUubmFtZSA9PT0gXCJNVUxUSV9QUk9KRUNUXCIpeyMgR2xvYmFsICMgfSBlbHNlIHsjIFNpbmdsZSAjfSMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5uYW1lJywgdGl0bGU6ICdNZXRob2QnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEubWV0aG9kICYmIGRhdGEubWV0aG9kLm5hbWUpPyBkYXRhLm1ldGhvZC5uYW1lLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubWF4JywgdGl0bGU6ICdTZXJ2ZXIvVG9rZW5zJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdyZXF1ZXN0RGF0ZScsIHRpdGxlOiAnSW5jZXB0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JywgdGVtcGxhdGU6ICd7eyBkYXRhSXRlbS5hY3RpdmF0aW9uRGF0ZSB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZXhwaXJhdGlvbkRhdGUnLCB0aXRsZTogJ0V4cGlyYXRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nLCB0ZW1wbGF0ZTogJ3t7IGRhdGFJdGVtLmV4cGlyYXRpb25EYXRlIHwgY29udmVydERhdGVJbnRvVGltZVpvbmUgfX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbnZpcm9ubWVudCcsIHRpdGxlOiAnRW52aXJvbm1lbnQnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEuZW52aXJvbm1lbnQpPyBkYXRhLmVudmlyb25tZW50LnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAncHJvamVjdC5uYW1lJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdExpY2Vuc2VJZCAhPT0gMCAmJiB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdmFyIGxhc3RMaWNlbnNlID0gdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhLmZpbmQoKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBsaWNlbnNlLmlkID09PSB0aGlzLm9wZW5MYXN0TGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobGFzdExpY2Vuc2UpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlRGV0YWlscyhsYXN0TGljZW5zZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0TGljZW5zZSBhcyByZXF1ZXN0TGljZW5zZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdOZXcgTGljZW5zZSBDcmVhdGVkOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICAgICAgdGhpcy5vbk5ld0xpY2Vuc2VDcmVhdGVkKGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZURldGFpbHMobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09wZW4gRGV0YWlscyBmb3I6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9kZXRhaWwvTGljZW5zZURldGFpbC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VEZXRhaWwgYXMgbGljZW5zZURldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcbiAgICAgICAgICAgIGlmKGRhdGEudXBkYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IGRhdGEuaWQ7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25OZXdMaWNlbnNlQ3JlYXRlZChsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmh0bWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQ3JlYXRlZExpY2Vuc2UgYXMgY3JlYXRlZExpY2Vuc2UnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBlbWFpbDogbGljZW5zZS5lbWFpbCAgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBNYW51YWxseVJlcXVlc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnNjb3BlID0gJHNjb3BlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlRW1haWxNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6ICBwYXJhbXMubGljZW5zZS5pZFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIC8vIEdldCB0aGUgaGFzaCBjb2RlIHVzaW5nIHRoZSBpZC5cclxuICAgICAgICB0aGlzLmdldEVtYWlsQ29udGVudCgpO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICBnZXRFbWFpbENvbnRlbnQoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldEVtYWlsQ29udGVudCh0aGlzLmxpY2Vuc2VFbWFpbE1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VFbWFpbE1vZGVsID0gZGF0YTtcclxuICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKiBDcmVhdGUgYSBuZXcgUmVxdWVzdCB0byBnZXQgYSBMaWNlbnNlXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RMaWNlbnNlIGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICAvKipcclxuICAgICAqIEluaXRpYWxpemUgYWxsIHRoZSBwcm9wZXJ0aWVzXHJcbiAgICAgKiBAcGFyYW0gJGxvZ1xyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VBZG1pblNlcnZpY2VcclxuICAgICAqIEBwYXJhbSAkdWliTW9kYWxJbnN0YW5jZVxyXG4gICAgICovXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgRW52aXJvbm1lbnQgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UgPSBbXTtcclxuICAgICAgICAvLyBEZWZpbmUgdGhlIFByb2plY3QgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSBbXTtcclxuXHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldFByb2plY3REYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIENyZWF0ZSB0aGUgTW9kZWwgZm9yIHRoZSBOZXcgTGljZW5zZVxyXG4gICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBlbWFpbDogJycsXHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiAnJyxcclxuICAgICAgICAgICAgcHJvamVjdElkOiAwLFxyXG4gICAgICAgICAgICBjbGllbnROYW1lOiAnJyxcclxuICAgICAgICAgICAgcmVxdWVzdE5vdGU6ICcnXHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpPT57XHJcbiAgICAgICAgICAgIHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlID0gZGF0YTtcclxuICAgICAgICAgICAgaWYodGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgICAgIHZhciBpbmRleCA9IHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlLmZpbmRJbmRleChmdW5jdGlvbihlbnZpcm9tZW50KXtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4gZW52aXJvbWVudCAgPT09ICdQUk9EVUNUSU9OJztcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgaW5kZXggPSBpbmRleCB8fCAwO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuZW52aXJvbm1lbnQgPSBkYXRhW2luZGV4XTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIFByb2plY3QgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLnByb2plY3RJZCA9IGRhdGFbMF0uaWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZSxcclxuICAgICAgICAgICAgc2VsZWN0OiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE9uIFByb2plY3QgQ2hhbmdlLCBzZWxlY3QgdGhlIENsaWVudCBOYW1lXHJcbiAgICAgICAgICAgICAgICB2YXIgaXRlbSA9IHRoaXMuc2VsZWN0UHJvamVjdC5kYXRhSXRlbShlLml0ZW0pO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuY2xpZW50TmFtZSA9IGl0ZW0uY2xpZW50Lm5hbWU7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBnZW5lcmF0ZSBhIG5ldyBMaWNlbnNlIHJlcXVlc3RcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2VSZXF1ZXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIFJlcXVlc3RlZDogJywgdGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QodGhpcy5uZXdMaWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UodGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZUFkbWluU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcbiAgICAgICAgdGhpcy5yb290U2NvcGUgPSAkcm9vdFNjb3BlO1xyXG4gICAgICAgIHRoaXMuc3RhdHVzU3VjY2VzcyA9ICdzdWNjZXNzJztcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnbGljZW5zZUFkbWluU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEVtYWlsQ29udGVudChsaWNlbnNlSWQsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRFbWFpbENvbnRlbnQobGljZW5zZUlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBvblN1Y2Nlc3Mpe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLnJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZS5pZCwgKGRhdGEpID0+IHtcclxuXHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdSZXF1ZXN0IExpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSd9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6IGRhdGEuZGF0YX0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2Vzcyh7IHN1Y2Nlc3M6IGZhbHNlfSk7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZW1haWxSZXF1ZXN0KGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmVtYWlsUmVxdWVzdChsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnUmVxdWVzdCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiAgQXBwbHkgVGhlIExpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gb25TdWNjZXNzXHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuXHJcbiAgICAgICAgdmFyIGhhc2ggPSAge1xyXG4gICAgICAgICAgICBoYXNoOiBsaWNlbnNlLmtleVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5hcHBseUxpY2Vuc2UobGljZW5zZS5pZCwgaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3IoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKHsgc3VjY2VzczogdHJ1ZX0pO1xyXG5cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5kZWxldGVMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RJbXBvcnQgZnJvbSAnLi9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbGljZW5zZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VNYW5hZ2VyTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0xpY2Vuc2luZyBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ01hbmFnZXInLCAnTGljZW5zZScsICdMaXN0J119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvbWFuYWdlci9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJMaXN0IGFzIGxpY2Vuc2VNYW5hZ2VyTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgJyRyb290U2NvcGUnLCBMaWNlbnNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlTWFuYWdlckxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlTWFuYWdlckxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignUmVxdWVzdEltcG9ydCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RJbXBvcnRdKTtcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZU1hbmFnZXJEZXRhaWwnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlTWFuYWdlckRldGFpbF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJEZXRhaWwgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMsIHRpbWVab25lQ29uZmlndXJhdGlvbikge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVzZXJQcmVmZXJlbmNlc1NlcnZpY2UgPSB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuXHJcbiAgICAgICAgdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24gPSB0aW1lWm9uZUNvbmZpZ3VyYXRpb247XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIG93bmVyTmFtZTogcGFyYW1zLmxpY2Vuc2Uub3duZXIubmFtZSxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UucHJvamVjdC5pZCxcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLnByb2plY3QubmFtZSxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY2xpZW50SWQ6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5pZCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcbiAgICAgICAgICAgIHN0YXR1czogcGFyYW1zLmxpY2Vuc2Uuc3RhdHVzLFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5uYW1lLFxyXG4gICAgICAgICAgICAgICAgbWF4OiBwYXJhbXMubGljZW5zZS5tZXRob2QubWF4LFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudDogcGFyYW1zLmxpY2Vuc2UuZW52aXJvbm1lbnQsXHJcbiAgICAgICAgICAgIHJlcXVlc3REYXRlOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0RGF0ZSxcclxuICAgICAgICAgICAgaW5pdERhdGU6IChwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSAhPT0gbnVsbCk/IGFuZ3VsYXIuY29weShwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSkgOiAnJyxcclxuICAgICAgICAgICAgZW5kRGF0ZTogKHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlICE9PSBudWxsKT8gYW5ndWxhci5jb3B5KHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlKSA6ICcnLFxyXG4gICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0Tm90ZSxcclxuICAgICAgICAgICAgd2Vic2l0ZU5hbWU6IHBhcmFtcy5saWNlbnNlLndlYnNpdGVuYW1lLFxyXG5cclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogcGFyYW1zLmxpY2Vuc2UuYmFubmVyTWVzc2FnZSxcclxuICAgICAgICAgICAgcmVxdWVzdGVkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3RlZElkLFxyXG4gICAgICAgICAgICByZXBsYWNlZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWQsXHJcbiAgICAgICAgICAgIHJlcGxhY2VkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkSWQsXHJcbiAgICAgICAgICAgIGhvc3ROYW1lOiBwYXJhbXMubGljZW5zZS5ob3N0TmFtZSxcclxuICAgICAgICAgICAgaGFzaDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIGdyYWNlUGVyaW9kRGF5czogcGFyYW1zLmxpY2Vuc2UuZ3JhY2VQZXJpb2REYXlzLFxyXG5cclxuICAgICAgICAgICAgYXBwbGllZDogcGFyYW1zLmxpY2Vuc2UuYXBwbGllZCxcclxuICAgICAgICAgICAga2V5SWQ6IHBhcmFtcy5saWNlbnNlLmtleUlkXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlS2V5ID0gJ0xpY2Vuc2VzIGhhcyBub3QgYmVlbiBpc3N1ZWQnO1xyXG5cclxuICAgICAgICAvLyBEZWZpbmVkIHRoZSBFbnZpcm9ubWVudCBTZWxlY3RcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RFbnZpcm9ubWVudExpc3RPcHRpb25zID0gW107XHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgU3RhdHVzIFNlbGVjdCBMaXN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RTdGF0dXMgPSBbXTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCB0aGUgdHdvIEtlbmRvIERhdGVzIGZvciBJbml0IGFuZCBFbmREYXRlXHJcbiAgICAgICAgdGhpcy5pbml0RGF0ZSA9IHt9O1xyXG4gICAgICAgIHRoaXMuaW5pdERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6IHRoaXMudXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlRm9ybWF0VG9LZW5kb0RhdGUoKSxcclxuICAgICAgICAgICAgb3BlbjogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgICAgIGNoYW5nZTogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmVuZERhdGUgPSB7fTtcclxuICAgICAgICB0aGlzLmVuZERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6IHRoaXMudXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlRm9ybWF0VG9LZW5kb0RhdGUoKSxcclxuICAgICAgICAgICAgb3BlbjogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlRW5kRGF0ZSgpO1xyXG4gICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgY2hhbmdlOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuXHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZU1ldGhvZE9wdGlvbnMoKTtcclxuICAgICAgICB0aGlzLnByZXBhcmVMaWNlbnNlS2V5KCk7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQWN0aXZpdHlMaXN0KCk7XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcblxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ29udHJvbHMgd2hhdCBidXR0b25zIHRvIHNob3dcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCkge1xyXG4gICAgICAgIHRoaXMucGVuZGluZ0xpY2Vuc2UgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXMgPT09ICdQRU5ESU5HJyAmJiAhdGhpcy5lZGl0TW9kZTtcclxuICAgICAgICB0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgPSAodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnRVhQSVJFRCcgfHwgdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnVEVSTUlOQVRFRCcpO1xyXG4gICAgICAgIHRoaXMuYWN0aXZlU2hvd01vZGUgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXMgPT09ICdBQ1RJVkUnICYmICF0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgJiYgIXRoaXMuZWRpdE1vZGU7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnTUFYX1NFUlZFUlMnLFxyXG4gICAgICAgICAgICAgICAgdGV4dDogJ1NlcnZlcnMnLFxyXG4gICAgICAgICAgICAgICAgbWF4OiAwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUT0tFTicsXHJcbiAgICAgICAgICAgICAgICB0ZXh0OiAnVG9rZW5zJyxcclxuICAgICAgICAgICAgICAgIG1heDogMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnQ1VTVE9NJyxcclxuICAgICAgICAgICAgICAgIHRleHQ6ICdDdXN0b20nXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdXHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUxpY2Vuc2VLZXkoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnQUNUSVZFJykge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRLZXlDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUtleSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVBY3Rpdml0eUxpc3QoKSB7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1LFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDIwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2RhdGVDcmVhdGVkJywgdGl0bGU6ICdEYXRlJywgd2lkdGg6MTYwLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eSBoOm1tOnNzIHR0fScsIHRlbXBsYXRlOiAne3sgZGF0YUl0ZW0uZGF0ZUNyZWF0ZWQgfCBjb252ZXJ0RGF0ZVRpbWVJbnRvVGltZVpvbmUgfX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhdXRob3IucGVyc29uTmFtZScsIHRpdGxlOiAnV2hvbScsICB3aWR0aDoxNjB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2hhbmdlcycsIHRpdGxlOiAnQWN0aW9uJywgdGVtcGxhdGU6ICc8dGFibGUgY2xhc3M9XCJpbm5lci1hY3Rpdml0eV90YWJsZVwiPjx0Ym9keT48dHI+PHRkPjwvdGQ+PHRkIGNsYXNzPVwiY29sLWFjdGlvbl90ZFwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1taW51c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L3RkPjx0ZCBjbGFzcz1cImNvbC1hY3Rpb25fdGRcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L3RkPjwvdHI+I2Zvcih2YXIgaSA9IDA7IGkgPCBkYXRhLmNoYW5nZXMubGVuZ3RoOyBpKyspeyM8dHI+PHRkIHN0eWxlPVwiZm9udC13ZWlnaHQ6IGJvbGQ7XCI+Iz1kYXRhLmNoYW5nZXNbaV0uZmllbGQjIDwvdGQ+PHRkIGNsYXNzPVwiY29sLXZhbHVlX3RkXCI+PHNwYW4gY2xhc3M9XCJhY3Rpdml0eS1saXN0LW9sZC12YWxcIiBzdHlsZT1cImNvbG9yOmRhcmtyZWQ7IGZvbnQtd2VpZ2h0OiBib2xkO1wiPnt7IFxcJyM9ZGF0YS5jaGFuZ2VzW2ldLm9sZFZhbHVlI1xcJyB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19PC9zcGFuPjwvdGQ+PHRkIGNsYXNzPVwiY29sLXZhbHVlX3RkXCI+PHNwYW4gY2xhc3M9XCJhY3Rpdml0eS1saXN0LW5ldy12YWxcIiBzdHlsZT1cImNvbG9yOiBncmVlbjsgZm9udC13ZWlnaHQ6IGJvbGQ7XCI+e3sgXFwnIz1kYXRhLmNoYW5nZXNbaV0ubmV3VmFsdWUjXFwnIHwgY29udmVydERhdGVJbnRvVGltZVpvbmUgfX08L3RkPjwvdHI+I30jPC90Ym9keT48L3RhYmxlPid9LFxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRBY3Rpdml0eUxvZyh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAnZGF0ZUNyZWF0ZWQnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc2Nyb2xsYWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgYWN0aXZhdGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmFjdGl2YXRlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYgKGRhdGEpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLnN0YXR1cyA9ICdBQ1RJVkUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5wcmVwYXJlTGljZW5zZUtleSgpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZWxvYWRSZXF1aXJlZCA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byByZXZva2UgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnJldm9rZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcbiAgICAgKi9cclxuICAgIG1hbnVhbGx5UmVxdWVzdCgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5tYW51YWxseVJlcXVlc3QodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWYWxpZGF0ZSB0aGUgaW5wdXQgb24gU2VydmVyIG9yIFRva2VucyBpcyBvbmx5IGludGVnZXIgb25seVxyXG4gICAgICogVGhpcyB3aWxsIGJlIGNvbnZlcnRlZCBpbiBhIG1vcmUgY29tcGxleCBkaXJlY3RpdmUgbGF0ZXJcclxuICAgICAqIFRPRE86IENvbnZlcnQgaW50byBhIGRpcmVjdGl2ZVxyXG4gICAgICovXHJcbiAgICB2YWxpZGF0ZUludGVnZXJPbmx5KGUsbW9kZWwpe1xyXG4gICAgICAgIHRyeSB7XHJcbiAgICAgICAgICAgIHZhciBuZXdWYWw9IHBhcnNlSW50KG1vZGVsKTtcclxuICAgICAgICAgICAgaWYoIWlzTmFOKG5ld1ZhbCkpIHtcclxuICAgICAgICAgICAgICAgIG1vZGVsID0gbmV3VmFsO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgbW9kZWwgPSAwO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIGlmKGUgJiYgZS5jdXJyZW50VGFyZ2V0KSB7XHJcbiAgICAgICAgICAgICAgICBlLmN1cnJlbnRUYXJnZXQudmFsdWUgPSBtb2RlbDtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICB0aGlzLiRsb2cud2FybignSW52YWxpZCBOdW1iZXIgRXhjZXB0aW9uJywgbW9kZWwpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgY3VycmVudCBjaGFuZ2VzXHJcbiAgICAgKi9cclxuICAgIHNhdmVMaWNlbnNlKCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2Uuc2F2ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlbG9hZFJlcXVpcmVkID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKTtcclxuICAgICAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ0xpY2Vuc2UgU2F2ZWQnKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpXHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2hhbmdlIHRoZSBzdGF0dXMgdG8gRWRpdFxyXG4gICAgICovXHJcbiAgICBtb2RpZnlMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSB0cnVlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0RW52aXJvbm1lbnRMaXN0T3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZighdGhpcy5saWNlbnNlTW9kZWwuZW52aXJvbm1lbnQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudCA9IGRhdGFbMF07XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgdmFsdWVUZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEpPyBkYXRhLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+JyxcclxuICAgICAgICAgICAgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhKT8gZGF0YS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPicsXHJcbiAgICAgICAgICAgIHZhbHVlUHJpbWl0aXZlOiB0cnVlXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBvbkNoYW5nZUluaXREYXRlKCkge1xyXG4gICAgICAgIHZhciBzdGFydERhdGUgPSB0aGlzLmluaXREYXRlLnZhbHVlKCksXHJcbiAgICAgICAgICAgIGVuZERhdGUgPSB0aGlzLmVuZERhdGUudmFsdWUoKTtcclxuXHJcbiAgICAgICAgaWYgKHN0YXJ0RGF0ZSkge1xyXG4gICAgICAgICAgICBzdGFydERhdGUgPSBuZXcgRGF0ZShzdGFydERhdGUpO1xyXG4gICAgICAgICAgICBzdGFydERhdGUuc2V0RGF0ZShzdGFydERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihzdGFydERhdGUpO1xyXG5cclxuICAgICAgICAgICAgaWYoZW5kRGF0ZSkge1xyXG4gICAgICAgICAgICAgICAgaWYodGhpcy5pbml0RGF0ZS52YWx1ZSgpID4gdGhpcy5lbmREYXRlLnZhbHVlKCkpIHtcclxuICAgICAgICAgICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgZW5kRGF0ZS5zZXREYXRlKHN0YXJ0RGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmVuZERhdGUgPSBlbmREYXRlO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIG9uQ2hhbmdlRW5kRGF0ZSgpe1xyXG4gICAgICAgIHZhciBlbmREYXRlID0gdGhpcy5lbmREYXRlLnZhbHVlKCksXHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZSA9IHRoaXMuaW5pdERhdGUudmFsdWUoKTtcclxuXHJcbiAgICAgICAgaWYgKGVuZERhdGUpIHtcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKGVuZERhdGUpO1xyXG4gICAgICAgICAgICBlbmREYXRlLnNldERhdGUoZW5kRGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgIH0gZWxzZSBpZiAoc3RhcnREYXRlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4obmV3IERhdGUoc3RhcnREYXRlKSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKCk7XHJcbiAgICAgICAgICAgIHRoaXMuaW5pdERhdGUubWF4KGVuZERhdGUpO1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKGVuZERhdGUpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICBpZih0aGlzLmVkaXRNb2RlKSB7XHJcbiAgICAgICAgICAgIHRoaXMucmVzZXRGb3JtKCgpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vblJlc2V0Rm9ybSgpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2UgaWYodGhpcy5yZWxvYWRSZXF1aXJlZCl7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSh7fSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERlcGVuZGluZyB0aGUgbnVtYmVyIG9mIGZpZWxkcyBhbmQgdHlwZSBvZiBmaWVsZCwgdGhlIHJlc2V0IGNhbid0IGJlIG9uIHRoZSBGb3JtVmFsaWRvciwgYXQgbGVhc3Qgbm90IG5vd1xyXG4gICAgICovXHJcbiAgICBvblJlc2V0Rm9ybSgpIHtcclxuICAgICAgICB0aGlzLnJlc2V0RHJvcERvd24odGhpcy5zZWxlY3RFbnZpcm9ubWVudCwgdGhpcy5saWNlbnNlTW9kZWwuZW52aXJvbm1lbnQpO1xyXG4gICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTWFudWFsIHJlbG9hZCBhZnRlciBhIGNoYW5nZSBoYXMgYmVlbiBwZXJmb3JtZWQgdG8gdGhlIExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgcmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMuYWN0aXZpdHlHcmlkLmRhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIC8vdGhpcy5nZXRMaWNlbnNlTGlzdCgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTWFuYWdlckxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gMDtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0Lm9uUmVxdWVzdEltcG9ydExpY2Vuc2UoKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBJbXBvcnQgTGljZW5zZSBSZXF1ZXN0PC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3QucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMjBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIGZpbHRlcmFibGU6IGZhbHNlLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3Qub25MaWNlbnNlTWFuYWdlckRldGFpbHModGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ293bmVyLm5hbWUnLCB0aXRsZTogJ093bmVyJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd3ZWJzaXRlbmFtZScsIHRpdGxlOiAnV2Vic2l0ZSBOYW1lJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjbGllbnQubmFtZScsIHRpdGxlOiAnQ2xpZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLCB0aXRsZTogJ1Byb2plY3QnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEucHJvamVjdCAmJiBkYXRhLnByb2plY3QubmFtZSk/IGRhdGEucHJvamVjdC5uYW1lLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbWFpbCcsIHRpdGxlOiAnQ29udGFjdCBFbWFpbCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3RhdHVzJywgdGl0bGU6ICdTdGF0dXMnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEuc3RhdHVzKT8gZGF0YS5zdGF0dXMudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZScsICB0ZW1wbGF0ZTogJyNpZihkYXRhLnR5cGUgJiYgZGF0YS50eXBlLm5hbWUgPT09IFwiTVVMVElfUFJPSkVDVFwiKXsjIEdsb2JhbCAjIH0gZWxzZSB7IyBTaW5nbGUgI30jJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubmFtZScsIHRpdGxlOiAnTWV0aG9kJywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLm1ldGhvZCAmJiBkYXRhLm1ldGhvZC5uYW1lKT8gZGF0YS5tZXRob2QubmFtZS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm1heCcsIHRpdGxlOiAnU2VydmVyL1Rva2Vucyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aXZhdGlvbkRhdGUnLCB0aXRsZTogJ0luY2VwdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScsIHRlbXBsYXRlOiAne3sgZGF0YUl0ZW0uYWN0aXZhdGlvbkRhdGUgfCBjb252ZXJ0RGF0ZUludG9UaW1lWm9uZSB9fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2V4cGlyYXRpb25EYXRlJywgdGl0bGU6ICdFeHBpcmF0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JywgdGVtcGxhdGU6ICd7eyBkYXRhSXRlbS5leHBpcmF0aW9uRGF0ZSB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW52aXJvbm1lbnQnLCB0aXRsZTogJ0Vudmlyb25tZW50JywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLmVudmlyb25tZW50KT8gZGF0YS5lbnZpcm9ubWVudC50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOidncmFjZVBlcmlvZERheXMnLCBoaWRkZW46IHRydWV9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBjaGFuZ2U6ICAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIC8vIFdlIGFyZSBjb21pbmcgZnJvbSBhIG5ldyBpbXBvcnRlZCByZXF1ZXN0IGxpY2Vuc2VcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgIT09IDAgJiYgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHZhciBuZXdMaWNlbnNlQ3JlYXRlZCA9IHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YS5maW5kKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gbGljZW5zZS5pZCA9PT0gdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IDA7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICBpZihuZXdMaWNlbnNlQ3JlYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyhuZXdMaWNlbnNlQ3JlYXRlZCk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGUgdXNlciBJbXBvcnQgYSBuZXcgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBvblJlcXVlc3RJbXBvcnRMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL3JlcXVlc3RJbXBvcnQvUmVxdWVzdEltcG9ydC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1JlcXVlc3RJbXBvcnQgYXMgcmVxdWVzdEltcG9ydCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IGxpY2Vuc2VJbXBvcnRlZC5pZDsgLy8gdGFrZSB0aGlzIHBhcmFtIGZyb20gdGhlIGxhc3QgaW1wb3J0ZWQgbGljZW5zZSwgb2YgY291cnNlXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKGxpY2Vuc2UpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPcGVuIERldGFpbHMgZm9yOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlTWFuYWdlckRldGFpbCBhcyBsaWNlbnNlTWFuYWdlckRldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgcmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SW1wb3J0IGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBoYXNoOiAnJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIG9uSW1wb3J0TGljZW5zZSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5pbXBvcnRMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UobGljZW5zZUltcG9ydGVkLmRhdGEpO1xyXG4gICAgICAgICAgICB9LCAobGljZW5zZUltcG9ydGVkKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShsaWNlbnNlSW1wb3J0ZWQuZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlciwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG4gICAgICAgIHRoaXMucm9vdFNjb3BlID0gJHJvb3RTY29wZTtcclxuICAgICAgICB0aGlzLnN0YXR1c1N1Y2Nlc3MgPSAnc3VjY2Vzcyc7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ2xpY2Vuc2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG5cclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0S2V5Q29kZShsaWNlbnNlSWQsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEtleUNvZGUobGljZW5zZUlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0VGltZVpvbmVDb25maWd1cmF0aW9uKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuY29tbW9uU2VydmljZUhhbmRsZXIoKS5nZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIG1hbnVhbGx5UmVxdWVzdChsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5tYW51YWxseVJlcXVlc3QobGljZW5zZS5pZCwgKGRhdGEpID0+IHtcclxuXHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdFbWFpbCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHknfSk7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ3dhcm5pbmcnLCB0ZXh0OiBkYXRhLmRhdGF9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZSB0aGUgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBzYXZlTGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuXHJcbiAgICAgICAgdmFyIGxpY2Vuc2VNb2RpZmllZCA9IHtcclxuICAgICAgICAgICAgZW52aXJvbm1lbnQ6IGxpY2Vuc2UuZW52aXJvbm1lbnQsXHJcbiAgICAgICAgICAgIG1ldGhvZDoge1xyXG4gICAgICAgICAgICAgICAgbmFtZTogbGljZW5zZS5tZXRob2QubmFtZVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBhY3RpdmF0aW9uRGF0ZTogbW9tZW50KGxpY2Vuc2UuaW5pdERhdGUpLmZvcm1hdCgnWVlZWS1NTS1ERCcpLFxyXG4gICAgICAgICAgICBleHBpcmF0aW9uRGF0ZTogbW9tZW50KGxpY2Vuc2UuZW5kRGF0ZSkuZm9ybWF0KCdZWVlZLU1NLUREJyksXHJcbiAgICAgICAgICAgIHN0YXR1czogbGljZW5zZS5zdGF0dXMsXHJcbiAgICAgICAgICAgIHByb2plY3Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiAobGljZW5zZS5wcm9qZWN0LmlkICE9PSAnYWxsJyk/IHBhcnNlSW50KGxpY2Vuc2UucHJvamVjdC5pZCkgOiBsaWNlbnNlLnByb2plY3QuaWQsICAvLyBXZSBwYXNzICdhbGwnIHdoZW4gaXMgbXVsdGlwcm9qZWN0XHJcbiAgICAgICAgICAgICAgICBuYW1lOiBsaWNlbnNlLnByb2plY3QubmFtZVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBiYW5uZXJNZXNzYWdlOiBsaWNlbnNlLmJhbm5lck1lc3NhZ2UsXHJcbiAgICAgICAgICAgIGdyYWNlUGVyaW9kRGF5czogbGljZW5zZS5ncmFjZVBlcmlvZERheXMsXHJcbiAgICAgICAgICAgIHdlYnNpdGVuYW1lOiBsaWNlbnNlLndlYnNpdGVOYW1lLFxyXG4gICAgICAgICAgICBob3N0TmFtZTogbGljZW5zZS5ob3N0TmFtZVxyXG4gICAgICAgIH07XHJcbiAgICAgICAgaWYobGljZW5zZS5tZXRob2QubmFtZSAhPT0gJ0NVU1RPTScpIHtcclxuICAgICAgICAgICAgbGljZW5zZU1vZGlmaWVkLm1ldGhvZC5tYXggPSBwYXJzZUludChsaWNlbnNlLm1ldGhvZC5tYXgpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuc2F2ZUxpY2Vuc2UobGljZW5zZS5pZCwgbGljZW5zZU1vZGlmaWVkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG4gICAgLyoqXHJcbiAgICAgKiBEb2VzIHRoZSBhY3RpdmF0aW9uIG9mIHRoZSBjdXJyZW50IGxpY2Vuc2UgaWYgdGhpcyBpcyBub3QgYWN0aXZlXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGFjdGl2YXRlTGljZW5zZShsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmFjdGl2YXRlTGljZW5zZShsaWNlbnNlLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICBpZihkYXRhLnN0YXR1cyA9PT0gdGhpcy5zdGF0dXNTdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHtcclxuICAgICAgICAgICAgICAgICAgICB0eXBlOiAnaW5mbycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogJ1RoZSBsaWNlbnNlIHdhcyBhY3RpdmF0ZWQgYW5kIHRoZSBsaWNlbnNlIHdhcyBlbWFpbGVkLidcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ3dhcm5pbmcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6IGRhdGEuZGF0YVxyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTWFrZSB0aGUgcmVxdWVzdCB0byBJbXBvcnQgdGhlIGxpY2Vuc2UsIGlmIGZhaWxzLCB0aHJvd3MgYW4gZXhjZXB0aW9uIHZpc2libGUgZm9yIHRoZSB1c2VyIHRvIHRha2UgYWN0aW9uXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGltcG9ydExpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcbiAgICAgICAgdmFyIGhhc2ggPSB7XHJcbiAgICAgICAgICAgIGRhdGE6IGxpY2Vuc2UuaGFzaFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnJlcXVlc3RJbXBvcnQoaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBJbXBvcnRlZCd9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6ICdMaWNlbnNlIHdhcyBub3QgYXBwbGllZC4gUmV2aWV3IHRoZSBwcm92aWRlZCBMaWNlbnNlIEtleSBpcyBjb3JyZWN0Lid9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHsgc3VjY2VzczogZmFsc2V9KTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJldm9rZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkucmV2b2tlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEFjdGl2aXR5TG9nKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEFjdGl2aXR5TG9nKGxpY2Vuc2UuaWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBjYWxsYmFjayl7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBOb3RpY2VMaXN0IGZyb20gJy4vbGlzdC9Ob3RpY2VMaXN0LmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9Ob3RpY2VNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBFZGl0Tm90aWNlIGZyb20gJy4vZWRpdC9FZGl0Tm90aWNlLmpzJztcclxuXHJcbnZhciBOb3RpY2VNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLk5vdGljZU1hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ25vdGljZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ25vdGljZUxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdOb3RpY2UgQWRtaW5pc3RyYXRpb24nLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnQWRtaW4nLCAnTm90aWNlJywgJ0xpc3QnXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvbm90aWNlL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvbGlzdC9Ob3RpY2VMaXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdOb3RpY2VMaXN0IGFzIG5vdGljZUxpc3QnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdOb3RpY2VNYW5hZ2VyU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCBOb3RpY2VNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdOb3RpY2VMaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdOb3RpY2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBOb3RpY2VMaXN0XSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignRWRpdE5vdGljZScsIFsnJGxvZycsICdOb3RpY2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRWRpdE5vdGljZV0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgTm90aWNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRWRpdE5vdGljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbm90aWNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy5hY3Rpb24gPSBwYXJhbXMuYWN0aW9uO1xyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHBhcmFtcy5hY3Rpb25UeXBlO1xyXG5cclxuICAgICAgICB0aGlzLmtlbmRvRWRpdG9yVG9vbHMgPSBbXHJcbiAgICAgICAgICAgICdmb3JtYXR0aW5nJywgJ2NsZWFuRm9ybWF0dGluZycsXHJcbiAgICAgICAgICAgICdmb250TmFtZScsICdmb250U2l6ZScsXHJcbiAgICAgICAgICAgICdqdXN0aWZ5TGVmdCcsICdqdXN0aWZ5Q2VudGVyJywgJ2p1c3RpZnlSaWdodCcsICdqdXN0aWZ5RnVsbCcsXHJcbiAgICAgICAgICAgICdib2xkJyxcclxuICAgICAgICAgICAgJ2l0YWxpYycsXHJcbiAgICAgICAgICAgICd2aWV3SHRtbCdcclxuICAgICAgICBdO1xyXG5cclxuICAgICAgICAvLyBDU1MgaGFzIG5vdCBjYW5jZWxpbmcgYXR0cmlidXRlcywgc28gaW5zdGVhZCBvZiByZW1vdmluZyBldmVyeSBwb3NzaWJsZSBIVE1MLCB3ZSBtYWtlIGVkaXRvciBoYXMgc2FtZSBjc3NcclxuICAgICAgICB0aGlzLmtlbmRvU3R5bGVzaGVldHMgPSBbXHJcbiAgICAgICAgICAgICcuLi9zdGF0aWMvZGlzdC9qcy92ZW5kb3JzL2Jvb3RzdHJhcC9kaXN0L2Nzcy9ib290c3RyYXAubWluLmNzcycsIC8vIE91cnQgY3VycmVudCBCb290c3RyYXAgY3NzXHJcbiAgICAgICAgICAgICcuLi9zdGF0aWMvZGlzdC9jc3MvVERTVE1MYXlvdXQubWluLmNzcycgLy8gT3JpZ2luYWwgVGVtcGxhdGUgQ1NTXHJcblxyXG4gICAgICAgIF07XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0VHlwZURhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbCA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICB0eXBlSWQ6IDAsXHJcbiAgICAgICAgICAgIGFjdGl2ZTogZmFsc2UsXHJcbiAgICAgICAgICAgIGh0bWxUZXh0OiAnJyxcclxuICAgICAgICAgICAgcmF3VGV4dDogJydcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIC8vIE9uIEVkaXRpb24gTW9kZSB3ZSBjYyB0aGUgbW9kZWwgYW5kIG9ubHkgdGhlIHBhcmFtcyB3ZSBuZWVkXHJcbiAgICAgICAgaWYocGFyYW1zLm5vdGljZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5pZCA9IHBhcmFtcy5ub3RpY2UuaWQ7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLnRpdGxlID0gcGFyYW1zLm5vdGljZS50aXRsZTtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwudHlwZUlkID0gcGFyYW1zLm5vdGljZS50eXBlLmlkO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5hY3RpdmUgPSBwYXJhbXMubm90aWNlLmFjdGl2ZTtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuaHRtbFRleHQgPSBwYXJhbXMubm90aWNlLmh0bWxUZXh0O1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0VHlwZURhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy50eXBlRGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge3R5cGVJZDogMSwgbmFtZTogJ1ByZWxvZ2luJ30sXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDIsIG5hbWU6ICdQb3N0bG9naW4nfVxyXG4gICAgICAgICAgICAvL3t0eXBlSWQ6IDMsIG5hbWU6ICdHZW5lcmFsJ30gRGlzYWJsZWQgdW50aWwgUGhhc2UgSUlcclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSB0aGUgU2VydmljZSBjYWxsIHRvIENyZWF0ZS9FZGl0IGEgbm90aWNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVOb3RpY2UoKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbyh0aGlzLmFjdGlvbiArICcgTm90aWNlIFJlcXVlc3RlZDogJywgdGhpcy5lZGl0TW9kZWwpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsLnJhd1RleHQgPSAkKCcja2VuZG8tZWRpdG9yLWNyZWF0ZS1lZGl0JykudGV4dCgpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsLnR5cGVJZCA9IHBhcnNlSW50KHRoaXMuZWRpdE1vZGVsLnR5cGVJZCk7XHJcbiAgICAgICAgaWYodGhpcy5hY3Rpb24gPT09IHRoaXMuYWN0aW9uVHlwZS5ORVcpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5jcmVhdGVOb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSBpZih0aGlzLmFjdGlvbiA9PT0gdGhpcy5hY3Rpb25UeXBlLkVESVQpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5lZGl0Tm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTm90aWNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIGRlbGV0ZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5kZWxldGVOb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBOb3RpY2VMaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIG5vdGljZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuXHJcbiAgICAgICAgdGhpcy5hY3Rpb25UeXBlID0ge1xyXG4gICAgICAgICAgICBORVc6ICdOZXcnLFxyXG4gICAgICAgICAgICBFRElUOiAnRWRpdCdcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLm5vdGljZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLm5vdGljZUdyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZSA9IG5vdGljZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cIm5vdGljZUxpc3Qub25FZGl0Q3JlYXRlTm90aWNlKG5vdGljZUxpc3QuYWN0aW9uVHlwZS5ORVcpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IENyZWF0ZSBOZXcgTm90aWNlPC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJub3RpY2VMaXN0LnJlbG9hZE5vdGljZUxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdpZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaHRtbFRleHQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cIm5vdGljZUxpc3Qub25FZGl0Q3JlYXRlTm90aWNlKG5vdGljZUxpc3QuYWN0aW9uVHlwZS5FRElULCB0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGl0bGUnLCB0aXRsZTogJ1RpdGxlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aXZlJywgdGl0bGU6ICdBY3RpdmUnLCB0ZW1wbGF0ZTogJyNpZihhY3RpdmUpIHsjIFllcyAjfSBlbHNlIHsjIE5vICN9IycgfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmdldE5vdGljZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHNvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICBmaWVsZDogJ3RpdGxlJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE9wZW4gYSBkaWFsb2cgd2l0aCB0aGUgQmFzaWMgRm9ybSB0byByZXF1ZXN0IGEgTmV3IE5vdGljZVxyXG4gICAgICovXHJcbiAgICBvbkVkaXRDcmVhdGVOb3RpY2UoYWN0aW9uLCBub3RpY2UpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2VkaXQvRWRpdE5vdGljZS5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0VkaXROb3RpY2UgYXMgZWRpdE5vdGljZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IG5vdGljZSAmJiBub3RpY2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgYWN0aW9uOiBhY3Rpb24sIG5vdGljZTogZGF0YUl0ZW0sIGFjdGlvblR5cGU6IHRoaXMuYWN0aW9uVHlwZX07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobm90aWNlKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oYWN0aW9uICsgJyBOb3RpY2U6ICcsIG5vdGljZSk7XHJcbiAgICAgICAgICAgIC8vIEFmdGVyIGEgbmV3IHZhbHVlIGlzIGFkZGVkLCBsZXRzIHRvIHJlZnJlc2ggdGhlIEdyaWRcclxuICAgICAgICAgICAgdGhpcy5yZWxvYWROb3RpY2VMaXN0KCk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZWxvYWROb3RpY2VMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubm90aWNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlR3JpZC5kYXRhU291cmNlLnJlYWQoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBOb3RpY2VNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcblxyXG4gICAgICAgIHRoaXMuVFlQRSA9IHtcclxuICAgICAgICAgICAgJzEnOiAnUHJlbG9naW4nLFxyXG4gICAgICAgICAgICAnMic6ICdQb3N0bG9naW4nLFxyXG4gICAgICAgICAgICAnMyc6ICdHZW5lcmFsJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdOb3RpY2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXROb3RpY2VMaXN0KGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHZhciBub3RpY2VMaXN0ID0gW107XHJcbiAgICAgICAgICAgIHRyeSB7XHJcbiAgICAgICAgICAgICAgICAvLyBWZXJpZnkgdGhlIExpc3QgcmV0dXJucyB3aGF0IHdlIGV4cGVjdCBhbmQgd2UgY29udmVydCBpdCB0byBhbiBBcnJheSB2YWx1ZVxyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSAmJiBkYXRhLm5vdGljZXMpIHtcclxuICAgICAgICAgICAgICAgICAgICBub3RpY2VMaXN0ID0gZGF0YS5ub3RpY2VzO1xyXG4gICAgICAgICAgICAgICAgICAgIGlmIChub3RpY2VMaXN0ICYmIG5vdGljZUxpc3QubGVuZ3RoID4gMCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IG5vdGljZUxpc3QubGVuZ3RoOyBpID0gaSArIDEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5vdGljZUxpc3RbaV0udHlwZSA9IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogbm90aWNlTGlzdFtpXS50eXBlSWQsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogdGhpcy5UWVBFW25vdGljZUxpc3RbaV0udHlwZUlkXVxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgfTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGRlbGV0ZSBub3RpY2VMaXN0W2ldLnR5cGVJZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSBjYXRjaChlKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxvZy5lcnJvcignRXJyb3IgcGFyc2luZyB0aGUgTm90aWNlIExpc3QnLCBlKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2sobm90aWNlTGlzdCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTm90aWNlIHBhc3NpbmcgcGFyYW1zXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTm90aWNlKG5vdGljZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTm90aWNlIHNob3VsZCBoYXZlIHRoZSBJRCBpbiBvcmRlciB0byBlZGl0IHRoZSBOb3RpY2VcclxuICAgICAqIEBwYXJhbSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBlZGl0Tm90aWNlKG5vdGljZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZWRpdE5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE5vdGljZSBzaG91bGQgaGF2ZSB0aGUgSUQgaW4gb3JkZXIgdG8gZGVsZXRlIHRoZSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBkZWxldGVOb3RpY2Uobm90aWNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZGVsZXRlTm90aWNlKG5vdGljZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgVGFza01hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9UYXNrTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJDb250cm9sbGVyIGZyb20gJy4vbGlzdC9UYXNrTWFuYWdlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJFZGl0IGZyb20gJy4vZWRpdC9UYXNrTWFuYWdlckVkaXQuanMnO1xyXG5cclxudmFyIFRhc2tNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLlRhc2tNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAnZm9ybWx5Q29uZmlnUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCBmb3JtbHlDb25maWdQcm92aWRlcikge1xyXG5cclxuICAgIGZvcm1seUNvbmZpZ1Byb3ZpZGVyLnNldFR5cGUoe1xyXG4gICAgICAgIG5hbWU6ICdjdXN0b20nLFxyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnY3VzdG9tLmh0bWwnXHJcbiAgICB9KTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ3Rhc2tMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTXkgVGFzayBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ1Rhc2sgTWFuYWdlciddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy90YXNrL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2xpc3QvVGFza01hbmFnZXJDb250YWluZXIuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyQ29udHJvbGxlciBhcyB0YXNrTWFuYWdlcidcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCd0YXNrTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgVGFza01hbmFnZXJTZXJ2aWNlXSk7XHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdUYXNrTWFuYWdlckNvbnRyb2xsZXInLCBbJyRsb2cnLCAndGFza01hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsIFRhc2tNYW5hZ2VyQ29udHJvbGxlcl0pO1xyXG5UYXNrTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdUYXNrTWFuYWdlckVkaXQnLCBbJyRsb2cnLCBUYXNrTWFuYWdlckVkaXRdKTtcclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBUYXNrTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMvMTEvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlckVkaXQge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHRhc2tNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcblxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjAvMjAxNS5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyQ29udHJvbGxlciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgdGFza01hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLm1vZHVsZSA9ICdUYXNrTWFuYWdlcic7XHJcbiAgICAgICAgdGhpcy50YXNrTWFuYWdlclNlcnZpY2UgPSB0YXNrTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy50YXNrR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmV2ZW50RGF0YVNvdXJjZSA9IFtdO1xyXG5cclxuICAgICAgICAvLyBJbml0IENsYXNzXHJcbiAgICAgICAgdGhpcy5nZXRFdmVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVGFza01hbmFnZXIgQ29udHJvbGxlciBJbnN0YW5jZWQnKTtcclxuICAgICAgICB0aGlzLmluaXRGb3JtKCk7XHJcblxyXG4gICAgfVxyXG5cclxuICAgIG9wZW5Nb2RhbERlbW8oKSB7XHJcblxyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJ2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2VkaXQvVGFza01hbmFnZXJFZGl0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnVGFza01hbmFnZXJFZGl0JyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgaXRlbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4gWycxJywnYTInLCdnZyddO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKHNlbGVjdGVkSXRlbSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmRlYnVnKHNlbGVjdGVkSXRlbSk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdNb2RhbCBkaXNtaXNzZWQgYXQ6ICcgKyBuZXcgRGF0ZSgpKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMudGFza0dyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBncm91cGFibGU6IHRydWUsXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFt7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGFzaycsIHRpdGxlOiAnVGFzayd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZGVzY3JpcHRpb24nLCB0aXRsZTogJ0Rlc2NyaXB0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldE5hbWUnLCB0aXRsZTogJ0Fzc2V0IE5hbWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Fzc2V0VHlwZScsIHRpdGxlOiAnQXNzZXQgVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndXBkYXRlZCcsIHRpdGxlOiAnVXBkYXRlZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZHVlJywgdGl0bGU6ICdEdWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NpZ25lZFRvJywgdGl0bGU6ICdBc3NpZ25lZCBUbyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGVhbScsIHRpdGxlOiAnVGVhbSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2F0ZWdvcnknLCB0aXRsZTogJ0NhdGVnb3J5J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdWMnLCB0aXRsZTogJ1N1Yy4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Njb3JlJywgdGl0bGU6ICdTY29yZSd9XSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgLyp0aGlzLnRhc2tNYW5hZ2VyU2VydmljZS50ZXN0U2VydmljZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTsqL1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RXZlbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuZXZlbnREYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMSwgZXZlbnROYW1lOiAnQWxsJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiAyLCBldmVudE5hbWU6ICdCdWlsZG91dCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMywgZXZlbnROYW1lOiAnRFItRVAnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDQsIGV2ZW50TmFtZTogJ00xLVBoeXNpY2FsJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIG9uRXJyb3JIYXBwZW5zKCkge1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlLmZhaWxDYWxsKGZ1bmN0aW9uICgpIHtcclxuXHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgaW5pdEZvcm0oKSB7XHJcbiAgICAgICAgdGhpcy51c2VyRmllbGRzID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdlbWFpbCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnaW5wdXQnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ2VtYWlsJyxcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ0VtYWlsIGFkZHJlc3MnLFxyXG4gICAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyOiAnRW50ZXIgZW1haWwnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGtleTogJ3Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdpbnB1dCcsXHJcbiAgICAgICAgICAgICAgICB0ZW1wbGF0ZU9wdGlvbnM6IHtcclxuICAgICAgICAgICAgICAgICAgICB0eXBlOiAncGFzc3dvcmQnLFxyXG4gICAgICAgICAgICAgICAgICAgIGxhYmVsOiAnUGFzc3dvcmQnLFxyXG4gICAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyOiAnUGFzc3dvcmQnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGtleTogJ2ZpbGUnLFxyXG4gICAgICAgICAgICAgICAgdHlwZTogJ2ZpbGUnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdGaWxlIGlucHV0JyxcclxuICAgICAgICAgICAgICAgICAgICBkZXNjcmlwdGlvbjogJ0V4YW1wbGUgYmxvY2stbGV2ZWwgaGVscCB0ZXh0IGhlcmUnLFxyXG4gICAgICAgICAgICAgICAgICAgIHVybDogJ2h0dHBzOi8vZXhhbXBsZS5jb20vdXBsb2FkJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdjaGVja2VkJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdjaGVja2JveCcsXHJcbiAgICAgICAgICAgICAgICB0ZW1wbGF0ZU9wdGlvbnM6IHtcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ0NoZWNrIG1lIG91dCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDIyLzA3LzE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgUmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSBSZXN0U2VydmljZUhhbmRsZXI7XHJcblxyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdUYXNrTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZmFpbENhbGwoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlJlc291cmNlU2VydmljZUhhbmRsZXIoKS5nZXRTVkcoKTtcclxuICAgIH1cclxuXHJcbiAgICB0ZXN0U2VydmljZShjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuVGFza1NlcnZpY2VIYW5kbGVyKCkuZ2V0RmVlZHMoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzMvMjAxNi5cclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnXHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBGb3JtVmFsaWRhdG9yIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuXHJcbiAgICAgICAgLy8gSlMgZG9lcyBhIGFyZ3VtZW50IHBhc3MgYnkgcmVmZXJlbmNlXHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gbnVsbDtcclxuICAgICAgICAvLyBBIGNvcHkgd2l0aG91dCByZWZlcmVuY2UgZnJvbSB0aGUgb3JpZ2luYWwgb2JqZWN0XHJcbiAgICAgICAgdGhpcy5vcmlnaW5hbERhdGEgPSBudWxsO1xyXG4gICAgICAgIC8vIEEgQ0MgYXMgSlNPTiBmb3IgY29tcGFyaXNvbiBQdXJwb3NlXHJcbiAgICAgICAgdGhpcy5vYmplY3RBc0pTT04gPSBudWxsO1xyXG5cclxuXHJcbiAgICAgICAgLy8gT25seSBmb3IgTW9kYWwgV2luZG93c1xyXG4gICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSBmYWxzZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cclxuICAgICAgICBpZiAoJHNjb3BlLiRvbikge1xyXG4gICAgICAgICAgICAkc2NvcGUuJG9uKCdtb2RhbC5jbG9zaW5nJywgKGV2ZW50LCByZWFzb24sIGNsb3NlZCk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2xvc2VEaWFsb2coZXZlbnQsIHJlYXNvbiwgY2xvc2VkKVxyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBTYXZlcyB0aGUgRm9ybSBpbiAzIGluc3RhbmNlcywgb25lIHRvIGtlZXAgdHJhY2sgb2YgdGhlIG9yaWdpbmFsIGRhdGEsIG90aGVyIGlzIHRoZSBjdXJyZW50IG9iamVjdCBhbmRcclxuICAgICAqIGEgSlNPTiBmb3JtYXQgZm9yIGNvbXBhcmlzb24gcHVycG9zZVxyXG4gICAgICogQHBhcmFtIG5ld09iamVjdEluc3RhbmNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVGb3JtKG5ld09iamVjdEluc3RhbmNlKSB7XHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gbmV3T2JqZWN0SW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5vcmlnaW5hbERhdGEgPSBhbmd1bGFyLmNvcHkobmV3T2JqZWN0SW5zdGFuY2UsIHRoaXMub3JpZ2luYWxEYXRhKTtcclxuICAgICAgICB0aGlzLm9iamVjdEFzSlNPTiA9IGFuZ3VsYXIudG9Kc29uKG5ld09iamVjdEluc3RhbmNlKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEdldCB0aGUgQ3VycmVudCBPYmplY3Qgb24gaGlzIHJlZmVyZW5jZSBGb3JtYXRcclxuICAgICAqIEByZXR1cm5zIHtudWxsfCp9XHJcbiAgICAgKi9cclxuICAgIGdldEZvcm0oKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuY3VycmVudE9iamVjdDtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEdldCB0aGUgT2JqZWN0IGFzIEpTT04gZnJvbSB0aGUgT3JpZ2luYWwgRGF0YVxyXG4gICAgICogQHJldHVybnMge251bGx8c3RyaW5nfHVuZGVmaW5lZHxzdHJpbmd8Kn1cclxuICAgICAqL1xyXG4gICAgZ2V0Rm9ybUFzSlNPTigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5vYmplY3RBc0pTT047XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKlxyXG4gICAgICogQHBhcmFtIG9iamV0VG9SZXNldCBvYmplY3QgdG8gcmVzZXRcclxuICAgICAqIEBwYXJhbSBvblJlc2V0Rm9ybSBjYWxsYmFja1xyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIHJlc2V0Rm9ybShvblJlc2V0Rm9ybSkge1xyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IGFuZ3VsYXIuY29weSh0aGlzLm9yaWdpbmFsRGF0YSwgdGhpcy5jdXJyZW50T2JqZWN0KTtcclxuICAgICAgICB0aGlzLnNhZmVBcHBseSgpO1xyXG5cclxuICAgICAgICBpZihvblJlc2V0Rm9ybSkge1xyXG4gICAgICAgICAgICByZXR1cm4gb25SZXNldEZvcm0oKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWYWxpZGF0ZXMgaWYgdGhlIGN1cnJlbnQgb2JqZWN0IGRpZmZlcnMgZnJvbSB3aGVyZSBpdCB3YXMgb3JpZ2luYWxseSBzYXZlZFxyXG4gICAgICogQHJldHVybnMge2Jvb2xlYW59XHJcbiAgICAgKi9cclxuICAgIGlzRGlydHkoKSB7XHJcbiAgICAgICAgdmFyIG5ld09iamVjdEluc3RhbmNlID0gYW5ndWxhci50b0pzb24odGhpcy5jdXJyZW50T2JqZWN0KTtcclxuICAgICAgICByZXR1cm4gbmV3T2JqZWN0SW5zdGFuY2UgIT09IHRoaXMuZ2V0Rm9ybUFzSlNPTigpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhpcyBmdW5jdGlvbiBpcyBvbmx5IGF2YWlsYWJsZSB3aGVuIHRoZSBGb3JtIGlzIGJlaW5nIGNhbGxlZCBmcm9tIGEgRGlhbG9nIFBvcFVwXHJcbiAgICAgKi9cclxuICAgIG9uQ2xvc2VEaWFsb2coZXZlbnQsIHJlYXNvbiwgY2xvc2VkKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnbW9kYWwuY2xvc2luZzogJyArIChjbG9zZWQgPyAnY2xvc2UnIDogJ2Rpc21pc3MnKSArICcoJyArIHJlYXNvbiArICcpJyk7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNEaXJ0eSgpICYmIHJlYXNvbiAhPT0gJ2NhbmNlbC1jb25maXJtYXRpb24nICYmIHR5cGVvZiByZWFzb24gIT09ICdvYmplY3QnKSB7XHJcbiAgICAgICAgICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XHJcbiAgICAgICAgICAgIHRoaXMuY29uZmlybUNsb3NlRm9ybSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEEgQ29uZmlybWF0aW9uIERpYWxvZyB3aGVuIHRoZSBpbmZvcm1hdGlvbiBjYW4gYmUgbG9zdFxyXG4gICAgICogQHBhcmFtIGV2ZW50XHJcbiAgICAgKi9cclxuICAgIGNvbmZpcm1DbG9zZUZvcm0oZXZlbnQpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgbWVzc2FnZTogJ0NoYW5nZXMgeW91IG1hZGUgbWF5IG5vdCBiZSBzYXZlZC4gRG8geW91IHdhbnQgdG8gY29udGludWU/J1xyXG4gICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwtY29uZmlybWF0aW9uJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBVdGlsIHRvIGNhbGwgc2FmZSBpZiByZXF1aXJlZFxyXG4gICAgICogQHBhcmFtIGZuXHJcbiAgICAgKi9cclxuICAgIHNhZmVBcHBseShmbikge1xyXG4gICAgICAgIHZhciBwaGFzZSA9IHRoaXMuc2NvcGUuJHJvb3QuJCRwaGFzZTtcclxuICAgICAgICBpZihwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgICAgICBpZihmbiAmJiAodHlwZW9mKGZuKSA9PT0gJ2Z1bmN0aW9uJykpIHtcclxuICAgICAgICAgICAgICAgIGZuKCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICB0aGlzLnNjb3BlLiRhcHBseShmbik7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVXRpbCB0byBSZXNldCBhIERyb3Bkb3duIGxpc3Qgb24gS2VuZG9cclxuICAgICAqL1xyXG5cclxuICAgIHJlc2V0RHJvcERvd24oc2VsZWN0b3JJbnN0YW5jZSwgc2VsZWN0ZWRJZCwgZm9yY2UpIHtcclxuICAgICAgICBpZihzZWxlY3Rvckluc3RhbmNlICYmIHNlbGVjdG9ySW5zdGFuY2UuZGF0YUl0ZW1zKSB7XHJcbiAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2UuZGF0YUl0ZW1zKCkuZm9yRWFjaCgodmFsdWUsIGluZGV4KSA9PiB7XHJcbiAgICAgICAgICAgICAgICBpZihzZWxlY3RlZElkID09PSB2YWx1ZS5pZCB8fCBzZWxlY3RlZElkID09PSB2YWx1ZSkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2Uuc2VsZWN0KGluZGV4KTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBpZihmb3JjZSkge1xyXG4gICAgICAgICAgICAgICAgc2VsZWN0b3JJbnN0YW5jZS50cmlnZ2VyKCdjaGFuZ2UnKTtcclxuICAgICAgICAgICAgICAgIHRoaXMuc2FmZUFwcGx5KCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIzLzIwMTUuXHJcbiAqIEltcGxlbWVudHMgUlggT2JzZXJ2YWJsZSB0byBkaXNwb3NlIGFuZCB0cmFjayBiZXR0ZXIgZWFjaCBjYWxsIHRvIHRoZSBzZXJ2ZXJcclxuICogVGhlIE9ic2VydmVyIHN1YnNjcmliZSBhIHByb21pc2UuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RIYW5kbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKHJ4KSB7XHJcbiAgICAgICAgdGhpcy5yeCA9IHJ4O1xyXG4gICAgICAgIHRoaXMucHJvbWlzZSA9IFtdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2FsbGVkIGZyb20gUmVzdFNlcnZpY2VIYW5kbGVyLnN1YnNjcmliZVJlcXVlc3RcclxuICAgICAqIGl0IHZlcmlmeSB0aGF0IHRoZSBjYWxsIGlzIGJlaW5nIGRvbmUgdG8gdGhlIHNlcnZlciBhbmQgcmV0dXJuIGEgcHJvbWlzZVxyXG4gICAgICogQHBhcmFtIHJlcXVlc3RcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICBzdWJzY3JpYmVSZXF1ZXN0KHJlcXVlc3QsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHZhciByeE9ic2VydmFibGUgPSB0aGlzLnJ4Lk9ic2VydmFibGUuZnJvbVByb21pc2UocmVxdWVzdCk7XHJcbiAgICAgICAgLy8gVmVyaWZ5IGlzIG5vdCBhIGR1cGxpY2F0ZSBjYWxsXHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgdGhpcy5jYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBTdWJzY3JpYmUgdGhlIHJlcXVlc3RcclxuICAgICAgICB2YXIgcmVzdWx0U3Vic2NyaWJlID0gdGhpcy5hZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgIGlmIChyZXN1bHRTdWJzY3JpYmUgJiYgcmVzdWx0U3Vic2NyaWJlLmlzU3RvcHBlZCkge1xyXG4gICAgICAgICAgICAvLyBBbiBlcnJvciBoYXBwZW5zLCB0cmFja2VkIGJ5IEh0dHBJbnRlcmNlcHRvckludGVyZmFjZVxyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGFkZFN1YnNjcmliZShyeE9ic2VydmFibGUsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdID0gcnhPYnNlcnZhYmxlLnN1YnNjcmliZShcclxuICAgICAgICAgICAgKHJlc3BvbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5vblN1YnNjcmliZWRTdWNjZXNzKHJlc3BvbnNlLCByeE9ic2VydmFibGUsIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIChlcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE5PLU9QIFN1YnNjcmliZSBjb21wbGV0ZWRcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgIH1cclxuXHJcbiAgICBjYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICAgICAgcnhPYnNlcnZhYmxlLmRpc3Bvc2UoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIHJldHVybiAocnhPYnNlcnZhYmxlICYmIHJ4T2JzZXJ2YWJsZS5fcCAmJiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKG9uU3VjY2Vzcyl7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MocmVzcG9uc2UuZGF0YSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhyb3dzIGltbWVkaWF0ZWx5IGVycm9yIHdoZW4gdGhlIHBldGl0aW9uIGNhbGwgaXMgd3JvbmdcclxuICAgICAqIG9yIHdpdGggYSBkZWxheSBpZiB0aGUgY2FsbCBpcyB2YWxpZFxyXG4gICAgICogQHBhcmFtIGVycm9yXHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgb25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcikge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICAgICAgaWYob25FcnJvcil7XHJcbiAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHt9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IFJlc3RTZXJ2aWNlSGFuZGxlciBmcm9tICcuL1Jlc3RTZXJ2aWNlSGFuZGxlci5qcyc7XHJcbmltcG9ydCBVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlIGZyb20gJy4vVXNlclByZWZlcmVuY2VzU2VydmljZS5qcydcclxuXHJcbnZhciBSZXN0QVBJTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLlJlc3RBUElNb2R1bGUnLFtdKTtcclxuXHJcblJlc3RBUElNb2R1bGUuc2VydmljZSgnUmVzdFNlcnZpY2VIYW5kbGVyJywgWyckbG9nJywgJyRodHRwJywgJyRyZXNvdXJjZScsICdyeCcsIFJlc3RTZXJ2aWNlSGFuZGxlcl0pO1xyXG5SZXN0QVBJTW9kdWxlLnNlcnZpY2UoJ1VzZXJQcmVmZXJlbmNlc1NlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgVXNlclByZWZlcmVuY2VzU2VydmljZV0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgUmVzdEFQSU1vZHVsZTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8wOC8xNS5cclxuICogSXQgYWJzdHJhY3QgZWFjaCBvbmUgb2YgdGhlIGV4aXN0aW5nIGNhbGwgdG8gdGhlIEFQSSwgaXQgc2hvdWxkIG9ubHkgY29udGFpbnMgdGhlIGNhbGwgZnVuY3Rpb25zIGFuZCByZWZlcmVuY2VcclxuICogdG8gdGhlIGNhbGxiYWNrLCBubyBsb2dpYyBhdCBhbGwuXHJcbiAqXHJcbiAqL1xyXG5cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBSZXF1ZXN0SGFuZGxlciBmcm9tICcuL1JlcXVlc3RIYW5kbGVyLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlc3RTZXJ2aWNlSGFuZGxlciB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkaHR0cCwgJHJlc291cmNlLCByeCkge1xyXG4gICAgICAgIHRoaXMucnggPSByeDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5odHRwID0gJGh0dHA7XHJcbiAgICAgICAgdGhpcy5yZXNvdXJjZSA9ICRyZXNvdXJjZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVIZWFkZXJzKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1Jlc3RTZXJ2aWNlIExvYWRlZCcpO1xyXG4gICAgICAgIHRoaXMucmVxID0ge1xyXG4gICAgICAgICAgICBtZXRob2Q6ICcnLFxyXG4gICAgICAgICAgICB1cmw6ICcnLFxyXG4gICAgICAgICAgICBoZWFkZXJzOiB7XHJcbiAgICAgICAgICAgICAgICAnQ29udGVudC1UeXBlJzogJ2FwcGxpY2F0aW9uL2pzb247Y2hhcnNldD1VVEYtOCdcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGF0YTogW11cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVIZWFkZXJzKCkge1xyXG4gICAgICAgIHRoaXMuaHR0cC5kZWZhdWx0cy5oZWFkZXJzLnBvc3RbJ0NvbnRlbnQtVHlwZSddID0gJ2FwcGxpY2F0aW9uL3gtd3d3LWZvcm0tdXJsZW5jb2RlZCc7XHJcbiAgICB9XHJcblxyXG4gICAgVGFza1NlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldEZlZWRzOiAoY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgndGVzdC9tb2NrdXBEYXRhL1Rhc2tNYW5hZ2VyL3Rhc2tNYW5hZ2VyTGlzdC5qc29uJyksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgY29tbW9uU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgZ2V0VGltZVpvbmVDb25maWd1cmF0aW9uOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvdXNlci9wcmVmZXJlbmNlcy9DVVJSX0RUX0ZPUk1BVCxDVVJSX1RaJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgbGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgZ2V0TGljZW5zZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2VzJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvZW52aXJvbm1lbnQnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0UHJvamVjdERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL3Byb2plY3QnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0TGljZW5zZUxpc3Q6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0OiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvcmVxdWVzdCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBhcHBseUxpY2Vuc2U6ICAobGljZW5zZUlkLCBkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9sb2FkJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEhhc2hDb2RlOiAgKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnR0VUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9oYXNoJztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbWFpbENvbnRlbnQ6ICAobGljZW5zZUlkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdHRVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2VtYWlsL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIC8vLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICAgICAgICAgICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdDogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvZW1haWwvcmVxdWVzdCc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZW1haWxSZXF1ZXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRlbGV0ZUxpY2Vuc2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdERUxFVEUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBsaWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIHJlcXVlc3RJbXBvcnQ6ICAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS9yZXF1ZXN0JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbWFuYWdlci9saWNlbnNlJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldFByb2plY3REYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9wcm9qZWN0JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvZW52aXJvbm1lbnQnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0S2V5Q29kZTogIChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9rZXknO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNhdmVMaWNlbnNlOiAobGljZW5zZUlkLCBsaWNlbnNlTW9kaWZpZWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BVVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGxpY2Vuc2VNb2RpZmllZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICByZXZva2VMaWNlbnNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBhY3RpdmF0ZUxpY2Vuc2U6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvYWN0aXZhdGUnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEFjdGl2aXR5TG9nOiAobGljZW5zZUlkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdHRVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvYWN0aXZpdHlsb2cnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIG1hbnVhbGx5UmVxdWVzdDogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9lbWFpbC9zZW5kJztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgZ2V0Tm90aWNlTGlzdDogKG9uU3VjY2VzcykgPT4geyAvLyByZWFsIHdzIGV4YW1wbGVcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9ub3RpY2VzJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNyZWF0ZU5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9ub3RpY2VzJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGVkaXROb3RpY2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQVVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9ub3RpY2VzLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRlbGV0ZU5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAzLzcvMjAxNy5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVXNlclByZWZlcmVuY2VzU2VydmljZSBJbnN0YW5jZWQnKTtcclxuXHJcbiAgICAgICAgdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24gPSB7XHJcbiAgICAgICAgICAgIHByZWZlcmVuY2VzOiB7fVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBnZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24ob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5jb21tb25TZXJ2aWNlSGFuZGxlcigpLmdldFRpbWVab25lQ29uZmlndXJhdGlvbigoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbiA9IGRhdGEuZGF0YTtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcygpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldENvbnZlcnRlZERhdGVJbnRvVGltZVpvbmUoZGF0ZVN0cmluZykge1xyXG4gICAgICAgIHZhciB0aW1lU3RyaW5nID0gZGF0ZVN0cmluZztcclxuICAgICAgICB2YXIgdXNlckRURm9ybWF0ID0gdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24ucHJlZmVyZW5jZXMuQ1VSUl9EVF9GT1JNQVQ7XHJcbiAgICAgICAgdmFyIHRpbWVab25lID0gdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24ucHJlZmVyZW5jZXMuQ1VSUl9UWjtcclxuXHJcbiAgICAgICAgaWYoZGF0ZVN0cmluZyAhPT0gJ251bGwnICYmXHJcbiAgICAgICAgICAgIGRhdGVTdHJpbmcgJiYgbW9tZW50KGRhdGVTdHJpbmcpLmlzVmFsaWQoKSAmJlxyXG4gICAgICAgICAgICAhKHR5cGVvZiBOdW1iZXIoZGF0ZVN0cmluZy50b1N0cmluZygpKSA9PT0gJ251bWJlcicgJiYgIWlzTmFOKGRhdGVTdHJpbmcudG9TdHJpbmcoKSkpKXtcclxuICAgICAgICAgICAgaWYgKHRpbWVab25lID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgICAgICB0aW1lWm9uZSA9ICdHTVQnO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHZhciBmb3JtYXQgPSAnTU0vREQvWVlZWSc7XHJcbiAgICAgICAgICAgIGlmICh1c2VyRFRGb3JtYXQgPT09ICdERC9NTS9ZWVlZJykge1xyXG4gICAgICAgICAgICAgICAgZm9ybWF0ID0gJ0REL01NL1lZWVknO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHRpbWVTdHJpbmcgPSBtb21lbnQoZGF0ZVN0cmluZykudHoodGltZVpvbmUpLmZvcm1hdChmb3JtYXQpXHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICByZXR1cm4gdGltZVN0cmluZyAhPT0gJ251bGwnPyB0aW1lU3RyaW5nOiAnJztcclxuICAgIH1cclxuXHJcbiAgICBnZXRDb252ZXJ0ZWREYXRlVGltZUludG9UaW1lWm9uZShkYXRlU3RyaW5nKSB7XHJcbiAgICAgICAgdmFyIHRpbWVTdHJpbmcgPSBkYXRlU3RyaW5nO1xyXG4gICAgICAgIHZhciB1c2VyRFRGb3JtYXQgPSB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbi5wcmVmZXJlbmNlcy5DVVJSX0RUX0ZPUk1BVDtcclxuICAgICAgICB2YXIgdGltZVpvbmUgPSB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbi5wcmVmZXJlbmNlcy5DVVJSX1RaO1xyXG5cclxuICAgICAgICBpZihkYXRlU3RyaW5nICE9PSAnbnVsbCcgJiZcclxuICAgICAgICAgICAgZGF0ZVN0cmluZyAmJiBtb21lbnQoZGF0ZVN0cmluZykuaXNWYWxpZCgpICYmXHJcbiAgICAgICAgICAgICEodHlwZW9mIE51bWJlcihkYXRlU3RyaW5nLnRvU3RyaW5nKCkpID09PSAnbnVtYmVyJyAmJiAhaXNOYU4oZGF0ZVN0cmluZy50b1N0cmluZygpKSkpe1xyXG4gICAgICAgICAgICBpZiAodGltZVpvbmUgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgICAgIHRpbWVab25lID0gJ0dNVCc7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgdmFyIGZvcm1hdCA9ICdNTS9ERC9ZWVlZIGhoOm1tIGEnXHJcbiAgICAgICAgICAgIGlmICh1c2VyRFRGb3JtYXQgPT09ICdERC9NTS9ZWVlZJykge1xyXG4gICAgICAgICAgICAgICAgZm9ybWF0ID0gJ0REL01NL1lZWVkgaGg6bW0gYSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB0aW1lU3RyaW5nID0gbW9tZW50KGRhdGVTdHJpbmcpLnR6KHRpbWVab25lKS5mb3JtYXQoZm9ybWF0KVxyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgcmV0dXJuIHRpbWVTdHJpbmcgIT09ICdudWxsJz8gdGltZVN0cmluZzogJyc7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBLZW5kbyBEYXRlIEZvcm1hdCBpcyBxdWl0ZSBkaWZmZXJlbnQgYW5kIHRocmVhdHMgdGhlIGNoYXJzIHdpdGggYSBkaWZmZXJlbnQgbWVhbmluZ1xyXG4gICAgICogdGhpcyBGdW5jdGlvbnMgcmV0dXJuIHRoZSBvbmUgaW4gdGhlIHN0YW5kYXIgZm9ybWF0LlxyXG4gICAgICogQ29uc2lkZXIgdGhpcyBpcyBvbmx5IGZyb20gb3VyIEN1cnJlbnQgRm9ybWF0XHJcbiAgICAgKi9cclxuICAgIGdldENvbnZlcnRlZERhdGVGb3JtYXRUb0tlbmRvRGF0ZSgpIHtcclxuICAgICAgICB2YXIgdXNlckRURm9ybWF0ID0gdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24ucHJlZmVyZW5jZXMuQ1VSUl9EVF9GT1JNQVQ7XHJcblxyXG4gICAgICAgIHZhciBmb3JtYXQgPSAnTU0vZGQveXl5eSc7XHJcbiAgICAgICAgaWYgKHVzZXJEVEZvcm1hdCAhPT0gbnVsbCkge1xyXG4gICAgICAgICAgICBmb3JtYXQgPSBmb3JtYXQucmVwbGFjZSgnREQnLCAnZGQnKTtcclxuICAgICAgICAgICAgZm9ybWF0ID0gZm9ybWF0LnJlcGxhY2UoJ1lZWVknLCAneXl5eScpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgcmV0dXJuIGZvcm1hdDtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIEVTNiBJbnRlcmNlcHRvciBjYWxscyBpbm5lciBtZXRob2RzIGluIGEgZ2xvYmFsIHNjb3BlLCB0aGVuIHRoZSBcInRoaXNcIiBpcyBiZWluZyBsb3N0XHJcbiAqIGluIHRoZSBkZWZpbml0aW9uIG9mIHRoZSBDbGFzcyBmb3IgaW50ZXJjZXB0b3JzIG9ubHlcclxuICogVGhpcyBpcyBhIGludGVyZmFjZSB0aGF0IHRha2UgY2FyZSBvZiB0aGUgaXNzdWUuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IC8qIGludGVyZmFjZSovIGNsYXNzIEh0dHBJbnRlcmNlcHRvciB7XHJcbiAgICBjb25zdHJ1Y3RvcihtZXRob2RUb0JpbmQpIHtcclxuICAgICAgICAvLyBJZiBub3QgbWV0aG9kIHRvIGJpbmQsIHdlIGFzc3VtZSBvdXIgaW50ZXJjZXB0b3IgaXMgdXNpbmcgYWxsIHRoZSBpbm5lciBmdW5jdGlvbnNcclxuICAgICAgICBpZighbWV0aG9kVG9CaW5kKSB7XHJcbiAgICAgICAgICAgIFsncmVxdWVzdCcsICdyZXF1ZXN0RXJyb3InLCAncmVzcG9uc2UnLCAncmVzcG9uc2VFcnJvciddXHJcbiAgICAgICAgICAgICAgICAuZm9yRWFjaCgobWV0aG9kKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpc1ttZXRob2RdKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXNbbWV0aG9kXSA9IHRoaXNbbWV0aG9kXS5iaW5kKHRoaXMpO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIC8vIG1ldGhvZFRvQmluZCByZWZlcmVuY2UgdG8gYSBzaW5nbGUgY2hpbGQgY2xhc3NcclxuICAgICAgICAgICAgdGhpc1ttZXRob2RUb0JpbmRdID0gdGhpc1ttZXRob2RUb0JpbmRdLmJpbmQodGhpcyk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIFVzZSB0aGlzIG1vZHVsZSB0byBtb2RpZnkgYW55dGhpbmcgcmVsYXRlZCB0byB0aGUgSGVhZGVycyBhbmQgUmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5cclxuXHJcbnZhciBIVFRQTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkhUVFBNb2R1bGUnLCBbJ25nUmVzb3VyY2UnXSkuY29uZmlnKFsnJGh0dHBQcm92aWRlcicsIGZ1bmN0aW9uKCRodHRwUHJvdmlkZXIpe1xyXG5cclxuICAgIC8vaW5pdGlhbGl6ZSBnZXQgaWYgbm90IHRoZXJlXHJcbiAgICBpZiAoISRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXQpIHtcclxuICAgICAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0ID0ge307XHJcbiAgICB9XHJcblxyXG4gICAgLy9EaXNhYmxlIElFIGFqYXggcmVxdWVzdCBjYWNoaW5nXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydJZi1Nb2RpZmllZC1TaW5jZSddID0gJ01vbiwgMjYgSnVsIDE5OTcgMDU6MDA6MDAgR01UJztcclxuICAgIC8vIGV4dHJhXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydDYWNoZS1Db250cm9sJ10gPSAnbm8tY2FjaGUnO1xyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnUHJhZ21hJ10gPSAnbm8tY2FjaGUnO1xyXG5cclxuXHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlcXVlc3RcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlc3BvbnNlXHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcblxyXG5cclxufV0pO1xyXG5cclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIVFRQTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIEl0IGltcGxlbWVudCBhbiBhYnN0cmFjdCBjYWxsIHRvIEhUVFAgSW50ZXJjZXB0b3JzIHRvIG1hbmFnZSBlcnJvciBoYW5kbGVyXHJcbiAqIFNvbWV0aW1lcyBhIHJlcXVlc3QgY2FuJ3QgYmUgc2VudCBvciBpdCBpcyByZWplY3RlZCBieSBhbiBpbnRlcmNlcHRvci5cclxuICogUmVxdWVzdCBlcnJvciBpbnRlcmNlcHRvciBjYXB0dXJlcyByZXF1ZXN0cyB0aGF0IGhhdmUgYmVlbiBjYW5jZWxlZCBieSBhIHByZXZpb3VzIHJlcXVlc3QgaW50ZXJjZXB0b3IuXHJcbiAqIEl0IGNhbiBiZSB1c2VkIGluIG9yZGVyIHRvIHJlY292ZXIgdGhlIHJlcXVlc3QgYW5kIHNvbWV0aW1lcyB1bmRvIHRoaW5ncyB0aGF0IGhhdmUgYmVlbiBzZXQgdXAgYmVmb3JlIGEgcmVxdWVzdCxcclxuICogbGlrZSByZW1vdmluZyBvdmVybGF5cyBhbmQgbG9hZGluZyBpbmRpY2F0b3JzLCBlbmFibGluZyBidXR0b25zIGFuZCBmaWVsZHMgYW5kIHNvIG9uLlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3JlcXVlc3RFcnJvcicpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdEVycm9yKHJlamVjdGlvbikge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vaWYgKGNhblJlY292ZXIocmVqZWN0aW9uKSkge1xyXG4gICAgICAgIC8vICAgIHJldHVybiByZXNwb25zZU9yTmV3UHJvbWlzZVxyXG4gICAgICAgIC8vfVxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnEucmVqZWN0KHJlamVjdGlvbik7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuRXJyb3IoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSXQgaW1wbGVtZW50IGFuIGFic3RyYWN0IGNhbGwgdG8gSFRUUCBJbnRlcmNlcHRvcnMgdG8gbWFuYWdlIG9ubHkgcmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXF1ZXN0Jyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdChjb25maWcpIHtcclxuICAgICAgICAvLyBXZSBjYW4gYWRkIGhlYWRlcnMgaWYgb24gdGhlIGluY29taW5nIHJlcXVlc3QgbWFkZSBpdCB3ZSBoYXZlIHRoZSB0b2tlbiBpbnNpZGVcclxuICAgICAgICAvLyBkZWZpbmVkIGJ5IHNvbWUgY29uZGl0aW9uc1xyXG4gICAgICAgIC8vY29uZmlnLmhlYWRlcnNbJ3gtc2Vzc2lvbi10b2tlbiddID0gbXkudG9rZW47XHJcblxyXG4gICAgICAgIGNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KGNvbmZpZyk7XHJcblxyXG4gICAgICAgIHJldHVybiBjb25maWcgfHwgdGhpcy5xLndoZW4oY29uZmlnKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXF1ZXN0KCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIElmIGJhY2tlbmQgY2FsbCBmYWlscyBvciBpdCBtaWdodCBiZSByZWplY3RlZCBieSBhIHJlcXVlc3QgaW50ZXJjZXB0b3Igb3IgYnkgYSBwcmV2aW91cyByZXNwb25zZSBpbnRlcmNlcHRvcjtcclxuICogSW4gdGhvc2UgY2FzZXMsIHJlc3BvbnNlIGVycm9yIGludGVyY2VwdG9yIGNhbiBoZWxwIHVzIHRvIHJlY292ZXIgdGhlIGJhY2tlbmQgY2FsbC5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZUVycm9yJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzcG9uc2VFcnJvcihyZWplY3Rpb24pIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvL2lmIChjYW5SZWNvdmVyKHJlamVjdGlvbikpIHtcclxuICAgICAgICAvLyAgICByZXR1cm4gcmVzcG9uc2VPck5ld1Byb21pc2VcclxuICAgICAgICAvLyB9XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMucS5yZWplY3QocmVqZWN0aW9uKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5FcnJvcigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBUaGlzIG1ldGhvZCBpcyBjYWxsZWQgcmlnaHQgYWZ0ZXIgJGh0dHAgcmVjZWl2ZXMgdGhlIHJlc3BvbnNlIGZyb20gdGhlIGJhY2tlbmQsXHJcbiAqIHNvIHlvdSBjYW4gbW9kaWZ5IHRoZSByZXNwb25zZSBhbmQgbWFrZSBvdGhlciBhY3Rpb25zLiBUaGlzIGZ1bmN0aW9uIHJlY2VpdmVzIGEgcmVzcG9uc2Ugb2JqZWN0IGFzIGEgcGFyYW1ldGVyXHJcbiAqIGFuZCBoYXMgdG8gcmV0dXJuIGEgcmVzcG9uc2Ugb2JqZWN0IG9yIGEgcHJvbWlzZS4gVGhlIHJlc3BvbnNlIG9iamVjdCBpbmNsdWRlc1xyXG4gKiB0aGUgcmVxdWVzdCBjb25maWd1cmF0aW9uLCBoZWFkZXJzLCBzdGF0dXMgYW5kIGRhdGEgdGhhdCByZXR1cm5lZCBmcm9tIHRoZSBiYWNrZW5kLlxyXG4gKiBSZXR1cm5pbmcgYW4gaW52YWxpZCByZXNwb25zZSBvYmplY3Qgb3IgcHJvbWlzZSB0aGF0IHdpbGwgYmUgcmVqZWN0ZWQsIHdpbGwgbWFrZSB0aGUgJGh0dHAgY2FsbCB0byBmYWlsLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZScpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXNwb25zZShyZXNwb25zZSkge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBzdWNjZXNzXHJcblxyXG4gICAgICAgIHJlc3BvbnNlLmNvbmZpZy5yZXNwb25zZVRpbWVzdGFtcCA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZXNwb25zZSk7XHJcbiAgICAgICAgcmV0dXJuIHJlc3BvbnNlIHx8IHRoaXMucS53aGVuKHJlc3BvbnNlKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXNwb25zZSgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG59XHJcblxyXG4iXX0=
