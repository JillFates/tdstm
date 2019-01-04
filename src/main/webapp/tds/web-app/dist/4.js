(window["webpackJsonp"] = window["webpackJsonp"] || []).push([[4],{

/***/ "./web-app/app-js/modules/assetTags/asset-tags-routing.states.ts":
/*!***********************************************************************!*\
  !*** ./web-app/app-js/modules/assetTags/asset-tags-routing.states.ts ***!
  \***********************************************************************/
/*! exports provided: AssetTagsRouteStates, AssetTagsRoute, AssetTagsRouteModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AssetTagsRouteStates", function() { return AssetTagsRouteStates; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AssetTagsRoute", function() { return AssetTagsRoute; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AssetTagsRouteModule", function() { return AssetTagsRouteModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/esm5/router.js");
/* harmony import */ var _shared_resolves_module_resolve_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../shared/resolves/module.resolve.service */ "./web-app/app-js/shared/resolves/module.resolve.service.ts");
/* harmony import */ var _security_services_auth_guard_service__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../security/services/auth.guard.service */ "./web-app/app-js/modules/security/services/auth.guard.service.ts");
/* harmony import */ var _components_tag_list_tag_list_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./components/tag-list/tag-list.component */ "./web-app/app-js/modules/assetTags/components/tag-list/tag-list.component.ts");
/* harmony import */ var _shared_model_permission_model__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../shared/model/permission.model */ "./web-app/app-js/shared/model/permission.model.ts");

// Angular


// Resolves

// Services

// Components

// Models

var TOP_MENU_PARENT_SECTION = 'menu-parent-projects';
/**
 * Asset Tag Route States
 * @class
 * @classdesc To use externally to reference possible state of the Asset Tag Module
 */
var AssetTagsRouteStates = /** @class */ (function () {
    function AssetTagsRouteStates() {
    }
    AssetTagsRouteStates.TAG_LIST = {
        url: 'list'
    };
    return AssetTagsRouteStates;
}());

var AssetTagsRoute = [
    { path: '', pathMatch: 'full', redirectTo: AssetTagsRouteStates.TAG_LIST.url },
    {
        path: AssetTagsRouteStates.TAG_LIST.url,
        data: {
            page: {
                title: 'ASSET_TAGS.MANAGE_TAGS',
                instruction: '',
                menu: ['GLOBAL.PROJECTS', 'ASSET_TAGS.MANAGE_TAGS'],
                topMenu: { parent: TOP_MENU_PARENT_SECTION, child: 'menu-parent-project-tags' }
            },
            requiresAuth: true,
            requiresPermissions: [_shared_model_permission_model__WEBPACK_IMPORTED_MODULE_6__["Permission"].TagView],
        },
        component: _components_tag_list_tag_list_component__WEBPACK_IMPORTED_MODULE_5__["TagListComponent"],
        canActivate: [_security_services_auth_guard_service__WEBPACK_IMPORTED_MODULE_4__["AuthGuardService"], _shared_resolves_module_resolve_service__WEBPACK_IMPORTED_MODULE_3__["ModuleResolveService"]]
    }
];
var AssetTagsRouteModule = /** @class */ (function () {
    function AssetTagsRouteModule() {
    }
    AssetTagsRouteModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
            exports: [_angular_router__WEBPACK_IMPORTED_MODULE_2__["RouterModule"]],
            imports: [_angular_router__WEBPACK_IMPORTED_MODULE_2__["RouterModule"].forChild(AssetTagsRoute)]
        })
    ], AssetTagsRouteModule);
    return AssetTagsRouteModule;
}());



/***/ }),

/***/ "./web-app/app-js/modules/assetTags/asset-tags.module.ts":
/*!***************************************************************!*\
  !*** ./web-app/app-js/modules/assetTags/asset-tags.module.ts ***!
  \***************************************************************/
