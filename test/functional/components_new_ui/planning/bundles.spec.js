'use strict';
var  Menu = require('../menu/menu.po.js');
var ListBundles = require('./bundles.po.js');
describe('Bundles', function(){
  var menu = new Menu();
  var bundlePage = new ListBundles();
  var bundleName = 'BundTest';

  it('should load list Bundles page after select Events/Bundles > List Bundles', function(){
    menu.goToPlanning('listBundles');
    
    expect(menu.waitForURL('/tdstm/moveBundle/list')).toBe(true);

  });

  it('should have "Bundle List" as title', function() {
    expect(bundlePage.getTitle().getText()).toEqual('Bundle List');

  });

  xit('should have TBD Boundle listed', function(){

  });
 
  describe('Create Page', function(){

    it('should load create Bundle page after hitting create Bundle button',function(){
      bundlePage.clickOnCreateBundleBtn();
      expect(menu.waitForURL('/tdstm/moveBundle/create')).toBe(true);
      
    });
    
    it('should have "Create Bundle" as title', function(){
      expect(bundlePage.getTitle().getText()).toEqual('Create Bundle');
    });

    it('should set bundle Name', function(){
      bundlePage.setName(bundleName);
      expect(bundlePage.getName()
        .getAttribute('value')).toEqual(bundleName);
    });

    it('should save the bundle and go to moveBundle/show/', function(){
      bundlePage.clickSaveBundle();
      browser.driver.wait(function() {
        return menu.getCurrentUrl().then(function(url){
          return url.indexOf('/tdstm/moveBundle/show/') !== -1;
        });
      },8000).then(function(){
        expect(menu.getCurrentUrl())
          .toMatch(process.env.BASE_URL+'/tdstm/moveBundle/show/\\d\\d\\d\\d');
      });
    });

  }); // Create Page

  it('should be displayed created bundle confirmation message', function(){
    expect(bundlePage.getConfirmMsg().getText()).toEqual('MoveBundle '+bundleName+' created');
  });

  it('should updated Page title with the created Bundle name', function(){
    browser.driver.sleep(5000);
    expect(menu.getHeaderTitle().getText()).toEqual(' TransitionManager™ - Test Project : '+bundleName);
  });


  describe('delete bundle assigned to an asset', function(){
    describe('preconditions', function(){
      //bundle created
      // 
    }); //  preconditions
  
  }); // delete bundle assigned to an asset
}); // Bundles