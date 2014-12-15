'use strict';
var ListAssets = function(){
  this.titleh =  $('h1');
  this.searchNamefield = element(by.id('gs_assetName'));
  this.confirmMsg = $('div#messageId');
  this.storageLoadingId = 'load_storageIdGrid';
  this.dependencyLoadingId ='load_dependencyGridIdGrid';
};

ListAssets.prototype = {};

// ListAssets.prototype.getLoadingStyle = function(asse) {
//   return element(by.id(asse)).getAttribute('style').then(function(attClass){
//     return (attClass.search('display: none;') !== -1);
//   });      
// };

ListAssets.prototype.getColumnsHeaders = function() {
  return browser.executeScript('return $("[role=\'grid\'] [class=\'ui-jqgrid-labels ui-sortable\'] th:visible")');
};

ListAssets.prototype.getColumnsWithHeaderSelector = function() {
  return browser.executeScript('return $("[role=\'grid\'] [class=\'ui-jqgrid-labels ui-sortable\'] th:visible img")');
};

// ListAssets.prototype.isLoadingHidden = function (device) {
//   /* Values for device base on asset type
//   application
//   assetList
//   storage
//   database */
//   return browser.wait(function () {
//     return element(by.id('load_'+device+'IdGrid')).isDisplayed().then(function (valor) {
//       return !valor;
//     });
//   }).then(function () {
//     return true;
//   });
// };

// ListAssets.prototype.getListItems = function(expListItems,appName){
//   return browser.wait(function () {
//     return $$('[role="grid"] tbody tr.ui-widget-content').then(function(list){
//       if(list.length===expListItems){
//         if(list.length === 1){
//           return list[0].$('[aria-describedby="applicationIdGrid_assetName"]').getText().then(function (text) {
//             console.log('text','"'+text+'"');
//             console.log('appName','"'+appName+'"');
//             return text === appName;
//           });
//         }else {
//           return true;
//         }
//         // return list.length===expListItems;
//       }
//     });
//   }).then(function () {
//     return true;
//   });

// };

// ListAssets.prototype.verifySearchResults = function(count,device,appName){
//   var that  =this;
//   var asse = 'assetnotdefine';
//   return browser.wait(function(){
//     var d = {
//       'device': function(){
//           asse = that.devicesLoadingId;
//       },
//       'application':function(){
//         asse = that.applicationLoadingId;
//       },
//       'database':function(){
//         asse = that.databaseLoadingId;
//       },
//       'storage':function(){
//         asse = that.storageLoadingId;
//       },
//       'dependency':function(){
//         asse =that.dependencyLoadingId;
//       }
//     };
//     d[device]();
//     return that.getLoadingStyle(asse)&&that.getListItems(count,appName);
//   }).then(function(){
//       return $$('[role="grid"] tbody tr.ui-widget-content').then(function(list){
//         return list;
//     });
//   },function(){
//     return 'No results found';}
//   );
// };

ListAssets.prototype.clickOnTaskIcon = function(appId){
  var addtaskIcon =  $('#icon_task_'+appId);
  addtaskIcon.click();
};

ListAssets.prototype.isTasksIconDisplayed = function(appId){
  var that = this;
  return browser.wait(function(){
    return that.getTaskIcon(appId).getAttribute('src').then(function(ico){
      return ico === process.env.BASE_URL+'/tdstm/icons/table_multiple.png';
    });
  }).then(function(){
    return true;
  });
};

ListAssets.prototype.isAddTaskIconDisplayed = function(appId){
  var that = this;
  return browser.wait(function(){
    return that.getTaskIcon(appId).getAttribute('src').then(function(ico){
      return ico === process.env.BASE_URL+'/tdstm/icons/table_add.png';
    });
  }).then(function(){
    return true;
  });
};

ListAssets.prototype.getTaskIcon = function(appId){
  return $('#icon_task_'+appId +' img');
};
  
ListAssets.prototype.clickOnCommentIcon = function(appId){
  var addCommentIcon =  $('#icon_comment_'+appId);
  addCommentIcon.click();
};
  
ListAssets.prototype.getCommentIcon = function(appId){
  return $('#icon_comment_'+appId +' img');
};

ListAssets.prototype.isCommentsIconDisplayed = function(appId){
  var that = this;
  return browser.wait(function(){
    return that.getCommentIcon(appId).getAttribute('src').then(function(ico){
      return ico === process.env.BASE_URL+'/tdstm/icons/comments.png';
    });
  }).then(function(){
    return true;
  });
};

ListAssets.prototype.isAddCommentIconDisplayed = function(appId){
  var that = this;
  return browser.wait(function(){
    return that.getCommentIcon(appId).getAttribute('src').then(function(ico){
      return ico === process.env.BASE_URL+'/tdstm/icons/comment_add.png';
    });
  }).then(function(){
    return true;
  });
};
  
module.exports = ListAssets;