/*! exports provided: AssetTagsModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AssetTagsModule", function() { return AssetTagsModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ "./node_modules/@angular/common/esm5/common.js");
/* harmony import */ var _angular_forms__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/forms */ "./node_modules/@angular/forms/esm5/forms.js");
/* harmony import */ var _shared_shared_module__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../shared/shared.module */ "./web-app/app-js/shared/shared.module.ts");
/* harmony import */ var _progress_kendo_angular_grid__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @progress/kendo-angular-grid */ "./node_modules/@progress/kendo-angular-grid/dist/es/index.js");
/* harmony import */ var _progress_kendo_angular_dropdowns__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @progress/kendo-angular-dropdowns */ "./node_modules/@progress/kendo-angular-dropdowns/dist/es/index.js");
/* harmony import */ var _progress_kendo_angular_dateinputs__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @progress/kendo-angular-dateinputs */ "./node_modules/@progress/kendo-angular-dateinputs/dist/es/index.js");
/* harmony import */ var _asset_tags_routing_states__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ./asset-tags-routing.states */ "./web-app/app-js/modules/assetTags/asset-tags-routing.states.ts");
/* harmony import */ var _shared_resolves_module_resolve_service__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ../../shared/resolves/module.resolve.service */ "./web-app/app-js/shared/resolves/module.resolve.service.ts");
/* harmony import */ var _service_tag_service__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ./service/tag.service */ "./web-app/app-js/modules/assetTags/service/tag.service.ts");
/* harmony import */ var _components_tag_list_tag_list_component__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ./components/tag-list/tag-list.component */ "./web-app/app-js/modules/assetTags/components/tag-list/tag-list.component.ts");
/* harmony import */ var _components_tag_merge_tag_merge_dialog_component__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! ./components/tag-merge/tag-merge-dialog.component */ "./web-app/app-js/modules/assetTags/components/tag-merge/tag-merge-dialog.component.ts");

// Angular



// Shared

// Kendo



// Route Module

// Resolves

// Services

// Components


var AssetTagsModule = /** @class */ (function () {
    function AssetTagsModule() {
    }
    AssetTagsModule = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
            imports: [
                // Angular
                _angular_common__WEBPACK_IMPORTED_MODULE_2__["CommonModule"],
                _shared_shared_module__WEBPACK_IMPORTED_MODULE_4__["SharedModule"],
                _angular_forms__WEBPACK_IMPORTED_MODULE_3__["FormsModule"],
                // Kendo
                _progress_kendo_angular_grid__WEBPACK_IMPORTED_MODULE_5__["GridModule"],
                _progress_kendo_angular_dropdowns__WEBPACK_IMPORTED_MODULE_6__["DropDownsModule"],
                _progress_kendo_angular_dateinputs__WEBPACK_IMPORTED_MODULE_7__["DateInputsModule"],
                // Route
                _asset_tags_routing_states__WEBPACK_IMPORTED_MODULE_8__["AssetTagsRouteModule"]
            ],
            declarations: [
                _components_tag_list_tag_list_component__WEBPACK_IMPORTED_MODULE_11__["TagListComponent"],
                _components_tag_merge_tag_merge_dialog_component__WEBPACK_IMPORTED_MODULE_12__["TagMergeDialogComponent"]
            ],
            providers: [
                _shared_resolves_module_resolve_service__WEBPACK_IMPORTED_MODULE_9__["ModuleResolveService"],
                _service_tag_service__WEBPACK_IMPORTED_MODULE_10__["TagService"]
            ],
            exports: [
                _components_tag_list_tag_list_component__WEBPACK_IMPORTED_MODULE_11__["TagListComponent"]
            ],
            entryComponents: [
                _components_tag_merge_tag_merge_dialog_component__WEBPACK_IMPORTED_MODULE_12__["TagMergeDialogComponent"]
            ]
        })
    ], AssetTagsModule);
    return AssetTagsModule;
}());



/***/ }),

/***/ "./web-app/app-js/modules/assetTags/components/tag-list/tag-list.component.ts":
/*!************************************************************************************!*\
  !*** ./web-app/app-js/modules/assetTags/components/tag-list/tag-list.component.ts ***!
  \************************************************************************************/
