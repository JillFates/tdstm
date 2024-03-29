// Angular
import {Component, Inject, Input, OnInit, ViewChild} from '@angular/core';
// Component
import {AssetDependencyEditComponent} from './edit/asset-dependency-edit.component';
// Model
import {DependencyChange, DependencyType} from './model/asset-dependency.model';
import {Permission} from '../../../../shared/model/permission.model';
// Service
import {DependecyService} from '../../service/dependecy.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {Dialog, DialogButtonType, DialogConfirmAction, DialogService} from 'tds-component-library';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Other
import {forkJoin} from 'rxjs/observable/forkJoin';
import {Observable} from 'rxjs';
import * as R from 'ramda';
import set = Reflect.set;

@Component({
	selector: 'asset-dependency',
	templateUrl: 'asset-dependency.component.html',
	styles: [`
        td.lbl-asset-dependency-direction,
        form.dependency-edit-fields td.legend-fields {
			padding: 2px 0;
            font-weight: bold;
            color: green;
            font: 12px helvetica, arial, sans-serif !important;
        }

        form.dependency-edit-fields table {
            border: 0px;
        }

        form.dependency-edit-fields div.form-group {
            height: 24px;
            font: 12px helvetica, arial, sans-serif !important;
        }

        form.dependency-edit-fields label.control-label {
            text-align: left;
            font-weight: inherit;
        }

        form.dependency-edit-fields label.control-label-title {
            font-weight: bold;
        }

        .btn-default {
            background-color: #f4f4f4;
            color: #444;
            border-color: #ddd;
        }

        .delete-property {
            text-decoration: line-through;
        }

		.separator-table {
            border-top: 2px solid #f4f4f4 !important;
            margin-top: 10px !important;
        }

        .modal-body select,
        .modal-body input.form-control {
            width: 150px;
        }`]
})
export class AssetDependencyComponent extends Dialog implements OnInit {
	@Input() data: any;

	@ViewChild('assetDependencyEdit', {
		read: AssetDependencyEditComponent,
		static: false,
	})
	assetDependencyEdit: AssetDependencyEditComponent;

	protected frequencyList: string[];
	protected typeList: string[];
	protected statusList: string[];
	protected directionList: string[];
	protected editedDependencies = this.getInitialEditDependencies();
	protected DependencyType = DependencyType;
	public dependencyA: any;
	public dependencyB: any;
	public isEditing: boolean;

	public assetDependency: any;

	constructor(
		private dialogService: DialogService,
		private assetService: DependecyService,
		private translatePipe: TranslatePipe,
		private permissionService: PermissionService) {
		super();
	}

	ngOnInit(): void {
		// Sub Objects are not being created, just copy
		this.assetDependency = R.clone(this.data.assetDependency);

		this.buttons.push({
			name: 'edit',
			icon: 'pencil',
			tooltipText: 'Edit',
			show: () => this.isEditAvailable(),
			active: () => this.isEditing,
			type: DialogButtonType.ACTION,
			action: this.changeToEditMode.bind(this)
		});

		this.buttons.push({
			name: 'save',
			icon: 'floppy',
			tooltipText: 'Save',
			show: () => this.isEditing,
			disabled: () => !this.editedDependencies.dependencies,
			type: DialogButtonType.ACTION,
			action: this.saveChanges.bind(this)
		});

		this.buttons.push({
			name: 'delete',
			icon: 'trash',
			tooltipText: 'Delete',
			show: () => this.isDeleteAvailable() && this.dependencyA && !this.dependencyB,
			type: DialogButtonType.ACTION,
			action: this.onDeleteDependencyA.bind(this)
		});

		this.buttons.push({
			name: 'close',
			icon: 'ban',
			tooltipText: 'Close',
			show: () => !this.isEditing,
			type: DialogButtonType.ACTION,
			action: this.cancelCloseDialog.bind(this)
		});

		this.buttons.push({
			name: 'cancel',
			icon: 'ban',
			tooltipText: 'Cancel',
			show: () => this.isEditing,
			type: DialogButtonType.ACTION,
			action: this.cancelEdit.bind(this)
		});

		this.isEditing = false;

		this.dependencyA = this.assetDependency.assetA && this.assetDependency.assetA.dependency || null;
		this.dependencyB = this.assetDependency.assetB && this.assetDependency.assetB.dependency || null;

		// while dateCreated and lastUpdated are not coming from server inside of assetDependency.assetX.dependency
		// we need to get them from assetDependency.assetX
		this.dependencyA = this.getDependencyWithDates(this.dependencyA, this.assetDependency.assetA);
		this.dependencyB = this.getDependencyWithDates(this.dependencyB, this.assetDependency.assetB);

		this.frequencyList = this.assetDependency.dataFlowFreq;
		this.typeList = this.assetDependency.dependencyType;
		this.statusList = this.assetDependency.dependencyStatus;
		this.directionList = this.assetDependency.directionList;

		setTimeout(() => {
			this.setTitle(this.getModalTitle());
		});
	}

	/**
	 * Add to the dependency provided as argument the created and updated info of the asset parameter
	 * @param {any} dependency Object containing the dependency to change
	 * @param {any} asset Asset containing the dateCreated and lastUpdated fields
	 * @return {any) Original dependency modified adding the extra date field
	 */
	private getDependencyWithDates(dependency: any, asset: any): any {
		if (!asset || !dependency) {
			return dependency;
		}

		const {dateCreated, lastUpdated, createdBy, updatedBy} = asset;
		return Object.assign({}, dependency, {dateCreated, lastUpdated, createdBy, updatedBy});
	}

