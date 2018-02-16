import {INTERVAL} from '../model/constants';

export class DateUtils {

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
				DateUtils.getNumberWithLeadingZeros(time.getMinutes(), 2);
	}

	public static getNumberWithLeadingZeros(number: number, leadingZeros: number): string {
		let result = number.toString();
		while (result.length < leadingZeros) {
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
}