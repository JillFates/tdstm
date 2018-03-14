package com.tdsops.etl

import getl.data.Dataset
import getl.data.Field
import getl.excel.ExcelDriver

class TDSExcelDriver extends ExcelDriver{

	@Override
	protected List<Field> fields(Dataset dataset) {




		return super.fields(dataset)
	}
}
