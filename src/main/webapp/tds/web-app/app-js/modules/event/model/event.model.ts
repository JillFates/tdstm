export class EventColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Name',
				property: 'name',
				type: 'text',
				width: 'auto'
			}, {
				label: 'Estimated Start',
				property: 'estStartTime',
				type: 'date',
				format: dateFormat,
				width: 'auto',
			}, {
				label: 'Estimated Completion',
				property: 'estCompletionTime',
				type: 'date',
				format: dateFormat,
				width: 'auto',
			}, {
				label: 'Description',
				property: 'description',
				type: 'text',
				width: 'auto',
			}, {
				label: 'Runbook Status',
				property: 'runbookStatus',
				type: 'text',
				width: 'auto'
			}, {
				label: 'Bundles',
				property: 'moveBundlesString',
				type: 'text',
				width: 'auto'
			}
		];
	}
}

export class EventModel {
	name: string;
	description?: string;
	tagIds?: number[];
	moveBundle?: number[];
	runbookStatus?: string;
	runbookBridge1?: string;
	runbookBridge2?: string;
	videolink?: string;
	estStartTime?: Date;
	estCompletionTime?: Date;
	apiActionBypass: boolean;
}
