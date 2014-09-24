'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Asset Tags Reports Only runs on IE', function(){
  var menu = new Menu();
  it('should load Asset Tags Report page after click on Reports > Asset Tags', function(){
    menu.goToReports('assetTags');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Asset+Tag');
  });
  
  it('should have Assets Tag as title',function(){
    var assetTagsPage = new Reports();
    expect(assetTagsPage.getPageTitle()).toEqual('Generate Asset Tag');
  });

}); // Asset Tags Report