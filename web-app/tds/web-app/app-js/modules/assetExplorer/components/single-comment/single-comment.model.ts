import {ModalType} from '../../../../shared/model/constants';

export class SingleCommentModel {
	public modal: {
		title:  string;
		type: ModalType
	}
	public archive: boolean;
	public comment: string;
	public category: string;
	public assetType: string;
	public assetName: string;

	public lastUpdated: string;
	public dateCreated: string;
}
