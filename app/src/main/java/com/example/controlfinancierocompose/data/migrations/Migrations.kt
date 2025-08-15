package com.example.controlfinancierocompose.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Crear nueva tabla con foreign key ON DELETE CASCADE
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS investments_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                platformId INTEGER NOT NULL,
                name TEXT NOT NULL,
                amount REAL NOT NULL,
                shares REAL NOT NULL,
                price REAL NOT NULL,
                type TEXT NOT NULL,
                notes TEXT NOT NULL,
                date TEXT NOT NULL,
                isActive INTEGER NOT NULL,
                FOREIGN KEY(platformId) REFERENCES investment_platforms(id) ON DELETE CASCADE
            )
        """)
        // 2. Copiar datos de la tabla antigua a la nueva
        database.execSQL("""
            INSERT INTO investments_new (id, platformId, name, amount, shares, price, type, notes, date, isActive)
            SELECT id, platformId, name, amount, shares, price, type, notes, date, isActive FROM investments
        """)
        // 3. Eliminar la tabla antigua
        database.execSQL("DROP TABLE investments")
        // 4. Renombrar la nueva tabla
        database.execSQL("ALTER TABLE investments_new RENAME TO investments")
    }
}
