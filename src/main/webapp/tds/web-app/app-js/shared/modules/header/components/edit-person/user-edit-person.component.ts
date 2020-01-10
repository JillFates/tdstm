import {Component} from '@angular/core';
import {HeaderService} from '../../services/header.service';
import {PersonModel} from '../../../../components/add-person/model/person.model';
import {UIActiveDialogService} from '../../../../services/ui-dialog.service';
import {UIPromptService} from '../../../../directives/ui-prompt.directive';
import {PasswordChangeModel} from '../../model/password-change.model';
import {PermissionService} from '../../../../services/permission.service';
import {TranslatePipe} from '../../../../pipes/translate.pipe';
import {SetUserContextPerson} from '../../../../../modules/auth/action/user-context-person.actions';
import {Store} from '@ngxs/store';
import {NotifierService} from '../../../../services/notifier.service';

@Component({
	selector: 'user-edit-person',
	templateUrl: 'user-edit-person.component.html'
})

export class UserEditPersonComponent {
	public currentPersonId;
	public startPageOptions;
	public editableUserPreferences;
	public savedPersonModel;
	public canUpdateAccount

	constructor(
		public personModel: PersonModel,
		public passwordChangeModel: PasswordChangeModel,
		private headerService: HeaderService,
		private permissionService: PermissionService,
		private translatePipe: TranslatePipe,
		private promptService: UIPromptService,
		public activeDialog: UIActiveDialogService,
		private notifierService: NotifierService,
		private store: Store) {
		this.startPageOptions = [];
		this.editableUserPreferences = [];
		this.savedPersonModel = {};
		this.retrieveStartPageOptions();
		this.loadComponentModel();
		this.canUpdateAccount = this.permissionService.hasPermission('UserUpdateOwnAccount');
	}

	/**
	 * Close the Dialog
	 */
	public cancelCloseDialog(): void {
		if (JSON.stringify(this.savedPersonModel) !== JSON.stringify(this.personModel)) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			)
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

	private loadComponentModel() {
		this.headerService.fetchComponentModel().subscribe(
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
					company: '',
					country: '',
					stateProv: '',
					keyWords: '',
					tdsNote: '',
					tdsLink: '',
					travelOK: 0,
					teams: [] ,
					staffType: [],
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
				this.savedPersonModel = Object.assign({},  this.personModel, this.savedPersonModel);
				this.currentPersonId = result.person.id;
			},
			(err) => console.log(err));
	}

	private retrieveStartPageOptions() {
		this.headerService.getStartPageOptions().subscribe(
			(result: any) => {
				this.startPageOptions = result.pages;
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
		} else if (this.passwordChangeModel.oldPassword || this.passwordChangeModel.newPassword) {
			if (!this.passwordChangeModel.oldPassword) {
				alert('Old Password should not be blank ')
				returnVal = false
			} else if (!this.passwordChangeModel.meetsCompositionRequirements || this.passwordChangeModel.containsUsername || !this.passwordChangeModel.atLeastMinimumLength) {
				alert('New Password does not meet all the requirements ')
				returnVal = false
			} else if (this.passwordChangeModel.newPassword !== this.passwordChangeModel.confirmPassword) {
				alert('New password and confirmation do not match')
				returnVal = false
			}
		}
		if (returnVal) {
			preferences.id = this.currentPersonId;
			this.headerService.updateAccount(preferences).subscribe(
				(result: any) => {
					this.savedPersonModel = this.personModel;
					// update user context state to show updated initials icon
					this.store.dispatch(new SetUserContextPerson())
						.subscribe(() => this.notifierService.broadcast('userDetailsUpdated'));
					this.cancelCloseDialog();
				},
				(err) => {
					if (err) {
						console.log(err)
					}
				});
		}
	}
}