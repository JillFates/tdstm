import {Injectable} from '@angular/core';
import {TDSButton} from '../components/button/model/action-button.model';
import {TDSActionsButton, TDSActionsIcon} from '../components/button/model/action-button.model';
import {TranslatePipe} from '../pipes/translate.pipe';

@Injectable()
export class ButtonsFactoryService {
	private registeredButtons: {[key: string]: TDSButton};

	constructor(private translateService: TranslatePipe) {
		const translate = this.translateService.transform.bind(this.translateService);

		this.registeredButtons = {
			// buttons
			[TDSActionsButton.AssetEdit] : { icon: 'edit', title: 'Edit Asset' },
			[TDSActionsButton.AssetClone]: { icon: 'clone', title: 'Clone Asset' },
			[TDSActionsButton.BulkEdit]: { icon: 'ellipsis-v', title: translate('ASSET_EXPLORER.BULK_CHANGE.TITLE') },
			[TDSActionsButton.CommentCreate]: { icon: 'comment', title: 'Create a Comment' },
			[TDSActionsButton.CommentList]: { icon: 'comments', title: 'List existing Comments' },
			[TDSActionsButton.FilterClear]: { icon: 'times', title: translate('GLOBAL.CLEAR_FILTERS') },
			[TDSActionsButton.GenericCreate]: { icon: 'plus-square', title: translate('GLOBAL.CREATE') },
			[TDSActionsButton.GenericExport]: { icon: 'download', title: 'Export' },
			[TDSActionsButton.GenericSave]: { icon: 'floppy-o', title: translate('GLOBAL.SAVE') },
			[TDSActionsButton.GenericSaveAs]: { icon: 'floppy-o', title: translate('GLOBAL.SAVE_AS') },
			[TDSActionsButton.TaskCreate]: { icon: 'file-text-o', title: 'Create a Task' },
			[TDSActionsButton.TaskSave]: { icon: 'floppy-o', title: 'Save' },
			[TDSActionsButton.TaskList]: { icon: 'list-alt', title: 'List existing Tasks' },
			[TDSActionsButton.ViewEdit]: { icon: 'pencil', title: 'Edit view' },
			[TDSActionsButton.ViewDelete]: { icon: 'trash', title: 'Delete view' },
			// just icons
			[TDSActionsIcon.GenericConfiguration]: { icon: 'cog', title: 'Configure', justIcon: true},
			[TDSActionsIcon.GenericIsFavorite]: { icon: 'star', title: 'Remove from favorites', justIcon: true},
			[TDSActionsIcon.GenericIsNotFavorite]: { icon: 'star-o', title: 'Add to favorites', justIcon: true},
			[TDSActionsIcon.GenericRefresh]: { icon: 'refresh', title: 'Refresh', justIcon: true}
		};
	}

	create(key: TDSActionsButton): TDSButton {
		return this.registeredButtons[key] || null;
	}
}