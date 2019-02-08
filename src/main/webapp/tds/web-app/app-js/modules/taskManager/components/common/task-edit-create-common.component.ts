import {Component, HostListener, OnInit, AfterViewInit, OnDestroy, ViewChild, ViewChildren, QueryList} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {NgForm} from '@angular/forms';
import {clone} from 'ramda';
import {ModalType} from '../../../../shared/model/constants';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from '../../model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils, DatePartUnit} from '../../../../shared/utils/date.utils';
import {DataGridOperationsHelper} from '../../../../shared/utils/data-grid-operations.helper';
import {TaskSuccessorPredecessorColumnsModel} from '../../model/task-successor-predecessor-columns.model';
import {Permission} from '../../../../shared/model/permission.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {DecoratorOptions} from '../../../../shared/model/ui-modal-decorator.model';
import {ComboBoxSearchModel} from '../../../../shared/components/combo-box/model/combobox-search-param.model';
import {DateRangeSelectorComponent} from '../../../../shared/components/date-range-selector/date-range-selector.component';
import {DateRangeSelectorModel} from '../../../../shared/components/date-range-selector/model/date-range-selector.model';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {TaskNotesColumnsModel} from '../../../../shared/components/task-notes/model/task-notes-columns.model';
import {TaskEditCreateModelHelper} from './task-edit-create-model.helper';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {YesNoList, TaskStatus} from '../../model/task-edit-create.model';
import {SHARED_TASK_SETTINGS} from '../../model/shared-task-settings';
import {UIHandleEscapeDirective as EscapeHandler} from '../../../../shared/directives/handle-escape-directive';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {takeUntil} from 'rxjs/operators';
import {SingleCommentComponent} from '../../../assetExplorer/components/single-comment/single-comment.component';
import {SingleCommentModel} from '../../../assetExplorer/components/single-comment/model/single-comment.model';
import {SingleNoteModel} from '../../../assetExplorer/components/single-note/model/single-note.model';
import {SingleNoteComponent} from '../../../assetExplorer/components/single-note/single-note.component';
declare var jQuery: any;

export class TaskEditCreateCommonComponent extends UIExtraDialog  implements OnInit, AfterViewInit, OnDestroy {
	@ViewChild('taskEditCreateForm') public taskEditCreateForm: NgForm;
	@ViewChildren(DropDownListComponent) dropdowns: QueryList<DropDownListComponent>;

	protected modalType = ModalType;
	protected dateFormat: string;
	protected dateFormatTime: string;
	protected dataGridTaskPredecessorsHelper: DataGridOperationsHelper;
	protected dataGridTaskSuccessorsHelper: DataGridOperationsHelper;
	protected taskSuccessorPredecessorColumnsModel = new TaskSuccessorPredecessorColumnsModel();
	protected collapsedTaskDetail = false;
	protected hasCookbookPermission = false;
	protected modalOptions: DecoratorOptions;
	protected model: any = {};
	protected getAssetList: Function;
	protected yesNoList =  [...YesNoList];
	protected predecessorSuccessorColumns: any[];
	protected userTimeZone: string;
	protected hasModelChanges = false;
	protected hasDeleteTaskPermission = false;
	protected hasEditTaskPermission = false;
	protected modelHelper: TaskEditCreateModelHelper;
	protected taskNotesColumnsModel = new TaskNotesColumnsModel();
	protected dataGridTaskNotesHelper: DataGridOperationsHelper;
	protected isEventLocked: boolean;
	protected SHARED_TASK_SETTINGS = SHARED_TASK_SETTINGS;
	protected metaParam: any;
	private destroySubject: Subject<any> = new Subject<any>();
	constructor(
		private taskDetailModel: TaskDetailModel,
		private taskManagerService: TaskService,
		private dialogService: UIDialogService,
		private promptService: UIPromptService,
		private userPreferenceService: PreferenceService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe) {

		super('#task-component');
		this.modalOptions = { isResizable: true, isCentered: true };
		this.getTasksForComboBox = this.getTasksForComboBox.bind(this);
	}

