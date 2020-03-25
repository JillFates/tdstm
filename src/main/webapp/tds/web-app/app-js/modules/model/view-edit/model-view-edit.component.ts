import {
	Component,
	ComponentFactoryResolver, ElementRef,
	Input,
	OnInit,
	QueryList,
	Renderer2,
	ViewChild,
	ViewChildren
} from '@angular/core';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import {NgForm} from '@angular/forms';
import {ModelModel} from '../model/model.model';
import {ActionType} from '../../dataScript/model/data-script.model';
import {TranslatePipe} from '../../../shared/pipes/translate.pipe';
import {PermissionService} from '../../../shared/services/permission.service';
import {ModelService} from '../service/model.service';
import {Permission} from '../../../shared/model/permission.model';
import {Connector} from "../../../shared/components/connector/model/connector.model";
import {Aka, AkaChanges} from "../../../shared/components/aka/model/aka.model";

@Component({
	selector: 'model-view-edit',
	templateUrl: 'model-view-edit.component.html'
})

export class ModelViewEditComponent extends Dialog implements OnInit {
	@Input() data: any;
	@ViewChild('modelForm', {read: NgForm, static: true}) modelForm: NgForm;
	@ViewChild('modelConnectorTableBody', { static: false }) d1: ElementRef;

	public modelModel: ModelModel;
	public modalTitle: string;
	public actionTypes = ActionType;
	public modalType = ActionType.VIEW;
	public manufacturerList;
	public assetTypeList;
	public sourceTDS = false;
	public modelConnectors: Connector[] = [];
	public modelAkas: any[] = [];
	public powerType: string;
	public powerTypes = ['Amp', 'Watts'];
	public modelAkasDisplay: string;
	public manufacturerName: string;
	public usizeList: number[] = [];
	public userList: any[] = [];
	// AKA
	public aliasControls = [];
	public aliasDeleted = [];
	public aliasUpdated = [];
	public aliasAdded: Aka[] = [];
	public akaChanges: AkaChanges;
	public displayedAliasErrorSpans = [];
	public modelCreatedBy: string;
	public modelUpdatedBy: string;
	public modelValidatedBy: string;
	@ViewChildren('aliasSpan') aliasSpanElements: QueryList<any>;
	protected hasOnlyUniqueAlias = true;
	private modelControllerTypes = [ 'Ether', 'Serial', 'Power', 'Fiber', 'SCSI', 'USB', 'KVM', 'ILO', 'Management',
		'SAS',
		'Other'];
	private modelControllerLabelPositions = [ 'Right', 'Left', 'Top', 'Bottom'];
	private modelConnectorCount = 0;

