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
	    
	  	xit('should have "Event" as title', function(){
	      expect(taskManager.controlRow.getText()).toEqual('Event');
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
	  	
	  });  // end of Time Line Functions

  });  // end of Action Bar
 
  describe('Table',function(){
  	
  	xit('should have "Tasks:" as label', function(){
      expect(taskManager.tasksLb.getText()).toEqual('Tasks:');
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
				
				xit('should open Create Task modal window after click', function(){
					taskManager.createTaskBt.click();
					// TODO evaluate create task modal window
				});	
					
		  });	// end of Create Task Button
	
 
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
	  
		});	// end of Table Header Button 	
		
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
				
			});	// end of Task Column	

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