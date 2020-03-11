import {ModalType} from '../../../shared/model/constants';

export class AssetCommentModel {
	public id?: string;
	public modal: {
		title?:  string;
		type: ModalType
	};
	public archive: boolean;
	public comment: string;
	public category: string;
	public assetClass: {
		id?: string;
		text?: string;
	};
	public asset: {
		id?: any;
		text: string;
	};
	public lastUpdated?: string;
	public dateCreated?: string;
}

export class AssetCommentColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 52,
				locked: true
			},
			{
				label: 'Description',
				property: 'comment',
				type: 'text',
				width: 574,
				locked: true,
				class: 'column-cursor-pointer'
			},
			{
				label: 'Asset',
				property: 'assetName',
				type: 'text',
				class: 'column-cursor-pointer',
				width: 170
			},
			{
				label: 'Asset Type',
				property: 'assetType',
				type: 'text',
				width: 170
			},
			{
				label: 'Category',
				property: 'category',
				type: 'text',
				width: 170
			},
			{
				label: 'Archived',
				property: 'isResolved',
				type: 'text',
				width: 170
			},
			{
				label: 'Created',
				property: 'dateCreated',
				type: 'date',
				format: dateFormat,
				width: 170
			},
			{
				label: 'Created By',
				property: 'createdBy.name',
				type: 'text',
				width: 170
			},
			{
				label: 'Updated',
				property: 'lastUpdated',
				type: 'date',
				format: dateFormat,
				width: 170
			}
		];
	}
}
