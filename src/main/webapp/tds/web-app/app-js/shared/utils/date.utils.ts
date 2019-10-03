import {INTERVAL} from '../model/constants';
import * as moment from 'moment-timezone';

export interface DurationParts {
	days: number;
	hours: number;
	minutes: number;
}

export type DatePartUnit = 'days' | 'hours' | 'months' | 'minutes';

export interface IncrementDateArgument {
	value: number;
	unit: DatePartUnit
}

export class DateUtils {

	// As managed by the server, due the difference on the Lowercase and Upercase
	public static readonly PREFERENCE_MIDDLE_ENDIAN = 'MM/DD/YYYY';
	public static readonly PREFERENCE_LITTLE_ENDIAN = 'DD/MM/YYYY';

	public static readonly DEFAULT_FORMAT_DATE = 'dd/MM/yyyy';
	public static readonly DEFAULT_FORMAT_TIME = 'hh:mm A';
	public static readonly OUTPUT_PIPE_TIME_FORMAT = 'HH:mm:ss';
	public static readonly SERVER_FORMAT_DATETIME = 'YYYY-MM-DDT' + DateUtils.OUTPUT_PIPE_TIME_FORMAT;
	public static readonly SERVER_FORMAT_DATE = 'YYYY-MM-DD';
	public static readonly TIMEZONE_GMT = 'GMT';

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
	 * @param userTimeZone the user's timezone to format the value as
	 * @param iso8601Value a datetime value in the ISO 8601 format (yyyy-mm-DDThh:MM:ssZ)
	 * @param timeFormat Optional, used only to overwrite the default time format value
	 * @return the the dateTimeValue converted to perferred user datetime format (e.g. 12/25/2018 02:10pm)
	 */
	public static formatUserDateTime(userTimeZone: string, iso8601Value: string, timeFormat = '') {
		if (iso8601Value === undefined || !iso8601Value) {
			return '';
		}
		if (timeFormat) {
			return moment.tz(iso8601Value, userTimeZone).format(timeFormat);
		}

		return moment.tz(iso8601Value, userTimeZone).format(`YYYY-MM-DD ${this.OUTPUT_PIPE_TIME_FORMAT}`);
	}

	/**
	 * Converts and Formats a Date into GMT
	 * GMT is being used on the server and should always consider the User Timezone preference
	 * @param {sourceLocalTime} date
	 * @param {userTimeZone} string i.e America/Monterrey
	 * @returns {string} The formatted day
	 */
	public static convertToGMT(sourceLocalTime: Date, userTimeZone: string): string {
		// We stripped any reference to a Time zone
		const datetimeStringWithNoTZ  = moment(sourceLocalTime).format('YYYY-MM-DDTHH:mm:ss');
		const momentObjectWithUserTimezone = moment.tz(datetimeStringWithNoTZ, userTimeZone);
		const momentObjectWithGMT = momentObjectWithUserTimezone.clone().tz(this.TIMEZONE_GMT);
		return momentObjectWithGMT.format();
	}

	/**
	 * Converts and Formats a Date from GMT (Default DATABASE Timezone) into the User Preference Format
	 * @param {sourceLocalTime} date
	 * @param {userTimeZone} string i.e America/Monterrey
	 * @returns {string} The formatted day
	 */
	public static convertFromGMT(sourceTime: Date | string, userTimeZone: string): string {
		const sourceZonedTime = moment.tz(sourceTime, this.TIMEZONE_GMT);
		const targetZonedTime = sourceZonedTime.clone().tz(userTimeZone);
		return targetZonedTime.format();
	}

	/**
	 * Corrects a date to GMT without need of the client's timezone
	 * @param {sourceTime} date
	 * @returns {Date} The date adjusted to GMT (Timezone remains the same)
	 */
	public static adjustDateTimezoneOffset(sourceTime: Date): Date {
		let adjustedTime = new Date ( sourceTime );
		adjustedTime.setMinutes ( sourceTime.getMinutes() + sourceTime.getTimezoneOffset() );
		return adjustedTime;
	}

