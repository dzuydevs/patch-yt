package app.revanced.patches.music.utils.litho.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.litho.fingerprints.LithoFilterFingerprint
import app.revanced.patches.shared.patch.litho.ComponentParserPatch
import app.revanced.patches.shared.patch.litho.ComponentParserPatch.pathBuilderHook
import app.revanced.util.integrations.Constants.MUSIC_ADS_PATH
import java.io.Closeable

@Patch(
    dependencies = [ComponentParserPatch::class]
)
@Suppress("unused")
object LithoFilterPatch : BytecodePatch(
    setOf(LithoFilterFingerprint)
), Closeable {

    internal lateinit var addFilter: (String) -> Unit
        private set

    private var filterCount = 0

    override fun execute(context: BytecodeContext) {
        pathBuilderHook("$MUSIC_ADS_PATH/LithoFilterPatch;->filter")

        LithoFilterFingerprint.result?.let {
            it.mutableMethod.apply {
                removeInstructions(0, 6)

                addFilter = { classDescriptor ->
                    addInstructions(
                        0, """
                        new-instance v0, $classDescriptor
                        invoke-direct {v0}, $classDescriptor-><init>()V
                        const/16 v3, ${filterCount++}
                        aput-object v0, v2, v3
                        """
                    )
                }
            }
        } ?: throw LithoFilterFingerprint.exception

    }

    override fun close() = LithoFilterFingerprint.result!!
        .mutableMethod.addInstructions(
            0, """
                const/16 v1, $filterCount
                new-array v2, v1, [Lapp/revanced/music/patches/ads/Filter;
                const/4 v1, 0x1
                """
        )
}
