import {INTERVAL} from '../model/constants';
import * as moment from 'moment';

export class DateUtils {

	public static readonly DEFAULT_FORMAT_DATE = 'dd/MM/yyyy';
	public static readonly DEFAULT_FORMAT_TIME = 'hh:mm a';

	/**
	 * Used to format an ISO 8601 Date String (e.g. 2018-08-03T20:44:15Z) to the user's preferred
	 * format. It is assumed that the value has been already adjusted to the user's preferred timezone
	 * on the server. Note that value will indicate Z that is not actually Z but that of the user's
	 * preferred timezone.
	 *
	 * This is done this way because the datetimes are stored in GMT and the user can choose in the UI
	 * to show datetimes in timezones differing from their local timezone set on their computer so
	 * using actual dates computed correctly can get quite outrageous.
	 *
	 * @param prefDateTimeFormat - the user's preferred datetime format
	 * @param dateTimeValue - the value to be converted
	 * @return the the dateTimeValue converted to perferred user datetime format (e.g. 12/25/2018 02:10pm)
	 */
	public static formatUserDateTime(prefDateTimeFormat: String, dateTimeValue: String) {
		if (dateTimeValue === undefined) {
			return '';
		}
		return dateTimeValue.replace('Z', '').replace('T',' ');
		// const isoPattern = /^(\d{4})-(\d{2})-(\d{2}).(\d{2}):(\d{2}):(\d{2})Z$/;
		// let dtParts = dateTimeValue.match(isoPattern);
		// if (dtParts.length !== 7) {
		// 	return '';
		// }

		// let result = '';
		// // if (prefDateTimeFormat.startsWith(this.DEFAULT_FORMAT_DATE)) {
		// // 	result = dtParts[3] + '/' + dtParts[2] + '/' + dtParts[1];
		// // } else {
		// // 	result = dtParts[2] + '/' + dtParts[3] + '/' + dtParts[1];
		// // }

		// result = dtParts[1] + '/' + dtParts[2] + '/' + dtParts[3];

		// let amPm = 'AM';
		// let hour = parseInt( dtParts[4], 0);
		// if (hour > 11) {
		// 	amPm = 'PM';
		// 	if (hour > 12) {
		// 		hour = hour - 12;
		// 		dtParts[4] = hour.toString();
		// 	}
		// }

		// // Assemble with the time component and return the sucker
		// return result + ' ' + dtParts[4] + ':' + dtParts[5] + amPm;
	}

	/**
	 * Create a Date Object
	 * @param destination
	 * @returns {Date}
	 */
	public static compose(destination: any): Date {
		// TODO: MomentJS? User Preference?
		let compose = destination;
		if (destination !== '' && destination !== null && destination !== 'undefined') {
			compose = new Date(destination);
		}
		return compose;

	}

	public static getTimestamp(): String {
		let time = new Date();
		return time.getFullYear().toString() +
				(time.getMonth() + 1 < 10 ? ('0' + (time.getMonth() + 1)) : time.getMonth() + 1) +
				(time.getDate() + 1 < 10 ? ('0' + (time.getDate())) : time.getDate()) + '_' +
				time.getHours() +
				DateUtils.getNumberWithLeadingZeros(time.getMinutes(), 1);
	}

	/**
	 * Fills out a number with desired leading zeros as a string.
	 * @param {number} number
	 * @param {number} leadingZeros
	 * @returns {string}
	 */
	public static getNumberWithLeadingZeros(number: number, leadingZeros: number): string {
		let result = number.toString();
		while (result.length <= leadingZeros) {
			result = '0' + result;
		}
		return result;
	}

	/**
	 * Convert a interval from one to another.
	 * @param intervalBase
	 * @param intervalTarget
	 * @returns {string}
	 */
	public static convertInterval(intervalBase: any, intervalTarget: any): number {
		if (intervalBase.interval === INTERVAL.SECONDS) {
			if (intervalTarget === INTERVAL.MINUTES) {
				return intervalBase.value / 60;
			}
			if (intervalTarget === INTERVAL.HOURS) {
				return intervalBase.value / 3600;
			}
		}

		if (intervalBase.interval === INTERVAL.MINUTES) {
			if (intervalTarget === INTERVAL.HOURS) {
				return intervalBase.value / 60;
			}
			if (intervalTarget === INTERVAL.SECONDS) {
				return intervalBase.value * 60;
			}
		}

		if (intervalBase.interval === INTERVAL.HOURS) {
			if (intervalTarget === INTERVAL.MINUTES) {
				return intervalBase.value * 60;
			}
			if (intervalTarget === INTERVAL.SECONDS) {
				return intervalBase.value * 3600;
			}
		}
		// Same Base / target conversion
		return intervalBase.value;
	}

