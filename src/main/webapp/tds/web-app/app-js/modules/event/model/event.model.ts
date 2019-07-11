export class EventModel {
	id: number;
	name: string;
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
