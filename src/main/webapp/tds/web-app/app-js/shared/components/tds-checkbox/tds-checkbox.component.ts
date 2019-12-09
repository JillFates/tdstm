import {Component, Input, SimpleChanges, OnChanges, Output, EventEmitter} from '@angular/core';

@Component({
	selector: 'tds-checkbox',
	template: `
        <input type="checkbox" [name]="name" (change)="onChange($event)" [(ngModel)]="value" [disabled]="disabled">{{title}}
        <label [for]="name"></label>
	`,
	styles: [
		`
			:host {
				display: flex;
				justify-content: flex-start;
				font-weight: normal;
			}
			input {
				margin-right: 5px;
				margin-top: 5px;
			}
		`
	]
})

export class TDSCheckboxComponent implements OnChanges {
	@Input() value: boolean;
	@Input() disabled: boolean;
	@Input() name: string;
	@Input() title: string;
	@Output() change: EventEmitter<any> = new EventEmitter<any>() ;

	/**
	 * Hook when the new Value is assigned to the ComboBox
	 * @param {SimpleChanges} changes
	 */
	ngOnChanges(changes: SimpleChanges) {
		console.log('on changes');
	}

	onChange(event): void {
		this.change.emit(event);
	}
}
