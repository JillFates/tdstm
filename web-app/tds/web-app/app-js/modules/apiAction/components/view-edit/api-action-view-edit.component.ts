import {Component, ViewChild, ViewChildren, HostListener, OnInit, QueryList, ElementRef} from '@angular/core';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {
	APIActionModel,
	APIActionParameterColumnModel,
	APIActionParameterModel,
	EventReaction,
	EventReactionType,
	EVENT_BEFORE_CALL_TEXT
} from '../../model/api-action.model';
import {ProviderModel} from '../../../provider/model/provider.model';
import {APIActionService} from '../../service/api-action.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ActionType, COLUMN_MIN_WIDTH} from '../../../../shared/model/data-list-grid.model';
import {INTERVAL, INTERVALS, KEYSTROKE} from '../../../../shared/model/constants';
import {DictionaryModel, AgentMethodModel, CredentialModel} from '../../model/agent.model';
import {DataScriptModel} from '../../../dataScript/model/data-script.model';
import {NgForm} from '@angular/forms';
import {CustomDomainService} from '../../../fieldSettings/service/custom-domain.service';
import {ObjectUtils} from '../../../../shared/utils/object.utils';
import {SortUtils} from '../../../../shared/utils/sort.utils';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
import * as R from 'ramda';
import {Observable} from 'rxjs';
import {CHECK_ACTION} from '../../../../shared/components/check-action/model/check-action.model';

declare var jQuery: any;

