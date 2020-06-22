package com.example.sid2020.APP.Database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseReader {
    SQLiteDatabase db;

    public DatabaseReader(DatabaseHandler dbHandler){
        db = dbHandler.getReadableDatabase();
    }

    public Cursor readMedicoesTemperatura(){
        Cursor cursor = db.query(
                DatabaseConfig.MedicoesTemperatura.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseConfig.MedicoesTemperatura.COLUMN_NAME_DATAHORAMEDICAO + " ASC"
        );
        return cursor;
    }

    public Cursor readAlertasGlobais(){
        Cursor cursor = db.query(
                DatabaseConfig.AlertasGlobais.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseConfig.AlertasGlobais.COLUMN_NAME_DATAHORAMEDICAO + " DESC"
        );
        return cursor;
    }
}
