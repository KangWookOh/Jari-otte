package com.eatpizzaquickly.jariotte.domain.seat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeat is a Querydsl query type for Seat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeat extends EntityPathBase<Seat> {

    private static final long serialVersionUID = 1038640344L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeat seat = new QSeat("seat");

    public final com.eatpizzaquickly.jariotte.domain.concert.entity.QConcert concert;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isReserved = createBoolean("isReserved");

    public final NumberPath<Integer> seatNumber = createNumber("seatNumber", Integer.class);

    public QSeat(String variable) {
        this(Seat.class, forVariable(variable), INITS);
    }

    public QSeat(Path<? extends Seat> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeat(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeat(PathMetadata metadata, PathInits inits) {
        this(Seat.class, metadata, inits);
    }

    public QSeat(Class<? extends Seat> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.concert = inits.isInitialized("concert") ? new com.eatpizzaquickly.jariotte.domain.concert.entity.QConcert(forProperty("concert"), inits.get("concert")) : null;
    }

}

