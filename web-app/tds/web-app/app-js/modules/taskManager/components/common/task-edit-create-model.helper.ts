import {Observable} from 'rxjs';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {clone} from 'ramda';
import {TaskDetailModel} from '../../model/task-detail.model';
import {YesNoList, PriorityList, ITask, TaskStatus} from '../../model/task-edit-create.model';
import {SingleNoteComponent} from '../../../assetExplorer/components/single-note/single-note.component';
import {ModalType} from '../../../../shared/model/constants';
import {SingleNoteModel} from '../../../assetExplorer/components/single-note/model/single-note.model';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

export class TaskEditCreateModelHelper {
	model: any;
	private userTimeZone: string;
	private userCurrentDateFormat: string;
	private userCurrentDateTimeFormat: string;
	private dataSignatureDependencyTasks: string;
	public STATUS = TaskStatus;
	CREATE_PREDECESSOR = '';
	CREATE_SUCCESSOR = '';

	constructor(
		userTimeZone: string,
		userCurrentDateFormat: string,
		private taskManagerService: TaskService,
		private dialogService: UIDialogService,
		private translate: TranslatePipe) {
		this.model = {};

		this.userTimeZone = userTimeZone;
		this.userCurrentDateFormat = userCurrentDateFormat;
		this.userCurrentDateTimeFormat =  `${userCurrentDateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}`;
		this.CREATE_PREDECESSOR = this.translate.transform('TASK_MANAGER.CREATE_PREDECESSOR');
		this.CREATE_SUCCESSOR = this.translate.transform('TASK_MANAGER.CREATE_SUCCESSOR');
	}

