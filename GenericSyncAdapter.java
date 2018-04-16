package com.cardinal.mda.SyncAdapters;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.android.volley.Request;
import com.cardinal.mda.Api.NetworkResponse;
import com.cardinal.mda.Interfaces.IDTO;
import com.cardinal.mda.Models.DatabaseContracts.BaseDatabaseContract;
import com.cardinal.mda.Models.Google.AccountGeneral;
import com.cardinal.mda.util.GsonRequest;
import com.cardinal.mda.util.LinqList;
import com.cardinal.mda.util.Mapper;
import com.cardinal.mda.util.NetworkHelper;
import com.cardinal.mda.util.RequestHandler;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import io.realm.RealmObject;

/**
 * Created by Chris Scherer on 4/11/18.
 */
public class GenericSyncAdapter<T extends RealmObject & IDTO> extends AbstractThreadedSyncAdapter {
	private static final String TAG =  "GenericSyncAdapter";
	private Class<T> clazz;
	private T dummy;

	private final ContentResolver resolver;

	public GenericSyncAdapter(Context context, boolean autoInitialize, Class<T> clazz, T dummy) {
		this(context, autoInitialize, false, clazz, dummy);
	}

	public GenericSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs, Class<T> clazz, T dummy) {
		super(context, autoInitialize, allowParallelSyncs);
		this.resolver = context.getContentResolver();
		this.clazz = clazz;
		this.dummy = dummy;
	}

	@Override
	public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
		Log.w(TAG, "Starting Sync...");

		try {
			syncData(syncResult);
		} catch (Exception ignored){}
	}

	private void syncData(SyncResult syncResult) throws IOException, JSONException, RemoteException, OperationApplicationException {
		final String endpoint = "";

		Log.i(TAG, "Fetching server entries...");

		GsonRequest<Void, NetworkResponse<T[]>> request = new GsonRequest<>(
				Request.Method.GET,
				endpoint,
				null,
				new TypeToken<NetworkResponse<T[]>>(){}.getType(),
				NetworkHelper.setupBaseHeaders(),
				response -> onSyncSuccess(response, syncResult),
				e -> onSyncFailure()
		);
		RequestHandler.getInstance(getContext()).addToRequestQueue(request);
	}

	private void onSyncSuccess(NetworkResponse<T[]> response, SyncResult syncResult) {
		LinqList<T> networkEntries = new LinqList<>(clazz, Arrays.asList(response.getPayload()));
		ArrayList<ContentProviderOperation> batch = new ArrayList<>();


		Log.i(TAG, "Fetching Local entries");
		Cursor c = resolver.query(BaseDatabaseContract.getUriForTable(clazz.getName()), null, null, null, null, null);
		assert c != null;
		c.moveToFirst();

		for(int i=0;i<c.getCount(); i++) {
			syncResult.stats.numEntries++;

			T localObject = Mapper.parseFromCursor(clazz, c);
			T networkObject = networkEntries.where(dummy.getPrimaryKeyName(), localObject.getId().toString()).first();

			if(networkObject != null) {
				try {
					networkEntries.remove(networkObject);
					boolean needsUpdate = false;

					for(Field f : localObject.getClazz().getDeclaredFields()) {
						if(needsUpdate) continue;
						if(!Objects.equals(f.get(localObject), f.get(networkObject))) {
							needsUpdate = true;
						}
					}

					if(needsUpdate) {
						ContentProviderOperation.Builder hold = ContentProviderOperation.newUpdate(BaseDatabaseContract.getUriForTable(clazz.getName()))
								.withSelection(localObject.getPrimaryKeyName() + "='" + localObject.getId() + "'", null);

						for(Field f : localObject.getClazz().getDeclaredFields()) {
							if(f.getName().equals(localObject.getPrimaryKeyName())) continue;

							hold.withValue(f.getName(), f.get(networkObject));
						}

						hold.build();
						syncResult.stats.numUpdates++;
					}
				} catch(Exception ignored) {

				}
			} else {
				batch.add(ContentProviderOperation.newDelete(BaseDatabaseContract.getUriForTable(clazz.getName()))
						.withSelection(localObject.getPrimaryKeyName() + "='" + localObject.getId() + "'", null)
						.build());

				syncResult.stats.numDeletes++;
			}
			c.moveToNext();
		}
		c.close();

		for(T networkObject : networkEntries) {
			try {
				ContentProviderOperation.Builder temp = ContentProviderOperation.newInsert(BaseDatabaseContract.getUriForTable(clazz.getName()));
				for(Field f : networkObject.getClazz().getDeclaredFields()) {
					temp.withValue(f.getName(), f.get(networkObject));
				}
				syncResult.stats.numInserts++;
			} catch(Exception ignored) {}

			try {
				resolver.applyBatch(BaseDatabaseContract.CONTENT_AUTHORITY, batch);
				resolver.notifyChange(BaseDatabaseContract.getUriForTable(clazz.getName()), null, false);
			}catch(Exception ignored) {}
		}
	}

	private void onSyncFailure() {

	}

	public static void performSync() {
		Bundle b = new Bundle();
		b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		ContentResolver.requestSync(AccountGeneral.getAccount(), BaseDatabaseContract.CONTENT_AUTHORITY, b);
	}
}
