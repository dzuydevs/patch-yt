package app.revanced.patches.youtube.layout.branding.icon

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.resources.IconHelper.customIcon
import app.revanced.util.resources.ResourceHelper.updatePatchStatusIcon

@Patch(
    name = "Custom branding icon Revancify Red",
    description = "Changes the YouTube launcher icon to Revancify Red.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41"
            ]
        )
    ],
    use = false
)
@Suppress("unused")
object CustomBrandingIconRevancifyRedPatch : ResourcePatch() {
    override fun execute(context: ResourceContext) {

        context.customIcon("revancify-red")
        context.updatePatchStatusIcon("revancify_red")

    }
}
