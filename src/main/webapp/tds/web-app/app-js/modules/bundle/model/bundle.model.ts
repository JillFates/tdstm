import {ModalType} from '../../../shared/model/constants';

export class BundleModel {
	public id?: any;
	public name: string;
	public description: string;
	public fromId: number;
	public toId: number;
	public startTime: Date;
	public completionTime: Date;
	public projectManagerId: number;
	public moveManagerId: number;
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
				type: 'text',
				width: '*'
			},
			{
				label: 'Description',
				property: 'description',
				type: 'text',
				width: '*'
			},
			{
				label: 'Planning',
				property: 'planning',
				type: 'boolean',
				width: 100
			},
			{
				label: 'Asset Quantity',
				property: 'assetqty',
				type: 'number',
				width: 225
			},
			{
				label: 'Start Time',
				property: 'startDate',
				type: 'date',
				format: dateFormat,
				width: 'auto'
			},
			{
				label: 'Completion Time',
				property: 'completion',
				type: 'date',
				format: dateFormat,
				width: 'auto'
			}
		];
	}
}
