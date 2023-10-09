package app.revanced.patches.reddit.misc.openlink

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.misc.openlink.fingerprints.ScreenNavigatorFingerprint
import app.revanced.patches.reddit.utils.settings.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.SettingsPatch

@Patch(
    name = "Open links directly",
    description = "Skips over redirection URLs to external links.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")]
)
@Suppress("unused")
object OpenLinksDirectlyPatch : BytecodePatch(
    setOf(ScreenNavigatorFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/reddit/patches/OpenLinksDirectlyPatch;" +
                "->parseRedirectUri(Landroid/net/Uri;)Landroid/net/Uri;"
    override fun execute(context: BytecodeContext) {
        ScreenNavigatorFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        invoke-static {p2}, $INTEGRATIONS_METHOD_DESCRIPTOR
                        move-result-object p2
                        """
                )
            }
        } ?: throw ScreenNavigatorFingerprint.exception

        updateSettingsStatus("OpenLinksDirectly")

    }
}