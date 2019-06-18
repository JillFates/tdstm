import {Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {ActivatedRoute, Router} from '@angular/router';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';

@Component({
	selector: `bundle-show`,
	templateUrl: 'bundle-show.component.html',
})
export class BundleShowComponent implements OnInit {
	public dashboardSteps;
	public isDefaultBundle;
	public moveBundleInstance;
	public moveManager;
	public projectId;
	public projectManager;
	public canEditBundle;
	private bundleId;
	protected userTimeZone: string;
	constructor(
		private bundleService: BundleService,
		private permissionService: PermissionService,
		private route: ActivatedRoute,
		private router: Router,
		private preferenceService: PreferenceService,
		private promptService: UIPromptService) {
		this.canEditBundle = this.permissionService.hasPermission('BundleEdit');
		this.bundleId = this.route.params['_value']['id'];
	}

	ngOnInit() {
		this.getModel(this.bundleId);
		this.userTimeZone = this.preferenceService.getUserTimeZone();
	}

	public confirmDeleteBundle() {
		this.promptService.open(
			'Confirmation Required',
			'WARNING: Deleting this bundle will remove any teams and any related step data',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteBundle();
				}
			})
			.catch((error) => console.log(error));
	}

	public confirmDeleteBundleAndAssets() {
		this.promptService.open(
			'Confirmation Required',
			'WARNING: Deleting this bundle will remove any teams, any related step data, AND ASSIGNED ASSETS (NO UNDO)',
			'Confirm', 'Cancel')
			.then(confirm => {
				if (confirm) {
					this.deleteBundleAndAssets();
				}
			})
			.catch((error) => console.log(error));
	}

	private deleteBundle() {
		this.bundleService.deleteBundle(this.bundleId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.router.navigateByUrl('bundle/list')
				}
			});
	}

	private deleteBundleAndAssets() {
		this.bundleService.deleteBundleAndAssets(this.bundleId)
			.subscribe((result) => {
				if (result.status === 'success') {
					this.router.navigateByUrl('bundle/list')
				}
			});
	}

	private getModel(id) {
		this.bundleService.getModelForBundleShow(id)
			.subscribe((result) => {
				let data = result.data;
				this.dashboardSteps = data.dashboardSteps;
				this.isDefaultBundle = data.isDefaultBundle;
				this.moveBundleInstance = data.moveBundleInstance;
				this.moveManager = data.moveManager;
				this.projectId = data.projectId;
				this.projectManager = data.projectManager;
			});
	}
}