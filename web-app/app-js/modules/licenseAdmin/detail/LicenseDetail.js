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
            projectName: params.license.project.name,
            clientName: params.license.client.name,
            email: params.license.email,
            serversTokens: params.license.serversTokens || params.license.maxServers,
            environmentName: params.license.environment.name,
            inception: params.license.requestDate,
            expiration: params.license.expirationDate,
            specialInstructions: params.license.requestNote,
            active: params.license.status.id === 1,
            id: params.license.id,
            replaced: params.license.replaced,
            encryptedDetail: params.license.encryptedDetail,
            applied: false
        };

        this.prepareMethodOptions();
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
                    console.log(this.licenseModel);
                    return { license: this.licenseModel };
                }
            }
        });

        modalInstance.result.then((data) => {
            this.licenseModel.applied = data.success;
            if(data.success) {
                this.licenseModel.active = data.success;
            }
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
        if(this.licenseModel.applied) {
            this.uibModalInstance.close();
        }
        this.uibModalInstance.dismiss('cancel');
    }

}