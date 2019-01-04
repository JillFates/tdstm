/**
 * Created by Jorge Morayta on 12/3/2016.
 */


'use strict'

export default class FormValidator {

    constructor($log, $scope, $uibModal, $uibModalInstance) {
        this.log = $log;
        this.scope = $scope;

        // JS does a argument pass by reference
        this.currentObject = null;
        // A copy without reference from the original object
        this.originalData = null;
        // A CC as JSON for comparison Purpose
        this.objectAsJSON = null;


        // Only for Modal Windows
        this.reloadRequired = false;
        this.uibModal = $uibModal;
        this.uibModalInstance = $uibModalInstance;

        if ($scope.$on) {
            $scope.$on('modal.closing', (event, reason, closed)=> {
                this.onCloseDialog(event, reason, closed)
            });
        }
    }

    /**
     * Saves the Form in 3 instances, one to keep track of the original data, other is the current object and
     * a JSON format for comparison purpose
     * @param newObjectInstance
     */
    saveForm(newObjectInstance) {
        this.currentObject = newObjectInstance;
        this.originalData = angular.copy(newObjectInstance, this.originalData);
        this.objectAsJSON = angular.toJson(newObjectInstance);
    }

    /**
     * Get the Current Object on his reference Format
     * @returns {null|*}
     */
    getForm() {
        return this.currentObject;
    }

    /**
     * Get the Object as JSON from the Original Data
     * @returns {null|string|undefined|string|*}
     */
    getFormAsJSON() {
        return this.objectAsJSON;
    }

    /**
     *
     * @param objetToReset object to reset
     * @param onResetForm callback
     * @returns {*}
     */
    resetForm(onResetForm) {
        this.currentObject = angular.copy(this.originalData, this.currentObject);
        this.safeApply();

        if(onResetForm) {
            return onResetForm();
        }
    }

    /**
     * Validates if the current object differs from where it was originally saved
     * @returns {boolean}
     */
    isDirty() {
        var newObjectInstance = angular.toJson(this.currentObject);
        return newObjectInstance !== this.getFormAsJSON();
    }

    /**
     * This function is only available when the Form is being called from a Dialog PopUp
     */
    onCloseDialog(event, reason, closed) {
        this.log.info('modal.closing: ' + (closed ? 'close' : 'dismiss') + '(' + reason + ')');
        if (this.isDirty() && reason !== 'cancel-confirmation' && typeof reason !== 'object') {
            event.preventDefault();
            this.confirmCloseForm();
        }
    }

    /**
     * A Confirmation Dialog when the information can be lost
     * @param event
     */
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

    /**
     * Util to call safe if required
     * @param fn
     */
    safeApply(fn) {
        var phase = this.scope.$root.$$phase;
        if(phase === '$apply' || phase === '$digest') {
            if(fn && (typeof(fn) === 'function')) {
                fn();
            }
        } else {
            this.scope.$apply(fn);
        }
    }

    /**
     * Util to Reset a Dropdown list on Kendo
     */

    resetDropDown(selectorInstance, selectedId, force) {
        if(selectorInstance && selectorInstance.dataItems) {
            selectorInstance.dataItems().forEach((value, index) => {
                if(selectedId === value.id || selectedId === value) {
                    selectorInstance.select(index);
                }
            });

            if(force) {
                selectorInstance.trigger('change');
                this.safeApply();
            }
        }
    }
}