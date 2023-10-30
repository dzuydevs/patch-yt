package app.revanced.patches.youtube.misc.spoofdimensions

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.misc.spoofdimensions.fingerprints.DeviceDimensionsModelToStringFingerprint
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.integrations.Constants.MISC_PATH
import com.android.tools.smali.dexlib2.util.MethodUtil

@Patch(
    name = "Spoof device dimensions",
    description = "Spoofs the device dimensions in order to unlock higher video qualities " +
            "that may not be available on your device.",
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
object SpoofDeviceDimensionsPatch : BytecodePatch(
    setOf(DeviceDimensionsModelToStringFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$MISC_PATH/SpoofDeviceDimensionsPatch;"

    override fun execute(context: BytecodeContext) {
        DeviceDimensionsModelToStringFingerprint.result?.let { result ->
            result.mutableClass.methods.first { method -> MethodUtil.isConstructor(method) }
                .addInstructions(
                    1, // Add after super call.
                    mapOf(
                        1 to "MinHeightOrWidth", // p1 = min height
                        2 to "MaxHeightOrWidth", // p2 = max height
                        3 to "MinHeightOrWidth", // p3 = min width
                        4 to "MaxHeightOrWidth"  // p4 = max width
                    ).map { (parameter, method) ->
                        """
                            invoke-static { p$parameter }, $INTEGRATIONS_CLASS_DESCRIPTOR->get$method(I)I
                            move-result p$parameter
                            """
                    }.joinToString("\n") { it }
                )
        } ?: throw DeviceDimensionsModelToStringFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: EXPERIMENTAL_FLAGS",
                "SETTINGS: SPOOF_DEVICE_DIMENSIONS"
            )
        )

        SettingsPatch.updatePatchStatus("Spoof device dimensions")
    }
}