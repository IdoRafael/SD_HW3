
package il.ac.technion.cs.sd.sub.app;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Subscription {
    private String userId;
    private String journalId;
    private Long journalPrice;
    private List<Subscribed> history;

    public enum Subscribed {
        CANCEL(false), SUBSCRIBE(true);

        private String value;

        Subscribed(Boolean value) {
            this.value = value ? "1" : "0";
        }

        @Override
        public String toString() {
            return value;
        }

        public Boolean toBoolean() {
            return value.equals("1");
        }

        public static Subscribed fromString(String s) {
            return s.equals("0") ? Subscribed.CANCEL : Subscribed.SUBSCRIBE;
        }
    }

    public Subscription(String csvString) {
        String[] splitString = csvString.split(",");
        this.userId = splitString[0];
        this.journalId = splitString[1];
        this.journalPrice = Long.parseLong(splitString[2]);
        history = Arrays.stream(splitString)
                .skip(3)
                .map(Subscribed::fromString)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.join(
                ",",
                userId,
                journalId,
                journalPrice.toString(),
                history
                        .stream()
                        .map(Subscribed::toString)
                        .collect(Collectors.joining(","))
        );
    }

    public Boolean isSubscribed() {
        return (!history.isEmpty())
                && (history.get(history.size() - 1).equals(Subscribed.SUBSCRIBE));
    }

    public Boolean wasSubscribed() {
        return (!history.isEmpty())
                && (history.contains(Subscribed.SUBSCRIBE));
    }

    public Boolean isCanceled() {
        return (!history.isEmpty())
                && (history.contains(Subscribed.SUBSCRIBE))
                && (history.get(history.size() - 1).equals(Subscribed.CANCEL));
    }

    public Boolean wasCanceled() {
        boolean isNonEmptyContainingCancelAndSub =
                (!history.isEmpty())
                && (history.contains(Subscribed.SUBSCRIBE))
                && (history.contains(Subscribed.CANCEL));

        if (isNonEmptyContainingCancelAndSub) {
            for (int i = 0 ; i < history.size() - 1 ; ++i) {
                if (history.get(i).equals(Subscribed.SUBSCRIBE)
                        && history.get(i+1).equals(Subscribed.CANCEL)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getJournalId() {
        return journalId;
    }

    public Long getJournalPrice() {
        return journalPrice;
    }

    public Optional<Long> getJournalPriceIfSubscribed() {
        return isSubscribed() ? Optional.of(journalPrice) : Optional.empty();
    }

    public List<Subscribed> getHistory() {
        return history;
    }

    public void subscribe() {
        history.add(Subscribed.SUBSCRIBE);
    }

    public void cancelIfNotCancelled() {
        if (history.isEmpty()
                || history.get(history.size() - 1).equals(Subscribed.SUBSCRIBE)) {
            history.add(Subscribed.CANCEL);
        }
    }
}

