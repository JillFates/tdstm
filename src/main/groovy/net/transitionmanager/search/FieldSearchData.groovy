package net.transitionmanager.search

import com.tdsops.tm.enums.domain.AssetClass

/**
 * Class that contain the information required for constructing a
 * SQL orHQL statement to filter a domain by a particular field.
 */
class FieldSearchData {

    private Map searchInfo = [:]

    private Map parsedInfo = [:]

    boolean mixed = false

    String mixedSqlExpression

    private static final REQUIRED_FIELDS = ["domain", "column", "filter"]

    /**
     * Instantiates a FieldSearchData from a Map.
     * Required entries:
     *  - domain (Class): the domain class being queried.
     *  - filter (String): the expression to parse.
     *  - column (String): the column being filtered.
     *
     *  Optional entries:
     *      - columnAlias (String): a handful string for building namedParameters.
     *      - fieldSpec: (FieldSpec): an instance of {@code FieldSpec} used to calculate types or values
     * @param searchMap
     */
    FieldSearchData(Map searchMap) {
        searchMap.with {
            this.searchInfo = [
                    domain : domain,
                    filter : filter?.trim(),
                    column : column,
                    columnAlias : columnAlias?: column,
                    useWildcards : false,
                    type: type,
                    whereProperty: whereProperty,
	                manyToManyQueries: manyToManyQueries,
                    fieldSpec: fieldSpec,
                    domainAlias: domainAlias,
                    referenceProperty: referenceProperty
            ]
        }

    }

    /**
     *
     * @param domain
     * @param column
     * @param filter
     * @param columnAlias
     */
    FieldSearchData(Class domain, String column, String filter, String columnAlias = null) {
        this([ domain: domain, filter: filter, column: column, columnAlias: columnAlias])
    }

    /**
     * Validate that the minimal information for searching is present.
     * @return error message.
     */
    String validate() {
        List<String> emptyColumns = []
        String errMsg
        // Iterate over the required fields validating they aren't null.
        for (field in REQUIRED_FIELDS) {
            if (!searchInfo[field]) {
                emptyColumns << field
            }
        }
        // If some required fields are empty, construct the error message.
        if (emptyColumns) {
            errMsg = "The following column(s) cannot be null: ${emptyColumns.join(", ")}"
        }
        return errMsg
    }

    void setDomain(Class domain) {
        searchInfo.domain = domain
    }

    Class getDomain() {
        return searchInfo.domain
    }

    void setFilter(String filter) {
        searchInfo.filter = filter?.trim()
    }

    String getFilter() {
        return searchInfo.filter
    }

    void setColumn(String column) {
        searchInfo.column = column
    }

    String getColumn() {
        return searchInfo.column
    }

    void setColumnAlias(String columnAlias) {
        searchInfo.columnAlias = columnAlias
    }

    String getColumnAlias() {
        return searchInfo.columnAlias
    }

    void setUseWildcards(Boolean override) {
        searchInfo.useWildcards = override
    }

    Boolean getUseWildcards() {
        return searchInfo.useWildcards
    }

    void setSqlSearchExpression(String searchExpression) {
        parsedInfo.searchExpression = searchExpression
    }

    String getReferenceProperty(){
        return searchInfo.referenceProperty
    }

    String getSqlSearchExpression() {
        String expression = parsedInfo.searchExpression
        if (isMixed()) {
            expression = "($mixedSqlExpression) OR ${expression})"
        }
        /* If the field is a custom field, in addition to the filter provided by the user,
           the corresponding AssetClass needs to be included to keep the search consistent and avoid
           matches for this custom field in other domains.
        */
        if (!isShared() && isCustomField() && parsedInfo.parameters) {
            /* Generate a name for the AssetClass parameter that will be unique for each domain.
               This is so when the user mixes custom fields from different domains, nothing is returned. */
            AssetClass assetClass = AssetClass.lookup(domain)
            String parameterName = "${assetClass.toString()}_assetClass"
            String assetClassField = "${getDomainAlias()}.assetClass"
            expression = "($expression AND $assetClassField = :$parameterName)"
            addSqlSearchParameter(parameterName, assetClass)
        }
        return expression
    }

    void addSqlSearchParameter(String param, Object value) {
        if (!parsedInfo.parameters) {
            parsedInfo.parameters = [:]
        }

        parsedInfo.parameters[param] = value
    }

    Map getSqlSearchParameters() {
        return parsedInfo.parameters
    }

    boolean isMixed() {
        return mixed
    }

    void setMixed(boolean mixed) {
        this.mixed = mixed
    }

    void setMixedSqlExpression(expression) {
        mixedSqlExpression = expression
    }

    Class getType() {
        return searchInfo.type
    }

    String getWhereProperty() {
        return searchInfo.whereProperty?: column
    }

	Map getManyToManyQueries() {
		return searchInfo.manyToManyQueries
	}

    boolean isManyToMany() {
        return searchInfo.manyToManyQueries != null
    }

    boolean isShared() {
        return searchInfo.fieldSpec?.shared
    }

    boolean isCustomField() {
        return searchInfo.fieldSpec?.isUserDefinedField
    }

    String getDomainAlias() {
        return searchInfo.domainAlias
    }
}