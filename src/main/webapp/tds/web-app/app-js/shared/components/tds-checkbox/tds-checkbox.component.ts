import {Component, Input, SimpleChanges, OnChanges, Output, EventEmitter} from '@angular/core';

@Component({
	selector: 'tds-checkbox',
	template: `
        <clr-checkbox-container clrInline>
            <clr-checkbox-wrapper>
        		<input clrCheckbox
					   type="checkbox"
					   [name]="name"
					   (change)="onChange($event)"
					   [(ngModel)]="inputValue"
					   [disabled]="disabled">
        		<label [for]="name">{{title}}</label>
			</clr-checkbox-wrapper>
		</clr-checkbox-container>
	`,
	styles: [
		`
			:host {
				display: flex;
				justify-content: flex-start;
				font-weight: normal;
			}
            clr-checkbox-container {
				margin-top: unset;
			}
			input {
				margin-right: 5px;
				margin-top: 5px;
			}
		`
	]
})

export class TDSCheckboxComponent implements OnChanges {
	@Input() disabled: boolean;
	@Input() name: string;
	@Input() title: string;
	@Output() change: EventEmitter<any> = new EventEmitter<any>() ;
	@Output() changeValue: EventEmitter<any> = new EventEmitter<any>() ;
	inputValue: any;

	/**
	 * Hook when the new Value is assigned to the ComboBox
	 * @param {SimpleChanges} changes
	 */
	ngOnChanges(changes: SimpleChanges) {
		console.log('on changes');
	}

	@Input('value')
	get value() {
		return this.inputValue;
	}

	set value(val: any) {
		this.inputValue = val;
		this.changeValue.emit(this.inputValue);
	}

	onChange(event): void {
		this.change.emit(event);
	}
}
