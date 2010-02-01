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
	
	String dtStr = dt.getClass().getName().toString();
	String dtParam = dt.toString();	
	
	if(dtStr.equals("java.util.Date")){	
		   DateFormat formatter ; 
		  formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		  dtParam = formatter.format(dt);		
	}  
		/* if null or any plain string */
		if (dtParam != "null") {
		
			dtParam = dtParam.trim();
			out << dtParam[5..6]+"/"+dtParam[8..9]+"/"+dtParam[0..3]+" "+dtParam[11..12]+":"+dtParam[14..15]
		
		}
	}
	def truncate = { attrs ->
		String value = attrs['value'];
		if(value){
			def length = value.size()
			if(length > 50){
				out << value.substring(0,50)+".."
			} else {
				out << value
			}
		}
	}
}
