/**
 * Created by Jorge Morayta on 10/07/2016.
 */

'use strict';

export default class EditNotice {

    constructor($log, noticeManagerService, $uibModal, $uibModalInstance, params) {
        this.noticeManagerService = noticeManagerService;
        this.uibModal = $uibModal;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;

        this.action = params.action;
        this.actionType = params.actionType;

        this.kendoEditorTools = [
            'formatting', 'cleanFormatting',
            'fontName', 'fontSize',
            'justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull',
            'bold',
            'italic',
            'viewHtml'
        ];

        // CSS has not canceling attributes, so instead of removing every possible HTML, we make editor has same css
        this.kendoStylesheets = [
            '../static/dist/js/vendors/bootstrap/dist/css/bootstrap.min.css', // Ourt current Bootstrap css
            '../static/dist/css/TDSTMLayout.min.css' // Original Template CSS

        ];

        this.getTypeDataSource();
        this.editModel = {
            title: '',
            typeId: 0,
            active: false,
            htmlText: '',
            rawText: ''
        }

        // On Edition Mode we cc the model and only the params we need
        if(params.notice) {
            this.editModel.id = params.notice.id;
            this.editModel.title = params.notice.title;
            this.editModel.typeId = params.notice.type.id;
            this.editModel.active = params.notice.active;
            this.editModel.htmlText = params.notice.htmlText;
        }
    }

    /**
     * Populate the Environment dropdown values
     */
    getTypeDataSource() {
        this.typeDataSource = [
            {typeId: 1, name: 'Prelogin'},
            {typeId: 2, name: 'Postlogin'}
            //{typeId: 3, name: 'General'} Disabled until Phase II
        ];
    }

    /**
     * Execute the Service call to Create/Edit a notice
     */
    saveNotice() {
        this.log.info(this.action + ' Notice Requested: ', this.editModel);
        this.editModel.rawText = $('#kendo-editor-create-edit').text();
        this.editModel.typeId = parseInt(this.editModel.typeId);
        if(this.action === this.actionType.NEW) {
            this.noticeManagerService.createNotice(this.editModel, (data) => {
                this.uibModalInstance.close(data);
            });
        } else if(this.action === this.actionType.EDIT) {
            this.noticeManagerService.editNotice(this.editModel, (data) => {
                this.uibModalInstance.close(data);
            });
        }
    }

    deleteNotice() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
            controller: 'DialogAction as dialogAction',
            size: 'sm',
            resolve: {
                params: () => {
                    return { title: 'Confirmation Required', message: 'Are you sure you want to delete it? This action cannot be undone.'};
                }
            }
        });

        modalInstance.result.then(() => {
            this.noticeManagerService.deleteNotice(this.editModel, (data) => {
                this.uibModalInstance.close(data);
            });
        });
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}