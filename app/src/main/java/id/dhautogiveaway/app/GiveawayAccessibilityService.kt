package id.dhautogiveaway.app

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import java.util.Locale

class GiveawayAccessibilityService : AccessibilityService() {
    // Verified from the supplied Google Play listing: com.anichin.donghub.
    private val donghivePackageId = "com.anichin.donghub"
    private var lastFingerprint: String = ""
    private var lastAlertAt: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        createChannel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        // Only inspect the verified Donghive app; do not read other apps.
        if (donghivePackageId.startsWith("REPLACE_") || event.packageName?.toString() != donghivePackageId) return
        // This starter intentionally does not click UI elements or interact with giveaway entries.
        val root = rootInActiveWindow ?: return
        val collected = mutableListOf<String>()
        collectText(root, collected)
        val allText = collected.joinToString(" ").lowercase(Locale.ROOT)
        if (!allText.contains("giveaway")) return
        val hasClaim = allText.contains("klaim sekarang") || allText.contains("claim") || allText.contains("ambil")
        val hasAvailableSlot = Regex("\\b([1-9][0-9]*)\\s*/\\s*([1-9][0-9]*)\\s*slot\\s*tersisa\\b").containsMatchIn(allText)
        val soldOut = allText.contains("slot habis") || Regex("\\b0\\s*/\\s*[0-9]+\\s*slot\\s*tersisa\\b").containsMatchIn(allText)
        if (hasClaim && hasAvailableSlot && !soldOut) {
            val fingerprint = allText.take(500)
            val now = System.currentTimeMillis()
            if (fingerprint != lastFingerprint || now - lastAlertAt > 60_000L) {
                lastFingerprint = fingerprint
                lastAlertAt = now
                notifyGiveaway("Giveaway mungkin tersedia", "Tombol klaim dan slot tersedia terdeteksi. Periksa Donghive untuk memastikan.")
            }
        }
    }

    private fun collectText(node: AccessibilityNodeInfo, out: MutableList<String>) {
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let(out::add)
        for (i in 0 until node.childCount) node.getChild(i)?.let { child ->
            collectText(child, out)
            child.recycle()
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("giveaway_alerts", "Giveaway alerts", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun notifyGiveaway(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, "giveaway_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1001, notification)
    }

    override fun onInterrupt() = Unit
}
