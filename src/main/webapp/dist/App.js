(function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c="function"==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code="MODULE_NOT_FOUND",a}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r)},p,p.exports,r,e,n,t)}return n[i].exports}for(var u="function"==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o}return r})()({1:[function(require,module,exports){
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
						inception: params.license.activationDate,
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
												return { title: 'Confirmation Required', message: 'You are about to delete the selected license. Are you sure? Click Confirm to delete otherwise press Cancel.' };
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
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project', template: '<span style="text-transform: capitalize;">#=((data.project && data.project.name)? data.project.name.toLowerCase(): "" )#</span>' }, { field: 'email', title: 'Contact Email' }, { field: 'status', title: 'Status', template: '<span style="text-transform: capitalize;">#=((data.status)? data.status.toLowerCase(): "" )#</span>' }, { field: 'type.name', title: 'Type', template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#' }, { field: 'method.name', title: 'Method', template: '<span style="text-transform: capitalize;">#=((data.method && data.method.name)? data.method.name.toLowerCase(): "" )#</span>' }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'activationDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.activationDate | convertDateIntoTimeZone }}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.expirationDate | convertDateIntoTimeZone }}' }, { field: 'environment', title: 'Environment', template: '<span style="text-transform: capitalize;">#=((data.environment)? data.environment.toLowerCase(): "" )#</span>' }],
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
    }, {
        key: 'deleteLicense',
        value: function deleteLicense() {
            var _this6 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
                controller: 'DialogAction as dialogAction',
                size: 'sm',
                resolve: {
                    params: function params() {
                        return { title: 'Confirmation Required', message: 'You are about to delete the selected license. Are you sure? Click Confirm to delete otherwise press Cancel.' };
                    }
                }
            });

            modalInstance.result.then(function () {
                _this6.licenseManagerService.deleteLicense(_this6.licenseModel, function (data) {
                    _this6.uibModalInstance.close(data);
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
            var _this7 = this;

            if (this.isDirty()) {
                this.editMode = false;
                this.prepareControlActionButtons();
                this.licenseManagerService.saveLicense(this.licenseModel, function (data) {
                    _this7.reloadRequired = true;
                    _this7.saveForm(_this7.licenseModel);
                    _this7.reloadLicenseManagerList();
                    _this7.log.info('License Saved');
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
            var _this8 = this;

            this.selectEnvironmentListOptions = {
                dataSource: {
                    transport: {
                        read: function read(e) {
                            _this8.licenseManagerService.getEnvironmentDataSource(function (data) {
                                if (!_this8.licenseModel.environment) {
                                    _this8.licenseModel.environment = data[0];
                                }

                                _this8.saveForm(_this8.licenseModel);
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
            var _this9 = this;

            if (this.editMode) {
                this.resetForm(function () {
                    _this9.onResetForm();
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
                activationDate: license.initDate ? moment(license.initDate).format('YYYY-MM-DD') : '',
                expirationDate: license.endDate ? moment(license.endDate).format('YYYY-MM-DD') : '',
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
        key: 'deleteLicense',
        value: function deleteLicense(license, onSuccess) {
            this.restService.licenseManagerServiceHandler().deleteLicense(license, function (data) {
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
                deleteLicense: function deleteLicense(data, onSuccess, onError) {
                    _this4.req.method = 'DELETE';
                    _this4.req.url = '../ws/manager/license/' + data.id + '/delete';
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJ3ZWItYXBwL2FwcC1qcy9jb25maWcvQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcC9hcHAtanMvY29uZmlnL0FwcC5qcyIsIndlYi1hcHAvYXBwLWpzL2RpcmVjdGl2ZXMvaW5kZXguanMiLCJ3ZWItYXBwL2FwcC1qcy9kaXJlY3RpdmVzL3Rvb2xzL01vZGFsV2luZG93QWN0aXZhdGlvbi5qcyIsIndlYi1hcHAvYXBwLWpzL2RpcmVjdGl2ZXMvdG9vbHMvVG9hc3RIYW5kbGVyLmpzIiwid2ViLWFwcC9hcHAtanMvbWFpbi5qcyIsIndlYi1hcHAvYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5qcyIsIndlYi1hcHAvYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJNb2R1bGUuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9MaWNlbnNlQWRtaW5Nb2R1bGUuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5LmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vY3JlYXRlZC9DcmVhdGVkTGljZW5zZS5qcyIsIndlYi1hcHAvYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2RldGFpbC9MaWNlbnNlRGV0YWlsLmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vbGlzdC9MaWNlbnNlQWRtaW5MaXN0LmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vbWFudWFsbHlSZXF1ZXN0L01hbnVhbGx5UmVxdWVzdC5qcyIsIndlYi1hcHAvYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9zZXJ2aWNlL0xpY2Vuc2VBZG1pblNlcnZpY2UuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL0xpY2Vuc2VNYW5hZ2VyTW9kdWxlLmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0LmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL3NlcnZpY2UvTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL05vdGljZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvZWRpdC9FZGl0Tm90aWNlLmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2xpc3QvTm90aWNlTGlzdC5qcyIsIndlYi1hcHAvYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9zZXJ2aWNlL05vdGljZU1hbmFnZXJTZXJ2aWNlLmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9UYXNrTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHAvYXBwLWpzL21vZHVsZXMvdGFza01hbmFnZXIvZWRpdC9UYXNrTWFuYWdlckVkaXQuanMiLCJ3ZWItYXBwL2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2xpc3QvVGFza01hbmFnZXJDb250cm9sbGVyLmpzIiwid2ViLWFwcC9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9zZXJ2aWNlL1Rhc2tNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHAvYXBwLWpzL21vZHVsZXMvdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzIiwid2ViLWFwcC9hcHAtanMvc2VydmljZXMvUmVzdEFQSS9SZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHAvYXBwLWpzL3NlcnZpY2VzL1Jlc3RBUEkvUmVzdEFQSU1vZHVsZS5qcyIsIndlYi1hcHAvYXBwLWpzL3NlcnZpY2VzL1Jlc3RBUEkvUmVzdFNlcnZpY2VIYW5kbGVyLmpzIiwid2ViLWFwcC9hcHAtanMvc2VydmljZXMvUmVzdEFQSS9Vc2VyUHJlZmVyZW5jZXNTZXJ2aWNlLmpzIiwid2ViLWFwcC9hcHAtanMvc2VydmljZXMvaHR0cC9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMiLCJ3ZWItYXBwL2FwcC1qcy9zZXJ2aWNlcy9odHRwL0hUVFBNb2R1bGUuanMiLCJ3ZWItYXBwL2FwcC1qcy9zZXJ2aWNlcy9odHRwL0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwL2FwcC1qcy9zZXJ2aWNlcy9odHRwL0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcC9hcHAtanMvc2VydmljZXMvaHR0cC9IVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHAvYXBwLWpzL3NlcnZpY2VzL2h0dHAvSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBOzs7QUNBQTs7Ozs7Ozs7OztBQVVBLElBQUksUUFBUSxRQUFaLFVBQVksQ0FBWjs7QUFFQTs7Ozs7QUFLQSxNQUFBLFNBQUEsR0FBa0IsVUFBQSxPQUFBLEVBQUEsRUFBQSxFQUF1QjtBQUNyQzs7QUFDQSxRQUFJLFFBQVEsUUFBQSxLQUFBLENBQVosT0FBQTtBQUNBLFFBQUksVUFBQSxRQUFBLElBQXNCLFVBQTFCLFNBQUEs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QWI7QUFDQSxjQUFBLEdBQUEsQ0FBQSxLQUFBLENBQUEsMENBQUE7QUFMc0IsZUFBQSxLQUFBO0FBTXpCOzs7O2lDQUVRLFMsRUFBVTtBQUNmOztBQUVBLHNCQUFBLE1BQUEsQ0FBQSxpQkFBQSxHQUFvQyxJQUFBLElBQUEsR0FBcEMsT0FBb0MsRUFBcEM7O0FBRUEsaUJBQUEsS0FBQSxDQUFBLE1BQUEsQ0FBQSxTQUFBO0FBQ0EsbUJBQU8sYUFBWSxLQUFBLENBQUEsQ0FBQSxJQUFBLENBQW5CLFNBQW1CLENBQW5CO0FBQ0g7Ozt5Q0FFZ0I7QUFDYixtQkFBTyxLQUFBLEtBQUEsQ0FBUCxPQUFBO0FBQ0g7Ozs7R0FwQnVELGNBQWUsMkJBQUEsTzs7a0JBQXRELDhCIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24oKXtmdW5jdGlvbiByKGUsbix0KXtmdW5jdGlvbiBvKGksZil7aWYoIW5baV0pe2lmKCFlW2ldKXt2YXIgYz1cImZ1bmN0aW9uXCI9PXR5cGVvZiByZXF1aXJlJiZyZXF1aXJlO2lmKCFmJiZjKXJldHVybiBjKGksITApO2lmKHUpcmV0dXJuIHUoaSwhMCk7dmFyIGE9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitpK1wiJ1wiKTt0aHJvdyBhLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsYX12YXIgcD1uW2ldPXtleHBvcnRzOnt9fTtlW2ldWzBdLmNhbGwocC5leHBvcnRzLGZ1bmN0aW9uKHIpe3ZhciBuPWVbaV1bMV1bcl07cmV0dXJuIG8obnx8cil9LHAscC5leHBvcnRzLHIsZSxuLHQpfXJldHVybiBuW2ldLmV4cG9ydHN9Zm9yKHZhciB1PVwiZnVuY3Rpb25cIj09dHlwZW9mIHJlcXVpcmUmJnJlcXVpcmUsaT0wO2k8dC5sZW5ndGg7aSsrKW8odFtpXSk7cmV0dXJuIG99cmV0dXJuIHJ9KSgpIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8yMC8yMDE1LlxyXG4gKiBURFNNIGlzIGEgZ2xvYmFsIG9iamVjdCB0aGF0IGNvbWVzIGZyb20gQXBwLmpzXHJcbiAqXHJcbiAqIFRoZSBmb2xsb3dpbmcgaGVscGVyIHdvcmtzIGluIGEgd2F5IHRvIG1ha2UgYXZhaWxhYmxlIHRoZSBjcmVhdGlvbiBvZiBEaXJlY3RpdmUsIFNlcnZpY2VzIGFuZCBDb250cm9sbGVyXHJcbiAqIG9uIGZseSBvciB3aGVuIGRlcGxveWluZyB0aGUgYXBwLlxyXG4gKlxyXG4gKiBXZSByZWR1Y2UgdGhlIHVzZSBvZiBjb21waWxlIGFuZCBleHRyYSBzdGVwc1xyXG4gKi9cclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4vQXBwLmpzJyk7XHJcblxyXG4vKipcclxuICogTGlzdGVuIHRvIGFuIGV4aXN0aW5nIGRpZ2VzdCBvZiB0aGUgY29tcGlsZSBwcm92aWRlciBhbmQgZXhlY3V0ZSB0aGUgJGFwcGx5IGltbWVkaWF0ZWx5IG9yIGFmdGVyIGl0J3MgcmVhZHlcclxuICogQHBhcmFtIGN1cnJlbnRcclxuICogQHBhcmFtIGZuXHJcbiAqL1xyXG5URFNUTS5zYWZlQXBwbHkgPSBmdW5jdGlvbiAoY3VycmVudCwgZm4pIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciBwaGFzZSA9IGN1cnJlbnQuJHJvb3QuJCRwaGFzZTtcclxuICAgIGlmIChwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRldmFsKGZuKTtcclxuICAgICAgICB9XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRhcHBseShmbik7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgY3VycmVudC4kYXBwbHkoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBkaXJlY3RpdmUgYXN5bmMgaWYgdGhlIGNvbXBpbGVQcm92aWRlciBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIuZGlyZWN0aXZlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5kaXJlY3RpdmUpIHtcclxuICAgICAgICBURFNUTS5kaXJlY3RpdmUoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBjb250cm9sbGVycyBhc3luYyBpZiB0aGUgY29udHJvbGxlclByb3ZpZGVyIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlQ29udHJvbGxlciA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbnRyb2xsZXJQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXJQcm92aWRlci5yZWdpc3RlcihzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uY29udHJvbGxlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXIoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBzZXJ2aWNlIGFzeW5jIGlmIHRoZSBwcm92aWRlU2VydmljZSBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZVNlcnZpY2UgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZSkge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZS5zZXJ2aWNlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5jb250cm9sbGVyKSB7XHJcbiAgICAgICAgVERTVE0uc2VydmljZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBGb3IgTGVnYWN5IHN5c3RlbSwgd2hhdCBpcyBkb2VzIGlzIHRvIHRha2UgcGFyYW1zIGZyb20gdGhlIHF1ZXJ5XHJcbiAqIG91dHNpZGUgdGhlIEFuZ3VsYXJKUyB1aS1yb3V0aW5nLlxyXG4gKiBAcGFyYW0gcGFyYW0gLy8gUGFyYW0gdG8gc2VhcmMgZm9yIC9leGFtcGxlLmh0bWw/YmFyPWZvbyNjdXJyZW50U3RhdGVcclxuICovXHJcblREU1RNLmdldFVSTFBhcmFtID0gZnVuY3Rpb24gKHBhcmFtKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICAkLnVybFBhcmFtID0gZnVuY3Rpb24gKG5hbWUpIHtcclxuICAgICAgICB2YXIgcmVzdWx0cyA9IG5ldyBSZWdFeHAoJ1tcXD8mXScgKyBuYW1lICsgJz0oW14mI10qKScpLmV4ZWMod2luZG93LmxvY2F0aW9uLmhyZWYpO1xyXG4gICAgICAgIGlmIChyZXN1bHRzID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgIHJldHVybiBudWxsO1xyXG4gICAgICAgIH1cclxuICAgICAgICBlbHNlIHtcclxuICAgICAgICAgICAgcmV0dXJuIHJlc3VsdHNbMV0gfHwgMDtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG5cclxuICAgIHJldHVybiAkLnVybFBhcmFtKHBhcmFtKTtcclxufTtcclxuXHJcbi8qKlxyXG4gKiBUaGlzIGNvZGUgd2FzIGludHJvZHVjZWQgb25seSBmb3IgdGhlIGlmcmFtZSBtaWdyYXRpb25cclxuICogaXQgZGV0ZWN0IHdoZW4gbW91c2UgZW50ZXJcclxuICovXHJcblREU1RNLmlmcmFtZUxvYWRlciA9IGZ1bmN0aW9uICgpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgICQoJy5pZnJhbWVMb2FkZXInKS5ob3ZlcihcclxuICAgICAgICBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICQoJy5uYXZiYXItdWwtY29udGFpbmVyIC5kcm9wZG93bi5vcGVuJykucmVtb3ZlQ2xhc3MoJ29wZW4nKTtcclxuICAgICAgICB9LCBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgfVxyXG4gICAgKTtcclxufTtcclxuXHJcblREU1RNLmdldENvbnZlcnRlZERhdGVGb3JtYXQgPSBmdW5jdGlvbiggZGF0ZVN0cmluZywgdXNlckRURm9ybWF0LCB0aW1lWm9uZSApIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciB0aW1lU3RyaW5nID0gJyc7XHJcbiAgICBpZihkYXRlU3RyaW5nKXtcclxuICAgICAgICBpZiAodGltZVpvbmUgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgdGltZVpvbmUgPSAnR01UJztcclxuICAgICAgICB9XHJcbiAgICAgICAgdmFyIGZvcm1hdCA9ICdNTS9ERC9ZWVlZJztcclxuICAgICAgICBpZiAodXNlckRURm9ybWF0ID09PSAnREQvTU0vWVlZWScpIHtcclxuICAgICAgICAgICAgZm9ybWF0ID0gJ0REL01NL1lZWVknO1xyXG4gICAgICAgIH1cclxuICAgICAgICAvLyBDb252ZXJ0IHp1bHUgZGF0ZXRpbWUgdG8gYSBzcGVjaWZpYyB0aW1lem9uZS9mb3JtYXRcclxuICAgICAgICB0aW1lU3RyaW5nID0gbW9tZW50KGRhdGVTdHJpbmcpLnR6KHRpbWVab25lKS5mb3JtYXQoZm9ybWF0KVxyXG4gICAgfVxyXG4gICAgcmV0dXJuIHRpbWVTdHJpbmc7XHJcbn07XHJcblxyXG53aW5kb3cuVERTVE0gPSBURFNUTTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzE2LzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxucmVxdWlyZSgnYW5ndWxhcicpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWFuaW1hdGUnKTtcclxucmVxdWlyZSgnYW5ndWxhci1tb2NrcycpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXNhbml0aXplJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItcmVzb3VyY2UnKTtcclxucmVxdWlyZSgnYW5ndWxhci10cmFuc2xhdGUnKTtcclxucmVxdWlyZSgnYW5ndWxhci10cmFuc2xhdGUtbG9hZGVyLXBhcnRpYWwnKTtcclxucmVxdWlyZSgnYW5ndWxhci11aS1ib290c3RyYXAnKTtcclxucmVxdWlyZSgnbmdjbGlwYm9hcmQnKTtcclxucmVxdWlyZSgndWktcm91dGVyJyk7XHJcbnJlcXVpcmUoJ3J4LWFuZ3VsYXInKTtcclxucmVxdWlyZSgnYXBpLWNoZWNrJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItZm9ybWx5Jyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItZm9ybWx5LXRlbXBsYXRlcy1ib290c3RyYXAnKTtcclxuXHJcbi8vIE1vZHVsZXNcclxuaW1wb3J0IEhUVFBNb2R1bGUgZnJvbSAnLi4vc2VydmljZXMvaHR0cC9IVFRQTW9kdWxlLmpzJztcclxuaW1wb3J0IFJlc3RBUElNb2R1bGUgZnJvbSAnLi4vc2VydmljZXMvUmVzdEFQSS9SZXN0QVBJTW9kdWxlLmpzJ1xyXG5pbXBvcnQgSGVhZGVyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvaGVhZGVyL0hlYWRlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBMaWNlbnNlQWRtaW5Nb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9saWNlbnNlQWRtaW4vTGljZW5zZUFkbWluTW9kdWxlLmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvTGljZW5zZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTm90aWNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL25vdGljZU1hbmFnZXIvTm90aWNlTWFuYWdlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL3Rhc2tNYW5hZ2VyL1Rhc2tNYW5hZ2VyTW9kdWxlLmpzJztcclxuXHJcbnZhciBQcm92aWRlckNvcmUgPSB7fTtcclxuXHJcbnZhciBURFNUTSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTScsIFtcclxuICAgICduZ1Nhbml0aXplJyxcclxuICAgICduZ1Jlc291cmNlJyxcclxuICAgICduZ0FuaW1hdGUnLFxyXG4gICAgJ3Bhc2NhbHByZWNodC50cmFuc2xhdGUnLCAvLyAnYW5ndWxhci10cmFuc2xhdGUnXHJcbiAgICAndWkucm91dGVyJyxcclxuICAgICduZ2NsaXBib2FyZCcsXHJcbiAgICAna2VuZG8uZGlyZWN0aXZlcycsXHJcbiAgICAncngnLFxyXG4gICAgJ2Zvcm1seScsXHJcbiAgICAnZm9ybWx5Qm9vdHN0cmFwJyxcclxuICAgICd1aS5ib290c3RyYXAnLFxyXG4gICAgSFRUUE1vZHVsZS5uYW1lLFxyXG4gICAgUmVzdEFQSU1vZHVsZS5uYW1lLFxyXG4gICAgSGVhZGVyTW9kdWxlLm5hbWUsXHJcbiAgICBUYXNrTWFuYWdlck1vZHVsZS5uYW1lLFxyXG4gICAgTGljZW5zZUFkbWluTW9kdWxlLm5hbWUsXHJcbiAgICBMaWNlbnNlTWFuYWdlck1vZHVsZS5uYW1lLFxyXG4gICAgTm90aWNlTWFuYWdlck1vZHVsZS5uYW1lXHJcbl0pLmNvbmZpZyhbXHJcbiAgICAnJGxvZ1Byb3ZpZGVyJyxcclxuICAgICckcm9vdFNjb3BlUHJvdmlkZXInLFxyXG4gICAgJyRjb21waWxlUHJvdmlkZXInLFxyXG4gICAgJyRjb250cm9sbGVyUHJvdmlkZXInLFxyXG4gICAgJyRwcm92aWRlJyxcclxuICAgICckaHR0cFByb3ZpZGVyJyxcclxuICAgICckdHJhbnNsYXRlUHJvdmlkZXInLFxyXG4gICAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgJyR1cmxSb3V0ZXJQcm92aWRlcicsXHJcbiAgICAnJGxvY2F0aW9uUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRsb2dQcm92aWRlciwgJHJvb3RTY29wZVByb3ZpZGVyLCAkY29tcGlsZVByb3ZpZGVyLCAkY29udHJvbGxlclByb3ZpZGVyLCAkcHJvdmlkZSwgJGh0dHBQcm92aWRlcixcclxuICAgICAgICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIsICR1cmxSb3V0ZXJQcm92aWRlciwgJGxvY2F0aW9uUHJvdmlkZXIpIHtcclxuXHJcbiAgICAgICAgJHJvb3RTY29wZVByb3ZpZGVyLmRpZ2VzdFR0bCgzMCk7XHJcbiAgICAgICAgLy8gR29pbmcgYmFjayB0byB5b3VcclxuICAgICAgICAkbG9jYXRpb25Qcm92aWRlci5odG1sNU1vZGUodHJ1ZSkuaGFzaFByZWZpeCgnIScpO1xyXG5cclxuICAgICAgICAkbG9nUHJvdmlkZXIuZGVidWdFbmFibGVkKHRydWUpO1xyXG5cclxuICAgICAgICAvLyBBZnRlciBib290c3RyYXBwaW5nIGFuZ3VsYXIgZm9yZ2V0IHRoZSBwcm92aWRlciBzaW5jZSBldmVyeXRoaW5nIFwid2FzIGFscmVhZHkgbG9hZGVkXCJcclxuICAgICAgICBQcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyID0gJGNvbXBpbGVQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUuY29udHJvbGxlclByb3ZpZGVyID0gJGNvbnRyb2xsZXJQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2UgPSAkcHJvdmlkZTtcclxuICAgICAgICBQcm92aWRlckNvcmUuaHR0cFByb3ZpZGVyID0gJGh0dHBQcm92aWRlcjtcclxuXHJcbiAgICAgICAgLyoqXHJcbiAgICAgICAgICogVHJhbnNsYXRpb25zXHJcbiAgICAgICAgICovXHJcblxyXG4gICAgICAgIC8qICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIudXNlU2FuaXRpemVWYWx1ZVN0cmF0ZWd5KG51bGwpO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ3Rkc3RtJyk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci51c2VMb2FkZXIoJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyJywge1xyXG4gICAgICAgICAgICB1cmxUZW1wbGF0ZTogJy4uL2kxOG4ve3BhcnR9L2FwcC5pMThuLXtsYW5nfS5qc29uJ1xyXG4gICAgICAgIH0pOyovXHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci5wcmVmZXJyZWRMYW5ndWFnZSgnZW5fVVMnKTtcclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIuZmFsbGJhY2tMYW5ndWFnZSgnZW5fVVMnKTtcclxuXHJcbiAgICAgICAgLy8kdXJsUm91dGVyUHJvdmlkZXIub3RoZXJ3aXNlKCdkYXNoYm9hcmQnKTtcclxuXHJcbiAgICB9XSkuXHJcbiAgICBydW4oWyckdHJhbnNpdGlvbnMnLCAnJGh0dHAnLCAnJGxvZycsICckbG9jYXRpb24nLCAnJHEnLCdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgZnVuY3Rpb24gKCR0cmFuc2l0aW9ucywgJGh0dHAsICRsb2csICRsb2NhdGlvbiwgJHEsIHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UpIHtcclxuICAgICAgICAkbG9nLmRlYnVnKCdDb25maWd1cmF0aW9uIGRlcGxveWVkJyk7XHJcblxyXG4gICAgICAgICR0cmFuc2l0aW9ucy5vbkJlZm9yZSgge30sICgkc3RhdGUsICR0cmFuc2l0aW9uJCkgPT4ge1xyXG4gICAgICAgICAgICB2YXIgZGVmZXIgPSAkcS5kZWZlcigpO1xyXG5cclxuICAgICAgICAgICAgdXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24oKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgZGVmZXIucmVzb2x2ZSgpO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIHJldHVybiBkZWZlci5wcm9taXNlO1xyXG4gICAgICAgIH0pO1xyXG5cclxuICAgIH1dKTtcclxuXHJcbi8vIHdlIG1hcHBlZCB0aGUgUHJvdmlkZXIgQ29yZSBsaXN0IChjb21waWxlUHJvdmlkZXIsIGNvbnRyb2xsZXJQcm92aWRlciwgcHJvdmlkZVNlcnZpY2UsIGh0dHBQcm92aWRlcikgdG8gcmV1c2UgYWZ0ZXIgb24gZmx5XHJcblREU1RNLlByb3ZpZGVyQ29yZSA9IFByb3ZpZGVyQ29yZTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBJdCBoYW5kbGVyIHRoZSBpbmRleCBmb3IgYW55IG9mIHRoZSBkaXJlY3RpdmVzIGF2YWlsYWJsZVxyXG4gKi9cclxuXHJcbnJlcXVpcmUoJy4vdG9vbHMvVG9hc3RIYW5kbGVyLmpzJyk7XHJcbnJlcXVpcmUoJy4vdG9vbHMvTW9kYWxXaW5kb3dBY3RpdmF0aW9uLmpzJyk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMzAvMTAvMjAxNi5cclxuICogTGlzdGVuIHRvIE1vZGFsIFdpbmRvdyB0byBtYWtlIGFueSBtb2RhbCB3aW5kb3cgZHJhZ2dhYmJsZVxyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi4vLi4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlKCdtb2RhbFJlbmRlcicsIFsnJGxvZycsIGZ1bmN0aW9uICgkbG9nKSB7XHJcbiAgICAkbG9nLmRlYnVnKCdNb2RhbFdpbmRvd0FjdGl2YXRpb24gbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHJlc3RyaWN0OiAnRUEnLFxyXG4gICAgICAgIGxpbms6IGZ1bmN0aW9uKCkge1xyXG4gICAgICAgICAgICAkKCcubW9kYWwtZGlhbG9nJykuZHJhZ2dhYmxlKHtcclxuICAgICAgICAgICAgICAgIGhhbmRsZTogJy5tb2RhbC1oZWFkZXInXHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH07XHJcbn1dKTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzE0LzIwMTUuXHJcbiAqIFByaW50cyBvdXQgYWxsIFRvYXN0IG1lc3NhZ2Ugd2hlbiBkZXRlY3RlZCBmcm9tIHNlcnZlciBvciBjdXN0b20gbXNnIHVzaW5nIHRoZSBkaXJlY3RpdmUgaXRzZWxmXHJcbiAqXHJcbiAqIFByb2JhYmx5IHZhbHVlcyBhcmU6XHJcbiAqXHJcbiAqIHN1Y2Nlc3MsIGRhbmdlciwgaW5mbywgd2FybmluZ1xyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi4vLi4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlKCd0b2FzdEhhbmRsZXInLCBbJyRsb2cnLCAnJHRpbWVvdXQnLCAnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InLCAnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICAnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyxcclxuICAgIGZ1bmN0aW9uICgkbG9nLCAkdGltZW91dCwgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IsXHJcbiAgICAgICAgICAgICAgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcikge1xyXG5cclxuICAgICRsb2cuZGVidWcoJ1RvYXN0SGFuZGxlciBsb2FkZWQnKTtcclxuICAgIHJldHVybiB7XHJcbiAgICAgICAgc2NvcGU6IHtcclxuICAgICAgICAgICAgbXNnOiAnPScsXHJcbiAgICAgICAgICAgIHR5cGU6ICc9JyxcclxuICAgICAgICAgICAgc3RhdHVzOiAnPSdcclxuICAgICAgICB9LFxyXG4gICAgICAgIHByaW9yaXR5OiA1LFxyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL2RpcmVjdGl2ZXMvdG9vbHMvVG9hc3RIYW5kbGVyLmh0bWwnLFxyXG4gICAgICAgIHJlc3RyaWN0OiAnRScsXHJcbiAgICAgICAgY29udHJvbGxlcjogWyckc2NvcGUnLCAnJHJvb3RTY29wZScsIGZ1bmN0aW9uICgkc2NvcGUsICRyb290U2NvcGUpIHtcclxuICAgICAgICAgICAgJHNjb3BlLmFsZXJ0ID0ge1xyXG4gICAgICAgICAgICAgICAgc3VjY2Vzczoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGltZTogMjAwMFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGRhbmdlcjoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGltZTogNDAwMFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGluZm86IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRpbWU6IDIwMDBcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICB3YXJuaW5nOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJyxcclxuICAgICAgICAgICAgICAgICAgICB0aW1lOiA0MDAwXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzID0ge1xyXG4gICAgICAgICAgICAgICAgc2hvdzogZmFsc2VcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIGZ1bmN0aW9uIHR1cm5PZmZOb3RpZmljYXRpb25zKCl7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuc3VjY2Vzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5pbmZvLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC53YXJuaW5nLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBMaXN0ZW4gdG8gYW55IHJlcXVlc3QsIHdlIGNhbiByZWdpc3RlciBsaXN0ZW5lciBpZiB3ZSB3YW50IHRvIGFkZCBleHRyYSBjb2RlLlxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVxdWVzdCgpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24oY29uZmlnKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgdG86ICcsICBjb25maWcpO1xyXG4gICAgICAgICAgICAgICAgdmFyIHRpbWUgPSBjb25maWcucmVxdWVzdFRpbWVzdGFtcDtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcodGltZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgZXJyb3I6ICcsICByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVzcG9uc2UoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlc3BvbnNlKXtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wIC0gcmVzcG9uc2UuY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdUaGUgcmVxdWVzdCB0b29rICcgKyAodGltZSAvIDEwMDApICsgJyBzZWNvbmRzJyk7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSByZXN1bHQ6ICcsIHJlc3BvbnNlKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcblxyXG4gICAgICAgICAgICAgICAgaWYocmVzcG9uc2UgJiYgcmVzcG9uc2UuaGVhZGVycyAmJiByZXNwb25zZS5oZWFkZXJzKCd4LWxvZ2luLXVybCcpKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgd2luZG93LmxvY2F0aW9uLmhyZWYgPSByZXNwb25zZS5oZWFkZXJzKCd4LWxvZ2luLXVybCcpO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmxpc3RlbkVycm9yKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZWplY3Rpb24pe1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVzcG9uc2UgZXJyb3I6ICcsIHJlamVjdGlvbik7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc3RhdHVzID0gcmVqZWN0aW9uLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc3RhdHVzVGV4dCA9IHJlamVjdGlvbi5zdGF0dXNUZXh0O1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5lcnJvcnMgPSByZWplY3Rpb24uZGF0YS5lcnJvcnM7XHJcbiAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMzAwMCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEhpZGUgdGhlIFBvcCB1cCBub3RpZmljYXRpb24gbWFudWFsbHlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRzY29wZS5vbkNhbmNlbFBvcFVwID0gZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgICAgICAgICB0dXJuT2ZmTm90aWZpY2F0aW9ucygpO1xyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkcm9vdFNjb3BlLiRvbignYnJvYWRjYXN0LW1zZycsIGZ1bmN0aW9uKGV2ZW50LCBhcmdzKSB7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdicm9hZGNhc3QtbXNnIGV4ZWN1dGVkJyk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnN0YXR1c1RleHQgPSBhcmdzLnRleHQ7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zdGF0dXMgPSBudWxsO1xyXG4gICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS50aW1lKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS4kYXBwbHkoKTsgLy8gcm9vdFNjb3BlIGFuZCB3YXRjaCBleGNsdWRlIHRoZSBhcHBseSBhbmQgbmVlZHMgdGhlIG5leHQgY3ljbGUgdG8gcnVuXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUuJHdhdGNoKCdtc2cnLCBmdW5jdGlvbihuZXdWYWx1ZSwgb2xkVmFsdWUpIHtcclxuICAgICAgICAgICAgICAgIGlmIChuZXdWYWx1ZSAmJiBuZXdWYWx1ZSAhPT0gJycpIHtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzVGV4dCA9IG5ld1ZhbHVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzID0gJHNjb3BlLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMjUwMCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICB9XVxyXG4gICAgfTtcclxufV0pO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzE3LzIwMTUuXHJcbiAqL1xyXG5cclxuLy8gTWFpbiBBbmd1bGFySnMgY29uZmlndXJhdGlvblxyXG5yZXF1aXJlKCcuL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcbi8vIEhlbHBlcnNcclxucmVxdWlyZSgnLi9jb25maWcvQW5ndWxhclByb3ZpZGVySGVscGVyLmpzJyk7XHJcblxyXG4vLyBEaXJlY3RpdmVzXHJcbnJlcXVpcmUoJy4vZGlyZWN0aXZlcy9pbmRleCcpO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRGlhbG9nQWN0aW9uIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy50aXRsZSA9IHBhcmFtcy50aXRsZTtcclxuICAgICAgICB0aGlzLm1lc3NhZ2UgPSBwYXJhbXMubWVzc2FnZTtcclxuXHJcbiAgICB9XHJcbiAgICAvKipcclxuICAgICAqIEFjY2NlcHQgYW5kIENvbmZpcm1cclxuICAgICAqL1xyXG4gICAgY29uZmlybUFjdGlvbigpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yLzIwMTUuXHJcbiAqIEhlYWRlciBDb250cm9sbGVyIG1hbmFnZSB0aGUgdmlldyBhdmFpbGFibGUgb24gdGhlIHN0YXRlLmRhdGFcclxuICogLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gKiBIZWFkZXIgQ29udHJvbGxlclxyXG4gKiBQYWdlIHRpdGxlICAgICAgICAgICAgICAgICAgICAgIEhvbWUgLT4gTGF5b3V0IC0gU3ViIExheW91dFxyXG4gKlxyXG4gKiBNb2R1bGUgQ29udHJvbGxlclxyXG4gKiBDb250ZW50XHJcbiAqIC0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIZWFkZXJDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2dcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG5cclxuICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICBpbnN0cnVjdGlvbjogJycsXHJcbiAgICAgICAgICAgIG1lbnU6IFtdXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hlYWRlciBDb250cm9sbGVyIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmVyaWZ5IGlmIHdlIGhhdmUgYSBtZW51IHRvIHNob3cgdG8gbWFkZSBpdCBhdmFpbGFibGUgdG8gdGhlIFZpZXdcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUhlYWRlcigpIHtcclxuICAgICAgICBpZiAodGhpcy5zdGF0ZSAmJiB0aGlzLnN0YXRlLiRjdXJyZW50ICYmIHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YSkge1xyXG4gICAgICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YS5wYWdlO1xyXG4gICAgICAgICAgICBkb2N1bWVudC50aXRsZSA9IHRoaXMucGFnZU1ldGFEYXRhLnRpdGxlO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIxLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgSGVhZGVyQ29udHJvbGxlciBmcm9tICcuL0hlYWRlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgRGlhbG9nQWN0aW9uIGZyb20gJy4uL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uanMnO1xyXG5cclxudmFyIEhlYWRlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5IZWFkZXJNb2R1bGUnLCBbXSk7XHJcblxyXG5IZWFkZXJNb2R1bGUuY29udHJvbGxlcignSGVhZGVyQ29udHJvbGxlcicsIFsnJGxvZycsICckc3RhdGUnLCBIZWFkZXJDb250cm9sbGVyXSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbkhlYWRlck1vZHVsZS5jb250cm9sbGVyKCdEaWFsb2dBY3Rpb24nLCBbJyRsb2cnLCckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRGlhbG9nQWN0aW9uXSk7XHJcblxyXG4vKlxyXG4gKiBGaWx0ZXIgY2hhbmdlIHRoZSBkYXRlIGludG8gYSBwcm9wZXIgZm9ybWF0IHRpbWV6b25lIGRhdGVcclxuICovXHJcbkhlYWRlck1vZHVsZS5maWx0ZXIoJ2NvbnZlcnREYXRlSW50b1RpbWVab25lJywgWydVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgZnVuY3Rpb24gKHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UpIHtcclxuICAgIHJldHVybiAoZGF0ZVN0cmluZykgPT4gdXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlSW50b1RpbWVab25lKGRhdGVTdHJpbmcpO1xyXG59XSk7XHJcblxyXG5IZWFkZXJNb2R1bGUuZmlsdGVyKCdjb252ZXJ0RGF0ZVRpbWVJbnRvVGltZVpvbmUnLCBbJ1VzZXJQcmVmZXJlbmNlc1NlcnZpY2UnLCBmdW5jdGlvbiAodXNlclByZWZlcmVuY2VzU2VydmljZSkge1xyXG4gICAgcmV0dXJuIChkYXRlU3RyaW5nKSA9PiB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlLmdldENvbnZlcnRlZERhdGVUaW1lSW50b1RpbWVab25lKGRhdGVTdHJpbmcpO1xyXG59XSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIZWFkZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTGljZW5zZUFkbWluTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZUFkbWluTGlzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlQWRtaW5TZXJ2aWNlIGZyb20gJy4vc2VydmljZS9MaWNlbnNlQWRtaW5TZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RMaWNlbnNlIGZyb20gJy4vcmVxdWVzdC9SZXF1ZXN0TGljZW5zZS5qcyc7XHJcbmltcG9ydCBDcmVhdGVkTGljZW5zZSBmcm9tICcuL2NyZWF0ZWQvQ3JlYXRlZExpY2Vuc2UuanMnO1xyXG5pbXBvcnQgQXBwbHlMaWNlbnNlS2V5IGZyb20gJy4vYXBwbHlMaWNlbnNlS2V5L0FwcGx5TGljZW5zZUtleS5qcyc7XHJcbmltcG9ydCBNYW51YWxseVJlcXVlc3QgZnJvbSAnLi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZURldGFpbC5qcyc7XHJcblxyXG5cclxudmFyIExpY2Vuc2VBZG1pbk1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlQWRtaW5Nb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsICckbG9jYXRpb25Qcm92aWRlcicsXHJcblx0XHRmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIsICRsb2NhdGlvblByb3ZpZGVyKSB7XHJcblxyXG5cdFx0JHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdsaWNlbnNlQWRtaW4nKTtcclxuXHJcblx0XHQvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG5cdFx0dmFyIGhlYWRlciA9IHtcclxuXHRcdFx0XHR0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG5cdFx0XHRcdGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuXHRcdH07XHJcblxyXG5cdFx0JHN0YXRlUHJvdmlkZXJcclxuXHRcdFx0XHQuc3RhdGUoJ2xpY2Vuc2VBZG1pbkxpc3QnLCB7XHJcblx0XHRcdFx0XHRcdGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdBZG1pbmlzdGVyIExpY2Vuc2VzJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FkbWluJywgJ0xpY2Vuc2UnLCAnTGlzdCddfX0sXHJcblx0XHRcdFx0XHRcdHVybDogJy9saWNlbnNlL2FkbWluL2xpc3QnLFxyXG5cdFx0XHRcdFx0XHR2aWV3czoge1xyXG5cdFx0XHRcdFx0XHRcdFx0J2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG5cdFx0XHRcdFx0XHRcdFx0J2JvZHlWaWV3QCc6IHtcclxuXHRcdFx0XHRcdFx0XHRcdFx0XHR0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9saXN0L0xpY2Vuc2VBZG1pbkxpc3QuaHRtbCcsXHJcblx0XHRcdFx0XHRcdFx0XHRcdFx0Y29udHJvbGxlcjogJ0xpY2Vuc2VBZG1pbkxpc3QgYXMgbGljZW5zZUFkbWluTGlzdCdcclxuXHRcdFx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuc2VydmljZSgnTGljZW5zZUFkbWluU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCAnJHJvb3RTY29wZScsIExpY2Vuc2VBZG1pblNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlQWRtaW5MaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsIExpY2Vuc2VBZG1pbkxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ1JlcXVlc3RMaWNlbnNlJywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RMaWNlbnNlXSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdDcmVhdGVkTGljZW5zZScsIFsnJGxvZycsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBDcmVhdGVkTGljZW5zZV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignQXBwbHlMaWNlbnNlS2V5JywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBBcHBseUxpY2Vuc2VLZXldKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ01hbnVhbGx5UmVxdWVzdCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBNYW51YWxseVJlcXVlc3RdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VEZXRhaWwnLCBbJyRsb2cnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTGljZW5zZURldGFpbF0pO1xyXG5cclxuLypcclxuICogRmlsdGVyIHRvIFVSTCBFbmNvZGUgdGV4dCBmb3IgdGhlICdtYWlsdG8nXHJcbiAqL1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuZmlsdGVyKCdlc2NhcGVVUkxFbmNvZGluZycsIGZ1bmN0aW9uICgpIHtcclxuXHRyZXR1cm4gZnVuY3Rpb24gKHRleHQpIHtcclxuXHRcdGlmKHRleHQpe1xyXG5cdFx0XHR0ZXh0ID0gZW5jb2RlVVJJKHRleHQpO1xyXG5cdFx0fVxyXG5cdFx0cmV0dXJuIHRleHQ7XHJcblx0fVxyXG59KTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VBZG1pbk1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEFwcGx5TGljZW5zZUtleSBleHRlbmRzIEZvcm1WYWxpZGF0b3J7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpXHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAga2V5OiBwYXJhbXMubGljZW5zZS5rZXlcclxuICAgICAgICB9XHJcbiAgICAgICAgO1xyXG4gICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIGFwcGx5S2V5KCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5hcHBseUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0sIChkYXRhKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQ3JlYXRlZFJlcXVlc3RMaWNlbnNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5jbGllbnQgPSBwYXJhbXM7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlRGV0YWlsIHtcclxuXHJcblx0XHRjb25zdHJ1Y3RvcigkbG9nLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuXHRcdFx0XHR0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG5cdFx0XHRcdHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cdFx0XHRcdHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuXHRcdFx0XHR0aGlzLmxvZyA9ICRsb2c7XHJcblx0XHRcdFx0dGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcblx0XHRcdFx0XHRcdG1ldGhvZDoge1xyXG5cdFx0XHRcdFx0XHRcdFx0bmFtZTogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLm5hbWUsXHJcblx0XHRcdFx0XHRcdFx0XHRtYXg6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5tYXhcclxuXHRcdFx0XHRcdFx0fSxcclxuXHRcdFx0XHRcdFx0cHJvamVjdE5hbWU6IHBhcmFtcy5saWNlbnNlLnByb2plY3QubmFtZSxcclxuXHRcdFx0XHRcdFx0Y2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcblx0XHRcdFx0XHRcdGVtYWlsOiBwYXJhbXMubGljZW5zZS5lbWFpbCxcclxuXHRcdFx0XHRcdFx0dG9FbWFpbDogcGFyYW1zLmxpY2Vuc2UudG9FbWFpbCxcclxuXHRcdFx0XHRcdFx0ZW52aXJvbm1lbnQ6IHBhcmFtcy5saWNlbnNlLmVudmlyb25tZW50LFxyXG5cdFx0XHRcdFx0XHRpbmNlcHRpb246IHBhcmFtcy5saWNlbnNlLmFjdGl2YXRpb25EYXRlLFxyXG5cdFx0XHRcdFx0XHRleHBpcmF0aW9uOiBwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSxcclxuXHRcdFx0XHRcdFx0cmVxdWVzdE5vdGU6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3ROb3RlLFxyXG5cdFx0XHRcdFx0XHRhY3RpdmU6IHBhcmFtcy5saWNlbnNlLnN0YXR1cyA9PT0gJ0FDVElWRScsXHJcblx0XHRcdFx0XHRcdGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuXHRcdFx0XHRcdFx0cmVwbGFjZWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkLFxyXG5cdFx0XHRcdFx0XHRlbmNyeXB0ZWREZXRhaWw6IHBhcmFtcy5saWNlbnNlLmVuY3J5cHRlZERldGFpbCxcclxuXHRcdFx0XHRcdFx0YXBwbGllZDogZmFsc2VcclxuXHRcdFx0XHR9O1xyXG5cclxuXHRcdFx0XHR0aGlzLnByZXBhcmVNZXRob2RPcHRpb25zKCk7XHJcblx0XHR9XHJcblxyXG5cdFx0cHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcblx0XHRcdFx0dGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG5cdFx0XHRcdFx0XHR7XHJcblx0XHRcdFx0XHRcdFx0XHRuYW1lOiAnTUFYX1NFUlZFUlMnLFxyXG5cdFx0XHRcdFx0XHRcdFx0dGV4dDogJ1NlcnZlcnMnXHJcblx0XHRcdFx0XHRcdH0sXHJcblx0XHRcdFx0XHRcdHtcclxuXHRcdFx0XHRcdFx0XHRcdG5hbWU6ICdUT0tFTicsXHJcblx0XHRcdFx0XHRcdFx0XHR0ZXh0OiAnVG9rZW5zJ1xyXG5cdFx0XHRcdFx0XHR9LFxyXG5cdFx0XHRcdFx0XHR7XHJcblx0XHRcdFx0XHRcdFx0XHRuYW1lOiAnQ1VTVE9NJyxcclxuXHRcdFx0XHRcdFx0XHRcdHRleHQ6ICdDdXN0b20nXHJcblx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHRdXHJcblx0XHR9XHJcblxyXG5cdFx0LyoqXHJcblx0XHQgKiBUaGUgdXNlciBhcHBseSBhbmQgc2VydmVyIHNob3VsZCB2YWxpZGF0ZSB0aGUga2V5IGlzIGNvcnJlY3RcclxuXHRcdCAqL1xyXG5cdFx0YXBwbHlMaWNlbnNlS2V5KCkge1xyXG5cdFx0XHRcdHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuXHRcdFx0XHRcdFx0YW5pbWF0aW9uOiB0cnVlLFxyXG5cdFx0XHRcdFx0XHR0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5Lmh0bWwnLFxyXG5cdFx0XHRcdFx0XHRjb250cm9sbGVyOiAnQXBwbHlMaWNlbnNlS2V5IGFzIGFwcGx5TGljZW5zZUtleScsXHJcblx0XHRcdFx0XHRcdHNpemU6ICdtZCcsXHJcblx0XHRcdFx0XHRcdHJlc29sdmU6IHtcclxuXHRcdFx0XHRcdFx0XHRcdHBhcmFtczogKCkgPT4ge1xyXG5cdFx0XHRcdFx0XHRcdFx0XHRcdHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcblx0XHRcdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHR9KTtcclxuXHJcblx0XHRcdFx0bW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoZGF0YSkgPT4ge1xyXG5cdFx0XHRcdFx0XHR0aGlzLmxpY2Vuc2VNb2RlbC5hcHBsaWVkID0gZGF0YS5zdWNjZXNzO1xyXG5cdFx0XHRcdFx0XHRpZihkYXRhLnN1Y2Nlc3MpIHtcclxuXHRcdFx0XHRcdFx0XHRcdHRoaXMubGljZW5zZU1vZGVsLmFjdGl2ZSA9IGRhdGEuc3VjY2VzcztcclxuXHRcdFx0XHRcdFx0XHRcdHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSh7IGlkOiB0aGlzLmxpY2Vuc2VNb2RlbC5pZCwgdXBkYXRlZDogdHJ1ZX0pO1xyXG5cdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0fSk7XHJcblx0XHR9XHJcblxyXG5cdFx0LyoqXHJcblx0XHQgKiBPcGVucyBhIGRpYWxvZyBhbmQgYWxsb3cgdGhlIHVzZXIgdG8gbWFudWFsbHkgc2VuZCB0aGUgcmVxdWVzdCBvciBjb3B5IHRoZSBlbmNyaXB0ZWQgY29kZVxyXG5cdFx0ICovXHJcblx0XHRtYW51YWxseVJlcXVlc3QoKSB7XHJcblx0XHRcdFx0dmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG5cdFx0XHRcdFx0XHRhbmltYXRpb246IHRydWUsXHJcblx0XHRcdFx0XHRcdHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL21hbnVhbGx5UmVxdWVzdC9NYW51YWxseVJlcXVlc3QuaHRtbCcsXHJcblx0XHRcdFx0XHRcdGNvbnRyb2xsZXI6ICdNYW51YWxseVJlcXVlc3QgYXMgbWFudWFsbHlSZXF1ZXN0JyxcclxuXHRcdFx0XHRcdFx0c2l6ZTogJ21kJyxcclxuXHRcdFx0XHRcdFx0cmVzb2x2ZToge1xyXG5cdFx0XHRcdFx0XHRcdFx0cGFyYW1zOiAoKSA9PiB7XHJcblx0XHRcdFx0XHRcdFx0XHRcdFx0cmV0dXJuIHsgbGljZW5zZTogdGhpcy5saWNlbnNlTW9kZWwgfTtcclxuXHRcdFx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdH0pO1xyXG5cclxuXHRcdFx0XHRtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHt9KTtcclxuXHRcdH1cclxuXHJcblx0XHQvKipcclxuXHRcdCAqIElmIGJ5IHNvbWUgcmVhc29uIHRoZSBMaWNlbnNlIHdhcyBub3QgYXBwbGllZCBhdCBmaXJzdCB0aW1lLCB0aGlzIHdpbGwgZG8gYSByZXF1ZXN0IGZvciBpdFxyXG5cdFx0ICovXHJcblx0XHRyZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KCkge1xyXG5cdFx0XHRcdHRoaXMubGljZW5zZUFkbWluU2VydmljZS5yZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge30pO1xyXG5cdFx0fVxyXG5cclxuXHRcdGRlbGV0ZUxpY2Vuc2UoKSB7XHJcblx0XHRcdFx0dmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG5cdFx0XHRcdFx0XHRhbmltYXRpb246IHRydWUsXHJcblx0XHRcdFx0XHRcdHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuXHRcdFx0XHRcdFx0Y29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG5cdFx0XHRcdFx0XHRzaXplOiAnc20nLFxyXG5cdFx0XHRcdFx0XHRyZXNvbHZlOiB7XHJcblx0XHRcdFx0XHRcdFx0XHRwYXJhbXM6ICgpID0+IHtcclxuXHRcdFx0XHRcdFx0XHRcdFx0XHRyZXR1cm4geyB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsIG1lc3NhZ2U6ICdZb3UgYXJlIGFib3V0IHRvIGRlbGV0ZSB0aGUgc2VsZWN0ZWQgbGljZW5zZS4gQXJlIHlvdSBzdXJlPyBDbGljayBDb25maXJtIHRvIGRlbGV0ZSBvdGhlcndpc2UgcHJlc3MgQ2FuY2VsLid9O1xyXG5cdFx0XHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0fSk7XHJcblxyXG5cdFx0XHRcdG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG5cdFx0XHRcdFx0XHR0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZGVsZXRlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuXHRcdFx0XHRcdFx0XHRcdHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuXHRcdFx0XHRcdFx0fSk7XHJcblx0XHRcdFx0fSk7XHJcblx0XHR9XHJcblxyXG5cdFx0LyoqXHJcblx0XHQgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuXHRcdCAqL1xyXG5cdFx0Y2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcblx0XHRcdFx0aWYodGhpcy5saWNlbnNlTW9kZWwuYXBwbGllZCkge1xyXG5cdFx0XHRcdFx0XHR0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoKTtcclxuXHRcdFx0XHR9XHJcblx0XHRcdFx0dGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG5cdFx0fVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VBZG1pbkxpc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZUFkbWluTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJsaWNlbnNlQWRtaW5MaXN0Lm9uUmVxdWVzdE5ld0xpY2Vuc2UoKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBSZXF1ZXN0IE5ldyBMaWNlbnNlPC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJsaWNlbnNlQWRtaW5MaXN0LnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAyMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdsaWNlbnNlSWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIGZpbHRlcmFibGU6IGZhbHNlLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJsaWNlbnNlQWRtaW5MaXN0Lm9uTGljZW5zZURldGFpbHModGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NsaWVudC5uYW1lJywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QubmFtZScsIHRpdGxlOiAnUHJvamVjdCcsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5wcm9qZWN0ICYmIGRhdGEucHJvamVjdC5uYW1lKT8gZGF0YS5wcm9qZWN0Lm5hbWUudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2VtYWlsJywgdGl0bGU6ICdDb250YWN0IEVtYWlsJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMnLCB0aXRsZTogJ1N0YXR1cycsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5zdGF0dXMpPyBkYXRhLnN0YXR1cy50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5uYW1lJywgdGl0bGU6ICdUeXBlJywgIHRlbXBsYXRlOiAnI2lmKGRhdGEudHlwZSAmJiBkYXRhLnR5cGUubmFtZSA9PT0gXCJNVUxUSV9QUk9KRUNUXCIpeyMgR2xvYmFsICMgfSBlbHNlIHsjIFNpbmdsZSAjfSMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5uYW1lJywgdGl0bGU6ICdNZXRob2QnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEubWV0aG9kICYmIGRhdGEubWV0aG9kLm5hbWUpPyBkYXRhLm1ldGhvZC5uYW1lLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubWF4JywgdGl0bGU6ICdTZXJ2ZXIvVG9rZW5zJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3RpdmF0aW9uRGF0ZScsIHRpdGxlOiAnSW5jZXB0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JywgdGVtcGxhdGU6ICd7eyBkYXRhSXRlbS5hY3RpdmF0aW9uRGF0ZSB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZXhwaXJhdGlvbkRhdGUnLCB0aXRsZTogJ0V4cGlyYXRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nLCB0ZW1wbGF0ZTogJ3t7IGRhdGFJdGVtLmV4cGlyYXRpb25EYXRlIHwgY29udmVydERhdGVJbnRvVGltZVpvbmUgfX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbnZpcm9ubWVudCcsIHRpdGxlOiAnRW52aXJvbm1lbnQnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEuZW52aXJvbm1lbnQpPyBkYXRhLmVudmlyb25tZW50LnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAncHJvamVjdC5uYW1lJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdExpY2Vuc2VJZCAhPT0gMCAmJiB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdmFyIGxhc3RMaWNlbnNlID0gdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhLmZpbmQoKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBsaWNlbnNlLmlkID09PSB0aGlzLm9wZW5MYXN0TGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobGFzdExpY2Vuc2UpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlRGV0YWlscyhsYXN0TGljZW5zZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0TGljZW5zZSBhcyByZXF1ZXN0TGljZW5zZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdOZXcgTGljZW5zZSBDcmVhdGVkOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICAgICAgdGhpcy5vbk5ld0xpY2Vuc2VDcmVhdGVkKGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZURldGFpbHMobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09wZW4gRGV0YWlscyBmb3I6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9kZXRhaWwvTGljZW5zZURldGFpbC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VEZXRhaWwgYXMgbGljZW5zZURldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcbiAgICAgICAgICAgIGlmKGRhdGEudXBkYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IGRhdGEuaWQ7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25OZXdMaWNlbnNlQ3JlYXRlZChsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmh0bWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQ3JlYXRlZExpY2Vuc2UgYXMgY3JlYXRlZExpY2Vuc2UnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBlbWFpbDogbGljZW5zZS5lbWFpbCAgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBNYW51YWxseVJlcXVlc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnNjb3BlID0gJHNjb3BlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlRW1haWxNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6ICBwYXJhbXMubGljZW5zZS5pZFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIC8vIEdldCB0aGUgaGFzaCBjb2RlIHVzaW5nIHRoZSBpZC5cclxuICAgICAgICB0aGlzLmdldEVtYWlsQ29udGVudCgpO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICBnZXRFbWFpbENvbnRlbnQoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldEVtYWlsQ29udGVudCh0aGlzLmxpY2Vuc2VFbWFpbE1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VFbWFpbE1vZGVsID0gZGF0YTtcclxuICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKiBDcmVhdGUgYSBuZXcgUmVxdWVzdCB0byBnZXQgYSBMaWNlbnNlXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RMaWNlbnNlIGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICAvKipcclxuICAgICAqIEluaXRpYWxpemUgYWxsIHRoZSBwcm9wZXJ0aWVzXHJcbiAgICAgKiBAcGFyYW0gJGxvZ1xyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VBZG1pblNlcnZpY2VcclxuICAgICAqIEBwYXJhbSAkdWliTW9kYWxJbnN0YW5jZVxyXG4gICAgICovXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgRW52aXJvbm1lbnQgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UgPSBbXTtcclxuICAgICAgICAvLyBEZWZpbmUgdGhlIFByb2plY3QgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSBbXTtcclxuXHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldFByb2plY3REYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIENyZWF0ZSB0aGUgTW9kZWwgZm9yIHRoZSBOZXcgTGljZW5zZVxyXG4gICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBlbWFpbDogJycsXHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiAnJyxcclxuICAgICAgICAgICAgcHJvamVjdElkOiAwLFxyXG4gICAgICAgICAgICBjbGllbnROYW1lOiAnJyxcclxuICAgICAgICAgICAgcmVxdWVzdE5vdGU6ICcnXHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpPT57XHJcbiAgICAgICAgICAgIHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlID0gZGF0YTtcclxuICAgICAgICAgICAgaWYodGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgICAgIHZhciBpbmRleCA9IHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlLmZpbmRJbmRleChmdW5jdGlvbihlbnZpcm9tZW50KXtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4gZW52aXJvbWVudCAgPT09ICdQUk9EVUNUSU9OJztcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgaW5kZXggPSBpbmRleCB8fCAwO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuZW52aXJvbm1lbnQgPSBkYXRhW2luZGV4XTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIFByb2plY3QgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLnByb2plY3RJZCA9IGRhdGFbMF0uaWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZSxcclxuICAgICAgICAgICAgc2VsZWN0OiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE9uIFByb2plY3QgQ2hhbmdlLCBzZWxlY3QgdGhlIENsaWVudCBOYW1lXHJcbiAgICAgICAgICAgICAgICB2YXIgaXRlbSA9IHRoaXMuc2VsZWN0UHJvamVjdC5kYXRhSXRlbShlLml0ZW0pO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuY2xpZW50TmFtZSA9IGl0ZW0uY2xpZW50Lm5hbWU7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBnZW5lcmF0ZSBhIG5ldyBMaWNlbnNlIHJlcXVlc3RcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2VSZXF1ZXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIFJlcXVlc3RlZDogJywgdGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QodGhpcy5uZXdMaWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UodGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZUFkbWluU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcbiAgICAgICAgdGhpcy5yb290U2NvcGUgPSAkcm9vdFNjb3BlO1xyXG4gICAgICAgIHRoaXMuc3RhdHVzU3VjY2VzcyA9ICdzdWNjZXNzJztcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnbGljZW5zZUFkbWluU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEVtYWlsQ29udGVudChsaWNlbnNlSWQsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRFbWFpbENvbnRlbnQobGljZW5zZUlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBvblN1Y2Nlc3Mpe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLnJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZS5pZCwgKGRhdGEpID0+IHtcclxuXHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdSZXF1ZXN0IExpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSd9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6IGRhdGEuZGF0YX0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2Vzcyh7IHN1Y2Nlc3M6IGZhbHNlfSk7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZW1haWxSZXF1ZXN0KGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmVtYWlsUmVxdWVzdChsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnUmVxdWVzdCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiAgQXBwbHkgVGhlIExpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gb25TdWNjZXNzXHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuXHJcbiAgICAgICAgdmFyIGhhc2ggPSAge1xyXG4gICAgICAgICAgICBoYXNoOiBsaWNlbnNlLmtleVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5hcHBseUxpY2Vuc2UobGljZW5zZS5pZCwgaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3IoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKHsgc3VjY2VzczogdHJ1ZX0pO1xyXG5cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5kZWxldGVMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RJbXBvcnQgZnJvbSAnLi9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbGljZW5zZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VNYW5hZ2VyTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0xpY2Vuc2luZyBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ01hbmFnZXInLCAnTGljZW5zZScsICdMaXN0J119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvbWFuYWdlci9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJMaXN0IGFzIGxpY2Vuc2VNYW5hZ2VyTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgJyRyb290U2NvcGUnLCBMaWNlbnNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlTWFuYWdlckxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlTWFuYWdlckxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignUmVxdWVzdEltcG9ydCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RJbXBvcnRdKTtcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZU1hbmFnZXJEZXRhaWwnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlTWFuYWdlckRldGFpbF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJEZXRhaWwgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMsIHRpbWVab25lQ29uZmlndXJhdGlvbikge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVzZXJQcmVmZXJlbmNlc1NlcnZpY2UgPSB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuXHJcbiAgICAgICAgdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24gPSB0aW1lWm9uZUNvbmZpZ3VyYXRpb247XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIG93bmVyTmFtZTogcGFyYW1zLmxpY2Vuc2Uub3duZXIubmFtZSxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UucHJvamVjdC5pZCxcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLnByb2plY3QubmFtZSxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY2xpZW50SWQ6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5pZCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcbiAgICAgICAgICAgIHN0YXR1czogcGFyYW1zLmxpY2Vuc2Uuc3RhdHVzLFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5uYW1lLFxyXG4gICAgICAgICAgICAgICAgbWF4OiBwYXJhbXMubGljZW5zZS5tZXRob2QubWF4LFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudDogcGFyYW1zLmxpY2Vuc2UuZW52aXJvbm1lbnQsXHJcbiAgICAgICAgICAgIHJlcXVlc3REYXRlOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0RGF0ZSxcclxuICAgICAgICAgICAgaW5pdERhdGU6IChwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSAhPT0gbnVsbCk/IGFuZ3VsYXIuY29weShwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSkgOiAnJyxcclxuICAgICAgICAgICAgZW5kRGF0ZTogKHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlICE9PSBudWxsKT8gYW5ndWxhci5jb3B5KHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlKSA6ICcnLFxyXG4gICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0Tm90ZSxcclxuICAgICAgICAgICAgd2Vic2l0ZU5hbWU6IHBhcmFtcy5saWNlbnNlLndlYnNpdGVuYW1lLFxyXG5cclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogcGFyYW1zLmxpY2Vuc2UuYmFubmVyTWVzc2FnZSxcclxuICAgICAgICAgICAgcmVxdWVzdGVkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3RlZElkLFxyXG4gICAgICAgICAgICByZXBsYWNlZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWQsXHJcbiAgICAgICAgICAgIHJlcGxhY2VkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkSWQsXHJcbiAgICAgICAgICAgIGhvc3ROYW1lOiBwYXJhbXMubGljZW5zZS5ob3N0TmFtZSxcclxuICAgICAgICAgICAgaGFzaDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIGdyYWNlUGVyaW9kRGF5czogcGFyYW1zLmxpY2Vuc2UuZ3JhY2VQZXJpb2REYXlzLFxyXG5cclxuICAgICAgICAgICAgYXBwbGllZDogcGFyYW1zLmxpY2Vuc2UuYXBwbGllZCxcclxuICAgICAgICAgICAga2V5SWQ6IHBhcmFtcy5saWNlbnNlLmtleUlkXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlS2V5ID0gJ0xpY2Vuc2VzIGhhcyBub3QgYmVlbiBpc3N1ZWQnO1xyXG5cclxuICAgICAgICAvLyBEZWZpbmVkIHRoZSBFbnZpcm9ubWVudCBTZWxlY3RcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RFbnZpcm9ubWVudExpc3RPcHRpb25zID0gW107XHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgU3RhdHVzIFNlbGVjdCBMaXN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RTdGF0dXMgPSBbXTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCB0aGUgdHdvIEtlbmRvIERhdGVzIGZvciBJbml0IGFuZCBFbmREYXRlXHJcbiAgICAgICAgdGhpcy5pbml0RGF0ZSA9IHt9O1xyXG4gICAgICAgIHRoaXMuaW5pdERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6IHRoaXMudXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlRm9ybWF0VG9LZW5kb0RhdGUoKSxcclxuICAgICAgICAgICAgb3BlbjogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgICAgIGNoYW5nZTogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmVuZERhdGUgPSB7fTtcclxuICAgICAgICB0aGlzLmVuZERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6IHRoaXMudXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlRm9ybWF0VG9LZW5kb0RhdGUoKSxcclxuICAgICAgICAgICAgb3BlbjogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlRW5kRGF0ZSgpO1xyXG4gICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgY2hhbmdlOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuXHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZU1ldGhvZE9wdGlvbnMoKTtcclxuICAgICAgICB0aGlzLnByZXBhcmVMaWNlbnNlS2V5KCk7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQWN0aXZpdHlMaXN0KCk7XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcblxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ29udHJvbHMgd2hhdCBidXR0b25zIHRvIHNob3dcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCkge1xyXG4gICAgICAgIHRoaXMucGVuZGluZ0xpY2Vuc2UgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXMgPT09ICdQRU5ESU5HJyAmJiAhdGhpcy5lZGl0TW9kZTtcclxuICAgICAgICB0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgPSAodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnRVhQSVJFRCcgfHwgdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnVEVSTUlOQVRFRCcpO1xyXG4gICAgICAgIHRoaXMuYWN0aXZlU2hvd01vZGUgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXMgPT09ICdBQ1RJVkUnICYmICF0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgJiYgIXRoaXMuZWRpdE1vZGU7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnTUFYX1NFUlZFUlMnLFxyXG4gICAgICAgICAgICAgICAgdGV4dDogJ1NlcnZlcnMnLFxyXG4gICAgICAgICAgICAgICAgbWF4OiAwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUT0tFTicsXHJcbiAgICAgICAgICAgICAgICB0ZXh0OiAnVG9rZW5zJyxcclxuICAgICAgICAgICAgICAgIG1heDogMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnQ1VTVE9NJyxcclxuICAgICAgICAgICAgICAgIHRleHQ6ICdDdXN0b20nXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdXHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUxpY2Vuc2VLZXkoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnQUNUSVZFJykge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRLZXlDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUtleSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVBY3Rpdml0eUxpc3QoKSB7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1LFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDIwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2RhdGVDcmVhdGVkJywgdGl0bGU6ICdEYXRlJywgd2lkdGg6MTYwLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eSBoOm1tOnNzIHR0fScsIHRlbXBsYXRlOiAne3sgZGF0YUl0ZW0uZGF0ZUNyZWF0ZWQgfCBjb252ZXJ0RGF0ZVRpbWVJbnRvVGltZVpvbmUgfX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhdXRob3IucGVyc29uTmFtZScsIHRpdGxlOiAnV2hvbScsICB3aWR0aDoxNjB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2hhbmdlcycsIHRpdGxlOiAnQWN0aW9uJywgdGVtcGxhdGU6ICc8dGFibGUgY2xhc3M9XCJpbm5lci1hY3Rpdml0eV90YWJsZVwiPjx0Ym9keT48dHI+PHRkPjwvdGQ+PHRkIGNsYXNzPVwiY29sLWFjdGlvbl90ZFwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1taW51c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L3RkPjx0ZCBjbGFzcz1cImNvbC1hY3Rpb25fdGRcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L3RkPjwvdHI+I2Zvcih2YXIgaSA9IDA7IGkgPCBkYXRhLmNoYW5nZXMubGVuZ3RoOyBpKyspeyM8dHI+PHRkIHN0eWxlPVwiZm9udC13ZWlnaHQ6IGJvbGQ7XCI+Iz1kYXRhLmNoYW5nZXNbaV0uZmllbGQjIDwvdGQ+PHRkIGNsYXNzPVwiY29sLXZhbHVlX3RkXCI+PHNwYW4gY2xhc3M9XCJhY3Rpdml0eS1saXN0LW9sZC12YWxcIiBzdHlsZT1cImNvbG9yOmRhcmtyZWQ7IGZvbnQtd2VpZ2h0OiBib2xkO1wiPnt7IFxcJyM9ZGF0YS5jaGFuZ2VzW2ldLm9sZFZhbHVlI1xcJyB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19PC9zcGFuPjwvdGQ+PHRkIGNsYXNzPVwiY29sLXZhbHVlX3RkXCI+PHNwYW4gY2xhc3M9XCJhY3Rpdml0eS1saXN0LW5ldy12YWxcIiBzdHlsZT1cImNvbG9yOiBncmVlbjsgZm9udC13ZWlnaHQ6IGJvbGQ7XCI+e3sgXFwnIz1kYXRhLmNoYW5nZXNbaV0ubmV3VmFsdWUjXFwnIHwgY29udmVydERhdGVJbnRvVGltZVpvbmUgfX08L3RkPjwvdHI+I30jPC90Ym9keT48L3RhYmxlPid9LFxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRBY3Rpdml0eUxvZyh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAnZGF0ZUNyZWF0ZWQnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc2Nyb2xsYWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgYWN0aXZhdGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmFjdGl2YXRlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYgKGRhdGEpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLnN0YXR1cyA9ICdBQ1RJVkUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5wcmVwYXJlTGljZW5zZUtleSgpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZWxvYWRSZXF1aXJlZCA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byByZXZva2UgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnJldm9rZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGRlbGV0ZUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG4gICAgICAgICAgICBzaXplOiAnc20nLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsIG1lc3NhZ2U6ICdZb3UgYXJlIGFib3V0IHRvIGRlbGV0ZSB0aGUgc2VsZWN0ZWQgbGljZW5zZS4gQXJlIHlvdSBzdXJlPyBDbGljayBDb25maXJtIHRvIGRlbGV0ZSBvdGhlcndpc2UgcHJlc3MgQ2FuY2VsLid9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5kZWxldGVMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgbWFudWFsbHlSZXF1ZXN0KCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLm1hbnVhbGx5UmVxdWVzdCh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHt9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFZhbGlkYXRlIHRoZSBpbnB1dCBvbiBTZXJ2ZXIgb3IgVG9rZW5zIGlzIG9ubHkgaW50ZWdlciBvbmx5XHJcbiAgICAgKiBUaGlzIHdpbGwgYmUgY29udmVydGVkIGluIGEgbW9yZSBjb21wbGV4IGRpcmVjdGl2ZSBsYXRlclxyXG4gICAgICogVE9ETzogQ29udmVydCBpbnRvIGEgZGlyZWN0aXZlXHJcbiAgICAgKi9cclxuICAgIHZhbGlkYXRlSW50ZWdlck9ubHkoZSxtb2RlbCl7XHJcbiAgICAgICAgdHJ5IHtcclxuICAgICAgICAgICAgdmFyIG5ld1ZhbD0gcGFyc2VJbnQobW9kZWwpO1xyXG4gICAgICAgICAgICBpZighaXNOYU4obmV3VmFsKSkge1xyXG4gICAgICAgICAgICAgICAgbW9kZWwgPSBuZXdWYWw7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICBtb2RlbCA9IDA7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgaWYoZSAmJiBlLmN1cnJlbnRUYXJnZXQpIHtcclxuICAgICAgICAgICAgICAgIGUuY3VycmVudFRhcmdldC52YWx1ZSA9IG1vZGVsO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSBjYXRjaChlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuJGxvZy53YXJuKCdJbnZhbGlkIE51bWJlciBFeGNlcHRpb24nLCBtb2RlbCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZSBjdXJyZW50IGNoYW5nZXNcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5zYXZlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTGljZW5zZSBTYXZlZCcpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcbiAgICAgICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKClcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDaGFuZ2UgdGhlIHN0YXR1cyB0byBFZGl0XHJcbiAgICAgKi9cclxuICAgIG1vZGlmeUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZSA9IHRydWU7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5zZWxlY3RFbnZpcm9ubWVudExpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlmKCF0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmVudmlyb25tZW50ID0gZGF0YVswXTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB2YWx1ZVRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YSk/IGRhdGEudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEpPyBkYXRhLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+JyxcclxuICAgICAgICAgICAgdmFsdWVQcmltaXRpdmU6IHRydWVcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIG9uQ2hhbmdlSW5pdERhdGUoKSB7XHJcbiAgICAgICAgdmFyIHN0YXJ0RGF0ZSA9IHRoaXMuaW5pdERhdGUudmFsdWUoKSxcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IHRoaXMuZW5kRGF0ZS52YWx1ZSgpO1xyXG5cclxuICAgICAgICBpZiAoc3RhcnREYXRlKSB7XHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZSA9IG5ldyBEYXRlKHN0YXJ0RGF0ZSk7XHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZS5zZXREYXRlKHN0YXJ0RGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKHN0YXJ0RGF0ZSk7XHJcblxyXG4gICAgICAgICAgICBpZihlbmREYXRlKSB7XHJcbiAgICAgICAgICAgICAgICBpZih0aGlzLmluaXREYXRlLnZhbHVlKCkgPiB0aGlzLmVuZERhdGUudmFsdWUoKSkge1xyXG4gICAgICAgICAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZShlbmREYXRlKTtcclxuICAgICAgICAgICAgICAgICAgICBlbmREYXRlLnNldERhdGUoc3RhcnREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuZW5kRGF0ZSA9IGVuZERhdGU7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgb25DaGFuZ2VFbmREYXRlKCl7XHJcbiAgICAgICAgdmFyIGVuZERhdGUgPSB0aGlzLmVuZERhdGUudmFsdWUoKSxcclxuICAgICAgICAgICAgc3RhcnREYXRlID0gdGhpcy5pbml0RGF0ZS52YWx1ZSgpO1xyXG5cclxuICAgICAgICBpZiAoZW5kRGF0ZSkge1xyXG4gICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgIGVuZERhdGUuc2V0RGF0ZShlbmREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgfSBlbHNlIGlmIChzdGFydERhdGUpIHtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihuZXcgRGF0ZShzdGFydERhdGUpKTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoKTtcclxuICAgICAgICAgICAgdGhpcy5pbml0RGF0ZS5tYXgoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4oZW5kRGF0ZSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIGlmKHRoaXMuZWRpdE1vZGUpIHtcclxuICAgICAgICAgICAgdGhpcy5yZXNldEZvcm0oKCk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uUmVzZXRGb3JtKCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSBpZih0aGlzLnJlbG9hZFJlcXVpcmVkKXtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKHt9KTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGVwZW5kaW5nIHRoZSBudW1iZXIgb2YgZmllbGRzIGFuZCB0eXBlIG9mIGZpZWxkLCB0aGUgcmVzZXQgY2FuJ3QgYmUgb24gdGhlIEZvcm1WYWxpZG9yLCBhdCBsZWFzdCBub3Qgbm93XHJcbiAgICAgKi9cclxuICAgIG9uUmVzZXRGb3JtKCkge1xyXG4gICAgICAgIHRoaXMucmVzZXREcm9wRG93bih0aGlzLnNlbGVjdEVudmlyb25tZW50LCB0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudCk7XHJcbiAgICAgICAgdGhpcy5vbkNoYW5nZUluaXREYXRlKCk7XHJcbiAgICAgICAgdGhpcy5vbkNoYW5nZUVuZERhdGUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBNYW51YWwgcmVsb2FkIGFmdGVyIGEgY2hhbmdlIGhhcyBiZWVuIHBlcmZvcm1lZCB0byB0aGUgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICByZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5hY3Rpdml0eUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLmFjdGl2aXR5R3JpZC5kYXRhU291cmNlLnJlYWQoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJMaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UgPSBsaWNlbnNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgLy90aGlzLmdldExpY2Vuc2VMaXN0KCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VNYW5hZ2VyTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSAwO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3Qub25SZXF1ZXN0SW1wb3J0TGljZW5zZSgpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IEltcG9ydCBMaWNlbnNlIFJlcXVlc3Q8L2J1dHRvbj4gPGRpdiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAyMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdpZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgZmlsdGVyYWJsZTogZmFsc2UsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5vbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyh0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnb3duZXIubmFtZScsIHRpdGxlOiAnT3duZXInfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3dlYnNpdGVuYW1lJywgdGl0bGU6ICdXZWJzaXRlIE5hbWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NsaWVudC5uYW1lJywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QubmFtZScsIHRpdGxlOiAnUHJvamVjdCcsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5wcm9qZWN0ICYmIGRhdGEucHJvamVjdC5uYW1lKT8gZGF0YS5wcm9qZWN0Lm5hbWUudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2VtYWlsJywgdGl0bGU6ICdDb250YWN0IEVtYWlsJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMnLCB0aXRsZTogJ1N0YXR1cycsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5zdGF0dXMpPyBkYXRhLnN0YXR1cy50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5uYW1lJywgdGl0bGU6ICdUeXBlJywgIHRlbXBsYXRlOiAnI2lmKGRhdGEudHlwZSAmJiBkYXRhLnR5cGUubmFtZSA9PT0gXCJNVUxUSV9QUk9KRUNUXCIpeyMgR2xvYmFsICMgfSBlbHNlIHsjIFNpbmdsZSAjfSMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5uYW1lJywgdGl0bGU6ICdNZXRob2QnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEubWV0aG9kICYmIGRhdGEubWV0aG9kLm5hbWUpPyBkYXRhLm1ldGhvZC5uYW1lLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubWF4JywgdGl0bGU6ICdTZXJ2ZXIvVG9rZW5zJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3RpdmF0aW9uRGF0ZScsIHRpdGxlOiAnSW5jZXB0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JywgdGVtcGxhdGU6ICd7eyBkYXRhSXRlbS5hY3RpdmF0aW9uRGF0ZSB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZXhwaXJhdGlvbkRhdGUnLCB0aXRsZTogJ0V4cGlyYXRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nLCB0ZW1wbGF0ZTogJ3t7IGRhdGFJdGVtLmV4cGlyYXRpb25EYXRlIHwgY29udmVydERhdGVJbnRvVGltZVpvbmUgfX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbnZpcm9ubWVudCcsIHRpdGxlOiAnRW52aXJvbm1lbnQnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEuZW52aXJvbm1lbnQpPyBkYXRhLmVudmlyb25tZW50LnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6J2dyYWNlUGVyaW9kRGF5cycsIGhpZGRlbjogdHJ1ZX1cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHNvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICBmaWVsZDogJ3Byb2plY3QubmFtZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGNoYW5nZTogIChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgLy8gV2UgYXJlIGNvbWluZyBmcm9tIGEgbmV3IGltcG9ydGVkIHJlcXVlc3QgbGljZW5zZVxyXG4gICAgICAgICAgICAgICAgICAgIGlmKHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCAhPT0gMCAmJiB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdmFyIG5ld0xpY2Vuc2VDcmVhdGVkID0gdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhLmZpbmQoKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBsaWNlbnNlLmlkID09PSB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gMDtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGlmKG5ld0xpY2Vuc2VDcmVhdGVkKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKG5ld0xpY2Vuc2VDcmVhdGVkKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc29ydGFibGU6IHRydWUsXHJcbiAgICAgICAgICAgIGZpbHRlcmFibGU6IHtcclxuICAgICAgICAgICAgICAgIGV4dHJhOiBmYWxzZVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRoZSB1c2VyIEltcG9ydCBhIG5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdEltcG9ydExpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvcmVxdWVzdEltcG9ydC9SZXF1ZXN0SW1wb3J0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnUmVxdWVzdEltcG9ydCBhcyByZXF1ZXN0SW1wb3J0JyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJ1xyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChsaWNlbnNlSW1wb3J0ZWQpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gbGljZW5zZUltcG9ydGVkLmlkOyAvLyB0YWtlIHRoaXMgcGFyYW0gZnJvbSB0aGUgbGFzdCBpbXBvcnRlZCBsaWNlbnNlLCBvZiBjb3Vyc2VcclxuICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEFmdGVyIGNsaWNraW5nIG9uIGVkaXQsIHdlIHJlZGlyZWN0IHRoZSB1c2VyIHRvIHRoZSBFZGl0aW9uIHNjcmVlbiBpbnN0ZWFkIG9mIG9wZW4gYSBkaWFsb2dcclxuICAgICAqIGR1IHRoZSBzaXplIG9mIHRoZSBpbnB1dHNcclxuICAgICAqL1xyXG4gICAgb25MaWNlbnNlTWFuYWdlckRldGFpbHMobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09wZW4gRGV0YWlscyBmb3I6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2RldGFpbC9MaWNlbnNlTWFuYWdlckRldGFpbC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VNYW5hZ2VyRGV0YWlsIGFzIGxpY2Vuc2VNYW5hZ2VyRGV0YWlsJyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgdmFyIGRhdGFJdGVtID0ge307XHJcbiAgICAgICAgICAgICAgICAgICAgaWYobGljZW5zZSAmJiBsaWNlbnNlLmRhdGFJdGVtKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGRhdGFJdGVtID0gbGljZW5zZS5kYXRhSXRlbTtcclxuICAgICAgICAgICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2U7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IGRhdGFJdGVtIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdSZXF1ZXN0IENhbmNlbGVkLicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICByZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RJbXBvcnQgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKSB7XHJcbiAgICAgICAgc3VwZXIoJGxvZywgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UgPSBsaWNlbnNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGhhc2g6ICcnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIGFuZCB2YWxpZGF0ZSB0aGUgS2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgb25JbXBvcnRMaWNlbnNlKCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmltcG9ydExpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChsaWNlbnNlSW1wb3J0ZWQpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShsaWNlbnNlSW1wb3J0ZWQuZGF0YSk7XHJcbiAgICAgICAgICAgIH0sIChsaWNlbnNlSW1wb3J0ZWQpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGxpY2Vuc2VJbXBvcnRlZC5kYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcbiAgICAgICAgdGhpcy5yb290U2NvcGUgPSAkcm9vdFNjb3BlO1xyXG4gICAgICAgIHRoaXMuc3RhdHVzU3VjY2VzcyA9ICdzdWNjZXNzJztcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnbGljZW5zZU1hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldExpY2Vuc2VMaXN0KG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldFByb2plY3REYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2Uob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRLZXlDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0S2V5Q29kZShsaWNlbnNlSWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24ob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5jb21tb25TZXJ2aWNlSGFuZGxlcigpLmdldFRpbWVab25lQ29uZmlndXJhdGlvbigoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgbWFudWFsbHlSZXF1ZXN0KGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLm1hbnVhbGx5UmVxdWVzdChsaWNlbnNlLmlkLCAoZGF0YSkgPT4ge1xyXG5cclxuICAgICAgICAgICAgaWYgKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdFbWFpbCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHknIH0pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogZGF0YS5kYXRhIH0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2Vzcyh7IHN1Y2Nlc3M6IGZhbHNlIH0pO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZSB0aGUgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBzYXZlTGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuXHJcbiAgICAgICAgdmFyIGxpY2Vuc2VNb2RpZmllZCA9IHtcclxuICAgICAgICAgICAgZW52aXJvbm1lbnQ6IGxpY2Vuc2UuZW52aXJvbm1lbnQsXHJcbiAgICAgICAgICAgIG1ldGhvZDoge1xyXG4gICAgICAgICAgICAgICAgbmFtZTogbGljZW5zZS5tZXRob2QubmFtZVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBhY3RpdmF0aW9uRGF0ZTogbGljZW5zZS5pbml0RGF0ZSA/IG1vbWVudChsaWNlbnNlLmluaXREYXRlKS5mb3JtYXQoJ1lZWVktTU0tREQnKSA6ICcnLFxyXG4gICAgICAgICAgICBleHBpcmF0aW9uRGF0ZTogbGljZW5zZS5lbmREYXRlID8gbW9tZW50KGxpY2Vuc2UuZW5kRGF0ZSkuZm9ybWF0KCdZWVlZLU1NLUREJykgOiAnJyxcclxuICAgICAgICAgICAgc3RhdHVzOiBsaWNlbnNlLnN0YXR1cyxcclxuICAgICAgICAgICAgcHJvamVjdDoge1xyXG4gICAgICAgICAgICAgICAgaWQ6IChsaWNlbnNlLnByb2plY3QuaWQgIT09ICdhbGwnKSA/IHBhcnNlSW50KGxpY2Vuc2UucHJvamVjdC5pZCkgOiBsaWNlbnNlLnByb2plY3QuaWQsICAvLyBXZSBwYXNzICdhbGwnIHdoZW4gaXMgbXVsdGlwcm9qZWN0XHJcbiAgICAgICAgICAgICAgICBuYW1lOiBsaWNlbnNlLnByb2plY3QubmFtZVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBiYW5uZXJNZXNzYWdlOiBsaWNlbnNlLmJhbm5lck1lc3NhZ2UsXHJcbiAgICAgICAgICAgIGdyYWNlUGVyaW9kRGF5czogbGljZW5zZS5ncmFjZVBlcmlvZERheXMsXHJcbiAgICAgICAgICAgIHdlYnNpdGVuYW1lOiBsaWNlbnNlLndlYnNpdGVOYW1lLFxyXG4gICAgICAgICAgICBob3N0TmFtZTogbGljZW5zZS5ob3N0TmFtZVxyXG4gICAgICAgIH07XHJcbiAgICAgICAgaWYgKGxpY2Vuc2UubWV0aG9kLm5hbWUgIT09ICdDVVNUT00nKSB7XHJcbiAgICAgICAgICAgIGxpY2Vuc2VNb2RpZmllZC5tZXRob2QubWF4ID0gcGFyc2VJbnQobGljZW5zZS5tZXRob2QubWF4KTtcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnNhdmVMaWNlbnNlKGxpY2Vuc2UuaWQsIGxpY2Vuc2VNb2RpZmllZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuICAgIC8qKlxyXG4gICAgICogRG9lcyB0aGUgYWN0aXZhdGlvbiBvZiB0aGUgY3VycmVudCBsaWNlbnNlIGlmIHRoaXMgaXMgbm90IGFjdGl2ZVxyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBhY3RpdmF0ZUxpY2Vuc2UobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5hY3RpdmF0ZUxpY2Vuc2UobGljZW5zZS5pZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYgKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywge1xyXG4gICAgICAgICAgICAgICAgICAgIHR5cGU6ICdpbmZvJyxcclxuICAgICAgICAgICAgICAgICAgICB0ZXh0OiAnVGhlIGxpY2Vuc2Ugd2FzIGFjdGl2YXRlZCBhbmQgdGhlIGxpY2Vuc2Ugd2FzIGVtYWlsZWQuJ1xyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHtcclxuICAgICAgICAgICAgICAgICAgICB0eXBlOiAnd2FybmluZycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogZGF0YS5kYXRhXHJcbiAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBjYWxsYmFjaygpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBNYWtlIHRoZSByZXF1ZXN0IHRvIEltcG9ydCB0aGUgbGljZW5zZSwgaWYgZmFpbHMsIHRocm93cyBhbiBleGNlcHRpb24gdmlzaWJsZSBmb3IgdGhlIHVzZXIgdG8gdGFrZSBhY3Rpb25cclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgaW1wb3J0TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB2YXIgaGFzaCA9IHtcclxuICAgICAgICAgICAgZGF0YTogbGljZW5zZS5oYXNoXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkucmVxdWVzdEltcG9ydChoYXNoLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICBpZiAoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBJbXBvcnRlZCcgfSk7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ3dhcm5pbmcnLCB0ZXh0OiAnTGljZW5zZSB3YXMgbm90IGFwcGxpZWQuIFJldmlldyB0aGUgcHJvdmlkZWQgTGljZW5zZSBLZXkgaXMgY29ycmVjdC4nIH0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3IoeyBzdWNjZXNzOiBmYWxzZSB9KTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJldm9rZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkucmV2b2tlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGRlbGV0ZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZGVsZXRlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEFjdGl2aXR5TG9nKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEFjdGl2aXR5TG9nKGxpY2Vuc2UuaWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0KG5ld0xpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTm90aWNlTGlzdCBmcm9tICcuL2xpc3QvTm90aWNlTGlzdC5qcyc7XHJcbmltcG9ydCBOb3RpY2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTm90aWNlTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgRWRpdE5vdGljZSBmcm9tICcuL2VkaXQvRWRpdE5vdGljZS5qcyc7XHJcblxyXG52YXIgTm90aWNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5Ob3RpY2VNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdub3RpY2VNYW5hZ2VyJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdub3RpY2VMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTm90aWNlIEFkbWluaXN0cmF0aW9uJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FkbWluJywgJ05vdGljZScsICdMaXN0J119fSxcclxuICAgICAgICAgICAgdXJsOiAnL25vdGljZS9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2xpc3QvTm90aWNlTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTm90aWNlTGlzdCBhcyBub3RpY2VMaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuc2VydmljZSgnTm90aWNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgTm90aWNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTm90aWNlTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTm90aWNlTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0VkaXROb3RpY2UnLCBbJyRsb2cnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIEVkaXROb3RpY2VdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IE5vdGljZU1hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEVkaXROb3RpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIG5vdGljZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlID0gbm90aWNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uID0gcGFyYW1zLmFjdGlvbjtcclxuICAgICAgICB0aGlzLmFjdGlvblR5cGUgPSBwYXJhbXMuYWN0aW9uVHlwZTtcclxuXHJcbiAgICAgICAgdGhpcy5rZW5kb0VkaXRvclRvb2xzID0gW1xyXG4gICAgICAgICAgICAnZm9ybWF0dGluZycsICdjbGVhbkZvcm1hdHRpbmcnLFxyXG4gICAgICAgICAgICAnZm9udE5hbWUnLCAnZm9udFNpemUnLFxyXG4gICAgICAgICAgICAnanVzdGlmeUxlZnQnLCAnanVzdGlmeUNlbnRlcicsICdqdXN0aWZ5UmlnaHQnLCAnanVzdGlmeUZ1bGwnLFxyXG4gICAgICAgICAgICAnYm9sZCcsXHJcbiAgICAgICAgICAgICdpdGFsaWMnLFxyXG4gICAgICAgICAgICAndmlld0h0bWwnXHJcbiAgICAgICAgXTtcclxuXHJcbiAgICAgICAgLy8gQ1NTIGhhcyBub3QgY2FuY2VsaW5nIGF0dHJpYnV0ZXMsIHNvIGluc3RlYWQgb2YgcmVtb3ZpbmcgZXZlcnkgcG9zc2libGUgSFRNTCwgd2UgbWFrZSBlZGl0b3IgaGFzIHNhbWUgY3NzXHJcbiAgICAgICAgdGhpcy5rZW5kb1N0eWxlc2hlZXRzID0gW1xyXG4gICAgICAgICAgICAnLi4vc3RhdGljL2Rpc3QvanMvdmVuZG9ycy9ib290c3RyYXAvZGlzdC9jc3MvYm9vdHN0cmFwLm1pbi5jc3MnLCAvLyBPdXJ0IGN1cnJlbnQgQm9vdHN0cmFwIGNzc1xyXG4gICAgICAgICAgICAnLi4vc3RhdGljL2Rpc3QvY3NzL1REU1RNTGF5b3V0Lm1pbi5jc3MnIC8vIE9yaWdpbmFsIFRlbXBsYXRlIENTU1xyXG5cclxuICAgICAgICBdO1xyXG5cclxuICAgICAgICB0aGlzLmdldFR5cGVEYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZWwgPSB7XHJcbiAgICAgICAgICAgIHRpdGxlOiAnJyxcclxuICAgICAgICAgICAgdHlwZUlkOiAwLFxyXG4gICAgICAgICAgICBhY3RpdmU6IGZhbHNlLFxyXG4gICAgICAgICAgICBodG1sVGV4dDogJycsXHJcbiAgICAgICAgICAgIHJhd1RleHQ6ICcnXHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBPbiBFZGl0aW9uIE1vZGUgd2UgY2MgdGhlIG1vZGVsIGFuZCBvbmx5IHRoZSBwYXJhbXMgd2UgbmVlZFxyXG4gICAgICAgIGlmKHBhcmFtcy5ub3RpY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuaWQgPSBwYXJhbXMubm90aWNlLmlkO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC50aXRsZSA9IHBhcmFtcy5ub3RpY2UudGl0bGU7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLnR5cGVJZCA9IHBhcmFtcy5ub3RpY2UudHlwZS5pZDtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuYWN0aXZlID0gcGFyYW1zLm5vdGljZS5hY3RpdmU7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLmh0bWxUZXh0ID0gcGFyYW1zLm5vdGljZS5odG1sVGV4dDtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFR5cGVEYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMudHlwZURhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDEsIG5hbWU6ICdQcmVsb2dpbid9LFxyXG4gICAgICAgICAgICB7dHlwZUlkOiAyLCBuYW1lOiAnUG9zdGxvZ2luJ31cclxuICAgICAgICAgICAgLy97dHlwZUlkOiAzLCBuYW1lOiAnR2VuZXJhbCd9IERpc2FibGVkIHVudGlsIFBoYXNlIElJXHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBDcmVhdGUvRWRpdCBhIG5vdGljZVxyXG4gICAgICovXHJcbiAgICBzYXZlTm90aWNlKCkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8odGhpcy5hY3Rpb24gKyAnIE5vdGljZSBSZXF1ZXN0ZWQ6ICcsIHRoaXMuZWRpdE1vZGVsKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbC5yYXdUZXh0ID0gJCgnI2tlbmRvLWVkaXRvci1jcmVhdGUtZWRpdCcpLnRleHQoKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbC50eXBlSWQgPSBwYXJzZUludCh0aGlzLmVkaXRNb2RlbC50eXBlSWQpO1xyXG4gICAgICAgIGlmKHRoaXMuYWN0aW9uID09PSB0aGlzLmFjdGlvblR5cGUuTkVXKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuY3JlYXRlTm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2UgaWYodGhpcy5hY3Rpb24gPT09IHRoaXMuYWN0aW9uVHlwZS5FRElUKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZWRpdE5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGRlbGV0ZU5vdGljZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byBkZWxldGUgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZGVsZXRlTm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBub3RpY2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHtcclxuICAgICAgICAgICAgTkVXOiAnTmV3JyxcclxuICAgICAgICAgICAgRURJVDogJ0VkaXQnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZUxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLm5vdGljZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuTkVXKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBDcmVhdGUgTmV3IE5vdGljZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibm90aWNlTGlzdC5yZWxvYWROb3RpY2VMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2h0bWxUZXh0JywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuRURJVCwgdGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RpdGxlJywgdGl0bGU6ICdUaXRsZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5pZCcsIGhpZGRlbjogdHJ1ZX0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2ZScsIHRpdGxlOiAnQWN0aXZlJywgdGVtcGxhdGU6ICcjaWYoYWN0aXZlKSB7IyBZZXMgI30gZWxzZSB7IyBObyAjfSMnIH1cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICd0aXRsZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBOb3RpY2VcclxuICAgICAqL1xyXG4gICAgb25FZGl0Q3JlYXRlTm90aWNlKGFjdGlvbiwgbm90aWNlKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9lZGl0L0VkaXROb3RpY2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdFZGl0Tm90aWNlIGFzIGVkaXROb3RpY2UnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSBub3RpY2UgJiYgbm90aWNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGFjdGlvbjogYWN0aW9uLCBub3RpY2U6IGRhdGFJdGVtLCBhY3Rpb25UeXBlOiB0aGlzLmFjdGlvblR5cGV9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKG5vdGljZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgTm90aWNlOiAnLCBub3RpY2UpO1xyXG4gICAgICAgICAgICAvLyBBZnRlciBhIG5ldyB2YWx1ZSBpcyBhZGRlZCwgbGV0cyB0byByZWZyZXNoIHRoZSBHcmlkXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTm90aWNlTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbyhhY3Rpb24gKyAnIFJlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVsb2FkTm90aWNlTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLlRZUEUgPSB7XHJcbiAgICAgICAgICAgICcxJzogJ1ByZWxvZ2luJyxcclxuICAgICAgICAgICAgJzInOiAnUG9zdGxvZ2luJyxcclxuICAgICAgICAgICAgJzMnOiAnR2VuZXJhbCdcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTm90aWNlTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0Tm90aWNlTGlzdChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0Tm90aWNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB2YXIgbm90aWNlTGlzdCA9IFtdO1xyXG4gICAgICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICAgICAgLy8gVmVyaWZ5IHRoZSBMaXN0IHJldHVybnMgd2hhdCB3ZSBleHBlY3QgYW5kIHdlIGNvbnZlcnQgaXQgdG8gYW4gQXJyYXkgdmFsdWVcclxuICAgICAgICAgICAgICAgIGlmKGRhdGEgJiYgZGF0YS5ub3RpY2VzKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgbm90aWNlTGlzdCA9IGRhdGEubm90aWNlcztcclxuICAgICAgICAgICAgICAgICAgICBpZiAobm90aWNlTGlzdCAmJiBub3RpY2VMaXN0Lmxlbmd0aCA+IDApIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBub3RpY2VMaXN0Lmxlbmd0aDsgaSA9IGkgKyAxKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBub3RpY2VMaXN0W2ldLnR5cGUgPSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IG5vdGljZUxpc3RbaV0udHlwZUlkLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6IHRoaXMuVFlQRVtub3RpY2VMaXN0W2ldLnR5cGVJZF1cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBkZWxldGUgbm90aWNlTGlzdFtpXS50eXBlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5sb2cuZXJyb3IoJ0Vycm9yIHBhcnNpbmcgdGhlIE5vdGljZSBMaXN0JywgZSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKG5vdGljZUxpc3QpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IE5vdGljZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGNyZWF0ZU5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE5vdGljZSBzaG91bGQgaGF2ZSB0aGUgSUQgaW4gb3JkZXIgdG8gZWRpdCB0aGUgTm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZWRpdE5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmVkaXROb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBOb3RpY2Ugc2hvdWxkIGhhdmUgdGhlIElEIGluIG9yZGVyIHRvIGRlbGV0ZSB0aGUgbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZGVsZXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmRlbGV0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IFRhc2tNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvVGFza01hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyQ29udHJvbGxlciBmcm9tICcuL2xpc3QvVGFza01hbmFnZXJDb250cm9sbGVyLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyRWRpdCBmcm9tICcuL2VkaXQvVGFza01hbmFnZXJFZGl0LmpzJztcclxuXHJcbnZhciBUYXNrTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5UYXNrTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgJ2Zvcm1seUNvbmZpZ1Byb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgZm9ybWx5Q29uZmlnUHJvdmlkZXIpIHtcclxuXHJcbiAgICBmb3JtbHlDb25maWdQcm92aWRlci5zZXRUeXBlKHtcclxuICAgICAgICBuYW1lOiAnY3VzdG9tJyxcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJ2N1c3RvbS5odG1sJ1xyXG4gICAgfSk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCd0YXNrTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ015IFRhc2sgTWFuYWdlcicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydUYXNrIE1hbmFnZXInXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvdGFzay9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9saXN0L1Rhc2tNYW5hZ2VyQ29udGFpbmVyLmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdUYXNrTWFuYWdlckNvbnRyb2xsZXIgYXMgdGFza01hbmFnZXInXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuVGFza01hbmFnZXJNb2R1bGUuc2VydmljZSgndGFza01hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFRhc2tNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJDb250cm9sbGVyJywgWyckbG9nJywgJ3Rhc2tNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBUYXNrTWFuYWdlckNvbnRyb2xsZXJdKTtcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJFZGl0JywgWyckbG9nJywgVGFza01hbmFnZXJFZGl0XSk7XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgVGFza01hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAzLzExLzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJFZGl0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCB0YXNrTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG5cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIwLzIwMTUuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlckNvbnRyb2xsZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHRhc2tNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5tb2R1bGUgPSAnVGFza01hbmFnZXInO1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlID0gdGFza01hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudGFza0dyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5ldmVudERhdGFTb3VyY2UgPSBbXTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCBDbGFzc1xyXG4gICAgICAgIHRoaXMuZ2V0RXZlbnREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1Rhc2tNYW5hZ2VyIENvbnRyb2xsZXIgSW5zdGFuY2VkJyk7XHJcbiAgICAgICAgdGhpcy5pbml0Rm9ybSgpO1xyXG5cclxuICAgIH1cclxuXHJcbiAgICBvcGVuTW9kYWxEZW1vKCkge1xyXG5cclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICdhcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9lZGl0L1Rhc2tNYW5hZ2VyRWRpdC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyRWRpdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIGl0ZW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIFsnMScsJ2EyJywnZ2cnXTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChzZWxlY3RlZEl0ZW0pID0+IHtcclxuICAgICAgICAgICAgdGhpcy5kZWJ1ZyhzZWxlY3RlZEl0ZW0pO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTW9kYWwgZGlzbWlzc2VkIGF0OiAnICsgbmV3IERhdGUoKSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnRhc2tHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZ3JvdXBhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbe2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Rhc2snLCB0aXRsZTogJ1Rhc2snfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Rlc2NyaXB0aW9uJywgdGl0bGU6ICdEZXNjcmlwdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzZXROYW1lJywgdGl0bGU6ICdBc3NldCBOYW1lJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldFR5cGUnLCB0aXRsZTogJ0Fzc2V0IFR5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3VwZGF0ZWQnLCB0aXRsZTogJ1VwZGF0ZWQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2R1ZScsIHRpdGxlOiAnRHVlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMnLCB0aXRsZTogJ1N0YXR1cyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzaWduZWRUbycsIHRpdGxlOiAnQXNzaWduZWQgVG8nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RlYW0nLCB0aXRsZTogJ1RlYW0nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NhdGVnb3J5JywgdGl0bGU6ICdDYXRlZ29yeSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3VjJywgdGl0bGU6ICdTdWMuJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzY29yZScsIHRpdGxlOiAnU2NvcmUnfV0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIC8qdGhpcy50YXNrTWFuYWdlclNlcnZpY2UudGVzdFNlcnZpY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7Ki9cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGdldEV2ZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmV2ZW50RGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDEsIGV2ZW50TmFtZTogJ0FsbCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMiwgZXZlbnROYW1lOiAnQnVpbGRvdXQnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDMsIGV2ZW50TmFtZTogJ0RSLUVQJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiA0LCBldmVudE5hbWU6ICdNMS1QaHlzaWNhbCd9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICBvbkVycm9ySGFwcGVucygpIHtcclxuICAgICAgICB0aGlzLnRhc2tNYW5hZ2VyU2VydmljZS5mYWlsQ2FsbChmdW5jdGlvbiAoKSB7XHJcblxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGluaXRGb3JtKCkge1xyXG4gICAgICAgIHRoaXMudXNlckZpZWxkcyA9IFtcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAga2V5OiAnZW1haWwnLFxyXG4gICAgICAgICAgICAgICAgdHlwZTogJ2lucHV0JyxcclxuICAgICAgICAgICAgICAgIHRlbXBsYXRlT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgICAgIHR5cGU6ICdlbWFpbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdFbWFpbCBhZGRyZXNzJyxcclxuICAgICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcjogJ0VudGVyIGVtYWlsJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdwYXNzd29yZCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnaW5wdXQnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ3Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ1Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcjogJ1Bhc3N3b3JkJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdmaWxlJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdmaWxlJyxcclxuICAgICAgICAgICAgICAgIHRlbXBsYXRlT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgICAgIGxhYmVsOiAnRmlsZSBpbnB1dCcsXHJcbiAgICAgICAgICAgICAgICAgICAgZGVzY3JpcHRpb246ICdFeGFtcGxlIGJsb2NrLWxldmVsIGhlbHAgdGV4dCBoZXJlJyxcclxuICAgICAgICAgICAgICAgICAgICB1cmw6ICdodHRwczovL2V4YW1wbGUuY29tL3VwbG9hZCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAga2V5OiAnY2hlY2tlZCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnY2hlY2tib3gnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdDaGVjayBtZSBvdXQnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAyMi8wNy8xNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIFJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gUmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVGFza01hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGZhaWxDYWxsKGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5SZXNvdXJjZVNlcnZpY2VIYW5kbGVyKCkuZ2V0U1ZHKCk7XHJcbiAgICB9XHJcblxyXG4gICAgdGVzdFNlcnZpY2UoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlRhc2tTZXJ2aWNlSGFuZGxlcigpLmdldEZlZWRzKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8zLzIwMTYuXHJcbiAqL1xyXG5cclxuXHJcbid1c2Ugc3RyaWN0J1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRm9ybVZhbGlkYXRvciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc2NvcGUgPSAkc2NvcGU7XHJcblxyXG4gICAgICAgIC8vIEpTIGRvZXMgYSBhcmd1bWVudCBwYXNzIGJ5IHJlZmVyZW5jZVxyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IG51bGw7XHJcbiAgICAgICAgLy8gQSBjb3B5IHdpdGhvdXQgcmVmZXJlbmNlIGZyb20gdGhlIG9yaWdpbmFsIG9iamVjdFxyXG4gICAgICAgIHRoaXMub3JpZ2luYWxEYXRhID0gbnVsbDtcclxuICAgICAgICAvLyBBIENDIGFzIEpTT04gZm9yIGNvbXBhcmlzb24gUHVycG9zZVxyXG4gICAgICAgIHRoaXMub2JqZWN0QXNKU09OID0gbnVsbDtcclxuXHJcblxyXG4gICAgICAgIC8vIE9ubHkgZm9yIE1vZGFsIFdpbmRvd3NcclxuICAgICAgICB0aGlzLnJlbG9hZFJlcXVpcmVkID0gZmFsc2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuXHJcbiAgICAgICAgaWYgKCRzY29wZS4kb24pIHtcclxuICAgICAgICAgICAgJHNjb3BlLiRvbignbW9kYWwuY2xvc2luZycsIChldmVudCwgcmVhc29uLCBjbG9zZWQpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNsb3NlRGlhbG9nKGV2ZW50LCByZWFzb24sIGNsb3NlZClcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZXMgdGhlIEZvcm0gaW4gMyBpbnN0YW5jZXMsIG9uZSB0byBrZWVwIHRyYWNrIG9mIHRoZSBvcmlnaW5hbCBkYXRhLCBvdGhlciBpcyB0aGUgY3VycmVudCBvYmplY3QgYW5kXHJcbiAgICAgKiBhIEpTT04gZm9ybWF0IGZvciBjb21wYXJpc29uIHB1cnBvc2VcclxuICAgICAqIEBwYXJhbSBuZXdPYmplY3RJbnN0YW5jZVxyXG4gICAgICovXHJcbiAgICBzYXZlRm9ybShuZXdPYmplY3RJbnN0YW5jZSkge1xyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IG5ld09iamVjdEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMub3JpZ2luYWxEYXRhID0gYW5ndWxhci5jb3B5KG5ld09iamVjdEluc3RhbmNlLCB0aGlzLm9yaWdpbmFsRGF0YSk7XHJcbiAgICAgICAgdGhpcy5vYmplY3RBc0pTT04gPSBhbmd1bGFyLnRvSnNvbihuZXdPYmplY3RJbnN0YW5jZSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBHZXQgdGhlIEN1cnJlbnQgT2JqZWN0IG9uIGhpcyByZWZlcmVuY2UgRm9ybWF0XHJcbiAgICAgKiBAcmV0dXJucyB7bnVsbHwqfVxyXG4gICAgICovXHJcbiAgICBnZXRGb3JtKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmN1cnJlbnRPYmplY3Q7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBHZXQgdGhlIE9iamVjdCBhcyBKU09OIGZyb20gdGhlIE9yaWdpbmFsIERhdGFcclxuICAgICAqIEByZXR1cm5zIHtudWxsfHN0cmluZ3x1bmRlZmluZWR8c3RyaW5nfCp9XHJcbiAgICAgKi9cclxuICAgIGdldEZvcm1Bc0pTT04oKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMub2JqZWN0QXNKU09OO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICpcclxuICAgICAqIEBwYXJhbSBvYmpldFRvUmVzZXQgb2JqZWN0IHRvIHJlc2V0XHJcbiAgICAgKiBAcGFyYW0gb25SZXNldEZvcm0gY2FsbGJhY2tcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICByZXNldEZvcm0ob25SZXNldEZvcm0pIHtcclxuICAgICAgICB0aGlzLmN1cnJlbnRPYmplY3QgPSBhbmd1bGFyLmNvcHkodGhpcy5vcmlnaW5hbERhdGEsIHRoaXMuY3VycmVudE9iamVjdCk7XHJcbiAgICAgICAgdGhpcy5zYWZlQXBwbHkoKTtcclxuXHJcbiAgICAgICAgaWYob25SZXNldEZvcm0pIHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uUmVzZXRGb3JtKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmFsaWRhdGVzIGlmIHRoZSBjdXJyZW50IG9iamVjdCBkaWZmZXJzIGZyb20gd2hlcmUgaXQgd2FzIG9yaWdpbmFsbHkgc2F2ZWRcclxuICAgICAqIEByZXR1cm5zIHtib29sZWFufVxyXG4gICAgICovXHJcbiAgICBpc0RpcnR5KCkge1xyXG4gICAgICAgIHZhciBuZXdPYmplY3RJbnN0YW5jZSA9IGFuZ3VsYXIudG9Kc29uKHRoaXMuY3VycmVudE9iamVjdCk7XHJcbiAgICAgICAgcmV0dXJuIG5ld09iamVjdEluc3RhbmNlICE9PSB0aGlzLmdldEZvcm1Bc0pTT04oKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRoaXMgZnVuY3Rpb24gaXMgb25seSBhdmFpbGFibGUgd2hlbiB0aGUgRm9ybSBpcyBiZWluZyBjYWxsZWQgZnJvbSBhIERpYWxvZyBQb3BVcFxyXG4gICAgICovXHJcbiAgICBvbkNsb3NlRGlhbG9nKGV2ZW50LCByZWFzb24sIGNsb3NlZCkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ21vZGFsLmNsb3Npbmc6ICcgKyAoY2xvc2VkID8gJ2Nsb3NlJyA6ICdkaXNtaXNzJykgKyAnKCcgKyByZWFzb24gKyAnKScpO1xyXG4gICAgICAgIGlmICh0aGlzLmlzRGlydHkoKSAmJiByZWFzb24gIT09ICdjYW5jZWwtY29uZmlybWF0aW9uJyAmJiB0eXBlb2YgcmVhc29uICE9PSAnb2JqZWN0Jykge1xyXG4gICAgICAgICAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xyXG4gICAgICAgICAgICB0aGlzLmNvbmZpcm1DbG9zZUZvcm0oKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBIENvbmZpcm1hdGlvbiBEaWFsb2cgd2hlbiB0aGUgaW5mb3JtYXRpb24gY2FuIGJlIGxvc3RcclxuICAgICAqIEBwYXJhbSBldmVudFxyXG4gICAgICovXHJcbiAgICBjb25maXJtQ2xvc2VGb3JtKGV2ZW50KSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG4gICAgICAgICAgICBzaXplOiAnc20nLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIG1lc3NhZ2U6ICdDaGFuZ2VzIHlvdSBtYWRlIG1heSBub3QgYmUgc2F2ZWQuIERvIHlvdSB3YW50IHRvIGNvbnRpbnVlPydcclxuICAgICAgICAgICAgICAgICAgICB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsLWNvbmZpcm1hdGlvbicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVXRpbCB0byBjYWxsIHNhZmUgaWYgcmVxdWlyZWRcclxuICAgICAqIEBwYXJhbSBmblxyXG4gICAgICovXHJcbiAgICBzYWZlQXBwbHkoZm4pIHtcclxuICAgICAgICB2YXIgcGhhc2UgPSB0aGlzLnNjb3BlLiRyb290LiQkcGhhc2U7XHJcbiAgICAgICAgaWYocGhhc2UgPT09ICckYXBwbHknIHx8IHBoYXNlID09PSAnJGRpZ2VzdCcpIHtcclxuICAgICAgICAgICAgaWYoZm4gJiYgKHR5cGVvZihmbikgPT09ICdmdW5jdGlvbicpKSB7XHJcbiAgICAgICAgICAgICAgICBmbigpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy5zY29wZS4kYXBwbHkoZm4pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFV0aWwgdG8gUmVzZXQgYSBEcm9wZG93biBsaXN0IG9uIEtlbmRvXHJcbiAgICAgKi9cclxuXHJcbiAgICByZXNldERyb3BEb3duKHNlbGVjdG9ySW5zdGFuY2UsIHNlbGVjdGVkSWQsIGZvcmNlKSB7XHJcbiAgICAgICAgaWYoc2VsZWN0b3JJbnN0YW5jZSAmJiBzZWxlY3Rvckluc3RhbmNlLmRhdGFJdGVtcykge1xyXG4gICAgICAgICAgICBzZWxlY3Rvckluc3RhbmNlLmRhdGFJdGVtcygpLmZvckVhY2goKHZhbHVlLCBpbmRleCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoc2VsZWN0ZWRJZCA9PT0gdmFsdWUuaWQgfHwgc2VsZWN0ZWRJZCA9PT0gdmFsdWUpIHtcclxuICAgICAgICAgICAgICAgICAgICBzZWxlY3Rvckluc3RhbmNlLnNlbGVjdChpbmRleCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgaWYoZm9yY2UpIHtcclxuICAgICAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2UudHJpZ2dlcignY2hhbmdlJyk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnNhZmVBcHBseSgpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMy8yMDE1LlxyXG4gKiBJbXBsZW1lbnRzIFJYIE9ic2VydmFibGUgdG8gZGlzcG9zZSBhbmQgdHJhY2sgYmV0dGVyIGVhY2ggY2FsbCB0byB0aGUgc2VydmVyXHJcbiAqIFRoZSBPYnNlcnZlciBzdWJzY3JpYmUgYSBwcm9taXNlLlxyXG4gKi9cclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SGFuZGxlciB7XHJcbiAgICBjb25zdHJ1Y3RvcihyeCkge1xyXG4gICAgICAgIHRoaXMucnggPSByeDtcclxuICAgICAgICB0aGlzLnByb21pc2UgPSBbXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENhbGxlZCBmcm9tIFJlc3RTZXJ2aWNlSGFuZGxlci5zdWJzY3JpYmVSZXF1ZXN0XHJcbiAgICAgKiBpdCB2ZXJpZnkgdGhhdCB0aGUgY2FsbCBpcyBiZWluZyBkb25lIHRvIHRoZSBzZXJ2ZXIgYW5kIHJldHVybiBhIHByb21pc2VcclxuICAgICAqIEBwYXJhbSByZXF1ZXN0XHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgc3Vic2NyaWJlUmVxdWVzdChyZXF1ZXN0LCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB2YXIgcnhPYnNlcnZhYmxlID0gdGhpcy5yeC5PYnNlcnZhYmxlLmZyb21Qcm9taXNlKHJlcXVlc3QpO1xyXG4gICAgICAgIC8vIFZlcmlmeSBpcyBub3QgYSBkdXBsaWNhdGUgY2FsbFxyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIHRoaXMuY2FuY2VsUmVxdWVzdChyeE9ic2VydmFibGUpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgLy8gU3Vic2NyaWJlIHRoZSByZXF1ZXN0XHJcbiAgICAgICAgdmFyIHJlc3VsdFN1YnNjcmliZSA9IHRoaXMuYWRkU3Vic2NyaWJlKHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICBpZiAocmVzdWx0U3Vic2NyaWJlICYmIHJlc3VsdFN1YnNjcmliZS5pc1N0b3BwZWQpIHtcclxuICAgICAgICAgICAgLy8gQW4gZXJyb3IgaGFwcGVucywgdHJhY2tlZCBieSBIdHRwSW50ZXJjZXB0b3JJbnRlcmZhY2VcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBhZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSA9IHJ4T2JzZXJ2YWJsZS5zdWJzY3JpYmUoXHJcbiAgICAgICAgICAgIChyZXNwb25zZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAoZXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLm9uU3Vic2NyaWJlZEVycm9yKGVycm9yLCByeE9ic2VydmFibGUsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAvLyBOTy1PUCBTdWJzY3JpYmUgY29tcGxldGVkXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICByZXR1cm4gdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICB9XHJcblxyXG4gICAgY2FuY2VsUmVxdWVzdChyeE9ic2VydmFibGUpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgICAgIHJ4T2JzZXJ2YWJsZS5kaXNwb3NlKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpIHtcclxuICAgICAgICByZXR1cm4gKHJ4T2JzZXJ2YWJsZSAmJiByeE9ic2VydmFibGUuX3AgJiYgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF0pO1xyXG4gICAgfVxyXG5cclxuICAgIG9uU3Vic2NyaWJlZFN1Y2Nlc3MocmVzcG9uc2UsIHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgICAgICBpZihvblN1Y2Nlc3Mpe1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKHJlc3BvbnNlLmRhdGEpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRocm93cyBpbW1lZGlhdGVseSBlcnJvciB3aGVuIHRoZSBwZXRpdGlvbiBjYWxsIGlzIHdyb25nXHJcbiAgICAgKiBvciB3aXRoIGEgZGVsYXkgaWYgdGhlIGNhbGwgaXMgdmFsaWRcclxuICAgICAqIEBwYXJhbSBlcnJvclxyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIG9uU3Vic2NyaWJlZEVycm9yKGVycm9yLCByeE9ic2VydmFibGUsIG9uRXJyb3IpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKG9uRXJyb3Ipe1xyXG4gICAgICAgICAgICByZXR1cm4gb25FcnJvcih7fSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBSZXN0U2VydmljZUhhbmRsZXIgZnJvbSAnLi9SZXN0U2VydmljZUhhbmRsZXIuanMnO1xyXG5pbXBvcnQgVXNlclByZWZlcmVuY2VzU2VydmljZSBmcm9tICcuL1VzZXJQcmVmZXJlbmNlc1NlcnZpY2UuanMnXHJcblxyXG52YXIgUmVzdEFQSU1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5SZXN0QVBJTW9kdWxlJyxbXSk7XHJcblxyXG5SZXN0QVBJTW9kdWxlLnNlcnZpY2UoJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFsnJGxvZycsICckaHR0cCcsICckcmVzb3VyY2UnLCAncngnLCBSZXN0U2VydmljZUhhbmRsZXJdKTtcclxuUmVzdEFQSU1vZHVsZS5zZXJ2aWNlKCdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFVzZXJQcmVmZXJlbmNlc1NlcnZpY2VdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IFJlc3RBUElNb2R1bGU7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMDgvMTUuXHJcbiAqIEl0IGFic3RyYWN0IGVhY2ggb25lIG9mIHRoZSBleGlzdGluZyBjYWxsIHRvIHRoZSBBUEksIGl0IHNob3VsZCBvbmx5IGNvbnRhaW5zIHRoZSBjYWxsIGZ1bmN0aW9ucyBhbmQgcmVmZXJlbmNlXHJcbiAqIHRvIHRoZSBjYWxsYmFjaywgbm8gbG9naWMgYXQgYWxsLlxyXG4gKlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgUmVxdWVzdEhhbmRsZXIgZnJvbSAnLi9SZXF1ZXN0SGFuZGxlci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXN0U2VydmljZUhhbmRsZXIge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJGh0dHAsICRyZXNvdXJjZSwgcngpIHtcclxuICAgICAgICB0aGlzLnJ4ID0gcng7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuaHR0cCA9ICRodHRwO1xyXG4gICAgICAgIHRoaXMucmVzb3VyY2UgPSAkcmVzb3VyY2U7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVycygpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdSZXN0U2VydmljZSBMb2FkZWQnKTtcclxuICAgICAgICB0aGlzLnJlcSA9IHtcclxuICAgICAgICAgICAgbWV0aG9kOiAnJyxcclxuICAgICAgICAgICAgdXJsOiAnJyxcclxuICAgICAgICAgICAgaGVhZGVyczoge1xyXG4gICAgICAgICAgICAgICAgJ0NvbnRlbnQtVHlwZSc6ICdhcHBsaWNhdGlvbi9qc29uO2NoYXJzZXQ9VVRGLTgnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGE6IFtdXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlSGVhZGVycygpIHtcclxuICAgICAgICB0aGlzLmh0dHAuZGVmYXVsdHMuaGVhZGVycy5wb3N0WydDb250ZW50LVR5cGUnXSA9ICdhcHBsaWNhdGlvbi94LXd3dy1mb3JtLXVybGVuY29kZWQnO1xyXG4gICAgfVxyXG5cclxuICAgIFRhc2tTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRGZWVkczogKGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJ3Rlc3QvbW9ja3VwRGF0YS9UYXNrTWFuYWdlci90YXNrTWFuYWdlckxpc3QuanNvbicpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGNvbW1vblNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldFRpbWVab25lQ29uZmlndXJhdGlvbjogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL3VzZXIvcHJlZmVyZW5jZXMvQ1VSUl9EVF9GT1JNQVQsQ1VSUl9UWicpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldExpY2Vuc2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlcycpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldFByb2plY3REYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9wcm9qZWN0JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdDogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL2xpY2Vuc2UvcmVxdWVzdCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBhcHBseUxpY2Vuc2U6IChsaWNlbnNlSWQsIGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvbG9hZCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRIYXNoQ29kZTogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnR0VUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2hhc2gnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEVtYWlsQ29udGVudDogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnR0VUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2VtYWlsL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIC8vLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICAgICAgICAgICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdDogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9lbWFpbC9yZXF1ZXN0JztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbWFpbFJlcXVlc3Q6IChkYXRhLCBjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkZWxldGVMaWNlbnNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9saWNlbnNlLycgKyBkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgcmVxdWVzdEltcG9ydDogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL21hbmFnZXIvbGljZW5zZS9yZXF1ZXN0JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbWFuYWdlci9saWNlbnNlJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldFByb2plY3REYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9wcm9qZWN0JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvZW52aXJvbm1lbnQnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0S2V5Q29kZTogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnR0VUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcva2V5JztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzYXZlTGljZW5zZTogKGxpY2Vuc2VJZCwgbGljZW5zZU1vZGlmaWVkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQVVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGxpY2Vuc2VNb2RpZmllZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkZWxldGVMaWNlbnNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGRhdGEuaWQgKyAnL2RlbGV0ZSc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgcmV2b2tlTGljZW5zZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGFjdGl2YXRlTGljZW5zZTogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2FjdGl2YXRlJztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRBY3Rpdml0eUxvZzogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnR0VUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvYWN0aXZpdHlsb2cnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIG1hbnVhbGx5UmVxdWVzdDogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2VtYWlsL3NlbmQnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIG5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXROb3RpY2VMaXN0OiAob25TdWNjZXNzKSA9PiB7IC8vIHJlYWwgd3MgZXhhbXBsZVxyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL25vdGljZXMnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY3JlYXRlTm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3Mvbm90aWNlcyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlZGl0Tm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUFVUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9ub3RpY2VzLycgKyBkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGVsZXRlTm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9ub3RpY2VzLycgKyBkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAzLzcvMjAxNy5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVXNlclByZWZlcmVuY2VzU2VydmljZSBJbnN0YW5jZWQnKTtcclxuXHJcbiAgICAgICAgdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24gPSB7XHJcbiAgICAgICAgICAgIHByZWZlcmVuY2VzOiB7fVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBnZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24ob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5jb21tb25TZXJ2aWNlSGFuZGxlcigpLmdldFRpbWVab25lQ29uZmlndXJhdGlvbigoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbiA9IGRhdGEuZGF0YTtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcygpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldENvbnZlcnRlZERhdGVJbnRvVGltZVpvbmUoZGF0ZVN0cmluZykge1xyXG4gICAgICAgIHZhciB0aW1lU3RyaW5nID0gZGF0ZVN0cmluZztcclxuICAgICAgICB2YXIgdXNlckRURm9ybWF0ID0gdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24ucHJlZmVyZW5jZXMuQ1VSUl9EVF9GT1JNQVQ7XHJcbiAgICAgICAgdmFyIHRpbWVab25lID0gdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24ucHJlZmVyZW5jZXMuQ1VSUl9UWjtcclxuXHJcbiAgICAgICAgaWYoZGF0ZVN0cmluZyAhPT0gJ251bGwnICYmXHJcbiAgICAgICAgICAgIGRhdGVTdHJpbmcgJiYgbW9tZW50KGRhdGVTdHJpbmcpLmlzVmFsaWQoKSAmJlxyXG4gICAgICAgICAgICAhKHR5cGVvZiBOdW1iZXIoZGF0ZVN0cmluZy50b1N0cmluZygpKSA9PT0gJ251bWJlcicgJiYgIWlzTmFOKGRhdGVTdHJpbmcudG9TdHJpbmcoKSkpKXtcclxuICAgICAgICAgICAgaWYgKHRpbWVab25lID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgICAgICB0aW1lWm9uZSA9ICdHTVQnO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHZhciBmb3JtYXQgPSAnTU0vREQvWVlZWSc7XHJcbiAgICAgICAgICAgIGlmICh1c2VyRFRGb3JtYXQgPT09ICdERC9NTS9ZWVlZJykge1xyXG4gICAgICAgICAgICAgICAgZm9ybWF0ID0gJ0REL01NL1lZWVknO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHRpbWVTdHJpbmcgPSBtb21lbnQoZGF0ZVN0cmluZykudHoodGltZVpvbmUpLmZvcm1hdChmb3JtYXQpXHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICByZXR1cm4gdGltZVN0cmluZyAhPT0gJ251bGwnPyB0aW1lU3RyaW5nOiAnJztcclxuICAgIH1cclxuXHJcbiAgICBnZXRDb252ZXJ0ZWREYXRlVGltZUludG9UaW1lWm9uZShkYXRlU3RyaW5nKSB7XHJcbiAgICAgICAgdmFyIHRpbWVTdHJpbmcgPSBkYXRlU3RyaW5nO1xyXG4gICAgICAgIHZhciB1c2VyRFRGb3JtYXQgPSB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbi5wcmVmZXJlbmNlcy5DVVJSX0RUX0ZPUk1BVDtcclxuICAgICAgICB2YXIgdGltZVpvbmUgPSB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbi5wcmVmZXJlbmNlcy5DVVJSX1RaO1xyXG5cclxuICAgICAgICBpZihkYXRlU3RyaW5nICE9PSAnbnVsbCcgJiZcclxuICAgICAgICAgICAgZGF0ZVN0cmluZyAmJiBtb21lbnQoZGF0ZVN0cmluZykuaXNWYWxpZCgpICYmXHJcbiAgICAgICAgICAgICEodHlwZW9mIE51bWJlcihkYXRlU3RyaW5nLnRvU3RyaW5nKCkpID09PSAnbnVtYmVyJyAmJiAhaXNOYU4oZGF0ZVN0cmluZy50b1N0cmluZygpKSkpe1xyXG4gICAgICAgICAgICBpZiAodGltZVpvbmUgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgICAgIHRpbWVab25lID0gJ0dNVCc7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgdmFyIGZvcm1hdCA9ICdNTS9ERC9ZWVlZIGhoOm1tIGEnXHJcbiAgICAgICAgICAgIGlmICh1c2VyRFRGb3JtYXQgPT09ICdERC9NTS9ZWVlZJykge1xyXG4gICAgICAgICAgICAgICAgZm9ybWF0ID0gJ0REL01NL1lZWVkgaGg6bW0gYSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB0aW1lU3RyaW5nID0gbW9tZW50KGRhdGVTdHJpbmcpLnR6KHRpbWVab25lKS5mb3JtYXQoZm9ybWF0KVxyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgcmV0dXJuIHRpbWVTdHJpbmcgIT09ICdudWxsJz8gdGltZVN0cmluZzogJyc7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBLZW5kbyBEYXRlIEZvcm1hdCBpcyBxdWl0ZSBkaWZmZXJlbnQgYW5kIHRocmVhdHMgdGhlIGNoYXJzIHdpdGggYSBkaWZmZXJlbnQgbWVhbmluZ1xyXG4gICAgICogdGhpcyBGdW5jdGlvbnMgcmV0dXJuIHRoZSBvbmUgaW4gdGhlIHN0YW5kYXIgZm9ybWF0LlxyXG4gICAgICogQ29uc2lkZXIgdGhpcyBpcyBvbmx5IGZyb20gb3VyIEN1cnJlbnQgRm9ybWF0XHJcbiAgICAgKi9cclxuICAgIGdldENvbnZlcnRlZERhdGVGb3JtYXRUb0tlbmRvRGF0ZSgpIHtcclxuICAgICAgICB2YXIgdXNlckRURm9ybWF0ID0gdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24ucHJlZmVyZW5jZXMuQ1VSUl9EVF9GT1JNQVQ7XHJcblxyXG4gICAgICAgIHZhciBmb3JtYXQgPSAnTU0vZGQveXl5eSc7XHJcbiAgICAgICAgaWYgKHVzZXJEVEZvcm1hdCAhPT0gbnVsbCkge1xyXG4gICAgICAgICAgICBmb3JtYXQgPSBmb3JtYXQucmVwbGFjZSgnREQnLCAnZGQnKTtcclxuICAgICAgICAgICAgZm9ybWF0ID0gZm9ybWF0LnJlcGxhY2UoJ1lZWVknLCAneXl5eScpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgcmV0dXJuIGZvcm1hdDtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIEVTNiBJbnRlcmNlcHRvciBjYWxscyBpbm5lciBtZXRob2RzIGluIGEgZ2xvYmFsIHNjb3BlLCB0aGVuIHRoZSBcInRoaXNcIiBpcyBiZWluZyBsb3N0XHJcbiAqIGluIHRoZSBkZWZpbml0aW9uIG9mIHRoZSBDbGFzcyBmb3IgaW50ZXJjZXB0b3JzIG9ubHlcclxuICogVGhpcyBpcyBhIGludGVyZmFjZSB0aGF0IHRha2UgY2FyZSBvZiB0aGUgaXNzdWUuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IC8qIGludGVyZmFjZSovIGNsYXNzIEh0dHBJbnRlcmNlcHRvciB7XHJcbiAgICBjb25zdHJ1Y3RvcihtZXRob2RUb0JpbmQpIHtcclxuICAgICAgICAvLyBJZiBub3QgbWV0aG9kIHRvIGJpbmQsIHdlIGFzc3VtZSBvdXIgaW50ZXJjZXB0b3IgaXMgdXNpbmcgYWxsIHRoZSBpbm5lciBmdW5jdGlvbnNcclxuICAgICAgICBpZighbWV0aG9kVG9CaW5kKSB7XHJcbiAgICAgICAgICAgIFsncmVxdWVzdCcsICdyZXF1ZXN0RXJyb3InLCAncmVzcG9uc2UnLCAncmVzcG9uc2VFcnJvciddXHJcbiAgICAgICAgICAgICAgICAuZm9yRWFjaCgobWV0aG9kKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpc1ttZXRob2RdKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXNbbWV0aG9kXSA9IHRoaXNbbWV0aG9kXS5iaW5kKHRoaXMpO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIC8vIG1ldGhvZFRvQmluZCByZWZlcmVuY2UgdG8gYSBzaW5nbGUgY2hpbGQgY2xhc3NcclxuICAgICAgICAgICAgdGhpc1ttZXRob2RUb0JpbmRdID0gdGhpc1ttZXRob2RUb0JpbmRdLmJpbmQodGhpcyk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIFVzZSB0aGlzIG1vZHVsZSB0byBtb2RpZnkgYW55dGhpbmcgcmVsYXRlZCB0byB0aGUgSGVhZGVycyBhbmQgUmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5cclxuXHJcbnZhciBIVFRQTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkhUVFBNb2R1bGUnLCBbJ25nUmVzb3VyY2UnXSkuY29uZmlnKFsnJGh0dHBQcm92aWRlcicsIGZ1bmN0aW9uKCRodHRwUHJvdmlkZXIpe1xyXG5cclxuICAgIC8vaW5pdGlhbGl6ZSBnZXQgaWYgbm90IHRoZXJlXHJcbiAgICBpZiAoISRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXQpIHtcclxuICAgICAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0ID0ge307XHJcbiAgICB9XHJcblxyXG4gICAgLy9EaXNhYmxlIElFIGFqYXggcmVxdWVzdCBjYWNoaW5nXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydJZi1Nb2RpZmllZC1TaW5jZSddID0gJ01vbiwgMjYgSnVsIDE5OTcgMDU6MDA6MDAgR01UJztcclxuICAgIC8vIGV4dHJhXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydDYWNoZS1Db250cm9sJ10gPSAnbm8tY2FjaGUnO1xyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnUHJhZ21hJ10gPSAnbm8tY2FjaGUnO1xyXG5cclxuXHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlcXVlc3RcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlc3BvbnNlXHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcblxyXG5cclxufV0pO1xyXG5cclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIVFRQTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIEl0IGltcGxlbWVudCBhbiBhYnN0cmFjdCBjYWxsIHRvIEhUVFAgSW50ZXJjZXB0b3JzIHRvIG1hbmFnZSBlcnJvciBoYW5kbGVyXHJcbiAqIFNvbWV0aW1lcyBhIHJlcXVlc3QgY2FuJ3QgYmUgc2VudCBvciBpdCBpcyByZWplY3RlZCBieSBhbiBpbnRlcmNlcHRvci5cclxuICogUmVxdWVzdCBlcnJvciBpbnRlcmNlcHRvciBjYXB0dXJlcyByZXF1ZXN0cyB0aGF0IGhhdmUgYmVlbiBjYW5jZWxlZCBieSBhIHByZXZpb3VzIHJlcXVlc3QgaW50ZXJjZXB0b3IuXHJcbiAqIEl0IGNhbiBiZSB1c2VkIGluIG9yZGVyIHRvIHJlY292ZXIgdGhlIHJlcXVlc3QgYW5kIHNvbWV0aW1lcyB1bmRvIHRoaW5ncyB0aGF0IGhhdmUgYmVlbiBzZXQgdXAgYmVmb3JlIGEgcmVxdWVzdCxcclxuICogbGlrZSByZW1vdmluZyBvdmVybGF5cyBhbmQgbG9hZGluZyBpbmRpY2F0b3JzLCBlbmFibGluZyBidXR0b25zIGFuZCBmaWVsZHMgYW5kIHNvIG9uLlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3JlcXVlc3RFcnJvcicpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdEVycm9yKHJlamVjdGlvbikge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vaWYgKGNhblJlY292ZXIocmVqZWN0aW9uKSkge1xyXG4gICAgICAgIC8vICAgIHJldHVybiByZXNwb25zZU9yTmV3UHJvbWlzZVxyXG4gICAgICAgIC8vfVxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnEucmVqZWN0KHJlamVjdGlvbik7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuRXJyb3IoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSXQgaW1wbGVtZW50IGFuIGFic3RyYWN0IGNhbGwgdG8gSFRUUCBJbnRlcmNlcHRvcnMgdG8gbWFuYWdlIG9ubHkgcmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXF1ZXN0Jyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdChjb25maWcpIHtcclxuICAgICAgICAvLyBXZSBjYW4gYWRkIGhlYWRlcnMgaWYgb24gdGhlIGluY29taW5nIHJlcXVlc3QgbWFkZSBpdCB3ZSBoYXZlIHRoZSB0b2tlbiBpbnNpZGVcclxuICAgICAgICAvLyBkZWZpbmVkIGJ5IHNvbWUgY29uZGl0aW9uc1xyXG4gICAgICAgIC8vY29uZmlnLmhlYWRlcnNbJ3gtc2Vzc2lvbi10b2tlbiddID0gbXkudG9rZW47XHJcblxyXG4gICAgICAgIGNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KGNvbmZpZyk7XHJcblxyXG4gICAgICAgIHJldHVybiBjb25maWcgfHwgdGhpcy5xLndoZW4oY29uZmlnKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXF1ZXN0KCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIElmIGJhY2tlbmQgY2FsbCBmYWlscyBvciBpdCBtaWdodCBiZSByZWplY3RlZCBieSBhIHJlcXVlc3QgaW50ZXJjZXB0b3Igb3IgYnkgYSBwcmV2aW91cyByZXNwb25zZSBpbnRlcmNlcHRvcjtcclxuICogSW4gdGhvc2UgY2FzZXMsIHJlc3BvbnNlIGVycm9yIGludGVyY2VwdG9yIGNhbiBoZWxwIHVzIHRvIHJlY292ZXIgdGhlIGJhY2tlbmQgY2FsbC5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZUVycm9yJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzcG9uc2VFcnJvcihyZWplY3Rpb24pIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvL2lmIChjYW5SZWNvdmVyKHJlamVjdGlvbikpIHtcclxuICAgICAgICAvLyAgICByZXR1cm4gcmVzcG9uc2VPck5ld1Byb21pc2VcclxuICAgICAgICAvLyB9XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMucS5yZWplY3QocmVqZWN0aW9uKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5FcnJvcigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBUaGlzIG1ldGhvZCBpcyBjYWxsZWQgcmlnaHQgYWZ0ZXIgJGh0dHAgcmVjZWl2ZXMgdGhlIHJlc3BvbnNlIGZyb20gdGhlIGJhY2tlbmQsXHJcbiAqIHNvIHlvdSBjYW4gbW9kaWZ5IHRoZSByZXNwb25zZSBhbmQgbWFrZSBvdGhlciBhY3Rpb25zLiBUaGlzIGZ1bmN0aW9uIHJlY2VpdmVzIGEgcmVzcG9uc2Ugb2JqZWN0IGFzIGEgcGFyYW1ldGVyXHJcbiAqIGFuZCBoYXMgdG8gcmV0dXJuIGEgcmVzcG9uc2Ugb2JqZWN0IG9yIGEgcHJvbWlzZS4gVGhlIHJlc3BvbnNlIG9iamVjdCBpbmNsdWRlc1xyXG4gKiB0aGUgcmVxdWVzdCBjb25maWd1cmF0aW9uLCBoZWFkZXJzLCBzdGF0dXMgYW5kIGRhdGEgdGhhdCByZXR1cm5lZCBmcm9tIHRoZSBiYWNrZW5kLlxyXG4gKiBSZXR1cm5pbmcgYW4gaW52YWxpZCByZXNwb25zZSBvYmplY3Qgb3IgcHJvbWlzZSB0aGF0IHdpbGwgYmUgcmVqZWN0ZWQsIHdpbGwgbWFrZSB0aGUgJGh0dHAgY2FsbCB0byBmYWlsLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZScpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXNwb25zZShyZXNwb25zZSkge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBzdWNjZXNzXHJcblxyXG4gICAgICAgIHJlc3BvbnNlLmNvbmZpZy5yZXNwb25zZVRpbWVzdGFtcCA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZXNwb25zZSk7XHJcbiAgICAgICAgcmV0dXJuIHJlc3BvbnNlIHx8IHRoaXMucS53aGVuKHJlc3BvbnNlKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXNwb25zZSgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG59XHJcblxyXG4iXX0=
