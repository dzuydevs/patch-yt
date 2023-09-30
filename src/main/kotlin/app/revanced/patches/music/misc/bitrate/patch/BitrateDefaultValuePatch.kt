package app.revanced.patches.music.misc.bitrate.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage

@Patch(
    name = "Bitrate default value",
    description = "Set the audio quality to \"Always High\" when you first install the app."
)
@Suppress("unused")
object BitrateDefaultValuePatch : ResourcePatch() {
    private const val RESOURCE_FILE_PATH = "res/xml/data_saving_settings.xml"

    override fun execute(context: ResourceContext) {
        context.xmlEditor[RESOURCE_FILE_PATH].use { editor ->
            editor.file.getElementsByTagName("com.google.android.apps.youtube.music.ui.preference.PreferenceCategoryCompat")
                .item(0).childNodes.apply {
                    arrayOf("BitrateAudioMobile", "BitrateAudioWiFi").forEach {
                        for (i in 1 until length) {
                            val view = item(i)
                            if (
                                view.hasAttributes() &&
                                view.attributes.getNamedItem("android:key").nodeValue.endsWith(it)
                            ) {
                                view.attributes.getNamedItem("android:defaultValue").nodeValue =
                                    "Always High"
                                break
                            }
                        }
                    }
                }
        }

    }
}