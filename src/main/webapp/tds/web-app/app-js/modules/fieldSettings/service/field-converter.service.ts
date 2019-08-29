import { Injectable } from '@angular/core';
import {TranslatePipe} from '../../../shared/pipes/translate.pipe';
import { CUSTOM_FIELD_TYPES } from '../../../shared/model/constants';

@Injectable()
export class FieldConverterService {
	private conversions: any[] ;

	constructor(private translate: TranslatePipe) {
		/* constructor */
	}

	getConversions(): any[] {
		if (this.conversions) {
			return this.conversions;
		}

		this.conversions = [
			{
				from: CUSTOM_FIELD_TYPES.List,
				to: CUSTOM_FIELD_TYPES.String,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.LIST_TO_STRING')
			},
			{
				from: CUSTOM_FIELD_TYPES.List,
				to: CUSTOM_FIELD_TYPES.YesNo,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.LIST_TO_YES_NO')
			},
			{
				from: CUSTOM_FIELD_TYPES.List,
				to: CUSTOM_FIELD_TYPES.Date,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.LIST_TO_DATE')
			},
			{
				from: CUSTOM_FIELD_TYPES.List,
				to: CUSTOM_FIELD_TYPES.DateTime,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.LIST_TO_DATETIME')
			},
			{
				from: CUSTOM_FIELD_TYPES.List,
				to: CUSTOM_FIELD_TYPES.Number,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.LIST_TO_NUMBER')
			},
			{
				from: CUSTOM_FIELD_TYPES.String,
				to: CUSTOM_FIELD_TYPES.List,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.STRING_TO_LIST')
			},
			{
				from: CUSTOM_FIELD_TYPES.String,
				to: CUSTOM_FIELD_TYPES.YesNo,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.STRING_TO_YES_NO')
			},
			{
				from: CUSTOM_FIELD_TYPES.String,
				to: CUSTOM_FIELD_TYPES.Date,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.STRING_TO_DATE')
			},
			{
				from: CUSTOM_FIELD_TYPES.String,
				to: CUSTOM_FIELD_TYPES.DateTime,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.STRING_TO_DATETIME')
			},
			{
				from: CUSTOM_FIELD_TYPES.String,
				to: CUSTOM_FIELD_TYPES.Number,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.STRING_TO_NUMBER')
			},
			{
				from: CUSTOM_FIELD_TYPES.YesNo,
				to: CUSTOM_FIELD_TYPES.List,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.YES_NO_TO_LIST')
			},
			{
				from: CUSTOM_FIELD_TYPES.YesNo,
				to: CUSTOM_FIELD_TYPES.String,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.YES_NO_TO_STRING')
			},
			{
				from: CUSTOM_FIELD_TYPES.YesNo,
				to: CUSTOM_FIELD_TYPES.Date,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.YES_NO_TO_DATE')
			},
			{
				from: CUSTOM_FIELD_TYPES.YesNo,
				to: CUSTOM_FIELD_TYPES.DateTime,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.YES_NO_TO_DATETIME')
			},
			{
				from: CUSTOM_FIELD_TYPES.YesNo,
				to: CUSTOM_FIELD_TYPES.Number,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.YES_NO_TO_NUMBER')
			},
			{
				from: CUSTOM_FIELD_TYPES.Date,
				to: CUSTOM_FIELD_TYPES.List,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATE_TO_LIST')
			},
			{
				from: CUSTOM_FIELD_TYPES.Date,
				to: CUSTOM_FIELD_TYPES.String,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATE_TO_STRING')
			},
			{
				from: CUSTOM_FIELD_TYPES.Date,
				to: CUSTOM_FIELD_TYPES.YesNo,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATE_TO_YES_NO')
			},
			{
				from: CUSTOM_FIELD_TYPES.Date,
				to: CUSTOM_FIELD_TYPES.DateTime,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATE_TO_DATETIME')
			},
			{
				from: CUSTOM_FIELD_TYPES.Date,
				to: CUSTOM_FIELD_TYPES.Number,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATE_TO_NUMBER')
			},
			{
				from: CUSTOM_FIELD_TYPES.DateTime,
				to: CUSTOM_FIELD_TYPES.List,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATETIME_TO_LIST')
			},
			{
				from: CUSTOM_FIELD_TYPES.DateTime,
				to: CUSTOM_FIELD_TYPES.String,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATETIME_TO_STRING')
			},
			{
				from: CUSTOM_FIELD_TYPES.DateTime,
				to: CUSTOM_FIELD_TYPES.YesNo,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATETIME_TO_YES_NO')
			},
			{
				from: CUSTOM_FIELD_TYPES.DateTime,
				to: CUSTOM_FIELD_TYPES.Date,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATETIME_TO_DATE')
			},
			{
				from: CUSTOM_FIELD_TYPES.DateTime,
				to: CUSTOM_FIELD_TYPES.Number,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.DATETIME_TO_NUMBER')
			},
			{
				from: CUSTOM_FIELD_TYPES.Number,
				to: CUSTOM_FIELD_TYPES.List,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.NUMBER_TO_LIST')
			},
			{
				from: CUSTOM_FIELD_TYPES.Number,
				to: CUSTOM_FIELD_TYPES.String,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.NUMBER_TO_STRING')
			},
			{
				from: CUSTOM_FIELD_TYPES.Number,
				to: CUSTOM_FIELD_TYPES.YesNo,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.NUMBER_TO_YES_NO')
			},
			{
				from: CUSTOM_FIELD_TYPES.Number,
				to: CUSTOM_FIELD_TYPES.Date,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.NUMBER_TO_DATE')
			},
			{
				from: CUSTOM_FIELD_TYPES.Number,
				to: CUSTOM_FIELD_TYPES.DateTime,
				getWarningMessage: this.translateMessage('FIELD_SETTINGS.CONVERSIONS.NUMBER_TO_DATETIME')
			}
		];

		return this.conversions;
	}

	private translateMessage(key: string): Function {
		return () => {
			return this.translate.transform(key);
		}
	}

	/*
	public getConfirmationMessage(from: string, to: string): string {
		const conversion = this.getConversion(from, to);

		return conversion ? conversion.message : '';
	}
	*/
	public findConversion(from: string, to: string): any {
		return this.getConversions().find((conversion) => conversion.from === from && conversion.to === to);
	}

}
