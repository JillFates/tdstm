import {NotifierService} from '../../../../shared/services/notifier.service';
import {UIActiveDialogService, UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {AssetExplorerService} from '../../../assetManager/service/asset-explorer.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {DependecyService} from '../../service/dependecy.service';
import {HostListener, OnInit, AfterContentInit } from '@angular/core';
import {DIALOG_SIZE, KEYSTROKE} from '../../../../shared/model/constants';
import {TagModel} from '../../../assetTags/model/tag.model';
import {AssetShowComponent} from './asset-show.component';
import {AssetDependencyComponent} from '../asset-dependency/asset-dependency.component';
import {AssetCommonHelper} from './asset-common-helper';
import {WindowService} from '../../../../shared/services/window.service';
import {UserContextModel} from '../../../auth/model/user-context.model';
import {UserContextService} from '../../../auth/service/user-context.service';
import {ArchitectureGraphService} from '../../../assetManager/service/architecture-graph.service';
import {ReplaySubject} from 'rxjs';
import {IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {Diagram, Layout, Link, Spot} from 'gojs';
import {AssetCommonDiagramHelper} from './asset-common-diagram.helper';
import {AssetTagUIWrapperService} from '../../../../shared/services/asset-tag-ui-wrapper.service';

declare var jQuery: any;

export class AssetCommonShow implements OnInit, AfterContentInit {

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
	protected data$: ReplaySubject<IDiagramData> = new ReplaySubject(1);
	protected diagramLayout$: ReplaySubject<Layout> = new ReplaySubject(1);
	protected linkTemplate$: ReplaySubject<Link> = new ReplaySubject(1);

	constructor(
		protected activeDialog: UIActiveDialogService,
		protected dialogService: UIDialogService,
		protected assetService: DependecyService,
		protected prompt: UIPromptService,
		protected assetExplorerService: AssetExplorerService,
		protected notifierService: NotifierService,
		protected userContextService: UserContextService,
		protected windowService: WindowService,
		protected architectureGraphService: ArchitectureGraphService,
		private assetTagUIWrapperService?: AssetTagUIWrapperService
	) {
			jQuery('[data-toggle="popover"]').popover();
			this.userContextService.getUserContext()
				.subscribe((userContext: UserContextModel) => {
					this.userDateFormat = userContext.dateFormat;
					this.userTimeZone = userContext.timezone;
					this.currentUser = userContext.user;
				});
	}

	@HostListener('keydown', ['$event']) handleKeyboardEvent(event: KeyboardEvent) {
		if (event && event.code === KEYSTROKE.ESCAPE) {
			this.cancelCloseDialog();
		}
	}

	/**
	 * Initiates The Injected Component
	 */
	ngOnInit(): void {
		jQuery('[data-toggle="popover"]').popover();
	}

	ngAfterContentInit(): void {
		setTimeout(() => {
			let tagsDiv = <HTMLElement>document.querySelector('.tags-container');
			let tableRow = <HTMLElement>document.querySelector('.one-column');
			let tableRowSiblingWidth = <HTMLElement>(<HTMLElement>document.querySelector('.fit-tags-to-view')).previousSibling;
			tagsDiv.style.width = (tableRow.offsetWidth - tableRowSiblingWidth.offsetWidth) + 'px';
			this.assetTagUIWrapperService.updateTagsWidthForAssetShowView('.tags-container', 'span.dots-for-tags', '.one-column');
		}, 500);
	}

	cancelCloseDialog(): void {
		this.activeDialog.dismiss();
		jQuery('body').removeClass('modal-open');
	}

	showAssetDetailView(assetClass: string, id: number) {
		this.dialogService.replace(AssetShowComponent, [
				{provide: 'ID', useValue: id},
				{provide: 'ASSET', useValue: assetClass}],
			DIALOG_SIZE.XXL);
		jQuery('body').addClass('modal-open');
	}

	/**
	 * Show the dependency dialog, in case a dependency is removed update the corresponding grid
	 * @param type Dependency type (support/ dependent on)
	 * @param assetId Main Asset id
	 * @param dependencyAsset  id of the asset dependent
	 * @param rowId Id fo the row to be deleted
	 */
	showDependencyView(type: string, assetId: number, dependencyAsset: number, rowId = '') {
		this.assetService.getDependencies(assetId, dependencyAsset)
			.subscribe((result) => {
				jQuery('body').addClass('modal-open');
				this.dialogService.extra(AssetDependencyComponent, [
					{ provide: 'ASSET_DEP_MODEL', useValue: result }])
					.then(res => {
						// if the dependency was deleted remove it from the grid
						if (res && res.delete) {
							this.deleteDependencyRowUpdateCounter(type, rowId)
						}
					})
					.catch(res => {
						console.log(res);
					});
			}, (error) => console.log(error));
	}

	/**
	 * Delete the dependency row and update the corresponding counter
	 * Supports and Dependent grids are generated through a gsp file, so we need to remove the row via JQuery
	 * @param type Type of dependency (support or dependent)
	 * @param rowId Id fo the row to be deleted
	 */
	private deleteDependencyRowUpdateCounter(type: string, rowId: string): void {
		if (rowId) {
			jQuery(`#${rowId}.asset-detail-${type}-row`).remove();

			const counter = jQuery(`#asset-detail-${type}-counter`);
			const currentRows = jQuery(`.asset-detail-${type}-row`);
			if (currentRows.length >= 0 && counter.length) {
				// decrease the counter badge
				counter.text(currentRows.length > 99 ? '99+' : currentRows.length);
			}
		}
	}

	getGraphUrl(): string {
		return `/tdstm/assetEntity/architectureViewer?assetId=${this.mainAsset}&level=2`;
	}

	openGraphUrl() {
		this.windowService.getWindow().open(this.getGraphUrl(), '_blank');
	}

	getGoJsGraphUrl(): string {
		return `/tdstm/module/asset/architecture-graph?assetId=${this.mainAsset}&levelsUp=0&levelsDown=3`;
	}

	openGoJsGraphUrl() {
		this.windowService.getWindow().open(this.getGoJsGraphUrl(), '_blank');
	}

	onExpandActionDispatched(): void {
		this.openGraphUrl();
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
						diagramOpts: {
							// initialAutoScale: Diagram.UniformToFill,
							contentAlignment: Spot.Center,
							allowZoom: false
						},
						isExpandable: false
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
