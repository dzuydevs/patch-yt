package app.revanced.patches.youtube.navigation.homepage.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.navigation.homepage.fingerprints.IntentExceptionFingerprint
import app.revanced.patches.youtube.navigation.homepage.fingerprints.LauncherActivityFingerprint
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.NAVIGATION

@Patch(
    name = "Change homepage",
    description = "Change home page to subscription feed.",
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
object ChangeHomePagePatch : BytecodePatch(
    setOf(
        IntentExceptionFingerprint,
        LauncherActivityFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        LauncherActivityFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    implementation!!.instructions.size - 1, """
                        move-object/from16 v0, p1
                        invoke-static {v0}, $NAVIGATION->changeHomePage(Landroid/app/Activity;)V
                        """
                )
            }
        } ?: throw LauncherActivityFingerprint.exception

        IntentExceptionFingerprint.result?.let {
            it.mutableMethod.apply {
                val index = it.scanResult.patternScanResult!!.endIndex + 1

                addInstructionsWithLabels(
                    index, """
                        invoke-static {}, $NAVIGATION->changeHomePage()Z
                        move-result v0
                        if-eqz v0, :default
                        return-void
                        """, ExternalLabel("default", getInstruction(index))
                )
            }
        } ?: throw IntentExceptionFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: NAVIGATION_SETTINGS",
                "SETTINGS: CHANGE_HOMEPAGE_TO_SUBSCRIPTION"
            )
        )

        SettingsPatch.updatePatchStatus("change-homepage")

    }
}