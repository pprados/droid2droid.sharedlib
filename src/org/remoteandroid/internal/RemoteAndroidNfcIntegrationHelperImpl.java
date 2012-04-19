package org.remoteandroid.internal;

import org.remoteandroid.RemoteAndroidNfcHelper;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;

public class RemoteAndroidNfcIntegrationHelperImpl 
implements RemoteAndroidNfcHelper
{
	private RemoteAndroidNfcHelper.OnNfcDiscover mCallBack; 
	public RemoteAndroidNfcIntegrationHelperImpl(RemoteAndroidNfcHelper.OnNfcDiscover callback)
	{
		mCallBack=callback;
	}
//	private void onNfcCreate()
//	{
//		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
//		{
//			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//	        if (nfcAdapter != null) 
//	        {
//	        	nfcAdapter.setNdefPushMessageCallback(new CreateNdefMessageCallback()
//	        	{
//
//					@Override
//					public NdefMessage createNdefMessage(NfcEvent event)
//					{
//						RemoteAndroidManager manager=getRemoteAndroidManager();
//						if (manager!=null)
//						{
//							return manager.createNdefMessage();
//						}
//						return null;
//					}
//	        		
//	        	}, this);
//	        }
//		}
//	}
	public void onNewIntent(Activity activity,RemoteAndroidManager manager,Intent intent)
	{
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
		{
			if (manager==null)
				return;
			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
			activity.setIntent(intent);
			final Tag tag=(Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if (tag!=null)
			{
				// Check the caller. Refuse spoof events
				activity.checkCallingPermission("com.android.nfc.permission.NFCEE_ADMIN");
				RemoteAndroidInfo info=manager.parseNfcRawMessages(activity, 
					intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));
				if (info!=null)
				{
					mCallBack.onNfcDiscover(info);
				}
			}
		}
	}	
	public void onResume(Activity activity)
	{
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
		{
			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
			if (nfcAdapter!=null)
			{
				PendingIntent pendingIntent = 
						PendingIntent.getActivity(activity, 0, 
							new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
			}
		}
	}
    public void onPause(Activity activity)
    {
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
		{
			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
			if (nfcAdapter!=null)
			{
				nfcAdapter.disableForegroundDispatch(activity);
			}
		}
    }

}
