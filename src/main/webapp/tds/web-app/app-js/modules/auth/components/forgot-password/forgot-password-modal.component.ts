import {Component, ComponentFactoryResolver, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {Event as NavigationEvent, NavigationStart, Router} from '@angular/router';
import {Dialog, DialogConfirmAction, DialogService} from 'tds-component-library';
import {NgForm} from '@angular/forms';
import {filter} from 'rxjs/operators';
import {ActionType} from '../../../dataScript/model/data-script.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {PermissionService} from '../../../../shared/services/permission.service';
import {LoginInfoModel} from '../../model/login-info.model';
import {LoginService} from '../../service/login.service';

@Component({
	selector: 'tds-forgot-password-modal',
	templateUrl: 'forgot-password-modal.component.html'
})

export class ForgotPasswordModalComponent extends Dialog implements OnInit {
	@Input() data: any;
	@ViewChild('forgotPasswordForm', {read: NgForm, static: true}) forgotPasswordForm: NgForm;
	@ViewChild('emailElement', {static: false}) emailElement: ElementRef;
	public loginInfoModel: LoginInfoModel;
	public modalTitle: string;
	public actionTypes = ActionType;
	public modalType = ActionType.VIEW;
	private dataSignature: string;
	public passwordSent = false;
	public email: '';
	public error: any;

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private translatePipe: TranslatePipe,
		private permissionService: PermissionService,
		private loginService: LoginService,
		private router: Router
	) {
		super();
		router.events
			.pipe(filter((event: NavigationEvent) => {
				return(event instanceof NavigationStart);
			}))
			.subscribe((event: NavigationStart) => {
				this.onCancelClose();
			});
	}

	ngOnInit(): void {
		setTimeout(() => {
			this.onSetUpFocus(this.emailElement);
		});
	}

	/**
	 * Request Password Recovery by sending email
	 */
	public onSendEmail(): void {
		this.validateFields()
			.then(() => {
				this.loginService.forgotPassword(this.email).subscribe((data: any) => {
					if (!data.success) {
						this.error = 'An error occurred, please try again later.';
						return;
					}
					this.passwordSent = true;
				});
			})
			.catch(err => this.passwordSent = false);
	}

	/**
	 *  Validates fields
	 */
	public validateFields(): Promise<boolean> {
		const emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,63})+$/;

		return new Promise((resolve, reject) => {
			this.error = '';
			if ((this.email && !emailExp.test(this.email)) || !this.email) {
				this.error = 'Not a valid e-mail address';
			}
			return this.error && this.error.length > 0 ? reject(new Error('Error validating fields')) : resolve(true);
		});
	}

	/**
	 * Verify the Object has not changed
	 * @returns {boolean}
	 */
	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.loginInfoModel);
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		if (this.isDirty()) {
			this.dialogService.confirm(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
			).subscribe((result: any) => {
				if (result.confirm === DialogConfirmAction.CONFIRM) {
					this.onCancelClose();
				}
			});
		} else {
			this.onCancelClose();
		}
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}