package financemonitor.org.financemonitor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class FinanceDao {

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public FinanceDao(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    synchronized public long addCategory (CategoryEntry category) {
        long id;
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_ENTRY_ID, category.getIdEntry());
        values.put(DBHelper.COLUMN_ENTRY_ENTRY, category.getEntry());
        values.put(DBHelper.COLUMN_ENTRY_CATEGORY, category.getCategory());

        try {
            id = db.insert(DBHelper.TABLE_ENTRY, null, values);
        } catch (Exception e) {
            return -1;
        }

        return id;
    }

    synchronized public boolean emptyCategories () {
        db.delete(DBHelper.TABLE_ENTRY, null, null);
        return true;
    }

    synchronized public List<CategoryEntry> getCategories () {
        List<CategoryEntry> categories = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + DBHelper.TABLE_ENTRY + "." + DBHelper.COLUMN_ENTRY_ID + ", " +
                                                DBHelper.TABLE_ENTRY + "." + DBHelper.COLUMN_ENTRY_ENTRY + ", " +
                                                DBHelper.TABLE_ENTRY + "." + DBHelper.COLUMN_ENTRY_CATEGORY + " " +
                                    "FROM " + DBHelper.TABLE_ENTRY + " " +
                                    "ORDER BY " + DBHelper.TABLE_ENTRY + "." + DBHelper.COLUMN_ENTRY_CATEGORY + ", " +
                                                  DBHelper.TABLE_ENTRY + "." + DBHelper.COLUMN_ENTRY_ENTRY, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            categories.add(cursorToCategory(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return categories;
    }

    synchronized public long addMove (MoveEntry move) {
        long id;
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_MOVE_AMOUNT, move.getAmount());
        values.put(DBHelper.COLUMN_MOVE_CURRENCY, move.getCurrency());
        values.put(DBHelper.COLUMN_MOVE_ID_ENTRY, move.getCategory().getIdEntry());
        values.put(DBHelper.COLUMN_MOVE_WHEN, move.getWhen().getTime());

        try {
            id = db.insert(DBHelper.TABLE_MOVE, null, values);
        } catch (Exception e) {
            return -1;
        }

        return id;
    }

    synchronized public boolean deleteMove (MoveEntry move) {
        String whereClause = DBHelper.COLUMN_MOVE_ID + " = " + move.getId();
        return db.delete(DBHelper.TABLE_MOVE, whereClause, null) == 1;
    }

    synchronized public ArrayList<MoveEntry> getMoves () {
        ArrayList<MoveEntry> moves = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + DBHelper.TABLE_MOVE + "." + DBHelper.COLUMN_MOVE_ID + ", " +
                                                DBHelper.TABLE_MOVE + "." + DBHelper.COLUMN_MOVE_AMOUNT + ", " +
                                                DBHelper.TABLE_MOVE + "." + DBHelper.COLUMN_MOVE_CURRENCY + ", " +
                                                DBHelper.TABLE_MOVE + "." + DBHelper.COLUMN_MOVE_WHEN + ", " +
                                                DBHelper.TABLE_MOVE + "." + DBHelper.COLUMN_MOVE_ID_ENTRY + ", " +
                                                DBHelper.TABLE_ENTRY + "." + DBHelper.COLUMN_ENTRY_ENTRY + ", " +
                                                DBHelper.TABLE_ENTRY + "." + DBHelper.COLUMN_ENTRY_CATEGORY + " " +
                                    "FROM " + DBHelper.TABLE_MOVE + ", " + DBHelper.TABLE_ENTRY + " " +
                                    "WHERE " + DBHelper.TABLE_MOVE + "." + DBHelper.COLUMN_MOVE_ID_ENTRY + " = " +
                                               DBHelper.TABLE_ENTRY + "." + DBHelper.COLUMN_ENTRY_ID + " " +
                                    "ORDER BY " + DBHelper.TABLE_MOVE + "." + DBHelper.COLUMN_MOVE_WHEN + " DESC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            moves.add(cursorToMove(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return moves;
    }

    private MoveEntry cursorToMove (Cursor cursor) {
        MoveEntry move = new MoveEntry();
        CategoryEntry category = new CategoryEntry();
        move.setId(cursor.getLong(0));
        move.setAmount(cursor.getDouble(1));
        move.setCurrency(cursor.getInt(2));
        move.setWhen(new Date(cursor.getLong(3)));
        category.setIdEntry(cursor.getLong(4));
        category.setEntry(cursor.getString(5));
        category.setCategory(cursor.getString(6));
        move.setCategory(category);
        return move;
    }

    private CategoryEntry cursorToCategory (Cursor cursor) {
        CategoryEntry category = new CategoryEntry();
        category.setIdEntry(cursor.getLong(0));
        category.setEntry(cursor.getString(1));
        category.setCategory(cursor.getString(2));
        return category;
    }

}
