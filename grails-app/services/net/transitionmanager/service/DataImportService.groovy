package net.transitionmanager.service

import com.tdsops.common.sql.SqlUtil
import com.tdsops.etl.DataImportHelper
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.DataTransferValue
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import org.codehaus.groovy.grails.web.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate

@Slf4j(value='log', category='net.transitionmanager.service.DataImportService')
@Transactional
class DataImportService implements ServiceMethods{

    SecurityService securityService
    JdbcTemplate jdbcTemplate

    // private Logger log = LoggerFactory.getLogger(DataImportService.class)


    /**
     * Root of the SQL Statement to insert Data Transer Values into the database.
     */
    private static final String DTV_SQL_INSERT =
            '''
            INSERT INTO data_transfer_value
            (asset_entity_id, data_transfer_batch_id, import_value, corrected_value, field_name, has_error, error_text, row_id)
            VALUES 
            '''


    /**
     *
     * Entry point for the process of importing assets.
     *
     * Create a DataTransfer batch for each asset class present in the JSON file being loaded for import.
     *
     * @param userLogin
     * @param project
     * @param fqFilename - the fully qualified path and filename of the source JSON file
     * @return a map containing details about the import creation
     *      batchesCreated: Integer - count of the number of batches created
     *      domains: List - A list of each domain that assets were created
     *          assetClass: String - name of the asset class
     *          assetsCreated: Integer - a count of the number of assets created
     */
    Map loadJsonIntoImportBatch(UserLogin userLogin, Project project, InputStream inputStream) {
        JSONObject assetClasses = JsonUtil.parseFile(inputStream)
        // Iterate over the asset classes.
        Map results = [ batchesCreated: 0, domains:[] ]
        assetClasses.each { assetClass, assets ->
            // Create a Transfer Batch for the asset class
            DataTransferBatch dataTransferBatch = createDataTransferBatch(assetClass, userLogin, project)
            if (dataTransferBatch) {
                results.batchesCreated++
                // Map with info for logging and validations during the process
                Map params = [assetClass: assetClass, project: project, assetIdx: 0]
                // Import the assets for this batch.
                importAssetBatch(dataTransferBatch, assets, params)

                results.domains << [assetClass: assetClass, assetsCreated: params.assetIdx]
            }
        }
        return results
    }

    /**
     * Create a Transfer Batch for the given Asset Class.
     * @param assetClass
     * @param currentUser
     * @param project
     * @return
     */
    private DataTransferBatch createDataTransferBatch(String assetClass, UserLogin currentUser, Project project) {
        DataTransferSet dts = DataTransferSet.findBySetCode('ETL')
        if (!dts) {
            dts = DataTransferSet.get(1)
        }
        DataTransferBatch dataTransferBatch = new DataTransferBatch(
                statusCode: "PENDING",
                transferMode: "I",
                project: project,
                userLogin: currentUser,
                eavEntityType: EavEntityType.findByDomainName(assetClass),
                dataTransferSet: dts
                )

        // Check if the transfer batch is valid, report the error if not.
        if (!dataTransferBatch.save()) {
            log.error 'DataImportService.createDataTransferBatch() failed save - {}', GormUtil.allErrorsString(dataTransferBatch)
            dataTransferBatch = null
        }
        return dataTransferBatch
    }

    /**
     * Import all the assets for the given batch.
     *
     * @param dataTransferBatch - current batch
     * @param assets - list of assets
     * @param params - additional parameters required for logging
     */
    private void importAssetBatch(DataTransferBatch dataTransferBatch, List assets, Map params) {
        // Iterate over the assets
        for (asset in assets) {
            // Process the fields for this asset.
            importAssetFields(dataTransferBatch, asset, params)
            // Update the index for the next asset.
            params.assetIdx++
        }
    }

    /**
     * Import an individual asset.
     *
     * @param dataTransferBatch
     * @param asset - LazyMap with all the field information
     * @param params - additional parameters required for logging
     */
    private void importAssetFields(DataTransferBatch dataTransferBatch, asset, Map params) {
        // Validate this asset
        validateAsset(asset, params)
        // Look up the Asset Entity Id
        Long assetId = lookUpIdValueForJsonAsset(asset)
        // Proceed to import all the fields for this asset.
        importAssetFields(dataTransferBatch, asset, assetId, params)
    }

    /**
     * Import the fields for an asset.
     * This is done by running a single INSERT statement with the values for
     * all the corresponsding DataTransferValues
     *
     * @param dataTransferBatch
     * @param asset
     * @param assetId
     * @param params
     */
    private void importAssetFields(DataTransferBatch dataTransferBatch, asset, Long assetId, Map params) {
/*

        // Build a list of statements for inserting all the DTVs for this asset.
        String sqlValues = buildInsertSQLForDtvValues(dataTransferBatch, asset, assetId, params)
        // Build the query
        String query = DTV_SQL_INSERT + sqlValues
        // Execute the query, inserting all the DTVs for the current asset.
        jdbcTemplate.execute(query)

*/
        insertDataTransferValuesForAsset(dataTransferBatch, asset, assetId, params)
    }

