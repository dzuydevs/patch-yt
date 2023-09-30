package app.revanced.patches.youtube.seekbar.tapping.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.seekbar.tapping.fingerprints.SeekbarTappingFingerprint
import app.revanced.patches.youtube.seekbar.tapping.fingerprints.SeekbarTappingReferenceFingerprint
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.SEEKBAR
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Enable seekbar tapping",
    description = "Enables tap-to-seek on the seekbar of the video player.",
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
object SeekbarTappingPatch : BytecodePatch(
    setOf(
        SeekbarTappingReferenceFingerprint,
        SeekbarTappingFingerprint
    )
) {
    lateinit var TappingLabel: String

    override fun execute(context: BytecodeContext) {
        SeekbarTappingReferenceFingerprint.result?.let {
            it.mutableMethod.apply {
                TappingLabel = """
                    invoke-static {}, $SEEKBAR->enableSeekbarTapping()Z
                    move-result v0
                    if-eqz v0, :disabled
                    invoke-virtual { p0, v2 }, ${getInstruction<ReferenceInstruction>(it.scanResult.patternScanResult!!.startIndex).reference}
                    invoke-virtual { p0, v2 }, ${getInstruction<ReferenceInstruction>(it.scanResult.patternScanResult!!.endIndex - 1).reference}
                    """
            }
        } ?: throw SeekbarTappingReferenceFingerprint.exception

        SeekbarTappingFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex + 2

                addInstructionsWithLabels(
                    insertIndex,
                    TappingLabel,
                    ExternalLabel("disabled", getInstruction(insertIndex))
                )
            }
        } ?: throw SeekbarTappingFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SEEKBAR_SETTINGS",
                "SETTINGS: ENABLE_SEEKBAR_TAPPING"
            )
        )

        SettingsPatch.updatePatchStatus("enable-seekbar-tapping")

    }
}