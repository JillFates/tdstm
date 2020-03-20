// Angular
import {
	Component,
	ElementRef,
	HostListener, Input,
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
	APIActionParameterColumnModel,
	APIActionParameterModel,
	APIActionType,
	EventReaction,
	Languages
} from '../../model/api-action.model';
import {ProviderModel} from '../../../provider/model/provider.model';
import {Permission} from '../../../../shared/model/permission.model';
import {ActionType, COLUMN_MIN_WIDTH} from '../../../../shared/model/data-list-grid.model';
import {INTERVAL, INTERVALS, KEYSTROKE} from '../../../../shared/model/constants';
import {AgentMethodModel, CredentialModel, DictionaryModel} from '../../model/agent.model';
import {DataScriptModel} from '../../../dataScript/model/data-script.model';
import {CHECK_ACTION} from '../../../../shared/components/check-action/model/check-action.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
// Component
import {ApiActionViewEditReactionsComponent} from './api-action-view-edit-reactions.component';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
// Service
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {APIActionService} from '../../service/api-action.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
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
	@ViewChild('apiActionForm', {static: false}) apiActionForm: NgForm;
	@ViewChild('scriptForm', {static: false}) scriptForm: NgForm;
	@ViewChild('simpleInfoForm', {static: false}) simpleInfoForm: NgForm;
	@ViewChild('httpAPIForm', {static: false}) httpAPIForm: NgForm;
	@ViewChild('apiActionParametersForm', {static: false}) apiActionParametersForm: NgForm;
	@ViewChild('apiActionReactionForm', {static: false}) apiActionReactionForm: NgForm;
	@ViewChildren('codeMirror') public codeMirrorComponents: QueryList<CodeMirrorComponent>;
	@ViewChild('apiActionContainer', {static: false}) apiActionContainer: ElementRef;
	@ViewChild('apiActionViewEditReactionsComponent', {static: false}) apiActionViewEditReactionsComponent: ApiActionViewEditReactionsComponent;
	public codeMirrorComponent: CodeMirrorComponent;
	protected tabsEnum = NavigationTab;
	private WEB_API = 'WEB_API';
	public apiActionModel: APIActionModel;
	public providerList = new Array<ProviderModel>();
	public dictionaryList = new Array<DictionaryModel>();
	public agentMethodList = new Array<AgentMethodModel>();
	protected httpMethodList = new Array<any>();
	public agentCredentialList = new Array<CredentialModel>();
	public providerCredentialList = new Array<CredentialModel>();
	public datascriptList = new Array<DataScriptModel>();
	public providerDatascriptList = new Array<DataScriptModel>();
	public parameterList: Array<any>;
	public apiActionParameterColumnModel = new APIActionParameterColumnModel();
	public editModeFromView = false;
	public dataScriptMode = APIActionModel;
	public actionTypes = ActionType;
	private dataSignature: string;
	private dataParameterListSignature: string;
	private requiredFields = ['name', 'provider', 'actionType'];
	private intervals = INTERVALS;
	public interval = INTERVAL;
	public selectedInterval = {value: 0, interval: ''};
	public selectedLapsed = {value: 0, interval: ''};
	public selectedStalled = {value: 0, interval: ''};
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public PLEASE_SELECT = null;
	public commonFieldSpecs;
	protected actionTypesList = [];
	protected remoteCredentials = [];
	protected defaultItem = {id: '', value: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')};
	public SUPPLIED_CREDENTIAL = 'SUPPLIED';
	public modalType = ActionType.VIEW;
	public assetClassesForParameters = [
		{
			assetClass: 'COMMON',
			value: 'Asset'
		}, {
			assetClass: 'APPLICATION',
			value: 'Application'
		}, {
			assetClass: 'DATABASE',
			value: 'Database'
		}, {
			assetClass: 'DEVICE',
			value: 'Device'
		}, {
			assetClass: 'STORAGE',
			value: 'Storage'
		}, {
			assetClass: 'TASK',
			value: 'Task'
		}, {
			assetClass: 'USER_DEF',
			value: 'User Defined'
		}
	];
	private currentTab: NavigationTab = NavigationTab.Info;
	private initFormLoad = true;
	private codeMirror = {
		mode: 'Groovy',
		rows: 10,
		cols: 4
	};
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
	private savedApiAction = false;
	private defaultDictionaryModel = {name: '', id: 0};
	protected EnumAPIActionType = APIActionType;
	protected formValidStates = {
		simpleInfoForm: {isConfiguredValidators: false},
		httpAPIForm: {isConfiguredValidators: false},
		scriptForm: {isConfiguredValidators: false},
	};
	protected hasEarlyAccessTMRPermission = false;
	loadingLists = true;
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	isApiActionParametersFormValid = true;

	constructor(
		public dialogService: DialogService,
		public permissionService: PermissionService,
		private apiActionService: APIActionService,
		private customDomainService: CustomDomainService,
		private translatePipe: TranslatePipe) {
		super();
	}

	ngOnInit(): void {
		this.hasEarlyAccessTMRPermission = this.permissionService.hasPermission(Permission.EarlyAccessTMR);
		this.apiActionModel = R.clone(this.data.apiActionModel);
		this.modalType = this.data.actionType;

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			active: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.changeToEditApiAction.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.CREATE,
			disabled: () => !this.canSave(),
			type: DialogButtonType.ACTION,
			action: this.onSaveApiAction.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			show: () => this.modalType !== this.actionTypes.CREATE,
			type: DialogButtonType.ACTION,
			action: this.onDeleteApiAction.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => this.modalType === this.actionTypes.VIEW || this.modalType === this.actionTypes.CREATE,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
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

		this.dataParameterListSignature = '';
		this.parameterList = [];
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

		setTimeout(() => {
			this.setTitle(this.getModalTitle(this.modalType));
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
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(modalType: ActionType): string {
		// Every time we change the title, it means we switched to View, Edit or Create
		setTimeout(() => {
			// This ensure the UI has loaded since Kendo can change the signature of an object
			this.dataSignature = JSON.stringify(this.apiActionModel);
		}, 800);

		let modalTitle = '';
		if (modalType === ActionType.CREATE) {
			modalTitle = 'Action Create';
		} else {
			modalTitle = modalType === ActionType.EDIT ? 'Action Edit' : 'Action Detail';
		}
		return modalTitle + ((this.apiActionModel && this.apiActionModel.name) ? ' ' + this.apiActionModel.name : '');
	}

	/**
	 * The NgIf hides elements completely in the UI but makes transition of tabs more smooth
	 * Some complex component like Grid or Code Source got affected by this
	 * this method subscribe the listener to have control of each tab validation.
	 */
	protected prepareFormListener(): void {
		setTimeout(() => {
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
			this.verifyIsValidForm();
			this.dataSignature = JSON.stringify(this.apiActionModel);
		}, 0);
	}

	/**
	 * Get the List of Providers
	 */
	getProviders(result): void {
		if (this.modalType === ActionType.CREATE) {
			this.providerList.push({id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')});
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
			this.dictionaryList.push({id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')});
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
			this.actionTypesList = [];
			const keys = Object.keys(result.data.actionTypes);
			keys.forEach((key: string) => {
				this.actionTypesList.push({id: key, name: result.data.actionTypes[key]});
			});
			if (this.apiActionModel.tabActionType === APIActionType.HTTP_API) {
				this.apiActionModel.actionType = {id: this.WEB_API};
			}
		}
		if (result && result.data.remoteCredentialMethods) {
			this.remoteCredentials = [];
			const keys = Object.keys(result.data.remoteCredentialMethods);
			keys.forEach((key: string) => {
				this.remoteCredentials.push({id: key, value: result.data.remoteCredentialMethods[key]});
			});
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
					this.loadingLists = false;
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
				}, 200);
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
	 * Create a new DataScript
	 */
	protected onSaveApiAction(): void {
		if (this.canSave()) {
			this.apiActionService.saveAPIAction(this.apiActionModel, this.parameterList).subscribe(
				(result: any) => {
					if (result) {
						super.onAcceptSuccess(result);
					}
				},
				(err) => console.log(err));
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
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	public isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.apiActionModel);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	public isParameterListDirty(): boolean {
		if (this.dataParameterListSignature !== '' && this.parameterList.length > 0) {
			return this.dataParameterListSignature !== JSON.stringify(this.parameterList);
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
		if ((this.isDirty() || this.isParameterListDirty()) && this.modalType !== this.actionTypes.VIEW) {
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
			this.dialogService.activatedDropdown.subscribe(res => {
				if (res) {
					this.dialogService.activatedDropdown.next(false);
					return;
				}
				else {
					super.onCancelClose();
				}
			});

		}
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
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
		// set validators for current tab
		// ------------------
		if (tab === NavigationTab.HttpAPI) {
			if (!this.formValidStates.httpAPIForm.isConfiguredValidators) {
				setTimeout(() => {
					if (this.httpAPIForm) {
						this.formValidStates.httpAPIForm.isConfiguredValidators = true;
					}
				}, 1000);
			}
		}
		if (tab === NavigationTab.Script) {
			this.disableCodeMirrors();
			if (!this.formValidStates.scriptForm.isConfiguredValidators) {
				setTimeout(() => {
					if (this.scriptForm) {
						this.formValidStates.scriptForm.isConfiguredValidators = true;
					}
				}, 1000);
			}
		}
		this.editModeFromView = false;
		if (this.currentTab === 0) {
			this.verifyIsValidForm();
		}
		if (tab === NavigationTab.Info) {
			this.prepareFormListener();
		}
		if (tab === NavigationTab.Reactions) {
			this.disableCodeMirrors();
		}
		this.currentTab = tab;
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
			this.verifyIsValidForm();
			// Populate Reaction Scripts if present
			this.populateReactionScripts();
			// Populate HttpMethod if present
			this.populateHttpMethod();
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
		this.providerCredentialList.push({id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')});
		this.providerCredentialList = this.providerCredentialList.concat(this.agentCredentialList.filter((credential) => (credential.provider) && credential.provider.id === providerModel.id));
		// Populate only the DataScripts that are related to the provider
		this.providerDatascriptList = new Array<DataScriptModel>();
		this.providerDatascriptList.push({id: 0, name: this.translatePipe.transform('GLOBAL.SELECT_PLACEHOLDER')});
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
	 * Verify if this is on View mode
	 * @returns {boolean}
	 */
	isViewMode(): boolean {
		return this.modalType === this.actionTypes.VIEW;
	}

	canSave(): boolean {
		const actionTypeId = R.pathOr(null, ['actionType', 'id'], this.apiActionModel);
		if (this.hasEarlyAccessTMRPermission && actionTypeId === this.WEB_API) {
			return (
				(this.simpleInfoForm && this.simpleInfoForm.valid) &&
				(this.httpAPIForm && this.httpAPIForm.valid) &&
				this.validParametersForm && this.validateRequiredFields(this.apiActionModel)
			);
		}
		if (this.hasEarlyAccessTMRPermission && actionTypeId !== null && actionTypeId !== this.WEB_API) {
			return (
				(this.simpleInfoForm && this.simpleInfoForm.valid) &&
				(this.scriptForm && this.scriptForm.valid) &&
				this.validParametersForm && this.validateRequiredFields(this.apiActionModel)
			);
		}
		return (this.apiActionForm && this.apiActionForm.valid && this.validParametersForm && this.validateRequiredFields(this.apiActionModel));
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
}
