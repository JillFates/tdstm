package com.tdsops.etl

/**
 * Structure to define a Column source from a GETL dataSet
 * in an ETL context.<br>
 * It contains:
 * 	<ul>
 * 	    <li>
 * 	        <b>label:</b> it represents column name
 * 	    </li>
 * 	    <li>
 * 	        <b>index:</b> it represents the ordinal position of the dataset column
 * 	    </li>
 * 	</ul>
 */
class Column {

    String label
    Integer index
}
