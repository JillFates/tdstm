import {Component} from '@angular/core';
import {UserService} from '../../service/user.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'date-timezone-modal',
	templateUrl: '../tds/web-app/app-js/modules/user/components/date-timezone/user-date-timezone.component.html'
})
export class UserDateTimezoneComponent {
	public currentUserName;
	public mapAreas;

	constructor(
		private userService: UserService,
		public activeDialog: UIActiveDialogService) {
		this.retrieveMapAreas();
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
				console.log(this.mapAreas);
			},
			(err) => console.log(err));
	}
}