package eContact;


import java.io.*;
import java.util.*;

/**
 * Clase gen�rica para manipulaci�n de par�metros desde archivos de par�metros. Key-Value Pair.
 * @author Julio G.<br>
 * Comentarios por Daniel Astudillo.
 * @version 2007
 */
public class Parameters
{
    public boolean IsLoaded = false;

    private Properties properties = null;

    /**
     * Constructor default.
     */
    public Parameters()
    {
        properties = new Properties();
    }

    /**
     * Lee archivo de par�metros y almacena en un objeto <b>properties</b> los pares key-value.
     * @param ParametersFile Archivo de par�metros
     * @return true o false
     */
    public boolean ReadParametersFile (String ParametersFile)
    {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(ParametersFile);

            properties.load(fis);

            fis.close();

            IsLoaded = true;
        }

        catch (IOException e) {
        	e.printStackTrace();
            try
            {
                if( fis != null) fis.close();
            }
            catch (Exception e2) {
            }

            return false;
        }

        return true;
    }

    /**
     * Escribe en un archivo de par�metros los pares key-value.
     * @param ParametersFile Archivo de par�metros
     * @return true o false
     */
    public boolean WriteParametersFile (String ParametersFile)
    {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(ParametersFile);

            properties.store(fos, "");

            fos.close();
        }

        catch (IOException e) {
            try
            {
                if( fos != null) fos.close();
            }
            catch (Exception e2) {
            }

            return false;
        }

        return true;
    }

    /**
     * Obtiene el valor de un par�metro.
     * @param ParameterKey Nombre del par�metro
     * @return Valor del par�metro
     */
    public String GetValue (String ParameterKey)
    {
        return properties.getProperty(ParameterKey, "");
    }

    /**
     * Obtiene el valor de un par�metro. Si el par�metro no se encuentra devuelve un valor por default.
     * @param ParameterKey Nombre del par�metro
     * @param DefaultValue Valor default.
     * @return Valor del par�metro
     */
    public String GetValue (String ParameterKey, String DefaultValue)
    {
        return properties.getProperty(ParameterKey, DefaultValue);
    }

    /**
     * Setea el valor de un par�metro.
     * @param ParameterKey Nombre del par�metro.
     * @param ParameterValue Valor del par�metro
     */
    public void SetValue (String ParameterKey, String ParameterValue)
    {
        properties.setProperty(ParameterKey, ParameterValue);

        return;
    }

    /**
     * Elimina todos los elementos. 
     */
    public void Clear ()
    {
        properties.clear();

        return;
    }
}