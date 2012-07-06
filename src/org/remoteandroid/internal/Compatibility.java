package org.remoteandroid.internal;

import android.os.Build;

public final class Compatibility
{
    public static String MANUFACTURER;
    public static int VERSION_SDK_INT;
    public static final int VERSION_CUPCAKE=3;
    public static final int VERSION_DONUT=4;
    public static final int VERSION_ECLAIR=5;
    public static final int VERSION_ECLAIR_0_1=6;
    public static final int VERSION_ECLAIR_MR1=7;
    public static final int VERSION_FROYO=8;
    public static final int VERSION_GINGERBREAD=9;
    public static final int VERSION_GINGERBREAD_MR1=10;
    public static final int VERSION_HONEYCOMB=11;
    public static final int VERSION_HONEYCOMB_MR1=12;
    public static final int VERSION_HONEYCOMB_MR2=13;
    public static final int VERSION_ICE_CREAM_SANDWICH=14;
    static
    {
    	VERSION_SDK_INT=Integer.parseInt(Build.VERSION.SDK);
    	if (VERSION_SDK_INT<4)
    	{
    		MANUFACTURER=Build.DEVICE;
    	}
    	else
    	{
    		try
			{
				MANUFACTURER=(String)Build.class.getDeclaredField("MANUFACTURER").get(null);
			}
			catch (Exception e)
			{
				throw new Error();
			}
    	}
    }
    
}
