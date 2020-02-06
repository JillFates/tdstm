import {ModalType} from '../../../shared/model/constants';

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
				filterable: true,
				property: 'name',
				type: 'text',
				width: '*'
			},
			{
				label: 'Description',
				filterable: true,
				property: 'description',
				type: 'text',
				width: '*'
			},
			{
				label: 'Planning',
				filterable: true,
				property: 'planning',
				type: 'boolean',
				width: 110
			},
			{
				label: 'Asset Quantity',
				filterable: true,
				property: 'assetqty',
				type: 'number',
				width: 225
			},
			{
				label: 'Start Time',
				filterable: true,
				property: 'startDate',
				type: 'date',
				format: dateFormat,
				width: 'auto'
			},
			{
				label: 'Completion Time',
				filterable: true,
				property: 'completion',
				type: 'date',
				format: dateFormat,
				width: 'auto'
			}
		];
	}
}
