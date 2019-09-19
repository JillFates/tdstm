import {Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges} from '@angular/core';
import {FieldConverterService} from '../../service/field-converter.service';
import {CUSTOM_FIELD_CONTROL_TYPE} from '../../model/field-settings.model';

@Component({
	selector: 'tds-field-type-selector',
	templateUrl: 'field-type-selector.component.html'
})

export class FieldTypeSelectorComponent implements OnInit , OnChanges  {
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

	/**
	 * Grab the reference to the initial field type provided
	 */
	ngOnInit(): void {
		this.value = this.fieldType;
	}

	/**
	 *  On changing control event report this action to host component
	 *  in order it will be avaible to decide whether move forward with the change
	 *  o reset the action
	 * @param {string }newType New type of control to be assigned
	 */
	onChange(newType: string): void {
		const conversion = this.fieldConverterService.findConversion(this.fieldType, newType);
		this.fieldTypeChange.emit({
			conversion: conversion,
			reset: () => { this.reset(conversion.from); },
			save: () => { this.onSave(conversion); },
		});
	}

	/**
	 * Reset type of control change
	 * @param {string} previous  Previous selected value
	 */
	reset(previous: string): void {
		this.value = previous;
	}

	/**
	 * Set the current value and report the change to host component
	 * @param conversion
	 */
	onSave(conversion: any): void {
		this.value = conversion.to;
		this.save.emit(conversion);
	}

	/**
	 * Detect any change on the control type
	 * @param changes
	 */
	ngOnChanges(changes: SimpleChanges): void {
		if (changes && changes.fieldType && changes.fieldType.currentValue) {
			this.value = changes.fieldType.currentValue;
		}
	}

}
