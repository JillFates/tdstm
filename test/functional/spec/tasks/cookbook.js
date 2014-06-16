'use strict';
// var UserMenuHelper = require('../support/helpers/userMenu-helper.js').UserMenuHelper;
var CookbookHelper = require('../../support/helpers/cookbook-helper.js').CookbookHelper;
var baseUrl =  process.env.BASE_URL;
describe('Cookbook', function(){ 
  var cookbook = new CookbookHelper();
  browser.driver.get(baseUrl+'/tdstm/cookbook');

  xit('verify page title is cookbook', function(){
    $$('title').then(function(title){
      console.log('title list lenght', title.lenght);
      expect(title[0].getText()).toEqual('hola');
      expect(title[1].getText()).toEqual('holaw');
    });
  });

  describe('create a recipe Modal', function(){

    var createRecipebtn;
    beforeEach(function(){
      createRecipebtn = $(cookbook.createRecipebtnCss);
    });
    it('create recipe button text should be Create Recipe', function(){
      expect(createRecipebtn.getText()).toEqual('Create Recipe');
    });

    it('open create recipe modal and validate title', function(){
      createRecipebtn.click().then(function(){
        var popupTitle = $(cookbook.modalTitleCss);
        expect(popupTitle.getText()).toEqual('Create a recipe');
      });
    });

      describe('tab nav', function(){
        it('validate tabs displayed', function(){
          // element.all(By.css(cookbook.modalTabsCss)).then(function(tabs){
          $$(cookbook.modalTabsCss).then(function(tabs){
            expect(tabs[0].getText()).toEqual('Brand New Recipe');
            expect(tabs[1].getText()).toEqual('Clone An Existing Recipe');
          });
        });

        it('validate active tab by default is Brand New Recipe', function(){
          var activeTab  = $(cookbook.modalActiveTabCss);
          expect(activeTab.getText()).toEqual('Brand New Recipe');
        });

        describe('Brand New Recipe', function(){

          it('Name required error message is not displayed by default', function(){
            var msg = browser.$(cookbook.nameErrorMsgCss);
            expect(msg.getAttribute('class')).toEqual('error-msg');
            //by default is not adding the ng-hide this is added once 
            //the user start typing on the field.
          });

          describe('save button', function(){
            var saveBtn;
            beforeEach(function(){
              saveBtn = browser.$(cookbook.modalSaveBtnCss);
            });

            it('is disabled by dafault', function(){
              expect(saveBtn.getAttribute('disabled')).toEqual('true');
            });

            it('after adding only the name is still disabled', function(){
              var inputName = browser.$(cookbook.nameInputCss);
              inputName.sendKeys('hola');
              expect(saveBtn.getAttribute('disabled')).toEqual('true');
            });
            it('adding description is still disabled', function(){
              var descriptionTextArea = browser.$(cookbook.descriptionTextAreaCss);
              descriptionTextArea.sendKeys('Adding this description to the recipe');
              expect(saveBtn.getAttribute('disabled')).toEqual('true');
            });

            it('adding context should turn to enabled', function(){
              browser.findElements(by.css(cookbook.contextOptionsCss)).then(function(options){
                options[1].click();
              });
              expect(saveBtn.getAttribute('disabled')).toBe(null);
            });

          }); // save button

          it('input fields name label', function(){
            var nameLabel = browser.findElement(by.css(cookbook.nameLabelCss));
            expect(nameLabel.getText()).toEqual('Name*');
          });

          it('while adding text error message is not displayed',function(){
            var inputName = browser.$(cookbook.nameInputCss);
            inputName.sendKeys('hola');
            var msg = browser.$(cookbook.nameErrorMsgCss);
            expect(msg.getAttribute('class')).toEqual('error-msg ng-hide');
          });

          it('if the filed is cleared the error should be displayed', function(){
            var inputName = browser.$(cookbook.nameInputCss);
            inputName.clear();
            inputName.sendKeys(' ');
            var msg = browser.$(cookbook.nameErrorMsgCss);
            expect(msg.getAttribute('class')).toEqual('error-msg');
          });

          it('input field description label', function(){
            var descriptionLabel = browser.findElement(by.css(cookbook.descriptionLabelCss));
            expect(descriptionLabel.getText()).toEqual('Description');
          });

          it('dropdown context label', function(){
            var contextLabel = browser.findElement(by.css(cookbook.contextLabelCss));
            expect(contextLabel.getText()).toEqual('Context*');
          });

          it('dropdown context options', function(){
            browser.findElements(by.css(cookbook.contextOptionsCss)).then(function(options){
              expect(options[0].getText()).toEqual('Select context');
              expect(options[1].getText()).toEqual('Event');
              expect(options[2].getText()).toEqual('Bundle');
              expect(options[3].getText()).toEqual('Application');
            });

          });

          xit('dropdown context default option', function(){

          });

          xit('context should be required', function(){
            //this is tested with the sabe button functionallity
          });


        }); // Brand New Recipe

      }); // tab nav

      it('cancel create recipe modal', function(){
        var cancelBtn = $(cookbook.modalCancelBtnCss);
        cancelBtn.click();
        var createRecipeModal = $(cookbook.createRecipeModalCss);
        expect(createRecipeModal.getAttribute('style')).toEqual('display: none;');

      });
        
  });// create a recipe Modal

  describe('create first recipe', function(){

    xit('list the recipes empty - message is displayed', function(){
      // msg = browser.$()
    });

    xit('create a recipe context Event', function(){

    });
    
    xit('create a recipe context Bundle', function(){

    });

    xit('create a recipe context Application', function(){

    });
    



  });




}); //Cookbook'.modal-body .tabbable'
