package com.cardinal.mda.DataProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cardinal.mda.Enums.ProviderTable;
import com.cardinal.mda.Interfaces.IDTO;
import com.cardinal.mda.Models.DatabaseContracts.BaseDatabaseContract;
import com.cardinal.mda.Models.DatabaseContracts.ContainerDatabaseContract;
import com.cardinal.mda.util.Mapper;
import com.cardinal.mda.util.RealmService;

import java.lang.reflect.Field;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Chris Scherer on 4/10/18.
 */

public class BaseContentProvider<T extends RealmObject & IDTO> extends ContentProvider {

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	public RealmService realmService;
	public Class<T> clazz;
	public T dummy;

	static {
		sUriMatcher.addURI(ContainerDatabaseContract.CONTENT_AUTHORITY, ContainerDatabaseContract.TABLE_CONTAINERS, ProviderTable.CONTAINER.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "container_data", ProviderTable.CONTAINERDATA.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "employees", ProviderTable.EMPLOYEE.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "facilities", ProviderTable.FACILITY.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "gpsdata", ProviderTable.GPSDATA.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "locations", ProviderTable.LOCATION.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "pickup_dropoffs", ProviderTable.PICKUPDROPOFF.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "routes", ProviderTable.ROUTE.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "stops", ProviderTable.STOP.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "transfers", ProviderTable.TRANSFER.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "users", ProviderTable.USER.ordinal());
		sUriMatcher.addURI("com.cardinal.mda.provider", "vehicles", ProviderTable.VEHICLE.ordinal());
	}

	public void setFields(RealmService rs, Class<T> clazz, T dummy) {
		this.realmService = rs;
		this.clazz = clazz;
		this.dummy = dummy;
	}

	@Override
	public boolean onCreate() {
		Realm.init(this.getContext());
		realmService = RealmService.getInstance();
		return true;
	}

	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
		MatrixCursor cursor = new MatrixCursor(new String[]{});

		try {
			RealmResults<T> results = realmService.getRealmObjects(clazz).findAll();

			for(T obj : results) {
				Object[] rowData = {};
				int count = 0;
				for(Field f : clazz.getDeclaredFields()) {
					rowData[count] = f.get(obj);
					count++;
				}
				cursor.addRow(rowData);
			}
		}catch(Exception ignored) {}

		return cursor;
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		return null;
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
		Uri returnUri;

		try {
			Number currId = realmService.getRealmObjects(clazz).findAll().max(dummy.getPrimaryKeyName());
			Integer nextId = (currId == null) ? 1 : currId.intValue() + 1;

			T newObj = Mapper.parseFromContentValues(clazz, contentValues);
			newObj.setId(nextId.longValue());
			realmService.addOrUpdateRealmObject(newObj);
		} catch(Exception ignored) {}
		returnUri = ContentUris.withAppendedId(BaseDatabaseContract.getUriForTable(clazz.getName()), '1');

		getContext().getContentResolver().notifyChange(uri, null);

		return returnUri;
	}

	@Override
	public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
		int count = 0;

		s = (s == null) ? "1" : s;
		RealmResults<T> results = realmService.getRealm().where(clazz).equalTo(s, Integer.parseInt(strings[0])).findAll();
		results.deleteAllFromRealm();
		count++;

		if(count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
		int nrUpdated = 0;

		try {
			Integer id = Integer.parseInt(uri.getPathSegments().get(1));
			T obj = realmService.getRealm().where(clazz).equalTo(dummy.getPrimaryKeyName(), id).findFirst();

			for(Field f : clazz.getDeclaredFields()) {
				f.set(obj, contentValues.get(f.getName()));
			}

			nrUpdated++;
		}catch(Exception ignored) {}

		if(nrUpdated != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return nrUpdated;
	}
}
