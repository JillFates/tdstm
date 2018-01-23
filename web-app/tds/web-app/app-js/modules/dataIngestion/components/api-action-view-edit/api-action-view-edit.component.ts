import {Component, ViewChild, ViewChildren, HostListener, OnInit, QueryList} from '@angular/core';
import {DropDownListComponent} from '@progress/kendo-angular-dropdowns';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {
	APIActionModel,
	APIActionParameterColumnModel,
	APIActionParameterModel,
	EventReaction,
	EventReactionType
} from '../../model/api-action.model';
import {ProviderModel} from '../../model/provider.model';
import {DataIngestionService} from '../../service/data-ingestion.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {ActionType, COLUMN_MIN_WIDTH} from '../../../../shared/model/data-list-grid.model';
import {INTERVAL, INTERVALS, DATA_TYPES, KEYSTROKE} from '../../../../shared/model/constants';
import {AgentModel, AgentMethodModel, CredentialModel} from '../../model/agent.model';
import {DataScriptModel} from '../../model/data-script.model';
import {NgForm} from '@angular/forms';
import {process, State} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';
import {CustomDomainService} from '../../../fieldSettings/service/custom-domain.service';
import {ObjectUtils} from '../../../../shared/utils/object.utils';
import {SortUtils} from '../../../../shared/utils/sort.utils';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {CodeMirrorComponent} from '../../../../shared/modules/code-mirror/code-mirror.component';
import * as R from 'ramda';
import {Observable} from 'rxjs/Observable';

declare var jQuery: any;

