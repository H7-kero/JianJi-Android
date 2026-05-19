package com.jianji.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.jianji.app.ConfirmTransactionActivity
import com.jianji.app.data.ParsedTransaction
import com.jianji.app.data.TransactionParser

class AutoAccountingService : AccessibilityService() {

    private val targetPackages = listOf(
        "com.tencent.mm",
        "com.eg.android.AlipayGphone",
        "com.jingdong.app.mall"
    )

    private var lastProcessedTime = 0L
    private var lastProcessedAmount = 0.0
    private val debounceMillis = 3000L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName !in targetPackages) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            val source = event.source ?: return
            val screenText = extractText(source)
            source.recycle()

            val now = System.currentTimeMillis()
            if (now - lastProcessedTime < debounceMillis) return

            val parsed = TransactionParser.parse(packageName, screenText) ?: return

            if (parsed.amount == lastProcessedAmount &&
                now - lastProcessedTime < debounceMillis * 2) return

            lastProcessedTime = now
            lastProcessedAmount = parsed.amount

            showConfirmDialog(parsed)
        }
    }

    private fun extractText(node: AccessibilityNodeInfo): String {
        val builder = StringBuilder()
        collectText(node, builder, 0)
        return builder.toString()
    }

    private fun collectText(node: AccessibilityNodeInfo, builder: StringBuilder, depth: Int) {
        if (depth > 30) return

        val text = node.text?.toString()
        if (!text.isNullOrBlank()) {
            builder.append(text).append(" ")
        }

        val contentDesc = node.contentDescription?.toString()
        if (!contentDesc.isNullOrBlank() && contentDesc != text) {
            builder.append(contentDesc).append(" ")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectText(child, builder, depth + 1)
            child.recycle()
        }
    }

    private fun showConfirmDialog(parsed: ParsedTransaction) {
        val intent = Intent(this, ConfirmTransactionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("type", parsed.type)
            putExtra("amount", parsed.amount)
            putExtra("channel", parsed.channel)
            putExtra("category", parsed.category)
            putExtra("subCategory", parsed.subCategory)
            putExtra("merchant", parsed.merchant)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
