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
        }
        this.noticeGridOptions = {};
        this.noticeManagerService = noticeManagerService;
        this.uibModal = $uibModal;

        this.getDataSource();
        this.log.debug('LicenseList Instanced');
    }

    getDataSource() {
        this.noticeGridOptions = {
            toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" ng-click="noticeList.onEditCreateNotice(noticeList.actionType.NEW)"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Create New Notice</button> <div onclick="loadGridBundleList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5
            },
            columns: [
                {field: 'noticeId', hidden: true },
                {field: 'htmlText', hidden: true },
                {field: 'action', title: 'Action', width: 80, template: '<button class="btn btn-default" ng-click="noticeList.onEditCreateNotice(noticeList.actionType.EDIT, this)"><span class="glyphicon glyphicon-edit"></span></button>' },
                {field: 'title', title: 'Title'},
                {field: 'type', title: 'Project'},
                {field: 'active', title: 'Active'},
                {field: 'project', title: 'project'},
                {field: 'type', title: 'Type'},
                {field: 'acknowledge', title: 'Acknowledge'}
            ],
            dataSource: {
                pageSize: 10,
                transport: {
                    read: (e) => {
                        this.noticeManagerService.getNoticeList((data) => {
                            e.success(data);
                        });
                    }
                }
            }
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

        modalInstance.result.then((license) => {
            this.log.info(action + ' Notice: ', license);
        }, () => {
            this.log.info(action + ' Request Canceled.');
        });
    }

}