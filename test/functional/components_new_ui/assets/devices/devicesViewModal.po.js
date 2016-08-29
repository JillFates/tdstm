'use strict';
var ViewAsset = require('../modals/assetView.po.js');

var DeviceViewModal = function(){
  this.altNameLabel = this.viewModal.$('[for="shortName"]');
  this.rackCabinetLabel = this.viewModal.$('[for="sourceRackId"]');
  this.chassisBladeLabel = this.viewModal.$('[for="sourceChassisId"]');
};
DeviceViewModal.prototype = new ViewAsset();

module.exports = DeviceViewModal;