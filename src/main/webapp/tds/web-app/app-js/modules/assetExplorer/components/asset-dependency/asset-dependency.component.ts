import { Component, Inject } from '@angular/core';
import {forkJoin} from 'rxjs/observable/forkJoin';

import { UIExtraDialog } from '../../../../shared/services/ui-dialog.service';
import { DependecyService } from '../../service/dependecy.service';
import {DependencyChange, DependencyType} from './model/asset-dependency.model';
import {BulkActions} from '../../../../shared/components/bulk-change/model/bulk-change.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: 'asset-dependency',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/asset-dependency/asset-dependency.component.html',
	styles: [`
        table {
            margin-top: 10px;
        }

        .modal-body th {
            background: white !important;
        }

        .modal-body {
            padding-left: 10px;
            padding-right: 10px;
        }

        .modal-body table {
            border-spacing: inherit !important;
        }

        .modal-body table tr th,
        .modal-body table tr td {
            border-top: 0px !important;
        }

        td.lbl-asset-dependency-direction,
        form.dependency-edit-fields td.legend-fields {
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

        .half-size {
            width: 60%;
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
export class AssetDependencyComponent extends UIExtraDialog {
	protected dependencyA: any;
	protected dependencyB: any;
	protected isEditing: boolean;
	protected frequencyList: string[];
	protected typeList: string[];
	protected statusList: string[];
	protected directionList: string[];
	protected editedDependencies = this.getInitialEditDependencies();
	protected DependencyType = DependencyType;

	constructor(
		@Inject('ASSET_DEP_MODEL') private assetDependency: any,
		private assetService: DependecyService,
		private promptService: UIPromptService,
		private translatePipe: TranslatePipe) {
		super('#assetDependency');

		this.isEditing = false;

		this.dependencyA = assetDependency.assetA && assetDependency.assetA.dependency || null;
		this.dependencyB = assetDependency.assetB && assetDependency.assetB.dependency || null ;

		// while dateCreated and lastUpdated are not coming from server inside of assetDependency.assetX.dependency
		// we need to get them from assetDependency.assetX
		this.dependencyA = this.getDependencyWithDates(this.dependencyA, assetDependency.assetA);
		this.dependencyB = this.getDependencyWithDates(this.dependencyB, assetDependency.assetB);

		this.frequencyList = this.assetDependency.dataFlowFreq;
		this.typeList = this.assetDependency.dependencyType;
		this.statusList = this.assetDependency.dependencyStatus;
		this.directionList = this.assetDependency.directionList;
	}

	/**
	 * Add to the dependency provided as argument the dateCreated and lastUpdated field of the asset parameter
	 * @param {any} dependency Object containing the dependency to change
	 * @param {any} asset Asset containing the dateCreated and lastUpdated fields
	 * @return {any) Original dependency modified adding the extra date field
	 */
	private getDependencyWithDates(dependency: any, asset: any): any {
		if (!asset || !dependency) {
			return dependency;
		}

		const { dateCreated, lastUpdated }  = asset;
		return Object.assign({}, dependency, { dateCreated, lastUpdated });
	}

	/**
	 * Close the window
	 * @return {void)
	 */
	protected cancelCloseDialog(): void {
		this.dismiss();
	}

	/**
	 * Set the flag to indicate if the view is in edit model
	 * @param {boolean} enabled Flag indicating the edit mode
	 * @return {void)
	 */
	protected setEditMode(enabled: boolean): void {
		this.isEditing = enabled;
	}

	/**
	 * Save the dependencies changes, based on the flags that indicate if the dependency has changes
	 * @return {void)
	 */
	protected saveChanges(): void {
		let updates = [];

		if (this.editedDependencies.aDependencyHasChanged) {
			updates.push(this.assetService.updateDependency({dependency: {...this.dependencyA,  ...this.editedDependencies.dependencies.a}}));
		}

		if (this.editedDependencies.bDependencyHasChanged) {
			updates.push(this.assetService.updateDependency({dependency: {...this.dependencyB, ...this.editedDependencies.dependencies.b}}));
		}

		forkJoin(updates)
			.subscribe((result: any[]) => {
				if (this.editedDependencies.aDependencyHasChanged) {
					this.dependencyA = { ...this.dependencyA, ...this.editedDependencies.dependencies.a };
				}
				if (this.editedDependencies.bDependencyHasChanged) {
					this.dependencyB = { ...this.dependencyB, ...this.editedDependencies.dependencies.b };
				}
				this.setEditMode(false);
				this.editedDependencies = this.getInitialEditDependencies();
		});
	}

	/**
	 * Cancel the edit changes
	 * @return {void)
	 */
	protected cancelEdit(): void {
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
	 * Delete a dependency previous confirmation
	 * @param {DependencyType} dependencyType Type of dependency to be deleted
	 * @return {void)
	 */
	protected onDeleteDependency(dependencyType: DependencyType): void {
		this.confirmDelete()
			.then((result) => {
				if (result) {
					console.log(result);
					const dependency = dependencyType === DependencyType.dependencyA ? this.dependencyA : this.dependencyB;

					const dependencyChange = {
						assetId: dependency.asset.id,
						dependencyId: dependency.id
					};

					this.assetService.deleteDependency(dependencyChange)
						.subscribe((result) => {
							if (result) {
								if (dependencyType === DependencyType.dependencyA ) {
									this.cancelCloseDialog();
								} else {
									this.dependencyB = null;
								}
							}
						}, (error) => console.log('Error:', error));
				}
			});
	}

	/**
	 * confirmation popup. Launched when user wants to delete a dependency
	 * @returns {Promise<boolean>}
	 */
	private confirmDelete(): Promise<boolean> {
		const message = this.translatePipe
			.transform('DEPENDENCIES.CONFIRM_DELETE_DEPENDENCY');

		return this.promptService.open(this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
					message, this.translatePipe.transform('GLOBAL.CONFIRM'),
					this.translatePipe.transform('GLOBAL.CANCEL'));
	}
}
