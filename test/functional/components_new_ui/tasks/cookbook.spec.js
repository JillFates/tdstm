'use strict';
//TM-2995  Unable to regenerate tasks after reverting and deleting version of a recipe
/*TM-2912 
user should not be able to delete the release version
delete the WIP / should only delete the WIP and not the release version
If the user delete the WIP and  this is the only recipe version , the editor should be cleared.
When you create a recipe description is not required, but it is required when you clone a recipe. - should not have the same behavior on both?
# tabs are not displayed if recipe list is is empty
# if you have 1 or more recipes, the  tabs are displayed and the 1st recipe is selected.

#  Task Generation 
 checkbox : 
 Generate using WIP recipe - > is only displayed when you select a recipe with wip created
 Delete previously generated tasks that were created using this context & recipe ->  This is only displayed if task are created using that wip recipe 
 # Clone create recipe -> description is not mandatory
*/

var  Menu = require('../menu/menu.po.js');
var Cookbook = require('./cookbook.po.js');

describe('Cookbook', function(){ 
  
	var cookbook = new Cookbook();

  it('should load cookbook page after select Task Cookbook', function(){
    var menu = new Menu();
	var tasks = browser.driver.findElement(by.className('menu-parent-tasks'));
	tasks.click();
	var isdisp;
	var items = tasks.findElement(by.className('menu-item-expand'));
	if (items.isDisplayed()){
		var cookbook = items.findElement(by.className('menu-parent-tasks-cookbook'));
		cookbook.click();
	}
	expect(menu.getCurrentUrl()).toContain(process.env.BASE_URL+'/tdstm/cookbook/index#/recipes');
  });

  describe('Create a recipe', function(){

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
      expect(cookbook.isModalClosed()).toBe(true);
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

    it('should have Edit button', function() {
      expect(cookbook.editEditorBtn.getText()).toEqual('Edit');
    });

    it('should open Editor after click on edit btn', function() {
      cookbook.editEditorBtn.click();
      expect(cookbook.isEditorModalOpened()).toBe(true);
    });

    it('should add text to editor', function() {
      var recipe = [
      'tasks: [',
      '  [',
      '    id: 1100,',
      '    description: \'Startup ALL applications\',',
      '    title: \'Startup app ${it.assetName}\',',
      '    workflow: \'AppStartup\',',
      '    team: \'APP_COORD\',',
      '    category: \'startup\',',
      '    duration: 10,',
      '      filter : [',
      '        class: \'application\',',
      '        group: \'BET_THIS_DOES_NOT_EXIST\'',
      '      ],',
      '  ],',
      ']'
      ];
      cookbook.setTextToEditor(recipe.join('\\n'));
      cookbook.editorTextArea.click();
      expect(cookbook.editorTextArea.getText()).toEqual(recipe);
    });

    it('should close Recipe modal', function () {
      cookbook.modalCloseBtn.click();
      expect(cookbook.isModalClosed()).toBe(true);
    });
    
  });//Add a recipe

  describe('Save WIP Button', function() {
    
    it('should have "Save WIP" as label', function() {

      expect(cookbook.saveWipBtn.getText()).toEqual('Save WIP');
          
        });    
    it('should have "Save Wip" button enabled', function() {
      expect(cookbook.saveWipBtn.getAttribute('disabled')).toBe(null);
    });

    it('should save WIP and disable the button', function() {
      cookbook.saveWipBtn.click();
      expect(cookbook.saveWipBtn.getAttribute('disabled')).toBe('true');
    });
    

  });// Save WIP Button

  describe('Check Syntax button', function() {
    
    it('should have "Check Syntax" as label', function() {
      expect(cookbook.checkSyntaxBtn.getText()).toEqual('Check Syntax');
    });
    
    it('should check syntax', function() {
      cookbook.checkSyntaxBtn.click();
      expect(cookbook.isSyntaxErrorTabSelected()).toBe(true);
    });
    
    it('should have Invalid syntax displayed',function () {
      expect(cookbook.checkSyntaxErrorTitle.getText()).toEqual('Invalid syntax');
    });
    
    it('should have an error displayed',function () {
      expect(cookbook.checkSyntaxErrorDetails.getText()).toEqual('Task id 1100 \'filter/group\' references an invalid group BET_THIS_DOES_NOT_EXIST');
    });
  
  }); // Check Syntax button

  describe('Fix Recipe and check syntax', function() {

    it('should edit the recipe',function () {
      cookbook.editEditorBtn.click();
      expect(cookbook.isEditorModalOpened()).toBe(true);    

    });
    
    it('should add text to editor', function() {
      var recipe = [
      'tasks: [',
      '  [',
      '    id: 1100,',
      '    description: \'Startup ALL applications\',',
      '    title: \'Startup app ${it.assetName}\',',
      '    workflow: \'AppStartup\',',
      '    team: \'APP_COORD\',',
      '    category: \'startup\',',
      '    duration: 10,',
      '      filter : [',
      '        class: \'application\'',
      '      ],',
      '  ],',
      ']'
      ];
      cookbook.setTextToEditor(recipe.join('\\n'));
      cookbook.editorTextAreaX.click();
      expect(cookbook.editorTextArea.getText()).toEqual(recipe);
    });

    it('should close Recipe modal', function () {
      cookbook.modalCloseBtn.click();
      expect(cookbook.isModalClosed()).toBe(true);
    });
    
    it('should save WIP and disable the button', function() {
      cookbook.saveWipBtn.click();
      expect(cookbook.saveWipBtn.getAttribute('disabled')).toBe('true');
    });

    it('should check syntax and no error is displayed', function() {
      cookbook.checkSyntaxBtn.click();
      expect(cookbook.isLoadingIndicatorHidden()).toBe(true);
    });

 //  -- DEBUG -- Verify the static values on objects assigned on po's
    it('should have the message "No errors found" displayed',function () {
      expect(cookbook.checkSyntaxErrorTitle.getText()).toEqual('No errors found');
    });

  }); //Fix Recipe and check syntax


  describe('Task Generation Tab', function() {

  it('should go to Task Generation tab', function() {
    cookbook.pageTabs.get(0).click();
    expect(cookbook.pageTabs.get(0).getAttribute('class')).toContain('active');
  });


    describe('Event Dropdown', function() {
    
      xit('should have "Event:" as label', function() {
        expect(cookbook.eventLabel.getText()).toEqual('Event:'); 
      });

      it('should have "Please Select" option selected by default', function() {
        expect(cookbook.eventSelected.getText()).toEqual('Please select');  
      });

      describe('Set as Default', function() {
        
        it('should have "Set As default" as text', function() {
          expect(cookbook.setAsDefaultLink.getText()).toEqual('Set as Default');
        });

        it('should have "Set as Default" disabled ', function() {
          expect(cookbook.setAsDefaultLink.getAttribute('disabled')).toBe('true');
        });

        it('should be displayed', function() {
          expect(cookbook.setAsDefaultLink.getAttribute('class')).not.toContain('ng-hide');
        });

      }); // Set as Default

      it('should select an event from the Dropdown', function() {
        cookbook.eventOptions.get(1).click();
        expect(cookbook.eventSelected.getText()).toEqual('Buildout');  
      });

      it('should have "Set As Default" enabled', function() {
          expect(cookbook.setAsDefaultLink.getAttribute('disabled')).toBe(null);
      });

      it('should set the event as default and link should changed to "Clear Default"', function() {
        cookbook.setAsDefaultLink.click();
        expect(cookbook.setAsDefaultLink.getAttribute('class')).toContain('ng-hide');
      });

      describe('Clear Default', function() {
        
        it('should have "Set As default" as text', function() {
          expect(cookbook.clearDefaultLink.getText()).toEqual('Clear Default');
        });

        it('should have "Clear Default" enabled ', function() {
          expect(cookbook.clearDefaultLink.getAttribute('disabled')).toBe(null);
        });

        it('should be displayed', function() {
          expect(cookbook.clearDefaultLink.getAttribute('class')).not.toContain('ng-hide');
        });

      }); // Clear Default

      
    }); //Event Dropdown


    describe('Automatically publish tasks Checkbox', function() {
      
      it('should have "Automatically publish tasks" as label', function() {
        expect(cookbook.autoPublishTaskLabel.getText()).toEqual('Automatically publish tasks');
      });

      it('should be unchecked by default', function() {
        expect(cookbook.autoPublishTaskCheck.getAttribute('checked')).toBe(null);
      });

    });

    describe('Generate using WIP checkbox', function() {
      
      it('should have "Generate using WIP" as label', function() {
        expect(cookbook.generateUsingWipLabel.getText()).toEqual('Generate using WIP recipe');
      });

      it('should be disabled by default', function() {
        expect(cookbook.generateUsingWipCheckBox.getAttribute('checked')).toBe(null);
        
      });

    });

    describe('Generate Tasks', function() {

      describe('From WIP with "generate using wip recipe" unchecked', function() {

        it('should have "Generate Tasks" as label on the button ', function() {
          expect(cookbook.generateTasksBtn.getText()).toEqual('Generate Tasks');
        });

        it('should click on generate task button and error should be displayed', function() {
          cookbook.generateTasksBtn.click();
          expect(cookbook.isErrorModalDisplayed()).toBe(true);
        });

        it('should validate error message', function() {
          expect(cookbook.errorModalText.getText()).toEqual('There is no released version of the recipe to generate tasks with');
        });

        it('should close error msg', function() {
          cookbook.errorModalCloseBtn.click();
          expect(cookbook.isErrorModalNotDisplayed()).toBe(true);
          browser.sleep(2000);
        });

      }); //From WIP with "generate using wip recipe" unchecked

      describe('From WIP with "Generate using WIP recipe" checked', function() {
      	
      	it('should enable "Generate using WIP Recipe" checkbox', function() {
          cookbook.generateUsingWipCheckBox.click();
          expect(cookbook.generateUsingWipCheckBox.getAttribute('checked')).toBe('true');
        });

        it('should click on generate task button', function() {
          cookbook.generateTasksBtn.click();
          expect(cookbook.isTaskGenerationTabsDisplayed()).toBe(true);
        });

        it('should have this result', function() {
          expect(cookbook.sumaryList.getText()).toEqual('Status: Completed\nTasks Created: 8\nNumber of Exceptions: 8');
        });

      }); //From WIP with "Generate using WIP recipe" checked

      describe('Generate Tasks with previously tasks created', function() {
      
        it('should click on generate task button', function() {
          cookbook.generateTasksBtn.click();
          browser.sleep(2000);
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('There are tasks previously created with this recipe for the selected context.\n\nPress Okay to delete or Cancel to abort.');
          alertDialog.accept();     
        });
      
      });
    });// Generate Tasks
  });

      	
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

