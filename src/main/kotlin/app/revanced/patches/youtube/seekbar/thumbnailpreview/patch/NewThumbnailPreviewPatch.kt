package app.revanced.patches.youtube.seekbar.thumbnailpreview.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.youtube.seekbar.thumbnailpreview.fingerprints.ThumbnailPreviewConfigFingerprint
import app.revanced.patches.youtube.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.integrations.Constants.SEEKBAR
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Enable new thumbnail preview",
    description = "Enables a new type of thumbnail preview.",
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
object NewThumbnailPreviewPatch : BytecodePatch(
    setOf(ThumbnailPreviewConfigFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        ThumbnailPreviewConfigFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val targetRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstructions(
                    insertIndex, """
                        invoke-static {}, $SEEKBAR->enableNewThumbnailPreview()Z
                        move-result v$targetRegister
                        """
                )
            }
        } ?: throw ThumbnailPreviewConfigFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: SEEKBAR_SETTINGS",
                "SETTINGS: ENABLE_NEW_THUMBNAIL_PREVIEW"
            )
        )

        SettingsPatch.updatePatchStatus("enable-new-thumbnail-preview")

    }
}
