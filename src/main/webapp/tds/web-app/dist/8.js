(window.webpackJsonp=window.webpackJsonp||[]).push([[8],{1126:function(t,e,r){"use strict";r.r(e);var o=r(1),i=r(0),n=r(10),s=r(15),a=r(121),c=r(200),l=r(1114),u=r(1115),p=r(377),h=r(1129),d=r(1118),f=r(1117),m=r(65),S=r(269),I=r(375),b=r(270),g=r(77),R=r(140),C=r(84),v=r(55),E=r(75),T=r(38),P=r(28),B=r(85),A=r(54),O=r(67),y=r(69),_=r(271),L=r(289),F=r(327),M=r(341),N=r(159),U=function(t){function e(e,r,o,i){var n=t.call(this,"#import-batch-record-dialog")||this;return n.importBatch=e,n.batchRecord=r,n.promptService=o,n.translatePipe=i,n.batchRecordUpdatedFlag=!1,n.isWindowMaximized=!1,n.modalOptions={isFullScreen:!0,isResizable:!0,isDraggable:!0},n}return o.c(e,t),e.prototype.onCancelCloseDialog=function(){var t=this;this.detailFieldsComponent.areOverrideValuesDirty()?this.promptService.open(this.translatePipe.transform(P.y),this.translatePipe.transform(P.x),"Confirm","Cancel").then(function(e){e&&t.close(t.batchRecordUpdatedFlag?"reload":null)},function(t){return console.log("confirm rejected",t)}):this.close(this.batchRecordUpdatedFlag?"reload":null)},e.prototype.keyDownHandler=function(t){t&&t.code===P.p.ESCAPE&&this.onCancelCloseDialog()},e.prototype.onUpdateSuccess=function(){this.batchRecordUpdatedFlag=!0},e.prototype.onBatchRecordUpdated=function(t){this.batchRecord=t.batchRecord},e.prototype.maximizeWindow=function(){this.isWindowMaximized=!0},e.prototype.restoreWindow=function(){this.isWindowMaximized=!1},o.b([Object(i.lb)("detailFieldsComponent"),o.d("design:type",N.b)],e.prototype,"detailFieldsComponent",void 0),e=o.b([Object(i.n)({selector:"import-batch-record-dialog",templateUrl:"../tds/web-app/app-js/modules/importBatch/components/record/import-batch-record-dialog.component.html",host:{"(keydown)":"keyDownHandler($event)"}}),o.d("design:paramtypes",[R.c,L.c,O.b,y.a])],e)}(T.c),H=r(106),D=function(){function t(t,e,r,o,i){this.importBatchModel=t,this.importBatchService=e,this.activeDialog=r,this.dialogService=o,this.userPreferenceService=i,this.BatchStatus=R.a,this.selectableSettings={mode:"single",checkboxOnly:!1},this.checkboxSelectionConfig={useColumn:"id"},this.batchRecordsFilter={options:[{id:1,name:"All"},{id:2,name:"Pending",filters:[{column:"status.label",value:"Pending"}]},{id:3,name:"Pending with Errors",filters:[{column:"status.label",value:"Pending"},{column:"errorCount",value:1,operator:"gte"}]},{id:4,name:"Ignored",filters:[{column:"status.label",value:"Ignored"}]},{id:5,name:"Completed",filters:[{column:"status.label",value:"Completed"}]}],selected:{id:1,name:"All"}},this.batchRecordsUpdatedFlag=!1,this.NULL_OBJECT_PIPE=M.a,this.importBatchPreferences={},this.batchRecords=[],this.prepareColumnsModel(),this.onLoad()}return t.prototype.onLoad=function(){var t=this;this.dateTimeFormat=this.userPreferenceService.getUserDateTimeFormat(),this.userPreferenceService.getSinglePreference(A.b.IMPORT_BATCH_PREFERENCES).subscribe(function(e){if(e){t.importBatchPreferences=JSON.parse(e);var r=t.batchRecordsFilter.options.find(function(e){return e.name===t.importBatchPreferences[A.a.RECORDS_FILTER]});r&&(t.batchRecordsFilter.selected=r)}},function(t){console.error(t)})},t.prototype.ngOnInit=function(){this.loadImportBatchRecords()},t.prototype.loadImportBatchRecords=function(){var t=this;this.importBatchService.getImportBatchRecords(this.importBatchModel.id).subscribe(function(e){e.status===B.a.API_SUCCESS?(t.batchRecords=e.data,t.dataGridOperationsHelper=new _.a(t.batchRecords,[],t.selectableSettings,t.checkboxSelectionConfig),t.onStatusFilter(t.batchRecordsFilter.selected,!0)):t.handleError(e.errors[0]?e.errors[0]:"error loading Batch Records")},function(e){return t.handleError(e)})},t.prototype.reloadSingleBatchRecord=function(t){var e=this;this.importBatchService.getImportBatchRecordUpdated(this.importBatchModel.id,t.id).subscribe(function(r){r?Object.assign(t,r):e.loadImportBatchRecords()})},t.prototype.prepareColumnsModel=function(){this.columnsModel=new L.b;var t=this.importBatchModel,e=t.fieldNameList,r=t.fieldLabelMap,o=e.map(function(t){var e=new F.f;return e.label=r&&r[t]||t,e.properties=["currentValues",t],e.width=130,e.cellStyle={"max-height":"20px"},e.type="dynamicValue",e});this.columnsModel.columns=this.columnsModel.columns.concat(o)},t.prototype.openBatchRecordDetail=function(t){var e=this;if(0!==t.columnIndex){this.dataGridOperationsHelper.selectCell(t);var r=t.dataItem;r&&r.id&&this.dialogService.extra(U,[{provide:R.c,useValue:this.importBatchModel},{provide:L.c,useValue:r}],!1,!1).then(function(t){"reload"===t&&(e.reloadSingleBatchRecord(r),e.batchRecordsUpdatedFlag=!0)}).catch(function(t){console.log("dismissed")})}},t.prototype.handleError=function(t){console.log(t)},t.prototype.cancelCloseDialog=function(){this.activeDialog.close(this.batchRecordsUpdatedFlag)},t.prototype.preProcessFilter=function(t){this.clearStatusFilter(t),this.dataGridOperationsHelper.onFilter(t)},t.prototype.preProcessClear=function(t){this.clearStatusFilter(t),this.dataGridOperationsHelper.clearValue(t)},t.prototype.clearStatusFilter=function(t){"status.label"!==t.property&&"errorCount"!==t.property||(this.batchRecordsFilter.selected={id:1,name:"All"})},t.prototype.onStatusFilter=function(t,e){void 0===e&&(e=!1);for(var r=function(t){var e=o.columnsModel.columns.find(function(e){return e.property===t});e.filter=null,o.dataGridOperationsHelper.clearValue(e)},o=this,i=0,n=["status.label","errorCount"];i<n.length;i++){r(n[i])}if(1!==t.id)for(var s=function(e){var r=a.columnsModel.columns.find(function(t){return t.property===e.column});r&&(r.filter=e.value,a.dataGridOperationsHelper.onFilter(r,3===t.id?"gte":null))},a=this,c=0,l=t.filters;c<l.length;c++){s(l[c])}e||(this.importBatchPreferences[A.a.RECORDS_FILTER]=t.name,this.userPreferenceService.setPreference(A.b.IMPORT_BATCH_PREFERENCES,JSON.stringify(this.importBatchPreferences)).subscribe(function(t){}))},t.prototype.onIgnore=function(){var t=this,e=this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();this.importBatchService.ignoreBatchRecords(this.importBatchModel.id,e).subscribe(function(e){e.status===B.a.API_SUCCESS?(t.loadImportBatchRecords(),t.batchRecordsUpdatedFlag=!0):t.handleError(e.errors[0]?e.errors[0]:"error on bulk ignore batch records.")},function(e){return t.handleError(e)})},t.prototype.onProcess=function(){var t=this,e=this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();this.importBatchService.processBatchRecords(this.importBatchModel.id,e).subscribe(function(e){e.status===B.a.API_SUCCESS?(t.loadImportBatchRecords(),t.batchRecordsUpdatedFlag=!0):t.handleError(e.errors[0]?e.errors[0]:"error on bulk Process batch records.")},function(e){return t.handleError(e)})},t.prototype.batchRecordCanAction=function(t){return t.status.code===R.a.PENDING||t.status.code===R.a.IGNORED},t.prototype.keyDownHandler=function(t){t&&t.code===P.p.ESCAPE&&this.cancelCloseDialog()},t.prototype.getInitOrValue=function(t,e){var r=!t.currentValues[e.properties[1]]||H.a.isEmptyObject(t.currentValues[e.properties[1]]);return r&&t.init?!t.init[e.properties[1]]||H.a.isEmptyObject(t.init[e.properties[1]])?"(null)":t.init[e.properties[1]]:r?"(null)":t.currentValues[e.properties[1]]},t.prototype.hasInitVal=function(t,e){return!(t.currentValues[e.properties[1]]&&!H.a.isEmptyObject(t.currentValues[e.properties[1]])||!t.init)&&!(!t.init[e.properties[1]]||H.a.isEmptyObject(t.init[e.properties[1]]))},t=o.b([Object(i.n)({selector:"import-batch-detail-dialog",templateUrl:"../tds/web-app/app-js/modules/importBatch/components/detail/import-batch-detail-dialog.component.html",host:{"(keydown)":"keyDownHandler($event)"}}),o.d("design:paramtypes",[R.c,p.a,T.a,T.b,A.c])],t)}(),G=function(){function t(t,e,r,o,i,n,s,a){this.dialogService=t,this.importBatchService=e,this.permissionService=r,this.promptService=o,this.translatePipe=i,this.notifierService=n,this.userPreferenceService=s,this.route=a,this.BatchStatus=R.a,this.importBatchPreferences={},this.selectableSettings={mode:"single",checkboxOnly:!1},this.initialSort=[{dir:"desc",field:"dateCreated"}],this.checkboxSelectionConfig={useColumn:"id"},this.viewArchived=!1,this.PROGRESS_MAX_TRIES=10,this.PROGRESS_CHECK_INTERVAL=3e3,this.STOP_BATCH_CONFIRMATION="IMPORT_BATCH.LIST.STOP_BATCH_CONFIRMATION",this.ARCHIVE_ITEM_CONFIRMATION="IMPORT_BATCH.LIST.ARCHIVE_ITEM_CONFIRMATION",this.ARCHIVE_ITEMS_CONFIRMATION="IMPORT_BATCH.LIST.ARCHIVE_ITEMS_CONFIRMATION",this.UNARCHIVE_ITEM_CONFIRMATION="IMPORT_BATCH.LIST.UNARCHIVE_ITEM_CONFIRMATION",this.UNARCHIVE_ITEMS_CONFIRMATION="IMPORT_BATCH.LIST.UNARCHIVE_ITEMS_CONFIRMATION",this.runningBatches=[],this.queuedBatches=[],this.onLoad()}return t.prototype.onLoad=function(){var t=this;this.userTimeZone=this.userPreferenceService.getUserTimeZone(),this.columnsModel=new R.b,this.canRunActions()||this.columnsModel.columns.splice(0,1),this.userPreferenceService.getSinglePreference(A.b.IMPORT_BATCH_PREFERENCES).subscribe(function(e){t.getUnarchivedBatches().then(function(r){var o;e?(t.importBatchPreferences=JSON.parse(e),o=parseInt(t.importBatchPreferences[A.a.LIST_SIZE],0)):o=P.l,t.dataGridOperationsHelper=new _.a(r,t.initialSort,t.selectableSettings,t.checkboxSelectionConfig,o),t.preSelectBatch(),t.setRunningLoop(),t.setQueuedLoop()})})},t.prototype.preSelectBatch=function(){var t=this;this.route.params.subscribe(function(e){var r=e.id?parseInt(e.id,0):null,o=r?t.dataGridOperationsHelper.resultSet.find(function(t){return t.id===r}):null;if(r&&o){var i={dataItem:o};t.openBatchDetail(i)}})},t.prototype.onClickTemplate=function(t){t.target&&t.target.parentNode&&t.target.parentNode.click()},t.prototype.reloadBatchList=function(){var t=this;this.getUnarchivedBatches().then(function(e){t.dataGridOperationsHelper.reloadData(e),t.clearLoopsLists()})},t.prototype.reloadImportBatch=function(t){this.importBatchService.getImportBatch(t.id).subscribe(function(e){e.status===B.a.API_SUCCESS&&Object.assign(t,e.data)})},t.prototype.getUnarchivedBatches=function(){var t=this;return new Promise(function(e,r){t.importBatchService.getImportBatches().subscribe(function(r){if("success"===r.status){var o=r.data.filter(function(t){return!t.archived});e(o)}else t.handleError(r.errors?r.errors[0]:null),e([])},function(r){t.handleError(r),e([])})})},t.prototype.loadArchivedBatchList=function(){var t=this;this.importBatchService.getImportBatches().subscribe(function(e){if("success"===e.status){var r=e.data.filter(function(t){return t.archived});t.dataGridOperationsHelper.reloadData(r),t.clearLoopsLists()}else t.handleError(e.errors?e.errors[0]:null)})},t.prototype.openBatchDetail=function(t){var e=this,r=t.dataItem;0!==t.columnIndex&&(this.dataGridOperationsHelper.selectCell(t),this.dialogService.open(D,[{provide:R.c,useValue:r}],P.e.XXL).then(function(t){t&&e.reloadImportBatch(r)}).catch(function(t){console.log("Dismissed Dialog")}))},t.prototype.confirmArchive=function(){var t=this,e=this.dataGridOperationsHelper.getCheckboxSelectedItems().map(function(t){return parseInt(t,10)});this.promptService.open(this.translatePipe.transform(P.y),this.translatePipe.transform(1===e.length?this.ARCHIVE_ITEM_CONFIRMATION:this.ARCHIVE_ITEMS_CONFIRMATION),"Confirm","Cancel").then(function(e){e&&t.onArchiveBatch()},function(t){return console.log("confirm rejected",t)})},t.prototype.onArchiveBatch=function(){var t=this,e=this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();this.importBatchService.archiveImportBatches(e).subscribe(function(e){e.status===B.a.API_SUCCESS?(t.reloadBatchList(),t.dataGridOperationsHelper.unSelectAllCheckboxes()):t.handleError(e.errors?e.errors[0]:null)},function(e){return t.handleError(e)})},t.prototype.confirmUnarchive=function(){var t=this,e=this.dataGridOperationsHelper.getCheckboxSelectedItems().map(function(t){return parseInt(t,10)});this.promptService.open(this.translatePipe.transform(P.y),this.translatePipe.transform(1===e.length?this.UNARCHIVE_ITEM_CONFIRMATION:this.UNARCHIVE_ITEMS_CONFIRMATION),"Confirm","Cancel").then(function(e){e&&t.onUnarchiveBatch()},function(t){return console.log("confirm rejected",t)})},t.prototype.onUnarchiveBatch=function(){var t=this,e=this.dataGridOperationsHelper.getCheckboxSelectedItemsAsNumbers();this.importBatchService.unArchiveImportBatches(e).subscribe(function(e){e.status===B.a.API_SUCCESS?(t.loadArchivedBatchList(),t.dataGridOperationsHelper.unSelectAllCheckboxes()):t.handleError(e.errors?e.errors[0]:null)},function(e){return t.handleError(e)})},t.prototype.confirmDelete=function(){var t=this,e=this.dataGridOperationsHelper.getCheckboxSelectedItems().map(function(t){return parseInt(t,10)});this.promptService.open(this.translatePipe.transform(P.y),this.translatePipe.transform(1===e.length?P.A:P.z),"Confirm","Cancel").then(function(e){e&&t.onDeleteBatch()},function(t){return console.log("confirm rejected",t)})},t.prototype.onDeleteBatch=function(){var t=this,e=this.dataGridOperationsHelper.getCheckboxSelectedItems().map(function(t){return parseInt(t,10)});this.importBatchService.deleteImportBatches(e).subscribe(function(e){e.status===B.a.API_SUCCESS?t.viewArchived?(t.loadArchivedBatchList(),t.dataGridOperationsHelper.unSelectAllCheckboxes()):(t.reloadBatchList(),t.dataGridOperationsHelper.unSelectAllCheckboxes()):t.handleError(e.errors?e.errors[0]:null)},function(e){return t.handleError(e)})},t.prototype.handleError=function(t){this.notifierService.broadcast({name:E.b.DANGER,message:t})},t.prototype.onToggleViewArchived=function(){this.dataGridOperationsHelper.unSelectAllCheckboxes(),this.viewArchived?this.loadArchivedBatchList():this.reloadBatchList()},t.prototype.onPlayButton=function(t){var e=this,r=[t.id];this.importBatchService.queueImportBatches(r).subscribe(function(r){r.status===B.a.API_SUCCESS&&1===r.data.QUEUE?(t.status.code=R.a.QUEUED.toString(),t.status.label="Queued",e.addToQueuedBatchesLoop(t)):(e.reloadBatchList(),e.handleError(r.errors?r.errors[0]:null))},function(t){return e.handleError(t)})},t.prototype.onEjectButton=function(t){var e=this,r=[t.id];this.importBatchService.ejectImportBatches(r).subscribe(function(r){r.status===B.a.API_SUCCESS?(t.status.code=R.a.PENDING.toString(),t.status.label="Pending",e.removeBatchFromQueuedLoop(t)):e.handleError(r.errors?r.errors[0]:null)},function(t){return e.handleError(t)})},t.prototype.onStopButton=function(t){this.confirmStopAction(t)},t.prototype.confirmStopAction=function(t){var e=this;this.promptService.open(this.translatePipe.transform(P.y),this.translatePipe.transform(this.STOP_BATCH_CONFIRMATION),"Confirm","Cancel").then(function(r){r&&e.stopBatch(t)})},t.prototype.stopBatch=function(t){var e=this,r=[t.id];this.importBatchService.stopImportBatch(r).subscribe(function(r){r.status===B.a.API_SUCCESS?(e.removeBatchFromRunningLoop(t),e.reloadImportBatch(t)):e.handleError(r.errors[0]?r.errors[0]:"Error on stop import batch endpoint.")},function(t){return e.handleError(t)})},t.prototype.canRunActions=function(){return this.permissionService.hasPermission(C.a.DataTransferBatchProcess)},t.prototype.canBulkDelete=function(){return this.permissionService.hasPermission(C.a.DataTransferBatchDelete)},t.prototype.canBulkArchive=function(){return this.permissionService.hasPermission(C.a.DataTransferBatchProcess)},t.prototype.preProcessFilter=function(t){if("status"===t.property){var e=o.a({},t);e.property="status.label",this.dataGridOperationsHelper.onFilter(e)}else this.dataGridOperationsHelper.onFilter(t)},t.prototype.setRunningLoop=function(){this.runningBatches=this.dataGridOperationsHelper.resultSet.filter(function(t){return t.status.code===R.a.RUNNING.toString()}),this.runningLoop()},t.prototype.runningLoop=function(){var t=this;if(0===this.runningBatches.length)this.batchRunningLoop=setTimeout(function(){t.setRunningLoop()},this.PROGRESS_CHECK_INTERVAL);else for(var e=function(e){r.importBatchService.getImportBatchProgress(e.id).subscribe(function(r){if(r.status===B.a.API_SUCCESS){e.currentProgress=r.data.progress?r.data.progress:0;var o=r.data.lastUpdated;e.stalledCounter=e.lastUpdated===o?e.stalledCounter+=1:0,e.stalledCounter>=t.PROGRESS_MAX_TRIES?(e.status.code=R.a.STALLED.toString(),e.status.label="Stalled",t.removeBatchFromRunningLoop(e)):e.currentProgress>=100?(e.status=r.data.status,e.currentProgress=0,t.removeBatchFromRunningLoop(e),t.reloadImportBatch(e)):e.lastUpdated=r.data.lastUpdated}else t.handleError(r.errors[0]?r.errors[0]:"error on get batch progress");t.batchRunningLoop=setTimeout(function(){t.runningLoop()},t.PROGRESS_CHECK_INTERVAL)},function(e){clearTimeout(t.batchRunningLoop),t.handleError(e)})},r=this,o=0,i=this.runningBatches;o<i.length;o++){e(i[o])}},t.prototype.setQueuedLoop=function(){this.queuedBatches=this.dataGridOperationsHelper.resultSet.filter(function(t){return t.status.code===R.a.QUEUED.toString()}),this.queuedLoop(this.queuedBatches.slice())},t.prototype.queuedLoop=function(t){var e=this;if(0===t.length)this.batchQueuedLoop=setTimeout(function(){e.setQueuedLoop()},this.PROGRESS_CHECK_INTERVAL);else for(var r=function(r){var i=t[r];o.importBatchService.getImportBatch(i.id).subscribe(function(o){o.status===B.a.API_SUCCESS&&o.data.status.code!==R.a.QUEUED&&(i.status.code=o.data.status.code,i.status.label=o.data.status.label,e.removeBatchFromQueuedLoop(i),i.status.code===R.a.RUNNING.toString()&&e.addToRunningBatchesLoop(i)),r===t.length-1&&(e.batchQueuedLoop=setTimeout(function(){e.setQueuedLoop()},e.PROGRESS_CHECK_INTERVAL))},function(t){clearTimeout(e.batchQueuedLoop),e.handleError(t)})},o=this,i=0;i<t.length;i++)r(i)},t.prototype.removeBatchFromRunningLoop=function(t){var e=this.runningBatches.findIndex(function(e){return e.id===t.id});this.runningBatches.splice(e,1)},t.prototype.removeBatchFromQueuedLoop=function(t){var e=this.queuedBatches.findIndex(function(e){return e.id===t.id});this.queuedBatches.splice(e,1)},t.prototype.addToRunningBatchesLoop=function(t){this.runningBatches.findIndex(function(e){return e.id===t.id})<0&&this.runningBatches.push(t)},t.prototype.addToQueuedBatchesLoop=function(t){this.queuedBatches.findIndex(function(e){return e.id===t.id})<0&&this.queuedBatches.push(t)},t.prototype.clearLoopsLists=function(){this.runningBatches=[],this.queuedBatches=[]},t.prototype.onPageChange=function(t){this.importBatchPreferences[A.a.LIST_SIZE]=t.take.toString(),this.userPreferenceService.setPreference(A.b.IMPORT_BATCH_PREFERENCES,JSON.stringify(this.importBatchPreferences)).subscribe(function(t){}),this.dataGridOperationsHelper.pageChange(t)},t.prototype.ngOnDestroy=function(){clearTimeout(this.batchRunningLoop),clearTimeout(this.batchQueuedLoop)},t=o.b([Object(i.n)({selector:"import-batch-list",templateUrl:"../tds/web-app/app-js/modules/importBatch/components/list/import-batch-list.component.html",providers:[y.a]}),o.d("design:paramtypes",[T.b,p.a,g.a,O.b,y.a,v.a,A.c,m.a])],t)}(),w=r(913),V=r(934),k=r(915),x=r(905),j=function(){function t(t,e,r){this.importAssetsService=t,this.notifier=e,this.dataIngestionService=r,this.file=new k.a,this.actionOptions=[],this.dataScriptOptions=[],this.selectedActionOption=-1,this.selectedScriptOption=-1,this.fetchInProcess=!1,this.fetchInputUsed="action",this.transformInProcess=!1,this.importInProcess=!1,this.uiConfig={labelColSize:3,inputColSize:3,buttonColSize:1,urlColSize:2},this.transformProgress={progressKey:null,currentProgress:0},this.IMPORT_BATCH_STATES=Q,this.file.fileUID=null}return t.prototype.ngOnInit=function(){var t=this;this.importAssetsService.getManualOptions().subscribe(function(e){t.actionOptions=e.actions,t.dataScriptOptions=e.dataScripts})},t.prototype.onFetch=function(){var t=this;this.fetchInProcess=!0,this.fetchResult=null,this.fetchFileContent=null,this.transformResult=null,this.transformFileContent=null,this.importResult=null,this.importAssetsService.postFetch(this.selectedActionOption).subscribe(function(e){t.fetchResult={status:e.status},t.fetchInputUsed="action","error"===e.status?t.notifier.broadcast({name:E.b.DANGER,message:e.errors[0]}):t.fetchResult.filename=e.data.filename,t.fetchInProcess=!1})},t.prototype.onActionScriptChange=function(t){var e=this.dataScriptOptions.find(function(e){return e.id===t.defaultDataScriptId});e&&(this.selectedScriptOption=e)},t.prototype.onTransform=function(){var t=this;this.transformInProcess=!0,this.transformResult=null,this.transformFileContent=null,this.importResult=null,this.importAssetsService.postTransform(this.selectedScriptOption,this.fetchResult.filename).subscribe(function(e){e.status===B.a.API_SUCCESS&&e.data.progressKey?(t.transformProgress.progressKey=e.data.progressKey,t.setTransformProgressInterval()):(t.transformResult=new B.a,t.transformResult.status=B.a.API_ERROR,t.transformResult.data={})},function(e){t.transformResult=new B.a,t.transformResult.status=B.a.API_ERROR,t.transformResult.data={},t.transformInProcess=!1})},t.prototype.clearTestScriptProgressInterval=function(){clearInterval(this.transformInterval)},t.prototype.setTransformProgressInterval=function(){var t=this;this.transformProgress.currentProgress=1,this.transformInterval=setInterval(function(){t.getTransformProgress()},P.u)},t.prototype.getTransformProgress=function(){var t=this;this.dataIngestionService.getJobProgress(this.transformProgress.progressKey).subscribe(function(e){var r=e.data.percentComp;t.transformProgress.currentProgress=r,e.data.status===x.c?(t.handleTransformResultError(e.data.detail),t.transformInProcess=!1,t.clearTestScriptProgressInterval()):100===r&&e.data.status===x.b&&(e.data.detail?setTimeout(function(){t.transformResult=new B.a,t.transformResult.status=B.a.API_SUCCESS,t.transformResult.data={filename:e.data.detail},t.transformInProcess=!1},500):(t.handleTransformResultError("The generated intermediate ETL data file could not be accessed."),t.transformInProcess=!1),t.clearTestScriptProgressInterval())})},t.prototype.handleTransformResultError=function(t){this.transformResult=new B.a,this.transformResult.status=B.a.API_ERROR,this.notifier.broadcast({name:E.b.DANGER,message:t})},t.prototype.onImport=function(){var t=this;this.importInProcess=!0,this.importResult=null,this.importAssetsService.postImport(this.transformResult.data.filename).subscribe(function(e){t.importResult=e,t.importInProcess=!1})},t.prototype.getFetchFileContentValue=function(){return this.fetchFileContent?JSON.stringify(this.fetchFileContent):""},t.prototype.getTransformFileContentValue=function(){return this.transformFileContent?JSON.stringify(this.transformFileContent):""},t.prototype.onViewData=function(t){var e=this;this.viewDataType=t,"FETCH"===this.viewDataType?(this.fetchFileContent=null,this.importAssetsService.getFileContent(this.fetchResult.filename).subscribe(function(t){e.fetchFileContent=t})):(this.transformFileContent=null,this.importAssetsService.getFileContent(this.transformResult.data.filename).subscribe(function(t){e.transformFileContent=t}))},t.prototype.onCloseFileContents=function(){this.fetchFileContent=null,this.transformFileContent=null,this.viewDataType=null},t.prototype.onClear=function(){this.removeFileByUID()},t.prototype.disableTransformButton=function(){return!this.selectedScriptOption||-1===this.selectedScriptOption||!this.fetchResult||!this.fetchResult.filename||this.fetchResult.status===B.a.API_ERROR},t.prototype.clearFilename=function(t){this.fetchResult=null,this.fetchFileContent=null},t.prototype.onSelectFile=function(t){this.file.fileUID=t.files[0].uid},t.prototype.onRemoveFile=function(t){if(this.fetchResult&&this.fetchResult.filename){var e=[this.fetchResult.filename];this.transformResult&&e.push(this.transformResult.data.filename),t.data={filename:e.join(",")},this.fetchResult=null,this.fetchFileContent=null,this.transformResult=null,this.transformFileContent=null,this.viewDataType=null,this.importResult=null}},t.prototype.onUploadFile=function(t){t.data={},t.data[P.k]=P.a,this.clearFilename()},t.prototype.removeFileByUID=function(){this.file.fileUID&&this.kendoUploadInstance.removeFilesByUid(this.file.fileUID)},t.prototype.completeEventHandler=function(t){var e=t.response.body.data;if("delete"===e.operation)this.clearFilename(),this.file.fileUID=null;else if(e.filename){var r=e.filename;this.fetchResult={status:"success",filename:r},this.fetchInputUsed="file"}else this.clearFilename(),this.fetchResult={status:"error"}},o.b([Object(i.lb)("kendoUploadInstance"),o.d("design:type",V.a)],t.prototype,"kendoUploadInstance",void 0),t=o.b([Object(i.n)({selector:"import-assets",templateUrl:"../tds/web-app/app-js/modules/importBatch/components/import-assets/import-assets.component.html"}),o.d("design:paramtypes",[w.a,v.a,x.a])],t)}(),Q=function(){function t(){}return t.IMPORT_BATCH_LIST={url:"list"},t.IMPORT_BATCH_VIEW={url:"list/:id"},t.IMPORT_ASSETS={url:"assets"},t}(),q=[{path:"",pathMatch:"full",redirectTo:Q.IMPORT_BATCH_LIST.url},{path:Q.IMPORT_BATCH_LIST.url,data:{page:{title:"IMPORT_BATCH.MANAGE_LIST",instruction:"",menu:["ASSETS.ASSETS","IMPORT_BATCH.MANAGE_LIST"],topMenu:{parent:"menu-parent-assets",child:"menu-parent-assets-manage-dep-batches"}},requiresAuth:!0,requiresPermission:C.a.DataTransferBatchView},component:G,canActivate:[b.a,S.a,I.a]},{path:Q.IMPORT_ASSETS.url,data:{page:{title:"IMPORT_ASSETS.MANUAL_IMPORT.IMPORT_ASSETS_ETL",instruction:"",menu:["ASSETS.ASSETS","IMPORT_ASSETS.MANUAL_IMPORT.IMPORT_ASSETS_ETL"],topMenu:{parent:"menu-parent-assets",child:"menu-parent-assets-import-assets-etl"}},requiresAuth:!0,hasPendingChanges:!1},component:j,canActivate:[b.a,S.a,I.a]},{path:Q.IMPORT_BATCH_VIEW.url,data:{page:{title:"IMPORT_BATCH.MANAGE_LIST",instruction:"",menu:["ASSETS.ASSETS","IMPORT_BATCH.MANAGE_LIST"],topMenu:{parent:"menu-parent-assets",child:"menu-parent-assets-manage-dep-batches"}},requiresAuth:!0,requiresPermission:C.a.DataTransferBatchView},component:G,canActivate:[b.a,S.a,I.a]}],J=function(){function t(){}return t=o.b([Object(i.J)({exports:[m.d],imports:[m.d.forChild(q)]})],t)}(),W=function(){function t(t){this.userPreferenceService=t,this.importBatchPreferences={},this.importBatchPrefEnum=A.a,this.onLoad()}return t.prototype.onLoad=function(){var t=this;this.userTimeZone=this.userPreferenceService.getUserTimeZone(),this.userPreferenceService.getSinglePreference(A.b.IMPORT_BATCH_PREFERENCES).subscribe(function(e){e?(t.importBatchPreferences=JSON.parse(e),void 0==t.importBatchPreferences[A.a.TWISTIE_COLLAPSED]&&(t.importBatchPreferences[A.a.TWISTIE_COLLAPSED]=!1)):t.importBatchPreferences[A.a.TWISTIE_COLLAPSED]=!1},function(t){console.error(t)})},t.prototype.batchHasErrors=function(){return this.batchRecord.errorCount>0||this.batchRecord.errorList&&this.batchRecord.errorList.length>0},t.prototype.toggleSummary=function(){this.importBatchPreferences[A.a.TWISTIE_COLLAPSED]=!this.importBatchPreferences[A.a.TWISTIE_COLLAPSED],this.userPreferenceService.setPreference(A.b.IMPORT_BATCH_PREFERENCES,JSON.stringify(this.importBatchPreferences)).subscribe(function(t){})},o.b([Object(i.E)("importBatch"),o.d("design:type",R.c)],t.prototype,"importBatch",void 0),o.b([Object(i.E)("batchRecord"),o.d("design:type",L.c)],t.prototype,"batchRecord",void 0),t=o.b([Object(i.n)({selector:"import-batch-record-summary",templateUrl:"../tds/web-app/app-js/modules/importBatch/components/record/import-batch-record-summary.component.html"}),o.d("design:paramtypes",[A.c])],t)}();r.d(e,"ImportBatchModule",function(){return z});var z=function(){function t(){}return t=o.b([Object(i.J)({imports:[n.b,c.a,s.c,a.c,l.a,h.a,u.a,d.a,f.a,J],declarations:[G,D,U,W,N.b,j],providers:[S.a,p.a,w.a,x.a,{provide:a.a,useClass:k.b,multi:!0}],exports:[G,j],entryComponents:[D,U]})],t)}()}}]);