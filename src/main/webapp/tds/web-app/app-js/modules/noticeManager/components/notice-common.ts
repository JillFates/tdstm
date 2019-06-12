// Angular
import { DomSanitizer} from '@angular/platform-browser';

declare var jQuery: any;

export abstract class NoticeCommonComponent {
	constructor(
		protected sanitizer: DomSanitizer) {
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

}
