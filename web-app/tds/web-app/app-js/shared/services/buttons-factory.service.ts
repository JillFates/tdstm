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
			[TDSActionsButton.Add]: { icon: 'plus-circle', title: translate('GLOBAL.ADD') },
			[TDSActionsButton.Cancel]: { icon: 'ban', title: translate('GLOBAL.CANCEL'), tooltip: translate('GLOBAL.CANCEL') },
			[TDSActionsButton.Clone]: { icon: 'clone', title: translate('GLOBAL.CLONE'), tooltip: translate('GLOBAL.CLONE') },
			[TDSActionsButton.Close]: { icon: 'ban', title: translate('GLOBAL.CLOSE'), tooltip: translate('GLOBAL.CLOSE') },
			[TDSActionsButton.Create]: { icon: 'plus-square', title: translate('GLOBAL.CREATE'), tooltip: translate('GLOBAL.CREATE') },
			[TDSActionsButton.Custom]: { icon: '', title: '', tooltip: '' },
			[TDSActionsButton.Delete]: { icon: 'trash', title: translate('GLOBAL.DELETE'), tooltip: translate('GLOBAL.DELETE') },
			[TDSActionsButton.Edit] : { icon: 'pencil', title: translate('GLOBAL.EDIT'), tooltip: translate('GLOBAL.EDIT') },
			[TDSActionsButton.Export] : { icon: 'download', title: translate('GLOBAL.EXPORT'), tooltip: translate('GLOBAL.EXPORT') },
			[TDSActionsButton.Save]: { icon: 'floppy-o', title: translate('GLOBAL.SAVE'), tooltip: translate('GLOBAL.SAVE') }
		};
	}

	create(key: TDSActionsButton): TDSButton {
		return this.registeredButtons[key] || null;
	}
}