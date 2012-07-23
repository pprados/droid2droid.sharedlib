package org.remoteandroid.internal;

import static org.remoteandroid.internal.Constants.TAG_RA;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

public final class NormalizeIntent
{
	public static void writeIntent(Intent intent, Parcel out, int flags)
	{
		out.writeString(intent.getAction());
		Uri uri = intent.getData();
		if (uri == null)
			out.writeString(null);
		else
			out.writeString(uri.toString());
		out.writeString(intent.getType());
		out.writeInt(intent.getFlags());
		out.writeString(intent.getPackage());
		ComponentName componentName = intent.getComponent();
		if (componentName == null)
		{
			out.writeString(null);
			out.writeString(null);
		}
		else
		{
			out.writeString(componentName.getPackageName());
			out.writeString(componentName.getClassName());
		}
		Set<String> categories = intent.getCategories();
		if (categories != null)
		{
			out.writeInt(categories.size());
			for (String category : categories)
			{
				out.writeString(category);
			}
		}
		else
		{
			out.writeInt(0);
		}
		Bundle bundle = intent.getExtras();
		writeBundle(out,bundle);
	}

	public static Intent readIntent(Parcel in)
	{
		Intent intent = new Intent();
		intent.setAction(in.readString());
		String data = in.readString();
		if (data != null)
			intent.setData(Uri.parse(data));
		intent.setType(in.readString());
		intent.setFlags(in.readInt());
		String packageName = in.readString();
		intent.setPackage(packageName);
		packageName = in.readString();
		String className = in.readString();
		if (packageName != null)
			intent.setComponent(new ComponentName(packageName, className));
		int size = in.readInt();
		for (int i = 0; i < size; ++i)
		{
			intent.addCategory(in.readString());
		}
		Bundle bundle=readBundle(in,Intent.class.getClassLoader());
		if (bundle.size()!=0)
		{
			intent.replaceExtras(bundle);
		}
		return intent;
	}

	private static final int VAL_NULL = -1;

	private static final int VAL_STRING = 0;

	private static final int VAL_INTEGER = 1;

	private static final int VAL_MAP = 2;

	private static final int VAL_BUNDLE = 3;

	private static final int VAL_PARCELABLE = 4;

	private static final int VAL_SHORT = 5;

	private static final int VAL_LONG = 6;

	private static final int VAL_FLOAT = 7;

	private static final int VAL_DOUBLE = 8;

	private static final int VAL_BOOLEAN = 9;

	private static final int VAL_CHARSEQUENCE = 10;

	private static final int VAL_LIST = 11;

	private static final int VAL_SPARSEARRAY = 12;

	private static final int VAL_BYTEARRAY = 13;

	private static final int VAL_STRINGARRAY = 14;

	private static final int VAL_IBINDER = 15;

	private static final int VAL_PARCELABLEARRAY = 16;

	private static final int VAL_OBJECTARRAY = 17;

	private static final int VAL_INTARRAY = 18;

	private static final int VAL_LONGARRAY = 19;

	private static final int VAL_BYTE = 20;

	private static final int VAL_SERIALIZABLE = 21;

	private static final int VAL_SPARSEBOOLEANARRAY = 22;

