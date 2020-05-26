import {Component, ComponentFactoryResolver, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {LoginInfoModel} from '../../model/login-info.model';
import {ActionType} from '../../../dataScript/model/data-script.model';
import {DialogService, ModalSize} from 'tds-component-library';
import {ForgotPasswordModalComponent} from './forgot-password-modal.component';

@Component({
	selector: 'tds-forget-password',
	templateUrl: 'forgot-password.component.html',
})

export class ForgotPasswordComponent implements OnInit {

	public loginInfoModel: LoginInfoModel = new LoginInfoModel();
	public error: any;
	public passwordSent = false;
	public email: '';

	constructor(
		private dialogService: DialogService,
		private componentFactoryResolver: ComponentFactoryResolver,
		private router: Router) {
	}

	ngOnInit() {
		this.openModal(this.loginInfoModel, ActionType.VIEW);
	}

	/**
	 * Open The Dialog to Create, View or Edit the Provider
	 * @param {LoginInfoModel} loginInfoModel
	 * @param {number} actionType
	 * @param openFromList
	 */
	private async openModal(loginInfoModel: LoginInfoModel, actionType: ActionType, openFromList = false): Promise<void> {
		try {
			await this.dialogService.open({
				componentFactoryResolver: this.componentFactoryResolver,
				component: ForgotPasswordModalComponent,
				data: {
					loginInfoModel: loginInfoModel,
					actionType: actionType,
					openFromList: openFromList
				},
				modalConfiguration: {
					title: '',
					draggable: true,
					modalSize: ModalSize.MD,
					resizable: true,
				}
			}).toPromise();
			await this.router.navigate(['/login']);
		} catch (error) {
			console.error(error);
		}
	}

}