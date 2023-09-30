package app.revanced.patches.reddit.layout.place.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patches.reddit.layout.place.fingerprints.HomePagerScreenFingerprint
import app.revanced.patches.reddit.utils.settings.bytecode.patch.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.resource.patch.SettingsPatch
import app.revanced.util.bytecode.getStringIndex
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide place button",
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")],
    description = "Hide r/place button in toolbar.",
    dependencies = [SettingsPatch::class]
)
@Suppress("unused")
object PlaceButtonPatch : BytecodePatch(
    setOf(HomePagerScreenFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/reddit/patches/PlaceButtonPatch;" +
                "->hidePlaceButton(Landroid/view/View;)V"
    override fun execute(context: BytecodeContext) {

        HomePagerScreenFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex =
                    getStringIndex("view.findViewById(Search\u2026nav_search_cta_container)")
                val targetRegister =
                    getInstruction<OneRegisterInstruction>(targetIndex - 1).registerA

                addInstruction(
                    targetIndex,
                    "invoke-static {v$targetRegister}, $INTEGRATIONS_METHOD_DESCRIPTOR"
                )
            }
        } ?: throw HomePagerScreenFingerprint.exception

        updateSettingsStatus("PlaceButton")

    }
}
