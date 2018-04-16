/**
 * Created by Chris Scherer on 4/10/18.
 */
public class ContainerDatabaseContract extends BaseDatabaseContract {
	//Database schema information
	public static final String TABLE_CONTAINERS = "containers";

//	public static final class ContainerColumns implements BaseColumns {
//		public static final String _ID = "containerIdNumber";
//		public static final String CONTAINER_NUMBER = "containerNumber";
//		public static final String PACKING_LIST_NUMBER = "packingListNumber";
//		public static final String ITEM = "item";
//		public static final String TYPE_CODE = "typeCode";
//		public static final String CONTAINER_BAR_CODE = "containerBarCode";
//		public static final String CALL_DISPATCH_INDICATOR = "callDispatchIndicator";
//		public static final String CALL_REASON_CODE = "callReasonCode";
//		public static final String CONTAINER_NOTE_TEXT = "containerNoteText";
//		public static final String ORDER_COUNT = "orderCount";
//		public static final String SIGNATURE_REQUIRED_INDICATOR = "signatureRequiredIndicator";
//		public static final String NOTES_TEXT = "notesText";
//		public static final String STATUS = "status";
//		public static final String DELIVERY_TIMESTAMP = "deliveryTimestamp";
//		public static final String ROW_ADD_TIMESTAMP = "rowAddTimestamp";
//		public static final String ROW_UPDATE_TIMESTAMP = "rowUpdateTimestamp";
//	}

	//Base content Uri for accessing the provider
	public static final Uri CONTENT_URI = new Uri.Builder().scheme("content")
			.authority(CONTENT_AUTHORITY)
			.appendPath(TABLE_CONTAINERS)
			.build();
}
