import {Component, Inject} from '@angular/core';

import {HeaderService} from '../../services/header.service';
import {PersonModel} from '../../../../components/add-person/model/person.model';
import {UIPromptService} from '../../../../directives/ui-prompt.directive';
import {UIActiveDialogService, UIExtraDialog} from '../../../../services/ui-dialog.service';
import {DecoratorOptions} from '../../../../model/ui-modal-decorator.model';
import {PermissionService} from '../../../../services/permission.service';

@Component({
	selector: 'user-manage-staff',
	templateUrl: 'user-manage-staff.component.html'
})
export class UserManageStaffComponent extends UIExtraDialog {
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

	constructor(
		public personModel: PersonModel,
		private headerService: HeaderService,
		private permissionService: PermissionService,
		private promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		@Inject('id') private id) {
		super('#user-manage-staff-component');
		this.modalOptions = { isResizable: true, isCentered: true };
		this.salaryOptions = ['Contractor', 'Hourly', 'Salary'];
		this.activeOptions = ['Y', 'N'];
		this.loadComponentModel();
		this.canEditPerson = this.permissionService.hasPermission('PersonEdit');
		this.editing = false;
		this.teamKeys = {};
	}

	// Decide whether or not to launch the confirmation dialogue before closing
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.savedPersonModel) !== JSON.stringify(this.personModel)) {
			this.promptService.open(
				'Abandon Changes?',
				'You have unsaved changes. Click Confirm to abandon your changes.',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.dismiss();
		}
	}

	// Decide what to do when the cancel button is clicked
	public handleCancelButton() {
		if (this.editing) {
			if (JSON.stringify(this.savedPersonModel) !== JSON.stringify(this.personModel)) {
				this.promptService.open(
					'Abandon Changes?',
					'You have unsaved changes. Click Confirm to abandon your changes.',
					'Confirm', 'Cancel')
					.then(confirm => {
						if (confirm) {
							this.personModel = Object.assign({}, this.savedPersonModel);
							this.editing = false;
						}
					})
					.catch((error) => console.log(error));
			} else {
				this.personModel = Object.assign({}, this.savedPersonModel);
				this.editing = false;
			}
		} else {
			this.cancelCloseDialog();
		}
	}

	// Populate the data for the model
	private loadComponentModel() {
		this.headerService.fetchModelForStaffViewEdit(this.id).subscribe(
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
		if (this.editing) {
			let data = Object.assign({}, this.personModel);

			// Convert travelOK into integer format from boolean
			data['travelOK'] = data['travelOK'] ? 1 : 0;
			// Add Id to the model
			data['id'] = this.id;
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
		} else {
			if (this.canEditPerson) {
				this.editing = true;
			}
		}
	}
}