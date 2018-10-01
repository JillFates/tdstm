import {DateUtils} from '../../../../shared/utils/date.utils';
import {clone} from 'ramda';
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
	private userCurrentDateFormat: string;
	private userCurrentDateTimeFormat: string;
	private dataSignatureDependencyTasks: string;

	constructor(userTimeZone: string, userCurrentDateFormat: string) {
		this.model = {};

		this.userTimeZone = userTimeZone;
		this.userCurrentDateFormat = userCurrentDateFormat;
		this.userCurrentDateTimeFormat =  `${userCurrentDateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}`;
	}

	public setModel(model: any): any {
		this.model = model;

		return model;
	}

	/**
	 * Set the model passing the all detail object
	 */
	public cleanAndSetModel(task: any): any {
		const detail = clone(task.detail);
		const assetComment = detail['assetComment'] || {};
		const durationScale = assetComment.durationScale && assetComment.durationScale.name || null;
		const [yes, no] = YesNoList;

		const categories =  [...(detail.categories || [])];
		categories.push(''); // add empty default value
		let  instructionLink = '';
		if (!detail.instructionsLinkLabel  && detail.instructionsLinkURL)  {
			instructionLink = detail.instructionsLinkURL;
		} else {
			if (detail.instructionsLinkLabel && detail.instructionsLinkURL) {
				instructionLink = `${detail.instructionsLinkLabel}|${detail.instructionsLinkURL}`;
			}
		}

		this.model = {
			title: task.modal.title,
			workflow: detail.workflow || '',
			workflowTransitionName: assetComment.workflowTransition && assetComment.workflowTransition.name || '',
			durationDelta: detail.durationDelta || '',
			recipe: detail.recipe || null,
			id: assetComment.id,
			personCreateObj: detail.personCreateObj || '',
			note: '',
			duration: assetComment.duration,
			durationText: DateUtils.formatDuration(assetComment.duration, durationScale),
			actualDuration: detail.actualDuration || '',
			dateCreated: assetComment.dateCreated,
			taskSpec: assetComment.taskSpec,
			lastUpdated: assetComment.lastUpdated,
			taskSpecId: detail.taskSpecId || '',
			taskNumber: assetComment.taskNumber,
			hardAssigned: Boolean(assetComment.hardAssigned === 1) ? yes : no,
			sendNotification: Boolean(assetComment.sendNotification) ? yes : no,
			durationScale,
			durationParts: DateUtils.getDurationParts(assetComment.duration, durationScale),
			locked: assetComment.durationLocked,
			actualStart: detail.atStart ? detail.atStart : '',
			actualFinish: detail.dtResolved ? detail.dtResolved : '',
			dueDate: assetComment.dueDate ? new Date(DateUtils.formatUserDateTime(this.userTimeZone, assetComment.dueDate)) : '',
			estimatedStart: assetComment.estStart ? new Date(DateUtils.formatUserDateTime(this.userTimeZone, assetComment.estStart)) : '',
			estimatedFinish: assetComment.estFinish ? new Date(DateUtils.formatUserDateTime(this.userTimeZone, assetComment.estFinish))  : '',
			instructionLink,
			instructionsLinkLabel: detail.instructionsLinkLabel || '',
			instructionsLinkURL: detail.instructionsLinkURL || '',
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
			notesList: (detail.notes || []),
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

	private getTaskAdded(tasks: any[], originalTasks: any[]): string[] {
		return tasks
			.filter(task => !originalTasks.find(original => original.id === task.id))
			.map((task, index) => (`-${index + 1}_${task.id}`));
	}

	private getTaskPrevious(tasks: any[], originalTasks: any[]): string[] {
		return tasks
			.filter(task => originalTasks.find(original => original.id === task.id))
			.map((task, index) => (`${task.id}_${task.taskNumber}`));
	}

	public getPayloadForUpdate(): any {
		const [Yes, No] = YesNoList;
		const {
			id, assetClass, predecessorList, successorList, originalPredecessorList, originalSuccessorList,
			asset, dueDate, durationParts, estimatedFinish, estimatedStart, taskNumber, note, locked,
			hardAssigned, sendNotification, instructionLink, event, category, apiAction, comment,
			priority, assignedTeam, status, assignedTo, durationScale} = this.model;

		const deletedItems = this.model.deletedPredecessorList
			.concat(this.model.deletedSuccessorList)
			.join(',');

		return  {
			assetClass: assetClass.id,
			assetEntity: this.getEmptyStringIfNull(asset && asset.id).toString(),
			assetType: assetClass.text,
			assignedTo: this.getEmptyStringIfNull(assignedTo && assignedTo.id).toString(),
			category: category,
			apiAction: this.getEmptyStringIfNull(apiAction && apiAction.id),
			apiActionId: this.getEmptyStringIfNull(apiAction && apiAction.id).toString() || '0',
			actionInvocable: '',
			actionMode: '',
			comment: comment,
			commentFromId: '',
			commentId: this.getEmptyStringIfNull(id).toString(),
			commentType: 'issue',
			deletePredId: '',  /* ? */
			dueDate: dueDate ? DateUtils.formatDate(dueDate, this.userCurrentDateFormat) : '',
			duration: DateUtils.convertDurationPartsToMinutes(durationParts).toString(),
			durationScale: durationScale,
			estFinish: estimatedFinish ? DateUtils.formatDate(estimatedFinish, this.userCurrentDateTimeFormat) : '',
			estStart: estimatedStart ? DateUtils.formatDate(estimatedStart, this.userCurrentDateTimeFormat) : '',
			forWhom: '',
			hardAssigned: hardAssigned === No ? '0' : '1',
			sendNotification: sendNotification ===  No ? '0' : '1',
			isResolved: '0', /* ? */
			instructionsLink: this.addProtocolToLabelURL(instructionLink),
			moveEvent: this.getEmptyStringIfNull(event && event.id).toString(),
			mustVerify: '0',
			override: '0',
			predCount: '-1',
			predecessorCategory: '',
			prevAsset: '',
			priority: priority.toString(),
			resolution: '',
			role: this.getEmptyStringIfNull(assignedTeam && assignedTeam.id),
			status: status,
			manageDependency: '1',
			taskDependency: this.getTaskAdded(predecessorList, originalPredecessorList)
				.concat(this.getTaskPrevious(predecessorList, originalPredecessorList)),
			taskSuccessor: this.getTaskAdded(successorList, originalSuccessorList)
				.concat(this.getTaskPrevious(successorList, originalSuccessorList)),
			deletedPreds: deletedItems,
			workflowTransition: '',
			canEdit: true, /* ? */
			durationLocked: locked ? '1' : '0',
			durationText: `${durationParts.days} days ${durationParts.hours} hrs ${durationParts.minutes} mins`,
			taskNumber: taskNumber.toString(),
			note: note,
			id: id
		};
	}

	getEmptyStringIfNull(value: any): any {
		return value === null || typeof value === 'undefined' || value === false ?  '' : value;
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
	private addProtocolToLabelURL(labelURL = ''): string {
		const separator = '|';
		let isJustURL = false;
		let [label, url] = labelURL.split(separator);

		if (!url) {
			isJustURL = true;
			url = label;
		}
		url = url.toLowerCase();
		if (url && !url.startsWith('http://') && !url.startsWith('https://') ) {
			url = 'http://' + url;
		}

		return isJustURL ? url : [label, url].join(separator);
	}

	hasDoubleAssignment(): boolean {
		const predecessors = this.extractDistinctIds(this.model.predecessorList).filter((id) => id);
		const successors = this.extractDistinctIds(this.model.successorList).filter((id) => id);

		return predecessors.some((p) => successors.indexOf(p) >= 0)
			||
			successors.some((s) => predecessors.indexOf(s) >= 0);
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
		const {id, desc = '', taskId = '', taskNumber, category, status, text = ''} = task;

		const description = this.removeTaskNumberFromDescription(desc || text, taskNumber);

		return {
			id,
			taskId,
			category,
			status,
			taskNumber,
			desc: description,
			model: {
				id,
				text: description
			}
		}
	}

	private removeTaskNumberFromDescription(description: string, taskNumber: string): string {
		return description.replace(new RegExp(`^${taskNumber}: `), '')
	}

	/**
	 * Create the structure of the notes from the simple array returned in the API
	 * @param notes
	 * @returns {Array<any>}
	 */
	public generateNotes(notes: any): Array<any> {
		let noteList = new Array<any>();
		notes.forEach((item: string[]) => {
			const [dateCreated, createdBy, note] = item;
			noteList.push({ dateCreated, createdBy, note });
		});

		return noteList;
	}

	/**
	 * Change the background color based on the task status
	 * @param {any} context
	 * @returns {string}
	 */
	public rowStatusColor(context: any) {
		const status = context.dataItem && context.dataItem.status || '';
		return 'task-' + status.toLowerCase();
	}
}