	private static final int VAL_BOOLEANARRAY = 23;

	
	private static void writeBundle(Parcel out,Bundle bundle)
	{
		if (bundle != null)
		{
			out.writeInt(bundle.size());
			for (String key : bundle.keySet())
			{
				out.writeString(key);
				writeValue(
					out, bundle.get(key));
			}
		}
		else
			out.writeInt(0);
	}
	private static Bundle readBundle(Parcel in,ClassLoader loader)
	{
		Bundle bundle=new Bundle();
		int size = in.readInt();
		if (size != 0)
		{
			for (int i = 0; i < size; ++i)
			{
				String key = in.readString();
				readValue(in, loader);
			}
		}
		return bundle;
	}
	private static void writeValue(Parcel out, Object v)
	{
		if (v == null)
		{
			out.writeInt(VAL_NULL);
		}
		else if (v instanceof String)
		{
			out.writeInt(VAL_STRING);
			out.writeString((String) v);
		}
		else if (v instanceof Integer)
		{
			out.writeInt(VAL_INTEGER);
			out.writeInt((Integer) v);
		}
		else if (v instanceof Map<?, ?>)
		{
			out.writeInt(VAL_MAP);
			out.writeMap((Map<?, ?>) v);
		}
		else if (v instanceof Bundle)
		{
			// Must be before Parcelable
			out.writeInt(VAL_BUNDLE);
			writeBundle(out,(Bundle) v);
		}
		else if (v instanceof Parcelable)
		{
			Log.w(TAG_RA,"Use a parcelable instance present a risk to be incompatible with differents versions of Androids. Use Serializable objects.");
			out.writeInt(VAL_PARCELABLE);
			out.writeParcelable(
				(Parcelable) v, 0);
		}
		else if (v instanceof Short)
		{
			out.writeInt(VAL_SHORT);
			out.writeInt(((Short) v).intValue());
		}
		else if (v instanceof Long)
		{
			out.writeInt(VAL_LONG);
			out.writeLong((Long) v);
		}
		else if (v instanceof Float)
		{
			out.writeInt(VAL_FLOAT);
			out.writeFloat((Float) v);
		}
		else if (v instanceof Double)
		{
			out.writeInt(VAL_DOUBLE);
			out.writeDouble((Double) v);
		}
		else if (v instanceof Boolean)
		{
			out.writeInt(VAL_BOOLEAN);
			out.writeInt((Boolean) v ? 1 : 0);
		}
		else if (v instanceof CharSequence)
		{
			// Must be after String
			out.writeInt(VAL_CHARSEQUENCE);
			out.writeString(v.toString());
		}
		else if (v instanceof List<?>)
		{
			out.writeInt(VAL_LIST);
			out.writeList((List<?>) v);
		}
		else if (v instanceof SparseArray<?>)
		{
			out.writeInt(VAL_SPARSEARRAY);
			out.writeSparseArray((SparseArray<Object>) v);
		}
		else if (v instanceof boolean[])
		{
			out.writeInt(VAL_BOOLEANARRAY);
			out.writeBooleanArray((boolean[]) v);
		}
		else if (v instanceof byte[])
		{
			out.writeInt(VAL_BYTEARRAY);
			out.writeByteArray((byte[]) v);
		}
		else if (v instanceof String[])
		{
			out.writeInt(VAL_STRINGARRAY);
			out.writeStringArray((String[]) v);
		}
		else if (v instanceof IBinder)
		{
			Log.e(TAG_RA,"It's not possible to send a Binder");
			throw new IllegalArgumentException("It's not possible to send a Binder");
//			out.writeInt(VAL_IBINDER);
//			out.writeStrongBinder((IBinder) v);
		}
		else if (v instanceof Parcelable[])
		{
			Log.w(TAG_RA,"Use a parcelable instance present a risk to be incompatible with differents versions of Androids. Use Serializable objects.");
			out.writeInt(VAL_PARCELABLEARRAY);
			out.writeParcelableArray(
				(Parcelable[]) v, 0);
		}
		else if (v instanceof Object[])
		{
			out.writeInt(VAL_OBJECTARRAY);
			out.writeArray((Object[]) v);
		}
		else if (v instanceof int[])
		{
			out.writeInt(VAL_INTARRAY);
			out.writeIntArray((int[]) v);
		}
		else if (v instanceof long[])
		{
			out.writeInt(VAL_LONGARRAY);
			out.writeLongArray((long[]) v);
		}
		else if (v instanceof Byte)
		{
			out.writeInt(VAL_BYTE);
			out.writeInt((Byte) v);
		}
		else if (v instanceof Serializable)
		{
			// Must be last
			out.writeInt(VAL_SERIALIZABLE);
			out.writeSerializable((Serializable) v);
		}
		else
		{
			throw new RuntimeException("Parcel: unable to marshal value " + v);
		}
	}

	/**
	 * Read a typed object from a parcel. The given class loader will be used to
	 * load any enclosed Parcelables. If it is null, the default class loader
	 * will be used.
	 */
	private final static Object readValue(Parcel in, ClassLoader loader)
	{
		int type = in.readInt();

		switch (type)
		{
			case VAL_NULL:
				return null;

			case VAL_STRING:
				return in.readString();

			case VAL_INTEGER:
				return in.readInt();

			case VAL_MAP:
				return in.readHashMap(loader);

			case VAL_PARCELABLE:
				Log.w(TAG_RA,"Use a parcelable instance present a risk to be incompatible with differents versions of Androids. Use Serializable objects.");
				return in.readParcelable(loader);

			case VAL_SHORT:
				return (short) in.readInt();

			case VAL_LONG:
				return in.readLong();

			case VAL_FLOAT:
				return in.readFloat();

			case VAL_DOUBLE:
				return in.readDouble();

			case VAL_BOOLEAN:
				return in.readInt() == 1;

			case VAL_CHARSEQUENCE:
				return in.readString();

			case VAL_LIST:
				return in.readArrayList(loader);

			case VAL_BOOLEANARRAY:
				return in.createBooleanArray();

			case VAL_BYTEARRAY:
				return in.createByteArray();

			case VAL_STRINGARRAY:
				return in.createStringArray();

			case VAL_IBINDER:
				throw new IllegalArgumentException("It's not possible to send a Binder");
//				return in.readStrongBinder();

			case VAL_OBJECTARRAY:
				return in.readArray(loader);

			case VAL_INTARRAY:
				return in.createIntArray();

			case VAL_LONGARRAY:
				return in.createLongArray();

			case VAL_BYTE:
				return in.readByte();

			case VAL_SERIALIZABLE:
				return in.readSerializable();

			case VAL_PARCELABLEARRAY:
				Log.w(TAG_RA,"Use a parcelable instance present a risk to be incompatible with differents versions of Androids. Use Serializable objects.");
				return in.readParcelableArray(loader);

			case VAL_SPARSEARRAY:
				return in.readSparseArray(loader);

			case VAL_SPARSEBOOLEANARRAY:
				return in.readSparseBooleanArray();

			case VAL_BUNDLE:
				return readBundle(in,loader); // loading will be deferred

			default:
				throw new RuntimeException("Parcel " + in + ": Unmarshalling unknown type code " + type + " at offset "
						+ (in.dataPosition() - 4));
		}
	}

}
