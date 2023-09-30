package app.revanced.patches.youtube.fullscreen.compactcontrolsoverlay.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.utils.fingerprints.YouTubeControlsOverlayFingerprint
import app.revanced.patches.youtube.utils.resourceid.patch.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.FULLSCREEN
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable compact controls overlay",
    description = "Enables compact control overlay.",
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
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ]
)
@Suppress("unused")
object CompactControlsOverlayPatch : BytecodePatch(
    setOf(YouTubeControlsOverlayFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        YouTubeControlsOverlayFingerprint.result?.let {
            with(
                context
                    .toMethodWalker(it.method)
                    .nextMethod(it.scanResult.patternScanResult!!.startIndex, true)
                    .getMethod() as MutableMethod
            ) {
                val insertIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static {v$targetRegister}, $FULLSCREEN->enableCompactControlsOverlay(Z)Z
                        move-result v$targetRegister
                    """
                )
            }
        } ?: throw YouTubeControlsOverlayFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: FULLSCREEN_SETTINGS",
                "SETTINGS: ENABLE_COMPACT_CONTROLS_OVERLAY"
            )
        )

        SettingsPatch.updatePatchStatus("enable-compact-controls-overlay")

    }
}
