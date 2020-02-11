import {FilterType} from 'tds-component-library';

export class ProviderColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				filterType: FilterType.text,
				width: 246,
			},
			{
				label: 'Description',
				property: 'description',
				filterType: FilterType.text,
				width: 300,
			},
			{
				label: 'Date Create',
				property: 'dateCreated',
				format: dateFormat,
				filterType: FilterType.date,
				width: 170
			}
		];
	}
}

export class ProviderModel {
	id?: number;
	name: string;
	description?: string;
	comment?: string;
	dateCreated?: Date;
}
