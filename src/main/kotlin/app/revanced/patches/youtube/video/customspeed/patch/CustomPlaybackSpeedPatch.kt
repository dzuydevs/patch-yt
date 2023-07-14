package app.revanced.patches.youtube.video.customspeed.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.flyoutpanel.oldspeedlayout.patch.OldSpeedLayoutPatch
import app.revanced.patches.youtube.utils.annotations.YouTubeCompatibility
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.patches.youtube.video.customspeed.fingerprints.SpeedArrayGeneratorFingerprint
import app.revanced.patches.youtube.video.customspeed.fingerprints.SpeedLimiterFallBackFingerprint
import app.revanced.patches.youtube.video.customspeed.fingerprints.SpeedLimiterFingerprint
import app.revanced.util.integrations.Constants.VIDEO_PATH
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.MethodReference

@Patch
@Name("Custom playback speed")
@Description("Adds more playback speed options.")
@DependsOn(
    [
        OldSpeedLayoutPatch::class,
        SettingsPatch::class
    ]
)
@YouTubeCompatibility
@Version("0.0.1")
class CustomPlaybackSpeedPatch : BytecodePatch(
    listOf(
        SpeedArrayGeneratorFingerprint,
        SpeedLimiterFallBackFingerprint,
        SpeedLimiterFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SpeedArrayGeneratorFingerprint.result?.let { result ->
            result.mutableMethod.apply {
                val targetIndex = result.scanResult.patternScanResult!!.startIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex + 1, """
                        invoke-static {v$targetRegister}, $VIDEO_PATH/CustomPlaybackSpeedPatch;->getLength(I)I
                        move-result v$targetRegister
                        """
                )

                val targetInstruction = implementation!!.instructions

                for ((index, instruction) in targetInstruction.withIndex()) {
                    if (instruction.opcode != Opcode.INVOKE_INTERFACE) continue

                    val sizeInstruction = getInstruction<Instruction35c>(index)
                    if ((sizeInstruction.reference as MethodReference).name != "size") continue

                    val register = getInstruction<OneRegisterInstruction>(index + 1).registerA

                    addInstructions(
                        index + 2, """
                        invoke-static {v$register}, $VIDEO_PATH/CustomPlaybackSpeedPatch;->getSize(I)I
                        move-result v$register
                        """
                    )
                    break
                }


                for ((index, instruction) in targetInstruction.withIndex()) {
                    if (instruction.opcode != Opcode.SGET_OBJECT) continue

                    val targetReference =
                        getInstruction<ReferenceInstruction>(index).reference.toString()

                    if (targetReference.contains("PlayerConfigModel;") && targetReference.endsWith("[F")) {
                        val register = getInstruction<OneRegisterInstruction>(index).registerA

                        addInstructions(
                            index + 1, """
                                invoke-static {v$register}, $VIDEO_PATH/CustomPlaybackSpeedPatch;->getArray([F)[F
                                move-result-object v$register
                                """
                        )
                        break
                    }
                }
            }
        } ?: return SpeedArrayGeneratorFingerprint.toErrorResult()

        arrayOf(
            SpeedLimiterFallBackFingerprint,
            SpeedLimiterFingerprint
        ).forEach { fingerprint ->
            fingerprint.result?.let { result ->
                result.mutableMethod.apply {
                    val limiterMinConstIndex =
                        implementation!!.instructions.indexOfFirst { (it as? NarrowLiteralInstruction)?.narrowLiteral == 0.25f.toRawBits() }
                    val limiterMaxConstIndex =
                        implementation!!.instructions.indexOfFirst { (it as? NarrowLiteralInstruction)?.narrowLiteral == 2.0f.toRawBits() }

                    val limiterMinConstDestination =
                        getInstruction<OneRegisterInstruction>(limiterMinConstIndex).registerA
                    val limiterMaxConstDestination =
                        getInstruction<OneRegisterInstruction>(limiterMaxConstIndex).registerA

                    replaceInstruction(
                        limiterMinConstIndex,
                        "const/high16 v$limiterMinConstDestination, 0x0"
                    )
                    replaceInstruction(
                        limiterMaxConstIndex,
                        "const/high16 v$limiterMaxConstDestination, 0x41200000    # 10.0f"
                    )
                }
            } ?: return fingerprint.toErrorResult()
        }

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: VIDEO_SETTINGS",
                "SETTINGS: CUSTOM_PLAYBACK_SPEED"
            )
        )

        SettingsPatch.updatePatchStatus("custom-playback-speed")

        return PatchResultSuccess()
    }
}
