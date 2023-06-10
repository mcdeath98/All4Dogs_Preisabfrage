package ch.all4dogs.db

import android.provider.BaseColumns

object ProductColumns : BaseColumns {
    const val TABLE_NAME = "products"

    const val COLUMN_NAME_ARTICLE = "article"
    const val COLUMN_NAME_GTIN = "gtin"
    const val COLUMN_NAME_HAN = "han"
    const val COLUMN_NAME_NAME = "name"
    const val COLUMN_NAME_PRICE = "price"

    const val sqlCreate = "CREATE TABLE ${ProductColumns.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${ProductColumns.COLUMN_NAME_ARTICLE} TEXT," +
            "${ProductColumns.COLUMN_NAME_GTIN} TEXT," +
            "${ProductColumns.COLUMN_NAME_HAN} TEXT," +
            "${ProductColumns.COLUMN_NAME_NAME} TEXT," +
            "${ProductColumns.COLUMN_NAME_PRICE} INTEGER" +
            ")"

    const val sqlDelete = "DROP TABLE IF EXISTS ${ProductColumns.TABLE_NAME}"

}
