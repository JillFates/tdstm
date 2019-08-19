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
     *
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
                    domainAlias: domainAlias,
                    shared: shared
            ]
        }

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

    String getSqlSearchExpression() {
        String expression = parsedInfo.searchExpression
        if (isMixed()) {
            expression = "($mixedSqlExpression) OR ${expression})"
        }
        /* If the field is a custom field, in addition to the filter provided by the user,
           the corresponding AssetClass needs to be included to keep the search consistent and avoid
           matches for this custom field in other domains.
        */
        if (isCustomField() && !isShared() && parsedInfo.parameters) {
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

	boolean isCustomField() {
        /*
            TM-14497: Merging the fix provided for 4.6.x into 4.5.x is not simple, as it relies on functionality not
            available on this branch (several changes to DataviewSpec and the addition of the FieldSpecProject class).
            Instead of reimplementing all this -- which would mean executing additional queries just to run a simple
            check, we can use the field's name which, in 4.5.x, always start with 'custom'.
         */
		return searchInfo.columnAlias.startsWith('custom')
	}

    String getDomainAlias() {
        return searchInfo.domainAlias
    }

    Boolean isShared() {
        return searchInfo.shared
    }
}