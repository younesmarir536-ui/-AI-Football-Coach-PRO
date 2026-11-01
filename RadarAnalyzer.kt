package com.aifootballcoach.pro

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt

/**
 * Lightweight first-pass radar detector.
 * The crop is deliberately configurable because HUD position/scale differs by device.
 * Results are approximate candidates, not guaranteed player identities.
 */
data class RadarPoint(val x: Float, val y: Float, val team: String, val pixelCount: Int)
data class RadarResult(
    val yellowPlayers: List<RadarPoint>,
    val bluePlayers: List<RadarPoint>,
    val ballCandidate: RadarPoint?,
    val confidenceNote: String
)

object RadarAnalyzer {
    // Initial radar crop: lower middle of the screen. Tune after testing on target device.
    fun cropRadar(frame: Bitmap): Bitmap {
        val left = (frame.width * 0.34f).toInt().coerceIn(0, frame.width - 1)
        val right = (frame.width * 0.68f).toInt().coerceIn(left + 1, frame.width)
        val top = (frame.height * 0.78f).toInt().coerceIn(0, frame.height - 1)
        val bottom = (frame.height * 0.98f).toInt().coerceIn(top + 1, frame.height)
        return Bitmap.createBitmap(frame, left, top, right-left, bottom-top)
    }

    fun analyze(radar: Bitmap): RadarResult {
        val w = radar.width
        val h = radar.height
        // Downsample to reduce CPU cost.
        val step = (minOf(w, h) / 100).coerceAtLeast(1)
        val yellow = ArrayList<Pair<Int,Int>>()
        val blue = ArrayList<Pair<Int,Int>>()
        for (y in 0 until h step step) for (x in 0 until w step step) {
            val c = radar.getPixel(x,y)
            val r = Color.red(c); val g = Color.green(c); val b = Color.blue(c)
            if (r > 185 && g > 155 && b < 105 && r > b * 1.7) yellow.add(x to y)
            if (b > 125 && b > r * 1.25 && b > g * 1.10) blue.add(x to y)
        }
        val yellowClusters = clusterApprox(yellow, step, w, h, "yellow")
        val blueClusters = clusterApprox(blue, step, w, h, "blue")
        // The four-arc ball marker is a yellow shape; pick a compact high-density yellow candidate.
        // It is intentionally not labelled certain until validated against real match frames.
        val ball = yellowClusters.maxByOrNull { it.pixelCount }
        return RadarResult(
            yellowPlayers = yellowClusters,
            bluePlayers = blueClusters,
            ballCandidate = ball,
            confidenceNote = "تعرّف أولي بالألوان؛ علامة الكرة ذات الأقواس الأربعة تحتاج معايرة واختباراً."
        )
    }

    private fun clusterApprox(
        pixels: List<Pair<Int,Int>>, step: Int, w: Int, h: Int, team: String
    ): List<RadarPoint> {
        if (pixels.isEmpty()) return emptyList()
        // Spatial bins approximate connected components at low resolution.
        val bins = LinkedHashMap<Pair<Int,Int>, MutableList<Pair<Int,Int>>>()
        val cell = (step * 3).coerceAtLeast(3)
        for (p in pixels) {
            val key = (p.first / cell) to (p.second / cell)
            bins.getOrPut(key) { mutableListOf() }.add(p)
        }
        return bins.values.filter { it.size >= 2 }.map { group ->
            val cx = group.map { it.first }.average().toFloat()
            val cy = group.map { it.second }.average().toFloat()
            RadarPoint(cx / w, cy / h, team, group.size)
        }
    }
}
