// Angular
import {Component, Inject, Input, OnInit} from '@angular/core';
// Service
import {HeaderService} from '../../services/header.service';
import {PermissionService} from '../../../../services/permission.service';
import {TranslatePipe} from '../../../../pipes/translate.pipe';
// Model
import {PersonModel} from '../../../../components/add-person/model/person.model';
import {DecoratorOptions} from '../../../../model/ui-modal-decorator.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import {Permission} from '../../../../model/permission.model';

@Component({
	selector: 'user-manage-staff',
	templateUrl: 'user-manage-staff.component.html'
})
export class UserManageStaffComponent extends Dialog implements OnInit {
	@Input() data: any;

	public modalOptions: DecoratorOptions;
	public editing;
	public currentTab;
	public availableTeamNames;
	public salaryOptions;
	public activeOptions;
	public canEditPerson;
	public defaultImageURL = '../images/blankPerson.jpg'
	public currentImageURL;
	private teamKeys;
	private savedPersonModel;
	public personId;
	public personModel: PersonModel;
	constructor(
		private headerService: HeaderService,
		private permissionService: PermissionService,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe) {
		super();
	}

	ngOnInit(): void {
		this.personId = this.data.personId;
		this.personModel = Object.assign({}, this.data.personModel);

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			show: () => true,
			disabled: () => !this.canEditPerson,
			active: () => this.editing,
			type: DialogButtonType.ACTION,
			action: this.changeToEdit.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => this.editing && this.canEditPerson,
			disabled: () => false,
			type: DialogButtonType.ACTION,
			action: this.submitInfo.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			show: () => !this.editing,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => this.editing,
			type: DialogButtonType.ACTION,
			action: this.cancelEditDialog.bind(this)
		});

		this.modalOptions = {isResizable: true, isDraggable: false, isCentered: true};
		this.salaryOptions = ['Contractor', 'Hourly', 'Salary'];
		this.activeOptions = ['Y', 'N'];
		this.loadComponentModel();
		this.canEditPerson = this.permissionService.hasPermission('PersonEdit');
		this.editing = false;
		this.teamKeys = {};
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return JSON.stringify(this.savedPersonModel) !== JSON.stringify(this.personModel);
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
	 * Cancel on Edit
	 */
	public cancelEditDialog() {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM ) {
						this.personModel = Object.assign({}, this.savedPersonModel);
						this.editing = false;
					}
				});
		} else {
			this.personModel = Object.assign({}, this.savedPersonModel);
			this.editing = false;
		}
	}

	// Populate the data for the model
	private loadComponentModel() {
		this.headerService.fetchModelForStaffViewEdit(this.personId).subscribe(
			(result: any) => {
				const defaultPerson = {
					firstName: '',
					middleName: '',
					lastName: '',
					nickName: '',
					title: '',
					email: '',
					department: '',
					location: '',
					workPhone: '',
					mobilePhone: '',
					active: 'Y',
					company: '',
					country: '',
					stateProv: '',
					keyWords: '',
					tdsNote: '',
					tdsLink: '',
					travelOK: 0,
					teams: [],
					staffType: [],
					personImageURL: ''
				};

				this.personModel = Object.assign({}, defaultPerson, this.personModel);
				let currentPersonInfo = result.person;
				let personModel = this.personModel;
				// Fill the model based on the current person.
				Object.keys(currentPersonInfo).forEach(function (key) {
					if (key in personModel) {
						personModel[key] = currentPersonInfo[key];
					}
				});
				this.savedPersonModel = Object.assign({}, this.personModel, this.savedPersonModel);
				this.availableTeamNames = result.availableTeams.map(a => a.description);
				this.currentImageURL = this.personModel.personImageURL;
				// Populate the key map so we can reference which ids apply to which descriptions
				for (let i = 0; i < result.availableTeams.length; i++) {
					this.teamKeys[result.availableTeams[i].description] = result.availableTeams[i].id;
				}
			},
			(err) => console.log(err));
	}

	// Add entry to the list of teams
	public addTeam() {
		this.personModel.teams.push({description: this.availableTeamNames[0], id: this.teamKeys[this.availableTeamNames[0]]});
	}

	// Apply changes to team entry based on description change
	public updateTeamEntry(team, e) {
		team.id = this.teamKeys[e];
	}

	// Remove entry from the list of teams
	public removeTeam(description) {
		for (let i = 0; i < this.personModel.teams.length; i++) {
			if (this.personModel.teams[i]['description'] === description) {
				this.personModel.teams.splice(i, 1);
			}
		}
	}

	// Save changes
	public submitInfo() {
		let data = Object.assign({}, this.personModel);

		// Convert travelOK into integer format from boolean
		data['travelOK'] = data['travelOK'] ? 1 : 0;
		// Add Id to the model
		data['id'] = this.personId;
		// Remove info that shouldn't be saved
		delete data['company'];

		// Filter out teams with duplicate ids
		let person = this.personModel;
		person.teams = person.teams.filter(function(team, index) {
			return person.teams.map(a => a.id).indexOf(team.id) === index;
		});

		// Isolate just team codes
		data['teams'] = this.personModel.teams.map(a => a.id);

		this.headerService.updateAccountAdmin(data).subscribe(
			(result) => {
				if (result) {
					this.savedPersonModel = Object.assign({}, this.personModel);
					this.editing = false;
				}
			});
	}

	/**
	 * Change to Edit mode if the use has the permission
	 */
	public changeToEdit(): void {
		if (this.canEditPerson && !this.editing) {
			this.editing = true;
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
