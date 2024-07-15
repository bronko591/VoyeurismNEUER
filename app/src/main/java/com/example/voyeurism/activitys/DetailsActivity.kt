package com.example.voyeurism.activitys

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.Notification
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import android.util.Log
import android.util.Rational
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.util.SizeFCompat
import androidx.fragment.app.FragmentActivity
import androidx.mediarouter.app.MediaRouteButton
import com.example.voyeurism.R
import com.example.voyeurism.chromecast.CastManagement
import com.example.voyeurism.chromecast.ExpandedControlsActivity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage

const val STATE_RESUME_WINDOW = "resumeWindow"
const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
const val STATE_PLAYER_PLAYING = "playerOnPlay"
const val STATE_RESUME_POSITION = "resumePosition"
const val MAX_HEIGHT = 539
const val MAX_WIDTH = 959

@Suppress("DEPRECATION")
class DetailsActivity : AppCompatActivity() {
    private lateinit var toolbar:Toolbar
    // PlayControls --->
    lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var playerView: PlayerView
    // Player Supports --->
    //private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var trackSelector: DefaultTrackSelector
    // StatusLayouts --->
    private lateinit var progressBarPlayer: ProgressBar
    lateinit var textViewPlayer: TextView
    // ToolBar ---->
    private lateinit var toolBar: Toolbar
    // PlayerControls --->
    private lateinit var hdQuality:ImageButton
    private lateinit var fullScreen: ImageButton
    private lateinit var play:ImageButton
    // Notification ExoPlayer
    private var notificationBoolean:Boolean = false

    // Bundle Items ---->
    private lateinit var modelM3u8:String
    private lateinit var modelName:String
    private lateinit var modelImage:String
    // Media Items ---->
    private lateinit var mediaItem:MediaItem
    // Variables ---->
    private var isAutoPlay:Boolean = true
    private var isPlayerPlaying:Boolean = false
    private var isFullscreen:Boolean = false
    private var trackDialog: Dialog? = null
    private var playbackPosition: Long = 0
    // chromeCast Controls
    private lateinit var castSession: CastSession
    private lateinit var castContext: CastContext
    private lateinit var sessionManager: SessionManager
    private lateinit var mediaRouteButton: MediaRouteButton
    private val sessionManagerListener: CastManagement.SessionManagerListenerImpl = CastManagement().SessionManagerListenerImpl()
    // SeekBar - Volume Chances ---->
    private lateinit var audioManager: AudioManager
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var volumeMute:ImageView
    private var isMute:Boolean = false
    private var saveVolume:Int = 0
    private var currentVolume:Int = 0
    private var minimumVolume:Int = 0
    private var maximumVolume:Int = 150
    //
    var isPipMode: Boolean = false

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        mediaRouteButton = findViewById(R.id.media_route_button)
        castContext = CastContext.getSharedInstance(this)
        sessionManager = CastContext.getSharedInstance(this).sessionManager
        CastButtonFactory.setUpMediaRouteButton(this, mediaRouteButton)

