import {FilterType} from 'tds-component-library';

export class ManufacturerColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				filterType: FilterType.text,
				width: 200
			},
			{
				label: 'AKA',
				property: 'alias',
				filterType: FilterType.text,
				width: 250
			},
			{
				label: 'Description',
				property: 'description',
				filterType: FilterType.text,
				width: 300
			},
			{
				label: 'Corporate Name',
				property: 'corporateName',
				filterType: FilterType.text,
				width: 200
			},
			{
				label: 'Corporate Location',
				property: 'corporateLocation',
				filterType: FilterType.text,
				width: 200
			},
			{
				label: 'Website',
				property: 'website',
				filterType: FilterType.text,
				width: 130
			},
			{
				label: 'Models',
				property: 'modelsCount',
				filterType: FilterType.number,
				width: 100,
			},
			{
				label: 'Asset Count',
				property: 'assetCount',
				filterType: FilterType.number,
				width: 120,
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