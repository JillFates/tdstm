/**
 * Created by Jorge Morayta on 03/21/2018.
 */

import {Component, OnInit, Input} from '@angular/core';

@Component({
	selector: 'tds-url-viewer',
	templateUrl: '../tds/web-app/app-js/shared/components/url-viewer/url-viewer.component.html'
})

export class URLViewerComponent implements OnInit {
	@Input('model') model: string;
	// _blank|_self|_parent|_top|framename
	@Input('target') target: string;

	public urlLabel = '';
	public urlReference = '';

	public ngOnInit() {
		// Open a new Page by Default
		if (!this.target || this.target === '') {
			this.target = '_blank';
		}
		this.getURLContext();
	}

	/**
	 * Get the Label of the URL if it has the structure |
	 * i.e. This URL | http://www.google.com
	 * @returns {string}
	 */
	protected getURLContext(): void {
		if (this.model && this.model.indexOf('|') !== -1) {
			let modelContent = this.model.split('|');
			// if there is no label fall back to url
			this.urlLabel = modelContent[0] || modelContent[1];
			this.urlReference = modelContent[1];
		} else {
			this.urlReference = this.model;
			this.urlLabel = 'Click to View';
		}
	}
}