    def insertDataTransferValuesForAsset = { DataTransferBatch dataTransferBatch, asset, Long assetId, Map params ->
        for (element in asset.elements) {

            DataTransferValue dtv = new DataTransferValue(
                dataTransferBatch: dataTransferBatch,
                fieldName: element.field.name,
                assetEntityId: assetId,
                importValue: element.originalValue,
                correctedValue: element.value,
                rowId: params.assetIdx
//                EavAttribute eavAttribute
            )
            if (asset.reference) {
                if (asset.reference.size() > 1) {
                    dtv.hasError = 1
                    dtv.errorText = "Multiple assets referenced"
                }
            }

            if (!dtv.save()) {
                //println "insertDataTransferValuesForAsset() failed to save"
                //log.error 'DataImportService.insertDataTransferValuesForAsset() failed save - {}', GormUtil.allErrorsString(dtv)
                println "DataImportService.insertDataTransferValuesForAsset() failed save - ${GormUtil.allErrorsString(dtv)}"
            }
        }

    }

    /**
     * Build the lines for inserting all the DataTransferValue required.
     *
     * @param dataTransferBatch
     * @param asset
     * @param assetId
     * @param params
     * @return
     */
    private String buildInsertSQLForDtvValues(DataTransferBatch dataTransferBatch, asset, Long assetId, Map params) {
        String[] dtvLines = []
        // Iterate over the fields for this asset.
        for (field in asset.elements) {
            // Construct the line for inserting the DTV
            String sqlLine = getDtvSqlValues(dataTransferBatch, field, assetId, params)
            // Add the line for execution only if it's not null.
            if (sqlLine) {
                dtvLines << lines
            }
        }

        return dtvLines.join(SqlUtil.COMMA)
    }


    /**
     * Build the SQL for inserting the data for a single Data Transfer Value.
     *
     * @param sqlValues
     * @param dataTransferBatch
     * @param fieldJson
     * @param assetId
     *
     * @return sql line with all the values for a data transfer value.
     */
    private String getDtvSqlValues(DataTransferBatch dataTransferBatch, fieldJson, Long assetId, Map params) {

        // If both values are empty (or null) don't build the line
        // TODO: we could do the same for existing assets when both values are the same
        if (StringUtil.isBlank(fieldJson.originalValue) && StringUtil.isBlank(fieldJson.value)) {
            return null
        }

        StringBuilder sqlValues = new StringBuilder()

        // Parse the values for this field
        Map parsingResults = DataImportHelper.parseFieldValues(fieldJson, params)
println "getDtvSqlValues(assetId=$assetId) parsingResults=$parsingResults"
        sqlValues.append(SqlUtil.LEFT_PARENTHESIS)
        sqlValues.append(assetId).append(SqlUtil.COMMA) // AssetId
        sqlValues.append(dataTransferBatch.id).append(SqlUtil.COMMA) // Batch Id

        appendStringValue(sqlValues, parsingResults.originalValue).append(SqlUtil.COMMA) // import value
        appendStringValue(sqlValues, parsingResults.newValue).append(SqlUtil.COMMA) // corrected value
        appendStringValue(sqlValues, fieldJson.field.name).append(SqlUtil.COMMA) // field name

        // If there were errors while parsing this field's values, add the message received.
        if (parsingResults.errorMsg) {
            sqlValues.append(1).append(SqlUtil.COMMA) // has error
            sqlValues.append(errorMsg).append(SqlUtil.COMMA) // error msg
        } else {
            sqlValues.append(0).append(SqlUtil.COMMA) // has error
            sqlValues.append(SqlUtil.NULL).append(SqlUtil.COMMA) // error msg
        }
        sqlValues.append(params.assetIdx).append(SqlUtil.COMMA) // asset index within the batch. TODO: Do we need this?
        sqlValues.append(SqlUtil.RIGHT_PARENTHESIS)

        return sqlValues.toString()

    }

    /**
     *  Check if the json for this asset is valid in terms of the content. Validate
     *  the id is present (and wasn't changed) and also the asset name.
     *
     *  If an error is detected, an exception will be thrown.
     *
     * @param asset
     * @param params - additional parameters required for logging
     */
    private void validateAsset(asset, Map params) throws RuntimeException{
        // Validate fields until the first error is detected.
        String errorMsg = DataImportHelper.validateAsset(asset, params)
        // If there was an error detected, report it and throw an exception.
        if (errorMsg) {
            securityService.reportViolation(errorMsg)
            throw new RuntimeException(errorMsg)
        }
    }

    /**
     * Retrieve the 'id' for the given asset.
     * @param asset
     * @return
     */
    private static Long lookUpIdValueForJsonAsset(asset) {
        def field = DataImportHelper.findFieldInAssetJson(asset, DataImportHelper.ID_FIELD)
        return NumberUtil.toPositiveLong(field.originalValue)
    }


    /**
     * Append a quotation mark, the escaped string and a trailing quotation mark to the String Builder.
     *
     * @param sql
     * @param value
     */
    private static void appendStringValue(StringBuilder sb, String value) {
        sb.append(SqlUtil.STRING_QUOTE)
        sb.append(SqlUtil.escapeStringParameter(value))
        sb.append(SqlUtil.STRING_QUOTE)
    }
}
