import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
	selector: `
		tds-progress-bar,
	`,
	template: `
		<kendo-dialog *ngIf="opened" class="bar" [width]="450">
			<kendo-dialog-titlebar
							[ngClass]="'title-bar'"
							[style.background-color]="titleBarBackground || ''"
							[style.color]="titleBarColor || ''">
					<div>Downloading file</div>
			</kendo-dialog-titlebar>
			<div>
				<h4>{{title}}</h4>
				<progress-bar [progress]="value.toString()" [color]="'#488aff'"></progress-bar>
				<div class="text-center">{{message}}</div>
				<div class="pull-right"><tds-button-close *ngIf="enableClose" (click)="onClose()"></tds-button-close></div>
			</div>
		</kendo-dialog>
	`,
	styles: [`.bar { background-color: rgba(0,0,0,0.3); height: 100%;}
    >>> .k-window-actions.k-dialog-actions {display: none;}
		.title-bar {
				background-color: #3c8dbc;
		}
	`]
})
export class TDSProgressBar {
	@Input() title = '';
	@Input() value = 0;
	@Input() opened = false;
	@Input() message;
	@Input() enableClose: boolean;
	@Input() titleBarBackground = '';
	@Input() titleBarColor = '';
	@Output() close: EventEmitter<void> = new EventEmitter<void>();

	constructor() {
		// Silence is golden
	}

	/**
	 * On button close click emit the close event to parent.
	 */
	onClose(): void {
		this.close.emit();
	}
}
