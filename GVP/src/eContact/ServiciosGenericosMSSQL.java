package eContact;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ServiciosGenericosMSSQL {

	private MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
	
	public ServiciosGenericosMSSQL(MSSQLDBAccess conectorBD) {		
		this.conectorBD = conectorBD;
	}
	
	public ServiciosGenericosMSSQL(String host, String port, String user, String password) {
		conectorBD.setHost(host);
		conectorBD.setPort(port);
		conectorBD.setUser(user);
		conectorBD.setPassword(password);		
	}

	public String obtener_DB(){
		return conectorBD.getDatabase();
	}
	
	public String ejecutarSP(String rut, String nombre, String telefono, String origen, String escliente, String marca, String canal, String idtrx_ws){
		String retornar = "";
		String resultado = "";
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		
		ResultSet rs = null;
		try 
		{
			CallableStatement ps = conectorBD.getConnection().prepareCall("{ call "+conectorBD.getDatabase()+".dbo.CTC_SP_OBTENERRESPUESTA(?,?,?,?,?,?,?,?,?) }");
		    ps.setQueryTimeout(30);
		    ps.setString(1, rut);
		    ps.setString(2, nombre);
		    ps.setString(3, telefono);
		    ps.setString(4, origen);
		    ps.setString(5, escliente);
		    ps.setString(6, marca);
		    ps.setString(7, canal);
		    ps.setString(8, idtrx_ws);
		    ps.setString(9, resultado);						
			rs = ps.executeQuery();
			
			if (rs.next()){
				retornar = rs.getString(1);
		   	}else{
		   		retornar = "NO HAY RESULTADOS";
		   	}
		}		
		catch (SQLException e) 
		{		
			System.out.println("SQL Exception");
			e.printStackTrace();
			
		}
		    		
		finally
		{
			conectorBD.cerrarConexion();
		}
		return retornar;
	}
	
	
	public String ejecutarSP_CallBack(String rut, String nombre, String telefono, String origen, String escliente, String marca){
		String retornar = "";
		String resultado = "";
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		
		ResultSet rs = null;
		try 
		{
			CallableStatement ps = conectorBD.getConnection().prepareCall("{ call "+conectorBD.getDatabase()+".dbo.CTC_SP_OBTENERRESPUESTA(?,?,?,?,?,?,?) }");
		    ps.setQueryTimeout(30);
		    ps.setString(1, rut);
		    ps.setString(2, nombre);
		    ps.setString(3, telefono);
		    ps.setString(4, origen);
		    ps.setString(5, escliente);
		    ps.setString(6, marca);
		    ps.setString(7, resultado);						
			rs = ps.executeQuery();
			
			if (rs.next()){
				retornar = rs.getString(1);
		   	}else{
		   		retornar = "NO HAY RESULTADOS";
		   	}
		}
		catch (SQLException e) 
		{		
			System.out.println("SQL Exception");
			e.printStackTrace();
			
		}
		    		
		finally
		{
			conectorBD.cerrarConexion();
		}
		return retornar;
	}
	
	
	
	
	
	
	public List<String> buscarColumnasEnTabla(String tabla) {
		List<String> lista = new ArrayList<String>();
		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try 
		{
			String sql = "select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where table_name = '"+tabla+"'";						
			ps = conectorBD.getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			
		}
		catch (SQLException e) 
		{		
			e.printStackTrace();
		}
		    
		try
		{    
		   	if (rs!=null)
		   	{
		   		while(rs.next())
				{		   			
		   			String columna = rs.getString("COLUMN_NAME");		   			
		   			lista.add(columna);
	            }
		   	}
		   	else
		   	{
		   		lista = null;
		   	}
	         	               
	    }
	    catch(Exception e)
	    {
	       e.printStackTrace();
	    }
	       
		finally
		{
			conectorBD.cerrarConexion();
		}
		
		return lista;
	}

	
	public Map<String, String> buscarColumnasYTipoDatoEnTabla(String tabla) {
		 Map<String, String> mapa = new HashMap<String, String>();
		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try 
		{
			String sql = "select COLUMN_NAME, DATA_TYPE from INFORMATION_SCHEMA.COLUMNS where table_name = '"+tabla+"'";						
			ps = conectorBD.getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			
		}
		catch (SQLException e) 
		{		
			e.printStackTrace();
		}
		    
		try
		{    
		   	if (rs!=null)
		   	{
		   		while(rs.next())
				{		   			
		   			String columna = rs.getString("COLUMN_NAME");
		   			String tipo_dato = rs.getString("DATA_TYPE");
		   			mapa.put(columna, tipo_dato);
	            }
		   	}
		   	else
		   	{
		   		mapa = null;
		   	}
	         	               
	    }
	    catch(Exception e)
	    {
	       e.printStackTrace();
	    }
	       
		finally
		{
			conectorBD.cerrarConexion();
		}
		
		return mapa;
	}
	
	public int buscarUltimoIDEnTabla(String tabla) {
		 int resultado = 0;
		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try 
		{
			String sql = "select TOP 1 record_id AS ULTIMOID from "+conectorBD.getDatabase()+".dbo."+tabla+" ORDER BY record_id DESC";						
			ps = conectorBD.getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			
		}
		catch (SQLException e) 
		{		
			e.printStackTrace();
		}
		    
		try
		{    
		   	if (rs!=null)
		   	{
		   		if(rs.next())
				{		   			
		   			resultado = rs.getInt("ULTIMOID") + 1;
		   			
		   			if (resultado == 0){
		   				resultado = 1000;
		   			}
	            }
		   	}
		   	else
		   	{
		   		resultado = 1000;
		   	}
	         	               
	    }
	    catch(Exception e)
	    {
	       e.printStackTrace();
	    }
	       
		finally
		{
			conectorBD.cerrarConexion();
		}
		
		return resultado;
	}

	public int contarRegistros(String tabla) {
		 int resultado = 0;
		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try 
		{
			String sql = "select count(*) AS ULTIMOID from "+conectorBD.getDatabase()+".dbo."+tabla;						
			ps = conectorBD.getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			
		}
		catch (SQLException e) 
		{		
			e.printStackTrace();
		}
		    
		try
		{    
		   	if (rs!=null)
		   	{
		   		if(rs.next())
				{		   			
		   			resultado = rs.getInt("ULTIMOID");
		   			
		   			if (resultado == 0){
		   				resultado = 1000;
		   			}
	            }
		   	}
		   	else
		   	{
		   		resultado = 1000;
		   	}
	         	               
	    }
	    catch(Exception e)
	    {
	       e.printStackTrace();
	    }
	       
		finally
		{
			conectorBD.cerrarConexion();
		}
		
		return resultado;
	}
	
	public List<String> ejecutar_SQL_SELECT_OUTBOUND_EXPORT(String sql, List<String> columnas, Map<String, String> mapa) {
		List<String> lista = new ArrayList<String>();
		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try 
		{									
			ps = conectorBD.getConnection().prepareStatement(sql);
			rs = ps.executeQuery();			
		}
		catch (SQLException e) 
		{		
			e.printStackTrace();
		}
		    
		try
		{    
		   	if (rs!=null)
		   	{
		   		int i=0, cant = 0;
		   		while(rs.next())
				{		 
		   			String valor = "";		   			
		   			cant = columnas.size() - 1;
		   			for (String columna : columnas) {
		   				valor += columna +"=";
						if(mapa.get(columna).equalsIgnoreCase("varchar")){
							valor += rs.getString(columna);
						}else if(mapa.get(columna).equalsIgnoreCase("int")){														
							valor += rs.getInt(columna)+"";	
						}else{
							valor += rs.getString(columna);
						}
						
						if(i < cant){
							valor += "|";
							i++;
						}else{
							i=0;
						}		   			
					}
		   			
		   			valor += "\n";
		   			lista.add(valor);
	            }
		   	}
		   	else
		   	{
		   		lista = null;
		   	}
	         	               
	    }
	    catch(Exception e)
	    {
	       e.printStackTrace();
	    }
	       
		finally
		{
			conectorBD.cerrarConexion();
		}
		
		
		return lista;
	}
	
	
	public List<String> ejecutar_SQL_SELECT(String sql, List<String> columnas, Map<String, String> mapa) {
		List<String> lista = new ArrayList<String>();
		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try 
		{									
			ps = conectorBD.getConnection().prepareStatement(sql);
			rs = ps.executeQuery();			
		}
		catch (SQLException e) 
		{		
			e.printStackTrace();
		}
		    
		try
		{    
		   	if (rs!=null)
		   	{
		   		int i=0, cant = 0;
		   		while(rs.next())
				{		 
		   			String valor = "";		   			
		   			cant = columnas.size() - 1;
		   			for (String columna : columnas) {
		   				valor += columna +"=";
						if(mapa.get(columna).equalsIgnoreCase("varchar")){
							valor += rs.getString(columna);
						}else if(mapa.get(columna).equalsIgnoreCase("int")){
							valor += rs.getInt(columna)+"";
						}else{
							valor += rs.getString(columna);
						}
						
						if(i < cant){
							valor += "|";
							i++;
						}else{
							i=0;
						}		   			
					}
		   			
		   			valor += "\n";
		   			lista.add(valor);
	            }
		   	}
		   	else
		   	{
		   		lista = null;
		   	}
	         	               
	    }
	    catch(Exception e)
	    {
	       e.printStackTrace();
	    }
	       
		finally
		{
			conectorBD.cerrarConexion();
		}
		
		
		return lista;
	}
	
	public List<String> buscarTablasBD() {
		List<String> lista = new ArrayList<String>();
		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
		ResultSet rs = null;
		try 
		{
			String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES ORDER BY TABLE_NAME;";						
			ps = conectorBD.getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			
		}
		catch (SQLException e) 
		{		
			e.printStackTrace();
		}
		    
		try
		{    
		   	if (rs!=null)
		   	{
		   		while(rs.next())
				{		   			
		   			String columna = rs.getString("TABLE_NAME");		   			
		   			lista.add(columna);
	            }
		   	}
		   	else
		   	{
		   		lista = null;
		   	}
	         	               
	    }
	    catch(Exception e)
	    {
	       e.printStackTrace();
	    }
	       
		finally
		{
			conectorBD.cerrarConexion();
		}
		
		return lista;
	}
	
	public String ejecutar_SP_GENERAL_INSERT(MSSQLDBAccess conectorBD, String sql, String sql2, String sql3){
		String retornar = "";
		String resultado = "";
//		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		
		ResultSet rs = null;
		try 
		{
			CallableStatement ps = conectorBD.getConnection().prepareCall("{ call "+conectorBD.getDatabase()+".dbo.GENERAL_SP_INSERT(?,?,?,?) }");
		    ps.setQueryTimeout(30);
		    ps.setString(1, sql);
		    ps.setString(2, sql2);
		    ps.setString(3, sql3);
		    ps.setString(4, resultado);						
			rs = ps.executeQuery();
			
			if (rs.next()){
				retornar = rs.getString(1);
		   	}else{
		   		retornar = "NO HAY RESULTADOS";
		   	}
		}
		catch (SQLException e) 
		{		
			System.out.println("SQL Exception");			
			retornar = "ERROR SQL: "+e.getMessage();
			e.printStackTrace();
		}
		    		
//		finally
//		{
//			conectorBD.cerrarConexion();
//		}
		return retornar;
	}
	
	
	public String ejecutar_SP_GENERAL_INSERT(String sql, String sql2, String sql3){
		String retornar = "";
		String resultado = "";
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		
		ResultSet rs = null;
		try 
		{
			CallableStatement ps = conectorBD.getConnection().prepareCall("{ call "+conectorBD.getDatabase()+".dbo.GENERAL_SP_INSERT(?,?,?,?) }");
		    ps.setQueryTimeout(30);
		    ps.setString(1, sql);
		    ps.setString(2, sql2);
		    ps.setString(3, sql3);
		    ps.setString(4, resultado);						
			rs = ps.executeQuery();
			
			if (rs.next()){
				retornar = rs.getString(1);
		   	}else{
		   		retornar = "NO HAY RESULTADOS";
		   	}
		}
		catch (SQLException e) 
		{		
			System.out.println("SQL Exception");			
			retornar = "ERROR SQL: "+e.getMessage();
			e.printStackTrace();
		}
		    		
		finally
		{
			conectorBD.cerrarConexion();
		}
		return retornar;
	}
	
	
	public String ejecutar_SP_GENERAL_INSERT(String sql){
		String retornar = "";
		String resultado = "";
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		
		ResultSet rs = null;
		try 
		{
			CallableStatement ps = conectorBD.getConnection().prepareCall("{ call "+conectorBD.getDatabase()+".dbo.GENERAL_SP_INSERT_1(?,?) }");
		    ps.setQueryTimeout(30);
		    ps.setString(1, sql);
		    ps.setString(2, resultado);						
			rs = ps.executeQuery();
			
			if (rs.next()){
				retornar = rs.getString(1);
		   	}else{
		   		retornar = "NO HAY RESULTADOS";
		   	}
		}
		catch (SQLException e) 
		{		
			System.out.println("SQL Exception");			
			retornar = "ERROR SQL: "+e.getMessage();
			e.printStackTrace();
		}
		    		
		finally
		{
			conectorBD.cerrarConexion();
		}
		return retornar;
	}
	
	
	public String ejecutar_SQL_INSERT(String sql) {
		String retornar = "99";		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
//		ResultSet rs = null;
		try 
		{

//				String g = "INSERT INTO "+conectorBD.getDatabase()+".dbo.CBK_Lista_Negra VALUES ( " +
//						   "'" 	+ listaNegra.getRut() +  "',  " +
//						   "'" 	+ listaNegra.getTelefono()+  "'" +
//						   ")";		
//				System.out.println("SQL: "+g);
				ps = conectorBD.getConnection().prepareStatement(sql);
//				rs = ps.executeQuery();
				ps.execute();
				retornar = "00";
			
		}
		catch (SQLException e) 
		{	
			retornar = "99 - ERROR :"+e.getMessage();
			e.printStackTrace();
		}		    		
		finally
		{
			conectorBD.cerrarConexion();
		}
		return retornar;
	}	
	
	
	
	public String eliminarTodos(String tabla) {
		String resultado = "99";
		
		MSSQLDBAccess conectorBD = new MSSQLDBAccess();	
		PreparedStatement ps = null;
//		ResultSet rs = null;
		try 
		{
			String sql = "DELETE FROM "+conectorBD.getDatabase()+".dbo."+tabla;
			System.out.println("DELETE SQL "+sql);
			ps = conectorBD.getConnection().prepareStatement(sql);
			if(ps.execute()){
				resultado = "00";
			}
			
		}
		catch (SQLException e) 
		{		
			e.printStackTrace();
		}		    		
		finally
		{
			conectorBD.cerrarConexion();
		}
		
		return resultado;
	}
	
}
