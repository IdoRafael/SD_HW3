package il.ac.technion.cs.sd.sub.app;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubscriptionTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Test
    public void basicGeneralTest() {
        Subscription subscription =
                new Subscription("bar1234,foo1234,100,0,1,1,0");

        assertTrue(subscription.wasSubscribed());
        assertTrue(subscription.wasCanceled());
        assertTrue(subscription.isCanceled());
        assertFalse(subscription.isSubscribed());
        assertEquals("bar1234", subscription.getUserId());
        assertEquals("foo1234", subscription.getJournalId());
        assertEquals((Long) 100L, subscription.getJournalPrice());
        assertEquals((Long) 0L, subscription.getJournalPriceIfSubscribed().orElse(0L));
    }

    @Test
    public void basicSubscribeTest() {
        Subscription subscription =
                new Subscription("bar1234,foo1234,100,0,1,1,0");

        assertTrue(subscription.wasSubscribed());
        assertTrue(subscription.wasCanceled());
        assertTrue(subscription.isCanceled());
        assertFalse(subscription.isSubscribed());
        assertEquals("bar1234", subscription.getUserId());
        assertEquals("foo1234", subscription.getJournalId());
        assertEquals((Long) 100L, subscription.getJournalPrice());
        assertEquals((Long) 0L, subscription.getJournalPriceIfSubscribed().orElse(0L));

        subscription.subscribe();

        assertTrue(subscription.wasSubscribed());
        assertTrue(subscription.wasCanceled());
        assertFalse(subscription.isCanceled());
        assertTrue(subscription.isSubscribed());
        assertEquals("bar1234", subscription.getUserId());
        assertEquals("foo1234", subscription.getJournalId());
        assertEquals((Long) 100L, subscription.getJournalPrice());
        assertEquals((Long) 100L, subscription.getJournalPriceIfSubscribed().orElse(0L));
    }

    @Test
    public void emptyHistoryShouldWork() {
        Subscription subscription =
                new Subscription("bar1234,foo1234,100");

        assertFalse(subscription.wasSubscribed());
        assertFalse(subscription.wasCanceled());
        assertFalse(subscription.isCanceled());
        assertFalse(subscription.isSubscribed());
        assertEquals("bar1234", subscription.getUserId());
        assertEquals("foo1234", subscription.getJournalId());
        assertEquals((Long) 100L, subscription.getJournalPrice());
        assertEquals((Long) 0L, subscription.getJournalPriceIfSubscribed().orElse(0L));
    }

    @Test
    public void cancelEmptyHistoryShouldCancel() {
        Subscription subscription =
                new Subscription("bar1234,foo1234,100");

        subscription.cancelIfNotCancelled();

        assertFalse(subscription.wasSubscribed());
        assertFalse(subscription.wasCanceled());
        assertFalse(subscription.isCanceled());
        assertFalse(subscription.isSubscribed());
        assertTrue(subscription.getHistory().contains(Subscription.Subscribed.CANCEL));
        assertEquals(1, subscription.getHistory().size());

        assertEquals((Long) 100L, subscription.getJournalPrice());
        assertEquals((Long) 0L, subscription.getJournalPriceIfSubscribed().orElse(0L));
    }

    @Test
    public void subscribeEmptyHistoryShouldSubscribe() {
        Subscription subscription =
                new Subscription("bar1234,foo1234,100");

        subscription.subscribe();

        assertTrue(subscription.wasSubscribed());
        assertFalse(subscription.wasCanceled());
        assertFalse(subscription.isCanceled());
        assertTrue(subscription.isSubscribed());

        assertEquals((Long) 100L, subscription.getJournalPrice());
        assertEquals((Long) 100L, subscription.getJournalPriceIfSubscribed().orElse(0L));
    }

    @Test
    public void cancelTwiceCancelsOnce() {
        Subscription subscription =
                new Subscription("bar1234,foo1234,100");

        subscription.cancelIfNotCancelled();
        subscription.cancelIfNotCancelled();

        assertFalse(subscription.wasSubscribed());
        assertFalse(subscription.wasCanceled());
        assertFalse(subscription.isCanceled());
        assertFalse(subscription.isSubscribed());

        assertEquals((Long) 100L, subscription.getJournalPrice());
        assertEquals((Long) 0L, subscription.getJournalPriceIfSubscribed().orElse(0L));

        assertTrue(subscription
                .getHistory()
                .contains(Subscription.Subscribed.CANCEL));

        assertEquals(1, subscription
                .getHistory()
                .size());
    }

    @Test
    public void historyTest() {
        Subscription subscription =
                new Subscription("bar1234,foo1234,100,0,1,1,0");

        subscription.cancelIfNotCancelled();
        subscription.subscribe();
        subscription.subscribe();
        subscription.cancelIfNotCancelled();
        subscription.cancelIfNotCancelled();
        subscription.cancelIfNotCancelled();
        subscription.subscribe();
        subscription.cancelIfNotCancelled();

        assertEquals("0,1,1,0,1,1,0,1,0", subscription
                .getHistory()
                .stream()
                .map(Subscription.Subscribed::toString)
                .collect(Collectors.joining(","))
        );

        assertTrue(subscription.wasSubscribed());
        assertTrue(subscription.wasCanceled());
        assertTrue(subscription.isCanceled());
        assertFalse(subscription.isSubscribed());
        assertEquals("bar1234", subscription.getUserId());
        assertEquals("foo1234", subscription.getJournalId());
        assertEquals((Long) 100L, subscription.getJournalPrice());
        assertEquals((Long) 0L, subscription.getJournalPriceIfSubscribed().orElse(0L));

        subscription.subscribe();

        assertEquals((Long) 100L, subscription.getJournalPriceIfSubscribed().orElse(0L));
    }
}
