import {Component, Inject, ViewChild, OnInit, OnDestroy} from '@angular/core';
import {ActivatedRoute, Router, NavigationEnd} from '@angular/router';
import { Observable } from 'rxjs';

import { UIDialogService } from '../../../../shared/services/ui-dialog.service';
import { PermissionService } from '../../../../shared/services/permission.service';
import {ViewGroupModel, ViewModel} from '../../model/view.model';
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
export class AssetExplorerViewShowComponent implements OnInit, OnDestroy {

	private currentId;
	private dataSignature: string;
	public fields: DomainModel[] = [];
	protected model: ViewModel = new ViewModel();
	protected domains: DomainModel[] = [];
	protected metadata: any = {};
	private lastSnapshot;
	protected navigationSubscription;

	@ViewChild('grid') grid: AssetExplorerViewGridComponent;
	@ViewChild('select') select: AssetExplorerViewSelectorComponent;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private dialogService: UIDialogService,
		private permissionService: PermissionService,
		private assetExplorerService: AssetExplorerService,
		private notifier: NotifierService) {

		this.metadata.tagList = this.route.snapshot.data['tagList'];
		this.fields = this.route.snapshot.data['fields'];
		this.domains = this.route.snapshot.data['fields'];
		this.model = this.route.snapshot.data['report'];
		this.dataSignature = JSON.stringify(this.model);
		this.reloadStrategy();
	}

	ngOnInit(): void {
		this.initialiseComponent();
	}

	/**
	 * Ensure the listener is not available after moving away from this component
	 */
	ngOnDestroy(): void {
		if (this.navigationSubscription) {
			this.navigationSubscription.unsubscribe();
		}
	}

	/**
	 * Reload Strategy keep listen To change to the route so we can reload whatever is inside the component
	 * Increase dramatically the Performance
	 */
	private reloadStrategy(): void {
		// The following code Listen to any change made on the rout to reload the page
		this.navigationSubscription = this.router.events.subscribe((event: any) => {
			if (event.snapshot && event.snapshot.data && event.snapshot.data.fields) {
				this.lastSnapshot = event.snapshot;
			}
			// If it is a NavigationEnd event re-initalise the component
			if (event instanceof NavigationEnd) {
				console.log(event);
				if (this.currentId !== this.lastSnapshot.params.id) {
					this.metadata.tagList = this.lastSnapshot.data['tagList'];
					this.fields = this.lastSnapshot.data['fields'];
					this.domains = this.lastSnapshot.data['fields'];
					this.model = this.lastSnapshot.data['report'];
					this.dataSignature = JSON.stringify(this.model);
					this.initialiseComponent();
				}
			}
		});
	}

	/**
	 * Calls every time the Component is recreated by calling same URL
	 */
	private initialiseComponent(): void {
		this.currentId = this.model.id;
		this.notifier.broadcast({
			name: 'notificationHeaderTitleChange',
			title: this.model.name
		});

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
		this.assetExplorerService.query(this.model.id, params).subscribe(result => {
			this.grid.apply(result);
			jQuery('[data-toggle="popover"]').popover();
		}, err => console.log(err));
	}

	protected onEdit(): void {
		if (this.isEditAvailable()) {
			this.router.navigate(['asset', 'views', this.model.id, 'edit']);
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
		const selectedData = this.select.data.filter(x => x.name === 'Favorites')[0];
		if (this.isSaveAsAvailable()) {
			this.dialogService.open(AssetExplorerViewSaveComponent, [
				{ provide: ViewModel, useValue: this.model },
				{ provide: ViewGroupModel, useValue: selectedData }
			]).then(result => {
				this.model = result;
				this.dataSignature = JSON.stringify(this.model);
				setTimeout(() => {
					this.router.navigate(['asset', 'views', this.model.id, 'edit']);
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