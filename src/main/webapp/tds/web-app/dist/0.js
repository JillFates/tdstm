(window["webpackJsonp"] = window["webpackJsonp"] || []).push([[0],{

/***/ "./web-app/app-js/modules/apiAction/model/agent.model.ts":
/*!***************************************************************!*\
  !*** ./web-app/app-js/modules/apiAction/model/agent.model.ts ***!
  \***************************************************************/
/*! exports provided: DictionaryModel, CredentialModel, AgentMethodModel */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "DictionaryModel", function() { return DictionaryModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CredentialModel", function() { return CredentialModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AgentMethodModel", function() { return AgentMethodModel; });
/* harmony import */ var _shared_model_constants__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../../shared/model/constants */ "./web-app/app-js/shared/model/constants.ts");

var DictionaryModel = /** @class */ (function () {
    function DictionaryModel() {
    }
    return DictionaryModel;
}());

var CredentialModel = /** @class */ (function () {
    function CredentialModel() {
    }
    return CredentialModel;
}());

var AgentMethodModel = /** @class */ (function () {
    function AgentMethodModel() {
        this.name = '';
        this.description = '';
        this.isPolling = false;
        this.producesData = false;
        this.endpointUrl = '';
        this.polling = {
            frequency: {
                value: 0,
                interval: _shared_model_constants__WEBPACK_IMPORTED_MODULE_0__["INTERVAL"].SECONDS
            },
            lapsedAfter: {
                value: 0,
                interval: _shared_model_constants__WEBPACK_IMPORTED_MODULE_0__["INTERVAL"].MINUTES
            },
            stalledAfter: {
                value: 0,
                interval: _shared_model_constants__WEBPACK_IMPORTED_MODULE_0__["INTERVAL"].MINUTES
            }
        };
        this.methodParams = [];
    }
    return AgentMethodModel;
}());



/***/ }),

/***/ "./web-app/app-js/modules/apiAction/model/api-action.model.ts":
/*!********************************************************************!*\
  !*** ./web-app/app-js/modules/apiAction/model/api-action.model.ts ***!
  \********************************************************************/
