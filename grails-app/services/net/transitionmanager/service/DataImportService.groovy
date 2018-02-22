package net.transitionmanager.service

import com.tdsops.common.sql.SqlUtil
import com.tdsops.etl.DataImportHelper
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import com.tdssrc.grails.StringUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.DataTransferValue
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import org.codehaus.groovy.grails.web.json.JSONObject
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
    private static final String DTV_SQL_INSERT = '''
            INSERT INTO data_transfer_value
            (asset_entity_id, data_transfer_batch_id, import_value, corrected_value, field_name, has_error, error_text, row_id)
            VALUES 
        '''.stripIndent()

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
        JSONObject assetsJson = JsonUtil.parseFile(inputStream)

        // Map which summarizes the results from the import process.
        Map results = [ batchesCreated: 0, domains:[], errors: []]

        List domains = assetsJson.domains

        // Check if the JSON contains any domains. If not, return the results map as is.
        if (!domains) {
            return results
        }

        // Iterate over the domains
        for (domainJson in domains) {
            String assetClass = domainJson.domain

            // Create a Transfer Batch for the asset class
            DataTransferBatch dataTransferBatch = createDataTransferBatch(assetClass, userLogin, project, results.errors)

            // Proceed with the import if the dtb is not null (if it is, the errors were already reported and added to the processErrors list).
            if (dataTransferBatch) {
                // Map with info for logging and validations during the process
                Map params = [assetClass: assetClass, project: project, assetIdx: 0, fields: domainJson.fields]

                List<String> newErrors = importAssetsIntoBatch (dataTransferBatch, domainJson.data, params)
                int errorCount = newErrors.size()
                if (errorCount > 0) {
                    // Record the errors into the Batch importResults
                    String results = "<h2>Import Errors</h2><ul>" +
                        errors.collect { "<li>$it</li>" } +
                        "</ul>"
                    dataTransferBatch.importResults = results
                }
                results.batchesCreated++
                results.domains << [assetClass: assetClass, assetsCreated: params.assetIdx, errorCount: errorCount]
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
    private DataTransferBatch createDataTransferBatch(String assetClass, UserLogin currentUser, Project project, List<String> processErrors) {
        DataTransferSet dts = DataTransferSet.findBySetCode('ETL')
        if (!dts) {
            // TODO : JPM 2/2018 : Augusto - what is going on here?
            dts = DataTransferSet.get(1)
        }

        // Check if the domain class is valid
        EavEntityType eavEntityType = EavEntityType.findByDomainName(assetClass)

        // If the asset class is invalid, return null
        if (!eavEntityType) {
            String error = "Import does not support domain type ${assetClass}"
            log.error(error)
            processErrors << error
            return null
        }

        DataTransferBatch dataTransferBatch = new DataTransferBatch(
                statusCode: "PENDING",
                transferMode: "I",
                project: project,
                userLogin: currentUser,
                eavEntityType: eavEntityType,
                dataTransferSet: dts
            )

        // Check if the transfer batch is valid, report the error if not
        if (!dataTransferBatch.save()) {
            String error = ""
            log error(error)
            processErrors << "An error occurred when creating the batch for ${assetClass}"
            log.error 'DataImportService.createDataTransferBatch() failed save: {}', GormUtil.allErrorsString(dataTransferBatch)

            // Use discard to get the domain object out of the session
            dataTransferBatch.discard()
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
     * @return a list of any errors that occurred during the importing
     */
    private List<String> importAssetsIntoBatch (DataTransferBatch dataTransferBatch, List assets , Map params) {
        List errors = []

        for (asset in assets) {
            List<String> newErrors = createFieldsForRow(dataTransferBatch, asset, params)
            if (newErrors) {
                errors.addAll(newErrors)
            }
            params.assetIdx++
        }

        return errors
    }

    /**
     * Import an individual asset.
     *
     * @param dataTransferBatch
     * @param asset - LazyMap with all the field information
     * @param params - additional parameters required for logging
     */
    private List<String> createFieldsForRow(DataTransferBatch dataTransferBatch, asset, Map params) {
        List<String> errors = []

        // Validate this asset
        // TODO : JPM 2/2018 : Change so that validateAsset returns list of errors
        boolean validAsset = validateAsset(asset, params)
        
        // Proceed with the import if the asset passed validations (if not, the fields that failed will have the corresponding error messages)
        if (! validAsset) {
            errors << "Row ${params.assetIdx} failed validation"
        } else {
            // Look up the Asset Entity Id
            Long assetId = lookUpIdValueForJsonAsset(asset)

            // Proceed to import all the fields for this asset
            for (fieldName in params.fields) {
                // Avoid inserting the 'id' into the batch as a field
                if (fieldName == 'id') {
                    continue
                }

                JSONObject fieldJSON = assetJson.fields[fieldName]
                def newValue = DataImportHelper.resolveFieldValue(fieldName, fieldJSON)
                DataTransferValue dtv = new DataTransferValue(
                    dataTransferBatch: dataTransferBatch,
                    fieldName: fieldName,
                    assetEntityId: assetId,
                    importValue: fieldJSON.originalValue,
                    correctedValue: newValue,
                    rowId: params.assetIdx
                )

                if (!dtv.save()) {
                    errors << "Row ${params.assetIdx} field $fieldName failed to save: ${GormUtil.allErrorsString(dtv)}"
                    log.warn "DataImportService.insertDataTransferValuesForAsset() failed save: ${GormUtil.allErrorsString(dtv)}"
                    dtv.discard()
                }
            }
        }
        return errors
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
    // private String buildInsertSQLForDtvValues(DataTransferBatch dataTransferBatch, asset, Long assetId, Map params) {
    //     String[] dtvLines = []
    //     // Iterate over the fields for this asset.
    //     for (field in asset.fields) {
    //         // Construct the line for inserting the DTV
    //         String sqlLine = getDtvSqlValues(dataTransferBatch, field, assetId, params)
    //         // Add the line for execution only if it's not null.
    //         if (sqlLine) {
    //             dtvLines << lines
    //         }
    //     }

    //     return dtvLines.join(SqlUtil.COMMA)
    // }


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
    // private String getDtvSqlValues(DataTransferBatch dataTransferBatch, fieldJson, Long assetId, Map params) {

    //     // If both values are empty (or null) don't build the line
    //     // TODO: we could do the same for existing assets when both values are the same
    //     if (StringUtil.isBlank(fieldJson.originalValue) && StringUtil.isBlank(fieldJson.value)) {
    //         return null
    //     }

    //     StringBuilder sqlValues = new StringBuilder()

    //     // Parse the values for this field
    //     Map parsingResults = DataImportHelper.parseFieldValues(fieldJson, params)
    //     println "getDtvSqlValues(assetId=$assetId) parsingResults=$parsingResults"

    //     sqlValues.append(SqlUtil.LEFT_PARENTHESIS)
    //     sqlValues.append(assetId).append(SqlUtil.COMMA) // AssetId
    //     sqlValues.append(dataTransferBatch.id).append(SqlUtil.COMMA) // Batch Id

    //     appendStringValue(sqlValues, parsingResults.originalValue).append(SqlUtil.COMMA) // import value
    //     appendStringValue(sqlValues, parsingResults.newValue).append(SqlUtil.COMMA) // corrected value
    //     appendStringValue(sqlValues, fieldJson.field.name).append(SqlUtil.COMMA) // field name

    //     // If there were errors while parsing this field's values, add the message received.
    //     if (parsingResults.errorMsg) {
    //         sqlValues.append(1).append(SqlUtil.COMMA) // has error
    //         sqlValues.append(errorMsg).append(SqlUtil.COMMA) // error msg
    //     } else {
    //         sqlValues.append(0).append(SqlUtil.COMMA) // has error
    //         sqlValues.append(SqlUtil.NULL).append(SqlUtil.COMMA) // error msg
    //     }
    //     sqlValues.append(params.assetIdx).append(SqlUtil.COMMA) // asset index within the batch. TODO: Do we need this?
    //     sqlValues.append(SqlUtil.RIGHT_PARENTHESIS)

    //     return sqlValues.toString()

    // }

    /**
     *  Check if the json for this asset is valid in terms of the content. Validate
     *  the id is present (and wasn't changed) and also the asset name.
     *
     *  If an error is detected, an exception will be thrown.
     *
     * @param asset
     * @param params - additional parameters required for logging
     *
     * @return true: the asset is valid, false otherwise.
     */
    private boolean validateAsset(asset, Map params) throws RuntimeException {
        // Validate fields until the first error is detected.
        return DataImportHelper.validateAsset(asset, params)
    }

    /**
     * Retrieve the 'id' for the given asset.
     * @param asset
     * @return
     */
    private static Long lookUpIdValueForJsonAsset(JSONObject assetJson) {
        return DataImportHelper.resolveAssetId(assetJson)
    }

    /**
     * Append a quotation mark, the escaped string and a trailing quotation mark to the String Builder.
     *
     * @param sql
     * @param value
     */
    // private static void appendStringValue(StringBuilder sb, String value) {
    //     sb.append(SqlUtil.STRING_QUOTE)
    //     sb.append(SqlUtil.escapeStringParameter(value))
    //     sb.append(SqlUtil.STRING_QUOTE)
    // }
}
