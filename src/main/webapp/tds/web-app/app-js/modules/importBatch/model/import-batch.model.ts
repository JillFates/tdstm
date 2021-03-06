import {GridColumnModel} from '../../../shared/model/data-list-grid.model';
import {ProviderModel} from '../../provider/model/provider.model';
import {DataScriptModel} from '../../dataScript/model/data-script.model';
import {ProjectModel} from '../../../shared/model/project.model';
import {EnumModel} from '../../../shared/model/enum.model';

export enum BatchStatus {
	RUNNING = 'RUNNING',
	PENDING = 'PENDING',
	QUEUED = 'QUEUED',
	COMPLETED = 'COMPLETED',
	IGNORED = 'IGNORED',
	STALLED = 'STALLED',
};

export class ImportBatchModel {
	id: number;
	status: EnumModel;
	domainClassName: any;
	project: ProjectModel;
	provider: ProviderModel;
	datascript: DataScriptModel;
	dataScript: any;
	createdBy: string;
	archived: boolean;
	timezone: string;
	dateFormat: string;
	progressInfoJob: any;
	originalFilename: string;
	nullIndicator: string;
	overwriteWithBlanks: number;
	autoProcess: number;
	warnOnChangesAfter: any;
	fieldNameList: Array<string>;
	fieldLabelMap: any;
	dateCreated: Date;
	lastUpdated: Date;
	recordsSummary: {
		count: number,
		erred: number,
		ignored: number,
		pending: number,
		processed: number
	};
	currentProgress?: number;
	stalledCounter ? = 0;
}

/**
 * Configuration of the Import Batches Grid.
 * Defines the columns.
 */
export class ImportBatchColumnsModel {
	columns: Array<GridColumnModel>;

	constructor() {
		this.columns = [
			{
				label: 'Id',
				property: 'id',
				type: 'number',
				width: 70,
				locked: false
			},
			{
				label: 'Status',
				property: 'status.label',
				type: 'text',
				width: 130,
				locked: false,
				filterable: true
			},
			{
				label: 'Domain',
				property: 'domainClassName',
				type: 'text',
				width: 120,
				locked: false,
				filterable: true
			},
			{
				label: 'Records',
				property: 'recordsSummary.count',
				type: 'number',
				width: 120,
				locked: false,
				cellStyle: { 'text-align': 'right' }
			},
			{
				label: 'Pending',
				property: 'recordsSummary.pending',
				type: 'number',
				width: 120,
				locked: false,
				cellStyle: { 'text-align': 'right' }
			},
			{
				label: 'Processed',
				property: 'recordsSummary.processed',
				type: 'number',
				width: 120,
				locked: false,
				cellStyle: { 'text-align': 'right' }
			},
			{
				label: 'Erred',
				property: 'recordsSummary.erred',
				type: 'number',
				width: 120,
				locked: false,
				cellStyle: { 'text-align': 'right' }
			},
			{
				label: 'Ignored',
				property: 'recordsSummary.ignored',
				type: 'number',
				width: 120,
				locked: false,
				cellStyle: { 'text-align': 'right' }
			},
			{
				label: 'Imported At',
				property: 'dateCreated',
				type: 'datetime',
				format: 'yyyy-MM-dd HH:mm:ss',
				width: 200,
				locked: false,
				filterable: true
			},
			{
				label: 'Last Updated',
				property: 'lastUpdated',
				type: 'datetime',
				format: 'yyyy-MM-dd HH:mm:ss',
				width: 200,
				locked: false,
				filterable: true
			},
			{
				label: 'Imported By',
				property: 'createdBy',
				type: 'text',
				width: 130,
				locked: false,
				filterable: true
			},
			{
				label: 'Provider',
				property: 'provider.name',
				type: 'text',
				width: 130,
				locked: false,
				filterable: true
			},
			{
				label: 'ETL Script',
				property: 'dataScript.name',
				type: 'text',
				width: 160,
				locked: false,
				filterable: true
			},
			{
				label: 'File Name',
				property: 'originalFilename',
				type: 'text',
				width: 150,
				locked: false,
				filterable: true
			}
		];
	}
}
