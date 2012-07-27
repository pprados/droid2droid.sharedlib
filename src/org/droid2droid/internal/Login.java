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

import java.io.IOException;
import java.net.UnknownHostException;

import org.droid2droid.internal.Messages.Msg;
import org.droid2droid.internal.Messages.Type;

import android.net.Uri;
import android.os.RemoteException;

public abstract class Login
{
	public static Login sLogin;
	public static Login getLogin()
	{
		return sLogin;
	}
	public abstract Pair<RemoteAndroidInfoImpl,Long> client(
		AbstractProtoBufRemoteAndroid android,
		Uri uri,
		Type type,
		int flags,
		long timeout) throws UnknownHostException, IOException, RemoteException;
	public abstract Msg server(Object conContext,Msg req,long cookie,boolean acceptAnonymous);
}
