package org.remoteandroid.internal;

import static org.remoteandroid.internal.Constants.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.remoteandroid.ListRemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidInfo;
import org.remoteandroid.RemoteAndroidManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


// FIXME: a laiser dans sharedlib ?
public final class ListRemoteAndroidInfoImpl implements ListRemoteAndroidInfo
{
	private final Object mutex=this;
	private ArrayList<RemoteAndroidInfo> mDiscoveredAndroid=new ArrayList<RemoteAndroidInfo>();
	private WeakReference<RemoteAndroidManager> mManager;
	private DiscoverListener mCallBack;

	private final BroadcastReceiver mReceiver=new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action=intent.getAction();
			if (RemoteAndroidManager.ACTION_DISCOVER_ANDROID.equals(action))
			{
				RemoteAndroidInfoImpl info=(RemoteAndroidInfoImpl)intent.getParcelableExtra(RemoteAndroidManager.EXTRA_DISCOVER);
				detectAndroid(info);
			}
			else if (RemoteAndroidManager.ACTION_START_DISCOVER_ANDROID.equals(action))
			{
				processOnStart();
			}
			else if (RemoteAndroidManager.ACTION_STOP_DISCOVER_ANDROID.equals(action))
			{
				processOnStop();
			}
		}

	};

	public ListRemoteAndroidInfoImpl(RemoteAndroidManager manager)
	{
		this(manager,null);
	}
	public ListRemoteAndroidInfoImpl(RemoteAndroidManager manager,DiscoverListener callback)
	{
		mManager=new WeakReference<RemoteAndroidManager>(manager);
		mCallBack=callback;
		IntentFilter filter=new IntentFilter();
		filter.addAction(RemoteAndroidManager.ACTION_DISCOVER_ANDROID);
		filter.addAction(RemoteAndroidManager.ACTION_START_DISCOVER_ANDROID);
		filter.addAction(RemoteAndroidManager.ACTION_STOP_DISCOVER_ANDROID);
		manager.getContext().registerReceiver(mReceiver, 
				filter,
				RemoteAndroidManager.PERMISSION_DISCOVER_SEND,null
				);
	}
	
	@Override
	public void setListener(DiscoverListener callback)
	{
		mCallBack=callback;
	}
	
	@Override
	public void close()
	{
		cancel();
		RemoteAndroidManager manager=mManager.get();
		if (manager!=null)
		{
			manager.getContext().unregisterReceiver(mReceiver);
		}
		mCallBack=null;
	}
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		try
		{
			RemoteAndroidManager manager=mManager.get();
			if (manager!=null && mReceiver!=null)
				manager.getContext().unregisterReceiver(mReceiver);
		}
		catch (Exception e)
		{
			// Ignore
		}
	}
	
	synchronized private void detectAndroid(RemoteAndroidInfoImpl android)
	{
		if (D) Log.d(TAG_CLIENT_BIND,PREFIX_LOG+"Discover "+android);
		for (RemoteAndroidInfo a:mDiscoveredAndroid)
		{
			RemoteAndroidInfoImpl and=(RemoteAndroidInfoImpl)a;
			if (and.uuid.equals(android.uuid))
			{
				and.merge(android);
				return;
			}
		}
		mDiscoveredAndroid.add(android);
		final DiscoverListener callBack=mCallBack;
		if (callBack!=null)
			callBack.onDiscover(android, false);
	}
	
	private void processOnStart()
	{
		if (mCallBack!=null)
			mCallBack.onDiscoverStart();
	}
	private void processOnStop()
	{
		if (mCallBack!=null)
			mCallBack.onDiscoverStop();
	}
	
	public void start(long timeToDiscover)
	{
		start(0,timeToDiscover);
	}
	public void start(int flag,long timeToDiscover)
	{
		RemoteAndroidManager manager=mManager.get();
		if (manager!=null)
			manager.startDiscover(flag,timeToDiscover);
		else
			Log.w(TAG_RA,"RemoteAndroidManager is disconbected");
	}
	public void cancel()
	{
		RemoteAndroidManager manager=mManager.get();
		if (manager!=null)
			manager.cancelDiscover();
	}

	public boolean add(RemoteAndroidInfo object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.add(object); }
	}
	public void add(int index, RemoteAndroidInfo object)
	{
		synchronized(mutex) { mDiscoveredAndroid.add(index, object); }
	}
	public boolean containsAll(Collection<?> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.containsAll(collection); }
	}
	public boolean addAll(Collection<? extends RemoteAndroidInfo> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.addAll(collection); }
	}
	public boolean addAll(int index, Collection<? extends RemoteAndroidInfo> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.addAll(index, collection); }
	}
	public void clear()
	{
		synchronized(mutex) { mDiscoveredAndroid.clear(); }
	}
	public Object clone()
	{
		synchronized(mutex) { return mDiscoveredAndroid.clone(); }
	}
	public boolean removeAll(Collection<?> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.removeAll(collection); }
	}
	public void ensureCapacity(int minimumCapacity)
	{
		synchronized(mutex) { mDiscoveredAndroid.ensureCapacity(minimumCapacity); }
	}
	public RemoteAndroidInfo get(int index)
	{
		synchronized(mutex) { return mDiscoveredAndroid.get(index); }
	}
	public int size()
	{
		synchronized(mutex) { return mDiscoveredAndroid.size(); }
	}
	public boolean isEmpty()
	{
		synchronized(mutex) { return mDiscoveredAndroid.isEmpty(); }
	}
	public boolean contains(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.contains(object); }
	}
	public boolean retainAll(Collection<?> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.retainAll(collection); }
	}
	public int indexOf(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.indexOf(object); }
	}
	public int lastIndexOf(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.lastIndexOf(object); }
	}
	public RemoteAndroidInfo remove(int index)
	{
		synchronized(mutex) { return mDiscoveredAndroid.remove(index); }
	}
	public boolean remove(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.remove(object); }
	}
	public String toString()
	{
		synchronized(mutex) { return mDiscoveredAndroid.toString(); }
	}
	public boolean equals(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.equals(object); }
	}
	public RemoteAndroidInfo set(int index, RemoteAndroidInfo object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.set(index, object); }
	}
	public Object[] toArray()
	{
		synchronized(mutex) { return mDiscoveredAndroid.toArray(); }
	}
	public <T> T[] toArray(T[] contents)
	{
		synchronized(mutex) { return mDiscoveredAndroid.toArray(contents); }
	}
	public int hashCode()
	{
		synchronized(mutex) { return mDiscoveredAndroid.hashCode(); }
	}
	public void trimToSize()
	{
		synchronized(mutex) { mDiscoveredAndroid.trimToSize(); }
	}
	public Iterator<RemoteAndroidInfo> iterator()
	{
		synchronized(mutex) { return mDiscoveredAndroid.iterator(); }
	}
	public ListIterator<RemoteAndroidInfo> listIterator()
	{
		synchronized(mutex) { return mDiscoveredAndroid.listIterator(); }
	}
	public ListIterator<RemoteAndroidInfo> listIterator(int location)
	{
		synchronized(mutex) { return mDiscoveredAndroid.listIterator(location); }
	}
	public List<RemoteAndroidInfo> subList(int start, int end)
	{
		synchronized(mutex) { return mDiscoveredAndroid.subList(start, end); }
	}
	
}
