/**
 * UI Dialog Directive works as a placeholder for any dialog being initiaded
 * 
 */

import {
    Component, ComponentRef,
    Input, ViewChild, ViewContainerRef,
    OnDestroy
} from '@angular/core';

import { NotifierService } from "../services/notifier.service";
import { UIActiveDialogService } from "../services/ui-dialog.service";
import { ComponentCreatorService } from "../services/component-creator.service";
declare var jQuery: any;

@Component({
    selector: 'tds-ui-dialog',
    template: `
    <div class="modal fade" id="tdsUiDialog" data-backdrop="static" tabindex="-1" role="dialog" >
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div #view></div>
            </div>
        </div>
    </div>`
})
export class UIDialogDirective implements OnDestroy {
    @Input('name') name: string;
    @ViewChild('view', { read: ViewContainerRef }) view: ViewContainerRef;
    cmpRef: ComponentRef<{}>;

    resolve: any;
    reject: any;

    openNotifier: any;
    closeNotifier: any;
    dismissNotifier: any;

    constructor(private notifierService: NotifierService,
        private activeDialog: UIActiveDialogService,
        private compCreator: ComponentCreatorService) {
        this.registerListeners();
    }

    private registerListeners(): void {
        this.openNotifier = this.notifierService.on('dialog.open', event => {
            if (this.cmpRef) {
                this.cmpRef.destroy();
                jQuery("#tdsUiDialog").modal('hide');
                this.reject("OTHER_DIALOG_OPENED");
            }

            this.reject = event.reject;
            this.resolve = event.resolve;
            this.cmpRef = this.compCreator.insert(event.component, event.params, this.view);

            this.activeDialog.componentInstance = this.cmpRef;
            
            jQuery("#tdsUiDialog").modal('show');
        });

        this.closeNotifier = this.notifierService.on('dialog.close', event => {
            if (this.cmpRef) {
                jQuery("#tdsUiDialog").modal('hide');
                this.resolve(event.result);
                this.cmpRef.destroy();
            }
        });

        this.dismissNotifier = this.notifierService.on('dialog.dismiss', event => {
            if (this.cmpRef) {
                jQuery("#tdsUiDialog").modal('hide');
                this.reject(event.result);
                this.cmpRef.destroy();
            }
        });

    };

    ngOnDestroy(): void {
        if (this.cmpRef)
            this.cmpRef.destroy();
        this.openNotifier();
        this.closeNotifier();
        this.dismissNotifier();
    }
}