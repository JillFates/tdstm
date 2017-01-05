/**
 * Created by Jorge Morayta on 10/07/2016.
 */
'use strict';

export default class NoticeList {

    constructor($log, $state, noticeManagerService, $uibModal) {
        this.log = $log;
        this.state = $state;

        this.actionType = {
            NEW: 'New',
            EDIT: 'Edit'
        };

        this.noticeGrid = {};
        this.noticeGridOptions = {};
        this.noticeManagerService = noticeManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        this.log.debug('LicenseList Instanced');
    }

    getDataSource() {
        this.noticeGridOptions = {
            toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="noticeList.onEditCreateNotice(noticeList.actionType.NEW)"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Create New Notice</button> <div ng-click="noticeList.reloadNoticeList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5
            },
            columns: [
                {field: 'id', hidden: true },
                {field: 'htmlText', hidden: true },
                {field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="noticeList.onEditCreateNotice(noticeList.actionType.EDIT, this)"><span class="glyphicon glyphicon-pencil"></span></button>' },
                {field: 'title', title: 'Title'},
                {field: 'type.id', hidden: true},
                {field: 'type.name', title: 'Type'},
                {field: 'active', title: 'Active', template: '#if(active) {# Yes #} else {# No #}#' }
            ],
            dataSource: {
                pageSize: 10,
                transport: {
                    read: (e) => {
                        this.noticeManagerService.getNoticeList((data) => {
                            e.success(data);
                        });
                    }
                },
                sort: {
                    field: 'title',
                    dir: 'asc'
                }
            },
            sortable: true
        };
    }

    /**
     * Open a dialog with the Basic Form to request a New Notice
     */
    onEditCreateNotice(action, notice) {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/noticeManager/edit/EditNotice.html',
            controller: 'EditNotice as editNotice',
            size: 'md',
            resolve: {
                params: () => {
                    var dataItem = notice && notice.dataItem;
                    return { action: action, notice: dataItem, actionType: this.actionType};
                }
            }
        });

        modalInstance.result.then((notice) => {
            this.log.info(action + ' Notice: ', notice);
            // After a new value is added, lets to refresh the Grid
            this.reloadNoticeList();
        }, () => {
            this.log.info(action + ' Request Canceled.');
        });
    }

    reloadNoticeList() {
        if(this.noticeGrid.dataSource) {
            this.noticeGrid.dataSource.read();
        }
    }

}