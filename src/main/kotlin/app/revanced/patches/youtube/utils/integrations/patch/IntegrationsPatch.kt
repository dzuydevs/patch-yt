package app.revanced.patches.youtube.utils.integrations.patch

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.patch.integrations.AbstractIntegrationsPatch
import app.revanced.patches.youtube.utils.integrations.fingerprints.APIPlayerServiceFingerprint
import app.revanced.patches.youtube.utils.integrations.fingerprints.ApplicationInitFingerprint
import app.revanced.patches.youtube.utils.integrations.fingerprints.EmbeddedPlayerControlsOverlayFingerprint
import app.revanced.patches.youtube.utils.integrations.fingerprints.EmbeddedPlayerFingerprint
import app.revanced.patches.youtube.utils.integrations.fingerprints.RemoteEmbedFragmentFingerprint
import app.revanced.patches.youtube.utils.integrations.fingerprints.RemoteEmbeddedPlayerFingerprint
import app.revanced.patches.youtube.utils.integrations.fingerprints.StandalonePlayerActivityFingerprint
import app.revanced.util.integrations.Constants.INTEGRATIONS_PATH

@Patch(requiresIntegrations = true)
object IntegrationsPatch : AbstractIntegrationsPatch(
    "$INTEGRATIONS_PATH/utils/ReVancedUtils;",
    setOf(
        ApplicationInitFingerprint,
        StandalonePlayerActivityFingerprint,
        RemoteEmbeddedPlayerFingerprint,
        RemoteEmbedFragmentFingerprint,
        EmbeddedPlayerControlsOverlayFingerprint,
        EmbeddedPlayerFingerprint,
        APIPlayerServiceFingerprint,
    ),
)
