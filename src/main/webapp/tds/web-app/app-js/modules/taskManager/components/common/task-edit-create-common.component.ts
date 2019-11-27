import {
	Component,
	HostListener,
	OnInit,
	AfterViewInit,
	OnDestroy,
	ViewChild,
	ViewChildren,
	QueryList
} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {NgForm} from '@angular/forms';
import {clone} from 'ramda';
import {ModalType} from '../../../../shared/model/constants';
import {UIDialogService, UIExtraDialog} from '../../../../shared/services/ui-dialog.service';
import {TaskDetailModel} from '../../model/task-detail.model';
import {TaskService} from '../../service/task.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
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
import {SortUtils} from '../../../../shared/utils/sort.utils';
import {StringUtils} from '../../../../shared/utils/string.utils';

declare var jQuery: any;

export class TaskEditCreateCommonComponent extends UIExtraDialog implements OnInit, AfterViewInit, OnDestroy {
	@ViewChild('taskEditCreateForm', {static: false}) public taskEditCreateForm: NgForm;
	@ViewChildren(DropDownListComponent) dropdowns: QueryList<DropDownListComponent>;
	@ViewChild('dueDate', {static: false}) dueDate;

	protected modalType = ModalType;
	protected dateFormat: string;
	protected dateFormatTime: string;
	public dataGridTaskPredecessorsHelper: DataGridOperationsHelper;
	public dataGridTaskSuccessorsHelper: DataGridOperationsHelper;
	protected taskSuccessorPredecessorColumnsModel = new TaskSuccessorPredecessorColumnsModel();
	public collapsedTaskDetail = false;
	protected hasCookbookPermission = false;
	public modalOptions: DecoratorOptions;
	public model: any = null;
	protected getAssetList: Function;
	protected yesNoList = [...YesNoList];
	protected predecessorSuccessorColumns: any[];
	protected userTimeZone: string;
	protected hasModelChanges = false;
	public hasDeleteTaskPermission = false;
	public hasEditTaskPermission = false;
	public modelHelper: TaskEditCreateModelHelper;
	protected taskNotesColumnsModel = new TaskNotesColumnsModel();
	public dataGridTaskNotesHelper: DataGridOperationsHelper;
	protected isEventLocked: boolean;
	public SHARED_TASK_SETTINGS = SHARED_TASK_SETTINGS;
	protected metaParam: any;
	private destroySubject: Subject<any> = new Subject<any>();
	public taskViewType: string;

	constructor(
		private taskDetailModel: TaskDetailModel,
		private taskManagerService: TaskService,
		private dialogService: UIDialogService,
		private promptService: UIPromptService,
		private userPreferenceService: PreferenceService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe) {

		super('#task-component');
		this.taskViewType = this.taskDetailModel.modal.type === ModalType.CREATE ? 'task-create-view' : 'task-edit-view';
		this.modalOptions = {isResizable: false, isCentered: true, isDraggable: false};
		this.getTasksForComboBox = this.getTasksForComboBox.bind(this);
	}

	ngOnInit() {
		console.log('----on init----');
	}

