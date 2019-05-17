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
				width: 260,
			}, {
				label: 'Active',
				property: 'active',
				type: 'boolean',
				width: 160,
			},
			{
				label: 'Need acknowledgement',
				property: 'needAcknowledgement',
				type: 'boolean',
				width: 160,
			}
			/*,
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
			*/
		];
	}
}

/*
export enum NoticeType {
	PreLogin = 1,
	PostLogin,
	Mandatory
}
*/
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
	acknowledgeLabel: string;
	notShowAgain?: boolean;
	needAcknowledgement?: boolean;
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

export class Notices {
	notices: NoticeModel[];
}

export const NOTICE_TYPE_PRE_LOGIN = 'PRE_LOGIN';
export const NOTICE_TYPE_POST_LOGIN = 'POST_LOGIN';
export const NOTICE_TYPE_MANDATORY = 'MANDATORY';

export const NoticeTypes = [
	{typeId: NOTICE_TYPE_PRE_LOGIN, name: 'Prelogin'},
	{typeId: NOTICE_TYPE_POST_LOGIN, name: 'Postlogin'},
	{typeId: NOTICE_TYPE_MANDATORY, name: 'Mandatory Acknowledgment Message'},
];
