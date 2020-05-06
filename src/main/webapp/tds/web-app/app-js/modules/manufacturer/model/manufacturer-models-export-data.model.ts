import {IConnector} from './connector.model';
import {IManufacturerModel} from './manufacturer-model.model';
import {IManufacturerExport} from './manufacturer-export.model';

export interface IManufacturerModelsExportData {
	connectors: IConnector[];
	manufacturers: IManufacturerExport[];
	models: IManufacturerModel[];
}