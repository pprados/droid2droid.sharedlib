package org.remoteandroid.internal;

import static org.remoteandroid.internal.Constants.NDEF_MIME_TYPE;
import static org.remoteandroid.internal.Constants.PREFIX_LOG;
import static org.remoteandroid.internal.Constants.TAG_NFC;
import static org.remoteandroid.internal.Constants.W;

import java.util.Arrays;

import org.remoteandroid.RemoteAndroidNfcHelper;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UninitializedMessageException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;

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
	
	@Override
    public NdefMessage createNdefMessage(RemoteAndroidManager manager)
    {
    	return manager.createNdefMessage();
    }
	
	@Override
    public RemoteAndroidInfo parseNfcRawMessages(Context context,Parcelable[] rawMessages)
	{
        if (rawMessages != null) 
        {
        	for (int i = 0; i < rawMessages.length; i++) 
            {
        		NdefMessage msg = (NdefMessage) rawMessages[i];
				for (NdefRecord record:msg.getRecords())
				{
					if ((record.getTnf()==NdefRecord.TNF_MIME_MEDIA)
							&& Arrays.equals(NDEF_MIME_TYPE, record.getType()))
					{
						try
						{
							Messages.Identity identity=Messages.Identity.newBuilder().mergeFrom(record.getPayload()).build();
		    				return ProtobufConvs.toRemoteAndroidInfo(context,identity);
						}
						catch (InvalidProtocolBufferException e)
						{
							if (W) Log.d(TAG_NFC,PREFIX_LOG+"Invalide data");
						}
						catch (UninitializedMessageException e)
						{
							if (W) Log.d(TAG_NFC,PREFIX_LOG+"Invalide data");
						}
					}
				}
            }
        }
		return null;
	}	
	
	@Override
	public void onNewIntent(Activity activity,Intent intent)
	{
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.GINGERBREAD)
		{
			NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
			activity.setIntent(intent);
			final Tag tag=(Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if (tag!=null)
			{
				// Check the caller. Refuse spoof events
				activity.checkCallingPermission("com.android.nfc.permission.NFCEE_ADMIN");
				RemoteAndroidInfo info=parseNfcRawMessages(activity, 
					intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES));
				if (info!=null)
				{
					if (mCallBack!=null)
						mCallBack.onNfcDiscover(info);
				}
			}
		}
	}	
	
	@Override
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
	
	@Override
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