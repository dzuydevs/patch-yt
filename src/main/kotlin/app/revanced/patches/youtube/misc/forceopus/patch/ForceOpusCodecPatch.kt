package app.revanced.patches.youtube.misc.forceopus.patch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.shared.patch.opus.AbstractOpusCodecsPatch
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.MISC_PATH

@Patch(
    name = "Force OPUS codec",
    description = "Forces the OPUS codec for audios.",
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
    dependencies = [SettingsPatch::class]
)
@Suppress("unused")
object ForceOpusCodecPatch : AbstractOpusCodecsPatch(
    "$MISC_PATH/CodecOverridePatch;->shouldForceOpus()Z"
) {
    override fun execute(context: BytecodeContext) {
        super.execute(context)

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: ENABLE_OPUS_CODEC"
            )
        )

        SettingsPatch.updatePatchStatus("force-opus-codec")

    }
}
