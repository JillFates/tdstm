package com.tdsops.etl
/**
 *
 * Collection row elements from an ETL Script.
 * It also could reference to an Asset Entity
 */
class ReferenceResult {

    Map<String, ?> reference
    List<Map<String, ?>> elements = []

}
