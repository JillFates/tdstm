export class NoticeColumnModel {
	columns: any[];

	constructor() {
		this.columns = [
			{
				label: 'Action',
				property: 'action',
				type: 'action',
				width: 108,
				locked: true
			}, {
				label: 'Title',
				property: 'title',
				type: 'text',
				width: 160,
			}, {
				label: 'Type',
				property: 'typeId',
				type: 'text',
				width: 160,
			}, {
				label: 'Active',
				property: 'active',
				type: 'boolean',
				width: 160,
			}
		];
	}
}

export class NoticeModel {
	acknowledgeable: boolean;
	active: boolean;
	createdBy: any;
	dateCreated: string;
	htmlText: string;
	id: number;
	lastModified: string;
	rawText: string;
	title: string;
	typeId: any;
}

export const NOTICE_TYPE_PRE_LOGIN = 'PRE_LOGIN';
export const NOTICE_TYPE_POST_LOGIN = 'POST_LOGIN';

export const NoticeTypes = [
	{typeId: NOTICE_TYPE_PRE_LOGIN, name: 'Prelogin'},
	{typeId: NOTICE_TYPE_POST_LOGIN, name: 'Postlogin'}
];
