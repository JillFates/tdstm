(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
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

},{"angular":"angular","clipboard":3}],2:[function(require,module,exports){
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
},{"select":8}],3:[function(require,module,exports){
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
},{"./clipboard-action":2,"good-listener":7,"tiny-emitter":9}],4:[function(require,module,exports){
var DOCUMENT_NODE_TYPE = 9;

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
    while (element && element.nodeType !== DOCUMENT_NODE_TYPE) {
        if (element.matches(selector)) return element;
        element = element.parentNode;
    }
}

module.exports = closest;

},{}],5:[function(require,module,exports){
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

},{"./closest":4}],6:[function(require,module,exports){
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

},{}],7:[function(require,module,exports){
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

},{"./is":6,"delegate":5}],8:[function(require,module,exports){
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

// Modules


var ProviderCore = {};

var TDSTM = angular.module('TDSTM', ['ngSanitize', 'ngResource', 'ngAnimate', 'pascalprecht.translate', // 'angular-translate'
'ui.router', 'ngclipboard', 'kendo.directives', 'rx', 'ui.bootstrap', _HTTPModule2.default.name, _RestAPIModule2.default.name, _HeaderModule2.default.name, _TaskManagerModule2.default.name, _LicenseAdminModule2.default.name, _LicenseManagerModule2.default.name, _NoticeManagerModule2.default.name]).config(['$logProvider', '$rootScopeProvider', '$compileProvider', '$controllerProvider', '$provide', '$httpProvider', '$translateProvider', '$translatePartialLoaderProvider', '$urlRouterProvider', '$locationProvider', function ($logProvider, $rootScopeProvider, $compileProvider, $controllerProvider, $provide, $httpProvider, $translateProvider, $translatePartialLoaderProvider, $urlRouterProvider) {

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

},{"../modules/header/HeaderModule.js":18,"../modules/licenseAdmin/LicenseAdminModule.js":19,"../modules/licenseManager/LicenseManagerModule.js":27,"../modules/noticeManager/NoticeManagerModule.js":32,"../modules/taskManager/TaskManagerModule.js":36,"../services/RestAPI/RestAPIModule.js":42,"../services/http/HTTPModule.js":45,"angular":"angular","angular-animate":"angular-animate","angular-mocks":"angular-mocks","angular-resource":"angular-resource","angular-sanitize":"angular-sanitize","angular-translate":"angular-translate","angular-translate-loader-partial":"angular-translate-loader-partial","angular-ui-bootstrap":"angular-ui-bootstrap","ngClipboard":1,"rx-angular":"rx-angular","ui-router":"ui-router"}],12:[function(require,module,exports){
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
            specialInstructions: params.license.requestNote,
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
                    buttonCount: 5
                },
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project' }, { field: 'email', title: 'Contact Email' }, { field: 'status.name', title: 'Status' }, { field: 'type.name', title: 'Type' }, { field: 'method.name', title: 'Method' }, { field: 'method.id', hidden: true }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'requestDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'environment.name', title: 'Env.' }],
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
                sortable: true
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
                _this.licenseModel.encryptedDetail = '-----BEGIN LICENSE REQUEST-----\n' + data + '\n-----END LICENSE REQUEST-----';
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

            applied: params.license.applied,
            keyId: params.license.keyId
        };

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

            this.licenseManagerService.getKeyCode(this.licenseModel.id, function (data) {
                _this3.licenseKey = '-----BEGIN LICENSE REQUEST-----\n' + data + '\n-----END LICENSE REQUEST-----';
            });
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
         * The user apply and server should validate the key is correct
         */

    }, {
        key: 'applyLicenseKey',
        value: function applyLicenseKey() {
            var _this4 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseManager/applyLicenseKey/ApplyLicenseKey.html',
                controller: 'ApplyLicenseKey as applyLicenseKey',
                size: 'md',
                resolve: {
                    params: function params() {
                        return { license: _this4.licenseModel };
                    }
                }
            });

            modalInstance.result.then(function () {
                _this4.licenseModel.applied = true;
            });
        }

        /**
         * If by some reason the License was not applied at first time, this will do a request for it
         */

    }, {
        key: 'activateLicense',
        value: function activateLicense() {
            this.licenseManagerService.activateLicense(this.licenseModel, function (data) {});
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
        value: function validateIntegerOnly(e) {
            try {
                var newVal = parseInt(this.licenseModel.method.max);
                if (!isNaN(newVal)) {
                    this.licenseModel.method.max = newVal;
                } else {
                    this.licenseModel.method.max = 0;
                }

                if (e && e.currentTarget && e.currentTarget.value) {
                    e.currentTarget.value = this.licenseModel.method.max;
                }
            } catch (e) {
                this.$log.warn('Invalid Number Expception', this.licenseModel.method.max);
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
            this.selectStatusListOptions = {
                dataSource: [{ id: 1, name: 'Active' }, { id: 2, name: 'Expired' }, { id: 3, name: 'Terminated' }, { id: 4, name: 'Pending' }],
                dataTextField: 'name',
                dataValueField: 'id',
                valuePrimitive: true
            };
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
         * Depeding the number of fields and type of field, the reset can't be on the FormValidor, at least not now
         */

    }, {
        key: 'onResetForm',
        value: function onResetForm() {
            // Reset Project Selector
            this.resetDropDown(this.selectProject, this.licenseModel.projectId);
            this.resetDropDown(this.selectStatus, this.licenseModel.statusId);
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
                    buttonCount: 5
                },
                columns: [{ field: 'id', hidden: true }, { field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseManagerList.onLicenseManagerDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'owner.name', title: 'Owner' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project' }, { field: 'email', title: 'Contact Email' }, { field: 'status.name', title: 'Status' }, { field: 'type.name', title: 'Type' }, { field: 'method.name', title: 'Method' }, { field: 'method.id', hidden: true }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'activationDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'environment.name', title: 'Environment' }],
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
                bannerMessage: license.bannerMessage
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

// Directives
//import SVGLoaderController from '../../directives/svg/SVGLoaderController.js'

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
TaskManagerModule.service('taskManagerService', ['$log', 'RestServiceHandler', _TaskManagerService2.default]);

// Controllers
TaskManagerModule.controller('TaskManagerController', ['$log', 'taskManagerService', '$uibModal', _TaskManagerController2.default]);
TaskManagerModule.controller('TaskManagerEdit', ['$log', _TaskManagerEdit2.default]);

// Directives
//TaskManagerModule.directive('svgLoader', SVGLoaderController);

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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvZGlzdC9uZ2NsaXBib2FyZC5qcyIsIm5vZGVfbW9kdWxlcy9uZ0NsaXBib2FyZC9ub2RlX21vZHVsZXMvY2xpcGJvYXJkL2xpYi9jbGlwYm9hcmQtYWN0aW9uLmpzIiwibm9kZV9tb2R1bGVzL25nQ2xpcGJvYXJkL25vZGVfbW9kdWxlcy9jbGlwYm9hcmQvbGliL2NsaXBib2FyZC5qcyIsIm5vZGVfbW9kdWxlcy9uZ0NsaXBib2FyZC9ub2RlX21vZHVsZXMvY2xpcGJvYXJkL25vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL25vZGVfbW9kdWxlcy9kZWxlZ2F0ZS9zcmMvY2xvc2VzdC5qcyIsIm5vZGVfbW9kdWxlcy9uZ0NsaXBib2FyZC9ub2RlX21vZHVsZXMvY2xpcGJvYXJkL25vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL25vZGVfbW9kdWxlcy9kZWxlZ2F0ZS9zcmMvZGVsZWdhdGUuanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvbm9kZV9tb2R1bGVzL2NsaXBib2FyZC9ub2RlX21vZHVsZXMvZ29vZC1saXN0ZW5lci9zcmMvaXMuanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvbm9kZV9tb2R1bGVzL2NsaXBib2FyZC9ub2RlX21vZHVsZXMvZ29vZC1saXN0ZW5lci9zcmMvbGlzdGVuLmpzIiwibm9kZV9tb2R1bGVzL25nQ2xpcGJvYXJkL25vZGVfbW9kdWxlcy9jbGlwYm9hcmQvbm9kZV9tb2R1bGVzL3NlbGVjdC9zcmMvc2VsZWN0LmpzIiwibm9kZV9tb2R1bGVzL25nQ2xpcGJvYXJkL25vZGVfbW9kdWxlcy9jbGlwYm9hcmQvbm9kZV9tb2R1bGVzL3RpbnktZW1pdHRlci9pbmRleC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcY29uZmlnXFxBbmd1bGFyUHJvdmlkZXJIZWxwZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQXBwLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxkaXJlY3RpdmVzXFxpbmRleC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcdG9vbHNcXE1vZGFsV2luZG93QWN0aXZhdGlvbi5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcdG9vbHNcXFRvYXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbWFpbi5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcZGlhbG9nQWN0aW9uXFxEaWFsb2dBY3Rpb24uanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGhlYWRlclxcSGVhZGVyQ29udHJvbGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcaGVhZGVyXFxIZWFkZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcTGljZW5zZUFkbWluTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXGFwcGx5TGljZW5zZUtleVxcQXBwbHlMaWNlbnNlS2V5LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXGNyZWF0ZWRcXENyZWF0ZWRMaWNlbnNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXGRldGFpbFxcTGljZW5zZURldGFpbC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxsaXN0XFxMaWNlbnNlQWRtaW5MaXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXG1hbnVhbGx5UmVxdWVzdFxcTWFudWFsbHlSZXF1ZXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXHJlcXVlc3RcXFJlcXVlc3RMaWNlbnNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXHNlcnZpY2VcXExpY2Vuc2VBZG1pblNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxMaWNlbnNlTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXGRldGFpbFxcTGljZW5zZU1hbmFnZXJEZXRhaWwuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxsaXN0XFxMaWNlbnNlTWFuYWdlckxpc3QuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxyZXF1ZXN0SW1wb3J0XFxSZXF1ZXN0SW1wb3J0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcc2VydmljZVxcTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxub3RpY2VNYW5hZ2VyXFxOb3RpY2VNYW5hZ2VyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxub3RpY2VNYW5hZ2VyXFxlZGl0XFxFZGl0Tm90aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxub3RpY2VNYW5hZ2VyXFxsaXN0XFxOb3RpY2VMaXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxub3RpY2VNYW5hZ2VyXFxzZXJ2aWNlXFxOb3RpY2VNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXFRhc2tNYW5hZ2VyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFx0YXNrTWFuYWdlclxcZWRpdFxcVGFza01hbmFnZXJFZGl0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFx0YXNrTWFuYWdlclxcbGlzdFxcVGFza01hbmFnZXJDb250cm9sbGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFx0YXNrTWFuYWdlclxcc2VydmljZVxcVGFza01hbmFnZXJTZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFx1dGlsc1xcZm9ybVxcRm9ybVZhbGlkYXRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlcXVlc3RIYW5kbGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcUmVzdEFQSVxcUmVzdEFQSU1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RTZXJ2aWNlSGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvci5qcyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiQUFBQTtBQ0FBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2hEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwT0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4TEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNqREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQy9GQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNqQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7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QUt0QixBQUFlO2VBQ2xCOzs7OztpQyxBQUVRLFdBQVUsQUFDZjtBQUVBOztzQkFBQSxBQUFTLE9BQVQsQUFBZ0Isb0JBQW9CLElBQUEsQUFBSSxPQUF4QyxBQUFvQyxBQUFXLEFBRS9DOztpQkFBQSxBQUFLLE1BQUwsQUFBVyxPQUFYLEFBQWtCLEFBQ2xCO21CQUFPLGFBQVksS0FBQSxBQUFLLEVBQUwsQUFBTyxLQUExQixBQUFtQixBQUFZLEFBQ2xDOzs7O3lDQUVnQixBQUNiO21CQUFPLEtBQUEsQUFBSyxNQUFaLEFBQWtCLEFBQ3JCOzs7OztHQXBCdUQsYzs7a0IsQUFBdkMiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwiLyohIG5nY2xpcGJvYXJkIC0gdjEuMS4xIC0gMjAxNi0wMi0yNlxyXG4qIGh0dHBzOi8vZ2l0aHViLmNvbS9zYWNoaW5jaG9vbHVyL25nY2xpcGJvYXJkXHJcbiogQ29weXJpZ2h0IChjKSAyMDE2IFNhY2hpbjsgTGljZW5zZWQgTUlUICovXHJcbihmdW5jdGlvbigpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciBNT0RVTEVfTkFNRSA9ICduZ2NsaXBib2FyZCc7XHJcbiAgICB2YXIgYW5ndWxhciwgQ2xpcGJvYXJkO1xyXG4gICAgXHJcbiAgICAvLyBDaGVjayBmb3IgQ29tbW9uSlMgc3VwcG9ydFxyXG4gICAgaWYgKHR5cGVvZiBtb2R1bGUgPT09ICdvYmplY3QnICYmIG1vZHVsZS5leHBvcnRzKSB7XHJcbiAgICAgIGFuZ3VsYXIgPSByZXF1aXJlKCdhbmd1bGFyJyk7XHJcbiAgICAgIENsaXBib2FyZCA9IHJlcXVpcmUoJ2NsaXBib2FyZCcpO1xyXG4gICAgICBtb2R1bGUuZXhwb3J0cyA9IE1PRFVMRV9OQU1FO1xyXG4gICAgfSBlbHNlIHtcclxuICAgICAgYW5ndWxhciA9IHdpbmRvdy5hbmd1bGFyO1xyXG4gICAgICBDbGlwYm9hcmQgPSB3aW5kb3cuQ2xpcGJvYXJkO1xyXG4gICAgfVxyXG5cclxuICAgIGFuZ3VsYXIubW9kdWxlKE1PRFVMRV9OQU1FLCBbXSkuZGlyZWN0aXZlKCduZ2NsaXBib2FyZCcsIGZ1bmN0aW9uKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIHJlc3RyaWN0OiAnQScsXHJcbiAgICAgICAgICAgIHNjb3BlOiB7XHJcbiAgICAgICAgICAgICAgICBuZ2NsaXBib2FyZFN1Y2Nlc3M6ICcmJyxcclxuICAgICAgICAgICAgICAgIG5nY2xpcGJvYXJkRXJyb3I6ICcmJ1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBsaW5rOiBmdW5jdGlvbihzY29wZSwgZWxlbWVudCkge1xyXG4gICAgICAgICAgICAgICAgdmFyIGNsaXBib2FyZCA9IG5ldyBDbGlwYm9hcmQoZWxlbWVudFswXSk7XHJcblxyXG4gICAgICAgICAgICAgICAgY2xpcGJvYXJkLm9uKCdzdWNjZXNzJywgZnVuY3Rpb24oZSkge1xyXG4gICAgICAgICAgICAgICAgICBzY29wZS4kYXBwbHkoZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNjb3BlLm5nY2xpcGJvYXJkU3VjY2Vzcyh7XHJcbiAgICAgICAgICAgICAgICAgICAgICBlOiBlXHJcbiAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAgICAgY2xpcGJvYXJkLm9uKCdlcnJvcicsIGZ1bmN0aW9uKGUpIHtcclxuICAgICAgICAgICAgICAgICAgc2NvcGUuJGFwcGx5KGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICBzY29wZS5uZ2NsaXBib2FyZEVycm9yKHtcclxuICAgICAgICAgICAgICAgICAgICAgIGU6IGVcclxuICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfSk7XHJcbn0oKSk7XHJcbiIsIihmdW5jdGlvbiAoZ2xvYmFsLCBmYWN0b3J5KSB7XG4gICAgaWYgKHR5cGVvZiBkZWZpbmUgPT09IFwiZnVuY3Rpb25cIiAmJiBkZWZpbmUuYW1kKSB7XG4gICAgICAgIGRlZmluZShbJ21vZHVsZScsICdzZWxlY3QnXSwgZmFjdG9yeSk7XG4gICAgfSBlbHNlIGlmICh0eXBlb2YgZXhwb3J0cyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgICAgICBmYWN0b3J5KG1vZHVsZSwgcmVxdWlyZSgnc2VsZWN0JykpO1xuICAgIH0gZWxzZSB7XG4gICAgICAgIHZhciBtb2QgPSB7XG4gICAgICAgICAgICBleHBvcnRzOiB7fVxuICAgICAgICB9O1xuICAgICAgICBmYWN0b3J5KG1vZCwgZ2xvYmFsLnNlbGVjdCk7XG4gICAgICAgIGdsb2JhbC5jbGlwYm9hcmRBY3Rpb24gPSBtb2QuZXhwb3J0cztcbiAgICB9XG59KSh0aGlzLCBmdW5jdGlvbiAobW9kdWxlLCBfc2VsZWN0KSB7XG4gICAgJ3VzZSBzdHJpY3QnO1xuXG4gICAgdmFyIF9zZWxlY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfc2VsZWN0KTtcblxuICAgIGZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7XG4gICAgICAgIHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7XG4gICAgICAgICAgICBkZWZhdWx0OiBvYmpcbiAgICAgICAgfTtcbiAgICB9XG5cbiAgICB2YXIgX3R5cGVvZiA9IHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiB0eXBlb2YgU3ltYm9sLml0ZXJhdG9yID09PSBcInN5bWJvbFwiID8gZnVuY3Rpb24gKG9iaikge1xuICAgICAgICByZXR1cm4gdHlwZW9mIG9iajtcbiAgICB9IDogZnVuY3Rpb24gKG9iaikge1xuICAgICAgICByZXR1cm4gb2JqICYmIHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiBvYmouY29uc3RydWN0b3IgPT09IFN5bWJvbCAmJiBvYmogIT09IFN5bWJvbC5wcm90b3R5cGUgPyBcInN5bWJvbFwiIDogdHlwZW9mIG9iajtcbiAgICB9O1xuXG4gICAgZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3Rvcikge1xuICAgICAgICBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIkNhbm5vdCBjYWxsIGEgY2xhc3MgYXMgYSBmdW5jdGlvblwiKTtcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBfY3JlYXRlQ2xhc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIGZ1bmN0aW9uIGRlZmluZVByb3BlcnRpZXModGFyZ2V0LCBwcm9wcykge1xuICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBwcm9wcy5sZW5ndGg7IGkrKykge1xuICAgICAgICAgICAgICAgIHZhciBkZXNjcmlwdG9yID0gcHJvcHNbaV07XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5lbnVtZXJhYmxlID0gZGVzY3JpcHRvci5lbnVtZXJhYmxlIHx8IGZhbHNlO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuY29uZmlndXJhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBpZiAoXCJ2YWx1ZVwiIGluIGRlc2NyaXB0b3IpIGRlc2NyaXB0b3Iud3JpdGFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIE9iamVjdC5kZWZpbmVQcm9wZXJ0eSh0YXJnZXQsIGRlc2NyaXB0b3Iua2V5LCBkZXNjcmlwdG9yKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBmdW5jdGlvbiAoQ29uc3RydWN0b3IsIHByb3RvUHJvcHMsIHN0YXRpY1Byb3BzKSB7XG4gICAgICAgICAgICBpZiAocHJvdG9Qcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvci5wcm90b3R5cGUsIHByb3RvUHJvcHMpO1xuICAgICAgICAgICAgaWYgKHN0YXRpY1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLCBzdGF0aWNQcm9wcyk7XG4gICAgICAgICAgICByZXR1cm4gQ29uc3RydWN0b3I7XG4gICAgICAgIH07XG4gICAgfSgpO1xuXG4gICAgdmFyIENsaXBib2FyZEFjdGlvbiA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgLyoqXG4gICAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvcHRpb25zXG4gICAgICAgICAqL1xuICAgICAgICBmdW5jdGlvbiBDbGlwYm9hcmRBY3Rpb24ob3B0aW9ucykge1xuICAgICAgICAgICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIENsaXBib2FyZEFjdGlvbik7XG5cbiAgICAgICAgICAgIHRoaXMucmVzb2x2ZU9wdGlvbnMob3B0aW9ucyk7XG4gICAgICAgICAgICB0aGlzLmluaXRTZWxlY3Rpb24oKTtcbiAgICAgICAgfVxuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBEZWZpbmVzIGJhc2UgcHJvcGVydGllcyBwYXNzZWQgZnJvbSBjb25zdHJ1Y3Rvci5cbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG5cblxuICAgICAgICBfY3JlYXRlQ2xhc3MoQ2xpcGJvYXJkQWN0aW9uLCBbe1xuICAgICAgICAgICAga2V5OiAncmVzb2x2ZU9wdGlvbnMnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlc29sdmVPcHRpb25zKCkge1xuICAgICAgICAgICAgICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiB7fTtcblxuICAgICAgICAgICAgICAgIHRoaXMuYWN0aW9uID0gb3B0aW9ucy5hY3Rpb247XG4gICAgICAgICAgICAgICAgdGhpcy5lbWl0dGVyID0gb3B0aW9ucy5lbWl0dGVyO1xuICAgICAgICAgICAgICAgIHRoaXMudGFyZ2V0ID0gb3B0aW9ucy50YXJnZXQ7XG4gICAgICAgICAgICAgICAgdGhpcy50ZXh0ID0gb3B0aW9ucy50ZXh0O1xuICAgICAgICAgICAgICAgIHRoaXMudHJpZ2dlciA9IG9wdGlvbnMudHJpZ2dlcjtcblxuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gJyc7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2luaXRTZWxlY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGluaXRTZWxlY3Rpb24oKSB7XG4gICAgICAgICAgICAgICAgaWYgKHRoaXMudGV4dCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdEZha2UoKTtcbiAgICAgICAgICAgICAgICB9IGVsc2UgaWYgKHRoaXMudGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0VGFyZ2V0KCk7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdzZWxlY3RGYWtlJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBzZWxlY3RGYWtlKCkge1xuICAgICAgICAgICAgICAgIHZhciBfdGhpcyA9IHRoaXM7XG5cbiAgICAgICAgICAgICAgICB2YXIgaXNSVEwgPSBkb2N1bWVudC5kb2N1bWVudEVsZW1lbnQuZ2V0QXR0cmlidXRlKCdkaXInKSA9PSAncnRsJztcblxuICAgICAgICAgICAgICAgIHRoaXMucmVtb3ZlRmFrZSgpO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrID0gZnVuY3Rpb24gKCkge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gX3RoaXMucmVtb3ZlRmFrZSgpO1xuICAgICAgICAgICAgICAgIH07XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlciA9IGRvY3VtZW50LmJvZHkuYWRkRXZlbnRMaXN0ZW5lcignY2xpY2snLCB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2spIHx8IHRydWU7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtID0gZG9jdW1lbnQuY3JlYXRlRWxlbWVudCgndGV4dGFyZWEnKTtcbiAgICAgICAgICAgICAgICAvLyBQcmV2ZW50IHpvb21pbmcgb24gaU9TXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5mb250U2l6ZSA9ICcxMnB0JztcbiAgICAgICAgICAgICAgICAvLyBSZXNldCBib3ggbW9kZWxcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLmJvcmRlciA9ICcwJztcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLnBhZGRpbmcgPSAnMCc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5tYXJnaW4gPSAnMCc7XG4gICAgICAgICAgICAgICAgLy8gTW92ZSBlbGVtZW50IG91dCBvZiBzY3JlZW4gaG9yaXpvbnRhbGx5XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5wb3NpdGlvbiA9ICdhYnNvbHV0ZSc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZVtpc1JUTCA/ICdyaWdodCcgOiAnbGVmdCddID0gJy05OTk5cHgnO1xuICAgICAgICAgICAgICAgIC8vIE1vdmUgZWxlbWVudCB0byB0aGUgc2FtZSBwb3NpdGlvbiB2ZXJ0aWNhbGx5XG4gICAgICAgICAgICAgICAgdmFyIHlQb3NpdGlvbiA9IHdpbmRvdy5wYWdlWU9mZnNldCB8fCBkb2N1bWVudC5kb2N1bWVudEVsZW1lbnQuc2Nyb2xsVG9wO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uYWRkRXZlbnRMaXN0ZW5lcignZm9jdXMnLCB3aW5kb3cuc2Nyb2xsVG8oMCwgeVBvc2l0aW9uKSk7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS50b3AgPSB5UG9zaXRpb24gKyAncHgnO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zZXRBdHRyaWJ1dGUoJ3JlYWRvbmx5JywgJycpO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0udmFsdWUgPSB0aGlzLnRleHQ7XG5cbiAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LmFwcGVuZENoaWxkKHRoaXMuZmFrZUVsZW0pO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RlZFRleHQgPSAoMCwgX3NlbGVjdDIuZGVmYXVsdCkodGhpcy5mYWtlRWxlbSk7XG4gICAgICAgICAgICAgICAgdGhpcy5jb3B5VGV4dCgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdyZW1vdmVGYWtlJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiByZW1vdmVGYWtlKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLmZha2VIYW5kbGVyKSB7XG4gICAgICAgICAgICAgICAgICAgIGRvY3VtZW50LmJvZHkucmVtb3ZlRXZlbnRMaXN0ZW5lcignY2xpY2snLCB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2spO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyID0gbnVsbDtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5mYWtlRWxlbSkge1xuICAgICAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LnJlbW92ZUNoaWxkKHRoaXMuZmFrZUVsZW0pO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3NlbGVjdFRhcmdldCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gc2VsZWN0VGFyZ2V0KCkge1xuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gKDAsIF9zZWxlY3QyLmRlZmF1bHQpKHRoaXMudGFyZ2V0KTtcbiAgICAgICAgICAgICAgICB0aGlzLmNvcHlUZXh0KCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2NvcHlUZXh0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBjb3B5VGV4dCgpIHtcbiAgICAgICAgICAgICAgICB2YXIgc3VjY2VlZGVkID0gdm9pZCAwO1xuXG4gICAgICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICAgICAgc3VjY2VlZGVkID0gZG9jdW1lbnQuZXhlY0NvbW1hbmQodGhpcy5hY3Rpb24pO1xuICAgICAgICAgICAgICAgIH0gY2F0Y2ggKGVycikge1xuICAgICAgICAgICAgICAgICAgICBzdWNjZWVkZWQgPSBmYWxzZTtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB0aGlzLmhhbmRsZVJlc3VsdChzdWNjZWVkZWQpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdoYW5kbGVSZXN1bHQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGhhbmRsZVJlc3VsdChzdWNjZWVkZWQpIHtcbiAgICAgICAgICAgICAgICB0aGlzLmVtaXR0ZXIuZW1pdChzdWNjZWVkZWQgPyAnc3VjY2VzcycgOiAnZXJyb3InLCB7XG4gICAgICAgICAgICAgICAgICAgIGFjdGlvbjogdGhpcy5hY3Rpb24sXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6IHRoaXMuc2VsZWN0ZWRUZXh0LFxuICAgICAgICAgICAgICAgICAgICB0cmlnZ2VyOiB0aGlzLnRyaWdnZXIsXG4gICAgICAgICAgICAgICAgICAgIGNsZWFyU2VsZWN0aW9uOiB0aGlzLmNsZWFyU2VsZWN0aW9uLmJpbmQodGhpcylcbiAgICAgICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnY2xlYXJTZWxlY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGNsZWFyU2VsZWN0aW9uKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLnRhcmdldCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnRhcmdldC5ibHVyKCk7XG4gICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgd2luZG93LmdldFNlbGVjdGlvbigpLnJlbW92ZUFsbFJhbmdlcygpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZXN0cm95JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZXN0cm95KCkge1xuICAgICAgICAgICAgICAgIHRoaXMucmVtb3ZlRmFrZSgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdhY3Rpb24nLFxuICAgICAgICAgICAgc2V0OiBmdW5jdGlvbiBzZXQoKSB7XG4gICAgICAgICAgICAgICAgdmFyIGFjdGlvbiA9IGFyZ3VtZW50cy5sZW5ndGggPiAwICYmIGFyZ3VtZW50c1swXSAhPT0gdW5kZWZpbmVkID8gYXJndW1lbnRzWzBdIDogJ2NvcHknO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5fYWN0aW9uID0gYWN0aW9uO1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuX2FjdGlvbiAhPT0gJ2NvcHknICYmIHRoaXMuX2FjdGlvbiAhPT0gJ2N1dCcpIHtcbiAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwiYWN0aW9uXCIgdmFsdWUsIHVzZSBlaXRoZXIgXCJjb3B5XCIgb3IgXCJjdXRcIicpO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5fYWN0aW9uO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICd0YXJnZXQnLFxuICAgICAgICAgICAgc2V0OiBmdW5jdGlvbiBzZXQodGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgaWYgKHRhcmdldCAhPT0gdW5kZWZpbmVkKSB7XG4gICAgICAgICAgICAgICAgICAgIGlmICh0YXJnZXQgJiYgKHR5cGVvZiB0YXJnZXQgPT09ICd1bmRlZmluZWQnID8gJ3VuZGVmaW5lZCcgOiBfdHlwZW9mKHRhcmdldCkpID09PSAnb2JqZWN0JyAmJiB0YXJnZXQubm9kZVR5cGUgPT09IDEpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIGlmICh0aGlzLmFjdGlvbiA9PT0gJ2NvcHknICYmIHRhcmdldC5oYXNBdHRyaWJ1dGUoJ2Rpc2FibGVkJykpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJ0YXJnZXRcIiBhdHRyaWJ1dGUuIFBsZWFzZSB1c2UgXCJyZWFkb25seVwiIGluc3RlYWQgb2YgXCJkaXNhYmxlZFwiIGF0dHJpYnV0ZScpO1xuICAgICAgICAgICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgICAgICAgICBpZiAodGhpcy5hY3Rpb24gPT09ICdjdXQnICYmICh0YXJnZXQuaGFzQXR0cmlidXRlKCdyZWFkb25seScpIHx8IHRhcmdldC5oYXNBdHRyaWJ1dGUoJ2Rpc2FibGVkJykpKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwidGFyZ2V0XCIgYXR0cmlidXRlLiBZb3UgY2FuXFwndCBjdXQgdGV4dCBmcm9tIGVsZW1lbnRzIHdpdGggXCJyZWFkb25seVwiIG9yIFwiZGlzYWJsZWRcIiBhdHRyaWJ1dGVzJyk7XG4gICAgICAgICAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuX3RhcmdldCA9IHRhcmdldDtcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignSW52YWxpZCBcInRhcmdldFwiIHZhbHVlLCB1c2UgYSB2YWxpZCBFbGVtZW50Jyk7XG4gICAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMuX3RhcmdldDtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfV0pO1xuXG4gICAgICAgIHJldHVybiBDbGlwYm9hcmRBY3Rpb247XG4gICAgfSgpO1xuXG4gICAgbW9kdWxlLmV4cG9ydHMgPSBDbGlwYm9hcmRBY3Rpb247XG59KTsiLCIoZnVuY3Rpb24gKGdsb2JhbCwgZmFjdG9yeSkge1xuICAgIGlmICh0eXBlb2YgZGVmaW5lID09PSBcImZ1bmN0aW9uXCIgJiYgZGVmaW5lLmFtZCkge1xuICAgICAgICBkZWZpbmUoWydtb2R1bGUnLCAnLi9jbGlwYm9hcmQtYWN0aW9uJywgJ3RpbnktZW1pdHRlcicsICdnb29kLWxpc3RlbmVyJ10sIGZhY3RvcnkpO1xuICAgIH0gZWxzZSBpZiAodHlwZW9mIGV4cG9ydHMgIT09IFwidW5kZWZpbmVkXCIpIHtcbiAgICAgICAgZmFjdG9yeShtb2R1bGUsIHJlcXVpcmUoJy4vY2xpcGJvYXJkLWFjdGlvbicpLCByZXF1aXJlKCd0aW55LWVtaXR0ZXInKSwgcmVxdWlyZSgnZ29vZC1saXN0ZW5lcicpKTtcbiAgICB9IGVsc2Uge1xuICAgICAgICB2YXIgbW9kID0ge1xuICAgICAgICAgICAgZXhwb3J0czoge31cbiAgICAgICAgfTtcbiAgICAgICAgZmFjdG9yeShtb2QsIGdsb2JhbC5jbGlwYm9hcmRBY3Rpb24sIGdsb2JhbC50aW55RW1pdHRlciwgZ2xvYmFsLmdvb2RMaXN0ZW5lcik7XG4gICAgICAgIGdsb2JhbC5jbGlwYm9hcmQgPSBtb2QuZXhwb3J0cztcbiAgICB9XG59KSh0aGlzLCBmdW5jdGlvbiAobW9kdWxlLCBfY2xpcGJvYXJkQWN0aW9uLCBfdGlueUVtaXR0ZXIsIF9nb29kTGlzdGVuZXIpIHtcbiAgICAndXNlIHN0cmljdCc7XG5cbiAgICB2YXIgX2NsaXBib2FyZEFjdGlvbjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jbGlwYm9hcmRBY3Rpb24pO1xuXG4gICAgdmFyIF90aW55RW1pdHRlcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF90aW55RW1pdHRlcik7XG5cbiAgICB2YXIgX2dvb2RMaXN0ZW5lcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9nb29kTGlzdGVuZXIpO1xuXG4gICAgZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHtcbiAgICAgICAgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHtcbiAgICAgICAgICAgIGRlZmF1bHQ6IG9ialxuICAgICAgICB9O1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIF9jbGFzc0NhbGxDaGVjayhpbnN0YW5jZSwgQ29uc3RydWN0b3IpIHtcbiAgICAgICAgaWYgKCEoaW5zdGFuY2UgaW5zdGFuY2VvZiBDb25zdHJ1Y3RvcikpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoXCJDYW5ub3QgY2FsbCBhIGNsYXNzIGFzIGEgZnVuY3Rpb25cIik7XG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIgX2NyZWF0ZUNsYXNzID0gZnVuY3Rpb24gKCkge1xuICAgICAgICBmdW5jdGlvbiBkZWZpbmVQcm9wZXJ0aWVzKHRhcmdldCwgcHJvcHMpIHtcbiAgICAgICAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgcHJvcHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgICAgICAgICB2YXIgZGVzY3JpcHRvciA9IHByb3BzW2ldO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuZW51bWVyYWJsZSA9IGRlc2NyaXB0b3IuZW51bWVyYWJsZSB8fCBmYWxzZTtcbiAgICAgICAgICAgICAgICBkZXNjcmlwdG9yLmNvbmZpZ3VyYWJsZSA9IHRydWU7XG4gICAgICAgICAgICAgICAgaWYgKFwidmFsdWVcIiBpbiBkZXNjcmlwdG9yKSBkZXNjcmlwdG9yLndyaXRhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBPYmplY3QuZGVmaW5lUHJvcGVydHkodGFyZ2V0LCBkZXNjcmlwdG9yLmtleSwgZGVzY3JpcHRvcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gZnVuY3Rpb24gKENvbnN0cnVjdG9yLCBwcm90b1Byb3BzLCBzdGF0aWNQcm9wcykge1xuICAgICAgICAgICAgaWYgKHByb3RvUHJvcHMpIGRlZmluZVByb3BlcnRpZXMoQ29uc3RydWN0b3IucHJvdG90eXBlLCBwcm90b1Byb3BzKTtcbiAgICAgICAgICAgIGlmIChzdGF0aWNQcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvciwgc3RhdGljUHJvcHMpO1xuICAgICAgICAgICAgcmV0dXJuIENvbnN0cnVjdG9yO1xuICAgICAgICB9O1xuICAgIH0oKTtcblxuICAgIGZ1bmN0aW9uIF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHNlbGYsIGNhbGwpIHtcbiAgICAgICAgaWYgKCFzZWxmKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgUmVmZXJlbmNlRXJyb3IoXCJ0aGlzIGhhc24ndCBiZWVuIGluaXRpYWxpc2VkIC0gc3VwZXIoKSBoYXNuJ3QgYmVlbiBjYWxsZWRcIik7XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gY2FsbCAmJiAodHlwZW9mIGNhbGwgPT09IFwib2JqZWN0XCIgfHwgdHlwZW9mIGNhbGwgPT09IFwiZnVuY3Rpb25cIikgPyBjYWxsIDogc2VsZjtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBfaW5oZXJpdHMoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIHtcbiAgICAgICAgaWYgKHR5cGVvZiBzdXBlckNsYXNzICE9PSBcImZ1bmN0aW9uXCIgJiYgc3VwZXJDbGFzcyAhPT0gbnVsbCkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIlN1cGVyIGV4cHJlc3Npb24gbXVzdCBlaXRoZXIgYmUgbnVsbCBvciBhIGZ1bmN0aW9uLCBub3QgXCIgKyB0eXBlb2Ygc3VwZXJDbGFzcyk7XG4gICAgICAgIH1cblxuICAgICAgICBzdWJDbGFzcy5wcm90b3R5cGUgPSBPYmplY3QuY3JlYXRlKHN1cGVyQ2xhc3MgJiYgc3VwZXJDbGFzcy5wcm90b3R5cGUsIHtcbiAgICAgICAgICAgIGNvbnN0cnVjdG9yOiB7XG4gICAgICAgICAgICAgICAgdmFsdWU6IHN1YkNsYXNzLFxuICAgICAgICAgICAgICAgIGVudW1lcmFibGU6IGZhbHNlLFxuICAgICAgICAgICAgICAgIHdyaXRhYmxlOiB0cnVlLFxuICAgICAgICAgICAgICAgIGNvbmZpZ3VyYWJsZTogdHJ1ZVxuICAgICAgICAgICAgfVxuICAgICAgICB9KTtcbiAgICAgICAgaWYgKHN1cGVyQ2xhc3MpIE9iamVjdC5zZXRQcm90b3R5cGVPZiA/IE9iamVjdC5zZXRQcm90b3R5cGVPZihzdWJDbGFzcywgc3VwZXJDbGFzcykgOiBzdWJDbGFzcy5fX3Byb3RvX18gPSBzdXBlckNsYXNzO1xuICAgIH1cblxuICAgIHZhciBDbGlwYm9hcmQgPSBmdW5jdGlvbiAoX0VtaXR0ZXIpIHtcbiAgICAgICAgX2luaGVyaXRzKENsaXBib2FyZCwgX0VtaXR0ZXIpO1xuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBAcGFyYW0ge1N0cmluZ3xIVE1MRWxlbWVudHxIVE1MQ29sbGVjdGlvbnxOb2RlTGlzdH0gdHJpZ2dlclxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cbiAgICAgICAgZnVuY3Rpb24gQ2xpcGJvYXJkKHRyaWdnZXIsIG9wdGlvbnMpIHtcbiAgICAgICAgICAgIF9jbGFzc0NhbGxDaGVjayh0aGlzLCBDbGlwYm9hcmQpO1xuXG4gICAgICAgICAgICB2YXIgX3RoaXMgPSBfcG9zc2libGVDb25zdHJ1Y3RvclJldHVybih0aGlzLCAoQ2xpcGJvYXJkLl9fcHJvdG9fXyB8fCBPYmplY3QuZ2V0UHJvdG90eXBlT2YoQ2xpcGJvYXJkKSkuY2FsbCh0aGlzKSk7XG5cbiAgICAgICAgICAgIF90aGlzLnJlc29sdmVPcHRpb25zKG9wdGlvbnMpO1xuICAgICAgICAgICAgX3RoaXMubGlzdGVuQ2xpY2sodHJpZ2dlcik7XG4gICAgICAgICAgICByZXR1cm4gX3RoaXM7XG4gICAgICAgIH1cblxuICAgICAgICAvKipcbiAgICAgICAgICogRGVmaW5lcyBpZiBhdHRyaWJ1dGVzIHdvdWxkIGJlIHJlc29sdmVkIHVzaW5nIGludGVybmFsIHNldHRlciBmdW5jdGlvbnNcbiAgICAgICAgICogb3IgY3VzdG9tIGZ1bmN0aW9ucyB0aGF0IHdlcmUgcGFzc2VkIGluIHRoZSBjb25zdHJ1Y3Rvci5cbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG5cblxuICAgICAgICBfY3JlYXRlQ2xhc3MoQ2xpcGJvYXJkLCBbe1xuICAgICAgICAgICAga2V5OiAncmVzb2x2ZU9wdGlvbnMnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlc29sdmVPcHRpb25zKCkge1xuICAgICAgICAgICAgICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiB7fTtcblxuICAgICAgICAgICAgICAgIHRoaXMuYWN0aW9uID0gdHlwZW9mIG9wdGlvbnMuYWN0aW9uID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy5hY3Rpb24gOiB0aGlzLmRlZmF1bHRBY3Rpb247XG4gICAgICAgICAgICAgICAgdGhpcy50YXJnZXQgPSB0eXBlb2Ygb3B0aW9ucy50YXJnZXQgPT09ICdmdW5jdGlvbicgPyBvcHRpb25zLnRhcmdldCA6IHRoaXMuZGVmYXVsdFRhcmdldDtcbiAgICAgICAgICAgICAgICB0aGlzLnRleHQgPSB0eXBlb2Ygb3B0aW9ucy50ZXh0ID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy50ZXh0IDogdGhpcy5kZWZhdWx0VGV4dDtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnbGlzdGVuQ2xpY2snLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGxpc3RlbkNsaWNrKHRyaWdnZXIpIHtcbiAgICAgICAgICAgICAgICB2YXIgX3RoaXMyID0gdGhpcztcblxuICAgICAgICAgICAgICAgIHRoaXMubGlzdGVuZXIgPSAoMCwgX2dvb2RMaXN0ZW5lcjIuZGVmYXVsdCkodHJpZ2dlciwgJ2NsaWNrJywgZnVuY3Rpb24gKGUpIHtcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIF90aGlzMi5vbkNsaWNrKGUpO1xuICAgICAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdvbkNsaWNrJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBvbkNsaWNrKGUpIHtcbiAgICAgICAgICAgICAgICB2YXIgdHJpZ2dlciA9IGUuZGVsZWdhdGVUYXJnZXQgfHwgZS5jdXJyZW50VGFyZ2V0O1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuY2xpcGJvYXJkQWN0aW9uKSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB0aGlzLmNsaXBib2FyZEFjdGlvbiA9IG5ldyBfY2xpcGJvYXJkQWN0aW9uMi5kZWZhdWx0KHtcbiAgICAgICAgICAgICAgICAgICAgYWN0aW9uOiB0aGlzLmFjdGlvbih0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdGFyZ2V0OiB0aGlzLnRhcmdldCh0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogdGhpcy50ZXh0KHRyaWdnZXIpLFxuICAgICAgICAgICAgICAgICAgICB0cmlnZ2VyOiB0cmlnZ2VyLFxuICAgICAgICAgICAgICAgICAgICBlbWl0dGVyOiB0aGlzXG4gICAgICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRBY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRBY3Rpb24odHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHJldHVybiBnZXRBdHRyaWJ1dGVWYWx1ZSgnYWN0aW9uJywgdHJpZ2dlcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRUYXJnZXQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRUYXJnZXQodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHZhciBzZWxlY3RvciA9IGdldEF0dHJpYnV0ZVZhbHVlKCd0YXJnZXQnLCB0cmlnZ2VyKTtcblxuICAgICAgICAgICAgICAgIGlmIChzZWxlY3Rvcikge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gZG9jdW1lbnQucXVlcnlTZWxlY3RvcihzZWxlY3Rvcik7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZWZhdWx0VGV4dCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVmYXVsdFRleHQodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHJldHVybiBnZXRBdHRyaWJ1dGVWYWx1ZSgndGV4dCcsIHRyaWdnZXIpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZXN0cm95JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZXN0cm95KCkge1xuICAgICAgICAgICAgICAgIHRoaXMubGlzdGVuZXIuZGVzdHJveSgpO1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuY2xpcGJvYXJkQWN0aW9uKSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uLmRlc3Ryb3koKTtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24gPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfV0pO1xuXG4gICAgICAgIHJldHVybiBDbGlwYm9hcmQ7XG4gICAgfShfdGlueUVtaXR0ZXIyLmRlZmF1bHQpO1xuXG4gICAgLyoqXG4gICAgICogSGVscGVyIGZ1bmN0aW9uIHRvIHJldHJpZXZlIGF0dHJpYnV0ZSB2YWx1ZS5cbiAgICAgKiBAcGFyYW0ge1N0cmluZ30gc3VmZml4XG4gICAgICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gICAgICovXG4gICAgZnVuY3Rpb24gZ2V0QXR0cmlidXRlVmFsdWUoc3VmZml4LCBlbGVtZW50KSB7XG4gICAgICAgIHZhciBhdHRyaWJ1dGUgPSAnZGF0YS1jbGlwYm9hcmQtJyArIHN1ZmZpeDtcblxuICAgICAgICBpZiAoIWVsZW1lbnQuaGFzQXR0cmlidXRlKGF0dHJpYnV0ZSkpIHtcbiAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBlbGVtZW50LmdldEF0dHJpYnV0ZShhdHRyaWJ1dGUpO1xuICAgIH1cblxuICAgIG1vZHVsZS5leHBvcnRzID0gQ2xpcGJvYXJkO1xufSk7IiwidmFyIERPQ1VNRU5UX05PREVfVFlQRSA9IDk7XG5cbi8qKlxuICogQSBwb2x5ZmlsbCBmb3IgRWxlbWVudC5tYXRjaGVzKClcbiAqL1xuaWYgKEVsZW1lbnQgJiYgIUVsZW1lbnQucHJvdG90eXBlLm1hdGNoZXMpIHtcbiAgICB2YXIgcHJvdG8gPSBFbGVtZW50LnByb3RvdHlwZTtcblxuICAgIHByb3RvLm1hdGNoZXMgPSBwcm90by5tYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ubW96TWF0Y2hlc1NlbGVjdG9yIHx8XG4gICAgICAgICAgICAgICAgICAgIHByb3RvLm1zTWF0Y2hlc1NlbGVjdG9yIHx8XG4gICAgICAgICAgICAgICAgICAgIHByb3RvLm9NYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ud2Via2l0TWF0Y2hlc1NlbGVjdG9yO1xufVxuXG4vKipcbiAqIEZpbmRzIHRoZSBjbG9zZXN0IHBhcmVudCB0aGF0IG1hdGNoZXMgYSBzZWxlY3Rvci5cbiAqXG4gKiBAcGFyYW0ge0VsZW1lbnR9IGVsZW1lbnRcbiAqIEBwYXJhbSB7U3RyaW5nfSBzZWxlY3RvclxuICogQHJldHVybiB7RnVuY3Rpb259XG4gKi9cbmZ1bmN0aW9uIGNsb3Nlc3QgKGVsZW1lbnQsIHNlbGVjdG9yKSB7XG4gICAgd2hpbGUgKGVsZW1lbnQgJiYgZWxlbWVudC5ub2RlVHlwZSAhPT0gRE9DVU1FTlRfTk9ERV9UWVBFKSB7XG4gICAgICAgIGlmIChlbGVtZW50Lm1hdGNoZXMoc2VsZWN0b3IpKSByZXR1cm4gZWxlbWVudDtcbiAgICAgICAgZWxlbWVudCA9IGVsZW1lbnQucGFyZW50Tm9kZTtcbiAgICB9XG59XG5cbm1vZHVsZS5leHBvcnRzID0gY2xvc2VzdDtcbiIsInZhciBjbG9zZXN0ID0gcmVxdWlyZSgnLi9jbG9zZXN0Jyk7XG5cbi8qKlxuICogRGVsZWdhdGVzIGV2ZW50IHRvIGEgc2VsZWN0b3IuXG4gKlxuICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHBhcmFtIHtCb29sZWFufSB1c2VDYXB0dXJlXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGRlbGVnYXRlKGVsZW1lbnQsIHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaywgdXNlQ2FwdHVyZSkge1xuICAgIHZhciBsaXN0ZW5lckZuID0gbGlzdGVuZXIuYXBwbHkodGhpcywgYXJndW1lbnRzKTtcblxuICAgIGVsZW1lbnQuYWRkRXZlbnRMaXN0ZW5lcih0eXBlLCBsaXN0ZW5lckZuLCB1c2VDYXB0dXJlKTtcblxuICAgIHJldHVybiB7XG4gICAgICAgIGRlc3Ryb3k6IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgZWxlbWVudC5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGxpc3RlbmVyRm4sIHVzZUNhcHR1cmUpO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEZpbmRzIGNsb3Nlc3QgbWF0Y2ggYW5kIGludm9rZXMgY2FsbGJhY2suXG4gKlxuICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7RnVuY3Rpb259XG4gKi9cbmZ1bmN0aW9uIGxpc3RlbmVyKGVsZW1lbnQsIHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaykge1xuICAgIHJldHVybiBmdW5jdGlvbihlKSB7XG4gICAgICAgIGUuZGVsZWdhdGVUYXJnZXQgPSBjbG9zZXN0KGUudGFyZ2V0LCBzZWxlY3Rvcik7XG5cbiAgICAgICAgaWYgKGUuZGVsZWdhdGVUYXJnZXQpIHtcbiAgICAgICAgICAgIGNhbGxiYWNrLmNhbGwoZWxlbWVudCwgZSk7XG4gICAgICAgIH1cbiAgICB9XG59XG5cbm1vZHVsZS5leHBvcnRzID0gZGVsZWdhdGU7XG4iLCIvKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgSFRNTCBlbGVtZW50LlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5ub2RlID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICByZXR1cm4gdmFsdWUgIT09IHVuZGVmaW5lZFxuICAgICAgICAmJiB2YWx1ZSBpbnN0YW5jZW9mIEhUTUxFbGVtZW50XG4gICAgICAgICYmIHZhbHVlLm5vZGVUeXBlID09PSAxO1xufTtcblxuLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIGxpc3Qgb2YgSFRNTCBlbGVtZW50cy5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gdmFsdWVcbiAqIEByZXR1cm4ge0Jvb2xlYW59XG4gKi9cbmV4cG9ydHMubm9kZUxpc3QgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHZhciB0eXBlID0gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZy5jYWxsKHZhbHVlKTtcblxuICAgIHJldHVybiB2YWx1ZSAhPT0gdW5kZWZpbmVkXG4gICAgICAgICYmICh0eXBlID09PSAnW29iamVjdCBOb2RlTGlzdF0nIHx8IHR5cGUgPT09ICdbb2JqZWN0IEhUTUxDb2xsZWN0aW9uXScpXG4gICAgICAgICYmICgnbGVuZ3RoJyBpbiB2YWx1ZSlcbiAgICAgICAgJiYgKHZhbHVlLmxlbmd0aCA9PT0gMCB8fCBleHBvcnRzLm5vZGUodmFsdWVbMF0pKTtcbn07XG5cbi8qKlxuICogQ2hlY2sgaWYgYXJndW1lbnQgaXMgYSBzdHJpbmcuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLnN0cmluZyA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgcmV0dXJuIHR5cGVvZiB2YWx1ZSA9PT0gJ3N0cmluZydcbiAgICAgICAgfHwgdmFsdWUgaW5zdGFuY2VvZiBTdHJpbmc7XG59O1xuXG4vKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLmZuID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICB2YXIgdHlwZSA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbCh2YWx1ZSk7XG5cbiAgICByZXR1cm4gdHlwZSA9PT0gJ1tvYmplY3QgRnVuY3Rpb25dJztcbn07XG4iLCJ2YXIgaXMgPSByZXF1aXJlKCcuL2lzJyk7XG52YXIgZGVsZWdhdGUgPSByZXF1aXJlKCdkZWxlZ2F0ZScpO1xuXG4vKipcbiAqIFZhbGlkYXRlcyBhbGwgcGFyYW1zIGFuZCBjYWxscyB0aGUgcmlnaHRcbiAqIGxpc3RlbmVyIGZ1bmN0aW9uIGJhc2VkIG9uIGl0cyB0YXJnZXQgdHlwZS5cbiAqXG4gKiBAcGFyYW0ge1N0cmluZ3xIVE1MRWxlbWVudHxIVE1MQ29sbGVjdGlvbnxOb2RlTGlzdH0gdGFyZ2V0XG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuKHRhcmdldCwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBpZiAoIXRhcmdldCAmJiAhdHlwZSAmJiAhY2FsbGJhY2spIHtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdNaXNzaW5nIHJlcXVpcmVkIGFyZ3VtZW50cycpO1xuICAgIH1cblxuICAgIGlmICghaXMuc3RyaW5nKHR5cGUpKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ1NlY29uZCBhcmd1bWVudCBtdXN0IGJlIGEgU3RyaW5nJyk7XG4gICAgfVxuXG4gICAgaWYgKCFpcy5mbihjYWxsYmFjaykpIHtcbiAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcignVGhpcmQgYXJndW1lbnQgbXVzdCBiZSBhIEZ1bmN0aW9uJyk7XG4gICAgfVxuXG4gICAgaWYgKGlzLm5vZGUodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuTm9kZSh0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKTtcbiAgICB9XG4gICAgZWxzZSBpZiAoaXMubm9kZUxpc3QodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuTm9kZUxpc3QodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2UgaWYgKGlzLnN0cmluZyh0YXJnZXQpKSB7XG4gICAgICAgIHJldHVybiBsaXN0ZW5TZWxlY3Rvcih0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKTtcbiAgICB9XG4gICAgZWxzZSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ0ZpcnN0IGFyZ3VtZW50IG11c3QgYmUgYSBTdHJpbmcsIEhUTUxFbGVtZW50LCBIVE1MQ29sbGVjdGlvbiwgb3IgTm9kZUxpc3QnKTtcbiAgICB9XG59XG5cbi8qKlxuICogQWRkcyBhbiBldmVudCBsaXN0ZW5lciB0byBhIEhUTUwgZWxlbWVudFxuICogYW5kIHJldHVybnMgYSByZW1vdmUgbGlzdGVuZXIgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtIVE1MRWxlbWVudH0gbm9kZVxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3Rlbk5vZGUobm9kZSwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBub2RlLmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuXG4gICAgcmV0dXJuIHtcbiAgICAgICAgZGVzdHJveTogZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZCBhbiBldmVudCBsaXN0ZW5lciB0byBhIGxpc3Qgb2YgSFRNTCBlbGVtZW50c1xuICogYW5kIHJldHVybnMgYSByZW1vdmUgbGlzdGVuZXIgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtOb2RlTGlzdHxIVE1MQ29sbGVjdGlvbn0gbm9kZUxpc3RcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5Ob2RlTGlzdChub2RlTGlzdCwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBBcnJheS5wcm90b3R5cGUuZm9yRWFjaC5jYWxsKG5vZGVMaXN0LCBmdW5jdGlvbihub2RlKSB7XG4gICAgICAgIG5vZGUuYWRkRXZlbnRMaXN0ZW5lcih0eXBlLCBjYWxsYmFjayk7XG4gICAgfSk7XG5cbiAgICByZXR1cm4ge1xuICAgICAgICBkZXN0cm95OiBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIEFycmF5LnByb3RvdHlwZS5mb3JFYWNoLmNhbGwobm9kZUxpc3QsIGZ1bmN0aW9uKG5vZGUpIHtcbiAgICAgICAgICAgICAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgICAgICAgICAgfSk7XG4gICAgICAgIH1cbiAgICB9XG59XG5cbi8qKlxuICogQWRkIGFuIGV2ZW50IGxpc3RlbmVyIHRvIGEgc2VsZWN0b3JcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7U3RyaW5nfSBzZWxlY3RvclxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3RlblNlbGVjdG9yKHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaykge1xuICAgIHJldHVybiBkZWxlZ2F0ZShkb2N1bWVudC5ib2R5LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spO1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGxpc3RlbjtcbiIsImZ1bmN0aW9uIHNlbGVjdChlbGVtZW50KSB7XG4gICAgdmFyIHNlbGVjdGVkVGV4dDtcblxuICAgIGlmIChlbGVtZW50Lm5vZGVOYW1lID09PSAnU0VMRUNUJykge1xuICAgICAgICBlbGVtZW50LmZvY3VzKCk7XG5cbiAgICAgICAgc2VsZWN0ZWRUZXh0ID0gZWxlbWVudC52YWx1ZTtcbiAgICB9XG4gICAgZWxzZSBpZiAoZWxlbWVudC5ub2RlTmFtZSA9PT0gJ0lOUFVUJyB8fCBlbGVtZW50Lm5vZGVOYW1lID09PSAnVEVYVEFSRUEnKSB7XG4gICAgICAgIGVsZW1lbnQuZm9jdXMoKTtcbiAgICAgICAgZWxlbWVudC5zZXRTZWxlY3Rpb25SYW5nZSgwLCBlbGVtZW50LnZhbHVlLmxlbmd0aCk7XG5cbiAgICAgICAgc2VsZWN0ZWRUZXh0ID0gZWxlbWVudC52YWx1ZTtcbiAgICB9XG4gICAgZWxzZSB7XG4gICAgICAgIGlmIChlbGVtZW50Lmhhc0F0dHJpYnV0ZSgnY29udGVudGVkaXRhYmxlJykpIHtcbiAgICAgICAgICAgIGVsZW1lbnQuZm9jdXMoKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHZhciBzZWxlY3Rpb24gPSB3aW5kb3cuZ2V0U2VsZWN0aW9uKCk7XG4gICAgICAgIHZhciByYW5nZSA9IGRvY3VtZW50LmNyZWF0ZVJhbmdlKCk7XG5cbiAgICAgICAgcmFuZ2Uuc2VsZWN0Tm9kZUNvbnRlbnRzKGVsZW1lbnQpO1xuICAgICAgICBzZWxlY3Rpb24ucmVtb3ZlQWxsUmFuZ2VzKCk7XG4gICAgICAgIHNlbGVjdGlvbi5hZGRSYW5nZShyYW5nZSk7XG5cbiAgICAgICAgc2VsZWN0ZWRUZXh0ID0gc2VsZWN0aW9uLnRvU3RyaW5nKCk7XG4gICAgfVxuXG4gICAgcmV0dXJuIHNlbGVjdGVkVGV4dDtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBzZWxlY3Q7XG4iLCJmdW5jdGlvbiBFICgpIHtcbiAgLy8gS2VlcCB0aGlzIGVtcHR5IHNvIGl0J3MgZWFzaWVyIHRvIGluaGVyaXQgZnJvbVxuICAvLyAodmlhIGh0dHBzOi8vZ2l0aHViLmNvbS9saXBzbWFjayBmcm9tIGh0dHBzOi8vZ2l0aHViLmNvbS9zY290dGNvcmdhbi90aW55LWVtaXR0ZXIvaXNzdWVzLzMpXG59XG5cbkUucHJvdG90eXBlID0ge1xuICBvbjogZnVuY3Rpb24gKG5hbWUsIGNhbGxiYWNrLCBjdHgpIHtcbiAgICB2YXIgZSA9IHRoaXMuZSB8fCAodGhpcy5lID0ge30pO1xuXG4gICAgKGVbbmFtZV0gfHwgKGVbbmFtZV0gPSBbXSkpLnB1c2goe1xuICAgICAgZm46IGNhbGxiYWNrLFxuICAgICAgY3R4OiBjdHhcbiAgICB9KTtcblxuICAgIHJldHVybiB0aGlzO1xuICB9LFxuXG4gIG9uY2U6IGZ1bmN0aW9uIChuYW1lLCBjYWxsYmFjaywgY3R4KSB7XG4gICAgdmFyIHNlbGYgPSB0aGlzO1xuICAgIGZ1bmN0aW9uIGxpc3RlbmVyICgpIHtcbiAgICAgIHNlbGYub2ZmKG5hbWUsIGxpc3RlbmVyKTtcbiAgICAgIGNhbGxiYWNrLmFwcGx5KGN0eCwgYXJndW1lbnRzKTtcbiAgICB9O1xuXG4gICAgbGlzdGVuZXIuXyA9IGNhbGxiYWNrXG4gICAgcmV0dXJuIHRoaXMub24obmFtZSwgbGlzdGVuZXIsIGN0eCk7XG4gIH0sXG5cbiAgZW1pdDogZnVuY3Rpb24gKG5hbWUpIHtcbiAgICB2YXIgZGF0YSA9IFtdLnNsaWNlLmNhbGwoYXJndW1lbnRzLCAxKTtcbiAgICB2YXIgZXZ0QXJyID0gKCh0aGlzLmUgfHwgKHRoaXMuZSA9IHt9KSlbbmFtZV0gfHwgW10pLnNsaWNlKCk7XG4gICAgdmFyIGkgPSAwO1xuICAgIHZhciBsZW4gPSBldnRBcnIubGVuZ3RoO1xuXG4gICAgZm9yIChpOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgIGV2dEFycltpXS5mbi5hcHBseShldnRBcnJbaV0uY3R4LCBkYXRhKTtcbiAgICB9XG5cbiAgICByZXR1cm4gdGhpcztcbiAgfSxcblxuICBvZmY6IGZ1bmN0aW9uIChuYW1lLCBjYWxsYmFjaykge1xuICAgIHZhciBlID0gdGhpcy5lIHx8ICh0aGlzLmUgPSB7fSk7XG4gICAgdmFyIGV2dHMgPSBlW25hbWVdO1xuICAgIHZhciBsaXZlRXZlbnRzID0gW107XG5cbiAgICBpZiAoZXZ0cyAmJiBjYWxsYmFjaykge1xuICAgICAgZm9yICh2YXIgaSA9IDAsIGxlbiA9IGV2dHMubGVuZ3RoOyBpIDwgbGVuOyBpKyspIHtcbiAgICAgICAgaWYgKGV2dHNbaV0uZm4gIT09IGNhbGxiYWNrICYmIGV2dHNbaV0uZm4uXyAhPT0gY2FsbGJhY2spXG4gICAgICAgICAgbGl2ZUV2ZW50cy5wdXNoKGV2dHNbaV0pO1xuICAgICAgfVxuICAgIH1cblxuICAgIC8vIFJlbW92ZSBldmVudCBmcm9tIHF1ZXVlIHRvIHByZXZlbnQgbWVtb3J5IGxlYWtcbiAgICAvLyBTdWdnZXN0ZWQgYnkgaHR0cHM6Ly9naXRodWIuY29tL2xhemRcbiAgICAvLyBSZWY6IGh0dHBzOi8vZ2l0aHViLmNvbS9zY290dGNvcmdhbi90aW55LWVtaXR0ZXIvY29tbWl0L2M2ZWJmYWE5YmM5NzNiMzNkMTEwYTg0YTMwNzc0MmI3Y2Y5NGM5NTMjY29tbWl0Y29tbWVudC01MDI0OTEwXG5cbiAgICAobGl2ZUV2ZW50cy5sZW5ndGgpXG4gICAgICA/IGVbbmFtZV0gPSBsaXZlRXZlbnRzXG4gICAgICA6IGRlbGV0ZSBlW25hbWVdO1xuXG4gICAgcmV0dXJuIHRoaXM7XG4gIH1cbn07XG5cbm1vZHVsZS5leHBvcnRzID0gRTtcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMjAvMjAxNS5cclxuICogVERTTSBpcyBhIGdsb2JhbCBvYmplY3QgdGhhdCBjb21lcyBmcm9tIEFwcC5qc1xyXG4gKlxyXG4gKiBUaGUgZm9sbG93aW5nIGhlbHBlciB3b3JrcyBpbiBhIHdheSB0byBtYWtlIGF2YWlsYWJsZSB0aGUgY3JlYXRpb24gb2YgRGlyZWN0aXZlLCBTZXJ2aWNlcyBhbmQgQ29udHJvbGxlclxyXG4gKiBvbiBmbHkgb3Igd2hlbiBkZXBsb3lpbmcgdGhlIGFwcC5cclxuICpcclxuICogV2UgcmVkdWNlIHRoZSB1c2Ugb2YgY29tcGlsZSBhbmQgZXh0cmEgc3RlcHNcclxuICovXHJcblxyXG52YXIgVERTVE0gPSByZXF1aXJlKCcuL0FwcC5qcycpO1xyXG5cclxuLyoqXHJcbiAqIExpc3RlbiB0byBhbiBleGlzdGluZyBkaWdlc3Qgb2YgdGhlIGNvbXBpbGUgcHJvdmlkZXIgYW5kIGV4ZWN1dGUgdGhlICRhcHBseSBpbW1lZGlhdGVseSBvciBhZnRlciBpdCdzIHJlYWR5XHJcbiAqIEBwYXJhbSBjdXJyZW50XHJcbiAqIEBwYXJhbSBmblxyXG4gKi9cclxuVERTVE0uc2FmZUFwcGx5ID0gZnVuY3Rpb24gKGN1cnJlbnQsIGZuKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICB2YXIgcGhhc2UgPSBjdXJyZW50LiRyb290LiQkcGhhc2U7XHJcbiAgICBpZiAocGhhc2UgPT09ICckYXBwbHknIHx8IHBoYXNlID09PSAnJGRpZ2VzdCcpIHtcclxuICAgICAgICBpZiAoZm4pIHtcclxuICAgICAgICAgICAgY3VycmVudC4kZXZhbChmbik7XHJcbiAgICAgICAgfVxyXG4gICAgfSBlbHNlIHtcclxuICAgICAgICBpZiAoZm4pIHtcclxuICAgICAgICAgICAgY3VycmVudC4kYXBwbHkoZm4pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGFwcGx5KCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3QgZGlyZWN0aXZlIGFzeW5jIGlmIHRoZSBjb21waWxlUHJvdmlkZXIgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVEaXJlY3RpdmUgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIpIHtcclxuICAgICAgICBURFNUTS5Qcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyLmRpcmVjdGl2ZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uZGlyZWN0aXZlKSB7XHJcbiAgICAgICAgVERTVE0uZGlyZWN0aXZlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3QgY29udHJvbGxlcnMgYXN5bmMgaWYgdGhlIGNvbnRyb2xsZXJQcm92aWRlciBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZUNvbnRyb2xsZXIgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5jb250cm9sbGVyUHJvdmlkZXIpIHtcclxuICAgICAgICBURFNUTS5jb250cm9sbGVyUHJvdmlkZXIucmVnaXN0ZXIoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmNvbnRyb2xsZXIpIHtcclxuICAgICAgICBURFNUTS5jb250cm9sbGVyKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEhlbHBlciB0byBpbmplY3Qgc2VydmljZSBhc3luYyBpZiB0aGUgcHJvdmlkZVNlcnZpY2UgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVTZXJ2aWNlID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2UpIHtcclxuICAgICAgICBURFNUTS5Qcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2Uuc2VydmljZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uY29udHJvbGxlcikge1xyXG4gICAgICAgIFREU1RNLnNlcnZpY2Uoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogRm9yIExlZ2FjeSBzeXN0ZW0sIHdoYXQgaXMgZG9lcyBpcyB0byB0YWtlIHBhcmFtcyBmcm9tIHRoZSBxdWVyeVxyXG4gKiBvdXRzaWRlIHRoZSBBbmd1bGFySlMgdWktcm91dGluZy5cclxuICogQHBhcmFtIHBhcmFtIC8vIFBhcmFtIHRvIHNlYXJjIGZvciAvZXhhbXBsZS5odG1sP2Jhcj1mb28jY3VycmVudFN0YXRlXHJcbiAqL1xyXG5URFNUTS5nZXRVUkxQYXJhbSA9IGZ1bmN0aW9uIChwYXJhbSkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgJC51cmxQYXJhbSA9IGZ1bmN0aW9uIChuYW1lKSB7XHJcbiAgICAgICAgdmFyIHJlc3VsdHMgPSBuZXcgUmVnRXhwKCdbXFw/Jl0nICsgbmFtZSArICc9KFteJiNdKiknKS5leGVjKHdpbmRvdy5sb2NhdGlvbi5ocmVmKTtcclxuICAgICAgICBpZiAocmVzdWx0cyA9PT0gbnVsbCkge1xyXG4gICAgICAgICAgICByZXR1cm4gbnVsbDtcclxuICAgICAgICB9XHJcbiAgICAgICAgZWxzZSB7XHJcbiAgICAgICAgICAgIHJldHVybiByZXN1bHRzWzFdIHx8IDA7XHJcbiAgICAgICAgfVxyXG4gICAgfTtcclxuXHJcbiAgICByZXR1cm4gJC51cmxQYXJhbShwYXJhbSk7XHJcbn07XHJcblxyXG4vKipcclxuICogVGhpcyBjb2RlIHdhcyBpbnRyb2R1Y2VkIG9ubHkgZm9yIHRoZSBpZnJhbWUgbWlncmF0aW9uXHJcbiAqIGl0IGRldGVjdCB3aGVuIG1vdXNlIGVudGVyXHJcbiAqL1xyXG5URFNUTS5pZnJhbWVMb2FkZXIgPSBmdW5jdGlvbiAoKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICAkKCcuaWZyYW1lTG9hZGVyJykuaG92ZXIoXHJcbiAgICAgICAgZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAkKCcubmF2YmFyLXVsLWNvbnRhaW5lciAuZHJvcGRvd24ub3BlbicpLnJlbW92ZUNsYXNzKCdvcGVuJyk7XHJcbiAgICAgICAgfSwgZnVuY3Rpb24gKCkge1xyXG4gICAgICAgIH1cclxuICAgICk7XHJcbn07XHJcblxyXG5cclxuLy8gSXQgd2lsbCBiZSByZW1vdmVkIGFmdGVyIHdlIHJpcCBvZmYgYWxsIGlmcmFtZXNcclxud2luZG93LlREU1RNID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8xNi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnJlcXVpcmUoJ2FuZ3VsYXInKTtcclxucmVxdWlyZSgnYW5ndWxhci1hbmltYXRlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItbW9ja3MnKTtcclxucmVxdWlyZSgnYW5ndWxhci1zYW5pdGl6ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXJlc291cmNlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdHJhbnNsYXRlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdHJhbnNsYXRlLWxvYWRlci1wYXJ0aWFsJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdWktYm9vdHN0cmFwJyk7XHJcbnJlcXVpcmUoJ25nQ2xpcGJvYXJkJyk7XHJcbnJlcXVpcmUoJ3VpLXJvdXRlcicpO1xyXG5yZXF1aXJlKCdyeC1hbmd1bGFyJyk7XHJcblxyXG4vLyBNb2R1bGVzXHJcbmltcG9ydCBIVFRQTW9kdWxlIGZyb20gJy4uL3NlcnZpY2VzL2h0dHAvSFRUUE1vZHVsZS5qcyc7XHJcbmltcG9ydCBSZXN0QVBJTW9kdWxlIGZyb20gJy4uL3NlcnZpY2VzL1Jlc3RBUEkvUmVzdEFQSU1vZHVsZS5qcydcclxuaW1wb3J0IEhlYWRlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2hlYWRlci9IZWFkZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTGljZW5zZUFkbWluTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZUFkbWluL0xpY2Vuc2VBZG1pbk1vZHVsZS5qcyc7XHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL0xpY2Vuc2VNYW5hZ2VyTW9kdWxlLmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL05vdGljZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy90YXNrTWFuYWdlci9UYXNrTWFuYWdlck1vZHVsZS5qcyc7XHJcblxyXG52YXIgUHJvdmlkZXJDb3JlID0ge307XHJcblxyXG52YXIgVERTVE0gPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0nLCBbXHJcbiAgICAnbmdTYW5pdGl6ZScsXHJcbiAgICAnbmdSZXNvdXJjZScsXHJcbiAgICAnbmdBbmltYXRlJyxcclxuICAgICdwYXNjYWxwcmVjaHQudHJhbnNsYXRlJywgLy8gJ2FuZ3VsYXItdHJhbnNsYXRlJ1xyXG4gICAgJ3VpLnJvdXRlcicsXHJcbiAgICAnbmdjbGlwYm9hcmQnLFxyXG4gICAgJ2tlbmRvLmRpcmVjdGl2ZXMnLFxyXG4gICAgJ3J4JyxcclxuICAgICd1aS5ib290c3RyYXAnLFxyXG4gICAgSFRUUE1vZHVsZS5uYW1lLFxyXG4gICAgUmVzdEFQSU1vZHVsZS5uYW1lLFxyXG4gICAgSGVhZGVyTW9kdWxlLm5hbWUsXHJcbiAgICBUYXNrTWFuYWdlck1vZHVsZS5uYW1lLFxyXG4gICAgTGljZW5zZUFkbWluTW9kdWxlLm5hbWUsXHJcbiAgICBMaWNlbnNlTWFuYWdlck1vZHVsZS5uYW1lLFxyXG4gICAgTm90aWNlTWFuYWdlck1vZHVsZS5uYW1lXHJcbl0pLmNvbmZpZyhbXHJcbiAgICAnJGxvZ1Byb3ZpZGVyJyxcclxuICAgICckcm9vdFNjb3BlUHJvdmlkZXInLFxyXG4gICAgJyRjb21waWxlUHJvdmlkZXInLFxyXG4gICAgJyRjb250cm9sbGVyUHJvdmlkZXInLFxyXG4gICAgJyRwcm92aWRlJyxcclxuICAgICckaHR0cFByb3ZpZGVyJyxcclxuICAgICckdHJhbnNsYXRlUHJvdmlkZXInLFxyXG4gICAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgJyR1cmxSb3V0ZXJQcm92aWRlcicsXHJcbiAgICAnJGxvY2F0aW9uUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRsb2dQcm92aWRlciwgJHJvb3RTY29wZVByb3ZpZGVyLCAkY29tcGlsZVByb3ZpZGVyLCAkY29udHJvbGxlclByb3ZpZGVyLCAkcHJvdmlkZSwgJGh0dHBQcm92aWRlcixcclxuICAgICAgICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIsICR1cmxSb3V0ZXJQcm92aWRlcikge1xyXG5cclxuICAgICAgICAkcm9vdFNjb3BlUHJvdmlkZXIuZGlnZXN0VHRsKDMwKTtcclxuXHJcbiAgICAgICAgJGxvZ1Byb3ZpZGVyLmRlYnVnRW5hYmxlZCh0cnVlKTtcclxuXHJcbiAgICAgICAgLy8gQWZ0ZXIgYm9vdHN0cmFwcGluZyBhbmd1bGFyIGZvcmdldCB0aGUgcHJvdmlkZXIgc2luY2UgZXZlcnl0aGluZyBcIndhcyBhbHJlYWR5IGxvYWRlZFwiXHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlciA9ICRjb21waWxlUHJvdmlkZXI7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmNvbnRyb2xsZXJQcm92aWRlciA9ICRjb250cm9sbGVyUHJvdmlkZXI7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlID0gJHByb3ZpZGU7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmh0dHBQcm92aWRlciA9ICRodHRwUHJvdmlkZXI7XHJcblxyXG4gICAgICAgIC8qKlxyXG4gICAgICAgICAqIFRyYW5zbGF0aW9uc1xyXG4gICAgICAgICAqL1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIudXNlU2FuaXRpemVWYWx1ZVN0cmF0ZWd5KG51bGwpO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ3Rkc3RtJyk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci51c2VMb2FkZXIoJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyJywge1xyXG4gICAgICAgICAgICB1cmxUZW1wbGF0ZTogJy4uL2kxOG4ve3BhcnR9L2FwcC5pMThuLXtsYW5nfS5qc29uJ1xyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIucHJlZmVycmVkTGFuZ3VhZ2UoJ2VuX1VTJyk7XHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLmZhbGxiYWNrTGFuZ3VhZ2UoJ2VuX1VTJyk7XHJcblxyXG4gICAgICAgIC8vJHVybFJvdXRlclByb3ZpZGVyLm90aGVyd2lzZSgnZGFzaGJvYXJkJyk7XHJcblxyXG4gICAgfV0pLlxyXG4gICAgcnVuKFsnJHJvb3RTY29wZScsICckaHR0cCcsICckbG9nJywgJyRsb2NhdGlvbicsIGZ1bmN0aW9uICgkcm9vdFNjb3BlLCAkaHR0cCwgJGxvZywgJGxvY2F0aW9uLCAkc3RhdGUsICRzdGF0ZVBhcmFtcywgJGxvY2FsZSkge1xyXG4gICAgICAgICRsb2cuZGVidWcoJ0NvbmZpZ3VyYXRpb24gZGVwbG95ZWQnKTtcclxuXHJcbiAgICAgICAgJHJvb3RTY29wZS4kb24oJyRzdGF0ZUNoYW5nZVN0YXJ0JywgZnVuY3Rpb24gKGV2ZW50LCB0b1N0YXRlLCB0b1BhcmFtcywgZnJvbVN0YXRlLCBmcm9tUGFyYW1zKSB7XHJcbiAgICAgICAgICAgICRsb2cuZGVidWcoJ1N0YXRlIENoYW5nZSB0byAnICsgdG9TdGF0ZS5uYW1lKTtcclxuICAgICAgICAgICAgaWYgKHRvU3RhdGUuZGF0YSAmJiB0b1N0YXRlLmRhdGEucGFnZSkge1xyXG4gICAgICAgICAgICAgICAgd2luZG93LmRvY3VtZW50LnRpdGxlID0gdG9TdGF0ZS5kYXRhLnBhZ2UudGl0bGU7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICB9XSk7XHJcblxyXG4vLyB3ZSBtYXBwZWQgdGhlIFByb3ZpZGVyIENvcmUgbGlzdCAoY29tcGlsZVByb3ZpZGVyLCBjb250cm9sbGVyUHJvdmlkZXIsIHByb3ZpZGVTZXJ2aWNlLCBodHRwUHJvdmlkZXIpIHRvIHJldXNlIGFmdGVyIG9uIGZseVxyXG5URFNUTS5Qcm92aWRlckNvcmUgPSBQcm92aWRlckNvcmU7XHJcblxyXG5tb2R1bGUuZXhwb3J0cyA9IFREU1RNOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTQvMjAxNS5cclxuICogSXQgaGFuZGxlciB0aGUgaW5kZXggZm9yIGFueSBvZiB0aGUgZGlyZWN0aXZlcyBhdmFpbGFibGVcclxuICovXHJcblxyXG5yZXF1aXJlKCcuL3Rvb2xzL1RvYXN0SGFuZGxlci5qcycpO1xyXG5yZXF1aXJlKCcuL3Rvb2xzL01vZGFsV2luZG93QWN0aXZhdGlvbi5qcycpO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMwLzEwLzIwMTYuXHJcbiAqIExpc3RlbiB0byBNb2RhbCBXaW5kb3cgdG8gbWFrZSBhbnkgbW9kYWwgd2luZG93IGRyYWdnYWJibGVcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgnbW9kYWxSZW5kZXInLCBbJyRsb2cnLCBmdW5jdGlvbiAoJGxvZykge1xyXG4gICAgJGxvZy5kZWJ1ZygnTW9kYWxXaW5kb3dBY3RpdmF0aW9uIGxvYWRlZCcpO1xyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICByZXN0cmljdDogJ0VBJyxcclxuICAgICAgICBsaW5rOiBmdW5jdGlvbigpIHtcclxuICAgICAgICAgICAgJCgnLm1vZGFsLWRpYWxvZycpLmRyYWdnYWJsZSh7XHJcbiAgICAgICAgICAgICAgICBoYW5kbGU6ICcubW9kYWwtaGVhZGVyJ1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG59XSk7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBQcmludHMgb3V0IGFsbCBUb2FzdCBtZXNzYWdlIHdoZW4gZGV0ZWN0ZWQgZnJvbSBzZXJ2ZXIgb3IgY3VzdG9tIG1zZyB1c2luZyB0aGUgZGlyZWN0aXZlIGl0c2VsZlxyXG4gKlxyXG4gKiBQcm9iYWJseSB2YWx1ZXMgYXJlOlxyXG4gKlxyXG4gKiBzdWNjZXNzLCBkYW5nZXIsIGluZm8sIHdhcm5pbmdcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgndG9hc3RIYW5kbGVyJywgWyckbG9nJywgJyR0aW1lb3V0JywgJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLFxyXG4gICAgJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicsICdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICBmdW5jdGlvbiAoJGxvZywgJHRpbWVvdXQsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLFxyXG4gICAgICAgICAgICAgIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciwgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IpIHtcclxuXHJcbiAgICAkbG9nLmRlYnVnKCdUb2FzdEhhbmRsZXIgbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHNjb3BlOiB7XHJcbiAgICAgICAgICAgIG1zZzogJz0nLFxyXG4gICAgICAgICAgICB0eXBlOiAnPScsXHJcbiAgICAgICAgICAgIHN0YXR1czogJz0nXHJcbiAgICAgICAgfSxcclxuICAgICAgICBwcmlvcml0eTogNSxcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9kaXJlY3RpdmVzL1Rvb2xzL1RvYXN0SGFuZGxlci5odG1sJyxcclxuICAgICAgICByZXN0cmljdDogJ0UnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6IFsnJHNjb3BlJywgJyRyb290U2NvcGUnLCBmdW5jdGlvbiAoJHNjb3BlLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgICAgICRzY29wZS5hbGVydCA9IHtcclxuICAgICAgICAgICAgICAgIHN1Y2Nlc3M6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgZGFuZ2VyOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGluZm86IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgd2FybmluZzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIH07XHJcblxyXG4gICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3MgPSB7XHJcbiAgICAgICAgICAgICAgICBzaG93OiBmYWxzZVxyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgZnVuY3Rpb24gdHVybk9mZk5vdGlmaWNhdGlvbnMoKXtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5zdWNjZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmluZm8uc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0Lndhcm5pbmcuc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIExpc3RlbiB0byBhbnkgcmVxdWVzdCwgd2UgY2FuIHJlZ2lzdGVyIGxpc3RlbmVyIGlmIHdlIHdhbnQgdG8gYWRkIGV4dHJhIGNvZGUuXHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5SZXF1ZXN0KCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihjb25maWcpe1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVxdWVzdCB0bzogJywgIGNvbmZpZyk7XHJcbiAgICAgICAgICAgICAgICB2YXIgdGltZSA9IGNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wO1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1Zyh0aW1lKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLmxpc3RlbkVycm9yKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZWplY3Rpb24pe1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVxdWVzdCBlcnJvcjogJywgIHJlamVjdGlvbik7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5SZXNwb25zZSgpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVzcG9uc2Upe1xyXG4gICAgICAgICAgICAgICAgdmFyIHRpbWUgPSByZXNwb25zZS5jb25maWcucmVzcG9uc2VUaW1lc3RhbXAgLSByZXNwb25zZS5jb25maWcucmVxdWVzdFRpbWVzdGFtcDtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1RoZSByZXF1ZXN0IHRvb2sgJyArICh0aW1lIC8gMTAwMCkgKyAnIHNlY29uZHMnKTtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1Jlc3BvbnNlIHJlc3VsdDogJywgcmVzcG9uc2UpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1Jlc3BvbnNlIGVycm9yOiAnLCByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnN0YXR1cyA9IHJlamVjdGlvbi5zdGF0dXM7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnN0YXR1c1RleHQgPSByZWplY3Rpb24uc3RhdHVzVGV4dDtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuZXJyb3JzID0gcmVqZWN0aW9uLmRhdGEuZXJyb3JzO1xyXG4gICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsIDMwMDApO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBIaWRlIHRoZSBQb3AgdXAgbm90aWZpY2F0aW9uIG1hbnVhbGx5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUub25DYW5jZWxQb3BVcCA9IGZ1bmN0aW9uKCkge1xyXG4gICAgICAgICAgICAgICAgdHVybk9mZk5vdGlmaWNhdGlvbnMoKTtcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBJdCB3YXRjaCB0aGUgdmFsdWUgdG8gc2hvdyB0aGUgbXNnIGlmIG5lY2Vzc2FyeVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHJvb3RTY29wZS4kb24oJ2Jyb2FkY2FzdC1tc2cnLCBmdW5jdGlvbihldmVudCwgYXJncykge1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnYnJvYWRjYXN0LW1zZyBleGVjdXRlZCcpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zdGF0dXNUZXh0ID0gYXJncy50ZXh0O1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc3RhdHVzID0gbnVsbDtcclxuICAgICAgICAgICAgICAgICR0aW1lb3V0KHR1cm5PZmZOb3RpZmljYXRpb25zLCAyMDAwKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS4kYXBwbHkoKTsgLy8gcm9vdFNjb3BlIGFuZCB3YXRjaCBleGNsdWRlIHRoZSBhcHBseSBhbmQgbmVlZHMgdGhlIG5leHQgY3ljbGUgdG8gcnVuXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUuJHdhdGNoKCdtc2cnLCBmdW5jdGlvbihuZXdWYWx1ZSwgb2xkVmFsdWUpIHtcclxuICAgICAgICAgICAgICAgIGlmIChuZXdWYWx1ZSAmJiBuZXdWYWx1ZSAhPT0gJycpIHtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzVGV4dCA9IG5ld1ZhbHVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzID0gJHNjb3BlLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMjUwMCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICB9XVxyXG4gICAgfTtcclxufV0pO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzE3LzIwMTUuXHJcbiAqL1xyXG5cclxuLy8gTWFpbiBBbmd1bGFySnMgY29uZmlndXJhdGlvblxyXG5yZXF1aXJlKCcuL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcbi8vIEhlbHBlcnNcclxucmVxdWlyZSgnLi9jb25maWcvQW5ndWxhclByb3ZpZGVySGVscGVyLmpzJyk7XHJcblxyXG4vLyBEaXJlY3RpdmVzXHJcbnJlcXVpcmUoJy4vZGlyZWN0aXZlcy9pbmRleCcpO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRGlhbG9nQWN0aW9uIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy50aXRsZSA9IHBhcmFtcy50aXRsZTtcclxuICAgICAgICB0aGlzLm1lc3NhZ2UgPSBwYXJhbXMubWVzc2FnZTtcclxuXHJcbiAgICB9XHJcbiAgICAvKipcclxuICAgICAqIEFjY2NlcHQgYW5kIENvbmZpcm1cclxuICAgICAqL1xyXG4gICAgY29uZmlybUFjdGlvbigpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yLzIwMTUuXHJcbiAqIEhlYWRlciBDb250cm9sbGVyIG1hbmFnZSB0aGUgdmlldyBhdmFpbGFibGUgb24gdGhlIHN0YXRlLmRhdGFcclxuICogLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gKiBIZWFkZXIgQ29udHJvbGxlclxyXG4gKiBQYWdlIHRpdGxlICAgICAgICAgICAgICAgICAgICAgIEhvbWUgLT4gTGF5b3V0IC0gU3ViIExheW91dFxyXG4gKlxyXG4gKiBNb2R1bGUgQ29udHJvbGxlclxyXG4gKiBDb250ZW50XHJcbiAqIC0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIZWFkZXJDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2dcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG5cclxuICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICBpbnN0cnVjdGlvbjogJycsXHJcbiAgICAgICAgICAgIG1lbnU6IFtdXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hlYWRlciBDb250cm9sbGVyIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmVyaWZ5IGlmIHdlIGhhdmUgYSBtZW51IHRvIHNob3cgdG8gbWFkZSBpdCBhdmFpbGFibGUgdG8gdGhlIFZpZXdcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUhlYWRlcigpIHtcclxuICAgICAgICBpZiAodGhpcy5zdGF0ZSAmJiB0aGlzLnN0YXRlLiRjdXJyZW50ICYmIHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YSkge1xyXG4gICAgICAgICAgICB0aGlzLnBhZ2VNZXRhRGF0YSA9IHRoaXMuc3RhdGUuJGN1cnJlbnQuZGF0YS5wYWdlO1xyXG4gICAgICAgICAgICBkb2N1bWVudC50aXRsZSA9IHRoaXMucGFnZU1ldGFEYXRhLnRpdGxlO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIxLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgSGVhZGVyQ29udHJvbGxlciBmcm9tICcuL0hlYWRlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgRGlhbG9nQWN0aW9uIGZyb20gJy4uL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uanMnO1xyXG5cclxudmFyIEhlYWRlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5IZWFkZXJNb2R1bGUnLCBbXSk7XHJcblxyXG5IZWFkZXJNb2R1bGUuY29udHJvbGxlcignSGVhZGVyQ29udHJvbGxlcicsIFsnJGxvZycsICckc3RhdGUnLCBIZWFkZXJDb250cm9sbGVyXSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbkhlYWRlck1vZHVsZS5jb250cm9sbGVyKCdEaWFsb2dBY3Rpb24nLCBbJyRsb2cnLCckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRGlhbG9nQWN0aW9uXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIZWFkZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTGljZW5zZUFkbWluTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZUFkbWluTGlzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlQWRtaW5TZXJ2aWNlIGZyb20gJy4vc2VydmljZS9MaWNlbnNlQWRtaW5TZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RMaWNlbnNlIGZyb20gJy4vcmVxdWVzdC9SZXF1ZXN0TGljZW5zZS5qcyc7XHJcbmltcG9ydCBDcmVhdGVkTGljZW5zZSBmcm9tICcuL2NyZWF0ZWQvQ3JlYXRlZExpY2Vuc2UuanMnO1xyXG5pbXBvcnQgQXBwbHlMaWNlbnNlS2V5IGZyb20gJy4vYXBwbHlMaWNlbnNlS2V5L0FwcGx5TGljZW5zZUtleS5qcyc7XHJcbmltcG9ydCBNYW51YWxseVJlcXVlc3QgZnJvbSAnLi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZURldGFpbC5qcyc7XHJcblxyXG5cclxudmFyIExpY2Vuc2VBZG1pbk1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlQWRtaW5Nb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ2xpY2Vuc2VBZG1pbicpO1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlclZpZXcuaHRtbCcsXHJcbiAgICAgICAgY29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG4gICAgfTtcclxuXHJcbiAgICAkc3RhdGVQcm92aWRlclxyXG4gICAgICAgIC5zdGF0ZSgnbGljZW5zZUFkbWluTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0FkbWluaXN0ZXIgTGljZW5zZXMnLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnQURNSU4nLCAnTElDRU5TRScsICdMSVNUJ119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvYWRtaW4vbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2xpc3QvTGljZW5zZUFkbWluTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZUFkbWluTGlzdCBhcyBsaWNlbnNlQWRtaW5MaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsICckcm9vdFNjb3BlJywgTGljZW5zZUFkbWluU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VBZG1pbkxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTGljZW5zZUFkbWluTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignUmVxdWVzdExpY2Vuc2UnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgUmVxdWVzdExpY2Vuc2VdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0NyZWF0ZWRMaWNlbnNlJywgWyckbG9nJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIENyZWF0ZWRMaWNlbnNlXSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdBcHBseUxpY2Vuc2VLZXknLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIEFwcGx5TGljZW5zZUtleV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTWFudWFsbHlSZXF1ZXN0JywgWyckbG9nJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTWFudWFsbHlSZXF1ZXN0XSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlRGV0YWlsJywgWyckbG9nJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIExpY2Vuc2VEZXRhaWxdKTtcclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBMaWNlbnNlQWRtaW5Nb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBBcHBseUxpY2Vuc2VLZXkgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgc3VwZXIoJGxvZywgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKVxyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIGtleTogcGFyYW1zLmxpY2Vuc2Uua2V5XHJcbiAgICAgICAgfVxyXG4gICAgICAgIDtcclxuICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgYW5kIHZhbGlkYXRlIHRoZSBLZXkgaXMgY29ycmVjdFxyXG4gICAgICovXHJcbiAgICBhcHBseUtleSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuYXBwbHlMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9LCAoZGF0YSk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIENyZWF0ZWRSZXF1ZXN0TGljZW5zZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMuY2xpZW50ID0gcGFyYW1zO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZURldGFpbCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0kdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBtZXRob2RJZDogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLmlkLFxyXG4gICAgICAgICAgICBwcm9qZWN0TmFtZTogcGFyYW1zLmxpY2Vuc2UucHJvamVjdC5uYW1lLFxyXG4gICAgICAgICAgICBjbGllbnROYW1lOiBwYXJhbXMubGljZW5zZS5jbGllbnQubmFtZSxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBzZXJ2ZXJzVG9rZW5zOiBwYXJhbXMubGljZW5zZS5tZXRob2QubWF4LFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudE5hbWU6IHBhcmFtcy5saWNlbnNlLmVudmlyb25tZW50Lm5hbWUsXHJcbiAgICAgICAgICAgIGluY2VwdGlvbjogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdERhdGUsXHJcbiAgICAgICAgICAgIGV4cGlyYXRpb246IHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlLFxyXG4gICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0Tm90ZSxcclxuICAgICAgICAgICAgYWN0aXZlOiBwYXJhbXMubGljZW5zZS5zdGF0dXMuaWQgPT09IDEsXHJcbiAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgcmVwbGFjZWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkLFxyXG4gICAgICAgICAgICBlbmNyeXB0ZWREZXRhaWw6IHBhcmFtcy5saWNlbnNlLmVuY3J5cHRlZERldGFpbCxcclxuICAgICAgICAgICAgYXBwbGllZDogZmFsc2VcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVNZXRob2RPcHRpb25zKCk7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdTZXJ2ZXJzJ1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbnMnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAzLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ0N1c3RvbSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRoZSB1c2VyIGFwcGx5IGFuZCBzZXJ2ZXIgc2hvdWxkIHZhbGlkYXRlIHRoZSBrZXkgaXMgY29ycmVjdFxyXG4gICAgICovXHJcbiAgICBhcHBseUxpY2Vuc2VLZXkoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2FwcGx5TGljZW5zZUtleS9BcHBseUxpY2Vuc2VLZXkuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdBcHBseUxpY2Vuc2VLZXkgYXMgYXBwbHlMaWNlbnNlS2V5JyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgbGljZW5zZTogdGhpcy5saWNlbnNlTW9kZWwgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQgPSBkYXRhLnN1Y2Nlc3M7XHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuYWN0aXZlID0gZGF0YS5zdWNjZXNzO1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKHsgaWQ6IHRoaXMubGljZW5zZU1vZGVsLmlkLCB1cGRhdGVkOiB0cnVlfSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE9wZW5zIGEgZGlhbG9nIGFuZCBhbGxvdyB0aGUgdXNlciB0byBtYW51YWxseSBzZW5kIHRoZSByZXF1ZXN0IG9yIGNvcHkgdGhlIGVuY3JpcHRlZCBjb2RlXHJcbiAgICAgKi9cclxuICAgIG1hbnVhbGx5UmVxdWVzdCgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vbWFudWFsbHlSZXF1ZXN0L01hbnVhbGx5UmVxdWVzdC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ01hbnVhbGx5UmVxdWVzdCBhcyBtYW51YWxseVJlcXVlc3QnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiB0aGlzLmxpY2Vuc2VNb2RlbCB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge30pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcbiAgICAgKi9cclxuICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3QoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLnJlc3VibWl0TGljZW5zZVJlcXVlc3QodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byBkZWxldGUgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5kZWxldGVMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VNb2RlbC5hcHBsaWVkKSB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSgpO1xyXG4gICAgICAgIH1cclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZUFkbWluTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlQWRtaW5MaXN0IEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3Qub25SZXF1ZXN0TmV3TGljZW5zZSgpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IFJlcXVlc3QgTmV3IExpY2Vuc2U8L2J1dHRvbj4gPGRpdiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3QucmVsb2FkTGljZW5zZUFkbWluTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2xpY2Vuc2VJZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vbkxpY2Vuc2VEZXRhaWxzKHRoaXMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjbGllbnQubmFtZScsIHRpdGxlOiAnQ2xpZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLCB0aXRsZTogJ1Byb2plY3QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2VtYWlsJywgdGl0bGU6ICdDb250YWN0IEVtYWlsJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMubmFtZScsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5uYW1lJywgdGl0bGU6ICdNZXRob2QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5pZCcsIGhpZGRlbjogdHJ1ZX0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubWF4JywgdGl0bGU6ICdTZXJ2ZXIvVG9rZW5zJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdyZXF1ZXN0RGF0ZScsIHRpdGxlOiAnSW5jZXB0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZXhwaXJhdGlvbkRhdGUnLCB0aXRsZTogJ0V4cGlyYXRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbnZpcm9ubWVudC5uYW1lJywgdGl0bGU6ICdFbnYuJ31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAncHJvamVjdC5uYW1lJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdExpY2Vuc2VJZCAhPT0gMCAmJiB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdmFyIGxhc3RMaWNlbnNlID0gdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhLmZpbmQoKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBsaWNlbnNlLmlkID09PSB0aGlzLm9wZW5MYXN0TGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobGFzdExpY2Vuc2UpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlRGV0YWlscyhsYXN0TGljZW5zZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0TGljZW5zZSBhcyByZXF1ZXN0TGljZW5zZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdOZXcgTGljZW5zZSBDcmVhdGVkOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICAgICAgdGhpcy5vbk5ld0xpY2Vuc2VDcmVhdGVkKGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZURldGFpbHMobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09wZW4gRGV0YWlscyBmb3I6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9kZXRhaWwvTGljZW5zZURldGFpbC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VEZXRhaWwgYXMgbGljZW5zZURldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcbiAgICAgICAgICAgIGlmKGRhdGEudXBkYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IGRhdGEuaWQ7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25OZXdMaWNlbnNlQ3JlYXRlZChsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmh0bWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQ3JlYXRlZExpY2Vuc2UgYXMgY3JlYXRlZExpY2Vuc2UnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBlbWFpbDogbGljZW5zZS5lbWFpbCAgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBNYW51YWxseVJlcXVlc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6ICBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBlbmNyeXB0ZWREZXRhaWw6ICcnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgLy8gR2V0IHRoZSBoYXNoIGNvZGUgdXNpbmcgdGhlIGlkLlxyXG4gICAgICAgIHRoaXMuZ2V0SGFzaENvZGUoKTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0SGFzaENvZGUoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldEhhc2hDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbmNyeXB0ZWREZXRhaWwgPSAnLS0tLS1CRUdJTiBMSUNFTlNFIFJFUVVFU1QtLS0tLVxcbicgKyBkYXRhICsgJ1xcbi0tLS0tRU5EIExJQ0VOU0UgUkVRVUVTVC0tLS0tJztcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKiBDcmVhdGUgYSBuZXcgUmVxdWVzdCB0byBnZXQgYSBMaWNlbnNlXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RMaWNlbnNlIGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICAvKipcclxuICAgICAqIEluaXRpYWxpemUgYWxsIHRoZSBwcm9wZXJ0aWVzXHJcbiAgICAgKiBAcGFyYW0gJGxvZ1xyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VBZG1pblNlcnZpY2VcclxuICAgICAqIEBwYXJhbSAkdWliTW9kYWxJbnN0YW5jZVxyXG4gICAgICovXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgRW52aXJvbm1lbnQgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UgPSBbXTtcclxuICAgICAgICAvLyBEZWZpbmUgdGhlIFByb2plY3QgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSBbXTtcclxuXHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldFByb2plY3REYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIENyZWF0ZSB0aGUgTW9kZWwgZm9yIHRoZSBOZXcgTGljZW5zZVxyXG4gICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBlbWFpbDogJycsXHJcbiAgICAgICAgICAgIGVudmlyb25tZW50SWQ6IDAsXHJcbiAgICAgICAgICAgIHByb2plY3RJZDogMCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogJycsXHJcbiAgICAgICAgICAgIHJlcXVlc3ROb3RlOiAnJ1xyXG4gICAgICAgIH1cclxuXHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKT0+e1xyXG4gICAgICAgICAgICB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSA9IGRhdGE7XHJcbiAgICAgICAgICAgIGlmKHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgICAgICB2YXIgaW5kZXggPSB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZS5maW5kSW5kZXgoZnVuY3Rpb24oZW52aXJvbWVudCl7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGVudmlyb21lbnQubmFtZSAgPT09ICdQcm9kdWN0aW9uJztcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgaW5kZXggPSBpbmRleCB8fCAwO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuZW52aXJvbm1lbnRJZCA9IGRhdGFbaW5kZXhdLmlkO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIFByb2plY3QgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLnByb2plY3RJZCA9IGRhdGFbMF0uaWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZSxcclxuICAgICAgICAgICAgc2VsZWN0OiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE9uIFByb2plY3QgQ2hhbmdlLCBzZWxlY3QgdGhlIENsaWVudCBOYW1lXHJcbiAgICAgICAgICAgICAgICB2YXIgaXRlbSA9IHRoaXMuc2VsZWN0UHJvamVjdC5kYXRhSXRlbShlLml0ZW0pO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuY2xpZW50TmFtZSA9IGl0ZW0uY2xpZW50Lm5hbWU7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBnZW5lcmF0ZSBhIG5ldyBMaWNlbnNlIHJlcXVlc3RcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2VSZXF1ZXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIFJlcXVlc3RlZDogJywgdGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QodGhpcy5uZXdMaWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UodGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZUFkbWluU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcbiAgICAgICAgdGhpcy5yb290U2NvcGUgPSAkcm9vdFNjb3BlO1xyXG4gICAgICAgIHRoaXMuc3RhdHVzU3VjY2VzcyA9ICdzdWNjZXNzJztcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnbGljZW5zZUFkbWluU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgb25TdWNjZXNzKXtcclxuICAgICAgICBuZXdMaWNlbnNlLmVudmlyb25tZW50SWQgPSBwYXJzZUludChuZXdMaWNlbnNlLmVudmlyb25tZW50SWQpO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkucmVzdWJtaXRMaWNlbnNlUmVxdWVzdChsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnUmVxdWVzdCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZW1haWxSZXF1ZXN0KGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmVtYWlsUmVxdWVzdChsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnUmVxdWVzdCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiAgQXBwbHkgVGhlIExpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gb25TdWNjZXNzXHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuXHJcbiAgICAgICAgdmFyIGhhc2ggPSAge1xyXG4gICAgICAgICAgICBoYXNoOiBsaWNlbnNlLmtleVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5hcHBseUxpY2Vuc2UobGljZW5zZS5pZCwgaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3IoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKHsgc3VjY2VzczogdHJ1ZX0pO1xyXG5cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5kZWxldGVMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RJbXBvcnQgZnJvbSAnLi9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbGljZW5zZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VNYW5hZ2VyTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0xpY2Vuc2luZyBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ01BTkFHRVInLCAnTElDRU5TRScsICdMSVNUJ119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvbWFuYWdlci9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJMaXN0IGFzIGxpY2Vuc2VNYW5hZ2VyTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgJyRyb290U2NvcGUnLCBMaWNlbnNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlTWFuYWdlckxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlTWFuYWdlckxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignUmVxdWVzdEltcG9ydCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RJbXBvcnRdKTtcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZU1hbmFnZXJEZXRhaWwnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTGljZW5zZU1hbmFnZXJEZXRhaWxdKTtcclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBMaWNlbnNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyRGV0YWlsIGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgc3VwZXIoJGxvZywgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0kdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIG93bmVyTmFtZTogcGFyYW1zLmxpY2Vuc2Uub3duZXIubmFtZSxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBwcm9qZWN0SWQ6IHBhcmFtcy5saWNlbnNlLnByb2plY3QuaWQsXHJcbiAgICAgICAgICAgIGNsaWVudElkOiBwYXJhbXMubGljZW5zZS5jbGllbnQuaWQsXHJcbiAgICAgICAgICAgIGNsaWVudE5hbWU6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5uYW1lLFxyXG4gICAgICAgICAgICBzdGF0dXNJZDogcGFyYW1zLmxpY2Vuc2Uuc3RhdHVzLmlkLFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5tZXRob2QuaWQsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiBwYXJhbXMubGljZW5zZS5tZXRob2QubmFtZSxcclxuICAgICAgICAgICAgICAgIG1heDogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLm1heCxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnQ6IHsgaWQ6IHBhcmFtcy5saWNlbnNlLmVudmlyb25tZW50LmlkIH0sXHJcbiAgICAgICAgICAgIHJlcXVlc3REYXRlOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0RGF0ZSxcclxuICAgICAgICAgICAgaW5pdERhdGU6IChwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSAhPT0gbnVsbCk/IGFuZ3VsYXIuY29weShwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSkgOiAnJyxcclxuICAgICAgICAgICAgZW5kRGF0ZTogKHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlICE9PSBudWxsKT8gYW5ndWxhci5jb3B5KHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlKSA6ICcnLFxyXG4gICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0Tm90ZSxcclxuICAgICAgICAgICAgd2Vic2l0ZU5hbWU6IHBhcmFtcy5saWNlbnNlLndlYnNpdGVuYW1lLFxyXG5cclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogcGFyYW1zLmxpY2Vuc2UuYmFubmVyTWVzc2FnZSxcclxuICAgICAgICAgICAgcmVxdWVzdGVkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3RlZElkLFxyXG4gICAgICAgICAgICByZXBsYWNlZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWQsXHJcbiAgICAgICAgICAgIHJlcGxhY2VkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkSWQsXHJcbiAgICAgICAgICAgIGFjdGl2aXR5TGlzdDogcGFyYW1zLmxpY2Vuc2UuYWN0aXZpdHlMaXN0LFxyXG4gICAgICAgICAgICBob3N0TmFtZTogcGFyYW1zLmxpY2Vuc2UuaG9zdE5hbWUsXHJcbiAgICAgICAgICAgIGhhc2g6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG5cclxuICAgICAgICAgICAgYXBwbGllZDogcGFyYW1zLmxpY2Vuc2UuYXBwbGllZCxcclxuICAgICAgICAgICAga2V5SWQ6IHBhcmFtcy5saWNlbnNlLmtleUlkXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgLy8gQ3JlYXRlcyB0aGUgUHJvamVjdCBTZWxlY3QgTGlzdFxyXG4gICAgICAgIC8vIERlZmluZSB0aGUgUHJvamVjdCBTZWxlY3RcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3QgPSB7fTtcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3RMaXN0T3B0aW9ucyA9IFtdO1xyXG4gICAgICAgIHRoaXMuZ2V0UHJvamVjdERhdGFTb3VyY2UoKTtcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgRW52aXJvbm1lbnQgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RFbnZpcm9ubWVudCA9IHt9O1xyXG4gICAgICAgIHRoaXMuc2VsZWN0RW52aXJvbm1lbnRMaXN0T3B0aW9ucyA9IFtdO1xyXG4gICAgICAgIHRoaXMuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIERlZmluZWQgdGhlIFN0YXR1cyBTZWxlY3QgTGlzdFxyXG4gICAgICAgIHRoaXMuc2VsZWN0U3RhdHVzID0gW107XHJcbiAgICAgICAgdGhpcy5nZXRTdGF0dXNEYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIEluaXQgdGhlIHR3byBLZW5kbyBEYXRlcyBmb3IgSW5pdCBhbmQgRW5kRGF0ZVxyXG4gICAgICAgIHRoaXMuaW5pdERhdGUgPSB7fTtcclxuICAgICAgICB0aGlzLmluaXREYXRlT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZm9ybWF0OiAneXl5eS9NTS9kZCcsXHJcbiAgICAgICAgICAgIG9wZW46ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUluaXREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pLFxyXG4gICAgICAgICAgICBjaGFuZ2U6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUluaXREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5lbmREYXRlID0ge307XHJcbiAgICAgICAgdGhpcy5lbmREYXRlT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZm9ybWF0OiAneXl5eS9NTS9kZCcsXHJcbiAgICAgICAgICAgIG9wZW46ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUVuZERhdGUoKTtcclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgICAgIGNoYW5nZTogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlRW5kRGF0ZSgpO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcblxyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVNZXRob2RPcHRpb25zKCk7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlTGljZW5zZUtleSgpO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUFjdGl2aXR5TGlzdCgpO1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG5cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBQcm9qZWN0IGRyb3Bkb3duIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3RMaXN0T3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlmKCF0aGlzLmxpY2Vuc2VNb2RlbC5wcm9qZWN0SWQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5wcm9qZWN0SWQgPSBkYXRhWzBdLmlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSlcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGFUZXh0RmllbGQ6ICduYW1lJyxcclxuICAgICAgICAgICAgZGF0YVZhbHVlRmllbGQ6ICdpZCcsXHJcbiAgICAgICAgICAgIHZhbHVlUHJpbWl0aXZlOiB0cnVlLFxyXG4gICAgICAgICAgICBzZWxlY3Q6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgLy8gT24gUHJvamVjdCBDaGFuZ2UsIHNlbGVjdCB0aGUgQ2xpZW50IE5hbWVcclxuICAgICAgICAgICAgICAgIHZhciBpdGVtID0gdGhpcy5zZWxlY3RQcm9qZWN0LmRhdGFJdGVtKGUuaXRlbSk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5jbGllbnROYW1lID0gaXRlbS5jbGllbnQubmFtZTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ29udHJvbHMgd2hhdCBidXR0b25zIHRvIHNob3dcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCkge1xyXG4gICAgICAgIHRoaXMucGVuZGluZ0xpY2Vuc2UgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gNCAmJiAhdGhpcy5lZGl0TW9kZTtcclxuICAgICAgICB0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgPSAodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDIgfHwgdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDMpO1xyXG4gICAgICAgIHRoaXMuYWN0aXZlU2hvd01vZGUgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMSAmJiAhdGhpcy5leHBpcmVkT3JUZXJtaW5hdGVkICYmICF0aGlzLmVkaXRNb2RlO1xyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVNZXRob2RPcHRpb25zKCkge1xyXG4gICAgICAgIHRoaXMubWV0aG9kT3B0aW9ucyA9IFtcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnU2VydmVycycsXHJcbiAgICAgICAgICAgICAgICBtYXg6IDBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnVG9rZW5zJyxcclxuICAgICAgICAgICAgICAgIG1heDogMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMyxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdDdXN0b20nXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdXHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUxpY2Vuc2VLZXkoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0S2V5Q29kZSh0aGlzLmxpY2Vuc2VNb2RlbC5pZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlS2V5ID0gJy0tLS0tQkVHSU4gTElDRU5TRSBSRVFVRVNULS0tLS1cXG4nICsgZGF0YSArICdcXG4tLS0tLUVORCBMSUNFTlNFIFJFUVVFU1QtLS0tLSc7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUFjdGl2aXR5TGlzdCgpIHtcclxuICAgICAgICB0aGlzLmFjdGl2aXR5R3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZGF0ZScsIHRpdGxlOiAnRGF0ZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnd2hvbScsIHRpdGxlOiAnV2hvbSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB0aGlzLmxpY2Vuc2VNb2RlbC5hY3Rpdml0eUxpc3QsXHJcbiAgICAgICAgICAgIHNjcm9sbGFibGU6IHRydWVcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhlIHVzZXIgYXBwbHkgYW5kIHNlcnZlciBzaG91bGQgdmFsaWRhdGUgdGhlIGtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZUtleSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQXBwbHlMaWNlbnNlS2V5IGFzIGFwcGx5TGljZW5zZUtleScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQgPSB0cnVlO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcbiAgICAgKi9cclxuICAgIGFjdGl2YXRlTGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5hY3RpdmF0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byByZXZva2UgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnJldm9rZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmFsaWRhdGUgdGhlIGlucHV0IG9uIFNlcnZlciBvciBUb2tlbnMgaXMgb25seSBpbnRlZ2VyIG9ubHlcclxuICAgICAqIFRoaXMgd2lsbCBiZSBjb252ZXJ0ZWQgaW4gYSBtb3JlIGNvbXBsZXggZGlyZWN0aXZlIGxhdGVyXHJcbiAgICAgKiBUT0RPOiBDb252ZXJ0IGludG8gYSBkaXJlY3RpdmVcclxuICAgICAqL1xyXG4gICAgdmFsaWRhdGVJbnRlZ2VyT25seShlKXtcclxuICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICB2YXIgbmV3VmFsPSBwYXJzZUludCh0aGlzLmxpY2Vuc2VNb2RlbC5tZXRob2QubWF4KTtcclxuICAgICAgICAgICAgaWYoIWlzTmFOKG5ld1ZhbCkpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLm1ldGhvZC5tYXggPSBuZXdWYWw7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5tZXRob2QubWF4ID0gMDtcclxuICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgaWYoZSAmJiBlLmN1cnJlbnRUYXJnZXQgJiYgZS5jdXJyZW50VGFyZ2V0LnZhbHVlKSB7XHJcbiAgICAgICAgICAgICAgICBlLmN1cnJlbnRUYXJnZXQudmFsdWUgPSB0aGlzLmxpY2Vuc2VNb2RlbC5tZXRob2QubWF4O1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSBjYXRjaChlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuJGxvZy53YXJuKCdJbnZhbGlkIE51bWJlciBFeHBjZXB0aW9uJywgdGhpcy5saWNlbnNlTW9kZWwubWV0aG9kLm1heCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZSBjdXJyZW50IGNoYW5nZXNcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5zYXZlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdMaWNlbnNlIFNhdmVkJyk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENoYW5nZSB0aGUgc3RhdHVzIHRvIEVkaXRcclxuICAgICAqL1xyXG4gICAgbW9kaWZ5TGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gdHJ1ZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50TGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgaWYoIXRoaXMubGljZW5zZU1vZGVsLmVudmlyb25tZW50SWQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudElkID0gZGF0YVswXS5pZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0U3RhdHVzRGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnNlbGVjdFN0YXR1c0xpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiBbXHJcbiAgICAgICAgICAgICAgICB7aWQ6IDEsIG5hbWU6ICdBY3RpdmUnfSxcclxuICAgICAgICAgICAgICAgIHtpZDogMiwgbmFtZTogJ0V4cGlyZWQnfSxcclxuICAgICAgICAgICAgICAgIHtpZDogMywgbmFtZTogJ1Rlcm1pbmF0ZWQnfSxcclxuICAgICAgICAgICAgICAgIHtpZDogNCwgbmFtZTogJ1BlbmRpbmcnfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEEgbmV3IFByb2plY3QgaGFzIGJlZW4gc2VsZWN0ZWQsIHRoYXQgbWVhbnMgd2UgbmVlZCB0byByZWxvYWQgdGhlIG5leHQgcHJvamVjdCBzZWN0aW9uXHJcbiAgICAgKiBAcGFyYW0gaXRlbVxyXG4gICAgICovXHJcbiAgICBvbkNoYW5nZVByb2plY3QoaXRlbSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09uIGNoYW5nZSBQcm9qZWN0JywgaXRlbSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25DaGFuZ2VJbml0RGF0ZSgpIHtcclxuICAgICAgICB2YXIgc3RhcnREYXRlID0gdGhpcy5pbml0RGF0ZS52YWx1ZSgpLFxyXG4gICAgICAgICAgICBlbmREYXRlID0gdGhpcy5lbmREYXRlLnZhbHVlKCk7XHJcblxyXG4gICAgICAgIGlmIChzdGFydERhdGUpIHtcclxuICAgICAgICAgICAgc3RhcnREYXRlID0gbmV3IERhdGUoc3RhcnREYXRlKTtcclxuICAgICAgICAgICAgc3RhcnREYXRlLnNldERhdGUoc3RhcnREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4oc3RhcnREYXRlKTtcclxuXHJcbiAgICAgICAgICAgIGlmKGVuZERhdGUpIHtcclxuICAgICAgICAgICAgICAgIGlmKHRoaXMuaW5pdERhdGUudmFsdWUoKSA+IHRoaXMuZW5kRGF0ZS52YWx1ZSgpKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKGVuZERhdGUpO1xyXG4gICAgICAgICAgICAgICAgICAgIGVuZERhdGUuc2V0RGF0ZShzdGFydERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbmREYXRlID0gZW5kRGF0ZTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBvbkNoYW5nZUVuZERhdGUoKXtcclxuICAgICAgICB2YXIgZW5kRGF0ZSA9IHRoaXMuZW5kRGF0ZS52YWx1ZSgpLFxyXG4gICAgICAgICAgICBzdGFydERhdGUgPSB0aGlzLmluaXREYXRlLnZhbHVlKCk7XHJcblxyXG4gICAgICAgIGlmIChlbmREYXRlKSB7XHJcbiAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZShlbmREYXRlKTtcclxuICAgICAgICAgICAgZW5kRGF0ZS5zZXREYXRlKGVuZERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICB9IGVsc2UgaWYgKHN0YXJ0RGF0ZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKG5ldyBEYXRlKHN0YXJ0RGF0ZSkpO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZSgpO1xyXG4gICAgICAgICAgICB0aGlzLmluaXREYXRlLm1heChlbmREYXRlKTtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihlbmREYXRlKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgaWYodGhpcy5lZGl0TW9kZSkge1xyXG4gICAgICAgICAgICB0aGlzLnJlc2V0Rm9ybSgoKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25SZXNldEZvcm0oKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIGlmKHRoaXMucmVsb2FkUmVxdWlyZWQpe1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2Uoe30pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEZXBlZGluZyB0aGUgbnVtYmVyIG9mIGZpZWxkcyBhbmQgdHlwZSBvZiBmaWVsZCwgdGhlIHJlc2V0IGNhbid0IGJlIG9uIHRoZSBGb3JtVmFsaWRvciwgYXQgbGVhc3Qgbm90IG5vd1xyXG4gICAgICovXHJcbiAgICBvblJlc2V0Rm9ybSgpIHtcclxuICAgICAgICAvLyBSZXNldCBQcm9qZWN0IFNlbGVjdG9yXHJcbiAgICAgICAgdGhpcy5yZXNldERyb3BEb3duKHRoaXMuc2VsZWN0UHJvamVjdCwgdGhpcy5saWNlbnNlTW9kZWwucHJvamVjdElkKTtcclxuICAgICAgICB0aGlzLnJlc2V0RHJvcERvd24odGhpcy5zZWxlY3RTdGF0dXMsIHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkKTtcclxuICAgICAgICB0aGlzLnJlc2V0RHJvcERvd24odGhpcy5zZWxlY3RFbnZpcm9ubWVudCwgdGhpcy5saWNlbnNlTW9kZWwuZW52aXJvbm1lbnQuaWQpO1xyXG4gICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIC8vdGhpcy5nZXRMaWNlbnNlTGlzdCgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTWFuYWdlckxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gMDtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0Lm9uUmVxdWVzdEltcG9ydExpY2Vuc2UoKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBJbXBvcnQgTGljZW5zZSBSZXF1ZXN0PC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3QucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5vbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyh0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnb3duZXIubmFtZScsIHRpdGxlOiAnT3duZXInfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NsaWVudC5uYW1lJywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QubmFtZScsIHRpdGxlOiAnUHJvamVjdCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cy5uYW1lJywgdGl0bGU6ICdTdGF0dXMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5tYXgnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2YXRpb25EYXRlJywgdGl0bGU6ICdJbmNlcHRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uRGF0ZScsIHRpdGxlOiAnRXhwaXJhdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Vudmlyb25tZW50Lm5hbWUnLCB0aXRsZTogJ0Vudmlyb25tZW50J31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHNvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICBmaWVsZDogJ3Byb2plY3QubmFtZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGNoYW5nZTogIChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgLy8gV2UgYXJlIGNvbWluZyBmcm9tIGEgbmV3IGltcG9ydGVkIHJlcXVlc3QgbGljZW5zZVxyXG4gICAgICAgICAgICAgICAgICAgIGlmKHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCAhPT0gMCAmJiB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdmFyIG5ld0xpY2Vuc2VDcmVhdGVkID0gdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhLmZpbmQoKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBsaWNlbnNlLmlkID09PSB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gMDtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGlmKG5ld0xpY2Vuc2VDcmVhdGVkKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKG5ld0xpY2Vuc2VDcmVhdGVkKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGUgdXNlciBJbXBvcnQgYSBuZXcgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBvblJlcXVlc3RJbXBvcnRMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL3JlcXVlc3RJbXBvcnQvUmVxdWVzdEltcG9ydC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1JlcXVlc3RJbXBvcnQgYXMgcmVxdWVzdEltcG9ydCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IGxpY2Vuc2VJbXBvcnRlZC5pZDsgLy8gdGFrZSB0aGlzIHBhcmFtIGZyb20gdGhlIGxhc3QgaW1wb3J0ZWQgbGljZW5zZSwgb2YgY291cnNlXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKGxpY2Vuc2UpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPcGVuIERldGFpbHMgZm9yOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlTWFuYWdlckRldGFpbCBhcyBsaWNlbnNlTWFuYWdlckRldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgcmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SW1wb3J0IGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBoYXNoOiAnJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIG9uSW1wb3J0TGljZW5zZSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5pbXBvcnRMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UobGljZW5zZUltcG9ydGVkLmRhdGEpO1xyXG4gICAgICAgICAgICB9LCAobGljZW5zZUltcG9ydGVkKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShsaWNlbnNlSW1wb3J0ZWQuZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlciwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG4gICAgICAgIHRoaXMucm9vdFNjb3BlID0gJHJvb3RTY29wZTtcclxuICAgICAgICB0aGlzLnN0YXR1c1N1Y2Nlc3MgPSAnc3VjY2Vzcyc7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ2xpY2Vuc2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldFByb2plY3REYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2Uob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRLZXlDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0S2V5Q29kZShsaWNlbnNlSWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgdGhlIExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcblxyXG4gICAgICAgIHZhciBsaWNlbnNlTW9kaWZpZWQgPSB7XHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiB7IGlkOiBwYXJzZUludChsaWNlbnNlLmVudmlyb25tZW50LmlkKSB9LFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiBwYXJzZUludChsaWNlbnNlLm1ldGhvZC5pZClcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGlvbkRhdGU6IGxpY2Vuc2UuaW5pdERhdGUsXHJcbiAgICAgICAgICAgIGV4cGlyYXRpb25EYXRlOiBsaWNlbnNlLmVuZERhdGUsXHJcbiAgICAgICAgICAgIHN0YXR1czogeyBpZDogbGljZW5zZS5zdGF0dXNJZCB9LFxyXG4gICAgICAgICAgICBwcm9qZWN0OiB7IGlkOiAobGljZW5zZS5wcm9qZWN0SWQgIT09ICdhbGwnKT8gcGFyc2VJbnQobGljZW5zZS5wcm9qZWN0SWQpIDogbGljZW5zZS5wcm9qZWN0SWQgIH0sIC8vIFdlIHBhc3MgJ2FsbCcgd2hlbiBpcyBtdWx0aXByb2plY3RcclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogbGljZW5zZS5iYW5uZXJNZXNzYWdlXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgaWYobGljZW5zZS5tZXRob2QgIT09IDMpIHtcclxuICAgICAgICAgICAgbGljZW5zZU1vZGlmaWVkLm1ldGhvZC5tYXggPSBwYXJzZUludChsaWNlbnNlLm1ldGhvZC5tYXgpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuc2F2ZUxpY2Vuc2UobGljZW5zZS5pZCwgbGljZW5zZU1vZGlmaWVkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG4gICAgLyoqXHJcbiAgICAgKiBEb2VzIHRoZSBhY3RpdmF0aW9uIG9mIHRoZSBjdXJyZW50IGxpY2Vuc2UgaWYgdGhpcyBpcyBub3QgYWN0aXZlXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGFjdGl2YXRlTGljZW5zZShsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmFjdGl2YXRlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnVGhlIGxpY2Vuc2Ugd2FzIGFjdGl2YXRlZCBhbmQgdGhlIGxpY2Vuc2Ugd2FzIGVtYWlsZWQuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBNYWtlIHRoZSByZXF1ZXN0IHRvIEltcG9ydCB0aGUgbGljZW5zZSwgaWYgZmFpbHMsIHRocm93cyBhbiBleGNlcHRpb24gdmlzaWJsZSBmb3IgdGhlIHVzZXIgdG8gdGFrZSBhY3Rpb25cclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgaW1wb3J0TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB2YXIgaGFzaCA9IHtcclxuICAgICAgICAgICAgZGF0YTogbGljZW5zZS5oYXNoXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkucmVxdWVzdEltcG9ydChoYXNoLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICBpZihkYXRhLnN0YXR1cyA9PT0gdGhpcy5zdGF0dXNTdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5IEltcG9ydGVkJ30pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkLiBSZXZpZXcgdGhlIHByb3ZpZGVkIExpY2Vuc2UgS2V5IGlzIGNvcnJlY3QuJ30pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3IoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5yZXZva2VMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBjYWxsYmFjayl7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBOb3RpY2VMaXN0IGZyb20gJy4vbGlzdC9Ob3RpY2VMaXN0LmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9Ob3RpY2VNYW5hZ2VyU2VydmljZS5qcyc7XHJcbmltcG9ydCBFZGl0Tm90aWNlIGZyb20gJy4vZWRpdC9FZGl0Tm90aWNlLmpzJztcclxuXHJcblxyXG52YXIgTm90aWNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5Ob3RpY2VNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdub3RpY2VNYW5hZ2VyJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdub3RpY2VMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTm90aWNlIEFkbWluaXN0cmF0aW9uJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FETUlOJywgJ05PVElDRScsICdMSVNUJ119fSxcclxuICAgICAgICAgICAgdXJsOiAnL25vdGljZS9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2xpc3QvTm90aWNlTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTm90aWNlTGlzdCBhcyBub3RpY2VMaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuc2VydmljZSgnTm90aWNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgTm90aWNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTm90aWNlTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTm90aWNlTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0VkaXROb3RpY2UnLCBbJyRsb2cnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIEVkaXROb3RpY2VdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IE5vdGljZU1hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEVkaXROb3RpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIG5vdGljZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlID0gbm90aWNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uID0gcGFyYW1zLmFjdGlvbjtcclxuICAgICAgICB0aGlzLmFjdGlvblR5cGUgPSBwYXJhbXMuYWN0aW9uVHlwZTtcclxuXHJcbiAgICAgICAgdGhpcy5rZW5kb0VkaXRvclRvb2xzID0gW1xyXG4gICAgICAgICAgICAnZm9ybWF0dGluZycsICdjbGVhbkZvcm1hdHRpbmcnLFxyXG4gICAgICAgICAgICAnZm9udE5hbWUnLCAnZm9udFNpemUnLFxyXG4gICAgICAgICAgICAnanVzdGlmeUxlZnQnLCAnanVzdGlmeUNlbnRlcicsICdqdXN0aWZ5UmlnaHQnLCAnanVzdGlmeUZ1bGwnLFxyXG4gICAgICAgICAgICAnYm9sZCcsXHJcbiAgICAgICAgICAgICdpdGFsaWMnLFxyXG4gICAgICAgICAgICAndmlld0h0bWwnXHJcbiAgICAgICAgXTtcclxuXHJcbiAgICAgICAgLy8gQ1NTIGhhcyBub3QgY2FuY2VsaW5nIGF0dHJpYnV0ZXMsIHNvIGluc3RlYWQgb2YgcmVtb3ZpbmcgZXZlcnkgcG9zc2libGUgSFRNTCwgd2UgbWFrZSBlZGl0b3IgaGFzIHNhbWUgY3NzXHJcbiAgICAgICAgdGhpcy5rZW5kb1N0eWxlc2hlZXRzID0gW1xyXG4gICAgICAgICAgICAnLi4vc3RhdGljL2Rpc3QvanMvdmVuZG9ycy9ib290c3RyYXAvZGlzdC9jc3MvYm9vdHN0cmFwLm1pbi5jc3MnLCAvLyBPdXJ0IGN1cnJlbnQgQm9vdHN0cmFwIGNzc1xyXG4gICAgICAgICAgICAnLi4vc3RhdGljL2Rpc3QvY3NzL1REU1RNTGF5b3V0Lm1pbi5jc3MnIC8vIE9yaWdpbmFsIFRlbXBsYXRlIENTU1xyXG5cclxuICAgICAgICBdO1xyXG5cclxuICAgICAgICB0aGlzLmdldFR5cGVEYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZWwgPSB7XHJcbiAgICAgICAgICAgIHRpdGxlOiAnJyxcclxuICAgICAgICAgICAgdHlwZUlkOiAwLFxyXG4gICAgICAgICAgICBhY3RpdmU6IGZhbHNlLFxyXG4gICAgICAgICAgICBodG1sVGV4dDogJycsXHJcbiAgICAgICAgICAgIHJhd1RleHQ6ICcnXHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBPbiBFZGl0aW9uIE1vZGUgd2UgY2MgdGhlIG1vZGVsIGFuZCBvbmx5IHRoZSBwYXJhbXMgd2UgbmVlZFxyXG4gICAgICAgIGlmKHBhcmFtcy5ub3RpY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuaWQgPSBwYXJhbXMubm90aWNlLmlkO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC50aXRsZSA9IHBhcmFtcy5ub3RpY2UudGl0bGU7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLnR5cGVJZCA9IHBhcmFtcy5ub3RpY2UudHlwZS5pZDtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuYWN0aXZlID0gcGFyYW1zLm5vdGljZS5hY3RpdmU7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLmh0bWxUZXh0ID0gcGFyYW1zLm5vdGljZS5odG1sVGV4dDtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFR5cGVEYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMudHlwZURhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDEsIG5hbWU6ICdQcmVsb2dpbid9LFxyXG4gICAgICAgICAgICB7dHlwZUlkOiAyLCBuYW1lOiAnUG9zdGxvZ2luJ31cclxuICAgICAgICAgICAgLy97dHlwZUlkOiAzLCBuYW1lOiAnR2VuZXJhbCd9IERpc2FibGVkIHVudGlsIFBoYXNlIElJXHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBDcmVhdGUvRWRpdCBhIG5vdGljZVxyXG4gICAgICovXHJcbiAgICBzYXZlTm90aWNlKCkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8odGhpcy5hY3Rpb24gKyAnIE5vdGljZSBSZXF1ZXN0ZWQ6ICcsIHRoaXMuZWRpdE1vZGVsKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbC5yYXdUZXh0ID0gJCgnI2tlbmRvLWVkaXRvci1jcmVhdGUtZWRpdCcpLnRleHQoKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbC50eXBlSWQgPSBwYXJzZUludCh0aGlzLmVkaXRNb2RlbC50eXBlSWQpO1xyXG4gICAgICAgIGlmKHRoaXMuYWN0aW9uID09PSB0aGlzLmFjdGlvblR5cGUuTkVXKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuY3JlYXRlTm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2UgaWYodGhpcy5hY3Rpb24gPT09IHRoaXMuYWN0aW9uVHlwZS5FRElUKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZWRpdE5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGRlbGV0ZU5vdGljZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byBkZWxldGUgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZGVsZXRlTm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBub3RpY2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHtcclxuICAgICAgICAgICAgTkVXOiAnTmV3JyxcclxuICAgICAgICAgICAgRURJVDogJ0VkaXQnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZUxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLm5vdGljZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuTkVXKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBDcmVhdGUgTmV3IE5vdGljZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibm90aWNlTGlzdC5yZWxvYWROb3RpY2VMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2h0bWxUZXh0JywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuRURJVCwgdGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RpdGxlJywgdGl0bGU6ICdUaXRsZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5pZCcsIGhpZGRlbjogdHJ1ZX0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2ZScsIHRpdGxlOiAnQWN0aXZlJywgdGVtcGxhdGU6ICcjaWYoYWN0aXZlKSB7IyBZZXMgI30gZWxzZSB7IyBObyAjfSMnIH1cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICd0aXRsZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBOb3RpY2VcclxuICAgICAqL1xyXG4gICAgb25FZGl0Q3JlYXRlTm90aWNlKGFjdGlvbiwgbm90aWNlKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9lZGl0L0VkaXROb3RpY2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdFZGl0Tm90aWNlIGFzIGVkaXROb3RpY2UnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSBub3RpY2UgJiYgbm90aWNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGFjdGlvbjogYWN0aW9uLCBub3RpY2U6IGRhdGFJdGVtLCBhY3Rpb25UeXBlOiB0aGlzLmFjdGlvblR5cGV9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKG5vdGljZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgTm90aWNlOiAnLCBub3RpY2UpO1xyXG4gICAgICAgICAgICAvLyBBZnRlciBhIG5ldyB2YWx1ZSBpcyBhZGRlZCwgbGV0cyB0byByZWZyZXNoIHRoZSBHcmlkXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTm90aWNlTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbyhhY3Rpb24gKyAnIFJlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVsb2FkTm90aWNlTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLlRZUEUgPSB7XHJcbiAgICAgICAgICAgICcxJzogJ1ByZWxvZ2luJyxcclxuICAgICAgICAgICAgJzInOiAnUG9zdGxvZ2luJyxcclxuICAgICAgICAgICAgJzMnOiAnR2VuZXJhbCdcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTm90aWNlTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0Tm90aWNlTGlzdChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0Tm90aWNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB2YXIgbm90aWNlTGlzdCA9IFtdO1xyXG4gICAgICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICAgICAgLy8gVmVyaWZ5IHRoZSBMaXN0IHJldHVybnMgd2hhdCB3ZSBleHBlY3QgYW5kIHdlIGNvbnZlcnQgaXQgdG8gYW4gQXJyYXkgdmFsdWVcclxuICAgICAgICAgICAgICAgIGlmKGRhdGEgJiYgZGF0YS5ub3RpY2VzKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgbm90aWNlTGlzdCA9IGRhdGEubm90aWNlcztcclxuICAgICAgICAgICAgICAgICAgICBpZiAobm90aWNlTGlzdCAmJiBub3RpY2VMaXN0Lmxlbmd0aCA+IDApIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBub3RpY2VMaXN0Lmxlbmd0aDsgaSA9IGkgKyAxKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBub3RpY2VMaXN0W2ldLnR5cGUgPSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IG5vdGljZUxpc3RbaV0udHlwZUlkLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6IHRoaXMuVFlQRVtub3RpY2VMaXN0W2ldLnR5cGVJZF1cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBkZWxldGUgbm90aWNlTGlzdFtpXS50eXBlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5sb2cuZXJyb3IoJ0Vycm9yIHBhcnNpbmcgdGhlIE5vdGljZSBMaXN0JywgZSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKG5vdGljZUxpc3QpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IE5vdGljZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGNyZWF0ZU5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE5vdGljZSBzaG91bGQgaGF2ZSB0aGUgSUQgaW4gb3JkZXIgdG8gZWRpdCB0aGUgTm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZWRpdE5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmVkaXROb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBOb3RpY2Ugc2hvdWxkIGhhdmUgdGhlIElEIGluIG9yZGVyIHRvIGRlbGV0ZSB0aGUgbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZGVsZXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmRlbGV0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5pbXBvcnQgVGFza01hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9UYXNrTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJDb250cm9sbGVyIGZyb20gJy4vbGlzdC9UYXNrTWFuYWdlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJFZGl0IGZyb20gJy4vZWRpdC9UYXNrTWFuYWdlckVkaXQuanMnO1xyXG5cclxuLy8gRGlyZWN0aXZlc1xyXG4vL2ltcG9ydCBTVkdMb2FkZXJDb250cm9sbGVyIGZyb20gJy4uLy4uL2RpcmVjdGl2ZXMvc3ZnL1NWR0xvYWRlckNvbnRyb2xsZXIuanMnXHJcblxyXG52YXIgVGFza01hbmFnZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uVGFza01hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlcikge1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlckNvbnRhaW5lci5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCd0YXNrTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ015IFRhc2sgTWFuYWdlcicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydUYXNrIE1hbmFnZXInXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvdGFzay9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICdhcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9saXN0L1Rhc2tNYW5hZ2VyQ29udGFpbmVyLmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdUYXNrTWFuYWdlckNvbnRyb2xsZXIgYXMgdGFza01hbmFnZXInXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuVGFza01hbmFnZXJNb2R1bGUuc2VydmljZSgndGFza01hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFRhc2tNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJDb250cm9sbGVyJywgWyckbG9nJywgJ3Rhc2tNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBUYXNrTWFuYWdlckNvbnRyb2xsZXJdKTtcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJFZGl0JywgWyckbG9nJywgVGFza01hbmFnZXJFZGl0XSk7XHJcblxyXG4vLyBEaXJlY3RpdmVzXHJcbi8vVGFza01hbmFnZXJNb2R1bGUuZGlyZWN0aXZlKCdzdmdMb2FkZXInLCBTVkdMb2FkZXJDb250cm9sbGVyKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IFRhc2tNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMy8xMS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyRWRpdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgdGFza01hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuXHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMC8yMDE1LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCB0YXNrTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMubW9kdWxlID0gJ1Rhc2tNYW5hZ2VyJztcclxuICAgICAgICB0aGlzLnRhc2tNYW5hZ2VyU2VydmljZSA9IHRhc2tNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnRhc2tHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMuZXZlbnREYXRhU291cmNlID0gW107XHJcblxyXG4gICAgICAgIC8vIEluaXQgQ2xhc3NcclxuICAgICAgICB0aGlzLmdldEV2ZW50RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdUYXNrTWFuYWdlciBDb250cm9sbGVyIEluc3RhbmNlZCcpO1xyXG5cclxuICAgIH1cclxuXHJcbiAgICBvcGVuTW9kYWxEZW1vKCkge1xyXG5cclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICdhcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9lZGl0L1Rhc2tNYW5hZ2VyRWRpdC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyRWRpdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIGl0ZW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIFsnMScsJ2EyJywnZ2cnXTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChzZWxlY3RlZEl0ZW0pID0+IHtcclxuICAgICAgICAgICAgdGhpcy5kZWJ1ZyhzZWxlY3RlZEl0ZW0pO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTW9kYWwgZGlzbWlzc2VkIGF0OiAnICsgbmV3IERhdGUoKSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnRhc2tHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZ3JvdXBhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbe2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Rhc2snLCB0aXRsZTogJ1Rhc2snfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Rlc2NyaXB0aW9uJywgdGl0bGU6ICdEZXNjcmlwdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzZXROYW1lJywgdGl0bGU6ICdBc3NldCBOYW1lJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldFR5cGUnLCB0aXRsZTogJ0Fzc2V0IFR5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3VwZGF0ZWQnLCB0aXRsZTogJ1VwZGF0ZWQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2R1ZScsIHRpdGxlOiAnRHVlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMnLCB0aXRsZTogJ1N0YXR1cyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzaWduZWRUbycsIHRpdGxlOiAnQXNzaWduZWQgVG8nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RlYW0nLCB0aXRsZTogJ1RlYW0nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NhdGVnb3J5JywgdGl0bGU6ICdDYXRlZ29yeSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3VjJywgdGl0bGU6ICdTdWMuJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzY29yZScsIHRpdGxlOiAnU2NvcmUnfV0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlLnRlc3RTZXJ2aWNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RXZlbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuZXZlbnREYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMSwgZXZlbnROYW1lOiAnQWxsJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiAyLCBldmVudE5hbWU6ICdCdWlsZG91dCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMywgZXZlbnROYW1lOiAnRFItRVAnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDQsIGV2ZW50TmFtZTogJ00xLVBoeXNpY2FsJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIG9uRXJyb3JIYXBwZW5zKCkge1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlLmZhaWxDYWxsKGZ1bmN0aW9uICgpIHtcclxuXHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDIyLzA3LzE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgUmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSBSZXN0U2VydmljZUhhbmRsZXI7XHJcblxyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdUYXNrTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZmFpbENhbGwoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlJlc291cmNlU2VydmljZUhhbmRsZXIoKS5nZXRTVkcoKTtcclxuICAgIH1cclxuXHJcbiAgICB0ZXN0U2VydmljZShjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuVGFza1NlcnZpY2VIYW5kbGVyKCkuZ2V0RmVlZHMoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzMvMjAxNi5cclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnXHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBGb3JtVmFsaWRhdG9yIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuXHJcbiAgICAgICAgLy8gSlMgZG9lcyBhIGFyZ3VtZW50IHBhc3MgYnkgcmVmZXJlbmNlXHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gbnVsbDtcclxuICAgICAgICAvLyBBIGNvcHkgd2l0aG91dCByZWZlcmVuY2UgZnJvbSB0aGUgb3JpZ2luYWwgb2JqZWN0XHJcbiAgICAgICAgdGhpcy5vcmlnaW5hbERhdGEgPSBudWxsO1xyXG4gICAgICAgIC8vIEEgQ0MgYXMgSlNPTiBmb3IgY29tcGFyaXNvbiBQdXJwb3NlXHJcbiAgICAgICAgdGhpcy5vYmplY3RBc0pTT04gPSBudWxsO1xyXG5cclxuXHJcbiAgICAgICAgLy8gT25seSBmb3IgTW9kYWwgV2luZG93c1xyXG4gICAgICAgIHRoaXMucmVsb2FkUmVxdWlyZWQgPSBmYWxzZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cclxuICAgICAgICBpZiAoJHNjb3BlLiRvbikge1xyXG4gICAgICAgICAgICAkc2NvcGUuJG9uKCdtb2RhbC5jbG9zaW5nJywgKGV2ZW50LCByZWFzb24sIGNsb3NlZCk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2xvc2VEaWFsb2coZXZlbnQsIHJlYXNvbiwgY2xvc2VkKVxyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBTYXZlcyB0aGUgRm9ybSBpbiAzIGluc3RhbmNlcywgb25lIHRvIGtlZXAgdHJhY2sgb2YgdGhlIG9yaWdpbmFsIGRhdGEsIG90aGVyIGlzIHRoZSBjdXJyZW50IG9iamVjdCBhbmRcclxuICAgICAqIGEgSlNPTiBmb3JtYXQgZm9yIGNvbXBhcmlzb24gcHVycG9zZVxyXG4gICAgICogQHBhcmFtIG5ld09iamVjdEluc3RhbmNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVGb3JtKG5ld09iamVjdEluc3RhbmNlKSB7XHJcbiAgICAgICAgdGhpcy5jdXJyZW50T2JqZWN0ID0gbmV3T2JqZWN0SW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5vcmlnaW5hbERhdGEgPSBhbmd1bGFyLmNvcHkobmV3T2JqZWN0SW5zdGFuY2UsIHRoaXMub3JpZ2luYWxEYXRhKTtcclxuICAgICAgICB0aGlzLm9iamVjdEFzSlNPTiA9IGFuZ3VsYXIudG9Kc29uKG5ld09iamVjdEluc3RhbmNlKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEdldCB0aGUgQ3VycmVudCBPYmplY3Qgb24gaGlzIHJlZmVyZW5jZSBGb3JtYXRcclxuICAgICAqIEByZXR1cm5zIHtudWxsfCp9XHJcbiAgICAgKi9cclxuICAgIGdldEZvcm0oKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuY3VycmVudE9iamVjdDtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEdldCB0aGUgT2JqZWN0IGFzIEpTT04gZnJvbSB0aGUgT3JpZ2luYWwgRGF0YVxyXG4gICAgICogQHJldHVybnMge251bGx8c3RyaW5nfHVuZGVmaW5lZHxzdHJpbmd8Kn1cclxuICAgICAqL1xyXG4gICAgZ2V0Rm9ybUFzSlNPTigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5vYmplY3RBc0pTT047XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKlxyXG4gICAgICogQHBhcmFtIG9iamV0VG9SZXNldCBvYmplY3QgdG8gcmVzZXRcclxuICAgICAqIEBwYXJhbSBvblJlc2V0Rm9ybSBjYWxsYmFja1xyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIHJlc2V0Rm9ybShvblJlc2V0Rm9ybSkge1xyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IGFuZ3VsYXIuY29weSh0aGlzLm9yaWdpbmFsRGF0YSwgdGhpcy5jdXJyZW50T2JqZWN0KTtcclxuICAgICAgICB0aGlzLnNhZmVBcHBseSgpO1xyXG5cclxuICAgICAgICBpZihvblJlc2V0Rm9ybSkge1xyXG4gICAgICAgICAgICByZXR1cm4gb25SZXNldEZvcm0oKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWYWxpZGF0ZXMgaWYgdGhlIGN1cnJlbnQgb2JqZWN0IGRpZmZlcnMgZnJvbSB3aGVyZSBpdCB3YXMgb3JpZ2luYWxseSBzYXZlZFxyXG4gICAgICogQHJldHVybnMge2Jvb2xlYW59XHJcbiAgICAgKi9cclxuICAgIGlzRGlydHkoKSB7XHJcbiAgICAgICAgdmFyIG5ld09iamVjdEluc3RhbmNlID0gYW5ndWxhci50b0pzb24odGhpcy5jdXJyZW50T2JqZWN0KTtcclxuICAgICAgICByZXR1cm4gbmV3T2JqZWN0SW5zdGFuY2UgIT09IHRoaXMuZ2V0Rm9ybUFzSlNPTigpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhpcyBmdW5jdGlvbiBpcyBvbmx5IGF2YWlsYWJsZSB3aGVuIHRoZSBGb3JtIGlzIGJlaW5nIGNhbGxlZCBmcm9tIGEgRGlhbG9nIFBvcFVwXHJcbiAgICAgKi9cclxuICAgIG9uQ2xvc2VEaWFsb2coZXZlbnQsIHJlYXNvbiwgY2xvc2VkKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnbW9kYWwuY2xvc2luZzogJyArIChjbG9zZWQgPyAnY2xvc2UnIDogJ2Rpc21pc3MnKSArICcoJyArIHJlYXNvbiArICcpJyk7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNEaXJ0eSgpICYmIHJlYXNvbiAhPT0gJ2NhbmNlbC1jb25maXJtYXRpb24nICYmIHR5cGVvZiByZWFzb24gIT09ICdvYmplY3QnKSB7XHJcbiAgICAgICAgICAgIGV2ZW50LnByZXZlbnREZWZhdWx0KCk7XHJcbiAgICAgICAgICAgIHRoaXMuY29uZmlybUNsb3NlRm9ybSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEEgQ29uZmlybWF0aW9uIERpYWxvZyB3aGVuIHRoZSBpbmZvcm1hdGlvbiBjYW4gYmUgbG9zdFxyXG4gICAgICogQHBhcmFtIGV2ZW50XHJcbiAgICAgKi9cclxuICAgIGNvbmZpcm1DbG9zZUZvcm0oZXZlbnQpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgbWVzc2FnZTogJ0NoYW5nZXMgeW91IG1hZGUgbWF5IG5vdCBiZSBzYXZlZC4gRG8geW91IHdhbnQgdG8gY29udGludWU/J1xyXG4gICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwtY29uZmlybWF0aW9uJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBVdGlsIHRvIGNhbGwgc2FmZSBpZiByZXF1aXJlZFxyXG4gICAgICogQHBhcmFtIGZuXHJcbiAgICAgKi9cclxuICAgIHNhZmVBcHBseShmbikge1xyXG4gICAgICAgIHZhciBwaGFzZSA9IHRoaXMuc2NvcGUuJHJvb3QuJCRwaGFzZTtcclxuICAgICAgICBpZihwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgICAgICBpZihmbiAmJiAodHlwZW9mKGZuKSA9PT0gJ2Z1bmN0aW9uJykpIHtcclxuICAgICAgICAgICAgICAgIGZuKCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICB0aGlzLnNjb3BlLiRhcHBseShmbik7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVXRpbCB0byBSZXNldCBhIERyb3Bkb3duIGxpc3Qgb24gS2VuZG9cclxuICAgICAqL1xyXG5cclxuICAgIHJlc2V0RHJvcERvd24oc2VsZWN0b3JJbnN0YW5jZSwgc2VsZWN0ZWRJZCwgZm9yY2UpIHtcclxuICAgICAgICBpZihzZWxlY3Rvckluc3RhbmNlICYmIHNlbGVjdG9ySW5zdGFuY2UuZGF0YUl0ZW1zKSB7XHJcbiAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2UuZGF0YUl0ZW1zKCkuZm9yRWFjaCgodmFsdWUsIGluZGV4KSA9PiB7XHJcbiAgICAgICAgICAgICAgICBpZihzZWxlY3RlZElkID09PSB2YWx1ZS5pZCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2Uuc2VsZWN0KGluZGV4KTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBpZihmb3JjZSkge1xyXG4gICAgICAgICAgICAgICAgc2VsZWN0b3JJbnN0YW5jZS50cmlnZ2VyKCdjaGFuZ2UnKTtcclxuICAgICAgICAgICAgICAgIHRoaXMuc2FmZUFwcGx5KCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIzLzIwMTUuXHJcbiAqIEltcGxlbWVudHMgUlggT2JzZXJ2YWJsZSB0byBkaXNwb3NlIGFuZCB0cmFjayBiZXR0ZXIgZWFjaCBjYWxsIHRvIHRoZSBzZXJ2ZXJcclxuICogVGhlIE9ic2VydmVyIHN1YnNjcmliZSBhIHByb21pc2UuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RIYW5kbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKHJ4KSB7XHJcbiAgICAgICAgdGhpcy5yeCA9IHJ4O1xyXG4gICAgICAgIHRoaXMucHJvbWlzZSA9IFtdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2FsbGVkIGZyb20gUmVzdFNlcnZpY2VIYW5kbGVyLnN1YnNjcmliZVJlcXVlc3RcclxuICAgICAqIGl0IHZlcmlmeSB0aGF0IHRoZSBjYWxsIGlzIGJlaW5nIGRvbmUgdG8gdGhlIHNlcnZlciBhbmQgcmV0dXJuIGEgcHJvbWlzZVxyXG4gICAgICogQHBhcmFtIHJlcXVlc3RcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICBzdWJzY3JpYmVSZXF1ZXN0KHJlcXVlc3QsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHZhciByeE9ic2VydmFibGUgPSB0aGlzLnJ4Lk9ic2VydmFibGUuZnJvbVByb21pc2UocmVxdWVzdCk7XHJcbiAgICAgICAgLy8gVmVyaWZ5IGlzIG5vdCBhIGR1cGxpY2F0ZSBjYWxsXHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgdGhpcy5jYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBTdWJzY3JpYmUgdGhlIHJlcXVlc3RcclxuICAgICAgICB2YXIgcmVzdWx0U3Vic2NyaWJlID0gdGhpcy5hZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgIGlmIChyZXN1bHRTdWJzY3JpYmUgJiYgcmVzdWx0U3Vic2NyaWJlLmlzU3RvcHBlZCkge1xyXG4gICAgICAgICAgICAvLyBBbiBlcnJvciBoYXBwZW5zLCB0cmFja2VkIGJ5IEh0dHBJbnRlcmNlcHRvckludGVyZmFjZVxyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGFkZFN1YnNjcmliZShyeE9ic2VydmFibGUsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdID0gcnhPYnNlcnZhYmxlLnN1YnNjcmliZShcclxuICAgICAgICAgICAgKHJlc3BvbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5vblN1YnNjcmliZWRTdWNjZXNzKHJlc3BvbnNlLCByeE9ic2VydmFibGUsIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIChlcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE5PLU9QIFN1YnNjcmliZSBjb21wbGV0ZWRcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgIH1cclxuXHJcbiAgICBjYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICAgICAgcnhPYnNlcnZhYmxlLmRpc3Bvc2UoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIHJldHVybiAocnhPYnNlcnZhYmxlICYmIHJ4T2JzZXJ2YWJsZS5fcCAmJiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKG9uU3VjY2Vzcyl7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MocmVzcG9uc2UuZGF0YSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhyb3dzIGltbWVkaWF0ZWx5IGVycm9yIHdoZW4gdGhlIHBldGl0aW9uIGNhbGwgaXMgd3JvbmdcclxuICAgICAqIG9yIHdpdGggYSBkZWxheSBpZiB0aGUgY2FsbCBpcyB2YWxpZFxyXG4gICAgICogQHBhcmFtIGVycm9yXHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgb25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcikge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICAgICAgaWYob25FcnJvcil7XHJcbiAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHt9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IFJlc3RTZXJ2aWNlSGFuZGxlciBmcm9tICcuL1Jlc3RTZXJ2aWNlSGFuZGxlci5qcyc7XHJcblxyXG52YXIgUmVzdEFQSU1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5SZXN0QVBJTW9kdWxlJyxbXSk7XHJcblxyXG5SZXN0QVBJTW9kdWxlLnNlcnZpY2UoJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFsnJGxvZycsICckaHR0cCcsICckcmVzb3VyY2UnLCAncngnLCBSZXN0U2VydmljZUhhbmRsZXJdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IFJlc3RBUElNb2R1bGU7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMDgvMTUuXHJcbiAqIEl0IGFic3RyYWN0IGVhY2ggb25lIG9mIHRoZSBleGlzdGluZyBjYWxsIHRvIHRoZSBBUEksIGl0IHNob3VsZCBvbmx5IGNvbnRhaW5zIHRoZSBjYWxsIGZ1bmN0aW9ucyBhbmQgcmVmZXJlbmNlXHJcbiAqIHRvIHRoZSBjYWxsYmFjaywgbm8gbG9naWMgYXQgYWxsLlxyXG4gKlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgUmVxdWVzdEhhbmRsZXIgZnJvbSAnLi9SZXF1ZXN0SGFuZGxlci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXN0U2VydmljZUhhbmRsZXIge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJGh0dHAsICRyZXNvdXJjZSwgcngpIHtcclxuICAgICAgICB0aGlzLnJ4ID0gcng7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuaHR0cCA9ICRodHRwO1xyXG4gICAgICAgIHRoaXMucmVzb3VyY2UgPSAkcmVzb3VyY2U7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVycygpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdSZXN0U2VydmljZSBMb2FkZWQnKTtcclxuICAgICAgICB0aGlzLnJlcSA9IHtcclxuICAgICAgICAgICAgbWV0aG9kOiAnJyxcclxuICAgICAgICAgICAgdXJsOiAnJyxcclxuICAgICAgICAgICAgaGVhZGVyczoge1xyXG4gICAgICAgICAgICAgICAgJ0NvbnRlbnQtVHlwZSc6ICdhcHBsaWNhdGlvbi9qc29uO2NoYXJzZXQ9VVRGLTgnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGE6IFtdXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlSGVhZGVycygpIHtcclxuICAgICAgICB0aGlzLmh0dHAuZGVmYXVsdHMuaGVhZGVycy5wb3N0WydDb250ZW50LVR5cGUnXSA9ICdhcHBsaWNhdGlvbi94LXd3dy1mb3JtLXVybGVuY29kZWQnO1xyXG4gICAgfVxyXG5cclxuICAgIFRhc2tTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRGZWVkczogKGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJ3Rlc3QvbW9ja3VwRGF0YS9UYXNrTWFuYWdlci90YXNrTWFuYWdlckxpc3QuanNvbicpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldExpY2Vuc2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlcycpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldFByb2plY3REYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9wcm9qZWN0JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdDogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYXBwbHlMaWNlbnNlOiAgKGxpY2Vuc2VJZCwgZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvbG9hZCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRIYXNoQ29kZTogIChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvaGFzaCc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgLy8tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gICAgICAgICAgICByZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGVtYWlsUmVxdWVzdDogKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkZWxldGVMaWNlbnNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgbGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICByZXF1ZXN0SW1wb3J0OiAgKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvcmVxdWVzdCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRMaWNlbnNlTGlzdDogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL21hbmFnZXIvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvcHJvamVjdCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldEtleUNvZGU6ICAobGljZW5zZUlkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdHRVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcva2V5JztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzYXZlTGljZW5zZTogKGxpY2Vuc2VpZCwgbGljZW5zZU1vZGlmaWVkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQVVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJyArIGxpY2Vuc2VpZDtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBsaWNlbnNlTW9kaWZpZWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgcmV2b2tlTGljZW5zZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGVMaWNlbnNlOiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIG5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXROb3RpY2VMaXN0OiAob25TdWNjZXNzKSA9PiB7IC8vIHJlYWwgd3MgZXhhbXBsZVxyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL25vdGljZXMnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY3JlYXRlTm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZWRpdE5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BVVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGVsZXRlTm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3Mvbm90aWNlcy8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIEVTNiBJbnRlcmNlcHRvciBjYWxscyBpbm5lciBtZXRob2RzIGluIGEgZ2xvYmFsIHNjb3BlLCB0aGVuIHRoZSBcInRoaXNcIiBpcyBiZWluZyBsb3N0XHJcbiAqIGluIHRoZSBkZWZpbml0aW9uIG9mIHRoZSBDbGFzcyBmb3IgaW50ZXJjZXB0b3JzIG9ubHlcclxuICogVGhpcyBpcyBhIGludGVyZmFjZSB0aGF0IHRha2UgY2FyZSBvZiB0aGUgaXNzdWUuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IC8qIGludGVyZmFjZSovIGNsYXNzIEh0dHBJbnRlcmNlcHRvciB7XHJcbiAgICBjb25zdHJ1Y3RvcihtZXRob2RUb0JpbmQpIHtcclxuICAgICAgICAvLyBJZiBub3QgbWV0aG9kIHRvIGJpbmQsIHdlIGFzc3VtZSBvdXIgaW50ZXJjZXB0b3IgaXMgdXNpbmcgYWxsIHRoZSBpbm5lciBmdW5jdGlvbnNcclxuICAgICAgICBpZighbWV0aG9kVG9CaW5kKSB7XHJcbiAgICAgICAgICAgIFsncmVxdWVzdCcsICdyZXF1ZXN0RXJyb3InLCAncmVzcG9uc2UnLCAncmVzcG9uc2VFcnJvciddXHJcbiAgICAgICAgICAgICAgICAuZm9yRWFjaCgobWV0aG9kKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpc1ttZXRob2RdKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXNbbWV0aG9kXSA9IHRoaXNbbWV0aG9kXS5iaW5kKHRoaXMpO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIC8vIG1ldGhvZFRvQmluZCByZWZlcmVuY2UgdG8gYSBzaW5nbGUgY2hpbGQgY2xhc3NcclxuICAgICAgICAgICAgdGhpc1ttZXRob2RUb0JpbmRdID0gdGhpc1ttZXRob2RUb0JpbmRdLmJpbmQodGhpcyk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgIH1cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqIFVzZSB0aGlzIG1vZHVsZSB0byBtb2RpZnkgYW55dGhpbmcgcmVsYXRlZCB0byB0aGUgSGVhZGVycyBhbmQgUmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5cclxuXHJcbnZhciBIVFRQTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkhUVFBNb2R1bGUnLCBbJ25nUmVzb3VyY2UnXSkuY29uZmlnKFsnJGh0dHBQcm92aWRlcicsIGZ1bmN0aW9uKCRodHRwUHJvdmlkZXIpe1xyXG5cclxuICAgIC8vaW5pdGlhbGl6ZSBnZXQgaWYgbm90IHRoZXJlXHJcbiAgICBpZiAoISRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXQpIHtcclxuICAgICAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0ID0ge307XHJcbiAgICB9XHJcblxyXG4gICAgLy9EaXNhYmxlIElFIGFqYXggcmVxdWVzdCBjYWNoaW5nXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydJZi1Nb2RpZmllZC1TaW5jZSddID0gJ01vbiwgMjYgSnVsIDE5OTcgMDU6MDA6MDAgR01UJztcclxuICAgIC8vIGV4dHJhXHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydDYWNoZS1Db250cm9sJ10gPSAnbm8tY2FjaGUnO1xyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnUHJhZ21hJ10gPSAnbm8tY2FjaGUnO1xyXG5cclxuXHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlcXVlc3RcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAvLyBJbmplY3RzIG91ciBJbnRlcmNlcHRvcnMgZm9yIFJlc3BvbnNlXHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyk7XHJcblxyXG5cclxufV0pO1xyXG5cclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBIVFRQTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIEl0IGltcGxlbWVudCBhbiBhYnN0cmFjdCBjYWxsIHRvIEhUVFAgSW50ZXJjZXB0b3JzIHRvIG1hbmFnZSBlcnJvciBoYW5kbGVyXHJcbiAqIFNvbWV0aW1lcyBhIHJlcXVlc3QgY2FuJ3QgYmUgc2VudCBvciBpdCBpcyByZWplY3RlZCBieSBhbiBpbnRlcmNlcHRvci5cclxuICogUmVxdWVzdCBlcnJvciBpbnRlcmNlcHRvciBjYXB0dXJlcyByZXF1ZXN0cyB0aGF0IGhhdmUgYmVlbiBjYW5jZWxlZCBieSBhIHByZXZpb3VzIHJlcXVlc3QgaW50ZXJjZXB0b3IuXHJcbiAqIEl0IGNhbiBiZSB1c2VkIGluIG9yZGVyIHRvIHJlY292ZXIgdGhlIHJlcXVlc3QgYW5kIHNvbWV0aW1lcyB1bmRvIHRoaW5ncyB0aGF0IGhhdmUgYmVlbiBzZXQgdXAgYmVmb3JlIGEgcmVxdWVzdCxcclxuICogbGlrZSByZW1vdmluZyBvdmVybGF5cyBhbmQgbG9hZGluZyBpbmRpY2F0b3JzLCBlbmFibGluZyBidXR0b25zIGFuZCBmaWVsZHMgYW5kIHNvIG9uLlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3JlcXVlc3RFcnJvcicpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdEVycm9yKHJlamVjdGlvbikge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vaWYgKGNhblJlY292ZXIocmVqZWN0aW9uKSkge1xyXG4gICAgICAgIC8vICAgIHJldHVybiByZXNwb25zZU9yTmV3UHJvbWlzZVxyXG4gICAgICAgIC8vfVxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnEucmVqZWN0KHJlamVjdGlvbik7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuRXJyb3IoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSXQgaW1wbGVtZW50IGFuIGFic3RyYWN0IGNhbGwgdG8gSFRUUCBJbnRlcmNlcHRvcnMgdG8gbWFuYWdlIG9ubHkgcmVxdWVzdFxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXF1ZXN0Jyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVxdWVzdChjb25maWcpIHtcclxuICAgICAgICAvLyBXZSBjYW4gYWRkIGhlYWRlcnMgaWYgb24gdGhlIGluY29taW5nIHJlcXVlc3QgbWFkZSBpdCB3ZSBoYXZlIHRoZSB0b2tlbiBpbnNpZGVcclxuICAgICAgICAvLyBkZWZpbmVkIGJ5IHNvbWUgY29uZGl0aW9uc1xyXG4gICAgICAgIC8vY29uZmlnLmhlYWRlcnNbJ3gtc2Vzc2lvbi10b2tlbiddID0gbXkudG9rZW47XHJcblxyXG4gICAgICAgIGNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KGNvbmZpZyk7XHJcblxyXG4gICAgICAgIHJldHVybiBjb25maWcgfHwgdGhpcy5xLndoZW4oY29uZmlnKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXF1ZXN0KCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIElmIGJhY2tlbmQgY2FsbCBmYWlscyBvciBpdCBtaWdodCBiZSByZWplY3RlZCBieSBhIHJlcXVlc3QgaW50ZXJjZXB0b3Igb3IgYnkgYSBwcmV2aW91cyByZXNwb25zZSBpbnRlcmNlcHRvcjtcclxuICogSW4gdGhvc2UgY2FzZXMsIHJlc3BvbnNlIGVycm9yIGludGVyY2VwdG9yIGNhbiBoZWxwIHVzIHRvIHJlY292ZXIgdGhlIGJhY2tlbmQgY2FsbC5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZUVycm9yJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzcG9uc2VFcnJvcihyZWplY3Rpb24pIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvL2lmIChjYW5SZWNvdmVyKHJlamVjdGlvbikpIHtcclxuICAgICAgICAvLyAgICByZXR1cm4gcmVzcG9uc2VPck5ld1Byb21pc2VcclxuICAgICAgICAvLyB9XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlamVjdGlvbik7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMucS5yZWplY3QocmVqZWN0aW9uKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5FcnJvcigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBUaGlzIG1ldGhvZCBpcyBjYWxsZWQgcmlnaHQgYWZ0ZXIgJGh0dHAgcmVjZWl2ZXMgdGhlIHJlc3BvbnNlIGZyb20gdGhlIGJhY2tlbmQsXHJcbiAqIHNvIHlvdSBjYW4gbW9kaWZ5IHRoZSByZXNwb25zZSBhbmQgbWFrZSBvdGhlciBhY3Rpb25zLiBUaGlzIGZ1bmN0aW9uIHJlY2VpdmVzIGEgcmVzcG9uc2Ugb2JqZWN0IGFzIGEgcGFyYW1ldGVyXHJcbiAqIGFuZCBoYXMgdG8gcmV0dXJuIGEgcmVzcG9uc2Ugb2JqZWN0IG9yIGEgcHJvbWlzZS4gVGhlIHJlc3BvbnNlIG9iamVjdCBpbmNsdWRlc1xyXG4gKiB0aGUgcmVxdWVzdCBjb25maWd1cmF0aW9uLCBoZWFkZXJzLCBzdGF0dXMgYW5kIGRhdGEgdGhhdCByZXR1cm5lZCBmcm9tIHRoZSBiYWNrZW5kLlxyXG4gKiBSZXR1cm5pbmcgYW4gaW52YWxpZCByZXNwb25zZSBvYmplY3Qgb3IgcHJvbWlzZSB0aGF0IHdpbGwgYmUgcmVqZWN0ZWQsIHdpbGwgbWFrZSB0aGUgJGh0dHAgY2FsbCB0byBmYWlsLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXNwb25zZScpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXNwb25zZShyZXNwb25zZSkge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBzdWNjZXNzXHJcblxyXG4gICAgICAgIHJlc3BvbnNlLmNvbmZpZy5yZXNwb25zZVRpbWVzdGFtcCA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZXNwb25zZSk7XHJcbiAgICAgICAgcmV0dXJuIHJlc3BvbnNlIHx8IHRoaXMucS53aGVuKHJlc3BvbnNlKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5SZXNwb25zZSgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG59XHJcblxyXG4iXX0=
