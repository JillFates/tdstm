'use strict';
var  Menu = require('../menu/menu.po.js');
var ListBundles = require('./bundles.po.js');
describe('Bundles', function(){
  var menu = new Menu();
  var bundlePage = new ListBundles();
  var bundleName = 'BundTest';

  it('should load list Bundles page after select Events/Bundles > List Bundles', function(){
    menu.goToPlanning('listBundles');
    expect(bundlePage.getTitle().getText()).toEqual('Bundle List');
  });
 
  describe('Create Page', function(){

    it('should load create Bundle page after hitting create Bundle button',function(){
      bundlePage.clickOnCreateBundleBtn();
      expect(bundlePage.getTitle().getText()).toEqual('Create Bundle');
    });
    
    it('should have moveBundle/create url', function(){
      expect(menu.getCurrentUrl())
        .toEqual(process.env.BASE_URL+'/tdstm/moveBundle/create');
    });

    it('should set bundle Name', function(){
      bundlePage.setName(bundleName);
      expect(bundlePage.getName()
        .getAttribute('value')).toEqual(bundleName);
    });

    it('should save the bundle and go to moveBundle/show/', function(){
      bundlePage.clickSaveBundle();
      expect(menu.getCurrentUrl())
        // .toMatch(/http:\/\/localhost:8080\/tdstm\/moveBundle\/show\/\d\d\d\d/);
        .toMatch(process.env.BASE_URL+'/tdstm/moveBundle/show/\\d\\d\\d\\d');
    });
  }); // Create Page

  it('should be displayed created bundle confirmation message', function(){
    expect(bundlePage.getConfirmMsg().getText()).toEqual('MoveBundle '+bundleName+' created');
  });

  it('should updated Page title with the created Bundle name', function(){
    browser.driver.sleep(5000);
    expect(menu.getHeaderTitle().getText()).toEqual(' TransitionManager™ - Test Project : '+bundleName);
  });


  xdescribe('delete bundle assigned to an asset', function(){
    describe('preconditions', function(){
      //bundle created
      // 
    }); //  preconditions
  
  }); // delete bundle assigned to an asset
}); // Bundles