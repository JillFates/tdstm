import {
	Component,
	ElementRef,
	EventEmitter,
	Input,
	OnInit,
	Output,
	ViewChild
} from '@angular/core';
import {ImportBatchService} from '../../service/import-batch.service';
import {BATCH_RECORD_OPERATION, ImportBatchRecordModel} from '../../model/import-batch-record.model';
import {ApiResponseModel} from '../../../../shared/model/ApiResponseModel';
import {BatchStatus, ImportBatchModel} from '../../model/import-batch.model';
import {DataResult, GroupDescriptor, process, State} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';
import {ValidationUtils} from '../../../../shared/utils/validation.utils';
import {CHECK_ACTION, OperationStatusModel} from '../../../../shared/components/check-action/model/check-action.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

/**
 * This is the various options that the Current Value column will have for display
 */
export enum CurrentValueAction {
	ShowValue = 'ShowValue',
	EditValue = 'EditValue',
	ShowInit = 'ShowInit',
	EditInit = 'EditInit'
}

export enum FieldInfoType {
	CREATE,
	UPDATE,
	FIND
}

@Component({
	selector: 'import-batch-record-fields',
	templateUrl: '../tds/web-app/app-js/modules/importBatch/components/record/import-batch-record-fields.component.html'
})
export class ImportBatchRecordFieldsComponent implements OnInit {

	@ViewChild('focusElement') popupEscFocusElement: ElementRef;
	@Input('importBatch') importBatch: ImportBatchModel;
	@Input('batchRecord') batchRecord: ImportBatchRecordModel;
	@Output('onClose') closeEvent = new EventEmitter<any>();
	@Output('onUpdateSuccess') updateSuccessEvent = new EventEmitter<any>();
	@Output('onBatchRecordUpdated') onBatchRecordUpdated = new EventEmitter<any>();

	private fieldsInfo: Array<{
		// Name of the field
		name: string,
		// Contains the value to post or was posted that the user can modify before posting
		currentValue: string,
		// Contains the initValue that if populated is used for posting. The user can modify this before posting
		initValue: string,
		// ???
		importValue: string,
		// Contains the Current value (from the domain property before posting) or the Previous value (from Record JSON)
		currentPreviousValue: string,
		// Indicates what action to take displaying input or value of the Current Value column
		currentValueAction: CurrentValueAction,
		// True if the field value was modified from the previous value
		modified: boolean,
		errors: Array<string>,
		errorsAsString: string,
		// Fields Popup Information (Create, Update, Info)
		create: any, update: any, find: any
	}>;

	private state: State = {
		filter: {
			filters: [],
			logic: 'and'
		}
	};

	protected gridData: GridDataResult;
	protected fieldsFilter: any = {
		options: [
			{text: 'All', value: 1},
			{text: 'With Errors', value: 2}
			],
		selected: {text: 'All', value: 1},
		nameFilter: {
			field: 'name',
			value: '',
			operator: 'contains',
			ignoreCase: true
		},
		errorsFilter: {
			field: 'errorsAsString',
			value: '',
			operator: 'isnotempty',
			ignoreCase: true
		}
	};

	private originalImportValues: string;
	protected saveStatus: OperationStatusModel = new OperationStatusModel();
	protected processStatus: OperationStatusModel = new OperationStatusModel();
	public MESSAGE_FIELD_WILL_BE_INITIALIZED: string;
	protected popup: any = {
		title: '',
		show: false,
		offset: {},
		margin: {horizontal: 2, vertical: 2},
		position: 'fixed',
		type: null
	};
	protected popupGridData: DataResult;
	protected popupGridGroups: Array<GroupDescriptor> = [{field: 'domainIndex'}];

	// Contains the Current/Previous value column label based on the state of the record
	protected currentPreviousColumnLabel = '';

	// Create vars of Enums to be used in the html
	protected BATCH_RECORD_OPERATION = BATCH_RECORD_OPERATION;
	protected CurrentValueAction = CurrentValueAction;
	protected FieldInfoType = FieldInfoType;
	protected BatchStatus = BatchStatus;