	ngOnInit() {
		this.userTimeZone = this.userPreferenceService.getUserTimeZone();
		this.dateFormat = this.userPreferenceService.getDefaultDateFormatAsKendoFormat();
		this.dateFormatTime = this.userPreferenceService.getUserDateTimeFormat();
		this.isEventLocked = false;

		this.modelHelper = new TaskEditCreateModelHelper(
			this.userTimeZone,
			this.userPreferenceService.getUserCurrentDateFormatOrDefault(),
			this.taskManagerService,
			this.dialogService,
			this.translatePipe);

		this.model = this.taskDetailModel.modal.type === ModalType.CREATE ?
			this.modelHelper.getModelForCreate(this.taskDetailModel)
			:
			this.modelHelper.getModelForEdit(this.taskDetailModel);

		this.getAssetList = this.taskManagerService.getAssetListForComboBox.bind(this.taskManagerService);
		this.predecessorSuccessorColumns = this.taskSuccessorPredecessorColumnsModel.columns;
		this.isEventLocked = this.model.predecessorList.length > 0 || this.model.successorList.length > 0;

		const commonCalls = [
			this.taskManagerService.getStatusList(this.model.id),
			this.taskManagerService.getAssignedTeam(this.model.id),
			this.taskManagerService.getStaffRoles(),
			this.userPreferenceService.getUserDatePreferenceAsKendoFormat(),
			this.taskManagerService.getActionList()
		];

		if (this.taskDetailModel.modal.type === ModalType.CREATE) {
			commonCalls.push(this.taskManagerService.getAssetClasses());
			commonCalls.push(this.taskManagerService.getCategories());
			commonCalls.push(this.taskManagerService.getEvents());
			commonCalls.push(this.taskManagerService.getLastCreatedTaskSessionParams());
			commonCalls.push(this.taskManagerService.getClassForAsset(this.model.id));
		}

		this.metaParam = this.getMetaParam();
		Observable.forkJoin(commonCalls)
			.subscribe((results: any[]) => {
				const [status, personList, staffRoles, dateFormat, actions, assetClasses, categories, events, taskDefaults, classForAsset] = results;

				this.model.statusList = status;
				this.model.personList = personList.map((item) => ({id: item.id, text: item.nameRole}));
				this.model.teamList = staffRoles.map((item) => ({id: item.id, text: item.description }));
				this.dateFormat = dateFormat;
				this.model.apiActionList = actions.map((item) => ({id: item.id, text: item.name}));

				if (assetClasses) {
					this.model.assetClasses = assetClasses.map((item) => ({id: item.key, text: item.label}));
				}
				if (categories) {
					this.model.categoriesList = [''].concat(categories);
				}

				if (events) {
					this.model.eventList = events.map((item) => ({id: item.id.toString(), text: item.name}));
				}

				if (taskDefaults && taskDefaults.preferences) {
					this.model.event = {id: taskDefaults.preferences['TASK_CREATE_EVENT']  || '', text: '' };
					this.model.category = taskDefaults.preferences['TASK_CREATE_CATEGORY'] || 'general';
					this.model.status = taskDefaults.preferences['TASK_CREATE_STATUS'] || TaskStatus.READY;
					this.metaParam = this.getMetaParam();
				}

				if (classForAsset) {
					this.model.assetClass = {id: classForAsset.assetClass, text: ''};
				}

				jQuery('[data-toggle="popover"]').popover();
			});

		this.dataGridTaskPredecessorsHelper = new DataGridOperationsHelper(this.model.predecessorList, null, null);
		this.dataGridTaskSuccessorsHelper = new DataGridOperationsHelper(this.model.successorList, null, null);
		this.dataGridTaskNotesHelper = new DataGridOperationsHelper(this.modelHelper.generateNotes(this.model.notesList), null, null);
		this.hasCookbookPermission = this.permissionService.hasPermission(Permission.CookbookView) || this.permissionService.hasPermission(Permission.CookbookEdit);

		this.hasDeleteTaskPermission = this.permissionService.hasPermission(Permission.TaskDelete);
		this.hasEditTaskPermission = this.permissionService.hasPermission(Permission.TaskEdit);
	}

