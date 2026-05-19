package com.jianji.app.data

data class ParsedTransaction(
    val type: String = "expense",
    val amount: Double = 0.0,
    val channel: String = "微信",
    val category: String = "其他",
    val subCategory: String? = null,
    val merchant: String? = null
)

object TransactionParser {

    private val channelKeywords = mapOf(
        "微信" to listOf("微信支付", "微信", "零钱通", "零钱"),
        "支付宝" to listOf("支付宝", "余额宝", "花呗"),
        "京东" to listOf("京东支付", "京东", "白条")
    )

    private val categoryKeywords = mapOf(
        "餐饮" to listOf(
            "餐厅", "饭店", "美食", "小吃", "火锅", "烧烤", "面馆", "快餐",
            "早餐", "午餐", "晚餐", "宵夜", "咖啡", "奶茶", "甜品", "烘焙",
            "食堂", "外卖", "饭馆", "粥", "粉", "麻辣烫", "串串", "炸鸡",
            "汉堡", "披萨", "寿司", "拉面", "饺子", "包子", "馒头"
        ),
        "交通" to listOf(
            "加油站", "充电", "停车", "高速", "收费站", "服务区", "地铁",
            "公交", "出租车", "打车", "骑行", "共享单车", "哈啰", "滴滴",
            "铁路", "火车票", "机票", "航班", "高铁", "过路费", "ETC"
        ),
        "购物" to listOf(
            "超市", "商场", "便利店", "百货", "杂货", "日用品", "服饰",
            "鞋帽", "化妆品", "数码", "电器", "家居", "文具", "书店"
        ),
        "娱乐" to listOf(
            "电影院", "KTV", "酒吧", "网吧", "游戏", "演出", "演唱会",
            "景点", "门票", "旅游", "酒店", "民宿", "度假"
        ),
        "医疗" to listOf(
            "医院", "药店", "诊所", "体检", "挂号", "药品", "医疗器械"
        ),
        "教育" to listOf(
            "书店", "培训", "学费", "考试", "课程", "教材", "文具"
        ),
        "居住" to listOf(
            "房租", "水电", "燃气", "物业", "维修", "装修", "宽带",
            "话费", "网费", "有线电视"
        )
    )

    private val subCategoryKeywords = mapOf(
        "早餐" to listOf("早餐", "早点", "包子", "豆浆", "油条", "煎饼"),
        "午餐" to listOf("午餐", "午餐", "盒饭", "快餐"),
        "晚餐" to listOf("晚餐", "晚饭"),
        "宵夜" to listOf("宵夜", "夜宵", "烧烤", "深夜"),
        "饮料" to listOf("奶茶", "咖啡", "饮品", "茶饮", "果汁", "可乐", "饮料"),
        "零食" to listOf("零食", "小吃", "甜品", "糖果", "薯片", "饼干"),
        "水果" to listOf("水果", "果切"),
        "充电" to listOf("充电", "充电桩", "充电站"),
        "停车费" to listOf("停车", "停车场"),
        "过路费" to listOf("过路费", "高速", "收费站", "ETC"),
        "地铁" to listOf("地铁"),
        "打车" to listOf("打车", "滴滴", "出租车"),
        "自行车" to listOf("共享单车", "自行车", "骑行", "哈啰")
    )

    fun parse(packageName: String, screenText: String): ParsedTransaction? {
        val cleanedText = screenText.replace("\\s+".toRegex(), " ").trim()
        if (cleanedText.isBlank()) return null

        val channel = detectChannel(packageName, cleanedText)
        val amount = extractAmount(cleanedText) ?: return null
        val merchant = extractMerchant(cleanedText)
        val (category, subCategory) = detectCategory(cleanedText, merchant)

        return ParsedTransaction(
            type = "expense",
            amount = amount,
            channel = channel,
            category = category,
            subCategory = subCategory,
            merchant = merchant
        )
    }

    private fun detectChannel(packageName: String, text: String): String {
        val lowerPackage = packageName.lowercase()

        if (lowerPackage.contains("tencent.mm")) return "微信"
        if (lowerPackage.contains("alipay") || lowerPackage.contains("eg.android")) return "支付宝"
        if (lowerPackage.contains("jingdong") || lowerPackage.contains("jd")) return "京东"

        for ((channel, keywords) in channelKeywords) {
            for (keyword in keywords) {
                if (text.contains(keyword)) return channel
            }
        }

        return "微信"
    }

    private fun extractAmount(text: String): Double? {
        val patterns = listOf(
            Regex("""[¥￥]\s*(\d+(?:\.\d{1,2})?)"""),
            Regex("""(\d+(?:\.\d{1,2})?)\s*元"""),
            Regex("""付款.*?(\d+(?:\.\d{1,2})?)"""),
            Regex("""支付.*?(\d+(?:\.\d{1,2})?)"""),
            Regex("""消费.*?(\d+(?:\.\d{1,2})?)"""),
            Regex("""(\d+(?:\.\d{1,2})?)""")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val value = match.groupValues[1].toDoubleOrNull()
                if (value != null && value > 0 && value < 1000000) {
                    return value
                }
            }
        }

        return null
    }

    private fun extractMerchant(text: String): String? {
        val patterns = listOf(
            Regex("""(?:收款方|商户|向|给)\s*[：:]?\s*(.+?)(?:[，。\s]|$)"""),
            Regex("""付款[给到]?\s*[：:]?\s*(.+?)(?:[，。\s￥¥\d]|$)"""),
            Regex("""-\s*(.+?)\s*[￥¥]""")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val merchant = match.groupValues[1].trim()
                if (merchant.length in 2..30 && !merchant.matches(Regex("""^\d+$"""))) {
                    return merchant
                }
            }
        }

        return null
    }

    private fun detectCategory(text: String, merchant: String?): Pair<String, String?> {
        val combined = "$text ${merchant ?: ""}"

        var bestCategory = "其他"
        var bestSubCategory: String? = null
        var bestScore = 0

        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (combined.contains(keyword)) {
                    if (keyword.length > bestScore) {
                        bestScore = keyword.length
                        bestCategory = category
                    }
                }
            }
        }

        if (bestCategory == "餐饮") {
            for ((subCategory, keywords) in subCategoryKeywords) {
                for (keyword in keywords) {
                    if (combined.contains(keyword)) {
                        if (keyword.length > (bestSubCategory?.length ?: 0)) {
                            bestSubCategory = subCategory
                        }
                    }
                }
            }
        }

        if (bestCategory == "交通") {
            for ((subCategory, keywords) in subCategoryKeywords) {
                for (keyword in keywords) {
                    if (combined.contains(keyword)) {
                        val validTransportation = listOf("充电", "停车费", "过路费", "地铁", "打车", "自行车")
                        if (subCategory in validTransportation && keyword.length > (bestSubCategory?.length ?: 0)) {
                            bestSubCategory = subCategory
                        }
                    }
                }
            }
        }

        return Pair(bestCategory, bestSubCategory)
    }
}
