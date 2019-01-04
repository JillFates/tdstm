import { FieldSettingsModel } from './field-settings.model';

export class DomainModel {
	domain: string;
	fields: Array<FieldSettingsModel>;
	version ? = 0;
	planMethodology?: string;
}