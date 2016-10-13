/**
 * Created by Jorge Morayta on 10/07/2016.
 */

'use strict';

export default class EditNotice {

    constructor($log, noticeManagerService, $uibModalInstance, params) {
        this.noticeManagerService = noticeManagerService;
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
        this.getTypeDataSource();
        this.editModel = {
            title: '',
            typeId: 0,
            active: false,
            htmlText: '',
            rawText: ''
        }

        if(params.notice) {
            this.editModel = params.notice;
        }
    }

    /**
     * Populate the Environment dropdown values
     */
    getTypeDataSource() {
        this.typeDataSource = [
            {typeId: 1, name: 'Prelogin'},
            {typeId: 2, name: 'Postlogin'}//,
            //{typeId: 3, name: 'General'}
        ];
    }

    /**
     * Execute the Service call to generate a new License request
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

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}