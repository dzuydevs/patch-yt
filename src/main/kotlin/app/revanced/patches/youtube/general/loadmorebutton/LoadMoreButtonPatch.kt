package app.revanced.patches.youtube.general.loadmorebutton

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.general.loadmorebutton.fingerprints.LoadMoreButtonFingerprint
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.integrations.Constants.GENERAL
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide load more button",
    description = "Hides the button under videos that loads similar videos.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
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
object LoadMoreButtonPatch : BytecodePatch(
    setOf(LoadMoreButtonFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        LoadMoreButtonFingerprint.result?.let {
            val getViewMethod =
                it.mutableClass.methods.find { method ->
                    method.parameters.isEmpty() &&
                            method.returnType == "Landroid/view/View;"
                }

            getViewMethod?.apply {
                val targetIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex,
                    "invoke-static {v$targetRegister}, $GENERAL->hideLoadMoreButton(Landroid/view/View;)V"
                )
            } ?: throw PatchException("Failed to find getView method")
        } ?: throw LoadMoreButtonFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: HIDE_LOAD_MORE_BUTTON"
            )
        )

        SettingsPatch.updatePatchStatus("Hide load more button")

    }
}
