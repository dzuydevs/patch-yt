package app.revanced.patches.youtube.video.hdr.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.patches.youtube.video.hdr.fingerprints.HdrCapabilitiesFingerprint
import app.revanced.util.integrations.Constants.VIDEO_PATH

@Patch(
    name = "Disable hdr video",
    description = "Disable HDR video.",
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
object DisableHdrVideoPatch : BytecodePatch(
    setOf(HdrCapabilitiesFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        HdrCapabilitiesFingerprint.result?.let {
            with(
                context
                    .toMethodWalker(it.method)
                    .nextMethod(it.scanResult.patternScanResult!!.endIndex, true)
                    .getMethod() as MutableMethod
            ) {
                addInstructionsWithLabels(
                    0, """
                        invoke-static {}, $VIDEO_PATH/HDRVideoPatch;->disableHDRVideo()Z
                        move-result v0
                        if-nez v0, :default
                        return v0
                        """, ExternalLabel("default", getInstruction(0))
                )
            }
        } ?: throw HdrCapabilitiesFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: VIDEO_SETTINGS",
                "SETTINGS: DISABLE_HDR_VIDEO"
            )
        )

        SettingsPatch.updatePatchStatus("disable-hdr-video")

    }
}
