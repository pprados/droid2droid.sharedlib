/******************************************************************************
 *
 * droid2droid - Distributed Android Framework
 * ==========================================
 *
 * Copyright (C) 2012 by Atos (http://www.http://atos.net)
 * http://www.droid2droid.org
 *
 ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
******************************************************************************/
package org.droid2droid.internal;

import static org.droid2droid.internal.Constants.D;
import static org.droid2droid.internal.Constants.TAG_D2D;
import static org.droid2droid.internal.Constants.W;

import java.security.PrivilegedAction;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

@TargetApi(10)
public final class NetworkTools
{
	public static final int ACTIVE_DROID2DROID		=1<<0;
	public static final int ACTIVE_NOAIRPLANE			=1<<1;
	public static final int ACTIVE_NETWORK			=1<<2;
	public static final int ACTIVE_LOCAL_NETWORK		=1<<3;
	public static final int ACTIVE_GLOBAL_NETWORK		=1<<4;
	public static final int ACTIVE_INTERNET_NETWORK	=1<<5;
	public static final int ACTIVE_BLUETOOTH			=1<<6;
	public static final int ACTIVE_PHONE_DATA			=1<<7;
	public static final int ACTIVE_PHONE_SIM			=1<<8;
	public static final int ACTIVE_NFC				=1<<9;

	@TargetApi(10)
	public static int getActiveNetwork(final Context context)
	{
		int activeNetwork=0;
		try
		{
			ConnectivityManager conn=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (conn!=null && conn.getActiveNetworkInfo()!=null)
			{
				int type=conn.getActiveNetworkInfo().getType();
				// If emulator
				if (Build.MANUFACTURER.equals("unknown"))
					type=ConnectivityManager.TYPE_ETHERNET;
				switch (type)
				{
					case ConnectivityManager.TYPE_MOBILE:
					case ConnectivityManager.TYPE_MOBILE_DUN:
					case ConnectivityManager.TYPE_MOBILE_HIPRI:
					case ConnectivityManager.TYPE_MOBILE_MMS:
					case ConnectivityManager.TYPE_MOBILE_SUPL:
					case ConnectivityManager.TYPE_WIMAX:
						activeNetwork|=ACTIVE_GLOBAL_NETWORK;
						activeNetwork|=ACTIVE_NETWORK;
						break;
					case ConnectivityManager.TYPE_BLUETOOTH:
					case ConnectivityManager.TYPE_ETHERNET:
					case ConnectivityManager.TYPE_WIFI:
						activeNetwork|=ACTIVE_LOCAL_NETWORK;
						activeNetwork|=ACTIVE_NETWORK;
						break;
		        }
			}
			
			TelephonyManager telephone=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephone.getDataState()==TelephonyManager.DATA_CONNECTED)
			{
				activeNetwork|=ACTIVE_PHONE_DATA|ACTIVE_GLOBAL_NETWORK;
				activeNetwork|=ACTIVE_NETWORK;
			}
			else
				activeNetwork&=~ACTIVE_PHONE_DATA;
	
			if (telephone.getSimState()==TelephonyManager.SIM_STATE_READY)
				activeNetwork|=ACTIVE_PHONE_SIM;
			else
				activeNetwork&=~ACTIVE_PHONE_SIM;
		}
		catch (SecurityException e)
		{
			if (W) Log.w(TAG_D2D,"Need ACCESS_NETWORK_STATE permission for the application ?");
		}
		
		try
		{
			BluetoothAdapter adapter=Droid2DroidManagerImpl.getBluetoothAdapter();
			if (adapter==null || !adapter.isEnabled())
				activeNetwork&=~ACTIVE_BLUETOOTH;
			else
				activeNetwork|=ACTIVE_BLUETOOTH;
		}
		catch (SecurityException e)
		{
			if (D) Log.d(TAG_D2D,"Need BLUETOOTH permission for the application ?");
		}
		
		
		
		if (VERSION.SDK_INT>=VERSION_CODES.GINGERBREAD_MR1)
		{
			try
			{
				boolean rc=new PrivilegedAction<Boolean>() // TODO: Optimize with static
				{

					@TargetApi(10)
					@Override
					public Boolean run()
					{
						NfcManager nfc=(NfcManager)context.getSystemService(Context.NFC_SERVICE);
						if (nfc==null || nfc.getDefaultAdapter()==null || !nfc.getDefaultAdapter().isEnabled())
							return false;
						else
							return true;
					}
				}.run();
				if (!rc)
					activeNetwork&=~ACTIVE_NFC;
				else
					activeNetwork|=ACTIVE_NFC;
			}
			catch (SecurityException e)
			{
				activeNetwork&=~ACTIVE_NFC;
			}
		}
		
		boolean airplane=Settings.System.getInt(context.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		if (airplane)
			activeNetwork&=~ACTIVE_NOAIRPLANE;
		else
			activeNetwork|=ACTIVE_NOAIRPLANE;
		return activeNetwork;
	}
	

}
