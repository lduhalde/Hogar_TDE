package eContact;

import java.sql.*;

public class OracleDBAccess {
	private String				ecErrorMessage		= "";
	private String				ecResultadosSP		= "";
	private Connection 			ecConexion			= null;
	private	Statement 			ecStatement			= null;
	private CallableStatement 	ecCallableStatement = null;
	
	
	public String GetErrorMessage(){
		return ecErrorMessage;
	}
	
	public String GetResultadoSP(){
		return ecResultadosSP;
	}
	
	public boolean OpenDataBase(String ecConnectionURL, String ecUserName, String ecPassword, int ConnectionTimeOut){
		ecErrorMessage 	= "";

		try{
			DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
		}
		catch( SQLException e ){
			ecErrorMessage = e.getMessage();		
			
			return false;
		}
		
		try{
			DriverManager.setLoginTimeout( ConnectionTimeOut );
			
			ecConexion = DriverManager.getConnection( ecConnectionURL, ecUserName, ecPassword );
			
			return true;
		}
		catch(Exception e){ 
			ecErrorMessage = e.getMessage();			
			
			return false;
		}
	}
	
	public ResultSet ExecuteQuery (String SQLQuery, int QueryTimeout)
	{	
		ecErrorMessage 		= "";
		ResultSet oRecords	= null;
		
		try{
			ecStatement = ecConexion.createStatement();
			
			if( QueryTimeout > 0 )
				ecStatement.setQueryTimeout( QueryTimeout);
				
			oRecords = ecStatement.executeQuery( SQLQuery );
		}
		catch(SQLException sqle)
		{
			ecErrorMessage = sqle.getMessage();			
			oRecords = null;
		}
		return oRecords;
	}
	
	public int ExecuteUpdate(String SQLQuery, int QueryTimeOut)
	{
		int	oRecords		= 0;
		this.ecErrorMessage	= "";
		
		try{
			ecStatement = (Statement) this.ecConexion.createStatement();
				
			if( QueryTimeOut > 0 )
				ecStatement.setQueryTimeout( QueryTimeOut );
			
			oRecords = ecStatement.executeUpdate( SQLQuery );
		}
		catch(SQLException sqle)
		{
			ecErrorMessage = sqle.getMessage();	
		
			oRecords = 0;
		}
		return oRecords;		
	}		
	
	public boolean ExecuteCallableStatement(String SQLSentence, int NumberOutParameters, String DelimiterOutParameters, int QueryTimeOut)
	{
		boolean	IsOk		= false;
		String 	Resultados 	= "";
		ecErrorMessage 	 	= "";
		ecCallableStatement = null;
		
		try{
			ecCallableStatement = this.ecConexion.prepareCall( SQLSentence );
			
			for(int oIndex = 1; oIndex <= NumberOutParameters; oIndex++)
			{
				ecCallableStatement.registerOutParameter(oIndex, java.sql.Types.VARCHAR);
			}
			
			if( QueryTimeOut > 0) 
				ecCallableStatement.setQueryTimeout( QueryTimeOut );
				
			ecCallableStatement.execute();			

			for(int oIndex = 1; oIndex <= NumberOutParameters; oIndex++)
			{
				Resultados += ecCallableStatement.getString(oIndex).toString() + DelimiterOutParameters;
			}

			if( Resultados.length() > 0 )
				Resultados = Resultados.substring(0, Resultados.length() - 1);
			
			IsOk = true;
		}
		catch(Exception e)
		{
			IsOk			= false;
			ecErrorMessage 	= e.getMessage();
			Resultados		= "";
		}
		
		ecResultadosSP 		= Resultados;
		ecCallableStatement = null;

		return IsOk;
	}	

	public void CloseDataBase(){
		try {
			if( ecConexion.isClosed() )
				return;
			else
				ecConexion.close();	
		}
		catch (SQLException sqle) 
		{			
			ecErrorMessage = sqle.getMessage();
			return;
		}
	}	
	
	public boolean IsConnected(){
		try {
			if( !ecConexion.isClosed() )
				return false;
			else
				return true;				
		} 
		catch (SQLException e) 
		{			
			return false;
		}
	}
	
	
	public String getURLConexion(String host, String port, String service, boolean isSID){
    	String connectString = "";
		try{  
			if (isSID){
				connectString = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = "+host+")(PORT = "+port+"))(CONNECT_DATA = (SID = "+service+")))";
			}else{
				connectString = "jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = "+host+")(PORT = "+port+"))(CONNECT_DATA = (SERVICE_NAME = "+service+")))";
			}			
		}catch(Exception e){        	   
			e.printStackTrace();               
		}  
        return connectString;	    
	}

	public String getEcErrorMessage() {
		return ecErrorMessage;
	}

	public void setEcErrorMessage(String ecErrorMessage) {
		this.ecErrorMessage = ecErrorMessage;
	}

	public String getEcResultadosSP() {
		return ecResultadosSP;
	}

	public void setEcResultadosSP(String ecResultadosSP) {
		this.ecResultadosSP = ecResultadosSP;
	}

	public Connection getEcConexion() {
		return ecConexion;
	}

	public void setEcConexion(Connection ecConexion) {
		this.ecConexion = ecConexion;
	}

	public Statement getEcStatement() {
		return ecStatement;
	}

	public void setEcStatement(Statement ecStatement) {
		this.ecStatement = ecStatement;
	}

	public CallableStatement getEcCallableStatement() {
		return ecCallableStatement;
	}

	public void setEcCallableStatement(CallableStatement ecCallableStatement) {
		this.ecCallableStatement = ecCallableStatement;
	}
	
	
	
}