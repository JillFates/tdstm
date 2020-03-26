import {ModalType} from '../../../shared/model/constants';
import {FilterType} from 'tds-component-library';

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
				label: 'Description',
				property: 'comment',
				filterType: FilterType.text,
				width: 300,
				class: 'column-cursor-pointer'
			},
			{
				label: 'Asset',
				property: 'assetName',
				filterType: FilterType.text,
				class: 'column-cursor-pointer',
				width: 170
			},
			{
				label: 'Asset Type',
				property: 'assetType',
				filterType: FilterType.text,
				width: 170
			},
			{
				label: 'Category',
				property: 'category',
				filterType: FilterType.text,
				width: 170
			},
			{
				label: 'Archived',
				property: 'isResolved',
				filterType: FilterType.text,
				width: 170
			},
			{
				label: 'Created',
				property: 'dateCreated',
				filterType: FilterType.date,
				format: dateFormat,
				width: 170
			},
			{
				label: 'Created By',
				property: 'createdBy.name',
				filterType: FilterType.text,
				width: 170
			},
			{
				label: 'Updated',
				property: 'lastUpdated',
				filterType: FilterType.date,
				format: dateFormat,
				width: 170
			}
		];
	}
}
