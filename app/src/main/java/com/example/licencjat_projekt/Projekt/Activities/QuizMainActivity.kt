package com.example.licencjat_projekt.Projekt.Activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.example.licencjat_projekt.Projekt.Models.*
import com.example.licencjat_projekt.Projekt.database.Quiz
import com.example.licencjat_projekt.Projekt.database.Quizes
import com.example.licencjat_projekt.Projekt.utils.falseToken
import com.example.licencjat_projekt.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_quiz_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.ByteArrayOutputStream
import java.io.IOException

class QuizMainActivity : AppCompatActivity(), View.OnClickListener {
    private var isPrivate: Boolean = false
    private lateinit var invitation_code: String
    private lateinit var quiz_image: ByteArray
    private var isImage: Boolean = false
    private var quizModel: CreateQuizModel? = null

    companion object {
        const val GALLERY_CODE = 1
        var QUIZ_DETAILS = "quiz_details"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (falseToken()){
            val intent = Intent(
                this,
                SignInActivity::class.java
            )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("EXIT",true)
            startActivity(intent)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_main)

        quizmain_toolbar.setNavigationOnClickListener {
            val alert = AlertDialog.Builder(this)
            alert.setTitle("Czy chcesz zakończyć tworzenie quizu?")
            val items = arrayOf(
                "Tak",
                "Nie"
            )
            alert.setItems(items) { _, n ->
                when (n) {
                    0 -> onBackPressed()
                    1 -> goBack()
                }
            }
            alert.show()
        }
        quizmain_start_creating.setOnClickListener(this)
        quizmain_privacy.setOnClickListener(this)
        quizmain_image.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.quizmain_start_creating -> {
                when {
                    quizmain_title.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "Podaj tytuł!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizmain_title.length() > 20 || quizmain_title.length() < 2 -> {
                        Toast.makeText(
                            this,
                            "Tytuł powinien zawierać od 2 do 20 znaków!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizmain_timer.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "Podaj czas trwania testu!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Integer.parseInt(quizmain_timer.text.toString()) < 1
                            || Integer.parseInt(quizmain_timer.text.toString()) > 60 -> {
                        Toast.makeText(
                            this,
                            "Czas trwania testu powinien wynosić od 1 do 60 minut!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizmain_description.length() > 200 ||
                            quizmain_description.length() < 2 -> {
                        Toast.makeText(
                            this,
                            "Opis powinien zawierać od 2 do 200 znaków!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizmain_description.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "Podaj opis!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizmain_tags.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "Dodaj tagi!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizmain_tags.length() > 100 || quizmain_tags.length() < 2 -> {
                        Toast.makeText(
                            this,
                            "Wiadomość końcowa powininna zawierać od 2 do 100 znaków!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizmain_final_comment.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            "Podaj komentarz końcowy!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    quizmain_final_comment.length() > 200 ||
                            quizmain_final_comment.length() < 2 -> {
                        Toast.makeText(
                            this,
                            "Wiadomość końcowa powininna zawierać od 2 do 200 znaków!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    !isImage -> {
                        Toast.makeText(
                            this,
                            "Dodaj zdjęcie!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {

                        invitation_code = generateInvitationCode()
                        while(checkIfInvitationCodeExists()){
                            invitation_code = generateInvitationCode()
                        }

                        quizModel = CreateQuizModel(
                            quizmain_title.text.toString(),
                            Integer.parseInt(quizmain_timer.text.toString()),
                            quizmain_description.text.toString(),
                            quizmain_tags.text.toString(),
                            quizmain_final_comment.text.toString(),
                            isPrivate,
                            invitation_code,
                            quiz_image
                        )

                        val intent = Intent(
                            this,
                            QuestionsActivity::class.java
                        )
                        if (quizModel != null) {
                            intent.putExtra(
                                QUIZ_DETAILS,
                                quizModel
                            )
                        }
                        startActivity(intent)

                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }

            R.id.quizmain_privacy -> {
                isPrivate = quizmain_privacy.isChecked
            }

            R.id.quizmain_image -> {
                chooseImageFromGalery()
            }
        }
    }

    private fun checkIfInvitationCodeExists(): Boolean {
        return runBlocking {
            return@runBlocking newSuspendedTransaction(Dispatchers.IO) {
                return@newSuspendedTransaction Quiz.find {
                    Quizes.invitation_code eq invitation_code
                }.toList().isNotEmpty()
            }
        }
    }

    private fun generateInvitationCode(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return ((1..6)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get).joinToString(""))
    }

    private fun chooseImageFromGalery() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(
                    report: MultiplePermissionsReport?
                ) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent =
                            Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                        startActivityForResult(
                            galleryIntent,
                            GALLERY_CODE
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    permissionDeniedDialog()
                }
            }).onSameThread().check()
    }

    private fun permissionDeniedDialog() {
        AlertDialog.Builder(this).setMessage("Brak uprawnień")
            .setPositiveButton("Przejdz do USTAWIEŃ")
            { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts(
                        "package",
                        packageName,
                        null
                    )
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("ANULUJ")
            { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_CODE) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImage =
                            MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                contentURI
                            )
                        quiz_image = saveImageByteArray(selectedImage)
                        isImage = true
                        quizmain_image.setImageBitmap(selectedImage)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Błąd!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun saveImageByteArray(
        bitmap: Bitmap
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(
            Bitmap.CompressFormat.JPEG,
            100,
            stream
        )
        return stream.toByteArray()
    }
    private fun goBack() {}
}