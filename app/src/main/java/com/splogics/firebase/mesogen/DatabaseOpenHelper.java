package com.splogics.firebase.mesogen;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "mesoInput.db";
    private static final int DB_VERSION = 1;

    DatabaseOpenHelper(Context context) {
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlSieveDataQuery = "CREATE TABLE IF NOT EXISTS sieveDataTable (id INTEGER PRIMARY KEY AUTOINCREMENT, modelName VARCHAR NOT NULL, sieveSize FLOAT, cumPercentPass FLOAT);";
        db.execSQL(sqlSieveDataQuery);

        String sqlMixRatioQuery = "CREATE TABLE IF NOT EXISTS mixRatioTable (id INTEGER PRIMARY KEY AUTOINCREMENT, modelName VARCHAR NOT NULL, mixRatioCement FLOAT, mixRatioSand FLOAT, mixRatioAggregate FLOAT);";
        db.execSQL(sqlMixRatioQuery);

        String sqlCubeSizeQuery = "CREATE TABLE IF NOT EXISTS cubeSizeTable (id INTEGER PRIMARY KEY AUTOINCREMENT, modelName VARCHAR NOT NULL, specimenLength FLOAT, specimenWidth FLOAT, specimenDepth FLOAT);";
        db.execSQL(sqlCubeSizeQuery);

        String sqlVoidsDataQuery = "CREATE TABLE IF NOT EXISTS voidsTable (id INTEGER PRIMARY KEY AUTOINCREMENT, modelName VARCHAR NOT NULL, voidsPercentage FLOAT, voidsMaxSize FLOAT, voidsMinSize FLOAT);";
        db.execSQL(sqlVoidsDataQuery);
    }

    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sieveDataTable = "DROP TABLE IF EXISTS sieveDataTable";
        db.execSQL(sieveDataTable);

        String sqlMixRatioQuery = "DROP TABLE IF EXISTS sqlMixRatioTable";
        db.execSQL(sqlMixRatioQuery);

        String sqlCubeSizeQuery = "DROP TABLE IF EXISTS cubeSizeTable";
        db.execSQL(sqlCubeSizeQuery);

        String sqlVoidsDataQuery = "DROP TABLE IF EXISTS voidsTable";
        db.execSQL(sqlVoidsDataQuery);

        onCreate(db);
    }
}