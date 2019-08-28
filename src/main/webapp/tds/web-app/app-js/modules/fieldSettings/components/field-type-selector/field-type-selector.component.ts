import {Component, Input, OnInit} from '@angular/core';

@Component({
	selector: 'tds-field-type-selector',
	templateUrl: 'field-type-selector.component.html'
})

export class FieldTypeSelectorComponent implements OnInit {
	@Input('fieldType') fieldType: string;
	@Input('controls') controls: any;
	@Input('disabled') disabled: boolean;
	@Input('name') name: string;
	constructor() {}

	ngOnInit(): void {
	}

	onChange(event): void {
		console.log('Changing model');
	}

}
