(window["webpackJsonp"] = window["webpackJsonp"] || []).push([[3],{

/***/ "./web-app/app-js/modules/security/components/error-page/error-page.component.ts":
/*!***************************************************************************************!*\
  !*** ./web-app/app-js/modules/security/components/error-page/error-page.component.ts ***!
  \***************************************************************************************/
/*! exports provided: ErrorPageComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "ErrorPageComponent", function() { return ErrorPageComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");


var ErrorPageComponent = /** @class */ (function () {
    function ErrorPageComponent() {
    }
    ErrorPageComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'error-page',
            templateUrl: '../tds/web-app/app-js/modules/security/components/error-page/error-page.component.html',
        })
    ], ErrorPageComponent);
    return ErrorPageComponent;
}());



/***/ }),

/***/ "./web-app/app-js/modules/security/components/not-found-page/not-found-page.component.ts":
/*!***********************************************************************************************!*\
  !*** ./web-app/app-js/modules/security/components/not-found-page/not-found-page.component.ts ***!
  \***********************************************************************************************/
/*! exports provided: NotFoundPageComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "NotFoundPageComponent", function() { return NotFoundPageComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");


var NotFoundPageComponent = /** @class */ (function () {
    function NotFoundPageComponent() {
    }
    NotFoundPageComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'not-found-page',
            templateUrl: '../tds/web-app/app-js/modules/security/components/not-found-page/not-found-page.component.html',
        })
    ], NotFoundPageComponent);
    return NotFoundPageComponent;
}());



/***/ }),

/***/ "./web-app/app-js/modules/security/components/unauthorized-page/unauthorized-page.component.ts":
/*!*****************************************************************************************************!*\
  !*** ./web-app/app-js/modules/security/components/unauthorized-page/unauthorized-page.component.ts ***!
  \*****************************************************************************************************/
/*! exports provided: UnauthorizedPageComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "UnauthorizedPageComponent", function() { return UnauthorizedPageComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");


var UnauthorizedPageComponent = /** @class */ (function () {
    function UnauthorizedPageComponent() {
    }
    UnauthorizedPageComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'unauthorized-page',
            templateUrl: '../tds/web-app/app-js/modules/security/components/unauthorized-page/unauthorized-page.component.html',
        })
    ], UnauthorizedPageComponent);
    return UnauthorizedPageComponent;
}());



/***/ }),

/***/ "./web-app/app-js/modules/security/security-route.module.ts":
/*!******************************************************************!*\
  !*** ./web-app/app-js/modules/security/security-route.module.ts ***!
  \******************************************************************/
/*! exports provided: SecurityRouteStates, SecurityRoute, SecurityRouteModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SecurityRouteStates", function() { return SecurityRouteStates; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SecurityRoute", function() { return SecurityRoute; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SecurityRouteModule", function() { return SecurityRouteModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/esm5/router.js");
/* harmony import */ var _components_error_page_error_page_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./components/error-page/error-page.component */ "./web-app/app-js/modules/security/components/error-page/error-page.component.ts");
/* harmony import */ var _components_unauthorized_page_unauthorized_page_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./components/unauthorized-page/unauthorized-page.component */ "./web-app/app-js/modules/security/components/unauthorized-page/unauthorized-page.component.ts");
/* harmony import */ var _components_not_found_page_not_found_page_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./components/not-found-page/not-found-page.component */ "./web-app/app-js/modules/security/components/not-found-page/not-found-page.component.ts");

// Angular


// Components



/**
 * Security Route States
 * @class
 * @classdesc To use externally to reference possible state of the Security Model
 */
var SecurityRouteStates = /** @class */ (function () {
    function SecurityRouteStates() {
    }
    SecurityRouteStates.PARENT = 'security';
    SecurityRouteStates.ERROR = {
        url: 'error'
    };
    SecurityRouteStates.UNAUTHORIZED = {
        url: 'unauthorized'
    };
    SecurityRouteStates.NOT_FOUND = {
        url: 'notfound'
    };
    return SecurityRouteStates;
}());

