export interface IManufacturerModel {
	id: number;
	name: string;
	aliases: string;
	description: string;
	manufacturer: {
		id: number,
		name: string
	};
	assetType: string;
	bladeCount: any;
	bladeLabelCount: any;
	bladeRows: any;
	sourceTDS: string;
	powerNameplate: number;
	powerDesign: number;
	powerUse: number;
	roomObject: string;
	sourceTDSVersion: number;
	useImage: string;
	usize: number;
	cpuType: any;
	cpuCount: any;
	memorySize: any;
	storageSize: any;
	height: number;
	weight: number;
	depth: number;
	width: number;
	layoutStyle: any;
	productLine: any;
	modelFamily: any;
	endOfLifeDate: any;
	endOfLifeStatus: any;
	createdBy: any;
	updatedBy: string;
	validatedBy: any;
	sourceURL: any;
	modelStatus: string;
	modelScope: any;
	dateCreated: string;
	lastModified: string;
}

export const ManufacturerModelColumns = [
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
		label: 'Aliases',
		property: 'aliases',
		width: 200
	},
	{
		label: 'Description',
		property: 'description',
		width: 200
	},
	{
		label: 'Manufacturer_ID',
		property: 'manufacturerId',
		width: 200
	},
	{
		label: 'Manufacturer_Name',
		property: 'manufacturerName',
		width: 200
	},
	{
		label: 'Asset_Type',
		property: 'assetType',
		width: 200
	},
	{
		label: 'Blade_Count',
		property: 'bladeCount',
		width: 200
	},
	{
		label: 'Blade_Label_Count',
		property: 'bladeLabelCount',
		width: 200
	},
	{
		label: 'Blade_Rows',
		property: 'bladeRows',
		width: 200
	},
	{
		label: 'Source_TDS',
		property: 'sourceTDS',
		width: 200
	},
	{
		label: 'Power_Nameplate',
		property: 'powerNameplate',
		width: 200
	},
	{
		label: 'Power_Design',
		property: 'powerDesign',
		width: 200
	},
	{
		label: 'Power_Use',
		property: 'powerUse',
		width: 200
	},
	{
		label: 'Room_Object',
		property: 'roomObject',
		width: 200
	},
	{
		label: 'Source_TDS_Version',
		property: 'sourceTDSVersion',
		width: 200
	},
	{
		label: 'Use_Image',
		property: 'useImage',
		width: 200
	},
	{
		label: 'U_Size',
		property: 'usize',
		width: 200
	},
	{
		label: 'Cpu_Type',
		property: 'cpuType',
		width: 200
	},
	{
		label: 'Cpu_Count',
		property: 'cpuCount',
		width: 200
	},
	{
		label: 'Memory_Size',
		property: 'memorySize',
		width: 200
	},
	{
		label: 'Storage_Size',
		property: 'storageSize',
		width: 200
	},
	{
		label: 'Height',
		property: 'height',
		width: 200
	},
	{
		label: 'Weight',
		property: 'weight',
		width: 200
	},
	{
		label: 'Depth',
		property: 'depth',
		width: 200
	},
	{
		label: 'Width',
		property: 'width',
		width: 200
	},
	{
		label: 'Layout_Style',
		property: 'layoutStyle',
		width: 200
	},
	{
		label: 'Produc_tLine',
		property: 'productLine',
		width: 200
	},
	{
		label: 'Model_Family',
		property: 'modelFamily',
		width: 200
	},
	{
		label: 'endOfLifeDate',
		property: 'endOfLifeDate',
		width: 200
	},
	{
		label: 'Create_dBy',
		property: 'createdBy',
		width: 200
	},
	{
		label: 'Updated_By',
		property: 'updatedBy',
		width: 200
	},
	{
		label: 'Validated_By',
		property: 'validatedBy',
		width: 200
	},
	{
		label: 'Source_URL',
		property: 'sourceURL',
		width: 200
	},
	{
		label: 'Model_Status',
		property: 'modelStatus',
		width: 200
	},
	{
		label: 'Model_Scope',
		property: 'modelScope',
		width: 200
	},
	{
		label: 'Date_Created',
		property: 'dateCreated',
		width: 200
	},
	{
		label: 'Last_Modified',
		property: 'lastModified',
		width: 200
	}
];