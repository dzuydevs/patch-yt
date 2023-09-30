package app.revanced.patches.music.player.oldstyleminiplayer.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.music.player.oldstyleminiplayer.fingerprints.NextButtonVisibilityFingerprint
import app.revanced.patches.music.player.oldstyleminiplayer.fingerprints.SwipeToCloseFingerprint
import app.revanced.patches.music.utils.fingerprints.PlayerColorFingerprint
import app.revanced.patches.music.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.enum.CategoryType
import app.revanced.util.integrations.Constants.MUSIC_PLAYER
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable old style miniplayer",
    description = "Return the miniplayers to old style.",
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
object OldStyleMiniPlayerPatch : BytecodePatch(
    setOf(
        PlayerColorFingerprint,
        SwipeToCloseFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        PlayerColorFingerprint.result?.let { parentResult ->
            NextButtonVisibilityFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val targetIndex = it.scanResult.patternScanResult!!.startIndex + 1
                    val targetRegister =
                        getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    addInstructions(
                        targetIndex + 1, """
                            invoke-static {v$targetRegister}, $MUSIC_PLAYER->enableOldStyleMiniPlayer(Z)Z
                            move-result v$targetRegister
                            """
                    )
                }
            } ?: throw NextButtonVisibilityFingerprint.exception
        } ?: throw PlayerColorFingerprint.exception

        SwipeToCloseFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$targetRegister}, $MUSIC_PLAYER->enableOldStyleMiniPlayer(Z)Z
                        move-result v$targetRegister
                        """
                )
            }
        } ?: throw SwipeToCloseFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.PLAYER,
            "revanced_enable_old_style_mini_player",
            "true"
        )

    }
}