	// set the handlers on open / on close to set the flags that indicate the state of the
	// dropdown list items (opened/closed)
	ngAfterViewInit() {
		this.userPreferenceService.getPreferences(PREFERENCES_LIST.CURR_TZ, PREFERENCES_LIST.CURRENT_DATE_FORMAT).subscribe(() => {
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
				this.modelHelper.getModelForEdit(this.taskDetailModel, this.userTimeZone);

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
				let assetId = this.model.asset && this.model.asset.id || null;
				commonCalls.push(this.taskManagerService.getAssetClasses());
				commonCalls.push(this.taskManagerService.getAssetCommentCategories());
				commonCalls.push(this.taskManagerService.getEvents());
				commonCalls.push(this.taskManagerService.getLastCreatedTaskSessionParams());
				commonCalls.push(this.taskManagerService.getClassForAsset(assetId));
			}

			this.metaParam = this.getMetaParam();
			Observable.forkJoin(commonCalls)
				.subscribe((results: any[]) => {
					const [status, persons, staffRoles, dateFormat, actions, assetClasses, categories, events, taskDefaults, classForAsset] = results;
					let personList = persons || [];

					this.model.statusList = status;
					personList = personList || [];
					// Add Unassigned and Auto options to staff list
					personList.push({id: 'AUTO', nameRole: 'Automatic', sortOn: 'Automatic'});
					this.model.personList = personList
						.sort((a, b) => SortUtils.compareByProperty(a, b, 'sortOn'))
						.map((item) => ({id: item.id, text: item.nameRole}));
					this.model.personList.unshift({id: 0, text: 'Unassigned'});
					// Add Unassigned and Auto options to roles list
					staffRoles.push({id: 'AUTO', description: 'Automatic'});
					this.model.teamList = staffRoles
						.sort((a, b) => SortUtils.compareByProperty(a, b, 'description'))
						.map((item) => ({id: item.id, text: item.description}));
					this.model.teamList.unshift({id: null, text: 'Unassigned'});
					// set defaults
					if (this.taskDetailModel.modal.type === ModalType.CREATE) {
						this.model.assignedTo = this.model.personList[0];
						this.model.assignedTeam = this.model.teamList[0];
					}
					this.dateFormat = dateFormat;
					this.model.apiActionList = actions.map((item) => ({id: item.id, text: item.name}));
					if (categories) {
						this.model.categoriesList = [''].concat(categories);
					}
					if (events) {
						this.model.eventList = events.map((item) => ({id: item.id.toString(), text: item.name}));
					}
					if (taskDefaults && taskDefaults.preferences && this.taskDetailModel.modal.type === ModalType.CREATE) {
						this.model.category = taskDefaults.preferences['TASK_CREATE_CATEGORY'] || 'general';
						this.model.status = taskDefaults.preferences['TASK_CREATE_STATUS'] || TaskStatus.READY;
						this.metaParam = this.getMetaParam();
					}
					if (assetClasses) {
						this.model.assetClasses = assetClasses.map((item) => ({id: item.key, text: item.label}));
					}
					// on CREATE mode
					if (this.taskDetailModel.modal.type === ModalType.CREATE) {
						this.model.assetClass = (classForAsset && classForAsset.assetClass)
							? {id: classForAsset.assetClass, text: ''}
							: {id: this.model.assetClasses[0].id, text: ''};
						// on EDIT mode and empty assetClass
					} else if (!this.model.assetClass || !this.model.assetClass.id) {
						this.model.assetClass = {id: this.model.assetClasses[0].id, text: ''};
					} else {
						// for displaying the existing assetClass
						const deCapitilized = StringUtils.toCapitalCase(this.model.assetClass.id);
						this.model.assetClass = {id: deCapitilized, text: deCapitilized};
					}
					jQuery('[data-toggle="popover"]').popover();
					const percentageComplete = this.taskEditCreateForm.form.controls['percentageComplete'];
					const comment = this.taskEditCreateForm.form.controls['comment'];

					if (percentageComplete) {
						percentageComplete.markAsPristine();
					}
				});

			this.dataGridTaskPredecessorsHelper = new DataGridOperationsHelper(this.model.predecessorList, null, null);
			this.dataGridTaskSuccessorsHelper = new DataGridOperationsHelper(this.model.successorList, null, null);
			this.dataGridTaskNotesHelper = new DataGridOperationsHelper(this.modelHelper.generateNotes(this.model.notesList), null, null);
			this.hasCookbookPermission = this.permissionService.hasPermission(Permission.CookbookView) || this.permissionService.hasPermission(Permission.CookbookEdit);

			this.hasDeleteTaskPermission = this.permissionService.hasPermission(Permission.TaskDelete);
			this.hasEditTaskPermission = this.permissionService.hasPermission(Permission.TaskEdit);
		});

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
		return date ? `${this.dateFormat} ${DateUtils.DEFAULT_FORMAT_TIME}` : this.dateFormat;
	}

	getDateFormat(date = null): string {
		return date ? date : this.dateFormat;
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
				this.model.durationParts = duration;
				this.model.locked = locked;
				this.hasModelChanges = true;
			}
		}).catch(result => {
			// console.log('Dismissed Dialog');
		});
	}

	/**
	 * Save the changes on the task or create it
	 */
	public onSave(): void {
		let observable = null;

		if (this.taskDetailModel.modal.type === ModalType.EDIT) {
			observable = this.taskManagerService.updateTask(this.modelHelper.getPayloadForUpdate())
		} else {
			observable = this.taskManagerService.createTask(this.modelHelper.getPayloadForCreate())
		}

		observable.subscribe((result) => this.close(result));
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
	public cancelCloseDialog(): void {
		if (this.isFormDirty()) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
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
	public deleteTask(): void {
		this.promptService.open(
			this.translatePipe.transform('GLOBAL.CONFIRM'),
			this.translatePipe.transform('TASK_MANAGER.DELETE_TASK'),
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
			this.model.estimatedFinish = DateUtils.increment(this.model.estimatedFinish, [{value: diff, unit}]);
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
		if (this.modelHelper && !this.modelHelper.hasDependencyTasksChanges()) {
			return false;
		}
		return this.modelHelper && this.modelHelper.hasInvalidTasksDependencies();
	}

	/**
	 * Determine whether the form should be disabled because of invalid fields
	 * @returns {boolean}
	 */
	public isFormInvalid(): boolean {
		const { form = null } = this.taskEditCreateForm || {};

		return form && ((!form.valid || this.hasInvalidFields()) || !(form.dirty || this.hasModelChanges));
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

		const errors = (!isEmpty && !ValidationUtils.isValidLabelURL(labelURL)) ? {'incorrect': true} : null;
		if (this.taskEditCreateForm.form) {
			this.taskEditCreateForm.form.controls['instructionLink'].setErrors(errors);
		}
	}

	/**
	 * On change selection on asset class reset asset entity value
	 * @returns {void}
	 */
	protected onAssetClassChange(event): void {
		this.model.asset = null;
	}

	/**
	 * Get the object required to query tasks, using the current commentId and eventId
	 * @eventId Event id number if it is selected
	 * @returns {any}
	 */
	protected getMetaParam(eventId = ''): any {
		const commentId = this.taskDetailModel.modal.type === ModalType.CREATE ? '' : this.model.id;
		return {commentId, eventId: eventId || this.model.event.id};
	}

	/**
	 * Whenever a event is selected update the metaParam object
	 * @returns {any}
	 */
	protected onSelectedEvent(event): void {
		this.metaParam = this.getMetaParam(event.id);
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
	public createNote() {
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

	onOpenDueDate(event) {
		event.preventDefault();
		this.dueDate.toggle();
	}

}
