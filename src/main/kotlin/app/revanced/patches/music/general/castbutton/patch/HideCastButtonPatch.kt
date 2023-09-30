package app.revanced.patches.music.general.castbutton.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.music.general.castbutton.fingerprints.MediaRouteButtonFingerprint
import app.revanced.patches.music.general.castbutton.fingerprints.PlayerOverlayChipFingerprint
import app.revanced.patches.music.utils.resourceid.patch.SharedResourceIdPatch
import app.revanced.patches.music.utils.resourceid.patch.SharedResourceIdPatch.PlayerOverlayChip
import app.revanced.patches.music.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.bytecode.getWideLiteralIndex
import app.revanced.util.enum.CategoryType
import app.revanced.util.integrations.Constants.MUSIC_GENERAL
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide cast button",
    description = "Hides the cast button.",
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
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ]
)
@Suppress("unused")
object HideCastButtonPatch : BytecodePatch(
    setOf(
        MediaRouteButtonFingerprint,
        PlayerOverlayChipFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Hide cast button
         */
        MediaRouteButtonFingerprint.result?.let {
            val setVisibilityMethod =
                it.mutableClass.methods.find { method -> method.name == "setVisibility" }

            setVisibilityMethod?.apply {
                addInstructions(
                    0, """
                        invoke-static {p1}, $MUSIC_GENERAL->hideCastButton(I)I
                        move-result p1
                        """
                )
            } ?: throw PatchException("Failed to find setVisibility method")
        } ?: throw MediaRouteButtonFingerprint.exception

        /**
         * Hide floating cast banner
         */
        PlayerOverlayChipFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralIndex(PlayerOverlayChip) + 2
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex + 1,
                    "invoke-static {v$targetRegister}, $MUSIC_GENERAL->hideCastButton(Landroid/view/View;)V"
                )
            }
        }

        SettingsPatch.addMusicPreference(
            CategoryType.GENERAL,
            "revanced_hide_cast_button",
            "true"
        )

    }
}
