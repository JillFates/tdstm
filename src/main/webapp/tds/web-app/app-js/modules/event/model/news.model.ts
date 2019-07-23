export class NewsModel {
	id?: number;
	type: string;
	created?: any;
	text: string;
	state: string;
}

export class NewsDetailModel {
	commentObject: {
		id: number;
		message: string;
		resolution: string;
		dateCreated: string;
		isArchived: number;
		commentType: string;
		archivedBy: string;
		comment: string;
		displayOption: string;
		createdBy: {
			id: number
		},
		dateArchived: string;
		moveEvent: {
			id: number
		}
	};
	personCreateObj: string;
	personResolvedObj: string;
	dtCreated: string;
	dtResolved: string;
	assetName: string;
	commentType: string;

	constructor() {
		this.commentObject = {
			id: null,
			message: '',
			resolution: '',
			dateCreated: '',
			isArchived: null,
			archivedBy: '',
			comment: '',
			displayOption: '',
			commentType: '',
			createdBy: {
				id: null
			},
			dateArchived: '',
			moveEvent: {
				id: null
			}
		};
		this.personCreateObj = '';
		this.personResolvedObj = '';
		this.dtCreated = '';
		this.dtResolved = '';
		this.assetName = '';
	}
}

export const CommentType = {
	'N': 'News',
	'I': 'Issue'
}

export const DisplayOptionGeneric = 'G';
export const DisplayOptionUser = 'U';

export const DisplayOptions = {
	DisplayOptionGeneric: 'Generic Comment',
	DisplayOptionUser: 'UserComment'
}