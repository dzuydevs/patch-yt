package app.revanced.patches.youtube.layout.forceheader.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

@Patch(
    name = "Force premium heading",
    description = "Forces premium heading on the homepage.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.22.37",
                "18.23.36",
                "18.24.37",
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39"
            ]
        )
    ],
    dependencies = [SettingsPatch::class],
    use = false
)
@Suppress("unused")
object PremiumHeadingPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        val resDirectory = context["res"]
        if (!resDirectory.isDirectory)
            throw PatchException("The res folder can not be found.")

        val (original, replacement) = "yt_premium_wordmark_header" to "yt_wordmark_header"
        val modes = arrayOf("light", "dark")

        arrayOf("xxxhdpi", "xxhdpi", "xhdpi", "hdpi", "mdpi").forEach { size ->
            val headingDirectory = resDirectory.resolve("drawable-$size")
            modes.forEach { mode ->
                val fromPath = headingDirectory.resolve("${original}_$mode.png").toPath()
                val toPath = headingDirectory.resolve("${replacement}_$mode.png").toPath()

                if (!fromPath.exists())
                    throw PatchException("The file $fromPath does not exist in the resources. Therefore, this patch can not succeed.")
                Files.copy(
                    fromPath,
                    toPath,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }

        val prefs = context["res/xml/revanced_prefs.xml"]
        prefs.writeText(
            prefs.readText()
                .replace(
                    "HEADER_SWITCH",
                    "FORCE_PREMIUM_HEADER"
                ).replace(
                    "header-switch",
                    "force-premium-heading"
                )
        )

        SettingsPatch.updatePatchStatus("force-premium-heading")

    }
}
