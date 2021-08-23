package com.app.blogholic.databaseHandler;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.blogholic.Blog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;


public class BlogholicSQLiteHelper extends SQLiteOpenHelper {

    public static final int DB_VER = 1;
    public static final String DB_NAME = "blogholic";

    public BlogholicSQLiteHelper(Context context){
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        manageDatabaseSchema(db, 0, DB_VER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        manageDatabaseSchema(db, oldVersion, newVersion);
    }

    public static void createBlogTable(SQLiteDatabase db){
        String query = "CREATE TABLE blog ("+
                            BlogTable.ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                            BlogTable.TITLE + " TEXT,"+
                            BlogTable.CONTENT + " TEXT,"+
                            BlogTable.ENTRY_DATE + " DATETIME DEFAULT (DATE('now')),"+
                            BlogTable.IMG_RES_PATH+ " TEXT " +
                            ")";
        db.execSQL(query);
    }

    public static void manageDatabaseSchema(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion == 0){
            createBlogTable(db);
        }
    }

    public long insertBlog(String title, String content, String imagePath){
        ContentValues blogData = new ContentValues();
        blogData.put(BlogTable.TITLE, title);
        blogData.put(BlogTable.CONTENT, content);
        blogData.put(BlogTable.IMG_RES_PATH, imagePath);
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(BlogTable.TABLE_NAME, null, blogData);
    }

    public void deleteBlogById(int blogId){
        getWritableDatabase().delete(BlogTable.TABLE_NAME, "_id = ?", new String[]{Integer.toString(blogId)});
    }

    public int updateBlog(int blogId, String title, String content, String imgResPath){
        ContentValues blogValues = new ContentValues();
        blogValues.put(BlogTable.TITLE, title);
        blogValues.put(BlogTable.CONTENT, content);
        blogValues.put(BlogTable.ENTRY_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime()));
        blogValues.put(BlogTable.IMG_RES_PATH, imgResPath);
        return getWritableDatabase().update(BlogTable.TABLE_NAME, blogValues, "_id = ?", new String[]{Integer.toString(blogId)});
    }

    public Blog getBlogById(int id){
        Blog blogData = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor blogCursor = db.query(BlogTable.TABLE_NAME, BlogTable.BLOG_COLUMNS, "_id = ?", new String[]{Integer.toString(id)}, null, null, null);
        if(blogCursor != null && blogCursor.moveToFirst()){
            blogData = new Blog(blogCursor.getInt(0), blogCursor.getString(1), blogCursor.getString(2), blogCursor.getString(3), blogCursor.getString(4));
        }
        if(blogCursor!=null)
            blogCursor.close();
        return blogData;
    }

    public int deleteBlog(Integer[] blogIds){
        int len = blogIds.length;
        String []ids = new String[len];
        StringBuilder whereClause = new StringBuilder("_id IN (");
        for(int i=0;i<len;i++){
            String c = "?";
            if(i!=len-1){
                c = "?,";
            }
            whereClause.append(c);
            ids[i] = blogIds[i]+"";
        }
        whereClause.append(")");
        return getWritableDatabase().delete(BlogTable.TABLE_NAME, whereClause.toString(), ids);
    }

    public static class BlogTable{
        public static final String TABLE_NAME = "blog";
        public static final String ID = "_id";
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String ENTRY_DATE = "entry_date";
        public static final String IMG_RES_PATH = "img_res_path";

        public static final String[] BLOG_COLUMNS = {ID, TITLE, CONTENT, ENTRY_DATE, IMG_RES_PATH};
    }
}
