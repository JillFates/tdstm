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
    <div class="modal fade" id="tdsUiDialog" data-backdrop="static" data-keyboard="false" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-{{size}}" role="document" #modalDialog>
            <div class="modal-content">
                <div #view></div>
            </div>
        </div>
	</div>
    <div #extraDialog></div>`,
	styles: [`
		.modal { background:none;}
	`]
})
export class UIDialogDirective implements OnDestroy, AfterViewInit {
	@Input('name') name: string;
	@ViewChild('view', { read: ViewContainerRef }) view: ViewContainerRef;
	@ViewChild('extraDialog', { read: ViewContainerRef }) extraDialog: ViewContainerRef;
	@ViewChild('modalDialog') el: ElementRef;
	size = 'md';
	tdsUiDialog: any;

	cmpRef: ComponentRef<{}>; // Instace of the component

	resolve: any;
	reject: any;

	openNotifier: any;
	closeNotifier: any;
	dismissNotifier: any;
	replaceNotifier: any;
	extraNotifier: any;

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
		this.replaceNotifier();
		this.extraNotifier();
	}

	/**
	 * Register the listener to handle dialog events
	 */
	private registerListeners(): void {
		this.openNotifier = this.notifierService.on('dialog.open', event => {
			// make sure UI has no other open dialog
			this.tdsUiDialog.modal('hide');
			if (this.cmpRef) {
				this.cmpRef.destroy();
				this.reject('OTHER_DIALOG_OPENED');
			}
			this.size = event.size;
			this.reject = event.reject;
			this.resolve = event.resolve;
			this.cmpRef = this.compCreator.insert(event.component, event.params, this.view);

			this.activeDialog.componentInstance = this.cmpRef;
			this.tdsUiDialog.modal('show');
		});

		this.extraNotifier = this.notifierService.on('dialog.extra', event => {
			const cmpRef = this.compCreator.insert(event.component, event.params, this.extraDialog);
			const instance = cmpRef.instance as any;
			instance.open(event.resolve, event.reject, cmpRef);
		});


		this.replaceNotifier = this.notifierService.on('dialog.replace', event => {
			if (this.cmpRef) {
				this.cmpRef.destroy();
				this.cmpRef = this.compCreator.insert(event.component, event.params, this.view);
				this.activeDialog.componentInstance = this.cmpRef;
			}
		});



		this.closeNotifier = this.notifierService.on('dialog.close', event => {
			if (this.cmpRef) {
				this.el.nativeElement.style.top = 'initial';
				this.el.nativeElement.style.left = 'initial';
				this.tdsUiDialog.modal('hide');
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
