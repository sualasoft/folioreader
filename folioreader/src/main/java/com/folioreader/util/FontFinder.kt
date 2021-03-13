package com.folioreader.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * @author Tyler Sedlar
 */
object FontFinder {

    private var sysFonts: Map<String, File>? = null
    private var LOG_TAG: String? = "FontAdapter"

    @JvmStatic
    fun getSystemFonts(): Map<String, File> {
        if (sysFonts != null) {
            return sysFonts!!
        }

        val fonts = HashMap<String, File>()

        val sysFontDir = File("/system/fonts/")
        val fontSuffix = ".ttf"

        // Collect system fonts
        for (fontFile in sysFontDir.listFiles()) {
            val fontName: String = fontFile.name
            if (fontName.endsWith(fontSuffix)) {
                val key = fontName.subSequence(0, fontName.lastIndexOf(fontSuffix)).toString()
                fonts[key] = fontFile
            }
        }

        sysFonts = fonts

        return fonts
    }

    @JvmStatic
    fun getUserFonts(): Map<String, File> {
        val fonts = HashMap<String, File>()

        val fontDirs = arrayOf(
            File(Environment.getExternalStorageDirectory(), "Fonts/")
        )
        val fontSuffix = ".ttf"

        fontDirs.forEach { fontDir ->
            // Collect user fonts
            if (fontDir.exists() && fontDir.isDirectory) {
                fontDir.walkTopDown()
                    .filter { f -> f.name.endsWith(fontSuffix) }
                    .forEach { fontFile ->
                        val fontName = fontFile.name
                        val key =
                            fontName.subSequence(0, fontName.lastIndexOf(fontSuffix)).toString()
                        fonts[key] = fontFile
                    }
            }
        }
        return fonts
    }

    @JvmStatic
    fun getAssetFonts(context: Context): Map<String, File> {
        Log.i(LOG_TAG, "GETAASSETS")
        val fonts = HashMap<String, File>()
        Log.i(LOG_TAG, "Specify Path")
        val fontDir = context.assets.list("fonts")
        val fontSuffix = ".ttf"

        Log.i(LOG_TAG, "FontList -> ${fontDir!!.last()}")
        for (fontName in fontDir!!) {
            val fontFile = File(context.cacheDir, fontName)
            writeBytesToFile(context.assets.open("fonts/$fontName"), fontFile)
            if (fontName.endsWith(fontSuffix)) {
                val key = fontName.subSequence(0, fontName.lastIndexOf(fontSuffix)).toString()
                fonts[key] = fontFile
            }
        }
        return fonts
    }

    @Throws(IOException::class)
    fun writeBytesToFile(`is`: InputStream, file: File?) {
        var fos: FileOutputStream? = null
        try {
            val data = ByteArray(2048)
            var nbread = 0
            fos = FileOutputStream(file)
            while (`is`.read(data).also({ nbread = it }) > -1) {
                fos.write(data, 0, nbread)
            }
        } catch (ex: Exception) {
//            logger.error("Exception", ex)
        } finally {
            if (fos != null) {
                fos.close()
            }
        }
    }

    @JvmStatic
    fun getFontFile(key: String, context: Context): File? {
        val system = getSystemFonts()
        val user = getUserFonts()
        val asset = getAssetFonts(context)

        return when {
            system.containsKey(key) -> system[key]
            user.containsKey(key) -> user[key]
            asset.containsKey(key) -> asset[key]
            else -> null
        }
    }

    @JvmStatic
    fun isSystemFont(key: String): Boolean {
        return getSystemFonts().containsKey(key)
    }
}