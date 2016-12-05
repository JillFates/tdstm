/**
 * Created by Jorge Morayta on 12/3/2016.
 */


'use strict'

export default class FormValidator {
    constructor($log, $scope, $uibModal, $uibModalInstance) {
        this.log = $log;
        this.objectInstance = null;
        this.objectAsJSON = null;


        // Only for Modal Windows
        this.uibModal = $uibModal;
        this.uibModalInstance = $uibModalInstance;

        if ($scope.$on) {
            $scope.$on('modal.closing', (event, reason, closed)=> {
                this.onCloseDialog(event, reason, closed)
            });
        }
        //-----------------------------------------------
    }

    saveForm(newObjectInstance) {
        this.objectInstance = newObjectInstance;
        this.objectAsJSON = angular.toJson(newObjectInstance);
    }

    getForm() {
        return this.objectInstance;
    }

    getFormAsJSON() {
        return this.objectAsJSON;
    }

    isDirty() {
        var newObjectInstance = angular.toJson(this.objectInstance);
        return newObjectInstance !== this.getFormAsJSON();
    }

    // This function is only available when the Form is being called from a Dialog PopUp
    onCloseDialog(event, reason, closed) {
        this.log.info('modal.closing: ' + (closed ? 'close' : 'dismiss') + '(' + reason + ')');
        if (this.isDirty() && reason !== 'cancel-confirmation' && typeof reason !== 'object') {
            event.preventDefault();
            this.confirmCloseForm();
        }
    }

    confirmCloseForm(event) {
        var modalInstance = this.uibModal.open({
            animation: true,
            templateUrl: '../app-js/modules/dialogAction/DialogAction.html',
            controller: 'DialogAction as dialogAction',
            size: 'sm',
            resolve: {
                params: () => {
                    return {
                        title: 'Confirmation Required',
                        message: 'Changes you made may not be saved. Do you want to continue?'
                    };
                }
            }
        });

        modalInstance.result.then(() => {
            this.uibModalInstance.dismiss('cancel-confirmation');
        });
    }
}

