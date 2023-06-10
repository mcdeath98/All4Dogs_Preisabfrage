package ch.all4dogs.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import ch.all4dogs.MainActivity
import ch.all4dogs.db.ProductColumns
import ch.all4dogs.db.ProductDbHelper
import ch.all4dogs.db.ProductItem
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Utils {

    private val queue: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private fun cleanColumn(input: String): String {
        var text = input.trim()
        if (text.startsWith("\"")) {
            text = text.substring(1)
        }
        if (text.endsWith("\"")) {
            text = text.dropLast(1)
        }
        return text.trim()
    }

    fun persist(context: Context, uri: Uri) {
        val contentResolver = context.applicationContext.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
    }

    fun importCSV(
        context: Context,
        root: DocumentFile,
        dbHelper: ProductDbHelper,
        progress: (String) -> Unit,
        callback: (String?, Int) -> Unit
    ) {
        queue.execute {
            val csvFile = root.findFile(MainActivity.FOLDER_CSV)
            if (csvFile == null || !csvFile.isFile) {
                callback("CSV file not found!", 0)
            } else {
                var inserted = 0
                var count = 0

                try {
                    context.contentResolver.openInputStream(csvFile.uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            var line: String?
                            do {
                                line = reader.readLine()

                                count++
                                if (count == 1) {
                                    continue
                                }

                                if (line == null) {
                                    continue
                                }

                                val columns = line.split(";")
                                if (columns.size != 6) {
                                    continue
                                }

                                val item = ProductItem(
                                    article = cleanColumn(columns[0]),
                                    gtin = cleanColumn(columns[1]),
                                    han = cleanColumn(columns[2]),
                                    name = cleanColumn(columns[3]),
                                    price = cleanColumn(columns[4]).toDoubleOrNull() ?: 0.0,
                                )

                                val oldItem =
                                    dbHelper.get(ProductColumns.COLUMN_NAME_ARTICLE, item.article)
                                if (oldItem == null) {
                                    if (dbHelper.insert(item)) {
                                        inserted++
                                    }
                                } else {
                                    dbHelper.update(oldItem.id, item)
                                }

                                progress("%d".format(count))
                            } while (line != null)

                        }
                    }
                    callback(null, inserted)
                } catch (_: Exception) {
                    callback("An error occurred!", 0)
                }
            }
        }
    }

    fun findProduct(dbHelper: ProductDbHelper, text: String, callback: (ProductItem?) -> Unit) {
        queue.execute {
            val product = dbHelper.find(text)
            if (product == null) {
                callback(null)
            } else {
                callback(product)
            }
        }
    }

    fun loadImage(
        context: Context,
        folder: DocumentFile?,
        filename: String,
        callback: (Bitmap?) -> Unit
    ) {
        queue.execute {
            var bitmap: Bitmap? = null

            if (folder != null) {
                val imageFolder = folder.findFile(MainActivity.FOLDER_IMAGES)
                if (imageFolder != null) {
                    val file = imageFolder.findFile(filename)
                    if (file != null && file.exists()) {
                        try {
                            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                                bitmap = BitmapFactory.decodeStream(inputStream)
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }

            callback(bitmap)
        }
    }

}