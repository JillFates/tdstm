import {FilterType} from 'tds-component-library';

export class EventNewsColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
		this.columns = [
			{
				label: 'Created At',
				property: 'createdAt',
				format: dateFormat,
				filterType: FilterType.date,
				width: 'auto'
			}, {
				label: 'Created By',
				property: 'createdBy',
				filterType: FilterType.text,
				width: 'auto',
			}, {
				label: 'Comment Type',
				property: 'commentType',
				filterType: FilterType.text,
				width: 'auto',
			}, {
				label: 'Comment',
				property: 'comment',
				filterType: FilterType.text,
				width: 'auto',
			}, {
				label: 'Resolution',
				property: 'resolution',
				filterType: FilterType.text,
				width: 'auto',
			}, {
				label: 'Resolved At',
				property: 'resolvedAt',
				format: dateFormat,
				filterType: FilterType.date,
				width: 'auto'
			}, {
				label: 'Resolved By',
				property: 'resolvedBy',
				filterType: FilterType.text,
				width: 'auto',
			}
		];
	}
}

export class EventNewsModel {
	newsId?: number;
	moveEventId?: number;
	isArchived?: boolean;
	createdAt?: Date;
	createdBy?: string;
	commentType?: string;
	comment?: string;
	resolution?: string;
	resolvedAt?: Date;
	resolvedBy?: string;
};
