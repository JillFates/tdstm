import {INTERVAL} from '../model/constants';

export class DateUtils {

	public static readonly DEFAULT_TIMEZONE_FORMAT = 'dd/MM/yyyy';

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
	 * Given a User Preference TimeZone format convert it to a known date format used by angular date pipe.
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

	public static getKendoDateFormat(userDateFormatPreference: string) {
		return (userDateFormatPreference === 'DD/MM/YYYY') ? 'dd/MMM/yyyy' : 'MMM/dd/yyy';
	}
}