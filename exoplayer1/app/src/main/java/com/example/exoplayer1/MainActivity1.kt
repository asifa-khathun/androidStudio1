package com.example.exoplayer


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerView
import com.example.exoplayer1.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.exoplayer2.*
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem


import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private var delayTime = 2000
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private lateinit  var playerView: PlayerView
    private  var map = mutableMapOf<Long,Long>()
    private var previous = 0L
    private lateinit var value2:String
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var urlType:URLType
    private lateinit var mediaSource: MediaSource
    private lateinit var progress: DefaultTimeBar
    private lateinit var  go_live:TextView
    private lateinit var graph:LineChart
    private  var checkCurrentPosition:Boolean = false
    private var xvalues = ArrayList<String>()
    var graphPoints = ArrayList<Entry>()
    private var rowCount=0
    private var duration:Float =0f
    private var checkPlayerStateEnded:Boolean = false
    private lateinit var graphDataSet:LineDataSet
    private var count:Float=0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        HttpsTrustManager.allowAllSSL()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        constraintLayout = findViewById(R.id.constraintlayoutroot)
        playerView = findViewById(R.id.exoPlayerview)
        go_live = findViewById(R.id.go_live)
        progress = findViewById(R.id.exo_progress)
        graph = findViewById(R.id.Linechart)
        go_live.setOnClickListener {

            count =100f
            checkCurrentPosition=false

            simpleExoPlayer.seekTo(simpleExoPlayer.duration)
            go_live.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))

        }
        chartlistener()
        initplayer()
    }
    fun setLinechartData(x:Float, y:Float)
    {
        graphPoints.add(Entry(x,y))
        graphDataSet = LineDataSet(graphPoints,"")
        graphDataSet.color = resources.getColor(R.color.teal_200)
        graphDataSet.setDrawFilled(true)
        val fillGradient = ContextCompat.getDrawable(this, R.drawable.green_gradient)
        graphDataSet.fillDrawable = fillGradient
        val data = LineData(graphDataSet)
        graph.data = data
        graph.animateXY(0,0)
        graph.axisLeft.setDrawGridLines(false)
        graph.axisRight.setDrawGridLines(false)
        graph.xAxis.isEnabled = false
        graph.axisRight.isEnabled = false
        graph.axisLeft.isEnabled = false
        graph.description.isEnabled = false
        graphDataSet.setDrawValues(false)
    }
    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer.removeListener(playerlistener)
        simpleExoPlayer.pause()
        simpleExoPlayer.clearMediaItems()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        runnable =  object :Runnable{
            override fun run() {
                if(checkPlayerStateEnded==false)
                {
                    var ar = assets
                    var im = ar.open("tests-example.xls")//inputstream
                    var fs = POIFSFileSystem(im)
                    var xlwb = HSSFWorkbook(fs)
                    var xlws = xlwb.getSheetAt(0)

                    //var myfile = File("C://Users//sai.rohith//AndroidStudioProjects//exoplayer//app//src//main//assets//Book1.xlsx")
                    // var istream = FileInputStream(myfile)
                    // val stream = FileInputStream("C:\\Users\\sai.rohith\\AndroidStudioProjects\\exoplayer\\app\\src\\main\\assets\\Book1.xlsx")
                    //val xlwb = .create(im)
                    //val xlws = xlwb.getSheetAt(0)
                    if(rowCount>=10)
                    {
                        value2 = xlws.getRow(9).getCell(0).toString()
                    }
                    else
                    {
                        value2 = xlws.getRow(rowCount).getCell(0).toString()
                    }
                    Log.i("excellvalues",value2)
                    Log.i("positionvalue",simpleExoPlayer.currentPosition.toString())
                    if(value2.toFloat()<=simpleExoPlayer.currentPosition.toFloat()) {
                        count = count + 100f
                        setLinechartData(
                            (simpleExoPlayer.currentPosition/1000f)/(simpleExoPlayer.duration/1000f),
                            count
                        )
                        map.put(simpleExoPlayer.currentPosition, previous)
                        previous = simpleExoPlayer.currentPosition
                        rowCount++
                    }
                }
                handler?.postDelayed(runnable, delayTime.toLong())
            }

        }
        handler.postDelayed(runnable,delayTime.toLong())
        super.onResume()

        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.play()
    }

    override fun onPause() {
        handler.removeCallbacks(runnable)
        super.onPause()

        simpleExoPlayer.pause()
        simpleExoPlayer.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayer.pause()
        simpleExoPlayer.playWhenReady = false
    }

    private  val playerlistener = object: Player.Listener{

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if(playbackState == Player.STATE_ENDED)
            {
                handler.removeCallbacks(runnable)
                checkPlayerStateEnded = true
                go_live.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.purple_500))
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if(isPlaying)
            {


            }

            if(simpleExoPlayer.currentPosition>simpleExoPlayer.duration)
            {
                checkCurrentPosition = true
            }

            if(isPlaying and checkCurrentPosition)
            {
                go_live.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.teal_200))
            }
        }

        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            if(urlType == URLType.Mp4)
            {
                playerView.useController = true
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Toast.makeText(this@MainActivity,"${error.message}",Toast.LENGTH_LONG).show()
        }
    }

    private fun initplayer()
    {
        var count = 0.00f
        simpleExoPlayer = SimpleExoPlayer.Builder(this).build()
        simpleExoPlayer.addListener(playerlistener)

        createmediasource()
        simpleExoPlayer.setMediaSource(mediaSource)

        simpleExoPlayer.prepare()
        //var resumepostion = Math.max(0,simpleExoPlayer.contentPosition)
        //simpleExoPlayer.seekTo(resumepostion)

        playerView.player = simpleExoPlayer
        duration = simpleExoPlayer.duration/1000f
        graph.xAxis.axisMaximum = (simpleExoPlayer.duration/1000f)/ (simpleExoPlayer.duration/1000f)
    }

    private fun createmediasource()
    {
        urlType = URLType.Mp4
        //urlType.url="https://www.youtube.com/watch?v=dp8PhLsUcFE"
        //urlType.url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
        //urlType.url = "https://exoplayerdemo.s3.amazonaws.com/video.mp4"
        var uri:Uri = RawResourceDataSource.buildRawResourceUri(R.raw.video)
        val datasorucefactory: com.google.android.exoplayer2.upstream.DataSource.Factory = DefaultDataSourceFactory(this,
            Util.getUserAgent(this,applicationInfo.name))
        mediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory()).createMediaSource(MediaItem.fromUri("https://cdn.theoplayer.com/video/big_buck_bunny/stream-4-6000000/index.m3u8"))
        // mediaSource = ProgressiveMediaSource.Factory(datasorucefactory).createMediaSource(MediaItem.fromUri(uri))
    }

    private  fun chartlistener()
    {
        graph.setOnChartValueSelectedListener(object: OnChartValueSelectedListener{
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                graphDataSet.setDrawHorizontalHighlightIndicator(false)
                graphDataSet.setDrawVerticalHighlightIndicator(false)
                val x = e?.x
                val y = e?.y
                if (x != null) {
                    var intent = Intent(this@MainActivity,clipvideo::class.java)
                    var res= (x*1000f)*(simpleExoPlayer.duration/1000f)
                    Log.i("mapvalue", map[res.toLong()].toString())
                    Log.i("mapvalue1",(res.toLong()).toString())
                    intent.putExtra("startpoint",map[res.toLong()]!!)
                    intent.putExtra("endpoint",res.toLong())

                    startActivity(intent)
                    simpleExoPlayer.seekTo((((x * 1000f)*((simpleExoPlayer.duration/1000f))).toLong()))
                    graphDataSet.setDrawHorizontalHighlightIndicator(false)
                    graphDataSet.setDrawVerticalHighlightIndicator(false)
                }
            }

            fun onNothingSelected() {
                Log.i("Entry selected","Nothing selected")
            }
        })
    }
}

enum class URLType(var url: String) {
    Mp4(""),HLS("")

}