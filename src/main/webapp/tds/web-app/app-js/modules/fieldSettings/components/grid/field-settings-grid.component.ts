// Angular
import {
	Component,
	Input,
	Output,
	EventEmitter,
	OnInit,
	ViewEncapsulation,
	ViewChild,
	OnDestroy,
	ComponentFactoryResolver
} from '@angular/core';
// Model
import {CUSTOM_FIELD_CONTROL_TYPE, FieldSettingsModel} from '../../model/field-settings.model';
import {DomainModel} from '../../model/domain.model';
import {FIELD_COLORS} from '../../model/field-settings.model';
import {DOMAIN} from '../../../../shared/model/constants';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {DialogConfirmAction, DialogExit, DialogService, ModalSize} from 'tds-component-library';
// Component
import {MinMaxConfigurationPopupComponent} from '../min-max/min-max-configuration-popup.component';
import {SelectListConfigurationPopupComponent} from '../select-list/selectlist-configuration-popup.component';
import {NumberConfigurationPopupComponent} from '../number/number-configuration-popup.component';
import {NumberConfigurationConstraintsModel} from '../number/number-configuration-constraints.model';
// Service
import {UILoaderService} from '../../../../shared/services/ui-loader.service';
import {NumberControlHelper} from '../../../../shared/components/custom-control/number/number-control.helper';
import {FieldSettingsService} from '../../service/field-settings.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ProjectService} from '../../../project/service/project.service';
// Other
import {
	GridDataResult,
	DataStateChangeEvent,
} from '@progress/kendo-angular-grid';
import {process, State} from '@progress/kendo-data-query';
import {Observable, Subject} from 'rxjs';
import {Select} from '@ngxs/store';
import {takeUntil} from 'rxjs/operators';

declare var jQuery: any;

@Component({
	selector: 'field-settings-grid',
	encapsulation: ViewEncapsulation.None,
	exportAs: 'fieldSettingsGrid',
	templateUrl: 'field-settings-grid.component.html',
	styles: [
			`
            tr .text-center {
                text-align: center;
            }

            .has-error,
            .has-error:focus {
                border: 1px #f00 solid;
            }
		`,
	],
})
export class FieldSettingsGridComponent implements OnInit, OnDestroy {

	// Retrieve the User context
	@Select(state => state.TDSApp.userContext) userContext$: Observable<any>;
	// Current User Context Model
	public userContextModel: UserContextModel = null;
	// To Call on Component Destroy
	private atComponentDestroy: Subject<boolean> = new Subject<boolean>();

	@Output('save') saveEmitter = new EventEmitter<any>();
	@Output('cancel') cancelEmitter = new EventEmitter<any>();
	@Output('add') addEmitter = new EventEmitter<any>();
	@Output('share') shareEmitter = new EventEmitter<any>();
	@Output('delete') deleteEmitter = new EventEmitter<any>();
	@Output('filter') filterEmitter = new EventEmitter<any>();

	@Input('data') data: DomainModel;
	@Input('domains') domainsList: DomainModel[];
	@Input('isEditable') isEditable: boolean;
	@Input('gridFilter') gridFilter: any;
	@ViewChild('minMax', {static: false})
	minMax: MinMaxConfigurationPopupComponent;
	@ViewChild('selectList', {static: false})
	selectList: SelectListConfigurationPopupComponent;
	public domains: DomainModel[] = [];
	private fieldsSettings: FieldSettingsModel[];
	public gridData: GridDataResult;
	public colors = FIELD_COLORS;
	public formHasError: boolean = null;
	public isDirty = false;
	public state: State = {
		sort: [
			{
				dir: 'asc',
				field: 'order',
			},
		],
		filter: {
			filters: [
				{
					field: 'field',
					operator: 'contains',
					value: '',
				},
			],
			logic: 'or',
		},
	};

	public isEditing = false;
	public isFilterDisabled = false;
	public sortable: boolean | object = {mode: 'single'};
	private fieldsToDelete = [];
	protected resettingChanges = false;
	protected lastEditedControl = null;
	public availableFieldTypes = ['All', 'Custom Fields', 'Standard Fields'];
	public showFilters = false;
	public columns: Array<string> = ['Field', 'Order', 'Label', 'Shared', 'Highlighting', 'Required', 'Display', 'Default Value', 'Control', 'Tooltip Help'];

