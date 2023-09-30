package app.revanced.patches.youtube.fullscreen.landscapemode.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.youtube.fullscreen.landscapemode.fingerprints.OrientationParentFingerprint
import app.revanced.patches.youtube.fullscreen.landscapemode.fingerprints.OrientationPrimaryFingerprint
import app.revanced.patches.youtube.fullscreen.landscapemode.fingerprints.OrientationSecondaryFingerprint
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.FULLSCREEN
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Disable landscape mode",
    description = "Disable landscape mode when entering fullscreen.",
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
    dependencies = [SettingsPatch::class],
    use = false
)
@Suppress("unused")
object LandScapeModePatch : BytecodePatch(
    setOf(OrientationParentFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$FULLSCREEN->disableLandScapeMode(Z)Z"

    override fun execute(context: BytecodeContext) {
        OrientationParentFingerprint.result?.classDef?.let { classDef ->
            arrayOf(
                OrientationPrimaryFingerprint,
                OrientationSecondaryFingerprint
            ).forEach {
                it.also { it.resolve(context, classDef) }.result?.injectOverride()
                    ?: throw it.exception
            }
        } ?: throw OrientationParentFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: FULLSCREEN_SETTINGS",
                "SETTINGS: DISABLE_LANDSCAPE_MODE"
            )
        )

        SettingsPatch.updatePatchStatus("disable-landscape-mode")

    }

    fun MethodFingerprintResult.injectOverride() {
        mutableMethod.apply {
            val index = scanResult.patternScanResult!!.endIndex
            val register = getInstruction<OneRegisterInstruction>(index).registerA

            addInstructions(
                index + 1, """
                    invoke-static {v$register}, $INTEGRATIONS_CLASS_DESCRIPTOR
                    move-result v$register
                    """
            )
        }
    }
}
