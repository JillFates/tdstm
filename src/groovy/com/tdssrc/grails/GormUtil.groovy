package com.tdssrc.grails;
/**
 * Created by IntelliJ IDEA.
 * User: John
 * Date: Apr 10, 2009
 * Time: 5:29:22 PM
 * To change this template use File | Settings | File Templates.
 */
import org.jsecurity.SecurityUtils
public class GormUtil {
	def static timeZones = [GMT:"GMT-00:00", PST:"GMT-08:00", PDT:"GMT-07:00", MST:"GMT-07:00", MDT:"GMT-06:00", 
							CST:"GMT-06:00", CDT:"GMT-05:00", EST:"GMT-05:00",EDT:"GMT-04:00"]
	def public static allErrorsString = { domain, separator=" : " ->
		def text = ""
		domain?.errors.allErrors.each() { text += separator + it }
		text
	}
	/**
	 * Converts date into GMT
	 * @param date
	 * @return converted Date
	 */
	def public static convertInToGMT = { date, tzId ->
		Date ret
		if(date){
			TimeZone tz
			if(date == 'now'){
				tz  = TimeZone.getDefault()
				Calendar calendar = Calendar.getInstance(tz);
				date = calendar.getTime()
			} else {
				tzId = tzId ? tzId : "EDT"
				def timeZoneId = timeZones[ tzId ]
				tz = TimeZone.getTimeZone( timeZoneId )
			}
			ret = new Date(date.getTime() - tz.getRawOffset());
			// if we are now in DST, back off by the delta. Note that we are
			// checking the GMT date, this is the KEY.
			/*if (tz.inDaylightTime(ret)) {
				Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());
				// check to make sure we have not crossed back into standard time
				// this happens when we are on the cusp of DST (7pm the day before
				// the change for PDT)
				if (tz.inDaylightTime(dstDate))	{
					ret = dstDate;
				}
			}*/
		}
		return ret;
		
	}
	/**
	 * Converts date from GMT to local format
	 * @param date
	 * @return converted Date
	 */
	def public static convertInToUserTZ = { date, tzId ->
		Date ret
		if(date){
			tzId = tzId ? tzId : "EDT"
			def timeZoneId = timeZones[ tzId ]
			TimeZone tz = TimeZone.getTimeZone( timeZoneId );
			ret = new Date(date.getTime() + tz.getRawOffset());
			// if we are now in DST, back off by the delta. Note that we are
			// checking the GMT date, this is the KEY.
			/*if (tz.inDaylightTime(ret)) {
				Date dstDate = new Date(ret.getTime() + tz.getDSTSavings());
				// check to make sure we have not crossed back into standard time
				// this happens when we are on the cusp of DST (7pm the day before
				// the change for PDT)
				if (tz.inDaylightTime(dstDate))	{
					ret = dstDate;
				}
			}*/
		}
		return ret;
	}

}
