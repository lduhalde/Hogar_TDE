var version = "8.1.44";
/*
 *****************************************************************************
 * 							xx-XX.js (Template locale file)
 * ---------------------------------------------------------------------------
 * 	This sample file is built for <The locale Language>.
 * 
 * 	Other locales may need adjustments or further enhancements. 
 * 	The function names must be unique across all JS files.
 * 	(See the corresponding locale JS file. e.g. fr-FR.js/es-MX/etc.)
 * 
 * 	IMPORTANT:
 *  1) The main function name must be of the format:  xxXXPlayBuiltinType
 *  Where 'xxXX' is the locale ID without the hyphen. 
 *  i.e. en-US -> enUSPlayBuiltinType()
 * 
 * 	2) This locale file must be located under its locale folder with the same 
 * 	locale ID name.
 * 	i.e. '../en-US/en-US.js'
 * 
 * 	Note: Do not put any comments as the starting line.
 *****************************************************************************
 */

/**
 * Translates the value of the specified type into an array of
 * vox file URLs.  The array is a list of vox files that when
 * played in succession will represent the value of the specified
 * type in a locale-specific manner.  The following types are
 * supported:
 *
 * <table>
 * <tr><td>"boolean"</td><td>"true" or "false"</td></tr>
 * <tr><td>"date"</td><td>YYYYMMDD<br>Unspecified fields may
 *                                   be replaced by "??" or "????"</td></tr>
 * <tr><td>"digits"</td><td>A string of 0 or more characters [0-9]</td></tr>
 * <tr><td>"currency"</td><td>UUUMM.NN<br>Where UUU is the ISO4217 currency
 *                            code.  The currency code may be omitted, in
 *                            which case, the default currency for the
 *                            current locale will be used.</td></tr>
 * <tr><td>"number"</td><td>Positive or negative, integer or decimal
 *                          number</td></tr>
 * <tr><td>"phone"</td><td>Sequence of digits [0-9], optionally followed
 *                         by an "x" and extension digits [0-9]</td></tr>
 * <tr><td>"time"</td><td>hhmm[aph?]<br>"a" means AM, "p" means PM,
 *                        h means 24 hour, and ? means ambiguous</td></tr>
 * <tr><td>"ordinal"</td><td>A positive integer</td></tr>
 * <tr><td>"alphanumeric"</td><td>A string of digits [0-9] and US-ASCII
 *                                characters [A-Za-z]</td></tr>
 * <tr><td>"dtmf"</td><td>A string of zero or more DTMF characters
 *                        [0-9A-D*#]</td></tr>
 *
 * @param value The value to translate.
 * @param type The type of the value.
 * @param promptUrl The base URL that is prepended to the URLs for all audio files [optional].
 * @param format The output format [optional].
 * @return An array of URL strings.
 */
function xxXXPlayBuiltinType(value, type, promptUrl, format) {
	 //The main function name must be of the format:  xxXXPlayBuiltinType
	 //Where 'xxXX' is the locale ID without the hyphen. 
	 //i.e. en-US -> enUSPlayBuiltinType()
	
	// Define your prompt resource business logic
    var promptsArray = new Array(1);
    return promptsArray;
}
