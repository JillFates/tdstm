(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
(function (global, factory) {
    if (typeof define === "function" && define.amd) {
        define(['module', 'select'], factory);
    } else if (typeof exports !== "undefined") {
        factory(module, require('select'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod, global.select);
        global.clipboardAction = mod.exports;
    }
})(this, function (module, _select) {
    'use strict';

    var _select2 = _interopRequireDefault(_select);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
        return typeof obj;
    } : function (obj) {
        return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
    };

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var _createClass = function () {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;
                if ("value" in descriptor) descriptor.writable = true;
                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function (Constructor, protoProps, staticProps) {
            if (protoProps) defineProperties(Constructor.prototype, protoProps);
            if (staticProps) defineProperties(Constructor, staticProps);
            return Constructor;
        };
    }();

    var ClipboardAction = function () {
        /**
         * @param {Object} options
         */
        function ClipboardAction(options) {
            _classCallCheck(this, ClipboardAction);

            this.resolveOptions(options);
            this.initSelection();
        }

        /**
         * Defines base properties passed from constructor.
         * @param {Object} options
         */


        _createClass(ClipboardAction, [{
            key: 'resolveOptions',
            value: function resolveOptions() {
                var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

                this.action = options.action;
                this.emitter = options.emitter;
                this.target = options.target;
                this.text = options.text;
                this.trigger = options.trigger;

                this.selectedText = '';
            }
        }, {
            key: 'initSelection',
            value: function initSelection() {
                if (this.text) {
                    this.selectFake();
                } else if (this.target) {
                    this.selectTarget();
                }
            }
        }, {
            key: 'selectFake',
            value: function selectFake() {
                var _this = this;

                var isRTL = document.documentElement.getAttribute('dir') == 'rtl';

                this.removeFake();

                this.fakeHandlerCallback = function () {
                    return _this.removeFake();
                };
                this.fakeHandler = document.body.addEventListener('click', this.fakeHandlerCallback) || true;

                this.fakeElem = document.createElement('textarea');
                // Prevent zooming on iOS
                this.fakeElem.style.fontSize = '12pt';
                // Reset box model
                this.fakeElem.style.border = '0';
                this.fakeElem.style.padding = '0';
                this.fakeElem.style.margin = '0';
                // Move element out of screen horizontally
                this.fakeElem.style.position = 'absolute';
                this.fakeElem.style[isRTL ? 'right' : 'left'] = '-9999px';
                // Move element to the same position vertically
                var yPosition = window.pageYOffset || document.documentElement.scrollTop;
                this.fakeElem.addEventListener('focus', window.scrollTo(0, yPosition));
                this.fakeElem.style.top = yPosition + 'px';

                this.fakeElem.setAttribute('readonly', '');
                this.fakeElem.value = this.text;

                document.body.appendChild(this.fakeElem);

                this.selectedText = (0, _select2.default)(this.fakeElem);
                this.copyText();
            }
        }, {
            key: 'removeFake',
            value: function removeFake() {
                if (this.fakeHandler) {
                    document.body.removeEventListener('click', this.fakeHandlerCallback);
                    this.fakeHandler = null;
                    this.fakeHandlerCallback = null;
                }

                if (this.fakeElem) {
                    document.body.removeChild(this.fakeElem);
                    this.fakeElem = null;
                }
            }
        }, {
            key: 'selectTarget',
            value: function selectTarget() {
                this.selectedText = (0, _select2.default)(this.target);
                this.copyText();
            }
        }, {
            key: 'copyText',
            value: function copyText() {
                var succeeded = void 0;

                try {
                    succeeded = document.execCommand(this.action);
                } catch (err) {
                    succeeded = false;
                }

                this.handleResult(succeeded);
            }
        }, {
            key: 'handleResult',
            value: function handleResult(succeeded) {
                this.emitter.emit(succeeded ? 'success' : 'error', {
                    action: this.action,
                    text: this.selectedText,
                    trigger: this.trigger,
                    clearSelection: this.clearSelection.bind(this)
                });
            }
        }, {
            key: 'clearSelection',
            value: function clearSelection() {
                if (this.target) {
                    this.target.blur();
                }

                window.getSelection().removeAllRanges();
            }
        }, {
            key: 'destroy',
            value: function destroy() {
                this.removeFake();
            }
        }, {
            key: 'action',
            set: function set() {
                var action = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'copy';

                this._action = action;

                if (this._action !== 'copy' && this._action !== 'cut') {
                    throw new Error('Invalid "action" value, use either "copy" or "cut"');
                }
            },
            get: function get() {
                return this._action;
            }
        }, {
            key: 'target',
            set: function set(target) {
                if (target !== undefined) {
                    if (target && (typeof target === 'undefined' ? 'undefined' : _typeof(target)) === 'object' && target.nodeType === 1) {
                        if (this.action === 'copy' && target.hasAttribute('disabled')) {
                            throw new Error('Invalid "target" attribute. Please use "readonly" instead of "disabled" attribute');
                        }

                        if (this.action === 'cut' && (target.hasAttribute('readonly') || target.hasAttribute('disabled'))) {
                            throw new Error('Invalid "target" attribute. You can\'t cut text from elements with "readonly" or "disabled" attributes');
                        }

                        this._target = target;
                    } else {
                        throw new Error('Invalid "target" value, use a valid Element');
                    }
                }
            },
            get: function get() {
                return this._target;
            }
        }]);

        return ClipboardAction;
    }();

    module.exports = ClipboardAction;
});
},{"select":8}],2:[function(require,module,exports){
(function (global, factory) {
    if (typeof define === "function" && define.amd) {
        define(['module', './clipboard-action', 'tiny-emitter', 'good-listener'], factory);
    } else if (typeof exports !== "undefined") {
        factory(module, require('./clipboard-action'), require('tiny-emitter'), require('good-listener'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod, global.clipboardAction, global.tinyEmitter, global.goodListener);
        global.clipboard = mod.exports;
    }
})(this, function (module, _clipboardAction, _tinyEmitter, _goodListener) {
    'use strict';

    var _clipboardAction2 = _interopRequireDefault(_clipboardAction);

    var _tinyEmitter2 = _interopRequireDefault(_tinyEmitter);

    var _goodListener2 = _interopRequireDefault(_goodListener);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var _createClass = function () {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;
                if ("value" in descriptor) descriptor.writable = true;
                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function (Constructor, protoProps, staticProps) {
            if (protoProps) defineProperties(Constructor.prototype, protoProps);
            if (staticProps) defineProperties(Constructor, staticProps);
            return Constructor;
        };
    }();

    function _possibleConstructorReturn(self, call) {
        if (!self) {
            throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
        }

        return call && (typeof call === "object" || typeof call === "function") ? call : self;
    }

    function _inherits(subClass, superClass) {
        if (typeof superClass !== "function" && superClass !== null) {
            throw new TypeError("Super expression must either be null or a function, not " + typeof superClass);
        }

        subClass.prototype = Object.create(superClass && superClass.prototype, {
            constructor: {
                value: subClass,
                enumerable: false,
                writable: true,
                configurable: true
            }
        });
        if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
    }

    var Clipboard = function (_Emitter) {
        _inherits(Clipboard, _Emitter);

        /**
         * @param {String|HTMLElement|HTMLCollection|NodeList} trigger
         * @param {Object} options
         */
        function Clipboard(trigger, options) {
            _classCallCheck(this, Clipboard);

            var _this = _possibleConstructorReturn(this, (Clipboard.__proto__ || Object.getPrototypeOf(Clipboard)).call(this));

            _this.resolveOptions(options);
            _this.listenClick(trigger);
            return _this;
        }

        /**
         * Defines if attributes would be resolved using internal setter functions
         * or custom functions that were passed in the constructor.
         * @param {Object} options
         */


        _createClass(Clipboard, [{
            key: 'resolveOptions',
            value: function resolveOptions() {
                var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

                this.action = typeof options.action === 'function' ? options.action : this.defaultAction;
                this.target = typeof options.target === 'function' ? options.target : this.defaultTarget;
                this.text = typeof options.text === 'function' ? options.text : this.defaultText;
            }
        }, {
            key: 'listenClick',
            value: function listenClick(trigger) {
                var _this2 = this;

                this.listener = (0, _goodListener2.default)(trigger, 'click', function (e) {
                    return _this2.onClick(e);
                });
            }
        }, {
            key: 'onClick',
            value: function onClick(e) {
                var trigger = e.delegateTarget || e.currentTarget;

                if (this.clipboardAction) {
                    this.clipboardAction = null;
                }

                this.clipboardAction = new _clipboardAction2.default({
                    action: this.action(trigger),
                    target: this.target(trigger),
                    text: this.text(trigger),
                    trigger: trigger,
                    emitter: this
                });
            }
        }, {
            key: 'defaultAction',
            value: function defaultAction(trigger) {
                return getAttributeValue('action', trigger);
            }
        }, {
            key: 'defaultTarget',
            value: function defaultTarget(trigger) {
                var selector = getAttributeValue('target', trigger);

                if (selector) {
                    return document.querySelector(selector);
                }
            }
        }, {
            key: 'defaultText',
            value: function defaultText(trigger) {
                return getAttributeValue('text', trigger);
            }
        }, {
            key: 'destroy',
            value: function destroy() {
                this.listener.destroy();

                if (this.clipboardAction) {
                    this.clipboardAction.destroy();
                    this.clipboardAction = null;
                }
            }
        }]);

        return Clipboard;
    }(_tinyEmitter2.default);

    /**
     * Helper function to retrieve attribute value.
     * @param {String} suffix
     * @param {Element} element
     */
    function getAttributeValue(suffix, element) {
        var attribute = 'data-clipboard-' + suffix;

        if (!element.hasAttribute(attribute)) {
            return;
        }

        return element.getAttribute(attribute);
    }

    module.exports = Clipboard;
});
},{"./clipboard-action":1,"good-listener":6,"tiny-emitter":9}],3:[function(require,module,exports){
/**
 * A polyfill for Element.matches()
 */
if (Element && !Element.prototype.matches) {
    var proto = Element.prototype;

    proto.matches = proto.matchesSelector ||
                    proto.mozMatchesSelector ||
                    proto.msMatchesSelector ||
                    proto.oMatchesSelector ||
                    proto.webkitMatchesSelector;
}

/**
 * Finds the closest parent that matches a selector.
 *
 * @param {Element} element
 * @param {String} selector
 * @return {Function}
 */
function closest (element, selector) {
    while (element && element !== document) {
        if (element.matches(selector)) return element;
        element = element.parentNode;
    }
}

module.exports = closest;

},{}],4:[function(require,module,exports){
var closest = require('./closest');

/**
 * Delegates event to a selector.
 *
 * @param {Element} element
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @param {Boolean} useCapture
 * @return {Object}
 */
function delegate(element, selector, type, callback, useCapture) {
    var listenerFn = listener.apply(this, arguments);

    element.addEventListener(type, listenerFn, useCapture);

    return {
        destroy: function() {
            element.removeEventListener(type, listenerFn, useCapture);
        }
    }
}

/**
 * Finds closest match and invokes callback.
 *
 * @param {Element} element
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @return {Function}
 */
function listener(element, selector, type, callback) {
    return function(e) {
        e.delegateTarget = closest(e.target, selector);

        if (e.delegateTarget) {
            callback.call(element, e);
        }
    }
}

module.exports = delegate;

},{"./closest":3}],5:[function(require,module,exports){
/**
 * Check if argument is a HTML element.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.node = function(value) {
    return value !== undefined
        && value instanceof HTMLElement
        && value.nodeType === 1;
};

/**
 * Check if argument is a list of HTML elements.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.nodeList = function(value) {
    var type = Object.prototype.toString.call(value);

    return value !== undefined
        && (type === '[object NodeList]' || type === '[object HTMLCollection]')
        && ('length' in value)
        && (value.length === 0 || exports.node(value[0]));
};

/**
 * Check if argument is a string.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.string = function(value) {
    return typeof value === 'string'
        || value instanceof String;
};

/**
 * Check if argument is a function.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.fn = function(value) {
    var type = Object.prototype.toString.call(value);

    return type === '[object Function]';
};

},{}],6:[function(require,module,exports){
var is = require('./is');
var delegate = require('delegate');

/**
 * Validates all params and calls the right
 * listener function based on its target type.
 *
 * @param {String|HTMLElement|HTMLCollection|NodeList} target
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listen(target, type, callback) {
    if (!target && !type && !callback) {
        throw new Error('Missing required arguments');
    }

    if (!is.string(type)) {
        throw new TypeError('Second argument must be a String');
    }

    if (!is.fn(callback)) {
        throw new TypeError('Third argument must be a Function');
    }

    if (is.node(target)) {
        return listenNode(target, type, callback);
    }
    else if (is.nodeList(target)) {
        return listenNodeList(target, type, callback);
    }
    else if (is.string(target)) {
        return listenSelector(target, type, callback);
    }
    else {
        throw new TypeError('First argument must be a String, HTMLElement, HTMLCollection, or NodeList');
    }
}

/**
 * Adds an event listener to a HTML element
 * and returns a remove listener function.
 *
 * @param {HTMLElement} node
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenNode(node, type, callback) {
    node.addEventListener(type, callback);

    return {
        destroy: function() {
            node.removeEventListener(type, callback);
        }
    }
}

/**
 * Add an event listener to a list of HTML elements
 * and returns a remove listener function.
 *
 * @param {NodeList|HTMLCollection} nodeList
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenNodeList(nodeList, type, callback) {
    Array.prototype.forEach.call(nodeList, function(node) {
        node.addEventListener(type, callback);
    });

    return {
        destroy: function() {
            Array.prototype.forEach.call(nodeList, function(node) {
                node.removeEventListener(type, callback);
            });
        }
    }
}

/**
 * Add an event listener to a selector
 * and returns a remove listener function.
 *
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenSelector(selector, type, callback) {
    return delegate(document.body, selector, type, callback);
}

module.exports = listen;

},{"./is":5,"delegate":4}],7:[function(require,module,exports){
/*! ngclipboard - v1.1.1 - 2016-02-26
* https://github.com/sachinchoolur/ngclipboard
* Copyright (c) 2016 Sachin; Licensed MIT */
(function() {
    'use strict';
    var MODULE_NAME = 'ngclipboard';
    var angular, Clipboard;
    
    // Check for CommonJS support
    if (typeof module === 'object' && module.exports) {
      angular = require('angular');
      Clipboard = require('clipboard');
      module.exports = MODULE_NAME;
    } else {
      angular = window.angular;
      Clipboard = window.Clipboard;
    }

    angular.module(MODULE_NAME, []).directive('ngclipboard', function() {
        return {
            restrict: 'A',
            scope: {
                ngclipboardSuccess: '&',
                ngclipboardError: '&'
            },
            link: function(scope, element) {
                var clipboard = new Clipboard(element[0]);

                clipboard.on('success', function(e) {
                  scope.$apply(function () {
                    scope.ngclipboardSuccess({
                      e: e
                    });
                  });
                });

                clipboard.on('error', function(e) {
                  scope.$apply(function () {
                    scope.ngclipboardError({
                      e: e
                    });
                  });
                });

            }
        };
    });
}());

},{"angular":"angular","clipboard":2}],8:[function(require,module,exports){
function select(element) {
    var selectedText;

    if (element.nodeName === 'SELECT') {
        element.focus();

        selectedText = element.value;
    }
    else if (element.nodeName === 'INPUT' || element.nodeName === 'TEXTAREA') {
        element.focus();
        element.setSelectionRange(0, element.value.length);

        selectedText = element.value;
    }
    else {
        if (element.hasAttribute('contenteditable')) {
            element.focus();
        }

        var selection = window.getSelection();
        var range = document.createRange();

        range.selectNodeContents(element);
        selection.removeAllRanges();
        selection.addRange(range);

        selectedText = selection.toString();
    }

    return selectedText;
}

module.exports = select;

},{}],9:[function(require,module,exports){
function E () {
  // Keep this empty so it's easier to inherit from
  // (via https://github.com/lipsmack from https://github.com/scottcorgan/tiny-emitter/issues/3)
}

E.prototype = {
  on: function (name, callback, ctx) {
    var e = this.e || (this.e = {});

    (e[name] || (e[name] = [])).push({
      fn: callback,
      ctx: ctx
    });

    return this;
  },

  once: function (name, callback, ctx) {
    var self = this;
    function listener () {
      self.off(name, listener);
      callback.apply(ctx, arguments);
    };

    listener._ = callback
    return this.on(name, listener, ctx);
  },

  emit: function (name) {
    var data = [].slice.call(arguments, 1);
    var evtArr = ((this.e || (this.e = {}))[name] || []).slice();
    var i = 0;
    var len = evtArr.length;

    for (i; i < len; i++) {
      evtArr[i].fn.apply(evtArr[i].ctx, data);
    }

    return this;
  },

  off: function (name, callback) {
    var e = this.e || (this.e = {});
    var evts = e[name];
    var liveEvents = [];

    if (evts && callback) {
      for (var i = 0, len = evts.length; i < len; i++) {
        if (evts[i].fn !== callback && evts[i].fn._ !== callback)
          liveEvents.push(evts[i]);
      }
    }

    // Remove event from queue to prevent memory leak
    // Suggested by https://github.com/lazd
    // Ref: https://github.com/scottcorgan/tiny-emitter/commit/c6ebfaa9bc973b33d110a84a307742b7cf94c953#commitcomment-5024910

    (liveEvents.length)
      ? e[name] = liveEvents
      : delete e[name];

    return this;
  }
};

module.exports = E;

},{}],10:[function(require,module,exports){
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

window.TDSTM = TDSTM;

},{"./App.js":11}],11:[function(require,module,exports){
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
require('ngClipboard');
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
}]).run(['$transitions', '$http', '$log', '$location', function ($transitions, $http, $log, $location, $state, $stateParams, $locale) {
        $log.debug('Configuration deployed');

        $transitions.onBefore({}, function ($state, $transition$) {
                $log.log('In start ', $state);
        });
}]);

// we mapped the Provider Core list (compileProvider, controllerProvider, provideService, httpProvider) to reuse after on fly
TDSTM.ProviderCore = ProviderCore;

module.exports = TDSTM;

},{"../modules/header/HeaderModule.js":18,"../modules/licenseAdmin/LicenseAdminModule.js":19,"../modules/licenseManager/LicenseManagerModule.js":27,"../modules/noticeManager/NoticeManagerModule.js":32,"../modules/taskManager/TaskManagerModule.js":36,"../services/RestAPI/RestAPIModule.js":42,"../services/http/HTTPModule.js":45,"angular":"angular","angular-animate":"angular-animate","angular-formly":"angular-formly","angular-formly-templates-bootstrap":"angular-formly-templates-bootstrap","angular-mocks":"angular-mocks","angular-resource":"angular-resource","angular-sanitize":"angular-sanitize","angular-translate":"angular-translate","angular-translate-loader-partial":"angular-translate-loader-partial","angular-ui-bootstrap":"angular-ui-bootstrap","api-check":"api-check","ngClipboard":7,"rx-angular":"rx-angular","ui-router":"ui-router"}],12:[function(require,module,exports){
'use strict';

/**
 * Created by Jorge Morayta on 12/14/2015.
 * It handler the index for any of the directives available
 */

require('./tools/ToastHandler.js');
require('./tools/ModalWindowActivation.js');

},{"./tools/ModalWindowActivation.js":13,"./tools/ToastHandler.js":14}],13:[function(require,module,exports){
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

},{"../../config/App.js":11}],14:[function(require,module,exports){
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

},{"../../config/App.js":11}],15:[function(require,module,exports){
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

},{"./config/AngularProviderHelper.js":10,"./config/App.js":11,"./directives/index":12}],16:[function(require,module,exports){
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

},{}],17:[function(require,module,exports){
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

},{}],18:[function(require,module,exports){
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

exports.default = HeaderModule;

},{"../dialogAction/DialogAction.js":16,"./HeaderController.js":17,"angular":"angular"}],19:[function(require,module,exports){
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

exports.default = LicenseAdminModule;

},{"./applyLicenseKey/ApplyLicenseKey.js":20,"./created/CreatedLicense.js":21,"./detail/LicenseDetail.js":22,"./list/LicenseAdminList.js":23,"./manuallyRequest/ManuallyRequest.js":24,"./request/RequestLicense.js":25,"./service/LicenseAdminService.js":26,"angular":"angular","ui-router":"ui-router"}],20:[function(require,module,exports){
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

},{"../../utils/form/FormValidator.js":40}],21:[function(require,module,exports){
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

},{}],22:[function(require,module,exports){
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
            methodId: params.license.method.id,
            projectName: params.license.project.name,
            clientName: params.license.client.name,
            email: params.license.email,
            serversTokens: params.license.method.max,
            environment: params.license.environment,
            inception: params.license.requestDate,
            expiration: params.license.expirationDate,
            requestNote: params.license.requestNote,
            active: params.license.status.id === 1,
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
                id: 1,
                name: 'Servers'
            }, {
                id: 2,
                name: 'Tokens'
            }, {
                id: 3,
                name: 'Custom'
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

},{}],23:[function(require,module,exports){
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
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project', template: '<span style="text-transform: capitalize;">#=((data.project && data.project.name)? data.project.name.toLowerCase(): "" )#</span>' }, { field: 'email', title: 'Contact Email' }, { field: 'status', title: 'Status', template: '<span style="text-transform: capitalize;">#=((data.status)? data.status.toLowerCase(): "" )#</span>' }, { field: 'type.name', title: 'Type', template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#' }, { field: 'method.name', title: 'Method', template: '<span style="text-transform: capitalize;">#=((data.method && data.method.name)? data.method.name.toLowerCase(): "" )#</span>' }, { field: 'method.id', hidden: true }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'requestDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'environment', title: 'Environment', template: '<span style="text-transform: capitalize;">#=((data.environment)? data.environment.toLowerCase(): "" )#</span>' }],
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

},{}],24:[function(require,module,exports){
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
        this.licenseModel = {
            id: params.license.id,
            email: params.license.email,
            encryptedDetail: ''
        };

        // Get the hash code using the id.
        this.getHashCode();
    }

    _createClass(ManuallyRequest, [{
        key: 'getHashCode',
        value: function getHashCode() {
            var _this = this;

            this.licenseAdminService.getHashCode(this.licenseModel.id, function (data) {
                _this.licenseModel.encryptedDetail = data;
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

},{}],25:[function(require,module,exports){
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

},{"../../utils/form/FormValidator.js":40}],26:[function(require,module,exports){
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
        value: function resubmitLicenseRequest(license, callback) {
            var _this = this;

            this.restService.licenseAdminServiceHandler().resubmitLicenseRequest(license, function (data) {
                _this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'Request License was successfully.' });
                return callback(data);
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

},{}],27:[function(require,module,exports){
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
LicenseManagerModule.controller('LicenseManagerDetail', ['$log', '$scope', 'LicenseManagerService', '$uibModal', '$uibModalInstance', 'params', _LicenseManagerDetail2.default]);

exports.default = LicenseManagerModule;

},{"./detail/LicenseManagerDetail.js":28,"./list/LicenseManagerList.js":29,"./requestImport/RequestImport.js":30,"./service/LicenseManagerService.js":31,"angular":"angular","ui-router":"ui-router"}],28:[function(require,module,exports){
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

    function LicenseManagerDetail($log, $scope, licenseManagerService, $uibModal, $uibModalInstance, params) {
        _classCallCheck(this, LicenseManagerDetail);

        var _this = _possibleConstructorReturn(this, (LicenseManagerDetail.__proto__ || Object.getPrototypeOf(LicenseManagerDetail)).call(this, $log, $scope, $uibModal, $uibModalInstance));

        _this.scope = $scope;
        _this.licenseManagerService = licenseManagerService;
        _this.uibModalInstance = $uibModalInstance;
        _this.uibModal = $uibModal;
        _this.log = $log;

        _this.editMode = false;

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
            statusId: params.license.status.id,
            method: {
                id: params.license.method.id,
                name: params.license.method.name,
                max: params.license.method.max
            },
            environment: { id: params.license.environment.id },
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
        _this.getStatusDataSource();

        // Init the two Kendo Dates for Init and EndDate
        _this.initDate = {};
        _this.initDateOptions = {
            format: 'yyyy/MM/dd',
            open: function open(e) {
                _this.onChangeInitDate();
            },
            change: function change(e) {
                _this.onChangeInitDate();
            }
        };

        _this.endDate = {};
        _this.endDateOptions = {
            format: 'yyyy/MM/dd',
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
            this.pendingLicense = this.licenseModel.statusId === 4 && !this.editMode;
            this.expiredOrTerminated = this.licenseModel.statusId === 2 || this.licenseModel.statusId === 3;
            this.activeShowMode = this.licenseModel.statusId === 1 && !this.expiredOrTerminated && !this.editMode;
        }
    }, {
        key: 'prepareMethodOptions',
        value: function prepareMethodOptions() {
            this.methodOptions = [{
                id: 1,
                name: 'Servers',
                max: 0
            }, {
                id: 2,
                name: 'Tokens',
                max: 0
            }, {
                id: 3,
                name: 'Custom'
            }];
        }
    }, {
        key: 'prepareLicenseKey',
        value: function prepareLicenseKey() {
            var _this2 = this;

            if (this.licenseModel.statusId === 1) {
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
                columns: [{ field: 'dateCreated', title: 'Date', width: 180, type: 'date', format: '{0:dd/MMM/yyyy h:mm:ss tt}' }, { field: 'author.personName', title: 'Whom', width: 180 }, { field: 'changes', title: 'Action', template: '<ul>#for(var i = 0; i < data.changes.length; i++){#<li>#=data.changes[i].field# <br /> <span class="activity-list-old-val" style="color:darkred; font-weight: bold;">#=data.changes[i].oldValue#</span> | <span class="activity-list-new-val" style="color: green; font-weight: bold;">#=data.changes[i].newValue#</span></li>#}#</ul> ' }],
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
                    _this4.licenseModel.statusId = 1;
                    _this4.getStatusDataSource();
                    _this4.saveForm(_this4.licenseModel);
                    _this4.prepareControlActionButtons();
                    _this4.prepareLicenseKey();
                    _this4.reloadRequired = true;
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
                this.$log.warn('Invalid Number Expception', model);
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
                                if (!_this7.licenseModel.environmentId) {
                                    _this7.licenseModel.environmentId = data[0].id;
                                }

                                _this7.saveForm(_this7.licenseModel);
                                return e.success(data);
                            });
                        }
                    }
                },
                dataTextField: 'name',
                dataValueField: 'id',
                valuePrimitive: true
            };
        }

        /**
         * Populate values
         */

    }, {
        key: 'getStatusDataSource',
        value: function getStatusDataSource() {
            this.statusText = this.licenseModel.statusId === 1 ? 'Active' : this.licenseModel.statusId === 2 ? 'Expired' : this.licenseModel.statusId === 3 ? 'Terminated' : this.licenseModel.statusId === 4 ? 'Pending' : this.licenseModel.statusId === 5 ? 'Corrupt' : '';

            /*this.selectStatusListOptions = {
                dataSource: [
                    {id: 1, name: 'Active'},
                    {id: 2, name: 'Expired'},
                    {id: 3, name: 'Terminated'},
                    {id: 4, name: 'Pending'}
                ],
                dataTextField: 'name',
                dataValueField: 'id',
                valuePrimitive: true
            }*/
        }

        /**
         * A new Project has been selected, that means we need to reload the next project section
         * @param item
         */

    }, {
        key: 'onChangeProject',
        value: function onChangeProject(item) {
            this.log.info('On change Project', item);
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
            this.resetDropDown(this.selectEnvironment, this.licenseModel.environment.id);
            this.onChangeInitDate();
            this.onChangeEndDate();

            this.editMode = false;
            this.prepareControlActionButtons();
        }
    }]);

    return LicenseManagerDetail;
}(_FormValidator3.default);

