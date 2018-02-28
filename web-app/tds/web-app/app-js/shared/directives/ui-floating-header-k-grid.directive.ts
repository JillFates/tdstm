/**
 * UI Directive that take on Grid and make its header floating
 * By default no action is required, just put the directive at the top of the Grid
 * if the Grid position changes or one is created on fly, a notifier event can be used to locate the grid header.
 */

import {Component, OnInit} from '@angular/core';
import {NotifierService} from '../services/notifier.service';

declare var jQuery: any;

@Component({
	selector: 'tds-ui-floating-header-k-grid',
	template: ''
})
export class UIFloatingHeaderKGridDirective implements OnInit {

	createHeaderGridWatcher: any;

	constructor(private notifierService: NotifierService) {
		this.registerListeners();
	}

	/**
	 * Detects all grid instantiated on the page
	 */
	ngOnInit() {
		setTimeout(() => this.getAllGridHeaders(), 1000);

	}

	/**
	 * Detects all grid instantiated on the page
	 */
	private registerListeners(): void {
		this.createHeaderGridWatcher = this.notifierService.on('grid.header.position.change', event => {
			this.getAllGridHeaders();
		});
	};

	/**
	 * Get all Grid Headers from the page.
	 */
	private getAllGridHeaders() {
		let headers = jQuery('.k-grid-header').length;
		if (headers >= 1) {
			jQuery('.k-grid-header').each((index, element) => {
				this.onScrollChange(element);
			});
		}
	}

	/**
	 * Receive the component and add the logic to make a floating header.
	 * @param {string} element
	 */
	private onScrollChange(element: string) {
		let offSet = jQuery(element).offset();
		jQuery(window).bind('scroll', function () {
			if (this.pageYOffset >= offSet.top) {
				jQuery('.k-grid-header').addClass('k-grid-dynamic-header');
			} else {
				jQuery('.k-grid-header').removeClass('k-grid-dynamic-header');
			}
		});
	}

	/**
	 * Clear resources on destroy
	 */
	ngOnDestroy(): void {
		this.createHeaderGridWatcher();
	}
}