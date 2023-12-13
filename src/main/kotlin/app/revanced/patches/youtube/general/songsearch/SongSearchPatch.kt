package app.revanced.patches.youtube.general.songsearch

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.general.songsearch.fingerprints.VoiceSearchConfigFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.GENERAL
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception

@Patch(
    name = "Enable song search",
    description = "Enables song search in the voice search screen.",
    dependencies = [SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
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
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43"
            ]
        )
    ],
    use = false
)
@Suppress("unused")
object SongSearchPatch : BytecodePatch(
    setOf(VoiceSearchConfigFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        VoiceSearchConfigFingerprint.result?.let {
            it.mutableMethod.apply {
                addInstructions(
                    0, """
                        invoke-static { }, $GENERAL->enableSongSearch()Z
                        move-result v0
                        return v0
                        """
                )
            }
        } ?: throw VoiceSearchConfigFingerprint.exception

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: GENERAL_SETTINGS",
                "SETTINGS: ENABLE_SONG_SEARCH"
            )
        )

        SettingsPatch.updatePatchStatus("Enable song search")
    }
}
