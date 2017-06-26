package il.ac.technion.cs.sd.sub.app;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;


/** This class will only be instantiated by Guice after one of the setup methods has been called. */
public interface SubscriberReader {
  /** Returns true if the user is currently subscribed to the journal. If the user is not found, returns empty. */
  CompletableFuture<Optional<Boolean>> isSubscribed(String userId, String journalId);
  /**
   * Returns true if the user was ever, but not necessarily currently, subscribed to the journal.
   * If the user is not found, returns empty.
   */
  CompletableFuture<Optional<Boolean>> wasSubscribed(String userId, String journalId);
  /**
   * Returns true if the user was subscribed to the journal, but is not anymore.
   * If the user is not found, returns empty.
   */
  CompletableFuture<Optional<Boolean>> isCanceled(String userId, String journalId);
  /**
   * Returns true if the user was subscribed to the journal, and then canceled his subscription. It's possible that the
   * the user later re-subscribed journal. If the user is not found, returns empty.
   */
  CompletableFuture<Optional<Boolean>> wasCanceled(String userId, String journalId);

  /**
   * Returns all the journals the user is <b>currently</b> subscribed to, ordered lexicographically.
   * If the user is not found, returns an empty list.
   */
  CompletableFuture<List<String>> getSubscribedJournals(String userId);
  /**
   * Returns a map from a journal ID to its history of subscriptions. A history is a list of boolean values, where true
   * means subscription, and false means cancellation. Multiple subsequent cancellation should only be counted once,
   * but subsequent subscriptions should be counted individually. For example, if the input for the user and journal
   * is "subscribed, cancelled, subscribed, subscribed, cancelled, cancelled, subscribed", the values in the list should
   * be "true, false, true, true, false, true". If the user is not found, returns an empty map.
   */
  CompletableFuture<Map<String, List<Boolean>>> getAllSubscriptions(String userId);
  /**
   * Returns the total amount of money spent by the user on their <b>active</b> subscriptions, i.e., the price of
   * each currently subscribed journal. If the user is not found, returns empty.
   */
  CompletableFuture<OptionalInt> getMonthlyBudget(String userId);

  /**
   * Returns all the users that are <b>currently</b> subscribed to this journal, ordered lexicographically.
   * If the journal is not, found returns an empty list.
   */
  CompletableFuture<List<String>> getSubscribedUsers(String journalId);
  /**
   * Returns the total amount of money earned by the journal from their <b>active</b> subscriptions, i.e., the price
   * of the journal times its number of subscribes. If the journal is not found, returns empty.
   */
  CompletableFuture<OptionalInt> getMonthlyIncome(String journalId);
  /**
   * Returns a map from a user ID to their history of subscriptions. A history is a list of boolean values, where true
   * means subscription, and false means cancellation. Multiple subsequent cancellation should only be counted once,
   * but subsequent subscriptions should be counted individually. For example, if the input for the user and journal
   * is "subscribed, cancelled, subscribed, subscribed, cancelled, cancelled, subscribed", the values in the list should
   * be "true, false, true, true, false, true". If the journal is not found, returns an empty map.
   */
  CompletableFuture<Map<String, List<Boolean>>> getSubscribers(String journalId);
}
