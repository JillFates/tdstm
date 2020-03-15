package com.tdsops.etl

import com.tdsops.etl.dataset.CSVDataset
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.WorkbookUtil
import getl.excel.ExcelConnection
import getl.excel.ExcelDataset
import getl.json.JSONConnection
import getl.json.JSONDataset
import getl.utils.FileUtils
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.common.FileSystemService
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFRow
import org.spockframework.runtime.SpockAssertionError
import spock.lang.Specification

abstract class ETLBaseSpec extends Specification {

    FileSystemService fileSystemService

    FileSystemService getFileSystemService() {
        if (!fileSystemService) {
            fileSystemService = applicationContext.getBean('fileSystemService')
        }
        return fileSystemService
    }

    /**
     * Builds a list of Mock Room using this fields order
     * ['id', 'project', 'roomName', 'location', 'roomDepth', 'roomWidth', 'address', 'city', 'stateProv', 'postalCode']
     * @param valuesList
     * @return a list of Mock(Room)
     */
    protected List<Room> buildRooms(List<List<?>> valuesList) {
        return valuesList.collect { List<?> values ->
            Room room = Mock()
            room.getId() >> values[0]
            room.getProject() >> values[1]
            room.getRoomName() >> values[2]
            room.getLocation() >> values[3]
            room.getRoomDepth() >> values[4]
            room.getRoomWidth() >> values[5]
            room.getAddress() >> values[6]
            room.getCity() >> values[7]
            room.getStateProv() >> values[8]
            room.getPostalCode() >> values[9]
            room
        }
    }

    /**
     * Builds a list of Mock Room using this fields order
     * ['id', 'project', 'room', 'manufacturer', 'model', 'location', 'front', 'source', 'roomX', 'roomY', 'powerA', 'powerB', 'powerC', 'rackType'],
     * @param valuesList
     * @return a list of Mock(Rack)
     */
    protected List<Rack> buildRacks(List<List<?>> valuesList) {
        return valuesList.collect { List<?> values ->
            Rack rack = Mock()
            rack.getId() >> values[0]
            rack.getProject() >> values[1]
            rack.getRoom() >> values[2]
            rack.getManufacturer() >> values[3]
            rack.getModel() >> values[4]
            rack.getLocation() >> values[5]
            rack.getFront() >> values[6]
            rack.getSource() >> values[7]
            rack.getRoomX() >> values[8]
            rack.getRoomY() >> values[9]
            rack.getPowerA() >> values[10]
            rack.getPowerB() >> values[11]
            rack.getPowerC() >> values[12]
            rack.getRackType() >> values[13]
            rack
        }
    }

    /**
     * Builds a spec structure used to validate asset fields
     * @param field
     * @param label
     * @param type
     * @param required
     * @return a map with the correct fieldSpec format
     */
    protected Map<String, ?> buildFieldSpec(String field, String label, String type = "String", Integer required = 0) {
        return [
                constraints: [
                        required: required
                ],
                control    : type,
                default    : '',
                field      : field,
                imp        : 'U',
                label      : label,
                order      : 0,
                shared     : 0,
                show       : 0,
                tip        : "",
                udf        : 0
        ]
    }


    /**
     * Builds a SpreadSheet dataSet for an Excel content
     * @param sheetName
     * @param sheetContent
     * @return
     */
    protected List buildSpreadSheetXLSXDataSet(String sheetName, String sheetContent) {

        def (String fileName, OutputStream outputStream) = getFileSystemService().createTemporaryFile('unit-test-', 'xlsx')
        Workbook workbook = WorkbookUtil.createWorkbook('xlsx')

        addSheetInXLSXWorkBook(workbook, sheetName, sheetContent)

        WorkbookUtil.saveToOutputStream(workbook, outputStream)

        ExcelConnection con = new ExcelConnection(
                path: getFileSystemService().temporaryDirectory,
                fileName: fileName,
                driver: TDSExcelDriver)
        ExcelDataset dataSet = new ExcelDataset(connection: con, header: true)

        return [fileName, new DataSetFacade(dataSet)]
    }

