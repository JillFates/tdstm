/**
 * Created by Jorge Morayta on 12/20/2015.
 */
'use strict';

export default class TaskManagerController {

    constructor($log, taskManagerService, $uibModal) {
        this.log = $log;
        this.uibModal = $uibModal;
        this.module = 'TaskManager';
        this.taskManagerService = taskManagerService;
        this.taskGridOptions = {};
        this.eventDataSource = [];

        // Init Class
        this.getEventDataSource();
        this.getDataSource();
        this.log.debug('TaskManager Controller Instanced');
        this.initForm();

    }

    openModalDemo() {

        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: 'app-js/modules/taskManager/edit/TaskManagerEdit.html',
            controller: 'TaskManagerEdit',
            size: 'lg',
            resolve: {
                items: function () {
                    return ['1','a2','gg'];
                }
            }
        });

        modalInstance.result.then((selectedItem) => {
            this.debug(selectedItem);
        }, () => {
            this.log.info('Modal dismissed at: ' + new Date());
        });
    }

    getDataSource() {
        this.taskGridOptions = {
            groupable: true,
            sortable: true,
            pageable: {
                refresh: true,
                pageSizes: true,
                buttonCount: 5
            },
            columns: [{field: 'action', title: 'Action'},
                {field: 'task', title: 'Task'},
                {field: 'description', title: 'Description'},
                {field: 'assetName', title: 'Asset Name'},
                {field: 'assetType', title: 'Asset Type'},
                {field: 'updated', title: 'Updated'},
                {field: 'due', title: 'Due'},
                {field: 'status', title: 'Status'},
                {field: 'assignedTo', title: 'Assigned To'},
                {field: 'team', title: 'Team'},
                {field: 'category', title: 'Category'},
                {field: 'suc', title: 'Suc.'},
                {field: 'score', title: 'Score'}],
            dataSource: {
                pageSize: 10,
                transport: {
                    read: (e) => {
                        /*this.taskManagerService.testService((data) => {
                            e.success(data);
                        });*/
                    }
                }
            }
        };
    }

    getEventDataSource() {
        this.eventDataSource = [
            {eventId: 1, eventName: 'All'},
            {eventId: 2, eventName: 'Buildout'},
            {eventId: 3, eventName: 'DR-EP'},
            {eventId: 4, eventName: 'M1-Physical'}
        ];
    }

    onErrorHappens() {
        this.taskManagerService.failCall(function () {

        });
    }

    initForm() {
        this.userFields = [
            {
                key: 'email',
                type: 'input',
                templateOptions: {
                    type: 'email',
                    label: 'Email address',
                    placeholder: 'Enter email'
                }
            },
            {
                key: 'password',
                type: 'input',
                templateOptions: {
                    type: 'password',
                    label: 'Password',
                    placeholder: 'Password'
                }
            },
            {
                key: 'file',
                type: 'file',
                templateOptions: {
                    label: 'File input',
                    description: 'Example block-level help text here',
                    url: 'https://example.com/upload'
                }
            },
            {
                key: 'checked',
                type: 'checkbox',
                templateOptions: {
                    label: 'Check me out'
                }
            }
        ];
    }
}