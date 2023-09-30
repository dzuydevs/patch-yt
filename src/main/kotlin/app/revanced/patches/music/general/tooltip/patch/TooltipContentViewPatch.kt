package app.revanced.patches.music.general.tooltip.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.music.general.tooltip.fingerprints.TooltipContentViewFingerprint
import app.revanced.patches.music.utils.resourceid.patch.SharedResourceIdPatch

@Patch(
    name = "Hide tooltip content",
    description = "Hides the tooltip box that appears on first install.",
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
    dependencies = [SharedResourceIdPatch::class]
)
@Suppress("unused")
object TooltipContentViewPatch : BytecodePatch(
    setOf(TooltipContentViewFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        TooltipContentViewFingerprint.result?.mutableMethod?.addInstruction(
            0,
            "return-void"
        ) ?: throw TooltipContentViewFingerprint.exception

    }
}
