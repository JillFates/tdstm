import {ModalType} from '../../../../../shared/model/constants';

export class SingleNoteModel {
	public modal: {
		title:  string;
		type: ModalType
	};
	public note: string;
}