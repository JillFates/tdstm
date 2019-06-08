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
}