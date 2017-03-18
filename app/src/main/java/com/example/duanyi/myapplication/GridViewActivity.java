package com.example.duanyi.myapplication;

/**
 * Created by chrissie on 2/27/17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class GridViewActivity extends AppCompatActivity {
    SQLiteExampleee mSQLiteExample;
    Button mSQLSubmitButton;
    Cursor mSQLCursor;
    SimpleCursorAdapter mSQLCursorAdapter;
    private static final String TAG = "SQLActivity";
    SQLiteDatabase mSQLDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relative);

        mSQLiteExample = new SQLiteExampleee(this);
        mSQLDB = mSQLiteExample.getWritableDatabase();

        mSQLSubmitButton = (Button) findViewById(R.id.sql_add_row_button);
        mSQLSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSQLDB != null){
                    ContentValues vals = new ContentValues();
                    vals.put(DBContracttt.DemoTable.COLUMN_NAME_DEMO_STRING, ((EditText)findViewById(R.id.sql_text_input)).getText().toString());
                    vals.put(DBContracttt.DemoTable.COLUMN_NAME_DEMO_INT, ((EditText)findViewById(R.id.sql_int_input)).getText().toString());
                    mSQLDB.insert(DBContracttt.DemoTable.TABLE_NAME,null,vals);
                    populateTable();
                } else {
                    Log.d(TAG, "Unable to access database for writing.");
                }
            }
        });

        populateTable();
    }

    private void populateTable(){
        if(mSQLDB != null) {
            try {
                if(mSQLCursorAdapter != null && mSQLCursorAdapter.getCursor() != null){
                    if(!mSQLCursorAdapter.getCursor().isClosed()){
                        mSQLCursorAdapter.getCursor().close();
                    }
                }
                mSQLCursor = mSQLDB.query(DBContracttt.DemoTable.TABLE_NAME,
                        new String[]{DBContracttt.DemoTable._ID, DBContracttt.DemoTable.COLUMN_NAME_DEMO_STRING,
                                DBContracttt.DemoTable.COLUMN_NAME_DEMO_INT}, DBContracttt.DemoTable.COLUMN_NAME_DEMO_INT + " > ?", new String[]{"100"}, null, null, null);
                ListView SQLListView = (ListView) findViewById(R.id.sql_list_view);
                mSQLCursorAdapter = new SimpleCursorAdapter(this,
                        R.layout.sql,
                        mSQLCursor,
                        new String[]{DBContracttt.DemoTable.COLUMN_NAME_DEMO_STRING, DBContracttt.DemoTable.COLUMN_NAME_DEMO_INT},
                        new int[]{R.id.sql_listview_string, R.id.sql_listview_int},
                        0);
                SQLListView.setAdapter(mSQLCursorAdapter);
            } catch (Exception e) {
                Log.d(TAG, "Error loading data from database");
            }
        }
    }
}

class SQLiteExampleee extends SQLiteOpenHelper {

    public SQLiteExampleee(Context context) {
        super(context, DBContracttt.DemoTable.DB_NAME, null, DBContracttt.DemoTable.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContracttt.DemoTable.SQL_CREATE_DEMO_TABLE);

        ContentValues testValues = new ContentValues();
        testValues.put(DBContracttt.DemoTable.COLUMN_NAME_DEMO_INT, 42);
        testValues.put(DBContracttt.DemoTable.COLUMN_NAME_DEMO_STRING, "Hello SQLite");
        db.insert(DBContracttt.DemoTable.TABLE_NAME,null,testValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBContracttt.DemoTable.SQL_DROP_DEMO_TABLE);
        onCreate(db);
    }
}

final class DBContracttt {
    private DBContracttt(){};

    public final class DemoTable implements BaseColumns {
        public static final String DB_NAME = "demo_db";
        public static final String TABLE_NAME = "demo";
        public static final String COLUMN_NAME_DEMO_STRING = "demo_string";
        public static final String COLUMN_NAME_DEMO_INT = "demo_int";
        public static final int DB_VERSION = 4;


        public static final String SQL_CREATE_DEMO_TABLE = "CREATE TABLE " +
                DemoTable.TABLE_NAME + "(" + DemoTable._ID + " INTEGER PRIMARY KEY NOT NULL," +
                DemoTable.COLUMN_NAME_DEMO_STRING + " VARCHAR(255)," +
                DemoTable.COLUMN_NAME_DEMO_INT + " INTEGER);";

        public static final String SQL_TEST_DEMO_TABLE_INSERT = "INSERT INTO " + TABLE_NAME +
                " (" + COLUMN_NAME_DEMO_STRING + "," + COLUMN_NAME_DEMO_INT + ") VALUES ('test', 123);";

        public  static final String SQL_DROP_DEMO_TABLE = "DROP TABLE IF EXISTS " + DemoTable.TABLE_NAME;
    }
}