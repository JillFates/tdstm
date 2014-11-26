'use strict';
var XLSJS = require('xlsjs'); // used to convert the xls file into a json
var fs = require('fs'); // used to delete a file
var File = function () {
  this.sheetsNames = ['Title','Devices','Applications','Databases','Storage','Dependencies','Room','Rack','Cabling','Comments'];
  this.devicesColHeader = [ 'assetId', 'Name', 'ShortName', 'S/N', 'AssetTag', 'Manufacturer', 'Model', 'Type', 'Description', 
  'IP', 'OS', 'SourceLoc', 'SourceRoom', 'SourceRack', 'SourcePos', 'Source Blade', 'Source Blade Position', 'VirtualHost', 
  'TargetLoc', 'TargetRoom', 'TargetRack', 'TargetPos', 'Target Blade', 'Target Blade Position', 'SupportType', 'Retire', 
  'MaintExp', 'Environment', 'Priority', 'Validation', 'MoveBundle', 'PlanStatus', 'Truck', 'Cart', 'Shelf', 'RailType', 
  'usize', 'DepGroup', 'ExternalRefId', 'Size', 'Scale', 'Rate Of Change', 'Application', 'Modified Date', 'Modified By', 
  'Custom1', 'Custom2', 'Custom3', 'Custom4', 'Custom5', 'Custom6', 'Custom7', 'Custom8', 'Custom9', 'Custom10', 'Custom11', 
  'Custom12', 'Custom13', 'Custom14', 'Custom15', 'Custom16', 'Custom17', 'Custom18', 'Custom19', 'Custom20', 'Custom21', 
  'Custom22', 'Custom23', 'Custom24', 'Custom25', 'Custom26', 'Custom27', 'Custom28', 'Custom29', 'Custom30', 'Custom31', 
  'Custom32', 'Custom33', 'Custom34', 'Custom35', 'Custom36', 'Custom37', 'Custom38', 'Custom39', 'Custom40', 'Custom41', 
  'Custom42', 'Custom43', 'Custom44', 'Custom45', 'Custom46', 'Custom47', 'Custom48', 'Custom49', 'Custom50', 'Custom51', 
  'Custom52', 'Custom53', 'Custom54', 'Custom55', 'Custom56', 'Custom57', 'Custom58', 'Custom59', 'Custom60', 'Custom61', 
  'Custom62', 'Custom63', 'Custom64', 'Custom65', 'Custom66', 'Custom67', 'Custom68', 'Custom69', 'Custom70', 'Custom71', 
  'Custom72', 'Custom73', 'Custom74', 'Custom75', 'Custom76', 'Custom77', 'Custom78', 'Custom79', 'Custom80', 'Custom81', 
  'Custom82', 'Custom83', 'Custom84', 'Custom85', 'Custom86', 'Custom87', 'Custom88', 'Custom89', 'Custom90', 'Custom91', 
  'Custom92', 'Custom93', 'Custom94', 'Custom95', 'Custom96'];
  this.applicationsColHeader = [ 'appId', 'Name', 'Vendor', 'Version', 'Technology', 'AccessType', 'Source', 'License', 
  'Description', 'SupportType', 'SME', 'SME2', 'BusinessUnit', 'AppOwner', 'Retire', 'MaintExp', 'Function', 'Environment', 
  'Criticality', 'Validation', 'MoveBundle', 'PlanStatus', 'TotalUsers', 'UserLocations', 'Frequency', 'RPO', 'RTO', 
  'DowntimeTolerance', 'Latency', 'TestProc', 'StartupProc', 'Url', 'DepGroup', 'ShutdownBy', 'ShutdownFixed', 
  'ShutdownDuration', 'StartupBy', 'StartupFixed', 'StartupDuration', 'TestingBy', 'TestingFixed', 'TestingDuration', 
  'ExternalRefId', 'Modified Date', 'Modified By', 'Custom1', 'Custom2', 'Custom3', 'Custom4', 'Custom5', 'Custom6', 
  'Custom7', 'Custom8', 'Custom9', 'Custom10', 'Custom11', 'Custom12', 'Custom13', 'Custom14', 'Custom15', 'Custom16', 
  'Custom17', 'Custom18', 'Custom19', 'Custom20', 'Custom21', 'Custom22', 'Custom23', 'Custom24', 'Custom25', 'Custom26', 
  'Custom27', 'Custom28', 'Custom29', 'Custom30', 'Custom31', 'Custom32', 'Custom33', 'Custom34', 'Custom35', 'Custom36', 
  'Custom37', 'Custom38', 'Custom39', 'Custom40', 'Custom41', 'Custom42', 'Custom43', 'Custom44', 'Custom45', 'Custom46', 
  'Custom47', 'Custom48', 'Custom49', 'Custom50', 'Custom51', 'Custom52', 'Custom53', 'Custom54', 'Custom55', 'Custom56', 
  'Custom57', 'Custom58', 'Custom59', 'Custom60', 'Custom61', 'Custom62', 'Custom63', 'Custom64', 'Custom65', 'Custom66', 
  'Custom67', 'Custom68', 'Custom69', 'Custom70', 'Custom71', 'Custom72', 'Custom73', 'Custom74', 'Custom75', 'Custom76', 
  'Custom77', 'Custom78', 'Custom79', 'Custom80', 'Custom81', 'Custom82', 'Custom83', 'Custom84', 'Custom85', 'Custom86', 
  'Custom87', 'Custom88', 'Custom89', 'Custom90', 'Custom91', 'Custom92', 'Custom93', 'Custom94', 'Custom95', 'Custom96' ];
  ['Title','Devices','Applications','Databases','Storage','Dependencies','Room','Rack','Cabling','Comments'];
  this.databasesColHeader = [ 'dbId', 'Name', 'Format', 'Size', 'Scale', 'Rate Of Change', 'Description', 'SupportType', 
  'Retire', 'MaintExp', 'Environment', 'Validation', 'MoveBundle', 'PlanStatus', 'DepGroup', 'ExternalRefId', 'Modified Date', 
  'Modified By', 'Custom1', 'Custom2', 'Custom3', 'Custom4', 'Custom5', 'Custom6', 'Custom7', 'Custom8', 'Custom9', 'Custom10', 
  'Custom11', 'Custom12', 'Custom13', 'Custom14', 'Custom15', 'Custom16', 'Custom17', 'Custom18', 'Custom19', 'Custom20', 
  'Custom21', 'Custom22', 'Custom23', 'Custom24', 'Custom25', 'Custom26', 'Custom27', 'Custom28', 'Custom29', 'Custom30', 
  'Custom31', 'Custom32', 'Custom33', 'Custom34', 'Custom35', 'Custom36', 'Custom37', 'Custom38', 'Custom39', 'Custom40', 
  'Custom41', 'Custom42', 'Custom43', 'Custom44', 'Custom45', 'Custom46', 'Custom47', 'Custom48', 'Custom49', 'Custom50',
  'Custom51', 'Custom52', 'Custom53', 'Custom54', 'Custom55', 'Custom56', 'Custom57', 'Custom58', 'Custom59', 'Custom60', 
  'Custom61', 'Custom62', 'Custom63', 'Custom64', 'Custom65', 'Custom66', 'Custom67', 'Custom68', 'Custom69', 'Custom70', 
  'Custom71', 'Custom72', 'Custom73', 'Custom74', 'Custom75', 'Custom76', 'Custom77', 'Custom78', 'Custom79', 'Custom80', 
  'Custom81', 'Custom82', 'Custom83', 'Custom84', 'Custom85', 'Custom86', 'Custom87', 'Custom88', 'Custom89', 'Custom90', 
  'Custom91', 'Custom92', 'Custom93', 'Custom94', 'Custom95', 'Custom96' ];
  this.storageColHeader = [ 'filesId', 'Name', 'Format', 'Size', 'Scale', 'Rate Of Change', 'Description', 'SupportType', 
  'Retire', 'MaintExp', 'Environment', 'Validation', 'MoveBundle', 'PlanStatus', 'DepGroup', 'ExternalRefId', 'Modified Date',
   'Modified By', 'Custom1', 'Custom2', 'Custom3', 'Custom4', 'Custom5', 'Custom6', 'Custom7', 'Custom8', 'Custom9', 'Custom10', 
   'Custom11', 'Custom12', 'Custom13', 'Custom14', 'Custom15', 'Custom16', 'Custom17', 'Custom18', 'Custom19', 'Custom20', 
   'Custom21', 'Custom22', 'Custom23', 'Custom24', 'Custom25', 'Custom26', 'Custom27', 'Custom28', 'Custom29', 'Custom30', 
   'Custom31', 'Custom32', 'Custom33', 'Custom34', 'Custom35', 'Custom36', 'Custom37', 'Custom38', 'Custom39', 'Custom40', 
   'Custom41', 'Custom42', 'Custom43', 'Custom44', 'Custom45', 'Custom46', 'Custom47', 'Custom48', 'Custom49', 'Custom50', 
   'Custom51', 'Custom52', 'Custom53', 'Custom54', 'Custom55', 'Custom56', 'Custom57', 'Custom58', 'Custom59', 'Custom60', 
   'Custom61', 'Custom62', 'Custom63', 'Custom64', 'Custom65', 'Custom66', 'Custom67', 'Custom68', 'Custom69', 'Custom70', 
   'Custom71', 'Custom72', 'Custom73', 'Custom74', 'Custom75', 'Custom76', 'Custom77', 'Custom78', 'Custom79', 'Custom80', 
   'Custom81', 'Custom82', 'Custom83', 'Custom84', 'Custom85', 'Custom86', 'Custom87', 'Custom88', 'Custom89', 'Custom90', 
   'Custom91', 'Custom92', 'Custom93', 'Custom94', 'Custom95', 'Custom96' ];
  this.dependenciesColHeader =[ 'AssetDependencyId', 'AssetId', 'AssetName', 'AssetType', 'DependentId', 'DependentName', 
  'DependentType', 'Type', 'DataFlowFreq', 'DataFlowDirection', 'status', 'comment', 'c1', 'c2', 'c3', 'c4' ];
  this.roomColHeader = [ 'roomId', 'Name', 'Location', 'Depth', 'Width', 'Source', 'Address', 'City', 'Country', 
  'StateProv', 'Postal Code', 'Date Created', 'Last Updated' ];
  this.rackColHeader = [ 'rackId', 'Tag', 'Location', 'Model', 'Room', 'Source', 'RoomX', 'RoomY', 'PowerA', 'PowerB', 
  'PowerC', 'Type', 'Front' ];
  this.cablingColHeader= [ 'CableType', 'Asset Id', 'Asset Name', 'Connector', 'Asset Id', 'Asset Name', 'Connector', 
  'Cable Comment', 'Cable Color', 'Potentially Room', 'Cable Status', 'Location/Room' ];
  this.commentsColHeader = [ 'commentId', 'assetId', 'Category', ' Created date', 'Created by', 'Comment' ];
};
File.prototype.getFileObject = function(filePath) {
  return XLSJS.readFile(filePath);
};
File.prototype.getSheetsName = function(object) {
  return object.SheetNames;
};

