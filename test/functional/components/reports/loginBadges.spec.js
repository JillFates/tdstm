'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Login Badges Reports Only runs on ie', function(){
  var menu = new Menu();
  it('should load Login Badges Report page after click on Reports > Login Badges', function(){
    menu.goToReports('loginBadges');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Login+Badges');
  });
  
  it('should have Login Badges as title',function(){
    var loginBadgesPage = new Reports();
    expect(loginBadgesPage.getPageTitle()).toEqual('Login Badges');
  });

}); // Login Badges Report