import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DomainModel } from '../model/domain.model';
import {CUSTOM_FIELD_CONTROL_TYPE, FieldSettingsModel} from '../model/field-settings.model';
import {HttpClient} from '@angular/common/http';
import {equals} from 'ramda';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class FieldSettingsService {

	private fieldSettingsUrl = '../ws/customDomain/fieldSpec';

	constructor(private http: HttpClient) {
	}

	getFieldSettingsByDomain(domain = 'ASSETS'): Observable<DomainModel[]> {
		return this.http.get(`${this.fieldSettingsUrl}/${domain}`)
			.map((response: any) => {
				let domains: DomainModel[] = Object.keys(response).map(key => {
					response[key].domain = response[key].domain.toUpperCase();
					return response[key];
				});
				if (domains.length > 0) {
					let sharedFields = domains[0].fields.filter(x => x.shared);
					domains.forEach(d => {
						d.fields.filter(s => s.control === CUSTOM_FIELD_CONTROL_TYPE.YesNo)
							.forEach(s => {
								s.constraints.values = ['Yes', 'No'];
								if (!s.constraints.required) {
									s.constraints.values.splice(0, 0, '');
								}
							});
						sharedFields.forEach(s => {
							let indexOf = d.fields.findIndex(f => f.field === s.field);
							if (indexOf !== -1) {
								d.fields.splice(indexOf, 1, s);
							}
						});
					});
				}
				return domains as any;
			})
			.catch((error: any) => error);
	}

	saveFieldSettings(domains: DomainModel[]): Observable<DomainModel[]> {
		let payload = {};
		domains
			.reduce((p: FieldSettingsModel[], c: DomainModel) => p.concat(c.fields), [])
			.forEach((item: any) => {
				item.constraints.required = +item.constraints.required;
				item.udf = +item.udf;
				item.show = +item.show;
				item.shared = +item.shared;
			});
		domains.forEach(domainModel => {
			payload[domainModel.domain.toUpperCase()] = domainModel;
		});
		return this.http.post(`${this.fieldSettingsUrl}/ASSETS`, JSON.stringify(payload))
			.map((response: any) => response['_body'] ? response : { status: 'Ok' })
			.catch((error: any) => Observable.throw(error || 'Server error'));
	}

	/**
	 * NOTE This function is added here in the service as it needs to be shared with different components.
	 *
	 * Checks if the label parameter string matches other labels in the fields list.
	 * 	  This comparison is case-insensitive and it doesn't take into account any trailing,
	 * 	  leading or in-between spaces.
	 * 	  e.g. 	label: "Last Modified or last modified or LastModified".
	 * 	  		other label: "Last Modified".
	 * 	  		This comparisons will error.
	 * NOTE The comparision at the end is done with "1", because there will always be one positive result in the list
	 * when the label compares to itself.
	 *
	 * @param The label string to be compared with the list of existing labels.
	 */
	conflictsWithAnotherLabel(label: string, fields: any): boolean {
		return fields.filter(
		item => item.label.replace(/\s/g, '').toLowerCase().trim() === label.replace(/\s/g, '').toLowerCase().trim()).length > 1;
	}

	/**
	 * NOTE This function is added here in the service as it needs to be shared with different components.
	 *
	 * Checks if the label String matches any field names in the list of fields.
	 * NOTE This comparison is case-insensitive and it doesn't take into account any trailing, leading or in-between spaces.
	 * 	  e.g. label: "Asset Name" or "asset Name" or "AssetName" or "assetName", and some field name: "assetName". <- This comparisons will error.
	 * @param  The label string to be compared with the list of existing field names.
	 */
	conflictsWithAnotherFieldName(field: any, fields: any): boolean {
		const label = field && field.label || '';
		return fields.some(
				item => !equals(field, item) && item.field.replace(/\s/g, '')
					.toLowerCase().trim() === label.replace(/\s/g, '').toLowerCase().trim()
		);
	}

	/**
	 * Check if the label from the field parameter has conflicts with any label or fieldName from another domains.
	 *
	 * @param field  The field from where we are taking the label string we are using to compare
	 * (It will be used only if the field is shared, otherwise it won't do anything).
	 * @param domains  The domains with the list of fields we will compare to (the list of fields corresponding to the
	 * domain where "field" comes from won't be used, just the list of fields of the other domains).
	 * @param originDomain  The domain to which "fields" belongs to.
	 * @returns {boolean} True if any conflict is found, false otherwise.
	 */
	conflictsWithAnotherDomain(field: any, domains: any, originDomain: any): boolean {
		let conflicts = [];
		// We are going to find conflicts in other domains, so first remove the origin domain from the list of domains
		let filteredDomains = domains.filter((d) => d.domain !== originDomain.domain)
		for (let domain of filteredDomains) {
			let fields = domain.fields;
			let x = 0;
			if (field.shared === 1) {
				conflicts = fields.filter(
					item => item.label.replace(/\s/g, '').toLowerCase().trim() === field.label.replace(/\s/g, '').toLowerCase().trim().length > 0 ||
						this.conflictsWithAnotherFieldName(field.label, fields));
				if (conflicts.length > 0) {
					return true;
				}
			}
		}
		;
		return false;
	}

	/**
	 * Checks for any conflicts between:
	 * - labels an other labels,
	 * - labels and fieldNames,
	 * - labels and other labels and fieldNames from other domains.
	 *
	 * @param fields  The list of fields to check from.
	 * @param domains  The list of all the domains, so conflicts against other domains can be checked as well.
	 * @param originDomain  The domain to which "fields" belongs to.
	 * @returns {boolean}  True if any conflict is found, false otherwise.
	 */
	checkLabelsAndNamesConflicts(fields: any, domains: any, originDomain: any): boolean {
		// Check if there are conflicts among labels
		let labelConflicts = fields.filter((item) => this.conflictsWithAnotherLabel(item.label, fields));
		// Now clone the field list to do the next check (this is needed because the cloned array will be potentially modified)
		let clonedFields = Object.assign([], fields);
		// We will need to remove the element corresponding to the item from the clonedFields array
		// before doing the comparision so it doesn't compare to itself.
		// (otherwise if for example the fieldName is "custom1" and the label is "custom1" in the same field it will error)
		// Here in actualElement we hold the removed element so we can restore it to the array later
		let actualElement = null;
		// Check if there are conflicts between labels and field names
		let nameConflicts = fields.filter((item, index) => {
			// If we have remove an element from the cloned list to
			// do the comparison, restore that element to the list
			if (actualElement != null) {
				clonedFields.splice(index - 1, 0, actualElement[0]);
			}
			// We need to temporarily remove the element corresponding to the item
			// we are using to compare, so item doesn't compare to itself
			actualElement = clonedFields.splice(index, 1);
			return this.conflictsWithAnotherFieldName(item.label, clonedFields)
		});
		// Finally, check if there are conflicts among labels and between labels and field names but against other domains
		let domainConflicts = fields.filter(item => this.conflictsWithAnotherDomain(item, domains, originDomain))

		return (labelConflicts.length > 0 || nameConflicts.length > 0 || domainConflicts.length > 0)
	}
}