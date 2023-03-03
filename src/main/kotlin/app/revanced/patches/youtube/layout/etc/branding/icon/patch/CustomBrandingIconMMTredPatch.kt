package app.revanced.patches.youtube.layout.etc.branding.icon.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.annotation.YouTubeCompatibility
import app.revanced.patches.youtube.misc.settings.resource.patch.SettingsPatch
import app.revanced.util.resources.IconHelper.customIcon
import app.revanced.util.resources.ResourceHelper.updatePatchStatusIcon

@Patch(false)
@Name("custom-branding-icon-mmtred")
@Description("Changes the YouTube launcher icon (MMT Red).")
@DependsOn([SettingsPatch::class])
@YouTubeCompatibility
@Version("0.0.1")
class CustomBrandingIconMMTredPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {

        context.customIcon("mmtred")
        context.updatePatchStatusIcon("mmtred")

        return PatchResultSuccess()
    }
}
