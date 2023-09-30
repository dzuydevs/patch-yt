package app.revanced.patches.youtube.utils.fix.parameter.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.utils.fix.parameter.fingerprints.PlayerParameterBuilderFingerprint
import app.revanced.patches.youtube.utils.fix.parameter.fingerprints.PlayerResponseModelImplGeneralFingerprint
import app.revanced.patches.youtube.utils.fix.parameter.fingerprints.PlayerResponseModelImplLiveStreamFingerprint
import app.revanced.patches.youtube.utils.fix.parameter.fingerprints.StoryboardRendererSpecFingerprint
import app.revanced.patches.youtube.utils.fix.parameter.fingerprints.StoryboardRendererSpecRecommendedLevelFingerprint
import app.revanced.patches.youtube.utils.fix.parameter.fingerprints.StoryboardThumbnailFingerprint
import app.revanced.patches.youtube.utils.fix.parameter.fingerprints.StoryboardThumbnailParentFingerprint
import app.revanced.patches.youtube.utils.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.MISC_PATH
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Spoof player parameters",
    description = "Spoofs player parameters to prevent playback issues.",
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
    dependencies = [
        PlayerTypeHookPatch::class,
        SettingsPatch::class
    ]
)
@Suppress("unused")
object SpoofPlayerParameterPatch : BytecodePatch(
    setOf(
        PlayerParameterBuilderFingerprint,
        PlayerResponseModelImplGeneralFingerprint,
        PlayerResponseModelImplLiveStreamFingerprint,
        StoryboardRendererSpecFingerprint,
        StoryboardRendererSpecRecommendedLevelFingerprint,
        StoryboardThumbnailParentFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$MISC_PATH/SpoofPlayerParameterPatch;"

    override fun execute(context: BytecodeContext) {

        /**
         * Hook player parameter
         */
        PlayerParameterBuilderFingerprint.result?.let {
            it.mutableMethod.apply {
                val videoIdRegister = 1
                val playerParameterRegister = 3

                addInstructions(
                    0, """
                        invoke-static {p$videoIdRegister, p$playerParameterRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->spoofParameter(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                        move-result-object p$playerParameterRegister
                        """
                )
            }
        } ?: throw PlayerParameterBuilderFingerprint.exception

        /**
         * Forces the SeekBar thumbnail preview container to be shown
         * I don't think this code is needed anymore
         */
        StoryboardThumbnailParentFingerprint.result?.classDef?.let { classDef ->
            StoryboardThumbnailFingerprint.also {
                it.resolve(
                    context,
                    classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val targetIndex = it.scanResult.patternScanResult!!.endIndex
                    val targetRegister =
                        getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    // Since this is end of the method must replace one line then add the rest.
                    addInstructions(
                        targetIndex + 1,
                        """
                            invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->getSeekbarThumbnailOverrideValue()Z
                            move-result v$targetRegister
                            return v$targetRegister
                            """
                    )
                    removeInstruction(targetIndex)
                }
            } ?: throw StoryboardThumbnailFingerprint.exception
        } ?: throw StoryboardThumbnailParentFingerprint.exception

        /**
         * Hook StoryBoard Renderer URL
         */
        arrayOf(
            PlayerResponseModelImplGeneralFingerprint,
            PlayerResponseModelImplLiveStreamFingerprint,
            StoryboardRendererSpecFingerprint
        ).forEach { fingerprint ->
            fingerprint.result?.let {
                it.mutableMethod.apply {
                    val getStoryBoardIndex = it.scanResult.patternScanResult!!.endIndex
                    val getStoryBoardRegister =
                        getInstruction<OneRegisterInstruction>(getStoryBoardIndex).registerA

                    addInstructionsWithLabels(
                        getStoryBoardIndex, """
                            if-nez v$getStoryBoardRegister, :ignore
                            invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->getStoryboardRendererSpec()Ljava/lang/String;
                            move-result-object v$getStoryBoardRegister
                            """, ExternalLabel("ignore", getInstruction(getStoryBoardIndex))
                    )
                }
            } ?: throw fingerprint.exception
        }

        /**
         * Hook recommended value and StoryBoard Renderer for live stream
         */
        StoryboardRendererSpecRecommendedLevelFingerprint.result?.let {
            it.mutableMethod.apply {
                val moveOriginalRecommendedValueIndex = it.scanResult.patternScanResult!!.endIndex
                val originalValueRegister =
                    getInstruction<OneRegisterInstruction>(moveOriginalRecommendedValueIndex).registerA

                val liveStreamStoryBoardUrlIndex =
                    implementation!!.instructions.indexOfFirst { instruction ->
                        instruction.opcode == Opcode.INVOKE_INTERFACE
                    } + 1
                val liveStreamStoryBoardUrlRegister =
                    getInstruction<OneRegisterInstruction>(liveStreamStoryBoardUrlIndex).registerA

                addInstructions(
                    moveOriginalRecommendedValueIndex + 1, """
                        invoke-static { v$originalValueRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getRecommendedLevel(I)I
                        move-result v$originalValueRegister
                        """
                )

                addInstructions(
                    liveStreamStoryBoardUrlIndex + 1, """
                        invoke-static { v$liveStreamStoryBoardUrlRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->getStoryboardRendererSpec(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$liveStreamStoryBoardUrlRegister
                        """
                )
            }
        } ?: throw StoryboardRendererSpecRecommendedLevelFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "SETTINGS: SPOOF_PLAYER_PARAMETER"
            )
        )

    }
}
