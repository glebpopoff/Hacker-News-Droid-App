package com.glebpopov.hackernews.util;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.glebpopov.hackernews.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseUtil extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "hackernews_db";
    
    protected Context context;
    
    public DatabaseUtil(Context context) {
            super(context, DATABASE_NAME, null, 1);
            this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
            String s;
            try {
                    InputStream in = context.getResources().openRawResource(R.raw.sql);
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.parse(in, null);
                    NodeList statements = doc.getElementsByTagName("statement");
                    for (int i=0; i<statements.getLength(); i++) {
                            s = statements.item(i).getChildNodes().item(0).getNodeValue();
                            db.execSQL(s);
                    }
            } catch (Throwable t) {
                    Toast.makeText(context, t.toString(), 50000).show();
            }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //db.execSQL("DROP TABLE IF EXISTS hackernews_saved");
            //onCreate(db);
    }
    
}
