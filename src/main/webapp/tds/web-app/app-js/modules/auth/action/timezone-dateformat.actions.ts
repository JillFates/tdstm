export class SetTimeZoneAndDateFormat {
	static readonly type = '[TimeZoneAndFormat] Update the Timezone and dateFormat';
	constructor(public payload: { timezone: string, dateFormat: string}) {}
}
