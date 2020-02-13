import {ModalType} from '../../../shared/model/constants';
import {FilterType} from 'tds-component-library';

export class BundleModel {
	public id?: any;
	public name: string;
	public description: string;
	public fromId: number;
	public toId: number;
	public startTime: any;
	public completionTime: any;
	public moveEvent: any;
	public operationalOrder: number;
	public useForPlanning: boolean;
}

export class BundleColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				filterType: FilterType.text,
				width: '*'
			},
			{
				label: 'Description',
				property: 'description',
				filterType: FilterType.text,
				width: '*'
			},
			{
				label: 'Planning',
				property: 'planning',
				filterType: FilterType.boolean,
				filterInputData: {
					data: [
						{
							text: 'True',
							value: true,
						},
						{
							text: 'False',
							value: false,
						},
					],
					defaultItem: { text: '', value: null },
				},
				width: 110
			},
			{
				label: 'Asset Quantity',
				property: 'assetqty',
				filterType: FilterType.number,
				width: 225
			},
			{
				label: 'Start Time',
				property: 'startDate',
				filterType: FilterType.date,
				format: dateFormat,
				width: 'auto'
			},
			{
				label: 'Completion Time',
				property: 'completion',
				filterType: FilterType.date,
				format: dateFormat,
				width: 'auto'
			}
		];
	}
}
