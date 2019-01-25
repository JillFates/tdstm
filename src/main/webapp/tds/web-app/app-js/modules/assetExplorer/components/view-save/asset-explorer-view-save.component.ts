import {AfterViewInit, Component, Inject, OnInit} from '@angular/core';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ViewModel, ViewGroupModel } from '../../model/view.model';
import { AssetExplorerService } from '../../../assetManager/service/asset-explorer.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';
import {Permission} from '../../../../shared/model/permission.model';
@Component({
	selector: 'asset-explorer-view-save',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-save/asset-explorer-view-save.component.html'
})
export class AssetExplorerViewSaveComponent implements AfterViewInit {
	model: ViewModel;
	private isUnique = true;
	constructor(
		model: ViewModel,
		private favorites: ViewGroupModel,
		private assetExpService: AssetExplorerService,
		public activeDialog: UIActiveDialogService,
		private permissionService: PermissionService,
		private notifier: NotifierService) {

		this.model = { ...model };
		if (this.model.id) {
			this.model.name = `Copy of ${this.model.name}`;
			this.model.id = null;
			this.model.isSystem = false;
			this.model.isFavorite = false;
		}
		if (this.model.isSystem) {
			this.model.isShared = false;
		}
	}

	ngAfterViewInit(): void {
		if (this.model.name) {
			this.validateUniquenessDataViewByName(this.model.name);
		}
	}

	protected cancelCloseDialog(): void {
		this.activeDialog.dismiss();
	}

	protected confirmCloseDialog() {
		this.assetExpService.saveReport(this.model)
			.subscribe(result => result && this.activeDialog.close(result),
			error => this.activeDialog.dismiss(error));
	}

	protected isValid(): boolean {
		return this.model.name && this.model.name.trim() !== '' && this.isUnique;
	}

	/**
	 * Disable the System View checkbox if the user does not have the proper permission
	 * @returns {boolean}
	 */
	private isSystemCreatePermitted(): boolean {
		return this.permissionService.hasPermission(Permission.AssetExplorerSystemCreate);
	}

	/**
	 * Should turn isShared to false when isSystem is selected as true.
	 */
	private onIsSystemChange(): void {
		if (this.model.isSystem && this.model.isShared) {
			this.model.isShared = false;
		}
	}

	protected onFavorite() {
		if (this.model.isFavorite) {
			this.model.isFavorite = false;
			if (this.model.id) {
				const reportIndex = this.favorites.items.findIndex(x => x.id === this.model.id);
				if (reportIndex !== -1) {
					this.favorites.items.splice(reportIndex, 1);
				}
			}
		} else {
			if (this.assetExpService.hasMaximumFavorites(this.favorites.items.length + 1)) {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Maximum number of favorite data views reached.'
				});
			} else {
				this.model.isFavorite = true;
			}
		}

	}

	protected onNameChanged() {
		this.validateUniquenessDataViewByName(this.model.name);
	}

	private validateUniquenessDataViewByName(dataViewName = '') {
		if (!dataViewName.trim()) {
			// handle empty string
			this.isUnique = false;
		} else {
			this.assetExpService.validateUniquenessDataViewByName(dataViewName)
				.subscribe((isUnique: boolean) => this.isUnique = isUnique,
					(error) => console.log(error.message));
		}

	}
}