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

import static org.droid2droid.Droid2DroidManager.ACTION_DISCOVER_ANDROID;
import static org.droid2droid.Droid2DroidManager.ACTION_START_DISCOVER_ANDROID;
import static org.droid2droid.Droid2DroidManager.ACTION_STOP_DISCOVER_ANDROID;
import static org.droid2droid.Droid2DroidManager.EXTRA_DISCOVER;
import static org.droid2droid.Droid2DroidManager.PERMISSION_DISCOVER_SEND;
import static org.droid2droid.internal.Constants.D;
import static org.droid2droid.internal.Constants.PREFIX_LOG;
import static org.droid2droid.internal.Constants.TAG_CLIENT_BIND;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.droid2droid.ListRemoteAndroidInfo;
import org.droid2droid.RemoteAndroidInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;


// FIXME: a laiser dans sharedlib ?
public final class ListRemoteAndroidInfoImpl implements ListRemoteAndroidInfo, Closeable
{
	private final Object mutex=this;
	private final ArrayList<RemoteAndroidInfo> mDiscoveredAndroid=new ArrayList<RemoteAndroidInfo>();
	private final Context mContext;
	private DiscoverListener mCallBack;

	private BroadcastReceiver mReceiver=new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action=intent.getAction();
			if (ACTION_DISCOVER_ANDROID.equals(action))
			{
				RemoteAndroidInfoImpl info=(RemoteAndroidInfoImpl)intent.getParcelableExtra(EXTRA_DISCOVER);
				detectAndroid(info);
			}
			else if (ACTION_START_DISCOVER_ANDROID.equals(action))
			{
				processOnStart();
			}
			else if (ACTION_STOP_DISCOVER_ANDROID.equals(action))
			{
				processOnStop();
			}
		}

	};

	public ListRemoteAndroidInfoImpl(Context context)
	{
		this(context,null);
	}
	public ListRemoteAndroidInfoImpl(Context context,DiscoverListener callback)
	{
		mCallBack=callback;
		mContext=context;
		IntentFilter filter=new IntentFilter();
		filter.addAction(ACTION_DISCOVER_ANDROID);
		filter.addAction(ACTION_START_DISCOVER_ANDROID);
		filter.addAction(ACTION_STOP_DISCOVER_ANDROID);
		context.registerReceiver(mReceiver, 
				filter,
				PERMISSION_DISCOVER_SEND,null
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
		if (mReceiver!=null)
			mContext.unregisterReceiver(mReceiver);
		mReceiver=null;
		mCallBack=null;
	}
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
		close();
	}
	
	synchronized private void detectAndroid(RemoteAndroidInfoImpl android)
	{
		if (D) Log.d(TAG_CLIENT_BIND,PREFIX_LOG+"Discover "+android);
		final DiscoverListener callBack=mCallBack;
		for (RemoteAndroidInfo a:mDiscoveredAndroid)
		{
			RemoteAndroidInfoImpl and=(RemoteAndroidInfoImpl)a;
			if (and.uuid.equals(android.uuid))
			{
				and.merge(android);
				if (callBack!=null)
					callBack.onDiscover(android, true); // FIXME: gérer le flag update
				return;
			}
		}
		mDiscoveredAndroid.add(android);
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
	
	@Override
	public boolean add(RemoteAndroidInfo object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.add(object); }
	}
	@Override
	public void add(int index, RemoteAndroidInfo object)
	{
		synchronized(mutex) { mDiscoveredAndroid.add(index, object); }
	}
	@Override
	public boolean containsAll(Collection<?> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.containsAll(collection); }
	}
	@Override
	public boolean addAll(Collection<? extends RemoteAndroidInfo> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.addAll(collection); }
	}
	@Override
	public boolean addAll(int index, Collection<? extends RemoteAndroidInfo> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.addAll(index, collection); }
	}
	@Override
	public void clear()
	{
		synchronized(mutex) { mDiscoveredAndroid.clear(); }
	}
	@Override
	public Object clone()
	{
		synchronized(mutex) { return mDiscoveredAndroid.clone(); }
	}
	@Override
	public boolean removeAll(Collection<?> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.removeAll(collection); }
	}
	public void ensureCapacity(int minimumCapacity)
	{
		synchronized(mutex) { mDiscoveredAndroid.ensureCapacity(minimumCapacity); }
	}
	@Override
	public RemoteAndroidInfo get(int index)
	{
		synchronized(mutex) { return mDiscoveredAndroid.get(index); }
	}
	@Override
	public int size()
	{
		synchronized(mutex) { return mDiscoveredAndroid.size(); }
	}
	@Override
	public boolean isEmpty()
	{
		synchronized(mutex) { return mDiscoveredAndroid.isEmpty(); }
	}
	@Override
	public boolean contains(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.contains(object); }
	}
	@Override
	public boolean retainAll(Collection<?> collection)
	{
		synchronized(mutex) { return mDiscoveredAndroid.retainAll(collection); }
	}
	@Override
	public int indexOf(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.indexOf(object); }
	}
	@Override
	public int lastIndexOf(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.lastIndexOf(object); }
	}
	@Override
	public RemoteAndroidInfo remove(int index)
	{
		synchronized(mutex) { return mDiscoveredAndroid.remove(index); }
	}
	@Override
	public boolean remove(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.remove(object); }
	}
	@Override
	public String toString()
	{
		synchronized(mutex) { return mDiscoveredAndroid.toString(); }
	}
	@Override
	public boolean equals(Object object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.equals(object); }
	}
	@Override
	public RemoteAndroidInfo set(int index, RemoteAndroidInfo object)
	{
		synchronized(mutex) { return mDiscoveredAndroid.set(index, object); }
	}
	@Override
	public Object[] toArray()
	{
		synchronized(mutex) { return mDiscoveredAndroid.toArray(); }
	}
	@Override
	public <T> T[] toArray(T[] contents)
	{
		synchronized(mutex) { return mDiscoveredAndroid.toArray(contents); }
	}
	@Override
	public int hashCode()
	{
		synchronized(mutex) { return mDiscoveredAndroid.hashCode(); }
	}
	public void trimToSize()
	{
		synchronized(mutex) { mDiscoveredAndroid.trimToSize(); }
	}
	@Override
	public Iterator<RemoteAndroidInfo> iterator()
	{
		synchronized(mutex) { return mDiscoveredAndroid.iterator(); }
	}
	@Override
	public ListIterator<RemoteAndroidInfo> listIterator()
	{
		synchronized(mutex) { return mDiscoveredAndroid.listIterator(); }
	}
	@Override
	public ListIterator<RemoteAndroidInfo> listIterator(int location)
	{
		synchronized(mutex) { return mDiscoveredAndroid.listIterator(location); }
	}
	@Override
	public List<RemoteAndroidInfo> subList(int start, int end)
	{
		synchronized(mutex) { return mDiscoveredAndroid.subList(start, end); }
	}
	
}
