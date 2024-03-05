package com.example.kotlindemoen

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindemoen.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class MainActivity : AppCompatActivity() {

    private var libVLC: LibVLC? = null
    private var vlcMediaPlayer: MediaPlayer? = null
    private lateinit var vlcVideoLayout: VLCVideoLayout

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val TAG = "VLCPlayer"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the VLCVideoLayout
        val vlcVideoLayout = binding.videoView

        binding.playButton.setOnClickListener { vlcMediaPlayer?.play() }
        binding.pauseButton.setOnClickListener { vlcMediaPlayer?.pause() }

        // Initialize VLC player
        initializeVlcPlayer("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4")
    }

    private fun initializeVlcPlayer(videoUrl: String?) {
        videoUrl?.let { url ->
            binding?.let { binding ->
                val args = arrayListOf(
                    "--file-caching=150",
                    "--network-caching=150",
                    "--clock-jitter=0",
                    "--live-caching=150",
                    "--drop-late-frames",
                    "--skip-frames",
                    "--vout=android-display",
                    "--sout-transcode-vb=20",
                    "--no-audio",
                    "--sout=#transcode{vcodec=h264,vb=20,acodec=mpga,ab=128,channels=2,samplerate=44100}:duplicate{dst=display}",
                    "--sout-x264-nf"
                )
                libVLC = LibVLC(binding.root.context, args)
                vlcMediaPlayer = org.videolan.libvlc.MediaPlayer(libVLC)
                vlcMediaPlayer?.attachViews(binding.videoView, null, false, false)
                vlcMediaPlayer?.setEventListener { event ->
                    handleVlcEvents(event, binding)
                }
                setVlcMedia(url)
                binding.videoView.visibility = View.VISIBLE
            }
        }
    }

    private fun handleVlcEvents(
        event: org.videolan.libvlc.MediaPlayer.Event,
        binding: ActivityMainBinding
    ) {
        when (event.type) {
            org.videolan.libvlc.MediaPlayer.Event.Playing -> {
                binding.loaderLa.visibility = View.GONE
                Log.d(TAG, "VLC Event Playing")
            }

            org.videolan.libvlc.MediaPlayer.Event.Paused -> {
                binding.loaderLa.visibility = View.GONE
                Log.d(TAG, "VLC Event Paused")
            }

            org.videolan.libvlc.MediaPlayer.Event.Stopped -> {
                binding.loaderLa.visibility = View.VISIBLE
                Log.d(TAG, "VLC Event Stopped")
            }

            org.videolan.libvlc.MediaPlayer.Event.Buffering -> {
                binding.loaderLa.visibility = View.VISIBLE
                Log.d(TAG, "VLC Event Buffering")
            }

            org.videolan.libvlc.MediaPlayer.Event.EncounteredError -> {
                binding.loaderLa.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                 //   refreshVideoUrl(videoUrl)
                }
                Log.d(TAG, "VLC Event Error")
            }

            org.videolan.libvlc.MediaPlayer.Event.EndReached -> {
                binding.loaderLa.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                  //  refreshVideoUrl(videoUrl)
                }
                Log.d(TAG, "VLC Event End Reached")
            }

            org.videolan.libvlc.MediaPlayer.Event.Opening -> {
                binding.loaderLa.visibility = View.VISIBLE
                Log.d(TAG, "VLC Event Opening")
            }

            org.videolan.libvlc.MediaPlayer.Event.TimeChanged -> {
                binding.loaderLa.visibility = View.GONE

                Log.d(TAG, "VLC Event Time Changed")
            }

            org.videolan.libvlc.MediaPlayer.Event.PositionChanged -> {
                binding.loaderLa.visibility = View.GONE
                Log.d(TAG, "VLC Event Position Changed")
            }

            org.videolan.libvlc.MediaPlayer.Event.SeekableChanged -> {
                binding.loaderLa.visibility = View.GONE
                Log.d(TAG, "VLC Event Seekable Changed")
            }

            org.videolan.libvlc.MediaPlayer.Event.PausableChanged -> {
                binding.loaderLa.visibility = View.GONE
                Log.d(TAG, "VLC Event Pausable Changed")
            }

            org.videolan.libvlc.MediaPlayer.Event.LengthChanged -> {
                binding.loaderLa.visibility = View.VISIBLE
                Log.d(TAG, "VLC Event Length Changed")
            }

            org.videolan.libvlc.MediaPlayer.Event.Vout -> {
                binding.loaderLa.visibility = View.GONE
                Log.d(TAG, "VLC Event Video Output")
            }

            org.videolan.libvlc.MediaPlayer.Event.ESAdded -> {
                binding.loaderLa.visibility = View.VISIBLE
                Log.d(TAG, "VLC Event Elementary Stream Added")
            }

            org.videolan.libvlc.MediaPlayer.Event.ESDeleted -> {
                binding.loaderLa.visibility = View.VISIBLE
                Log.d(TAG, "VLC Event Elementary Stream Deleted")
            }

            org.videolan.libvlc.MediaPlayer.Event.ESSelected -> {
                binding.loaderLa.visibility = View.VISIBLE
                Log.d(TAG, "VLC Event Elementary Stream Selected")
            }

            else -> {
                binding.loaderLa.visibility = View.VISIBLE
                Log.d(TAG, "VLC Event Other: ${event.type}")
            }
        }
    }

    private fun setVlcMedia(videoUrl: String) {
        val media = Media(libVLC, Uri.parse(videoUrl))
        vlcMediaPlayer?.media = media
        vlcMediaPlayer?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseVlcPlayer()
    }

    private fun releaseVlcPlayer() {
        vlcMediaPlayer?.stop()
        vlcMediaPlayer?.release()
        libVLC?.release()
    }
}
