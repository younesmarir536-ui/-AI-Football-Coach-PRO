package com.aifootballcoach.pro

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager

class CaptureService : Service() {
    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var worker: HandlerThread? = null
    private var handler: Handler? = null
    private var lastFrameAt = 0L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        val notification = if (Build.VERSION.SDK_INT >= 26)
            Notification.Builder(this, "coach_capture")
                .setContentTitle("AI Football Coach PRO")
                .setContentText("التقاط الشاشة يعمل بإذنك — مرحلة تجهيز الرادار")
                .setSmallIcon(android.R.drawable.ic_media_play).build()
        else Notification.Builder(this).setContentTitle("AI Football Coach PRO")
            .setContentText("التقاط الشاشة يعمل").setSmallIcon(android.R.drawable.ic_media_play).build()
        startForeground(7, notification)

        val code = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        @Suppress("DEPRECATION")
        val data = intent?.getParcelableExtra<Intent>("captureData")
        if (code == Activity.RESULT_OK && data != null && projection == null) {
            val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projection = manager.getMediaProjection(code, data)
            startFrameReader()
        }
        return START_NOT_STICKY
    }

    private fun startFrameReader() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi
        worker = HandlerThread("CoachFrameWorker").also { it.start() }
        handler = Handler(worker!!.looper)
        reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        reader?.setOnImageAvailableListener({ source ->
            var image: Image? = null
            try {
                image = source.acquireLatestImage()
                val now = System.currentTimeMillis()
                if (image != null && now - lastFrameAt >= 500) {
                    lastFrameAt = now
                    val plane = image.planes[0]
                    val buffer = plane.buffer
                    val pixelStride = plane.pixelStride
                    val rowStride = plane.rowStride
                    val rowPadding = rowStride - pixelStride * image.width
                    val paddedWidth = image.width + rowPadding / pixelStride
                    val padded = Bitmap.createBitmap(paddedWidth, image.height, Bitmap.Config.ARGB_8888)
                    padded.copyPixelsFromBuffer(buffer)
                    val frame = Bitmap.createBitmap(padded, 0, 0, image.width, image.height)
                    padded.recycle()
                    val radar = RadarAnalyzer.cropRadar(frame)
                    val result = RadarAnalyzer.analyze(radar)
                    CoachFrameStats.lastFrameTime = now
                    CoachFrameStats.lastWidth = image.width
                    CoachFrameStats.lastHeight = image.height
                    CoachFrameStats.lastRadarYellowCount = result.yellowPlayers.size
                    CoachFrameStats.lastRadarBlueCount = result.bluePlayers.size
                    CoachFrameStats.lastBallCandidate = result.ballCandidate != null
                    radar.recycle()
                    frame.recycle()
                }
            } catch (_: Exception) {
            } finally { image?.close() }
        }, handler)
        display = projection?.createVirtualDisplay("CoachScreenCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader?.surface, null, handler)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("coach_capture", "Screen analysis", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    override fun onDestroy() {
        display?.release(); display = null
        reader?.close(); reader = null
        projection?.stop(); projection = null
        worker?.quitSafely(); worker = null; handler = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}

object CoachFrameStats {
    @Volatile var lastFrameTime: Long = 0
    @Volatile var lastWidth: Int = 0
    @Volatile var lastHeight: Int = 0
    @Volatile var lastRadarYellowCount: Int = 0
    @Volatile var lastRadarBlueCount: Int = 0
    @Volatile var lastBallCandidate: Boolean = false
}
