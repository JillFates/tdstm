// Angular
import {Component, HostListener, OnInit} from '@angular/core';
// Services
import {HeaderService} from '../../services/header.service';
import {UIActiveDialogService, UIExtraDialog} from '../../../../services/ui-dialog.service';
import {PreferenceService} from '../../../../services/preference.service';
import {UIPromptService} from '../../../../directives/ui-prompt.directive';
// Models
import {PREFERENCES_LIST} from '../../../../services/preference.service';
// Utils
import {DateUtils} from '../../../../utils/date.utils';
import {SortUtils} from '../../../../utils/sort.utils';
import {forkJoin} from 'rxjs/observable/forkJoin';
import {Observable} from 'rxjs';
import {TranslatePipe} from '../../../../pipes/translate.pipe';

declare var jQuery: any;
// Work-around for deprecated jQuery functionality
jQuery.browser = {};
(function () {
	jQuery.browser.msie = false;
	jQuery.browser.version = 0;
	if (navigator.userAgent.match(/MSIE ([0-9]+)\./)) {
		jQuery.browser.msie = true;
		jQuery.browser.version = RegExp.$1;
	}
})();

@Component({
	selector: 'date-timezone-modal',
	templateUrl: 'user-date-timezone.component.html'
})
export class UserDateTimezoneComponent extends UIExtraDialog implements OnInit {
	public currentUserName;
	// List of elements to show on the Map
	public mapAreaList = [];
	// List of timezones
	public timezonesList = [];
	// List of possible date time formats
	public timeFormatList = [DateUtils.PREFERENCE_MIDDLE_ENDIAN, DateUtils.PREFERENCE_LITTLE_ENDIAN];
	public selectedTimezone;
	public selectedTimeFormat;
	// The timezone Pin is controlled by the Timezone picker, this helps to control it outside the jquery lib
	public timezonePinShow = false;
	// Local Data copy
	private dateTimezoneData = '';

	constructor(
		public shouldReturnData: Boolean,
		public defaultTimeZone: String,
		private headerService: HeaderService,
		private preferenceService: PreferenceService,
		private translatePipe: TranslatePipe,
		private promptService: UIPromptService) {
		super('#datetime-modal');
	}

	private getUserData(): void {
		let data = [
			this.retrieveMapAreas(),
			this.retrieveTimezones()
		];

		forkJoin(data).subscribe((result: any) => {
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
		setTimeout(() => {
			jQuery('#timezoneImage').timezonePicker({
				target: '#dateTimezone'
			});

			// Get the Selected area
			jQuery('#timezoneMap').find('area').click((element: any) => {
				this.timezonePinShow = true;
				this.selectedTimezone = jQuery(element.currentTarget).attr('data-timezone');
			});

			if (this.selectedTimezone && this.selectedTimezone !== null && this.selectedTimezone !== '') {
				// Preselect the timezone (this feature was not available in the original implementation)
				this.onTimezoneSelected(this.selectedTimezone);
			}
			// Delay time to allow the Picker to be initialized
		}, 800);

		setTimeout(() => {
			this.defaultTimeZone = this.timezonesList.includes(this.defaultTimeZone) ? this.defaultTimeZone : '';
			if (this.defaultTimeZone) {
				this.selectedTimezone = this.defaultTimeZone;
			}
			// Timeout to ensure this is always done if a valid timezone is provided
		}, 1500)

		this.preferenceService.getPreferences(PREFERENCES_LIST.CURR_DT_FORMAT, PREFERENCES_LIST.CURR_TZ).subscribe((res: any) => {
			this.selectedTimeFormat = res[PREFERENCES_LIST.CURR_DT_FORMAT];
			this.selectedTimezone = res[PREFERENCES_LIST.CURR_TZ];

			this.dateTimezoneData = JSON.stringify({
				timezone: this.selectedTimezone,
				datetimeFormat: this.selectedTimeFormat
			});
		});
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

		if (this.dateTimezoneData !== JSON.stringify({
			timezone: this.selectedTimezone,
			datetimeFormat: this.selectedTimeFormat
		})) {
			this.promptService.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM')	,
				this.translatePipe.transform('GLOBAL.CANCEL')	,
			)
				.then(confirm => {
					if (confirm) {
						this.dismiss();
					}
				})
				.catch((error) => console.log(error));
		} else {
			this.dismiss();
		}
	}

	/**
	 * Get the List of Map Areas
	 */
	private retrieveMapAreas(): Observable<any> {
		return new Observable((observer: any) => {
			this.headerService.getMapAreas().subscribe(
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
			this.headerService.getTimezones().subscribe(
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
	 * After the timezone has been selected from the dropdown it should auto-select
	 * the proper value on the Map
	 * @param timezone
	 */
	public onTimezoneSelected(timezone: string): void {
		// we ensure it is hide by default for elements like GMT that does not exist in the map
		this.timezonePinShow = false;
		jQuery('#timezoneMap').find('area').each((m, areaElement) => {
			// Find the timezone attribute from the map itself
			if (areaElement.getAttribute('data-timezone') === timezone) {
				// Emulate the click to get the PIN in place
				this.timezonePinShow = true;
				setTimeout(() => {
					jQuery(areaElement).triggerHandler('click');
				});
			}
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

		if (this.shouldReturnData) {
			this.close(params);
		} else {
			this.headerService.saveDateAndTimePreferences(params).subscribe(
				(result: any) => {
					this.cancelCloseDialog();
					location.reload();
				},
				(err) => console.log(err));
		}
	}
}