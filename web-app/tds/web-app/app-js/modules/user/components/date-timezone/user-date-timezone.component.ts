import {Component, OnInit} from '@angular/core';
import {UserService} from '../../service/user.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';

declare var jQuery: any;

@Component({
	selector: 'date-timezone-modal',
	templateUrl: '../tds/web-app/app-js/modules/user/components/date-timezone/user-date-timezone.component.html'
})
export class UserDateTimezoneComponent implements OnInit {
	public currentUserName;
	public mapAreas;

	constructor(
		private userService: UserService,
		public activeDialog: UIActiveDialogService) {
		this.retrieveMapAreas();
	}

	/**
	 * Initialize the jQuery Time Picker
	 */
	ngOnInit(): void {
		jQuery('#timezoneImage').timezonePicker({
			target: '#dateTimezone'
		});
	}

	/**
	 * Close the Dialog
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	private retrieveMapAreas() {
		this.userService.getMapAreas().subscribe(
			(result: any) => {
				this.mapAreas = [];
				for (let key in result.areas) {
					if (result.areas.hasOwnProperty(key)) {
						result.areas[key].name = key;
						this.mapAreas.push(result.areas[key]);
					}
				}
			},
			(err) => console.log(err));
	}
}