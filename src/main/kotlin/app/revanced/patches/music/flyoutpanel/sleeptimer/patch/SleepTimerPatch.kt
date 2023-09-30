package app.revanced.patches.music.flyoutpanel.sleeptimer.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.music.flyoutpanel.sleeptimer.fingerprints.SleepTimerFingerprint
import app.revanced.patches.music.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.enum.CategoryType
import app.revanced.util.integrations.Constants.MUSIC_FLYOUT
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable sleep timer",
    description = "Add sleep timer to flyout menu.",
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.apps.youtube.music",
            [
                "6.15.52",
                "6.20.51",
                "6.21.51"
            ]
        )
    ],
    dependencies = [SettingsPatch::class]
)
@Suppress("unused")
object SleepTimerPatch : BytecodePatch(
    setOf(SleepTimerFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        SleepTimerFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {}, $MUSIC_FLYOUT->enableSleepTimer()Z
                        move-result v$targetRegister
                        """
                )
            }
        } ?: throw SleepTimerFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.FLYOUT,
            "revanced_enable_sleep_timer",
            "true"
        )

    }
}