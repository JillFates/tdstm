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
		isArchived: number,
		archivedBy: string;
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