exports.default = LicenseManagerDetail;

},{"../../utils/form/FormValidator.js":40}],29:[function(require,module,exports){
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
                columns: [{ field: 'id', hidden: true }, { field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseManagerList.onLicenseManagerDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'owner.name', title: 'Owner' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project' }, { field: 'email', title: 'Contact Email' }, { field: 'status.name', title: 'Status' }, { field: 'type.name', title: 'Type', template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#' }, { field: 'method.name', title: 'Method' }, { field: 'method.id', hidden: true }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'activationDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'environment.name', title: 'Environment' }, { field: 'gracePeriodDays', hidden: true }],
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

},{}],30:[function(require,module,exports){
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

},{"../../utils/form/FormValidator.js":40}],31:[function(require,module,exports){
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

        /**
         * Save the License
         */

    }, {
        key: 'saveLicense',
        value: function saveLicense(license, onSuccess) {

            var licenseModified = {
                environment: { id: parseInt(license.environment.id) },
                method: {
                    id: parseInt(license.method.id)
                },
                activationDate: moment(license.initDate).format('YYYY-MM-DD'),
                expirationDate: moment(license.endDate).format('YYYY-MM-DD'),
                status: { id: license.statusId },
                project: {
                    id: license.project.id !== 'all' ? parseInt(license.project.id) : license.project.id, // We pass 'all' when is multiproject
                    name: license.project.name
                },
                bannerMessage: license.bannerMessage,
                gracePeriodDays: license.gracePeriodDays,
                websitename: license.websiteName,
                hostName: license.hostName
            };
            if (license.method !== 3) {
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
            var _this = this;

            this.restService.licenseManagerServiceHandler().activateLicense(license.id, function (data) {
                if (data.status === _this.statusSuccess) {
                    _this.rootScope.$emit('broadcast-msg', {
                        type: 'info',
                        text: 'The license was activated and the license was emailed.'
                    });
                    return callback(data);
                } else {
                    _this.rootScope.$emit('broadcast-msg', {
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
            var _this2 = this;

            var hash = {
                data: license.hash
            };

            this.restService.licenseManagerServiceHandler().requestImport(hash, function (data) {
                if (data.status === _this2.statusSuccess) {
                    _this2.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully Imported' });
                } else {
                    _this2.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was not applied. Review the provided License Key is correct.' });
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

},{}],32:[function(require,module,exports){
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

},{"./edit/EditNotice.js":33,"./list/NoticeList.js":34,"./service/NoticeManagerService.js":35,"angular":"angular","ui-router":"ui-router"}],33:[function(require,module,exports){
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
        };

        // On Edition Mode we cc the model and only the params we need
        if (params.notice) {
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
            this.typeDataSource = [{ typeId: 1, name: 'Prelogin' }, { typeId: 2, name: 'Postlogin' }
            //{typeId: 3, name: 'General'} Disabled until Phase II
            ];
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

},{}],34:[function(require,module,exports){
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

},{}],35:[function(require,module,exports){
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

},{}],36:[function(require,module,exports){
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

},{"./edit/TaskManagerEdit.js":37,"./list/TaskManagerController.js":38,"./service/TaskManagerService.js":39,"angular":"angular","ui-router":"ui-router"}],37:[function(require,module,exports){
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

},{}],38:[function(require,module,exports){
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

},{}],39:[function(require,module,exports){
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

},{}],40:[function(require,module,exports){
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
                    if (selectedId === value.id) {
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

},{}],41:[function(require,module,exports){
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

},{}],42:[function(require,module,exports){
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

},{"./RestServiceHandler.js":43,"angular":"angular"}],43:[function(require,module,exports){
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
        key: 'licenseAdminServiceHandler',
        value: function licenseAdminServiceHandler() {
            var _this2 = this;

            return {
                getLicense: function getLicense(onSuccess) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/licenses'), onSuccess);
                },
                getEnvironmentDataSource: function getEnvironmentDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/license/environment'), onSuccess);
                },
                getProjectDataSource: function getProjectDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/license/project'), onSuccess);
                },
                getLicenseList: function getLicenseList(onSuccess) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/license'), onSuccess);
                },
                createNewLicenseRequest: function createNewLicenseRequest(data, onSuccess, onError) {
                    _this2.req.method = 'POST';
                    _this2.req.url = '../ws/license/request';
                    _this2.req.data = data;
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http(_this2.req), onSuccess, onError);
                },
                applyLicense: function applyLicense(licenseId, data, onSuccess, onError) {
                    _this2.req.method = 'POST';
                    _this2.req.url = '../ws/license/' + licenseId + '/load';
                    _this2.req.data = data;
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http(_this2.req), onSuccess, onError);
                },
                getHashCode: function getHashCode(licenseId, onSuccess, onError) {
                    _this2.req.method = 'GET';
                    _this2.req.url = '../ws/license/' + licenseId + '/hash';
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http(_this2.req), onSuccess, onError);
                },
                //--------------------------------------------
                resubmitLicenseRequest: function resubmitLicenseRequest(data, callback) {
                    _this2.req.method = 'POST';
                    _this2.req.url = '../ws/???';
                    _this2.req.data = data;
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
                },
                emailRequest: function emailRequest(data, callback) {
                    _this2.req.method = 'POST';
                    _this2.req.url = '../ws/???';
                    _this2.req.data = data;
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
                },
                deleteLicense: function deleteLicense(data, onSuccess, onError) {
                    _this2.req.method = 'DELETE';
                    _this2.req.url = '../ws/license/' + data.id;
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http(_this2.req), onSuccess, onError);
                }
            };
        }
    }, {
        key: 'licenseManagerServiceHandler',
        value: function licenseManagerServiceHandler() {
            var _this3 = this;

            return {
                requestImport: function requestImport(data, onSuccess, onError) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/manager/license/request';
                    _this3.req.data = data;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                getLicenseList: function getLicenseList(onSuccess) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/manager/license'), onSuccess);
                },
                getProjectDataSource: function getProjectDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/license/project'), onSuccess);
                },
                getEnvironmentDataSource: function getEnvironmentDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/license/environment'), onSuccess);
                },
                getKeyCode: function getKeyCode(licenseId, onSuccess, onError) {
                    _this3.req.method = 'GET';
                    _this3.req.url = '../ws/manager/license/' + licenseId + '/key';
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                saveLicense: function saveLicense(licenseId, licenseModified, onSuccess, onError) {
                    _this3.req.method = 'PUT';
                    _this3.req.url = '../ws/manager/license/' + licenseId;
                    _this3.req.data = licenseModified;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                revokeLicense: function revokeLicense(data, onSuccess, onError) {
                    _this3.req.method = 'DELETE';
                    _this3.req.url = '../ws/manager/license/' + data.id;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                activateLicense: function activateLicense(licenseId, onSuccess, onError) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/manager/license/' + licenseId + '/activate';
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                getActivityLog: function getActivityLog(licenseId, onSuccess, onError) {
                    _this3.req.method = 'GET';
                    _this3.req.url = '../ws/manager/license/' + licenseId + '/activitylog';
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                }
            };
        }
    }, {
        key: 'noticeManagerServiceHandler',
        value: function noticeManagerServiceHandler() {
            var _this4 = this;

            return {
                getNoticeList: function getNoticeList(onSuccess) {
                    // real ws example
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http.get('../ws/notices'), onSuccess);
                },
                createNotice: function createNotice(data, onSuccess, onError) {
                    _this4.req.method = 'POST';
                    _this4.req.url = '../ws/notices';
                    _this4.req.data = data;
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                },
                editNotice: function editNotice(data, onSuccess, onError) {
                    _this4.req.method = 'PUT';
                    _this4.req.url = '../ws/notices/' + data.id;
                    _this4.req.data = data;
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                },
                deleteNotice: function deleteNotice(data, onSuccess, onError) {
                    _this4.req.method = 'DELETE';
                    _this4.req.url = '../ws/notices/' + data.id;
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http(_this4.req), onSuccess, onError);
                }
            };
        }
    }]);

    return RestServiceHandler;
}();

