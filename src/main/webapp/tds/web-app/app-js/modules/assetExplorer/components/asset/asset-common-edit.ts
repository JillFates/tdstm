import { TagModel } from '../../../assetTags/model/tag.model';
import { AssetExplorerService } from '../../../assetManager/service/asset-explorer.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
import {
	UIActiveDialogService,
	UIDialogService,
} from '../../../../shared/services/ui-dialog.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import {
	OnInit,
	AfterViewInit,
	OnDestroy,
	ViewChildren,
	ViewChild,
	QueryList, ComponentFactoryResolver,
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { TagService } from '../../../assetTags/service/tag.service';
import { AssetShowComponent } from './asset-show.component';
import { equals as ramdaEquals, clone as ramdaClone } from 'ramda';
import { AssetCommonHelper } from './asset-common-helper';
import { DropDownListComponent } from '@progress/kendo-angular-dropdowns';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { UIHandleEscapeDirective as EscapeHandler } from '../../../../shared/directives/handle-escape-directive';
import { UserContextService } from '../../../auth/service/user-context.service';
import { UserContextModel } from '../../../auth/model/user-context.model';
import { PermissionService } from '../../../../shared/services/permission.service';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
import {DialogConfirmAction, DialogService, ModalSize} from 'tds-component-library';
import {AssetEditComponent} from './asset-edit.component';

declare var jQuery: any;

export class AssetCommonEdit implements OnInit, AfterViewInit, OnDestroy {
	@ViewChild('form', { static: false }) public form: NgForm;
	@ViewChildren(DropDownListComponent) dropdowns: QueryList<
		DropDownListComponent
	>;
	private destroySubject: Subject<any> = new Subject<any>();

	private assetTagsDirty = false;
	protected assetTagsModel: any = { tags: [] };
	protected newAssetTagsSelection: any = { tags: [] };
	protected tagList: Array<TagModel> = [];
	protected dateFormat: string;
	protected readMore = false;
	public isDependenciesValidForm = true;
	protected defaultSelectOption = 'Please Select';
	protected defaultPlanStatus = 'Unassigned';
	protected defaultValidation = 'Unknown';
	protected isHighField = AssetCommonHelper.isHighField;
	private initialModel: any = null;