	// set the handlers on open / on close to set the flags that indicate the state of the
	// dropdown list items (opened/closed)
	ngAfterViewInit() {
		this.dropdowns.toArray()
			.forEach((dropdown) => {
				dropdown.open
					.pipe(takeUntil(this.destroySubject))
					.subscribe(() => EscapeHandler.setIsDropdownListOpen(dropdown.wrapper.nativeElement, true));

				dropdown.close
					.pipe(takeUntil(this.destroySubject))
					.subscribe(() => setTimeout(() => EscapeHandler.setIsDropdownListOpen(dropdown.wrapper.nativeElement, false), 200));
			});
	}

	ngOnDestroy() {
		this.destroySubject.next();
	}

	/**
	 * Pass Service as Reference
	 * @param {ComboBoxSearchModel} searchParam
	 * @returns {Observable<any>}
	 */
	public getAssetListForComboBox(searchParam: ComboBoxSearchModel): Observable<any> {
		return this.taskManagerService.getAssetListForComboBox(searchParam);
	}

	/**
	 * Everytime a task dependency changes, then update the corresponding collection that hold predeccessor/successor  tasks
	 * If the task is deleted set the model changes flag to true
	 * @param {object} dataItem
	 * @param {string} collectionType  could be predecessor/ successor string
	 * @param {object} gridHelper
	 * @returns {void}
	 */
	onDependencyTaskChange(dataItem: any, collectionType: string, gridHelper: DataGridOperationsHelper, rowIndex: number): void {
		let addTask, getCollection;

		if (collectionType === 'predecessor') {
			addTask = this.modelHelper.addPredecessor;
			getCollection = this.modelHelper.getPredecessor;
		} else {
			addTask = this.modelHelper.addSuccessor;
			getCollection = this.modelHelper.getSuccessor;
		}

		if (dataItem) {
			const {id, text} = dataItem;
			addTask.bind(this.modelHelper)(rowIndex, {id, text, ...dataItem.metaFields})
		}

		gridHelper.reloadData(getCollection.bind(this.modelHelper)());
	}

	/**
	 * Adda a task predecessor/successor to the top of the corresponding collection sent as parameter
	 * @param {object[]} collection
	 * @param {object} gridHelper
	 * @returns {void}
	 */
	onAddTaskDependency(collection: any[], gridHelper: DataGridOperationsHelper): void {
		const defaultText = '';
		const task = {
			id: '',
			desc: defaultText,
			model: {id: '', text: defaultText}
		};
		collection.unshift(task);
		gridHelper.addDataItem(task);
	}

	/**
	 * Pass Service as Reference
	 * @param {ComboBoxSearchModel} searchParam
	 * @returns {Observable<any>}
	 */
	public getTasksForComboBox(searchParam: ComboBoxSearchModel): Observable<any> {
		return this.taskManagerService.getTasksForComboBox(searchParam);
	}

	/**
	 * Toggle lock property value
	 */
	toggleLocked(): void {
		this.modelHelper.toggleLocked();
		this.hasModelChanges = true;
	}

