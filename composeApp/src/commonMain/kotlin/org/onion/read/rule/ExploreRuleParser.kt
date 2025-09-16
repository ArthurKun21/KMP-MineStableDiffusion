package org.onion.read.rule

import com.dokar.quickjs.binding.function
import com.dokar.quickjs.quickJs
import kotlinx.coroutines.runBlocking
import org.onion.read.constant.JsPattern.JS_PATTERN
import org.onion.read.constant.JsPattern.PAGE_PATTERN

class ExploreRuleParser(var ruleUrl: String,val page: Int? = null) {

    private suspend fun analyzeJs() {
        // 1. 初始化累加器和游标
        var accumulatedResult = ruleUrl // 使用 Any 类型以匹配 evalJS 的返回类型
        var currentIndex = 0

        // 提取重复的逻辑到一个局部函数中，使代码更清晰
        fun processTextSegment(segment: String, currentResult: String): String {
            val trimmedSegment = segment.trim()
            return if (trimmedSegment.isNotEmpty()) {
                trimmedSegment.replace("@result", currentResult)
            } else {
                currentResult // 如果片段为空，则结果不变
            }
        }

        // 2. 查找所有匹配项并遍历
        JS_PATTERN.findAll(ruleUrl).forEach { matchResult ->
            // 2a. 处理上一个匹配结束到当前匹配开始之间的“间隔”文本
            val textBeforeMatch = ruleUrl.substring(currentIndex, matchResult.range.first)
            accumulatedResult = processTextSegment(textBeforeMatch, accumulatedResult)

            // 2b. 处理当前匹配到的JS部分
            val jsCode = matchResult.groupValues[2].ifEmpty { matchResult.groupValues[1] }
            quickJs {
                function("result"){
                    accumulatedResult
                }
                accumulatedResult = evaluate(jsCode)
            }
            // 2c. 更新游标到当前匹配的末尾
            currentIndex = matchResult.range.last + 1

        }

        // 3. 处理最后一个匹配到字符串末尾的“尾部”文本
        if (currentIndex < ruleUrl.length) {
            val tailingText = ruleUrl.substring(currentIndex)
            accumulatedResult = processTextSegment(tailingText, accumulatedResult)
        }

        ruleUrl = accumulatedResult.toString()
    }


    /**
     * 替换关键字,页数,JS
     */
    private suspend fun replaceKeyPageJs() { //先替换内嵌规则再替换页数规则，避免内嵌规则中存在大于小于号时，规则被切错
        //js
        if (ruleUrl.contains("{{") && ruleUrl.contains("}}")) {
            val analyze = CommonRuleParser(ruleUrl) //创建解析
            //替换所有内嵌{{js}}
            val url = analyze.innerRule("{{", "}}") {
                val jsEval = runBlocking {
                    quickJs {
                        evaluate<Any>(it)
                    }
                }
                when {
                    jsEval is String -> jsEval
                    jsEval is Double && jsEval % 1.0 == 0.0 -> jsEval.toInt().toString()
                    else -> jsEval.toString()
                }
            }
            if (url.isNotEmpty()) ruleUrl = url
        }
        //page
        page?.let { currentPage ->
            // 使用 Regex.replace 替换所有页码块，这比手动循环更安全、更简洁
            ruleUrl = JS_PATTERN.replace(ruleUrl) { matchResult ->
                // matchResult.groupValues[1] 包含 < 和 > 之间的页码字符串，例如 "1,2,3"
                val pagesString = matchResult.groupValues[1]
                val pages = pagesString.split(',').map { it.trim() }

                if (pages.isEmpty()) {
                    "" // 如果 pages 列表为空，则用空字符串替换
                } else {
                    // 如果当前页码在范围内，则使用对应页码；否则使用最后一页。
                    if (currentPage > 0 && currentPage <= pages.size) {
                        pages[currentPage - 1]
                    } else {
                        pages.last()
                    }
                }
            }
        }
    }
}