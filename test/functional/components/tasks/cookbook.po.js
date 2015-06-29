'use strict';
var Cookbook = function (){

  this.createRecipeBtn = $('#generateTask[ng-click="createRecipe()"]');
  this.createModal = $('[class="modal fade in"]');
  this.modalTitleCss = this.createModal.$('h3');
  this.modalTabs = this.createModal.$$('.nav-tabs li');
  this.modalButtons = this.createModal.$$('.modal-footer button');
  this.nameField = this.createModal.$('#inputName');
  this.nameLabel = this.createModal.$('[for="inputName"]');
  this.descriptionField = this.createModal.$('#textareaDescription');
  this.descriptionLabel = this.createModal.$('[for="textareaDescription"]');
  this.contextLabel = this.createModal.$('[for="contextSelector2"]');
  this.contextField = this.createModal.$('#contextSelector2');
  this.contextOptions = this.createModal.$$('#contextSelector2 option');
  this.contextOptionSelected = this.createModal.$('#contextSelector2 option:checked');

  this.pageTabs = $$('#mainTabset li');
  //EditorTab
  this.editEditorBtn = $('[ng-click="showEditPopup()"]');
  this.saveWipBtn = $('[ng-click="saveWIP()"]');
  this.undoBtn =$('[ng-click="cancelChanges()"]');

  this.releaseBtn = $('[ng-click="releaseVersion()"]');
  this.discardWipBtn = $('[ng-click="discardWIP()"]');
  
  this.diffBtn = $('[ng-click="diff()"]');

  this.checkSyntaxBtn = $('[ng-click="validateSyntax()"]');
  this.checkSyntaxModal = $('#errorModal');
  this.checkSyntaxErrorDetails = this.checkSyntaxModal.$('#errorModalText');
  this.closeCheckSyntaxModal = this.checkSyntaxModal.$('button.btn');
  this.loadingIndicator = $('loading-indicator [ng-show="isLoading"]');
  //Editor Modal
  this.editorModal = $('[class="modal fade code-editor-modal in"]');
  this.editorTextAreaX = this.editorModal.$('[ng-model="modal.sourceCode"]');
  this.editorTextArea = $$('[ng-model="modal.sourceCode"] .CodeMirror-code pre');
  this.modalCloseBtn = $('[ng-click="storeCode()"]');

  //Task Generation tab
  this.eventLabel = $('[for="eventSelect"]');
  this.eventOptions = $$('#eventSelect option');
  this.eventSelected = $('#eventSelect option:checked');

  this.setAsDefaultLink = $('#setDefaultContext');
  this.clearDefaultLink = $('#clearDefaultContext');

  this.autoPublishTaskLabel = $('[for="autoPublishTasks"]');
  this.autoPublishTaskCheck = $('#autoPublishTasks');

  this.generateUsingWipLabel = $('[for="generateUsingWIP"]');
  this.generateUsingWipCheck = $('#generateUsingWIP');

  this.generateTasksBtn = $('[ng-click="tasks.generateTask(this)"]');
  this.errorMsg = $('[ng-bind="alert.msg"]');
  this.errorMsgCloseBtn = $('[ng-click="alerts.closeAlert($index)"');
  
  this.taskGenerationTabs = $('#taskGenerationTabs');
  //task Generation Tabs
  this.sumaryList = $('[ui-view="taskBatchCompleted"] [class="summaryList ng-scope"]');
};

Cookbook.prototype = {};

Cookbook.prototype.isTaskGenerationTabsDisplayed = function() {
  return browser.wait(function () {
    return $('[ui-view="taskBatchCompleted"]').getText().then(function (text) {
      return text !== '';
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.isErrorMsgDisplayed = function() {
  var that = this;
  return browser.wait(function () {
    return that.errorMsg.getText().then(function (text) {
      return text !== '';
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.isErrorMsgNotDisplayed = function() {
  var that = this;
  return browser.wait(function () {
    return that.errorMsg.isDisplayed().then(function (valor) {
      return !valor;
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.isCreateRecipeModalOpened = function() {
  return browser.wait(function () {
    return $('body').getAttribute('class').then(function (val) {
      return val==='modal-open';
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.isModalClosed = function() {
  return browser.wait(function () {
    return $('body').getAttribute('class').then(function (val) {
      return val==='';
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.isEditorModalOpened = function() {
  var that = this;
  return browser.wait(function () {
    return that.editorModal.isDisplayed().then(function (valor) {
      return valor;
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.setTextToEditor = function(recipe) {
  browser.executeScript('return angular.element("#recipeModalSourceCode").scope().modal.sourceCode = "'+recipe+'"');
};

Cookbook.prototype.isCheckSyntaxModalOpened = function() {
  var that = this;
  return browser.wait(function () {
    return that.checkSyntaxModal.getAttribute('style').then(function (style) {
      return style === 'display: block;';
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.isLoadingIndicatorHidden = function() {
  var that = this;
  return browser.wait(function () {
    return that.loadingIndicator.getAttribute('class').then(function (text) {
      return text.indexOf('ng-hide') !== -1;
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.isCheckSyntaxModalClosed = function() {
var that = this;
  return browser.wait(function () {
    return that.checkSyntaxModal.getAttribute('style').then(function (style) {
      return style === 'display: none;';
    });
  }).then(function () {
    return true;
  });
};

module.exports =  Cookbook;
