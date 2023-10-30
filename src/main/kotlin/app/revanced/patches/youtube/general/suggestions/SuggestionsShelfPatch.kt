package app.revanced.patches.youtube.general.suggestions

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.shared.patch.litho.ComponentParserPatch.emptyComponentLabel
import app.revanced.patches.youtube.general.suggestions.fingerprints.BreakingNewsFingerprint
import app.revanced.patches.youtube.general.suggestions.fingerprints.SuggestionContentsBuilderFingerprint
import app.revanced.patches.youtube.general.suggestions.fingerprints.SuggestionContentsBuilderLegacyFingerprint
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.navigationbuttons.NavigationButtonHookPatch
import app.revanced.patches.youtube.utils.navigationbuttons.NavigationButtonHookPatch.PivotBarMethod
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch.AvatarImageWithTextTab
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.bytecode.getWideLiteralIndex
import app.revanced.util.integrations.Constants.COMPONENTS_PATH
import app.revanced.util.pivotbar.InjectionUtils.REGISTER_TEMPLATE_REPLACEMENT
import app.revanced.util.pivotbar.InjectionUtils.injectHook
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide suggestions shelf",
    description = "Hides the suggestions shelf.",
    dependencies = [
        LithoFilterPatch::class,
        NavigationButtonHookPatch::class,
        SettingsPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41"
            ]
        )
    ]
)
@Suppress("unused")
object SuggestionsShelfPatch : BytecodePatch(
    setOf(
        BreakingNewsFingerprint,
        SuggestionContentsBuilderFingerprint,
        SuggestionContentsBuilderLegacyFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Only used to tablet layout and the old UI components.
         */
        BreakingNewsFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.endIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex + 1,
                    "invoke-static {v$targetRegister}, $FILTER_CLASS_DESCRIPTOR->hideBreakingNewsShelf(Landroid/view/View;)V"
                )
            }
        } ?: throw BreakingNewsFingerprint.exception

        /**
         * Target method only removes the horizontal video shelf's content in the feed.
         * Since the header of the horizontal video shelf is not removed, it should be removed through the SuggestionsShelfFilter
         */
        val result = SuggestionContentsBuilderFingerprint.result // YouTube v18.36.xx ~
            ?: SuggestionContentsBuilderLegacyFingerprint.result // ~ YouTube v18.35.xx
            ?: throw SuggestionContentsBuilderFingerprint.exception

        result.let {
            it.mutableMethod.apply {
                addInstructionsWithLabels(
                    2, """
                        invoke-static/range {p2 .. p2}, $FILTER_CLASS_DESCRIPTOR->filterSuggestionsShelfSubComponents(Ljava/lang/Object;)Z
                        move-result v0
                        if-eqz v0, :show
                        """ + emptyComponentLabel, ExternalLabel("show", getInstruction(2))
                )
            }
        }

        PivotBarMethod.apply {
            val insertIndex = implementation!!.instructions.let {
                val scanStart = getWideLiteralIndex(AvatarImageWithTextTab)

                scanStart + it.subList(scanStart, it.size - 1).indexOfFirst { instruction ->
                    instruction.opcode == Opcode.INVOKE_VIRTUAL
                }
            } + 2
            injectHook(YOU_BUTTON_HOOK, insertIndex)
        }

        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)


        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: HIDE_SUGGESTIONS_SHELF"
            )
        )

        SettingsPatch.updatePatchStatus("Hide suggestions shelf")

    }

    private const val FILTER_CLASS_DESCRIPTOR =
        "$COMPONENTS_PATH/SuggestionsShelfFilter;"

    private const val YOU_BUTTON_HOOK =
        "invoke-static { v${REGISTER_TEMPLATE_REPLACEMENT} }, $FILTER_CLASS_DESCRIPTOR" +
                "->" +
                "isYouButtonEnabled(Landroid/view/View;)V"
}
