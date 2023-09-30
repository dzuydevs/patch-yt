package app.revanced.patches.music.general.floatingbutton.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.music.general.floatingbutton.fingerprints.FloatingButtonFingerprint
import app.revanced.patches.music.general.floatingbutton.fingerprints.FloatingButtonParentFingerprint
import app.revanced.patches.music.utils.resourceid.patch.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.enum.CategoryType
import app.revanced.util.integrations.Constants.MUSIC_GENERAL

@Patch(
    name = "Hide new playlist button",
    description = "Hides the \"New playlist\" button in the library.",
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
object NewPlaylistButtonPatch : BytecodePatch(
    setOf(FloatingButtonParentFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        FloatingButtonParentFingerprint.result?.let { parentResult ->
            FloatingButtonFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    addInstructionsWithLabels(
                        1, """
                            invoke-static {}, $MUSIC_GENERAL->hideNewPlaylistButton()Z
                            move-result v0
                            if-eqz v0, :show
                            return-void
                            """, ExternalLabel("show", getInstruction(1))
                    )
                }
            } ?: throw FloatingButtonFingerprint.exception
        } ?: throw FloatingButtonParentFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.GENERAL,
            "revanced_hide_new_playlist_button",
            "false"
        )

    }
}
