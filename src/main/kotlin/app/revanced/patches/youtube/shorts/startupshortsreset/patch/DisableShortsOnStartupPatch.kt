package app.revanced.patches.youtube.shorts.startupshortsreset.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.shorts.startupshortsreset.fingerprints.UserWasInShortsFingerprint
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.bytecode.getWide32LiteralIndex
import app.revanced.util.integrations.Constants.SHORTS
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Disable Shorts on startup",
    description = "Disables playing YouTube Shorts when launching YouTube.",
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
object DisableShortsOnStartupPatch : BytecodePatch(
    setOf(UserWasInShortsFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        UserWasInShortsFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWide32LiteralIndex(45381394)
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructionsWithLabels(
                    insertIndex,
                    """
                        invoke-static { }, $SHORTS->disableStartupShortsPlayer()Z
                        move-result v$insertRegister
                        if-eqz v$insertRegister, :show_startup_shorts_player
                        return-void
                        """,
                    ExternalLabel("show_startup_shorts_player", getInstruction(insertIndex))
                )
            }
        } ?: throw UserWasInShortsFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SHORTS_SETTINGS",
                "SETTINGS: SHORTS_PLAYER_PARENT",
                "SETTINGS: DISABLE_STARTUP_SHORTS_PLAYER"
            )
        )

        SettingsPatch.updatePatchStatus("disable-startup-shorts-player")

    }
}
