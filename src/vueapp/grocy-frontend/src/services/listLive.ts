import { ACCESS_TOKEN_KEY } from "@/constants/auth";

type LiveEvent = "connected" | "presence" | "list-changed";

// Fetch keeps JWT authentication in a header. Tokens must not appear in URLs.
export function subscribeToList(listId: string, onEvent: (event: LiveEvent, data: any) => void,
    onDisconnected: () => void) {
    const controller = new AbortController();
    const { signal } = controller;
    let connectionId: string | null = null;

    function disconnect() {
        if (!connectionId) return;
        const token = localStorage.getItem(ACCESS_TOKEN_KEY);
        const id = connectionId;
        connectionId = null;
        if (token) void fetch(`/api/lists/${listId}/events/${id}`, {
            method: "DELETE", headers: { Authorization: `Bearer ${token}` }, keepalive: true
        }).catch(() => {});
    }

    async function listen() {
        while (!signal.aborted) {
            const attempt = new AbortController();
            const abortAttempt = () => attempt.abort();
            signal.addEventListener("abort", abortAttempt, { once: true });
            let idleTimeout = setTimeout(abortAttempt, 45_000);
            function receivedData() {
                clearTimeout(idleTimeout);
                idleTimeout = setTimeout(abortAttempt, 45_000);
            }
            try {
                const token = localStorage.getItem(ACCESS_TOKEN_KEY);
                if (!token) return;
                const response = await fetch(`/api/lists/${listId}/events`, {
                    headers: { Authorization: `Bearer ${token}`, Accept: "text/event-stream" },
                    signal: attempt.signal
                });
                if (!response.ok) {
                    // Authentication and permission failures need user action, not endless retries.
                    if ([401, 403, 404].includes(response.status)) return;
                    throw new Error(`Live connection failed: ${response.status}`);
                }
                if (!response.body) throw new Error("Missing event stream");
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let pending = "";
                try {
                    while (!signal.aborted) {
                        const { value, done } = await reader.read();
                        if (done) break;
                        receivedData();
                        pending += decoder.decode(value, { stream: true });
                        // Frames can span several network chunks; decode only complete frames.
                        let boundary;
                        while ((boundary = pending.search(/\r?\n\r?\n/)) >= 0) {
                            const frame = pending.slice(0, boundary);
                            const separator = pending.slice(boundary).match(/^\r?\n\r?\n/)![0];
                            pending = pending.slice(boundary + separator.length);
                            let event = "message";
                            const data: string[] = [];
                            for (const line of frame.split(/\r?\n/)) {
                                if (line.startsWith("event:")) event = line.slice(6).trim();
                                if (line.startsWith("data:")) data.push(line.slice(5).replace(/^ /, ""));
                            }
                            if (["connected", "presence", "list-changed"].includes(event)) {
                                const payload = JSON.parse(data.join("\n"));
                                if (event === "connected") connectionId = payload;
                                onEvent(event as LiveEvent, payload);
                            }
                        }
                    }
                } finally {
                    await reader.cancel().catch(() => {});
                    reader.releaseLock();
                }
            } catch (error) {
                if (signal.aborted) return;
                console.debug("Reconnecting list stream", error);
            } finally {
                clearTimeout(idleTimeout);
                signal.removeEventListener("abort", abortAttempt);
                attempt.abort();
                disconnect();
                if (!signal.aborted) onDisconnected();
            }
            await new Promise<void>(resolve => {
                const finish = () => {
                    clearTimeout(timeout);
                    signal.removeEventListener("abort", finish);
                    resolve();
                };
                const timeout = setTimeout(finish, 1500);
                signal.addEventListener("abort", finish, { once: true });
            });
        }
    }

    void listen();
    return () => {
        disconnect();
        controller.abort();
    };
}
