import {ModalType} from '../../../shared/model/constants';

export class BundleModel {
	public name: string;
	public description: string;
	public fromId: number;
	public toId: number;
	public startTime: string;
	public completionTime: string;
	public projectManagerId: number;
	public moveManagerId: number;
	public operationalOrder: number;
	public workflowCode: string;
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
				width: 70
			},
			{
				label: 'Asset Qty',
				property: 'assetqty',
				type: 'number',
				width: 'auto'
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