'use strict';
var  Menu = require('../menu/menu.po.js');
var ProjectStaff = require('../projects/projectStaff.po.js');

describe('Project Staff Page', function() {
  var menu = new Menu();
  var projStaff = new ProjectStaff();
  var expectTeams = ['All','Account Manager', 'App Coordinator', 'Automatic', 'Backup Admin', 'Database Admin', 
  'Database Admin-DB2','Database Admin-MSSQL', 'Database Admin-Oracle', 'Logistics Technician', 
  'Migration Analyst', 'Migration Lead', 'Move Manager', 'Move Technician','Move Technician-Sr', 
  'Network Admin','Project Admin','Project Manager','Storage Admin','System Admin','System Admin-AIX',
  'System Admin-Linux', 'System Admin-Unix','System Admin-Win', 'Technician','VM Admin'];

  it('should get to Project Staff Page after select Projects > Project Staff on the menu', function () {
    menu.goToProjects('projectStaff');
    expect(menu.getCurrentUrl('/tdstm/person/manageProjectStaff')).toEqual(process.env.BASE_URL+'/tdstm/person/manageProjectStaff');
  });

  it('should have Project Staff as title', function () {
    expect(projStaff.getTitle.getText()).toEqual('Project Staff');
  });

  describe('Team dropdown', function() {
    
    it('should have "Team" as label', function() {
      expect(projStaff.teamLabel.getText()).toEqual('Team');
    });
 
    it('should have 26 options on Team Dropdown', function() {
      expect(projStaff.teamOptions.count()).toEqual(26);
    });

    for (var i=0; i< expectTeams.length;i++) {
      
      (function(index) {
        
        it('should have '+expectTeams[i]+' as Team', function() {
          projStaff.teamOptions.then(function (list) {
            expect(list[index].getText()).toEqual(expectTeams[index]);
          });  
        });

      })(i);

    }
    
    xit('should have "" as default option', function() {
      //Which is the default option?
    });
    
  }); // Team Dropdown

  describe('Only Client Staff checkbox', function() {
    
    it('should have "Only Client Staff" as label', function() {
      expect(projStaff.onlyClientStaffLabel.getText()).toEqual('Only Client Staff');
    });

    it('should be unchecked by default', function() {
      expect(projStaff.onlyClientStaffCheck.getAttribute('checked')).toEqual(null);
    });

    it('should show only Selected Project Staff if the checkbox is selected', function() {
      projStaff.onlyClientStaffCheck.click();
      expect(projStaff.onlyClientStaffCheck.getAttribute('checked')).toEqual('true');
      projStaff.getTableCompanyList().then(function (list) { 
        list.forEach(function (elem) {
          expect(elem).toEqual('[Marketing Demo]');
        });
      });
    });

    it('should show Selected Project and TDS project if the Checkbox is disabled', function() {
      projStaff.onlyClientStaffCheck.click();
      expect(projStaff.onlyClientStaffCheck.getAttribute('checked')).toEqual(null);
      projStaff.getTableCompanyList().then(function (list) { 
        list.forEach(function (elem) {
          var expCompanies = ['[Marketing Demo]','[TDS]'];
          expect(expCompanies).toContain(elem);
        });
      });
    });

  }); // Only Client Staff checkbox

  describe('Only Assigned checkbox', function() {
    
    it('should have "Only Assigned" as label', function() {
      expect(projStaff.onlyAssignedLabel.getText()).toEqual('Only Assigned');
    });

    it('should be unchecked by default', function() {
      expect(projStaff.onlyAssignedCheck.getAttribute('checked')).toEqual(null);
    });

    it('should only displayed assigned staff if the checkbox is enabled', function() {
      projStaff.onlyAssignedCheck.click();
      expect(projStaff.onlyAssignedCheck.getAttribute('checked')).toEqual('true');
      projStaff.tableProjectList.then(function (list) {
        list.forEach(function(elem){
          expect(elem.$('input').getAttribute('value')).toEqual('1');
        });
      });
    });

    it('should displayed all staff if the checkbox is disabled', function() {
      projStaff.onlyAssignedCheck.click();
      expect(projStaff.onlyAssignedCheck.getAttribute('checked')).toEqual(null);
      projStaff.tableProjectList.then(function (list) {
        list.forEach(function(elem){
          expect(['0','1']).toContain(elem.$('input').getAttribute('value'));
        });
      });
      
    });


  }); //Only Assigned Checkbox

  describe('Project dropdown', function() {
    
    it('should have "Project" as label', function() {
      expect(projStaff.projectLabel.getText()).toEqual('Project');
    });

    it('should have x options listed', function() {
      expect(projStaff.projectOptions.count()).toEqual(8);
    });

    it('should have the following options', function() {
      projStaff.projectOptions.then(function (list) {
        expect(list[0].getText()).toEqual('All');
        // expect(list[1].getText()).toMatch('\\s*AFC\\s*');
        expect(list[1].getText()).toMatch('\\s*Bemis Corp Prod\\s*');
        // expect(list[3].getText()).toMatch('\\s*BJs\\s*');
        // expect(list[4].getText()).toMatch('\\s*CLSA\\s*');
        expect(list[2].getText()).toMatch('\\s*ExchangeMove2\\s*');
        expect(list[3].getText()).toMatch('\\s*HBGUSA Exchange Migration\\s*');
        expect(list[4].getText()).toMatch('\\s*HealthcoreNoAssets\\s*');
        expect(list[5].getText()).toMatch('\\s*MarketingDemo\\s*');
        expect(list[6].getText()).toMatch('\\s*Maxion Data Centers\\s*');
        // expect(list[7].getText()).toMatch('\\s*Travelers\\s*');
        expect(list[7].getText()).toMatch('\\s*West\\s*');
      });
    });

    it('should have "MarketingDemo" as default project', function() {
      expect(projStaff.getProjectSelected()).toMatch('\\s*MarketingDemo\\s*');
    });

  }); // Project dropdown

  describe('Table', function () {

    it('should have at least 4 columns', function() {
      projStaff.tableHeads.then(function (list) {
        expect(list.length >=4).toBeTruthy();
      });
    });

    it('should select "All" as project', function() {
      projStaff.projectOptions.get(0).click();
      expect(projStaff.projectSelected.getText()).toEqual('All');
    });

    it('should select "All" as Team', function() {
      projStaff.teamOptions.get(0).click();
      expect(projStaff.teamSelected.getText()).toEqual('All');
    });

    it('should have the following headers', function () {
      projStaff.tableHeads.then(function (list) {
        expect(list[0].getText()).toEqual('Name');
        expect(list[1].getText()).toEqual('Company');
        expect(list[2].getText()).toEqual('Team');
        expect(list[3].getText()).toMatch('\\s*'+projStaff.getProjectSelected()+'\\s*');
      });
    });

    it('should order by name asc when you click on name head',function () {
      projStaff.tableHeads.get(0).click();
      projStaff.getTableNameList().then(function (list) {
        var sortList = list.slice(0);
        for(var i=0;i <sortList.length;i++){
          list[i]=list[i].toLowerCase();
          sortList[i]=sortList[i].toLowerCase();
        }
        expect(list).toEqual(sortList.sort());
      });
    });

    xit('should order by "Company" desc when you click on Company head', function() {
      projStaff.tableHeads.get(1).click();
      // browser.sleep(8000);
      projStaff.getTableCompanyList().then(function (list) {
        // var origList = list;
        var sortList = list.slice(0);
        for(var i=0;i <sortList.length;i++){
          list[i]=list[i].toLowerCase();
          sortList[i]=sortList[i].toLowerCase();
        }
        expect(list).toEqual(sortList.sort());
      });
    });

    xit('should order by name desc when you click on name head',function () {
      projStaff.tableHeads.get(0).click();
      browser.driver.sleep(8000); 
      projStaff.getTableNameList().then(function (list) {
        var reverseList = list.slice(0);
        for(var i=0;i <reverseList.length;i++){
          list[i]=list[i].toLowerCase();
          reverseList[i]=reverseList[i].toLowerCase();
        }
        expect(list).toEqual(reverseList.reverse());
      });
    });

    xit('should order by "Company" asc when you click on Company head', function() {
      projStaff.tableHeads.get(1).click();
      projStaff.getTableCompanyList().then(function (list) {
        console.log('complist desc', list );
        var origList = list;
        var sortList = list.slice(0).sort();
        var reverseList = list.slice(0).reverse();
        console.log(origList[0]);
        console.log(sortList[0]);
        console.log(reverseList[0]);
        console.log(origList[list.length-1]);
        console.log(sortList[list.length-1]);
        console.log(reverseList[list.length-1]);
        expect(list).toEqual(list.slice(0).reverse());
      });  
    });

    xit('should order by "Team" asc when you click on Team Head', function() {
      projStaff.tableHeads.get(2).click();
      projStaff.getTableTeamList().then(function (list) {
        // console.log('team asc', list );
        expect(list).toEqual(list.reverse());
      });
    });
    
    xit('should order by "Team" desc when you click on Team Head', function() {
       projStaff.tableHeads.get(1).click();
      projStaff.getTableTeamList().then(function (list) {
        // console.log('team desc', list );
        expect(list).toEqual(list.sort());
      });  
    });
  

  });// Table

  describe('Project = all', function() {

    it('should select "All" as project', function() {
      projStaff.projectOptions.get(0).click();
      expect(projStaff.projectSelected.getText()).toEqual('All');
    });

    for (var i=0; i< expectTeams.length;i++) {
      
      (function(index) {
        
        it('should list the selected team staff: '+expectTeams[index]+'', function() {
          projStaff.teamOptions.get(index).click();
          expect(projStaff.teamSelected.getText()).toEqual(expectTeams[index]);
          projStaff.getTableTeamList().then(function (list) { 
            list.forEach(function (elem) {
              if(index ===0){
                var all = ['Staff'].concat(expectTeams);
                expect(all).toContain(elem);
              }else{
                expect(elem).toEqual(expectTeams[index]);
              }
            });
          });

        });

      })(i);
    }

  });

  xit('should not allow to assigned a person for a different team',function () {
    
  });
}); // Project Staff