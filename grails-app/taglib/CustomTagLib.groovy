import java.text.DateFormat
import java.text.SimpleDateFormat
class CustomTagLib {
	static namespace = 'tds'

	def convertDate = { attrs ->
	Date dt = attrs['date'];
	
	String dtStr = dt.getClass().getName().toString();
	String dtParam = dt.toString();	
	
	if(dtStr.equals("java.util.Date")){	
		   DateFormat formatter ; 
		  formatter = new SimpleDateFormat("yyyy-MM-dd  kk:mm:ss");
		  dtParam = formatter.format(dt);		
	}  
		/* if null or any plain string */
		if (dtParam != "null") {
		
			dtParam = dtParam.trim();
			out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]
		
		}
	}
	def convertDateTime = { attrs ->
	Date dt = attrs['date'];
	def formate = attrs['formate'];
	
	String dtStr = dt.getClass().getName().toString();
	String dtParam = dt.toString();	
	
	if( dtStr.equals("java.util.Date") || dtStr.equals("java.sql.Timestamp") ){	
		   DateFormat formatter ; 
		   if(formate == "mm/dd" || formate == "12hrs"){
			   formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
		   } else {
			   formatter = new SimpleDateFormat("yyyy-MM-dd HH:MM");
		   }
		  dtParam = formatter.format(dt);		
	}  
		/* if null or any plain string */
		if (dtParam != "null") {
			dtParam = dtParam.trim();
			if(formate == "mm/dd"){
				out << dtParam[5..6]+"/"+dtParam[8..9]+" "+dtParam[11..12]+":"+dtParam[14..15]+" "+dtParam[17..18]
			} else if(formate == "12hrs") {
				out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]+" "+dtParam[11..12]+":"+dtParam[14..15]+" "+dtParam[17..18]
			}else
				out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]+" "+dtParam[11..12]+":"+dtParam[14..15]
			}
	}
	
	def truncate = { attrs ->
		String value = attrs['value'];
		if(value){
			def length = value.size()
			if(length > 50){
				out << '"'+value.substring(0,50)+'.."'
			} else {
				out << '"'+value+'"'
			}
		}
	}
	/*
	 * will return the time + GMT as hh:mm AM/PM formate 
	 */
	def convertToGMT = { attrs ->
		Date dt = attrs['date'];
		def offsetTZ =  new Date().getTimezoneOffset() / 60 ;
		String dtStr = dt.getClass().getName().toString();
		String dtParam = dt.toString();	
		// check to see whether the input date is Date object or not
		if( dtStr.equals("java.util.Date") || dtStr.equals("java.sql.Timestamp") ){
			// convert the date into GMT
			def date = new Date( (Long)(dt.getTime() + (3600000 * offsetTZ)) ) ;
			DateFormat formatter ; 
			// convert the date into required formate
			formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
			dtParam = formatter.format(date);		
		}  
		/* if null or any plain string */
		if (dtParam != "null") {
			dtParam = dtParam.trim();
			out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]+" "+dtParam[11..12]+":"+dtParam[14..15]+" "+dtParam[17..18]
		}
	}
	/*
	 * Convert seconds into HH:MM format
	 * value should be in seconds
	 */
	def formatIntoHHMMSS = { attrs ->
		def value = attrs['value'];
		if( value ){
			def timeFormate 
    	    def hours = (Integer)(value / 3600 )
    	    	timeFormate = hours >= 10 ? hours : '0'+hours
    	    def minutes = (Integer)(( value % 3600 ) / 60 )
    	    	timeFormate += ":"+(minutes >= 10 ? minutes : '0'+minutes)
			
			out << timeFormate
		}
	}
}
