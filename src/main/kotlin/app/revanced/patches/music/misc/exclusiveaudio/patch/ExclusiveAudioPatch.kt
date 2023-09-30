package app.revanced.patches.music.misc.exclusiveaudio.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.music.misc.exclusiveaudio.fingerprints.DataSavingSettingsFragmentFingerprint
import app.revanced.patches.music.misc.exclusiveaudio.fingerprints.MusicBrowserServiceFingerprint
import app.revanced.patches.music.misc.exclusiveaudio.fingerprints.PodCastConfigFingerprint
import app.revanced.util.bytecode.getStringIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Patch(
    name = "Exclusive audio playback",
    description = "Enables the option to play music without video."
)
@Suppress("unused")
object ExclusiveAudioPatch : BytecodePatch(
    setOf(
        DataSavingSettingsFragmentFingerprint,
        MusicBrowserServiceFingerprint,
        PodCastConfigFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Don't play music videos
         */
        MusicBrowserServiceFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex =
                    getStringIndex("MBS: Return empty root for client: %s, isFullMediaBrowserEnabled: %b, is client browsable: %b, isRedAccount: %b")

                for (index in targetIndex downTo 0) {
                    if (getInstruction(index).opcode != Opcode.INVOKE_VIRTUAL) continue

                    val targetReference = getInstruction<ReferenceInstruction>(index).reference

                    if (!targetReference.toString().endsWith("()Z")) continue

                    with(
                        context
                            .toMethodWalker(it.method)
                            .nextMethod(index, true)
                            .getMethod() as MutableMethod
                    ) {
                        addInstructions(
                            0, """
                                const/4 v0, 0x1
                                return v0
                                """
                        )
                    }
                    break
                }
            }
        } ?: throw MusicBrowserServiceFingerprint.exception

        /**
         * Don't play podcast videos
         */
        PodCastConfigFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(
                    insertIndex,
                    "const/4 v$targetRegister, 0x1"
                )
            }
        } ?: throw PodCastConfigFingerprint.exception

        DataSavingSettingsFragmentFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getStringIndex("pref_key_dont_play_nma_video") + 4
                val targetRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerD

                addInstruction(
                    insertIndex,
                    "const/4 v$targetRegister, 0x1"
                )
            }
        } ?: throw DataSavingSettingsFragmentFingerprint.exception
    }
}
