import {FilterType} from 'tds-component-library';

export class ManufacturerColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				filterType: FilterType.text,
				width: 'auto'
			},
			{
				label: 'AKA',
				property: 'alias',
				filterType: FilterType.text,
				width: 'auto'
			},
			{
				label: 'Description',
				property: 'description',
				filterType: FilterType.text,
				width: 'auto'
			},
			{
				label: 'Corporate Name',
				property: 'corporateName',
				filterType: FilterType.text,
				width: 'auto'
			},
			{
				label: 'Corporate Location',
				property: 'corporateLocation',
				filterType: FilterType.text,
				width: 'auto'
			},
			{
				label: 'Website',
				property: 'website',
				filterType: FilterType.text,
				width: 'auto'
			},
			{
				label: 'Models',
				property: 'modelsCount',
				filterType: FilterType.number,
				width: 10,
			},
			{
				label: 'Asset Count',
				property: 'assetCount',
				filterType: FilterType.number,
				width: 10,
			}
		];
	}
}

export class ManufacturerModel {
	id?: number;
	name: string;
	alias?: string;
	description?: string;
	corporateName?: string;
	corporateLocation?: string;
	website?: string;
	models?: number;
	assetCount?: number;
	aliases?: any[]
}