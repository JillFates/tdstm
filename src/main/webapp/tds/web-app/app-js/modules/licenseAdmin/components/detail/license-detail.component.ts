// Angular
import {Component, ComponentFactoryResolver, Input, OnInit} from '@angular/core';
// Component
import {ApplyKeyComponent} from '../apply-key/apply-key.component';
import {ManualRequestComponent} from '../manual-request/manual-request.component';
// Service
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {LicenseAdminService} from '../../service/license-admin.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {NotifierService} from '../../../../shared/services/notifier.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
// Model
import {LicenseModel, MethodOptions, LicenseStatus} from '../../model/license.model';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
import {AlertType} from '../../../../shared/model/alert.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'tds-license-detail',
	templateUrl: 'license-detail.component.html'
})
export class LicenseDetailComponent extends Dialog implements OnInit {
	@Input() data: any;

	public environmentList: any = [];
	protected projectList: any = [];
	public dateFormat = DateUtils.DEFAULT_FORMAT_DATE;
	public methodOptions = MethodOptions;
	public licenseStatus = LicenseStatus;
	public licenseModel: LicenseModel = {};

	constructor(
		private componentFactoryResolver: ComponentFactoryResolver,
		private dialogService: DialogService,
		private licenseAdminService: LicenseAdminService,
		private preferenceService: PreferenceService,
		private translatePipe: TranslatePipe,
		private notifierService: NotifierService
	) {
		super();
	}

	ngOnInit(): void {
		this.licenseModel = Object.assign({}, this.data.licenseModel);

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			tooltipText: 'Delete',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.onDelete.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: 'Close',
			show: () => true,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'applyKey',
			icon: 'key',
			text: 'Apply License Key',
			show: () => this.licenseModel.status !== this.licenseStatus.ACTIVE,
			type: DialogButtonType.CONTEXT,
			action: this.applyLicenseKey.bind(this)
		});

		this.buttons.push({
			name: 'resubmitRequest',
			icon: 'redo',
			text: 'Resubmit Request',
			show: () => this.licenseModel.status !== this.licenseStatus.ACTIVE,
			type: DialogButtonType.CONTEXT,
			action: this.resubmitLicenseRequest.bind(this)
		});

		this.buttons.push({
			name: 'resubmitRequest',
			icon: 'details',
			text: 'Manually Submit Request',
			show: () => this.licenseModel.status !== this.licenseStatus.ACTIVE,
			type: DialogButtonType.CONTEXT,
			action: this.manuallyRequest.bind(this)
		});

		this.preferenceService.getUserDatePreferenceAsKendoFormat().subscribe((dateFormat) => {
			this.dateFormat = dateFormat;
		});

		this.licenseAdminService.getLicense(this.licenseModel.id).subscribe((licenseModel: LicenseModel) => {
			this.licenseModel = licenseModel;
		})
	}

	/**
	 * Close the Dialog but first it verify is not Dirty
	 */
	public cancelCloseDialog(): void {
		super.onCancelClose();
	}

	/**
	 * Open Apply License Key Dialog
	 */
	private async applyLicenseKey(): Promise<void> {
		await this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: ApplyKeyComponent,
			data: {
				licenseModel: this.licenseModel
			},
			modalConfiguration: {
				title: 'Apply License Key',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).toPromise();
	}

	/**
	 * Open a dialog for a Manual Request
	 */
	private async manuallyRequest(): Promise<void> {
		await this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: ManualRequestComponent,
			data: {
				licenseModel: this.licenseModel
			},
			modalConfiguration: {
				title: 'Email License',
				draggable: true,
				modalSize: ModalSize.MD
			}
		}).toPromise();
	}

	/**
	 * Submit again the License in case there was an error on the original creation
	 */
	protected resubmitLicenseRequest(): void {
		this.licenseAdminService.resubmitLicenseRequest(this.licenseModel.id).subscribe(
			(result) => {
				let message = '';
				let alertType: AlertType = null;
				if (result) {
					alertType = AlertType.INFO;
					message = 'Request License was successfully';
				} else {
					message = 'There was an error on the request';
					alertType = AlertType.WARNING;
				}
				this.notifierService.broadcast({
					name: alertType,
					message: message
				});
			},
			(err) => console.log(err));
	}

	/**
	 * Delete the current License
	 */
	public onDelete(): void {
		this.dialogService.confirm(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
			'You are about to delete the license. Do you want to proceed?'
		).subscribe((result: any) => {
			if (result.confirm === DialogConfirmAction.CONFIRM) {
				this.licenseAdminService
					.deleteLicense(this.licenseModel.id)
					.subscribe(
						(result) => {
							this.onCancelClose();
						});
			}
		});
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}
}
