package org.remoteandroid.internal.socket.bluetooth;

import static org.remoteandroid.internal.Constants.*;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.remoteandroid.internal.Compatibility;
import org.remoteandroid.internal.Messages.Msg;
import org.remoteandroid.internal.socket.BossSocketSender;
import org.remoteandroid.internal.socket.DownstreamHandler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

// Hack: http://stackoverflow.com/questions/3397071/android-bluetooth-service-discovery-failed-exception

public class BluetoothSocketBossSender implements BossSocketSender
{
	private static AtomicInteger sId=new AtomicInteger();
	
	public static final UUID[] sKeys=
	{
		UUID.fromString("07687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("17687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("27687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("37687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("47687b0e-cbd1-42b2-ad70-fabf739e5a56"),
/*	
		UUID.fromString("47687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("2bad19e0-8662-480a-b74e-8fe031a6c8e9"),
		UUID.fromString("9cec93f9-0caa-4114-81b2-eed208b48f83"),
		UUID.fromString("6bcdea3b-e4eb-43e7-acf1-7c37ae805497"),
		UUID.fromString("306e9a8d-9a1b-4910-b168-b85af5b84f7c"),
		UUID.fromString("5b4c6919-5b62-416c-be5c-f4a8e3f1bca4"),
		UUID.fromString("aa0b4e69-5600-4149-88d4-9c85b2733e81"),
*/		
/*		
		UUID.fromString("514a9fa1-538d-4d34-9559-f0dd2a8f9280"),
		UUID.fromString("7c29e5e8-1c9b-43c9-b349-d99503f1ecdc"),
		UUID.fromString("f6b67392-8319-4c75-b0a8-8229a4bef402"),
		UUID.fromString("9adee858-77fd-4ffa-93a7-5d0a11af8f4a"),
		UUID.fromString("9404715e-54cf-4444-99a3-67e282ca11a6"),
		UUID.fromString("91890d84-e0da-42e0-85ab-a58b50abfd1d"),
		UUID.fromString("70e96911-8edb-45b5-aaf3-c7da3cb473da"),
		UUID.fromString("0f866321-527d-4354-a71c-eee07455ccef"),
*/		
	};
	public static final UUID[] sKeysAno=
	{
		UUID.fromString("57687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("67687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("77687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("87687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("97687b0e-cbd1-42b2-ad70-fabf739e5a56"),
/*	
		UUID.fromString("47687b0e-cbd1-42b2-ad70-fabf739e5a56"),
		UUID.fromString("2bad19e0-8662-480a-b74e-8fe031a6c8e9"),
		UUID.fromString("9cec93f9-0caa-4114-81b2-eed208b48f83"),
		UUID.fromString("6bcdea3b-e4eb-43e7-acf1-7c37ae805497"),
		UUID.fromString("306e9a8d-9a1b-4910-b168-b85af5b84f7c"),
		UUID.fromString("5b4c6919-5b62-416c-be5c-f4a8e3f1bca4"),
		UUID.fromString("aa0b4e69-5600-4149-88d4-9c85b2733e81"),
*/		
/*		
		UUID.fromString("514a9fa1-538d-4d34-9559-f0dd2a8f9280"),
		UUID.fromString("7c29e5e8-1c9b-43c9-b349-d99503f1ecdc"),
		UUID.fromString("f6b67392-8319-4c75-b0a8-8229a4bef402"),
		UUID.fromString("9adee858-77fd-4ffa-93a7-5d0a11af8f4a"),
		UUID.fromString("9404715e-54cf-4444-99a3-67e282ca11a6"),
		UUID.fromString("91890d84-e0da-42e0-85ab-a58b50abfd1d"),
		UUID.fromString("70e96911-8edb-45b5-aaf3-c7da3cb473da"),
		UUID.fromString("0f866321-527d-4354-a71c-eee07455ccef"),
*/		
	};
	public static final UUID sDiscoverUUID=UUID.fromString("15ef2fee-f765-438b-b540-df1bf93d7712");

	static List<UUID> sUUIDUseds=Collections.synchronizedList(new ArrayList<UUID>());
	static Method sMethod;
	static
	{
		try
		{
			sMethod = BluetoothDevice.class.getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
		}
		catch (Exception e)
		{
			if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Anonymous bluetooth not supported with this device");
		}
	}

	private BluetoothSocketChannel mChannel;

	private int mId;
    private Thread mThreadW;
    private Thread mThreadR;
    
    private DownstreamHandler mHandler;
	BluetoothAdapter mAdapter;

    private LinkedBlockingQueue<Msg> mMsgs=new LinkedBlockingQueue<Msg>();
   
    /*package*/ String mName;
    /*package*/ String mMac;
    /*package*/ boolean mSecure; 
    
    BluetoothSocketBossSender(Uri uri,DownstreamHandler handler) throws UnknownHostException, IOException
	{
    	mId=sId.incrementAndGet();
    	// FIXME: tolérer l'absence de privilège bluetooth
    	mHandler=handler;
    	mSecure=SCHEME_BTS.equals(uri.getScheme());
    	if (!mSecure && (Compatibility.VERSION_SDK_INT<Compatibility.VERSION_GINGERBREAD))
    		throw new IllegalArgumentException("Insecure bluetooth not supported");
    	final String authority=uri.getAuthority();
    	StringBuilder mac=new StringBuilder(authority.length()/2*3);
    	for (int i=0;i<authority.length();i+=2)
    	{
    		mac.append(authority.substring(i,i+2)).append(':');
    	}
    	mac.setLength(mac.length()-1);
    	mMac=mac.toString();
    	mName=null;
    	if (!BluetoothAdapter.checkBluetoothAddress(mMac))
    	{
    		mName=mMac; // Find by name
    		mMac=null;
    	}

    	mAdapter=BluetoothAdapter.getDefaultAdapter();
    	if (!mAdapter.isEnabled())
    		throw new IOException("Bluetooth disabled");
    	BluetoothDevice device=null;
    	
    	device=mAdapter.getRemoteDevice(mMac);
    	
    	if (!AUTO_PAIRING_BT)
    	{
	    	if (mSecure)
	    	{
		    	for (BluetoothDevice dev:mAdapter.getBondedDevices())
		    	{
		    		if (mMac!=null && dev.getAddress().equals(mMac))
		    		{
		    			device=dev;
		    			break;
		    		}
		    		if (mName!=null && dev.getName().equalsIgnoreCase(mName))
		    		{
		    			device=dev;
		    			break;
		    		}
		    	}
	    	}
	    	else
	    		device=mAdapter.getRemoteDevice(mMac);
    	}
    	else
    		device=mAdapter.getRemoteDevice(mMac);
    	
    	if (device==null)
    		throw new IllegalArgumentException("BT Not found device "+((mName==null) ? mMac : mName));
    	
    	// Try one UUID. If it's failed, try with another one.
    	// Use a random selection.
    	BluetoothSocket socket=null;
    	IOException lastException=new IOException("No more connection");

    	/* 2 téléphones différents peuvent utiliser le même UUID pour se connecter. Mais un téléphone ne peut utiliser le même UUID 2 fois. */
    	UUID uuid=null;
    	for (int i=0;i<BT_NB_UUID;++i)
    	{
    		for (;;)
    		{
    			if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"Try with uuid "+i+"...");
	    		try
	    		{
	    			if (mSecure)
	    			{
	    	    		uuid=sKeys[i];
	    				socket=device.createRfcommSocketToServiceRecord(uuid);
	    			}
	    			else
	    			{
	    	    		uuid=sKeysAno[i];
	    				socket=device.createInsecureRfcommSocketToServiceRecord(uuid);
	    			}
	    			if (BT_HACK_WAIT_AFTER_CREATE_RF_COMM!=0)
	    				try { Thread.sleep(BT_HACK_WAIT_AFTER_CREATE_RF_COMM); } catch (InterruptedException e) {} 
	    	    	socket.connect(); // May be blocked if the same uuid is allready used.
	    	    	if (I) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Use uuid "+i);
	    	        mChannel=new BluetoothSocketChannel(uuid,socket,mSecure);
	    			return; // Connection correct.
	    		}
	    		catch (IOException e)
	    		{
	    			lastException=e;
	    		}
    			if (lastException.getMessage().indexOf("busy")==-1)
    				throw lastException;
    			socket=null;
    			break;
    		}
    	}
    	if (socket==null)
    	{
    		throw lastException;
    	}
	}
    public void pushMessage(Msg msg)
    {
    	mMsgs.add(msg);
    }
    public boolean isConnected()
    {
    	return mChannel.isConnected();
    }
    @Override
    public void close() throws IOException
    {
    	mChannel.close(); // Stop read thread 
    	mThreadW.interrupt();
    	mThreadW=null;
    	mThreadR.interrupt();
    	mThreadR=null;
    }
    
    @Override
    public void start()
    {
    	if (mThreadW!=null) 
    	{
    		if (W) Log.w(TAG_CLIENT_BIND,PREFIX_LOG+"Allrealdy started");
    		return;
    	}
    	mThreadW=new Thread(new WriteThread(),"Write Bluetooth Socket #"+mId);
    	mThreadW.setDaemon(true);
    	mThreadW.start();
    	mThreadR=new Thread(new ReadThread(),"Read Bluetooth Socket #"+mId);
    	mThreadR.setDaemon(true);
    	mThreadR.start();
    }

    class WriteThread implements Runnable
    {
	    @Override
	    public void run()
	    {
	    	Looper.prepare();
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
	    	for (;;)
	    	{
	    		try
				{
	        		Msg msg=mMsgs.take();
		    		if (Thread.interrupted())
		    			return;
					mChannel.write(msg);
				}
				catch (EOFException e)
				{
					if (I) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed");
					if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed",e);
					mHandler.channelDisconnected(e);
				}
				catch (IOException e)
				{
					if (D) Log.d(TAG_CLIENT_BIND,PREFIX_LOG+"ReadThread",e);
					mHandler.channelDisconnected(e);
				}
				catch (InterruptedException e)
				{
					if (D) Log.d(TAG_CLIENT_BIND,PREFIX_LOG+"ReadThread",e);
					mHandler.channelDisconnected(e);
					return;
				}
	    	}
	    }
    }
    class ReadThread implements Runnable
    {
    	@Override
    	public void run()
    	{
	    	Looper.prepare();
	    	Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    		for (;;)
    		{
				try
				{
		    		if (Thread.interrupted())
		    			return;
	    			Msg msg = mChannel.read();
	    			mHandler.messageReceived(msg);
				}
				catch (EOFException e)
				{
					if (I) Log.i(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed");
					if (V) Log.v(TAG_CLIENT_BIND,PREFIX_LOG+"Socket closed",e);
					mHandler.channelDisconnected(e);
					return;
				}
				catch (IOException e)
				{
					if (D) Log.d(TAG_CLIENT_BIND,PREFIX_LOG+"ReadThread",e);
					mHandler.channelDisconnected(e);
					return;
				}
    		}
    	}
    }
   
}
