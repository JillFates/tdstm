// Angular
import {Component, HostListener, Input, OnDestroy, OnInit} from '@angular/core';
// Services
import {HeaderService} from '../../services/header.service';
import {PreferenceService} from '../../../../services/preference.service';
// Models
import {PREFERENCES_LIST} from '../../../../services/preference.service';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import {SetTimeZoneAndDateFormat} from '../../../../../modules/auth/action/timezone-dateformat.actions';
// Utils
import {DateUtils} from '../../../../utils/date.utils';
import {SortUtils} from '../../../../utils/sort.utils';
import {TranslatePipe} from '../../../../pipes/translate.pipe';
import {Store} from '@ngxs/store';
// Other
import {forkJoin} from 'rxjs/observable/forkJoin';
import {Observable, ReplaySubject} from 'rxjs';
import * as R from 'ramda';
import {takeUntil} from 'rxjs/operators';

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
	templateUrl: 'user-date-timezone.component.html',
})
export class UserDateTimezoneComponent extends Dialog implements OnInit, OnDestroy {
	@Input() data: any;

	public currentUserName;
	// List of elements to show on the Map
	public mapAreaList = [];
	// List of timezones
	public timezonesList = [];
	// List of possible date time formats
	public timeFormatList = [
		DateUtils.PREFERENCE_MIDDLE_ENDIAN,
		DateUtils.PREFERENCE_LITTLE_ENDIAN,
	];
	public selectedTimezone: any;
	public selectedTimeFormat;
	// The timezone Pin is controlled by the Timezone picker, this helps to control it outside the jquery lib
	public timezonePinShow = false;
	// Local Data copy
	private dataSignature = '';

	public defaultTimeZone: string;
	public shouldReturnData: boolean;
	virtualizationSettings = {
		itemHeight: 15
	};
	unsubscribeAll$: ReplaySubject<void> = new ReplaySubject<void>();

	constructor(
		private dialogService: DialogService,
		private headerService: HeaderService,
		private preferenceService: PreferenceService,
		private translatePipe: TranslatePipe,
		private store: Store) {
		super();
	}

	/**
	 * Initialize the jQuery Time Picker
	 */
	ngOnInit(): void {
		this.shouldReturnData = this.data.shouldReturnData;
		this.defaultTimeZone = R.clone(this.data.defaultTimeZone);

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			show: () => true,
			disabled: () => !this.selectedTimezone  || !this.selectedTimeFormat,
			type: DialogButtonType.ACTION,
			action: this.onSave.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.getUserData();
	}

	/**
	 * Close the Dialog
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
				),
				this.translatePipe.transform(
					'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
				)
			)
				.pipe(takeUntil(this.unsubscribeAll$))
				.subscribe((data: any) => {
					if (data.confirm === DialogConfirmAction.CONFIRM) {
						super.onCancelClose();
					}
				});
		} else {
			super.onCancelClose();
		}
	}

	/**
	 * After the timezone has been selected from the dropdown it should auto-select
	 * the proper value on the Map
	 * @param timezone
	 */
	public onTimezoneSelected(timezone: any): void {
		// we ensure it is hide by default for elements like GMT that does not exist in the map
		this.timezonePinShow = false;
		jQuery('#timezoneMap')
			.find('area')
			.each((m, areaElement) => {
				// Find the timezone attribute from the map itself
				if (areaElement.getAttribute('data-timezone') === (timezone && timezone.code)) {
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
			timezone: this.selectedTimezone && this.selectedTimezone.code,
			datetimeFormat: this.selectedTimeFormat,
		};
		if (this.shouldReturnData) {
			this.onAcceptSuccess(params);
		} else {
			this.headerService.saveDateAndTimePreferences(params).subscribe(
				(result: any) => {
					// Update the storage before reload the window
					this.store.dispatch(new SetTimeZoneAndDateFormat({
						timezone: params && params.timezone,
						dateFormat: params && params.datetimeFormat
					}))
						.pipe(takeUntil(this.unsubscribeAll$))
						.subscribe(() => {
						this.onAcceptSuccess();
						location.reload();
					});
				},
				err => console.log(err)
			);
		}
	}

	private getUserData(): void {
		let data = [
			this.retrieveMapAreas(),
			this.headerService.getTimezones(),
			this.preferenceService.getPreferences(
				PREFERENCES_LIST.CURR_DT_FORMAT,
				PREFERENCES_LIST.CURR_TZ
			),
		];
		forkJoin(data).subscribe((result: any) => {
			this.mapAreaList = result[0];
			this.timezonesList = result[1];
			this.selectedTimeFormat =
				result[2][PREFERENCES_LIST.CURR_DT_FORMAT];
			this.selectedTimezone = this.timezonesList.find(
				timezone =>
					(timezone && timezone.code) === result[2][PREFERENCES_LIST.CURR_TZ]
			);
			let defaultTimeZone = this.timezonesList.find(
				timezone => (timezone && timezone.code) === this.defaultTimeZone
			);
			if (defaultTimeZone) {
				this.selectedTimezone = defaultTimeZone;
			}
			this.prepareMapData();
		});
	}

	/**
	 *  Prepare all the data for the Map
	 */
	private prepareMapData(): void {
		setTimeout(() => {
			jQuery('#timezoneImage').timezonePicker({
				target: '#dateTimezone',
			});
			// Get the Selected area
			jQuery('#timezoneMap')
				.find('area')
				.click((element: any) => {
					this.timezonePinShow = true;
					this.selectedTimezone = this.timezonesList.find(
						timezone =>
							timezone.code ===
							jQuery(element.currentTarget).attr('data-timezone')
					);
				});
			if (
				this.selectedTimezone &&
				this.selectedTimezone.code !== null &&
				this.selectedTimezone.code !== ''
			) {
				// Preselect the timezone (this feature was not available in the original implementation)
				this.onTimezoneSelected(this.selectedTimezone);
			}
			// Delay time to allow the Picker to be initialized
			this.dataSignature = JSON.stringify({
				timezone: this.selectedTimezone,
				datetimeFormat: this.selectedTimeFormat,
			});
		});
	}

	/**
	 * Get the List of Map Areas
	 */
	private retrieveMapAreas(): Observable<any> {
		return new Observable((observer: any) => {
			this.headerService.getMapAreas()
				.pipe(takeUntil(this.unsubscribeAll$))
				.subscribe(
				(result: any) => {
					let mapAreas = [];
					for (let key in result) {
						if (result.hasOwnProperty(key)) {
							result[key].name = key;
							mapAreas.push(result[key]);
						}
					}
					observer.next(
						mapAreas.sort((a, b) =>
							SortUtils.compareByProperty(a, b, 'name')
						)
					);
					observer.complete();
				},
				err => console.log(err)
			);
		});
	}

	/**
	 * Verify if the data has changed
	 */
	private isDirty(): boolean {
		return this.selectedTimezone || this.selectedTimeFormat;
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	@HostListener('window:beforeunload', ['$event'])
	ngOnDestroy(): void {
		this.unsubscribeAll$.next();
		this.unsubscribeAll$.complete();
	}
}
