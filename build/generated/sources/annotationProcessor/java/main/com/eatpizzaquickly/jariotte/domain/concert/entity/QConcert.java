package com.eatpizzaquickly.jariotte.domain.concert.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QConcert is a Querydsl query type for Concert
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QConcert extends EntityPathBase<Concert> {

    private static final long serialVersionUID = 225831156L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QConcert concert = new QConcert("concert");

    public final ListPath<String, StringPath> artists = this.<String, StringPath>createList("artists", String.class, StringPath.class, PathInits.DIRECT2);

    public final NumberPath<Integer> avgRating = createNumber("avgRating", Integer.class);

    public final EnumPath<Category> category = createEnum("category", Category.class);

    public final BooleanPath deleted = createBoolean("deleted");

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> endDate = createDateTime("endDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> seatCount = createNumber("seatCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    public final QVenue venue;

    public QConcert(String variable) {
        this(Concert.class, forVariable(variable), INITS);
    }

    public QConcert(Path<? extends Concert> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QConcert(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QConcert(PathMetadata metadata, PathInits inits) {
        this(Concert.class, metadata, inits);
    }

    public QConcert(Class<? extends Concert> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.venue = inits.isInitialized("venue") ? new QVenue(forProperty("venue")) : null;
    }

}

