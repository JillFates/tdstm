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
import {SortUtils} from '../../../../shared/utils/sort.utils';
import {forkJoin} from 'rxjs/observable/forkJoin';
import {Observable} from 'rxjs';

declare var jQuery: any;

@Component({
	selector: 'date-timezone-modal',
	templateUrl: '../tds/web-app/app-js/modules/user/components/date-timezone/user-date-timezone.component.html'
})
export class UserDateTimezoneComponent implements OnInit {
	public currentUserName;
	// List of elements to show on the Map
	public mapAreaList  = [];
	// List of timezones
	public timezonesList  = [];
	// List of possible date time formats
	public timeFormatList = [DateUtils.PREFERENCE_MIDDLE_ENDIAN, DateUtils.PREFERENCE_LITTLE_ENDIAN];
	public selectedTimezone;
	public selectedTimeFormat;

	constructor(
		private userService: UserService,
		public activeDialog: UIActiveDialogService,
		private preferenceService: PreferenceService) {
	}

	private getUserData(): void {
		let data = [
			this.retrieveMapAreas(),
			this.retrieveTimezones()
		];

		forkJoin(data).subscribe( (result: any) => {
			this.mapAreaList = result[0];
			this.timezonesList = result[1];
		});
	}

	/**
	 * Initialize the jQuery Time Picker
	 */
	ngOnInit(): void {
		// Resize modal window to fit content
		let modal = document.getElementsByClassName('modal-dialog') as HTMLCollectionOf<HTMLElement>;
		// TODO: Revisit Dialog Size
		if (modal.length !== 0) {
			modal[0].style.width = 'fit-content';
		}

		this.getUserData();
		setTimeout( () => {
			jQuery('#timezoneImage').timezonePicker({
				target: '#dateTimezone'
			});
			// Get the Selected area
			jQuery('#timezoneMap').find('area').click( (element: any) => {
				this.selectedTimezone = jQuery(element.currentTarget).attr('data-timezone');
			});
			// Delay time to allow the Picker to be initialized
		}, 600);

		this.preferenceService.getPreferences(PREFERENCES_LIST.CURR_DT_FORMAT, PREFERENCES_LIST.CURR_TZ).subscribe( (res: any) => {
			this.selectedTimeFormat = res[PREFERENCES_LIST.CURR_DT_FORMAT];
			this.selectedTimezone = res[PREFERENCES_LIST.CURR_TZ];
		});
	}

	/**
	 * Close the Dialog
	 */
	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	/**
	 * Get the List of Map Areas
	 */
	private retrieveMapAreas(): Observable<any> {
		return new Observable((observer: any) => {
			this.userService.getMapAreas().subscribe(
				(result: any) => {
					let mapAreas = [];
					for (let key in result) {
						if (result.hasOwnProperty(key)) {
							result[key].name = key;
							mapAreas.push(result[key]);
						}
					}
					observer.next(mapAreas.sort((a, b) => SortUtils.compareByProperty(a, b, 'name')));
					observer.complete();
				},
				(err) => console.log(err));
		});
	}

	/**
	 * Get the List of TimeZones
	 */
	private retrieveTimezones(): Observable<any> {
		return new Observable((observer: any) => {
			this.userService.getTimezones().subscribe(
				(result: any) => {
					let timezones = [];
					result.forEach((timezone: any) => {
						timezones.push(timezone.code);
					});
					observer.next(timezones);
					observer.complete();
				},
				(err) => console.log(err));
		});
	}

	/**
	 * Save the selected time zone and format
	 */
	public onSave(): void {

		const params = {
			timezone: this.selectedTimezone,
			datetimeFormat: this.selectedTimeFormat
		};

		this.userService.saveDateAndTimePreferences(params).subscribe(
			(result: any) => {
				this.cancelCloseDialog();
				location.reload();
			},
			(err) => console.log(err));
	}
}