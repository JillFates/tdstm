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
            activityList: params.license.activityList,
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
            var _this3 = this;

            this.licenseManagerService.activateLicense(this.licenseModel, function (data) {
                _this3.licenseModel.statusId = 1;
                _this3.getStatusDataSource();
                _this3.saveForm(_this3.licenseModel);
                _this3.prepareControlActionButtons();
                _this3.prepareLicenseKey();
                _this3.reloadRequired = true;
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

                                _this6.saveForm(_this6.licenseModel);
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
                activationDate: license.initDate,
                expirationDate: license.endDate,
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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvY2xpcGJvYXJkL2xpYi9jbGlwYm9hcmQtYWN0aW9uLmpzIiwibm9kZV9tb2R1bGVzL2NsaXBib2FyZC9saWIvY2xpcGJvYXJkLmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9jbG9zZXN0LmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9kZWxlZ2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9pcy5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9saXN0ZW4uanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvZGlzdC9uZ2NsaXBib2FyZC5qcyIsIm5vZGVfbW9kdWxlcy9zZWxlY3Qvc3JjL3NlbGVjdC5qcyIsIm5vZGVfbW9kdWxlcy90aW55LWVtaXR0ZXIvaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxjb25maWdcXEFwcC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxNb2RhbFdpbmRvd0FjdGl2YXRpb24uanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxUb2FzdEhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1haW4uanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGRpYWxvZ0FjdGlvblxcRGlhbG9nQWN0aW9uLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxoZWFkZXJcXEhlYWRlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGhlYWRlclxcSGVhZGVyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXExpY2Vuc2VBZG1pbk1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxhcHBseUxpY2Vuc2VLZXlcXEFwcGx5TGljZW5zZUtleS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxjcmVhdGVkXFxDcmVhdGVkTGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxkZXRhaWxcXExpY2Vuc2VEZXRhaWwuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcbGlzdFxcTGljZW5zZUFkbWluTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxtYW51YWxseVJlcXVlc3RcXE1hbnVhbGx5UmVxdWVzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxyZXF1ZXN0XFxSZXF1ZXN0TGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxzZXJ2aWNlXFxMaWNlbnNlQWRtaW5TZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcTGljZW5zZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxkZXRhaWxcXExpY2Vuc2VNYW5hZ2VyRGV0YWlsLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcbGlzdFxcTGljZW5zZU1hbmFnZXJMaXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxccmVxdWVzdEltcG9ydFxcUmVxdWVzdEltcG9ydC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXHNlcnZpY2VcXExpY2Vuc2VNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcTm90aWNlTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcZWRpdFxcRWRpdE5vdGljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcbGlzdFxcTm90aWNlTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcc2VydmljZVxcTm90aWNlTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxUYXNrTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGVkaXRcXFRhc2tNYW5hZ2VyRWRpdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGxpc3RcXFRhc2tNYW5hZ2VyQ29udHJvbGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXHNlcnZpY2VcXFRhc2tNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdXRpbHNcXGZvcm1cXEZvcm1WYWxpZGF0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RBUElNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXN0U2VydmljZUhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IuanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNwT0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4TEE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzVDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2pEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDL0ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7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QTtBQUNBO0FBQ0E7QUFFQTs7aUJBQUEsQUFBSyxNQUFMLEFBQVcsT0FBWCxBQUFrQixBQUNsQjttQkFBTyxLQUFBLEFBQUssRUFBTCxBQUFPLE9BQWQsQUFBTyxBQUFjLEFBQ3hCOzs7O3NDQUVhLEFBQ1Y7bUJBQU8sS0FBQSxBQUFLLE1BQVosQUFBa0IsQUFDckI7Ozs7O0dBckI0RCxjOztrQixBQUE1Qzs7O0FDVnJCOzs7Ozs7Ozs7QUFTQTs7Ozs7Ozs7Ozs7Ozs7Ozs7O0FBRUE7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7O0ksQUFFcUI7OENBQ2pCOzs0Q0FBQSxBQUFZLE1BQVosQUFBa0IsSUFBbEIsQUFBc0IsSUFBSTs4QkFBQTs7b0tBQUEsQUFDaEIsQUFDTjs7Y0FBQSxBQUFLLE1BQUwsQUFBVyxBQUNYO2NBQUEsQUFBSyxJQUFMLEFBQVMsQUFDVDtjQUFBLEFBQUssUUFBUSxNQUFBLEFBQUssRUFBbEIsQUFBYSxBQUFPLEFBQ3BCO2NBQUEsQUFBSyxJQUFMLEFBQVMsTUFMYSxBQUt0QixBQUFlO2VBQ2xCOzs7OztpQyxBQUVRLFdBQVUsQUFDZjtBQUVBOztzQkFBQSxBQUFTLE9BQVQsQUFBZ0Isb0JBQW9CLElBQUEsQUFBSSxPQUF4QyxBQUFvQyxBQUFXLEFBRS9DOztpQkFBQSxBQUFLLE1BQUwsQUFBVyxPQUFYLEFBQWtCLEFBQ2xCO21CQUFPLGFBQVksS0FBQSxBQUFLLEVBQUwsQUFBTyxLQUExQixBQUFtQixBQUFZLEFBQ2xDOzs7O3lDQUVnQixBQUNiO21CQUFPLEtBQUEsQUFBSyxNQUFaLEFBQWtCLEFBQ3JCOzs7OztHQXBCdUQsYzs7a0IsQUFBdkMiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwiKGZ1bmN0aW9uIChnbG9iYWwsIGZhY3RvcnkpIHtcbiAgICBpZiAodHlwZW9mIGRlZmluZSA9PT0gXCJmdW5jdGlvblwiICYmIGRlZmluZS5hbWQpIHtcbiAgICAgICAgZGVmaW5lKFsnbW9kdWxlJywgJ3NlbGVjdCddLCBmYWN0b3J5KTtcbiAgICB9IGVsc2UgaWYgKHR5cGVvZiBleHBvcnRzICE9PSBcInVuZGVmaW5lZFwiKSB7XG4gICAgICAgIGZhY3RvcnkobW9kdWxlLCByZXF1aXJlKCdzZWxlY3QnKSk7XG4gICAgfSBlbHNlIHtcbiAgICAgICAgdmFyIG1vZCA9IHtcbiAgICAgICAgICAgIGV4cG9ydHM6IHt9XG4gICAgICAgIH07XG4gICAgICAgIGZhY3RvcnkobW9kLCBnbG9iYWwuc2VsZWN0KTtcbiAgICAgICAgZ2xvYmFsLmNsaXBib2FyZEFjdGlvbiA9IG1vZC5leHBvcnRzO1xuICAgIH1cbn0pKHRoaXMsIGZ1bmN0aW9uIChtb2R1bGUsIF9zZWxlY3QpIHtcbiAgICAndXNlIHN0cmljdCc7XG5cbiAgICB2YXIgX3NlbGVjdDIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9zZWxlY3QpO1xuXG4gICAgZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHtcbiAgICAgICAgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHtcbiAgICAgICAgICAgIGRlZmF1bHQ6IG9ialxuICAgICAgICB9O1xuICAgIH1cblxuICAgIHZhciBfdHlwZW9mID0gdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIHR5cGVvZiBTeW1ib2wuaXRlcmF0b3IgPT09IFwic3ltYm9sXCIgPyBmdW5jdGlvbiAob2JqKSB7XG4gICAgICAgIHJldHVybiB0eXBlb2Ygb2JqO1xuICAgIH0gOiBmdW5jdGlvbiAob2JqKSB7XG4gICAgICAgIHJldHVybiBvYmogJiYgdHlwZW9mIFN5bWJvbCA9PT0gXCJmdW5jdGlvblwiICYmIG9iai5jb25zdHJ1Y3RvciA9PT0gU3ltYm9sICYmIG9iaiAhPT0gU3ltYm9sLnByb3RvdHlwZSA/IFwic3ltYm9sXCIgOiB0eXBlb2Ygb2JqO1xuICAgIH07XG5cbiAgICBmdW5jdGlvbiBfY2xhc3NDYWxsQ2hlY2soaW5zdGFuY2UsIENvbnN0cnVjdG9yKSB7XG4gICAgICAgIGlmICghKGluc3RhbmNlIGluc3RhbmNlb2YgQ29uc3RydWN0b3IpKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKFwiQ2Fubm90IGNhbGwgYSBjbGFzcyBhcyBhIGZ1bmN0aW9uXCIpO1xuICAgICAgICB9XG4gICAgfVxuXG4gICAgdmFyIF9jcmVhdGVDbGFzcyA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgZnVuY3Rpb24gZGVmaW5lUHJvcGVydGllcyh0YXJnZXQsIHByb3BzKSB7XG4gICAgICAgICAgICBmb3IgKHZhciBpID0gMDsgaSA8IHByb3BzLmxlbmd0aDsgaSsrKSB7XG4gICAgICAgICAgICAgICAgdmFyIGRlc2NyaXB0b3IgPSBwcm9wc1tpXTtcbiAgICAgICAgICAgICAgICBkZXNjcmlwdG9yLmVudW1lcmFibGUgPSBkZXNjcmlwdG9yLmVudW1lcmFibGUgfHwgZmFsc2U7XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5jb25maWd1cmFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIGlmIChcInZhbHVlXCIgaW4gZGVzY3JpcHRvcikgZGVzY3JpcHRvci53cml0YWJsZSA9IHRydWU7XG4gICAgICAgICAgICAgICAgT2JqZWN0LmRlZmluZVByb3BlcnR5KHRhcmdldCwgZGVzY3JpcHRvci5rZXksIGRlc2NyaXB0b3IpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGZ1bmN0aW9uIChDb25zdHJ1Y3RvciwgcHJvdG9Qcm9wcywgc3RhdGljUHJvcHMpIHtcbiAgICAgICAgICAgIGlmIChwcm90b1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLnByb3RvdHlwZSwgcHJvdG9Qcm9wcyk7XG4gICAgICAgICAgICBpZiAoc3RhdGljUHJvcHMpIGRlZmluZVByb3BlcnRpZXMoQ29uc3RydWN0b3IsIHN0YXRpY1Byb3BzKTtcbiAgICAgICAgICAgIHJldHVybiBDb25zdHJ1Y3RvcjtcbiAgICAgICAgfTtcbiAgICB9KCk7XG5cbiAgICB2YXIgQ2xpcGJvYXJkQWN0aW9uID0gZnVuY3Rpb24gKCkge1xuICAgICAgICAvKipcbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG4gICAgICAgIGZ1bmN0aW9uIENsaXBib2FyZEFjdGlvbihvcHRpb25zKSB7XG4gICAgICAgICAgICBfY2xhc3NDYWxsQ2hlY2sodGhpcywgQ2xpcGJvYXJkQWN0aW9uKTtcblxuICAgICAgICAgICAgdGhpcy5yZXNvbHZlT3B0aW9ucyhvcHRpb25zKTtcbiAgICAgICAgICAgIHRoaXMuaW5pdFNlbGVjdGlvbigpO1xuICAgICAgICB9XG5cbiAgICAgICAgLyoqXG4gICAgICAgICAqIERlZmluZXMgYmFzZSBwcm9wZXJ0aWVzIHBhc3NlZCBmcm9tIGNvbnN0cnVjdG9yLlxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cblxuXG4gICAgICAgIF9jcmVhdGVDbGFzcyhDbGlwYm9hcmRBY3Rpb24sIFt7XG4gICAgICAgICAgICBrZXk6ICdyZXNvbHZlT3B0aW9ucycsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gcmVzb2x2ZU9wdGlvbnMoKSB7XG4gICAgICAgICAgICAgICAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoID4gMCAmJiBhcmd1bWVudHNbMF0gIT09IHVuZGVmaW5lZCA/IGFyZ3VtZW50c1swXSA6IHt9O1xuXG4gICAgICAgICAgICAgICAgdGhpcy5hY3Rpb24gPSBvcHRpb25zLmFjdGlvbjtcbiAgICAgICAgICAgICAgICB0aGlzLmVtaXR0ZXIgPSBvcHRpb25zLmVtaXR0ZXI7XG4gICAgICAgICAgICAgICAgdGhpcy50YXJnZXQgPSBvcHRpb25zLnRhcmdldDtcbiAgICAgICAgICAgICAgICB0aGlzLnRleHQgPSBvcHRpb25zLnRleHQ7XG4gICAgICAgICAgICAgICAgdGhpcy50cmlnZ2VyID0gb3B0aW9ucy50cmlnZ2VyO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RlZFRleHQgPSAnJztcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnaW5pdFNlbGVjdGlvbicsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gaW5pdFNlbGVjdGlvbigpIHtcbiAgICAgICAgICAgICAgICBpZiAodGhpcy50ZXh0KSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0RmFrZSgpO1xuICAgICAgICAgICAgICAgIH0gZWxzZSBpZiAodGhpcy50YXJnZXQpIHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RUYXJnZXQoKTtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3NlbGVjdEZha2UnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHNlbGVjdEZha2UoKSB7XG4gICAgICAgICAgICAgICAgdmFyIF90aGlzID0gdGhpcztcblxuICAgICAgICAgICAgICAgIHZhciBpc1JUTCA9IGRvY3VtZW50LmRvY3VtZW50RWxlbWVudC5nZXRBdHRyaWJ1dGUoJ2RpcicpID09ICdydGwnO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5yZW1vdmVGYWtlKCk7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2sgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBfdGhpcy5yZW1vdmVGYWtlKCk7XG4gICAgICAgICAgICAgICAgfTtcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyID0gZG9jdW1lbnQuYm9keS5hZGRFdmVudExpc3RlbmVyKCdjbGljaycsIHRoaXMuZmFrZUhhbmRsZXJDYWxsYmFjaykgfHwgdHJ1ZTtcblxuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0gPSBkb2N1bWVudC5jcmVhdGVFbGVtZW50KCd0ZXh0YXJlYScpO1xuICAgICAgICAgICAgICAgIC8vIFByZXZlbnQgem9vbWluZyBvbiBpT1NcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLmZvbnRTaXplID0gJzEycHQnO1xuICAgICAgICAgICAgICAgIC8vIFJlc2V0IGJveCBtb2RlbFxuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGUuYm9yZGVyID0gJzAnO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uc3R5bGUucGFkZGluZyA9ICcwJztcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLm1hcmdpbiA9ICcwJztcbiAgICAgICAgICAgICAgICAvLyBNb3ZlIGVsZW1lbnQgb3V0IG9mIHNjcmVlbiBob3Jpem9udGFsbHlcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLnBvc2l0aW9uID0gJ2Fic29sdXRlJztcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlW2lzUlRMID8gJ3JpZ2h0JyA6ICdsZWZ0J10gPSAnLTk5OTlweCc7XG4gICAgICAgICAgICAgICAgLy8gTW92ZSBlbGVtZW50IHRvIHRoZSBzYW1lIHBvc2l0aW9uIHZlcnRpY2FsbHlcbiAgICAgICAgICAgICAgICB2YXIgeVBvc2l0aW9uID0gd2luZG93LnBhZ2VZT2Zmc2V0IHx8IGRvY3VtZW50LmRvY3VtZW50RWxlbWVudC5zY3JvbGxUb3A7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5hZGRFdmVudExpc3RlbmVyKCdmb2N1cycsIHdpbmRvdy5zY3JvbGxUbygwLCB5UG9zaXRpb24pKTtcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLnRvcCA9IHlQb3NpdGlvbiArICdweCc7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnNldEF0dHJpYnV0ZSgncmVhZG9ubHknLCAnJyk7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS52YWx1ZSA9IHRoaXMudGV4dDtcblxuICAgICAgICAgICAgICAgIGRvY3VtZW50LmJvZHkuYXBwZW5kQ2hpbGQodGhpcy5mYWtlRWxlbSk7XG5cbiAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdGVkVGV4dCA9ICgwLCBfc2VsZWN0Mi5kZWZhdWx0KSh0aGlzLmZha2VFbGVtKTtcbiAgICAgICAgICAgICAgICB0aGlzLmNvcHlUZXh0KCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3JlbW92ZUZha2UnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlbW92ZUZha2UoKSB7XG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuZmFrZUhhbmRsZXIpIHtcbiAgICAgICAgICAgICAgICAgICAgZG9jdW1lbnQuYm9keS5yZW1vdmVFdmVudExpc3RlbmVyKCdjbGljaycsIHRoaXMuZmFrZUhhbmRsZXJDYWxsYmFjayk7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuZmFrZUhhbmRsZXIgPSBudWxsO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2sgPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgIGlmICh0aGlzLmZha2VFbGVtKSB7XG4gICAgICAgICAgICAgICAgICAgIGRvY3VtZW50LmJvZHkucmVtb3ZlQ2hpbGQodGhpcy5mYWtlRWxlbSk7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0gPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnc2VsZWN0VGFyZ2V0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBzZWxlY3RUYXJnZXQoKSB7XG4gICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RlZFRleHQgPSAoMCwgX3NlbGVjdDIuZGVmYXVsdCkodGhpcy50YXJnZXQpO1xuICAgICAgICAgICAgICAgIHRoaXMuY29weVRleHQoKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnY29weVRleHQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGNvcHlUZXh0KCkge1xuICAgICAgICAgICAgICAgIHZhciBzdWNjZWVkZWQgPSB2b2lkIDA7XG5cbiAgICAgICAgICAgICAgICB0cnkge1xuICAgICAgICAgICAgICAgICAgICBzdWNjZWVkZWQgPSBkb2N1bWVudC5leGVjQ29tbWFuZCh0aGlzLmFjdGlvbik7XG4gICAgICAgICAgICAgICAgfSBjYXRjaCAoZXJyKSB7XG4gICAgICAgICAgICAgICAgICAgIHN1Y2NlZWRlZCA9IGZhbHNlO1xuICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgIHRoaXMuaGFuZGxlUmVzdWx0KHN1Y2NlZWRlZCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2hhbmRsZVJlc3VsdCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gaGFuZGxlUmVzdWx0KHN1Y2NlZWRlZCkge1xuICAgICAgICAgICAgICAgIHRoaXMuZW1pdHRlci5lbWl0KHN1Y2NlZWRlZCA/ICdzdWNjZXNzJyA6ICdlcnJvcicsIHtcbiAgICAgICAgICAgICAgICAgICAgYWN0aW9uOiB0aGlzLmFjdGlvbixcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogdGhpcy5zZWxlY3RlZFRleHQsXG4gICAgICAgICAgICAgICAgICAgIHRyaWdnZXI6IHRoaXMudHJpZ2dlcixcbiAgICAgICAgICAgICAgICAgICAgY2xlYXJTZWxlY3Rpb246IHRoaXMuY2xlYXJTZWxlY3Rpb24uYmluZCh0aGlzKVxuICAgICAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdjbGVhclNlbGVjdGlvbicsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gY2xlYXJTZWxlY3Rpb24oKSB7XG4gICAgICAgICAgICAgICAgaWYgKHRoaXMudGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMudGFyZ2V0LmJsdXIoKTtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB3aW5kb3cuZ2V0U2VsZWN0aW9uKCkucmVtb3ZlQWxsUmFuZ2VzKCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2Rlc3Ryb3knLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlc3Ryb3koKSB7XG4gICAgICAgICAgICAgICAgdGhpcy5yZW1vdmVGYWtlKCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2FjdGlvbicsXG4gICAgICAgICAgICBzZXQ6IGZ1bmN0aW9uIHNldCgpIHtcbiAgICAgICAgICAgICAgICB2YXIgYWN0aW9uID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiAnY29weSc7XG5cbiAgICAgICAgICAgICAgICB0aGlzLl9hY3Rpb24gPSBhY3Rpb247XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5fYWN0aW9uICE9PSAnY29weScgJiYgdGhpcy5fYWN0aW9uICE9PSAnY3V0Jykge1xuICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJhY3Rpb25cIiB2YWx1ZSwgdXNlIGVpdGhlciBcImNvcHlcIiBvciBcImN1dFwiJyk7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfSxcbiAgICAgICAgICAgIGdldDogZnVuY3Rpb24gZ2V0KCkge1xuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLl9hY3Rpb247XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3RhcmdldCcsXG4gICAgICAgICAgICBzZXQ6IGZ1bmN0aW9uIHNldCh0YXJnZXQpIHtcbiAgICAgICAgICAgICAgICBpZiAodGFyZ2V0ICE9PSB1bmRlZmluZWQpIHtcbiAgICAgICAgICAgICAgICAgICAgaWYgKHRhcmdldCAmJiAodHlwZW9mIHRhcmdldCA9PT0gJ3VuZGVmaW5lZCcgPyAndW5kZWZpbmVkJyA6IF90eXBlb2YodGFyZ2V0KSkgPT09ICdvYmplY3QnICYmIHRhcmdldC5ub2RlVHlwZSA9PT0gMSkge1xuICAgICAgICAgICAgICAgICAgICAgICAgaWYgKHRoaXMuYWN0aW9uID09PSAnY29weScgJiYgdGFyZ2V0Lmhhc0F0dHJpYnV0ZSgnZGlzYWJsZWQnKSkge1xuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignSW52YWxpZCBcInRhcmdldFwiIGF0dHJpYnV0ZS4gUGxlYXNlIHVzZSBcInJlYWRvbmx5XCIgaW5zdGVhZCBvZiBcImRpc2FibGVkXCIgYXR0cmlidXRlJyk7XG4gICAgICAgICAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICAgICAgICAgIGlmICh0aGlzLmFjdGlvbiA9PT0gJ2N1dCcgJiYgKHRhcmdldC5oYXNBdHRyaWJ1dGUoJ3JlYWRvbmx5JykgfHwgdGFyZ2V0Lmhhc0F0dHJpYnV0ZSgnZGlzYWJsZWQnKSkpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJ0YXJnZXRcIiBhdHRyaWJ1dGUuIFlvdSBjYW5cXCd0IGN1dCB0ZXh0IGZyb20gZWxlbWVudHMgd2l0aCBcInJlYWRvbmx5XCIgb3IgXCJkaXNhYmxlZFwiIGF0dHJpYnV0ZXMnKTtcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5fdGFyZ2V0ID0gdGFyZ2V0O1xuICAgICAgICAgICAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwidGFyZ2V0XCIgdmFsdWUsIHVzZSBhIHZhbGlkIEVsZW1lbnQnKTtcbiAgICAgICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5fdGFyZ2V0O1xuICAgICAgICAgICAgfVxuICAgICAgICB9XSk7XG5cbiAgICAgICAgcmV0dXJuIENsaXBib2FyZEFjdGlvbjtcbiAgICB9KCk7XG5cbiAgICBtb2R1bGUuZXhwb3J0cyA9IENsaXBib2FyZEFjdGlvbjtcbn0pOyIsIihmdW5jdGlvbiAoZ2xvYmFsLCBmYWN0b3J5KSB7XG4gICAgaWYgKHR5cGVvZiBkZWZpbmUgPT09IFwiZnVuY3Rpb25cIiAmJiBkZWZpbmUuYW1kKSB7XG4gICAgICAgIGRlZmluZShbJ21vZHVsZScsICcuL2NsaXBib2FyZC1hY3Rpb24nLCAndGlueS1lbWl0dGVyJywgJ2dvb2QtbGlzdGVuZXInXSwgZmFjdG9yeSk7XG4gICAgfSBlbHNlIGlmICh0eXBlb2YgZXhwb3J0cyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgICAgICBmYWN0b3J5KG1vZHVsZSwgcmVxdWlyZSgnLi9jbGlwYm9hcmQtYWN0aW9uJyksIHJlcXVpcmUoJ3RpbnktZW1pdHRlcicpLCByZXF1aXJlKCdnb29kLWxpc3RlbmVyJykpO1xuICAgIH0gZWxzZSB7XG4gICAgICAgIHZhciBtb2QgPSB7XG4gICAgICAgICAgICBleHBvcnRzOiB7fVxuICAgICAgICB9O1xuICAgICAgICBmYWN0b3J5KG1vZCwgZ2xvYmFsLmNsaXBib2FyZEFjdGlvbiwgZ2xvYmFsLnRpbnlFbWl0dGVyLCBnbG9iYWwuZ29vZExpc3RlbmVyKTtcbiAgICAgICAgZ2xvYmFsLmNsaXBib2FyZCA9IG1vZC5leHBvcnRzO1xuICAgIH1cbn0pKHRoaXMsIGZ1bmN0aW9uIChtb2R1bGUsIF9jbGlwYm9hcmRBY3Rpb24sIF90aW55RW1pdHRlciwgX2dvb2RMaXN0ZW5lcikge1xuICAgICd1c2Ugc3RyaWN0JztcblxuICAgIHZhciBfY2xpcGJvYXJkQWN0aW9uMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2NsaXBib2FyZEFjdGlvbik7XG5cbiAgICB2YXIgX3RpbnlFbWl0dGVyMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX3RpbnlFbWl0dGVyKTtcblxuICAgIHZhciBfZ29vZExpc3RlbmVyMiA9IF9pbnRlcm9wUmVxdWlyZURlZmF1bHQoX2dvb2RMaXN0ZW5lcik7XG5cbiAgICBmdW5jdGlvbiBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KG9iaikge1xuICAgICAgICByZXR1cm4gb2JqICYmIG9iai5fX2VzTW9kdWxlID8gb2JqIDoge1xuICAgICAgICAgICAgZGVmYXVsdDogb2JqXG4gICAgICAgIH07XG4gICAgfVxuXG4gICAgZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3Rvcikge1xuICAgICAgICBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIkNhbm5vdCBjYWxsIGEgY2xhc3MgYXMgYSBmdW5jdGlvblwiKTtcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBfY3JlYXRlQ2xhc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIGZ1bmN0aW9uIGRlZmluZVByb3BlcnRpZXModGFyZ2V0LCBwcm9wcykge1xuICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBwcm9wcy5sZW5ndGg7IGkrKykge1xuICAgICAgICAgICAgICAgIHZhciBkZXNjcmlwdG9yID0gcHJvcHNbaV07XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5lbnVtZXJhYmxlID0gZGVzY3JpcHRvci5lbnVtZXJhYmxlIHx8IGZhbHNlO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuY29uZmlndXJhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBpZiAoXCJ2YWx1ZVwiIGluIGRlc2NyaXB0b3IpIGRlc2NyaXB0b3Iud3JpdGFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIE9iamVjdC5kZWZpbmVQcm9wZXJ0eSh0YXJnZXQsIGRlc2NyaXB0b3Iua2V5LCBkZXNjcmlwdG9yKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBmdW5jdGlvbiAoQ29uc3RydWN0b3IsIHByb3RvUHJvcHMsIHN0YXRpY1Byb3BzKSB7XG4gICAgICAgICAgICBpZiAocHJvdG9Qcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvci5wcm90b3R5cGUsIHByb3RvUHJvcHMpO1xuICAgICAgICAgICAgaWYgKHN0YXRpY1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLCBzdGF0aWNQcm9wcyk7XG4gICAgICAgICAgICByZXR1cm4gQ29uc3RydWN0b3I7XG4gICAgICAgIH07XG4gICAgfSgpO1xuXG4gICAgZnVuY3Rpb24gX3Bvc3NpYmxlQ29uc3RydWN0b3JSZXR1cm4oc2VsZiwgY2FsbCkge1xuICAgICAgICBpZiAoIXNlbGYpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBSZWZlcmVuY2VFcnJvcihcInRoaXMgaGFzbid0IGJlZW4gaW5pdGlhbGlzZWQgLSBzdXBlcigpIGhhc24ndCBiZWVuIGNhbGxlZFwiKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBjYWxsICYmICh0eXBlb2YgY2FsbCA9PT0gXCJvYmplY3RcIiB8fCB0eXBlb2YgY2FsbCA9PT0gXCJmdW5jdGlvblwiKSA/IGNhbGwgOiBzZWxmO1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIF9pbmhlcml0cyhzdWJDbGFzcywgc3VwZXJDbGFzcykge1xuICAgICAgICBpZiAodHlwZW9mIHN1cGVyQ2xhc3MgIT09IFwiZnVuY3Rpb25cIiAmJiBzdXBlckNsYXNzICE9PSBudWxsKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKFwiU3VwZXIgZXhwcmVzc2lvbiBtdXN0IGVpdGhlciBiZSBudWxsIG9yIGEgZnVuY3Rpb24sIG5vdCBcIiArIHR5cGVvZiBzdXBlckNsYXNzKTtcbiAgICAgICAgfVxuXG4gICAgICAgIHN1YkNsYXNzLnByb3RvdHlwZSA9IE9iamVjdC5jcmVhdGUoc3VwZXJDbGFzcyAmJiBzdXBlckNsYXNzLnByb3RvdHlwZSwge1xuICAgICAgICAgICAgY29uc3RydWN0b3I6IHtcbiAgICAgICAgICAgICAgICB2YWx1ZTogc3ViQ2xhc3MsXG4gICAgICAgICAgICAgICAgZW51bWVyYWJsZTogZmFsc2UsXG4gICAgICAgICAgICAgICAgd3JpdGFibGU6IHRydWUsXG4gICAgICAgICAgICAgICAgY29uZmlndXJhYmxlOiB0cnVlXG4gICAgICAgICAgICB9XG4gICAgICAgIH0pO1xuICAgICAgICBpZiAoc3VwZXJDbGFzcykgT2JqZWN0LnNldFByb3RvdHlwZU9mID8gT2JqZWN0LnNldFByb3RvdHlwZU9mKHN1YkNsYXNzLCBzdXBlckNsYXNzKSA6IHN1YkNsYXNzLl9fcHJvdG9fXyA9IHN1cGVyQ2xhc3M7XG4gICAgfVxuXG4gICAgdmFyIENsaXBib2FyZCA9IGZ1bmN0aW9uIChfRW1pdHRlcikge1xuICAgICAgICBfaW5oZXJpdHMoQ2xpcGJvYXJkLCBfRW1pdHRlcik7XG5cbiAgICAgICAgLyoqXG4gICAgICAgICAqIEBwYXJhbSB7U3RyaW5nfEhUTUxFbGVtZW50fEhUTUxDb2xsZWN0aW9ufE5vZGVMaXN0fSB0cmlnZ2VyXG4gICAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvcHRpb25zXG4gICAgICAgICAqL1xuICAgICAgICBmdW5jdGlvbiBDbGlwYm9hcmQodHJpZ2dlciwgb3B0aW9ucykge1xuICAgICAgICAgICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIENsaXBib2FyZCk7XG5cbiAgICAgICAgICAgIHZhciBfdGhpcyA9IF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHRoaXMsIChDbGlwYm9hcmQuX19wcm90b19fIHx8IE9iamVjdC5nZXRQcm90b3R5cGVPZihDbGlwYm9hcmQpKS5jYWxsKHRoaXMpKTtcblxuICAgICAgICAgICAgX3RoaXMucmVzb2x2ZU9wdGlvbnMob3B0aW9ucyk7XG4gICAgICAgICAgICBfdGhpcy5saXN0ZW5DbGljayh0cmlnZ2VyKTtcbiAgICAgICAgICAgIHJldHVybiBfdGhpcztcbiAgICAgICAgfVxuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBEZWZpbmVzIGlmIGF0dHJpYnV0ZXMgd291bGQgYmUgcmVzb2x2ZWQgdXNpbmcgaW50ZXJuYWwgc2V0dGVyIGZ1bmN0aW9uc1xuICAgICAgICAgKiBvciBjdXN0b20gZnVuY3Rpb25zIHRoYXQgd2VyZSBwYXNzZWQgaW4gdGhlIGNvbnN0cnVjdG9yLlxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cblxuXG4gICAgICAgIF9jcmVhdGVDbGFzcyhDbGlwYm9hcmQsIFt7XG4gICAgICAgICAgICBrZXk6ICdyZXNvbHZlT3B0aW9ucycsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gcmVzb2x2ZU9wdGlvbnMoKSB7XG4gICAgICAgICAgICAgICAgdmFyIG9wdGlvbnMgPSBhcmd1bWVudHMubGVuZ3RoID4gMCAmJiBhcmd1bWVudHNbMF0gIT09IHVuZGVmaW5lZCA/IGFyZ3VtZW50c1swXSA6IHt9O1xuXG4gICAgICAgICAgICAgICAgdGhpcy5hY3Rpb24gPSB0eXBlb2Ygb3B0aW9ucy5hY3Rpb24gPT09ICdmdW5jdGlvbicgPyBvcHRpb25zLmFjdGlvbiA6IHRoaXMuZGVmYXVsdEFjdGlvbjtcbiAgICAgICAgICAgICAgICB0aGlzLnRhcmdldCA9IHR5cGVvZiBvcHRpb25zLnRhcmdldCA9PT0gJ2Z1bmN0aW9uJyA/IG9wdGlvbnMudGFyZ2V0IDogdGhpcy5kZWZhdWx0VGFyZ2V0O1xuICAgICAgICAgICAgICAgIHRoaXMudGV4dCA9IHR5cGVvZiBvcHRpb25zLnRleHQgPT09ICdmdW5jdGlvbicgPyBvcHRpb25zLnRleHQgOiB0aGlzLmRlZmF1bHRUZXh0O1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdsaXN0ZW5DbGljaycsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gbGlzdGVuQ2xpY2sodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHZhciBfdGhpczIgPSB0aGlzO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5saXN0ZW5lciA9ICgwLCBfZ29vZExpc3RlbmVyMi5kZWZhdWx0KSh0cmlnZ2VyLCAnY2xpY2snLCBmdW5jdGlvbiAoZSkge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gX3RoaXMyLm9uQ2xpY2soZSk7XG4gICAgICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ29uQ2xpY2snLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIG9uQ2xpY2soZSkge1xuICAgICAgICAgICAgICAgIHZhciB0cmlnZ2VyID0gZS5kZWxlZ2F0ZVRhcmdldCB8fCBlLmN1cnJlbnRUYXJnZXQ7XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5jbGlwYm9hcmRBY3Rpb24pIHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24gPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cblxuICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uID0gbmV3IF9jbGlwYm9hcmRBY3Rpb24yLmRlZmF1bHQoe1xuICAgICAgICAgICAgICAgICAgICBhY3Rpb246IHRoaXMuYWN0aW9uKHRyaWdnZXIpLFxuICAgICAgICAgICAgICAgICAgICB0YXJnZXQ6IHRoaXMudGFyZ2V0KHRyaWdnZXIpLFxuICAgICAgICAgICAgICAgICAgICB0ZXh0OiB0aGlzLnRleHQodHJpZ2dlciksXG4gICAgICAgICAgICAgICAgICAgIHRyaWdnZXI6IHRyaWdnZXIsXG4gICAgICAgICAgICAgICAgICAgIGVtaXR0ZXI6IHRoaXNcbiAgICAgICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnZGVmYXVsdEFjdGlvbicsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVmYXVsdEFjdGlvbih0cmlnZ2VyKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIGdldEF0dHJpYnV0ZVZhbHVlKCdhY3Rpb24nLCB0cmlnZ2VyKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnZGVmYXVsdFRhcmdldCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVmYXVsdFRhcmdldCh0cmlnZ2VyKSB7XG4gICAgICAgICAgICAgICAgdmFyIHNlbGVjdG9yID0gZ2V0QXR0cmlidXRlVmFsdWUoJ3RhcmdldCcsIHRyaWdnZXIpO1xuXG4gICAgICAgICAgICAgICAgaWYgKHNlbGVjdG9yKSB7XG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBkb2N1bWVudC5xdWVyeVNlbGVjdG9yKHNlbGVjdG9yKTtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRUZXh0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZWZhdWx0VGV4dCh0cmlnZ2VyKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIGdldEF0dHJpYnV0ZVZhbHVlKCd0ZXh0JywgdHJpZ2dlcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2Rlc3Ryb3knLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlc3Ryb3koKSB7XG4gICAgICAgICAgICAgICAgdGhpcy5saXN0ZW5lci5kZXN0cm95KCk7XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5jbGlwYm9hcmRBY3Rpb24pIHtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24uZGVzdHJveSgpO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmNsaXBib2FyZEFjdGlvbiA9IG51bGw7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9XSk7XG5cbiAgICAgICAgcmV0dXJuIENsaXBib2FyZDtcbiAgICB9KF90aW55RW1pdHRlcjIuZGVmYXVsdCk7XG5cbiAgICAvKipcbiAgICAgKiBIZWxwZXIgZnVuY3Rpb24gdG8gcmV0cmlldmUgYXR0cmlidXRlIHZhbHVlLlxuICAgICAqIEBwYXJhbSB7U3RyaW5nfSBzdWZmaXhcbiAgICAgKiBAcGFyYW0ge0VsZW1lbnR9IGVsZW1lbnRcbiAgICAgKi9cbiAgICBmdW5jdGlvbiBnZXRBdHRyaWJ1dGVWYWx1ZShzdWZmaXgsIGVsZW1lbnQpIHtcbiAgICAgICAgdmFyIGF0dHJpYnV0ZSA9ICdkYXRhLWNsaXBib2FyZC0nICsgc3VmZml4O1xuXG4gICAgICAgIGlmICghZWxlbWVudC5oYXNBdHRyaWJ1dGUoYXR0cmlidXRlKSkge1xuICAgICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG5cbiAgICAgICAgcmV0dXJuIGVsZW1lbnQuZ2V0QXR0cmlidXRlKGF0dHJpYnV0ZSk7XG4gICAgfVxuXG4gICAgbW9kdWxlLmV4cG9ydHMgPSBDbGlwYm9hcmQ7XG59KTsiLCIvKipcbiAqIEEgcG9seWZpbGwgZm9yIEVsZW1lbnQubWF0Y2hlcygpXG4gKi9cbmlmIChFbGVtZW50ICYmICFFbGVtZW50LnByb3RvdHlwZS5tYXRjaGVzKSB7XG4gICAgdmFyIHByb3RvID0gRWxlbWVudC5wcm90b3R5cGU7XG5cbiAgICBwcm90by5tYXRjaGVzID0gcHJvdG8ubWF0Y2hlc1NlbGVjdG9yIHx8XG4gICAgICAgICAgICAgICAgICAgIHByb3RvLm1vek1hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by5tc01hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by5vTWF0Y2hlc1NlbGVjdG9yIHx8XG4gICAgICAgICAgICAgICAgICAgIHByb3RvLndlYmtpdE1hdGNoZXNTZWxlY3Rvcjtcbn1cblxuLyoqXG4gKiBGaW5kcyB0aGUgY2xvc2VzdCBwYXJlbnQgdGhhdCBtYXRjaGVzIGEgc2VsZWN0b3IuXG4gKlxuICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEByZXR1cm4ge0Z1bmN0aW9ufVxuICovXG5mdW5jdGlvbiBjbG9zZXN0IChlbGVtZW50LCBzZWxlY3Rvcikge1xuICAgIHdoaWxlIChlbGVtZW50ICYmIGVsZW1lbnQgIT09IGRvY3VtZW50KSB7XG4gICAgICAgIGlmIChlbGVtZW50Lm1hdGNoZXMoc2VsZWN0b3IpKSByZXR1cm4gZWxlbWVudDtcbiAgICAgICAgZWxlbWVudCA9IGVsZW1lbnQucGFyZW50Tm9kZTtcbiAgICB9XG59XG5cbm1vZHVsZS5leHBvcnRzID0gY2xvc2VzdDtcbiIsInZhciBjbG9zZXN0ID0gcmVxdWlyZSgnLi9jbG9zZXN0Jyk7XG5cbi8qKlxuICogRGVsZWdhdGVzIGV2ZW50IHRvIGEgc2VsZWN0b3IuXG4gKlxuICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHBhcmFtIHtCb29sZWFufSB1c2VDYXB0dXJlXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGRlbGVnYXRlKGVsZW1lbnQsIHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaywgdXNlQ2FwdHVyZSkge1xuICAgIHZhciBsaXN0ZW5lckZuID0gbGlzdGVuZXIuYXBwbHkodGhpcywgYXJndW1lbnRzKTtcblxuICAgIGVsZW1lbnQuYWRkRXZlbnRMaXN0ZW5lcih0eXBlLCBsaXN0ZW5lckZuLCB1c2VDYXB0dXJlKTtcblxuICAgIHJldHVybiB7XG4gICAgICAgIGRlc3Ryb3k6IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgZWxlbWVudC5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGxpc3RlbmVyRm4sIHVzZUNhcHR1cmUpO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEZpbmRzIGNsb3Nlc3QgbWF0Y2ggYW5kIGludm9rZXMgY2FsbGJhY2suXG4gKlxuICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7RnVuY3Rpb259XG4gKi9cbmZ1bmN0aW9uIGxpc3RlbmVyKGVsZW1lbnQsIHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaykge1xuICAgIHJldHVybiBmdW5jdGlvbihlKSB7XG4gICAgICAgIGUuZGVsZWdhdGVUYXJnZXQgPSBjbG9zZXN0KGUudGFyZ2V0LCBzZWxlY3Rvcik7XG5cbiAgICAgICAgaWYgKGUuZGVsZWdhdGVUYXJnZXQpIHtcbiAgICAgICAgICAgIGNhbGxiYWNrLmNhbGwoZWxlbWVudCwgZSk7XG4gICAgICAgIH1cbiAgICB9XG59XG5cbm1vZHVsZS5leHBvcnRzID0gZGVsZWdhdGU7XG4iLCIvKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgSFRNTCBlbGVtZW50LlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5ub2RlID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICByZXR1cm4gdmFsdWUgIT09IHVuZGVmaW5lZFxuICAgICAgICAmJiB2YWx1ZSBpbnN0YW5jZW9mIEhUTUxFbGVtZW50XG4gICAgICAgICYmIHZhbHVlLm5vZGVUeXBlID09PSAxO1xufTtcblxuLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIGxpc3Qgb2YgSFRNTCBlbGVtZW50cy5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gdmFsdWVcbiAqIEByZXR1cm4ge0Jvb2xlYW59XG4gKi9cbmV4cG9ydHMubm9kZUxpc3QgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHZhciB0eXBlID0gT2JqZWN0LnByb3RvdHlwZS50b1N0cmluZy5jYWxsKHZhbHVlKTtcblxuICAgIHJldHVybiB2YWx1ZSAhPT0gdW5kZWZpbmVkXG4gICAgICAgICYmICh0eXBlID09PSAnW29iamVjdCBOb2RlTGlzdF0nIHx8IHR5cGUgPT09ICdbb2JqZWN0IEhUTUxDb2xsZWN0aW9uXScpXG4gICAgICAgICYmICgnbGVuZ3RoJyBpbiB2YWx1ZSlcbiAgICAgICAgJiYgKHZhbHVlLmxlbmd0aCA9PT0gMCB8fCBleHBvcnRzLm5vZGUodmFsdWVbMF0pKTtcbn07XG5cbi8qKlxuICogQ2hlY2sgaWYgYXJndW1lbnQgaXMgYSBzdHJpbmcuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLnN0cmluZyA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgcmV0dXJuIHR5cGVvZiB2YWx1ZSA9PT0gJ3N0cmluZydcbiAgICAgICAgfHwgdmFsdWUgaW5zdGFuY2VvZiBTdHJpbmc7XG59O1xuXG4vKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLmZuID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICB2YXIgdHlwZSA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbCh2YWx1ZSk7XG5cbiAgICByZXR1cm4gdHlwZSA9PT0gJ1tvYmplY3QgRnVuY3Rpb25dJztcbn07XG4iLCJ2YXIgaXMgPSByZXF1aXJlKCcuL2lzJyk7XG52YXIgZGVsZWdhdGUgPSByZXF1aXJlKCdkZWxlZ2F0ZScpO1xuXG4vKipcbiAqIFZhbGlkYXRlcyBhbGwgcGFyYW1zIGFuZCBjYWxscyB0aGUgcmlnaHRcbiAqIGxpc3RlbmVyIGZ1bmN0aW9uIGJhc2VkIG9uIGl0cyB0YXJnZXQgdHlwZS5cbiAqXG4gKiBAcGFyYW0ge1N0cmluZ3xIVE1MRWxlbWVudHxIVE1MQ29sbGVjdGlvbnxOb2RlTGlzdH0gdGFyZ2V0XG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuKHRhcmdldCwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBpZiAoIXRhcmdldCAmJiAhdHlwZSAmJiAhY2FsbGJhY2spIHtcbiAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdNaXNzaW5nIHJlcXVpcmVkIGFyZ3VtZW50cycpO1xuICAgIH1cblxuICAgIGlmICghaXMuc3RyaW5nKHR5cGUpKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ1NlY29uZCBhcmd1bWVudCBtdXN0IGJlIGEgU3RyaW5nJyk7XG4gICAgfVxuXG4gICAgaWYgKCFpcy5mbihjYWxsYmFjaykpIHtcbiAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcignVGhpcmQgYXJndW1lbnQgbXVzdCBiZSBhIEZ1bmN0aW9uJyk7XG4gICAgfVxuXG4gICAgaWYgKGlzLm5vZGUodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuTm9kZSh0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKTtcbiAgICB9XG4gICAgZWxzZSBpZiAoaXMubm9kZUxpc3QodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuTm9kZUxpc3QodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2UgaWYgKGlzLnN0cmluZyh0YXJnZXQpKSB7XG4gICAgICAgIHJldHVybiBsaXN0ZW5TZWxlY3Rvcih0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKTtcbiAgICB9XG4gICAgZWxzZSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ0ZpcnN0IGFyZ3VtZW50IG11c3QgYmUgYSBTdHJpbmcsIEhUTUxFbGVtZW50LCBIVE1MQ29sbGVjdGlvbiwgb3IgTm9kZUxpc3QnKTtcbiAgICB9XG59XG5cbi8qKlxuICogQWRkcyBhbiBldmVudCBsaXN0ZW5lciB0byBhIEhUTUwgZWxlbWVudFxuICogYW5kIHJldHVybnMgYSByZW1vdmUgbGlzdGVuZXIgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtIVE1MRWxlbWVudH0gbm9kZVxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3Rlbk5vZGUobm9kZSwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBub2RlLmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuXG4gICAgcmV0dXJuIHtcbiAgICAgICAgZGVzdHJveTogZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZCBhbiBldmVudCBsaXN0ZW5lciB0byBhIGxpc3Qgb2YgSFRNTCBlbGVtZW50c1xuICogYW5kIHJldHVybnMgYSByZW1vdmUgbGlzdGVuZXIgZnVuY3Rpb24uXG4gKlxuICogQHBhcmFtIHtOb2RlTGlzdHxIVE1MQ29sbGVjdGlvbn0gbm9kZUxpc3RcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5Ob2RlTGlzdChub2RlTGlzdCwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICBBcnJheS5wcm90b3R5cGUuZm9yRWFjaC5jYWxsKG5vZGVMaXN0LCBmdW5jdGlvbihub2RlKSB7XG4gICAgICAgIG5vZGUuYWRkRXZlbnRMaXN0ZW5lcih0eXBlLCBjYWxsYmFjayk7XG4gICAgfSk7XG5cbiAgICByZXR1cm4ge1xuICAgICAgICBkZXN0cm95OiBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIEFycmF5LnByb3RvdHlwZS5mb3JFYWNoLmNhbGwobm9kZUxpc3QsIGZ1bmN0aW9uKG5vZGUpIHtcbiAgICAgICAgICAgICAgICBub2RlLnJlbW92ZUV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgICAgICAgICAgfSk7XG4gICAgICAgIH1cbiAgICB9XG59XG5cbi8qKlxuICogQWRkIGFuIGV2ZW50IGxpc3RlbmVyIHRvIGEgc2VsZWN0b3JcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7U3RyaW5nfSBzZWxlY3RvclxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3RlblNlbGVjdG9yKHNlbGVjdG9yLCB0eXBlLCBjYWxsYmFjaykge1xuICAgIHJldHVybiBkZWxlZ2F0ZShkb2N1bWVudC5ib2R5LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spO1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGxpc3RlbjtcbiIsIi8qISBuZ2NsaXBib2FyZCAtIHYxLjEuMSAtIDIwMTYtMDItMjZcclxuKiBodHRwczovL2dpdGh1Yi5jb20vc2FjaGluY2hvb2x1ci9uZ2NsaXBib2FyZFxyXG4qIENvcHlyaWdodCAoYykgMjAxNiBTYWNoaW47IExpY2Vuc2VkIE1JVCAqL1xyXG4oZnVuY3Rpb24oKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICB2YXIgTU9EVUxFX05BTUUgPSAnbmdjbGlwYm9hcmQnO1xyXG4gICAgdmFyIGFuZ3VsYXIsIENsaXBib2FyZDtcclxuICAgIFxyXG4gICAgLy8gQ2hlY2sgZm9yIENvbW1vbkpTIHN1cHBvcnRcclxuICAgIGlmICh0eXBlb2YgbW9kdWxlID09PSAnb2JqZWN0JyAmJiBtb2R1bGUuZXhwb3J0cykge1xyXG4gICAgICBhbmd1bGFyID0gcmVxdWlyZSgnYW5ndWxhcicpO1xyXG4gICAgICBDbGlwYm9hcmQgPSByZXF1aXJlKCdjbGlwYm9hcmQnKTtcclxuICAgICAgbW9kdWxlLmV4cG9ydHMgPSBNT0RVTEVfTkFNRTtcclxuICAgIH0gZWxzZSB7XHJcbiAgICAgIGFuZ3VsYXIgPSB3aW5kb3cuYW5ndWxhcjtcclxuICAgICAgQ2xpcGJvYXJkID0gd2luZG93LkNsaXBib2FyZDtcclxuICAgIH1cclxuXHJcbiAgICBhbmd1bGFyLm1vZHVsZShNT0RVTEVfTkFNRSwgW10pLmRpcmVjdGl2ZSgnbmdjbGlwYm9hcmQnLCBmdW5jdGlvbigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICByZXN0cmljdDogJ0EnLFxyXG4gICAgICAgICAgICBzY29wZToge1xyXG4gICAgICAgICAgICAgICAgbmdjbGlwYm9hcmRTdWNjZXNzOiAnJicsXHJcbiAgICAgICAgICAgICAgICBuZ2NsaXBib2FyZEVycm9yOiAnJidcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgbGluazogZnVuY3Rpb24oc2NvcGUsIGVsZW1lbnQpIHtcclxuICAgICAgICAgICAgICAgIHZhciBjbGlwYm9hcmQgPSBuZXcgQ2xpcGJvYXJkKGVsZW1lbnRbMF0pO1xyXG5cclxuICAgICAgICAgICAgICAgIGNsaXBib2FyZC5vbignc3VjY2VzcycsIGZ1bmN0aW9uKGUpIHtcclxuICAgICAgICAgICAgICAgICAgc2NvcGUuJGFwcGx5KGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICBzY29wZS5uZ2NsaXBib2FyZFN1Y2Nlc3Moe1xyXG4gICAgICAgICAgICAgICAgICAgICAgZTogZVxyXG4gICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgICAgIGNsaXBib2FyZC5vbignZXJyb3InLCBmdW5jdGlvbihlKSB7XHJcbiAgICAgICAgICAgICAgICAgIHNjb3BlLiRhcHBseShmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2NvcGUubmdjbGlwYm9hcmRFcnJvcih7XHJcbiAgICAgICAgICAgICAgICAgICAgICBlOiBlXHJcbiAgICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH0pO1xyXG59KCkpO1xyXG4iLCJmdW5jdGlvbiBzZWxlY3QoZWxlbWVudCkge1xuICAgIHZhciBzZWxlY3RlZFRleHQ7XG5cbiAgICBpZiAoZWxlbWVudC5ub2RlTmFtZSA9PT0gJ1NFTEVDVCcpIHtcbiAgICAgICAgZWxlbWVudC5mb2N1cygpO1xuXG4gICAgICAgIHNlbGVjdGVkVGV4dCA9IGVsZW1lbnQudmFsdWU7XG4gICAgfVxuICAgIGVsc2UgaWYgKGVsZW1lbnQubm9kZU5hbWUgPT09ICdJTlBVVCcgfHwgZWxlbWVudC5ub2RlTmFtZSA9PT0gJ1RFWFRBUkVBJykge1xuICAgICAgICBlbGVtZW50LmZvY3VzKCk7XG4gICAgICAgIGVsZW1lbnQuc2V0U2VsZWN0aW9uUmFuZ2UoMCwgZWxlbWVudC52YWx1ZS5sZW5ndGgpO1xuXG4gICAgICAgIHNlbGVjdGVkVGV4dCA9IGVsZW1lbnQudmFsdWU7XG4gICAgfVxuICAgIGVsc2Uge1xuICAgICAgICBpZiAoZWxlbWVudC5oYXNBdHRyaWJ1dGUoJ2NvbnRlbnRlZGl0YWJsZScpKSB7XG4gICAgICAgICAgICBlbGVtZW50LmZvY3VzKCk7XG4gICAgICAgIH1cblxuICAgICAgICB2YXIgc2VsZWN0aW9uID0gd2luZG93LmdldFNlbGVjdGlvbigpO1xuICAgICAgICB2YXIgcmFuZ2UgPSBkb2N1bWVudC5jcmVhdGVSYW5nZSgpO1xuXG4gICAgICAgIHJhbmdlLnNlbGVjdE5vZGVDb250ZW50cyhlbGVtZW50KTtcbiAgICAgICAgc2VsZWN0aW9uLnJlbW92ZUFsbFJhbmdlcygpO1xuICAgICAgICBzZWxlY3Rpb24uYWRkUmFuZ2UocmFuZ2UpO1xuXG4gICAgICAgIHNlbGVjdGVkVGV4dCA9IHNlbGVjdGlvbi50b1N0cmluZygpO1xuICAgIH1cblxuICAgIHJldHVybiBzZWxlY3RlZFRleHQ7XG59XG5cbm1vZHVsZS5leHBvcnRzID0gc2VsZWN0O1xuIiwiZnVuY3Rpb24gRSAoKSB7XG4gIC8vIEtlZXAgdGhpcyBlbXB0eSBzbyBpdCdzIGVhc2llciB0byBpbmhlcml0IGZyb21cbiAgLy8gKHZpYSBodHRwczovL2dpdGh1Yi5jb20vbGlwc21hY2sgZnJvbSBodHRwczovL2dpdGh1Yi5jb20vc2NvdHRjb3JnYW4vdGlueS1lbWl0dGVyL2lzc3Vlcy8zKVxufVxuXG5FLnByb3RvdHlwZSA9IHtcbiAgb246IGZ1bmN0aW9uIChuYW1lLCBjYWxsYmFjaywgY3R4KSB7XG4gICAgdmFyIGUgPSB0aGlzLmUgfHwgKHRoaXMuZSA9IHt9KTtcblxuICAgIChlW25hbWVdIHx8IChlW25hbWVdID0gW10pKS5wdXNoKHtcbiAgICAgIGZuOiBjYWxsYmFjayxcbiAgICAgIGN0eDogY3R4XG4gICAgfSk7XG5cbiAgICByZXR1cm4gdGhpcztcbiAgfSxcblxuICBvbmNlOiBmdW5jdGlvbiAobmFtZSwgY2FsbGJhY2ssIGN0eCkge1xuICAgIHZhciBzZWxmID0gdGhpcztcbiAgICBmdW5jdGlvbiBsaXN0ZW5lciAoKSB7XG4gICAgICBzZWxmLm9mZihuYW1lLCBsaXN0ZW5lcik7XG4gICAgICBjYWxsYmFjay5hcHBseShjdHgsIGFyZ3VtZW50cyk7XG4gICAgfTtcblxuICAgIGxpc3RlbmVyLl8gPSBjYWxsYmFja1xuICAgIHJldHVybiB0aGlzLm9uKG5hbWUsIGxpc3RlbmVyLCBjdHgpO1xuICB9LFxuXG4gIGVtaXQ6IGZ1bmN0aW9uIChuYW1lKSB7XG4gICAgdmFyIGRhdGEgPSBbXS5zbGljZS5jYWxsKGFyZ3VtZW50cywgMSk7XG4gICAgdmFyIGV2dEFyciA9ICgodGhpcy5lIHx8ICh0aGlzLmUgPSB7fSkpW25hbWVdIHx8IFtdKS5zbGljZSgpO1xuICAgIHZhciBpID0gMDtcbiAgICB2YXIgbGVuID0gZXZ0QXJyLmxlbmd0aDtcblxuICAgIGZvciAoaTsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICBldnRBcnJbaV0uZm4uYXBwbHkoZXZ0QXJyW2ldLmN0eCwgZGF0YSk7XG4gICAgfVxuXG4gICAgcmV0dXJuIHRoaXM7XG4gIH0sXG5cbiAgb2ZmOiBmdW5jdGlvbiAobmFtZSwgY2FsbGJhY2spIHtcbiAgICB2YXIgZSA9IHRoaXMuZSB8fCAodGhpcy5lID0ge30pO1xuICAgIHZhciBldnRzID0gZVtuYW1lXTtcbiAgICB2YXIgbGl2ZUV2ZW50cyA9IFtdO1xuXG4gICAgaWYgKGV2dHMgJiYgY2FsbGJhY2spIHtcbiAgICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBldnRzLmxlbmd0aDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgICAgIGlmIChldnRzW2ldLmZuICE9PSBjYWxsYmFjayAmJiBldnRzW2ldLmZuLl8gIT09IGNhbGxiYWNrKVxuICAgICAgICAgIGxpdmVFdmVudHMucHVzaChldnRzW2ldKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICAvLyBSZW1vdmUgZXZlbnQgZnJvbSBxdWV1ZSB0byBwcmV2ZW50IG1lbW9yeSBsZWFrXG4gICAgLy8gU3VnZ2VzdGVkIGJ5IGh0dHBzOi8vZ2l0aHViLmNvbS9sYXpkXG4gICAgLy8gUmVmOiBodHRwczovL2dpdGh1Yi5jb20vc2NvdHRjb3JnYW4vdGlueS1lbWl0dGVyL2NvbW1pdC9jNmViZmFhOWJjOTczYjMzZDExMGE4NGEzMDc3NDJiN2NmOTRjOTUzI2NvbW1pdGNvbW1lbnQtNTAyNDkxMFxuXG4gICAgKGxpdmVFdmVudHMubGVuZ3RoKVxuICAgICAgPyBlW25hbWVdID0gbGl2ZUV2ZW50c1xuICAgICAgOiBkZWxldGUgZVtuYW1lXTtcblxuICAgIHJldHVybiB0aGlzO1xuICB9XG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IEU7XG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzIwLzIwMTUuXHJcbiAqIFREU00gaXMgYSBnbG9iYWwgb2JqZWN0IHRoYXQgY29tZXMgZnJvbSBBcHAuanNcclxuICpcclxuICogVGhlIGZvbGxvd2luZyBoZWxwZXIgd29ya3MgaW4gYSB3YXkgdG8gbWFrZSBhdmFpbGFibGUgdGhlIGNyZWF0aW9uIG9mIERpcmVjdGl2ZSwgU2VydmljZXMgYW5kIENvbnRyb2xsZXJcclxuICogb24gZmx5IG9yIHdoZW4gZGVwbG95aW5nIHRoZSBhcHAuXHJcbiAqXHJcbiAqIFdlIHJlZHVjZSB0aGUgdXNlIG9mIGNvbXBpbGUgYW5kIGV4dHJhIHN0ZXBzXHJcbiAqL1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi9BcHAuanMnKTtcclxuXHJcbi8qKlxyXG4gKiBMaXN0ZW4gdG8gYW4gZXhpc3RpbmcgZGlnZXN0IG9mIHRoZSBjb21waWxlIHByb3ZpZGVyIGFuZCBleGVjdXRlIHRoZSAkYXBwbHkgaW1tZWRpYXRlbHkgb3IgYWZ0ZXIgaXQncyByZWFkeVxyXG4gKiBAcGFyYW0gY3VycmVudFxyXG4gKiBAcGFyYW0gZm5cclxuICovXHJcblREU1RNLnNhZmVBcHBseSA9IGZ1bmN0aW9uIChjdXJyZW50LCBmbikge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgdmFyIHBoYXNlID0gY3VycmVudC4kcm9vdC4kJHBoYXNlO1xyXG4gICAgaWYgKHBoYXNlID09PSAnJGFwcGx5JyB8fCBwaGFzZSA9PT0gJyRkaWdlc3QnKSB7XHJcbiAgICAgICAgaWYgKGZuKSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGV2YWwoZm4pO1xyXG4gICAgICAgIH1cclxuICAgIH0gZWxzZSB7XHJcbiAgICAgICAgaWYgKGZuKSB7XHJcbiAgICAgICAgICAgIGN1cnJlbnQuJGFwcGx5KGZuKTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRhcHBseSgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IGRpcmVjdGl2ZSBhc3luYyBpZiB0aGUgY29tcGlsZVByb3ZpZGVyIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyKSB7XHJcbiAgICAgICAgVERTVE0uUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlci5kaXJlY3RpdmUoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmRpcmVjdGl2ZSkge1xyXG4gICAgICAgIFREU1RNLmRpcmVjdGl2ZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IGNvbnRyb2xsZXJzIGFzeW5jIGlmIHRoZSBjb250cm9sbGVyUHJvdmlkZXIgaXMgYXZhaWxhYmxlXHJcbiAqIEBwYXJhbSBzZXR0aW5nXHJcbiAqIEBwYXJhbSBhcmdzXHJcbiAqL1xyXG5URFNUTS5jcmVhdGVDb250cm9sbGVyID0gZnVuY3Rpb24gKHNldHRpbmcsIGFyZ3MpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIGlmIChURFNUTS5Qcm92aWRlckNvcmUuY29udHJvbGxlclByb3ZpZGVyKSB7XHJcbiAgICAgICAgVERTVE0uY29udHJvbGxlclByb3ZpZGVyLnJlZ2lzdGVyKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5jb250cm9sbGVyKSB7XHJcbiAgICAgICAgVERTVE0uY29udHJvbGxlcihzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBIZWxwZXIgdG8gaW5qZWN0IHNlcnZpY2UgYXN5bmMgaWYgdGhlIHByb3ZpZGVTZXJ2aWNlIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlU2VydmljZSA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlKSB7XHJcbiAgICAgICAgVERTVE0uUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlLnNlcnZpY2Uoc2V0dGluZywgYXJncyk7XHJcbiAgICB9IGVsc2UgaWYgKFREU1RNLmNvbnRyb2xsZXIpIHtcclxuICAgICAgICBURFNUTS5zZXJ2aWNlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfVxyXG59O1xyXG5cclxuLyoqXHJcbiAqIEZvciBMZWdhY3kgc3lzdGVtLCB3aGF0IGlzIGRvZXMgaXMgdG8gdGFrZSBwYXJhbXMgZnJvbSB0aGUgcXVlcnlcclxuICogb3V0c2lkZSB0aGUgQW5ndWxhckpTIHVpLXJvdXRpbmcuXHJcbiAqIEBwYXJhbSBwYXJhbSAvLyBQYXJhbSB0byBzZWFyYyBmb3IgL2V4YW1wbGUuaHRtbD9iYXI9Zm9vI2N1cnJlbnRTdGF0ZVxyXG4gKi9cclxuVERTVE0uZ2V0VVJMUGFyYW0gPSBmdW5jdGlvbiAocGFyYW0pIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgICQudXJsUGFyYW0gPSBmdW5jdGlvbiAobmFtZSkge1xyXG4gICAgICAgIHZhciByZXN1bHRzID0gbmV3IFJlZ0V4cCgnW1xcPyZdJyArIG5hbWUgKyAnPShbXiYjXSopJykuZXhlYyh3aW5kb3cubG9jYXRpb24uaHJlZik7XHJcbiAgICAgICAgaWYgKHJlc3VsdHMgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgcmV0dXJuIG51bGw7XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGVsc2Uge1xyXG4gICAgICAgICAgICByZXR1cm4gcmVzdWx0c1sxXSB8fCAwO1xyXG4gICAgICAgIH1cclxuICAgIH07XHJcblxyXG4gICAgcmV0dXJuICQudXJsUGFyYW0ocGFyYW0pO1xyXG59O1xyXG5cclxuLyoqXHJcbiAqIFRoaXMgY29kZSB3YXMgaW50cm9kdWNlZCBvbmx5IGZvciB0aGUgaWZyYW1lIG1pZ3JhdGlvblxyXG4gKiBpdCBkZXRlY3Qgd2hlbiBtb3VzZSBlbnRlclxyXG4gKi9cclxuVERTVE0uaWZyYW1lTG9hZGVyID0gZnVuY3Rpb24gKCkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgJCgnLmlmcmFtZUxvYWRlcicpLmhvdmVyKFxyXG4gICAgICAgIGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgJCgnLm5hdmJhci11bC1jb250YWluZXIgLmRyb3Bkb3duLm9wZW4nKS5yZW1vdmVDbGFzcygnb3BlbicpO1xyXG4gICAgICAgIH0sIGZ1bmN0aW9uICgpIHtcclxuICAgICAgICB9XHJcbiAgICApO1xyXG59O1xyXG5cclxud2luZG93LlREU1RNID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8xNi8yMDE1LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbnJlcXVpcmUoJ2FuZ3VsYXInKTtcclxucmVxdWlyZSgnYW5ndWxhci1hbmltYXRlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItbW9ja3MnKTtcclxucmVxdWlyZSgnYW5ndWxhci1zYW5pdGl6ZScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXJlc291cmNlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdHJhbnNsYXRlJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdHJhbnNsYXRlLWxvYWRlci1wYXJ0aWFsJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItdWktYm9vdHN0cmFwJyk7XHJcbnJlcXVpcmUoJ25nQ2xpcGJvYXJkJyk7XHJcbnJlcXVpcmUoJ3VpLXJvdXRlcicpO1xyXG5yZXF1aXJlKCdyeC1hbmd1bGFyJyk7XHJcbnJlcXVpcmUoJ2FwaS1jaGVjaycpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWZvcm1seScpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWZvcm1seS10ZW1wbGF0ZXMtYm9vdHN0cmFwJyk7XHJcblxyXG4vLyBNb2R1bGVzXHJcbmltcG9ydCBIVFRQTW9kdWxlIGZyb20gJy4uL3NlcnZpY2VzL2h0dHAvSFRUUE1vZHVsZS5qcyc7XHJcbmltcG9ydCBSZXN0QVBJTW9kdWxlIGZyb20gJy4uL3NlcnZpY2VzL1Jlc3RBUEkvUmVzdEFQSU1vZHVsZS5qcydcclxuaW1wb3J0IEhlYWRlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2hlYWRlci9IZWFkZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTGljZW5zZUFkbWluTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZUFkbWluL0xpY2Vuc2VBZG1pbk1vZHVsZS5qcyc7XHJcbmltcG9ydCBMaWNlbnNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL0xpY2Vuc2VNYW5hZ2VyTW9kdWxlLmpzJztcclxuaW1wb3J0IE5vdGljZU1hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL05vdGljZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgVGFza01hbmFnZXJNb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy90YXNrTWFuYWdlci9UYXNrTWFuYWdlck1vZHVsZS5qcyc7XHJcblxyXG52YXIgUHJvdmlkZXJDb3JlID0ge307XHJcblxyXG52YXIgVERTVE0gPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0nLCBbXHJcbiAgICAnbmdTYW5pdGl6ZScsXHJcbiAgICAnbmdSZXNvdXJjZScsXHJcbiAgICAnbmdBbmltYXRlJyxcclxuICAgICdwYXNjYWxwcmVjaHQudHJhbnNsYXRlJywgLy8gJ2FuZ3VsYXItdHJhbnNsYXRlJ1xyXG4gICAgJ3VpLnJvdXRlcicsXHJcbiAgICAnbmdjbGlwYm9hcmQnLFxyXG4gICAgJ2tlbmRvLmRpcmVjdGl2ZXMnLFxyXG4gICAgJ3J4JyxcclxuICAgICdmb3JtbHknLFxyXG4gICAgJ2Zvcm1seUJvb3RzdHJhcCcsXHJcbiAgICAndWkuYm9vdHN0cmFwJyxcclxuICAgIEhUVFBNb2R1bGUubmFtZSxcclxuICAgIFJlc3RBUElNb2R1bGUubmFtZSxcclxuICAgIEhlYWRlck1vZHVsZS5uYW1lLFxyXG4gICAgVGFza01hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIExpY2Vuc2VBZG1pbk1vZHVsZS5uYW1lLFxyXG4gICAgTGljZW5zZU1hbmFnZXJNb2R1bGUubmFtZSxcclxuICAgIE5vdGljZU1hbmFnZXJNb2R1bGUubmFtZVxyXG5dKS5jb25maWcoW1xyXG4gICAgJyRsb2dQcm92aWRlcicsXHJcbiAgICAnJHJvb3RTY29wZVByb3ZpZGVyJyxcclxuICAgICckY29tcGlsZVByb3ZpZGVyJyxcclxuICAgICckY29udHJvbGxlclByb3ZpZGVyJyxcclxuICAgICckcHJvdmlkZScsXHJcbiAgICAnJGh0dHBQcm92aWRlcicsXHJcbiAgICAnJHRyYW5zbGF0ZVByb3ZpZGVyJyxcclxuICAgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgICckdXJsUm91dGVyUHJvdmlkZXInLFxyXG4gICAgJyRsb2NhdGlvblByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkbG9nUHJvdmlkZXIsICRyb290U2NvcGVQcm92aWRlciwgJGNvbXBpbGVQcm92aWRlciwgJGNvbnRyb2xsZXJQcm92aWRlciwgJHByb3ZpZGUsICRodHRwUHJvdmlkZXIsXHJcbiAgICAgICAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLCAkdXJsUm91dGVyUHJvdmlkZXIsICRsb2NhdGlvblByb3ZpZGVyKSB7XHJcblxyXG4gICAgICAgICRyb290U2NvcGVQcm92aWRlci5kaWdlc3RUdGwoMzApO1xyXG4gICAgICAgIC8vIEdvaW5nIGJhY2sgdG8geW91XHJcbiAgICAgICAgJGxvY2F0aW9uUHJvdmlkZXIuaHRtbDVNb2RlKHRydWUpLmhhc2hQcmVmaXgoJyEnKTtcclxuXHJcbiAgICAgICAgJGxvZ1Byb3ZpZGVyLmRlYnVnRW5hYmxlZCh0cnVlKTtcclxuXHJcbiAgICAgICAgLy8gQWZ0ZXIgYm9vdHN0cmFwcGluZyBhbmd1bGFyIGZvcmdldCB0aGUgcHJvdmlkZXIgc2luY2UgZXZlcnl0aGluZyBcIndhcyBhbHJlYWR5IGxvYWRlZFwiXHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlciA9ICRjb21waWxlUHJvdmlkZXI7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmNvbnRyb2xsZXJQcm92aWRlciA9ICRjb250cm9sbGVyUHJvdmlkZXI7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLnByb3ZpZGVTZXJ2aWNlID0gJHByb3ZpZGU7XHJcbiAgICAgICAgUHJvdmlkZXJDb3JlLmh0dHBQcm92aWRlciA9ICRodHRwUHJvdmlkZXI7XHJcblxyXG4gICAgICAgIC8qKlxyXG4gICAgICAgICAqIFRyYW5zbGF0aW9uc1xyXG4gICAgICAgICAqL1xyXG5cclxuICAgICAgICAvKiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLnVzZVNhbml0aXplVmFsdWVTdHJhdGVneShudWxsKTtcclxuXHJcbiAgICAgICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCd0ZHN0bScpO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIudXNlTG9hZGVyKCckdHJhbnNsYXRlUGFydGlhbExvYWRlcicsIHtcclxuICAgICAgICAgICAgdXJsVGVtcGxhdGU6ICcuLi9pMThuL3twYXJ0fS9hcHAuaTE4bi17bGFuZ30uanNvbidcclxuICAgICAgICB9KTsqL1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIucHJlZmVycmVkTGFuZ3VhZ2UoJ2VuX1VTJyk7XHJcbiAgICAgICAgJHRyYW5zbGF0ZVByb3ZpZGVyLmZhbGxiYWNrTGFuZ3VhZ2UoJ2VuX1VTJyk7XHJcblxyXG4gICAgICAgIC8vJHVybFJvdXRlclByb3ZpZGVyLm90aGVyd2lzZSgnZGFzaGJvYXJkJyk7XHJcblxyXG4gICAgfV0pLlxyXG4gICAgcnVuKFsnJHRyYW5zaXRpb25zJywgJyRodHRwJywgJyRsb2cnLCAnJGxvY2F0aW9uJywgZnVuY3Rpb24gKCR0cmFuc2l0aW9ucywgJGh0dHAsICRsb2csICRsb2NhdGlvbiwgJHN0YXRlLCAkc3RhdGVQYXJhbXMsICRsb2NhbGUpIHtcclxuICAgICAgICAkbG9nLmRlYnVnKCdDb25maWd1cmF0aW9uIGRlcGxveWVkJyk7XHJcblxyXG4gICAgICAgICR0cmFuc2l0aW9ucy5vbkJlZm9yZSgge30sIGZ1bmN0aW9uKCRzdGF0ZSwgJHRyYW5zaXRpb24kKSB7XHJcbiAgICAgICAgICAgICRsb2cubG9nKCdJbiBzdGFydCAnLCAkc3RhdGUpO1xyXG4gICAgICAgIH0pO1xyXG5cclxuICAgIH1dKTtcclxuXHJcbi8vIHdlIG1hcHBlZCB0aGUgUHJvdmlkZXIgQ29yZSBsaXN0IChjb21waWxlUHJvdmlkZXIsIGNvbnRyb2xsZXJQcm92aWRlciwgcHJvdmlkZVNlcnZpY2UsIGh0dHBQcm92aWRlcikgdG8gcmV1c2UgYWZ0ZXIgb24gZmx5XHJcblREU1RNLlByb3ZpZGVyQ29yZSA9IFByb3ZpZGVyQ29yZTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBJdCBoYW5kbGVyIHRoZSBpbmRleCBmb3IgYW55IG9mIHRoZSBkaXJlY3RpdmVzIGF2YWlsYWJsZVxyXG4gKi9cclxuXHJcbnJlcXVpcmUoJy4vdG9vbHMvVG9hc3RIYW5kbGVyLmpzJyk7XHJcbnJlcXVpcmUoJy4vdG9vbHMvTW9kYWxXaW5kb3dBY3RpdmF0aW9uLmpzJyk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMzAvMTAvMjAxNi5cclxuICogTGlzdGVuIHRvIE1vZGFsIFdpbmRvdyB0byBtYWtlIGFueSBtb2RhbCB3aW5kb3cgZHJhZ2dhYmJsZVxyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi4vLi4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlKCdtb2RhbFJlbmRlcicsIFsnJGxvZycsIGZ1bmN0aW9uICgkbG9nKSB7XHJcbiAgICAkbG9nLmRlYnVnKCdNb2RhbFdpbmRvd0FjdGl2YXRpb24gbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHJlc3RyaWN0OiAnRUEnLFxyXG4gICAgICAgIGxpbms6IGZ1bmN0aW9uKCkge1xyXG4gICAgICAgICAgICAkKCcubW9kYWwtZGlhbG9nJykuZHJhZ2dhYmxlKHtcclxuICAgICAgICAgICAgICAgIGhhbmRsZTogJy5tb2RhbC1oZWFkZXInXHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH07XHJcbn1dKTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzE0LzIwMTUuXHJcbiAqIFByaW50cyBvdXQgYWxsIFRvYXN0IG1lc3NhZ2Ugd2hlbiBkZXRlY3RlZCBmcm9tIHNlcnZlciBvciBjdXN0b20gbXNnIHVzaW5nIHRoZSBkaXJlY3RpdmUgaXRzZWxmXHJcbiAqXHJcbiAqIFByb2JhYmx5IHZhbHVlcyBhcmU6XHJcbiAqXHJcbiAqIHN1Y2Nlc3MsIGRhbmdlciwgaW5mbywgd2FybmluZ1xyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi4vLi4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlKCd0b2FzdEhhbmRsZXInLCBbJyRsb2cnLCAnJHRpbWVvdXQnLCAnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InLCAnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICAnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyxcclxuICAgIGZ1bmN0aW9uICgkbG9nLCAkdGltZW91dCwgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IsXHJcbiAgICAgICAgICAgICAgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcikge1xyXG5cclxuICAgICRsb2cuZGVidWcoJ1RvYXN0SGFuZGxlciBsb2FkZWQnKTtcclxuICAgIHJldHVybiB7XHJcbiAgICAgICAgc2NvcGU6IHtcclxuICAgICAgICAgICAgbXNnOiAnPScsXHJcbiAgICAgICAgICAgIHR5cGU6ICc9JyxcclxuICAgICAgICAgICAgc3RhdHVzOiAnPSdcclxuICAgICAgICB9LFxyXG4gICAgICAgIHByaW9yaXR5OiA1LFxyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL2RpcmVjdGl2ZXMvVG9vbHMvVG9hc3RIYW5kbGVyLmh0bWwnLFxyXG4gICAgICAgIHJlc3RyaWN0OiAnRScsXHJcbiAgICAgICAgY29udHJvbGxlcjogWyckc2NvcGUnLCAnJHJvb3RTY29wZScsIGZ1bmN0aW9uICgkc2NvcGUsICRyb290U2NvcGUpIHtcclxuICAgICAgICAgICAgJHNjb3BlLmFsZXJ0ID0ge1xyXG4gICAgICAgICAgICAgICAgc3VjY2Vzczoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBkYW5nZXI6IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgaW5mbzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICB3YXJuaW5nOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJ1xyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcyA9IHtcclxuICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlXHJcbiAgICAgICAgICAgIH07XHJcblxyXG4gICAgICAgICAgICBmdW5jdGlvbiB0dXJuT2ZmTm90aWZpY2F0aW9ucygpe1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LnN1Y2Nlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuaW5mby5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQud2FybmluZy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogTGlzdGVuIHRvIGFueSByZXF1ZXN0LCB3ZSBjYW4gcmVnaXN0ZXIgbGlzdGVuZXIgaWYgd2Ugd2FudCB0byBhZGQgZXh0cmEgY29kZS5cclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgIEhUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yLmxpc3RlblJlcXVlc3QoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKGNvbmZpZyl7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXF1ZXN0IHRvOiAnLCAgY29uZmlnKTtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKHRpbWUpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuRXJyb3IoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlamVjdGlvbil7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXF1ZXN0IGVycm9yOiAnLCAgcmVqZWN0aW9uKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmxpc3RlblJlc3BvbnNlKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZXNwb25zZSl7XHJcbiAgICAgICAgICAgICAgICB2YXIgdGltZSA9IHJlc3BvbnNlLmNvbmZpZy5yZXNwb25zZVRpbWVzdGFtcCAtIHJlc3BvbnNlLmNvbmZpZy5yZXF1ZXN0VGltZXN0YW1wO1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnVGhlIHJlcXVlc3QgdG9vayAnICsgKHRpbWUgLyAxMDAwKSArICcgc2Vjb25kcycpO1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVzcG9uc2UgcmVzdWx0OiAnLCByZXNwb25zZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmxpc3RlbkVycm9yKCkudGhlbihudWxsLCBudWxsLCBmdW5jdGlvbihyZWplY3Rpb24pe1xyXG4gICAgICAgICAgICAgICAgJGxvZy5kZWJ1ZygnUmVzcG9uc2UgZXJyb3I6ICcsIHJlamVjdGlvbik7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IGZhbHNlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc3RhdHVzID0gcmVqZWN0aW9uLnN0YXR1cztcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5kYW5nZXIuc3RhdHVzVGV4dCA9IHJlamVjdGlvbi5zdGF0dXNUZXh0O1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5lcnJvcnMgPSByZWplY3Rpb24uZGF0YS5lcnJvcnM7XHJcbiAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgMzAwMCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEhpZGUgdGhlIFBvcCB1cCBub3RpZmljYXRpb24gbWFudWFsbHlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRzY29wZS5vbkNhbmNlbFBvcFVwID0gZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgICAgICAgICB0dXJuT2ZmTm90aWZpY2F0aW9ucygpO1xyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgLyoqXHJcbiAgICAgICAgICAgICAqIEl0IHdhdGNoIHRoZSB2YWx1ZSB0byBzaG93IHRoZSBtc2cgaWYgbmVjZXNzYXJ5XHJcbiAgICAgICAgICAgICAqL1xyXG4gICAgICAgICAgICAkcm9vdFNjb3BlLiRvbignYnJvYWRjYXN0LW1zZycsIGZ1bmN0aW9uKGV2ZW50LCBhcmdzKSB7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdicm9hZGNhc3QtbXNnIGV4ZWN1dGVkJyk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zaG93ID0gdHJ1ZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnN0YXR1c1RleHQgPSBhcmdzLnRleHQ7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnRbYXJncy50eXBlXS5zdGF0dXMgPSBudWxsO1xyXG4gICAgICAgICAgICAgICAgJHRpbWVvdXQodHVybk9mZk5vdGlmaWNhdGlvbnMsIDIwMDApO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLiRhcHBseSgpOyAvLyByb290U2NvcGUgYW5kIHdhdGNoIGV4Y2x1ZGUgdGhlIGFwcGx5IGFuZCBuZWVkcyB0aGUgbmV4dCBjeWNsZSB0byBydW5cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogSXQgd2F0Y2ggdGhlIHZhbHVlIHRvIHNob3cgdGhlIG1zZyBpZiBuZWNlc3NhcnlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRzY29wZS4kd2F0Y2goJ21zZycsIGZ1bmN0aW9uKG5ld1ZhbHVlLCBvbGRWYWx1ZSkge1xyXG4gICAgICAgICAgICAgICAgaWYgKG5ld1ZhbHVlICYmIG5ld1ZhbHVlICE9PSAnJykge1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0WyRzY29wZS50eXBlXS5zdGF0dXNUZXh0ID0gbmV3VmFsdWU7XHJcbiAgICAgICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0WyRzY29wZS50eXBlXS5zdGF0dXMgPSAkc2NvcGUuc3RhdHVzO1xyXG4gICAgICAgICAgICAgICAgICAgICR0aW1lb3V0KHR1cm5PZmZOb3RpZmljYXRpb25zLCAyNTAwKTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIH1dXHJcbiAgICB9O1xyXG59XSk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMTcvMjAxNS5cclxuICovXHJcblxyXG4vLyBNYWluIEFuZ3VsYXJKcyBjb25maWd1cmF0aW9uXHJcbnJlcXVpcmUoJy4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuLy8gSGVscGVyc1xyXG5yZXF1aXJlKCcuL2NvbmZpZy9Bbmd1bGFyUHJvdmlkZXJIZWxwZXIuanMnKTtcclxuXHJcbi8vIERpcmVjdGl2ZXNcclxucmVxdWlyZSgnLi9kaXJlY3RpdmVzL2luZGV4Jyk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBEaWFsb2dBY3Rpb24ge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLnRpdGxlID0gcGFyYW1zLnRpdGxlO1xyXG4gICAgICAgIHRoaXMubWVzc2FnZSA9IHBhcmFtcy5tZXNzYWdlO1xyXG5cclxuICAgIH1cclxuICAgIC8qKlxyXG4gICAgICogQWNjY2VwdCBhbmQgQ29uZmlybVxyXG4gICAgICovXHJcbiAgICBjb25maXJtQWN0aW9uKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSgpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIvMjAxNS5cclxuICogSGVhZGVyIENvbnRyb2xsZXIgbWFuYWdlIHRoZSB2aWV3IGF2YWlsYWJsZSBvbiB0aGUgc3RhdGUuZGF0YVxyXG4gKiAtLS0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAqIEhlYWRlciBDb250cm9sbGVyXHJcbiAqIFBhZ2UgdGl0bGUgICAgICAgICAgICAgICAgICAgICAgSG9tZSAtPiBMYXlvdXQgLSBTdWIgTGF5b3V0XHJcbiAqXHJcbiAqIE1vZHVsZSBDb250cm9sbGVyXHJcbiAqIENvbnRlbnRcclxuICogLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhlYWRlckNvbnRyb2xsZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZ1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcblxyXG4gICAgICAgIHRoaXMucGFnZU1ldGFEYXRhID0ge1xyXG4gICAgICAgICAgICB0aXRsZTogJycsXHJcbiAgICAgICAgICAgIGluc3RydWN0aW9uOiAnJyxcclxuICAgICAgICAgICAgbWVudTogW11cclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVIZWFkZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSGVhZGVyIENvbnRyb2xsZXIgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWZXJpZnkgaWYgd2UgaGF2ZSBhIG1lbnUgdG8gc2hvdyB0byBtYWRlIGl0IGF2YWlsYWJsZSB0byB0aGUgVmlld1xyXG4gICAgICovXHJcbiAgICBwcmVwYXJlSGVhZGVyKCkge1xyXG4gICAgICAgIGlmICh0aGlzLnN0YXRlICYmIHRoaXMuc3RhdGUuJGN1cnJlbnQgJiYgdGhpcy5zdGF0ZS4kY3VycmVudC5kYXRhKSB7XHJcbiAgICAgICAgICAgIHRoaXMucGFnZU1ldGFEYXRhID0gdGhpcy5zdGF0ZS4kY3VycmVudC5kYXRhLnBhZ2U7XHJcbiAgICAgICAgICAgIGRvY3VtZW50LnRpdGxlID0gdGhpcy5wYWdlTWV0YURhdGEudGl0bGU7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjEvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBIZWFkZXJDb250cm9sbGVyIGZyb20gJy4vSGVhZGVyQ29udHJvbGxlci5qcyc7XHJcbmltcG9ydCBEaWFsb2dBY3Rpb24gZnJvbSAnLi4vZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5qcyc7XHJcblxyXG52YXIgSGVhZGVyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkhlYWRlck1vZHVsZScsIFtdKTtcclxuXHJcbkhlYWRlck1vZHVsZS5jb250cm9sbGVyKCdIZWFkZXJDb250cm9sbGVyJywgWyckbG9nJywgJyRzdGF0ZScsIEhlYWRlckNvbnRyb2xsZXJdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuSGVhZGVyTW9kdWxlLmNvbnRyb2xsZXIoJ0RpYWxvZ0FjdGlvbicsIFsnJGxvZycsJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBEaWFsb2dBY3Rpb25dKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IEhlYWRlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBMaWNlbnNlQWRtaW5MaXN0IGZyb20gJy4vbGlzdC9MaWNlbnNlQWRtaW5MaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VBZG1pblNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL0xpY2Vuc2VBZG1pblNlcnZpY2UuanMnO1xyXG5pbXBvcnQgUmVxdWVzdExpY2Vuc2UgZnJvbSAnLi9yZXF1ZXN0L1JlcXVlc3RMaWNlbnNlLmpzJztcclxuaW1wb3J0IENyZWF0ZWRMaWNlbnNlIGZyb20gJy4vY3JlYXRlZC9DcmVhdGVkTGljZW5zZS5qcyc7XHJcbmltcG9ydCBBcHBseUxpY2Vuc2VLZXkgZnJvbSAnLi9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5LmpzJztcclxuaW1wb3J0IE1hbnVhbGx5UmVxdWVzdCBmcm9tICcuL21hbnVhbGx5UmVxdWVzdC9NYW51YWxseVJlcXVlc3QuanMnO1xyXG5pbXBvcnQgTGljZW5zZURldGFpbCBmcm9tICcuL2RldGFpbC9MaWNlbnNlRGV0YWlsLmpzJztcclxuXHJcblxyXG52YXIgTGljZW5zZUFkbWluTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkxpY2Vuc2VBZG1pbk1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJywgJyRsb2NhdGlvblByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlciwgJGxvY2F0aW9uUHJvdmlkZXIpIHtcclxuXHJcbiAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ2xpY2Vuc2VBZG1pbicpO1xyXG5cclxuICAgIC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcbiAgICB2YXIgaGVhZGVyID0ge1xyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlclZpZXcuaHRtbCcsXHJcbiAgICAgICAgY29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG4gICAgfTtcclxuXHJcbiAgICAkc3RhdGVQcm92aWRlclxyXG4gICAgICAgIC5zdGF0ZSgnbGljZW5zZUFkbWluTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0FkbWluaXN0ZXIgTGljZW5zZXMnLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnQWRtaW4nLCAnTGljZW5zZScsICdMaXN0J119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvYWRtaW4vbGlzdCcsXHJcbiAgICAgICAgICAgIHZpZXdzOiB7XHJcbiAgICAgICAgICAgICAgICAnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcbiAgICAgICAgICAgICAgICAnYm9keVZpZXdAJzoge1xyXG4gICAgICAgICAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2xpc3QvTGljZW5zZUFkbWluTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZUFkbWluTGlzdCBhcyBsaWNlbnNlQWRtaW5MaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsICckcm9vdFNjb3BlJywgTGljZW5zZUFkbWluU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VBZG1pbkxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTGljZW5zZUFkbWluTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignUmVxdWVzdExpY2Vuc2UnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgUmVxdWVzdExpY2Vuc2VdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0NyZWF0ZWRMaWNlbnNlJywgWyckbG9nJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIENyZWF0ZWRMaWNlbnNlXSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdBcHBseUxpY2Vuc2VLZXknLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIEFwcGx5TGljZW5zZUtleV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTWFudWFsbHlSZXF1ZXN0JywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIE1hbnVhbGx5UmVxdWVzdF0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTGljZW5zZURldGFpbCcsIFsnJGxvZycsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlRGV0YWlsXSk7XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgTGljZW5zZUFkbWluTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQXBwbHlMaWNlbnNlS2V5IGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSlcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG4gICAgICAgICAgICBrZXk6IHBhcmFtcy5saWNlbnNlLmtleVxyXG4gICAgICAgIH1cclxuICAgICAgICA7XHJcbiAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIGFuZCB2YWxpZGF0ZSB0aGUgS2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgYXBwbHlLZXkoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmFwcGx5TGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSwgKGRhdGEpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBDcmVhdGVkUmVxdWVzdExpY2Vuc2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmNsaWVudCA9IHBhcmFtcztcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VEZXRhaWwge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9JHVpYk1vZGFsO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgbWV0aG9kSWQ6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5pZCxcclxuICAgICAgICAgICAgcHJvamVjdE5hbWU6IHBhcmFtcy5saWNlbnNlLnByb2plY3QubmFtZSxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcbiAgICAgICAgICAgIGVtYWlsOiBwYXJhbXMubGljZW5zZS5lbWFpbCxcclxuICAgICAgICAgICAgc2VydmVyc1Rva2VuczogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLm1heCxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnROYW1lOiBwYXJhbXMubGljZW5zZS5lbnZpcm9ubWVudC5uYW1lLFxyXG4gICAgICAgICAgICBpbmNlcHRpb246IHBhcmFtcy5saWNlbnNlLnJlcXVlc3REYXRlLFxyXG4gICAgICAgICAgICBleHBpcmF0aW9uOiBwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSxcclxuICAgICAgICAgICAgcmVxdWVzdE5vdGU6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3ROb3RlLFxyXG4gICAgICAgICAgICBhY3RpdmU6IHBhcmFtcy5saWNlbnNlLnN0YXR1cy5pZCA9PT0gMSxcclxuICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG4gICAgICAgICAgICByZXBsYWNlZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWQsXHJcbiAgICAgICAgICAgIGVuY3J5cHRlZERldGFpbDogcGFyYW1zLmxpY2Vuc2UuZW5jcnlwdGVkRGV0YWlsLFxyXG4gICAgICAgICAgICBhcHBsaWVkOiBmYWxzZVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZU1ldGhvZE9wdGlvbnMoKTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlTWV0aG9kT3B0aW9ucygpIHtcclxuICAgICAgICB0aGlzLm1ldGhvZE9wdGlvbnMgPSBbXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAxLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ1NlcnZlcnMnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAyLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ1Rva2VucydcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAgaWQ6IDMsXHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnQ3VzdG9tJ1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgXVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhlIHVzZXIgYXBwbHkgYW5kIHNlcnZlciBzaG91bGQgdmFsaWRhdGUgdGhlIGtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZUtleSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vYXBwbHlMaWNlbnNlS2V5L0FwcGx5TGljZW5zZUtleS5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0FwcGx5TGljZW5zZUtleSBhcyBhcHBseUxpY2Vuc2VLZXknLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiB0aGlzLmxpY2Vuc2VNb2RlbCB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuYXBwbGllZCA9IGRhdGEuc3VjY2VzcztcclxuICAgICAgICAgICAgaWYoZGF0YS5zdWNjZXNzKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5hY3RpdmUgPSBkYXRhLnN1Y2Nlc3M7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoeyBpZDogdGhpcy5saWNlbnNlTW9kZWwuaWQsIHVwZGF0ZWQ6IHRydWV9KTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogT3BlbnMgYSBkaWFsb2cgYW5kIGFsbG93IHRoZSB1c2VyIHRvIG1hbnVhbGx5IHNlbmQgdGhlIHJlcXVlc3Qgb3IgY29weSB0aGUgZW5jcmlwdGVkIGNvZGVcclxuICAgICAqL1xyXG4gICAgbWFudWFsbHlSZXF1ZXN0KCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9tYW51YWxseVJlcXVlc3QvTWFudWFsbHlSZXF1ZXN0Lmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTWFudWFsbHlSZXF1ZXN0IGFzIG1hbnVhbGx5UmVxdWVzdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IHRoaXMubGljZW5zZU1vZGVsIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7fSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdCgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UucmVzdWJtaXRMaWNlbnNlUmVxdWVzdCh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHt9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIGRlbGV0ZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmRlbGV0ZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQpIHtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKCk7XHJcbiAgICAgICAgfVxyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5MaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnN0YXRlID0gJHN0YXRlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLm9wZW5MYXN0TGljZW5zZUlkID0gMDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VBZG1pbkxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgdG9vbGJhcjoga2VuZG8udGVtcGxhdGUoJzxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0IGFjdGlvbi10b29sYmFyLWJ0blwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vblJlcXVlc3ROZXdMaWNlbnNlKClcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gUmVxdWVzdCBOZXcgTGljZW5zZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5yZWxvYWRMaWNlbnNlQWRtaW5MaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMjBcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbGljZW5zZUlkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCBmaWx0ZXJhYmxlOiBmYWxzZSwgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibGljZW5zZUFkbWluTGlzdC5vbkxpY2Vuc2VEZXRhaWxzKHRoaXMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdjbGllbnQubmFtZScsIHRpdGxlOiAnQ2xpZW50J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLCB0aXRsZTogJ1Byb2plY3QnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2VtYWlsJywgdGl0bGU6ICdDb250YWN0IEVtYWlsJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMubmFtZScsIHRpdGxlOiAnU3RhdHVzJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnLCAgdGVtcGxhdGU6ICcjaWYoZGF0YS50eXBlICYmIGRhdGEudHlwZS5uYW1lID09PSBcIk1VTFRJX1BST0pFQ1RcIil7IyBHbG9iYWwgIyB9IGVsc2UgeyMgU2luZ2xlICN9Iyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLmlkJywgaGlkZGVuOiB0cnVlfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5tYXgnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3JlcXVlc3REYXRlJywgdGl0bGU6ICdJbmNlcHRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uRGF0ZScsIHRpdGxlOiAnRXhwaXJhdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Vudmlyb25tZW50Lm5hbWUnLCB0aXRsZTogJ0Vudmlyb25tZW50J31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAncHJvamVjdC5uYW1lJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdExpY2Vuc2VJZCAhPT0gMCAmJiB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdmFyIGxhc3RMaWNlbnNlID0gdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhLmZpbmQoKGxpY2Vuc2UpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBsaWNlbnNlLmlkID09PSB0aGlzLm9wZW5MYXN0TGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobGFzdExpY2Vuc2UpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlRGV0YWlscyhsYXN0TGljZW5zZSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIG9uUmVxdWVzdE5ld0xpY2Vuc2UoKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL3JlcXVlc3QvUmVxdWVzdExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0TGljZW5zZSBhcyByZXF1ZXN0TGljZW5zZScsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdOZXcgTGljZW5zZSBDcmVhdGVkOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICAgICAgdGhpcy5vbk5ld0xpY2Vuc2VDcmVhdGVkKGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZURldGFpbHMobGljZW5zZSkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ09wZW4gRGV0YWlscyBmb3I6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9kZXRhaWwvTGljZW5zZURldGFpbC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0xpY2Vuc2VEZXRhaWwgYXMgbGljZW5zZURldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcbiAgICAgICAgICAgIGlmKGRhdGEudXBkYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IGRhdGEuaWQ7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgb25OZXdMaWNlbnNlQ3JlYXRlZChsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VBZG1pbi9jcmVhdGVkL0NyZWF0ZWRMaWNlbnNlLmh0bWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnQ3JlYXRlZExpY2Vuc2UgYXMgY3JlYXRlZExpY2Vuc2UnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBlbWFpbDogbGljZW5zZS5lbWFpbCAgfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VBZG1pbkxpc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBNYW51YWxseVJlcXVlc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnNjb3BlID0gJHNjb3BlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGlkOiAgcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIGVtYWlsOiBwYXJhbXMubGljZW5zZS5lbWFpbCxcclxuICAgICAgICAgICAgZW5jcnlwdGVkRGV0YWlsOiAnJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIC8vIEdldCB0aGUgaGFzaCBjb2RlIHVzaW5nIHRoZSBpZC5cclxuICAgICAgICB0aGlzLmdldEhhc2hDb2RlKCk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIGdldEhhc2hDb2RlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRIYXNoQ29kZSh0aGlzLmxpY2Vuc2VNb2RlbC5pZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuZW5jcnlwdGVkRGV0YWlsID0gZGF0YTtcclxuICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKiBDcmVhdGUgYSBuZXcgUmVxdWVzdCB0byBnZXQgYSBMaWNlbnNlXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFJlcXVlc3RMaWNlbnNlIGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICAvKipcclxuICAgICAqIEluaXRpYWxpemUgYWxsIHRoZSBwcm9wZXJ0aWVzXHJcbiAgICAgKiBAcGFyYW0gJGxvZ1xyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VBZG1pblNlcnZpY2VcclxuICAgICAqIEBwYXJhbSAkdWliTW9kYWxJbnN0YW5jZVxyXG4gICAgICovXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgRW52aXJvbm1lbnQgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UgPSBbXTtcclxuICAgICAgICAvLyBEZWZpbmUgdGhlIFByb2plY3QgU2VsZWN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSBbXTtcclxuXHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmdldFByb2plY3REYXRhU291cmNlKCk7XHJcblxyXG4gICAgICAgIC8vIENyZWF0ZSB0aGUgTW9kZWwgZm9yIHRoZSBOZXcgTGljZW5zZVxyXG4gICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBlbWFpbDogJycsXHJcbiAgICAgICAgICAgIGVudmlyb25tZW50SWQ6IDAsXHJcbiAgICAgICAgICAgIHByb2plY3RJZDogMCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogJycsXHJcbiAgICAgICAgICAgIHJlcXVlc3ROb3RlOiAnJ1xyXG4gICAgICAgIH1cclxuXHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKT0+e1xyXG4gICAgICAgICAgICB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSA9IGRhdGE7XHJcbiAgICAgICAgICAgIGlmKHRoaXMuZW52aXJvbm1lbnREYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgICAgICB2YXIgaW5kZXggPSB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZS5maW5kSW5kZXgoZnVuY3Rpb24oZW52aXJvbWVudCl7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGVudmlyb21lbnQubmFtZSAgPT09ICdQcm9kdWN0aW9uJztcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgaW5kZXggPSBpbmRleCB8fCAwO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuZW52aXJvbm1lbnRJZCA9IGRhdGFbaW5kZXhdLmlkO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIFByb2plY3QgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0UHJvamVjdExpc3RPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubmV3TGljZW5zZU1vZGVsLnByb2plY3RJZCA9IGRhdGFbMF0uaWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubmV3TGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHJldHVybiBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pXHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhVGV4dEZpZWxkOiAnbmFtZScsXHJcbiAgICAgICAgICAgIGRhdGFWYWx1ZUZpZWxkOiAnaWQnLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZSxcclxuICAgICAgICAgICAgc2VsZWN0OiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIC8vIE9uIFByb2plY3QgQ2hhbmdlLCBzZWxlY3QgdGhlIENsaWVudCBOYW1lXHJcbiAgICAgICAgICAgICAgICB2YXIgaXRlbSA9IHRoaXMuc2VsZWN0UHJvamVjdC5kYXRhSXRlbShlLml0ZW0pO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwuY2xpZW50TmFtZSA9IGl0ZW0uY2xpZW50Lm5hbWU7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBnZW5lcmF0ZSBhIG5ldyBMaWNlbnNlIHJlcXVlc3RcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2VSZXF1ZXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMuaXNEaXJ0eSgpKSB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIFJlcXVlc3RlZDogJywgdGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QodGhpcy5uZXdMaWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UodGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZUFkbWluU2VydmljZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgcmVzdFNlcnZpY2VIYW5kbGVyLCAkcm9vdFNjb3BlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UgPSByZXN0U2VydmljZUhhbmRsZXI7XHJcbiAgICAgICAgdGhpcy5yb290U2NvcGUgPSAkcm9vdFNjb3BlO1xyXG4gICAgICAgIHRoaXMuc3RhdHVzU3VjY2VzcyA9ICdzdWNjZXNzJztcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnbGljZW5zZUFkbWluU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmdldEhhc2hDb2RlKGxpY2Vuc2VJZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgb25TdWNjZXNzKXtcclxuICAgICAgICBuZXdMaWNlbnNlLmVudmlyb25tZW50SWQgPSBwYXJzZUludChuZXdMaWNlbnNlLmVudmlyb25tZW50SWQpO1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkucmVzdWJtaXRMaWNlbnNlUmVxdWVzdChsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnUmVxdWVzdCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZW1haWxSZXF1ZXN0KGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmVtYWlsUmVxdWVzdChsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnUmVxdWVzdCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiAgQXBwbHkgVGhlIExpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gb25TdWNjZXNzXHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuXHJcbiAgICAgICAgdmFyIGhhc2ggPSAge1xyXG4gICAgICAgICAgICBoYXNoOiBsaWNlbnNlLmtleVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5hcHBseUxpY2Vuc2UobGljZW5zZS5pZCwgaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3IoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKHsgc3VjY2VzczogdHJ1ZX0pO1xyXG5cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5kZWxldGVMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RJbXBvcnQgZnJvbSAnLi9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbGljZW5zZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VNYW5hZ2VyTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0xpY2Vuc2luZyBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ01hbmFnZXInLCAnTGljZW5zZScsICdMaXN0J119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvbWFuYWdlci9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJMaXN0IGFzIGxpY2Vuc2VNYW5hZ2VyTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgJyRyb290U2NvcGUnLCBMaWNlbnNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlTWFuYWdlckxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlTWFuYWdlckxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignUmVxdWVzdEltcG9ydCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RJbXBvcnRdKTtcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZU1hbmFnZXJEZXRhaWwnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCAnJHVpYk1vZGFsSW5zdGFuY2UnLCAncGFyYW1zJywgTGljZW5zZU1hbmFnZXJEZXRhaWxdKTtcclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBMaWNlbnNlTWFuYWdlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEZvcm1WYWxpZGF0b3IgZnJvbSAnLi4vLi4vdXRpbHMvZm9ybS9Gb3JtVmFsaWRhdG9yLmpzJztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VNYW5hZ2VyRGV0YWlsIGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgc3VwZXIoJGxvZywgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKTtcclxuICAgICAgICB0aGlzLnNjb3BlID0gJHNjb3BlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgb3duZXJOYW1lOiBwYXJhbXMubGljZW5zZS5vd25lci5uYW1lLFxyXG4gICAgICAgICAgICBlbWFpbDogcGFyYW1zLmxpY2Vuc2UuZW1haWwsXHJcbiAgICAgICAgICAgIHByb2plY3Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiBwYXJhbXMubGljZW5zZS5wcm9qZWN0LmlkLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogcGFyYW1zLmxpY2Vuc2UucHJvamVjdC5uYW1lLFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjbGllbnRJZDogcGFyYW1zLmxpY2Vuc2UuY2xpZW50LmlkLFxyXG4gICAgICAgICAgICBjbGllbnROYW1lOiBwYXJhbXMubGljZW5zZS5jbGllbnQubmFtZSxcclxuICAgICAgICAgICAgc3RhdHVzSWQ6IHBhcmFtcy5saWNlbnNlLnN0YXR1cy5pZCxcclxuICAgICAgICAgICAgbWV0aG9kOiB7XHJcbiAgICAgICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLmlkLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLm5hbWUsXHJcbiAgICAgICAgICAgICAgICBtYXg6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5tYXgsXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiB7IGlkOiBwYXJhbXMubGljZW5zZS5lbnZpcm9ubWVudC5pZCB9LFxyXG4gICAgICAgICAgICByZXF1ZXN0RGF0ZTogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdERhdGUsXHJcbiAgICAgICAgICAgIGluaXREYXRlOiAocGFyYW1zLmxpY2Vuc2UuYWN0aXZhdGlvbkRhdGUgIT09IG51bGwpPyBhbmd1bGFyLmNvcHkocGFyYW1zLmxpY2Vuc2UuYWN0aXZhdGlvbkRhdGUpIDogJycsXHJcbiAgICAgICAgICAgIGVuZERhdGU6IChwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSAhPT0gbnVsbCk/IGFuZ3VsYXIuY29weShwYXJhbXMubGljZW5zZS5leHBpcmF0aW9uRGF0ZSkgOiAnJyxcclxuICAgICAgICAgICAgc3BlY2lhbEluc3RydWN0aW9uczogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdE5vdGUsXHJcbiAgICAgICAgICAgIHdlYnNpdGVOYW1lOiBwYXJhbXMubGljZW5zZS53ZWJzaXRlbmFtZSxcclxuXHJcbiAgICAgICAgICAgIGJhbm5lck1lc3NhZ2U6IHBhcmFtcy5saWNlbnNlLmJhbm5lck1lc3NhZ2UsXHJcbiAgICAgICAgICAgIHJlcXVlc3RlZElkOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0ZWRJZCxcclxuICAgICAgICAgICAgcmVwbGFjZWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkLFxyXG4gICAgICAgICAgICByZXBsYWNlZElkOiBwYXJhbXMubGljZW5zZS5yZXBsYWNlZElkLFxyXG4gICAgICAgICAgICBhY3Rpdml0eUxpc3Q6IHBhcmFtcy5saWNlbnNlLmFjdGl2aXR5TGlzdCxcclxuICAgICAgICAgICAgaG9zdE5hbWU6IHBhcmFtcy5saWNlbnNlLmhvc3ROYW1lLFxyXG4gICAgICAgICAgICBoYXNoOiBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgZ3JhY2VQZXJpb2REYXlzOiBwYXJhbXMubGljZW5zZS5ncmFjZVBlcmlvZERheXMsXHJcblxyXG4gICAgICAgICAgICBhcHBsaWVkOiBwYXJhbXMubGljZW5zZS5hcHBsaWVkLFxyXG4gICAgICAgICAgICBrZXlJZDogcGFyYW1zLmxpY2Vuc2Uua2V5SWRcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmxpY2Vuc2VLZXkgPSAnTGljZW5zZXMgaGFzIG5vdCBiZWVuIGlzc3VlZCc7XHJcblxyXG4gICAgICAgIC8vIERlZmluZWQgdGhlIEVudmlyb25tZW50IFNlbGVjdFxyXG4gICAgICAgIHRoaXMuc2VsZWN0RW52aXJvbm1lbnQgPSB7fTtcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50TGlzdE9wdGlvbnMgPSBbXTtcclxuICAgICAgICB0aGlzLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpO1xyXG5cclxuICAgICAgICAvLyBEZWZpbmVkIHRoZSBTdGF0dXMgU2VsZWN0IExpc3RcclxuICAgICAgICB0aGlzLnNlbGVjdFN0YXR1cyA9IFtdO1xyXG4gICAgICAgIHRoaXMuZ2V0U3RhdHVzRGF0YVNvdXJjZSgpO1xyXG5cclxuICAgICAgICAvLyBJbml0IHRoZSB0d28gS2VuZG8gRGF0ZXMgZm9yIEluaXQgYW5kIEVuZERhdGVcclxuICAgICAgICB0aGlzLmluaXREYXRlID0ge307XHJcbiAgICAgICAgdGhpcy5pbml0RGF0ZU9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGZvcm1hdDogJ3l5eXkvTU0vZGQnLFxyXG4gICAgICAgICAgICBvcGVuOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgY2hhbmdlOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VJbml0RGF0ZSgpO1xyXG4gICAgICAgICAgICB9KVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuZW5kRGF0ZSA9IHt9O1xyXG4gICAgICAgIHRoaXMuZW5kRGF0ZU9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGZvcm1hdDogJ3l5eXkvTU0vZGQnLFxyXG4gICAgICAgICAgICBvcGVuOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pLFxyXG4gICAgICAgICAgICBjaGFuZ2U6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNoYW5nZUVuZERhdGUoKTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG5cclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlTWV0aG9kT3B0aW9ucygpO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUxpY2Vuc2VLZXkoKTtcclxuICAgICAgICB0aGlzLnByZXBhcmVBY3Rpdml0eUxpc3QoKTtcclxuXHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuXHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDb250cm9scyB3aGF0IGJ1dHRvbnMgdG8gc2hvd1xyXG4gICAgICovXHJcbiAgICBwcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5wZW5kaW5nTGljZW5zZSA9IHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSA0ICYmICF0aGlzLmVkaXRNb2RlO1xyXG4gICAgICAgIHRoaXMuZXhwaXJlZE9yVGVybWluYXRlZCA9ICh0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMiB8fCB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMyk7XHJcbiAgICAgICAgdGhpcy5hY3RpdmVTaG93TW9kZSA9IHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSAxICYmICF0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgJiYgIXRoaXMuZWRpdE1vZGU7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMSxcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdTZXJ2ZXJzJyxcclxuICAgICAgICAgICAgICAgIG1heDogMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBpZDogMixcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUb2tlbnMnLFxyXG4gICAgICAgICAgICAgICAgbWF4OiAwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIGlkOiAzLFxyXG4gICAgICAgICAgICAgICAgbmFtZTogJ0N1c3RvbSdcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIF1cclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlTGljZW5zZUtleSgpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRLZXlDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUtleSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVBY3Rpdml0eUxpc3QoKSB7XHJcbiAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWQgPSB7fTtcclxuICAgICAgICB0aGlzLmFjdGl2aXR5R3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2RhdGUnLCB0aXRsZTogJ0RhdGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3dob20nLCB0aXRsZTogJ1dob20nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGlvbicsIHRpdGxlOiAnQWN0aW9uJ31cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZTogdGhpcy5saWNlbnNlTW9kZWwuYWN0aXZpdHlMaXN0LFxyXG4gICAgICAgICAgICBzY3JvbGxhYmxlOiB0cnVlXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIElmIGJ5IHNvbWUgcmVhc29uIHRoZSBMaWNlbnNlIHdhcyBub3QgYXBwbGllZCBhdCBmaXJzdCB0aW1lLCB0aGlzIHdpbGwgZG8gYSByZXF1ZXN0IGZvciBpdFxyXG4gICAgICovXHJcbiAgICBhY3RpdmF0ZUxpY2Vuc2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuYWN0aXZhdGVMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9IDE7XHJcbiAgICAgICAgICAgIHRoaXMuZ2V0U3RhdHVzRGF0YVNvdXJjZSgpO1xyXG4gICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlTGljZW5zZUtleSgpO1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZFJlcXVpcmVkID0gdHJ1ZTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZXZva2VMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2RpYWxvZ0FjdGlvbi9EaWFsb2dBY3Rpb24uaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdEaWFsb2dBY3Rpb24gYXMgZGlhbG9nQWN0aW9uJyxcclxuICAgICAgICAgICAgc2l6ZTogJ3NtJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgdGl0bGU6ICdDb25maXJtYXRpb24gUmVxdWlyZWQnLCBtZXNzYWdlOiAnQXJlIHlvdSBzdXJlIHlvdSB3YW50IHRvIHJldm9rZSBpdD8gVGhpcyBhY3Rpb24gY2Fubm90IGJlIHVuZG9uZS4nfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UucmV2b2tlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWYWxpZGF0ZSB0aGUgaW5wdXQgb24gU2VydmVyIG9yIFRva2VucyBpcyBvbmx5IGludGVnZXIgb25seVxyXG4gICAgICogVGhpcyB3aWxsIGJlIGNvbnZlcnRlZCBpbiBhIG1vcmUgY29tcGxleCBkaXJlY3RpdmUgbGF0ZXJcclxuICAgICAqIFRPRE86IENvbnZlcnQgaW50byBhIGRpcmVjdGl2ZVxyXG4gICAgICovXHJcbiAgICB2YWxpZGF0ZUludGVnZXJPbmx5KGUsbW9kZWwpe1xyXG4gICAgICAgIHRyeSB7XHJcbiAgICAgICAgICAgIHZhciBuZXdWYWw9IHBhcnNlSW50KG1vZGVsKTtcclxuICAgICAgICAgICAgaWYoIWlzTmFOKG5ld1ZhbCkpIHtcclxuICAgICAgICAgICAgICAgIG1vZGVsID0gbmV3VmFsO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgbW9kZWwgPSAwO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIGlmKGUgJiYgZS5jdXJyZW50VGFyZ2V0KSB7XHJcbiAgICAgICAgICAgICAgICBlLmN1cnJlbnRUYXJnZXQudmFsdWUgPSBtb2RlbDtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICB0aGlzLiRsb2cud2FybignSW52YWxpZCBOdW1iZXIgRXhwY2VwdGlvbicsIG1vZGVsKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBTYXZlIGN1cnJlbnQgY2hhbmdlc1xyXG4gICAgICovXHJcbiAgICBzYXZlTGljZW5zZSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcbiAgICAgICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnNhdmVMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZWxvYWRSZXF1aXJlZCA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ0xpY2Vuc2UgU2F2ZWQnKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpXHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ2hhbmdlIHRoZSBzdGF0dXMgdG8gRWRpdFxyXG4gICAgICovXHJcbiAgICBtb2RpZnlMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSB0cnVlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc2VsZWN0RW52aXJvbm1lbnRMaXN0T3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBpZighdGhpcy5saWNlbnNlTW9kZWwuZW52aXJvbm1lbnRJZCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLmVudmlyb25tZW50SWQgPSBkYXRhWzBdLmlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSlcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGFUZXh0RmllbGQ6ICduYW1lJyxcclxuICAgICAgICAgICAgZGF0YVZhbHVlRmllbGQ6ICdpZCcsXHJcbiAgICAgICAgICAgIHZhbHVlUHJpbWl0aXZlOiB0cnVlXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFBvcHVsYXRlIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRTdGF0dXNEYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMuc3RhdHVzVGV4dCA9XHJcbiAgICAgICAgICAgICAodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDEpPyAnQWN0aXZlJyA6XHJcbiAgICAgICAgICAgICAgICAgKCh0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMik/ICdFeHBpcmVkJzpcclxuICAgICAgICAgICAgICAgICAgICAgKCh0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXNJZCA9PT0gMyk/ICdUZXJtaW5hdGVkJzpcclxuICAgICAgICAgICAgICAgICAgICAgICAgICgodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzSWQgPT09IDQpPyAnUGVuZGluZyc6XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAoKHRoaXMubGljZW5zZU1vZGVsLnN0YXR1c0lkID09PSA1KT8gJ0NvcnJ1cHQnIDogJycpKSkpO1xyXG5cclxuICAgICAgICAvKnRoaXMuc2VsZWN0U3RhdHVzTGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IFtcclxuICAgICAgICAgICAgICAgIHtpZDogMSwgbmFtZTogJ0FjdGl2ZSd9LFxyXG4gICAgICAgICAgICAgICAge2lkOiAyLCBuYW1lOiAnRXhwaXJlZCd9LFxyXG4gICAgICAgICAgICAgICAge2lkOiAzLCBuYW1lOiAnVGVybWluYXRlZCd9LFxyXG4gICAgICAgICAgICAgICAge2lkOiA0LCBuYW1lOiAnUGVuZGluZyd9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFUZXh0RmllbGQ6ICduYW1lJyxcclxuICAgICAgICAgICAgZGF0YVZhbHVlRmllbGQ6ICdpZCcsXHJcbiAgICAgICAgICAgIHZhbHVlUHJpbWl0aXZlOiB0cnVlXHJcbiAgICAgICAgfSovXHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBIG5ldyBQcm9qZWN0IGhhcyBiZWVuIHNlbGVjdGVkLCB0aGF0IG1lYW5zIHdlIG5lZWQgdG8gcmVsb2FkIHRoZSBuZXh0IHByb2plY3Qgc2VjdGlvblxyXG4gICAgICogQHBhcmFtIGl0ZW1cclxuICAgICAqL1xyXG4gICAgb25DaGFuZ2VQcm9qZWN0KGl0ZW0pIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPbiBjaGFuZ2UgUHJvamVjdCcsIGl0ZW0pO1xyXG4gICAgfVxyXG5cclxuICAgIG9uQ2hhbmdlSW5pdERhdGUoKSB7XHJcbiAgICAgICAgdmFyIHN0YXJ0RGF0ZSA9IHRoaXMuaW5pdERhdGUudmFsdWUoKSxcclxuICAgICAgICAgICAgZW5kRGF0ZSA9IHRoaXMuZW5kRGF0ZS52YWx1ZSgpO1xyXG5cclxuICAgICAgICBpZiAoc3RhcnREYXRlKSB7XHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZSA9IG5ldyBEYXRlKHN0YXJ0RGF0ZSk7XHJcbiAgICAgICAgICAgIHN0YXJ0RGF0ZS5zZXREYXRlKHN0YXJ0RGF0ZS5nZXREYXRlKCkpO1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKHN0YXJ0RGF0ZSk7XHJcblxyXG4gICAgICAgICAgICBpZihlbmREYXRlKSB7XHJcbiAgICAgICAgICAgICAgICBpZih0aGlzLmluaXREYXRlLnZhbHVlKCkgPiB0aGlzLmVuZERhdGUudmFsdWUoKSkge1xyXG4gICAgICAgICAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZShlbmREYXRlKTtcclxuICAgICAgICAgICAgICAgICAgICBlbmREYXRlLnNldERhdGUoc3RhcnREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuZW5kRGF0ZSA9IGVuZERhdGU7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgb25DaGFuZ2VFbmREYXRlKCl7XHJcbiAgICAgICAgdmFyIGVuZERhdGUgPSB0aGlzLmVuZERhdGUudmFsdWUoKSxcclxuICAgICAgICAgICAgc3RhcnREYXRlID0gdGhpcy5pbml0RGF0ZS52YWx1ZSgpO1xyXG5cclxuICAgICAgICBpZiAoZW5kRGF0ZSkge1xyXG4gICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgIGVuZERhdGUuc2V0RGF0ZShlbmREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgfSBlbHNlIGlmIChzdGFydERhdGUpIHtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihuZXcgRGF0ZShzdGFydERhdGUpKTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICBlbmREYXRlID0gbmV3IERhdGUoKTtcclxuICAgICAgICAgICAgdGhpcy5pbml0RGF0ZS5tYXgoZW5kRGF0ZSk7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4oZW5kRGF0ZSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIGlmKHRoaXMuZWRpdE1vZGUpIHtcclxuICAgICAgICAgICAgdGhpcy5yZXNldEZvcm0oKCk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uUmVzZXRGb3JtKCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSBpZih0aGlzLnJlbG9hZFJlcXVpcmVkKXtcclxuICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKHt9KTtcclxuICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGVwZW5kaW5nIHRoZSBudW1iZXIgb2YgZmllbGRzIGFuZCB0eXBlIG9mIGZpZWxkLCB0aGUgcmVzZXQgY2FuJ3QgYmUgb24gdGhlIEZvcm1WYWxpZG9yLCBhdCBsZWFzdCBub3Qgbm93XHJcbiAgICAgKi9cclxuICAgIG9uUmVzZXRGb3JtKCkge1xyXG4gICAgICAgIHRoaXMucmVzZXREcm9wRG93bih0aGlzLnNlbGVjdEVudmlyb25tZW50LCB0aGlzLmxpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudC5pZCk7XHJcbiAgICAgICAgdGhpcy5vbkNoYW5nZUluaXREYXRlKCk7XHJcbiAgICAgICAgdGhpcy5vbkNoYW5nZUVuZERhdGUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZSA9IGZhbHNlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJMaXN0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc3RhdGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZCA9IHt9O1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UgPSBsaWNlbnNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuXHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgLy90aGlzLmdldExpY2Vuc2VMaXN0KCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ0xpY2Vuc2VNYW5hZ2VyTGlzdCBJbnN0YW5jZWQnKTtcclxuICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSAwO1xyXG4gICAgfVxyXG5cclxuXHJcbiAgICBnZXREYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJsaWNlbnNlTWFuYWdlckxpc3Qub25SZXF1ZXN0SW1wb3J0TGljZW5zZSgpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IEltcG9ydCBMaWNlbnNlIFJlcXVlc3Q8L2J1dHRvbj4gPGRpdiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKVwiIGNsYXNzPVwiYWN0aW9uLXRvb2xiYXItcmVmcmVzaC1idG5cIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcmVmcmVzaFwiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L2Rpdj4nKSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAyMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdpZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgZmlsdGVyYWJsZTogZmFsc2UsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5vbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyh0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnb3duZXIubmFtZScsIHRpdGxlOiAnT3duZXInfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NsaWVudC5uYW1lJywgdGl0bGU6ICdDbGllbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Byb2plY3QubmFtZScsIHRpdGxlOiAnUHJvamVjdCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cy5uYW1lJywgdGl0bGU6ICdTdGF0dXMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3R5cGUubmFtZScsIHRpdGxlOiAnVHlwZScsICB0ZW1wbGF0ZTogJyNpZihkYXRhLnR5cGUgJiYgZGF0YS50eXBlLm5hbWUgPT09IFwiTVVMVElfUFJPSkVDVFwiKXsjIEdsb2JhbCAjIH0gZWxzZSB7IyBTaW5nbGUgI30jJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QubmFtZScsIHRpdGxlOiAnTWV0aG9kJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdtZXRob2QuaWQnLCBoaWRkZW46IHRydWV9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm1heCcsIHRpdGxlOiAnU2VydmVyL1Rva2Vucyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aXZhdGlvbkRhdGUnLCB0aXRsZTogJ0luY2VwdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2V4cGlyYXRpb25EYXRlJywgdGl0bGU6ICdFeHBpcmF0aW9uJywgdHlwZTogJ2RhdGUnLCBmb3JtYXQgOiAnezA6ZGQvTU1NL3l5eXl9JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW52aXJvbm1lbnQubmFtZScsIHRpdGxlOiAnRW52aXJvbm1lbnQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDonZ3JhY2VQZXJpb2REYXlzJyxoaWRkZW46IHRydWV9XHJcbiAgICAgICAgICAgIF0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldExpY2Vuc2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBjaGFuZ2U6ICAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIC8vIFdlIGFyZSBjb21pbmcgZnJvbSBhIG5ldyBpbXBvcnRlZCByZXF1ZXN0IGxpY2Vuc2VcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgIT09IDAgJiYgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLl9kYXRhKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHZhciBuZXdMaWNlbnNlQ3JlYXRlZCA9IHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YS5maW5kKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gbGljZW5zZS5pZCA9PT0gdGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IDA7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICBpZihuZXdMaWNlbnNlQ3JlYXRlZCkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyhuZXdMaWNlbnNlQ3JlYXRlZCk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHNvcnRhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBmaWx0ZXJhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICBleHRyYTogZmFsc2VcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBUaGUgdXNlciBJbXBvcnQgYSBuZXcgTGljZW5zZVxyXG4gICAgICovXHJcbiAgICBvblJlcXVlc3RJbXBvcnRMaWNlbnNlKCkge1xyXG4gICAgICAgIHZhciBtb2RhbEluc3RhbmNlID0gdGhpcy51aWJNb2RhbC5vcGVuKHtcclxuICAgICAgICAgICAgYW5pbWF0aW9uOiB0cnVlLFxyXG4gICAgICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2xpY2Vuc2VNYW5hZ2VyL3JlcXVlc3RJbXBvcnQvUmVxdWVzdEltcG9ydC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1JlcXVlc3RJbXBvcnQgYXMgcmVxdWVzdEltcG9ydCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCdcclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IGxpY2Vuc2VJbXBvcnRlZC5pZDsgLy8gdGFrZSB0aGlzIHBhcmFtIGZyb20gdGhlIGxhc3QgaW1wb3J0ZWQgbGljZW5zZSwgb2YgY291cnNlXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBZnRlciBjbGlja2luZyBvbiBlZGl0LCB3ZSByZWRpcmVjdCB0aGUgdXNlciB0byB0aGUgRWRpdGlvbiBzY3JlZW4gaW5zdGVhZCBvZiBvcGVuIGEgZGlhbG9nXHJcbiAgICAgKiBkdSB0aGUgc2l6ZSBvZiB0aGUgaW5wdXRzXHJcbiAgICAgKi9cclxuICAgIG9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKGxpY2Vuc2UpIHtcclxuICAgICAgICB0aGlzLmxvZy5pbmZvKCdPcGVuIERldGFpbHMgZm9yOiAnLCBsaWNlbnNlKTtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdMaWNlbnNlTWFuYWdlckRldGFpbCBhcyBsaWNlbnNlTWFuYWdlckRldGFpbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHZhciBkYXRhSXRlbSA9IHt9O1xyXG4gICAgICAgICAgICAgICAgICAgIGlmKGxpY2Vuc2UgJiYgbGljZW5zZS5kYXRhSXRlbSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2UuZGF0YUl0ZW07XHJcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4geyBsaWNlbnNlOiBkYXRhSXRlbSB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgcmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCkge1xyXG4gICAgICAgIGlmKHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBGb3JtVmFsaWRhdG9yIGZyb20gJy4uLy4uL3V0aWxzL2Zvcm0vRm9ybVZhbGlkYXRvci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SW1wb3J0IGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlID0gbGljZW5zZU1hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBoYXNoOiAnJ1xyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSBhbmQgdmFsaWRhdGUgdGhlIEtleSBpcyBjb3JyZWN0XHJcbiAgICAgKi9cclxuICAgIG9uSW1wb3J0TGljZW5zZSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5pbXBvcnRMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAobGljZW5zZUltcG9ydGVkKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UobGljZW5zZUltcG9ydGVkLmRhdGEpO1xyXG4gICAgICAgICAgICB9LCAobGljZW5zZUltcG9ydGVkKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShsaWNlbnNlSW1wb3J0ZWQuZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlciwgJHJvb3RTY29wZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG4gICAgICAgIHRoaXMucm9vdFNjb3BlID0gJHJvb3RTY29wZTtcclxuICAgICAgICB0aGlzLnN0YXR1c1N1Y2Nlc3MgPSAnc3VjY2Vzcyc7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ2xpY2Vuc2VNYW5hZ2VyU2VydmljZSBJbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRMaWNlbnNlTGlzdChvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmdldFByb2plY3REYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2Uob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRLZXlDb2RlKGxpY2Vuc2VJZCwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0S2V5Q29kZShsaWNlbnNlSWQsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgdGhlIExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcblxyXG4gICAgICAgIHZhciBsaWNlbnNlTW9kaWZpZWQgPSB7XHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiB7IGlkOiBwYXJzZUludChsaWNlbnNlLmVudmlyb25tZW50LmlkKSB9LFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIGlkOiBwYXJzZUludChsaWNlbnNlLm1ldGhvZC5pZClcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGlvbkRhdGU6IGxpY2Vuc2UuaW5pdERhdGUsXHJcbiAgICAgICAgICAgIGV4cGlyYXRpb25EYXRlOiBsaWNlbnNlLmVuZERhdGUsXHJcbiAgICAgICAgICAgIHN0YXR1czogeyBpZDogbGljZW5zZS5zdGF0dXNJZCB9LFxyXG4gICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICBpZDogKGxpY2Vuc2UucHJvamVjdC5pZCAhPT0gJ2FsbCcpPyBwYXJzZUludChsaWNlbnNlLnByb2plY3QuaWQpIDogbGljZW5zZS5wcm9qZWN0LmlkLCAgLy8gV2UgcGFzcyAnYWxsJyB3aGVuIGlzIG11bHRpcHJvamVjdFxyXG4gICAgICAgICAgICAgICAgbmFtZTogbGljZW5zZS5wcm9qZWN0Lm5hbWVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogbGljZW5zZS5iYW5uZXJNZXNzYWdlLFxyXG4gICAgICAgICAgICBncmFjZVBlcmlvZERheXM6IGxpY2Vuc2UuZ3JhY2VQZXJpb2REYXlzLFxyXG4gICAgICAgICAgICB3ZWJzaXRlbmFtZTogbGljZW5zZS53ZWJzaXRlTmFtZSxcclxuICAgICAgICAgICAgaG9zdE5hbWU6IGxpY2Vuc2UuaG9zdE5hbWVcclxuICAgICAgICB9O1xyXG4gICAgICAgIGlmKGxpY2Vuc2UubWV0aG9kICE9PSAzKSB7XHJcbiAgICAgICAgICAgIGxpY2Vuc2VNb2RpZmllZC5tZXRob2QubWF4ID0gcGFyc2VJbnQobGljZW5zZS5tZXRob2QubWF4KTtcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnNhdmVMaWNlbnNlKGxpY2Vuc2UuaWQsIGxpY2Vuc2VNb2RpZmllZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuICAgIC8qKlxyXG4gICAgICogRG9lcyB0aGUgYWN0aXZhdGlvbiBvZiB0aGUgY3VycmVudCBsaWNlbnNlIGlmIHRoaXMgaXMgbm90IGFjdGl2ZVxyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBhY3RpdmF0ZUxpY2Vuc2UobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5hY3RpdmF0ZUxpY2Vuc2UobGljZW5zZS5pZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ1RoZSBsaWNlbnNlIHdhcyBhY3RpdmF0ZWQgYW5kIHRoZSBsaWNlbnNlIHdhcyBlbWFpbGVkLid9KTtcclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogTWFrZSB0aGUgcmVxdWVzdCB0byBJbXBvcnQgdGhlIGxpY2Vuc2UsIGlmIGZhaWxzLCB0aHJvd3MgYW4gZXhjZXB0aW9uIHZpc2libGUgZm9yIHRoZSB1c2VyIHRvIHRha2UgYWN0aW9uXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGltcG9ydExpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzLCBvbkVycm9yKSB7XHJcbiAgICAgICAgdmFyIGhhc2ggPSB7XHJcbiAgICAgICAgICAgIGRhdGE6IGxpY2Vuc2UuaGFzaFxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnJlcXVlc3RJbXBvcnQoaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBJbXBvcnRlZCd9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6ICdMaWNlbnNlIHdhcyBub3QgYXBwbGllZC4gUmV2aWV3IHRoZSBwcm92aWRlZCBMaWNlbnNlIEtleSBpcyBjb3JyZWN0Lid9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBvbkVycm9yKHsgc3VjY2VzczogZmFsc2V9KTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJldm9rZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkucmV2b2tlTGljZW5zZShsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0KG5ld0xpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTm90aWNlTGlzdCBmcm9tICcuL2xpc3QvTm90aWNlTGlzdC5qcyc7XHJcbmltcG9ydCBOb3RpY2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTm90aWNlTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgRWRpdE5vdGljZSBmcm9tICcuL2VkaXQvRWRpdE5vdGljZS5qcyc7XHJcblxyXG52YXIgTm90aWNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5Ob3RpY2VNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdub3RpY2VNYW5hZ2VyJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdub3RpY2VMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTm90aWNlIEFkbWluaXN0cmF0aW9uJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FkbWluJywgJ05vdGljZScsICdMaXN0J119fSxcclxuICAgICAgICAgICAgdXJsOiAnL25vdGljZS9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2xpc3QvTm90aWNlTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTm90aWNlTGlzdCBhcyBub3RpY2VMaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuc2VydmljZSgnTm90aWNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgTm90aWNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTm90aWNlTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTm90aWNlTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0VkaXROb3RpY2UnLCBbJyRsb2cnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIEVkaXROb3RpY2VdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IE5vdGljZU1hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEVkaXROb3RpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIG5vdGljZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlID0gbm90aWNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uID0gcGFyYW1zLmFjdGlvbjtcclxuICAgICAgICB0aGlzLmFjdGlvblR5cGUgPSBwYXJhbXMuYWN0aW9uVHlwZTtcclxuXHJcbiAgICAgICAgdGhpcy5rZW5kb0VkaXRvclRvb2xzID0gW1xyXG4gICAgICAgICAgICAnZm9ybWF0dGluZycsICdjbGVhbkZvcm1hdHRpbmcnLFxyXG4gICAgICAgICAgICAnZm9udE5hbWUnLCAnZm9udFNpemUnLFxyXG4gICAgICAgICAgICAnanVzdGlmeUxlZnQnLCAnanVzdGlmeUNlbnRlcicsICdqdXN0aWZ5UmlnaHQnLCAnanVzdGlmeUZ1bGwnLFxyXG4gICAgICAgICAgICAnYm9sZCcsXHJcbiAgICAgICAgICAgICdpdGFsaWMnLFxyXG4gICAgICAgICAgICAndmlld0h0bWwnXHJcbiAgICAgICAgXTtcclxuXHJcbiAgICAgICAgLy8gQ1NTIGhhcyBub3QgY2FuY2VsaW5nIGF0dHJpYnV0ZXMsIHNvIGluc3RlYWQgb2YgcmVtb3ZpbmcgZXZlcnkgcG9zc2libGUgSFRNTCwgd2UgbWFrZSBlZGl0b3IgaGFzIHNhbWUgY3NzXHJcbiAgICAgICAgdGhpcy5rZW5kb1N0eWxlc2hlZXRzID0gW1xyXG4gICAgICAgICAgICAnLi4vc3RhdGljL2Rpc3QvanMvdmVuZG9ycy9ib290c3RyYXAvZGlzdC9jc3MvYm9vdHN0cmFwLm1pbi5jc3MnLCAvLyBPdXJ0IGN1cnJlbnQgQm9vdHN0cmFwIGNzc1xyXG4gICAgICAgICAgICAnLi4vc3RhdGljL2Rpc3QvY3NzL1REU1RNTGF5b3V0Lm1pbi5jc3MnIC8vIE9yaWdpbmFsIFRlbXBsYXRlIENTU1xyXG5cclxuICAgICAgICBdO1xyXG5cclxuICAgICAgICB0aGlzLmdldFR5cGVEYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZWwgPSB7XHJcbiAgICAgICAgICAgIHRpdGxlOiAnJyxcclxuICAgICAgICAgICAgdHlwZUlkOiAwLFxyXG4gICAgICAgICAgICBhY3RpdmU6IGZhbHNlLFxyXG4gICAgICAgICAgICBodG1sVGV4dDogJycsXHJcbiAgICAgICAgICAgIHJhd1RleHQ6ICcnXHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBPbiBFZGl0aW9uIE1vZGUgd2UgY2MgdGhlIG1vZGVsIGFuZCBvbmx5IHRoZSBwYXJhbXMgd2UgbmVlZFxyXG4gICAgICAgIGlmKHBhcmFtcy5ub3RpY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuaWQgPSBwYXJhbXMubm90aWNlLmlkO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC50aXRsZSA9IHBhcmFtcy5ub3RpY2UudGl0bGU7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLnR5cGVJZCA9IHBhcmFtcy5ub3RpY2UudHlwZS5pZDtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuYWN0aXZlID0gcGFyYW1zLm5vdGljZS5hY3RpdmU7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLmh0bWxUZXh0ID0gcGFyYW1zLm5vdGljZS5odG1sVGV4dDtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFR5cGVEYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMudHlwZURhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDEsIG5hbWU6ICdQcmVsb2dpbid9LFxyXG4gICAgICAgICAgICB7dHlwZUlkOiAyLCBuYW1lOiAnUG9zdGxvZ2luJ31cclxuICAgICAgICAgICAgLy97dHlwZUlkOiAzLCBuYW1lOiAnR2VuZXJhbCd9IERpc2FibGVkIHVudGlsIFBoYXNlIElJXHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBDcmVhdGUvRWRpdCBhIG5vdGljZVxyXG4gICAgICovXHJcbiAgICBzYXZlTm90aWNlKCkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8odGhpcy5hY3Rpb24gKyAnIE5vdGljZSBSZXF1ZXN0ZWQ6ICcsIHRoaXMuZWRpdE1vZGVsKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbC5yYXdUZXh0ID0gJCgnI2tlbmRvLWVkaXRvci1jcmVhdGUtZWRpdCcpLnRleHQoKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbC50eXBlSWQgPSBwYXJzZUludCh0aGlzLmVkaXRNb2RlbC50eXBlSWQpO1xyXG4gICAgICAgIGlmKHRoaXMuYWN0aW9uID09PSB0aGlzLmFjdGlvblR5cGUuTkVXKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuY3JlYXRlTm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2UgaWYodGhpcy5hY3Rpb24gPT09IHRoaXMuYWN0aW9uVHlwZS5FRElUKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZWRpdE5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGRlbGV0ZU5vdGljZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byBkZWxldGUgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZGVsZXRlTm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBub3RpY2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHtcclxuICAgICAgICAgICAgTkVXOiAnTmV3JyxcclxuICAgICAgICAgICAgRURJVDogJ0VkaXQnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZUxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLm5vdGljZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuTkVXKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBDcmVhdGUgTmV3IE5vdGljZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibm90aWNlTGlzdC5yZWxvYWROb3RpY2VMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2h0bWxUZXh0JywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuRURJVCwgdGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RpdGxlJywgdGl0bGU6ICdUaXRsZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5pZCcsIGhpZGRlbjogdHJ1ZX0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2ZScsIHRpdGxlOiAnQWN0aXZlJywgdGVtcGxhdGU6ICcjaWYoYWN0aXZlKSB7IyBZZXMgI30gZWxzZSB7IyBObyAjfSMnIH1cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICd0aXRsZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBOb3RpY2VcclxuICAgICAqL1xyXG4gICAgb25FZGl0Q3JlYXRlTm90aWNlKGFjdGlvbiwgbm90aWNlKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9lZGl0L0VkaXROb3RpY2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdFZGl0Tm90aWNlIGFzIGVkaXROb3RpY2UnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSBub3RpY2UgJiYgbm90aWNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGFjdGlvbjogYWN0aW9uLCBub3RpY2U6IGRhdGFJdGVtLCBhY3Rpb25UeXBlOiB0aGlzLmFjdGlvblR5cGV9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKG5vdGljZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgTm90aWNlOiAnLCBub3RpY2UpO1xyXG4gICAgICAgICAgICAvLyBBZnRlciBhIG5ldyB2YWx1ZSBpcyBhZGRlZCwgbGV0cyB0byByZWZyZXNoIHRoZSBHcmlkXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTm90aWNlTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbyhhY3Rpb24gKyAnIFJlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVsb2FkTm90aWNlTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLlRZUEUgPSB7XHJcbiAgICAgICAgICAgICcxJzogJ1ByZWxvZ2luJyxcclxuICAgICAgICAgICAgJzInOiAnUG9zdGxvZ2luJyxcclxuICAgICAgICAgICAgJzMnOiAnR2VuZXJhbCdcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTm90aWNlTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0Tm90aWNlTGlzdChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0Tm90aWNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB2YXIgbm90aWNlTGlzdCA9IFtdO1xyXG4gICAgICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICAgICAgLy8gVmVyaWZ5IHRoZSBMaXN0IHJldHVybnMgd2hhdCB3ZSBleHBlY3QgYW5kIHdlIGNvbnZlcnQgaXQgdG8gYW4gQXJyYXkgdmFsdWVcclxuICAgICAgICAgICAgICAgIGlmKGRhdGEgJiYgZGF0YS5ub3RpY2VzKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgbm90aWNlTGlzdCA9IGRhdGEubm90aWNlcztcclxuICAgICAgICAgICAgICAgICAgICBpZiAobm90aWNlTGlzdCAmJiBub3RpY2VMaXN0Lmxlbmd0aCA+IDApIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBub3RpY2VMaXN0Lmxlbmd0aDsgaSA9IGkgKyAxKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBub3RpY2VMaXN0W2ldLnR5cGUgPSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IG5vdGljZUxpc3RbaV0udHlwZUlkLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6IHRoaXMuVFlQRVtub3RpY2VMaXN0W2ldLnR5cGVJZF1cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBkZWxldGUgbm90aWNlTGlzdFtpXS50eXBlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5sb2cuZXJyb3IoJ0Vycm9yIHBhcnNpbmcgdGhlIE5vdGljZSBMaXN0JywgZSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKG5vdGljZUxpc3QpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IE5vdGljZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGNyZWF0ZU5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE5vdGljZSBzaG91bGQgaGF2ZSB0aGUgSUQgaW4gb3JkZXIgdG8gZWRpdCB0aGUgTm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZWRpdE5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmVkaXROb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBOb3RpY2Ugc2hvdWxkIGhhdmUgdGhlIElEIGluIG9yZGVyIHRvIGRlbGV0ZSB0aGUgbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZGVsZXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmRlbGV0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IFRhc2tNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvVGFza01hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyQ29udHJvbGxlciBmcm9tICcuL2xpc3QvVGFza01hbmFnZXJDb250cm9sbGVyLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyRWRpdCBmcm9tICcuL2VkaXQvVGFza01hbmFnZXJFZGl0LmpzJztcclxuXHJcbnZhciBUYXNrTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5UYXNrTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgJ2Zvcm1seUNvbmZpZ1Byb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgZm9ybWx5Q29uZmlnUHJvdmlkZXIpIHtcclxuXHJcbiAgICBmb3JtbHlDb25maWdQcm92aWRlci5zZXRUeXBlKHtcclxuICAgICAgICBuYW1lOiAnY3VzdG9tJyxcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJ2N1c3RvbS5odG1sJ1xyXG4gICAgfSk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCd0YXNrTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ015IFRhc2sgTWFuYWdlcicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydUYXNrIE1hbmFnZXInXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvdGFzay9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9saXN0L1Rhc2tNYW5hZ2VyQ29udGFpbmVyLmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdUYXNrTWFuYWdlckNvbnRyb2xsZXIgYXMgdGFza01hbmFnZXInXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuVGFza01hbmFnZXJNb2R1bGUuc2VydmljZSgndGFza01hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFRhc2tNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJDb250cm9sbGVyJywgWyckbG9nJywgJ3Rhc2tNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBUYXNrTWFuYWdlckNvbnRyb2xsZXJdKTtcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJFZGl0JywgWyckbG9nJywgVGFza01hbmFnZXJFZGl0XSk7XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgVGFza01hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAzLzExLzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJFZGl0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCB0YXNrTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG5cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIwLzIwMTUuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlckNvbnRyb2xsZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHRhc2tNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5tb2R1bGUgPSAnVGFza01hbmFnZXInO1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlID0gdGFza01hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudGFza0dyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5ldmVudERhdGFTb3VyY2UgPSBbXTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCBDbGFzc1xyXG4gICAgICAgIHRoaXMuZ2V0RXZlbnREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1Rhc2tNYW5hZ2VyIENvbnRyb2xsZXIgSW5zdGFuY2VkJyk7XHJcbiAgICAgICAgdGhpcy5pbml0Rm9ybSgpO1xyXG5cclxuICAgIH1cclxuXHJcbiAgICBvcGVuTW9kYWxEZW1vKCkge1xyXG5cclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICdhcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9lZGl0L1Rhc2tNYW5hZ2VyRWRpdC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyRWRpdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIGl0ZW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIFsnMScsJ2EyJywnZ2cnXTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChzZWxlY3RlZEl0ZW0pID0+IHtcclxuICAgICAgICAgICAgdGhpcy5kZWJ1ZyhzZWxlY3RlZEl0ZW0pO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTW9kYWwgZGlzbWlzc2VkIGF0OiAnICsgbmV3IERhdGUoKSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnRhc2tHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZ3JvdXBhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbe2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Rhc2snLCB0aXRsZTogJ1Rhc2snfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Rlc2NyaXB0aW9uJywgdGl0bGU6ICdEZXNjcmlwdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzZXROYW1lJywgdGl0bGU6ICdBc3NldCBOYW1lJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldFR5cGUnLCB0aXRsZTogJ0Fzc2V0IFR5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3VwZGF0ZWQnLCB0aXRsZTogJ1VwZGF0ZWQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2R1ZScsIHRpdGxlOiAnRHVlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMnLCB0aXRsZTogJ1N0YXR1cyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzaWduZWRUbycsIHRpdGxlOiAnQXNzaWduZWQgVG8nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RlYW0nLCB0aXRsZTogJ1RlYW0nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NhdGVnb3J5JywgdGl0bGU6ICdDYXRlZ29yeSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3VjJywgdGl0bGU6ICdTdWMuJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzY29yZScsIHRpdGxlOiAnU2NvcmUnfV0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIC8qdGhpcy50YXNrTWFuYWdlclNlcnZpY2UudGVzdFNlcnZpY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7Ki9cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGdldEV2ZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmV2ZW50RGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDEsIGV2ZW50TmFtZTogJ0FsbCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMiwgZXZlbnROYW1lOiAnQnVpbGRvdXQnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDMsIGV2ZW50TmFtZTogJ0RSLUVQJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiA0LCBldmVudE5hbWU6ICdNMS1QaHlzaWNhbCd9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICBvbkVycm9ySGFwcGVucygpIHtcclxuICAgICAgICB0aGlzLnRhc2tNYW5hZ2VyU2VydmljZS5mYWlsQ2FsbChmdW5jdGlvbiAoKSB7XHJcblxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGluaXRGb3JtKCkge1xyXG4gICAgICAgIHRoaXMudXNlckZpZWxkcyA9IFtcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAga2V5OiAnZW1haWwnLFxyXG4gICAgICAgICAgICAgICAgdHlwZTogJ2lucHV0JyxcclxuICAgICAgICAgICAgICAgIHRlbXBsYXRlT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgICAgIHR5cGU6ICdlbWFpbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdFbWFpbCBhZGRyZXNzJyxcclxuICAgICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcjogJ0VudGVyIGVtYWlsJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdwYXNzd29yZCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnaW5wdXQnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ3Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ1Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcjogJ1Bhc3N3b3JkJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdmaWxlJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdmaWxlJyxcclxuICAgICAgICAgICAgICAgIHRlbXBsYXRlT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgICAgIGxhYmVsOiAnRmlsZSBpbnB1dCcsXHJcbiAgICAgICAgICAgICAgICAgICAgZGVzY3JpcHRpb246ICdFeGFtcGxlIGJsb2NrLWxldmVsIGhlbHAgdGV4dCBoZXJlJyxcclxuICAgICAgICAgICAgICAgICAgICB1cmw6ICdodHRwczovL2V4YW1wbGUuY29tL3VwbG9hZCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAga2V5OiAnY2hlY2tlZCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnY2hlY2tib3gnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdDaGVjayBtZSBvdXQnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAyMi8wNy8xNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIFJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gUmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVGFza01hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGZhaWxDYWxsKGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5SZXNvdXJjZVNlcnZpY2VIYW5kbGVyKCkuZ2V0U1ZHKCk7XHJcbiAgICB9XHJcblxyXG4gICAgdGVzdFNlcnZpY2UoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlRhc2tTZXJ2aWNlSGFuZGxlcigpLmdldEZlZWRzKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8zLzIwMTYuXHJcbiAqL1xyXG5cclxuXHJcbid1c2Ugc3RyaWN0J1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRm9ybVZhbGlkYXRvciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc2NvcGUgPSAkc2NvcGU7XHJcblxyXG4gICAgICAgIC8vIEpTIGRvZXMgYSBhcmd1bWVudCBwYXNzIGJ5IHJlZmVyZW5jZVxyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IG51bGw7XHJcbiAgICAgICAgLy8gQSBjb3B5IHdpdGhvdXQgcmVmZXJlbmNlIGZyb20gdGhlIG9yaWdpbmFsIG9iamVjdFxyXG4gICAgICAgIHRoaXMub3JpZ2luYWxEYXRhID0gbnVsbDtcclxuICAgICAgICAvLyBBIENDIGFzIEpTT04gZm9yIGNvbXBhcmlzb24gUHVycG9zZVxyXG4gICAgICAgIHRoaXMub2JqZWN0QXNKU09OID0gbnVsbDtcclxuXHJcblxyXG4gICAgICAgIC8vIE9ubHkgZm9yIE1vZGFsIFdpbmRvd3NcclxuICAgICAgICB0aGlzLnJlbG9hZFJlcXVpcmVkID0gZmFsc2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuXHJcbiAgICAgICAgaWYgKCRzY29wZS4kb24pIHtcclxuICAgICAgICAgICAgJHNjb3BlLiRvbignbW9kYWwuY2xvc2luZycsIChldmVudCwgcmVhc29uLCBjbG9zZWQpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNsb3NlRGlhbG9nKGV2ZW50LCByZWFzb24sIGNsb3NlZClcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZXMgdGhlIEZvcm0gaW4gMyBpbnN0YW5jZXMsIG9uZSB0byBrZWVwIHRyYWNrIG9mIHRoZSBvcmlnaW5hbCBkYXRhLCBvdGhlciBpcyB0aGUgY3VycmVudCBvYmplY3QgYW5kXHJcbiAgICAgKiBhIEpTT04gZm9ybWF0IGZvciBjb21wYXJpc29uIHB1cnBvc2VcclxuICAgICAqIEBwYXJhbSBuZXdPYmplY3RJbnN0YW5jZVxyXG4gICAgICovXHJcbiAgICBzYXZlRm9ybShuZXdPYmplY3RJbnN0YW5jZSkge1xyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IG5ld09iamVjdEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMub3JpZ2luYWxEYXRhID0gYW5ndWxhci5jb3B5KG5ld09iamVjdEluc3RhbmNlLCB0aGlzLm9yaWdpbmFsRGF0YSk7XHJcbiAgICAgICAgdGhpcy5vYmplY3RBc0pTT04gPSBhbmd1bGFyLnRvSnNvbihuZXdPYmplY3RJbnN0YW5jZSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBHZXQgdGhlIEN1cnJlbnQgT2JqZWN0IG9uIGhpcyByZWZlcmVuY2UgRm9ybWF0XHJcbiAgICAgKiBAcmV0dXJucyB7bnVsbHwqfVxyXG4gICAgICovXHJcbiAgICBnZXRGb3JtKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmN1cnJlbnRPYmplY3Q7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBHZXQgdGhlIE9iamVjdCBhcyBKU09OIGZyb20gdGhlIE9yaWdpbmFsIERhdGFcclxuICAgICAqIEByZXR1cm5zIHtudWxsfHN0cmluZ3x1bmRlZmluZWR8c3RyaW5nfCp9XHJcbiAgICAgKi9cclxuICAgIGdldEZvcm1Bc0pTT04oKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMub2JqZWN0QXNKU09OO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICpcclxuICAgICAqIEBwYXJhbSBvYmpldFRvUmVzZXQgb2JqZWN0IHRvIHJlc2V0XHJcbiAgICAgKiBAcGFyYW0gb25SZXNldEZvcm0gY2FsbGJhY2tcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICByZXNldEZvcm0ob25SZXNldEZvcm0pIHtcclxuICAgICAgICB0aGlzLmN1cnJlbnRPYmplY3QgPSBhbmd1bGFyLmNvcHkodGhpcy5vcmlnaW5hbERhdGEsIHRoaXMuY3VycmVudE9iamVjdCk7XHJcbiAgICAgICAgdGhpcy5zYWZlQXBwbHkoKTtcclxuXHJcbiAgICAgICAgaWYob25SZXNldEZvcm0pIHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uUmVzZXRGb3JtKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmFsaWRhdGVzIGlmIHRoZSBjdXJyZW50IG9iamVjdCBkaWZmZXJzIGZyb20gd2hlcmUgaXQgd2FzIG9yaWdpbmFsbHkgc2F2ZWRcclxuICAgICAqIEByZXR1cm5zIHtib29sZWFufVxyXG4gICAgICovXHJcbiAgICBpc0RpcnR5KCkge1xyXG4gICAgICAgIHZhciBuZXdPYmplY3RJbnN0YW5jZSA9IGFuZ3VsYXIudG9Kc29uKHRoaXMuY3VycmVudE9iamVjdCk7XHJcbiAgICAgICAgcmV0dXJuIG5ld09iamVjdEluc3RhbmNlICE9PSB0aGlzLmdldEZvcm1Bc0pTT04oKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRoaXMgZnVuY3Rpb24gaXMgb25seSBhdmFpbGFibGUgd2hlbiB0aGUgRm9ybSBpcyBiZWluZyBjYWxsZWQgZnJvbSBhIERpYWxvZyBQb3BVcFxyXG4gICAgICovXHJcbiAgICBvbkNsb3NlRGlhbG9nKGV2ZW50LCByZWFzb24sIGNsb3NlZCkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ21vZGFsLmNsb3Npbmc6ICcgKyAoY2xvc2VkID8gJ2Nsb3NlJyA6ICdkaXNtaXNzJykgKyAnKCcgKyByZWFzb24gKyAnKScpO1xyXG4gICAgICAgIGlmICh0aGlzLmlzRGlydHkoKSAmJiByZWFzb24gIT09ICdjYW5jZWwtY29uZmlybWF0aW9uJyAmJiB0eXBlb2YgcmVhc29uICE9PSAnb2JqZWN0Jykge1xyXG4gICAgICAgICAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xyXG4gICAgICAgICAgICB0aGlzLmNvbmZpcm1DbG9zZUZvcm0oKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBIENvbmZpcm1hdGlvbiBEaWFsb2cgd2hlbiB0aGUgaW5mb3JtYXRpb24gY2FuIGJlIGxvc3RcclxuICAgICAqIEBwYXJhbSBldmVudFxyXG4gICAgICovXHJcbiAgICBjb25maXJtQ2xvc2VGb3JtKGV2ZW50KSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG4gICAgICAgICAgICBzaXplOiAnc20nLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIG1lc3NhZ2U6ICdDaGFuZ2VzIHlvdSBtYWRlIG1heSBub3QgYmUgc2F2ZWQuIERvIHlvdSB3YW50IHRvIGNvbnRpbnVlPydcclxuICAgICAgICAgICAgICAgICAgICB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsLWNvbmZpcm1hdGlvbicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVXRpbCB0byBjYWxsIHNhZmUgaWYgcmVxdWlyZWRcclxuICAgICAqIEBwYXJhbSBmblxyXG4gICAgICovXHJcbiAgICBzYWZlQXBwbHkoZm4pIHtcclxuICAgICAgICB2YXIgcGhhc2UgPSB0aGlzLnNjb3BlLiRyb290LiQkcGhhc2U7XHJcbiAgICAgICAgaWYocGhhc2UgPT09ICckYXBwbHknIHx8IHBoYXNlID09PSAnJGRpZ2VzdCcpIHtcclxuICAgICAgICAgICAgaWYoZm4gJiYgKHR5cGVvZihmbikgPT09ICdmdW5jdGlvbicpKSB7XHJcbiAgICAgICAgICAgICAgICBmbigpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy5zY29wZS4kYXBwbHkoZm4pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFV0aWwgdG8gUmVzZXQgYSBEcm9wZG93biBsaXN0IG9uIEtlbmRvXHJcbiAgICAgKi9cclxuXHJcbiAgICByZXNldERyb3BEb3duKHNlbGVjdG9ySW5zdGFuY2UsIHNlbGVjdGVkSWQsIGZvcmNlKSB7XHJcbiAgICAgICAgaWYoc2VsZWN0b3JJbnN0YW5jZSAmJiBzZWxlY3Rvckluc3RhbmNlLmRhdGFJdGVtcykge1xyXG4gICAgICAgICAgICBzZWxlY3Rvckluc3RhbmNlLmRhdGFJdGVtcygpLmZvckVhY2goKHZhbHVlLCBpbmRleCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoc2VsZWN0ZWRJZCA9PT0gdmFsdWUuaWQpIHtcclxuICAgICAgICAgICAgICAgICAgICBzZWxlY3Rvckluc3RhbmNlLnNlbGVjdChpbmRleCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgaWYoZm9yY2UpIHtcclxuICAgICAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2UudHJpZ2dlcignY2hhbmdlJyk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnNhZmVBcHBseSgpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMy8yMDE1LlxyXG4gKiBJbXBsZW1lbnRzIFJYIE9ic2VydmFibGUgdG8gZGlzcG9zZSBhbmQgdHJhY2sgYmV0dGVyIGVhY2ggY2FsbCB0byB0aGUgc2VydmVyXHJcbiAqIFRoZSBPYnNlcnZlciBzdWJzY3JpYmUgYSBwcm9taXNlLlxyXG4gKi9cclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SGFuZGxlciB7XHJcbiAgICBjb25zdHJ1Y3RvcihyeCkge1xyXG4gICAgICAgIHRoaXMucnggPSByeDtcclxuICAgICAgICB0aGlzLnByb21pc2UgPSBbXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENhbGxlZCBmcm9tIFJlc3RTZXJ2aWNlSGFuZGxlci5zdWJzY3JpYmVSZXF1ZXN0XHJcbiAgICAgKiBpdCB2ZXJpZnkgdGhhdCB0aGUgY2FsbCBpcyBiZWluZyBkb25lIHRvIHRoZSBzZXJ2ZXIgYW5kIHJldHVybiBhIHByb21pc2VcclxuICAgICAqIEBwYXJhbSByZXF1ZXN0XHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgc3Vic2NyaWJlUmVxdWVzdChyZXF1ZXN0LCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB2YXIgcnhPYnNlcnZhYmxlID0gdGhpcy5yeC5PYnNlcnZhYmxlLmZyb21Qcm9taXNlKHJlcXVlc3QpO1xyXG4gICAgICAgIC8vIFZlcmlmeSBpcyBub3QgYSBkdXBsaWNhdGUgY2FsbFxyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIHRoaXMuY2FuY2VsUmVxdWVzdChyeE9ic2VydmFibGUpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgLy8gU3Vic2NyaWJlIHRoZSByZXF1ZXN0XHJcbiAgICAgICAgdmFyIHJlc3VsdFN1YnNjcmliZSA9IHRoaXMuYWRkU3Vic2NyaWJlKHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICBpZiAocmVzdWx0U3Vic2NyaWJlICYmIHJlc3VsdFN1YnNjcmliZS5pc1N0b3BwZWQpIHtcclxuICAgICAgICAgICAgLy8gQW4gZXJyb3IgaGFwcGVucywgdHJhY2tlZCBieSBIdHRwSW50ZXJjZXB0b3JJbnRlcmZhY2VcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBhZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSA9IHJ4T2JzZXJ2YWJsZS5zdWJzY3JpYmUoXHJcbiAgICAgICAgICAgIChyZXNwb25zZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAoZXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLm9uU3Vic2NyaWJlZEVycm9yKGVycm9yLCByeE9ic2VydmFibGUsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAvLyBOTy1PUCBTdWJzY3JpYmUgY29tcGxldGVkXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICByZXR1cm4gdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICB9XHJcblxyXG4gICAgY2FuY2VsUmVxdWVzdChyeE9ic2VydmFibGUpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgICAgIHJ4T2JzZXJ2YWJsZS5kaXNwb3NlKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpIHtcclxuICAgICAgICByZXR1cm4gKHJ4T2JzZXJ2YWJsZSAmJiByeE9ic2VydmFibGUuX3AgJiYgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF0pO1xyXG4gICAgfVxyXG5cclxuICAgIG9uU3Vic2NyaWJlZFN1Y2Nlc3MocmVzcG9uc2UsIHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgICAgICBpZihvblN1Y2Nlc3Mpe1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKHJlc3BvbnNlLmRhdGEpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRocm93cyBpbW1lZGlhdGVseSBlcnJvciB3aGVuIHRoZSBwZXRpdGlvbiBjYWxsIGlzIHdyb25nXHJcbiAgICAgKiBvciB3aXRoIGEgZGVsYXkgaWYgdGhlIGNhbGwgaXMgdmFsaWRcclxuICAgICAqIEBwYXJhbSBlcnJvclxyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIG9uU3Vic2NyaWJlZEVycm9yKGVycm9yLCByeE9ic2VydmFibGUsIG9uRXJyb3IpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKG9uRXJyb3Ipe1xyXG4gICAgICAgICAgICByZXR1cm4gb25FcnJvcih7fSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBSZXN0U2VydmljZUhhbmRsZXIgZnJvbSAnLi9SZXN0U2VydmljZUhhbmRsZXIuanMnO1xyXG5cclxudmFyIFJlc3RBUElNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uUmVzdEFQSU1vZHVsZScsW10pO1xyXG5cclxuUmVzdEFQSU1vZHVsZS5zZXJ2aWNlKCdSZXN0U2VydmljZUhhbmRsZXInLCBbJyRsb2cnLCAnJGh0dHAnLCAnJHJlc291cmNlJywgJ3J4JywgUmVzdFNlcnZpY2VIYW5kbGVyXSk7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBSZXN0QVBJTW9kdWxlO1xyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzA4LzE1LlxyXG4gKiBJdCBhYnN0cmFjdCBlYWNoIG9uZSBvZiB0aGUgZXhpc3RpbmcgY2FsbCB0byB0aGUgQVBJLCBpdCBzaG91bGQgb25seSBjb250YWlucyB0aGUgY2FsbCBmdW5jdGlvbnMgYW5kIHJlZmVyZW5jZVxyXG4gKiB0byB0aGUgY2FsbGJhY2ssIG5vIGxvZ2ljIGF0IGFsbC5cclxuICpcclxuICovXHJcblxyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IFJlcXVlc3RIYW5kbGVyIGZyb20gJy4vUmVxdWVzdEhhbmRsZXIuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVzdFNlcnZpY2VIYW5kbGVyIHtcclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRodHRwLCAkcmVzb3VyY2UsIHJ4KSB7XHJcbiAgICAgICAgdGhpcy5yeCA9IHJ4O1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLmh0dHAgPSAkaHR0cDtcclxuICAgICAgICB0aGlzLnJlc291cmNlID0gJHJlc291cmNlO1xyXG4gICAgICAgIHRoaXMucHJlcGFyZUhlYWRlcnMoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnUmVzdFNlcnZpY2UgTG9hZGVkJyk7XHJcbiAgICAgICAgdGhpcy5yZXEgPSB7XHJcbiAgICAgICAgICAgIG1ldGhvZDogJycsXHJcbiAgICAgICAgICAgIHVybDogJycsXHJcbiAgICAgICAgICAgIGhlYWRlcnM6IHtcclxuICAgICAgICAgICAgICAgICdDb250ZW50LVR5cGUnOiAnYXBwbGljYXRpb24vanNvbjtjaGFyc2V0PVVURi04J1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkYXRhOiBbXVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUhlYWRlcnMoKSB7XHJcbiAgICAgICAgdGhpcy5odHRwLmRlZmF1bHRzLmhlYWRlcnMucG9zdFsnQ29udGVudC1UeXBlJ10gPSAnYXBwbGljYXRpb24veC13d3ctZm9ybS11cmxlbmNvZGVkJztcclxuICAgIH1cclxuXHJcbiAgICBUYXNrU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgZ2V0RmVlZHM6IChjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCd0ZXN0L21vY2t1cERhdGEvVGFza01hbmFnZXIvdGFza01hbmFnZXJMaXN0Lmpzb24nKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBsaWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRMaWNlbnNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZXMnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9lbnZpcm9ubWVudCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRQcm9qZWN0RGF0YVNvdXJjZTogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UvcHJvamVjdCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRMaWNlbnNlTGlzdDogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL2xpY2Vuc2UnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3Q6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS9yZXF1ZXN0JztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLmRhdGEgPSBkYXRhO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGFwcGx5TGljZW5zZTogIChsaWNlbnNlSWQsIGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2xvYWQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0SGFzaENvZGU6ICAobGljZW5zZUlkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdHRVQnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2hhc2gnO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIC8vLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICAgICAgICAgICAgcmVzdWJtaXRMaWNlbnNlUmVxdWVzdDogKGRhdGEsIGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzLz8/Pyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAucG9zdCgnLi4vdGVzdC9tb2NrdXBEYXRhL0xpY2Vuc2VBZG1pbi9saWNlbnNlQWRtaW5MaXN0Lmpzb24nLCBkYXRhKSwgY2FsbGJhY2spO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbWFpbFJlcXVlc3Q6IChkYXRhLCBjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy8/Pz8nO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLnBvc3QoJy4uL3Rlc3QvbW9ja3VwRGF0YS9MaWNlbnNlQWRtaW4vbGljZW5zZUFkbWluTGlzdC5qc29uJywgZGF0YSksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGVsZXRlTGljZW5zZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgcmVxdWVzdEltcG9ydDogIChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0TGljZW5zZUxpc3Q6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0UHJvamVjdERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL3Byb2plY3QnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9lbnZpcm9ubWVudCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRLZXlDb2RlOiAgKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnR0VUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2tleSc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc2F2ZUxpY2Vuc2U6IChsaWNlbnNlSWQsIGxpY2Vuc2VNb2RpZmllZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUFVUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQ7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gbGljZW5zZU1vZGlmaWVkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHJldm9rZUxpY2Vuc2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdERUxFVEUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGFjdGl2YXRlTGljZW5zZTogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9hY3RpdmF0ZSc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgbm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldE5vdGljZUxpc3Q6IChvblN1Y2Nlc3MpID0+IHsgLy8gcmVhbCB3cyBleGFtcGxlXHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3Mvbm90aWNlcycpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOb3RpY2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3Mvbm90aWNlcyc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlZGl0Tm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUFVUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3Mvbm90aWNlcy8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBkZWxldGVOb3RpY2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdERUxFVEUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9ub3RpY2VzLycrZGF0YS5pZDtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICogRVM2IEludGVyY2VwdG9yIGNhbGxzIGlubmVyIG1ldGhvZHMgaW4gYSBnbG9iYWwgc2NvcGUsIHRoZW4gdGhlIFwidGhpc1wiIGlzIGJlaW5nIGxvc3RcclxuICogaW4gdGhlIGRlZmluaXRpb24gb2YgdGhlIENsYXNzIGZvciBpbnRlcmNlcHRvcnMgb25seVxyXG4gKiBUaGlzIGlzIGEgaW50ZXJmYWNlIHRoYXQgdGFrZSBjYXJlIG9mIHRoZSBpc3N1ZS5cclxuICovXHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgLyogaW50ZXJmYWNlKi8gY2xhc3MgSHR0cEludGVyY2VwdG9yIHtcclxuICAgIGNvbnN0cnVjdG9yKG1ldGhvZFRvQmluZCkge1xyXG4gICAgICAgIC8vIElmIG5vdCBtZXRob2QgdG8gYmluZCwgd2UgYXNzdW1lIG91ciBpbnRlcmNlcHRvciBpcyB1c2luZyBhbGwgdGhlIGlubmVyIGZ1bmN0aW9uc1xyXG4gICAgICAgIGlmKCFtZXRob2RUb0JpbmQpIHtcclxuICAgICAgICAgICAgWydyZXF1ZXN0JywgJ3JlcXVlc3RFcnJvcicsICdyZXNwb25zZScsICdyZXNwb25zZUVycm9yJ11cclxuICAgICAgICAgICAgICAgIC5mb3JFYWNoKChtZXRob2QpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzW21ldGhvZF0pIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpc1ttZXRob2RdID0gdGhpc1ttZXRob2RdLmJpbmQodGhpcyk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgLy8gbWV0aG9kVG9CaW5kIHJlZmVyZW5jZSB0byBhIHNpbmdsZSBjaGlsZCBjbGFzc1xyXG4gICAgICAgICAgICB0aGlzW21ldGhvZFRvQmluZF0gPSB0aGlzW21ldGhvZFRvQmluZF0uYmluZCh0aGlzKTtcclxuICAgICAgICB9XHJcblxyXG4gICAgfVxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICogVXNlIHRoaXMgbW9kdWxlIHRvIG1vZGlmeSBhbnl0aGluZyByZWxhdGVkIHRvIHRoZSBIZWFkZXJzIGFuZCBSZXF1ZXN0XHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcblxyXG5cclxudmFyIEhUVFBNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uSFRUUE1vZHVsZScsIFsnbmdSZXNvdXJjZSddKS5jb25maWcoWyckaHR0cFByb3ZpZGVyJywgZnVuY3Rpb24oJGh0dHBQcm92aWRlcil7XHJcblxyXG4gICAgLy9pbml0aWFsaXplIGdldCBpZiBub3QgdGhlcmVcclxuICAgIGlmICghJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldCkge1xyXG4gICAgICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXQgPSB7fTtcclxuICAgIH1cclxuXHJcbiAgICAvL0Rpc2FibGUgSUUgYWpheCByZXF1ZXN0IGNhY2hpbmdcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ0lmLU1vZGlmaWVkLVNpbmNlJ10gPSAnTW9uLCAyNiBKdWwgMTk5NyAwNTowMDowMCBHTVQnO1xyXG4gICAgLy8gZXh0cmFcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ0NhY2hlLUNvbnRyb2wnXSA9ICduby1jYWNoZSc7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydQcmFnbWEnXSA9ICduby1jYWNoZSc7XHJcblxyXG5cclxuICAgIC8vIEluamVjdHMgb3VyIEludGVyY2VwdG9ycyBmb3IgUmVxdWVzdFxyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgIC8vIEluamVjdHMgb3VyIEludGVyY2VwdG9ycyBmb3IgUmVzcG9uc2VcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuXHJcblxyXG59XSk7XHJcblxyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IEhUVFBNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSXQgaW1wbGVtZW50IGFuIGFic3RyYWN0IGNhbGwgdG8gSFRUUCBJbnRlcmNlcHRvcnMgdG8gbWFuYWdlIGVycm9yIGhhbmRsZXJcclxuICogU29tZXRpbWVzIGEgcmVxdWVzdCBjYW4ndCBiZSBzZW50IG9yIGl0IGlzIHJlamVjdGVkIGJ5IGFuIGludGVyY2VwdG9yLlxyXG4gKiBSZXF1ZXN0IGVycm9yIGludGVyY2VwdG9yIGNhcHR1cmVzIHJlcXVlc3RzIHRoYXQgaGF2ZSBiZWVuIGNhbmNlbGVkIGJ5IGEgcHJldmlvdXMgcmVxdWVzdCBpbnRlcmNlcHRvci5cclxuICogSXQgY2FuIGJlIHVzZWQgaW4gb3JkZXIgdG8gcmVjb3ZlciB0aGUgcmVxdWVzdCBhbmQgc29tZXRpbWVzIHVuZG8gdGhpbmdzIHRoYXQgaGF2ZSBiZWVuIHNldCB1cCBiZWZvcmUgYSByZXF1ZXN0LFxyXG4gKiBsaWtlIHJlbW92aW5nIG92ZXJsYXlzIGFuZCBsb2FkaW5nIGluZGljYXRvcnMsIGVuYWJsaW5nIGJ1dHRvbnMgYW5kIGZpZWxkcyBhbmQgc28gb24uXHJcbiAqL1xyXG5cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVxdWVzdEVycm9yJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXF1ZXN0RXJyb3IocmVqZWN0aW9uKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy9pZiAoY2FuUmVjb3ZlcihyZWplY3Rpb24pKSB7XHJcbiAgICAgICAgLy8gICAgcmV0dXJuIHJlc3BvbnNlT3JOZXdQcm9taXNlXHJcbiAgICAgICAgLy99XHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVqZWN0aW9uKTtcclxuXHJcbiAgICAgICAgcmV0dXJuIHRoaXMucS5yZWplY3QocmVqZWN0aW9uKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5FcnJvcigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJdCBpbXBsZW1lbnQgYW4gYWJzdHJhY3QgY2FsbCB0byBIVFRQIEludGVyY2VwdG9ycyB0byBtYW5hZ2Ugb25seSByZXF1ZXN0XHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3JlcXVlc3QnKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXF1ZXN0KGNvbmZpZykge1xyXG4gICAgICAgIC8vIFdlIGNhbiBhZGQgaGVhZGVycyBpZiBvbiB0aGUgaW5jb21pbmcgcmVxdWVzdCBtYWRlIGl0IHdlIGhhdmUgdGhlIHRva2VuIGluc2lkZVxyXG4gICAgICAgIC8vIGRlZmluZWQgYnkgc29tZSBjb25kaXRpb25zXHJcbiAgICAgICAgLy9jb25maWcuaGVhZGVyc1sneC1zZXNzaW9uLXRva2VuJ10gPSBteS50b2tlbjtcclxuXHJcbiAgICAgICAgY29uZmlnLnJlcXVlc3RUaW1lc3RhbXAgPSBuZXcgRGF0ZSgpLmdldFRpbWUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkoY29uZmlnKTtcclxuXHJcbiAgICAgICAgcmV0dXJuIGNvbmZpZyB8fCB0aGlzLnEud2hlbihjb25maWcpO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlblJlcXVlc3QoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSWYgYmFja2VuZCBjYWxsIGZhaWxzIG9yIGl0IG1pZ2h0IGJlIHJlamVjdGVkIGJ5IGEgcmVxdWVzdCBpbnRlcmNlcHRvciBvciBieSBhIHByZXZpb3VzIHJlc3BvbnNlIGludGVyY2VwdG9yO1xyXG4gKiBJbiB0aG9zZSBjYXNlcywgcmVzcG9uc2UgZXJyb3IgaW50ZXJjZXB0b3IgY2FuIGhlbHAgdXMgdG8gcmVjb3ZlciB0aGUgYmFja2VuZCBjYWxsLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3Jlc3BvbnNlRXJyb3InKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXNwb25zZUVycm9yKHJlamVjdGlvbikge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vaWYgKGNhblJlY292ZXIocmVqZWN0aW9uKSkge1xyXG4gICAgICAgIC8vICAgIHJldHVybiByZXNwb25zZU9yTmV3UHJvbWlzZVxyXG4gICAgICAgIC8vIH1cclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVqZWN0aW9uKTtcclxuICAgICAgICByZXR1cm4gdGhpcy5xLnJlamVjdChyZWplY3Rpb24pO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlbkVycm9yKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIFRoaXMgbWV0aG9kIGlzIGNhbGxlZCByaWdodCBhZnRlciAkaHR0cCByZWNlaXZlcyB0aGUgcmVzcG9uc2UgZnJvbSB0aGUgYmFja2VuZCxcclxuICogc28geW91IGNhbiBtb2RpZnkgdGhlIHJlc3BvbnNlIGFuZCBtYWtlIG90aGVyIGFjdGlvbnMuIFRoaXMgZnVuY3Rpb24gcmVjZWl2ZXMgYSByZXNwb25zZSBvYmplY3QgYXMgYSBwYXJhbWV0ZXJcclxuICogYW5kIGhhcyB0byByZXR1cm4gYSByZXNwb25zZSBvYmplY3Qgb3IgYSBwcm9taXNlLiBUaGUgcmVzcG9uc2Ugb2JqZWN0IGluY2x1ZGVzXHJcbiAqIHRoZSByZXF1ZXN0IGNvbmZpZ3VyYXRpb24sIGhlYWRlcnMsIHN0YXR1cyBhbmQgZGF0YSB0aGF0IHJldHVybmVkIGZyb20gdGhlIGJhY2tlbmQuXHJcbiAqIFJldHVybmluZyBhbiBpbnZhbGlkIHJlc3BvbnNlIG9iamVjdCBvciBwcm9taXNlIHRoYXQgd2lsbCBiZSByZWplY3RlZCwgd2lsbCBtYWtlIHRoZSAkaHR0cCBjYWxsIHRvIGZhaWwuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3Jlc3BvbnNlJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3BvbnNlKHJlc3BvbnNlKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIHN1Y2Nlc3NcclxuXHJcbiAgICAgICAgcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlc3BvbnNlKTtcclxuICAgICAgICByZXR1cm4gcmVzcG9uc2UgfHwgdGhpcy5xLndoZW4ocmVzcG9uc2UpO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlblJlc3BvbnNlKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcbn1cclxuXHJcbiJdfQ==
