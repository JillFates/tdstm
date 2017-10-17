import { Component, Inject, ViewChild, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { StateService } from '@uirouter/angular';
import { AssetExplorerStates } from '../../asset-explorer-routing.states';

import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import { ViewModel } from '../../model/view.model';
import { AssetExplorerService } from '../../service/asset-explorer.service';
import { Permission } from '../../../../shared/model/permission.model';

import { AssetExplorerViewGridComponent } from '../view-grid/asset-explorer-view-grid.component';
import { AssetExplorerViewSaveComponent } from '../view-save/asset-explorer-view-save.component';
import { AssetExplorerViewExportComponent } from '../view-export/asset-explorer-view-export.component';
import { AssetQueryParams } from '../../model/asset-query-params';
import { DomainModel } from '../../../fieldSettings/model/domain.model';
import { AssetExportModel } from '../../model/asset-export-model';

@Component({
	selector: 'asset-explorer-view-show',
	templateUrl: '../tds/web-app/app-js/modules/assetExplorer/components/view-show/asset-explorer-view-show.component.html'
})
export class AssetExplorerViewShowComponent implements OnInit {
	model: ViewModel;
	domains: DomainModel[] = [];
	@ViewChild('grid') grid: AssetExplorerViewGridComponent;

	constructor(
		@Inject('report') report: Observable<ViewModel>,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private assetService: AssetExplorerService,
		private stateService: StateService,
		@Inject('fields') fields: Observable<DomainModel[]>) {
		report.subscribe(
			(result) => {
				this.model = result;
			},
			(err) => console.log(err));
		Observable.zip(fields, report).subscribe((result: [DomainModel[], ViewModel]) => {
			this.domains = result[0];
		}, (err) => console.log(err));
	}

	ngOnInit(): void {
		this.grid.state.sort = [
			{
				field: `${this.model.schema.sort.domain}_${this.model.schema.sort.property}`,
				dir: this.model.schema.sort.order === 'a' ? 'asc' : 'desc'
			}
		];
		this.onQuery();
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
		this.assetService.query(this.model.id, params).subscribe(result => {
			this.grid.apply(result);
		}, err => console.log(err));
	}

	protected onEdit(): void {
		if (this.isEditAvailable()) {
			this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: this.model.id });
		}
	}

	protected onSaveAs(): void {
		if (this.isSaveAsAvailable()) {
			this.dialogService.open(AssetExplorerViewSaveComponent, [
				{ provide: ViewModel, useValue: this.model }
			]).then(result => {
				this.model = result;
				setTimeout(() => {
					this.stateService.go(AssetExplorerStates.REPORT_EDIT.name, { id: this.model.id });
				});
			}).catch(result => {
				console.log('error');
			});
		}
	}

	protected onExport(): void {
		let assetExportModel: AssetExportModel = {
			assetQueryParams: this.getQueryParams(),
			domains: this.domains,
			previewMode: false,
			queryId: this.model.id,
			totalData: this.grid.gridData.total,
			searchExecuted: true,
		};

		this.dialogService.open(AssetExplorerViewExportComponent, [
			{ provide: AssetExportModel, useValue: assetExportModel }
		]).then(result => {
			console.log(result);
		}).catch(result => {
			console.log('error');
		});
	}

	protected onFavorite(): void {
		if (this.model.isFavorite) {
			this.assetService.deleteFavorite(this.model.id)
				.subscribe(d => {
					this.model.isFavorite = false;
				});
		} else {
			this.assetService.saveFavorite(this.model.id)
				.subscribe(d => {
					this.model.isFavorite = true;
				});
		}
	}

	/**
	 * Prepare the Params for the Query with the current UI configuration
	 * @returns {AssetQueryParams}
	 */
	private getQueryParams(): AssetQueryParams {
		let assetQueryParams = {
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
			assetQueryParams['justPlanning'] = this.grid.justPlanning;
		}

		return assetQueryParams;
	}

	protected isSaveAsAvailable(): boolean {
		return this.model.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemSaveAs) :
			this.permissionService.hasPermission(Permission.AssetExplorerSaveAs);
	}

	protected isEditAvailable(): boolean {
		return this.model.isSystem ?
			this.permissionService.hasPermission(Permission.AssetExplorerSystemEdit) :
			this.permissionService.hasPermission(Permission.AssetExplorerEdit);
	}

}