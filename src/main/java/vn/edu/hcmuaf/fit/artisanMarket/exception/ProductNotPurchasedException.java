package vn.edu.hcmuaf.fit.artisanMarket.exception;

public class ProductNotPurchasedException extends RuntimeException {
    public ProductNotPurchasedException(String message) {
        super(message);
    }
}