/*! exports provided: TagListComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "TagListComponent", function() { return TagListComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");
/* harmony import */ var _shared_services_ui_dialog_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../../../shared/services/ui-dialog.service */ "./web-app/app-js/shared/services/ui-dialog.service.ts");
/* harmony import */ var _shared_services_permission_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../../../shared/services/permission.service */ "./web-app/app-js/shared/services/permission.service.ts");
/* harmony import */ var _shared_directives_ui_prompt_directive__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../../../shared/directives/ui-prompt.directive */ "./web-app/app-js/shared/directives/ui-prompt.directive.ts");
/* harmony import */ var _shared_services_preference_service__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../../../shared/services/preference.service */ "./web-app/app-js/shared/services/preference.service.ts");
/* harmony import */ var _shared_utils_data_grid_operations_helper__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../../../shared/utils/data-grid-operations.helper */ "./web-app/app-js/shared/utils/data-grid-operations.helper.ts");
/* harmony import */ var _service_tag_service__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../service/tag.service */ "./web-app/app-js/modules/assetTags/service/tag.service.ts");
/* harmony import */ var _model_tag_model__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../model/tag.model */ "./web-app/app-js/modules/assetTags/model/tag.model.ts");
/* harmony import */ var _model_tag_list_columns_model__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! ../../model/tag-list-columns.model */ "./web-app/app-js/modules/assetTags/model/tag-list-columns.model.ts");
/* harmony import */ var _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! ../../../../shared/model/ApiResponseModel */ "./web-app/app-js/shared/model/ApiResponseModel.ts");
/* harmony import */ var _shared_model_constants__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! ../../../../shared/model/constants */ "./web-app/app-js/shared/model/constants.ts");
/* harmony import */ var _shared_pipes_translate_pipe__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! ../../../../shared/pipes/translate.pipe */ "./web-app/app-js/shared/pipes/translate.pipe.ts");
/* harmony import */ var _tag_merge_tag_merge_dialog_component__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! ../tag-merge/tag-merge-dialog.component */ "./web-app/app-js/modules/assetTags/components/tag-merge/tag-merge-dialog.component.ts");
/* harmony import */ var _shared_model_permission_model__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! ../../../../shared/model/permission.model */ "./web-app/app-js/shared/model/permission.model.ts");















