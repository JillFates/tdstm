import {ModalType} from '../../../../../shared/model/constants';

export class SingleNoteModel {
	public id?: string;
	public modal: {
		title:  string;
		type: ModalType
	};
	public note: string;
	public asset: {
		id?: any;
		text: string;
	};
}