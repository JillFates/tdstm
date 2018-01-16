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
				time.getMinutes();
	}
}