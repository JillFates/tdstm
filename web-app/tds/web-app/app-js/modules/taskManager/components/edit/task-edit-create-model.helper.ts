import {DateUtils} from "../../../../shared/utils/date.utils";
export const YesNoList = ['Yes', 'No'];
const PriorityList = [1, 2, 3, 4, 5];


export interface ITask {
	id: string | number;
	taskId: string | number;
	taskNumber: string | number ,
	category: string;
	status: string;
	desc: string;
	model: {
		id: string | number;
		text: string;
	}
}


export class TaskEditCreateModelHelper {
	model: any;
	private userTimeZone: string;
	private dataSignatureDependencyTasks: string;

	constructor(userTimeZone: string) {
		this.model = {};
		this.userTimeZone = userTimeZone;
	}

	/**
	 * Set the model passing the all detail object
	 */
	public setModel(task: any): any {
		const detail = task.detail;
		const assetComment = detail['assetComment'] || {};
		const durationScale = assetComment.durationScale && assetComment.durationScale.name || null;
		const [yes, no] = YesNoList;

		const categories =  [...(detail.categories || [])];
		categories.push(''); // add empty default value

		this.model = {
			title: task.modal.title,
			recipe: detail.recipe || null,
			id: assetComment.id,
			personCreateObj: detail.personCreateObj || '',
			note: '',
			duration: assetComment.duration,
			dateCreated: assetComment.dateCreated,
			taskSpec: assetComment.taskSpec,
			lastUpdated: assetComment.lastUpdated,
			taskSpecId: detail.taskSpecId || '',
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
			originalPredecessorList: (detail.predecessorList || []),
			originalSuccessorList: (detail.successorList || []),
			predecessorList: this.extractDependencyTasks (detail.predecessorList || []),
			successorList: this.extractDependencyTasks(detail.successorList || []),
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

		this.dataSignatureDependencyTasks = JSON.stringify({predecessors: this.model.predecessorList, successors: this.model.successorList});

		return this.model;
	}

	private extractDependencyTasks(array: any[]): any[] {
		return array
			.map((task) => this.makeTaskItem(task));
	}

	private getTaskAdded(tasks: any[], originalTasks: any[]) : string[] {
		return tasks
			.filter(task => !originalTasks.find(original => original.id === task.id))
			.map((task, index) => (`-${index + 1}_${task.id}`));
	}

	private getTaskPrevious(tasks: any[], originalTasks: any[]) : string[] {
		return tasks
			.filter(task => originalTasks.find(original => original.id === task.id))
			.map((task, index) => (`${task.id}_${task.taskNumber}`));
	}

	public getPayloadForUpdate(): any {
		const {
			id, assetClass, predecessorList, successorList, originalPredecessorList, originalSuccessorList,
			asset, dueDate, durationParts, estimatedFinish, estimatedStart, taskNumber, note, durationLocked,
			hardAssigned, sendNotification, instructionLink, event, category, apiAction, comment,
			priority, assignedTeam, status, assignedTo, durationScale} = this.model;

		const deletedItems = this.model.deletedPredecessorList
			.concat(this.model.deletedSuccessorList)
			.join(',');

		return  {
			assetClass: assetClass.id,
			assetEntity: asset.id.toString(),
			assetType: assetClass.text, // 'Application', /* ? */
			assignedTo: assignedTo.id.toString(),
			category: category,
			apiAction: apiAction.id,
			apiActionId: apiAction.id.toString() || "0",
			actionInvocable: '',
			actionMode: '',
			comment: comment,
			commentFromId: '',
			commentId: id.toString(),
			commentType: 'issue',
			deletePredId: '',  /* ? */
			dueDate: dueDate ? dueDate.toISOString() : '',
			duration: DateUtils.convertDurationPartsToMinutes(durationParts).toString(),
			durationScale: durationScale,
			estFinish: estimatedFinish ? estimatedFinish.toISOString() : '',
			estStart: estimatedStart ? estimatedStart.toISOString() : '',
			forWhom: '',
			hardAssigned: hardAssigned === 'No' ? "0" : "1",
			sendNotification: sendNotification === 'Yes',
			isResolved: "0", /* ? */
			instructionsLink: instructionLink,
			moveEvent: event.id.toString(),
			mustVerify: "0", /* ? */
			override: "0", /* ? */
			predCount: "-1", /* ? */
			predecessorCategory: '', /* ? */
			prevAsset: '', /* ? */
			priority: priority.toString(), /* ? */
			resolution: '', /* ? */
			role: assignedTeam.id,
			status: status,
			manageDependency: "1",
			taskDependency: this.getTaskAdded(predecessorList, originalPredecessorList)
				.concat(this.getTaskPrevious(predecessorList, originalPredecessorList)),
			taskSuccessor: this.getTaskAdded(successorList, originalSuccessorList)
				.concat(this.getTaskPrevious(successorList, originalSuccessorList)),
			deletedPreds: deletedItems,
			workflowTransition: '', /* ? */
			canEdit: true, /* ? */
			durationLocked: durationLocked,
			durationText: `${durationParts.days} days ${durationParts.hours} hrs ${durationParts.minutes} mins`,
			taskNumber: taskNumber.toString(),
			note: note,
			id: id
		};

		// taskDependency: '3002_233386', /* ? */
		// taskSuccessor: '3012_233398', /* ? */
	}

	/**
	 * Determine if predecessor/succesor collections contain changes
	 * @returns {boolean}
	 */
	hasDependencyTasksChanges(): boolean {
		return this.dataSignatureDependencyTasks !== JSON.stringify({predecessors: this.model.predecessorList, successors: this.model.successorList});
	}

	hasDuplicatedPredecessors(): boolean {
		return this.hasDuplicates(this.model.predecessorList);
	}

	hasDuplicatedSuccessors(): boolean {
		return this.hasDuplicates(this.model.successorList);
	}

	/**
	 * Determine if the array of objects passed as argument has duplicated id properties
	 * @param {any[]} array
	 * @returns {boolean}
	 */
	private hasDuplicates(array: any[]): boolean {
		return this.extractDistinctIds(array).length < array.length;
	}

	private extractDistinctIds(array): string[] {
		const ids = [];

		array.forEach((item) => {
			let idField = 'id';

			if (item.taskId) {
				// existing tasks have taskId as key, new ones use id
				idField = 'taskId';
			}
			if (ids.indexOf(item[idField]) === -1) {
				ids.push(item[idField]);
			}
		});

		return ids;
	}

	hasDoubleAssignment(): boolean {
		const predecessors = this.extractDistinctIds(this.model.predecessorList).filter((id) => id);
		const successors = this.extractDistinctIds(this.model.successorList).filter((id) => id);

		console.log('P', this.model.predecessorList);
		console.log('S', this.model.successorList);

		return predecessors.some((p) => successors.indexOf(p) >= 0)
			||
			successors.some((s) =>predecessors.indexOf(s) >= 0);
	}

	/**
	 * Determine if the array of objects passed as argument contains empty ids
	 * @param {any[]} array
	 * @returns {boolean}
	 */
	private hasEmptyIds(array: any[]): boolean {
		return array.filter((item) => item.id === '').length > 0;
	}

	/**
	 * Determine if predecessor/successor collections contains invalid data, like duplicates or empty ids
	 * @returns {boolean}
	 */
	hasInvalidTasksDependencies(): boolean {
		const {predecessorList, successorList} = this.model;

		return this.hasDuplicates(predecessorList) ||
			this.hasDuplicates(successorList) ||
			this.hasEmptyIds(predecessorList) ||
			this.hasEmptyIds(successorList) ||
			this.hasDoubleAssignment();
	}


	toggleLocked() {
		this.model.locked = !this.model.locked;
	}

	addPredecessor(index: number, task: any): ITask {
		return this.model.predecessorList[index] = this.makeTaskItem(task) ;
	}

	addSuccessor(index: number, task: any): ITask {
		return this.model.successorList[index] = this.makeTaskItem(task);
	}

	deleteSuccessor(index: number): boolean {
		return this.deleteTaskItem(index, this.model.successorList, this.model.deletedSuccessorList);
	}

	deletePredecessor(index: number): boolean {
		return this.deleteTaskItem(index, this.model.predecessorList, this.model.deletedPredecessorList);
	}

	getPredecessor(): any[] {
		return this.model.predecessorList;
	}

	getSuccessor(): any[] {
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

	private makeTaskItem(task: any): ITask {
		const {id, desc='', taskId = '', taskNumber, category, status, text=''} = task;

		return {
			id,
			taskId,
			category,
			status,
			taskNumber,
			desc: desc || text,
			model: {
				id,
				text: desc || text
			}
		}
	}
}





