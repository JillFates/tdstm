import {ModalType} from '../../../../../shared/model/constants';

export class SingleCommentModel {
	public id?: string;
	public modal: {
		title:  string;
		type: ModalType
	}
	public archive: boolean;
	public comment: string;
	public category: string;
	public assetClass: {
		id?: string;
		text?: string;
	};
	public asset: {
		id?: any;
		text: string;
	};
	public lastUpdated?: string;
	public dateCreated?: string;
}