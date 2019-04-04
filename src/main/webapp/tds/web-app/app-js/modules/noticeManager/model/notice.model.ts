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
				type: 'text',
				width: 160,
			}
		];
	}
}

export enum NoticeType {
	PreLogin,
	PostLogin
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

export const NoticeTypes = [
	{typeId: '1', name: 'Postlogin'},
	{typeId: '2', name: 'Prelogin'}
];