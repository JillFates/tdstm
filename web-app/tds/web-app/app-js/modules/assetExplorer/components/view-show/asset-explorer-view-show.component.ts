import { Component, Inject, ViewChild, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import { Observable } from 'rxjs';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';

import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ViewModel } from '../../model/view.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { Permission } from '../../../../shared/model/permission.model';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';

import { AssetExplorerViewGridComponent } from '../view-grid/asset-explorer-view-grid.component';
import { AssetExplorerViewSelectorComponent } from '../view-selector/asset-explorer-view-selector.component';
import { AssetExplorerViewSaveComponent } from '../view-save/asset-explorer-view-save.component';
import { AssetExplorerViewExportComponent } from '../view-export/asset-explorer-view-export.component';
import { AssetQueryParams } from '../../model/asset-query-params';
import { DomainModel } from '../../../fieldSettings/model/domain.model';
import { AssetExportModel } from '../../model/asset-export-model';
import {TagModel} from '../../../assetTags/model/tag.model';

declare var jQuery: any;
@Component({
	selector: 'asset-explorer-view-show',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-show/asset-explorer-view-show.component.html'
})
export class AssetExplorerViewShowComponent implements OnInit {
	private dataSignature: string;
	model: ViewModel;
	domains: DomainModel[] = [];
	protected metadata: any = {};

	@ViewChild('grid') grid: AssetExplorerViewGridComponent;
	@ViewChild('select') select: AssetExplorerViewSelectorComponent;

	constructor(
		private route: ActivatedRoute,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private assetExplorerService: AssetExplorerService,
		private notifier: NotifierService) {
			// @Inject('report') report: Observable<ViewModel>
			// @Inject('fields') fields: Observable<DomainModel[]>
			// @Inject('tagList') tagList: Observable<Array<TagModel>>
			// tagList.subscribe( result => this.metadata.tagList = result);
			/*Observable.zip(fields, report).subscribe((result: [DomainModel[], ViewModel]) => {
				this.domains = result[0];
				this.model = result[1];
				this.dataSignature = JSON.stringify(this.model);
				// TODO: STATE SERVICE GO
				// this.stateService.$current.data.page.title = this.model.name;
				document.title = this.model.name;
			}, (err) => console.log(err));*/
	}

	ngOnInit(): void {
		// this.grid.state.sort = [
		// 	{
		// 		field: `${this.model.schema.sort.domain}_${this.model.schema.sort.property}`,
		// 		dir: this.model.schema.sort.order === 'a' ? 'asc' : 'desc'
		// 	}
		// ];
		// this.onQuery();
	}

	protected onQuery(): void {
		let params = {
			offset: this.grid.state.skip,
			limit: this.grid.state.take,
			sortDomain: this.model.schema.sort.domain,
			sortProperty: this.model.schema.sort.property,
			sortOrder: this.model.schema.sort.order,
			filters: {
				domains: this.model.schema.domains,
				columns: this.model.schema.columns
			}
		};
		if (this.grid.justPlanning) {
			params['justPlanning'] = true;
		}
		this.assetExplorerService.query(this.model.id, params).subscribe(result => {
			this.grid.apply(result);
			jQuery('[data-toggle="popover"]').popover();
		}, err => console.log(err));
	}

	protected onEdit(): void {
		if (this.isEditAvailable()) {
			// TODO: STATE SERVICE GO
			// this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: this.model.id });
		}
	}

	protected onSave() {
		if (this.isSaveAvailable()) {
			this.assetExplorerService.saveReport(this.model)
				.subscribe(result => {
					this.dataSignature = JSON.stringify(this.model);
				});
		}
	}

	protected onSaveAs(): void {
		if (this.isSaveAsAvailable()) {
			this.dialogService.open(AssetExplorerViewSaveComponent, [
				{ provide: ViewModel, useValue: this.model },
				{ provide: 'favorites', useValue: this.select.data.filter(x => x.name === 'Favorites')[0] }
			]).then(result => {
				this.model = result;
				this.dataSignature = JSON.stringify(this.model);
				setTimeout(() => {
					// TODO: STATE SERVICE GO
					// this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: this.model.id });
				});
			}).catch(result => {
				console.log('error');
			});
		}
	}

	protected isDirty(): boolean {
		let result = this.dataSignature !== JSON.stringify(this.model);
		return result;
	}

	protected onExport(): void {
		let assetExportModel: AssetExportModel = {
			assetQueryParams: this.getQueryParamsForExport(),
			domains: this.domains,
			queryId: this.model.id,
			viewName: this.model.name
		};

		this.dialogService.open(AssetExplorerViewExportComponent, [
			{ provide: AssetExportModel, useValue: assetExportModel }
		]).then(result => {
			console.log(result);
		}).catch(result => {
			console.log('error:', result);
		});
	}

	protected onFavorite(): void {
		if (this.model.isFavorite) {
			this.assetExplorerService.deleteFavorite(this.model.id)
				.subscribe(d => {
					this.model.isFavorite = false;
					this.select.loadData();
				});
		} else {
			if (this.assetExplorerService.hasMaximumFavorites(this.select.data.filter(x => x.name === 'Favorites')[0].items.length + 1)) {
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Maximum number of favorite data views reached.'
				});
			} else {
				this.assetExplorerService.saveFavorite(this.model.id)
					.subscribe(d => {
						this.model.isFavorite = true;
						this.select.loadData();
					});
			}
		}
	}

	/**
	 * Prepare the Params for the Query with the current UI configuration
	 * Params should 'limit' to total number of total from the gridData since we want to export ALL DATA, not
	 * the configured pagination results.
	 * @returns {AssetQueryParams}
	 */
	private getQueryParamsForExport(): AssetQueryParams {
		let assetQueryParams: AssetQueryParams = {
			offset: this.grid.gridData ? 0 : this.grid.state.skip,
			limit: this.grid.gridData ? this.grid.gridData.total : this.grid.state.take,
			forExport: true,
			sortDomain: this.model.schema.sort.domain,
			sortProperty: this.model.schema.sort.property,
			sortOrder: this.model.schema.sort.order,
			filters: {
				domains: this.model.schema.domains,
				columns: this.model.schema.columns
			}
		};
		if (this.grid.justPlanning) {
			assetQueryParams['justPlanning'] = this.grid.justPlanning;
		}

		return assetQueryParams;
	}

	protected isSaveAvailable(): boolean {
		return this.assetExplorerService.isSaveAvailable(this.model);
	}

	protected isSaveAsAvailable(): boolean {
		return this.model.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
			this.permissionService.hasPermission(Permission.AssetExplorerSaveAs);
	}

	protected isSystemSaveAvailable(edit): boolean {
		return edit ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs);
	}

	protected isEditAvailable(): boolean {
		return this.model.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit);
	}

}