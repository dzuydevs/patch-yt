package app.revanced.patches.music.layout.branding.name.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption

@Patch(
    name = "Custom branding Music name",
    description = "Rename the YouTube Music app to the name specified in options.json.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.apps.youtube.music",
            [
                "6.15.52",
                "6.20.51",
                "6.21.51"
            ]
        )
    ],
    dependencies = [RemoveElementsPatch::class]
)
@Suppress("unused")
object CustomBrandingNamePatch : ResourcePatch() {

    private var MusicLongName by stringPatchOption(
        key = "MusicLongName",
        default = "ReVanced Extended Music",
        title = "Application Name of YouTube Music",
        description = "The name of the YouTube Music it will show on your notification panel."
    )

    private var MusicShortName by stringPatchOption(
        key = "MusicShortName",
        default = "RVX Music",
        title = "Application Name of YouTube Music",
        description = "The name of the YouTube Music it will show on your home screen."
    )

    override fun execute(context: ResourceContext) {

        val longName = MusicLongName
            ?: throw PatchException("Invalid app name.")

        val shortName = MusicShortName
            ?: throw PatchException("Invalid app name.")

        context.xmlEditor["res/values/strings.xml"].use { editor ->
            val document = editor.file

            mapOf(
                "app_name" to longName,
                "app_launcher_name" to shortName
            ).forEach { (k, v) ->
                val stringElement = document.createElement("string")

                stringElement.setAttribute("name", k)
                stringElement.textContent = v

                document.getElementsByTagName("resources").item(0).appendChild(stringElement)
            }
        }

    }
}
