import { Component, Inject, OnInit, OnDestroy, ViewChildren, QueryList } from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import { Observable } from 'rxjs';
import {switchMap} from 'rxjs/operators';

import { FieldSettingsGridComponent } from '../grid/field-settings-grid.component';
import { FieldSettingsService } from '../../service/field-settings.service';
import { FieldSettingsModel } from '../../model/field-settings.model';
import { DomainModel } from '../../model/domain.model';

import { PermissionService } from '../../../../shared/services/permission.service';
import { UIPromptService } from '../../../../shared/directives/ui-prompt.directive';
import { NotifierService } from '../../../../shared/services/notifier.service';
import { AlertType } from '../../../../shared/model/alert.model';
import { ValidationUtils } from '../../../../shared/utils/validation.utils';
import { Permission } from '../../../../shared/model/permission.model';
import {TranslatePipe} from '../../../../shared/pipes/translate.pipe';

@Component({
	selector: 'field-settings-list',
	templateUrl: 'field-settings-list.component.html'
})
export class FieldSettingsListComponent implements OnInit, OnDestroy {
	@ViewChildren('grid') grids: QueryList<FieldSettingsGridComponent>;
	public domains: DomainModel[] = [];
	private dataSignature: string;
	selectedTab = '';
	editing = false;
	private lastSnapshot;
	protected navigationSubscription;
	private isEditable: boolean;

	private filter = {
		search: '',
		fieldType: 'All'
	};