	constructor(private importBatchService: ImportBatchService, private translatePipe: TranslatePipe) {
		this.state.filter.filters.push(this.fieldsFilter.nameFilter);
		this.processStatus.state = CHECK_ACTION.NONE;
		this.saveStatus.state = CHECK_ACTION.NONE;
	}

	/**
	 * On Init Load Record Field details.
	 */
	ngOnInit(): void {
		this.MESSAGE_FIELD_WILL_BE_INITIALIZED =  this.translatePipe.transform('DATA_INGESTION.DATASCRIPT.DESIGNER.FIELD_WILL_BE_INITIALIZED');
		this.loadRecordFieldDetails();
		this.currentPreviousColumnLabel = (this.batchRecord.status.code === BatchStatus.COMPLETED ? 'Previous Value' : 'Current Value');
	}

	/**
	 * Gets the Batch Record Field Details from API.
	 */
	private loadRecordFieldDetails(updateProcessStatus = false): void {
		this.importBatchService.getImportBatchRecordFieldDetail(this.batchRecord.importBatch.id, this.batchRecord.id)
			.subscribe( (result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {

					// TODO : Fix the parent batchRecord reassignment and this code should be able to be removed
					// The parent gets updated with the following emit but the this.batchRecord never gets updated
					if (result.data['existingRecord']) {
						this.batchRecord.existingRecord = result.data.existingRecord;
					}

					this.onBatchRecordUpdated.emit({batchRecord: result.data});
					this.buildGridData(result.data.fieldsInfo);
					this.processStatus.state = CHECK_ACTION.NONE;
					if (updateProcessStatus) {
						let fieldsWithErrors = this.fieldsInfo.filter(item => item.errors.length > 0);
						this.processStatus.state = fieldsWithErrors.length > 0 ? CHECK_ACTION.INVALID : CHECK_ACTION.NONE;
					}
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error calling endpoint');
				}
			}, error => {
				this.fieldsInfo = [];
				this.handleError(error);
			});
	}

