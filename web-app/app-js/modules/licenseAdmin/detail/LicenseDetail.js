/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

export default class LicenseDetail {

    constructor($log, licenseAdminService, $uibModal, $uibModalInstance, params) {
        this.licenseAdminService = licenseAdminService;
        this.uibModalInstance = $uibModalInstance;
        this.uibModal =$uibModal;
        this.log = $log;
        this.licenseModel = {
            methodId: params.license.method.id,
            environment: params.license.environment,
            inception: params.license.inception,
            expiration: params.license.expiration,
            specialInstructions: params.license.specialInstructions,
            applied: params.license.applied,
            keyId: params.license.keyId,
            replaced: params.license.replaced,
            encryptedDetail: params.license.encryptedDetail
        };

        this. prepareMethodOptions();
    }

    prepareMethodOptions() {
        this.methodOptions = [
            {
                id: 1,
                name: 'Servers'
            },
            {
                id: 2,
                name: 'Tokens'
            },
            {
                id: 3,
                name: 'Custom'
            }
        ]
    }

    /**
     * The user apply and server should validate the key is correct
     */
    applyLicenseKey() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseAdmin/applyLicenseKey/ApplyLicenseKey.html',
            controller: 'ApplyLicenseKey as applyLicenseKey',
            size: 'md',
            resolve: {
                params: () => {
                    return { license: this.licenseModel };
                }
            }
        });

        modalInstance.result.then(() => {
            this.licenseModel.applied = true;
        });
    }

    /**
     * Opens a dialog and allow the user to manually send the request or copy the encripted code
     */
    manuallyRequest() {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/licenseAdmin/manuallyRequest/ManuallyRequest.html',
            controller: 'ManuallyRequest as manuallyRequest',
            size: 'md',
            resolve: {
                params: () => {
                    return { license: this.licenseModel };
                }
            }
        });

        modalInstance.result.then(() => {});
    }

    /**
     * If by some reason the License was not applied at first time, this will do a request for it
     */
    resubmitLicenseRequest() {
        this.licenseAdminService.resubmitLicenseRequest(this.licenseModel, (data) => {});
    }

    deleteLicense() {
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
            this.licenseAdminService.deleteLicense(this.licenseModel, (data) => {
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