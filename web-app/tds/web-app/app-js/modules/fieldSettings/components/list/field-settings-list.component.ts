import { Component, Inject, OnInit, ViewChildren, QueryList } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { FieldSettingsGridComponent } from '../grid/field-settings-grid.component';
import { FieldSettingsService } from '../../service/field-settings.service';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { DomainModel } from '../../model/domain.model';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';

@Component({
	moduleId: module.id,
	selector: 'field-settings-list',
	templateUrl: '../tds/web-app/app-js/modules/fieldSettings/components/list/field-settings-list.component.html'
})
export class FieldSettingsListComponent implements OnInit {
	@ViewChildren('grid') grids: QueryList<FieldSettingsGridComponent>;
	public domains: DomainModel[] = [];
	private dataSignature: string;
	selectedTab = '';
	editing = false;

	constructor(
		@Inject('fields') fields: Observable<DomainModel[]>,
		private fieldService: FieldSettingsService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private notifier: NotifierService
	) {
		fields.subscribe(
			(result) => {
				this.domains = result;
				if (this.domains.length > 0) {
					this.selectedTab = this.domains[0].domain;
				}
			},
			(err) => console.log(err));
	}

	ngOnInit(): void {
		this.dataSignature = JSON.stringify(this.domains);
	}

	protected onTabChange(domain: string): void {
		this.selectedTab = domain;
	}

	protected isTabSelected(domain: string): boolean {
		return this.selectedTab === domain;
	}

	protected onSaveAll(callback): void {
		if (this.isEditAvailable()) {
			let invalid = this.domains.filter(domain => !this.isValid(domain));
			if (invalid.length === 0) {
				this.domains.forEach(domain => {
					domain.fields.filter(x => x['isNew']).forEach(x => delete x['isNew']);
				});
				this.fieldService.saveFieldSettings(this.domains)
					.subscribe((res: any) => {
						if (res.status === 'error') {
							let message: string = res.errors.join(',');
							this.notifier.broadcast({
								name: AlertType.DANGER,
								message: message + '. Refreshing...'
							});
						}
						this.refresh();
						callback();
					});
			} else {
				this.selectedTab = invalid[0].domain;
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Please review your changes.'
				});
			}
		}
	}

	protected onCancel(callback) {
		if (this.isDirty()) {
			this.prompt.open(
				'Confirmation Required',
				'You have changes that have not been saved. Do you want to continue and lose those changes?',
				'Confirm', 'Cancel').then(result => {
					if (result) {
						this.refresh();
					}
				});
		} else {
			callback();
		}
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission('ProjectFieldSettingsEdit');
	}

	protected isDirty(): boolean {
		return this.dataSignature !== JSON.stringify(this.domains);
	}

	protected getCurrentState(domain: DomainModel) {
		return {
			editable: this.isEditAvailable(),
			dirty: this.isDirty(),
			valid: this.isValid(domain)
		};
	}

	protected refresh(): void {
		this.fieldService.getFieldSettingsByDomain().subscribe(
			(result) => { this.domains = result; },
			(err) => console.log(err));
	}

	protected isValid(domain: DomainModel): boolean {
		return domain.fields.filter(item =>
			!item.label || !item.field).length === 0;
	}

	protected onAdd(callback): void {
		let custom = this.domains
			.filter(domain => domain.domain === this.selectedTab)
			.reduce((p: FieldSettingsModel[], c: DomainModel) => p.concat(c.fields), [])
			.filter(item => item.udf)
			.map((item) => +item.field.replace(/[a-z]/ig, ''))
			.filter((x, i, dt) => dt.indexOf(x) === i)
			.sort((a, b) => a - b);
		let number = custom.findIndex((item, i) => item !== i + 1);
		callback('custom' + ((number === -1 ? custom.length : number) + 1));
	}

	protected onShare(value: { field: FieldSettingsModel, domain: string }): void {
		if (value.field.shared) {
			this.prompt.open(
				'Confirmation Required',
				`This will overwrite field ${value.field.field}. Do you want to continue?`,
				'Confirm', 'Cancel').then(result => {
					if (result) {
						this.handleSharedField(value.field, value.domain);
					} else {
						value.field.shared = false;
					}
				});
		} else {
			this.handleSharedField(value.field, value.domain);
		}

	}

	protected onDelete(value: { field: FieldSettingsModel, domain: string, callback: any }): void {
		this.domains.filter(domain =>
			value.field.shared ?
				true : domain.domain === value.domain).forEach(domain => {
					domain.fields.splice(domain.fields.indexOf(value.field), 1);
				});
		this.refreshGrids(value.field.shared, value.callback);
	}

	public refreshGrids(all, callback: any) {
		if (all) {
			this.grids.forEach(grid => grid.refresh());
		} else {
			callback();
		}
	}

	protected handleSharedField(field: FieldSettingsModel, domain: string) {
		this.domains
			.filter(d => d.domain !== domain)
			.forEach(d => {
				let indexOf = d.fields.findIndex(f => f.field === field.field);
				if (field.shared) {
					if (indexOf === -1) {
						d.fields.push(field);
					} else {
						d.fields.splice(indexOf, 1, field);
					}
				} else {
					d.fields.splice(indexOf, 1, { ...field });
				}
			});
		this.refreshGrids(true, null);
	}
}