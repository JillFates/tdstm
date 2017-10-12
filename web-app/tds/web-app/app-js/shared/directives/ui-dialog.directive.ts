/**
 * UI Dialog Directive works as a placeholder for any dialog being initiaded
 */

import {
	Component, ComponentRef, ElementRef,
	Input, ViewChild, ViewContainerRef,
	OnDestroy, AfterViewInit
} from '@angular/core';

import { NotifierService } from '../services/notifier.service';
import { UIActiveDialogService } from '../services/ui-dialog.service';
import { ComponentCreatorService } from '../services/component-creator.service';
declare var jQuery: any;

@Component({
	selector: 'tds-ui-dialog',
	template: `
    <div class="modal fade" id="tdsUiDialog" data-backdrop="static" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-{{size}}" role="document" #modalDialog>
            <div class="modal-content">
                <div #view></div>
            </div>
        </div>
    </div>`
})
export class UIDialogDirective implements OnDestroy, AfterViewInit {
	@Input('name') name: string;
	@ViewChild('view', { read: ViewContainerRef }) view: ViewContainerRef;
	@ViewChild('modalDialog') el: ElementRef;
	size = 'md';
	tdsUiDialog: any;

	cmpRef: ComponentRef<{}>; // Instace of the component

	resolve: any;
	reject: any;

	openNotifier: any;
	closeNotifier: any;
	dismissNotifier: any;

	constructor(private notifierService: NotifierService, private activeDialog: UIActiveDialogService, private compCreator: ComponentCreatorService) {
		this.registerListeners();
	}

	ngAfterViewInit(): void {
		this.tdsUiDialog = jQuery('#tdsUiDialog');
		jQuery(this.el.nativeElement).draggable({
			handle: '.modal-header'
		});
	}

	/**
	 * Clear resources on destroy
	 */
	ngOnDestroy(): void {
		if (this.cmpRef) {
			this.tdsUiDialog.modal('hide');
			this.cmpRef.destroy();
		}
		this.openNotifier();
		this.closeNotifier();
		this.dismissNotifier();
	}

	/**
	 * Register the listener to handle dialog events
	 */
	private registerListeners(): void {
		this.openNotifier = this.notifierService.on('dialog.open', event => {
			// make sure UI has no other open dialog
			if (this.cmpRef) {
				this.cmpRef.destroy();
				this.tdsUiDialog.modal('hide');
				this.reject('OTHER_DIALOG_OPENED');
			}
			this.size = event.size;
			this.reject = event.reject;
			this.resolve = event.resolve;
			this.cmpRef = this.compCreator.insert(event.component, event.params, this.view);

			this.activeDialog.componentInstance = this.cmpRef;

			this.tdsUiDialog.modal('show');
		});

		this.closeNotifier = this.notifierService.on('dialog.close', event => {
			if (this.cmpRef) {
				this.el.nativeElement.style.top = 'initial';
				this.el.nativeElement.style.left = 'initial';
				jQuery('#tdsUiDialog').modal('hide');
				this.resolve(event.result);
				this.cmpRef.destroy();
			}
		});

		this.dismissNotifier = this.notifierService.on('dialog.dismiss', event => {
			if (this.cmpRef) {
				this.el.nativeElement.style.top = 'initial';
				this.el.nativeElement.style.left = 'initial';
				this.tdsUiDialog.modal('hide');
				this.reject(event.result);
				this.cmpRef.destroy();
			}
		});

	};
}