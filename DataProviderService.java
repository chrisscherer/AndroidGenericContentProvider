/**
 * Created by Chris Scherer on 4/10/18.
 */
public class DataProviderService extends IntentService {

	private static final String TAG = DataProviderService.class.getSimpleName();
	//Intent actions
	public static final String ACTION_INSERT = TAG + ".INSERT";
	public static final String ACTION_UPDATE = TAG + ".UPDATE";
	public static final String ACTION_DELETE = TAG + ".DELETE";

	public static final String EXTRA_VALUES = TAG + ".ContentValues";

	public DataProviderService() {
		super("DataProviderService");
	}

	public static void insert(Context context, ContentValues contentValues, Uri uri) {
		Intent intent = new Intent(context, DataProviderService.class);
		intent.setAction(ACTION_INSERT);
		intent.putExtra(EXTRA_VALUES, contentValues);
		intent.putExtra("uri", uri);
		context.startService(intent);
	}

	public static void update(Context context, Uri uri, ContentValues values) {
		Intent intent = new Intent(context, DataProviderService.class);
		intent.setAction(ACTION_UPDATE);
		intent.setData(uri);
		intent.putExtra(EXTRA_VALUES, values);
		context.startService(intent);
	}

	public static void delete(Context context, Uri uri) {
		Intent intent = new Intent(context, DataProviderService.class);
		intent.setAction(ACTION_DELETE);
		intent.setData(uri);
		context.startService(intent);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if (ACTION_INSERT.equals(intent.getAction())) {
			ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
			Uri uri = intent.getParcelableExtra("uri");
			performInsert(values, uri);
		} else if (ACTION_UPDATE.equals(intent.getAction())) {
			ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
			performUpdate(intent.getData(), values);
		} else if (ACTION_DELETE.equals(intent.getAction())) {
			performDelete(intent.getData());
		}
	}

	private void performInsert(ContentValues values, Uri uri) {
		if (getContentResolver().insert(uri, values) != null) {
			Log.d(TAG, "Inserted new task");
		} else {
			Log.w(TAG, "Error inserting new task");
		}
	}

	private void performUpdate(Uri uri, ContentValues values) {
		int count = getContentResolver().update(uri, values, null, null);
		Log.d(TAG, "Updated " + count + " task items");
	}

	private void performDelete(Uri uri) {
		int count = getContentResolver().delete(uri, null, null);
		Log.d(TAG, "Deleted "+count+" tasks");
	}

}
