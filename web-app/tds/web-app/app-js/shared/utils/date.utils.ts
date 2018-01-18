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

	/**
	 * Convert a interval from one to another.
	 * @param intervalBase
	 * @param intervalTarget
	 * @returns {string}
	 */
	public static convertInterval(intervalBase: any, intervalTarget: any): number {
		if (intervalBase.interval === INTERVAL.SECONDS) {
			if (intervalTarget.interval === INTERVAL.MINUTES) {
				return intervalBase.value / 60;
			}
			if (intervalTarget.interval === INTERVAL.HOURS) {
				return intervalBase.value / 3600;
			}
		}

		if (intervalBase.interval === INTERVAL.MINUTES) {
			if (intervalTarget.interval === INTERVAL.HOURS) {
				return intervalBase.value / 60;
			}
			if (intervalTarget.interval === INTERVAL.SECONDS) {
				return intervalBase.value * 60;
			}
		}

		if (intervalBase.interval === INTERVAL.HOURS) {
			if (intervalTarget.interval === INTERVAL.MINUTES) {
				return intervalBase.value * 60;
			}
			if (intervalTarget.interval === INTERVAL.SECONDS) {
				return intervalBase.value * 3600;
			}
		}
	}
}