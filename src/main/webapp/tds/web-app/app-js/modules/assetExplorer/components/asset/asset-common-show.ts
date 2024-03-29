// Angular
import {OnInit, ComponentFactoryResolver, ViewChild} from '@angular/core';
// Service
import {NotifierService} from '../../../../shared/services/notifier.service';
import {DependecyService} from '../../service/dependecy.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {WindowService} from '../../../../shared/services/window.service';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {AssetCommonDiagramHelper} from './asset-common-diagram.helper';
import {ModelService} from '../../service/model.service';
// Model
import {TagModel} from '../../../assetTags/model/tag.model';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {DialogService, ModalSize} from 'tds-component-library';
import {DependentType} from '../dependents/model/support-dependents-columns.model';
// Component
import {AssetShowComponent} from './asset-show.component';
import {AssetDependencyComponent} from '../asset-dependency/asset-dependency.component';
import {AssetCommonHelper} from './asset-common-helper';
import {DependentsComponent} from '../dependents/dependents.component';
// Other
import {ReplaySubject} from 'rxjs';
import {Diagram, Layout, Link} from 'gojs';

declare var jQuery: any;

export class AssetCommonShow implements OnInit {
	@ViewChild('dependentsComponent', {static: false}) dependentsComponent: DependentsComponent;

	protected userDateFormat: string;
	protected userTimeZone: string;
	protected mainAsset;
	protected assetTags: Array<TagModel>;
	protected isHighField = AssetCommonHelper.isHighField;
	protected showDetails = false;
	protected readMore = false;
	protected currentUser: any;
	protected commentCount: number;
	protected taskCount: number;
	protected dependencies = {
		asset: {},
		supports: [],
		dependents: []
	};
	protected data$: ReplaySubject<IDiagramData> = new ReplaySubject(1);
	protected diagramLayout$: ReplaySubject<Layout> = new ReplaySubject(1);
	protected linkTemplate$: ReplaySubject<Link> = new ReplaySubject(1);

	constructor(
		protected componentFactoryResolver: ComponentFactoryResolver,
		protected dialogService: DialogService,
		protected assetService: DependecyService,
		protected assetExplorerService: AssetExplorerService,
		protected notifierService: NotifierService,
		protected userContextService: UserContextService,
		protected windowService: WindowService,
		protected architectureGraphService: ArchitectureGraphService,
		private parentDialog: any,
		private metadata: any
	) {
			jQuery('[data-toggle="popover"]').popover();
			this.userContextService.getUserContext()
				.subscribe((userContext: UserContextModel) => {
					this.userDateFormat = userContext.dateFormat;
					this.userTimeZone = userContext.timezone;
					this.currentUser = userContext.user;
				});
			this.dependencies.asset = metadata.asset;
			this.dependencies.supports = metadata.supports;
			this.dependencies.dependents = metadata.dependents;
	}

	/**
	 * Initiates The Injected Component
	 */
	ngOnInit(): void {
		jQuery('[data-toggle="popover"]').popover();
	}

	/***
	 * Close the Active Dialog
	 */
	protected cancelCloseDialog(): void {
		const assetShowComponent = <AssetShowComponent>this.parentDialog;
		assetShowComponent.onDismiss();
	}

	/**
	 * Open an Asset from the inner Dependency View Tables
	 * @param event
	 */
	protected onAssetShowFromDependency(event: any) {
		this.showAssetDetailView(event.assetClass, event.id);
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
				title: '&nbsp;',
				draggable: true,
				modalSize: ModalSize.CUSTOM,
				modalCustomClass: 'custom-asset-modal-dialog'
			}
		}).subscribe();
	}

	/**
	 * Open a Dependency from the inner Dependency View Tables
	 * @param event
	 */
	protected onDependencyShowFromDependency(event: any) {
		this.showDependencyView(event.type, event.assetId, event.dependencyAsset, event.dataItem);
	}

	/**
	 * Show the dependency dialog, in case a dependency is removed update the corresponding grid
	 * @param type Dependency type (support/ dependent on)
	 * @param assetId Main Asset id
	 * @param dependencyAsset  id of the asset dependent
	 * @param rowId Id fo the row to be deleted
	 */
	showDependencyView(type: string, assetId: number, dependencyAsset: number, dataItem: any = null) {
		this.assetService.getDependencies(assetId, dependencyAsset)
			.subscribe((result) => {

				this.dialogService.open({
					componentFactoryResolver: this.componentFactoryResolver,
					component: AssetDependencyComponent,
					data: {
						assetDependency: result
					},
					modalConfiguration: {
						title: 'Dependency Detail',
						draggable: true,
						modalSize: ModalSize.MD,
					}
				}).subscribe( (data) => {
					if (data && data.delete) {
						this.deleteDependencyRowUpdateCounter(type, assetId, dependencyAsset, dataItem);
					}
				});
			}, (error) => console.log(error));
	}

	/**
	 * Delete the dependency row and update the corresponding counter
	 * Supports and Dependent Also get Updated
	 */
	private deleteDependencyRowUpdateCounter(type: string, assetId: number, dependencyAssetId: number, dataItem: any): void {
		if (type === DependentType.SUPPORT) {
			// Let's remove it from the Header
			let index = this.metadata.supports.findIndex((support: any) => {
				return support.assetId === assetId;
			});
			if (index !== -1) {
				this.metadata.supports.splice(index, 1);
				if (dataItem !== null) {
					this.dependentsComponent.gridSupportsData.removeDataItem(dataItem);
				}
			}
		} else if (type === DependentType.DEPENDENT) {
			let index = this.metadata.dependents.findIndex((dependent: any) => {
				return dependent.assetId === dependencyAssetId;
			});
			if (index !== -1) {
				this.metadata.dependents.splice(index, 1);
				if (dataItem !== null) {
					this.dependentsComponent.gridDependenciesData.removeDataItem(dataItem);
				}
			}
		}
	}

	getGoJsGraphUrl(): string {
		return `/tdstm/module/asset/architecture-graph?assetId=${this.mainAsset}&levelsUp=0&levelsDown=3`;
	}

	openGoJsGraphUrl() {
		this.windowService.getWindow().open(this.getGoJsGraphUrl(), '_blank');
	}

	onExpandActionDispatched(): void {
		this.openGoJsGraphUrl();
	}

	/**
	 * Load asset details for architecture graph thumbnail
	 */
	loadThumbnailData(assetId: number | string): void {
		this.architectureGraphService.getAssetDetails(assetId, 0, 1)
			.subscribe(res => {
				const diagramHelper = new AssetCommonDiagramHelper();
				this.data$.next(diagramHelper.diagramData({
					rootNode: assetId,
					currentUserId: this.currentUser.id,
					data: res,
					iconsOnly: true,
					extras: {
						initialAutoScale: Diagram.Uniform,
						allowZoom: false
					}
				}));
			});
	}

	/**
	 * Allows for the comment count to be updated
	 * @param commentCount - New comment count value
	 */
	updateCommentCount(commentCount: number): void {
		this.commentCount = commentCount;
	}

	/**
	 * Allows for the task count to be updated
	 * @param taskCount - New task count value
	 */
	updateTaskCount(taskCount: number): void {
		this.taskCount = taskCount;
	}
}
