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
	id: number;
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
};

export enum EventRowType {
	Header,
	Percents,
	Tasks,
	PlannedStart,
	PlannedCompletion,
	ActualStart,
	ActualCompletion
}

export class EventPlanStatus {
	dialIndicator: number;
	cssClass: string;
	dayTime: string;
	description: string;
	eventTitle: string;
	startDate: string;
	status: string;

	constructor() {
		this.dialIndicator = 0;
		this.cssClass = '';
		this.dayTime = '';
		this.description = '';
		this.eventTitle = '';
		this.status = '';
	}
}

export class TaskSummaryStep {
	columnNumber: number;
	status: {
		value: string;
		classes: string;
	};
	tasks: string;
	plannedStart: string;
	plannedCompletion: string;
	actualStart: string;
	actualCompletion: string;
}
