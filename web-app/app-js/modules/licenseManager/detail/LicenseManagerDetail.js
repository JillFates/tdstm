/**
 * Created by Jorge Morayta on 09/26/2016.
 */

'use strict';

export default class LicenseManagerDetail {

    constructor($log, licenseManagerService, $uibModal, $uibModalInstance, params) {
        this.licenseManagerService = licenseManagerService;
        this.uibModalInstance = $uibModalInstance;
        this.uibModal =$uibModal;
        this.log = $log;
        this.licenseModel = {
            principalId: params.license.principal.id,
            email: params.license.contact_email,
            projectId: params.license.project.id,
            clientId: params.license.client.id,
            statusId: params.license.status.id,


            environment: params.license.environment,
            inception: params.license.inception,
            expiration: params.license.expiration,
            specialInstructions: params.license.specialInstructions,
            applied: params.license.applied,
            keyId: params.license.keyId,
            replaced: params.license.replaced,
            encryptedDetail: params.license.encryptedDetail
        };

        this.getPrincipalDataSource();
        this.getProjectsDataSource();
        this.getClientDataSource();
        this.getStatusDataSource();

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
            templateUrl: '../app-js/modules/licenseManager/applyLicenseKey/ApplyLicenseKey.html',
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
            templateUrl: '../app-js/modules/licenseManager/manuallyRequest/ManuallyRequest.html',
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
        this.licenseManagerService.resubmitLicenseRequest(this.licenseModel, (data) => {});
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
            this.licenseManagerService.deleteLicense(this.licenseModel, (data) => {
                this.uibModalInstance.close(data);
            });
        });
    }

    /**
     * Populate values
     */
    getPrincipalDataSource() {
        this.principalDataSource = [
            {id: 1, name: 'EMC'},
            {id: 2, name: 'IBM'}
        ];
    }

    /**
     * Populate values
     */
    getProjectsDataSource() {
        this.projectsDataSource = [
            {id: 1, name: 'n/a'},
            {id: 2, name: 'Bank East'}
        ];
    }

    /**
     * Populate values
     */
    getClientDataSource() {
        this.clientsDataSource = [
            {id: 1, name: 'n/a'},
            {id: 2, name: 'Gold Bank'}
        ];
    }

    /**
     * Populate values
     */
    getStatusDataSource() {
        this.statusDataSource = [
            {id: 1, name: 'Active'},
            {id: 2, name: 'Pending'}
        ];
    }

    /**
     * Dismiss the dialog, no action necessary
     */
    cancelCloseDialog() {
        this.uibModalInstance.dismiss('cancel');
    }

}