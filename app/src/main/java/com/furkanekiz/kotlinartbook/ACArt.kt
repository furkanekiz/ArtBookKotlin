package com.furkanekiz.kotlinartbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.furkanekiz.kotlinartbook.databinding.AcArtBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class ACArt : AppCompatActivity() {


    private lateinit var binding: AcArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var database: SQLiteDatabase
    private var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database=this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")){
            binding.etArtName.setText("")
            binding.etArtistName.setText("")
            binding.etYear.setText("")
            binding.btSave.visibility = View.VISIBLE
            binding.ivSelectImage.setImageResource(R.drawable.bg_selected_image)
        }else{
            binding.btSave.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)

            try {
                val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

                val artNameIx= cursor.getColumnIndex("artname")
                val artistNameIx=cursor.getColumnIndex("artistname")
                val yearIx=cursor.getColumnIndex("year")
                val imageIx= cursor.getColumnIndex("image")

                while (cursor.moveToNext()){
                    binding.etArtName.setText(cursor.getString(artNameIx))
                    binding.etArtistName.setText(cursor.getString(artistNameIx))
                    binding.etYear.setText(cursor.getString(yearIx))

                    val byteArray = cursor.getBlob(imageIx)
                    val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                    binding.ivSelectImage.setImageBitmap(bitmap)
                }
                cursor.close()
            }catch (e: Exception){
                e.printStackTrace()
            }


        }

    }

    fun saveButtonClicked(view: View) {
        val artName = binding.etArtName.text.toString()
        val artistName = binding.etArtistName.text.toString()
        val year =binding.etYear.text.toString()

        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {

                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)
                statement.execute()

            }catch (e: Exception){
                e.printStackTrace()
            }
            //finish()
            //or
            val intent = Intent(this,ACMain::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int):Bitmap{
        //return Bitmap.createScaledBitmap(image,100,100,true)
         var width = image.width
         var height = image.height

         val bitmapRatio : Double = width.toDouble() / height.toDouble()
         if (bitmapRatio > 1) {
             width = maximumSize
             val scaledHeight = width / bitmapRatio
             height = scaledHeight.toInt()
         } else {
             height = maximumSize
             val scaledWidth = height * bitmapRatio
             width = scaledWidth.toInt()
         }
         return Bitmap.createScaledBitmap(image,width,height,true)

    }

    fun selectImage(view: View) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //rationale
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Give Permission") {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                    }.show()

            } else {
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        } else {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
            //intent

        }
    }

    private fun registerLauncher() {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageData = intentFromResult.data
                        //binding.imageView.setImageURI(imageData)
                        if (imageData != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(contentResolver, imageData)
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.ivSelectImage.setImageBitmap(selectedBitmap)
                                }else{
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                    binding.ivSelectImage.setImageBitmap(selectedBitmap)
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result){
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //permission denied
                Toast.makeText(this,"Permission needed!",Toast.LENGTH_LONG).show()
            }
        }
    }
}