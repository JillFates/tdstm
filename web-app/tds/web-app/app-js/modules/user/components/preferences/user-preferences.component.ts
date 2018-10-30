import {Component, ElementRef, Inject, OnInit, Renderer2, ChangeDetectorRef} from '@angular/core';
import {UserService} from '../../service/user.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'user-preferences',
	templateUrl: '../tds/web-app/app-js/modules/user/components/preferences/user-preferences.component.html'
})
export class UserPreferencesComponent implements OnInit {
	public showPreferences:boolean;
	public currentUserPreferences;
	public currentUserName;
	constructor(
		private userService: UserService,
		public activeDialog: UIActiveDialogService,
		private cd: ChangeDetectorRef) {
		this.retrieveUserPreferences();
		this.showPreferences = true;
		this.retrieveUserName();
	}

	ngOnInit(): void {
		let modal = document.getElementsByClassName('modal-dialog') as HTMLCollectionOf<HTMLElement>;

		if (modal.length != 0) {
			modal[0].style.width = "fit-content";
		}
	}

	/**
	 * Close the Dialog
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();

	}

	protected printCurrentPrefs(): void {
		console.log(this.currentUserPreferences);
	}

	/**
	 * Get the user preferences for the current user
	 */

	private retrieveUserPreferences() {
		this.userService.getUserPreferences().subscribe(
			(result: any) => {
				this.currentUserPreferences = result.prefMap;
			},
			(err) => console.log(err));
	}

	private retrieveUserName() {
		this.userService.getUserName().subscribe(
			(result: any) => {
				this.currentUserName = result.person.firstName;
			},
			(err) => console.log(err));
	}

	private removePreference(prefCode) {
		this.userService.removePreference(prefCode).subscribe(
			(result: any) => {
				this.currentUserPreferences.splice( this.currentUserPreferences.findIndex(x => x.prefCode == prefCode), 1 );
			},
			(err) => console.log(err));
	}


	public handleRemoveClick(e) {
		let prefCode = e.srcElement.parentElement.parentElement.id;
		this.removePreference(prefCode);
	}

	public resetPreferences() {
		this.userService.resetPreferences().subscribe(
			(result: any) => {
				this.currentUserPreferences = [];
				window.location.href="../../tdstm/project/list"
			},
			(err) => console.log(err));
	}
}