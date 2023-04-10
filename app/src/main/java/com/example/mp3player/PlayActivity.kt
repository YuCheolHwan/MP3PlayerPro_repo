package com.example.mp3player

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.example.mp3player.databinding.ActivityPlayBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat

class PlayActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityPlayBinding
    val ALBUM_IMAGE_SIZE = 90
    var mediaPlayer: MediaPlayer? = null
    lateinit var musicData: MusicData
    var mp3playerJob: Job? = null
    var pauseFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 전달해온 intent값을 가져 오기
        musicData = intent.getSerializableExtra("musicData") as MusicData
        // 화면에 바인딩 진행
        binding.albumTitle.text = musicData.title
        binding.albumArtist.text = musicData.artist
        binding.totalDuration.text = SimpleDateFormat("mm:ss").format(musicData.duration)
        binding.playDuration.text = "00:00"
        val bitmap = musicData.getAlbumBitmap(this, ALBUM_IMAGE_SIZE)
        if (bitmap != null) {
            binding.albumImage.setImageBitmap(bitmap)
        } else {
            binding.albumImage.setImageResource(R.drawable.music_video_24)
        }
        // 음악 파일 객체 가져 오기
        mediaPlayer = MediaPlayer.create(this, musicData.getMusicUri())
        // 이벤트 처리(일시 정지, 실행, 리스트(돌아가기), 정지, seek바 조절)
        binding.playButton.setOnClickListener(this)
        binding.stopButton.setOnClickListener(this)
        binding.listButton.setOnClickListener(this)
        binding.seekBar.max = mediaPlayer!!.duration
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.listButton -> {
                mp3playerJob?.cancel()
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                finish()
            }
            R.id.playButton -> {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause()
                    binding.playButton.setImageResource(R.drawable.play_24)
                    pauseFlag = true
                } else {
                    mediaPlayer!!.start()
                    binding.playButton.setImageResource(R.drawable.pause_24)
                    pauseFlag = false
                    // 코루틴으로 음악을 재생하지 않으면 ANR이 발생함
                    val backgroundScope = CoroutineScope(Dispatchers.Default + Job())
                    mp3playerJob = backgroundScope.launch {
                        while (mediaPlayer!!.isPlaying) {
                            var currentPosition = mediaPlayer?.currentPosition!!
                            // 코루틴 속에서 화면 값을 변동 시키고자 할 때 runOnUiThread 를 사용하면 바꿀 수 있다.
                            runOnUiThread {
                                binding.seekBar.progress = currentPosition
                                binding.playDuration.text =
                                    SimpleDateFormat("mm:ss").format(mediaPlayer?.currentPosition)
                            }
                            try {
                                delay(1000)
                            } catch (e: java.lang.Exception) {
                                Log.e("PlayActivity - delay", e.toString())
                            }
                        }
                        if (pauseFlag == false) {
                            runOnUiThread {
                                binding.seekBar.progress = 0
                                binding.playButton.setImageResource(R.drawable.play_24)
                                binding.playDuration.text = "00:00"
                            }
                        }
                    } // end of mp3PlayerJob
                }
            }
            R.id.stopButton -> {
                mp3playerJob?.cancel()
                mediaPlayer?.stop()
                mediaPlayer = MediaPlayer.create(this, musicData.getMusicUri())
                binding.seekBar.progress = 0
                binding.playDuration.text = "00:00"
                binding.seekBar.max = mediaPlayer!!.duration
                binding.totalDuration.text = SimpleDateFormat("mm:ss").format(musicData.duration)
                binding.playButton.setImageResource(R.drawable.play_24)
            }
        }
    }

    override fun onBackPressed() {
        mp3playerJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        finish()
    }
}