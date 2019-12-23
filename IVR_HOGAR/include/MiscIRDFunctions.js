var MiscIRDFunctions_version = "8.1.4";

/**
 *  Function to return Random number
 *  Replacement to IRD Function Name: "Rand"
 *  Return value type: INTEGER. 
 *  This function return random number in bounds from 1 to specified interval.
 * @param interval
 * @return
 */
function irdRand(interval)
{
	return Math.floor(Math.random()*interval);
}

/**
 *  Function to attach a category to the current interaction
 *  Replacement to IRD Block: "Attach Categories"
 * @param categoryname
 * @param categoryid
 * @param relevancy
 */
function attachCategory(categoryname, categoryid, relevancy)
{
	var data = new Object();
	data['CtgId'] = categoryid;
	data['CtgName'] = categoryname;
	if (typeof relevancy != 'undefined') {
		data['Ctg_' + categoryid] = relevancy;
		data['CtgRelevancy'] = relevancy;
	}
	_genesys.ixn.setuData(data, _data.system.context.InteractionID);
}

/**
 * Function to determine if a particular category exists.
 * @param classification
 * @param name
 */
function containsCategoryName(classification, name)
{
	if (!(classification instanceof Array)) return false;

	for (var index = 0; index < classification.length; index++) {
		var item = classification[index];
		if (item['name'] == name) return true;
	}
	return false;
}

/**
 * Function to determine if a particular category exists.
 * @param classification
 * @param id
 */
function containsCategoryId(classification, id)
{
	if (!(classification instanceof Array)) return false;
	for (var index = 0; index < classification.length; index++) {
		var item = classification[index];
		if (item['id'] == id) return true;
	}
	return false;
}

