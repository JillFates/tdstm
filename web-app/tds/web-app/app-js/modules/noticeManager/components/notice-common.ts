// Angular
import { DomSanitizer} from '@angular/platform-browser';

export abstract class NoticeCommonComponent {
	constructor(
		protected sanitizer: DomSanitizer) {
	}

	abstract onCancel(): void;

	protected sanitizeHTML(html: string) {
		return this.sanitizer.bypassSecurityTrustHtml(html);
	}

	abstract  onAccept(): void;

}
