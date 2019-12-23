package eContact;

import eContact.SSTConector;
import eContact.OracleDBAccess;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Functions {
	public ArrayList<String> 	PromptList 				= new ArrayList<String>(1);	
	
	public KVPairList			RoutingKVPairs			= new KVPairList();
	public KVPairList 			RecordSetKVPs			= new KVPairList();
	
	public boolean				SocketIsOK				= false;
	
	private Parameters			Params					= new Parameters();
	private DBAccess			RoutingDAP				= new DBAccess();
	private OracleDBAccess      DataAccessPointOracle   = new OracleDBAccess();
	
	private Sockets				SocketRequest			= new Sockets();
	private SSTConector			SSTSockets				= new SSTConector();
	
	private boolean				RutEsValido				= false;
	
	private String				RUTClienteIVR			= "";
	private String				DVReal					= "";
	
    private String 				DebugLevel 				= "Detail";
    private String 				DebugFilePath 			= "";
    private String 				DebugFileMaxSize 		= "1000";
	private String 				AsteriskID 				= "";	
	
	public String GetRutCliente()
	{
		return this.RUTClienteIVR;
	}
	
	public String GetDV()
	{
		return this.DVReal;
	}
	
	public String GetParameterValue(String ParameterName)
	{
		return Params.GetValue(ParameterName);
	}
	
	public String ParseXMLData(String InputXMLString, String XMLParentNode, String XMLNodeRequested)
    {
		return ParseXMLData(InputXMLString, XMLParentNode, XMLNodeRequested, false, -1);
    }
	
	public String ParseXMLData(String InputXMLString, String XMLParentNode, String XMLNodeRequested, boolean XMLNodesJoint, int XMLNodePosition)
    {
		boolean	XMLNextValue	= true;
		String 	XMLNodeValue 	= "";
		
    	Debug("[Functions - ParseXMLData] (init)", "Standard");    	
    	
    	Debug("[Functions - ParseXMLData] ParentXMLNode: " + XMLParentNode + ", XMLNode: " + XMLNodeRequested + ".", "Trace");
    	
    	try{
				DocumentBuilderFactory DocBuilderFactory	= DocumentBuilderFactory.newInstance();
				DocumentBuilder DocBuilder 					= DocBuilderFactory.newDocumentBuilder();
				InputSource inStream 						= new InputSource();
				
				inStream.setCharacterStream( new StringReader( InputXMLString ) );
				
				Document oDocument	= DocBuilder.parse(inStream);	
				NodeList nodeList	= oDocument.getElementsByTagName( XMLParentNode );
	    	 
				for(int oIndex=0; oIndex < nodeList.getLength(); oIndex++)
				{
					Node node = nodeList.item( oIndex );
				 
					if (node.getNodeType() == Node.ELEMENT_NODE)
					{
						Element element		= (Element) node;				 
						NodeList nameNode	= element.getElementsByTagName( XMLNodeRequested );
				 
						for( int auxI=0; auxI < nameNode.getLength(); auxI++ )
						{
							if( XMLNextValue == true )
							{	
								if ( nameNode.item(auxI).getNodeType() == Node.ELEMENT_NODE )
								{
									Element XMLElement	= (Element) nameNode.item(auxI);
									
									if( XMLNodesJoint == true )								
									{
										XMLNodeValue += XMLElement.getFirstChild().getNodeValue().trim();
									}
									else if( XMLNodePosition > -1 )
									{
										if( XMLNodePosition == auxI )
										{
											XMLNextValue = false;
											XMLNodeValue = XMLElement.getFirstChild().getNodeValue().trim();
										}
									}
									else
									{
										XMLNodeValue = XMLElement.getFirstChild().getNodeValue().trim();
									}
								}
							}
						}
					}
				}
    	}
    	catch( Exception e ){
    		Debug("[Functions - ParseXMLData] - Problemas al recuperar valor para Nodo XML.", "Standard");
			Debug("[Functions - ParseXMLData] - Error: " + e.getMessage(), "Trace");
			
			XMLNodeValue = "";
	    }
    	
    	Debug("[Functions - ParseXMLData] XMLNode value: " + XMLNodeValue + ".", "Trace");
    	Debug("[Functions - ParseXMLData] (stop)", "Standard");
    	
		return XMLNodeValue;
    }
	
    private String RemoveXMLTags(String XMLInputMessage)
	{
		int 	auxI   				= 0;
		int     auxJ   				= 0;
		String	XMLOutputMessage	= "";
		String 	CurrentXMLTag 		= "";
		
		while( XMLInputMessage.length() > 0 )
		{
			auxI 			= XMLInputMessage.indexOf("<");
			auxJ 			= XMLInputMessage.indexOf(">");
			CurrentXMLTag 	= XMLInputMessage.substring(auxI, auxJ + 1);
			
			if( CurrentXMLTag.toUpperCase().indexOf("XML VERSION") >= 0 )
			{
				XMLInputMessage = XMLInputMessage.substring( CurrentXMLTag.length() );
			}
			else
			{
				if( auxI == 0 )
				{
					XMLOutputMessage =  XMLOutputMessage + XMLInputMessage.substring(auxI, auxJ + 1);
					XMLInputMessage	 =  XMLInputMessage.substring(auxJ + 1);
				}
				
				if ( auxI > 0 )
				{
					XMLOutputMessage =  XMLOutputMessage + XMLInputMessage.substring(0, auxJ + 1);
					XMLInputMessage  =  XMLInputMessage.substring(auxJ + 1);
				}
			}
		}   	  
		return XMLOutputMessage;
	}
	
	/**
	 * 
	 * @param Monto
	 * @param ComposeAll
	 */
    private void GeneraMontoHablado (String Monto, boolean ComposeAll)
    {
    	Debug("[Functions - GeneraMontoHablado] Determinando Prompts para: " + Monto + " (ComposeAll=" + (ComposeAll ? "true" : "false") + ").", "Detail");
        
        Monto = Integer.toString(Integer.parseInt(Monto));

        if( Monto.substring(0, 1).compareTo("-") == 0 )
        {
            PromptList.add(Params.GetValue("PromptMENOS"));
            
            Monto = Monto.substring(1);
        }

        if( Monto.length() > 6 )
        {
            GeneraMontoHablado(Monto.substring(0, Monto.length() - 6), ComposeAll);

            if( ( (Monto.substring(0, Monto.length() - 6)).length() == 1) && (Monto.substring(0, Monto.length() - 6).compareTo("1") == 0) )
            {
                PromptList.add(Params.GetValue("PromptMILLON"));
            }
            else
            {
                PromptList.add(Params.GetValue("PromptMILLONES"));
            }

            GeneraMontoHablado(Monto.substring(Monto.length() - 6, Monto.length()), ComposeAll);
        }
        else if( Monto.length() > 3 )
        {      	
        	if( Monto.substring(0, Monto.length() - 3).compareTo("1") != 0 )
        	{
        		GeneraMontoHablado(Monto.substring(0, Monto.length() - 3), ComposeAll);        	
        	}
        	
            if( Monto.substring(0, Monto.length() - 3).compareTo("000") != 0 )
            {
                PromptList.add(Params.GetValue("PromptMIL"));
            }                   
            
           	GeneraMontoHablado(Monto.substring(Monto.length() - 3, Monto.length()), ComposeAll);         
        }
        else if( Monto.length() == 3 )
        {
            switch( Integer.parseInt(Monto.substring(0, 1)) )
            {
                case 1:
                    if( Monto.compareTo("100") == 0 )
                    {
                        PromptList.add(Params.GetValue("PromptCIEN"));
                    }
                    else
                    {
                        PromptList.add(Params.GetValue("PromptCIENTO"));
                    }
                    break;
                case 2: PromptList.add(Params.GetValue("PromptDOSCIENTOS"));    break;
                case 3: PromptList.add(Params.GetValue("PromptTRESCIENTOS"));   break;
                case 4: PromptList.add(Params.GetValue("PromptCUATROCIENTOS")); break;
                case 5: PromptList.add(Params.GetValue("PromptQUINIENTOS"));    break;
                case 6: PromptList.add(Params.GetValue("PromptSEISCIENTOS"));   break;
                case 7: PromptList.add(Params.GetValue("PromptSETECIENTOS"));   break;
                case 8: PromptList.add(Params.GetValue("PromptOCHOCIENTOS"));   break;
                case 9: PromptList.add(Params.GetValue("PromptNOVECIENTOS"));   break;
            }

            if( Monto.compareTo("100") != 0 ) 
            	GeneraMontoHablado(Monto.substring(1, 3), ComposeAll);
        }
        else if( Monto.length() == 2 )
        {
            if( ComposeAll )
            {
                switch( Integer.parseInt(Monto.substring(0, 1)) )
                {
                    case 1:
                        switch( Integer.parseInt(Monto.substring(1, 2)) )
                        {
                            case 0:  PromptList.add(Params.GetValue("PromptDIEZ"));    break;
                            case 1:  PromptList.add(Params.GetValue("PromptONCE"));    break;
                            case 2:  PromptList.add(Params.GetValue("PromptDOCE"));    break;
                            case 3:  PromptList.add(Params.GetValue("PromptTRECE"));   break;
                            case 4:  PromptList.add(Params.GetValue("PromptCATORCE")); break;
                            case 5:  PromptList.add(Params.GetValue("PromptQUINCE"));  break;
                            default: PromptList.add(Params.GetValue("PromptDIECI"));   break;
                        }
                        break;
                    case 2:
                        if( Monto.substring(1, 2).compareTo("0") == 0 )
                        {
                            PromptList.add(Params.GetValue("PromptVEINTE"));
                        }
                        else
                        {
                            PromptList.add(Params.GetValue("PromptVEINTI"));
                        }
                        break;
                    case 3: PromptList.add(Params.GetValue("PromptTREINTA"));   break;
                    case 4: PromptList.add(Params.GetValue("PromptCUARENTA"));  break;
                    case 5: PromptList.add(Params.GetValue("PromptCINCUENTA")); break;
                    case 6: PromptList.add(Params.GetValue("PromptSESENTA"));   break;
                    case 7: PromptList.add(Params.GetValue("PromptSETENTA"));   break;
                    case 8: PromptList.add(Params.GetValue("PromptOCHENTA"));   break;
                    case 9: PromptList.add(Params.GetValue("PromptNOVENTA"));   break;
                }

                if( (Monto.substring(0, 1).compareTo("1") != 0) && (Monto.substring(0, 1).compareTo("2") != 0) && (Monto.substring(1, 2).compareTo("0") != 0) )
                {
                    PromptList.add(Params.GetValue("PromptY"));
                }

                if( (Monto.substring(0, 1).compareTo("1") != 0) || (Integer.parseInt(Monto.substring(1, 2)) > 5) )
                {
                    GeneraMontoHablado(Monto.substring(Monto.length() - 1, Monto.length()), ComposeAll);
                }
            }
            else
            {
                switch( Integer.parseInt(Monto) )
                {
                    case  1: PromptList.add(Params.GetValue("PromptUN"));               break;
                    case  2: PromptList.add(Params.GetValue("PromptDOS"));              break;
                    case  3: PromptList.add(Params.GetValue("PromptTRES"));             break;
                    case  4: PromptList.add(Params.GetValue("PromptCUATRO"));           break;
                    case  5: PromptList.add(Params.GetValue("PromptCINCO"));            break;
                    case  6: PromptList.add(Params.GetValue("PromptSEIS"));             break;
                    case  7: PromptList.add(Params.GetValue("PromptSIETE"));            break;
                    case  8: PromptList.add(Params.GetValue("PromptOCHO"));             break;
                    case  9: PromptList.add(Params.GetValue("PromptNUEVE"));            break;
                    case 10: PromptList.add(Params.GetValue("PromptDIEZ"));             break;
                    case 11: PromptList.add(Params.GetValue("PromptONCE"));             break;
                    case 12: PromptList.add(Params.GetValue("PromptDOCE"));             break;
                    case 13: PromptList.add(Params.GetValue("PromptTRECE"));            break;
                    case 14: PromptList.add(Params.GetValue("PromptCATORCE"));          break;
                    case 15: PromptList.add(Params.GetValue("PromptQUINCE"));           break;
                    case 16: PromptList.add(Params.GetValue("PromptDIECISEIS"));        break;
                    case 17: PromptList.add(Params.GetValue("PromptDIECISIETE"));       break;
                    case 18: PromptList.add(Params.GetValue("PromptDIECIOCHO"));        break;
                    case 19: PromptList.add(Params.GetValue("PromptDIECINUEVE"));       break;
                    case 20: PromptList.add(Params.GetValue("PromptVEINTE"));           break;
                    case 21: PromptList.add(Params.GetValue("PromptVEINTIUN"));         break;
                    case 22: PromptList.add(Params.GetValue("PromptVEINTIDOS"));        break;
                    case 23: PromptList.add(Params.GetValue("PromptVEINTITRES"));       break;
                    case 24: PromptList.add(Params.GetValue("PromptVEINTICUATRO"));     break;
                    case 25: PromptList.add(Params.GetValue("PromptVEINTICINCO"));      break;
                    case 26: PromptList.add(Params.GetValue("PromptVEINTISEIS"));       break;
                    case 27: PromptList.add(Params.GetValue("PromptVEINTISIETE"));      break;
                    case 28: PromptList.add(Params.GetValue("PromptVEINTIOCHO"));       break;
                    case 29: PromptList.add(Params.GetValue("PromptVEINTINUEVE"));      break;
                    case 30: PromptList.add(Params.GetValue("PromptTREINTA"));          break;
                    case 31: PromptList.add(Params.GetValue("PromptTREINTAYUN"));       break;
                    case 32: PromptList.add(Params.GetValue("PromptTREINTAYDOS"));      break;
                    case 33: PromptList.add(Params.GetValue("PromptTREINTAYTRES"));     break;
                    case 34: PromptList.add(Params.GetValue("PromptTREINTAYCUATRO"));   break;
                    case 35: PromptList.add(Params.GetValue("PromptTREINTAYCINCO"));    break;
                    case 36: PromptList.add(Params.GetValue("PromptTREINTAYSEIS"));     break;
                    case 37: PromptList.add(Params.GetValue("PromptTREINTAYSIETE"));    break;
                    case 38: PromptList.add(Params.GetValue("PromptTREINTAYOCHO"));     break;
                    case 39: PromptList.add(Params.GetValue("PromptTREINTAYNUEVE"));    break;
                    case 40: PromptList.add(Params.GetValue("PromptCUARENTA"));         break;
                    case 41: PromptList.add(Params.GetValue("PromptCUARENTAYUN"));      break;
                    case 42: PromptList.add(Params.GetValue("PromptCUARENTAYDOS"));     break;
                    case 43: PromptList.add(Params.GetValue("PromptCUARENTAYTRES"));    break;
                    case 44: PromptList.add(Params.GetValue("PromptCUARENTAYCUATRO"));  break;
                    case 45: PromptList.add(Params.GetValue("PromptCUARENTAYCINCO"));   break;
                    case 46: PromptList.add(Params.GetValue("PromptCUARENTAYSEIS"));    break;
                    case 47: PromptList.add(Params.GetValue("PromptCUARENTAYSIETE"));   break;
                    case 48: PromptList.add(Params.GetValue("PromptCUARENTAYOCHO"));    break;
                    case 49: PromptList.add(Params.GetValue("PromptCUARENTAYNUEVE"));   break;
                    case 50: PromptList.add(Params.GetValue("PromptCINCUENTA"));        break;
                    case 51: PromptList.add(Params.GetValue("PromptCINCUENTAYUN"));     break;
                    case 52: PromptList.add(Params.GetValue("PromptCINCUENTAYDOS"));    break;
                    case 53: PromptList.add(Params.GetValue("PromptCINCUENTAYTRES"));   break;
                    case 54: PromptList.add(Params.GetValue("PromptCINCUENTAYCUATRO")); break;
                    case 55: PromptList.add(Params.GetValue("PromptCINCUENTAYCINCO"));  break;
                    case 56: PromptList.add(Params.GetValue("PromptCINCUENTAYSEIS"));   break;
                    case 57: PromptList.add(Params.GetValue("PromptCINCUENTAYSIETE"));  break;
                    case 58: PromptList.add(Params.GetValue("PromptCINCUENTAYOCHO"));   break;
                    case 59: PromptList.add(Params.GetValue("PromptCINCUENTAYNUEVE"));  break;
                    case 60: PromptList.add(Params.GetValue("PromptSESENTA"));          break;
                    case 61: PromptList.add(Params.GetValue("PromptSESENTAYUN"));       break;
                    case 62: PromptList.add(Params.GetValue("PromptSESENTAYDOS"));      break;
                    case 63: PromptList.add(Params.GetValue("PromptSESENTAYTRES"));     break;
                    case 64: PromptList.add(Params.GetValue("PromptSESENTAYCUATRO"));   break;
                    case 65: PromptList.add(Params.GetValue("PromptSESENTAYCINCO"));    break;
                    case 66: PromptList.add(Params.GetValue("PromptSESENTAYSEIS"));     break;
                    case 67: PromptList.add(Params.GetValue("PromptSESENTAYSIETE"));    break;
                    case 68: PromptList.add(Params.GetValue("PromptSESENTAYOCHO"));     break;
                    case 69: PromptList.add(Params.GetValue("PromptSESENTAYNUEVE"));    break;
                    case 70: PromptList.add(Params.GetValue("PromptSETENTA"));          break;
                    case 71: PromptList.add(Params.GetValue("PromptSETENTAYUN"));       break;
                    case 72: PromptList.add(Params.GetValue("PromptSETENTAYDOS"));      break;
                    case 73: PromptList.add(Params.GetValue("PromptSETENTAYTRES"));     break;
                    case 74: PromptList.add(Params.GetValue("PromptSETENTAYCUATRO"));   break;
                    case 75: PromptList.add(Params.GetValue("PromptSETENTAYCINCO"));    break;
                    case 76: PromptList.add(Params.GetValue("PromptSETENTAYSEIS"));     break;
                    case 77: PromptList.add(Params.GetValue("PromptSETENTAYSIETE"));    break;
                    case 78: PromptList.add(Params.GetValue("PromptSETENTAYOCHO"));     break;
                    case 79: PromptList.add(Params.GetValue("PromptSETENTAYNUEVE"));    break;
                    case 80: PromptList.add(Params.GetValue("PromptOCHENTA"));          break;
                    case 81: PromptList.add(Params.GetValue("PromptOCHENTAYUN"));       break;
                    case 82: PromptList.add(Params.GetValue("PromptOCHENTAYDOS"));      break;
                    case 83: PromptList.add(Params.GetValue("PromptOCHENTAYTRES"));     break;
                    case 84: PromptList.add(Params.GetValue("PromptOCHENTAYCUATRO"));   break;
                    case 85: PromptList.add(Params.GetValue("PromptOCHENTAYCINCO"));    break;
                    case 86: PromptList.add(Params.GetValue("PromptOCHENTAYSEIS"));     break;
                    case 87: PromptList.add(Params.GetValue("PromptOCHENTAYSIETE"));    break;
                    case 88: PromptList.add(Params.GetValue("PromptOCHENTAYOCHO"));     break;
                    case 89: PromptList.add(Params.GetValue("PromptOCHENTAYNUEVE"));    break;
                    case 90: PromptList.add(Params.GetValue("PromptNOVENTA"));          break;
                    case 91: PromptList.add(Params.GetValue("PromptNOVENTAYUN"));       break;
                    case 92: PromptList.add(Params.GetValue("PromptNOVENTAYDOS"));      break;
                    case 93: PromptList.add(Params.GetValue("PromptNOVENTAYTRES"));     break;
                    case 94: PromptList.add(Params.GetValue("PromptNOVENTAYCUATRO"));   break;
                    case 95: PromptList.add(Params.GetValue("PromptNOVENTAYCINCO"));    break;
                    case 96: PromptList.add(Params.GetValue("PromptNOVENTAYSEIS"));     break;
                    case 97: PromptList.add(Params.GetValue("PromptNOVENTAYSIETE"));    break;
                    case 98: PromptList.add(Params.GetValue("PromptNOVENTAYOCHO"));     break;
                    case 99: PromptList.add(Params.GetValue("PromptNOVENTAYNUEVE"));    break;
                }
            }
        }
        else
        {
            switch( Integer.parseInt(Monto.substring(0, 1)) )
            {
                case 1: PromptList.add(Params.GetValue("PromptUN"));     break;
                case 2: PromptList.add(Params.GetValue("PromptDOS"));    break;
                case 3: PromptList.add(Params.GetValue("PromptTRES"));   break;
                case 4: PromptList.add(Params.GetValue("PromptCUATRO")); break;
                case 5: PromptList.add(Params.GetValue("PromptCINCO"));  break;
                case 6: PromptList.add(Params.GetValue("PromptSEIS"));   break;
                case 7: PromptList.add(Params.GetValue("PromptSIETE"));  break;
                case 8: PromptList.add(Params.GetValue("PromptOCHO"));   break;
                case 9: PromptList.add(Params.GetValue("PromptNUEVE"));  break;
            }
        }        
        return;
    }
	
    /**
     * 
     * @param Monto
     * @param ComposeAll
     */
    public void MontoHablado (String Monto, boolean ComposeAll)
    {
        PromptList.clear();

        Monto = Monto.replaceAll(" ", "");        
        
        Monto = Integer.toString(Integer.parseInt(Monto));

    	Debug("[Functions - MontoHablado] (init)", "Standard");
    	
        Debug("[Functions - MontoHablado] A determinar Prompts para decir el Monto: " + Monto + " (ComposeAll=" + (ComposeAll ? "true" : "false") + ").", "Trace");
        
        if( Monto.compareTo("0") == 0 )
        {
            PromptList.add(Params.GetValue("PromptCERO"));
        }
        else if ( Monto.compareTo("1") == 0 )
        {
        	PromptList.add(Params.GetValue("PromptUN"));      	
        }
        else
        {
        	GeneraMontoHablado(Monto, ComposeAll);
        }
        
        for(int i=0; i<PromptList.size(); i++)
        {
            Debug("[Functions - MontoHablado] Prompt " + (i + 1) + ": " + ((String) PromptList.get(i)) + ".", "Trace");     
        }
        
        Debug("[Functions - FechaHablada] (stop)", "Standard");
        
        return;
    }

    /**
     * 
     * @param Monto
     */
    public void MontoHablado (String Monto)
    {   	
        MontoHablado(Monto, false);
    }    
    
    /**
     * 
     * @param Dia
     * @param Mes
     * @param Anno
     * @param ComposeAll
     */
    public void FechaHablada (String Dia, String Mes, String Anno, boolean ComposeAll)
    {
        PromptList.clear();
        
        Debug("[Functions - FechaHablada] (init)", "Standard");
        
        Debug("[Functions - FechaHablada] A determinar prompts para decir la Fecha: " + Dia + "/" + Mes + "/" + Anno + ".", "Trace");
               
        if ( Dia.compareTo("1") == 0 || Dia.compareTo("01") == 0 )
        {
        	PromptList.add(Params.GetValue("PromptPRIMERO"));	
        }
        else
        {
        	GeneraMontoHablado(Dia, ComposeAll);
        }
                
        PromptList.add(Params.GetValue("PromptDE"));
        
        switch( Integer.parseInt(Mes) )
        {
            case  1: PromptList.add(Params.GetValue("PromptENERO"));      break;
            case  2: PromptList.add(Params.GetValue("PromptFEBRERO"));    break;
            case  3: PromptList.add(Params.GetValue("PromptMARZO"));      break;
            case  4: PromptList.add(Params.GetValue("PromptABRIL"));      break;
            case  5: PromptList.add(Params.GetValue("PromptMAYO"));       break;
            case  6: PromptList.add(Params.GetValue("PromptJUNIO"));      break;
            case  7: PromptList.add(Params.GetValue("PromptJULIO"));      break;
            case  8: PromptList.add(Params.GetValue("PromptAGOSTO"));     break;
            case  9: PromptList.add(Params.GetValue("PromptSEPTIEMBRE")); break;
            case 10: PromptList.add(Params.GetValue("PromptOCTUBRE"));    break;
            case 11: PromptList.add(Params.GetValue("PromptNOVIEMBRE"));  break;
            case 12: PromptList.add(Params.GetValue("PromptDICIEMBRE"));  break;
        }

        PromptList.add(Params.GetValue("PromptDEL"));

        GeneraMontoHablado(Anno, ComposeAll);
        
        for(int i=0; i<PromptList.size(); i++)
        {
            Debug("[Functions - MontoHablado] Prompt " + (i + 1) + ": " + ((String) PromptList.get(i)) + ".", "Trace");    
        }        
        
        Debug("[Functions - FechaHablada] (stop)", "Standard");
        
        return;
    }
    
    /**
     * 
     * @param Dia
     * @param Mes
     * @param Anno
     */
    public void FechaHablada (String Dia, String Mes, String Anno)
    {   	
    	FechaHablada(Dia, Mes, Anno, false);
    }
    
    /**
     * 
     * @param Numero
     */
    public void DictaNumero (String Numero)
    {
    	String	oNumero = "";
    	
        PromptList.clear();
        
        Debug("[Functions - DictaNumero] (init)", "Standard");             

        Numero = Numero.replaceAll(" ", "");
        
        Debug("[Functions - DictaNumero] A determinar prompts para dictar el N�mero: " + Numero + ".", "Trace");

        for( int auxI=0; auxI < Numero.length(); auxI++ )
        {
        	oNumero = Numero.substring(auxI, auxI + 1);

        	if ( oNumero.compareTo("0") == 0 )
        	{
        		PromptList.add(Params.GetValue("PromptCERO"));
        	}
        	else if ( oNumero.compareTo("1") == 0 )
        	{
        		PromptList.add(Params.GetValue("PromptUNO"));
        	}
        	else
        	{
        		GeneraMontoHablado(oNumero, false);
        	}
        }

        for(int i=0; i<PromptList.size(); i++)
        {
            Debug("[Functions - MontoHablado] Prompt " + (i + 1) + ": " + ((String) PromptList.get(i)) + ".", "Trace");                                 
        }    
        
        Debug("[Functions - DictaNumero] (stop)", "Standard");
        
        return;
    }
    
    /**
     * 
     * @param RUTCliente
     * @param DV
     * @param EquivalenciaK
     * @return
     */
    public boolean ValidaRUT(String RUTCliente, String DV, String EquivalenciaK)
    {
    	int		auxI	= 0;
    	int 	auxJ	= 2;
    	int 	auxDV	= 0;
    	int 	auxSuma = 0;
    	boolean	auxB	= false;
    	
    	RUTClienteIVR = "";
    	
    	Debug("[Functions - ValidaRUT] (init)", "Standard");    	
    	
    	Debug("[Functions - ValidaRUT] Validando RUT: " + RUTCliente + "-" + DV + " (EquivalenciaK=" + EquivalenciaK + ").", "Trace");
    	
        for( auxI = RUTCliente.length() - 1 ; auxI >= 0 ; auxI-- )
        {
            auxSuma += Integer.parseInt(RUTCliente.substring(auxI, auxI + 1)) * auxJ;

            if( ++auxJ > 7 )
                auxJ = 2;
        }    	
        auxDV = 11 - ( auxSuma % 11 );

        if( auxDV < 10 )
        {
            if( Integer.parseInt(DV) == auxDV )
            {
            	DVReal = String.valueOf(auxDV);
            	auxB = true;
            }
        }
        else if( auxDV == 10 )
        {
            if( Integer.parseInt(DV) == Integer.parseInt(EquivalenciaK) )
            {
            	DVReal = "K";
            	auxB = true;                
            }
        }
        else
        {
            if( Integer.parseInt(DV) == 0 )
            {
            	DVReal = "0";
            	auxB = true;
            }
        }

        if ( auxB )
        {
        	RutEsValido = true;        	
        	RUTClienteIVR = RUTCliente;
        }
        else
        {
        	RutEsValido = false;        	
        	RUTClienteIVR = "";
        }
 
        Debug("[Functions - ValidaRUT]     RUT: " + RUTCliente + "-" + DV + " es " + (auxB ? "valido" : "invalido") + ".", "Trace");
        
        Debug("[Functions - ValidaRUT] (stop)", "Standard");
        
        return RutEsValido;        
    }
        
    /**
     * 
     * @param DNIS
     * @param VectorCodigoServicio
     * @return
     */
    public boolean RecuperaParametrosServicio(String DNIS, int VectorCodigoServicio)
    {
    	boolean 	Retorno        		= false;
    	int			KVPCount       		= 0;
    	int 		ConnectionTimeOut	= 0; 
    	int 		QueryTimeOut		= 0;
    	    	
    	Debug("[Functions - RecuperaParametrosServicio] (init)", "Standard");
    	Debug("[Functions - RecuperaParametrosServicio] DNIS: " + DNIS + ", VectorCodigoServicio: "+ String.valueOf(VectorCodigoServicio), "Trace");
    	
    	RoutingKVPairs.clear();
    	
    	if( Params.GetValue("RoutingConnectionTimeout").compareTo("") != 0 )
    		ConnectionTimeOut = Integer.parseInt(Params.GetValue("RoutingConnectionTimeout"));
    	else
    		ConnectionTimeOut = 10;
    	
    	if( Params.GetValue("RoutingQueryTimeOut").compareTo("") != 0 )
    		QueryTimeOut = Integer.parseInt(Params.GetValue("RoutingQueryTimeOut"));
    	else
    		QueryTimeOut = 10;
    	
    	if( RoutingDAP.OpenDataBase(Params.GetValue("PrimaryRoutingURL"), 
    								Params.GetValue("GenesysRoutingClassDriver"), 
    								Params.GetValue("PrimaryRoutingUserName"), 
    								Params.GetValue("PrimaryRoutingPassword"),
    								ConnectionTimeOut
    								)
    	)
    	{
    		Debug("[Functions - RecuperaParametrosServicio] - Coneccion exitosa con la Base de Datos Genesys Routing [" + Params.GetValue("PrimaryRoutingURL") + "]", "Standard");    	 
    		
    		RoutingKVPairs.clear();
    		RoutingKVPairs = RoutingDAP.ObtieneParametrosServicio(DNIS, VectorCodigoServicio, QueryTimeOut);
    		
    		if( RoutingKVPairs != null )
    		{   
    			KVPCount = RoutingKVPairs.count();
    			
    			Debug("[Functions - RecuperaParametrosServicio] - Numero Total de KVPairs: " + String.valueOf(KVPCount), "Standard");
    			
	    		if( RoutingKVPairs.count() > 0 )
	    		{
	    			for ( int oIndex = 0; oIndex < RoutingKVPairs.count(); oIndex++ ) 
	    			{
	    				Debug("[Functions - RecuperaParametrosServicio] - KeyName: " + RoutingKVPairs.getKey(oIndex) +", KeyValue: " + RoutingKVPairs.getValue(oIndex), "Standard");	
	    			}
	    		}	    		
	    		Retorno = true;
	    		RoutingDAP.CloseDataBase();
    		}
    		else
    		{
    			if( RoutingDAP.GetErrorMessage() != "" )
    			{
    				Debug("[Functions - RecuperaParametrosServicio] - Problemas al ejecutar procedimiento almacenado spRecuperaParametrosServicio", "Standard");
    				Debug("[Functions - RecuperaParametrosServicio] - Error: " + RoutingDAP.GetErrorMessage(), "Trace");
    				
    				Retorno = false;
    				RoutingDAP.CloseDataBase();
    			}
    		}    		    		
    	}
    	else 
    	{
    		Retorno = false;
    		Debug("[Functions - RecuperaParametrosServicio] - Problemas al conectar con la Base de Datos primaria Genesys Routing", "Standard");
    		Debug("[Functions - RecuperaParametrosServicio] - Error: " + RoutingDAP.GetErrorMessage(), "Trace");
    		
    		if( Params.GetValue("BackupRoutingURL").compareTo("") != 0 )
    		{    			
    			Debug("[Functions - RecuperaParametrosServicio] - Intentando conectar con la Base de Datos backup Genesys Routing [" + Params.GetValue("BackupRoutingURL") + "]", "Standard");
    		
    			RoutingKVPairs.clear();
    			if( RoutingDAP.OpenDataBase(Params.GetValue("BackupRoutingURL"), 
    										Params.GetValue("GenesysRoutingClassDriver"), 
    										Params.GetValue("BackupRoutingUserName"), 
    										Params.GetValue("BackupRoutingPassword"), 
    										ConnectionTimeOut
    										) 
    			)
    			{	
    				Debug("[Functions - RecuperaParametrosServicio] - Coneccion exitosa con la Base de Datos Genesys Routing [" + Params.GetValue("BackupRoutingURL") + "]", "Standard");    	 
        		
    				RoutingKVPairs.clear();
    				RoutingKVPairs = RoutingDAP.ObtieneParametrosServicio( DNIS, VectorCodigoServicio, QueryTimeOut );
        		
    				if( RoutingKVPairs != null )
    				{   
    					KVPCount = RoutingKVPairs.count();
        			
    					Debug("[Functions - RecuperaParametrosServicio] - Numero Total de KVPairs: " + String.valueOf(KVPCount), "Standard");
        			
    					if( RoutingKVPairs.count() > 0 )
    					{
    						for ( int oIndex = 0; oIndex < RoutingKVPairs.count(); oIndex++ ) {
    	    					Debug("[Functions - RecuperaParametrosServicio] - KeyName: " + RoutingKVPairs.getKey(oIndex) +", KeyValue: " + RoutingKVPairs.getValue(oIndex), "Standard");	
    						}
    					}	    		
    					Retorno = true;
    					RoutingDAP.CloseDataBase();
    				}
    				else
    				{
    					if( RoutingDAP.GetErrorMessage() != "" )
    					{
    						Debug("[Functions - RecuperaParametrosServicio] - Problemas al ejecutar procedimiento almacenado spRecuperaParametrosServicio", "Standard");
    						Debug("[Functions - RecuperaParametrosServicio] - Error: " + RoutingDAP.GetErrorMessage(), "Trace");
        				
    						Retorno = false;
    						RoutingDAP.CloseDataBase();
    					}
    				}          		
    			}
    			else
    			{
            		Retorno = false;
            		Debug("[Functions - RecuperaParametrosServicio] - Problemas al conectar con la Base de Datos backup Genesys Routing", "Standard");
            		Debug("[Functions - RecuperaParametrosServicio] - Error: " + RoutingDAP.GetErrorMessage(), "Trace");    				
    			}
    		}
        	else
        	{
        		Retorno = false;
        		Debug("[Functions - RecuperaParametrosServicio] - No existe definici�n de Base de Datos backup Genesys Routing", "Standard");
        		Debug("[Functions - RecuperaParametrosServicio] - Error: " + RoutingDAP.GetErrorMessage(), "Trace");
        	}        	    		
    	}
    	
    	Debug("[Functions - RecuperaParametrosServicio] (stop)", "Standard");
    	return Retorno;
    }

    public String SendMessageToSST(String RemoteHost, int RemotePort, int ConnectionTimeOut, int ReadWriteTimeOut, String User, String Password, String Service, String Provider, String UserData, boolean IsPersistent, boolean RemoveXMLTags, String Message2IVR)
    {
    	String Retorno = "";
    		
    	Debug("[Functions - SendMessageToSST] (init)", "Standard");
    	Debug("[Functions - SendMessageToSST] Host: " + RemoteHost + ", Port: "+ String.valueOf(RemotePort) + ", TimeOut: "+ String.valueOf(ConnectionTimeOut) + " (ms)", "Trace");	
    	Debug("[Functions - SendMessageToSST] Message To Send: " + Message2IVR, "Trace");    	
	
    	Debug("[Functions - SendMessageToSST (OpenConnection)] - Intentando coneccion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    	
    	if( SSTSockets.OpenConnection(RemoteHost, RemotePort, ConnectionTimeOut, ReadWriteTimeOut) )
    	{
    		Debug("[Functions - SendMessageToSST (OpenConnection)] - Coneccion exitosa con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");

    		SSTSockets.SendReceiveMessage( AsteriskID, User, Password, Service, Provider, UserData, IsPersistent, Message2IVR );
    		Debug("[Functions - SendMessageToSST (SendReceiveMessage)] - Enviando mensaje formateado: " + SSTSockets.RequestMessage(), "Standard");    		
    	
    		if( SSTSockets.ResultCode() < 0 )
    			Debug("[Functions - SendMessageToSST (SendReceiveMessage)] - Error [" + String.valueOf(SSTSockets.ResultCode()) + ":" + SSTSockets.ResultMessage() + "]", "Standard");

    		
    		Debug("[Functions - SendMessageToSST] - Recibiendo mensaje: " + SSTSockets.ResponseMessage(), "Standard");
    		
    		
    		Debug("[Functions - SendMessageToSST (CloseConnection)] - Cerrando conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");		
    		SSTSockets.CloseConnection();
    		
    		if( SSTSockets.ResultCode() < 0 )
    			Debug("[Functions - SendMessageToSST (CloseConnection)] - Error [" + String.valueOf(SSTSockets.ResultCode()) + ":" + SSTSockets.ResultMessage() + "]", "Standard");

    		
      		if( RemoveXMLTags )
      			Retorno = RemoveXMLTags( SSTSockets.RemoveNoXMLInfo( SSTSockets.ResponseMessage() ) );
      		else
      			Retorno = SSTSockets.RemoveNoXMLInfo( SSTSockets.ResponseMessage() );
    		
    		if( SSTSockets.ResultCode() < 0 )
    			Debug("[Functions - SendMessageToSST (RemoveNoXMLInfo)] - Error [" + String.valueOf(SSTSockets.ResultCode()) + ":" + SSTSockets.ResultMessage() + "]", "Standard");
    	}
    	else
    	{
    		Debug("[Functions - SendMessageToSST (OpenConnection)] - Problemas al realizar conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    		
    		if( SSTSockets.ResultCode() < 0 )
    			Debug("[Functions - SendMessageToSST (OpenConnection)] - Error [" + String.valueOf(SSTSockets.ResultCode()) + ":" + SSTSockets.ResultMessage() + "]", "Standard");    		    		   	
    	}
    	
    	Debug("[Functions - SendMessageToSST] (stop)", "Standard");
    	
    	SSTSockets.CloseConnection();
    	
    	return Retorno;
    }
    
    /**
     * 
     * @param RemoteHost
     * @param RemotePort
     * @param ConnectionTimeOut
     * @param ReadWriteTimeOut
     * @param Message2Send
     * @return
     */
    public String SendReceiveSocketMessage(String RemoteHost, int RemotePort, int ConnectionTimeOut, int ReadWriteTimeOut, String Message2Send)
    {
    	String OutPutMessage = "";
    	
    	Debug("[Functions - SendReceiveSocketMessage] (init)", "Standard");
    	Debug("[Functions - SendReceiveSocketMessage] Host: " + RemoteHost + ", Port: "+ String.valueOf(RemotePort) + ", TimeOut: "+ String.valueOf(ConnectionTimeOut) + " (ms)", "Trace");	
    	Debug("[Functions - SendReceiveSocketMessage] Input Message: " + Message2Send, "Trace");
    	
    	Debug("[Functions - SendReceiveSocketMessage (OpenConnection)] - Intentando coneccion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    	
    	if( SocketRequest.OpenConnection(RemoteHost, RemotePort, ConnectionTimeOut, ReadWriteTimeOut) )
    	{
    		Debug("[Functions - SendReceiveSocketMessage (OpenConnection)] - Coneccion exitosa con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    		Debug("[Functions - SendReceiveSocketMessage (SendReceiveMessage)] - Enviando mensaje formateado: " + Message2Send, "Standard"); 
    		
    		OutPutMessage = SocketRequest.SendReceiveMessage(Message2Send);
    		
    		if( SocketRequest.IsSocketWithError() )
    		{
    			Debug("[Functions - SendReceiveSocketMessage (SendReceiveMessage)] - Problemas al enviar/recibir mensaje '[" + SocketRequest.GetErrorMessage() + "'].", "Standard");
    			
    			SocketIsOK = false;
    			OutPutMessage = "";
    		}
    		else
    		{
    			SocketIsOK = true;
    			Debug("[Functions - SendReceiveSocketMessage] - Se ha recibido el siguiente mensaje: '" + OutPutMessage + "'", "Standard");
    		}
    		
    		Debug("[Functions - SendReceiveSocketMessage (CloseConnection)] - Cerrando conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");		
    		SocketRequest.CloseConnection();
    		
    		if( SocketRequest.IsSocketWithError() )
    			Debug("[Functions - SendReceiveSocketMessage (CloseConnection)] - Problemas al cerrar conexi�n '[" + SocketRequest.GetErrorMessage() + "'].", "Standard");   			
    	}
    	else
    	{
    		Debug("[Functions - SendReceiveSocketMessage (OpenConnection)] - Problemas al realizar conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    		
    		if( SocketRequest.IsSocketWithError() )
    			Debug("[Functions - SendReceiveSocketMessage (OpenConnection)] - " + SocketRequest.GetErrorMessage() + "'.", "Standard");
    		
    		SocketIsOK = false;
    	}
    	
    	Debug("[Functions - SendReceiveSocketMessage] (stop)", "Standard");
    	
    	return OutPutMessage;
    }
    
    /**
     * 
     * @param RemoteHost
     * @param RemotePort
     * @param ConnectionTimeOut
     * @param ReadWriteTimeOut
     * @param Message2Send
     * @return
     */
    public String SendReceiveSocketMessageIO(String RemoteHost, int RemotePort, int ConnectionTimeOut, int ReadWriteTimeOut, String Message2Send)
    {
    	String OutPutMessage = "";
    	
    	Debug("[Functions - SendReceiveSocketMessageIO] (init)", "Standard");
    	Debug("[Functions - SendReceiveSocketMessageIO] Host: " + RemoteHost + ", Port: "+ String.valueOf(RemotePort) + ", TimeOut: "+ String.valueOf(ConnectionTimeOut) + " (ms)", "Trace");	
    	Debug("[Functions - SendReceiveSocketMessageIO] Input Message: " + Message2Send, "Trace");
    	
    	Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - Intentando coneccion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    	
    	if( SocketRequest.OpenConnection(RemoteHost, RemotePort, ConnectionTimeOut, ReadWriteTimeOut) )
    	{
    		Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - Coneccion exitosa con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    		Debug("[Functions - SendReceiveSocketMessageIO (SendReceiveMessage)] - Enviando mensaje formateado: " + Message2Send, "Standard"); 
    		
    		OutPutMessage = SocketRequest.SendReceiveMessageIO(Message2Send);
    		
    		if( SocketRequest.IsSocketWithError() )
    		{
    			Debug("[Functions - SendReceiveSocketMessageIO (SendReceiveMessage)] - Problemas al enviar/recibir mensaje '[" + SocketRequest.GetErrorMessage() + "'].", "Standard");
    			
    			SocketIsOK = false;
    			OutPutMessage = "";
    		}
    		else
    		{
    			SocketIsOK = true;
    			Debug("[Functions - SendReceiveSocketMessageIO] - Se ha recibido el siguiente mensaje: '" + OutPutMessage + "'", "Standard");
    		}
    		
    		Debug("[Functions - SendReceiveSocketMessageIO (CloseConnection)] - Cerrando conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");		
    		SocketRequest.CloseConnection();
    		
    		if( SocketRequest.IsSocketWithError() )
    			Debug("[Functions - SendReceiveSocketMessageIO (CloseConnection)] - Problemas al cerrar conexi�n '[" + SocketRequest.GetErrorMessage() + "'].", "Standard");   			
    	}
    	else
    	{
    		Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - Problemas al realizar conexion con servidor [" + RemoteHost + ":" + String.valueOf(RemotePort) + "]", "Standard");
    		
    		if( SocketRequest.IsSocketWithError() )
    			Debug("[Functions - SendReceiveSocketMessageIO (OpenConnection)] - " + SocketRequest.GetErrorMessage() + "'.", "Standard");
    		
    		SocketIsOK = false;
    	}
    	
    	Debug("[Functions - SendReceiveSocketMessageIO] (stop)", "Standard");
    	
    	return OutPutMessage;
    }    
    
    /**
     * 
     * @param pConnectionURL
     * @param pUserName
     * @param pPassword
     * @param DriverName
     * @param pReturnRecorset
     * @param pSQLQuery
     * @param ConnectionTimeOut
     * @param QueryTimeOut
     * @return
     */
    public boolean ExecuteSQLQuery(String pConnectionURL, String pUserName, String pPassword, String DriverName, boolean pReturnRecorset, String pSQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
    	boolean				Retorno				= false;
    	int					oIndex				= 0;
    	int					oRowsAffected		= 0;
    	String				RecordName			= "";
    	String				RecordValue 		= "";
    	DBAccess			LocalDAP			= new DBAccess();
    	ArrayList<String>	RecordFieldName		= new ArrayList<String>(1);    	
    	ResultSet			OutputRecorset		= null;
    	ResultSetMetaData	eResultSetMetaData	= null;

    	Debug("[Functions - ExecuteSQLQuery] (init)", "Standard");
    	Debug("[Functions - ExecuteSQLQuery] *** ConnectionURL:" + pConnectionURL, "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** DriverName   :" + DriverName, "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** UserName     :" + pUserName, "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** Password     :" + pPassword, "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** Out Recordset:" + String.valueOf(pReturnRecorset), "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** Connection TO:" + String.valueOf(ConnectionTimeOut), "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** Query TimeOut:" + String.valueOf(QueryTimeOut), "Trace");
    	Debug("[Functions - ExecuteSQLQuery] *** SQL Sentence :" + pSQLQuery, "Trace");
    	
    	RecordSetKVPs.clear();
    	
    	if( LocalDAP.OpenDataBase(pConnectionURL, DriverName, pUserName, pPassword, ConnectionTimeOut) )
    	{
    		Debug("[Functions - ExecuteSQLQuery] - Coneccion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");    	 

    		Debug("[Functions - ExecuteSQLQuery] - Ejecutando Query en la Base de Datos '" + pSQLQuery + "'", "Standard");

    		if( pReturnRecorset == true )
    		{
    			OutputRecorset = LocalDAP.ExecuteQuery(pSQLQuery, QueryTimeOut);
    		}
    		else
    		{
    			oRowsAffected 	= LocalDAP.ExecuteUpdate( pSQLQuery, QueryTimeOut);

    			Retorno 		= true;
    			OutputRecorset 	= null;

    			Debug("[Functions - ExecuteSQLQuery] - Rows Affected: " + String.valueOf(oRowsAffected) + "',", "Standard");
    		}

    		if( OutputRecorset != null )
    		{
    			try {
    				eResultSetMetaData = OutputRecorset.getMetaData();

    				for( oIndex = 1; oIndex <= eResultSetMetaData.getColumnCount(); oIndex++ )
    					RecordFieldName.add( eResultSetMetaData.getColumnName( oIndex ) );

    				while( OutputRecorset.next() )
    				{
    					for( oIndex = 0; oIndex < RecordFieldName.size(); oIndex++ )
    					{
    						RecordName	= RecordFieldName.get(oIndex);																
    						RecordValue	= OutputRecorset.getString( RecordName );
	
    						if( RecordName != null )
    						{
    							if( RecordValue != null )
    								RecordSetKVPs.add( RecordName, RecordValue );
    							else
    								RecordSetKVPs.add( RecordName, "" );
    						}
    					}
    				}
    				Retorno = true;
    			} 
    			catch (SQLException e) 
    			{
    				Retorno = false;
    				Debug("[Functions - ExecuteSQLQuery] - Error: " + e.getMessage(), "Standard");
    			}

    			Debug("[Functions - ExecuteSQLQuery] - Resultado exitoso de la Query en la Base de Datos", "Standard");
    			Debug("[Functions - ExecuteSQLQuery] - Numero Total de KVPairs: " + String.valueOf(RecordSetKVPs.count()), "Standard");

    			if( RecordSetKVPs.count() > 0 )
    			{
    				for ( oIndex = 0; oIndex < RecordSetKVPs.count(); oIndex++ ) 
    				{
    					Debug("[Functions - ExecuteSQLQuery] - KeyName: " + RecordSetKVPs.getKey(oIndex) +", KeyValue: " + RecordSetKVPs.getValue(oIndex), "Standard");	
    				}
    			}
    			LocalDAP.CloseDataBase();
    		}
    		else
    		{
    			if( LocalDAP.GetErrorMessage() != "" )
    			{
    				Debug("[Functions - ExecuteSQLQuery] - Problemas al ejecutar query '" + pSQLQuery + "'", "Standard");
    				Debug("[Functions - ExecuteSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
    			}

    			if( pReturnRecorset )
    			{
    				Retorno			= false;
    				RecordSetKVPs	= null;
    			}

    			LocalDAP.CloseDataBase();
    		}
    	}
    	else
    	{
    		Debug("[Functions - ExecuteSQLQuery] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
    		Debug("[Functions - ExecuteSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
    	}
    	Debug("[Functions - ExecuteSQLQuery] (stop)", "Standard");
    
    	return Retorno;
    }    
    
    
    public boolean ExecutePLSQLQuery(String pConnectionURL, String pUserName, String pPassword, boolean pReturnRecorset, String pSQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
    	boolean				Retorno				= false;
    	int					oIndex				= 0;
    	int					oRowsAffected		= 0;
    	String				RecordName			= "";
    	String				RecordValue 		= "";
    	OracleDBAccess		LocalDAP			= new OracleDBAccess();
    	ArrayList<String>	RecordFieldName		= new ArrayList<String>(1);    	
    	ResultSet			OutputRecorset		= null;
    	ResultSetMetaData	eResultSetMetaData	= null;

    	Debug("[Functions - ExecutePLSQLQuery] (init)", "Standard");
    	Debug("[Functions - ExecutePLSQLQuery] *** ConnectionURL:" + pConnectionURL, "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** UserName     :" + pUserName, "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** Password     :" + pPassword, "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** Out Recordset:" + String.valueOf(pReturnRecorset), "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** Connection TO:" + String.valueOf(ConnectionTimeOut), "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** Query TimeOut:" + String.valueOf(QueryTimeOut), "Trace");
    	Debug("[Functions - ExecutePLSQLQuery] *** SQL Sentence :" + pSQLQuery, "Trace");
    	
    	RecordSetKVPs.clear();
    	
    	if( LocalDAP.OpenDataBase(pConnectionURL, pUserName, pPassword, ConnectionTimeOut) )
    	{
    		Debug("[Functions - ExecutePLSQLQuery] - Coneccion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");    	 

    		Debug("[Functions - ExecutePLSQLQuery] - Ejecutando Query en la Base de Datos '" + pSQLQuery + "'", "Standard");

    		if( pReturnRecorset == true )
    		{
    			OutputRecorset = LocalDAP.ExecuteQuery(pSQLQuery, QueryTimeOut);
    		}
    		else
    		{
    			oRowsAffected 	= LocalDAP.ExecuteUpdate( pSQLQuery, QueryTimeOut);

    			Retorno 		= true;
    			OutputRecorset 	= null;

    			Debug("[Functions - ExecutePLSQLQuery] - Rows Affected: " + String.valueOf(oRowsAffected) + "',", "Standard");
    		}

    		if( OutputRecorset != null )
    		{
    			try {
    				eResultSetMetaData = OutputRecorset.getMetaData();

    				for( oIndex = 1; oIndex <= eResultSetMetaData.getColumnCount(); oIndex++ )
    					RecordFieldName.add( eResultSetMetaData.getColumnName( oIndex ) );

    				while( OutputRecorset.next() )
    				{
    					for( oIndex = 0; oIndex < RecordFieldName.size(); oIndex++ )
    					{
    						RecordName	= RecordFieldName.get(oIndex);																
    						RecordValue	= OutputRecorset.getString( RecordName );
	
    						if( RecordName != null )
    						{
    							if( RecordValue != null )
    								RecordSetKVPs.add( RecordName, RecordValue );
    							else
    								RecordSetKVPs.add( RecordName, "" );
    						}
    					}
    				}
    				Retorno = true;
    			} 
    			catch (SQLException e) 
    			{
    				Retorno = false;
    				Debug("[Functions - ExecutePLSQLQuery] - Error: " + e.getMessage(), "Standard");
    			}

    			Debug("[Functions - ExecutePLSQLQuery] - Resultado exitoso de la Query en la Base de Datos", "Standard");
    			Debug("[Functions - ExecutePLSQLQuery] - Numero Total de KVPairs: " + String.valueOf(RecordSetKVPs.count()), "Standard");

    			if( RecordSetKVPs.count() > 0 )
    			{
    				for ( oIndex = 0; oIndex < RecordSetKVPs.count(); oIndex++ ) 
    				{
    					Debug("[Functions - ExecutePLSQLQuery] - KeyName: " + RecordSetKVPs.getKey(oIndex) +", KeyValue: " + RecordSetKVPs.getValue(oIndex), "Standard");	
    				}
    			}
    			LocalDAP.CloseDataBase();
    		}
    		else
    		{
    			if( LocalDAP.GetErrorMessage() != "" )
    			{
    				Debug("[Functions - ExecutePLSQLQuery] - Problemas al ejecutar query '" + pSQLQuery + "'", "Standard");
    				Debug("[Functions - ExecutePLSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
    			}

    			if( pReturnRecorset )
    			{
    				Retorno			= false;
    				RecordSetKVPs	= null;
    			}

    			LocalDAP.CloseDataBase();
    		}
    	}
    	else
    	{
    		Debug("[Functions - ExecutePLSQLQuery] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
    		Debug("[Functions - ExecutePLSQLQuery] - Error: " + LocalDAP.GetErrorMessage(), "Trace");
    	}
    	Debug("[Functions - ExecutePLSQLQuery] (stop)", "Standard");
    
    	return Retorno;
    }    
    
    public String ExecOracleStoreProcedure(String pConnectionURL, String pUserName, String pPassword, int pNumberOutParameters, String pDelimiterOutParameters, String SQLQuery, int ConnectionTimeOut, int QueryTimeOut)
    {
    	String OutputValue = "";
    	
       	Debug("[Functions - ExecOracleStoreProcedure] (init)", "Standard");
    	Debug("[Functions - ExecOracleStoreProcedure] *** ConnectionURL :" + pConnectionURL, "Trace");
    	Debug("[Functions - ExecOracleStoreProcedure] *** UserName      :" + pUserName, "Trace");
    	Debug("[Functions - ExecOracleStoreProcedure] *** Password      :" + pPassword, "Trace");
    	Debug("[Functions - ExecOracleStoreProcedure] *** Out Parameters:" + String.valueOf(pNumberOutParameters), "Trace");
    	Debug("[Functions - ExecOracleStoreProcedure] *** Procedure     :" + SQLQuery, "Trace");
    	
    	if( DataAccessPointOracle.OpenDataBase(pConnectionURL, pUserName, pPassword, ConnectionTimeOut) )
    	{
    		Debug("[Functions - ExecOracleStoreProcedure] - Coneccion exitosa con la Base de Datos '" + pConnectionURL + "',", "Standard");
    		
    		Debug("[Functions - ExecOracleStoreProcedure] - Ejecutando Store Procedure en la Base de Datos '" + SQLQuery + "'", "Standard");
    		
    		if( DataAccessPointOracle.ExecuteCallableStatement(SQLQuery, pNumberOutParameters, pDelimiterOutParameters, QueryTimeOut) )
    		{
    			OutputValue = DataAccessPointOracle.GetResultadoSP();
    			
    			Debug("[Functions - ExecOracleStoreProcedure] - Se ha ejecutado en forma correcta store procedure '" + SQLQuery + "'", "Standard");
				Debug("[Functions - ExecOracleStoreProcedure] - Parametros obtenidos de store procedure '" + OutputValue + "'", "Trace");
    		}
    		else
    		{
				Debug("[Functions - ExecOracleStoreProcedure] - Problemas al ejecutar store procedure '" + SQLQuery + "'", "Standard");
				Debug("[Functions - ExecOracleStoreProcedure] - Error: " + DataAccessPointOracle.GetErrorMessage(), "Trace");
    		}
    		
    		DataAccessPointOracle.CloseDataBase();
    	}
    	else
    	{
    		Debug("[Functions - ExecOracleStoreProcedure] - Problemas al conectar con la Base de Datos '" + pConnectionURL + "'.", "Standard");
			Debug("[Functions - ExecOracleStoreProcedure] - Error: " + DataAccessPointOracle.GetErrorMessage(), "Trace");
    	}
    	
    	Debug("[Functions - ExecOracleStoreProcedure] (stop)", "Standard");
    	
    	return OutputValue;
    }
    
    public boolean WriteLocalFile(String InputFileName, String InputMessage, boolean AppendMessage)
    {
    	return ( WriteLocalFile(InputFileName, InputMessage, AppendMessage, true) );
    }
    
    public boolean WriteLocalFile(String InputFileName, String InputMessage, boolean AppendMessage, boolean PrintDetails)
    {
    	boolean				IsOK				= false;
    	FileOutputStream 	oFileOutputStream	= null;
    	
    	Debug("[Functions - WriteLocalFile] (init)", "Standard");
    	
        try
        {
            File oLocalFile	= new File(InputFileName);

            oLocalFile.createNewFile();

            if( oLocalFile.canWrite() )
            {
            	if( AppendMessage )
            		oFileOutputStream = new FileOutputStream( oLocalFile, true );
            	else
            		oFileOutputStream = new FileOutputStream( oLocalFile, false );
            	            	
                oFileOutputStream.write( InputMessage.getBytes() );
                oFileOutputStream.close();
                
                IsOK = true;
                
                if (PrintDetails == true )
                	Debug("[Functions - WriteLocalFile] - Se ha registrado la informacion '" + InputMessage + "' en forma exitosa.", "Standard");
            }
            else
            {
            	IsOK = false;
            	Debug("[Functions - WriteLocalFile] - Problemas al escribir archivo local '" + InputFileName + "'.", "Standard");
        	}
        }

        catch( Exception e )
        {
        	IsOK = false;
        	Debug("[Functions - WriteLocalFile] - Se ha detectado el siguiente Error: " + e.getMessage(), "Trace");      	
        }    	
    	
    	Debug("[Functions - WriteLocalFile] (stop)", "Standard");
    	
    	return IsOK;
    }
    
    public boolean Debug (String Message, String Level)
    {
        String DebugMessage = "";

        if( DebugLevel.compareTo("None") == 0 )
            return true;

        if( DebugFilePath.compareTo("") == 0 )
            return true;

        if( (DebugLevel.compareTo("Standard") == 0) && (Level.compareTo("Standard") != 0) )
            return true;

        if( (DebugLevel.compareTo("Trace") == 0) && (Level.compareTo("Detail") == 0) )
            return true;

        if( (DebugLevel.compareTo("TRACE") == 0) && (Level.compareTo("Detail") == 0) )
            return true;
             
        TimeZone tz = TimeZone.getTimeZone("Chile/Continental");
        
        SimpleDateFormat DateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
        
        DateFormatter.setTimeZone(tz);
        
        String DateString = DateFormatter.format(new Date());

        DebugMessage += "[" + DateString + "] (" + AsteriskID + ") " + (new String(Level + "     ")).substring(0, 10);

        DebugMessage += Message + "\n";

        try
        {
            File fDebugFile = new File(DebugFilePath);

            if( fDebugFile.length() > Integer.parseInt(DebugFileMaxSize) )
            {
                SimpleDateFormat df = new SimpleDateFormat("_yyyyMMdd_HHmmss.");
                df.setTimeZone(tz);
                String ds = df.format(new Date());

                File fOldDebugFile = new File(DebugFilePath.replaceFirst("\\.", ds));

                if( fDebugFile.renameTo(fOldDebugFile) )
                    fDebugFile = new File(DebugFilePath);
            }

            fDebugFile.createNewFile();

            if( fDebugFile.canWrite() )
            {
                FileOutputStream osDebugFile = new FileOutputStream(fDebugFile, true);
                osDebugFile.write(DebugMessage.getBytes());
                osDebugFile.close();
            }
            else
            {
                return false;
            }
        }

        catch( Exception e )
        {
            return false;
        }

        return true;
    }
    
	public boolean Debug (String Message)
    {
    	return Debug(Message, "Standard");
    }
    
    private void ReadParameters (String ParametersFile)
    {      
        Params.ReadParametersFile(ParametersFile);

        Debug("[Functions - ReadParameters] Leyendo archivo de par�metros.", "Detail");
    }    
    
	private void Initialize(String ParametersFile, String AsteriskUniqueID)
    {
		AsteriskID = AsteriskUniqueID;		
		ReadParameters(ParametersFile);
		
		PromptList.clear();
		
        DebugLevel 			= Params.GetValue("DebugLevel", "None");
        DebugFilePath 		= Params.GetValue("DebugFilePath");
        DebugFileMaxSize 	= Params.GetValue("DebugFileMaxSize", "1000");        
    }
	
	public Functions (String ParametersFile, String AsteriskUniqueID)
	{	
		Initialize(ParametersFile, AsteriskUniqueID);
	}
	
	public Functions (String AsteriskUniqueID)
	{	
		Initialize("Functions.properties", AsteriskUniqueID);
	}		
}