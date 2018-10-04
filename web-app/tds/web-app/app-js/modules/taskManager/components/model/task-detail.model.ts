import {ModalType} from '../../../../shared/model/constants';

export class TaskDetailModel {
	public id?: string;
	public modal: {
		title:  string;
		type: ModalType
	};
	public detail?: any;
}