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

},{"../modules/header/HeaderModule.js":18,"../modules/licenseAdmin/LicenseAdminModule.js":19,"../modules/licenseManager/LicenseManagerModule.js":27,"../modules/noticeManager/NoticeManagerModule.js":32,"../modules/taskManager/TaskManagerModule.js":36,"../services/RestAPI/RestAPIModule.js":42,"../services/http/HTTPModule.js":46,"angular":"angular","angular-animate":"angular-animate","angular-formly":"angular-formly","angular-formly-templates-bootstrap":"angular-formly-templates-bootstrap","angular-mocks":"angular-mocks","angular-resource":"angular-resource","angular-sanitize":"angular-sanitize","angular-translate":"angular-translate","angular-translate-loader-partial":"angular-translate-loader-partial","angular-ui-bootstrap":"angular-ui-bootstrap","api-check":"api-check","ngClipboard":7,"rx-angular":"rx-angular","ui-router":"ui-router"}],12:[function(require,module,exports){
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
						method: {
								name: params.license.method.name,
								max: params.license.method.max
						},
						projectName: params.license.project.name,
						clientName: params.license.client.name,
						email: params.license.email,
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
                columns: [{ field: 'licenseId', hidden: true }, { field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseAdminList.onLicenseDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project', template: '<span style="text-transform: capitalize;">#=((data.project && data.project.name)? data.project.name.toLowerCase(): "" )#</span>' }, { field: 'email', title: 'Contact Email' }, { field: 'status', title: 'Status', template: '<span style="text-transform: capitalize;">#=((data.status)? data.status.toLowerCase(): "" )#</span>' }, { field: 'type.name', title: 'Type', template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#' }, { field: 'method.name', title: 'Method', template: '<span style="text-transform: capitalize;">#=((data.method && data.method.name)? data.method.name.toLowerCase(): "" )#</span>' }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'requestDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.requestDate | convertDateIntoTimeZone }}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.expirationDate | convertDateIntoTimeZone }}' }, { field: 'environment', title: 'Environment', template: '<span style="text-transform: capitalize;">#=((data.environment)? data.environment.toLowerCase(): "" )#</span>' }],
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
LicenseManagerModule.controller('LicenseManagerDetail', ['$log', '$scope', 'LicenseManagerService', 'UserPreferencesService', '$uibModal', '$uibModalInstance', 'params', _LicenseManagerDetail2.default]);

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
                columns: [{ field: 'id', hidden: true }, { field: 'action', filterable: false, title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="licenseManagerList.onLicenseManagerDetails(this)"><span class="glyphicon glyphicon-pencil"></span></button>' }, { field: 'owner.name', title: 'Owner' }, { field: 'client.name', title: 'Client' }, { field: 'project.name', title: 'Project', template: '<span style="text-transform: capitalize;">#=((data.project && data.project.name)? data.project.name.toLowerCase(): "" )#</span>' }, { field: 'email', title: 'Contact Email' }, { field: 'status', title: 'Status', template: '<span style="text-transform: capitalize;">#=((data.status)? data.status.toLowerCase(): "" )#</span>' }, { field: 'type.name', title: 'Type', template: '#if(data.type && data.type.name === "MULTI_PROJECT"){# Global # } else {# Single #}#' }, { field: 'method.name', title: 'Method', template: '<span style="text-transform: capitalize;">#=((data.method && data.method.name)? data.method.name.toLowerCase(): "" )#</span>' }, { field: 'method.max', title: 'Server/Tokens' }, { field: 'activationDate', title: 'Inception', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.activationDate | convertDateIntoTimeZone }}' }, { field: 'expirationDate', title: 'Expiration', type: 'date', format: '{0:dd/MMM/yyyy}', template: '{{ dataItem.expirationDate | convertDateIntoTimeZone }}' }, { field: 'environment', title: 'Environment', template: '<span style="text-transform: capitalize;">#=((data.environment)? data.environment.toLowerCase(): "" )#</span>' }, { field: 'gracePeriodDays', hidden: true }],
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
    }, {
        key: 'getTimeZoneConfiguration',
        value: function getTimeZoneConfiguration(onSuccess) {
            this.restService.commonServiceHandler().getTimeZoneConfiguration(function (data) {
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

var _UserPreferencesService = require('./UserPreferencesService.js');

var _UserPreferencesService2 = _interopRequireDefault(_UserPreferencesService);

function _interopRequireDefault(obj) {
  return obj && obj.__esModule ? obj : { default: obj };
}

var RestAPIModule = _angular2.default.module('TDSTM.RestAPIModule', []);

RestAPIModule.service('RestServiceHandler', ['$log', '$http', '$resource', 'rx', _RestServiceHandler2.default]);
RestAPIModule.service('UserPreferencesService', ['$log', 'RestServiceHandler', _UserPreferencesService2.default]);

exports.default = RestAPIModule;

},{"./RestServiceHandler.js":43,"./UserPreferencesService.js":44,"angular":"angular"}],43:[function(require,module,exports){
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

},{"./RequestHandler.js":41}],44:[function(require,module,exports){
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

},{}],45:[function(require,module,exports){
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

},{}],46:[function(require,module,exports){
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

},{"./HTTPRequestErrorHandlerInterceptor.js":47,"./HTTPRequestHandlerInterceptor.js":48,"./HTTPResponseErrorHandlerInterceptor.js":49,"./HTTPResponseHandlerInterceptor.js":50,"angular":"angular"}],47:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":45}],48:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":45}],49:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":45}],50:[function(require,module,exports){
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

},{"./HTTPInterceptorInterface.js":45}]},{},[15])
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJub2RlX21vZHVsZXMvY2xpcGJvYXJkL2xpYi9jbGlwYm9hcmQtYWN0aW9uLmpzIiwibm9kZV9tb2R1bGVzL2NsaXBib2FyZC9saWIvY2xpcGJvYXJkLmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9jbG9zZXN0LmpzIiwibm9kZV9tb2R1bGVzL2RlbGVnYXRlL3NyYy9kZWxlZ2F0ZS5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9pcy5qcyIsIm5vZGVfbW9kdWxlcy9nb29kLWxpc3RlbmVyL3NyYy9saXN0ZW4uanMiLCJub2RlX21vZHVsZXMvbmdDbGlwYm9hcmQvZGlzdC9uZ2NsaXBib2FyZC5qcyIsIm5vZGVfbW9kdWxlcy9zZWxlY3Qvc3JjL3NlbGVjdC5qcyIsIm5vZGVfbW9kdWxlcy90aW55LWVtaXR0ZXIvaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGNvbmZpZ1xcQW5ndWxhclByb3ZpZGVySGVscGVyLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxjb25maWdcXEFwcC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcZGlyZWN0aXZlc1xcaW5kZXguanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxNb2RhbFdpbmRvd0FjdGl2YXRpb24uanMiLCJ3ZWItYXBwXFxhcHAtanNcXGRpcmVjdGl2ZXNcXHRvb2xzXFxUb2FzdEhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1haW4uanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGRpYWxvZ0FjdGlvblxcRGlhbG9nQWN0aW9uLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxoZWFkZXJcXEhlYWRlckNvbnRyb2xsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGhlYWRlclxcSGVhZGVyTW9kdWxlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlQWRtaW5cXExpY2Vuc2VBZG1pbk1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxhcHBseUxpY2Vuc2VLZXlcXEFwcGx5TGljZW5zZUtleS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxjcmVhdGVkXFxDcmVhdGVkTGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxkZXRhaWxcXExpY2Vuc2VEZXRhaWwuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VBZG1pblxcbGlzdFxcTGljZW5zZUFkbWluTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxtYW51YWxseVJlcXVlc3RcXE1hbnVhbGx5UmVxdWVzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxyZXF1ZXN0XFxSZXF1ZXN0TGljZW5zZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZUFkbWluXFxzZXJ2aWNlXFxMaWNlbnNlQWRtaW5TZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcTGljZW5zZU1hbmFnZXJNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXGxpY2Vuc2VNYW5hZ2VyXFxkZXRhaWxcXExpY2Vuc2VNYW5hZ2VyRGV0YWlsLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxcbGlzdFxcTGljZW5zZU1hbmFnZXJMaXN0LmpzIiwid2ViLWFwcFxcYXBwLWpzXFxtb2R1bGVzXFxsaWNlbnNlTWFuYWdlclxccmVxdWVzdEltcG9ydFxcUmVxdWVzdEltcG9ydC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbGljZW5zZU1hbmFnZXJcXHNlcnZpY2VcXExpY2Vuc2VNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcTm90aWNlTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcZWRpdFxcRWRpdE5vdGljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcbGlzdFxcTm90aWNlTGlzdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcbm90aWNlTWFuYWdlclxcc2VydmljZVxcTm90aWNlTWFuYWdlclNlcnZpY2UuanMiLCJ3ZWItYXBwXFxhcHAtanNcXG1vZHVsZXNcXHRhc2tNYW5hZ2VyXFxUYXNrTWFuYWdlck1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGVkaXRcXFRhc2tNYW5hZ2VyRWRpdC5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXGxpc3RcXFRhc2tNYW5hZ2VyQ29udHJvbGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdGFza01hbmFnZXJcXHNlcnZpY2VcXFRhc2tNYW5hZ2VyU2VydmljZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcbW9kdWxlc1xcdXRpbHNcXGZvcm1cXEZvcm1WYWxpZGF0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXF1ZXN0SGFuZGxlci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXFJlc3RBUElcXFJlc3RBUElNb2R1bGUuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxSZXN0U2VydmljZUhhbmRsZXIuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxSZXN0QVBJXFxVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUEludGVyY2VwdG9ySW50ZXJmYWNlLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUE1vZHVsZS5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IuanMiLCJ3ZWItYXBwXFxhcHAtanNcXHNlcnZpY2VzXFxodHRwXFxIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvci5qcyIsIndlYi1hcHBcXGFwcC1qc1xcc2VydmljZXNcXGh0dHBcXEhUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzIiwid2ViLWFwcFxcYXBwLWpzXFxzZXJ2aWNlc1xcaHR0cFxcSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0FDQUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDcE9BO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDeExBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDNUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUM1Q0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNqREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQy9GQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNoREE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDakNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOzs7O0FDbEVBOzs7Ozs7Ozs7O0FBVUEsSUFBSSxRQUFRLFFBQVosQUFBWSxBQUFROztBQUVwQjs7Ozs7QUFLQSxNQUFBLEFBQU0sWUFBWSxVQUFBLEFBQVUsU0FBVixBQUFtQixJQUFJLEFBQ3JDO0FBQ0E7O1FBQUksUUFBUSxRQUFBLEFBQVEsTUFBcEIsQUFBMEIsQUFDMUI7UUFBSSxVQUFBLEFBQVUsWUFBWSxVQUExQixBQUFvQyxXQUFXLEFBQzND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QUFMLEFBQVcsQUFDWDtjQUFBLEFBQUssSUFBTCxBQUFTLEFBQ1Q7Y0FBQSxBQUFLLFFBQVEsTUFBQSxBQUFLLEVBQWxCLEFBQWEsQUFBTyxBQUNwQjtjQUFBLEFBQUssSUFBTCxBQUFTLE1BTGEsQUFLdEIsQUFBZTtlQUNsQjs7Ozs7aUMsQUFFUSxXQUFVLEFBQ2Y7QUFFQTs7c0JBQUEsQUFBUyxPQUFULEFBQWdCLG9CQUFvQixJQUFBLEFBQUksT0FBeEMsQUFBb0MsQUFBVyxBQUUvQzs7aUJBQUEsQUFBSyxNQUFMLEFBQVcsT0FBWCxBQUFrQixBQUNsQjttQkFBTyxhQUFZLEtBQUEsQUFBSyxFQUFMLEFBQU8sS0FBMUIsQUFBbUIsQUFBWSxBQUNsQzs7Ozt5Q0FFZ0IsQUFDYjttQkFBTyxLQUFBLEFBQUssTUFBWixBQUFrQixBQUNyQjs7Ozs7R0FwQnVELGM7O2tCLEFBQXZDIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIihmdW5jdGlvbiAoZ2xvYmFsLCBmYWN0b3J5KSB7XG4gICAgaWYgKHR5cGVvZiBkZWZpbmUgPT09IFwiZnVuY3Rpb25cIiAmJiBkZWZpbmUuYW1kKSB7XG4gICAgICAgIGRlZmluZShbJ21vZHVsZScsICdzZWxlY3QnXSwgZmFjdG9yeSk7XG4gICAgfSBlbHNlIGlmICh0eXBlb2YgZXhwb3J0cyAhPT0gXCJ1bmRlZmluZWRcIikge1xuICAgICAgICBmYWN0b3J5KG1vZHVsZSwgcmVxdWlyZSgnc2VsZWN0JykpO1xuICAgIH0gZWxzZSB7XG4gICAgICAgIHZhciBtb2QgPSB7XG4gICAgICAgICAgICBleHBvcnRzOiB7fVxuICAgICAgICB9O1xuICAgICAgICBmYWN0b3J5KG1vZCwgZ2xvYmFsLnNlbGVjdCk7XG4gICAgICAgIGdsb2JhbC5jbGlwYm9hcmRBY3Rpb24gPSBtb2QuZXhwb3J0cztcbiAgICB9XG59KSh0aGlzLCBmdW5jdGlvbiAobW9kdWxlLCBfc2VsZWN0KSB7XG4gICAgJ3VzZSBzdHJpY3QnO1xuXG4gICAgdmFyIF9zZWxlY3QyID0gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChfc2VsZWN0KTtcblxuICAgIGZ1bmN0aW9uIF9pbnRlcm9wUmVxdWlyZURlZmF1bHQob2JqKSB7XG4gICAgICAgIHJldHVybiBvYmogJiYgb2JqLl9fZXNNb2R1bGUgPyBvYmogOiB7XG4gICAgICAgICAgICBkZWZhdWx0OiBvYmpcbiAgICAgICAgfTtcbiAgICB9XG5cbiAgICB2YXIgX3R5cGVvZiA9IHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiB0eXBlb2YgU3ltYm9sLml0ZXJhdG9yID09PSBcInN5bWJvbFwiID8gZnVuY3Rpb24gKG9iaikge1xuICAgICAgICByZXR1cm4gdHlwZW9mIG9iajtcbiAgICB9IDogZnVuY3Rpb24gKG9iaikge1xuICAgICAgICByZXR1cm4gb2JqICYmIHR5cGVvZiBTeW1ib2wgPT09IFwiZnVuY3Rpb25cIiAmJiBvYmouY29uc3RydWN0b3IgPT09IFN5bWJvbCAmJiBvYmogIT09IFN5bWJvbC5wcm90b3R5cGUgPyBcInN5bWJvbFwiIDogdHlwZW9mIG9iajtcbiAgICB9O1xuXG4gICAgZnVuY3Rpb24gX2NsYXNzQ2FsbENoZWNrKGluc3RhbmNlLCBDb25zdHJ1Y3Rvcikge1xuICAgICAgICBpZiAoIShpbnN0YW5jZSBpbnN0YW5jZW9mIENvbnN0cnVjdG9yKSkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIkNhbm5vdCBjYWxsIGEgY2xhc3MgYXMgYSBmdW5jdGlvblwiKTtcbiAgICAgICAgfVxuICAgIH1cblxuICAgIHZhciBfY3JlYXRlQ2xhc3MgPSBmdW5jdGlvbiAoKSB7XG4gICAgICAgIGZ1bmN0aW9uIGRlZmluZVByb3BlcnRpZXModGFyZ2V0LCBwcm9wcykge1xuICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBwcm9wcy5sZW5ndGg7IGkrKykge1xuICAgICAgICAgICAgICAgIHZhciBkZXNjcmlwdG9yID0gcHJvcHNbaV07XG4gICAgICAgICAgICAgICAgZGVzY3JpcHRvci5lbnVtZXJhYmxlID0gZGVzY3JpcHRvci5lbnVtZXJhYmxlIHx8IGZhbHNlO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuY29uZmlndXJhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBpZiAoXCJ2YWx1ZVwiIGluIGRlc2NyaXB0b3IpIGRlc2NyaXB0b3Iud3JpdGFibGUgPSB0cnVlO1xuICAgICAgICAgICAgICAgIE9iamVjdC5kZWZpbmVQcm9wZXJ0eSh0YXJnZXQsIGRlc2NyaXB0b3Iua2V5LCBkZXNjcmlwdG9yKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBmdW5jdGlvbiAoQ29uc3RydWN0b3IsIHByb3RvUHJvcHMsIHN0YXRpY1Byb3BzKSB7XG4gICAgICAgICAgICBpZiAocHJvdG9Qcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvci5wcm90b3R5cGUsIHByb3RvUHJvcHMpO1xuICAgICAgICAgICAgaWYgKHN0YXRpY1Byb3BzKSBkZWZpbmVQcm9wZXJ0aWVzKENvbnN0cnVjdG9yLCBzdGF0aWNQcm9wcyk7XG4gICAgICAgICAgICByZXR1cm4gQ29uc3RydWN0b3I7XG4gICAgICAgIH07XG4gICAgfSgpO1xuXG4gICAgdmFyIENsaXBib2FyZEFjdGlvbiA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgLyoqXG4gICAgICAgICAqIEBwYXJhbSB7T2JqZWN0fSBvcHRpb25zXG4gICAgICAgICAqL1xuICAgICAgICBmdW5jdGlvbiBDbGlwYm9hcmRBY3Rpb24ob3B0aW9ucykge1xuICAgICAgICAgICAgX2NsYXNzQ2FsbENoZWNrKHRoaXMsIENsaXBib2FyZEFjdGlvbik7XG5cbiAgICAgICAgICAgIHRoaXMucmVzb2x2ZU9wdGlvbnMob3B0aW9ucyk7XG4gICAgICAgICAgICB0aGlzLmluaXRTZWxlY3Rpb24oKTtcbiAgICAgICAgfVxuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBEZWZpbmVzIGJhc2UgcHJvcGVydGllcyBwYXNzZWQgZnJvbSBjb25zdHJ1Y3Rvci5cbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG5cblxuICAgICAgICBfY3JlYXRlQ2xhc3MoQ2xpcGJvYXJkQWN0aW9uLCBbe1xuICAgICAgICAgICAga2V5OiAncmVzb2x2ZU9wdGlvbnMnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlc29sdmVPcHRpb25zKCkge1xuICAgICAgICAgICAgICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiB7fTtcblxuICAgICAgICAgICAgICAgIHRoaXMuYWN0aW9uID0gb3B0aW9ucy5hY3Rpb247XG4gICAgICAgICAgICAgICAgdGhpcy5lbWl0dGVyID0gb3B0aW9ucy5lbWl0dGVyO1xuICAgICAgICAgICAgICAgIHRoaXMudGFyZ2V0ID0gb3B0aW9ucy50YXJnZXQ7XG4gICAgICAgICAgICAgICAgdGhpcy50ZXh0ID0gb3B0aW9ucy50ZXh0O1xuICAgICAgICAgICAgICAgIHRoaXMudHJpZ2dlciA9IG9wdGlvbnMudHJpZ2dlcjtcblxuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gJyc7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2luaXRTZWxlY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGluaXRTZWxlY3Rpb24oKSB7XG4gICAgICAgICAgICAgICAgaWYgKHRoaXMudGV4dCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnNlbGVjdEZha2UoKTtcbiAgICAgICAgICAgICAgICB9IGVsc2UgaWYgKHRoaXMudGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0VGFyZ2V0KCk7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdzZWxlY3RGYWtlJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBzZWxlY3RGYWtlKCkge1xuICAgICAgICAgICAgICAgIHZhciBfdGhpcyA9IHRoaXM7XG5cbiAgICAgICAgICAgICAgICB2YXIgaXNSVEwgPSBkb2N1bWVudC5kb2N1bWVudEVsZW1lbnQuZ2V0QXR0cmlidXRlKCdkaXInKSA9PSAncnRsJztcblxuICAgICAgICAgICAgICAgIHRoaXMucmVtb3ZlRmFrZSgpO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrID0gZnVuY3Rpb24gKCkge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gX3RoaXMucmVtb3ZlRmFrZSgpO1xuICAgICAgICAgICAgICAgIH07XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlciA9IGRvY3VtZW50LmJvZHkuYWRkRXZlbnRMaXN0ZW5lcignY2xpY2snLCB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2spIHx8IHRydWU7XG5cbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtID0gZG9jdW1lbnQuY3JlYXRlRWxlbWVudCgndGV4dGFyZWEnKTtcbiAgICAgICAgICAgICAgICAvLyBQcmV2ZW50IHpvb21pbmcgb24gaU9TXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5mb250U2l6ZSA9ICcxMnB0JztcbiAgICAgICAgICAgICAgICAvLyBSZXNldCBib3ggbW9kZWxcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLmJvcmRlciA9ICcwJztcbiAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtLnN0eWxlLnBhZGRpbmcgPSAnMCc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5tYXJnaW4gPSAnMCc7XG4gICAgICAgICAgICAgICAgLy8gTW92ZSBlbGVtZW50IG91dCBvZiBzY3JlZW4gaG9yaXpvbnRhbGx5XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS5wb3NpdGlvbiA9ICdhYnNvbHV0ZSc7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZVtpc1JUTCA/ICdyaWdodCcgOiAnbGVmdCddID0gJy05OTk5cHgnO1xuICAgICAgICAgICAgICAgIC8vIE1vdmUgZWxlbWVudCB0byB0aGUgc2FtZSBwb3NpdGlvbiB2ZXJ0aWNhbGx5XG4gICAgICAgICAgICAgICAgdmFyIHlQb3NpdGlvbiA9IHdpbmRvdy5wYWdlWU9mZnNldCB8fCBkb2N1bWVudC5kb2N1bWVudEVsZW1lbnQuc2Nyb2xsVG9wO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0uYWRkRXZlbnRMaXN0ZW5lcignZm9jdXMnLCB3aW5kb3cuc2Nyb2xsVG8oMCwgeVBvc2l0aW9uKSk7XG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zdHlsZS50b3AgPSB5UG9zaXRpb24gKyAncHgnO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5mYWtlRWxlbS5zZXRBdHRyaWJ1dGUoJ3JlYWRvbmx5JywgJycpO1xuICAgICAgICAgICAgICAgIHRoaXMuZmFrZUVsZW0udmFsdWUgPSB0aGlzLnRleHQ7XG5cbiAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LmFwcGVuZENoaWxkKHRoaXMuZmFrZUVsZW0pO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5zZWxlY3RlZFRleHQgPSAoMCwgX3NlbGVjdDIuZGVmYXVsdCkodGhpcy5mYWtlRWxlbSk7XG4gICAgICAgICAgICAgICAgdGhpcy5jb3B5VGV4dCgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdyZW1vdmVGYWtlJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiByZW1vdmVGYWtlKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLmZha2VIYW5kbGVyKSB7XG4gICAgICAgICAgICAgICAgICAgIGRvY3VtZW50LmJvZHkucmVtb3ZlRXZlbnRMaXN0ZW5lcignY2xpY2snLCB0aGlzLmZha2VIYW5kbGVyQ2FsbGJhY2spO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VIYW5kbGVyID0gbnVsbDtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5mYWtlSGFuZGxlckNhbGxiYWNrID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICBpZiAodGhpcy5mYWtlRWxlbSkge1xuICAgICAgICAgICAgICAgICAgICBkb2N1bWVudC5ib2R5LnJlbW92ZUNoaWxkKHRoaXMuZmFrZUVsZW0pO1xuICAgICAgICAgICAgICAgICAgICB0aGlzLmZha2VFbGVtID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ3NlbGVjdFRhcmdldCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gc2VsZWN0VGFyZ2V0KCkge1xuICAgICAgICAgICAgICAgIHRoaXMuc2VsZWN0ZWRUZXh0ID0gKDAsIF9zZWxlY3QyLmRlZmF1bHQpKHRoaXMudGFyZ2V0KTtcbiAgICAgICAgICAgICAgICB0aGlzLmNvcHlUZXh0KCk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2NvcHlUZXh0JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBjb3B5VGV4dCgpIHtcbiAgICAgICAgICAgICAgICB2YXIgc3VjY2VlZGVkID0gdm9pZCAwO1xuXG4gICAgICAgICAgICAgICAgdHJ5IHtcbiAgICAgICAgICAgICAgICAgICAgc3VjY2VlZGVkID0gZG9jdW1lbnQuZXhlY0NvbW1hbmQodGhpcy5hY3Rpb24pO1xuICAgICAgICAgICAgICAgIH0gY2F0Y2ggKGVycikge1xuICAgICAgICAgICAgICAgICAgICBzdWNjZWVkZWQgPSBmYWxzZTtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB0aGlzLmhhbmRsZVJlc3VsdChzdWNjZWVkZWQpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdoYW5kbGVSZXN1bHQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGhhbmRsZVJlc3VsdChzdWNjZWVkZWQpIHtcbiAgICAgICAgICAgICAgICB0aGlzLmVtaXR0ZXIuZW1pdChzdWNjZWVkZWQgPyAnc3VjY2VzcycgOiAnZXJyb3InLCB7XG4gICAgICAgICAgICAgICAgICAgIGFjdGlvbjogdGhpcy5hY3Rpb24sXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6IHRoaXMuc2VsZWN0ZWRUZXh0LFxuICAgICAgICAgICAgICAgICAgICB0cmlnZ2VyOiB0aGlzLnRyaWdnZXIsXG4gICAgICAgICAgICAgICAgICAgIGNsZWFyU2VsZWN0aW9uOiB0aGlzLmNsZWFyU2VsZWN0aW9uLmJpbmQodGhpcylcbiAgICAgICAgICAgICAgICB9KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnY2xlYXJTZWxlY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGNsZWFyU2VsZWN0aW9uKCkge1xuICAgICAgICAgICAgICAgIGlmICh0aGlzLnRhcmdldCkge1xuICAgICAgICAgICAgICAgICAgICB0aGlzLnRhcmdldC5ibHVyKCk7XG4gICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgd2luZG93LmdldFNlbGVjdGlvbigpLnJlbW92ZUFsbFJhbmdlcygpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZXN0cm95JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZXN0cm95KCkge1xuICAgICAgICAgICAgICAgIHRoaXMucmVtb3ZlRmFrZSgpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdhY3Rpb24nLFxuICAgICAgICAgICAgc2V0OiBmdW5jdGlvbiBzZXQoKSB7XG4gICAgICAgICAgICAgICAgdmFyIGFjdGlvbiA9IGFyZ3VtZW50cy5sZW5ndGggPiAwICYmIGFyZ3VtZW50c1swXSAhPT0gdW5kZWZpbmVkID8gYXJndW1lbnRzWzBdIDogJ2NvcHknO1xuXG4gICAgICAgICAgICAgICAgdGhpcy5fYWN0aW9uID0gYWN0aW9uO1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuX2FjdGlvbiAhPT0gJ2NvcHknICYmIHRoaXMuX2FjdGlvbiAhPT0gJ2N1dCcpIHtcbiAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwiYWN0aW9uXCIgdmFsdWUsIHVzZSBlaXRoZXIgXCJjb3B5XCIgb3IgXCJjdXRcIicpO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH0sXG4gICAgICAgICAgICBnZXQ6IGZ1bmN0aW9uIGdldCgpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5fYWN0aW9uO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICd0YXJnZXQnLFxuICAgICAgICAgICAgc2V0OiBmdW5jdGlvbiBzZXQodGFyZ2V0KSB7XG4gICAgICAgICAgICAgICAgaWYgKHRhcmdldCAhPT0gdW5kZWZpbmVkKSB7XG4gICAgICAgICAgICAgICAgICAgIGlmICh0YXJnZXQgJiYgKHR5cGVvZiB0YXJnZXQgPT09ICd1bmRlZmluZWQnID8gJ3VuZGVmaW5lZCcgOiBfdHlwZW9mKHRhcmdldCkpID09PSAnb2JqZWN0JyAmJiB0YXJnZXQubm9kZVR5cGUgPT09IDEpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIGlmICh0aGlzLmFjdGlvbiA9PT0gJ2NvcHknICYmIHRhcmdldC5oYXNBdHRyaWJ1dGUoJ2Rpc2FibGVkJykpIHtcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ0ludmFsaWQgXCJ0YXJnZXRcIiBhdHRyaWJ1dGUuIFBsZWFzZSB1c2UgXCJyZWFkb25seVwiIGluc3RlYWQgb2YgXCJkaXNhYmxlZFwiIGF0dHJpYnV0ZScpO1xuICAgICAgICAgICAgICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICAgICAgICAgICAgICBpZiAodGhpcy5hY3Rpb24gPT09ICdjdXQnICYmICh0YXJnZXQuaGFzQXR0cmlidXRlKCdyZWFkb25seScpIHx8IHRhcmdldC5oYXNBdHRyaWJ1dGUoJ2Rpc2FibGVkJykpKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhyb3cgbmV3IEVycm9yKCdJbnZhbGlkIFwidGFyZ2V0XCIgYXR0cmlidXRlLiBZb3UgY2FuXFwndCBjdXQgdGV4dCBmcm9tIGVsZW1lbnRzIHdpdGggXCJyZWFkb25seVwiIG9yIFwiZGlzYWJsZWRcIiBhdHRyaWJ1dGVzJyk7XG4gICAgICAgICAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuX3RhcmdldCA9IHRhcmdldDtcbiAgICAgICAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICAgICAgICAgIHRocm93IG5ldyBFcnJvcignSW52YWxpZCBcInRhcmdldFwiIHZhbHVlLCB1c2UgYSB2YWxpZCBFbGVtZW50Jyk7XG4gICAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9LFxuICAgICAgICAgICAgZ2V0OiBmdW5jdGlvbiBnZXQoKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMuX3RhcmdldDtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfV0pO1xuXG4gICAgICAgIHJldHVybiBDbGlwYm9hcmRBY3Rpb247XG4gICAgfSgpO1xuXG4gICAgbW9kdWxlLmV4cG9ydHMgPSBDbGlwYm9hcmRBY3Rpb247XG59KTsiLCIoZnVuY3Rpb24gKGdsb2JhbCwgZmFjdG9yeSkge1xuICAgIGlmICh0eXBlb2YgZGVmaW5lID09PSBcImZ1bmN0aW9uXCIgJiYgZGVmaW5lLmFtZCkge1xuICAgICAgICBkZWZpbmUoWydtb2R1bGUnLCAnLi9jbGlwYm9hcmQtYWN0aW9uJywgJ3RpbnktZW1pdHRlcicsICdnb29kLWxpc3RlbmVyJ10sIGZhY3RvcnkpO1xuICAgIH0gZWxzZSBpZiAodHlwZW9mIGV4cG9ydHMgIT09IFwidW5kZWZpbmVkXCIpIHtcbiAgICAgICAgZmFjdG9yeShtb2R1bGUsIHJlcXVpcmUoJy4vY2xpcGJvYXJkLWFjdGlvbicpLCByZXF1aXJlKCd0aW55LWVtaXR0ZXInKSwgcmVxdWlyZSgnZ29vZC1saXN0ZW5lcicpKTtcbiAgICB9IGVsc2Uge1xuICAgICAgICB2YXIgbW9kID0ge1xuICAgICAgICAgICAgZXhwb3J0czoge31cbiAgICAgICAgfTtcbiAgICAgICAgZmFjdG9yeShtb2QsIGdsb2JhbC5jbGlwYm9hcmRBY3Rpb24sIGdsb2JhbC50aW55RW1pdHRlciwgZ2xvYmFsLmdvb2RMaXN0ZW5lcik7XG4gICAgICAgIGdsb2JhbC5jbGlwYm9hcmQgPSBtb2QuZXhwb3J0cztcbiAgICB9XG59KSh0aGlzLCBmdW5jdGlvbiAobW9kdWxlLCBfY2xpcGJvYXJkQWN0aW9uLCBfdGlueUVtaXR0ZXIsIF9nb29kTGlzdGVuZXIpIHtcbiAgICAndXNlIHN0cmljdCc7XG5cbiAgICB2YXIgX2NsaXBib2FyZEFjdGlvbjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9jbGlwYm9hcmRBY3Rpb24pO1xuXG4gICAgdmFyIF90aW55RW1pdHRlcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF90aW55RW1pdHRlcik7XG5cbiAgICB2YXIgX2dvb2RMaXN0ZW5lcjIgPSBfaW50ZXJvcFJlcXVpcmVEZWZhdWx0KF9nb29kTGlzdGVuZXIpO1xuXG4gICAgZnVuY3Rpb24gX2ludGVyb3BSZXF1aXJlRGVmYXVsdChvYmopIHtcbiAgICAgICAgcmV0dXJuIG9iaiAmJiBvYmouX19lc01vZHVsZSA/IG9iaiA6IHtcbiAgICAgICAgICAgIGRlZmF1bHQ6IG9ialxuICAgICAgICB9O1xuICAgIH1cblxuICAgIGZ1bmN0aW9uIF9jbGFzc0NhbGxDaGVjayhpbnN0YW5jZSwgQ29uc3RydWN0b3IpIHtcbiAgICAgICAgaWYgKCEoaW5zdGFuY2UgaW5zdGFuY2VvZiBDb25zdHJ1Y3RvcikpIHtcbiAgICAgICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoXCJDYW5ub3QgY2FsbCBhIGNsYXNzIGFzIGEgZnVuY3Rpb25cIik7XG4gICAgICAgIH1cbiAgICB9XG5cbiAgICB2YXIgX2NyZWF0ZUNsYXNzID0gZnVuY3Rpb24gKCkge1xuICAgICAgICBmdW5jdGlvbiBkZWZpbmVQcm9wZXJ0aWVzKHRhcmdldCwgcHJvcHMpIHtcbiAgICAgICAgICAgIGZvciAodmFyIGkgPSAwOyBpIDwgcHJvcHMubGVuZ3RoOyBpKyspIHtcbiAgICAgICAgICAgICAgICB2YXIgZGVzY3JpcHRvciA9IHByb3BzW2ldO1xuICAgICAgICAgICAgICAgIGRlc2NyaXB0b3IuZW51bWVyYWJsZSA9IGRlc2NyaXB0b3IuZW51bWVyYWJsZSB8fCBmYWxzZTtcbiAgICAgICAgICAgICAgICBkZXNjcmlwdG9yLmNvbmZpZ3VyYWJsZSA9IHRydWU7XG4gICAgICAgICAgICAgICAgaWYgKFwidmFsdWVcIiBpbiBkZXNjcmlwdG9yKSBkZXNjcmlwdG9yLndyaXRhYmxlID0gdHJ1ZTtcbiAgICAgICAgICAgICAgICBPYmplY3QuZGVmaW5lUHJvcGVydHkodGFyZ2V0LCBkZXNjcmlwdG9yLmtleSwgZGVzY3JpcHRvcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gZnVuY3Rpb24gKENvbnN0cnVjdG9yLCBwcm90b1Byb3BzLCBzdGF0aWNQcm9wcykge1xuICAgICAgICAgICAgaWYgKHByb3RvUHJvcHMpIGRlZmluZVByb3BlcnRpZXMoQ29uc3RydWN0b3IucHJvdG90eXBlLCBwcm90b1Byb3BzKTtcbiAgICAgICAgICAgIGlmIChzdGF0aWNQcm9wcykgZGVmaW5lUHJvcGVydGllcyhDb25zdHJ1Y3Rvciwgc3RhdGljUHJvcHMpO1xuICAgICAgICAgICAgcmV0dXJuIENvbnN0cnVjdG9yO1xuICAgICAgICB9O1xuICAgIH0oKTtcblxuICAgIGZ1bmN0aW9uIF9wb3NzaWJsZUNvbnN0cnVjdG9yUmV0dXJuKHNlbGYsIGNhbGwpIHtcbiAgICAgICAgaWYgKCFzZWxmKSB7XG4gICAgICAgICAgICB0aHJvdyBuZXcgUmVmZXJlbmNlRXJyb3IoXCJ0aGlzIGhhc24ndCBiZWVuIGluaXRpYWxpc2VkIC0gc3VwZXIoKSBoYXNuJ3QgYmVlbiBjYWxsZWRcIik7XG4gICAgICAgIH1cblxuICAgICAgICByZXR1cm4gY2FsbCAmJiAodHlwZW9mIGNhbGwgPT09IFwib2JqZWN0XCIgfHwgdHlwZW9mIGNhbGwgPT09IFwiZnVuY3Rpb25cIikgPyBjYWxsIDogc2VsZjtcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBfaW5oZXJpdHMoc3ViQ2xhc3MsIHN1cGVyQ2xhc3MpIHtcbiAgICAgICAgaWYgKHR5cGVvZiBzdXBlckNsYXNzICE9PSBcImZ1bmN0aW9uXCIgJiYgc3VwZXJDbGFzcyAhPT0gbnVsbCkge1xuICAgICAgICAgICAgdGhyb3cgbmV3IFR5cGVFcnJvcihcIlN1cGVyIGV4cHJlc3Npb24gbXVzdCBlaXRoZXIgYmUgbnVsbCBvciBhIGZ1bmN0aW9uLCBub3QgXCIgKyB0eXBlb2Ygc3VwZXJDbGFzcyk7XG4gICAgICAgIH1cblxuICAgICAgICBzdWJDbGFzcy5wcm90b3R5cGUgPSBPYmplY3QuY3JlYXRlKHN1cGVyQ2xhc3MgJiYgc3VwZXJDbGFzcy5wcm90b3R5cGUsIHtcbiAgICAgICAgICAgIGNvbnN0cnVjdG9yOiB7XG4gICAgICAgICAgICAgICAgdmFsdWU6IHN1YkNsYXNzLFxuICAgICAgICAgICAgICAgIGVudW1lcmFibGU6IGZhbHNlLFxuICAgICAgICAgICAgICAgIHdyaXRhYmxlOiB0cnVlLFxuICAgICAgICAgICAgICAgIGNvbmZpZ3VyYWJsZTogdHJ1ZVxuICAgICAgICAgICAgfVxuICAgICAgICB9KTtcbiAgICAgICAgaWYgKHN1cGVyQ2xhc3MpIE9iamVjdC5zZXRQcm90b3R5cGVPZiA/IE9iamVjdC5zZXRQcm90b3R5cGVPZihzdWJDbGFzcywgc3VwZXJDbGFzcykgOiBzdWJDbGFzcy5fX3Byb3RvX18gPSBzdXBlckNsYXNzO1xuICAgIH1cblxuICAgIHZhciBDbGlwYm9hcmQgPSBmdW5jdGlvbiAoX0VtaXR0ZXIpIHtcbiAgICAgICAgX2luaGVyaXRzKENsaXBib2FyZCwgX0VtaXR0ZXIpO1xuXG4gICAgICAgIC8qKlxuICAgICAgICAgKiBAcGFyYW0ge1N0cmluZ3xIVE1MRWxlbWVudHxIVE1MQ29sbGVjdGlvbnxOb2RlTGlzdH0gdHJpZ2dlclxuICAgICAgICAgKiBAcGFyYW0ge09iamVjdH0gb3B0aW9uc1xuICAgICAgICAgKi9cbiAgICAgICAgZnVuY3Rpb24gQ2xpcGJvYXJkKHRyaWdnZXIsIG9wdGlvbnMpIHtcbiAgICAgICAgICAgIF9jbGFzc0NhbGxDaGVjayh0aGlzLCBDbGlwYm9hcmQpO1xuXG4gICAgICAgICAgICB2YXIgX3RoaXMgPSBfcG9zc2libGVDb25zdHJ1Y3RvclJldHVybih0aGlzLCAoQ2xpcGJvYXJkLl9fcHJvdG9fXyB8fCBPYmplY3QuZ2V0UHJvdG90eXBlT2YoQ2xpcGJvYXJkKSkuY2FsbCh0aGlzKSk7XG5cbiAgICAgICAgICAgIF90aGlzLnJlc29sdmVPcHRpb25zKG9wdGlvbnMpO1xuICAgICAgICAgICAgX3RoaXMubGlzdGVuQ2xpY2sodHJpZ2dlcik7XG4gICAgICAgICAgICByZXR1cm4gX3RoaXM7XG4gICAgICAgIH1cblxuICAgICAgICAvKipcbiAgICAgICAgICogRGVmaW5lcyBpZiBhdHRyaWJ1dGVzIHdvdWxkIGJlIHJlc29sdmVkIHVzaW5nIGludGVybmFsIHNldHRlciBmdW5jdGlvbnNcbiAgICAgICAgICogb3IgY3VzdG9tIGZ1bmN0aW9ucyB0aGF0IHdlcmUgcGFzc2VkIGluIHRoZSBjb25zdHJ1Y3Rvci5cbiAgICAgICAgICogQHBhcmFtIHtPYmplY3R9IG9wdGlvbnNcbiAgICAgICAgICovXG5cblxuICAgICAgICBfY3JlYXRlQ2xhc3MoQ2xpcGJvYXJkLCBbe1xuICAgICAgICAgICAga2V5OiAncmVzb2x2ZU9wdGlvbnMnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIHJlc29sdmVPcHRpb25zKCkge1xuICAgICAgICAgICAgICAgIHZhciBvcHRpb25zID0gYXJndW1lbnRzLmxlbmd0aCA+IDAgJiYgYXJndW1lbnRzWzBdICE9PSB1bmRlZmluZWQgPyBhcmd1bWVudHNbMF0gOiB7fTtcblxuICAgICAgICAgICAgICAgIHRoaXMuYWN0aW9uID0gdHlwZW9mIG9wdGlvbnMuYWN0aW9uID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy5hY3Rpb24gOiB0aGlzLmRlZmF1bHRBY3Rpb247XG4gICAgICAgICAgICAgICAgdGhpcy50YXJnZXQgPSB0eXBlb2Ygb3B0aW9ucy50YXJnZXQgPT09ICdmdW5jdGlvbicgPyBvcHRpb25zLnRhcmdldCA6IHRoaXMuZGVmYXVsdFRhcmdldDtcbiAgICAgICAgICAgICAgICB0aGlzLnRleHQgPSB0eXBlb2Ygb3B0aW9ucy50ZXh0ID09PSAnZnVuY3Rpb24nID8gb3B0aW9ucy50ZXh0IDogdGhpcy5kZWZhdWx0VGV4dDtcbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwge1xuICAgICAgICAgICAga2V5OiAnbGlzdGVuQ2xpY2snLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGxpc3RlbkNsaWNrKHRyaWdnZXIpIHtcbiAgICAgICAgICAgICAgICB2YXIgX3RoaXMyID0gdGhpcztcblxuICAgICAgICAgICAgICAgIHRoaXMubGlzdGVuZXIgPSAoMCwgX2dvb2RMaXN0ZW5lcjIuZGVmYXVsdCkodHJpZ2dlciwgJ2NsaWNrJywgZnVuY3Rpb24gKGUpIHtcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIF90aGlzMi5vbkNsaWNrKGUpO1xuICAgICAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdvbkNsaWNrJyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBvbkNsaWNrKGUpIHtcbiAgICAgICAgICAgICAgICB2YXIgdHJpZ2dlciA9IGUuZGVsZWdhdGVUYXJnZXQgfHwgZS5jdXJyZW50VGFyZ2V0O1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuY2xpcGJvYXJkQWN0aW9uKSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uID0gbnVsbDtcbiAgICAgICAgICAgICAgICB9XG5cbiAgICAgICAgICAgICAgICB0aGlzLmNsaXBib2FyZEFjdGlvbiA9IG5ldyBfY2xpcGJvYXJkQWN0aW9uMi5kZWZhdWx0KHtcbiAgICAgICAgICAgICAgICAgICAgYWN0aW9uOiB0aGlzLmFjdGlvbih0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdGFyZ2V0OiB0aGlzLnRhcmdldCh0cmlnZ2VyKSxcbiAgICAgICAgICAgICAgICAgICAgdGV4dDogdGhpcy50ZXh0KHRyaWdnZXIpLFxuICAgICAgICAgICAgICAgICAgICB0cmlnZ2VyOiB0cmlnZ2VyLFxuICAgICAgICAgICAgICAgICAgICBlbWl0dGVyOiB0aGlzXG4gICAgICAgICAgICAgICAgfSk7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRBY3Rpb24nLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRBY3Rpb24odHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHJldHVybiBnZXRBdHRyaWJ1dGVWYWx1ZSgnYWN0aW9uJywgdHJpZ2dlcik7XG4gICAgICAgICAgICB9XG4gICAgICAgIH0sIHtcbiAgICAgICAgICAgIGtleTogJ2RlZmF1bHRUYXJnZXQnLFxuICAgICAgICAgICAgdmFsdWU6IGZ1bmN0aW9uIGRlZmF1bHRUYXJnZXQodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHZhciBzZWxlY3RvciA9IGdldEF0dHJpYnV0ZVZhbHVlKCd0YXJnZXQnLCB0cmlnZ2VyKTtcblxuICAgICAgICAgICAgICAgIGlmIChzZWxlY3Rvcikge1xuICAgICAgICAgICAgICAgICAgICByZXR1cm4gZG9jdW1lbnQucXVlcnlTZWxlY3RvcihzZWxlY3Rvcik7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZWZhdWx0VGV4dCcsXG4gICAgICAgICAgICB2YWx1ZTogZnVuY3Rpb24gZGVmYXVsdFRleHQodHJpZ2dlcikge1xuICAgICAgICAgICAgICAgIHJldHVybiBnZXRBdHRyaWJ1dGVWYWx1ZSgndGV4dCcsIHRyaWdnZXIpO1xuICAgICAgICAgICAgfVxuICAgICAgICB9LCB7XG4gICAgICAgICAgICBrZXk6ICdkZXN0cm95JyxcbiAgICAgICAgICAgIHZhbHVlOiBmdW5jdGlvbiBkZXN0cm95KCkge1xuICAgICAgICAgICAgICAgIHRoaXMubGlzdGVuZXIuZGVzdHJveSgpO1xuXG4gICAgICAgICAgICAgICAgaWYgKHRoaXMuY2xpcGJvYXJkQWN0aW9uKSB7XG4gICAgICAgICAgICAgICAgICAgIHRoaXMuY2xpcGJvYXJkQWN0aW9uLmRlc3Ryb3koKTtcbiAgICAgICAgICAgICAgICAgICAgdGhpcy5jbGlwYm9hcmRBY3Rpb24gPSBudWxsO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfV0pO1xuXG4gICAgICAgIHJldHVybiBDbGlwYm9hcmQ7XG4gICAgfShfdGlueUVtaXR0ZXIyLmRlZmF1bHQpO1xuXG4gICAgLyoqXG4gICAgICogSGVscGVyIGZ1bmN0aW9uIHRvIHJldHJpZXZlIGF0dHJpYnV0ZSB2YWx1ZS5cbiAgICAgKiBAcGFyYW0ge1N0cmluZ30gc3VmZml4XG4gICAgICogQHBhcmFtIHtFbGVtZW50fSBlbGVtZW50XG4gICAgICovXG4gICAgZnVuY3Rpb24gZ2V0QXR0cmlidXRlVmFsdWUoc3VmZml4LCBlbGVtZW50KSB7XG4gICAgICAgIHZhciBhdHRyaWJ1dGUgPSAnZGF0YS1jbGlwYm9hcmQtJyArIHN1ZmZpeDtcblxuICAgICAgICBpZiAoIWVsZW1lbnQuaGFzQXR0cmlidXRlKGF0dHJpYnV0ZSkpIHtcbiAgICAgICAgICAgIHJldHVybjtcbiAgICAgICAgfVxuXG4gICAgICAgIHJldHVybiBlbGVtZW50LmdldEF0dHJpYnV0ZShhdHRyaWJ1dGUpO1xuICAgIH1cblxuICAgIG1vZHVsZS5leHBvcnRzID0gQ2xpcGJvYXJkO1xufSk7IiwiLyoqXG4gKiBBIHBvbHlmaWxsIGZvciBFbGVtZW50Lm1hdGNoZXMoKVxuICovXG5pZiAoRWxlbWVudCAmJiAhRWxlbWVudC5wcm90b3R5cGUubWF0Y2hlcykge1xuICAgIHZhciBwcm90byA9IEVsZW1lbnQucHJvdG90eXBlO1xuXG4gICAgcHJvdG8ubWF0Y2hlcyA9IHByb3RvLm1hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by5tb3pNYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ubXNNYXRjaGVzU2VsZWN0b3IgfHxcbiAgICAgICAgICAgICAgICAgICAgcHJvdG8ub01hdGNoZXNTZWxlY3RvciB8fFxuICAgICAgICAgICAgICAgICAgICBwcm90by53ZWJraXRNYXRjaGVzU2VsZWN0b3I7XG59XG5cbi8qKlxuICogRmluZHMgdGhlIGNsb3Nlc3QgcGFyZW50IHRoYXQgbWF0Y2hlcyBhIHNlbGVjdG9yLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcmV0dXJuIHtGdW5jdGlvbn1cbiAqL1xuZnVuY3Rpb24gY2xvc2VzdCAoZWxlbWVudCwgc2VsZWN0b3IpIHtcbiAgICB3aGlsZSAoZWxlbWVudCAmJiBlbGVtZW50ICE9PSBkb2N1bWVudCkge1xuICAgICAgICBpZiAoZWxlbWVudC5tYXRjaGVzKHNlbGVjdG9yKSkgcmV0dXJuIGVsZW1lbnQ7XG4gICAgICAgIGVsZW1lbnQgPSBlbGVtZW50LnBhcmVudE5vZGU7XG4gICAgfVxufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGNsb3Nlc3Q7XG4iLCJ2YXIgY2xvc2VzdCA9IHJlcXVpcmUoJy4vY2xvc2VzdCcpO1xuXG4vKipcbiAqIERlbGVnYXRlcyBldmVudCB0byBhIHNlbGVjdG9yLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEBwYXJhbSB7Qm9vbGVhbn0gdXNlQ2FwdHVyZVxuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBkZWxlZ2F0ZShlbGVtZW50LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2ssIHVzZUNhcHR1cmUpIHtcbiAgICB2YXIgbGlzdGVuZXJGbiA9IGxpc3RlbmVyLmFwcGx5KHRoaXMsIGFyZ3VtZW50cyk7XG5cbiAgICBlbGVtZW50LmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgbGlzdGVuZXJGbiwgdXNlQ2FwdHVyZSk7XG5cbiAgICByZXR1cm4ge1xuICAgICAgICBkZXN0cm95OiBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIGVsZW1lbnQucmVtb3ZlRXZlbnRMaXN0ZW5lcih0eXBlLCBsaXN0ZW5lckZuLCB1c2VDYXB0dXJlKTtcbiAgICAgICAgfVxuICAgIH1cbn1cblxuLyoqXG4gKiBGaW5kcyBjbG9zZXN0IG1hdGNoIGFuZCBpbnZva2VzIGNhbGxiYWNrLlxuICpcbiAqIEBwYXJhbSB7RWxlbWVudH0gZWxlbWVudFxuICogQHBhcmFtIHtTdHJpbmd9IHNlbGVjdG9yXG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge0Z1bmN0aW9ufVxuICovXG5mdW5jdGlvbiBsaXN0ZW5lcihlbGVtZW50LCBzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICByZXR1cm4gZnVuY3Rpb24oZSkge1xuICAgICAgICBlLmRlbGVnYXRlVGFyZ2V0ID0gY2xvc2VzdChlLnRhcmdldCwgc2VsZWN0b3IpO1xuXG4gICAgICAgIGlmIChlLmRlbGVnYXRlVGFyZ2V0KSB7XG4gICAgICAgICAgICBjYWxsYmFjay5jYWxsKGVsZW1lbnQsIGUpO1xuICAgICAgICB9XG4gICAgfVxufVxuXG5tb2R1bGUuZXhwb3J0cyA9IGRlbGVnYXRlO1xuIiwiLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIEhUTUwgZWxlbWVudC5cbiAqXG4gKiBAcGFyYW0ge09iamVjdH0gdmFsdWVcbiAqIEByZXR1cm4ge0Jvb2xlYW59XG4gKi9cbmV4cG9ydHMubm9kZSA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgcmV0dXJuIHZhbHVlICE9PSB1bmRlZmluZWRcbiAgICAgICAgJiYgdmFsdWUgaW5zdGFuY2VvZiBIVE1MRWxlbWVudFxuICAgICAgICAmJiB2YWx1ZS5ub2RlVHlwZSA9PT0gMTtcbn07XG5cbi8qKlxuICogQ2hlY2sgaWYgYXJndW1lbnQgaXMgYSBsaXN0IG9mIEhUTUwgZWxlbWVudHMuXG4gKlxuICogQHBhcmFtIHtPYmplY3R9IHZhbHVlXG4gKiBAcmV0dXJuIHtCb29sZWFufVxuICovXG5leHBvcnRzLm5vZGVMaXN0ID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICB2YXIgdHlwZSA9IE9iamVjdC5wcm90b3R5cGUudG9TdHJpbmcuY2FsbCh2YWx1ZSk7XG5cbiAgICByZXR1cm4gdmFsdWUgIT09IHVuZGVmaW5lZFxuICAgICAgICAmJiAodHlwZSA9PT0gJ1tvYmplY3QgTm9kZUxpc3RdJyB8fCB0eXBlID09PSAnW29iamVjdCBIVE1MQ29sbGVjdGlvbl0nKVxuICAgICAgICAmJiAoJ2xlbmd0aCcgaW4gdmFsdWUpXG4gICAgICAgICYmICh2YWx1ZS5sZW5ndGggPT09IDAgfHwgZXhwb3J0cy5ub2RlKHZhbHVlWzBdKSk7XG59O1xuXG4vKipcbiAqIENoZWNrIGlmIGFyZ3VtZW50IGlzIGEgc3RyaW5nLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5zdHJpbmcgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHJldHVybiB0eXBlb2YgdmFsdWUgPT09ICdzdHJpbmcnXG4gICAgICAgIHx8IHZhbHVlIGluc3RhbmNlb2YgU3RyaW5nO1xufTtcblxuLyoqXG4gKiBDaGVjayBpZiBhcmd1bWVudCBpcyBhIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7T2JqZWN0fSB2YWx1ZVxuICogQHJldHVybiB7Qm9vbGVhbn1cbiAqL1xuZXhwb3J0cy5mbiA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgdmFyIHR5cGUgPSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nLmNhbGwodmFsdWUpO1xuXG4gICAgcmV0dXJuIHR5cGUgPT09ICdbb2JqZWN0IEZ1bmN0aW9uXSc7XG59O1xuIiwidmFyIGlzID0gcmVxdWlyZSgnLi9pcycpO1xudmFyIGRlbGVnYXRlID0gcmVxdWlyZSgnZGVsZWdhdGUnKTtcblxuLyoqXG4gKiBWYWxpZGF0ZXMgYWxsIHBhcmFtcyBhbmQgY2FsbHMgdGhlIHJpZ2h0XG4gKiBsaXN0ZW5lciBmdW5jdGlvbiBiYXNlZCBvbiBpdHMgdGFyZ2V0IHR5cGUuXG4gKlxuICogQHBhcmFtIHtTdHJpbmd8SFRNTEVsZW1lbnR8SFRNTENvbGxlY3Rpb258Tm9kZUxpc3R9IHRhcmdldFxuICogQHBhcmFtIHtTdHJpbmd9IHR5cGVcbiAqIEBwYXJhbSB7RnVuY3Rpb259IGNhbGxiYWNrXG4gKiBAcmV0dXJuIHtPYmplY3R9XG4gKi9cbmZ1bmN0aW9uIGxpc3Rlbih0YXJnZXQsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgaWYgKCF0YXJnZXQgJiYgIXR5cGUgJiYgIWNhbGxiYWNrKSB7XG4gICAgICAgIHRocm93IG5ldyBFcnJvcignTWlzc2luZyByZXF1aXJlZCBhcmd1bWVudHMnKTtcbiAgICB9XG5cbiAgICBpZiAoIWlzLnN0cmluZyh0eXBlKSkge1xuICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdTZWNvbmQgYXJndW1lbnQgbXVzdCBiZSBhIFN0cmluZycpO1xuICAgIH1cblxuICAgIGlmICghaXMuZm4oY2FsbGJhY2spKSB7XG4gICAgICAgIHRocm93IG5ldyBUeXBlRXJyb3IoJ1RoaXJkIGFyZ3VtZW50IG11c3QgYmUgYSBGdW5jdGlvbicpO1xuICAgIH1cblxuICAgIGlmIChpcy5ub2RlKHRhcmdldCkpIHtcbiAgICAgICAgcmV0dXJuIGxpc3Rlbk5vZGUodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2UgaWYgKGlzLm5vZGVMaXN0KHRhcmdldCkpIHtcbiAgICAgICAgcmV0dXJuIGxpc3Rlbk5vZGVMaXN0KHRhcmdldCwgdHlwZSwgY2FsbGJhY2spO1xuICAgIH1cbiAgICBlbHNlIGlmIChpcy5zdHJpbmcodGFyZ2V0KSkge1xuICAgICAgICByZXR1cm4gbGlzdGVuU2VsZWN0b3IodGFyZ2V0LCB0eXBlLCBjYWxsYmFjayk7XG4gICAgfVxuICAgIGVsc2Uge1xuICAgICAgICB0aHJvdyBuZXcgVHlwZUVycm9yKCdGaXJzdCBhcmd1bWVudCBtdXN0IGJlIGEgU3RyaW5nLCBIVE1MRWxlbWVudCwgSFRNTENvbGxlY3Rpb24sIG9yIE5vZGVMaXN0Jyk7XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZHMgYW4gZXZlbnQgbGlzdGVuZXIgdG8gYSBIVE1MIGVsZW1lbnRcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7SFRNTEVsZW1lbnR9IG5vZGVcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5Ob2RlKG5vZGUsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgbm9kZS5hZGRFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcblxuICAgIHJldHVybiB7XG4gICAgICAgIGRlc3Ryb3k6IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgbm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcbiAgICAgICAgfVxuICAgIH1cbn1cblxuLyoqXG4gKiBBZGQgYW4gZXZlbnQgbGlzdGVuZXIgdG8gYSBsaXN0IG9mIEhUTUwgZWxlbWVudHNcbiAqIGFuZCByZXR1cm5zIGEgcmVtb3ZlIGxpc3RlbmVyIGZ1bmN0aW9uLlxuICpcbiAqIEBwYXJhbSB7Tm9kZUxpc3R8SFRNTENvbGxlY3Rpb259IG5vZGVMaXN0XG4gKiBAcGFyYW0ge1N0cmluZ30gdHlwZVxuICogQHBhcmFtIHtGdW5jdGlvbn0gY2FsbGJhY2tcbiAqIEByZXR1cm4ge09iamVjdH1cbiAqL1xuZnVuY3Rpb24gbGlzdGVuTm9kZUxpc3Qobm9kZUxpc3QsIHR5cGUsIGNhbGxiYWNrKSB7XG4gICAgQXJyYXkucHJvdG90eXBlLmZvckVhY2guY2FsbChub2RlTGlzdCwgZnVuY3Rpb24obm9kZSkge1xuICAgICAgICBub2RlLmFkZEV2ZW50TGlzdGVuZXIodHlwZSwgY2FsbGJhY2spO1xuICAgIH0pO1xuXG4gICAgcmV0dXJuIHtcbiAgICAgICAgZGVzdHJveTogZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICBBcnJheS5wcm90b3R5cGUuZm9yRWFjaC5jYWxsKG5vZGVMaXN0LCBmdW5jdGlvbihub2RlKSB7XG4gICAgICAgICAgICAgICAgbm9kZS5yZW1vdmVFdmVudExpc3RlbmVyKHR5cGUsIGNhbGxiYWNrKTtcbiAgICAgICAgICAgIH0pO1xuICAgICAgICB9XG4gICAgfVxufVxuXG4vKipcbiAqIEFkZCBhbiBldmVudCBsaXN0ZW5lciB0byBhIHNlbGVjdG9yXG4gKiBhbmQgcmV0dXJucyBhIHJlbW92ZSBsaXN0ZW5lciBmdW5jdGlvbi5cbiAqXG4gKiBAcGFyYW0ge1N0cmluZ30gc2VsZWN0b3JcbiAqIEBwYXJhbSB7U3RyaW5nfSB0eXBlXG4gKiBAcGFyYW0ge0Z1bmN0aW9ufSBjYWxsYmFja1xuICogQHJldHVybiB7T2JqZWN0fVxuICovXG5mdW5jdGlvbiBsaXN0ZW5TZWxlY3RvcihzZWxlY3RvciwgdHlwZSwgY2FsbGJhY2spIHtcbiAgICByZXR1cm4gZGVsZWdhdGUoZG9jdW1lbnQuYm9keSwgc2VsZWN0b3IsIHR5cGUsIGNhbGxiYWNrKTtcbn1cblxubW9kdWxlLmV4cG9ydHMgPSBsaXN0ZW47XG4iLCIvKiEgbmdjbGlwYm9hcmQgLSB2MS4xLjEgLSAyMDE2LTAyLTI2XHJcbiogaHR0cHM6Ly9naXRodWIuY29tL3NhY2hpbmNob29sdXIvbmdjbGlwYm9hcmRcclxuKiBDb3B5cmlnaHQgKGMpIDIwMTYgU2FjaGluOyBMaWNlbnNlZCBNSVQgKi9cclxuKGZ1bmN0aW9uKCkge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgdmFyIE1PRFVMRV9OQU1FID0gJ25nY2xpcGJvYXJkJztcclxuICAgIHZhciBhbmd1bGFyLCBDbGlwYm9hcmQ7XHJcbiAgICBcclxuICAgIC8vIENoZWNrIGZvciBDb21tb25KUyBzdXBwb3J0XHJcbiAgICBpZiAodHlwZW9mIG1vZHVsZSA9PT0gJ29iamVjdCcgJiYgbW9kdWxlLmV4cG9ydHMpIHtcclxuICAgICAgYW5ndWxhciA9IHJlcXVpcmUoJ2FuZ3VsYXInKTtcclxuICAgICAgQ2xpcGJvYXJkID0gcmVxdWlyZSgnY2xpcGJvYXJkJyk7XHJcbiAgICAgIG1vZHVsZS5leHBvcnRzID0gTU9EVUxFX05BTUU7XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICBhbmd1bGFyID0gd2luZG93LmFuZ3VsYXI7XHJcbiAgICAgIENsaXBib2FyZCA9IHdpbmRvdy5DbGlwYm9hcmQ7XHJcbiAgICB9XHJcblxyXG4gICAgYW5ndWxhci5tb2R1bGUoTU9EVUxFX05BTUUsIFtdKS5kaXJlY3RpdmUoJ25nY2xpcGJvYXJkJywgZnVuY3Rpb24oKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgcmVzdHJpY3Q6ICdBJyxcclxuICAgICAgICAgICAgc2NvcGU6IHtcclxuICAgICAgICAgICAgICAgIG5nY2xpcGJvYXJkU3VjY2VzczogJyYnLFxyXG4gICAgICAgICAgICAgICAgbmdjbGlwYm9hcmRFcnJvcjogJyYnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGxpbms6IGZ1bmN0aW9uKHNjb3BlLCBlbGVtZW50KSB7XHJcbiAgICAgICAgICAgICAgICB2YXIgY2xpcGJvYXJkID0gbmV3IENsaXBib2FyZChlbGVtZW50WzBdKTtcclxuXHJcbiAgICAgICAgICAgICAgICBjbGlwYm9hcmQub24oJ3N1Y2Nlc3MnLCBmdW5jdGlvbihlKSB7XHJcbiAgICAgICAgICAgICAgICAgIHNjb3BlLiRhcHBseShmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2NvcGUubmdjbGlwYm9hcmRTdWNjZXNzKHtcclxuICAgICAgICAgICAgICAgICAgICAgIGU6IGVcclxuICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgICAgICBjbGlwYm9hcmQub24oJ2Vycm9yJywgZnVuY3Rpb24oZSkge1xyXG4gICAgICAgICAgICAgICAgICBzY29wZS4kYXBwbHkoZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHNjb3BlLm5nY2xpcGJvYXJkRXJyb3Ioe1xyXG4gICAgICAgICAgICAgICAgICAgICAgZTogZVxyXG4gICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9KTtcclxufSgpKTtcclxuIiwiZnVuY3Rpb24gc2VsZWN0KGVsZW1lbnQpIHtcbiAgICB2YXIgc2VsZWN0ZWRUZXh0O1xuXG4gICAgaWYgKGVsZW1lbnQubm9kZU5hbWUgPT09ICdTRUxFQ1QnKSB7XG4gICAgICAgIGVsZW1lbnQuZm9jdXMoKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBlbGVtZW50LnZhbHVlO1xuICAgIH1cbiAgICBlbHNlIGlmIChlbGVtZW50Lm5vZGVOYW1lID09PSAnSU5QVVQnIHx8IGVsZW1lbnQubm9kZU5hbWUgPT09ICdURVhUQVJFQScpIHtcbiAgICAgICAgZWxlbWVudC5mb2N1cygpO1xuICAgICAgICBlbGVtZW50LnNldFNlbGVjdGlvblJhbmdlKDAsIGVsZW1lbnQudmFsdWUubGVuZ3RoKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBlbGVtZW50LnZhbHVlO1xuICAgIH1cbiAgICBlbHNlIHtcbiAgICAgICAgaWYgKGVsZW1lbnQuaGFzQXR0cmlidXRlKCdjb250ZW50ZWRpdGFibGUnKSkge1xuICAgICAgICAgICAgZWxlbWVudC5mb2N1cygpO1xuICAgICAgICB9XG5cbiAgICAgICAgdmFyIHNlbGVjdGlvbiA9IHdpbmRvdy5nZXRTZWxlY3Rpb24oKTtcbiAgICAgICAgdmFyIHJhbmdlID0gZG9jdW1lbnQuY3JlYXRlUmFuZ2UoKTtcblxuICAgICAgICByYW5nZS5zZWxlY3ROb2RlQ29udGVudHMoZWxlbWVudCk7XG4gICAgICAgIHNlbGVjdGlvbi5yZW1vdmVBbGxSYW5nZXMoKTtcbiAgICAgICAgc2VsZWN0aW9uLmFkZFJhbmdlKHJhbmdlKTtcblxuICAgICAgICBzZWxlY3RlZFRleHQgPSBzZWxlY3Rpb24udG9TdHJpbmcoKTtcbiAgICB9XG5cbiAgICByZXR1cm4gc2VsZWN0ZWRUZXh0O1xufVxuXG5tb2R1bGUuZXhwb3J0cyA9IHNlbGVjdDtcbiIsImZ1bmN0aW9uIEUgKCkge1xuICAvLyBLZWVwIHRoaXMgZW1wdHkgc28gaXQncyBlYXNpZXIgdG8gaW5oZXJpdCBmcm9tXG4gIC8vICh2aWEgaHR0cHM6Ly9naXRodWIuY29tL2xpcHNtYWNrIGZyb20gaHR0cHM6Ly9naXRodWIuY29tL3Njb3R0Y29yZ2FuL3RpbnktZW1pdHRlci9pc3N1ZXMvMylcbn1cblxuRS5wcm90b3R5cGUgPSB7XG4gIG9uOiBmdW5jdGlvbiAobmFtZSwgY2FsbGJhY2ssIGN0eCkge1xuICAgIHZhciBlID0gdGhpcy5lIHx8ICh0aGlzLmUgPSB7fSk7XG5cbiAgICAoZVtuYW1lXSB8fCAoZVtuYW1lXSA9IFtdKSkucHVzaCh7XG4gICAgICBmbjogY2FsbGJhY2ssXG4gICAgICBjdHg6IGN0eFxuICAgIH0pO1xuXG4gICAgcmV0dXJuIHRoaXM7XG4gIH0sXG5cbiAgb25jZTogZnVuY3Rpb24gKG5hbWUsIGNhbGxiYWNrLCBjdHgpIHtcbiAgICB2YXIgc2VsZiA9IHRoaXM7XG4gICAgZnVuY3Rpb24gbGlzdGVuZXIgKCkge1xuICAgICAgc2VsZi5vZmYobmFtZSwgbGlzdGVuZXIpO1xuICAgICAgY2FsbGJhY2suYXBwbHkoY3R4LCBhcmd1bWVudHMpO1xuICAgIH07XG5cbiAgICBsaXN0ZW5lci5fID0gY2FsbGJhY2tcbiAgICByZXR1cm4gdGhpcy5vbihuYW1lLCBsaXN0ZW5lciwgY3R4KTtcbiAgfSxcblxuICBlbWl0OiBmdW5jdGlvbiAobmFtZSkge1xuICAgIHZhciBkYXRhID0gW10uc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpO1xuICAgIHZhciBldnRBcnIgPSAoKHRoaXMuZSB8fCAodGhpcy5lID0ge30pKVtuYW1lXSB8fCBbXSkuc2xpY2UoKTtcbiAgICB2YXIgaSA9IDA7XG4gICAgdmFyIGxlbiA9IGV2dEFyci5sZW5ndGg7XG5cbiAgICBmb3IgKGk7IGkgPCBsZW47IGkrKykge1xuICAgICAgZXZ0QXJyW2ldLmZuLmFwcGx5KGV2dEFycltpXS5jdHgsIGRhdGEpO1xuICAgIH1cblxuICAgIHJldHVybiB0aGlzO1xuICB9LFxuXG4gIG9mZjogZnVuY3Rpb24gKG5hbWUsIGNhbGxiYWNrKSB7XG4gICAgdmFyIGUgPSB0aGlzLmUgfHwgKHRoaXMuZSA9IHt9KTtcbiAgICB2YXIgZXZ0cyA9IGVbbmFtZV07XG4gICAgdmFyIGxpdmVFdmVudHMgPSBbXTtcblxuICAgIGlmIChldnRzICYmIGNhbGxiYWNrKSB7XG4gICAgICBmb3IgKHZhciBpID0gMCwgbGVuID0gZXZ0cy5sZW5ndGg7IGkgPCBsZW47IGkrKykge1xuICAgICAgICBpZiAoZXZ0c1tpXS5mbiAhPT0gY2FsbGJhY2sgJiYgZXZ0c1tpXS5mbi5fICE9PSBjYWxsYmFjaylcbiAgICAgICAgICBsaXZlRXZlbnRzLnB1c2goZXZ0c1tpXSk7XG4gICAgICB9XG4gICAgfVxuXG4gICAgLy8gUmVtb3ZlIGV2ZW50IGZyb20gcXVldWUgdG8gcHJldmVudCBtZW1vcnkgbGVha1xuICAgIC8vIFN1Z2dlc3RlZCBieSBodHRwczovL2dpdGh1Yi5jb20vbGF6ZFxuICAgIC8vIFJlZjogaHR0cHM6Ly9naXRodWIuY29tL3Njb3R0Y29yZ2FuL3RpbnktZW1pdHRlci9jb21taXQvYzZlYmZhYTliYzk3M2IzM2QxMTBhODRhMzA3NzQyYjdjZjk0Yzk1MyNjb21taXRjb21tZW50LTUwMjQ5MTBcblxuICAgIChsaXZlRXZlbnRzLmxlbmd0aClcbiAgICAgID8gZVtuYW1lXSA9IGxpdmVFdmVudHNcbiAgICAgIDogZGVsZXRlIGVbbmFtZV07XG5cbiAgICByZXR1cm4gdGhpcztcbiAgfVxufTtcblxubW9kdWxlLmV4cG9ydHMgPSBFO1xuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMS8yMC8yMDE1LlxyXG4gKiBURFNNIGlzIGEgZ2xvYmFsIG9iamVjdCB0aGF0IGNvbWVzIGZyb20gQXBwLmpzXHJcbiAqXHJcbiAqIFRoZSBmb2xsb3dpbmcgaGVscGVyIHdvcmtzIGluIGEgd2F5IHRvIG1ha2UgYXZhaWxhYmxlIHRoZSBjcmVhdGlvbiBvZiBEaXJlY3RpdmUsIFNlcnZpY2VzIGFuZCBDb250cm9sbGVyXHJcbiAqIG9uIGZseSBvciB3aGVuIGRlcGxveWluZyB0aGUgYXBwLlxyXG4gKlxyXG4gKiBXZSByZWR1Y2UgdGhlIHVzZSBvZiBjb21waWxlIGFuZCBleHRyYSBzdGVwc1xyXG4gKi9cclxuXHJcbnZhciBURFNUTSA9IHJlcXVpcmUoJy4vQXBwLmpzJyk7XHJcblxyXG4vKipcclxuICogTGlzdGVuIHRvIGFuIGV4aXN0aW5nIGRpZ2VzdCBvZiB0aGUgY29tcGlsZSBwcm92aWRlciBhbmQgZXhlY3V0ZSB0aGUgJGFwcGx5IGltbWVkaWF0ZWx5IG9yIGFmdGVyIGl0J3MgcmVhZHlcclxuICogQHBhcmFtIGN1cnJlbnRcclxuICogQHBhcmFtIGZuXHJcbiAqL1xyXG5URFNUTS5zYWZlQXBwbHkgPSBmdW5jdGlvbiAoY3VycmVudCwgZm4pIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciBwaGFzZSA9IGN1cnJlbnQuJHJvb3QuJCRwaGFzZTtcclxuICAgIGlmIChwaGFzZSA9PT0gJyRhcHBseScgfHwgcGhhc2UgPT09ICckZGlnZXN0Jykge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRldmFsKGZuKTtcclxuICAgICAgICB9XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICAgIGlmIChmbikge1xyXG4gICAgICAgICAgICBjdXJyZW50LiRhcHBseShmbik7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgY3VycmVudC4kYXBwbHkoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBkaXJlY3RpdmUgYXN5bmMgaWYgdGhlIGNvbXBpbGVQcm92aWRlciBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZURpcmVjdGl2ZSA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbXBpbGVQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5jb21waWxlUHJvdmlkZXIuZGlyZWN0aXZlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5kaXJlY3RpdmUpIHtcclxuICAgICAgICBURFNUTS5kaXJlY3RpdmUoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBjb250cm9sbGVycyBhc3luYyBpZiB0aGUgY29udHJvbGxlclByb3ZpZGVyIGlzIGF2YWlsYWJsZVxyXG4gKiBAcGFyYW0gc2V0dGluZ1xyXG4gKiBAcGFyYW0gYXJnc1xyXG4gKi9cclxuVERTVE0uY3JlYXRlQ29udHJvbGxlciA9IGZ1bmN0aW9uIChzZXR0aW5nLCBhcmdzKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICBpZiAoVERTVE0uUHJvdmlkZXJDb3JlLmNvbnRyb2xsZXJQcm92aWRlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXJQcm92aWRlci5yZWdpc3RlcihzZXR0aW5nLCBhcmdzKTtcclxuICAgIH0gZWxzZSBpZiAoVERTVE0uY29udHJvbGxlcikge1xyXG4gICAgICAgIFREU1RNLmNvbnRyb2xsZXIoc2V0dGluZywgYXJncyk7XHJcbiAgICB9XHJcbn07XHJcblxyXG4vKipcclxuICogSGVscGVyIHRvIGluamVjdCBzZXJ2aWNlIGFzeW5jIGlmIHRoZSBwcm92aWRlU2VydmljZSBpcyBhdmFpbGFibGVcclxuICogQHBhcmFtIHNldHRpbmdcclxuICogQHBhcmFtIGFyZ3NcclxuICovXHJcblREU1RNLmNyZWF0ZVNlcnZpY2UgPSBmdW5jdGlvbiAoc2V0dGluZywgYXJncykge1xyXG4gICAgJ3VzZSBzdHJpY3QnO1xyXG4gICAgaWYgKFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZSkge1xyXG4gICAgICAgIFREU1RNLlByb3ZpZGVyQ29yZS5wcm92aWRlU2VydmljZS5zZXJ2aWNlKHNldHRpbmcsIGFyZ3MpO1xyXG4gICAgfSBlbHNlIGlmIChURFNUTS5jb250cm9sbGVyKSB7XHJcbiAgICAgICAgVERTVE0uc2VydmljZShzZXR0aW5nLCBhcmdzKTtcclxuICAgIH1cclxufTtcclxuXHJcbi8qKlxyXG4gKiBGb3IgTGVnYWN5IHN5c3RlbSwgd2hhdCBpcyBkb2VzIGlzIHRvIHRha2UgcGFyYW1zIGZyb20gdGhlIHF1ZXJ5XHJcbiAqIG91dHNpZGUgdGhlIEFuZ3VsYXJKUyB1aS1yb3V0aW5nLlxyXG4gKiBAcGFyYW0gcGFyYW0gLy8gUGFyYW0gdG8gc2VhcmMgZm9yIC9leGFtcGxlLmh0bWw/YmFyPWZvbyNjdXJyZW50U3RhdGVcclxuICovXHJcblREU1RNLmdldFVSTFBhcmFtID0gZnVuY3Rpb24gKHBhcmFtKSB7XHJcbiAgICAndXNlIHN0cmljdCc7XHJcbiAgICAkLnVybFBhcmFtID0gZnVuY3Rpb24gKG5hbWUpIHtcclxuICAgICAgICB2YXIgcmVzdWx0cyA9IG5ldyBSZWdFeHAoJ1tcXD8mXScgKyBuYW1lICsgJz0oW14mI10qKScpLmV4ZWMod2luZG93LmxvY2F0aW9uLmhyZWYpO1xyXG4gICAgICAgIGlmIChyZXN1bHRzID09PSBudWxsKSB7XHJcbiAgICAgICAgICAgIHJldHVybiBudWxsO1xyXG4gICAgICAgIH1cclxuICAgICAgICBlbHNlIHtcclxuICAgICAgICAgICAgcmV0dXJuIHJlc3VsdHNbMV0gfHwgMDtcclxuICAgICAgICB9XHJcbiAgICB9O1xyXG5cclxuICAgIHJldHVybiAkLnVybFBhcmFtKHBhcmFtKTtcclxufTtcclxuXHJcbi8qKlxyXG4gKiBUaGlzIGNvZGUgd2FzIGludHJvZHVjZWQgb25seSBmb3IgdGhlIGlmcmFtZSBtaWdyYXRpb25cclxuICogaXQgZGV0ZWN0IHdoZW4gbW91c2UgZW50ZXJcclxuICovXHJcblREU1RNLmlmcmFtZUxvYWRlciA9IGZ1bmN0aW9uICgpIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgICQoJy5pZnJhbWVMb2FkZXInKS5ob3ZlcihcclxuICAgICAgICBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICQoJy5uYXZiYXItdWwtY29udGFpbmVyIC5kcm9wZG93bi5vcGVuJykucmVtb3ZlQ2xhc3MoJ29wZW4nKTtcclxuICAgICAgICB9LCBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgfVxyXG4gICAgKTtcclxufTtcclxuXHJcblREU1RNLmdldENvbnZlcnRlZERhdGVGb3JtYXQgPSBmdW5jdGlvbiggZGF0ZVN0cmluZywgdXNlckRURm9ybWF0LCB0aW1lWm9uZSApIHtcclxuICAgICd1c2Ugc3RyaWN0JztcclxuICAgIHZhciB0aW1lU3RyaW5nID0gJyc7XHJcbiAgICBpZihkYXRlU3RyaW5nKXtcclxuICAgICAgICBpZiAodGltZVpvbmUgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgdGltZVpvbmUgPSAnR01UJztcclxuICAgICAgICB9XHJcbiAgICAgICAgdmFyIGZvcm1hdCA9ICdNTS9ERC9ZWVlZJztcclxuICAgICAgICBpZiAodXNlckRURm9ybWF0ID09PSAnREQvTU0vWVlZWScpIHtcclxuICAgICAgICAgICAgZm9ybWF0ID0gJ0REL01NL1lZWVknO1xyXG4gICAgICAgIH1cclxuICAgICAgICAvLyBDb252ZXJ0IHp1bHUgZGF0ZXRpbWUgdG8gYSBzcGVjaWZpYyB0aW1lem9uZS9mb3JtYXRcclxuICAgICAgICB0aW1lU3RyaW5nID0gbW9tZW50KGRhdGVTdHJpbmcpLnR6KHRpbWVab25lKS5mb3JtYXQoZm9ybWF0KVxyXG4gICAgfVxyXG4gICAgcmV0dXJuIHRpbWVTdHJpbmc7XHJcbn07XHJcblxyXG53aW5kb3cuVERTVE0gPSBURFNUTTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDExLzE2LzIwMTUuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxucmVxdWlyZSgnYW5ndWxhcicpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLWFuaW1hdGUnKTtcclxucmVxdWlyZSgnYW5ndWxhci1tb2NrcycpO1xyXG5yZXF1aXJlKCdhbmd1bGFyLXNhbml0aXplJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItcmVzb3VyY2UnKTtcclxucmVxdWlyZSgnYW5ndWxhci10cmFuc2xhdGUnKTtcclxucmVxdWlyZSgnYW5ndWxhci10cmFuc2xhdGUtbG9hZGVyLXBhcnRpYWwnKTtcclxucmVxdWlyZSgnYW5ndWxhci11aS1ib290c3RyYXAnKTtcclxucmVxdWlyZSgnbmdDbGlwYm9hcmQnKTtcclxucmVxdWlyZSgndWktcm91dGVyJyk7XHJcbnJlcXVpcmUoJ3J4LWFuZ3VsYXInKTtcclxucmVxdWlyZSgnYXBpLWNoZWNrJyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItZm9ybWx5Jyk7XHJcbnJlcXVpcmUoJ2FuZ3VsYXItZm9ybWx5LXRlbXBsYXRlcy1ib290c3RyYXAnKTtcclxuXHJcbi8vIE1vZHVsZXNcclxuaW1wb3J0IEhUVFBNb2R1bGUgZnJvbSAnLi4vc2VydmljZXMvaHR0cC9IVFRQTW9kdWxlLmpzJztcclxuaW1wb3J0IFJlc3RBUElNb2R1bGUgZnJvbSAnLi4vc2VydmljZXMvUmVzdEFQSS9SZXN0QVBJTW9kdWxlLmpzJ1xyXG5pbXBvcnQgSGVhZGVyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvaGVhZGVyL0hlYWRlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBMaWNlbnNlQWRtaW5Nb2R1bGUgZnJvbSAnLi4vbW9kdWxlcy9saWNlbnNlQWRtaW4vTGljZW5zZUFkbWluTW9kdWxlLmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlIGZyb20gJy4uL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvTGljZW5zZU1hbmFnZXJNb2R1bGUuanMnO1xyXG5pbXBvcnQgTm90aWNlTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL25vdGljZU1hbmFnZXIvTm90aWNlTWFuYWdlck1vZHVsZS5qcyc7XHJcbmltcG9ydCBUYXNrTWFuYWdlck1vZHVsZSBmcm9tICcuLi9tb2R1bGVzL3Rhc2tNYW5hZ2VyL1Rhc2tNYW5hZ2VyTW9kdWxlLmpzJztcclxuXHJcbnZhciBQcm92aWRlckNvcmUgPSB7fTtcclxuXHJcbnZhciBURFNUTSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTScsIFtcclxuICAgICduZ1Nhbml0aXplJyxcclxuICAgICduZ1Jlc291cmNlJyxcclxuICAgICduZ0FuaW1hdGUnLFxyXG4gICAgJ3Bhc2NhbHByZWNodC50cmFuc2xhdGUnLCAvLyAnYW5ndWxhci10cmFuc2xhdGUnXHJcbiAgICAndWkucm91dGVyJyxcclxuICAgICduZ2NsaXBib2FyZCcsXHJcbiAgICAna2VuZG8uZGlyZWN0aXZlcycsXHJcbiAgICAncngnLFxyXG4gICAgJ2Zvcm1seScsXHJcbiAgICAnZm9ybWx5Qm9vdHN0cmFwJyxcclxuICAgICd1aS5ib290c3RyYXAnLFxyXG4gICAgSFRUUE1vZHVsZS5uYW1lLFxyXG4gICAgUmVzdEFQSU1vZHVsZS5uYW1lLFxyXG4gICAgSGVhZGVyTW9kdWxlLm5hbWUsXHJcbiAgICBUYXNrTWFuYWdlck1vZHVsZS5uYW1lLFxyXG4gICAgTGljZW5zZUFkbWluTW9kdWxlLm5hbWUsXHJcbiAgICBMaWNlbnNlTWFuYWdlck1vZHVsZS5uYW1lLFxyXG4gICAgTm90aWNlTWFuYWdlck1vZHVsZS5uYW1lXHJcbl0pLmNvbmZpZyhbXHJcbiAgICAnJGxvZ1Byb3ZpZGVyJyxcclxuICAgICckcm9vdFNjb3BlUHJvdmlkZXInLFxyXG4gICAgJyRjb21waWxlUHJvdmlkZXInLFxyXG4gICAgJyRjb250cm9sbGVyUHJvdmlkZXInLFxyXG4gICAgJyRwcm92aWRlJyxcclxuICAgICckaHR0cFByb3ZpZGVyJyxcclxuICAgICckdHJhbnNsYXRlUHJvdmlkZXInLFxyXG4gICAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgJyR1cmxSb3V0ZXJQcm92aWRlcicsXHJcbiAgICAnJGxvY2F0aW9uUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRsb2dQcm92aWRlciwgJHJvb3RTY29wZVByb3ZpZGVyLCAkY29tcGlsZVByb3ZpZGVyLCAkY29udHJvbGxlclByb3ZpZGVyLCAkcHJvdmlkZSwgJGh0dHBQcm92aWRlcixcclxuICAgICAgICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIsICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIsICR1cmxSb3V0ZXJQcm92aWRlciwgJGxvY2F0aW9uUHJvdmlkZXIpIHtcclxuXHJcbiAgICAgICAgJHJvb3RTY29wZVByb3ZpZGVyLmRpZ2VzdFR0bCgzMCk7XHJcbiAgICAgICAgLy8gR29pbmcgYmFjayB0byB5b3VcclxuICAgICAgICAkbG9jYXRpb25Qcm92aWRlci5odG1sNU1vZGUodHJ1ZSkuaGFzaFByZWZpeCgnIScpO1xyXG5cclxuICAgICAgICAkbG9nUHJvdmlkZXIuZGVidWdFbmFibGVkKHRydWUpO1xyXG5cclxuICAgICAgICAvLyBBZnRlciBib290c3RyYXBwaW5nIGFuZ3VsYXIgZm9yZ2V0IHRoZSBwcm92aWRlciBzaW5jZSBldmVyeXRoaW5nIFwid2FzIGFscmVhZHkgbG9hZGVkXCJcclxuICAgICAgICBQcm92aWRlckNvcmUuY29tcGlsZVByb3ZpZGVyID0gJGNvbXBpbGVQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUuY29udHJvbGxlclByb3ZpZGVyID0gJGNvbnRyb2xsZXJQcm92aWRlcjtcclxuICAgICAgICBQcm92aWRlckNvcmUucHJvdmlkZVNlcnZpY2UgPSAkcHJvdmlkZTtcclxuICAgICAgICBQcm92aWRlckNvcmUuaHR0cFByb3ZpZGVyID0gJGh0dHBQcm92aWRlcjtcclxuXHJcbiAgICAgICAgLyoqXHJcbiAgICAgICAgICogVHJhbnNsYXRpb25zXHJcbiAgICAgICAgICovXHJcblxyXG4gICAgICAgIC8qICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIudXNlU2FuaXRpemVWYWx1ZVN0cmF0ZWd5KG51bGwpO1xyXG5cclxuICAgICAgICAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ3Rkc3RtJyk7XHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci51c2VMb2FkZXIoJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyJywge1xyXG4gICAgICAgICAgICB1cmxUZW1wbGF0ZTogJy4uL2kxOG4ve3BhcnR9L2FwcC5pMThuLXtsYW5nfS5qc29uJ1xyXG4gICAgICAgIH0pOyovXHJcblxyXG4gICAgICAgICR0cmFuc2xhdGVQcm92aWRlci5wcmVmZXJyZWRMYW5ndWFnZSgnZW5fVVMnKTtcclxuICAgICAgICAkdHJhbnNsYXRlUHJvdmlkZXIuZmFsbGJhY2tMYW5ndWFnZSgnZW5fVVMnKTtcclxuXHJcbiAgICAgICAgLy8kdXJsUm91dGVyUHJvdmlkZXIub3RoZXJ3aXNlKCdkYXNoYm9hcmQnKTtcclxuXHJcbiAgICB9XSkuXHJcbiAgICBydW4oWyckdHJhbnNpdGlvbnMnLCAnJGh0dHAnLCAnJGxvZycsICckbG9jYXRpb24nLCAnJHEnLCdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgZnVuY3Rpb24gKCR0cmFuc2l0aW9ucywgJGh0dHAsICRsb2csICRsb2NhdGlvbiwgJHEsIHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UpIHtcclxuICAgICAgICAkbG9nLmRlYnVnKCdDb25maWd1cmF0aW9uIGRlcGxveWVkJyk7XHJcblxyXG4gICAgICAgICR0cmFuc2l0aW9ucy5vbkJlZm9yZSgge30sICgkc3RhdGUsICR0cmFuc2l0aW9uJCkgPT4ge1xyXG4gICAgICAgICAgICB2YXIgZGVmZXIgPSAkcS5kZWZlcigpO1xyXG5cclxuICAgICAgICAgICAgdXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRUaW1lWm9uZUNvbmZpZ3VyYXRpb24oKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgZGVmZXIucmVzb2x2ZSgpO1xyXG4gICAgICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgICAgIHJldHVybiBkZWZlci5wcm9taXNlO1xyXG4gICAgICAgIH0pO1xyXG5cclxuICAgIH1dKTtcclxuXHJcbi8vIHdlIG1hcHBlZCB0aGUgUHJvdmlkZXIgQ29yZSBsaXN0IChjb21waWxlUHJvdmlkZXIsIGNvbnRyb2xsZXJQcm92aWRlciwgcHJvdmlkZVNlcnZpY2UsIGh0dHBQcm92aWRlcikgdG8gcmV1c2UgYWZ0ZXIgb24gZmx5XHJcblREU1RNLlByb3ZpZGVyQ29yZSA9IFByb3ZpZGVyQ29yZTtcclxuXHJcbm1vZHVsZS5leHBvcnRzID0gVERTVE07IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xNC8yMDE1LlxyXG4gKiBJdCBoYW5kbGVyIHRoZSBpbmRleCBmb3IgYW55IG9mIHRoZSBkaXJlY3RpdmVzIGF2YWlsYWJsZVxyXG4gKi9cclxuXHJcbnJlcXVpcmUoJy4vdG9vbHMvVG9hc3RIYW5kbGVyLmpzJyk7XHJcbnJlcXVpcmUoJy4vdG9vbHMvTW9kYWxXaW5kb3dBY3RpdmF0aW9uLmpzJyk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMzAvMTAvMjAxNi5cclxuICogTGlzdGVuIHRvIE1vZGFsIFdpbmRvdyB0byBtYWtlIGFueSBtb2RhbCB3aW5kb3cgZHJhZ2dhYmJsZVxyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi4vLi4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlKCdtb2RhbFJlbmRlcicsIFsnJGxvZycsIGZ1bmN0aW9uICgkbG9nKSB7XHJcbiAgICAkbG9nLmRlYnVnKCdNb2RhbFdpbmRvd0FjdGl2YXRpb24gbG9hZGVkJyk7XHJcbiAgICByZXR1cm4ge1xyXG4gICAgICAgIHJlc3RyaWN0OiAnRUEnLFxyXG4gICAgICAgIGxpbms6IGZ1bmN0aW9uKCkge1xyXG4gICAgICAgICAgICAkKCcubW9kYWwtZGlhbG9nJykuZHJhZ2dhYmxlKHtcclxuICAgICAgICAgICAgICAgIGhhbmRsZTogJy5tb2RhbC1oZWFkZXInXHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH07XHJcbn1dKTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzE0LzIwMTUuXHJcbiAqIFByaW50cyBvdXQgYWxsIFRvYXN0IG1lc3NhZ2Ugd2hlbiBkZXRlY3RlZCBmcm9tIHNlcnZlciBvciBjdXN0b20gbXNnIHVzaW5nIHRoZSBkaXJlY3RpdmUgaXRzZWxmXHJcbiAqXHJcbiAqIFByb2JhYmx5IHZhbHVlcyBhcmU6XHJcbiAqXHJcbiAqIHN1Y2Nlc3MsIGRhbmdlciwgaW5mbywgd2FybmluZ1xyXG4gKlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxudmFyIFREU1RNID0gcmVxdWlyZSgnLi4vLi4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuVERTVE0uY3JlYXRlRGlyZWN0aXZlKCd0b2FzdEhhbmRsZXInLCBbJyRsb2cnLCAnJHRpbWVvdXQnLCAnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InLCAnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcicsXHJcbiAgICAnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yJywgJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJyxcclxuICAgIGZ1bmN0aW9uICgkbG9nLCAkdGltZW91dCwgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IsIEhUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3IsXHJcbiAgICAgICAgICAgICAgSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yLCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvcikge1xyXG5cclxuICAgICRsb2cuZGVidWcoJ1RvYXN0SGFuZGxlciBsb2FkZWQnKTtcclxuICAgIHJldHVybiB7XHJcbiAgICAgICAgc2NvcGU6IHtcclxuICAgICAgICAgICAgbXNnOiAnPScsXHJcbiAgICAgICAgICAgIHR5cGU6ICc9JyxcclxuICAgICAgICAgICAgc3RhdHVzOiAnPSdcclxuICAgICAgICB9LFxyXG4gICAgICAgIHByaW9yaXR5OiA1LFxyXG4gICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL2RpcmVjdGl2ZXMvVG9vbHMvVG9hc3RIYW5kbGVyLmh0bWwnLFxyXG4gICAgICAgIHJlc3RyaWN0OiAnRScsXHJcbiAgICAgICAgY29udHJvbGxlcjogWyckc2NvcGUnLCAnJHJvb3RTY29wZScsIGZ1bmN0aW9uICgkc2NvcGUsICRyb290U2NvcGUpIHtcclxuICAgICAgICAgICAgJHNjb3BlLmFsZXJ0ID0ge1xyXG4gICAgICAgICAgICAgICAgc3VjY2Vzczoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGltZTogMjAwMFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGRhbmdlcjoge1xyXG4gICAgICAgICAgICAgICAgICAgIHNob3c6IGZhbHNlLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1czogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzVGV4dDogJycsXHJcbiAgICAgICAgICAgICAgICAgICAgdGltZTogNDAwMFxyXG4gICAgICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgICAgIGluZm86IHtcclxuICAgICAgICAgICAgICAgICAgICBzaG93OiBmYWxzZSxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXM6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHN0YXR1c1RleHQ6ICcnLFxyXG4gICAgICAgICAgICAgICAgICAgIHRpbWU6IDIwMDBcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICB3YXJuaW5nOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgc2hvdzogZmFsc2UsXHJcbiAgICAgICAgICAgICAgICAgICAgc3RhdHVzOiAnJyxcclxuICAgICAgICAgICAgICAgICAgICBzdGF0dXNUZXh0OiAnJyxcclxuICAgICAgICAgICAgICAgICAgICB0aW1lOiA0MDAwXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB9O1xyXG5cclxuICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzID0ge1xyXG4gICAgICAgICAgICAgICAgc2hvdzogZmFsc2VcclxuICAgICAgICAgICAgfTtcclxuXHJcbiAgICAgICAgICAgIGZ1bmN0aW9uIHR1cm5PZmZOb3RpZmljYXRpb25zKCl7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuc3VjY2Vzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC5pbmZvLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydC53YXJuaW5nLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIC8qKlxyXG4gICAgICAgICAgICAgKiBMaXN0ZW4gdG8gYW55IHJlcXVlc3QsIHdlIGNhbiByZWdpc3RlciBsaXN0ZW5lciBpZiB3ZSB3YW50IHRvIGFkZCBleHRyYSBjb2RlLlxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVxdWVzdCgpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24oY29uZmlnKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgdG86ICcsICBjb25maWcpO1xyXG4gICAgICAgICAgICAgICAgdmFyIHRpbWUgPSBjb25maWcucmVxdWVzdFRpbWVzdGFtcDtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcodGltZSk7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUucHJvZ3Jlc3Muc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5saXN0ZW5FcnJvcigpLnRoZW4obnVsbCwgbnVsbCwgZnVuY3Rpb24ocmVqZWN0aW9uKXtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ1JlcXVlc3QgZXJyb3I6ICcsICByZWplY3Rpb24pO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLnByb2dyZXNzLnNob3cgPSBmYWxzZTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuUmVzcG9uc2UoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlc3BvbnNlKXtcclxuICAgICAgICAgICAgICAgIHZhciB0aW1lID0gcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wIC0gcmVzcG9uc2UuY29uZmlnLnJlcXVlc3RUaW1lc3RhbXA7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdUaGUgcmVxdWVzdCB0b29rICcgKyAodGltZSAvIDEwMDApICsgJyBzZWNvbmRzJyk7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSByZXN1bHQ6ICcsIHJlc3BvbnNlKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IubGlzdGVuRXJyb3IoKS50aGVuKG51bGwsIG51bGwsIGZ1bmN0aW9uKHJlamVjdGlvbil7XHJcbiAgICAgICAgICAgICAgICAkbG9nLmRlYnVnKCdSZXNwb25zZSBlcnJvcjogJywgcmVqZWN0aW9uKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5wcm9ncmVzcy5zaG93ID0gZmFsc2U7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zdGF0dXMgPSByZWplY3Rpb24uc3RhdHVzO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0LmRhbmdlci5zdGF0dXNUZXh0ID0gcmVqZWN0aW9uLnN0YXR1c1RleHQ7XHJcbiAgICAgICAgICAgICAgICAkc2NvcGUuYWxlcnQuZGFuZ2VyLmVycm9ycyA9IHJlamVjdGlvbi5kYXRhLmVycm9ycztcclxuICAgICAgICAgICAgICAgICR0aW1lb3V0KHR1cm5PZmZOb3RpZmljYXRpb25zLCAzMDAwKTtcclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogSGlkZSB0aGUgUG9wIHVwIG5vdGlmaWNhdGlvbiBtYW51YWxseVxyXG4gICAgICAgICAgICAgKi9cclxuICAgICAgICAgICAgJHNjb3BlLm9uQ2FuY2VsUG9wVXAgPSBmdW5jdGlvbigpIHtcclxuICAgICAgICAgICAgICAgIHR1cm5PZmZOb3RpZmljYXRpb25zKCk7XHJcbiAgICAgICAgICAgIH07XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogSXQgd2F0Y2ggdGhlIHZhbHVlIHRvIHNob3cgdGhlIG1zZyBpZiBuZWNlc3NhcnlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRyb290U2NvcGUuJG9uKCdicm9hZGNhc3QtbXNnJywgZnVuY3Rpb24oZXZlbnQsIGFyZ3MpIHtcclxuICAgICAgICAgICAgICAgICRsb2cuZGVidWcoJ2Jyb2FkY2FzdC1tc2cgZXhlY3V0ZWQnKTtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnNob3cgPSB0cnVlO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0W2FyZ3MudHlwZV0uc3RhdHVzVGV4dCA9IGFyZ3MudGV4dDtcclxuICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnN0YXR1cyA9IG51bGw7XHJcbiAgICAgICAgICAgICAgICAkdGltZW91dCh0dXJuT2ZmTm90aWZpY2F0aW9ucywgICRzY29wZS5hbGVydFthcmdzLnR5cGVdLnRpbWUpO1xyXG4gICAgICAgICAgICAgICAgJHNjb3BlLiRhcHBseSgpOyAvLyByb290U2NvcGUgYW5kIHdhdGNoIGV4Y2x1ZGUgdGhlIGFwcGx5IGFuZCBuZWVkcyB0aGUgbmV4dCBjeWNsZSB0byBydW5cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAvKipcclxuICAgICAgICAgICAgICogSXQgd2F0Y2ggdGhlIHZhbHVlIHRvIHNob3cgdGhlIG1zZyBpZiBuZWNlc3NhcnlcclxuICAgICAgICAgICAgICovXHJcbiAgICAgICAgICAgICRzY29wZS4kd2F0Y2goJ21zZycsIGZ1bmN0aW9uKG5ld1ZhbHVlLCBvbGRWYWx1ZSkge1xyXG4gICAgICAgICAgICAgICAgaWYgKG5ld1ZhbHVlICYmIG5ld1ZhbHVlICE9PSAnJykge1xyXG4gICAgICAgICAgICAgICAgICAgICRzY29wZS5hbGVydFskc2NvcGUudHlwZV0uc2hvdyA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0WyRzY29wZS50eXBlXS5zdGF0dXNUZXh0ID0gbmV3VmFsdWU7XHJcbiAgICAgICAgICAgICAgICAgICAgJHNjb3BlLmFsZXJ0WyRzY29wZS50eXBlXS5zdGF0dXMgPSAkc2NvcGUuc3RhdHVzO1xyXG4gICAgICAgICAgICAgICAgICAgICR0aW1lb3V0KHR1cm5PZmZOb3RpZmljYXRpb25zLCAyNTAwKTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIH1dXHJcbiAgICB9O1xyXG59XSk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTEvMTcvMjAxNS5cclxuICovXHJcblxyXG4vLyBNYWluIEFuZ3VsYXJKcyBjb25maWd1cmF0aW9uXHJcbnJlcXVpcmUoJy4vY29uZmlnL0FwcC5qcycpO1xyXG5cclxuLy8gSGVscGVyc1xyXG5yZXF1aXJlKCcuL2NvbmZpZy9Bbmd1bGFyUHJvdmlkZXJIZWxwZXIuanMnKTtcclxuXHJcbi8vIERpcmVjdGl2ZXNcclxucmVxdWlyZSgnLi9kaXJlY3RpdmVzL2luZGV4Jyk7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBEaWFsb2dBY3Rpb24ge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICB0aGlzLnRpdGxlID0gcGFyYW1zLnRpdGxlO1xyXG4gICAgICAgIHRoaXMubWVzc2FnZSA9IHBhcmFtcy5tZXNzYWdlO1xyXG5cclxuICAgIH1cclxuICAgIC8qKlxyXG4gICAgICogQWNjY2VwdCBhbmQgQ29uZmlybVxyXG4gICAgICovXHJcbiAgICBjb25maXJtQWN0aW9uKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSgpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIvMjAxNS5cclxuICogSGVhZGVyIENvbnRyb2xsZXIgbWFuYWdlIHRoZSB2aWV3IGF2YWlsYWJsZSBvbiB0aGUgc3RhdGUuZGF0YVxyXG4gKiAtLS0tLS0tLS0tLS0tLS0tLS0tLS0tXHJcbiAqIEhlYWRlciBDb250cm9sbGVyXHJcbiAqIFBhZ2UgdGl0bGUgICAgICAgICAgICAgICAgICAgICAgSG9tZSAtPiBMYXlvdXQgLSBTdWIgTGF5b3V0XHJcbiAqXHJcbiAqIE1vZHVsZSBDb250cm9sbGVyXHJcbiAqIENvbnRlbnRcclxuICogLS0tLS0tLS0tLS0tLS0tLS0tLS1cclxuICpcclxuICovXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEhlYWRlckNvbnRyb2xsZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSkge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZ1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcblxyXG4gICAgICAgIHRoaXMucGFnZU1ldGFEYXRhID0ge1xyXG4gICAgICAgICAgICB0aXRsZTogJycsXHJcbiAgICAgICAgICAgIGluc3RydWN0aW9uOiAnJyxcclxuICAgICAgICAgICAgbWVudTogW11cclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnByZXBhcmVIZWFkZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSGVhZGVyIENvbnRyb2xsZXIgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBWZXJpZnkgaWYgd2UgaGF2ZSBhIG1lbnUgdG8gc2hvdyB0byBtYWRlIGl0IGF2YWlsYWJsZSB0byB0aGUgVmlld1xyXG4gICAgICovXHJcbiAgICBwcmVwYXJlSGVhZGVyKCkge1xyXG4gICAgICAgIGlmICh0aGlzLnN0YXRlICYmIHRoaXMuc3RhdGUuJGN1cnJlbnQgJiYgdGhpcy5zdGF0ZS4kY3VycmVudC5kYXRhKSB7XHJcbiAgICAgICAgICAgIHRoaXMucGFnZU1ldGFEYXRhID0gdGhpcy5zdGF0ZS4kY3VycmVudC5kYXRhLnBhZ2U7XHJcbiAgICAgICAgICAgIGRvY3VtZW50LnRpdGxlID0gdGhpcy5wYWdlTWV0YURhdGEudGl0bGU7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjEvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBIZWFkZXJDb250cm9sbGVyIGZyb20gJy4vSGVhZGVyQ29udHJvbGxlci5qcyc7XHJcbmltcG9ydCBEaWFsb2dBY3Rpb24gZnJvbSAnLi4vZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5qcyc7XHJcblxyXG52YXIgSGVhZGVyTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkhlYWRlck1vZHVsZScsIFtdKTtcclxuXHJcbkhlYWRlck1vZHVsZS5jb250cm9sbGVyKCdIZWFkZXJDb250cm9sbGVyJywgWyckbG9nJywgJyRzdGF0ZScsIEhlYWRlckNvbnRyb2xsZXJdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuSGVhZGVyTW9kdWxlLmNvbnRyb2xsZXIoJ0RpYWxvZ0FjdGlvbicsIFsnJGxvZycsJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBEaWFsb2dBY3Rpb25dKTtcclxuXHJcbi8qXHJcbiAqIEZpbHRlciBjaGFuZ2UgdGhlIGRhdGUgaW50byBhIHByb3BlciBmb3JtYXQgdGltZXpvbmUgZGF0ZVxyXG4gKi9cclxuSGVhZGVyTW9kdWxlLmZpbHRlcignY29udmVydERhdGVJbnRvVGltZVpvbmUnLCBbJ1VzZXJQcmVmZXJlbmNlc1NlcnZpY2UnLCBmdW5jdGlvbiAodXNlclByZWZlcmVuY2VzU2VydmljZSkge1xyXG4gICAgcmV0dXJuIChkYXRlU3RyaW5nKSA9PiB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlLmdldENvbnZlcnRlZERhdGVJbnRvVGltZVpvbmUoZGF0ZVN0cmluZyk7XHJcbn1dKTtcclxuXHJcbkhlYWRlck1vZHVsZS5maWx0ZXIoJ2NvbnZlcnREYXRlVGltZUludG9UaW1lWm9uZScsIFsnVXNlclByZWZlcmVuY2VzU2VydmljZScsIGZ1bmN0aW9uICh1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlKSB7XHJcbiAgICByZXR1cm4gKGRhdGVTdHJpbmcpID0+IHVzZXJQcmVmZXJlbmNlc1NlcnZpY2UuZ2V0Q29udmVydGVkRGF0ZVRpbWVJbnRvVGltZVpvbmUoZGF0ZVN0cmluZyk7XHJcbn1dKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IEhlYWRlck1vZHVsZTsiLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgdWlSb3V0ZXIgZnJvbSAndWktcm91dGVyJztcclxuXHJcbmltcG9ydCBMaWNlbnNlQWRtaW5MaXN0IGZyb20gJy4vbGlzdC9MaWNlbnNlQWRtaW5MaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VBZG1pblNlcnZpY2UgZnJvbSAnLi9zZXJ2aWNlL0xpY2Vuc2VBZG1pblNlcnZpY2UuanMnO1xyXG5pbXBvcnQgUmVxdWVzdExpY2Vuc2UgZnJvbSAnLi9yZXF1ZXN0L1JlcXVlc3RMaWNlbnNlLmpzJztcclxuaW1wb3J0IENyZWF0ZWRMaWNlbnNlIGZyb20gJy4vY3JlYXRlZC9DcmVhdGVkTGljZW5zZS5qcyc7XHJcbmltcG9ydCBBcHBseUxpY2Vuc2VLZXkgZnJvbSAnLi9hcHBseUxpY2Vuc2VLZXkvQXBwbHlMaWNlbnNlS2V5LmpzJztcclxuaW1wb3J0IE1hbnVhbGx5UmVxdWVzdCBmcm9tICcuL21hbnVhbGx5UmVxdWVzdC9NYW51YWxseVJlcXVlc3QuanMnO1xyXG5pbXBvcnQgTGljZW5zZURldGFpbCBmcm9tICcuL2RldGFpbC9MaWNlbnNlRGV0YWlsLmpzJztcclxuXHJcblxyXG52YXIgTGljZW5zZUFkbWluTW9kdWxlID0gYW5ndWxhci5tb2R1bGUoJ1REU1RNLkxpY2Vuc2VBZG1pbk1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJywgJyRsb2NhdGlvblByb3ZpZGVyJyxcclxuXHRcdGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlciwgJGxvY2F0aW9uUHJvdmlkZXIpIHtcclxuXHJcblx0XHQkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyLmFkZFBhcnQoJ2xpY2Vuc2VBZG1pbicpO1xyXG5cclxuXHRcdC8vIERlZmluZSBhIGdlbmVyaWMgaGVhZGVyIGZvciB0aGUgZW50aXJlIG1vZHVsZSwgb3IgaXQgY2FuIGJlIGNoYW5nZWQgZm9yIGVhY2ggaW5zdGFuY2UuXHJcblx0XHR2YXIgaGVhZGVyID0ge1xyXG5cdFx0XHRcdHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvaGVhZGVyL0hlYWRlclZpZXcuaHRtbCcsXHJcblx0XHRcdFx0Y29udHJvbGxlcjogJ0hlYWRlckNvbnRyb2xsZXIgYXMgaGVhZGVyJ1xyXG5cdFx0fTtcclxuXHJcblx0XHQkc3RhdGVQcm92aWRlclxyXG5cdFx0XHRcdC5zdGF0ZSgnbGljZW5zZUFkbWluTGlzdCcsIHtcclxuXHRcdFx0XHRcdFx0ZGF0YToge3BhZ2U6IHt0aXRsZTogJ0FkbWluaXN0ZXIgTGljZW5zZXMnLCBpbnN0cnVjdGlvbjogJycsIG1lbnU6IFsnQWRtaW4nLCAnTGljZW5zZScsICdMaXN0J119fSxcclxuXHRcdFx0XHRcdFx0dXJsOiAnL2xpY2Vuc2UvYWRtaW4vbGlzdCcsXHJcblx0XHRcdFx0XHRcdHZpZXdzOiB7XHJcblx0XHRcdFx0XHRcdFx0XHQnaGVhZGVyVmlld0AnOiBoZWFkZXIsXHJcblx0XHRcdFx0XHRcdFx0XHQnYm9keVZpZXdAJzoge1xyXG5cdFx0XHRcdFx0XHRcdFx0XHRcdHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2xpc3QvTGljZW5zZUFkbWluTGlzdC5odG1sJyxcclxuXHRcdFx0XHRcdFx0XHRcdFx0XHRjb250cm9sbGVyOiAnTGljZW5zZUFkbWluTGlzdCBhcyBsaWNlbnNlQWRtaW5MaXN0J1xyXG5cdFx0XHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0fSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsICckcm9vdFNjb3BlJywgTGljZW5zZUFkbWluU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0xpY2Vuc2VBZG1pbkxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTGljZW5zZUFkbWluTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignUmVxdWVzdExpY2Vuc2UnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgUmVxdWVzdExpY2Vuc2VdKTtcclxuTGljZW5zZUFkbWluTW9kdWxlLmNvbnRyb2xsZXIoJ0NyZWF0ZWRMaWNlbnNlJywgWyckbG9nJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIENyZWF0ZWRMaWNlbnNlXSk7XHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5jb250cm9sbGVyKCdBcHBseUxpY2Vuc2VLZXknLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VBZG1pblNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIEFwcGx5TGljZW5zZUtleV0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTWFudWFsbHlSZXF1ZXN0JywgWyckbG9nJywgJyRzY29wZScsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIE1hbnVhbGx5UmVxdWVzdF0pO1xyXG5MaWNlbnNlQWRtaW5Nb2R1bGUuY29udHJvbGxlcignTGljZW5zZURldGFpbCcsIFsnJGxvZycsICdMaWNlbnNlQWRtaW5TZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlRGV0YWlsXSk7XHJcblxyXG4vKlxyXG4gKiBGaWx0ZXIgdG8gVVJMIEVuY29kZSB0ZXh0IGZvciB0aGUgJ21haWx0bydcclxuICovXHJcbkxpY2Vuc2VBZG1pbk1vZHVsZS5maWx0ZXIoJ2VzY2FwZVVSTEVuY29kaW5nJywgZnVuY3Rpb24gKCkge1xyXG5cdHJldHVybiBmdW5jdGlvbiAodGV4dCkge1xyXG5cdFx0aWYodGV4dCl7XHJcblx0XHRcdHRleHQgPSBlbmNvZGVVUkkodGV4dCk7XHJcblx0XHR9XHJcblx0XHRyZXR1cm4gdGV4dDtcclxuXHR9XHJcbn0pO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgTGljZW5zZUFkbWluTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQXBwbHlMaWNlbnNlS2V5IGV4dGVuZHMgRm9ybVZhbGlkYXRvcntcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkc2NvcGUsIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSlcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG5cclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG4gICAgICAgICAgICBrZXk6IHBhcmFtcy5saWNlbnNlLmtleVxyXG4gICAgICAgIH1cclxuICAgICAgICA7XHJcbiAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBFeGVjdXRlIGFuZCB2YWxpZGF0ZSB0aGUgS2V5IGlzIGNvcnJlY3RcclxuICAgICAqL1xyXG4gICAgYXBwbHlLZXkoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmFwcGx5TGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSwgKGRhdGEpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBDcmVhdGVkUmVxdWVzdExpY2Vuc2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmNsaWVudCA9IHBhcmFtcztcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIExpY2Vuc2VEZXRhaWwge1xyXG5cclxuXHRcdGNvbnN0cnVjdG9yKCRsb2csIGxpY2Vuc2VBZG1pblNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UsIHBhcmFtcykge1xyXG5cdFx0XHRcdHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcblx0XHRcdFx0dGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcblx0XHRcdFx0dGhpcy51aWJNb2RhbCA9JHVpYk1vZGFsO1xyXG5cdFx0XHRcdHRoaXMubG9nID0gJGxvZztcclxuXHRcdFx0XHR0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuXHRcdFx0XHRcdFx0bWV0aG9kOiB7XHJcblx0XHRcdFx0XHRcdFx0XHRuYW1lOiBwYXJhbXMubGljZW5zZS5tZXRob2QubmFtZSxcclxuXHRcdFx0XHRcdFx0XHRcdG1heDogcGFyYW1zLmxpY2Vuc2UubWV0aG9kLm1heFxyXG5cdFx0XHRcdFx0XHR9LFxyXG5cdFx0XHRcdFx0XHRwcm9qZWN0TmFtZTogcGFyYW1zLmxpY2Vuc2UucHJvamVjdC5uYW1lLFxyXG5cdFx0XHRcdFx0XHRjbGllbnROYW1lOiBwYXJhbXMubGljZW5zZS5jbGllbnQubmFtZSxcclxuXHRcdFx0XHRcdFx0ZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG5cdFx0XHRcdFx0XHRlbnZpcm9ubWVudDogcGFyYW1zLmxpY2Vuc2UuZW52aXJvbm1lbnQsXHJcblx0XHRcdFx0XHRcdGluY2VwdGlvbjogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdERhdGUsXHJcblx0XHRcdFx0XHRcdGV4cGlyYXRpb246IHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlLFxyXG5cdFx0XHRcdFx0XHRyZXF1ZXN0Tm90ZTogcGFyYW1zLmxpY2Vuc2UucmVxdWVzdE5vdGUsXHJcblx0XHRcdFx0XHRcdGFjdGl2ZTogcGFyYW1zLmxpY2Vuc2Uuc3RhdHVzID09PSAnQUNUSVZFJyxcclxuXHRcdFx0XHRcdFx0aWQ6IHBhcmFtcy5saWNlbnNlLmlkLFxyXG5cdFx0XHRcdFx0XHRyZXBsYWNlZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWQsXHJcblx0XHRcdFx0XHRcdGVuY3J5cHRlZERldGFpbDogcGFyYW1zLmxpY2Vuc2UuZW5jcnlwdGVkRGV0YWlsLFxyXG5cdFx0XHRcdFx0XHRhcHBsaWVkOiBmYWxzZVxyXG5cdFx0XHRcdH07XHJcblxyXG5cdFx0XHRcdHRoaXMucHJlcGFyZU1ldGhvZE9wdGlvbnMoKTtcclxuXHRcdH1cclxuXHJcblx0XHRwcmVwYXJlTWV0aG9kT3B0aW9ucygpIHtcclxuXHRcdFx0XHR0aGlzLm1ldGhvZE9wdGlvbnMgPSBbXHJcblx0XHRcdFx0XHRcdHtcclxuXHRcdFx0XHRcdFx0XHRcdG5hbWU6ICdNQVhfU0VSVkVSUycsXHJcblx0XHRcdFx0XHRcdFx0XHR0ZXh0OiAnU2VydmVycydcclxuXHRcdFx0XHRcdFx0fSxcclxuXHRcdFx0XHRcdFx0e1xyXG5cdFx0XHRcdFx0XHRcdFx0bmFtZTogJ1RPS0VOJyxcclxuXHRcdFx0XHRcdFx0XHRcdHRleHQ6ICdUb2tlbnMnXHJcblx0XHRcdFx0XHRcdH0sXHJcblx0XHRcdFx0XHRcdHtcclxuXHRcdFx0XHRcdFx0XHRcdG5hbWU6ICdDVVNUT00nLFxyXG5cdFx0XHRcdFx0XHRcdFx0dGV4dDogJ0N1c3RvbSdcclxuXHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdF1cclxuXHRcdH1cclxuXHJcblx0XHQvKipcclxuXHRcdCAqIFRoZSB1c2VyIGFwcGx5IGFuZCBzZXJ2ZXIgc2hvdWxkIHZhbGlkYXRlIHRoZSBrZXkgaXMgY29ycmVjdFxyXG5cdFx0ICovXHJcblx0XHRhcHBseUxpY2Vuc2VLZXkoKSB7XHJcblx0XHRcdFx0dmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG5cdFx0XHRcdFx0XHRhbmltYXRpb246IHRydWUsXHJcblx0XHRcdFx0XHRcdHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2FwcGx5TGljZW5zZUtleS9BcHBseUxpY2Vuc2VLZXkuaHRtbCcsXHJcblx0XHRcdFx0XHRcdGNvbnRyb2xsZXI6ICdBcHBseUxpY2Vuc2VLZXkgYXMgYXBwbHlMaWNlbnNlS2V5JyxcclxuXHRcdFx0XHRcdFx0c2l6ZTogJ21kJyxcclxuXHRcdFx0XHRcdFx0cmVzb2x2ZToge1xyXG5cdFx0XHRcdFx0XHRcdFx0cGFyYW1zOiAoKSA9PiB7XHJcblx0XHRcdFx0XHRcdFx0XHRcdFx0cmV0dXJuIHsgbGljZW5zZTogdGhpcy5saWNlbnNlTW9kZWwgfTtcclxuXHRcdFx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdH0pO1xyXG5cclxuXHRcdFx0XHRtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChkYXRhKSA9PiB7XHJcblx0XHRcdFx0XHRcdHRoaXMubGljZW5zZU1vZGVsLmFwcGxpZWQgPSBkYXRhLnN1Y2Nlc3M7XHJcblx0XHRcdFx0XHRcdGlmKGRhdGEuc3VjY2Vzcykge1xyXG5cdFx0XHRcdFx0XHRcdFx0dGhpcy5saWNlbnNlTW9kZWwuYWN0aXZlID0gZGF0YS5zdWNjZXNzO1xyXG5cdFx0XHRcdFx0XHRcdFx0dGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKHsgaWQ6IHRoaXMubGljZW5zZU1vZGVsLmlkLCB1cGRhdGVkOiB0cnVlfSk7XHJcblx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHR9KTtcclxuXHRcdH1cclxuXHJcblx0XHQvKipcclxuXHRcdCAqIE9wZW5zIGEgZGlhbG9nIGFuZCBhbGxvdyB0aGUgdXNlciB0byBtYW51YWxseSBzZW5kIHRoZSByZXF1ZXN0IG9yIGNvcHkgdGhlIGVuY3JpcHRlZCBjb2RlXHJcblx0XHQgKi9cclxuXHRcdG1hbnVhbGx5UmVxdWVzdCgpIHtcclxuXHRcdFx0XHR2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcblx0XHRcdFx0XHRcdGFuaW1hdGlvbjogdHJ1ZSxcclxuXHRcdFx0XHRcdFx0dGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vbWFudWFsbHlSZXF1ZXN0L01hbnVhbGx5UmVxdWVzdC5odG1sJyxcclxuXHRcdFx0XHRcdFx0Y29udHJvbGxlcjogJ01hbnVhbGx5UmVxdWVzdCBhcyBtYW51YWxseVJlcXVlc3QnLFxyXG5cdFx0XHRcdFx0XHRzaXplOiAnbWQnLFxyXG5cdFx0XHRcdFx0XHRyZXNvbHZlOiB7XHJcblx0XHRcdFx0XHRcdFx0XHRwYXJhbXM6ICgpID0+IHtcclxuXHRcdFx0XHRcdFx0XHRcdFx0XHRyZXR1cm4geyBsaWNlbnNlOiB0aGlzLmxpY2Vuc2VNb2RlbCB9O1xyXG5cdFx0XHRcdFx0XHRcdFx0fVxyXG5cdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0fSk7XHJcblxyXG5cdFx0XHRcdG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge30pO1xyXG5cdFx0fVxyXG5cclxuXHRcdC8qKlxyXG5cdFx0ICogSWYgYnkgc29tZSByZWFzb24gdGhlIExpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkIGF0IGZpcnN0IHRpbWUsIHRoaXMgd2lsbCBkbyBhIHJlcXVlc3QgZm9yIGl0XHJcblx0XHQgKi9cclxuXHRcdHJlc3VibWl0TGljZW5zZVJlcXVlc3QoKSB7XHJcblx0XHRcdFx0dGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLnJlc3VibWl0TGljZW5zZVJlcXVlc3QodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7fSk7XHJcblx0XHR9XHJcblxyXG5cdFx0ZGVsZXRlTGljZW5zZSgpIHtcclxuXHRcdFx0XHR2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcblx0XHRcdFx0XHRcdGFuaW1hdGlvbjogdHJ1ZSxcclxuXHRcdFx0XHRcdFx0dGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG5cdFx0XHRcdFx0XHRjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcblx0XHRcdFx0XHRcdHNpemU6ICdzbScsXHJcblx0XHRcdFx0XHRcdHJlc29sdmU6IHtcclxuXHRcdFx0XHRcdFx0XHRcdHBhcmFtczogKCkgPT4ge1xyXG5cdFx0XHRcdFx0XHRcdFx0XHRcdHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byBkZWxldGUgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcblx0XHRcdFx0XHRcdFx0XHR9XHJcblx0XHRcdFx0XHRcdH1cclxuXHRcdFx0XHR9KTtcclxuXHJcblx0XHRcdFx0bW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcblx0XHRcdFx0XHRcdHRoaXMubGljZW5zZUFkbWluU2VydmljZS5kZWxldGVMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG5cdFx0XHRcdFx0XHRcdFx0dGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG5cdFx0XHRcdFx0XHR9KTtcclxuXHRcdFx0XHR9KTtcclxuXHRcdH1cclxuXHJcblx0XHQvKipcclxuXHRcdCAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG5cdFx0ICovXHJcblx0XHRjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuXHRcdFx0XHRpZih0aGlzLmxpY2Vuc2VNb2RlbC5hcHBsaWVkKSB7XHJcblx0XHRcdFx0XHRcdHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSgpO1xyXG5cdFx0XHRcdH1cclxuXHRcdFx0XHR0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcblx0XHR9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNS8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZUFkbWluTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UgPSBsaWNlbnNlQWRtaW5TZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcblxyXG4gICAgICAgIHRoaXMuZ2V0RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdMaWNlbnNlQWRtaW5MaXN0IEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3Qub25SZXF1ZXN0TmV3TGljZW5zZSgpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBsdXNcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+IFJlcXVlc3QgTmV3IExpY2Vuc2U8L2J1dHRvbj4gPGRpdiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3QucmVsb2FkTGljZW5zZUFkbWluTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1LFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDIwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2xpY2Vuc2VJZCcsIGhpZGRlbjogdHJ1ZSB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYWN0aW9uJywgZmlsdGVyYWJsZTogZmFsc2UsIHRpdGxlOiAnQWN0aW9uJywgd2lkdGg6IDgwLCB0ZW1wbGF0ZTogJzxidXR0b24gY2xhc3M9XCJidG4gYnRuLWRlZmF1bHRcIiBuZy1jbGljaz1cImxpY2Vuc2VBZG1pbkxpc3Qub25MaWNlbnNlRGV0YWlscyh0aGlzKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wZW5jaWxcIj48L3NwYW4+PC9idXR0b24+JyB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2xpZW50Lm5hbWUnLCB0aXRsZTogJ0NsaWVudCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAncHJvamVjdC5uYW1lJywgdGl0bGU6ICdQcm9qZWN0JywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLnByb2plY3QgJiYgZGF0YS5wcm9qZWN0Lm5hbWUpPyBkYXRhLnByb2plY3QubmFtZS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLnN0YXR1cyk/IGRhdGEuc3RhdHVzLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnLCAgdGVtcGxhdGU6ICcjaWYoZGF0YS50eXBlICYmIGRhdGEudHlwZS5uYW1lID09PSBcIk1VTFRJX1BST0pFQ1RcIil7IyBHbG9iYWwgIyB9IGVsc2UgeyMgU2luZ2xlICN9Iyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCcsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5tZXRob2QgJiYgZGF0YS5tZXRob2QubmFtZSk/IGRhdGEubWV0aG9kLm5hbWUudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5tYXgnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3JlcXVlc3REYXRlJywgdGl0bGU6ICdJbmNlcHRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nLCB0ZW1wbGF0ZTogJ3t7IGRhdGFJdGVtLnJlcXVlc3REYXRlIHwgY29udmVydERhdGVJbnRvVGltZVpvbmUgfX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uRGF0ZScsIHRpdGxlOiAnRXhwaXJhdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScsIHRlbXBsYXRlOiAne3sgZGF0YUl0ZW0uZXhwaXJhdGlvbkRhdGUgfCBjb252ZXJ0RGF0ZUludG9UaW1lWm9uZSB9fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Vudmlyb25tZW50JywgdGl0bGU6ICdFbnZpcm9ubWVudCcsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5lbnZpcm9ubWVudCk/IGRhdGEuZW52aXJvbm1lbnQudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VBZG1pblNlcnZpY2UuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICdwcm9qZWN0Lm5hbWUnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBjaGFuZ2U6ICAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIC8vIFdlIGFyZSBjb21pbmcgZnJvbSBhIG5ldyBpbXBvcnRlZCByZXF1ZXN0IGxpY2Vuc2VcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzLm9wZW5MYXN0TGljZW5zZUlkICE9PSAwICYmIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB2YXIgbGFzdExpY2Vuc2UgPSB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEuZmluZCgobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGxpY2Vuc2UuaWQgPT09IHRoaXMub3Blbkxhc3RMaWNlbnNlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vcGVuTGFzdExpY2Vuc2VJZCA9IDA7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICBpZihsYXN0TGljZW5zZSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5vbkxpY2Vuc2VEZXRhaWxzKGxhc3RMaWNlbnNlKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc29ydGFibGU6IHRydWUsXHJcbiAgICAgICAgICAgIGZpbHRlcmFibGU6IHtcclxuICAgICAgICAgICAgICAgIGV4dHJhOiBmYWxzZVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE9wZW4gYSBkaWFsb2cgd2l0aCB0aGUgQmFzaWMgRm9ybSB0byByZXF1ZXN0IGEgTmV3IExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgb25SZXF1ZXN0TmV3TGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlQWRtaW4vcmVxdWVzdC9SZXF1ZXN0TGljZW5zZS5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1JlcXVlc3RMaWNlbnNlIGFzIHJlcXVlc3RMaWNlbnNlJyxcclxuICAgICAgICAgICAgc2l6ZTogJ21kJ1xyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChsaWNlbnNlKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ05ldyBMaWNlbnNlIENyZWF0ZWQ6ICcsIGxpY2Vuc2UpO1xyXG4gICAgICAgICAgICB0aGlzLm9uTmV3TGljZW5zZUNyZWF0ZWQobGljZW5zZSk7XHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZUFkbWluTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEFmdGVyIGNsaWNraW5nIG9uIGVkaXQsIHdlIHJlZGlyZWN0IHRoZSB1c2VyIHRvIHRoZSBFZGl0aW9uIHNjcmVlbiBpbnN0ZWFkIG9mIG9wZW4gYSBkaWFsb2dcclxuICAgICAqIGR1IHRoZSBzaXplIG9mIHRoZSBpbnB1dHNcclxuICAgICAqL1xyXG4gICAgb25MaWNlbnNlRGV0YWlscyhsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnT3BlbiBEZXRhaWxzIGZvcjogJywgbGljZW5zZSk7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2RldGFpbC9MaWNlbnNlRGV0YWlsLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZURldGFpbCBhcyBsaWNlbnNlRGV0YWlsJyxcclxuICAgICAgICAgICAgc2l6ZTogJ2xnJyxcclxuICAgICAgICAgICAgcmVzb2x2ZToge1xyXG4gICAgICAgICAgICAgICAgcGFyYW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgdmFyIGRhdGFJdGVtID0ge307XHJcbiAgICAgICAgICAgICAgICAgICAgaWYobGljZW5zZSAmJiBsaWNlbnNlLmRhdGFJdGVtKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGRhdGFJdGVtID0gbGljZW5zZS5kYXRhSXRlbTtcclxuICAgICAgICAgICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICBkYXRhSXRlbSA9IGxpY2Vuc2U7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGxpY2Vuc2U6IGRhdGFJdGVtIH07XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLm9wZW5MYXN0TGljZW5zZUlkID0gMDtcclxuICAgICAgICAgICAgaWYoZGF0YS51cGRhdGVkKSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9wZW5MYXN0TGljZW5zZUlkID0gZGF0YS5pZDsgLy8gdGFrZSB0aGlzIHBhcmFtIGZyb20gdGhlIGxhc3QgaW1wb3J0ZWQgbGljZW5zZSwgb2YgY291cnNlXHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZUFkbWluTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnUmVxdWVzdCBDYW5jZWxlZC4nKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBvbk5ld0xpY2Vuc2VDcmVhdGVkKGxpY2Vuc2UpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZUFkbWluL2NyZWF0ZWQvQ3JlYXRlZExpY2Vuc2UuaHRtbCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdtZCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdDcmVhdGVkTGljZW5zZSBhcyBjcmVhdGVkTGljZW5zZScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogZnVuY3Rpb24gKCkge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGVtYWlsOiBsaWNlbnNlLmVtYWlsICB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVsb2FkTGljZW5zZUFkbWluTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLnJlYWQoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yOC8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIE1hbnVhbGx5UmVxdWVzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCBsaWNlbnNlQWRtaW5TZXJ2aWNlLCAkdWliTW9kYWxJbnN0YW5jZSwgcGFyYW1zKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc2NvcGUgPSAkc2NvcGU7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlID0gbGljZW5zZUFkbWluU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaWQ6ICBwYXJhbXMubGljZW5zZS5pZCxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBlbmNyeXB0ZWREZXRhaWw6ICcnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgLy8gR2V0IHRoZSBoYXNoIGNvZGUgdXNpbmcgdGhlIGlkLlxyXG4gICAgICAgIHRoaXMuZ2V0SGFzaENvZGUoKTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0SGFzaENvZGUoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldEhhc2hDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbmNyeXB0ZWREZXRhaWwgPSBkYXRhO1xyXG4gICAgICAgICAgICB3aW5kb3cuVERTVE0uc2FmZUFwcGx5KHRoaXMuc2NvcGUpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRGlzbWlzcyB0aGUgZGlhbG9nLCBubyBhY3Rpb24gbmVjZXNzYXJ5XHJcbiAgICAgKi9cclxuICAgIGNhbmNlbENsb3NlRGlhbG9nKCkge1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI2LzIwMTYuXHJcbiAqIENyZWF0ZSBhIG5ldyBSZXF1ZXN0IHRvIGdldCBhIExpY2Vuc2VcclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdExpY2Vuc2UgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIC8qKlxyXG4gICAgICogSW5pdGlhbGl6ZSBhbGwgdGhlIHByb3BlcnRpZXNcclxuICAgICAqIEBwYXJhbSAkbG9nXHJcbiAgICAgKiBAcGFyYW0gbGljZW5zZUFkbWluU2VydmljZVxyXG4gICAgICogQHBhcmFtICR1aWJNb2RhbEluc3RhbmNlXHJcbiAgICAgKi9cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZUFkbWluU2VydmljZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSkge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpO1xyXG4gICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZSA9IGxpY2Vuc2VBZG1pblNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlID0gJHVpYk1vZGFsSW5zdGFuY2U7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG5cclxuICAgICAgICAvLyBEZWZpbmVkIHRoZSBFbnZpcm9ubWVudCBTZWxlY3RcclxuICAgICAgICB0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSA9IFtdO1xyXG4gICAgICAgIC8vIERlZmluZSB0aGUgUHJvamVjdCBTZWxlY3RcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3QgPSB7fTtcclxuICAgICAgICB0aGlzLnNlbGVjdFByb2plY3RMaXN0T3B0aW9ucyA9IFtdO1xyXG5cclxuICAgICAgICB0aGlzLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpO1xyXG4gICAgICAgIHRoaXMuZ2V0UHJvamVjdERhdGFTb3VyY2UoKTtcclxuXHJcbiAgICAgICAgLy8gQ3JlYXRlIHRoZSBNb2RlbCBmb3IgdGhlIE5ldyBMaWNlbnNlXHJcbiAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwgPSB7XHJcbiAgICAgICAgICAgIGVtYWlsOiAnJyxcclxuICAgICAgICAgICAgZW52aXJvbm1lbnQ6ICcnLFxyXG4gICAgICAgICAgICBwcm9qZWN0SWQ6IDAsXHJcbiAgICAgICAgICAgIGNsaWVudE5hbWU6ICcnLFxyXG4gICAgICAgICAgICByZXF1ZXN0Tm90ZTogJydcclxuICAgICAgICB9XHJcblxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdGhlIEVudmlyb25tZW50IGRyb3Bkb3duIHZhbHVlc1xyXG4gICAgICovXHJcbiAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlQWRtaW5TZXJ2aWNlLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSk9PntcclxuICAgICAgICAgICAgdGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UgPSBkYXRhO1xyXG4gICAgICAgICAgICBpZih0aGlzLmVudmlyb25tZW50RGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICAgICAgdmFyIGluZGV4ID0gdGhpcy5lbnZpcm9ubWVudERhdGFTb3VyY2UuZmluZEluZGV4KGZ1bmN0aW9uKGVudmlyb21lbnQpe1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBlbnZpcm9tZW50ICA9PT0gJ1BST0RVQ1RJT04nO1xyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgICAgICAgICBpbmRleCA9IGluZGV4IHx8IDA7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm5ld0xpY2Vuc2VNb2RlbC5lbnZpcm9ubWVudCA9IGRhdGFbaW5kZXhdO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgUHJvamVjdCBkcm9wZG93biB2YWx1ZXNcclxuICAgICAqL1xyXG4gICAgZ2V0UHJvamVjdERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5zZWxlY3RQcm9qZWN0TGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5uZXdMaWNlbnNlTW9kZWwucHJvamVjdElkID0gZGF0YVswXS5pZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5uZXdMaWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSlcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGFUZXh0RmllbGQ6ICduYW1lJyxcclxuICAgICAgICAgICAgZGF0YVZhbHVlRmllbGQ6ICdpZCcsXHJcbiAgICAgICAgICAgIHZhbHVlUHJpbWl0aXZlOiB0cnVlLFxyXG4gICAgICAgICAgICBzZWxlY3Q6ICgoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgLy8gT24gUHJvamVjdCBDaGFuZ2UsIHNlbGVjdCB0aGUgQ2xpZW50IE5hbWVcclxuICAgICAgICAgICAgICAgIHZhciBpdGVtID0gdGhpcy5zZWxlY3RQcm9qZWN0LmRhdGFJdGVtKGUuaXRlbSk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm5ld0xpY2Vuc2VNb2RlbC5jbGllbnROYW1lID0gaXRlbS5jbGllbnQubmFtZTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogRXhlY3V0ZSB0aGUgU2VydmljZSBjYWxsIHRvIGdlbmVyYXRlIGEgbmV3IExpY2Vuc2UgcmVxdWVzdFxyXG4gICAgICovXHJcbiAgICBzYXZlTGljZW5zZVJlcXVlc3QoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTmV3IExpY2Vuc2UgUmVxdWVzdGVkOiAnLCB0aGlzLm5ld0xpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZUFkbWluU2VydmljZS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdCh0aGlzLm5ld0xpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZSh0aGlzLm5ld0xpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAwOS8yNi8xNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlQWRtaW5TZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIsICRyb290U2NvcGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLnJvb3RTY29wZSA9ICRyb290U2NvcGU7XHJcbiAgICAgICAgdGhpcy5zdGF0dXNTdWNjZXNzID0gJ3N1Y2Nlc3MnO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdsaWNlbnNlQWRtaW5TZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGdldExpY2Vuc2VMaXN0KG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldFByb2plY3REYXRhU291cmNlKG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5nZXRQcm9qZWN0RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0SGFzaENvZGUobGljZW5zZUlkLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkuZ2V0SGFzaENvZGUobGljZW5zZUlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEuZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBDcmVhdGUgYSBOZXcgTGljZW5zZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5ld0xpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCBvblN1Y2Nlc3Mpe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5jcmVhdGVOZXdMaWNlbnNlUmVxdWVzdChuZXdMaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLnJlc3VibWl0TGljZW5zZVJlcXVlc3QobGljZW5zZS5pZCwgKGRhdGEpID0+IHtcclxuXHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdSZXF1ZXN0IExpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSd9KTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnd2FybmluZycsIHRleHQ6IGRhdGEuZGF0YX0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2Vzcyh7IHN1Y2Nlc3M6IGZhbHNlfSk7XHJcbiAgICAgICAgICAgIH1cclxuXHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZW1haWxSZXF1ZXN0KGxpY2Vuc2UsIGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlQWRtaW5TZXJ2aWNlSGFuZGxlcigpLmVtYWlsUmVxdWVzdChsaWNlbnNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ2luZm8nLCB0ZXh0OiAnUmVxdWVzdCBMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkuJ30pO1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiAgQXBwbHkgVGhlIExpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBsaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gb25TdWNjZXNzXHJcbiAgICAgKi9cclxuICAgIGFwcGx5TGljZW5zZShsaWNlbnNlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuXHJcbiAgICAgICAgdmFyIGhhc2ggPSAge1xyXG4gICAgICAgICAgICBoYXNoOiBsaWNlbnNlLmtleVxyXG4gICAgICAgIH07XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5hcHBseUxpY2Vuc2UobGljZW5zZS5pZCwgaGFzaCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICdpbmZvJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIHN1Y2Nlc3NmdWxseSBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICB9IGVsc2Uge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7IHR5cGU6ICd3YXJuaW5nJywgdGV4dDogJ0xpY2Vuc2Ugd2FzIG5vdCBhcHBsaWVkJ30pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG9uRXJyb3IoeyBzdWNjZXNzOiBmYWxzZX0pO1xyXG4gICAgICAgICAgICB9XHJcblxyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKHsgc3VjY2VzczogdHJ1ZX0pO1xyXG5cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBkZWxldGVMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZUFkbWluU2VydmljZUhhbmRsZXIoKS5kZWxldGVMaWNlbnNlKGxpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjUvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyTGlzdCBmcm9tICcuL2xpc3QvTGljZW5zZU1hbmFnZXJMaXN0LmpzJztcclxuaW1wb3J0IExpY2Vuc2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTGljZW5zZU1hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFJlcXVlc3RJbXBvcnQgZnJvbSAnLi9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuanMnO1xyXG5pbXBvcnQgTGljZW5zZU1hbmFnZXJEZXRhaWwgZnJvbSAnLi9kZXRhaWwvTGljZW5zZU1hbmFnZXJEZXRhaWwuanMnO1xyXG5cclxuXHJcbnZhciBMaWNlbnNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5MaWNlbnNlTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgICckdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlcikge1xyXG5cclxuICAgICR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXIuYWRkUGFydCgnbGljZW5zZU1hbmFnZXInKTtcclxuXHJcbiAgICAvLyBEZWZpbmUgYSBnZW5lcmljIGhlYWRlciBmb3IgdGhlIGVudGlyZSBtb2R1bGUsIG9yIGl0IGNhbiBiZSBjaGFuZ2VkIGZvciBlYWNoIGluc3RhbmNlLlxyXG4gICAgdmFyIGhlYWRlciA9IHtcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJy4uL2FwcC1qcy9tb2R1bGVzL2hlYWRlci9IZWFkZXJWaWV3Lmh0bWwnLFxyXG4gICAgICAgIGNvbnRyb2xsZXI6ICdIZWFkZXJDb250cm9sbGVyIGFzIGhlYWRlcidcclxuICAgIH07XHJcblxyXG4gICAgJHN0YXRlUHJvdmlkZXJcclxuICAgICAgICAuc3RhdGUoJ2xpY2Vuc2VNYW5hZ2VyTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ0xpY2Vuc2luZyBNYW5hZ2VyJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ01hbmFnZXInLCAnTGljZW5zZScsICdMaXN0J119fSxcclxuICAgICAgICAgICAgdXJsOiAnL2xpY2Vuc2UvbWFuYWdlci9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9saXN0L0xpY2Vuc2VNYW5hZ2VyTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJMaXN0IGFzIGxpY2Vuc2VNYW5hZ2VyTGlzdCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG59XSk7XHJcblxyXG4vLyBTZXJ2aWNlc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5zZXJ2aWNlKCdMaWNlbnNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgJyRyb290U2NvcGUnLCBMaWNlbnNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcblxyXG4vLyBDb250cm9sbGVyc1xyXG5MaWNlbnNlTWFuYWdlck1vZHVsZS5jb250cm9sbGVyKCdMaWNlbnNlTWFuYWdlckxpc3QnLCBbJyRsb2cnLCAnJHN0YXRlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBMaWNlbnNlTWFuYWdlckxpc3RdKTtcclxuXHJcbi8vIE1vZGFsIC0gQ29udHJvbGxlcnNcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignUmVxdWVzdEltcG9ydCcsIFsnJGxvZycsICckc2NvcGUnLCAnTGljZW5zZU1hbmFnZXJTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsIFJlcXVlc3RJbXBvcnRdKTtcclxuTGljZW5zZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTGljZW5zZU1hbmFnZXJEZXRhaWwnLCBbJyRsb2cnLCAnJHNjb3BlJywgJ0xpY2Vuc2VNYW5hZ2VyU2VydmljZScsICdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgJyR1aWJNb2RhbCcsICckdWliTW9kYWxJbnN0YW5jZScsICdwYXJhbXMnLCBMaWNlbnNlTWFuYWdlckRldGFpbF0pO1xyXG5cclxuXHJcbmV4cG9ydCBkZWZhdWx0IExpY2Vuc2VNYW5hZ2VyTW9kdWxlOyIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJEZXRhaWwgZXh0ZW5kcyBGb3JtVmFsaWRhdG9ye1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzY29wZSwgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMsIHRpbWVab25lQ29uZmlndXJhdGlvbikge1xyXG4gICAgICAgIHN1cGVyKCRsb2csICRzY29wZSwgJHVpYk1vZGFsLCAkdWliTW9kYWxJbnN0YW5jZSk7XHJcbiAgICAgICAgdGhpcy5zY29wZSA9ICRzY29wZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVzZXJQcmVmZXJlbmNlc1NlcnZpY2UgPSB1c2VyUHJlZmVyZW5jZXNTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZSA9ICR1aWJNb2RhbEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuXHJcbiAgICAgICAgdGhpcy50aW1lWm9uZUNvbmZpZ3VyYXRpb24gPSB0aW1lWm9uZUNvbmZpZ3VyYXRpb247XHJcblxyXG4gICAgICAgIHRoaXMubGljZW5zZU1vZGVsID0ge1xyXG4gICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIG93bmVyTmFtZTogcGFyYW1zLmxpY2Vuc2Uub3duZXIubmFtZSxcclxuICAgICAgICAgICAgZW1haWw6IHBhcmFtcy5saWNlbnNlLmVtYWlsLFxyXG4gICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICBpZDogcGFyYW1zLmxpY2Vuc2UucHJvamVjdC5pZCxcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLnByb2plY3QubmFtZSxcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY2xpZW50SWQ6IHBhcmFtcy5saWNlbnNlLmNsaWVudC5pZCxcclxuICAgICAgICAgICAgY2xpZW50TmFtZTogcGFyYW1zLmxpY2Vuc2UuY2xpZW50Lm5hbWUsXHJcbiAgICAgICAgICAgIHN0YXR1czogcGFyYW1zLmxpY2Vuc2Uuc3RhdHVzLFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIG5hbWU6IHBhcmFtcy5saWNlbnNlLm1ldGhvZC5uYW1lLFxyXG4gICAgICAgICAgICAgICAgbWF4OiBwYXJhbXMubGljZW5zZS5tZXRob2QubWF4LFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbnZpcm9ubWVudDogcGFyYW1zLmxpY2Vuc2UuZW52aXJvbm1lbnQsXHJcbiAgICAgICAgICAgIHJlcXVlc3REYXRlOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0RGF0ZSxcclxuICAgICAgICAgICAgaW5pdERhdGU6IChwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSAhPT0gbnVsbCk/IGFuZ3VsYXIuY29weShwYXJhbXMubGljZW5zZS5hY3RpdmF0aW9uRGF0ZSkgOiAnJyxcclxuICAgICAgICAgICAgZW5kRGF0ZTogKHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlICE9PSBudWxsKT8gYW5ndWxhci5jb3B5KHBhcmFtcy5saWNlbnNlLmV4cGlyYXRpb25EYXRlKSA6ICcnLFxyXG4gICAgICAgICAgICBzcGVjaWFsSW5zdHJ1Y3Rpb25zOiBwYXJhbXMubGljZW5zZS5yZXF1ZXN0Tm90ZSxcclxuICAgICAgICAgICAgd2Vic2l0ZU5hbWU6IHBhcmFtcy5saWNlbnNlLndlYnNpdGVuYW1lLFxyXG5cclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogcGFyYW1zLmxpY2Vuc2UuYmFubmVyTWVzc2FnZSxcclxuICAgICAgICAgICAgcmVxdWVzdGVkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcXVlc3RlZElkLFxyXG4gICAgICAgICAgICByZXBsYWNlZDogcGFyYW1zLmxpY2Vuc2UucmVwbGFjZWQsXHJcbiAgICAgICAgICAgIHJlcGxhY2VkSWQ6IHBhcmFtcy5saWNlbnNlLnJlcGxhY2VkSWQsXHJcbiAgICAgICAgICAgIGhvc3ROYW1lOiBwYXJhbXMubGljZW5zZS5ob3N0TmFtZSxcclxuICAgICAgICAgICAgaGFzaDogcGFyYW1zLmxpY2Vuc2UuaWQsXHJcbiAgICAgICAgICAgIGdyYWNlUGVyaW9kRGF5czogcGFyYW1zLmxpY2Vuc2UuZ3JhY2VQZXJpb2REYXlzLFxyXG5cclxuICAgICAgICAgICAgYXBwbGllZDogcGFyYW1zLmxpY2Vuc2UuYXBwbGllZCxcclxuICAgICAgICAgICAga2V5SWQ6IHBhcmFtcy5saWNlbnNlLmtleUlkXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5saWNlbnNlS2V5ID0gJ0xpY2Vuc2VzIGhhcyBub3QgYmVlbiBpc3N1ZWQnO1xyXG5cclxuICAgICAgICAvLyBEZWZpbmVkIHRoZSBFbnZpcm9ubWVudCBTZWxlY3RcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50ID0ge307XHJcbiAgICAgICAgdGhpcy5zZWxlY3RFbnZpcm9ubWVudExpc3RPcHRpb25zID0gW107XHJcbiAgICAgICAgdGhpcy5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKTtcclxuXHJcbiAgICAgICAgLy8gRGVmaW5lZCB0aGUgU3RhdHVzIFNlbGVjdCBMaXN0XHJcbiAgICAgICAgdGhpcy5zZWxlY3RTdGF0dXMgPSBbXTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCB0aGUgdHdvIEtlbmRvIERhdGVzIGZvciBJbml0IGFuZCBFbmREYXRlXHJcbiAgICAgICAgdGhpcy5pbml0RGF0ZSA9IHt9O1xyXG4gICAgICAgIHRoaXMuaW5pdERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6IHRoaXMudXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlRm9ybWF0VG9LZW5kb0RhdGUoKSxcclxuICAgICAgICAgICAgb3BlbjogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICAgICAgfSksXHJcbiAgICAgICAgICAgIGNoYW5nZTogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICAgICAgfSlcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmVuZERhdGUgPSB7fTtcclxuICAgICAgICB0aGlzLmVuZERhdGVPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBmb3JtYXQ6IHRoaXMudXNlclByZWZlcmVuY2VzU2VydmljZS5nZXRDb252ZXJ0ZWREYXRlRm9ybWF0VG9LZW5kb0RhdGUoKSxcclxuICAgICAgICAgICAgb3BlbjogKChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLm9uQ2hhbmdlRW5kRGF0ZSgpO1xyXG4gICAgICAgICAgICB9KSxcclxuICAgICAgICAgICAgY2hhbmdlOiAoKGUpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25DaGFuZ2VFbmREYXRlKCk7XHJcbiAgICAgICAgICAgIH0pXHJcbiAgICAgICAgfTtcclxuXHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZU1ldGhvZE9wdGlvbnMoKTtcclxuICAgICAgICB0aGlzLnByZXBhcmVMaWNlbnNlS2V5KCk7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQWN0aXZpdHlMaXN0KCk7XHJcblxyXG4gICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcblxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ29udHJvbHMgd2hhdCBidXR0b25zIHRvIHNob3dcclxuICAgICAqL1xyXG4gICAgcHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCkge1xyXG4gICAgICAgIHRoaXMucGVuZGluZ0xpY2Vuc2UgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXMgPT09ICdQRU5ESU5HJyAmJiAhdGhpcy5lZGl0TW9kZTtcclxuICAgICAgICB0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgPSAodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnRVhQSVJFRCcgfHwgdGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnVEVSTUlOQVRFRCcpO1xyXG4gICAgICAgIHRoaXMuYWN0aXZlU2hvd01vZGUgPSB0aGlzLmxpY2Vuc2VNb2RlbC5zdGF0dXMgPT09ICdBQ1RJVkUnICYmICF0aGlzLmV4cGlyZWRPclRlcm1pbmF0ZWQgJiYgIXRoaXMuZWRpdE1vZGU7XHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZU1ldGhvZE9wdGlvbnMoKSB7XHJcbiAgICAgICAgdGhpcy5tZXRob2RPcHRpb25zID0gW1xyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnTUFYX1NFUlZFUlMnLFxyXG4gICAgICAgICAgICAgICAgdGV4dDogJ1NlcnZlcnMnLFxyXG4gICAgICAgICAgICAgICAgbWF4OiAwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHtcclxuICAgICAgICAgICAgICAgIG5hbWU6ICdUT0tFTicsXHJcbiAgICAgICAgICAgICAgICB0ZXh0OiAnVG9rZW5zJyxcclxuICAgICAgICAgICAgICAgIG1heDogMFxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBuYW1lOiAnQ1VTVE9NJyxcclxuICAgICAgICAgICAgICAgIHRleHQ6ICdDdXN0b20nXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdXHJcbiAgICB9XHJcblxyXG4gICAgcHJlcGFyZUxpY2Vuc2VLZXkoKSB7XHJcbiAgICAgICAgaWYodGhpcy5saWNlbnNlTW9kZWwuc3RhdHVzID09PSAnQUNUSVZFJykge1xyXG4gICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRLZXlDb2RlKHRoaXMubGljZW5zZU1vZGVsLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZUtleSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICAgICAgd2luZG93LlREU1RNLnNhZmVBcHBseSh0aGlzLnNjb3BlKTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIHByZXBhcmVBY3Rpdml0eUxpc3QoKSB7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5hY3Rpdml0eUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1LFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDIwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2RhdGVDcmVhdGVkJywgdGl0bGU6ICdEYXRlJywgd2lkdGg6MTYwLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eSBoOm1tOnNzIHR0fScsIHRlbXBsYXRlOiAne3sgZGF0YUl0ZW0uZGF0ZUNyZWF0ZWQgfCBjb252ZXJ0RGF0ZVRpbWVJbnRvVGltZVpvbmUgfX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhdXRob3IucGVyc29uTmFtZScsIHRpdGxlOiAnV2hvbScsICB3aWR0aDoxNjB9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2hhbmdlcycsIHRpdGxlOiAnQWN0aW9uJywgdGVtcGxhdGU6ICc8dGFibGUgY2xhc3M9XCJpbm5lci1hY3Rpdml0eV90YWJsZVwiPjx0Ym9keT48dHI+PHRkPjwvdGQ+PHRkIGNsYXNzPVwiY29sLWFjdGlvbl90ZFwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1taW51c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L3RkPjx0ZCBjbGFzcz1cImNvbC1hY3Rpb25fdGRcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj48L3RkPjwvdHI+I2Zvcih2YXIgaSA9IDA7IGkgPCBkYXRhLmNoYW5nZXMubGVuZ3RoOyBpKyspeyM8dHI+PHRkIHN0eWxlPVwiZm9udC13ZWlnaHQ6IGJvbGQ7XCI+Iz1kYXRhLmNoYW5nZXNbaV0uZmllbGQjIDwvdGQ+PHRkIGNsYXNzPVwiY29sLXZhbHVlX3RkXCI+PHNwYW4gY2xhc3M9XCJhY3Rpdml0eS1saXN0LW9sZC12YWxcIiBzdHlsZT1cImNvbG9yOmRhcmtyZWQ7IGZvbnQtd2VpZ2h0OiBib2xkO1wiPnt7IFxcJyM9ZGF0YS5jaGFuZ2VzW2ldLm9sZFZhbHVlI1xcJyB8IGNvbnZlcnREYXRlSW50b1RpbWVab25lIH19PC9zcGFuPjwvdGQ+PHRkIGNsYXNzPVwiY29sLXZhbHVlX3RkXCI+PHNwYW4gY2xhc3M9XCJhY3Rpdml0eS1saXN0LW5ldy12YWxcIiBzdHlsZT1cImNvbG9yOiBncmVlbjsgZm9udC13ZWlnaHQ6IGJvbGQ7XCI+e3sgXFwnIz1kYXRhLmNoYW5nZXNbaV0ubmV3VmFsdWUjXFwnIHwgY29udmVydERhdGVJbnRvVGltZVpvbmUgfX08L3RkPjwvdHI+I30jPC90Ym9keT48L3RhYmxlPid9LFxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRBY3Rpdml0eUxvZyh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAnZGF0ZUNyZWF0ZWQnLFxyXG4gICAgICAgICAgICAgICAgICAgIGRpcjogJ2FzYydcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc2Nyb2xsYWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBJZiBieSBzb21lIHJlYXNvbiB0aGUgTGljZW5zZSB3YXMgbm90IGFwcGxpZWQgYXQgZmlyc3QgdGltZSwgdGhpcyB3aWxsIGRvIGEgcmVxdWVzdCBmb3IgaXRcclxuICAgICAqL1xyXG4gICAgYWN0aXZhdGVMaWNlbnNlKCkge1xyXG4gICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmFjdGl2YXRlTGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYgKGRhdGEpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1vZGVsLnN0YXR1cyA9ICdBQ1RJVkUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5zYXZlRm9ybSh0aGlzLmxpY2Vuc2VNb2RlbCk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5wcmVwYXJlTGljZW5zZUtleSgpO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZWxvYWRSZXF1aXJlZCA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmV2b2tlTGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byByZXZva2UgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnJldm9rZUxpY2Vuc2UodGhpcy5saWNlbnNlTW9kZWwsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UoZGF0YSk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmFsaWRhdGUgdGhlIGlucHV0IG9uIFNlcnZlciBvciBUb2tlbnMgaXMgb25seSBpbnRlZ2VyIG9ubHlcclxuICAgICAqIFRoaXMgd2lsbCBiZSBjb252ZXJ0ZWQgaW4gYSBtb3JlIGNvbXBsZXggZGlyZWN0aXZlIGxhdGVyXHJcbiAgICAgKiBUT0RPOiBDb252ZXJ0IGludG8gYSBkaXJlY3RpdmVcclxuICAgICAqL1xyXG4gICAgdmFsaWRhdGVJbnRlZ2VyT25seShlLG1vZGVsKXtcclxuICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICB2YXIgbmV3VmFsPSBwYXJzZUludChtb2RlbCk7XHJcbiAgICAgICAgICAgIGlmKCFpc05hTihuZXdWYWwpKSB7XHJcbiAgICAgICAgICAgICAgICBtb2RlbCA9IG5ld1ZhbDtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIG1vZGVsID0gMDtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICBpZihlICYmIGUuY3VycmVudFRhcmdldCkge1xyXG4gICAgICAgICAgICAgICAgZS5jdXJyZW50VGFyZ2V0LnZhbHVlID0gbW9kZWw7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9IGNhdGNoKGUpIHtcclxuICAgICAgICAgICAgdGhpcy4kbG9nLndhcm4oJ0ludmFsaWQgTnVtYmVyIEV4Y2VwdGlvbicsIG1vZGVsKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBTYXZlIGN1cnJlbnQgY2hhbmdlc1xyXG4gICAgICovXHJcbiAgICBzYXZlTGljZW5zZSgpIHtcclxuICAgICAgICBpZih0aGlzLmlzRGlydHkoKSkge1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcbiAgICAgICAgICAgIHRoaXMucHJlcGFyZUNvbnRyb2xBY3Rpb25CdXR0b25zKCk7XHJcbiAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLnNhdmVMaWNlbnNlKHRoaXMubGljZW5zZU1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZWxvYWRSZXF1aXJlZCA9IHRydWU7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVsb2FkTGljZW5zZU1hbmFnZXJMaXN0KCk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLmxvZy5pbmZvKCdMaWNlbnNlIFNhdmVkJyk7XHJcbiAgICAgICAgICAgIH0pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGUgPSBmYWxzZTtcclxuICAgICAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENoYW5nZSB0aGUgc3RhdHVzIHRvIEVkaXRcclxuICAgICAqL1xyXG4gICAgbW9kaWZ5TGljZW5zZSgpIHtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gdHJ1ZTtcclxuICAgICAgICB0aGlzLnByZXBhcmVDb250cm9sQWN0aW9uQnV0dG9ucygpO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogUG9wdWxhdGUgdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnNlbGVjdEVudmlyb25tZW50TGlzdE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMubGljZW5zZU1hbmFnZXJTZXJ2aWNlLmdldEVudmlyb25tZW50RGF0YVNvdXJjZSgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgaWYoIXRoaXMubGljZW5zZU1vZGVsLmVudmlyb25tZW50KSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5saWNlbnNlTW9kZWwuZW52aXJvbm1lbnQgPSBkYXRhWzBdO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgfVxyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMuc2F2ZUZvcm0odGhpcy5saWNlbnNlTW9kZWwpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSlcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHZhbHVlVGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhKT8gZGF0YS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPicsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YSk/IGRhdGEudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nLFxyXG4gICAgICAgICAgICB2YWx1ZVByaW1pdGl2ZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgb25DaGFuZ2VJbml0RGF0ZSgpIHtcclxuICAgICAgICB2YXIgc3RhcnREYXRlID0gdGhpcy5pbml0RGF0ZS52YWx1ZSgpLFxyXG4gICAgICAgICAgICBlbmREYXRlID0gdGhpcy5lbmREYXRlLnZhbHVlKCk7XHJcblxyXG4gICAgICAgIGlmIChzdGFydERhdGUpIHtcclxuICAgICAgICAgICAgc3RhcnREYXRlID0gbmV3IERhdGUoc3RhcnREYXRlKTtcclxuICAgICAgICAgICAgc3RhcnREYXRlLnNldERhdGUoc3RhcnREYXRlLmdldERhdGUoKSk7XHJcbiAgICAgICAgICAgIHRoaXMuZW5kRGF0ZS5taW4oc3RhcnREYXRlKTtcclxuXHJcbiAgICAgICAgICAgIGlmKGVuZERhdGUpIHtcclxuICAgICAgICAgICAgICAgIGlmKHRoaXMuaW5pdERhdGUudmFsdWUoKSA+IHRoaXMuZW5kRGF0ZS52YWx1ZSgpKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgZW5kRGF0ZSA9IG5ldyBEYXRlKGVuZERhdGUpO1xyXG4gICAgICAgICAgICAgICAgICAgIGVuZERhdGUuc2V0RGF0ZShzdGFydERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbC5lbmREYXRlID0gZW5kRGF0ZTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBvbkNoYW5nZUVuZERhdGUoKXtcclxuICAgICAgICB2YXIgZW5kRGF0ZSA9IHRoaXMuZW5kRGF0ZS52YWx1ZSgpLFxyXG4gICAgICAgICAgICBzdGFydERhdGUgPSB0aGlzLmluaXREYXRlLnZhbHVlKCk7XHJcblxyXG4gICAgICAgIGlmIChlbmREYXRlKSB7XHJcbiAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZShlbmREYXRlKTtcclxuICAgICAgICAgICAgZW5kRGF0ZS5zZXREYXRlKGVuZERhdGUuZ2V0RGF0ZSgpKTtcclxuICAgICAgICB9IGVsc2UgaWYgKHN0YXJ0RGF0ZSkge1xyXG4gICAgICAgICAgICB0aGlzLmVuZERhdGUubWluKG5ldyBEYXRlKHN0YXJ0RGF0ZSkpO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIGVuZERhdGUgPSBuZXcgRGF0ZSgpO1xyXG4gICAgICAgICAgICB0aGlzLmluaXREYXRlLm1heChlbmREYXRlKTtcclxuICAgICAgICAgICAgdGhpcy5lbmREYXRlLm1pbihlbmREYXRlKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgaWYodGhpcy5lZGl0TW9kZSkge1xyXG4gICAgICAgICAgICB0aGlzLnJlc2V0Rm9ybSgoKT0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMub25SZXNldEZvcm0oKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIGlmKHRoaXMucmVsb2FkUmVxdWlyZWQpe1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2Uoe30pO1xyXG4gICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5kaXNtaXNzKCdjYW5jZWwnKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEZXBlbmRpbmcgdGhlIG51bWJlciBvZiBmaWVsZHMgYW5kIHR5cGUgb2YgZmllbGQsIHRoZSByZXNldCBjYW4ndCBiZSBvbiB0aGUgRm9ybVZhbGlkb3IsIGF0IGxlYXN0IG5vdCBub3dcclxuICAgICAqL1xyXG4gICAgb25SZXNldEZvcm0oKSB7XHJcbiAgICAgICAgdGhpcy5yZXNldERyb3BEb3duKHRoaXMuc2VsZWN0RW52aXJvbm1lbnQsIHRoaXMubGljZW5zZU1vZGVsLmVudmlyb25tZW50KTtcclxuICAgICAgICB0aGlzLm9uQ2hhbmdlSW5pdERhdGUoKTtcclxuICAgICAgICB0aGlzLm9uQ2hhbmdlRW5kRGF0ZSgpO1xyXG5cclxuICAgICAgICB0aGlzLmVkaXRNb2RlID0gZmFsc2U7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlQ29udHJvbEFjdGlvbkJ1dHRvbnMoKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE1hbnVhbCByZWxvYWQgYWZ0ZXIgYSBjaGFuZ2UgaGFzIGJlZW4gcGVyZm9ybWVkIHRvIHRoZSBMaWNlbnNlXHJcbiAgICAgKi9cclxuICAgIHJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLmFjdGl2aXR5R3JpZC5kYXRhU291cmNlKSB7XHJcbiAgICAgICAgICAgIHRoaXMuYWN0aXZpdHlHcmlkLmRhdGFTb3VyY2UucmVhZCgpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDA5LzI1LzIwMTYuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBMaWNlbnNlTWFuYWdlckxpc3Qge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csICRzdGF0ZSwgbGljZW5zZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5zdGF0ZSA9ICRzdGF0ZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7fTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICAvL3RoaXMuZ2V0TGljZW5zZUxpc3QoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZU1hbmFnZXJMaXN0IEluc3RhbmNlZCcpO1xyXG4gICAgICAgIHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZCA9IDA7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIGdldERhdGFTb3VyY2UoKSB7XHJcbiAgICAgICAgdGhpcy5saWNlbnNlR3JpZE9wdGlvbnMgPSB7XHJcbiAgICAgICAgICAgIHRvb2xiYXI6IGtlbmRvLnRlbXBsYXRlKCc8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdCBhY3Rpb24tdG9vbGJhci1idG5cIiBuZy1jbGljaz1cImxpY2Vuc2VNYW5hZ2VyTGlzdC5vblJlcXVlc3RJbXBvcnRMaWNlbnNlKClcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGx1c1wiIGFyaWEtaGlkZGVuPVwidHJ1ZVwiPjwvc3Bhbj4gSW1wb3J0IExpY2Vuc2UgUmVxdWVzdDwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0LnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpXCIgY2xhc3M9XCJhY3Rpb24tdG9vbGJhci1yZWZyZXNoLWJ0blwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1yZWZyZXNoXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPjwvZGl2PicpLFxyXG4gICAgICAgICAgICBwYWdlYWJsZToge1xyXG4gICAgICAgICAgICAgICAgcmVmcmVzaDogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplczogdHJ1ZSxcclxuICAgICAgICAgICAgICAgIGJ1dHRvbkNvdW50OiA1LFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDIwXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGNvbHVtbnM6IFtcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2lkJywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCBmaWx0ZXJhYmxlOiBmYWxzZSwgdGl0bGU6ICdBY3Rpb24nLCB3aWR0aDogODAsIHRlbXBsYXRlOiAnPGJ1dHRvbiBjbGFzcz1cImJ0biBidG4tZGVmYXVsdFwiIG5nLWNsaWNrPVwibGljZW5zZU1hbmFnZXJMaXN0Lm9uTGljZW5zZU1hbmFnZXJEZXRhaWxzKHRoaXMpXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXBlbmNpbFwiPjwvc3Bhbj48L2J1dHRvbj4nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdvd25lci5uYW1lJywgdGl0bGU6ICdPd25lcid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnY2xpZW50Lm5hbWUnLCB0aXRsZTogJ0NsaWVudCd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAncHJvamVjdC5uYW1lJywgdGl0bGU6ICdQcm9qZWN0JywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLnByb2plY3QgJiYgZGF0YS5wcm9qZWN0Lm5hbWUpPyBkYXRhLnByb2plY3QubmFtZS50b0xvd2VyQ2FzZSgpOiBcIlwiICkjPC9zcGFuPid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnZW1haWwnLCB0aXRsZTogJ0NvbnRhY3QgRW1haWwnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3N0YXR1cycsIHRpdGxlOiAnU3RhdHVzJywgdGVtcGxhdGU6ICc8c3BhbiBzdHlsZT1cInRleHQtdHJhbnNmb3JtOiBjYXBpdGFsaXplO1wiPiM9KChkYXRhLnN0YXR1cyk/IGRhdGEuc3RhdHVzLnRvTG93ZXJDYXNlKCk6IFwiXCIgKSM8L3NwYW4+J30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnLCAgdGVtcGxhdGU6ICcjaWYoZGF0YS50eXBlICYmIGRhdGEudHlwZS5uYW1lID09PSBcIk1VTFRJX1BST0pFQ1RcIil7IyBHbG9iYWwgIyB9IGVsc2UgeyMgU2luZ2xlICN9Iyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnbWV0aG9kLm5hbWUnLCB0aXRsZTogJ01ldGhvZCcsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5tZXRob2QgJiYgZGF0YS5tZXRob2QubmFtZSk/IGRhdGEubWV0aG9kLm5hbWUudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ21ldGhvZC5tYXgnLCB0aXRsZTogJ1NlcnZlci9Ub2tlbnMnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2YXRpb25EYXRlJywgdGl0bGU6ICdJbmNlcHRpb24nLCB0eXBlOiAnZGF0ZScsIGZvcm1hdCA6ICd7MDpkZC9NTU0veXl5eX0nLCB0ZW1wbGF0ZTogJ3t7IGRhdGFJdGVtLmFjdGl2YXRpb25EYXRlIHwgY29udmVydERhdGVJbnRvVGltZVpvbmUgfX0nIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdleHBpcmF0aW9uRGF0ZScsIHRpdGxlOiAnRXhwaXJhdGlvbicsIHR5cGU6ICdkYXRlJywgZm9ybWF0IDogJ3swOmRkL01NTS95eXl5fScsIHRlbXBsYXRlOiAne3sgZGF0YUl0ZW0uZXhwaXJhdGlvbkRhdGUgfCBjb252ZXJ0RGF0ZUludG9UaW1lWm9uZSB9fScgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Vudmlyb25tZW50JywgdGl0bGU6ICdFbnZpcm9ubWVudCcsIHRlbXBsYXRlOiAnPHNwYW4gc3R5bGU9XCJ0ZXh0LXRyYW5zZm9ybTogY2FwaXRhbGl6ZTtcIj4jPSgoZGF0YS5lbnZpcm9ubWVudCk/IGRhdGEuZW52aXJvbm1lbnQudG9Mb3dlckNhc2UoKTogXCJcIiApIzwvc3Bhbj4nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDonZ3JhY2VQZXJpb2REYXlzJywgaGlkZGVuOiB0cnVlfVxyXG4gICAgICAgICAgICBdLFxyXG4gICAgICAgICAgICBkYXRhU291cmNlOiB7XHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZTogMTAsXHJcbiAgICAgICAgICAgICAgICB0cmFuc3BvcnQ6IHtcclxuICAgICAgICAgICAgICAgICAgICByZWFkOiAoZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZS5nZXRMaWNlbnNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZS5zdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgc29ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIGZpZWxkOiAncHJvamVjdC5uYW1lJyxcclxuICAgICAgICAgICAgICAgICAgICBkaXI6ICdhc2MnXHJcbiAgICAgICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAgICAgY2hhbmdlOiAgKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAvLyBXZSBhcmUgY29taW5nIGZyb20gYSBuZXcgaW1wb3J0ZWQgcmVxdWVzdCBsaWNlbnNlXHJcbiAgICAgICAgICAgICAgICAgICAgaWYodGhpcy5vcGVuTGFzdEltcG9ydGVkTGljZW5zZUlkICE9PSAwICYmIHRoaXMubGljZW5zZUdyaWQuZGF0YVNvdXJjZS5fZGF0YSkge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB2YXIgbmV3TGljZW5zZUNyZWF0ZWQgPSB0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UuX2RhdGEuZmluZCgobGljZW5zZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgcmV0dXJuIGxpY2Vuc2UuaWQgPT09IHRoaXMub3Blbkxhc3RJbXBvcnRlZExpY2Vuc2VJZDtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7XHJcblxyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSAwO1xyXG5cclxuICAgICAgICAgICAgICAgICAgICAgICAgaWYobmV3TGljZW5zZUNyZWF0ZWQpIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMub25MaWNlbnNlTWFuYWdlckRldGFpbHMobmV3TGljZW5zZUNyZWF0ZWQpO1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgZmlsdGVyYWJsZToge1xyXG4gICAgICAgICAgICAgICAgZXh0cmE6IGZhbHNlXHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVGhlIHVzZXIgSW1wb3J0IGEgbmV3IExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgb25SZXF1ZXN0SW1wb3J0TGljZW5zZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9saWNlbnNlTWFuYWdlci9yZXF1ZXN0SW1wb3J0L1JlcXVlc3RJbXBvcnQuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdSZXF1ZXN0SW1wb3J0IGFzIHJlcXVlc3RJbXBvcnQnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnXHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKGxpY2Vuc2VJbXBvcnRlZCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLm9wZW5MYXN0SW1wb3J0ZWRMaWNlbnNlSWQgPSBsaWNlbnNlSW1wb3J0ZWQuaWQ7IC8vIHRha2UgdGhpcyBwYXJhbSBmcm9tIHRoZSBsYXN0IGltcG9ydGVkIGxpY2Vuc2UsIG9mIGNvdXJzZVxyXG4gICAgICAgICAgICB0aGlzLnJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQWZ0ZXIgY2xpY2tpbmcgb24gZWRpdCwgd2UgcmVkaXJlY3QgdGhlIHVzZXIgdG8gdGhlIEVkaXRpb24gc2NyZWVuIGluc3RlYWQgb2Ygb3BlbiBhIGRpYWxvZ1xyXG4gICAgICogZHUgdGhlIHNpemUgb2YgdGhlIGlucHV0c1xyXG4gICAgICovXHJcbiAgICBvbkxpY2Vuc2VNYW5hZ2VyRGV0YWlscyhsaWNlbnNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cuaW5mbygnT3BlbiBEZXRhaWxzIGZvcjogJywgbGljZW5zZSk7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbGljZW5zZU1hbmFnZXIvZGV0YWlsL0xpY2Vuc2VNYW5hZ2VyRGV0YWlsLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnTGljZW5zZU1hbmFnZXJEZXRhaWwgYXMgbGljZW5zZU1hbmFnZXJEZXRhaWwnLFxyXG4gICAgICAgICAgICBzaXplOiAnbGcnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6IGZ1bmN0aW9uICgpIHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSB7fTtcclxuICAgICAgICAgICAgICAgICAgICBpZihsaWNlbnNlICYmIGxpY2Vuc2UuZGF0YUl0ZW0pIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZGF0YUl0ZW0gPSBsaWNlbnNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIGRhdGFJdGVtID0gbGljZW5zZTtcclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIHsgbGljZW5zZTogZGF0YUl0ZW0gfTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKCgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5yZWxvYWRMaWNlbnNlTWFuYWdlckxpc3QoKTtcclxuICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubG9nLmluZm8oJ1JlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG5cclxuICAgIHJlbG9hZExpY2Vuc2VNYW5hZ2VyTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLmxpY2Vuc2VHcmlkLmRhdGFTb3VyY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlR3JpZC5kYXRhU291cmNlLnJlYWQoKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjgvMjAxNi5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgRm9ybVZhbGlkYXRvciBmcm9tICcuLi8uLi91dGlscy9mb3JtL0Zvcm1WYWxpZGF0b3IuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUmVxdWVzdEltcG9ydCBleHRlbmRzIEZvcm1WYWxpZGF0b3J7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCBsaWNlbnNlTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpIHtcclxuICAgICAgICBzdXBlcigkbG9nLCAkc2NvcGUsICR1aWJNb2RhbCwgJHVpYk1vZGFsSW5zdGFuY2UpO1xyXG5cclxuICAgICAgICB0aGlzLmxpY2Vuc2VNYW5hZ2VyU2VydmljZSA9IGxpY2Vuc2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxpY2Vuc2VNb2RlbCA9IHtcclxuICAgICAgICAgICAgaGFzaDogJydcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnNhdmVGb3JtKHRoaXMubGljZW5zZU1vZGVsKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgYW5kIHZhbGlkYXRlIHRoZSBLZXkgaXMgY29ycmVjdFxyXG4gICAgICovXHJcbiAgICBvbkltcG9ydExpY2Vuc2UoKSB7XHJcbiAgICAgICAgaWYodGhpcy5pc0RpcnR5KCkpIHtcclxuICAgICAgICAgICAgdGhpcy5saWNlbnNlTWFuYWdlclNlcnZpY2UuaW1wb3J0TGljZW5zZSh0aGlzLmxpY2Vuc2VNb2RlbCwgKGxpY2Vuc2VJbXBvcnRlZCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGxpY2Vuc2VJbXBvcnRlZC5kYXRhKTtcclxuICAgICAgICAgICAgfSwgKGxpY2Vuc2VJbXBvcnRlZCk9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuY2xvc2UobGljZW5zZUltcG9ydGVkLmRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBEaXNtaXNzIHRoZSBkaWFsb2csIG5vIGFjdGlvbiBuZWNlc3NhcnlcclxuICAgICAqL1xyXG4gICAgY2FuY2VsQ2xvc2VEaWFsb2coKSB7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmRpc21pc3MoJ2NhbmNlbCcpO1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMDkvMjYvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTGljZW5zZU1hbmFnZXJTZXJ2aWNlIHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCByZXN0U2VydmljZUhhbmRsZXIsICRyb290U2NvcGUpIHtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZSA9IHJlc3RTZXJ2aWNlSGFuZGxlcjtcclxuICAgICAgICB0aGlzLnJvb3RTY29wZSA9ICRyb290U2NvcGU7XHJcbiAgICAgICAgdGhpcy5zdGF0dXNTdWNjZXNzID0gJ3N1Y2Nlc3MnO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdsaWNlbnNlTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0TGljZW5zZUxpc3Qob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0TGljZW5zZUxpc3QoKGRhdGEpID0+IHtcclxuXHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcblxyXG4gICAgZ2V0UHJvamVjdERhdGFTb3VyY2Uob25TdWNjZXNzKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5saWNlbnNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0UHJvamVjdERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEVudmlyb25tZW50RGF0YVNvdXJjZShvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRFbnZpcm9ubWVudERhdGFTb3VyY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldEtleUNvZGUobGljZW5zZUlkLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRLZXlDb2RlKGxpY2Vuc2VJZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhLmRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGdldFRpbWVab25lQ29uZmlndXJhdGlvbihvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmNvbW1vblNlcnZpY2VIYW5kbGVyKCkuZ2V0VGltZVpvbmVDb25maWd1cmF0aW9uKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBvblN1Y2Nlc3MoZGF0YS5kYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFNhdmUgdGhlIExpY2Vuc2VcclxuICAgICAqL1xyXG4gICAgc2F2ZUxpY2Vuc2UobGljZW5zZSwgb25TdWNjZXNzKSB7XHJcblxyXG4gICAgICAgIHZhciBsaWNlbnNlTW9kaWZpZWQgPSB7XHJcbiAgICAgICAgICAgIGVudmlyb25tZW50OiBsaWNlbnNlLmVudmlyb25tZW50LFxyXG4gICAgICAgICAgICBtZXRob2Q6IHtcclxuICAgICAgICAgICAgICAgIG5hbWU6IGxpY2Vuc2UubWV0aG9kLm5hbWVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYWN0aXZhdGlvbkRhdGU6IG1vbWVudChsaWNlbnNlLmluaXREYXRlKS5mb3JtYXQoJ1lZWVktTU0tREQnKSxcclxuICAgICAgICAgICAgZXhwaXJhdGlvbkRhdGU6IG1vbWVudChsaWNlbnNlLmVuZERhdGUpLmZvcm1hdCgnWVlZWS1NTS1ERCcpLFxyXG4gICAgICAgICAgICBzdGF0dXM6IGxpY2Vuc2Uuc3RhdHVzLFxyXG4gICAgICAgICAgICBwcm9qZWN0OiB7XHJcbiAgICAgICAgICAgICAgICBpZDogKGxpY2Vuc2UucHJvamVjdC5pZCAhPT0gJ2FsbCcpPyBwYXJzZUludChsaWNlbnNlLnByb2plY3QuaWQpIDogbGljZW5zZS5wcm9qZWN0LmlkLCAgLy8gV2UgcGFzcyAnYWxsJyB3aGVuIGlzIG11bHRpcHJvamVjdFxyXG4gICAgICAgICAgICAgICAgbmFtZTogbGljZW5zZS5wcm9qZWN0Lm5hbWVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYmFubmVyTWVzc2FnZTogbGljZW5zZS5iYW5uZXJNZXNzYWdlLFxyXG4gICAgICAgICAgICBncmFjZVBlcmlvZERheXM6IGxpY2Vuc2UuZ3JhY2VQZXJpb2REYXlzLFxyXG4gICAgICAgICAgICB3ZWJzaXRlbmFtZTogbGljZW5zZS53ZWJzaXRlTmFtZSxcclxuICAgICAgICAgICAgaG9zdE5hbWU6IGxpY2Vuc2UuaG9zdE5hbWVcclxuICAgICAgICB9O1xyXG4gICAgICAgIGlmKGxpY2Vuc2UubWV0aG9kLm5hbWUgIT09ICdDVVNUT00nKSB7XHJcbiAgICAgICAgICAgIGxpY2Vuc2VNb2RpZmllZC5tZXRob2QubWF4ID0gcGFyc2VJbnQobGljZW5zZS5tZXRob2QubWF4KTtcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnNhdmVMaWNlbnNlKGxpY2Vuc2UuaWQsIGxpY2Vuc2VNb2RpZmllZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuICAgIC8qKlxyXG4gICAgICogRG9lcyB0aGUgYWN0aXZhdGlvbiBvZiB0aGUgY3VycmVudCBsaWNlbnNlIGlmIHRoaXMgaXMgbm90IGFjdGl2ZVxyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBhY3RpdmF0ZUxpY2Vuc2UobGljZW5zZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5hY3RpdmF0ZUxpY2Vuc2UobGljZW5zZS5pZCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgaWYoZGF0YS5zdGF0dXMgPT09IHRoaXMuc3RhdHVzU3VjY2Vzcykge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yb290U2NvcGUuJGVtaXQoJ2Jyb2FkY2FzdC1tc2cnLCB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ2luZm8nLFxyXG4gICAgICAgICAgICAgICAgICAgIHRleHQ6ICdUaGUgbGljZW5zZSB3YXMgYWN0aXZhdGVkIGFuZCB0aGUgbGljZW5zZSB3YXMgZW1haWxlZC4nXHJcbiAgICAgICAgICAgICAgICB9KTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywge1xyXG4gICAgICAgICAgICAgICAgICAgIHR5cGU6ICd3YXJuaW5nJyxcclxuICAgICAgICAgICAgICAgICAgICB0ZXh0OiBkYXRhLmRhdGFcclxuICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKCk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE1ha2UgdGhlIHJlcXVlc3QgdG8gSW1wb3J0IHRoZSBsaWNlbnNlLCBpZiBmYWlscywgdGhyb3dzIGFuIGV4Y2VwdGlvbiB2aXNpYmxlIGZvciB0aGUgdXNlciB0byB0YWtlIGFjdGlvblxyXG4gICAgICogQHBhcmFtIGxpY2Vuc2VcclxuICAgICAqIEBwYXJhbSBjYWxsYmFja1xyXG4gICAgICovXHJcbiAgICBpbXBvcnRMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcywgb25FcnJvcikge1xyXG4gICAgICAgIHZhciBoYXNoID0ge1xyXG4gICAgICAgICAgICBkYXRhOiBsaWNlbnNlLmhhc2hcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5yZXF1ZXN0SW1wb3J0KGhhc2gsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIGlmKGRhdGEuc3RhdHVzID09PSB0aGlzLnN0YXR1c1N1Y2Nlc3MpIHtcclxuICAgICAgICAgICAgICAgIHRoaXMucm9vdFNjb3BlLiRlbWl0KCdicm9hZGNhc3QtbXNnJywgeyB0eXBlOiAnaW5mbycsIHRleHQ6ICdMaWNlbnNlIHdhcyBzdWNjZXNzZnVsbHkgSW1wb3J0ZWQnfSk7XHJcbiAgICAgICAgICAgIH0gZWxzZSB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJvb3RTY29wZS4kZW1pdCgnYnJvYWRjYXN0LW1zZycsIHsgdHlwZTogJ3dhcm5pbmcnLCB0ZXh0OiAnTGljZW5zZSB3YXMgbm90IGFwcGxpZWQuIFJldmlldyB0aGUgcHJvdmlkZWQgTGljZW5zZSBLZXkgaXMgY29ycmVjdC4nfSk7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gb25FcnJvcih7IHN1Y2Nlc3M6IGZhbHNlfSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICByZXZva2VMaWNlbnNlKGxpY2Vuc2UsIG9uU3VjY2Vzcykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLnJldm9rZUxpY2Vuc2UobGljZW5zZSwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uU3VjY2VzcyhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICBnZXRBY3Rpdml0eUxvZyhsaWNlbnNlLCBvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKS5nZXRBY3Rpdml0eUxvZyhsaWNlbnNlLmlkLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKGRhdGEpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IExpY2Vuc2UgcGFzc2luZyBwYXJhbXNcclxuICAgICAqIEBwYXJhbSBuZXdMaWNlbnNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgY3JlYXRlTmV3TGljZW5zZVJlcXVlc3QobmV3TGljZW5zZSwgY2FsbGJhY2spe1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2UubGljZW5zZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5ld0xpY2Vuc2VSZXF1ZXN0KG5ld0xpY2Vuc2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBhbmd1bGFyICBmcm9tICdhbmd1bGFyJztcclxuaW1wb3J0IHVpUm91dGVyIGZyb20gJ3VpLXJvdXRlcic7XHJcblxyXG5pbXBvcnQgTm90aWNlTGlzdCBmcm9tICcuL2xpc3QvTm90aWNlTGlzdC5qcyc7XHJcbmltcG9ydCBOb3RpY2VNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvTm90aWNlTWFuYWdlclNlcnZpY2UuanMnO1xyXG5pbXBvcnQgRWRpdE5vdGljZSBmcm9tICcuL2VkaXQvRWRpdE5vdGljZS5qcyc7XHJcblxyXG52YXIgTm90aWNlTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5Ob3RpY2VNYW5hZ2VyTW9kdWxlJywgW3VpUm91dGVyXSkuY29uZmlnKFsnJHN0YXRlUHJvdmlkZXInLCAgJyR0cmFuc2xhdGVQYXJ0aWFsTG9hZGVyUHJvdmlkZXInLFxyXG4gICAgZnVuY3Rpb24gKCRzdGF0ZVByb3ZpZGVyLCAkdHJhbnNsYXRlUGFydGlhbExvYWRlclByb3ZpZGVyKSB7XHJcblxyXG4gICAgJHRyYW5zbGF0ZVBhcnRpYWxMb2FkZXJQcm92aWRlci5hZGRQYXJ0KCdub3RpY2VNYW5hZ2VyJyk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCdub3RpY2VMaXN0Jywge1xyXG4gICAgICAgICAgICBkYXRhOiB7cGFnZToge3RpdGxlOiAnTm90aWNlIEFkbWluaXN0cmF0aW9uJywgaW5zdHJ1Y3Rpb246ICcnLCBtZW51OiBbJ0FkbWluJywgJ05vdGljZScsICdMaXN0J119fSxcclxuICAgICAgICAgICAgdXJsOiAnL25vdGljZS9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9ub3RpY2VNYW5hZ2VyL2xpc3QvTm90aWNlTGlzdC5odG1sJyxcclxuICAgICAgICAgICAgICAgICAgICBjb250cm9sbGVyOiAnTm90aWNlTGlzdCBhcyBub3RpY2VMaXN0J1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcbn1dKTtcclxuXHJcbi8vIFNlcnZpY2VzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuc2VydmljZSgnTm90aWNlTWFuYWdlclNlcnZpY2UnLCBbJyRsb2cnLCAnUmVzdFNlcnZpY2VIYW5kbGVyJywgTm90aWNlTWFuYWdlclNlcnZpY2VdKTtcclxuXHJcbi8vIENvbnRyb2xsZXJzXHJcbk5vdGljZU1hbmFnZXJNb2R1bGUuY29udHJvbGxlcignTm90aWNlTGlzdCcsIFsnJGxvZycsICckc3RhdGUnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgTm90aWNlTGlzdF0pO1xyXG5cclxuLy8gTW9kYWwgLSBDb250cm9sbGVyc1xyXG5Ob3RpY2VNYW5hZ2VyTW9kdWxlLmNvbnRyb2xsZXIoJ0VkaXROb3RpY2UnLCBbJyRsb2cnLCAnTm90aWNlTWFuYWdlclNlcnZpY2UnLCAnJHVpYk1vZGFsJywgJyR1aWJNb2RhbEluc3RhbmNlJywgJ3BhcmFtcycsIEVkaXROb3RpY2VdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IE5vdGljZU1hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIEVkaXROb3RpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIG5vdGljZU1hbmFnZXJTZXJ2aWNlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlLCBwYXJhbXMpIHtcclxuICAgICAgICB0aGlzLm5vdGljZU1hbmFnZXJTZXJ2aWNlID0gbm90aWNlTWFuYWdlclNlcnZpY2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uID0gcGFyYW1zLmFjdGlvbjtcclxuICAgICAgICB0aGlzLmFjdGlvblR5cGUgPSBwYXJhbXMuYWN0aW9uVHlwZTtcclxuXHJcbiAgICAgICAgdGhpcy5rZW5kb0VkaXRvclRvb2xzID0gW1xyXG4gICAgICAgICAgICAnZm9ybWF0dGluZycsICdjbGVhbkZvcm1hdHRpbmcnLFxyXG4gICAgICAgICAgICAnZm9udE5hbWUnLCAnZm9udFNpemUnLFxyXG4gICAgICAgICAgICAnanVzdGlmeUxlZnQnLCAnanVzdGlmeUNlbnRlcicsICdqdXN0aWZ5UmlnaHQnLCAnanVzdGlmeUZ1bGwnLFxyXG4gICAgICAgICAgICAnYm9sZCcsXHJcbiAgICAgICAgICAgICdpdGFsaWMnLFxyXG4gICAgICAgICAgICAndmlld0h0bWwnXHJcbiAgICAgICAgXTtcclxuXHJcbiAgICAgICAgLy8gQ1NTIGhhcyBub3QgY2FuY2VsaW5nIGF0dHJpYnV0ZXMsIHNvIGluc3RlYWQgb2YgcmVtb3ZpbmcgZXZlcnkgcG9zc2libGUgSFRNTCwgd2UgbWFrZSBlZGl0b3IgaGFzIHNhbWUgY3NzXHJcbiAgICAgICAgdGhpcy5rZW5kb1N0eWxlc2hlZXRzID0gW1xyXG4gICAgICAgICAgICAnLi4vc3RhdGljL2Rpc3QvanMvdmVuZG9ycy9ib290c3RyYXAvZGlzdC9jc3MvYm9vdHN0cmFwLm1pbi5jc3MnLCAvLyBPdXJ0IGN1cnJlbnQgQm9vdHN0cmFwIGNzc1xyXG4gICAgICAgICAgICAnLi4vc3RhdGljL2Rpc3QvY3NzL1REU1RNTGF5b3V0Lm1pbi5jc3MnIC8vIE9yaWdpbmFsIFRlbXBsYXRlIENTU1xyXG5cclxuICAgICAgICBdO1xyXG5cclxuICAgICAgICB0aGlzLmdldFR5cGVEYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5lZGl0TW9kZWwgPSB7XHJcbiAgICAgICAgICAgIHRpdGxlOiAnJyxcclxuICAgICAgICAgICAgdHlwZUlkOiAwLFxyXG4gICAgICAgICAgICBhY3RpdmU6IGZhbHNlLFxyXG4gICAgICAgICAgICBodG1sVGV4dDogJycsXHJcbiAgICAgICAgICAgIHJhd1RleHQ6ICcnXHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICAvLyBPbiBFZGl0aW9uIE1vZGUgd2UgY2MgdGhlIG1vZGVsIGFuZCBvbmx5IHRoZSBwYXJhbXMgd2UgbmVlZFxyXG4gICAgICAgIGlmKHBhcmFtcy5ub3RpY2UpIHtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuaWQgPSBwYXJhbXMubm90aWNlLmlkO1xyXG4gICAgICAgICAgICB0aGlzLmVkaXRNb2RlbC50aXRsZSA9IHBhcmFtcy5ub3RpY2UudGl0bGU7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLnR5cGVJZCA9IHBhcmFtcy5ub3RpY2UudHlwZS5pZDtcclxuICAgICAgICAgICAgdGhpcy5lZGl0TW9kZWwuYWN0aXZlID0gcGFyYW1zLm5vdGljZS5hY3RpdmU7XHJcbiAgICAgICAgICAgIHRoaXMuZWRpdE1vZGVsLmh0bWxUZXh0ID0gcGFyYW1zLm5vdGljZS5odG1sVGV4dDtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBQb3B1bGF0ZSB0aGUgRW52aXJvbm1lbnQgZHJvcGRvd24gdmFsdWVzXHJcbiAgICAgKi9cclxuICAgIGdldFR5cGVEYXRhU291cmNlKCkge1xyXG4gICAgICAgIHRoaXMudHlwZURhdGFTb3VyY2UgPSBbXHJcbiAgICAgICAgICAgIHt0eXBlSWQ6IDEsIG5hbWU6ICdQcmVsb2dpbid9LFxyXG4gICAgICAgICAgICB7dHlwZUlkOiAyLCBuYW1lOiAnUG9zdGxvZ2luJ31cclxuICAgICAgICAgICAgLy97dHlwZUlkOiAzLCBuYW1lOiAnR2VuZXJhbCd9IERpc2FibGVkIHVudGlsIFBoYXNlIElJXHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEV4ZWN1dGUgdGhlIFNlcnZpY2UgY2FsbCB0byBDcmVhdGUvRWRpdCBhIG5vdGljZVxyXG4gICAgICovXHJcbiAgICBzYXZlTm90aWNlKCkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8odGhpcy5hY3Rpb24gKyAnIE5vdGljZSBSZXF1ZXN0ZWQ6ICcsIHRoaXMuZWRpdE1vZGVsKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbC5yYXdUZXh0ID0gJCgnI2tlbmRvLWVkaXRvci1jcmVhdGUtZWRpdCcpLnRleHQoKTtcclxuICAgICAgICB0aGlzLmVkaXRNb2RlbC50eXBlSWQgPSBwYXJzZUludCh0aGlzLmVkaXRNb2RlbC50eXBlSWQpO1xyXG4gICAgICAgIGlmKHRoaXMuYWN0aW9uID09PSB0aGlzLmFjdGlvblR5cGUuTkVXKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuY3JlYXRlTm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9IGVsc2UgaWYodGhpcy5hY3Rpb24gPT09IHRoaXMuYWN0aW9uVHlwZS5FRElUKSB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZWRpdE5vdGljZSh0aGlzLmVkaXRNb2RlbCwgKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMudWliTW9kYWxJbnN0YW5jZS5jbG9zZShkYXRhKTtcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGRlbGV0ZU5vdGljZSgpIHtcclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9kaWFsb2dBY3Rpb24vRGlhbG9nQWN0aW9uLmh0bWwnLFxyXG4gICAgICAgICAgICBjb250cm9sbGVyOiAnRGlhbG9nQWN0aW9uIGFzIGRpYWxvZ0FjdGlvbicsXHJcbiAgICAgICAgICAgIHNpemU6ICdzbScsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIHBhcmFtczogKCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IHRpdGxlOiAnQ29uZmlybWF0aW9uIFJlcXVpcmVkJywgbWVzc2FnZTogJ0FyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byBkZWxldGUgaXQ/IFRoaXMgYWN0aW9uIGNhbm5vdCBiZSB1bmRvbmUuJ307XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxuXHJcbiAgICAgICAgbW9kYWxJbnN0YW5jZS5yZXN1bHQudGhlbigoKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UuZGVsZXRlTm90aWNlKHRoaXMuZWRpdE1vZGVsLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy51aWJNb2RhbEluc3RhbmNlLmNsb3NlKGRhdGEpO1xyXG4gICAgICAgICAgICB9KTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIERpc21pc3MgdGhlIGRpYWxvZywgbm8gYWN0aW9uIG5lY2Vzc2FyeVxyXG4gICAgICovXHJcbiAgICBjYW5jZWxDbG9zZURpYWxvZygpIHtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsJyk7XHJcbiAgICB9XHJcblxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMC8wNy8yMDE2LlxyXG4gKi9cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTGlzdCB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHN0YXRlLCBub3RpY2VNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc3RhdGUgPSAkc3RhdGU7XHJcblxyXG4gICAgICAgIHRoaXMuYWN0aW9uVHlwZSA9IHtcclxuICAgICAgICAgICAgTkVXOiAnTmV3JyxcclxuICAgICAgICAgICAgRURJVDogJ0VkaXQnXHJcbiAgICAgICAgfTtcclxuXHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkID0ge307XHJcbiAgICAgICAgdGhpcy5ub3RpY2VHcmlkT3B0aW9ucyA9IHt9O1xyXG4gICAgICAgIHRoaXMubm90aWNlTWFuYWdlclNlcnZpY2UgPSBub3RpY2VNYW5hZ2VyU2VydmljZTtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsID0gJHVpYk1vZGFsO1xyXG5cclxuICAgICAgICB0aGlzLmdldERhdGFTb3VyY2UoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTGljZW5zZUxpc3QgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLm5vdGljZUdyaWRPcHRpb25zID0ge1xyXG4gICAgICAgICAgICB0b29sYmFyOiBrZW5kby50ZW1wbGF0ZSgnPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3M9XCJidG4gYnRuLWRlZmF1bHQgYWN0aW9uLXRvb2xiYXItYnRuXCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuTkVXKVwiPjxzcGFuIGNsYXNzPVwiZ2x5cGhpY29uIGdseXBoaWNvbi1wbHVzXCIgYXJpYS1oaWRkZW49XCJ0cnVlXCI+PC9zcGFuPiBDcmVhdGUgTmV3IE5vdGljZTwvYnV0dG9uPiA8ZGl2IG5nLWNsaWNrPVwibm90aWNlTGlzdC5yZWxvYWROb3RpY2VMaXN0KClcIiBjbGFzcz1cImFjdGlvbi10b29sYmFyLXJlZnJlc2gtYnRuXCI+PHNwYW4gY2xhc3M9XCJnbHlwaGljb24gZ2x5cGhpY29uLXJlZnJlc2hcIiBhcmlhLWhpZGRlbj1cInRydWVcIj48L3NwYW4+PC9kaXY+JyksXHJcbiAgICAgICAgICAgIHBhZ2VhYmxlOiB7XHJcbiAgICAgICAgICAgICAgICByZWZyZXNoOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgcGFnZVNpemVzOiB0cnVlLFxyXG4gICAgICAgICAgICAgICAgYnV0dG9uQ291bnQ6IDVcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY29sdW1uczogW1xyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnaWQnLCBoaWRkZW46IHRydWUgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2h0bWxUZXh0JywgaGlkZGVuOiB0cnVlIH0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhY3Rpb24nLCB0aXRsZTogJ0FjdGlvbicsIHdpZHRoOiA4MCwgdGVtcGxhdGU6ICc8YnV0dG9uIGNsYXNzPVwiYnRuIGJ0bi1kZWZhdWx0XCIgbmctY2xpY2s9XCJub3RpY2VMaXN0Lm9uRWRpdENyZWF0ZU5vdGljZShub3RpY2VMaXN0LmFjdGlvblR5cGUuRURJVCwgdGhpcylcIj48c3BhbiBjbGFzcz1cImdseXBoaWNvbiBnbHlwaGljb24tcGVuY2lsXCI+PC9zcGFuPjwvYnV0dG9uPicgfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RpdGxlJywgdGl0bGU6ICdUaXRsZSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAndHlwZS5pZCcsIGhpZGRlbjogdHJ1ZX0sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICd0eXBlLm5hbWUnLCB0aXRsZTogJ1R5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2FjdGl2ZScsIHRpdGxlOiAnQWN0aXZlJywgdGVtcGxhdGU6ICcjaWYoYWN0aXZlKSB7IyBZZXMgI30gZWxzZSB7IyBObyAjfSMnIH1cclxuICAgICAgICAgICAgXSxcclxuICAgICAgICAgICAgZGF0YVNvdXJjZToge1xyXG4gICAgICAgICAgICAgICAgcGFnZVNpemU6IDEwLFxyXG4gICAgICAgICAgICAgICAgdHJhbnNwb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmVhZDogKGUpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpcy5ub3RpY2VNYW5hZ2VyU2VydmljZS5nZXROb3RpY2VMaXN0KChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBlLnN1Y2Nlc3MoZGF0YSk7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH0pO1xyXG4gICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgICAgICBzb3J0OiB7XHJcbiAgICAgICAgICAgICAgICAgICAgZmllbGQ6ICd0aXRsZScsXHJcbiAgICAgICAgICAgICAgICAgICAgZGlyOiAnYXNjJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBPcGVuIGEgZGlhbG9nIHdpdGggdGhlIEJhc2ljIEZvcm0gdG8gcmVxdWVzdCBhIE5ldyBOb3RpY2VcclxuICAgICAqL1xyXG4gICAgb25FZGl0Q3JlYXRlTm90aWNlKGFjdGlvbiwgbm90aWNlKSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvbm90aWNlTWFuYWdlci9lZGl0L0VkaXROb3RpY2UuaHRtbCcsXHJcbiAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdFZGl0Tm90aWNlIGFzIGVkaXROb3RpY2UnLFxyXG4gICAgICAgICAgICBzaXplOiAnbWQnLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICB2YXIgZGF0YUl0ZW0gPSBub3RpY2UgJiYgbm90aWNlLmRhdGFJdGVtO1xyXG4gICAgICAgICAgICAgICAgICAgIHJldHVybiB7IGFjdGlvbjogYWN0aW9uLCBub3RpY2U6IGRhdGFJdGVtLCBhY3Rpb25UeXBlOiB0aGlzLmFjdGlvblR5cGV9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKG5vdGljZSkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLmxvZy5pbmZvKGFjdGlvbiArICcgTm90aWNlOiAnLCBub3RpY2UpO1xyXG4gICAgICAgICAgICAvLyBBZnRlciBhIG5ldyB2YWx1ZSBpcyBhZGRlZCwgbGV0cyB0byByZWZyZXNoIHRoZSBHcmlkXHJcbiAgICAgICAgICAgIHRoaXMucmVsb2FkTm90aWNlTGlzdCgpO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbyhhY3Rpb24gKyAnIFJlcXVlc3QgQ2FuY2VsZWQuJyk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgcmVsb2FkTm90aWNlTGlzdCgpIHtcclxuICAgICAgICBpZih0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZSkge1xyXG4gICAgICAgICAgICB0aGlzLm5vdGljZUdyaWQuZGF0YVNvdXJjZS5yZWFkKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTAvMDcvMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgTm90aWNlTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLlRZUEUgPSB7XHJcbiAgICAgICAgICAgICcxJzogJ1ByZWxvZ2luJyxcclxuICAgICAgICAgICAgJzInOiAnUG9zdGxvZ2luJyxcclxuICAgICAgICAgICAgJzMnOiAnR2VuZXJhbCdcclxuICAgICAgICB9O1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnTm90aWNlTWFuYWdlclNlcnZpY2UgSW5zdGFuY2VkJyk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0Tm90aWNlTGlzdChjYWxsYmFjaykge1xyXG4gICAgICAgIHRoaXMucmVzdFNlcnZpY2Uubm90aWNlTWFuYWdlclNlcnZpY2VIYW5kbGVyKCkuZ2V0Tm90aWNlTGlzdCgoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICB2YXIgbm90aWNlTGlzdCA9IFtdO1xyXG4gICAgICAgICAgICB0cnkge1xyXG4gICAgICAgICAgICAgICAgLy8gVmVyaWZ5IHRoZSBMaXN0IHJldHVybnMgd2hhdCB3ZSBleHBlY3QgYW5kIHdlIGNvbnZlcnQgaXQgdG8gYW4gQXJyYXkgdmFsdWVcclxuICAgICAgICAgICAgICAgIGlmKGRhdGEgJiYgZGF0YS5ub3RpY2VzKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgbm90aWNlTGlzdCA9IGRhdGEubm90aWNlcztcclxuICAgICAgICAgICAgICAgICAgICBpZiAobm90aWNlTGlzdCAmJiBub3RpY2VMaXN0Lmxlbmd0aCA+IDApIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgZm9yICh2YXIgaSA9IDA7IGkgPCBub3RpY2VMaXN0Lmxlbmd0aDsgaSA9IGkgKyAxKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBub3RpY2VMaXN0W2ldLnR5cGUgPSB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgaWQ6IG5vdGljZUxpc3RbaV0udHlwZUlkLFxyXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIG5hbWU6IHRoaXMuVFlQRVtub3RpY2VMaXN0W2ldLnR5cGVJZF1cclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIH07XHJcbiAgICAgICAgICAgICAgICAgICAgICAgICAgICBkZWxldGUgbm90aWNlTGlzdFtpXS50eXBlSWQ7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0gY2F0Y2goZSkge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5sb2cuZXJyb3IoJ0Vycm9yIHBhcnNpbmcgdGhlIE5vdGljZSBMaXN0JywgZSk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgcmV0dXJuIGNhbGxiYWNrKG5vdGljZUxpc3QpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogQ3JlYXRlIGEgTmV3IE5vdGljZSBwYXNzaW5nIHBhcmFtc1xyXG4gICAgICogQHBhcmFtIG5vdGljZVxyXG4gICAgICogQHBhcmFtIGNhbGxiYWNrXHJcbiAgICAgKi9cclxuICAgIGNyZWF0ZU5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmNyZWF0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIE5vdGljZSBzaG91bGQgaGF2ZSB0aGUgSUQgaW4gb3JkZXIgdG8gZWRpdCB0aGUgTm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZWRpdE5vdGljZShub3RpY2UsIGNhbGxiYWNrKXtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmVkaXROb3RpY2Uobm90aWNlLCAoZGF0YSkgPT4ge1xyXG4gICAgICAgICAgICByZXR1cm4gY2FsbGJhY2soZGF0YSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBOb3RpY2Ugc2hvdWxkIGhhdmUgdGhlIElEIGluIG9yZGVyIHRvIGRlbGV0ZSB0aGUgbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gbm90aWNlXHJcbiAgICAgKiBAcGFyYW0gY2FsbGJhY2tcclxuICAgICAqL1xyXG4gICAgZGVsZXRlTm90aWNlKG5vdGljZSwgY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLm5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpLmRlbGV0ZU5vdGljZShub3RpY2UsIChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxuXHJcbn1cclxuXHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCB1aVJvdXRlciBmcm9tICd1aS1yb3V0ZXInO1xyXG5cclxuaW1wb3J0IFRhc2tNYW5hZ2VyU2VydmljZSBmcm9tICcuL3NlcnZpY2UvVGFza01hbmFnZXJTZXJ2aWNlLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyQ29udHJvbGxlciBmcm9tICcuL2xpc3QvVGFza01hbmFnZXJDb250cm9sbGVyLmpzJztcclxuaW1wb3J0IFRhc2tNYW5hZ2VyRWRpdCBmcm9tICcuL2VkaXQvVGFza01hbmFnZXJFZGl0LmpzJztcclxuXHJcbnZhciBUYXNrTWFuYWdlck1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5UYXNrTWFuYWdlck1vZHVsZScsIFt1aVJvdXRlcl0pLmNvbmZpZyhbJyRzdGF0ZVByb3ZpZGVyJywgJ2Zvcm1seUNvbmZpZ1Byb3ZpZGVyJyxcclxuICAgIGZ1bmN0aW9uICgkc3RhdGVQcm92aWRlciwgZm9ybWx5Q29uZmlnUHJvdmlkZXIpIHtcclxuXHJcbiAgICBmb3JtbHlDb25maWdQcm92aWRlci5zZXRUeXBlKHtcclxuICAgICAgICBuYW1lOiAnY3VzdG9tJyxcclxuICAgICAgICB0ZW1wbGF0ZVVybDogJ2N1c3RvbS5odG1sJ1xyXG4gICAgfSk7XHJcblxyXG4gICAgLy8gRGVmaW5lIGEgZ2VuZXJpYyBoZWFkZXIgZm9yIHRoZSBlbnRpcmUgbW9kdWxlLCBvciBpdCBjYW4gYmUgY2hhbmdlZCBmb3IgZWFjaCBpbnN0YW5jZS5cclxuICAgIHZhciBoZWFkZXIgPSB7XHJcbiAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy9oZWFkZXIvSGVhZGVyVmlldy5odG1sJyxcclxuICAgICAgICBjb250cm9sbGVyOiAnSGVhZGVyQ29udHJvbGxlciBhcyBoZWFkZXInXHJcbiAgICB9O1xyXG5cclxuICAgICRzdGF0ZVByb3ZpZGVyXHJcbiAgICAgICAgLnN0YXRlKCd0YXNrTGlzdCcsIHtcclxuICAgICAgICAgICAgZGF0YToge3BhZ2U6IHt0aXRsZTogJ015IFRhc2sgTWFuYWdlcicsIGluc3RydWN0aW9uOiAnJywgbWVudTogWydUYXNrIE1hbmFnZXInXX19LFxyXG4gICAgICAgICAgICB1cmw6ICcvdGFzay9saXN0JyxcclxuICAgICAgICAgICAgdmlld3M6IHtcclxuICAgICAgICAgICAgICAgICdoZWFkZXJWaWV3QCc6IGhlYWRlcixcclxuICAgICAgICAgICAgICAgICdib2R5Vmlld0AnOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICcuLi9hcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9saXN0L1Rhc2tNYW5hZ2VyQ29udGFpbmVyLmh0bWwnLFxyXG4gICAgICAgICAgICAgICAgICAgIGNvbnRyb2xsZXI6ICdUYXNrTWFuYWdlckNvbnRyb2xsZXIgYXMgdGFza01hbmFnZXInXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9KTtcclxufV0pO1xyXG5cclxuLy8gU2VydmljZXNcclxuVGFza01hbmFnZXJNb2R1bGUuc2VydmljZSgndGFza01hbmFnZXJTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFRhc2tNYW5hZ2VyU2VydmljZV0pO1xyXG5cclxuLy8gQ29udHJvbGxlcnNcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJDb250cm9sbGVyJywgWyckbG9nJywgJ3Rhc2tNYW5hZ2VyU2VydmljZScsICckdWliTW9kYWwnLCBUYXNrTWFuYWdlckNvbnRyb2xsZXJdKTtcclxuVGFza01hbmFnZXJNb2R1bGUuY29udHJvbGxlcignVGFza01hbmFnZXJFZGl0JywgWyckbG9nJywgVGFza01hbmFnZXJFZGl0XSk7XHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgVGFza01hbmFnZXJNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAzLzExLzIwMTYuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgVGFza01hbmFnZXJFZGl0IHtcclxuXHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCB0YXNrTWFuYWdlclNlcnZpY2UsICR1aWJNb2RhbCkge1xyXG5cclxuICAgIH1cclxuXHJcbn0iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzIwLzIwMTUuXHJcbiAqL1xyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlckNvbnRyb2xsZXIge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHRhc2tNYW5hZ2VyU2VydmljZSwgJHVpYk1vZGFsKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMudWliTW9kYWwgPSAkdWliTW9kYWw7XHJcbiAgICAgICAgdGhpcy5tb2R1bGUgPSAnVGFza01hbmFnZXInO1xyXG4gICAgICAgIHRoaXMudGFza01hbmFnZXJTZXJ2aWNlID0gdGFza01hbmFnZXJTZXJ2aWNlO1xyXG4gICAgICAgIHRoaXMudGFza0dyaWRPcHRpb25zID0ge307XHJcbiAgICAgICAgdGhpcy5ldmVudERhdGFTb3VyY2UgPSBbXTtcclxuXHJcbiAgICAgICAgLy8gSW5pdCBDbGFzc1xyXG4gICAgICAgIHRoaXMuZ2V0RXZlbnREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5nZXREYXRhU291cmNlKCk7XHJcbiAgICAgICAgdGhpcy5sb2cuZGVidWcoJ1Rhc2tNYW5hZ2VyIENvbnRyb2xsZXIgSW5zdGFuY2VkJyk7XHJcbiAgICAgICAgdGhpcy5pbml0Rm9ybSgpO1xyXG5cclxuICAgIH1cclxuXHJcbiAgICBvcGVuTW9kYWxEZW1vKCkge1xyXG5cclxuICAgICAgICB2YXIgbW9kYWxJbnN0YW5jZSA9IHRoaXMudWliTW9kYWwub3Blbih7XHJcbiAgICAgICAgICAgIGFuaW1hdGlvbjogdHJ1ZSxcclxuICAgICAgICAgICAgdGVtcGxhdGVVcmw6ICdhcHAtanMvbW9kdWxlcy90YXNrTWFuYWdlci9lZGl0L1Rhc2tNYW5hZ2VyRWRpdC5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ1Rhc2tNYW5hZ2VyRWRpdCcsXHJcbiAgICAgICAgICAgIHNpemU6ICdsZycsXHJcbiAgICAgICAgICAgIHJlc29sdmU6IHtcclxuICAgICAgICAgICAgICAgIGl0ZW1zOiBmdW5jdGlvbiAoKSB7XHJcbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIFsnMScsJ2EyJywnZ2cnXTtcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH0pO1xyXG5cclxuICAgICAgICBtb2RhbEluc3RhbmNlLnJlc3VsdC50aGVuKChzZWxlY3RlZEl0ZW0pID0+IHtcclxuICAgICAgICAgICAgdGhpcy5kZWJ1ZyhzZWxlY3RlZEl0ZW0pO1xyXG4gICAgICAgIH0sICgpID0+IHtcclxuICAgICAgICAgICAgdGhpcy5sb2cuaW5mbygnTW9kYWwgZGlzbWlzc2VkIGF0OiAnICsgbmV3IERhdGUoKSk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLnRhc2tHcmlkT3B0aW9ucyA9IHtcclxuICAgICAgICAgICAgZ3JvdXBhYmxlOiB0cnVlLFxyXG4gICAgICAgICAgICBzb3J0YWJsZTogdHJ1ZSxcclxuICAgICAgICAgICAgcGFnZWFibGU6IHtcclxuICAgICAgICAgICAgICAgIHJlZnJlc2g6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBwYWdlU2l6ZXM6IHRydWUsXHJcbiAgICAgICAgICAgICAgICBidXR0b25Db3VudDogNVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjb2x1bW5zOiBbe2ZpZWxkOiAnYWN0aW9uJywgdGl0bGU6ICdBY3Rpb24nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3Rhc2snLCB0aXRsZTogJ1Rhc2snfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2Rlc2NyaXB0aW9uJywgdGl0bGU6ICdEZXNjcmlwdGlvbid9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzZXROYW1lJywgdGl0bGU6ICdBc3NldCBOYW1lJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdhc3NldFR5cGUnLCB0aXRsZTogJ0Fzc2V0IFR5cGUnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3VwZGF0ZWQnLCB0aXRsZTogJ1VwZGF0ZWQnfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2R1ZScsIHRpdGxlOiAnRHVlJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzdGF0dXMnLCB0aXRsZTogJ1N0YXR1cyd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnYXNzaWduZWRUbycsIHRpdGxlOiAnQXNzaWduZWQgVG8nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ3RlYW0nLCB0aXRsZTogJ1RlYW0nfSxcclxuICAgICAgICAgICAgICAgIHtmaWVsZDogJ2NhdGVnb3J5JywgdGl0bGU6ICdDYXRlZ29yeSd9LFxyXG4gICAgICAgICAgICAgICAge2ZpZWxkOiAnc3VjJywgdGl0bGU6ICdTdWMuJ30sXHJcbiAgICAgICAgICAgICAgICB7ZmllbGQ6ICdzY29yZScsIHRpdGxlOiAnU2NvcmUnfV0sXHJcbiAgICAgICAgICAgIGRhdGFTb3VyY2U6IHtcclxuICAgICAgICAgICAgICAgIHBhZ2VTaXplOiAxMCxcclxuICAgICAgICAgICAgICAgIHRyYW5zcG9ydDoge1xyXG4gICAgICAgICAgICAgICAgICAgIHJlYWQ6IChlKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAgICAgICAgIC8qdGhpcy50YXNrTWFuYWdlclNlcnZpY2UudGVzdFNlcnZpY2UoKGRhdGEpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGUuc3VjY2VzcyhkYXRhKTtcclxuICAgICAgICAgICAgICAgICAgICAgICAgfSk7Ki9cclxuICAgICAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGdldEV2ZW50RGF0YVNvdXJjZSgpIHtcclxuICAgICAgICB0aGlzLmV2ZW50RGF0YVNvdXJjZSA9IFtcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDEsIGV2ZW50TmFtZTogJ0FsbCd9LFxyXG4gICAgICAgICAgICB7ZXZlbnRJZDogMiwgZXZlbnROYW1lOiAnQnVpbGRvdXQnfSxcclxuICAgICAgICAgICAge2V2ZW50SWQ6IDMsIGV2ZW50TmFtZTogJ0RSLUVQJ30sXHJcbiAgICAgICAgICAgIHtldmVudElkOiA0LCBldmVudE5hbWU6ICdNMS1QaHlzaWNhbCd9XHJcbiAgICAgICAgXTtcclxuICAgIH1cclxuXHJcbiAgICBvbkVycm9ySGFwcGVucygpIHtcclxuICAgICAgICB0aGlzLnRhc2tNYW5hZ2VyU2VydmljZS5mYWlsQ2FsbChmdW5jdGlvbiAoKSB7XHJcblxyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIGluaXRGb3JtKCkge1xyXG4gICAgICAgIHRoaXMudXNlckZpZWxkcyA9IFtcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAga2V5OiAnZW1haWwnLFxyXG4gICAgICAgICAgICAgICAgdHlwZTogJ2lucHV0JyxcclxuICAgICAgICAgICAgICAgIHRlbXBsYXRlT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgICAgIHR5cGU6ICdlbWFpbCcsXHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdFbWFpbCBhZGRyZXNzJyxcclxuICAgICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcjogJ0VudGVyIGVtYWlsJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdwYXNzd29yZCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnaW5wdXQnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgdHlwZTogJ3Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgICAgICBsYWJlbDogJ1Bhc3N3b3JkJyxcclxuICAgICAgICAgICAgICAgICAgICBwbGFjZWhvbGRlcjogJ1Bhc3N3b3JkJ1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICB7XHJcbiAgICAgICAgICAgICAgICBrZXk6ICdmaWxlJyxcclxuICAgICAgICAgICAgICAgIHR5cGU6ICdmaWxlJyxcclxuICAgICAgICAgICAgICAgIHRlbXBsYXRlT3B0aW9uczoge1xyXG4gICAgICAgICAgICAgICAgICAgIGxhYmVsOiAnRmlsZSBpbnB1dCcsXHJcbiAgICAgICAgICAgICAgICAgICAgZGVzY3JpcHRpb246ICdFeGFtcGxlIGJsb2NrLWxldmVsIGhlbHAgdGV4dCBoZXJlJyxcclxuICAgICAgICAgICAgICAgICAgICB1cmw6ICdodHRwczovL2V4YW1wbGUuY29tL3VwbG9hZCdcclxuICAgICAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAge1xyXG4gICAgICAgICAgICAgICAga2V5OiAnY2hlY2tlZCcsXHJcbiAgICAgICAgICAgICAgICB0eXBlOiAnY2hlY2tib3gnLFxyXG4gICAgICAgICAgICAgICAgdGVtcGxhdGVPcHRpb25zOiB7XHJcbiAgICAgICAgICAgICAgICAgICAgbGFiZWw6ICdDaGVjayBtZSBvdXQnXHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICBdO1xyXG4gICAgfVxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAyMi8wNy8xNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBUYXNrTWFuYWdlclNlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIFJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gUmVzdFNlcnZpY2VIYW5kbGVyO1xyXG5cclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnVGFza01hbmFnZXJTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIGZhaWxDYWxsKGNhbGxiYWNrKSB7XHJcbiAgICAgICAgdGhpcy5yZXN0U2VydmljZS5SZXNvdXJjZVNlcnZpY2VIYW5kbGVyKCkuZ2V0U1ZHKCk7XHJcbiAgICB9XHJcblxyXG4gICAgdGVzdFNlcnZpY2UoY2FsbGJhY2spIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLlRhc2tTZXJ2aWNlSGFuZGxlcigpLmdldEZlZWRzKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHJldHVybiBjYWxsYmFjayhkYXRhKTtcclxuICAgICAgICB9KTtcclxuICAgIH1cclxufVxyXG5cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8zLzIwMTYuXHJcbiAqL1xyXG5cclxuXHJcbid1c2Ugc3RyaWN0J1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRm9ybVZhbGlkYXRvciB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHNjb3BlLCAkdWliTW9kYWwsICR1aWJNb2RhbEluc3RhbmNlKSB7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuc2NvcGUgPSAkc2NvcGU7XHJcblxyXG4gICAgICAgIC8vIEpTIGRvZXMgYSBhcmd1bWVudCBwYXNzIGJ5IHJlZmVyZW5jZVxyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IG51bGw7XHJcbiAgICAgICAgLy8gQSBjb3B5IHdpdGhvdXQgcmVmZXJlbmNlIGZyb20gdGhlIG9yaWdpbmFsIG9iamVjdFxyXG4gICAgICAgIHRoaXMub3JpZ2luYWxEYXRhID0gbnVsbDtcclxuICAgICAgICAvLyBBIENDIGFzIEpTT04gZm9yIGNvbXBhcmlzb24gUHVycG9zZVxyXG4gICAgICAgIHRoaXMub2JqZWN0QXNKU09OID0gbnVsbDtcclxuXHJcblxyXG4gICAgICAgIC8vIE9ubHkgZm9yIE1vZGFsIFdpbmRvd3NcclxuICAgICAgICB0aGlzLnJlbG9hZFJlcXVpcmVkID0gZmFsc2U7XHJcbiAgICAgICAgdGhpcy51aWJNb2RhbCA9ICR1aWJNb2RhbDtcclxuICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UgPSAkdWliTW9kYWxJbnN0YW5jZTtcclxuXHJcbiAgICAgICAgaWYgKCRzY29wZS4kb24pIHtcclxuICAgICAgICAgICAgJHNjb3BlLiRvbignbW9kYWwuY2xvc2luZycsIChldmVudCwgcmVhc29uLCBjbG9zZWQpPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5vbkNsb3NlRGlhbG9nKGV2ZW50LCByZWFzb24sIGNsb3NlZClcclxuICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogU2F2ZXMgdGhlIEZvcm0gaW4gMyBpbnN0YW5jZXMsIG9uZSB0byBrZWVwIHRyYWNrIG9mIHRoZSBvcmlnaW5hbCBkYXRhLCBvdGhlciBpcyB0aGUgY3VycmVudCBvYmplY3QgYW5kXHJcbiAgICAgKiBhIEpTT04gZm9ybWF0IGZvciBjb21wYXJpc29uIHB1cnBvc2VcclxuICAgICAqIEBwYXJhbSBuZXdPYmplY3RJbnN0YW5jZVxyXG4gICAgICovXHJcbiAgICBzYXZlRm9ybShuZXdPYmplY3RJbnN0YW5jZSkge1xyXG4gICAgICAgIHRoaXMuY3VycmVudE9iamVjdCA9IG5ld09iamVjdEluc3RhbmNlO1xyXG4gICAgICAgIHRoaXMub3JpZ2luYWxEYXRhID0gYW5ndWxhci5jb3B5KG5ld09iamVjdEluc3RhbmNlLCB0aGlzLm9yaWdpbmFsRGF0YSk7XHJcbiAgICAgICAgdGhpcy5vYmplY3RBc0pTT04gPSBhbmd1bGFyLnRvSnNvbihuZXdPYmplY3RJbnN0YW5jZSk7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBHZXQgdGhlIEN1cnJlbnQgT2JqZWN0IG9uIGhpcyByZWZlcmVuY2UgRm9ybWF0XHJcbiAgICAgKiBAcmV0dXJucyB7bnVsbHwqfVxyXG4gICAgICovXHJcbiAgICBnZXRGb3JtKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmN1cnJlbnRPYmplY3Q7XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBHZXQgdGhlIE9iamVjdCBhcyBKU09OIGZyb20gdGhlIE9yaWdpbmFsIERhdGFcclxuICAgICAqIEByZXR1cm5zIHtudWxsfHN0cmluZ3x1bmRlZmluZWR8c3RyaW5nfCp9XHJcbiAgICAgKi9cclxuICAgIGdldEZvcm1Bc0pTT04oKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMub2JqZWN0QXNKU09OO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICpcclxuICAgICAqIEBwYXJhbSBvYmpldFRvUmVzZXQgb2JqZWN0IHRvIHJlc2V0XHJcbiAgICAgKiBAcGFyYW0gb25SZXNldEZvcm0gY2FsbGJhY2tcclxuICAgICAqIEByZXR1cm5zIHsqfVxyXG4gICAgICovXHJcbiAgICByZXNldEZvcm0ob25SZXNldEZvcm0pIHtcclxuICAgICAgICB0aGlzLmN1cnJlbnRPYmplY3QgPSBhbmd1bGFyLmNvcHkodGhpcy5vcmlnaW5hbERhdGEsIHRoaXMuY3VycmVudE9iamVjdCk7XHJcbiAgICAgICAgdGhpcy5zYWZlQXBwbHkoKTtcclxuXHJcbiAgICAgICAgaWYob25SZXNldEZvcm0pIHtcclxuICAgICAgICAgICAgcmV0dXJuIG9uUmVzZXRGb3JtKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVmFsaWRhdGVzIGlmIHRoZSBjdXJyZW50IG9iamVjdCBkaWZmZXJzIGZyb20gd2hlcmUgaXQgd2FzIG9yaWdpbmFsbHkgc2F2ZWRcclxuICAgICAqIEByZXR1cm5zIHtib29sZWFufVxyXG4gICAgICovXHJcbiAgICBpc0RpcnR5KCkge1xyXG4gICAgICAgIHZhciBuZXdPYmplY3RJbnN0YW5jZSA9IGFuZ3VsYXIudG9Kc29uKHRoaXMuY3VycmVudE9iamVjdCk7XHJcbiAgICAgICAgcmV0dXJuIG5ld09iamVjdEluc3RhbmNlICE9PSB0aGlzLmdldEZvcm1Bc0pTT04oKTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRoaXMgZnVuY3Rpb24gaXMgb25seSBhdmFpbGFibGUgd2hlbiB0aGUgRm9ybSBpcyBiZWluZyBjYWxsZWQgZnJvbSBhIERpYWxvZyBQb3BVcFxyXG4gICAgICovXHJcbiAgICBvbkNsb3NlRGlhbG9nKGV2ZW50LCByZWFzb24sIGNsb3NlZCkge1xyXG4gICAgICAgIHRoaXMubG9nLmluZm8oJ21vZGFsLmNsb3Npbmc6ICcgKyAoY2xvc2VkID8gJ2Nsb3NlJyA6ICdkaXNtaXNzJykgKyAnKCcgKyByZWFzb24gKyAnKScpO1xyXG4gICAgICAgIGlmICh0aGlzLmlzRGlydHkoKSAmJiByZWFzb24gIT09ICdjYW5jZWwtY29uZmlybWF0aW9uJyAmJiB0eXBlb2YgcmVhc29uICE9PSAnb2JqZWN0Jykge1xyXG4gICAgICAgICAgICBldmVudC5wcmV2ZW50RGVmYXVsdCgpO1xyXG4gICAgICAgICAgICB0aGlzLmNvbmZpcm1DbG9zZUZvcm0oKTtcclxuICAgICAgICB9XHJcbiAgICB9XHJcblxyXG4gICAgLyoqXHJcbiAgICAgKiBBIENvbmZpcm1hdGlvbiBEaWFsb2cgd2hlbiB0aGUgaW5mb3JtYXRpb24gY2FuIGJlIGxvc3RcclxuICAgICAqIEBwYXJhbSBldmVudFxyXG4gICAgICovXHJcbiAgICBjb25maXJtQ2xvc2VGb3JtKGV2ZW50KSB7XHJcbiAgICAgICAgdmFyIG1vZGFsSW5zdGFuY2UgPSB0aGlzLnVpYk1vZGFsLm9wZW4oe1xyXG4gICAgICAgICAgICBhbmltYXRpb246IHRydWUsXHJcbiAgICAgICAgICAgIHRlbXBsYXRlVXJsOiAnLi4vYXBwLWpzL21vZHVsZXMvZGlhbG9nQWN0aW9uL0RpYWxvZ0FjdGlvbi5odG1sJyxcclxuICAgICAgICAgICAgY29udHJvbGxlcjogJ0RpYWxvZ0FjdGlvbiBhcyBkaWFsb2dBY3Rpb24nLFxyXG4gICAgICAgICAgICBzaXplOiAnc20nLFxyXG4gICAgICAgICAgICByZXNvbHZlOiB7XHJcbiAgICAgICAgICAgICAgICBwYXJhbXM6ICgpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICAgICAgICAgICAgICB0aXRsZTogJ0NvbmZpcm1hdGlvbiBSZXF1aXJlZCcsXHJcbiAgICAgICAgICAgICAgICAgICAgICAgIG1lc3NhZ2U6ICdDaGFuZ2VzIHlvdSBtYWRlIG1heSBub3QgYmUgc2F2ZWQuIERvIHlvdSB3YW50IHRvIGNvbnRpbnVlPydcclxuICAgICAgICAgICAgICAgICAgICB9O1xyXG4gICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSk7XHJcblxyXG4gICAgICAgIG1vZGFsSW5zdGFuY2UucmVzdWx0LnRoZW4oKCkgPT4ge1xyXG4gICAgICAgICAgICB0aGlzLnVpYk1vZGFsSW5zdGFuY2UuZGlzbWlzcygnY2FuY2VsLWNvbmZpcm1hdGlvbicpO1xyXG4gICAgICAgIH0pO1xyXG4gICAgfVxyXG5cclxuICAgIC8qKlxyXG4gICAgICogVXRpbCB0byBjYWxsIHNhZmUgaWYgcmVxdWlyZWRcclxuICAgICAqIEBwYXJhbSBmblxyXG4gICAgICovXHJcbiAgICBzYWZlQXBwbHkoZm4pIHtcclxuICAgICAgICB2YXIgcGhhc2UgPSB0aGlzLnNjb3BlLiRyb290LiQkcGhhc2U7XHJcbiAgICAgICAgaWYocGhhc2UgPT09ICckYXBwbHknIHx8IHBoYXNlID09PSAnJGRpZ2VzdCcpIHtcclxuICAgICAgICAgICAgaWYoZm4gJiYgKHR5cGVvZihmbikgPT09ICdmdW5jdGlvbicpKSB7XHJcbiAgICAgICAgICAgICAgICBmbigpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgdGhpcy5zY29wZS4kYXBwbHkoZm4pO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFV0aWwgdG8gUmVzZXQgYSBEcm9wZG93biBsaXN0IG9uIEtlbmRvXHJcbiAgICAgKi9cclxuXHJcbiAgICByZXNldERyb3BEb3duKHNlbGVjdG9ySW5zdGFuY2UsIHNlbGVjdGVkSWQsIGZvcmNlKSB7XHJcbiAgICAgICAgaWYoc2VsZWN0b3JJbnN0YW5jZSAmJiBzZWxlY3Rvckluc3RhbmNlLmRhdGFJdGVtcykge1xyXG4gICAgICAgICAgICBzZWxlY3Rvckluc3RhbmNlLmRhdGFJdGVtcygpLmZvckVhY2goKHZhbHVlLCBpbmRleCkgPT4ge1xyXG4gICAgICAgICAgICAgICAgaWYoc2VsZWN0ZWRJZCA9PT0gdmFsdWUuaWQgfHwgc2VsZWN0ZWRJZCA9PT0gdmFsdWUpIHtcclxuICAgICAgICAgICAgICAgICAgICBzZWxlY3Rvckluc3RhbmNlLnNlbGVjdChpbmRleCk7XHJcbiAgICAgICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICAgICAgaWYoZm9yY2UpIHtcclxuICAgICAgICAgICAgICAgIHNlbGVjdG9ySW5zdGFuY2UudHJpZ2dlcignY2hhbmdlJyk7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnNhZmVBcHBseSgpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG59IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8yMy8yMDE1LlxyXG4gKiBJbXBsZW1lbnRzIFJYIE9ic2VydmFibGUgdG8gZGlzcG9zZSBhbmQgdHJhY2sgYmV0dGVyIGVhY2ggY2FsbCB0byB0aGUgc2VydmVyXHJcbiAqIFRoZSBPYnNlcnZlciBzdWJzY3JpYmUgYSBwcm9taXNlLlxyXG4gKi9cclxuXHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXF1ZXN0SGFuZGxlciB7XHJcbiAgICBjb25zdHJ1Y3RvcihyeCkge1xyXG4gICAgICAgIHRoaXMucnggPSByeDtcclxuICAgICAgICB0aGlzLnByb21pc2UgPSBbXTtcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIENhbGxlZCBmcm9tIFJlc3RTZXJ2aWNlSGFuZGxlci5zdWJzY3JpYmVSZXF1ZXN0XHJcbiAgICAgKiBpdCB2ZXJpZnkgdGhhdCB0aGUgY2FsbCBpcyBiZWluZyBkb25lIHRvIHRoZSBzZXJ2ZXIgYW5kIHJldHVybiBhIHByb21pc2VcclxuICAgICAqIEBwYXJhbSByZXF1ZXN0XHJcbiAgICAgKiBAcmV0dXJucyB7Kn1cclxuICAgICAqL1xyXG4gICAgc3Vic2NyaWJlUmVxdWVzdChyZXF1ZXN0LCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB2YXIgcnhPYnNlcnZhYmxlID0gdGhpcy5yeC5PYnNlcnZhYmxlLmZyb21Qcm9taXNlKHJlcXVlc3QpO1xyXG4gICAgICAgIC8vIFZlcmlmeSBpcyBub3QgYSBkdXBsaWNhdGUgY2FsbFxyXG4gICAgICAgIGlmICh0aGlzLmlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpKSB7XHJcbiAgICAgICAgICAgIHRoaXMuY2FuY2VsUmVxdWVzdChyeE9ic2VydmFibGUpO1xyXG4gICAgICAgIH1cclxuXHJcbiAgICAgICAgLy8gU3Vic2NyaWJlIHRoZSByZXF1ZXN0XHJcbiAgICAgICAgdmFyIHJlc3VsdFN1YnNjcmliZSA9IHRoaXMuYWRkU3Vic2NyaWJlKHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICBpZiAocmVzdWx0U3Vic2NyaWJlICYmIHJlc3VsdFN1YnNjcmliZS5pc1N0b3BwZWQpIHtcclxuICAgICAgICAgICAgLy8gQW4gZXJyb3IgaGFwcGVucywgdHJhY2tlZCBieSBIdHRwSW50ZXJjZXB0b3JJbnRlcmZhY2VcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICBhZGRTdWJzY3JpYmUocnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MsIG9uRXJyb3IpIHtcclxuICAgICAgICB0aGlzLnByb21pc2VbcnhPYnNlcnZhYmxlLl9wXSA9IHJ4T2JzZXJ2YWJsZS5zdWJzY3JpYmUoXHJcbiAgICAgICAgICAgIChyZXNwb25zZSkgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIHRoaXMub25TdWJzY3JpYmVkU3VjY2VzcyhyZXNwb25zZSwgcnhPYnNlcnZhYmxlLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICAoZXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiB0aGlzLm9uU3Vic2NyaWJlZEVycm9yKGVycm9yLCByeE9ic2VydmFibGUsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LCAoKSA9PiB7XHJcbiAgICAgICAgICAgICAgICAvLyBOTy1PUCBTdWJzY3JpYmUgY29tcGxldGVkXHJcbiAgICAgICAgICAgIH0pO1xyXG5cclxuICAgICAgICByZXR1cm4gdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICB9XHJcblxyXG4gICAgY2FuY2VsUmVxdWVzdChyeE9ic2VydmFibGUpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgICAgIHJ4T2JzZXJ2YWJsZS5kaXNwb3NlKCk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGlzU3Vic2NyaWJlZChyeE9ic2VydmFibGUpIHtcclxuICAgICAgICByZXR1cm4gKHJ4T2JzZXJ2YWJsZSAmJiByeE9ic2VydmFibGUuX3AgJiYgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF0pO1xyXG4gICAgfVxyXG5cclxuICAgIG9uU3Vic2NyaWJlZFN1Y2Nlc3MocmVzcG9uc2UsIHJ4T2JzZXJ2YWJsZSwgb25TdWNjZXNzKSB7XHJcbiAgICAgICAgaWYgKHRoaXMuaXNTdWJzY3JpYmVkKHJ4T2JzZXJ2YWJsZSkpIHtcclxuICAgICAgICAgICAgZGVsZXRlIHRoaXMucHJvbWlzZVtyeE9ic2VydmFibGUuX3BdO1xyXG4gICAgICAgIH1cclxuICAgICAgICBpZihvblN1Y2Nlc3Mpe1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKHJlc3BvbnNlLmRhdGEpO1xyXG4gICAgICAgIH1cclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIFRocm93cyBpbW1lZGlhdGVseSBlcnJvciB3aGVuIHRoZSBwZXRpdGlvbiBjYWxsIGlzIHdyb25nXHJcbiAgICAgKiBvciB3aXRoIGEgZGVsYXkgaWYgdGhlIGNhbGwgaXMgdmFsaWRcclxuICAgICAqIEBwYXJhbSBlcnJvclxyXG4gICAgICogQHJldHVybnMgeyp9XHJcbiAgICAgKi9cclxuICAgIG9uU3Vic2NyaWJlZEVycm9yKGVycm9yLCByeE9ic2VydmFibGUsIG9uRXJyb3IpIHtcclxuICAgICAgICBpZiAodGhpcy5pc1N1YnNjcmliZWQocnhPYnNlcnZhYmxlKSkge1xyXG4gICAgICAgICAgICBkZWxldGUgdGhpcy5wcm9taXNlW3J4T2JzZXJ2YWJsZS5fcF07XHJcbiAgICAgICAgfVxyXG4gICAgICAgIGlmKG9uRXJyb3Ipe1xyXG4gICAgICAgICAgICByZXR1cm4gb25FcnJvcih7fSk7XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICovXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgYW5ndWxhciAgZnJvbSAnYW5ndWxhcic7XHJcbmltcG9ydCBSZXN0U2VydmljZUhhbmRsZXIgZnJvbSAnLi9SZXN0U2VydmljZUhhbmRsZXIuanMnO1xyXG5pbXBvcnQgVXNlclByZWZlcmVuY2VzU2VydmljZSBmcm9tICcuL1VzZXJQcmVmZXJlbmNlc1NlcnZpY2UuanMnXHJcblxyXG52YXIgUmVzdEFQSU1vZHVsZSA9IGFuZ3VsYXIubW9kdWxlKCdURFNUTS5SZXN0QVBJTW9kdWxlJyxbXSk7XHJcblxyXG5SZXN0QVBJTW9kdWxlLnNlcnZpY2UoJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFsnJGxvZycsICckaHR0cCcsICckcmVzb3VyY2UnLCAncngnLCBSZXN0U2VydmljZUhhbmRsZXJdKTtcclxuUmVzdEFQSU1vZHVsZS5zZXJ2aWNlKCdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlJywgWyckbG9nJywgJ1Jlc3RTZXJ2aWNlSGFuZGxlcicsIFVzZXJQcmVmZXJlbmNlc1NlcnZpY2VdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IFJlc3RBUElNb2R1bGU7XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMDgvMTUuXHJcbiAqIEl0IGFic3RyYWN0IGVhY2ggb25lIG9mIHRoZSBleGlzdGluZyBjYWxsIHRvIHRoZSBBUEksIGl0IHNob3VsZCBvbmx5IGNvbnRhaW5zIHRoZSBjYWxsIGZ1bmN0aW9ucyBhbmQgcmVmZXJlbmNlXHJcbiAqIHRvIHRoZSBjYWxsYmFjaywgbm8gbG9naWMgYXQgYWxsLlxyXG4gKlxyXG4gKi9cclxuXHJcblxyXG4ndXNlIHN0cmljdCc7XHJcblxyXG5pbXBvcnQgUmVxdWVzdEhhbmRsZXIgZnJvbSAnLi9SZXF1ZXN0SGFuZGxlci5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBSZXN0U2VydmljZUhhbmRsZXIge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJGh0dHAsICRyZXNvdXJjZSwgcngpIHtcclxuICAgICAgICB0aGlzLnJ4ID0gcng7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMuaHR0cCA9ICRodHRwO1xyXG4gICAgICAgIHRoaXMucmVzb3VyY2UgPSAkcmVzb3VyY2U7XHJcbiAgICAgICAgdGhpcy5wcmVwYXJlSGVhZGVycygpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdSZXN0U2VydmljZSBMb2FkZWQnKTtcclxuICAgICAgICB0aGlzLnJlcSA9IHtcclxuICAgICAgICAgICAgbWV0aG9kOiAnJyxcclxuICAgICAgICAgICAgdXJsOiAnJyxcclxuICAgICAgICAgICAgaGVhZGVyczoge1xyXG4gICAgICAgICAgICAgICAgJ0NvbnRlbnQtVHlwZSc6ICdhcHBsaWNhdGlvbi9qc29uO2NoYXJzZXQ9VVRGLTgnXHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGRhdGE6IFtdXHJcbiAgICAgICAgfTtcclxuICAgIH1cclxuXHJcbiAgICBwcmVwYXJlSGVhZGVycygpIHtcclxuICAgICAgICB0aGlzLmh0dHAuZGVmYXVsdHMuaGVhZGVycy5wb3N0WydDb250ZW50LVR5cGUnXSA9ICdhcHBsaWNhdGlvbi94LXd3dy1mb3JtLXVybGVuY29kZWQnO1xyXG4gICAgfVxyXG5cclxuICAgIFRhc2tTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXRGZWVkczogKGNhbGxiYWNrKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gdGhpcy5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJ3Rlc3QvbW9ja3VwRGF0YS9UYXNrTWFuYWdlci90YXNrTWFuYWdlckxpc3QuanNvbicpLCBjYWxsYmFjayk7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGNvbW1vblNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldFRpbWVab25lQ29uZmlndXJhdGlvbjogKG9uU3VjY2VzcykgPT4ge1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL3VzZXIvcHJlZmVyZW5jZXMvQ1VSUl9EVF9GT1JNQVQsQ1VSUl9UWicpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VBZG1pblNlcnZpY2VIYW5kbGVyKCkge1xyXG4gICAgICAgIHJldHVybiB7XHJcbiAgICAgICAgICAgIGdldExpY2Vuc2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlcycpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRFbnZpcm9ubWVudERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL2Vudmlyb25tZW50JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldFByb2plY3REYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9wcm9qZWN0JyksIG9uU3VjY2Vzcyk7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGdldExpY2Vuc2VMaXN0OiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZScpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBjcmVhdGVOZXdMaWNlbnNlUmVxdWVzdDogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9saWNlbnNlL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgYXBwbHlMaWNlbnNlOiAgKGxpY2Vuc2VJZCwgZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvbG9hZCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gZGF0YTtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRIYXNoQ29kZTogIChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJyArIGxpY2Vuc2VJZCArICcvaGFzaCc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgLy8tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLVxyXG4gICAgICAgICAgICByZXN1Ym1pdExpY2Vuc2VSZXF1ZXN0OiAobGljZW5zZUlkLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9lbWFpbC9yZXF1ZXN0JztcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAodGhpcy5yZXEpLCBvblN1Y2Nlc3MsIG9uRXJyb3IpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBlbWFpbFJlcXVlc3Q6IChkYXRhLCBjYWxsYmFjaykgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BPU1QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy8/Pz8nO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLnBvc3QoJy4uL3Rlc3QvbW9ja3VwRGF0YS9MaWNlbnNlQWRtaW4vbGljZW5zZUFkbWluTGlzdC5qc29uJywgZGF0YSksIGNhbGxiYWNrKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGVsZXRlTGljZW5zZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0RFTEVURSc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL2xpY2Vuc2UvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIGxpY2Vuc2VNYW5hZ2VyU2VydmljZUhhbmRsZXIoKSB7XHJcbiAgICAgICAgcmV0dXJuIHtcclxuICAgICAgICAgICAgcmVxdWVzdEltcG9ydDogIChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdQT1NUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlL3JlcXVlc3QnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0TGljZW5zZUxpc3Q6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0UHJvamVjdERhdGFTb3VyY2U6IChvblN1Y2Nlc3MpID0+IHtcclxuICAgICAgICAgICAgICAgIHJldHVybiBuZXcgUmVxdWVzdEhhbmRsZXIodGhpcy5yeCkuc3Vic2NyaWJlUmVxdWVzdCh0aGlzLmh0dHAuZ2V0KCcuLi93cy9saWNlbnNlL3Byb2plY3QnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0RW52aXJvbm1lbnREYXRhU291cmNlOiAob25TdWNjZXNzKSA9PiB7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwLmdldCgnLi4vd3MvbGljZW5zZS9lbnZpcm9ubWVudCcpLCBvblN1Y2Nlc3MpO1xyXG4gICAgICAgICAgICB9LFxyXG4gICAgICAgICAgICBnZXRLZXlDb2RlOiAgKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnR0VUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQgKyAnL2tleSc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgc2F2ZUxpY2Vuc2U6IChsaWNlbnNlSWQsIGxpY2Vuc2VNb2RpZmllZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUFVUJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3MvbWFuYWdlci9saWNlbnNlLycgKyBsaWNlbnNlSWQ7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5kYXRhID0gbGljZW5zZU1vZGlmaWVkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIHJldm9rZUxpY2Vuc2U6IChkYXRhLCBvblN1Y2Nlc3MsIG9uRXJyb3IpID0+IHtcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLm1ldGhvZCA9ICdERUxFVEUnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEudXJsID0gICcuLi93cy9tYW5hZ2VyL2xpY2Vuc2UvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cCh0aGlzLnJlcSksIG9uU3VjY2Vzcywgb25FcnJvcik7XHJcbiAgICAgICAgICAgIH0sXHJcbiAgICAgICAgICAgIGFjdGl2YXRlTGljZW5zZTogKGxpY2Vuc2VJZCwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9hY3RpdmF0ZSc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZ2V0QWN0aXZpdHlMb2c6IChsaWNlbnNlSWQsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ0dFVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL21hbmFnZXIvbGljZW5zZS8nICsgbGljZW5zZUlkICsgJy9hY3Rpdml0eWxvZyc7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICB9O1xyXG4gICAgfVxyXG5cclxuICAgIG5vdGljZU1hbmFnZXJTZXJ2aWNlSGFuZGxlcigpIHtcclxuICAgICAgICByZXR1cm4ge1xyXG4gICAgICAgICAgICBnZXROb3RpY2VMaXN0OiAob25TdWNjZXNzKSA9PiB7IC8vIHJlYWwgd3MgZXhhbXBsZVxyXG4gICAgICAgICAgICAgICAgcmV0dXJuIG5ldyBSZXF1ZXN0SGFuZGxlcih0aGlzLnJ4KS5zdWJzY3JpYmVSZXF1ZXN0KHRoaXMuaHR0cC5nZXQoJy4uL3dzL25vdGljZXMnKSwgb25TdWNjZXNzKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgY3JlYXRlTm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnUE9TVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMnO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZWRpdE5vdGljZTogKGRhdGEsIG9uU3VjY2Vzcywgb25FcnJvcikgPT4ge1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEubWV0aG9kID0gJ1BVVCc7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS51cmwgPSAgJy4uL3dzL25vdGljZXMvJytkYXRhLmlkO1xyXG4gICAgICAgICAgICAgICAgdGhpcy5yZXEuZGF0YSA9IGRhdGE7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfSxcclxuICAgICAgICAgICAgZGVsZXRlTm90aWNlOiAoZGF0YSwgb25TdWNjZXNzLCBvbkVycm9yKSA9PiB7XHJcbiAgICAgICAgICAgICAgICB0aGlzLnJlcS5tZXRob2QgPSAnREVMRVRFJztcclxuICAgICAgICAgICAgICAgIHRoaXMucmVxLnVybCA9ICAnLi4vd3Mvbm90aWNlcy8nK2RhdGEuaWQ7XHJcbiAgICAgICAgICAgICAgICByZXR1cm4gbmV3IFJlcXVlc3RIYW5kbGVyKHRoaXMucngpLnN1YnNjcmliZVJlcXVlc3QodGhpcy5odHRwKHRoaXMucmVxKSwgb25TdWNjZXNzLCBvbkVycm9yKTtcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgIH07XHJcbiAgICB9XHJcblxyXG59XHJcblxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDMvNy8yMDE3LlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFVzZXJQcmVmZXJlbmNlc1NlcnZpY2Uge1xyXG5cclxuICAgIGNvbnN0cnVjdG9yKCRsb2csIHJlc3RTZXJ2aWNlSGFuZGxlcikge1xyXG4gICAgICAgIHRoaXMubG9nID0gJGxvZztcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlID0gcmVzdFNlcnZpY2VIYW5kbGVyO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdVc2VyUHJlZmVyZW5jZXNTZXJ2aWNlIEluc3RhbmNlZCcpO1xyXG5cclxuICAgICAgICB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbiA9IHtcclxuICAgICAgICAgICAgcHJlZmVyZW5jZXM6IHt9XHJcbiAgICAgICAgfVxyXG4gICAgfVxyXG5cclxuICAgIGdldFRpbWVab25lQ29uZmlndXJhdGlvbihvblN1Y2Nlc3MpIHtcclxuICAgICAgICB0aGlzLnJlc3RTZXJ2aWNlLmNvbW1vblNlcnZpY2VIYW5kbGVyKCkuZ2V0VGltZVpvbmVDb25maWd1cmF0aW9uKChkYXRhKSA9PiB7XHJcbiAgICAgICAgICAgIHRoaXMudGltZVpvbmVDb25maWd1cmF0aW9uID0gZGF0YS5kYXRhO1xyXG4gICAgICAgICAgICByZXR1cm4gb25TdWNjZXNzKCk7XHJcbiAgICAgICAgfSk7XHJcbiAgICB9XHJcblxyXG4gICAgZ2V0Q29udmVydGVkRGF0ZUludG9UaW1lWm9uZShkYXRlU3RyaW5nKSB7XHJcbiAgICAgICAgdmFyIHRpbWVTdHJpbmcgPSBkYXRlU3RyaW5nO1xyXG4gICAgICAgIHZhciB1c2VyRFRGb3JtYXQgPSB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbi5wcmVmZXJlbmNlcy5DVVJSX0RUX0ZPUk1BVDtcclxuICAgICAgICB2YXIgdGltZVpvbmUgPSB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbi5wcmVmZXJlbmNlcy5DVVJSX1RaO1xyXG5cclxuICAgICAgICBpZihkYXRlU3RyaW5nICE9PSAnbnVsbCcgJiZcclxuICAgICAgICAgICAgZGF0ZVN0cmluZyAmJiBtb21lbnQoZGF0ZVN0cmluZykuaXNWYWxpZCgpICYmXHJcbiAgICAgICAgICAgICEodHlwZW9mIE51bWJlcihkYXRlU3RyaW5nLnRvU3RyaW5nKCkpID09PSAnbnVtYmVyJyAmJiAhaXNOYU4oZGF0ZVN0cmluZy50b1N0cmluZygpKSkpe1xyXG4gICAgICAgICAgICBpZiAodGltZVpvbmUgPT09IG51bGwpIHtcclxuICAgICAgICAgICAgICAgIHRpbWVab25lID0gJ0dNVCc7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgdmFyIGZvcm1hdCA9ICdNTS9ERC9ZWVlZJztcclxuICAgICAgICAgICAgaWYgKHVzZXJEVEZvcm1hdCA9PT0gJ0REL01NL1lZWVknKSB7XHJcbiAgICAgICAgICAgICAgICBmb3JtYXQgPSAnREQvTU0vWVlZWSc7XHJcbiAgICAgICAgICAgIH1cclxuICAgICAgICAgICAgdGltZVN0cmluZyA9IG1vbWVudChkYXRlU3RyaW5nKS50eih0aW1lWm9uZSkuZm9ybWF0KGZvcm1hdClcclxuICAgICAgICB9XHJcblxyXG4gICAgICAgIHJldHVybiB0aW1lU3RyaW5nICE9PSAnbnVsbCc/IHRpbWVTdHJpbmc6ICcnO1xyXG4gICAgfVxyXG5cclxuICAgIGdldENvbnZlcnRlZERhdGVUaW1lSW50b1RpbWVab25lKGRhdGVTdHJpbmcpIHtcclxuICAgICAgICB2YXIgdGltZVN0cmluZyA9IGRhdGVTdHJpbmc7XHJcbiAgICAgICAgdmFyIHVzZXJEVEZvcm1hdCA9IHRoaXMudGltZVpvbmVDb25maWd1cmF0aW9uLnByZWZlcmVuY2VzLkNVUlJfRFRfRk9STUFUO1xyXG4gICAgICAgIHZhciB0aW1lWm9uZSA9IHRoaXMudGltZVpvbmVDb25maWd1cmF0aW9uLnByZWZlcmVuY2VzLkNVUlJfVFo7XHJcblxyXG4gICAgICAgIGlmKGRhdGVTdHJpbmcgIT09ICdudWxsJyAmJlxyXG4gICAgICAgICAgICBkYXRlU3RyaW5nICYmIG1vbWVudChkYXRlU3RyaW5nKS5pc1ZhbGlkKCkgJiZcclxuICAgICAgICAgICAgISh0eXBlb2YgTnVtYmVyKGRhdGVTdHJpbmcudG9TdHJpbmcoKSkgPT09ICdudW1iZXInICYmICFpc05hTihkYXRlU3RyaW5nLnRvU3RyaW5nKCkpKSl7XHJcbiAgICAgICAgICAgIGlmICh0aW1lWm9uZSA9PT0gbnVsbCkge1xyXG4gICAgICAgICAgICAgICAgdGltZVpvbmUgPSAnR01UJztcclxuICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICB2YXIgZm9ybWF0ID0gJ01NL0REL1lZWVkgaGg6bW0gYSdcclxuICAgICAgICAgICAgaWYgKHVzZXJEVEZvcm1hdCA9PT0gJ0REL01NL1lZWVknKSB7XHJcbiAgICAgICAgICAgICAgICBmb3JtYXQgPSAnREQvTU0vWVlZWSBoaDptbSBhJ1xyXG4gICAgICAgICAgICB9XHJcbiAgICAgICAgICAgIHRpbWVTdHJpbmcgPSBtb21lbnQoZGF0ZVN0cmluZykudHoodGltZVpvbmUpLmZvcm1hdChmb3JtYXQpXHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICByZXR1cm4gdGltZVN0cmluZyAhPT0gJ251bGwnPyB0aW1lU3RyaW5nOiAnJztcclxuICAgIH1cclxuXHJcbiAgICAvKipcclxuICAgICAqIEtlbmRvIERhdGUgRm9ybWF0IGlzIHF1aXRlIGRpZmZlcmVudCBhbmQgdGhyZWF0cyB0aGUgY2hhcnMgd2l0aCBhIGRpZmZlcmVudCBtZWFuaW5nXHJcbiAgICAgKiB0aGlzIEZ1bmN0aW9ucyByZXR1cm4gdGhlIG9uZSBpbiB0aGUgc3RhbmRhciBmb3JtYXQuXHJcbiAgICAgKiBDb25zaWRlciB0aGlzIGlzIG9ubHkgZnJvbSBvdXIgQ3VycmVudCBGb3JtYXRcclxuICAgICAqL1xyXG4gICAgZ2V0Q29udmVydGVkRGF0ZUZvcm1hdFRvS2VuZG9EYXRlKCkge1xyXG4gICAgICAgIHZhciB1c2VyRFRGb3JtYXQgPSB0aGlzLnRpbWVab25lQ29uZmlndXJhdGlvbi5wcmVmZXJlbmNlcy5DVVJSX0RUX0ZPUk1BVDtcclxuXHJcbiAgICAgICAgdmFyIGZvcm1hdCA9ICdNTS9kZC95eXl5JztcclxuICAgICAgICBpZiAodXNlckRURm9ybWF0ICE9PSBudWxsKSB7XHJcbiAgICAgICAgICAgIGZvcm1hdCA9IGZvcm1hdC5yZXBsYWNlKCdERCcsICdkZCcpO1xyXG4gICAgICAgICAgICBmb3JtYXQgPSBmb3JtYXQucmVwbGFjZSgnWVlZWScsICd5eXl5Jyk7XHJcbiAgICAgICAgfVxyXG5cclxuICAgICAgICByZXR1cm4gZm9ybWF0O1xyXG4gICAgfVxyXG5cclxufSIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICogRVM2IEludGVyY2VwdG9yIGNhbGxzIGlubmVyIG1ldGhvZHMgaW4gYSBnbG9iYWwgc2NvcGUsIHRoZW4gdGhlIFwidGhpc1wiIGlzIGJlaW5nIGxvc3RcclxuICogaW4gdGhlIGRlZmluaXRpb24gb2YgdGhlIENsYXNzIGZvciBpbnRlcmNlcHRvcnMgb25seVxyXG4gKiBUaGlzIGlzIGEgaW50ZXJmYWNlIHRoYXQgdGFrZSBjYXJlIG9mIHRoZSBpc3N1ZS5cclxuICovXHJcblxyXG5cclxuZXhwb3J0IGRlZmF1bHQgLyogaW50ZXJmYWNlKi8gY2xhc3MgSHR0cEludGVyY2VwdG9yIHtcclxuICAgIGNvbnN0cnVjdG9yKG1ldGhvZFRvQmluZCkge1xyXG4gICAgICAgIC8vIElmIG5vdCBtZXRob2QgdG8gYmluZCwgd2UgYXNzdW1lIG91ciBpbnRlcmNlcHRvciBpcyB1c2luZyBhbGwgdGhlIGlubmVyIGZ1bmN0aW9uc1xyXG4gICAgICAgIGlmKCFtZXRob2RUb0JpbmQpIHtcclxuICAgICAgICAgICAgWydyZXF1ZXN0JywgJ3JlcXVlc3RFcnJvcicsICdyZXNwb25zZScsICdyZXNwb25zZUVycm9yJ11cclxuICAgICAgICAgICAgICAgIC5mb3JFYWNoKChtZXRob2QpID0+IHtcclxuICAgICAgICAgICAgICAgICAgICBpZih0aGlzW21ldGhvZF0pIHtcclxuICAgICAgICAgICAgICAgICAgICAgICAgdGhpc1ttZXRob2RdID0gdGhpc1ttZXRob2RdLmJpbmQodGhpcyk7XHJcbiAgICAgICAgICAgICAgICAgICAgfVxyXG4gICAgICAgICAgICAgICAgfSk7XHJcbiAgICAgICAgfSBlbHNlIHtcclxuICAgICAgICAgICAgLy8gbWV0aG9kVG9CaW5kIHJlZmVyZW5jZSB0byBhIHNpbmdsZSBjaGlsZCBjbGFzc1xyXG4gICAgICAgICAgICB0aGlzW21ldGhvZFRvQmluZF0gPSB0aGlzW21ldGhvZFRvQmluZF0uYmluZCh0aGlzKTtcclxuICAgICAgICB9XHJcblxyXG4gICAgfVxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMjIvMjAxNS5cclxuICogVXNlIHRoaXMgbW9kdWxlIHRvIG1vZGlmeSBhbnl0aGluZyByZWxhdGVkIHRvIHRoZSBIZWFkZXJzIGFuZCBSZXF1ZXN0XHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IGFuZ3VsYXIgIGZyb20gJ2FuZ3VsYXInO1xyXG5pbXBvcnQgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3IgZnJvbSAnLi9IVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yIGZyb20gJy4vSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcbmltcG9ydCBIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yLmpzJztcclxuaW1wb3J0IEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvciBmcm9tICcuL0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvci5qcyc7XHJcblxyXG5cclxudmFyIEhUVFBNb2R1bGUgPSBhbmd1bGFyLm1vZHVsZSgnVERTVE0uSFRUUE1vZHVsZScsIFsnbmdSZXNvdXJjZSddKS5jb25maWcoWyckaHR0cFByb3ZpZGVyJywgZnVuY3Rpb24oJGh0dHBQcm92aWRlcil7XHJcblxyXG4gICAgLy9pbml0aWFsaXplIGdldCBpZiBub3QgdGhlcmVcclxuICAgIGlmICghJGh0dHBQcm92aWRlci5kZWZhdWx0cy5oZWFkZXJzLmdldCkge1xyXG4gICAgICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXQgPSB7fTtcclxuICAgIH1cclxuXHJcbiAgICAvL0Rpc2FibGUgSUUgYWpheCByZXF1ZXN0IGNhY2hpbmdcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ0lmLU1vZGlmaWVkLVNpbmNlJ10gPSAnTW9uLCAyNiBKdWwgMTk5NyAwNTowMDowMCBHTVQnO1xyXG4gICAgLy8gZXh0cmFcclxuICAgICRodHRwUHJvdmlkZXIuZGVmYXVsdHMuaGVhZGVycy5nZXRbJ0NhY2hlLUNvbnRyb2wnXSA9ICduby1jYWNoZSc7XHJcbiAgICAkaHR0cFByb3ZpZGVyLmRlZmF1bHRzLmhlYWRlcnMuZ2V0WydQcmFnbWEnXSA9ICduby1jYWNoZSc7XHJcblxyXG5cclxuICAgIC8vIEluamVjdHMgb3VyIEludGVyY2VwdG9ycyBmb3IgUmVxdWVzdFxyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXF1ZXN0RXJyb3JIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuICAgIC8vIEluamVjdHMgb3VyIEludGVyY2VwdG9ycyBmb3IgUmVzcG9uc2VcclxuICAgICRodHRwUHJvdmlkZXIuaW50ZXJjZXB0b3JzLnB1c2goJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicpO1xyXG4gICAgJGh0dHBQcm92aWRlci5pbnRlcmNlcHRvcnMucHVzaCgnSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3InKTtcclxuXHJcblxyXG59XSk7XHJcblxyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXF1ZXN0SGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlcXVlc3RIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuSFRUUE1vZHVsZS5zZXJ2aWNlKCdIVFRQUmVxdWVzdEVycm9ySGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcicsIFsnJGxvZycsICckcScsICdyeCcsIEhUVFBSZXNwb25zZUhhbmRsZXJJbnRlcmNlcHRvcl0pO1xyXG5IVFRQTW9kdWxlLnNlcnZpY2UoJ0hUVFBSZXNwb25zZUVycm9ySGFuZGxlckludGVyY2VwdG9yJywgWyckbG9nJywgJyRxJywgJ3J4JywgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3JdKTtcclxuXHJcbmV4cG9ydCBkZWZhdWx0IEhUVFBNb2R1bGU7IiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSXQgaW1wbGVtZW50IGFuIGFic3RyYWN0IGNhbGwgdG8gSFRUUCBJbnRlcmNlcHRvcnMgdG8gbWFuYWdlIGVycm9yIGhhbmRsZXJcclxuICogU29tZXRpbWVzIGEgcmVxdWVzdCBjYW4ndCBiZSBzZW50IG9yIGl0IGlzIHJlamVjdGVkIGJ5IGFuIGludGVyY2VwdG9yLlxyXG4gKiBSZXF1ZXN0IGVycm9yIGludGVyY2VwdG9yIGNhcHR1cmVzIHJlcXVlc3RzIHRoYXQgaGF2ZSBiZWVuIGNhbmNlbGVkIGJ5IGEgcHJldmlvdXMgcmVxdWVzdCBpbnRlcmNlcHRvci5cclxuICogSXQgY2FuIGJlIHVzZWQgaW4gb3JkZXIgdG8gcmVjb3ZlciB0aGUgcmVxdWVzdCBhbmQgc29tZXRpbWVzIHVuZG8gdGhpbmdzIHRoYXQgaGF2ZSBiZWVuIHNldCB1cCBiZWZvcmUgYSByZXF1ZXN0LFxyXG4gKiBsaWtlIHJlbW92aW5nIG92ZXJsYXlzIGFuZCBsb2FkaW5nIGluZGljYXRvcnMsIGVuYWJsaW5nIGJ1dHRvbnMgYW5kIGZpZWxkcyBhbmQgc28gb24uXHJcbiAqL1xyXG5cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcbiAgICBjb25zdHJ1Y3RvcigkbG9nLCAkcSwgcngpIHtcclxuICAgICAgICBzdXBlcigncmVxdWVzdEVycm9yJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlcXVlc3RFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXF1ZXN0RXJyb3IocmVqZWN0aW9uKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIGVycm9yXHJcbiAgICAgICAgLy9pZiAoY2FuUmVjb3ZlcihyZWplY3Rpb24pKSB7XHJcbiAgICAgICAgLy8gICAgcmV0dXJuIHJlc3BvbnNlT3JOZXdQcm9taXNlXHJcbiAgICAgICAgLy99XHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVqZWN0aW9uKTtcclxuXHJcbiAgICAgICAgcmV0dXJuIHRoaXMucS5yZWplY3QocmVqZWN0aW9uKTtcclxuICAgIH1cclxuXHJcbiAgICBsaXN0ZW5FcnJvcigpIHtcclxuICAgICAgICByZXR1cm4gdGhpcy5kZWZlci5wcm9taXNlO1xyXG4gICAgfVxyXG5cclxufVxyXG4iLCIvKipcclxuICogQ3JlYXRlZCBieSBKb3JnZSBNb3JheXRhIG9uIDEyLzExLzE1LlxyXG4gKiBJdCBpbXBsZW1lbnQgYW4gYWJzdHJhY3QgY2FsbCB0byBIVFRQIEludGVyY2VwdG9ycyB0byBtYW5hZ2Ugb25seSByZXF1ZXN0XHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBleHRlbmRzIC8qaW1wbGVtZW50cyovIEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSB7XHJcblxyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3JlcXVlc3QnKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVxdWVzdEhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXF1ZXN0KGNvbmZpZykge1xyXG4gICAgICAgIC8vIFdlIGNhbiBhZGQgaGVhZGVycyBpZiBvbiB0aGUgaW5jb21pbmcgcmVxdWVzdCBtYWRlIGl0IHdlIGhhdmUgdGhlIHRva2VuIGluc2lkZVxyXG4gICAgICAgIC8vIGRlZmluZWQgYnkgc29tZSBjb25kaXRpb25zXHJcbiAgICAgICAgLy9jb25maWcuaGVhZGVyc1sneC1zZXNzaW9uLXRva2VuJ10gPSBteS50b2tlbjtcclxuXHJcbiAgICAgICAgY29uZmlnLnJlcXVlc3RUaW1lc3RhbXAgPSBuZXcgRGF0ZSgpLmdldFRpbWUoKTtcclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkoY29uZmlnKTtcclxuXHJcbiAgICAgICAgcmV0dXJuIGNvbmZpZyB8fCB0aGlzLnEud2hlbihjb25maWcpO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlblJlcXVlc3QoKSB7XHJcbiAgICAgICAgcmV0dXJuIHRoaXMuZGVmZXIucHJvbWlzZTtcclxuICAgIH1cclxuXHJcbn1cclxuIiwiLyoqXHJcbiAqIENyZWF0ZWQgYnkgSm9yZ2UgTW9yYXl0YSBvbiAxMi8xMS8xNS5cclxuICogSWYgYmFja2VuZCBjYWxsIGZhaWxzIG9yIGl0IG1pZ2h0IGJlIHJlamVjdGVkIGJ5IGEgcmVxdWVzdCBpbnRlcmNlcHRvciBvciBieSBhIHByZXZpb3VzIHJlc3BvbnNlIGludGVyY2VwdG9yO1xyXG4gKiBJbiB0aG9zZSBjYXNlcywgcmVzcG9uc2UgZXJyb3IgaW50ZXJjZXB0b3IgY2FuIGhlbHAgdXMgdG8gcmVjb3ZlciB0aGUgYmFja2VuZCBjYWxsLlxyXG4gKi9cclxuXHJcbid1c2Ugc3RyaWN0JztcclxuXHJcbmltcG9ydCBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UgZnJvbSAnLi9IVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2UuanMnO1xyXG5cclxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSFRUUFJlc3BvbnNlRXJyb3JIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3Jlc3BvbnNlRXJyb3InKTtcclxuICAgICAgICB0aGlzLmxvZyA9ICRsb2c7XHJcbiAgICAgICAgdGhpcy5xID0gJHE7XHJcbiAgICAgICAgdGhpcy5kZWZlciA9IHRoaXMucS5kZWZlcigpO1xyXG4gICAgICAgIHRoaXMubG9nLmRlYnVnKCdIVFRQUmVzcG9uc2VFcnJvckhhbmRsZXJJbnRlcmNlcHRvciBpbnN0YW5jZWQnKTtcclxuICAgIH1cclxuXHJcbiAgICByZXNwb25zZUVycm9yKHJlamVjdGlvbikge1xyXG4gICAgICAgIC8vIGRvIHNvbWV0aGluZyBvbiBlcnJvclxyXG4gICAgICAgIC8vaWYgKGNhblJlY292ZXIocmVqZWN0aW9uKSkge1xyXG4gICAgICAgIC8vICAgIHJldHVybiByZXNwb25zZU9yTmV3UHJvbWlzZVxyXG4gICAgICAgIC8vIH1cclxuXHJcbiAgICAgICAgdGhpcy5kZWZlci5ub3RpZnkocmVqZWN0aW9uKTtcclxuICAgICAgICByZXR1cm4gdGhpcy5xLnJlamVjdChyZWplY3Rpb24pO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlbkVycm9yKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcblxyXG59XHJcbiIsIi8qKlxyXG4gKiBDcmVhdGVkIGJ5IEpvcmdlIE1vcmF5dGEgb24gMTIvMTEvMTUuXHJcbiAqIFRoaXMgbWV0aG9kIGlzIGNhbGxlZCByaWdodCBhZnRlciAkaHR0cCByZWNlaXZlcyB0aGUgcmVzcG9uc2UgZnJvbSB0aGUgYmFja2VuZCxcclxuICogc28geW91IGNhbiBtb2RpZnkgdGhlIHJlc3BvbnNlIGFuZCBtYWtlIG90aGVyIGFjdGlvbnMuIFRoaXMgZnVuY3Rpb24gcmVjZWl2ZXMgYSByZXNwb25zZSBvYmplY3QgYXMgYSBwYXJhbWV0ZXJcclxuICogYW5kIGhhcyB0byByZXR1cm4gYSByZXNwb25zZSBvYmplY3Qgb3IgYSBwcm9taXNlLiBUaGUgcmVzcG9uc2Ugb2JqZWN0IGluY2x1ZGVzXHJcbiAqIHRoZSByZXF1ZXN0IGNvbmZpZ3VyYXRpb24sIGhlYWRlcnMsIHN0YXR1cyBhbmQgZGF0YSB0aGF0IHJldHVybmVkIGZyb20gdGhlIGJhY2tlbmQuXHJcbiAqIFJldHVybmluZyBhbiBpbnZhbGlkIHJlc3BvbnNlIG9iamVjdCBvciBwcm9taXNlIHRoYXQgd2lsbCBiZSByZWplY3RlZCwgd2lsbCBtYWtlIHRoZSAkaHR0cCBjYWxsIHRvIGZhaWwuXHJcbiAqL1xyXG5cclxuJ3VzZSBzdHJpY3QnO1xyXG5cclxuaW1wb3J0IEhUVFBJbnRlcmNlcHRvckludGVyZmFjZSBmcm9tICcuL0hUVFBJbnRlcmNlcHRvckludGVyZmFjZS5qcyc7XHJcblxyXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBIVFRQUmVzcG9uc2VIYW5kbGVySW50ZXJjZXB0b3IgZXh0ZW5kcyAvKmltcGxlbWVudHMqLyBIVFRQSW50ZXJjZXB0b3JJbnRlcmZhY2Uge1xyXG4gICAgY29uc3RydWN0b3IoJGxvZywgJHEsIHJ4KSB7XHJcbiAgICAgICAgc3VwZXIoJ3Jlc3BvbnNlJyk7XHJcbiAgICAgICAgdGhpcy5sb2cgPSAkbG9nO1xyXG4gICAgICAgIHRoaXMucSA9ICRxO1xyXG4gICAgICAgIHRoaXMuZGVmZXIgPSB0aGlzLnEuZGVmZXIoKTtcclxuICAgICAgICB0aGlzLmxvZy5kZWJ1ZygnSFRUUFJlc3BvbnNlSGFuZGxlckludGVyY2VwdG9yIGluc3RhbmNlZCcpO1xyXG4gICAgfVxyXG5cclxuICAgIHJlc3BvbnNlKHJlc3BvbnNlKSB7XHJcbiAgICAgICAgLy8gZG8gc29tZXRoaW5nIG9uIHN1Y2Nlc3NcclxuXHJcbiAgICAgICAgcmVzcG9uc2UuY29uZmlnLnJlc3BvbnNlVGltZXN0YW1wID0gbmV3IERhdGUoKS5nZXRUaW1lKCk7XHJcblxyXG4gICAgICAgIHRoaXMuZGVmZXIubm90aWZ5KHJlc3BvbnNlKTtcclxuICAgICAgICByZXR1cm4gcmVzcG9uc2UgfHwgdGhpcy5xLndoZW4ocmVzcG9uc2UpO1xyXG4gICAgfVxyXG5cclxuICAgIGxpc3RlblJlc3BvbnNlKCkge1xyXG4gICAgICAgIHJldHVybiB0aGlzLmRlZmVyLnByb21pc2U7XHJcbiAgICB9XHJcbn1cclxuXHJcbiJdfQ==
