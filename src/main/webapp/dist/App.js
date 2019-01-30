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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL2NvbmZpZy9Bbmd1bGFyUHJvdmlkZXJIZWxwZXIuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL2NvbmZpZy9BcHAuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL2RpcmVjdGl2ZXMvaW5kZXguanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL2RpcmVjdGl2ZXMvdG9vbHMvTW9kYWxXaW5kb3dBY3RpdmF0aW9uLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9kaXJlY3RpdmVzL3Rvb2xzL1RvYXN0SGFuZGxlci5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbWFpbi5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJDb250cm9sbGVyLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJNb2R1bGUuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL0xpY2Vuc2VBZG1pbk1vZHVsZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vYXBwbHlMaWNlbnNlS2V5L0FwcGx5TGljZW5zZUtleS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vY3JlYXRlZC9DcmVhdGVkTGljZW5zZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vZGV0YWlsL0xpY2Vuc2VEZXRhaWwuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2xpc3QvTGljZW5zZUFkbWluTGlzdC5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vbWFudWFsbHlSZXF1ZXN0L01hbnVhbGx5UmVxdWVzdC5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vcmVxdWVzdC9SZXF1ZXN0TGljZW5zZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vc2VydmljZS9MaWNlbnNlQWRtaW5TZXJ2aWNlLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL0xpY2Vuc2VNYW5hZ2VyTW9kdWxlLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2RldGFpbC9MaWNlbnNlTWFuYWdlckRldGFpbC5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvc2VydmljZS9MaWNlbnNlTWFuYWdlclNlcnZpY2UuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9Ob3RpY2VNYW5hZ2VyTW9kdWxlLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvZWRpdC9FZGl0Tm90aWNlLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvbGlzdC9Ob3RpY2VMaXN0LmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvc2VydmljZS9Ob3RpY2VNYW5hZ2VyU2VydmljZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9UYXNrTWFuYWdlck1vZHVsZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9lZGl0L1Rhc2tNYW5hZ2VyRWRpdC5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9saXN0L1Rhc2tNYW5hZ2VyQ29udHJvbGxlci5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9zZXJ2aWNlL1Rhc2tNYW5hZ2VyU2VydmljZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvbW9kdWxlcy91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL3NlcnZpY2VzL1Jlc3RBUEkvUmVxdWVzdEhhbmRsZXIuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL3NlcnZpY2VzL1Jlc3RBUEkvUmVzdEFQSU1vZHVsZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvc2VydmljZXMvUmVzdEFQSS9SZXN0U2VydmljZUhhbmRsZXIuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL3NlcnZpY2VzL1Jlc3RBUEkvVXNlclByZWZlcmVuY2VzU2VydmljZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvc2VydmljZXMvaHR0cC9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMiLCJzcmMvbWFpbi93ZWJhcHAvYXBwLWpzL3NlcnZpY2VzL2h0dHAvSFRUUE1vZHVsZS5qcyIsInNyYy9tYWluL3dlYmFwcC9hcHAtanMvc2VydmljZXMvaHR0cC9IVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9zZXJ2aWNlcy9odHRwL0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9zZXJ2aWNlcy9odHRwL0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzIiwic3JjL21haW4vd2ViYXBwL2FwcC1qcy9zZXJ2aWNlcy9odHRwL0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvci5qcyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQTs7O0FDQUE7Ozs7Ozs7Ozs7QUFVQSxJQUFJLFFBQVEsUUFBWixVQUFZLENBQVo7O0FBRUE7Ozs7O0FBS0EsTUFBQSxTQUFBLEdBQWtCLFVBQUEsT0FBQSxFQUFBLEVBQUEsRUFBdUI7QUFDckM7O0FBQ0EsUUFBSSxRQUFRLFFBQUEsS0FBQSxDQUFaLE9BQUE7QUFDQSxRQUFJLFVBQUEsUUFBQSxJQUFzQixVQUExQixTQUFBLEVBQStDO0FBQzNDLFlBQUEsRUFBQSxFQUFRO0FBQ0osb0JBQUEsS0FBQSxDQUFBLEVBQUE7QUFDSDtBQUhMLEtBQUEsTUFJTztBQUNILFlBQUEsRUFBQSxFQUFRO0FBQ0osb0JBQUEsTUFBQSxDQUFBLEVBQUE7QUFESixTQUFBLE1BRU87QUFDSCxvQkFBQSxNQUFBO0FBQ0g7QUFDSjtBQWJMLENBQUE7O0FBZ0JBOzs7OztBQUtBLE1BQUEsZUFBQSxHQUF3QixVQUFBLE9BQUEsRUFBQSxJQUFBLEVBQXlCO0FBQzdDOztBQUNBLFFBQUksTUFBQSxZQUFBLENBQUosZUFBQSxFQUF3QztBQUNwQyxjQUFBLFlBQUEsQ0FBQSxlQUFBLENBQUEsU0FBQSxDQUFBLE9BQUEsRUFBQSxJQUFBO0FBREosS0FBQSxNQUVPLElBQUksTUFBSixTQUFBLEVBQXFCO0FBQ3hCLGNBQUEsU0FBQSxDQUFBLE9BQUEsRUFBQSxJQUFBO0FBQ0g7QUFOTCxDQUFBOztBQVNBOzs7OztBQUtBLE1BQUEsZ0JBQUEsR0FBeUIsVUFBQSxPQUFBLEVBQUEsSUFBQSxFQUF5QjtBQUM5Qzs7QUFDQSxRQUFJLE1BQUEsWUFBQSxDQUFKLGtCQUFBLEVBQTJDO0FBQ3ZDLGNBQUEsa0JBQUEsQ0FBQSxRQUFBLENBQUEsT0FBQSxFQUFBLElBQUE7QUFESixLQUFB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Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7SUFFcUIsaUM7OztBQUNqQixhQUFBLDhCQUFBLENBQUEsSUFBQSxFQUFBLEVBQUEsRUFBQSxFQUFBLEVBQTBCO0FBQUEsd0JBQUEsSUFBQSxFQUFBLDhCQUFBOztBQUFBLFlBQUEsUUFBQSwyQkFBQSxJQUFBLEVBQUEsQ0FBQSwrQkFBQSxTQUFBLElBQUEsT0FBQSxjQUFBLENBQUEsOEJBQUEsQ0FBQSxFQUFBLElBQUEsQ0FBQSxJQUFBLEVBQUEsVUFBQSxDQUFBLENBQUE7O0FBRXRCLGNBQUEsR0FBQSxHQUFBLElBQUE7QUFDQSxjQUFBLENBQUEsR0FBQSxFQUFBO0FBQ0EsY0FBQSxLQUFBLEdBQWEsTUFBQSxDQUFBLENBQWIsS0FBYSxFQUFiO0FBQ0EsY0FBQSxHQUFBLENBQUEsS0FBQSxDQUFBLDBDQUFBO0FBTHNCLGVBQUEsS0FBQTtBQU16Qjs7OztpQ0FFUSxTLEVBQVU7QUFDZjs7QUFFQSxzQkFBQSxNQUFBLENBQUEsaUJBQUEsR0FBb0MsSUFBQSxJQUFBLEdBQXBDLE9BQW9DLEVBQXBDOztBQUVBLGlCQUFBLEtBQUEsQ0FBQSxNQUFBLENBQUEsU0FBQTtBQUNBLG1CQUFPLGFBQVksS0FBQSxDQUFBLENBQUEsSUFBQSxDQUFuQixTQUFtQixDQUFuQjtBQUNIOzs7eUNBRWdCO0FBQ2IsbUJBQU8sS0FBQSxLQUFBLENBQVAsT0FBQTtBQUNIOzs7O0dBcEJ1RCxjQUFlLDJCQUFBLE87O2tCQUF0RCw4QiIsImZpbGUiOiJnZW5lcmF0ZWQuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlc0NvbnRlbnQiOlsiKGZ1bmN0aW9uKCl7ZnVuY3Rpb24gcihlLG4sdCl7ZnVuY3Rpb24gbyhpLGYpe2lmKCFuW2ldKXtpZighZVtpXSl7dmFyIGM9XCJmdW5jdGlvblwiPT10eXBlb2YgcmVxdWlyZSYmcmVxdWlyZTtpZighZiYmYylyZXR1cm4gYyhpLCEwKTtpZih1KXJldHVybiB1KGksITApO3ZhciBhPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIraStcIidcIik7dGhyb3cgYS5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGF9dmFyIHA9bltpXT17ZXhwb3J0czp7fX07ZVtpXVswXS5jYWxsKHAuZXhwb3J0cyxmdW5jdGlvbihyKXt2YXIgbj1lW2ldWzFdW3JdO3JldHVybiBvKG58fHIpfSxwLHAuZXhwb3J0cyxyLGUsbix0KX1yZXR1cm4gbltpXS5leHBvcnRzfWZvcih2YXIgdT1cImZ1bmN0aW9uXCI9PXR5cGVvZiByZXF1aXJlJiZyZXF1aXJlLGk9MDtpPHQubGVuZ3RoO2krKylvKHRbaV0pO3JldHVybiBvfXJldHVybiByfSkoKSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMjAvMjAxNS5cclxuICogVERTTSBpcyBhIGdsb2JhbCBvYmplY3QgdGhhdCBjb21lcyBmcm9tIEFwcC5qc1xyXG4gKlxyXG4gKiBUaGUgZm9sbG93aW5nIGhlbHBlciB3b3JrcyBpbiBhIHdheSB0byBtYWtlIGF2YWlsYWJsZSB0aGUgY3JlYXRpb24gb2YgRGlyZWN0aXZlLCBTZXJ2aWNlcyBhbmQgQ29udHJvbGxlclxyXG4gKiBvbiBmbHkgb3Igd2hlbiBkZXBsb3lpbmcgdGhlIGFwcC5cclxuICpcclxuICogV2UgcmVkdWNlIHRoZSB1c2Ugb2YgY29tcGlsZSBhbmQgZXh0cmEgc3RlcHNcclxuICovXHJcblxyXG52YXIgVERTVE0gPSByZXF1aXJlKCcuL0FwcC5qcycpO1xyXG5cclxuLyoqXHJcbiAqIExpc3RlbiB0byBhbiBleGlzdGluZyBkaWdlc3Qgb2YgdGhlIGNvbXBpbGUgcHJvdmlkZXIgYW5kIGV4ZWN1dGUgdGhlICRhcHBseSBpbW1lZGlhdGVseSBvciBhZnRlciBpdCdzIHJlYWR5XHJcbiAqIEBwYXJhbSBjdXJyZW50XHJcbiAqIEBwYXJhbSBmblxyXG4gKi9cclxuVERTVE0uc2FmZUFwcGx5ID0gZnVuY3Rpb24gKGN1cnJlbnQsIGZuKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICB2YXIgcGhhc2UgPSBjdXJyZW50LiRyb290LiQkcGhhc2U7XHJcbiAgICBpZiAocGhhc2UgPT09ICckYXBwbHknIHx8IHBoYXNlID09PSAnJGRpZ2VzdCcpIHtcclxuICAgICAgICBpZiAoZm4pIHtcclxuICAgICAgICAgICAgY3VycmVudC4kZXZhbChmbik7XHJcbiAgICAgICAgfVxyXG4gICAgfSBlbHNlIHtcclxuICAgICAgICBpZiAoZm4pIHtcclxuICAgICAgICAgICAgY3VycmVudC4kYXBwbHkoZm4pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGFwcGx5KCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3QgZGlyZWN0aXZlIGFzeW5jIGlmIHRoZSBjb21waWxlUHJvdmlkZXIgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVEaXJlY3RpdmUgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIpIHtcclxuICAgICAgICBURFNUTS5Qcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyLmRpcmVjdGl2ZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uZGlyZWN0aXZlKSB7XHJcbiAgICAgICAgVERTVE0uZGlyZWN0aXZlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3QgY29udHJvbGxlcnMgYXN5bmMgaWYgdGhlIGNvbnRyb2xsZXJQcm92aWRlciBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZUNvbnRyb2xsZXIgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5jb250cm9sbGVyUHJvdmlkZXIpIHtcclxuICAgICAgICBURFNUTS5jb250cm9sbGVyUHJvdmlkZXIucmVnaXN0ZXIoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmNvbnRyb2xsZXIpIHtcclxuICAgICAgICBURFNUTS5jb250cm9sbGVyKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3Qgc2VydmljZSBhc3luYyBpZiB0aGUgcHJvdmlkZVNlcnZpY2UgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVTZXJ2aWNlID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2UpIHtcclxuICAgICAgICBURFNUTS5Qcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2Uuc2VydmljZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uY29udHJvbGxlcikge1xyXG4gICAgICAgIFREU1RNLnNlcnZpY2Uoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogRm9yIExlZ2FjeSBzeXN0ZW0sIHdoYXQgaXMgZG9lcyBpcyB0byB0YWtlIHBhcmFtcyBmcm9tIHRoZSBxdWVyeVxyXG4gKiBvdXRzaWRlIHRoZSBBbmd1bGFySlMgdWktcm91dGluZy5cclxuICogQHBhcmFtIHBhcmFtIC8vIFBhcmFtIHRvIHNlYXJjIGZvciAvZXhhbXBsZS5odG1sP2Jhcj1mb28jY3VycmVudFN0YXRlXHJcbiAqL1xyXG5URFNUTS5nZXRVUkxQYXJhbSA9IGZ1bmN0aW9uIChwYXJhbSkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgJC51cmxQYXJhbSA9IGZ1bmN0aW9uIChuYW1lKSB7XHJcbiAgICAgICAgdmFyIHJlc3VsdHMgPSBuZXcgUmVnRXhwKCdbXFw/Jl0nICsgbmFtZSArICc9KFteJiNdKiknKS5leGVjKHdpbmRvdy5sb2NhdGlvbi5ocmVmKTtcclxuICAgICAgICBpZiAocmVzdWx0cyA9PT0gbnVsbCkge1xyXG4gICAgICAgICAgICByZXR1cm4gbnVsbDtcclxuICAgICAgICB9XHJcbiAgICAgICAgZWxzZSB7XHJcbiAgICAgICAgICAgIHJldHVybiByZXN1bHRzWzFdIHx8IDA7XHJcbiAgICAgICAgfVxyXG4gICAgfTtcclxuXHJcbiAgICByZXR1cm4gJC51cmxQYXJhbShwYXJhbSk7XHJcbn07XHJcblxyXG4vKipcclxuICogVGhpcyBjb2RlIHdhcyBpbnRyb2R1Y2VkIG9ubHkgZm9yIHRoZSBpZnJhbWUgbWlncmF0aW9uXHJcbiAqIGl0IGRldGVjdCB3aGVuIG1vdXNlIGVudGVyXHJcbiAqL1xyXG5URFNUTS5pZnJhbWVMb2FkZXIgPSBmdW5jdGlvbiAoKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICAkKCcuaWZyYW1lTG9hZGVyJykuaG92ZXIoXHJcbiAgICAgICAgZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAkKCcubmF2YmFyLXVsLWNvbnRhaW5lciAuZHJvcGRvd24ub3BlbicpLnJlbW92ZUNsYXNzKCdvcGVuJyk7XHJcbiAgICAgICAgfSwgZnVuY3Rpb24gKCkge1xyXG4gICAgICAgIH1cclxuICAgICk7XHJcbn07XHJcblxyXG5URFNUTS5nZXRDb252ZXJ0ZWREYXRlRm9ybWF0ID0gZnVuY3Rpb24oIGRhdGVTdHJpbmcsIHVzZXJEVEZvcm1hdCwgdGltZVpvbmUgKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICB2YXIgdGltZVN0cmluZyA9ICcnO1xyXG4gICAgaWYoZGF0ZVN0cmluZyl7XHJcbiAgICAgICAgaWYgKHRpbWVab25lID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgIHRpbWVab25lID0gJ0dNVCc7XHJcbiAgICAgICAgfVxyXG4gICAgICAgIHZhciBmb3JtYXQgPSAnTU0vREQvWVlZWSc7XHJcbiAgICAgICAgaWYgKHVzZXJEVEZvcm1hdCA9PT0gJ0REL01NL1lZWVknKSB7XHJcbiAgICAgICAgICAgIGZvcm1hdCA9ICdERC9NTS9ZWVlZJztcclxuICAgICAgICB9XHJcbiAgICAgICAgLy8gQ29udmVydCB6dWx1IGRhdGV0aW1lIHRvIGEgc3BlY2lmaWMgdGltZXpvbmUvZm9ybWF0XHJcbiAgICAgICAgdGltZVN0cmluZyA9IG1vbWVudChkYXRlU3RyaW5nKS50eih0aW1lWm9uZSkuZm9ybWF0KGZvcm1hdClcclxuICAgIH1cclxuICAgIHJldHVybiB0aW1lU3RyaW5nO1xyXG59O1xyXG5cclxud2luZG93LlREU1RNID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8xNi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnJlcXVpcmUoJ2FuZ3VsYXInKTtcclxucmVxdWlyZSgnYW5ndWxhci1hbmltYXRlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItbW9ja3MnKTtcclxucmVxdWlyZSgnYW5ndWxhci1zYW5pdGl6ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXJlc291cmNlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdHJhbnNsYXRlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdHJhbnNsYXRlLWxvYWRlci1wYXJ0aWFsJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdWktYm9vdHN0cmFwJyk7XHJcbnJlcXVpcmUoJ25nY2xpcGJvYXJkJyk7XHJcbnJlcXVpcmUoJ3VpLXJvdXRlcicpO1xyXG5yZXF1aXJlKCdyeC1hbmd1bGFyJyk7XHJcbnJlcXVpcmUoJ2FwaS1jaGVjaycpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWZvcm1seScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWZvcm1seS10ZW1wbGF0ZXMtYm9vdHN0cmFwJyk7XHJcblxyXG4vLyBNb2R1bGVzXHJcbmltcG9ydCBIVFRQTW9kdWxlIGZyb20gJy4uL3NlcnZpY2VzL2h0dHAvSFRUUE1vZHVsZS5qcyc7XHJcbmltcG9ydCBSZXN0QVBJTW9kdWxlIGZyb20gJy4uL3NlcnZpY2VzL1Jlc3RBUEkvUmVzdEFQSU1vZHVsZS5qcydcclxuaW1wb3J0IEhlYWRlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2hlYWRlci9IZWFkZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTGljZW5zZUFkbWluTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZUFkbWluL0xpY2Vuc2VBZG1pbk1vZHVsZS5qcyc7XHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL0xpY2Vuc2VNYW5hZ2VyTW9kdWxlLmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL05vdGljZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy90YXNrTWFuYWdlci9UYXNrTWFuYWdlck1vZHVsZS5qcyc7XHJcblxyXG52YXIgUHJvdmlkZXJDb3JlID0ge307XHJcblxyXG52YXIgVERTVE0gPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0nLCBbXHJcbiAgICAnbmdTYW5pdGl6ZScsXHJcbiAgICAnbmdSZXNvdXJjZScsXHJcbiAgICAnbmdBbmltYXRlJyxcclxuICAgICdwYXNjYWxwcmVjaHQudHJhbnNsYXRlJywgLy8gJ2FuZ3VsYXItdHJhbnNsYXRlJ1xyXG4gICAgJ3VpLnJvdXRlcicsXHJcbiAgICAnbmdjbGlwYm9hcmQnLFxyXG4gICAgJ2tlbmRvLmRpcmVjdGl2ZXMnLFxyXG4gICAgJ3J4JyxcclxuICAgICdmb3JtbHknLFxyXG4gICAgJ2Zvcm1seUJvb3RzdHJhcCcsXHJcbiAgICAndWkuYm9vdHN0cmFwJyxcclxuICAgIEhUVFBNb2R1bGUubmFtZSxcclxuICAgIFJlc3RBUElNb2R1bGUubmFtZSxcclxuICAgIEhlYWRlck1vZHVsZS5uYW1lLFxyXG4gICAgVGFza01hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIExpY2Vuc2VBZG1pbk1vZHVsZS5uYW1lLFxyXG4gICAgTGljZW5zZU1hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIE5vdGljZU1hbmFnZXJNb2R1bGUubmFtZVxyXG5dKS5jb25maWcoW1xyXG4gICAgJyRsb2dQcm92aWRlcicsXHJcbiAgICAnJHJvb3RTY29wZVByb3ZpZGVyJyxcclxuICAgICckY29tcGlsZVByb3ZpZGVyJyxcclxuICAgICckY29udHJvbGxlclByb3ZpZGVyJyxcclxuICAgICckcHJvdmlkZScsXHJcbiAgICAnJGh0dHBQcm92aWRlcicsXHJcbiAgICAnJHRyYW5zbGF0ZVByb3ZpZGVyJyxcclxuICAgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgICckdXJsUm91dGVyUHJvdmlkZXInLFxyXG4gICAgJyRsb2NhdGlvblByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkbG9nUHJvdmlkZXIsICRyb290U2NvcGVQcm92aWRlciwgJGNvbXBpbGVQcm92aWRlciwgJGNvbnRyb2xsZXJQcm92aWRlciwgJHByb3ZpZGUsICRodHRwUHJvdmlkZXIsXHJcbiAgICAgICAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLCAkdXJsUm91dGVyUHJvdmlkZXIsICRsb2NhdGlvblByb3ZpZGVyKSB7XHJcblxyXG4gICAgICAgICRyb290U2NvcGVQcm92aWRlci5kaWdlc3RUdGwoMzApO1xyXG4gICAgICAgIC8vIEdvaW5nIGJhY2sgdG8geW91XHJcbiAgICAgICAgJGxvY2F0aW9uUHJvdmlkZXIuaHRtbDVNb2RlKHRydWUpLmhhc2hQcmVmaXgoJyEnKTtcclxuXHJcbiAgICAgICAgJGxvZ1Byb3ZpZGVyLmRlYnVnRW5hYmxlZCh0cnVlKTtcclxuXHJcbiAgICAgICAgLy8gQWZ0ZXIgYm9vdHN0cmFwcGluZyBhbmd1bGFyIGZvcmdldCB0aGUgcHJvdmlkZXIgc2luY2UgZXZlcnl0aGluZyBcIndhcyBhbHJlYWR5IGxvYWRlZFwiXHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlciA9ICRjb21waWxlUHJvdmlkZXI7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmNvbnRyb2xsZXJQcm92aWRlciA9ICRjb250cm9sbGVyUHJvdmlkZXI7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlID0gJHByb3ZpZGU7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmh0dHBQcm92aWRlciA9ICRodHRwUHJvdmlkZXI7XHJcblxyXG4gICAgICAgIC8qKlxyXG4gICAgICAgICAqIFRyYW5zbGF0aW9uc1xyXG4gICAgICAgICAqL1xyXG5cclxuICAgICAgICAvKiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnVzZVNhbml0aXplVmFsdWVTdHJhdGVneShudWxsKTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCd0ZHN0bScpO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIudXNlTG9hZGVyKCckdHJhbnNsYXRlUGFydGlhbExvYWRlcicsIHtcclxuICAgICAgICAgICAgdXJsVGVtcGxhdGU6ICcuLi9pMThuL3twYXJ0fS9hcHAuaTE4bi17bGFuZ30uanNvbidcclxuICAgICAgICB9KTsqL1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIucHJlZmVycmVkTGFuZ3VhZ2UoJ2VuX1VTJyk7XHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLmZhbGxiYWNrTGFuZ3VhZ2UoJ2VuX1VTJyk7XHJcblxyXG4gICAgICAgIC8vJHVybFJvdXRlclByb3ZpZGVyLm90aGVyd2lzZSgnZGFzaGJvYXJkJyk7XHJcblxyXG4gICAgfV0pLlxyXG4gICAgcnVuKFsnJHRyYW5zaXRpb25zJywgJyRodHRwJywgJyRsb2cnLCAnJGxvY2F0aW9uJywgJyRxJywnVXNlclByZWZlcmVuY2VzU2VydmljZScsIGZ1bmN0aW9uICgkdHJhbnNpdGlvbnMsICRodHRwLCAkbG9nLCAkbG9jYXRpb24sICRxLCB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlKSB7XHJcbiAgICAgICAgJGxvZy5kZWJ1ZygnQ29uZmlndXJhdGlvbiBkZXBsb3llZCcpO1xyXG5cclxuICAgICAgICAkdHJhbnNpdGlvbnMub25CZWZvcmUoIHt9LCAoJHN0YXRlLCAkdHJhbnNpdGlvbiQpID0+IHtcclxuICAgICAgICAgICAgdmFyIGRlZmVyID0gJHEuZGVmZXIoKTtcclxuXHJcbiAgICAgICAgICAgIHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UuZ2V0VGltZVpvbmVDb25maWd1cmF0aW9uKCgpID0+IHtcclxuICAgICAgICAgICAgICAgIGRlZmVyLnJlc29sdmUoKTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gZGVmZXIucHJvbWlzZTtcclxuICAgICAgICB9KTtcclxuXHJcbiAgICB9XSk7XHJcblxyXG4vLyB3ZSBtYXBwZWQgdGhlIFByb3ZpZGVyIENvcmUgbGlzdCAoY29tcGlsZVByb3ZpZGVyLCBjb250cm9sbGVyUHJvdmlkZXIsIHByb3ZpZGVTZXJ2aWNlLCBodHRwUHJvdmlkZXIpIHRvIHJldXNlIGFmdGVyIG9uIGZseVxyXG5URFNUTS5Qcm92aWRlckNvcmUgPSBQcm92aWRlckNvcmU7XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IFREU1RNOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTQvMjAxNS5cclxuICogSXQgaGFuZGxlciB0aGUgaW5kZXggZm9yIGFueSBvZiB0aGUgZGlyZWN0aXZlcyBhdmFpbGFibGVcclxuICovXHJcblxyXG5yZXF1aXJlKCcuL3Rvb2xzL1RvYXN0SGFuZGxlci5qcycpO1xyXG5yZXF1aXJlKCcuL3Rvb2xzL01vZGFsV2luZG93QWN0aXZhdGlvbi5qcycpO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMwLzEwLzIwMTYuXHJcbiAqIExpc3RlbiB0byBNb2RhbCBXaW5kb3cgdG8gbWFrZSBhbnkgbW9kYWwgd2luZG93IGRyYWdnYWJibGVcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgnbW9kYWxSZW5kZXInLCBbJyRsb2cnLCBmdW5jdGlvbiAoJGxvZykge1xyXG4gICAgJGxvZy5kZWJ1ZygnTW9kYWxXaW5kb3dBY3RpdmF0aW9uIGxvYWRlZCcpO1xyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICByZXN0cmljdDogJ0VBJyxcclxuICAgICAgICBsaW5rOiBmdW5jdGlvbigpIHtcclxuICAgICAgICAgICAgJCgnLm1vZGFsLWRpYWxvZycpLmRyYWdnYWJsZSh7XHJcbiAgICAgICAgICAgICAgICBoYW5kbGU6ICcubW9kYWwtaGVhZGVyJ1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG59XSk7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBQcmludHMgb3V0IGFsbCBUb2FzdCBtZXNzYWdlIHdoZW4gZGV0ZWN0ZWQgZnJvbSBzZXJ2ZXIgb3IgY3VzdG9tIG1zZyB1c2luZyB0aGUgZGlyZWN0aXZlIGl0c2VsZlxyXG4gKlxyXG4gKiBQcm9iYWJseSB2YWx1ZXMgYXJlOlxyXG4gKlxyXG4gKiBzdWNjZXNzLCBkYW5nZXIsIGluZm8sIHdhcm5pbmdcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgndG9hc3RIYW5kbGVyJywgWyckbG9nJywgJyR0aW1lb3V0JywgJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLFxyXG4gICAgJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicsICdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICBmdW5jdGlvbiAoJGxvZywgJHRpbWVvdXQsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLFxyXG4gICAgICAgICAgICAgIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciwgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IpIHtcclxuXHJcbiAgICAkbG9nLmRlYnVnKCdUb2FzdEhhbmRsZXIgbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHNjb3BlOiB7XHJcbiAgICAgICAgICAgIG1zZzogJz0nLFxyXG4gICAgICAgICAgICB0eXBlOiAnPScsXHJcbiAgICAgICAgICAgIHN0YXR1czogJz0nXHJcbiAgICAgICAgfSxcclxuICAgICAgICBwcmlvcml0eTogNSxcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9kaXJlY3RpdmVzL3Rvb2xzL1RvYXN0SGFuZGxlci5odG1sJyxcclxuICAgICAgICByZXN0cmljdDogJ0UnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6IFsnJHNjb3BlJywgJyRyb290U2NvcGUnLCBmdW5jdGlvbiAoJHNjb3BlLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgICAgICRzY29wZS5hbGVydCA9IHtcclxuICAgICAgICAgICAgICAgIHN1Y2Nlc3M6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRpbWU6IDIwMDBcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBkYW5nZXI6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRpbWU6IDQwMDBcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBpbmZvOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJyxcclxuICAgICAgICAgICAgICAgICAgICB0aW1lOiAyMDAwXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgd2FybmluZzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGltZTogNDAwMFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcyA9IHtcclxuICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlXHJcbiAgICAgICAgICAgIH07XHJcblxyXG4gICAgICAgICAgICBmdW5jdGlvbiB0dXJuT2ZmTm90aWZpY2F0aW9ucygpe1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LnN1Y2Nlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuaW5mby5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQud2FybmluZy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogTGlzdGVuIHRvIGFueSByZXF1ZXN0LCB3ZSBjYW4gcmVnaXN0ZXIgbGlzdGVuZXIgaWYgd2Ugd2FudCB0byBhZGQgZXh0cmEgY29kZS5cclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmxpc3RlblJlcXVlc3QoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKGNvbmZpZyl7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXF1ZXN0IHRvOiAnLCAgY29uZmlnKTtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKHRpbWUpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuRXJyb3IoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlamVjdGlvbil7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXF1ZXN0IGVycm9yOiAnLCAgcmVqZWN0aW9uKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmxpc3RlblJlc3BvbnNlKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZXNwb25zZSl7XHJcbiAgICAgICAgICAgICAgICB2YXIgdGltZSA9IHJlc3BvbnNlLmNvbmZpZy5yZXNwb25zZVRpbWVzdGFtcCAtIHJlc3BvbnNlLmNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wO1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnVGhlIHJlcXVlc3QgdG9vayAnICsgKHRpbWUgLyAxMDAwKSArICcgc2Vjb25kcycpO1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVzcG9uc2UgcmVzdWx0OiAnLCByZXNwb25zZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG5cclxuICAgICAgICAgICAgICAgIGlmKHJlc3BvbnNlICYmIHJlc3BvbnNlLmhlYWRlcnMgJiYgcmVzcG9uc2UuaGVhZGVycygneC1sb2dpbi11cmwnKSkge1xyXG4gICAgICAgICAgICAgICAgICAgIHdpbmRvdy5sb2NhdGlvbi5ocmVmID0gcmVzcG9uc2UuaGVhZGVycygneC1sb2dpbi11cmwnKTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1Jlc3BvbnNlIGVycm9yOiAnLCByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnN0YXR1cyA9IHJlamVjdGlvbi5zdGF0dXM7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnN0YXR1c1RleHQgPSByZWplY3Rpb24uc3RhdHVzVGV4dDtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuZXJyb3JzID0gcmVqZWN0aW9uLmRhdGEuZXJyb3JzO1xyXG4gICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsIDMwMDApO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBIaWRlIHRoZSBQb3AgdXAgbm90aWZpY2F0aW9uIG1hbnVhbGx5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUub25DYW5jZWxQb3BVcCA9IGZ1bmN0aW9uKCkge1xyXG4gICAgICAgICAgICAgICAgdHVybk9mZk5vdGlmaWNhdGlvbnMoKTtcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBJdCB3YXRjaCB0aGUgdmFsdWUgdG8gc2hvdyB0aGUgbXNnIGlmIG5lY2Vzc2FyeVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHJvb3RTY29wZS4kb24oJ2Jyb2FkY2FzdC1tc2cnLCBmdW5jdGlvbihldmVudCwgYXJncykge1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnYnJvYWRjYXN0LW1zZyBleGVjdXRlZCcpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zdGF0dXNUZXh0ID0gYXJncy50ZXh0O1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc3RhdHVzID0gbnVsbDtcclxuICAgICAgICAgICAgICAgICR0aW1lb3V0KHR1cm5PZmZOb3RpZmljYXRpb25zLCAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0udGltZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuJGFwcGx5KCk7IC8vIHJvb3RTY29wZSBhbmQgd2F0Y2ggZXhjbHVkZSB0aGUgYXBwbHkgYW5kIG5lZWRzIHRoZSBuZXh0IGN5Y2xlIHRvIHJ1blxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBJdCB3YXRjaCB0aGUgdmFsdWUgdG8gc2hvdyB0aGUgbXNnIGlmIG5lY2Vzc2FyeVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHNjb3BlLiR3YXRjaCgnbXNnJywgZnVuY3Rpb24obmV3VmFsdWUsIG9sZFZhbHVlKSB7XHJcbiAgICAgICAgICAgICAgICBpZiAobmV3VmFsdWUgJiYgbmV3VmFsdWUgIT09ICcnKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0WyRzY29wZS50eXBlXS5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnN0YXR1c1RleHQgPSBuZXdWYWx1ZTtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnN0YXR1cyA9ICRzY29wZS5zdGF0dXM7XHJcbiAgICAgICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsIDI1MDApO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgfV1cclxuICAgIH07XHJcbn1dKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8xNy8yMDE1LlxyXG4gKi9cclxuXHJcbi8vIE1haW4gQW5ndWxhckpzIGNvbmZpZ3VyYXRpb25cclxucmVxdWlyZSgnLi9jb25maWcvQXBwLmpzJyk7XHJcblxyXG4vLyBIZWxwZXJzXHJcbnJlcXVpcmUoJy4vY29uZmlnL0FuZ3VsYXJQcm92aWRlckhlbHBlci5qcycpO1xyXG5cclxuLy8gRGlyZWN0aXZlc1xyXG5yZXF1aXJlKCcuL2RpcmVjdGl2ZXMvaW5kZXgnKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIERpYWxvZ0FjdGlvbiB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMudGl0bGUgPSBwYXJhbXMudGl0bGU7XHJcbiAgICAgICAgdGhpcy5tZXNzYWdlID0gcGFyYW1zLm1lc3NhZ2U7XHJcblxyXG4gICAgfVxyXG4gICAgLyoqXHJcbiAgICAgKiBBY2NjZXB0IGFuZCBDb25maXJtXHJcbiAgICAgKi9cclxuICAgIGNvbmZpcm1BY3Rpb24oKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMi8yMDE1LlxyXG4gKiBIZWFkZXIgQ29udHJvbGxlciBtYW5hZ2UgdGhlIHZpZXcgYXZhaWxhYmxlIG9uIHRoZSBzdGF0ZS5kYXRhXHJcbiAqIC0tLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICogSGVhZGVyIENvbnRyb2xsZXJcclxuICogUGFnZSB0aXRsZSAgICAgICAgICAgICAgICAgICAgICBIb21lIC0+IExheW91dCAtIFN1YiBMYXlvdXRcclxuICpcclxuICogTW9kdWxlIENvbnRyb2xsZXJcclxuICogQ29udGVudFxyXG4gKiAtLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSGVhZGVyQ29udHJvbGxlciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nXHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuXHJcbiAgICAgICAgdGhpcy5wYWdlTWV0YURhdGEgPSB7XHJcbiAgICAgICAgICAgIHRpdGxlOiAnJyxcclxuICAgICAgICAgICAgaW5zdHJ1Y3Rpb246ICcnLFxyXG4gICAgICAgICAgICBtZW51OiBbXVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZUhlYWRlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIZWFkZXIgQ29udHJvbGxlciBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFZlcmlmeSBpZiB3ZSBoYXZlIGEgbWVudSB0byBzaG93IHRvIG1hZGUgaXQgYXZhaWxhYmxlIHRvIHRoZSBWaWV3XHJcbiAgICAgKi9cclxuICAgIHByZXBhcmVIZWFkZXIoKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuc3RhdGUgJiYgdGhpcy5zdGF0ZS4kY3VycmVudCAmJiB0aGlzLnN0YXRlLiRjdXJyZW50LmRhdGEpIHtcclxuICAgICAgICAgICAgdGhpcy5wYWdlTWV0YURhdGEgPSB0aGlzLnN0YXRlLiRjdXJyZW50LmRhdGEucGFnZTtcclxuICAgICAgICAgICAgZG9jdW1lbnQudGl0bGUgPSB0aGlzLnBhZ2VNZXRhRGF0YS50aXRsZTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMS8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IEhlYWRlckNvbnRyb2xsZXIgZnJvbSAnLi9IZWFkZXJDb250cm9sbGVyLmpzJztcclxuaW1wb3J0IERpYWxvZ0FjdGlvbiBmcm9tICcuLi9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmpzJztcclxuXHJcbnZhciBIZWFkZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uSGVhZGVyTW9kdWxlJywgW10pO1xyXG5cclxuSGVhZGVyTW9kdWxlLmNvbnRyb2xsZXIoJ0hlYWRlckNvbnRyb2xsZXInLCBbJyRsb2cnLCAnJHN0YXRlJywgSGVhZGVyQ29udHJvbGxlcl0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5IZWFkZXJNb2R1bGUuY29udHJvbGxlcignRGlhbG9nQWN0aW9uJywgWyckbG9nJywnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIERpYWxvZ0FjdGlvbl0pO1xyXG5cclxuLypcclxuICogRmlsdGVyIGNoYW5nZSB0aGUgZGF0ZSBpbnRvIGEgcHJvcGVyIGZvcm1hdCB0aW1lem9uZSBkYXRlXHJcbiAqL1xyXG5IZWFkZXJNb2R1bGUuZmlsdGVyKCdjb252ZXJ0RGF0ZUludG9UaW1lWm9uZScsIFsnVXNlclByZWZlcmVuY2VzU2VydmljZScsIGZ1bmN0aW9uICh1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlKSB7XHJcbiAgICByZXR1cm4gKGRhdGVTdHJpbmcpID0+IHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UuZ2V0Q29udmVydGVkRGF0ZUludG9UaW1lWm9uZShkYXRlU3RyaW5nKTtcclxufV0pO1xyXG5cclxuSGVhZGVyTW9kdWxlLmZpbHRlcignY29udmVydERhdGVUaW1lSW50b1RpbWVab25lJywgWydVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgZnVuY3Rpb24gKHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UpIHtcclxuICAgIHJldHVybiAoZGF0ZVN0cmluZykgPT4gdXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlVGltZUludG9UaW1lWm9uZShkYXRlU3RyaW5nKTtcclxufV0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgSGVhZGVyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VBZG1pbkxpc3QgZnJvbSAnLi9saXN0L0xpY2Vuc2VBZG1pbkxpc3QuanMnO1xyXG5pbXBvcnQgTGljZW5zZUFkbWluU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZUFkbWluU2VydmljZS5qcyc7XHJcbmltcG9ydCBSZXF1ZXN0TGljZW5zZSBmcm9tICcuL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuanMnO1xyXG5pbXBvcnQgQ3JlYXRlZExpY2Vuc2UgZnJvbSAnLi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmpzJztcclxuaW1wb3J0IEFwcGx5TGljZW5zZUtleSBmcm9tICcuL2FwcGx5TGljZW5zZUtleS9BcHBseUxpY2Vuc2VLZXkuanMnO1xyXG5pbXBvcnQgTWFudWFsbHlSZXF1ZXN0IGZyb20gJy4vbWFudWFsbHlSZXF1ZXN0L01hbnVhbGx5UmVxdWVzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlRGV0YWlsIGZyb20gJy4vZGV0YWlsL0xpY2Vuc2VEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlQWRtaW5Nb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uTGljZW5zZUFkbWluTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLCAnJGxvY2F0aW9uUHJvdmlkZXInLFxyXG5cdFx0ZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLCAkbG9jYXRpb25Qcm92aWRlcikge1xyXG5cclxuXHRcdCR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbGljZW5zZUFkbWluJyk7XHJcblxyXG5cdFx0Ly8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuXHRcdHZhciBoZWFkZXIgPSB7XHJcblx0XHRcdFx0dGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuXHRcdFx0XHRjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcblx0XHR9O1xyXG5cclxuXHRcdCRzdGF0ZVByb3ZpZGVyXHJcblx0XHRcdFx0LnN0YXRlKCdsaWNlbnNlQWRtaW5MaXN0Jywge1xyXG5cdFx0XHRcdFx0XHRkYXRhOiB7cGFnZToge3RpdGxlOiAnQWRtaW5pc3RlciBMaWNlbnNlcycsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydBZG1pbicsICdMaWNlbnNlJywgJ0xpc3QnXX19LFxyXG5cdFx0XHRcdFx0XHR1cmw6ICcvbGljZW5zZS9hZG1pbi9saXN0JyxcclxuXHRcdFx0XHRcdFx0dmlld3M6IHtcclxuXHRcdFx0XHRcdFx0XHRcdCdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuXHRcdFx0XHRcdFx0XHRcdCdib2R5Vmlld0AnOiB7XHJcblx0XHRcdFx0XHRcdFx0XHRcdFx0dGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vbGlzdC9MaWNlbnNlQWRtaW5MaXN0Lmh0bWwnLFxyXG5cdFx0XHRcdFx0XHRcdFx0XHRcdGNvbnRyb2xsZXI6ICdMaWNlbnNlQWRtaW5MaXN0IGFzIGxpY2Vuc2VBZG1pbkxpc3QnXHJcblx0XHRcdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHR9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTGljZW5zZUFkbWluTW9kdWxlLnNlcnZpY2UoJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgJyRyb290U2NvcGUnLCBMaWNlbnNlQWRtaW5TZXJ2aWNlXSk7XHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTGljZW5zZUFkbWluTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlQWRtaW5MaXN0XSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdSZXF1ZXN0TGljZW5zZScsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCBSZXF1ZXN0TGljZW5zZV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignQ3JlYXRlZExpY2Vuc2UnLCBbJyRsb2cnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgQ3JlYXRlZExpY2Vuc2VdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0FwcGx5TGljZW5zZUtleScsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgQXBwbHlMaWNlbnNlS2V5XSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdNYW51YWxseVJlcXVlc3QnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTWFudWFsbHlSZXF1ZXN0XSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlRGV0YWlsJywgWyckbG9nJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIExpY2Vuc2VEZXRhaWxdKTtcclxuXHJcbi8qXHJcbiAqIEZpbHRlciB0byBVUkwgRW5jb2RlIHRleHQgZm9yIHRoZSAnbWFpbHRvJ1xyXG4gKi9cclxuTGljZW5zZUFkbWluTW9kdWxlLmZpbHRlcignZXNjYXBlVVJMRW5jb2RpbmcnLCBmdW5jdGlvbiAoKSB7XHJcblx0cmV0dXJuIGZ1bmN0aW9uICh0ZXh0KSB7XHJcblx0XHRpZih0ZXh0KXtcclxuXHRcdFx0dGV4dCA9IGVuY29kZVVSSSh0ZXh0KTtcclxuXHRcdH1cclxuXHRcdHJldHVybiB0ZXh0O1xyXG5cdH1cclxufSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBMaWNlbnNlQWRtaW5Nb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBBcHBseUxpY2Vuc2VLZXkgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgc3VwZXIoJGxvZywgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKVxyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIGtleTogcGFyYW1zLmxpY2Vuc2Uua2V5XHJcbiAgICAgICAgfVxyXG4gICAgICAgIDtcclxuICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgYW5kIHZhbGlkYXRlIHRoZSBLZXkgaXMgY29ycmVjdFxyXG4gICAgICovXHJcbiAgICBhcHBseUtleSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuYXBwbHlMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9LCAoZGF0YSk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIENyZWF0ZWRSZXF1ZXN0TGljZW5zZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMuY2xpZW50ID0gcGFyYW1zO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZURldGFpbCB7XHJcblxyXG5cdFx0Y29uc3RydWN0b3IoJGxvZywgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcblx0XHRcdFx0dGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuXHRcdFx0XHR0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuXHRcdFx0XHR0aGlzLnVpYk1vZGFsID0kdWliTW9kYWw7XHJcblx0XHRcdFx0dGhpcy5sb2cgPSAkbG9nO1xyXG5cdFx0XHRcdHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG5cdFx0XHRcdFx0XHRtZXRob2Q6IHtcclxuXHRcdFx0XHRcdFx0XHRcdG5hbWU6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5uYW1lLFxyXG5cdFx0XHRcdFx0XHRcdFx0bWF4OiBwYXJhbXMubGljZW5zZS5tZXRob2QubWF4XHJcblx0XHRcdFx0XHRcdH0sXHJcblx0XHRcdFx0XHRcdHByb2plY3ROYW1lOiBwYXJhbXMubGljZW5zZS5wcm9qZWN0Lm5hbWUsXHJcblx0XHRcdFx0XHRcdGNsaWVudE5hbWU6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5uYW1lLFxyXG5cdFx0XHRcdFx0XHRlbWFpbDogcGFyYW1zLmxpY2Vuc2UuZW1haWwsXHJcblx0XHRcdFx0XHRcdHRvRW1haWw6IHBhcmFtcy5saWNlbnNlLnRvRW1haWwsXHJcblx0XHRcdFx0XHRcdGVudmlyb25tZW50OiBwYXJhbXMubGljZW5zZS5lbnZpcm9ubWVudCxcclxuXHRcdFx0XHRcdFx0aW5jZXB0aW9uOiBwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSxcclxuXHRcdFx0XHRcdFx0ZXhwaXJhdGlvbjogcGFyYW1zLmxpY2Vuc2UuZXhwaXJhdGlvbkRhdGUsXHJcblx0XHRcdFx0XHRcdHJlcXVlc3ROb3RlOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0Tm90ZSxcclxuXHRcdFx0XHRcdFx0YWN0aXZlOiBwYXJhbXMubGljZW5zZS5zdGF0dXMgPT09ICdBQ1RJVkUnLFxyXG5cdFx0XHRcdFx0XHRpZDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcblx0XHRcdFx0XHRcdHJlcGxhY2VkOiBwYXJhbXMubGljZW5zZS5yZXBsYWNlZCxcclxuXHRcdFx0XHRcdFx0ZW5jcnlwdGVkRGV0YWlsOiBwYXJhbXMubGljZW5zZS5lbmNyeXB0ZWREZXRhaWwsXHJcblx0XHRcdFx0XHRcdGFwcGxpZWQ6IGZhbHNlXHJcblx0XHRcdFx0fTtcclxuXHJcblx0XHRcdFx0dGhpcy5wcmVwYXJlTWV0aG9kT3B0aW9ucygpO1xyXG5cdFx0fVxyXG5cclxuXHRcdHByZXBhcmVNZXRob2RPcHRpb25zKCkge1xyXG5cdFx0XHRcdHRoaXMubWV0aG9kT3B0aW9ucyA9IFtcclxuXHRcdFx0XHRcdFx0e1xyXG5cdFx0XHRcdFx0XHRcdFx0bmFtZTogJ01BWF9TRVJWRVJTJyxcclxuXHRcdFx0XHRcdFx0XHRcdHRleHQ6ICdTZXJ2ZXJzJ1xyXG5cdFx0XHRcdFx0XHR9LFxyXG5cdFx0XHRcdFx0XHR7XHJcblx0XHRcdFx0XHRcdFx0XHRuYW1lOiAnVE9LRU4nLFxyXG5cdFx0XHRcdFx0XHRcdFx0dGV4dDogJ1Rva2VucydcclxuXHRcdFx0XHRcdFx0fSxcclxuXHRcdFx0XHRcdFx0e1xyXG5cdFx0XHRcdFx0XHRcdFx0bmFtZTogJ0NVU1RPTScsXHJcblx0XHRcdFx0XHRcdFx0XHR0ZXh0OiAnQ3VzdG9tJ1xyXG5cdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0XVxyXG5cdFx0fVxyXG5cclxuXHRcdC8qKlxyXG5cdFx0ICogVGhlIHVzZXIgYXBwbHkgYW5kIHNlcnZlciBzaG91bGQgdmFsaWRhdGUgdGhlIGtleSBpcyBjb3JyZWN0XHJcblx0XHQgKi9cclxuXHRcdGFwcGx5TGljZW5zZUtleSgpIHtcclxuXHRcdFx0XHR2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcblx0XHRcdFx0XHRcdGFuaW1hdGlvbjogdHJ1ZSxcclxuXHRcdFx0XHRcdFx0dGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vYXBwbHlMaWNlbnNlS2V5L0FwcGx5TGljZW5zZUtleS5odG1sJyxcclxuXHRcdFx0XHRcdFx0Y29udHJvbGxlcjogJ0FwcGx5TGljZW5zZUtleSBhcyBhcHBseUxpY2Vuc2VLZXknLFxyXG5cdFx0XHRcdFx0XHRzaXplOiAnbWQnLFxyXG5cdFx0XHRcdFx0XHRyZXNvbHZlOiB7XHJcblx0XHRcdFx0XHRcdFx0XHRwYXJhbXM6ICgpID0+IHtcclxuXHRcdFx0XHRcdFx0XHRcdFx0XHRyZXR1cm4geyBsaWNlbnNlOiB0aGlzLmxpY2Vuc2VNb2RlbCB9O1xyXG5cdFx0XHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0fSk7XHJcblxyXG5cdFx0XHRcdG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuXHRcdFx0XHRcdFx0dGhpcy5saWNlbnNlTW9kZWwuYXBwbGllZCA9IGRhdGEuc3VjY2VzcztcclxuXHRcdFx0XHRcdFx0aWYoZGF0YS5zdWNjZXNzKSB7XHJcblx0XHRcdFx0XHRcdFx0XHR0aGlzLmxpY2Vuc2VNb2RlbC5hY3RpdmUgPSBkYXRhLnN1Y2Nlc3M7XHJcblx0XHRcdFx0XHRcdFx0XHR0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoeyBpZDogdGhpcy5saWNlbnNlTW9kZWwuaWQsIHVwZGF0ZWQ6IHRydWV9KTtcclxuXHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdH0pO1xyXG5cdFx0fVxyXG5cclxuXHRcdC8qKlxyXG5cdFx0ICogT3BlbnMgYSBkaWFsb2cgYW5kIGFsbG93IHRoZSB1c2VyIHRvIG1hbnVhbGx5IHNlbmQgdGhlIHJlcXVlc3Qgb3IgY29weSB0aGUgZW5jcmlwdGVkIGNvZGVcclxuXHRcdCAqL1xyXG5cdFx0bWFudWFsbHlSZXF1ZXN0KCkge1xyXG5cdFx0XHRcdHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuXHRcdFx0XHRcdFx0YW5pbWF0aW9uOiB0cnVlLFxyXG5cdFx0XHRcdFx0XHR0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0Lmh0bWwnLFxyXG5cdFx0XHRcdFx0XHRjb250cm9sbGVyOiAnTWFudWFsbHlSZXF1ZXN0IGFzIG1hbnVhbGx5UmVxdWVzdCcsXHJcblx0XHRcdFx0XHRcdHNpemU6ICdtZCcsXHJcblx0XHRcdFx0XHRcdHJlc29sdmU6IHtcclxuXHRcdFx0XHRcdFx0XHRcdHBhcmFtczogKCkgPT4ge1xyXG5cdFx0XHRcdFx0XHRcdFx0XHRcdHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcblx0XHRcdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHR9KTtcclxuXHJcblx0XHRcdFx0bW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7fSk7XHJcblx0XHR9XHJcblxyXG5cdFx0LyoqXHJcblx0XHQgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuXHRcdCAqL1xyXG5cdFx0cmVzdWJtaXRMaWNlbnNlUmVxdWVzdCgpIHtcclxuXHRcdFx0XHR0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UucmVzdWJtaXRMaWNlbnNlUmVxdWVzdCh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHt9KTtcclxuXHRcdH1cclxuXHJcblx0XHRkZWxldGVMaWNlbnNlKCkge1xyXG5cdFx0XHRcdHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuXHRcdFx0XHRcdFx0YW5pbWF0aW9uOiB0cnVlLFxyXG5cdFx0XHRcdFx0XHR0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcblx0XHRcdFx0XHRcdGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuXHRcdFx0XHRcdFx0c2l6ZTogJ3NtJyxcclxuXHRcdFx0XHRcdFx0cmVzb2x2ZToge1xyXG5cdFx0XHRcdFx0XHRcdFx0cGFyYW1zOiAoKSA9PiB7XHJcblx0XHRcdFx0XHRcdFx0XHRcdFx0cmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnWW91IGFyZSBhYm91dCB0byBkZWxldGUgdGhlIHNlbGVjdGVkIGxpY2Vuc2UuIEFyZSB5b3Ugc3VyZT8gQ2xpY2sgQ29uZmlybSB0byBkZWxldGUgb3RoZXJ3aXNlIHByZXNzIENhbmNlbC4nfTtcclxuXHRcdFx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdH0pO1xyXG5cclxuXHRcdFx0XHRtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuXHRcdFx0XHRcdFx0dGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmRlbGV0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcblx0XHRcdFx0XHRcdFx0XHR0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcblx0XHRcdFx0XHRcdH0pO1xyXG5cdFx0XHRcdH0pO1xyXG5cdFx0fVxyXG5cclxuXHRcdC8qKlxyXG5cdFx0ICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcblx0XHQgKi9cclxuXHRcdGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG5cdFx0XHRcdGlmKHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQpIHtcclxuXHRcdFx0XHRcdFx0dGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKCk7XHJcblx0XHRcdFx0fVxyXG5cdFx0XHRcdHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuXHRcdH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5MaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLm9wZW5MYXN0TGljZW5zZUlkID0gMDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VBZG1pbkxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vblJlcXVlc3ROZXdMaWNlbnNlKClcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gUmVxdWVzdCBOZXcgTGljZW5zZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5yZWxvYWRMaWNlbnNlQWRtaW5MaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMjBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbGljZW5zZUlkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCBmaWx0ZXJhYmxlOiBmYWxzZSwgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vbkxpY2Vuc2VEZXRhaWxzKHRoaXMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjbGllbnQubmFtZScsIHRpdGxlOiAnQ2xpZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLCB0aXRsZTogJ1Byb2plY3QnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEucHJvamVjdCAmJiBkYXRhLnByb2plY3QubmFtZSk/IGRhdGEucHJvamVjdC5uYW1lLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbWFpbCcsIHRpdGxlOiAnQ29udGFjdCBFbWFpbCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3RhdHVzJywgdGl0bGU6ICdTdGF0dXMnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEuc3RhdHVzKT8gZGF0YS5zdGF0dXMudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZScsICB0ZW1wbGF0ZTogJyNpZihkYXRhLnR5cGUgJiYgZGF0YS50eXBlLm5hbWUgPT09IFwiTVVMVElfUFJPSkVDVFwiKXsjIEdsb2JhbCAjIH0gZWxzZSB7IyBTaW5nbGUgI30jJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubmFtZScsIHRpdGxlOiAnTWV0aG9kJywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLm1ldGhvZCAmJiBkYXRhLm1ldGhvZC5uYW1lKT8gZGF0YS5tZXRob2QubmFtZS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm1heCcsIHRpdGxlOiAnU2VydmVyL1Rva2Vucyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aXZhdGlvbkRhdGUnLCB0aXRsZTogJ0luY2VwdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScsIHRlbXBsYXRlOiAne3sgZGF0YUl0ZW0uYWN0aXZhdGlvbkRhdGUgfCBjb252ZXJ0RGF0ZUludG9UaW1lWm9uZSB9fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2V4cGlyYXRpb25EYXRlJywgdGl0bGU6ICdFeHBpcmF0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JywgdGVtcGxhdGU6ICd7eyBkYXRhSXRlbS5leHBpcmF0aW9uRGF0ZSB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW52aXJvbm1lbnQnLCB0aXRsZTogJ0Vudmlyb25tZW50JywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLmVudmlyb25tZW50KT8gZGF0YS5lbnZpcm9ubWVudC50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHNvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICBmaWVsZDogJ3Byb2plY3QubmFtZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGNoYW5nZTogIChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgLy8gV2UgYXJlIGNvbWluZyBmcm9tIGEgbmV3IGltcG9ydGVkIHJlcXVlc3QgbGljZW5zZVxyXG4gICAgICAgICAgICAgICAgICAgIGlmKHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgIT09IDAgJiYgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHZhciBsYXN0TGljZW5zZSA9IHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YS5maW5kKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gbGljZW5zZS5pZCA9PT0gdGhpcy5vcGVuTGFzdExpY2Vuc2VJZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm9wZW5MYXN0TGljZW5zZUlkID0gMDtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGlmKGxhc3RMaWNlbnNlKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm9uTGljZW5zZURldGFpbHMobGFzdExpY2Vuc2UpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgZmlsdGVyYWJsZToge1xyXG4gICAgICAgICAgICAgICAgZXh0cmE6IGZhbHNlXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogT3BlbiBhIGRpYWxvZyB3aXRoIHRoZSBCYXNpYyBGb3JtIHRvIHJlcXVlc3QgYSBOZXcgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBvblJlcXVlc3ROZXdMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9yZXF1ZXN0L1JlcXVlc3RMaWNlbnNlLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnUmVxdWVzdExpY2Vuc2UgYXMgcmVxdWVzdExpY2Vuc2UnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnXHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTmV3IExpY2Vuc2UgQ3JlYXRlZDogJywgbGljZW5zZSk7XHJcbiAgICAgICAgICAgIHRoaXMub25OZXdMaWNlbnNlQ3JlYXRlZChsaWNlbnNlKTtcclxuICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlQWRtaW5MaXN0KCk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdSZXF1ZXN0IENhbmNlbGVkLicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQWZ0ZXIgY2xpY2tpbmcgb24gZWRpdCwgd2UgcmVkaXJlY3QgdGhlIHVzZXIgdG8gdGhlIEVkaXRpb24gc2NyZWVuIGluc3RlYWQgb2Ygb3BlbiBhIGRpYWxvZ1xyXG4gICAgICogZHUgdGhlIHNpemUgb2YgdGhlIGlucHV0c1xyXG4gICAgICovXHJcbiAgICBvbkxpY2Vuc2VEZXRhaWxzKGxpY2Vuc2UpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPcGVuIERldGFpbHMgZm9yOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vZGV0YWlsL0xpY2Vuc2VEZXRhaWwuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlRGV0YWlsIGFzIGxpY2Vuc2VEZXRhaWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbGcnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSB7fTtcclxuICAgICAgICAgICAgICAgICAgICBpZihsaWNlbnNlICYmIGxpY2Vuc2UuZGF0YUl0ZW0pIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGRhdGFJdGVtID0gbGljZW5zZTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgbGljZW5zZTogZGF0YUl0ZW0gfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG4gICAgICAgICAgICBpZihkYXRhLnVwZGF0ZWQpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSBkYXRhLmlkOyAvLyB0YWtlIHRoaXMgcGFyYW0gZnJvbSB0aGUgbGFzdCBpbXBvcnRlZCBsaWNlbnNlLCBvZiBjb3Vyc2VcclxuICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlQWRtaW5MaXN0KCk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdSZXF1ZXN0IENhbmNlbGVkLicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIG9uTmV3TGljZW5zZUNyZWF0ZWQobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vY3JlYXRlZC9DcmVhdGVkTGljZW5zZS5odG1sJyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0NyZWF0ZWRMaWNlbnNlIGFzIGNyZWF0ZWRMaWNlbnNlJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgZW1haWw6IGxpY2Vuc2UuZW1haWwgIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZWxvYWRMaWNlbnNlQWRtaW5MaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTWFudWFsbHlSZXF1ZXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUVtYWlsTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGlkOiAgcGFyYW1zLmxpY2Vuc2UuaWRcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICAvLyBHZXQgdGhlIGhhc2ggY29kZSB1c2luZyB0aGUgaWQuXHJcbiAgICAgICAgdGhpcy5nZXRFbWFpbENvbnRlbnQoKTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0RW1haWxDb250ZW50KCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRFbWFpbENvbnRlbnQodGhpcy5saWNlbnNlRW1haWxNb2RlbC5pZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlRW1haWxNb2RlbCA9IGRhdGE7XHJcbiAgICAgICAgICAgIHdpbmRvdy5URFNUTS5zYWZlQXBwbHkodGhpcy5zY29wZSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICogQ3JlYXRlIGEgbmV3IFJlcXVlc3QgdG8gZ2V0IGEgTGljZW5zZVxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0TGljZW5zZSBleHRlbmRzIEZvcm1WYWxpZGF0b3J7XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJbml0aWFsaXplIGFsbCB0aGUgcHJvcGVydGllc1xyXG4gICAgICogQHBhcmFtICRsb2dcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlQWRtaW5TZXJ2aWNlXHJcbiAgICAgKiBAcGFyYW0gJHVpYk1vZGFsSW5zdGFuY2VcclxuICAgICAqL1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKSB7XHJcbiAgICAgICAgc3VwZXIoJGxvZywgICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIC8vIERlZmluZWQgdGhlIEVudmlyb25tZW50IFNlbGVjdFxyXG4gICAgICAgIHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlID0gW107XHJcbiAgICAgICAgLy8gRGVmaW5lIHRoZSBQcm9qZWN0IFNlbGVjdFxyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdCA9IHt9O1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0gW107XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXRQcm9qZWN0RGF0YVNvdXJjZSgpO1xyXG5cclxuICAgICAgICAvLyBDcmVhdGUgdGhlIE1vZGVsIGZvciB0aGUgTmV3IExpY2Vuc2VcclxuICAgICAgICB0aGlzLm5ld0xpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgZW1haWw6ICcnLFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudDogJycsXHJcbiAgICAgICAgICAgIHByb2plY3RJZDogMCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogJycsXHJcbiAgICAgICAgICAgIHJlcXVlc3ROb3RlOiAnJ1xyXG4gICAgICAgIH1cclxuXHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKT0+e1xyXG4gICAgICAgICAgICB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSA9IGRhdGE7XHJcbiAgICAgICAgICAgIGlmKHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgICAgICB2YXIgaW5kZXggPSB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZS5maW5kSW5kZXgoZnVuY3Rpb24oZW52aXJvbWVudCl7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGVudmlyb21lbnQgID09PSAnUFJPRFVDVElPTic7XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgIGluZGV4ID0gaW5kZXggfHwgMDtcclxuICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLmVudmlyb25tZW50ID0gZGF0YVtpbmRleF07XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBQcm9qZWN0IGRyb3Bkb3duIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3RMaXN0T3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldFByb2plY3REYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm5ld0xpY2Vuc2VNb2RlbC5wcm9qZWN0SWQgPSBkYXRhWzBdLmlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLm5ld0xpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGF0YVRleHRGaWVsZDogJ25hbWUnLFxyXG4gICAgICAgICAgICBkYXRhVmFsdWVGaWVsZDogJ2lkJyxcclxuICAgICAgICAgICAgdmFsdWVQcmltaXRpdmU6IHRydWUsXHJcbiAgICAgICAgICAgIHNlbGVjdDogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAvLyBPbiBQcm9qZWN0IENoYW5nZSwgc2VsZWN0IHRoZSBDbGllbnQgTmFtZVxyXG4gICAgICAgICAgICAgICAgdmFyIGl0ZW0gPSB0aGlzLnNlbGVjdFByb2plY3QuZGF0YUl0ZW0oZS5pdGVtKTtcclxuICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLmNsaWVudE5hbWUgPSBpdGVtLmNsaWVudC5uYW1lO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIHRoZSBTZXJ2aWNlIGNhbGwgdG8gZ2VuZXJhdGUgYSBuZXcgTGljZW5zZSByZXF1ZXN0XHJcbiAgICAgKi9cclxuICAgIHNhdmVMaWNlbnNlUmVxdWVzdCgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdOZXcgTGljZW5zZSBSZXF1ZXN0ZWQ6ICcsIHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0KHRoaXMubmV3TGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VBZG1pblNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlciwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG4gICAgICAgIHRoaXMucm9vdFNjb3BlID0gJHJvb3RTY29wZTtcclxuICAgICAgICB0aGlzLnN0YXR1c1N1Y2Nlc3MgPSAnc3VjY2Vzcyc7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ2xpY2Vuc2VBZG1pblNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0TGljZW5zZUxpc3Qob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2Uob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0UHJvamVjdERhdGFTb3VyY2Uob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldFByb2plY3REYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRIYXNoQ29kZShsaWNlbnNlSWQsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRIYXNoQ29kZShsaWNlbnNlSWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRFbWFpbENvbnRlbnQobGljZW5zZUlkLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0RW1haWxDb250ZW50KGxpY2Vuc2VJZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgb25TdWNjZXNzKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5yZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KGxpY2Vuc2UuaWQsIChkYXRhKSA9PiB7XHJcblxyXG4gICAgICAgICAgICBpZihkYXRhLnN0YXR1cyA9PT0gdGhpcy5zdGF0dXNTdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnUmVxdWVzdCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHknfSk7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ3dhcm5pbmcnLCB0ZXh0OiBkYXRhLmRhdGF9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGVtYWlsUmVxdWVzdChsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5lbWFpbFJlcXVlc3QobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ1JlcXVlc3QgTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5Lid9KTtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogIEFwcGx5IFRoZSBMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIG9uU3VjY2Vzc1xyXG4gICAgICovXHJcbiAgICBhcHBseUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcblxyXG4gICAgICAgIHZhciBoYXNoID0gIHtcclxuICAgICAgICAgICAgaGFzaDogbGljZW5zZS5rZXlcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuYXBwbHlMaWNlbnNlKGxpY2Vuc2UuaWQsIGhhc2gsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkgYXBwbGllZCd9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6ICdMaWNlbnNlIHdhcyBub3QgYXBwbGllZCd9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHsgc3VjY2VzczogZmFsc2V9KTtcclxuICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2Vzcyh7IHN1Y2Nlc3M6IHRydWV9KTtcclxuXHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZGVsZXRlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlckxpc3QgZnJvbSAnLi9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlclNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL0xpY2Vuc2VNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBSZXF1ZXN0SW1wb3J0IGZyb20gJy4vcmVxdWVzdEltcG9ydC9SZXF1ZXN0SW1wb3J0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyRGV0YWlsIGZyb20gJy4vZGV0YWlsL0xpY2Vuc2VNYW5hZ2VyRGV0YWlsLmpzJztcclxuXHJcblxyXG52YXIgTGljZW5zZU1hbmFnZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uTGljZW5zZU1hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ2xpY2Vuc2VNYW5hZ2VyJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdsaWNlbnNlTWFuYWdlckxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdMaWNlbnNpbmcgTWFuYWdlcicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydNYW5hZ2VyJywgJ0xpY2Vuc2UnLCAnTGlzdCddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy9saWNlbnNlL21hbmFnZXIvbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvbGlzdC9MaWNlbnNlTWFuYWdlckxpc3QuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VNYW5hZ2VyTGlzdCBhcyBsaWNlbnNlTWFuYWdlckxpc3QnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuc2VydmljZSgnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsICckcm9vdFNjb3BlJywgTGljZW5zZU1hbmFnZXJTZXJ2aWNlXSk7XHJcblxyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZU1hbmFnZXJMaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTGljZW5zZU1hbmFnZXJMaXN0XSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ1JlcXVlc3RJbXBvcnQnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCBSZXF1ZXN0SW1wb3J0XSk7XHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VNYW5hZ2VyRGV0YWlsJywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCAnVXNlclByZWZlcmVuY2VzU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTGljZW5zZU1hbmFnZXJEZXRhaWxdKTtcclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBMaWNlbnNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyRGV0YWlsIGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgdXNlclByZWZlcmVuY2VzU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zLCB0aW1lWm9uZUNvbmZpZ3VyYXRpb24pIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpO1xyXG4gICAgICAgIHRoaXMuc2NvcGUgPSAkc2NvcGU7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UgPSBsaWNlbnNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51c2VyUHJlZmVyZW5jZXNTZXJ2aWNlID0gdXNlclByZWZlcmVuY2VzU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0kdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcblxyXG4gICAgICAgIHRoaXMudGltZVpvbmVDb25maWd1cmF0aW9uID0gdGltZVpvbmVDb25maWd1cmF0aW9uO1xyXG5cclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG4gICAgICAgICAgICBvd25lck5hbWU6IHBhcmFtcy5saWNlbnNlLm93bmVyLm5hbWUsXHJcbiAgICAgICAgICAgIGVtYWlsOiBwYXJhbXMubGljZW5zZS5lbWFpbCxcclxuICAgICAgICAgICAgcHJvamVjdDoge1xyXG4gICAgICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLnByb2plY3QuaWQsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiBwYXJhbXMubGljZW5zZS5wcm9qZWN0Lm5hbWUsXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNsaWVudElkOiBwYXJhbXMubGljZW5zZS5jbGllbnQuaWQsXHJcbiAgICAgICAgICAgIGNsaWVudE5hbWU6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5uYW1lLFxyXG4gICAgICAgICAgICBzdGF0dXM6IHBhcmFtcy5saWNlbnNlLnN0YXR1cyxcclxuICAgICAgICAgICAgbWV0aG9kOiB7XHJcbiAgICAgICAgICAgICAgICBuYW1lOiBwYXJhbXMubGljZW5zZS5tZXRob2QubmFtZSxcclxuICAgICAgICAgICAgICAgIG1heDogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLm1heCxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnQ6IHBhcmFtcy5saWNlbnNlLmVudmlyb25tZW50LFxyXG4gICAgICAgICAgICByZXF1ZXN0RGF0ZTogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdERhdGUsXHJcbiAgICAgICAgICAgIGluaXREYXRlOiAocGFyYW1zLmxpY2Vuc2UuYWN0aXZhdGlvbkRhdGUgIT09IG51bGwpPyBhbmd1bGFyLmNvcHkocGFyYW1zLmxpY2Vuc2UuYWN0aXZhdGlvbkRhdGUpIDogJycsXHJcbiAgICAgICAgICAgIGVuZERhdGU6IChwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSAhPT0gbnVsbCk/IGFuZ3VsYXIuY29weShwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSkgOiAnJyxcclxuICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdE5vdGUsXHJcbiAgICAgICAgICAgIHdlYnNpdGVOYW1lOiBwYXJhbXMubGljZW5zZS53ZWJzaXRlbmFtZSxcclxuXHJcbiAgICAgICAgICAgIGJhbm5lck1lc3NhZ2U6IHBhcmFtcy5saWNlbnNlLmJhbm5lck1lc3NhZ2UsXHJcbiAgICAgICAgICAgIHJlcXVlc3RlZElkOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0ZWRJZCxcclxuICAgICAgICAgICAgcmVwbGFjZWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkLFxyXG4gICAgICAgICAgICByZXBsYWNlZElkOiBwYXJhbXMubGljZW5zZS5yZXBsYWNlZElkLFxyXG4gICAgICAgICAgICBob3N0TmFtZTogcGFyYW1zLmxpY2Vuc2UuaG9zdE5hbWUsXHJcbiAgICAgICAgICAgIGhhc2g6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG4gICAgICAgICAgICBncmFjZVBlcmlvZERheXM6IHBhcmFtcy5saWNlbnNlLmdyYWNlUGVyaW9kRGF5cyxcclxuXHJcbiAgICAgICAgICAgIGFwcGxpZWQ6IHBhcmFtcy5saWNlbnNlLmFwcGxpZWQsXHJcbiAgICAgICAgICAgIGtleUlkOiBwYXJhbXMubGljZW5zZS5rZXlJZFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZUtleSA9ICdMaWNlbnNlcyBoYXMgbm90IGJlZW4gaXNzdWVkJztcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgRW52aXJvbm1lbnQgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RFbnZpcm9ubWVudCA9IHt9O1xyXG4gICAgICAgIHRoaXMuc2VsZWN0RW52aXJvbm1lbnRMaXN0T3B0aW9ucyA9IFtdO1xyXG4gICAgICAgIHRoaXMuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIERlZmluZWQgdGhlIFN0YXR1cyBTZWxlY3QgTGlzdFxyXG4gICAgICAgIHRoaXMuc2VsZWN0U3RhdHVzID0gW107XHJcblxyXG4gICAgICAgIC8vIEluaXQgdGhlIHR3byBLZW5kbyBEYXRlcyBmb3IgSW5pdCBhbmQgRW5kRGF0ZVxyXG4gICAgICAgIHRoaXMuaW5pdERhdGUgPSB7fTtcclxuICAgICAgICB0aGlzLmluaXREYXRlT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZm9ybWF0OiB0aGlzLnVzZXJQcmVmZXJlbmNlc1NlcnZpY2UuZ2V0Q29udmVydGVkRGF0ZUZvcm1hdFRvS2VuZG9EYXRlKCksXHJcbiAgICAgICAgICAgIG9wZW46ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUluaXREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pLFxyXG4gICAgICAgICAgICBjaGFuZ2U6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUluaXREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5lbmREYXRlID0ge307XHJcbiAgICAgICAgdGhpcy5lbmREYXRlT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZm9ybWF0OiB0aGlzLnVzZXJQcmVmZXJlbmNlc1NlcnZpY2UuZ2V0Q29udmVydGVkRGF0ZUZvcm1hdFRvS2VuZG9EYXRlKCksXHJcbiAgICAgICAgICAgIG9wZW46ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUVuZERhdGUoKTtcclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgICAgIGNoYW5nZTogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlRW5kRGF0ZSgpO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcblxyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVNZXRob2RPcHRpb25zKCk7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlTGljZW5zZUtleSgpO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUFjdGl2aXR5TGlzdCgpO1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG5cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENvbnRyb2xzIHdoYXQgYnV0dG9ucyB0byBzaG93XHJcbiAgICAgKi9cclxuICAgIHByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpIHtcclxuICAgICAgICB0aGlzLnBlbmRpbmdMaWNlbnNlID0gdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnUEVORElORycgJiYgIXRoaXMuZWRpdE1vZGU7XHJcbiAgICAgICAgdGhpcy5leHBpcmVkT3JUZXJtaW5hdGVkID0gKHRoaXMubGljZW5zZU1vZGVsLnN0YXR1cyA9PT0gJ0VYUElSRUQnIHx8IHRoaXMubGljZW5zZU1vZGVsLnN0YXR1cyA9PT0gJ1RFUk1JTkFURUQnKTtcclxuICAgICAgICB0aGlzLmFjdGl2ZVNob3dNb2RlID0gdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnQUNUSVZFJyAmJiAhdGhpcy5leHBpcmVkT3JUZXJtaW5hdGVkICYmICF0aGlzLmVkaXRNb2RlO1xyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVNZXRob2RPcHRpb25zKCkge1xyXG4gICAgICAgIHRoaXMubWV0aG9kT3B0aW9ucyA9IFtcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgbmFtZTogJ01BWF9TRVJWRVJTJyxcclxuICAgICAgICAgICAgICAgIHRleHQ6ICdTZXJ2ZXJzJyxcclxuICAgICAgICAgICAgICAgIG1heDogMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnVE9LRU4nLFxyXG4gICAgICAgICAgICAgICAgdGV4dDogJ1Rva2VucycsXHJcbiAgICAgICAgICAgICAgICBtYXg6IDBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgbmFtZTogJ0NVU1RPTScsXHJcbiAgICAgICAgICAgICAgICB0ZXh0OiAnQ3VzdG9tJ1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgXVxyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVMaWNlbnNlS2V5KCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZU1vZGVsLnN0YXR1cyA9PT0gJ0FDVElWRScpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0S2V5Q29kZSh0aGlzLmxpY2Vuc2VNb2RlbC5pZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIGlmKGRhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VLZXkgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgICAgIHdpbmRvdy5URFNUTS5zYWZlQXBwbHkodGhpcy5zY29wZSk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlQWN0aXZpdHlMaXN0KCkge1xyXG5cclxuICAgICAgICB0aGlzLmFjdGl2aXR5R3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAyMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdkYXRlQ3JlYXRlZCcsIHRpdGxlOiAnRGF0ZScsIHdpZHRoOjE2MCwgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXkgaDptbTpzcyB0dH0nLCB0ZW1wbGF0ZTogJ3t7IGRhdGFJdGVtLmRhdGVDcmVhdGVkIHwgY29udmVydERhdGVUaW1lSW50b1RpbWVab25lIH19JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXV0aG9yLnBlcnNvbk5hbWUnLCB0aXRsZTogJ1dob20nLCAgd2lkdGg6MTYwfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NoYW5nZXMnLCB0aXRsZTogJ0FjdGlvbicsIHRlbXBsYXRlOiAnPHRhYmxlIGNsYXNzPVwiaW5uZXItYWN0aXZpdHlfdGFibGVcIj48dGJvZHk+PHRyPjx0ZD48L3RkPjx0ZCBjbGFzcz1cImNvbC1hY3Rpb25fdGRcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tbWludXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC90ZD48dGQgY2xhc3M9XCJjb2wtYWN0aW9uX3RkXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC90ZD48L3RyPiNmb3IodmFyIGkgPSAwOyBpIDwgZGF0YS5jaGFuZ2VzLmxlbmd0aDsgaSsrKXsjPHRyPjx0ZCBzdHlsZT1cImZvbnQtd2VpZ2h0OiBib2xkO1wiPiM9ZGF0YS5jaGFuZ2VzW2ldLmZpZWxkIyA8L3RkPjx0ZCBjbGFzcz1cImNvbC12YWx1ZV90ZFwiPjxzcGFuIGNsYXNzPVwiYWN0aXZpdHktbGlzdC1vbGQtdmFsXCIgc3R5bGU9XCJjb2xvcjpkYXJrcmVkOyBmb250LXdlaWdodDogYm9sZDtcIj57eyBcXCcjPWRhdGEuY2hhbmdlc1tpXS5vbGRWYWx1ZSNcXCcgfCBjb252ZXJ0RGF0ZUludG9UaW1lWm9uZSB9fTwvc3Bhbj48L3RkPjx0ZCBjbGFzcz1cImNvbC12YWx1ZV90ZFwiPjxzcGFuIGNsYXNzPVwiYWN0aXZpdHktbGlzdC1uZXctdmFsXCIgc3R5bGU9XCJjb2xvcjogZ3JlZW47IGZvbnQtd2VpZ2h0OiBib2xkO1wiPnt7IFxcJyM9ZGF0YS5jaGFuZ2VzW2ldLm5ld1ZhbHVlI1xcJyB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19PC90ZD48L3RyPiN9IzwvdGJvZHk+PC90YWJsZT4nfSxcclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0QWN0aXZpdHlMb2codGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHNvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICBmaWVsZDogJ2RhdGVDcmVhdGVkJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNjcm9sbGFibGU6IHRydWVcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcbiAgICAgKi9cclxuICAgIGFjdGl2YXRlTGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5hY3RpdmF0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIGlmIChkYXRhKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXMgPSAnQUNUSVZFJztcclxuICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuICAgICAgICAgICAgICAgIHRoaXMucHJlcGFyZUxpY2Vuc2VLZXkoKTtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJldm9rZUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG4gICAgICAgICAgICBzaXplOiAnc20nLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsIG1lc3NhZ2U6ICdBcmUgeW91IHN1cmUgeW91IHdhbnQgdG8gcmV2b2tlIGl0PyBUaGlzIGFjdGlvbiBjYW5ub3QgYmUgdW5kb25lLid9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5yZXZva2VMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnWW91IGFyZSBhYm91dCB0byBkZWxldGUgdGhlIHNlbGVjdGVkIGxpY2Vuc2UuIEFyZSB5b3Ugc3VyZT8gQ2xpY2sgQ29uZmlybSB0byBkZWxldGUgb3RoZXJ3aXNlIHByZXNzIENhbmNlbC4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZGVsZXRlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIC8qKlxyXG4gICAgICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcbiAgICAgKi9cclxuICAgIG1hbnVhbGx5UmVxdWVzdCgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5tYW51YWxseVJlcXVlc3QodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWYWxpZGF0ZSB0aGUgaW5wdXQgb24gU2VydmVyIG9yIFRva2VucyBpcyBvbmx5IGludGVnZXIgb25seVxyXG4gICAgICogVGhpcyB3aWxsIGJlIGNvbnZlcnRlZCBpbiBhIG1vcmUgY29tcGxleCBkaXJlY3RpdmUgbGF0ZXJcclxuICAgICAqIFRPRE86IENvbnZlcnQgaW50byBhIGRpcmVjdGl2ZVxyXG4gICAgICovXHJcbiAgICB2YWxpZGF0ZUludGVnZXJPbmx5KGUsbW9kZWwpe1xyXG4gICAgICAgIHRyeSB7XHJcbiAgICAgICAgICAgIHZhciBuZXdWYWw9IHBhcnNlSW50KG1vZGVsKTtcclxuICAgICAgICAgICAgaWYoIWlzTmFOKG5ld1ZhbCkpIHtcclxuICAgICAgICAgICAgICAgIG1vZGVsID0gbmV3VmFsO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgbW9kZWwgPSAwO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIGlmKGUgJiYgZS5jdXJyZW50VGFyZ2V0KSB7XHJcbiAgICAgICAgICAgICAgICBlLmN1cnJlbnRUYXJnZXQudmFsdWUgPSBtb2RlbDtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICB0aGlzLiRsb2cud2FybignSW52YWxpZCBOdW1iZXIgRXhjZXB0aW9uJywgbW9kZWwpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgY3VycmVudCBjaGFuZ2VzXHJcbiAgICAgKi9cclxuICAgIHNhdmVMaWNlbnNlKCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2Uuc2F2ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlbG9hZFJlcXVpcmVkID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKTtcclxuICAgICAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ0xpY2Vuc2UgU2F2ZWQnKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpXHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2hhbmdlIHRoZSBzdGF0dXMgdG8gRWRpdFxyXG4gICAgICovXHJcbiAgICBtb2RpZnlMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSB0cnVlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0RW52aXJvbm1lbnRMaXN0T3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZighdGhpcy5saWNlbnNlTW9kZWwuZW52aXJvbm1lbnQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudCA9IGRhdGFbMF07XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgdmFsdWVUZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEpPyBkYXRhLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+JyxcclxuICAgICAgICAgICAgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhKT8gZGF0YS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPicsXHJcbiAgICAgICAgICAgIHZhbHVlUHJpbWl0aXZlOiB0cnVlXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBvbkNoYW5nZUluaXREYXRlKCkge1xyXG4gICAgICAgIHZhciBzdGFydERhdGUgPSB0aGlzLmluaXREYXRlLnZhbHVlKCksXHJcbiAgICAgICAgICAgIGVuZERhdGUgPSB0aGlzLmVuZERhdGUudmFsdWUoKTtcclxuXHJcbiAgICAgICAgaWYgKHN0YXJ0RGF0ZSkge1xyXG4gICAgICAgICAgICBzdGFydERhdGUgPSBuZXcgRGF0ZShzdGFydERhdGUpO1xyXG4gICAgICAgICAgICBzdGFydERhdGUuc2V0RGF0ZShzdGFydERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihzdGFydERhdGUpO1xyXG5cclxuICAgICAgICAgICAgaWYoZW5kRGF0ZSkge1xyXG4gICAgICAgICAgICAgICAgaWYodGhpcy5pbml0RGF0ZS52YWx1ZSgpID4gdGhpcy5lbmREYXRlLnZhbHVlKCkpIHtcclxuICAgICAgICAgICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgZW5kRGF0ZS5zZXREYXRlKHN0YXJ0RGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmVuZERhdGUgPSBlbmREYXRlO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIG9uQ2hhbmdlRW5kRGF0ZSgpe1xyXG4gICAgICAgIHZhciBlbmREYXRlID0gdGhpcy5lbmREYXRlLnZhbHVlKCksXHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZSA9IHRoaXMuaW5pdERhdGUudmFsdWUoKTtcclxuXHJcbiAgICAgICAgaWYgKGVuZERhdGUpIHtcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKGVuZERhdGUpO1xyXG4gICAgICAgICAgICBlbmREYXRlLnNldERhdGUoZW5kRGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgIH0gZWxzZSBpZiAoc3RhcnREYXRlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4obmV3IERhdGUoc3RhcnREYXRlKSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKCk7XHJcbiAgICAgICAgICAgIHRoaXMuaW5pdERhdGUubWF4KGVuZERhdGUpO1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKGVuZERhdGUpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICBpZih0aGlzLmVkaXRNb2RlKSB7XHJcbiAgICAgICAgICAgIHRoaXMucmVzZXRGb3JtKCgpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vblJlc2V0Rm9ybSgpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2UgaWYodGhpcy5yZWxvYWRSZXF1aXJlZCl7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSh7fSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERlcGVuZGluZyB0aGUgbnVtYmVyIG9mIGZpZWxkcyBhbmQgdHlwZSBvZiBmaWVsZCwgdGhlIHJlc2V0IGNhbid0IGJlIG9uIHRoZSBGb3JtVmFsaWRvciwgYXQgbGVhc3Qgbm90IG5vd1xyXG4gICAgICovXHJcbiAgICBvblJlc2V0Rm9ybSgpIHtcclxuICAgICAgICB0aGlzLnJlc2V0RHJvcERvd24odGhpcy5zZWxlY3RFbnZpcm9ubWVudCwgdGhpcy5saWNlbnNlTW9kZWwuZW52aXJvbm1lbnQpO1xyXG4gICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTWFudWFsIHJlbG9hZCBhZnRlciBhIGNoYW5nZSBoYXMgYmVlbiBwZXJmb3JtZWQgdG8gdGhlIExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgcmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMuYWN0aXZpdHlHcmlkLmRhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIC8vdGhpcy5nZXRMaWNlbnNlTGlzdCgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTWFuYWdlckxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gMDtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0Lm9uUmVxdWVzdEltcG9ydExpY2Vuc2UoKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBJbXBvcnQgTGljZW5zZSBSZXF1ZXN0PC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3QucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMjBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIGZpbHRlcmFibGU6IGZhbHNlLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3Qub25MaWNlbnNlTWFuYWdlckRldGFpbHModGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ293bmVyLm5hbWUnLCB0aXRsZTogJ093bmVyJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd3ZWJzaXRlbmFtZScsIHRpdGxlOiAnV2Vic2l0ZSBOYW1lJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjbGllbnQubmFtZScsIHRpdGxlOiAnQ2xpZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLCB0aXRsZTogJ1Byb2plY3QnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEucHJvamVjdCAmJiBkYXRhLnByb2plY3QubmFtZSk/IGRhdGEucHJvamVjdC5uYW1lLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbWFpbCcsIHRpdGxlOiAnQ29udGFjdCBFbWFpbCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3RhdHVzJywgdGl0bGU6ICdTdGF0dXMnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEuc3RhdHVzKT8gZGF0YS5zdGF0dXMudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZScsICB0ZW1wbGF0ZTogJyNpZihkYXRhLnR5cGUgJiYgZGF0YS50eXBlLm5hbWUgPT09IFwiTVVMVElfUFJPSkVDVFwiKXsjIEdsb2JhbCAjIH0gZWxzZSB7IyBTaW5nbGUgI30jJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubmFtZScsIHRpdGxlOiAnTWV0aG9kJywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLm1ldGhvZCAmJiBkYXRhLm1ldGhvZC5uYW1lKT8gZGF0YS5tZXRob2QubmFtZS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm1heCcsIHRpdGxlOiAnU2VydmVyL1Rva2Vucyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aXZhdGlvbkRhdGUnLCB0aXRsZTogJ0luY2VwdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScsIHRlbXBsYXRlOiAne3sgZGF0YUl0ZW0uYWN0aXZhdGlvbkRhdGUgfCBjb252ZXJ0RGF0ZUludG9UaW1lWm9uZSB9fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2V4cGlyYXRpb25EYXRlJywgdGl0bGU6ICdFeHBpcmF0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JywgdGVtcGxhdGU6ICd7eyBkYXRhSXRlbS5leHBpcmF0aW9uRGF0ZSB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW52aXJvbm1lbnQnLCB0aXRsZTogJ0Vudmlyb25tZW50JywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLmVudmlyb25tZW50KT8gZGF0YS5lbnZpcm9ubWVudC50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOidncmFjZVBlcmlvZERheXMnLCBoaWRkZW46IHRydWV9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBjaGFuZ2U6ICAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIC8vIFdlIGFyZSBjb21pbmcgZnJvbSBhIG5ldyBpbXBvcnRlZCByZXF1ZXN0IGxpY2Vuc2VcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgIT09IDAgJiYgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHZhciBuZXdMaWNlbnNlQ3JlYXRlZCA9IHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YS5maW5kKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gbGljZW5zZS5pZCA9PT0gdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IDA7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICBpZihuZXdMaWNlbnNlQ3JlYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyhuZXdMaWNlbnNlQ3JlYXRlZCk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGUgdXNlciBJbXBvcnQgYSBuZXcgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBvblJlcXVlc3RJbXBvcnRMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL3JlcXVlc3RJbXBvcnQvUmVxdWVzdEltcG9ydC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1JlcXVlc3RJbXBvcnQgYXMgcmVxdWVzdEltcG9ydCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IGxpY2Vuc2VJbXBvcnRlZC5pZDsgLy8gdGFrZSB0aGlzIHBhcmFtIGZyb20gdGhlIGxhc3QgaW1wb3J0ZWQgbGljZW5zZSwgb2YgY291cnNlXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKGxpY2Vuc2UpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPcGVuIERldGFpbHMgZm9yOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlTWFuYWdlckRldGFpbCBhcyBsaWNlbnNlTWFuYWdlckRldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgcmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SW1wb3J0IGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBoYXNoOiAnJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIG9uSW1wb3J0TGljZW5zZSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5pbXBvcnRMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UobGljZW5zZUltcG9ydGVkLmRhdGEpO1xyXG4gICAgICAgICAgICB9LCAobGljZW5zZUltcG9ydGVkKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShsaWNlbnNlSW1wb3J0ZWQuZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlciwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG4gICAgICAgIHRoaXMucm9vdFNjb3BlID0gJHJvb3RTY29wZTtcclxuICAgICAgICB0aGlzLnN0YXR1c1N1Y2Nlc3MgPSAnc3VjY2Vzcyc7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ2xpY2Vuc2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG5cclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0S2V5Q29kZShsaWNlbnNlSWQsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEtleUNvZGUobGljZW5zZUlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0VGltZVpvbmVDb25maWd1cmF0aW9uKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuY29tbW9uU2VydmljZUhhbmRsZXIoKS5nZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIG1hbnVhbGx5UmVxdWVzdChsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5tYW51YWxseVJlcXVlc3QobGljZW5zZS5pZCwgKGRhdGEpID0+IHtcclxuXHJcbiAgICAgICAgICAgIGlmIChkYXRhLnN0YXR1cyA9PT0gdGhpcy5zdGF0dXNTdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnRW1haWwgTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5JyB9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6IGRhdGEuZGF0YSB9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoeyBzdWNjZXNzOiBmYWxzZSB9KTtcclxuICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgdGhlIExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcblxyXG4gICAgICAgIHZhciBsaWNlbnNlTW9kaWZpZWQgPSB7XHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiBsaWNlbnNlLmVudmlyb25tZW50LFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIG5hbWU6IGxpY2Vuc2UubWV0aG9kLm5hbWVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGlvbkRhdGU6IGxpY2Vuc2UuaW5pdERhdGUgPyBtb21lbnQobGljZW5zZS5pbml0RGF0ZSkuZm9ybWF0KCdZWVlZLU1NLUREJykgOiAnJyxcclxuICAgICAgICAgICAgZXhwaXJhdGlvbkRhdGU6IGxpY2Vuc2UuZW5kRGF0ZSA/IG1vbWVudChsaWNlbnNlLmVuZERhdGUpLmZvcm1hdCgnWVlZWS1NTS1ERCcpIDogJycsXHJcbiAgICAgICAgICAgIHN0YXR1czogbGljZW5zZS5zdGF0dXMsXHJcbiAgICAgICAgICAgIHByb2plY3Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiAobGljZW5zZS5wcm9qZWN0LmlkICE9PSAnYWxsJykgPyBwYXJzZUludChsaWNlbnNlLnByb2plY3QuaWQpIDogbGljZW5zZS5wcm9qZWN0LmlkLCAgLy8gV2UgcGFzcyAnYWxsJyB3aGVuIGlzIG11bHRpcHJvamVjdFxyXG4gICAgICAgICAgICAgICAgbmFtZTogbGljZW5zZS5wcm9qZWN0Lm5hbWVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogbGljZW5zZS5iYW5uZXJNZXNzYWdlLFxyXG4gICAgICAgICAgICBncmFjZVBlcmlvZERheXM6IGxpY2Vuc2UuZ3JhY2VQZXJpb2REYXlzLFxyXG4gICAgICAgICAgICB3ZWJzaXRlbmFtZTogbGljZW5zZS53ZWJzaXRlTmFtZSxcclxuICAgICAgICAgICAgaG9zdE5hbWU6IGxpY2Vuc2UuaG9zdE5hbWVcclxuICAgICAgICB9O1xyXG4gICAgICAgIGlmIChsaWNlbnNlLm1ldGhvZC5uYW1lICE9PSAnQ1VTVE9NJykge1xyXG4gICAgICAgICAgICBsaWNlbnNlTW9kaWZpZWQubWV0aG9kLm1heCA9IHBhcnNlSW50KGxpY2Vuc2UubWV0aG9kLm1heCk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5zYXZlTGljZW5zZShsaWNlbnNlLmlkLCBsaWNlbnNlTW9kaWZpZWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbiAgICAvKipcclxuICAgICAqIERvZXMgdGhlIGFjdGl2YXRpb24gb2YgdGhlIGN1cnJlbnQgbGljZW5zZSBpZiB0aGlzIGlzIG5vdCBhY3RpdmVcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgYWN0aXZhdGVMaWNlbnNlKGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuYWN0aXZhdGVMaWNlbnNlKGxpY2Vuc2UuaWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIGlmIChkYXRhLnN0YXR1cyA9PT0gdGhpcy5zdGF0dXNTdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHtcclxuICAgICAgICAgICAgICAgICAgICB0eXBlOiAnaW5mbycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogJ1RoZSBsaWNlbnNlIHdhcyBhY3RpdmF0ZWQgYW5kIHRoZSBsaWNlbnNlIHdhcyBlbWFpbGVkLidcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ3dhcm5pbmcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6IGRhdGEuZGF0YVxyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTWFrZSB0aGUgcmVxdWVzdCB0byBJbXBvcnQgdGhlIGxpY2Vuc2UsIGlmIGZhaWxzLCB0aHJvd3MgYW4gZXhjZXB0aW9uIHZpc2libGUgZm9yIHRoZSB1c2VyIHRvIHRha2UgYWN0aW9uXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGltcG9ydExpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcbiAgICAgICAgdmFyIGhhc2ggPSB7XHJcbiAgICAgICAgICAgIGRhdGE6IGxpY2Vuc2UuaGFzaFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnJlcXVlc3RJbXBvcnQoaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYgKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkgSW1wb3J0ZWQnIH0pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkLiBSZXZpZXcgdGhlIHByb3ZpZGVkIExpY2Vuc2UgS2V5IGlzIGNvcnJlY3QuJyB9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHsgc3VjY2VzczogZmFsc2UgfSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZXZva2VMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnJldm9rZUxpY2Vuc2UobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmRlbGV0ZUxpY2Vuc2UobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRBY3Rpdml0eUxvZyhsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRBY3Rpdml0eUxvZyhsaWNlbnNlLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IE5vdGljZUxpc3QgZnJvbSAnLi9saXN0L05vdGljZUxpc3QuanMnO1xyXG5pbXBvcnQgTm90aWNlTWFuYWdlclNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL05vdGljZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IEVkaXROb3RpY2UgZnJvbSAnLi9lZGl0L0VkaXROb3RpY2UuanMnO1xyXG5cclxudmFyIE5vdGljZU1hbmFnZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uTm90aWNlTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbm90aWNlTWFuYWdlcicpO1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlclZpZXcuaHRtbCcsXHJcbiAgICAgICAgY29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG4gICAgfTtcclxuXHJcbiAgICAkc3RhdGVQcm92aWRlclxyXG4gICAgICAgIC5zdGF0ZSgnbm90aWNlTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ05vdGljZSBBZG1pbmlzdHJhdGlvbicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydBZG1pbicsICdOb3RpY2UnLCAnTGlzdCddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy9ub3RpY2UvbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9saXN0L05vdGljZUxpc3QuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ05vdGljZUxpc3QgYXMgbm90aWNlTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLnNlcnZpY2UoJ05vdGljZU1hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIE5vdGljZU1hbmFnZXJTZXJ2aWNlXSk7XHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ05vdGljZUxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ05vdGljZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsIE5vdGljZUxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdFZGl0Tm90aWNlJywgWyckbG9nJywgJ05vdGljZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBFZGl0Tm90aWNlXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBOb3RpY2VNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBFZGl0Tm90aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCBub3RpY2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZSA9IG5vdGljZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLmFjdGlvbiA9IHBhcmFtcy5hY3Rpb247XHJcbiAgICAgICAgdGhpcy5hY3Rpb25UeXBlID0gcGFyYW1zLmFjdGlvblR5cGU7XHJcblxyXG4gICAgICAgIHRoaXMua2VuZG9FZGl0b3JUb29scyA9IFtcclxuICAgICAgICAgICAgJ2Zvcm1hdHRpbmcnLCAnY2xlYW5Gb3JtYXR0aW5nJyxcclxuICAgICAgICAgICAgJ2ZvbnROYW1lJywgJ2ZvbnRTaXplJyxcclxuICAgICAgICAgICAgJ2p1c3RpZnlMZWZ0JywgJ2p1c3RpZnlDZW50ZXInLCAnanVzdGlmeVJpZ2h0JywgJ2p1c3RpZnlGdWxsJyxcclxuICAgICAgICAgICAgJ2JvbGQnLFxyXG4gICAgICAgICAgICAnaXRhbGljJyxcclxuICAgICAgICAgICAgJ3ZpZXdIdG1sJ1xyXG4gICAgICAgIF07XHJcblxyXG4gICAgICAgIC8vIENTUyBoYXMgbm90IGNhbmNlbGluZyBhdHRyaWJ1dGVzLCBzbyBpbnN0ZWFkIG9mIHJlbW92aW5nIGV2ZXJ5IHBvc3NpYmxlIEhUTUwsIHdlIG1ha2UgZWRpdG9yIGhhcyBzYW1lIGNzc1xyXG4gICAgICAgIHRoaXMua2VuZG9TdHlsZXNoZWV0cyA9IFtcclxuICAgICAgICAgICAgJy4uL3N0YXRpYy9kaXN0L2pzL3ZlbmRvcnMvYm9vdHN0cmFwL2Rpc3QvY3NzL2Jvb3RzdHJhcC5taW4uY3NzJywgLy8gT3VydCBjdXJyZW50IEJvb3RzdHJhcCBjc3NcclxuICAgICAgICAgICAgJy4uL3N0YXRpYy9kaXN0L2Nzcy9URFNUTUxheW91dC5taW4uY3NzJyAvLyBPcmlnaW5hbCBUZW1wbGF0ZSBDU1NcclxuXHJcbiAgICAgICAgXTtcclxuXHJcbiAgICAgICAgdGhpcy5nZXRUeXBlRGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsID0ge1xyXG4gICAgICAgICAgICB0aXRsZTogJycsXHJcbiAgICAgICAgICAgIHR5cGVJZDogMCxcclxuICAgICAgICAgICAgYWN0aXZlOiBmYWxzZSxcclxuICAgICAgICAgICAgaHRtbFRleHQ6ICcnLFxyXG4gICAgICAgICAgICByYXdUZXh0OiAnJ1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgLy8gT24gRWRpdGlvbiBNb2RlIHdlIGNjIHRoZSBtb2RlbCBhbmQgb25seSB0aGUgcGFyYW1zIHdlIG5lZWRcclxuICAgICAgICBpZihwYXJhbXMubm90aWNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLmlkID0gcGFyYW1zLm5vdGljZS5pZDtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwudGl0bGUgPSBwYXJhbXMubm90aWNlLnRpdGxlO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC50eXBlSWQgPSBwYXJhbXMubm90aWNlLnR5cGUuaWQ7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLmFjdGl2ZSA9IHBhcmFtcy5ub3RpY2UuYWN0aXZlO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5odG1sVGV4dCA9IHBhcmFtcy5ub3RpY2UuaHRtbFRleHQ7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIEVudmlyb25tZW50IGRyb3Bkb3duIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRUeXBlRGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnR5cGVEYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7dHlwZUlkOiAxLCBuYW1lOiAnUHJlbG9naW4nfSxcclxuICAgICAgICAgICAge3R5cGVJZDogMiwgbmFtZTogJ1Bvc3Rsb2dpbid9XHJcbiAgICAgICAgICAgIC8ve3R5cGVJZDogMywgbmFtZTogJ0dlbmVyYWwnfSBEaXNhYmxlZCB1bnRpbCBQaGFzZSBJSVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIHRoZSBTZXJ2aWNlIGNhbGwgdG8gQ3JlYXRlL0VkaXQgYSBub3RpY2VcclxuICAgICAqL1xyXG4gICAgc2F2ZU5vdGljZSgpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKHRoaXMuYWN0aW9uICsgJyBOb3RpY2UgUmVxdWVzdGVkOiAnLCB0aGlzLmVkaXRNb2RlbCk7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZWwucmF3VGV4dCA9ICQoJyNrZW5kby1lZGl0b3ItY3JlYXRlLWVkaXQnKS50ZXh0KCk7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZWwudHlwZUlkID0gcGFyc2VJbnQodGhpcy5lZGl0TW9kZWwudHlwZUlkKTtcclxuICAgICAgICBpZih0aGlzLmFjdGlvbiA9PT0gdGhpcy5hY3Rpb25UeXBlLk5FVykge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmNyZWF0ZU5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIGlmKHRoaXMuYWN0aW9uID09PSB0aGlzLmFjdGlvblR5cGUuRURJVCkge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmVkaXROb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVOb3RpY2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG4gICAgICAgICAgICBzaXplOiAnc20nLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsIG1lc3NhZ2U6ICdBcmUgeW91IHN1cmUgeW91IHdhbnQgdG8gZGVsZXRlIGl0PyBUaGlzIGFjdGlvbiBjYW5ub3QgYmUgdW5kb25lLid9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmRlbGV0ZU5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIE5vdGljZUxpc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSwgbm90aWNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG5cclxuICAgICAgICB0aGlzLmFjdGlvblR5cGUgPSB7XHJcbiAgICAgICAgICAgIE5FVzogJ05ldycsXHJcbiAgICAgICAgICAgIEVESVQ6ICdFZGl0J1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlID0gbm90aWNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VMaXN0IEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibm90aWNlTGlzdC5vbkVkaXRDcmVhdGVOb3RpY2Uobm90aWNlTGlzdC5hY3Rpb25UeXBlLk5FVylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gQ3JlYXRlIE5ldyBOb3RpY2U8L2J1dHRvbj4gPGRpdiBuZy1jbGljaz1cIm5vdGljZUxpc3QucmVsb2FkTm90aWNlTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2lkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdodG1sVGV4dCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibm90aWNlTGlzdC5vbkVkaXRDcmVhdGVOb3RpY2Uobm90aWNlTGlzdC5hY3Rpb25UeXBlLkVESVQsIHRoaXMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0aXRsZScsIHRpdGxlOiAnVGl0bGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUuaWQnLCBoaWRkZW46IHRydWV9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5uYW1lJywgdGl0bGU6ICdUeXBlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3RpdmUnLCB0aXRsZTogJ0FjdGl2ZScsIHRlbXBsYXRlOiAnI2lmKGFjdGl2ZSkgeyMgWWVzICN9IGVsc2UgeyMgTm8gI30jJyB9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZ2V0Tm90aWNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAndGl0bGUnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc29ydGFibGU6IHRydWVcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogT3BlbiBhIGRpYWxvZyB3aXRoIHRoZSBCYXNpYyBGb3JtIHRvIHJlcXVlc3QgYSBOZXcgTm90aWNlXHJcbiAgICAgKi9cclxuICAgIG9uRWRpdENyZWF0ZU5vdGljZShhY3Rpb24sIG5vdGljZSkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvZWRpdC9FZGl0Tm90aWNlLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRWRpdE5vdGljZSBhcyBlZGl0Tm90aWNlJyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdmFyIGRhdGFJdGVtID0gbm90aWNlICYmIG5vdGljZS5kYXRhSXRlbTtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBhY3Rpb246IGFjdGlvbiwgbm90aWNlOiBkYXRhSXRlbSwgYWN0aW9uVHlwZTogdGhpcy5hY3Rpb25UeXBlfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChub3RpY2UpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbyhhY3Rpb24gKyAnIE5vdGljZTogJywgbm90aWNlKTtcclxuICAgICAgICAgICAgLy8gQWZ0ZXIgYSBuZXcgdmFsdWUgaXMgYWRkZWQsIGxldHMgdG8gcmVmcmVzaCB0aGUgR3JpZFxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZE5vdGljZUxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oYWN0aW9uICsgJyBSZXF1ZXN0IENhbmNlbGVkLicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZE5vdGljZUxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5ub3RpY2VHcmlkLmRhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIE5vdGljZU1hbmFnZXJTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuXHJcbiAgICAgICAgdGhpcy5UWVBFID0ge1xyXG4gICAgICAgICAgICAnMSc6ICdQcmVsb2dpbicsXHJcbiAgICAgICAgICAgICcyJzogJ1Bvc3Rsb2dpbicsXHJcbiAgICAgICAgICAgICczJzogJ0dlbmVyYWwnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ05vdGljZU1hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldE5vdGljZUxpc3QoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldE5vdGljZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdmFyIG5vdGljZUxpc3QgPSBbXTtcclxuICAgICAgICAgICAgdHJ5IHtcclxuICAgICAgICAgICAgICAgIC8vIFZlcmlmeSB0aGUgTGlzdCByZXR1cm5zIHdoYXQgd2UgZXhwZWN0IGFuZCB3ZSBjb252ZXJ0IGl0IHRvIGFuIEFycmF5IHZhbHVlXHJcbiAgICAgICAgICAgICAgICBpZihkYXRhICYmIGRhdGEubm90aWNlcykge1xyXG4gICAgICAgICAgICAgICAgICAgIG5vdGljZUxpc3QgPSBkYXRhLm5vdGljZXM7XHJcbiAgICAgICAgICAgICAgICAgICAgaWYgKG5vdGljZUxpc3QgJiYgbm90aWNlTGlzdC5sZW5ndGggPiAwKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgbm90aWNlTGlzdC5sZW5ndGg7IGkgPSBpICsgMSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgbm90aWNlTGlzdFtpXS50eXBlID0ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiBub3RpY2VMaXN0W2ldLnR5cGVJZCxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiB0aGlzLlRZUEVbbm90aWNlTGlzdFtpXS50eXBlSWRdXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9O1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZGVsZXRlIG5vdGljZUxpc3RbaV0udHlwZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9IGNhdGNoKGUpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMubG9nLmVycm9yKCdFcnJvciBwYXJzaW5nIHRoZSBOb3RpY2UgTGlzdCcsIGUpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhub3RpY2VMaXN0KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENyZWF0ZSBhIE5ldyBOb3RpY2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOb3RpY2Uobm90aWNlLCBjYWxsYmFjayl7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBOb3RpY2Ugc2hvdWxkIGhhdmUgdGhlIElEIGluIG9yZGVyIHRvIGVkaXQgdGhlIE5vdGljZVxyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGVkaXROb3RpY2Uobm90aWNlLCBjYWxsYmFjayl7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5lZGl0Tm90aWNlKG5vdGljZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTm90aWNlIHNob3VsZCBoYXZlIHRoZSBJRCBpbiBvcmRlciB0byBkZWxldGUgdGhlIG5vdGljZVxyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGRlbGV0ZU5vdGljZShub3RpY2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5kZWxldGVOb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBUYXNrTWFuYWdlclNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL1Rhc2tNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlckNvbnRyb2xsZXIgZnJvbSAnLi9saXN0L1Rhc2tNYW5hZ2VyQ29udHJvbGxlci5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlckVkaXQgZnJvbSAnLi9lZGl0L1Rhc2tNYW5hZ2VyRWRpdC5qcyc7XHJcblxyXG52YXIgVGFza01hbmFnZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uVGFza01hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICdmb3JtbHlDb25maWdQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsIGZvcm1seUNvbmZpZ1Byb3ZpZGVyKSB7XHJcblxyXG4gICAgZm9ybWx5Q29uZmlnUHJvdmlkZXIuc2V0VHlwZSh7XHJcbiAgICAgICAgbmFtZTogJ2N1c3RvbScsXHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICdjdXN0b20uaHRtbCdcclxuICAgIH0pO1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlclZpZXcuaHRtbCcsXHJcbiAgICAgICAgY29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG4gICAgfTtcclxuXHJcbiAgICAkc3RhdGVQcm92aWRlclxyXG4gICAgICAgIC5zdGF0ZSgndGFza0xpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdNeSBUYXNrIE1hbmFnZXInLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnVGFzayBNYW5hZ2VyJ119fSxcclxuICAgICAgICAgICAgdXJsOiAnL3Rhc2svbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvdGFza01hbmFnZXIvbGlzdC9UYXNrTWFuYWdlckNvbnRhaW5lci5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnVGFza01hbmFnZXJDb250cm9sbGVyIGFzIHRhc2tNYW5hZ2VyJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcblRhc2tNYW5hZ2VyTW9kdWxlLnNlcnZpY2UoJ3Rhc2tNYW5hZ2VyU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCBUYXNrTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcblRhc2tNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ1Rhc2tNYW5hZ2VyQ29udHJvbGxlcicsIFsnJGxvZycsICd0YXNrTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgVGFza01hbmFnZXJDb250cm9sbGVyXSk7XHJcblRhc2tNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ1Rhc2tNYW5hZ2VyRWRpdCcsIFsnJGxvZycsIFRhc2tNYW5hZ2VyRWRpdF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IFRhc2tNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMy8xMS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyRWRpdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgdGFza01hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuXHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMC8yMDE1LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCB0YXNrTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMubW9kdWxlID0gJ1Rhc2tNYW5hZ2VyJztcclxuICAgICAgICB0aGlzLnRhc2tNYW5hZ2VyU2VydmljZSA9IHRhc2tNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnRhc2tHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMuZXZlbnREYXRhU291cmNlID0gW107XHJcblxyXG4gICAgICAgIC8vIEluaXQgQ2xhc3NcclxuICAgICAgICB0aGlzLmdldEV2ZW50RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdUYXNrTWFuYWdlciBDb250cm9sbGVyIEluc3RhbmNlZCcpO1xyXG4gICAgICAgIHRoaXMuaW5pdEZvcm0oKTtcclxuXHJcbiAgICB9XHJcblxyXG4gICAgb3Blbk1vZGFsRGVtbygpIHtcclxuXHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnYXBwLWpzL21vZHVsZXMvdGFza01hbmFnZXIvZWRpdC9UYXNrTWFuYWdlckVkaXQuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdUYXNrTWFuYWdlckVkaXQnLFxyXG4gICAgICAgICAgICBzaXplOiAnbGcnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBpdGVtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBbJzEnLCdhMicsJ2dnJ107XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoc2VsZWN0ZWRJdGVtKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMuZGVidWcoc2VsZWN0ZWRJdGVtKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ01vZGFsIGRpc21pc3NlZCBhdDogJyArIG5ldyBEYXRlKCkpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy50YXNrR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGdyb3VwYWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgc29ydGFibGU6IHRydWUsXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW3tmaWVsZDogJ2FjdGlvbicsIHRpdGxlOiAnQWN0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0YXNrJywgdGl0bGU6ICdUYXNrJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdkZXNjcmlwdGlvbicsIHRpdGxlOiAnRGVzY3JpcHRpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Fzc2V0TmFtZScsIHRpdGxlOiAnQXNzZXQgTmFtZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzZXRUeXBlJywgdGl0bGU6ICdBc3NldCBUeXBlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd1cGRhdGVkJywgdGl0bGU6ICdVcGRhdGVkJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdkdWUnLCB0aXRsZTogJ0R1ZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3RhdHVzJywgdGl0bGU6ICdTdGF0dXMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Fzc2lnbmVkVG8nLCB0aXRsZTogJ0Fzc2lnbmVkIFRvJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0ZWFtJywgdGl0bGU6ICdUZWFtJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjYXRlZ29yeScsIHRpdGxlOiAnQ2F0ZWdvcnknfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N1YycsIHRpdGxlOiAnU3VjLid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc2NvcmUnLCB0aXRsZTogJ1Njb3JlJ31dLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAvKnRoaXMudGFza01hbmFnZXJTZXJ2aWNlLnRlc3RTZXJ2aWNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pOyovXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRFdmVudERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5ldmVudERhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHtldmVudElkOiAxLCBldmVudE5hbWU6ICdBbGwnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDIsIGV2ZW50TmFtZTogJ0J1aWxkb3V0J30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiAzLCBldmVudE5hbWU6ICdEUi1FUCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogNCwgZXZlbnROYW1lOiAnTTEtUGh5c2ljYWwnfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgb25FcnJvckhhcHBlbnMoKSB7XHJcbiAgICAgICAgdGhpcy50YXNrTWFuYWdlclNlcnZpY2UuZmFpbENhbGwoZnVuY3Rpb24gKCkge1xyXG5cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBpbml0Rm9ybSgpIHtcclxuICAgICAgICB0aGlzLnVzZXJGaWVsZHMgPSBbXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGtleTogJ2VtYWlsJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdpbnB1dCcsXHJcbiAgICAgICAgICAgICAgICB0ZW1wbGF0ZU9wdGlvbnM6IHtcclxuICAgICAgICAgICAgICAgICAgICB0eXBlOiAnZW1haWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGxhYmVsOiAnRW1haWwgYWRkcmVzcycsXHJcbiAgICAgICAgICAgICAgICAgICAgcGxhY2Vob2xkZXI6ICdFbnRlciBlbWFpbCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAga2V5OiAncGFzc3dvcmQnLFxyXG4gICAgICAgICAgICAgICAgdHlwZTogJ2lucHV0JyxcclxuICAgICAgICAgICAgICAgIHRlbXBsYXRlT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgICAgIHR5cGU6ICdwYXNzd29yZCcsXHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdQYXNzd29yZCcsXHJcbiAgICAgICAgICAgICAgICAgICAgcGxhY2Vob2xkZXI6ICdQYXNzd29yZCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAga2V5OiAnZmlsZScsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnZmlsZScsXHJcbiAgICAgICAgICAgICAgICB0ZW1wbGF0ZU9wdGlvbnM6IHtcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ0ZpbGUgaW5wdXQnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRlc2NyaXB0aW9uOiAnRXhhbXBsZSBibG9jay1sZXZlbCBoZWxwIHRleHQgaGVyZScsXHJcbiAgICAgICAgICAgICAgICAgICAgdXJsOiAnaHR0cHM6Ly9leGFtcGxlLmNvbS91cGxvYWQnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGtleTogJ2NoZWNrZWQnLFxyXG4gICAgICAgICAgICAgICAgdHlwZTogJ2NoZWNrYm94JyxcclxuICAgICAgICAgICAgICAgIHRlbXBsYXRlT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgICAgIGxhYmVsOiAnQ2hlY2sgbWUgb3V0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMjIvMDcvMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCBSZXN0U2VydmljZUhhbmRsZXIpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IFJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuXHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1Rhc2tNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBmYWlsQ2FsbChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuUmVzb3VyY2VTZXJ2aWNlSGFuZGxlcigpLmdldFNWRygpO1xyXG4gICAgfVxyXG5cclxuICAgIHRlc3RTZXJ2aWNlKGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5UYXNrU2VydmljZUhhbmRsZXIoKS5nZXRGZWVkcygoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMy8yMDE2LlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCdcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEZvcm1WYWxpZGF0b3Ige1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnNjb3BlID0gJHNjb3BlO1xyXG5cclxuICAgICAgICAvLyBKUyBkb2VzIGEgYXJndW1lbnQgcGFzcyBieSByZWZlcmVuY2VcclxuICAgICAgICB0aGlzLmN1cnJlbnRPYmplY3QgPSBudWxsO1xyXG4gICAgICAgIC8vIEEgY29weSB3aXRob3V0IHJlZmVyZW5jZSBmcm9tIHRoZSBvcmlnaW5hbCBvYmplY3RcclxuICAgICAgICB0aGlzLm9yaWdpbmFsRGF0YSA9IG51bGw7XHJcbiAgICAgICAgLy8gQSBDQyBhcyBKU09OIGZvciBjb21wYXJpc29uIFB1cnBvc2VcclxuICAgICAgICB0aGlzLm9iamVjdEFzSlNPTiA9IG51bGw7XHJcblxyXG5cclxuICAgICAgICAvLyBPbmx5IGZvciBNb2RhbCBXaW5kb3dzXHJcbiAgICAgICAgdGhpcy5yZWxvYWRSZXF1aXJlZCA9IGZhbHNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcblxyXG4gICAgICAgIGlmICgkc2NvcGUuJG9uKSB7XHJcbiAgICAgICAgICAgICRzY29wZS4kb24oJ21vZGFsLmNsb3NpbmcnLCAoZXZlbnQsIHJlYXNvbiwgY2xvc2VkKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DbG9zZURpYWxvZyhldmVudCwgcmVhc29uLCBjbG9zZWQpXHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmVzIHRoZSBGb3JtIGluIDMgaW5zdGFuY2VzLCBvbmUgdG8ga2VlcCB0cmFjayBvZiB0aGUgb3JpZ2luYWwgZGF0YSwgb3RoZXIgaXMgdGhlIGN1cnJlbnQgb2JqZWN0IGFuZFxyXG4gICAgICogYSBKU09OIGZvcm1hdCBmb3IgY29tcGFyaXNvbiBwdXJwb3NlXHJcbiAgICAgKiBAcGFyYW0gbmV3T2JqZWN0SW5zdGFuY2VcclxuICAgICAqL1xyXG4gICAgc2F2ZUZvcm0obmV3T2JqZWN0SW5zdGFuY2UpIHtcclxuICAgICAgICB0aGlzLmN1cnJlbnRPYmplY3QgPSBuZXdPYmplY3RJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLm9yaWdpbmFsRGF0YSA9IGFuZ3VsYXIuY29weShuZXdPYmplY3RJbnN0YW5jZSwgdGhpcy5vcmlnaW5hbERhdGEpO1xyXG4gICAgICAgIHRoaXMub2JqZWN0QXNKU09OID0gYW5ndWxhci50b0pzb24obmV3T2JqZWN0SW5zdGFuY2UpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogR2V0IHRoZSBDdXJyZW50IE9iamVjdCBvbiBoaXMgcmVmZXJlbmNlIEZvcm1hdFxyXG4gICAgICogQHJldHVybnMge251bGx8Kn1cclxuICAgICAqL1xyXG4gICAgZ2V0Rm9ybSgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5jdXJyZW50T2JqZWN0O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogR2V0IHRoZSBPYmplY3QgYXMgSlNPTiBmcm9tIHRoZSBPcmlnaW5hbCBEYXRhXHJcbiAgICAgKiBAcmV0dXJucyB7bnVsbHxzdHJpbmd8dW5kZWZpbmVkfHN0cmluZ3wqfVxyXG4gICAgICovXHJcbiAgICBnZXRGb3JtQXNKU09OKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLm9iamVjdEFzSlNPTjtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqXHJcbiAgICAgKiBAcGFyYW0gb2JqZXRUb1Jlc2V0IG9iamVjdCB0byByZXNldFxyXG4gICAgICogQHBhcmFtIG9uUmVzZXRGb3JtIGNhbGxiYWNrXHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgcmVzZXRGb3JtKG9uUmVzZXRGb3JtKSB7XHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gYW5ndWxhci5jb3B5KHRoaXMub3JpZ2luYWxEYXRhLCB0aGlzLmN1cnJlbnRPYmplY3QpO1xyXG4gICAgICAgIHRoaXMuc2FmZUFwcGx5KCk7XHJcblxyXG4gICAgICAgIGlmKG9uUmVzZXRGb3JtKSB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblJlc2V0Rm9ybSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFZhbGlkYXRlcyBpZiB0aGUgY3VycmVudCBvYmplY3QgZGlmZmVycyBmcm9tIHdoZXJlIGl0IHdhcyBvcmlnaW5hbGx5IHNhdmVkXHJcbiAgICAgKiBAcmV0dXJucyB7Ym9vbGVhbn1cclxuICAgICAqL1xyXG4gICAgaXNEaXJ0eSgpIHtcclxuICAgICAgICB2YXIgbmV3T2JqZWN0SW5zdGFuY2UgPSBhbmd1bGFyLnRvSnNvbih0aGlzLmN1cnJlbnRPYmplY3QpO1xyXG4gICAgICAgIHJldHVybiBuZXdPYmplY3RJbnN0YW5jZSAhPT0gdGhpcy5nZXRGb3JtQXNKU09OKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGlzIGZ1bmN0aW9uIGlzIG9ubHkgYXZhaWxhYmxlIHdoZW4gdGhlIEZvcm0gaXMgYmVpbmcgY2FsbGVkIGZyb20gYSBEaWFsb2cgUG9wVXBcclxuICAgICAqL1xyXG4gICAgb25DbG9zZURpYWxvZyhldmVudCwgcmVhc29uLCBjbG9zZWQpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdtb2RhbC5jbG9zaW5nOiAnICsgKGNsb3NlZCA/ICdjbG9zZScgOiAnZGlzbWlzcycpICsgJygnICsgcmVhc29uICsgJyknKTtcclxuICAgICAgICBpZiAodGhpcy5pc0RpcnR5KCkgJiYgcmVhc29uICE9PSAnY2FuY2VsLWNvbmZpcm1hdGlvbicgJiYgdHlwZW9mIHJlYXNvbiAhPT0gJ29iamVjdCcpIHtcclxuICAgICAgICAgICAgZXZlbnQucHJldmVudERlZmF1bHQoKTtcclxuICAgICAgICAgICAgdGhpcy5jb25maXJtQ2xvc2VGb3JtKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQSBDb25maXJtYXRpb24gRGlhbG9nIHdoZW4gdGhlIGluZm9ybWF0aW9uIGNhbiBiZSBsb3N0XHJcbiAgICAgKiBAcGFyYW0gZXZlbnRcclxuICAgICAqL1xyXG4gICAgY29uZmlybUNsb3NlRm9ybShldmVudCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICBtZXNzYWdlOiAnQ2hhbmdlcyB5b3UgbWFkZSBtYXkgbm90IGJlIHNhdmVkLiBEbyB5b3Ugd2FudCB0byBjb250aW51ZT8nXHJcbiAgICAgICAgICAgICAgICAgICAgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbC1jb25maXJtYXRpb24nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFV0aWwgdG8gY2FsbCBzYWZlIGlmIHJlcXVpcmVkXHJcbiAgICAgKiBAcGFyYW0gZm5cclxuICAgICAqL1xyXG4gICAgc2FmZUFwcGx5KGZuKSB7XHJcbiAgICAgICAgdmFyIHBoYXNlID0gdGhpcy5zY29wZS4kcm9vdC4kJHBoYXNlO1xyXG4gICAgICAgIGlmKHBoYXNlID09PSAnJGFwcGx5JyB8fCBwaGFzZSA9PT0gJyRkaWdlc3QnKSB7XHJcbiAgICAgICAgICAgIGlmKGZuICYmICh0eXBlb2YoZm4pID09PSAnZnVuY3Rpb24nKSkge1xyXG4gICAgICAgICAgICAgICAgZm4oKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIHRoaXMuc2NvcGUuJGFwcGx5KGZuKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBVdGlsIHRvIFJlc2V0IGEgRHJvcGRvd24gbGlzdCBvbiBLZW5kb1xyXG4gICAgICovXHJcblxyXG4gICAgcmVzZXREcm9wRG93bihzZWxlY3Rvckluc3RhbmNlLCBzZWxlY3RlZElkLCBmb3JjZSkge1xyXG4gICAgICAgIGlmKHNlbGVjdG9ySW5zdGFuY2UgJiYgc2VsZWN0b3JJbnN0YW5jZS5kYXRhSXRlbXMpIHtcclxuICAgICAgICAgICAgc2VsZWN0b3JJbnN0YW5jZS5kYXRhSXRlbXMoKS5mb3JFYWNoKCh2YWx1ZSwgaW5kZXgpID0+IHtcclxuICAgICAgICAgICAgICAgIGlmKHNlbGVjdGVkSWQgPT09IHZhbHVlLmlkIHx8IHNlbGVjdGVkSWQgPT09IHZhbHVlKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2VsZWN0b3JJbnN0YW5jZS5zZWxlY3QoaW5kZXgpO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIGlmKGZvcmNlKSB7XHJcbiAgICAgICAgICAgICAgICBzZWxlY3Rvckluc3RhbmNlLnRyaWdnZXIoJ2NoYW5nZScpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5zYWZlQXBwbHkoKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH1cclxuICAgIH1cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjMvMjAxNS5cclxuICogSW1wbGVtZW50cyBSWCBPYnNlcnZhYmxlIHRvIGRpc3Bvc2UgYW5kIHRyYWNrIGJldHRlciBlYWNoIGNhbGwgdG8gdGhlIHNlcnZlclxyXG4gKiBUaGUgT2JzZXJ2ZXIgc3Vic2NyaWJlIGEgcHJvbWlzZS5cclxuICovXHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdEhhbmRsZXIge1xyXG4gICAgY29uc3RydWN0b3IocngpIHtcclxuICAgICAgICB0aGlzLnJ4ID0gcng7XHJcbiAgICAgICAgdGhpcy5wcm9taXNlID0gW107XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDYWxsZWQgZnJvbSBSZXN0U2VydmljZUhhbmRsZXIuc3Vic2NyaWJlUmVxdWVzdFxyXG4gICAgICogaXQgdmVyaWZ5IHRoYXQgdGhlIGNhbGwgaXMgYmVpbmcgZG9uZSB0byB0aGUgc2VydmVyIGFuZCByZXR1cm4gYSBwcm9taXNlXHJcbiAgICAgKiBAcGFyYW0gcmVxdWVzdFxyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIHN1YnNjcmliZVJlcXVlc3QocmVxdWVzdCwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcbiAgICAgICAgdmFyIHJ4T2JzZXJ2YWJsZSA9IHRoaXMucnguT2JzZXJ2YWJsZS5mcm9tUHJvbWlzZShyZXF1ZXN0KTtcclxuICAgICAgICAvLyBWZXJpZnkgaXMgbm90IGEgZHVwbGljYXRlIGNhbGxcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICB0aGlzLmNhbmNlbFJlcXVlc3QocnhPYnNlcnZhYmxlKTtcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIC8vIFN1YnNjcmliZSB0aGUgcmVxdWVzdFxyXG4gICAgICAgIHZhciByZXN1bHRTdWJzY3JpYmUgPSB0aGlzLmFkZFN1YnNjcmliZShyeE9ic2VydmFibGUsIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgaWYgKHJlc3VsdFN1YnNjcmliZSAmJiByZXN1bHRTdWJzY3JpYmUuaXNTdG9wcGVkKSB7XHJcbiAgICAgICAgICAgIC8vIEFuIGVycm9yIGhhcHBlbnMsIHRyYWNrZWQgYnkgSHR0cEludGVyY2VwdG9ySW50ZXJmYWNlXHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgYWRkU3Vic2NyaWJlKHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcbiAgICAgICAgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF0gPSByeE9ic2VydmFibGUuc3Vic2NyaWJlKFxyXG4gICAgICAgICAgICAocmVzcG9uc2UpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLm9uU3Vic2NyaWJlZFN1Y2Nlc3MocmVzcG9uc2UsIHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgKGVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5vblN1YnNjcmliZWRFcnJvcihlcnJvciwgcnhPYnNlcnZhYmxlLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgLy8gTk8tT1AgU3Vic2NyaWJlIGNvbXBsZXRlZFxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgcmV0dXJuIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgfVxyXG5cclxuICAgIGNhbmNlbFJlcXVlc3QocnhPYnNlcnZhYmxlKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgICAgICByeE9ic2VydmFibGUuZGlzcG9zZSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBpc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSB7XHJcbiAgICAgICAgcmV0dXJuIChyeE9ic2VydmFibGUgJiYgcnhPYnNlcnZhYmxlLl9wICYmIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdKTtcclxuICAgIH1cclxuXHJcbiAgICBvblN1YnNjcmliZWRTdWNjZXNzKHJlc3BvbnNlLCByeE9ic2VydmFibGUsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICAgICAgaWYob25TdWNjZXNzKXtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhyZXNwb25zZS5kYXRhKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaHJvd3MgaW1tZWRpYXRlbHkgZXJyb3Igd2hlbiB0aGUgcGV0aXRpb24gY2FsbCBpcyB3cm9uZ1xyXG4gICAgICogb3Igd2l0aCBhIGRlbGF5IGlmIHRoZSBjYWxsIGlzIHZhbGlkXHJcbiAgICAgKiBAcGFyYW0gZXJyb3JcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICBvblN1YnNjcmliZWRFcnJvcihlcnJvciwgcnhPYnNlcnZhYmxlLCBvbkVycm9yKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgICAgICBpZihvbkVycm9yKXtcclxuICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3Ioe30pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgUmVzdFNlcnZpY2VIYW5kbGVyIGZyb20gJy4vUmVzdFNlcnZpY2VIYW5kbGVyLmpzJztcclxuaW1wb3J0IFVzZXJQcmVmZXJlbmNlc1NlcnZpY2UgZnJvbSAnLi9Vc2VyUHJlZmVyZW5jZXNTZXJ2aWNlLmpzJ1xyXG5cclxudmFyIFJlc3RBUElNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uUmVzdEFQSU1vZHVsZScsW10pO1xyXG5cclxuUmVzdEFQSU1vZHVsZS5zZXJ2aWNlKCdSZXN0U2VydmljZUhhbmRsZXInLCBbJyRsb2cnLCAnJGh0dHAnLCAnJHJlc291cmNlJywgJ3J4JywgUmVzdFNlcnZpY2VIYW5kbGVyXSk7XHJcblJlc3RBUElNb2R1bGUuc2VydmljZSgnVXNlclByZWZlcmVuY2VzU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCBVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBSZXN0QVBJTW9kdWxlO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzA4LzE1LlxyXG4gKiBJdCBhYnN0cmFjdCBlYWNoIG9uZSBvZiB0aGUgZXhpc3RpbmcgY2FsbCB0byB0aGUgQVBJLCBpdCBzaG91bGQgb25seSBjb250YWlucyB0aGUgY2FsbCBmdW5jdGlvbnMgYW5kIHJlZmVyZW5jZVxyXG4gKiB0byB0aGUgY2FsbGJhY2ssIG5vIGxvZ2ljIGF0IGFsbC5cclxuICpcclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IFJlcXVlc3RIYW5kbGVyIGZyb20gJy4vUmVxdWVzdEhhbmRsZXIuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVzdFNlcnZpY2VIYW5kbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRodHRwLCAkcmVzb3VyY2UsIHJ4KSB7XHJcbiAgICAgICAgdGhpcy5yeCA9IHJ4O1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLmh0dHAgPSAkaHR0cDtcclxuICAgICAgICB0aGlzLnJlc291cmNlID0gJHJlc291cmNlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUhlYWRlcnMoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnUmVzdFNlcnZpY2UgTG9hZGVkJyk7XHJcbiAgICAgICAgdGhpcy5yZXEgPSB7XHJcbiAgICAgICAgICAgIG1ldGhvZDogJycsXHJcbiAgICAgICAgICAgIHVybDogJycsXHJcbiAgICAgICAgICAgIGhlYWRlcnM6IHtcclxuICAgICAgICAgICAgICAgICdDb250ZW50LVR5cGUnOiAnYXBwbGljYXRpb24vanNvbjtjaGFyc2V0PVVURi04J1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhOiBbXVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUhlYWRlcnMoKSB7XHJcbiAgICAgICAgdGhpcy5odHRwLmRlZmF1bHRzLmhlYWRlcnMucG9zdFsnQ29udGVudC1UeXBlJ10gPSAnYXBwbGljYXRpb24veC13d3ctZm9ybS11cmxlbmNvZGVkJztcclxuICAgIH1cclxuXHJcbiAgICBUYXNrU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgZ2V0RmVlZHM6IChjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCd0ZXN0L21vY2t1cERhdGEvVGFza01hbmFnZXIvdGFza01hbmFnZXJMaXN0Lmpzb24nKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBjb21tb25TZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb246IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy91c2VyL3ByZWZlcmVuY2VzL0NVUlJfRFRfRk9STUFULENVUlJfVFonKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBsaWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRMaWNlbnNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZXMnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9lbnZpcm9ubWVudCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvcHJvamVjdCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRMaWNlbnNlTGlzdDogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3Q6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9saWNlbnNlL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYXBwbHlMaWNlbnNlOiAobGljZW5zZUlkLCBkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2xvYWQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0SGFzaENvZGU6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9oYXNoJztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbWFpbENvbnRlbnQ6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9lbWFpbC9yZXF1ZXN0JztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAvLy0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAgICAgICAgICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3Q6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvZW1haWwvcmVxdWVzdCc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZW1haWxSZXF1ZXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy8/Pz8nO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLnBvc3QoJy4uL3Rlc3QvbW9ja3VwRGF0YS9MaWNlbnNlQWRtaW4vbGljZW5zZUFkbWluTGlzdC5qc29uJywgZGF0YSksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGVsZXRlTGljZW5zZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbGljZW5zZS8nICsgZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBsaWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIHJlcXVlc3RJbXBvcnQ6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvcmVxdWVzdCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRMaWNlbnNlTGlzdDogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL21hbmFnZXIvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvcHJvamVjdCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEtleUNvZGU6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2tleSc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc2F2ZUxpY2Vuc2U6IChsaWNlbnNlSWQsIGxpY2Vuc2VNb2RpZmllZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUFVUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZDtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBsaWNlbnNlTW9kaWZpZWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGVsZXRlTGljZW5zZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBkYXRhLmlkICsgJy9kZWxldGUnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHJldm9rZUxpY2Vuc2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdERUxFVEUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBhY3RpdmF0ZUxpY2Vuc2U6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9hY3RpdmF0ZSc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0QWN0aXZpdHlMb2c6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2FjdGl2aXR5bG9nJztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBtYW51YWxseVJlcXVlc3Q6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9lbWFpbC9zZW5kJztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgZ2V0Tm90aWNlTGlzdDogKG9uU3VjY2VzcykgPT4geyAvLyByZWFsIHdzIGV4YW1wbGVcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9ub3RpY2VzJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNyZWF0ZU5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gJy4uL3dzL25vdGljZXMnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZWRpdE5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BVVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3Mvbm90aWNlcy8nICsgZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRlbGV0ZU5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAnLi4vd3Mvbm90aWNlcy8nICsgZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMy83LzIwMTcuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVXNlclByZWZlcmVuY2VzU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1VzZXJQcmVmZXJlbmNlc1NlcnZpY2UgSW5zdGFuY2VkJyk7XHJcblxyXG4gICAgICAgIHRoaXMudGltZVpvbmVDb25maWd1cmF0aW9uID0ge1xyXG4gICAgICAgICAgICBwcmVmZXJlbmNlczoge31cclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0VGltZVpvbmVDb25maWd1cmF0aW9uKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuY29tbW9uU2VydmljZUhhbmRsZXIoKS5nZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24gPSBkYXRhLmRhdGE7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRDb252ZXJ0ZWREYXRlSW50b1RpbWVab25lKGRhdGVTdHJpbmcpIHtcclxuICAgICAgICB2YXIgdGltZVN0cmluZyA9IGRhdGVTdHJpbmc7XHJcbiAgICAgICAgdmFyIHVzZXJEVEZvcm1hdCA9IHRoaXMudGltZVpvbmVDb25maWd1cmF0aW9uLnByZWZlcmVuY2VzLkNVUlJfRFRfRk9STUFUO1xyXG4gICAgICAgIHZhciB0aW1lWm9uZSA9IHRoaXMudGltZVpvbmVDb25maWd1cmF0aW9uLnByZWZlcmVuY2VzLkNVUlJfVFo7XHJcblxyXG4gICAgICAgIGlmKGRhdGVTdHJpbmcgIT09ICdudWxsJyAmJlxyXG4gICAgICAgICAgICBkYXRlU3RyaW5nICYmIG1vbWVudChkYXRlU3RyaW5nKS5pc1ZhbGlkKCkgJiZcclxuICAgICAgICAgICAgISh0eXBlb2YgTnVtYmVyKGRhdGVTdHJpbmcudG9TdHJpbmcoKSkgPT09ICdudW1iZXInICYmICFpc05hTihkYXRlU3RyaW5nLnRvU3RyaW5nKCkpKSl7XHJcbiAgICAgICAgICAgIGlmICh0aW1lWm9uZSA9PT0gbnVsbCkge1xyXG4gICAgICAgICAgICAgICAgdGltZVpvbmUgPSAnR01UJztcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB2YXIgZm9ybWF0ID0gJ01NL0REL1lZWVknO1xyXG4gICAgICAgICAgICBpZiAodXNlckRURm9ybWF0ID09PSAnREQvTU0vWVlZWScpIHtcclxuICAgICAgICAgICAgICAgIGZvcm1hdCA9ICdERC9NTS9ZWVlZJztcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB0aW1lU3RyaW5nID0gbW9tZW50KGRhdGVTdHJpbmcpLnR6KHRpbWVab25lKS5mb3JtYXQoZm9ybWF0KVxyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgcmV0dXJuIHRpbWVTdHJpbmcgIT09ICdudWxsJz8gdGltZVN0cmluZzogJyc7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0Q29udmVydGVkRGF0ZVRpbWVJbnRvVGltZVpvbmUoZGF0ZVN0cmluZykge1xyXG4gICAgICAgIHZhciB0aW1lU3RyaW5nID0gZGF0ZVN0cmluZztcclxuICAgICAgICB2YXIgdXNlckRURm9ybWF0ID0gdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24ucHJlZmVyZW5jZXMuQ1VSUl9EVF9GT1JNQVQ7XHJcbiAgICAgICAgdmFyIHRpbWVab25lID0gdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24ucHJlZmVyZW5jZXMuQ1VSUl9UWjtcclxuXHJcbiAgICAgICAgaWYoZGF0ZVN0cmluZyAhPT0gJ251bGwnICYmXHJcbiAgICAgICAgICAgIGRhdGVTdHJpbmcgJiYgbW9tZW50KGRhdGVTdHJpbmcpLmlzVmFsaWQoKSAmJlxyXG4gICAgICAgICAgICAhKHR5cGVvZiBOdW1iZXIoZGF0ZVN0cmluZy50b1N0cmluZygpKSA9PT0gJ251bWJlcicgJiYgIWlzTmFOKGRhdGVTdHJpbmcudG9TdHJpbmcoKSkpKXtcclxuICAgICAgICAgICAgaWYgKHRpbWVab25lID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgICAgICB0aW1lWm9uZSA9ICdHTVQnO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHZhciBmb3JtYXQgPSAnTU0vREQvWVlZWSBoaDptbSBhJ1xyXG4gICAgICAgICAgICBpZiAodXNlckRURm9ybWF0ID09PSAnREQvTU0vWVlZWScpIHtcclxuICAgICAgICAgICAgICAgIGZvcm1hdCA9ICdERC9NTS9ZWVlZIGhoOm1tIGEnXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgdGltZVN0cmluZyA9IG1vbWVudChkYXRlU3RyaW5nKS50eih0aW1lWm9uZSkuZm9ybWF0KGZvcm1hdClcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIHJldHVybiB0aW1lU3RyaW5nICE9PSAnbnVsbCc/IHRpbWVTdHJpbmc6ICcnO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogS2VuZG8gRGF0ZSBGb3JtYXQgaXMgcXVpdGUgZGlmZmVyZW50IGFuZCB0aHJlYXRzIHRoZSBjaGFycyB3aXRoIGEgZGlmZmVyZW50IG1lYW5pbmdcclxuICAgICAqIHRoaXMgRnVuY3Rpb25zIHJldHVybiB0aGUgb25lIGluIHRoZSBzdGFuZGFyIGZvcm1hdC5cclxuICAgICAqIENvbnNpZGVyIHRoaXMgaXMgb25seSBmcm9tIG91ciBDdXJyZW50IEZvcm1hdFxyXG4gICAgICovXHJcbiAgICBnZXRDb252ZXJ0ZWREYXRlRm9ybWF0VG9LZW5kb0RhdGUoKSB7XHJcbiAgICAgICAgdmFyIHVzZXJEVEZvcm1hdCA9IHRoaXMudGltZVpvbmVDb25maWd1cmF0aW9uLnByZWZlcmVuY2VzLkNVUlJfRFRfRk9STUFUO1xyXG5cclxuICAgICAgICB2YXIgZm9ybWF0ID0gJ01NL2RkL3l5eXknO1xyXG4gICAgICAgIGlmICh1c2VyRFRGb3JtYXQgIT09IG51bGwpIHtcclxuICAgICAgICAgICAgZm9ybWF0ID0gZm9ybWF0LnJlcGxhY2UoJ0REJywgJ2RkJyk7XHJcbiAgICAgICAgICAgIGZvcm1hdCA9IGZvcm1hdC5yZXBsYWNlKCdZWVlZJywgJ3l5eXknKTtcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIHJldHVybiBmb3JtYXQ7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKiBFUzYgSW50ZXJjZXB0b3IgY2FsbHMgaW5uZXIgbWV0aG9kcyBpbiBhIGdsb2JhbCBzY29wZSwgdGhlbiB0aGUgXCJ0aGlzXCIgaXMgYmVpbmcgbG9zdFxyXG4gKiBpbiB0aGUgZGVmaW5pdGlvbiBvZiB0aGUgQ2xhc3MgZm9yIGludGVyY2VwdG9ycyBvbmx5XHJcbiAqIFRoaXMgaXMgYSBpbnRlcmZhY2UgdGhhdCB0YWtlIGNhcmUgb2YgdGhlIGlzc3VlLlxyXG4gKi9cclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCAvKiBpbnRlcmZhY2UqLyBjbGFzcyBIdHRwSW50ZXJjZXB0b3Ige1xyXG4gICAgY29uc3RydWN0b3IobWV0aG9kVG9CaW5kKSB7XHJcbiAgICAgICAgLy8gSWYgbm90IG1ldGhvZCB0byBiaW5kLCB3ZSBhc3N1bWUgb3VyIGludGVyY2VwdG9yIGlzIHVzaW5nIGFsbCB0aGUgaW5uZXIgZnVuY3Rpb25zXHJcbiAgICAgICAgaWYoIW1ldGhvZFRvQmluZCkge1xyXG4gICAgICAgICAgICBbJ3JlcXVlc3QnLCAncmVxdWVzdEVycm9yJywgJ3Jlc3BvbnNlJywgJ3Jlc3BvbnNlRXJyb3InXVxyXG4gICAgICAgICAgICAgICAgLmZvckVhY2goKG1ldGhvZCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKHRoaXNbbWV0aG9kXSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzW21ldGhvZF0gPSB0aGlzW21ldGhvZF0uYmluZCh0aGlzKTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAvLyBtZXRob2RUb0JpbmQgcmVmZXJlbmNlIHRvIGEgc2luZ2xlIGNoaWxkIGNsYXNzXHJcbiAgICAgICAgICAgIHRoaXNbbWV0aG9kVG9CaW5kXSA9IHRoaXNbbWV0aG9kVG9CaW5kXS5iaW5kKHRoaXMpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICB9XHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKiBVc2UgdGhpcyBtb2R1bGUgdG8gbW9kaWZ5IGFueXRoaW5nIHJlbGF0ZWQgdG8gdGhlIEhlYWRlcnMgYW5kIFJlcXVlc3RcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuXHJcblxyXG52YXIgSFRUUE1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5IVFRQTW9kdWxlJywgWyduZ1Jlc291cmNlJ10pLmNvbmZpZyhbJyRodHRwUHJvdmlkZXInLCBmdW5jdGlvbigkaHR0cFByb3ZpZGVyKXtcclxuXHJcbiAgICAvL2luaXRpYWxpemUgZ2V0IGlmIG5vdCB0aGVyZVxyXG4gICAgaWYgKCEkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0KSB7XHJcbiAgICAgICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldCA9IHt9O1xyXG4gICAgfVxyXG5cclxuICAgIC8vRGlzYWJsZSBJRSBhamF4IHJlcXVlc3QgY2FjaGluZ1xyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnSWYtTW9kaWZpZWQtU2luY2UnXSA9ICdNb24sIDI2IEp1bCAxOTk3IDA1OjAwOjAwIEdNVCc7XHJcbiAgICAvLyBleHRyYVxyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnQ2FjaGUtQ29udHJvbCddID0gJ25vLWNhY2hlJztcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ1ByYWdtYSddID0gJ25vLWNhY2hlJztcclxuXHJcblxyXG4gICAgLy8gSW5qZWN0cyBvdXIgSW50ZXJjZXB0b3JzIGZvciBSZXF1ZXN0XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgLy8gSW5qZWN0cyBvdXIgSW50ZXJjZXB0b3JzIGZvciBSZXNwb25zZVxyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG5cclxuXHJcbn1dKTtcclxuXHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgSFRUUE1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJdCBpbXBsZW1lbnQgYW4gYWJzdHJhY3QgY2FsbCB0byBIVFRQIEludGVyY2VwdG9ycyB0byBtYW5hZ2UgZXJyb3IgaGFuZGxlclxyXG4gKiBTb21ldGltZXMgYSByZXF1ZXN0IGNhbid0IGJlIHNlbnQgb3IgaXQgaXMgcmVqZWN0ZWQgYnkgYW4gaW50ZXJjZXB0b3IuXHJcbiAqIFJlcXVlc3QgZXJyb3IgaW50ZXJjZXB0b3IgY2FwdHVyZXMgcmVxdWVzdHMgdGhhdCBoYXZlIGJlZW4gY2FuY2VsZWQgYnkgYSBwcmV2aW91cyByZXF1ZXN0IGludGVyY2VwdG9yLlxyXG4gKiBJdCBjYW4gYmUgdXNlZCBpbiBvcmRlciB0byByZWNvdmVyIHRoZSByZXF1ZXN0IGFuZCBzb21ldGltZXMgdW5kbyB0aGluZ3MgdGhhdCBoYXZlIGJlZW4gc2V0IHVwIGJlZm9yZSBhIHJlcXVlc3QsXHJcbiAqIGxpa2UgcmVtb3Zpbmcgb3ZlcmxheXMgYW5kIGxvYWRpbmcgaW5kaWNhdG9ycywgZW5hYmxpbmcgYnV0dG9ucyBhbmQgZmllbGRzIGFuZCBzbyBvbi5cclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXF1ZXN0RXJyb3InKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlcXVlc3RFcnJvcihyZWplY3Rpb24pIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvL2lmIChjYW5SZWNvdmVyKHJlamVjdGlvbikpIHtcclxuICAgICAgICAvLyAgICByZXR1cm4gcmVzcG9uc2VPck5ld1Byb21pc2VcclxuICAgICAgICAvL31cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZWplY3Rpb24pO1xyXG5cclxuICAgICAgICByZXR1cm4gdGhpcy5xLnJlamVjdChyZWplY3Rpb24pO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlbkVycm9yKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIEl0IGltcGxlbWVudCBhbiBhYnN0cmFjdCBjYWxsIHRvIEhUVFAgSW50ZXJjZXB0b3JzIHRvIG1hbmFnZSBvbmx5IHJlcXVlc3RcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVxdWVzdCcpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlcXVlc3QoY29uZmlnKSB7XHJcbiAgICAgICAgLy8gV2UgY2FuIGFkZCBoZWFkZXJzIGlmIG9uIHRoZSBpbmNvbWluZyByZXF1ZXN0IG1hZGUgaXQgd2UgaGF2ZSB0aGUgdG9rZW4gaW5zaWRlXHJcbiAgICAgICAgLy8gZGVmaW5lZCBieSBzb21lIGNvbmRpdGlvbnNcclxuICAgICAgICAvL2NvbmZpZy5oZWFkZXJzWyd4LXNlc3Npb24tdG9rZW4nXSA9IG15LnRva2VuO1xyXG5cclxuICAgICAgICBjb25maWcucmVxdWVzdFRpbWVzdGFtcCA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShjb25maWcpO1xyXG5cclxuICAgICAgICByZXR1cm4gY29uZmlnIHx8IHRoaXMucS53aGVuKGNvbmZpZyk7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuUmVxdWVzdCgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJZiBiYWNrZW5kIGNhbGwgZmFpbHMgb3IgaXQgbWlnaHQgYmUgcmVqZWN0ZWQgYnkgYSByZXF1ZXN0IGludGVyY2VwdG9yIG9yIGJ5IGEgcHJldmlvdXMgcmVzcG9uc2UgaW50ZXJjZXB0b3I7XHJcbiAqIEluIHRob3NlIGNhc2VzLCByZXNwb25zZSBlcnJvciBpbnRlcmNlcHRvciBjYW4gaGVscCB1cyB0byByZWNvdmVyIHRoZSBiYWNrZW5kIGNhbGwuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVzcG9uc2VFcnJvcicpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3BvbnNlRXJyb3IocmVqZWN0aW9uKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy9pZiAoY2FuUmVjb3ZlcihyZWplY3Rpb24pKSB7XHJcbiAgICAgICAgLy8gICAgcmV0dXJuIHJlc3BvbnNlT3JOZXdQcm9taXNlXHJcbiAgICAgICAgLy8gfVxyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZWplY3Rpb24pO1xyXG4gICAgICAgIHJldHVybiB0aGlzLnEucmVqZWN0KHJlamVjdGlvbik7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuRXJyb3IoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogVGhpcyBtZXRob2QgaXMgY2FsbGVkIHJpZ2h0IGFmdGVyICRodHRwIHJlY2VpdmVzIHRoZSByZXNwb25zZSBmcm9tIHRoZSBiYWNrZW5kLFxyXG4gKiBzbyB5b3UgY2FuIG1vZGlmeSB0aGUgcmVzcG9uc2UgYW5kIG1ha2Ugb3RoZXIgYWN0aW9ucy4gVGhpcyBmdW5jdGlvbiByZWNlaXZlcyBhIHJlc3BvbnNlIG9iamVjdCBhcyBhIHBhcmFtZXRlclxyXG4gKiBhbmQgaGFzIHRvIHJldHVybiBhIHJlc3BvbnNlIG9iamVjdCBvciBhIHByb21pc2UuIFRoZSByZXNwb25zZSBvYmplY3QgaW5jbHVkZXNcclxuICogdGhlIHJlcXVlc3QgY29uZmlndXJhdGlvbiwgaGVhZGVycywgc3RhdHVzIGFuZCBkYXRhIHRoYXQgcmV0dXJuZWQgZnJvbSB0aGUgYmFja2VuZC5cclxuICogUmV0dXJuaW5nIGFuIGludmFsaWQgcmVzcG9uc2Ugb2JqZWN0IG9yIHByb21pc2UgdGhhdCB3aWxsIGJlIHJlamVjdGVkLCB3aWxsIG1ha2UgdGhlICRodHRwIGNhbGwgdG8gZmFpbC5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVzcG9uc2UnKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzcG9uc2UocmVzcG9uc2UpIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gc3VjY2Vzc1xyXG5cclxuICAgICAgICByZXNwb25zZS5jb25maWcucmVzcG9uc2VUaW1lc3RhbXAgPSBuZXcgRGF0ZSgpLmdldFRpbWUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVzcG9uc2UpO1xyXG4gICAgICAgIHJldHVybiByZXNwb25zZSB8fCB0aGlzLnEud2hlbihyZXNwb25zZSk7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuUmVzcG9uc2UoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxufVxyXG5cclxuIl19
