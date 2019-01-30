/**
 * UI AutoCenter Directive, center horizontally the attached component
 */
import {Directive, AfterViewInit, OnInit, ElementRef, OnDestroy} from '@angular/core';

declare var jQuery: any;
@Directive({ selector: '[tds-autocenter]' })
export class UIAutoCenterDirective implements AfterViewInit, OnInit, OnDestroy {
	private modalDialog: any;
	private modalContent: any;
	constructor(private el: ElementRef) {
	}

	ngOnInit() {
		this.modalDialog = jQuery(jQuery(this.el.nativeElement).closest('.modal-dialog'));
		this.modalContent = jQuery(this.el.nativeElement);

		// hide to avoid flashing
		if (this.modalDialog) {
			jQuery(this.modalDialog).css({
				'display': 'block',
				'visibility': 'hidden'
			});
		}
	}

	/**
	 * After the dialog with the auto-center is destroyed, verify the width is set back to the initial one
	 * This is being used on replace option on ui-dialog.directive.ts where a dialog holder can be the host for several containers
	 */
	ngOnDestroy() {
		if (this.modalDialog) {
			jQuery(this.modalDialog)
				.css({
					'width': ''
				});
		}
	}

	ngAfterViewInit() {
		this.center();
	}

	/**
	 * Center horizontally the host component calculating the left position using the widths of window and the host component
	 */
	private center(): void {
		if (this.modalDialog) {
			const left = (jQuery(window).width() - this.modalContent.width()) / 2;

			// handle container width whenever the content exceeds the container width
			const width = this.modalContent.width() > this.modalDialog.width() ? this.modalContent.width()  : this.modalDialog.width();
			jQuery(this.modalDialog)
				.css({
					'top': '30px',
					'left': `${left}px`,
					'width': `${width}px`
				}).css({
				'visibility': 'visible'
			});
		}
	}

}