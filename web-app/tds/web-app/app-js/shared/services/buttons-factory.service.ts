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
			[TDSActionsButton.ArchitectureGraphShow] : {
				icon: 'sitemap',
				title: translate('GLOBAL.ARTIFACTS.ARCHITECTURE_GRAPH'),
				tooltip: `${translate('GLOBAL.SHOW')} ${translate('GLOBAL.ARTIFACTS.ARCHITECTURE_GRAPH')}`
			},
			[TDSActionsButton.AssetEdit] : {
				icon: 'pencil',
				title: translate('GLOBAL.EDIT'),
				tooltip: `${translate('GLOBAL.EDIT')} ${translate('GLOBAL.ARTIFACTS.ASSET')}`
			},
			[TDSActionsButton.AssetClone]: {
				icon: 'clone',
				title: translate('GLOBAL.CLONE'),
				tooltip: `${translate('GLOBAL.CLONE')} ${translate('GLOBAL.ARTIFACTS.ASSET')}`
			},
			[TDSActionsButton.AssetClose]: {
				icon: 'ban',
				title: translate('GLOBAL.CLOSE'),
				tooltip: `${translate('GLOBAL.CLOSE')} ${translate('GLOBAL.ARTIFACTS.ASSET')}`
			},
			[TDSActionsButton.AssetDelete]: {
				icon: 'trash',
				title: translate('GLOBAL.DELETE'),
				tooltip: `${translate('GLOBAL.DELETE')} ${translate('GLOBAL.ARTIFACTS.ASSET')}`
			},
			[TDSActionsButton.BulkEdit]: {
				icon: 'ellipsis-v',
				title: translate('ASSET_EXPLORER.BULK_CHANGE.TITLE')
			},
			[TDSActionsButton.CommentCreate]: {
				icon: 'comment-o',
				title: translate('GLOBAL.CREATE'),
				tooltip: `${translate('GLOBAL.CREATE')} ${translate('GLOBAL.ARTIFACTS.COMMENT')}`
			},
			[TDSActionsButton.CommentEdit]: {
			icon: 'pencil',
				title: translate('GLOBAL.EDIT'),
				tooltip: `${translate('GLOBAL.EDIT')} ${translate('GLOBAL.ARTIFACTS.COMMENT')}`
			},
			[TDSActionsButton.CommentDelete]: {
				icon: 'trash',
				title: translate('GLOBAL.TRASH'),
				tooltip: `${translate('GLOBAL.DELETE')} ${translate('GLOBAL.ARTIFACTS.COMMENT')}`
			},
			[TDSActionsButton.CommentList]: {
				icon: 'comments-o',
				title: `${translate('GLOBAL.LIST')} ${translate('GLOBAL.ARTIFACTS.COMMENTS')}`,
				tooltip: `${translate('GLOBAL.LIST')} ${translate('GLOBAL.ARTIFACTS.COMMENTS')}`
			},
			[TDSActionsButton.FilterClear]: {
				icon: 'times',
				title: translate('GLOBAL.CLEAR_FILTERS')
			},
			[TDSActionsButton.GenericConfiguration]: {
				icon: 'cog',
				title: translate('GLOBAL.CONFIGURE')
			},
			[TDSActionsButton.GenericExport]: {
				icon: 'download',
				title: translate('GLOBAL.EXPORT')
			},
			[TDSActionsButton.GenericIsFavorite]: {
				icon: 'star',
				title: translate('GLOBAL.REMOVE_FAVORITES')
			},
			[TDSActionsButton.GenericIsNotFavorite]: {
				icon: 'star-o',
				title: translate('GLOBAL.ADD_FAVORITES')
			},
			[TDSActionsButton.GenericRefresh]: {
				icon: 'refresh',
				title: translate('GLOBAL.REFRESH')
			},
			[TDSActionsButton.GenericSave]: {
				icon: 'floppy-o',
				title: translate('GLOBAL.SAVE')
			},
			[TDSActionsButton.GenericSaveAs]: {
				icon: 'floppy-o',
				title: translate('GLOBAL.SAVE_AS')
			},
			[TDSActionsButton.TaskCreate]: {
				icon: 'file-text-o',
				title: translate('GLOBAL.CREATE'),
				tooltip: `${translate('GLOBAL.CREATE')} ${translate('GLOBAL.ARTIFACTS.TASK')}`
			},
			[TDSActionsButton.TaskDelete]: {
				icon: 'trash',
				title: translate('GLOBAL.DELETE'),
				tooltip: `${translate('GLOBAL.DELETE')} ${translate('GLOBAL.ARTIFACTS.TASK')}`
			},
			[TDSActionsButton.TaskEdit]: {
				icon: 'pencil',
				title: translate('GLOBAL.EDIT'),
				tooltip: `${translate('GLOBAL.EDIT')} ${translate('GLOBAL.ARTIFACTS.TASK')}`
			},
			[TDSActionsButton.TaskSave]: {
				icon: 'floppy-o',
				title: translate('GLOBAL.SAVE')
			},
			[TDSActionsButton.TaskList]: {
				icon: 'list-alt',
				title: translate('GLOBAL.LIST'),
				tooltip: `${translate('GLOBAL.LIST')} ${translate('GLOBAL.ARTIFACTS.TASKS')}`
			},
			[TDSActionsButton.ViewCreate]: {
				icon: 'plus-square',
				title: translate('GLOBAL.CREATE'),
				tooltip: `${translate('GLOBAL.CREATE')} ${translate('GLOBAL.ARTIFACTS.VIEW')}`
			},
			[TDSActionsButton.ViewEdit]: {
				icon: 'pencil',
				title: translate('GLOBAL.EDIT'),
				tooltip: `${translate('GLOBAL.EDIT')} ${translate('GLOBAL.ARTIFACTS.VIEW')}`
			},
			[TDSActionsButton.ViewDelete]: {
				icon: 'trash',
				title: translate('GLOBAL.DELETE'),
				tooltip: `${translate('GLOBAL.DELETE')} ${translate('GLOBAL.ARTIFACTS.VIEW')}`
			}
		};
	}

	create(key: TDSActionsButton): TDSButton {
		return this.registeredButtons[key] || null;
	}
}