/**
 * This script replaces the binary representation of the 'legendTwistieState' preference
 * with a more verbose version. It also removes the value for "Dependency Group Status".
 */
databaseChangeLog = {
	
	changeSet(author: "jmorayta", id: "20161006 TM-5382-1") {
		comment("Updates the value for legendTwistieState preferences with ac,de,hb accordingly. ")


		// This closure returns a 4 digits representation for a binary.
		def standarizedBinary = {number ->
		    String bin = Integer.toString(number,2)
		    int size = bin.size()
		    int missingDigits = 4 - size
		    String preffix = ""
		    if(missingDigits > 0){
		        for(i in 1..missingDigits){
		            preffix += "0"
		        }
		    }
		    
		    return preffix + bin
		}

		def newValues = [0:"ac",1:"de",3:"hb"]
		// This 2nd structure is to avoid using map.keySet, which may return keys in the wrong order.
		def idx = [0,1,3]

		for(i in 0..15){
		    String value = standarizedBinary(i)
		    def sqlValueList = []
		    idx.each{
		        if(value.charAt(it) == "1"){
		            sqlValueList << newValues[it]
		        }
		    }
		    String sqlValue = sqlValueList.join(",")

		    String updateStatement = """
		    							UPDATE user_preference SET value = '${sqlValue}' 
		    							WHERE preference_code='legendTwistieState' AND value='${value}'
		    						 """
		    
		    sql(updateStatement)
		}

	}
}
