package app.revanced.patches.youtube.shorts.shortscomponent.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsPivotFingerprint
import app.revanced.patches.youtube.shorts.shortscomponent.fingerprints.ShortsPivotLegacyFingerprint
import app.revanced.patches.youtube.utils.resourceid.patch.SharedResourceIdPatch.ReelForcedMuteButton
import app.revanced.patches.youtube.utils.resourceid.patch.SharedResourceIdPatch.ReelPivotButton
import app.revanced.util.bytecode.getWideLiteralIndex
import app.revanced.util.integrations.Constants.SHORTS
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

object ShortsPivotButtonPatch : BytecodePatch(
    setOf(
        ShortsPivotFingerprint,
        ShortsPivotLegacyFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {
        ShortsPivotLegacyFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralIndex(ReelForcedMuteButton)
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                val insertIndex = getTargetIndexDownTo(targetIndex, Opcode.IF_EQZ)
                val jumpIndex = getTargetIndexUpTo(targetIndex, Opcode.GOTO)

                addInstructionsWithLabels(
                    insertIndex, """
                        invoke-static {}, $SHORTS->hideShortsPlayerPivotButton()Z
                        move-result v$targetRegister
                        if-nez v$targetRegister, :hide
                        """, ExternalLabel("hide", getInstruction(jumpIndex))
                )
            }
        } ?: ShortsPivotFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = getWideLiteralIndex(ReelPivotButton)

                val insertIndex = getTargetIndexDownTo(targetIndex, Opcode.INVOKE_STATIC) + 2
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {v$insertRegister}, $SHORTS->hideShortsPlayerPivotButton(Ljava/lang/Object;)Ljava/lang/Object;
                        move-result-object v$insertRegister
                        """
                )
            }
        } ?: throw ShortsPivotFingerprint.exception

    }

    fun MutableMethod.getTargetIndexDownTo(
        startIndex: Int,
        opcode: Opcode
    ): Int {
        for (index in startIndex downTo 0) {
            if (getInstruction(index).opcode != opcode)
                continue

            return index
        }
        throw PatchException("Failed to find hook method")
    }

    fun MutableMethod.getTargetIndexUpTo(
        startIndex: Int,
        opcode: Opcode
    ): Int {
        for (index in startIndex until implementation!!.instructions.size) {
            if (getInstruction(index).opcode != opcode)
                continue

            return index
        }
        throw PatchException("Failed to find hook method")
    }
}