	/**
	 * Set the model and keep the reference to the initial data to detect changes
	 * @param {any} model Model to set
	 * @returns {any}
	 */
	public getModelForEdit(model: any): any {
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
	public getModelForCreate(detailModel: TaskDetailModel): any {
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
			sendNotification:  yes,
			durationScale: defaultDurationScale,
			durationParts: {days: 0, hours: 0, minutes: 0},
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
			assetClass: {id: detailModel.detail.assetClass, text: ''},
			assetClasses: [{id: '', text: ''}],
			status: this.STATUS.READY,
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
			priorityList: PriorityList,
			asset: {id: detailModel.detail.assetEntity, text: detailModel.detail.assetName},
			assignedTo: {id : parseInt(detailModel.detail.currentUserId, 10), text: ''},
			assignedTeam: {id: '', text: ''},
			event: {id: '', text: ''},
			category: 'general',
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
	public getModelForDetails(task: any): any {
		const detail = clone(task.detail);
		const assetComment = detail['assetComment'] || {};
		const durationScale = assetComment.durationScale && assetComment.durationScale.name || null;
		const [yes, no] = YesNoList;

		const categories =  [...(detail.categories || [])];
		categories.push(''); // add empty default value
		const  instructionLink = this.getInstructionsLink(detail);

		this.model = {
			apiActionInvokedAt: assetComment.apiActionInvokedAt,
			apiActionCompletedAt: assetComment.apiActionCompletedAt,
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
			status: assetComment && assetComment.status || '',
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
			priorityList: PriorityList,
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
			.filter(task => !Boolean(task.originalId))
			.map((task, index) => (`-${index + 1}_${task.id}`));
	}

	/**
	 * Extract only the task added by the user
	 * @param {any[]} tasks Current task array manipulated by the user
	 * @param {any[]} originalTasks Original task array not touched
	 * @returns {string[]}
	 */
	private getTaskEdited(tasks: any[], originalTasks: any[]): string[] {
		return tasks
			.filter(task => Boolean(task.originalId))
			.map((task, index) => {
				return (`${task.originalId}_${task.id === task.originalId ? task.taskId : task.id}`)
			});
	}

	/**
	 * Extract only the task previously present (not added by the user)
	 * @param {any[]} tasks Current task array manipulated by the user
	 * @param {any[]} originalTasks Original task array not touched
	 * @returns {string[]}
	 */
	private getTaskPrevious(tasks: any[], originalTasks: any[]): string[] {
		return tasks
			.filter(task => originalTasks.find(original => original.taskId === task.id))
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

		// ignore blank tasks
		const predecessorTasks = predecessorList.filter((task) => task.id);
		const successorTasks = successorList.filter((task) => task.id);

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
			taskDependency: this.getTaskAdded(predecessorTasks, originalPredecessorList)
				.concat(this.getTaskEdited(predecessorTasks, originalPredecessorList)),
			taskSuccessor: this.getTaskAdded(successorTasks, originalSuccessorList)
				.concat(this.getTaskEdited(successorTasks, originalSuccessorList)),
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
	 * Get and clean the object that will send to the service to create a not
	 */
	public getPayloadForUpdateNote(note: string): any {
		const { id, taskNumber } = this.model;

		return  {
			commentId: this.getEmptyStringIfNull(id).toString(),
			commentType: 'issue',
			taskNumber: taskNumber.toString(),
			note: note,
			id: id
		};
	}

	/**
	 * Get and clean the object that will send to the service update the task
	 */
	public getPayloadForCreate(): any {
		const [Yes, No] = YesNoList;
		const {
			id, assetClass, predecessorList, successorList, originalPredecessorList, originalSuccessorList,
			asset, dueDate, durationParts, estimatedFinish, estimatedStart, taskNumber, note, locked,
			hardAssigned, sendNotification, instructionLink, event, category, apiAction, comment,
			priority, assignedTeam, status, assignedTo, durationScale} = this.model;

		// ignore blank tasks
		const predecessorTasks = predecessorList.filter((task) => task.id);
		const successorTasks = successorList.filter((task) => task.id);

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
			taskDependency: this.getTaskAdded(predecessorTasks, originalPredecessorList)
				.concat(this.getTaskPrevious(predecessorTasks, originalPredecessorList)),
			taskSuccessor: this.getTaskAdded(successorTasks, originalSuccessorList)
				.concat(this.getTaskPrevious(successorTasks, originalSuccessorList)),
			deletedPreds: deletedItems,
			workflowTransition: '',
			canEdit: true, /* ? */
			durationLocked: locked ? '1' : '0',
			id: ''
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
	 * Ignore blank tasks
	 * @returns {boolean}
	 */
	hasDependencyTasksChanges(): boolean {
		const predecessors = this.model.predecessorList.filter((task) => task.id);
		const successors = this.model.successorList.filter((task) => task.id);

		return this.dataSignatureDependencyTasks !== JSON.stringify({predecessors, successors});
	}

	/**
	 * Determine if array predecessor contains duplicated tasks
	 * It ignores blanks
	 * @returns {boolean}
	 */
	hasDuplicatedPredecessors(): boolean {
		const predecessorList = this.model.predecessorList.filter((item) => item.id);
		return this.hasDuplicates(predecessorList);
	}

	/**
	 * Determine if array successors contains duplicated tasks
	 * It ignores blanks
	 * @returns {boolean}
	 */
	hasDuplicatedSuccessors(): boolean {
		const successorList = this.model.successorList.filter((item) => item.id);
		return this.hasDuplicates(successorList);
	}

	/**
	 * Determine if the array of objects passed as argument has duplicated id properties
	 * @param {any[]} array with values
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
	 * Determine if predecessor/successor collections contains invalid data, like duplicates
	 * Ignore blank tasks
	 * @returns {boolean}
	 */
	hasInvalidTasksDependencies(): boolean {
		const predecessorList = this.model.predecessorList.filter((task) => task.id);
		const successorList = this.model.successorList.filter((task) => task.id);

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
		let originalId = null;
		if (this.model.predecessorList[index]) {
			originalId = this.model.predecessorList[index].originalId;
		}

		task = this.makeTaskItem(task);
		task.originalId = originalId;

		return this.model.predecessorList[index] = task;

	}

	/**
	 * Add a task to successor tasks array
	 * @param {number} index Index where the task will be allocated
	 * @param {any} task  Task to insert
	 * @returns {ITask}
	 */
	addSuccessor(index: number, task: any): ITask {
		let originalId = null;
		if (this.model.successorList[index]) {
			originalId = this.model.successorList[index].originalId;
		}

		task = this.makeTaskItem(task);
		task.originalId = originalId;

		return this.model.successorList[index] = task;
	}

	/**
	 * Delete a task from successors task array
	 * @param {number} index Index to delete
	 * @returns {boolean}
	 */
	deleteSuccessor(index: number): boolean {
		const deleted =  this.deleteTaskItem(index, this.model.successorList, this.model.deletedSuccessorList);
		this.model.successorList = this.model.successorList.slice();
		return deleted;

	}

	/**
	 * Delete a task from predecessors task array
	 * @param {number} index Index to delete
	 * @returns {boolean}
	 */
	deletePredecessor(index: number): boolean {
		const deleted = this.deleteTaskItem(index, this.model.predecessorList, this.model.deletedPredecessorList);
		this.model.predecessorList = this.model.predecessorList.slice();

		return deleted;
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
			originalId: id,
			taskId,
			category,
			status,
			taskNumber,
			desc: description,
			model: {
				id: taskId || id,
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
	public removeTaskNumberFromDescription(description: string, taskNumber: string): string {
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

	/**
	 * Add a note
	 */
	protected createNote(id: string, note: string): Observable<any> {
		return this.taskManagerService.addNote(id, note);
	}

	/**
	 * Open the note modal dialog to capture the note, call the method for save it
	 */
	onCreateNote(): Observable<any> {
		return Observable.create((observer) => {
			let singleNoteModel: SingleNoteModel = {
				modal: {
					title: 'Create Note',
					type: ModalType.CREATE
				},
				note: ''
			};

			this.dialogService.extra(SingleNoteComponent, [
				{provide: SingleNoteModel, useValue: singleNoteModel}
			], false, false)
				.then(addedNote => {
					this.createNote(this.model.id, addedNote)
						.subscribe((result) => {
							console.log('The result is:', result);
							observer.next(result);
						});
				}).catch(result => {
				console.log(result);
				observer.next(null);
			});
		});
	}
}
