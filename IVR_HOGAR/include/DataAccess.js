var dataaccessFeature_version = "8.1.4";

/**************** START DATA ACCESS FUNCTIONS *******************/

function Cursor(Records, PropertyNames, VariableMappings) {
	// Initialize data to iterate
	if (Records == undefined || !(Records instanceof Array) || Records.length == 0) {
		throw new Error("No data to iterate");
	}
	this.dataArray = Records;
	// this.dataArray = [["19.44","CORP1","CRP1","2009-01-01 00:00:00.0"],
	//		["12.20","CORP2","CRP2","2009-01-01 00:00:00.0"],
	//		["307.65","CORP3","CRP3","2009-01-01 00:00:00.0"],
	//		["3.82","CORP4","CRP4","2009-01-01 00:00:00.0"],
	//		["17.73","CORP5","CRP5","2009-01-01 00:00:00.0"]];
	this.recordCount = this.dataArray.length;
	this.currentRecordNumber = -1;
	this.currentRecord = new Object();

	// Initialize data properties
	if (PropertyNames != undefined) {
		if (!(PropertyNames instanceof Array)) return undefined;
	} else {
		PropertyNames = getJSONPropertyNames(Records);
	}
	this.properties = PropertyNames;
	// this.properties = ["quote_value","company_name","stock_symbol","quote_datetime"];

	// Initialize variable mappings
	this.varMaps = undefined;
	if (VariableMappings != undefined && VariableMappings instanceof Array) {
		this.varMaps = VariableMappings;
	}
	
	// methods
	this.isAvailable = isAvailable;
	this.fetchFirst = fetchFirst;
	this.fetchNext = fetchNext;
	this.fetchPrevious = fetchPrevious;
	this.fetchLast = fetchLast;
	this.fetchRecord = fetchRecord;
	this.hasMoreRecords = hasMoreRecords;
	this.fetchFirst();
}


function getJSONPropertyNames(Records) {
	// fetch first record and identify the property/column names
	var firstRecord = Records[0];
	
	// The iterated data is made of literal items (string, number, ...)
	if (typeof (firstRecord) != 'object') return undefined;

	var propertiesList = new Array();
	var index = 0;
	for (var property in firstRecord) {
	    if (firstRecord.hasOwnProperty(property)) {	// true if it is a direct property
	        propertiesList[index++] = property;
	    }
	}
	return propertiesList;
}

function hasMoreRecords() {
	return this.isAvailable(this.currentRecordNumber+1);
}

function isAvailable(recNumber) {
	if (recNumber >= 0 && recNumber < this.recordCount) {
		return true;
	} else {
		return false;
	}
}

function fetchFirst() {
	return this.fetchRecord (0); 
}

function fetchLast() {
	return this.fetchRecord (this.recordCount-1); 
}

function fetchPrevious() {
	return this.fetchRecord(this.currentRecordNumber - 1);
}

function fetchNext() {
	return this.fetchRecord(this.currentRecordNumber + 1);
}

function getMappedResult(inputData, variablesMapping) {
	var mappedResult = new Object();
	if (variablesMapping == undefined) {
		return undefined;
	}
	var dataObjectStr = "inputData";
	if (inputData instanceof Array && inputData.length > 0) {
		dataObjectStr = "inputData[0]";
	}

	var errorMessage = "";
	for (var mapIndex = 0; mapIndex < variablesMapping.length; mapIndex++) {
		// check if mapping exists
		if (variablesMapping[mapIndex].variable === undefined || variablesMapping[mapIndex].mapping === undefined) {
			// throw error if mapping doesn't exist
			errorMessage += "Mapping incomplete. Variable: " + variablesMapping[mapIndex].variable + ". Mapping: " + variablesMapping[mapIndex].mapping + ". ";
		}
		try {
			// check if mapped value exists
			if (eval(dataObjectStr + "." + variablesMapping[mapIndex].mapping.replace(/\[\]/g,"[0]")) === undefined) {
				// assign undefined value to the variable if mapped value doesn't exist
				eval("mappedResult." + variablesMapping[mapIndex].variable + " = undefined");
			} else {
				eval("mappedResult." + variablesMapping[mapIndex].variable + " = " + dataObjectStr + "." + variablesMapping[mapIndex].mapping.replace(/\[\]/g,"[0]"));
			}
		} catch (e) {
			// throw error if multiple levels are specified when data for sublevels is not available
			errorMessage += "Variable: " + variablesMapping[mapIndex].variable + " cannot be filled. Value not available in result for Mapping: " + variablesMapping[mapIndex].mapping + ". ";
			errorMessage += e;
		}
	}
	// if a variable mapping error is found, return the error message
	if (errorMessage.length > 0) return errorMessage;
	// else, return the JSON mapping object
	return mappedResult;
}


function fetchRecord(recNumber) {
	if (this.isAvailable(recNumber)) {
		this.currentRecordNumber = recNumber;
		if (typeof (this.dataArray[this.currentRecordNumber]) != 'object') {
			// iterating an array of strings or an array of numbers
			this.currentRecord = this.dataArray[this.currentRecordNumber];
		} else {
			for (var propertyIndex = 0; propertyIndex < this.properties.length; propertyIndex++) {
				if (this.dataArray[this.currentRecordNumber] instanceof Array) {
					// iterating an array of arrays
					this.currentRecord[this.properties[propertyIndex]] = this.dataArray[this.currentRecordNumber][propertyIndex];
				} else {
					// iterating an array of JSON objects
					this.currentRecord[this.properties[propertyIndex]] = this.dataArray[this.currentRecordNumber][this.properties[propertyIndex]];
				}				
			}
		}
		return this.currentRecord; 
	} else {
		return undefined;
	}
}

/**************** END DATA ACCESS FUNCTIONS *******************/
