package app.revanced.patches.youtube.player.castbutton.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.youtube.player.castbutton.fingerprints.CastButtonFingerprint
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.PLAYER

@Patch(
    name = "Hide cast button",
    description = "Hides the cast button in the video player.",
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
object HideCastButtonPatch : BytecodePatch(
    setOf(CastButtonFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        CastButtonFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        invoke-static {p1}, $PLAYER->hideCastButton(I)I
                        move-result p1
                        """
                )
            }
        } ?: throw CastButtonFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: PLAYER_SETTINGS",
                "SETTINGS: HIDE_CAST_BUTTON"
            )
        )

        SettingsPatch.updatePatchStatus("hide-cast-button")

    }
}
