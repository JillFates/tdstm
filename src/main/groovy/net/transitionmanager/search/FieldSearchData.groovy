package net.transitionmanager.search

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
                    fieldSpec: fieldSpec
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

    String getSqlSearchExpression() {
        String expression = parsedInfo.searchExpression
        if (isMixed()) {
            expression = "($mixedSqlExpression) OR ${expression})"
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
}