	/**
	 * Builds and prepares fieldInfo array to be display on the gridData.
	 * @param fields
	 */
	private buildGridData(fields): void {
		// let data: Array<{name: string, currentValue: string, importValue: string, error: boolean}> = [];
		const {fieldNameList, fieldLabelMap} = this.importBatch;

		this.fieldsInfo = [];
		for (const fieldName of fieldNameList) {

			// Determine if the fieldName exists in the current record since not all records have all of the fields
			if (fields[fieldName] === undefined) {
				continue;
			}

			// Determine the currentPreviousValue based on update and the batch status
			let currentPreviousValue = '';
			let modified = false;
			if (this.batchRecord.operation === BATCH_RECORD_OPERATION.UPDATE) {
				// Before POSTING the record
				if (this.batchRecord.status.code === BatchStatus.PENDING) {
					// Use current record property
					currentPreviousValue = this.batchRecord.existingRecord[fieldName];
				} else {
					// After the POSTING the record
					if ('previousValue' in fields[fieldName] && !ValidationUtils.isEmptyObject(fields[fieldName].previousValue)) {
						// Use previousValue in Record JSON
						currentPreviousValue = fields[fieldName].previousValue;
						modified = true;
					}
				}
			}

			// Determine what action should be shown for the Current Value column
			let currentValueAction: CurrentValueAction;
			if (this.batchRecord.status.code === BatchStatus.PENDING) {
				currentValueAction = ValidationUtils.isEmptyObject(fields[fieldName].init) ? CurrentValueAction.EditValue : CurrentValueAction.EditInit
			} else {
				currentValueAction = ValidationUtils.isEmptyObject(fields[fieldName].init) ? CurrentValueAction.ShowValue : CurrentValueAction.ShowInit;
			}

			// Not all rows will have all of the same fields so must check first
			if ( fields[fieldName] !== undefined ) {
				this.fieldsInfo.push({
					name: (fieldLabelMap && fieldLabelMap[fieldName]) || fieldName,
					currentValue: !ValidationUtils.isEmptyObject(fields[fieldName].originalValue)
						? fields[fieldName].originalValue : '(null)',
					currentPreviousValue: currentPreviousValue,
					currentValueAction: currentValueAction,
					importValue: !ValidationUtils.isEmptyObject(fields[fieldName].value)
						? fields[fieldName].value : '',
					initValue: !ValidationUtils.isEmptyObject(fields[fieldName].init)
						? fields[fieldName].init : '',
					modified: modified,
					errors: fields[fieldName].errors,
					errorsAsString: fields[fieldName].errors ? fields[fieldName].errors.join() : '',
					create: !ValidationUtils.isEmptyObject(fields[fieldName].create) ? fields[fieldName].create : null,
					update: !ValidationUtils.isEmptyObject(fields[fieldName].update) ? fields[fieldName].update : null,
					find: !ValidationUtils.isEmptyObject(fields[fieldName].find) ? fields[fieldName].find : null,
				});
			}
		}
		let importValues = this.fieldsInfo.map( item => item.importValue);
		this.originalImportValues = JSON.stringify(importValues);
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * Checks if input overrided values are not empty or with a text value.
	 */
	public areOverrideValuesDirty(): boolean {
		let currentImportValues = this.fieldsInfo.map( item => item.importValue);
		if (this.originalImportValues !== JSON.stringify(currentImportValues)) {
			this.saveStatus.state = CHECK_ACTION.NONE;
			return true;
		}
		return false;
	}

	/**
	 * On Ignore button click.
	 */
	private onIgnore(): void {
		const ids = [this.batchRecord.id];
		this.importBatchService.ignoreBatchRecords(this.importBatch.id, ids)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadRecordFieldDetails();
					this.updateSuccessEvent.emit();
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error on ignore batch record.');
				}
			}, error => this.handleError(error));
	}

	/**
	 * On Include button click.
	 */
	protected onInclude(): void {
		const ids = [this.batchRecord.id];
		this.importBatchService.includeBatchRecords(this.importBatch.id, ids)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadRecordFieldDetails();
					this.updateSuccessEvent.emit();
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error on include batch record.');
				}
			}, error => this.handleError(error));
	}

	/**
	 * On Update button click.
	 */
	protected onUpdate(): void {
		let newFieldsValues: Array<{fieldName: string, value: string}> = [];
		for (let field of this.fieldsInfo) {
			if (field.importValue) {
				const newFieldValue = {fieldName: field.name, value: field.importValue};
				newFieldsValues.push(newFieldValue);
			}
		}
		this.importBatchService.updateBatchRecordFieldsValues(this.importBatch.id, this.batchRecord.id, newFieldsValues)
			.subscribe((result: ApiResponseModel) => {
				this.loadRecordFieldDetails();
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.updateSuccessEvent.emit();
					setTimeout(() => this.saveStatus.state = CHECK_ACTION.VALID, 200);
				} else {
					setTimeout(() => this.saveStatus.state = CHECK_ACTION.INVALID, 200);
					this.handleError(result.errors[0] ? result.errors[0] : 'error updating field values');
				}
		}, error => this.handleError(error));
	}

	/**
	 * On Process button click.
	 */
	protected onProcess(): void {
		const ids = [this.batchRecord.id];
		this.importBatchService.processBatchRecords(this.importBatch.id, ids)
			.subscribe((result: ApiResponseModel) => {
				if (result.status === ApiResponseModel.API_SUCCESS) {
					this.loadRecordFieldDetails(true);
					// TODO : This is not updating the datagrid to show the posted information correctly
					this.updateSuccessEvent.emit();
				} else {
					this.handleError(result.errors[0] ? result.errors[0] : 'error on Process batch record.');
				}
			}, error => this.handleError(error));
	}

	/**
	 * Determine if Ignore button should be showed on UI.
	 * @returns {boolean}
	 */
	protected showIgnoreButton(): boolean {
		return this.batchRecord.status.code === BatchStatus.PENDING;
	}

	/**
	 * Determine if Include button should be showed on UI.
	 * @returns {boolean}
	 */
	protected showIncludeButton(): boolean {
		return this.batchRecord.status.code === BatchStatus.IGNORED;
	}

	/**
	 * Hide Action buttons if Record is already completed.
	 * @returns {boolean}
	 */
	protected showActionButtons(): boolean {
		return this.batchRecord.status.code === BatchStatus.PENDING;
	}

	/**
	 * On Text Filter input change it's value.
	 */
	private onTextFilter(): void {
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * On Fields Filter dropdown select change.
	 * @param {{text: string; value: number}} $event
	 */
	private onFieldsFilter($event: {text: string, value: number}): void {
		if ($event.value === 2) {
			this.state.filter.filters.push(this.fieldsFilter.errorsFilter);
		} else {
			const filterIndex = this.state.filter.filters.findIndex((r: any) => r.field === 'errorsAsString');
			this.state.filter.filters.splice(filterIndex, 1);
		}
		this.gridData = process(this.fieldsInfo, this.state);
	}

	/**
	 * On Text Filter input clear icon click.
	 */
	private clearTextFilter(): void {
		this.fieldsFilter.nameFilter.value = '';
		this.onTextFilter();
	}

	/**
	 * On Cancel.
	 */
	private onCancel(): void {
		this.closeEvent.emit();
	}

	/**
	 * Handles any API error and displays it on UI.
	 * @param e
	 */
	private handleError(e): void {
		console.log(e);
	}

	/**
	 * Opens and positions the popup based on the click event.
	 * @param {MouseEvent} $event
	 */
	protected onShowPopup($event: MouseEvent, type: FieldInfoType, field: any): void {
		let typeString = this.FieldInfoType[type].toLowerCase();
		if (type === FieldInfoType.FIND) {
			this.buildPopupFieldDataForFindObject(field[typeString]);
		} else {
			this.buildPopupFieldData(field[typeString]);
			// this ugly will be removed when the BE returns the correct domain on the create/update object.
			if (field.find && field.find.query && field.find.query.length > 0) {
				this.popup.domain = field.find.query[0].domain;
			}
		}
		this.popup.type = type;
		this.popup.title = this.getPopupTitle(type);
		this.popup.offset = { left: $event.pageX, top: $event.pageY};
		this.popup.show = true;
		// focus input element to help the on escape key exit.
		setTimeout( () => {
			if (this.popupEscFocusElement) {
				this.popupEscFocusElement.nativeElement.focus();
			}
		}, 300);
	}

	/**
	 * Builds the popup grid field info data.
	 * @param field
	 */
	private buildPopupFieldData(field: any): void {
		let popupFields: Array<any> = [];
		const {fieldLabelMap} = this.importBatch;
		for (let fieldName in field) {
			if (field[fieldName]) {
				popupFields.push({
					fieldName: fieldLabelMap[fieldName] ? fieldLabelMap[fieldName] : fieldName,
					value: field[fieldName]
				});
			}
		}
		this.popupGridData = process(popupFields, {});
	}

	/**
	 * Builds the popup grid field info data.
	 * @param field
	 */
	private buildPopupFieldDataForFindObject(field: any): void {
		const {matchOn, results} = field;
		const {fieldLabelMap} = this.importBatch;
		let popupFields: Array<any> = [];
		field.query.forEach( (item, index) => {
			const domain = item.domain;
			let recordsFound = null;
			if (matchOn && results && matchOn === index && results.length > 0) {
				recordsFound = results.length;
				this.popup.results = results;
			}
			for (let fieldName in item.kv) {
				if (item.kv[fieldName]) {
					popupFields.push({
						domainIndex: index,
						domainName: domain,
						fieldName: fieldLabelMap[fieldName] ? fieldLabelMap[fieldName] : fieldName,
						value: item.kv[fieldName],
						recordsFound: recordsFound
					});
				}
			}
		});
		this.popupGridData = process(popupFields, { group: this.popupGridGroups});
	}

	/**
	 * Returns the proper popup field info title.
	 * @param {FieldInfoType} type
	 * @returns {string}
	 */
	private getPopupTitle(type: FieldInfoType): string {
		switch (type) {
			case FieldInfoType.CREATE: return 'Create Reference';
			case FieldInfoType.UPDATE: return 'Update Reference';
			case FieldInfoType.FIND: return 'Find Results';
			default: return '';
		}
	}
}