	/**
	 * Close the window
	 * @return {void)
	 */
	public cancelCloseDialog(): void {
		if (this.hasChanges()) {
			this.promptForSave().subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					super.onCancelClose();
				}
			});
		} else {
			super.onCancelClose();
		}
	}

	/**
	 * Launch the prompt modal asking for saving changes
	 *
	 */
	private promptForSave(): Observable<any> {
		return this.dialogService.confirm(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE')
		);
	}

	/**
	 * Change to Edit Mode
	 */
	private changeToEditMode(): void {
		this.setEditMode(true);
	}

	/**
	 * Set the flag to indicate if the view is in edit model
	 * @param {boolean} enabled Flag indicating the edit mode
	 * @return {void)
	 */
	protected setEditMode(enabled: boolean): void {
		this.isEditing = enabled;
		this.setTitle(this.getModalTitle());
	}

	/**
	 * Save the dependencies changes, based on the flags that indicate if the dependency has changes
	 * @return {void)
	 */
	protected saveChanges(): void {
		let updates = [];

		if (this.editedDependencies.aDependencyHasChanged) {
			updates.push(this.assetService.updateDependency({dependency: {...this.dependencyA, ...this.editedDependencies.dependencies.a}}));
		}

		if (this.editedDependencies.bDependencyHasChanged) {
			updates.push(this.assetService.updateDependency({dependency: {...this.dependencyB, ...this.editedDependencies.dependencies.b}}));
		}

		forkJoin(updates)
			.subscribe((result: any[]) => {
				if (this.editedDependencies.aDependencyHasChanged) {
					this.dependencyA = {...this.dependencyA, ...this.editedDependencies.dependencies.a};
				}
				if (this.editedDependencies.bDependencyHasChanged) {
					this.dependencyB = {...this.dependencyB, ...this.editedDependencies.dependencies.b};
				}
				this.setEditMode(false);
				this.editedDependencies = this.getInitialEditDependencies();
			});
	}

	/**
	 * Cancel the edit changes, if there is changes prompt for saving
	 * @return {void)
	 */
	protected cancelEdit(): void {
		if (this.hasChanges()) {
			this.promptForSave().subscribe((data: any) => {
				if (data.confirm === DialogConfirmAction.CONFIRM) {
					this.changeToViewMode();
				}
			});
		} else {
			this.changeToViewMode();
		}
	}

	/**
	 * Se the current view mode to read mode
	 */
	private changeToViewMode(): void {
		this.setEditMode(false);
		this.editedDependencies = this.getInitialEditDependencies();
	}

	/**
	 * On change a dependency, grab the dependency value and set the flag indicating which dependency has changed
	 * @param {DependencyChange} change Object containing the dependency change
	 * @return {void)
	 */
	protected onChangeDependencies(change: DependencyChange): void {
		if (change.dependencies) {
			if (change.type === DependencyType.dependencyA) {
				this.editedDependencies.aDependencyHasChanged = true;
			} else {
				this.editedDependencies.bDependencyHasChanged = true;
			}
			this.editedDependencies.dependencies = change.dependencies;
		}
	}

	/**
	 * Set the initial dependencies values
	 * @return {void)
	 */
	private getInitialEditDependencies(): any {
		return {
			dependencies: null,
			aDependencyHasChanged: false,
			bDependencyHasChanged: false,
		}
	}

	/**
	 * Return the name of the person name who created or update the dependency
	 * @param {any}  person  Person who created or updated the dependency
	 * @returns {string}
	 */
	protected getPersonName(person: any): string {
		const {firstName, lastName = ''} = person;

		return `${firstName} ${lastName}`;
	}

	/**
	 * When there is only one Dependency, we delete the first one
	 */
	private onDeleteDependencyA(): void {
		this.onDeleteDependency(DependencyType.dependencyA);
	}
	/**
	 * Delete a dependency previous confirmation
	 * @param {DependencyType} dependencyType Type of dependency to be deleted
	 * @return {void)
	 */
	protected onDeleteDependency(dependencyType: DependencyType): void {
		this.confirmDelete().subscribe((data: any) => {
			if (data.confirm === DialogConfirmAction.CONFIRM) {
				const dependency = dependencyType === DependencyType.dependencyA ? this.dependencyA : this.dependencyB;

				const dependencyChange = {
					assetId: dependency.asset.id,
					dependencyId: dependency.id
				};

				this.assetService.deleteDependency(dependencyChange)
					.subscribe((result) => {
						if (result) {
							if (dependencyType === DependencyType.dependencyA) {
								super.onCancelClose({delete: true});
							} else {
								this.dependencyB = null;
							}
						}
					}, (error) => console.log('Error:', error));
			}
		});
	}

	/**
	 * Determines if there is changes in place
	 */
	private hasChanges(): boolean {
		return this.editedDependencies.dependencies;
	}

	/**
	 * confirmation popup. Launched when user wants to delete a dependency
	 * @returns {Promise<boolean>}
	 */
	private confirmDelete(): Observable<any> {
		const message = this.translatePipe.transform('DEPENDENCIES.CONFIRM_DELETE_DEPENDENCY');

		return this.dialogService.confirm(
			this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONTINUE_WITH_CHANGES'),
			message);
	}

	public isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDependencyEdit);
	}

	public isDeleteAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.AssetDependencyDelete);
	}

	/**
	 * User Dismiss Changes
	 */
	public onDismiss(): void {
		this.cancelCloseDialog();
	}

	/**
	 * Based on action returns the corresponding title
	 * @returns {string}
	 */
	private getModalTitle(): string {
		if (this.isEditing) {
			setTimeout(() => {
				this.assetDependencyEdit.frequencyListA.focus();
			}, 1000);
		}
		return (!this.isEditing
				? this.translatePipe.transform('ASSET_EXPLORER.DEPENDENCY_DETAIL')
				: this.translatePipe.transform('ASSET_EXPLORER.DEPENDENCY_EDIT')
		);
	}
}
