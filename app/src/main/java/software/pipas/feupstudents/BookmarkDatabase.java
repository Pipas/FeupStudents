package software.pipas.feupstudents;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class BookmarkDatabase extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BookmarkDB";
    private static final String TABLE_BOOKMARKS = "bookmarks";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_URL = "url";

    private static final String[] COLUMNS = {KEY_ID,KEY_TITLE,KEY_URL};

    public BookmarkDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_BOOK_TABLE = "CREATE TABLE "+ TABLE_BOOKMARKS + " ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, "+
                "url TEXT )";

        db.execSQL(CREATE_BOOK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS books");

        this.onCreate(db);
    }

    public long addBookmark(Bookmark bookmark)
    {
        Log.d("FEUPDEBUG", "Added bookmark: " + bookmark.toString());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, bookmark.getTitle());
        values.put(KEY_URL, bookmark.getUrl());

        long id = db.insert(TABLE_BOOKMARKS, null, values);

        db.close();

        return id;
    }

    public Bookmark getBookmark(int id)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKMARKS, COLUMNS, " id = ?", new String[] { String.valueOf(id) },null,null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Bookmark bookmark = new Bookmark();
        bookmark.setId(Integer.parseInt(cursor.getString(0)));
        bookmark.setTitle(cursor.getString(1));
        bookmark.setUrl(cursor.getString(2));

        Log.d("FEUPDEBUG", "Fetched bookmark: " + bookmark.toString());

        return bookmark;
    }

    public ArrayList<Bookmark> getAllBookmarks()
    {
        ArrayList<Bookmark> bookmarks = new ArrayList<Bookmark>();

        String query = "SELECT  * FROM " + TABLE_BOOKMARKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Bookmark bookmark = null;
        if (cursor.moveToFirst())
        {
            do
            {
                bookmark = new Bookmark();
                bookmark.setId(Integer.parseInt(cursor.getString(0)));
                bookmark.setTitle(cursor.getString(1));
                bookmark.setUrl(cursor.getString(2));

                bookmarks.add(bookmark);
            }
            while (cursor.moveToNext());
        }

        Log.d("FEUPDEBUG", "Fetched all bookmarks: " + bookmarks.toString());

        return bookmarks;
    }

    public void deleteBookmark(Bookmark bookmark)
    {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_BOOKMARKS, KEY_ID+" = ?", new String[] { String.valueOf(bookmark.getId()) });

        db.close();

        Log.d("FEUPDEBUG", "Deleted bookmark: " + bookmark.toString());
    }
}