File.prototype.getExpectedColHeaders = function(page) {
  var that = this;
  var d = {
    'Devices': function(){
      return that.devicesColHeader;
    },
    'Applications': function(){
      return that.applicationsColHeader;
    },
    'Databases': function(){
      return that.databasesColHeader;
    },
    'Storage': function(){
      return that.storageColHeader;
    },
    'Dependencies': function(){
      return that.dependenciesColHeader;
    },
    'Room': function(){
      return that.roomColHeader;
    },
    'Rack': function(){
      return that.rackColHeader;
    },
    'Cabling': function(){
      return that.cablingColHeader;
    },
    'Comments': function(){
      return that.commentsColHeader;
    }
  };
  return d[page]();
};

File.prototype.generatColName = function(top) {
  var list = [],
      j = 0,
      lett = '',
      k = 0;

  for (var i = 0; i < top; ++i) {
    if (i !== 0 && i % 26 === 0 ) {
      j=0;
      lett = list[k];
      ++k;
    }
    list[i] = lett + String.fromCharCode(j + 65);
    ++j;
  }
  return list;
};

File.prototype.getPageSize = function(object,page) {
  return object.Sheets[page]['!range']['e']['r'];
};

File.prototype.getColumnsHead = function(object,page) {
  var colEnd = object.Sheets[page]['!range']['e']['c'];
  var list = this.generatColName(colEnd);
  var head = [];
  var i =0;
  var rowN =1;
  list.forEach(function(colName){
    if('Cabling' === page){
      rowN =2;
    }
    head[i] =object.Sheets[page][colName+rowN].v;
    ++i;
  });
  return head;
};

// File.prototype.getColumn = function(object, page, col) {
  
// };

File.prototype.delete = function(filePath) {
  fs.unlinkSync(filePath);
};

File.prototype.existsSync = function(filePath) {
  return fs.existsSync(filePath);
};

module.exports = File;