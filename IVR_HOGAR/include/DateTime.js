var DateTime_version = "8.1.4";

/**
* Function to get the current Date
* Replacement to IRD Function Name: "DATE"
* Return value type: DATE. 
* This function returns the current date, in the MM/DD/YYYY format.
*/
function irdDate()
{
	var dat = new Date();
	var endResult = "";
	
	function prefiller(num) {
		return num < 10 ? '0' + num : num;
	}
	
	endResult = prefiller(dat.getMonth()+1)+"/"+prefiller(dat.getDate())+"/"+dat.getFullYear();
	return endResult;
}

/**
* Function to get current date in specified time zone
* Replacement to IRD Function Name: "DateInZone"
* Return value type: DATE. 
* Returns the current date in the specified time zone.
*/
function irdDateInZone(timeZone)
{
	var todayIndex = null;
	
	if ( typeof timeZone != 'undefined') {

		todayIndex = _genesys.session.dateInZone(timeZone);
	}
	return todayIndex;
	
}

/**
* Function to get value of day
* Replacement to IRD Function Name: "DAY"
* Return value type: DAY. 
* This function returns a numerical value related to the day of the week 
* (0-Sunday, 1-Monday, 2-Tuesday, 3-Wednesday, 4-Thursday, 5-Friday, or 6-Saturday).
*/
function irdDay()
{
	var dat = new Date();
	var nDay = -1;
	
	nDay = dat.getDay();
	return nDay;
}

/**
* Function to return UTC in seconds
* Replacement to IRD Function Name: "GetUTC"
* Return value type: INTEGER. 
* This function returns UTC in seconds - the number of seconds elapsed 
* since midnight (00:00:00), January 1, 1970, coordinated universal time, according to the system clock.
*/
function irdGetUTC()
{
	var nDat = Date.UTC(1970,1,1);
	
	return nDat;
}

/**
* Function to get Special Day
* Replacement to IRD Function Name: "IsSpecialDay"
* Return value type: INTEGER. 
* The returned result is interpreted as a Boolean value, 
* where 0 indicates false and 1 indicates true. 
* A non-empty value for the Stat Day parameter of this function is optional. 
* If it is specified, Router inquires from Configuration Server whether 
* the specified Statistical Day is configured for the specified Statistical Table and 
* whether the current date meets the definition of the Statistical Day.
*/ 
function irdIsSpecialDay(statTable, statDay)
{
	var endResult = 0;
	
	if(typeof statTable != 'undefined' && typeof statDay != 'undefined'){
		endResult = _genesys.session.isSpecialDay(statTable,statDay);
	}
	
	return endResult;
}

/**
* Function to get Special Day
* Replacement to IRD Function Name: "IsSpecialDayEx"
* Return value type: INTEGER. 
* The returned result is interpreted as a Boolean value, 
* where 0 indicates false and 1 indicates true. 
* A non-empty value for the Stat Day parameter of this function is optional. 
* If it is specified, Router inquires from Configuration Server 
* whether the specified Statistical Day is configured for the specified Statistical Table and 
* whether the current date (and if UseTime is true then current time too) 
* meets the definition of the Statistical Day. If TimeZone is not empty 
* then current date and time are taken in this TimeZone. 
*/
function irdIsSpecialDayEx(statTable, statDay,timeZone,useTime)
{
	var endResult = 0;
	
	if(typeof statTable != 'undefined' && typeof statDay != 'undefined'){
		endResult = _genesys.session.isSpecialDay(statTable,statDay,timeZone,useTime);
	}
	
	return endResult;
	
}

/**
* Function to return Time
* Replacement to IRD Function Name: "Time"
* Return value type: TIME. 
* This function returns the current 24-hour clock time, in the hh:mm format.
*/
function irdTime()
{
	var ctdate = new Date();
	var endResult = "";
	
	var hrs = ctdate.getHours();
	var mins = ctdate.getMinutes();
	
	if(ctdate.getHours()<10){
		hrs = "0" + hrs;
	}
	if(ctdate.getMinutes()<10){
		mins = "0" + mins;
	}
	endResult = (hrs+":"+mins);
	return endResult;
}

