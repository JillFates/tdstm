import {Component, ElementRef, OnInit, Renderer2} from '@angular/core';
import {BundleService} from '../../service/bundle.service';
import {PermissionService} from '../../../../shared/services/permission.service';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';
import {ActivatedRoute} from '@angular/router';

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
	public canCreateBundle;
	private bundleId;
	protected userTimeZone: string;
	constructor(
		private bundleService: BundleService,
		private permissionService: PermissionService,
		private route: ActivatedRoute,
		private preferenceService: PreferenceService) {
		this.canCreateBundle = this.permissionService.hasPermission('BundleCreate');
		this.bundleId = this.route.params['_value']['id'];
	}

	ngOnInit() {
		this.getModel(this.bundleId);
		this.userTimeZone = this.preferenceService.getUserTimeZone();
	}

	public deleteBundle() {
		this.bundleService.deleteBundle(this.bundleId)
			.subscribe((result) => {
				return result;
			});
	}

	public deleteBundleAndAssets() {
		this.bundleService.deleteBundleAndAssets(this.bundleId)
			.subscribe((result) => {
				return result;
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