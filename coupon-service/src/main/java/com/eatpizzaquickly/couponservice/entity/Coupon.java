package com.eatpizzaquickly.couponservice.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    private String couponCode;

    private String couponName;

    private CouponType couponType;

    private DiscountType discountType;

    private int discount = 0;

    private int price = 0;

    private int quantity = 0;

    private  Boolean isActive = true;

    private Long userId;


    @Builder
    private Coupon(String couponCode, String couponName, CouponType couponType,DiscountType discountType ,int discount, int price, int quantity) {
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.couponType = couponType;
        this.discountType = discountType;
        this.discount = discount;
        this.price = price;
        this.quantity = quantity;
    }

    public void decreaseQuantity() {
        if (this.quantity > 0) {
            this.quantity--;
        } else {
            throw new IllegalStateException("수량이 0인 쿠폰입니다.");
        }
    }

    public void decreaseQuantity(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
        } else {
            throw new IllegalStateException("발급할 수량이 부족합니다.");
        }
    }

    public void checkIsActive(){
        this.isActive = false;

    }

    public void updateUserId(Long userId){
        this.userId = userId;
    }

}
