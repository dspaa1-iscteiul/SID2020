package com.example.sid2020.APP.Database;

import android.provider.BaseColumns;

public class DatabaseConfig {

    public static class MedicoesTemperatura implements BaseColumns {
        public static final String TABLE_NAME="MedicoesSensores";
        public static final String COLUMN_NAME_ID_MEDICAO ="idMedicao";
        public static final String COLUMN_NAME_DATAHORAMEDICAO="DataHoraMedicao";
        public static final String COLUMN_NAME_VALOR_MEDICAO ="ValorMedicao";
    }

    public static class AlertasGlobais implements BaseColumns {
        public static final String TABLE_NAME="Alerta";
        public static final String COLUMN_NAME_ID_ALERTA ="idAlerta";
        public static final String COLUMN_NAME_ID ="ID";
        public static final String COLUMN_NAME_DATAHORAMEDICAO="DataHoraMedicao";
        public static final String COLUMN_NAME_TIPO_SENSOR ="TipoSensor";
        public static final String COLUMN_NAME_VALOR_MEDICAO ="ValorMedicao";
        public static final String COLUMN_NAME_LIMITE ="Limite";
        public static final String COLUMN_NAME_DESCRICAO ="Descricao";
        public static final String COLUMN_NAME_CONTROLO ="Controlo";
        public static final String COLUMN_NAME_EXTRA ="Extra";
    }

    protected static final String SQL_CREATE_MEDICOESTEMPERATURA=
            "CREATE TABLE " + MedicoesTemperatura.TABLE_NAME +
                    " (" + MedicoesTemperatura.COLUMN_NAME_ID_MEDICAO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MedicoesTemperatura.COLUMN_NAME_VALOR_MEDICAO + " DOUBLE," +
                    MedicoesTemperatura.COLUMN_NAME_DATAHORAMEDICAO + " TIMESTAMP  )";

    protected static final String SQL_DELETE_MEDICOESTEMPERATURA_DATA=
            "DELETE FROM " + MedicoesTemperatura.TABLE_NAME;

    protected static final String SQL_CREATE_ALERTASGLOBAIS=
            "CREATE TABLE " + AlertasGlobais.TABLE_NAME +
                    " (" + AlertasGlobais.COLUMN_NAME_ID_ALERTA + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    AlertasGlobais.COLUMN_NAME_ID + " INTEGER," +
                    AlertasGlobais.COLUMN_NAME_DATAHORAMEDICAO + " TIMESTAMP," +
                    AlertasGlobais.COLUMN_NAME_TIPO_SENSOR + " TEXT, " +
                    AlertasGlobais.COLUMN_NAME_VALOR_MEDICAO + " DOUBLE," +
                    AlertasGlobais.COLUMN_NAME_LIMITE + " DOUBLE," +
                    AlertasGlobais.COLUMN_NAME_DESCRICAO + " TEXT," +
                    AlertasGlobais.COLUMN_NAME_CONTROLO + " INTEGER," +
                    AlertasGlobais.COLUMN_NAME_EXTRA + " TEXT )";

    protected static final String SQL_DELETE_ALERTASGLOBAIS_DATA=
            "DELETE FROM " + AlertasGlobais.TABLE_NAME;

    protected static final String SQL_CREATE_DROP_ALERTA_IFEXISTS=("DROP TABLE IF EXISTS ") + AlertasGlobais.TABLE_NAME;
    protected static final String SQL_CREATE_DROP_MEDICOES_IFEXISTS=("DROP TABLE IF EXISTS ") + MedicoesTemperatura.TABLE_NAME;

}
