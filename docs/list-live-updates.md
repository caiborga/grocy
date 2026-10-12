# Live list updates and presence

The list page opens an authenticated SSE stream at `GET /api/lists/{listId}/events`.
Writes still use the existing REST endpoints. `ListChanged` events are delivered
after transaction commit, so a rollback cannot expose an unsaved change.
Item changes (including recipe imports) publish through `ListActivityService`;
list updates, deletion and default-list changes publish through `ListService`.

The stream emits `connected`, `presence`, `list-changed` and `heartbeat` events.
The frontend reloads the list and its filtered items on connection and invalidation.
This also recovers changes missed during a network outage. Reloads wait for local
writes and discard responses if a write or filter change happened in the meantime.
Edit dialogs retain their drafts. Concurrent edits keep the existing REST API's
conflict behavior; SSE does not introduce collaborative text editing.

## Presence semantics

A user is present while at least one stream for that list is open. Background tabs
count too. Several tabs belonging to the same user appear once; names include the
current user. A fixed dock shows initials even while scrolling, above the mobile
navigation. Hover, keyboard focus or a tap opens the person's full name, household
role and membership date. Tapping again, outside the dock or pressing Escape closes
the details. Account email addresses and authentication data are not shared.

Navigation sends `DELETE /api/lists/{listId}/events/{connectionId}` using `keepalive`.
Only the connection's owner can disconnect it. Unexpected disconnects are detected
by server heartbeats every 15 seconds or the stream's 60-second timeout. The browser
reconnects after 1.5 seconds and aborts streams silent for 45 seconds. Presence is
approximate during outages and may briefly disappear during reconnection.

JWT validity, session revocation and household read permissions are checked before
delivery and during heartbeats. Tokens remain in Authorization headers. Async
dispatches finish requests that were authenticated on their initial dispatch.

## Deployment

Connections and presence are kept in memory for **one backend instance**. Multiple
instances require shared event distribution and presence storage before rollout.
No database migration or additional package dependency is required.

The reverse proxy must stream responses immediately: disable buffering for the
events route and allow a read timeout longer than 60 seconds. The endpoint sets
`X-Accel-Buffering: no` and `Cache-Control: no-store`. HTTP/2 is preferable when
several tabs are open. A server restart clears presence; clients reconnect.

## Review and manual verification

1. Open the same list as two household members in separate browser sessions.
   Both names should appear.
2. Add, rename, check and delete items, clear checked items, rename the list and
   add recipe ingredients. The other session should update without reloading.
3. Open a second tab for one member. Their name should still appear only once.
   Close one tab, then the last tab, and check the presence display.
4. Disconnect and restore the network. Check that the latest list state returns
   and no items are duplicated if a local write overlapped a notification.
5. Change filters and lists quickly, including the default-list route. A response
   for the previous list must not replace the current list.
6. Delete a viewed list or revoke a member's access/session. Their stream must stop;
   deleted lists must become unavailable in the UI.

Automated integration coverage checks authentication, household isolation,
commit/rollback delivery, list-specific routing, multiple-tab deduplication,
connection ownership and invalidation from an actual item write.