/**
 * Function to return Time Difference
 * Replacement to IRD Function Name: "TimeDifference"
 * Return value type: INTEGER. 
 * The returned value is the result of subtracting First Time from Second Time. 
 * First Time and Second Time are thought of as moments specified in milliseconds. 
 * If First Time is greater than Second Time, it is presumed that Second Time occurs on the day following First Time, 
 * therefore 24 hours (86,400,000 milliseconds) are added to Second Time before the subtraction.
 * @param firstTime
 * @param secondTime
 * @return
 */
function irdTimeDifference(firstTime,secondTime)
{
	var endResult = 0;
	
	if(firstTime!= null && secondTime!=null){
		
		endResult = secondTime - firstTime;  
		if(firstTime>secondTime){
			endResult = endResult + 86400000;
		}
	}
	
	return endResult;
}

/**
 * Function to return Time Stamp
 * Replacement to IRD Function Name: "TimeStamp"
 * Return value type: INTEGER. 
 * The result of this function is the current time in milliseconds, measured from midnight the same day.
 * @return
 */
function irdTimeStamp()
{
	var endResult = 0;

	var date = new Date();
	var seconds = 1000;
	var minutes=seconds*60;
	var hours=minutes*60;

	endResult = ((date.getHours())*hours) + ((date.getMinutes())*minutes) + ((date.getSeconds())*seconds);
	return endResult;
}

/**
 * Function to return UTC Time
 * Replacement to IRD Function Name: "UTCAdd"
 * Return value type: INTEGER. 
 * This function increments provided UTC time with specified numbers of years, months, etc (they can be negative) and returns resulted UTC time
 * @return
 */
function irdUTCAdd(UTC,Yrs,Mns,Dys,Hrs,Mins,Secs)
{
	var endResult = 0;
	
	endResult = UTC + Date.UTC(Yrs,Mns,Dys,Hrs,Mins,Secs);
	
	return endResult;
}

/**
 * Replacement to IRD Function Name: "UTCFromString"
 * Return value type: INTEGER. 
 * This function takes string presenting UTC in format YYYY-MM-DDTHH:MM:SSZ and returns it as number of seconds.
 * @param UTCString
 * @return
 */
function irdUTCFromString(UTCString)
{
	var endResult = 0;
	var sArray = null;
	var str;
	
	str = UTCString.replace(/-/g,",");
	sArray = str.split(",");
	
	endResult = Date.UTC(sArray[0],sArray[1],sArray[2],sArray[3],sArray[4],sArray[5])/1000;
	
	return endResult;
}

/**
 * Replacement to IRD Function Name: "UTCToString"
 * Return value type: STRING.
 * This function takes UTC in seconds and converts it into string in format 'YYYY-MM-DDTHH:MM:SSZ'.
 * @param The UTC in seconds.
 * @return
 */
function irdUTCToString(seconds) {

	var milliseconds = seconds * 1000;
	var d = new Date(milliseconds);

	function prefiller(num) {
		return num < 10 ? '0' + num : num;
	}
	
	return d.getUTCFullYear() + '-' + prefiller(d.getUTCMonth() + 1) + '-'
			+ prefiller(d.getUTCDate()) + 'T' + prefiller(d.getUTCHours())
			+ ':' + prefiller(d.getUTCMinutes()) + ':'
			+ prefiller(d.getUTCSeconds()) + 'Z';
}

/**
 * Checks to see if Today is in the given Date range.
 * 
 * @param fromDate The start range. e.g. '01/31/2000'
 * @param toDate The end range. e.g. '12/31/2099'
 */
function isDateInRange(fromDate, toDate) {
	
    if ( typeof fromDate != 'undefined' && typeof toDate != 'undefined') {
	
    	var todayDate = new Date();
        var startRangeDate = new Date(Date.parse(fromDate));
        var endRangeDate = new Date(Date.parse(toDate));
    	
    	if (todayDate>=startRangeDate && todayDate<endRangeDate) {
	    	// Today is within the Date range.
	    	return true;
	    }
    }
    // Today does not fall within the given Date range.
    return false;
}