	constructor(
		public componentFactoryResolver: ComponentFactoryResolver,
		protected model: any,
		protected activeDialog: UIActiveDialogService,
		protected userContextService: UserContextService,
		protected permissionService: PermissionService,
		protected assetExplorerService: AssetExplorerService,
		protected dialogService: DialogService,
		protected oldDialogService: UIDialogService,
		protected notifierService: NotifierService,
		protected tagService: TagService,
		protected metadata: any,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe,
		private parentDialog: any
	) {
		this.assetTagsModel = { tags: metadata.assetTags };
		this.tagList = metadata.tagList;

		this.userContextService
			.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				this.dateFormat = userContext.dateFormat;
				if (this.dateFormat && this.dateFormat !== null) {
					this.dateFormat = this.dateFormat
						.toLowerCase()
						.replace(/m/g, 'M');
				}
			});
	}
	/**
	 * Initiates The Injected Component
	 */
	ngOnInit(): void {
		jQuery('[data-toggle="popover"]').popover();
	}

	// set the handlers on open / on close to set the flags that indicate the state of the
	// dropdown list items (opened/closed)
	ngAfterViewInit() {
		this.dropdowns.toArray().forEach(dropdown => {
			dropdown.open
				.pipe(takeUntil(this.destroySubject))
				.subscribe(() =>
					EscapeHandler.setIsDropdownListOpen(
						dropdown.wrapper.nativeElement,
						true
					)
				);

			dropdown.close
				.pipe(takeUntil(this.destroySubject))
				.subscribe(() =>
					setTimeout(
						() =>
							EscapeHandler.setIsDropdownListOpen(
								dropdown.wrapper.nativeElement,
								false
							),
						200
					)
				);
		});
	}

	ngOnDestroy() {
		this.destroySubject.next();
	}

	/**
	 * Save a reference to the initial model in order to detect changes later on
	 */
	onInitDependenciesDone(model): void {
		this.initialModel = ramdaClone(model);
	}

	/**
	 * On Tag Selector change event.
	 * @param $event
	 */
	protected onTagValueChange($event: any): void {
		this.newAssetTagsSelection.tags = $event.tags;
		this.assetTagsDirty = true;
	}

	/**
	 * Used to check if the form is dirty
	 */
	public isDirty = (): boolean => {
		return !ramdaEquals(this.initialModel, this.model) || this.assetTagsDirty;
	}

	/**
	 * Used on Create Asset view.
	 * Creates the tag associations if configured.
	 */
	protected createTags(assetId: number): void {
		let tagsToAdd = { tags: [] };
		if (
			this.newAssetTagsSelection.tags &&
			this.newAssetTagsSelection.tags.length > 0
		) {
			tagsToAdd = this.newAssetTagsSelection;
		}
		this.tagService
			.createAssetTags(
				assetId,
				tagsToAdd.tags.map(item => item.id)
			)
			.subscribe(
				result => {
					this.showAssetDetailView(this.model.asset.assetClass.name, assetId);
				},
				error => console.error('Error while saving asset tags', error)
			);
	}

	/**
	 * Save Asset Tags configuration
	 */
	protected saveAssetTags(): void {
		let tagsToAdd = { tags: [] };
		let tagsToDelete = { ...this.assetTagsModel };
		this.newAssetTagsSelection.tags.forEach((asset: TagModel) => {
			let foundIndex = this.assetTagsModel.tags.findIndex(
				item => item.id === asset.id
			);
			if (foundIndex === -1) {
				// add tag
				tagsToAdd.tags.push(asset);
			} else {
				// tag remains
				tagsToDelete.tags.splice(foundIndex, 1);
			}
		});
		if (
			!this.assetTagsDirty ||
			(tagsToAdd.tags.length === 0 && tagsToDelete.tags.length === 0)
		) {
			this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
		} else {
			this.tagService
				.createAndDeleteAssetTags(
					this.model.assetId,
					tagsToAdd.tags.map(item => item.id),
					tagsToDelete.tags.map(item => item.assetTagId)
				)
				.subscribe(
					result => {
						this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
					},
					error => console.log('error when saving asset tags', error)
				);
		}
	}

	protected showAssetDetailView(assetClass: string, id: number) {
		// Close current dialog before open new one
		this.cancelCloseDialog();

		this.dialogService.open({
			componentFactoryResolver: this.componentFactoryResolver,
			component: AssetShowComponent,
			data: {
				assetId: id,
				assetClass: assetClass
			},
			modalConfiguration: {
				title: '', // data['common_assetName'] + ' ' + data['common_moveBundle'],
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe();
	}

	/***
	 * Close the Active Dialog
	 */
	protected cancelCloseDialog(): void {
		const assetEditComponent = <AssetEditComponent>this.parentDialog;
		assetEditComponent.onDismiss();
	}

	/**
	 * Validate if the current content of the Dependencies is correct
	 * @param {boolean} invalidForm
	 */
	protected onDependenciesValidationChange(validForm: boolean): void {
		this.isDependenciesValidForm = validForm;
	}

	/**
	 * Notify user there are changes
	 * @param {boolean} closeModal - specify as true if the modal should be closed (as opposed to reopening the asset summary modal)
	 */
	protected promptSaveChanges(closeModal: boolean): void {
		this.dialogService.confirm(
			this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'
			),
			this.translatePipe.transform(
				'GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'
			)
		).subscribe((data: any) => {
			if (data.confirm === DialogConfirmAction.CONFIRM) {
				if (closeModal) {
					this.cancelCloseDialog();
				} else {
					this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
				}
			} else {
				this.focusAssetModal();
			}
		});
	}

	/**
	 * On Cancel if there is changes notify user
	 */
	public onCancelEdit = (): void => {
		if ( this.assetTagsDirty || !ramdaEquals(this.initialModel, this.model)) {
			this.promptSaveChanges(this.model.assetId === undefined);
		} else {
			if (this.model.assetId) {
				this.showAssetDetailView(this.model.asset.assetClass.name, this.model.assetId);
			}
		}
	}

	/**
	 * On Close if there is changes notify user, then close the modal if prompt return is true
	 */
	protected onCloseEdit(): void {
		if (
			this.assetTagsDirty ||
			!ramdaEquals(this.initialModel, this.model)
		) {
			this.promptSaveChanges(true);
		} else {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Submit the form in case errors select the first invalid field
	 */
	public submitForm = (event) => {
		if (!this.form.onSubmit(event)) {
			this.focusFirstInvalidFieldInput();
		}
	}

	/**
	 * Focus the first control that belongs to the asset entry form and has an invalid status
	 */
	private focusFirstInvalidFieldInput(): void {
		jQuery(
			'form.asset-entry-form .tm-input-control.ng-invalid:first'
		).focus();
	}

	/**
	 allows to delete the application assets
	 */
	deleteAsset(assetId) {
		this.promptService
			.open(
				'Confirmation Required',
				'You are about to delete the selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel',
				'OK',
				'Cancel'
			)
			.then(success => {
				if (success) {
					this.assetExplorerService.deleteAssets([assetId]).subscribe(
						res => {
							if (res) {
								this.notifierService.broadcast({
									name: 'reloadCurrentAssetList',
								});
								this.activeDialog.dismiss();
							}
						},
						error => console.log(error)
					);
				}
			})
			.catch(error => console.log(error));
	}

	protected focusAssetModal(): void {
		setTimeout(() => jQuery('.modal-content').focus(), 500);
	}

	/**
	 * Focus a control matching by name
	 */
	protected focusControlByName(name): void {
		// delay selection until bootstrap effects are done
		setTimeout(() => {
			jQuery(
				`form.asset-entry-form .tm-input-control[name='${name}']:first`
			).focus();
		}, 600);
	}

	/**
	 * Added listener to last button to move the focus
	 * back to the begin of the form.
	 **/
	protected onFocusOutOfCancel(): void {
		let all = document.getElementsByClassName(
			'modal-content tds-angular-component-content'
		)[0];
		if (all === undefined) {
			return;
		}
		let focusable = all.querySelectorAll(
			'input, select, textarea, [tabindex]:not([tabindex="-1"])'
		);
		let firstFocusable = <HTMLElement>focusable[0];
		let lastFocusable = focusable[focusable.length - 1];
		lastFocusable.addEventListener('focusout', () => {
			firstFocusable.focus();
		});
	}
}
