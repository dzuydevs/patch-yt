package app.revanced.patches.youtube.general.layout.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.youtube.utils.litho.patch.LithoFilterPatch
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.bytecode.BytecodeHelper.updatePatchStatus
import app.revanced.util.integrations.Constants.PATCHES_PATH

@Patch(
    name = "Hide layout components",
    description = "Hides general layout components.",
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
        LithoFilterPatch::class,
        SettingsPatch::class
    ]
)
@Suppress("unused")
object LayoutComponentsPatch : BytecodePatch() {
    override fun execute(context: BytecodeContext) {
        LithoFilterPatch.addFilter("$PATCHES_PATH/ads/CommunityPostFilter;")
        LithoFilterPatch.addFilter("$PATCHES_PATH/ads/LayoutComponentsFilter;")
        LithoFilterPatch.addFilter("$PATCHES_PATH/ads/LayoutComponentsUniversalFilter;")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "PREFERENCE: PLAYER_SETTINGS",

                "SETTINGS: HIDE_AUDIO_TRACK_BUTTON",
                "SETTINGS: HIDE_LAYOUT_COMPONENTS"
            )
        )

        SettingsPatch.updatePatchStatus("hide-layout-components")

        context.updatePatchStatus("LayoutComponent")

    }
}
