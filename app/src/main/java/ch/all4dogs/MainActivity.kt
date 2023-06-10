package ch.all4dogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import ch.all4dogs.databinding.ActivityMainBinding
import ch.all4dogs.db.ProductDbHelper
import ch.all4dogs.utils.Prefs
import ch.all4dogs.utils.Utils


class MainActivity : AppCompatActivity() {

    companion object {
        const val FOLDER_CSV = "prices.csv"
        const val FOLDER_IMAGES = "images"
    }

    private lateinit var launchResult: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityMainBinding

    private var handler = Handler(Looper.getMainLooper())

    private val dbHelper = ProductDbHelper(this)
    private var rootFolder: DocumentFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    importFiles(uri)
                }
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.buttonLokal.setOnClickListener { selectFolder() }


        binding.editCode.setOnKeyListener { _, keyCode, keyEvent ->
            if ((keyCode == EditorInfo.IME_ACTION_SEARCH ||
                        keyCode == EditorInfo.IME_ACTION_DONE ||
                        keyEvent.action == KeyEvent.ACTION_DOWN) &&
                keyEvent.keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                val text = binding.editCode.text.toString().trim()

                if (text.isNotEmpty()) {
                    clearInput()

                    findProduct(text)
                    return@setOnKeyListener true
                }
            }

            return@setOnKeyListener false
        }

        setContentView(binding.root)

        binding.titel2.setTextColor(Color.BLUE);
        binding.titel2.setTypeface(null, Typeface.BOLD);
        binding.titel2.setText(R.string.welcome_message);


        binding.imageView.setImageResource(R.drawable.default_image)

        loadFolder()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    private fun selectFolder() {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        launchResult.launch(Intent.createChooser(i, "Select Folder"))
    }

    private fun loadFolder() {
        val folder = Prefs.getFolder(this)
        if (folder.isEmpty()) {
            showToast("Import data first!")
            return
        }

        val uri = Uri.parse(folder)
        val document = DocumentFile.fromTreeUri(this, uri)
        if (document == null) {
            showToast("An error occurred!")
            return
        }

        if (!document.exists()) {
            showToast("Folder not found, import data first!")
            return
        }

        binding.textFolder.text = document.name
        binding.textStats.text = getString(R.string.d_items_available).format(dbHelper.count())
        rootFolder = document
    }

    private fun importFiles(uri: Uri) {
        val document = DocumentFile.fromTreeUri(this, uri)
        if (document == null) {
            showToast("An error occurred!")
            return
        }

        rootFolder = document
        binding.textFolder.text = getString(R.string.importing_csv)
        binding.buttonLokal.isEnabled = false

        Utils.importCSV(this@MainActivity, document, dbHelper, ::showStatus) { err, _ ->
            handler.post {
                if (err != null) {
                    showToast(err)

                    binding.textStats.text = err
                } else {
                    binding.textStats.text = getString(R.string.d_items_imported)
                        .format(dbHelper.count())

                    Prefs.setFolder(this@MainActivity, uri.toString())
                    Utils.persist(this@MainActivity, uri)
                }
                binding.textFolder.text = document.name
                binding.buttonLokal.isEnabled = true
            }
        }
    }

    private fun findProduct(text: String) {
        binding.progressBar.isVisible = true

        binding.textName.text = ""
        binding.textPrice.text = ""
        binding.imageView.setImageBitmap(null)

        Utils.findProduct(dbHelper, text) { product ->
            handler.post {
                if (product == null) {
                    binding.progressBar.isVisible = false

                    showToast("Product not found!")
                } else {

                    binding.textName.text = product.name
                    binding.textPrice.text = "CHF %.2f".format(product.price)

                    val filename = "${product.article}-1.jpg"

                    Utils.loadImage(this@MainActivity, rootFolder, filename) { bitmap ->
                        handler.post {
                            binding.imageView.setImageBitmap(bitmap)
                            binding.progressBar.isVisible = false
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        handler.post {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showStatus(status: String) {
        handler.post {
            binding.textStats.text = status
        }
    }

    private fun clearInput() {
        binding.editCode.setText("")
        binding.editCode.clearFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editCode.windowToken, 0)
    }

}