	private dataSignature: string;
	protected isUnique = true;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private modelService: ModelService,
		private permissionService: PermissionService,
		private renderer: Renderer2
	) {
		super();
	}

	ngOnInit(): void {
		this.modelModel = Object.assign({}, this.data.modelModel);
		this.manufacturerList = this.data.manufacturerList;
		this.modalType = this.data.actionType;
		this.modalTitle = this.getModalTitle(this.modalType);

		if (this.modalType !== ActionType.CREATE) this.getModelDetails();
		if (this.modalType === ActionType.CREATE) this.preLoadData();

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.VIEW,
			disabled: () => !this.permissionService.hasPermission(Permission.ModelEdit),
			active: () => this.modalType === this.actionTypes.EDIT,
			type: DialogButtonType.ACTION,
			action: this.changeToEditModel.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.modalType === this.actionTypes.EDIT || this.modalType === this.actionTypes.CREATE,
			disabled: () => !this.modelForm.form.valid || !this.isUnique || this.isEmptyValue() || !this.modelForm.form.dirty,
			type: DialogButtonType.ACTION,
			action: this.onSaveModel.bind(this)
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

		setTimeout(() => {
			this.setTitle(this.getModalTitle(this.modalType));
		});
	}

	public cancelEditDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM && !this.data.openFromList) {
					// Put back original model
					this.modelModel = JSON.parse(this.dataSignature);
					this.dataSignature = JSON.stringify(this.modelModel);
					this.modalType = this.actionTypes.VIEW;
					this.setTitle(this.getModalTitle(this.modalType));
				} else if (result.confirm === DialogConfirmAction.CONFIRM && this.data.openFromList) {
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

	private preLoadData(): void {
		this.modelService.getPreData()
			.subscribe((response: any) => {
				this.assetTypeList = response.assetTypes;
				this.powerType = response.powerType;
				this.usizeList = response.usizeList;
			})
	}

	private getModelDetails(): void {
		this.modelService.getModelDetails(this.modelModel.id)
			.subscribe((response: any) => {
				console.log(`Model Detail response ${response}`);
				this.modelModel = response.data.modelInstance;
				this.modelConnectors = response.data.modelConnectors;
				this.modelAkas = response.data.modelAkas;
				this.modelAkasDisplay = response.data.modelAkas;
				this.sourceTDS = this.modelModel.sourceTDS === 1;
				this.manufacturerName = this.manufacturerList.find(m => m.id === this.modelModel.manufacturer['id']).name;
				this.usizeList = response.data.usizeList;
				this.assetTypeList = response.data.assetTypes;
				this.aliasControls = (this.modalType === this.actionTypes.EDIT) ? (this.modelAkas) ? this.modelAkas : [] : [];
				this.dataSignature = JSON.stringify(this.modelModel);
				this.userList = response.data.userList;
				this.modelCreatedBy = response.data.modelCreatedBy
			});
	}

	public addConnector(): void {
		this.modelConnectorCount++;
		const tr = this.renderer.createElement('tr');

		const tdSelect = this.renderer.createElement('td');
		const tdLabel = this.renderer.createElement('td');
		const tdLabelPosition = this.renderer.createElement('td');
		const tdPosX = this.renderer.createElement('td');
		const tdPosY = this.renderer.createElement('td');

		const selectTypeDiv = this.renderer.createElement('div');
		const selectType = this.renderer.createElement('select');
		const inputLabel = this.renderer.createElement('input');
		const selectLabelPositionDiv = this.renderer.createElement('div');
		const selectLabelPosition = this.renderer.createElement('select');
		const inputPosX = this.renderer.createElement('input');
		const inputPosY = this.renderer.createElement('input');

		this.renderer.addClass(selectTypeDiv, 'clr-select-wrapper');
		this.renderer.addClass(selectType, 'clr-select');
		this.renderer.addClass(selectLabelPositionDiv, 'clr-select-wrapper');
		this.renderer.addClass(selectLabelPosition, 'clr-select');
		this.renderer.addClass(inputLabel, 'clr-input');
		this.renderer.addClass(inputPosX, 'clr-input');
		this.renderer.addClass(inputPosY, 'clr-input');

		this.renderer.setAttribute(inputLabel, 'value', `Connector${this.modelConnectorCount}`);
		this.renderer.setAttribute(inputPosX, 'type', 'number');
		this.renderer.setAttribute(inputPosY, 'type', 'number');

		// Values
		this.modelControllerTypes.forEach(type => {
			const option = this.renderer.createElement('option');
			const text = this.renderer.createText(type);
			this.renderer.setProperty(option, 'value', type);
			this.renderer.appendChild(option, text);
			this.renderer.appendChild(selectType, option);
		});

		this.modelControllerLabelPositions.forEach(type => {
			const option = this.renderer.createElement('option');
			const text = this.renderer.createText(type);
			this.renderer.setProperty(option, 'value', type);
			this.renderer.appendChild(option, text);
			this.renderer.appendChild(selectLabelPosition, option);
		})

		this.renderer.appendChild(selectTypeDiv, selectType);
		this.renderer.appendChild(tr, tdSelect);
		this.renderer.appendChild(tdSelect, selectTypeDiv);

		this.renderer.appendChild(tr, tdLabel);
		this.renderer.appendChild(tdLabel, inputLabel);

		this.renderer.appendChild(selectLabelPositionDiv, selectLabelPosition);
		this.renderer.appendChild(tr, tdLabelPosition);
		this.renderer.appendChild(tdLabelPosition, selectLabelPositionDiv);

		this.renderer.appendChild(tr, tdPosX);
		this.renderer.appendChild(tdPosX, inputPosX);

		this.renderer.appendChild(tr, tdPosY);
		this.renderer.appendChild(tdPosY, inputPosY);

		this.renderer.appendChild(this.d1.nativeElement, tr);


		/*<tr id="connectorTr1" style="display: none;">
		<td><select id="typeId1" name="type">
		<option value="Ether">Ether</option>
			<option value="Serial">Serial</option>
			<option value="Power">Power</option>
			<option value="Fiber">Fiber</option>
			<option value="SCSI">SCSI</option>
			<option value="USB">USB</option>
			<option value="KVM">KVM</option>
			<option value="ILO">ILO</option>
			<option value="Management">Management</option>
			<option value="SAS">SAS</option>
			<option value="Other">Other</option>
			</select></td>
		<td><input id="labelId1" type="text" onchange="changeLabel(1, this.value)"></td>
		<td><select id="labelPositionId1" name="labelPosition" onchange="changeLabelPosition(1, this.value)">
		<option value="Right">Right</option>
			<option value="Left">Left</option>
			<option value="Top">Top</option>
			<option value="Bottom">Bottom</option>
			</select></td>
		<td><input id="connectorPosXId1" maxlength="3" style="width: 35px;" type="number" min="0" value="0"></td>
		<td>
		<input id="connectorId1" maxlength="5" style="width: 35px;" type="hidden" value="1">
		<input id="connectorPosYId1" maxlength="3" style="width: 35px;" type="number" min="0" value="360">
		<input id="statusId1" type="hidden">
			</td>
			</tr>*/
	}

	/**
	 * Add alias to collection
	 */
	public addAlias(): void {
		this.aliasControls.push('');
	}

	/**
	 * Remove alias from collection
	 */
	public removeAlias(index: number, itemId: number): void {
		this.modelForm.form.markAsDirty();
		this.aliasDeleted.push(itemId);
		this.aliasControls.splice(index, 1);

		this.aliasSpanElements.toArray().splice(index, 1);
		this.displayedAliasErrorSpans.splice(index, 1);
		this.hasOnlyUniqueAlias = (this.displayedAliasErrorSpans.length === 0);
	}

	/**
	 * Add alias value to collection
	 */
	public focusOutAlias(event: any, item: any, index: number): void {
		this.modelForm.form.markAsDirty();
		if (item) {
			if (event.target.value !== item.name) {
				this.aliasUpdated.push({id: item.id, name: event.target.value});
			} else {
				const arrayItem = this.aliasAdded.find(i => i === event.target.value);
				if (!arrayItem) {
					this.aliasAdded.push(event.target.value);
				}
			}
		} else {
			const alreadyAdded = this.aliasAdded.find(i => i.id === index);
			if (alreadyAdded) {
				this.aliasAdded[index].name = event.target.value;
			} else {
				this.aliasAdded.push({id: index, name: event.target.value});
			}
		}
	}

	/**
	 * Based on modalType action returns the corresponding title
	 * @param {ActionType} modalType
	 * @returns {string}
	 */
	private getModalTitle(modalType: ActionType): string {
		if (modalType === ActionType.CREATE) {
			return 'Model Create';
		}
		return modalType === ActionType.EDIT
			? 'Model Edit'
			: 'Model Detail';
	}

	/**
	 * Change the View Mode to Edit Mode
	 */
	protected changeToEditModel(): void {
		this.modalType = this.actionTypes.EDIT;
		this.setTitle(this.getModalTitle(this.modalType));
	}

	/**
	 * Create Edit a Manufacturer
	 */
	protected onSaveModel(): void {
		this.getModelConnectors();
		this.modelModel.connectorCount = this.modelConnectorCount;
		this.modelModel.modelConnectors = this.modelConnectors;
		this.modelModel.sourceTDS = (this.sourceTDS) ? 1 : 0;
		this.modelModel.akaChanges = { added: this.aliasAdded, deleted: this.aliasDeleted, edited: this.aliasUpdated };
		this.modelService.saveModel(this.modelModel)
			.subscribe(
			(result: any) => {
				this.onAcceptSuccess(result);
			},
			err => console.log(err)
		);
	}

	/**
	 * Verify if the Name is Empty
	 * @returns {boolean}
	 */
	protected isEmptyValue(): boolean {
		let term = '';
		if (this.modelModel.modelName) {
			term = this.modelModel.modelName.trim();
		}
		return term === '';
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.modelModel);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	private getModelConnectors(): void {
		const ele = this.d1.nativeElement;
		let connectorNumber = 0;
		for (let child of ele.children) {
			connectorNumber++;
			const connector = new Connector();
			connector.connector = connectorNumber;
			connector.status = 'missing';
			connector.type = child.children[0].children[0].children[0].value;
			connector.label = child.children[1].children[0].value;
			connector.labelPosition = child.children[2].children[0].children[0].value;
			connector.connectorPosX = child.children[3].children[0].value;
			connector.connectorPosY = child.children[4].children[0].value;

			this.modelConnectors.push(connector);
			/*for (let g of child.children) {
				if (g.children[0].value) {
					console.log('Inputs: ' + g.children[0].value);
				} else {
					if (g.children[0].children[0].value) {
						console.log('Selects: ' + g.children[0].children[0].value);
					}
				}
			}*/
		}
	}

	public onChangeManufacturer(event: any): void {
		this.modelModel.manufacturer = event.target.value;
	}
}