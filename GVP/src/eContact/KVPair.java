package eContact;

import eContact.KVPairList;

public class KVPair {

    public static final int vString  = 1;
    public static final int vInt     = 2;
    public static final int vFloat   = 3;
    public static final int vBoolean = 4;
    public static final int vList    = 5;
    
    public String Key;
    public int Type;

    public int        IntValue;
    public float      FloatValue;
    public boolean    BooleanValue;
    public String     StringValue;
    public KVPairList ListValue;    
    
    public String getValue()
    {        
        if( Type == vInt     ) return Integer.toString(IntValue);
        if( Type == vFloat   ) return Float.toString(FloatValue);
        if( Type == vBoolean ) return Boolean.toString(BooleanValue);
        if( Type == vList    ) return new String("(list)");
        if( Type == vString  ) return StringValue;       
        return "";
    }

    public String Value()
    {
        if( Type == vInt     ) return Integer.toString(IntValue);
        if( Type == vFloat   ) return Float.toString(FloatValue);
        if( Type == vBoolean ) return Boolean.toString(BooleanValue);
        if( Type == vList    ) return new String("(list)");
        if( Type == vString  ) return StringValue;
        return "";
    }

    public KVPair(String KVKey, String KVValue)
    {
        Key         = new String(KVKey);
        Type        = vString;
        StringValue = new String(KVValue);
    }

    public KVPair(String KVKey, int KVValue)
    {
        Key      = new String(KVKey);
        Type     = vInt;
        IntValue = KVValue;
    }

    public KVPair(String KVKey, float KVValue)
    {
        Key        = new String(KVKey);
        Type       = vFloat;
        FloatValue = KVValue;
    }

    public KVPair(String KVKey, boolean KVValue)
    {
        Key          = new String(KVKey);
        Type         = vBoolean;
        BooleanValue = KVValue;
    }

    public KVPair(String KVKey, KVPairList KVValue)
    {
        Key       = new String(KVKey);
        Type      = vList;
        ListValue = KVValue;
    } 
       
}
