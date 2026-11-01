package com.aifootballcoach.pro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private val captureRequest = 4102
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 40, 28, 24) }
        layout.addView(TextView(this).apply { text = "AI Football Coach PRO\nالإصدار التجريبي 0.2"; textSize = 24f })
        layout.addView(TextView(this).apply {
            text = "المرحلة الحالية: تجهيز التقاط الشاشة ومنطقة الرادار. التحليل التكتيكي وتتبع اللاعبين ما زالا قيد التطوير."
            textSize = 16f
        })
        layout.addView(Button(this).apply {
            text = "تفعيل النافذة العائمة"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else startService(Intent(this@MainActivity, CoachOverlayService::class.java))
            }
        })
        layout.addView(Button(this).apply {
            text = "السماح بالتقاط الشاشة"
            setOnClickListener {
                val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                startActivityForResult(manager.createScreenCaptureIntent(), captureRequest)
            }
        })
        setContentView(layout)
    }
    @Deprecated("Deprecated by Android, retained for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == captureRequest && resultCode == RESULT_OK && data != null) {
            startForegroundService(Intent(this, CaptureService::class.java).apply {
                putExtra("resultCode", resultCode); putExtra("captureData", data)
            })
        }
    }
}