/*! exports provided: APIActionColumnModel, APIActionParameterColumnModel, EVENT_STATUS_TEXT, EVENT_SUCCESS_TEXT, EVENT_DEFAULT_TEXT, EVENT_BEFORE_CALL_TEXT, APIActionModel, EventReaction, APIActionParameterModel, EventReactionType */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "APIActionColumnModel", function() { return APIActionColumnModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "APIActionParameterColumnModel", function() { return APIActionParameterColumnModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "EVENT_STATUS_TEXT", function() { return EVENT_STATUS_TEXT; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "EVENT_SUCCESS_TEXT", function() { return EVENT_SUCCESS_TEXT; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "EVENT_DEFAULT_TEXT", function() { return EVENT_DEFAULT_TEXT; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "EVENT_BEFORE_CALL_TEXT", function() { return EVENT_BEFORE_CALL_TEXT; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "APIActionModel", function() { return APIActionModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "EventReaction", function() { return EventReaction; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "APIActionParameterModel", function() { return APIActionParameterModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "EventReactionType", function() { return EventReactionType; });
/* harmony import */ var _shared_model_constants__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ../../../shared/model/constants */ "./web-app/app-js/shared/model/constants.ts");
/* harmony import */ var _shared_components_check_action_model_check_action_model__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../../../shared/components/check-action/model/check-action.model */ "./web-app/app-js/shared/components/check-action/model/check-action.model.ts");
/* harmony import */ var _agent_model__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./agent.model */ "./web-app/app-js/modules/apiAction/model/agent.model.ts");



var APIActionColumnModel = /** @class */ (function () {
    function APIActionColumnModel(dateFormat) {
        this.columns = [
            {
                label: 'Action',
                property: 'action',
                type: 'action',
                width: 106,
                locked: true
            }, {
                label: 'Name',
                property: 'name',
                type: 'text',
                width: 150,
                locked: true
            }, {
                label: 'Provider',
                property: 'provider.name',
                type: 'text',
                width: 180
            }, {
                label: 'Description',
                property: 'description',
                type: 'text',
                width: 300
            }, {
                label: 'Method',
                property: 'agentMethod.name',
                type: 'text',
                width: 125
            }, {
                label: 'Data',
                property: 'producesData',
                type: 'boolean',
                width: 90
            }, {
                label: 'Default DataScript',
                property: 'defaultDataScript.name',
                type: 'text',
                width: 180
            }, {
                label: 'Created',
                property: 'dateCreated',
                type: 'date',
                format: dateFormat,
                width: 160
            }, {
                label: 'Last Updated',
                property: 'lastUpdated',
                type: 'date',
                format: dateFormat,
                width: 160
            }
        ];
    }
    return APIActionColumnModel;
}());

var APIActionParameterColumnModel = /** @class */ (function () {
    function APIActionParameterColumnModel() {
        this.columns = [
            {
                label: 'Action',
                property: 'action',
                type: 'action',
                width: 64,
                locked: true
            },
            {
                label: 'custom',
                property: 'required',
                type: 'boolean',
                width: 42
            },
            {
                label: 'custom',
                property: 'readonly',
                type: 'boolean',
                width: 42
            },
            {
                label: 'Name',
                property: 'paramName',
                type: 'text',
                width: 198
            }, {
                label: 'Context',
                property: 'context.value',
                type: 'text',
                width: 140
            }, {
                label: 'Value',
                property: 'value',
                type: 'text',
                width: 218
            },
            {
                label: 'custom',
                property: 'encoded',
                type: 'boolean',
                width: 42
            },
            {
                label: 'Description',
                property: 'desc',
                type: 'text',
                width: 268
            }
        ];
    }
    return APIActionParameterColumnModel;
}());

var EVENT_STATUS_TEXT = '// Check the HTTP response code for a 200 OK \n if (response.status == SC.OK) { \n \t return SUCCESS \n } else { \n \t return ERROR \n}';
var EVENT_SUCCESS_TEXT = '// Update the task status that the task completed\n task.done()';
var EVENT_DEFAULT_TEXT = '// Put the task on hold and add a comment with the cause of the error\n task.error( response.error )';
var EVENT_BEFORE_CALL_TEXT = "// Setting Content Type, default 'application/json'\n// request.config.setProperty('Content-Type', 'text/csv')\n\n// Setting content type Accepted, default 'application/json'\n// request.config.setProperty('Accept', 'application/xml;q=0.9')";
var APIActionModel = /** @class */ (function () {
    function APIActionModel() {
        this.name = '';
        this.description = '';
        this.provider = { id: null, name: '' };
        this.dictionary = { id: null, name: '' };
        this.agentMethod = new _agent_model__WEBPACK_IMPORTED_MODULE_2__["AgentMethodModel"]();
        this.defaultDataScript = { id: null, name: '' };
        this.isPolling = false;
        this.producesData = false;
        this.endpointUrl = '';
        this.docUrl = '';
        this.polling = {
            frequency: {
                value: 0,
                interval: _shared_model_constants__WEBPACK_IMPORTED_MODULE_0__["INTERVAL"].SECONDS
            },
            lapsedAfter: {
                value: 0,
                interval: _shared_model_constants__WEBPACK_IMPORTED_MODULE_0__["INTERVAL"].MINUTES
            },
            stalledAfter: {
                value: 0,
                interval: _shared_model_constants__WEBPACK_IMPORTED_MODULE_0__["INTERVAL"].MINUTES
            }
        };
        APIActionModel.createBasicReactions(this);
    }
    APIActionModel.createBasicReactions = function (apiActionModel) {
        apiActionModel.eventReactions = [];
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STATUS, true, EVENT_STATUS_TEXT));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.SUCCESS, true, EVENT_SUCCESS_TEXT));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.DEFAULT, true, EVENT_DEFAULT_TEXT));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.ERROR, false, ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FAILED, false, ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.LAPSED, false, ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STALLED, false, ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.PRE, false, ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FINAL, false, ''));
    };
    /**
     * TODO: I think this can be removed.
     * @deprecated look at the api-action-view-edit.component.ts -> loadReactionScripts
     * @param {APIActionModel} apiActionModel
     * @param {string} reactionScriptsStringModel
     */
    APIActionModel.createReactions = function (apiActionModel, reactionScriptsStringModel) {
        var reactions = JSON.parse(reactionScriptsStringModel);
        apiActionModel.eventReactions = [];
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STATUS, reactions['STATUS'] ? true : false, reactions['STATUS'] ? reactions['STATUS'] : ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.SUCCESS, reactions['SUCCESS'] ? true : false, reactions['SUCCESS'] ? reactions['SUCCESS'] : ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.DEFAULT, reactions['DEFAULT'] ? true : false, reactions['DEFAULT'] ? reactions['DEFAULT'] : ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.ERROR, reactions['ERROR'] ? true : false, reactions['ERROR'] ? reactions['ERROR'] : ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FAILED, reactions['FAILED'] ? true : false, reactions['FAILED'] ? reactions['FAILED'] : ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.LAPSED, reactions['LAPSED'] ? true : false, reactions['LAPSED'] ? reactions['LAPSED'] : ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.STALLED, reactions['STALLED'] ? true : false, reactions['STALLED'] ? reactions['STALLED'] : ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.PRE, reactions['PRE'] ? true : false, reactions['PRE'] ? reactions['PRE'] : ''));
        apiActionModel.eventReactions.push(new EventReaction(EventReactionType.FINAL, reactions['FINAL'] ? true : false, reactions['FINAL'] ? reactions['FINAL'] : ''));
    };
    return APIActionModel;
}());

var EventReaction = /** @class */ (function () {
    function EventReaction(type, selected, value) {
        this.type = type;
        this.selected = selected;
        this.value = value;
        this.open = true;
        this.state = _shared_components_check_action_model_check_action_model__WEBPACK_IMPORTED_MODULE_1__["CHECK_ACTION"].UNKNOWN;
        this.error = '';
    }
    return EventReaction;
}());