	/**
	 * Set the flag to determine if some property handled outside [(ngModel)] has changed
	 * On undefined value delete the record
	 */
	onModelChange(value: any, collectionName: string, index: number): void {
		if (value) {
			value.text = this.modelHelper.removeTaskNumberFromDescription(value.text, value.metaFields.taskNumber);
			this.hasModelChanges = true;
			return;
		}

		// if value is undefined Clear X was clicked
		if (value === undefined) {
			if (collectionName === 'predecessor') {
				this.modelHelper.deletePredecessor(index);
				this.dataGridTaskPredecessorsHelper.reloadData(this.model.predecessorList);
				this.hasModelChanges = true;
			}

			if (collectionName === 'successor') {
				this.modelHelper.deleteSuccessor(index);
				this.dataGridTaskSuccessorsHelper.reloadData(this.model.successorList);
				this.hasModelChanges = true;
			}
			this.hasModelChanges = true;
		}
	}

	/**
	 * Set the reference to the asset entity once combobox changed
	 */
	onAssetEntityChange(assetSelected: any): void {
		this.model.asset = assetSelected;
		this.hasModelChanges = true;
	}

	/**
	 * Return the date passed as argument formatted using the user timezone
	 * @param {any} date
	 * @returns {string}
	 */
	getFormattedDate(date): any {
		return new Date(DateUtils.formatUserDateTime(this.userTimeZone, date));
	}

	/**
	 * Passing a valid date returns the user preference date format plus the default time format
	 * Otherwise returns empty string
	 * @param {any} date
	 * @returns {string}
	 */
	getDateTimeFormat(date = null): string {
		return date ? `${this.dateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}` :  this.dateFormat;
	}

