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

// It will be removed after we rip off all iframes
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
'ui.router', 'ngclipboard', 'kendo.directives', 'rx', 'formly', 'formlyBootstrap', 'ui.bootstrap', _HTTPModule2.default.name, _RestAPIModule2.default.name, _HeaderModule2.default.name, _TaskManagerModule2.default.name, _LicenseAdminModule2.default.name, _LicenseManagerModule2.default.name, _NoticeManagerModule2.default.name]).config(['$logProvider', '$rootScopeProvider', '$compileProvider', '$controllerProvider', '$provide', '$httpProvider', '$translateProvider', '$translatePartialLoaderProvider', '$urlRouterProvider', '$locationProvider', function ($logProvider, $rootScopeProvider, $compileProvider, $controllerProvider, $provide, $httpProvider, $translateProvider, $translatePartialLoaderProvider, $urlRouterProvider) {

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
                $timeout(turnOffNotifications, 2000);
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

var LicenseAdminModule = _angular2.default.module('TDSTM.LicenseAdminModule', [_uiRouter2.default]).config(['$stateProvider', '$translatePartialLoaderProvider', function ($stateProvider, $translatePartialLoaderProvider) {

    $translatePartialLoaderProvider.addPart('licenseAdmin');

    // Define a generic header for the entire module, or it can be changed for each instance.
    var header = {
        templateUrl: '../app-js/modules/header/HeaderView.html',
        controller: 'HeaderController as header'
    };

    $stateProvider.state('licenseAdminList', {
        data: { page: { title: 'Administer Licenses', instruction: '', menu: ['ADMIN', 'LICENSE', 'LIST'] } },
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
LicenseAdminModule.controller('ManuallyRequest', ['$log', 'LicenseAdminService', '$uibModalInstance', 'params', _ManuallyRequest2.default]);
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
            environmentName: params.license.environment.name,
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
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project' }, { field: 'email', title: 'Contact Email' }, { field: 'status.name', title: 'Status' }, { field: 'type.name', title: 'Type', template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#' }, { field: 'method.name', title: 'Method' }, { field: 'method.id', hidden: true }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'requestDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'environment.name', title: 'Environment' }],
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
    function ManuallyRequest($log, licenseAdminService, $uibModalInstance, params) {
        _classCallCheck(this, ManuallyRequest);

        this.log = $log;
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
            environmentId: 0,
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
                        return enviroment.name === 'Production';
                    });
                    index = index || 0;
                    _this2.newLicenseModel.environmentId = data[index].id;
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
            newLicense.environmentId = parseInt(newLicense.environmentId);
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
        data: { page: { title: 'Licensing Manager', instruction: '', menu: ['MANAGER', 'LICENSE', 'LIST'] } },
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

        _this.licenseManagerService = licenseManagerService;
        _this.uibModalInstance = $uibModalInstance;
        _this.uibModal = $uibModal;
        _this.log = $log;

        _this.editMode = false;

        _this.licenseModel = {
            id: params.license.id,
            ownerName: params.license.owner.name,
            email: params.license.email,
            projectId: params.license.project.id,
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
            activityList: params.license.activityList,
            hostName: params.license.hostName,
            hash: params.license.id,
            gracePeriodDays: params.license.gracePeriodDays,

            applied: params.license.applied,
            keyId: params.license.keyId
        };

        _this.licenseKey = 'Licenses has not been issued';

        // Creates the Project Select List
        // Define the Project Select
        _this.selectProject = {};
        _this.selectProjectListOptions = [];
        _this.getProjectDataSource();

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
     * Populate the Project dropdown values
     */

    _createClass(LicenseManagerDetail, [{
        key: 'getProjectDataSource',
        value: function getProjectDataSource() {
            var _this2 = this;

            this.selectProjectListOptions = {
                dataSource: {
                    transport: {
                        read: function read(e) {
                            _this2.licenseManagerService.getProjectDataSource(function (data) {
                                if (!_this2.licenseModel.projectId) {
                                    _this2.licenseModel.projectId = data[0].id;
                                }

                                _this2.saveForm(_this2.licenseModel);
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
                    var item = _this2.selectProject.dataItem(e.item);
                    _this2.licenseModel.clientName = item.client.name;
                }
            };
        }

        /**
         * Controls what buttons to show
         */

    }, {
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
            var _this3 = this;

            if (this.licenseModel.statusId === 1) {
                this.licenseManagerService.getKeyCode(this.licenseModel.id, function (data) {
                    if (data) {
                        _this3.licenseKey = data;
                    }
                });
            }
        }
    }, {
        key: 'prepareActivityList',
        value: function prepareActivityList() {
            this.activityGrid = {};
            this.activityGridOptions = {
                columns: [{ field: 'date', title: 'Date' }, { field: 'whom', title: 'Whom' }, { field: 'action', title: 'Action' }],
                dataSource: this.licenseModel.activityList,
                scrollable: true
            };
        }

        /**
         * If by some reason the License was not applied at first time, this will do a request for it
         */

    }, {
        key: 'activateLicense',
        value: function activateLicense() {
            this.licenseManagerService.activateLicense(this.licenseModel, function (data) {
                //this.prepareLicenseKey();
            });
        }
    }, {
        key: 'revokeLicense',
        value: function revokeLicense() {
            var _this4 = this;

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
                _this4.licenseManagerService.revokeLicense(_this4.licenseModel, function (data) {
                    _this4.uibModalInstance.close(data);
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
            var _this5 = this;

            if (this.isDirty()) {
                this.editMode = false;
                this.prepareControlActionButtons();
                this.licenseManagerService.saveLicense(this.licenseModel, function (data) {
                    _this5.reloadRequired = true;
                    _this5.saveForm(_this5.licenseModel);
                    _this5.log.info('License Saved');
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
            var _this6 = this;

            this.selectEnvironmentListOptions = {
                dataSource: {
                    transport: {
                        read: function read(e) {
                            _this6.licenseManagerService.getEnvironmentDataSource(function (data) {
                                if (!_this6.licenseModel.environmentId) {
                                    _this6.licenseModel.environmentId = data[0].id;
                                }
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
            this.statusText = this.licenseModel.statusId === 1 ? 'Active' : this.licenseModel.statusId === 2 ? 'Expired' : this.licenseModel.statusId === 3 ? 'Terminated' : this.licenseModel.statusId === 4 ? 'Pending' : '';

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
            var _this7 = this;

            if (this.editMode) {
                this.resetForm(function () {
                    _this7.onResetForm();
                });
            } else if (this.reloadRequired) {
                this.uibModalInstance.close({});
            } else {
                this.uibModalInstance.dismiss('cancel');
            }
        }

        /**
         * Depeding the number of fields and type of field, the reset can't be on the FormValidor, at least not now
         */

    }, {
        key: 'onResetForm',
        value: function onResetForm() {
            // Reset Project Selector
            this.resetDropDown(this.selectProject, this.licenseModel.projectId);
            //this.resetDropDown(this.selectStatus, this.licenseModel.statusId);
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
                activationDate: license.initDate,
                expirationDate: license.endDate,
                status: { id: license.statusId },
                project: { id: license.projectId !== 'all' ? parseInt(license.projectId) : license.projectId }, // We pass 'all' when is multiproject
                bannerMessage: license.bannerMessage,
                gracePeriodDays: license.gracePeriodDays
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

            this.restService.licenseManagerServiceHandler().activateLicense(license, function (data) {
                _this.rootScope.$emit('broadcast-msg', { type: 'info', text: 'The license was activated and the license was emailed.' });
                return callback(data);
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
                saveLicense: function saveLicense(licenseid, licenseModified, onSuccess, onError) {
                    _this3.req.method = 'PUT';
                    _this3.req.url = '../ws/manager/license/' + licenseid;
                    _this3.req.data = licenseModified;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                revokeLicense: function revokeLicense(data, onSuccess, onError) {
                    _this3.req.method = 'DELETE';
                    _this3.req.url = '../ws/manager/license/' + data.id;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http(_this3.req), onSuccess, onError);
                },
                activateLicense: function activateLicense(data, callback) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/???';
                    _this3.req.data = data;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvY2xpcGJvYXJkL2xpYi9jbGlwYm9hcmQtYWN0aW9uLmpzIiwibm9kZV9tb2R1bGVzL2NsaXBib2FyZC9saWIvY2xpcGJvYXJkLmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9jbG9zZXN0LmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9kZWxlZ2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9pcy5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9saXN0ZW4uanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvZGlzdC9uZ2NsaXBib2FyZC5qcyIsIm5vZGVfbW9kdWxlcy9zZWxlY3Qvc3JjL3NlbGVjdC5qcyIsIm5vZGVfbW9kdWxlcy90aW55LWVtaXR0ZXIvaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxjb25maWdcXEFwcC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxNb2RhbFdpbmRvd0FjdGl2YXRpb24uanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxUb2FzdEhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1haW4uanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGRpYWxvZ0FjdGlvblxcRGlhbG9nQWN0aW9uLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxoZWFkZXJcXEhlYWRlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGhlYWRlclxcSGVhZGVyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXExpY2Vuc2VBZG1pbk1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxhcHBseUxpY2Vuc2VLZXlcXEFwcGx5TGljZW5zZUtleS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxjcmVhdGVkXFxDcmVhdGVkTGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxkZXRhaWxcXExpY2Vuc2VEZXRhaWwuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcbGlzdFxcTGljZW5zZUFkbWluTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxtYW51YWxseVJlcXVlc3RcXE1hbnVhbGx5UmVxdWVzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxyZXF1ZXN0XFxSZXF1ZXN0TGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxzZXJ2aWNlXFxMaWNlbnNlQWRtaW5TZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcTGljZW5zZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxkZXRhaWxcXExpY2Vuc2VNYW5hZ2VyRGV0YWlsLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcbGlzdFxcTGljZW5zZU1hbmFnZXJMaXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxccmVxdWVzdEltcG9ydFxcUmVxdWVzdEltcG9ydC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXHNlcnZpY2VcXExpY2Vuc2VNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcTm90aWNlTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcZWRpdFxcRWRpdE5vdGljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcbGlzdFxcTm90aWNlTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcc2VydmljZVxcTm90aWNlTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxUYXNrTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGVkaXRcXFRhc2tNYW5hZ2VyRWRpdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGxpc3RcXFRhc2tNYW5hZ2VyQ29udHJvbGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXHNlcnZpY2VcXFRhc2tNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdXRpbHNcXGZvcm1cXEZvcm1WYWxpZGF0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RBUElNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXN0U2VydmljZUhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwT0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4TEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL0ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7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O2lCQUFBLEFBQUssTUFBTCxBQUFXLE9BQVgsQUFBa0IsQUFDbEI7bUJBQU8sYUFBWSxLQUFBLEFBQUssRUFBTCxBQUFPLEtBQTFCLEFBQW1CLEFBQVksQUFDbEM7Ozs7eUNBRWdCLEFBQ2I7bUJBQU8sS0FBQSxBQUFLLE1BQVosQUFBa0IsQUFDckI7Ozs7O0dBcEJ1RCxjOztrQixBQUF2QyIsImZpbGUiOiJnZW5lcmF0ZWQuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlc0NvbnRlbnQiOlsiKGZ1bmN0aW9uIGUodCxuLHIpe2Z1bmN0aW9uIHMobyx1KXtpZighbltvXSl7aWYoIXRbb10pe3ZhciBhPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7aWYoIXUmJmEpcmV0dXJuIGEobywhMCk7aWYoaSlyZXR1cm4gaShvLCEwKTt2YXIgZj1uZXcgRXJyb3IoXCJDYW5ub3QgZmluZCBtb2R1bGUgJ1wiK28rXCInXCIpO3Rocm93IGYuY29kZT1cIk1PRFVMRV9OT1RfRk9VTkRcIixmfXZhciBsPW5bb109e2V4cG9ydHM6e319O3Rbb11bMF0uY2FsbChsLmV4cG9ydHMsZnVuY3Rpb24oZSl7dmFyIG49dFtvXVsxXVtlXTtyZXR1cm4gcyhuP246ZSl9LGwsbC5leHBvcnRzLGUsdCxuLHIpfXJldHVybiBuW29dLmV4cG9ydHN9dmFyIGk9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtmb3IodmFyIG89MDtvPHIubGVuZ3RoO28rKylzKHJbb10pO3JldHVybiBzfSkiLCIoZnVuY3Rpb24gKGdsb2JhbCwgZmFjdG9yeSkge1xuICAgIGlmICh0eXBlb2YgZGVmaW5lID09PSBcImZ1bmN0aW9uXCIgJiYgZGVmaW5lLmFtZCkge1xuICAgICAgICBkZWZpbmUoWydtb2R1bGUnLCAnc2VsZWN0J10sIGZhY3RvcnkpO1xuICAgIH0gZWxzZSBpZiAodHlwZW9mIGV4cG9ydHMgIT09IFwidW5kZWZpbmVkXCIpIHtcbiAgICAgICAgZmFjdG9yeShtb2R1bGUsIHJlcXVpcmUoJ3NlbGVjdCcpKTtcbiAgICB9IGVsc2Uge1xuICAgICAgICB2YXIgbW9kID0ge1xuICAgICAgICAgICAgZXhwb3J0czoge31cbiAgICAgICAgfTtcbiAgICAgICAgZmFjdG9yeShtb2QsIGdsb2JhbC5zZWxlY3QpO1xuICAgICAgICBnbG9iYWwuY2xpcGJvYXJkQWN0aW9uID0gbW9kLmV4cG9ydHM7XG4gICAgfVxufSkodGhpcywgZnVuY3Rpb24gKG1vZHVsZSwgX3NlbGVjdCkge1xuICAgICd1c2Ugc3RyaWN0JztcblxuICAgIHZhciBfc2VsZWN0MiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3NlbGVjdCk7XG5cbiAgICBmdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikge1xuICAgICAgICByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDoge1xuICAgICAgICAgICAgZGVmYXVsdDogb2JqXG4gICAgICAgIH07XG4gICAgfVxuXG4gICAgdmFyIF90eXBlb2YgPSB0eXBlb2YgU3ltYm9sID09PSBcImZ1bmN0aW9uXCIgJiYgdHlwZW9mIFN5bWJvbC5pdGVyYXRvciA9PT0gXCJzeW1ib2xcIiA/IGZ1bmN0aW9uIChvYmopIHtcbiAgICAgICAgcmV0dXJuIHR5cGVvZiBvYmo7XG4gICAgfSA6IGZ1bmN0aW9uIChvYmopIHtcbiAgICAgICAgcmV0dXJuIG9iaiAmJiB0eXBlb2YgU3ltYm9sID09PSBcImZ1bmN0aW9uXCIgJiYgb2JqLmNvbnN0cnVjdG9yID09PSBTeW1ib2wgJiYgb2JqICE9PSBTeW1ib2wucHJvdG90eXBlID8gXCJzeW1ib2xcIiA6IHR5cGVvZiBvYmo7XG4gICAgfTtcblxuICAgIGZ1bmN0aW9uIF9jbGFzc0NhbGxDaGVjayhpbnN0YW5jZSwgQ29uc3RydWN0b3IpIHtcbiAgICAgICAgaWYgKCEoaW5zdGFuY2UgaW5zdGFuY2VvZiBDb25zdHJ1Y3RvcikpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoXCJDYW5ub3QgY2FsbCBhIGNsYXNzIGFzIGEgZnVuY3Rpb25cIik7XG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIgX2NyZWF0ZUNsYXNzID0gZnVuY3Rpb24gKCkge1xuICAgICAgICBmdW5jdGlvbiBkZWZpbmVQcm9wZXJ0aWVzKHRhcmdldCwgcHJvcHMpIHtcbiAgICAgICAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgcHJvcHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgICAgICAgICB2YXIgZGVzY3JpcHRvciA9IHByb3BzW2ldO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuZW51bWVyYWJsZSA9IGRlc2NyaXB0b3IuZW51bWVyYWJsZSB8fCBmYWxzZTtcbiAgICAgICAgICAgICAgICBkZXNjcmlwdG9yLmNvbmZpZ3VyYWJsZSA9IHRydWU7XG4gICAgICAgICAgICAgICAgaWYgKFwidmFsdWVcIiBpbiBkZXNjcmlwdG9yKSBkZXNjcmlwdG9yLndyaXRhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBPYmplY3QuZGVmaW5lUHJvcGVydHkodGFyZ2V0LCBkZXNjcmlwdG9yLmtleSwgZGVzY3JpcHRvcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gZnVuY3Rpb24gKENvbnN0cnVjdG9yLCBwcm90b1Byb3BzLCBzdGF0aWNQcm9wcykge1xuICAgICAgICAgICAgaWYgKHByb3RvUHJvcHMpIGRlZmluZVByb3BlcnRpZXMoQ29uc3RydWN0b3IucHJvdG90eXBlLCBwcm90b1Byb3BzKTtcbiAgICAgICAgICAgIGlmIChzdGF0aWNQcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvciwgc3RhdGljUHJvcHMpO1xuICAgICAgICAgICAgcmV0dXJuIENvbnN0cnVjdG9yO1xuICAgICAgICB9O1xuICAgIH0oKTtcblxuICAgIHZhciBDbGlwYm9hcmRBY3Rpb24gPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIC8qKlxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cbiAgICAgICAgZnVuY3Rpb24gQ2xpcGJvYXJkQWN0aW9uKG9wdGlvbnMpIHtcbiAgICAgICAgICAgIF9jbGFzc0NhbGxDaGVjayh0aGlzLCBDbGlwYm9hcmRBY3Rpb24pO1xuXG4gICAgICAgICAgICB0aGlzLnJlc29sdmVPcHRpb25zKG9wdGlvbnMpO1xuICAgICAgICAgICAgdGhpcy5pbml0U2VsZWN0aW9uKCk7XG4gICAgICAgIH1cblxuICAgICAgICAvKipcbiAgICAgICAgICogRGVmaW5lcyBiYXNlIHByb3BlcnRpZXMgcGFzc2VkIGZyb20gY29uc3RydWN0b3IuXG4gICAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvcHRpb25zXG4gICAgICAgICAqL1xuXG5cbiAgICAgICAgX2NyZWF0ZUNsYXNzKENsaXBib2FyZEFjdGlvbiwgW3tcbiAgICAgICAgICAgIGtleTogJ3Jlc29sdmVPcHRpb25zJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiByZXNvbHZlT3B0aW9ucygpIHtcbiAgICAgICAgICAgICAgICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPiAwICYmIGFyZ3VtZW50c1swXSAhPT0gdW5kZWZpbmVkID8gYXJndW1lbnRzWzBdIDoge307XG5cbiAgICAgICAgICAgICAgICB0aGlzLmFjdGlvbiA9IG9wdGlvbnMuYWN0aW9uO1xuICAgICAgICAgICAgICAgIHRoaXMuZW1pdHRlciA9IG9wdGlvbnMuZW1pdHRlcjtcbiAgICAgICAgICAgICAgICB0aGlzLnRhcmdldCA9IG9wdGlvbnMudGFyZ2V0O1xuICAgICAgICAgICAgICAgIHRoaXMudGV4dCA9IG9wdGlvbnMudGV4dDtcbiAgICAgICAgICAgICAgICB0aGlzLnRyaWdnZXIgPSBvcHRpb25zLnRyaWdnZXI7XG5cbiAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdGVkVGV4dCA9ICcnO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdpbml0U2VsZWN0aW9uJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBpbml0U2VsZWN0aW9uKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLnRleHQpIHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RGYWtlKCk7XG4gICAgICAgICAgICAgICAgfSBlbHNlIGlmICh0aGlzLnRhcmdldCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdFRhcmdldCgpO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnc2VsZWN0RmFrZScsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gc2VsZWN0RmFrZSgpIHtcbiAgICAgICAgICAgICAgICB2YXIgX3RoaXMgPSB0aGlzO1xuXG4gICAgICAgICAgICAgICAgdmFyIGlzUlRMID0gZG9jdW1lbnQuZG9jdW1lbnRFbGVtZW50LmdldEF0dHJpYnV0ZSgnZGlyJykgPT0gJ3J0bCc7XG5cbiAgICAgICAgICAgICAgICB0aGlzLnJlbW92ZUZha2UoKTtcblxuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUhhbmRsZXJDYWxsYmFjayA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIF90aGlzLnJlbW92ZUZha2UoKTtcbiAgICAgICAgICAgICAgICB9O1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUhhbmRsZXIgPSBkb2N1bWVudC5ib2R5LmFkZEV2ZW50TGlzdGVuZXIoJ2NsaWNrJywgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrKSB8fCB0cnVlO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbSA9IGRvY3VtZW50LmNyZWF0ZUVsZW1lbnQoJ3RleHRhcmVhJyk7XG4gICAgICAgICAgICAgICAgLy8gUHJldmVudCB6b29taW5nIG9uIGlPU1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGUuZm9udFNpemUgPSAnMTJwdCc7XG4gICAgICAgICAgICAgICAgLy8gUmVzZXQgYm94IG1vZGVsXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5ib3JkZXIgPSAnMCc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5wYWRkaW5nID0gJzAnO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGUubWFyZ2luID0gJzAnO1xuICAgICAgICAgICAgICAgIC8vIE1vdmUgZWxlbWVudCBvdXQgb2Ygc2NyZWVuIGhvcml6b250YWxseVxuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGUucG9zaXRpb24gPSAnYWJzb2x1dGUnO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGVbaXNSVEwgPyAncmlnaHQnIDogJ2xlZnQnXSA9ICctOTk5OXB4JztcbiAgICAgICAgICAgICAgICAvLyBNb3ZlIGVsZW1lbnQgdG8gdGhlIHNhbWUgcG9zaXRpb24gdmVydGljYWxseVxuICAgICAgICAgICAgICAgIHZhciB5UG9zaXRpb24gPSB3aW5kb3cucGFnZVlPZmZzZXQgfHwgZG9jdW1lbnQuZG9jdW1lbnRFbGVtZW50LnNjcm9sbFRvcDtcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLmFkZEV2ZW50TGlzdGVuZXIoJ2ZvY3VzJywgd2luZG93LnNjcm9sbFRvKDAsIHlQb3NpdGlvbikpO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGUudG9wID0geVBvc2l0aW9uICsgJ3B4JztcblxuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc2V0QXR0cmlidXRlKCdyZWFkb25seScsICcnKTtcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnZhbHVlID0gdGhpcy50ZXh0O1xuXG4gICAgICAgICAgICAgICAgZG9jdW1lbnQuYm9keS5hcHBlbmRDaGlsZCh0aGlzLmZha2VFbGVtKTtcblxuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gKDAsIF9zZWxlY3QyLmRlZmF1bHQpKHRoaXMuZmFrZUVsZW0pO1xuICAgICAgICAgICAgICAgIHRoaXMuY29weVRleHQoKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAncmVtb3ZlRmFrZScsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gcmVtb3ZlRmFrZSgpIHtcbiAgICAgICAgICAgICAgICBpZiAodGhpcy5mYWtlSGFuZGxlcikge1xuICAgICAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LnJlbW92ZUV2ZW50TGlzdGVuZXIoJ2NsaWNrJywgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrKTtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlciA9IG51bGw7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuZmFrZUhhbmRsZXJDYWxsYmFjayA9IG51bGw7XG4gICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuZmFrZUVsZW0pIHtcbiAgICAgICAgICAgICAgICAgICAgZG9jdW1lbnQuYm9keS5yZW1vdmVDaGlsZCh0aGlzLmZha2VFbGVtKTtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbSA9IG51bGw7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdzZWxlY3RUYXJnZXQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHNlbGVjdFRhcmdldCgpIHtcbiAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdGVkVGV4dCA9ICgwLCBfc2VsZWN0Mi5kZWZhdWx0KSh0aGlzLnRhcmdldCk7XG4gICAgICAgICAgICAgICAgdGhpcy5jb3B5VGV4dCgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdjb3B5VGV4dCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gY29weVRleHQoKSB7XG4gICAgICAgICAgICAgICAgdmFyIHN1Y2NlZWRlZCA9IHZvaWQgMDtcblxuICAgICAgICAgICAgICAgIHRyeSB7XG4gICAgICAgICAgICAgICAgICAgIHN1Y2NlZWRlZCA9IGRvY3VtZW50LmV4ZWNDb21tYW5kKHRoaXMuYWN0aW9uKTtcbiAgICAgICAgICAgICAgICB9IGNhdGNoIChlcnIpIHtcbiAgICAgICAgICAgICAgICAgICAgc3VjY2VlZGVkID0gZmFsc2U7XG4gICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgdGhpcy5oYW5kbGVSZXN1bHQoc3VjY2VlZGVkKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnaGFuZGxlUmVzdWx0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBoYW5kbGVSZXN1bHQoc3VjY2VlZGVkKSB7XG4gICAgICAgICAgICAgICAgdGhpcy5lbWl0dGVyLmVtaXQoc3VjY2VlZGVkID8gJ3N1Y2Nlc3MnIDogJ2Vycm9yJywge1xuICAgICAgICAgICAgICAgICAgICBhY3Rpb246IHRoaXMuYWN0aW9uLFxuICAgICAgICAgICAgICAgICAgICB0ZXh0OiB0aGlzLnNlbGVjdGVkVGV4dCxcbiAgICAgICAgICAgICAgICAgICAgdHJpZ2dlcjogdGhpcy50cmlnZ2VyLFxuICAgICAgICAgICAgICAgICAgICBjbGVhclNlbGVjdGlvbjogdGhpcy5jbGVhclNlbGVjdGlvbi5iaW5kKHRoaXMpXG4gICAgICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2NsZWFyU2VsZWN0aW9uJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBjbGVhclNlbGVjdGlvbigpIHtcbiAgICAgICAgICAgICAgICBpZiAodGhpcy50YXJnZXQpIHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy50YXJnZXQuYmx1cigpO1xuICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgIHdpbmRvdy5nZXRTZWxlY3Rpb24oKS5yZW1vdmVBbGxSYW5nZXMoKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnZGVzdHJveScsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVzdHJveSgpIHtcbiAgICAgICAgICAgICAgICB0aGlzLnJlbW92ZUZha2UoKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnYWN0aW9uJyxcbiAgICAgICAgICAgIHNldDogZnVuY3Rpb24gc2V0KCkge1xuICAgICAgICAgICAgICAgIHZhciBhY3Rpb24gPSBhcmd1bWVudHMubGVuZ3RoID4gMCAmJiBhcmd1bWVudHNbMF0gIT09IHVuZGVmaW5lZCA/IGFyZ3VtZW50c1swXSA6ICdjb3B5JztcblxuICAgICAgICAgICAgICAgIHRoaXMuX2FjdGlvbiA9IGFjdGlvbjtcblxuICAgICAgICAgICAgICAgIGlmICh0aGlzLl9hY3Rpb24gIT09ICdjb3B5JyAmJiB0aGlzLl9hY3Rpb24gIT09ICdjdXQnKSB7XG4gICAgICAgICAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignSW52YWxpZCBcImFjdGlvblwiIHZhbHVlLCB1c2UgZWl0aGVyIFwiY29weVwiIG9yIFwiY3V0XCInKTtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMuX2FjdGlvbjtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAndGFyZ2V0JyxcbiAgICAgICAgICAgIHNldDogZnVuY3Rpb24gc2V0KHRhcmdldCkge1xuICAgICAgICAgICAgICAgIGlmICh0YXJnZXQgIT09IHVuZGVmaW5lZCkge1xuICAgICAgICAgICAgICAgICAgICBpZiAodGFyZ2V0ICYmICh0eXBlb2YgdGFyZ2V0ID09PSAndW5kZWZpbmVkJyA/ICd1bmRlZmluZWQnIDogX3R5cGVvZih0YXJnZXQpKSA9PT0gJ29iamVjdCcgJiYgdGFyZ2V0Lm5vZGVUeXBlID09PSAxKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICBpZiAodGhpcy5hY3Rpb24gPT09ICdjb3B5JyAmJiB0YXJnZXQuaGFzQXR0cmlidXRlKCdkaXNhYmxlZCcpKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwidGFyZ2V0XCIgYXR0cmlidXRlLiBQbGVhc2UgdXNlIFwicmVhZG9ubHlcIiBpbnN0ZWFkIG9mIFwiZGlzYWJsZWRcIiBhdHRyaWJ1dGUnKTtcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgICAgICAgICAgaWYgKHRoaXMuYWN0aW9uID09PSAnY3V0JyAmJiAodGFyZ2V0Lmhhc0F0dHJpYnV0ZSgncmVhZG9ubHknKSB8fCB0YXJnZXQuaGFzQXR0cmlidXRlKCdkaXNhYmxlZCcpKSkge1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignSW52YWxpZCBcInRhcmdldFwiIGF0dHJpYnV0ZS4gWW91IGNhblxcJ3QgY3V0IHRleHQgZnJvbSBlbGVtZW50cyB3aXRoIFwicmVhZG9ubHlcIiBvciBcImRpc2FibGVkXCIgYXR0cmlidXRlcycpO1xuICAgICAgICAgICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLl90YXJnZXQgPSB0YXJnZXQ7XG4gICAgICAgICAgICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJ0YXJnZXRcIiB2YWx1ZSwgdXNlIGEgdmFsaWQgRWxlbWVudCcpO1xuICAgICAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLl90YXJnZXQ7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1dKTtcblxuICAgICAgICByZXR1cm4gQ2xpcGJvYXJkQWN0aW9uO1xuICAgIH0oKTtcblxuICAgIG1vZHVsZS5leHBvcnRzID0gQ2xpcGJvYXJkQWN0aW9uO1xufSk7IiwiKGZ1bmN0aW9uIChnbG9iYWwsIGZhY3RvcnkpIHtcbiAgICBpZiAodHlwZW9mIGRlZmluZSA9PT0gXCJmdW5jdGlvblwiICYmIGRlZmluZS5hbWQpIHtcbiAgICAgICAgZGVmaW5lKFsnbW9kdWxlJywgJy4vY2xpcGJvYXJkLWFjdGlvbicsICd0aW55LWVtaXR0ZXInLCAnZ29vZC1saXN0ZW5lciddLCBmYWN0b3J5KTtcbiAgICB9IGVsc2UgaWYgKHR5cGVvZiBleHBvcnRzICE9PSBcInVuZGVmaW5lZFwiKSB7XG4gICAgICAgIGZhY3RvcnkobW9kdWxlLCByZXF1aXJlKCcuL2NsaXBib2FyZC1hY3Rpb24nKSwgcmVxdWlyZSgndGlueS1lbWl0dGVyJyksIHJlcXVpcmUoJ2dvb2QtbGlzdGVuZXInKSk7XG4gICAgfSBlbHNlIHtcbiAgICAgICAgdmFyIG1vZCA9IHtcbiAgICAgICAgICAgIGV4cG9ydHM6IHt9XG4gICAgICAgIH07XG4gICAgICAgIGZhY3RvcnkobW9kLCBnbG9iYWwuY2xpcGJvYXJkQWN0aW9uLCBnbG9iYWwudGlueUVtaXR0ZXIsIGdsb2JhbC5nb29kTGlzdGVuZXIpO1xuICAgICAgICBnbG9iYWwuY2xpcGJvYXJkID0gbW9kLmV4cG9ydHM7XG4gICAgfVxufSkodGhpcywgZnVuY3Rpb24gKG1vZHVsZSwgX2NsaXBib2FyZEFjdGlvbiwgX3RpbnlFbWl0dGVyLCBfZ29vZExpc3RlbmVyKSB7XG4gICAgJ3VzZSBzdHJpY3QnO1xuXG4gICAgdmFyIF9jbGlwYm9hcmRBY3Rpb24yID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfY2xpcGJvYXJkQWN0aW9uKTtcblxuICAgIHZhciBfdGlueUVtaXR0ZXIyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfdGlueUVtaXR0ZXIpO1xuXG4gICAgdmFyIF9nb29kTGlzdGVuZXIyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfZ29vZExpc3RlbmVyKTtcblxuICAgIGZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7XG4gICAgICAgIHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7XG4gICAgICAgICAgICBkZWZhdWx0OiBvYmpcbiAgICAgICAgfTtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBfY2xhc3NDYWxsQ2hlY2soaW5zdGFuY2UsIENvbnN0cnVjdG9yKSB7XG4gICAgICAgIGlmICghKGluc3RhbmNlIGluc3RhbmNlb2YgQ29uc3RydWN0b3IpKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKFwiQ2Fubm90IGNhbGwgYSBjbGFzcyBhcyBhIGZ1bmN0aW9uXCIpO1xuICAgICAgICB9XG4gICAgfVxuXG4gICAgdmFyIF9jcmVhdGVDbGFzcyA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgZnVuY3Rpb24gZGVmaW5lUHJvcGVydGllcyh0YXJnZXQsIHByb3BzKSB7XG4gICAgICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IHByb3BzLmxlbmd0aDsgaSsrKSB7XG4gICAgICAgICAgICAgICAgdmFyIGRlc2NyaXB0b3IgPSBwcm9wc1tpXTtcbiAgICAgICAgICAgICAgICBkZXNjcmlwdG9yLmVudW1lcmFibGUgPSBkZXNjcmlwdG9yLmVudW1lcmFibGUgfHwgZmFsc2U7XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5jb25maWd1cmFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIGlmIChcInZhbHVlXCIgaW4gZGVzY3JpcHRvcikgZGVzY3JpcHRvci53cml0YWJsZSA9IHRydWU7XG4gICAgICAgICAgICAgICAgT2JqZWN0LmRlZmluZVByb3BlcnR5KHRhcmdldCwgZGVzY3JpcHRvci5rZXksIGRlc2NyaXB0b3IpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGZ1bmN0aW9uIChDb25zdHJ1Y3RvciwgcHJvdG9Qcm9wcywgc3RhdGljUHJvcHMpIHtcbiAgICAgICAgICAgIGlmIChwcm90b1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLnByb3RvdHlwZSwgcHJvdG9Qcm9wcyk7XG4gICAgICAgICAgICBpZiAoc3RhdGljUHJvcHMpIGRlZmluZVByb3BlcnRpZXMoQ29uc3RydWN0b3IsIHN0YXRpY1Byb3BzKTtcbiAgICAgICAgICAgIHJldHVybiBDb25zdHJ1Y3RvcjtcbiAgICAgICAgfTtcbiAgICB9KCk7XG5cbiAgICBmdW5jdGlvbiBfcG9zc2libGVDb25zdHJ1Y3RvclJldHVybihzZWxmLCBjYWxsKSB7XG4gICAgICAgIGlmICghc2VsZikge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFJlZmVyZW5jZUVycm9yKFwidGhpcyBoYXNuJ3QgYmVlbiBpbml0aWFsaXNlZCAtIHN1cGVyKCkgaGFzbid0IGJlZW4gY2FsbGVkXCIpO1xuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGNhbGwgJiYgKHR5cGVvZiBjYWxsID09PSBcIm9iamVjdFwiIHx8IHR5cGVvZiBjYWxsID09PSBcImZ1bmN0aW9uXCIpID8gY2FsbCA6IHNlbGY7XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gX2luaGVyaXRzKHN1YkNsYXNzLCBzdXBlckNsYXNzKSB7XG4gICAgICAgIGlmICh0eXBlb2Ygc3VwZXJDbGFzcyAhPT0gXCJmdW5jdGlvblwiICYmIHN1cGVyQ2xhc3MgIT09IG51bGwpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoXCJTdXBlciBleHByZXNzaW9uIG11c3QgZWl0aGVyIGJlIG51bGwgb3IgYSBmdW5jdGlvbiwgbm90IFwiICsgdHlwZW9mIHN1cGVyQ2xhc3MpO1xuICAgICAgICB9XG5cbiAgICAgICAgc3ViQ2xhc3MucHJvdG90eXBlID0gT2JqZWN0LmNyZWF0ZShzdXBlckNsYXNzICYmIHN1cGVyQ2xhc3MucHJvdG90eXBlLCB7XG4gICAgICAgICAgICBjb25zdHJ1Y3Rvcjoge1xuICAgICAgICAgICAgICAgIHZhbHVlOiBzdWJDbGFzcyxcbiAgICAgICAgICAgICAgICBlbnVtZXJhYmxlOiBmYWxzZSxcbiAgICAgICAgICAgICAgICB3cml0YWJsZTogdHJ1ZSxcbiAgICAgICAgICAgICAgICBjb25maWd1cmFibGU6IHRydWVcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICAgIGlmIChzdXBlckNsYXNzKSBPYmplY3Quc2V0UHJvdG90eXBlT2YgPyBPYmplY3Quc2V0UHJvdG90eXBlT2Yoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIDogc3ViQ2xhc3MuX19wcm90b19fID0gc3VwZXJDbGFzcztcbiAgICB9XG5cbiAgICB2YXIgQ2xpcGJvYXJkID0gZnVuY3Rpb24gKF9FbWl0dGVyKSB7XG4gICAgICAgIF9pbmhlcml0cyhDbGlwYm9hcmQsIF9FbWl0dGVyKTtcblxuICAgICAgICAvKipcbiAgICAgICAgICogQHBhcmFtIHtTdHJpbmd8SFRNTEVsZW1lbnR8SFRNTENvbGxlY3Rpb258Tm9kZUxpc3R9IHRyaWdnZXJcbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG4gICAgICAgIGZ1bmN0aW9uIENsaXBib2FyZCh0cmlnZ2VyLCBvcHRpb25zKSB7XG4gICAgICAgICAgICBfY2xhc3NDYWxsQ2hlY2sodGhpcywgQ2xpcGJvYXJkKTtcblxuICAgICAgICAgICAgdmFyIF90aGlzID0gX3Bvc3NpYmxlQ29uc3RydWN0b3JSZXR1cm4odGhpcywgKENsaXBib2FyZC5fX3Byb3RvX18gfHwgT2JqZWN0LmdldFByb3RvdHlwZU9mKENsaXBib2FyZCkpLmNhbGwodGhpcykpO1xuXG4gICAgICAgICAgICBfdGhpcy5yZXNvbHZlT3B0aW9ucyhvcHRpb25zKTtcbiAgICAgICAgICAgIF90aGlzLmxpc3RlbkNsaWNrKHRyaWdnZXIpO1xuICAgICAgICAgICAgcmV0dXJuIF90aGlzO1xuICAgICAgICB9XG5cbiAgICAgICAgLyoqXG4gICAgICAgICAqIERlZmluZXMgaWYgYXR0cmlidXRlcyB3b3VsZCBiZSByZXNvbHZlZCB1c2luZyBpbnRlcm5hbCBzZXR0ZXIgZnVuY3Rpb25zXG4gICAgICAgICAqIG9yIGN1c3RvbSBmdW5jdGlvbnMgdGhhdCB3ZXJlIHBhc3NlZCBpbiB0aGUgY29uc3RydWN0b3IuXG4gICAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvcHRpb25zXG4gICAgICAgICAqL1xuXG5cbiAgICAgICAgX2NyZWF0ZUNsYXNzKENsaXBib2FyZCwgW3tcbiAgICAgICAgICAgIGtleTogJ3Jlc29sdmVPcHRpb25zJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiByZXNvbHZlT3B0aW9ucygpIHtcbiAgICAgICAgICAgICAgICB2YXIgb3B0aW9ucyA9IGFyZ3VtZW50cy5sZW5ndGggPiAwICYmIGFyZ3VtZW50c1swXSAhPT0gdW5kZWZpbmVkID8gYXJndW1lbnRzWzBdIDoge307XG5cbiAgICAgICAgICAgICAgICB0aGlzLmFjdGlvbiA9IHR5cGVvZiBvcHRpb25zLmFjdGlvbiA9PT0gJ2Z1bmN0aW9uJyA/IG9wdGlvbnMuYWN0aW9uIDogdGhpcy5kZWZhdWx0QWN0aW9uO1xuICAgICAgICAgICAgICAgIHRoaXMudGFyZ2V0ID0gdHlwZW9mIG9wdGlvbnMudGFyZ2V0ID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy50YXJnZXQgOiB0aGlzLmRlZmF1bHRUYXJnZXQ7XG4gICAgICAgICAgICAgICAgdGhpcy50ZXh0ID0gdHlwZW9mIG9wdGlvbnMudGV4dCA9PT0gJ2Z1bmN0aW9uJyA/IG9wdGlvbnMudGV4dCA6IHRoaXMuZGVmYXVsdFRleHQ7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2xpc3RlbkNsaWNrJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBsaXN0ZW5DbGljayh0cmlnZ2VyKSB7XG4gICAgICAgICAgICAgICAgdmFyIF90aGlzMiA9IHRoaXM7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmxpc3RlbmVyID0gKDAsIF9nb29kTGlzdGVuZXIyLmRlZmF1bHQpKHRyaWdnZXIsICdjbGljaycsIGZ1bmN0aW9uIChlKSB7XG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBfdGhpczIub25DbGljayhlKTtcbiAgICAgICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnb25DbGljaycsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gb25DbGljayhlKSB7XG4gICAgICAgICAgICAgICAgdmFyIHRyaWdnZXIgPSBlLmRlbGVnYXRlVGFyZ2V0IHx8IGUuY3VycmVudFRhcmdldDtcblxuICAgICAgICAgICAgICAgIGlmICh0aGlzLmNsaXBib2FyZEFjdGlvbikge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmNsaXBib2FyZEFjdGlvbiA9IG51bGw7XG4gICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24gPSBuZXcgX2NsaXBib2FyZEFjdGlvbjIuZGVmYXVsdCh7XG4gICAgICAgICAgICAgICAgICAgIGFjdGlvbjogdGhpcy5hY3Rpb24odHJpZ2dlciksXG4gICAgICAgICAgICAgICAgICAgIHRhcmdldDogdGhpcy50YXJnZXQodHJpZ2dlciksXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6IHRoaXMudGV4dCh0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdHJpZ2dlcjogdHJpZ2dlcixcbiAgICAgICAgICAgICAgICAgICAgZW1pdHRlcjogdGhpc1xuICAgICAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZWZhdWx0QWN0aW9uJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZWZhdWx0QWN0aW9uKHRyaWdnZXIpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gZ2V0QXR0cmlidXRlVmFsdWUoJ2FjdGlvbicsIHRyaWdnZXIpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZWZhdWx0VGFyZ2V0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZWZhdWx0VGFyZ2V0KHRyaWdnZXIpIHtcbiAgICAgICAgICAgICAgICB2YXIgc2VsZWN0b3IgPSBnZXRBdHRyaWJ1dGVWYWx1ZSgndGFyZ2V0JywgdHJpZ2dlcik7XG5cbiAgICAgICAgICAgICAgICBpZiAoc2VsZWN0b3IpIHtcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGRvY3VtZW50LnF1ZXJ5U2VsZWN0b3Ioc2VsZWN0b3IpO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnZGVmYXVsdFRleHQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRUZXh0KHRyaWdnZXIpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gZ2V0QXR0cmlidXRlVmFsdWUoJ3RleHQnLCB0cmlnZ2VyKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnZGVzdHJveScsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVzdHJveSgpIHtcbiAgICAgICAgICAgICAgICB0aGlzLmxpc3RlbmVyLmRlc3Ryb3koKTtcblxuICAgICAgICAgICAgICAgIGlmICh0aGlzLmNsaXBib2FyZEFjdGlvbikge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmNsaXBib2FyZEFjdGlvbi5kZXN0cm95KCk7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH1dKTtcblxuICAgICAgICByZXR1cm4gQ2xpcGJvYXJkO1xuICAgIH0oX3RpbnlFbWl0dGVyMi5kZWZhdWx0KTtcblxuICAgIC8qKlxuICAgICAqIEhlbHBlciBmdW5jdGlvbiB0byByZXRyaWV2ZSBhdHRyaWJ1dGUgdmFsdWUuXG4gICAgICogQHBhcmFtIHtTdHJpbmd9IHN1ZmZpeFxuICAgICAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICAgICAqL1xuICAgIGZ1bmN0aW9uIGdldEF0dHJpYnV0ZVZhbHVlKHN1ZmZpeCwgZWxlbWVudCkge1xuICAgICAgICB2YXIgYXR0cmlidXRlID0gJ2RhdGEtY2xpcGJvYXJkLScgKyBzdWZmaXg7XG5cbiAgICAgICAgaWYgKCFlbGVtZW50Lmhhc0F0dHJpYnV0ZShhdHRyaWJ1dGUpKSB7XG4gICAgICAgICAgICByZXR1cm47XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gZWxlbWVudC5nZXRBdHRyaWJ1dGUoYXR0cmlidXRlKTtcbiAgICB9XG5cbiAgICBtb2R1bGUuZXhwb3J0cyA9IENsaXBib2FyZDtcbn0pOyIsIi8qKlxuICogQSBwb2x5ZmlsbCBmb3IgRWxlbWVudC5tYXRjaGVzKClcbiAqL1xuaWYgKEVsZW1lbnQgJiYgIUVsZW1lbnQucHJvdG90eXBlLm1hdGNoZXMpIHtcbiAgICB2YXIgcHJvdG8gPSBFbGVtZW50LnByb3RvdHlwZTtcblxuICAgIHByb3RvLm1hdGNoZXMgPSBwcm90by5tYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ubW96TWF0Y2hlc1NlbGVjdG9yIHx8XG4gICAgICAgICAgICAgICAgICAgIHByb3RvLm1zTWF0Y2hlc1NlbGVjdG9yIHx8XG4gICAgICAgICAgICAgICAgICAgIHByb3RvLm9NYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ud2Via2l0TWF0Y2hlc1NlbGVjdG9yO1xufVxuXG4vKipcbiAqIEZpbmRzIHRoZSBjbG9zZXN0IHBhcmVudCB0aGF0IG1hdGNoZXMgYSBzZWxlY3Rvci5cbiAqXG4gKiBAcGFyYW0ge0VsZW1lbnR9IGVsZW1lbnRcbiAqIEBwYXJhbSB7U3RyaW5nfSBzZWxlY3RvclxuICogQHJldHVybiB7RnVuY3Rpb259XG4gKi9cbmZ1bmN0aW9uIGNsb3Nlc3QgKGVsZW1lbnQsIHNlbGVjdG9yKSB7XG4gICAgd2hpbGUgKGVsZW1lbnQgJiYgZWxlbWVudCAhPT0gZG9jdW1lbnQpIHtcbiAgICAgICAgaWYgKGVsZW1lbnQubWF0Y2hlcyhzZWxlY3RvcikpIHJldHVybiBlbGVtZW50O1xuICAgICAgICBlbGVtZW50ID0gZWxlbWVudC5wYXJlbnROb2RlO1xuICAgIH1cbn1cblxubW9kdWxlLmV4cG9ydHMgPSBjbG9zZXN0O1xuIiwidmFyIGNsb3Nlc3QgPSByZXF1aXJlKCcuL2Nsb3Nlc3QnKTtcblxuLyoqXG4gKiBEZWxlZ2F0ZXMgZXZlbnQgdG8gYSBzZWxlY3Rvci5cbiAqXG4gKiBAcGFyYW0ge0VsZW1lbnR9IGVsZW1lbnRcbiAqIEBwYXJhbSB7U3RyaW5nfSBzZWxlY3RvclxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcGFyYW0ge0Jvb2xlYW59IHVzZUNhcHR1cmVcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gZGVsZWdhdGUoZWxlbWVudCwgc2VsZWN0b3IsIHR5cGUsIGNhbGxiYWNrLCB1c2VDYXB0dXJlKSB7XG4gICAgdmFyIGxpc3RlbmVyRm4gPSBsaXN0ZW5lci5hcHBseSh0aGlzLCBhcmd1bWVudHMpO1xuXG4gICAgZWxlbWVudC5hZGRFdmVudExpc3RlbmVyKHR5cGUsIGxpc3RlbmVyRm4sIHVzZUNhcHR1cmUpO1xuXG4gICAgcmV0dXJuIHtcbiAgICAgICAgZGVzdHJveTogZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICBlbGVtZW50LnJlbW92ZUV2ZW50TGlzdGVuZXIodHlwZSwgbGlzdGVuZXJGbiwgdXNlQ2FwdHVyZSk7XG4gICAgICAgIH1cbiAgICB9XG59XG5cbi8qKlxuICogRmluZHMgY2xvc2VzdCBtYXRjaCBhbmQgaW52b2tlcyBjYWxsYmFjay5cbiAqXG4gKiBAcGFyYW0ge0VsZW1lbnR9IGVsZW1lbnRcbiAqIEBwYXJhbSB7U3RyaW5nfSBzZWxlY3RvclxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtGdW5jdGlvbn1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuZXIoZWxlbWVudCwgc2VsZWN0b3IsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uKGUpIHtcbiAgICAgICAgZS5kZWxlZ2F0ZVRhcmdldCA9IGNsb3Nlc3QoZS50YXJnZXQsIHNlbGVjdG9yKTtcblxuICAgICAgICBpZiAoZS5kZWxlZ2F0ZVRhcmdldCkge1xuICAgICAgICAgICAgY2FsbGJhY2suY2FsbChlbGVtZW50LCBlKTtcbiAgICAgICAgfVxuICAgIH1cbn1cblxubW9kdWxlLmV4cG9ydHMgPSBkZWxlZ2F0ZTtcbiIsIi8qKlxuICogQ2hlY2sgaWYgYXJndW1lbnQgaXMgYSBIVE1MIGVsZW1lbnQuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLm5vZGUgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHJldHVybiB2YWx1ZSAhPT0gdW5kZWZpbmVkXG4gICAgICAgICYmIHZhbHVlIGluc3RhbmNlb2YgSFRNTEVsZW1lbnRcbiAgICAgICAgJiYgdmFsdWUubm9kZVR5cGUgPT09IDE7XG59O1xuXG4vKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgbGlzdCBvZiBIVE1MIGVsZW1lbnRzLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5ub2RlTGlzdCA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgdmFyIHR5cGUgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nLmNhbGwodmFsdWUpO1xuXG4gICAgcmV0dXJuIHZhbHVlICE9PSB1bmRlZmluZWRcbiAgICAgICAgJiYgKHR5cGUgPT09ICdbb2JqZWN0IE5vZGVMaXN0XScgfHwgdHlwZSA9PT0gJ1tvYmplY3QgSFRNTENvbGxlY3Rpb25dJylcbiAgICAgICAgJiYgKCdsZW5ndGgnIGluIHZhbHVlKVxuICAgICAgICAmJiAodmFsdWUubGVuZ3RoID09PSAwIHx8IGV4cG9ydHMubm9kZSh2YWx1ZVswXSkpO1xufTtcblxuLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIHN0cmluZy5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gdmFsdWVcbiAqIEByZXR1cm4ge0Jvb2xlYW59XG4gKi9cbmV4cG9ydHMuc3RyaW5nID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICByZXR1cm4gdHlwZW9mIHZhbHVlID09PSAnc3RyaW5nJ1xuICAgICAgICB8fCB2YWx1ZSBpbnN0YW5jZW9mIFN0cmluZztcbn07XG5cbi8qKlxuICogQ2hlY2sgaWYgYXJndW1lbnQgaXMgYSBmdW5jdGlvbi5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gdmFsdWVcbiAqIEByZXR1cm4ge0Jvb2xlYW59XG4gKi9cbmV4cG9ydHMuZm4gPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHZhciB0eXBlID0gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZy5jYWxsKHZhbHVlKTtcblxuICAgIHJldHVybiB0eXBlID09PSAnW29iamVjdCBGdW5jdGlvbl0nO1xufTtcbiIsInZhciBpcyA9IHJlcXVpcmUoJy4vaXMnKTtcbnZhciBkZWxlZ2F0ZSA9IHJlcXVpcmUoJ2RlbGVnYXRlJyk7XG5cbi8qKlxuICogVmFsaWRhdGVzIGFsbCBwYXJhbXMgYW5kIGNhbGxzIHRoZSByaWdodFxuICogbGlzdGVuZXIgZnVuY3Rpb24gYmFzZWQgb24gaXRzIHRhcmdldCB0eXBlLlxuICpcbiAqIEBwYXJhbSB7U3RyaW5nfEhUTUxFbGVtZW50fEhUTUxDb2xsZWN0aW9ufE5vZGVMaXN0fSB0YXJnZXRcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW4odGFyZ2V0LCB0eXBlLCBjYWxsYmFjaykge1xuICAgIGlmICghdGFyZ2V0ICYmICF0eXBlICYmICFjYWxsYmFjaykge1xuICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ01pc3NpbmcgcmVxdWlyZWQgYXJndW1lbnRzJyk7XG4gICAgfVxuXG4gICAgaWYgKCFpcy5zdHJpbmcodHlwZSkpIHtcbiAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcignU2Vjb25kIGFyZ3VtZW50IG11c3QgYmUgYSBTdHJpbmcnKTtcbiAgICB9XG5cbiAgICBpZiAoIWlzLmZuKGNhbGxiYWNrKSkge1xuICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdUaGlyZCBhcmd1bWVudCBtdXN0IGJlIGEgRnVuY3Rpb24nKTtcbiAgICB9XG5cbiAgICBpZiAoaXMubm9kZSh0YXJnZXQpKSB7XG4gICAgICAgIHJldHVybiBsaXN0ZW5Ob2RlKHRhcmdldCwgdHlwZSwgY2FsbGJhY2spO1xuICAgIH1cbiAgICBlbHNlIGlmIChpcy5ub2RlTGlzdCh0YXJnZXQpKSB7XG4gICAgICAgIHJldHVybiBsaXN0ZW5Ob2RlTGlzdCh0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKTtcbiAgICB9XG4gICAgZWxzZSBpZiAoaXMuc3RyaW5nKHRhcmdldCkpIHtcbiAgICAgICAgcmV0dXJuIGxpc3RlblNlbGVjdG9yKHRhcmdldCwgdHlwZSwgY2FsbGJhY2spO1xuICAgIH1cbiAgICBlbHNlIHtcbiAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcignRmlyc3QgYXJndW1lbnQgbXVzdCBiZSBhIFN0cmluZywgSFRNTEVsZW1lbnQsIEhUTUxDb2xsZWN0aW9uLCBvciBOb2RlTGlzdCcpO1xuICAgIH1cbn1cblxuLyoqXG4gKiBBZGRzIGFuIGV2ZW50IGxpc3RlbmVyIHRvIGEgSFRNTCBlbGVtZW50XG4gKiBhbmQgcmV0dXJucyBhIHJlbW92ZSBsaXN0ZW5lciBmdW5jdGlvbi5cbiAqXG4gKiBAcGFyYW0ge0hUTUxFbGVtZW50fSBub2RlXG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuTm9kZShub2RlLCB0eXBlLCBjYWxsYmFjaykge1xuICAgIG5vZGUuYWRkRXZlbnRMaXN0ZW5lcih0eXBlLCBjYWxsYmFjayk7XG5cbiAgICByZXR1cm4ge1xuICAgICAgICBkZXN0cm95OiBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIG5vZGUucmVtb3ZlRXZlbnRMaXN0ZW5lcih0eXBlLCBjYWxsYmFjayk7XG4gICAgICAgIH1cbiAgICB9XG59XG5cbi8qKlxuICogQWRkIGFuIGV2ZW50IGxpc3RlbmVyIHRvIGEgbGlzdCBvZiBIVE1MIGVsZW1lbnRzXG4gKiBhbmQgcmV0dXJucyBhIHJlbW92ZSBsaXN0ZW5lciBmdW5jdGlvbi5cbiAqXG4gKiBAcGFyYW0ge05vZGVMaXN0fEhUTUxDb2xsZWN0aW9ufSBub2RlTGlzdFxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3Rlbk5vZGVMaXN0KG5vZGVMaXN0LCB0eXBlLCBjYWxsYmFjaykge1xuICAgIEFycmF5LnByb3RvdHlwZS5mb3JFYWNoLmNhbGwobm9kZUxpc3QsIGZ1bmN0aW9uKG5vZGUpIHtcbiAgICAgICAgbm9kZS5hZGRFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcbiAgICB9KTtcblxuICAgIHJldHVybiB7XG4gICAgICAgIGRlc3Ryb3k6IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgQXJyYXkucHJvdG90eXBlLmZvckVhY2guY2FsbChub2RlTGlzdCwgZnVuY3Rpb24obm9kZSkge1xuICAgICAgICAgICAgICAgIG5vZGUucmVtb3ZlRXZlbnRMaXN0ZW5lcih0eXBlLCBjYWxsYmFjayk7XG4gICAgICAgICAgICB9KTtcbiAgICAgICAgfVxuICAgIH1cbn1cblxuLyoqXG4gKiBBZGQgYW4gZXZlbnQgbGlzdGVuZXIgdG8gYSBzZWxlY3RvclxuICogYW5kIHJldHVybnMgYSByZW1vdmUgbGlzdGVuZXIgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuU2VsZWN0b3Ioc2VsZWN0b3IsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgcmV0dXJuIGRlbGVnYXRlKGRvY3VtZW50LmJvZHksIHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjayk7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gbGlzdGVuO1xuIiwiLyohIG5nY2xpcGJvYXJkIC0gdjEuMS4xIC0gMjAxNi0wMi0yNlxyXG4qIGh0dHBzOi8vZ2l0aHViLmNvbS9zYWNoaW5jaG9vbHVyL25nY2xpcGJvYXJkXHJcbiogQ29weXJpZ2h0IChjKSAyMDE2IFNhY2hpbjsgTGljZW5zZWQgTUlUICovXHJcbihmdW5jdGlvbigpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciBNT0RVTEVfTkFNRSA9ICduZ2NsaXBib2FyZCc7XHJcbiAgICB2YXIgYW5ndWxhciwgQ2xpcGJvYXJkO1xyXG4gICAgXHJcbiAgICAvLyBDaGVjayBmb3IgQ29tbW9uSlMgc3VwcG9ydFxyXG4gICAgaWYgKHR5cGVvZiBtb2R1bGUgPT09ICdvYmplY3QnICYmIG1vZHVsZS5leHBvcnRzKSB7XHJcbiAgICAgIGFuZ3VsYXIgPSByZXF1aXJlKCdhbmd1bGFyJyk7XHJcbiAgICAgIENsaXBib2FyZCA9IHJlcXVpcmUoJ2NsaXBib2FyZCcpO1xyXG4gICAgICBtb2R1bGUuZXhwb3J0cyA9IE1PRFVMRV9OQU1FO1xyXG4gICAgfSBlbHNlIHtcclxuICAgICAgYW5ndWxhciA9IHdpbmRvdy5hbmd1bGFyO1xyXG4gICAgICBDbGlwYm9hcmQgPSB3aW5kb3cuQ2xpcGJvYXJkO1xyXG4gICAgfVxyXG5cclxuICAgIGFuZ3VsYXIubW9kdWxlKE1PRFVMRV9OQU1FLCBbXSkuZGlyZWN0aXZlKCduZ2NsaXBib2FyZCcsIGZ1bmN0aW9uKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIHJlc3RyaWN0OiAnQScsXHJcbiAgICAgICAgICAgIHNjb3BlOiB7XHJcbiAgICAgICAgICAgICAgICBuZ2NsaXBib2FyZFN1Y2Nlc3M6ICcmJyxcclxuICAgICAgICAgICAgICAgIG5nY2xpcGJvYXJkRXJyb3I6ICcmJ1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBsaW5rOiBmdW5jdGlvbihzY29wZSwgZWxlbWVudCkge1xyXG4gICAgICAgICAgICAgICAgdmFyIGNsaXBib2FyZCA9IG5ldyBDbGlwYm9hcmQoZWxlbWVudFswXSk7XHJcblxyXG4gICAgICAgICAgICAgICAgY2xpcGJvYXJkLm9uKCdzdWNjZXNzJywgZnVuY3Rpb24oZSkge1xyXG4gICAgICAgICAgICAgICAgICBzY29wZS4kYXBwbHkoZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNjb3BlLm5nY2xpcGJvYXJkU3VjY2Vzcyh7XHJcbiAgICAgICAgICAgICAgICAgICAgICBlOiBlXHJcbiAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAgICAgY2xpcGJvYXJkLm9uKCdlcnJvcicsIGZ1bmN0aW9uKGUpIHtcclxuICAgICAgICAgICAgICAgICAgc2NvcGUuJGFwcGx5KGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICBzY29wZS5uZ2NsaXBib2FyZEVycm9yKHtcclxuICAgICAgICAgICAgICAgICAgICAgIGU6IGVcclxuICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfSk7XHJcbn0oKSk7XHJcbiIsImZ1bmN0aW9uIHNlbGVjdChlbGVtZW50KSB7XG4gICAgdmFyIHNlbGVjdGVkVGV4dDtcblxuICAgIGlmIChlbGVtZW50Lm5vZGVOYW1lID09PSAnU0VMRUNUJykge1xuICAgICAgICBlbGVtZW50LmZvY3VzKCk7XG5cbiAgICAgICAgc2VsZWN0ZWRUZXh0ID0gZWxlbWVudC52YWx1ZTtcbiAgICB9XG4gICAgZWxzZSBpZiAoZWxlbWVudC5ub2RlTmFtZSA9PT0gJ0lOUFVUJyB8fCBlbGVtZW50Lm5vZGVOYW1lID09PSAnVEVYVEFSRUEnKSB7XG4gICAgICAgIGVsZW1lbnQuZm9jdXMoKTtcbiAgICAgICAgZWxlbWVudC5zZXRTZWxlY3Rpb25SYW5nZSgwLCBlbGVtZW50LnZhbHVlLmxlbmd0aCk7XG5cbiAgICAgICAgc2VsZWN0ZWRUZXh0ID0gZWxlbWVudC52YWx1ZTtcbiAgICB9XG4gICAgZWxzZSB7XG4gICAgICAgIGlmIChlbGVtZW50Lmhhc0F0dHJpYnV0ZSgnY29udGVudGVkaXRhYmxlJykpIHtcbiAgICAgICAgICAgIGVsZW1lbnQuZm9jdXMoKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHZhciBzZWxlY3Rpb24gPSB3aW5kb3cuZ2V0U2VsZWN0aW9uKCk7XG4gICAgICAgIHZhciByYW5nZSA9IGRvY3VtZW50LmNyZWF0ZVJhbmdlKCk7XG5cbiAgICAgICAgcmFuZ2Uuc2VsZWN0Tm9kZUNvbnRlbnRzKGVsZW1lbnQpO1xuICAgICAgICBzZWxlY3Rpb24ucmVtb3ZlQWxsUmFuZ2VzKCk7XG4gICAgICAgIHNlbGVjdGlvbi5hZGRSYW5nZShyYW5nZSk7XG5cbiAgICAgICAgc2VsZWN0ZWRUZXh0ID0gc2VsZWN0aW9uLnRvU3RyaW5nKCk7XG4gICAgfVxuXG4gICAgcmV0dXJuIHNlbGVjdGVkVGV4dDtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBzZWxlY3Q7XG4iLCJmdW5jdGlvbiBFICgpIHtcbiAgLy8gS2VlcCB0aGlzIGVtcHR5IHNvIGl0J3MgZWFzaWVyIHRvIGluaGVyaXQgZnJvbVxuICAvLyAodmlhIGh0dHBzOi8vZ2l0aHViLmNvbS9saXBzbWFjayBmcm9tIGh0dHBzOi8vZ2l0aHViLmNvbS9zY290dGNvcmdhbi90aW55LWVtaXR0ZXIvaXNzdWVzLzMpXG59XG5cbkUucHJvdG90eXBlID0ge1xuICBvbjogZnVuY3Rpb24gKG5hbWUsIGNhbGxiYWNrLCBjdHgpIHtcbiAgICB2YXIgZSA9IHRoaXMuZSB8fCAodGhpcy5lID0ge30pO1xuXG4gICAgKGVbbmFtZV0gfHwgKGVbbmFtZV0gPSBbXSkpLnB1c2goe1xuICAgICAgZm46IGNhbGxiYWNrLFxuICAgICAgY3R4OiBjdHhcbiAgICB9KTtcblxuICAgIHJldHVybiB0aGlzO1xuICB9LFxuXG4gIG9uY2U6IGZ1bmN0aW9uIChuYW1lLCBjYWxsYmFjaywgY3R4KSB7XG4gICAgdmFyIHNlbGYgPSB0aGlzO1xuICAgIGZ1bmN0aW9uIGxpc3RlbmVyICgpIHtcbiAgICAgIHNlbGYub2ZmKG5hbWUsIGxpc3RlbmVyKTtcbiAgICAgIGNhbGxiYWNrLmFwcGx5KGN0eCwgYXJndW1lbnRzKTtcbiAgICB9O1xuXG4gICAgbGlzdGVuZXIuXyA9IGNhbGxiYWNrXG4gICAgcmV0dXJuIHRoaXMub24obmFtZSwgbGlzdGVuZXIsIGN0eCk7XG4gIH0sXG5cbiAgZW1pdDogZnVuY3Rpb24gKG5hbWUpIHtcbiAgICB2YXIgZGF0YSA9IFtdLnNsaWNlLmNhbGwoYXJndW1lbnRzLCAxKTtcbiAgICB2YXIgZXZ0QXJyID0gKCh0aGlzLmUgfHwgKHRoaXMuZSA9IHt9KSlbbmFtZV0gfHwgW10pLnNsaWNlKCk7XG4gICAgdmFyIGkgPSAwO1xuICAgIHZhciBsZW4gPSBldnRBcnIubGVuZ3RoO1xuXG4gICAgZm9yIChpOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgIGV2dEFycltpXS5mbi5hcHBseShldnRBcnJbaV0uY3R4LCBkYXRhKTtcbiAgICB9XG5cbiAgICByZXR1cm4gdGhpcztcbiAgfSxcblxuICBvZmY6IGZ1bmN0aW9uIChuYW1lLCBjYWxsYmFjaykge1xuICAgIHZhciBlID0gdGhpcy5lIHx8ICh0aGlzLmUgPSB7fSk7XG4gICAgdmFyIGV2dHMgPSBlW25hbWVdO1xuICAgIHZhciBsaXZlRXZlbnRzID0gW107XG5cbiAgICBpZiAoZXZ0cyAmJiBjYWxsYmFjaykge1xuICAgICAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IGV2dHMubGVuZ3RoOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgaWYgKGV2dHNbaV0uZm4gIT09IGNhbGxiYWNrICYmIGV2dHNbaV0uZm4uXyAhPT0gY2FsbGJhY2spXG4gICAgICAgICAgbGl2ZUV2ZW50cy5wdXNoKGV2dHNbaV0pO1xuICAgICAgfVxuICAgIH1cblxuICAgIC8vIFJlbW92ZSBldmVudCBmcm9tIHF1ZXVlIHRvIHByZXZlbnQgbWVtb3J5IGxlYWtcbiAgICAvLyBTdWdnZXN0ZWQgYnkgaHR0cHM6Ly9naXRodWIuY29tL2xhemRcbiAgICAvLyBSZWY6IGh0dHBzOi8vZ2l0aHViLmNvbS9zY290dGNvcmdhbi90aW55LWVtaXR0ZXIvY29tbWl0L2M2ZWJmYWE5YmM5NzNiMzNkMTEwYTg0YTMwNzc0MmI3Y2Y5NGM5NTMjY29tbWl0Y29tbWVudC01MDI0OTEwXG5cbiAgICAobGl2ZUV2ZW50cy5sZW5ndGgpXG4gICAgICA/IGVbbmFtZV0gPSBsaXZlRXZlbnRzXG4gICAgICA6IGRlbGV0ZSBlW25hbWVdO1xuXG4gICAgcmV0dXJuIHRoaXM7XG4gIH1cbn07XG5cbm1vZHVsZS5leHBvcnRzID0gRTtcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMjAvMjAxNS5cclxuICogVERTTSBpcyBhIGdsb2JhbCBvYmplY3QgdGhhdCBjb21lcyBmcm9tIEFwcC5qc1xyXG4gKlxyXG4gKiBUaGUgZm9sbG93aW5nIGhlbHBlciB3b3JrcyBpbiBhIHdheSB0byBtYWtlIGF2YWlsYWJsZSB0aGUgY3JlYXRpb24gb2YgRGlyZWN0aXZlLCBTZXJ2aWNlcyBhbmQgQ29udHJvbGxlclxyXG4gKiBvbiBmbHkgb3Igd2hlbiBkZXBsb3lpbmcgdGhlIGFwcC5cclxuICpcclxuICogV2UgcmVkdWNlIHRoZSB1c2Ugb2YgY29tcGlsZSBhbmQgZXh0cmEgc3RlcHNcclxuICovXHJcblxyXG52YXIgVERTVE0gPSByZXF1aXJlKCcuL0FwcC5qcycpO1xyXG5cclxuLyoqXHJcbiAqIExpc3RlbiB0byBhbiBleGlzdGluZyBkaWdlc3Qgb2YgdGhlIGNvbXBpbGUgcHJvdmlkZXIgYW5kIGV4ZWN1dGUgdGhlICRhcHBseSBpbW1lZGlhdGVseSBvciBhZnRlciBpdCdzIHJlYWR5XHJcbiAqIEBwYXJhbSBjdXJyZW50XHJcbiAqIEBwYXJhbSBmblxyXG4gKi9cclxuVERTVE0uc2FmZUFwcGx5ID0gZnVuY3Rpb24gKGN1cnJlbnQsIGZuKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICB2YXIgcGhhc2UgPSBjdXJyZW50LiRyb290LiQkcGhhc2U7XHJcbiAgICBpZiAocGhhc2UgPT09ICckYXBwbHknIHx8IHBoYXNlID09PSAnJGRpZ2VzdCcpIHtcclxuICAgICAgICBpZiAoZm4pIHtcclxuICAgICAgICAgICAgY3VycmVudC4kZXZhbChmbik7XHJcbiAgICAgICAgfVxyXG4gICAgfSBlbHNlIHtcclxuICAgICAgICBpZiAoZm4pIHtcclxuICAgICAgICAgICAgY3VycmVudC4kYXBwbHkoZm4pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGFwcGx5KCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3QgZGlyZWN0aXZlIGFzeW5jIGlmIHRoZSBjb21waWxlUHJvdmlkZXIgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVEaXJlY3RpdmUgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIpIHtcclxuICAgICAgICBURFNUTS5Qcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyLmRpcmVjdGl2ZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uZGlyZWN0aXZlKSB7XHJcbiAgICAgICAgVERTVE0uZGlyZWN0aXZlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3QgY29udHJvbGxlcnMgYXN5bmMgaWYgdGhlIGNvbnRyb2xsZXJQcm92aWRlciBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZUNvbnRyb2xsZXIgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5jb250cm9sbGVyUHJvdmlkZXIpIHtcclxuICAgICAgICBURFNUTS5jb250cm9sbGVyUHJvdmlkZXIucmVnaXN0ZXIoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmNvbnRyb2xsZXIpIHtcclxuICAgICAgICBURFNUTS5jb250cm9sbGVyKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3Qgc2VydmljZSBhc3luYyBpZiB0aGUgcHJvdmlkZVNlcnZpY2UgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVTZXJ2aWNlID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2UpIHtcclxuICAgICAgICBURFNUTS5Qcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2Uuc2VydmljZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uY29udHJvbGxlcikge1xyXG4gICAgICAgIFREU1RNLnNlcnZpY2Uoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogRm9yIExlZ2FjeSBzeXN0ZW0sIHdoYXQgaXMgZG9lcyBpcyB0byB0YWtlIHBhcmFtcyBmcm9tIHRoZSBxdWVyeVxyXG4gKiBvdXRzaWRlIHRoZSBBbmd1bGFySlMgdWktcm91dGluZy5cclxuICogQHBhcmFtIHBhcmFtIC8vIFBhcmFtIHRvIHNlYXJjIGZvciAvZXhhbXBsZS5odG1sP2Jhcj1mb28jY3VycmVudFN0YXRlXHJcbiAqL1xyXG5URFNUTS5nZXRVUkxQYXJhbSA9IGZ1bmN0aW9uIChwYXJhbSkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgJC51cmxQYXJhbSA9IGZ1bmN0aW9uIChuYW1lKSB7XHJcbiAgICAgICAgdmFyIHJlc3VsdHMgPSBuZXcgUmVnRXhwKCdbXFw/Jl0nICsgbmFtZSArICc9KFteJiNdKiknKS5leGVjKHdpbmRvdy5sb2NhdGlvbi5ocmVmKTtcclxuICAgICAgICBpZiAocmVzdWx0cyA9PT0gbnVsbCkge1xyXG4gICAgICAgICAgICByZXR1cm4gbnVsbDtcclxuICAgICAgICB9XHJcbiAgICAgICAgZWxzZSB7XHJcbiAgICAgICAgICAgIHJldHVybiByZXN1bHRzWzFdIHx8IDA7XHJcbiAgICAgICAgfVxyXG4gICAgfTtcclxuXHJcbiAgICByZXR1cm4gJC51cmxQYXJhbShwYXJhbSk7XHJcbn07XHJcblxyXG4vKipcclxuICogVGhpcyBjb2RlIHdhcyBpbnRyb2R1Y2VkIG9ubHkgZm9yIHRoZSBpZnJhbWUgbWlncmF0aW9uXHJcbiAqIGl0IGRldGVjdCB3aGVuIG1vdXNlIGVudGVyXHJcbiAqL1xyXG5URFNUTS5pZnJhbWVMb2FkZXIgPSBmdW5jdGlvbiAoKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICAkKCcuaWZyYW1lTG9hZGVyJykuaG92ZXIoXHJcbiAgICAgICAgZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAkKCcubmF2YmFyLXVsLWNvbnRhaW5lciAuZHJvcGRvd24ub3BlbicpLnJlbW92ZUNsYXNzKCdvcGVuJyk7XHJcbiAgICAgICAgfSwgZnVuY3Rpb24gKCkge1xyXG4gICAgICAgIH1cclxuICAgICk7XHJcbn07XHJcblxyXG5cclxuLy8gSXQgd2lsbCBiZSByZW1vdmVkIGFmdGVyIHdlIHJpcCBvZmYgYWxsIGlmcmFtZXNcclxud2luZG93LlREU1RNID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8xNi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnJlcXVpcmUoJ2FuZ3VsYXInKTtcclxucmVxdWlyZSgnYW5ndWxhci1hbmltYXRlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItbW9ja3MnKTtcclxucmVxdWlyZSgnYW5ndWxhci1zYW5pdGl6ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXJlc291cmNlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdHJhbnNsYXRlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdHJhbnNsYXRlLWxvYWRlci1wYXJ0aWFsJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdWktYm9vdHN0cmFwJyk7XHJcbnJlcXVpcmUoJ25nQ2xpcGJvYXJkJyk7XHJcbnJlcXVpcmUoJ3VpLXJvdXRlcicpO1xyXG5yZXF1aXJlKCdyeC1hbmd1bGFyJyk7XHJcbnJlcXVpcmUoJ2FwaS1jaGVjaycpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWZvcm1seScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWZvcm1seS10ZW1wbGF0ZXMtYm9vdHN0cmFwJyk7XHJcblxyXG4vLyBNb2R1bGVzXHJcbmltcG9ydCBIVFRQTW9kdWxlIGZyb20gJy4uL3NlcnZpY2VzL2h0dHAvSFRUUE1vZHVsZS5qcyc7XHJcbmltcG9ydCBSZXN0QVBJTW9kdWxlIGZyb20gJy4uL3NlcnZpY2VzL1Jlc3RBUEkvUmVzdEFQSU1vZHVsZS5qcydcclxuaW1wb3J0IEhlYWRlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2hlYWRlci9IZWFkZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTGljZW5zZUFkbWluTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZUFkbWluL0xpY2Vuc2VBZG1pbk1vZHVsZS5qcyc7XHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL0xpY2Vuc2VNYW5hZ2VyTW9kdWxlLmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL05vdGljZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy90YXNrTWFuYWdlci9UYXNrTWFuYWdlck1vZHVsZS5qcyc7XHJcblxyXG52YXIgUHJvdmlkZXJDb3JlID0ge307XHJcblxyXG52YXIgVERTVE0gPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0nLCBbXHJcbiAgICAnbmdTYW5pdGl6ZScsXHJcbiAgICAnbmdSZXNvdXJjZScsXHJcbiAgICAnbmdBbmltYXRlJyxcclxuICAgICdwYXNjYWxwcmVjaHQudHJhbnNsYXRlJywgLy8gJ2FuZ3VsYXItdHJhbnNsYXRlJ1xyXG4gICAgJ3VpLnJvdXRlcicsXHJcbiAgICAnbmdjbGlwYm9hcmQnLFxyXG4gICAgJ2tlbmRvLmRpcmVjdGl2ZXMnLFxyXG4gICAgJ3J4JyxcclxuICAgICdmb3JtbHknLFxyXG4gICAgJ2Zvcm1seUJvb3RzdHJhcCcsXHJcbiAgICAndWkuYm9vdHN0cmFwJyxcclxuICAgIEhUVFBNb2R1bGUubmFtZSxcclxuICAgIFJlc3RBUElNb2R1bGUubmFtZSxcclxuICAgIEhlYWRlck1vZHVsZS5uYW1lLFxyXG4gICAgVGFza01hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIExpY2Vuc2VBZG1pbk1vZHVsZS5uYW1lLFxyXG4gICAgTGljZW5zZU1hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIE5vdGljZU1hbmFnZXJNb2R1bGUubmFtZVxyXG5dKS5jb25maWcoW1xyXG4gICAgJyRsb2dQcm92aWRlcicsXHJcbiAgICAnJHJvb3RTY29wZVByb3ZpZGVyJyxcclxuICAgICckY29tcGlsZVByb3ZpZGVyJyxcclxuICAgICckY29udHJvbGxlclByb3ZpZGVyJyxcclxuICAgICckcHJvdmlkZScsXHJcbiAgICAnJGh0dHBQcm92aWRlcicsXHJcbiAgICAnJHRyYW5zbGF0ZVByb3ZpZGVyJyxcclxuICAgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgICckdXJsUm91dGVyUHJvdmlkZXInLFxyXG4gICAgJyRsb2NhdGlvblByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkbG9nUHJvdmlkZXIsICRyb290U2NvcGVQcm92aWRlciwgJGNvbXBpbGVQcm92aWRlciwgJGNvbnRyb2xsZXJQcm92aWRlciwgJHByb3ZpZGUsICRodHRwUHJvdmlkZXIsXHJcbiAgICAgICAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLCAkdXJsUm91dGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAgICAgJHJvb3RTY29wZVByb3ZpZGVyLmRpZ2VzdFR0bCgzMCk7XHJcblxyXG4gICAgICAgICRsb2dQcm92aWRlci5kZWJ1Z0VuYWJsZWQodHJ1ZSk7XHJcblxyXG4gICAgICAgIC8vIEFmdGVyIGJvb3RzdHJhcHBpbmcgYW5ndWxhciBmb3JnZXQgdGhlIHByb3ZpZGVyIHNpbmNlIGV2ZXJ5dGhpbmcgXCJ3YXMgYWxyZWFkeSBsb2FkZWRcIlxyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIgPSAkY29tcGlsZVByb3ZpZGVyO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5jb250cm9sbGVyUHJvdmlkZXIgPSAkY29udHJvbGxlclByb3ZpZGVyO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZSA9ICRwcm92aWRlO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5odHRwUHJvdmlkZXIgPSAkaHR0cFByb3ZpZGVyO1xyXG5cclxuICAgICAgICAvKipcclxuICAgICAgICAgKiBUcmFuc2xhdGlvbnNcclxuICAgICAgICAgKi9cclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnVzZVNhbml0aXplVmFsdWVTdHJhdGVneShudWxsKTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCd0ZHN0bScpO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIudXNlTG9hZGVyKCckdHJhbnNsYXRlUGFydGlhbExvYWRlcicsIHtcclxuICAgICAgICAgICAgdXJsVGVtcGxhdGU6ICcuLi9pMThuL3twYXJ0fS9hcHAuaTE4bi17bGFuZ30uanNvbidcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnByZWZlcnJlZExhbmd1YWdlKCdlbl9VUycpO1xyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci5mYWxsYmFja0xhbmd1YWdlKCdlbl9VUycpO1xyXG5cclxuICAgICAgICAvLyR1cmxSb3V0ZXJQcm92aWRlci5vdGhlcndpc2UoJ2Rhc2hib2FyZCcpO1xyXG5cclxuICAgIH1dKS5cclxuICAgIHJ1bihbJyRyb290U2NvcGUnLCAnJGh0dHAnLCAnJGxvZycsICckbG9jYXRpb24nLCBmdW5jdGlvbiAoJHJvb3RTY29wZSwgJGh0dHAsICRsb2csICRsb2NhdGlvbiwgJHN0YXRlLCAkc3RhdGVQYXJhbXMsICRsb2NhbGUpIHtcclxuICAgICAgICAkbG9nLmRlYnVnKCdDb25maWd1cmF0aW9uIGRlcGxveWVkJyk7XHJcblxyXG4gICAgICAgICRyb290U2NvcGUuJG9uKCckc3RhdGVDaGFuZ2VTdGFydCcsIGZ1bmN0aW9uIChldmVudCwgdG9TdGF0ZSwgdG9QYXJhbXMsIGZyb21TdGF0ZSwgZnJvbVBhcmFtcykge1xyXG4gICAgICAgICAgICAkbG9nLmRlYnVnKCdTdGF0ZSBDaGFuZ2UgdG8gJyArIHRvU3RhdGUubmFtZSk7XHJcbiAgICAgICAgICAgIGlmICh0b1N0YXRlLmRhdGEgJiYgdG9TdGF0ZS5kYXRhLnBhZ2UpIHtcclxuICAgICAgICAgICAgICAgIHdpbmRvdy5kb2N1bWVudC50aXRsZSA9IHRvU3RhdGUuZGF0YS5wYWdlLnRpdGxlO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgfV0pO1xyXG5cclxuLy8gd2UgbWFwcGVkIHRoZSBQcm92aWRlciBDb3JlIGxpc3QgKGNvbXBpbGVQcm92aWRlciwgY29udHJvbGxlclByb3ZpZGVyLCBwcm92aWRlU2VydmljZSwgaHR0cFByb3ZpZGVyKSB0byByZXVzZSBhZnRlciBvbiBmbHlcclxuVERTVE0uUHJvdmlkZXJDb3JlID0gUHJvdmlkZXJDb3JlO1xyXG5cclxubW9kdWxlLmV4cG9ydHMgPSBURFNUTTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzE0LzIwMTUuXHJcbiAqIEl0IGhhbmRsZXIgdGhlIGluZGV4IGZvciBhbnkgb2YgdGhlIGRpcmVjdGl2ZXMgYXZhaWxhYmxlXHJcbiAqL1xyXG5cclxucmVxdWlyZSgnLi90b29scy9Ub2FzdEhhbmRsZXIuanMnKTtcclxucmVxdWlyZSgnLi90b29scy9Nb2RhbFdpbmRvd0FjdGl2YXRpb24uanMnKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAzMC8xMC8yMDE2LlxyXG4gKiBMaXN0ZW4gdG8gTW9kYWwgV2luZG93IHRvIG1ha2UgYW55IG1vZGFsIHdpbmRvdyBkcmFnZ2FiYmxlXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG52YXIgVERTVE0gPSByZXF1aXJlKCcuLi8uLi9jb25maWcvQXBwLmpzJyk7XHJcblxyXG5URFNUTS5jcmVhdGVEaXJlY3RpdmUoJ21vZGFsUmVuZGVyJywgWyckbG9nJywgZnVuY3Rpb24gKCRsb2cpIHtcclxuICAgICRsb2cuZGVidWcoJ01vZGFsV2luZG93QWN0aXZhdGlvbiBsb2FkZWQnKTtcclxuICAgIHJldHVybiB7XHJcbiAgICAgICAgcmVzdHJpY3Q6ICdFQScsXHJcbiAgICAgICAgbGluazogZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgICAgICQoJy5tb2RhbC1kaWFsb2cnKS5kcmFnZ2FibGUoe1xyXG4gICAgICAgICAgICAgICAgaGFuZGxlOiAnLm1vZGFsLWhlYWRlcidcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfTtcclxufV0pOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTQvMjAxNS5cclxuICogUHJpbnRzIG91dCBhbGwgVG9hc3QgbWVzc2FnZSB3aGVuIGRldGVjdGVkIGZyb20gc2VydmVyIG9yIGN1c3RvbSBtc2cgdXNpbmcgdGhlIGRpcmVjdGl2ZSBpdHNlbGZcclxuICpcclxuICogUHJvYmFibHkgdmFsdWVzIGFyZTpcclxuICpcclxuICogc3VjY2VzcywgZGFuZ2VyLCBpbmZvLCB3YXJuaW5nXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG52YXIgVERTVE0gPSByZXF1aXJlKCcuLi8uLi9jb25maWcvQXBwLmpzJyk7XHJcblxyXG5URFNUTS5jcmVhdGVEaXJlY3RpdmUoJ3RvYXN0SGFuZGxlcicsIFsnJGxvZycsICckdGltZW91dCcsICdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicsICdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJyxcclxuICAgICdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InLCAnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLFxyXG4gICAgZnVuY3Rpb24gKCRsb2csICR0aW1lb3V0LCBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciwgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcixcclxuICAgICAgICAgICAgICBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IsIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yKSB7XHJcblxyXG4gICAgJGxvZy5kZWJ1ZygnVG9hc3RIYW5kbGVyIGxvYWRlZCcpO1xyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICBzY29wZToge1xyXG4gICAgICAgICAgICBtc2c6ICc9JyxcclxuICAgICAgICAgICAgdHlwZTogJz0nLFxyXG4gICAgICAgICAgICBzdGF0dXM6ICc9J1xyXG4gICAgICAgIH0sXHJcbiAgICAgICAgcHJpb3JpdHk6IDUsXHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvZGlyZWN0aXZlcy9Ub29scy9Ub2FzdEhhbmRsZXIuaHRtbCcsXHJcbiAgICAgICAgcmVzdHJpY3Q6ICdFJyxcclxuICAgICAgICBjb250cm9sbGVyOiBbJyRzY29wZScsICckcm9vdFNjb3BlJywgZnVuY3Rpb24gKCRzY29wZSwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgICAgICAkc2NvcGUuYWxlcnQgPSB7XHJcbiAgICAgICAgICAgICAgICBzdWNjZXNzOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGRhbmdlcjoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBpbmZvOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHdhcm5pbmc6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzID0ge1xyXG4gICAgICAgICAgICAgICAgc2hvdzogZmFsc2VcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIGZ1bmN0aW9uIHR1cm5PZmZOb3RpZmljYXRpb25zKCl7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuc3VjY2Vzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5pbmZvLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC53YXJuaW5nLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBMaXN0ZW4gdG8gYW55IHJlcXVlc3QsIHdlIGNhbiByZWdpc3RlciBsaXN0ZW5lciBpZiB3ZSB3YW50IHRvIGFkZCBleHRyYSBjb2RlLlxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVxdWVzdCgpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24oY29uZmlnKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgdG86ICcsICBjb25maWcpO1xyXG4gICAgICAgICAgICAgICAgdmFyIHRpbWUgPSBjb25maWcucmVxdWVzdFRpbWVzdGFtcDtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcodGltZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgZXJyb3I6ICcsICByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVzcG9uc2UoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlc3BvbnNlKXtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wIC0gcmVzcG9uc2UuY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdUaGUgcmVxdWVzdCB0b29rICcgKyAodGltZSAvIDEwMDApICsgJyBzZWNvbmRzJyk7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSByZXN1bHQ6ICcsIHJlc3BvbnNlKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuRXJyb3IoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlamVjdGlvbil7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSBlcnJvcjogJywgcmVqZWN0aW9uKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zdGF0dXMgPSByZWplY3Rpb24uc3RhdHVzO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zdGF0dXNUZXh0ID0gcmVqZWN0aW9uLnN0YXR1c1RleHQ7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLmVycm9ycyA9IHJlamVjdGlvbi5kYXRhLmVycm9ycztcclxuICAgICAgICAgICAgICAgICR0aW1lb3V0KHR1cm5PZmZOb3RpZmljYXRpb25zLCAzMDAwKTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogSGlkZSB0aGUgUG9wIHVwIG5vdGlmaWNhdGlvbiBtYW51YWxseVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHNjb3BlLm9uQ2FuY2VsUG9wVXAgPSBmdW5jdGlvbigpIHtcclxuICAgICAgICAgICAgICAgIHR1cm5PZmZOb3RpZmljYXRpb25zKCk7XHJcbiAgICAgICAgICAgIH07XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogSXQgd2F0Y2ggdGhlIHZhbHVlIHRvIHNob3cgdGhlIG1zZyBpZiBuZWNlc3NhcnlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRyb290U2NvcGUuJG9uKCdicm9hZGNhc3QtbXNnJywgZnVuY3Rpb24oZXZlbnQsIGFyZ3MpIHtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ2Jyb2FkY2FzdC1tc2cgZXhlY3V0ZWQnKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc3RhdHVzVGV4dCA9IGFyZ3MudGV4dDtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnN0YXR1cyA9IG51bGw7XHJcbiAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMjAwMCk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuJGFwcGx5KCk7IC8vIHJvb3RTY29wZSBhbmQgd2F0Y2ggZXhjbHVkZSB0aGUgYXBwbHkgYW5kIG5lZWRzIHRoZSBuZXh0IGN5Y2xlIHRvIHJ1blxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBJdCB3YXRjaCB0aGUgdmFsdWUgdG8gc2hvdyB0aGUgbXNnIGlmIG5lY2Vzc2FyeVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHNjb3BlLiR3YXRjaCgnbXNnJywgZnVuY3Rpb24obmV3VmFsdWUsIG9sZFZhbHVlKSB7XHJcbiAgICAgICAgICAgICAgICBpZiAobmV3VmFsdWUgJiYgbmV3VmFsdWUgIT09ICcnKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0WyRzY29wZS50eXBlXS5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnN0YXR1c1RleHQgPSBuZXdWYWx1ZTtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnN0YXR1cyA9ICRzY29wZS5zdGF0dXM7XHJcbiAgICAgICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsIDI1MDApO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgfV1cclxuICAgIH07XHJcbn1dKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8xNy8yMDE1LlxyXG4gKi9cclxuXHJcbi8vIE1haW4gQW5ndWxhckpzIGNvbmZpZ3VyYXRpb25cclxucmVxdWlyZSgnLi9jb25maWcvQXBwLmpzJyk7XHJcblxyXG4vLyBIZWxwZXJzXHJcbnJlcXVpcmUoJy4vY29uZmlnL0FuZ3VsYXJQcm92aWRlckhlbHBlci5qcycpO1xyXG5cclxuLy8gRGlyZWN0aXZlc1xyXG5yZXF1aXJlKCcuL2RpcmVjdGl2ZXMvaW5kZXgnKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIERpYWxvZ0FjdGlvbiB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMudGl0bGUgPSBwYXJhbXMudGl0bGU7XHJcbiAgICAgICAgdGhpcy5tZXNzYWdlID0gcGFyYW1zLm1lc3NhZ2U7XHJcblxyXG4gICAgfVxyXG4gICAgLyoqXHJcbiAgICAgKiBBY2NjZXB0IGFuZCBDb25maXJtXHJcbiAgICAgKi9cclxuICAgIGNvbmZpcm1BY3Rpb24oKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMi8yMDE1LlxyXG4gKiBIZWFkZXIgQ29udHJvbGxlciBtYW5hZ2UgdGhlIHZpZXcgYXZhaWxhYmxlIG9uIHRoZSBzdGF0ZS5kYXRhXHJcbiAqIC0tLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICogSGVhZGVyIENvbnRyb2xsZXJcclxuICogUGFnZSB0aXRsZSAgICAgICAgICAgICAgICAgICAgICBIb21lIC0+IExheW91dCAtIFN1YiBMYXlvdXRcclxuICpcclxuICogTW9kdWxlIENvbnRyb2xsZXJcclxuICogQ29udGVudFxyXG4gKiAtLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSGVhZGVyQ29udHJvbGxlciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nXHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuXHJcbiAgICAgICAgdGhpcy5wYWdlTWV0YURhdGEgPSB7XHJcbiAgICAgICAgICAgIHRpdGxlOiAnJyxcclxuICAgICAgICAgICAgaW5zdHJ1Y3Rpb246ICcnLFxyXG4gICAgICAgICAgICBtZW51OiBbXVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZUhlYWRlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIZWFkZXIgQ29udHJvbGxlciBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFZlcmlmeSBpZiB3ZSBoYXZlIGEgbWVudSB0byBzaG93IHRvIG1hZGUgaXQgYXZhaWxhYmxlIHRvIHRoZSBWaWV3XHJcbiAgICAgKi9cclxuICAgIHByZXBhcmVIZWFkZXIoKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuc3RhdGUgJiYgdGhpcy5zdGF0ZS4kY3VycmVudCAmJiB0aGlzLnN0YXRlLiRjdXJyZW50LmRhdGEpIHtcclxuICAgICAgICAgICAgdGhpcy5wYWdlTWV0YURhdGEgPSB0aGlzLnN0YXRlLiRjdXJyZW50LmRhdGEucGFnZTtcclxuICAgICAgICAgICAgZG9jdW1lbnQudGl0bGUgPSB0aGlzLnBhZ2VNZXRhRGF0YS50aXRsZTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMS8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IEhlYWRlckNvbnRyb2xsZXIgZnJvbSAnLi9IZWFkZXJDb250cm9sbGVyLmpzJztcclxuaW1wb3J0IERpYWxvZ0FjdGlvbiBmcm9tICcuLi9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmpzJztcclxuXHJcbnZhciBIZWFkZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uSGVhZGVyTW9kdWxlJywgW10pO1xyXG5cclxuSGVhZGVyTW9kdWxlLmNvbnRyb2xsZXIoJ0hlYWRlckNvbnRyb2xsZXInLCBbJyRsb2cnLCAnJHN0YXRlJywgSGVhZGVyQ29udHJvbGxlcl0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5IZWFkZXJNb2R1bGUuY29udHJvbGxlcignRGlhbG9nQWN0aW9uJywgWyckbG9nJywnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIERpYWxvZ0FjdGlvbl0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgSGVhZGVyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VBZG1pbkxpc3QgZnJvbSAnLi9saXN0L0xpY2Vuc2VBZG1pbkxpc3QuanMnO1xyXG5pbXBvcnQgTGljZW5zZUFkbWluU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZUFkbWluU2VydmljZS5qcyc7XHJcbmltcG9ydCBSZXF1ZXN0TGljZW5zZSBmcm9tICcuL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuanMnO1xyXG5pbXBvcnQgQ3JlYXRlZExpY2Vuc2UgZnJvbSAnLi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmpzJztcclxuaW1wb3J0IEFwcGx5TGljZW5zZUtleSBmcm9tICcuL2FwcGx5TGljZW5zZUtleS9BcHBseUxpY2Vuc2VLZXkuanMnO1xyXG5pbXBvcnQgTWFudWFsbHlSZXF1ZXN0IGZyb20gJy4vbWFudWFsbHlSZXF1ZXN0L01hbnVhbGx5UmVxdWVzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlRGV0YWlsIGZyb20gJy4vZGV0YWlsL0xpY2Vuc2VEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlQWRtaW5Nb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uTGljZW5zZUFkbWluTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdsaWNlbnNlQWRtaW4nKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VBZG1pbkxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdBZG1pbmlzdGVyIExpY2Vuc2VzJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FETUlOJywgJ0xJQ0VOU0UnLCAnTElTVCddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy9saWNlbnNlL2FkbWluL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9saXN0L0xpY2Vuc2VBZG1pbkxpc3QuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VBZG1pbkxpc3QgYXMgbGljZW5zZUFkbWluTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuc2VydmljZSgnTGljZW5zZUFkbWluU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCAnJHJvb3RTY29wZScsIExpY2Vuc2VBZG1pblNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlQWRtaW5MaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsIExpY2Vuc2VBZG1pbkxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ1JlcXVlc3RMaWNlbnNlJywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RMaWNlbnNlXSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdDcmVhdGVkTGljZW5zZScsIFsnJGxvZycsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBDcmVhdGVkTGljZW5zZV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignQXBwbHlMaWNlbnNlS2V5JywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBBcHBseUxpY2Vuc2VLZXldKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ01hbnVhbGx5UmVxdWVzdCcsIFsnJGxvZycsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIE1hbnVhbGx5UmVxdWVzdF0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTGljZW5zZURldGFpbCcsIFsnJGxvZycsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlRGV0YWlsXSk7XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgTGljZW5zZUFkbWluTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQXBwbHlMaWNlbnNlS2V5IGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSlcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG4gICAgICAgICAgICBrZXk6IHBhcmFtcy5saWNlbnNlLmtleVxyXG4gICAgICAgIH1cclxuICAgICAgICA7XHJcbiAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIGFuZCB2YWxpZGF0ZSB0aGUgS2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgYXBwbHlLZXkoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmFwcGx5TGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSwgKGRhdGEpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBDcmVhdGVkUmVxdWVzdExpY2Vuc2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmNsaWVudCA9IHBhcmFtcztcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VEZXRhaWwge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9JHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgbWV0aG9kSWQ6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5pZCxcclxuICAgICAgICAgICAgcHJvamVjdE5hbWU6IHBhcmFtcy5saWNlbnNlLnByb2plY3QubmFtZSxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcbiAgICAgICAgICAgIGVtYWlsOiBwYXJhbXMubGljZW5zZS5lbWFpbCxcclxuICAgICAgICAgICAgc2VydmVyc1Rva2VuczogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLm1heCxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnROYW1lOiBwYXJhbXMubGljZW5zZS5lbnZpcm9ubWVudC5uYW1lLFxyXG4gICAgICAgICAgICBpbmNlcHRpb246IHBhcmFtcy5saWNlbnNlLnJlcXVlc3REYXRlLFxyXG4gICAgICAgICAgICBleHBpcmF0aW9uOiBwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSxcclxuICAgICAgICAgICAgcmVxdWVzdE5vdGU6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3ROb3RlLFxyXG4gICAgICAgICAgICBhY3RpdmU6IHBhcmFtcy5saWNlbnNlLnN0YXR1cy5pZCA9PT0gMSxcclxuICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG4gICAgICAgICAgICByZXBsYWNlZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWQsXHJcbiAgICAgICAgICAgIGVuY3J5cHRlZERldGFpbDogcGFyYW1zLmxpY2Vuc2UuZW5jcnlwdGVkRGV0YWlsLFxyXG4gICAgICAgICAgICBhcHBsaWVkOiBmYWxzZVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZU1ldGhvZE9wdGlvbnMoKTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlTWV0aG9kT3B0aW9ucygpIHtcclxuICAgICAgICB0aGlzLm1ldGhvZE9wdGlvbnMgPSBbXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAxLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ1NlcnZlcnMnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ1Rva2VucydcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgaWQ6IDMsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnQ3VzdG9tJ1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgXVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhlIHVzZXIgYXBwbHkgYW5kIHNlcnZlciBzaG91bGQgdmFsaWRhdGUgdGhlIGtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZUtleSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vYXBwbHlMaWNlbnNlS2V5L0FwcGx5TGljZW5zZUtleS5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0FwcGx5TGljZW5zZUtleSBhcyBhcHBseUxpY2Vuc2VLZXknLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiB0aGlzLmxpY2Vuc2VNb2RlbCB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuYXBwbGllZCA9IGRhdGEuc3VjY2VzcztcclxuICAgICAgICAgICAgaWYoZGF0YS5zdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5hY3RpdmUgPSBkYXRhLnN1Y2Nlc3M7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoeyBpZDogdGhpcy5saWNlbnNlTW9kZWwuaWQsIHVwZGF0ZWQ6IHRydWV9KTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogT3BlbnMgYSBkaWFsb2cgYW5kIGFsbG93IHRoZSB1c2VyIHRvIG1hbnVhbGx5IHNlbmQgdGhlIHJlcXVlc3Qgb3IgY29weSB0aGUgZW5jcmlwdGVkIGNvZGVcclxuICAgICAqL1xyXG4gICAgbWFudWFsbHlSZXF1ZXN0KCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTWFudWFsbHlSZXF1ZXN0IGFzIG1hbnVhbGx5UmVxdWVzdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdCgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UucmVzdWJtaXRMaWNlbnNlUmVxdWVzdCh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHt9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIGRlbGV0ZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmRlbGV0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQpIHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKCk7XHJcbiAgICAgICAgfVxyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5MaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLm9wZW5MYXN0TGljZW5zZUlkID0gMDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VBZG1pbkxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vblJlcXVlc3ROZXdMaWNlbnNlKClcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gUmVxdWVzdCBOZXcgTGljZW5zZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5yZWxvYWRMaWNlbnNlQWRtaW5MaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMjBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbGljZW5zZUlkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCBmaWx0ZXJhYmxlOiBmYWxzZSwgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vbkxpY2Vuc2VEZXRhaWxzKHRoaXMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjbGllbnQubmFtZScsIHRpdGxlOiAnQ2xpZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLCB0aXRsZTogJ1Byb2plY3QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2VtYWlsJywgdGl0bGU6ICdDb250YWN0IEVtYWlsJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMubmFtZScsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnLCAgdGVtcGxhdGU6ICcjaWYoZGF0YS50eXBlICYmIGRhdGEudHlwZS5uYW1lID09PSBcIk1VTFRJX1BST0pFQ1RcIil7IyBHbG9iYWwgIyB9IGVsc2UgeyMgU2luZ2xlICN9Iyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5tYXgnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3JlcXVlc3REYXRlJywgdGl0bGU6ICdJbmNlcHRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uRGF0ZScsIHRpdGxlOiAnRXhwaXJhdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Vudmlyb25tZW50Lm5hbWUnLCB0aXRsZTogJ0Vudmlyb25tZW50J31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAncHJvamVjdC5uYW1lJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdExpY2Vuc2VJZCAhPT0gMCAmJiB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdmFyIGxhc3RMaWNlbnNlID0gdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhLmZpbmQoKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBsaWNlbnNlLmlkID09PSB0aGlzLm9wZW5MYXN0TGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobGFzdExpY2Vuc2UpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlRGV0YWlscyhsYXN0TGljZW5zZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0TGljZW5zZSBhcyByZXF1ZXN0TGljZW5zZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdOZXcgTGljZW5zZSBDcmVhdGVkOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICAgICAgdGhpcy5vbk5ld0xpY2Vuc2VDcmVhdGVkKGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZURldGFpbHMobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09wZW4gRGV0YWlscyBmb3I6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9kZXRhaWwvTGljZW5zZURldGFpbC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VEZXRhaWwgYXMgbGljZW5zZURldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcbiAgICAgICAgICAgIGlmKGRhdGEudXBkYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IGRhdGEuaWQ7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25OZXdMaWNlbnNlQ3JlYXRlZChsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmh0bWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQ3JlYXRlZExpY2Vuc2UgYXMgY3JlYXRlZExpY2Vuc2UnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBlbWFpbDogbGljZW5zZS5lbWFpbCAgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBNYW51YWxseVJlcXVlc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6ICBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBlbmNyeXB0ZWREZXRhaWw6ICcnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgLy8gR2V0IHRoZSBoYXNoIGNvZGUgdXNpbmcgdGhlIGlkLlxyXG4gICAgICAgIHRoaXMuZ2V0SGFzaENvZGUoKTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0SGFzaENvZGUoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldEhhc2hDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbmNyeXB0ZWREZXRhaWwgPSBkYXRhO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqIENyZWF0ZSBhIG5ldyBSZXF1ZXN0IHRvIGdldCBhIExpY2Vuc2VcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdExpY2Vuc2UgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIC8qKlxyXG4gICAgICogSW5pdGlhbGl6ZSBhbGwgdGhlIHByb3BlcnRpZXNcclxuICAgICAqIEBwYXJhbSAkbG9nXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZUFkbWluU2VydmljZVxyXG4gICAgICogQHBhcmFtICR1aWJNb2RhbEluc3RhbmNlXHJcbiAgICAgKi9cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICAvLyBEZWZpbmVkIHRoZSBFbnZpcm9ubWVudCBTZWxlY3RcclxuICAgICAgICB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSA9IFtdO1xyXG4gICAgICAgIC8vIERlZmluZSB0aGUgUHJvamVjdCBTZWxlY3RcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3QgPSB7fTtcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3RMaXN0T3B0aW9ucyA9IFtdO1xyXG5cclxuICAgICAgICB0aGlzLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0UHJvamVjdERhdGFTb3VyY2UoKTtcclxuXHJcbiAgICAgICAgLy8gQ3JlYXRlIHRoZSBNb2RlbCBmb3IgdGhlIE5ldyBMaWNlbnNlXHJcbiAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGVtYWlsOiAnJyxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnRJZDogMCxcclxuICAgICAgICAgICAgcHJvamVjdElkOiAwLFxyXG4gICAgICAgICAgICBjbGllbnROYW1lOiAnJyxcclxuICAgICAgICAgICAgcmVxdWVzdE5vdGU6ICcnXHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpPT57XHJcbiAgICAgICAgICAgIHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlID0gZGF0YTtcclxuICAgICAgICAgICAgaWYodGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgICAgIHZhciBpbmRleCA9IHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlLmZpbmRJbmRleChmdW5jdGlvbihlbnZpcm9tZW50KXtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4gZW52aXJvbWVudC5uYW1lICA9PT0gJ1Byb2R1Y3Rpb24nO1xyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICBpbmRleCA9IGluZGV4IHx8IDA7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm5ld0xpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudElkID0gZGF0YVtpbmRleF0uaWQ7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgUHJvamVjdCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0UHJvamVjdERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwucHJvamVjdElkID0gZGF0YVswXS5pZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSlcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGFUZXh0RmllbGQ6ICduYW1lJyxcclxuICAgICAgICAgICAgZGF0YVZhbHVlRmllbGQ6ICdpZCcsXHJcbiAgICAgICAgICAgIHZhbHVlUHJpbWl0aXZlOiB0cnVlLFxyXG4gICAgICAgICAgICBzZWxlY3Q6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgLy8gT24gUHJvamVjdCBDaGFuZ2UsIHNlbGVjdCB0aGUgQ2xpZW50IE5hbWVcclxuICAgICAgICAgICAgICAgIHZhciBpdGVtID0gdGhpcy5zZWxlY3RQcm9qZWN0LmRhdGFJdGVtKGUuaXRlbSk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm5ld0xpY2Vuc2VNb2RlbC5jbGllbnROYW1lID0gaXRlbS5jbGllbnQubmFtZTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSB0aGUgU2VydmljZSBjYWxsIHRvIGdlbmVyYXRlIGEgbmV3IExpY2Vuc2UgcmVxdWVzdFxyXG4gICAgICovXHJcbiAgICBzYXZlTGljZW5zZVJlcXVlc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTmV3IExpY2Vuc2UgUmVxdWVzdGVkOiAnLCB0aGlzLm5ld0xpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdCh0aGlzLm5ld0xpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSh0aGlzLm5ld0xpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5TZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIsICRyb290U2NvcGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLnJvb3RTY29wZSA9ICRyb290U2NvcGU7XHJcbiAgICAgICAgdGhpcy5zdGF0dXNTdWNjZXNzID0gJ3N1Y2Nlc3MnO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdsaWNlbnNlQWRtaW5TZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldExpY2Vuc2VMaXN0KG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0SGFzaENvZGUobGljZW5zZUlkLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0SGFzaENvZGUobGljZW5zZUlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBvblN1Y2Nlc3Mpe1xyXG4gICAgICAgIG5ld0xpY2Vuc2UuZW52aXJvbm1lbnRJZCA9IHBhcnNlSW50KG5ld0xpY2Vuc2UuZW52aXJvbm1lbnRJZCk7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0KG5ld0xpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdChsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5yZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdSZXF1ZXN0IExpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseS4nfSk7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBlbWFpbFJlcXVlc3QobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZW1haWxSZXF1ZXN0KGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdSZXF1ZXN0IExpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseS4nfSk7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqICBBcHBseSBUaGUgTGljZW5zZVxyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBvblN1Y2Nlc3NcclxuICAgICAqL1xyXG4gICAgYXBwbHlMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG5cclxuICAgICAgICB2YXIgaGFzaCA9ICB7XHJcbiAgICAgICAgICAgIGhhc2g6IGxpY2Vuc2Uua2V5XHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmFwcGx5TGljZW5zZShsaWNlbnNlLmlkLCBoYXNoLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICBpZihkYXRhLnN0YXR1cyA9PT0gdGhpcy5zdGF0dXNTdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5IGFwcGxpZWQnfSk7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ3dhcm5pbmcnLCB0ZXh0OiAnTGljZW5zZSB3YXMgbm90IGFwcGxpZWQnfSk7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gb25FcnJvcih7IHN1Y2Nlc3M6IGZhbHNlfSk7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoeyBzdWNjZXNzOiB0cnVlfSk7XHJcblxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGRlbGV0ZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmRlbGV0ZUxpY2Vuc2UobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJMaXN0IGZyb20gJy4vbGlzdC9MaWNlbnNlTWFuYWdlckxpc3QuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9MaWNlbnNlTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgUmVxdWVzdEltcG9ydCBmcm9tICcuL3JlcXVlc3RJbXBvcnQvUmVxdWVzdEltcG9ydC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlckRldGFpbCBmcm9tICcuL2RldGFpbC9MaWNlbnNlTWFuYWdlckRldGFpbC5qcyc7XHJcblxyXG5cclxudmFyIExpY2Vuc2VNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkxpY2Vuc2VNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdsaWNlbnNlTWFuYWdlcicpO1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlclZpZXcuaHRtbCcsXHJcbiAgICAgICAgY29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG4gICAgfTtcclxuXHJcbiAgICAkc3RhdGVQcm92aWRlclxyXG4gICAgICAgIC5zdGF0ZSgnbGljZW5zZU1hbmFnZXJMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTGljZW5zaW5nIE1hbmFnZXInLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnTUFOQUdFUicsICdMSUNFTlNFJywgJ0xJU1QnXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvbGljZW5zZS9tYW5hZ2VyL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlTWFuYWdlckxpc3QgYXMgbGljZW5zZU1hbmFnZXJMaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLnNlcnZpY2UoJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCAnJHJvb3RTY29wZScsIExpY2Vuc2VNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VNYW5hZ2VyTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsIExpY2Vuc2VNYW5hZ2VyTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdSZXF1ZXN0SW1wb3J0JywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgUmVxdWVzdEltcG9ydF0pO1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlTWFuYWdlckRldGFpbCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlTWFuYWdlckRldGFpbF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJEZXRhaWwgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgb3duZXJOYW1lOiBwYXJhbXMubGljZW5zZS5vd25lci5uYW1lLFxyXG4gICAgICAgICAgICBlbWFpbDogcGFyYW1zLmxpY2Vuc2UuZW1haWwsXHJcbiAgICAgICAgICAgIHByb2plY3RJZDogcGFyYW1zLmxpY2Vuc2UucHJvamVjdC5pZCxcclxuICAgICAgICAgICAgY2xpZW50SWQ6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5pZCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcbiAgICAgICAgICAgIHN0YXR1c0lkOiBwYXJhbXMubGljZW5zZS5zdGF0dXMuaWQsXHJcbiAgICAgICAgICAgIG1ldGhvZDoge1xyXG4gICAgICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5pZCxcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5uYW1lLFxyXG4gICAgICAgICAgICAgICAgbWF4OiBwYXJhbXMubGljZW5zZS5tZXRob2QubWF4LFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudDogeyBpZDogcGFyYW1zLmxpY2Vuc2UuZW52aXJvbm1lbnQuaWQgfSxcclxuICAgICAgICAgICAgcmVxdWVzdERhdGU6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3REYXRlLFxyXG4gICAgICAgICAgICBpbml0RGF0ZTogKHBhcmFtcy5saWNlbnNlLmFjdGl2YXRpb25EYXRlICE9PSBudWxsKT8gYW5ndWxhci5jb3B5KHBhcmFtcy5saWNlbnNlLmFjdGl2YXRpb25EYXRlKSA6ICcnLFxyXG4gICAgICAgICAgICBlbmREYXRlOiAocGFyYW1zLmxpY2Vuc2UuZXhwaXJhdGlvbkRhdGUgIT09IG51bGwpPyBhbmd1bGFyLmNvcHkocGFyYW1zLmxpY2Vuc2UuZXhwaXJhdGlvbkRhdGUpIDogJycsXHJcbiAgICAgICAgICAgIHNwZWNpYWxJbnN0cnVjdGlvbnM6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3ROb3RlLFxyXG4gICAgICAgICAgICB3ZWJzaXRlTmFtZTogcGFyYW1zLmxpY2Vuc2Uud2Vic2l0ZW5hbWUsXHJcblxyXG4gICAgICAgICAgICBiYW5uZXJNZXNzYWdlOiBwYXJhbXMubGljZW5zZS5iYW5uZXJNZXNzYWdlLFxyXG4gICAgICAgICAgICByZXF1ZXN0ZWRJZDogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdGVkSWQsXHJcbiAgICAgICAgICAgIHJlcGxhY2VkOiBwYXJhbXMubGljZW5zZS5yZXBsYWNlZCxcclxuICAgICAgICAgICAgcmVwbGFjZWRJZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWRJZCxcclxuICAgICAgICAgICAgYWN0aXZpdHlMaXN0OiBwYXJhbXMubGljZW5zZS5hY3Rpdml0eUxpc3QsXHJcbiAgICAgICAgICAgIGhvc3ROYW1lOiBwYXJhbXMubGljZW5zZS5ob3N0TmFtZSxcclxuICAgICAgICAgICAgaGFzaDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIGdyYWNlUGVyaW9kRGF5czogcGFyYW1zLmxpY2Vuc2UuZ3JhY2VQZXJpb2REYXlzLFxyXG5cclxuICAgICAgICAgICAgYXBwbGllZDogcGFyYW1zLmxpY2Vuc2UuYXBwbGllZCxcclxuICAgICAgICAgICAga2V5SWQ6IHBhcmFtcy5saWNlbnNlLmtleUlkXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlS2V5ID0gJ0xpY2Vuc2VzIGhhcyBub3QgYmVlbiBpc3N1ZWQnO1xyXG5cclxuICAgICAgICAvLyBDcmVhdGVzIHRoZSBQcm9qZWN0IFNlbGVjdCBMaXN0XHJcbiAgICAgICAgLy8gRGVmaW5lIHRoZSBQcm9qZWN0IFNlbGVjdFxyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdCA9IHt9O1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0gW107XHJcbiAgICAgICAgdGhpcy5nZXRQcm9qZWN0RGF0YVNvdXJjZSgpO1xyXG5cclxuICAgICAgICAvLyBEZWZpbmVkIHRoZSBFbnZpcm9ubWVudCBTZWxlY3RcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RFbnZpcm9ubWVudExpc3RPcHRpb25zID0gW107XHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgU3RhdHVzIFNlbGVjdCBMaXN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RTdGF0dXMgPSBbXTtcclxuICAgICAgICB0aGlzLmdldFN0YXR1c0RhdGFTb3VyY2UoKTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCB0aGUgdHdvIEtlbmRvIERhdGVzIGZvciBJbml0IGFuZCBFbmREYXRlXHJcbiAgICAgICAgdGhpcy5pbml0RGF0ZSA9IHt9O1xyXG4gICAgICAgIHRoaXMuaW5pdERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6ICd5eXl5L01NL2RkJyxcclxuICAgICAgICAgICAgb3BlbjogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgICAgIGNoYW5nZTogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmVuZERhdGUgPSB7fTtcclxuICAgICAgICB0aGlzLmVuZERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6ICd5eXl5L01NL2RkJyxcclxuICAgICAgICAgICAgb3BlbjogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlRW5kRGF0ZSgpO1xyXG4gICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgY2hhbmdlOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuXHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZU1ldGhvZE9wdGlvbnMoKTtcclxuICAgICAgICB0aGlzLnByZXBhcmVMaWNlbnNlS2V5KCk7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQWN0aXZpdHlMaXN0KCk7XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcblxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIFByb2plY3QgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgaWYoIXRoaXMubGljZW5zZU1vZGVsLnByb2plY3RJZCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLnByb2plY3RJZCA9IGRhdGFbMF0uaWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGF0YVRleHRGaWVsZDogJ25hbWUnLFxyXG4gICAgICAgICAgICBkYXRhVmFsdWVGaWVsZDogJ2lkJyxcclxuICAgICAgICAgICAgdmFsdWVQcmltaXRpdmU6IHRydWUsXHJcbiAgICAgICAgICAgIHNlbGVjdDogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAvLyBPbiBQcm9qZWN0IENoYW5nZSwgc2VsZWN0IHRoZSBDbGllbnQgTmFtZVxyXG4gICAgICAgICAgICAgICAgdmFyIGl0ZW0gPSB0aGlzLnNlbGVjdFByb2plY3QuZGF0YUl0ZW0oZS5pdGVtKTtcclxuICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmNsaWVudE5hbWUgPSBpdGVtLmNsaWVudC5uYW1lO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDb250cm9scyB3aGF0IGJ1dHRvbnMgdG8gc2hvd1xyXG4gICAgICovXHJcbiAgICBwcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5wZW5kaW5nTGljZW5zZSA9IHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSA0ICYmICF0aGlzLmVkaXRNb2RlO1xyXG4gICAgICAgIHRoaXMuZXhwaXJlZE9yVGVybWluYXRlZCA9ICh0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMiB8fCB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMyk7XHJcbiAgICAgICAgdGhpcy5hY3RpdmVTaG93TW9kZSA9IHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSAxICYmICF0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgJiYgIXRoaXMuZWRpdE1vZGU7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdTZXJ2ZXJzJyxcclxuICAgICAgICAgICAgICAgIG1heDogMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbnMnLFxyXG4gICAgICAgICAgICAgICAgbWF4OiAwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAzLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ0N1c3RvbSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF1cclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlTGljZW5zZUtleSgpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRLZXlDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUtleSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlQWN0aXZpdHlMaXN0KCkge1xyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdkYXRlJywgdGl0bGU6ICdEYXRlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd3aG9tJywgdGl0bGU6ICdXaG9tJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbid9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHRoaXMubGljZW5zZU1vZGVsLmFjdGl2aXR5TGlzdCxcclxuICAgICAgICAgICAgc2Nyb2xsYWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgYWN0aXZhdGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmFjdGl2YXRlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgLy90aGlzLnByZXBhcmVMaWNlbnNlS2V5KCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byByZXZva2UgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnJldm9rZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmFsaWRhdGUgdGhlIGlucHV0IG9uIFNlcnZlciBvciBUb2tlbnMgaXMgb25seSBpbnRlZ2VyIG9ubHlcclxuICAgICAqIFRoaXMgd2lsbCBiZSBjb252ZXJ0ZWQgaW4gYSBtb3JlIGNvbXBsZXggZGlyZWN0aXZlIGxhdGVyXHJcbiAgICAgKiBUT0RPOiBDb252ZXJ0IGludG8gYSBkaXJlY3RpdmVcclxuICAgICAqL1xyXG4gICAgdmFsaWRhdGVJbnRlZ2VyT25seShlLG1vZGVsKXtcclxuICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICB2YXIgbmV3VmFsPSBwYXJzZUludChtb2RlbCk7XHJcbiAgICAgICAgICAgIGlmKCFpc05hTihuZXdWYWwpKSB7XHJcbiAgICAgICAgICAgICAgICBtb2RlbCA9IG5ld1ZhbDtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIG1vZGVsID0gMDtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICBpZihlICYmIGUuY3VycmVudFRhcmdldCkge1xyXG4gICAgICAgICAgICAgICAgZS5jdXJyZW50VGFyZ2V0LnZhbHVlID0gbW9kZWw7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9IGNhdGNoKGUpIHtcclxuICAgICAgICAgICAgdGhpcy4kbG9nLndhcm4oJ0ludmFsaWQgTnVtYmVyIEV4cGNlcHRpb24nLCBtb2RlbCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZSBjdXJyZW50IGNoYW5nZXNcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5zYXZlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdMaWNlbnNlIFNhdmVkJyk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENoYW5nZSB0aGUgc3RhdHVzIHRvIEVkaXRcclxuICAgICAqL1xyXG4gICAgbW9kaWZ5TGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gdHJ1ZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50TGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgaWYoIXRoaXMubGljZW5zZU1vZGVsLmVudmlyb25tZW50SWQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudElkID0gZGF0YVswXS5pZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0U3RhdHVzRGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnN0YXR1c1RleHQgPVxyXG4gICAgICAgICAgICAgKHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSAxKT8gJ0FjdGl2ZScgOlxyXG4gICAgICAgICAgICAgICAgICgodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDIpPyAnRXhwaXJlZCc6XHJcbiAgICAgICAgICAgICAgICAgICAgICgodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDMpPyAnVGVybWluYXRlZCc6XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAoKHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSA0KT8gJ1BlbmRpbmcnIDogJycpKSk7XHJcblxyXG4gICAgICAgIC8qdGhpcy5zZWxlY3RTdGF0dXNMaXN0T3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZGF0YVNvdXJjZTogW1xyXG4gICAgICAgICAgICAgICAge2lkOiAxLCBuYW1lOiAnQWN0aXZlJ30sXHJcbiAgICAgICAgICAgICAgICB7aWQ6IDIsIG5hbWU6ICdFeHBpcmVkJ30sXHJcbiAgICAgICAgICAgICAgICB7aWQ6IDMsIG5hbWU6ICdUZXJtaW5hdGVkJ30sXHJcbiAgICAgICAgICAgICAgICB7aWQ6IDQsIG5hbWU6ICdQZW5kaW5nJ31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVRleHRGaWVsZDogJ25hbWUnLFxyXG4gICAgICAgICAgICBkYXRhVmFsdWVGaWVsZDogJ2lkJyxcclxuICAgICAgICAgICAgdmFsdWVQcmltaXRpdmU6IHRydWVcclxuICAgICAgICB9Ki9cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEEgbmV3IFByb2plY3QgaGFzIGJlZW4gc2VsZWN0ZWQsIHRoYXQgbWVhbnMgd2UgbmVlZCB0byByZWxvYWQgdGhlIG5leHQgcHJvamVjdCBzZWN0aW9uXHJcbiAgICAgKiBAcGFyYW0gaXRlbVxyXG4gICAgICovXHJcbiAgICBvbkNoYW5nZVByb2plY3QoaXRlbSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09uIGNoYW5nZSBQcm9qZWN0JywgaXRlbSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25DaGFuZ2VJbml0RGF0ZSgpIHtcclxuICAgICAgICB2YXIgc3RhcnREYXRlID0gdGhpcy5pbml0RGF0ZS52YWx1ZSgpLFxyXG4gICAgICAgICAgICBlbmREYXRlID0gdGhpcy5lbmREYXRlLnZhbHVlKCk7XHJcblxyXG4gICAgICAgIGlmIChzdGFydERhdGUpIHtcclxuICAgICAgICAgICAgc3RhcnREYXRlID0gbmV3IERhdGUoc3RhcnREYXRlKTtcclxuICAgICAgICAgICAgc3RhcnREYXRlLnNldERhdGUoc3RhcnREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4oc3RhcnREYXRlKTtcclxuXHJcbiAgICAgICAgICAgIGlmKGVuZERhdGUpIHtcclxuICAgICAgICAgICAgICAgIGlmKHRoaXMuaW5pdERhdGUudmFsdWUoKSA+IHRoaXMuZW5kRGF0ZS52YWx1ZSgpKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKGVuZERhdGUpO1xyXG4gICAgICAgICAgICAgICAgICAgIGVuZERhdGUuc2V0RGF0ZShzdGFydERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbmREYXRlID0gZW5kRGF0ZTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBvbkNoYW5nZUVuZERhdGUoKXtcclxuICAgICAgICB2YXIgZW5kRGF0ZSA9IHRoaXMuZW5kRGF0ZS52YWx1ZSgpLFxyXG4gICAgICAgICAgICBzdGFydERhdGUgPSB0aGlzLmluaXREYXRlLnZhbHVlKCk7XHJcblxyXG4gICAgICAgIGlmIChlbmREYXRlKSB7XHJcbiAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZShlbmREYXRlKTtcclxuICAgICAgICAgICAgZW5kRGF0ZS5zZXREYXRlKGVuZERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICB9IGVsc2UgaWYgKHN0YXJ0RGF0ZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKG5ldyBEYXRlKHN0YXJ0RGF0ZSkpO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZSgpO1xyXG4gICAgICAgICAgICB0aGlzLmluaXREYXRlLm1heChlbmREYXRlKTtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihlbmREYXRlKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgaWYodGhpcy5lZGl0TW9kZSkge1xyXG4gICAgICAgICAgICB0aGlzLnJlc2V0Rm9ybSgoKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25SZXNldEZvcm0oKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIGlmKHRoaXMucmVsb2FkUmVxdWlyZWQpe1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2Uoe30pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEZXBlZGluZyB0aGUgbnVtYmVyIG9mIGZpZWxkcyBhbmQgdHlwZSBvZiBmaWVsZCwgdGhlIHJlc2V0IGNhbid0IGJlIG9uIHRoZSBGb3JtVmFsaWRvciwgYXQgbGVhc3Qgbm90IG5vd1xyXG4gICAgICovXHJcbiAgICBvblJlc2V0Rm9ybSgpIHtcclxuICAgICAgICAvLyBSZXNldCBQcm9qZWN0IFNlbGVjdG9yXHJcbiAgICAgICAgdGhpcy5yZXNldERyb3BEb3duKHRoaXMuc2VsZWN0UHJvamVjdCwgdGhpcy5saWNlbnNlTW9kZWwucHJvamVjdElkKTtcclxuICAgICAgICAvL3RoaXMucmVzZXREcm9wRG93bih0aGlzLnNlbGVjdFN0YXR1cywgdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQpO1xyXG4gICAgICAgIHRoaXMucmVzZXREcm9wRG93bih0aGlzLnNlbGVjdEVudmlyb25tZW50LCB0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudC5pZCk7XHJcbiAgICAgICAgdGhpcy5vbkNoYW5nZUluaXREYXRlKCk7XHJcbiAgICAgICAgdGhpcy5vbkNoYW5nZUVuZERhdGUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJMaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UgPSBsaWNlbnNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgLy90aGlzLmdldExpY2Vuc2VMaXN0KCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VNYW5hZ2VyTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSAwO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3Qub25SZXF1ZXN0SW1wb3J0TGljZW5zZSgpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IEltcG9ydCBMaWNlbnNlIFJlcXVlc3Q8L2J1dHRvbj4gPGRpdiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAyMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdpZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgZmlsdGVyYWJsZTogZmFsc2UsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5vbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyh0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnb3duZXIubmFtZScsIHRpdGxlOiAnT3duZXInfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NsaWVudC5uYW1lJywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QubmFtZScsIHRpdGxlOiAnUHJvamVjdCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cy5uYW1lJywgdGl0bGU6ICdTdGF0dXMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZScsICB0ZW1wbGF0ZTogJyNpZihkYXRhLnR5cGUgJiYgZGF0YS50eXBlLm5hbWUgPT09IFwiTVVMVElfUFJPSkVDVFwiKXsjIEdsb2JhbCAjIH0gZWxzZSB7IyBTaW5nbGUgI30jJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubmFtZScsIHRpdGxlOiAnTWV0aG9kJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QuaWQnLCBoaWRkZW46IHRydWV9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm1heCcsIHRpdGxlOiAnU2VydmVyL1Rva2Vucyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aXZhdGlvbkRhdGUnLCB0aXRsZTogJ0luY2VwdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2V4cGlyYXRpb25EYXRlJywgdGl0bGU6ICdFeHBpcmF0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW52aXJvbm1lbnQubmFtZScsIHRpdGxlOiAnRW52aXJvbm1lbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDonZ3JhY2VQZXJpb2REYXlzJyxoaWRkZW46IHRydWV9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBjaGFuZ2U6ICAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIC8vIFdlIGFyZSBjb21pbmcgZnJvbSBhIG5ldyBpbXBvcnRlZCByZXF1ZXN0IGxpY2Vuc2VcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgIT09IDAgJiYgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHZhciBuZXdMaWNlbnNlQ3JlYXRlZCA9IHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YS5maW5kKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gbGljZW5zZS5pZCA9PT0gdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IDA7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICBpZihuZXdMaWNlbnNlQ3JlYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyhuZXdMaWNlbnNlQ3JlYXRlZCk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGUgdXNlciBJbXBvcnQgYSBuZXcgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBvblJlcXVlc3RJbXBvcnRMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL3JlcXVlc3RJbXBvcnQvUmVxdWVzdEltcG9ydC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1JlcXVlc3RJbXBvcnQgYXMgcmVxdWVzdEltcG9ydCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IGxpY2Vuc2VJbXBvcnRlZC5pZDsgLy8gdGFrZSB0aGlzIHBhcmFtIGZyb20gdGhlIGxhc3QgaW1wb3J0ZWQgbGljZW5zZSwgb2YgY291cnNlXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKGxpY2Vuc2UpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPcGVuIERldGFpbHMgZm9yOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlTWFuYWdlckRldGFpbCBhcyBsaWNlbnNlTWFuYWdlckRldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgcmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SW1wb3J0IGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBoYXNoOiAnJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIG9uSW1wb3J0TGljZW5zZSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5pbXBvcnRMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UobGljZW5zZUltcG9ydGVkLmRhdGEpO1xyXG4gICAgICAgICAgICB9LCAobGljZW5zZUltcG9ydGVkKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShsaWNlbnNlSW1wb3J0ZWQuZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlciwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG4gICAgICAgIHRoaXMucm9vdFNjb3BlID0gJHJvb3RTY29wZTtcclxuICAgICAgICB0aGlzLnN0YXR1c1N1Y2Nlc3MgPSAnc3VjY2Vzcyc7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ2xpY2Vuc2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldFByb2plY3REYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2Uob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRLZXlDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0S2V5Q29kZShsaWNlbnNlSWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgdGhlIExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcblxyXG4gICAgICAgIHZhciBsaWNlbnNlTW9kaWZpZWQgPSB7XHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiB7IGlkOiBwYXJzZUludChsaWNlbnNlLmVudmlyb25tZW50LmlkKSB9LFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiBwYXJzZUludChsaWNlbnNlLm1ldGhvZC5pZClcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGlvbkRhdGU6IGxpY2Vuc2UuaW5pdERhdGUsXHJcbiAgICAgICAgICAgIGV4cGlyYXRpb25EYXRlOiBsaWNlbnNlLmVuZERhdGUsXHJcbiAgICAgICAgICAgIHN0YXR1czogeyBpZDogbGljZW5zZS5zdGF0dXNJZCB9LFxyXG4gICAgICAgICAgICBwcm9qZWN0OiB7IGlkOiAobGljZW5zZS5wcm9qZWN0SWQgIT09ICdhbGwnKT8gcGFyc2VJbnQobGljZW5zZS5wcm9qZWN0SWQpIDogbGljZW5zZS5wcm9qZWN0SWQgIH0sIC8vIFdlIHBhc3MgJ2FsbCcgd2hlbiBpcyBtdWx0aXByb2plY3RcclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogbGljZW5zZS5iYW5uZXJNZXNzYWdlLFxyXG4gICAgICAgICAgICBncmFjZVBlcmlvZERheXM6IGxpY2Vuc2UuZ3JhY2VQZXJpb2REYXlzXHJcbiAgICAgICAgICAgIH07XHJcbiAgICAgICAgaWYobGljZW5zZS5tZXRob2QgIT09IDMpIHtcclxuICAgICAgICAgICAgbGljZW5zZU1vZGlmaWVkLm1ldGhvZC5tYXggPSBwYXJzZUludChsaWNlbnNlLm1ldGhvZC5tYXgpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuc2F2ZUxpY2Vuc2UobGljZW5zZS5pZCwgbGljZW5zZU1vZGlmaWVkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG4gICAgLyoqXHJcbiAgICAgKiBEb2VzIHRoZSBhY3RpdmF0aW9uIG9mIHRoZSBjdXJyZW50IGxpY2Vuc2UgaWYgdGhpcyBpcyBub3QgYWN0aXZlXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGFjdGl2YXRlTGljZW5zZShsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmFjdGl2YXRlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnVGhlIGxpY2Vuc2Ugd2FzIGFjdGl2YXRlZCBhbmQgdGhlIGxpY2Vuc2Ugd2FzIGVtYWlsZWQuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBNYWtlIHRoZSByZXF1ZXN0IHRvIEltcG9ydCB0aGUgbGljZW5zZSwgaWYgZmFpbHMsIHRocm93cyBhbiBleGNlcHRpb24gdmlzaWJsZSBmb3IgdGhlIHVzZXIgdG8gdGFrZSBhY3Rpb25cclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgaW1wb3J0TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB2YXIgaGFzaCA9IHtcclxuICAgICAgICAgICAgZGF0YTogbGljZW5zZS5oYXNoXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkucmVxdWVzdEltcG9ydChoYXNoLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICBpZihkYXRhLnN0YXR1cyA9PT0gdGhpcy5zdGF0dXNTdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5IEltcG9ydGVkJ30pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkLiBSZXZpZXcgdGhlIHByb3ZpZGVkIExpY2Vuc2UgS2V5IGlzIGNvcnJlY3QuJ30pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3IoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5yZXZva2VMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBjYWxsYmFjayl7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBOb3RpY2VMaXN0IGZyb20gJy4vbGlzdC9Ob3RpY2VMaXN0LmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9Ob3RpY2VNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBFZGl0Tm90aWNlIGZyb20gJy4vZWRpdC9FZGl0Tm90aWNlLmpzJztcclxuXHJcbnZhciBOb3RpY2VNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLk5vdGljZU1hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ25vdGljZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ25vdGljZUxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdOb3RpY2UgQWRtaW5pc3RyYXRpb24nLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnQURNSU4nLCAnTk9USUNFJywgJ0xJU1QnXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvbm90aWNlL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvbGlzdC9Ob3RpY2VMaXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdOb3RpY2VMaXN0IGFzIG5vdGljZUxpc3QnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdOb3RpY2VNYW5hZ2VyU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCBOb3RpY2VNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdOb3RpY2VMaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdOb3RpY2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBOb3RpY2VMaXN0XSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignRWRpdE5vdGljZScsIFsnJGxvZycsICdOb3RpY2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRWRpdE5vdGljZV0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgTm90aWNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRWRpdE5vdGljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbm90aWNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy5hY3Rpb24gPSBwYXJhbXMuYWN0aW9uO1xyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHBhcmFtcy5hY3Rpb25UeXBlO1xyXG5cclxuICAgICAgICB0aGlzLmtlbmRvRWRpdG9yVG9vbHMgPSBbXHJcbiAgICAgICAgICAgICdmb3JtYXR0aW5nJywgJ2NsZWFuRm9ybWF0dGluZycsXHJcbiAgICAgICAgICAgICdmb250TmFtZScsICdmb250U2l6ZScsXHJcbiAgICAgICAgICAgICdqdXN0aWZ5TGVmdCcsICdqdXN0aWZ5Q2VudGVyJywgJ2p1c3RpZnlSaWdodCcsICdqdXN0aWZ5RnVsbCcsXHJcbiAgICAgICAgICAgICdib2xkJyxcclxuICAgICAgICAgICAgJ2l0YWxpYycsXHJcbiAgICAgICAgICAgICd2aWV3SHRtbCdcclxuICAgICAgICBdO1xyXG5cclxuICAgICAgICAvLyBDU1MgaGFzIG5vdCBjYW5jZWxpbmcgYXR0cmlidXRlcywgc28gaW5zdGVhZCBvZiByZW1vdmluZyBldmVyeSBwb3NzaWJsZSBIVE1MLCB3ZSBtYWtlIGVkaXRvciBoYXMgc2FtZSBjc3NcclxuICAgICAgICB0aGlzLmtlbmRvU3R5bGVzaGVldHMgPSBbXHJcbiAgICAgICAgICAgICcuLi9zdGF0aWMvZGlzdC9qcy92ZW5kb3JzL2Jvb3RzdHJhcC9kaXN0L2Nzcy9ib290c3RyYXAubWluLmNzcycsIC8vIE91cnQgY3VycmVudCBCb290c3RyYXAgY3NzXHJcbiAgICAgICAgICAgICcuLi9zdGF0aWMvZGlzdC9jc3MvVERTVE1MYXlvdXQubWluLmNzcycgLy8gT3JpZ2luYWwgVGVtcGxhdGUgQ1NTXHJcblxyXG4gICAgICAgIF07XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0VHlwZURhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbCA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICB0eXBlSWQ6IDAsXHJcbiAgICAgICAgICAgIGFjdGl2ZTogZmFsc2UsXHJcbiAgICAgICAgICAgIGh0bWxUZXh0OiAnJyxcclxuICAgICAgICAgICAgcmF3VGV4dDogJydcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIC8vIE9uIEVkaXRpb24gTW9kZSB3ZSBjYyB0aGUgbW9kZWwgYW5kIG9ubHkgdGhlIHBhcmFtcyB3ZSBuZWVkXHJcbiAgICAgICAgaWYocGFyYW1zLm5vdGljZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5pZCA9IHBhcmFtcy5ub3RpY2UuaWQ7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLnRpdGxlID0gcGFyYW1zLm5vdGljZS50aXRsZTtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwudHlwZUlkID0gcGFyYW1zLm5vdGljZS50eXBlLmlkO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5hY3RpdmUgPSBwYXJhbXMubm90aWNlLmFjdGl2ZTtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuaHRtbFRleHQgPSBwYXJhbXMubm90aWNlLmh0bWxUZXh0O1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0VHlwZURhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy50eXBlRGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge3R5cGVJZDogMSwgbmFtZTogJ1ByZWxvZ2luJ30sXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDIsIG5hbWU6ICdQb3N0bG9naW4nfVxyXG4gICAgICAgICAgICAvL3t0eXBlSWQ6IDMsIG5hbWU6ICdHZW5lcmFsJ30gRGlzYWJsZWQgdW50aWwgUGhhc2UgSUlcclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSB0aGUgU2VydmljZSBjYWxsIHRvIENyZWF0ZS9FZGl0IGEgbm90aWNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVOb3RpY2UoKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbyh0aGlzLmFjdGlvbiArICcgTm90aWNlIFJlcXVlc3RlZDogJywgdGhpcy5lZGl0TW9kZWwpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsLnJhd1RleHQgPSAkKCcja2VuZG8tZWRpdG9yLWNyZWF0ZS1lZGl0JykudGV4dCgpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsLnR5cGVJZCA9IHBhcnNlSW50KHRoaXMuZWRpdE1vZGVsLnR5cGVJZCk7XHJcbiAgICAgICAgaWYodGhpcy5hY3Rpb24gPT09IHRoaXMuYWN0aW9uVHlwZS5ORVcpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5jcmVhdGVOb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSBpZih0aGlzLmFjdGlvbiA9PT0gdGhpcy5hY3Rpb25UeXBlLkVESVQpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5lZGl0Tm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTm90aWNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIGRlbGV0ZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5kZWxldGVOb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBOb3RpY2VMaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIG5vdGljZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuXHJcbiAgICAgICAgdGhpcy5hY3Rpb25UeXBlID0ge1xyXG4gICAgICAgICAgICBORVc6ICdOZXcnLFxyXG4gICAgICAgICAgICBFRElUOiAnRWRpdCdcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLm5vdGljZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLm5vdGljZUdyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZSA9IG5vdGljZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cIm5vdGljZUxpc3Qub25FZGl0Q3JlYXRlTm90aWNlKG5vdGljZUxpc3QuYWN0aW9uVHlwZS5ORVcpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IENyZWF0ZSBOZXcgTm90aWNlPC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJub3RpY2VMaXN0LnJlbG9hZE5vdGljZUxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdpZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaHRtbFRleHQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cIm5vdGljZUxpc3Qub25FZGl0Q3JlYXRlTm90aWNlKG5vdGljZUxpc3QuYWN0aW9uVHlwZS5FRElULCB0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGl0bGUnLCB0aXRsZTogJ1RpdGxlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aXZlJywgdGl0bGU6ICdBY3RpdmUnLCB0ZW1wbGF0ZTogJyNpZihhY3RpdmUpIHsjIFllcyAjfSBlbHNlIHsjIE5vICN9IycgfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmdldE5vdGljZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHNvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICBmaWVsZDogJ3RpdGxlJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE9wZW4gYSBkaWFsb2cgd2l0aCB0aGUgQmFzaWMgRm9ybSB0byByZXF1ZXN0IGEgTmV3IE5vdGljZVxyXG4gICAgICovXHJcbiAgICBvbkVkaXRDcmVhdGVOb3RpY2UoYWN0aW9uLCBub3RpY2UpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2VkaXQvRWRpdE5vdGljZS5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0VkaXROb3RpY2UgYXMgZWRpdE5vdGljZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IG5vdGljZSAmJiBub3RpY2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgYWN0aW9uOiBhY3Rpb24sIG5vdGljZTogZGF0YUl0ZW0sIGFjdGlvblR5cGU6IHRoaXMuYWN0aW9uVHlwZX07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobm90aWNlKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oYWN0aW9uICsgJyBOb3RpY2U6ICcsIG5vdGljZSk7XHJcbiAgICAgICAgICAgIC8vIEFmdGVyIGEgbmV3IHZhbHVlIGlzIGFkZGVkLCBsZXRzIHRvIHJlZnJlc2ggdGhlIEdyaWRcclxuICAgICAgICAgICAgdGhpcy5yZWxvYWROb3RpY2VMaXN0KCk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZWxvYWROb3RpY2VMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubm90aWNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlR3JpZC5kYXRhU291cmNlLnJlYWQoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBOb3RpY2VNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcblxyXG4gICAgICAgIHRoaXMuVFlQRSA9IHtcclxuICAgICAgICAgICAgJzEnOiAnUHJlbG9naW4nLFxyXG4gICAgICAgICAgICAnMic6ICdQb3N0bG9naW4nLFxyXG4gICAgICAgICAgICAnMyc6ICdHZW5lcmFsJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdOb3RpY2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXROb3RpY2VMaXN0KGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHZhciBub3RpY2VMaXN0ID0gW107XHJcbiAgICAgICAgICAgIHRyeSB7XHJcbiAgICAgICAgICAgICAgICAvLyBWZXJpZnkgdGhlIExpc3QgcmV0dXJucyB3aGF0IHdlIGV4cGVjdCBhbmQgd2UgY29udmVydCBpdCB0byBhbiBBcnJheSB2YWx1ZVxyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSAmJiBkYXRhLm5vdGljZXMpIHtcclxuICAgICAgICAgICAgICAgICAgICBub3RpY2VMaXN0ID0gZGF0YS5ub3RpY2VzO1xyXG4gICAgICAgICAgICAgICAgICAgIGlmIChub3RpY2VMaXN0ICYmIG5vdGljZUxpc3QubGVuZ3RoID4gMCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IG5vdGljZUxpc3QubGVuZ3RoOyBpID0gaSArIDEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5vdGljZUxpc3RbaV0udHlwZSA9IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogbm90aWNlTGlzdFtpXS50eXBlSWQsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogdGhpcy5UWVBFW25vdGljZUxpc3RbaV0udHlwZUlkXVxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgfTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGRlbGV0ZSBub3RpY2VMaXN0W2ldLnR5cGVJZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSBjYXRjaChlKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxvZy5lcnJvcignRXJyb3IgcGFyc2luZyB0aGUgTm90aWNlIExpc3QnLCBlKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2sobm90aWNlTGlzdCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTm90aWNlIHBhc3NpbmcgcGFyYW1zXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTm90aWNlKG5vdGljZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTm90aWNlIHNob3VsZCBoYXZlIHRoZSBJRCBpbiBvcmRlciB0byBlZGl0IHRoZSBOb3RpY2VcclxuICAgICAqIEBwYXJhbSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBlZGl0Tm90aWNlKG5vdGljZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZWRpdE5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE5vdGljZSBzaG91bGQgaGF2ZSB0aGUgSUQgaW4gb3JkZXIgdG8gZGVsZXRlIHRoZSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBkZWxldGVOb3RpY2Uobm90aWNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZGVsZXRlTm90aWNlKG5vdGljZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgVGFza01hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9UYXNrTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJDb250cm9sbGVyIGZyb20gJy4vbGlzdC9UYXNrTWFuYWdlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJFZGl0IGZyb20gJy4vZWRpdC9UYXNrTWFuYWdlckVkaXQuanMnO1xyXG5cclxudmFyIFRhc2tNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLlRhc2tNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAnZm9ybWx5Q29uZmlnUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCBmb3JtbHlDb25maWdQcm92aWRlcikge1xyXG5cclxuICAgIGZvcm1seUNvbmZpZ1Byb3ZpZGVyLnNldFR5cGUoe1xyXG4gICAgICAgIG5hbWU6ICdjdXN0b20nLFxyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnY3VzdG9tLmh0bWwnXHJcbiAgICB9KTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ3Rhc2tMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTXkgVGFzayBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ1Rhc2sgTWFuYWdlciddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy90YXNrL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2xpc3QvVGFza01hbmFnZXJDb250YWluZXIuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyQ29udHJvbGxlciBhcyB0YXNrTWFuYWdlcidcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCd0YXNrTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgVGFza01hbmFnZXJTZXJ2aWNlXSk7XHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdUYXNrTWFuYWdlckNvbnRyb2xsZXInLCBbJyRsb2cnLCAndGFza01hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsIFRhc2tNYW5hZ2VyQ29udHJvbGxlcl0pO1xyXG5UYXNrTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdUYXNrTWFuYWdlckVkaXQnLCBbJyRsb2cnLCBUYXNrTWFuYWdlckVkaXRdKTtcclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBUYXNrTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMvMTEvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlckVkaXQge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHRhc2tNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcblxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjAvMjAxNS5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyQ29udHJvbGxlciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgdGFza01hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLm1vZHVsZSA9ICdUYXNrTWFuYWdlcic7XHJcbiAgICAgICAgdGhpcy50YXNrTWFuYWdlclNlcnZpY2UgPSB0YXNrTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy50YXNrR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmV2ZW50RGF0YVNvdXJjZSA9IFtdO1xyXG5cclxuICAgICAgICAvLyBJbml0IENsYXNzXHJcbiAgICAgICAgdGhpcy5nZXRFdmVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVGFza01hbmFnZXIgQ29udHJvbGxlciBJbnN0YW5jZWQnKTtcclxuICAgICAgICB0aGlzLmluaXRGb3JtKCk7XHJcblxyXG4gICAgfVxyXG5cclxuICAgIG9wZW5Nb2RhbERlbW8oKSB7XHJcblxyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJ2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2VkaXQvVGFza01hbmFnZXJFZGl0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnVGFza01hbmFnZXJFZGl0JyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgaXRlbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4gWycxJywnYTInLCdnZyddO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKHNlbGVjdGVkSXRlbSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmRlYnVnKHNlbGVjdGVkSXRlbSk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdNb2RhbCBkaXNtaXNzZWQgYXQ6ICcgKyBuZXcgRGF0ZSgpKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMudGFza0dyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBncm91cGFibGU6IHRydWUsXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFt7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGFzaycsIHRpdGxlOiAnVGFzayd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZGVzY3JpcHRpb24nLCB0aXRsZTogJ0Rlc2NyaXB0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldE5hbWUnLCB0aXRsZTogJ0Fzc2V0IE5hbWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Fzc2V0VHlwZScsIHRpdGxlOiAnQXNzZXQgVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndXBkYXRlZCcsIHRpdGxlOiAnVXBkYXRlZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZHVlJywgdGl0bGU6ICdEdWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NpZ25lZFRvJywgdGl0bGU6ICdBc3NpZ25lZCBUbyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGVhbScsIHRpdGxlOiAnVGVhbSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2F0ZWdvcnknLCB0aXRsZTogJ0NhdGVnb3J5J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdWMnLCB0aXRsZTogJ1N1Yy4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Njb3JlJywgdGl0bGU6ICdTY29yZSd9XSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgLyp0aGlzLnRhc2tNYW5hZ2VyU2VydmljZS50ZXN0U2VydmljZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTsqL1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RXZlbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuZXZlbnREYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMSwgZXZlbnROYW1lOiAnQWxsJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiAyLCBldmVudE5hbWU6ICdCdWlsZG91dCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMywgZXZlbnROYW1lOiAnRFItRVAnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDQsIGV2ZW50TmFtZTogJ00xLVBoeXNpY2FsJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIG9uRXJyb3JIYXBwZW5zKCkge1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlLmZhaWxDYWxsKGZ1bmN0aW9uICgpIHtcclxuXHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgaW5pdEZvcm0oKSB7XHJcbiAgICAgICAgdGhpcy51c2VyRmllbGRzID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdlbWFpbCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnaW5wdXQnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ2VtYWlsJyxcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ0VtYWlsIGFkZHJlc3MnLFxyXG4gICAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyOiAnRW50ZXIgZW1haWwnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGtleTogJ3Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdpbnB1dCcsXHJcbiAgICAgICAgICAgICAgICB0ZW1wbGF0ZU9wdGlvbnM6IHtcclxuICAgICAgICAgICAgICAgICAgICB0eXBlOiAncGFzc3dvcmQnLFxyXG4gICAgICAgICAgICAgICAgICAgIGxhYmVsOiAnUGFzc3dvcmQnLFxyXG4gICAgICAgICAgICAgICAgICAgIHBsYWNlaG9sZGVyOiAnUGFzc3dvcmQnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGtleTogJ2ZpbGUnLFxyXG4gICAgICAgICAgICAgICAgdHlwZTogJ2ZpbGUnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdGaWxlIGlucHV0JyxcclxuICAgICAgICAgICAgICAgICAgICBkZXNjcmlwdGlvbjogJ0V4YW1wbGUgYmxvY2stbGV2ZWwgaGVscCB0ZXh0IGhlcmUnLFxyXG4gICAgICAgICAgICAgICAgICAgIHVybDogJ2h0dHBzOi8vZXhhbXBsZS5jb20vdXBsb2FkJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdjaGVja2VkJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdjaGVja2JveCcsXHJcbiAgICAgICAgICAgICAgICB0ZW1wbGF0ZU9wdGlvbnM6IHtcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ0NoZWNrIG1lIG91dCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDIyLzA3LzE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgUmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSBSZXN0U2VydmljZUhhbmRsZXI7XHJcblxyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdUYXNrTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZmFpbENhbGwoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlJlc291cmNlU2VydmljZUhhbmRsZXIoKS5nZXRTVkcoKTtcclxuICAgIH1cclxuXHJcbiAgICB0ZXN0U2VydmljZShjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuVGFza1NlcnZpY2VIYW5kbGVyKCkuZ2V0RmVlZHMoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzMvMjAxNi5cclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnXHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBGb3JtVmFsaWRhdG9yIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuXHJcbiAgICAgICAgLy8gSlMgZG9lcyBhIGFyZ3VtZW50IHBhc3MgYnkgcmVmZXJlbmNlXHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gbnVsbDtcclxuICAgICAgICAvLyBBIGNvcHkgd2l0aG91dCByZWZlcmVuY2UgZnJvbSB0aGUgb3JpZ2luYWwgb2JqZWN0XHJcbiAgICAgICAgdGhpcy5vcmlnaW5hbERhdGEgPSBudWxsO1xyXG4gICAgICAgIC8vIEEgQ0MgYXMgSlNPTiBmb3IgY29tcGFyaXNvbiBQdXJwb3NlXHJcbiAgICAgICAgdGhpcy5vYmplY3RBc0pTT04gPSBudWxsO1xyXG5cclxuXHJcbiAgICAgICAgLy8gT25seSBmb3IgTW9kYWwgV2luZG93c1xyXG4gICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSBmYWxzZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cclxuICAgICAgICBpZiAoJHNjb3BlLiRvbikge1xyXG4gICAgICAgICAgICAkc2NvcGUuJG9uKCdtb2RhbC5jbG9zaW5nJywgKGV2ZW50LCByZWFzb24sIGNsb3NlZCk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2xvc2VEaWFsb2coZXZlbnQsIHJlYXNvbiwgY2xvc2VkKVxyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBTYXZlcyB0aGUgRm9ybSBpbiAzIGluc3RhbmNlcywgb25lIHRvIGtlZXAgdHJhY2sgb2YgdGhlIG9yaWdpbmFsIGRhdGEsIG90aGVyIGlzIHRoZSBjdXJyZW50IG9iamVjdCBhbmRcclxuICAgICAqIGEgSlNPTiBmb3JtYXQgZm9yIGNvbXBhcmlzb24gcHVycG9zZVxyXG4gICAgICogQHBhcmFtIG5ld09iamVjdEluc3RhbmNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVGb3JtKG5ld09iamVjdEluc3RhbmNlKSB7XHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gbmV3T2JqZWN0SW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5vcmlnaW5hbERhdGEgPSBhbmd1bGFyLmNvcHkobmV3T2JqZWN0SW5zdGFuY2UsIHRoaXMub3JpZ2luYWxEYXRhKTtcclxuICAgICAgICB0aGlzLm9iamVjdEFzSlNPTiA9IGFuZ3VsYXIudG9Kc29uKG5ld09iamVjdEluc3RhbmNlKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEdldCB0aGUgQ3VycmVudCBPYmplY3Qgb24gaGlzIHJlZmVyZW5jZSBGb3JtYXRcclxuICAgICAqIEByZXR1cm5zIHtudWxsfCp9XHJcbiAgICAgKi9cclxuICAgIGdldEZvcm0oKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuY3VycmVudE9iamVjdDtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEdldCB0aGUgT2JqZWN0IGFzIEpTT04gZnJvbSB0aGUgT3JpZ2luYWwgRGF0YVxyXG4gICAgICogQHJldHVybnMge251bGx8c3RyaW5nfHVuZGVmaW5lZHxzdHJpbmd8Kn1cclxuICAgICAqL1xyXG4gICAgZ2V0Rm9ybUFzSlNPTigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5vYmplY3RBc0pTT047XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKlxyXG4gICAgICogQHBhcmFtIG9iamV0VG9SZXNldCBvYmplY3QgdG8gcmVzZXRcclxuICAgICAqIEBwYXJhbSBvblJlc2V0Rm9ybSBjYWxsYmFja1xyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIHJlc2V0Rm9ybShvblJlc2V0Rm9ybSkge1xyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IGFuZ3VsYXIuY29weSh0aGlzLm9yaWdpbmFsRGF0YSwgdGhpcy5jdXJyZW50T2JqZWN0KTtcclxuICAgICAgICB0aGlzLnNhZmVBcHBseSgpO1xyXG5cclxuICAgICAgICBpZihvblJlc2V0Rm9ybSkge1xyXG4gICAgICAgICAgICByZXR1cm4gb25SZXNldEZvcm0oKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWYWxpZGF0ZXMgaWYgdGhlIGN1cnJlbnQgb2JqZWN0IGRpZmZlcnMgZnJvbSB3aGVyZSBpdCB3YXMgb3JpZ2luYWxseSBzYXZlZFxyXG4gICAgICogQHJldHVybnMge2Jvb2xlYW59XHJcbiAgICAgKi9cclxuICAgIGlzRGlydHkoKSB7XHJcbiAgICAgICAgdmFyIG5ld09iamVjdEluc3RhbmNlID0gYW5ndWxhci50b0pzb24odGhpcy5jdXJyZW50T2JqZWN0KTtcclxuICAgICAgICByZXR1cm4gbmV3T2JqZWN0SW5zdGFuY2UgIT09IHRoaXMuZ2V0Rm9ybUFzSlNPTigpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhpcyBmdW5jdGlvbiBpcyBvbmx5IGF2YWlsYWJsZSB3aGVuIHRoZSBGb3JtIGlzIGJlaW5nIGNhbGxlZCBmcm9tIGEgRGlhbG9nIFBvcFVwXHJcbiAgICAgKi9cclxuICAgIG9uQ2xvc2VEaWFsb2coZXZlbnQsIHJlYXNvbiwgY2xvc2VkKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnbW9kYWwuY2xvc2luZzogJyArIChjbG9zZWQgPyAnY2xvc2UnIDogJ2Rpc21pc3MnKSArICcoJyArIHJlYXNvbiArICcpJyk7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNEaXJ0eSgpICYmIHJlYXNvbiAhPT0gJ2NhbmNlbC1jb25maXJtYXRpb24nICYmIHR5cGVvZiByZWFzb24gIT09ICdvYmplY3QnKSB7XHJcbiAgICAgICAgICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XHJcbiAgICAgICAgICAgIHRoaXMuY29uZmlybUNsb3NlRm9ybSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEEgQ29uZmlybWF0aW9uIERpYWxvZyB3aGVuIHRoZSBpbmZvcm1hdGlvbiBjYW4gYmUgbG9zdFxyXG4gICAgICogQHBhcmFtIGV2ZW50XHJcbiAgICAgKi9cclxuICAgIGNvbmZpcm1DbG9zZUZvcm0oZXZlbnQpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgbWVzc2FnZTogJ0NoYW5nZXMgeW91IG1hZGUgbWF5IG5vdCBiZSBzYXZlZC4gRG8geW91IHdhbnQgdG8gY29udGludWU/J1xyXG4gICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwtY29uZmlybWF0aW9uJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBVdGlsIHRvIGNhbGwgc2FmZSBpZiByZXF1aXJlZFxyXG4gICAgICogQHBhcmFtIGZuXHJcbiAgICAgKi9cclxuICAgIHNhZmVBcHBseShmbikge1xyXG4gICAgICAgIHZhciBwaGFzZSA9IHRoaXMuc2NvcGUuJHJvb3QuJCRwaGFzZTtcclxuICAgICAgICBpZihwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgICAgICBpZihmbiAmJiAodHlwZW9mKGZuKSA9PT0gJ2Z1bmN0aW9uJykpIHtcclxuICAgICAgICAgICAgICAgIGZuKCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICB0aGlzLnNjb3BlLiRhcHBseShmbik7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVXRpbCB0byBSZXNldCBhIERyb3Bkb3duIGxpc3Qgb24gS2VuZG9cclxuICAgICAqL1xyXG5cclxuICAgIHJlc2V0RHJvcERvd24oc2VsZWN0b3JJbnN0YW5jZSwgc2VsZWN0ZWRJZCwgZm9yY2UpIHtcclxuICAgICAgICBpZihzZWxlY3Rvckluc3RhbmNlICYmIHNlbGVjdG9ySW5zdGFuY2UuZGF0YUl0ZW1zKSB7XHJcbiAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2UuZGF0YUl0ZW1zKCkuZm9yRWFjaCgodmFsdWUsIGluZGV4KSA9PiB7XHJcbiAgICAgICAgICAgICAgICBpZihzZWxlY3RlZElkID09PSB2YWx1ZS5pZCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2Uuc2VsZWN0KGluZGV4KTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBpZihmb3JjZSkge1xyXG4gICAgICAgICAgICAgICAgc2VsZWN0b3JJbnN0YW5jZS50cmlnZ2VyKCdjaGFuZ2UnKTtcclxuICAgICAgICAgICAgICAgIHRoaXMuc2FmZUFwcGx5KCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIzLzIwMTUuXHJcbiAqIEltcGxlbWVudHMgUlggT2JzZXJ2YWJsZSB0byBkaXNwb3NlIGFuZCB0cmFjayBiZXR0ZXIgZWFjaCBjYWxsIHRvIHRoZSBzZXJ2ZXJcclxuICogVGhlIE9ic2VydmVyIHN1YnNjcmliZSBhIHByb21pc2UuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RIYW5kbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKHJ4KSB7XHJcbiAgICAgICAgdGhpcy5yeCA9IHJ4O1xyXG4gICAgICAgIHRoaXMucHJvbWlzZSA9IFtdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2FsbGVkIGZyb20gUmVzdFNlcnZpY2VIYW5kbGVyLnN1YnNjcmliZVJlcXVlc3RcclxuICAgICAqIGl0IHZlcmlmeSB0aGF0IHRoZSBjYWxsIGlzIGJlaW5nIGRvbmUgdG8gdGhlIHNlcnZlciBhbmQgcmV0dXJuIGEgcHJvbWlzZVxyXG4gICAgICogQHBhcmFtIHJlcXVlc3RcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICBzdWJzY3JpYmVSZXF1ZXN0KHJlcXVlc3QsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHZhciByeE9ic2VydmFibGUgPSB0aGlzLnJ4Lk9ic2VydmFibGUuZnJvbVByb21pc2UocmVxdWVzdCk7XHJcbiAgICAgICAgLy8gVmVyaWZ5IGlzIG5vdCBhIGR1cGxpY2F0ZSBjYWxsXHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgdGhpcy5jYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBTdWJzY3JpYmUgdGhlIHJlcXVlc3RcclxuICAgICAgICB2YXIgcmVzdWx0U3Vic2NyaWJlID0gdGhpcy5hZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgIGlmIChyZXN1bHRTdWJzY3JpYmUgJiYgcmVzdWx0U3Vic2NyaWJlLmlzU3RvcHBlZCkge1xyXG4gICAgICAgICAgICAvLyBBbiBlcnJvciBoYXBwZW5zLCB0cmFja2VkIGJ5IEh0dHBJbnRlcmNlcHRvckludGVyZmFjZVxyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGFkZFN1YnNjcmliZShyeE9ic2VydmFibGUsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdID0gcnhPYnNlcnZhYmxlLnN1YnNjcmliZShcclxuICAgICAgICAgICAgKHJlc3BvbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5vblN1YnNjcmliZWRTdWNjZXNzKHJlc3BvbnNlLCByeE9ic2VydmFibGUsIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIChlcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE5PLU9QIFN1YnNjcmliZSBjb21wbGV0ZWRcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgIH1cclxuXHJcbiAgICBjYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICAgICAgcnhPYnNlcnZhYmxlLmRpc3Bvc2UoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIHJldHVybiAocnhPYnNlcnZhYmxlICYmIHJ4T2JzZXJ2YWJsZS5fcCAmJiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKG9uU3VjY2Vzcyl7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MocmVzcG9uc2UuZGF0YSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhyb3dzIGltbWVkaWF0ZWx5IGVycm9yIHdoZW4gdGhlIHBldGl0aW9uIGNhbGwgaXMgd3JvbmdcclxuICAgICAqIG9yIHdpdGggYSBkZWxheSBpZiB0aGUgY2FsbCBpcyB2YWxpZFxyXG4gICAgICogQHBhcmFtIGVycm9yXHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgb25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcikge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICAgICAgaWYob25FcnJvcil7XHJcbiAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHt9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IFJlc3RTZXJ2aWNlSGFuZGxlciBmcm9tICcuL1Jlc3RTZXJ2aWNlSGFuZGxlci5qcyc7XHJcblxyXG52YXIgUmVzdEFQSU1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5SZXN0QVBJTW9kdWxlJyxbXSk7XHJcblxyXG5SZXN0QVBJTW9kdWxlLnNlcnZpY2UoJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFsnJGxvZycsICckaHR0cCcsICckcmVzb3VyY2UnLCAncngnLCBSZXN0U2VydmljZUhhbmRsZXJdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IFJlc3RBUElNb2R1bGU7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMDgvMTUuXHJcbiAqIEl0IGFic3RyYWN0IGVhY2ggb25lIG9mIHRoZSBleGlzdGluZyBjYWxsIHRvIHRoZSBBUEksIGl0IHNob3VsZCBvbmx5IGNvbnRhaW5zIHRoZSBjYWxsIGZ1bmN0aW9ucyBhbmQgcmVmZXJlbmNlXHJcbiAqIHRvIHRoZSBjYWxsYmFjaywgbm8gbG9naWMgYXQgYWxsLlxyXG4gKlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgUmVxdWVzdEhhbmRsZXIgZnJvbSAnLi9SZXF1ZXN0SGFuZGxlci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXN0U2VydmljZUhhbmRsZXIge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJGh0dHAsICRyZXNvdXJjZSwgcngpIHtcclxuICAgICAgICB0aGlzLnJ4ID0gcng7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuaHR0cCA9ICRodHRwO1xyXG4gICAgICAgIHRoaXMucmVzb3VyY2UgPSAkcmVzb3VyY2U7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVycygpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdSZXN0U2VydmljZSBMb2FkZWQnKTtcclxuICAgICAgICB0aGlzLnJlcSA9IHtcclxuICAgICAgICAgICAgbWV0aG9kOiAnJyxcclxuICAgICAgICAgICAgdXJsOiAnJyxcclxuICAgICAgICAgICAgaGVhZGVyczoge1xyXG4gICAgICAgICAgICAgICAgJ0NvbnRlbnQtVHlwZSc6ICdhcHBsaWNhdGlvbi9qc29uO2NoYXJzZXQ9VVRGLTgnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGE6IFtdXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlSGVhZGVycygpIHtcclxuICAgICAgICB0aGlzLmh0dHAuZGVmYXVsdHMuaGVhZGVycy5wb3N0WydDb250ZW50LVR5cGUnXSA9ICdhcHBsaWNhdGlvbi94LXd3dy1mb3JtLXVybGVuY29kZWQnO1xyXG4gICAgfVxyXG5cclxuICAgIFRhc2tTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRGZWVkczogKGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJ3Rlc3QvbW9ja3VwRGF0YS9UYXNrTWFuYWdlci90YXNrTWFuYWdlckxpc3QuanNvbicpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldExpY2Vuc2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlcycpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldFByb2plY3REYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9wcm9qZWN0JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdDogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYXBwbHlMaWNlbnNlOiAgKGxpY2Vuc2VJZCwgZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvbG9hZCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRIYXNoQ29kZTogIChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvaGFzaCc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgLy8tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gICAgICAgICAgICByZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGVtYWlsUmVxdWVzdDogKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkZWxldGVMaWNlbnNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgbGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICByZXF1ZXN0SW1wb3J0OiAgKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvcmVxdWVzdCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRMaWNlbnNlTGlzdDogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL21hbmFnZXIvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvcHJvamVjdCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEtleUNvZGU6ICAobGljZW5zZUlkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdHRVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcva2V5JztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzYXZlTGljZW5zZTogKGxpY2Vuc2VpZCwgbGljZW5zZU1vZGlmaWVkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQVVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VpZDtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBsaWNlbnNlTW9kaWZpZWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgcmV2b2tlTGljZW5zZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGVMaWNlbnNlOiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIG5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXROb3RpY2VMaXN0OiAob25TdWNjZXNzKSA9PiB7IC8vIHJlYWwgd3MgZXhhbXBsZVxyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL25vdGljZXMnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY3JlYXRlTm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZWRpdE5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BVVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGVsZXRlTm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3Mvbm90aWNlcy8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIEVTNiBJbnRlcmNlcHRvciBjYWxscyBpbm5lciBtZXRob2RzIGluIGEgZ2xvYmFsIHNjb3BlLCB0aGVuIHRoZSBcInRoaXNcIiBpcyBiZWluZyBsb3N0XHJcbiAqIGluIHRoZSBkZWZpbml0aW9uIG9mIHRoZSBDbGFzcyBmb3IgaW50ZXJjZXB0b3JzIG9ubHlcclxuICogVGhpcyBpcyBhIGludGVyZmFjZSB0aGF0IHRha2UgY2FyZSBvZiB0aGUgaXNzdWUuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IC8qIGludGVyZmFjZSovIGNsYXNzIEh0dHBJbnRlcmNlcHRvciB7XHJcbiAgICBjb25zdHJ1Y3RvcihtZXRob2RUb0JpbmQpIHtcclxuICAgICAgICAvLyBJZiBub3QgbWV0aG9kIHRvIGJpbmQsIHdlIGFzc3VtZSBvdXIgaW50ZXJjZXB0b3IgaXMgdXNpbmcgYWxsIHRoZSBpbm5lciBmdW5jdGlvbnNcclxuICAgICAgICBpZighbWV0aG9kVG9CaW5kKSB7XHJcbiAgICAgICAgICAgIFsncmVxdWVzdCcsICdyZXF1ZXN0RXJyb3InLCAncmVzcG9uc2UnLCAncmVzcG9uc2VFcnJvciddXHJcbiAgICAgICAgICAgICAgICAuZm9yRWFjaCgobWV0aG9kKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpc1ttZXRob2RdKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXNbbWV0aG9kXSA9IHRoaXNbbWV0aG9kXS5iaW5kKHRoaXMpO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIC8vIG1ldGhvZFRvQmluZCByZWZlcmVuY2UgdG8gYSBzaW5nbGUgY2hpbGQgY2xhc3NcclxuICAgICAgICAgICAgdGhpc1ttZXRob2RUb0JpbmRdID0gdGhpc1ttZXRob2RUb0JpbmRdLmJpbmQodGhpcyk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIFVzZSB0aGlzIG1vZHVsZSB0byBtb2RpZnkgYW55dGhpbmcgcmVsYXRlZCB0byB0aGUgSGVhZGVycyBhbmQgUmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5cclxuXHJcbnZhciBIVFRQTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkhUVFBNb2R1bGUnLCBbJ25nUmVzb3VyY2UnXSkuY29uZmlnKFsnJGh0dHBQcm92aWRlcicsIGZ1bmN0aW9uKCRodHRwUHJvdmlkZXIpe1xyXG5cclxuICAgIC8vaW5pdGlhbGl6ZSBnZXQgaWYgbm90IHRoZXJlXHJcbiAgICBpZiAoISRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXQpIHtcclxuICAgICAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0ID0ge307XHJcbiAgICB9XHJcblxyXG4gICAgLy9EaXNhYmxlIElFIGFqYXggcmVxdWVzdCBjYWNoaW5nXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydJZi1Nb2RpZmllZC1TaW5jZSddID0gJ01vbiwgMjYgSnVsIDE5OTcgMDU6MDA6MDAgR01UJztcclxuICAgIC8vIGV4dHJhXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydDYWNoZS1Db250cm9sJ10gPSAnbm8tY2FjaGUnO1xyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnUHJhZ21hJ10gPSAnbm8tY2FjaGUnO1xyXG5cclxuXHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlcXVlc3RcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlc3BvbnNlXHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcblxyXG5cclxufV0pO1xyXG5cclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIVFRQTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIEl0IGltcGxlbWVudCBhbiBhYnN0cmFjdCBjYWxsIHRvIEhUVFAgSW50ZXJjZXB0b3JzIHRvIG1hbmFnZSBlcnJvciBoYW5kbGVyXHJcbiAqIFNvbWV0aW1lcyBhIHJlcXVlc3QgY2FuJ3QgYmUgc2VudCBvciBpdCBpcyByZWplY3RlZCBieSBhbiBpbnRlcmNlcHRvci5cclxuICogUmVxdWVzdCBlcnJvciBpbnRlcmNlcHRvciBjYXB0dXJlcyByZXF1ZXN0cyB0aGF0IGhhdmUgYmVlbiBjYW5jZWxlZCBieSBhIHByZXZpb3VzIHJlcXVlc3QgaW50ZXJjZXB0b3IuXHJcbiAqIEl0IGNhbiBiZSB1c2VkIGluIG9yZGVyIHRvIHJlY292ZXIgdGhlIHJlcXVlc3QgYW5kIHNvbWV0aW1lcyB1bmRvIHRoaW5ncyB0aGF0IGhhdmUgYmVlbiBzZXQgdXAgYmVmb3JlIGEgcmVxdWVzdCxcclxuICogbGlrZSByZW1vdmluZyBvdmVybGF5cyBhbmQgbG9hZGluZyBpbmRpY2F0b3JzLCBlbmFibGluZyBidXR0b25zIGFuZCBmaWVsZHMgYW5kIHNvIG9uLlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3JlcXVlc3RFcnJvcicpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdEVycm9yKHJlamVjdGlvbikge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vaWYgKGNhblJlY292ZXIocmVqZWN0aW9uKSkge1xyXG4gICAgICAgIC8vICAgIHJldHVybiByZXNwb25zZU9yTmV3UHJvbWlzZVxyXG4gICAgICAgIC8vfVxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnEucmVqZWN0KHJlamVjdGlvbik7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuRXJyb3IoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSXQgaW1wbGVtZW50IGFuIGFic3RyYWN0IGNhbGwgdG8gSFRUUCBJbnRlcmNlcHRvcnMgdG8gbWFuYWdlIG9ubHkgcmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXF1ZXN0Jyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdChjb25maWcpIHtcclxuICAgICAgICAvLyBXZSBjYW4gYWRkIGhlYWRlcnMgaWYgb24gdGhlIGluY29taW5nIHJlcXVlc3QgbWFkZSBpdCB3ZSBoYXZlIHRoZSB0b2tlbiBpbnNpZGVcclxuICAgICAgICAvLyBkZWZpbmVkIGJ5IHNvbWUgY29uZGl0aW9uc1xyXG4gICAgICAgIC8vY29uZmlnLmhlYWRlcnNbJ3gtc2Vzc2lvbi10b2tlbiddID0gbXkudG9rZW47XHJcblxyXG4gICAgICAgIGNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KGNvbmZpZyk7XHJcblxyXG4gICAgICAgIHJldHVybiBjb25maWcgfHwgdGhpcy5xLndoZW4oY29uZmlnKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXF1ZXN0KCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIElmIGJhY2tlbmQgY2FsbCBmYWlscyBvciBpdCBtaWdodCBiZSByZWplY3RlZCBieSBhIHJlcXVlc3QgaW50ZXJjZXB0b3Igb3IgYnkgYSBwcmV2aW91cyByZXNwb25zZSBpbnRlcmNlcHRvcjtcclxuICogSW4gdGhvc2UgY2FzZXMsIHJlc3BvbnNlIGVycm9yIGludGVyY2VwdG9yIGNhbiBoZWxwIHVzIHRvIHJlY292ZXIgdGhlIGJhY2tlbmQgY2FsbC5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZUVycm9yJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzcG9uc2VFcnJvcihyZWplY3Rpb24pIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvL2lmIChjYW5SZWNvdmVyKHJlamVjdGlvbikpIHtcclxuICAgICAgICAvLyAgICByZXR1cm4gcmVzcG9uc2VPck5ld1Byb21pc2VcclxuICAgICAgICAvLyB9XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMucS5yZWplY3QocmVqZWN0aW9uKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5FcnJvcigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBUaGlzIG1ldGhvZCBpcyBjYWxsZWQgcmlnaHQgYWZ0ZXIgJGh0dHAgcmVjZWl2ZXMgdGhlIHJlc3BvbnNlIGZyb20gdGhlIGJhY2tlbmQsXHJcbiAqIHNvIHlvdSBjYW4gbW9kaWZ5IHRoZSByZXNwb25zZSBhbmQgbWFrZSBvdGhlciBhY3Rpb25zLiBUaGlzIGZ1bmN0aW9uIHJlY2VpdmVzIGEgcmVzcG9uc2Ugb2JqZWN0IGFzIGEgcGFyYW1ldGVyXHJcbiAqIGFuZCBoYXMgdG8gcmV0dXJuIGEgcmVzcG9uc2Ugb2JqZWN0IG9yIGEgcHJvbWlzZS4gVGhlIHJlc3BvbnNlIG9iamVjdCBpbmNsdWRlc1xyXG4gKiB0aGUgcmVxdWVzdCBjb25maWd1cmF0aW9uLCBoZWFkZXJzLCBzdGF0dXMgYW5kIGRhdGEgdGhhdCByZXR1cm5lZCBmcm9tIHRoZSBiYWNrZW5kLlxyXG4gKiBSZXR1cm5pbmcgYW4gaW52YWxpZCByZXNwb25zZSBvYmplY3Qgb3IgcHJvbWlzZSB0aGF0IHdpbGwgYmUgcmVqZWN0ZWQsIHdpbGwgbWFrZSB0aGUgJGh0dHAgY2FsbCB0byBmYWlsLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZScpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXNwb25zZShyZXNwb25zZSkge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBzdWNjZXNzXHJcblxyXG4gICAgICAgIHJlc3BvbnNlLmNvbmZpZy5yZXNwb25zZVRpbWVzdGFtcCA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZXNwb25zZSk7XHJcbiAgICAgICAgcmV0dXJuIHJlc3BvbnNlIHx8IHRoaXMucS53aGVuKHJlc3BvbnNlKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXNwb25zZSgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG59XHJcblxyXG4iXX0=
