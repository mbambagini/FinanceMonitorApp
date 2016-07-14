package financemonitor.org.financemonitor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    //databases
    public static final String TABLE_MOVE  = "FINANCE";
    public static final String TABLE_ENTRY = "CATEGORY";

    //columns
    public static final String COLUMN_MOVE_ID = "_id";
    public static final String COLUMN_MOVE_AMOUNT = "amount";
    public static final String COLUMN_MOVE_CURRENCY = "currency";
    public static final String COLUMN_MOVE_WHEN = "execution_date";
    public static final String COLUMN_MOVE_ID_ENTRY = "id_entry";

    public static final String COLUMN_ENTRY_ID = "_id";
    public static final String COLUMN_ENTRY_ENTRY = "entry";
    public static final String COLUMN_ENTRY_CATEGORY = "category";

    //creation queries
    private static final String DATABASE_CREATE_MOVE = "create table "
            + TABLE_MOVE + "("
            + COLUMN_MOVE_ID + " integer primary key autoincrement, "
            + COLUMN_MOVE_AMOUNT + " float,"
            + COLUMN_MOVE_CURRENCY + " integer,"
            + COLUMN_MOVE_WHEN + " integer,"
            + COLUMN_MOVE_ID_ENTRY + " integer);";

    private static final String DATABASE_CREATE_ENTRY = "create table "
            + TABLE_ENTRY + "("
            + COLUMN_ENTRY_ID + " integer primary key, "
            + COLUMN_ENTRY_ENTRY + " test,"
            + COLUMN_ENTRY_CATEGORY + " text);";

    //version
    private static final int DATABASE_VERSION = 2;
    //file
    private static final String DATABASE_NAME = "finance.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_MOVE);
        database.execSQL(DATABASE_CREATE_ENTRY);
        ContentValues values = new ContentValues();
        database.insert(DBHelper.TABLE_ENTRY, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRY);
        onCreate(db);
    }
}
