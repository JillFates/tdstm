import {Component} from '@angular/core';
import {UserService} from '../../service/user.service';
import {PersonModel} from '../../../../shared/components/add-person/model/person.model';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PasswordChangeModel} from '../../../../shared/components/password-change/model/password-change.model';

@Component({
	selector: 'user-preferences',
	templateUrl: '../tds/web-app/app-js/modules/user/components/view-edit/user-view-edit.component.html'
})

export class UserViewEditComponent {
	public currentPersonId;
	public startPageOptions;
	public editableUserPreferences;

	constructor(
		public personModel: PersonModel,
		public passwordChangeModel: PasswordChangeModel,
		private userService: UserService,
		public activeDialog: UIActiveDialogService) {
		this.startPageOptions = [];
		this.editableUserPreferences = [];
		this.retrieveStartPageOptions();
		this.loadComponentModel();
	}

	/**
	 * Close the Dialog
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();

	}

	private loadComponentModel() {
		this.userService.fetchComponentModel().subscribe(
			(result: any) => {
				const defaultPerson = { firstName: '',
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
					staffTypeId: '',
					company: '',
					companies: [],
					teams: [] ,
					staffType: [],
					selectedTeams: []
				};
				this.personModel = Object.assign({},  defaultPerson, this.personModel);

				// Get the preference list and get those that match the desired fields
				let preferenceList = result.preferences;
				let startPagePref = preferenceList.find(p => p.code === 'START_PAGE');
				let powerTypePref = preferenceList.find(p => p.code === 'CURR_POWER_TYPE');
				this.editableUserPreferences.START_PAGE = startPagePref ? startPagePref.value : 'Project Settings';
				this.editableUserPreferences.CURR_POWER_TYPE = powerTypePref ? powerTypePref.value : 'Watts';
				let currentPersonInfo = result.person;
				let personModel = this.personModel;
				// Fill the model based on the current person.
				Object.keys(currentPersonInfo).forEach(function(key) {
					if (key in personModel) {
						personModel[key] = currentPersonInfo[key];
					}
				});
				this.personModel = personModel;
				this.currentPersonId = result.person.id;
			},
			(err) => console.log(err));
	}

	private retrieveStartPageOptions() {
		this.userService.getStartPageOptions().subscribe(
			(result: any) => {
				this.startPageOptions = result.pages;
				console.log(this.startPageOptions);
			},
			(err) => console.log(err));
	}

	public submitPreferences() {
		let returnVal = true
		let preferences = {
			'id' : undefined,
			'firstName': this.personModel.firstName,
			'middleName': this.personModel.middleName,
			'lastName': this.personModel.lastName,
			'nickName': this.personModel.nickName,
			'title': this.personModel.title,
			'email': this.personModel.email,
			'oldPassword': this.passwordChangeModel.oldPassword,
			'confirmPassword': this.passwordChangeModel.confirmPassword,
			'newPassword': this.passwordChangeModel.newPassword,
			'startPage': this.editableUserPreferences.START_PAGE,
			'powerType': this.editableUserPreferences.CURR_POWER_TYPE
		}
		if (!preferences.firstName) {
			alert('First Name should not be blank ')
			returnVal = false
		} else if (this.passwordChangeModel.oldPassword + this.passwordChangeModel.newPassword != "") {
			if (!this.passwordChangeModel.oldPassword) {
				alert("Old Password should not be blank ")
				returnVal = false
			} else if (!this.passwordChangeModel.meetsCompositionRequirements || this.passwordChangeModel.containsUsername || !this.passwordChangeModel.atLeastMinimumLength) {
				alert("New Password does not meet all the requirements ")
				returnVal = false
			}
		}
		if (returnVal) {
			preferences.id = this.currentPersonId;
			this.userService.updateAccount(preferences).subscribe(
				(err) => console.log(err));
		}
	}
}