	/**
	 * Open the view to allow select a range of estimated dates
	 */
	openRangeDatesSelector(): void {
		const dateModel: DateRangeSelectorModel = {
			start: this.model.estimatedStart,
			end: this.model.estimatedFinish,
			locked: this.model.locked,
			dateFormat: this.dateFormat,
			timeFormat: DateUtils.DEFAULT_FORMAT_TIME,
			duration: this.model.durationParts
		};

		this.dialogService.extra(DateRangeSelectorComponent,
			[
				{provide: UIPromptService, useValue: this.promptService},
				{provide: DateRangeSelectorModel, useValue: clone(dateModel)}
			], false, false).then((result: DateRangeSelectorModel) => {
			if (result) {
				const {start, end, duration, locked} = result;
				this.model.estimatedStart = start;
				this.model.estimatedFinish = end;
				this.model.durationParts =  duration;
				this.model.locked = locked;
				this.hasModelChanges = true;
			}
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Save the changes on the task
	 */
	protected onSave(): void {
		this.taskManagerService.updateTask(this.modelHelper.getPayloadForUpdate())
			.subscribe((result) => this.close(result));
	}

	/**
	 * Create the task
	*/
	protected onCreate(): void {
		this.taskManagerService.createTask(this.modelHelper.getPayloadForCreate())
			.subscribe((result) => this.close(result));
	}

	/**
	 * Toggle task details
	 */
	public onCollapseTaskDetail(): void {
		this.collapsedTaskDetail = !this.collapsedTaskDetail;
	}

	/**
	 * Close Dialog
	 */
	protected cancelCloseDialog(): void {
		if (this.isFormDirty()) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED')	,
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')	,
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'))
				.then(result => {
					if (result) {
						this.dismiss();
					}
				});
		} else {
			this.dismiss();
		}
	}

	/**
	 * Prompt confirm delete a task
	 * delegate operation to host component
	 */
	protected deleteTask(): void {
		this.promptService.open(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED')	,
			this.translatePipe.transform('TASK_MANAGER.DELETE_TASK')	,
			this.translatePipe.transform('GLOBAL.CONFIRM'),
			this.translatePipe.transform('GLOBAL.CANCEL'))
			.then(result => {
				if (result) {
					this.close({id: this.taskDetailModel, isDeleted: true})
				}
			});
	}

	/**
	 * Based upon the date unit, increment/decrement the value of estimated finish date
	 * @param {DatePartUnit} unit
	 * @returns {void}
	 */
	protected updateEstimatedFinish(value: string, unit: DatePartUnit): void {
		const originalParts = DateUtils.getDurationPartsAmongDates(this.model.estimatedStart, this.model.estimatedFinish);

		if (value === '') {
			this.model.durationParts[unit] = 0;
		}

		const diff = this.model.durationParts[unit] - originalParts[unit];

		if (this.model.estimatedFinish) {
			this.model.estimatedFinish =  DateUtils.increment(this.model.estimatedFinish, [{value: diff, unit}]);
		}
	}

	/**
	 * Clear the values of start/finish estimated dates
	 */
	protected cleanEstimatedDates(): void {
		if (this.model.estimatedStart || this.model.estimatedFinish) {
			this.model.estimatedFinish = '';
			this.model.estimatedStart = '';
			this.hasModelChanges = true;
		}
	}

	/**
	 * Determine if predecessor/successor collections contains invalid data, like duplicates or empty ids
	 * @returns {boolean}
	 */
	protected hasInvalidFields(): boolean {
		if (!this.modelHelper.hasDependencyTasksChanges()) {
			return false;
		}
		return this.modelHelper.hasInvalidTasksDependencies();
	}

	/**
	 * Determine whether the form should be disabled because of invalid fields
	 * @returns {boolean}
	 */
	protected isFormInvalid(): boolean {
		return !this.taskEditCreateForm.form.valid ||
			this.hasInvalidFields() ||
			!(this.taskEditCreateForm.form.dirty || this.hasModelChanges)
	}

	/**
	 * Determine if the form has changes based upon ngModel,external tds-checkbox or task grids changes
	 * @returns {boolean}
	 */
	protected isFormDirty(): boolean {
		return this.modelHelper.hasDependencyTasksChanges() || this.taskEditCreateForm.dirty || this.hasModelChanges;
	}

	/**
	 *  Mark labelURL field as invalid if it doesn't fit the format
	 * @param labelURL {string}
	 * @returns {boolean}
	 */
	protected validateLabelURL(labelURL: string): void {
		let isEmpty = false;

		if (!labelURL) {
			isEmpty = true
		}

		const errors =  (!isEmpty && !ValidationUtils.isValidLabelURL(labelURL)) ? {'incorrect' : true}  : null;
		this.taskEditCreateForm.form.controls['instructionLink'].setErrors(errors);
	}

	/**
	 * On change selection on asset class reset asset entity value
	 * @returns {void}
	 */
	protected onAssetClassChange(): void {
		this.model.asset = null;
	}

	/**
	 * Get the object required to query tasks, using the current commentId and eventId
	 * @eventId Event id number if it is selected
	 * @returns {any}
	*/
	protected getMetaParam(eventId = ''): any {
		const commentId = this.taskDetailModel.modal.type === ModalType.CREATE ? '' : this.model.id;
		return {commentId, eventId: eventId || this.model.event.id} ;
	}

	/**
	 * Whenever a event is selected update the metaParam object
	 * @returns {any}
	 */
	protected onSelectedEvent(event): void {
		this.metaParam = this.getMetaParam(event.id) ;
	}

	/**
	 * Generate the item template for the task items to show in the format taskId : Description
	 * @event dataItem contains the task item information
	 * @returns {string}
	 */
	protected innerTemplateTaskItem(dataItem: any): string {
		if (dataItem.metaFields) {
			if (!dataItem.text.startsWith(dataItem.metaFields.taskNumber)) {
				dataItem.text = `${dataItem.metaFields.taskNumber}: ${dataItem.text}`;
			}
		}
		return dataItem.text;
	}

	/**
	 * Create a note and update the datagrid task notes component
	 */
	protected createNote() {
		this.modelHelper.onCreateNote()
			.subscribe((result) => {
				if (result) {
					this.model.notesList = result && result.data || [];
					this.dataGridTaskNotesHelper =
						new DataGridOperationsHelper(
							this.modelHelper.generateNotes(this.model.notesList), null, null);
				}
			});
	}

}