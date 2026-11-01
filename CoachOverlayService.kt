package com.aifootballcoach.pro

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class CoachOverlayService : Service() {
    private var wm: WindowManager? = null
    private var view: TextView? = null
    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                   else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x = 16; y = 180 }
        view = TextView(this).apply {
            text = "⚽ المدرب\nيحلل الرادار..."
            textSize = 14f; setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xCC173B68.toInt()); setPadding(18, 14, 18, 14)
        }
        wm?.addView(view, params)
    }
    override fun onDestroy() { view?.let { wm?.removeView(it) }; view = null; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
