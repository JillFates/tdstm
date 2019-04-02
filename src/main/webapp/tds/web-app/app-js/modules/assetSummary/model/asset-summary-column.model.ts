export class AssetSummaryColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Bundle',
				property: 'bundle.name',
				type: 'text',
				width: 300
			},
			{
				label: 'Applications',
				property: 'applicationCount',
				type: 'text',
				width: 70
			},
			{
				label: 'Servers',
				property: 'serverCount',
				type: 'text',
				width: 70
			},
			{
				label: 'Physical Devices',
				property: 'deviceCount',
				type: 'text',
				width: 80
			},
			{
				label: 'Databases',
				property: 'databaseCount',
				type: 'text',
				width: 70
			},
			{
				label: 'Logical Storage',
				property: 'storageCount',
				type: 'text',
				width: 80
			}
		];
	}
}