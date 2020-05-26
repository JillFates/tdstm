// Angular
import {
	Component,
	ElementRef,
	HostListener,
	Input,
	OnDestroy,
	OnInit,
	QueryList,
	ViewChild,
	ViewChildren
} from '@angular/core';
import {NgForm} from '@angular/forms';
// Model
import {
	APIActionModel,
	APIActionParameterModel,
	APIActionType,
	EventReaction,
	Languages
} from '../../model/api-action.model';
import {ProviderModel} from '../../../provider/model/provider.model';
import {Permission} from '../../../../shared/model/permission.model';
import {ActionType, COLUMN_MIN_WIDTH} from '../../../../shared/model/data-list-grid.model';
import {INTERVAL, INTERVALS} from '../../../../shared/model/constants';
import {AgentMethodModel, CredentialModel, DictionaryModel} from '../../model/agent.model';
import {DataScriptModel} from '../../../dataScript/model/data-script.model';
import {CHECK_ACTION} from '../../../../shared/components/check-action/model/check-action.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Component
import {ApiActionViewEditReactionsComponent} from './api-action-view-edit-reactions.component';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
// Service
import {APIActionService} from '../../service/api-action.service';
import {CustomDomainService} from '../../../fieldSettings/service/custom-domain.service';
import {ObjectUtils} from '../../../../shared/utils/object.utils';
import {SortUtils} from '../../../../shared/utils/sort.utils';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {PermissionService} from '../../../../shared/services/permission.service';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Other
import * as R from 'ramda';
import {forkJoin, ReplaySubject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';

declare var jQuery: any;

enum NavigationTab {
	Info,
	Parameters,
	Reactions,
	HttpAPI,
	Script
}

@Component({
	selector: 'api-action-view-edit',
	templateUrl: 'api-action-view-edit.component.html',
	styles: [`
      .has-error, .has-error:focus {
          border: 1px #f00 solid;
      }

      .invalid-form {
          color: red;
          font-weight: bold;
      }

      .script-error {
          margin-bottom: 18px;
      }

      label.url-label {
          width: 146px;
      }

      .url-input {
          width: 82%;
      }
	`]
})
export class APIActionViewEditComponent extends Dialog implements OnInit, OnDestroy {
	@Input() data: any;
	// Forms
	@ViewChild('actionNameInput', { static: false }) actionNameInput: ElementRef;
	@ViewChild('apiActionForm', { static: false }) apiActionForm: NgForm;
	@ViewChild('scriptForm', { static: false }) scriptForm: NgForm;
	@ViewChild('simpleInfoForm', { static: false }) simpleInfoForm: NgForm;
	@ViewChild('httpAPIForm', { static: false }) httpAPIForm: NgForm;
	@ViewChild('apiActionParametersForm', { static: false }) apiActionParametersForm: NgForm;
	@ViewChild('apiActionReactionForm', { static: false }) apiActionReactionForm: NgForm;
	@ViewChildren('codeMirror') public codeMirrorComponents: QueryList<CodeMirrorComponent>;
	@ViewChild('apiActionContainer', { static: false }) apiActionContainer: ElementRef;
	@ViewChild('apiActionViewEditReactionsComponent', { static: false }) apiActionViewEditReactionsComponent: ApiActionViewEditReactionsComponent;
	public codeMirrorComponent: CodeMirrorComponent;
	public apiActionModel: APIActionModel;
	public providerList = new Array<ProviderModel>();
	public dictionaryList = new Array<DictionaryModel>();
	public agentMethodList = new Array<AgentMethodModel>();
	public agentCredentialList = new Array<CredentialModel>();
	public providerCredentialList = new Array<CredentialModel>();
	public datascriptList = new Array<DataScriptModel>();
	public providerDatascriptList = new Array<DataScriptModel>();
	public parameterList: Array<any>;
	public editModeFromView = false;
	public dataScriptMode = APIActionModel;
	public actionTypes = ActionType;
	public interval = INTERVAL;
	public selectedInterval = { value: 0, interval: '' };
	public selectedLapsed = { value: 0, interval: '' };
	public selectedStalled = { value: 0, interval: '' };
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public PLEASE_SELECT = null;
	public commonFieldSpecs;
	public SUPPLIED_CREDENTIAL = 'SUPPLIED';
	public modalType = ActionType.VIEW;
	public validInfoForm = false;
	public validParametersForm = true;
	public invalidScriptSyntax = false;
	public checkActionModel = CHECK_ACTION;
	lastSelectedDictionaryModel: DictionaryModel = {
		id: 0,
		name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')
	};
	lastSelectedAgentMethodModel: AgentMethodModel = {
		uId: '0',
		dictionaryMethodName: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')
	};
	disableRemoteInvocationCbx = true;
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	isApiActionParametersFormValid = true;
	loadingLists = true;
	protected tabsEnum = NavigationTab;
	protected httpMethodList = new Array<any>();
	protected actionTypesList = [];
	protected remoteCredentials = [];
	protected defaultItem = { id: '', value: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER') };
	protected EnumAPIActionType = APIActionType;
	protected formValidStates = {
		simpleInfoForm: { isConfiguredValidators: false },
		httpAPIForm: { isConfiguredValidators: false },
		scriptForm: { isConfiguredValidators: false },
	};
	protected hasEarlyAccessTMRPermission = false;
	private WEB_API = 'WEB_API';
	private dataSignature: string;
	private dataParameterListSignature: string;
	private requiredFields = ['name', 'provider', 'actionType'];
	private intervals = INTERVALS;
	private currentTab: NavigationTab = NavigationTab.Info;
	private initFormLoad = true;
	private codeMirror = {
		mode: 'Groovy',
		rows: 10,
		cols: 4
	};
	private defaultDictionaryModel = { name: '', id: 0 };

	constructor(
		public dialogService: DialogService,
		public permissionService: PermissionService,
		private apiActionService: APIActionService,
		private customDomainService: CustomDomainService,
		private translatePipe: TranslatePipe) {
		super();
		this.dataParameterListSignature = '';
		this.parameterList = [];
	}

	ngOnInit(): void {
		this.hasEarlyAccessTMRPermission = this.permissionService.hasPermission(Permission.EarlyAccessTMR);
		this.apiActionModel = R.clone(this.data.apiActionModel);
		this.modalType = this.data.actionType;
		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			tooltipText: 'Edit',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			active: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.changeToEditApiAction.bind(this)
		});
		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			tooltipText: 'Save',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.CREATE,
			disabled: () => !this.canSave() || !this.isDirty() || this.loadingLists,
			type: DialogButtonType.ACTION,
			action: this.onSaveApiAction.bind(this)
		});
		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			tooltipText: 'Delete',
			show: () => this.modalType !== this.actionTypes.CREATE,
			type: DialogButtonType.ACTION,
			action: this.onDeleteApiAction.bind(this)
		});
		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: ((this.modalType === this.actionTypes.VIEW) ? 'Close' : 'Cancel'),
			show: () => this.modalType === this.actionTypes.VIEW || this.modalType === this.actionTypes.CREATE,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});
		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			tooltipText: 'Cancel',
			show: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.cancelEditDialog.bind(this)
		});
		// set the default empty values for dictionary in case it is not defined
		if (!this.apiActionModel.dictionary) {
			this.apiActionModel.dictionary = this.defaultDictionaryModel;
		}
		this.selectedInterval = R.clone(this.data.apiActionModel.polling.frequency);
		this.selectedLapsed = R.clone(this.data.apiActionModel.polling.lapsedAfter);
		this.selectedStalled = R.clone(this.data.apiActionModel.polling.stalledAfter);
		this.apiActionModel.script = this.apiActionModel.script || '';
		this.apiActionModel.isRemote = this.isRemote();
		// Fork Join load list api calls.
		/**
		 * Note!! with this fork joined api calls, now the last api call load list should always be getDataScripts()
		 */
		forkJoin<any>(
			this.apiActionService.getProviders(),
			this.apiActionService.getAPIActionEnums(),
			this.customDomainService.getCommonFieldSpecsWithShared()
		)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe({
				next: result => {
					this.getProviders(result[0]);
					this.getAgents(result[1]);
					this.getCommonFieldSpecs(result[2]);
				},
				complete: () => {
					this.prepareFormListener();
				},
			});
	}

	/**
	 * unsubscribe from all subscriptions on destroy hook.
	 * @HostListener decorator ensures the OnDestroy hook is called on events like
	 * Page refresh, Tab close, Browser close, navigation to another view.
	 */
	@HostListener('window:beforeunload')
	ngOnDestroy(): void {
		this.unsubscribeOnDestroy$.next();
		this.unsubscribeOnDestroy$.complete();
	}

	/**
	 * Get the List of Providers
	 */
	getProviders(result): void {
		if (this.modalType === ActionType.CREATE) {
			this.providerList.push({ id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER') });
			this.apiActionModel.provider = this.providerList[0];
			this.modifySignatureByProperty('provider');
		}
		this.providerList.push(...result);
		this.getCredentials();
	}

	/**
	 * Get the list of possible Agents
	 */
	getAgents(result: any): void {
		if (this.modalType === ActionType.CREATE) {
			this.dictionaryList.push({ id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER') });
			this.apiActionModel.dictionary = this.dictionaryList[0];
			this.modifySignatureByProperty('dictionary');
		}
		this.dictionaryList.push(...result.data.agentNames);
		if (this.apiActionModel.agentMethod && this.apiActionModel.agentMethod.uId) {
			this.onDictionaryValueChange(this.apiActionModel.dictionary);
		} else {
			this.agentMethodList.push({
				uId: '0',
				dictionaryMethodName: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')
			});
			this.apiActionModel.agentMethod = this.agentMethodList[0];
			this.modifySignatureByProperty('agentMethod');
		}
		this.httpMethodList.push(this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER'));
		this.httpMethodList.push(...result.data.httpMethod);
		if (!this.apiActionModel.httpMethod) {
			this.apiActionModel.httpMethod = this.httpMethodList[0];
		}
		if (result && result.data.actionTypes) {
			this.prepareActionTypeList(result);
		}
		if (result && result.data.remoteCredentialMethods) {
			this.prepareRemoteCredentialsList(result);
		}
	}

	/**
	 * Prepare remote credentials list to display in UI
	 * @param result
	 */
	prepareRemoteCredentialsList(result: any): void {
		this.remoteCredentials = [];
		this.remoteCredentials.push({ id: 0, value: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER') });
		const keys = Object.keys(result.data.remoteCredentialMethods);
		keys.forEach((key: string) => {
			this.remoteCredentials.push({ id: key, value: result.data.remoteCredentialMethods[key] });
		});
		if (!this.apiActionModel.remoteCredentialMethod) {
			this.apiActionModel.remoteCredentialMethod = this.remoteCredentials[0];
		}
	}

	/**
	 * Prepare action type list to display in UI.
	 * @param result
	 */
	prepareActionTypeList(result: any): void {
		this.actionTypesList = [];
		this.actionTypesList.push({ id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER') });
		const keys = Object.keys(result.data.actionTypes);
		keys.forEach((key: string) => {
			this.actionTypesList.push({ id: key, name: result.data.actionTypes[key] });
		});
		if (this.apiActionModel.tabActionType === APIActionType.HTTP_API) {
			this.apiActionModel.actionType = { id: this.WEB_API };
		} else if (!this.apiActionModel.actionType) {
			this.apiActionModel.actionType = this.actionTypesList[0];
		}
	}

	/**
	 * Get the list of Credentials
	 */
	getCredentials(): void {
		this.apiActionService.getCredentials()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(result: any) => {
					if (this.modalType === ActionType.CREATE || !this.apiActionModel.credential) {
						this.agentCredentialList.push({
							id: 0,
							name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')
						});
						this.apiActionModel.credential = this.agentCredentialList[0];
						this.modifySignatureByProperty('credential');
					}
					this.agentCredentialList.push(...result);
					// Important this should be at this point the last call of ALL loading lists since now we forkJoin all.
					this.getDataScripts();
				},
				(err) => console.log(err));
	}

	/**
	 * Get the list of DataScript
	 */
	getDataScripts(): void {
		this.apiActionService.getDataScripts()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe(
				(result) => {
					if (this.modalType === ActionType.CREATE) {
						this.datascriptList.push({
							id: 0,
							name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')
						});
						this.apiActionModel.defaultDataScript = this.datascriptList[0];
						this.modifySignatureByProperty('defaultDataScript');
					}
					if (result.length > 0) {
						result = result.sort((a, b) => SortUtils.compareByProperty(a, b, 'name'));
						this.datascriptList.push(...result);
						if (this.apiActionModel.provider && this.apiActionModel.provider.id !== 0) {
							this.onProviderValueChange(this.apiActionModel.provider, true);
						}
					}
				},
				(err) => console.log(err));
	}

	/**
	 * Get the list of existing parameters for the API Action
	 */
	getParameters(): void {
		this.apiActionService.getParameters(this.apiActionModel).subscribe(
			(result: any) => {
				this.parameterList = result;
				this.parameterList.forEach((parameter) => {
					this.onContextValueChange(parameter);
				});
				setTimeout(() => {
					this.dataParameterListSignature = JSON.stringify(this.parameterList);
				}, 100);
			},
			(err) => console.log(err));
	}

	/**
	 * Preload the list of Common Fields Specs
	 */
	getCommonFieldSpecs(result): void {
		this.commonFieldSpecs = result;
		if (this.modalType !== ActionType.CREATE) {
			this.getParameters();
		}
	}

	/**
	 * Validate required fields before saving model
	 * @param model - The model to be saved
	 */
	public validateRequiredFields(model: APIActionModel): boolean {
		let returnVal = true;
		this.requiredFields.forEach((field) => {
			if (!model[field]) {
				returnVal = false;
				return false;
			} else if (typeof model[field] === 'string' && !model[field].replace(/\s/g, '').length) {
				returnVal = false;
				return false;
			}
		});
		return returnVal;
	}

	/**
	 * Verify if model has changed, also checks the param list, if one of the two are dirty returns true.
	 * @returns {boolean}
	 */
	isDirty(): boolean {
		return (this.dataSignature !== JSON.stringify(this.apiActionModel)) || this.isParameterListDirty();
	}

	/**
	 * Verify if parameter list has changed:
	 * If we have pre-existing parameters then compare vs the current parameter list.
	 * If we don't have any pre-existing parameter then just validate current parameter this is not empty.
	 * @returns {boolean}
	 */
	isParameterListDirty(): boolean {
		if (this.dataParameterListSignature !== '') {
			let newParamList = '';
			if (this.parameterList.length) {
				newParamList = JSON.stringify(this.parameterList);
			}
			return this.dataParameterListSignature !== newParamList;
		} else {
			return this.parameterList.length > 0;
		}
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelEditDialog(): void {
		// Prevent exit if everything hasn't been loaded yet.
		if (this.loadingLists) {
			return;
		}
		if (this.isDirty() && this.modalType !== this.actionTypes.VIEW) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					// Put back original model
					this.apiActionModel = JSON.parse(this.dataSignature);
					this.dataSignature = JSON.stringify(this.apiActionModel);
					this.modalType = this.actionTypes.VIEW;
					this.setTitle(this.getModalTitle(this.modalType));
				} else if (data.confirm === DialogConfirmAction.CONFIRM && this.data.openFromList) {
					this.onCancelClose();
				}
			});
		} else {
			if (!this.data.openFromList) {
				this.modalType = this.actionTypes.VIEW;
				this.setTitle(this.getModalTitle(this.modalType));
			} else {
				this.onCancelClose();
			}
		}
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
				),
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
				)
			)
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
	}

	/**
	 * Determine if the tab sent as parameter is the current one selected
	 * @param num
	 */
	public isCurrentTab(num: number): boolean {
		return this.currentTab === num;
	}

	/**
	 * Set the flag that grabs the reference to the current tab
	 * @param tab
	 */
	public setCurrentTab(tab: NavigationTab): void {
		if (tab === NavigationTab.Script) {
			this.disableCodeMirrors();
		}
		this.editModeFromView = false;
		if (tab === NavigationTab.Info && this.loadingLists) {
			this.prepareFormListener();
		}
		if (tab === NavigationTab.Reactions) {
			this.disableCodeMirrors();
		}
		this.currentTab = tab;
		this.verifyIsValidForm();
	}

	/**
	 * On a new Dictionary selected
	 * @param dictionaryModel
	 */
	onDictionaryValueChange(dictionaryModel: DictionaryModel): void {
		dictionaryModel = dictionaryModel ? dictionaryModel : this.defaultDictionaryModel;
		this.apiActionModel.dictionary = dictionaryModel;
		if (this.modalType === this.actionTypes.EDIT && this.lastSelectedDictionaryModel && this.lastSelectedDictionaryModel.id !== 0) {
			this.dialogService.confirm(
				'Confirmation Required',
				'Changing the Dictionary or Method will overwrite many of the settings of the Action. Are you certain that you want to proceed?')
				.subscribe((data: any) => {
					this.loadDictionaryModel(dictionaryModel, data.confirm === DialogConfirmAction.CONFIRM);
				});
		} else {
			this.loadDictionaryModel(dictionaryModel, true);
		}
	}

	/**
	 * Listener for the Select when the Value Method Changes.
	 * @param event
	 */
	onMethodValueChange(event: any): void {
		if (this.modalType === this.actionTypes.EDIT && this.lastSelectedAgentMethodModel && this.lastSelectedAgentMethodModel.id !== '0') {
			this.dialogService.confirm(
				'Confirmation Required',
				'Changing the Dictionary or Method will overwrite many of the settings of the Action. Are you certain that you want to proceed?'
			)
				.subscribe((data: any) => {
					this.loadAgentMethodModel(data.confirm === DialogConfirmAction.CONFIRM);
				});
		} else {
			this.loadAgentMethodModel(true);
		}
	}

	/**
	 * When the Context has change, we should load the list of params associate with the Asset Class,
	 * if the value is USER_DEF, the field will become a text input field
	 */
	onContextValueChange(dataItem: APIActionParameterModel): void {
		if (dataItem && dataItem.context) {
			let context = (dataItem.context['assetClass']) ? dataItem.context['assetClass'] : dataItem.context;
			let fieldSpecs = this.commonFieldSpecs.find((spec) => {
				return spec.domain === context;
			});
			if (fieldSpecs) {
				dataItem.currentFieldList = fieldSpecs.fields;
				dataItem.sourceFieldList = fieldSpecs.fields;
				let property = dataItem.currentFieldList.find((field) => {
					const fieldName = dataItem.fieldName.toLowerCase();
					return field.field.toLowerCase() === fieldName || field.label.toLowerCase() === fieldName;
				});
				if (property) {
					dataItem.fieldName = property;
				}
			}
			dataItem.fieldName = dataItem.fieldName && dataItem.fieldName.field ? dataItem.fieldName.field : '';
			this.verifyIsValidForm();
		}
	}

	/**
	 * Execute the API to validated every Syntax Value.
	 */
	onCheckAllSyntax(): void {
		this.apiActionViewEditReactionsComponent.onCheckAllSyntax();
	}

	/**
	 * Verify if this is on View mode
	 * @returns {boolean}
	 */
	isViewMode(): boolean {
		return this.modalType === this.actionTypes.VIEW;
	}

	/**
	 * Validation cases:
	 * - When hasEarlyAccessTMRPermission and action type is WEB API validate:
	 * 		Main Simple form, HTTP API form, Script form, Parameters form, Required Fields
	 * - When hasEarlyAccessTMRPermission and action type IS NOT WEB API validate:
	 * 		Main Simple form, Script form, Parameters form, Required Fields
	 * - When doesn't have earlyAccessTMRPermission validate:
	 * 		API Action form, Parameters form, Required Fields
	 */
	canSave(): boolean {
		const actionTypeId = R.pathOr(null, ['actionType', 'id'], this.apiActionModel);
		if (this.hasEarlyAccessTMRPermission && actionTypeId === this.WEB_API) {
			return this.commonValidationsForEarlyAccessTMRPermission() && this.httpAPIForm && this.httpAPIForm.valid;
		} else if (this.hasEarlyAccessTMRPermission && actionTypeId !== this.WEB_API) {
			return this.commonValidationsForEarlyAccessTMRPermission();
		} else {
			return this.apiActionForm && this.apiActionForm.valid
				&& this.validParametersForm
				&& this.validateRequiredFields(this.apiActionModel);
		}
	}

	/**
	 * Evaluates the common validations for hasEarlyAccessTMRPermission
	 */
	commonValidationsForEarlyAccessTMRPermission(): boolean {
		return this.simpleInfoForm && this.simpleInfoForm.valid
		&& this.isScriptFormValid()
		&& this.validParametersForm
		&& this.validateRequiredFields(this.apiActionModel);
	}

	onChangeType(type: any): void {
		const language = Languages[type.id];
		if (language) {
			this.codeMirror.mode = language
		}
		this.apiActionModel.isRemote = this.isRemote();
		// on create api action, set the default value for is remote action
		if (this.modalType === ActionType.CREATE) {
			APIActionModel.createBasicReactions(this.apiActionModel, type.id === this.WEB_API);
			if (this.apiActionModel.isRemote) {
				this.setSelectEventReaction('ERROR', true);
				this.setSelectEventReaction('SUCCESS', true);
			}
		}
	}

	getClonedCodeMirrorSettings(properties: any): any {
		const cloned = Object.assign({}, this.codeMirror, properties);
		return cloned;
	}

	/**
	 * Based on the action type determines if the invocation is remote
	 */
	isRemote(): boolean {
		return Boolean(this.apiActionModel.actionType && this.apiActionModel.actionType.id !== this.WEB_API);
	}

	/**
	 * Determine if the current credential type selected is Supplied by Transition Manager
	 */
	isSuppliedCredential(): boolean {
		const id = R.pathOr('', ['remoteCredentialMethod', 'id'], this.apiActionModel);
		return id === this.SUPPLIED_CREDENTIAL;
	}

	/**
	 * Get a specific event reaction searching by type and set its selected property
	 */
	setSelectEventReaction(type: string, selected: boolean): void {
		const evenReaction = this.apiActionModel.eventReactions.find((item: EventReaction) => item.type === type);
		if (evenReaction) {
			evenReaction.selected = selected;
		}
	}

	/**
	 * Extract the datascript name
	 */
	getDataScriptName(): string {
		return (this.apiActionModel && this.apiActionModel.defaultDataScript && this.apiActionModel.defaultDataScript.name) || '';
	}

	onParametersFormChange(event: { parameterList: Array<any>, isFormValid: boolean }): void {
		this.isApiActionParametersFormValid = event.isFormValid;
		this.parameterList = this.parameterList;
		this.verifyIsValidForm();
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * The NgIf hides elements completely in the UI but makes transition of tabs more smooth
	 * Some complex component like Grid or Code Source got affected by this
	 * this method subscribe the listener to have control of each tab validation.
	 */
	protected prepareFormListener(): void {
		if (this.simpleInfoForm) {
			this.formValidStates.simpleInfoForm.isConfiguredValidators = true;
		}
		if (this.apiActionForm) {
			this.apiActionForm.valueChanges
				.pipe(takeUntil(this.unsubscribeOnDestroy$))
				.subscribe(val => {
					this.verifyIsValidForm();
				});
		}
		// Every time we change the title, it means we switched to View, Edit or Create
		this.setTitle(this.getModalTitle(this.modalType));
		this.dataSignature = JSON.stringify(this.apiActionModel);
		// this should be the last call in order to say hey I finish loading all data needed for the form.
		this.loadingLists = false;
		this.verifyIsValidForm();
	}

	/**
	 * Create a new DataScript
	 */
	protected onSaveApiAction(): void {
		if (this.canSave()) {
			this.apiActionService.saveAPIAction(this.apiActionModel, this.parameterList).subscribe(
				(result: any) => {
					if (result) {
						this.dataSignature = JSON.stringify(this.apiActionModel);
						super.onAcceptSuccess(result);
					}
				},
				(err) => console.log(err));
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditApiAction(): void {
		this.editModeFromView = true;
		this.modalType = this.actionTypes.EDIT;
		this.setTitle(this.getModalTitle(this.modalType));
		this.verifyIsValidForm();
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteApiAction(): void {
		this.dialogService.confirm(
			this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_TITLE'
			),
			this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.DELETE_ITEM_CONFIRMATION'
			)
		)
			.subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.apiActionService
						.deleteAPIAction(this.apiActionModel.id)
						.subscribe(
							result => {
								this.onCancelClose(result);
							},
							err => console.log(err)
						);
				}
			});
	}

	/**
	 * Determine if the tab is enabled
	 * @param num
	 */
	protected isTabEnabled(actionType: APIActionType): boolean {
		const actionTypeId = R.pathOr(null, ['actionType', 'id'], this.apiActionModel);
		if (actionType === APIActionType.HTTP_API) {
			return actionTypeId === this.WEB_API;
		}
		if (actionType === APIActionType.SCRIPT) {
			return actionTypeId !== null || actionTypeId !== this.WEB_API;
		}
		return false;
	}

	/**
	 *  Verify if the Form is on a Valid state when switching between tabs.
	 */
	protected verifyIsValidForm(): void {
		// Test API Action Form
		if (this.apiActionForm) {
			this.validInfoForm = this.apiActionForm.valid;
			this.initFormLoad = false;
		}
		if (this.editModeFromView) {
			this.validInfoForm = this.editModeFromView;
		}
		this.validParametersForm = this.isApiActionParametersFormValid;
		if (!this.formValidStates.httpAPIForm.isConfiguredValidators) {
			if (this.httpAPIForm) {
				this.formValidStates.httpAPIForm.isConfiguredValidators = true;
			}
		}
		if (!this.formValidStates.scriptForm.isConfiguredValidators) {
			if (this.scriptForm) {
				this.formValidStates.scriptForm.isConfiguredValidators = true;
			}
		}
	}

	/**
	 *
	 * @param pollingObject
	 */
	protected onIntervalChange(interval: any, pollingObject: any): void {
		let newVal = DateUtils.convertInterval(pollingObject, interval.interval);
		pollingObject.interval = interval.interval;
		pollingObject.value = newVal;
	}

	/**
	 * On a new Provider Value change
	 * @param value
	 */
	protected onProviderValueChange(providerModel: ProviderModel, previousValue: boolean): void {
		// Populate only the Credentials that are related to the provider
		this.providerCredentialList = new Array<CredentialModel>();
		this.providerCredentialList.push({ id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER') });
		this.providerCredentialList = this.providerCredentialList.concat(this.agentCredentialList.filter((credential) => (credential.provider) && credential.provider.id === providerModel.id));
		// Populate only the DataScripts that are related to the provider
		this.providerDatascriptList = new Array<DataScriptModel>();
		this.providerDatascriptList.push({ id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER') });
		this.providerDatascriptList = this.providerDatascriptList.concat(this.datascriptList.filter((dataScript) => (dataScript.provider) && dataScript.provider.id === providerModel.id));
		if (previousValue) {
			this.apiActionModel.defaultDataScript = this.providerDatascriptList.find((datascript) => datascript.id === this.apiActionModel.defaultDataScript.id);
			this.modifySignatureByProperty('defaultDataScript');
			this.apiActionModel.credential = this.providerCredentialList.find((credential) => credential.id === this.apiActionModel.credential.id);
			this.modifySignatureByProperty('credential');
		} else {
			this.apiActionModel.defaultDataScript = this.providerDatascriptList[0];
			this.apiActionModel.credential = this.providerCredentialList[0];
		}
	}

	/**
	 * Track the Last Agent Selected
	 */
	protected onOpenDictionary(): void {
		if (this.apiActionModel.dictionary && this.apiActionModel.dictionary.id !== 0) {
			this.lastSelectedDictionaryModel = R.clone(this.apiActionModel.dictionary);
		}
	}

	/**
	 * Dropdown opens in a global document context, this helps to expands the limits
	 */
	protected onOpenAgentMethod(): void {
		if (this.apiActionModel.agentMethod && this.apiActionModel.agentMethod.uId !== '0') {
			this.lastSelectedAgentMethodModel = R.clone(this.apiActionModel.agentMethod);
		}
		setTimeout(() => {
			jQuery('kendo-popup').css('width', 'auto');
		}, 100);
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(modalType: ActionType): string {
		let modalTitle = '';
		if (modalType === ActionType.CREATE) {
			modalTitle = 'Action Create';
		} else {
			modalTitle = modalType === ActionType.EDIT ? 'Action Edit' : 'Action Detail';
		}
		if (modalType === ActionType.CREATE || modalType === ActionType.EDIT) {
			setTimeout(() => {
				this.onSetUpFocus(this.actionNameInput);
			});
		}
		return modalTitle + ((this.apiActionModel && this.apiActionModel.name) ? ' ' + this.apiActionModel.name : '');
	}

	/**
	 * Disables codemirrors on the current tab if not in edit state
	 */
	private disableCodeMirrors() {
		this.codeMirrorComponents.changes
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((comps: QueryList<CodeMirrorComponent>) => {
				comps.forEach((child) => {
					this.codeMirrorComponent = child;
					setTimeout(() => {
						child.setDisabled(this.modalType === ActionType.VIEW);
					}, 100);
				});
			});
	}

	private loadDictionaryModel(dictionaryModel: DictionaryModel, changeAgent: boolean): void {
		if (changeAgent) {
			if (dictionaryModel.id !== 0) {
				this.apiActionService.getActionMethodById(dictionaryModel.id)
					.pipe(takeUntil(this.unsubscribeOnDestroy$))
					.subscribe(
						(result: any) => {
							this.agentMethodList = new Array<AgentMethodModel>();
							this.agentMethodList.push({
								uId: '0',
								dictionaryMethodName: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')
							});
							if (this.apiActionModel.agentMethod) {
								this.apiActionModel.agentMethod = result.find((agent) => {
									return (agent.id + agent.name) === this.apiActionModel.agentMethod.uId;
								});
							}
							if (!this.apiActionModel.agentMethod) {
								this.apiActionModel.agentMethod = this.agentMethodList[0];
							}
							this.modifySignatureByProperty('agentMethod');
							this.agentMethodList = result;
						},
						(err) => console.log(err));
			} else {
				this.agentMethodList = new Array<AgentMethodModel>();
				this.agentMethodList.push({
					uId: '0',
					dictionaryMethodName: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')
				});
				this.apiActionModel.agentMethod = this.agentMethodList[0];
			}
		} else if (this.lastSelectedDictionaryModel) {
			// Return the value to the previous one if is on the same List
			let agent = this.dictionaryList.find((method) => {
				return method.id === this.lastSelectedDictionaryModel.id;
			});
			if (agent) {
				this.apiActionModel.dictionary = R.clone(this.lastSelectedDictionaryModel);
			} else {
				this.apiActionModel.dictionary = R.clone(this.dictionaryList[0]);
			}
		}
	}

	/**
	 * Pre-populate the values of the Agent and Params if the changeMethod is true
	 * or if this is the firs time ( Create )
	 * @param {boolean} changeMethod
	 */
	private loadAgentMethodModel(changeMethod: boolean): void {
		if (changeMethod) {
			this.apiActionModel.endpointUrl = this.apiActionModel.agentMethod.endpointUrl;
			this.apiActionModel.dictionaryMethodName = this.apiActionModel.agentMethod.name || this.apiActionModel.agentMethod.dictionaryMethodName;
			this.apiActionModel.docUrl = this.apiActionModel.agentMethod.docUrl;
			this.apiActionModel.isPolling = this.apiActionModel.agentMethod.isPolling;
			this.apiActionModel.polling = this.apiActionModel.agentMethod.polling;
			this.apiActionModel.producesData = this.apiActionModel.agentMethod.producesData;
			this.lastSelectedAgentMethodModel = R.clone(this.apiActionModel.agentMethod);
			this.guardParams();
			this.parameterList = this.apiActionModel.agentMethod.methodParams;
			this.parameterList.forEach((parameter) => {
				this.onContextValueChange(parameter);
			});
			// Populate Reaction Scripts if present
			this.populateReactionScripts();
			// Populate HttpMethod if present
			this.populateHttpMethod();
			this.verifyIsValidForm();
		} else if (this.lastSelectedAgentMethodModel) {
			// Return the value to the previous one if is on the same List
			let agentMethod = this.agentMethodList.find((method) => {
				return (method.id + method.name) === this.lastSelectedAgentMethodModel.uId;
			});
			if (agentMethod) {
				this.apiActionModel.agentMethod = R.clone(this.lastSelectedAgentMethodModel);
			} else {
				this.apiActionModel.agentMethod = R.clone(this.agentMethodList[0]);
			}
		}
	}

	/**
	 * Populates reaction scripts code mirrors based on the method dictionary configuration.
	 */
	private populateReactionScripts(): void {
		const methodScripts = this.apiActionModel.agentMethod.script;
		APIActionModel.createBasicReactions(this.apiActionModel, this.apiActionModel.actionType && this.apiActionModel.actionType.id === this.WEB_API);
		for (let reactionType in methodScripts) {
			if (methodScripts[reactionType]) {
				let match = this.apiActionModel.eventReactions.find(item => item.type === reactionType);
				if (match) {
					match.value = methodScripts[reactionType];
					match.open = true;
					match.selected = true;
				}
			}
		}
	}

	/**
	 * Populates http method based on the Method dictionary configuration.
	 */
	private populateHttpMethod(): void {
		const httpMethod = this.apiActionModel.agentMethod.httpMethod;
		const match = this.httpMethodList.find(item => item === httpMethod);
		if (match) {
			this.apiActionModel.httpMethod = httpMethod;
		}
	}

	/**
	 * Temp Fix to obtain the context, I need this just to proceed with the TM-9849
	 * @returns {any}
	 */
	private guardParams(): any {
		this.apiActionModel.agentMethod.methodParams.forEach((item, index) => {
			if (item.context && item.context.name) {
				item.context = item.context.name;
			}
			if (!item.context || item.context === 'null' || item.context === null) {
				this.apiActionModel.agentMethod.methodParams.splice(index, 1);
			}
			if (item.param) {
				if (item.param === 'null' || item.param === null) {
					item.param = '';
				}
				item['paramName'] = item.param;
				delete item.param;
			}
			if (item.property) {
				if (item.property === 'null' || item.property === null) {
					item.property = '';
				}
				item['fieldName'] = item.property;
				delete item.property;
			}
		});
		return this.apiActionModel.agentMethod.methodParams;
	}

	/**
	 * Keep Data Signature Clean even when there are so many values incoming
	 * @param property
	 * @param value
	 */
	private modifySignatureByProperty(property: any): void {
		if (this.dataSignature) {
			this.dataSignature = ObjectUtils.modifySignatureByProperty(this.dataSignature, property, this.apiActionModel[property]);
		}
	}

	/**
	 * Returns true if script form valid?
	 */
	private isScriptFormValid(): boolean {
		return this.scriptForm && this.scriptForm.valid
			&& this.apiActionModel.remoteCredentialMethod
			&& this.apiActionModel.remoteCredentialMethod.id !== 0;
	}
}
