import {Aka, AkaChanges} from '../../../../../../shared/components/aka/model/aka.model';

export class DeviceManufacturer {
	public id: number;
	public name: string;
	public description: string;
	public aka: string;
	public akaCollection: Array<Aka>;
	public corporateLocation: string;
	public corporateName: string;
	public website: string;
	public akaChanges: AkaChanges
}
