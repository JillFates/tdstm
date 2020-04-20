import {Component, Input, SimpleChanges, OnChanges, Output, EventEmitter} from '@angular/core';

@Component({
	selector: 'tds-checkbox',
	template: `
<!--        (change)="onChange($event)"-->
			<clr-checkbox-wrapper class="inline">
                    <input
                        clrCheckbox
                        [id]="name"
                        type="checkbox"
                        [name]="name"
						[(ngModel)]="inputModel"
				       (ngModelChange)="inputModelChange.emit(inputModel)"
						[disabled]="disabled"
                    />
                    <label
                        class="clr-control-label clr-control-label-sm inline"
                        [for]="name"
                    >{{title}}
                    </label>
            </clr-checkbox-wrapper>

	`,
	styles: [
		`
			:host {
				display: flex;
				justify-content: flex-start;
				font-weight: normal;
			}
			label {
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
	@Input() disabled: boolean;
	@Input() name: string;
	@Input() title = '';
	@Input() inputModel: boolean;
	@Output() inputModelChange: EventEmitter<boolean> = new EventEmitter<boolean>();

	@Output() change: EventEmitter<any> = new EventEmitter<any>() ;
	inputValue: any;

	/**
	 * Hook when the new Value is assigned to the ComboBox
	 * @param {SimpleChanges} changes
	 */
	ngOnChanges(changes: SimpleChanges) {
		// console.log('on changes');
	}

	onChange(event): void {
		this.change.emit(event);
	}
}
