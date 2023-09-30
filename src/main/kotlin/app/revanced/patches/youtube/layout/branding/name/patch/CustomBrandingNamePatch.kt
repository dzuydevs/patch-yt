package app.revanced.patches.youtube.layout.branding.name.patch

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.resources.ResourceHelper.updatePatchStatusLabel
import app.revanced.patcher.patch.options.types.StringPatchOption.Companion.stringPatchOption

@Patch(
    name = "Custom branding YouTube name",
    description = "Rename the YouTube app to the name specified in options.json.",
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
    dependencies = [
        RemoveElementsPatch::class,
        SettingsPatch::class
    ]
)
@Suppress("unused")
object CustomBrandingNamePatch : ResourcePatch() {
    var YouTubeAppName by stringPatchOption(
            key = "YouTubeAppName",
            default = "ReVanced Extended",
            title = "Application Name of YouTube",
            description = "The name of the YouTube it will show on your home screen."
    )

    override fun execute(context: ResourceContext) {

        val appName = YouTubeAppName
            ?: throw PatchException("Invalid app name.")

        context.xmlEditor["res/values/strings.xml"].use { editor ->
            val document = editor.file

            mapOf(
                "application_name" to appName
            ).forEach { (k, v) ->
                val stringElement = document.createElement("string")

                stringElement.setAttribute("name", k)
                stringElement.textContent = v

                document.getElementsByTagName("resources").item(0).appendChild(stringElement)
            }
        }


        context.updatePatchStatusLabel("$appName")

    }
}
