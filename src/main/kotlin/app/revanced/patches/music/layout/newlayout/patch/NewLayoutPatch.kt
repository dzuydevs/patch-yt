package app.revanced.patches.music.layout.newlayout.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.layout.newlayout.fingerprints.NewLayoutFingerprint
import app.revanced.patches.music.utils.annotations.MusicCompatibility
import app.revanced.patches.music.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.enum.CategoryType
import app.revanced.util.integrations.Constants.MUSIC_LAYOUT
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@Name("Enable new layout")
@Description("Enable new player layouts. (YT Music v5.47.51+)")
@DependsOn([SettingsPatch::class])
@MusicCompatibility
@Version("0.0.1")
class NewLayoutPatch : BytecodePatch(
    listOf(NewLayoutFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {

        NewLayoutFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {}, $MUSIC_LAYOUT->enableNewLayout()Z
                        move-result v$targetRegister
                        """
                )
            }
        } ?: return NewLayoutFingerprint.toErrorResult()

        SettingsPatch.addMusicPreference(
            CategoryType.LAYOUT,
            "revanced_enable_new_layout",
            "true"
        )

        return PatchResultSuccess()
    }
}