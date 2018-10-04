import {DateUtils} from '../../../../shared/utils/date.utils';
import {clone} from 'ramda';
import {TaskDetailModel} from './task-detail.model';
export const YesNoList = ['Yes', 'No'];

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

	/**
	 * Set the model and keep the reference to the initial data to detect changes
	 * @param {any} model Model to set
	 * @returns {any}
	 */
	public setModel(model: any): any {
		this.model = model;

		this.dataSignatureDependencyTasks = JSON.stringify({predecessors: this.model.predecessorList, successors: this.model.successorList});

		return model;
	}

	/**
	 * Concatenates the label and url part of the labelURL field
	 * @param {any} Object which contains the parts
	 * @returns {string}
	 */
	public getInstructionsLink(detail: any): string {
		let  instructionLink = '';

		if (!detail.instructionsLinkLabel  && detail.instructionsLinkURL)  {
			instructionLink = detail.instructionsLinkURL;
		} else {
			if (detail.instructionsLinkLabel && detail.instructionsLinkURL) {
				instructionLink = `${detail.instructionsLinkLabel}|${detail.instructionsLinkURL}`;
			}
		}
		return instructionLink;
	}

	/**
	 * Get an empty model, used for create task component
	 * @returns {any}
	 */
	public getEmptyModel(detailModel: TaskDetailModel): any {
		const [yes, no] = YesNoList;
		const defaultDurationScale = 'M';

		this.model = {
			title: detailModel.modal.title ,
			workflow: '' ,
			workflowTransitionName: '' ,
			durationDelta: '' ,
			recipe: '' ,
			id: detailModel.id,
			personCreateObj:  '',
			note: '',
			duration: 0,
			durationText: '',
			actualDuration:  '',
			dateCreated: '',
			taskSpec: '',
			lastUpdated: '',
			taskSpecId: '',
			taskNumber: '',
			hardAssigned:  no,
			sendNotification:  no,
			durationScale: defaultDurationScale,
			durationParts: '',
			locked: false,
			actualStart:  '',
			actualFinish:  '',
			dueDate:  '',
			estimatedStart:  '',
			estimatedFinish:  '',
			instructionLink: '',
			instructionsLinkLabel: '',
			instructionsLinkURL:  '',
			priority: 3,
			assetName: '',
			comment:  '',
			assetClass: {id: null, text: ''},
			assetClasses: [{id: '', text: ''}],
			status: 'Ready',
			statusList: [],
			personList: [],
			teamList: [],
			originalPredecessorList: [],
			originalSuccessorList: [],
			notesList: [],
			predecessorList: [],
			successorList: [],
			apiActionList: [{id: '', text: ''}],
			categoriesList: [],
			eventList: [{id: '', text: ''}],
			priorityList: [],
			asset: {id: '', text: ''},
			assignedTo: {id : '' , text: ''},
			assignedTeam: {id: '', text: ''},
			event: {id: '', text: ''},
			category: '',
			apiAction: {id: '', text: ''},
			deletedPredecessorList: [],
			deletedSuccessorList: []
		};

		this.dataSignatureDependencyTasks = JSON.stringify({predecessors: this.model.predecessorList, successors: this.model.successorList});

		return this.model;
	}

	/**
	 * Set the model passing the all detail object
	 * @param {any} task Holds the detail object coming from the service, this will be extracted and cleaned
	 * @returns {any}
	 */
	public cleanAndSetModel(task: any): any {
		const detail = clone(task.detail);
		const assetComment = detail['assetComment'] || {};
		const durationScale = assetComment.durationScale && assetComment.durationScale.name || null;
		const [yes, no] = YesNoList;

		const categories =  [...(detail.categories || [])];
		categories.push(''); // add empty default value
		const  instructionLink = this.getInstructionsLink(detail);

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
			priorityList: detail.priorityList,
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

	/**
	 * Convert the array provided by the service to a task array
	 * @param {any[]} array coming from service holding task info
	 * @returns {any[]}
	 */
	private extractDependencyTasks(array: any[]): any[] {
		return array
			.map((task) => this.makeTaskItem(task));
	}

	/**
	 * Extract only the task added by the user
	 * @param {any[]} tasks Current task array manipulated by the user
	 * @param {any[]} originalTasks Original task array not touched
	 * @returns {string[]}
	 */
	private getTaskAdded(tasks: any[], originalTasks: any[]): string[] {
		return tasks
			.filter(task => !originalTasks.find(original => original.id === task.id))
			.map((task, index) => (`-${index + 1}_${task.id}`));
	}

	/**
	 * Extract only the task previously present (not added by the user)
	 * @param {any[]} tasks Current task array manipulated by the user
	 * @param {any[]} originalTasks Original task array not touched
	 * @returns {string[]}
	 */
	private getTaskPrevious(tasks: any[], originalTasks: any[]): string[] {
		return tasks
			.filter(task => originalTasks.find(original => original.id === task.id))
			.map((task, index) => (`${task.id}_${task.taskNumber}`));
	}

	/**
	 * Get and clean the object that will send to the service update the task
	 */
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

	/**
	 * Returns empty string whenever value is null, undefined or false
	 * @param {any} value to validate
	 */
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

	/**
	 * Determine if array predecessor contains duplicated tasks
	 * @returns {boolean}
	 */
	hasDuplicatedPredecessors(): boolean {
		return this.hasDuplicates(this.model.predecessorList);
	}

	/**
	 * Determine if array successors contains duplicated tasks
	 * @returns {boolean}
	 */
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

	/**
	 * Extract only distinct task ids from the array provided
	 * @param {any[]} array
	 * @returns {string[]}
	 */
	private extractDistinctIds(array): string[] {
		const ids = [];

		(array || []).forEach((item) => {
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

	/**
	 * Add protocol in case is not present
	 * @param {string} labelURL
	 * @returns {string}
	 */
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

	/**
	 * Determine if predecessors and successors tasks share a common task
	 * @returns {boolean}
	 */
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

	/**
	 * Toggle the locked state of estimated dates
	 */
	toggleLocked() {
		this.model.locked = !this.model.locked;
	}

	/**
	 * Add a task to predecessor tasks array
	 * @param {number} index Index where the task will be allocated
	 * @param {any} task  Task to insert
	 * @returns {ITask}
	 */
	addPredecessor(index: number, task: any): ITask {
		return this.model.predecessorList[index] = this.makeTaskItem(task) ;
	}

	/**
	 * Add a task to successor tasks array
	 * @param {number} index Index where the task will be allocated
	 * @param {any} task  Task to insert
	 * @returns {ITask}
	 */
	addSuccessor(index: number, task: any): ITask {
		return this.model.successorList[index] = this.makeTaskItem(task);
	}

	/**
	 * Delete a task from successors task array
	 * @param {number} index Index to delete
	 * @returns {boolean}
	 */
	deleteSuccessor(index: number): boolean {
		return this.deleteTaskItem(index, this.model.successorList, this.model.deletedSuccessorList);
	}

	/**
	 * Delete a task from predecessors task array
	 * @param {number} index Index to delete
	 * @returns {boolean}
	 */
	deletePredecessor(index: number): boolean {
		return this.deleteTaskItem(index, this.model.predecessorList, this.model.deletedPredecessorList);
	}

	/**
	 * Get the reference to the predecessor array
	 * @returns {any[]}
	 */
	getPredecessor(): any[] {
		return this.model.predecessorList;
	}

	/**
	 * Get the reference to the successor array
	 * @returns {any[]}
	 */
	getSuccessor(): any[] {
		return this.model.successorList;
	}

	/**
	 * Delete a task from predecessors or successors task array, put the delete item in the tasks deleted array
	 * @param {number} index Index to delete
	 * @param {array[]} collection Could be predecessors or succesors array
	 * @param {array[]}  deletedCollection Collection that contains the reference to deleted items
	 * @returns {boolean}
	 */
	private deleteTaskItem(index: number, collection: any[], deletedCollection: any[]): boolean {
			const id = collection[index].id;
			const exists = Boolean(id);

			if (exists) {
				deletedCollection.push(id);
			}

			collection.splice(index, 1);
			return exists;
	}

	/**
	 * Format an object and return a task object
	 * @param {any} task Object to format on
	 * @returns {ITask}
	 */
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

	/**
	 * Task description is coming with the taskNumber mixed into the description
	 * This function removes the taskNumber from the description
	 * @param {string} description Text that contains the description and taskNumber
	 * @param {string} taskNumber Task number to delete
	 * @returns {string}
	 */
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
