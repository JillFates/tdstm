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
						method: {
								name: params.license.method.name,
								max: params.license.method.max
						},
						projectName: params.license.project.name,
						clientName: params.license.client.name,
						email: params.license.email,
						toEmail: params.license.toEmail,
						environment: params.license.environment,
						inception: params.license.activationDate,
						expiration: params.license.expirationDate,
						requestNote: params.license.requestNote,
						active: params.license.status === 'ACTIVE',
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
								name: 'MAX_SERVERS',
								text: 'Servers'
						},
						{
								name: 'TOKEN',
								text: 'Tokens'
						},
						{
								name: 'CUSTOM',
								text: 'Custom'
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

				modalInstance.result.then((data) => {
						this.licenseModel.applied = data.success;
						if(data.success) {
								this.licenseModel.active = data.success;
								this.uibModalInstance.close({ id: this.licenseModel.id, updated: true});
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
										return { title: 'Confirmation Required', message: 'You are about to delete the selected license. Are you sure? Click Confirm to delete otherwise press Cancel.'};
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