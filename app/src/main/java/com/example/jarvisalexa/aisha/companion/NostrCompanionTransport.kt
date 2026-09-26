package com.example.jarvisalexa.aisha.companion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rust.nostr.sdk.Client
import rust.nostr.sdk.EventBuilder
import rust.nostr.sdk.Kind
import rust.nostr.sdk.KindStandard
import rust.nostr.sdk.Keys
import rust.nostr.sdk.NostrSigner
import rust.nostr.sdk.RelayUrl
import rust.nostr.sdk.Tag

class NostrCompanionTransport : CompanionTransport {

    private var client: Client? = null
    private var keys: Keys? = null

    private val relays = listOf(
        "wss://nos.lol",
        "wss://relay.primal.net",
        "wss://relay.nostr.band"
    )

    private suspend fun ensureClient(): Client {
        if (client == null) {
            val localKeys = Keys.generate()
            val signer = NostrSigner.Companion.keys(localKeys)
            val newClient = Client(signer)

            for (relay in relays) {
                runCatching {
                    newClient.addRelay(RelayUrl.Companion.parse(relay))
                }
            }

            newClient.connect()

            keys = localKeys
            client = newClient
        }

        return client!!
    }

    override suspend fun send(
        request: CompanionRequest,
        config: CompanionConfig
    ): CompanionResponse = withContext(Dispatchers.IO) {

        if (!config.isPaired()) {
            return@withContext CompanionResponse(
                id = request.id,
                ok = false,
                error = "Companion is not paired"
            )
        }

        try {
            val nostrClient = ensureClient()
            val localKeys = keys ?: error("Nostr keys unavailable")

            val payload = CompanionCrypto.encrypt(
                request,
                config.secret
            )

            val tags = listOf(
                Tag.Companion.client("Aisha"),
                Tag.Companion.alt("Aisha Companion")
            )

            val builder = EventBuilder(
                Kind.Companion.fromStd(KindStandard.TEXT_NOTE),
                payload
            ).tags(tags)

            val event = builder.signWithKeys(localKeys)
            val result = nostrClient.sendEvent(event)

            CompanionResponse(
                id = request.id,
                ok = true,
                result = result.toString()
            )

        } catch (e: Exception) {
            CompanionResponse(
                id = request.id,
                ok = false,
                error = e.message ?: "Nostr transport error"
            )
        }
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            runCatching {
                client?.close()
            }

            client = null
            keys?.close()
            keys = null
        }
    }
}
