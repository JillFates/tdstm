'use strict';
var Person = function () {
  this.id = '';
  this.loginId = 0;
  this.application = {
    'bySme1': [],
    'bySme2': []
  };
  this.assetComment = {
    'createdBy':[],
    'resolvedBy':[],
    'ownerId':[],
    'assignedToId':[]
  };
  this.assetDependency = {
    'createdBy': [],
    'updatedBy': []
  };
  this.assetEntity = {
    'ownerId': [],
    'modifiedBy':[],
    'appOwnerId': []
  };
  this.commentNote = {
    'createdById': []
  };
  this.exceptionDates = {
    'personId':[]
  };
  this.model = {
    'createdBy': [],
    'updatedBy':[],
    'validatedBy': []
  }; 
  this.modelSync = {
    'createdById':[],
    'updatedById':[],
    'ValidatedById':[]
  };
  this.moveEventNews ={
    'createdBy':[]
  };         
  this.moveEventStaff = {
    'personId':[]
  };


  // model_sync_batch = { userLoginId,createdById,userlogin_Id}  
  //user_login_id
  this.assetTransition ={
    'userLoginId': []
  };
  this.dataTransferBatch ={
    'userLoginId':[]
  };


};

module.exports = Person;
