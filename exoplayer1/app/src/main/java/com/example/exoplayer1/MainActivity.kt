package com.example.exoplayer1

import android.icu.util.UniversalTimeScale.toLong
import android.os.Bundle
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.*
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.util.Log
import androidx.media3.common.util.Util
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.*
import androidx.media3.exoplayer.hls.DefaultHlsExtractorFactory
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultAllocator
import com.example.exoplayer1.databinding.ActivityMainBinding
import com.google.common.primitives.UnsignedBytes.toInt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.internal.http2.Http2Reader
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Handler
import kotlin.time.Duration


class MainActivity : AppCompatActivity() {
    private lateinit var downloadurl:String
    private lateinit var binding:ActivityMainBinding
    private var player:ExoPlayer?=null
    private lateinit var type:UrlType
    private var trackSelector:DefaultTrackSelector?=null
    private lateinit  var downloadCache:SimpleCache
    //private lateinit var mediasource: HlsMediaSource


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        HttpsTrushManager.allowAllSSL()
        player?.addListener(playerlistener)
        //initplayer()

        /*var timeline = player?.currentTimeline
        var windowCount = player?.currentTimeline?.windowCount
        var periodCount = player?.currentTimeline?.periodCount
        var window: Timeline.Window = Timeline.Window()

        if (timeline != null) {
            //var lastPeriod = timeline.isLastPeriod(0, period, window, REPEAT_MODE_OFF, false)
            Log.i("PeriodCount", periodCount.toString())
            Log.i("WindowCount", windowCount.toString())
        }
        else
        {
            Log.i("tlnull", "yes")
        }*/

        /*var dynamic = Timeline.Window.CREATOR.run { mediasource.isSingleWindow }
        *//*var live = Timeline.Window.CREATOR.run { player?.isCurrentMediaItemLive }
        var seekable = Timeline.Window.CREATOR.run { player?.isCurrentMediaItemSeekable }*//*
        Log.i("dynamic", dynamic.toString())
        *//*Log.i("live", live.toString())
        Log.i("seekable", seekable.toString())*/

