import {Component} from '@angular/core';
import {UserService} from '../../service/user.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'user-preferences',
	templateUrl: '../tds/web-app/app-js/modules/user/components/view-edit/user-view-edit.component.html'
})
export class UserViewEditComponent {
	public currentUserInfo;
	public startPageOptions;
	public editableUserPreferences;

	constructor(
		private userService: UserService,
		public activeDialog: UIActiveDialogService) {
		this.currentUserInfo = [];
		this.startPageOptions = [];
		this.editableUserPreferences = [];
		this.retrieveUserInfo();
		this.retrieveStartPageOptions();
		this.retrieveEditablePreferences();
	}

	/**
	 * Close the Dialog
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();

	}

	private retrieveEditablePreferences() {
		let editablePrefCodes = ["START_PAGE","CURR_POWER_TYPE"];
		this.userService.getPreference(editablePrefCodes).subscribe(
			(result: any) => {
				this.editableUserPreferences = result.preferences;
				console.log(this.editableUserPreferences);
			},
			(err) => console.log(err));
	}

	private retrieveUserInfo() {
		this.userService.getUserName().subscribe(
			(result: any) => {
				this.currentUserInfo = result.person;
				console.log(this.currentUserInfo);
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

	private savePreference(pref) {
		this.userService.savePreference(pref).subscribe(
			(result: any) => {
				this.startPageOptions = result.pages;
				console.log(this.startPageOptions);
			},
			(err) => console.log(err));
	}

	public submitPreferences(e) {
		console.log(e);
		let dateRegExpForExp = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;

		let returnVal = true
		let firstName = e.target.querySelector("#firstNameId").value;
		let middleName = e.target.querySelector("#middleNameId").value;
		let lastName = e.target.querySelector("#lastNameId").value;
		let nickName = e.target.querySelector("#nickNameId").value;
		let title = e.target.querySelector("#titleId").value;
		let email = e.target.querySelector("#emailId").value;
		let expiryDate = '06/24/2020 12:23 PM'
		let powerType = e.target.querySelector("#powerTypeId").value;
		let startPage = e.target.querySelector("#startPageId").value;

		if (expiryDate + "" == "undefined") {
			expiryDate = "null"
		}
		if (!firstName) {
			alert("First Name should not be blank ")
			returnVal = false
		} else if (expiryDate != "null" && !expiryDate) {
			alert("Expiry Date should not be blank ")
			returnVal = false
		} else if (expiryDate != "null" && !dateRegExpForExp.test(expiryDate)) {
			alert("Expiry Date should be in 'mm/dd/yyyy HH:MM AM/PM' format")
			returnVal = false
		}
		if (returnVal) {
			this.userService.getCurrentUser().subscribe(
				(result: any) => {
					let parameters = 'id=' + result.id
						+'&firstName='+firstName +'&lastName='+lastName +'&middleName='+middleName
						+'&nickName='+nickName+'&title='+title+'&email='+email+'&expiryDate='+expiryDate
						+'&powerType='+powerType+'&startPage='+startPage;
					this.userService.updateAccount(parameters).subscribe(
						(err) => console.log(err));
				},
				(err) => console.log(err));
		}
	}
}