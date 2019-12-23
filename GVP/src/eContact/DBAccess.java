package eContact;

import java.sql.*;
import java.util.StringTokenizer;

public class DBAccess 
{
	private String				ecErrorMessage		= "";
	private Connection 			ecConexion			= null;
	private	Statement 			ecStatement 		= null;
	private CallableStatement 	ecCallableStatement = null;
	
	KVPairList eKVPairList = new KVPairList();

	public String GetErrorMessage()
	{
		return ecErrorMessage;
	}
	
	public boolean OpenDataBase(String ecConnectionURL, String ecDriverName, String ecUserName, String ecPassword, int ConnectionTimeOut)
	{
		this.ecErrorMessage = "";
		
		try{
			Class.forName( ecDriverName );
		}
		
		catch( ClassNotFoundException e ){
			ecErrorMessage = e.getMessage();
			e.printStackTrace();
			return false;
		}
		
		try{
			if( ConnectionTimeOut > 0)
				DriverManager.setLoginTimeout( ConnectionTimeOut );
			
			ecConexion = DriverManager.getConnection( ecConnectionURL, ecUserName, ecPassword );
			
			return true;
		}
		catch(Exception e){ 
			this.ecErrorMessage = e.getMessage();
			e.printStackTrace();
		
			return false;
		}
	}

	public ResultSet ExecuteQuery (String SQLQuery, int QueryTimeOut)
	{	
		ResultSet oRecords	= null;		
		this.ecErrorMessage	= "";
		
		try{
			ecStatement = this.ecConexion.createStatement();
			
			if( QueryTimeOut > 0 )
				ecStatement.setQueryTimeout( QueryTimeOut );
			
			oRecords = this.ecStatement.executeQuery( SQLQuery );
		}
		catch(SQLException sqle)
		{
			ecErrorMessage = sqle.getMessage();	
			sqle.printStackTrace();
						
			oRecords = null;
		}
		return oRecords;
	}

	public int ExecuteUpdate(String SQLQuery, int QueryTimeOut)
	{
		int	oRecords		= 0;
		this.ecErrorMessage	= "";
		
		try{
			ecConexion.setAutoCommit(true);
			ecStatement = (Statement) this.ecConexion.createStatement();
				
			if( QueryTimeOut > 0 )
				ecStatement.setQueryTimeout( QueryTimeOut );
			
			oRecords = ecStatement.executeUpdate( SQLQuery );
		}
		catch(SQLException sqle)
		{
			sqle.printStackTrace(); 
			ecErrorMessage = sqle.getMessage();
			oRecords = 0;
		}
		return oRecords;		
	}	
	
	public String ExecuteCallableStatement(String SQLSentence, int NumberOutParameters, String DelimiterOutParameters, int QueryTimeOut)
	{
		String Resultados 	= "";
		ecErrorMessage 	 	= "";
		ecCallableStatement = null;
		
		try{
			ecCallableStatement = this.ecConexion.prepareCall( SQLSentence );
			
			for(int oIndex = 1; oIndex < NumberOutParameters; oIndex++)
			{
				ecCallableStatement.registerOutParameter(oIndex, java.sql.Types.VARCHAR);
			}
	
			if( QueryTimeOut > 0 )
				ecCallableStatement.setQueryTimeout( QueryTimeOut );
				
			ecCallableStatement.execute();
			
			for(int oIndex = 1; oIndex < NumberOutParameters; oIndex++)
			{
				Resultados += ecCallableStatement.getString(oIndex).toString() + DelimiterOutParameters;
			}

			Resultados = Resultados.substring(0, Resultados.length() - 1);
		}
		catch(Exception e)
		{
			ecErrorMessage 	= e.getMessage();
			
			Resultados		= "";
		}
		
		ecCallableStatement = null;
		
		return Resultados;
	}	
	
	public KVPairList ObtieneParametrosServicio(String DNIS, int VectorCodigoServicio, int QueuryTimeOut)
	{
		try{
			CallableStatement 	ecCStm 			= null;
			ResultSet			ecRecordData 	= null;
			String				ecKVPName		= "";
			String				ecKVPValue		= "";
			KVPairList 			eKVPairList		= new KVPairList();
			
			ecCStm = this.ecConexion.prepareCall( "{call dbo.spRecuperaParametrosServicio(?,?)}" );
			
			ecCStm.setString(1, DNIS);			
			ecCStm.setInt(2, VectorCodigoServicio);
			
			if( QueuryTimeOut > 0 )
				ecCStm.setQueryTimeout( QueuryTimeOut );
								
			ecRecordData = ecCStm.executeQuery();
			
			while( ecRecordData.next() ){
				StringTokenizer KVPTokens = new StringTokenizer( ecRecordData.getString(1), ":" );
				
				if( KVPTokens.countTokens() > 0 )
				{
					ecKVPName  = "";
					ecKVPValue = "";
					
					ecKVPName  = KVPTokens.nextElement().toString();					
					StringTokenizer KVPValues = new StringTokenizer( KVPTokens.nextElement().toString(), "\\|" );
					
					if ( KVPValues.countTokens() > 0 )
						ecKVPValue = KVPValues.nextElement().toString();					
										
					KVPValues = null;
					eKVPairList.add( ecKVPName, ecKVPValue );			
				}
				KVPTokens = null;
			}
			
			ecRecordData.close(); 
			ecCStm.close();
			
			ecCStm 			= null;
			ecRecordData	= null;
			
			return eKVPairList;
		}
		catch (Exception e) {
			this.ecErrorMessage = e.getMessage();
			return null;
		}
	}
	
	public void CloseDataBase()
	{
		try 
		{
			if( ecConexion.isClosed() )
				return;
			else
				ecConexion.close();	
		} catch (SQLException e) {			
			this.ecErrorMessage = e.getMessage();
			return;
		}
	}	
	
	public boolean IsConnected()
	{
		try 
		{
			if( !ecConexion.isClosed() )
				return false;
			else
				return true;
		} catch (SQLException e) {
			return true;
		}
	}	
}
