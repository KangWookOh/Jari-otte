package com.eatpizzaquickly.jariotte.domain.coupon.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoupon is a Querydsl query type for Coupon
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoupon extends EntityPathBase<Coupon> {

    private static final long serialVersionUID = -2094032294L;

    public static final QCoupon coupon = new QCoupon("coupon");

    public final StringPath couponCode = createString("couponCode");

    public final StringPath couponName = createString("couponName");

    public final EnumPath<CouponType> couponType = createEnum("couponType", CouponType.class);

    public final NumberPath<Integer> discount = createNumber("discount", Integer.class);

    public final EnumPath<DiscountType> discountType = createEnum("discountType", DiscountType.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final SetPath<UserCoupon, QUserCoupon> userCoupons = this.<UserCoupon, QUserCoupon>createSet("userCoupons", UserCoupon.class, QUserCoupon.class, PathInits.DIRECT2);

    public QCoupon(String variable) {
        super(Coupon.class, forVariable(variable));
    }

    public QCoupon(Path<? extends Coupon> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoupon(PathMetadata metadata) {
        super(Coupon.class, metadata);
    }

}

