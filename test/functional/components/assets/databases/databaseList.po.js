'use strict';
var  ListApps = require('../listApps.po.js');
var DatabaseList = function(){
  var extendListApp = new ListApps();
  this.titleh = extendListApp.titleh;
  // this.createDBBtn = $('input[value="Create DB"]');
  this.createDBBtn = $('input[onclick="EntityCrud.showAssetCreateView(\'DATABASE\');"]');
};
module.exports = DatabaseList;