    /**
     * Builds a SpreadSheet dataSet for an Excel content
     * @param sheetName
     * @param sheetContent
     * @return
     */
    protected List buildSpreadSheetXLSDataSet(String sheetName, String sheetContent) {

        def (String fileName, OutputStream outputStream) = getFileSystemService().createTemporaryFile('unit-test-', 'xls')
        Workbook workbook = WorkbookUtil.createWorkbook('xls')

        addSheetInXLSWorkBook(workbook, sheetName, sheetContent)

        WorkbookUtil.saveToOutputStream(workbook, outputStream)

        ExcelConnection con = new ExcelConnection(
                path: getFileSystemService().temporaryDirectory,
                fileName: fileName,
                driver: TDSExcelDriver)
        ExcelDataset dataSet = new ExcelDataset(connection: con, header: true)

        return [fileName, new DataSetFacade(dataSet)]
    }

    /**
     * Adds a Sheet in a Workbook instance using the sheet name and the sheet contents
     * @param workbook
     * @param sheetName
     * @param sheetContent
     */
    protected void addSheetInXLSWorkBook(Workbook workbook, String sheetName, String sheetContent) {
        Sheet sheet = workbook.createSheet(sheetName)
        sheetContent.readLines().eachWithIndex { String line, int rowNumber ->
            HSSFRow currentRow = sheet.createRow(rowNumber)
            line.split(",").eachWithIndex { String cellContent, int columnNumber ->
                Cell cell = currentRow.createCell(columnNumber)
                WorkbookUtil.setCellValue(cell, cellContent)
            }
        }
    }
    /**
     * Adds a Sheet in a Workbook instance using the sheet name and the sheet contents
     * @param workbook
     * @param sheetName
     * @param sheetContent
     */
    protected void addSheetInXLSXWorkBook(Workbook workbook, String sheetName, String sheetContent) {
        Sheet sheet = workbook.createSheet(sheetName)
        sheetContent.readLines().eachWithIndex { String line, int rowNumber ->
            XSSFRow currentRow = sheet.createRow(rowNumber)
            line.split(",").eachWithIndex { String cellContent, int columnNumber ->
                Cell cell = currentRow.createCell(columnNumber)
                WorkbookUtil.setCellValue(cell, cellContent)
            }
        }
    }

    /**
     * Builds a SpreadSheet dataSet for an Excel content with multiple sheets
     * @param sheetsContent
     * @return
     */
    protected List buildSpreadSheetDataSetWithMultipleSheets(Map<String, String> sheetsContent) {

        def (String fileName, OutputStream outputStream) = getFileSystemService().createTemporaryFile('unit-test-', 'xlsx')
        Workbook workbook = WorkbookUtil.createWorkbook('xlsx')

        // Getting the Sheet at index zero
        sheetsContent.each { String sheetName, String sheetContent ->
            Sheet sheet = workbook.createSheet(sheetName)
            sheetContent.readLines().eachWithIndex { String line, int rowNumber ->
                XSSFRow currentRow = sheet.createRow(rowNumber)
                line.split(",").eachWithIndex { String cellContent, int columnNumber ->
                    Cell cell = currentRow.createCell(columnNumber)
                    WorkbookUtil.setCellValue(cell, cellContent)
                }
            }
        }

        WorkbookUtil.saveToOutputStream(workbook, outputStream)

        ExcelConnection con = new ExcelConnection(
                path: getFileSystemService().temporaryDirectory,
                fileName: fileName,
                driver: TDSExcelDriver)
        ExcelDataset dataSet = new ExcelDataset(connection: con, header: true)

        return [fileName, new DataSetFacade(dataSet)]
    }

