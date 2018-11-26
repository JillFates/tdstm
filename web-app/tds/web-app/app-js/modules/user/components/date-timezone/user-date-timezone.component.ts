// Angular
import {Component, OnInit} from '@angular/core';
// Services
import {UserService} from '../../service/user.service';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
// Models
import {PREFERENCES_LIST} from '../../../../shared/services/preference.service';
// Utils
import {DateUtils} from '../../../../shared/utils/date.utils';

declare var jQuery: any;

@Component({
	selector: 'date-timezone-modal',
	templateUrl: '../tds/web-app/app-js/modules/user/components/date-timezone/user-date-timezone.component.html'
})
export class UserDateTimezoneComponent implements OnInit {
	public currentUserName;
	// List of elements to show on the Map
	public mapAreas  = [];
	// List of time zone only, taken directly from the list above
	public mapTimeZones  = [];
	// List of possible date time formats
	public dateTimeFormats = [DateUtils.PREFERENCE_MIDDLE_ENDIAN, DateUtils.PREFERENCE_LITTLE_ENDIAN];
	// Current Selected Time zone
	public dateTimezone;
	// Current Selected Date Time Format
	public dateTimeFormat;

	constructor(
		private userService: UserService,
		public activeDialog: UIActiveDialogService,
		private preferenceService: PreferenceService) {
		this.retrieveMapAreas();

	}

	/**
	 * Initialize the jQuery Time Picker
	 */
	ngOnInit(): void {
		setTimeout( () => {
			jQuery('#timezoneImage').timezonePicker({
				target: '#dateTimezone'
			});
			// Get the Selected area
			jQuery('#timezoneMap').find('area').click( (element: any) => {
				this.dateTimezone = jQuery(element.currentTarget).attr('data-timezone');
			});
		// Delay time to allow the Picker to be initialized
		}, 600);
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
				for (let key in result.areas) {
					if (result.areas.hasOwnProperty(key)) {
						result.areas[key].name = key;
						this.mapTimeZones.push(result.areas[key].name);
						this.mapAreas.push(result.areas[key]);
					}
				}
			},
			(err) => console.log(err));
	}
}