@Component({
	selector: 'api-action-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/apiAction/components/view-edit/api-action-view-edit.component.html',
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
export class APIActionViewEditComponent implements OnInit {

	// Forms
	@ViewChild('apiActionForm') apiActionForm: NgForm;
	@ViewChild('apiActionParametersForm') apiActionParametersForm: NgForm;
	@ViewChild('apiActionReactionForm') apiActionReactionForm: NgForm;

	@ViewChildren('codeMirror') public codeMirrorComponents: QueryList<CodeMirrorComponent>;
	@ViewChild('apiActionContainer') apiActionContainer: ElementRef;

	public codeMirrorComponent: CodeMirrorComponent;

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
	public modalTitle: string;
	public editModeFromView = false;
	public dataScriptMode = APIActionModel;
	public actionTypes = ActionType;
	private dataSignature: string;
	private dataParameterListSignature: string;
	private intervals = INTERVALS;
	public eventBeforeCallText = EVENT_BEFORE_CALL_TEXT;
	public interval = INTERVAL;
	public selectedInterval = {value: 0, interval: ''};
	public selectedLapsed = {value: 0, interval: ''};
	public selectedStalled = {value: 0, interval: ''};
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	public commonFieldSpecs;
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
	private currentTab = 0;
	private initFormLoad = true;
	private codeMirror = {
		mode: {
			name: 'groovy' // Looks like we lack of JS support for coloring
		},
		rows: 10,
		cols: 4
	};
	public validInfoForm = false;
	public validParametersForm = true;
	public invalidScriptSyntax = false;
	public checkActionModel = CHECK_ACTION;
	private lastSelectedDictionaryModel: DictionaryModel = {
		id: 0,
		name: 'Select...'
	};
	private lastSelectedAgentMethodModel: AgentMethodModel = {
		id: '0',
		name: 'Select...'
	};
	private savedApiAction = false;
	private defaultDictionaryModel = { name: '', id: 0 };

	constructor(
		public originalModel: APIActionModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private apiActionService: APIActionService,
		private customDomainService: CustomDomainService) {
	}

	ngOnInit(): void {
		// Sub Objects are not being created, just copy
		this.apiActionModel = R.clone(this.originalModel);

		// set the default empty values for dictionary in case it is not defined
		if (!this.apiActionModel.dictionary) {
			this.apiActionModel.dictionary = this.defaultDictionaryModel;
		}

		this.selectedInterval = R.clone(this.originalModel.polling.frequency);
		this.selectedLapsed = R.clone(this.originalModel.polling.lapsedAfter);
		this.selectedStalled = R.clone(this.originalModel.polling.stalledAfter);

		this.dataSignature = JSON.stringify(this.apiActionModel);
		this.dataParameterListSignature = '';
		this.parameterList = [];

		this.getProviders();
		this.getAgents();
		this.getCommonFieldSpecs();
		this.getModalTitle();

		this.prepareFormListener();
	}

	private getModalTitle(): void {
		this.modalTitle = (this.modalType === ActionType.CREATE) ? 'Create API Action' : (this.modalType === ActionType.EDIT ? 'API Action Edit' : 'API Action Detail');
	}

	/**
	 * The NgIf hides elements completely in the UI but makes transition of tabs more smooth
	 * Some complex component like Grid or Code Source got affected by this
	 * this method subscribe the listener to have control of each tab validation.
	 */
	protected prepareFormListener(): void {
		setTimeout(() => {
			this.apiActionForm.valueChanges.subscribe(val => {
				this.verifyIsValidForm();
			});
			this.verifyIsValidForm();
		}, 100);
	}

	/**
	 * Get the List of Providers
	 */
	getProviders(): void {
		this.apiActionService.getProviders().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.providerList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.provider = this.providerList[0];
					this.modifySignatureByProperty('provider');
				}
				this.providerList.push(...result);

				this.getCredentials();
			},
			(err) => console.log(err));
	}

	/**
	 * Get the list of possible Agents
	 */
	getAgents(): void {
		this.apiActionService.getAPIActionEnums().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.dictionaryList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.dictionary = this.dictionaryList[0];
					this.modifySignatureByProperty('dictionary');
				}
				this.dictionaryList.push(...result.data.agentNames);
				if (this.apiActionModel.agentMethod && this.apiActionModel.agentMethod.id) {
					this.onDictionaryValueChange(this.apiActionModel.dictionary);
				} else {
					this.agentMethodList.push({ id: '0', name: 'Select...' });
					this.apiActionModel.agentMethod = this.agentMethodList[0];
					this.modifySignatureByProperty('agentMethod');
				}
				this.httpMethodList.push('Select...');
				this.httpMethodList.push(...result.data.httpMethod);
				if (!this.apiActionModel.httpMethod) {
					this.apiActionModel.httpMethod = this.httpMethodList[0];
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Get the list of Credentials
	 */
	getCredentials(): void {
		this.apiActionService.getCredentials().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE || !this.apiActionModel.credential) {
					this.agentCredentialList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.credential = this.agentCredentialList[0];
					this.modifySignatureByProperty('credential');
				}
				this.agentCredentialList.push(...result);
				this.getDataScripts();
			},
			(err) => console.log(err));
	}

	/**
	 * Get the list of DataScript
	 */
	getDataScripts(): void {
		this.apiActionService.getDataScripts().subscribe(
			(result: any) => {
				result = result.sort((a, b) => SortUtils.compareByProperty(a, b, 'name'));
				if (this.modalType === ActionType.CREATE) {
					this.datascriptList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.defaultDataScript = this.datascriptList[0];
					this.modifySignatureByProperty('defaultDataScript');
				}
				this.datascriptList.push(...result);
				if (this.apiActionModel.provider && this.apiActionModel.provider.id !== 0) {
					this.onProviderValueChange(this.apiActionModel.provider, true);
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
				}, 200);
			},
			(err) => console.log(err));
	}

	/**
	 * Preload the list of Common Fields Specs
	 */
	getCommonFieldSpecs(): void {
		this.customDomainService.getCommonFieldSpecsWithShared().subscribe(
			(result: any) => {
				this.commonFieldSpecs = result;
				if (this.modalType !== ActionType.CREATE) {
					this.getParameters();
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Create a new DataScript
	 */
	protected onSaveApiAction(): void {
		this.apiActionService.saveAPIAction(this.apiActionModel, this.parameterList).subscribe(
			(result: any) => {
				if (result) {
					this.savedApiAction = true;
					this.modalType = this.actionTypes.VIEW;
					this.getModalTitle();
					this.apiActionModel.id = result.id;
					this.apiActionModel.version = result.version;
					this.dataSignature = JSON.stringify(this.apiActionModel);
					this.dataParameterListSignature = JSON.stringify(this.parameterList);
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.apiActionModel);
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isParameterListDirty(): boolean {
		if (this.dataParameterListSignature !== '' && this.parameterList.length > 0) {
			return this.dataParameterListSignature !== JSON.stringify(this.parameterList);
		}
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty() || this.isParameterListDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.activeDialog.close(this.savedApiAction ? this.apiActionModel : null);
					} else {
						this.focusForm();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.close(this.savedApiAction ? this.apiActionModel : null);
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
		this.getModalTitle();
		this.verifyIsValidForm();
		this.focusForm();
	}

	/**
	 * Delete the selected DataScript
	 * @param dataItem
	 */
	protected onDeleteApiAction(): void {
		this.prompt.open('Confirmation Required', 'Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.apiActionService.deleteAPIAction(this.apiActionModel.id).subscribe(
						(result) => {
							this.activeDialog.dismiss(result);
						},
						(err) => console.log(err));
				}
			});
	}

	protected isCurrentTab(num: number): boolean {
		return this.currentTab === num;
	}

	protected setCurrentTab(num: number): void {
		this.editModeFromView = false;
		if (this.currentTab === 0) {
			this.verifyIsValidForm();
		}
		if (num === 0) {
			this.prepareFormListener();
		}

		if (num === 2) {
			this.codeMirrorComponents.changes.subscribe((comps: QueryList<CodeMirrorComponent>) => {
				comps.forEach((child) => {
					this.codeMirrorComponent = child;
					this.codeMirrorComponent.setDisabled(this.modalType === ActionType.VIEW);
				});
			});
		}
		this.currentTab = num;
	}

	/**
	 *  Verify if the Form is on a Valid state when switching between tabs.
	 */
	protected verifyIsValidForm(): void {
		// Test API Action Form
		if (this.apiActionForm) {
			this.validInfoForm = this.apiActionForm.valid &&
				(this.apiActionModel.agentMethod.id !== '0'
					&& this.apiActionModel.dictionary.id !== 0
					&& this.apiActionModel.provider.id !== 0
					&& this.apiActionModel.httpMethod !== 'Select...');

			if (!this.validInfoForm && !this.initFormLoad) {
				for (let i in this.apiActionForm.controls) {
					if (this.apiActionForm.controls[i]) {
						this.apiActionForm.controls[i].markAsTouched();
					}
				}
			}
			this.initFormLoad = false;
		}
		if (this.editModeFromView) {
			this.validInfoForm = this.editModeFromView;
		}

		if (this.apiActionParametersForm) {
			this.validParametersForm = this.apiActionParametersForm.valid;
		}
	}

	/**
	 * On a new Dictionary selected
	 * @param value
	 */
	protected onDictionaryValueChange(dictionaryModel: DictionaryModel): void {
		dictionaryModel = dictionaryModel ?  dictionaryModel : this.defaultDictionaryModel;
		this.apiActionModel.dictionary = dictionaryModel;

		if (this.lastSelectedDictionaryModel && this.lastSelectedDictionaryModel.id !== 0) {
			this.prompt.open('Confirmation Required', 'Changing the Dictionary or Method will overwrite many of the settings of the Action. Are you certain that you want to proceed?', 'Yes', 'No')
				.then((res) => {
					this.loadDictionaryModel(dictionaryModel, res);
				});
		} else {
			this.loadDictionaryModel(dictionaryModel, true);
		}
	}

	private loadDictionaryModel(dictionaryModel: DictionaryModel, changeAgent: boolean): void {
		if (changeAgent) {
			if (dictionaryModel.id !== 0) {
				this.apiActionService.getActionMethodById(dictionaryModel.id).subscribe(
					(result: any) => {
						this.agentMethodList = new Array<AgentMethodModel>();
						this.agentMethodList.push({id: '0', name: 'Select...'});

						if (this.apiActionModel.agentMethod) {
							this.apiActionModel.agentMethod = result.find((agent) => agent.id === this.apiActionModel.agentMethod.id);
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
				this.agentMethodList.push({id: '0', name: 'Select...'});
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
	protected onMethodValueChange(event: any): void {
		if (this.lastSelectedAgentMethodModel && this.lastSelectedAgentMethodModel.id !== '0') {
			this.prompt.open('Confirmation Required', 'Changing the Dictionary or Method will overwrite many of the settings of the Action. Are you certain that you want to proceed?', 'Yes', 'No')
				.then((res) => {
					this.loadAgentMethodModel(res);
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
				return method.id === this.lastSelectedAgentMethodModel.id;
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
		APIActionModel.createBasicReactions(this.apiActionModel);
		for (let reactionType in methodScripts) {
			if (methodScripts[reactionType]) {
				let match = this.apiActionModel.eventReactions.find( item => item.type === reactionType);
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
		const match = this.httpMethodList.find( item => item === httpMethod);
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
		this.providerCredentialList.push({id: 0, name: 'Select...'});
		this.providerCredentialList = this.providerCredentialList.concat(this.agentCredentialList.filter((credential) => (credential.provider) && credential.provider.id === providerModel.id));

		// Populate only the DataScripts that are related to the provider
		this.providerDatascriptList = new Array<DataScriptModel>();
		this.providerDatascriptList.push({id: 0, name: 'Select...'});
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
		if (this.apiActionModel.agentMethod && this.apiActionModel.agentMethod.id !== '0') {
			this.lastSelectedAgentMethodModel = R.clone(this.apiActionModel.agentMethod);
		}
		setTimeout(() => {
			jQuery('kendo-popup').css('width', 'auto');
		}, 100);
	}

	/**
	 * Show only the Event Label if one Event is selected
	 */
	showsEventLabel(): boolean {
		let events = [EventReactionType.SUCCESS, EventReactionType.DEFAULT, EventReactionType.ERROR, EventReactionType.LAPSED, EventReactionType.STALLED];

		let eventRectionItem = this.apiActionModel.eventReactions.find((eventReaction) => {
			let eventItem = events.find((event) => {
				return eventReaction.type === event;
			});
			return eventItem !== undefined && eventReaction.selected;
		});
		return eventRectionItem !== undefined;
	}

	/**
	 * Show only the Customize Label if one Custom is selected
	 */
	showsCustomizeLabel(): boolean {
		let events = [EventReactionType.PRE, EventReactionType.FINAL];

		let eventRectionItem = this.apiActionModel.eventReactions.find((eventReaction) => {
			let eventItem = events.find((event) => {
				return eventReaction.type === event;
			});
			return eventItem !== undefined && eventReaction.selected;
		});
		return eventRectionItem !== undefined;
	}

	/**
	 * Close and Open the Panel for Code Mirror
	 * @param {EventReaction} eventReaction
	 */
	openCloseCodeMirror(eventReaction: EventReaction): void {
		eventReaction.open = !eventReaction.open;
	}

	/**
	 * Add a new argument to the list of parameters and refresh the list.
	 */
	onAddParameter(): void {
		this.parameterList.push({
			paramName: '',
			desc: '',
			type: 'string',
			context: '',
			fieldName: '',
			currentFieldList: [],
			value: '',
			readonly: false,
			required: false,
			encoded: false
		});
		this.verifyIsValidForm();
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
					return field.field === dataItem.fieldName;
				});
				if (property) {
					dataItem.fieldName = property;
				}
			}

			this.verifyIsValidForm();
		}
	}

	/**
	 * Make the Field from Context, filterable
	 * @param filter
	 */
	public filterChange(filter: any, dataItem: any): void {
		dataItem.currentFieldList = dataItem.sourceFieldList.filter((s) => s.label.toLowerCase().indexOf(filter.toLowerCase()) !== -1);
	}

	/**
	 * Delete from the paramaters the argument passed.
	 * @param dataItem
	 */
	onDeleteParameter(event: any, dataItem: APIActionParameterModel): void {
		let parameterIndex = this.parameterList.indexOf(dataItem);
		if (parameterIndex >= 0) {
			this.parameterList.splice(parameterIndex, 1);
			this.verifyIsValidForm();
		}
	}

	/**
	 * Execute the validation and return an Observable
	 * so we can attach this event to different validations
	 * @returns {Observable<any>}
	 */
	validateAllSyntax(singleEventReaction?: EventReaction): Observable<any> {
		return new Observable(observer => {
			let scripts = [];
			// Doing a single Event reaction Validation
			if (singleEventReaction) {
				if (singleEventReaction.value !== '') {
					scripts.push({code: singleEventReaction.type, script: singleEventReaction.value});
				}
			} else {
				this.apiActionModel.eventReactions.forEach((eventReaction: EventReaction) => {
					eventReaction.state = this.checkActionModel.UNKNOWN;
					eventReaction.error = '';
					if (eventReaction.value !== '') {
						scripts.push({code: eventReaction.type, script: eventReaction.value});
					}
				});
			}
			this.apiActionService.validateCode(scripts).subscribe(
				(result: any) => {
					this.invalidScriptSyntax = false;
					result.forEach((eventResult: any) => {
						let eventReaction = this.apiActionModel.eventReactions.find((r: EventReaction) => r.type === eventResult['code']);
						if (!eventResult['validSyntax']) {
							let errorResult = '';
							eventResult.errors.forEach((error: string) => {
								errorResult += error['message'] + '\n';
							});
							eventReaction.error = errorResult;
							eventReaction.state = this.checkActionModel.INVALID;
							this.invalidScriptSyntax = true;
						} else {
							eventReaction.state = this.checkActionModel.VALID;
						}
					});
					observer.next();
				},
				(err) => console.log(err));
		});
	}

	/**
	 *  Verify the current Event Reaction input is a valid code
	 * @param {EventReaction} eventReaction
	 */
	verifyCode(eventReaction: EventReaction): void {
		this.validateAllSyntax(eventReaction).subscribe();
	}

	/**
	 * Execute the API to validated every Syntax Value.
	 */
	onCheckAllSyntax(): void {
		this.validateAllSyntax().subscribe();
	}

	/**
	 * Keep Data Signature Clean even when there are so many values incoming
	 * @param property
	 * @param value
	 */
	private modifySignatureByProperty(property: any): void {
		this.dataSignature = ObjectUtils.modifySignatureByProperty(this.dataSignature, property, this.apiActionModel[property]);
	}

	/**
	 * Verify if this is on View mode
	 * @returns {boolean}
	 */
	isViewMode(): boolean {
		return this.modalType === this.actionTypes.VIEW;
	}

	/**
	 * Get the Label value to show on the UI like Application instead of APPLICATION
	 * @param context
	 * @returns {string}
	 */
	getAssetClassValue(context: any): string {
		let assetClass = this.assetClassesForParameters.find((param) => {
			return param.assetClass === context || param.assetClass === context.assetClass;
		});
		if (assetClass && assetClass.value) {
			return assetClass.value;
		}
		return context;
	};

	/**
	 * Workaround to stop propagation on shared events on Kendo
	 * Clicking on enter was causing other events to execute
	 * @param event
	 */
	public getOnInputKey(event: any): void {
		if (event.key === KEYSTROKE.ENTER) {
			event.preventDefault();
			event.stopPropagation();
		}
	}

	private focusForm() {
		this.apiActionContainer.nativeElement.focus();
	}

	protected isCheckSyntaxSectionDisabled(sectionIndex: number): boolean {
		const eventReaction: EventReaction = this.apiActionModel.eventReactions[sectionIndex];
		return eventReaction.value === '' || eventReaction.state === CHECK_ACTION.VALID;
	}

	public onEventReactionSelect(eventReaction: EventReaction): void {
		if (eventReaction.type === EventReactionType.PRE) {
			if (eventReaction.selected && eventReaction.value === '') {
				eventReaction.value = this.eventBeforeCallText;
			} else {
				eventReaction.value = '';
			}
		}
	}

}
