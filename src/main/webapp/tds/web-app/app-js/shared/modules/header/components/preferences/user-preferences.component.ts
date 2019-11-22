import { Component, OnInit, HostListener } from '@angular/core';
import { HeaderService } from '../../services/header.service';
import { UIActiveDialogService } from '../../../../services/ui-dialog.service';

@Component({
	selector: 'user-preferences',
	templateUrl: 'user-preferences.component.html',
})
export class UserPreferencesComponent implements OnInit {
	public preferenceList;
	public personName;
	public fixedPreferenceCodes;

	constructor(
		private headerService: HeaderService,
		public activeDialog: UIActiveDialogService
	) {
		this.loadComponentModel();
	}

	ngOnInit(): void {
		// Resize modal window to fit content
		let modal = document.getElementsByClassName(
			'modal-dialog'
		) as HTMLCollectionOf<HTMLElement>;

		if (modal.length !== 0) {
			// TODO: Refactor the Dialog so it is no longer a Table
			modal[0].style.width = '-moz-fit-content';
			modal[0].style.width = 'fit-content';
		}
	}

	@HostListener('window:keydown', ['$event'])
	handleKeyboardEvent(event: KeyboardEvent) {
		if (event.key === 'Escape') {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Close the Dialog
	 */
	public cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	/**
	 * Used to fetch all of the model data that will be used by this componet for the User Preferences Manager
	 */
	private loadComponentModel() {
		this.headerService.fetchComponentModel().subscribe(
			(result: any) => {
				this.preferenceList = result.preferences;
				this.fixedPreferenceCodes = result.fixedPreferenceCodes;
				this.personName = result.person.firstName;
			},
			err => console.log(err)
		);
	}

	/**
	 * Used to remove a single preference for the user
	 * @param preferenceCode
	 */
	public removePreference(preferenceCode: String) {
		this.headerService.removePreference(preferenceCode).subscribe(
			(result: any) => {
				// Remove the delete preference from the list
				let idx = this.preferenceList.findIndex(
					x => x.code === preferenceCode
				);
				if (idx > -1) {
					this.preferenceList.splice(idx, 1);
				}
			},
			err => console.log(err)
		);
	}

	/**
	 * Used to remove all preferences for a user other than those that are fixed (e.g. CURR_PROJ)
	 */
	public resetPreferences() {
		this.headerService.resetPreferences().subscribe(
			(result: any) => {
				this.cancelCloseDialog();
				location.reload();
			},
			err => console.log(err)
		);
	}
}