var TagListComponent = /** @class */ (function () {
    function TagListComponent(tagService, dialogService, permissionService, promptService, translatePipe, userPreferenceService) {
        var _this = this;
        this.tagService = tagService;
        this.dialogService = dialogService;
        this.permissionService = permissionService;
        this.promptService = promptService;
        this.translatePipe = translatePipe;
        this.duplicateName = false;
        this.REMOVE_CONFIRMATION = 'ASSET_TAGS.TAG_LIST.REMOVE_CONFIRMATION';
        userPreferenceService.getUserDatePreferenceAsKendoFormat().subscribe(function (dateFormat) {
            _this.dateFormat = dateFormat;
            _this.gridColumns = new _model_tag_list_columns_model__WEBPACK_IMPORTED_MODULE_9__["TagListColumnsModel"]("{0:" + dateFormat + "}");
            _this.onLoad();
        });
    }
    /**
     * Load necessary lists to render the view.
     */
    TagListComponent.prototype.onLoad = function () {
        var _this = this;
        this.colorList = this.tagService.getTagColorList();
        this.tagService.getTags().subscribe(function (result) {
            if (result.status === _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_10__["ApiResponseModel"].API_SUCCESS) {
                _this.gridSettings = new _shared_utils_data_grid_operations_helper__WEBPACK_IMPORTED_MODULE_6__["DataGridOperationsHelper"](result.data, [{ dir: 'asc', field: 'name' }], // initial sort config.
                { mode: 'single', checkboxOnly: false }, // selectable config.
                { useColumn: 'id' }); // checkbox config.
            }
            else {
                _this.handleError(result.errors ? result.errors[0] : 'an error ocurred while loading the tag list.');
            }
        }, function (error) { return _this.handleError(error); });
    };
    /**
     * On Merge button click.
     */
    TagListComponent.prototype.onMerge = function (dataItem) {
        var _this = this;
        this.dialogService.open(_tag_merge_tag_merge_dialog_component__WEBPACK_IMPORTED_MODULE_13__["TagMergeDialogComponent"], [
            { provide: _model_tag_model__WEBPACK_IMPORTED_MODULE_8__["TagModel"], useValue: dataItem }
        ], _shared_model_constants__WEBPACK_IMPORTED_MODULE_11__["DIALOG_SIZE"].MD).then(function (result) {
            if (result) {
                _this.reloadTagList();
            }
        }).catch(function (result) {
            console.log('Dismissed Dialog');
        });
    };
    /**
     * Handles the Remove action on Remove/Delete button click.
     * @param {any} sender
     */
    TagListComponent.prototype.removeHandler = function (_a) {
        var _this = this;
        var dataItem = _a.dataItem;
        this.promptService.open(this.translatePipe.transform(_shared_model_constants__WEBPACK_IMPORTED_MODULE_11__["PROMPT_DEFAULT_TITLE_KEY"]), this.translatePipe.transform(this.REMOVE_CONFIRMATION), this.translatePipe.transform(_shared_model_constants__WEBPACK_IMPORTED_MODULE_11__["PROMPT_CONFIRM"]), this.translatePipe.transform(_shared_model_constants__WEBPACK_IMPORTED_MODULE_11__["PROMPT_CANCEL"])).then(function (result) {
            if (result) {
                _this.tagService.deleteTag(dataItem.id).subscribe(function (result) {
                    if (result.status === _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_10__["ApiResponseModel"].API_SUCCESS) {
                        _this.reloadTagList();
                    }
                    else {
                        _this.handleError(result.errors ? result.errors[0] : 'an error ocurred while deleting the tag.');
                    }
                }, function (error) { return _this.handleError(error); });
            }
        }, function (reason) { return console.log('confirm rejected', reason); });
    };
    /**
     * Handles the Add process.
     * @param {any} sender
     */
    TagListComponent.prototype.addHandler = function (_a) {
        var sender = _a.sender;
        this.closeEditor(sender);
        sender.addRow(new _model_tag_model__WEBPACK_IMPORTED_MODULE_8__["TagModel"]());
    };
    /**
     * Handles the Update process.
     * @param {any} sender
     * @param {any} rowIndex
     * @param {any} dataItem
     */
    TagListComponent.prototype.editHandler = function (_a) {
        var sender = _a.sender, rowIndex = _a.rowIndex, dataItem = _a.dataItem;
        // close the previously edited item
        this.closeEditor(sender);
        // track the most recently edited row
        // it will be used in `closeEditor` for closing the previously edited row
        this.editedRowIndex = rowIndex;
        // clone the current - `[(ngModel)]` will modify the original item
        // use this copy to revert changes
        this.editedTag = Object.assign({}, dataItem);
        // edit the row
        sender.editRow(rowIndex);
    };
    /**
     * Handles the Save action on button click.
     * @param {any} sender
     * @param {any} rowIndex
     * @param {any} dataItem
     * @param {any} isNew
     */
    TagListComponent.prototype.saveHandler = function (_a) {
        var sender = _a.sender, rowIndex = _a.rowIndex, dataItem = _a.dataItem, isNew = _a.isNew;
        var tagModel = dataItem;
        if (isNew) {
            this.createTag(tagModel, sender, rowIndex);
        }
        else {
            this.updateTag(tagModel, sender, rowIndex);
        }
    };
    /**
     * Handles the Cancel action on Cancel button click.
     * @param {any} sender
     * @param {any} rowIndex
     */
    TagListComponent.prototype.cancelHandler = function (_a) {
        var sender = _a.sender, rowIndex = _a.rowIndex;
        // call the helper method
        this.closeEditor(sender, rowIndex);
    };
    /**
     * Closes the current row in edition.
     * @param grid
     * @param {number} rowIndex
     */
    TagListComponent.prototype.closeEditor = function (grid, rowIndex) {
        var _this = this;
        if (rowIndex === void 0) { rowIndex = this.editedRowIndex; }
        // close the editor
        grid.closeRow(rowIndex);
        // revert the data item to original state
        if (this.editedTag) {
            var match = this.gridSettings.resultSet.find(function (item) {
                return item.id === _this.editedTag.id;
            });
            Object.assign(match, this.editedTag);
        }
        // reset the helpers
        this.editedRowIndex = undefined;
        this.editedTag = undefined;
    };
    /**
     * Creates a new tag.
     * @param {TagModel} tagModel
     * @param sender
     * @param rowIndex
     */
    TagListComponent.prototype.createTag = function (tagModel, sender, rowIndex) {
        var _this = this;
        this.tagService.createTag(tagModel).subscribe(function (result) {
            if (result.status === _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_10__["ApiResponseModel"].API_SUCCESS) {
                _this.finishSave(sender, rowIndex);
                _this.reloadTagList();
            }
            else {
                _this.handleError(result.errors ? result.errors[0] : 'an error ocurred while creating the tag');
            }
        }, function (error) { return _this.handleError(error); });
    };
    /**
     * Updates an existing tag.
     * @param {TagModel} tagModel
     * @param sender
     * @param rowIndex
     */
    TagListComponent.prototype.updateTag = function (tagModel, sender, rowIndex) {
        var _this = this;
        this.tagService.updateTag(tagModel).subscribe(function (result) {
            if (result.status === _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_10__["ApiResponseModel"].API_SUCCESS) {
                _this.finishSave(sender, rowIndex);
                _this.reloadTagList();
            }
            else {
                _this.handleError(result.errors ? result.errors[0] : 'an error ocurred while updating the tag');
            }
        }, function (error) { return _this.handleError(error); });
    };
    /**
     * Common logic for create and update processes.
     * @param sender
     * @param rowIndex
     */
    TagListComponent.prototype.finishSave = function (sender, rowIndex) {
        // reset the helpers
        sender.closeRow(rowIndex);
        this.editedRowIndex = undefined;
        this.editedTag = undefined;
    };
    /**
     * Reloads the current tag list from grid.
     */
    TagListComponent.prototype.reloadTagList = function () {
        var _this = this;
        this.tagService.getTags().subscribe(function (result) {
            if (result.status === _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_10__["ApiResponseModel"].API_SUCCESS) {
                _this.gridSettings.reloadData(result.data);
            }
            else {
                _this.handleError(result.errors ? result.errors[0] : 'an error ocurred while loading the tag list.');
            }
        }, function (error) { return _this.handleError(error); });
    };
    /**
     * Generic error handler function.
     * @param error
     */
    TagListComponent.prototype.handleError = function (error) {
        console.log(error);
    };
    /**
     * Validates the current name on form is unique.
     * @param {string} name
     */
    TagListComponent.prototype.validateUniqueName = function (dataItem) {
        this.duplicateName = false;
        var match = this.gridSettings.resultSet.find(function (item) { return item.name.toLowerCase() === dataItem.name.trim().toLocaleLowerCase(); });
        if (match) {
            this.duplicateName = dataItem.id ? dataItem.id !== match.id : true;
        }
    };
    TagListComponent.prototype.canCreate = function () {
        return this.permissionService.hasPermission(_shared_model_permission_model__WEBPACK_IMPORTED_MODULE_14__["Permission"].TagCreate);
    };
    TagListComponent.prototype.canEdit = function () {
        return this.permissionService.hasPermission(_shared_model_permission_model__WEBPACK_IMPORTED_MODULE_14__["Permission"].TagEdit);
    };
    TagListComponent.prototype.canDelete = function () {
        return this.permissionService.hasPermission(_shared_model_permission_model__WEBPACK_IMPORTED_MODULE_14__["Permission"].TagDelete);
    };
    TagListComponent.prototype.canMerge = function () {
        return this.permissionService.hasPermission(_shared_model_permission_model__WEBPACK_IMPORTED_MODULE_14__["Permission"].TagMerge);
    };
    TagListComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'tag-list',
            templateUrl: '../tds/web-app/app-js/modules/assetTags/components/tag-list/tag-list.component.html',
            providers: [_shared_pipes_translate_pipe__WEBPACK_IMPORTED_MODULE_12__["TranslatePipe"]]
        }),
        tslib__WEBPACK_IMPORTED_MODULE_0__["__metadata"]("design:paramtypes", [_service_tag_service__WEBPACK_IMPORTED_MODULE_7__["TagService"],
            _shared_services_ui_dialog_service__WEBPACK_IMPORTED_MODULE_2__["UIDialogService"],
            _shared_services_permission_service__WEBPACK_IMPORTED_MODULE_3__["PermissionService"],
            _shared_directives_ui_prompt_directive__WEBPACK_IMPORTED_MODULE_4__["UIPromptService"],
            _shared_pipes_translate_pipe__WEBPACK_IMPORTED_MODULE_12__["TranslatePipe"],
            _shared_services_preference_service__WEBPACK_IMPORTED_MODULE_5__["PreferenceService"]])
    ], TagListComponent);
    return TagListComponent;
}());



