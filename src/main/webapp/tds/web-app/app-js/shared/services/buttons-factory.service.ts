import {Injectable} from '@angular/core';
import {TDSButton} from '../components/button/model/action-button.model';
import {TranslatePipe} from '../pipes/translate.pipe';
import {PermissionService} from './permission.service';

@Injectable()
export class ButtonsFactoryService {
	private registeredButtons: {[key: string]: TDSButton};
	private permissions: any = {};

	constructor(private translateService: TranslatePipe, private permissionService: PermissionService) {
		const translate = this.translateService.transform.bind(this.translateService);

		this.permissionService.getPermissions()
			.subscribe((permissions: any) => this.permissions = permissions);

		this.registeredButtons = {
			['tds-button-add']: { icon: 'plus-circle', title: translate('GLOBAL.ADD') },
			['tds-button-cancel']: { icon: 'ban', title: translate('GLOBAL.CANCEL'), tooltip: translate('GLOBAL.CANCEL') },
			['tds-button-clone']: { icon: 'clone', title: translate('GLOBAL.CLONE'), tooltip: translate('GLOBAL.CLONE') },
			['tds-button-close']: { icon: 'ban', title: translate('GLOBAL.CLOSE'), tooltip: translate('GLOBAL.CLOSE') },
			['tds-button-create']: { icon: 'plus-square', title: translate('GLOBAL.CREATE'), tooltip: translate('GLOBAL.CREATE') },
			['tds-button-custom']: { icon: '', title: '', tooltip: '' },
			['tds-button-delete']: { icon: 'trash', title: translate('GLOBAL.DELETE'), tooltip: translate('GLOBAL.DELETE') },
			['tds-button-edit']: { icon: 'pencil', title: translate('GLOBAL.EDIT'), tooltip: translate('GLOBAL.EDIT') },
			['tds-button-export'] : { icon: 'download', title: translate('GLOBAL.EXPORT'), tooltip: translate('GLOBAL.EXPORT') },
			['tds-button-filter'] : { icon: 'filter', title: translate('GLOBAL.FILTER'), tooltip: translate('GLOBAL.FILTER') },
			['tds-button-save']: { icon: 'floppy-o', title: translate('GLOBAL.SAVE'), tooltip: translate('GLOBAL.SAVE') },
			['tds-button-undo']: { icon: 'undo', title: translate('GLOBAL.UNDO'), tooltip: translate('GLOBAL.UNDO') }
		};
	}

	/**
	 * Create a button
	 * @param {string}  key of the button to create
	 * @params {Array<String>} permissionsList  List of strings containing the permissions required by this button
	 * @returns {TDSButton} created button, if key is not found returns  null
	 */
	create(key: string, permissionsList =  []): TDSButton {
		const button =  this.registeredButtons[key] || null;

		button.hasAllPermissions = this.hasAllPermissions(permissionsList);

		return button;
	}

	/**
	 * Returns a boolean determining if all the permissions provided by the button are fulfilled in the permissions list
	 */
	private hasAllPermissions(permissionsList: string[]): boolean {
		if (permissionsList.length === 0) {
			return true;
		}

		return permissionsList.every((permission: string) => {
			return this.permissions[permission] === 1;
		});
	}

}