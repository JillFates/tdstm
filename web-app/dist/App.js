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
LicenseAdminModule.controller('ApplyLicenseKey', ['$log', 'LicenseAdminService', '$uibModalInstance', '$rootScope', 'params', _ApplyLicenseKey2.default]);
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
            environment: params.license.environment,
            inception: params.license.inception,
            expiration: params.license.expiration,
            specialInstructions: params.license.specialInstructions,
            applied: params.license.applied,
            keyId: params.license.keyId,
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
            this.licenseGridOptions = {
                toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="licenseAdminList.onRequestNewLicense()"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Request New License</button> <div ng-click="licenseManagerList.reloadLicenseAdminList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
                pageable: {
                    refresh: true,
                    pageSizes: true,
                    buttonCount: 5
                },
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'client', title: 'Client' }, { field: 'project', title: 'Project' }, { field: 'contact_email', title: 'Contact Email' }, { field: 'status', title: 'Status' }, { field: 'type', title: 'Type' }, { field: 'method.name', title: 'Method' }, { field: 'method.id', hidden: true }, { field: 'servers_tokens', title: 'Server/Tokens' }, { field: 'inception', title: 'Inception' }, { field: 'expiration', title: 'Expiration' }, { field: 'environment', title: 'Env.' }],
                dataSource: {
                    pageSize: 10,
                    transport: {
                        read: function read(e) {
                            /*this.licenseAdminService.testService((data) => {*/
                            var data = [{
                                licenseId: 1,
                                keyId: 'ce42cfd1-1ac5-4fcc-be5c-cc7885c8f83b',
                                action: '',
                                client: 'n/a',
                                project: 'n/a',
                                contact_email: 'west.coast@xyyy.com',
                                status: 'Active',
                                type: 'Multi-Project',
                                method: {
                                    id: 1,
                                    name: 'Server'
                                },
                                servers_tokens: '8000',
                                inception: '2016-09-15',
                                expiration: '2016-12-01',
                                environment: 'Production',
                                specialInstructions: 'Help, Help, Help',
                                applied: false,
                                replaced: {
                                    date: new Date(),
                                    serverUrl: 'http:blablaba.com',
                                    name: 'aasdas54-5asd4a5sd-asd45a4sd'
                                },
                                encryptedDetail: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618'
                            }, {
                                licenseId: 2,
                                keyId: 'df42dge2-2bd6-5gdd-cf6d-dd8996d9g94c',
                                action: '',
                                client: 'Acme Inc.',
                                project: 'DR Relo',
                                contact_email: 'jim.laucher@acme.com',
                                status: 'Pending',
                                type: 'Project',
                                method: {
                                    id: 2,
                                    name: 'Token'
                                },
                                servers_tokens: '15000',
                                inception: '2016-09-01',
                                expiration: '2016-10-01',
                                environment: 'Demo',
                                specialInstructions: '',
                                applied: true,
                                replaced: {
                                    date: new Date(),
                                    serverUrl: 'http:blablaba.com',
                                    name: 'basfasd-2aphgosdf-asoqweqwe'
                                },
                                encryptedDetail: 'asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618asdasdasd4as56da6sd46325e4q65asd4a65sd4a65sd4as65d4864286e41286e41682e453a4sd5as4d6a8s4d61284d12684d61824d6184d61824d126d426184d6182d46182d2618'
                            }];
                            e.success(data);
                            /* });*/
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
            var _this = this;

            var modalInstance = this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseAdmin/request/RequestLicense.html',
                controller: 'RequestLicense as requestLicense',
                size: 'md',
                draggable: true
            });

            modalInstance.result.then(function (license) {
                _this.log.info('New License Created: ', license);
                _this.onNewLicenseCreated(license);
                _this.reloadLicenseAdminList();
            }, function () {
                _this.log.info('Request Canceled.');
            });
        }

        /**
         * After clicking on edit, we redirect the user to the Edition screen instead of open a dialog
         * du the size of the inputs
         */

    }, {
        key: 'onLicenseDetails',
        value: function onLicenseDetails(license) {
            var _this2 = this;

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

            modalInstance.result.then(function () {}, function () {
                _this2.log.info('Request Canceled.');
            });
        }
    }, {
        key: 'onNewLicenseCreated',
        value: function onNewLicenseCreated() {
            this.uibModal.open({
                animation: true,
                templateUrl: '../app-js/modules/licenseAdmin/created/CreatedLicense.html',
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
            contactEmail: '',
            environmentId: 0,
            projectId: 0,
            clientName: '',
            specialInstructions: ''
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
                _this3.uibModalInstance.close(data);
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
        this.log.debug('licenseAdminService Instanced');
    }

    _createClass(LicenseAdminService, [{
        key: 'getEnvironmentDataSource',
        value: function getEnvironmentDataSource(onSuccess) {
            this.restService.licenseAdminServiceHandler().getEnvironmentDataSource(function (data) {
                return onSuccess(data);
            });
        }
    }, {
        key: 'getProjectDataSource',
        value: function getProjectDataSource(onSuccess) {
            this.restService.licenseAdminServiceHandler().getProjectDataSource(function (data) {
                return onSuccess(data);
            });
        }
    }, {
        key: 'testService',
        value: function testService(callback) {
            this.restService.licenseAdminServiceHandler().getLicense(function (data) {
                return callback(data);
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
    }, {
        key: 'getLicenseList',
        value: function getLicenseList(callback) {
            this.restService.licenseAdminServiceHandler().getLicenseList(function (data) {
                return callback(data);
            });
        }
    }, {
        key: 'applyLicense',
        value: function applyLicense(license, callback) {
            var _this3 = this;

            this.restService.licenseAdminServiceHandler().applyLicense(license, function (data) {
                //if(data.applied) {
                _this3.rootScope.$emit('broadcast-msg', { type: 'info', text: 'License was successfully applied' });
                /*} else {
                    this.rootScope.$emit('broadcast-msg', { type: 'warning', text: 'License was successfully applied'});
                }*/
                return callback(data);
            });
        }
    }, {
        key: 'deleteLicense',
        value: function deleteLicense(license, callback) {
            this.restService.licenseAdminServiceHandler().deleteLicense(license, function (data) {
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
            this.restService.licenseAdminServiceHandler().createNewLicenseRequest(newLicense, function (data) {
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
                getEnvironmentDataSource: function getEnvironmentDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/license/environment'), onSuccess);
                },
                getProjectDataSource: function getProjectDataSource(onSuccess) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/license/project'), onSuccess);
                },
                getLicenseList: function getLicenseList(data, onSuccess) {
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../ws/license'), onSuccess);
                },
                getLicense: function getLicense(callback) {
                    // Mockup Data for testing see url
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.get('../test/mockupData/LicenseAdmin/licenseAdminList.json'), callback);
                },
                createNewLicenseRequest: function createNewLicenseRequest(data, callback) {
                    _this2.req.method = 'POST';
                    _this2.req.url = '../ws/???';
                    _this2.req.data = data;
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
                },
                applyLicense: function applyLicense(data, callback) {
                    _this2.req.method = 'POST';
                    _this2.req.url = '../ws/???';
                    _this2.req.data = data;
                    return new _RequestHandler2.default(_this2.rx).subscribeRequest(_this2.http.post('../test/mockupData/LicenseAdmin/licenseAdminList.json', data), callback);
                },
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvY2xpcGJvYXJkL2xpYi9jbGlwYm9hcmQtYWN0aW9uLmpzIiwibm9kZV9tb2R1bGVzL2NsaXBib2FyZC9saWIvY2xpcGJvYXJkLmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9jbG9zZXN0LmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9kZWxlZ2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9pcy5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9saXN0ZW4uanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvZGlzdC9uZ2NsaXBib2FyZC5qcyIsIm5vZGVfbW9kdWxlcy9zZWxlY3Qvc3JjL3NlbGVjdC5qcyIsIm5vZGVfbW9kdWxlcy90aW55LWVtaXR0ZXIvaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxjb25maWdcXEFwcC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcU3ZnXFxzdmdMb2FkZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXFRvb2xzXFxNb2RhbFdpbmRvd0FjdGl2YXRpb24uanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXFRvb2xzXFxUb2FzdEhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXGluZGV4LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtYWluLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxkaWFsb2dBY3Rpb25cXERpYWxvZ0FjdGlvbi5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcaGVhZGVyXFxIZWFkZXJDb250cm9sbGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxoZWFkZXJcXEhlYWRlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxMaWNlbnNlQWRtaW5Nb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcYXBwbHlMaWNlbnNlS2V5XFxBcHBseUxpY2Vuc2VLZXkuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcY3JlYXRlZFxcQ3JlYXRlZExpY2Vuc2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcZGV0YWlsXFxMaWNlbnNlRGV0YWlsLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXGxpc3RcXExpY2Vuc2VBZG1pbkxpc3QuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcbWFudWFsbHlSZXF1ZXN0XFxNYW51YWxseVJlcXVlc3QuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxccmVxdWVzdFxcUmVxdWVzdExpY2Vuc2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcc2VydmljZVxcTGljZW5zZUFkbWluU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXExpY2Vuc2VNYW5hZ2VyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcZGV0YWlsXFxMaWNlbnNlTWFuYWdlckRldGFpbC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXGxpc3RcXExpY2Vuc2VNYW5hZ2VyTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXHJlcXVlc3RJbXBvcnRcXFJlcXVlc3RJbXBvcnQuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxzZXJ2aWNlXFxMaWNlbnNlTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXE5vdGljZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXGVkaXRcXEVkaXROb3RpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXGxpc3RcXE5vdGljZUxpc3QuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXG5vdGljZU1hbmFnZXJcXHNlcnZpY2VcXE5vdGljZU1hbmFnZXJTZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFx0YXNrTWFuYWdlclxcVGFza01hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxlZGl0XFxUYXNrTWFuYWdlckVkaXQuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxsaXN0XFxUYXNrTWFuYWdlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxzZXJ2aWNlXFxUYXNrTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RBUElNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXN0U2VydmljZUhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwT0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4TEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL0ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNB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WCxBQUFrQixBQUNsQjttQkFBTyxLQUFBLEFBQUssRUFBTCxBQUFPLE9BQWQsQUFBTyxBQUFjLEFBQ3hCOzs7O3NDQUVhLEFBQ1Y7bUJBQU8sS0FBQSxBQUFLLE1BQVosQUFBa0IsQUFDckI7Ozs7O0dBckI0RCxjOztrQixBQUE1Qzs7O0FDVnJCOzs7Ozs7Ozs7QUFTQTs7Ozs7Ozs7Ozs7Ozs7Ozs7O0FBRUE7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7O0ksQUFFcUI7OENBQ2pCOzs0Q0FBQSxBQUFZLE1BQVosQUFBa0IsSUFBbEIsQUFBc0IsSUFBSTs4QkFBQTs7b0tBQUEsQUFDaEIsQUFDTjs7Y0FBQSxBQUFLLE1BQUwsQUFBVyxBQUNYO2NBQUEsQUFBSyxJQUFMLEFBQVMsQUFDVDtjQUFBLEFBQUssUUFBUSxNQUFBLEFBQUssRUFBbEIsQUFBYSxBQUFPLEFBQ3BCO2NBQUEsQUFBSyxJQUFMLEFBQVMsTUFMYSxBQUt0QixBQUFlO2VBQ2xCOzs7OztpQyxBQUVRLFdBQVUsQUFDZjtBQUVBOztzQkFBQSxBQUFTLE9BQVQsQUFBZ0Isb0JBQW9CLElBQUEsQUFBSSxPQUF4QyxBQUFvQyxBQUFXLEFBRS9DOztpQkFBQSxBQUFLLE1BQUwsQUFBVyxPQUFYLEFBQWtCLEFBQ2xCO21CQUFPLGFBQVksS0FBQSxBQUFLLEVBQUwsQUFBTyxLQUExQixBQUFtQixBQUFZLEFBQ2xDOzs7O3lDQUVnQixBQUNiO21CQUFPLEtBQUEsQUFBSyxNQUFaLEFBQWtCLEFBQ3JCOzs7OztHQXBCdUQsYzs7a0IsQUFBdkMiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwiKGZ1bmN0aW9uIChnbG9iYWwsIGZhY3RvcnkpIHtcbiAgICBpZiAodHlwZW9mIGRlZmluZSA9PT0gXCJmdW5jdGlvblwiICYmIGRlZmluZS5hbWQpIHtcbiAgICAgICAgZGVmaW5lKFsnbW9kdWxlJywgJ3NlbGVjdCddLCBmYWN0b3J5KTtcbiAgICB9IGVsc2UgaWYgKHR5cGVvZiBleHBvcnRzICE9PSBcInVuZGVmaW5lZFwiKSB7XG4gICAgICAgIGZhY3RvcnkobW9kdWxlLCByZXF1aXJlKCdzZWxlY3QnKSk7XG4gICAgfSBlbHNlIHtcbiAgICAgICAgdmFyIG1vZCA9IHtcbiAgICAgICAgICAgIGV4cG9ydHM6IHt9XG4gICAgICAgIH07XG4gICAgICAgIGZhY3RvcnkobW9kLCBnbG9iYWwuc2VsZWN0KTtcbiAgICAgICAgZ2xvYmFsLmNsaXBib2FyZEFjdGlvbiA9IG1vZC5leHBvcnRzO1xuICAgIH1cbn0pKHRoaXMsIGZ1bmN0aW9uIChtb2R1bGUsIF9zZWxlY3QpIHtcbiAgICAndXNlIHN0cmljdCc7XG5cbiAgICB2YXIgX3NlbGVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9zZWxlY3QpO1xuXG4gICAgZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHtcbiAgICAgICAgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHtcbiAgICAgICAgICAgIGRlZmF1bHQ6IG9ialxuICAgICAgICB9O1xuICAgIH1cblxuICAgIHZhciBfdHlwZW9mID0gdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIHR5cGVvZiBTeW1ib2wuaXRlcmF0b3IgPT09IFwic3ltYm9sXCIgPyBmdW5jdGlvbiAob2JqKSB7XG4gICAgICAgIHJldHVybiB0eXBlb2Ygb2JqO1xuICAgIH0gOiBmdW5jdGlvbiAob2JqKSB7XG4gICAgICAgIHJldHVybiBvYmogJiYgdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIG9iai5jb25zdHJ1Y3RvciA9PT0gU3ltYm9sICYmIG9iaiAhPT0gU3ltYm9sLnByb3RvdHlwZSA/IFwic3ltYm9sXCIgOiB0eXBlb2Ygb2JqO1xuICAgIH07XG5cbiAgICBmdW5jdGlvbiBfY2xhc3NDYWxsQ2hlY2soaW5zdGFuY2UsIENvbnN0cnVjdG9yKSB7XG4gICAgICAgIGlmICghKGluc3RhbmNlIGluc3RhbmNlb2YgQ29uc3RydWN0b3IpKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKFwiQ2Fubm90IGNhbGwgYSBjbGFzcyBhcyBhIGZ1bmN0aW9uXCIpO1xuICAgICAgICB9XG4gICAgfVxuXG4gICAgdmFyIF9jcmVhdGVDbGFzcyA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgZnVuY3Rpb24gZGVmaW5lUHJvcGVydGllcyh0YXJnZXQsIHByb3BzKSB7XG4gICAgICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IHByb3BzLmxlbmd0aDsgaSsrKSB7XG4gICAgICAgICAgICAgICAgdmFyIGRlc2NyaXB0b3IgPSBwcm9wc1tpXTtcbiAgICAgICAgICAgICAgICBkZXNjcmlwdG9yLmVudW1lcmFibGUgPSBkZXNjcmlwdG9yLmVudW1lcmFibGUgfHwgZmFsc2U7XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5jb25maWd1cmFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIGlmIChcInZhbHVlXCIgaW4gZGVzY3JpcHRvcikgZGVzY3JpcHRvci53cml0YWJsZSA9IHRydWU7XG4gICAgICAgICAgICAgICAgT2JqZWN0LmRlZmluZVByb3BlcnR5KHRhcmdldCwgZGVzY3JpcHRvci5rZXksIGRlc2NyaXB0b3IpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGZ1bmN0aW9uIChDb25zdHJ1Y3RvciwgcHJvdG9Qcm9wcywgc3RhdGljUHJvcHMpIHtcbiAgICAgICAgICAgIGlmIChwcm90b1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLnByb3RvdHlwZSwgcHJvdG9Qcm9wcyk7XG4gICAgICAgICAgICBpZiAoc3RhdGljUHJvcHMpIGRlZmluZVByb3BlcnRpZXMoQ29uc3RydWN0b3IsIHN0YXRpY1Byb3BzKTtcbiAgICAgICAgICAgIHJldHVybiBDb25zdHJ1Y3RvcjtcbiAgICAgICAgfTtcbiAgICB9KCk7XG5cbiAgICB2YXIgQ2xpcGJvYXJkQWN0aW9uID0gZnVuY3Rpb24gKCkge1xuICAgICAgICAvKipcbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG4gICAgICAgIGZ1bmN0aW9uIENsaXBib2FyZEFjdGlvbihvcHRpb25zKSB7XG4gICAgICAgICAgICBfY2xhc3NDYWxsQ2hlY2sodGhpcywgQ2xpcGJvYXJkQWN0aW9uKTtcblxuICAgICAgICAgICAgdGhpcy5yZXNvbHZlT3B0aW9ucyhvcHRpb25zKTtcbiAgICAgICAgICAgIHRoaXMuaW5pdFNlbGVjdGlvbigpO1xuICAgICAgICB9XG5cbiAgICAgICAgLyoqXG4gICAgICAgICAqIERlZmluZXMgYmFzZSBwcm9wZXJ0aWVzIHBhc3NlZCBmcm9tIGNvbnN0cnVjdG9yLlxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cblxuXG4gICAgICAgIF9jcmVhdGVDbGFzcyhDbGlwYm9hcmRBY3Rpb24sIFt7XG4gICAgICAgICAgICBrZXk6ICdyZXNvbHZlT3B0aW9ucycsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gcmVzb2x2ZU9wdGlvbnMoKSB7XG4gICAgICAgICAgICAgICAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoID4gMCAmJiBhcmd1bWVudHNbMF0gIT09IHVuZGVmaW5lZCA/IGFyZ3VtZW50c1swXSA6IHt9O1xuXG4gICAgICAgICAgICAgICAgdGhpcy5hY3Rpb24gPSBvcHRpb25zLmFjdGlvbjtcbiAgICAgICAgICAgICAgICB0aGlzLmVtaXR0ZXIgPSBvcHRpb25zLmVtaXR0ZXI7XG4gICAgICAgICAgICAgICAgdGhpcy50YXJnZXQgPSBvcHRpb25zLnRhcmdldDtcbiAgICAgICAgICAgICAgICB0aGlzLnRleHQgPSBvcHRpb25zLnRleHQ7XG4gICAgICAgICAgICAgICAgdGhpcy50cmlnZ2VyID0gb3B0aW9ucy50cmlnZ2VyO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RlZFRleHQgPSAnJztcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnaW5pdFNlbGVjdGlvbicsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gaW5pdFNlbGVjdGlvbigpIHtcbiAgICAgICAgICAgICAgICBpZiAodGhpcy50ZXh0KSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0RmFrZSgpO1xuICAgICAgICAgICAgICAgIH0gZWxzZSBpZiAodGhpcy50YXJnZXQpIHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RUYXJnZXQoKTtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3NlbGVjdEZha2UnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHNlbGVjdEZha2UoKSB7XG4gICAgICAgICAgICAgICAgdmFyIF90aGlzID0gdGhpcztcblxuICAgICAgICAgICAgICAgIHZhciBpc1JUTCA9IGRvY3VtZW50LmRvY3VtZW50RWxlbWVudC5nZXRBdHRyaWJ1dGUoJ2RpcicpID09ICdydGwnO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5yZW1vdmVGYWtlKCk7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2sgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBfdGhpcy5yZW1vdmVGYWtlKCk7XG4gICAgICAgICAgICAgICAgfTtcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyID0gZG9jdW1lbnQuYm9keS5hZGRFdmVudExpc3RlbmVyKCdjbGljaycsIHRoaXMuZmFrZUhhbmRsZXJDYWxsYmFjaykgfHwgdHJ1ZTtcblxuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0gPSBkb2N1bWVudC5jcmVhdGVFbGVtZW50KCd0ZXh0YXJlYScpO1xuICAgICAgICAgICAgICAgIC8vIFByZXZlbnQgem9vbWluZyBvbiBpT1NcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLmZvbnRTaXplID0gJzEycHQnO1xuICAgICAgICAgICAgICAgIC8vIFJlc2V0IGJveCBtb2RlbFxuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGUuYm9yZGVyID0gJzAnO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGUucGFkZGluZyA9ICcwJztcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLm1hcmdpbiA9ICcwJztcbiAgICAgICAgICAgICAgICAvLyBNb3ZlIGVsZW1lbnQgb3V0IG9mIHNjcmVlbiBob3Jpem9udGFsbHlcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLnBvc2l0aW9uID0gJ2Fic29sdXRlJztcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlW2lzUlRMID8gJ3JpZ2h0JyA6ICdsZWZ0J10gPSAnLTk5OTlweCc7XG4gICAgICAgICAgICAgICAgLy8gTW92ZSBlbGVtZW50IHRvIHRoZSBzYW1lIHBvc2l0aW9uIHZlcnRpY2FsbHlcbiAgICAgICAgICAgICAgICB2YXIgeVBvc2l0aW9uID0gd2luZG93LnBhZ2VZT2Zmc2V0IHx8IGRvY3VtZW50LmRvY3VtZW50RWxlbWVudC5zY3JvbGxUb3A7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5hZGRFdmVudExpc3RlbmVyKCdmb2N1cycsIHdpbmRvdy5zY3JvbGxUbygwLCB5UG9zaXRpb24pKTtcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLnRvcCA9IHlQb3NpdGlvbiArICdweCc7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnNldEF0dHJpYnV0ZSgncmVhZG9ubHknLCAnJyk7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS52YWx1ZSA9IHRoaXMudGV4dDtcblxuICAgICAgICAgICAgICAgIGRvY3VtZW50LmJvZHkuYXBwZW5kQ2hpbGQodGhpcy5mYWtlRWxlbSk7XG5cbiAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdGVkVGV4dCA9ICgwLCBfc2VsZWN0Mi5kZWZhdWx0KSh0aGlzLmZha2VFbGVtKTtcbiAgICAgICAgICAgICAgICB0aGlzLmNvcHlUZXh0KCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3JlbW92ZUZha2UnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlbW92ZUZha2UoKSB7XG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuZmFrZUhhbmRsZXIpIHtcbiAgICAgICAgICAgICAgICAgICAgZG9jdW1lbnQuYm9keS5yZW1vdmVFdmVudExpc3RlbmVyKCdjbGljaycsIHRoaXMuZmFrZUhhbmRsZXJDYWxsYmFjayk7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuZmFrZUhhbmRsZXIgPSBudWxsO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2sgPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgIGlmICh0aGlzLmZha2VFbGVtKSB7XG4gICAgICAgICAgICAgICAgICAgIGRvY3VtZW50LmJvZHkucmVtb3ZlQ2hpbGQodGhpcy5mYWtlRWxlbSk7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0gPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnc2VsZWN0VGFyZ2V0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBzZWxlY3RUYXJnZXQoKSB7XG4gICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RlZFRleHQgPSAoMCwgX3NlbGVjdDIuZGVmYXVsdCkodGhpcy50YXJnZXQpO1xuICAgICAgICAgICAgICAgIHRoaXMuY29weVRleHQoKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnY29weVRleHQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGNvcHlUZXh0KCkge1xuICAgICAgICAgICAgICAgIHZhciBzdWNjZWVkZWQgPSB2b2lkIDA7XG5cbiAgICAgICAgICAgICAgICB0cnkge1xuICAgICAgICAgICAgICAgICAgICBzdWNjZWVkZWQgPSBkb2N1bWVudC5leGVjQ29tbWFuZCh0aGlzLmFjdGlvbik7XG4gICAgICAgICAgICAgICAgfSBjYXRjaCAoZXJyKSB7XG4gICAgICAgICAgICAgICAgICAgIHN1Y2NlZWRlZCA9IGZhbHNlO1xuICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgIHRoaXMuaGFuZGxlUmVzdWx0KHN1Y2NlZWRlZCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2hhbmRsZVJlc3VsdCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gaGFuZGxlUmVzdWx0KHN1Y2NlZWRlZCkge1xuICAgICAgICAgICAgICAgIHRoaXMuZW1pdHRlci5lbWl0KHN1Y2NlZWRlZCA/ICdzdWNjZXNzJyA6ICdlcnJvcicsIHtcbiAgICAgICAgICAgICAgICAgICAgYWN0aW9uOiB0aGlzLmFjdGlvbixcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogdGhpcy5zZWxlY3RlZFRleHQsXG4gICAgICAgICAgICAgICAgICAgIHRyaWdnZXI6IHRoaXMudHJpZ2dlcixcbiAgICAgICAgICAgICAgICAgICAgY2xlYXJTZWxlY3Rpb246IHRoaXMuY2xlYXJTZWxlY3Rpb24uYmluZCh0aGlzKVxuICAgICAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdjbGVhclNlbGVjdGlvbicsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gY2xlYXJTZWxlY3Rpb24oKSB7XG4gICAgICAgICAgICAgICAgaWYgKHRoaXMudGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMudGFyZ2V0LmJsdXIoKTtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB3aW5kb3cuZ2V0U2VsZWN0aW9uKCkucmVtb3ZlQWxsUmFuZ2VzKCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2Rlc3Ryb3knLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlc3Ryb3koKSB7XG4gICAgICAgICAgICAgICAgdGhpcy5yZW1vdmVGYWtlKCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2FjdGlvbicsXG4gICAgICAgICAgICBzZXQ6IGZ1bmN0aW9uIHNldCgpIHtcbiAgICAgICAgICAgICAgICB2YXIgYWN0aW9uID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiAnY29weSc7XG5cbiAgICAgICAgICAgICAgICB0aGlzLl9hY3Rpb24gPSBhY3Rpb247XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5fYWN0aW9uICE9PSAnY29weScgJiYgdGhpcy5fYWN0aW9uICE9PSAnY3V0Jykge1xuICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJhY3Rpb25cIiB2YWx1ZSwgdXNlIGVpdGhlciBcImNvcHlcIiBvciBcImN1dFwiJyk7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLl9hY3Rpb247XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3RhcmdldCcsXG4gICAgICAgICAgICBzZXQ6IGZ1bmN0aW9uIHNldCh0YXJnZXQpIHtcbiAgICAgICAgICAgICAgICBpZiAodGFyZ2V0ICE9PSB1bmRlZmluZWQpIHtcbiAgICAgICAgICAgICAgICAgICAgaWYgKHRhcmdldCAmJiAodHlwZW9mIHRhcmdldCA9PT0gJ3VuZGVmaW5lZCcgPyAndW5kZWZpbmVkJyA6IF90eXBlb2YodGFyZ2V0KSkgPT09ICdvYmplY3QnICYmIHRhcmdldC5ub2RlVHlwZSA9PT0gMSkge1xuICAgICAgICAgICAgICAgICAgICAgICAgaWYgKHRoaXMuYWN0aW9uID09PSAnY29weScgJiYgdGFyZ2V0Lmhhc0F0dHJpYnV0ZSgnZGlzYWJsZWQnKSkge1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignSW52YWxpZCBcInRhcmdldFwiIGF0dHJpYnV0ZS4gUGxlYXNlIHVzZSBcInJlYWRvbmx5XCIgaW5zdGVhZCBvZiBcImRpc2FibGVkXCIgYXR0cmlidXRlJyk7XG4gICAgICAgICAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICAgICAgICAgIGlmICh0aGlzLmFjdGlvbiA9PT0gJ2N1dCcgJiYgKHRhcmdldC5oYXNBdHRyaWJ1dGUoJ3JlYWRvbmx5JykgfHwgdGFyZ2V0Lmhhc0F0dHJpYnV0ZSgnZGlzYWJsZWQnKSkpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJ0YXJnZXRcIiBhdHRyaWJ1dGUuIFlvdSBjYW5cXCd0IGN1dCB0ZXh0IGZyb20gZWxlbWVudHMgd2l0aCBcInJlYWRvbmx5XCIgb3IgXCJkaXNhYmxlZFwiIGF0dHJpYnV0ZXMnKTtcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5fdGFyZ2V0ID0gdGFyZ2V0O1xuICAgICAgICAgICAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwidGFyZ2V0XCIgdmFsdWUsIHVzZSBhIHZhbGlkIEVsZW1lbnQnKTtcbiAgICAgICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5fdGFyZ2V0O1xuICAgICAgICAgICAgfVxuICAgICAgICB9XSk7XG5cbiAgICAgICAgcmV0dXJuIENsaXBib2FyZEFjdGlvbjtcbiAgICB9KCk7XG5cbiAgICBtb2R1bGUuZXhwb3J0cyA9IENsaXBib2FyZEFjdGlvbjtcbn0pOyIsIihmdW5jdGlvbiAoZ2xvYmFsLCBmYWN0b3J5KSB7XG4gICAgaWYgKHR5cGVvZiBkZWZpbmUgPT09IFwiZnVuY3Rpb25cIiAmJiBkZWZpbmUuYW1kKSB7XG4gICAgICAgIGRlZmluZShbJ21vZHVsZScsICcuL2NsaXBib2FyZC1hY3Rpb24nLCAndGlueS1lbWl0dGVyJywgJ2dvb2QtbGlzdGVuZXInXSwgZmFjdG9yeSk7XG4gICAgfSBlbHNlIGlmICh0eXBlb2YgZXhwb3J0cyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgICAgICBmYWN0b3J5KG1vZHVsZSwgcmVxdWlyZSgnLi9jbGlwYm9hcmQtYWN0aW9uJyksIHJlcXVpcmUoJ3RpbnktZW1pdHRlcicpLCByZXF1aXJlKCdnb29kLWxpc3RlbmVyJykpO1xuICAgIH0gZWxzZSB7XG4gICAgICAgIHZhciBtb2QgPSB7XG4gICAgICAgICAgICBleHBvcnRzOiB7fVxuICAgICAgICB9O1xuICAgICAgICBmYWN0b3J5KG1vZCwgZ2xvYmFsLmNsaXBib2FyZEFjdGlvbiwgZ2xvYmFsLnRpbnlFbWl0dGVyLCBnbG9iYWwuZ29vZExpc3RlbmVyKTtcbiAgICAgICAgZ2xvYmFsLmNsaXBib2FyZCA9IG1vZC5leHBvcnRzO1xuICAgIH1cbn0pKHRoaXMsIGZ1bmN0aW9uIChtb2R1bGUsIF9jbGlwYm9hcmRBY3Rpb24sIF90aW55RW1pdHRlciwgX2dvb2RMaXN0ZW5lcikge1xuICAgICd1c2Ugc3RyaWN0JztcblxuICAgIHZhciBfY2xpcGJvYXJkQWN0aW9uMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NsaXBib2FyZEFjdGlvbik7XG5cbiAgICB2YXIgX3RpbnlFbWl0dGVyMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3RpbnlFbWl0dGVyKTtcblxuICAgIHZhciBfZ29vZExpc3RlbmVyMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2dvb2RMaXN0ZW5lcik7XG5cbiAgICBmdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikge1xuICAgICAgICByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDoge1xuICAgICAgICAgICAgZGVmYXVsdDogb2JqXG4gICAgICAgIH07XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3Rvcikge1xuICAgICAgICBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIkNhbm5vdCBjYWxsIGEgY2xhc3MgYXMgYSBmdW5jdGlvblwiKTtcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBfY3JlYXRlQ2xhc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIGZ1bmN0aW9uIGRlZmluZVByb3BlcnRpZXModGFyZ2V0LCBwcm9wcykge1xuICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBwcm9wcy5sZW5ndGg7IGkrKykge1xuICAgICAgICAgICAgICAgIHZhciBkZXNjcmlwdG9yID0gcHJvcHNbaV07XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5lbnVtZXJhYmxlID0gZGVzY3JpcHRvci5lbnVtZXJhYmxlIHx8IGZhbHNlO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuY29uZmlndXJhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBpZiAoXCJ2YWx1ZVwiIGluIGRlc2NyaXB0b3IpIGRlc2NyaXB0b3Iud3JpdGFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIE9iamVjdC5kZWZpbmVQcm9wZXJ0eSh0YXJnZXQsIGRlc2NyaXB0b3Iua2V5LCBkZXNjcmlwdG9yKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBmdW5jdGlvbiAoQ29uc3RydWN0b3IsIHByb3RvUHJvcHMsIHN0YXRpY1Byb3BzKSB7XG4gICAgICAgICAgICBpZiAocHJvdG9Qcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvci5wcm90b3R5cGUsIHByb3RvUHJvcHMpO1xuICAgICAgICAgICAgaWYgKHN0YXRpY1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLCBzdGF0aWNQcm9wcyk7XG4gICAgICAgICAgICByZXR1cm4gQ29uc3RydWN0b3I7XG4gICAgICAgIH07XG4gICAgfSgpO1xuXG4gICAgZnVuY3Rpb24gX3Bvc3NpYmxlQ29uc3RydWN0b3JSZXR1cm4oc2VsZiwgY2FsbCkge1xuICAgICAgICBpZiAoIXNlbGYpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBSZWZlcmVuY2VFcnJvcihcInRoaXMgaGFzbid0IGJlZW4gaW5pdGlhbGlzZWQgLSBzdXBlcigpIGhhc24ndCBiZWVuIGNhbGxlZFwiKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBjYWxsICYmICh0eXBlb2YgY2FsbCA9PT0gXCJvYmplY3RcIiB8fCB0eXBlb2YgY2FsbCA9PT0gXCJmdW5jdGlvblwiKSA/IGNhbGwgOiBzZWxmO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIF9pbmhlcml0cyhzdWJDbGFzcywgc3VwZXJDbGFzcykge1xuICAgICAgICBpZiAodHlwZW9mIHN1cGVyQ2xhc3MgIT09IFwiZnVuY3Rpb25cIiAmJiBzdXBlckNsYXNzICE9PSBudWxsKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKFwiU3VwZXIgZXhwcmVzc2lvbiBtdXN0IGVpdGhlciBiZSBudWxsIG9yIGEgZnVuY3Rpb24sIG5vdCBcIiArIHR5cGVvZiBzdXBlckNsYXNzKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHN1YkNsYXNzLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoc3VwZXJDbGFzcyAmJiBzdXBlckNsYXNzLnByb3RvdHlwZSwge1xuICAgICAgICAgICAgY29uc3RydWN0b3I6IHtcbiAgICAgICAgICAgICAgICB2YWx1ZTogc3ViQ2xhc3MsXG4gICAgICAgICAgICAgICAgZW51bWVyYWJsZTogZmFsc2UsXG4gICAgICAgICAgICAgICAgd3JpdGFibGU6IHRydWUsXG4gICAgICAgICAgICAgICAgY29uZmlndXJhYmxlOiB0cnVlXG4gICAgICAgICAgICB9XG4gICAgICAgIH0pO1xuICAgICAgICBpZiAoc3VwZXJDbGFzcykgT2JqZWN0LnNldFByb3RvdHlwZU9mID8gT2JqZWN0LnNldFByb3RvdHlwZU9mKHN1YkNsYXNzLCBzdXBlckNsYXNzKSA6IHN1YkNsYXNzLl9fcHJvdG9fXyA9IHN1cGVyQ2xhc3M7XG4gICAgfVxuXG4gICAgdmFyIENsaXBib2FyZCA9IGZ1bmN0aW9uIChfRW1pdHRlcikge1xuICAgICAgICBfaW5oZXJpdHMoQ2xpcGJvYXJkLCBfRW1pdHRlcik7XG5cbiAgICAgICAgLyoqXG4gICAgICAgICAqIEBwYXJhbSB7U3RyaW5nfEhUTUxFbGVtZW50fEhUTUxDb2xsZWN0aW9ufE5vZGVMaXN0fSB0cmlnZ2VyXG4gICAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvcHRpb25zXG4gICAgICAgICAqL1xuICAgICAgICBmdW5jdGlvbiBDbGlwYm9hcmQodHJpZ2dlciwgb3B0aW9ucykge1xuICAgICAgICAgICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIENsaXBib2FyZCk7XG5cbiAgICAgICAgICAgIHZhciBfdGhpcyA9IF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHRoaXMsIChDbGlwYm9hcmQuX19wcm90b19fIHx8IE9iamVjdC5nZXRQcm90b3R5cGVPZihDbGlwYm9hcmQpKS5jYWxsKHRoaXMpKTtcblxuICAgICAgICAgICAgX3RoaXMucmVzb2x2ZU9wdGlvbnMob3B0aW9ucyk7XG4gICAgICAgICAgICBfdGhpcy5saXN0ZW5DbGljayh0cmlnZ2VyKTtcbiAgICAgICAgICAgIHJldHVybiBfdGhpcztcbiAgICAgICAgfVxuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBEZWZpbmVzIGlmIGF0dHJpYnV0ZXMgd291bGQgYmUgcmVzb2x2ZWQgdXNpbmcgaW50ZXJuYWwgc2V0dGVyIGZ1bmN0aW9uc1xuICAgICAgICAgKiBvciBjdXN0b20gZnVuY3Rpb25zIHRoYXQgd2VyZSBwYXNzZWQgaW4gdGhlIGNvbnN0cnVjdG9yLlxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cblxuXG4gICAgICAgIF9jcmVhdGVDbGFzcyhDbGlwYm9hcmQsIFt7XG4gICAgICAgICAgICBrZXk6ICdyZXNvbHZlT3B0aW9ucycsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gcmVzb2x2ZU9wdGlvbnMoKSB7XG4gICAgICAgICAgICAgICAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoID4gMCAmJiBhcmd1bWVudHNbMF0gIT09IHVuZGVmaW5lZCA/IGFyZ3VtZW50c1swXSA6IHt9O1xuXG4gICAgICAgICAgICAgICAgdGhpcy5hY3Rpb24gPSB0eXBlb2Ygb3B0aW9ucy5hY3Rpb24gPT09ICdmdW5jdGlvbicgPyBvcHRpb25zLmFjdGlvbiA6IHRoaXMuZGVmYXVsdEFjdGlvbjtcbiAgICAgICAgICAgICAgICB0aGlzLnRhcmdldCA9IHR5cGVvZiBvcHRpb25zLnRhcmdldCA9PT0gJ2Z1bmN0aW9uJyA/IG9wdGlvbnMudGFyZ2V0IDogdGhpcy5kZWZhdWx0VGFyZ2V0O1xuICAgICAgICAgICAgICAgIHRoaXMudGV4dCA9IHR5cGVvZiBvcHRpb25zLnRleHQgPT09ICdmdW5jdGlvbicgPyBvcHRpb25zLnRleHQgOiB0aGlzLmRlZmF1bHRUZXh0O1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdsaXN0ZW5DbGljaycsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gbGlzdGVuQ2xpY2sodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHZhciBfdGhpczIgPSB0aGlzO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5saXN0ZW5lciA9ICgwLCBfZ29vZExpc3RlbmVyMi5kZWZhdWx0KSh0cmlnZ2VyLCAnY2xpY2snLCBmdW5jdGlvbiAoZSkge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gX3RoaXMyLm9uQ2xpY2soZSk7XG4gICAgICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ29uQ2xpY2snLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIG9uQ2xpY2soZSkge1xuICAgICAgICAgICAgICAgIHZhciB0cmlnZ2VyID0gZS5kZWxlZ2F0ZVRhcmdldCB8fCBlLmN1cnJlbnRUYXJnZXQ7XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5jbGlwYm9hcmRBY3Rpb24pIHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24gPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uID0gbmV3IF9jbGlwYm9hcmRBY3Rpb24yLmRlZmF1bHQoe1xuICAgICAgICAgICAgICAgICAgICBhY3Rpb246IHRoaXMuYWN0aW9uKHRyaWdnZXIpLFxuICAgICAgICAgICAgICAgICAgICB0YXJnZXQ6IHRoaXMudGFyZ2V0KHRyaWdnZXIpLFxuICAgICAgICAgICAgICAgICAgICB0ZXh0OiB0aGlzLnRleHQodHJpZ2dlciksXG4gICAgICAgICAgICAgICAgICAgIHRyaWdnZXI6IHRyaWdnZXIsXG4gICAgICAgICAgICAgICAgICAgIGVtaXR0ZXI6IHRoaXNcbiAgICAgICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnZGVmYXVsdEFjdGlvbicsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVmYXVsdEFjdGlvbih0cmlnZ2VyKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIGdldEF0dHJpYnV0ZVZhbHVlKCdhY3Rpb24nLCB0cmlnZ2VyKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnZGVmYXVsdFRhcmdldCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVmYXVsdFRhcmdldCh0cmlnZ2VyKSB7XG4gICAgICAgICAgICAgICAgdmFyIHNlbGVjdG9yID0gZ2V0QXR0cmlidXRlVmFsdWUoJ3RhcmdldCcsIHRyaWdnZXIpO1xuXG4gICAgICAgICAgICAgICAgaWYgKHNlbGVjdG9yKSB7XG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBkb2N1bWVudC5xdWVyeVNlbGVjdG9yKHNlbGVjdG9yKTtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRUZXh0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZWZhdWx0VGV4dCh0cmlnZ2VyKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIGdldEF0dHJpYnV0ZVZhbHVlKCd0ZXh0JywgdHJpZ2dlcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2Rlc3Ryb3knLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlc3Ryb3koKSB7XG4gICAgICAgICAgICAgICAgdGhpcy5saXN0ZW5lci5kZXN0cm95KCk7XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5jbGlwYm9hcmRBY3Rpb24pIHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24uZGVzdHJveSgpO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmNsaXBib2FyZEFjdGlvbiA9IG51bGw7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9XSk7XG5cbiAgICAgICAgcmV0dXJuIENsaXBib2FyZDtcbiAgICB9KF90aW55RW1pdHRlcjIuZGVmYXVsdCk7XG5cbiAgICAvKipcbiAgICAgKiBIZWxwZXIgZnVuY3Rpb24gdG8gcmV0cmlldmUgYXR0cmlidXRlIHZhbHVlLlxuICAgICAqIEBwYXJhbSB7U3RyaW5nfSBzdWZmaXhcbiAgICAgKiBAcGFyYW0ge0VsZW1lbnR9IGVsZW1lbnRcbiAgICAgKi9cbiAgICBmdW5jdGlvbiBnZXRBdHRyaWJ1dGVWYWx1ZShzdWZmaXgsIGVsZW1lbnQpIHtcbiAgICAgICAgdmFyIGF0dHJpYnV0ZSA9ICdkYXRhLWNsaXBib2FyZC0nICsgc3VmZml4O1xuXG4gICAgICAgIGlmICghZWxlbWVudC5oYXNBdHRyaWJ1dGUoYXR0cmlidXRlKSkge1xuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGVsZW1lbnQuZ2V0QXR0cmlidXRlKGF0dHJpYnV0ZSk7XG4gICAgfVxuXG4gICAgbW9kdWxlLmV4cG9ydHMgPSBDbGlwYm9hcmQ7XG59KTsiLCIvKipcbiAqIEEgcG9seWZpbGwgZm9yIEVsZW1lbnQubWF0Y2hlcygpXG4gKi9cbmlmIChFbGVtZW50ICYmICFFbGVtZW50LnByb3RvdHlwZS5tYXRjaGVzKSB7XG4gICAgdmFyIHByb3RvID0gRWxlbWVudC5wcm90b3R5cGU7XG5cbiAgICBwcm90by5tYXRjaGVzID0gcHJvdG8ubWF0Y2hlc1NlbGVjdG9yIHx8XG4gICAgICAgICAgICAgICAgICAgIHByb3RvLm1vek1hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by5tc01hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by5vTWF0Y2hlc1NlbGVjdG9yIHx8XG4gICAgICAgICAgICAgICAgICAgIHByb3RvLndlYmtpdE1hdGNoZXNTZWxlY3Rvcjtcbn1cblxuLyoqXG4gKiBGaW5kcyB0aGUgY2xvc2VzdCBwYXJlbnQgdGhhdCBtYXRjaGVzIGEgc2VsZWN0b3IuXG4gKlxuICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEByZXR1cm4ge0Z1bmN0aW9ufVxuICovXG5mdW5jdGlvbiBjbG9zZXN0IChlbGVtZW50LCBzZWxlY3Rvcikge1xuICAgIHdoaWxlIChlbGVtZW50ICYmIGVsZW1lbnQgIT09IGRvY3VtZW50KSB7XG4gICAgICAgIGlmIChlbGVtZW50Lm1hdGNoZXMoc2VsZWN0b3IpKSByZXR1cm4gZWxlbWVudDtcbiAgICAgICAgZWxlbWVudCA9IGVsZW1lbnQucGFyZW50Tm9kZTtcbiAgICB9XG59XG5cbm1vZHVsZS5leHBvcnRzID0gY2xvc2VzdDtcbiIsInZhciBjbG9zZXN0ID0gcmVxdWlyZSgnLi9jbG9zZXN0Jyk7XG5cbi8qKlxuICogRGVsZWdhdGVzIGV2ZW50IHRvIGEgc2VsZWN0b3IuXG4gKlxuICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHBhcmFtIHtCb29sZWFufSB1c2VDYXB0dXJlXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGRlbGVnYXRlKGVsZW1lbnQsIHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaywgdXNlQ2FwdHVyZSkge1xuICAgIHZhciBsaXN0ZW5lckZuID0gbGlzdGVuZXIuYXBwbHkodGhpcywgYXJndW1lbnRzKTtcblxuICAgIGVsZW1lbnQuYWRkRXZlbnRMaXN0ZW5lcih0eXBlLCBsaXN0ZW5lckZuLCB1c2VDYXB0dXJlKTtcblxuICAgIHJldHVybiB7XG4gICAgICAgIGRlc3Ryb3k6IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgZWxlbWVudC5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGxpc3RlbmVyRm4sIHVzZUNhcHR1cmUpO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEZpbmRzIGNsb3Nlc3QgbWF0Y2ggYW5kIGludm9rZXMgY2FsbGJhY2suXG4gKlxuICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7RnVuY3Rpb259XG4gKi9cbmZ1bmN0aW9uIGxpc3RlbmVyKGVsZW1lbnQsIHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaykge1xuICAgIHJldHVybiBmdW5jdGlvbihlKSB7XG4gICAgICAgIGUuZGVsZWdhdGVUYXJnZXQgPSBjbG9zZXN0KGUudGFyZ2V0LCBzZWxlY3Rvcik7XG5cbiAgICAgICAgaWYgKGUuZGVsZWdhdGVUYXJnZXQpIHtcbiAgICAgICAgICAgIGNhbGxiYWNrLmNhbGwoZWxlbWVudCwgZSk7XG4gICAgICAgIH1cbiAgICB9XG59XG5cbm1vZHVsZS5leHBvcnRzID0gZGVsZWdhdGU7XG4iLCIvKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgSFRNTCBlbGVtZW50LlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5ub2RlID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICByZXR1cm4gdmFsdWUgIT09IHVuZGVmaW5lZFxuICAgICAgICAmJiB2YWx1ZSBpbnN0YW5jZW9mIEhUTUxFbGVtZW50XG4gICAgICAgICYmIHZhbHVlLm5vZGVUeXBlID09PSAxO1xufTtcblxuLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIGxpc3Qgb2YgSFRNTCBlbGVtZW50cy5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gdmFsdWVcbiAqIEByZXR1cm4ge0Jvb2xlYW59XG4gKi9cbmV4cG9ydHMubm9kZUxpc3QgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHZhciB0eXBlID0gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZy5jYWxsKHZhbHVlKTtcblxuICAgIHJldHVybiB2YWx1ZSAhPT0gdW5kZWZpbmVkXG4gICAgICAgICYmICh0eXBlID09PSAnW29iamVjdCBOb2RlTGlzdF0nIHx8IHR5cGUgPT09ICdbb2JqZWN0IEhUTUxDb2xsZWN0aW9uXScpXG4gICAgICAgICYmICgnbGVuZ3RoJyBpbiB2YWx1ZSlcbiAgICAgICAgJiYgKHZhbHVlLmxlbmd0aCA9PT0gMCB8fCBleHBvcnRzLm5vZGUodmFsdWVbMF0pKTtcbn07XG5cbi8qKlxuICogQ2hlY2sgaWYgYXJndW1lbnQgaXMgYSBzdHJpbmcuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLnN0cmluZyA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgcmV0dXJuIHR5cGVvZiB2YWx1ZSA9PT0gJ3N0cmluZydcbiAgICAgICAgfHwgdmFsdWUgaW5zdGFuY2VvZiBTdHJpbmc7XG59O1xuXG4vKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLmZuID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICB2YXIgdHlwZSA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbCh2YWx1ZSk7XG5cbiAgICByZXR1cm4gdHlwZSA9PT0gJ1tvYmplY3QgRnVuY3Rpb25dJztcbn07XG4iLCJ2YXIgaXMgPSByZXF1aXJlKCcuL2lzJyk7XG52YXIgZGVsZWdhdGUgPSByZXF1aXJlKCdkZWxlZ2F0ZScpO1xuXG4vKipcbiAqIFZhbGlkYXRlcyBhbGwgcGFyYW1zIGFuZCBjYWxscyB0aGUgcmlnaHRcbiAqIGxpc3RlbmVyIGZ1bmN0aW9uIGJhc2VkIG9uIGl0cyB0YXJnZXQgdHlwZS5cbiAqXG4gKiBAcGFyYW0ge1N0cmluZ3xIVE1MRWxlbWVudHxIVE1MQ29sbGVjdGlvbnxOb2RlTGlzdH0gdGFyZ2V0XG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuKHRhcmdldCwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBpZiAoIXRhcmdldCAmJiAhdHlwZSAmJiAhY2FsbGJhY2spIHtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdNaXNzaW5nIHJlcXVpcmVkIGFyZ3VtZW50cycpO1xuICAgIH1cblxuICAgIGlmICghaXMuc3RyaW5nKHR5cGUpKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ1NlY29uZCBhcmd1bWVudCBtdXN0IGJlIGEgU3RyaW5nJyk7XG4gICAgfVxuXG4gICAgaWYgKCFpcy5mbihjYWxsYmFjaykpIHtcbiAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcignVGhpcmQgYXJndW1lbnQgbXVzdCBiZSBhIEZ1bmN0aW9uJyk7XG4gICAgfVxuXG4gICAgaWYgKGlzLm5vZGUodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuTm9kZSh0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKTtcbiAgICB9XG4gICAgZWxzZSBpZiAoaXMubm9kZUxpc3QodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuTm9kZUxpc3QodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2UgaWYgKGlzLnN0cmluZyh0YXJnZXQpKSB7XG4gICAgICAgIHJldHVybiBsaXN0ZW5TZWxlY3Rvcih0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKTtcbiAgICB9XG4gICAgZWxzZSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ0ZpcnN0IGFyZ3VtZW50IG11c3QgYmUgYSBTdHJpbmcsIEhUTUxFbGVtZW50LCBIVE1MQ29sbGVjdGlvbiwgb3IgTm9kZUxpc3QnKTtcbiAgICB9XG59XG5cbi8qKlxuICogQWRkcyBhbiBldmVudCBsaXN0ZW5lciB0byBhIEhUTUwgZWxlbWVudFxuICogYW5kIHJldHVybnMgYSByZW1vdmUgbGlzdGVuZXIgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtIVE1MRWxlbWVudH0gbm9kZVxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3Rlbk5vZGUobm9kZSwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBub2RlLmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuXG4gICAgcmV0dXJuIHtcbiAgICAgICAgZGVzdHJveTogZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZCBhbiBldmVudCBsaXN0ZW5lciB0byBhIGxpc3Qgb2YgSFRNTCBlbGVtZW50c1xuICogYW5kIHJldHVybnMgYSByZW1vdmUgbGlzdGVuZXIgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtOb2RlTGlzdHxIVE1MQ29sbGVjdGlvbn0gbm9kZUxpc3RcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5Ob2RlTGlzdChub2RlTGlzdCwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBBcnJheS5wcm90b3R5cGUuZm9yRWFjaC5jYWxsKG5vZGVMaXN0LCBmdW5jdGlvbihub2RlKSB7XG4gICAgICAgIG5vZGUuYWRkRXZlbnRMaXN0ZW5lcih0eXBlLCBjYWxsYmFjayk7XG4gICAgfSk7XG5cbiAgICByZXR1cm4ge1xuICAgICAgICBkZXN0cm95OiBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIEFycmF5LnByb3RvdHlwZS5mb3JFYWNoLmNhbGwobm9kZUxpc3QsIGZ1bmN0aW9uKG5vZGUpIHtcbiAgICAgICAgICAgICAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgICAgICAgICAgfSk7XG4gICAgICAgIH1cbiAgICB9XG59XG5cbi8qKlxuICogQWRkIGFuIGV2ZW50IGxpc3RlbmVyIHRvIGEgc2VsZWN0b3JcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7U3RyaW5nfSBzZWxlY3RvclxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3RlblNlbGVjdG9yKHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaykge1xuICAgIHJldHVybiBkZWxlZ2F0ZShkb2N1bWVudC5ib2R5LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spO1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGxpc3RlbjtcbiIsIi8qISBuZ2NsaXBib2FyZCAtIHYxLjEuMSAtIDIwMTYtMDItMjZcclxuKiBodHRwczovL2dpdGh1Yi5jb20vc2FjaGluY2hvb2x1ci9uZ2NsaXBib2FyZFxyXG4qIENvcHlyaWdodCAoYykgMjAxNiBTYWNoaW47IExpY2Vuc2VkIE1JVCAqL1xyXG4oZnVuY3Rpb24oKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICB2YXIgTU9EVUxFX05BTUUgPSAnbmdjbGlwYm9hcmQnO1xyXG4gICAgdmFyIGFuZ3VsYXIsIENsaXBib2FyZDtcclxuICAgIFxyXG4gICAgLy8gQ2hlY2sgZm9yIENvbW1vbkpTIHN1cHBvcnRcclxuICAgIGlmICh0eXBlb2YgbW9kdWxlID09PSAnb2JqZWN0JyAmJiBtb2R1bGUuZXhwb3J0cykge1xyXG4gICAgICBhbmd1bGFyID0gcmVxdWlyZSgnYW5ndWxhcicpO1xyXG4gICAgICBDbGlwYm9hcmQgPSByZXF1aXJlKCdjbGlwYm9hcmQnKTtcclxuICAgICAgbW9kdWxlLmV4cG9ydHMgPSBNT0RVTEVfTkFNRTtcclxuICAgIH0gZWxzZSB7XHJcbiAgICAgIGFuZ3VsYXIgPSB3aW5kb3cuYW5ndWxhcjtcclxuICAgICAgQ2xpcGJvYXJkID0gd2luZG93LkNsaXBib2FyZDtcclxuICAgIH1cclxuXHJcbiAgICBhbmd1bGFyLm1vZHVsZShNT0RVTEVfTkFNRSwgW10pLmRpcmVjdGl2ZSgnbmdjbGlwYm9hcmQnLCBmdW5jdGlvbigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICByZXN0cmljdDogJ0EnLFxyXG4gICAgICAgICAgICBzY29wZToge1xyXG4gICAgICAgICAgICAgICAgbmdjbGlwYm9hcmRTdWNjZXNzOiAnJicsXHJcbiAgICAgICAgICAgICAgICBuZ2NsaXBib2FyZEVycm9yOiAnJidcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgbGluazogZnVuY3Rpb24oc2NvcGUsIGVsZW1lbnQpIHtcclxuICAgICAgICAgICAgICAgIHZhciBjbGlwYm9hcmQgPSBuZXcgQ2xpcGJvYXJkKGVsZW1lbnRbMF0pO1xyXG5cclxuICAgICAgICAgICAgICAgIGNsaXBib2FyZC5vbignc3VjY2VzcycsIGZ1bmN0aW9uKGUpIHtcclxuICAgICAgICAgICAgICAgICAgc2NvcGUuJGFwcGx5KGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICBzY29wZS5uZ2NsaXBib2FyZFN1Y2Nlc3Moe1xyXG4gICAgICAgICAgICAgICAgICAgICAgZTogZVxyXG4gICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgICAgIGNsaXBib2FyZC5vbignZXJyb3InLCBmdW5jdGlvbihlKSB7XHJcbiAgICAgICAgICAgICAgICAgIHNjb3BlLiRhcHBseShmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2NvcGUubmdjbGlwYm9hcmRFcnJvcih7XHJcbiAgICAgICAgICAgICAgICAgICAgICBlOiBlXHJcbiAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH0pO1xyXG59KCkpO1xyXG4iLCJmdW5jdGlvbiBzZWxlY3QoZWxlbWVudCkge1xuICAgIHZhciBzZWxlY3RlZFRleHQ7XG5cbiAgICBpZiAoZWxlbWVudC5ub2RlTmFtZSA9PT0gJ1NFTEVDVCcpIHtcbiAgICAgICAgZWxlbWVudC5mb2N1cygpO1xuXG4gICAgICAgIHNlbGVjdGVkVGV4dCA9IGVsZW1lbnQudmFsdWU7XG4gICAgfVxuICAgIGVsc2UgaWYgKGVsZW1lbnQubm9kZU5hbWUgPT09ICdJTlBVVCcgfHwgZWxlbWVudC5ub2RlTmFtZSA9PT0gJ1RFWFRBUkVBJykge1xuICAgICAgICBlbGVtZW50LmZvY3VzKCk7XG4gICAgICAgIGVsZW1lbnQuc2V0U2VsZWN0aW9uUmFuZ2UoMCwgZWxlbWVudC52YWx1ZS5sZW5ndGgpO1xuXG4gICAgICAgIHNlbGVjdGVkVGV4dCA9IGVsZW1lbnQudmFsdWU7XG4gICAgfVxuICAgIGVsc2Uge1xuICAgICAgICBpZiAoZWxlbWVudC5oYXNBdHRyaWJ1dGUoJ2NvbnRlbnRlZGl0YWJsZScpKSB7XG4gICAgICAgICAgICBlbGVtZW50LmZvY3VzKCk7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgc2VsZWN0aW9uID0gd2luZG93LmdldFNlbGVjdGlvbigpO1xuICAgICAgICB2YXIgcmFuZ2UgPSBkb2N1bWVudC5jcmVhdGVSYW5nZSgpO1xuXG4gICAgICAgIHJhbmdlLnNlbGVjdE5vZGVDb250ZW50cyhlbGVtZW50KTtcbiAgICAgICAgc2VsZWN0aW9uLnJlbW92ZUFsbFJhbmdlcygpO1xuICAgICAgICBzZWxlY3Rpb24uYWRkUmFuZ2UocmFuZ2UpO1xuXG4gICAgICAgIHNlbGVjdGVkVGV4dCA9IHNlbGVjdGlvbi50b1N0cmluZygpO1xuICAgIH1cblxuICAgIHJldHVybiBzZWxlY3RlZFRleHQ7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gc2VsZWN0O1xuIiwiZnVuY3Rpb24gRSAoKSB7XG4gIC8vIEtlZXAgdGhpcyBlbXB0eSBzbyBpdCdzIGVhc2llciB0byBpbmhlcml0IGZyb21cbiAgLy8gKHZpYSBodHRwczovL2dpdGh1Yi5jb20vbGlwc21hY2sgZnJvbSBodHRwczovL2dpdGh1Yi5jb20vc2NvdHRjb3JnYW4vdGlueS1lbWl0dGVyL2lzc3Vlcy8zKVxufVxuXG5FLnByb3RvdHlwZSA9IHtcbiAgb246IGZ1bmN0aW9uIChuYW1lLCBjYWxsYmFjaywgY3R4KSB7XG4gICAgdmFyIGUgPSB0aGlzLmUgfHwgKHRoaXMuZSA9IHt9KTtcblxuICAgIChlW25hbWVdIHx8IChlW25hbWVdID0gW10pKS5wdXNoKHtcbiAgICAgIGZuOiBjYWxsYmFjayxcbiAgICAgIGN0eDogY3R4XG4gICAgfSk7XG5cbiAgICByZXR1cm4gdGhpcztcbiAgfSxcblxuICBvbmNlOiBmdW5jdGlvbiAobmFtZSwgY2FsbGJhY2ssIGN0eCkge1xuICAgIHZhciBzZWxmID0gdGhpcztcbiAgICBmdW5jdGlvbiBsaXN0ZW5lciAoKSB7XG4gICAgICBzZWxmLm9mZihuYW1lLCBsaXN0ZW5lcik7XG4gICAgICBjYWxsYmFjay5hcHBseShjdHgsIGFyZ3VtZW50cyk7XG4gICAgfTtcblxuICAgIGxpc3RlbmVyLl8gPSBjYWxsYmFja1xuICAgIHJldHVybiB0aGlzLm9uKG5hbWUsIGxpc3RlbmVyLCBjdHgpO1xuICB9LFxuXG4gIGVtaXQ6IGZ1bmN0aW9uIChuYW1lKSB7XG4gICAgdmFyIGRhdGEgPSBbXS5zbGljZS5jYWxsKGFyZ3VtZW50cywgMSk7XG4gICAgdmFyIGV2dEFyciA9ICgodGhpcy5lIHx8ICh0aGlzLmUgPSB7fSkpW25hbWVdIHx8IFtdKS5zbGljZSgpO1xuICAgIHZhciBpID0gMDtcbiAgICB2YXIgbGVuID0gZXZ0QXJyLmxlbmd0aDtcblxuICAgIGZvciAoaTsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICBldnRBcnJbaV0uZm4uYXBwbHkoZXZ0QXJyW2ldLmN0eCwgZGF0YSk7XG4gICAgfVxuXG4gICAgcmV0dXJuIHRoaXM7XG4gIH0sXG5cbiAgb2ZmOiBmdW5jdGlvbiAobmFtZSwgY2FsbGJhY2spIHtcbiAgICB2YXIgZSA9IHRoaXMuZSB8fCAodGhpcy5lID0ge30pO1xuICAgIHZhciBldnRzID0gZVtuYW1lXTtcbiAgICB2YXIgbGl2ZUV2ZW50cyA9IFtdO1xuXG4gICAgaWYgKGV2dHMgJiYgY2FsbGJhY2spIHtcbiAgICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBldnRzLmxlbmd0aDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgIGlmIChldnRzW2ldLmZuICE9PSBjYWxsYmFjayAmJiBldnRzW2ldLmZuLl8gIT09IGNhbGxiYWNrKVxuICAgICAgICAgIGxpdmVFdmVudHMucHVzaChldnRzW2ldKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICAvLyBSZW1vdmUgZXZlbnQgZnJvbSBxdWV1ZSB0byBwcmV2ZW50IG1lbW9yeSBsZWFrXG4gICAgLy8gU3VnZ2VzdGVkIGJ5IGh0dHBzOi8vZ2l0aHViLmNvbS9sYXpkXG4gICAgLy8gUmVmOiBodHRwczovL2dpdGh1Yi5jb20vc2NvdHRjb3JnYW4vdGlueS1lbWl0dGVyL2NvbW1pdC9jNmViZmFhOWJjOTczYjMzZDExMGE4NGEzMDc3NDJiN2NmOTRjOTUzI2NvbW1pdGNvbW1lbnQtNTAyNDkxMFxuXG4gICAgKGxpdmVFdmVudHMubGVuZ3RoKVxuICAgICAgPyBlW25hbWVdID0gbGl2ZUV2ZW50c1xuICAgICAgOiBkZWxldGUgZVtuYW1lXTtcblxuICAgIHJldHVybiB0aGlzO1xuICB9XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IEU7XG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzIwLzIwMTUuXHJcbiAqIFREU00gaXMgYSBnbG9iYWwgb2JqZWN0IHRoYXQgY29tZXMgZnJvbSBBcHAuanNcclxuICpcclxuICogVGhlIGZvbGxvd2luZyBoZWxwZXIgd29ya3MgaW4gYSB3YXkgdG8gbWFrZSBhdmFpbGFibGUgdGhlIGNyZWF0aW9uIG9mIERpcmVjdGl2ZSwgU2VydmljZXMgYW5kIENvbnRyb2xsZXJcclxuICogb24gZmx5IG9yIHdoZW4gZGVwbG95aW5nIHRoZSBhcHAuXHJcbiAqXHJcbiAqIFdlIHJlZHVjZSB0aGUgdXNlIG9mIGNvbXBpbGUgYW5kIGV4dHJhIHN0ZXBzXHJcbiAqL1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi9BcHAuanMnKTtcclxuXHJcbi8qKlxyXG4gKiBMaXN0ZW4gdG8gYW4gZXhpc3RpbmcgZGlnZXN0IG9mIHRoZSBjb21waWxlIHByb3ZpZGVyIGFuZCBleGVjdXRlIHRoZSAkYXBwbHkgaW1tZWRpYXRlbHkgb3IgYWZ0ZXIgaXQncyByZWFkeVxyXG4gKiBAcGFyYW0gY3VycmVudFxyXG4gKiBAcGFyYW0gZm5cclxuICovXHJcblREU1RNLnNhZmVBcHBseSA9IGZ1bmN0aW9uIChjdXJyZW50LCBmbikge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgdmFyIHBoYXNlID0gY3VycmVudC4kcm9vdC4kJHBoYXNlO1xyXG4gICAgaWYgKHBoYXNlID09PSAnJGFwcGx5JyB8fCBwaGFzZSA9PT0gJyRkaWdlc3QnKSB7XHJcbiAgICAgICAgaWYgKGZuKSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGV2YWwoZm4pO1xyXG4gICAgICAgIH1cclxuICAgIH0gZWxzZSB7XHJcbiAgICAgICAgaWYgKGZuKSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGFwcGx5KGZuKTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRhcHBseSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IGRpcmVjdGl2ZSBhc3luYyBpZiB0aGUgY29tcGlsZVByb3ZpZGVyIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyKSB7XHJcbiAgICAgICAgVERTVE0uUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlci5kaXJlY3RpdmUoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmRpcmVjdGl2ZSkge1xyXG4gICAgICAgIFREU1RNLmRpcmVjdGl2ZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IGNvbnRyb2xsZXJzIGFzeW5jIGlmIHRoZSBjb250cm9sbGVyUHJvdmlkZXIgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVDb250cm9sbGVyID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUuY29udHJvbGxlclByb3ZpZGVyKSB7XHJcbiAgICAgICAgVERTVE0uY29udHJvbGxlclByb3ZpZGVyLnJlZ2lzdGVyKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5jb250cm9sbGVyKSB7XHJcbiAgICAgICAgVERTVE0uY29udHJvbGxlcihzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IHNlcnZpY2UgYXN5bmMgaWYgdGhlIHByb3ZpZGVTZXJ2aWNlIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlU2VydmljZSA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlKSB7XHJcbiAgICAgICAgVERTVE0uUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlLnNlcnZpY2Uoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmNvbnRyb2xsZXIpIHtcclxuICAgICAgICBURFNUTS5zZXJ2aWNlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEZvciBMZWdhY3kgc3lzdGVtLCB3aGF0IGlzIGRvZXMgaXMgdG8gdGFrZSBwYXJhbXMgZnJvbSB0aGUgcXVlcnlcclxuICogb3V0c2lkZSB0aGUgQW5ndWxhckpTIHVpLXJvdXRpbmcuXHJcbiAqIEBwYXJhbSBwYXJhbSAvLyBQYXJhbSB0byBzZWFyYyBmb3IgL2V4YW1wbGUuaHRtbD9iYXI9Zm9vI2N1cnJlbnRTdGF0ZVxyXG4gKi9cclxuVERTVE0uZ2V0VVJMUGFyYW0gPSBmdW5jdGlvbiAocGFyYW0pIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgICQudXJsUGFyYW0gPSBmdW5jdGlvbiAobmFtZSkge1xyXG4gICAgICAgIHZhciByZXN1bHRzID0gbmV3IFJlZ0V4cCgnW1xcPyZdJyArIG5hbWUgKyAnPShbXiYjXSopJykuZXhlYyh3aW5kb3cubG9jYXRpb24uaHJlZik7XHJcbiAgICAgICAgaWYgKHJlc3VsdHMgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgcmV0dXJuIG51bGw7XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGVsc2Uge1xyXG4gICAgICAgICAgICByZXR1cm4gcmVzdWx0c1sxXSB8fCAwO1xyXG4gICAgICAgIH1cclxuICAgIH07XHJcblxyXG4gICAgcmV0dXJuICQudXJsUGFyYW0ocGFyYW0pO1xyXG59O1xyXG5cclxuLyoqXHJcbiAqIFRoaXMgY29kZSB3YXMgaW50cm9kdWNlZCBvbmx5IGZvciB0aGUgaWZyYW1lIG1pZ3JhdGlvblxyXG4gKiBpdCBkZXRlY3Qgd2hlbiBtb3VzZSBlbnRlclxyXG4gKi9cclxuVERTVE0uaWZyYW1lTG9hZGVyID0gZnVuY3Rpb24gKCkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgJCgnLmlmcmFtZUxvYWRlcicpLmhvdmVyKFxyXG4gICAgICAgIGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgJCgnLm5hdmJhci11bC1jb250YWluZXIgLmRyb3Bkb3duLm9wZW4nKS5yZW1vdmVDbGFzcygnb3BlbicpO1xyXG4gICAgICAgIH0sIGZ1bmN0aW9uICgpIHtcclxuICAgICAgICB9XHJcbiAgICApO1xyXG59O1xyXG5cclxuXHJcbi8vIEl0IHdpbGwgYmUgcmVtb3ZlZCBhZnRlciB3ZSByaXAgb2ZmIGFsbCBpZnJhbWVzXHJcbndpbmRvdy5URFNUTSA9IFREU1RNOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMTYvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5yZXF1aXJlKCdhbmd1bGFyJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItYW5pbWF0ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLW1vY2tzJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItc2FuaXRpemUnKTtcclxucmVxdWlyZSgnYW5ndWxhci1yZXNvdXJjZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXRyYW5zbGF0ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXRyYW5zbGF0ZS1sb2FkZXItcGFydGlhbCcpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXVpLWJvb3RzdHJhcCcpO1xyXG5yZXF1aXJlKCduZ0NsaXBib2FyZCcpO1xyXG5yZXF1aXJlKCd1aS1yb3V0ZXInKTtcclxucmVxdWlyZSgncngtYW5ndWxhcicpO1xyXG5cclxuLy8gTW9kdWxlc1xyXG5pbXBvcnQgSFRUUE1vZHVsZSBmcm9tICcuLi9zZXJ2aWNlcy9odHRwL0hUVFBNb2R1bGUuanMnO1xyXG5pbXBvcnQgUmVzdEFQSU1vZHVsZSBmcm9tICcuLi9zZXJ2aWNlcy9SZXN0QVBJL1Jlc3RBUElNb2R1bGUuanMnXHJcbmltcG9ydCBIZWFkZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9oZWFkZXIvSGVhZGVyTW9kdWxlLmpzJztcclxuaW1wb3J0IExpY2Vuc2VBZG1pbk1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9MaWNlbnNlQWRtaW5Nb2R1bGUuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9saWNlbnNlTWFuYWdlci9MaWNlbnNlTWFuYWdlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBOb3RpY2VNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbm90aWNlTWFuYWdlci9Ob3RpY2VNYW5hZ2VyTW9kdWxlLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvdGFza01hbmFnZXIvVGFza01hbmFnZXJNb2R1bGUuanMnO1xyXG5cclxudmFyIFByb3ZpZGVyQ29yZSA9IHt9O1xyXG5cclxudmFyIFREU1RNID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNJywgW1xyXG4gICAgJ25nU2FuaXRpemUnLFxyXG4gICAgJ25nUmVzb3VyY2UnLFxyXG4gICAgJ25nQW5pbWF0ZScsXHJcbiAgICAncGFzY2FscHJlY2h0LnRyYW5zbGF0ZScsIC8vICdhbmd1bGFyLXRyYW5zbGF0ZSdcclxuICAgICd1aS5yb3V0ZXInLFxyXG4gICAgJ25nY2xpcGJvYXJkJyxcclxuICAgICdrZW5kby5kaXJlY3RpdmVzJyxcclxuICAgICdyeCcsXHJcbiAgICAndWkuYm9vdHN0cmFwJyxcclxuICAgIEhUVFBNb2R1bGUubmFtZSxcclxuICAgIFJlc3RBUElNb2R1bGUubmFtZSxcclxuICAgIEhlYWRlck1vZHVsZS5uYW1lLFxyXG4gICAgVGFza01hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIExpY2Vuc2VBZG1pbk1vZHVsZS5uYW1lLFxyXG4gICAgTGljZW5zZU1hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIE5vdGljZU1hbmFnZXJNb2R1bGUubmFtZVxyXG5dKS5jb25maWcoW1xyXG4gICAgJyRsb2dQcm92aWRlcicsXHJcbiAgICAnJHJvb3RTY29wZVByb3ZpZGVyJyxcclxuICAgICckY29tcGlsZVByb3ZpZGVyJyxcclxuICAgICckY29udHJvbGxlclByb3ZpZGVyJyxcclxuICAgICckcHJvdmlkZScsXHJcbiAgICAnJGh0dHBQcm92aWRlcicsXHJcbiAgICAnJHRyYW5zbGF0ZVByb3ZpZGVyJyxcclxuICAgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgICckdXJsUm91dGVyUHJvdmlkZXInLFxyXG4gICAgJyRsb2NhdGlvblByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkbG9nUHJvdmlkZXIsICRyb290U2NvcGVQcm92aWRlciwgJGNvbXBpbGVQcm92aWRlciwgJGNvbnRyb2xsZXJQcm92aWRlciwgJHByb3ZpZGUsICRodHRwUHJvdmlkZXIsXHJcbiAgICAgICAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLCAkdXJsUm91dGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAgICAgJHJvb3RTY29wZVByb3ZpZGVyLmRpZ2VzdFR0bCgzMCk7XHJcblxyXG4gICAgICAgICRsb2dQcm92aWRlci5kZWJ1Z0VuYWJsZWQodHJ1ZSk7XHJcblxyXG4gICAgICAgIC8vIEFmdGVyIGJvb3RzdHJhcHBpbmcgYW5ndWxhciBmb3JnZXQgdGhlIHByb3ZpZGVyIHNpbmNlIGV2ZXJ5dGhpbmcgXCJ3YXMgYWxyZWFkeSBsb2FkZWRcIlxyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIgPSAkY29tcGlsZVByb3ZpZGVyO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5jb250cm9sbGVyUHJvdmlkZXIgPSAkY29udHJvbGxlclByb3ZpZGVyO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZSA9ICRwcm92aWRlO1xyXG4gICAgICAgIFByb3ZpZGVyQ29yZS5odHRwUHJvdmlkZXIgPSAkaHR0cFByb3ZpZGVyO1xyXG5cclxuICAgICAgICAvKipcclxuICAgICAgICAgKiBUcmFuc2xhdGlvbnNcclxuICAgICAgICAgKi9cclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnVzZVNhbml0aXplVmFsdWVTdHJhdGVneShudWxsKTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCd0ZHN0bScpO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIudXNlTG9hZGVyKCckdHJhbnNsYXRlUGFydGlhbExvYWRlcicsIHtcclxuICAgICAgICAgICAgdXJsVGVtcGxhdGU6ICcuLi9pMThuL3twYXJ0fS9hcHAuaTE4bi17bGFuZ30uanNvbidcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnByZWZlcnJlZExhbmd1YWdlKCdlbl9VUycpO1xyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci5mYWxsYmFja0xhbmd1YWdlKCdlbl9VUycpO1xyXG5cclxuICAgICAgICAvLyR1cmxSb3V0ZXJQcm92aWRlci5vdGhlcndpc2UoJ2Rhc2hib2FyZCcpO1xyXG5cclxuICAgIH1dKS5cclxuICAgIHJ1bihbJyRyb290U2NvcGUnLCAnJGh0dHAnLCAnJGxvZycsICckbG9jYXRpb24nLCBmdW5jdGlvbiAoJHJvb3RTY29wZSwgJGh0dHAsICRsb2csICRsb2NhdGlvbiwgJHN0YXRlLCAkc3RhdGVQYXJhbXMsICRsb2NhbGUpIHtcclxuICAgICAgICAkbG9nLmRlYnVnKCdDb25maWd1cmF0aW9uIGRlcGxveWVkJyk7XHJcblxyXG4gICAgICAgICRyb290U2NvcGUuJG9uKCckc3RhdGVDaGFuZ2VTdGFydCcsIGZ1bmN0aW9uIChldmVudCwgdG9TdGF0ZSwgdG9QYXJhbXMsIGZyb21TdGF0ZSwgZnJvbVBhcmFtcykge1xyXG4gICAgICAgICAgICAkbG9nLmRlYnVnKCdTdGF0ZSBDaGFuZ2UgdG8gJyArIHRvU3RhdGUubmFtZSk7XHJcbiAgICAgICAgICAgIGlmICh0b1N0YXRlLmRhdGEgJiYgdG9TdGF0ZS5kYXRhLnBhZ2UpIHtcclxuICAgICAgICAgICAgICAgIHdpbmRvdy5kb2N1bWVudC50aXRsZSA9IHRvU3RhdGUuZGF0YS5wYWdlLnRpdGxlO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgfV0pO1xyXG5cclxuLy8gd2UgbWFwcGVkIHRoZSBQcm92aWRlciBDb3JlIGxpc3QgKGNvbXBpbGVQcm92aWRlciwgY29udHJvbGxlclByb3ZpZGVyLCBwcm92aWRlU2VydmljZSwgaHR0cFByb3ZpZGVyKSB0byByZXVzZSBhZnRlciBvbiBmbHlcclxuVERTVE0uUHJvdmlkZXJDb3JlID0gUHJvdmlkZXJDb3JlO1xyXG5cclxubW9kdWxlLmV4cG9ydHMgPSBURFNUTTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEvMTcvMTUuXHJcbiAqIFdoZW4gQW5ndWxhciBpbnZva2VzIHRoZSBsaW5rIGZ1bmN0aW9uLCBpdCBpcyBubyBsb25nZXIgaW4gdGhlIGNvbnRleHQgb2YgdGhlIGNsYXNzIGluc3RhbmNlLCBhbmQgdGhlcmVmb3JlIGFsbCB0aGlzIHdpbGwgYmUgdW5kZWZpbmVkXHJcbiAqIFRoaXMgc3RydWN0dXJlIHdpbGwgYXZvaWQgdGhvc2UgaXNzdWVzLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0J1xyXG5cclxuLy8gQ29udHJvbGxlciBpcyBiZWluZyB1c2VkIHRvIEluamVjdCBhbGwgRGVwZW5kZW5jaWVzXHJcbmNsYXNzIFNWR0xvYWRlckNvbnRyb2xsZXIge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZykge1xyXG4gICAgICAgIHRoaXMuJGxvZyA9ICRsb2c7XHJcbiAgICB9XHJcbn1cclxuXHJcblNWR0xvYWRlckNvbnRyb2xsZXIuJGluamVjdCA9IFsnJGxvZyddO1xyXG5cclxuLy8gRGlyZWN0aXZlXHJcbmNsYXNzIFNWR0xvYWRlciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoKXtcclxuICAgICAgICB0aGlzLnJlc3RyaWN0ID0gJ0UnO1xyXG4gICAgICAgIHRoaXMuY29udHJvbGxlciA9IFNWR0xvYWRlckNvbnRyb2xsZXI7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9IHtcclxuICAgICAgICAgICAgc3ZnRGF0YTogJz0nXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBsaW5rKHNjb3BlLCBlbGVtZW50LCBhdHRycywgY3RybCkge1xyXG4gICAgICAgIGVsZW1lbnQuaHRtbChzY29wZS5zdmdEYXRhKTtcclxuICAgIH1cclxuXHJcbiAgICBzdGF0aWMgZGlyZWN0aXZlKCkge1xyXG4gICAgICAgIHJldHVybiBuZXcgU1ZHTG9hZGVyKCk7XHJcbiAgICB9XHJcbn1cclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBTVkdMb2FkZXIuZGlyZWN0aXZlO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMwLzEwLzIwMTYuXHJcbiAqIExpc3RlbiB0byBNb2RhbCBXaW5kb3cgdG8gbWFrZSBhbnkgbW9kYWwgd2luZG93IGRyYWdnYWJibGVcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgnbW9kYWxSZW5kZXInLCBbJyRsb2cnLCBmdW5jdGlvbiAoJGxvZykge1xyXG4gICAgJGxvZy5kZWJ1ZygnTW9kYWxXaW5kb3dBY3RpdmF0aW9uIGxvYWRlZCcpO1xyXG4gICAgcmV0dXJuIHtcclxuICAgICAgICByZXN0cmljdDogJ0VBJyxcclxuICAgICAgICBsaW5rOiBmdW5jdGlvbigpIHtcclxuICAgICAgICAgICAgJCgnLm1vZGFsLWRpYWxvZycpLmRyYWdnYWJsZSh7XHJcbiAgICAgICAgICAgICAgICBoYW5kbGU6ICcubW9kYWwtaGVhZGVyJ1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG59XSk7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBQcmludHMgb3V0IGFsbCBUb2FzdCBtZXNzYWdlIHdoZW4gZGV0ZWN0ZWQgZnJvbSBzZXJ2ZXIgb3IgY3VzdG9tIG1zZyB1c2luZyB0aGUgZGlyZWN0aXZlIGl0c2VsZlxyXG4gKlxyXG4gKiBQcm9iYWJseSB2YWx1ZXMgYXJlOlxyXG4gKlxyXG4gKiBzdWNjZXNzLCBkYW5nZXIsIGluZm8sIHdhcm5pbmdcclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4uLy4uL2NvbmZpZy9BcHAuanMnKTtcclxuXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSgndG9hc3RIYW5kbGVyJywgWyckbG9nJywgJyR0aW1lb3V0JywgJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLFxyXG4gICAgJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicsICdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICBmdW5jdGlvbiAoJGxvZywgJHRpbWVvdXQsIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLFxyXG4gICAgICAgICAgICAgIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciwgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IpIHtcclxuXHJcbiAgICAkbG9nLmRlYnVnKCdUb2FzdEhhbmRsZXIgbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHNjb3BlOiB7XHJcbiAgICAgICAgICAgIG1zZzogJz0nLFxyXG4gICAgICAgICAgICB0eXBlOiAnPScsXHJcbiAgICAgICAgICAgIHN0YXR1czogJz0nXHJcbiAgICAgICAgfSxcclxuICAgICAgICBwcmlvcml0eTogNSxcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9kaXJlY3RpdmVzL1Rvb2xzL1RvYXN0SGFuZGxlci5odG1sJyxcclxuICAgICAgICByZXN0cmljdDogJ0UnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6IFsnJHNjb3BlJywgJyRyb290U2NvcGUnLCBmdW5jdGlvbiAoJHNjb3BlLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgICAgICRzY29wZS5hbGVydCA9IHtcclxuICAgICAgICAgICAgICAgIHN1Y2Nlc3M6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgZGFuZ2VyOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGluZm86IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgd2FybmluZzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIH07XHJcblxyXG4gICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3MgPSB7XHJcbiAgICAgICAgICAgICAgICBzaG93OiBmYWxzZVxyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgZnVuY3Rpb24gdHVybk9mZk5vdGlmaWNhdGlvbnMoKXtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5zdWNjZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmluZm8uc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0Lndhcm5pbmcuc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIExpc3RlbiB0byBhbnkgcmVxdWVzdCwgd2UgY2FuIHJlZ2lzdGVyIGxpc3RlbmVyIGlmIHdlIHdhbnQgdG8gYWRkIGV4dHJhIGNvZGUuXHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5SZXF1ZXN0KCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihjb25maWcpe1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVxdWVzdCB0bzogJywgIGNvbmZpZyk7XHJcbiAgICAgICAgICAgICAgICB2YXIgdGltZSA9IGNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wO1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1Zyh0aW1lKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLmxpc3RlbkVycm9yKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZWplY3Rpb24pe1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVxdWVzdCBlcnJvcjogJywgIHJlamVjdGlvbik7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5SZXNwb25zZSgpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVzcG9uc2Upe1xyXG4gICAgICAgICAgICAgICAgdmFyIHRpbWUgPSByZXNwb25zZS5jb25maWcucmVzcG9uc2VUaW1lc3RhbXAgLSByZXNwb25zZS5jb25maWcucmVxdWVzdFRpbWVzdGFtcDtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1RoZSByZXF1ZXN0IHRvb2sgJyArICh0aW1lIC8gMTAwMCkgKyAnIHNlY29uZHMnKTtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1Jlc3BvbnNlIHJlc3VsdDogJywgcmVzcG9uc2UpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1Jlc3BvbnNlIGVycm9yOiAnLCByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnN0YXR1cyA9IHJlamVjdGlvbi5zdGF0dXM7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnN0YXR1c1RleHQgPSByZWplY3Rpb24uc3RhdHVzVGV4dDtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuZXJyb3JzID0gcmVqZWN0aW9uLmRhdGEuZXJyb3JzO1xyXG4gICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsIDMwMDApO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBIaWRlIHRoZSBQb3AgdXAgbm90aWZpY2F0aW9uIG1hbnVhbGx5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUub25DYW5jZWxQb3BVcCA9IGZ1bmN0aW9uKCkge1xyXG4gICAgICAgICAgICAgICAgdHVybk9mZk5vdGlmaWNhdGlvbnMoKTtcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBJdCB3YXRjaCB0aGUgdmFsdWUgdG8gc2hvdyB0aGUgbXNnIGlmIG5lY2Vzc2FyeVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHJvb3RTY29wZS4kb24oJ2Jyb2FkY2FzdC1tc2cnLCBmdW5jdGlvbihldmVudCwgYXJncykge1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnYnJvYWRjYXN0LW1zZyBleGVjdXRlZCcpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zdGF0dXNUZXh0ID0gYXJncy50ZXh0O1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc3RhdHVzID0gbnVsbDtcclxuICAgICAgICAgICAgICAgICR0aW1lb3V0KHR1cm5PZmZOb3RpZmljYXRpb25zLCAyMDAwKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS4kYXBwbHkoKTsgLy8gcm9vdFNjb3BlIGFuZCB3YXRjaCBleGNsdWRlIHRoZSBhcHBseSBhbmQgbmVlZHMgdGhlIG5leHQgY3ljbGUgdG8gcnVuXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkc2NvcGUuJHdhdGNoKCdtc2cnLCBmdW5jdGlvbihuZXdWYWx1ZSwgb2xkVmFsdWUpIHtcclxuICAgICAgICAgICAgICAgIGlmIChuZXdWYWx1ZSAmJiBuZXdWYWx1ZSAhPT0gJycpIHtcclxuICAgICAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbJHNjb3BlLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzVGV4dCA9IG5ld1ZhbHVlO1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc3RhdHVzID0gJHNjb3BlLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMjUwMCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICB9XVxyXG4gICAgfTtcclxufV0pO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzE0LzIwMTUuXHJcbiAqIEl0IGhhbmRsZXIgdGhlIGluZGV4IGZvciBhbnkgb2YgdGhlIGRpcmVjdGl2ZXMgYXZhaWxhYmxlXHJcbiAqL1xyXG5cclxucmVxdWlyZSgnLi9Ub29scy9Ub2FzdEhhbmRsZXIuanMnKTtcclxucmVxdWlyZSgnLi9Ub29scy9Nb2RhbFdpbmRvd0FjdGl2YXRpb24uanMnKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8xNy8yMDE1LlxyXG4gKi9cclxuXHJcbi8vIE1haW4gQW5ndWxhckpzIGNvbmZpZ3VyYXRpb25cclxucmVxdWlyZSgnLi9jb25maWcvQXBwLmpzJyk7XHJcblxyXG4vLyBIZWxwZXJzXHJcbnJlcXVpcmUoJy4vY29uZmlnL0FuZ3VsYXJQcm92aWRlckhlbHBlci5qcycpO1xyXG5cclxuLy8gRGlyZWN0aXZlc1xyXG5yZXF1aXJlKCcuL2RpcmVjdGl2ZXMvaW5kZXgnKTtcclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIERpYWxvZ0FjdGlvbiB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMudGl0bGUgPSBwYXJhbXMudGl0bGU7XHJcbiAgICAgICAgdGhpcy5tZXNzYWdlID0gcGFyYW1zLm1lc3NhZ2U7XHJcblxyXG4gICAgfVxyXG4gICAgLyoqXHJcbiAgICAgKiBBY2NjZXB0IGFuZCBDb25maXJtXHJcbiAgICAgKi9cclxuICAgIGNvbmZpcm1BY3Rpb24oKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMi8yMDE1LlxyXG4gKiBIZWFkZXIgQ29udHJvbGxlciBtYW5hZ2UgdGhlIHZpZXcgYXZhaWxhYmxlIG9uIHRoZSBzdGF0ZS5kYXRhXHJcbiAqIC0tLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICogSGVhZGVyIENvbnRyb2xsZXJcclxuICogUGFnZSB0aXRsZSAgICAgICAgICAgICAgICAgICAgICBIb21lIC0+IExheW91dCAtIFN1YiBMYXlvdXRcclxuICpcclxuICogTW9kdWxlIENvbnRyb2xsZXJcclxuICogQ29udGVudFxyXG4gKiAtLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSGVhZGVyQ29udHJvbGxlciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nXHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuXHJcbiAgICAgICAgdGhpcy5wYWdlTWV0YURhdGEgPSB7XHJcbiAgICAgICAgICAgIHRpdGxlOiAnJyxcclxuICAgICAgICAgICAgaW5zdHJ1Y3Rpb246ICcnLFxyXG4gICAgICAgICAgICBtZW51OiBbXVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZUhlYWRlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIZWFkZXIgQ29udHJvbGxlciBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFZlcmlmeSBpZiB3ZSBoYXZlIGEgbWVudSB0byBzaG93IHRvIG1hZGUgaXQgYXZhaWxhYmxlIHRvIHRoZSBWaWV3XHJcbiAgICAgKi9cclxuICAgIHByZXBhcmVIZWFkZXIoKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuc3RhdGUgJiYgdGhpcy5zdGF0ZS4kY3VycmVudCAmJiB0aGlzLnN0YXRlLiRjdXJyZW50LmRhdGEpIHtcclxuICAgICAgICAgICAgdGhpcy5wYWdlTWV0YURhdGEgPSB0aGlzLnN0YXRlLiRjdXJyZW50LmRhdGEucGFnZTtcclxuICAgICAgICAgICAgZG9jdW1lbnQudGl0bGUgPSB0aGlzLnBhZ2VNZXRhRGF0YS50aXRsZTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMS8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IEhlYWRlckNvbnRyb2xsZXIgZnJvbSAnLi9IZWFkZXJDb250cm9sbGVyLmpzJztcclxuaW1wb3J0IERpYWxvZ0FjdGlvbiBmcm9tICcuLi9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmpzJztcclxuXHJcbnZhciBIZWFkZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uSGVhZGVyTW9kdWxlJywgW10pO1xyXG5cclxuSGVhZGVyTW9kdWxlLmNvbnRyb2xsZXIoJ0hlYWRlckNvbnRyb2xsZXInLCBbJyRsb2cnLCAnJHN0YXRlJywgSGVhZGVyQ29udHJvbGxlcl0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5IZWFkZXJNb2R1bGUuY29udHJvbGxlcignRGlhbG9nQWN0aW9uJywgWyckbG9nJywnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIERpYWxvZ0FjdGlvbl0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgSGVhZGVyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VBZG1pbkxpc3QgZnJvbSAnLi9saXN0L0xpY2Vuc2VBZG1pbkxpc3QuanMnO1xyXG5pbXBvcnQgTGljZW5zZUFkbWluU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZUFkbWluU2VydmljZS5qcyc7XHJcbmltcG9ydCBSZXF1ZXN0TGljZW5zZSBmcm9tICcuL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuanMnO1xyXG5pbXBvcnQgQ3JlYXRlZExpY2Vuc2UgZnJvbSAnLi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmpzJztcclxuaW1wb3J0IEFwcGx5TGljZW5zZUtleSBmcm9tICcuL2FwcGx5TGljZW5zZUtleS9BcHBseUxpY2Vuc2VLZXkuanMnO1xyXG5pbXBvcnQgTWFudWFsbHlSZXF1ZXN0IGZyb20gJy4vbWFudWFsbHlSZXF1ZXN0L01hbnVhbGx5UmVxdWVzdC5qcyc7XHJcbmltcG9ydCBMaWNlbnNlRGV0YWlsIGZyb20gJy4vZGV0YWlsL0xpY2Vuc2VEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlQWRtaW5Nb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uTGljZW5zZUFkbWluTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdsaWNlbnNlQWRtaW4nKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VBZG1pbkxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdBZG1pbmlzdGVyIExpY2Vuc2VzJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FETUlOJywgJ0xJQ0VOU0UnLCAnTElTVCddfX0sXHJcbiAgICAgICAgICAgIHVybDogJy9saWNlbnNlL2FkbWluL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9saXN0L0xpY2Vuc2VBZG1pbkxpc3QuaHRtbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VBZG1pbkxpc3QgYXMgbGljZW5zZUFkbWluTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuc2VydmljZSgnTGljZW5zZUFkbWluU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCAnJHJvb3RTY29wZScsIExpY2Vuc2VBZG1pblNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlQWRtaW5MaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsIExpY2Vuc2VBZG1pbkxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ1JlcXVlc3RMaWNlbnNlJywgWyckbG9nJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCBSZXF1ZXN0TGljZW5zZV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignQ3JlYXRlZExpY2Vuc2UnLCBbJyRsb2cnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgQ3JlYXRlZExpY2Vuc2VdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0FwcGx5TGljZW5zZUtleScsIFsnJGxvZycsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJyRyb290U2NvcGUnLCAncGFyYW1zJywgQXBwbHlMaWNlbnNlS2V5XSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdNYW51YWxseVJlcXVlc3QnLCBbJyRsb2cnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBNYW51YWxseVJlcXVlc3RdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VEZXRhaWwnLCBbJyRsb2cnLCAnTGljZW5zZUFkbWluU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTGljZW5zZURldGFpbF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VBZG1pbk1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI4LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQXBwbHlMaWNlbnNlS2V5IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHBhcmFtcy5saWNlbnNlO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIGFwcGx5S2V5KCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5hcHBseUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIENyZWF0ZWRSZXF1ZXN0TGljZW5zZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMuY2xpZW50ID0gcGFyYW1zO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZURldGFpbCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0kdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBtZXRob2RJZDogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLmlkLFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudDogcGFyYW1zLmxpY2Vuc2UuZW52aXJvbm1lbnQsXHJcbiAgICAgICAgICAgIGluY2VwdGlvbjogcGFyYW1zLmxpY2Vuc2UuaW5jZXB0aW9uLFxyXG4gICAgICAgICAgICBleHBpcmF0aW9uOiBwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uLFxyXG4gICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiBwYXJhbXMubGljZW5zZS5zcGVjaWFsSW5zdHJ1Y3Rpb25zLFxyXG4gICAgICAgICAgICBhcHBsaWVkOiBwYXJhbXMubGljZW5zZS5hcHBsaWVkLFxyXG4gICAgICAgICAgICBrZXlJZDogcGFyYW1zLmxpY2Vuc2Uua2V5SWQsXHJcbiAgICAgICAgICAgIHJlcGxhY2VkOiBwYXJhbXMubGljZW5zZS5yZXBsYWNlZCxcclxuICAgICAgICAgICAgZW5jcnlwdGVkRGV0YWlsOiBwYXJhbXMubGljZW5zZS5lbmNyeXB0ZWREZXRhaWxcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLiBwcmVwYXJlTWV0aG9kT3B0aW9ucygpO1xyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVNZXRob2RPcHRpb25zKCkge1xyXG4gICAgICAgIHRoaXMubWV0aG9kT3B0aW9ucyA9IFtcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnU2VydmVycydcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnVG9rZW5zJ1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMyxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdDdXN0b20nXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdXHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGUgdXNlciBhcHBseSBhbmQgc2VydmVyIHNob3VsZCB2YWxpZGF0ZSB0aGUga2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgYXBwbHlMaWNlbnNlS2V5KCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQXBwbHlMaWNlbnNlS2V5IGFzIGFwcGx5TGljZW5zZUtleScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQgPSB0cnVlO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogT3BlbnMgYSBkaWFsb2cgYW5kIGFsbG93IHRoZSB1c2VyIHRvIG1hbnVhbGx5IHNlbmQgdGhlIHJlcXVlc3Qgb3IgY29weSB0aGUgZW5jcmlwdGVkIGNvZGVcclxuICAgICAqL1xyXG4gICAgbWFudWFsbHlSZXF1ZXN0KCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTWFudWFsbHlSZXF1ZXN0IGFzIG1hbnVhbGx5UmVxdWVzdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdCgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UucmVzdWJtaXRMaWNlbnNlUmVxdWVzdCh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHt9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIGRlbGV0ZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmRlbGV0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5MaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VBZG1pbkxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vblJlcXVlc3ROZXdMaWNlbnNlKClcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gUmVxdWVzdCBOZXcgTGljZW5zZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0LnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdsaWNlbnNlSWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3Qub25MaWNlbnNlRGV0YWlscyh0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2xpZW50JywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QnLCB0aXRsZTogJ1Byb2plY3QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NvbnRhY3RfZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlJywgdGl0bGU6ICdUeXBlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubmFtZScsIHRpdGxlOiAnTWV0aG9kJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QuaWQnLCBoaWRkZW46IHRydWV9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc2VydmVyc190b2tlbnMnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2luY2VwdGlvbicsIHRpdGxlOiAnSW5jZXB0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uJywgdGl0bGU6ICdFeHBpcmF0aW9uJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdlbnZpcm9ubWVudCcsIHRpdGxlOiAnRW52Lid9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIC8qdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLnRlc3RTZXJ2aWNlKChkYXRhKSA9PiB7Ki9cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHZhciBkYXRhID0gW1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbGljZW5zZUlkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBrZXlJZDogJ2NlNDJjZmQxLTFhYzUtNGZjYy1iZTVjLWNjNzg4NWM4ZjgzYicsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFjdGlvbjogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNsaWVudDogJ24vYScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByb2plY3Q6ICduL2EnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjb250YWN0X2VtYWlsOiAnd2VzdC5jb2FzdEB4eXl5LmNvbScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN0YXR1czogJ0FjdGl2ZScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHR5cGU6ICdNdWx0aS1Qcm9qZWN0JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbWV0aG9kOiAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnU2VydmVyJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzZXJ2ZXJzX3Rva2VuczogJzgwMDAnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpbmNlcHRpb246ICcyMDE2LTA5LTE1JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZXhwaXJhdGlvbjogJzIwMTYtMTItMDEnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbnZpcm9ubWVudDogJ1Byb2R1Y3Rpb24nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiAnSGVscCwgSGVscCwgSGVscCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFwcGxpZWQ6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXBsYWNlZDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZGF0ZTogbmV3IERhdGUoKSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHNlcnZlclVybDogJ2h0dHA6YmxhYmxhYmEuY29tJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdhYXNkYXM1NC01YXNkNGE1c2QtYXNkNDVhNHNkJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbmNyeXB0ZWREZXRhaWw6ICdhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOGFzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4YXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MTgnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGxpY2Vuc2VJZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAga2V5SWQ6ICdkZjQyZGdlMi0yYmQ2LTVnZGQtY2Y2ZC1kZDg5OTZkOWc5NGMnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhY3Rpb246ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjbGllbnQ6ICdBY21lIEluYy4nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBwcm9qZWN0OiAnRFIgUmVsbycsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNvbnRhY3RfZW1haWw6ICdqaW0ubGF1Y2hlckBhY21lLmNvbScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN0YXR1czogJ1BlbmRpbmcnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0eXBlOiAnUHJvamVjdCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG1ldGhvZDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnVG9rZW4nXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHNlcnZlcnNfdG9rZW5zOiAnMTUwMDAnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpbmNlcHRpb246ICcyMDE2LTA5LTAxJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZXhwaXJhdGlvbjogJzIwMTYtMTAtMDEnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbnZpcm9ubWVudDogJ0RlbW8nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYXBwbGllZDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVwbGFjZWQ6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGRhdGU6IG5ldyBEYXRlKCksXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzZXJ2ZXJVcmw6ICdodHRwOmJsYWJsYWJhLmNvbScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnYmFzZmFzZC0yYXBoZ29zZGYtYXNvcXdlcXdlJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbmNyeXB0ZWREZXRhaWw6ICdhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOGFzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4YXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MTgnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgXTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAvKiB9KTsqL1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0TGljZW5zZSBhcyByZXF1ZXN0TGljZW5zZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIGRyYWdnYWJsZTogdHJ1ZVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIENyZWF0ZWQ6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLm9uTmV3TGljZW5zZUNyZWF0ZWQobGljZW5zZSk7XHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZUFkbWluTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEFmdGVyIGNsaWNraW5nIG9uIGVkaXQsIHdlIHJlZGlyZWN0IHRoZSB1c2VyIHRvIHRoZSBFZGl0aW9uIHNjcmVlbiBpbnN0ZWFkIG9mIG9wZW4gYSBkaWFsb2dcclxuICAgICAqIGR1IHRoZSBzaXplIG9mIHRoZSBpbnB1dHNcclxuICAgICAqL1xyXG4gICAgb25MaWNlbnNlRGV0YWlscyhsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnT3BlbiBEZXRhaWxzIGZvcjogJywgbGljZW5zZSk7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2RldGFpbC9MaWNlbnNlRGV0YWlsLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZURldGFpbCBhcyBsaWNlbnNlRGV0YWlsJyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgdmFyIGRhdGFJdGVtID0gbGljZW5zZSAmJiBsaWNlbnNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IGRhdGFJdGVtIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcblxyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBvbk5ld0xpY2Vuc2VDcmVhdGVkKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vY3JlYXRlZC9DcmVhdGVkTGljZW5zZS5odG1sJyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0NyZWF0ZWRMaWNlbnNlIGFzIGNyZWF0ZWRMaWNlbnNlJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgaWQ6IDUwLCBuYW1lOiAnQWNtZSwgSW5jLicsIGVtYWlsOiAnYWNtZUBpbmMuY29tJyAgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBNYW51YWxseVJlcXVlc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0gcGFyYW1zLmxpY2Vuc2U7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIGFuZCB2YWxpZGF0ZSB0aGUgS2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgZW1haWxSZXF1ZXN0KCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5lbWFpbFJlcXVlc3QodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKiBDcmVhdGUgYSBuZXcgUmVxdWVzdCB0byBnZXQgYSBMaWNlbnNlXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdExpY2Vuc2Uge1xyXG5cclxuICAgIC8qKlxyXG4gICAgICogSW5pdGlhbGl6ZSBhbGwgdGhlIHByb3BlcnRpZXNcclxuICAgICAqIEBwYXJhbSAkbG9nXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZUFkbWluU2VydmljZVxyXG4gICAgICogQHBhcmFtICR1aWJNb2RhbEluc3RhbmNlXHJcbiAgICAgKi9cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbEluc3RhbmNlKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIC8vIERlZmluZWQgdGhlIEVudmlyb25tZW50IFNlbGVjdFxyXG4gICAgICAgIHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlID0gW107XHJcbiAgICAgICAgLy8gRGVmaW5lIHRoZSBQcm9qZWN0IFNlbGVjdFxyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdCA9IHt9O1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0gW107XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXRQcm9qZWN0RGF0YVNvdXJjZSgpO1xyXG5cclxuICAgICAgICAvLyBDcmVhdGUgdGhlIE1vZGVsIGZvciB0aGUgTmV3IExpY2Vuc2VcclxuICAgICAgICB0aGlzLm5ld0xpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgY29udGFjdEVtYWlsOiAnJyxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnRJZDogMCxcclxuICAgICAgICAgICAgcHJvamVjdElkOiAwLFxyXG4gICAgICAgICAgICBjbGllbnROYW1lOiAnJyxcclxuICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogJydcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKT0+e1xyXG4gICAgICAgICAgICB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSA9IGRhdGE7XHJcbiAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLmVudmlyb25tZW50SWQgPSBkYXRhWzBdLmlkO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIFByb2plY3QgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLnByb2plY3RJZCA9IGRhdGFbMF0uaWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGF0YVRleHRGaWVsZDogJ25hbWUnLFxyXG4gICAgICAgICAgICBkYXRhVmFsdWVGaWVsZDogJ2lkJyxcclxuICAgICAgICAgICAgdmFsdWVQcmltaXRpdmU6IHRydWUsXHJcbiAgICAgICAgICAgIHNlbGVjdDogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAvLyBPbiBQcm9qZWN0IENoYW5nZSwgc2VsZWN0IHRoZSBDbGllbnQgTmFtZVxyXG4gICAgICAgICAgICAgICAgdmFyIGl0ZW0gPSB0aGlzLnNlbGVjdFByb2plY3QuZGF0YUl0ZW0oZS5pdGVtKTtcclxuICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLmNsaWVudE5hbWUgPSBpdGVtLmNsaWVudC5uYW1lO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIHRoZSBTZXJ2aWNlIGNhbGwgdG8gZ2VuZXJhdGUgYSBuZXcgTGljZW5zZSByZXF1ZXN0XHJcbiAgICAgKi9cclxuICAgIHNhdmVMaWNlbnNlUmVxdWVzdCgpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdOZXcgTGljZW5zZSBSZXF1ZXN0ZWQ6ICcsIHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QodGhpcy5uZXdMaWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5TZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIsICRyb290U2NvcGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLnJvb3RTY29wZSA9ICRyb290U2NvcGU7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ2xpY2Vuc2VBZG1pblNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICB0ZXN0U2VydmljZShjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0KGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLnJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ1JlcXVlc3QgTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5Lid9KTtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGVtYWlsUmVxdWVzdChsaWNlbnNlLCBjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5lbWFpbFJlcXVlc3QobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ1JlcXVlc3QgTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5Lid9KTtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldExpY2Vuc2VMaXN0KGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBhcHBseUxpY2Vuc2UobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuYXBwbHlMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIC8vaWYoZGF0YS5hcHBsaWVkKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnTGljZW5zZSB3YXMgc3VjY2Vzc2Z1bGx5IGFwcGxpZWQnfSk7XHJcbiAgICAgICAgICAgIC8qfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6ICdMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkgYXBwbGllZCd9KTtcclxuICAgICAgICAgICAgfSovXHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmRlbGV0ZUxpY2Vuc2UobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RJbXBvcnQgZnJvbSAnLi9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbGljZW5zZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VNYW5hZ2VyTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0xpY2Vuc2luZyBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ01BTkFHRVInLCAnTElDRU5TRScsICdMSVNUJ119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvbWFuYWdlci9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJMaXN0IGFzIGxpY2Vuc2VNYW5hZ2VyTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgJyRyb290U2NvcGUnLCBMaWNlbnNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlTWFuYWdlckxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlTWFuYWdlckxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignUmVxdWVzdEltcG9ydCcsIFsnJGxvZycsICdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCBSZXF1ZXN0SW1wb3J0XSk7XHJcbkxpY2Vuc2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VNYW5hZ2VyRGV0YWlsJywgWyckbG9nJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTGljZW5zZU1hbmFnZXJEZXRhaWxdKTtcclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBMaWNlbnNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJEZXRhaWwge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UgPSBsaWNlbnNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9JHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBwcmluY2lwYWxJZDogcGFyYW1zLmxpY2Vuc2UucHJpbmNpcGFsLmlkLFxyXG4gICAgICAgICAgICBlbWFpbDogcGFyYW1zLmxpY2Vuc2UuY29udGFjdF9lbWFpbCxcclxuICAgICAgICAgICAgcHJvamVjdElkOiBwYXJhbXMubGljZW5zZS5wcm9qZWN0LmlkLFxyXG4gICAgICAgICAgICBjbGllbnRJZDogcGFyYW1zLmxpY2Vuc2UuY2xpZW50LmlkLFxyXG4gICAgICAgICAgICBzdGF0dXNJZDogcGFyYW1zLmxpY2Vuc2Uuc3RhdHVzLmlkLFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5tZXRob2QuaWQsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiBwYXJhbXMubGljZW5zZS5tZXRob2QubmFtZSxcclxuICAgICAgICAgICAgICAgIHF1YW50aXR5OiBwYXJhbXMubGljZW5zZS5tZXRob2QucXVhbnRpdHlcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnRJZDogcGFyYW1zLmxpY2Vuc2UuZW52aXJvbm1lbnQuaWQsXHJcbiAgICAgICAgICAgIHJlcXVlc3RlZDogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdGVkLFxyXG4gICAgICAgICAgICBpbml0RGF0ZTogcGFyYW1zLmxpY2Vuc2UuaW5pdERhdGUsXHJcbiAgICAgICAgICAgIGVuZERhdGU6IHBhcmFtcy5saWNlbnNlLmVuZERhdGUsXHJcbiAgICAgICAgICAgIHNwZWNpYWxJbnN0cnVjdGlvbnM6IHBhcmFtcy5saWNlbnNlLnNwZWNpYWxJbnN0cnVjdGlvbnMsXHJcbiAgICAgICAgICAgIGJhbm5lck1lc3NhZ2U6IHBhcmFtcy5saWNlbnNlLmJhbm5lck1lc3NhZ2UsXHJcbiAgICAgICAgICAgIHJlcXVlc3RlZElkOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0ZWRJZCxcclxuICAgICAgICAgICAgcmVwbGFjZWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkLFxyXG4gICAgICAgICAgICByZXBsYWNlZElkOiBwYXJhbXMubGljZW5zZS5yZXBsYWNlZElkLFxyXG4gICAgICAgICAgICBsaWNlbnNlS2V5OiBwYXJhbXMubGljZW5zZS5saWNlbnNlS2V5LFxyXG4gICAgICAgICAgICBhY3Rpdml0eUxpc3Q6IHBhcmFtcy5saWNlbnNlLmFjdGl2aXR5TGlzdCxcclxuICAgICAgICAgICAgaG9zdE5hbWU6IHBhcmFtcy5saWNlbnNlLmhvc3ROYW1lLFxyXG4gICAgICAgICAgICB3ZWJzaXRlTmFtZTogcGFyYW1zLmxpY2Vuc2Uud2Vic2l0ZU5hbWUsXHJcbiAgICAgICAgICAgIGhhc2g6IHBhcmFtcy5saWNlbnNlLmhhc2gsXHJcblxyXG4gICAgICAgICAgICBhcHBsaWVkOiBwYXJhbXMubGljZW5zZS5hcHBsaWVkLFxyXG4gICAgICAgICAgICBrZXlJZDogcGFyYW1zLmxpY2Vuc2Uua2V5SWRcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICAvLyBDcmVhdGVzIHRoZSBLZW5kbyBQcm9qZWN0IFNlbGVjdCBMaXN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHRoaXMuZ2V0UHJvamVjdHNEYXRhU291cmNlKCksXHJcbiAgICAgICAgICAgIG9wdGlvbkxhYmVsOiAnU2VsZWN0IGEgUHJvamVjdCcsXHJcbiAgICAgICAgICAgIGRhdGFUZXh0RmllbGQ6ICduYW1lJyxcclxuICAgICAgICAgICAgZGF0YVZhbHVlRmllbGQ6ICdpZCcsXHJcbiAgICAgICAgICAgIHZhbHVlUHJpbWl0aXZlOiB0cnVlLFxyXG4gICAgICAgICAgICBzZWxlY3Q6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdmFyIGl0ZW0gPSB0aGlzLnNlbGVjdFByb2plY3QuZGF0YUl0ZW0oZS5pdGVtKTtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VQcm9qZWN0KGl0ZW0pO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIC8vIEluaXQgdGhlIHR3byBLZW5kbyBEYXRlcyBmb3IgSW5pdCBhbmQgRW5kRGF0ZVxyXG4gICAgICAgIHRoaXMuaW5pdERhdGUgPSB7fTtcclxuICAgICAgICB0aGlzLmluaXREYXRlT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZm9ybWF0OiAneXl5eS9NTS9kZCcsXHJcbiAgICAgICAgICAgIG1heDogdGhpcy5saWNlbnNlTW9kZWwuZW5kRGF0ZSxcclxuICAgICAgICAgICAgY2hhbmdlOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuZW5kRGF0ZSA9IHt9O1xyXG4gICAgICAgIHRoaXMuZW5kRGF0ZU9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGZvcm1hdDogJ3l5eXkvTU0vZGQnLFxyXG4gICAgICAgICAgICBtaW46IHRoaXMubGljZW5zZU1vZGVsLmluaXREYXRlLFxyXG4gICAgICAgICAgICBjaGFuZ2U6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUVuZERhdGUoKTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG5cclxuICAgICAgICB0aGlzLmdldFByaW5jaXBhbERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0Q2xpZW50RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0U3RhdHVzRGF0YVNvdXJjZSgpO1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVNZXRob2RPcHRpb25zKCk7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQWN0aXZpdHlMaXN0KCk7XHJcblxyXG5cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENvbnRyb2xzIHdoYXQgYnV0dG9ucyB0byBzaG93XHJcbiAgICAgKi9cclxuICAgIHByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpIHtcclxuICAgICAgICB0aGlzLnBlbmRpbmdMaWNlbnNlID0gdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDIgJiYgIXRoaXMuZWRpdE1vZGU7XHJcbiAgICAgICAgdGhpcy5leHBpcmVkT3JUZXJtaW5hdGVkID0gKHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSAzIHx8IHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSA0KTtcclxuICAgICAgICB0aGlzLmFjdGl2ZVNob3dNb2RlID0gdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDEgJiYgIXRoaXMuZXhwaXJlZE9yVGVybWluYXRlZCAmJiAhdGhpcy5lZGl0TW9kZTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlTWV0aG9kT3B0aW9ucygpIHtcclxuICAgICAgICB0aGlzLm1ldGhvZE9wdGlvbnMgPSBbXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAxLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ1NlcnZlcnMnLFxyXG4gICAgICAgICAgICAgICAgcXVhbnRpdHk6IDgwMDBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnVG9rZW5zJyxcclxuICAgICAgICAgICAgICAgIHF1YW50aXR5OiA0MDAwMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMyxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdDdXN0b20nXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdXHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUFjdGl2aXR5TGlzdCgpIHtcclxuICAgICAgICB0aGlzLmFjdGl2aXR5R3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZGF0ZScsIHRpdGxlOiAnRGF0ZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnd2hvbScsIHRpdGxlOiAnV2hvbSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB0aGlzLmxpY2Vuc2VNb2RlbC5hY3Rpdml0eUxpc3QsXHJcbiAgICAgICAgICAgIHNjcm9sbGFibGU6IHRydWVcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhlIHVzZXIgYXBwbHkgYW5kIHNlcnZlciBzaG91bGQgdmFsaWRhdGUgdGhlIGtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZUtleSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQXBwbHlMaWNlbnNlS2V5IGFzIGFwcGx5TGljZW5zZUtleScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQgPSB0cnVlO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcbiAgICAgKi9cclxuICAgIGFjdGl2YXRlTGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5hY3RpdmF0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byByZXZva2UgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnJldm9rZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmFsaWRhdGUgdGhlIGlucHV0IG9uIFNlcnZlciBvciBUb2tlbnMgaXMgb25seSBpbnRlZ2VyIG9ubHlcclxuICAgICAqIFRoaXMgd2lsbCBiZSBjb252ZXJ0ZWQgaW4gYSBtb3JlIGNvbXBsZXggZGlyZWN0aXZlIGxhdGVyXHJcbiAgICAgKiBUT0RPOiBDb252ZXJ0IGludG8gYSBkaXJlY3RpdmVcclxuICAgICAqL1xyXG4gICAgdmFsaWRhdGVJbnRlZ2VyT25seShlKXtcclxuICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICB2YXIgbmV3VmFsPSBwYXJzZUludCh0aGlzLmxpY2Vuc2VNb2RlbC5tZXRob2QucXVhbnRpdHkpO1xyXG4gICAgICAgICAgICBpZighaXNOYU4obmV3VmFsKSkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwubWV0aG9kLnF1YW50aXR5ID0gbmV3VmFsO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwubWV0aG9kLnF1YW50aXR5ID0gMDtcclxuICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgaWYoZSAmJiBlLmN1cnJlbnRUYXJnZXQgJiYgZS5jdXJyZW50VGFyZ2V0LnZhbHVlKSB7XHJcbiAgICAgICAgICAgICAgICBlLmN1cnJlbnRUYXJnZXQudmFsdWUgPSB0aGlzLmxpY2Vuc2VNb2RlbC5tZXRob2QucXVhbnRpdHk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9IGNhdGNoKGUpIHtcclxuICAgICAgICAgICAgdGhpcy4kbG9nLndhcm4oJ0ludmFsaWQgTnVtYmVyIEV4cGNlcHRpb24nLCB0aGlzLmxpY2Vuc2VNb2RlbC5tZXRob2QucXVhbnRpdHkpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgY3VycmVudCBjaGFuZ2VzXHJcbiAgICAgKi9cclxuICAgIHNhdmVMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnNhdmVMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ0xpY2Vuc2UgU2F2ZWQnKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENoYW5nZSB0aGUgc3RhdHVzIHRvIEVkaXRcclxuICAgICAqL1xyXG4gICAgbW9kaWZ5TGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gdHJ1ZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByaW5jaXBhbERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5wcmluY2lwYWxEYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7aWQ6IDEsIG5hbWU6ICdFTUMnfSxcclxuICAgICAgICAgICAge2lkOiAyLCBuYW1lOiAnSUJNJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge2lkOiAxLCBuYW1lOiAnUHJvZHVjdGlvbid9LFxyXG4gICAgICAgICAgICB7aWQ6IDIsIG5hbWU6ICdPdGhlcid9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRQcm9qZWN0c0RhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgcmV0dXJuICBbXHJcbiAgICAgICAgICAgIHtpZDogMSwgbmFtZTogJ24vYSd9LFxyXG4gICAgICAgICAgICB7aWQ6IDIsIG5hbWU6ICdCYW5rIEVhc3QnfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0Q2xpZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmNsaWVudHNEYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7aWQ6IDEsIG5hbWU6ICduL2EnfSxcclxuICAgICAgICAgICAge2lkOiAyLCBuYW1lOiAnR29sZCBCYW5rJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFN0YXR1c0RhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5zdGF0dXNEYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7aWQ6IDEsIG5hbWU6ICdBY3RpdmUnfSxcclxuICAgICAgICAgICAge2lkOiAyLCBuYW1lOiAnUGVuZGluZyd9LFxyXG4gICAgICAgICAgICB7aWQ6IDMsIG5hbWU6ICdFeHBpcmVkJ30sXHJcbiAgICAgICAgICAgIHtpZDogNCwgbmFtZTogJ1Rlcm1pbmF0ZWQnfVxyXG4gICAgICAgIF07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBIG5ldyBQcm9qZWN0IGhhcyBiZWVuIHNlbGVjdGVkLCB0aGF0IG1lYW5zIHdlIG5lZWQgdG8gcmVsb2FkIHRoZSBuZXh0IHByb2plY3Qgc2VjdGlvblxyXG4gICAgICogQHBhcmFtIGl0ZW1cclxuICAgICAqL1xyXG4gICAgb25DaGFuZ2VQcm9qZWN0KGl0ZW0pIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPbiBjaGFuZ2UgUHJvamVjdCcsIGl0ZW0pO1xyXG4gICAgfVxyXG5cclxuICAgIG9uQ2hhbmdlSW5pdERhdGUoKSB7XHJcbiAgICAgICAgdmFyIHN0YXJ0RGF0ZSA9IHRoaXMuaW5pdERhdGUudmFsdWUoKSxcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IHRoaXMuZW5kRGF0ZS52YWx1ZSgpO1xyXG5cclxuICAgICAgICBpZiAoc3RhcnREYXRlKSB7XHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZSA9IG5ldyBEYXRlKHN0YXJ0RGF0ZSk7XHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZS5zZXREYXRlKHN0YXJ0RGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKHN0YXJ0RGF0ZSk7XHJcbiAgICAgICAgfSBlbHNlIGlmIChlbmREYXRlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuaW5pdERhdGUubWF4KG5ldyBEYXRlKGVuZERhdGUpKTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoKTtcclxuICAgICAgICAgICAgdGhpcy5pbml0RGF0ZS5pbml0RGF0ZS5tYXgoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4oZW5kRGF0ZSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIG9uQ2hhbmdlRW5kRGF0ZSgpe1xyXG4gICAgICAgIHZhciBlbmREYXRlID0gdGhpcy5lbmREYXRlLnZhbHVlKCksXHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZSA9IHRoaXMuaW5pdERhdGUudmFsdWUoKTtcclxuXHJcbiAgICAgICAgaWYgKGVuZERhdGUpIHtcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKGVuZERhdGUpO1xyXG4gICAgICAgICAgICBlbmREYXRlLnNldERhdGUoZW5kRGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgICAgICB0aGlzLmluaXREYXRlLm1heChlbmREYXRlKTtcclxuICAgICAgICB9IGVsc2UgaWYgKHN0YXJ0RGF0ZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKG5ldyBEYXRlKHN0YXJ0RGF0ZSkpO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZSgpO1xyXG4gICAgICAgICAgICB0aGlzLmluaXREYXRlLm1heChlbmREYXRlKTtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihlbmREYXRlKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgaWYodGhpcy5lZGl0TW9kZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcbiAgICAgICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlckxpc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSwgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICAvL3RoaXMuZ2V0TGljZW5zZUxpc3QoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZU1hbmFnZXJMaXN0IEluc3RhbmNlZCcpO1xyXG4gICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IDA7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5vblJlcXVlc3RJbXBvcnRMaWNlbnNlKClcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gSW1wb3J0IExpY2Vuc2UgUmVxdWVzdDwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0LnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2xpY2Vuc2VJZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0Lm9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKHRoaXMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcmluY2lwYWwubmFtZScsIHRpdGxlOiAnUHJpbmNpcGFsJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjbGllbnQubmFtZScsIHRpdGxlOiAnQ2xpZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLCB0aXRsZTogJ1Byb2plY3QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NvbnRhY3RfZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cy50eXBlJywgdGl0bGU6ICdTdGF0dXMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUnLCB0aXRsZTogJ1R5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5uYW1lJywgdGl0bGU6ICdNZXRob2QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5pZCcsIGhpZGRlbjogdHJ1ZX0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzZXJ2ZXJzX3Rva2VucycsIHRpdGxlOiAnU2VydmVyL1Rva2Vucyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaW5jZXB0aW9uJywgdGl0bGU6ICdJbmNlcHRpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2V4cGlyYXRpb24nLCB0aXRsZTogJ0V4cGlyYXRpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Vudmlyb25tZW50JywgdGl0bGU6ICdFbnYuJ31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgLyp0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4geyovXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YSA9IFtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGxpY2Vuc2VJZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAga2V5SWQ6ICdjZTQyY2ZkMS0xYWM1LTRmY2MtYmU1Yy1jYzc4ODVjOGY4M2InLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhY3Rpb246ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBwcmluY2lwYWw6ICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdFTUMnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNsaWVudDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnbi9hJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcHJvamVjdDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnbi9hJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjb250YWN0X2VtYWlsOiAnd2VzdC5jb2FzdEB4eXl5LmNvbScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN0YXR1czoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0eXBlOiAnQWN0aXZlJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0eXBlOiAnTXVsdGktUHJvamVjdCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG1ldGhvZDogIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ1NlcnZlcicsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBxdWFudGl0eTogNDAwMFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpbml0RGF0ZTogJzIwMTYtMDktMTUnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbmREYXRlOiAnMjAxNi0wOS0xOCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcXVlc3RlZDogJzIwMTYtMTItMDgnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbnZpcm9ubWVudDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnUHJvZHVjdGlvbidcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogJ0hlbHAsIEhlbHAsIEhlbHAnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBiYW5uZXJNZXNzYWdlOiAnVGhpcyBhcHBsaWNhdGlvbiBzaG91bGQgb25seSBiZSB1c2VkIGZvciB0cmFpbmluZyBwdXJwb3NlJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVxdWVzdGVkSWQ6ICc1NjQ2NTQ2NTQ2NTQ2NTQ1LWFzZGFzZGFzZDU0YXNkLWFzZGFzNmFzZGFzZCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFwcGxpZWQ6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXBsYWNlZDogWyB7aWQ6IDEsIHN0YXR1czogJzIwMTYtMDUtMDgnICsgJyBodHRwOmJsYWJsYWJhLmNvbSAnICsnIGFhc2RhczU0LTVhc2Q0YTVzZC1hc2Q0NWE0c2QgJ30sIHtpZDogMiwgc3RhdHVzOiAnMjAxNi0wNS0xMCcgKyAnIGh0dHA6YmxhYmxhYmEuY29tICcgKycgYWFzZGFzNTQtNWFzZDRhNXNkLWFzZDQ1YTRzZCAnIH1dLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXBsYWNlZElkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBsaWNlbnNlS2V5OiAnYXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MThhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOGFzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYWN0aXZpdHlMaXN0OiBbXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTA4Jywgd2hvbTogJ0hhcnJ5IExlZ3MnLCBhY3Rpb246ICdSZXF1ZXN0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTAnLCB3aG9tOiAnUm9iaW4gQmFua3MnLCBhY3Rpb246ICdJbXBvcnRlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xMScsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnQWN0aXZhdGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTE0Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ1VubG9ja2VkIExpY2Vuc2UgdG8gbW9kaWZ5J30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTIwJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdNb2RpZmllZCBMaWNlbnNlIGRldGFpbCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yMScsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnQWN0aXZhdGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfVxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBob3N0TmFtZTogJ3RtLWFjbWUtYmlnbW92ZS0uc29tZWRvbWFpbmcubG9jYWwnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB3ZWJzaXRlTmFtZTogJ3RyYW5tYW4uc29tZWNvcnAuY29tJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaGFzaDogJ2thc2tsZHNhbGRramFzZGE1czRhNjVzZGE2NXNkNGE2NXNkNDZhNXNkNGFzNjVkJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBsaWNlbnNlSWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGtleUlkOiAnY2U0MmNmZDEtMWFjNS00ZmNjLWJlNWMtY2M3ODg1YzhmODNiJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYWN0aW9uOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcHJpbmNpcGFsOiAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnSUJNJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjbGllbnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ0dvbGQgQmFuaycsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByb2plY3Q6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ0JhbmsgRWFzdCdcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgY29udGFjdF9lbWFpbDogJ3dlc3QuY29hc3RAeHl5eS5jb20nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzdGF0dXM6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgdHlwZTogJ1BlbmRpbmcnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHR5cGU6ICdQcm9qZWN0JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbWV0aG9kOiAge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnVG9rZW4nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcXVhbnRpdHk6IDUwMDBcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaW5pdERhdGU6ICcyMDE2LTA5LTE1JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZW5kRGF0ZTogJzIwMTYtMDktMTgnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXF1ZXN0ZWQ6ICcyMDE2LTEyLTA4JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZW52aXJvbm1lbnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ1Byb2R1Y3Rpb24nXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHNwZWNpYWxJbnN0cnVjdGlvbnM6ICdIZWxwLCBIZWxwLCBIZWxwJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVxdWVzdGVkSWQ6ICc1NjQ2NTQ2NTQ2NTQ2NTQ1LWFzZGFzZGFzZDU0YXNkLWFzZGFzNmFzZGFzZCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFwcGxpZWQ6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBiYW5uZXJNZXNzYWdlOiAnVGhpcyBhcHBsaWNhdGlvbiBzaG91bGQgb25seSBiZSB1c2VkIGZvciB0cmFpbmluZyBwdXJwb3NlJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVwbGFjZWQ6IFsge2lkOiAxLCBzdGF0dXM6ICcyMDE2LTA1LTA4JyArICcgaHR0cDpibGFibGFiYS5jb20gJyArJyBhYXNkYXM1NC01YXNkNGE1c2QtYXNkNDVhNHNkICd9LCB7aWQ6IDIsIHN0YXR1czogJzIwMTYtMDUtMTAnICsgJyBodHRwOmJsYWJsYWJhLmNvbSAnICsnIGFhc2RhczU0LTVhc2Q0YTVzZC1hc2Q0NWE0c2QgJyB9XSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVwbGFjZWRJZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbGljZW5zZUtleTogJ2FzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4YXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MThhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFjdGl2aXR5TGlzdDogW1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0wOCcsIHdob206ICdIYXJyeSBMZWdzJywgYWN0aW9uOiAnUmVxdWVzdGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTEwJywgd2hvbTogJ1JvYmluIEJhbmtzJywgYWN0aW9uOiAnSW1wb3J0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTEnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0FjdGl2YXRlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xNCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTE4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdVbmxvY2tlZCBMaWNlbnNlIHRvIG1vZGlmeSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yMCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnTW9kaWZpZWQgTGljZW5zZSBkZXRhaWwnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjEnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0FjdGl2YXRlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ31cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaG9zdE5hbWU6ICd0bS1hY21lLWJpZ21vdmUtLnNvbWVkb21haW5nLmxvY2FsJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgd2Vic2l0ZU5hbWU6ICd0cmFubWFuLnNvbWVjb3JwLmNvbScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGhhc2g6ICdrYXNrbGRzYWxka2phc2RhNXM0YTY1c2RhNjVzZDRhNjVzZDQ2YTVzZDRhczY1ZCdcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbGljZW5zZUlkOiAzLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBrZXlJZDogJ2NlNDJjZmQxLTFhYzUtNGZjYy1iZTVjLWNjNzg4NWM4ZjgzYicsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGFjdGlvbjogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHByaW5jaXBhbDogIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ0lCTSdcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgY2xpZW50OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdHb2xkIEJhbmsnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdCYW5rIEVhc3QnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNvbnRhY3RfZW1haWw6ICd3ZXN0LmNvYXN0QHh5eXkuY29tJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHR5cGU6ICdFeHBpcmVkJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0eXBlOiAnUHJvamVjdCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG1ldGhvZDogIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgbmFtZTogJ1Rva2VuJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHF1YW50aXR5OiA1MDAwXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGluaXREYXRlOiAnMjAxNi0wOS0xNScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGVuZERhdGU6ICcyMDE2LTA5LTE4JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcmVxdWVzdGVkOiAnMjAxNi0xMi0wOCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGVudmlyb25tZW50OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdQcm9kdWN0aW9uJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiAnSGVscCwgSGVscCwgSGVscCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcXVlc3RlZElkOiAnNTY0NjU0NjU0NjU0NjU0NS1hc2Rhc2Rhc2Q1NGFzZC1hc2RhczZhc2Rhc2QnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhcHBsaWVkOiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogJ1RoaXMgYXBwbGljYXRpb24gc2hvdWxkIG9ubHkgYmUgdXNlZCBmb3IgdHJhaW5pbmcgcHVycG9zZScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcGxhY2VkOiBbIHtpZDogMSwgc3RhdHVzOiAnMjAxNi0wNS0wOCcgKyAnIGh0dHA6YmxhYmxhYmEuY29tICcgKycgYWFzZGFzNTQtNWFzZDRhNXNkLWFzZDQ1YTRzZCAnfSwge2lkOiAyLCBzdGF0dXM6ICcyMDE2LTA1LTEwJyArICcgaHR0cDpibGFibGFiYS5jb20gJyArJyBhYXNkYXM1NC01YXNkNGE1c2QtYXNkNDVhNHNkICcgfV0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcGxhY2VkSWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGxpY2Vuc2VLZXk6ICdhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOGFzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4YXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MTgnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhY3Rpdml0eUxpc3Q6IFtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMDgnLCB3aG9tOiAnSGFycnkgTGVncycsIGFjdGlvbjogJ1JlcXVlc3RlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xMCcsIHdob206ICdSb2JpbiBCYW5rcycsIGFjdGlvbjogJ0ltcG9ydGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTExJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdBY3RpdmF0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTQnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnVW5sb2NrZWQgTGljZW5zZSB0byBtb2RpZnknfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjAnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ01vZGlmaWVkIExpY2Vuc2UgZGV0YWlsJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTIxJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdBY3RpdmF0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGhvc3ROYW1lOiAndG0tYWNtZS1iaWdtb3ZlLS5zb21lZG9tYWluZy5sb2NhbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHdlYnNpdGVOYW1lOiAndHJhbm1hbi5zb21lY29ycC5jb20nLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBoYXNoOiAna2Fza2xkc2FsZGtqYXNkYTVzNGE2NXNkYTY1c2Q0YTY1c2Q0NmE1c2Q0YXM2NWQnXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGxpY2Vuc2VJZDogNCxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAga2V5SWQ6ICdjZTQyY2ZkMS0xYWM1LTRmY2MtYmU1Yy1jYzc4ODVjOGY4M2InLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBhY3Rpb246ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBwcmluY2lwYWw6ICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdJQk0nXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGNsaWVudDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnR29sZCBCYW5rJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgcHJvamVjdDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDIsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnQmFuayBFYXN0J1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBjb250YWN0X2VtYWlsOiAnd2VzdC5jb2FzdEB4eXl5LmNvbScsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHN0YXR1czoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDQsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB0eXBlOiAnVGVybWluYXRlZCdcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgdHlwZTogJ1Byb2plY3QnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBtZXRob2Q6ICB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbicsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBxdWFudGl0eTogNTAwMFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBpbml0RGF0ZTogJzIwMTYtMDktMTUnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbmREYXRlOiAnMjAxNi0wOS0xOCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJlcXVlc3RlZDogJzIwMTYtMTItMDgnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBlbnZpcm9ubWVudDoge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IDEsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiAnUHJvZHVjdGlvbidcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogJ0hlbHAsIEhlbHAsIEhlbHAnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXF1ZXN0ZWRJZDogJzU2NDY1NDY1NDY1NDY1NDUtYXNkYXNkYXNkNTRhc2QtYXNkYXM2YXNkYXNkJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYXBwbGllZDogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGJhbm5lck1lc3NhZ2U6ICdUaGlzIGFwcGxpY2F0aW9uIHNob3VsZCBvbmx5IGJlIHVzZWQgZm9yIHRyYWluaW5nIHB1cnBvc2UnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXBsYWNlZDogWyB7aWQ6IDEsIHN0YXR1czogJzIwMTYtMDUtMDgnICsgJyBodHRwOmJsYWJsYWJhLmNvbSAnICsnIGFhc2RhczU0LTVhc2Q0YTVzZC1hc2Q0NWE0c2QgJ30sIHtpZDogMiwgc3RhdHVzOiAnMjAxNi0wNS0xMCcgKyAnIGh0dHA6YmxhYmxhYmEuY29tICcgKycgYWFzZGFzNTQtNWFzZDRhNXNkLWFzZDQ1YTRzZCAnIH1dLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXBsYWNlZElkOiAxLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBsaWNlbnNlS2V5OiAnYXNkYXNkYXNkNGFzNTZkYTZzZDQ2MzI1ZTRxNjVhc2Q0YTY1c2Q0YTY1c2Q0YXM2NWQ0ODY0Mjg2ZTQxMjg2ZTQxNjgyZTQ1M2E0c2Q1YXM0ZDZhOHM0ZDYxMjg0ZDEyNjg0ZDYxODI0ZDYxODRkNjE4MjRkMTI2ZDQyNjE4NGQ2MTgyZDQ2MTgyZDI2MThhc2Rhc2Rhc2Q0YXM1NmRhNnNkNDYzMjVlNHE2NWFzZDRhNjVzZDRhNjVzZDRhczY1ZDQ4NjQyODZlNDEyODZlNDE2ODJlNDUzYTRzZDVhczRkNmE4czRkNjEyODRkMTI2ODRkNjE4MjRkNjE4NGQ2MTgyNGQxMjZkNDI2MTg0ZDYxODJkNDYxODJkMjYxOGFzZGFzZGFzZDRhczU2ZGE2c2Q0NjMyNWU0cTY1YXNkNGE2NXNkNGE2NXNkNGFzNjVkNDg2NDI4NmU0MTI4NmU0MTY4MmU0NTNhNHNkNWFzNGQ2YThzNGQ2MTI4NGQxMjY4NGQ2MTgyNGQ2MTg0ZDYxODI0ZDEyNmQ0MjYxODRkNjE4MmQ0NjE4MmQyNjE4JyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgYWN0aXZpdHlMaXN0OiBbXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTA4Jywgd2hvbTogJ0hhcnJ5IExlZ3MnLCBhY3Rpb246ICdSZXF1ZXN0ZWQnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTAnLCB3aG9tOiAnUm9iaW4gQmFua3MnLCBhY3Rpb246ICdJbXBvcnRlZCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0xMScsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnQWN0aXZhdGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTE0Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMTgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ1VubG9ja2VkIExpY2Vuc2UgdG8gbW9kaWZ5J30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTIwJywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdNb2RpZmllZCBMaWNlbnNlIGRldGFpbCd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yMScsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnQWN0aXZhdGVkJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfSxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsgZGF0ZTogJzIwMTYtMDUtMjgnLCB3aG9tOiAnQmVuIEQgT3ZhcicsIGFjdGlvbjogJ0VtYWlsZWQgTGljZW5zZSd9LFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgeyBkYXRlOiAnMjAxNi0wNS0yOCcsIHdob206ICdCZW4gRCBPdmFyJywgYWN0aW9uOiAnRW1haWxlZCBMaWNlbnNlJ30sXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB7IGRhdGU6ICcyMDE2LTA1LTI4Jywgd2hvbTogJ0JlbiBEIE92YXInLCBhY3Rpb246ICdFbWFpbGVkIExpY2Vuc2UnfVxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBob3N0TmFtZTogJ3RtLWFjbWUtYmlnbW92ZS0uc29tZWRvbWFpbmcubG9jYWwnLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB3ZWJzaXRlTmFtZTogJ3RyYW5tYW4uc29tZWNvcnAuY29tJyxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaGFzaDogJ2thc2tsZHNhbGRramFzZGE1czRhNjVzZGE2NXNkNGE2NXNkNDZhNXNkNGFzNjVkJ1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIF07XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgLyogfSk7Ki9cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkICE9PSAwICYmIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB2YXIgbmV3TGljZW5zZUNyZWF0ZWQgPSB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEuZmluZCgobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGxpY2Vuc2UubGljZW5zZUlkID09PSB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkID0gMDtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGlmKG5ld0xpY2Vuc2VDcmVhdGVkKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKG5ld0xpY2Vuc2VDcmVhdGVkKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGUgdXNlciBJbXBvcnQgYSBuZXcgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBvblJlcXVlc3RJbXBvcnRMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL3JlcXVlc3RJbXBvcnQvUmVxdWVzdEltcG9ydC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1JlcXVlc3RJbXBvcnQgYXMgcmVxdWVzdEltcG9ydCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSAxOyAvLyB0YWtlIHRoaXMgcGFyYW0gZnJvbSB0aGUgbGFzdCBpbXBvcnRlZCBsaWNlbnNlLCBvZiBjb3Vyc2VcclxuICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEFmdGVyIGNsaWNraW5nIG9uIGVkaXQsIHdlIHJlZGlyZWN0IHRoZSB1c2VyIHRvIHRoZSBFZGl0aW9uIHNjcmVlbiBpbnN0ZWFkIG9mIG9wZW4gYSBkaWFsb2dcclxuICAgICAqIGR1IHRoZSBzaXplIG9mIHRoZSBpbnB1dHNcclxuICAgICAqL1xyXG4gICAgb25MaWNlbnNlTWFuYWdlckRldGFpbHMobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09wZW4gRGV0YWlscyBmb3I6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL2RldGFpbC9MaWNlbnNlTWFuYWdlckRldGFpbC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VNYW5hZ2VyRGV0YWlsIGFzIGxpY2Vuc2VNYW5hZ2VyRGV0YWlsJyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgdmFyIGRhdGFJdGVtID0ge307XHJcbiAgICAgICAgICAgICAgICAgICAgaWYobGljZW5zZSAmJiBsaWNlbnNlLmRhdGFJdGVtKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGRhdGFJdGVtID0gbGljZW5zZS5kYXRhSXRlbTtcclxuICAgICAgICAgICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2U7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IGRhdGFJdGVtIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcblxyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgcmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RJbXBvcnQge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgbGljZW5zZTogJydcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIG9uSW1wb3J0TGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5pbXBvcnRMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIsICRyb290U2NvcGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLnJvb3RTY29wZSA9ICRyb290U2NvcGU7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ2xpY2Vuc2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgdGhlIExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5zYXZlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbiAgICAvKipcclxuICAgICAqIERvZXMgdGhlIGFjdGl2YXRpb24gb2YgdGhlIGN1cnJlbnQgbGljZW5zZSBpZiB0aGlzIGlzIG5vdCBhY3RpdmVcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgYWN0aXZhdGVMaWNlbnNlKGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuYWN0aXZhdGVMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdUaGUgbGljZW5zZSB3YXMgYWN0aXZhdGVkIGFuZCB0aGUgbGljZW5zZSB3YXMgZW1haWxlZC4nfSk7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE1ha2UgdGhlIHJlcXVlc3QgdG8gSW1wb3J0IHRoZSBsaWNlbnNlLCBpZiBmYWlscywgdGhyb3dzIGFuIGV4Y2VwdGlvbiB2aXNpYmxlIGZvciB0aGUgdXNlciB0byB0YWtlIGFjdGlvblxyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBpbXBvcnRMaWNlbnNlKGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkucmVxdWVzdEltcG9ydChsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAvL2lmKGRhdGEuYXBwbGllZCkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBJbXBvcnRlZCd9KTtcclxuICAgICAgICAgICAgLyp9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICB9Ki9cclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJldm9rZUxpY2Vuc2UobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5yZXZva2VMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENyZWF0ZSBhIE5ldyBMaWNlbnNlIHBhc3NpbmcgcGFyYW1zXHJcbiAgICAgKiBAcGFyYW0gbmV3TGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0KG5ld0xpY2Vuc2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IE5vdGljZUxpc3QgZnJvbSAnLi9saXN0L05vdGljZUxpc3QuanMnO1xyXG5pbXBvcnQgTm90aWNlTWFuYWdlclNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL05vdGljZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IEVkaXROb3RpY2UgZnJvbSAnLi9lZGl0L0VkaXROb3RpY2UuanMnO1xyXG5cclxuXHJcbnZhciBOb3RpY2VNYW5hZ2VyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLk5vdGljZU1hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsICAnJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcicsXHJcbiAgICBmdW5jdGlvbiAoJHN0YXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ25vdGljZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ25vdGljZUxpc3QnLCB7XHJcbiAgICAgICAgICAgIGRhdGE6IHtwYWdlOiB7dGl0bGU6ICdOb3RpY2UgQWRtaW5pc3RyYXRpb24nLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnQURNSU4nLCAnTk9USUNFJywgJ0xJU1QnXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvbm90aWNlL2xpc3QnLFxyXG4gICAgICAgICAgICB2aWV3czoge1xyXG4gICAgICAgICAgICAgICAgJ2hlYWRlclZpZXdAJzogaGVhZGVyLFxyXG4gICAgICAgICAgICAgICAgJ2JvZHlWaWV3QCc6IHtcclxuICAgICAgICAgICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvbGlzdC9Ob3RpY2VMaXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdOb3RpY2VMaXN0IGFzIG5vdGljZUxpc3QnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdOb3RpY2VNYW5hZ2VyU2VydmljZScsIFsnJGxvZycsICdSZXN0U2VydmljZUhhbmRsZXInLCBOb3RpY2VNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTm90aWNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdOb3RpY2VMaXN0JywgWyckbG9nJywgJyRzdGF0ZScsICdOb3RpY2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBOb3RpY2VMaXN0XSk7XHJcblxyXG4vLyBNb2RhbCAtIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignRWRpdE5vdGljZScsIFsnJGxvZycsICdOb3RpY2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgRWRpdE5vdGljZV0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgTm90aWNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRWRpdE5vdGljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgbm90aWNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgdGhpcy5hY3Rpb24gPSBwYXJhbXMuYWN0aW9uO1xyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHBhcmFtcy5hY3Rpb25UeXBlO1xyXG5cclxuICAgICAgICB0aGlzLmtlbmRvRWRpdG9yVG9vbHMgPSBbXHJcbiAgICAgICAgICAgICdmb3JtYXR0aW5nJywgJ2NsZWFuRm9ybWF0dGluZycsXHJcbiAgICAgICAgICAgICdmb250TmFtZScsICdmb250U2l6ZScsXHJcbiAgICAgICAgICAgICdqdXN0aWZ5TGVmdCcsICdqdXN0aWZ5Q2VudGVyJywgJ2p1c3RpZnlSaWdodCcsICdqdXN0aWZ5RnVsbCcsXHJcbiAgICAgICAgICAgICdib2xkJyxcclxuICAgICAgICAgICAgJ2l0YWxpYycsXHJcbiAgICAgICAgICAgICd2aWV3SHRtbCdcclxuICAgICAgICBdO1xyXG5cclxuICAgICAgICAvLyBDU1MgaGFzIG5vdCBjYW5jZWxpbmcgYXR0cmlidXRlcywgc28gaW5zdGVhZCBvZiByZW1vdmluZyBldmVyeSBwb3NzaWJsZSBIVE1MLCB3ZSBtYWtlIGVkaXRvciBoYXMgc2FtZSBjc3NcclxuICAgICAgICB0aGlzLmtlbmRvU3R5bGVzaGVldHMgPSBbXHJcbiAgICAgICAgICAgICcuLi9zdGF0aWMvZGlzdC9qcy92ZW5kb3JzL2Jvb3RzdHJhcC9kaXN0L2Nzcy9ib290c3RyYXAubWluLmNzcycsIC8vIE91cnQgY3VycmVudCBCb290c3RyYXAgY3NzXHJcbiAgICAgICAgICAgICcuLi9zdGF0aWMvZGlzdC9jc3MvVERTVE1MYXlvdXQubWluLmNzcycgLy8gT3JpZ2luYWwgVGVtcGxhdGUgQ1NTXHJcblxyXG4gICAgICAgIF07XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0VHlwZURhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbCA9IHtcclxuICAgICAgICAgICAgdGl0bGU6ICcnLFxyXG4gICAgICAgICAgICB0eXBlSWQ6IDAsXHJcbiAgICAgICAgICAgIGFjdGl2ZTogZmFsc2UsXHJcbiAgICAgICAgICAgIGh0bWxUZXh0OiAnJyxcclxuICAgICAgICAgICAgcmF3VGV4dDogJydcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIC8vIE9uIEVkaXRpb24gTW9kZSB3ZSBjYyB0aGUgbW9kZWwgYW5kIG9ubHkgdGhlIHBhcmFtcyB3ZSBuZWVkXHJcbiAgICAgICAgaWYocGFyYW1zLm5vdGljZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5pZCA9IHBhcmFtcy5ub3RpY2UuaWQ7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLnRpdGxlID0gcGFyYW1zLm5vdGljZS50aXRsZTtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwudHlwZUlkID0gcGFyYW1zLm5vdGljZS50eXBlLmlkO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC5hY3RpdmUgPSBwYXJhbXMubm90aWNlLmFjdGl2ZTtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuaHRtbFRleHQgPSBwYXJhbXMubm90aWNlLmh0bWxUZXh0O1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHRoZSBFbnZpcm9ubWVudCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0VHlwZURhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy50eXBlRGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge3R5cGVJZDogMSwgbmFtZTogJ1ByZWxvZ2luJ30sXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDIsIG5hbWU6ICdQb3N0bG9naW4nfVxyXG4gICAgICAgICAgICAvL3t0eXBlSWQ6IDMsIG5hbWU6ICdHZW5lcmFsJ30gRGlzYWJsZWQgdW50aWwgUGhhc2UgSUlcclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSB0aGUgU2VydmljZSBjYWxsIHRvIENyZWF0ZS9FZGl0IGEgbm90aWNlXHJcbiAgICAgKi9cclxuICAgIHNhdmVOb3RpY2UoKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbyh0aGlzLmFjdGlvbiArICcgTm90aWNlIFJlcXVlc3RlZDogJywgdGhpcy5lZGl0TW9kZWwpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsLnJhd1RleHQgPSAkKCcja2VuZG8tZWRpdG9yLWNyZWF0ZS1lZGl0JykudGV4dCgpO1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGVsLnR5cGVJZCA9IHBhcnNlSW50KHRoaXMuZWRpdE1vZGVsLnR5cGVJZCk7XHJcbiAgICAgICAgaWYodGhpcy5hY3Rpb24gPT09IHRoaXMuYWN0aW9uVHlwZS5ORVcpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5jcmVhdGVOb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSBpZih0aGlzLmFjdGlvbiA9PT0gdGhpcy5hY3Rpb25UeXBlLkVESVQpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5lZGl0Tm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgZGVsZXRlTm90aWNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIGRlbGV0ZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5kZWxldGVOb3RpY2UodGhpcy5lZGl0TW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBOb3RpY2VMaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIG5vdGljZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuXHJcbiAgICAgICAgdGhpcy5hY3Rpb25UeXBlID0ge1xyXG4gICAgICAgICAgICBORVc6ICdOZXcnLFxyXG4gICAgICAgICAgICBFRElUOiAnRWRpdCdcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMubm90aWNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlID0gbm90aWNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VMaXN0IEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibm90aWNlTGlzdC5vbkVkaXRDcmVhdGVOb3RpY2Uobm90aWNlTGlzdC5hY3Rpb25UeXBlLk5FVylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gQ3JlYXRlIE5ldyBOb3RpY2U8L2J1dHRvbj4gPGRpdiBuZy1jbGljaz1cIm5vdGljZUxpc3QucmVsb2FkTm90aWNlTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2lkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdodG1sVGV4dCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibm90aWNlTGlzdC5vbkVkaXRDcmVhdGVOb3RpY2Uobm90aWNlTGlzdC5hY3Rpb25UeXBlLkVESVQsIHRoaXMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0aXRsZScsIHRpdGxlOiAnVGl0bGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUuaWQnLCBoaWRkZW46IHRydWV9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5uYW1lJywgdGl0bGU6ICdUeXBlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3RpdmUnLCB0aXRsZTogJ0FjdGl2ZScsIHRlbXBsYXRlOiAnI2lmKGFjdGl2ZSkgeyMgWWVzICN9IGVsc2UgeyMgTm8gI30jJyB9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZ2V0Tm90aWNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAndGl0bGUnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc29ydGFibGU6IHRydWVcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogT3BlbiBhIGRpYWxvZyB3aXRoIHRoZSBCYXNpYyBGb3JtIHRvIHJlcXVlc3QgYSBOZXcgTm90aWNlXHJcbiAgICAgKi9cclxuICAgIG9uRWRpdENyZWF0ZU5vdGljZShhY3Rpb24sIG5vdGljZSkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL25vdGljZU1hbmFnZXIvZWRpdC9FZGl0Tm90aWNlLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRWRpdE5vdGljZSBhcyBlZGl0Tm90aWNlJyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdmFyIGRhdGFJdGVtID0gbm90aWNlICYmIG5vdGljZS5kYXRhSXRlbTtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBhY3Rpb246IGFjdGlvbiwgbm90aWNlOiBkYXRhSXRlbSwgYWN0aW9uVHlwZTogdGhpcy5hY3Rpb25UeXBlfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChub3RpY2UpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbyhhY3Rpb24gKyAnIE5vdGljZTogJywgbm90aWNlKTtcclxuICAgICAgICAgICAgLy8gQWZ0ZXIgYSBuZXcgdmFsdWUgaXMgYWRkZWQsIGxldHMgdG8gcmVmcmVzaCB0aGUgR3JpZFxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZE5vdGljZUxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oYWN0aW9uICsgJyBSZXF1ZXN0IENhbmNlbGVkLicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZE5vdGljZUxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5ub3RpY2VHcmlkLmRhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5ub3RpY2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEwLzA3LzE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIE5vdGljZU1hbmFnZXJTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuXHJcbiAgICAgICAgdGhpcy5UWVBFID0ge1xyXG4gICAgICAgICAgICAnMSc6ICdQcmVsb2dpbicsXHJcbiAgICAgICAgICAgICcyJzogJ1Bvc3Rsb2dpbicsXHJcbiAgICAgICAgICAgICczJzogJ0dlbmVyYWwnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ05vdGljZU1hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldE5vdGljZUxpc3QoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldE5vdGljZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdmFyIG5vdGljZUxpc3QgPSBbXTtcclxuICAgICAgICAgICAgdHJ5IHtcclxuICAgICAgICAgICAgICAgIC8vIFZlcmlmeSB0aGUgTGlzdCByZXR1cm5zIHdoYXQgd2UgZXhwZWN0IGFuZCB3ZSBjb252ZXJ0IGl0IHRvIGFuIEFycmF5IHZhbHVlXHJcbiAgICAgICAgICAgICAgICBpZihkYXRhICYmIGRhdGEubm90aWNlcykge1xyXG4gICAgICAgICAgICAgICAgICAgIG5vdGljZUxpc3QgPSBkYXRhLm5vdGljZXM7XHJcbiAgICAgICAgICAgICAgICAgICAgaWYgKG5vdGljZUxpc3QgJiYgbm90aWNlTGlzdC5sZW5ndGggPiAwKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgbm90aWNlTGlzdC5sZW5ndGg7IGkgPSBpICsgMSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgbm90aWNlTGlzdFtpXS50eXBlID0ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIGlkOiBub3RpY2VMaXN0W2ldLnR5cGVJZCxcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBuYW1lOiB0aGlzLlRZUEVbbm90aWNlTGlzdFtpXS50eXBlSWRdXHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9O1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZGVsZXRlIG5vdGljZUxpc3RbaV0udHlwZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9IGNhdGNoKGUpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMubG9nLmVycm9yKCdFcnJvciBwYXJzaW5nIHRoZSBOb3RpY2UgTGlzdCcsIGUpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhub3RpY2VMaXN0KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENyZWF0ZSBhIE5ldyBOb3RpY2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBub3RpY2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOb3RpY2Uobm90aWNlLCBjYWxsYmFjayl7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBOb3RpY2Ugc2hvdWxkIGhhdmUgdGhlIElEIGluIG9yZGVyIHRvIGVkaXQgdGhlIE5vdGljZVxyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGVkaXROb3RpY2Uobm90aWNlLCBjYWxsYmFjayl7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5lZGl0Tm90aWNlKG5vdGljZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTm90aWNlIHNob3VsZCBoYXZlIHRoZSBJRCBpbiBvcmRlciB0byBkZWxldGUgdGhlIG5vdGljZVxyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGRlbGV0ZU5vdGljZShub3RpY2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5ub3RpY2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5kZWxldGVOb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIyLzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvVGFza01hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyQ29udHJvbGxlciBmcm9tICcuL2xpc3QvVGFza01hbmFnZXJDb250cm9sbGVyLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyRWRpdCBmcm9tICcuL2VkaXQvVGFza01hbmFnZXJFZGl0LmpzJztcclxuXHJcbi8vIERpcmVjdGl2ZXNcclxuaW1wb3J0IFNWR0xvYWRlciBmcm9tICcuLi8uLi9kaXJlY3RpdmVzL1N2Zy9zdmdMb2FkZXIuanMnXHJcblxyXG52YXIgVGFza01hbmFnZXJNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uVGFza01hbmFnZXJNb2R1bGUnLCBbdWlSb3V0ZXJdKS5jb25maWcoWyckc3RhdGVQcm92aWRlcicsIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlcikge1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlckNvbnRhaW5lci5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCd0YXNrTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ015IFRhc2sgTWFuYWdlcicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydUYXNrIE1hbmFnZXInXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvdGFzay9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICdhcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9saXN0L1Rhc2tNYW5hZ2VyQ29udGFpbmVyLmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdUYXNrTWFuYWdlckNvbnRyb2xsZXIgYXMgdGFza01hbmFnZXInXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuVGFza01hbmFnZXJNb2R1bGUuc2VydmljZSgndGFza01hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFRhc2tNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJDb250cm9sbGVyJywgWyckbG9nJywgJ3Rhc2tNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBUYXNrTWFuYWdlckNvbnRyb2xsZXJdKTtcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJFZGl0JywgWyckbG9nJywgVGFza01hbmFnZXJFZGl0XSk7XHJcblxyXG4vLyBEaXJlY3RpdmVzXHJcblRhc2tNYW5hZ2VyTW9kdWxlLmRpcmVjdGl2ZSgnc3ZnTG9hZGVyJywgU1ZHTG9hZGVyKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IFRhc2tNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMy8xMS8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyRWRpdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgdGFza01hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuXHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMC8yMDE1LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJDb250cm9sbGVyIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCB0YXNrTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMubW9kdWxlID0gJ1Rhc2tNYW5hZ2VyJztcclxuICAgICAgICB0aGlzLnRhc2tNYW5hZ2VyU2VydmljZSA9IHRhc2tNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnRhc2tHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMuZXZlbnREYXRhU291cmNlID0gW107XHJcblxyXG4gICAgICAgIC8vIEluaXQgQ2xhc3NcclxuICAgICAgICB0aGlzLmdldEV2ZW50RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdUYXNrTWFuYWdlciBDb250cm9sbGVyIEluc3RhbmNlZCcpO1xyXG5cclxuICAgIH1cclxuXHJcbiAgICBvcGVuTW9kYWxEZW1vKCkge1xyXG5cclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICdhcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9lZGl0L1Rhc2tNYW5hZ2VyRWRpdC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyRWRpdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIGl0ZW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIFsnMScsJ2EyJywnZ2cnXTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChzZWxlY3RlZEl0ZW0pID0+IHtcclxuICAgICAgICAgICAgdGhpcy5kZWJ1ZyhzZWxlY3RlZEl0ZW0pO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTW9kYWwgZGlzbWlzc2VkIGF0OiAnICsgbmV3IERhdGUoKSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnRhc2tHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZ3JvdXBhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbe2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Rhc2snLCB0aXRsZTogJ1Rhc2snfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Rlc2NyaXB0aW9uJywgdGl0bGU6ICdEZXNjcmlwdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzZXROYW1lJywgdGl0bGU6ICdBc3NldCBOYW1lJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldFR5cGUnLCB0aXRsZTogJ0Fzc2V0IFR5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3VwZGF0ZWQnLCB0aXRsZTogJ1VwZGF0ZWQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2R1ZScsIHRpdGxlOiAnRHVlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMnLCB0aXRsZTogJ1N0YXR1cyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzaWduZWRUbycsIHRpdGxlOiAnQXNzaWduZWQgVG8nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RlYW0nLCB0aXRsZTogJ1RlYW0nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NhdGVnb3J5JywgdGl0bGU6ICdDYXRlZ29yeSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3VjJywgdGl0bGU6ICdTdWMuJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzY29yZScsIHRpdGxlOiAnU2NvcmUnfV0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlLnRlc3RTZXJ2aWNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RXZlbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuZXZlbnREYXRhU291cmNlID0gW1xyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMSwgZXZlbnROYW1lOiAnQWxsJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiAyLCBldmVudE5hbWU6ICdCdWlsZG91dCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMywgZXZlbnROYW1lOiAnRFItRVAnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDQsIGV2ZW50TmFtZTogJ00xLVBoeXNpY2FsJ31cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG5cclxuICAgIG9uRXJyb3JIYXBwZW5zKCkge1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlLmZhaWxDYWxsKGZ1bmN0aW9uICgpIHtcclxuXHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDIyLzA3LzE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFRhc2tNYW5hZ2VyU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgUmVzdFNlcnZpY2VIYW5kbGVyKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSBSZXN0U2VydmljZUhhbmRsZXI7XHJcblxyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdUYXNrTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZmFpbENhbGwoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlJlc291cmNlU2VydmljZUhhbmRsZXIoKS5nZXRTVkcoKTtcclxuICAgIH1cclxuXHJcbiAgICB0ZXN0U2VydmljZShjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UuVGFza1NlcnZpY2VIYW5kbGVyKCkuZ2V0RmVlZHMoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIzLzIwMTUuXHJcbiAqIEltcGxlbWVudHMgUlggT2JzZXJ2YWJsZSB0byBkaXNwb3NlIGFuZCB0cmFjayBiZXR0ZXIgZWFjaCBjYWxsIHRvIHRoZSBzZXJ2ZXJcclxuICogVGhlIE9ic2VydmVyIHN1YnNjcmliZSBhIHByb21pc2UuXHJcbiAqL1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RIYW5kbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKHJ4KSB7XHJcbiAgICAgICAgdGhpcy5yeCA9IHJ4O1xyXG4gICAgICAgIHRoaXMucHJvbWlzZSA9IFtdO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2FsbGVkIGZyb20gUmVzdFNlcnZpY2VIYW5kbGVyLnN1YnNjcmliZVJlcXVlc3RcclxuICAgICAqIGl0IHZlcmlmeSB0aGF0IHRoZSBjYWxsIGlzIGJlaW5nIGRvbmUgdG8gdGhlIHNlcnZlciBhbmQgcmV0dXJuIGEgcHJvbWlzZVxyXG4gICAgICogQHBhcmFtIHJlcXVlc3RcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICBzdWJzY3JpYmVSZXF1ZXN0KHJlcXVlc3QsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHZhciByeE9ic2VydmFibGUgPSB0aGlzLnJ4Lk9ic2VydmFibGUuZnJvbVByb21pc2UocmVxdWVzdCk7XHJcbiAgICAgICAgLy8gVmVyaWZ5IGlzIG5vdCBhIGR1cGxpY2F0ZSBjYWxsXHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgdGhpcy5jYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBTdWJzY3JpYmUgdGhlIHJlcXVlc3RcclxuICAgICAgICB2YXIgcmVzdWx0U3Vic2NyaWJlID0gdGhpcy5hZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgIGlmIChyZXN1bHRTdWJzY3JpYmUgJiYgcmVzdWx0U3Vic2NyaWJlLmlzU3RvcHBlZCkge1xyXG4gICAgICAgICAgICAvLyBBbiBlcnJvciBoYXBwZW5zLCB0cmFja2VkIGJ5IEh0dHBJbnRlcmNlcHRvckludGVyZmFjZVxyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGFkZFN1YnNjcmliZShyeE9ic2VydmFibGUsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdID0gcnhPYnNlcnZhYmxlLnN1YnNjcmliZShcclxuICAgICAgICAgICAgKHJlc3BvbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5vblN1YnNjcmliZWRTdWNjZXNzKHJlc3BvbnNlLCByeE9ic2VydmFibGUsIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIChlcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE5PLU9QIFN1YnNjcmliZSBjb21wbGV0ZWRcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIHJldHVybiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgIH1cclxuXHJcbiAgICBjYW5jZWxSZXF1ZXN0KHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICAgICAgcnhPYnNlcnZhYmxlLmRpc3Bvc2UoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkge1xyXG4gICAgICAgIHJldHVybiAocnhPYnNlcnZhYmxlICYmIHJ4T2JzZXJ2YWJsZS5fcCAmJiB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKG9uU3VjY2Vzcyl7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MocmVzcG9uc2UuZGF0YSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhyb3dzIGltbWVkaWF0ZWx5IGVycm9yIHdoZW4gdGhlIHBldGl0aW9uIGNhbGwgaXMgd3JvbmdcclxuICAgICAqIG9yIHdpdGggYSBkZWxheSBpZiB0aGUgY2FsbCBpcyB2YWxpZFxyXG4gICAgICogQHBhcmFtIGVycm9yXHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgb25TdWJzY3JpYmVkRXJyb3IoZXJyb3IsIHJ4T2JzZXJ2YWJsZSwgb25FcnJvcikge1xyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIGRlbGV0ZSB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXTtcclxuICAgICAgICB9XHJcbiAgICAgICAgaWYob25FcnJvcil7XHJcbiAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHt9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IFJlc3RTZXJ2aWNlSGFuZGxlciBmcm9tICcuL1Jlc3RTZXJ2aWNlSGFuZGxlci5qcyc7XHJcblxyXG52YXIgUmVzdEFQSU1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5SZXN0QVBJTW9kdWxlJyxbXSk7XHJcblxyXG5SZXN0QVBJTW9kdWxlLnNlcnZpY2UoJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFsnJGxvZycsICckaHR0cCcsICckcmVzb3VyY2UnLCAncngnLCBSZXN0U2VydmljZUhhbmRsZXJdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IFJlc3RBUElNb2R1bGU7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMDgvMTUuXHJcbiAqIEl0IGFic3RyYWN0IGVhY2ggb25lIG9mIHRoZSBleGlzdGluZyBjYWxsIHRvIHRoZSBBUEksIGl0IHNob3VsZCBvbmx5IGNvbnRhaW5zIHRoZSBjYWxsIGZ1bmN0aW9ucyBhbmQgcmVmZXJlbmNlXHJcbiAqIHRvIHRoZSBjYWxsYmFjaywgbm8gbG9naWMgYXQgYWxsLlxyXG4gKlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgUmVxdWVzdEhhbmRsZXIgZnJvbSAnLi9SZXF1ZXN0SGFuZGxlci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXN0U2VydmljZUhhbmRsZXIge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJGh0dHAsICRyZXNvdXJjZSwgcngpIHtcclxuICAgICAgICB0aGlzLnJ4ID0gcng7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuaHR0cCA9ICRodHRwO1xyXG4gICAgICAgIHRoaXMucmVzb3VyY2UgPSAkcmVzb3VyY2U7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVycygpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdSZXN0U2VydmljZSBMb2FkZWQnKTtcclxuICAgICAgICB0aGlzLnJlcSA9IHtcclxuICAgICAgICAgICAgbWV0aG9kOiAnJyxcclxuICAgICAgICAgICAgdXJsOiAnJyxcclxuICAgICAgICAgICAgaGVhZGVyczoge1xyXG4gICAgICAgICAgICAgICAgJ0NvbnRlbnQtVHlwZSc6ICdhcHBsaWNhdGlvbi9qc29uO2NoYXJzZXQ9VVRGLTgnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGE6IFtdXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlSGVhZGVycygpIHtcclxuICAgICAgICB0aGlzLmh0dHAuZGVmYXVsdHMuaGVhZGVycy5wb3N0WydDb250ZW50LVR5cGUnXSA9ICdhcHBsaWNhdGlvbi94LXd3dy1mb3JtLXVybGVuY29kZWQnO1xyXG4gICAgfVxyXG5cclxuICAgIFRhc2tTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRGZWVkczogKGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJ3Rlc3QvbW9ja3VwRGF0YS9UYXNrTWFuYWdlci90YXNrTWFuYWdlckxpc3QuanNvbicpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvZW52aXJvbm1lbnQnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0UHJvamVjdERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL3Byb2plY3QnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0TGljZW5zZUxpc3Q6IChkYXRhLCBvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2U6IChjYWxsYmFjaykgPT4geyAvLyBNb2NrdXAgRGF0YSBmb3IgdGVzdGluZyBzZWUgdXJsXHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdDogKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBhcHBseUxpY2Vuc2U6ICAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3Q6IChkYXRhLCBjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy8/Pz8nO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLnBvc3QoJy4uL3Rlc3QvbW9ja3VwRGF0YS9MaWNlbnNlQWRtaW4vbGljZW5zZUFkbWluTGlzdC5qc29uJywgZGF0YSksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZW1haWxSZXF1ZXN0OiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRlbGV0ZUxpY2Vuc2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdERUxFVEUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBsaWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAoZGF0YSwgb25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzYXZlTGljZW5zZTogKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICByZXF1ZXN0SW1wb3J0OiAgKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICByZXZva2VMaWNlbnNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGVMaWNlbnNlOiAoZGF0YSwgY2FsbGJhY2spID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvPz8/JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5wb3N0KCcuLi90ZXN0L21vY2t1cERhdGEvTGljZW5zZUFkbWluL2xpY2Vuc2VBZG1pbkxpc3QuanNvbicsIGRhdGEpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIG5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXROb3RpY2VMaXN0OiAob25TdWNjZXNzKSA9PiB7IC8vIHJlYWwgd3MgZXhhbXBsZVxyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL25vdGljZXMnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0Tm90aWNlTW9ja1VwOiAob25TdWNjZXNzKSA9PiB7IC8vIE1vY2t1cCBEYXRhIGZvciB0ZXN0aW5nIHNlZSB1cmxcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi90ZXN0L21vY2t1cERhdGEvTm90aWNlTWFuYWdlci9ub3RpY2VNYW5hZ2VyTGlzdC5qc29uJyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNyZWF0ZU5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9ub3RpY2VzJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGVkaXROb3RpY2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQVVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9ub3RpY2VzLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRlbGV0ZU5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKiBFUzYgSW50ZXJjZXB0b3IgY2FsbHMgaW5uZXIgbWV0aG9kcyBpbiBhIGdsb2JhbCBzY29wZSwgdGhlbiB0aGUgXCJ0aGlzXCIgaXMgYmVpbmcgbG9zdFxyXG4gKiBpbiB0aGUgZGVmaW5pdGlvbiBvZiB0aGUgQ2xhc3MgZm9yIGludGVyY2VwdG9ycyBvbmx5XHJcbiAqIFRoaXMgaXMgYSBpbnRlcmZhY2UgdGhhdCB0YWtlIGNhcmUgb2YgdGhlIGlzc3VlLlxyXG4gKi9cclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCAvKiBpbnRlcmZhY2UqLyBjbGFzcyBIdHRwSW50ZXJjZXB0b3Ige1xyXG4gICAgY29uc3RydWN0b3IobWV0aG9kVG9CaW5kKSB7XHJcbiAgICAgICAgLy8gSWYgbm90IG1ldGhvZCB0byBiaW5kLCB3ZSBhc3N1bWUgb3VyIGludGVyY2VwdG9yIGlzIHVzaW5nIGFsbCB0aGUgaW5uZXIgZnVuY3Rpb25zXHJcbiAgICAgICAgaWYoIW1ldGhvZFRvQmluZCkge1xyXG4gICAgICAgICAgICBbJ3JlcXVlc3QnLCAncmVxdWVzdEVycm9yJywgJ3Jlc3BvbnNlJywgJ3Jlc3BvbnNlRXJyb3InXVxyXG4gICAgICAgICAgICAgICAgLmZvckVhY2goKG1ldGhvZCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKHRoaXNbbWV0aG9kXSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzW21ldGhvZF0gPSB0aGlzW21ldGhvZF0uYmluZCh0aGlzKTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAvLyBtZXRob2RUb0JpbmQgcmVmZXJlbmNlIHRvIGEgc2luZ2xlIGNoaWxkIGNsYXNzXHJcbiAgICAgICAgICAgIHRoaXNbbWV0aG9kVG9CaW5kXSA9IHRoaXNbbWV0aG9kVG9CaW5kXS5iaW5kKHRoaXMpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICB9XHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMi8yMDE1LlxyXG4gKiBVc2UgdGhpcyBtb2R1bGUgdG8gbW9kaWZ5IGFueXRoaW5nIHJlbGF0ZWQgdG8gdGhlIEhlYWRlcnMgYW5kIFJlcXVlc3RcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMnO1xyXG5pbXBvcnQgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuXHJcblxyXG52YXIgSFRUUE1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5IVFRQTW9kdWxlJywgWyduZ1Jlc291cmNlJ10pLmNvbmZpZyhbJyRodHRwUHJvdmlkZXInLCBmdW5jdGlvbigkaHR0cFByb3ZpZGVyKXtcclxuXHJcbiAgICAvL2luaXRpYWxpemUgZ2V0IGlmIG5vdCB0aGVyZVxyXG4gICAgaWYgKCEkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0KSB7XHJcbiAgICAgICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldCA9IHt9O1xyXG4gICAgfVxyXG5cclxuICAgIC8vRGlzYWJsZSBJRSBhamF4IHJlcXVlc3QgY2FjaGluZ1xyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnSWYtTW9kaWZpZWQtU2luY2UnXSA9ICdNb24sIDI2IEp1bCAxOTk3IDA1OjAwOjAwIEdNVCc7XHJcbiAgICAvLyBleHRyYVxyXG4gICAgJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldFsnQ2FjaGUtQ29udHJvbCddID0gJ25vLWNhY2hlJztcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ1ByYWdtYSddID0gJ25vLWNhY2hlJztcclxuXHJcblxyXG4gICAgLy8gSW5qZWN0cyBvdXIgSW50ZXJjZXB0b3JzIGZvciBSZXF1ZXN0XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgLy8gSW5qZWN0cyBvdXIgSW50ZXJjZXB0b3JzIGZvciBSZXNwb25zZVxyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJyk7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmludGVyY2VwdG9ycy5wdXNoKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG5cclxuXHJcbn1dKTtcclxuXHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yXSk7XHJcbkhUVFBNb2R1bGUuc2VydmljZSgnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3InLCBbJyRsb2cnLCAnJHEnLCAncngnLCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgSFRUUE1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJdCBpbXBsZW1lbnQgYW4gYWJzdHJhY3QgY2FsbCB0byBIVFRQIEludGVyY2VwdG9ycyB0byBtYW5hZ2UgZXJyb3IgaGFuZGxlclxyXG4gKiBTb21ldGltZXMgYSByZXF1ZXN0IGNhbid0IGJlIHNlbnQgb3IgaXQgaXMgcmVqZWN0ZWQgYnkgYW4gaW50ZXJjZXB0b3IuXHJcbiAqIFJlcXVlc3QgZXJyb3IgaW50ZXJjZXB0b3IgY2FwdHVyZXMgcmVxdWVzdHMgdGhhdCBoYXZlIGJlZW4gY2FuY2VsZWQgYnkgYSBwcmV2aW91cyByZXF1ZXN0IGludGVyY2VwdG9yLlxyXG4gKiBJdCBjYW4gYmUgdXNlZCBpbiBvcmRlciB0byByZWNvdmVyIHRoZSByZXF1ZXN0IGFuZCBzb21ldGltZXMgdW5kbyB0aGluZ3MgdGhhdCBoYXZlIGJlZW4gc2V0IHVwIGJlZm9yZSBhIHJlcXVlc3QsXHJcbiAqIGxpa2UgcmVtb3Zpbmcgb3ZlcmxheXMgYW5kIGxvYWRpbmcgaW5kaWNhdG9ycywgZW5hYmxpbmcgYnV0dG9ucyBhbmQgZmllbGRzIGFuZCBzbyBvbi5cclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRxLCByeCkge1xyXG4gICAgICAgIHN1cGVyKCdyZXF1ZXN0RXJyb3InKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlcXVlc3RFcnJvcihyZWplY3Rpb24pIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gZXJyb3JcclxuICAgICAgICAvL2lmIChjYW5SZWNvdmVyKHJlamVjdGlvbikpIHtcclxuICAgICAgICAvLyAgICByZXR1cm4gcmVzcG9uc2VPck5ld1Byb21pc2VcclxuICAgICAgICAvL31cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZWplY3Rpb24pO1xyXG5cclxuICAgICAgICByZXR1cm4gdGhpcy5xLnJlamVjdChyZWplY3Rpb24pO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlbkVycm9yKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIEl0IGltcGxlbWVudCBhbiBhYnN0cmFjdCBjYWxsIHRvIEhUVFAgSW50ZXJjZXB0b3JzIHRvIG1hbmFnZSBvbmx5IHJlcXVlc3RcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGV4dGVuZHMgLyppbXBsZW1lbnRzKi8gSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVxdWVzdCcpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlcXVlc3QoY29uZmlnKSB7XHJcbiAgICAgICAgLy8gV2UgY2FuIGFkZCBoZWFkZXJzIGlmIG9uIHRoZSBpbmNvbWluZyByZXF1ZXN0IG1hZGUgaXQgd2UgaGF2ZSB0aGUgdG9rZW4gaW5zaWRlXHJcbiAgICAgICAgLy8gZGVmaW5lZCBieSBzb21lIGNvbmRpdGlvbnNcclxuICAgICAgICAvL2NvbmZpZy5oZWFkZXJzWyd4LXNlc3Npb24tdG9rZW4nXSA9IG15LnRva2VuO1xyXG5cclxuICAgICAgICBjb25maWcucmVxdWVzdFRpbWVzdGFtcCA9IG5ldyBEYXRlKCkuZ2V0VGltZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShjb25maWcpO1xyXG5cclxuICAgICAgICByZXR1cm4gY29uZmlnIHx8IHRoaXMucS53aGVuKGNvbmZpZyk7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuUmVxdWVzdCgpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJZiBiYWNrZW5kIGNhbGwgZmFpbHMgb3IgaXQgbWlnaHQgYmUgcmVqZWN0ZWQgYnkgYSByZXF1ZXN0IGludGVyY2VwdG9yIG9yIGJ5IGEgcHJldmlvdXMgcmVzcG9uc2UgaW50ZXJjZXB0b3I7XHJcbiAqIEluIHRob3NlIGNhc2VzLCByZXNwb25zZSBlcnJvciBpbnRlcmNlcHRvciBjYW4gaGVscCB1cyB0byByZWNvdmVyIHRoZSBiYWNrZW5kIGNhbGwuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVzcG9uc2VFcnJvcicpO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnEgPSAkcTtcclxuICAgICAgICB0aGlzLmRlZmVyID0gdGhpcy5xLmRlZmVyKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3BvbnNlRXJyb3IocmVqZWN0aW9uKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy9pZiAoY2FuUmVjb3ZlcihyZWplY3Rpb24pKSB7XHJcbiAgICAgICAgLy8gICAgcmV0dXJuIHJlc3BvbnNlT3JOZXdQcm9taXNlXHJcbiAgICAgICAgLy8gfVxyXG5cclxuICAgICAgICB0aGlzLmRlZmVyLm5vdGlmeShyZWplY3Rpb24pO1xyXG4gICAgICAgIHJldHVybiB0aGlzLnEucmVqZWN0KHJlamVjdGlvbik7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuRXJyb3IoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogVGhpcyBtZXRob2QgaXMgY2FsbGVkIHJpZ2h0IGFmdGVyICRodHRwIHJlY2VpdmVzIHRoZSByZXNwb25zZSBmcm9tIHRoZSBiYWNrZW5kLFxyXG4gKiBzbyB5b3UgY2FuIG1vZGlmeSB0aGUgcmVzcG9uc2UgYW5kIG1ha2Ugb3RoZXIgYWN0aW9ucy4gVGhpcyBmdW5jdGlvbiByZWNlaXZlcyBhIHJlc3BvbnNlIG9iamVjdCBhcyBhIHBhcmFtZXRlclxyXG4gKiBhbmQgaGFzIHRvIHJldHVybiBhIHJlc3BvbnNlIG9iamVjdCBvciBhIHByb21pc2UuIFRoZSByZXNwb25zZSBvYmplY3QgaW5jbHVkZXNcclxuICogdGhlIHJlcXVlc3QgY29uZmlndXJhdGlvbiwgaGVhZGVycywgc3RhdHVzIGFuZCBkYXRhIHRoYXQgcmV0dXJuZWQgZnJvbSB0aGUgYmFja2VuZC5cclxuICogUmV0dXJuaW5nIGFuIGludmFsaWQgcmVzcG9uc2Ugb2JqZWN0IG9yIHByb21pc2UgdGhhdCB3aWxsIGJlIHJlamVjdGVkLCB3aWxsIG1ha2UgdGhlICRodHRwIGNhbGwgdG8gZmFpbC5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlIGZyb20gJy4vSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVzcG9uc2UnKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgaW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVzcG9uc2UocmVzcG9uc2UpIHtcclxuICAgICAgICAvLyBkbyBzb21ldGhpbmcgb24gc3VjY2Vzc1xyXG5cclxuICAgICAgICByZXNwb25zZS5jb25maWcucmVzcG9uc2VUaW1lc3RhbXAgPSBuZXcgRGF0ZSgpLmdldFRpbWUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVzcG9uc2UpO1xyXG4gICAgICAgIHJldHVybiByZXNwb25zZSB8fCB0aGlzLnEud2hlbihyZXNwb25zZSk7XHJcbiAgICB9XHJcblxyXG4gICAgbGlzdGVuUmVzcG9uc2UoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxufVxyXG5cclxuIl19
