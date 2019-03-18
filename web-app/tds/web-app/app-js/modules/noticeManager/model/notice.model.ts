export class NoticeColumnModel {
	columns: any[];

	constructor(dateFormat: string) {
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
				width: 220,
			}, {
				label: 'Active',
				property: 'active',
				type: 'boolean',
				width: 160,
			},
			{
				label: 'Activation Date',
				property: 'activationDate',
				format: dateFormat,
				type: 'date',
				width: 160
			},
			{
				label: 'Expiration Date',
				property: 'expirationDate',
				format: dateFormat,
				type: 'date',
				width: 160
			},
			{
				label: 'Sequence',
				property: 'sequence',
				type: 'text',
				width: 160
			},
			{
				label: 'Locked',
				property: 'locked',
				type: 'boolean',
				width: 160
			}
		];
	}
}

export enum NoticeType {
	PreLogin,
	PostLogin,
	Mandatory
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
	activationDate: any;
	expirationDate: any;
	sequence: number;
	locked: boolean;
	postMessageText: string;
	notShowAgain?: boolean;
}

export class PostNoticeResponse {
	redirectUri: string;
	notices: NoticeModel[];
}

/*
export class PostNoticeModel {
	htmlText: string;
	id: number;
	name: string;
	acknowledgeable: number;
	notShowAgain?: boolean;
	sequence?: number;
}
*/

export class StandardNotices {
	notices: NoticeModel[];
}

export const NoticeTypes = [
	{typeId: NoticeType.PreLogin, name: 'Pre Login'},
	{typeId: NoticeType.PostLogin, name: 'Post Login'},
	{typeId: NoticeType.Mandatory, name: 'Mandatory Acknowledgement'}
];