/***/ }),

/***/ "./web-app/app-js/modules/assetTags/components/tag-merge/tag-merge-dialog.component.ts":
/*!*********************************************************************************************!*\
  !*** ./web-app/app-js/modules/assetTags/components/tag-merge/tag-merge-dialog.component.ts ***!
  \*********************************************************************************************/
/*! exports provided: TagMergeDialogComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "TagMergeDialogComponent", function() { return TagMergeDialogComponent; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "./node_modules/tslib/tslib.es6.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/esm5/core.js");
/* harmony import */ var _shared_services_ui_dialog_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../../../shared/services/ui-dialog.service */ "./web-app/app-js/shared/services/ui-dialog.service.ts");
/* harmony import */ var _service_tag_service__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../service/tag.service */ "./web-app/app-js/modules/assetTags/service/tag.service.ts");
/* harmony import */ var _shared_directives_ui_prompt_directive__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ../../../../shared/directives/ui-prompt.directive */ "./web-app/app-js/shared/directives/ui-prompt.directive.ts");
/* harmony import */ var _model_tag_model__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ../../model/tag.model */ "./web-app/app-js/modules/assetTags/model/tag.model.ts");
/* harmony import */ var _shared_model_constants__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ../../../../shared/model/constants */ "./web-app/app-js/shared/model/constants.ts");
/* harmony import */ var _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! ../../../../shared/model/ApiResponseModel */ "./web-app/app-js/shared/model/ApiResponseModel.ts");
/* harmony import */ var _shared_pipes_translate_pipe__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ../../../../shared/pipes/translate.pipe */ "./web-app/app-js/shared/pipes/translate.pipe.ts");









