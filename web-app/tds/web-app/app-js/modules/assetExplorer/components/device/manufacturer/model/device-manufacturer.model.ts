import {Aka} from '../../../../../../shared/components/aka/model/aka.model';

export class DeviceManufacturer {
	public manufacturerId: number;
	public name: string;
	public description: string;
	public aka: string;
	public akaCollection: Array<Aka>;
	public corporateLocation: string;
	public corporateName: string;
	public website: string;
}
