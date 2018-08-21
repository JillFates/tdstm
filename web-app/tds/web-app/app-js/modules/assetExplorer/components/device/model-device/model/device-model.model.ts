import {Aka} from '../../../../../../shared/components/aka/model/aka.model';
import {Connector} from '../../../../../../shared/components/connector/model/connector.model';

export class DeviceModel {
	public id: string;
	public name: string;
	public manufacturer: string;
	public aka: string;
	public modelName: string;
	public assetType: string;
	public usize: string;
	public powerUse: string;
	public powerNameplate: string;
	public powerDesign: string;
}

export class DeviceModelDetails {
	public manufacturer: { id: string | number, text: string};
	public assetType: { id: string | number, text: string};
	public aka: Array<Aka>;
	public usize: number;
	public weight: number;
	public productLine: string;
	public endOfLifeDate: any;
	public powerNamePlate: number;
	public powerDesign: number;
	public powerUse: number;
	public powerUnit: string;
	public roomObject: boolean;
	public createdBy: string;
	public UpdatedBy: string;
	public validatedBy: string;
	public modelName: string;
	public dimensionH: number;
	public dimensionW: number;
	public dimensionD: number;
	public layoutStyle: string;
	public endOfLineStatus: string;
	public modelFamily: string;
	public Notes: string;
	public sourceTDS: boolean;
	public sourceURL: string;
	public modelStatus: string;
	public connectors: Array<Connector>;
}
