/**
 * Created by Jorge Morayta on 10/07/2016.
 */

'use strict';

export default class DialogAction {

    constructor($log, $uibModal, $uibModalInstance, params) {
        this.uibModal = $uibModal;
        this.uibModalInstance = $uibModalInstance;
        this.log = $log;

        this.title = params.title;
        this.message = params.message;

    }
    /**
     * Acccept and Confirm
     */
    confirmAction() {
        this.uibModalInstance.close();
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}