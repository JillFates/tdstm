import {
	ChangeDetectorRef,
	Component,
	OnInit
} from '@angular/core';

import {AlertType} from '../../../../shared/model/alert.model';

import {
	DomSanitizer,
	SafeHtml
} from '@angular/platform-browser';

import {
	Observable,
} from 'rxjs';

import {
	pathOr,
} from 'ramda';

import {ActivatedRoute} from '@angular/router';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';
import {ReportsService} from '../../service/reports.service';
import {NotifierService} from '../../../../shared/services/notifier.service';

declare var jQuery: any;

@Component({
	selector: 'tds-application-conflicts',
	template: `
		<!--
		<div class="actions">
						<div>
						<div>Output:</div>
						<div>
						<input type="radio" id="output" name="output" checked>
						<label for="output">Web</label>
						</div>
						</div>
						<div>
						<tds-button-custom
						class="btn-primary"
						(click)="onGenerateReport(model.defaultEvent.id)"
						title="Generate"
						tooltip="Generate report"
						icon="check-square">
						</tds-button-custom>
						</div>
						</div>
		-->
		<div class="application-conflicts">
			<div class="application-section">
				<div class="section-title">Bundle</div>
				<div class="section-control">
					<kendo-dropdownlist
							name="bundleList"
							class="form-control"
							[data]="model.moveBundleList"
							[textField]="'name'"
							[valueField]="'id'"
							[(ngModel)]="model.defaultBundle">
					</kendo-dropdownlist>
				</div>
			</div>

			<div class="application-section">
				<div class="section-title">App Owner</div>
				<div class="section-control">
					<kendo-dropdownlist
							name="appOwner"
							class="form-control"
							[data]="model.appOwnerList"
							[textField]="'name'"
							[valueField]="'id'"
							[(ngModel)]="model.defaultAppowner">
					</kendo-dropdownlist>
				</div>
			</div>

			<div class="application-section">
				<div class="section-title">
					<input [checked]="model.bundleConflict" (change)="model.bundleConflict = !model.bundleConflict"
					id="bundleConflict" name="bundleConflict" type="checkbox">
					<label for="bundleConflict">Bundle Conflict</label>
				</div>
				<div class="section-control">
					<label>Having dependency references to assets assigned to unrelated bundles</label>
				</div>
			</div>

			<div class="application-section">
				<div class="section-title">
					<input [checked]="model.unresolvedDependencies" (change)="model.unresolvedDependencies = !model.unresolvedDependencies"
					id="unresolvedDependencies" name="unresolvedDependencies" type="checkbox">
					<label for="unresolvedDependencies">Unresolved Dependencies</label>
				</div>
				<div class="section-control">
					<label>Having dependencies with status Unknown or Questioned</label>
				</div>
			</div>

			<div class="application-section">
				<div class="section-title">
					<input [checked]="model.missingDependencies" (change)="model.missingDependencies = !model.missingDependencies"
					id="missingDependencies" name="missingDependencies" type="checkbox">
					<label for="missingDependencies">Missing Dependencies</label>
				</div>
				<div class="section-control">
					<label>Having no defined Supports or Requires dependencies</label>
				</div>
			</div>

			<div class="application-section">
				<div class="section-title">
					<div>Maximum Applications to report</div>
				</div>
				<div class="section-control">
					<kendo-dropdownlist name="appOwner"
						class="form-control"
						[data]="model.maxApplicationsList"
						[textField]="'value'"
						[valueField]="'value'"
						[(ngModel)]="model.maxApplications">
					</kendo-dropdownlist>
				</div>
			</div>
			<div class="application-section">
				<div class="section-title">
					<tds-button-custom
						class="btn-primary"
						title="Generate"
						tooltip="Generate report"
						icon="check-square">
					</tds-button-custom>
				</div>
				<div class="section-control">
				</div>
			</div>
		</div>
`
})
export class ApplicationConflictsComponent implements OnInit {
	public model = {
		moveBundleList: [],
		appOwnerList: [],
		defaultBundle: {id: -1, text: 'Planning Bundles'},
		defaultAppOwner: {id: -1, text: 'All'},
		bundleConflict: true,
		unresolvedDependencies: true,
		missingDependencies: true,
		maxApplications: {value: 100},
		maxApplicationsList: [{value: 100}, {value: 250}, {value: 500}]
	};

	constructor(
		private sanitizer: DomSanitizer,
		private route: ActivatedRoute,
		private changeDetectorRef: ChangeDetectorRef,
		private translatePipe: TranslatePipe,
		private notifierService: NotifierService,
		private reportsService: ReportsService) {
	}

	ngOnInit() {
		const commonCalls = [this.reportsService.getDefaults(), this.reportsService.getDefaultsApplicationConflicts()];

		// on init
		Observable.forkJoin(commonCalls)
			.subscribe((results) => {
				const [events, defaultsApplication] = results;
				this.model.moveBundleList = defaultsApplication.moveBundleList;
				this.model.appOwnerList = defaultsApplication.appOwnerList;

				/*
				this.model.events = events.map((item) => ({id: item.id.toString(), text: item.name}));
				this.model.defaultEvent.id = pathOr(null, ['preferences', 'TASK_CREATE_EVENT'], defaults);
				if (this.model.defaultEvent.id === 'null' || this.model.defaultEvent.id === null) {
					this.model.defaultEvent.id = this.model.events[0].id;
				}
				*/
			})
	}

}
