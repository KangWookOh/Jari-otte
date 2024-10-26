package com.eatpizzaquickly.jariotte.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1786894492L;

    public static final QUser user = new QUser("user");

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDelete = createBoolean("isDelete");

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final SetPath<com.eatpizzaquickly.jariotte.domain.coupon.entity.UserCoupon, com.eatpizzaquickly.jariotte.domain.coupon.entity.QUserCoupon> userCoupons = this.<com.eatpizzaquickly.jariotte.domain.coupon.entity.UserCoupon, com.eatpizzaquickly.jariotte.domain.coupon.entity.QUserCoupon>createSet("userCoupons", com.eatpizzaquickly.jariotte.domain.coupon.entity.UserCoupon.class, com.eatpizzaquickly.jariotte.domain.coupon.entity.QUserCoupon.class, PathInits.DIRECT2);

    public final EnumPath<UserRole> userRole = createEnum("userRole", UserRole.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