/**
 * Checks to see if Today's Date for a specific time zone is in the given Date range.
 * 
 * @param fromDate The start range. e.g. '01/31/2000'
 * @param toDate The end range. e.g. '12/31/2099'
 * @param timeZone The time zone for today's date. e.g. 'PST'
 */
function isDateInTimeZoneRange(fromDate, toDate, timeZone) {
	
	var todayDate = new Date();
	if ( typeof timeZone != 'undefined') {
		// Sets today's date for the given time zone.
		// e.g. '2011-01-30'
		var dateInZone = _genesys.session.dateInZone(timeZone);
		var dateArray = dateInZone.split('-');
		
		if(dateArray.length == 3) {

			// Set the Date according to the time zone.
			todayDate.setFullYear(dateArray[0]);
			todayDate.setMonth(parseInt(dateArray[1], 10)-1);
			todayDate.setDate(dateArray[2]);
		}
	}

	if ( typeof fromDate != 'undefined' & typeof toDate != 'undefined') {

		var startRangeDate = new Date(Date.parse(fromDate));
		var endRangeDate = new Date(Date.parse(toDate));
		
	    if (todayDate>=startRangeDate && todayDate<endRangeDate) {
	    	// Today is within the Date range.
	    	return true;
	    }
	}
    // Today does not fall within the given Date range.
    return false;
}



/**
 * Checks to see if the current time is in the given Time range.
 * 
 * @param fromTime The start range in 24-Hour format. e.g. '02:40'
 * @param toTime The end range in 24-Hour format. e.g. '14:45'
 */
function isTimeInRange(fromTime, toTime) {
	
   if ( typeof fromTime != 'undefined' && typeof toTime != 'undefined') {
    	
    	var rightNow = new Date();
    	var startRangeTime = new Date();
    	var endRangeTime = new Date();
    	  	
    	var fromTimeArray = fromTime.split(':');
    	var toTimeArray = toTime.split(':');

    	if(fromTimeArray.length != 2 || toTimeArray.length != 2) {
    		// The time format is incorrect.
    		return false;
    	}
    	
    	// From Time
        startRangeTime.setHours(parseInt(fromTimeArray[0], 10));
        startRangeTime.setMinutes(parseInt(fromTimeArray[1], 10));
        
        // To Time
        endRangeTime.setHours(parseInt(toTimeArray[0], 10));
        endRangeTime.setMinutes(parseInt(toTimeArray[1], 10));
         	
    	if (rightNow>=startRangeTime && rightNow<endRangeTime) {
	    	// Current time is within the Time range.
	    	return true;
	    }
    }
	
    // Does not fall within the given Time range.
    return false;
}

/**
 * Checks to see if the current Time for a specific time zone is in the given Time range.
 * 
 * @param fromTime The start range in 24-Hour format. e.g. '02:40'
 * @param toTime The end range in 24-Hour format. e.g. '14:45'
 * @param timeZone The time zone. e.g. 'PST'
 */
function isTimeInTimeZoneRange(fromTime, toTime, timeZone) {

	var currentTime = new Date();

	if ( typeof timeZone != 'undefined') {
		// Sets the time for the given time zone.

		var timeInZone = _genesys.session.timeInZone(timeZone); // e.g. '17:00:00'

		var timeArray = timeInZone.split(':');
		if ( timeArray.length >= 3 ) {
			// Set the hours and minutes according to the Time Zone
			currentTime.setHours( parseInt( timeArray[0], 10 ) );
			currentTime.setMinutes( parseInt( timeArray[1], 10 ) );
			currentTime.setSeconds( parseInt( timeArray[2], 10 ) );
		}
		else {
			return false;
		}
	}

	if (typeof fromTime != 'undefined' && typeof toTime != 'undefined') {

		var startRangeTime = new Date();
		var endRangeTime = new Date();

		var fromTimeArray = fromTime.split(':');
		var toTimeArray = toTime.split(':');

		if (fromTimeArray.length < 2 || toTimeArray.length < 2) {
			// Input Time format is incorrect.
			return false;
		}

		// From Time
		startRangeTime.setHours(parseInt(fromTimeArray[0], 10));
		startRangeTime.setMinutes(parseInt(fromTimeArray[1], 10));
		startRangeTime.setSeconds((fromTimeArray.length > 2) ? parseInt(fromTimeArray[2], 10): 0 );

		// To Time
		endRangeTime.setHours(parseInt(toTimeArray[0], 10));
		endRangeTime.setMinutes(parseInt(toTimeArray[1], 10));
		endRangeTime.setSeconds((toTimeArray.length > 2) ? parseInt(toTimeArray[2], 10): 0 );

	    if (currentTime >= startRangeTime && currentTime < endRangeTime) {
			// Current time is within the Time range.
			return true;
		}
	}
    // Date does not fall within the given Time range.
    return false;
}

