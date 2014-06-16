'use strict';
function CookbookHelper(){
  this.createRecipebtnCss = '.col-md-4 button';
  this.createRecipeModalCss = '#createRecipeModal';
  this.createRecipeFormCss = '#createRecipeForm';
  this.createRecBodyCss = this.createRecipeFormCss+' '+'.modal-body .tabbable';
  this.modalTitleCss = this.createRecipeFormCss+' '+'h3';
  this.modalActiveTabCss = this.createRecBodyCss+' '+'.nav-tabs li.active';
  this.modalTabsCss = this.createRecBodyCss +' '+'.nav-tabs li';
  this.modalCancelBtnCss = this.createRecipeFormCss+' '+'[ng-click="modalBtns.cancel()"]';
  this.modalSaveBtnCss = this.createRecipeFormCss+' '+'[ng-click="modalBtns.save()"]';
  this.modalTabContentCss = this.createRecipeFormCss+' '+'.tab-content .active';
  this.nameLabelCss = this.modalTabContentCss+' '+ '[for="inputName"]';
  this.nameInputCss = this.modalTabContentCss+' '+ '#inputName';
  this.nameErrorMsgCss = this.modalTabContentCss+' '+ '.error-msg';
  this.descriptionLabelCss = this.modalTabContentCss+' '+ '[for="textareaDescription"]';
  this.descriptionTextAreaCss = this.modalTabContentCss+' '+ 'textarea';
  this.contextLabelCss = this.modalTabContentCss+' '+ '[for="contextSelector2"]';
  this.contextOptionsCss = this.modalTabContentCss+' '+ '[name="contextSelector2"] option';
} 

CookbookHelper.prototype = {};

  

exports.CookbookHelper = CookbookHelper;