import {DurationParts} from '../../../utils/date.utils';

export class DateRangeSelectorModel {
	start: any;
	end: any;
	locked?: boolean;
	dateFormat: string;
	timeFormat: string;
	duration: DurationParts;
}