/**
 * Checks to see if Today is in the given DayOfWeek.
 * 
 * @param dayOfWeek
 *            The index number of the day of the week. i.e. 0=Sunday, 1=Monday,
 *            2=Tuesday, etc.
 */
function isDayOfWeek(dayOfWeek) {
	
    if (typeof dayOfWeek != 'undefined') {
	
    	var today = new Date();

    	if (today.getDay() == dayOfWeek) {
	    	// Today is the specified day of the week.
	    	return true;
	    }
    }
    // Today is not the specified day of week.
    return false;
}

/**
 * Checks to see if Today is in the given DayOfWeek.
 * 
 * @param dayOfWeek
 *            The index number of the day of the week. i.e. 0=Sunday, 1=Monday,
 *            2=Tuesday, etc.
 * @param timeZone The time zone for today's date. e.g. 'PST'
 */
function isDayOfWeekInTimeZone(dayOfWeek, timeZone) {
	
	var todayIndex;
	
	if ( typeof timeZone != 'undefined') {

		todayIndex = _genesys.session.dayInZone(timeZone);
	}
	else {
		// Time zone not specified, get the default.
		var today = new Date();
		todayIndex = today.getDay();
	}
	
    if (typeof dayOfWeek != 'undefined') {
	
    	var today = new Date();

    	if (dayOfWeek == todayIndex) {
	    	// Today is the specified day of the week.
	    	return true;
	    }
    }
    // Today is not the specified day of week.
    return false;
}

/**
 * Checks to see if Today is in the given DayOfWeek range.
 * 
 * @param fromDayOfWeek
 *            The index number of the day of the week. i.e. 0=Sunday, 1=Monday,
 *            2=Tuesday, etc.
 * @param toDayOfWeek
 *            The index number of the day of the week. i.e. 0=Sunday, 1=Monday,
 *            2=Tuesday, etc.
 */
function isDayOfWeekInRange(fromDayOfWeek, toDayOfWeek) {
	
    if ( typeof fromDayOfWeek != 'undefined' && typeof toDayOfWeek != 'undefined') {
	
    	var today = new Date();
    	var todayIndex = today.getDay();

    	if (todayIndex>=fromDayOfWeek && todayIndex<=toDayOfWeek) {
	    	// Today is within the DayOfWeek range.
	    	return true;
	    }
    }
    // Today does not fall under the given DayOfWeek range.
    return false;
}

/**
 * Checks to see if Today for a specific time zone is in the given DayOfWeek range.
 * 
 * @param fromDayOfWeek
 *            The index number of the day of the week. i.e. 0=Sunday, 1=Monday,
 *            2=Tuesday, etc.
 * @param toDayOfWeek
 *            The index number of the day of the week. i.e. 0=Sunday, 1=Monday,
 *            2=Tuesday, etc.
 * @param timeZone The time zone for today's date. e.g. 'PST'
 */
function isDayOfWeekInTimeZoneRange(fromDayOfWeek, toDayOfWeek, timeZone) {
	
	var todayIndex;
	
	if ( typeof timeZone != 'undefined') {

		todayIndex = _genesys.session.dayInZone(timeZone);
	}
	else {
		// Time zone not specified, get the default.
		var today = new Date();
		todayIndex = today.getDay();
	}

	if ( typeof fromDayOfWeek != 'undefined' && typeof toDayOfWeek != 'undefined') {

    	if (todayIndex>=fromDayOfWeek && todayIndex<=toDayOfWeek) {
	    	// Today is within the DayOfWeek range.
	    	return true;
	    }
	}
	// Today does not fall within the given DayOfWeek range.
    return false;
}