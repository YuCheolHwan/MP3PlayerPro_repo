package com.example.mp3player

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mp3player.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val REQUEST_CODE = 100
    lateinit var musicDataList: MutableList<MusicData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 외장 메모리 읽기 권한 승인 받아야 함
        val flag = ContextCompat.checkSelfPermission(this,permission[0])
        if(flag == PackageManager.PERMISSION_GRANTED){
            // 원하는 것을 진행하면 됨
            startProcess()
        } else{
            // 승인 요청
            ActivityCompat.requestPermissions(this,permission,REQUEST_CODE)
        }
    }




    // 승인 요청을 하면
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startProcess()
            } else{
                Toast.makeText(this,"권한 승인을 해야만 앱을 사용 할 수 있습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun startProcess() {
        // 컨텐트 리졸버를 통해서 음원 정보를 가져옴
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        val cursor = contentResolver.query(musicUri,projection,null,null,null)
        musicDataList = mutableListOf()
        if (cursor!!.count <= 0){
            Toast.makeText(this,"메모리에 음악 파일이 없습니다. 다운 받아 주세요.", Toast.LENGTH_SHORT).show()
            finish()
        }
        while (cursor.moveToNext()){
            val id = cursor.getString(0)
            val title = cursor.getString(1)
            val artist = cursor.getString(2)
            val albumId = cursor.getString(3)
            val duration = cursor.getLong(4)
            val musicData = MusicData(id, title, artist, albumId, duration)
            musicDataList.add(musicData)
        }
        // Adapter와 RecyclerView 연결
        val musicRecyclerAdapter = MusicRecyclerAdapter(this, musicDataList)
        binding.recyclerView.adapter = musicRecyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }
}