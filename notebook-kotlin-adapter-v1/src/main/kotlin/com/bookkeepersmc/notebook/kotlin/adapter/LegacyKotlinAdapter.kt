package com.bookkeepersmc.notebook.kotlin.adapter

import net.fabricmc.loader.language.LanguageAdapter
import org.slf4j.LoggerFactory

private val Logger = LoggerFactory.getLogger("Notebook Kotlin Adapter")

@Deprecated("Old style")
class LegacyKotlinAdapter : LanguageAdapter {
    override fun createInstance(baseClass: Class<*>, options: LanguageAdapter.Options?): Any {
        Logger.warn("$baseClass is using a deprecated language adapter, support for this will be dropped in a future update. Update $baseClass.")
        return baseClass.kotlin.objectInstance ?: run {
            Logger.info("Unable to find Kotlin object instance for ${baseClass.name}, constructing new instance")
            baseClass.newInstance()
        }
    }
}