var TagMergeDialogComponent = /** @class */ (function () {
    function TagMergeDialogComponent(tagModel, tagService, promptService, activeDialog, translatePipe) {
        this.tagModel = tagModel;
        this.tagService = tagService;
        this.promptService = promptService;
        this.activeDialog = activeDialog;
        this.translatePipe = translatePipe;
        this.tagList = [];
        this.MERGE_CONFIRMATION = 'ASSET_TAGS.TAG_LIST.MERGE_CONFIRMATION';
        this.onLoad();
    }
    /**
     * Load necessary lists to render the view.
     */
    TagMergeDialogComponent.prototype.onLoad = function () {
        var _this = this;
        this.tagService.getTags().subscribe(function (result) {
            if (result.status === _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_7__["ApiResponseModel"].API_SUCCESS) {
                var defaultEmptyItem = new _model_tag_model__WEBPACK_IMPORTED_MODULE_5__["TagModel"]();
                defaultEmptyItem.name = 'Select...';
                _this.mergeToTag = defaultEmptyItem;
                _this.tagList.push(defaultEmptyItem);
                (_a = _this.tagList).push.apply(_a, result.data.filter(function (item) { return item.id !== _this.tagModel.id; }));
            }
            else {
                _this.handleError(result.errors ? result.errors[0] : 'an error ocurred while loading the tag list.');
            }
            var _a;
        }, function (error) { return _this.handleError(error); });
    };
    /**
     * On Merge button click, prompts a confirmation, then does the merge operation if confirmed.
     */
    TagMergeDialogComponent.prototype.onMerge = function () {
        var _this = this;
        this.promptService.open(this.translatePipe.transform(_shared_model_constants__WEBPACK_IMPORTED_MODULE_6__["PROMPT_DEFAULT_TITLE_KEY"]), this.translatePipe.transform(this.MERGE_CONFIRMATION), this.translatePipe.transform(_shared_model_constants__WEBPACK_IMPORTED_MODULE_6__["PROMPT_CONFIRM"]), this.translatePipe.transform(_shared_model_constants__WEBPACK_IMPORTED_MODULE_6__["PROMPT_CANCEL"])).then(function (result) {
            if (result) {
                // Do the merge, then close popup
                _this.tagService.mergeTags(_this.tagModel.id, _this.mergeToTag.id).subscribe(function (result) {
                    if (result.status === _shared_model_ApiResponseModel__WEBPACK_IMPORTED_MODULE_7__["ApiResponseModel"].API_SUCCESS) {
                        _this.activeDialog.close(true);
                    }
                    else {
                        _this.handleError(result.errors ? result.errors[0] : 'error ocurred while merging tags.');
                    }
                }, function (error) { return _this.handleError(error); });
            }
        }, function (reason) { return console.log('confirm rejected', reason); });
    };
    /**
     * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
     * @param {KeyboardEvent} event
     */
    TagMergeDialogComponent.prototype.keyDownHandler = function ($event) {
        if ($event && $event.code === _shared_model_constants__WEBPACK_IMPORTED_MODULE_6__["KEYSTROKE"].ESCAPE) {
            this.cancelCloseDialog();
        }
    };
    /**
     * Close the Dialog but first it verify is not Dirty
     */
    TagMergeDialogComponent.prototype.cancelCloseDialog = function () {
        this.activeDialog.close(false);
    };
    /**
     * Generic error handler function.
     * @param error
     */
    TagMergeDialogComponent.prototype.handleError = function (error) {
        console.log(error);
    };
    TagMergeDialogComponent = tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"]([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["Component"])({
            selector: 'tag-merge-dialog',
            templateUrl: '../tds/web-app/app-js/modules/assetTags/components/tag-merge/tag-merge-dialog.component.html',
            host: {
                '(keydown)': 'keyDownHandler($event)'
            },
            providers: [_shared_pipes_translate_pipe__WEBPACK_IMPORTED_MODULE_8__["TranslatePipe"]]
        }),
        tslib__WEBPACK_IMPORTED_MODULE_0__["__metadata"]("design:paramtypes", [_model_tag_model__WEBPACK_IMPORTED_MODULE_5__["TagModel"],
            _service_tag_service__WEBPACK_IMPORTED_MODULE_3__["TagService"],
            _shared_directives_ui_prompt_directive__WEBPACK_IMPORTED_MODULE_4__["UIPromptService"],
            _shared_services_ui_dialog_service__WEBPACK_IMPORTED_MODULE_2__["UIActiveDialogService"],
            _shared_pipes_translate_pipe__WEBPACK_IMPORTED_MODULE_8__["TranslatePipe"]])
    ], TagMergeDialogComponent);
    return TagMergeDialogComponent;
}());



