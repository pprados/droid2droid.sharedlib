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

import org.droid2droid.internal.RemoteAndroidInfoImpl;
import android.nfc.NdefMessage;
interface IRemoteAndroidManager
{

	RemoteAndroidInfoImpl getInfo();
	void startDiscover(int flags,long timeToDiscover);
	void cancelDiscover();
	boolean isDiscovering();
	
	List<RemoteAndroidInfoImpl> getBondedDevices();
	boolean isBonded(in RemoteAndroidInfoImpl info);
	long getCookie(int flags,String uri);
	void removeCookie(String uri);
	NdefMessage createNdefMessage();
	void setLog(int type,boolean state);
}