        DefaultDataSourceFactory(this, Util.getUserAgent(this, R.string.app_name.toString()))
        modelM3u8 = this.intent.getStringExtra("modelM3u8").toString()
        modelName = this.intent.getStringExtra("modelName").toString()
        modelImage = this.intent.getStringExtra("modelImage").toString()
        mediaItem = MediaItem.Builder()
            .setUri(modelM3u8)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
        initActionBar()
        playerView = findViewById(R.id.playerView)
        initImageViews()
        audioManager()
        hdQuality.setOnClickListener {
            if(trackDialog == null){
                initPopupQuality()
            }
            trackDialog?.show()
        }
        initStatusViews()
        initFullScreenButton()
        //initExoPlayer()
        //initPlayer(true)
        if (isCastConnected()){
            initPlayer(false)
            textViewPlayer.visibility = View.VISIBLE
            //textViewPlayer.text = "Chromecast is Connected!"
            startPlaybackOnChromecast(modelName, "Chaturbate presents.", modelM3u8, modelImage)
            Log.d("",modelImage)
        }else{
            textViewPlayer.visibility = View.GONE
            initPlayer(true)
        }
        if (savedInstanceState != null) {
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }
        if (isFullscreen) {
            openFullscreen()
        }
    }

    //Init´s
    private fun initFullScreenButton() {
        fullScreen.setOnClickListener {
            if (!isFullscreen) {
                openFullscreen()
            } else {
                closeFullscreen()
            }
        }
    }
    private fun initPlayer(starting:Boolean) {
        trackSelector = DefaultTrackSelector(this)
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSize(MAX_WIDTH, MAX_HEIGHT))
        exoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build().apply {
            playWhenReady = isPlayerPlaying
            setMediaItem(mediaItem)
            prepare()
            if (starting){
                prepare()
                play()
            }else{
                prepare()
                pause()
            }
        }
        exoPlayer.addListener(object : Player.Listener {
            @Deprecated("Deprecated in Java")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        progressBarPlayer.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        progressBarPlayer.visibility = View.GONE
                        hdQuality.visibility = View.VISIBLE
                    }
                    ExoPlayer.STATE_IDLE -> {
                        progressBarPlayer.visibility = View.VISIBLE
                        //play.visibility = View.VISIBLE
                    }
                    Player.STATE_ENDED -> {
                        progressBarPlayer.visibility = View.GONE
                        play.visibility = View.VISIBLE
                        TODO()
                    }
                }
            }
        })
        //exoPlayer.playWhenReady = true
        playerView.setOnClickListener  {
            if (exoPlayer.isPlaying) {
                Toast.makeText(applicationContext, "Pause", Toast.LENGTH_SHORT).show()
                play.visibility = View.VISIBLE
                exoPlayer.pause()
            } else {
                Toast.makeText(applicationContext, "Play", Toast.LENGTH_SHORT).show()
                play.visibility = View.GONE
                exoPlayer.play()
            }
        }
        play.setOnClickListener {
            if (exoPlayer.isPlaying) {
                Toast.makeText(applicationContext,"Pause", Toast.LENGTH_SHORT).show()
                play.visibility = View.VISIBLE
                exoPlayer.pause()
            } else {
                Toast.makeText(applicationContext,"Play", Toast.LENGTH_SHORT).show()
                play.visibility = View.GONE
                exoPlayer.play()
            }
        }
        playerView.player = exoPlayer
      ///  if(isFullscreen) openFullscreen()

    }
    private fun initStatusViews(){
        progressBarPlayer = playerView.findViewById(R.id.progressBarPlayer)
        textViewPlayer = playerView.findViewById(R.id.textViewPlayer)
        play = playerView.findViewById(R.id.exo_play)
    }
    private fun initImageViews(){
        hdQuality = playerView.findViewById(R.id.exo_quality)
        volumeSeekBar = playerView.findViewById(R.id.volume_seekbar)
        volumeMute = playerView.findViewById(R.id.exo_mute)
        fullScreen = playerView.findViewById(R.id.exo_fullscreen)
    }
    private fun initActionBar(){
        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "$modelName's Cam"
        supportActionBar?.subtitle = "Suddenly we got online #asian #petite #bigcock #smalltits #natural"
    }

    //CHROMECAST PARTS
    private fun isCastConnected(): Boolean {
        val castSession = castContext
            .sessionManager
            .currentCastSession
        return (castSession != null) && castSession.isConnected
    }

    private fun playOnChromeCast(){
        val videoUrl =
            "https://github.com/mediaelement/mediaelement-files/blob/master/big_buck_bunny.mp4?raw=true"
        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, "Test Stream")
        movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist")
        movieMetadata.addImage(WebImage(Uri.parse("https://github.com/mkaflowski/HybridMediaPlayer/blob/master/images/cover.jpg?raw=true")))
        val mediaInfo = MediaInfo.Builder(videoUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(MimeTypes.VIDEO_UNKNOWN)
            .setMetadata(movieMetadata).build()
        val mediaItems = arrayOf(MediaQueueItem.Builder(mediaInfo).build())
        val castPlayer = CastPlayer(castContext)
        castPlayer.setSessionAvailabilityListener(object : SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                castPlayer.loadItems(mediaItems, 0, playbackPosition, Player.REPEAT_MODE_OFF)
            }
            override fun onCastSessionUnavailable() {}
        })
    }

    private fun loadRemoteMedia(position: Int, autoPlay: Boolean) {
        val remoteMediaClient = castSession.remoteMediaClient ?: return

        remoteMediaClient.registerCallback(object : RemoteMediaClient.Callback() {
            override fun onStatusUpdated() {
                val intent = Intent(this@DetailsActivity, ExpandedControlsActivity::class.java)
                startActivity(intent)
                remoteMediaClient.unregisterCallback(this)
            }
        })
        class MyActivity : FragmentActivity() {
            @SuppressLint("RestrictedApi")
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                return (CastContext.getSharedInstance(applicationContext)
                    .onDispatchVolumeKeyEventBeforeJellyBean(event)
                        || super.dispatchKeyEvent(event))
            }
        }
        remoteMediaClient.load(
            MediaLoadRequestData.Builder()
                //  .setMediaInfo(mSelectedMedia)
                .setAutoplay(autoPlay)
                .setCurrentTime(position.toLong()).build()
        )
    }

    private fun startPlaybackOnChromecast(title:String, subtitle:String, link: String, preview:String) {
        // var mediaMetadata: MediaMetadata? = null
        //         mediaMetadata?.putString(MediaMetadata.KEY_TITLE, title)
        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title)
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, subtitle)
        mediaMetadata.putString(MediaMetadata.MEDIA_TYPE_PHOTO.toString(), preview)
        //  mediaMetadata?.putString(MediaMetadata.KEY_SUBTITLE, subtitle)
        Log.i("Chromecast", "use image: $preview")
        if(TextUtils.isEmpty(preview)){
            mediaMetadata.addImage(WebImage(Uri.parse("https://roomimg.stream.highwebmedia.com/ri/alyce_pettit.jpg?1662244170")))
        }else{
            mediaMetadata.addImage(WebImage(Uri.parse(preview)))
        }
        Log.i("cast", "play $link")
        val mediaInfo = MediaInfo.Builder(link)
            .setContentType("video/m3u8")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setMetadata(mediaMetadata)
            .build()
        castContext.sessionManager
            .currentCastSession
            ?.remoteMediaClient
            ?.load(mediaInfo, true)
    }

    //Audio
    @RequiresApi(Build.VERSION_CODES.P)
    private fun audioManager(){
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        minimumVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        maximumVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        volumeMute.setOnClickListener(){
            if (!isMute){
                isMute = true
                saveVolume = volumeSeekBar.progress
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                volumeSeekBar.progress = 0
                currentVolume = 0
                volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_off_24dp))
            } else {
                if (saveVolume == 0) saveVolume = 50
                isMute = false
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, saveVolume, 0)
                volumeSeekBar.progress = saveVolume
                currentVolume = saveVolume
                volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_down_24dp))
            }
        }
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0)
                currentVolume = i
                volumeSeekBar.progress = currentVolume
                seekBar.progress = i
                Log.d("Tag ", i.toString())
                if (seekBar.progress == 0){
                    volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_off_24dp))
                } else if(seekBar.progress <= 15){
                    volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_mute_24dp))
                }else if(seekBar.progress <= 35){
                    volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_down_24dp))
                }else if(seekBar.progress >= 75){
                    volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_up_24dp))
                }
                when (i) {
                    0 -> {
                        volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_off_24dp))
                    }
                    15 -> {
                        volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_mute_24dp))
                    }
                    35 -> {
                        volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_down_24dp))
                    }
                    75 -> {
                        volumeMute.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.volume_up_24dp))
                    }
                }

            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ){
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            currentVolume = volumeSeekBar.progress
        }
        return super.onKeyDown(keyCode, event)
    }

    //PICTURE IN PICTURE MODE!
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        isPipMode = !isInPictureInPictureMode
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }
    fun checkPIPPermission() {
        isPipMode = isInPictureInPictureMode
        if(!isInPictureInPictureMode) {
            onBackPressed()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        createPIPMode()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPIPMode() {
        startPictureInPictureWithRatio(this)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startPictureInPictureWithRatio(activity: Activity) {
        activity.enterPictureInPictureMode(
            PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build())
    }

    //Override
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentWindowIndex)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.getString(STATE_RESUME_POSITION, playbackPosition.toString())
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
        playerView.player?.stop()
        exoPlayer.release()
        playerView.player?.release()
    }
    override fun onStop() {
        super.onStop()
        exoPlayer.stop()
        playerView.player?.stop()
        exoPlayer.release()
        playerView.player?.release()
    }
    override fun supportFinishAfterTransition() {
        super.supportFinishAfterTransition()
        supportFinishAfterTransition()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.detail_menu, menu)
        CastButtonFactory.setUpMediaRouteButton(
            applicationContext,
            menu,
            R.id.media_route_menu_item
        )
        return true
    }
    override fun onResume() {
        super.onResume()
        if( CastContext.getSharedInstance(this).sessionManager.currentCastSession != null
            && CastContext.getSharedInstance(this).sessionManager.currentCastSession?.remoteMediaClient != null ) {
            castSession = sessionManager.currentCastSession!!
            sessionManager.addSessionManagerListener(sessionManagerListener, CastSession::class.java)
        }else
            exoPlayer.seekToDefaultPosition()
        exoPlayer.play()
        exoPlayer.playWhenReady = true
        super.onResume()
    }
    override fun onPause() {
        if( CastContext.getSharedInstance(this).sessionManager.currentCastSession != null
            && CastContext.getSharedInstance(this).sessionManager.currentCastSession?.remoteMediaClient != null ) {
            sessionManager.removeSessionManagerListener(sessionManagerListener, CastSession::class.java)
            castSession = null!!
        }else
            exoPlayer.pause()
        exoPlayer.playWhenReady = false
        super.onPause()
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if(isFullscreen){
            closeFullscreen()
            return
        }else if(!isPipMode!!) {
            enterPictureInPictureMode()
            isPipMode = true
        } else {
            super.onBackPressed()
        }
    }
    //Override  -  ChromeCast
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return (CastContext.getSharedInstance(this)
            .onDispatchVolumeKeyEventBeforeJellyBean(event)
                || super.dispatchKeyEvent(event))
    }

    //ExpPlayer Play & Pause
    fun pausePlayer() {
        exoPlayer.playWhenReady = false
        exoPlayer.playbackState
    }
    fun startPlayer() {
        exoPlayer.playWhenReady = true
        exoPlayer.playbackState
    }

    //FullScreen
    @SuppressLint("SourceLockedOrientationActivity")
    private fun openFullscreen() {
        // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        fullScreen.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.fullscreen_exit_24dp))
        // playerView.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        Toast.makeText(this, "Open Fullscreen",Toast.LENGTH_SHORT).show()
        val params: ConstraintLayout.LayoutParams = playerView.layoutParams as ConstraintLayout.LayoutParams
        params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
        params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        playerView.layoutParams = params
        supportActionBar?.hide()
        hideSystemUi()
        playerView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        isFullscreen = true
    }

    private fun closeFullscreen() {
        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        Toast.makeText(this, "Closed Fullscreen",Toast.LENGTH_SHORT).show()
        fullScreen.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.fullscreen_24dp))
        // playerView.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        val params: ConstraintLayout.LayoutParams = playerView.layoutParams as ConstraintLayout.LayoutParams
        params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
        params.height = playerView.minimumHeight
        playerView.layoutParams = params
        supportActionBar?.show()
        playerView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        isFullscreen = false
    }

    private fun hideSystemUi() {
        playerView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    //QUALITY POPUP DIALOG PARTS
    private fun initPopupQuality() {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo
        var videoRenderer : Int? = null

        if(mappedTrackInfo == null) return else hdQuality.visibility = View.VISIBLE
        for(i in 0 until mappedTrackInfo.rendererCount) {
            if(isVideoRenderer(mappedTrackInfo, i)) {
                videoRenderer = i
            }
        }
        if(videoRenderer == null){
            hdQuality.visibility = View.GONE
            return
        }
        val trackSelectionDialogBuilder = TrackSelectionDialogBuilder(this, getString(R.string.qualitySelector), trackSelector, videoRenderer)
        trackSelectionDialogBuilder.setTrackNameProvider {
            getString(R.string.exo_track_resolution_pixel, it.height)
        }
        trackDialog = trackSelectionDialogBuilder.build()

    }

    private fun isVideoRenderer(mappedTrackInfo: MappingTrackSelector.MappedTrackInfo, rendererIndex: Int): Boolean {
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        if (trackGroupArray.length == 0) {
            return false
        }
        val trackType = mappedTrackInfo.getRendererType(rendererIndex)
        return C.TRACK_TYPE_VIDEO == trackType
    }

}