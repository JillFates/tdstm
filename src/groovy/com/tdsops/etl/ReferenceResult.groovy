package com.tdsops.etl

import com.tds.asset.AssetEntity

/**
 *
 * Collection row elements from an ETL Script.
 * It also could reference to an Asset Entity
 */
class ReferenceResult {

    AssetEntity reference
    List<Map<String, ?>> elements = []

}