exports.default = RestServiceHandler;

},{"./RequestHandler.js":41}],44:[function(require,module,exports){
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

},{}],45:[function(require,module,exports){
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

},{"./HTTPRequestErrorHandlerInterceptor.js":46,"./HTTPRequestHandlerInterceptor.js":47,"./HTTPResponseErrorHandlerInterceptor.js":48,"./HTTPResponseHandlerInterceptor.js":49,"angular":"angular"}],46:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":44}],47:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":44}],48:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":44}],49:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":44}]},{},[15])
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvY2xpcGJvYXJkL2xpYi9jbGlwYm9hcmQtYWN0aW9uLmpzIiwibm9kZV9tb2R1bGVzL2NsaXBib2FyZC9saWIvY2xpcGJvYXJkLmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9jbG9zZXN0LmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9kZWxlZ2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9pcy5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9saXN0ZW4uanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvZGlzdC9uZ2NsaXBib2FyZC5qcyIsIm5vZGVfbW9kdWxlcy9zZWxlY3Qvc3JjL3NlbGVjdC5qcyIsIm5vZGVfbW9kdWxlcy90aW55LWVtaXR0ZXIvaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxjb25maWdcXEFwcC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxNb2RhbFdpbmRvd0FjdGl2YXRpb24uanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxUb2FzdEhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1haW4uanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGRpYWxvZ0FjdGlvblxcRGlhbG9nQWN0aW9uLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxoZWFkZXJcXEhlYWRlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGhlYWRlclxcSGVhZGVyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXExpY2Vuc2VBZG1pbk1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxhcHBseUxpY2Vuc2VLZXlcXEFwcGx5TGljZW5zZUtleS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxjcmVhdGVkXFxDcmVhdGVkTGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxkZXRhaWxcXExpY2Vuc2VEZXRhaWwuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcbGlzdFxcTGljZW5zZUFkbWluTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxtYW51YWxseVJlcXVlc3RcXE1hbnVhbGx5UmVxdWVzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxyZXF1ZXN0XFxSZXF1ZXN0TGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxzZXJ2aWNlXFxMaWNlbnNlQWRtaW5TZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcTGljZW5zZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxkZXRhaWxcXExpY2Vuc2VNYW5hZ2VyRGV0YWlsLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcbGlzdFxcTGljZW5zZU1hbmFnZXJMaXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxccmVxdWVzdEltcG9ydFxcUmVxdWVzdEltcG9ydC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXHNlcnZpY2VcXExpY2Vuc2VNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcTm90aWNlTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcZWRpdFxcRWRpdE5vdGljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcbGlzdFxcTm90aWNlTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcc2VydmljZVxcTm90aWNlTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxUYXNrTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGVkaXRcXFRhc2tNYW5hZ2VyRWRpdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGxpc3RcXFRhc2tNYW5hZ2VyQ29udHJvbGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXHNlcnZpY2VcXFRhc2tNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdXRpbHNcXGZvcm1cXEZvcm1WYWxpZGF0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RBUElNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXN0U2VydmljZUhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwT0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4TEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL0ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7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QUNyQjs7Ozs7R0FwQnVELGM7O2tCLEFBQXZDIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIihmdW5jdGlvbiAoZ2xvYmFsLCBmYWN0b3J5KSB7XG4gICAgaWYgKHR5cGVvZiBkZWZpbmUgPT09IFwiZnVuY3Rpb25cIiAmJiBkZWZpbmUuYW1kKSB7XG4gICAgICAgIGRlZmluZShbJ21vZHVsZScsICdzZWxlY3QnXSwgZmFjdG9yeSk7XG4gICAgfSBlbHNlIGlmICh0eXBlb2YgZXhwb3J0cyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgICAgICBmYWN0b3J5KG1vZHVsZSwgcmVxdWlyZSgnc2VsZWN0JykpO1xuICAgIH0gZWxzZSB7XG4gICAgICAgIHZhciBtb2QgPSB7XG4gICAgICAgICAgICBleHBvcnRzOiB7fVxuICAgICAgICB9O1xuICAgICAgICBmYWN0b3J5KG1vZCwgZ2xvYmFsLnNlbGVjdCk7XG4gICAgICAgIGdsb2JhbC5jbGlwYm9hcmRBY3Rpb24gPSBtb2QuZXhwb3J0cztcbiAgICB9XG59KSh0aGlzLCBmdW5jdGlvbiAobW9kdWxlLCBfc2VsZWN0KSB7XG4gICAgJ3VzZSBzdHJpY3QnO1xuXG4gICAgdmFyIF9zZWxlY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfc2VsZWN0KTtcblxuICAgIGZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7XG4gICAgICAgIHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7XG4gICAgICAgICAgICBkZWZhdWx0OiBvYmpcbiAgICAgICAgfTtcbiAgICB9XG5cbiAgICB2YXIgX3R5cGVvZiA9IHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiB0eXBlb2YgU3ltYm9sLml0ZXJhdG9yID09PSBcInN5bWJvbFwiID8gZnVuY3Rpb24gKG9iaikge1xuICAgICAgICByZXR1cm4gdHlwZW9mIG9iajtcbiAgICB9IDogZnVuY3Rpb24gKG9iaikge1xuICAgICAgICByZXR1cm4gb2JqICYmIHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiBvYmouY29uc3RydWN0b3IgPT09IFN5bWJvbCAmJiBvYmogIT09IFN5bWJvbC5wcm90b3R5cGUgPyBcInN5bWJvbFwiIDogdHlwZW9mIG9iajtcbiAgICB9O1xuXG4gICAgZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3Rvcikge1xuICAgICAgICBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIkNhbm5vdCBjYWxsIGEgY2xhc3MgYXMgYSBmdW5jdGlvblwiKTtcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBfY3JlYXRlQ2xhc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIGZ1bmN0aW9uIGRlZmluZVByb3BlcnRpZXModGFyZ2V0LCBwcm9wcykge1xuICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBwcm9wcy5sZW5ndGg7IGkrKykge1xuICAgICAgICAgICAgICAgIHZhciBkZXNjcmlwdG9yID0gcHJvcHNbaV07XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5lbnVtZXJhYmxlID0gZGVzY3JpcHRvci5lbnVtZXJhYmxlIHx8IGZhbHNlO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuY29uZmlndXJhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBpZiAoXCJ2YWx1ZVwiIGluIGRlc2NyaXB0b3IpIGRlc2NyaXB0b3Iud3JpdGFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIE9iamVjdC5kZWZpbmVQcm9wZXJ0eSh0YXJnZXQsIGRlc2NyaXB0b3Iua2V5LCBkZXNjcmlwdG9yKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBmdW5jdGlvbiAoQ29uc3RydWN0b3IsIHByb3RvUHJvcHMsIHN0YXRpY1Byb3BzKSB7XG4gICAgICAgICAgICBpZiAocHJvdG9Qcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvci5wcm90b3R5cGUsIHByb3RvUHJvcHMpO1xuICAgICAgICAgICAgaWYgKHN0YXRpY1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLCBzdGF0aWNQcm9wcyk7XG4gICAgICAgICAgICByZXR1cm4gQ29uc3RydWN0b3I7XG4gICAgICAgIH07XG4gICAgfSgpO1xuXG4gICAgdmFyIENsaXBib2FyZEFjdGlvbiA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgLyoqXG4gICAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvcHRpb25zXG4gICAgICAgICAqL1xuICAgICAgICBmdW5jdGlvbiBDbGlwYm9hcmRBY3Rpb24ob3B0aW9ucykge1xuICAgICAgICAgICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIENsaXBib2FyZEFjdGlvbik7XG5cbiAgICAgICAgICAgIHRoaXMucmVzb2x2ZU9wdGlvbnMob3B0aW9ucyk7XG4gICAgICAgICAgICB0aGlzLmluaXRTZWxlY3Rpb24oKTtcbiAgICAgICAgfVxuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBEZWZpbmVzIGJhc2UgcHJvcGVydGllcyBwYXNzZWQgZnJvbSBjb25zdHJ1Y3Rvci5cbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG5cblxuICAgICAgICBfY3JlYXRlQ2xhc3MoQ2xpcGJvYXJkQWN0aW9uLCBbe1xuICAgICAgICAgICAga2V5OiAncmVzb2x2ZU9wdGlvbnMnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlc29sdmVPcHRpb25zKCkge1xuICAgICAgICAgICAgICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiB7fTtcblxuICAgICAgICAgICAgICAgIHRoaXMuYWN0aW9uID0gb3B0aW9ucy5hY3Rpb247XG4gICAgICAgICAgICAgICAgdGhpcy5lbWl0dGVyID0gb3B0aW9ucy5lbWl0dGVyO1xuICAgICAgICAgICAgICAgIHRoaXMudGFyZ2V0ID0gb3B0aW9ucy50YXJnZXQ7XG4gICAgICAgICAgICAgICAgdGhpcy50ZXh0ID0gb3B0aW9ucy50ZXh0O1xuICAgICAgICAgICAgICAgIHRoaXMudHJpZ2dlciA9IG9wdGlvbnMudHJpZ2dlcjtcblxuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gJyc7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2luaXRTZWxlY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGluaXRTZWxlY3Rpb24oKSB7XG4gICAgICAgICAgICAgICAgaWYgKHRoaXMudGV4dCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdEZha2UoKTtcbiAgICAgICAgICAgICAgICB9IGVsc2UgaWYgKHRoaXMudGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0VGFyZ2V0KCk7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdzZWxlY3RGYWtlJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBzZWxlY3RGYWtlKCkge1xuICAgICAgICAgICAgICAgIHZhciBfdGhpcyA9IHRoaXM7XG5cbiAgICAgICAgICAgICAgICB2YXIgaXNSVEwgPSBkb2N1bWVudC5kb2N1bWVudEVsZW1lbnQuZ2V0QXR0cmlidXRlKCdkaXInKSA9PSAncnRsJztcblxuICAgICAgICAgICAgICAgIHRoaXMucmVtb3ZlRmFrZSgpO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrID0gZnVuY3Rpb24gKCkge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gX3RoaXMucmVtb3ZlRmFrZSgpO1xuICAgICAgICAgICAgICAgIH07XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlciA9IGRvY3VtZW50LmJvZHkuYWRkRXZlbnRMaXN0ZW5lcignY2xpY2snLCB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2spIHx8IHRydWU7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtID0gZG9jdW1lbnQuY3JlYXRlRWxlbWVudCgndGV4dGFyZWEnKTtcbiAgICAgICAgICAgICAgICAvLyBQcmV2ZW50IHpvb21pbmcgb24gaU9TXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5mb250U2l6ZSA9ICcxMnB0JztcbiAgICAgICAgICAgICAgICAvLyBSZXNldCBib3ggbW9kZWxcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLmJvcmRlciA9ICcwJztcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLnBhZGRpbmcgPSAnMCc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5tYXJnaW4gPSAnMCc7XG4gICAgICAgICAgICAgICAgLy8gTW92ZSBlbGVtZW50IG91dCBvZiBzY3JlZW4gaG9yaXpvbnRhbGx5XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5wb3NpdGlvbiA9ICdhYnNvbHV0ZSc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZVtpc1JUTCA/ICdyaWdodCcgOiAnbGVmdCddID0gJy05OTk5cHgnO1xuICAgICAgICAgICAgICAgIC8vIE1vdmUgZWxlbWVudCB0byB0aGUgc2FtZSBwb3NpdGlvbiB2ZXJ0aWNhbGx5XG4gICAgICAgICAgICAgICAgdmFyIHlQb3NpdGlvbiA9IHdpbmRvdy5wYWdlWU9mZnNldCB8fCBkb2N1bWVudC5kb2N1bWVudEVsZW1lbnQuc2Nyb2xsVG9wO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uYWRkRXZlbnRMaXN0ZW5lcignZm9jdXMnLCB3aW5kb3cuc2Nyb2xsVG8oMCwgeVBvc2l0aW9uKSk7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS50b3AgPSB5UG9zaXRpb24gKyAncHgnO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zZXRBdHRyaWJ1dGUoJ3JlYWRvbmx5JywgJycpO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0udmFsdWUgPSB0aGlzLnRleHQ7XG5cbiAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LmFwcGVuZENoaWxkKHRoaXMuZmFrZUVsZW0pO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RlZFRleHQgPSAoMCwgX3NlbGVjdDIuZGVmYXVsdCkodGhpcy5mYWtlRWxlbSk7XG4gICAgICAgICAgICAgICAgdGhpcy5jb3B5VGV4dCgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdyZW1vdmVGYWtlJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiByZW1vdmVGYWtlKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLmZha2VIYW5kbGVyKSB7XG4gICAgICAgICAgICAgICAgICAgIGRvY3VtZW50LmJvZHkucmVtb3ZlRXZlbnRMaXN0ZW5lcignY2xpY2snLCB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2spO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyID0gbnVsbDtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5mYWtlRWxlbSkge1xuICAgICAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LnJlbW92ZUNoaWxkKHRoaXMuZmFrZUVsZW0pO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3NlbGVjdFRhcmdldCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gc2VsZWN0VGFyZ2V0KCkge1xuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gKDAsIF9zZWxlY3QyLmRlZmF1bHQpKHRoaXMudGFyZ2V0KTtcbiAgICAgICAgICAgICAgICB0aGlzLmNvcHlUZXh0KCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2NvcHlUZXh0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBjb3B5VGV4dCgpIHtcbiAgICAgICAgICAgICAgICB2YXIgc3VjY2VlZGVkID0gdm9pZCAwO1xuXG4gICAgICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICAgICAgc3VjY2VlZGVkID0gZG9jdW1lbnQuZXhlY0NvbW1hbmQodGhpcy5hY3Rpb24pO1xuICAgICAgICAgICAgICAgIH0gY2F0Y2ggKGVycikge1xuICAgICAgICAgICAgICAgICAgICBzdWNjZWVkZWQgPSBmYWxzZTtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB0aGlzLmhhbmRsZVJlc3VsdChzdWNjZWVkZWQpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdoYW5kbGVSZXN1bHQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGhhbmRsZVJlc3VsdChzdWNjZWVkZWQpIHtcbiAgICAgICAgICAgICAgICB0aGlzLmVtaXR0ZXIuZW1pdChzdWNjZWVkZWQgPyAnc3VjY2VzcycgOiAnZXJyb3InLCB7XG4gICAgICAgICAgICAgICAgICAgIGFjdGlvbjogdGhpcy5hY3Rpb24sXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6IHRoaXMuc2VsZWN0ZWRUZXh0LFxuICAgICAgICAgICAgICAgICAgICB0cmlnZ2VyOiB0aGlzLnRyaWdnZXIsXG4gICAgICAgICAgICAgICAgICAgIGNsZWFyU2VsZWN0aW9uOiB0aGlzLmNsZWFyU2VsZWN0aW9uLmJpbmQodGhpcylcbiAgICAgICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnY2xlYXJTZWxlY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGNsZWFyU2VsZWN0aW9uKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLnRhcmdldCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnRhcmdldC5ibHVyKCk7XG4gICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgd2luZG93LmdldFNlbGVjdGlvbigpLnJlbW92ZUFsbFJhbmdlcygpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZXN0cm95JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZXN0cm95KCkge1xuICAgICAgICAgICAgICAgIHRoaXMucmVtb3ZlRmFrZSgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdhY3Rpb24nLFxuICAgICAgICAgICAgc2V0OiBmdW5jdGlvbiBzZXQoKSB7XG4gICAgICAgICAgICAgICAgdmFyIGFjdGlvbiA9IGFyZ3VtZW50cy5sZW5ndGggPiAwICYmIGFyZ3VtZW50c1swXSAhPT0gdW5kZWZpbmVkID8gYXJndW1lbnRzWzBdIDogJ2NvcHknO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5fYWN0aW9uID0gYWN0aW9uO1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuX2FjdGlvbiAhPT0gJ2NvcHknICYmIHRoaXMuX2FjdGlvbiAhPT0gJ2N1dCcpIHtcbiAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwiYWN0aW9uXCIgdmFsdWUsIHVzZSBlaXRoZXIgXCJjb3B5XCIgb3IgXCJjdXRcIicpO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5fYWN0aW9uO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICd0YXJnZXQnLFxuICAgICAgICAgICAgc2V0OiBmdW5jdGlvbiBzZXQodGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgaWYgKHRhcmdldCAhPT0gdW5kZWZpbmVkKSB7XG4gICAgICAgICAgICAgICAgICAgIGlmICh0YXJnZXQgJiYgKHR5cGVvZiB0YXJnZXQgPT09ICd1bmRlZmluZWQnID8gJ3VuZGVmaW5lZCcgOiBfdHlwZW9mKHRhcmdldCkpID09PSAnb2JqZWN0JyAmJiB0YXJnZXQubm9kZVR5cGUgPT09IDEpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIGlmICh0aGlzLmFjdGlvbiA9PT0gJ2NvcHknICYmIHRhcmdldC5oYXNBdHRyaWJ1dGUoJ2Rpc2FibGVkJykpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJ0YXJnZXRcIiBhdHRyaWJ1dGUuIFBsZWFzZSB1c2UgXCJyZWFkb25seVwiIGluc3RlYWQgb2YgXCJkaXNhYmxlZFwiIGF0dHJpYnV0ZScpO1xuICAgICAgICAgICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgICAgICAgICBpZiAodGhpcy5hY3Rpb24gPT09ICdjdXQnICYmICh0YXJnZXQuaGFzQXR0cmlidXRlKCdyZWFkb25seScpIHx8IHRhcmdldC5oYXNBdHRyaWJ1dGUoJ2Rpc2FibGVkJykpKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwidGFyZ2V0XCIgYXR0cmlidXRlLiBZb3UgY2FuXFwndCBjdXQgdGV4dCBmcm9tIGVsZW1lbnRzIHdpdGggXCJyZWFkb25seVwiIG9yIFwiZGlzYWJsZWRcIiBhdHRyaWJ1dGVzJyk7XG4gICAgICAgICAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuX3RhcmdldCA9IHRhcmdldDtcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignSW52YWxpZCBcInRhcmdldFwiIHZhbHVlLCB1c2UgYSB2YWxpZCBFbGVtZW50Jyk7XG4gICAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMuX3RhcmdldDtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfV0pO1xuXG4gICAgICAgIHJldHVybiBDbGlwYm9hcmRBY3Rpb247XG4gICAgfSgpO1xuXG4gICAgbW9kdWxlLmV4cG9ydHMgPSBDbGlwYm9hcmRBY3Rpb247XG59KTsiLCIoZnVuY3Rpb24gKGdsb2JhbCwgZmFjdG9yeSkge1xuICAgIGlmICh0eXBlb2YgZGVmaW5lID09PSBcImZ1bmN0aW9uXCIgJiYgZGVmaW5lLmFtZCkge1xuICAgICAgICBkZWZpbmUoWydtb2R1bGUnLCAnLi9jbGlwYm9hcmQtYWN0aW9uJywgJ3RpbnktZW1pdHRlcicsICdnb29kLWxpc3RlbmVyJ10sIGZhY3RvcnkpO1xuICAgIH0gZWxzZSBpZiAodHlwZW9mIGV4cG9ydHMgIT09IFwidW5kZWZpbmVkXCIpIHtcbiAgICAgICAgZmFjdG9yeShtb2R1bGUsIHJlcXVpcmUoJy4vY2xpcGJvYXJkLWFjdGlvbicpLCByZXF1aXJlKCd0aW55LWVtaXR0ZXInKSwgcmVxdWlyZSgnZ29vZC1saXN0ZW5lcicpKTtcbiAgICB9IGVsc2Uge1xuICAgICAgICB2YXIgbW9kID0ge1xuICAgICAgICAgICAgZXhwb3J0czoge31cbiAgICAgICAgfTtcbiAgICAgICAgZmFjdG9yeShtb2QsIGdsb2JhbC5jbGlwYm9hcmRBY3Rpb24sIGdsb2JhbC50aW55RW1pdHRlciwgZ2xvYmFsLmdvb2RMaXN0ZW5lcik7XG4gICAgICAgIGdsb2JhbC5jbGlwYm9hcmQgPSBtb2QuZXhwb3J0cztcbiAgICB9XG59KSh0aGlzLCBmdW5jdGlvbiAobW9kdWxlLCBfY2xpcGJvYXJkQWN0aW9uLCBfdGlueUVtaXR0ZXIsIF9nb29kTGlzdGVuZXIpIHtcbiAgICAndXNlIHN0cmljdCc7XG5cbiAgICB2YXIgX2NsaXBib2FyZEFjdGlvbjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jbGlwYm9hcmRBY3Rpb24pO1xuXG4gICAgdmFyIF90aW55RW1pdHRlcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF90aW55RW1pdHRlcik7XG5cbiAgICB2YXIgX2dvb2RMaXN0ZW5lcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9nb29kTGlzdGVuZXIpO1xuXG4gICAgZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHtcbiAgICAgICAgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHtcbiAgICAgICAgICAgIGRlZmF1bHQ6IG9ialxuICAgICAgICB9O1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIF9jbGFzc0NhbGxDaGVjayhpbnN0YW5jZSwgQ29uc3RydWN0b3IpIHtcbiAgICAgICAgaWYgKCEoaW5zdGFuY2UgaW5zdGFuY2VvZiBDb25zdHJ1Y3RvcikpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoXCJDYW5ub3QgY2FsbCBhIGNsYXNzIGFzIGEgZnVuY3Rpb25cIik7XG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIgX2NyZWF0ZUNsYXNzID0gZnVuY3Rpb24gKCkge1xuICAgICAgICBmdW5jdGlvbiBkZWZpbmVQcm9wZXJ0aWVzKHRhcmdldCwgcHJvcHMpIHtcbiAgICAgICAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgcHJvcHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgICAgICAgICB2YXIgZGVzY3JpcHRvciA9IHByb3BzW2ldO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuZW51bWVyYWJsZSA9IGRlc2NyaXB0b3IuZW51bWVyYWJsZSB8fCBmYWxzZTtcbiAgICAgICAgICAgICAgICBkZXNjcmlwdG9yLmNvbmZpZ3VyYWJsZSA9IHRydWU7XG4gICAgICAgICAgICAgICAgaWYgKFwidmFsdWVcIiBpbiBkZXNjcmlwdG9yKSBkZXNjcmlwdG9yLndyaXRhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBPYmplY3QuZGVmaW5lUHJvcGVydHkodGFyZ2V0LCBkZXNjcmlwdG9yLmtleSwgZGVzY3JpcHRvcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gZnVuY3Rpb24gKENvbnN0cnVjdG9yLCBwcm90b1Byb3BzLCBzdGF0aWNQcm9wcykge1xuICAgICAgICAgICAgaWYgKHByb3RvUHJvcHMpIGRlZmluZVByb3BlcnRpZXMoQ29uc3RydWN0b3IucHJvdG90eXBlLCBwcm90b1Byb3BzKTtcbiAgICAgICAgICAgIGlmIChzdGF0aWNQcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvciwgc3RhdGljUHJvcHMpO1xuICAgICAgICAgICAgcmV0dXJuIENvbnN0cnVjdG9yO1xuICAgICAgICB9O1xuICAgIH0oKTtcblxuICAgIGZ1bmN0aW9uIF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHNlbGYsIGNhbGwpIHtcbiAgICAgICAgaWYgKCFzZWxmKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgUmVmZXJlbmNlRXJyb3IoXCJ0aGlzIGhhc24ndCBiZWVuIGluaXRpYWxpc2VkIC0gc3VwZXIoKSBoYXNuJ3QgYmVlbiBjYWxsZWRcIik7XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gY2FsbCAmJiAodHlwZW9mIGNhbGwgPT09IFwib2JqZWN0XCIgfHwgdHlwZW9mIGNhbGwgPT09IFwiZnVuY3Rpb25cIikgPyBjYWxsIDogc2VsZjtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBfaW5oZXJpdHMoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIHtcbiAgICAgICAgaWYgKHR5cGVvZiBzdXBlckNsYXNzICE9PSBcImZ1bmN0aW9uXCIgJiYgc3VwZXJDbGFzcyAhPT0gbnVsbCkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIlN1cGVyIGV4cHJlc3Npb24gbXVzdCBlaXRoZXIgYmUgbnVsbCBvciBhIGZ1bmN0aW9uLCBub3QgXCIgKyB0eXBlb2Ygc3VwZXJDbGFzcyk7XG4gICAgICAgIH1cblxuICAgICAgICBzdWJDbGFzcy5wcm90b3R5cGUgPSBPYmplY3QuY3JlYXRlKHN1cGVyQ2xhc3MgJiYgc3VwZXJDbGFzcy5wcm90b3R5cGUsIHtcbiAgICAgICAgICAgIGNvbnN0cnVjdG9yOiB7XG4gICAgICAgICAgICAgICAgdmFsdWU6IHN1YkNsYXNzLFxuICAgICAgICAgICAgICAgIGVudW1lcmFibGU6IGZhbHNlLFxuICAgICAgICAgICAgICAgIHdyaXRhYmxlOiB0cnVlLFxuICAgICAgICAgICAgICAgIGNvbmZpZ3VyYWJsZTogdHJ1ZVxuICAgICAgICAgICAgfVxuICAgICAgICB9KTtcbiAgICAgICAgaWYgKHN1cGVyQ2xhc3MpIE9iamVjdC5zZXRQcm90b3R5cGVPZiA/IE9iamVjdC5zZXRQcm90b3R5cGVPZihzdWJDbGFzcywgc3VwZXJDbGFzcykgOiBzdWJDbGFzcy5fX3Byb3RvX18gPSBzdXBlckNsYXNzO1xuICAgIH1cblxuICAgIHZhciBDbGlwYm9hcmQgPSBmdW5jdGlvbiAoX0VtaXR0ZXIpIHtcbiAgICAgICAgX2luaGVyaXRzKENsaXBib2FyZCwgX0VtaXR0ZXIpO1xuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBAcGFyYW0ge1N0cmluZ3xIVE1MRWxlbWVudHxIVE1MQ29sbGVjdGlvbnxOb2RlTGlzdH0gdHJpZ2dlclxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cbiAgICAgICAgZnVuY3Rpb24gQ2xpcGJvYXJkKHRyaWdnZXIsIG9wdGlvbnMpIHtcbiAgICAgICAgICAgIF9jbGFzc0NhbGxDaGVjayh0aGlzLCBDbGlwYm9hcmQpO1xuXG4gICAgICAgICAgICB2YXIgX3RoaXMgPSBfcG9zc2libGVDb25zdHJ1Y3RvclJldHVybih0aGlzLCAoQ2xpcGJvYXJkLl9fcHJvdG9fXyB8fCBPYmplY3QuZ2V0UHJvdG90eXBlT2YoQ2xpcGJvYXJkKSkuY2FsbCh0aGlzKSk7XG5cbiAgICAgICAgICAgIF90aGlzLnJlc29sdmVPcHRpb25zKG9wdGlvbnMpO1xuICAgICAgICAgICAgX3RoaXMubGlzdGVuQ2xpY2sodHJpZ2dlcik7XG4gICAgICAgICAgICByZXR1cm4gX3RoaXM7XG4gICAgICAgIH1cblxuICAgICAgICAvKipcbiAgICAgICAgICogRGVmaW5lcyBpZiBhdHRyaWJ1dGVzIHdvdWxkIGJlIHJlc29sdmVkIHVzaW5nIGludGVybmFsIHNldHRlciBmdW5jdGlvbnNcbiAgICAgICAgICogb3IgY3VzdG9tIGZ1bmN0aW9ucyB0aGF0IHdlcmUgcGFzc2VkIGluIHRoZSBjb25zdHJ1Y3Rvci5cbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG5cblxuICAgICAgICBfY3JlYXRlQ2xhc3MoQ2xpcGJvYXJkLCBbe1xuICAgICAgICAgICAga2V5OiAncmVzb2x2ZU9wdGlvbnMnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlc29sdmVPcHRpb25zKCkge1xuICAgICAgICAgICAgICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiB7fTtcblxuICAgICAgICAgICAgICAgIHRoaXMuYWN0aW9uID0gdHlwZW9mIG9wdGlvbnMuYWN0aW9uID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy5hY3Rpb24gOiB0aGlzLmRlZmF1bHRBY3Rpb247XG4gICAgICAgICAgICAgICAgdGhpcy50YXJnZXQgPSB0eXBlb2Ygb3B0aW9ucy50YXJnZXQgPT09ICdmdW5jdGlvbicgPyBvcHRpb25zLnRhcmdldCA6IHRoaXMuZGVmYXVsdFRhcmdldDtcbiAgICAgICAgICAgICAgICB0aGlzLnRleHQgPSB0eXBlb2Ygb3B0aW9ucy50ZXh0ID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy50ZXh0IDogdGhpcy5kZWZhdWx0VGV4dDtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnbGlzdGVuQ2xpY2snLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGxpc3RlbkNsaWNrKHRyaWdnZXIpIHtcbiAgICAgICAgICAgICAgICB2YXIgX3RoaXMyID0gdGhpcztcblxuICAgICAgICAgICAgICAgIHRoaXMubGlzdGVuZXIgPSAoMCwgX2dvb2RMaXN0ZW5lcjIuZGVmYXVsdCkodHJpZ2dlciwgJ2NsaWNrJywgZnVuY3Rpb24gKGUpIHtcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIF90aGlzMi5vbkNsaWNrKGUpO1xuICAgICAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdvbkNsaWNrJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBvbkNsaWNrKGUpIHtcbiAgICAgICAgICAgICAgICB2YXIgdHJpZ2dlciA9IGUuZGVsZWdhdGVUYXJnZXQgfHwgZS5jdXJyZW50VGFyZ2V0O1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuY2xpcGJvYXJkQWN0aW9uKSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB0aGlzLmNsaXBib2FyZEFjdGlvbiA9IG5ldyBfY2xpcGJvYXJkQWN0aW9uMi5kZWZhdWx0KHtcbiAgICAgICAgICAgICAgICAgICAgYWN0aW9uOiB0aGlzLmFjdGlvbih0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdGFyZ2V0OiB0aGlzLnRhcmdldCh0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogdGhpcy50ZXh0KHRyaWdnZXIpLFxuICAgICAgICAgICAgICAgICAgICB0cmlnZ2VyOiB0cmlnZ2VyLFxuICAgICAgICAgICAgICAgICAgICBlbWl0dGVyOiB0aGlzXG4gICAgICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRBY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRBY3Rpb24odHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHJldHVybiBnZXRBdHRyaWJ1dGVWYWx1ZSgnYWN0aW9uJywgdHJpZ2dlcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRUYXJnZXQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRUYXJnZXQodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHZhciBzZWxlY3RvciA9IGdldEF0dHJpYnV0ZVZhbHVlKCd0YXJnZXQnLCB0cmlnZ2VyKTtcblxuICAgICAgICAgICAgICAgIGlmIChzZWxlY3Rvcikge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gZG9jdW1lbnQucXVlcnlTZWxlY3RvcihzZWxlY3Rvcik7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZWZhdWx0VGV4dCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVmYXVsdFRleHQodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHJldHVybiBnZXRBdHRyaWJ1dGVWYWx1ZSgndGV4dCcsIHRyaWdnZXIpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZXN0cm95JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZXN0cm95KCkge1xuICAgICAgICAgICAgICAgIHRoaXMubGlzdGVuZXIuZGVzdHJveSgpO1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuY2xpcGJvYXJkQWN0aW9uKSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uLmRlc3Ryb3koKTtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24gPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfV0pO1xuXG4gICAgICAgIHJldHVybiBDbGlwYm9hcmQ7XG4gICAgfShfdGlueUVtaXR0ZXIyLmRlZmF1bHQpO1xuXG4gICAgLyoqXG4gICAgICogSGVscGVyIGZ1bmN0aW9uIHRvIHJldHJpZXZlIGF0dHJpYnV0ZSB2YWx1ZS5cbiAgICAgKiBAcGFyYW0ge1N0cmluZ30gc3VmZml4XG4gICAgICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gICAgICovXG4gICAgZnVuY3Rpb24gZ2V0QXR0cmlidXRlVmFsdWUoc3VmZml4LCBlbGVtZW50KSB7XG4gICAgICAgIHZhciBhdHRyaWJ1dGUgPSAnZGF0YS1jbGlwYm9hcmQtJyArIHN1ZmZpeDtcblxuICAgICAgICBpZiAoIWVsZW1lbnQuaGFzQXR0cmlidXRlKGF0dHJpYnV0ZSkpIHtcbiAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBlbGVtZW50LmdldEF0dHJpYnV0ZShhdHRyaWJ1dGUpO1xuICAgIH1cblxuICAgIG1vZHVsZS5leHBvcnRzID0gQ2xpcGJvYXJkO1xufSk7IiwiLyoqXG4gKiBBIHBvbHlmaWxsIGZvciBFbGVtZW50Lm1hdGNoZXMoKVxuICovXG5pZiAoRWxlbWVudCAmJiAhRWxlbWVudC5wcm90b3R5cGUubWF0Y2hlcykge1xuICAgIHZhciBwcm90byA9IEVsZW1lbnQucHJvdG90eXBlO1xuXG4gICAgcHJvdG8ubWF0Y2hlcyA9IHByb3RvLm1hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by5tb3pNYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ubXNNYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ub01hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by53ZWJraXRNYXRjaGVzU2VsZWN0b3I7XG59XG5cbi8qKlxuICogRmluZHMgdGhlIGNsb3Nlc3QgcGFyZW50IHRoYXQgbWF0Y2hlcyBhIHNlbGVjdG9yLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcmV0dXJuIHtGdW5jdGlvbn1cbiAqL1xuZnVuY3Rpb24gY2xvc2VzdCAoZWxlbWVudCwgc2VsZWN0b3IpIHtcbiAgICB3aGlsZSAoZWxlbWVudCAmJiBlbGVtZW50ICE9PSBkb2N1bWVudCkge1xuICAgICAgICBpZiAoZWxlbWVudC5tYXRjaGVzKHNlbGVjdG9yKSkgcmV0dXJuIGVsZW1lbnQ7XG4gICAgICAgIGVsZW1lbnQgPSBlbGVtZW50LnBhcmVudE5vZGU7XG4gICAgfVxufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGNsb3Nlc3Q7XG4iLCJ2YXIgY2xvc2VzdCA9IHJlcXVpcmUoJy4vY2xvc2VzdCcpO1xuXG4vKipcbiAqIERlbGVnYXRlcyBldmVudCB0byBhIHNlbGVjdG9yLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEBwYXJhbSB7Qm9vbGVhbn0gdXNlQ2FwdHVyZVxuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBkZWxlZ2F0ZShlbGVtZW50LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2ssIHVzZUNhcHR1cmUpIHtcbiAgICB2YXIgbGlzdGVuZXJGbiA9IGxpc3RlbmVyLmFwcGx5KHRoaXMsIGFyZ3VtZW50cyk7XG5cbiAgICBlbGVtZW50LmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgbGlzdGVuZXJGbiwgdXNlQ2FwdHVyZSk7XG5cbiAgICByZXR1cm4ge1xuICAgICAgICBkZXN0cm95OiBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIGVsZW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcih0eXBlLCBsaXN0ZW5lckZuLCB1c2VDYXB0dXJlKTtcbiAgICAgICAgfVxuICAgIH1cbn1cblxuLyoqXG4gKiBGaW5kcyBjbG9zZXN0IG1hdGNoIGFuZCBpbnZva2VzIGNhbGxiYWNrLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge0Z1bmN0aW9ufVxuICovXG5mdW5jdGlvbiBsaXN0ZW5lcihlbGVtZW50LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICByZXR1cm4gZnVuY3Rpb24oZSkge1xuICAgICAgICBlLmRlbGVnYXRlVGFyZ2V0ID0gY2xvc2VzdChlLnRhcmdldCwgc2VsZWN0b3IpO1xuXG4gICAgICAgIGlmIChlLmRlbGVnYXRlVGFyZ2V0KSB7XG4gICAgICAgICAgICBjYWxsYmFjay5jYWxsKGVsZW1lbnQsIGUpO1xuICAgICAgICB9XG4gICAgfVxufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGRlbGVnYXRlO1xuIiwiLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIEhUTUwgZWxlbWVudC5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gdmFsdWVcbiAqIEByZXR1cm4ge0Jvb2xlYW59XG4gKi9cbmV4cG9ydHMubm9kZSA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgcmV0dXJuIHZhbHVlICE9PSB1bmRlZmluZWRcbiAgICAgICAgJiYgdmFsdWUgaW5zdGFuY2VvZiBIVE1MRWxlbWVudFxuICAgICAgICAmJiB2YWx1ZS5ub2RlVHlwZSA9PT0gMTtcbn07XG5cbi8qKlxuICogQ2hlY2sgaWYgYXJndW1lbnQgaXMgYSBsaXN0IG9mIEhUTUwgZWxlbWVudHMuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLm5vZGVMaXN0ID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICB2YXIgdHlwZSA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbCh2YWx1ZSk7XG5cbiAgICByZXR1cm4gdmFsdWUgIT09IHVuZGVmaW5lZFxuICAgICAgICAmJiAodHlwZSA9PT0gJ1tvYmplY3QgTm9kZUxpc3RdJyB8fCB0eXBlID09PSAnW29iamVjdCBIVE1MQ29sbGVjdGlvbl0nKVxuICAgICAgICAmJiAoJ2xlbmd0aCcgaW4gdmFsdWUpXG4gICAgICAgICYmICh2YWx1ZS5sZW5ndGggPT09IDAgfHwgZXhwb3J0cy5ub2RlKHZhbHVlWzBdKSk7XG59O1xuXG4vKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgc3RyaW5nLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5zdHJpbmcgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHJldHVybiB0eXBlb2YgdmFsdWUgPT09ICdzdHJpbmcnXG4gICAgICAgIHx8IHZhbHVlIGluc3RhbmNlb2YgU3RyaW5nO1xufTtcblxuLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5mbiA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgdmFyIHR5cGUgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nLmNhbGwodmFsdWUpO1xuXG4gICAgcmV0dXJuIHR5cGUgPT09ICdbb2JqZWN0IEZ1bmN0aW9uXSc7XG59O1xuIiwidmFyIGlzID0gcmVxdWlyZSgnLi9pcycpO1xudmFyIGRlbGVnYXRlID0gcmVxdWlyZSgnZGVsZWdhdGUnKTtcblxuLyoqXG4gKiBWYWxpZGF0ZXMgYWxsIHBhcmFtcyBhbmQgY2FsbHMgdGhlIHJpZ2h0XG4gKiBsaXN0ZW5lciBmdW5jdGlvbiBiYXNlZCBvbiBpdHMgdGFyZ2V0IHR5cGUuXG4gKlxuICogQHBhcmFtIHtTdHJpbmd8SFRNTEVsZW1lbnR8SFRNTENvbGxlY3Rpb258Tm9kZUxpc3R9IHRhcmdldFxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3Rlbih0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgaWYgKCF0YXJnZXQgJiYgIXR5cGUgJiYgIWNhbGxiYWNrKSB7XG4gICAgICAgIHRocm93IG5ldyBFcnJvcignTWlzc2luZyByZXF1aXJlZCBhcmd1bWVudHMnKTtcbiAgICB9XG5cbiAgICBpZiAoIWlzLnN0cmluZyh0eXBlKSkge1xuICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdTZWNvbmQgYXJndW1lbnQgbXVzdCBiZSBhIFN0cmluZycpO1xuICAgIH1cblxuICAgIGlmICghaXMuZm4oY2FsbGJhY2spKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ1RoaXJkIGFyZ3VtZW50IG11c3QgYmUgYSBGdW5jdGlvbicpO1xuICAgIH1cblxuICAgIGlmIChpcy5ub2RlKHRhcmdldCkpIHtcbiAgICAgICAgcmV0dXJuIGxpc3Rlbk5vZGUodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2UgaWYgKGlzLm5vZGVMaXN0KHRhcmdldCkpIHtcbiAgICAgICAgcmV0dXJuIGxpc3Rlbk5vZGVMaXN0KHRhcmdldCwgdHlwZSwgY2FsbGJhY2spO1xuICAgIH1cbiAgICBlbHNlIGlmIChpcy5zdHJpbmcodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuU2VsZWN0b3IodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2Uge1xuICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdGaXJzdCBhcmd1bWVudCBtdXN0IGJlIGEgU3RyaW5nLCBIVE1MRWxlbWVudCwgSFRNTENvbGxlY3Rpb24sIG9yIE5vZGVMaXN0Jyk7XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZHMgYW4gZXZlbnQgbGlzdGVuZXIgdG8gYSBIVE1MIGVsZW1lbnRcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7SFRNTEVsZW1lbnR9IG5vZGVcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5Ob2RlKG5vZGUsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgbm9kZS5hZGRFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcblxuICAgIHJldHVybiB7XG4gICAgICAgIGRlc3Ryb3k6IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgbm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcbiAgICAgICAgfVxuICAgIH1cbn1cblxuLyoqXG4gKiBBZGQgYW4gZXZlbnQgbGlzdGVuZXIgdG8gYSBsaXN0IG9mIEhUTUwgZWxlbWVudHNcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7Tm9kZUxpc3R8SFRNTENvbGxlY3Rpb259IG5vZGVMaXN0XG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuTm9kZUxpc3Qobm9kZUxpc3QsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgQXJyYXkucHJvdG90eXBlLmZvckVhY2guY2FsbChub2RlTGlzdCwgZnVuY3Rpb24obm9kZSkge1xuICAgICAgICBub2RlLmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgIH0pO1xuXG4gICAgcmV0dXJuIHtcbiAgICAgICAgZGVzdHJveTogZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICBBcnJheS5wcm90b3R5cGUuZm9yRWFjaC5jYWxsKG5vZGVMaXN0LCBmdW5jdGlvbihub2RlKSB7XG4gICAgICAgICAgICAgICAgbm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcbiAgICAgICAgICAgIH0pO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZCBhbiBldmVudCBsaXN0ZW5lciB0byBhIHNlbGVjdG9yXG4gKiBhbmQgcmV0dXJucyBhIHJlbW92ZSBsaXN0ZW5lciBmdW5jdGlvbi5cbiAqXG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5TZWxlY3RvcihzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICByZXR1cm4gZGVsZWdhdGUoZG9jdW1lbnQuYm9keSwgc2VsZWN0b3IsIHR5cGUsIGNhbGxiYWNrKTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBsaXN0ZW47XG4iLCIvKiEgbmdjbGlwYm9hcmQgLSB2MS4xLjEgLSAyMDE2LTAyLTI2XHJcbiogaHR0cHM6Ly9naXRodWIuY29tL3NhY2hpbmNob29sdXIvbmdjbGlwYm9hcmRcclxuKiBDb3B5cmlnaHQgKGMpIDIwMTYgU2FjaGluOyBMaWNlbnNlZCBNSVQgKi9cclxuKGZ1bmN0aW9uKCkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgdmFyIE1PRFVMRV9OQU1FID0gJ25nY2xpcGJvYXJkJztcclxuICAgIHZhciBhbmd1bGFyLCBDbGlwYm9hcmQ7XHJcbiAgICBcclxuICAgIC8vIENoZWNrIGZvciBDb21tb25KUyBzdXBwb3J0XHJcbiAgICBpZiAodHlwZW9mIG1vZHVsZSA9PT0gJ29iamVjdCcgJiYgbW9kdWxlLmV4cG9ydHMpIHtcclxuICAgICAgYW5ndWxhciA9IHJlcXVpcmUoJ2FuZ3VsYXInKTtcclxuICAgICAgQ2xpcGJvYXJkID0gcmVxdWlyZSgnY2xpcGJvYXJkJyk7XHJcbiAgICAgIG1vZHVsZS5leHBvcnRzID0gTU9EVUxFX05BTUU7XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICBhbmd1bGFyID0gd2luZG93LmFuZ3VsYXI7XHJcbiAgICAgIENsaXBib2FyZCA9IHdpbmRvdy5DbGlwYm9hcmQ7XHJcbiAgICB9XHJcblxyXG4gICAgYW5ndWxhci5tb2R1bGUoTU9EVUxFX05BTUUsIFtdKS5kaXJlY3RpdmUoJ25nY2xpcGJvYXJkJywgZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgcmVzdHJpY3Q6ICdBJyxcclxuICAgICAgICAgICAgc2NvcGU6IHtcclxuICAgICAgICAgICAgICAgIG5nY2xpcGJvYXJkU3VjY2VzczogJyYnLFxyXG4gICAgICAgICAgICAgICAgbmdjbGlwYm9hcmRFcnJvcjogJyYnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGxpbms6IGZ1bmN0aW9uKHNjb3BlLCBlbGVtZW50KSB7XHJcbiAgICAgICAgICAgICAgICB2YXIgY2xpcGJvYXJkID0gbmV3IENsaXBib2FyZChlbGVtZW50WzBdKTtcclxuXHJcbiAgICAgICAgICAgICAgICBjbGlwYm9hcmQub24oJ3N1Y2Nlc3MnLCBmdW5jdGlvbihlKSB7XHJcbiAgICAgICAgICAgICAgICAgIHNjb3BlLiRhcHBseShmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2NvcGUubmdjbGlwYm9hcmRTdWNjZXNzKHtcclxuICAgICAgICAgICAgICAgICAgICAgIGU6IGVcclxuICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICBjbGlwYm9hcmQub24oJ2Vycm9yJywgZnVuY3Rpb24oZSkge1xyXG4gICAgICAgICAgICAgICAgICBzY29wZS4kYXBwbHkoZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNjb3BlLm5nY2xpcGJvYXJkRXJyb3Ioe1xyXG4gICAgICAgICAgICAgICAgICAgICAgZTogZVxyXG4gICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9KTtcclxufSgpKTtcclxuIiwiZnVuY3Rpb24gc2VsZWN0KGVsZW1lbnQpIHtcbiAgICB2YXIgc2VsZWN0ZWRUZXh0O1xuXG4gICAgaWYgKGVsZW1lbnQubm9kZU5hbWUgPT09ICdTRUxFQ1QnKSB7XG4gICAgICAgIGVsZW1lbnQuZm9jdXMoKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBlbGVtZW50LnZhbHVlO1xuICAgIH1cbiAgICBlbHNlIGlmIChlbGVtZW50Lm5vZGVOYW1lID09PSAnSU5QVVQnIHx8IGVsZW1lbnQubm9kZU5hbWUgPT09ICdURVhUQVJFQScpIHtcbiAgICAgICAgZWxlbWVudC5mb2N1cygpO1xuICAgICAgICBlbGVtZW50LnNldFNlbGVjdGlvblJhbmdlKDAsIGVsZW1lbnQudmFsdWUubGVuZ3RoKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBlbGVtZW50LnZhbHVlO1xuICAgIH1cbiAgICBlbHNlIHtcbiAgICAgICAgaWYgKGVsZW1lbnQuaGFzQXR0cmlidXRlKCdjb250ZW50ZWRpdGFibGUnKSkge1xuICAgICAgICAgICAgZWxlbWVudC5mb2N1cygpO1xuICAgICAgICB9XG5cbiAgICAgICAgdmFyIHNlbGVjdGlvbiA9IHdpbmRvdy5nZXRTZWxlY3Rpb24oKTtcbiAgICAgICAgdmFyIHJhbmdlID0gZG9jdW1lbnQuY3JlYXRlUmFuZ2UoKTtcblxuICAgICAgICByYW5nZS5zZWxlY3ROb2RlQ29udGVudHMoZWxlbWVudCk7XG4gICAgICAgIHNlbGVjdGlvbi5yZW1vdmVBbGxSYW5nZXMoKTtcbiAgICAgICAgc2VsZWN0aW9uLmFkZFJhbmdlKHJhbmdlKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBzZWxlY3Rpb24udG9TdHJpbmcoKTtcbiAgICB9XG5cbiAgICByZXR1cm4gc2VsZWN0ZWRUZXh0O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IHNlbGVjdDtcbiIsImZ1bmN0aW9uIEUgKCkge1xuICAvLyBLZWVwIHRoaXMgZW1wdHkgc28gaXQncyBlYXNpZXIgdG8gaW5oZXJpdCBmcm9tXG4gIC8vICh2aWEgaHR0cHM6Ly9naXRodWIuY29tL2xpcHNtYWNrIGZyb20gaHR0cHM6Ly9naXRodWIuY29tL3Njb3R0Y29yZ2FuL3RpbnktZW1pdHRlci9pc3N1ZXMvMylcbn1cblxuRS5wcm90b3R5cGUgPSB7XG4gIG9uOiBmdW5jdGlvbiAobmFtZSwgY2FsbGJhY2ssIGN0eCkge1xuICAgIHZhciBlID0gdGhpcy5lIHx8ICh0aGlzLmUgPSB7fSk7XG5cbiAgICAoZVtuYW1lXSB8fCAoZVtuYW1lXSA9IFtdKSkucHVzaCh7XG4gICAgICBmbjogY2FsbGJhY2ssXG4gICAgICBjdHg6IGN0eFxuICAgIH0pO1xuXG4gICAgcmV0dXJuIHRoaXM7XG4gIH0sXG5cbiAgb25jZTogZnVuY3Rpb24gKG5hbWUsIGNhbGxiYWNrLCBjdHgpIHtcbiAgICB2YXIgc2VsZiA9IHRoaXM7XG4gICAgZnVuY3Rpb24gbGlzdGVuZXIgKCkge1xuICAgICAgc2VsZi5vZmYobmFtZSwgbGlzdGVuZXIpO1xuICAgICAgY2FsbGJhY2suYXBwbHkoY3R4LCBhcmd1bWVudHMpO1xuICAgIH07XG5cbiAgICBsaXN0ZW5lci5fID0gY2FsbGJhY2tcbiAgICByZXR1cm4gdGhpcy5vbihuYW1lLCBsaXN0ZW5lciwgY3R4KTtcbiAgfSxcblxuICBlbWl0OiBmdW5jdGlvbiAobmFtZSkge1xuICAgIHZhciBkYXRhID0gW10uc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpO1xuICAgIHZhciBldnRBcnIgPSAoKHRoaXMuZSB8fCAodGhpcy5lID0ge30pKVtuYW1lXSB8fCBbXSkuc2xpY2UoKTtcbiAgICB2YXIgaSA9IDA7XG4gICAgdmFyIGxlbiA9IGV2dEFyci5sZW5ndGg7XG5cbiAgICBmb3IgKGk7IGkgPCBsZW47IGkrKykge1xuICAgICAgZXZ0QXJyW2ldLmZuLmFwcGx5KGV2dEFycltpXS5jdHgsIGRhdGEpO1xuICAgIH1cblxuICAgIHJldHVybiB0aGlzO1xuICB9LFxuXG4gIG9mZjogZnVuY3Rpb24gKG5hbWUsIGNhbGxiYWNrKSB7XG4gICAgdmFyIGUgPSB0aGlzLmUgfHwgKHRoaXMuZSA9IHt9KTtcbiAgICB2YXIgZXZ0cyA9IGVbbmFtZV07XG4gICAgdmFyIGxpdmVFdmVudHMgPSBbXTtcblxuICAgIGlmIChldnRzICYmIGNhbGxiYWNrKSB7XG4gICAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gZXZ0cy5sZW5ndGg7IGkgPCBsZW47IGkrKykge1xuICAgICAgICBpZiAoZXZ0c1tpXS5mbiAhPT0gY2FsbGJhY2sgJiYgZXZ0c1tpXS5mbi5fICE9PSBjYWxsYmFjaylcbiAgICAgICAgICBsaXZlRXZlbnRzLnB1c2goZXZ0c1tpXSk7XG4gICAgICB9XG4gICAgfVxuXG4gICAgLy8gUmVtb3ZlIGV2ZW50IGZyb20gcXVldWUgdG8gcHJldmVudCBtZW1vcnkgbGVha1xuICAgIC8vIFN1Z2dlc3RlZCBieSBodHRwczovL2dpdGh1Yi5jb20vbGF6ZFxuICAgIC8vIFJlZjogaHR0cHM6Ly9naXRodWIuY29tL3Njb3R0Y29yZ2FuL3RpbnktZW1pdHRlci9jb21taXQvYzZlYmZhYTliYzk3M2IzM2QxMTBhODRhMzA3NzQyYjdjZjk0Yzk1MyNjb21taXRjb21tZW50LTUwMjQ5MTBcblxuICAgIChsaXZlRXZlbnRzLmxlbmd0aClcbiAgICAgID8gZVtuYW1lXSA9IGxpdmVFdmVudHNcbiAgICAgIDogZGVsZXRlIGVbbmFtZV07XG5cbiAgICByZXR1cm4gdGhpcztcbiAgfVxufTtcblxubW9kdWxlLmV4cG9ydHMgPSBFO1xuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8yMC8yMDE1LlxyXG4gKiBURFNNIGlzIGEgZ2xvYmFsIG9iamVjdCB0aGF0IGNvbWVzIGZyb20gQXBwLmpzXHJcbiAqXHJcbiAqIFRoZSBmb2xsb3dpbmcgaGVscGVyIHdvcmtzIGluIGEgd2F5IHRvIG1ha2UgYXZhaWxhYmxlIHRoZSBjcmVhdGlvbiBvZiBEaXJlY3RpdmUsIFNlcnZpY2VzIGFuZCBDb250cm9sbGVyXHJcbiAqIG9uIGZseSBvciB3aGVuIGRlcGxveWluZyB0aGUgYXBwLlxyXG4gKlxyXG4gKiBXZSByZWR1Y2UgdGhlIHVzZSBvZiBjb21waWxlIGFuZCBleHRyYSBzdGVwc1xyXG4gKi9cclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4vQXBwLmpzJyk7XHJcblxyXG4vKipcclxuICogTGlzdGVuIHRvIGFuIGV4aXN0aW5nIGRpZ2VzdCBvZiB0aGUgY29tcGlsZSBwcm92aWRlciBhbmQgZXhlY3V0ZSB0aGUgJGFwcGx5IGltbWVkaWF0ZWx5IG9yIGFmdGVyIGl0J3MgcmVhZHlcclxuICogQHBhcmFtIGN1cnJlbnRcclxuICogQHBhcmFtIGZuXHJcbiAqL1xyXG5URFNUTS5zYWZlQXBwbHkgPSBmdW5jdGlvbiAoY3VycmVudCwgZm4pIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciBwaGFzZSA9IGN1cnJlbnQuJHJvb3QuJCRwaGFzZTtcclxuICAgIGlmIChwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRldmFsKGZuKTtcclxuICAgICAgICB9XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRhcHBseShmbik7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgY3VycmVudC4kYXBwbHkoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBkaXJlY3RpdmUgYXN5bmMgaWYgdGhlIGNvbXBpbGVQcm92aWRlciBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIuZGlyZWN0aXZlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5kaXJlY3RpdmUpIHtcclxuICAgICAgICBURFNUTS5kaXJlY3RpdmUoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBjb250cm9sbGVycyBhc3luYyBpZiB0aGUgY29udHJvbGxlclByb3ZpZGVyIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlQ29udHJvbGxlciA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbnRyb2xsZXJQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXJQcm92aWRlci5yZWdpc3RlcihzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uY29udHJvbGxlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXIoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBzZXJ2aWNlIGFzeW5jIGlmIHRoZSBwcm92aWRlU2VydmljZSBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZVNlcnZpY2UgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZSkge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZS5zZXJ2aWNlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5jb250cm9sbGVyKSB7XHJcbiAgICAgICAgVERTVE0uc2VydmljZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBGb3IgTGVnYWN5IHN5c3RlbSwgd2hhdCBpcyBkb2VzIGlzIHRvIHRha2UgcGFyYW1zIGZyb20gdGhlIHF1ZXJ5XHJcbiAqIG91dHNpZGUgdGhlIEFuZ3VsYXJKUyB1aS1yb3V0aW5nLlxyXG4gKiBAcGFyYW0gcGFyYW0gLy8gUGFyYW0gdG8gc2VhcmMgZm9yIC9leGFtcGxlLmh0bWw/YmFyPWZvbyNjdXJyZW50U3RhdGVcclxuICovXHJcblREU1RNLmdldFVSTFBhcmFtID0gZnVuY3Rpb24gKHBhcmFtKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICAkLnVybFBhcmFtID0gZnVuY3Rpb24gKG5hbWUpIHtcclxuICAgICAgICB2YXIgcmVzdWx0cyA9IG5ldyBSZWdFeHAoJ1tcXD8mXScgKyBuYW1lICsgJz0oW14mI10qKScpLmV4ZWMod2luZG93LmxvY2F0aW9uLmhyZWYpO1xyXG4gICAgICAgIGlmIChyZXN1bHRzID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgIHJldHVybiBudWxsO1xyXG4gICAgICAgIH1cclxuICAgICAgICBlbHNlIHtcclxuICAgICAgICAgICAgcmV0dXJuIHJlc3VsdHNbMV0gfHwgMDtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG5cclxuICAgIHJldHVybiAkLnVybFBhcmFtKHBhcmFtKTtcclxufTtcclxuXHJcbi8qKlxyXG4gKiBUaGlzIGNvZGUgd2FzIGludHJvZHVjZWQgb25seSBmb3IgdGhlIGlmcmFtZSBtaWdyYXRpb25cclxuICogaXQgZGV0ZWN0IHdoZW4gbW91c2UgZW50ZXJcclxuICovXHJcblREU1RNLmlmcmFtZUxvYWRlciA9IGZ1bmN0aW9uICgpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgICQoJy5pZnJhbWVMb2FkZXInKS5ob3ZlcihcclxuICAgICAgICBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICQoJy5uYXZiYXItdWwtY29udGFpbmVyIC5kcm9wZG93bi5vcGVuJykucmVtb3ZlQ2xhc3MoJ29wZW4nKTtcclxuICAgICAgICB9LCBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgfVxyXG4gICAgKTtcclxufTtcclxuXHJcbndpbmRvdy5URFNUTSA9IFREU1RNOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMTYvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5yZXF1aXJlKCdhbmd1bGFyJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItYW5pbWF0ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLW1vY2tzJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItc2FuaXRpemUnKTtcclxucmVxdWlyZSgnYW5ndWxhci1yZXNvdXJjZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXRyYW5zbGF0ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXRyYW5zbGF0ZS1sb2FkZXItcGFydGlhbCcpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXVpLWJvb3RzdHJhcCcpO1xyXG5yZXF1aXJlKCduZ0NsaXBib2FyZCcpO1xyXG5yZXF1aXJlKCd1aS1yb3V0ZXInKTtcclxucmVxdWlyZSgncngtYW5ndWxhcicpO1xyXG5yZXF1aXJlKCdhcGktY2hlY2snKTtcclxucmVxdWlyZSgnYW5ndWxhci1mb3JtbHknKTtcclxucmVxdWlyZSgnYW5ndWxhci1mb3JtbHktdGVtcGxhdGVzLWJvb3RzdHJhcCcpO1xyXG5cclxuLy8gTW9kdWxlc1xyXG5pbXBvcnQgSFRUUE1vZHVsZSBmcm9tICcuLi9zZXJ2aWNlcy9odHRwL0hUVFBNb2R1bGUuanMnO1xyXG5pbXBvcnQgUmVzdEFQSU1vZHVsZSBmcm9tICcuLi9zZXJ2aWNlcy9SZXN0QVBJL1Jlc3RBUElNb2R1bGUuanMnXHJcbmltcG9ydCBIZWFkZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9oZWFkZXIvSGVhZGVyTW9kdWxlLmpzJztcclxuaW1wb3J0IExpY2Vuc2VBZG1pbk1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9MaWNlbnNlQWRtaW5Nb2R1bGUuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9saWNlbnNlTWFuYWdlci9MaWNlbnNlTWFuYWdlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBOb3RpY2VNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbm90aWNlTWFuYWdlci9Ob3RpY2VNYW5hZ2VyTW9kdWxlLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvdGFza01hbmFnZXIvVGFza01hbmFnZXJNb2R1bGUuanMnO1xyXG5cclxudmFyIFByb3ZpZGVyQ29yZSA9IHt9O1xyXG5cclxudmFyIFREU1RNID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNJywgW1xyXG4gICAgJ25nU2FuaXRpemUnLFxyXG4gICAgJ25nUmVzb3VyY2UnLFxyXG4gICAgJ25nQW5pbWF0ZScsXHJcbiAgICAncGFzY2FscHJlY2h0LnRyYW5zbGF0ZScsIC8vICdhbmd1bGFyLXRyYW5zbGF0ZSdcclxuICAgICd1aS5yb3V0ZXInLFxyXG4gICAgJ25nY2xpcGJvYXJkJyxcclxuICAgICdrZW5kby5kaXJlY3RpdmVzJyxcclxuICAgICdyeCcsXHJcbiAgICAnZm9ybWx5JyxcclxuICAgICdmb3JtbHlCb290c3RyYXAnLFxyXG4gICAgJ3VpLmJvb3RzdHJhcCcsXHJcbiAgICBIVFRQTW9kdWxlLm5hbWUsXHJcbiAgICBSZXN0QVBJTW9kdWxlLm5hbWUsXHJcbiAgICBIZWFkZXJNb2R1bGUubmFtZSxcclxuICAgIFRhc2tNYW5hZ2VyTW9kdWxlLm5hbWUsXHJcbiAgICBMaWNlbnNlQWRtaW5Nb2R1bGUubmFtZSxcclxuICAgIExpY2Vuc2VNYW5hZ2VyTW9kdWxlLm5hbWUsXHJcbiAgICBOb3RpY2VNYW5hZ2VyTW9kdWxlLm5hbWVcclxuXSkuY29uZmlnKFtcclxuICAgICckbG9nUHJvdmlkZXInLFxyXG4gICAgJyRyb290U2NvcGVQcm92aWRlcicsXHJcbiAgICAnJGNvbXBpbGVQcm92aWRlcicsXHJcbiAgICAnJGNvbnRyb2xsZXJQcm92aWRlcicsXHJcbiAgICAnJHByb3ZpZGUnLFxyXG4gICAgJyRodHRwUHJvdmlkZXInLFxyXG4gICAgJyR0cmFuc2xhdGVQcm92aWRlcicsXHJcbiAgICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICAnJHVybFJvdXRlclByb3ZpZGVyJyxcclxuICAgICckbG9jYXRpb25Qcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJGxvZ1Byb3ZpZGVyLCAkcm9vdFNjb3BlUHJvdmlkZXIsICRjb21waWxlUHJvdmlkZXIsICRjb250cm9sbGVyUHJvdmlkZXIsICRwcm92aWRlLCAkaHR0cFByb3ZpZGVyLFxyXG4gICAgICAgICAgICAgICR0cmFuc2xhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlciwgJHVybFJvdXRlclByb3ZpZGVyLCAkbG9jYXRpb25Qcm92aWRlcikge1xyXG5cclxuICAgICAgICAkcm9vdFNjb3BlUHJvdmlkZXIuZGlnZXN0VHRsKDMwKTtcclxuICAgICAgICAvLyBHb2luZyBiYWNrIHRvIHlvdVxyXG4gICAgICAgICRsb2NhdGlvblByb3ZpZGVyLmh0bWw1TW9kZSh0cnVlKS5oYXNoUHJlZml4KCchJyk7XHJcblxyXG4gICAgICAgICRsb2dQcm92aWRlci5kZWJ1Z0VuYWJsZWQodHJ1ZSk7XHJcblxyXG4gICAgICAgIC8vIEFmdGVyIGJvb3RzdHJhcHBpbmcgYW5ndWxhciBmb3JnZXQgdGhlIHByb3ZpZGVyIHNpbmNlIGV2ZXJ5dGhpbmcgXCJ3YXMgYWxyZWFkeSBsb2FkZWRcIlxyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIgPSAkY29tcGlsZVByb3ZpZGVyO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5jb250cm9sbGVyUHJvdmlkZXIgPSAkY29udHJvbGxlclByb3ZpZGVyO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZSA9ICRwcm92aWRlO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5odHRwUHJvdmlkZXIgPSAkaHR0cFByb3ZpZGVyO1xyXG5cclxuICAgICAgICAvKipcclxuICAgICAgICAgKiBUcmFuc2xhdGlvbnNcclxuICAgICAgICAgKi9cclxuXHJcbiAgICAgICAgLyogICAgICAgICR0cmFuc2xhdGVQcm92aWRlci51c2VTYW5pdGl6ZVZhbHVlU3RyYXRlZ3kobnVsbCk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgndGRzdG0nKTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnVzZUxvYWRlcignJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXInLCB7XHJcbiAgICAgICAgICAgIHVybFRlbXBsYXRlOiAnLi4vaTE4bi97cGFydH0vYXBwLmkxOG4te2xhbmd9Lmpzb24nXHJcbiAgICAgICAgfSk7Ki9cclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnByZWZlcnJlZExhbmd1YWdlKCdlbl9VUycpO1xyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci5mYWxsYmFja0xhbmd1YWdlKCdlbl9VUycpO1xyXG5cclxuICAgICAgICAvLyR1cmxSb3V0ZXJQcm92aWRlci5vdGhlcndpc2UoJ2Rhc2hib2FyZCcpO1xyXG5cclxuICAgIH1dKS5cclxuICAgIHJ1bihbJyR0cmFuc2l0aW9ucycsICckaHR0cCcsICckbG9nJywgJyRsb2NhdGlvbicsIGZ1bmN0aW9uICgkdHJhbnNpdGlvbnMsICRodHRwLCAkbG9nLCAkbG9jYXRpb24sICRzdGF0ZSwgJHN0YXRlUGFyYW1zLCAkbG9jYWxlKSB7XHJcbiAgICAgICAgJGxvZy5kZWJ1ZygnQ29uZmlndXJhdGlvbiBkZXBsb3llZCcpO1xyXG5cclxuICAgICAgICAkdHJhbnNpdGlvbnMub25CZWZvcmUoIHt9LCBmdW5jdGlvbigkc3RhdGUsICR0cmFuc2l0aW9uJCkge1xyXG4gICAgICAgICAgICAkbG9nLmxvZygnSW4gc3RhcnQgJywgJHN0YXRlKTtcclxuICAgICAgICB9KTtcclxuXHJcbiAgICB9XSk7XHJcblxyXG4vLyB3ZSBtYXBwZWQgdGhlIFByb3ZpZGVyIENvcmUgbGlzdCAoY29tcGlsZVByb3ZpZGVyLCBjb250cm9sbGVyUHJvdmlkZXIsIHByb3ZpZGVTZXJ2aWNlLCBodHRwUHJvdmlkZXIpIHRvIHJldXNlIGFmdGVyIG9uIGZseVxyXG5URFNUTS5Qcm92aWRlckNvcmUgPSBQcm92aWRlckNvcmU7XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IFREU1RNOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTQvMjAxNS5cclxuICogSXQgaGFuZGxlciB0aGUgaW5kZXggZm9yIGFueSBvZiB0aGUgZGlyZWN0aXZlcyBhdmFpbGFibGVcclxuICovXHJcblxyXG5yZXF1aXJlKCcuL3Rvb2xzL1RvYXN0SGFuZGxlci5qcycpO1xyXG5yZXF1aXJlKCcuL3Rvb2xzL01vZGFsV2luZG93QWN0aXZhdGlvbi5qcycpO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMwLzEwLzIwMTYuXHJcbiAqIExpc3RlbiB0byBNb2RhbCBXaW5kb3cgdG8gbWFrZSBhbnkgbW9kYWwgd2luZG93IGRyYWdnYWJibGVcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgnbW9kYWxSZW5kZXInLCBbJyRsb2cnLCBmdW5jdGlvbiAoJGxvZykge1xyXG4gICAgJGxvZy5kZWJ1ZygnTW9kYWxXaW5kb3dBY3RpdmF0aW9uIGxvYWRlZCcpO1xyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICByZXN0cmljdDogJ0VBJyxcclxuICAgICAgICBsaW5rOiBmdW5jdGlvbigpIHtcclxuICAgICAgICAgICAgJCgnLm1vZGFsLWRpYWxvZycpLmRyYWdnYWJsZSh7XHJcbiAgICAgICAgICAgICAgICBoYW5kbGU6ICcubW9kYWwtaGVhZGVyJ1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG59XSk7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBQcmludHMgb3V0IGFsbCBUb2FzdCBtZXNzYWdlIHdoZW4gZGV0ZWN0ZWQgZnJvbSBzZXJ2ZXIgb3IgY3VzdG9tIG1zZyB1c2luZyB0aGUgZGlyZWN0aXZlIGl0c2VsZlxyXG4gKlxyXG4gKiBQcm9iYWJseSB2YWx1ZXMgYXJlOlxyXG4gKlxyXG4gKiBzdWNjZXNzLCBkYW5nZXIsIGluZm8sIHdhcm5pbmdcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgndG9hc3RIYW5kbGVyJywgWyckbG9nJywgJyR0aW1lb3V0JywgJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLFxyXG4gICAgJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicsICdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICBmdW5jdGlvbiAoJGxvZywgJHRpbWVvdXQsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLFxyXG4gICAgICAgICAgICAgIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciwgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IpIHtcclxuXHJcbiAgICAkbG9nLmRlYnVnKCdUb2FzdEhhbmRsZXIgbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHNjb3BlOiB7XHJcbiAgICAgICAgICAgIG1zZzogJz0nLFxyXG4gICAgICAgICAgICB0eXBlOiAnPScsXHJcbiAgICAgICAgICAgIHN0YXR1czogJz0nXHJcbiAgICAgICAgfSxcclxuICAgICAgICBwcmlvcml0eTogNSxcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9kaXJlY3RpdmVzL1Rvb2xzL1RvYXN0SGFuZGxlci5odG1sJyxcclxuICAgICAgICByZXN0cmljdDogJ0UnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6IFsnJHNjb3BlJywgJyRyb290U2NvcGUnLCBmdW5jdGlvbiAoJHNjb3BlLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgICAgICRzY29wZS5hbGVydCA9IHtcclxuICAgICAgICAgICAgICAgIHN1Y2Nlc3M6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRpbWU6IDIwMDBcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBkYW5nZXI6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRpbWU6IDQwMDBcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBpbmZvOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJyxcclxuICAgICAgICAgICAgICAgICAgICB0aW1lOiAyMDAwXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgd2FybmluZzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGltZTogNDAwMFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcyA9IHtcclxuICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlXHJcbiAgICAgICAgICAgIH07XHJcblxyXG4gICAgICAgICAgICBmdW5jdGlvbiB0dXJuT2ZmTm90aWZpY2F0aW9ucygpe1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LnN1Y2Nlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuaW5mby5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQud2FybmluZy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogTGlzdGVuIHRvIGFueSByZXF1ZXN0LCB3ZSBjYW4gcmVnaXN0ZXIgbGlzdGVuZXIgaWYgd2Ugd2FudCB0byBhZGQgZXh0cmEgY29kZS5cclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmxpc3RlblJlcXVlc3QoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKGNvbmZpZyl7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXF1ZXN0IHRvOiAnLCAgY29uZmlnKTtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKHRpbWUpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuRXJyb3IoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlamVjdGlvbil7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXF1ZXN0IGVycm9yOiAnLCAgcmVqZWN0aW9uKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmxpc3RlblJlc3BvbnNlKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZXNwb25zZSl7XHJcbiAgICAgICAgICAgICAgICB2YXIgdGltZSA9IHJlc3BvbnNlLmNvbmZpZy5yZXNwb25zZVRpbWVzdGFtcCAtIHJlc3BvbnNlLmNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wO1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnVGhlIHJlcXVlc3QgdG9vayAnICsgKHRpbWUgLyAxMDAwKSArICcgc2Vjb25kcycpO1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVzcG9uc2UgcmVzdWx0OiAnLCByZXNwb25zZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmxpc3RlbkVycm9yKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZWplY3Rpb24pe1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVzcG9uc2UgZXJyb3I6ICcsIHJlamVjdGlvbik7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc3RhdHVzID0gcmVqZWN0aW9uLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc3RhdHVzVGV4dCA9IHJlamVjdGlvbi5zdGF0dXNUZXh0O1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5lcnJvcnMgPSByZWplY3Rpb24uZGF0YS5lcnJvcnM7XHJcbiAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMzAwMCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEhpZGUgdGhlIFBvcCB1cCBub3RpZmljYXRpb24gbWFudWFsbHlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRzY29wZS5vbkNhbmNlbFBvcFVwID0gZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgICAgICAgICB0dXJuT2ZmTm90aWZpY2F0aW9ucygpO1xyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkcm9vdFNjb3BlLiRvbignYnJvYWRjYXN0LW1zZycsIGZ1bmN0aW9uKGV2ZW50LCBhcmdzKSB7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdicm9hZGNhc3QtbXNnIGV4ZWN1dGVkJyk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnN0YXR1c1RleHQgPSBhcmdzLnRleHQ7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zdGF0dXMgPSBudWxsO1xyXG4gICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS50aW1lKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS4kYXBwbHkoKTsgLy8gcm9vdFNjb3BlIGFuZCB3YXRjaCBleGNsdWRlIHRoZSBhcHBseSBhbmQgbmVlZHMgdGhlIG5leHQgY3ljbGUgdG8gcnVuXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUuJHdhdGNoKCdtc2cnLCBmdW5jdGlvbihuZXdWYWx1ZSwgb2xkVmFsdWUpIHtcclxuICAgICAgICAgICAgICAgIGlmIChuZXdWYWx1ZSAmJiBuZXdWYWx1ZSAhPT0gJycpIHtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzVGV4dCA9IG5ld1ZhbHVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzID0gJHNjb3BlLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMjUwMCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICB9XVxyXG4gICAgfTtcclxufV0pO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzE3LzIwMTUuXHJcbiAqL1xyXG5cclxuLy8gTWFpbiBBbmd1bGFySnMgY29uZmlndXJhdGlvblxyXG5yZXF1aXJlKCcuL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcbi8vIEhlbHBlcnNcclxucmVxdWlyZSgnLi9jb25maWcvQW5ndWxhclByb3ZpZGVySGVscGVyLmpzJyk7XHJcblxyXG4vLyBEaXJlY3RpdmVzXHJcbnJlcXVpcmUoJy4vZGlyZWN0aXZlcy9pbmRleCcpO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRGlhbG9nQWN0aW9uIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy50aXRsZSA9IHBhcmFtcy50aXRsZTtcclxuICAgICAgICB0aGlzLm1lc3NhZ2UgPSBwYXJhbXMubWVzc2FnZTtcclxuXHJcbiAgICB9XHJcbiAgICAvKipcclxuICAgICAqIEFjY2NlcHQgYW5kIENvbmZpcm1cclxuICAgICAqL1xyXG4gICAgY29uZmlybUFjdGlvbigpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yLzIwMTUuXHJcbiAqIEhlYWRlciBDb250cm9sbGVyIG1hbmFnZSB0aGUgdmlldyBhdmFpbGFibGUgb24gdGhlIHN0YXRlLmRhdGFcclxuICogLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gKiBIZWFkZXIgQ29udHJvbGxlclxyXG4gKiBQYWdlIHRpdGxlICAgICAgICAgICAgICAgICAgICAgIEhvbWUgLT4gTGF5b3V0IC0gU3ViIExheW91dFxyXG4gKlxyXG4gKiBNb2R1bGUgQ29udHJvbGxlclxyXG4gKiBDb250ZW50XHJcbiAqIC0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIZWFkZXJDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2dcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG5cclxuICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICBpbnN0cnVjdGlvbjogJycsXHJcbiAgICAgICAgICAgIG1lbnU6IFtdXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hlYWRlciBDb250cm9sbGVyIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmVyaWZ5IGlmIHdlIGhhdmUgYSBtZW51IHRvIHNob3cgdG8gbWFkZSBpdCBhdmFpbGFibGUgdG8gdGhlIFZpZXdcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUhlYWRlcigpIHtcclxuICAgICAgICBpZiAodGhpcy5zdGF0ZSAmJiB0aGlzLnN0YXRlLiRjdXJyZW50ICYmIHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YSkge1xyXG4gICAgICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YS5wYWdlO1xyXG4gICAgICAgICAgICBkb2N1bWVudC50aXRsZSA9IHRoaXMucGFnZU1ldGFEYXRhLnRpdGxlO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIxLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgSGVhZGVyQ29udHJvbGxlciBmcm9tICcuL0hlYWRlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgRGlhbG9nQWN0aW9uIGZyb20gJy4uL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uanMnO1xyXG5cclxudmFyIEhlYWRlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5IZWFkZXJNb2R1bGUnLCBbXSk7XHJcblxyXG5IZWFkZXJNb2R1bGUuY29udHJvbGxlcignSGVhZGVyQ29udHJvbGxlcicsIFsnJGxvZycsICckc3RhdGUnLCBIZWFkZXJDb250cm9sbGVyXSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbkhlYWRlck1vZHVsZS5jb250cm9sbGVyKCdEaWFsb2dBY3Rpb24nLCBbJyRsb2cnLCckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRGlhbG9nQWN0aW9uXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIZWFkZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTGljZW5zZUFkbWluTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZUFkbWluTGlzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlQWRtaW5TZXJ2aWNlIGZyb20gJy4vc2VydmljZS9MaWNlbnNlQWRtaW5TZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RMaWNlbnNlIGZyb20gJy4vcmVxdWVzdC9SZXF1ZXN0TGljZW5zZS5qcyc7XHJcbmltcG9ydCBDcmVhdGVkTGljZW5zZSBmcm9tICcuL2NyZWF0ZWQvQ3JlYXRlZExpY2Vuc2UuanMnO1xyXG5pbXBvcnQgQXBwbHlMaWNlbnNlS2V5IGZyb20gJy4vYXBwbHlMaWNlbnNlS2V5L0FwcGx5TGljZW5zZUtleS5qcyc7XHJcbmltcG9ydCBNYW51YWxseVJlcXVlc3QgZnJvbSAnLi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZURldGFpbC5qcyc7XHJcblxyXG5cclxudmFyIExpY2Vuc2VBZG1pbk1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlQWRtaW5Nb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsICckbG9jYXRpb25Qcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIsICRsb2NhdGlvblByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdsaWNlbnNlQWRtaW4nKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VBZG1pbkxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdBZG1pbmlzdGVyIExpY2Vuc2VzJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FkbWluJywgJ0xpY2Vuc2UnLCAnTGlzdCddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy9saWNlbnNlL2FkbWluL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9saXN0L0xpY2Vuc2VBZG1pbkxpc3QuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VBZG1pbkxpc3QgYXMgbGljZW5zZUFkbWluTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuc2VydmljZSgnTGljZW5zZUFkbWluU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCAnJHJvb3RTY29wZScsIExpY2Vuc2VBZG1pblNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlQWRtaW5MaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsIExpY2Vuc2VBZG1pbkxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ1JlcXVlc3RMaWNlbnNlJywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RMaWNlbnNlXSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdDcmVhdGVkTGljZW5zZScsIFsnJGxvZycsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBDcmVhdGVkTGljZW5zZV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignQXBwbHlMaWNlbnNlS2V5JywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBBcHBseUxpY2Vuc2VLZXldKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ01hbnVhbGx5UmVxdWVzdCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBNYW51YWxseVJlcXVlc3RdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VEZXRhaWwnLCBbJyRsb2cnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTGljZW5zZURldGFpbF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VBZG1pbk1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEFwcGx5TGljZW5zZUtleSBleHRlbmRzIEZvcm1WYWxpZGF0b3J7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpXHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAga2V5OiBwYXJhbXMubGljZW5zZS5rZXlcclxuICAgICAgICB9XHJcbiAgICAgICAgO1xyXG4gICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIGFwcGx5S2V5KCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5hcHBseUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0sIChkYXRhKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQ3JlYXRlZFJlcXVlc3RMaWNlbnNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5jbGllbnQgPSBwYXJhbXM7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlRGV0YWlsIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIG1ldGhvZElkOiBwYXJhbXMubGljZW5zZS5tZXRob2QuaWQsXHJcbiAgICAgICAgICAgIHByb2plY3ROYW1lOiBwYXJhbXMubGljZW5zZS5wcm9qZWN0Lm5hbWUsXHJcbiAgICAgICAgICAgIGNsaWVudE5hbWU6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5uYW1lLFxyXG4gICAgICAgICAgICBlbWFpbDogcGFyYW1zLmxpY2Vuc2UuZW1haWwsXHJcbiAgICAgICAgICAgIHNlcnZlcnNUb2tlbnM6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5tYXgsXHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiBwYXJhbXMubGljZW5zZS5lbnZpcm9ubWVudCxcclxuICAgICAgICAgICAgaW5jZXB0aW9uOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0RGF0ZSxcclxuICAgICAgICAgICAgZXhwaXJhdGlvbjogcGFyYW1zLmxpY2Vuc2UuZXhwaXJhdGlvbkRhdGUsXHJcbiAgICAgICAgICAgIHJlcXVlc3ROb3RlOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0Tm90ZSxcclxuICAgICAgICAgICAgYWN0aXZlOiBwYXJhbXMubGljZW5zZS5zdGF0dXMuaWQgPT09IDEsXHJcbiAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgcmVwbGFjZWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkLFxyXG4gICAgICAgICAgICBlbmNyeXB0ZWREZXRhaWw6IHBhcmFtcy5saWNlbnNlLmVuY3J5cHRlZERldGFpbCxcclxuICAgICAgICAgICAgYXBwbGllZDogZmFsc2VcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVNZXRob2RPcHRpb25zKCk7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdTZXJ2ZXJzJ1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbnMnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAzLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ0N1c3RvbSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRoZSB1c2VyIGFwcGx5IGFuZCBzZXJ2ZXIgc2hvdWxkIHZhbGlkYXRlIHRoZSBrZXkgaXMgY29ycmVjdFxyXG4gICAgICovXHJcbiAgICBhcHBseUxpY2Vuc2VLZXkoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2FwcGx5TGljZW5zZUtleS9BcHBseUxpY2Vuc2VLZXkuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdBcHBseUxpY2Vuc2VLZXkgYXMgYXBwbHlMaWNlbnNlS2V5JyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgbGljZW5zZTogdGhpcy5saWNlbnNlTW9kZWwgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQgPSBkYXRhLnN1Y2Nlc3M7XHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuYWN0aXZlID0gZGF0YS5zdWNjZXNzO1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKHsgaWQ6IHRoaXMubGljZW5zZU1vZGVsLmlkLCB1cGRhdGVkOiB0cnVlfSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE9wZW5zIGEgZGlhbG9nIGFuZCBhbGxvdyB0aGUgdXNlciB0byBtYW51YWxseSBzZW5kIHRoZSByZXF1ZXN0IG9yIGNvcHkgdGhlIGVuY3JpcHRlZCBjb2RlXHJcbiAgICAgKi9cclxuICAgIG1hbnVhbGx5UmVxdWVzdCgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vbWFudWFsbHlSZXF1ZXN0L01hbnVhbGx5UmVxdWVzdC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ01hbnVhbGx5UmVxdWVzdCBhcyBtYW51YWxseVJlcXVlc3QnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiB0aGlzLmxpY2Vuc2VNb2RlbCB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge30pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcbiAgICAgKi9cclxuICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3QoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLnJlc3VibWl0TGljZW5zZVJlcXVlc3QodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byBkZWxldGUgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5kZWxldGVMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VNb2RlbC5hcHBsaWVkKSB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSgpO1xyXG4gICAgICAgIH1cclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZUFkbWluTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlQWRtaW5MaXN0IEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3Qub25SZXF1ZXN0TmV3TGljZW5zZSgpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IFJlcXVlc3QgTmV3IExpY2Vuc2U8L2J1dHRvbj4gPGRpdiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3QucmVsb2FkTGljZW5zZUFkbWluTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1LFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDIwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2xpY2Vuc2VJZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgZmlsdGVyYWJsZTogZmFsc2UsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3Qub25MaWNlbnNlRGV0YWlscyh0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2xpZW50Lm5hbWUnLCB0aXRsZTogJ0NsaWVudCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAncHJvamVjdC5uYW1lJywgdGl0bGU6ICdQcm9qZWN0JywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLnByb2plY3QgJiYgZGF0YS5wcm9qZWN0Lm5hbWUpPyBkYXRhLnByb2plY3QubmFtZS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLnN0YXR1cyk/IGRhdGEuc3RhdHVzLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnLCAgdGVtcGxhdGU6ICcjaWYoZGF0YS50eXBlICYmIGRhdGEudHlwZS5uYW1lID09PSBcIk1VTFRJX1BST0pFQ1RcIil7IyBHbG9iYWwgIyB9IGVsc2UgeyMgU2luZ2xlICN9Iyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCcsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5tZXRob2QgJiYgZGF0YS5tZXRob2QubmFtZSk/IGRhdGEubWV0aG9kLm5hbWUudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5pZCcsIGhpZGRlbjogdHJ1ZX0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubWF4JywgdGl0bGU6ICdTZXJ2ZXIvVG9rZW5zJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdyZXF1ZXN0RGF0ZScsIHRpdGxlOiAnSW5jZXB0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZXhwaXJhdGlvbkRhdGUnLCB0aXRsZTogJ0V4cGlyYXRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbnZpcm9ubWVudCcsIHRpdGxlOiAnRW52aXJvbm1lbnQnLCB0ZW1wbGF0ZTogJzxzcGFuIHN0eWxlPVwidGV4dC10cmFuc2Zvcm06IGNhcGl0YWxpemU7XCI+Iz0oKGRhdGEuZW52aXJvbm1lbnQpPyBkYXRhLmVudmlyb25tZW50LnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAncHJvamVjdC5uYW1lJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdExpY2Vuc2VJZCAhPT0gMCAmJiB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdmFyIGxhc3RMaWNlbnNlID0gdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhLmZpbmQoKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBsaWNlbnNlLmlkID09PSB0aGlzLm9wZW5MYXN0TGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobGFzdExpY2Vuc2UpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlRGV0YWlscyhsYXN0TGljZW5zZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0TGljZW5zZSBhcyByZXF1ZXN0TGljZW5zZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdOZXcgTGljZW5zZSBDcmVhdGVkOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICAgICAgdGhpcy5vbk5ld0xpY2Vuc2VDcmVhdGVkKGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZURldGFpbHMobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09wZW4gRGV0YWlscyBmb3I6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9kZXRhaWwvTGljZW5zZURldGFpbC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VEZXRhaWwgYXMgbGljZW5zZURldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcbiAgICAgICAgICAgIGlmKGRhdGEudXBkYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IGRhdGEuaWQ7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25OZXdMaWNlbnNlQ3JlYXRlZChsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmh0bWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQ3JlYXRlZExpY2Vuc2UgYXMgY3JlYXRlZExpY2Vuc2UnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBlbWFpbDogbGljZW5zZS5lbWFpbCAgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBNYW51YWxseVJlcXVlc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnNjb3BlID0gJHNjb3BlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGlkOiAgcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIGVtYWlsOiBwYXJhbXMubGljZW5zZS5lbWFpbCxcclxuICAgICAgICAgICAgZW5jcnlwdGVkRGV0YWlsOiAnJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIC8vIEdldCB0aGUgaGFzaCBjb2RlIHVzaW5nIHRoZSBpZC5cclxuICAgICAgICB0aGlzLmdldEhhc2hDb2RlKCk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIGdldEhhc2hDb2RlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRIYXNoQ29kZSh0aGlzLmxpY2Vuc2VNb2RlbC5pZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuZW5jcnlwdGVkRGV0YWlsID0gZGF0YTtcclxuICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKiBDcmVhdGUgYSBuZXcgUmVxdWVzdCB0byBnZXQgYSBMaWNlbnNlXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RMaWNlbnNlIGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICAvKipcclxuICAgICAqIEluaXRpYWxpemUgYWxsIHRoZSBwcm9wZXJ0aWVzXHJcbiAgICAgKiBAcGFyYW0gJGxvZ1xyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VBZG1pblNlcnZpY2VcclxuICAgICAqIEBwYXJhbSAkdWliTW9kYWxJbnN0YW5jZVxyXG4gICAgICovXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgRW52aXJvbm1lbnQgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UgPSBbXTtcclxuICAgICAgICAvLyBEZWZpbmUgdGhlIFByb2plY3QgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSBbXTtcclxuXHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldFByb2plY3REYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIENyZWF0ZSB0aGUgTW9kZWwgZm9yIHRoZSBOZXcgTGljZW5zZVxyXG4gICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBlbWFpbDogJycsXHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiAnJyxcclxuICAgICAgICAgICAgcHJvamVjdElkOiAwLFxyXG4gICAgICAgICAgICBjbGllbnROYW1lOiAnJyxcclxuICAgICAgICAgICAgcmVxdWVzdE5vdGU6ICcnXHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpPT57XHJcbiAgICAgICAgICAgIHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlID0gZGF0YTtcclxuICAgICAgICAgICAgaWYodGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgICAgIHZhciBpbmRleCA9IHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlLmZpbmRJbmRleChmdW5jdGlvbihlbnZpcm9tZW50KXtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4gZW52aXJvbWVudCAgPT09ICdQUk9EVUNUSU9OJztcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgaW5kZXggPSBpbmRleCB8fCAwO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuZW52aXJvbm1lbnQgPSBkYXRhW2luZGV4XTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIFByb2plY3QgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLnByb2plY3RJZCA9IGRhdGFbMF0uaWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZSxcclxuICAgICAgICAgICAgc2VsZWN0OiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE9uIFByb2plY3QgQ2hhbmdlLCBzZWxlY3QgdGhlIENsaWVudCBOYW1lXHJcbiAgICAgICAgICAgICAgICB2YXIgaXRlbSA9IHRoaXMuc2VsZWN0UHJvamVjdC5kYXRhSXRlbShlLml0ZW0pO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuY2xpZW50TmFtZSA9IGl0ZW0uY2xpZW50Lm5hbWU7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBnZW5lcmF0ZSBhIG5ldyBMaWNlbnNlIHJlcXVlc3RcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2VSZXF1ZXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIFJlcXVlc3RlZDogJywgdGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QodGhpcy5uZXdMaWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UodGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZUFkbWluU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcbiAgICAgICAgdGhpcy5yb290U2NvcGUgPSAkcm9vdFNjb3BlO1xyXG4gICAgICAgIHRoaXMuc3RhdHVzU3VjY2VzcyA9ICdzdWNjZXNzJztcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnbGljZW5zZUFkbWluU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgb25TdWNjZXNzKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLnJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ1JlcXVlc3QgTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5Lid9KTtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGVtYWlsUmVxdWVzdChsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5lbWFpbFJlcXVlc3QobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ1JlcXVlc3QgTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5Lid9KTtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogIEFwcGx5IFRoZSBMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIG9uU3VjY2Vzc1xyXG4gICAgICovXHJcbiAgICBhcHBseUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcblxyXG4gICAgICAgIHZhciBoYXNoID0gIHtcclxuICAgICAgICAgICAgaGFzaDogbGljZW5zZS5rZXlcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuYXBwbHlMaWNlbnNlKGxpY2Vuc2UuaWQsIGhhc2gsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkgYXBwbGllZCd9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6ICdMaWNlbnNlIHdhcyBub3QgYXBwbGllZCd9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHsgc3VjY2VzczogZmFsc2V9KTtcclxuICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2Vzcyh7IHN1Y2Nlc3M6IHRydWV9KTtcclxuXHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZGVsZXRlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlckxpc3QgZnJvbSAnLi9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlclNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL0xpY2Vuc2VNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBSZXF1ZXN0SW1wb3J0IGZyb20gJy4vcmVxdWVzdEltcG9ydC9SZXF1ZXN0SW1wb3J0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyRGV0YWlsIGZyb20gJy4vZGV0YWlsL0xpY2Vuc2VNYW5hZ2VyRGV0YWlsLmpzJztcclxuXHJcblxyXG52YXIgTGljZW5zZU1hbmFnZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uTGljZW5zZU1hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ2xpY2Vuc2VNYW5hZ2VyJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdsaWNlbnNlTWFuYWdlckxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdMaWNlbnNpbmcgTWFuYWdlcicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydNYW5hZ2VyJywgJ0xpY2Vuc2UnLCAnTGlzdCddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy9saWNlbnNlL21hbmFnZXIvbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvbGlzdC9MaWNlbnNlTWFuYWdlckxpc3QuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VNYW5hZ2VyTGlzdCBhcyBsaWNlbnNlTWFuYWdlckxpc3QnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuc2VydmljZSgnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsICckcm9vdFNjb3BlJywgTGljZW5zZU1hbmFnZXJTZXJ2aWNlXSk7XHJcblxyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZU1hbmFnZXJMaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTGljZW5zZU1hbmFnZXJMaXN0XSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ1JlcXVlc3RJbXBvcnQnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCBSZXF1ZXN0SW1wb3J0XSk7XHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VNYW5hZ2VyRGV0YWlsJywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIExpY2Vuc2VNYW5hZ2VyRGV0YWlsXSk7XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgTGljZW5zZU1hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlckRldGFpbCBleHRlbmRzIEZvcm1WYWxpZGF0b3J7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0kdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIG93bmVyTmFtZTogcGFyYW1zLmxpY2Vuc2Uub3duZXIubmFtZSxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UucHJvamVjdC5pZCxcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLnByb2plY3QubmFtZSxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY2xpZW50SWQ6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5pZCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcbiAgICAgICAgICAgIHN0YXR1c0lkOiBwYXJhbXMubGljZW5zZS5zdGF0dXMuaWQsXHJcbiAgICAgICAgICAgIG1ldGhvZDoge1xyXG4gICAgICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5pZCxcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5uYW1lLFxyXG4gICAgICAgICAgICAgICAgbWF4OiBwYXJhbXMubGljZW5zZS5tZXRob2QubWF4LFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudDogeyBpZDogcGFyYW1zLmxpY2Vuc2UuZW52aXJvbm1lbnQuaWQgfSxcclxuICAgICAgICAgICAgcmVxdWVzdERhdGU6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3REYXRlLFxyXG4gICAgICAgICAgICBpbml0RGF0ZTogKHBhcmFtcy5saWNlbnNlLmFjdGl2YXRpb25EYXRlICE9PSBudWxsKT8gYW5ndWxhci5jb3B5KHBhcmFtcy5saWNlbnNlLmFjdGl2YXRpb25EYXRlKSA6ICcnLFxyXG4gICAgICAgICAgICBlbmREYXRlOiAocGFyYW1zLmxpY2Vuc2UuZXhwaXJhdGlvbkRhdGUgIT09IG51bGwpPyBhbmd1bGFyLmNvcHkocGFyYW1zLmxpY2Vuc2UuZXhwaXJhdGlvbkRhdGUpIDogJycsXHJcbiAgICAgICAgICAgIHNwZWNpYWxJbnN0cnVjdGlvbnM6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3ROb3RlLFxyXG4gICAgICAgICAgICB3ZWJzaXRlTmFtZTogcGFyYW1zLmxpY2Vuc2Uud2Vic2l0ZW5hbWUsXHJcblxyXG4gICAgICAgICAgICBiYW5uZXJNZXNzYWdlOiBwYXJhbXMubGljZW5zZS5iYW5uZXJNZXNzYWdlLFxyXG4gICAgICAgICAgICByZXF1ZXN0ZWRJZDogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdGVkSWQsXHJcbiAgICAgICAgICAgIHJlcGxhY2VkOiBwYXJhbXMubGljZW5zZS5yZXBsYWNlZCxcclxuICAgICAgICAgICAgcmVwbGFjZWRJZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWRJZCxcclxuICAgICAgICAgICAgaG9zdE5hbWU6IHBhcmFtcy5saWNlbnNlLmhvc3ROYW1lLFxyXG4gICAgICAgICAgICBoYXNoOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgZ3JhY2VQZXJpb2REYXlzOiBwYXJhbXMubGljZW5zZS5ncmFjZVBlcmlvZERheXMsXHJcblxyXG4gICAgICAgICAgICBhcHBsaWVkOiBwYXJhbXMubGljZW5zZS5hcHBsaWVkLFxyXG4gICAgICAgICAgICBrZXlJZDogcGFyYW1zLmxpY2Vuc2Uua2V5SWRcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmxpY2Vuc2VLZXkgPSAnTGljZW5zZXMgaGFzIG5vdCBiZWVuIGlzc3VlZCc7XHJcblxyXG4gICAgICAgIC8vIERlZmluZWQgdGhlIEVudmlyb25tZW50IFNlbGVjdFxyXG4gICAgICAgIHRoaXMuc2VsZWN0RW52aXJvbm1lbnQgPSB7fTtcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50TGlzdE9wdGlvbnMgPSBbXTtcclxuICAgICAgICB0aGlzLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpO1xyXG5cclxuICAgICAgICAvLyBEZWZpbmVkIHRoZSBTdGF0dXMgU2VsZWN0IExpc3RcclxuICAgICAgICB0aGlzLnNlbGVjdFN0YXR1cyA9IFtdO1xyXG4gICAgICAgIHRoaXMuZ2V0U3RhdHVzRGF0YVNvdXJjZSgpO1xyXG5cclxuICAgICAgICAvLyBJbml0IHRoZSB0d28gS2VuZG8gRGF0ZXMgZm9yIEluaXQgYW5kIEVuZERhdGVcclxuICAgICAgICB0aGlzLmluaXREYXRlID0ge307XHJcbiAgICAgICAgdGhpcy5pbml0RGF0ZU9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGZvcm1hdDogJ3l5eXkvTU0vZGQnLFxyXG4gICAgICAgICAgICBvcGVuOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgY2hhbmdlOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuZW5kRGF0ZSA9IHt9O1xyXG4gICAgICAgIHRoaXMuZW5kRGF0ZU9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGZvcm1hdDogJ3l5eXkvTU0vZGQnLFxyXG4gICAgICAgICAgICBvcGVuOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pLFxyXG4gICAgICAgICAgICBjaGFuZ2U6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUVuZERhdGUoKTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG5cclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlTWV0aG9kT3B0aW9ucygpO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUxpY2Vuc2VLZXkoKTtcclxuICAgICAgICB0aGlzLnByZXBhcmVBY3Rpdml0eUxpc3QoKTtcclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuXHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDb250cm9scyB3aGF0IGJ1dHRvbnMgdG8gc2hvd1xyXG4gICAgICovXHJcbiAgICBwcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5wZW5kaW5nTGljZW5zZSA9IHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSA0ICYmICF0aGlzLmVkaXRNb2RlO1xyXG4gICAgICAgIHRoaXMuZXhwaXJlZE9yVGVybWluYXRlZCA9ICh0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMiB8fCB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMyk7XHJcbiAgICAgICAgdGhpcy5hY3RpdmVTaG93TW9kZSA9IHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSAxICYmICF0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgJiYgIXRoaXMuZWRpdE1vZGU7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdTZXJ2ZXJzJyxcclxuICAgICAgICAgICAgICAgIG1heDogMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbnMnLFxyXG4gICAgICAgICAgICAgICAgbWF4OiAwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAzLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ0N1c3RvbSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF1cclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlTGljZW5zZUtleSgpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRLZXlDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUtleSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVBY3Rpdml0eUxpc3QoKSB7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1LFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDIwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2RhdGVDcmVhdGVkJywgdGl0bGU6ICdEYXRlJywgd2lkdGg6MTgwLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eSBoOm1tOnNzIHR0fSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXV0aG9yLnBlcnNvbk5hbWUnLCB0aXRsZTogJ1dob20nLCAgd2lkdGg6MTgwfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NoYW5nZXMnLCB0aXRsZTogJ0FjdGlvbicsIHRlbXBsYXRlOiAnPHVsPiNmb3IodmFyIGkgPSAwOyBpIDwgZGF0YS5jaGFuZ2VzLmxlbmd0aDsgaSsrKXsjPGxpPiM9ZGF0YS5jaGFuZ2VzW2ldLmZpZWxkIyA8YnIgLz4gPHNwYW4gY2xhc3M9XCJhY3Rpdml0eS1saXN0LW9sZC12YWxcIiBzdHlsZT1cImNvbG9yOmRhcmtyZWQ7IGZvbnQtd2VpZ2h0OiBib2xkO1wiPiM9ZGF0YS5jaGFuZ2VzW2ldLm9sZFZhbHVlIzwvc3Bhbj4gfCA8c3BhbiBjbGFzcz1cImFjdGl2aXR5LWxpc3QtbmV3LXZhbFwiIHN0eWxlPVwiY29sb3I6IGdyZWVuOyBmb250LXdlaWdodDogYm9sZDtcIj4jPWRhdGEuY2hhbmdlc1tpXS5uZXdWYWx1ZSM8L3NwYW4+PC9saT4jfSM8L3VsPiAnfSxcclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0QWN0aXZpdHlMb2codGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHNvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICBmaWVsZDogJ2RhdGVDcmVhdGVkJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNjcm9sbGFibGU6IHRydWVcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcbiAgICAgKi9cclxuICAgIGFjdGl2YXRlTGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5hY3RpdmF0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIGlmIChkYXRhKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9IDE7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmdldFN0YXR1c0RhdGFTb3VyY2UoKTtcclxuICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuICAgICAgICAgICAgICAgIHRoaXMucHJlcGFyZUxpY2Vuc2VLZXkoKTtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSB0cnVlO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byByZXZva2UgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnJldm9rZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmFsaWRhdGUgdGhlIGlucHV0IG9uIFNlcnZlciBvciBUb2tlbnMgaXMgb25seSBpbnRlZ2VyIG9ubHlcclxuICAgICAqIFRoaXMgd2lsbCBiZSBjb252ZXJ0ZWQgaW4gYSBtb3JlIGNvbXBsZXggZGlyZWN0aXZlIGxhdGVyXHJcbiAgICAgKiBUT0RPOiBDb252ZXJ0IGludG8gYSBkaXJlY3RpdmVcclxuICAgICAqL1xyXG4gICAgdmFsaWRhdGVJbnRlZ2VyT25seShlLG1vZGVsKXtcclxuICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICB2YXIgbmV3VmFsPSBwYXJzZUludChtb2RlbCk7XHJcbiAgICAgICAgICAgIGlmKCFpc05hTihuZXdWYWwpKSB7XHJcbiAgICAgICAgICAgICAgICBtb2RlbCA9IG5ld1ZhbDtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIG1vZGVsID0gMDtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICBpZihlICYmIGUuY3VycmVudFRhcmdldCkge1xyXG4gICAgICAgICAgICAgICAgZS5jdXJyZW50VGFyZ2V0LnZhbHVlID0gbW9kZWw7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9IGNhdGNoKGUpIHtcclxuICAgICAgICAgICAgdGhpcy4kbG9nLndhcm4oJ0ludmFsaWQgTnVtYmVyIEV4cGNlcHRpb24nLCBtb2RlbCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZSBjdXJyZW50IGNoYW5nZXNcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5zYXZlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdMaWNlbnNlIFNhdmVkJyk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENoYW5nZSB0aGUgc3RhdHVzIHRvIEVkaXRcclxuICAgICAqL1xyXG4gICAgbW9kaWZ5TGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gdHJ1ZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50TGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgaWYoIXRoaXMubGljZW5zZU1vZGVsLmVudmlyb25tZW50SWQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudElkID0gZGF0YVswXS5pZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0U3RhdHVzRGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnN0YXR1c1RleHQgPVxyXG4gICAgICAgICAgICAgKHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSAxKT8gJ0FjdGl2ZScgOlxyXG4gICAgICAgICAgICAgICAgICgodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDIpPyAnRXhwaXJlZCc6XHJcbiAgICAgICAgICAgICAgICAgICAgICgodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDMpPyAnVGVybWluYXRlZCc6XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAoKHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSA0KT8gJ1BlbmRpbmcnOlxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgKCh0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gNSk/ICdDb3JydXB0JyA6ICcnKSkpKTtcclxuXHJcbiAgICAgICAgLyp0aGlzLnNlbGVjdFN0YXR1c0xpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiBbXHJcbiAgICAgICAgICAgICAgICB7aWQ6IDEsIG5hbWU6ICdBY3RpdmUnfSxcclxuICAgICAgICAgICAgICAgIHtpZDogMiwgbmFtZTogJ0V4cGlyZWQnfSxcclxuICAgICAgICAgICAgICAgIHtpZDogMywgbmFtZTogJ1Rlcm1pbmF0ZWQnfSxcclxuICAgICAgICAgICAgICAgIHtpZDogNCwgbmFtZTogJ1BlbmRpbmcnfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZVxyXG4gICAgICAgIH0qL1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQSBuZXcgUHJvamVjdCBoYXMgYmVlbiBzZWxlY3RlZCwgdGhhdCBtZWFucyB3ZSBuZWVkIHRvIHJlbG9hZCB0aGUgbmV4dCBwcm9qZWN0IHNlY3Rpb25cclxuICAgICAqIEBwYXJhbSBpdGVtXHJcbiAgICAgKi9cclxuICAgIG9uQ2hhbmdlUHJvamVjdChpdGVtKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnT24gY2hhbmdlIFByb2plY3QnLCBpdGVtKTtcclxuICAgIH1cclxuXHJcbiAgICBvbkNoYW5nZUluaXREYXRlKCkge1xyXG4gICAgICAgIHZhciBzdGFydERhdGUgPSB0aGlzLmluaXREYXRlLnZhbHVlKCksXHJcbiAgICAgICAgICAgIGVuZERhdGUgPSB0aGlzLmVuZERhdGUudmFsdWUoKTtcclxuXHJcbiAgICAgICAgaWYgKHN0YXJ0RGF0ZSkge1xyXG4gICAgICAgICAgICBzdGFydERhdGUgPSBuZXcgRGF0ZShzdGFydERhdGUpO1xyXG4gICAgICAgICAgICBzdGFydERhdGUuc2V0RGF0ZShzdGFydERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihzdGFydERhdGUpO1xyXG5cclxuICAgICAgICAgICAgaWYoZW5kRGF0ZSkge1xyXG4gICAgICAgICAgICAgICAgaWYodGhpcy5pbml0RGF0ZS52YWx1ZSgpID4gdGhpcy5lbmREYXRlLnZhbHVlKCkpIHtcclxuICAgICAgICAgICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgZW5kRGF0ZS5zZXREYXRlKHN0YXJ0RGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmVuZERhdGUgPSBlbmREYXRlO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIG9uQ2hhbmdlRW5kRGF0ZSgpe1xyXG4gICAgICAgIHZhciBlbmREYXRlID0gdGhpcy5lbmREYXRlLnZhbHVlKCksXHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZSA9IHRoaXMuaW5pdERhdGUudmFsdWUoKTtcclxuXHJcbiAgICAgICAgaWYgKGVuZERhdGUpIHtcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKGVuZERhdGUpO1xyXG4gICAgICAgICAgICBlbmREYXRlLnNldERhdGUoZW5kRGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgIH0gZWxzZSBpZiAoc3RhcnREYXRlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4obmV3IERhdGUoc3RhcnREYXRlKSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKCk7XHJcbiAgICAgICAgICAgIHRoaXMuaW5pdERhdGUubWF4KGVuZERhdGUpO1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKGVuZERhdGUpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICBpZih0aGlzLmVkaXRNb2RlKSB7XHJcbiAgICAgICAgICAgIHRoaXMucmVzZXRGb3JtKCgpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vblJlc2V0Rm9ybSgpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2UgaWYodGhpcy5yZWxvYWRSZXF1aXJlZCl7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSh7fSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERlcGVuZGluZyB0aGUgbnVtYmVyIG9mIGZpZWxkcyBhbmQgdHlwZSBvZiBmaWVsZCwgdGhlIHJlc2V0IGNhbid0IGJlIG9uIHRoZSBGb3JtVmFsaWRvciwgYXQgbGVhc3Qgbm90IG5vd1xyXG4gICAgICovXHJcbiAgICBvblJlc2V0Rm9ybSgpIHtcclxuICAgICAgICB0aGlzLnJlc2V0RHJvcERvd24odGhpcy5zZWxlY3RFbnZpcm9ubWVudCwgdGhpcy5saWNlbnNlTW9kZWwuZW52aXJvbm1lbnQuaWQpO1xyXG4gICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIC8vdGhpcy5nZXRMaWNlbnNlTGlzdCgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTWFuYWdlckxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gMDtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0Lm9uUmVxdWVzdEltcG9ydExpY2Vuc2UoKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBJbXBvcnQgTGljZW5zZSBSZXF1ZXN0PC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3QucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMjBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIGZpbHRlcmFibGU6IGZhbHNlLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3Qub25MaWNlbnNlTWFuYWdlckRldGFpbHModGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ293bmVyLm5hbWUnLCB0aXRsZTogJ093bmVyJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjbGllbnQubmFtZScsIHRpdGxlOiAnQ2xpZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLCB0aXRsZTogJ1Byb2plY3QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2VtYWlsJywgdGl0bGU6ICdDb250YWN0IEVtYWlsJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMubmFtZScsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnLCAgdGVtcGxhdGU6ICcjaWYoZGF0YS50eXBlICYmIGRhdGEudHlwZS5uYW1lID09PSBcIk1VTFRJX1BST0pFQ1RcIil7IyBHbG9iYWwgIyB9IGVsc2UgeyMgU2luZ2xlICN9Iyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5tYXgnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2YXRpb25EYXRlJywgdGl0bGU6ICdJbmNlcHRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uRGF0ZScsIHRpdGxlOiAnRXhwaXJhdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Vudmlyb25tZW50Lm5hbWUnLCB0aXRsZTogJ0Vudmlyb25tZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6J2dyYWNlUGVyaW9kRGF5cycsaGlkZGVuOiB0cnVlfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAncHJvamVjdC5uYW1lJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkICE9PSAwICYmIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB2YXIgbmV3TGljZW5zZUNyZWF0ZWQgPSB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEuZmluZCgobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGxpY2Vuc2UuaWQgPT09IHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobmV3TGljZW5zZUNyZWF0ZWQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlTWFuYWdlckRldGFpbHMobmV3TGljZW5zZUNyZWF0ZWQpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgZmlsdGVyYWJsZToge1xyXG4gICAgICAgICAgICAgICAgZXh0cmE6IGZhbHNlXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhlIHVzZXIgSW1wb3J0IGEgbmV3IExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgb25SZXF1ZXN0SW1wb3J0TGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0SW1wb3J0IGFzIHJlcXVlc3RJbXBvcnQnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnXHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGxpY2Vuc2VJbXBvcnRlZCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSBsaWNlbnNlSW1wb3J0ZWQuaWQ7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQWZ0ZXIgY2xpY2tpbmcgb24gZWRpdCwgd2UgcmVkaXJlY3QgdGhlIHVzZXIgdG8gdGhlIEVkaXRpb24gc2NyZWVuIGluc3RlYWQgb2Ygb3BlbiBhIGRpYWxvZ1xyXG4gICAgICogZHUgdGhlIHNpemUgb2YgdGhlIGlucHV0c1xyXG4gICAgICovXHJcbiAgICBvbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyhsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnT3BlbiBEZXRhaWxzIGZvcjogJywgbGljZW5zZSk7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvZGV0YWlsL0xpY2Vuc2VNYW5hZ2VyRGV0YWlsLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJEZXRhaWwgYXMgbGljZW5zZU1hbmFnZXJEZXRhaWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbGcnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSB7fTtcclxuICAgICAgICAgICAgICAgICAgICBpZihsaWNlbnNlICYmIGxpY2Vuc2UuZGF0YUl0ZW0pIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGRhdGFJdGVtID0gbGljZW5zZTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgbGljZW5zZTogZGF0YUl0ZW0gfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLnJlYWQoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdEltcG9ydCBleHRlbmRzIEZvcm1WYWxpZGF0b3J7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpO1xyXG5cclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaGFzaDogJydcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgYW5kIHZhbGlkYXRlIHRoZSBLZXkgaXMgY29ycmVjdFxyXG4gICAgICovXHJcbiAgICBvbkltcG9ydExpY2Vuc2UoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuaW1wb3J0TGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGxpY2Vuc2VJbXBvcnRlZCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGxpY2Vuc2VJbXBvcnRlZC5kYXRhKTtcclxuICAgICAgICAgICAgfSwgKGxpY2Vuc2VJbXBvcnRlZCk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UobGljZW5zZUltcG9ydGVkLmRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIsICRyb290U2NvcGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLnJvb3RTY29wZSA9ICRyb290U2NvcGU7XHJcbiAgICAgICAgdGhpcy5zdGF0dXNTdWNjZXNzID0gJ3N1Y2Nlc3MnO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdsaWNlbnNlTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0TGljZW5zZUxpc3Qob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0S2V5Q29kZShsaWNlbnNlSWQsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEtleUNvZGUobGljZW5zZUlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBTYXZlIHRoZSBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG5cclxuICAgICAgICB2YXIgbGljZW5zZU1vZGlmaWVkID0ge1xyXG4gICAgICAgICAgICBlbnZpcm9ubWVudDogeyBpZDogcGFyc2VJbnQobGljZW5zZS5lbnZpcm9ubWVudC5pZCkgfSxcclxuICAgICAgICAgICAgbWV0aG9kOiB7XHJcbiAgICAgICAgICAgICAgICBpZDogcGFyc2VJbnQobGljZW5zZS5tZXRob2QuaWQpXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGFjdGl2YXRpb25EYXRlOiBtb21lbnQobGljZW5zZS5pbml0RGF0ZSkuZm9ybWF0KCdZWVlZLU1NLUREJyksXHJcbiAgICAgICAgICAgIGV4cGlyYXRpb25EYXRlOiBtb21lbnQobGljZW5zZS5lbmREYXRlKS5mb3JtYXQoJ1lZWVktTU0tREQnKSxcclxuICAgICAgICAgICAgc3RhdHVzOiB7IGlkOiBsaWNlbnNlLnN0YXR1c0lkIH0sXHJcbiAgICAgICAgICAgIHByb2plY3Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiAobGljZW5zZS5wcm9qZWN0LmlkICE9PSAnYWxsJyk/IHBhcnNlSW50KGxpY2Vuc2UucHJvamVjdC5pZCkgOiBsaWNlbnNlLnByb2plY3QuaWQsICAvLyBXZSBwYXNzICdhbGwnIHdoZW4gaXMgbXVsdGlwcm9qZWN0XHJcbiAgICAgICAgICAgICAgICBuYW1lOiBsaWNlbnNlLnByb2plY3QubmFtZVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBiYW5uZXJNZXNzYWdlOiBsaWNlbnNlLmJhbm5lck1lc3NhZ2UsXHJcbiAgICAgICAgICAgIGdyYWNlUGVyaW9kRGF5czogbGljZW5zZS5ncmFjZVBlcmlvZERheXMsXHJcbiAgICAgICAgICAgIHdlYnNpdGVuYW1lOiBsaWNlbnNlLndlYnNpdGVOYW1lLFxyXG4gICAgICAgICAgICBob3N0TmFtZTogbGljZW5zZS5ob3N0TmFtZVxyXG4gICAgICAgIH07XHJcbiAgICAgICAgaWYobGljZW5zZS5tZXRob2QgIT09IDMpIHtcclxuICAgICAgICAgICAgbGljZW5zZU1vZGlmaWVkLm1ldGhvZC5tYXggPSBwYXJzZUludChsaWNlbnNlLm1ldGhvZC5tYXgpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuc2F2ZUxpY2Vuc2UobGljZW5zZS5pZCwgbGljZW5zZU1vZGlmaWVkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG4gICAgLyoqXHJcbiAgICAgKiBEb2VzIHRoZSBhY3RpdmF0aW9uIG9mIHRoZSBjdXJyZW50IGxpY2Vuc2UgaWYgdGhpcyBpcyBub3QgYWN0aXZlXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGFjdGl2YXRlTGljZW5zZShsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmFjdGl2YXRlTGljZW5zZShsaWNlbnNlLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICBpZihkYXRhLnN0YXR1cyA9PT0gdGhpcy5zdGF0dXNTdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHtcclxuICAgICAgICAgICAgICAgICAgICB0eXBlOiAnaW5mbycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogJ1RoZSBsaWNlbnNlIHdhcyBhY3RpdmF0ZWQgYW5kIHRoZSBsaWNlbnNlIHdhcyBlbWFpbGVkLidcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ3dhcm5pbmcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6IGRhdGEuZGF0YVxyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTWFrZSB0aGUgcmVxdWVzdCB0byBJbXBvcnQgdGhlIGxpY2Vuc2UsIGlmIGZhaWxzLCB0aHJvd3MgYW4gZXhjZXB0aW9uIHZpc2libGUgZm9yIHRoZSB1c2VyIHRvIHRha2UgYWN0aW9uXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGltcG9ydExpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcbiAgICAgICAgdmFyIGhhc2ggPSB7XHJcbiAgICAgICAgICAgIGRhdGE6IGxpY2Vuc2UuaGFzaFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnJlcXVlc3RJbXBvcnQoaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBJbXBvcnRlZCd9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6ICdMaWNlbnNlIHdhcyBub3QgYXBwbGllZC4gUmV2aWV3IHRoZSBwcm92aWRlZCBMaWNlbnNlIEtleSBpcyBjb3JyZWN0Lid9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHsgc3VjY2VzczogZmFsc2V9KTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJldm9rZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkucmV2b2tlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEFjdGl2aXR5TG9nKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldEFjdGl2aXR5TG9nKGxpY2Vuc2UuaWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBjYWxsYmFjayl7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBOb3RpY2VMaXN0IGZyb20gJy4vbGlzdC9Ob3RpY2VMaXN0LmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9Ob3RpY2VNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBFZGl0Tm90aWNlIGZyb20gJy4vZWRpdC9FZGl0Tm90aWNlLmpzJztcclxuXHJcbnZhciBOb3RpY2VNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLk5vdGljZU1hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ25vdGljZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ25vdGljZUxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdOb3RpY2UgQWRtaW5pc3RyYXRpb24nLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnQWRtaW4nLCAnTm90aWNlJywgJ0xpc3QnXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvbm90aWNlL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvbGlzdC9Ob3RpY2VMaXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdOb3RpY2VMaXN0IGFzIG5vdGljZUxpc3QnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdOb3RpY2VNYW5hZ2VyU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCBOb3RpY2VNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdOb3RpY2VMaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdOb3RpY2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBOb3RpY2VMaXN0XSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignRWRpdE5vdGljZScsIFsnJGxvZycsICdOb3RpY2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRWRpdE5vdGljZV0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgTm90aWNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRWRpdE5vdGljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbm90aWNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy5hY3Rpb24gPSBwYXJhbXMuYWN0aW9uO1xyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHBhcmFtcy5hY3Rpb25UeXBlO1xyXG5cclxuICAgICAgICB0aGlzLmtlbmRvRWRpdG9yVG9vbHMgPSBbXHJcbiAgICAgICAgICAgICdmb3JtYXR0aW5nJywgJ2NsZWFuRm9ybWF0dGluZycsXHJcbiAgICAgICAgICAgICdmb250TmFtZScsICdmb250U2l6ZScsXHJcbiAgICAgICAgICAgICdqdXN0aWZ5TGVmdCcsICdqdXN0aWZ5Q2VudGVyJywgJ2p1c3RpZnlSaWdodCcsICdqdXN0aWZ5RnVsbCcsXHJcbiAgICAgICAgICAgICdib2xkJyxcclxuICAgICAgICAgICAgJ2l0YWxpYycsXHJcbiAgICAgICAgICAgICd2aWV3SHRtbCdcclxuICAgICAgICBdO1xyXG5cclxuICAgICAgICAvLyBDU1MgaGFzIG5vdCBjYW5jZWxpbmcgYXR0cmlidXRlcywgc28gaW5zdGVhZCBvZiByZW1vdmluZyBldmVyeSBwb3NzaWJsZSBIVE1MLCB3ZSBtYWtlIGVkaXRvciBoYXMgc2FtZSBjc3NcclxuICAgICAgICB0aGlzLmtlbmRvU3R5bGVzaGVldHMgPSBbXHJcbiAgICAgICAgICAgICcuLi9zdGF0aWMvZGlzdC9qcy92ZW5kb3JzL2Jvb3RzdHJhcC9kaXN0L2Nzcy9ib290c3RyYXAubWluLmNzcycsIC8vIE91cnQgY3VycmVudCBCb290c3RyYXAgY3NzXHJcbiAgICAgICAgICAgICcuLi9zdGF0aWMvZGlzdC9jc3MvVERTVE1MYXlvdXQubWluLmNzcycgLy8gT3JpZ2luYWwgVGVtcGxhdGUgQ1NTXHJcblxyXG4gICAgICAgIF07XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0VHlwZURhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbCA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICB0eXBlSWQ6IDAsXHJcbiAgICAgICAgICAgIGFjdGl2ZTogZmFsc2UsXHJcbiAgICAgICAgICAgIGh0bWxUZXh0OiAnJyxcclxuICAgICAgICAgICAgcmF3VGV4dDogJydcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIC8vIE9uIEVkaXRpb24gTW9kZSB3ZSBjYyB0aGUgbW9kZWwgYW5kIG9ubHkgdGhlIHBhcmFtcyB3ZSBuZWVkXHJcbiAgICAgICAgaWYocGFyYW1zLm5vdGljZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5pZCA9IHBhcmFtcy5ub3RpY2UuaWQ7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLnRpdGxlID0gcGFyYW1zLm5vdGljZS50aXRsZTtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwudHlwZUlkID0gcGFyYW1zLm5vdGljZS50eXBlLmlkO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5hY3RpdmUgPSBwYXJhbXMubm90aWNlLmFjdGl2ZTtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuaHRtbFRleHQgPSBwYXJhbXMubm90aWNlLmh0bWxUZXh0O1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0VHlwZURhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy50eXBlRGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge3R5cGVJZDogMSwgbmFtZTogJ1ByZWxvZ2luJ30sXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDIsIG5hbWU6ICdQb3N0bG9naW4nfVxyXG4gICAgICAgICAgICAvL3t0eXBlSWQ6IDMsIG5hbWU6ICdHZW5lcmFsJ30gRGlzYWJsZWQgdW50aWwgUGhhc2UgSUlcclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSB0aGUgU2VydmljZSBjYWxsIHRvIENyZWF0ZS9FZGl0IGEgbm90aWNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVOb3RpY2UoKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbyh0aGlzLmFjdGlvbiArICcgTm90aWNlIFJlcXVlc3RlZDogJywgdGhpcy5lZGl0TW9kZWwpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsLnJhd1RleHQgPSAkKCcja2VuZG8tZWRpdG9yLWNyZWF0ZS1lZGl0JykudGV4dCgpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsLnR5cGVJZCA9IHBhcnNlSW50KHRoaXMuZWRpdE1vZGVsLnR5cGVJZCk7XHJcbiAgICAgICAgaWYodGhpcy5hY3Rpb24gPT09IHRoaXMuYWN0aW9uVHlwZS5ORVcpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5jcmVhdGVOb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSBpZih0aGlzLmFjdGlvbiA9PT0gdGhpcy5hY3Rpb25UeXBlLkVESVQpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5lZGl0Tm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTm90aWNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIGRlbGV0ZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5kZWxldGVOb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBOb3RpY2VMaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIG5vdGljZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuXHJcbiAgICAgICAgdGhpcy5hY3Rpb25UeXBlID0ge1xyXG4gICAgICAgICAgICBORVc6ICdOZXcnLFxyXG4gICAgICAgICAgICBFRElUOiAnRWRpdCdcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLm5vdGljZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLm5vdGljZUdyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZSA9IG5vdGljZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cIm5vdGljZUxpc3Qub25FZGl0Q3JlYXRlTm90aWNlKG5vdGljZUxpc3QuYWN0aW9uVHlwZS5ORVcpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IENyZWF0ZSBOZXcgTm90aWNlPC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJub3RpY2VMaXN0LnJlbG9hZE5vdGljZUxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdpZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaHRtbFRleHQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cIm5vdGljZUxpc3Qub25FZGl0Q3JlYXRlTm90aWNlKG5vdGljZUxpc3QuYWN0aW9uVHlwZS5FRElULCB0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGl0bGUnLCB0aXRsZTogJ1RpdGxlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aXZlJywgdGl0bGU6ICdBY3RpdmUnLCB0ZW1wbGF0ZTogJyNpZihhY3RpdmUpIHsjIFllcyAjfSBlbHNlIHsjIE5vICN9IycgfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmdldE5vdGljZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHNvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICBmaWVsZDogJ3RpdGxlJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE9wZW4gYSBkaWFsb2cgd2l0aCB0aGUgQmFzaWMgRm9ybSB0byByZXF1ZXN0IGEgTmV3IE5vdGljZVxyXG4gICAgICovXHJcbiAgICBvbkVkaXRDcmVhdGVOb3RpY2UoYWN0aW9uLCBub3RpY2UpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2VkaXQvRWRpdE5vdGljZS5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0VkaXROb3RpY2UgYXMgZWRpdE5vdGljZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IG5vdGljZSAmJiBub3RpY2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgYWN0aW9uOiBhY3Rpb24sIG5vdGljZTogZGF0YUl0ZW0sIGFjdGlvblR5cGU6IHRoaXMuYWN0aW9uVHlwZX07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobm90aWNlKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oYWN0aW9uICsgJyBOb3RpY2U6ICcsIG5vdGljZSk7XHJcbiAgICAgICAgICAgIC8vIEFmdGVyIGEgbmV3IHZhbHVlIGlzIGFkZGVkLCBsZXRzIHRvIHJlZnJlc2ggdGhlIEdyaWRcclxuICAgICAgICAgICAgdGhpcy5yZWxvYWROb3RpY2VMaXN0KCk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZWxvYWROb3RpY2VMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubm90aWNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlR3JpZC5kYXRhU291cmNlLnJlYWQoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBOb3RpY2VNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcblxyXG4gICAgICAgIHRoaXMuVFlQRSA9IHtcclxuICAgICAgICAgICAgJzEnOiAnUHJlbG9naW4nLFxyXG4gICAgICAgICAgICAnMic6ICdQb3N0bG9naW4nLFxyXG4gICAgICAgICAgICAnMyc6ICdHZW5lcmFsJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdOb3RpY2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXROb3RpY2VMaXN0KGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHZhciBub3RpY2VMaXN0ID0gW107XHJcbiAgICAgICAgICAgIHRyeSB7XHJcbiAgICAgICAgICAgICAgICAvLyBWZXJpZnkgdGhlIExpc3QgcmV0dXJucyB3aGF0IHdlIGV4cGVjdCBhbmQgd2UgY29udmVydCBpdCB0byBhbiBBcnJheSB2YWx1ZVxyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSAmJiBkYXRhLm5vdGljZXMpIHtcclxuICAgICAgICAgICAgICAgICAgICBub3RpY2VMaXN0ID0gZGF0YS5ub3RpY2VzO1xyXG4gICAgICAgICAgICAgICAgICAgIGlmIChub3RpY2VMaXN0ICYmIG5vdGljZUxpc3QubGVuZ3RoID4gMCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IG5vdGljZUxpc3QubGVuZ3RoOyBpID0gaSArIDEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5vdGljZUxpc3RbaV0udHlwZSA9IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogbm90aWNlTGlzdFtpXS50eXBlSWQsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogdGhpcy5UWVBFW25vdGljZUxpc3RbaV0udHlwZUlkXVxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgfTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGRlbGV0ZSBub3RpY2VMaXN0W2ldLnR5cGVJZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSBjYXRjaChlKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxvZy5lcnJvcignRXJyb3IgcGFyc2luZyB0aGUgTm90aWNlIExpc3QnLCBlKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2sobm90aWNlTGlzdCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTm90aWNlIHBhc3NpbmcgcGFyYW1zXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTm90aWNlKG5vdGljZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTm90aWNlIHNob3VsZCBoYXZlIHRoZSBJRCBpbiBvcmRlciB0byBlZGl0IHRoZSBOb3RpY2VcclxuICAgICAqIEBwYXJhbSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBlZGl0Tm90aWNlKG5vdGljZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZWRpdE5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE5vdGljZSBzaG91bGQgaGF2ZSB0aGUgSUQgaW4gb3JkZXIgdG8gZGVsZXRlIHRoZSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBkZWxldGVOb3RpY2Uobm90aWNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZGVsZXRlTm90aWNlKG5vdGljZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgVGFza01hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9UYXNrTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJDb250cm9sbGVyIGZyb20gJy4vbGlzdC9UYXNrTWFuYWdlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJFZGl0IGZyb20gJy4vZWRpdC9UYXNrTWFuYWdlckVkaXQuanMnO1xyXG5cclxudmFyIFRhc2tNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLlRhc2tNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAnZm9ybWx5Q29uZmlnUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCBmb3JtbHlDb25maWdQcm92aWRlcikge1xyXG5cclxuICAgIGZvcm1seUNvbmZpZ1Byb3ZpZGVyLnNldFR5cGUoe1xyXG4gICAgICAgIG5hbWU6ICdjdXN0b20nLFxyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnY3VzdG9tLmh0bWwnXHJcbiAgICB9KTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ3Rhc2tMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTXkgVGFzayBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ1Rhc2sgTWFuYWdlciddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy90YXNrL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2xpc3QvVGFza01hbmFnZXJDb250YWluZXIuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyQ29udHJvbGxlciBhcyB0YXNrTWFuYWdlcidcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCd0YXNrTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgVGFza01hbmFnZXJTZXJ2aWNlXSk7XHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdUYXNrTWFuYWdlckNvbnRyb2xsZXInLCBbJyRsb2cnLCAndGFza01hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsIFRhc2tNYW5hZ2VyQ29udHJvbGxlcl0pO1xyXG5UYXNrTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdUYXNrTWFuYWdlckVkaXQnLCBbJyRsb2cnLCBUYXNrTWFuYWdlckVkaXRdKTtcclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBUYXNrTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMvMTEvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlckVkaXQge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHRhc2tNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcblxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjAvMjAxNS5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyQ29udHJvbGxlciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgdGFza01hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLm1vZHVsZSA9ICdUYXNrTWFuYWdlcic7XHJcbiAgICAgICAgdGhpcy50YXNrTWFuYWdlclNlcnZpY2UgPSB0YXNrTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy50YXNrR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmV2ZW50RGF0YVNvdXJjZSA9IFtdO1xyXG5cclxuICAgICAgICAvLyBJbml0IENsYXNzXHJcbiAgICAgICAgdGhpcy5nZXRFdmVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVGFza01hbmFnZXIgQ29udHJvbGxlciBJbnN0YW5jZWQnKTtcclxuICAgICAgICB0aGlzLmluaXRGb3JtKCk7XHJcblxyXG4gICAgfVxyXG5cclxuICAgIG9wZW5Nb2RhbERlbW8oKSB7XHJcblxyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJ2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2VkaXQvVGFza01hbmFnZXJFZGl0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnVGFza01hbmFnZXJFZGl0JyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgaXRlbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4gWycxJywnYTInLCdnZyddO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKHNlbGVjdGVkSXRlbSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmRlYnVnKHNlbGVjdGVkSXRlbSk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdNb2RhbCBkaXNtaXNzZWQgYXQ6ICcgKyBuZXcgRGF0ZSgpKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMudGFza0dyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBncm91cGFibGU6IHRydWUsXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFt7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGFzaycsIHRpdGxlOiAnVGFzayd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZGVzY3JpcHRpb24nLCB0aXRsZTogJ0Rlc2NyaXB0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldE5hbWUnLCB0aXRsZTogJ0Fzc2V0IE5hbWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Fzc2V0VHlwZScsIHRpdGxlOiAnQXNzZXQgVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndXBkYXRlZCcsIHRpdGxlOiAnVXBkYXRlZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZHVlJywgdGl0bGU6ICdEdWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NpZ25lZFRvJywgdGl0bGU6ICdBc3NpZ25lZCBUbyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGVhbScsIHRpdGxlOiAnVGVhbSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2F0ZWdvcnknLCB0aXRsZTogJ0NhdGVnb3J5J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdWMnLCB0aXRsZTogJ1N1Yy4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Njb3JlJywgdGl0bGU6ICdTY29yZSd9XSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgLyp0aGlzLnRhc2tNYW5hZ2VyU2VydmljZS50ZXN0U2VydmljZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTsqL1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RXZlbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuZXZlbnREYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMSwgZXZlbnROYW1lOiAnQWxsJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiAyLCBldmVudE5hbWU6ICdCdWlsZG91dCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMywgZXZlbnROYW1lOiAnRFItRVAnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDQsIGV2ZW50TmFtZTogJ00xLVBoeXNpY2FsJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIG9uRXJyb3JIYXBwZW5zKCkge1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlLmZhaWxDYWxsKGZ1bmN0aW9uICgpIHtcclxuXHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgaW5pdEZvcm0oKSB7XHJcbiAgICAgICAgdGhpcy51c2VyRmllbGRzID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdlbWFpbCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnaW5wdXQnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ2VtYWlsJyxcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ0VtYWlsIGFkZHJlc3MnLFxyXG4gICAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyOiAnRW50ZXIgZW1haWwnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGtleTogJ3Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdpbnB1dCcsXHJcbiAgICAgICAgICAgICAgICB0ZW1wbGF0ZU9wdGlvbnM6IHtcclxuICAgICAgICAgICAgICAgICAgICB0eXBlOiAncGFzc3dvcmQnLFxyXG4gICAgICAgICAgICAgICAgICAgIGxhYmVsOiAnUGFzc3dvcmQnLFxyXG4gICAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyOiAnUGFzc3dvcmQnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGtleTogJ2ZpbGUnLFxyXG4gICAgICAgICAgICAgICAgdHlwZTogJ2ZpbGUnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdGaWxlIGlucHV0JyxcclxuICAgICAgICAgICAgICAgICAgICBkZXNjcmlwdGlvbjogJ0V4YW1wbGUgYmxvY2stbGV2ZWwgaGVscCB0ZXh0IGhlcmUnLFxyXG4gICAgICAgICAgICAgICAgICAgIHVybDogJ2h0dHBzOi8vZXhhbXBsZS5jb20vdXBsb2FkJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdjaGVja2VkJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdjaGVja2JveCcsXHJcbiAgICAgICAgICAgICAgICB0ZW1wbGF0ZU9wdGlvbnM6IHtcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ0NoZWNrIG1lIG91dCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDIyLzA3LzE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgUmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSBSZXN0U2VydmljZUhhbmRsZXI7XHJcblxyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdUYXNrTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZmFpbENhbGwoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlJlc291cmNlU2VydmljZUhhbmRsZXIoKS5nZXRTVkcoKTtcclxuICAgIH1cclxuXHJcbiAgICB0ZXN0U2VydmljZShjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuVGFza1NlcnZpY2VIYW5kbGVyKCkuZ2V0RmVlZHMoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzMvMjAxNi5cclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnXHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBGb3JtVmFsaWRhdG9yIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuXHJcbiAgICAgICAgLy8gSlMgZG9lcyBhIGFyZ3VtZW50IHBhc3MgYnkgcmVmZXJlbmNlXHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gbnVsbDtcclxuICAgICAgICAvLyBBIGNvcHkgd2l0aG91dCByZWZlcmVuY2UgZnJvbSB0aGUgb3JpZ2luYWwgb2JqZWN0XHJcbiAgICAgICAgdGhpcy5vcmlnaW5hbERhdGEgPSBudWxsO1xyXG4gICAgICAgIC8vIEEgQ0MgYXMgSlNPTiBmb3IgY29tcGFyaXNvbiBQdXJwb3NlXHJcbiAgICAgICAgdGhpcy5vYmplY3RBc0pTT04gPSBudWxsO1xyXG5cclxuXHJcbiAgICAgICAgLy8gT25seSBmb3IgTW9kYWwgV2luZG93c1xyXG4gICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSBmYWxzZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cclxuICAgICAgICBpZiAoJHNjb3BlLiRvbikge1xyXG4gICAgICAgICAgICAkc2NvcGUuJG9uKCdtb2RhbC5jbG9zaW5nJywgKGV2ZW50LCByZWFzb24sIGNsb3NlZCk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2xvc2VEaWFsb2coZXZlbnQsIHJlYXNvbiwgY2xvc2VkKVxyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBTYXZlcyB0aGUgRm9ybSBpbiAzIGluc3RhbmNlcywgb25lIHRvIGtlZXAgdHJhY2sgb2YgdGhlIG9yaWdpbmFsIGRhdGEsIG90aGVyIGlzIHRoZSBjdXJyZW50IG9iamVjdCBhbmRcclxuICAgICAqIGEgSlNPTiBmb3JtYXQgZm9yIGNvbXBhcmlzb24gcHVycG9zZVxyXG4gICAgICogQHBhcmFtIG5ld09iamVjdEluc3RhbmNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVGb3JtKG5ld09iamVjdEluc3RhbmNlKSB7XHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gbmV3T2JqZWN0SW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5vcmlnaW5hbERhdGEgPSBhbmd1bGFyLmNvcHkobmV3T2JqZWN0SW5zdGFuY2UsIHRoaXMub3JpZ2luYWxEYXRhKTtcclxuICAgICAgICB0aGlzLm9iamVjdEFzSlNPTiA9IGFuZ3VsYXIudG9Kc29uKG5ld09iamVjdEluc3RhbmNlKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEdldCB0aGUgQ3VycmVudCBPYmplY3Qgb24gaGlzIHJlZmVyZW5jZSBGb3JtYXRcclxuICAgICAqIEByZXR1cm5zIHtudWxsfCp9XHJcbiAgICAgKi9cclxuICAgIGdldEZvcm0oKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuY3VycmVudE9iamVjdDtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEdldCB0aGUgT2JqZWN0IGFzIEpTT04gZnJvbSB0aGUgT3JpZ2luYWwgRGF0YVxyXG4gICAgICogQHJldHVybnMge251bGx8c3RyaW5nfHVuZGVmaW5lZHxzdHJpbmd8Kn1cclxuICAgICAqL1xyXG4gICAgZ2V0Rm9ybUFzSlNPTigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5vYmplY3RBc0pTT047XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKlxyXG4gICAgICogQHBhcmFtIG9iamV0VG9SZXNldCBvYmplY3QgdG8gcmVzZXRcclxuICAgICAqIEBwYXJhbSBvblJlc2V0Rm9ybSBjYWxsYmFja1xyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIHJlc2V0Rm9ybShvblJlc2V0Rm9ybSkge1xyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IGFuZ3VsYXIuY29weSh0aGlzLm9yaWdpbmFsRGF0YSwgdGhpcy5jdXJyZW50T2JqZWN0KTtcclxuICAgICAgICB0aGlzLnNhZmVBcHBseSgpO1xyXG5cclxuICAgICAgICBpZihvblJlc2V0Rm9ybSkge1xyXG4gICAgICAgICAgICByZXR1cm4gb25SZXNldEZvcm0oKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWYWxpZGF0ZXMgaWYgdGhlIGN1cnJlbnQgb2JqZWN0IGRpZmZlcnMgZnJvbSB3aGVyZSBpdCB3YXMgb3JpZ2luYWxseSBzYXZlZFxyXG4gICAgICogQHJldHVybnMge2Jvb2xlYW59XHJcbiAgICAgKi9cclxuICAgIGlzRGlydHkoKSB7XHJcbiAgICAgICAgdmFyIG5ld09iamVjdEluc3RhbmNlID0gYW5ndWxhci50b0pzb24odGhpcy5jdXJyZW50T2JqZWN0KTtcclxuICAgICAgICByZXR1cm4gbmV3T2JqZWN0SW5zdGFuY2UgIT09IHRoaXMuZ2V0Rm9ybUFzSlNPTigpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhpcyBmdW5jdGlvbiBpcyBvbmx5IGF2YWlsYWJsZSB3aGVuIHRoZSBGb3JtIGlzIGJlaW5nIGNhbGxlZCBmcm9tIGEgRGlhbG9nIFBvcFVwXHJcbiAgICAgKi9cclxuICAgIG9uQ2xvc2VEaWFsb2coZXZlbnQsIHJlYXNvbiwgY2xvc2VkKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnbW9kYWwuY2xvc2luZzogJyArIChjbG9zZWQgPyAnY2xvc2UnIDogJ2Rpc21pc3MnKSArICcoJyArIHJlYXNvbiArICcpJyk7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNEaXJ0eSgpICYmIHJlYXNvbiAhPT0gJ2NhbmNlbC1jb25maXJtYXRpb24nICYmIHR5cGVvZiByZWFzb24gIT09ICdvYmplY3QnKSB7XHJcbiAgICAgICAgICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XHJcbiAgICAgICAgICAgIHRoaXMuY29uZmlybUNsb3NlRm9ybSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEEgQ29uZmlybWF0aW9uIERpYWxvZyB3aGVuIHRoZSBpbmZvcm1hdGlvbiBjYW4gYmUgbG9zdFxyXG4gICAgICogQHBhcmFtIGV2ZW50XHJcbiAgICAgKi9cclxuICAgIGNvbmZpcm1DbG9zZUZvcm0oZXZlbnQpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgbWVzc2FnZTogJ0NoYW5nZXMgeW91IG1hZGUgbWF5IG5vdCBiZSBzYXZlZC4gRG8geW91IHdhbnQgdG8gY29udGludWU/J1xyXG4gICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwtY29uZmlybWF0aW9uJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBVdGlsIHRvIGNhbGwgc2FmZSBpZiByZXF1aXJlZFxyXG4gICAgICogQHBhcmFtIGZuXHJcbiAgICAgKi9cclxuICAgIHNhZmVBcHBseShmbikge1xyXG4gICAgICAgIHZhciBwaGFzZSA9IHRoaXMuc2NvcGUuJHJvb3QuJCRwaGFzZTtcclxuICAgICAgICBpZihwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgICAgICBpZihmbiAmJiAodHlwZW9mKGZuKSA9PT0gJ2Z1bmN0aW9uJykpIHtcclxuICAgICAgICAgICAgICAgIGZuKCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICB0aGlzLnNjb3BlLiRhcHBseShmbik7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVXRpbCB0byBSZXNldCBhIERyb3Bkb3duIGxpc3Qgb24gS2VuZG9cclxuICAgICAqL1xyXG5cclxuICAgIHJlc2V0RHJvcERvd24oc2VsZWN0b3JJbnN0YW5jZSwgc2VsZWN0ZWRJZCwgZm9yY2UpIHtcclxuICAgICAgICBpZihzZWxlY3Rvckluc3RhbmNlICYmIHNlbGVjdG9ySW5zdGFuY2UuZGF0YUl0ZW1zKSB7XHJcbiAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2UuZGF0YUl0ZW1zKCkuZm9yRWFjaCgodmFsdWUsIGluZGV4KSA9PiB7XHJcbiAgICAgICAgICAgICAgICBpZihzZWxlY3RlZElkID09PSB2YWx1ZS5pZCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2Uuc2VsZWN0KGluZGV4KTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBpZihmb3JjZSkge1xyXG4gICAgICAgICAgICAgICAgc2VsZWN0b3JJbnN0YW5jZS50cmlnZ2VyKCdjaGFuZ2UnKTtcclxuICAgICAgICAgICAgICAgIHRoaXMuc2FmZUFwcGx5KCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIzLzIwMTUuXHJcbiAqIEltcGxlbWVudHMgUlggT2JzZXJ2YWJsZSB0byBkaXNwb3NlIGFuZCB0cmFjayBiZXR0ZXIgZWFjaCBjYWxsIHRvIHRoZSBzZXJ2ZXJcclxuICogVGhlIE9ic2VydmVyIHN1YnNjcmliZSBhIHByb21pc2UuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RIYW5kbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKHJ4KSB7XHJcbiAgICAgICAgdGhpcy5yeCA9IHJ4O1xyXG4gICAgICAgIHRoaXMucHJvbWlzZSA9IFtdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2FsbGVkIGZyb20gUmVzdFNlcnZpY2VIYW5kbGVyLnN1YnNjcmliZVJlcXVlc3RcclxuICAgICAqIGl0IHZlcmlmeSB0aGF0IHRoZSBjYWxsIGlzIGJlaW5nIGRvbmUgdG8gdGhlIHNlcnZlciBhbmQgcmV0dXJuIGEgcHJvbWlzZVxyXG4gICAgICogQHBhcmFtIHJlcXVlc3RcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICBzdWJzY3JpYmVSZXF1ZXN0KHJlcXVlc3QsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHZhciByeE9ic2VydmFibGUgPSB0aGlzLnJ4Lk9ic2VydmFibGUuZnJvbVByb21pc2UocmVxdWVzdCk7XHJcbiAgICAgICAgLy8gVmVyaWZ5IGlzIG5vdCBhIGR1cGxpY2F0ZSBjYWxsXHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgdGhpcy5jYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBTdWJzY3JpYmUgdGhlIHJlcXVlc3RcclxuICAgICAgICB2YXIgcmVzdWx0U3Vic2NyaWJlID0gdGhpcy5hZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgIGlmIChyZXN1bHRTdWJzY3JpYmUgJiYgcmVzdWx0U3Vic2NyaWJlLmlzU3RvcHBlZCkge1xyXG4gICAgICAgICAgICAvLyBBbiBlcnJvciBoYXBwZW5zLCB0cmFja2VkIGJ5IEh0dHBJbnRlcmNlcHRvckludGVyZmFjZVxyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGFkZFN1YnNjcmliZShyeE9ic2VydmFibGUsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdID0gcnhPYnNlcnZhYmxlLnN1YnNjcmliZShcclxuICAgICAgICAgICAgKHJlc3BvbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5vblN1YnNjcmliZWRTdWNjZXNzKHJlc3BvbnNlLCByeE9ic2VydmFibGUsIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIChlcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE5PLU9QIFN1YnNjcmliZSBjb21wbGV0ZWRcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgIH1cclxuXHJcbiAgICBjYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICAgICAgcnhPYnNlcnZhYmxlLmRpc3Bvc2UoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIHJldHVybiAocnhPYnNlcnZhYmxlICYmIHJ4T2JzZXJ2YWJsZS5fcCAmJiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKG9uU3VjY2Vzcyl7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MocmVzcG9uc2UuZGF0YSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhyb3dzIGltbWVkaWF0ZWx5IGVycm9yIHdoZW4gdGhlIHBldGl0aW9uIGNhbGwgaXMgd3JvbmdcclxuICAgICAqIG9yIHdpdGggYSBkZWxheSBpZiB0aGUgY2FsbCBpcyB2YWxpZFxyXG4gICAgICogQHBhcmFtIGVycm9yXHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgb25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcikge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICAgICAgaWYob25FcnJvcil7XHJcbiAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHt9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IFJlc3RTZXJ2aWNlSGFuZGxlciBmcm9tICcuL1Jlc3RTZXJ2aWNlSGFuZGxlci5qcyc7XHJcblxyXG52YXIgUmVzdEFQSU1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5SZXN0QVBJTW9kdWxlJyxbXSk7XHJcblxyXG5SZXN0QVBJTW9kdWxlLnNlcnZpY2UoJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFsnJGxvZycsICckaHR0cCcsICckcmVzb3VyY2UnLCAncngnLCBSZXN0U2VydmljZUhhbmRsZXJdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IFJlc3RBUElNb2R1bGU7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMDgvMTUuXHJcbiAqIEl0IGFic3RyYWN0IGVhY2ggb25lIG9mIHRoZSBleGlzdGluZyBjYWxsIHRvIHRoZSBBUEksIGl0IHNob3VsZCBvbmx5IGNvbnRhaW5zIHRoZSBjYWxsIGZ1bmN0aW9ucyBhbmQgcmVmZXJlbmNlXHJcbiAqIHRvIHRoZSBjYWxsYmFjaywgbm8gbG9naWMgYXQgYWxsLlxyXG4gKlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgUmVxdWVzdEhhbmRsZXIgZnJvbSAnLi9SZXF1ZXN0SGFuZGxlci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXN0U2VydmljZUhhbmRsZXIge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJGh0dHAsICRyZXNvdXJjZSwgcngpIHtcclxuICAgICAgICB0aGlzLnJ4ID0gcng7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuaHR0cCA9ICRodHRwO1xyXG4gICAgICAgIHRoaXMucmVzb3VyY2UgPSAkcmVzb3VyY2U7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVycygpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdSZXN0U2VydmljZSBMb2FkZWQnKTtcclxuICAgICAgICB0aGlzLnJlcSA9IHtcclxuICAgICAgICAgICAgbWV0aG9kOiAnJyxcclxuICAgICAgICAgICAgdXJsOiAnJyxcclxuICAgICAgICAgICAgaGVhZGVyczoge1xyXG4gICAgICAgICAgICAgICAgJ0NvbnRlbnQtVHlwZSc6ICdhcHBsaWNhdGlvbi9qc29uO2NoYXJzZXQ9VVRGLTgnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGE6IFtdXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlSGVhZGVycygpIHtcclxuICAgICAgICB0aGlzLmh0dHAuZGVmYXVsdHMuaGVhZGVycy5wb3N0WydDb250ZW50LVR5cGUnXSA9ICdhcHBsaWNhdGlvbi94LXd3dy1mb3JtLXVybGVuY29kZWQnO1xyXG4gICAgfVxyXG5cclxuICAgIFRhc2tTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRGZWVkczogKGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJ3Rlc3QvbW9ja3VwRGF0YS9UYXNrTWFuYWdlci90YXNrTWFuYWdlckxpc3QuanNvbicpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldExpY2Vuc2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlcycpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldFByb2plY3REYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9wcm9qZWN0JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdDogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYXBwbHlMaWNlbnNlOiAgKGxpY2Vuc2VJZCwgZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvbG9hZCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRIYXNoQ29kZTogIChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvaGFzaCc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgLy8tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gICAgICAgICAgICByZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGVtYWlsUmVxdWVzdDogKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkZWxldGVMaWNlbnNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgbGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICByZXF1ZXN0SW1wb3J0OiAgKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvcmVxdWVzdCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRMaWNlbnNlTGlzdDogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL21hbmFnZXIvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvcHJvamVjdCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEtleUNvZGU6ICAobGljZW5zZUlkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdHRVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcva2V5JztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzYXZlTGljZW5zZTogKGxpY2Vuc2VJZCwgbGljZW5zZU1vZGlmaWVkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQVVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZDtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBsaWNlbnNlTW9kaWZpZWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgcmV2b2tlTGljZW5zZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGVMaWNlbnNlOiAobGljZW5zZUlkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2FjdGl2YXRlJztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRBY3Rpdml0eUxvZzogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnR0VUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2FjdGl2aXR5bG9nJztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgbm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldE5vdGljZUxpc3Q6IChvblN1Y2Nlc3MpID0+IHsgLy8gcmVhbCB3cyBleGFtcGxlXHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3Mvbm90aWNlcycpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOb3RpY2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3Mvbm90aWNlcyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlZGl0Tm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUFVUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3Mvbm90aWNlcy8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkZWxldGVOb3RpY2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdERUxFVEUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9ub3RpY2VzLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICogRVM2IEludGVyY2VwdG9yIGNhbGxzIGlubmVyIG1ldGhvZHMgaW4gYSBnbG9iYWwgc2NvcGUsIHRoZW4gdGhlIFwidGhpc1wiIGlzIGJlaW5nIGxvc3RcclxuICogaW4gdGhlIGRlZmluaXRpb24gb2YgdGhlIENsYXNzIGZvciBpbnRlcmNlcHRvcnMgb25seVxyXG4gKiBUaGlzIGlzIGEgaW50ZXJmYWNlIHRoYXQgdGFrZSBjYXJlIG9mIHRoZSBpc3N1ZS5cclxuICovXHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgLyogaW50ZXJmYWNlKi8gY2xhc3MgSHR0cEludGVyY2VwdG9yIHtcclxuICAgIGNvbnN0cnVjdG9yKG1ldGhvZFRvQmluZCkge1xyXG4gICAgICAgIC8vIElmIG5vdCBtZXRob2QgdG8gYmluZCwgd2UgYXNzdW1lIG91ciBpbnRlcmNlcHRvciBpcyB1c2luZyBhbGwgdGhlIGlubmVyIGZ1bmN0aW9uc1xyXG4gICAgICAgIGlmKCFtZXRob2RUb0JpbmQpIHtcclxuICAgICAgICAgICAgWydyZXF1ZXN0JywgJ3JlcXVlc3RFcnJvcicsICdyZXNwb25zZScsICdyZXNwb25zZUVycm9yJ11cclxuICAgICAgICAgICAgICAgIC5mb3JFYWNoKChtZXRob2QpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzW21ldGhvZF0pIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpc1ttZXRob2RdID0gdGhpc1ttZXRob2RdLmJpbmQodGhpcyk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgLy8gbWV0aG9kVG9CaW5kIHJlZmVyZW5jZSB0byBhIHNpbmdsZSBjaGlsZCBjbGFzc1xyXG4gICAgICAgICAgICB0aGlzW21ldGhvZFRvQmluZF0gPSB0aGlzW21ldGhvZFRvQmluZF0uYmluZCh0aGlzKTtcclxuICAgICAgICB9XHJcblxyXG4gICAgfVxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICogVXNlIHRoaXMgbW9kdWxlIHRvIG1vZGlmeSBhbnl0aGluZyByZWxhdGVkIHRvIHRoZSBIZWFkZXJzIGFuZCBSZXF1ZXN0XHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcblxyXG5cclxudmFyIEhUVFBNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uSFRUUE1vZHVsZScsIFsnbmdSZXNvdXJjZSddKS5jb25maWcoWyckaHR0cFByb3ZpZGVyJywgZnVuY3Rpb24oJGh0dHBQcm92aWRlcil7XHJcblxyXG4gICAgLy9pbml0aWFsaXplIGdldCBpZiBub3QgdGhlcmVcclxuICAgIGlmICghJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldCkge1xyXG4gICAgICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXQgPSB7fTtcclxuICAgIH1cclxuXHJcbiAgICAvL0Rpc2FibGUgSUUgYWpheCByZXF1ZXN0IGNhY2hpbmdcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ0lmLU1vZGlmaWVkLVNpbmNlJ10gPSAnTW9uLCAyNiBKdWwgMTk5NyAwNTowMDowMCBHTVQnO1xyXG4gICAgLy8gZXh0cmFcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ0NhY2hlLUNvbnRyb2wnXSA9ICduby1jYWNoZSc7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydQcmFnbWEnXSA9ICduby1jYWNoZSc7XHJcblxyXG5cclxuICAgIC8vIEluamVjdHMgb3VyIEludGVyY2VwdG9ycyBmb3IgUmVxdWVzdFxyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgIC8vIEluamVjdHMgb3VyIEludGVyY2VwdG9ycyBmb3IgUmVzcG9uc2VcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuXHJcblxyXG59XSk7XHJcblxyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IEhUVFBNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSXQgaW1wbGVtZW50IGFuIGFic3RyYWN0IGNhbGwgdG8gSFRUUCBJbnRlcmNlcHRvcnMgdG8gbWFuYWdlIGVycm9yIGhhbmRsZXJcclxuICogU29tZXRpbWVzIGEgcmVxdWVzdCBjYW4ndCBiZSBzZW50IG9yIGl0IGlzIHJlamVjdGVkIGJ5IGFuIGludGVyY2VwdG9yLlxyXG4gKiBSZXF1ZXN0IGVycm9yIGludGVyY2VwdG9yIGNhcHR1cmVzIHJlcXVlc3RzIHRoYXQgaGF2ZSBiZWVuIGNhbmNlbGVkIGJ5IGEgcHJldmlvdXMgcmVxdWVzdCBpbnRlcmNlcHRvci5cclxuICogSXQgY2FuIGJlIHVzZWQgaW4gb3JkZXIgdG8gcmVjb3ZlciB0aGUgcmVxdWVzdCBhbmQgc29tZXRpbWVzIHVuZG8gdGhpbmdzIHRoYXQgaGF2ZSBiZWVuIHNldCB1cCBiZWZvcmUgYSByZXF1ZXN0LFxyXG4gKiBsaWtlIHJlbW92aW5nIG92ZXJsYXlzIGFuZCBsb2FkaW5nIGluZGljYXRvcnMsIGVuYWJsaW5nIGJ1dHRvbnMgYW5kIGZpZWxkcyBhbmQgc28gb24uXHJcbiAqL1xyXG5cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVxdWVzdEVycm9yJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXF1ZXN0RXJyb3IocmVqZWN0aW9uKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy9pZiAoY2FuUmVjb3ZlcihyZWplY3Rpb24pKSB7XHJcbiAgICAgICAgLy8gICAgcmV0dXJuIHJlc3BvbnNlT3JOZXdQcm9taXNlXHJcbiAgICAgICAgLy99XHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVqZWN0aW9uKTtcclxuXHJcbiAgICAgICAgcmV0dXJuIHRoaXMucS5yZWplY3QocmVqZWN0aW9uKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5FcnJvcigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJdCBpbXBsZW1lbnQgYW4gYWJzdHJhY3QgY2FsbCB0byBIVFRQIEludGVyY2VwdG9ycyB0byBtYW5hZ2Ugb25seSByZXF1ZXN0XHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3JlcXVlc3QnKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXF1ZXN0KGNvbmZpZykge1xyXG4gICAgICAgIC8vIFdlIGNhbiBhZGQgaGVhZGVycyBpZiBvbiB0aGUgaW5jb21pbmcgcmVxdWVzdCBtYWRlIGl0IHdlIGhhdmUgdGhlIHRva2VuIGluc2lkZVxyXG4gICAgICAgIC8vIGRlZmluZWQgYnkgc29tZSBjb25kaXRpb25zXHJcbiAgICAgICAgLy9jb25maWcuaGVhZGVyc1sneC1zZXNzaW9uLXRva2VuJ10gPSBteS50b2tlbjtcclxuXHJcbiAgICAgICAgY29uZmlnLnJlcXVlc3RUaW1lc3RhbXAgPSBuZXcgRGF0ZSgpLmdldFRpbWUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkoY29uZmlnKTtcclxuXHJcbiAgICAgICAgcmV0dXJuIGNvbmZpZyB8fCB0aGlzLnEud2hlbihjb25maWcpO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlblJlcXVlc3QoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSWYgYmFja2VuZCBjYWxsIGZhaWxzIG9yIGl0IG1pZ2h0IGJlIHJlamVjdGVkIGJ5IGEgcmVxdWVzdCBpbnRlcmNlcHRvciBvciBieSBhIHByZXZpb3VzIHJlc3BvbnNlIGludGVyY2VwdG9yO1xyXG4gKiBJbiB0aG9zZSBjYXNlcywgcmVzcG9uc2UgZXJyb3IgaW50ZXJjZXB0b3IgY2FuIGhlbHAgdXMgdG8gcmVjb3ZlciB0aGUgYmFja2VuZCBjYWxsLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3Jlc3BvbnNlRXJyb3InKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXNwb25zZUVycm9yKHJlamVjdGlvbikge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vaWYgKGNhblJlY292ZXIocmVqZWN0aW9uKSkge1xyXG4gICAgICAgIC8vICAgIHJldHVybiByZXNwb25zZU9yTmV3UHJvbWlzZVxyXG4gICAgICAgIC8vIH1cclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVqZWN0aW9uKTtcclxuICAgICAgICByZXR1cm4gdGhpcy5xLnJlamVjdChyZWplY3Rpb24pO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlbkVycm9yKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIFRoaXMgbWV0aG9kIGlzIGNhbGxlZCByaWdodCBhZnRlciAkaHR0cCByZWNlaXZlcyB0aGUgcmVzcG9uc2UgZnJvbSB0aGUgYmFja2VuZCxcclxuICogc28geW91IGNhbiBtb2RpZnkgdGhlIHJlc3BvbnNlIGFuZCBtYWtlIG90aGVyIGFjdGlvbnMuIFRoaXMgZnVuY3Rpb24gcmVjZWl2ZXMgYSByZXNwb25zZSBvYmplY3QgYXMgYSBwYXJhbWV0ZXJcclxuICogYW5kIGhhcyB0byByZXR1cm4gYSByZXNwb25zZSBvYmplY3Qgb3IgYSBwcm9taXNlLiBUaGUgcmVzcG9uc2Ugb2JqZWN0IGluY2x1ZGVzXHJcbiAqIHRoZSByZXF1ZXN0IGNvbmZpZ3VyYXRpb24sIGhlYWRlcnMsIHN0YXR1cyBhbmQgZGF0YSB0aGF0IHJldHVybmVkIGZyb20gdGhlIGJhY2tlbmQuXHJcbiAqIFJldHVybmluZyBhbiBpbnZhbGlkIHJlc3BvbnNlIG9iamVjdCBvciBwcm9taXNlIHRoYXQgd2lsbCBiZSByZWplY3RlZCwgd2lsbCBtYWtlIHRoZSAkaHR0cCBjYWxsIHRvIGZhaWwuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3Jlc3BvbnNlJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3BvbnNlKHJlc3BvbnNlKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIHN1Y2Nlc3NcclxuXHJcbiAgICAgICAgcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlc3BvbnNlKTtcclxuICAgICAgICByZXR1cm4gcmVzcG9uc2UgfHwgdGhpcy5xLndoZW4ocmVzcG9uc2UpO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlblJlc3BvbnNlKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcbn1cclxuXHJcbiJdfQ==
