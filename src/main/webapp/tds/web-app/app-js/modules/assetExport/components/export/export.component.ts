// Angular
import {
	Component,
	HostListener,
	OnDestroy,
	OnInit,
} from '@angular/core';
// Services
import {ExportAssetService} from '../../service/export-asset.service';
// Models
import {ExportAssetModel} from '../../model/export-asset.model';
import {UserContextModel} from '../../../auth/model/user-context.model';
// Pipes
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
// Others
import {Store} from '@ngxs/store';

declare var jQuery: any;

@Component({
	selector: 'tds-asset-export',
	templateUrl: 'export.component.html',
	styles: []
})
export class ExportComponent implements OnInit, OnDestroy {
	protected gridColumns: any[];
	public selectedAll = false;

	private userPreferences = [];
	private selectedBundles = [];
	private errorMessage = '';
	private userContext: UserContextModel;
	public exportAssetsData: ExportAssetModel = new ExportAssetModel();

	constructor(
		private exportService: ExportAssetService,
		private translatePipe: TranslatePipe,
		private store: Store) {
		// comment
	}

	ngOnInit() {
		this.exportService.getExportAssetsData().subscribe( (res: any) => {
			console.log('res', res);
			this.exportAssetsData = res;
			this.updateUserPreferencesModel();
		});

		this.store.select(state => state.TDSApp.userContext).subscribe((userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	updateUserPreferencesModel() {
		this.userPreferences = [
			{preference: 'ImportApplication', selected: this.exportAssetsData.userPreferences['ImportApplication'] === 'true'},
			{preference: 'ImportServer', selected: this.exportAssetsData.userPreferences['ImportServer'] === 'true'},
			{preference: 'ImportDatabase', selected: this.exportAssetsData.userPreferences['ImportDatabase'] === 'true'},
			{preference: 'ImportStorage', selected: this.exportAssetsData.userPreferences['ImportStorage'] === 'true'},
			{preference: 'ImportRoom', selected: this.exportAssetsData.userPreferences['ImportRoom'] === 'true'},
			{preference: 'ImportRack', selected: this.exportAssetsData.userPreferences['ImportRack'] === 'true'},
			{preference: 'ImportDependency', selected: this.exportAssetsData.userPreferences['ImportDependency'] === 'true'},
			{preference: 'ImportCabling', selected: this.exportAssetsData.userPreferences['ImportCabling'] === 'true'},
			{preference: 'ImportComment', selected: this.exportAssetsData.userPreferences['ImportComment'] === 'true'}
		];
	}

	selectAll() {
		this.selectedAll = !this.selectedAll;
		for (let i = 0; i < this.userPreferences.length; i++) {
			this.userPreferences[i].selected = this.selectedAll;
		}
	}

	checkIfAllSelected() {
		let totalSelected =  0;
		for (let i = 0; i < this.userPreferences.length; i++) {
			if (this.userPreferences[i].selected) {
				totalSelected++;
			}
		}
		this.selectedAll = totalSelected === this.userPreferences.length;
		return true;
	}

	exportData() {
		if (this.selectedBundles.length === 0) {
			this.errorMessage = this.translatePipe.transform('ASSET_EXPORT.BUNDLE_ERROR');
		} else {
			// get user preferences
			let data = {
				projectIdExport: this.userContext.project.id,
				dataTransferSet: 1,
				application: 'application',
				asset: 'asset',
				exportFormat: 'xlsx'
			};
			let result = this.userPreferences.reduce(function(map, obj) {
				map[obj.preference] = obj.selected;
				data[obj.preference] = obj.selected;
				return map;
			}, {});

			if (this.selectedBundles.find(el => {return el === 'all'})) {
				data['bundle'] = 'all';
			} else if (this.selectedBundles.find(el => {return el === 'planning-bundle'})) {
				data['bundle'] = 'planning-bundle';
			} else {
				data['bundle'] = this.selectedBundles;
			}
			console.log('data', data);

			this.exportService.downloadBundleFile(data).subscribe( res => {
				console.log('data', res);
			})
		}
	}

	updateError() {
		this.errorMessage = '';
	}
	/**
	 * unsubscribe from all subscriptions on destroy hook.
	 * @HostListener decorator ensures the OnDestroy hook is called on events like
	 * Page refresh, Tab close, Browser close, navigation to another view.
	 */
	@HostListener('window:beforeunload')
	ngOnDestroy(): void {
		// comment
	}
}