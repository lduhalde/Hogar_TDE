package eContact;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MSSQLDBAccess {
	
	protected Connection con = null;
	
	private String host = "";	//Ejm:	200.14.147.169
	private String port = "";	//Ejm:	1433
	private String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";	    
	
	private String database = "";
	private String connectString = ""; 
	
	private String user = "GenRouting";
	private String password = "Banco013";

    public boolean inicioBD = false;
	
    
    public MSSQLDBAccess()  
	{
	    this.iniciarConexion();
	}
    
    public MSSQLDBAccess(String host, String port, String user, String password)  
	{
    	this.host = host;
    	this.port = port;
    	this.user = user;
    	this.password = password;
	    this.iniciarConexion();
	}
    
    public void iniciarConexion() 
    {       	    
    	this.obtenerParametrosConexion();
    	try 
    	{
    		Class.forName(driver);			
			setConnection(DriverManager.getConnection(connectString, user , password));
			if (getConnection() != null) 
    		{
				inicioBD = true;
    		    System.out.println("Conexion Exitosa a la Base de datos "+database);    /*  [ Modifique IVRConfig_MAC] */
    		}
		}	 
    	catch (ClassNotFoundException e) 
    	{	
    		inicioBD = false;
    		System.out.println("oClase no encontrada!");
			e.printStackTrace();
		} 
    	catch (SQLException e) 
		{	
    		inicioBD = false;
			System.out.println("Falla de conexion con la base de datos "+database+", contacte con su proveedor!!");  /*Modifique mensaje*/
			e.printStackTrace();
		}
    }
        
	
	
	

	public void cerrarConexion()
	{
		if (con != null)
	    try 
	    {
	    	inicioBD = false;
	    	con.close();
	    }
	    catch (Exception e) 
	    {
	    	e.printStackTrace();
	    }
	 } 
			
	public void abrir()
	{
		try 
		{				
			Class.forName(driver);
			con = DriverManager.getConnection(connectString, user ,password);
			con.setAutoCommit(false);
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void cerrar()
    {        	
		try 
		{
			con.close();
		}
		catch (SQLException e) 
		{    			
			e.printStackTrace();
		}        
    }
		
	// Preguntar si Esta cerrada la conexion
    public boolean conexionCerrada()
    {
        if (con == null)
            return true;
        else
            return false;
    }
    
    
    public void obtenerParametrosConexion(){
    	Properties prop = new Properties();
    	String file = "configDB_PYME.properties";
    	
    	try {    		
			InputStream in = getClass().getResourceAsStream("/"+file);
			prop.load(in);
                             
    	   	host = prop.getProperty("host");//Ejm:	200.14.147.169
       		port = prop.getProperty("port");//Ejm:	1521
//           		String host_backup = prop.getProperty("host_backup");//Ejm:	200.14.147.169
//           		String port_backup = prop.getProperty("port_backup");//Ejm:	1521
       		String db = prop.getProperty("database");   		
        
       		connectString = "jdbc:sqlserver://"+host+":"+port+";databaseName="+db;
       		database = db;

       		System.out.println(file+"--> Exito [Leido correctamente].");

                                             
        }catch (IOException io) {
    		io.printStackTrace();
    		System.out.println(file+"--> Error IO [No se ha podido leer].");
    	}
    }
    
    
    public void setConnection(Connection con) {
		this.con = con;
	}
    
    public Connection getConnection()
    {
        return this.con;
    }                                                             
    
    @SuppressWarnings("unused")
	private void dispose() 
    {		
		return;
	}
    
    
    

	public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getConnectString() {
		return connectString;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public boolean isInicioBD() {
		return inicioBD;
	}

	public void setInicioBD(boolean inicioBD) {
		this.inicioBD = inicioBD;
	}
}