package com.furkanekiz.kotlinartbook

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.furkanekiz.kotlinartbook.adapter.AdapterArt
import com.furkanekiz.kotlinartbook.databinding.AcMainBinding

class ACMain : AppCompatActivity() {

    private lateinit var binding: AcMainBinding
    private lateinit var artList: ArrayList<Art>
    private lateinit var adapterArt: AdapterArt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AcMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        artList = ArrayList()

        adapterArt= AdapterArt(artList)
        binding.rvArt.layoutManager = LinearLayoutManager(this)
        binding.rvArt.adapter = adapterArt

        getData()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getData(){
        try {
            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)

            val cursor = database.rawQuery("SELECT * FROM arts",null)

            val artNameIx = cursor.getColumnIndex("artname")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name = cursor.getString(artNameIx)
                val id = cursor.getInt(idIx)
                val art = Art(name,id)
                artList.add(art)
            }
            adapterArt.notifyDataSetChanged()
            cursor.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //inflater
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.addArtItem) {
            val intent = Intent(this, ACArt::class.java)
            intent.putExtra("info","new")
            startActivity(intent)

        }

        return super.onOptionsItemSelected(item)
    }
}