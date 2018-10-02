/**
 * Created by Jorge Morayta on 03/21/2018.
 */

import {Component, OnInit, Input, SimpleChanges, OnChanges} from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
	selector: 'tds-url-viewer',
	templateUrl: '../tds/web-app/app-js/shared/components/url-viewer/url-viewer.component.html'
})

export class URLViewerComponent implements OnInit {
	@Input('model') model: string;
	// _blank|_self|_parent|_top|framename
	@Input('target') target: string;
	public label: string ;
	public trustedUrl: SafeUrl;

	constructor(private sanitizer: DomSanitizer) {
	}

	public ngOnInit() {
		// Open a new Page by Default
		if (!this.target || this.target === '') {
			this.target = '_blank';
		}
		this.getURLContext(this.model);
	}

	/**
	 * Get the Label of the URL if it has the structure |
	 * i.e. This URL | http://www.google.com
	 * @returns {string}
	 */
	protected getURLContext(model: string): void {
		if (model && model.indexOf('|') !== -1) {
			const [label, url] = model.split('|');

			this.label = label || url; // if there is no label fall back to url, (for label in the template is shown interpolated, we don't need sanitize)
			this.trustedUrl = this.sanitizer.bypassSecurityTrustUrl(url);
		} else {
			this.trustedUrl = this.sanitizer.bypassSecurityTrustUrl(model);
			this.label = 'Click to View';
		}
	}

	/**
	 * Hook when the new Value is assigned to the ComboBox
	 * @param {SimpleChanges} changes
	 */
	ngOnChanges(changes: SimpleChanges) {
		// To avoid doing extra Rest Call, the initial set in the Combo will be the current value.
		if (changes['model'] && changes['model'].currentValue) {
			this.getURLContext(changes['model'].currentValue);
		}
	}
}