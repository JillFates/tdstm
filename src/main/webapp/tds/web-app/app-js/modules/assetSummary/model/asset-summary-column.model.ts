export class AssetSummaryColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Bundle',
				property: 'bundle',
				type: 'text',
				width: 300
			},
			{
				label: 'Applications',
				property: 'application',
				type: 'text',
				width: 70
			},
			{
				label: 'Servers',
				property: 'server',
				type: 'text',
				width: 70
			},
			{
				label: 'Physical Devices',
				property: 'device',
				type: 'text',
				width: 80
			},
			{
				label: 'Databases',
				property: 'database',
				type: 'text',
				width: 70
			},
			{
				label: 'Logical Storage',
				property: 'storage',
				type: 'text',
				width: 80
			}
		];
	}
}