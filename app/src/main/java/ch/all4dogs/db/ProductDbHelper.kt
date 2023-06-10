package ch.all4dogs.db

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class ProductDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ProductColumns.sqlCreate)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(ProductColumns.sqlDelete)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insert(item: ProductItem): Boolean {
        val values = ContentValues().apply {
            put(ProductColumns.COLUMN_NAME_ARTICLE, item.article)
            put(ProductColumns.COLUMN_NAME_GTIN, item.gtin)
            put(ProductColumns.COLUMN_NAME_HAN, item.han)
            put(ProductColumns.COLUMN_NAME_NAME, item.name)
            put(ProductColumns.COLUMN_NAME_PRICE, item.price)
        }

        val id = writableDatabase.insert(ProductColumns.TABLE_NAME, null, values)
        return id > 0
    }

    fun update(id: Long, item: ProductItem): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(ProductColumns.COLUMN_NAME_ARTICLE, item.article)
            put(ProductColumns.COLUMN_NAME_GTIN, item.gtin)
            put(ProductColumns.COLUMN_NAME_HAN, item.han)
            put(ProductColumns.COLUMN_NAME_PRICE, item.price)
            put(ProductColumns.COLUMN_NAME_NAME, item.name)
        }

        val count = db.update(
            ProductColumns.TABLE_NAME,
            values,
            "${BaseColumns._ID} = ?",
            arrayOf(id.toString())
        )

        return (count > 0)
    }

    fun delete(id: Long): Boolean {
        val count = readableDatabase.delete(
            ProductColumns.TABLE_NAME,
            "${BaseColumns._ID} = ?",
            arrayOf(id.toString())
        )
        return count > 0
    }

    fun count(): Long {
        return DatabaseUtils.queryNumEntries(
            readableDatabase, ProductColumns.TABLE_NAME,
            null, null
        )
    }

    fun get(column: String, value: String, sensitive: Boolean = true): ProductItem? {
        val cursor = readableDatabase.query(
            ProductColumns.TABLE_NAME,
            null,
            if (sensitive) "$column = ?" else "UPPER($column) = ?",
            if (sensitive) arrayOf(value) else arrayOf(value.uppercase()),
            null,
            null,
            null,
            "1"
        )

        with(cursor) {
            while (moveToNext()) {
                try {
                    return ProductItem(
                        id = getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                        article = getString(getColumnIndexOrThrow(ProductColumns.COLUMN_NAME_ARTICLE)),
                        gtin = getString(getColumnIndexOrThrow(ProductColumns.COLUMN_NAME_GTIN)),
                        han = getString(getColumnIndexOrThrow(ProductColumns.COLUMN_NAME_HAN)),
                        name = getString(getColumnIndexOrThrow(ProductColumns.COLUMN_NAME_NAME)),
                        price = getDouble(getColumnIndexOrThrow(ProductColumns.COLUMN_NAME_PRICE)),
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        cursor.close()

        return null
    }

    fun find(text: String): ProductItem? {
        var product = get(ProductColumns.COLUMN_NAME_HAN, text, false)
        if (product != null) {
            return product
        }

        product = get(ProductColumns.COLUMN_NAME_GTIN, text, false)
        if (product != null) {
            return product
        }

        product = get(ProductColumns.COLUMN_NAME_ARTICLE, text, false)
        if (product != null) {
            return product
        }
        return null
    }

    companion object {

        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "products.db"

    }
}