        /*var timeLine = Timeline.Window.CREATOR.run { player }

        //timeLine.onSuccess { timeLine->initplayer() }

        if (timeLine != null) {
            //print(timeLine.isCurrentMediaItemDynamic)
            //print(timeLine.isCurrentMediaItemLive)
            //print(timeLine.isCurrentMediaItemSeekable)
            Log.i("dynamic", timeLine.isCurrentMediaItemDynamic.toString())
            Log.i("live", timeLine.isCurrentMediaItemLive.toString())
            Log.i("seekable", timeLine.isCurrentMediaItemSeekable.toString())
            //Log.i("seekable", timeLine.isCurrentMediaItemSeekable.toString())
        }
        if(timeLine == null)
        {
            print("Null timeline")
            Log.i("null", "Null")
        }*/
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initplayer()
        }
        if (Util.SDK_INT >= 21) {
           WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
    }

    /*fun onTimeLineChanged(timeline: Timeline, reason: Int)
    {
        val timeline = player?.currentTimeline
        var totalTime = 0F
        val tempWindow = Timeline.Window()
        if (timeline != null) {
            for (i in 0 until timeline.windowCount)
            {
                val windowDuration = timeline.getWindow(i, tempWindow).durationMs
                totalTime += windowDuration
                //durationList.add(Duration(inWholeSeconds(windowDuration)))
            }
        }
        if (totalTime>0)
        {
            //renderTimeline()
        }
    }*/

    public override fun onResume() {
        super.onResume()
    }


    private fun initplayer()
    {
        GlobalScope.launch {
            var client = OkHttpClient.Builder()
            client.readTimeout(5,TimeUnit.MILLISECONDS)
            client.writeTimeout(5,TimeUnit.MILLISECONDS)
            client.retryOnConnectionFailure(retryOnConnectionFailure = true)
            client.build()

        }
        trackSelector = DefaultTrackSelector(this, AdaptiveTrackSelection.Factory())
        var allocator = DefaultAllocator(false, 64000)
        var loadControl: LoadControl = object:DefaultLoadControl(
            allocator,
            25000,
            30000,
            10000,
            10000,
            60000,
            true,
            10000,
            true
        ){}

        /*val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
        val downloadContentDirectory =
            File(getExternalFilesDir(null),DOWNLOAD_CONTENT_DIRECTORY)
        downloadCache =
            SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), StandaloneDatabaseProvider(this@MainActivity))
            val cacheSink = CacheDataSink.Factory()
                .setCache(downloadCache)
            val upstreamFactory = DefaultDataSource.Factory(this, DefaultHttpDataSource.Factory())
            val downStreamFactory = FileDataSource.Factory()
            val cacheDataSourceFactory  =
                CacheDataSource.Factory()
                    .setCache(downloadCache)
                    .setCacheWriteDataSinkFactory(cacheSink)
                    .setCacheReadDataSourceFactory(downStreamFactory)
                    .setUpstreamDataSourceFactory(upstreamFactory)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)*/


        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector!!).setLoadControl(loadControl).setRenderersFactory(DefaultRenderersFactory(this)).build().also{
            exoPlayer ->

            binding.videoView.player = exoPlayer
            type = UrlType.Hls
           type.url = "https://cph-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
           //"https://cph-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
           //"https://manifest.googlevideo.com/api/manifest/hls_variant/expire/1663069987/ei/wxogY_mOG4iRz7sP-NOYiAM/ip/157.47.112.164/id/lyeyoqwXm5o.2/source/yt_live_broadcast/requiressl/yes/hfr/1/playlist_duration/30/manifest_duration/30/maxh/4320/maudio/1/vprv/1/go/1/pacing/0/nvgoi/1/keepalive/yes/fexp/24001373%2C24007246/dover/11/itag/0/playlist_type/DVR/sparams/expire%2Cei%2Cip%2Cid%2Csource%2Crequiressl%2Chfr%2Cplaylist_duration%2Cmanifest_duration%2Cmaxh%2Cmaudio%2Cvprv%2Cgo%2Citag%2Cplaylist_type/sig/AOq0QJ8wRgIhAOxa9FHt6tMQS13SYtGeD4A1PmwSAG43rrc7KyxQdFORAiEAjJSio1lISty8bKEYvy0asSuLG7ddpb0SU3jQDt4HHfc%3D/file/index.m3u8"
           //https://manifest.googlevideo.com/api/manifest/hls_variant/expire/1662981460/ei/9MAeY4cDgpnj4Q_loqiICw/ip/2409%3A4070%3A4e9d%3A20bd%3Af9c4%3Aac01%3Ac04d%3A57a0/id/8QTO7PJh6qs.1/source/yt_live_broadcast/requiressl/yes/hfr/1/playlist_duration/30/manifest_duration/30/maxh/4320/maudio/1/vprv/1/go/1/pacing/0/nvgoi/1/keepalive/yes/fexp/24001373%2C24007246/dover/11/itag/0/playlist_type/DVR/sparams/expire%2Cei%2Cip%2Cid%2Csource%2Crequiressl%2Chfr%2Cplaylist_duration%2Cmanifest_duration%2Cmaxh%2Cmaudio%2Cvprv%2Cgo%2Citag%2Cplaylist_type/sig/AOq0QJ8wRAIgP3-DRSLwpGX4j7iwxvBIdfYZ3qw9x8L5oNGVIwB9WHgCIGY6LSrWx62z5e5gZwgrsbUH9ttfZrQ4CnVeIhB9F6XE/file/index.m3u8"
           // type.url = "https://manifest.googlevideo.com/api/manifest/dash/expire/1662467805/ei/feoWY_a7JNfA4-EPuJahqAw/ip/157.48.208.255/id/lyeyoqwXm5o.2/source/yt_live_broadcast/requiressl/yes/tx/24268153/txs/24268153%2C24268154%2C24268155/as/fmp4_audio_clear%2Cwebm_audio_clear%2Cwebm2_audio_clear%2Cfmp4_sd_hd_clear%2Cwebm2_sd_hd_clear/vprv/1/pacing/0/keepalive/yes/fexp/24001373%2C24007246/itag/0/playlist_type/DVR/sparams/expire%2Cei%2Cip%2Cid%2Csource%2Crequiressl%2Ctx%2Ctxs%2Cas%2Cvprv%2Citag%2Cplaylist_type/sig/AOq0QJ8wRAIgWVB0Tbw9gl6n42rKH4GuLrXZ2c6mOAhwc8VRSEJUuxMCIDVP6d6YHY3RdAk8d89GpvzZCwMpUx95d4TX0Guqdcw"
            /*var obj = object:YouTubeExtractor(this@MainActivity){
                override fun onExtractionComplete(
                    ytFiles: SparseArray<YtFile>?,
                    videoMeta: VideoMeta?
                ) {*/
                    //var atag = 139
                    //var itag = 140

            /*var loadControl: LoadControl = DefaultLoadControl.Builder().setBufferDurationsMs(PubVar.minBuffer, PubVar.maxBuffer, 2000, 5000)
        .setTargetBufferBytes(C.LENGTH_UNSET).setPrioritizeTimeOverSizeThresholds(false).build()*/

            var item = MediaItem.Builder().setLiveConfiguration(MediaItem.LiveConfiguration.Builder().build()).setUri(type.url).build()
            //var item1 = MediaItem.Builder().setUri(ytFiles?.get(atag)?.url).setLiveConfiguration(MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build()).build()
            var mediasource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
                .setAllowChunklessPreparation(false).createMediaSource(item)
            //cacheDataSourceFactory
            /*var mediasource = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(item)*/


            // var mediasource1 = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory()).createMediaSource(item1)
            // exoPlayer.setMediaSource(MergingMediaSource(true,mediasource,mediasource1))

            exoPlayer.playWhenReady = true
            //exoPlayer.seekTo(0, 0)
            //exoPlayer.setMediaSource(mediasource, true)
            exoPlayer.setMediaSource(mediasource)
            exoPlayer.addListener(playerlistener)
            exoPlayer.prepare()


            /*  }

          }.extract("https://www.youtube.com/watch?v=6gwXWBZgUm0")*/
        }

    }
    enum class UrlType(var url:String)
    {
        Mp4(""),Hls("")
    }
 private  val playerlistener = object :Player.Listener{
     override fun onPlayerErrorChanged(error: PlaybackException?) {
         super.onPlayerErrorChanged(error)

             if(error?.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                 player?.seekToDefaultPosition()
                 player?.prepare()
             }

     }
 }
}