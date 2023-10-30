package app.revanced.patches.youtube.layout.doubletaplength

import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.resources.ResourceHelper.addEntryValues
import app.revanced.util.resources.ResourceUtils
import app.revanced.util.resources.ResourceUtils.copyResources

@Patch(
    name = "Custom double tap length",
    description = "Add 'double-tap to seek' value.",
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
    ]
)
@Suppress("unused")
object DoubleTapLengthPatch : ResourcePatch() {
    private val DoubleTapLengthArrays by stringPatchOption(
        key = "DoubleTapLengthArrays",
        default = "3, 5, 10, 15, 20, 30, 60, 120, 180",
        title = "Double-tap to seek Values",
        description = "A list of custom double-tap to seek lengths. Be sure to separate them with commas (,).",
        required = true
    )

    override fun execute(context: ResourceContext) {
        val arrayPath = "res/values-v21/arrays.xml"
        val entriesName = "double_tap_length_entries"
        val entryValueName = "double_tap_length_values"

        /**
         * Copy arrays
         */
        context.copyResources(
            "youtube/doubletap",
            ResourceUtils.ResourceGroup(
                "values-v21",
                "arrays.xml"
            )
        )

        val length = DoubleTapLengthArrays
            ?: throw PatchException("Invalid double-tap length array.")

        val splits = length.replace(" ", "").split(",")
        if (splits.isEmpty()) throw IllegalArgumentException("Invalid double-tap length elements")
        val lengthElements = splits.map { it }
        for (index in 0 until splits.count()) {
            context.addEntryValues(arrayPath, lengthElements[index], entryValueName)
            context.addEntryValues(arrayPath, lengthElements[index], entriesName)
        }

        SettingsPatch.updatePatchStatus("Custom double tap length")

    }
}