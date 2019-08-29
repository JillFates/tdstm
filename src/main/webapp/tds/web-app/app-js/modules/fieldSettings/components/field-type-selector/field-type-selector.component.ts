import {Component, Input, Output, EventEmitter, OnInit} from '@angular/core';
import {FieldConverterService} from '../../service/field-converter.service';
import {CUSTOM_FIELD_CONTROL_TYPE} from '../../model/field-settings.model';

@Component({
	selector: 'tds-field-type-selector',
	templateUrl: 'field-type-selector.component.html'
})

export class FieldTypeSelectorComponent implements OnInit /*, OnChanges */ {
	private value: string;
	@Input() fieldType: string;
	@Input() disabled: boolean;
	@Input() name: string;
	@Output() save: EventEmitter<any> = new EventEmitter<any>();
	@Output()  fieldTypeChange: EventEmitter<any> = new EventEmitter<any>();

	constructor(private fieldConverterService: FieldConverterService) {}

	readonly availableControls = [
		{ text: CUSTOM_FIELD_CONTROL_TYPE.List, value: CUSTOM_FIELD_CONTROL_TYPE.List},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.String, value: CUSTOM_FIELD_CONTROL_TYPE.String},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.YesNo, value: CUSTOM_FIELD_CONTROL_TYPE.YesNo},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.Date, value: CUSTOM_FIELD_CONTROL_TYPE.Date},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.DateTime, value: CUSTOM_FIELD_CONTROL_TYPE.DateTime},
		{ text: CUSTOM_FIELD_CONTROL_TYPE.Number, value: CUSTOM_FIELD_CONTROL_TYPE.Number}
	];

	ngOnInit(): void {
		this.value = this.fieldType;
	}

	onChange(newType): void {
		const conversion = this.fieldConverterService.findConversion(this.fieldType, newType);
		this.fieldTypeChange.emit({
			conversion: conversion,
			reset: () => { this.reset(conversion.from); },
			save: () => { this.onSave(conversion); },
		});
	}

	reset(previous: string) {
		this.value = previous;
	}

	onSave(conversion: any) {
		this.value = conversion.to;
		this.save.emit(conversion);
	}

}
