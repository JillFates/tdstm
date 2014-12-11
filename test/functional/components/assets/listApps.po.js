'use strict';
var ListApps = function(){
  this.titleh =  $('h1');
  this.createAppBtn = $('input[value="Create App"]');
  // this.createServerBtn = $('[onclick="createAssetDetails(\'Device\')"]');
  this.createServerBtn = $('[onclick="EntityCrud.showAssetCreateView(\'DEVICE\');"]');
  this.searchNamefield = element(by.id('gs_assetName'));
  // this.appsOnList =  $$('[role="grid"] tbody tr.ui-widget-content');
  this.confirmMsg = $('div#messageId');
  this.devicesLoadingId = 'load_assetListIdGrid';
  this.applicationLoadingId = 'load_applicationIdGrid';
  this.databaseLoadingId = 'load_databaseIdGrid';
  this.storageLoadingId = 'load_storageIdGrid';
  this.dependencyLoadingId ='load_dependencyGridIdGrid';

  this.getLoadingStyle = function(asse){
    return element(by.id(asse)).getAttribute('style').then(function(attClass){
      return (attClass.search('display: none;') !== -1);
    });      
  };
  this.getColumnsHeaders =function(){
    return browser.executeScript('return $("[role=\'grid\'] [class=\'ui-jqgrid-labels ui-sortable\'] th:visible")');
  };

  this.getColumnsWithHeaderSelector =function(){
    return browser.executeScript('return $("[role=\'grid\'] [class=\'ui-jqgrid-labels ui-sortable\'] th:visible img")');
  };

  this.isLoadingHidden = function (device) {
    /* Values for device base on asset type
    application
    assetList
    storage
    database */
    return browser.wait(function () {
      return element(by.id('load_'+device+'IdGrid')).isDisplayed().then(function (valor) {
        return !valor;
      });
    }).then(function () {
      return true;
    });
  };

  this.getListItems = function(expListItems){
    return browser.wait(function () {
      return $$('[role="grid"] tbody tr.ui-widget-content').then(function(list){
        return list.length===expListItems;
      });
    }).then(function () {
      return true;
    });

  };

  this.verifySearchResults = function(count,device){
    var that  =this;
    var asse = 'assetnotdefine';
    return browser.wait(function(){
      var d = {
        'device': function(){
            asse = that.devicesLoadingId;
        },
        'application':function(){
          asse = that.applicationLoadingId;
        },
        'database':function(){
          asse = that.databaseLoadingId;
        },
        'storage':function(){
          asse = that.storageLoadingId;
        },
        'dependency':function(){
          asse =that.dependencyLoadingId;
        }
      };
      d[device]();
      return that.getLoadingStyle(asse)&&that.getListItems(count);
    }).then(function(){
        return $$('[role="grid"] tbody tr.ui-widget-content').then(function(list){
          return list;
      });
    },function(){
      return 'No results found';}
    );
  };

  this.clickOnTaskIcon = function(appId){
    var addtaskIcon =  $('#icon_task_'+appId);
    addtaskIcon.click();
  };
  this.isTasksIconDisplayed = function(appId){
    var that = this;
    return browser.wait(function(){
      return that.getTaskIcon(appId).getAttribute('src').then(function(ico){
        return ico === process.env.BASE_URL+'/tdstm/icons/table_multiple.png';
      });
    }).then(function(){
      return true;
    });
  };
  this.isAddTaskIconDisplayed = function(appId){
    var that = this;
    return browser.wait(function(){
      return that.getTaskIcon(appId).getAttribute('src').then(function(ico){
        return ico === process.env.BASE_URL+'/tdstm/icons/table_add.png';
      });
    }).then(function(){
      return true;
    });
  };
  this.getTaskIcon = function(appId){
    return $('#icon_task_'+appId +' img');
  };
  
  this.clickOnCommentIcon = function(appId){
    var addCommentIcon =  $('#icon_comment_'+appId);
    addCommentIcon.click();
  };
  
  this.getCommentIcon = function(appId){
    return $('#icon_comment_'+appId +' img');
  };

  this.isCommentsIconDisplayed = function(appId){
    var that = this;
    return browser.wait(function(){
      return that.getCommentIcon(appId).getAttribute('src').then(function(ico){
        return ico === process.env.BASE_URL+'/tdstm/icons/comments.png';
      });
    }).then(function(){
      return true;
    });
  };

  this.isAddCommentIconDisplayed = function(appId){
    var that = this;
    return browser.wait(function(){
      return that.getCommentIcon(appId).getAttribute('src').then(function(ico){
        return ico === process.env.BASE_URL+'/tdstm/icons/comment_add.png';
      });
    }).then(function(){
      return true;
    });
  };
  
};
module.exports = ListApps;