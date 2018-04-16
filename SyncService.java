package com.cardinal.mda.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cardinal.mda.Interfaces.IDTO;
import com.cardinal.mda.SyncAdapters.GenericSyncAdapter;

import io.realm.RealmObject;

/**
 * Created by Chris Scherer on 4/11/18.
 */
public class SyncService<T extends RealmObject & IDTO> extends Service {

	private static final Object LOCK = new Object();
	private static GenericSyncAdapter syncAdapter;
	private Class<T> clazz;
	private T dummy;

	public SyncService() { }

	public SyncService(Class<T> clazz, T dummy) {
		this.clazz = clazz;
		this.dummy = dummy;
	}

	@Override
	public void onCreate() {
		if(clazz == null || dummy == null) return;
		synchronized (LOCK) {
			syncAdapter = new GenericSyncAdapter<>(this, false, clazz, dummy);
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return syncAdapter.getSyncAdapterBinder();
	}

	public void setSyncServiceTypes(Class<T> clazz, T dummy) {
		this.clazz = clazz;
		this.dummy = dummy;

		synchronized (LOCK) {
			syncAdapter = new GenericSyncAdapter<>(this, false, clazz, dummy);
		}
	}
}