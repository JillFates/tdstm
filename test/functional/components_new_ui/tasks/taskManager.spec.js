'use strict';
var  Menu = require('../menu/menu.po.js');
var TaskManager = require('./taskManager.po.js');

describe('Task Manager', function(){
	
	var taskManager = new TaskManager();
	
  it('should load My tasks page after select Tasks - Task Manager', function(){
    var menu = new Menu();
  	var tasks = browser.driver.findElement(by.className('menu-parent-tasks'));
  	tasks.click();
  	var items = tasks.findElement(by.className('menu-item-expand'));
  	if (items.isDisplayed()){
  		items.findElement(by.className('menu-parent-tasks-task-manager')).click();
  	}
  	expect(menu.getCurrentUrl()).toContain(process.env.BASE_URL+'/tdstm/assetEntity/listTasks?initSession=true');
  });
  
  it('should have "Task Manager" as title',function(){
    expect(taskManager.headerTitle.getText()).toEqual('Task Manager');
  });
  
  xit('should have "Task > Task Manager" as breadcrumb',function(){
    expect(taskManager.breadCrumb.getText()).toEqual('Task > Task Manager');
    // TODO check the ::before element and split the expects for each part of breadcrumb
  });
 
  describe('Action Bar',function(){
  	
	  describe('Control Row',function(){
	    
	  	it('should have "Event" as title', function(){
	      expect(taskManager.eventLb.getText()).toEqual('Event');
	    });
	    
	  	describe('Event Dropdown', function() {
	      
		  	it('should have Event dropdown', function(){
		      expect(taskManager.moveEventDD.isPresent()).toBe(true);
		    });	
	      xit('should have 10 options',function () {
	        expect(taskManager.moveEventDDOptions.count()).toEqual(10);
	      });	
	      it('should have "All" as option', function() {
	        expect(taskManager.moveEventDDOptions.get(0).getText()).toEqual(' All'); // TODO Trim the text
	      });
	      xit('should have'+taskManager.eventUserTxt+' displayed by default',function () {
	        expect(taskManager.moveEventDDSelected.getText()).toEqual(taskManager.eventUserTxt);
	      });
	      it('should can select "All" as option',function () {
	      	taskManager.moveEventDDOptions.get(0).click();
	        expect(taskManager.moveEventDDSelected.getText()).toEqual(' All'); // TODO Trim the text
	      });

	    }); //Event Dropdown
	    
	  	it('should have Just Remaining checkbox', function(){
	      expect(taskManager.justRemainingCB.isPresent()).toBe(true);
	    });
	  	it('should have "Just Remaining" checkbox label', function(){
	      expect(taskManager.justRemainingLb.isPresent()).toBe(true);
	      expect(taskManager.justRemainingLb.getText()).toEqual('Just Remaining');
	    });  	
	  	it('should have Just Mine checkbox ', function(){
	      expect(taskManager.justMyTasksCB.isPresent()).toBe(true);
	    });
	  	it('should have "Just Mine" checkbox label', function(){
	      expect(taskManager.justMyTasksLb.isPresent()).toBe(true);
	      expect(taskManager.justMyTasksLb.getText()).toEqual('Just Mine');
	    });
	  	it('should have View Unpublished checkbox', function(){
	      expect(taskManager.viewUnpublishedCB.isPresent()).toBe(true);
	    });
	  	it('should have "View Unpublished" checkbox label', function(){
	      expect(taskManager.viewUnpublishedLb.isPresent()).toBe(true);
	      expect(taskManager.viewUnpublishedLb.getText()).toEqual('View Unpublished');
	    });
	 
	  });  // end of control Row
	  
	  describe('Time Line Functions',function(){ 
	  	
	  	it('should have View Task Graph button', function(){
	      expect(taskManager.viewTaskGraphBt.isPresent()).toBe(true);
	    });
	  	it('should have View Task Graph button image', function(){
	      expect(taskManager.viewTaskGraphBtImg.isPresent()).toBe(true);
	    });	
	  	it('should have "View Task Graph" button label', function(){
	      expect(taskManager.viewTaskGraphBtLb.isPresent()).toBe(true);
	      expect(taskManager.viewTaskGraphBtLb.getText()).toEqual('View Task Graph');
	    });
	  	it('should have View Timeline button', function(){
	      expect(taskManager.viewTimeLineBt.isPresent()).toBe(true);
	    });
	  	it('should have View Timeline button image', function(){
	      expect(taskManager.viewTimeLineBtImg.isPresent()).toBe(true);
	    });	
	  	it('should have "View Timeline" button label', function(){
	      expect(taskManager.viewTimeLineBtLb.isPresent()).toBe(true);
	      expect(taskManager.viewTimeLineBtLb.getText()).toEqual('View Timeline');
	    });
	  	
	    describe('Time Refresh Options', function() {
	      
		  	it('should have refresh button', function(){
		      expect(taskManager.timeRefreshBt.isPresent()).toBe(true);
		    });	
				xit('should refresh the list after click', function(){
					taskManager.clearFiltersBt.click();
				// TODO evaluate refreshed list with default filters
				});	
		  	it('should have Time Refresh dropdown', function(){
		      expect(taskManager.selectTimedBarDD.isPresent()).toBe(true);
		    });	
	      it('should have 6 option',function () {
	        expect(taskManager.selectTimedBarDDOptions.count()).toEqual(6);
	      });	
	      it('should have "Manual" as option', function() {
	        expect(taskManager.selectTimedBarDDOptions.get(0).getText()).toEqual('Manual');
	      });
	      xit('should have "Manual" displayed by default',function () {
	        expect(taskManager.selectTimedBarDDSelected.getText()).toEqual('Manual');
	      });
	      it('should have "1 Min" as option', function() {
	        expect(taskManager.selectTimedBarDDOptions.get(1).getText()).toEqual('1 Min');
	      });
	      it('should have "2 Min" as option', function() {
	        expect(taskManager.selectTimedBarDDOptions.get(2).getText()).toEqual('2 Min');
	      });
	      it('should have "3 Min" as option', function() {
	        expect(taskManager.selectTimedBarDDOptions.get(3).getText()).toEqual('3 Min');
	      });
	      it('should have "4 Min" as option', function() {
	        expect(taskManager.selectTimedBarDDOptions.get(4).getText()).toEqual('4 Min');
	      });
	      it('should have "5 Min" as option', function() {
	        expect(taskManager.selectTimedBarDDOptions.get(5).getText()).toEqual('5 Min');
	      });	      
	      it('should can select "2 Min" as timer',function () {
	      	taskManager.selectTimedBarDDOptions.get(2).click();
	        expect(taskManager.selectTimedBarDDSelected.getText()).toEqual('2 Min');
	      });

	    }); //Time Refresh Options
	  	
	  }); // end of Time Line Functions

  });  // end of Action Bar
  
  describe('Task Functions',function(){ 	
  	
  	it('should have "Tasks" as label', function(){
      expect(taskManager.tasksLb.getText()).toEqual('Tasks');
    });
  	
  	describe('Action Buttons',function(){
  		
  		describe('Create Task Button',function(){
		  	
				it('should have Create Task button', function(){
					expect(taskManager.createTaskBt.isPresent()).toBe(true);
				});	
				it('should have "Create Task" button label', function(){
		      expect(taskManager.createTaskBtLb.isPresent()).toBe(true);
		      expect(taskManager.createTaskBtLb.getText()).toEqual('Create Task');
				});			  	
				it('should open Create Task modal window after click', function(){
					taskManager.createTaskBt.click();
		      expect(taskManager.isCreateTaskModalOpened()).toBe(true);
		    });
	    	it('should close the modal window if clicked "Close" button', function() {
	    		taskManager.ctModalCancelBt.click();
	        expect(taskManager.isModalClosed()).toBe(true);
	      });	
	    	
				// Start of Create Task				
				
				describe('Create Task Modal Window',function(){

					it('should open Create Task modal window after click', function(){
						taskManager.createTaskBt.click();
			      expect(taskManager.isCreateTaskModalOpened()).toBe(true);
			    });
					it('should have "Create Task" as title', function(){
						expect(taskManager.ctModalTitle.getText()).toEqual('Create Task');
					});
			    
					describe('Modal Window Buttons', function() {
			      
			      it('should have 2 buttons', function() {
			        expect(taskManager.ctModalButtons.count()).toEqual(2);
			      });
			      it('should have "Save" button', function() {
			        expect(taskManager.ctModalSaveBt.getText()).toEqual('Save');
			      });
			      // TODO : activate when functionality change to disable this button 
			      xit('should have save button disabled by default',function () {
			        expect(taskManager.ctModalSaveBt.get(0).getAttribute('disabled')).toEqual('true');
			      });
			      it('should have "Cancel" button', function() {
			        expect(taskManager.ctModalCancelBt.getText()).toEqual('Cancel');
			      });
	
			    });// End Of Modal Window Buttons		    
			    
					describe('Task Name Field', function() {
			      
			      it('should have "Task : *" as name ', function() {
			        expect(taskManager.ctModalNameLb.getText()).toEqual('Task : *');
			      });
			      // TODO: Check the error message embedded on the Task label 
			      it('should be empty by default', function() {
			        expect(taskManager.ctModalNameTA.getAttribute('value')).toEqual('');
			      });
			      it('should be required', function() {
			        expect(taskManager.ctModalNameTA.getAttribute('class')).toContain('ng-invalid-required');
			      });
			      it('should add a Name to the new Task', function() {
			      	taskManager.ctModalNameTA.sendKeys('Task 1');
			        expect(taskManager.ctModalNameTA.getAttribute('value')).toEqual('Task 1');
			      });
			      // TODO : activate when functionality change to disable this button 
			      xit('should have save button enabled after adding the task name',function () {
			        expect(taskManager.ctModalSaveBt.get(0).getAttribute('enabled')).toEqual('true');
			      });
	
			    }); // Task Name Field

			    describe('Person/team Dropdowns', function() {

		    		it('should have "Person/Team:" as label', function() {
			        expect(taskManager.ctModalPersTeamLb.getText()).toEqual('Person/Team:');
			      });
		    		
			    	describe('Person Dropdown', function() {			      
				    
				      // TODO check the current user logged as the selected by default
				      xit('should have "Automatic" selected by default',function () {
				        expect(taskManager.ctModalPersDDSelected.getText()).toEqual('Automatic');
				      });
				      // TODO check the current user logged to create staff from scratch
				      xit('should have a N options',function () {
				        expect(taskManager.ctModalPersDDOptions.count()).toEqual(1);
				      });
				      // TODO check the blank value on empty project
				      xit('should have "Unassigned" as option', function() {
				        expect(taskManager.ctModalPersDDOptions.get(0).getText()).toEqual('Unassigned');
				      });
				      // TODO check the staff orderer list
				      xit('should have "Staff: TDS Admin" as option', function() {
				        expect(taskManager.ctModalPersDDOptions.get(75).getText()).toEqual('Staff: TDS Admin');
				      });
				      it('should select "Unassigned" as Person',function () {
				        taskManager.ctModalPersDDOptions.get(0).click();
				        expect(taskManager.ctModalPersDDSelected.getText()).toEqual('Unassigned');
				      });
				    }); //end Person Dropdown					

			    	describe('Team Dropdown', function() {
			      
				      // TODO check the current user logged as the selected by default
				      xit('should have "Unassigned" selected by default',function () {
				        expect(taskManager.ctModalTeamDDSelected.getText()).toEqual('Unassigned');
				      });
				      // TODO check the current user logged to create staff from scratch
				      xit('should have a N options',function () {
				        expect(taskManager.ctModalTeamDDOptions.count()).toEqual(1);
				      });
				      // TODO check the blank value on empty project
				      xit('should have "Unassigned" as option', function() {
				        expect(taskManager.ctModalTeamDDOptions.get(0).getText()).toEqual('Unassigned');
				      });
				      // TODO check the staff orderer list
				      it('should have "Account Manager" as option', function() {
				        expect(taskManager.ctModalTeamDDOptions.get(1).getText()).toEqual('Account Manager');
				      });
				      it('should select "Account Manager" as Team',function () {
				        taskManager.ctModalTeamDDOptions.get(1).click();
				        expect(taskManager.ctModalTeamDDSelected.getText()).toEqual('Account Manager');
				      });

			    	}); //end Team Dropdown					
		    
			    }); //end Person/Team Dropdowns				    

		    	describe('Event Dropdown', function() {
		    		
		    		it('should have "Event:" as label', function() {
			        expect(taskManager.ctModalEventLb.getText()).toEqual('Event:');
			      });
			      // TODO check the null value on dropdown
			      it('should have blank by default',function () {
			        expect(taskManager.ctModalEventDDSelected.getText()).toEqual('');
			      });
			      // TODO check the current user logged to create staff from scratch
			      it('should have 15 options',function () {
			        expect(taskManager.ctModalEventDDOptions.count()).toEqual(15);
			      });
			      // TODO check the blank value on empty project
			      it('should have "Buildout" as option', function() {
			        expect(taskManager.ctModalEventDDOptions.get(2).getText()).toEqual('Buildout ');
			      });
			      // TODO check the staff orderer list
			      it('should have "DR-ERP" as option', function() {
			        expect(taskManager.ctModalEventDDOptions.get(3).getText()).toEqual('DR-ERP ');
			      });
			      it('should select "Buildout" as Event',function () {
			        taskManager.ctModalEventDDOptions.get(2).click();
			        expect(taskManager.ctModalEventDDSelected.getText()).toEqual('Buildout ');
			      });

		    	}); //end Event Dropdown							    

		    	describe('Category Dropdown', function() {
		    		
		    		it('should have "Category:" as label', function() {
			        expect(taskManager.ctModalCategoryLb.getText()).toEqual('Category:');
			      });
			      it('should have "general" selected by default',function () {
			        expect(taskManager.ctModalCategoryDDSelected.getText()).toEqual('general ');
			      });
			      // TODO check the current user logged to create staff from scratch
			      it('should have 18 options',function () {
			        expect(taskManager.ctModalCategoryDDOptions.count()).toEqual(18);
			      });
			      it('should have "discovery" as option', function() {
			        expect(taskManager.ctModalCategoryDDOptions.get(2).getText()).toEqual('discovery ');
			      });
			      // TODO check the staff orderer list
			      it('should have "planning" as option', function() {
			        expect(taskManager.ctModalCategoryDDOptions.get(3).getText()).toEqual('planning ');
			      });
			      it('should select "discovery" as Category',function () {
			        taskManager.ctModalCategoryDDOptions.get(2).click();
			        expect(taskManager.ctModalCategoryDDSelected.getText()).toEqual('discovery ');
			      });

		    	}); //end Category Dropdown	

		    	
		    	describe('Asset Dropdowns', function() {

		    		it('should have "Asset:" as label', function() {
			        expect(taskManager.ctModalAssetLb.getText()).toEqual('Asset:');
			      });
		    		
		    		describe('Asset Type Dropdown', function() {

				      it('should have blank by default',function () {
				        expect(taskManager.ctModalAssetTypeDDSelected.getText()).toEqual('');
				      });
				      it('should have 7 options',function () {
				        expect(taskManager.ctModalAssetTypeDDOptions.count()).toEqual(7+1);
				      }); //TODO Check the "blank" option also
				      it('should have "Applications" as option', function() {
				        expect(taskManager.ctModalAssetTypeDDOptions.get(1).getText()).toEqual('Applications');
				      });
				      it('should have "Databases" as option', function() {
				        expect(taskManager.ctModalAssetTypeDDOptions.get(2).getText()).toEqual('Databases');
				      });		
				      it('should have "Network Devices" as option', function() {
				        expect(taskManager.ctModalAssetTypeDDOptions.get(3).getText()).toEqual('Network Devices');
				      });
				      it('should have "Other Devices" as option', function() {
				        expect(taskManager.ctModalAssetTypeDDOptions.get(4).getText()).toEqual('Other Devices');
				      });
				      it('should have "Servers" as option', function() {
				        expect(taskManager.ctModalAssetTypeDDOptions.get(5).getText()).toEqual('Servers');
				      });
				      it('should have "Storage Devices" as option', function() {
				        expect(taskManager.ctModalAssetTypeDDOptions.get(6).getText()).toEqual('Storage Devices');
				      });
				      it('should have "Logical Storage" as option', function() {
				        expect(taskManager.ctModalAssetTypeDDOptions.get(7).getText()).toEqual('Logical Storage');
				      });
				      it('should select "Servers" as asset type',function () {
				        taskManager.ctModalAssetTypeDDOptions.get(5).click();
				        expect(taskManager.ctModalAssetTypeDDSelected.getText()).toEqual('Servers');
				      });

			    	}); //end Asset Dropdown

		    		describe('Asset Name Dropdown', function() {

				      xit('should have "Please Select" option by default',function () {
				        expect(taskManager.ctModalAssetNameDDSelected.getText()).toEqual('');
				      });
				      xit('should have N options',function () {
				        expect(taskManager.ctModalAssetNameDDOptions.count()).toEqual(37);
				      }); //TODO Validate the Assets names related to asset type selected
				      xit('should have "AGPM" as option', function() {
				        expect(taskManager.ctModalAssetNameDDOptions.get(1).getText()).toEqual('AGPM');
				      });
				      xit('should select "Servers" as asset type',function () {
				        taskManager.ctModalAssetNameDDOptions.get(5).click();
				        expect(taskManager.ctModalAssetNameDDSelected.getText()).toEqual('Servers');
				      });

			    	}); //end Asset Name Dropdown
		    		
		    	}); //end Asset Dropdowns

		    	describe('Priority Dropdown', function() {
					
		    		it('should have "Priority:" as label', function() {
						  expect(taskManager.ctModalPriorityLb.getText()).toEqual('Priority:');
						});
						it('should have "3" by default',function () {
						  expect(taskManager.ctModalPriorityDDSelected.getText()).toEqual('3 ');
						});
						it('should have 5 options',function () {
						  expect(taskManager.ctModalPriorityDDOptions.count()).toEqual(5);
						}); 					  
						it('should have "1" as option', function() {
						  expect(taskManager.ctModalPriorityDDOptions.get(0).getText()).toEqual('1 ');
						});
						it('should have "2" as option', function() {
						  expect(taskManager.ctModalPriorityDDOptions.get(1).getText()).toEqual('2 ');
						});		
						it('should have "3" as option', function() {
						  expect(taskManager.ctModalPriorityDDOptions.get(2).getText()).toEqual('3 ');
						});
						it('should have "4" as option', function() {
						  expect(taskManager.ctModalPriorityDDOptions.get(3).getText()).toEqual('4 ');
						});
						it('should have "5" as option', function() {
						  expect(taskManager.ctModalPriorityDDOptions.get(4).getText()).toEqual('5 ');
						});
						it('should select "5" as priority',function () {
						  taskManager.ctModalPriorityDDOptions.get(4).click();
						  expect(taskManager.ctModalPriorityDDSelected.getText()).toEqual('5 ');
						});
					  
		    	});	//end Priority Dropdown
					
		    	describe('Instructions Link field', function() {
			      
			      it('should have "Instructions Link:" as name ', function() {
			        expect(taskManager.ctModalInstLinkLb.getText()).toEqual('Instructions Link:');
			      });
			      it('should have "Instructions Link:" as name ', function() {
			        expect(taskManager.ctModalInstLinkTF.getAttribute('placeholder')).toEqual('Enter URL or Label|URL');
			      });					      
			      it('should be empty by default', function() {
			        expect(taskManager.ctModalInstLinkTF.getAttribute('value')).toEqual('');
			      });
			      it('should show an error after complete with a non-url format value', function() {
			      	taskManager.ctModalInstLinkTF.sendKeys('non url value');
			      	taskManager.ctModalInstLinkLb.click();
			      	expect(taskManager.isErrorModalDisplayed()).toBe(true);
			      });
			      it('should show the "The format of the linkable URL is invalid." error message', function() {
			      	expect(taskManager.errorModalText.getText()).toEqual('The format of the linkable URL is invalid.');
			      });
			      it('should close error msg', function() {
			      	taskManager.errorModalCloseBtn.click();
		          browser.waitForAngular();
		          expect(taskManager.isErrorModalNotDisplayed()).toBe(true);
		        });
			      it('should have save button disabled until fix the url',function () {
			        expect(taskManager.ctModalSaveBt.getAttribute('disabled')).toEqual('true');
			      });
			      it('should add a correct url to field', function() {
			      	taskManager.ctModalInstLinkTF.clear().sendKeys('https://www.transitionaldata.com');
			        taskManager.ctModalInstLinkLb.click();
			      	expect(taskManager.isErrorModalNotDisplayed()).toBe(true);				        
			      });
			      it('should have save button enabled after fix the url',function () {
			        expect(taskManager.ctModalSaveBt.isEnabled()).toBe(true);
			      });
	
			    }); // End of Instructions Link field
					  
		    	it('should save the new task', function() {
		    		taskManager.ctModalSaveBt.click();
		        expect(taskManager.isModalClosed()).toBe(true);
		      });		    	
				
				}); // end of Create Task modal Window				
		  
  		});	// end of Create Task
	
 
  		describe('Bulk Edit Button',function(){
		  	
				it('should have Bulk Edit button', function(){
					expect(taskManager.bulkEditBt.isPresent()).toBe(true);
				});	
				
				it('should have "Bulk Edit" button label', function(){
		      expect(taskManager.bulkEditBtLb.isPresent()).toBe(true);
		      expect(taskManager.bulkEditBtLb.getText()).toEqual('Bulk Edit');
				});			  	
				
				xit('should show status button bars after click', function(){
					taskManager.bulkEditBt.click();
				// TODO evaluate status button bars
				});	
				xit('should hide status button bars after click again', function(){
					taskManager.bulkEditBt.click();
				// TODO evaluate status button bars
				});	
				
		  });	// end of Bulk Edit Button
  
  		describe('Clear Filters Button',function(){
		  	
				it('should have Clear Filters button', function(){
					expect(taskManager.clearFiltersBt.isPresent()).toBe(true);
				});	
				
				it('should have "Clear Filters" button', function(){
		      expect(taskManager.clearFiltersBtLb.isPresent()).toBe(true);
		      expect(taskManager.clearFiltersBtLb.getText()).toEqual('Clear Filters');
				});			  	
				
				xit('should refresh list with default filters after click', function(){
					taskManager.clearFiltersBt.click();
				// TODO evaluate refreshed list with default filters
				});	
					
		  });	// end of Clear Filters Button
  		
  	});	// end of Action Buttons
  	
	});	// end of Task Functions

  describe('Table',function(){
    	
  	describe('Table Toggle Button',function(){
	  	
			it('should have Table Toggle button', function(){
				expect(taskManager.tableToggleBt.isPresent()).toBe(true);
			});	

			xit('should hide the table after click', function(){
				taskManager.tableToggleBt.click();
				// TODO evaluate table hidden
			});	
			
			xit('should show the table after click again', function(){
				taskManager.tableToggleBt.click();
				// TODO evaluate show table again
			});
	  
		});	// end of Table Toggle Button 	
		
		describe('Table Columns Header',function(){
  		
			describe('Action Column',function(){
		  	
				it('should have header', function(){
					expect(taskManager.actionTCol.isPresent()).toBe(true);
				});	
				
				it('should have "Action" as name', function(){
		      expect(taskManager.actionTColLb.isPresent()).toBe(true);
		      expect(taskManager.actionTColLb.getText()).toEqual('Action');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.actionTCol.click();
					// TODO evaluate column ascending
				});	

				xit('should sort column descending after click again', function(){
					taskManager.actionTCol.click();
					// TODO evaluate column descending
				});	
				
		  });	// end of Action Column			

			describe('Task Column',function(){
		  	
				it('should have header', function(){
					expect(taskManager.taskTCol.isPresent()).toBe(true);
				});	
				
				it('should have "Task" as name', function(){
		      expect(taskManager.taskTColLb.isPresent()).toBe(true);
		      expect(taskManager.taskTColLb.getText()).toEqual('Task');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.taskTCol.click();
					// TODO evaluate column ascending
				});	

				xit('should sort column descending after click again', function(){
					taskManager.taskTCol.click();
					// TODO evaluate column descending
				});	
				
				it('should have column filter', function(){
					expect(taskManager.taskTColFlt.isPresent()).toBe(true);
				});	
				
				xit('should filter tasks by input text', function(){
					taskManager.taskTColFlt.setText('16600');
					// TODO evaluate column filtering
				});				
		  
			});	// end of Task Column						

			describe('Description Column',function(){
		  	
				it('should have header', function(){
					expect(taskManager.descriptionTCol.isPresent()).toBe(true);
				});	
				
				it('should have "Description" as name', function(){
		      expect(taskManager.descriptionTColLb.isPresent()).toBe(true);
		      expect(taskManager.descriptionTColLb.getText()).toEqual('Description');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.descriptionTCol.click();
					// TODO evaluate column ascending
				});	

				xit('should sort column descending after click again', function(){
					taskManager.descriptionTCol.click();
					// TODO evaluate column descending
				});	
				
				it('should have column filter', function(){
					expect(taskManager.descriptionTColFlt.isPresent()).toBe(true);
				});				

				xit('should filter by description input text', function(){
					taskManager.descriptionTColFlt.setText('Test Task');
					// TODO evaluate column filtering
				});
				
			});	// end of Description Column	

			describe('Updated Column',function(){
		  	
				it('should have header', function(){
					expect(taskManager.updatedTCol.isPresent()).toBe(true);
				});	
				
				it('should have "Updated" as name', function(){
		      expect(taskManager.updatedTColLb.isPresent()).toBe(true);
		      expect(taskManager.updatedTColLb.getText()).toEqual('Updated');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.updatedTCol.click();
					// TODO evaluate column ascending
				});	

				xit('should sort column descending after click again', function(){
					taskManager.updatedTCol.click();
					// TODO evaluate column descending
				});	
				
		  });	// end of Updated Column	
			
			describe('Due Date Column',function(){
		  	
				it('should have header', function(){
					expect(taskManager.dueDateTCol.isPresent()).toBe(true);
				});	
				
				it('should have "Due Date" as name', function(){
		      expect(taskManager.dueDateTColLb.isPresent()).toBe(true);
		      expect(taskManager.dueDateTColLb.getText()).toEqual('Due Date');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.dueDateTCol.click();
					// TODO evaluate column ascending
				});	

				xit('should sort column descending after click again', function(){
					taskManager.dueDateTCol.click();
					// TODO evaluate column descending
				});	
				
				it('should have column filter', function(){
					expect(taskManager.dueDateTColFlt.isPresent()).toBe(true);
				});				

				xit('should filter by due date input text', function(){
					taskManager.dueDateTColFlt.setText('07/07/2017');
					// TODO evaluate column filtering
				});			  				
		  
			});	// end of Due Date Column	
			
			describe('Status Column',function(){
		  	
				it('should have header', function(){
					expect(taskManager.statusTCol.isPresent()).toBe(true);
				});	
				
				it('should have "Status" as name', function(){
		      expect(taskManager.statusTColLb.isPresent()).toBe(true);
		      expect(taskManager.statusTColLb.getText()).toEqual('Status');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.statusTCol.click();
					// TODO evaluate column ascending
				});	

				xit('should sort column descending after click again', function(){
					taskManager.statusTCol.click();
					// TODO evaluate column descending
				});	
				
				it('should have column filter', function(){
					expect(taskManager.statusTColFlt.isPresent()).toBe(true);
				});				
				
				xit('should filter by status input text', function(){
					taskManager.statusTColFlt.setText('Ready');
					// TODO evaluate column filtering
				});			  
		  
			});	// end of Status Column				
			
			describe('Suc. Column',function(){
		  	
				it('should have header', function(){
					expect(taskManager.sucTCol.isPresent()).toBe(true);
				});	
				
				it('should have "Suc." as name', function(){
		      expect(taskManager.sucTColLb.isPresent()).toBe(true);
		      expect(taskManager.sucTColLb.getText()).toEqual('Suc.');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.sucTCol.click();
					// TODO evaluate column ascending
				});	

				xit('should sort column descending after click again', function(){
					taskManager.sucTCol.click();
					// TODO evaluate column descending
				});	
				
		  });	// end of Suc. Column	
			
			describe('Score Column',function(){
		  	
				it('should have header', function(){
					expect(taskManager.scoreTCol.isPresent()).toBe(true);
				});	
				
				it('should have "Score" as name', function(){
		      expect(taskManager.scoreTColLb.isPresent()).toBe(true);
		      expect(taskManager.scoreTColLb.getText()).toEqual('Score');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.scoreTCol.click();
					// TODO evaluate column ascending
				});	

				xit('should sort column descending after click again', function(){
					taskManager.scoreTCol.click();
					// TODO evaluate column descending
				});	
				
		  });	// end of Score Column	
			
		
			describe('Custom Column',function(){
		  	
				xit('should have header', function(){
					expect(taskManager.customTCol.isPresent()).toBe(true);
				});	
				
				xit('should have selected name', function(){
		      expect(taskManager.customTColLb.isPresent()).toBe(true);
		      expect(taskManager.customTColLb.getText()).toEqual('Score');
				});			  	
				
				xit('should sort column ascending after click', function(){
					taskManager.customTCol.click();
					// TODO evaluate column ascending
				});	
	
				xit('should sort column descending after click again', function(){
					taskManager.customTCol.click();
					// TODO evaluate column descending
				});	
			
			});	// end of Custom Column	
		
		});	// end of Table Columns Header   

		describe('Refresh button',function(){
			
		  it('should have refresh button at bottom', function(){
		    expect(taskManager.tableRefreshBt.isPresent()).toBe(true);
		  });	
		  
			xit('should refresh the list after click', function(){
				taskManager.tableRefreshBt.click();
			// TODO evaluate refreshed list with default filters
			});	
			
		});  // end of Refresh button

  });  // end of Table

});