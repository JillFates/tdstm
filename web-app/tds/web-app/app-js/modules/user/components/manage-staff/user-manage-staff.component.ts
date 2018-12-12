import {Component} from '@angular/core';

import {UserService} from '../../service/user.service';
import {PersonModel} from '../../../../shared/components/add-person/model/person.model';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'user-manage-staff',
	templateUrl: '../tds/web-app/app-js/modules/user/components/manage-staff/user-manage-staff.component.html'
})
export class UserManageStaffComponent {
	public editing;
	public currentTab;
	public availableTeamNames;
	private teamKeys;
	private currentPersonId;
	private savedPersonModel;

	constructor(
		public personModel: PersonModel,
		private userService: UserService,
		private promptService: UIPromptService,
		public activeDialog: UIActiveDialogService) {
		this.loadComponentModel();
		this.editing = false;
		this.teamKeys = {};
	}

	// Decide whether or not to launch the confirmation dialogue before closing
	protected cancelCloseDialog(): void {
		if (JSON.stringify(this.savedPersonModel) !== JSON.stringify(this.personModel)) {
			this.promptService.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel')
				.then(confirm => {
					if (confirm) {
						this.activeDialog.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.activeDialog.dismiss();
		}
	}

	// Decide what to do when the cancel button is clicked
	protected handleCancelButton() {
		if(this.editing) {
			this.personModel =  Object.assign({}, this.savedPersonModel);
			this.editing = false;
		}
		else {
			this.cancelCloseDialog();
		}
	}

	// Populate the data for the model
	private loadComponentModel() {
		this.userService.fetchModelForStaffViewEdit().subscribe(
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
				this.currentPersonId = result.person.id;
				this.availableTeamNames = result.availableTeams.map(a => a.description);
				//Populate the key map so we can reference which ids apply to which descriptions
				for(let i = 0; i < result.availableTeams.length; i++)
				{
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
			data['id'] = this.currentPersonId;
			// Remove info that shouldn't be saved
			delete data['company'];

			// Filter out teams with duplicate ids
			let person = this.personModel;
			person.teams = person.teams.filter(function(team, index) {
				return person.teams.map(a => a.id).indexOf(team.id) === index;
			})

			// Isolate just team codes
			data['teams'] = this.personModel.teams.map(a => a.id);

			this.userService.updateAccountAdmin(data).subscribe(
				(result) => {
					if(result) {
						this.savedPersonModel = Object.assign({}, this.personModel);
						this.editing = false;
					}
				});
		} else {
			this.editing = true;
		}
	}
}