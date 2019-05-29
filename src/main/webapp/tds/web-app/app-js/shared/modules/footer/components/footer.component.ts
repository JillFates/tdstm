import {Component} from '@angular/core';

@Component({
	selector: 'tds-footer',
	templateUrl: 'footer.component.html',
})
export class FooterComponent {
	public today = new Date();
	public buildInfo = '';
}