	// Only APPLICATION asset types has the plan methodology feature.
	private planMethodology = null;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private loaderService: UILoaderService,
		private projectService: ProjectService,
		private translate: TranslatePipe,
		private fieldSettingsService: FieldSettingsService) {

		this.userContext$.pipe(takeUntil(this.atComponentDestroy)).subscribe((userContext: UserContextModel) => {
			this.userContextModel = userContext;
		});
	}

	ngOnInit(): void {
		// if we are on the Application, we get the planMethodology param
		if (this.data.domain === DOMAIN.APPLICATION) {
			this.projectService.getModelForProjectViewEdit(this.userContextModel.project.id).subscribe((project: any) => {
				this.planMethodology = project.data.planMethodology;
			});
		}

		this.fieldsSettings = this.data.fields;
		this.refresh();
		this.domains = [].concat.apply([], this.domainsList);
	}

	public dataStateChange(state: DataStateChangeEvent): void {
		this.state = state;
		this.refresh();
	}

	protected onFilter(): void {
		this.filterEmitter.emit(null);
	}

	public applyFilter(): void {
		this.state.filter.filters = [];

		this.fieldsSettings = this.data.fields;
		if (this.gridFilter.search !== '') {
			let search = new RegExp(this.gridFilter.search, 'i');
			this.fieldsSettings = this.data.fields.filter(
				item =>
					search.test(item.field) ||
					search.test(item.label) ||
					item['isNew']
			);
		}
		if (this.gridFilter.fieldType !== 'All') {
			this.state.filter.filters.push({
				field: 'udf',
				operator: 'eq',
				value: this.gridFilter.fieldType === 'Custom Fields' ? 1 : 0,
			});
			this.state.filter.filters.push({
				field: 'isNew',
				operator: 'eq',
				value: true,
			});
		}
		this.refresh();
	}

	public toggleFilter(): void {
		this.showFilters = !this.showFilters;
	}

	// TODO: Wire this up.
	public filterCount(): number {
		// return this.model.columns.filter((c: ViewColumn) => c.filter).length
		return 0;
	}

	hasFilterApplied(): boolean {
		// return this.model.columns.filter((c: ViewColumn) => c.filter).length > 0 || this.hiddenFilters;
		return false;
	}

	protected onEdit(): void {
		this.loaderService.show();
		setTimeout(() => {
			this.isEditing = true;
			this.resetValidationFlags();
			this.sortable = {mode: 'single'};
			this.isFilterDisabled = false;
			this.onFilter();
			this.loaderService.hide();
		});
	}

	/**
	 * On save all prevent to the user that underlaying data could be deleted
	 * just whenever there is a delete action pending to be saved
	 */
	protected onSaveAll(): void {
		if (!(!this.isEditable || !this.isDirty || this.formHasError)) {
			this.askForDeleteUnderlayingData().subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.notifySaveAll(true);
				}
			});
		}
	}

	/**
	 * If there is records to be deleted, show the confirmation propmpt dialog asking
	 * if those fields should be deleted in the back end as well
	 */
	private askForDeleteUnderlayingData(): Observable<any> {
		const countFieldsToDelete = this.fieldsToDelete.length;

		if (countFieldsToDelete) {
			return this.dialogService.confirm(
				this.translate.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_TITLE'),
				this.translate.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_MESSAGE'),
			);
		}

		return Observable.of({confirm: DialogConfirmAction.CONFIRM});
	}

	/**
	 * Notify to the host component about a save all action
	 * Send the flag to inform about deleting the underlaying data
	 * @param {boolean} deleteUnderLaying True to delete the underlaying data in the backend
	 */
	private notifySaveAll(deleteUnderLaying: boolean): void {
		const savingInfo = {
			deleteUnderLaying,
			callback: () => this.reset(),
		};
		this.saveEmitter.emit(savingInfo);
	}

	/**
	 * On click Cancel button send the message to the host container to cancel the changes
	 * @param {any} event containing the current event control which has the latest error
	 */
	protected onCancel(event: any): void {
		this.resettingChanges = true;
		event = event || this.lastEditedControl || null;

		this.cancelEmitter.emit({
			success: () => {
				this.reset();
				this.refresh();
			},
			failure: () => {
				if (event) {
					this.resettingChanges = false;
					event.target.focus();
				}
			},
		});
	}

	/**
	 * Delete button action, adds field to the pending to delete queue.
	 * if field is shared delete for every domain
	 * @param {FieldSettingsModel} dataItem
	 */
	protected onDelete(dataItem: FieldSettingsModel): void {
		const targetField = this.data.fields.find(
			item => item.field === dataItem.field
		);
		if (targetField) {
			targetField.errorMessage = '';
		}

		dataItem.toBeDeleted = true;
		this.setIsDirty(true);
		this.fieldsToDelete.push(dataItem.field);
		if (dataItem.shared) {
			this.domains.forEach(domain => {
				this.deleteEmitter.emit({
					domain: domain.domain,
					fieldsToDelete: [dataItem.field],
					isSharedField: true,
					addToDeleteCollection: true,
				});
			});
		} else {
			this.deleteEmitter.emit({
				domain: this.data.domain,
				fieldsToDelete: this.fieldsToDelete,
				isSharedField: false,
				addToDeleteCollection: true,
			});
		}
	}

	/**
	 * Undo Delete button action, removes field from pending to delete queue.
	 * if field is shared undo for every domain
	 * @param {FieldSettingsModel} dataItem
	 */
	protected undoDelete(dataItem: FieldSettingsModel): void {
		dataItem.toBeDeleted = false;
		let index = this.fieldsToDelete.indexOf(dataItem.field, 0);
		this.fieldsToDelete.splice(index, 1);

		if (dataItem.shared) {
			this.domains.forEach(domain => {
				this.deleteEmitter.emit({
					domain: domain.domain,
					fieldsToDelete: [dataItem.field],
					isSharedField: true,
					addToDeleteCollection: false,
				});
			});
		} else {
			this.deleteEmitter.emit({
				domain: this.data.domain,
				fieldsToDelete: this.fieldsToDelete,
			});
		}
	}

	/**
	 * Check if a given field is on the pending to deleted queue.
	 * @param {FieldSettingsModel} dataItem
	 * @returns {boolean}
	 */
	protected toBeDeleted(dataItem: FieldSettingsModel): boolean {
		return this.fieldsToDelete.some(item => item === dataItem.field);
	}

	protected onAddCustom(): void {
		this.setIsDirty(true);
		this.formHasError = true;

		this.addEmitter.emit(custom => {
			this.state.sort = [
				{
					dir: 'desc',
					field: 'isNew',
				},
				{
					dir: 'desc',
					field: 'count',
				},
			];
			let model = new FieldSettingsModel();
			model.field = custom;
			model.constraints = {
				required: false,
			};
			model.label = '';
			model['isNew'] = true;
			model['count'] = this.data.fields.length;
			model.control = CUSTOM_FIELD_CONTROL_TYPE.String;
			model.show = true;
			let availableOrder = this.data.fields
				.map(f => f.order)
				.sort((a, b) => a - b)
				.filter(item => !isNaN(item));
			model.order = availableOrder[availableOrder.length - 1] + 1;
			this.data.fields.push(model);
			this.onFilter();

			setTimeout(function () {
				jQuery('#' + model.field).focus();
			});
		});
	}

	protected onShare(field: FieldSettingsModel) {
		this.shareEmitter.emit({
			field: field,
			domain: this.data.domain,
		});
	}

	protected onRequired(field: FieldSettingsModel) {
		if (
			field.constraints.values &&
			(field.control === CUSTOM_FIELD_CONTROL_TYPE.List ||
				field.control === CUSTOM_FIELD_CONTROL_TYPE.YesNo)
		) {
			if (field.constraints.required) {
				field.constraints.values.splice(
					field.constraints.values.indexOf(''),
					1
				);
			} else if (field.constraints.values.indexOf('') === -1) {
				field.constraints.values.splice(0, 0, '');
			}
			if (field.constraints.values.indexOf(field.default) === -1) {
				field.default = null;
			}
		}
	}

	protected onClearTextFilter(): void {
		this.gridFilter.search = '';
		this.onFilter();
	}

	protected reset(): void {
		this.isEditing = false;
		this.sortable = {mode: 'single'};
		this.isFilterDisabled = false;
		this.state.sort = [
			{
				dir: 'asc',
				field: 'order',
			},
		];
		this.resetValidationFlags();
		this.applyFilter();
	}

	/**
	 * Refresh the changes in the data grid, in addition reset the values of the resettingChanges
	 * and lastEditeControls variables
	 */
	public refresh(): void {
		this.gridData = process(this.fieldsSettings, this.state);
		this.resettingChanges = false;
		this.lastEditedControl = null;
	}

	/**
	 * TODO dontiveros: I need to remove this specific type of custom field code, create a helper for each field type and put code in there.
	 * TODO dontiveros: Just like has been done on the Number Field Type.
	 * @param dataItem
	 */
	protected onControlChange(
		previousControl: CUSTOM_FIELD_CONTROL_TYPE,
		dataItem: FieldSettingsModel
	): void {
		switch (dataItem.control) {
			case CUSTOM_FIELD_CONTROL_TYPE.List:
				NumberControlHelper.cleanNumberConstraints(
					dataItem.constraints as any
				);
				// Removes String constraints
				delete dataItem.constraints.maxSize;
				delete dataItem.constraints.minSize;
				if (
					dataItem.constraints.values &&
					dataItem.constraints.values.indexOf('Yes') !== -1 &&
					dataItem.constraints.values.indexOf('No') !== -1
				) {
					dataItem.constraints.values = [];
				}
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.String:
				NumberControlHelper.cleanNumberConstraints(
					dataItem.constraints as any
				);
				// Remove List & YesNo constraints
				delete dataItem.constraints.values;
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.YesNo:
				NumberControlHelper.cleanNumberConstraints(
					dataItem.constraints as any
				);
				dataItem.constraints.values = ['Yes', 'No'];
				if (
					dataItem.constraints.values.indexOf(dataItem.default) === -1
				) {
					dataItem.default = null;
				}
				if (!dataItem.constraints.required) {
					dataItem.constraints.values.splice(0, 0, '');
				}
				break;
			case CUSTOM_FIELD_CONTROL_TYPE.Number:
				// Remove List & YesNo constraints
				delete dataItem.constraints.values;
				// Removes String constraints
				delete dataItem.constraints.maxSize;
				delete dataItem.constraints.minSize;
				NumberControlHelper.initConfiguration(
					dataItem.constraints as NumberConfigurationConstraintsModel
				);
				break;
			default:
				NumberControlHelper.cleanNumberConstraints(
					dataItem.constraints as any
				);
				// Remove List & YesNo constraints
				delete dataItem.constraints.values;
				// Removes String constraints
				delete dataItem.constraints.maxSize;
				delete dataItem.constraints.minSize;
				break;
		}
	}

	/**
	 * Event to update the control of the dataItem after the user has confirmed the change action
	 * @param dataItem  Current grid cell item
	 * @param conversion Contains the information of this conversion
	 */
	protected onFieldTypeChangeSave(dataItem: any, conversion: any): void {
		dataItem.control = conversion.to;
		this.onControlChange(conversion.from, dataItem);
	}

	/**
	 * Handle a change of custom control type event
	 * just for already created fields display a warning about changing the control type
	 * @param dataItem Current grid cell item
	 * @param fieldTypeChange contains the info about the conversion, save and reset events
	 */
	protected onFieldTypeChange(dataItem: any, fieldTypeChange: any) {
		if (dataItem && !dataItem.isNew && fieldTypeChange) {
			this.dialogService.confirm(
				this.translate.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				fieldTypeChange.conversion.getWarningMessage()
			).subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.setIsDirty(true);
					fieldTypeChange.save();
				} else {
					fieldTypeChange.reset();
				}
			});
		} else {
			this.setIsDirty(true);
			fieldTypeChange.save();
		}
	}

	protected onControlModelChange(
		newValue: CUSTOM_FIELD_CONTROL_TYPE,
		dataItem: FieldSettingsModel
	) {
		this.setIsDirty(true);
		const previousControl = dataItem.control;
		if (dataItem.control === CUSTOM_FIELD_CONTROL_TYPE.List) {
			this.dialogService.confirm(
				this.translate.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				'Changing the control will lose all List options. Click Ok to continue otherwise Cancel'
			).subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					dataItem.control = newValue;
					this.onControlChange(previousControl, dataItem);
				} else {
					setTimeout(() => {
						jQuery('#control' + dataItem.field).val('List');
					});
				}
			});
		} else {
			dataItem.control = newValue;
			this.onControlChange(previousControl, dataItem);
		}
	}

	/**
	 * Checks if the given label is an invalid String, and returns true or false respectively.
	 * The error conditions are:
	 * 	- The label is an empty String
	 * 	- The label String matches other label in the fields list.
	 * 	- The label String matches a field name in the fields list.
	 * 	- The label String matches other label or field name in the fields list of another domain.
	 * 	  This comparison is case-insensitive and it doesn't take into account any trailing, leading or in-between spaces.
	 * 	  e.g. label: "Last Modified or last modified or LastModified". other label: "Last Modified". This comparisons will error.
	 *
	 * 	- The label String matches any field names in the list of fields.
	 * 	  This comparison is case-insensitive and it doesn't take into account any trailing, leading or in-between spaces.
	 * 	  e.g. label: "Asset Name or asset N ame or AssetName or assetName". some field name: "assetName". This comparisons will error.
	 *
	 * @See TM-13505
	 * @param label
	 * @returns {boolean}
	 */
	protected hasError(dataItem: FieldSettingsModel) {
		const fields = this.getFieldsExcludingDeleted();

		return (
			dataItem.label.trim() === '' ||
			fields.some(field => field.errorMessage) ||
			this.fieldSettingsService.conflictsWithAnotherLabel(
				dataItem.label,
				fields
			) ||
			this.fieldSettingsService.conflictsWithAnotherFieldName(
				dataItem.label,
				fields
			) ||
			this.fieldSettingsService.conflictsWithAnotherDomain(
				dataItem,
				this.domains,
				this.domains[0]
			)
		);
	}

	/**
	 * Returns a boolean indicating if the fields contain at least one field with error
	 */
	private atLeastOneInvalidField(): boolean {
		const fields = this.getFieldsExcludingDeleted() || [];

		return fields.some(field => field.errorMessage || !field.label);
	}

	/**
	 * On blur input field controls, it applies the validation rules to the label and name of the field control
	 * @param {FieldSettingsModel} dataItem - The model of the asset field control which launched the event
	 * @param {any} event - Context event from the input that launched the change
	 */
	protected onLabelBlur(dataItem: FieldSettingsModel, event: any) {
		// if the data item has an error, mark the whole form as having an error
		this.formHasError = Boolean(
			dataItem.errorMessage || this.atLeastOneInvalidField()
		);

		if (dataItem.errorMessage) {
			if (!this.resettingChanges) {
				this.lastEditedControl = this.lastEditedControl || event;
				setTimeout(() => this.lastEditedControl.target.focus(), 0.1);
			}
		} else {
			if (this.lastEditedControl) {
				if (this.lastEditedControl.target.id === event.target.id) {
					this.lastEditedControl = null;
				}
			}
		}
	}

	/**
	 * Get the list of fields excluding fields to be deleted
	 * @returns {any[]}
	 */
	protected getFieldsExcludingDeleted(): any {
		return this.data.fields.filter(
			item => !(this.fieldsToDelete || []).includes(item.field)
		);
	}

	/**
	 * Function to determine if given field is currently used as Project Plan Methodology.
	 * Note: Only APPLICATION asset types has the plan methodology feature.
	 * @param {FieldSettingsModel} field
	 * @returns {boolean} True or False
	 */
	protected isFieldUsedAsPlanMethodology(field: FieldSettingsModel): boolean {
		if (this.data.domain === DOMAIN.APPLICATION) {
			return this.planMethodology !== null && this.planMethodology.field && this.planMethodology === field.field;
		}
		return false;
	}

	/**
	 * Open The Dialog to Edit Custom Field Setting
	 * @param {ProviderModel} providerModel
	 * @param {number} actionType
	 */
	private openFieldSettingsPopup(dataItem: FieldSettingsModel): void {

		let component: any;

		switch (dataItem.control) {
			case CUSTOM_FIELD_CONTROL_TYPE.String:
				component = MinMaxConfigurationPopupComponent;
				break;

			case CUSTOM_FIELD_CONTROL_TYPE.List:
				component = SelectListConfigurationPopupComponent;
				break;

			default:
				component = NumberConfigurationPopupComponent;
				break;
		}

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: component,
			data: {
				fieldSettingsModel: dataItem,
				domain: this.data.domain
			},
			modalConfiguration: {
				title: 'Confirmation Required',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).subscribe((confirmation: any) => {
			if (confirmation) {
				if (confirmation.status === DialogExit.ACCEPT) {
					this.setIsDirty(true);
				}
			}
		});
	}

	/**
	 * Check if selected field control has configuration available.
	 * @param {CUSTOM_FIELD_CONTROL_TYPE} control
	 * @returns {boolean}
	 */
	protected isAllowedConfigurationForField(control: CUSTOM_FIELD_CONTROL_TYPE): boolean {
		if (
			control === CUSTOM_FIELD_CONTROL_TYPE.List ||
			control === CUSTOM_FIELD_CONTROL_TYPE.String ||
			control === CUSTOM_FIELD_CONTROL_TYPE.Number
		) {
			return true;
		}
		return false;
	}

	/**
	 * Check if the field is allowed to have a default value.
	 * @param {CUSTOM_FIELD_CONTROL_TYPE} control
	 * @returns {boolean}
	 */
	protected isAllowedDefaultValueForField(control: CUSTOM_FIELD_CONTROL_TYPE): boolean {
		if (control &&
			(control === CUSTOM_FIELD_CONTROL_TYPE.String ||
				control === CUSTOM_FIELD_CONTROL_TYPE.YesNo ||
				control === CUSTOM_FIELD_CONTROL_TYPE.List)
		) {
			return true;
		}
		return false;
	}

	/**
	 * This code runs each time a key is pressed.
	 *
	 * @param {FieldSettingsModel} dataItem - The model of the asset field control which launched the event
	 * @param {KeyboardEvent} event - Context event from the input that launched the change
	 */
	protected onKeyPressed(dataItem: FieldSettingsModel, event: KeyboardEvent): void {
		// Mark form as dirty
		this.setIsDirty(true);
		// On esc key pressed, open confirmation dialog
		if (event.code === 'Escape') {
			this.onCancel(event);
		}
		// Validate conflicts between label names and field names
		dataItem.errorMessage = '';
		const fields = this.getFieldsExcludingDeleted();
		const message =
			'The label must be different from all other field names and labels';

		if (this.fieldSettingsService.conflictsWithAnotherLabel(dataItem.label, fields)) {
			dataItem.errorMessage = message;
		} else {
			if (this.fieldSettingsService.conflictsWithAnotherFieldName(dataItem.label, fields)) {
				dataItem.errorMessage = message;
			} else {
				if (this.fieldSettingsService.conflictsWithAnotherDomain(dataItem, this.domains, this.domains[0])) {
					dataItem.errorMessage = message;
				}
			}
		}
		this.formHasError = Boolean(
			dataItem.errorMessage || this.atLeastOneInvalidField()
		);
	}

	/**
	 * Set the flag to indicate the form is dirty
	 */
	protected setIsDirty(value: boolean):
		void {
		this.isDirty = value;
	}

	/**
	 * Reset the flags to keep track the validation state
	 */
	private resetValidationFlags(): void {
		this.formHasError = null;
		this.setIsDirty(false);
	}

	/**
	 * Destroy any subscribed observable
	 */
	ngOnDestroy(): void {
		// Globally Destroy anything attached to it
		this.atComponentDestroy.unsubscribe();
	}

}
