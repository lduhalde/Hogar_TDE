package eContact;

import eContact.KVPair;

import java.util.*;

public class KVPairList {

    private ArrayList<String> KeyList 	= new ArrayList<String>(1);
    private ArrayList<KVPair> ValueList = new ArrayList<KVPair>(1);
    private int auxI 					= -1;	
	
    public void clear()
    {
        KeyList.clear();
        ValueList.clear();
        return;
    }

	public boolean add(String Key, String Value)
    {
        if ( KeyList.add(Key) )
        {
            if ( ValueList.add(new KVPair(Key, Value)) )
            {
                return true;
            }
        }
        return false;
    }    
 
    public boolean add (String Key, int Value)
    {
        if ( KeyList.add(Key) )
        {
            if ( ValueList.add(new KVPair(Key, Value)) )
            {
                return true;
            }
        }
        return false;
    }

    public boolean add (String Key, float Value)
    {
        if ( KeyList.add(Key) )
        {
            if ( ValueList.add(new KVPair(Key, Value)) )
            {
                return true;
            }
        }
        return false;
    }

    public boolean add (String Key, boolean Value)
    {
        if ( KeyList.add(Key) )
        {
            if ( ValueList.add(new KVPair(Key, Value)) )
            {
                return true;
            }
        }
        return false;
    }

    public boolean add (String Key, KVPairList Value)
    {
        if ( KeyList.add(Key) )
        {
            if ( ValueList.add(new KVPair(Key, Value)) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean remove (String Key)
    {
        String sKey = "";
        String sValue = "";

        if ( (auxI = KeyList.indexOf(Key)) == -1 ) return false;

        sKey   = (String) KeyList.get(auxI).toString();
        sValue = (String) ValueList.get(auxI).toString();

        if ( KeyList.remove(sKey) )
        {
            if ( ValueList.remove(sValue) )
            {
                return true;
            }
        }
        return false;
    }

    public int count ()
    {
        return KeyList.size();
    }

    public boolean find (String Key)
    {
        if ( KeyList.indexOf(Key) != -1 ) return true;

        return false;
    }

    public String getKey (int Index)
    {
        return (String) KeyList.get(Index);
    }

    public String getValue (int Index)
    {
        return (String) ((KVPair) ValueList.get(Index)).getValue();
    }

    public String getValue (String Key)
    {
        if ( (auxI = KeyList.indexOf(Key)) == -1 ) return "";

        return (String) ((KVPair) ValueList.get(auxI)).getValue();
    }

    public KVPair getItem (int Index)
    {
        return (KVPair) ValueList.get(Index);
    }

    public KVPair getItem (String Key)
    {
        if ( (auxI = KeyList.indexOf(Key)) == -1 ) return null;
        
        return (KVPair) ValueList.get(auxI);
    }    
}
