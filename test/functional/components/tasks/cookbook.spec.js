'use strict';
//TM-2995  Unable to regenerate tasks after reverting and deleting version of a recipe
/*TM-2912 
user should not be albe to delete the release version
delete the wip / should only delete the wip and not the release version
If the user delete the wip and  this is the only recepe versio , the editor should be cleared.
When you create a recepe description is not required, but it is required when you clone a recipe. - should not have the same behavior on both?
# tabs are not displayed if recipe list is is empty
# if you have 1 or more recipes, the  tabs are displayed and the 1st recipe is selected.

#  Task Generation 
 checkbox : 
 Generate using WIP recipe - > is only displayed when you select a recipe with wip created
 Delete previously generated tasks that were created using this context & recipe ->  This is only displayed if task are created using that wip recipe 
 # Clone create recipe -> description is not mandaroty
*/

var  Menu = require('../menu/menu.po.js');
var Cookbook = require('./cookbook.po.js');
describe('Cookbook', function(){ 
  var cookbook = new Cookbook();

  it('should load cookbook page after select Task Cookbook', function(){
    var menu = new Menu();
    menu.goToTasks('cookbook');
    expect(menu.getCurrentUrl()).toContain(process.env.BASE_URL+'/tdstm/cookbook/index#/recipes');
  });

  describe('Create a recipe', function(){

    var createRecipebtn;

    it('should have "Create Recipe..." as label', function(){
      expect(cookbook.createRecipeBtn.getText()).toEqual('Create Recipe...');
    });

    it('should open "Create a Recipe" modal', function() {
      cookbook.createRecipeBtn.click();
      expect(cookbook.isCreateRecipeModalOpened()).toBe(true);
    });

    it('should have "Create a recipe" as title', function(){
      expect(cookbook.modalTitleCss.getText()).toEqual('Create a recipe');
    });

    describe('tab nav', function(){

      it('should have 2 tabs', function() {
        expect(cookbook.modalTabs.count()).toEqual(2);
      });

      it('should have "Brand New Recipe" as tab label',function () {
        expect(cookbook.modalTabs.get(0).getText()).toEqual('Brand New Recipe');
      });
      
      it('should have "Clone An Existing Recipe" as tab label',function () {
        expect(cookbook.modalTabs.get(1).getText()).toEqual('Clone An Existing Recipe');
      });

      it('should have "Brand New Recipe" as active tab', function() {
        expect(cookbook.modalTabs.get(0).getAttribute('class')).toContain('active');
      });

    }); // tab nav

    describe('Buttons', function() {
      
      it('should have 2 buttons', function() {
        expect(cookbook.modalButtons.count()).toEqual(2);
      });

      it('should have "Save" button', function() {
        expect(cookbook.modalButtons.get(0).getText()).toEqual('Save');
      });

      it('should have save button disabled by default',function () {
        expect(cookbook.modalButtons.get(0).getAttribute('disabled')).toEqual('true');
      });
      
      it('should have "Cancel" button', function() {
        expect(cookbook.modalButtons.get(1).getText()).toEqual('Cancel');
      });

    });//Buttons

    describe('Name Field', function() {
      
      it('should have "Name*" field ', function() {
        expect(cookbook.nameLabel.getText()).toEqual('Name*');
      });

      it('should be empty by default', function() {
        expect(cookbook.nameField.getAttribute('value')).toEqual('');
      });

      it('should be required', function() {
        expect(cookbook.nameField.getAttribute('class')).toContain('required');
      });

      it('should add a Name to the new recipe', function() {
        cookbook.nameField.sendKeys('Recipe 1');
        expect(cookbook.nameField.getAttribute('value')).toEqual('Recipe 1');
      });

      it('should have save button disabled after adding only the name',function () {
        expect(cookbook.modalButtons.get(0).getAttribute('disabled')).toEqual('true');
      });

    }); // Name Field

    describe('Context Dropdown', function() {
      
      it('should have "Context" as label', function() {
        expect(cookbook.contextLabel.getText()).toEqual('Context*');
      });

      it('should have "Select Context" displayed by default',function () {
        expect(cookbook.contextOptionSelected.getText()).toEqual('Select context');
      });

      it('should be required', function() {
        expect(cookbook.contextField.getAttribute('class')).toContain('ng-invalid-required');
      });

      it('should have 4 option',function () {
        expect(cookbook.contextOptions.count()).toEqual(4);
      });

      it('should have "Select context" as option', function() {
        expect(cookbook.contextOptions.get(0).getText()).toEqual('Select context');
      });

      it('should have "Event" as option', function() {
        expect(cookbook.contextOptions.get(1).getText()).toEqual('Event');
      });

      it('should have "Bundle" as option', function() {
        expect(cookbook.contextOptions.get(2).getText()).toEqual('Bundle');
      });

      it('should have "Application" as option', function() {
        expect(cookbook.contextOptions.get(3).getText()).toEqual('Application');
      });

      it('should select "Event" as context',function () {
        cookbook.contextOptions.get(1).click();
        expect(cookbook.contextOptionSelected.getText()).toEqual('Event');
      });

      it('should have save button enabled after adding name and context',function () {
        expect(cookbook.modalButtons.get(0).getAttribute('disabled')).toBe(null);
      });            

    }); //Context Dropdown

    describe('Description Field', function() {
      
      it('should have "Description" as label',function () {
        expect(cookbook.descriptionLabel.getText()).toEqual('Description');
      });

      it('should be empty by default',function () {
        expect(cookbook.descriptionField.getAttribute('value')).toEqual('');
      });

      it('should add a descripton to the new Recipe',function () {
        cookbook.descriptionField.sendKeys('This is a description for Recipe 1');
        expect(cookbook.descriptionField.getAttribute('value')).toEqual('This is a description for Recipe 1');
      });
      
    }); // Description Field

    it('should save the recipe', function() {
      cookbook.modalButtons.get(0).click();
      expect(cookbook.isCreateRecipeModalClosed()).toBe(true);
    });


  });// create a recipe Modal

  describe('Cookbook Page Tabs', function() {
    
    it('should have 4 tabs', function() {
      expect(cookbook.pageTabs.count()).toEqual(4);
    });
    
    it('should have "Task Generation" as tab', function() {
      expect(cookbook.pageTabs.get(0).getText()).toEqual('Task Generation');
    });

    it('should have "History" as tab', function() {
      expect(cookbook.pageTabs.get(1).getText()).toEqual('History');
    });

    it('should have "Editor" as tab', function() {
      expect(cookbook.pageTabs.get(2).getText()).toEqual('Editor');
    });

    it('should have "Version" as tab', function() {
      expect(cookbook.pageTabs.get(3).getText()).toEqual('Versions');
    });

    it('should have "Editor" tab selected after recipe is created', function() {
      expect(cookbook.pageTabs.get(2).getAttribute('class')).toContain('active');
    });

  });

  describe('Add a recipe', function() {

    xit('should have Edit button', function() {
      expect(cookbook.editEditorBtn.getText()).toEqual('Edit');
    });

    xit('should open Editor after click on edit btn', function() {
      cookbook.editEditorBtn.click();
      expect(cookbook.isEditorModalOpened()).toBe(true);
    });

    xit('should add text to editor', function() {
      var recipe = [
      'tasks: [',
      '  [',
      '    id: 1100,',
      '    description: \'Shutdown ALL applications\',',
      '    title: \'Shutdown app ${it.assetName}\',',
      '    workflow: \'AppShutdown\',',
      '    team: \'APP_COORD\',',
      '    category: \'shutdown\',',
      '    duration: 10,',
      '      filter : [',
      '        class: \'application\',',
      '        group: \'BET_THIS_DOES_NOT_EXIST\'',
      '      ],',
      '  ],',
      ']'
      ].join('\n');
      cookbook.editorTextArea.click();
      // cookbook.editorTextArea.sendKeys(recipe);

    });
    
  });//Add a recipe
// describe('Editor Tab', function(){
//   describe ('show diff button', function(){

//       xit('should be enabled', function(){
//         //user has changed the syntax of a recipe
//       });
// // It used to be that when editing a recipe you could click the Diff button and see the changes between the currently edited recipe and WIP or Release. For some reason right now the button is always disabled.

// // The button should be enabled when:

// //     The user has changed the syntax of recipe

// // The button should be disabled when:

// //     Recipe first loaded
// //     After Save WIP or Release buttons are clicked successfully

// // There are two situations for comparison:

// //     When there is a WIP recipe, should compare the local changes to WIP
// //     When there is a Version/Release of a recipe and no WIP exists, should compare the local changes to the Version.


//   }); // show diff button
//   });//Editor Tab



}); //Cookbook'.modal-body .tabbable'
