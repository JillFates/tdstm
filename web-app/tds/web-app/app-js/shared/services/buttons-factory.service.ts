import {Injectable} from '@angular/core';
import {TDSButton} from '../components/button/model/action-button.model';
import {TDSActionsButton} from '../components/button/model/action-button.model';
import {TranslatePipe} from '../pipes/translate.pipe';

@Injectable()
export class ButtonsFactoryService {
	private registeredButtons: {[key: string]: TDSButton};

	constructor(private translateService: TranslatePipe) {
		const translate = this.translateService.transform.bind(this.translateService);

		this.registeredButtons = {
			[TDSActionsButton.AssetEdit] : { icon: 'edit', title: 'Edit Asset' },
			[TDSActionsButton.AssetClone]: { icon: 'clone', title: 'Clone Asset' },
			[TDSActionsButton.BulkEdit]: { icon: 'ellipsis-v', title: translate('ASSET_EXPLORER.BULK_CHANGE.TITLE') },
			[TDSActionsButton.FilterClear]: { icon: 'times', title: translate('GLOBAL.CLEAR_FILTERS') },
			[TDSActionsButton.GenericExport]: { icon: 'download', title: 'Export' },
			[TDSActionsButton.GenericSave]: { icon: 'floppy-o', title: translate('GLOBAL.SAVE') },
			[TDSActionsButton.GenericSaveAs]: { icon: 'floppy-o', title: translate('GLOBAL.SAVE_AS') },
			[TDSActionsButton.TaskCreate]: { icon: 'file-text-o', title: 'Create a Task' },
			[TDSActionsButton.TaskSave]: { icon: 'floppy-o', title: 'Save' },
			[TDSActionsButton.TaskList]: { icon: 'list-alt', title: 'List existing Tasks' },
			[TDSActionsButton.CommentCreate]: { icon: 'comment', title: 'Create a Comment' },
			[TDSActionsButton.CommentList]: { icon: 'comments', title: 'List existing Comments' },
		};
	}

	create(key: TDSActionsButton): TDSButton {
		return this.registeredButtons[key] || null;
	}
}