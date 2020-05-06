// Angular
import { DomSanitizer} from '@angular/platform-browser';

import {
	Dialog
} from 'tds-component-library';

declare var jQuery: any;

export abstract class NoticeCommonComponent extends Dialog {
	constructor(
		protected sanitizer: DomSanitizer) {
		super();
		jQuery('.main-header').css('pointer-events', 'all');
	}

	/**
	 * needs to be defined by the instance
	*/
	abstract onCancel(): void;

	/**
	 * Sanitize the html value to be displayed
	*/
	protected sanitizeHTML(html: string) {
		return this.sanitizer.bypassSecurityTrustHtml(html);
	}

	/**
	 * needs to be defined by the instance
	*/
	abstract  onAccept(): void;

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.onCancel();
	}

}
