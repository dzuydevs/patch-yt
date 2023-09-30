package app.revanced.patches.youtube.misc.externalbrowser.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.youtube.misc.externalbrowser.fingerprints.ExternalBrowserPrimaryFingerprint
import app.revanced.patches.youtube.misc.externalbrowser.fingerprints.ExternalBrowserSecondaryFingerprint
import app.revanced.patches.youtube.misc.externalbrowser.fingerprints.ExternalBrowserTertiaryFingerprint
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.bytecode.getStringIndex
import app.revanced.util.integrations.Constants.MISC_PATH
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable external browser",
    description = "Open url outside the app in an external browser.",
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
object ExternalBrowserPatch : BytecodePatch(
    setOf(
        ExternalBrowserPrimaryFingerprint,
        ExternalBrowserSecondaryFingerprint,
        ExternalBrowserTertiaryFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        arrayOf(
            ExternalBrowserPrimaryFingerprint,
            ExternalBrowserSecondaryFingerprint,
            ExternalBrowserTertiaryFingerprint
        ).forEach { fingerprint ->
            fingerprint.result?.let {
                it.mutableMethod.apply {
                    val targetIndex =
                        getStringIndex("android.support.customtabs.action.CustomTabsService")
                    val register = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    addInstructions(
                        targetIndex + 1, """
                            invoke-static {v$register}, $MISC_PATH/ExternalBrowserPatch;->enableExternalBrowser(Ljava/lang/String;)Ljava/lang/String;
                            move-result-object v$register
                            """
                    )
                }
            } ?: throw fingerprint.exception
        }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: ENABLE_EXTERNAL_BROWSER"
            )
        )

        SettingsPatch.updatePatchStatus("enable-external-browser")

    }
}