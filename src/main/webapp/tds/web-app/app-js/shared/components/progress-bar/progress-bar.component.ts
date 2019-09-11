import {Component, Input} from '@angular/core';

@Component({
	selector: `
		tds-progress-bar,
	`,
	template: `
		<kendo-dialog title="Downloading file" *ngIf="opened" class="bar" [width]="450">
			<div>
				<h4>{{title}}</h4>
				<progress-bar [progress]="value.toString()" [color]="'#488aff'"></progress-bar>
			</div>
		</kendo-dialog>
	`,
	styles: [`.bar { background-color: rgba(0,0,0,0.3); height: 100%;}
    >>> .k-window-actions.k-dialog-actions {display: none;}`]
})
export class TDSProgressBar {
	@Input() title = '';
	@Input() value = 0;
	@Input() opened = false;

	constructor() {
		// comment
	}
}
