package app.revanced.patches.youtube.player.watermark

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.player.watermark.fingerprints.HideWatermarkFingerprint
import app.revanced.patches.youtube.player.watermark.fingerprints.HideWatermarkParentFingerprint
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.integrations.Constants.COMPONENTS_PATH
import app.revanced.util.integrations.Constants.PLAYER
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Hide channel watermark",
    description = "Hides creator's watermarks on videos.",
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class
    ],
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
object HideChannelWatermarkBytecodePatch : BytecodePatch(
    setOf(HideWatermarkParentFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        HideWatermarkParentFingerprint.result?.let { parentResult ->
            HideWatermarkFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val insertIndex = it.scanResult.patternScanResult!!.endIndex
                    val register = getInstruction<TwoRegisterInstruction>(insertIndex).registerA

                    removeInstruction(insertIndex)
                    addInstructions(
                        insertIndex, """
                            invoke-static {}, $PLAYER->hideChannelWatermark()Z
                            move-result v$register
                            """
                    )
                }
            } ?: throw HideWatermarkFingerprint.exception
        } ?: throw HideWatermarkParentFingerprint.exception

        LithoFilterPatch.addFilter("$COMPONENTS_PATH/WaterMarkFilter;")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: PLAYER_SETTINGS",
                "SETTINGS: HIDE_CHANNEL_WATERMARK"
            )
        )

        SettingsPatch.updatePatchStatus("Hide channel watermark")

    }
}
