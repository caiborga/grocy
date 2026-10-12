import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { stripTypeScriptTypes } from 'node:module';

const source = readFileSync(new URL('../src/stores/activityStore.ts', import.meta.url), 'utf8')
    .replace(/^import .*;\r?\n/gm, '').replace('export const useActivityStore', 'const useActivityStore');
const createStore = new Function('defineStore', 'activityService',
    stripTypeScriptTypes(source) + '\nreturn useActivityStore();');
const event = (id, type = 'ITEM_ADDED', unread = true) => ({ id, type, unread });
const page = (events, hasMore = false, unreadCount = events.filter(e => e.unread).length) =>
    ({ data: { events, hasMore, nextCursor: events.at(-1)?.id ?? null, unreadCount } });
function fixture(service) {
    const reads = [];
    const store = createStore((_id, options) => () => {
        const state = options.state();
        for (const [name, action] of Object.entries(options.actions)) state[name] = action.bind(state);
        return state;
    }, { markRead: async (_id, ids) => { reads.push(ids); }, ...service });
    Object.assign(store, { householdId: 'household', panelOpen: true });
    return { store, reads };
}

test('an open panel receives uncheck and add events without losing older history', async () => {
    const checked = event('checked', 'ITEM_CHECKED', false);
    const older = event('older', 'ITEM_UPDATED', false);
    const { store, reads } = fixture({ getFeed: async () => page([
        event('added'), event('reopened', 'ITEM_UNCHECKED'), checked
    ], true, 2) });
    Object.assign(store, { events: [checked, older], hasMore: true, nextCursor: 'older' });
    await store.refreshUnread();
    assert.deepEqual(store.events.map(e => e.id), ['added', 'reopened', 'checked', 'older']);
    assert.equal(store.nextCursor, 'older');
    assert.equal(store.hasMore, true);
    assert.equal(store.unreadCount, 0);
    assert.deepEqual(reads, [['added', 'reopened']]);
    await store.refreshUnread();
    assert.equal(store.events.length, 4);
    assert.equal(reads.length, 1);
});

test('missed changes spanning multiple pages are recovered in order', async () => {
    const fresh = Array.from({ length: 25 }, (_, i) => event(`new-${i}`));
    const requests = [];
    const { store, reads } = fixture({ getFeed: async (_id, before, older) => {
        requests.push([before, older]);
        return before ? page([...fresh.slice(20), event('known', 'ITEM_CHECKED', false)], false, 25)
            : page(fresh.slice(0, 20), true, 25);
    } });
    store.events = [event('known', 'ITEM_CHECKED', false)];
    await store.refreshUnread();
    assert.deepEqual(store.events.map(e => e.id), [...fresh.map(e => e.id), 'known']);
    assert.deepEqual(requests, [[null, false], ['new-19', true]]);
    assert.deepEqual(reads.map(ids => ids.length), [20, 5]);
});

test('a second live event during a request triggers a follow-up refresh', async () => {
    let resolveFirst;
    let calls = 0;
    const { store } = fixture({ getFeed: async () => {
        calls++;
        if (calls === 1) return new Promise(resolve => { resolveFirst = resolve; });
        return page([event('second'), event('first'), event('known', 'ITEM_CHECKED', false)], false, 1);
    } });
    store.events = [event('known', 'ITEM_CHECKED', false)];
    const pending = store.refreshUnread();
    await store.refreshUnread();
    resolveFirst(page([event('first'), event('known', 'ITEM_CHECKED', false)]));
    await pending;
    await new Promise(resolve => setImmediate(resolve));
    assert.equal(calls, 2);
    assert.deepEqual(store.events.map(e => e.id), ['second', 'first', 'known']);
});

test('closed panels refresh only the badge', async () => {
    const { store } = fixture({ getUnreadCount: async () => ({ data: 3 }),
        getFeed: async () => { throw new Error('Feed must not be requested'); } });
    store.panelOpen = false;
    await store.refreshUnread();
    assert.equal(store.unreadCount, 3);
    assert.deepEqual(store.events, []);
});

test('closing the panel or switching households discards a pending feed response', async () => {
    for (const reset of [false, true]) {
        let resolveFeed;
        const { store, reads } = fixture({ getFeed: async () => new Promise(resolve => { resolveFeed = resolve; }) });
        const pending = store.refreshUnread();
        if (reset) store.reset(); else store.panelOpen = false;
        resolveFeed(page([event('late')]));
        await pending;
        assert.deepEqual(store.events, []);
        assert.deepEqual(reads, []);
    }
});
