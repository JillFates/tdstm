'use strict';
var ViewAsset = require('../modals/assetView.po.js');

var DeviceViewModal = function(){
  this.altNameLabel = this.viewModal.$('[for="shortName"]');
};
DeviceViewModal.prototype = new ViewAsset();

module.exports = DeviceViewModal;