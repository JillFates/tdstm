import {process} from '@progress/kendo-data-query';
import {FieldInfoType} from '../../../modules/importBatch/components/record/import-batch-record-fields.component';
import {ValidationUtils} from '../../utils/validation.utils';
import {NULL_OBJECT_LABEL} from '../../model/constants';

/**
 * Class to do common shared operations for building the field reference popups content.
 */
export class FieldReferencePopupHelper {

	protected FieldInfoType = FieldInfoType;
	public popup: any = {
		offset: null,
		show: false,
		type: null,
		gridData: null,
		gridGroups: [{field: 'domainIndex'}]
	};

	/**
	 * Opens and positions the popup based on the click event.
	 * @param {MouseEvent} $event
	 */
	public onShowPopup($event: MouseEvent, type: FieldInfoType, field: any, fieldLabelMap: Array<any>): void {
		let typeString = this.FieldInfoType[type].toLowerCase();
		if (type === FieldInfoType.FIND) {
			this.buildPopupFieldDataForFindObject(field[typeString], fieldLabelMap);
		} else {
			this.buildPopupFieldData(field[typeString], fieldLabelMap);
			if (field.find && field.find.query && field.find.query.length > 0) {
				this.popup.domain = field.find.query[0].domain;
			}
		}
		this.popup.type = type;
		this.popup.mouseEvent = $event;
		if (this.popup.show) {
			this.popup.offset = { left: $event.pageX, top: $event.pageY};
		}
		this.popup.show = true;
	}

	/**
	 * Builds the popup grid field info data.
	 * @param field
	 */
	private buildPopupFieldData(field: any, fieldLabelMap: Array<any>): void {
		let popupFields: Array<any> = [];
		for (let fieldName in field) {
			if (field[fieldName]) {
				popupFields.push({
					fieldName: fieldLabelMap[fieldName] ? fieldLabelMap[fieldName] : fieldName,
					value: field[fieldName]
				});
			}
		}
		this.popup.gridData = process(popupFields, {});
	}

	/**
	 * Builds the popup grid field info data.
	 * @param field
	 */
	private buildPopupFieldDataForFindObject(field: any, fieldLabelMap: Array<any>): void {
		this.popup.results = [];
		const {matchOn, results} = field;
		let popupFields: Array<any> = [];
		field.query.forEach( (item, index) => {
			const domain = item.domain;
			let recordsFound = null;
			if ((matchOn !== null && results !== null) && (matchOn === index) && results.length > 0) {
				recordsFound = results.length;
				this.popup.results = results;
			}
			// safe null check for the new json object structure vs the old one.
			if (item.criteria) {
				item.criteria.forEach( field => {
					popupFields.push({
						domainIndex: index,
						domainName: domain,
						fieldName: fieldLabelMap[field.propertyName] ? fieldLabelMap[field.propertyName] : field.propertyName,
						value: !ValidationUtils.isEmptyObject(field.value) ? field.value : NULL_OBJECT_LABEL,
						operator: field.operator,
						recordsFound: recordsFound
					});
				});
			}
		});
		this.popup.gridData = process(popupFields, { group: this.popup.gridGroups});
	}
}