var APIActionParameterModel = /** @class */ (function () {
    function APIActionParameterModel() {
    }
    return APIActionParameterModel;
}());

var EventReactionType;
(function (EventReactionType) {
    EventReactionType["STATUS"] = "STATUS";
    EventReactionType["SUCCESS"] = "SUCCESS";
    EventReactionType["DEFAULT"] = "DEFAULT";
    EventReactionType["ERROR"] = "ERROR";
    EventReactionType["FAILED"] = "FAILED";
    EventReactionType["LAPSED"] = "LAPSED";
    EventReactionType["STALLED"] = "STALLED";
    EventReactionType["PRE"] = "PRE";
    EventReactionType["FINAL"] = "FINAL";
})(EventReactionType || (EventReactionType = {}));
;


/***/ }),

/***/ "./web-app/app-js/modules/credential/model/credential.model.ts":
/*!*********************************************************************!*\
  !*** ./web-app/app-js/modules/credential/model/credential.model.ts ***!
  \*********************************************************************/
/*! exports provided: CredentialColumnModel, CredentialModel, REQUEST_MODE, AUTH_METHODS, ENVIRONMENT, HTTP_METHOD, CREDENTIAL_STATUS */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CredentialColumnModel", function() { return CredentialColumnModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CredentialModel", function() { return CredentialModel; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "REQUEST_MODE", function() { return REQUEST_MODE; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AUTH_METHODS", function() { return AUTH_METHODS; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ENVIRONMENT", function() { return ENVIRONMENT; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "HTTP_METHOD", function() { return HTTP_METHOD; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CREDENTIAL_STATUS", function() { return CREDENTIAL_STATUS; });
var CredentialColumnModel = /** @class */ (function () {
    function CredentialColumnModel(dateFormat) {
        this.columns = [
            {
                label: 'Action',
                property: 'action',
                type: 'action',
                width: 108,
                locked: true
            }, {
                label: 'Name',
                property: 'name',
                type: 'text',
                locked: true
            }, {
                label: 'Description',
                property: 'description',
                type: 'text'
            }, {
                label: 'Provider',
                property: 'provider.name',
                type: 'text',
                width: 200
            }, {
                label: 'Environment',
                property: 'environment',
                type: 'text',
                width: 200
            }, {
                label: 'Status',
                property: 'status',
                type: 'text',
                width: 100
            }, {
                label: 'Auth Method',
                property: 'authMethod',
                type: 'text',
                width: 200
            }, {
                label: 'Created',
                property: 'dateCreated',
                type: 'date',
                format: dateFormat,
                width: 170
            }
        ];
    }
    return CredentialColumnModel;
}());

var CredentialModel = /** @class */ (function () {
    function CredentialModel() {
        this.name = '';
        this.description = '';
        this.provider = { id: null, name: '' };
        this.environment = '';
        this.status = '';
        this.authMethod = '';
        this.username = '';
        this.password = '';
        this.authenticationUrl = '';
        this.renewTokenUrl = '';
        this.terminateUrl = '';
        this.httpMethod = '';
        this.sessionName = '';
        this.validationExpression = '';
    }
    return CredentialModel;
}());

var REQUEST_MODE;
(function (REQUEST_MODE) {
    REQUEST_MODE["BASIC_AUTH"] = "BASIC_AUTH";
    REQUEST_MODE["FORM_VARS"] = "FORM_VARS";
})(REQUEST_MODE || (REQUEST_MODE = {}));
var AUTH_METHODS;
(function (AUTH_METHODS) {
    AUTH_METHODS["BASIC_AUTH"] = "Basic Auth";
    AUTH_METHODS["COOKIE"] = "Cookie Session";
    AUTH_METHODS["HEADER"] = "Header Session";
    AUTH_METHODS["JWT"] = "JSON Web Tokens";
})(AUTH_METHODS || (AUTH_METHODS = {}));
var ENVIRONMENT;
(function (ENVIRONMENT) {
    ENVIRONMENT["DEVELOPMENT"] = "Development";
    ENVIRONMENT["OTHER"] = "Other";
    ENVIRONMENT["PRODUCTION"] = "Production";
    ENVIRONMENT["SANDBOX"] = "Sandbox";
})(ENVIRONMENT || (ENVIRONMENT = {}));
var HTTP_METHOD;
(function (HTTP_METHOD) {
    HTTP_METHOD["POST"] = "POST";
    HTTP_METHOD["GET"] = "GET";
    HTTP_METHOD["PUT"] = "PUT";
})(HTTP_METHOD || (HTTP_METHOD = {}));
var CREDENTIAL_STATUS;
(function (CREDENTIAL_STATUS) {
    CREDENTIAL_STATUS["ACTIVE"] = "Active";
    CREDENTIAL_STATUS["INACTIVE"] = "Inactive";
})(CREDENTIAL_STATUS || (CREDENTIAL_STATUS = {}));


/***/ })

}]);
//# sourceMappingURL=0.js.map