package il.ac.technion.cs.sd.sub.app;

import java.util.ArrayList;
import java.util.List;

public class Subscription {
    private String userId;
    private String productId;
    private Long productPrice;
    private Long latestAmount;
    private boolean isCancelled;
    private boolean isModified;
    private List<Long> amountHistory = new ArrayList<>();

    public Subscription(){

    }
    /*
    public Subscription(String orderId, String userId, String productId, Long initialAmount, Long productPrice) {
        this.userId = userId;
        this.productId = productId;
        this.latestAmount = initialAmount;
        this.productPrice = productPrice;

        this.isCancelled = false;
        this.isModified = false;
        amountHistory.add(initialAmount);
    }

    public Subscription(String csvString) {
        String[] splitString = csvString.split(",");
        this.userId = splitString[1];
        this.productId = splitString[2];
        this.latestAmount = Long.parseLong(splitString[3]);
        this.productPrice = Long.parseLong(splitString[4]);
        this.isCancelled = stringToBoolean(splitString[5]);
        this.isModified = stringToBoolean(splitString[6]);

        amountHistory.add(latestAmount);
    }

    @Override
    public String toString() {
        return String.join(
                ",",
                userId,
                productId,
                latestAmount.toString(),
                productPrice.toString(),
                serializeBoolean(isCancelled()),
                serializeBoolean(isModified())
        );
    }


    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public Long getProductPrice() {
        return productPrice;
    }

    public Long getLatestAmount() {
        return latestAmount;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isModified() {
        return isModified || amountHistory.size() > 1;
    }

    public List<Long> getAmountHistory() {
        return amountHistory;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    public void modifyAmount(Long newAmount) {
        latestAmount = newAmount;
        amountHistory.add(newAmount);
        setCancelled(false);
    }

    private String serializeBoolean(Boolean b) {
        return b ? "1" : "0";
    }

    private Boolean stringToBoolean(String s) {
        return s.equals("1");
    }*/
}
