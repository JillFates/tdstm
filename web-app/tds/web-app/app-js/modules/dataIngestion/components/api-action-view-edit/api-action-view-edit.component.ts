import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {Subject} from 'rxjs/Subject';
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
import {INTERVAL, INTERVALS} from '../../../../shared/model/constants';
import {AgentModel, AgentMethodModel, CredentialModel} from '../../model/agent.model';
import {DataScriptModel} from '../../model/data-script.model';
import {NgForm} from '@angular/forms';
import {process, State} from '@progress/kendo-data-query';
import {GridDataResult} from '@progress/kendo-angular-grid';

declare var jQuery: any;

@Component({
	selector: 'api-action-view-edit',
	templateUrl: '../tds/web-app/app-js/modules/dataIngestion/components/api-action-view-edit/api-action-view-edit.component.html',
	styles: [`
        .has-error, .has-error:focus {
            border: 1px #f00 solid;
        }
	`]
})
export class APIActionViewEditComponent {

	// Forms
	@ViewChild('apiActionForm') apiActionForm: NgForm;
	@ViewChild('apiActionReactionForm') apiActionReactionForm: NgForm;

	@ViewChild('apiActionProvider', { read: DropDownListComponent }) apiActionProvider: DropDownListComponent;
	@ViewChild('apiActionAgent', { read: DropDownListComponent }) apiActionAgent: DropDownListComponent;
	@ViewChild('apiActionAgentMethod', { read: DropDownListComponent }) apiActionAgentMethod: DropDownListComponent;
	@ViewChild('apiActionCredential', { read: DropDownListComponent }) apiActionCredential: DropDownListComponent;
	public apiActionModel: APIActionModel;
	public providerList = new Array<ProviderModel>();
	public agentList = new Array<AgentModel>();
	public agentMethodList = new Array<AgentMethodModel>();
	public agentCredentialList = new Array<CredentialModel>();
	public agentDatascriptList = new Array<DataScriptModel>();
	public parameterList: GridDataResult;
	public apiActionParameterColumnModel = new APIActionParameterColumnModel();
	public modalTitle: string;
	public dataScriptMode = APIActionModel;
	public actionTypes = ActionType;
	private dataSignature: string;
	private intervals = INTERVALS;
	public COLUMN_MIN_WIDTH = COLUMN_MIN_WIDTH;
	private currentTab = 0;
	public isEditing = false;
	private codeMirror = {
		mode: {
			name: 'javascript'
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

	constructor(
		public originalModel: APIActionModel,
		public modalType: ActionType,
		public promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private prompt: UIPromptService,
		private dataIngestionService: DataIngestionService,
		private dialogService: UIDialogService) {

		this.apiActionModel = Object.assign({}, this.originalModel);
		this.getProviders();
		this.getAgents();
		this.getCredentials();
		this.getDataScripts();
		this.getParameters();
		this.modalTitle = (this.modalType === ActionType.CREATE) ? 'Create API Action' : (this.modalType === ActionType.EDIT ? 'API Action Edit' : 'API Action Detail');
		this.dataSignature = JSON.stringify(this.apiActionModel);
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
				}
				this.providerList.push(...result);
				this.dataSignature = JSON.stringify(this.apiActionModel);

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
					this.agentList.push({ id: '', name: 'Select...' });
					this.apiActionModel.agentClass = this.agentList[0];
				}
				this.agentList.push(...result);
				if (this.apiActionModel.agentMethod && this.apiActionModel.agentMethod.name) {
					this.onAgentValueChange(this.apiActionModel.agentMethod);
				} else {
					this.agentMethodList.push({ id: '', name: 'Select...' });
					this.apiActionModel.agentMethod = this.agentMethodList[0];
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
					this.agentCredentialList.push({ id: '', name: 'Select...' });
					this.apiActionModel.credential = this.agentCredentialList[0];
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
				if (this.modalType === ActionType.CREATE) {
					this.agentDatascriptList.push({ id: null, name: 'Select...' });
					this.apiActionModel.defaultDataScript = this.agentCredentialList[0];
				}
				this.agentDatascriptList.push(...result);
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
	 * Create a new DataScript
	 */
	protected onSaveApiAction(): void {
		this.dataIngestionService.saveAPIAction(this.apiActionModel).subscribe(
			(result: any) => {
				this.activeDialog.close(result);
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
		this.currentTab = num;
	}

	/**
	 * On a new Agent change
	 * @param value
	 */
	onAgentValueChange(agentModel: AgentModel): void {
		this.dataIngestionService.getActionMethodById(agentModel.id).subscribe(
			(result: any) => {
				this.agentMethodList = new Array<AgentMethodModel>();
				if (this.apiActionModel.agentMethod && this.apiActionModel.agentMethod.id) {
					this.apiActionModel.agentMethod = result.find((agent) => agent.name === this.apiActionModel.agentMethod.id);
				} else {
					this.agentMethodList.push({ id: '', name: 'Select...' });
					this.apiActionModel.agentMethod = this.agentMethodList[0];
				}
				this.agentMethodList = result;
				this.dataSignature = JSON.stringify(this.apiActionModel);
			},
			(err) => console.log(err));
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
		let events = [EventReactionType.SUCCESS, EventReactionType.DEFAULT, EventReactionType.ERROR, EventReactionType.TIMEDOUT, EventReactionType.LAPSED, EventReactionType.STALLED];

		let eventRectionItem = this.apiActionModel.eventReactions.find((eventReaction) => {
			let eventItem = events.find((event) => {
				return eventReaction.type === event;
			});
			return eventItem !== undefined && eventReaction.selected;
		});
		return eventRectionItem !== undefined;
	}

	/**
	 * Show only the Customize Label if one Custm is selected
	 */
	showsCustomizeLabel(): boolean {
		let events = [EventReactionType.PRE_API_CALL, EventReactionType.FINALIZED_API_CALL];

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

	onAddParameter(): void {
		this.parameterList.data.push({
			id: 0,
			name: '',
			description: '',
			dataType: '',
			context: '',
			value: ''
		});
		this.refreshParametersList();
	}

	public refreshParametersList(): void {
		this.parameterList = process(this.parameterList.data, this.state);
	}
}