	/**
	 * Used to convert a User preferred Date Format to the date format used by angular date pipe
	 * @param dateFormat - the TM Date Format from User Preferences
	 * @returns {string}
	 */
	public static translateTimeZoneFormat(dateFormat: string): string {
		let result = dateFormat;
		const dayRegExp = /D/g;
		result = result.replace(dayRegExp, 'd');
		const yearRegExp = /Y/g;
		result = result.replace(yearRegExp, 'y');
		return result;
	}

	/**
	 * Used to convert a User preferred Date Format to the date format plus time used by angular date pipe
	 * @param dateFormat - the TM Date Format from User Preferences
	 * @returns {string}
	 */
	public static translateDateTimeFormat(dateFormat: string): string {
		return this.translateTimeZoneFormat(dateFormat) + ' ' + this.DEFAULT_FORMAT_TIME;
	}

	/**
	 * Given a User Preference TimeZone format and a date, returns the corresponding date transformed to unix time
	 * This function can be used to sort by dates
	 * @returns {number}
	 */
	public static convertDateToUnixTime(format: string, date = ''): number  {
		let yearMontDayDate = null;
		const fragments = date.split('/') ;

		if (fragments.length < 3) { return null; }

		if (format === 'DD/MM/YYYY') {
			yearMontDayDate = new Date(`${fragments[2]}.${fragments[1]}.${fragments[0]}`) ;
		}

		if (format === 'MM/DD/YYYY') {
			yearMontDayDate = new Date(`${fragments[2]}.${fragments[0]}.${fragments[1]}`) ;
		}

		return !isNaN(yearMontDayDate) && yearMontDayDate ?  yearMontDayDate.getTime() / 1000 : null;
	};

	/**
	 * Given a  date returns the beginning and end interval for that particular date
	 * @returns {init, end}
	 */
	public static getInitEndFromDate(date: Date): {init: Date, end: Date} {
		const init = new Date(date.getFullYear(), date.getMonth(), date.getDate());
		const end = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 23, 59, 59);

		return {init, end};
	}

	/**
	 * Given a User Preference TimeZone format convert it to a format used by Kendo controls
	 * if format provided doesn't exists returns default date format
	 * @returns {string}
	 */
	public static translateDateFormatToKendoFormat(userDateFormatPreference: string): string {
		const defaultFormat = 'MM/dd/yyy';

		const dateFormats = {
			'DD/MM/YYYY' : 'dd/MM/yyyy',
			'MM/DD/YYYY' : defaultFormat
		};

		return dateFormats[userDateFormatPreference] || defaultFormat;
	}

	/**
	 * Return a duration in a readable human way, show we use https://www.unc.edu/~rowlett/units/symbol.html ?
	 * @param duration (number)
	 * @param scale (char val)
	 * return string representation of the duration in terms of days, hours, minutes
	 */
	public static formatDuration(duration: any, scale: any): string {
		scale = scale.toLowerCase();
		let startDate = moment().startOf('day');
		let endDate = moment().startOf('day');
		endDate.add(duration, scale);

		let durationDate = moment.duration(endDate.diff(startDate)), durationResult = '';

		let days = durationDate.asDays();
		if (days >= 1) {
			durationResult += parseInt(days.toString(), 0) + ' day' + ((days > 1) ? 's ' : ' ');
		}

		let hours = durationDate.hours();
		if (hours >= 1) {
			durationResult += parseInt(hours.toString(), 0) + ' hr' + ((hours > 1) ? 's ' : ' ');
		}

		let minutes = durationDate.minutes();
		if (minutes >= 1) {
			durationResult += parseInt(minutes.toString(), 0) + ' min' + ((minutes > 1) ? 's ' : ' ');
		}

		return durationResult;
	}
}