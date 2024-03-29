import {
	ColumnHeaderData,
	FilterType,
	DropdownFilterData,
} from 'tds-component-library';

export class NoticeColumnModel {
	public columns: ColumnHeaderData[];

	constructor(dateFormat: string, typeDropdownData: DropdownFilterData) {
		this.columns = [
			{
				label: 'Title',
				property: 'title',
				filterType: FilterType.text
			},
			{
				label: 'Type',
				property: 'typeId',
				filterType: FilterType.dropdown,
				filterInputData: typeDropdownData
			},
			{
				label: 'Active',
				property: 'active',
				filterType: FilterType.boolean,
				filterInputData: {
					data: [
						{
							text: 'Yes',
							value: true,
						},
						{
							text: 'No',
							value: false,
						},
					],
					defaultItem: { text: '', value: null },
				}
			},
			{
				label: 'Activation Date',
				property: 'activationDate',
				format: dateFormat,
				filterType: FilterType.date
			},
			{
				label: 'Expiration Date',
				property: 'expirationDate',
				format: dateFormat,
				filterType: FilterType.date
			},
			{
				label: 'Sequence',
				property: 'sequence',
				filterType: FilterType.text
			},
			{
				label: 'Locked',
				property: 'locked',
				filterType: FilterType.boolean,
				filterInputData: {
					data: [
						{
							text: 'Yes',
							value: true,
						},
						{
							text: 'No',
							value: false,
						},
					],
					defaultItem: { text: '', value: null },
				}
			},
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

export class Notices {
	notices: NoticeModel[];
}

export const NOTICE_TYPE_PRE_LOGIN = 'PRE_LOGIN';
export const NOTICE_TYPE_POST_LOGIN = 'POST_LOGIN';
export const NOTICE_TYPE_MANDATORY = 'MANDATORY';

export const NoticeTypes = [
	{ typeId: NOTICE_TYPE_PRE_LOGIN, name: 'Prelogin' },
	{ typeId: NOTICE_TYPE_POST_LOGIN, name: 'Postlogin' },
	{ typeId: NOTICE_TYPE_MANDATORY, name: 'Mandatory Acknowledgment Message' },
];