    /**
     * Creates an instance of DomainClassFieldsValidator with all the fields spec correctly set.
     * @return an intance of DomainClassFieldsValidator
     */
    protected ETLFieldsValidator createDomainClassFieldsValidator() {
        ETLFieldsValidator validator = new ETLFieldsValidator()
        List<Map<String, ?>> commonFieldsSpec = buildFieldSpecsFor(CustomDomainService.COMMON)

        validator.addAssetClassFieldsSpecFor(ETLDomain.Asset, commonFieldsSpec)
        validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION) + commonFieldsSpec)
        validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE) + commonFieldsSpec)
        validator.addAssetClassFieldsSpecFor(ETLDomain.Storage, buildFieldSpecsFor(AssetClass.STORAGE) + commonFieldsSpec)
        validator.addAssetClassFieldsSpecFor(ETLDomain.Database, buildFieldSpecsFor(AssetClass.DATABASE) + commonFieldsSpec)

        return validator
    }

    /**
     * Builds a CSV dataSet from a csv content
     * @param csvContent
     * @return
     */
    protected List buildCSVDataSet(String csvContent) {
        String fullName = createCSVFIle(csvContent)
        return [fullName, new CSVDataset(fullName)]
    }

    /**
     * Create CSV file using csvContent parameter
     *
     * @param csvContent CSV String content
     * @return a filename created
     */
    protected String createCSVFIle(String csvContent) {

        def (String fileName, OutputStream dataSetOS) = getFileSystemService().createTemporaryFile('unit-test-', 'csv')
        dataSetOS << csvContent.stripIndent().trim()
        dataSetOS.close()

        return getFileSystemService().getTemporaryFullFilename(fileName)
    }

    /**
     * Builds a JSON dataSet for json content
     * @param sheetName
     * @param sheetContent
     * @return
     */
    protected List buildJSONDataSet(String jsonContent) {

        def (String fileName, OutputStream dataSetOS) = getFileSystemService().createTemporaryFile('unit-test-', 'json')
        dataSetOS << jsonContent
        dataSetOS.close()

        String fullName = getFileSystemService().getTemporaryFullFilename(fileName)

        JSONConnection jsonCon = new JSONConnection(config: "json", path: FileUtils.PathFromFile(fullName), driver: TDSJSONDriver)
        JSONDataset dataSet = new JSONDataset(connection: jsonCon, rootNode: "", fileName: FileUtils.FileName(fullName))

        return [fileName, new DataSetFacade(dataSet)]
    }

    /**
     * Helper method to create Fields Specs based on Asset definition
     * @param asset
     * @return
     */
    protected List<Map<String, ?>> buildFieldSpecsFor(def asset) {

        List<Map<String, ?>> fieldSpecs = []
        switch (asset) {
            case AssetClass.APPLICATION:
                fieldSpecs = [
                        buildFieldSpec('appFunction', 'Function', 'String'),
                        buildFieldSpec('appOwner', 'App Owner', 'Person'),
                        buildFieldSpec('appSource', 'Source', 'String'),
                        buildFieldSpec('appTech', 'Technology', 'String'),
                        buildFieldSpec('appVendor', 'Vendor', 'String'),
                        buildFieldSpec('appVersion', 'Version', 'String'),
                        buildFieldSpec('businessUnit', 'Business Unit', 'String'),
                        buildFieldSpec('criticality', 'Criticality', 'InList'),
                        buildFieldSpec('drRpoDesc', 'DR RPO', 'String'),
                        buildFieldSpec('drRtoDesc', 'DR RTO', 'String'),
                        buildFieldSpec('latency', 'Latency OK', 'YesNo'),
                        buildFieldSpec('license', 'License', 'String'),
                        buildFieldSpec('maintExpDate', 'Maint Expiration', 'Date'),
                        buildFieldSpec('retireDate', 'Retire Date', 'Date'),
                        buildFieldSpec('shutdownBy', 'Shutdown By', 'String'),
                        buildFieldSpec('shutdownDuration', 'Shutdown Duration', 'Number'),
                        buildFieldSpec('shutdownFixed', 'Shutdown Fixed', 'Number'),
                        buildFieldSpec('sme', 'SME1', 'Person'),
                        buildFieldSpec('sme2', 'SME2', 'Person'),
                        buildFieldSpec('startupBy', 'Startup By', 'String'),
                        buildFieldSpec('startupDuration', 'Startup Duration', 'Number'),
                        buildFieldSpec('startupFixed', 'Startup Fixed', 'Number'),
                        buildFieldSpec('startupProc', 'Startup Proc OK', 'YesNo'),
                        buildFieldSpec('testingBy', 'Testing By', 'String'),
                        buildFieldSpec('testingDuration', 'Testing Duration', 'Number'),
                        buildFieldSpec('testingFixed', 'Testing Fixed', 'Number'),
                        buildFieldSpec('testProc', 'Test Proc OK', 'YesNo'),
                        buildFieldSpec('url', 'URL', 'String'),
                        buildFieldSpec('useFrequency', 'Use Frequency', 'String'),
                        buildFieldSpec('userCount', 'User Count', 'String'),
                        buildFieldSpec('userLocations', 'User Locations', 'String'),
                        buildFieldSpec('custom1', 'Network Interfaces', 'String'),
                        buildFieldSpec('custom2', 'SLA Name', 'String'),
                        buildFieldSpec('custom3', 'Cost Basis', 'String'),
                        buildFieldSpec('custom4', 'OPS Manual', 'String'),
                        buildFieldSpec('custom5', 'DR Plan', 'String'),
                        buildFieldSpec('custom6', 'App Code', 'String'),
                        buildFieldSpec('custom7', 'Latency Timing', 'String'),
                        buildFieldSpec('custom8', 'App ID', 'String'),
                        buildFieldSpec('custom9', 'Backup Plan Complete', 'String'),
                        buildFieldSpec('custom10', 'Custom10', 'String'),
                        buildFieldSpec('custom11', 'Custom11', 'String'),
                        buildFieldSpec('custom12', 'Custom12', 'String'),
                ]
                break
            case AssetClass.DATABASE:
                fieldSpecs = [
                        buildFieldSpec('dbFormat', 'Format', 'String'),
                        buildFieldSpec('retireDate', 'Retire Date', 'Date'),
                        buildFieldSpec('size', 'Size', 'String'),
                        buildFieldSpec('scale', 'Scale', 'String'),
                        buildFieldSpec('maintExpDate', 'Maint Expiration', 'Date'),
                        buildFieldSpec('rateOfChange', 'Rate Of Change', 'Number'),
                        buildFieldSpec('custom1', 'Network Interfaces', 'String'),
                        buildFieldSpec('custom2', 'SLA Name', 'String'),
                        buildFieldSpec('custom3', 'Cost Basis', 'String'),
                        buildFieldSpec('custom4', 'OPS Manual', 'String'),
                        buildFieldSpec('custom5', 'DR Plan', 'String'),
                        buildFieldSpec('custom6', 'App Code', 'String'),
                        buildFieldSpec('custom7', 'Latency Timing', 'String'),
                        buildFieldSpec('custom8', 'App ID', 'String'),
                        buildFieldSpec('custom9', 'Backup Plan Complete', 'String'),
                        buildFieldSpec('custom10', 'Custom10', 'String'),
                        buildFieldSpec('custom11', 'Custom11', 'String'),
                        buildFieldSpec('custom12', 'Custom12', 'String'),
                ]
                break
                break
            case AssetClass.DEVICE:
                fieldSpecs = [
                        buildFieldSpec('assetTag', 'Asset Tag', 'String'),
                        buildFieldSpec('assetType', 'Device Type', 'AssetType'),
                        buildFieldSpec('cart', 'Cart', 'String'),
                        buildFieldSpec('ipAddress', 'IP Address', 'String'),
                        buildFieldSpec('maintExpDate', 'Maint Expiration', 'Date'),
                        buildFieldSpec('manufacturer', 'Manufacturer', 'String'),
                        buildFieldSpec('model', 'Model', 'String'),
                        buildFieldSpec('os', 'OS', 'String'),
                        buildFieldSpec('priority', 'Priority', 'Options.Priority'),
                        buildFieldSpec('railType', 'Rail Type', 'InList'),
                        buildFieldSpec('rateOfChange', 'Rate Of Change', 'Number'),
                        buildFieldSpec('retireDate', 'Retire Date', 'Date'),
                        buildFieldSpec('scale', 'Scale', 'String'),
                        buildFieldSpec('serialNumber', 'Serial #', 'String'),
                        buildFieldSpec('shelf', 'Shelf', 'String'),
                        buildFieldSpec('shortName', 'Alternate Name', 'String'),
                        buildFieldSpec('size', 'Size', 'Number'),
                        buildFieldSpec('sourceBladePosition', 'Source Blade Position', 'Number'),
                        buildFieldSpec('sourceChassis', 'Source Chassis', 'Chassis.S'),
                        buildFieldSpec('locationSource', 'Source Location', 'Location.S'),
                        buildFieldSpec('rackSource', 'Source Rack', 'Rack.S'),
                        buildFieldSpec('sourceRackPosition', 'Source Position', 'Number'),
                        buildFieldSpec('roomSource', 'Source Room', 'Room.S'),
                        buildFieldSpec('targetBladePosition', 'Target Blade Position', 'Number'),
                        buildFieldSpec('targetChassis', 'Target Chassis', 'Chassis.T'),
                        buildFieldSpec('locationTarget', 'Target Location', 'Location.T'),
                        buildFieldSpec('rackTarget', 'Target Rack', 'Rack.T'),
                        buildFieldSpec('targetRackPosition', 'Target Position', 'Number'),
                        buildFieldSpec('roomTarget', 'Target Room', 'Room.T'),
                        buildFieldSpec('truck', 'Truck', 'String'),
                        buildFieldSpec('custom1', 'Network Interfaces', 'String'),
                        buildFieldSpec('custom2', 'SLA Name', 'String'),
                        buildFieldSpec('custom3', 'Cost Basis', 'String'),
                        buildFieldSpec('custom4', 'OPS Manual', 'String'),
                        buildFieldSpec('custom5', 'DR Plan', 'String'),
                        buildFieldSpec('custom6', 'App Code', 'String'),
                        buildFieldSpec('custom7', 'Latency Timing', 'String'),
                        buildFieldSpec('custom8', 'App ID', 'String'),
                        buildFieldSpec('custom9', 'Backup Plan Complete', 'String'),
                        buildFieldSpec('custom10', 'Custom10', 'String'),
                        buildFieldSpec('custom11', 'Custom11', 'String'),
                        buildFieldSpec('custom12', 'Custom12', 'String'),
                ]
                break
            case ETLDomain.Dependency:
                fieldSpecs = [
                        buildFieldSpec('id', 'Id', 'Number'),
                        buildFieldSpec('assetName', 'AssetName'),
                        buildFieldSpec('assetType', 'AssetType'),
                        buildFieldSpec('asset', 'Asset'),
                        buildFieldSpec('dependent', 'Dependent'),
                        buildFieldSpec('comment', 'Comment'),
                        buildFieldSpec('status', 'Status'),
                        buildFieldSpec('dataFlowFreq', 'DataFlowFreq'),
                        buildFieldSpec('dataFlowDirection', 'DataFlowDirection')
                ]
                break
            case CustomDomainService.COMMON:
                fieldSpecs = [
                        buildFieldSpec('id', 'Id', 'Number'),
                        buildFieldSpec('assetName', 'Name', 'String'),
                        buildFieldSpec('description', 'Description', 'String'),
                        buildFieldSpec('environment', 'Environment', 'Options.Environment'),
                        buildFieldSpec('externalRefId', 'External Ref Id', 'String'),
                        buildFieldSpec('lastUpdated', 'Modified Date', 'String'),
                        buildFieldSpec('moveBundle', 'Bundle', 'String'),
                        buildFieldSpec('planStatus', 'Plan Status', 'Options.PlanStatus'),
                        buildFieldSpec('supportType', 'Support', 'String'),
                        buildFieldSpec('validation', 'Validation', 'InList'),
                        buildFieldSpec('assetClass', 'Asset Class', 'String'),
                ]
                break
            case AssetClass.STORAGE:
                fieldSpecs = [
                        buildFieldSpec('LUN', 'LUN', 'String'),
                        buildFieldSpec('fileFormat', 'Format', 'String'),
                        buildFieldSpec('size', 'Size', 'String'),
                        buildFieldSpec('rateOfChange', 'Rate Of Change', 'Number'),
                        buildFieldSpec('scale', 'Scale', 'String'),
                        buildFieldSpec('custom1', 'Network Interfaces', 'String'),
                        buildFieldSpec('custom2', 'SLA Name', 'String'),
                        buildFieldSpec('custom3', 'Cost Basis', 'String'),
                        buildFieldSpec('custom4', 'OPS Manual', 'String'),
                        buildFieldSpec('custom5', 'DR Plan', 'String'),
                        buildFieldSpec('custom6', 'App Code', 'String'),
                        buildFieldSpec('custom7', 'Latency Timing', 'String'),
                        buildFieldSpec('custom8', 'App ID', 'String'),
                        buildFieldSpec('custom9', 'Backup Plan Complete', 'String'),
                        buildFieldSpec('custom10', 'Custom10', 'String'),
                        buildFieldSpec('custom11', 'Custom11', 'String'),
                        buildFieldSpec('custom12', 'Custom12', 'String'),
                ]
                break
        }

        return fieldSpecs
    }

    /**
     * Assertions for a {@code FieldResult} instance
     * @param fieldResult
     * @param originalValue
     * @param value
     * @param initValue
     * @param errors
     * @param warn
     */
    def assertFieldResult(Map<String, ?> fieldResult,
                          Object originalValue = null,
                          Object value = null,
                          Object initValue = null,
                          List errors = [],
                          Boolean warn = false) {
        assert fieldResult.originalValue == originalValue
        assert fieldResult.value == value
        assert fieldResult.init == initValue
        assert fieldResult.errors == errors
        assert fieldResult.warn == warn
        return true
    }

    /**
     * Assertions for a {@code QueryResult} instance
     * //TODO dcorrea add an example
     * @param queryResult
     * @param domain
     * @param values
     */
    def assertQueryResult(Map<String, ?> queryResult, ETLDomain domain, List<List<Object>> values) {
        assert queryResult.domain == domain.name()
        queryResult.criteria.eachWithIndex { Map map, int i ->
            assert map['propertyName'] == values[i][0]
            assert map['operator'] == values[i][1]
            assert map['value'] == values[i][2]
            return true
        }
    }

    /**
     * Assert if a {@code FindCondition} is complete and the rest of the fields are correct
     * @param condition
     * @param propertyName
     * @param operator
     * @param value
     */
    def assertFindConditionComplete(
            FindCondition condition,
            String propertyName,
            FindOperator operator,
            Object value,
            Boolean isComplete = true
    ) {
        assert propertyName == condition.propertyName
        assert operator == condition.operator
        assert value == condition.value
        assert condition.isComplete() == isComplete
        return true
    }

    def assertWith(Object target, Closure<?> closure) {
        if (target == null) {
            throw new SpockAssertionError("Target of 'with' block must not be null");
        }
        closure.setDelegate(target)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
        return closure.call(target)
    }

    def assertWith(Object target, Class<?> type, Closure closure) {
        if (target != null && !type.isInstance(target)) {
            throw new SpockAssertionError(String.format("Expected target of 'with' block to have type '%s', but got '%s'",
                    type, target.getClass().getName()))
        }
        return assertWith(target, closure)
    }
}
