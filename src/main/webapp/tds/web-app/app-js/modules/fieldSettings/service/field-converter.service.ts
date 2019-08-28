import { Injectable } from '@angular/core';
import {TranslatePipe} from '../../../shared/pipes/translate.pipe';

@Injectable()
export class FieldConverterService {
	private conversions = [];

	constructor(private translate: TranslatePipe) {
		const transform = this.translate.transform.bind(this);

		this.conversions = [
			{
				from: 'list',
				to: 'string',
				message: transform('CONVERSIONS.LIST_TO_STRING')
			},
			{
				from: 'list',
				to: 'yesNo',
				message: transform('CONVERSIONS.LIST_YES_NO')
			},
			{
				from: 'list',
				to: 'date',
				message: transform('CONVERSIONS.LIST_TO_DATE')
			},
			{
				from: 'list',
				to: 'dateTime',
				message: transform('CONVERSIONS.LIST_TO_DATETIME')
			},
			{
				from: 'list',
				to: 'number',
				message: transform('CONVERSIONS.LIST_TO_NUMBER')
			},
			{
				from: 'string',
				to: 'list',
				message: transform('CONVERSIONS.STRING_TO_LIST')
			},
			{
				from: 'string',
				to: 'yesNo',
				message: transform('CONVERSIONS.STRING_TO_YES_NO')
			},
			{
				from: 'string',
				to: 'date',
				message: transform('CONVERSIONS.STRING_TO_DATE')
			},
			{
				from: 'string',
				to: 'dateTime',
				message: transform('CONVERSIONS.STRING_TO_DATETIME')
			},
			{
				from: 'string',
				to: 'number',
				message: transform('CONVERSIONS.STRING_TO_NUMBER')
			},
			{
				from: 'yesNo',
				to: 'list',
				message: transform('CONVERSIONS.YES_NO_TO_LIST')
			},
			{
				from: 'yesNo',
				to: 'string',
				message: transform('CONVERSIONS.YES_NO_TO_STRING')
			},
			{
				from: 'yesNo',
				to: 'date',
				message: transform('CONVERSIONS.YES_NO_TO_DATE')
			},
			{
				from: 'yesNo',
				to: 'dateTime',
				message: transform('CONVERSIONS.YES_NO_TO_DATETIME')
			},
			{
				from: 'yesNo',
				to: 'number',
				message: transform('CONVERSIONS.YES_NO_TO_NUMBER')
			},
			{
				from: 'date',
				to: 'list',
				message: transform('CONVERSIONS.DATE_TO_LIST')
			},
			{
				from: 'date',
				to: 'string',
				message: transform('CONVERSIONS.DATE_TO_STRING')
			},
			{
				from: 'date',
				to: 'yesNo',
				message: transform('CONVERSIONS.DATE_TO_YES_NO')
			},
			{
				from: 'date',
				to: 'dateTime',
				message: transform('CONVERSIONS.DATE_TO_DATETIME')
			},
			{
				from: 'date',
				to: 'number',
				message: transform('CONVERSIONS.DATE_TO_NUMBER')
			},
			{
				from: 'dateTime',
				to: 'list',
				message: transform('CONVERSIONS.DATETIME_TO_LIST')
			},
			{
				from: 'dateTime',
				to: 'string',
				message: transform('CONVERSIONS.DATETIME_TO_STRING')
			},
			{
				from: 'dateTime',
				to: 'yesNo',
				message: transform('CONVERSIONS.DATETIME_TO_YES_NO')
			},
			{
				from: 'dateTime',
				to: 'date',
				message: transform('CONVERSIONS.DATETIME_TO_DATE')
			},
			{
				from: 'dateTime',
				to: 'number',
				message: transform('CONVERSIONS.DATETIME_TO_NUMBER')
			},
			{
				from: 'number',
				to: 'list',
				message: transform('CONVERSIONS.NUMBER_TO_LIST')
			},
			{
				from: 'number',
				to: 'string',
				message: transform('CONVERSIONS.NUMBER_TO_STRING')
			},
			{
				from: 'number',
				to: 'yesNo',
				message: transform('CONVERSIONS.NUMBER_TO_YES_NO')
			},
			{
				from: 'number',
				to: 'date',
				message: transform('CONVERSIONS.NUMBER_TO_DATE')
			},
			{
				from: 'number',
				to: 'datetime',
				message: transform('CONVERSIONS.NUMBER_TO_DATETIME')
			}
		];
	}

	public getConfirmationMessage(from: string, to: string): string {
		const conversion = this.getConversion(from, to);

		return conversion ? conversion.message : '';
	}

	private getConversion(from: string, to: string): any {
		return this.conversions.find((conversion) => conversion.from === from && conversion.to === to);
	}

}
