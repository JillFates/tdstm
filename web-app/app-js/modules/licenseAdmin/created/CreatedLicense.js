/**
 * Created by Jorge Morayta on 09/28/2016.
 */

'use strict';

export default class CreatedRequestLicense {

    constructor($log, $uibModalInstance, params) {
        this.uibModalInstance = $uibModalInstance;
        this.client = params;
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}