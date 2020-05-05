export interface IManufacturerExport {
	id?: number;
	name: string;
	description?: string;
	aliases?: string;
}

export const ManufacturerColumns = [
	{
		label: 'ID',
		property: 'id',
		width: 50
	},
	{
		label: 'Name',
		property: 'name',
		width: 200
	},
	{
		label: 'Description',
		property: 'description',
		width: 200
	},
	{
		label: 'Aliases',
		property: 'aliases',
		width: 200
	},
];