export class AssetCommentColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 60,
				locked: true
			},
			{
				label: 'Description',
				property: 'commentInstance.comment',
				type: 'text',
				width: 798
			},
			{
				label: 'Updated',
				property: 'commentInstance.lastUpdated',
				type: 'date',
				format: dateFormat,
				width: 170
			},
			{
				label: 'Asset',
				property: 'assetName',
				type: 'text',
				width: 455
			},
			{
				label: 'AssetType',
				property: 'assetType',
				type: 'text',
				width: 170
			},
			{
				label: 'Category',
				property: 'commentInstance.category',
				type: 'text',
				width: 170
			}
		];
	}
}

export class AssetCommentModel {
	id?: number;
	name: string;
	description?: string;
	comment?: string;
	dateCreated?: Date;
}
