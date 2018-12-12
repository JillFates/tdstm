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

	protected handleCancelButton() {
		if(this.editing) {
			this.personModel =  Object.assign({}, this.savedPersonModel);
			this.editing = false;
		}
		else {
			this.cancelCloseDialog();
		}
	}

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
				this.personModel = personModel;
				this.savedPersonModel = Object.assign({}, this.personModel, this.savedPersonModel);
				this.currentPersonId = result.person.id;
				this.availableTeamNames = result.availableTeams.map(a => a.description);
				//Populate the key map
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

	public submitPreferences() {
		if (this.editing) {
			let preferences = Object.assign({}, this.personModel);
			preferences['travelOK'] = preferences['travelOK'] ? 1 : 0;
			preferences['id'] = this.currentPersonId;

			// Filter out duplicates
			let person = this.personModel;
			person.teams = person.teams.filter(function(team, index) {
				return person.teams.map(a => a.id).indexOf(team.id) === index;
			})

			// Isolate just team codes
			preferences['teams'] = this.personModel.teams.map(a => a.id);

			delete preferences['company'];
			this.userService.updateAccountAdmin(preferences).subscribe(
				(result: any) => {
					this.savedPersonModel =  Object.assign({}, this.personModel);
					this.editing = false;
				},
				(err) => {
					if (err) {
						console.log(err)
					}
				});
		} else {
			this.editing = true;
		}
	}
}