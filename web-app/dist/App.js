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

},{"../modules/header/HeaderModule.js":19,"../modules/licenseAdmin/LicenseAdminModule.js":20,"../modules/licenseManager/LicenseManagerModule.js":28,"../modules/noticeManager/NoticeManagerModule.js":33,"../modules/taskManager/TaskManagerModule.js":37,"../services/RestAPI/RestAPIModule.js":42,"../services/http/HTTPModule.js":45,"angular":"angular","angular-animate":"angular-animate","angular-mocks":"angular-mocks","angular-resource":"angular-resource","angular-sanitize":"angular-sanitize","angular-translate":"angular-translate","angular-translate-loader-partial":"angular-translate-loader-partial","angular-ui-bootstrap":"angular-ui-bootstrap","ngClipboard":7,"rx-angular":"rx-angular","ui-router":"ui-router"}],12:[function(require,module,exports){
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

},{}],13:[function(require,module,exports){
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
 * Created by Jorge Morayta on 12/14/2015.
 * It handler the index for any of the directives available
 */

require('./Tools/ToastHandler.js');
require('./Tools/ModalWindowActivation.js');

},{"./Tools/ModalWindowActivation.js":13,"./Tools/ToastHandler.js":14}],16:[function(require,module,exports){
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

},{"./config/AngularProviderHelper.js":10,"./config/App.js":11,"./directives/index":15}],17:[function(require,module,exports){
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

},{}],18:[function(require,module,exports){
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

},{}],19:[function(require,module,exports){
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

},{"../dialogAction/DialogAction.js":17,"./HeaderController.js":18,"angular":"angular"}],20:[function(require,module,exports){
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
LicenseAdminModule.controller('RequestLicense', ['$log', 'LicenseAdminService', '$uibModalInstance', _RequestLicense2.default]);
LicenseAdminModule.controller('CreatedLicense', ['$log', '$uibModalInstance', 'params', _CreatedLicense2.default]);
LicenseAdminModule.controller('ApplyLicenseKey', ['$log', 'LicenseAdminService', '$uibModalInstance', 'params', _ApplyLicenseKey2.default]);
LicenseAdminModule.controller('ManuallyRequest', ['$log', 'LicenseAdminService', '$uibModalInstance', 'params', _ManuallyRequest2.default]);
LicenseAdminModule.controller('LicenseDetail', ['$log', 'LicenseAdminService', '$uibModal', '$uibModalInstance', 'params', _LicenseDetail2.default]);

exports.default = LicenseAdminModule;

},{"./applyLicenseKey/ApplyLicenseKey.js":21,"./created/CreatedLicense.js":22,"./detail/LicenseDetail.js":23,"./list/LicenseAdminList.js":24,"./manuallyRequest/ManuallyRequest.js":25,"./request/RequestLicense.js":26,"./service/LicenseAdminService.js":27,"angular":"angular","ui-router":"ui-router"}],21:[function(require,module,exports){
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

var ApplyLicenseKey = function () {
    function ApplyLicenseKey($log, licenseAdminService, $uibModalInstance, params) {
        _classCallCheck(this, ApplyLicenseKey);

        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = params.license;
    }

    /**
     * Execute and validate the Key is correct
     */

    _createClass(ApplyLicenseKey, [{
        key: 'applyKey',
        value: function applyKey() {
            var _this = this;

            this.licenseAdminService.applyLicense(this.licenseModel, function (data) {
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

    return ApplyLicenseKey;
}();

exports.default = ApplyLicenseKey;

},{}],22:[function(require,module,exports){
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

},{}],23:[function(require,module,exports){
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
            serversTokens: params.license.serversTokens || params.license.maxServers,
            environmentName: params.license.environment.name,
            inception: params.license.requestDate,
            expiration: params.license.expirationDate,
            specialInstructions: params.license.requestNote,
            applied: params.license.applied,
            id: params.license.id,
            replaced: params.license.replaced,
            encryptedDetail: params.license.encryptedDetail
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
                        console.log(_this.licenseModel);
                        return { license: _this.licenseModel };
                    }
                }
            });

            modalInstance.result.then(function () {
                _this.licenseModel.applied = true;
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
            this.uibModalInstance.dismiss('cancel');
        }
    }]);

    return LicenseDetail;
}();

exports.default = LicenseDetail;

},{}],24:[function(require,module,exports){
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
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project' }, { field: 'email', title: 'Contact Email' }, { field: 'status.name', title: 'Status' }, { field: 'type.name', title: 'Type' }, { field: 'method.name', title: 'Method' }, { field: 'method.id', hidden: true }, { field: 'serversTokens', title: 'Server/Tokens', template: '#:maxServers#' }, { field: 'requestDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}' }, { field: 'environment.name', title: 'Env.' }],
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
                        field: 'title',
                        dir: 'asc'
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
                size: 'md',
                draggable: true
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
                        var dataItem = license && license.dataItem;
                        return { license: dataItem };
                    }
                }
            });

            modalInstance.result.then(function () {
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

},{}],25:[function(require,module,exports){
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

        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = params.license;
    }

    /**
     * Execute and validate the Key is correct
     */

    _createClass(ManuallyRequest, [{
        key: 'emailRequest',
        value: function emailRequest() {
            var _this = this;

            this.licenseAdminService.emailRequest(this.licenseModel, function (data) {
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

    return ManuallyRequest;
}();

exports.default = ManuallyRequest;

},{}],26:[function(require,module,exports){
/**
 * Created by Jorge Morayta on 09/26/2016.
 * Create a new Request to get a License
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

    /**
     * Initialize all the properties
     * @param $log
     * @param licenseAdminService
     * @param $uibModalInstance
     */
    function RequestLicense($log, licenseAdminService, $uibModalInstance) {
        _classCallCheck(this, RequestLicense);

        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;

        // Defined the Environment Select
        this.environmentDataSource = [];
        // Define the Project Select
        this.selectProject = {};
        this.selectProjectListOptions = [];

        this.getEnvironmentDataSource();
        this.getProjectDataSource();

        // Create the Model for the New License
        this.newLicenseModel = {
            email: '',
            environmentId: 0,
            projectId: 0,
            clientName: '',
            requestNote: ''
        };
    }

    /**
     * Populate the Environment dropdown values
     */

    _createClass(RequestLicense, [{
        key: 'getEnvironmentDataSource',
        value: function getEnvironmentDataSource() {
            var _this = this;

            this.licenseAdminService.getEnvironmentDataSource(function (data) {
                _this.environmentDataSource = data;
                _this.newLicenseModel.environmentId = data[0].id;
            });
        }

        /**
         * Populate the Project dropdown values
         */

    }, {
        key: 'getProjectDataSource',
        value: function getProjectDataSource() {
            var _this2 = this;

            this.selectProjectListOptions = {
                dataSource: {
                    transport: {
                        read: function read(e) {
                            _this2.licenseAdminService.getProjectDataSource(function (data) {
                                _this2.newLicenseModel.projectId = data[0].id;
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
                    _this2.newLicenseModel.clientName = item.client.name;
                }
            };
        }

        /**
         * Execute the Service call to generate a new License request
         */

    }, {
        key: 'saveLicenseRequest',
        value: function saveLicenseRequest() {
            var _this3 = this;

            this.log.info('New License Requested: ', this.newLicenseModel);
            this.licenseAdminService.createNewLicenseRequest(this.newLicenseModel, function (data) {
                _this3.uibModalInstance.close(_this3.newLicenseModel);
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

},{}],27:[function(require,module,exports){
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
         *  Apply The Lincense
         * @param license
         * @param onSuccess
         */

    }, {
        key: 'applyLicense',
        value: function applyLicense(license, onSuccess) {
            var _this3 = this;

            var hash = {
                hash: license.key
            };

            this.restService.licenseAdminServiceHandler().applyLicense(license.id, hash, function (data) {
                if (data.status === _this3.statusSuccess) {
                    _this3.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully applied' });
                } else {
                    _this3.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was successfully applied' });
                }
                return onSuccess(data);
            });
        }
    }, {
        key: 'deleteLicense',
        value: function deleteLicense(license, callback) {
            this.restService.licenseAdminServiceHandler().deleteLicense(license, function (data) {
                return callback(data);
            });
        }
    }]);

    return LicenseAdminService;
}();

exports.default = LicenseAdminService;

},{}],28:[function(require,module,exports){
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
LicenseManagerModule.controller('RequestImport', ['$log', 'LicenseManagerService', '$uibModalInstance', _RequestImport2.default]);
LicenseManagerModule.controller('LicenseManagerDetail', ['$log', 'LicenseManagerService', '$uibModal', '$uibModalInstance', 'params', _LicenseManagerDetail2.default]);

exports.default = LicenseManagerModule;

},{"./detail/LicenseManagerDetail.js":29,"./list/LicenseManagerList.js":30,"./requestImport/RequestImport.js":31,"./service/LicenseManagerService.js":32,"angular":"angular","ui-router":"ui-router"}],29:[function(require,module,exports){
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

var LicenseManagerDetail = function () {
    function LicenseManagerDetail($log, licenseManagerService, $uibModal, $uibModalInstance, params) {
        var _this = this;

        _classCallCheck(this, LicenseManagerDetail);

        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.uibModal = $uibModal;
        this.log = $log;

        this.editMode = false;
        this.licenseModel = {
            principalId: params.license.principal.id,
            email: params.license.contact_email,
            projectId: params.license.project.id,
            clientId: params.license.client.id,
            statusId: params.license.status.id,
            method: {
                id: params.license.method.id,
                name: params.license.method.name,
                quantity: params.license.method.quantity
            },
            environmentId: params.license.environment.id,
            requested: params.license.requested,
            initDate: params.license.initDate,
            endDate: params.license.endDate,
            specialInstructions: params.license.specialInstructions,
            bannerMessage: params.license.bannerMessage,
            requestedId: params.license.requestedId,
            replaced: params.license.replaced,
            replacedId: params.license.replacedId,
            licenseKey: params.license.licenseKey,
            activityList: params.license.activityList,
            hostName: params.license.hostName,
            websiteName: params.license.websiteName,
            hash: params.license.hash,

            applied: params.license.applied,
            keyId: params.license.keyId
        };

        // Creates the Kendo Project Select List
        this.selectProject = {};
        this.selectProjectListOptions = {
            dataSource: this.getProjectsDataSource(),
            optionLabel: 'Select a Project',
            dataTextField: 'name',
            dataValueField: 'id',
            valuePrimitive: true,
            select: function select(e) {
                var item = _this.selectProject.dataItem(e.item);
                _this.onChangeProject(item);
            }
        };

        // Init the two Kendo Dates for Init and EndDate
        this.initDate = {};
        this.initDateOptions = {
            format: 'yyyy/MM/dd',
            max: this.licenseModel.endDate,
            change: function change(e) {
                _this.onChangeInitDate();
            }
        };

        this.endDate = {};
        this.endDateOptions = {
            format: 'yyyy/MM/dd',
            min: this.licenseModel.initDate,
            change: function change(e) {
                _this.onChangeEndDate();
            }
        };

        this.prepareControlActionButtons();

        this.getPrincipalDataSource();
        this.getEnvironmentDataSource();
        this.getClientDataSource();
        this.getStatusDataSource();

        this.prepareMethodOptions();
        this.prepareActivityList();
    }

    /**
     * Controls what buttons to show
     */

    _createClass(LicenseManagerDetail, [{
        key: 'prepareControlActionButtons',
        value: function prepareControlActionButtons() {
            this.pendingLicense = this.licenseModel.statusId === 2 && !this.editMode;
            this.expiredOrTerminated = this.licenseModel.statusId === 3 || this.licenseModel.statusId === 4;
            this.activeShowMode = this.licenseModel.statusId === 1 && !this.expiredOrTerminated && !this.editMode;
        }
    }, {
        key: 'prepareMethodOptions',
        value: function prepareMethodOptions() {
            this.methodOptions = [{
                id: 1,
                name: 'Servers',
                quantity: 8000
            }, {
                id: 2,
                name: 'Tokens',
                quantity: 40000
            }, {
                id: 3,
                name: 'Custom'
            }];
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
            var _this2 = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseManager/applyLicenseKey/ApplyLicenseKey.html',
                controller: 'ApplyLicenseKey as applyLicenseKey',
                size: 'md',
                resolve: {
                    params: function params() {
                        return { license: _this2.licenseModel };
                    }
                }
            });

            modalInstance.result.then(function () {
                _this2.licenseModel.applied = true;
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
            var _this3 = this;

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
                _this3.licenseManagerService.revokeLicense(_this3.licenseModel, function (data) {
                    _this3.uibModalInstance.close(data);
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
                var newVal = parseInt(this.licenseModel.method.quantity);
                if (!isNaN(newVal)) {
                    this.licenseModel.method.quantity = newVal;
                } else {
                    this.licenseModel.method.quantity = 0;
                }

                if (e && e.currentTarget && e.currentTarget.value) {
                    e.currentTarget.value = this.licenseModel.method.quantity;
                }
            } catch (e) {
                this.$log.warn('Invalid Number Expception', this.licenseModel.method.quantity);
            }
        }

        /**
         * Save current changes
         */

    }, {
        key: 'saveLicense',
        value: function saveLicense() {
            var _this4 = this;

            this.licenseManagerService.saveLicense(this.licenseModel, function (data) {
                _this4.uibModalInstance.close(data);
                _this4.log.info('License Saved');
            });
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
        key: 'getPrincipalDataSource',
        value: function getPrincipalDataSource() {
            this.principalDataSource = [{ id: 1, name: 'EMC' }, { id: 2, name: 'IBM' }];
        }

        /**
         * Populate values
         */

    }, {
        key: 'getEnvironmentDataSource',
        value: function getEnvironmentDataSource() {
            this.environmentDataSource = [{ id: 1, name: 'Production' }, { id: 2, name: 'Other' }];
        }

        /**
         * Populate values
         */

    }, {
        key: 'getProjectsDataSource',
        value: function getProjectsDataSource() {
            return [{ id: 1, name: 'n/a' }, { id: 2, name: 'Bank East' }];
        }

        /**
         * Populate values
         */

    }, {
        key: 'getClientDataSource',
        value: function getClientDataSource() {
            this.clientsDataSource = [{ id: 1, name: 'n/a' }, { id: 2, name: 'Gold Bank' }];
        }

        /**
         * Populate values
         */

    }, {
        key: 'getStatusDataSource',
        value: function getStatusDataSource() {
            this.statusDataSource = [{ id: 1, name: 'Active' }, { id: 2, name: 'Pending' }, { id: 3, name: 'Expired' }, { id: 4, name: 'Terminated' }];
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
            } else if (endDate) {
                this.initDate.max(new Date(endDate));
            } else {
                endDate = new Date();
                this.initDate.initDate.max(endDate);
                this.endDate.min(endDate);
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
                this.initDate.max(endDate);
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
            if (this.editMode) {
                this.editMode = false;
                this.prepareControlActionButtons();
            } else {
                this.uibModalInstance.dismiss('cancel');
            }
        }
    }]);

    return LicenseManagerDetail;
}();

exports.default = LicenseManagerDetail;

},{}],30:[function(require,module,exports){
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
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseManagerList.onLicenseManagerDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'principal.name', title: 'Principal' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project' }, { field: 'contact_email', title: 'Contact Email' }, { field: 'status.type', title: 'Status' }, { field: 'type', title: 'Type' }, { field: 'method.name', title: 'Method' }, { field: 'method.id', hidden: true }, { field: 'servers_tokens', title: 'Server/Tokens' }, { field: 'inception', title: 'Inception' }, { field: 'expiration', title: 'Expiration' }, { field: 'environment', title: 'Env.' }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            /*this.licenseManagerService.getLicenseList((data) => {*/
                            var data = [{
                                licenseId: 1,
                                keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                action: '',
                                principal: {
                                    id: 1,
                                    name: 'EMC'
                                },
                                client: {
                                    id: 1,
                                    name: 'n/a'
                                },
                                project: {
                                    id: 1,
                                    name: 'n/a'
                                },
                                contact_email: 'west.coast@xyyy.com',
                                status: {
                                    id: 1,
                                    type: 'Active'
                                },
                                type: 'Multi-Project',
                                method: {
                                    id: 1,
                                    name: 'Server',
                                    quantity: 4000
                                },
                                initDate: '2016-09-15',
                                endDate: '2016-09-18',
                                requested: '2016-12-08',
                                environment: {
                                    id: 1,
                                    name: 'Production'
                                },
                                specialInstructions: 'Help, Help, Help',
                                bannerMessage: 'This application should only be used for training purpose',
                                requestedId: '5646546546546545-asdasdasd54asd-asdas6asdasd',
                                applied: false,
                                replaced: [{ id: 1, status: '2016-05-08' + ' http:blablaba.com ' + ' aasdas54-5asd4a5sd-asd45a4sd ' }, { id: 2, status: '2016-05-10' + ' http:blablaba.com ' + ' aasdas54-5asd4a5sd-asd45a4sd ' }],
                                replacedId: 1,
                                licenseKey: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618',
                                activityList: [{ date: '2016-05-08', whom: 'Harry Legs', action: 'Requested' }, { date: '2016-05-10', whom: 'Robin Banks', action: 'Imported' }, { date: '2016-05-11', whom: 'Ben D Ovar', action: 'Activated' }, { date: '2016-05-14', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-18', whom: 'Ben D Ovar', action: 'Unlocked License to modify' }, { date: '2016-05-20', whom: 'Ben D Ovar', action: 'Modified License detail' }, { date: '2016-05-21', whom: 'Ben D Ovar', action: 'Activated' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }],
                                hostName: 'tm-acme-bigmove-.somedomaing.local',
                                websiteName: 'tranman.somecorp.com',
                                hash: 'kaskldsaldkjasda5s4a65sda65sd4a65sd46a5sd4as65d'
                            }, {
                                licenseId: 2,
                                keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                action: '',
                                principal: {
                                    id: 2,
                                    name: 'IBM'
                                },
                                client: {
                                    id: 2,
                                    name: 'Gold Bank'
                                },
                                project: {
                                    id: 2,
                                    name: 'Bank East'
                                },
                                contact_email: 'west.coast@xyyy.com',
                                status: {
                                    id: 2,
                                    type: 'Pending'
                                },
                                type: 'Project',
                                method: {
                                    id: 2,
                                    name: 'Token',
                                    quantity: 5000
                                },
                                initDate: '2016-09-15',
                                endDate: '2016-09-18',
                                requested: '2016-12-08',
                                environment: {
                                    id: 1,
                                    name: 'Production'
                                },
                                specialInstructions: 'Help, Help, Help',
                                requestedId: '5646546546546545-asdasdasd54asd-asdas6asdasd',
                                applied: false,
                                bannerMessage: 'This application should only be used for training purpose',
                                replaced: [{ id: 1, status: '2016-05-08' + ' http:blablaba.com ' + ' aasdas54-5asd4a5sd-asd45a4sd ' }, { id: 2, status: '2016-05-10' + ' http:blablaba.com ' + ' aasdas54-5asd4a5sd-asd45a4sd ' }],
                                replacedId: 1,
                                licenseKey: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618',
                                activityList: [{ date: '2016-05-08', whom: 'Harry Legs', action: 'Requested' }, { date: '2016-05-10', whom: 'Robin Banks', action: 'Imported' }, { date: '2016-05-11', whom: 'Ben D Ovar', action: 'Activated' }, { date: '2016-05-14', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-18', whom: 'Ben D Ovar', action: 'Unlocked License to modify' }, { date: '2016-05-20', whom: 'Ben D Ovar', action: 'Modified License detail' }, { date: '2016-05-21', whom: 'Ben D Ovar', action: 'Activated' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }],
                                hostName: 'tm-acme-bigmove-.somedomaing.local',
                                websiteName: 'tranman.somecorp.com',
                                hash: 'kaskldsaldkjasda5s4a65sda65sd4a65sd46a5sd4as65d'
                            }, {
                                licenseId: 3,
                                keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                action: '',
                                principal: {
                                    id: 2,
                                    name: 'IBM'
                                },
                                client: {
                                    id: 2,
                                    name: 'Gold Bank'
                                },
                                project: {
                                    id: 2,
                                    name: 'Bank East'
                                },
                                contact_email: 'west.coast@xyyy.com',
                                status: {
                                    id: 3,
                                    type: 'Expired'
                                },
                                type: 'Project',
                                method: {
                                    id: 2,
                                    name: 'Token',
                                    quantity: 5000
                                },
                                initDate: '2016-09-15',
                                endDate: '2016-09-18',
                                requested: '2016-12-08',
                                environment: {
                                    id: 1,
                                    name: 'Production'
                                },
                                specialInstructions: 'Help, Help, Help',
                                requestedId: '5646546546546545-asdasdasd54asd-asdas6asdasd',
                                applied: false,
                                bannerMessage: 'This application should only be used for training purpose',
                                replaced: [{ id: 1, status: '2016-05-08' + ' http:blablaba.com ' + ' aasdas54-5asd4a5sd-asd45a4sd ' }, { id: 2, status: '2016-05-10' + ' http:blablaba.com ' + ' aasdas54-5asd4a5sd-asd45a4sd ' }],
                                replacedId: 1,
                                licenseKey: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618',
                                activityList: [{ date: '2016-05-08', whom: 'Harry Legs', action: 'Requested' }, { date: '2016-05-10', whom: 'Robin Banks', action: 'Imported' }, { date: '2016-05-11', whom: 'Ben D Ovar', action: 'Activated' }, { date: '2016-05-14', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-18', whom: 'Ben D Ovar', action: 'Unlocked License to modify' }, { date: '2016-05-20', whom: 'Ben D Ovar', action: 'Modified License detail' }, { date: '2016-05-21', whom: 'Ben D Ovar', action: 'Activated' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }],
                                hostName: 'tm-acme-bigmove-.somedomaing.local',
                                websiteName: 'tranman.somecorp.com',
                                hash: 'kaskldsaldkjasda5s4a65sda65sd4a65sd46a5sd4as65d'
                            }, {
                                licenseId: 4,
                                keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                action: '',
                                principal: {
                                    id: 2,
                                    name: 'IBM'
                                },
                                client: {
                                    id: 2,
                                    name: 'Gold Bank'
                                },
                                project: {
                                    id: 2,
                                    name: 'Bank East'
                                },
                                contact_email: 'west.coast@xyyy.com',
                                status: {
                                    id: 4,
                                    type: 'Terminated'
                                },
                                type: 'Project',
                                method: {
                                    id: 2,
                                    name: 'Token',
                                    quantity: 5000
                                },
                                initDate: '2016-09-15',
                                endDate: '2016-09-18',
                                requested: '2016-12-08',
                                environment: {
                                    id: 1,
                                    name: 'Production'
                                },
                                specialInstructions: 'Help, Help, Help',
                                requestedId: '5646546546546545-asdasdasd54asd-asdas6asdasd',
                                applied: false,
                                bannerMessage: 'This application should only be used for training purpose',
                                replaced: [{ id: 1, status: '2016-05-08' + ' http:blablaba.com ' + ' aasdas54-5asd4a5sd-asd45a4sd ' }, { id: 2, status: '2016-05-10' + ' http:blablaba.com ' + ' aasdas54-5asd4a5sd-asd45a4sd ' }],
                                replacedId: 1,
                                licenseKey: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618',
                                activityList: [{ date: '2016-05-08', whom: 'Harry Legs', action: 'Requested' }, { date: '2016-05-10', whom: 'Robin Banks', action: 'Imported' }, { date: '2016-05-11', whom: 'Ben D Ovar', action: 'Activated' }, { date: '2016-05-14', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-18', whom: 'Ben D Ovar', action: 'Unlocked License to modify' }, { date: '2016-05-20', whom: 'Ben D Ovar', action: 'Modified License detail' }, { date: '2016-05-21', whom: 'Ben D Ovar', action: 'Activated' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }, { date: '2016-05-28', whom: 'Ben D Ovar', action: 'Emailed License' }],
                                hostName: 'tm-acme-bigmove-.somedomaing.local',
                                websiteName: 'tranman.somecorp.com',
                                hash: 'kaskldsaldkjasda5s4a65sda65sd4a65sd46a5sd4as65d'
                            }];
                            e.success(data);
                            /* });*/
                        }
                    },
                    change: function change(e) {
                        // We are coming from a new imported request license
                        if (_this.openLastImportedLicenseId !== 0 && _this.licenseGrid.dataSource._data) {
                            var newLicenseCreated = _this.licenseGrid.dataSource._data.find(function (license) {
                                return license.licenseId === _this.openLastImportedLicenseId;
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

            modalInstance.result.then(function (data) {
                _this2.openLastImportedLicenseId = 1; // take this param from the last imported license, of course
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

            modalInstance.result.then(function () {}, function () {
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

},{}],31:[function(require,module,exports){
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

var RequestImport = function () {
    function RequestImport($log, licenseManagerService, $uibModalInstance) {
        _classCallCheck(this, RequestImport);

        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.licenseModel = {
            license: ''
        };
    }

    /**
     * Execute and validate the Key is correct
     */

    _createClass(RequestImport, [{
        key: 'onImportLicense',
        value: function onImportLicense() {
            var _this = this;

            this.licenseManagerService.importLicense(this.licenseModel, function (data) {
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

    return RequestImport;
}();

exports.default = RequestImport;

},{}],32:[function(require,module,exports){
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
        this.log.debug('licenseManagerService Instanced');
    }

    _createClass(LicenseManagerService, [{
        key: 'getLicenseList',
        value: function getLicenseList(callback) {
            this.restService.licenseManagerServiceHandler().getLicenseList(function (data) {
                return callback(data);
            });
        }

        /**
         * Save the License
         */

    }, {
        key: 'saveLicense',
        value: function saveLicense(license, callback) {
            this.restService.licenseManagerServiceHandler().saveLicense(license, function (data) {
                return callback(data);
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
        value: function importLicense(license, callback) {
            var _this2 = this;

            this.restService.licenseManagerServiceHandler().requestImport(license, function (data) {
                //if(data.applied) {
                _this2.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully Imported' });
                /*} else {
                    this.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was successfully applied'});
                }*/
                return callback(data);
            });
        }
    }, {
        key: 'revokeLicense',
        value: function revokeLicense(license, callback) {
            this.restService.licenseManagerServiceHandler().revokeLicense(license, function (data) {
                return callback(data);
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

},{}],33:[function(require,module,exports){
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

},{"./edit/EditNotice.js":34,"./list/NoticeList.js":35,"./service/NoticeManagerService.js":36,"angular":"angular","ui-router":"ui-router"}],34:[function(require,module,exports){
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

},{}],35:[function(require,module,exports){
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

},{}],36:[function(require,module,exports){
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

},{}],37:[function(require,module,exports){
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

},{"../../directives/Svg/svgLoader.js":12,"./edit/TaskManagerEdit.js":38,"./list/TaskManagerController.js":39,"./service/TaskManagerService.js":40,"angular":"angular","ui-router":"ui-router"}],38:[function(require,module,exports){
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

},{}],39:[function(require,module,exports){
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

},{}],40:[function(require,module,exports){
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
                getLicense: function getLicense(callback) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/licenses'), callback);
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
                getLicenseList: function getLicenseList(data, onSuccess) {
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.get('../ws/license'), onSuccess);
                },
                saveLicense: function saveLicense(data, callback) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/???';
                    _this3.req.data = data;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
                },
                requestImport: function requestImport(data, callback) {
                    _this3.req.method = 'POST';
                    _this3.req.url = '../ws/???';
                    _this3.req.data = data;
                    return new _RequestHandler2.default(_this3.rx).subscribeRequest(_this3.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
                },
                revokeLicense: function revokeLicense(data, onSuccess, onError) {
                    _this3.req.method = 'DELETE';
                    _this3.req.url = '../ws/license/' + data.id;
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
                getNoticeMockUp: function getNoticeMockUp(onSuccess) {
                    // Mockup Data for testing see url
                    return new _RequestHandler2.default(_this4.rx).subscribeRequest(_this4.http.get('../test/mockupData/NoticeManager/noticeManagerList.json'), onSuccess);
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

},{"./HTTPInterceptorInterface.js":44}]},{},[16])
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvY2xpcGJvYXJkL2xpYi9jbGlwYm9hcmQtYWN0aW9uLmpzIiwibm9kZV9tb2R1bGVzL2NsaXBib2FyZC9saWIvY2xpcGJvYXJkLmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9jbG9zZXN0LmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9kZWxlZ2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9pcy5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9saXN0ZW4uanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvZGlzdC9uZ2NsaXBib2FyZC5qcyIsIm5vZGVfbW9kdWxlcy9zZWxlY3Qvc3JjL3NlbGVjdC5qcyIsIm5vZGVfbW9kdWxlcy90aW55LWVtaXR0ZXIvaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxjb25maWdcXEFwcC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcU3ZnXFxzdmdMb2FkZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXFRvb2xzXFxNb2RhbFdpbmRvd0FjdGl2YXRpb24uanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXFRvb2xzXFxUb2FzdEhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXGluZGV4LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtYWluLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxkaWFsb2dBY3Rpb25cXERpYWxvZ0FjdGlvbi5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcaGVhZGVyXFxIZWFkZXJDb250cm9sbGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxoZWFkZXJcXEhlYWRlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxMaWNlbnNlQWRtaW5Nb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcYXBwbHlMaWNlbnNlS2V5XFxBcHBseUxpY2Vuc2VLZXkuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcY3JlYXRlZFxcQ3JlYXRlZExpY2Vuc2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcZGV0YWlsXFxMaWNlbnNlRGV0YWlsLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXGxpc3RcXExpY2Vuc2VBZG1pbkxpc3QuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcbWFudWFsbHlSZXF1ZXN0XFxNYW51YWxseVJlcXVlc3QuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxccmVxdWVzdFxcUmVxdWVzdExpY2Vuc2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcc2VydmljZVxcTGljZW5zZUFkbWluU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXExpY2Vuc2VNYW5hZ2VyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcZGV0YWlsXFxMaWNlbnNlTWFuYWdlckRldGFpbC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXGxpc3RcXExpY2Vuc2VNYW5hZ2VyTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXHJlcXVlc3RJbXBvcnRcXFJlcXVlc3RJbXBvcnQuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxzZXJ2aWNlXFxMaWNlbnNlTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXE5vdGljZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXGVkaXRcXEVkaXROb3RpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXGxpc3RcXE5vdGljZUxpc3QuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXHNlcnZpY2VcXE5vdGljZU1hbmFnZXJTZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFx0YXNrTWFuYWdlclxcVGFza01hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxlZGl0XFxUYXNrTWFuYWdlckVkaXQuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxsaXN0XFxUYXNrTWFuYWdlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxzZXJ2aWNlXFxUYXNrTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RBUElNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXN0U2VydmljZUhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwT0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4TEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL0ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNB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QUVBOzs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OztJLEFBRXFCOzhDQUNqQjs7NENBQUEsQUFBWSxNQUFaLEFBQWtCLElBQWxCLEFBQXNCLElBQUk7OEJBQUE7O29LQUFBLEFBQ2hCLEFBQ047O2NBQUEsQUFBSyxNQUFMLEFBQVcsQUFDWDtjQUFBLEFBQUssSUFBTCxBQUFTLEFBQ1Q7Y0FBQSxBQUFLLFFBQVEsTUFBQSxBQUFLLEVBQWxCLEFBQWEsQUFBTyxBQUNwQjtjQUFBLEFBQUssSUFBTCxBQUFTLE1BTGEsQUFLdEIsQUFBZTtlQUNsQjs7Ozs7aUMsQUFFUSxXQUFVLEFBQ2Y7QUFFQTs7c0JBQUEsQUFBUyxPQUFULEFBQWdCLG9CQUFvQixJQUFBLEFBQUksT0FBeEMsQUFBb0MsQUFBVyxBQUUvQzs7aUJBQUEsQUFBSyxNQUFMLEFBQVcsT0FBWCxBQUFrQixBQUNsQjttQkFBTyxhQUFZLEtBQUEsQUFBSyxFQUFMLEFBQU8sS0FBMUIsQUFBbUIsQUFBWSxBQUNsQzs7Ozt5Q0FFZ0IsQUFDYjttQkFBTyxLQUFBLEFBQUssTUFBWixBQUFrQixBQUNyQjs7Ozs7R0FwQnVELGM7O2tCLEFBQXZDIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIihmdW5jdGlvbiAoZ2xvYmFsLCBmYWN0b3J5KSB7XG4gICAgaWYgKHR5cGVvZiBkZWZpbmUgPT09IFwiZnVuY3Rpb25cIiAmJiBkZWZpbmUuYW1kKSB7XG4gICAgICAgIGRlZmluZShbJ21vZHVsZScsICdzZWxlY3QnXSwgZmFjdG9yeSk7XG4gICAgfSBlbHNlIGlmICh0eXBlb2YgZXhwb3J0cyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgICAgICBmYWN0b3J5KG1vZHVsZSwgcmVxdWlyZSgnc2VsZWN0JykpO1xuICAgIH0gZWxzZSB7XG4gICAgICAgIHZhciBtb2QgPSB7XG4gICAgICAgICAgICBleHBvcnRzOiB7fVxuICAgICAgICB9O1xuICAgICAgICBmYWN0b3J5KG1vZCwgZ2xvYmFsLnNlbGVjdCk7XG4gICAgICAgIGdsb2JhbC5jbGlwYm9hcmRBY3Rpb24gPSBtb2QuZXhwb3J0cztcbiAgICB9XG59KSh0aGlzLCBmdW5jdGlvbiAobW9kdWxlLCBfc2VsZWN0KSB7XG4gICAgJ3VzZSBzdHJpY3QnO1xuXG4gICAgdmFyIF9zZWxlY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfc2VsZWN0KTtcblxuICAgIGZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7XG4gICAgICAgIHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7XG4gICAgICAgICAgICBkZWZhdWx0OiBvYmpcbiAgICAgICAgfTtcbiAgICB9XG5cbiAgICB2YXIgX3R5cGVvZiA9IHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiB0eXBlb2YgU3ltYm9sLml0ZXJhdG9yID09PSBcInN5bWJvbFwiID8gZnVuY3Rpb24gKG9iaikge1xuICAgICAgICByZXR1cm4gdHlwZW9mIG9iajtcbiAgICB9IDogZnVuY3Rpb24gKG9iaikge1xuICAgICAgICByZXR1cm4gb2JqICYmIHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiBvYmouY29uc3RydWN0b3IgPT09IFN5bWJvbCAmJiBvYmogIT09IFN5bWJvbC5wcm90b3R5cGUgPyBcInN5bWJvbFwiIDogdHlwZW9mIG9iajtcbiAgICB9O1xuXG4gICAgZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3Rvcikge1xuICAgICAgICBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIkNhbm5vdCBjYWxsIGEgY2xhc3MgYXMgYSBmdW5jdGlvblwiKTtcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBfY3JlYXRlQ2xhc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIGZ1bmN0aW9uIGRlZmluZVByb3BlcnRpZXModGFyZ2V0LCBwcm9wcykge1xuICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBwcm9wcy5sZW5ndGg7IGkrKykge1xuICAgICAgICAgICAgICAgIHZhciBkZXNjcmlwdG9yID0gcHJvcHNbaV07XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5lbnVtZXJhYmxlID0gZGVzY3JpcHRvci5lbnVtZXJhYmxlIHx8IGZhbHNlO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuY29uZmlndXJhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBpZiAoXCJ2YWx1ZVwiIGluIGRlc2NyaXB0b3IpIGRlc2NyaXB0b3Iud3JpdGFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIE9iamVjdC5kZWZpbmVQcm9wZXJ0eSh0YXJnZXQsIGRlc2NyaXB0b3Iua2V5LCBkZXNjcmlwdG9yKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBmdW5jdGlvbiAoQ29uc3RydWN0b3IsIHByb3RvUHJvcHMsIHN0YXRpY1Byb3BzKSB7XG4gICAgICAgICAgICBpZiAocHJvdG9Qcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvci5wcm90b3R5cGUsIHByb3RvUHJvcHMpO1xuICAgICAgICAgICAgaWYgKHN0YXRpY1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLCBzdGF0aWNQcm9wcyk7XG4gICAgICAgICAgICByZXR1cm4gQ29uc3RydWN0b3I7XG4gICAgICAgIH07XG4gICAgfSgpO1xuXG4gICAgdmFyIENsaXBib2FyZEFjdGlvbiA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgLyoqXG4gICAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvcHRpb25zXG4gICAgICAgICAqL1xuICAgICAgICBmdW5jdGlvbiBDbGlwYm9hcmRBY3Rpb24ob3B0aW9ucykge1xuICAgICAgICAgICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIENsaXBib2FyZEFjdGlvbik7XG5cbiAgICAgICAgICAgIHRoaXMucmVzb2x2ZU9wdGlvbnMob3B0aW9ucyk7XG4gICAgICAgICAgICB0aGlzLmluaXRTZWxlY3Rpb24oKTtcbiAgICAgICAgfVxuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBEZWZpbmVzIGJhc2UgcHJvcGVydGllcyBwYXNzZWQgZnJvbSBjb25zdHJ1Y3Rvci5cbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG5cblxuICAgICAgICBfY3JlYXRlQ2xhc3MoQ2xpcGJvYXJkQWN0aW9uLCBbe1xuICAgICAgICAgICAga2V5OiAncmVzb2x2ZU9wdGlvbnMnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlc29sdmVPcHRpb25zKCkge1xuICAgICAgICAgICAgICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiB7fTtcblxuICAgICAgICAgICAgICAgIHRoaXMuYWN0aW9uID0gb3B0aW9ucy5hY3Rpb247XG4gICAgICAgICAgICAgICAgdGhpcy5lbWl0dGVyID0gb3B0aW9ucy5lbWl0dGVyO1xuICAgICAgICAgICAgICAgIHRoaXMudGFyZ2V0ID0gb3B0aW9ucy50YXJnZXQ7XG4gICAgICAgICAgICAgICAgdGhpcy50ZXh0ID0gb3B0aW9ucy50ZXh0O1xuICAgICAgICAgICAgICAgIHRoaXMudHJpZ2dlciA9IG9wdGlvbnMudHJpZ2dlcjtcblxuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gJyc7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2luaXRTZWxlY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGluaXRTZWxlY3Rpb24oKSB7XG4gICAgICAgICAgICAgICAgaWYgKHRoaXMudGV4dCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdEZha2UoKTtcbiAgICAgICAgICAgICAgICB9IGVsc2UgaWYgKHRoaXMudGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0VGFyZ2V0KCk7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdzZWxlY3RGYWtlJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBzZWxlY3RGYWtlKCkge1xuICAgICAgICAgICAgICAgIHZhciBfdGhpcyA9IHRoaXM7XG5cbiAgICAgICAgICAgICAgICB2YXIgaXNSVEwgPSBkb2N1bWVudC5kb2N1bWVudEVsZW1lbnQuZ2V0QXR0cmlidXRlKCdkaXInKSA9PSAncnRsJztcblxuICAgICAgICAgICAgICAgIHRoaXMucmVtb3ZlRmFrZSgpO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrID0gZnVuY3Rpb24gKCkge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gX3RoaXMucmVtb3ZlRmFrZSgpO1xuICAgICAgICAgICAgICAgIH07XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlciA9IGRvY3VtZW50LmJvZHkuYWRkRXZlbnRMaXN0ZW5lcignY2xpY2snLCB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2spIHx8IHRydWU7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtID0gZG9jdW1lbnQuY3JlYXRlRWxlbWVudCgndGV4dGFyZWEnKTtcbiAgICAgICAgICAgICAgICAvLyBQcmV2ZW50IHpvb21pbmcgb24gaU9TXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5mb250U2l6ZSA9ICcxMnB0JztcbiAgICAgICAgICAgICAgICAvLyBSZXNldCBib3ggbW9kZWxcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLmJvcmRlciA9ICcwJztcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLnBhZGRpbmcgPSAnMCc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5tYXJnaW4gPSAnMCc7XG4gICAgICAgICAgICAgICAgLy8gTW92ZSBlbGVtZW50IG91dCBvZiBzY3JlZW4gaG9yaXpvbnRhbGx5XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5wb3NpdGlvbiA9ICdhYnNvbHV0ZSc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZVtpc1JUTCA/ICdyaWdodCcgOiAnbGVmdCddID0gJy05OTk5cHgnO1xuICAgICAgICAgICAgICAgIC8vIE1vdmUgZWxlbWVudCB0byB0aGUgc2FtZSBwb3NpdGlvbiB2ZXJ0aWNhbGx5XG4gICAgICAgICAgICAgICAgdmFyIHlQb3NpdGlvbiA9IHdpbmRvdy5wYWdlWU9mZnNldCB8fCBkb2N1bWVudC5kb2N1bWVudEVsZW1lbnQuc2Nyb2xsVG9wO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uYWRkRXZlbnRMaXN0ZW5lcignZm9jdXMnLCB3aW5kb3cuc2Nyb2xsVG8oMCwgeVBvc2l0aW9uKSk7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS50b3AgPSB5UG9zaXRpb24gKyAncHgnO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zZXRBdHRyaWJ1dGUoJ3JlYWRvbmx5JywgJycpO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0udmFsdWUgPSB0aGlzLnRleHQ7XG5cbiAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LmFwcGVuZENoaWxkKHRoaXMuZmFrZUVsZW0pO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RlZFRleHQgPSAoMCwgX3NlbGVjdDIuZGVmYXVsdCkodGhpcy5mYWtlRWxlbSk7XG4gICAgICAgICAgICAgICAgdGhpcy5jb3B5VGV4dCgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdyZW1vdmVGYWtlJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiByZW1vdmVGYWtlKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLmZha2VIYW5kbGVyKSB7XG4gICAgICAgICAgICAgICAgICAgIGRvY3VtZW50LmJvZHkucmVtb3ZlRXZlbnRMaXN0ZW5lcignY2xpY2snLCB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2spO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyID0gbnVsbDtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5mYWtlRWxlbSkge1xuICAgICAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LnJlbW92ZUNoaWxkKHRoaXMuZmFrZUVsZW0pO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3NlbGVjdFRhcmdldCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gc2VsZWN0VGFyZ2V0KCkge1xuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gKDAsIF9zZWxlY3QyLmRlZmF1bHQpKHRoaXMudGFyZ2V0KTtcbiAgICAgICAgICAgICAgICB0aGlzLmNvcHlUZXh0KCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2NvcHlUZXh0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBjb3B5VGV4dCgpIHtcbiAgICAgICAgICAgICAgICB2YXIgc3VjY2VlZGVkID0gdm9pZCAwO1xuXG4gICAgICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICAgICAgc3VjY2VlZGVkID0gZG9jdW1lbnQuZXhlY0NvbW1hbmQodGhpcy5hY3Rpb24pO1xuICAgICAgICAgICAgICAgIH0gY2F0Y2ggKGVycikge1xuICAgICAgICAgICAgICAgICAgICBzdWNjZWVkZWQgPSBmYWxzZTtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB0aGlzLmhhbmRsZVJlc3VsdChzdWNjZWVkZWQpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdoYW5kbGVSZXN1bHQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGhhbmRsZVJlc3VsdChzdWNjZWVkZWQpIHtcbiAgICAgICAgICAgICAgICB0aGlzLmVtaXR0ZXIuZW1pdChzdWNjZWVkZWQgPyAnc3VjY2VzcycgOiAnZXJyb3InLCB7XG4gICAgICAgICAgICAgICAgICAgIGFjdGlvbjogdGhpcy5hY3Rpb24sXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6IHRoaXMuc2VsZWN0ZWRUZXh0LFxuICAgICAgICAgICAgICAgICAgICB0cmlnZ2VyOiB0aGlzLnRyaWdnZXIsXG4gICAgICAgICAgICAgICAgICAgIGNsZWFyU2VsZWN0aW9uOiB0aGlzLmNsZWFyU2VsZWN0aW9uLmJpbmQodGhpcylcbiAgICAgICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnY2xlYXJTZWxlY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGNsZWFyU2VsZWN0aW9uKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLnRhcmdldCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnRhcmdldC5ibHVyKCk7XG4gICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgd2luZG93LmdldFNlbGVjdGlvbigpLnJlbW92ZUFsbFJhbmdlcygpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZXN0cm95JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZXN0cm95KCkge1xuICAgICAgICAgICAgICAgIHRoaXMucmVtb3ZlRmFrZSgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdhY3Rpb24nLFxuICAgICAgICAgICAgc2V0OiBmdW5jdGlvbiBzZXQoKSB7XG4gICAgICAgICAgICAgICAgdmFyIGFjdGlvbiA9IGFyZ3VtZW50cy5sZW5ndGggPiAwICYmIGFyZ3VtZW50c1swXSAhPT0gdW5kZWZpbmVkID8gYXJndW1lbnRzWzBdIDogJ2NvcHknO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5fYWN0aW9uID0gYWN0aW9uO1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuX2FjdGlvbiAhPT0gJ2NvcHknICYmIHRoaXMuX2FjdGlvbiAhPT0gJ2N1dCcpIHtcbiAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwiYWN0aW9uXCIgdmFsdWUsIHVzZSBlaXRoZXIgXCJjb3B5XCIgb3IgXCJjdXRcIicpO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5fYWN0aW9uO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICd0YXJnZXQnLFxuICAgICAgICAgICAgc2V0OiBmdW5jdGlvbiBzZXQodGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgaWYgKHRhcmdldCAhPT0gdW5kZWZpbmVkKSB7XG4gICAgICAgICAgICAgICAgICAgIGlmICh0YXJnZXQgJiYgKHR5cGVvZiB0YXJnZXQgPT09ICd1bmRlZmluZWQnID8gJ3VuZGVmaW5lZCcgOiBfdHlwZW9mKHRhcmdldCkpID09PSAnb2JqZWN0JyAmJiB0YXJnZXQubm9kZVR5cGUgPT09IDEpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIGlmICh0aGlzLmFjdGlvbiA9PT0gJ2NvcHknICYmIHRhcmdldC5oYXNBdHRyaWJ1dGUoJ2Rpc2FibGVkJykpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJ0YXJnZXRcIiBhdHRyaWJ1dGUuIFBsZWFzZSB1c2UgXCJyZWFkb25seVwiIGluc3RlYWQgb2YgXCJkaXNhYmxlZFwiIGF0dHJpYnV0ZScpO1xuICAgICAgICAgICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgICAgICAgICBpZiAodGhpcy5hY3Rpb24gPT09ICdjdXQnICYmICh0YXJnZXQuaGFzQXR0cmlidXRlKCdyZWFkb25seScpIHx8IHRhcmdldC5oYXNBdHRyaWJ1dGUoJ2Rpc2FibGVkJykpKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwidGFyZ2V0XCIgYXR0cmlidXRlLiBZb3UgY2FuXFwndCBjdXQgdGV4dCBmcm9tIGVsZW1lbnRzIHdpdGggXCJyZWFkb25seVwiIG9yIFwiZGlzYWJsZWRcIiBhdHRyaWJ1dGVzJyk7XG4gICAgICAgICAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuX3RhcmdldCA9IHRhcmdldDtcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignSW52YWxpZCBcInRhcmdldFwiIHZhbHVlLCB1c2UgYSB2YWxpZCBFbGVtZW50Jyk7XG4gICAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMuX3RhcmdldDtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfV0pO1xuXG4gICAgICAgIHJldHVybiBDbGlwYm9hcmRBY3Rpb247XG4gICAgfSgpO1xuXG4gICAgbW9kdWxlLmV4cG9ydHMgPSBDbGlwYm9hcmRBY3Rpb247XG59KTsiLCIoZnVuY3Rpb24gKGdsb2JhbCwgZmFjdG9yeSkge1xuICAgIGlmICh0eXBlb2YgZGVmaW5lID09PSBcImZ1bmN0aW9uXCIgJiYgZGVmaW5lLmFtZCkge1xuICAgICAgICBkZWZpbmUoWydtb2R1bGUnLCAnLi9jbGlwYm9hcmQtYWN0aW9uJywgJ3RpbnktZW1pdHRlcicsICdnb29kLWxpc3RlbmVyJ10sIGZhY3RvcnkpO1xuICAgIH0gZWxzZSBpZiAodHlwZW9mIGV4cG9ydHMgIT09IFwidW5kZWZpbmVkXCIpIHtcbiAgICAgICAgZmFjdG9yeShtb2R1bGUsIHJlcXVpcmUoJy4vY2xpcGJvYXJkLWFjdGlvbicpLCByZXF1aXJlKCd0aW55LWVtaXR0ZXInKSwgcmVxdWlyZSgnZ29vZC1saXN0ZW5lcicpKTtcbiAgICB9IGVsc2Uge1xuICAgICAgICB2YXIgbW9kID0ge1xuICAgICAgICAgICAgZXhwb3J0czoge31cbiAgICAgICAgfTtcbiAgICAgICAgZmFjdG9yeShtb2QsIGdsb2JhbC5jbGlwYm9hcmRBY3Rpb24sIGdsb2JhbC50aW55RW1pdHRlciwgZ2xvYmFsLmdvb2RMaXN0ZW5lcik7XG4gICAgICAgIGdsb2JhbC5jbGlwYm9hcmQgPSBtb2QuZXhwb3J0cztcbiAgICB9XG59KSh0aGlzLCBmdW5jdGlvbiAobW9kdWxlLCBfY2xpcGJvYXJkQWN0aW9uLCBfdGlueUVtaXR0ZXIsIF9nb29kTGlzdGVuZXIpIHtcbiAgICAndXNlIHN0cmljdCc7XG5cbiAgICB2YXIgX2NsaXBib2FyZEFjdGlvbjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jbGlwYm9hcmRBY3Rpb24pO1xuXG4gICAgdmFyIF90aW55RW1pdHRlcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF90aW55RW1pdHRlcik7XG5cbiAgICB2YXIgX2dvb2RMaXN0ZW5lcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9nb29kTGlzdGVuZXIpO1xuXG4gICAgZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHtcbiAgICAgICAgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHtcbiAgICAgICAgICAgIGRlZmF1bHQ6IG9ialxuICAgICAgICB9O1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIF9jbGFzc0NhbGxDaGVjayhpbnN0YW5jZSwgQ29uc3RydWN0b3IpIHtcbiAgICAgICAgaWYgKCEoaW5zdGFuY2UgaW5zdGFuY2VvZiBDb25zdHJ1Y3RvcikpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoXCJDYW5ub3QgY2FsbCBhIGNsYXNzIGFzIGEgZnVuY3Rpb25cIik7XG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIgX2NyZWF0ZUNsYXNzID0gZnVuY3Rpb24gKCkge1xuICAgICAgICBmdW5jdGlvbiBkZWZpbmVQcm9wZXJ0aWVzKHRhcmdldCwgcHJvcHMpIHtcbiAgICAgICAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgcHJvcHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgICAgICAgICB2YXIgZGVzY3JpcHRvciA9IHByb3BzW2ldO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuZW51bWVyYWJsZSA9IGRlc2NyaXB0b3IuZW51bWVyYWJsZSB8fCBmYWxzZTtcbiAgICAgICAgICAgICAgICBkZXNjcmlwdG9yLmNvbmZpZ3VyYWJsZSA9IHRydWU7XG4gICAgICAgICAgICAgICAgaWYgKFwidmFsdWVcIiBpbiBkZXNjcmlwdG9yKSBkZXNjcmlwdG9yLndyaXRhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBPYmplY3QuZGVmaW5lUHJvcGVydHkodGFyZ2V0LCBkZXNjcmlwdG9yLmtleSwgZGVzY3JpcHRvcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gZnVuY3Rpb24gKENvbnN0cnVjdG9yLCBwcm90b1Byb3BzLCBzdGF0aWNQcm9wcykge1xuICAgICAgICAgICAgaWYgKHByb3RvUHJvcHMpIGRlZmluZVByb3BlcnRpZXMoQ29uc3RydWN0b3IucHJvdG90eXBlLCBwcm90b1Byb3BzKTtcbiAgICAgICAgICAgIGlmIChzdGF0aWNQcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvciwgc3RhdGljUHJvcHMpO1xuICAgICAgICAgICAgcmV0dXJuIENvbnN0cnVjdG9yO1xuICAgICAgICB9O1xuICAgIH0oKTtcblxuICAgIGZ1bmN0aW9uIF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHNlbGYsIGNhbGwpIHtcbiAgICAgICAgaWYgKCFzZWxmKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgUmVmZXJlbmNlRXJyb3IoXCJ0aGlzIGhhc24ndCBiZWVuIGluaXRpYWxpc2VkIC0gc3VwZXIoKSBoYXNuJ3QgYmVlbiBjYWxsZWRcIik7XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gY2FsbCAmJiAodHlwZW9mIGNhbGwgPT09IFwib2JqZWN0XCIgfHwgdHlwZW9mIGNhbGwgPT09IFwiZnVuY3Rpb25cIikgPyBjYWxsIDogc2VsZjtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBfaW5oZXJpdHMoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIHtcbiAgICAgICAgaWYgKHR5cGVvZiBzdXBlckNsYXNzICE9PSBcImZ1bmN0aW9uXCIgJiYgc3VwZXJDbGFzcyAhPT0gbnVsbCkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIlN1cGVyIGV4cHJlc3Npb24gbXVzdCBlaXRoZXIgYmUgbnVsbCBvciBhIGZ1bmN0aW9uLCBub3QgXCIgKyB0eXBlb2Ygc3VwZXJDbGFzcyk7XG4gICAgICAgIH1cblxuICAgICAgICBzdWJDbGFzcy5wcm90b3R5cGUgPSBPYmplY3QuY3JlYXRlKHN1cGVyQ2xhc3MgJiYgc3VwZXJDbGFzcy5wcm90b3R5cGUsIHtcbiAgICAgICAgICAgIGNvbnN0cnVjdG9yOiB7XG4gICAgICAgICAgICAgICAgdmFsdWU6IHN1YkNsYXNzLFxuICAgICAgICAgICAgICAgIGVudW1lcmFibGU6IGZhbHNlLFxuICAgICAgICAgICAgICAgIHdyaXRhYmxlOiB0cnVlLFxuICAgICAgICAgICAgICAgIGNvbmZpZ3VyYWJsZTogdHJ1ZVxuICAgICAgICAgICAgfVxuICAgICAgICB9KTtcbiAgICAgICAgaWYgKHN1cGVyQ2xhc3MpIE9iamVjdC5zZXRQcm90b3R5cGVPZiA/IE9iamVjdC5zZXRQcm90b3R5cGVPZihzdWJDbGFzcywgc3VwZXJDbGFzcykgOiBzdWJDbGFzcy5fX3Byb3RvX18gPSBzdXBlckNsYXNzO1xuICAgIH1cblxuICAgIHZhciBDbGlwYm9hcmQgPSBmdW5jdGlvbiAoX0VtaXR0ZXIpIHtcbiAgICAgICAgX2luaGVyaXRzKENsaXBib2FyZCwgX0VtaXR0ZXIpO1xuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBAcGFyYW0ge1N0cmluZ3xIVE1MRWxlbWVudHxIVE1MQ29sbGVjdGlvbnxOb2RlTGlzdH0gdHJpZ2dlclxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cbiAgICAgICAgZnVuY3Rpb24gQ2xpcGJvYXJkKHRyaWdnZXIsIG9wdGlvbnMpIHtcbiAgICAgICAgICAgIF9jbGFzc0NhbGxDaGVjayh0aGlzLCBDbGlwYm9hcmQpO1xuXG4gICAgICAgICAgICB2YXIgX3RoaXMgPSBfcG9zc2libGVDb25zdHJ1Y3RvclJldHVybih0aGlzLCAoQ2xpcGJvYXJkLl9fcHJvdG9fXyB8fCBPYmplY3QuZ2V0UHJvdG90eXBlT2YoQ2xpcGJvYXJkKSkuY2FsbCh0aGlzKSk7XG5cbiAgICAgICAgICAgIF90aGlzLnJlc29sdmVPcHRpb25zKG9wdGlvbnMpO1xuICAgICAgICAgICAgX3RoaXMubGlzdGVuQ2xpY2sodHJpZ2dlcik7XG4gICAgICAgICAgICByZXR1cm4gX3RoaXM7XG4gICAgICAgIH1cblxuICAgICAgICAvKipcbiAgICAgICAgICogRGVmaW5lcyBpZiBhdHRyaWJ1dGVzIHdvdWxkIGJlIHJlc29sdmVkIHVzaW5nIGludGVybmFsIHNldHRlciBmdW5jdGlvbnNcbiAgICAgICAgICogb3IgY3VzdG9tIGZ1bmN0aW9ucyB0aGF0IHdlcmUgcGFzc2VkIGluIHRoZSBjb25zdHJ1Y3Rvci5cbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG5cblxuICAgICAgICBfY3JlYXRlQ2xhc3MoQ2xpcGJvYXJkLCBbe1xuICAgICAgICAgICAga2V5OiAncmVzb2x2ZU9wdGlvbnMnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlc29sdmVPcHRpb25zKCkge1xuICAgICAgICAgICAgICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiB7fTtcblxuICAgICAgICAgICAgICAgIHRoaXMuYWN0aW9uID0gdHlwZW9mIG9wdGlvbnMuYWN0aW9uID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy5hY3Rpb24gOiB0aGlzLmRlZmF1bHRBY3Rpb247XG4gICAgICAgICAgICAgICAgdGhpcy50YXJnZXQgPSB0eXBlb2Ygb3B0aW9ucy50YXJnZXQgPT09ICdmdW5jdGlvbicgPyBvcHRpb25zLnRhcmdldCA6IHRoaXMuZGVmYXVsdFRhcmdldDtcbiAgICAgICAgICAgICAgICB0aGlzLnRleHQgPSB0eXBlb2Ygb3B0aW9ucy50ZXh0ID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy50ZXh0IDogdGhpcy5kZWZhdWx0VGV4dDtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnbGlzdGVuQ2xpY2snLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGxpc3RlbkNsaWNrKHRyaWdnZXIpIHtcbiAgICAgICAgICAgICAgICB2YXIgX3RoaXMyID0gdGhpcztcblxuICAgICAgICAgICAgICAgIHRoaXMubGlzdGVuZXIgPSAoMCwgX2dvb2RMaXN0ZW5lcjIuZGVmYXVsdCkodHJpZ2dlciwgJ2NsaWNrJywgZnVuY3Rpb24gKGUpIHtcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIF90aGlzMi5vbkNsaWNrKGUpO1xuICAgICAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdvbkNsaWNrJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBvbkNsaWNrKGUpIHtcbiAgICAgICAgICAgICAgICB2YXIgdHJpZ2dlciA9IGUuZGVsZWdhdGVUYXJnZXQgfHwgZS5jdXJyZW50VGFyZ2V0O1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuY2xpcGJvYXJkQWN0aW9uKSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB0aGlzLmNsaXBib2FyZEFjdGlvbiA9IG5ldyBfY2xpcGJvYXJkQWN0aW9uMi5kZWZhdWx0KHtcbiAgICAgICAgICAgICAgICAgICAgYWN0aW9uOiB0aGlzLmFjdGlvbih0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdGFyZ2V0OiB0aGlzLnRhcmdldCh0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogdGhpcy50ZXh0KHRyaWdnZXIpLFxuICAgICAgICAgICAgICAgICAgICB0cmlnZ2VyOiB0cmlnZ2VyLFxuICAgICAgICAgICAgICAgICAgICBlbWl0dGVyOiB0aGlzXG4gICAgICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRBY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRBY3Rpb24odHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHJldHVybiBnZXRBdHRyaWJ1dGVWYWx1ZSgnYWN0aW9uJywgdHJpZ2dlcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRUYXJnZXQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRUYXJnZXQodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHZhciBzZWxlY3RvciA9IGdldEF0dHJpYnV0ZVZhbHVlKCd0YXJnZXQnLCB0cmlnZ2VyKTtcblxuICAgICAgICAgICAgICAgIGlmIChzZWxlY3Rvcikge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gZG9jdW1lbnQucXVlcnlTZWxlY3RvcihzZWxlY3Rvcik7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZWZhdWx0VGV4dCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVmYXVsdFRleHQodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHJldHVybiBnZXRBdHRyaWJ1dGVWYWx1ZSgndGV4dCcsIHRyaWdnZXIpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZXN0cm95JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZXN0cm95KCkge1xuICAgICAgICAgICAgICAgIHRoaXMubGlzdGVuZXIuZGVzdHJveSgpO1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuY2xpcGJvYXJkQWN0aW9uKSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uLmRlc3Ryb3koKTtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24gPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfV0pO1xuXG4gICAgICAgIHJldHVybiBDbGlwYm9hcmQ7XG4gICAgfShfdGlueUVtaXR0ZXIyLmRlZmF1bHQpO1xuXG4gICAgLyoqXG4gICAgICogSGVscGVyIGZ1bmN0aW9uIHRvIHJldHJpZXZlIGF0dHJpYnV0ZSB2YWx1ZS5cbiAgICAgKiBAcGFyYW0ge1N0cmluZ30gc3VmZml4XG4gICAgICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gICAgICovXG4gICAgZnVuY3Rpb24gZ2V0QXR0cmlidXRlVmFsdWUoc3VmZml4LCBlbGVtZW50KSB7XG4gICAgICAgIHZhciBhdHRyaWJ1dGUgPSAnZGF0YS1jbGlwYm9hcmQtJyArIHN1ZmZpeDtcblxuICAgICAgICBpZiAoIWVsZW1lbnQuaGFzQXR0cmlidXRlKGF0dHJpYnV0ZSkpIHtcbiAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBlbGVtZW50LmdldEF0dHJpYnV0ZShhdHRyaWJ1dGUpO1xuICAgIH1cblxuICAgIG1vZHVsZS5leHBvcnRzID0gQ2xpcGJvYXJkO1xufSk7IiwiLyoqXG4gKiBBIHBvbHlmaWxsIGZvciBFbGVtZW50Lm1hdGNoZXMoKVxuICovXG5pZiAoRWxlbWVudCAmJiAhRWxlbWVudC5wcm90b3R5cGUubWF0Y2hlcykge1xuICAgIHZhciBwcm90byA9IEVsZW1lbnQucHJvdG90eXBlO1xuXG4gICAgcHJvdG8ubWF0Y2hlcyA9IHByb3RvLm1hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by5tb3pNYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ubXNNYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ub01hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by53ZWJraXRNYXRjaGVzU2VsZWN0b3I7XG59XG5cbi8qKlxuICogRmluZHMgdGhlIGNsb3Nlc3QgcGFyZW50IHRoYXQgbWF0Y2hlcyBhIHNlbGVjdG9yLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcmV0dXJuIHtGdW5jdGlvbn1cbiAqL1xuZnVuY3Rpb24gY2xvc2VzdCAoZWxlbWVudCwgc2VsZWN0b3IpIHtcbiAgICB3aGlsZSAoZWxlbWVudCAmJiBlbGVtZW50ICE9PSBkb2N1bWVudCkge1xuICAgICAgICBpZiAoZWxlbWVudC5tYXRjaGVzKHNlbGVjdG9yKSkgcmV0dXJuIGVsZW1lbnQ7XG4gICAgICAgIGVsZW1lbnQgPSBlbGVtZW50LnBhcmVudE5vZGU7XG4gICAgfVxufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGNsb3Nlc3Q7XG4iLCJ2YXIgY2xvc2VzdCA9IHJlcXVpcmUoJy4vY2xvc2VzdCcpO1xuXG4vKipcbiAqIERlbGVnYXRlcyBldmVudCB0byBhIHNlbGVjdG9yLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEBwYXJhbSB7Qm9vbGVhbn0gdXNlQ2FwdHVyZVxuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBkZWxlZ2F0ZShlbGVtZW50LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2ssIHVzZUNhcHR1cmUpIHtcbiAgICB2YXIgbGlzdGVuZXJGbiA9IGxpc3RlbmVyLmFwcGx5KHRoaXMsIGFyZ3VtZW50cyk7XG5cbiAgICBlbGVtZW50LmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgbGlzdGVuZXJGbiwgdXNlQ2FwdHVyZSk7XG5cbiAgICByZXR1cm4ge1xuICAgICAgICBkZXN0cm95OiBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIGVsZW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcih0eXBlLCBsaXN0ZW5lckZuLCB1c2VDYXB0dXJlKTtcbiAgICAgICAgfVxuICAgIH1cbn1cblxuLyoqXG4gKiBGaW5kcyBjbG9zZXN0IG1hdGNoIGFuZCBpbnZva2VzIGNhbGxiYWNrLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge0Z1bmN0aW9ufVxuICovXG5mdW5jdGlvbiBsaXN0ZW5lcihlbGVtZW50LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICByZXR1cm4gZnVuY3Rpb24oZSkge1xuICAgICAgICBlLmRlbGVnYXRlVGFyZ2V0ID0gY2xvc2VzdChlLnRhcmdldCwgc2VsZWN0b3IpO1xuXG4gICAgICAgIGlmIChlLmRlbGVnYXRlVGFyZ2V0KSB7XG4gICAgICAgICAgICBjYWxsYmFjay5jYWxsKGVsZW1lbnQsIGUpO1xuICAgICAgICB9XG4gICAgfVxufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGRlbGVnYXRlO1xuIiwiLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIEhUTUwgZWxlbWVudC5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gdmFsdWVcbiAqIEByZXR1cm4ge0Jvb2xlYW59XG4gKi9cbmV4cG9ydHMubm9kZSA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgcmV0dXJuIHZhbHVlICE9PSB1bmRlZmluZWRcbiAgICAgICAgJiYgdmFsdWUgaW5zdGFuY2VvZiBIVE1MRWxlbWVudFxuICAgICAgICAmJiB2YWx1ZS5ub2RlVHlwZSA9PT0gMTtcbn07XG5cbi8qKlxuICogQ2hlY2sgaWYgYXJndW1lbnQgaXMgYSBsaXN0IG9mIEhUTUwgZWxlbWVudHMuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLm5vZGVMaXN0ID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICB2YXIgdHlwZSA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbCh2YWx1ZSk7XG5cbiAgICByZXR1cm4gdmFsdWUgIT09IHVuZGVmaW5lZFxuICAgICAgICAmJiAodHlwZSA9PT0gJ1tvYmplY3QgTm9kZUxpc3RdJyB8fCB0eXBlID09PSAnW29iamVjdCBIVE1MQ29sbGVjdGlvbl0nKVxuICAgICAgICAmJiAoJ2xlbmd0aCcgaW4gdmFsdWUpXG4gICAgICAgICYmICh2YWx1ZS5sZW5ndGggPT09IDAgfHwgZXhwb3J0cy5ub2RlKHZhbHVlWzBdKSk7XG59O1xuXG4vKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgc3RyaW5nLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5zdHJpbmcgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHJldHVybiB0eXBlb2YgdmFsdWUgPT09ICdzdHJpbmcnXG4gICAgICAgIHx8IHZhbHVlIGluc3RhbmNlb2YgU3RyaW5nO1xufTtcblxuLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5mbiA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgdmFyIHR5cGUgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nLmNhbGwodmFsdWUpO1xuXG4gICAgcmV0dXJuIHR5cGUgPT09ICdbb2JqZWN0IEZ1bmN0aW9uXSc7XG59O1xuIiwidmFyIGlzID0gcmVxdWlyZSgnLi9pcycpO1xudmFyIGRlbGVnYXRlID0gcmVxdWlyZSgnZGVsZWdhdGUnKTtcblxuLyoqXG4gKiBWYWxpZGF0ZXMgYWxsIHBhcmFtcyBhbmQgY2FsbHMgdGhlIHJpZ2h0XG4gKiBsaXN0ZW5lciBmdW5jdGlvbiBiYXNlZCBvbiBpdHMgdGFyZ2V0IHR5cGUuXG4gKlxuICogQHBhcmFtIHtTdHJpbmd8SFRNTEVsZW1lbnR8SFRNTENvbGxlY3Rpb258Tm9kZUxpc3R9IHRhcmdldFxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3Rlbih0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgaWYgKCF0YXJnZXQgJiYgIXR5cGUgJiYgIWNhbGxiYWNrKSB7XG4gICAgICAgIHRocm93IG5ldyBFcnJvcignTWlzc2luZyByZXF1aXJlZCBhcmd1bWVudHMnKTtcbiAgICB9XG5cbiAgICBpZiAoIWlzLnN0cmluZyh0eXBlKSkge1xuICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdTZWNvbmQgYXJndW1lbnQgbXVzdCBiZSBhIFN0cmluZycpO1xuICAgIH1cblxuICAgIGlmICghaXMuZm4oY2FsbGJhY2spKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ1RoaXJkIGFyZ3VtZW50IG11c3QgYmUgYSBGdW5jdGlvbicpO1xuICAgIH1cblxuICAgIGlmIChpcy5ub2RlKHRhcmdldCkpIHtcbiAgICAgICAgcmV0dXJuIGxpc3Rlbk5vZGUodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2UgaWYgKGlzLm5vZGVMaXN0KHRhcmdldCkpIHtcbiAgICAgICAgcmV0dXJuIGxpc3Rlbk5vZGVMaXN0KHRhcmdldCwgdHlwZSwgY2FsbGJhY2spO1xuICAgIH1cbiAgICBlbHNlIGlmIChpcy5zdHJpbmcodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuU2VsZWN0b3IodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2Uge1xuICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdGaXJzdCBhcmd1bWVudCBtdXN0IGJlIGEgU3RyaW5nLCBIVE1MRWxlbWVudCwgSFRNTENvbGxlY3Rpb24sIG9yIE5vZGVMaXN0Jyk7XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZHMgYW4gZXZlbnQgbGlzdGVuZXIgdG8gYSBIVE1MIGVsZW1lbnRcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7SFRNTEVsZW1lbnR9IG5vZGVcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5Ob2RlKG5vZGUsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgbm9kZS5hZGRFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcblxuICAgIHJldHVybiB7XG4gICAgICAgIGRlc3Ryb3k6IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgbm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcbiAgICAgICAgfVxuICAgIH1cbn1cblxuLyoqXG4gKiBBZGQgYW4gZXZlbnQgbGlzdGVuZXIgdG8gYSBsaXN0IG9mIEhUTUwgZWxlbWVudHNcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7Tm9kZUxpc3R8SFRNTENvbGxlY3Rpb259IG5vZGVMaXN0XG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuTm9kZUxpc3Qobm9kZUxpc3QsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgQXJyYXkucHJvdG90eXBlLmZvckVhY2guY2FsbChub2RlTGlzdCwgZnVuY3Rpb24obm9kZSkge1xuICAgICAgICBub2RlLmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgIH0pO1xuXG4gICAgcmV0dXJuIHtcbiAgICAgICAgZGVzdHJveTogZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICBBcnJheS5wcm90b3R5cGUuZm9yRWFjaC5jYWxsKG5vZGVMaXN0LCBmdW5jdGlvbihub2RlKSB7XG4gICAgICAgICAgICAgICAgbm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcbiAgICAgICAgICAgIH0pO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZCBhbiBldmVudCBsaXN0ZW5lciB0byBhIHNlbGVjdG9yXG4gKiBhbmQgcmV0dXJucyBhIHJlbW92ZSBsaXN0ZW5lciBmdW5jdGlvbi5cbiAqXG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5TZWxlY3RvcihzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICByZXR1cm4gZGVsZWdhdGUoZG9jdW1lbnQuYm9keSwgc2VsZWN0b3IsIHR5cGUsIGNhbGxiYWNrKTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBsaXN0ZW47XG4iLCIvKiEgbmdjbGlwYm9hcmQgLSB2MS4xLjEgLSAyMDE2LTAyLTI2XHJcbiogaHR0cHM6Ly9naXRodWIuY29tL3NhY2hpbmNob29sdXIvbmdjbGlwYm9hcmRcclxuKiBDb3B5cmlnaHQgKGMpIDIwMTYgU2FjaGluOyBMaWNlbnNlZCBNSVQgKi9cclxuKGZ1bmN0aW9uKCkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgdmFyIE1PRFVMRV9OQU1FID0gJ25nY2xpcGJvYXJkJztcclxuICAgIHZhciBhbmd1bGFyLCBDbGlwYm9hcmQ7XHJcbiAgICBcclxuICAgIC8vIENoZWNrIGZvciBDb21tb25KUyBzdXBwb3J0XHJcbiAgICBpZiAodHlwZW9mIG1vZHVsZSA9PT0gJ29iamVjdCcgJiYgbW9kdWxlLmV4cG9ydHMpIHtcclxuICAgICAgYW5ndWxhciA9IHJlcXVpcmUoJ2FuZ3VsYXInKTtcclxuICAgICAgQ2xpcGJvYXJkID0gcmVxdWlyZSgnY2xpcGJvYXJkJyk7XHJcbiAgICAgIG1vZHVsZS5leHBvcnRzID0gTU9EVUxFX05BTUU7XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICBhbmd1bGFyID0gd2luZG93LmFuZ3VsYXI7XHJcbiAgICAgIENsaXBib2FyZCA9IHdpbmRvdy5DbGlwYm9hcmQ7XHJcbiAgICB9XHJcblxyXG4gICAgYW5ndWxhci5tb2R1bGUoTU9EVUxFX05BTUUsIFtdKS5kaXJlY3RpdmUoJ25nY2xpcGJvYXJkJywgZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgcmVzdHJpY3Q6ICdBJyxcclxuICAgICAgICAgICAgc2NvcGU6IHtcclxuICAgICAgICAgICAgICAgIG5nY2xpcGJvYXJkU3VjY2VzczogJyYnLFxyXG4gICAgICAgICAgICAgICAgbmdjbGlwYm9hcmRFcnJvcjogJyYnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGxpbms6IGZ1bmN0aW9uKHNjb3BlLCBlbGVtZW50KSB7XHJcbiAgICAgICAgICAgICAgICB2YXIgY2xpcGJvYXJkID0gbmV3IENsaXBib2FyZChlbGVtZW50WzBdKTtcclxuXHJcbiAgICAgICAgICAgICAgICBjbGlwYm9hcmQub24oJ3N1Y2Nlc3MnLCBmdW5jdGlvbihlKSB7XHJcbiAgICAgICAgICAgICAgICAgIHNjb3BlLiRhcHBseShmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2NvcGUubmdjbGlwYm9hcmRTdWNjZXNzKHtcclxuICAgICAgICAgICAgICAgICAgICAgIGU6IGVcclxuICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICBjbGlwYm9hcmQub24oJ2Vycm9yJywgZnVuY3Rpb24oZSkge1xyXG4gICAgICAgICAgICAgICAgICBzY29wZS4kYXBwbHkoZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNjb3BlLm5nY2xpcGJvYXJkRXJyb3Ioe1xyXG4gICAgICAgICAgICAgICAgICAgICAgZTogZVxyXG4gICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9KTtcclxufSgpKTtcclxuIiwiZnVuY3Rpb24gc2VsZWN0KGVsZW1lbnQpIHtcbiAgICB2YXIgc2VsZWN0ZWRUZXh0O1xuXG4gICAgaWYgKGVsZW1lbnQubm9kZU5hbWUgPT09ICdTRUxFQ1QnKSB7XG4gICAgICAgIGVsZW1lbnQuZm9jdXMoKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBlbGVtZW50LnZhbHVlO1xuICAgIH1cbiAgICBlbHNlIGlmIChlbGVtZW50Lm5vZGVOYW1lID09PSAnSU5QVVQnIHx8IGVsZW1lbnQubm9kZU5hbWUgPT09ICdURVhUQVJFQScpIHtcbiAgICAgICAgZWxlbWVudC5mb2N1cygpO1xuICAgICAgICBlbGVtZW50LnNldFNlbGVjdGlvblJhbmdlKDAsIGVsZW1lbnQudmFsdWUubGVuZ3RoKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBlbGVtZW50LnZhbHVlO1xuICAgIH1cbiAgICBlbHNlIHtcbiAgICAgICAgaWYgKGVsZW1lbnQuaGFzQXR0cmlidXRlKCdjb250ZW50ZWRpdGFibGUnKSkge1xuICAgICAgICAgICAgZWxlbWVudC5mb2N1cygpO1xuICAgICAgICB9XG5cbiAgICAgICAgdmFyIHNlbGVjdGlvbiA9IHdpbmRvdy5nZXRTZWxlY3Rpb24oKTtcbiAgICAgICAgdmFyIHJhbmdlID0gZG9jdW1lbnQuY3JlYXRlUmFuZ2UoKTtcblxuICAgICAgICByYW5nZS5zZWxlY3ROb2RlQ29udGVudHMoZWxlbWVudCk7XG4gICAgICAgIHNlbGVjdGlvbi5yZW1vdmVBbGxSYW5nZXMoKTtcbiAgICAgICAgc2VsZWN0aW9uLmFkZFJhbmdlKHJhbmdlKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBzZWxlY3Rpb24udG9TdHJpbmcoKTtcbiAgICB9XG5cbiAgICByZXR1cm4gc2VsZWN0ZWRUZXh0O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IHNlbGVjdDtcbiIsImZ1bmN0aW9uIEUgKCkge1xuICAvLyBLZWVwIHRoaXMgZW1wdHkgc28gaXQncyBlYXNpZXIgdG8gaW5oZXJpdCBmcm9tXG4gIC8vICh2aWEgaHR0cHM6Ly9naXRodWIuY29tL2xpcHNtYWNrIGZyb20gaHR0cHM6Ly9naXRodWIuY29tL3Njb3R0Y29yZ2FuL3RpbnktZW1pdHRlci9pc3N1ZXMvMylcbn1cblxuRS5wcm90b3R5cGUgPSB7XG4gIG9uOiBmdW5jdGlvbiAobmFtZSwgY2FsbGJhY2ssIGN0eCkge1xuICAgIHZhciBlID0gdGhpcy5lIHx8ICh0aGlzLmUgPSB7fSk7XG5cbiAgICAoZVtuYW1lXSB8fCAoZVtuYW1lXSA9IFtdKSkucHVzaCh7XG4gICAgICBmbjogY2FsbGJhY2ssXG4gICAgICBjdHg6IGN0eFxuICAgIH0pO1xuXG4gICAgcmV0dXJuIHRoaXM7XG4gIH0sXG5cbiAgb25jZTogZnVuY3Rpb24gKG5hbWUsIGNhbGxiYWNrLCBjdHgpIHtcbiAgICB2YXIgc2VsZiA9IHRoaXM7XG4gICAgZnVuY3Rpb24gbGlzdGVuZXIgKCkge1xuICAgICAgc2VsZi5vZmYobmFtZSwgbGlzdGVuZXIpO1xuICAgICAgY2FsbGJhY2suYXBwbHkoY3R4LCBhcmd1bWVudHMpO1xuICAgIH07XG5cbiAgICBsaXN0ZW5lci5fID0gY2FsbGJhY2tcbiAgICByZXR1cm4gdGhpcy5vbihuYW1lLCBsaXN0ZW5lciwgY3R4KTtcbiAgfSxcblxuICBlbWl0OiBmdW5jdGlvbiAobmFtZSkge1xuICAgIHZhciBkYXRhID0gW10uc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpO1xuICAgIHZhciBldnRBcnIgPSAoKHRoaXMuZSB8fCAodGhpcy5lID0ge30pKVtuYW1lXSB8fCBbXSkuc2xpY2UoKTtcbiAgICB2YXIgaSA9IDA7XG4gICAgdmFyIGxlbiA9IGV2dEFyci5sZW5ndGg7XG5cbiAgICBmb3IgKGk7IGkgPCBsZW47IGkrKykge1xuICAgICAgZXZ0QXJyW2ldLmZuLmFwcGx5KGV2dEFycltpXS5jdHgsIGRhdGEpO1xuICAgIH1cblxuICAgIHJldHVybiB0aGlzO1xuICB9LFxuXG4gIG9mZjogZnVuY3Rpb24gKG5hbWUsIGNhbGxiYWNrKSB7XG4gICAgdmFyIGUgPSB0aGlzLmUgfHwgKHRoaXMuZSA9IHt9KTtcbiAgICB2YXIgZXZ0cyA9IGVbbmFtZV07XG4gICAgdmFyIGxpdmVFdmVudHMgPSBbXTtcblxuICAgIGlmIChldnRzICYmIGNhbGxiYWNrKSB7XG4gICAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gZXZ0cy5sZW5ndGg7IGkgPCBsZW47IGkrKykge1xuICAgICAgICBpZiAoZXZ0c1tpXS5mbiAhPT0gY2FsbGJhY2sgJiYgZXZ0c1tpXS5mbi5fICE9PSBjYWxsYmFjaylcbiAgICAgICAgICBsaXZlRXZlbnRzLnB1c2goZXZ0c1tpXSk7XG4gICAgICB9XG4gICAgfVxuXG4gICAgLy8gUmVtb3ZlIGV2ZW50IGZyb20gcXVldWUgdG8gcHJldmVudCBtZW1vcnkgbGVha1xuICAgIC8vIFN1Z2dlc3RlZCBieSBodHRwczovL2dpdGh1Yi5jb20vbGF6ZFxuICAgIC8vIFJlZjogaHR0cHM6Ly9naXRodWIuY29tL3Njb3R0Y29yZ2FuL3RpbnktZW1pdHRlci9jb21taXQvYzZlYmZhYTliYzk3M2IzM2QxMTBhODRhMzA3NzQyYjdjZjk0Yzk1MyNjb21taXRjb21tZW50LTUwMjQ5MTBcblxuICAgIChsaXZlRXZlbnRzLmxlbmd0aClcbiAgICAgID8gZVtuYW1lXSA9IGxpdmVFdmVudHNcbiAgICAgIDogZGVsZXRlIGVbbmFtZV07XG5cbiAgICByZXR1cm4gdGhpcztcbiAgfVxufTtcblxubW9kdWxlLmV4cG9ydHMgPSBFO1xuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8yMC8yMDE1LlxyXG4gKiBURFNNIGlzIGEgZ2xvYmFsIG9iamVjdCB0aGF0IGNvbWVzIGZyb20gQXBwLmpzXHJcbiAqXHJcbiAqIFRoZSBmb2xsb3dpbmcgaGVscGVyIHdvcmtzIGluIGEgd2F5IHRvIG1ha2UgYXZhaWxhYmxlIHRoZSBjcmVhdGlvbiBvZiBEaXJlY3RpdmUsIFNlcnZpY2VzIGFuZCBDb250cm9sbGVyXHJcbiAqIG9uIGZseSBvciB3aGVuIGRlcGxveWluZyB0aGUgYXBwLlxyXG4gKlxyXG4gKiBXZSByZWR1Y2UgdGhlIHVzZSBvZiBjb21waWxlIGFuZCBleHRyYSBzdGVwc1xyXG4gKi9cclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4vQXBwLmpzJyk7XHJcblxyXG4vKipcclxuICogTGlzdGVuIHRvIGFuIGV4aXN0aW5nIGRpZ2VzdCBvZiB0aGUgY29tcGlsZSBwcm92aWRlciBhbmQgZXhlY3V0ZSB0aGUgJGFwcGx5IGltbWVkaWF0ZWx5IG9yIGFmdGVyIGl0J3MgcmVhZHlcclxuICogQHBhcmFtIGN1cnJlbnRcclxuICogQHBhcmFtIGZuXHJcbiAqL1xyXG5URFNUTS5zYWZlQXBwbHkgPSBmdW5jdGlvbiAoY3VycmVudCwgZm4pIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciBwaGFzZSA9IGN1cnJlbnQuJHJvb3QuJCRwaGFzZTtcclxuICAgIGlmIChwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRldmFsKGZuKTtcclxuICAgICAgICB9XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRhcHBseShmbik7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgY3VycmVudC4kYXBwbHkoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBkaXJlY3RpdmUgYXN5bmMgaWYgdGhlIGNvbXBpbGVQcm92aWRlciBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIuZGlyZWN0aXZlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5kaXJlY3RpdmUpIHtcclxuICAgICAgICBURFNUTS5kaXJlY3RpdmUoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBjb250cm9sbGVycyBhc3luYyBpZiB0aGUgY29udHJvbGxlclByb3ZpZGVyIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlQ29udHJvbGxlciA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbnRyb2xsZXJQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXJQcm92aWRlci5yZWdpc3RlcihzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uY29udHJvbGxlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXIoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBzZXJ2aWNlIGFzeW5jIGlmIHRoZSBwcm92aWRlU2VydmljZSBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZVNlcnZpY2UgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZSkge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZS5zZXJ2aWNlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5jb250cm9sbGVyKSB7XHJcbiAgICAgICAgVERTVE0uc2VydmljZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBGb3IgTGVnYWN5IHN5c3RlbSwgd2hhdCBpcyBkb2VzIGlzIHRvIHRha2UgcGFyYW1zIGZyb20gdGhlIHF1ZXJ5XHJcbiAqIG91dHNpZGUgdGhlIEFuZ3VsYXJKUyB1aS1yb3V0aW5nLlxyXG4gKiBAcGFyYW0gcGFyYW0gLy8gUGFyYW0gdG8gc2VhcmMgZm9yIC9leGFtcGxlLmh0bWw/YmFyPWZvbyNjdXJyZW50U3RhdGVcclxuICovXHJcblREU1RNLmdldFVSTFBhcmFtID0gZnVuY3Rpb24gKHBhcmFtKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICAkLnVybFBhcmFtID0gZnVuY3Rpb24gKG5hbWUpIHtcclxuICAgICAgICB2YXIgcmVzdWx0cyA9IG5ldyBSZWdFeHAoJ1tcXD8mXScgKyBuYW1lICsgJz0oW14mI10qKScpLmV4ZWMod2luZG93LmxvY2F0aW9uLmhyZWYpO1xyXG4gICAgICAgIGlmIChyZXN1bHRzID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgIHJldHVybiBudWxsO1xyXG4gICAgICAgIH1cclxuICAgICAgICBlbHNlIHtcclxuICAgICAgICAgICAgcmV0dXJuIHJlc3VsdHNbMV0gfHwgMDtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG5cclxuICAgIHJldHVybiAkLnVybFBhcmFtKHBhcmFtKTtcclxufTtcclxuXHJcbi8qKlxyXG4gKiBUaGlzIGNvZGUgd2FzIGludHJvZHVjZWQgb25seSBmb3IgdGhlIGlmcmFtZSBtaWdyYXRpb25cclxuICogaXQgZGV0ZWN0IHdoZW4gbW91c2UgZW50ZXJcclxuICovXHJcblREU1RNLmlmcmFtZUxvYWRlciA9IGZ1bmN0aW9uICgpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgICQoJy5pZnJhbWVMb2FkZXInKS5ob3ZlcihcclxuICAgICAgICBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICQoJy5uYXZiYXItdWwtY29udGFpbmVyIC5kcm9wZG93bi5vcGVuJykucmVtb3ZlQ2xhc3MoJ29wZW4nKTtcclxuICAgICAgICB9LCBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgfVxyXG4gICAgKTtcclxufTtcclxuXHJcblxyXG4vLyBJdCB3aWxsIGJlIHJlbW92ZWQgYWZ0ZXIgd2UgcmlwIG9mZiBhbGwgaWZyYW1lc1xyXG53aW5kb3cuVERTVE0gPSBURFNUTTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzE2LzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxucmVxdWlyZSgnYW5ndWxhcicpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWFuaW1hdGUnKTtcclxucmVxdWlyZSgnYW5ndWxhci1tb2NrcycpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXNhbml0aXplJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItcmVzb3VyY2UnKTtcclxucmVxdWlyZSgnYW5ndWxhci10cmFuc2xhdGUnKTtcclxucmVxdWlyZSgnYW5ndWxhci10cmFuc2xhdGUtbG9hZGVyLXBhcnRpYWwnKTtcclxucmVxdWlyZSgnYW5ndWxhci11aS1ib290c3RyYXAnKTtcclxucmVxdWlyZSgnbmdDbGlwYm9hcmQnKTtcclxucmVxdWlyZSgndWktcm91dGVyJyk7XHJcbnJlcXVpcmUoJ3J4LWFuZ3VsYXInKTtcclxuXHJcbi8vIE1vZHVsZXNcclxuaW1wb3J0IEhUVFBNb2R1bGUgZnJvbSAnLi4vc2VydmljZXMvaHR0cC9IVFRQTW9kdWxlLmpzJztcclxuaW1wb3J0IFJlc3RBUElNb2R1bGUgZnJvbSAnLi4vc2VydmljZXMvUmVzdEFQSS9SZXN0QVBJTW9kdWxlLmpzJ1xyXG5pbXBvcnQgSGVhZGVyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvaGVhZGVyL0hlYWRlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBMaWNlbnNlQWRtaW5Nb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9saWNlbnNlQWRtaW4vTGljZW5zZUFkbWluTW9kdWxlLmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvTGljZW5zZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTm90aWNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL25vdGljZU1hbmFnZXIvTm90aWNlTWFuYWdlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL3Rhc2tNYW5hZ2VyL1Rhc2tNYW5hZ2VyTW9kdWxlLmpzJztcclxuXHJcbnZhciBQcm92aWRlckNvcmUgPSB7fTtcclxuXHJcbnZhciBURFNUTSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTScsIFtcclxuICAgICduZ1Nhbml0aXplJyxcclxuICAgICduZ1Jlc291cmNlJyxcclxuICAgICduZ0FuaW1hdGUnLFxyXG4gICAgJ3Bhc2NhbHByZWNodC50cmFuc2xhdGUnLCAvLyAnYW5ndWxhci10cmFuc2xhdGUnXHJcbiAgICAndWkucm91dGVyJyxcclxuICAgICduZ2NsaXBib2FyZCcsXHJcbiAgICAna2VuZG8uZGlyZWN0aXZlcycsXHJcbiAgICAncngnLFxyXG4gICAgJ3VpLmJvb3RzdHJhcCcsXHJcbiAgICBIVFRQTW9kdWxlLm5hbWUsXHJcbiAgICBSZXN0QVBJTW9kdWxlLm5hbWUsXHJcbiAgICBIZWFkZXJNb2R1bGUubmFtZSxcclxuICAgIFRhc2tNYW5hZ2VyTW9kdWxlLm5hbWUsXHJcbiAgICBMaWNlbnNlQWRtaW5Nb2R1bGUubmFtZSxcclxuICAgIExpY2Vuc2VNYW5hZ2VyTW9kdWxlLm5hbWUsXHJcbiAgICBOb3RpY2VNYW5hZ2VyTW9kdWxlLm5hbWVcclxuXSkuY29uZmlnKFtcclxuICAgICckbG9nUHJvdmlkZXInLFxyXG4gICAgJyRyb290U2NvcGVQcm92aWRlcicsXHJcbiAgICAnJGNvbXBpbGVQcm92aWRlcicsXHJcbiAgICAnJGNvbnRyb2xsZXJQcm92aWRlcicsXHJcbiAgICAnJHByb3ZpZGUnLFxyXG4gICAgJyRodHRwUHJvdmlkZXInLFxyXG4gICAgJyR0cmFuc2xhdGVQcm92aWRlcicsXHJcbiAgICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICAnJHVybFJvdXRlclByb3ZpZGVyJyxcclxuICAgICckbG9jYXRpb25Qcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJGxvZ1Byb3ZpZGVyLCAkcm9vdFNjb3BlUHJvdmlkZXIsICRjb21waWxlUHJvdmlkZXIsICRjb250cm9sbGVyUHJvdmlkZXIsICRwcm92aWRlLCAkaHR0cFByb3ZpZGVyLFxyXG4gICAgICAgICAgICAgICR0cmFuc2xhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlciwgJHVybFJvdXRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgICAgICRyb290U2NvcGVQcm92aWRlci5kaWdlc3RUdGwoMzApO1xyXG5cclxuICAgICAgICAkbG9nUHJvdmlkZXIuZGVidWdFbmFibGVkKHRydWUpO1xyXG5cclxuICAgICAgICAvLyBBZnRlciBib290c3RyYXBwaW5nIGFuZ3VsYXIgZm9yZ2V0IHRoZSBwcm92aWRlciBzaW5jZSBldmVyeXRoaW5nIFwid2FzIGFscmVhZHkgbG9hZGVkXCJcclxuICAgICAgICBQcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyID0gJGNvbXBpbGVQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUuY29udHJvbGxlclByb3ZpZGVyID0gJGNvbnRyb2xsZXJQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2UgPSAkcHJvdmlkZTtcclxuICAgICAgICBQcm92aWRlckNvcmUuaHR0cFByb3ZpZGVyID0gJGh0dHBQcm92aWRlcjtcclxuXHJcbiAgICAgICAgLyoqXHJcbiAgICAgICAgICogVHJhbnNsYXRpb25zXHJcbiAgICAgICAgICovXHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci51c2VTYW5pdGl6ZVZhbHVlU3RyYXRlZ3kobnVsbCk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgndGRzdG0nKTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnVzZUxvYWRlcignJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXInLCB7XHJcbiAgICAgICAgICAgIHVybFRlbXBsYXRlOiAnLi4vaTE4bi97cGFydH0vYXBwLmkxOG4te2xhbmd9Lmpzb24nXHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci5wcmVmZXJyZWRMYW5ndWFnZSgnZW5fVVMnKTtcclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIuZmFsbGJhY2tMYW5ndWFnZSgnZW5fVVMnKTtcclxuXHJcbiAgICAgICAgLy8kdXJsUm91dGVyUHJvdmlkZXIub3RoZXJ3aXNlKCdkYXNoYm9hcmQnKTtcclxuXHJcbiAgICB9XSkuXHJcbiAgICBydW4oWyckcm9vdFNjb3BlJywgJyRodHRwJywgJyRsb2cnLCAnJGxvY2F0aW9uJywgZnVuY3Rpb24gKCRyb290U2NvcGUsICRodHRwLCAkbG9nLCAkbG9jYXRpb24sICRzdGF0ZSwgJHN0YXRlUGFyYW1zLCAkbG9jYWxlKSB7XHJcbiAgICAgICAgJGxvZy5kZWJ1ZygnQ29uZmlndXJhdGlvbiBkZXBsb3llZCcpO1xyXG5cclxuICAgICAgICAkcm9vdFNjb3BlLiRvbignJHN0YXRlQ2hhbmdlU3RhcnQnLCBmdW5jdGlvbiAoZXZlbnQsIHRvU3RhdGUsIHRvUGFyYW1zLCBmcm9tU3RhdGUsIGZyb21QYXJhbXMpIHtcclxuICAgICAgICAgICAgJGxvZy5kZWJ1ZygnU3RhdGUgQ2hhbmdlIHRvICcgKyB0b1N0YXRlLm5hbWUpO1xyXG4gICAgICAgICAgICBpZiAodG9TdGF0ZS5kYXRhICYmIHRvU3RhdGUuZGF0YS5wYWdlKSB7XHJcbiAgICAgICAgICAgICAgICB3aW5kb3cuZG9jdW1lbnQudGl0bGUgPSB0b1N0YXRlLmRhdGEucGFnZS50aXRsZTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgIH1dKTtcclxuXHJcbi8vIHdlIG1hcHBlZCB0aGUgUHJvdmlkZXIgQ29yZSBsaXN0IChjb21waWxlUHJvdmlkZXIsIGNvbnRyb2xsZXJQcm92aWRlciwgcHJvdmlkZVNlcnZpY2UsIGh0dHBQcm92aWRlcikgdG8gcmV1c2UgYWZ0ZXIgb24gZmx5XHJcblREU1RNLlByb3ZpZGVyQ29yZSA9IFByb3ZpZGVyQ29yZTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxLzE3LzE1LlxyXG4gKiBXaGVuIEFuZ3VsYXIgaW52b2tlcyB0aGUgbGluayBmdW5jdGlvbiwgaXQgaXMgbm8gbG9uZ2VyIGluIHRoZSBjb250ZXh0IG9mIHRoZSBjbGFzcyBpbnN0YW5jZSwgYW5kIHRoZXJlZm9yZSBhbGwgdGhpcyB3aWxsIGJlIHVuZGVmaW5lZFxyXG4gKiBUaGlzIHN0cnVjdHVyZSB3aWxsIGF2b2lkIHRob3NlIGlzc3Vlcy5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCdcclxuXHJcbi8vIENvbnRyb2xsZXIgaXMgYmVpbmcgdXNlZCB0byBJbmplY3QgYWxsIERlcGVuZGVuY2llc1xyXG5jbGFzcyBTVkdMb2FkZXJDb250cm9sbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2cpIHtcclxuICAgICAgICB0aGlzLiRsb2cgPSAkbG9nO1xyXG4gICAgfVxyXG59XHJcblxyXG5TVkdMb2FkZXJDb250cm9sbGVyLiRpbmplY3QgPSBbJyRsb2cnXTtcclxuXHJcbi8vIERpcmVjdGl2ZVxyXG5jbGFzcyBTVkdMb2FkZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCl7XHJcbiAgICAgICAgdGhpcy5yZXN0cmljdCA9ICdFJztcclxuICAgICAgICB0aGlzLmNvbnRyb2xsZXIgPSBTVkdMb2FkZXJDb250cm9sbGVyO1xyXG4gICAgICAgIHRoaXMuc2NvcGUgPSB7XHJcbiAgICAgICAgICAgIHN2Z0RhdGE6ICc9J1xyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgbGluayhzY29wZSwgZWxlbWVudCwgYXR0cnMsIGN0cmwpIHtcclxuICAgICAgICBlbGVtZW50Lmh0bWwoc2NvcGUuc3ZnRGF0YSk7XHJcbiAgICB9XHJcblxyXG4gICAgc3RhdGljIGRpcmVjdGl2ZSgpIHtcclxuICAgICAgICByZXR1cm4gbmV3IFNWR0xvYWRlcigpO1xyXG4gICAgfVxyXG59XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgU1ZHTG9hZGVyLmRpcmVjdGl2ZTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAzMC8xMC8yMDE2LlxyXG4gKiBMaXN0ZW4gdG8gTW9kYWwgV2luZG93IHRvIG1ha2UgYW55IG1vZGFsIHdpbmRvdyBkcmFnZ2FiYmxlXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG52YXIgVERTVE0gPSByZXF1aXJlKCcuLi8uLi9jb25maWcvQXBwLmpzJyk7XHJcblxyXG5URFNUTS5jcmVhdGVEaXJlY3RpdmUoJ21vZGFsUmVuZGVyJywgWyckbG9nJywgZnVuY3Rpb24gKCRsb2cpIHtcclxuICAgICRsb2cuZGVidWcoJ01vZGFsV2luZG93QWN0aXZhdGlvbiBsb2FkZWQnKTtcclxuICAgIHJldHVybiB7XHJcbiAgICAgICAgcmVzdHJpY3Q6ICdFQScsXHJcbiAgICAgICAgbGluazogZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgICAgICQoJy5tb2RhbC1kaWFsb2cnKS5kcmFnZ2FibGUoe1xyXG4gICAgICAgICAgICAgICAgaGFuZGxlOiAnLm1vZGFsLWhlYWRlcidcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfTtcclxufV0pOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTQvMjAxNS5cclxuICogUHJpbnRzIG91dCBhbGwgVG9hc3QgbWVzc2FnZSB3aGVuIGRldGVjdGVkIGZyb20gc2VydmVyIG9yIGN1c3RvbSBtc2cgdXNpbmcgdGhlIGRpcmVjdGl2ZSBpdHNlbGZcclxuICpcclxuICogUHJvYmFibHkgdmFsdWVzIGFyZTpcclxuICpcclxuICogc3VjY2VzcywgZGFuZ2VyLCBpbmZvLCB3YXJuaW5nXHJcbiAqXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG52YXIgVERTVE0gPSByZXF1aXJlKCcuLi8uLi9jb25maWcvQXBwLmpzJyk7XHJcblxyXG5URFNUTS5jcmVhdGVEaXJlY3RpdmUoJ3RvYXN0SGFuZGxlcicsIFsnJGxvZycsICckdGltZW91dCcsICdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicsICdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJyxcclxuICAgICdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3InLCAnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLFxyXG4gICAgZnVuY3Rpb24gKCRsb2csICR0aW1lb3V0LCBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciwgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcixcclxuICAgICAgICAgICAgICBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IsIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yKSB7XHJcblxyXG4gICAgJGxvZy5kZWJ1ZygnVG9hc3RIYW5kbGVyIGxvYWRlZCcpO1xyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICBzY29wZToge1xyXG4gICAgICAgICAgICBtc2c6ICc9JyxcclxuICAgICAgICAgICAgdHlwZTogJz0nLFxyXG4gICAgICAgICAgICBzdGF0dXM6ICc9J1xyXG4gICAgICAgIH0sXHJcbiAgICAgICAgcHJpb3JpdHk6IDUsXHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvZGlyZWN0aXZlcy9Ub29scy9Ub2FzdEhhbmRsZXIuaHRtbCcsXHJcbiAgICAgICAgcmVzdHJpY3Q6ICdFJyxcclxuICAgICAgICBjb250cm9sbGVyOiBbJyRzY29wZScsICckcm9vdFNjb3BlJywgZnVuY3Rpb24gKCRzY29wZSwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgICAgICAkc2NvcGUuYWxlcnQgPSB7XHJcbiAgICAgICAgICAgICAgICBzdWNjZXNzOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGRhbmdlcjoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBpbmZvOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIHdhcm5pbmc6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzID0ge1xyXG4gICAgICAgICAgICAgICAgc2hvdzogZmFsc2VcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIGZ1bmN0aW9uIHR1cm5PZmZOb3RpZmljYXRpb25zKCl7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuc3VjY2Vzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5pbmZvLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC53YXJuaW5nLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBMaXN0ZW4gdG8gYW55IHJlcXVlc3QsIHdlIGNhbiByZWdpc3RlciBsaXN0ZW5lciBpZiB3ZSB3YW50IHRvIGFkZCBleHRyYSBjb2RlLlxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVxdWVzdCgpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24oY29uZmlnKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgdG86ICcsICBjb25maWcpO1xyXG4gICAgICAgICAgICAgICAgdmFyIHRpbWUgPSBjb25maWcucmVxdWVzdFRpbWVzdGFtcDtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcodGltZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgZXJyb3I6ICcsICByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVzcG9uc2UoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlc3BvbnNlKXtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wIC0gcmVzcG9uc2UuY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdUaGUgcmVxdWVzdCB0b29rICcgKyAodGltZSAvIDEwMDApICsgJyBzZWNvbmRzJyk7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSByZXN1bHQ6ICcsIHJlc3BvbnNlKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuRXJyb3IoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlamVjdGlvbil7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSBlcnJvcjogJywgcmVqZWN0aW9uKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zdGF0dXMgPSByZWplY3Rpb24uc3RhdHVzO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zdGF0dXNUZXh0ID0gcmVqZWN0aW9uLnN0YXR1c1RleHQ7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLmVycm9ycyA9IHJlamVjdGlvbi5kYXRhLmVycm9ycztcclxuICAgICAgICAgICAgICAgICR0aW1lb3V0KHR1cm5PZmZOb3RpZmljYXRpb25zLCAzMDAwKTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogSGlkZSB0aGUgUG9wIHVwIG5vdGlmaWNhdGlvbiBtYW51YWxseVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHNjb3BlLm9uQ2FuY2VsUG9wVXAgPSBmdW5jdGlvbigpIHtcclxuICAgICAgICAgICAgICAgIHR1cm5PZmZOb3RpZmljYXRpb25zKCk7XHJcbiAgICAgICAgICAgIH07XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogSXQgd2F0Y2ggdGhlIHZhbHVlIHRvIHNob3cgdGhlIG1zZyBpZiBuZWNlc3NhcnlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRyb290U2NvcGUuJG9uKCdicm9hZGNhc3QtbXNnJywgZnVuY3Rpb24oZXZlbnQsIGFyZ3MpIHtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ2Jyb2FkY2FzdC1tc2cgZXhlY3V0ZWQnKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc3RhdHVzVGV4dCA9IGFyZ3MudGV4dDtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnN0YXR1cyA9IG51bGw7XHJcbiAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMjAwMCk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuJGFwcGx5KCk7IC8vIHJvb3RTY29wZSBhbmQgd2F0Y2ggZXhjbHVkZSB0aGUgYXBwbHkgYW5kIG5lZWRzIHRoZSBuZXh0IGN5Y2xlIHRvIHJ1blxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBJdCB3YXRjaCB0aGUgdmFsdWUgdG8gc2hvdyB0aGUgbXNnIGlmIG5lY2Vzc2FyeVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHNjb3BlLiR3YXRjaCgnbXNnJywgZnVuY3Rpb24obmV3VmFsdWUsIG9sZFZhbHVlKSB7XHJcbiAgICAgICAgICAgICAgICBpZiAobmV3VmFsdWUgJiYgbmV3VmFsdWUgIT09ICcnKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0WyRzY29wZS50eXBlXS5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnN0YXR1c1RleHQgPSBuZXdWYWx1ZTtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnN0YXR1cyA9ICRzY29wZS5zdGF0dXM7XHJcbiAgICAgICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsIDI1MDApO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgfV1cclxuICAgIH07XHJcbn1dKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBJdCBoYW5kbGVyIHRoZSBpbmRleCBmb3IgYW55IG9mIHRoZSBkaXJlY3RpdmVzIGF2YWlsYWJsZVxyXG4gKi9cclxuXHJcbnJlcXVpcmUoJy4vVG9vbHMvVG9hc3RIYW5kbGVyLmpzJyk7XHJcbnJlcXVpcmUoJy4vVG9vbHMvTW9kYWxXaW5kb3dBY3RpdmF0aW9uLmpzJyk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMTcvMjAxNS5cclxuICovXHJcblxyXG4vLyBNYWluIEFuZ3VsYXJKcyBjb25maWd1cmF0aW9uXHJcbnJlcXVpcmUoJy4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuLy8gSGVscGVyc1xyXG5yZXF1aXJlKCcuL2NvbmZpZy9Bbmd1bGFyUHJvdmlkZXJIZWxwZXIuanMnKTtcclxuXHJcbi8vIERpcmVjdGl2ZXNcclxucmVxdWlyZSgnLi9kaXJlY3RpdmVzL2luZGV4Jyk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBEaWFsb2dBY3Rpb24ge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLnRpdGxlID0gcGFyYW1zLnRpdGxlO1xyXG4gICAgICAgIHRoaXMubWVzc2FnZSA9IHBhcmFtcy5tZXNzYWdlO1xyXG5cclxuICAgIH1cclxuICAgIC8qKlxyXG4gICAgICogQWNjY2VwdCBhbmQgQ29uZmlybVxyXG4gICAgICovXHJcbiAgICBjb25maXJtQWN0aW9uKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSgpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIvMjAxNS5cclxuICogSGVhZGVyIENvbnRyb2xsZXIgbWFuYWdlIHRoZSB2aWV3IGF2YWlsYWJsZSBvbiB0aGUgc3RhdGUuZGF0YVxyXG4gKiAtLS0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAqIEhlYWRlciBDb250cm9sbGVyXHJcbiAqIFBhZ2UgdGl0bGUgICAgICAgICAgICAgICAgICAgICAgSG9tZSAtPiBMYXlvdXQgLSBTdWIgTGF5b3V0XHJcbiAqXHJcbiAqIE1vZHVsZSBDb250cm9sbGVyXHJcbiAqIENvbnRlbnRcclxuICogLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhlYWRlckNvbnRyb2xsZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZ1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcblxyXG4gICAgICAgIHRoaXMucGFnZU1ldGFEYXRhID0ge1xyXG4gICAgICAgICAgICB0aXRsZTogJycsXHJcbiAgICAgICAgICAgIGluc3RydWN0aW9uOiAnJyxcclxuICAgICAgICAgICAgbWVudTogW11cclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVIZWFkZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSGVhZGVyIENvbnRyb2xsZXIgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWZXJpZnkgaWYgd2UgaGF2ZSBhIG1lbnUgdG8gc2hvdyB0byBtYWRlIGl0IGF2YWlsYWJsZSB0byB0aGUgVmlld1xyXG4gICAgICovXHJcbiAgICBwcmVwYXJlSGVhZGVyKCkge1xyXG4gICAgICAgIGlmICh0aGlzLnN0YXRlICYmIHRoaXMuc3RhdGUuJGN1cnJlbnQgJiYgdGhpcy5zdGF0ZS4kY3VycmVudC5kYXRhKSB7XHJcbiAgICAgICAgICAgIHRoaXMucGFnZU1ldGFEYXRhID0gdGhpcy5zdGF0ZS4kY3VycmVudC5kYXRhLnBhZ2U7XHJcbiAgICAgICAgICAgIGRvY3VtZW50LnRpdGxlID0gdGhpcy5wYWdlTWV0YURhdGEudGl0bGU7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjEvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBIZWFkZXJDb250cm9sbGVyIGZyb20gJy4vSGVhZGVyQ29udHJvbGxlci5qcyc7XHJcbmltcG9ydCBEaWFsb2dBY3Rpb24gZnJvbSAnLi4vZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5qcyc7XHJcblxyXG52YXIgSGVhZGVyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkhlYWRlck1vZHVsZScsIFtdKTtcclxuXHJcbkhlYWRlck1vZHVsZS5jb250cm9sbGVyKCdIZWFkZXJDb250cm9sbGVyJywgWyckbG9nJywgJyRzdGF0ZScsIEhlYWRlckNvbnRyb2xsZXJdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuSGVhZGVyTW9kdWxlLmNvbnRyb2xsZXIoJ0RpYWxvZ0FjdGlvbicsIFsnJGxvZycsJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBEaWFsb2dBY3Rpb25dKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IEhlYWRlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBMaWNlbnNlQWRtaW5MaXN0IGZyb20gJy4vbGlzdC9MaWNlbnNlQWRtaW5MaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VBZG1pblNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL0xpY2Vuc2VBZG1pblNlcnZpY2UuanMnO1xyXG5pbXBvcnQgUmVxdWVzdExpY2Vuc2UgZnJvbSAnLi9yZXF1ZXN0L1JlcXVlc3RMaWNlbnNlLmpzJztcclxuaW1wb3J0IENyZWF0ZWRMaWNlbnNlIGZyb20gJy4vY3JlYXRlZC9DcmVhdGVkTGljZW5zZS5qcyc7XHJcbmltcG9ydCBBcHBseUxpY2Vuc2VLZXkgZnJvbSAnLi9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5LmpzJztcclxuaW1wb3J0IE1hbnVhbGx5UmVxdWVzdCBmcm9tICcuL21hbnVhbGx5UmVxdWVzdC9NYW51YWxseVJlcXVlc3QuanMnO1xyXG5pbXBvcnQgTGljZW5zZURldGFpbCBmcm9tICcuL2RldGFpbC9MaWNlbnNlRGV0YWlsLmpzJztcclxuXHJcblxyXG52YXIgTGljZW5zZUFkbWluTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkxpY2Vuc2VBZG1pbk1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbGljZW5zZUFkbWluJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdsaWNlbnNlQWRtaW5MaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnQWRtaW5pc3RlciBMaWNlbnNlcycsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydBRE1JTicsICdMSUNFTlNFJywgJ0xJU1QnXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvbGljZW5zZS9hZG1pbi9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vbGlzdC9MaWNlbnNlQWRtaW5MaXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlQWRtaW5MaXN0IGFzIGxpY2Vuc2VBZG1pbkxpc3QnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTGljZW5zZUFkbWluTW9kdWxlLnNlcnZpY2UoJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgJyRyb290U2NvcGUnLCBMaWNlbnNlQWRtaW5TZXJ2aWNlXSk7XHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTGljZW5zZUFkbWluTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlQWRtaW5MaXN0XSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdSZXF1ZXN0TGljZW5zZScsIFsnJGxvZycsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbEluc3RhbmNlJywgUmVxdWVzdExpY2Vuc2VdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0NyZWF0ZWRMaWNlbnNlJywgWyckbG9nJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIENyZWF0ZWRMaWNlbnNlXSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdBcHBseUxpY2Vuc2VLZXknLCBbJyRsb2cnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBBcHBseUxpY2Vuc2VLZXldKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ01hbnVhbGx5UmVxdWVzdCcsIFsnJGxvZycsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIE1hbnVhbGx5UmVxdWVzdF0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTGljZW5zZURldGFpbCcsIFsnJGxvZycsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlRGV0YWlsXSk7XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgTGljZW5zZUFkbWluTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBBcHBseUxpY2Vuc2VLZXkge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0gcGFyYW1zLmxpY2Vuc2U7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIGFuZCB2YWxpZGF0ZSB0aGUgS2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgYXBwbHlLZXkoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmFwcGx5TGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQ3JlYXRlZFJlcXVlc3RMaWNlbnNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5jbGllbnQgPSBwYXJhbXM7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlRGV0YWlsIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIG1ldGhvZElkOiBwYXJhbXMubGljZW5zZS5tZXRob2QuaWQsXHJcbiAgICAgICAgICAgIHByb2plY3ROYW1lOiBwYXJhbXMubGljZW5zZS5wcm9qZWN0Lm5hbWUsXHJcbiAgICAgICAgICAgIGNsaWVudE5hbWU6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5uYW1lLFxyXG4gICAgICAgICAgICBlbWFpbDogcGFyYW1zLmxpY2Vuc2UuZW1haWwsXHJcbiAgICAgICAgICAgIHNlcnZlcnNUb2tlbnM6IHBhcmFtcy5saWNlbnNlLnNlcnZlcnNUb2tlbnMgfHwgcGFyYW1zLmxpY2Vuc2UubWF4U2VydmVycyxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnROYW1lOiBwYXJhbXMubGljZW5zZS5lbnZpcm9ubWVudC5uYW1lLFxyXG4gICAgICAgICAgICBpbmNlcHRpb246IHBhcmFtcy5saWNlbnNlLnJlcXVlc3REYXRlLFxyXG4gICAgICAgICAgICBleHBpcmF0aW9uOiBwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSxcclxuICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdE5vdGUsXHJcbiAgICAgICAgICAgIGFwcGxpZWQ6IHBhcmFtcy5saWNlbnNlLmFwcGxpZWQsXHJcbiAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgcmVwbGFjZWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkLFxyXG4gICAgICAgICAgICBlbmNyeXB0ZWREZXRhaWw6IHBhcmFtcy5saWNlbnNlLmVuY3J5cHRlZERldGFpbFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuIHByZXBhcmVNZXRob2RPcHRpb25zKCk7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdTZXJ2ZXJzJ1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbnMnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAzLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ0N1c3RvbSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRoZSB1c2VyIGFwcGx5IGFuZCBzZXJ2ZXIgc2hvdWxkIHZhbGlkYXRlIHRoZSBrZXkgaXMgY29ycmVjdFxyXG4gICAgICovXHJcbiAgICBhcHBseUxpY2Vuc2VLZXkoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2FwcGx5TGljZW5zZUtleS9BcHBseUxpY2Vuc2VLZXkuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdBcHBseUxpY2Vuc2VLZXkgYXMgYXBwbHlMaWNlbnNlS2V5JyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgY29uc29sZS5sb2codGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQgPSB0cnVlO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogT3BlbnMgYSBkaWFsb2cgYW5kIGFsbG93IHRoZSB1c2VyIHRvIG1hbnVhbGx5IHNlbmQgdGhlIHJlcXVlc3Qgb3IgY29weSB0aGUgZW5jcmlwdGVkIGNvZGVcclxuICAgICAqL1xyXG4gICAgbWFudWFsbHlSZXF1ZXN0KCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTWFudWFsbHlSZXF1ZXN0IGFzIG1hbnVhbGx5UmVxdWVzdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdCgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UucmVzdWJtaXRMaWNlbnNlUmVxdWVzdCh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHt9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIGRlbGV0ZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmRlbGV0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5MaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VBZG1pbkxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vblJlcXVlc3ROZXdMaWNlbnNlKClcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gUmVxdWVzdCBOZXcgTGljZW5zZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5yZWxvYWRMaWNlbnNlQWRtaW5MaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbGljZW5zZUlkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJsaWNlbnNlQWRtaW5MaXN0Lm9uTGljZW5zZURldGFpbHModGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NsaWVudC5uYW1lJywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QubmFtZScsIHRpdGxlOiAnUHJvamVjdCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cy5uYW1lJywgdGl0bGU6ICdTdGF0dXMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3NlcnZlcnNUb2tlbnMnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnLCB0ZW1wbGF0ZTogJyM6bWF4U2VydmVycyMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3JlcXVlc3REYXRlJywgdGl0bGU6ICdJbmNlcHRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uRGF0ZScsIHRpdGxlOiAnRXhwaXJhdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Vudmlyb25tZW50Lm5hbWUnLCB0aXRsZTogJ0Vudi4nfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICd0aXRsZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0TGljZW5zZSBhcyByZXF1ZXN0TGljZW5zZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIGRyYWdnYWJsZTogdHJ1ZVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIENyZWF0ZWQ6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLm9uTmV3TGljZW5zZUNyZWF0ZWQobGljZW5zZSk7XHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZUFkbWluTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEFmdGVyIGNsaWNraW5nIG9uIGVkaXQsIHdlIHJlZGlyZWN0IHRoZSB1c2VyIHRvIHRoZSBFZGl0aW9uIHNjcmVlbiBpbnN0ZWFkIG9mIG9wZW4gYSBkaWFsb2dcclxuICAgICAqIGR1IHRoZSBzaXplIG9mIHRoZSBpbnB1dHNcclxuICAgICAqL1xyXG4gICAgb25MaWNlbnNlRGV0YWlscyhsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnT3BlbiBEZXRhaWxzIGZvcjogJywgbGljZW5zZSk7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2RldGFpbC9MaWNlbnNlRGV0YWlsLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZURldGFpbCBhcyBsaWNlbnNlRGV0YWlsJyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgdmFyIGRhdGFJdGVtID0gbGljZW5zZSAmJiBsaWNlbnNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IGRhdGFJdGVtIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZUFkbWluTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBvbk5ld0xpY2Vuc2VDcmVhdGVkKGxpY2Vuc2UpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2NyZWF0ZWQvQ3JlYXRlZExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdDcmVhdGVkTGljZW5zZSBhcyBjcmVhdGVkTGljZW5zZScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGVtYWlsOiBsaWNlbnNlLmVtYWlsICB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVsb2FkTGljZW5zZUFkbWluTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLnJlYWQoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIE1hbnVhbGx5UmVxdWVzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSBwYXJhbXMubGljZW5zZTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgYW5kIHZhbGlkYXRlIHRoZSBLZXkgaXMgY29ycmVjdFxyXG4gICAgICovXHJcbiAgICBlbWFpbFJlcXVlc3QoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmVtYWlsUmVxdWVzdCh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqIENyZWF0ZSBhIG5ldyBSZXF1ZXN0IHRvIGdldCBhIExpY2Vuc2VcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0TGljZW5zZSB7XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJbml0aWFsaXplIGFsbCB0aGUgcHJvcGVydGllc1xyXG4gICAgICogQHBhcmFtICRsb2dcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlQWRtaW5TZXJ2aWNlXHJcbiAgICAgKiBAcGFyYW0gJHVpYk1vZGFsSW5zdGFuY2VcclxuICAgICAqL1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgRW52aXJvbm1lbnQgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UgPSBbXTtcclxuICAgICAgICAvLyBEZWZpbmUgdGhlIFByb2plY3QgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSBbXTtcclxuXHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldFByb2plY3REYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIENyZWF0ZSB0aGUgTW9kZWwgZm9yIHRoZSBOZXcgTGljZW5zZVxyXG4gICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBlbWFpbDogJycsXHJcbiAgICAgICAgICAgIGVudmlyb25tZW50SWQ6IDAsXHJcbiAgICAgICAgICAgIHByb2plY3RJZDogMCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogJycsXHJcbiAgICAgICAgICAgIHJlcXVlc3ROb3RlOiAnJ1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpPT57XHJcbiAgICAgICAgICAgIHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlID0gZGF0YTtcclxuICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuZW52aXJvbm1lbnRJZCA9IGRhdGFbMF0uaWQ7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgUHJvamVjdCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0UHJvamVjdERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwucHJvamVjdElkID0gZGF0YVswXS5pZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZSxcclxuICAgICAgICAgICAgc2VsZWN0OiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE9uIFByb2plY3QgQ2hhbmdlLCBzZWxlY3QgdGhlIENsaWVudCBOYW1lXHJcbiAgICAgICAgICAgICAgICB2YXIgaXRlbSA9IHRoaXMuc2VsZWN0UHJvamVjdC5kYXRhSXRlbShlLml0ZW0pO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuY2xpZW50TmFtZSA9IGl0ZW0uY2xpZW50Lm5hbWU7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBnZW5lcmF0ZSBhIG5ldyBMaWNlbnNlIHJlcXVlc3RcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2VSZXF1ZXN0KCkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIFJlcXVlc3RlZDogJywgdGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdCh0aGlzLm5ld0xpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5TZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIsICRyb290U2NvcGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLnJvb3RTY29wZSA9ICRyb290U2NvcGU7XHJcbiAgICAgICAgdGhpcy5zdGF0dXNTdWNjZXNzID0gJ3N1Y2Nlc3MnO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdsaWNlbnNlQWRtaW5TZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldExpY2Vuc2VMaXN0KG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBvblN1Y2Nlc3Mpe1xyXG4gICAgICAgIG5ld0xpY2Vuc2UuZW52aXJvbm1lbnRJZCA9IHBhcnNlSW50KG5ld0xpY2Vuc2UuZW52aXJvbm1lbnRJZCk7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0KG5ld0xpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdChsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5yZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdSZXF1ZXN0IExpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseS4nfSk7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBlbWFpbFJlcXVlc3QobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZW1haWxSZXF1ZXN0KGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdSZXF1ZXN0IExpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseS4nfSk7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqICBBcHBseSBUaGUgTGluY2Vuc2VcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gb25TdWNjZXNzXHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuXHJcbiAgICAgICAgdmFyIGhhc2ggPSAge1xyXG4gICAgICAgICAgICBoYXNoOiBsaWNlbnNlLmtleVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5hcHBseUxpY2Vuc2UobGljZW5zZS5pZCwgaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTGljZW5zZShsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5kZWxldGVMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJMaXN0IGZyb20gJy4vbGlzdC9MaWNlbnNlTWFuYWdlckxpc3QuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9MaWNlbnNlTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgUmVxdWVzdEltcG9ydCBmcm9tICcuL3JlcXVlc3RJbXBvcnQvUmVxdWVzdEltcG9ydC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlckRldGFpbCBmcm9tICcuL2RldGFpbC9MaWNlbnNlTWFuYWdlckRldGFpbC5qcyc7XHJcblxyXG5cclxudmFyIExpY2Vuc2VNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkxpY2Vuc2VNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdsaWNlbnNlTWFuYWdlcicpO1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlclZpZXcuaHRtbCcsXHJcbiAgICAgICAgY29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG4gICAgfTtcclxuXHJcbiAgICAkc3RhdGVQcm92aWRlclxyXG4gICAgICAgIC5zdGF0ZSgnbGljZW5zZU1hbmFnZXJMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTGljZW5zaW5nIE1hbmFnZXInLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnTUFOQUdFUicsICdMSUNFTlNFJywgJ0xJU1QnXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvbGljZW5zZS9tYW5hZ2VyL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlTWFuYWdlckxpc3QgYXMgbGljZW5zZU1hbmFnZXJMaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLnNlcnZpY2UoJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCAnJHJvb3RTY29wZScsIExpY2Vuc2VNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VNYW5hZ2VyTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsIExpY2Vuc2VNYW5hZ2VyTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdSZXF1ZXN0SW1wb3J0JywgWyckbG9nJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RJbXBvcnRdKTtcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZU1hbmFnZXJEZXRhaWwnLCBbJyRsb2cnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlTWFuYWdlckRldGFpbF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlckRldGFpbCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0kdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIHByaW5jaXBhbElkOiBwYXJhbXMubGljZW5zZS5wcmluY2lwYWwuaWQsXHJcbiAgICAgICAgICAgIGVtYWlsOiBwYXJhbXMubGljZW5zZS5jb250YWN0X2VtYWlsLFxyXG4gICAgICAgICAgICBwcm9qZWN0SWQ6IHBhcmFtcy5saWNlbnNlLnByb2plY3QuaWQsXHJcbiAgICAgICAgICAgIGNsaWVudElkOiBwYXJhbXMubGljZW5zZS5jbGllbnQuaWQsXHJcbiAgICAgICAgICAgIHN0YXR1c0lkOiBwYXJhbXMubGljZW5zZS5zdGF0dXMuaWQsXHJcbiAgICAgICAgICAgIG1ldGhvZDoge1xyXG4gICAgICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5pZCxcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5uYW1lLFxyXG4gICAgICAgICAgICAgICAgcXVhbnRpdHk6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5xdWFudGl0eVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudElkOiBwYXJhbXMubGljZW5zZS5lbnZpcm9ubWVudC5pZCxcclxuICAgICAgICAgICAgcmVxdWVzdGVkOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0ZWQsXHJcbiAgICAgICAgICAgIGluaXREYXRlOiBwYXJhbXMubGljZW5zZS5pbml0RGF0ZSxcclxuICAgICAgICAgICAgZW5kRGF0ZTogcGFyYW1zLmxpY2Vuc2UuZW5kRGF0ZSxcclxuICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogcGFyYW1zLmxpY2Vuc2Uuc3BlY2lhbEluc3RydWN0aW9ucyxcclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogcGFyYW1zLmxpY2Vuc2UuYmFubmVyTWVzc2FnZSxcclxuICAgICAgICAgICAgcmVxdWVzdGVkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3RlZElkLFxyXG4gICAgICAgICAgICByZXBsYWNlZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWQsXHJcbiAgICAgICAgICAgIHJlcGxhY2VkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkSWQsXHJcbiAgICAgICAgICAgIGxpY2Vuc2VLZXk6IHBhcmFtcy5saWNlbnNlLmxpY2Vuc2VLZXksXHJcbiAgICAgICAgICAgIGFjdGl2aXR5TGlzdDogcGFyYW1zLmxpY2Vuc2UuYWN0aXZpdHlMaXN0LFxyXG4gICAgICAgICAgICBob3N0TmFtZTogcGFyYW1zLmxpY2Vuc2UuaG9zdE5hbWUsXHJcbiAgICAgICAgICAgIHdlYnNpdGVOYW1lOiBwYXJhbXMubGljZW5zZS53ZWJzaXRlTmFtZSxcclxuICAgICAgICAgICAgaGFzaDogcGFyYW1zLmxpY2Vuc2UuaGFzaCxcclxuXHJcbiAgICAgICAgICAgIGFwcGxpZWQ6IHBhcmFtcy5saWNlbnNlLmFwcGxpZWQsXHJcbiAgICAgICAgICAgIGtleUlkOiBwYXJhbXMubGljZW5zZS5rZXlJZFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIC8vIENyZWF0ZXMgdGhlIEtlbmRvIFByb2plY3QgU2VsZWN0IExpc3RcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3QgPSB7fTtcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3RMaXN0T3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZGF0YVNvdXJjZTogdGhpcy5nZXRQcm9qZWN0c0RhdGFTb3VyY2UoKSxcclxuICAgICAgICAgICAgb3B0aW9uTGFiZWw6ICdTZWxlY3QgYSBQcm9qZWN0JyxcclxuICAgICAgICAgICAgZGF0YVRleHRGaWVsZDogJ25hbWUnLFxyXG4gICAgICAgICAgICBkYXRhVmFsdWVGaWVsZDogJ2lkJyxcclxuICAgICAgICAgICAgdmFsdWVQcmltaXRpdmU6IHRydWUsXHJcbiAgICAgICAgICAgIHNlbGVjdDogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB2YXIgaXRlbSA9IHRoaXMuc2VsZWN0UHJvamVjdC5kYXRhSXRlbShlLml0ZW0pO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZVByb2plY3QoaXRlbSk7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCB0aGUgdHdvIEtlbmRvIERhdGVzIGZvciBJbml0IGFuZCBFbmREYXRlXHJcbiAgICAgICAgdGhpcy5pbml0RGF0ZSA9IHt9O1xyXG4gICAgICAgIHRoaXMuaW5pdERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6ICd5eXl5L01NL2RkJyxcclxuICAgICAgICAgICAgbWF4OiB0aGlzLmxpY2Vuc2VNb2RlbC5lbmREYXRlLFxyXG4gICAgICAgICAgICBjaGFuZ2U6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUluaXREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5lbmREYXRlID0ge307XHJcbiAgICAgICAgdGhpcy5lbmREYXRlT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZm9ybWF0OiAneXl5eS9NTS9kZCcsXHJcbiAgICAgICAgICAgIG1pbjogdGhpcy5saWNlbnNlTW9kZWwuaW5pdERhdGUsXHJcbiAgICAgICAgICAgIGNoYW5nZTogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlRW5kRGF0ZSgpO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0UHJpbmNpcGFsRGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXRDbGllbnREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXRTdGF0dXNEYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZU1ldGhvZE9wdGlvbnMoKTtcclxuICAgICAgICB0aGlzLnByZXBhcmVBY3Rpdml0eUxpc3QoKTtcclxuXHJcblxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ29udHJvbHMgd2hhdCBidXR0b25zIHRvIHNob3dcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCkge1xyXG4gICAgICAgIHRoaXMucGVuZGluZ0xpY2Vuc2UgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMiAmJiAhdGhpcy5lZGl0TW9kZTtcclxuICAgICAgICB0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgPSAodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDMgfHwgdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDQpO1xyXG4gICAgICAgIHRoaXMuYWN0aXZlU2hvd01vZGUgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMSAmJiAhdGhpcy5leHBpcmVkT3JUZXJtaW5hdGVkICYmICF0aGlzLmVkaXRNb2RlO1xyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVNZXRob2RPcHRpb25zKCkge1xyXG4gICAgICAgIHRoaXMubWV0aG9kT3B0aW9ucyA9IFtcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnU2VydmVycycsXHJcbiAgICAgICAgICAgICAgICBxdWFudGl0eTogODAwMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbnMnLFxyXG4gICAgICAgICAgICAgICAgcXVhbnRpdHk6IDQwMDAwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAzLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ0N1c3RvbSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF1cclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlQWN0aXZpdHlMaXN0KCkge1xyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdkYXRlJywgdGl0bGU6ICdEYXRlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd3aG9tJywgdGl0bGU6ICdXaG9tJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbid9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHRoaXMubGljZW5zZU1vZGVsLmFjdGl2aXR5TGlzdCxcclxuICAgICAgICAgICAgc2Nyb2xsYWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGUgdXNlciBhcHBseSBhbmQgc2VydmVyIHNob3VsZCB2YWxpZGF0ZSB0aGUga2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgYXBwbHlMaWNlbnNlS2V5KCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2FwcGx5TGljZW5zZUtleS9BcHBseUxpY2Vuc2VLZXkuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdBcHBseUxpY2Vuc2VLZXkgYXMgYXBwbHlMaWNlbnNlS2V5JyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgbGljZW5zZTogdGhpcy5saWNlbnNlTW9kZWwgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuYXBwbGllZCA9IHRydWU7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgYWN0aXZhdGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmFjdGl2YXRlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHt9KTtcclxuICAgIH1cclxuXHJcbiAgICByZXZva2VMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIHJldm9rZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UucmV2b2tlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWYWxpZGF0ZSB0aGUgaW5wdXQgb24gU2VydmVyIG9yIFRva2VucyBpcyBvbmx5IGludGVnZXIgb25seVxyXG4gICAgICogVGhpcyB3aWxsIGJlIGNvbnZlcnRlZCBpbiBhIG1vcmUgY29tcGxleCBkaXJlY3RpdmUgbGF0ZXJcclxuICAgICAqIFRPRE86IENvbnZlcnQgaW50byBhIGRpcmVjdGl2ZVxyXG4gICAgICovXHJcbiAgICB2YWxpZGF0ZUludGVnZXJPbmx5KGUpe1xyXG4gICAgICAgIHRyeSB7XHJcbiAgICAgICAgICAgIHZhciBuZXdWYWw9IHBhcnNlSW50KHRoaXMubGljZW5zZU1vZGVsLm1ldGhvZC5xdWFudGl0eSk7XHJcbiAgICAgICAgICAgIGlmKCFpc05hTihuZXdWYWwpKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5tZXRob2QucXVhbnRpdHkgPSBuZXdWYWw7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5tZXRob2QucXVhbnRpdHkgPSAwO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICBpZihlICYmIGUuY3VycmVudFRhcmdldCAmJiBlLmN1cnJlbnRUYXJnZXQudmFsdWUpIHtcclxuICAgICAgICAgICAgICAgIGUuY3VycmVudFRhcmdldC52YWx1ZSA9IHRoaXMubGljZW5zZU1vZGVsLm1ldGhvZC5xdWFudGl0eTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICB0aGlzLiRsb2cud2FybignSW52YWxpZCBOdW1iZXIgRXhwY2VwdGlvbicsIHRoaXMubGljZW5zZU1vZGVsLm1ldGhvZC5xdWFudGl0eSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZSBjdXJyZW50IGNoYW5nZXNcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2Uuc2F2ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTGljZW5zZSBTYXZlZCcpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2hhbmdlIHRoZSBzdGF0dXMgdG8gRWRpdFxyXG4gICAgICovXHJcbiAgICBtb2RpZnlMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSB0cnVlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0UHJpbmNpcGFsRGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnByaW5jaXBhbERhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHtpZDogMSwgbmFtZTogJ0VNQyd9LFxyXG4gICAgICAgICAgICB7aWQ6IDIsIG5hbWU6ICdJQk0nfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7aWQ6IDEsIG5hbWU6ICdQcm9kdWN0aW9uJ30sXHJcbiAgICAgICAgICAgIHtpZDogMiwgbmFtZTogJ090aGVyJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByb2plY3RzRGF0YVNvdXJjZSgpIHtcclxuICAgICAgICByZXR1cm4gIFtcclxuICAgICAgICAgICAge2lkOiAxLCBuYW1lOiAnbi9hJ30sXHJcbiAgICAgICAgICAgIHtpZDogMiwgbmFtZTogJ0JhbmsgRWFzdCd9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRDbGllbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuY2xpZW50c0RhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHtpZDogMSwgbmFtZTogJ24vYSd9LFxyXG4gICAgICAgICAgICB7aWQ6IDIsIG5hbWU6ICdHb2xkIEJhbmsnfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0U3RhdHVzRGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnN0YXR1c0RhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHtpZDogMSwgbmFtZTogJ0FjdGl2ZSd9LFxyXG4gICAgICAgICAgICB7aWQ6IDIsIG5hbWU6ICdQZW5kaW5nJ30sXHJcbiAgICAgICAgICAgIHtpZDogMywgbmFtZTogJ0V4cGlyZWQnfSxcclxuICAgICAgICAgICAge2lkOiA0LCBuYW1lOiAnVGVybWluYXRlZCd9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEEgbmV3IFByb2plY3QgaGFzIGJlZW4gc2VsZWN0ZWQsIHRoYXQgbWVhbnMgd2UgbmVlZCB0byByZWxvYWQgdGhlIG5leHQgcHJvamVjdCBzZWN0aW9uXHJcbiAgICAgKiBAcGFyYW0gaXRlbVxyXG4gICAgICovXHJcbiAgICBvbkNoYW5nZVByb2plY3QoaXRlbSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09uIGNoYW5nZSBQcm9qZWN0JywgaXRlbSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25DaGFuZ2VJbml0RGF0ZSgpIHtcclxuICAgICAgICB2YXIgc3RhcnREYXRlID0gdGhpcy5pbml0RGF0ZS52YWx1ZSgpLFxyXG4gICAgICAgICAgICBlbmREYXRlID0gdGhpcy5lbmREYXRlLnZhbHVlKCk7XHJcblxyXG4gICAgICAgIGlmIChzdGFydERhdGUpIHtcclxuICAgICAgICAgICAgc3RhcnREYXRlID0gbmV3IERhdGUoc3RhcnREYXRlKTtcclxuICAgICAgICAgICAgc3RhcnREYXRlLnNldERhdGUoc3RhcnREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4oc3RhcnREYXRlKTtcclxuICAgICAgICB9IGVsc2UgaWYgKGVuZERhdGUpIHtcclxuICAgICAgICAgICAgdGhpcy5pbml0RGF0ZS5tYXgobmV3IERhdGUoZW5kRGF0ZSkpO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZSgpO1xyXG4gICAgICAgICAgICB0aGlzLmluaXREYXRlLmluaXREYXRlLm1heChlbmREYXRlKTtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihlbmREYXRlKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgb25DaGFuZ2VFbmREYXRlKCl7XHJcbiAgICAgICAgdmFyIGVuZERhdGUgPSB0aGlzLmVuZERhdGUudmFsdWUoKSxcclxuICAgICAgICAgICAgc3RhcnREYXRlID0gdGhpcy5pbml0RGF0ZS52YWx1ZSgpO1xyXG5cclxuICAgICAgICBpZiAoZW5kRGF0ZSkge1xyXG4gICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgIGVuZERhdGUuc2V0RGF0ZShlbmREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgICAgIHRoaXMuaW5pdERhdGUubWF4KGVuZERhdGUpO1xyXG4gICAgICAgIH0gZWxzZSBpZiAoc3RhcnREYXRlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4obmV3IERhdGUoc3RhcnREYXRlKSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKCk7XHJcbiAgICAgICAgICAgIHRoaXMuaW5pdERhdGUubWF4KGVuZERhdGUpO1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKGVuZERhdGUpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICBpZih0aGlzLmVkaXRNb2RlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIC8vdGhpcy5nZXRMaWNlbnNlTGlzdCgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlTWFuYWdlckxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gMDtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0Lm9uUmVxdWVzdEltcG9ydExpY2Vuc2UoKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBJbXBvcnQgTGljZW5zZSBSZXF1ZXN0PC9idXR0b24+IDxkaXYgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3QucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbGljZW5zZUlkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3Qub25MaWNlbnNlTWFuYWdlckRldGFpbHModGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3ByaW5jaXBhbC5uYW1lJywgdGl0bGU6ICdQcmluY2lwYWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NsaWVudC5uYW1lJywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QubmFtZScsIHRpdGxlOiAnUHJvamVjdCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY29udGFjdF9lbWFpbCcsIHRpdGxlOiAnQ29udGFjdCBFbWFpbCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3RhdHVzLnR5cGUnLCB0aXRsZTogJ1N0YXR1cyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZScsIHRpdGxlOiAnVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3NlcnZlcnNfdG9rZW5zJywgdGl0bGU6ICdTZXJ2ZXIvVG9rZW5zJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdpbmNlcHRpb24nLCB0aXRsZTogJ0luY2VwdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZXhwaXJhdGlvbicsIHRpdGxlOiAnRXhwaXJhdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW52aXJvbm1lbnQnLCB0aXRsZTogJ0Vudi4nfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAvKnRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7Ki9cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHZhciBkYXRhID0gW1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbGljZW5zZUlkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBrZXlJZDogJ2NlNDJjZmQxLTFhYzUtNGZjYy1iZTVjLWNjNzg4NWM4ZjgzYicsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFjdGlvbjogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByaW5jaXBhbDogIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ0VNQydcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgY2xpZW50OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICduL2EnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICduL2EnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNvbnRhY3RfZW1haWw6ICd3ZXN0LmNvYXN0QHh5eXkuY29tJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHR5cGU6ICdBY3RpdmUnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHR5cGU6ICdNdWx0aS1Qcm9qZWN0JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbWV0aG9kOiAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnU2VydmVyJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHF1YW50aXR5OiA0MDAwXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGluaXREYXRlOiAnMjAxNi0wOS0xNScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGVuZERhdGU6ICcyMDE2LTA5LTE4JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVxdWVzdGVkOiAnMjAxNi0xMi0wOCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGVudmlyb25tZW50OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdQcm9kdWN0aW9uJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiAnSGVscCwgSGVscCwgSGVscCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGJhbm5lck1lc3NhZ2U6ICdUaGlzIGFwcGxpY2F0aW9uIHNob3VsZCBvbmx5IGJlIHVzZWQgZm9yIHRyYWluaW5nIHB1cnBvc2UnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXF1ZXN0ZWRJZDogJzU2NDY1NDY1NDY1NDY1NDUtYXNkYXNkYXNkNTRhc2QtYXNkYXM2YXNkYXNkJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYXBwbGllZDogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcGxhY2VkOiBbIHtpZDogMSwgc3RhdHVzOiAnMjAxNi0wNS0wOCcgKyAnIGh0dHA6YmxhYmxhYmEuY29tICcgKycgYWFzZGFzNTQtNWFzZDRhNXNkLWFzZDQ1YTRzZCAnfSwge2lkOiAyLCBzdGF0dXM6ICcyMDE2LTA1LTEwJyArICcgaHR0cDpibGFibGFiYS5jb20gJyArJyBhYXNkYXM1NC01YXNkNGE1c2QtYXNkNDVhNHNkICcgfV0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcGxhY2VkSWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGxpY2Vuc2VLZXk6ICdhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOGFzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4YXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MTgnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhY3Rpdml0eUxpc3Q6IFtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMDgnLCB3aG9tOiAnSGFycnkgTGVncycsIGFjdGlvbjogJ1JlcXVlc3RlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xMCcsIHdob206ICdSb2JpbiBCYW5rcycsIGFjdGlvbjogJ0ltcG9ydGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTExJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdBY3RpdmF0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTQnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnVW5sb2NrZWQgTGljZW5zZSB0byBtb2RpZnknfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjAnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ01vZGlmaWVkIExpY2Vuc2UgZGV0YWlsJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTIxJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdBY3RpdmF0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGhvc3ROYW1lOiAndG0tYWNtZS1iaWdtb3ZlLS5zb21lZG9tYWluZy5sb2NhbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHdlYnNpdGVOYW1lOiAndHJhbm1hbi5zb21lY29ycC5jb20nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBoYXNoOiAna2Fza2xkc2FsZGtqYXNkYTVzNGE2NXNkYTY1c2Q0YTY1c2Q0NmE1c2Q0YXM2NWQnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGxpY2Vuc2VJZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAga2V5SWQ6ICdjZTQyY2ZkMS0xYWM1LTRmY2MtYmU1Yy1jYzc4ODVjOGY4M2InLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhY3Rpb246ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBwcmluY2lwYWw6ICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdJQk0nXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNsaWVudDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnR29sZCBCYW5rJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcHJvamVjdDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnQmFuayBFYXN0J1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjb250YWN0X2VtYWlsOiAnd2VzdC5jb2FzdEB4eXl5LmNvbScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN0YXR1czoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0eXBlOiAnUGVuZGluZydcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgdHlwZTogJ1Byb2plY3QnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBtZXRob2Q6ICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbicsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBxdWFudGl0eTogNTAwMFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpbml0RGF0ZTogJzIwMTYtMDktMTUnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbmREYXRlOiAnMjAxNi0wOS0xOCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcXVlc3RlZDogJzIwMTYtMTItMDgnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbnZpcm9ubWVudDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnUHJvZHVjdGlvbidcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogJ0hlbHAsIEhlbHAsIEhlbHAnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXF1ZXN0ZWRJZDogJzU2NDY1NDY1NDY1NDY1NDUtYXNkYXNkYXNkNTRhc2QtYXNkYXM2YXNkYXNkJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYXBwbGllZDogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGJhbm5lck1lc3NhZ2U6ICdUaGlzIGFwcGxpY2F0aW9uIHNob3VsZCBvbmx5IGJlIHVzZWQgZm9yIHRyYWluaW5nIHB1cnBvc2UnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXBsYWNlZDogWyB7aWQ6IDEsIHN0YXR1czogJzIwMTYtMDUtMDgnICsgJyBodHRwOmJsYWJsYWJhLmNvbSAnICsnIGFhc2RhczU0LTVhc2Q0YTVzZC1hc2Q0NWE0c2QgJ30sIHtpZDogMiwgc3RhdHVzOiAnMjAxNi0wNS0xMCcgKyAnIGh0dHA6YmxhYmxhYmEuY29tICcgKycgYWFzZGFzNTQtNWFzZDRhNXNkLWFzZDQ1YTRzZCAnIH1dLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXBsYWNlZElkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBsaWNlbnNlS2V5OiAnYXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MThhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOGFzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYWN0aXZpdHlMaXN0OiBbXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTA4Jywgd2hvbTogJ0hhcnJ5IExlZ3MnLCBhY3Rpb246ICdSZXF1ZXN0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTAnLCB3aG9tOiAnUm9iaW4gQmFua3MnLCBhY3Rpb246ICdJbXBvcnRlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xMScsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnQWN0aXZhdGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTE0Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ1VubG9ja2VkIExpY2Vuc2UgdG8gbW9kaWZ5J30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTIwJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdNb2RpZmllZCBMaWNlbnNlIGRldGFpbCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yMScsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnQWN0aXZhdGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfVxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBob3N0TmFtZTogJ3RtLWFjbWUtYmlnbW92ZS0uc29tZWRvbWFpbmcubG9jYWwnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB3ZWJzaXRlTmFtZTogJ3RyYW5tYW4uc29tZWNvcnAuY29tJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaGFzaDogJ2thc2tsZHNhbGRramFzZGE1czRhNjVzZGE2NXNkNGE2NXNkNDZhNXNkNGFzNjVkJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBsaWNlbnNlSWQ6IDMsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGtleUlkOiAnY2U0MmNmZDEtMWFjNS00ZmNjLWJlNWMtY2M3ODg1YzhmODNiJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYWN0aW9uOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcHJpbmNpcGFsOiAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnSUJNJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjbGllbnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ0dvbGQgQmFuaycsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByb2plY3Q6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ0JhbmsgRWFzdCdcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgY29udGFjdF9lbWFpbDogJ3dlc3QuY29hc3RAeHl5eS5jb20nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzdGF0dXM6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAzLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgdHlwZTogJ0V4cGlyZWQnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHR5cGU6ICdQcm9qZWN0JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbWV0aG9kOiAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnVG9rZW4nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcXVhbnRpdHk6IDUwMDBcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaW5pdERhdGU6ICcyMDE2LTA5LTE1JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZW5kRGF0ZTogJzIwMTYtMDktMTgnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXF1ZXN0ZWQ6ICcyMDE2LTEyLTA4JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZW52aXJvbm1lbnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ1Byb2R1Y3Rpb24nXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHNwZWNpYWxJbnN0cnVjdGlvbnM6ICdIZWxwLCBIZWxwLCBIZWxwJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVxdWVzdGVkSWQ6ICc1NjQ2NTQ2NTQ2NTQ2NTQ1LWFzZGFzZGFzZDU0YXNkLWFzZGFzNmFzZGFzZCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFwcGxpZWQ6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBiYW5uZXJNZXNzYWdlOiAnVGhpcyBhcHBsaWNhdGlvbiBzaG91bGQgb25seSBiZSB1c2VkIGZvciB0cmFpbmluZyBwdXJwb3NlJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVwbGFjZWQ6IFsge2lkOiAxLCBzdGF0dXM6ICcyMDE2LTA1LTA4JyArICcgaHR0cDpibGFibGFiYS5jb20gJyArJyBhYXNkYXM1NC01YXNkNGE1c2QtYXNkNDVhNHNkICd9LCB7aWQ6IDIsIHN0YXR1czogJzIwMTYtMDUtMTAnICsgJyBodHRwOmJsYWJsYWJhLmNvbSAnICsnIGFhc2RhczU0LTVhc2Q0YTVzZC1hc2Q0NWE0c2QgJyB9XSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVwbGFjZWRJZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbGljZW5zZUtleTogJ2FzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4YXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MThhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFjdGl2aXR5TGlzdDogW1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0wOCcsIHdob206ICdIYXJyeSBMZWdzJywgYWN0aW9uOiAnUmVxdWVzdGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTEwJywgd2hvbTogJ1JvYmluIEJhbmtzJywgYWN0aW9uOiAnSW1wb3J0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTEnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0FjdGl2YXRlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xNCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTE4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdVbmxvY2tlZCBMaWNlbnNlIHRvIG1vZGlmeSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yMCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnTW9kaWZpZWQgTGljZW5zZSBkZXRhaWwnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjEnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0FjdGl2YXRlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ31cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaG9zdE5hbWU6ICd0bS1hY21lLWJpZ21vdmUtLnNvbWVkb21haW5nLmxvY2FsJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgd2Vic2l0ZU5hbWU6ICd0cmFubWFuLnNvbWVjb3JwLmNvbScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGhhc2g6ICdrYXNrbGRzYWxka2phc2RhNXM0YTY1c2RhNjVzZDRhNjVzZDQ2YTVzZDRhczY1ZCdcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbGljZW5zZUlkOiA0LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBrZXlJZDogJ2NlNDJjZmQxLTFhYzUtNGZjYy1iZTVjLWNjNzg4NWM4ZjgzYicsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFjdGlvbjogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByaW5jaXBhbDogIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ0lCTSdcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgY2xpZW50OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdHb2xkIEJhbmsnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdCYW5rIEVhc3QnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNvbnRhY3RfZW1haWw6ICd3ZXN0LmNvYXN0QHh5eXkuY29tJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogNCxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHR5cGU6ICdUZXJtaW5hdGVkJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0eXBlOiAnUHJvamVjdCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG1ldGhvZDogIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ1Rva2VuJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHF1YW50aXR5OiA1MDAwXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGluaXREYXRlOiAnMjAxNi0wOS0xNScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGVuZERhdGU6ICcyMDE2LTA5LTE4JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVxdWVzdGVkOiAnMjAxNi0xMi0wOCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGVudmlyb25tZW50OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdQcm9kdWN0aW9uJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiAnSGVscCwgSGVscCwgSGVscCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcXVlc3RlZElkOiAnNTY0NjU0NjU0NjU0NjU0NS1hc2Rhc2Rhc2Q1NGFzZC1hc2RhczZhc2Rhc2QnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhcHBsaWVkOiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogJ1RoaXMgYXBwbGljYXRpb24gc2hvdWxkIG9ubHkgYmUgdXNlZCBmb3IgdHJhaW5pbmcgcHVycG9zZScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcGxhY2VkOiBbIHtpZDogMSwgc3RhdHVzOiAnMjAxNi0wNS0wOCcgKyAnIGh0dHA6YmxhYmxhYmEuY29tICcgKycgYWFzZGFzNTQtNWFzZDRhNXNkLWFzZDQ1YTRzZCAnfSwge2lkOiAyLCBzdGF0dXM6ICcyMDE2LTA1LTEwJyArICcgaHR0cDpibGFibGFiYS5jb20gJyArJyBhYXNkYXM1NC01YXNkNGE1c2QtYXNkNDVhNHNkICcgfV0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcGxhY2VkSWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGxpY2Vuc2VLZXk6ICdhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOGFzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4YXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MTgnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhY3Rpdml0eUxpc3Q6IFtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMDgnLCB3aG9tOiAnSGFycnkgTGVncycsIGFjdGlvbjogJ1JlcXVlc3RlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xMCcsIHdob206ICdSb2JpbiBCYW5rcycsIGFjdGlvbjogJ0ltcG9ydGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTExJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdBY3RpdmF0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTQnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnVW5sb2NrZWQgTGljZW5zZSB0byBtb2RpZnknfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjAnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ01vZGlmaWVkIExpY2Vuc2UgZGV0YWlsJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTIxJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdBY3RpdmF0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGhvc3ROYW1lOiAndG0tYWNtZS1iaWdtb3ZlLS5zb21lZG9tYWluZy5sb2NhbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHdlYnNpdGVOYW1lOiAndHJhbm1hbi5zb21lY29ycC5jb20nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBoYXNoOiAna2Fza2xkc2FsZGtqYXNkYTVzNGE2NXNkYTY1c2Q0YTY1c2Q0NmE1c2Q0YXM2NWQnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgXTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAvKiB9KTsqL1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBjaGFuZ2U6ICAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIC8vIFdlIGFyZSBjb21pbmcgZnJvbSBhIG5ldyBpbXBvcnRlZCByZXF1ZXN0IGxpY2Vuc2VcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgIT09IDAgJiYgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHZhciBuZXdMaWNlbnNlQ3JlYXRlZCA9IHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YS5maW5kKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gbGljZW5zZS5saWNlbnNlSWQgPT09IHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobmV3TGljZW5zZUNyZWF0ZWQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlTWFuYWdlckRldGFpbHMobmV3TGljZW5zZUNyZWF0ZWQpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRoZSB1c2VyIEltcG9ydCBhIG5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdEltcG9ydExpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvcmVxdWVzdEltcG9ydC9SZXF1ZXN0SW1wb3J0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnUmVxdWVzdEltcG9ydCBhcyByZXF1ZXN0SW1wb3J0JyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJ1xyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IDE7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQWZ0ZXIgY2xpY2tpbmcgb24gZWRpdCwgd2UgcmVkaXJlY3QgdGhlIHVzZXIgdG8gdGhlIEVkaXRpb24gc2NyZWVuIGluc3RlYWQgb2Ygb3BlbiBhIGRpYWxvZ1xyXG4gICAgICogZHUgdGhlIHNpemUgb2YgdGhlIGlucHV0c1xyXG4gICAgICovXHJcbiAgICBvbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyhsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnT3BlbiBEZXRhaWxzIGZvcjogJywgbGljZW5zZSk7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvZGV0YWlsL0xpY2Vuc2VNYW5hZ2VyRGV0YWlsLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJEZXRhaWwgYXMgbGljZW5zZU1hbmFnZXJEZXRhaWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbGcnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSB7fTtcclxuICAgICAgICAgICAgICAgICAgICBpZihsaWNlbnNlICYmIGxpY2Vuc2UuZGF0YUl0ZW0pIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGRhdGFJdGVtID0gbGljZW5zZTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgbGljZW5zZTogZGF0YUl0ZW0gfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuXHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdSZXF1ZXN0IENhbmNlbGVkLicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICByZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdEltcG9ydCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBsaWNlbnNlOiAnJ1xyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIGFuZCB2YWxpZGF0ZSB0aGUgS2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgb25JbXBvcnRMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmltcG9ydExpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlciwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG4gICAgICAgIHRoaXMucm9vdFNjb3BlID0gJHJvb3RTY29wZTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnbGljZW5zZU1hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldExpY2Vuc2VMaXN0KGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZSB0aGUgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBzYXZlTGljZW5zZShsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnNhdmVMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuICAgIC8qKlxyXG4gICAgICogRG9lcyB0aGUgYWN0aXZhdGlvbiBvZiB0aGUgY3VycmVudCBsaWNlbnNlIGlmIHRoaXMgaXMgbm90IGFjdGl2ZVxyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBhY3RpdmF0ZUxpY2Vuc2UobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5hY3RpdmF0ZUxpY2Vuc2UobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ1RoZSBsaWNlbnNlIHdhcyBhY3RpdmF0ZWQgYW5kIHRoZSBsaWNlbnNlIHdhcyBlbWFpbGVkLid9KTtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTWFrZSB0aGUgcmVxdWVzdCB0byBJbXBvcnQgdGhlIGxpY2Vuc2UsIGlmIGZhaWxzLCB0aHJvd3MgYW4gZXhjZXB0aW9uIHZpc2libGUgZm9yIHRoZSB1c2VyIHRvIHRha2UgYWN0aW9uXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGltcG9ydExpY2Vuc2UobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5yZXF1ZXN0SW1wb3J0KGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIC8vaWYoZGF0YS5hcHBsaWVkKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5IEltcG9ydGVkJ30pO1xyXG4gICAgICAgICAgICAvKn0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ3dhcm5pbmcnLCB0ZXh0OiAnTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5IGFwcGxpZWQnfSk7XHJcbiAgICAgICAgICAgIH0qL1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZShsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnJldm9rZUxpY2Vuc2UobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0KG5ld0xpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTm90aWNlTGlzdCBmcm9tICcuL2xpc3QvTm90aWNlTGlzdC5qcyc7XHJcbmltcG9ydCBOb3RpY2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTm90aWNlTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgRWRpdE5vdGljZSBmcm9tICcuL2VkaXQvRWRpdE5vdGljZS5qcyc7XHJcblxyXG5cclxudmFyIE5vdGljZU1hbmFnZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uTm90aWNlTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbm90aWNlTWFuYWdlcicpO1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlclZpZXcuaHRtbCcsXHJcbiAgICAgICAgY29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG4gICAgfTtcclxuXHJcbiAgICAkc3RhdGVQcm92aWRlclxyXG4gICAgICAgIC5zdGF0ZSgnbm90aWNlTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ05vdGljZSBBZG1pbmlzdHJhdGlvbicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydBRE1JTicsICdOT1RJQ0UnLCAnTElTVCddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy9ub3RpY2UvbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9saXN0L05vdGljZUxpc3QuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ05vdGljZUxpc3QgYXMgbm90aWNlTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLnNlcnZpY2UoJ05vdGljZU1hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIE5vdGljZU1hbmFnZXJTZXJ2aWNlXSk7XHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ05vdGljZUxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ05vdGljZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsIE5vdGljZUxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdFZGl0Tm90aWNlJywgWyckbG9nJywgJ05vdGljZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBFZGl0Tm90aWNlXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBOb3RpY2VNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBFZGl0Tm90aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCBub3RpY2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZSA9IG5vdGljZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLmFjdGlvbiA9IHBhcmFtcy5hY3Rpb247XHJcbiAgICAgICAgdGhpcy5hY3Rpb25UeXBlID0gcGFyYW1zLmFjdGlvblR5cGU7XHJcblxyXG4gICAgICAgIHRoaXMua2VuZG9FZGl0b3JUb29scyA9IFtcclxuICAgICAgICAgICAgJ2Zvcm1hdHRpbmcnLCAnY2xlYW5Gb3JtYXR0aW5nJyxcclxuICAgICAgICAgICAgJ2ZvbnROYW1lJywgJ2ZvbnRTaXplJyxcclxuICAgICAgICAgICAgJ2p1c3RpZnlMZWZ0JywgJ2p1c3RpZnlDZW50ZXInLCAnanVzdGlmeVJpZ2h0JywgJ2p1c3RpZnlGdWxsJyxcclxuICAgICAgICAgICAgJ2JvbGQnLFxyXG4gICAgICAgICAgICAnaXRhbGljJyxcclxuICAgICAgICAgICAgJ3ZpZXdIdG1sJ1xyXG4gICAgICAgIF07XHJcblxyXG4gICAgICAgIC8vIENTUyBoYXMgbm90IGNhbmNlbGluZyBhdHRyaWJ1dGVzLCBzbyBpbnN0ZWFkIG9mIHJlbW92aW5nIGV2ZXJ5IHBvc3NpYmxlIEhUTUwsIHdlIG1ha2UgZWRpdG9yIGhhcyBzYW1lIGNzc1xyXG4gICAgICAgIHRoaXMua2VuZG9TdHlsZXNoZWV0cyA9IFtcclxuICAgICAgICAgICAgJy4uL3N0YXRpYy9kaXN0L2pzL3ZlbmRvcnMvYm9vdHN0cmFwL2Rpc3QvY3NzL2Jvb3RzdHJhcC5taW4uY3NzJywgLy8gT3VydCBjdXJyZW50IEJvb3RzdHJhcCBjc3NcclxuICAgICAgICAgICAgJy4uL3N0YXRpYy9kaXN0L2Nzcy9URFNUTUxheW91dC5taW4uY3NzJyAvLyBPcmlnaW5hbCBUZW1wbGF0ZSBDU1NcclxuXHJcbiAgICAgICAgXTtcclxuXHJcbiAgICAgICAgdGhpcy5nZXRUeXBlRGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsID0ge1xyXG4gICAgICAgICAgICB0aXRsZTogJycsXHJcbiAgICAgICAgICAgIHR5cGVJZDogMCxcclxuICAgICAgICAgICAgYWN0aXZlOiBmYWxzZSxcclxuICAgICAgICAgICAgaHRtbFRleHQ6ICcnLFxyXG4gICAgICAgICAgICByYXdUZXh0OiAnJ1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgLy8gT24gRWRpdGlvbiBNb2RlIHdlIGNjIHRoZSBtb2RlbCBhbmQgb25seSB0aGUgcGFyYW1zIHdlIG5lZWRcclxuICAgICAgICBpZihwYXJhbXMubm90aWNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLmlkID0gcGFyYW1zLm5vdGljZS5pZDtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwudGl0bGUgPSBwYXJhbXMubm90aWNlLnRpdGxlO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC50eXBlSWQgPSBwYXJhbXMubm90aWNlLnR5cGUuaWQ7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLmFjdGl2ZSA9IHBhcmFtcy5ub3RpY2UuYWN0aXZlO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5odG1sVGV4dCA9IHBhcmFtcy5ub3RpY2UuaHRtbFRleHQ7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIEVudmlyb25tZW50IGRyb3Bkb3duIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRUeXBlRGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnR5cGVEYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7dHlwZUlkOiAxLCBuYW1lOiAnUHJlbG9naW4nfSxcclxuICAgICAgICAgICAge3R5cGVJZDogMiwgbmFtZTogJ1Bvc3Rsb2dpbid9XHJcbiAgICAgICAgICAgIC8ve3R5cGVJZDogMywgbmFtZTogJ0dlbmVyYWwnfSBEaXNhYmxlZCB1bnRpbCBQaGFzZSBJSVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIHRoZSBTZXJ2aWNlIGNhbGwgdG8gQ3JlYXRlL0VkaXQgYSBub3RpY2VcclxuICAgICAqL1xyXG4gICAgc2F2ZU5vdGljZSgpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKHRoaXMuYWN0aW9uICsgJyBOb3RpY2UgUmVxdWVzdGVkOiAnLCB0aGlzLmVkaXRNb2RlbCk7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZWwucmF3VGV4dCA9ICQoJyNrZW5kby1lZGl0b3ItY3JlYXRlLWVkaXQnKS50ZXh0KCk7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZWwudHlwZUlkID0gcGFyc2VJbnQodGhpcy5lZGl0TW9kZWwudHlwZUlkKTtcclxuICAgICAgICBpZih0aGlzLmFjdGlvbiA9PT0gdGhpcy5hY3Rpb25UeXBlLk5FVykge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmNyZWF0ZU5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIGlmKHRoaXMuYWN0aW9uID09PSB0aGlzLmFjdGlvblR5cGUuRURJVCkge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmVkaXROb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVOb3RpY2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG4gICAgICAgICAgICBzaXplOiAnc20nLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsIG1lc3NhZ2U6ICdBcmUgeW91IHN1cmUgeW91IHdhbnQgdG8gZGVsZXRlIGl0PyBUaGlzIGFjdGlvbiBjYW5ub3QgYmUgdW5kb25lLid9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlLmRlbGV0ZU5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIE5vdGljZUxpc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSwgbm90aWNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG5cclxuICAgICAgICB0aGlzLmFjdGlvblR5cGUgPSB7XHJcbiAgICAgICAgICAgIE5FVzogJ05ldycsXHJcbiAgICAgICAgICAgIEVESVQ6ICdFZGl0J1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZUxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLm5vdGljZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuTkVXKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBDcmVhdGUgTmV3IE5vdGljZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibm90aWNlTGlzdC5yZWxvYWROb3RpY2VMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2h0bWxUZXh0JywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuRURJVCwgdGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RpdGxlJywgdGl0bGU6ICdUaXRsZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5pZCcsIGhpZGRlbjogdHJ1ZX0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2ZScsIHRpdGxlOiAnQWN0aXZlJywgdGVtcGxhdGU6ICcjaWYoYWN0aXZlKSB7IyBZZXMgI30gZWxzZSB7IyBObyAjfSMnIH1cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICd0aXRsZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBOb3RpY2VcclxuICAgICAqL1xyXG4gICAgb25FZGl0Q3JlYXRlTm90aWNlKGFjdGlvbiwgbm90aWNlKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9lZGl0L0VkaXROb3RpY2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdFZGl0Tm90aWNlIGFzIGVkaXROb3RpY2UnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSBub3RpY2UgJiYgbm90aWNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGFjdGlvbjogYWN0aW9uLCBub3RpY2U6IGRhdGFJdGVtLCBhY3Rpb25UeXBlOiB0aGlzLmFjdGlvblR5cGV9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKG5vdGljZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgTm90aWNlOiAnLCBub3RpY2UpO1xyXG4gICAgICAgICAgICAvLyBBZnRlciBhIG5ldyB2YWx1ZSBpcyBhZGRlZCwgbGV0cyB0byByZWZyZXNoIHRoZSBHcmlkXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTm90aWNlTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbyhhY3Rpb24gKyAnIFJlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVsb2FkTm90aWNlTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLlRZUEUgPSB7XHJcbiAgICAgICAgICAgICcxJzogJ1ByZWxvZ2luJyxcclxuICAgICAgICAgICAgJzInOiAnUG9zdGxvZ2luJyxcclxuICAgICAgICAgICAgJzMnOiAnR2VuZXJhbCdcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTm90aWNlTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0Tm90aWNlTGlzdChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0Tm90aWNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB2YXIgbm90aWNlTGlzdCA9IFtdO1xyXG4gICAgICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICAgICAgLy8gVmVyaWZ5IHRoZSBMaXN0IHJldHVybnMgd2hhdCB3ZSBleHBlY3QgYW5kIHdlIGNvbnZlcnQgaXQgdG8gYW4gQXJyYXkgdmFsdWVcclxuICAgICAgICAgICAgICAgIGlmKGRhdGEgJiYgZGF0YS5ub3RpY2VzKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgbm90aWNlTGlzdCA9IGRhdGEubm90aWNlcztcclxuICAgICAgICAgICAgICAgICAgICBpZiAobm90aWNlTGlzdCAmJiBub3RpY2VMaXN0Lmxlbmd0aCA+IDApIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBub3RpY2VMaXN0Lmxlbmd0aDsgaSA9IGkgKyAxKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBub3RpY2VMaXN0W2ldLnR5cGUgPSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IG5vdGljZUxpc3RbaV0udHlwZUlkLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6IHRoaXMuVFlQRVtub3RpY2VMaXN0W2ldLnR5cGVJZF1cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBkZWxldGUgbm90aWNlTGlzdFtpXS50eXBlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5sb2cuZXJyb3IoJ0Vycm9yIHBhcnNpbmcgdGhlIE5vdGljZSBMaXN0JywgZSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKG5vdGljZUxpc3QpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IE5vdGljZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGNyZWF0ZU5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE5vdGljZSBzaG91bGQgaGF2ZSB0aGUgSUQgaW4gb3JkZXIgdG8gZWRpdCB0aGUgTm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZWRpdE5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmVkaXROb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBOb3RpY2Ugc2hvdWxkIGhhdmUgdGhlIElEIGluIG9yZGVyIHRvIGRlbGV0ZSB0aGUgbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZGVsZXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmRlbGV0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5pbXBvcnQgVGFza01hbmFnZXJTZXJ2aWNlIGZyb20gJy4vc2VydmljZS9UYXNrTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJDb250cm9sbGVyIGZyb20gJy4vbGlzdC9UYXNrTWFuYWdlckNvbnRyb2xsZXIuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJFZGl0IGZyb20gJy4vZWRpdC9UYXNrTWFuYWdlckVkaXQuanMnO1xyXG5cclxuLy8gRGlyZWN0aXZlc1xyXG5pbXBvcnQgU1ZHTG9hZGVyIGZyb20gJy4uLy4uL2RpcmVjdGl2ZXMvU3ZnL3N2Z0xvYWRlci5qcydcclxuXHJcbnZhciBUYXNrTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5UYXNrTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyKSB7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICdhcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyQ29udGFpbmVyLmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ3Rhc2tMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTXkgVGFzayBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ1Rhc2sgTWFuYWdlciddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy90YXNrL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJ2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2xpc3QvVGFza01hbmFnZXJDb250YWluZXIuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyQ29udHJvbGxlciBhcyB0YXNrTWFuYWdlcidcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCd0YXNrTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgVGFza01hbmFnZXJTZXJ2aWNlXSk7XHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5UYXNrTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdUYXNrTWFuYWdlckNvbnRyb2xsZXInLCBbJyRsb2cnLCAndGFza01hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsIFRhc2tNYW5hZ2VyQ29udHJvbGxlcl0pO1xyXG5UYXNrTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdUYXNrTWFuYWdlckVkaXQnLCBbJyRsb2cnLCBUYXNrTWFuYWdlckVkaXRdKTtcclxuXHJcbi8vIERpcmVjdGl2ZXNcclxuVGFza01hbmFnZXJNb2R1bGUuZGlyZWN0aXZlKCdzdmdMb2FkZXInLCBTVkdMb2FkZXIpO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgVGFza01hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAzLzExLzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJFZGl0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCB0YXNrTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG5cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIwLzIwMTUuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlckNvbnRyb2xsZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHRhc2tNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5tb2R1bGUgPSAnVGFza01hbmFnZXInO1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlID0gdGFza01hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudGFza0dyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5ldmVudERhdGFTb3VyY2UgPSBbXTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCBDbGFzc1xyXG4gICAgICAgIHRoaXMuZ2V0RXZlbnREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1Rhc2tNYW5hZ2VyIENvbnRyb2xsZXIgSW5zdGFuY2VkJyk7XHJcblxyXG4gICAgfVxyXG5cclxuICAgIG9wZW5Nb2RhbERlbW8oKSB7XHJcblxyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJ2FwcC1qcy9tb2R1bGVzL3Rhc2tNYW5hZ2VyL2VkaXQvVGFza01hbmFnZXJFZGl0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnVGFza01hbmFnZXJFZGl0JyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgaXRlbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4gWycxJywnYTInLCdnZyddO1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKHNlbGVjdGVkSXRlbSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmRlYnVnKHNlbGVjdGVkSXRlbSk7XHJcbiAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdNb2RhbCBkaXNtaXNzZWQgYXQ6ICcgKyBuZXcgRGF0ZSgpKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMudGFza0dyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBncm91cGFibGU6IHRydWUsXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFt7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGFzaycsIHRpdGxlOiAnVGFzayd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZGVzY3JpcHRpb24nLCB0aXRsZTogJ0Rlc2NyaXB0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldE5hbWUnLCB0aXRsZTogJ0Fzc2V0IE5hbWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Fzc2V0VHlwZScsIHRpdGxlOiAnQXNzZXQgVHlwZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndXBkYXRlZCcsIHRpdGxlOiAnVXBkYXRlZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZHVlJywgdGl0bGU6ICdEdWUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NpZ25lZFRvJywgdGl0bGU6ICdBc3NpZ25lZCBUbyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndGVhbScsIHRpdGxlOiAnVGVhbSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2F0ZWdvcnknLCB0aXRsZTogJ0NhdGVnb3J5J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdWMnLCB0aXRsZTogJ1N1Yy4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Njb3JlJywgdGl0bGU6ICdTY29yZSd9XSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy50YXNrTWFuYWdlclNlcnZpY2UudGVzdFNlcnZpY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRFdmVudERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5ldmVudERhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHtldmVudElkOiAxLCBldmVudE5hbWU6ICdBbGwnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDIsIGV2ZW50TmFtZTogJ0J1aWxkb3V0J30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiAzLCBldmVudE5hbWU6ICdEUi1FUCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogNCwgZXZlbnROYW1lOiAnTTEtUGh5c2ljYWwnfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgb25FcnJvckhhcHBlbnMoKSB7XHJcbiAgICAgICAgdGhpcy50YXNrTWFuYWdlclNlcnZpY2UuZmFpbENhbGwoZnVuY3Rpb24gKCkge1xyXG5cclxuICAgICAgICB9KTtcclxuICAgIH1cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMjIvMDcvMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCBSZXN0U2VydmljZUhhbmRsZXIpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IFJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuXHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1Rhc2tNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBmYWlsQ2FsbChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuUmVzb3VyY2VTZXJ2aWNlSGFuZGxlcigpLmdldFNWRygpO1xyXG4gICAgfVxyXG5cclxuICAgIHRlc3RTZXJ2aWNlKGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5UYXNrU2VydmljZUhhbmRsZXIoKS5nZXRGZWVkcygoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjMvMjAxNS5cclxuICogSW1wbGVtZW50cyBSWCBPYnNlcnZhYmxlIHRvIGRpc3Bvc2UgYW5kIHRyYWNrIGJldHRlciBlYWNoIGNhbGwgdG8gdGhlIHNlcnZlclxyXG4gKiBUaGUgT2JzZXJ2ZXIgc3Vic2NyaWJlIGEgcHJvbWlzZS5cclxuICovXHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdEhhbmRsZXIge1xyXG4gICAgY29uc3RydWN0b3IocngpIHtcclxuICAgICAgICB0aGlzLnJ4ID0gcng7XHJcbiAgICAgICAgdGhpcy5wcm9taXNlID0gW107XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDYWxsZWQgZnJvbSBSZXN0U2VydmljZUhhbmRsZXIuc3Vic2NyaWJlUmVxdWVzdFxyXG4gICAgICogaXQgdmVyaWZ5IHRoYXQgdGhlIGNhbGwgaXMgYmVpbmcgZG9uZSB0byB0aGUgc2VydmVyIGFuZCByZXR1cm4gYSBwcm9taXNlXHJcbiAgICAgKiBAcGFyYW0gcmVxdWVzdFxyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIHN1YnNjcmliZVJlcXVlc3QocmVxdWVzdCwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcbiAgICAgICAgdmFyIHJ4T2JzZXJ2YWJsZSA9IHRoaXMucnguT2JzZXJ2YWJsZS5mcm9tUHJvbWlzZShyZXF1ZXN0KTtcclxuICAgICAgICAvLyBWZXJpZnkgaXMgbm90IGEgZHVwbGljYXRlIGNhbGxcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICB0aGlzLmNhbmNlbFJlcXVlc3QocnhPYnNlcnZhYmxlKTtcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIC8vIFN1YnNjcmliZSB0aGUgcmVxdWVzdFxyXG4gICAgICAgIHZhciByZXN1bHRTdWJzY3JpYmUgPSB0aGlzLmFkZFN1YnNjcmliZShyeE9ic2VydmFibGUsIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgaWYgKHJlc3VsdFN1YnNjcmliZSAmJiByZXN1bHRTdWJzY3JpYmUuaXNTdG9wcGVkKSB7XHJcbiAgICAgICAgICAgIC8vIEFuIGVycm9yIGhhcHBlbnMsIHRyYWNrZWQgYnkgSHR0cEludGVyY2VwdG9ySW50ZXJmYWNlXHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgYWRkU3Vic2NyaWJlKHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcbiAgICAgICAgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF0gPSByeE9ic2VydmFibGUuc3Vic2NyaWJlKFxyXG4gICAgICAgICAgICAocmVzcG9uc2UpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLm9uU3Vic2NyaWJlZFN1Y2Nlc3MocmVzcG9uc2UsIHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgKGVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5vblN1YnNjcmliZWRFcnJvcihlcnJvciwgcnhPYnNlcnZhYmxlLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSwgKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgLy8gTk8tT1AgU3Vic2NyaWJlIGNvbXBsZXRlZFxyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgcmV0dXJuIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgfVxyXG5cclxuICAgIGNhbmNlbFJlcXVlc3QocnhPYnNlcnZhYmxlKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgICAgICByeE9ic2VydmFibGUuZGlzcG9zZSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBpc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSB7XHJcbiAgICAgICAgcmV0dXJuIChyeE9ic2VydmFibGUgJiYgcnhPYnNlcnZhYmxlLl9wICYmIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdKTtcclxuICAgIH1cclxuXHJcbiAgICBvblN1YnNjcmliZWRTdWNjZXNzKHJlc3BvbnNlLCByeE9ic2VydmFibGUsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICAgICAgaWYob25TdWNjZXNzKXtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhyZXNwb25zZS5kYXRhKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaHJvd3MgaW1tZWRpYXRlbHkgZXJyb3Igd2hlbiB0aGUgcGV0aXRpb24gY2FsbCBpcyB3cm9uZ1xyXG4gICAgICogb3Igd2l0aCBhIGRlbGF5IGlmIHRoZSBjYWxsIGlzIHZhbGlkXHJcbiAgICAgKiBAcGFyYW0gZXJyb3JcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICBvblN1YnNjcmliZWRFcnJvcihlcnJvciwgcnhPYnNlcnZhYmxlLCBvbkVycm9yKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgICAgICBpZihvbkVycm9yKXtcclxuICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3Ioe30pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgUmVzdFNlcnZpY2VIYW5kbGVyIGZyb20gJy4vUmVzdFNlcnZpY2VIYW5kbGVyLmpzJztcclxuXHJcbnZhciBSZXN0QVBJTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLlJlc3RBUElNb2R1bGUnLFtdKTtcclxuXHJcblJlc3RBUElNb2R1bGUuc2VydmljZSgnUmVzdFNlcnZpY2VIYW5kbGVyJywgWyckbG9nJywgJyRodHRwJywgJyRyZXNvdXJjZScsICdyeCcsIFJlc3RTZXJ2aWNlSGFuZGxlcl0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgUmVzdEFQSU1vZHVsZTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8wOC8xNS5cclxuICogSXQgYWJzdHJhY3QgZWFjaCBvbmUgb2YgdGhlIGV4aXN0aW5nIGNhbGwgdG8gdGhlIEFQSSwgaXQgc2hvdWxkIG9ubHkgY29udGFpbnMgdGhlIGNhbGwgZnVuY3Rpb25zIGFuZCByZWZlcmVuY2VcclxuICogdG8gdGhlIGNhbGxiYWNrLCBubyBsb2dpYyBhdCBhbGwuXHJcbiAqXHJcbiAqL1xyXG5cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBSZXF1ZXN0SGFuZGxlciBmcm9tICcuL1JlcXVlc3RIYW5kbGVyLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlc3RTZXJ2aWNlSGFuZGxlciB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkaHR0cCwgJHJlc291cmNlLCByeCkge1xyXG4gICAgICAgIHRoaXMucnggPSByeDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5odHRwID0gJGh0dHA7XHJcbiAgICAgICAgdGhpcy5yZXNvdXJjZSA9ICRyZXNvdXJjZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVIZWFkZXJzKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1Jlc3RTZXJ2aWNlIExvYWRlZCcpO1xyXG4gICAgICAgIHRoaXMucmVxID0ge1xyXG4gICAgICAgICAgICBtZXRob2Q6ICcnLFxyXG4gICAgICAgICAgICB1cmw6ICcnLFxyXG4gICAgICAgICAgICBoZWFkZXJzOiB7XHJcbiAgICAgICAgICAgICAgICAnQ29udGVudC1UeXBlJzogJ2FwcGxpY2F0aW9uL2pzb247Y2hhcnNldD1VVEYtOCdcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGF0YTogW11cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVIZWFkZXJzKCkge1xyXG4gICAgICAgIHRoaXMuaHR0cC5kZWZhdWx0cy5oZWFkZXJzLnBvc3RbJ0NvbnRlbnQtVHlwZSddID0gJ2FwcGxpY2F0aW9uL3gtd3d3LWZvcm0tdXJsZW5jb2RlZCc7XHJcbiAgICB9XHJcblxyXG4gICAgVGFza1NlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldEZlZWRzOiAoY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgndGVzdC9tb2NrdXBEYXRhL1Rhc2tNYW5hZ2VyL3Rhc2tNYW5hZ2VyTGlzdC5qc29uJyksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgbGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgZ2V0TGljZW5zZTogKGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZXMnKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldFByb2plY3REYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9wcm9qZWN0JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdDogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYXBwbHlMaWNlbnNlOiAgKGxpY2Vuc2VJZCwgZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvbG9hZCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAvLy0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAgICAgICAgICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3Q6IChkYXRhLCBjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy8/Pz8nO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLnBvc3QoJy4uL3Rlc3QvbW9ja3VwRGF0YS9MaWNlbnNlQWRtaW4vbGljZW5zZUFkbWluTGlzdC5qc29uJywgZGF0YSksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZW1haWxSZXF1ZXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRlbGV0ZUxpY2Vuc2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdERUxFVEUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBsaWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAoZGF0YSwgb25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzYXZlTGljZW5zZTogKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICByZXF1ZXN0SW1wb3J0OiAgKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICByZXZva2VMaWNlbnNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGVMaWNlbnNlOiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIG5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXROb3RpY2VMaXN0OiAob25TdWNjZXNzKSA9PiB7IC8vIHJlYWwgd3MgZXhhbXBsZVxyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL25vdGljZXMnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0Tm90aWNlTW9ja1VwOiAob25TdWNjZXNzKSA9PiB7IC8vIE1vY2t1cCBEYXRhIGZvciB0ZXN0aW5nIHNlZSB1cmxcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi90ZXN0L21vY2t1cERhdGEvTm90aWNlTWFuYWdlci9ub3RpY2VNYW5hZ2VyTGlzdC5qc29uJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNyZWF0ZU5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9ub3RpY2VzJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGVkaXROb3RpY2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQVVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9ub3RpY2VzLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRlbGV0ZU5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKiBFUzYgSW50ZXJjZXB0b3IgY2FsbHMgaW5uZXIgbWV0aG9kcyBpbiBhIGdsb2JhbCBzY29wZSwgdGhlbiB0aGUgXCJ0aGlzXCIgaXMgYmVpbmcgbG9zdFxyXG4gKiBpbiB0aGUgZGVmaW5pdGlvbiBvZiB0aGUgQ2xhc3MgZm9yIGludGVyY2VwdG9ycyBvbmx5XHJcbiAqIFRoaXMgaXMgYSBpbnRlcmZhY2UgdGhhdCB0YWtlIGNhcmUgb2YgdGhlIGlzc3VlLlxyXG4gKi9cclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCAvKiBpbnRlcmZhY2UqLyBjbGFzcyBIdHRwSW50ZXJjZXB0b3Ige1xyXG4gICAgY29uc3RydWN0b3IobWV0aG9kVG9CaW5kKSB7XHJcbiAgICAgICAgLy8gSWYgbm90IG1ldGhvZCB0byBiaW5kLCB3ZSBhc3N1bWUgb3VyIGludGVyY2VwdG9yIGlzIHVzaW5nIGFsbCB0aGUgaW5uZXIgZnVuY3Rpb25zXHJcbiAgICAgICAgaWYoIW1ldGhvZFRvQmluZCkge1xyXG4gICAgICAgICAgICBbJ3JlcXVlc3QnLCAncmVxdWVzdEVycm9yJywgJ3Jlc3BvbnNlJywgJ3Jlc3BvbnNlRXJyb3InXVxyXG4gICAgICAgICAgICAgICAgLmZvckVhY2goKG1ldGhvZCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKHRoaXNbbWV0aG9kXSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzW21ldGhvZF0gPSB0aGlzW21ldGhvZF0uYmluZCh0aGlzKTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAvLyBtZXRob2RUb0JpbmQgcmVmZXJlbmNlIHRvIGEgc2luZ2xlIGNoaWxkIGNsYXNzXHJcbiAgICAgICAgICAgIHRoaXNbbWV0aG9kVG9CaW5kXSA9IHRoaXNbbWV0aG9kVG9CaW5kXS5iaW5kKHRoaXMpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICB9XHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKiBVc2UgdGhpcyBtb2R1bGUgdG8gbW9kaWZ5IGFueXRoaW5nIHJlbGF0ZWQgdG8gdGhlIEhlYWRlcnMgYW5kIFJlcXVlc3RcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuXHJcblxyXG52YXIgSFRUUE1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5IVFRQTW9kdWxlJywgWyduZ1Jlc291cmNlJ10pLmNvbmZpZyhbJyRodHRwUHJvdmlkZXInLCBmdW5jdGlvbigkaHR0cFByb3ZpZGVyKXtcclxuXHJcbiAgICAvL2luaXRpYWxpemUgZ2V0IGlmIG5vdCB0aGVyZVxyXG4gICAgaWYgKCEkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0KSB7XHJcbiAgICAgICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldCA9IHt9O1xyXG4gICAgfVxyXG5cclxuICAgIC8vRGlzYWJsZSBJRSBhamF4IHJlcXVlc3QgY2FjaGluZ1xyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnSWYtTW9kaWZpZWQtU2luY2UnXSA9ICdNb24sIDI2IEp1bCAxOTk3IDA1OjAwOjAwIEdNVCc7XHJcbiAgICAvLyBleHRyYVxyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnQ2FjaGUtQ29udHJvbCddID0gJ25vLWNhY2hlJztcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ1ByYWdtYSddID0gJ25vLWNhY2hlJztcclxuXHJcblxyXG4gICAgLy8gSW5qZWN0cyBvdXIgSW50ZXJjZXB0b3JzIGZvciBSZXF1ZXN0XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgLy8gSW5qZWN0cyBvdXIgSW50ZXJjZXB0b3JzIGZvciBSZXNwb25zZVxyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG5cclxuXHJcbn1dKTtcclxuXHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgSFRUUE1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJdCBpbXBsZW1lbnQgYW4gYWJzdHJhY3QgY2FsbCB0byBIVFRQIEludGVyY2VwdG9ycyB0byBtYW5hZ2UgZXJyb3IgaGFuZGxlclxyXG4gKiBTb21ldGltZXMgYSByZXF1ZXN0IGNhbid0IGJlIHNlbnQgb3IgaXQgaXMgcmVqZWN0ZWQgYnkgYW4gaW50ZXJjZXB0b3IuXHJcbiAqIFJlcXVlc3QgZXJyb3IgaW50ZXJjZXB0b3IgY2FwdHVyZXMgcmVxdWVzdHMgdGhhdCBoYXZlIGJlZW4gY2FuY2VsZWQgYnkgYSBwcmV2aW91cyByZXF1ZXN0IGludGVyY2VwdG9yLlxyXG4gKiBJdCBjYW4gYmUgdXNlZCBpbiBvcmRlciB0byByZWNvdmVyIHRoZSByZXF1ZXN0IGFuZCBzb21ldGltZXMgdW5kbyB0aGluZ3MgdGhhdCBoYXZlIGJlZW4gc2V0IHVwIGJlZm9yZSBhIHJlcXVlc3QsXHJcbiAqIGxpa2UgcmVtb3Zpbmcgb3ZlcmxheXMgYW5kIGxvYWRpbmcgaW5kaWNhdG9ycywgZW5hYmxpbmcgYnV0dG9ucyBhbmQgZmllbGRzIGFuZCBzbyBvbi5cclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXF1ZXN0RXJyb3InKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlcXVlc3RFcnJvcihyZWplY3Rpb24pIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvL2lmIChjYW5SZWNvdmVyKHJlamVjdGlvbikpIHtcclxuICAgICAgICAvLyAgICByZXR1cm4gcmVzcG9uc2VPck5ld1Byb21pc2VcclxuICAgICAgICAvL31cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZWplY3Rpb24pO1xyXG5cclxuICAgICAgICByZXR1cm4gdGhpcy5xLnJlamVjdChyZWplY3Rpb24pO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlbkVycm9yKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIEl0IGltcGxlbWVudCBhbiBhYnN0cmFjdCBjYWxsIHRvIEhUVFAgSW50ZXJjZXB0b3JzIHRvIG1hbmFnZSBvbmx5IHJlcXVlc3RcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVxdWVzdCcpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlcXVlc3QoY29uZmlnKSB7XHJcbiAgICAgICAgLy8gV2UgY2FuIGFkZCBoZWFkZXJzIGlmIG9uIHRoZSBpbmNvbWluZyByZXF1ZXN0IG1hZGUgaXQgd2UgaGF2ZSB0aGUgdG9rZW4gaW5zaWRlXHJcbiAgICAgICAgLy8gZGVmaW5lZCBieSBzb21lIGNvbmRpdGlvbnNcclxuICAgICAgICAvL2NvbmZpZy5oZWFkZXJzWyd4LXNlc3Npb24tdG9rZW4nXSA9IG15LnRva2VuO1xyXG5cclxuICAgICAgICBjb25maWcucmVxdWVzdFRpbWVzdGFtcCA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShjb25maWcpO1xyXG5cclxuICAgICAgICByZXR1cm4gY29uZmlnIHx8IHRoaXMucS53aGVuKGNvbmZpZyk7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuUmVxdWVzdCgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJZiBiYWNrZW5kIGNhbGwgZmFpbHMgb3IgaXQgbWlnaHQgYmUgcmVqZWN0ZWQgYnkgYSByZXF1ZXN0IGludGVyY2VwdG9yIG9yIGJ5IGEgcHJldmlvdXMgcmVzcG9uc2UgaW50ZXJjZXB0b3I7XHJcbiAqIEluIHRob3NlIGNhc2VzLCByZXNwb25zZSBlcnJvciBpbnRlcmNlcHRvciBjYW4gaGVscCB1cyB0byByZWNvdmVyIHRoZSBiYWNrZW5kIGNhbGwuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVzcG9uc2VFcnJvcicpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3BvbnNlRXJyb3IocmVqZWN0aW9uKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy9pZiAoY2FuUmVjb3ZlcihyZWplY3Rpb24pKSB7XHJcbiAgICAgICAgLy8gICAgcmV0dXJuIHJlc3BvbnNlT3JOZXdQcm9taXNlXHJcbiAgICAgICAgLy8gfVxyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZWplY3Rpb24pO1xyXG4gICAgICAgIHJldHVybiB0aGlzLnEucmVqZWN0KHJlamVjdGlvbik7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuRXJyb3IoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogVGhpcyBtZXRob2QgaXMgY2FsbGVkIHJpZ2h0IGFmdGVyICRodHRwIHJlY2VpdmVzIHRoZSByZXNwb25zZSBmcm9tIHRoZSBiYWNrZW5kLFxyXG4gKiBzbyB5b3UgY2FuIG1vZGlmeSB0aGUgcmVzcG9uc2UgYW5kIG1ha2Ugb3RoZXIgYWN0aW9ucy4gVGhpcyBmdW5jdGlvbiByZWNlaXZlcyBhIHJlc3BvbnNlIG9iamVjdCBhcyBhIHBhcmFtZXRlclxyXG4gKiBhbmQgaGFzIHRvIHJldHVybiBhIHJlc3BvbnNlIG9iamVjdCBvciBhIHByb21pc2UuIFRoZSByZXNwb25zZSBvYmplY3QgaW5jbHVkZXNcclxuICogdGhlIHJlcXVlc3QgY29uZmlndXJhdGlvbiwgaGVhZGVycywgc3RhdHVzIGFuZCBkYXRhIHRoYXQgcmV0dXJuZWQgZnJvbSB0aGUgYmFja2VuZC5cclxuICogUmV0dXJuaW5nIGFuIGludmFsaWQgcmVzcG9uc2Ugb2JqZWN0IG9yIHByb21pc2UgdGhhdCB3aWxsIGJlIHJlamVjdGVkLCB3aWxsIG1ha2UgdGhlICRodHRwIGNhbGwgdG8gZmFpbC5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVzcG9uc2UnKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzcG9uc2UocmVzcG9uc2UpIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gc3VjY2Vzc1xyXG5cclxuICAgICAgICByZXNwb25zZS5jb25maWcucmVzcG9uc2VUaW1lc3RhbXAgPSBuZXcgRGF0ZSgpLmdldFRpbWUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVzcG9uc2UpO1xyXG4gICAgICAgIHJldHVybiByZXNwb25zZSB8fCB0aGlzLnEud2hlbihyZXNwb25zZSk7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuUmVzcG9uc2UoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxufVxyXG5cclxuIl19