@Component({
	selector: 'api-action-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/api-action-view-edit/api-action-view-edit.component.html',
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
	`]
})
export class APIActionViewEditComponent implements OnInit {

	// Forms
	@ViewChild('apiActionForm') apiActionForm: NgForm;
	@ViewChild('apiActionReactionForm') apiActionReactionForm: NgForm;

	@ViewChild('apiActionProvider', { read: DropDownListComponent }) apiActionProvider: DropDownListComponent;
	@ViewChild('apiActionAgent', { read: DropDownListComponent }) apiActionAgent: DropDownListComponent;
	@ViewChild('apiActionAgentMethod', { read: DropDownListComponent }) apiActionAgentMethod: DropDownListComponent;
	@ViewChild('apiActionCredential', { read: DropDownListComponent }) apiActionCredential: DropDownListComponent;

	@ViewChildren('codeMirror') public codeMirrorComponents: QueryList<CodeMirrorComponent>;
	public codeMirrorComponent: CodeMirrorComponent;

	public apiActionModel: APIActionModel;
	public providerList = new Array<ProviderModel>();
	public agentList = new Array<AgentModel>();
	public agentMethodList = new Array<AgentMethodModel>();
	public agentCredentialList = new Array<CredentialModel>();
	public datascriptList = new Array<DataScriptModel>();
	public providerDatascriptList = new Array<DataScriptModel>();
	public parameterList: GridDataResult;
	public apiActionParameterColumnModel = new APIActionParameterColumnModel();
	public modalTitle: string;
	public dataScriptMode = APIActionModel;
	public actionTypes = ActionType;
	private dataSignature: string;
	private intervals = INTERVALS;
	public interval = INTERVAL;
	public selectedInterval = {value: 0, interval: ''};
	public selectedLapsed = {value: 0, interval: ''};
	public selectedStalled = {value: 0, interval: ''};
	public dataTypes = DATA_TYPES;
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
			value: 'Logical Storage'
		}, {
			assetClass: 'TASK',
			value: 'Task'
		}, {
			assetClass: 'USER_DEFINED',
			value: 'User Defined'
		}
	];
	private currentTab = 0;
	public isEditing = false;
	private initFormLoad = true;
	private codeMirror = {
		mode: {
			name: 'groovy' // Looks like we lack of JS support for coloring
		},
		rows: 10,
		cols: 4
	};
	private state: State = {
		sort: [{
			dir: 'asc',
			field: 'name'
		}]
	};
	public validInfoForm = false;
	public invalidScriptSyntax = false;
	constructor(
		public originalModel: APIActionModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private dataIngestionService: DataIngestionService,
		private customDomainService: CustomDomainService,
		private dialogService: UIDialogService) {

		// Sub Objects are not being created, just copy
		this.apiActionModel = R.clone(this.originalModel);

		this.selectedInterval = R.clone(this.originalModel.polling.frequency);
		this.selectedLapsed = R.clone(this.originalModel.polling.lapsedAfter);
		this.selectedStalled = R.clone(this.originalModel.polling.stalledAfter);

		this.dataSignature = JSON.stringify(this.apiActionModel);

		this.getProviders();
		this.getAgents();
		this.getCredentials();
		this.getDataScripts();
		this.getParameters();
		this.getCommonFieldSpecs();
		this.modalTitle = (this.modalType === ActionType.CREATE) ? 'Create API Action' : (this.modalType === ActionType.EDIT ? 'API Action Edit' : 'API Action Detail');
	}

	ngOnInit(): void {
		this.prepareFormListener();
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
		this.dataIngestionService.getProviders().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.providerList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.provider = this.providerList[0];
					this.modifySignatureByProperty('provider');
				}
				this.providerList.push(...result);
			},
			(err) => console.log(err));
	}

	/**
	 * Get the list of possible Agents
	 */
	getAgents(): void {
		this.dataIngestionService.getAgents().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.agentList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.agentClass = this.agentList[0];
					this.modifySignatureByProperty('agentClass');
				}
				this.agentList.push(...result);
				if (this.apiActionModel.agentMethod && this.apiActionModel.agentMethod.name) {
					this.onAgentValueChange(this.apiActionModel.agentClass);
				} else {
					this.agentMethodList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.agentMethod = this.agentMethodList[0];
					this.modifySignatureByProperty('agentMethod');
				}
			},
			(err) => console.log(err));
	}

	/**
	 * Get the list of Credentials
	 */
	getCredentials(): void {
		this.dataIngestionService.getCredentials().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.agentCredentialList.push({ id: 0, name: 'Select...' });
					this.apiActionModel.credential = this.agentCredentialList[0];
					this.modifySignatureByProperty('credential');
				}
				this.agentCredentialList.push(...result);
			},
			(err) => console.log(err));
	}

	/**
	 * Get the list of DataScript
	 */
	getDataScripts(): void {
		this.dataIngestionService.getDataScripts().subscribe(
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
		this.dataIngestionService.getParameters().subscribe(
			(result: any) => {
				if (this.modalType === ActionType.CREATE) {
					this.parameterList = process([], this.state);
				}
				this.parameterList = process(result, this.state);
			},
			(err) => console.log(err));
	}

	/**
	 * Preload the list of Common Fields Specs
	 */
	getCommonFieldSpecs(): void {
		this.customDomainService.getCommonFieldSpecs().subscribe(
			(result: any) => {
				this.commonFieldSpecs = result;
			},
			(err) => console.log(err));
	}

	/**
	 * Create a new DataScript
	 */
	protected onSaveApiAction(): void {
		this.validateAllSyntax().subscribe(() => {
			if (!this.invalidScriptSyntax) {
				this.dataIngestionService.saveAPIAction(this.apiActionModel).subscribe(
					(result: any) => {
						this.activeDialog.close(result);
					},
					(err) => console.log(err));
			}
		});
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.apiActionModel);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	protected cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel').then(result => {
					if (result) {
						this.activeDialog.dismiss();
					}
				});
		} else {
			this.activeDialog.dismiss();
		}
	}

	/**
	 * Detect if the use has pressed the on Escape to close the dialog and popup if there are pending changes.
	 * @param {KeyboardEvent} event
	 */
	@HostListener('document:keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditApiAction(): void {
		this.modalType = this.actionTypes.EDIT;
	}

	/**
	 * Delete the selected Data Script
	 * @param dataItem
	 */
	protected onDeleteApiAction(): void {
		this.prompt.open('Confirmation Required', 'Do you want to proceed?', 'Yes', 'No')
			.then((res) => {
				if (res) {
					this.dataIngestionService.deleteAPIAction(this.apiActionModel.id).subscribe(
						(result) => {
							this.activeDialog.close(result);
						},
						(err) => console.log(err));
				}
			});
	}

	protected isCurrentTab(num: number): boolean {
		return this.currentTab === num;
	}

	protected setCurrentTab(num: number): void {
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
				(this.apiActionModel.agentMethod.id !== 0 && this.apiActionModel.agentClass.id !== 0 && this.apiActionModel.provider.id !== 0);

			if (this.apiActionModel.producesData) {
				this.validInfoForm = this.apiActionModel.defaultDataScript.id !== 0;
			}
			if (!this.validInfoForm && !this.initFormLoad) {
				for (let i in this.apiActionForm.controls) {
					if (this.apiActionForm.controls[i]) {
						this.apiActionForm.controls[i].markAsTouched();
					}
				}
			}
			this.initFormLoad = false;
		}
	}

	/**
	 * On a new Agent change
	 * @param value
	 */
	protected onAgentValueChange(agentModel: AgentModel): void {
		if (agentModel.id !== 0) {
			this.dataIngestionService.getActionMethodById(agentModel.id).subscribe(
				(result: any) => {
					this.agentMethodList = new Array<AgentMethodModel>();
					this.agentMethodList.push({id: 0, name: 'Select...'});

					if (this.apiActionModel.agentMethod) {
						this.apiActionModel.agentMethod = result.find((agent) => agent.name === this.apiActionModel.agentMethod.name);
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
			this.agentMethodList.push({id: 0, name: 'Select...'});
			this.apiActionModel.agentMethod = this.agentMethodList[0];
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
		// Call Credential API that does not exist yet...

		// Populate only the DataScripts that are related to the provider
		this.providerDatascriptList = new Array<DataScriptModel>();
		this.providerDatascriptList.push({id: 0, name: 'Select...'});
		this.providerDatascriptList = this.providerDatascriptList.concat(this.datascriptList.filter((dataScript) => (dataScript.provider) && dataScript.provider.id === providerModel.id));
		if (previousValue) {
			this.apiActionModel.defaultDataScript = this.providerDatascriptList.find((datascript) => datascript.id === this.apiActionModel.defaultDataScript.id);
			this.modifySignatureByProperty('defaultDataScript');
		} else {
			this.apiActionModel.defaultDataScript = this.providerDatascriptList[0];
			// Set Credential to default
			this.apiActionModel.credential = this.agentCredentialList[0];
		}
	}

	/**
	 * Dropdown opens in a global document context, this helps to expands the limits
	 */
	onOpenAgentMethod(): void {
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
	 *  Verify the current Event Reaction input is a valid code
	 * @param {EventReaction} eventReaction
	 */
	verifyCode(eventReaction: EventReaction): void {
		// to all validateCode on date ingestion service
		eventReaction.valid = false;
		eventReaction.error = 'Error at Line 3: Unknow variable burt!';
	}

	onEditParameters(): void {
		this.isEditing = true;
	}

	/**
	 * Add a new argument to the list of parameters and refresh the list.
	 */
	onAddParameter(): void {
		this.parameterList.data.push({
			id: 0,
			name: '',
			description: '',
			dataType: '',
			context: {
				value: '',
				assetClass: ''
			},
			field: '',
			currentFieldList: [],
			value: ''
		});
		this.refreshParametersList();
	}

	/**
	 * When the Context has change, we should load the list of params associate with the Asset Class,
	 * if the value is USER_DEFINED, the field will become a text input field
	 */
	onContextValueChange(dataItem: APIActionParameterModel): void {
		let fieldSpecs = this.commonFieldSpecs.find((spec) => {
			return spec.domain === dataItem.context.assetClass;
		});
		if (fieldSpecs) {
			dataItem.currentFieldList = fieldSpecs.fields;
		}
	}

	/**
	 * Delete from the paramaters the argument passed.
	 * @param dataItem
	 */
	onDelete(dataItem: APIActionParameterModel): void {
		let parameterIndex = this.parameterList.data.indexOf(dataItem);
		if (parameterIndex >= 0) {
			this.parameterList.data.splice(parameterIndex, 1);
			this.refreshParametersList();
		}
	}

	/**
	 * Execute the validation and return an Observable
	 * so we can attach this event to different validations
	 * @returns {Observable<any>}
	 */
	validateAllSyntax(): Observable<any> {
		return new Observable(observer => {
			let scripts = [];
			this.apiActionModel.eventReactions.forEach((eventReaction: EventReaction) => {
				eventReaction.valid = true;
				eventReaction.error = '';
				if (eventReaction.value !== '') {
					scripts.push({code: eventReaction.type, script: eventReaction.value});
				}
			});
			this.dataIngestionService.validateCode(scripts).subscribe(
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
							eventReaction.valid = false;
							this.invalidScriptSyntax = true;
						}
					});
					observer.next();
				},
				(err) => console.log(err));
		});
	}

	/**
	 * Execute the API to validated every Syntax Value.
	 */
	onCheckAllSyntax(): void {
		this.validateAllSyntax().subscribe();
	}

	/**
	 * Refresh the list of elements after update, create or delete parameters / arguments
	 */
	public refreshParametersList(): void {
		this.parameterList = process(this.parameterList.data, this.state);
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
}