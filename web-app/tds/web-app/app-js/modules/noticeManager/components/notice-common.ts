// Angular
import { DomSanitizer} from '@angular/platform-browser';

export abstract class NoticeCommonComponent {
	constructor(
		protected sanitizer: DomSanitizer) {
	}

	// TODO move to base
	abstract onCancel(): void;

	// TODO move to base
	protected sanitizeHTML(html: string) {
		return this.sanitizer.bypassSecurityTrustHtml(html);
	}

	abstract  onAccept(): void;

}
