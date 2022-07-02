package net.oelement.player

import android.Manifest
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    private var isLoading: Boolean = false;
//    private var mediaPlayer: MediaPlayer? = null
    var arrayList_details:ArrayList<Model> = ArrayList();
    var uris: ArrayList<Uri> = ArrayList();
    val client = OkHttpClient()
    private lateinit var connectionLiveData: ConnectionLiveData
    private var videoView: VideoView? = null;
    private var currentVideoIndex: Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()
        videoView = findViewById<VideoView>(R.id.video_view)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)

        videoView?.setOnCompletionListener { mp ->
            var index = currentVideoIndex + 1;
            runOnUiThread {
                if (index < uris.size) {
                    playVideo(uris[index])
                    currentVideoIndex = index
                } else {
                    loadPlayList(PLAYLIST)
                    currentVideoIndex = 0
                }
            }
        }

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this) { isNetworkAvailable ->
            isNetworkAvailable?.let {
                if (it) {
                    runOnUiThread {
                        loadPlayList(PLAYLIST);
                    }
                }
            }
        }
    }

    fun playVideo(uri: Uri) {
        videoView?.setVideoURI(uri);
        videoView?.requestFocus();
        videoView?.start()
    }

    companion object {
        const val PLAYLIST = "https://assets.gold-dev.oelement.net/68e3385f-4527-4f3a-9b8f-0b07b3f107b7/playlist.json"
        const val BASE_RUL = "https://assets.gold-dev.oelement.net";
    }

    fun loadPlayList(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()
        isLoading = true;
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                log(e.localizedMessage);
            }

            override fun onResponse(call: Call, response: Response) {
                var str_response = response.body()!!.string()
                val json_contact: JSONObject = JSONObject(str_response)
                var jsonarray_info: JSONArray = json_contact.getJSONArray("newsAssets")
                var i:Int = 0
                var size:Int = jsonarray_info.length()
                arrayList_details= ArrayList();
                uris.clear();
                for (i in 0.. size-1) {
                    var json_objectdetail:JSONObject=jsonarray_info.getJSONObject(i)
                    var model = Model();
//                    var url = BASE_RUL + "/" + json_objectdetail.getJSONObject("file").getString("directory") + "/" + json_objectdetail.getJSONObject("file").getString("name");
                    var url = BASE_RUL + json_objectdetail.getJSONObject("file").getString("outputHls");
                    model.url = url
                    model.data = json_objectdetail
                    arrayList_details.add(model)
                    uris.add(Uri.parse(url));
                    Log.d("VideoUrl-" + i, url);
                }
                runOnUiThread {
                    if(uris.size > 0) {
                        playVideo(uris[0])
                    }
                }
                isLoading = false;
            }
        })
    }

    fun log(err: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appendLog("\n " + DateTimeFormatter.ISO_INSTANT.format(Instant.now()) + ":" + err)
        } else {
            val sdf = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
            val timestamp = Timestamp(System.currentTimeMillis())
            appendLog("\n " + sdf.format(timestamp) + ":" + err)
        }
    }

    fun appendLog(text: String?) {
        val logFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,  "player_logs.txt")
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
//                logFile.mkdirs()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append(text)
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            Log.d("write file", e.toString())
        }
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ), 1
        )
    }
}