	private fieldsToDelete = {};

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		private fieldService: FieldSettingsService,
		private permissionService: PermissionService,
		private prompt: UIPromptService,
		private notifier: NotifierService,
		private translatePipe: TranslatePipe,
	) {
		this.domains = this.route.snapshot.data['fields'];
		if (this.domains.length > 0) {
			this.selectedTab = this.domains[0].domain;
		}
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
				this.domains = this.lastSnapshot.data['fields'];
				if (this.domains.length > 0) {
					this.selectedTab = this.domains[0].domain;
				}
			}
		});
	}

	/**
	 * Calls every time the Component is recreated by calling same URL
	 */
	private initialiseComponent(): void {
		this.dataSignature = JSON.stringify(this.domains);
		this.isEditable = this.isEditAvailable();
		this.initialiseFieldsToDelete();
	}

	/**
	 * Set the initial empty structure to hold the fields to delete
	 */
	private initialiseFieldsToDelete(): void {
		for (let domain of this.domains) {
			this.fieldsToDelete[domain.domain] = [];
		}
	}

	protected onTabChange(domain: string): void {
		this.selectedTab = domain;
	}

	protected isTabSelected(domain: string): boolean {
		return this.selectedTab === domain;
	}

	protected onSaveAll(savingInfo: any): void {
		if (this.isEditAvailable()) {
			let invalid = this.domains.filter(domain => !this.isValid(domain));
			if (invalid.length === 0) {

				// remove(delete) fields if user requested
				for (let domain of this.domains) {
					if (this.fieldsToDelete[domain.domain].length > 0) {
						this.fieldsToDelete[domain.domain].forEach(field => {
							let index = domain.fields.findIndex(x => x.field === field);
							if (index !== -1) {
								domain.fields.splice(index, 1);
							}
						});
					}
				}

				this.domains.forEach(domain => {
					domain.fields.filter(x => x['isNew'])
						.forEach(x => {
							delete x['isNew'];
							delete x['count'];
						});
				});

				this.fieldService.saveFieldSettings(this.domains)
					.pipe(
						switchMap((res: any) => {
							if (res.status === 'error') {
								return res;
							} else {
								// delete underlaying data
								return savingInfo.deleteUnderLaying ?
									this.fieldService.deleteCustomFields(this.fieldsToDelete) :
									Observable.of(true);
							}
						})
					)
					.subscribe((res: any) => {
						if (savingInfo.deleteUnderLaying) {
							this.initialiseFieldsToDelete();
						}

						if (res.status === 'error') {
							let message: string = res.errors.join(',');
							this.notifier.broadcast({
								name: AlertType.DANGER,
								message: message + '. Refreshing...'
							});
						}
						this.refresh();
						savingInfo.callback();
					});
			} else {
				this.selectedTab = invalid[0].domain;
				this.notifier.broadcast({
					name: AlertType.DANGER,
					message: 'Label is a required field and must be unique. Please correct before saving.'
				});
			}
		}
	}

	/**
	 * Shows the confirmation cancel changes dialog, on success refresh the grid otherwise reset the flag variables
	 */
	protected onCancel(callback) {
		if (this.isDirty()) {
			this.prompt.open(
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.CONFIRMATION_REQUIRED'),
				this.translatePipe.transform('GLOBAL.CONFIRMATION_PROMPT.UNSAVED_CHANGES_MESSAGE'),
				this.translatePipe.transform('GLOBAL.CONFIRM'),
				this.translatePipe.transform('GLOBAL.CANCEL'),
			).then(result => {
					if (result) {
						this.refresh();
					} else {
						callback.failure();
					}
				});
		} else {
			callback.success();
		}
	}

	protected isEditAvailable(): boolean {
		return this.permissionService.hasPermission(Permission.ProjectFieldSettingsEdit);
	}

	protected isDirty(): boolean {
		let result = this.dataSignature !== JSON.stringify(this.domains);
		// TODO: STATE SERVICE GO
		// if (this.state && this.state.$current && this.state.$current.data) {
		// 	this.state.$current.data.hasPendingChanges = result;
		// }
		result = result || this.hasPendingDeletes();
		return result;
	}

	protected refresh(): void {
		this.fieldService.getFieldSettingsByDomain().subscribe(
			(result) => {
				this.domains = result;
				this.dataSignature = JSON.stringify(this.domains);
				setTimeout(() => {
					this.grids.forEach(grid => grid.applyFilter());
				});
			},
			(err) => console.log(err));
	}

	protected isValid(domain: DomainModel): boolean {
		const deletedFields  = this.fieldsToDelete && this.fieldsToDelete[domain.domain] || [];

		const fields =  domain.fields
			.filter((item) => !(deletedFields.includes(item.field)));

		let values = fields.map(x => x.label);

		// Validates "Field Order" is not null && should be a number on the range of [0, X)
		let invalidOrderFields = fields.filter(item =>
			item.order === null || !ValidationUtils.isValidNumber(item.order) || item.order < 0
		);
		let invalidFieldLabels = this.fieldService.checkLabelsAndNamesConflicts(fields, this.domains, domain);

		return fields.filter(item => !item.label.trim() || !item.field).length === 0
			// Validates "Field Labels" should be unique by domain
			&& values.filter((l, i) => values.indexOf(l) !== i).length === 0
			&& invalidOrderFields.length === 0
			&& invalidFieldLabels === false;
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
		if (((number === -1 ? custom.length : number) + 1) <= 96) {
			callback('custom' + ((number === -1 ? custom.length : number) + 1));
		} else {
			this.notifier.broadcast({
				name: AlertType.DANGER,
				message: 'Custom fields is limited to 96 fields.'
			});
		}
	}

	protected onShare(value: { field: FieldSettingsModel, domain: string }): void {

		if (value.field.shared) {
			this.prompt.open(
				'Confirmation Required',
				this.translatePipe.transform('FIELD_SETTINGS.ON_SHARED', [value.field.field]),
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

	/**
	 * Checks if there are any pending fields to be deleted on all the domains.
	 * @returns {boolean}
	 */
	private hasPendingDeletes(): boolean {
		for (let domain in this.fieldsToDelete) {
			if (this.fieldsToDelete[domain].length > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * Whenever the user clicks Delete button for a field on the grid, this event is called and
	 * fieldsToDelete gets updated, if the field it is shared it is added to the array, otherwise new value replace old one
	 * @param {{domain: string; fieldsToDelete: string[]; isSharedField: boolean; addToDeleteCollection: boolean}} value
	 */
	protected onDelete(value: { domain: string, fieldsToDelete: string[], isSharedField: boolean, addToDeleteCollection: boolean }): void {
		let fieldsToDelete = this.fieldsToDelete[value.domain] || [];

		if (value.addToDeleteCollection) {
			if (value.isSharedField) {
				this.fieldsToDelete[value.domain] = fieldsToDelete.concat(value.fieldsToDelete);
			} else {
				this.fieldsToDelete[value.domain] = value.fieldsToDelete;
			}
		} else {
			if (value.isSharedField) {
				this.fieldsToDelete[value.domain] = fieldsToDelete.filter((field) => value.fieldsToDelete.indexOf(field) === -1);
			} else {
				this.fieldsToDelete[value.domain] = value.fieldsToDelete;
			}
		}
	}

	protected onFilter(): void {
		this.grids.forEach(grid => grid.applyFilter());
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