	/**
	 * Get the Date without the hh:mm:ss
	 * @param {sourceTime} The original source in a GMT based timezone
	 * @returns {string} The formatted day without hh:mm:ss
	 */
	public static getDateFromGMT(sourceTime: Date): string {
		const sourceZonedTime = moment.tz(sourceTime, this.TIMEZONE_GMT);
		return sourceZonedTime.format('YYYY-MM-DD');
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

	/**
	 * Having a duration returns the parts which that duration is made
	 * @param duration (number)
	 * @param scale (string val)
	 * @returns {DurationParts}
	 */
	public static getDurationParts(duration: number, scale = 'M' ): DurationParts  {
		const result = {days: null, hours: null, minutes: null};

		if (duration || duration === 0) {
			const startDate = moment().startOf('day');
			const endDate = moment().startOf('day');
			endDate.add(scale.toLowerCase(), duration);

			const parts = moment.duration(endDate.diff(startDate));
			result.days = parseInt(parts.asDays(), 10);
			result.hours =  parseInt(parts.hours(), 10);
			result.minutes = parseInt(parts.minutes(), 10)
		}

		return result;
	}

	/**
	 * Calculate duration parts among two dates
	 * @param start (date)
	 * @param end (date)
	 * @returns {DurationParts}
	 */
	public static getDurationPartsAmongDates(start: any, end: any): DurationParts  {
		const result = {days: null, hours: null, minutes: null};

		if (!start || !end) {
			return result;
		}
		const begin = moment(start);
		const finish = moment(end);

		const duration = moment.duration(finish.diff(begin));

		if (duration) {
			result.days = parseInt(duration.asDays(), 10);
			result.hours =  parseInt(duration.hours(), 10);
			result.minutes = parseInt(duration.minutes(), 10)
		}

		return result;
	}

	/**
	 * Apply an array of Increment/decrement operations to the date provided
	 * @param date (date)
	 * @param incrementArguments (object[])
	 * @returns {Date}
	 */
	public static increment(date: any, incrementArguments: IncrementDateArgument[]): any {
		let resultingDate = date;

		incrementArguments.forEach(
			(argument: IncrementDateArgument) =>
				resultingDate =  new Date(moment(resultingDate).add(argument.value, argument.unit))
		);

		return resultingDate;
	}

	/**
	 * Convert a datepart object to the equivalent minutes
	 * @param durationParts (DurationParts)
	 * @returns {number}
	 */
	public static convertDurationPartsToMinutes(durationParts: DurationParts): number {
		return (durationParts.days * 24 * 60) + (durationParts.hours * 60) + durationParts.minutes;
	}

	/**
	 * Return the provided dated formated using moment
	 * @param date (Object)
	 * @param format (String)
	 * @returns {string}
	 */
	public static formatDate(date: any, format: string): string {
		return moment(date)
			.format(format);
	}

	/**
	 * Converts a string source value to a Date object, following a format pattern.
	 * @param {string} value
	 * @param {string} format
	 * @returns {Date}
	 */
	public static toDateUsingFormat(value: string, format: string): Date {
		let momentObject: moment.Moment = moment(value, format);
		if (momentObject.isValid()) {
			return momentObject.toDate();
		}
		return null;
	}

	/**
	 * Could receive a string or date, based in the type make sure returns a date  object
	 * @param {any} value:  String or Date to cast
	 * @returns {Date}
	 */
	public static stringDateToDate(date: any): any {
		return (date && date.toDateString) ? date : DateUtils.toDateUsingFormat(date, DateUtils.SERVER_FORMAT_DATE)
	}

	/**
	 * Checks if a provided date range is valid and sends an alert if the dates are reversed
	 * @param startTime (Date): Starting date of the range
	 * @param endTime (Date): Ending date of the range
	 * @param reverseMessage (String): Message to alert if the date is reversed
	 * @returns {Boolean}
	 */
	public static validateDateRange (startTime: Date, endTime: Date, reverseMessage = 'The completion time must be later than the start time.'): boolean {
		if (!startTime || !endTime) {
			return true;
		} else if (startTime > endTime) {
			alert(reverseMessage);
			return false;
		} else {
			return true;
		}
	}
}