// routes
var SecurityRoute = [
    { path: '', pathMatch: 'full', redirectTo: SecurityRouteStates.NOT_FOUND.url },
    {
        path: SecurityRouteStates.NOT_FOUND.url,
        data: {
            page: {
                title: '', instruction: '', menu: []
            }
        },
        component: _components_not_found_page_not_found_page_component__WEBPACK_IMPORTED_MODULE_5__["NotFoundPageComponent"]
    },
    {
        path: SecurityRouteStates.ERROR.url,
        data: {
            page: {
                title: '', instruction: '', menu: []
            }
        },
        component: _components_error_page_error_page_component__WEBPACK_IMPORTED_MODULE_3__["ErrorPageComponent"]
    },
    {
        path: SecurityRouteStates.UNAUTHORIZED.url,
        data: {
            page: {
                title: '', instruction: '', menu: []
            }
        },
        component: _components_unauthorized_page_unauthorized_page_component__WEBPACK_IMPORTED_MODULE_4__["UnauthorizedPageComponent"]
    },
];
var SecurityRouteModule = /** @class */ (function () {
    function SecurityRouteModule() {
    }
    SecurityRouteModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
            exports: [_angular_router__WEBPACK_IMPORTED_MODULE_2__["RouterModule"]],
            imports: [_angular_router__WEBPACK_IMPORTED_MODULE_2__["RouterModule"].forChild(SecurityRoute)]
        })
    ], SecurityRouteModule);
    return SecurityRouteModule;
}());



/***/ }),

/***/ "./web-app/app-js/modules/security/security.module.ts":
/*!************************************************************!*\
  !*** ./web-app/app-js/modules/security/security.module.ts ***!
  \************************************************************/
/*! exports provided: SecurityModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SecurityModule", function() { return SecurityModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ "./node_modules/@angular/common/esm5/common.js");
/* harmony import */ var _angular_forms__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/forms */ "./node_modules/@angular/forms/esm5/forms.js");
/* harmony import */ var _shared_shared_module__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../shared/shared.module */ "./web-app/app-js/shared/shared.module.ts");
/* harmony import */ var _security_route_module__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./security-route.module */ "./web-app/app-js/modules/security/security-route.module.ts");
/* harmony import */ var _components_error_page_error_page_component__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ./components/error-page/error-page.component */ "./web-app/app-js/modules/security/components/error-page/error-page.component.ts");
/* harmony import */ var _components_unauthorized_page_unauthorized_page_component__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ./components/unauthorized-page/unauthorized-page.component */ "./web-app/app-js/modules/security/components/unauthorized-page/unauthorized-page.component.ts");
/* harmony import */ var _components_not_found_page_not_found_page_component__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ./components/not-found-page/not-found-page.component */ "./web-app/app-js/modules/security/components/not-found-page/not-found-page.component.ts");

// Angular



// Shared

// Route Module

// Components



var SecurityModule = /** @class */ (function () {
    function SecurityModule() {
    }
    SecurityModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
            imports: [
                // Angular
                _angular_common__WEBPACK_IMPORTED_MODULE_2__["CommonModule"],
                _shared_shared_module__WEBPACK_IMPORTED_MODULE_4__["SharedModule"],
                _angular_forms__WEBPACK_IMPORTED_MODULE_3__["FormsModule"],
                // Route
                _security_route_module__WEBPACK_IMPORTED_MODULE_5__["SecurityRouteModule"]
            ],
            declarations: [
                _components_error_page_error_page_component__WEBPACK_IMPORTED_MODULE_6__["ErrorPageComponent"],
                _components_unauthorized_page_unauthorized_page_component__WEBPACK_IMPORTED_MODULE_7__["UnauthorizedPageComponent"],
                _components_not_found_page_not_found_page_component__WEBPACK_IMPORTED_MODULE_8__["NotFoundPageComponent"]
            ],
            exports: [
                _components_error_page_error_page_component__WEBPACK_IMPORTED_MODULE_6__["ErrorPageComponent"],
                _components_unauthorized_page_unauthorized_page_component__WEBPACK_IMPORTED_MODULE_7__["UnauthorizedPageComponent"],
                _components_not_found_page_not_found_page_component__WEBPACK_IMPORTED_MODULE_8__["NotFoundPageComponent"]
            ],
        })
    ], SecurityModule);
    return SecurityModule;
}());



/***/ })

}]);
//# sourceMappingURL=3.js.map