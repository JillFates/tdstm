import {DateUtils} from "../../../../shared/utils/date.utils";
export const YesNoList = ['Yes', 'No'];
const PriorityList = [1, 2, 3, 4, 5];


export class TaskEditCreateModelHelper {
	model: any;
	userTimeZone: string;

	constructor(userTimeZone: string) {
		this.model = {};
		this.userTimeZone = userTimeZone;
	}

	/**
	 * Set the model passing the all detail object
	 */
	public setModel(detail: any): any {
		const assetComment = detail['assetComment'] || {};
		const durationScale = assetComment.durationScale && assetComment.durationScale.name || null;
		const [yes, no] = YesNoList;

		const categories =  [...(detail.categories || [])];
		categories.push(''); // add empty default value

		this.model = {
			id: assetComment.id,
			note: '',
			duration: assetComment.duration,
			taskSpec: assetComment.taskSpec,
			taskNumber: assetComment.taskNumber,
			hardAssigned: Boolean(assetComment.hardAssigned === 1) ? yes : no,
			sendNotification: Boolean(assetComment.sendNotification) ? yes : no,
			durationScale,
			durationParts: DateUtils.getDurationParts(assetComment.duration, durationScale),
			durationLocked: assetComment.durationLocked,
			actualStart: detail.atStart ? detail.atStart : '',
			actualFinish: detail.dtResolved ? detail.dtResolved : '',
			dueDate: assetComment.dueDate ? new Date(DateUtils.formatUserDateTime(this.userTimeZone, assetComment.dueDate)) : '',
			estimatedStart: assetComment.estStart ? new Date(DateUtils.formatUserDateTime(this.userTimeZone, assetComment.estStart)) : '',
			estimatedFinish: assetComment.estFinish ? new Date(DateUtils.formatUserDateTime(this.userTimeZone, assetComment.estFinish))  : '',
			instructionLink: detail.instructionLink === '|' ? '' : detail.instructionLink,
			priority: assetComment.priority,
			assetName: detail.assetName,
			comment:  assetComment.comment || '',
			assetClass: {id: detail.assetClass, text: detail.assetClasses && detail.assetClasses[detail.assetClass] || ''},
			assetClasses: Object.keys(detail.assetClasses || {}).map((key: string) => ({id: key, text: detail.assetClasses[key]}) ),
			status: assetComment.status,
			statusList: [],
			personList: [],
			teamList: [],
			predecessorList: (detail.predecessorList || [])
				.map((item) => ({id: item.id, desc: `${item.taskNumber}: ${item.desc}`, model: {id: item.id, text: `${item.taskNumber}: ${item.desc}` }})),
			successorList: (detail.successorList || [])
				.map((item) => ({id: item.id, desc: `${item.taskNumber}: ${item.desc}`, model: {id: item.id, text: `${item.taskNumber}: ${item.desc}` }})),
			apiActionList: (detail.apiActionList || []).map((action) => ({id: action.id, text: action.name})),
			categoriesList: categories.sort(),
			eventList: (detail.eventList || []).map((event) => ({id: event.id, text: event.name})),
			priorityList: [...PriorityList],
			asset: {id: detail.assetId, text: detail.assetName},
			assignedTo: {id : (assetComment.assignedTo && assetComment.assignedTo.id) || null, text: detail.assignedTo},
			assignedTeam: {id: assetComment.role, text: detail.roles},
			event: {id: (assetComment.moveEvent && assetComment.moveEvent.id) || null, text: detail.eventName},
			category: assetComment.category,
			apiAction: {id: detail.apiAction && detail.apiAction.id || '', text: detail.apiAction && detail.apiAction.name || ''},
			deletedPredecessorList: [],
			deletedSuccessorList: []
		};

		return this.model;
	}

	public getPayloadForUpdate(): any {
		const deletedItems = this.model.deletedPredecessorList
			.concat(this.model.deletedSuccessorList)
			.join(',');

		return  {
			assetClass: this.model.assetClass.id,
			assetEntity: this.model.asset.id.toString(),
			assetType: this.model.assetClass.text, // 'Application', /* ? */
			assignedTo: this.model.assignedTo.id.toString(),
			category: this.model.category,
			apiAction: this.model.apiAction.id,
			actionInvocable: '',
			actionMode: '',
			comment: this.model.comment,
			commentFromId: '',
			commentId: this.model.id.toString(),
			commentType: 'issue',
			deletePredId: '',  /* ? */
			dueDate: this.model.dueDate ? this.model.dueDate.toISOString() : '',
			duration: DateUtils.convertDurationPartsToMinutes(this.model.durationParts).toString(),
			durationScale: this.model.durationScale,
			estFinish: this.model.estimatedFinish ? this.model.estimatedFinish.toISOString() : '',
			estStart: this.model.estimatedStart ? this.model.estimatedStart.toISOString() : '',
			forWhom: '',
			hardAssigned: this.model.hardAssigned === 'No' ? "0" : "1",
			sendNotification: this.model.sendNotification === 'Yes',
			isResolved: "0", /* ? */
			instructionsLink: this.model.instructionLink,
			moveEvent: this.model.event.id.toString(),
			mustVerify: "0", /* ? */
			override: "0", /* ? */
			predCount: "-1", /* ? */
			predecessorCategory: '', /* ? */
			prevAsset: '', /* ? */
			priority: this.model.priority.toString(), /* ? */
			resolution: '', /* ? */
			role: this.model.assignedTeam.id,
			status: this.model.status,
			// manageDependency: "1", /* ? */
			// taskDependency: [], /* ? */
			// taskSuccessor: [], /* ? */
			// deletedPreds: deletedItems
			workflowTransition: '', /* ? */
			canEdit: true, /* ? */
			durationLocked: this.model.durationLocked,
			durationText: `${this.model.durationParts.days} days ${this.model.durationParts.hours} hrs ${this.model.durationParts.minutes} mins`,
			taskNumber: this.model.taskNumber.toString(),
			note: this.model.note,
			id: this.model.id,
			apiActionId: this.model.apiAction.id.toString() || "0"
		};

		// taskDependency: '3002_233386', /* ? */
		// taskSuccessor: '3012_233398', /* ? */
	}

	public toggleLocked() {
		this.model.locked = !this.model.locked;
	}

	public addPredecessor(index: number, id: string, text: string) {
		this.model.predecessorList[index] = this.makeTaskItem(id, text) ;
	}

	public addSuccessor(index: number, id: string, text: string) {
		this.model.successorList[index] = this.makeTaskItem(id, text);
	}

	public deleteSuccessor(index: number): boolean {
		return this.deleteTaskItem(index, this.model.successorList, this.model.deletedSuccessorList);
	}

	public deletePredecessor(index: number): boolean {
		return this.deleteTaskItem(index, this.model.predecessorList, this.model.deletedPredecessorList);
	}

	public getPredecessor(): any[] {
		return this.model.predecessorList;
	}

	public getSuccessor(): any[] {
		return this.model.successorList;
	}

	private deleteTaskItem(index: number, collection: any[], deletedCollection: any[]): boolean {
			const id = collection[index].id;
			const exists = Boolean(id);

			if (exists) {
				deletedCollection.push(id);
			}

			collection.splice(index, 1);
			return exists;
	}

	private makeTaskItem(id: string, text: string): any {
		return { id, desc: text, model: {id , text} };
	}


}