/***/ }),

/***/ "./web-app/app-js/modules/assetTags/model/tag-list-columns.model.ts":
/*!**************************************************************************!*\
  !*** ./web-app/app-js/modules/assetTags/model/tag-list-columns.model.ts ***!
  \**************************************************************************/
/*! exports provided: TagListColumnsModel */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "TagListColumnsModel", function() { return TagListColumnsModel; });
var TagListColumnsModel = /** @class */ (function () {
    function TagListColumnsModel(dateFormat) {
        this.columns = [
            {
                label: 'Name',
                property: 'name',
                type: 'text',
                width: 170,
                locked: false
            },
            {
                label: 'Description',
                property: 'description',
                type: 'text',
                width: 380,
                locked: false
            },
            {
                label: 'Color',
                property: 'color',
                type: 'text',
                width: 100,
                locked: false
            },
            {
                label: 'Assets',
                property: 'assets',
                type: 'number',
                width: 120,
                locked: false,
                cellStyle: { 'text-align': 'center' }
            },
            // Disable these two when data available on API.
            /*{
                label: 'Dependencies',
                property: 'dependencies',
                type: 'number',
                width: 130,
                locked: false
            },
            {
                label: 'Tasks',
                property: 'tasks',
                type: 'number',
                width: 130,
                locked: false
            },*/
            {
                label: 'Date Created',
                property: 'dateCreated',
                type: 'date',
                format: dateFormat,
                width: 160,
                locked: false
            },
            {
                label: 'Last Modified',
                property: 'lastModified',
                type: 'date',
                format: dateFormat,
                width: 160,
                locked: false
            },
        ];
    }
    return TagListColumnsModel;
}());



/***/ })

}]);
//# sourceMappingURL=4.js.map