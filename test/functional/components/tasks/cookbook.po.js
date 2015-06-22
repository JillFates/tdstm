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
  //Editor Modal
  this.editorModal = $('[class="modal fade code-editor-modal in"]');
  // this.editorTextArea = this.editorModal.$('[ng-model="modal.sourceCode"]');
  this.editorTextArea = $('[ng-model="modal.sourceCode"] .CodeMirror-code');
};

Cookbook.prototype = {};
Cookbook.prototype.isCreateRecipeModalOpened = function() {
  var that = this;
  return browser.wait(function () {
    return that.createModal.isDisplayed().then(function (valor) {
      return valor;
    });
  }).then(function () {
    return true;
  });
};

Cookbook.prototype.isCreateRecipeModalClosed = function() {
  var that = this;
  return browser.wait(function () {
    return $('body').getAttribute('class').then(function (val) {
      console.log('closed',val);
      return val==='';
    });
    // return that.createModal.isDisplayed().then(function (valor) {
      // console.log('valor en close',valor)
      // return !valor;
    // });
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

module.exports =  Cookbook;
