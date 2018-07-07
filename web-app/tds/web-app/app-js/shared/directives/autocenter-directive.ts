/**
 * UI AutoCenter Directive, center horizontally the attached component
 */
import {Directive, AfterViewInit, ElementRef} from '@angular/core';

declare var jQuery: any;
@Directive({ selector: '[tds-autocenter]' })
export class UIAutoCenterDirective implements AfterViewInit {
	constructor(private el: ElementRef) {
	}

	ngAfterViewInit() {
		this.center();
	}

	/**
	 * Center horizontally the host component calculating the left position using the widths of window and the host component
	 */
	private center(): void {
		let modalDialog = jQuery(jQuery(this.el.nativeElement).closest('.modal-dialog'));
		const modalContent = jQuery(this.el.nativeElement);

		const left = (jQuery(window).width() - modalContent.width()) / 2;

		// handle container width whenever the content exceeds the container width
		const width = modalContent.width() > modalDialog.width() ? modalContent.width()  : modalDialog.width();

		jQuery(modalDialog).css({
			'display': 'block',
			'visibility': 'hidden'
		}).css({
			'top': '30px',
			'left': `${left}px`,
			'width': `${width}px`
		}).css({
			'visibility': 'visible'
		});
	}

}