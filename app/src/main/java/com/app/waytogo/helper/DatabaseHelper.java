package com.app.waytogo.helper;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static String DB_PATH;
	private static String DB_NAME = "Ride";
	public static String DB_NEWPATH;
	private Context myContext;
	private SQLiteDatabase myDataBase;
	
	public DatabaseHelper(Context context) {
		super(context,DB_NAME,null,1);
		// TODO Auto-generated constructor stub
		this.myContext = context;
		DB_PATH = "/data/data/"+context.getPackageName()+"/databases/";
		DB_NEWPATH = DB_PATH + DB_NAME;
	}
	
	/*
	 * Creates a empty database on the system and rewrites it with your own database.
	 * 
	 */
	public void CreateDatabase() throws IOException
	{
		boolean dbExists = checkDatabase();
		if(!dbExists)
		{
			this.getReadableDatabase();
			copyDatabase();
		}
	}
	
	
	/* 
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * @return true if it exists, false if it doesn't 
	 */
	private boolean checkDatabase() {
		// TODO Auto-generated method stub
		SQLiteDatabase checkDB = null; 
		try{ 
			checkDB = SQLiteDatabase.openDatabase(DB_NEWPATH, null, SQLiteDatabase.OPEN_READONLY); 
		}catch(SQLiteException e){ } 
		
		if(checkDB != null){ checkDB.close(); } 
		
		return (checkDB != null) ? true : false;
	}

	/* Copies your database from your local assets-folder to the just created empty database in the * system folder, 
	 * from where it can be accessed and handled. 
	 * This is done by transferring bytestream. * 
	 * @throws IOException 
	 */ 
	private void copyDatabase() throws IOException {
		// TODO Auto-generated method stub
		
		//Open your local db as the input stream 
		InputStream myInput = myContext.getAssets().open(DB_NAME); // Path to the asset folder database
		
		// Path to the just created empty db
    	String outFileName = DB_NEWPATH;
		
		FileOutputStream myOutput = new FileOutputStream(outFileName);
		byte[] buffer = new byte[1024];
		int length;
		while((length = myInput.read(buffer)) > 0)
		{
			myOutput.write(buffer,0,length);
		}
		
		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	/*
	 * Open Database
	 * 
	 */
	public SQLiteDatabase openDB() throws SQLException
	{
		//Log.i("newdbpath",DB_NEWPATH);
		myDataBase = SQLiteDatabase.openDatabase(DB_NEWPATH, null, SQLiteDatabase.OPEN_READWRITE);
		return myDataBase;
	}
	
	/*
	 * Close Database 
	 */
	public synchronized void close() {
		if(myDataBase != null)
			myDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//Do Nothing, DB created already
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		/* No upgrade of Database */
	}

}
