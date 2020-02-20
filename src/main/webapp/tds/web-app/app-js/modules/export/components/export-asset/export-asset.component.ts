// Angular
import { Component, OnInit } from '@angular/core';
// Services
import { ExportAssetService } from '../../service/export-asset.service';
import { NotifierService } from '../../../../shared/services/notifier.service';
// Models
import { ExportAssetModel } from '../../model/export-asset.model';
import { UserContextModel } from '../../../auth/model/user-context.model';
// Pipes
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';
// Others
import { Store } from '@ngxs/store';
import { saveAs } from 'file-saver';
import { UIDialogService } from '../../../../shared/services/ui-dialog.service';

@Component({
	selector: 'tds-asset-export',
	templateUrl: 'export-asset.component.html',
	styles: [],
})
export class ExportAssetComponent implements OnInit {
	protected gridColumns: any[];
	public selectedAll = false;
	protected userPreferences = [];
	protected selectedBundles = ['useForPlanning'];
	protected errorMessage = '';
	protected opened = false;
	protected title = '';
	protected progress_value = 0;
	private userContext: UserContextModel;
	public exportAssetsData: ExportAssetModel = new ExportAssetModel();

	constructor(
		private exportService: ExportAssetService,
		protected translatePipe: TranslatePipe,
		private store: Store,
		protected dialogService: UIDialogService,
		private notifierService: NotifierService
	) {}

	/**
	 * onInit retrieve the information for displaying all the bundles and filling up
	 * the users preference to item to export
	 */
	ngOnInit() {
		this.exportService.getExportAssetsData().subscribe((res: any) => {
			this.exportAssetsData = res;
			this.updateUserPreferencesModel();
		});

		this.store
			.select(state => state.TDSApp.userContext)
			.subscribe((userContext: UserContextModel) => {
				this.userContext = userContext;
			});
	}

	/**
	 * Set the user preferences for the selected items on the Items to Export list
	 */
	updateUserPreferencesModel(): void {
		this.userPreferences = [
			{
				preference: 'ImportApplication',
				selected:
					this.exportAssetsData.userPreferences[
						'ImportApplication'
					] === 'true',
			},
			{
				preference: 'ImportServer',
				selected:
					this.exportAssetsData.userPreferences['ImportServer'] ===
					'true',
			},
			{
				preference: 'ImportDatabase',
				selected:
					this.exportAssetsData.userPreferences['ImportDatabase'] ===
					'true',
			},
			{
				preference: 'ImportStorage',
				selected:
					this.exportAssetsData.userPreferences['ImportStorage'] ===
					'true',
			},
			{
				preference: 'ImportRoom',
				selected:
					this.exportAssetsData.userPreferences['ImportRoom'] ===
					'true',
			},
			{
				preference: 'ImportRack',
				selected:
					this.exportAssetsData.userPreferences['ImportRack'] ===
					'true',
			},
			{
				preference: 'ImportDependency',
				selected:
					this.exportAssetsData.userPreferences[
						'ImportDependency'
					] === 'true',
			},
			{
				preference: 'ImportCabling',
				selected:
					this.exportAssetsData.userPreferences['ImportCabling'] ===
					'true',
			},
			{
				preference: 'ImportComment',
				selected:
					this.exportAssetsData.userPreferences['ImportComment'] ===
					'true',
			},
		];
	}

	/**
	 * Used for select/deselect all items on checkbox list
	 */
	selectAll(): void {
		this.selectedAll = !this.selectedAll;
		this.userPreferences.forEach((el, index) => {
			this.userPreferences[index].selected = this.selectedAll;
		});
	}

	/**
	 * If the user selects all checkboxes this function checks if all
	 * are marked to activate All Items checkbox
	 */
	checkIfAllSelected(): boolean {
		let totalSelected = 0;
		this.userPreferences.forEach((el, index) => {
			if (this.userPreferences[index].selected) {
				totalSelected++;
			}
		});
		this.selectedAll = totalSelected === this.userPreferences.length;
		return true;
	}

	/**
	 * Setup the data we will send to the backend to export the selected assets
	 * Added validation to make sure at least one bundle is selected
	 */
	exportData(): void {
		this.progress_value = 0;
		if (this.selectedBundles.length === 0) {
			this.errorMessage = this.translatePipe.transform(
				'ASSET_EXPORT.BUNDLE_ERROR'
			);
		} else {
			let data = {
				projectIdExport: this.userContext.project.id,
				dataTransferSet: 1,
				application: 'application',
				asset: 'asset',
				exportFormat: 'xlsx',
			};
			this.userPreferences.reduce((map, obj) => {
				data[obj.preference] = obj.selected;
				return map;
			}, {});

			if (
				this.selectedBundles.find(el => {
					return el === 'All';
				})
			) {
				data['bundle'] = 'All';
			} else if (
				this.selectedBundles.find(el => {
					return el === 'useForPlanning';
				})
			) {
				data['bundle'] = 'useForPlanning';
			} else {
				data['bundle'] = this.selectedBundles;
			}
			this.disableGlobalAnimation(true);
			this.exportService.downloadBundleFile(data).subscribe(res => {
				this.opened = true;
				if (res['key']) {
					this.pollUntilTaskFinished(res['key']);
				}
			});
		}
	}

	/**
	 * Recursive function to monitor progress of file exporting in backend
	 * Calls to backend to retrieve progress every second while progress hasn't reached
	 * to 100 % ready
	 */
	async pollUntilTaskFinished(taskId) {
		this.exportService.getProgress(taskId).subscribe(
			progress => {
				console.log(progress);
				this.progress_value = progress['percentComp'];
				this.title = progress['status'];
				if (this.progress_value < 100) {
					setTimeout(() => this.pollUntilTaskFinished(taskId), 1000);
				} else {
					saveAs(
						this.exportService.getBundleFile(taskId),
						progress['data'].header.split('=')[1]
					);
					this.opened = false;
					this.disableGlobalAnimation(false);
				}
			},
			error => {
				console.log(error);
				this.disableGlobalAnimation(false);
			}
		);
	}

	/**
	 * Set the error to empty when a bundle is selected
	 * */
	updateError(): void {
		this.errorMessage = '';
	}

	/**
	 * To disable Global animation that is being manually represent by the Progress bar
	 * @param disabled
	 */
	private disableGlobalAnimation(disabled: boolean): void {
		this.notifierService.broadcast({
			name: 'notificationDisableProgress',
			disabled: disabled,
		});
	}
}
