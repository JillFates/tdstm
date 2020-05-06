export interface IConnector {
	id?: number;
	connector?: string;
	connectorPosX?: number;
	connectorPosY?: number;
	label?: string;
	labelPosition?: string;
	model: {
		id?: number,
		name?: string
	};
	option?: any;
	status?: string;
	type?: string;
}

export const ConnectorColumns = [
	{
		label: 'ID',
		property: 'id',
		width: 50
	},
	{
		label: 'Connector',
		property: 'connector',
		width: 200
	},
	{
		label: 'ConnectorPosX',
		property: 'connectorPosX',
		width: 200
	},
	{
		label: 'ConnectorPosY',
		property: 'connectorPosY',
		width: 200
	},
	{
		label: 'Label',
		property: 'label',
		width: 200
	},
	{
		label: 'LabelPosition',
		property: 'labelPosition',
		width: 200
	},
	{
		label: 'Model_ID',
		property: 'modelId',
		width: 200
	},
	{
		label: 'Model_Name',
		property: 'modelName',
		width: 200
	},
	{
		label: 'Option',
		property: 'option',
		width: 200
	},
	{
		label: 'Status',
		property: 'status',
		width: 200
	},
	{
		label: 'Type',
		property: 'type',
		width: 200
	},
];
